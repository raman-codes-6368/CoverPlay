package com.example.myapplication.ui.theme

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.min

// ═════════════════════════════════════════════════════════════════════════════
//  PREMIUM CONTROLS — the single source of truth for glow, switches, sliders and
//  list rows. Everything in the app that glows uses GlowSpec / premiumGlow, every
//  toggle is PremiumSwitch, every horizontal slider is PremiumSlider and every
//  list entry (Home library, Folders, Playlists) is a ListRowSurface.
// ═════════════════════════════════════════════════════════════════════════════

/** Shared elevation-glow values: use these instead of ad-hoc accent alphas. */
object GlowSpec {
    const val AMBIENT = 0.20f
    const val SPOT = 0.42f
    val Elevation: Dp = 12.dp
}

/** Three glow strengths. Same recipe everywhere; only intensity changes. */
enum class GlowLevel(val alpha: Float, val reach: Dp) {
    Subtle(0.14f, 10.dp),
    Medium(0.26f, 16.dp),
    Strong(0.40f, 26.dp)
}

/**
 * Soft, even, never-clipped halo painted behind the element (apply BEFORE clip).
 * [strength] 0..1 lets the glow animate in/out with state (switch on, slider drag...).
 */
fun Modifier.premiumGlow(
    color: Color,
    level: GlowLevel = GlowLevel.Medium,
    strength: Float = 1f,
    corner: Dp? = null
): Modifier = drawBehind {
    val s = strength.coerceIn(0f, 1f)
    if (s < 0.01f) return@drawBehind
    val reach = level.reach.toPx()
    val base = corner?.toPx() ?: (min(size.width, size.height) / 2f)
    val steps = 6
    for (i in steps downTo 1) {
        val t = i / steps.toFloat()
        val grow = reach * t
        val a = (level.alpha * s * (1f - t) * 0.45f).coerceIn(0f, 1f)
        drawRoundRect(
            color = color.copy(alpha = a),
            topLeft = Offset(-grow, -grow),
            size = Size(size.width + grow * 2f, size.height + grow * 2f),
            cornerRadius = CornerRadius(base + grow)
        )
    }
}

// ── Switch ───────────────────────────────────────────────────────────────────

/**
 * The one toggle for the whole app. Recessed glass track, accent-lit when on (with a
 * soft halo), pearl thumb that stretches under the finger. Pass [onCheckedChange] = null
 * when a parent row already handles the click (display-only).
 */
@Composable
fun PremiumSwitch(
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
    val haptic = rememberHaptic()

    val progress by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.66f, stiffness = 480f), label = "pswProgress"
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
    val offFill = if (dark) Color.White.copy(alpha = 0.10f) else Color.Black.copy(alpha = 0.10f)
    val rimTop = if (dark) Color.White.copy(alpha = 0.22f) else accent.copy(alpha = 0.28f)

    Box(
        modifier = modifier
            .size(trackW, trackH)
            .premiumGlow(accent, GlowLevel.Medium, progress)
            .clip(CircleShape)
            .background(offFill)
            .background(Brush.horizontalGradient(listOf(IconSky, accent)), CircleShape, alpha = progress)
            .background(
                Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.34f), Color.Transparent)),
                CircleShape, alpha = progress
            )
            .border(1.dp, Brush.verticalGradient(listOf(rimTop, Color.White.copy(alpha = 0.04f))), CircleShape)
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
                .shadow(
                    if (compact) 3.dp else 5.dp, CircleShape,
                    ambientColor = Color.Black.copy(alpha = 0.12f),
                    spotColor = Color.Black.copy(alpha = 0.30f)
                )
                .clip(CircleShape)
                .background(Brush.verticalGradient(listOf(Color.White, Color(0xFFE9ECFA))))
        ) {
            Box(
                Modifier
                    .align(Alignment.Center)
                    .size(thumbH * 0.34f)
                    .graphicsLayer {
                        alpha = progress
                        val sc = 0.4f + 0.6f * progress
                        scaleX = sc; scaleY = sc
                    }
                    .background(accent, CircleShape)
            )
        }
    }
}

// ── Slider ───────────────────────────────────────────────────────────────────

