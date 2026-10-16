package com.example.myapplication.ui.theme

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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import kotlin.math.max
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.drawOutline

// ═════════════════════════════════════════════════════════════════════════════
//  LIQUID GLASS — one material for the whole app
//
//  Layers, back to front:
//    1. live backdrop blur (chrome only: nav, bars, floating controls)
//    2. clear tint (or frosted fill where nothing blurred sits behind)
//    3. caustic  — accent-coloured light bending across the pane
//    4. sheen    — top-lit highlight
//    5. depth    — soft shade pooling at the bottom edge
//    6. lens edge — bright band hugging the inside of the rim (refraction)
//    7. press bloom — light that follows your finger
//    8. content
//    9. specular rim — bright top-left & bottom-right, dim between
// ═════════════════════════════════════════════════════════════════════════════

/** Blur source for live-blurred glass. Provided by [CinematicBackground]. */
val LocalGlassHaze = compositionLocalOf<HazeState?> { null }

/** Fallback blur source (own graphics-layer backdrop) when Haze isn't provided. */
val LocalGlassBackdrop = compositionLocalOf<BackdropState?> { null }

/** True where the OS can blur behind a window (dialogs / sheets). */
val WindowBlurSupported: Boolean get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

@Immutable
data class GlassLook(
    /** Opaque-ish fill used when nothing blurred sits behind the pane. */
    val frostedFill: Color,
    /** Clear tint used when live/window blur sits behind the pane. */
    val clearTint: Color,
    val sheen: Color,
    val rimHi: Color,
    val rimLo: Color,
    val topLight: Color,
    val edgeGlow: Color,
    val depth: Color,
)

/** Accent-tinted lens look; follows the active theme (no fixed blue). */
internal val LensLook: GlassLook
    get() {
        val hi = LiveTheme.highlight
        val ac = LiveTheme.accent
        return GlassLook(
            frostedFill = Color.Transparent,
            clearTint = Color.Transparent,
            sheen = Color.White.copy(alpha = 0.12f),
            rimHi = lerp(Color.White, hi, 0.45f).copy(alpha = 0.62f),
            rimLo = ac.copy(alpha = 0.14f),
            topLight = Color.White.copy(alpha = 0.20f),
            edgeGlow = hi.copy(alpha = 0.12f),
            depth = Color.Black.copy(alpha = 0.28f)
        )
    }

/** Resolves the glass look for the current theme mode. */
@Composable
fun rememberGlassLook(
    overVideo: Boolean = false,
    strong: Boolean = false,
    tint: Color? = null
): GlassLook {
    val settings = LocalAppUiSettings.current
    val p = settings.palette
    val mode = settings.themeMode
    val accent = settings.accent
    val hi = settings.highlight

    return remember(overVideo, strong, tint, mode, accent, hi, p) {
        when {
            overVideo -> {
                val base = tint ?: p.videoGlass.copy(alpha = if (strong) 0.94f else 0.85f)
                GlassLook(
                    frostedFill = base,
                    clearTint = base,
                    sheen = Color.White.copy(alpha = 0.07f),
                    rimHi = lerp(Color.White, hi, 0.45f).copy(alpha = 0.38f),
                    rimLo = Color.White.copy(alpha = 0.025f),
                    topLight = Color.White.copy(alpha = 0.12f),
                    edgeGlow = accent.copy(alpha = 0.08f),
                    depth = Color.Black.copy(alpha = 0.38f)
                )
            }

            mode == ThemeMode.LIGHT -> GlassLook(
                frostedFill = tint ?: p.glassBase.copy(alpha = if (strong) 0.95f else 0.88f),
                clearTint = tint ?: p.glassBase.copy(alpha = if (strong) 0.88f else 0.72f),
                sheen = Color.White.copy(alpha = 0.50f),
                rimHi = Color.White,
                rimLo = p.outlineMedium.copy(alpha = 0.35f),
                topLight = Color.White.copy(alpha = 0.60f),
                edgeGlow = accent.copy(alpha = 0.12f),
                depth = accent.copy(alpha = 0.06f)
            )

            mode == ThemeMode.AMOLED -> GlassLook(
                frostedFill = tint ?: p.glassBase.copy(alpha = if (strong) 0.98f else 0.92f),
                clearTint = tint ?: p.glassBase.copy(alpha = if (strong) 0.92f else 0.80f),
                sheen = Color.White.copy(alpha = 0.04f),
                rimHi = lerp(Color.White, hi, 0.55f).copy(alpha = 0.40f),
                rimLo = hi.copy(alpha = 0.07f),
                topLight = Color.White.copy(alpha = 0.08f),
                edgeGlow = accent.copy(alpha = 0.15f),
                depth = Color.Black
            )

            else -> GlassLook(
                frostedFill = tint ?: p.glassBase.copy(alpha = if (strong) 0.96f else 0.88f),
                clearTint = tint ?: p.glassBase.copy(alpha = if (strong) 0.88f else 0.72f),
                sheen = Color.White.copy(alpha = 0.08f),
                rimHi = lerp(Color.White, hi, 0.50f).copy(alpha = 0.42f),
                rimLo = hi.copy(alpha = 0.08f),
                topLight = Color.White.copy(alpha = 0.15f),
                edgeGlow = accent.copy(alpha = 0.10f),
                depth = Color.Black.copy(alpha = 0.42f)
            )
        }
    }
}

