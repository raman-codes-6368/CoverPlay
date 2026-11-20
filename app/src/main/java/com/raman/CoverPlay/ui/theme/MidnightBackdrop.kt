package com.raman.CoverPlay.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.lerp

private val BackdropEase = CubicBezierEasing(0.2f, 0f, 0f, 1f)

@Composable
fun CoverPlayMidnightBackdrop(
    accent: Color,
    highlight: Color,
    secondary: Color,
    base: Color,
    modifier: Modifier = Modifier,
    intensity: Float = 1f,
) {
    val art = AmbientTint.color
    val keyTarget = if (art.isSpecified) lerp(accent, art, 0.72f) else accent
    val rimTarget = if (art.isSpecified) lerp(highlight, art, 0.35f) else highlight
    val key = animateColorAsState(keyTarget, tween(700, easing = BackdropEase), label = "backdropKey")
    val rim = animateColorAsState(rimTarget, tween(700, easing = BackdropEase), label = "backdropRim")
    val grain = rememberCoverPlayGrainBrush()

    Box(modifier.fillMaxSize()) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val reach = maxOf(w, h)
            val k = key.value
            val r = rim.value

            drawRect(lerp(base, k, 0.028f))

            drawRect(
                Brush.radialGradient(
                    0f to k.copy(alpha = 0.34f * intensity),
                    0.38f to k.copy(alpha = 0.15f * intensity),
                    0.72f to k.copy(alpha = 0.04f * intensity),
                    1f to Color.Transparent,
                    center = Offset(w * -0.06f, h * -0.02f),
                    radius = reach * 0.78f,
                )
            )
            drawRect(
                Brush.radialGradient(
                    0f to r.copy(alpha = 0.11f * intensity),
                    0.55f to r.copy(alpha = 0.03f * intensity),
                    1f to Color.Transparent,
                    center = Offset(w * 1.02f, h * 0.20f),
                    radius = reach * 0.42f,
                )
            )
            drawRect(
                Brush.radialGradient(
                    0f to secondary.copy(alpha = 0.10f * intensity),
                    0.6f to secondary.copy(alpha = 0.025f * intensity),
                    1f to Color.Transparent,
                    center = Offset(w * 1.05f, h * 0.68f),
                    radius = reach * 0.55f,
                )
            )
            drawRect(
                Brush.radialGradient(
                    0f to k.copy(alpha = 0.11f * intensity),
                    1f to Color.Transparent,
                    center = Offset(w * 0.5f, h * 1.06f),
                    radius = w * 0.95f,
                )
            )
            drawRect(
                Brush.verticalGradient(
                    0f to Color.Transparent,
                    0.55f to Color.Transparent,
                    1f to base.copy(alpha = 0.42f),
                )
            )
            drawRect(grain)
        }
    }
}
