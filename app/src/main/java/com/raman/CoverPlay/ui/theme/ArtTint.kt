package com.raman.CoverPlay.ui.theme

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.drawable.toBitmap
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import coil.request.videoFrameMillis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object AmbientTint {
    var color: Color by mutableStateOf(Color.Unspecified)
}

@Composable
fun CoverPlayProvideArtTint(model: Any?) {
    val context = LocalContext.current
    LaunchedEffect(model) {
        AmbientTint.color = if (model == null) Color.Unspecified
        else sampleArtTint(context, model) ?: Color.Unspecified
    }
    DisposableEffect(Unit) { onDispose { AmbientTint.color = Color.Unspecified } }
}

suspend fun sampleArtTint(context: Context, model: Any?): Color? {
    if (model == null) return null
    val request = ImageRequest.Builder(context)
        .data(model)
        .size(32, 32)
        .allowHardware(false)
        .videoFrameMillis(1000)
        .build()
    val result = runCatching { context.imageLoader.execute(request) }.getOrNull() as? SuccessResult
        ?: return null
    return withContext(Dispatchers.Default) {
        val bmp = runCatching { result.drawable.toBitmap(32, 32) }.getOrNull() ?: return@withContext null
        val w = bmp.width
        val h = bmp.height
        val px = IntArray(w * h)
        bmp.getPixels(px, 0, w, 0, 0, w, h)
        var rs = 0.0; var gs = 0.0; var bs = 0.0; var ws = 0.0
        for (p in px) {
            val r = (p shr 16) and 0xFF
            val g = (p shr 8) and 0xFF
            val b = p and 0xFF
            val mx = maxOf(r, g, b)
            val mn = minOf(r, g, b)
            if (mx == 0) continue
            val sat = (mx - mn) / mx.toDouble()
            val lum = mx / 255.0
            val wt = (sat * sat + 0.02) * (if (lum < 0.12) 0.15 else 1.0)
            rs += r * wt; gs += g * wt; bs += b * wt; ws += wt
        }
        if (ws <= 0.0) return@withContext null
        val argb = android.graphics.Color.rgb((rs / ws).toInt(), (gs / ws).toInt(), (bs / ws).toInt())
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(argb, hsv)
        hsv[1] = hsv[1].coerceIn(0.42f, 0.82f)
        hsv[2] = hsv[2].coerceIn(0.58f, 0.92f)
        Color(android.graphics.Color.HSVToColor(hsv))
    }
}