/**
 * Paints the liquid-glass layers onto an already-clipped node. Press / selection / touch
 * are read as lambdas so animating them only redraws — it never recomposes.
 * The caller must clip to [shape] first (the rim's outer half is clipped away on purpose).
 */
fun Modifier.liquidGlassDecor(
    shape: Shape,
    look: GlassLook,
    accent: Color,
    paintFill: Boolean = true,
    press: () -> Float = { 0f },
    selected: () -> Float = { 0f },
    touch: () -> Offset = { Offset.Unspecified }
): Modifier = drawWithCache {
    val w = size.width
    val h = size.height
    val outline = shape.createOutline(size, layoutDirection, this)
    val path = Path().apply { addOutline(outline) }
    val rimW = 2.4.dp.toPx()          // half is clipped → 1.2dp visible
    // Lens edge is built from stacked, progressively thinner strokes so the light
    // FALLS OFF smoothly toward the centre. One thick stroke left a hard inner edge,
    // which read as a second 'box' inside every card, pill and button.
    // 10 hairline-thin steps whose widths shrink toward the rim: perfectly smooth falloff,
    // and capped to a fraction of the shape so small badges/chips don't light up entirely.
    val edgeMax = minOf(20.dp.toPx(), minOf(w, h) * 0.34f)
    val edgeSteps = 10
    val edgeWidths = FloatArray(edgeSteps) { i -> edgeMax * (1f - i / edgeSteps.toFloat()) + 1.5.dp.toPx() }
    val diag = Offset(w, h)

    val caustic = Brush.linearGradient(
        listOf(
            accent.copy(alpha = 0.085f),
            Color.Transparent,
            look.rimHi.copy(alpha = 0.035f)
        ),
        start = Offset.Zero,
        end = diag
    )

    val sheen = Brush.verticalGradient(
        0f to look.sheen.copy(alpha = look.sheen.alpha * 0.55f),
        0.35f to Color.Transparent,
        startY = 0f,
        endY = h
    )

    val depth = Brush.verticalGradient(
        0.58f to Color.Transparent, 1f to look.depth, startY = 0f, endY = h
    )
    val edge = Brush.linearGradient(
        listOf(look.edgeGlow.copy(alpha = look.edgeGlow.alpha * 0.10f), Color.Transparent, look.edgeGlow.copy(alpha = look.edgeGlow.alpha * 0.06f)),
        start = Offset.Zero, end = diag
    )
    val rim = Brush.linearGradient(
        listOf(look.rimHi, look.rimLo, look.rimHi.copy(alpha = look.rimHi.alpha * 0.55f)),
        start = Offset.Zero, end = diag
    )
    val topRim = Brush.verticalGradient(
        0f to look.topLight.copy(alpha = look.topLight.alpha * 0.55f),
        0.12f to Color.Transparent,
        startY = 0f,
        endY = h
    )

    onDrawWithContent {
        if (paintFill && look.frostedFill.alpha > 0f) drawOutline(outline, look.frostedFill)
        drawOutline(outline, caustic)
        drawOutline(outline, sheen)
        drawOutline(outline, depth)
        clipPath(path) { for (w in edgeWidths) drawOutline(outline, edge, style = Stroke(w)) }

        val p = press()
        if (p > 0.001f) {
            val c = touch().let { if (it == Offset.Unspecified) Offset(w / 2f, h / 2f) else it }
            drawRect(
                Brush.radialGradient(
                    listOf(Color.White.copy(alpha = 0.30f * p), Color.Transparent),
                    center = c, radius = max(w, h) * 0.85f
                )
            )
        }

        drawContent()

        drawOutline(outline, rim, style = Stroke(rimW))
        drawOutline(outline, topRim, style = Stroke(rimW))
        val s = selected()
        if (s > 0.001f) {
            drawOutline(
                outline,
                Brush.verticalGradient(
                    listOf(accent.copy(alpha = 0.95f * s), accent.copy(alpha = 0.40f * s)),
                    startY = 0f, endY = h
                ),
                style = Stroke(rimW * 1.35f)
            )
        }
    }
}

/** Flipped to true shortly after the home screen first appears; see [glassBlur]. */
object GlassWarmup {
    private val _ready = androidx.compose.runtime.mutableStateOf(false)
    var ready: Boolean
        get() = _ready.value
        set(v) { _ready.value = v }
}

/**
 * Live backdrop blur for chrome surfaces.
 * backgroundColor MUST be opaque: Haze blurs the source pixels and composites them over it,
 * so without it a translucent source gives a translucent (invisible) blur.
 */
