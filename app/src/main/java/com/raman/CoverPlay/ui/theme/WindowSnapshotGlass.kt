package com.raman.CoverPlay.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.PixelCopy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionOnScreen
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

@Stable
class WindowSnapshot internal constructor(
    internal val image: ImageBitmap,
    internal val origin: IntOffset,
    internal val size: IntSize,
)

private const val SNAPSHOT_DOWNSCALE = 6

private const val SNAPSHOT_GLASS_ENABLED = false

@Composable
fun rememberCoverPlayWindowSnapshot(enabled: Boolean = true): WindowSnapshot? {
    val context = LocalContext.current
    var snapshot by remember { mutableStateOf<WindowSnapshot?>(null) }
    LaunchedEffect(enabled) {
        if (!SNAPSHOT_GLASS_ENABLED || !enabled || Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return@LaunchedEffect
        val window = context.snapshotHostWindow() ?: return@LaunchedEffect
        val decor = window.decorView
        if (decor.width <= 0 || decor.height <= 0) return@LaunchedEffect
        val target = Bitmap.createBitmap(
            (decor.width / SNAPSHOT_DOWNSCALE).coerceAtLeast(1),
            (decor.height / SNAPSHOT_DOWNSCALE).coerceAtLeast(1),
            Bitmap.Config.ARGB_8888
        )
        val result = suspendCancellableCoroutine<Int> { cont ->
            PixelCopy.request(window, target, { code -> cont.resume(code) }, Handler(Looper.getMainLooper()))
        }
        if (result != PixelCopy.SUCCESS) return@LaunchedEffect
        val xy = IntArray(2).also { decor.getLocationOnScreen(it) }
        snapshot = WindowSnapshot(target.asImageBitmap(), IntOffset(xy[0], xy[1]), IntSize(decor.width, decor.height))
    }
    return snapshot
}

@Composable
fun Modifier.snapshotGlass(snapshot: WindowSnapshot?, blurPx: Float = 36f): Modifier {
    if (snapshot == null) return this
    var nodeOnScreen by remember { mutableStateOf(Offset.Zero) }
    return this
        .onGloballyPositioned { nodeOnScreen = it.positionOnScreen() }
        .graphicsLayer {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                renderEffect = BlurEffect(blurPx, blurPx, TileMode.Clamp)
            }
            clip = true
        }
        .drawBehind {
            drawImage(
                image = snapshot.image,
                dstOffset = IntOffset(
                    (snapshot.origin.x - nodeOnScreen.x).toInt(),
                    (snapshot.origin.y - nodeOnScreen.y).toInt()
                ),
                dstSize = snapshot.size,
                filterQuality = FilterQuality.Medium
            )
        }
}

private tailrec fun Context.snapshotHostWindow(): android.view.Window? = when (this) {
    is Activity -> window
    is ContextWrapper -> baseContext.snapshotHostWindow()
    else -> null
}
