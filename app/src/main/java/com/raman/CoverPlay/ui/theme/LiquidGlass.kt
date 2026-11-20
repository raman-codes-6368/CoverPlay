package com.raman.CoverPlay.ui.theme

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.raman.CoverPlay.util.rememberCoverPlayHaptic
import dev.chrisbanes.haze.ExperimentalHazeApi
import dev.chrisbanes.haze.HazeInputScale
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource

val LocalGlassHaze = compositionLocalOf<HazeState?> { null }

val LocalGlassBackdrop = compositionLocalOf<BackdropState?> { null }

val WindowBlurSupported: Boolean get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

@Immutable
data class GlassLook(
    val frostedFill: Color,
    val clearTint: Color,
    val sheen: Color,
    val rimHi: Color,
    val rimLo: Color,
    val topLight: Color,
    val edgeGlow: Color,
    val depth: Color,
)

internal val LensLook: GlassLook
    get() = GlassLook(
        frostedFill = Color.Transparent,
        clearTint = Color.Transparent,
        sheen = Color.Transparent,
        rimHi = Color.White.copy(alpha = 0.16f),
        rimLo = Color.White.copy(alpha = 0.04f),
        topLight = Color.Transparent,
        edgeGlow = Color.Transparent,
        depth = Color.Transparent
    )

@Composable
fun rememberCoverPlayGlassLook(
    overVideo: Boolean = false,
    strong: Boolean = false,
    tint: Color? = null
): GlassLook {
    val settings = LocalAppUiSettings.current
    val p = settings.palette
    val mode = settings.themeMode
    val accentNow = settings.accent

    return remember(overVideo, strong, tint, mode, p, accentNow) {
        when {
            overVideo -> {
                val glass = if (p.isDark) p.videoGlass else Color(0xFF15171D)
                val base = tint ?: glass.copy(alpha = if (strong) 0.94f else 0.85f)
                GlassLook(
                    frostedFill = base, clearTint = base,
                    sheen = Color.Transparent,
                    rimHi = Color.White.copy(alpha = 0.14f),
                    rimLo = Color.White.copy(alpha = 0.05f),
                    topLight = Color.Transparent, edgeGlow = Color.Transparent, depth = Color.Transparent
                )
            }

            mode == ThemeMode.LIGHT -> GlassLook(
                frostedFill = tint ?: p.glassBase,
                clearTint = tint ?: p.glassBase.copy(alpha = if (strong) 0.92f else 0.86f),
                sheen = Color.Transparent,
                rimHi = p.outlineMedium,
                rimLo = p.outlineFaint,
                topLight = Color.Transparent, edgeGlow = Color.Transparent, depth = Color.Transparent
            )

            else -> GlassLook(
                frostedFill = tint ?: p.containerHigh,
                clearTint = tint ?: p.containerHigh,
                sheen = Color.Transparent,
                rimHi = Color.White.copy(alpha = 0.10f),
                rimLo = Color.White.copy(alpha = 0.04f),
                topLight = Color.Transparent, edgeGlow = Color.Transparent, depth = Color.Transparent
            )
        }
    }
}

fun Modifier.liquidGlassDecor(
    shape: Shape,
    look: GlassLook,
    accent: Color,
    paintFill: Boolean = true,
    press: () -> Float = { 0f },
    selected: () -> Float = { 0f },
    @Suppress("UNUSED_PARAMETER") touch: () -> Offset = { Offset.Unspecified }
): Modifier = drawWithCache {
    val outline = shape.createOutline(size, layoutDirection, this)
    val onLight = look.frostedFill.alpha > 0.5f && look.frostedFill.luminance() > 0.6f
    val stateLayer = if (onLight) Color.Black.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.08f)

    onDrawWithContent {
        if (paintFill && look.frostedFill.alpha > 0f) drawOutline(outline, look.frostedFill)
        val s = selected()
        if (s > 0.001f) drawOutline(outline, accent.copy(alpha = 0.20f * s))
        val p = press()
        if (p > 0.001f) drawOutline(outline, stateLayer.copy(alpha = stateLayer.alpha * p))
        drawContent()
    }
}

object GlassWarmup {
    private val _ready = androidx.compose.runtime.mutableStateOf(false)
    var ready: Boolean
        get() = _ready.value
        set(v) { _ready.value = v }
}

@OptIn(ExperimentalHazeApi::class)
@Composable
fun Modifier.glassBlur(radius: Dp, tint: Color): Modifier {
    val haze = LocalGlassHaze.current
    val backdrop = LocalGlassBackdrop.current
    val p = LocalAppUiSettings.current.palette
    return when {
        haze != null -> {
            val ready = GlassWarmup.ready
            val cover by androidx.compose.animation.core.animateFloatAsState(
                if (ready) 0f else 1f,
                androidx.compose.animation.core.tween(500),
                label = "glassWarm"
            )
            this
                .then(
                    if (ready) Modifier.hazeEffect(
                        state = haze,
                        style = HazeStyle(
                            backgroundColor = p.bg,
                            tint = HazeTint(tint),
                            blurRadius = radius,
                            noiseFactor = 0.045f,
                            fallbackTint = HazeTint(p.surfaceRaised.copy(alpha = 0.96f))
                        )
                    ) {
                        inputScale = HazeInputScale.Fixed(0.33f)
                    } else Modifier
                )
                .then(if (cover > 0.01f) Modifier.background(p.surfaceRaised.copy(alpha = 0.92f * cover)) else Modifier)
        }
        backdrop != null -> this.backdropBlur(backdrop).background(tint)
        else -> this
    }
}

