package com.raman.CoverPlay.data

import android.app.RecoverableSecurityException
import android.content.ContentValues
import android.content.Context
import android.content.IntentSender
import android.os.Build
import android.provider.MediaStore
import com.raman.CoverPlay.VideoFile
import java.io.File

sealed class FileOpResult {
    object Success : FileOpResult()

    data class Error(val message: String) : FileOpResult()

    data class NeedsUserConsent(val intentSender: IntentSender) : FileOpResult()
}

fun renameVideoFile(context: Context, video: VideoFile, newNameWithoutExtension: String): FileOpResult {
    val oldFile = File(video.path)
    val extension = oldFile.extension
    val sanitized = newNameWithoutExtension.trim()
    if (sanitized.isEmpty()) return FileOpResult.Error("Name can't be empty")
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
            val values = ContentValues().apply {
                put(MediaStore.Video.Media.DISPLAY_NAME, newFileName)
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
        FileOpResult.Error(e.message ?: "Couldn't rename the file")
    }
}

fun deleteVideoFile(context: Context, video: VideoFile): FileOpResult {
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val pendingIntent = MediaStore.createDeleteRequest(
                context.contentResolver,
                listOf(video.uri)
            )
            FileOpResult.NeedsUserConsent(pendingIntent.intentSender)
        } else {
            val rows = context.contentResolver.delete(video.uri, null, null)
            if (rows > 0) {
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
            val relativePath = destinationFolderPath
                .substringAfter("Android/media/", destinationFolderPath)
                .let { path ->
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
