package com.raman.CoverPlay.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitHorizontalTouchSlopOrCancellation
import androidx.compose.foundation.gestures.horizontalDrag
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.raman.CoverPlay.ui.theme.LocalAppUiSettings
import com.raman.CoverPlay.ui.theme.onFill
import com.raman.CoverPlay.ui.theme.rememberCoverPlayGlassLook
import com.raman.CoverPlay.util.rememberCoverPlayHaptic
import kotlin.math.abs
import kotlin.math.roundToInt

object GlowSpec {
    const val AMBIENT = 0.05f
    const val SPOT = 0.10f
    val Elevation: Dp = 4.dp
}

enum class GlowLevel(val alpha: Float, val reach: Dp) {
    Subtle(0.05f, 8.dp),
    Medium(0.09f, 12.dp),
    Strong(0.16f, 18.dp)
}

@Suppress("UNUSED_PARAMETER")
fun Modifier.premiumGlow(
    color: Color,
    level: GlowLevel = GlowLevel.Medium,
    strength: Float = 1f,
    corner: Dp? = null
): Modifier = this

@Composable
fun CoverPlaySwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val settings = LocalAppUiSettings.current
    val accent = settings.accent
    val dark = settings.palette.isDark
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val haptic = rememberCoverPlayHaptic()

    val progress by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 520f), label = "pswProgress"
    )
    val trackW = if (compact) 44.dp else 56.dp
    val trackH = if (compact) 26.dp else 32.dp
    val pad = 3.dp
    val thumbH = trackH - pad * 2
    val thumbW by animateDpAsState(
        targetValue = if (pressed) thumbH * 1.35f else thumbH,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 600f), label = "pswThumb"
    )
    val travel = trackW - thumbW - pad * 2
    val offFill = if (dark) Color.White.copy(alpha = 0.14f) else Color.Black.copy(alpha = 0.14f)

    Box(
        modifier = modifier
            .size(trackW, trackH)
            .clip(CircleShape)
            .background(offFill)
            .background(androidx.compose.ui.graphics.SolidColor(accent), CircleShape, alpha = progress)
            .then(
                if (onCheckedChange != null) Modifier.toggleable(
                    value = checked,
                    interactionSource = interaction,
                    indication = null,
                    role = Role.Switch
                ) { haptic(); onCheckedChange(it) } else Modifier
            )
    ) {
        Box(
            Modifier
                .padding(pad)
                .offset(x = travel * progress)
                .size(width = thumbW, height = thumbH)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            val checkTint = if (accent.luminance() > 0.5f) Color(0xFF24211A) else accent
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = null,
                tint = checkTint,
                modifier = Modifier
                    .size(thumbH * 0.62f)
                    .graphicsLayer {
                        alpha = progress
                        scaleX = 0.4f + 0.6f * progress
                        scaleY = 0.4f + 0.6f * progress
                    }
            )
        }
    }
}