/**
 * The one horizontal slider for the whole app: slim glass groove, glowing gradient fill and a
 * pearl thumb with an accent core that blooms a halo while dragging. Replaces Material's Slider.
 * Set [onDark] on dark glass over video so the groove stays visible in Light mode.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    onValueChangeFinished: (() -> Unit)? = null,
    accentColor: Color = LocalAppUiSettings.current.accent,
    onDark: Boolean = false,
    enabled: Boolean = true
) {
    val settings = LocalAppUiSettings.current
    val dark = settings.palette.isDark || onDark
    val hi = settings.highlight
    val interaction = remember { MutableInteractionSource() }
    val dragging by interaction.collectIsDraggedAsState()
    val pressed by interaction.collectIsPressedAsState()
    val active = dragging || pressed

    val trackH by animateDpAsState(
        targetValue = if (active) 8.dp else 6.dp,
        animationSpec = spring(stiffness = 500f), label = "pslTrack"
    )
    val thumbScale by animateFloatAsState(
        targetValue = if (active) 1.18f else 1f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 520f), label = "pslThumb"
    )
    val halo by animateFloatAsState(
        targetValue = if (active) 1f else 0f,
        animationSpec = tween(180), label = "pslHalo"
    )
    val span = (valueRange.endInclusive - valueRange.start).takeIf { it != 0f } ?: 1f
    val fraction = ((value - valueRange.start) / span).coerceIn(0f, 1f)
    val groove = if (dark) Color.White.copy(alpha = 0.16f) else Color.Black.copy(alpha = 0.10f)

    Slider(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        valueRange = valueRange,
        steps = steps,
        onValueChangeFinished = onValueChangeFinished,
        interactionSource = interaction,
        thumb = {
            Box(Modifier.size(36.dp), contentAlignment = Alignment.Center) {
                Box(
                    Modifier
                        .size(36.dp)
                        .graphicsLayer { alpha = halo }
                        .background(
                            Brush.radialGradient(listOf(accentColor.copy(alpha = 0.38f), Color.Transparent)),
                            CircleShape
                        )
                )
                Box(
                    Modifier
                        .size(22.dp)
                        .graphicsLayer { scaleX = thumbScale; scaleY = thumbScale }
                        .shadow(
                            6.dp, CircleShape,
                            ambientColor = accentColor.copy(alpha = GlowSpec.AMBIENT),
                            spotColor = accentColor.copy(alpha = GlowSpec.SPOT)
                        )
                        .clip(CircleShape)
                        .background(Brush.verticalGradient(listOf(Color.White, Color(0xFFE4E8FB))))
                        .border(1.dp, Color.White.copy(alpha = 0.85f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        Modifier
                            .size(8.dp)
                            .background(Brush.linearGradient(listOf(hi, accentColor)), CircleShape)
                    )
                }
            }
        },
        track = {
            Box(Modifier.fillMaxWidth().height(trackH)) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(groove)
                )
                Box(
                    Modifier
                        .fillMaxWidth(fraction)
                        .fillMaxHeight()
                        .premiumGlow(accentColor, GlowLevel.Subtle, 0.65f + 0.35f * halo)
                        .clip(CircleShape)
                        .background(Brush.horizontalGradient(listOf(IconSky, accentColor, hi)))
                        .background(Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.35f), Color.Transparent)))
                )
            }
        }
    )
}

// ── List rows (Home library, Folders, Playlists) ─────────────────────────────

/**
 * Shared list-entry surface for every list in the app. Flat, no per-row box: a subtle glass
 * highlight on press, a lit glass card when [selected] (expanded), and a faint hairline that
 * fades at both ends separates one entry from the next.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ListRowSurface(
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
    val shape = RoundedCornerShape(22.dp)
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val press by animateFloatAsState(if (pressed) 1f else 0f, tween(140), label = "rowPress")
    val sel by animateFloatAsState(if (selected) 1f else 0f, tween(260), label = "rowSel")
    val longHaptic = rememberHaptic(HapticFeedbackType.LongPress)
    val look = rememberGlassLook()
    val line = s.palette.textTertiary.copy(alpha = if (dark) 0.20f else 0.24f)
    val pressTint = if (dark) Color.White.copy(alpha = 0.07f) else accent.copy(alpha = 0.08f)

    Box(modifier.fillMaxWidth().padding(horizontal = 10.dp)) {
        Box(
            Modifier
                .fillMaxWidth()
                .premiumGlow(accent, GlowLevel.Subtle, sel * 0.9f, 22.dp)
                .clip(shape)
                .background(pressTint.copy(alpha = pressTint.alpha * press))
                .background(Brush.verticalGradient(listOf(accent.copy(alpha = 0.13f * sel), accent.copy(alpha = 0.04f * sel))))
                .background((if (dark) Color.White else accent).copy(alpha = 0.025f * sel))
                .border(1.dp, Brush.verticalGradient(listOf(accent.copy(alpha = 0.45f * sel), line.copy(alpha = line.alpha * sel))), shape)
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

        if (showDivider && !selected) {
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth()
                    .height(0.8.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color.Transparent, line, line, Color.Transparent)
                        )
                    )
            )
        }
    }
}