@Composable
fun Modifier.glassBlur(radius: Dp, tint: Color): Modifier {
    val haze = LocalGlassHaze.current
    val backdrop = LocalGlassBackdrop.current
    val p = LocalAppUiSettings.current.palette
    return when {
        haze != null -> {
            // Cold-start warm-up: for the first second the live blur is skipped (a frosted panel stands in),
            // so the first frames aren't fighting JIT + thumbnail decoding + blur passes. The panel then
            // fades out as the real blur takes over.
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
                            noiseFactor = 0.035f,
                            // Android 7–11 can't blur; use a deliberate frosted panel instead of a flat tint.
                            fallbackTint = HazeTint(p.surfaceRaised.copy(alpha = 0.90f))
                        )
                    ) else Modifier
                )
                .then(if (cover > 0.01f) Modifier.background(p.surfaceRaised.copy(alpha = 0.92f * cover)) else Modifier)
        }
        backdrop != null -> this.backdropBlur(backdrop).background(tint)
        else -> this
    }
}

/**
 * Marks a scrolling list/grid as blur source. Put it on the LazyColumn / LazyVerticalGrid ONLY,
 * never on a parent that also contains a blurred bar (Haze can't blur its own ancestor).
 */
@Composable
fun Modifier.glassSource(): Modifier {
    val haze = LocalGlassHaze.current
    return if (haze != null) this.hazeSource(haze, zIndex = 0f) else this
}

@Composable
fun rememberHasLiveBlur(): Boolean =
    LocalGlassHaze.current != null || LocalGlassBackdrop.current != null

/**
 * Accent-tinted glass capsule — the "selected" indicator of the tab bar, theme selector, etc.
 * More saturated than regular glass but still carries the sheen, lens edge and specular rim.
 */
fun Modifier.liquidLens(accent: Color, shape: Shape = CircleShape): Modifier = this
    .clip(shape)
    .background(Brush.linearGradient(listOf(accent.copy(alpha = 0.85f), accent, accent.copy(alpha = 0.95f))))
    .liquidGlassDecor(shape, LensLook, accent, paintFill = false)

/** Lightweight, non-interactive glass for badges / chips / tags (cheap enough for list items). */
fun Modifier.glassBadge(
    fill: Color,
    rim: Color = Color.White,
    shape: Shape = CircleShape
): Modifier {
    val look = GlassLook(
        frostedFill = fill, clearTint = fill,
        sheen = Color.White.copy(alpha = 0.22f),
        rimHi = rim.copy(alpha = 0.55f), rimLo = rim.copy(alpha = 0.10f),
        topLight = Color.White.copy(alpha = 0.34f),
        edgeGlow = Color.White.copy(alpha = 0.10f),
        depth = Color.Black.copy(alpha = 0.16f)
    )
    return this.clip(shape).liquidGlassDecor(shape, look, rim)
}

/**
 * Fades the bottom of a blurred bar to nothing — iOS 26's soft "scroll edge" instead of a
 * hard bar. Apply BEFORE the blur modifier so the mask covers the blurred output.
 */
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

/** Glass chip: regular glass when idle, accent lens when [selected]. */
@Composable
fun GlassChip(
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    shape: Shape = CircleShape,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    if (selected) {
        val accent = LocalAppUiSettings.current.accent
        val interaction = remember { MutableInteractionSource() }
        val pressed by interaction.collectIsPressedAsState()
        val haptic = rememberHaptic()
        androidx.compose.foundation.layout.Box(
            modifier = modifier
                .graphicsLayer { val s = if (pressed) 0.95f else 1f; scaleX = s; scaleY = s }
                .liquidLens(accent, shape)
                .then(
                    if (onClick != null) Modifier.clickable(interactionSource = interaction, indication = null) { haptic(); onClick() }
                    else Modifier
                ),
            content = content
        )
    } else {
        GlassSurface(modifier = modifier, shape = shape, onClick = onClick, content = content)
    }
}

/** Touch-point tracker so the press bloom blooms where the finger lands. Never consumes. */
fun Modifier.trackTouch(onDown: (Offset) -> Unit): Modifier = this.pointerInput(Unit) {
    awaitEachGesture {
        val d = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
        onDown(d.position)
    }
}

@Composable
internal fun rememberTouchState() = remember { mutableStateOf(Offset.Unspecified) }

/** Container tint for text fields so they read as recessed glass. */
@Composable
fun glassFieldFill(): Color {
    val s = LocalAppUiSettings.current
    return if (s.palette.isDark) Color.White.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.62f)
}

/** Selectable list row: flat when idle, accent-tinted glass with a lit rim when [selected]. */
@Composable
fun GlassRow(
    onClick: () -> Unit,
    selected: Boolean,
    modifier: Modifier = Modifier,
    shape: Shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
    content: @Composable BoxScope.() -> Unit
) {
    val accent = LocalAppUiSettings.current.accent
    val look = rememberGlassLook()
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val haptic = rememberHaptic()
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
                else Modifier.background(if (pressed) Color.White.copy(alpha = 0.07f) else Color.Transparent)
            )
            .clickable(
                interactionSource = interaction, indication = null,
                role = androidx.compose.ui.semantics.Role.RadioButton
            ) { haptic(); onClick() },
        content = content
    )
}