@Composable
fun CoverPlaySlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    onValueChangeFinished: (() -> Unit)? = null,
    accentColor: Color = LocalAppUiSettings.current.accent,
    onDark: Boolean = false,
    enabled: Boolean = true,
    detents: List<Float> = emptyList()
) {
    val settings = LocalAppUiSettings.current
    val dark = settings.palette.isDark || onDark
    val bumpHaptic = rememberCoverPlayHaptic(HapticFeedbackType.LongPress)
    val stepHaptic = rememberCoverPlayHaptic(HapticFeedbackType.TextHandleMove)

    val onChange by rememberUpdatedState(onValueChange)
    val onFinish by rememberUpdatedState(onValueChangeFinished)
    val range by rememberUpdatedState(valueRange)
    val stepCount by rememberUpdatedState(steps)
    val detentList by rememberUpdatedState(detents)
    val externalValue by rememberUpdatedState(value)

    var dragging by remember { mutableStateOf(false) }
    var local by remember { mutableFloatStateOf(value) }

    val trackH by animateDpAsState(
        targetValue = if (dragging) 16.dp else 14.dp,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 500f), label = "pslTrack"
    )
    val handleW by animateDpAsState(
        targetValue = if (dragging) 2.dp else 4.dp,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 600f), label = "pslHandle"
    )
    val handleScale by animateFloatAsState(
        targetValue = if (dragging) 1.10f else 1f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 520f), label = "pslThumb"
    )

    val span = (valueRange.endInclusive - valueRange.start).takeIf { it > 0f } ?: 1f
    val shown = if (dragging) local else value
    val fraction = ((shown - valueRange.start) / span).coerceIn(0f, 1f)
    val groove = if (dark) Color.White.copy(alpha = 0.16f) else Color.Black.copy(alpha = 0.10f)
    val notchOnActive = accentColor.onFill()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .semantics {
                progressBarRangeInfo = ProgressBarRangeInfo(shown, valueRange, steps)
                setProgress { target ->
                    if (!enabled) false else {
                        onChange(target.coerceIn(range.start, range.endInclusive))
                        onFinish?.invoke()
                        true
                    }
                }
            }
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val inset = 10.dp.toPx()
                    val usable = (size.width - 2f * inset).coerceAtLeast(1f)
                    var lastValue = externalValue

                    fun resolve(x: Float): Float {
                        val f = ((x - inset) / usable).coerceIn(0f, 1f)
                        val r = range
                        val sp = (r.endInclusive - r.start).coerceAtLeast(1e-6f)
                        var v = r.start + f * sp
                        val st = stepCount
                        if (st > 0) {
                            val n = st + 1
                            v = r.start + ((f * n).roundToInt() / n.toFloat()) * sp
                        }
                        for (d in detentList) if (abs(v - d) <= sp * 0.025f) v = d
                        return v.coerceIn(r.start, r.endInclusive)
                    }

                    fun applyX(x: Float) {
                        val v = resolve(x)
                        if (v != lastValue) {
                            if (detentList.any { it == v }) bumpHaptic()
                            else if (stepCount > 0) stepHaptic()
                            lastValue = v
                            local = v
                            onChange(v)
                        }
                    }

                    val slop = awaitHorizontalTouchSlopOrCancellation(down.id) { change, _ -> change.consume() }
                    if (slop == null) {
                        val up = currentEvent.changes.firstOrNull { it.id == down.id }
                        if (up != null && !up.pressed) {
                            local = lastValue
                            applyX(down.position.x)
                            onFinish?.invoke()
                        }
                        return@awaitEachGesture
                    }
                    local = lastValue
                    dragging = true
                    applyX(slop.position.x)
                    horizontalDrag(slop.id) { change ->
                        change.consume()
                        applyX(change.position.x)
                    }
                    dragging = false
                    onFinish?.invoke()
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val hTotal = size.height
            val th = trackH.toPx()
            val top = (hTotal - th) / 2f
            val inset = 10.dp.toPx()
            val usable = (w - 2f * inset).coerceAtLeast(1f)
            val cx = inset + usable * fraction
            val hw = handleW.toPx()
            val gap = 6.dp.toPx() + hw / 2f
            val outer = CornerRadius(th / 2f, th / 2f)
            val inner = CornerRadius(2.dp.toPx(), 2.dp.toPx())

            val activeEnd = (cx - gap).coerceAtLeast(0f)
            if (activeEnd > 1f) {
                drawPath(
                    Path().apply { addRoundRect(RoundRect(0f, top, activeEnd, top + th, outer, inner, inner, outer)) },
                    color = accentColor
                )
            }
            val restStart = (cx + gap).coerceAtMost(w)
            if (w - restStart > 1f) {
                drawPath(
                    Path().apply { addRoundRect(RoundRect(restStart, top, w, top + th, inner, outer, outer, inner)) },
                    color = groove
                )
                if (w - restStart > th * 1.5f) {
                    drawCircle(accentColor, 2.dp.toPx(), Offset(w - th / 2f, hTotal / 2f))
                }
            }
            for (d in detentList) {
                val df = ((d - valueRange.start) / span).coerceIn(0f, 1f)
                val nx = inset + usable * df
                if (abs(nx - cx) < gap + 3.dp.toPx()) continue
                drawCircle(
                    color = if (nx < cx) notchOnActive.copy(alpha = 0.85f) else accentColor,
                    radius = 2.5.dp.toPx(),
                    center = Offset(nx, hTotal / 2f)
                )
            }
            val hh = 36.dp.toPx() * handleScale
            drawRoundRect(
                color = accentColor,
                topLeft = Offset(cx - hw / 2f, (hTotal - hh) / 2f),
                size = Size(hw, hh),
                cornerRadius = CornerRadius(hw / 2f, hw / 2f)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CoverPlayListRowSurface(
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    showDivider: Boolean = true,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val s = LocalAppUiSettings.current
    val accent = s.accent
    val dark = s.palette.isDark
    val shape = RoundedCornerShape(CoverPlayCorners.card)
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val press by animateFloatAsState(if (pressed) 1f else 0f, tween(140), label = "rowPress")
    val sel by animateFloatAsState(if (selected) 1f else 0f, tween(260), label = "rowSel")
    val longHaptic = rememberCoverPlayHaptic(HapticFeedbackType.LongPress)
    val look = rememberCoverPlayGlassLook()
    val line = s.palette.textTertiary.copy(alpha = if (dark) 0.20f else 0.24f)
    val pressTint = if (dark) Color.White.copy(alpha = 0.07f) else accent.copy(alpha = 0.08f)

    Box(modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
        Box(
            Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(s.palette.containerHigh)
                .background(pressTint.copy(alpha = pressTint.alpha * press))
                .background(accent.copy(alpha = 0.20f * sel))
                .then(
                    if (onClick != null || onLongClick != null) Modifier.combinedClickable(
                        interactionSource = interaction,
                        indication = null,
                        onClick = { onClick?.invoke() },
                        onLongClick = onLongClick?.let { lc -> { longHaptic(); lc() } }
                    ) else Modifier
                ),
            propagateMinConstraints = true
        ) { content() }
    }
}