@OptIn(ExperimentalHazeApi::class)
@Composable
fun Modifier.progressiveGlassBlur(radius: Dp, tint: Color): Modifier {
    val haze = LocalGlassHaze.current
    val p = LocalAppUiSettings.current.palette
    if (haze == null || !GlassWarmup.ready) return this.glassBlur(radius, tint)
    return this.hazeEffect(
        state = haze,
        style = HazeStyle(
            backgroundColor = p.bg,
            tint = HazeTint(tint),
            blurRadius = radius,
            noiseFactor = 0.03f,
            fallbackTint = HazeTint(p.bg.copy(alpha = 0.90f))
        )
    ) {
        inputScale = HazeInputScale.Fixed(0.33f)
        progressive = HazeProgressive.verticalGradient(startIntensity = 1f, endIntensity = 0f)
    }
}

@Composable
fun Modifier.glassSource(): Modifier {
    val haze = LocalGlassHaze.current
    return if (haze != null) this.hazeSource(haze, zIndex = 0f) else this
}

@Composable
fun rememberCoverPlayHasLiveBlur(): Boolean =
    LocalGlassHaze.current != null || LocalGlassBackdrop.current != null

fun Modifier.liquidLens(accent: Color, shape: Shape = CircleShape): Modifier = this
    .clip(shape)
    .background(accent.forWhiteContent())

fun Modifier.glassBadge(
    fill: Color,
    rim: Color = Color.White,
    shape: Shape = CircleShape
): Modifier {
    val look = GlassLook(
        frostedFill = fill, clearTint = fill,
        sheen = Color.Transparent,
        rimHi = rim.copy(alpha = 0.16f), rimLo = rim.copy(alpha = 0.05f),
        topLight = Color.Transparent, edgeGlow = Color.Transparent, depth = Color.Transparent
    )
    return this.clip(shape).liquidGlassDecor(shape, look, rim)
}

fun Modifier.softEdgeMask(solidUntil: Float = 0.55f): Modifier = this
    .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
    .drawWithContent {
        drawContent()
        drawRect(
            brush = Brush.verticalGradient(
                solidUntil to Color.Black, 1f to Color.Transparent,
                startY = 0f, endY = size.height
            ),
            blendMode = BlendMode.DstIn
        )
    }

@Composable
fun CoverPlayGlassChip(
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    shape: Shape = CircleShape,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val s = LocalAppUiSettings.current
    val accent = s.accent
    val p = s.palette
    val sel by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = androidx.compose.animation.core.tween(200),
        label = "chipSel"
    )
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val press by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = androidx.compose.animation.core.spring(dampingRatio = 0.9f, stiffness = 600f),
        label = "chipPress"
    )
    val haptic = rememberCoverPlayHaptic()
    androidx.compose.foundation.layout.Box(
        modifier = modifier
            .graphicsLayer { scaleX = press; scaleY = press }
            .clip(shape)
            .background(lerp(p.containerHigh, accent.copy(alpha = 1f), 0.24f * sel))
            .then(
                if (onClick != null) Modifier.clickable(
                    interactionSource = interaction, indication = null,
                    role = androidx.compose.ui.semantics.Role.Button
                ) { haptic(); onClick() } else Modifier
            ),
        content = content
    )
}

@Suppress("UNUSED_PARAMETER")
fun Modifier.accentGlow(
    color: Color,
    radius: Dp = 40.dp,
    maxAlpha: Float = 0.40f,
    strength: () -> Float = { 1f }
): Modifier = this

fun Modifier.trackTouch(onDown: (Offset) -> Unit): Modifier = this.pointerInput(Unit) {
    awaitEachGesture {
        val d = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
        onDown(d.position)
    }
}

@Composable
internal fun rememberCoverPlayTouchState() = remember { mutableStateOf(Offset.Unspecified) }

@Composable
fun glassFieldFill(): Color {
    val s = LocalAppUiSettings.current
    return if (s.palette.isDark) Color.White.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.62f)
}

@Composable
fun CoverPlayGlassRow(
    onClick: () -> Unit,
    selected: Boolean,
    modifier: Modifier = Modifier,
    shape: Shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
    content: @Composable BoxScope.() -> Unit
) {
    val accent = LocalAppUiSettings.current.accent
    val look = rememberCoverPlayGlassLook()
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val haptic = rememberCoverPlayHaptic()
    val scale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (pressed) 0.975f else 1f,
        animationSpec = GlassTokens.SpringPress, label = "glassRowScale"
    )
    androidx.compose.foundation.layout.Box(
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(shape)
            .then(
                if (selected) Modifier
                    .background(accent.copy(alpha = 0.16f))
                    .liquidGlassDecor(shape, look, accent, paintFill = false, selected = { 1f })
                else Modifier.background(if (pressed) LocalAppUiSettings.current.palette.textPrimary.copy(alpha = 0.07f) else Color.Transparent)
            )
            .clickable(
                interactionSource = interaction, indication = null,
                role = androidx.compose.ui.semantics.Role.RadioButton
            ) { haptic(); onClick() },
        content = content
    )
}
