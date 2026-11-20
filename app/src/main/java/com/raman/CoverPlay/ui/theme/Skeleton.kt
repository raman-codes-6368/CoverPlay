package com.raman.CoverPlay.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.findRootCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import kotlinx.coroutines.flow.collectLatest

object SkeletonClock {
    var progress by mutableFloatStateOf(0f)
    var consumers by mutableIntStateOf(0)
}

@Composable
internal fun CoverPlaySkeletonClockEffect() {
    LaunchedEffect(Unit) {
        snapshotFlow { SkeletonClock.consumers > 0 }.collectLatest { active ->
            if (!active) return@collectLatest
            var start = -1L
            while (true) {
                androidx.compose.runtime.withFrameMillis { now ->
                    if (start < 0L) start = now
                    SkeletonClock.progress = ((now - start) % 1900L) / 1900f
                }
            }
        }
    }
}

@Composable
fun Modifier.skeleton(active: Boolean = true, shape: Shape = RectangleShape): Modifier {
    if (!active) return this
    val p = LocalAppUiSettings.current.palette
    val base = p.containerHigh
    val shoulder = remember(base) { lerp(base, Color.White, 0.045f) }
    val peak = remember(base) { lerp(base, Color.White, 0.11f) }
    var rootWidth by remember { mutableFloatStateOf(0f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    DisposableEffect(Unit) {
        SkeletonClock.consumers++
        onDispose { SkeletonClock.consumers-- }
    }
    return this
        .clip(shape)
        .onGloballyPositioned { c ->
            rootWidth = c.findRootCoordinates().size.width.toFloat()
            offsetX = c.positionInRoot().x
        }
        .drawBehind {
            val width = if (rootWidth > 0f) rootWidth else size.width
            val band = width * 0.7f
            val start = (width + band) * SkeletonClock.progress - band - offsetX
            drawRect(base)
            drawRect(
                Brush.linearGradient(
                    colorStops = arrayOf(0f to base, 0.3f to shoulder, 0.5f to peak, 0.7f to shoulder, 1f to base),
                    start = Offset(start, 0f),
                    end = Offset(start + band, 0f),
                )
            )
        }
}
