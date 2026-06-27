package com.example.myapplication

import android.app.Activity
import android.app.RecoverableSecurityException
import android.content.ContentValues
import android.content.Context
import android.content.IntentSender
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import java.io.File

// ═════════════════════════════════════════════════════════════════════════════
// FILE OPERATIONS — rename / delete / move for video files
//
// These all go through MediaStore, not raw java.io.File, because the app only
// holds READ_MEDIA_VIDEO / READ_EXTERNAL_STORAGE (see MainActivity's
// permission launcher) -- there's no WRITE_EXTERNAL_STORAGE or
// MANAGE_EXTERNAL_STORAGE. Under scoped storage (Android 10+), writing to or
// deleting a file the app didn't create itself requires going through
// MediaStore's ContentResolver, which the system mediates with its own
// permission grant (sometimes a one-time user confirmation) rather than a
// blanket storage permission.
//
// Three different OS eras need three different strategies, so each function
// below tries the simple path first and only escalates if the OS rejects it:
//
//  - API < 29 (Q): plain java.io.File rename/delete works directly once the
//    legacy storage permission is held, then MediaStore is poked to re-scan.
//  - API 29 (Q): ContentResolver.update()/delete() works UNLESS the row was
//    written by another app, in which case it throws
//    RecoverableSecurityException, carrying an IntentSender the caller can
//    launch to get a one-time user grant for that specific file.
//  - API 30+ (R+): same idea, but the proper API is
//    MediaStore.createWriteRequest / createDeleteRequest, which batches this
//    into a single system dialog and works reliably across OEM skins (some
//    of which never threw RecoverableSecurityException correctly on Q).
// ═════════════════════════════════════════════════════════════════════════════

/** Outcome of a file operation that might need the user to confirm via a system dialog. */
sealed class FileOpResult {
    /** Operation completed immediately, no further action needed. */
    object Success : FileOpResult()

    /** Operation failed for a reason unrelated to scoped-storage permission (e.g. name collision, I/O error). */
    data class Error(val message: String) : FileOpResult()

    /**
     * The OS needs a one-time user confirmation before this op can proceed.
     * Launch [intentSender] via an IntentSenderRequest, then retry the same
     * call (the retry will succeed because the grant is now in place for
     * this content URI).
     */
    data class NeedsUserConsent(val intentSender: IntentSender) : FileOpResult()
}

/**
 * Renames a video in place (same folder, new display name) and keeps
 * MediaStore's row in sync so the change is reflected everywhere in the app
 * immediately, without waiting for a media scan.
 */
fun renameVideoFile(context: Context, video: VideoFile, newNameWithoutExtension: String): FileOpResult {
    val oldFile = File(video.path)
    val extension = oldFile.extension
    val sanitized = newNameWithoutExtension.trim()
    if (sanitized.isEmpty()) return FileOpResult.Error("Name can't be empty")
    // Guard against path separators sneaking a rename into a move -- this
    // function only ever renames within the same parent directory.
    if (sanitized.contains("/") || sanitized.contains("\\")) {
        return FileOpResult.Error("Name can't contain / or \\")
    }
    val newFileName = if (extension.isNotEmpty()) "$sanitized.$extension" else sanitized
    val newFile = File(oldFile.parentFile, newFileName)

    if (newFile.exists()) {
        return FileOpResult.Error("A file named \"$newFileName\" already exists here")
    }

    return try {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            // Legacy storage: plain rename, then nudge MediaStore to notice.
            if (oldFile.renameTo(newFile)) {
                val values = ContentValues().apply {
                    put(MediaStore.Video.Media.DISPLAY_NAME, newFileName)
                    put(MediaStore.Video.Media.DATA, newFile.absolutePath)
                }
                context.contentResolver.update(video.uri, values, null, null)
                FileOpResult.Success
            } else {
                FileOpResult.Error("Couldn't rename the file")
            }
        } else {
            // Scoped storage: ask MediaStore to do it. This alone moves the
            // file on disk to match, on API 29+ -- no separate File.renameTo
            // needed (and it would fail anyway without write access).
            val values = ContentValues().apply {
                put(MediaStore.Video.Media.DISPLAY_NAME, newFileName)
            }
            context.contentResolver.update(video.uri, values, null, null)
            FileOpResult.Success
        }
    } catch (e: RecoverableSecurityException) {
        FileOpResult.NeedsUserConsent(e.userAction.actionIntent.intentSender)
    } catch (e: SecurityException) {
        // API 30+ ContentResolver.update on a file this app doesn't own
        // throws a plain SecurityException rather than the recoverable
        // variant; the createWriteRequest path is the documented fix.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val pendingIntent = MediaStore.createWriteRequest(
                context.contentResolver,
                listOf(video.uri)
            )
            FileOpResult.NeedsUserConsent(pendingIntent.intentSender)
        } else {
            FileOpResult.Error(e.message ?: "Permission denied")
        }
    } catch (e: Exception) {
        FileOpResult.Error(e.message ?: "Couldn't rename the file")
    }
}

