package com.raman.CoverPlay.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState

object AppChrome {
    private val _visible = mutableStateOf(true)
    var visible: Boolean
        get() = _visible.value
        set(v) { _visible.value = v }

    fun reveal() { visible = true }
}

@Composable
fun rememberCoverPlayChromeScrollConnection(@Suppress("UNUSED_PARAMETER") threshold: Dp = 24.dp): NestedScrollConnection {
    val enabled by rememberUpdatedState(LocalAppUiSettings.current.hideTopBarOnScroll)
    return remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (!enabled) {
                    if (!AppChrome.visible) AppChrome.reveal()
                    return Offset.Zero
                }
                if (available.y < -6f) AppChrome.visible = false
                else if (available.y > 6f) AppChrome.visible = true
                return Offset.Zero
            }
        }
    }
}

@Composable
fun CoverPlayScrollAwareTopBar(
    visible: Boolean = AppChrome.visible,
    content: @Composable () -> Unit,
) {
    val enabled = LocalAppUiSettings.current.hideTopBarOnScroll
    val show = !enabled || visible
    val reveal by animateFloatAsState(
        targetValue = if (show) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.85f, stiffness = 420f),
        label = "topBarReveal",
    )
    Box(
        Modifier.graphicsLayer {
            translationY = -size.height * (1f - reveal)
            alpha = reveal
        }
    ) { content() }
}

@Composable
fun Modifier.chromeFade(
    scrolled: Boolean,
    @Suppress("UNUSED_PARAMETER") hazeState: HazeState? = LocalGlassHaze.current,
): Modifier {
    val p = LocalAppUiSettings.current.palette
    val edge by animateFloatAsState(
        targetValue = if (scrolled) 1f else 0f,
        animationSpec = tween(260, easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)),
        label = "headerEdge",
    )
    val live = rememberCoverPlayHasLiveBlur()
    val bg = p.bg
    val blurred = if (live && edge > 0.01f) Modifier.progressiveGlassBlur(28.dp, bg.copy(alpha = 0.30f)) else Modifier
    val restAlpha = if (live) 0f else 0.8f * edge
    return this
        .then(blurred)
        .background(bg.copy(alpha = restAlpha))
        .drawBehind {
            if (edge > 0.01f) {
                val tail = 32.dp.toPx()
                val total = size.height + tail
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            bg.copy(alpha = 0.78f * edge),
                            bg.copy(alpha = 0.50f * edge),
                            bg.copy(alpha = 0.18f * edge),
                            Color.Transparent,
                        ),
                        startY = 0f,
                        endY = total,
                    ),
                    size = Size(size.width, total),
                )
            }
        }
}

@Composable
fun Modifier.frosted(
    shape: Shape,
    @Suppress("UNUSED_PARAMETER") radius: Dp = 16.dp,
    @Suppress("UNUSED_PARAMETER") tint: Color? = null,
    border: Boolean = true,
): Modifier {
    val p = LocalAppUiSettings.current.palette
    return this
        .clip(shape)
        .background(p.container.copy(alpha = 0.78f))
        .then(if (border) Modifier.border(1.dp, p.outlineFaint, shape) else Modifier)
}

@Composable
fun frostedTintOrNull(): Color? = null
