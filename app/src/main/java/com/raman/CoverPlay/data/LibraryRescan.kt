package com.raman.CoverPlay.data

import android.content.Context
import android.media.MediaScannerConnection
import android.os.Environment
import java.io.File
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull

private val VideoExtensions = setOf(
    "mp4", "mkv", "webm", "avi", "mov", "m4v", "3gp", "flv", "ts", "wmv", "mpg", "mpeg", "m2ts", "ogv"
)

suspend fun rescanDeviceFolders(context: Context) {
    val roots = LinkedHashSet<File>()
    Environment.getExternalStorageDirectory()?.let { roots += it }
    context.getExternalFilesDirs(null).forEach { dir ->
        dir?.absolutePath?.substringBefore("/Android")?.let { roots += File(it) }
    }

    val found = ArrayList<String>()
    fun walk(dir: File, depth: Int) {
        if (depth > 8) return
        val kids = dir.listFiles() ?: return
        for (f in kids) {
            if (f.name.startsWith(".")) continue
            if (f.isDirectory) {
                if (depth == 0 && f.name == "Android") continue
                walk(f, depth + 1)
            } else if (f.extension.lowercase() in VideoExtensions) {
                found += f.absolutePath
            }
        }
    }
    roots.filter { it.exists() }.forEach { walk(it, 0) }
    if (found.isEmpty()) return

    withTimeoutOrNull(30_000) {
        suspendCancellableCoroutine<Unit> { cont ->
            var remaining = found.size
            MediaScannerConnection.scanFile(context, found.toTypedArray(), null) { _, _ ->
                val done = synchronized(found) { --remaining <= 0 }
                if (done && cont.isActive) cont.resume(Unit)
            }
        }
    }
}