/**
 * Deletes a video from disk and from MediaStore.
 */
fun deleteVideoFile(context: Context, video: VideoFile): FileOpResult {
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // createDeleteRequest is the documented, OEM-reliable path on R+
            // and handles the system confirmation dialog itself.
            val pendingIntent = MediaStore.createDeleteRequest(
                context.contentResolver,
                listOf(video.uri)
            )
            FileOpResult.NeedsUserConsent(pendingIntent.intentSender)
        } else {
            val rows = context.contentResolver.delete(video.uri, null, null)
            if (rows > 0) {
                // Belt-and-suspenders: also remove the file directly in case
                // the MediaStore delete didn't reach the underlying file on
                // this OEM's storage layer.
                runCatching { File(video.path).delete() }
                FileOpResult.Success
            } else {
                FileOpResult.Error("Couldn't delete the file")
            }
        }
    } catch (e: RecoverableSecurityException) {
        FileOpResult.NeedsUserConsent(e.userAction.actionIntent.intentSender)
    } catch (e: Exception) {
        FileOpResult.Error(e.message ?: "Couldn't delete the file")
    }
}

/**
 * Moves a video to a different folder (by absolute folder path, matching
 * VideoFolder.path as already modeled in the app). Keeps the same display
 * name; fails with a clear error on a name collision in the destination
 * rather than silently overwriting.
 */
fun moveVideoFile(context: Context, video: VideoFile, destinationFolderPath: String): FileOpResult {
    val oldFile = File(video.path)
    val destinationDir = File(destinationFolderPath)
    val newFile = File(destinationDir, oldFile.name)

    if (oldFile.parent == destinationFolderPath) {
        return FileOpResult.Error("Already in that folder")
    }
    if (newFile.exists()) {
        return FileOpResult.Error("A file named \"${oldFile.name}\" already exists in that folder")
    }

    return try {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (!destinationDir.exists()) destinationDir.mkdirs()
            if (oldFile.renameTo(newFile)) {
                val values = ContentValues().apply {
                    put(MediaStore.Video.Media.DATA, newFile.absolutePath)
                }
                context.contentResolver.update(video.uri, values, null, null)
                FileOpResult.Success
            } else {
                FileOpResult.Error("Couldn't move the file")
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // RELATIVE_PATH is how MediaStore models "which folder" under
            // scoped storage on R+; updating it physically relocates the
            // file. destinationFolderPath comes from VideoFolder.path, an
            // absolute path -- MediaStore wants it relative to the public
            // root (e.g. "Movies/MyFolder"), so strip the common storage
            // prefix.
            val relativePath = destinationFolderPath
                .substringAfter("Android/media/", destinationFolderPath)
                .let { path ->
                    // Strip everything up through ".../storage/emulated/0/"
                    // (or equivalent), leaving the public-root-relative tail.
                    val marker = Regex("/storage/[^/]+/[0-9]+/")
                    val match = marker.find(destinationFolderPath)
                    if (match != null) destinationFolderPath.substring(match.range.last + 1) else path
                }
            val values = ContentValues().apply {
                put(MediaStore.Video.Media.RELATIVE_PATH, "$relativePath/")
            }
            context.contentResolver.update(video.uri, values, null, null)
            FileOpResult.Success
        } else {
            // API 29: try the direct update; on failure to a file we don't
            // own, surface the recoverable consent flow like the others.
            if (!destinationDir.exists()) destinationDir.mkdirs()
            val values = ContentValues().apply {
                put(MediaStore.Video.Media.DATA, newFile.absolutePath)
            }
            context.contentResolver.update(video.uri, values, null, null)
            FileOpResult.Success
        }
    } catch (e: RecoverableSecurityException) {
        FileOpResult.NeedsUserConsent(e.userAction.actionIntent.intentSender)
    } catch (e: SecurityException) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val pendingIntent = MediaStore.createWriteRequest(
                context.contentResolver,
                listOf(video.uri)
            )
            FileOpResult.NeedsUserConsent(pendingIntent.intentSender)
        } else {
            FileOpResult.Error(e.message ?: "Permission denied")
        }
    } catch (e: Exception) {
        FileOpResult.Error(e.message ?: "Couldn't move the file")
    }
}
