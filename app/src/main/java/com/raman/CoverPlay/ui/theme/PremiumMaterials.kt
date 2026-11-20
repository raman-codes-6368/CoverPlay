package com.raman.CoverPlay.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.raman.CoverPlay.util.rememberCoverPlayHaptic

private class SurfaceInk(val fill: Color, val stateLayer: Color, val edge: Color)

@Composable
private fun rememberCoverPlaySurfaceInk(raised: Boolean): SurfaceInk {
    val p = LocalAppUiSettings.current.palette
    return remember(p, raised) {
        SurfaceInk(
            fill = if (raised) p.containerHigh else p.container,
            stateLayer = if (p.isDark) Color.White else Color.Black,
            edge = if (p.isDark) p.outlineFaint.copy(alpha = 0.55f) else p.outlineFaint,
        )
    }
}

@Composable
fun Modifier.solidCard(
    shape: Shape,
    selected: Boolean = false,
    raised: Boolean = false,
    elevation: Dp = 0.dp,
    pressProgress: () -> Float = { 0f },
): Modifier {
    val accent = LocalAppUiSettings.current.accent
    val ink = rememberCoverPlaySurfaceInk(raised)
    val selection by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = tween(220, easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)),
        label = "solidCardSelection",
    )
    val container = lerp(ink.fill, accent, 0.20f * selection)
    return this
        .then(if (elevation > 0.dp) Modifier.shadow(elevation, shape) else Modifier)
        .clip(shape)
        .background(container)
        .border(1.dp, ink.edge, shape)
        .drawWithContent {
            drawContent()
            val press = pressProgress()
            if (press > 0.001f) drawRect(ink.stateLayer.copy(alpha = 0.08f * press))
        }
}

@Composable
fun Modifier.blurCard(
    shape: Shape,
    radius: Dp = 28.dp,
    tintAlpha: Float = 0.72f,
    selected: Boolean = false,
    elevation: Dp = 0.dp,
): Modifier {
    val s = LocalAppUiSettings.current
    val p = s.palette
    val accent = s.accent
    val live = rememberCoverPlayHasLiveBlur()
    val tint = remember(p, tintAlpha) {
        (if (p.isDark) lerp(p.container, Color.Black, 0.25f) else Color.White).copy(alpha = tintAlpha)
    }
    val opaqueFallback = remember(p) {
        if (p.isDark) lerp(p.containerHigh, Color.Black, 0.12f) else lerp(Color.White, p.container, 0.35f)
    }
    val rimHi = if (p.isDark) Color.White.copy(alpha = 0.20f) else p.outlineMedium
    val rimLo = if (p.isDark) Color.White.copy(alpha = 0.05f) else p.outlineFaint
    val sheen = if (p.isDark) Color.White.copy(alpha = 0.07f) else Color.White.copy(alpha = 0.55f)
    return this
        .then(
            if (elevation > 0.dp) Modifier.shadow(
                elevation, shape,
                ambientColor = Color.Black.copy(alpha = if (p.isDark) 0.35f else 0.06f),
                spotColor = Color.Black.copy(alpha = if (p.isDark) 0.50f else 0.14f),
            ) else Modifier
        )
        .clip(shape)
        .then(if (live) Modifier.glassBlur(radius, tint) else Modifier.background(opaqueFallback))
        .drawWithCache {
            val outline = shape.createOutline(size, layoutDirection, this)
            val top = Brush.verticalGradient(listOf(sheen, Color.Transparent), startY = 0f, endY = size.height * 0.5f)
            val rim = Brush.verticalGradient(listOf(rimHi, rimLo), startY = 0f, endY = size.height)
            val hairline = 2.dp.toPx()
            val ring = 3.dp.toPx()
            onDrawWithContent {
                drawOutline(outline, top)
                drawContent()
                drawOutline(outline, rim, style = Stroke(hairline))
                if (selected) drawOutline(outline, accent.copy(alpha = 0.90f), style = Stroke(ring))
            }
        }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CoverPlaySolidCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(CoverPlayTokens.Radius.card),
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    selected: Boolean = false,
    raised: Boolean = false,
    elevation: Dp = 0.dp,
    content: @Composable BoxScope.() -> Unit,
) {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val tapHaptic = rememberCoverPlayHaptic()
    val longHaptic = rememberCoverPlayHaptic(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
    val scale by animateFloatAsState(
        targetValue = if (pressed && onClick != null) 0.985f else 1f,
        animationSpec = spring(dampingRatio = 0.9f, stiffness = 600f),
        label = "solidCardScale",
    )
    val press by animateFloatAsState(
        targetValue = if (pressed && onClick != null) 1f else 0f,
        animationSpec = tween(120),
        label = "solidCardState",
    )
    Box(
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .solidCard(shape, selected, raised, elevation) { press }
            .then(
                if (onClick != null || onLongClick != null) Modifier.combinedClickable(
                    interactionSource = source,
                    indication = null,
                    onClick = { tapHaptic(); onClick?.invoke() },
                    onLongClick = onLongClick?.let { lc -> { longHaptic(); lc() } },
                ) else Modifier
            ),
        content = content,
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CoverPlayBlurCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    radius: Dp = 28.dp,
    tintAlpha: Float = 0.72f,
    onClick: (() -> Unit)? = null,
    selected: Boolean = false,
    elevation: Dp = 0.dp,
    content: @Composable BoxScope.() -> Unit,
) {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && onClick != null) 0.97f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMediumLow),
        label = "blurCardScale",
    )
    Box(
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .blurCard(shape, radius, tintAlpha, selected, elevation)
            .then(
                if (onClick != null) Modifier.combinedClickable(
                    interactionSource = source, indication = null, onClick = onClick,
                ) else Modifier
            ),
        content = content,
    )
}
