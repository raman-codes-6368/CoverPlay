package com.raman.CoverPlay.ui.theme

import android.os.Build
import android.view.WindowManager
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import com.raman.CoverPlay.R
import com.raman.CoverPlay.components.CoverPlayQuipLine
import com.raman.CoverPlay.components.Quips
import com.raman.CoverPlay.player.CoverPlayPlayerPanel
import com.raman.CoverPlay.util.rememberCoverPlayHaptic
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import kotlin.math.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class OrganicShape(
    private val lobes: Int = 12
) : Shape {
    override fun equals(other: Any?): Boolean = other is OrganicShape && other.lobes == lobes
    override fun hashCode(): Int = lobes

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path()
        val cx = size.width / 2f
        val cy = size.height / 2f

        val maxPossibleRadius = min(size.width, size.height) / 2f
        val radius = maxPossibleRadius * 0.88f

        val points = 180

        for (i in 0..points) {
            val angle = 2f * PI.toFloat() * i / points
            val wave = sin(angle * lobes) * radius * 0.05f
            val r = radius + wave
            val x = cx + cos(angle) * r
            val y = cy + sin(angle) * r

            if (i == 0) path.moveTo(x, y)
            else path.lineTo(x, y)
        }
        path.close()
        return Outline.Generic(path)
    }
}

const val ENABLE_THEME_SCENES = false

object CoverPlayMotion {
    val standardEasing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    val standardTween = tween<Float>(durationMillis = 300, easing = standardEasing)
    const val sheetEnterMillis = 420
    val cardLiftSpring = spring<Float>(dampingRatio = 0.70f, stiffness = 400f)
    val tabSelectionSpring = spring<Float>(dampingRatio = 0.75f, stiffness = 500f)
}

class CoverPlayStretchState(val density: Density) {
    val stretchAnim = Animatable(0f)
    private var settleJob: kotlinx.coroutines.Job? = null

    val stretchPx: Float get() = stretchAnim.value

    suspend fun snapTo(value: Float) {
        settleJob?.cancel()
        stretchAnim.snapTo(value)
    }

    fun settle(scope: kotlinx.coroutines.CoroutineScope, initialVelocity: Float = 0f, stiffness: Float = 320f) {
        settleJob?.cancel()
        settleJob = scope.launch {
            stretchAnim.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = stiffness
                ),
                initialVelocity = initialVelocity
            )
        }
    }
}

@Composable
fun rememberCoverPlayStretchState(): CoverPlayStretchState {
    val density = LocalDensity.current
    return remember(density) { CoverPlayStretchState(density) }
}

@Composable
fun rememberCoverPlayOverscrollConnection(
    state: CoverPlayStretchState = rememberCoverPlayStretchState(),
    maxStretchDp: Dp = 180.dp
): androidx.compose.ui.input.nestedscroll.NestedScrollConnection {
    val scope = rememberCoroutineScope()
    val maxStretchPx = with(state.density) { maxStretchDp.toPx() }

    return remember(state, maxStretchPx, scope) {
        object : androidx.compose.ui.input.nestedscroll.NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: androidx.compose.ui.input.nestedscroll.NestedScrollSource
            ): Offset {
                if (available.y > 0f) {
                    val current = state.stretchPx
                    val resistance = 0.55f * (1f - current / maxStretchPx).coerceIn(0f, 1f)
                    val next = (current + available.y * resistance).coerceAtMost(maxStretchPx)
                    scope.launch { state.snapTo(next) }
                    return Offset(0f, available.y)
                }
                return Offset.Zero
            }

            override suspend fun onPreFling(available: androidx.compose.ui.unit.Velocity): androidx.compose.ui.unit.Velocity {
                if (state.stretchPx > 0.5f) {
                    state.settle(scope, initialVelocity = available.y * 0.35f, stiffness = 320f)
                    return androidx.compose.ui.unit.Velocity(0f, available.y)
                }
                return androidx.compose.ui.unit.Velocity.Zero
            }

            override suspend fun onPostFling(consumed: androidx.compose.ui.unit.Velocity, available: androidx.compose.ui.unit.Velocity): androidx.compose.ui.unit.Velocity {
                if (available.y > 0f || state.stretchPx > 0.5f) {
                    state.settle(scope, initialVelocity = available.y * 60f, stiffness = 110f)
                    return androidx.compose.ui.unit.Velocity(0f, available.y)
                }
                return androidx.compose.ui.unit.Velocity.Zero
            }
        }
    }
}

fun Modifier.heroStretchZoom(stretchPx: () -> Float): Modifier = graphicsLayer {
    val stretch = stretchPx().coerceAtLeast(0f)
    val h = size.height.coerceAtLeast(1f)
    val zoom = 1f + stretch / h
    transformOrigin = TransformOrigin(0.5f, 0f)
    scaleX = zoom
    scaleY = zoom
}

object MotionSpec {
    val SoftSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMediumLow
    )
    val EmphasizedSpring = spring<Float>(
        dampingRatio = 0.75f,
        stiffness = 500f
    )
    val SpatialSpring = spring<Float>(
        dampingRatio = 0.65f,
        stiffness = 300f
    )
    val EmphasizedEasing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    const val DURATION_SHORT = 250
    const val DURATION_MEDIUM = 500
    const val DURATION_LONG = 800
}

object DialogBlur { var count by mutableIntStateOf(0) }

object GlassTokens {
    val OverVideoFill       = Color(0x8F0B0B0D)
    val OverVideoFillStrong = Color(0xC70A0A0C)
    val SpringPress         = spring<Float>(dampingRatio = 0.55f, stiffness = 520f)
}

val LocalOverVideo = compositionLocalOf { false }

@Composable
fun CoverPlayOverVideoScope(content: @Composable () -> Unit) {
    content()
}

fun Modifier.cinematicOverlay(accent: Color, highlight: Color, dark: Boolean, boost: Float = 0f): Modifier =
    this.drawBehind {
        val w = size.width
        val h = size.height
        val reach = max(w, h)
        if (dark) {
            val ink = lerp(Color.Black, accent, 0.02f)
            drawRect(
                Brush.verticalGradient(
                    0f to ink.copy(alpha = (0.50f + boost).coerceAtMost(1f)),
                    0.45f to ink.copy(alpha = (0.68f + boost).coerceAtMost(1f)),
                    1f to Color.Black.copy(alpha = (0.88f + boost * 0.5f).coerceAtMost(1f))
                )
            )
        } else {
            drawRect(
                Brush.verticalGradient(
                    0f to Color.White.copy(alpha = 0.64f),
                    1f to lerp(Color.White, highlight, 0.04f).copy(alpha = 0.90f)
                )
            )
        }
    }

@Composable
fun Modifier.aliveClickable(
    enabled: Boolean = true,
    pressedScale: Float = 0.94f,
    haptic: Boolean = true,
    onClick: () -> Unit
): Modifier {
    val interaction = remember { MutableInteractionSource() }
    val scale by rememberCoverPlayPressScale(interaction, pressedScale)
    val buzz = rememberCoverPlayHaptic()
    return this
        .graphicsLayer { scaleX = scale; scaleY = scale }
        .clickable(interactionSource = interaction, indication = null, enabled = enabled) {
            if (haptic) buzz()
            onClick()
        }
}

@Composable
fun Modifier.staggerIn(index: Int, stepMs: Int = 45): Modifier {
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay((index.coerceAtLeast(0) % 8) * stepMs.toLong())
        shown = true
    }
    val p by animateFloatAsState(
        targetValue = if (shown) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.72f, stiffness = 260f),
        label = "stagger"
    )
    return this.graphicsLayer {
        alpha = p.coerceIn(0f, 1f)
        val sc = 0.90f + 0.10f * p
        scaleX = sc; scaleY = sc
        translationY = (1f - p) * 36f
    }
}

@Composable
fun Modifier.floating(amplitudeDp: Float = 3f, periodMs: Int = 3200): Modifier {
    val t = rememberInfiniteTransition(label = "float")
    val v by t.animateFloat(-1f, 1f, infiniteRepeatable(tween(periodMs, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "floatV")
    return this.graphicsLayer { translationY = v * amplitudeDp * density }
}

@Composable
fun CoverPlayCinematicPanel(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    @Suppress("UNUSED_PARAMETER") elevation: Dp = 0.dp,
    strong: Boolean = true,
    overVideo: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    CoverPlayOverVideoScope {
        val st = LocalAppUiSettings.current
        CoverPlayGlassSurface(
            modifier = modifier,
            shape = shape,
            overVideo = true,
            strong = true,
            tint = Color.Transparent,
            elevation = 0.dp
        ) {
            Box(Modifier.matchParentSize().windowFrost(st.palette.isDark, st.accent, blurred = false, solid = 0.78f))
            Box(Modifier.matchParentSize().cinematicOverlay(st.accent, st.highlight, st.palette.isDark))
            content()
        }
    }
}

@Composable
fun rememberCoverPlayPressScale(
    interactionSource: MutableInteractionSource,
    pressed: Float = 0.94f
): State<Float> {
    val isPressed by interactionSource.collectIsPressedAsState()
    return animateFloatAsState(
        targetValue = if (isPressed) pressed else 1f,
        animationSpec = GlassTokens.SpringPress,
        label = "pressScale"
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CoverPlayGlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    strong: Boolean = false,
    overVideo: Boolean = false,
    selected: Boolean = false,
    glow: Color? = null,
    elevation: Dp = 0.dp,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    blur: Boolean = false,
    frosted: Boolean = false,
    enabled: Boolean = true,
    tint: Color? = null,
    hapticOnClick: Boolean = true,
    pressedScale: Float = 0.965f,
    content: @Composable BoxScope.() -> Unit
) {
    val settings = LocalAppUiSettings.current
    val accent = settings.accent
    val isLight = settings.themeMode == ThemeMode.LIGHT && !overVideo
    val liveBlur = blur && !overVideo && rememberCoverPlayHasLiveBlur()
    val baseLook = rememberCoverPlayGlassLook(overVideo, strong, tint)
    val look = remember(baseLook, frosted, liveBlur) {
        if (frosted && !liveBlur && !overVideo) baseLook.copy(frostedFill = baseLook.clearTint) else baseLook
    }
    val clickable = enabled && (onClick != null || onLongClick != null)

    val interaction = remember { MutableInteractionSource() }
    val scale by rememberCoverPlayPressScale(interaction, pressedScale)
    val pressed by interaction.collectIsPressedAsState()
    val bloom = animateFloatAsState(
        targetValue = if (pressed && clickable) 1f else 0f,
        animationSpec = tween(160), label = "glassBloom"
    )
    val selectedAnim = animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = tween(MotionSpec.DURATION_SHORT, easing = MotionSpec.EmphasizedEasing),
        label = "glassSelected"
    )
    val selPop = animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = spring(dampingRatio = 1f, stiffness = 500f), label = "glassPop"
    )
    var touch by remember { mutableStateOf(Offset.Unspecified) }
    val haptic = rememberCoverPlayHaptic()
    val longHaptic = rememberCoverPlayHaptic(HapticFeedbackType.LongPress)
    val glowColor = glow ?: accent
    val disabledAlpha = if (!enabled && onClick != null) 0.45f else 1f

    Box(
        modifier = modifier
            .graphicsLayer {
                val pop = scale
                scaleX = pop; scaleY = pop; alpha = disabledAlpha
            }
            .then(
                if (elevation > 0.dp && overVideo) Modifier.shadow(
                    elevation, shape,
                    ambientColor = Color.Black.copy(alpha = 0.30f),
                    spotColor = Color.Black.copy(alpha = 0.40f)
                ) else Modifier
            )
            .clip(shape)
            .then(if (liveBlur) Modifier.glassBlur(if (isLight) 30.dp else 26.dp, look.clearTint.copy(alpha = minOf(look.clearTint.alpha, 0.72f))) else Modifier)
            .liquidGlassDecor(
                shape = shape, look = look, accent = glowColor,
                paintFill = !liveBlur,
                press = { bloom.value },
                selected = { selectedAnim.value },
                touch = { touch }
            )
            .then(
                if (clickable) Modifier
                    .trackTouch { touch = it }
                    .combinedClickable(
                        interactionSource = interaction,
                        indication = null,
                        role = Role.Button,
                        onClick = { if (hapticOnClick) haptic(); onClick?.invoke() },
                        onLongClick = onLongClick?.let { lc -> { longHaptic(); lc() } }
                    )
                else Modifier
            ),
        content = content
    )
}

@Composable
fun rememberCoverPlayWindowBlurActive(): Boolean {
    val ctx = LocalContext.current
    return remember {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                runCatching { ctx.getSystemService(WindowManager::class.java).isCrossWindowBlurEnabled }.getOrDefault(false)
    }
}

@Composable
fun Modifier.windowFrost(dark: Boolean, accent: Color, blurred: Boolean, solid: Float = 0.80f): Modifier {
    val base = if (dark) lerp(Color(0xFF0E0B14), accent, 0.02f) else Color.White
    return this.background(base)
}

@Composable
fun CoverPlayDialogBackdrop(
    blurRadius: Int = 40,
    dim: Float = if (WindowBlurSupported) 0.30f else 0.5f
) {
    val view = LocalView.current
    DisposableEffect(Unit) { DialogBlur.count++; onDispose { DialogBlur.count-- } }
    SideEffect {
        val window = (view.parent as? DialogWindowProvider)?.window ?: return@SideEffect
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
        window.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))
        window.addFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
        )
        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val lp0 = window.attributes
            lp0.layoutInDisplayCutoutMode =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
                else WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            window.attributes = lp0
        }
        window.setDimAmount(dim)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            window.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
            val lp = window.attributes
            lp.blurBehindRadius = blurRadius
            window.attributes = lp
        }
    }
}

@Composable
private fun CoverPlayDialogShell(
    onDismiss: () -> Unit,
    modifier: Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val overVideo = LocalOverVideo.current
    val blurActive = rememberCoverPlayWindowBlurActive()
    val maxHeight = (LocalConfiguration.current.screenHeightDp * 0.86f).dp
    val snapshot = rememberCoverPlayWindowSnapshot(enabled = !overVideo)
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        CoverPlayDialogBackdrop(blurRadius = 48, dim = if (blurActive) 0.38f else 0.55f)
        CompositionLocalProvider(LocalGlassHaze provides null, LocalGlassBackdrop provides null) {
            val body: @Composable () -> Unit = {
                val st = LocalAppUiSettings.current
                val dark = st.palette.isDark
                var shown by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) { shown = true }
                val progress by animateFloatAsState(
                    targetValue = if (shown) 1f else 0f,
                    animationSpec = tween(300, easing = MotionSpec.EmphasizedEasing),
                    label = "dialogIn"
                )
                val blurIn by animateFloatAsState(
                    targetValue = if (snapshot != null) 1f else 0f,
                    animationSpec = tween(360, easing = MotionSpec.EmphasizedEasing),
                    label = "dialogBlurIn"
                )
                CoverPlayGlassSurface(
                    modifier = modifier
                        .heightIn(max = maxHeight)
                        .graphicsLayer {
                            alpha = progress.coerceIn(0f, 1f)
                            val sc = 0.96f + 0.04f * progress
                            scaleX = sc; scaleY = sc
                            translationY = (1f - progress) * 16f
                        },
                    shape = RoundedCornerShape(CoverPlayTokens.Radius.sheet),
                    strong = true,
                    overVideo = overVideo,
                    tint = Color.Transparent,
                    elevation = 0.dp
                ) {
                    Box(Modifier.matchParentSize().graphicsLayer { alpha = blurIn }.snapshotGlass(snapshot))
                    Box(
                        Modifier.matchParentSize().background(
                            (if (overVideo && st.palette.isDark) st.palette.surfaceNavy else st.palette.containerHigh)
                                .copy(alpha = 0.94f - 0.18f * blurIn)
                        )
                    )
                    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp), content = content)
                }
            }
            if (overVideo) CoverPlayOverVideoScope(body) else body()
        }
    }
}

@Composable
fun CoverPlayCinematicBackground(
    modifier: Modifier = Modifier,
    ambientArtUri: Any? = null,
    backdrop: BackdropState? = LocalGlassBackdrop.current,
    hazeState: HazeState? = LocalGlassHaze.current,
    overlay: @Composable BoxScope.() -> Unit = {},
    content: @Composable BoxScope.() -> Unit
) {
    val settings = LocalAppUiSettings.current
    val palette = settings.palette

    if (ambientArtUri != null) {
        CoverPlayProvideArtTint(ambientArtUri)
    }

    val bgSource = when {
        hazeState != null -> Modifier.hazeSource(hazeState, zIndex = 0f)
        backdrop != null -> Modifier.backdropSource(backdrop)
        else -> Modifier
    }

    Box(modifier = modifier.fillMaxSize().background(palette.bg)) {
        Box(modifier = Modifier.fillMaxSize().then(bgSource).background(palette.bg)) {
            CoverPlayShapeBackdrop()
        }

        CompositionLocalProvider(
            LocalGlassHaze provides hazeState,
            LocalGlassBackdrop provides backdrop
        ) {
            Box(modifier = Modifier.fillMaxSize()) { content() }
            overlay()
        }
    }
}

private fun DrawScope.orb(cx: Float, cy: Float, r: Float, c: Color) {
    if (c.alpha <= 0.001f || r <= 0f) return
    val center = Offset(cx, cy)
    drawCircle(
        brush = Brush.radialGradient(
            0f to c,
            0.35f to c.copy(alpha = c.alpha * 0.55f),
            0.70f to c.copy(alpha = c.alpha * 0.15f),
            1f to Color.Transparent,
            center = center, radius = r
        ),
        radius = r, center = center
    )
}

private fun DrawScope.petals(cx: Float, cy: Float, r: Float, rotation: Float, count: Int, inner: Color, outer: Color) {
    val center = Offset(cx, cy)
    for (i in 0 until count) {
        rotate(rotation + 360f * i / count, center) {
            drawOval(
                brush = Brush.linearGradient(0f to inner, 1f to outer, start = center, end = Offset(cx, cy - r)),
                topLeft = Offset(cx - r * 0.31f, cy - r),
                size = Size(r * 0.62f, r)
            )
        }
    }
}

private fun hash01(i: Int, salt: Float): Float {
    val v = sin(i * 12.9898f + salt * 78.233f) * 43758.547f
    return v - floor(v)
}

private fun DrawScope.shootingStar(p: Float, seed: Int, color: Color, px: Float) {
    val k = p / 0.20f
    if (k < 0f || k > 1f) return
    val w = size.width
    val h = size.height
    val x0 = w * (0.30f + 0.65f * hash01(seed, 11f))
    val y0 = h * (0.04f + 0.30f * hash01(seed, 12f))
    val len = w * 0.26f
    val dx = -len * 0.84f
    val dy = len * 0.54f
    val head = Offset(x0 + dx * k * 2.2f, y0 + dy * k * 2.2f)
    val tail = Offset(head.x - dx * 0.55f, head.y - dy * 0.55f)
    val fade = sin(k * PI.toFloat())
    drawLine(
        brush = Brush.linearGradient(
            listOf(Color.Transparent, color.copy(alpha = 0.85f * fade)), start = tail, end = head
        ),
        start = tail, end = head, strokeWidth = 1.8f * px, cap = StrokeCap.Round
    )
    drawCircle(color.copy(alpha = fade), radius = 2.2f * px, center = head)
}

private fun DrawScope.mote(cx: Float, cy: Float, r: Float, c: Color) {
    if (c.alpha <= 0.01f) return
    drawCircle(
        brush = Brush.radialGradient(listOf(c, c.copy(alpha = c.alpha * 0.35f), Color.Transparent), center = Offset(cx, cy), radius = r * 3f),
        radius = r * 3f, center = Offset(cx, cy)
    )
}

private fun DrawScope.sparkle(cx: Float, cy: Float, r: Float, c: Color) {
    if (c.alpha <= 0.01f) return
    mote(cx, cy, r, c)
    val arm = r * 6f
    val line = c.copy(alpha = c.alpha * 0.7f)
    drawLine(line, Offset(cx - arm, cy), Offset(cx + arm, cy), strokeWidth = r * 0.5f, cap = StrokeCap.Round)
    drawLine(line, Offset(cx, cy - arm), Offset(cx, cy + arm), strokeWidth = r * 0.5f, cap = StrokeCap.Round)
}

private fun DrawScope.ridge(baseY: Float, amp: Float, freq: Float, phase: Float, color: Color) {
    val w = size.width
    val h = size.height
    val tau = 2f * PI.toFloat()
    val p = Path()
    p.moveTo(0f, h)
    val steps = 28
    for (k in 0..steps) {
        val x = w * k / steps
        val y = baseY + amp * sin(x / w * tau * freq + phase) + amp * 0.45f * sin(x / w * tau * freq * 2.3f + phase * 1.7f)
        p.lineTo(x, y)
    }
    p.lineTo(w, h)
    p.close()
    drawPath(p, color)
}

private fun DrawScope.fluidBlob(cx: Float, cy: Float, r: Float, sx: Float, sy: Float, rot: Float, c: Color) {
    val pivot = Offset(cx, cy)
    withTransform({ rotate(rot, pivot); scale(sx, sy, pivot) }) { orb(cx, cy, r, c) }
}

private fun DrawScope.silk(baseY: Float, thick: Float, amp: Float, freq: Float, phase: Float, tilt: Float, c: Color) {
    if (c.alpha <= 0.002f) return
    val w = size.width
    val tau = 2f * PI.toFloat()
    val steps = 28
    val path = Path()
    for (k in 0..steps) {
        val x = w * k / steps
        val y = baseY + amp * sin(x / w * tau * freq + phase)
        if (k == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    for (k in steps downTo 0) {
        val x = w * k / steps
        path.lineTo(x, baseY + thick + amp * 0.8f * sin(x / w * tau * freq + phase + 1.4f))
    }
    path.close()
    rotate(tilt, Offset(w / 2f, baseY + thick / 2f)) {
        drawPath(
            path,
            Brush.verticalGradient(
                0f to Color.Transparent, 0.5f to c, 1f to Color.Transparent,
                startY = baseY - amp, endY = baseY + thick + amp
            )
        )
    }
}

private fun DrawScope.irisRipples(t: Float, c: Color, s: Float, px: Float) {
    val cx = size.width * 0.96f
    val cy = size.height * 0.08f
    for (i in 0 until 3) {
        val ph = (t * 3f + i / 3f) % 1f
        val fade = (1f - ph) * (1f - ph)
        drawCircle(c.copy(alpha = 0.10f * fade * s), radius = size.width * (0.12f + 0.85f * ph), center = Offset(cx, cy), style = Stroke(1.3f * px))
    }
}

private fun DrawScope.nebulaDrift(t1: Float, t2: Float, c: Float, speck: Color, s: Float, px: Float) {
    val w = size.width
    val h = size.height
    for (i in 0 until 12) {
        val x = ((hash01(i, 71f) * w - t2 * w) % w + w) % w
        val tw = 0.5f + 0.5f * sin(c + i * 1.3f)
        drawCircle(speck.copy(alpha = (0.04f + 0.14f * tw) * s), 0.7f * px, Offset(x, h * (0.05f + 0.80f * hash01(i, 72f))))
    }
    for (i in 0 until 7) {
        val x = ((hash01(i, 81f) * w - t1 * w * 2f) % w + w) % w
        val tw = 0.5f + 0.5f * sin(c * 2f + i * 2.1f)
        mote(x, h * (0.08f + 0.70f * hash01(i, 82f)), 1.1f * px, speck.copy(alpha = (0.05f + 0.16f * tw) * s))
    }
}

private fun DrawScope.velvetSpot(b: Float, color: Color, s: Float) {
    val w = size.width
    val h = size.height
    val ax = w * 0.5f
    val bx = w * 0.5f + sin(b) * w * 0.34f
    val half = w * 0.17f
    val p = Path().apply {
        moveTo(ax - w * 0.015f, -h * 0.02f); lineTo(ax + w * 0.015f, -h * 0.02f)
        lineTo(bx + half, h); lineTo(bx - half, h); close()
    }
    drawPath(p, Brush.verticalGradient(0f to color.copy(alpha = 0.075f * s), 0.7f to color.copy(alpha = 0.02f * s), 1f to Color.Transparent))
    orb(bx, h * 0.97f, w * 0.40f, color.copy(alpha = 0.07f * s))
}

private fun DrawScope.emberRays(t1: Float, color: Color, s: Float, px: Float) {
    val w = size.width
    val h = size.height
    val sun = Offset(w * 0.5f, h)
    val len = h * 1.25f
    val spread = len * 0.085f
    val brush = Brush.radialGradient(0f to color.copy(alpha = 0.075f * s), 1f to Color.Transparent, center = sun, radius = len)
    for (i in 0 until 9) {
        rotate(t1 * 360f / 9f + i * 40f, sun) {
            val p = Path().apply { moveTo(sun.x, sun.y); lineTo(sun.x - spread, sun.y - len); lineTo(sun.x + spread, sun.y - len); close() }
            drawPath(p, brush)
        }
    }
    val tau = 2f * PI.toFloat()
    for (i in 0 until 3) {
        val ph = (t1 * 2f + i / 3f) % 1f
        val y0 = h * (0.96f - 0.55f * ph)
        val path = Path()
        for (k in 0..20) {
            val x = w * k / 20f
            val y = y0 + sin(x / w * tau * 3f + ph * tau * 2f) * 3.5f * px
            if (k == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, color.copy(alpha = 0.06f * sin(ph * PI.toFloat()) * s), style = Stroke(1.1f * px))
    }
}

private fun DrawScope.auroraSea(c: Float, a: Float, color: Color, s: Float, px: Float) {
    val w = size.width
    val h = size.height
    drawRect(
        Brush.verticalGradient(0f to Color.Transparent, 1f to color.copy(alpha = 0.05f * s), startY = h * 0.84f, endY = h),
        topLeft = Offset(0f, h * 0.84f), size = Size(w, h * 0.16f)
    )
    for (i in 0 until 20) {
        val tw = 0.5f + 0.5f * sin(c * (1f + i % 2) + i * 1.9f)
        val x = hash01(i, 91f) * w + sin(a + i) * w * 0.01f
        val y = h * (0.86f + 0.13f * hash01(i, 92f))
        val len = w * (0.02f + 0.05f * hash01(i, 93f))
        drawLine(color.copy(alpha = 0.16f * tw * s), Offset(x - len, y), Offset(x + len, y), strokeWidth = 1.1f * px, cap = StrokeCap.Round)
    }
}

private data class SceneSpec(
    val scene: BackdropScene,
    val accent: Color,
    val highlight: Color,
    val secondary: Color,
    val strength: Float,
    val isLight: Boolean
)

@Composable
private fun CoverPlayThemeSceneLayer(
    scene: BackdropScene,
    accent: Color,
    highlight: Color,
    secondary: Color,
    strength: Float,
    isLight: Boolean,
    reveal: State<Float>,
    t1: State<Float>,
    t2: State<Float>,
    t3: State<Float>,
    breathe: State<Float>
) {
    Box(
        modifier = Modifier.fillMaxSize().drawBehind {
            val w = size.width
            val h = size.height
            val px = 1.dp.toPx()
            val tau = 2f * PI.toFloat()
            val a = t1.value * tau
            val b = t2.value * tau
            val c = t3.value * tau
            val br = breathe.value
            val s = strength * reveal.value
            if (s <= 0.002f) return@drawBehind
            val speck = if (isLight) highlight else Color.White

            when (scene) {
                BackdropScene.BLOOM -> {
                    irisRipples(t1.value, highlight, s, px)
                    fluidBlob(w * (0.25f + 0.12f * sin(a)), h * (0.18f + 0.07f * cos(b)), w * 0.95f * br, 1.25f + 0.15f * sin(b), 0.85f, sin(a) * 30f, accent.copy(alpha = 0.24f * s))
                    fluidBlob(w * (0.85f + 0.08f * cos(a)), h * (0.55f + 0.08f * sin(b)), w * 0.80f, 0.90f, 1.30f + 0.15f * cos(a), -20f + cos(b) * 25f, secondary.copy(alpha = 0.14f * s))
                    fluidBlob(w * (0.30f + 0.10f * sin(b)), h * (0.92f + 0.04f * cos(a)), w * 0.90f, 1.40f, 0.80f, cos(a) * 20f, highlight.copy(alpha = 0.10f * s))
                    silk(h * 0.30f, h * 0.16f, h * 0.035f, 1.2f, a, -8f, highlight.copy(alpha = 0.07f * s))
                    silk(h * 0.62f, h * 0.18f, h * 0.040f, 0.9f, -b + 1f, 6f, accent.copy(alpha = 0.09f * s))
                    petals(w * 0.96f, h * 0.08f, w * 0.62f, t1.value * 360f / 7f, 7, accent.copy(alpha = 0.015f * s), highlight.copy(alpha = 0.07f * s))
                    petals(w * 0.02f, h * 0.97f, w * 0.50f, -t2.value * 360f / 9f, 9, accent.copy(alpha = 0.012f * s), secondary.copy(alpha = 0.06f * s))
                    for (i in 0 until 12) {
                        val ph = (t1.value * (1f + (i % 3) * 0.2f) + hash01(i, 41f)) % 1f
                        val x = w * hash01(i, 42f) + sin(ph * tau * 2f + i * 1.5f) * w * 0.04f
                        val y = h * (1.02f - 1.04f * ph)
                        mote(x, y, (1.2f + hash01(i, 43f) * 2.0f) * px, highlight.copy(alpha = 0.28f * s * sin(ph * PI.toFloat())))
                    }
                }

                BackdropScene.NEBULA -> {
                    nebulaDrift(t1.value, t2.value, c, speck, s, px)
                    fluidBlob(w * (0.20f + 0.12f * sin(a)), h * (0.16f + 0.06f * cos(b)), w * 0.95f * br, 1.3f, 0.85f + 0.1f * sin(b), -25f + sin(a) * 25f, accent.copy(alpha = 0.26f * s))
                    fluidBlob(w * (0.88f + 0.06f * cos(b)), h * (0.80f + 0.05f * sin(a)), w * 0.90f, 1.2f, 0.9f, 20f + cos(a) * 20f, secondary.copy(alpha = 0.16f * s))
                    fluidBlob(w * (0.55f + 0.10f * sin(b)), h * (0.48f + 0.06f * cos(a)), w * 0.70f, 1.5f, 0.7f, -30f, highlight.copy(alpha = 0.08f * s))
                    silk(h * 0.36f, h * 0.20f, h * 0.030f, 1.1f, a, -24f, highlight.copy(alpha = 0.08f * s))
                    silk(h * 0.50f, h * 0.14f, h * 0.025f, 1.0f, -b, -24f, secondary.copy(alpha = 0.07f * s))
                    for (i in 0 until 28) {
                        val tw = 0.5f + 0.5f * sin(c + i * 1.7f)
                        drawCircle(
                            color = speck.copy(alpha = (0.08f + 0.28f * tw) * s),
                            radius = (0.6f + hash01(i, 3f) * 1.2f) * px,
                            center = Offset(hash01(i, 1f) * w, hash01(i, 2f) * h)
                        )
                    }
                    shootingStar(t3.value, 0, speck, px)
                    shootingStar((t3.value + 0.4f) % 1f, 1, speck, px)
                    shootingStar((t3.value + 0.7f) % 1f, 2, speck, px)
                    var lx = 0f
                    var ly = 0f
                    for (i in 0 until 6) {
                        val x = w * (0.50f + 0.42f * hash01(i, 61f))
                        val y = h * (0.05f + 0.28f * hash01(i, 62f))
                        if (i > 0) drawLine(speck.copy(alpha = (0.05f + 0.09f * (0.5f + 0.5f * sin(c + i))) * s), Offset(lx, ly), Offset(x, y), strokeWidth = 1.0f * px)
                        drawCircle(speck.copy(alpha = 0.40f * s), 1.8f * px, Offset(x, y))
                        lx = x; ly = y
                    }
                }

                BackdropScene.VELVET -> {
                    velvetSpot(b, highlight, s)
                    fluidBlob(w * (0.05f + 0.08f * sin(a)), h * (0.95f + 0.04f * cos(b)), w * 1.10f * br, 1.3f, 0.9f, -15f + sin(b) * 20f, accent.copy(alpha = 0.30f * s))
                    fluidBlob(w * (0.95f + 0.05f * cos(a)), h * (0.12f + 0.06f * sin(b)), w * 0.90f, 1.1f, 1.2f, 25f + cos(a) * 20f, secondary.copy(alpha = 0.18f * s))
                    fluidBlob(w * (0.55f + 0.12f * sin(b)), h * (0.55f + 0.06f * cos(a)), w * 0.75f, 1.4f, 0.8f, 10f, highlight.copy(alpha = 0.06f * s))
                    silk(h * 0.28f, h * 0.20f, h * 0.045f, 0.9f, a, 12f, accent.copy(alpha = 0.11f * s))
                    silk(h * 0.62f, h * 0.22f, h * 0.050f, 0.8f, -b + 2f, -10f, secondary.copy(alpha = 0.09f * s))
                    val folds = 8
                    val fw = w / folds * 0.95f
                    for (k in 0 until folds) {
                        val fx = w * (k + 0.5f) / folds + sin(a + k * 0.8f) * w * 0.018f
                        drawRect(
                            Brush.horizontalGradient(
                                0f to Color.Transparent,
                                0.5f to accent.copy(alpha = (0.045f + 0.035f * (0.5f + 0.5f * sin(b + k * 1.3f))) * s),
                                1f to Color.Transparent,
                                startX = fx - fw / 2f, endX = fx + fw / 2f
                            ),
                            topLeft = Offset(fx - fw / 2f, 0f), size = Size(fw, h)
                        )
                    }
                    val sweepX = (t2.value * 1.6f - 0.3f) * w
                    val half = w * 0.18f
                    rotate(18f, Offset(sweepX, h / 2f)) {
                        drawRect(
                            Brush.horizontalGradient(
                                0f to Color.Transparent, 0.5f to highlight.copy(alpha = 0.08f * s), 1f to Color.Transparent,
                                startX = sweepX - half, endX = sweepX + half
                            ),
                            topLeft = Offset(sweepX - half, -h * 0.2f), size = Size(half * 2f, h * 1.4f)
                        )
                    }
                    for (i in 0 until 10) {
                        val ph = (t2.value * (1f + (i % 2) * 0.3f) + hash01(i, 31f)) % 1f
                        val x = w * hash01(i, 32f) + sin(ph * tau * 2f + i * 1.2f) * w * 0.06f
                        val y = h * (-0.05f + 1.1f * ph)
                        val r = (5f + hash01(i, 33f) * 5f) * px
                        rotate(ph * 540f + i * 40f, Offset(x, y)) {
                            drawOval(highlight.copy(alpha = (sin(ph * PI.toFloat()) * 0.26f * s).coerceIn(0f, 1f)), Offset(x - r * 0.6f, y - r), Size(r * 1.2f, r * 2f))
                        }
                    }
                }

                BackdropScene.HORIZON -> {
                    emberRays(t1.value, highlight, s, px)
                    val pulse = 0.5f + 0.5f * sin(c)
                    fluidBlob(w * (0.50f + 0.10f * sin(a)), h * (1.00f + 0.02f * cos(b)), w * (1.10f + 0.05f * pulse) * br, 1.5f, 0.75f, sin(b) * 8f, accent.copy(alpha = 0.30f * s))
                    fluidBlob(w * (0.50f + 0.06f * cos(a)), h * 0.98f, w * (0.45f + 0.04f * pulse), 1.3f, 0.8f, 0f, highlight.copy(alpha = 0.15f * s))
                    fluidBlob(w * (0.12f + 0.08f * sin(b)), h * (0.12f + 0.05f * cos(a)), w * 0.80f, 1.2f, 1.0f, 20f, secondary.copy(alpha = 0.12f * s))
                    silk(h * 0.58f, h * 0.18f, h * 0.030f, 1.3f, a, -6f, accent.copy(alpha = 0.09f * s))
                    silk(h * 0.76f, h * 0.16f, h * 0.025f, 1.0f, -b + 1f, 5f, highlight.copy(alpha = 0.07f * s))
                    val sunY = h * (1.0f - 0.02f * sin(a))
                    val sunR = w * (0.22f + 0.01f * pulse)
                    drawCircle(
                        Brush.radialGradient(
                            0f to highlight.copy(alpha = 0.20f * s), 0.55f to accent.copy(alpha = 0.14f * s), 1f to Color.Transparent,
                            center = Offset(w * 0.5f, sunY), radius = sunR * 1.8f
                        ),
                        radius = sunR * 1.8f, center = Offset(w * 0.5f, sunY)
                    )
                    for (i in 0 until 4) {
                        val ph = (t1.value + i / 4f) % 1f
                        drawCircle(highlight.copy(alpha = (1f - ph) * 0.09f * s), radius = sunR * (1f + ph * 1.6f), center = Offset(w * 0.5f, sunY), style = Stroke(1.2f * px))
                    }
                    for (i in 0 until 16) {
                        val phase = (t1.value * (1f + (i % 3) * 0.25f) + hash01(i, 4f)) % 1f
                        val x = hash01(i, 5f) * w + sin(phase * tau + i * 1.4f) * w * 0.04f
                        val flick = 0.65f + 0.35f * sin(c * 2f + i * 1.3f)
                        mote(x, h * (1.02f - phase), (0.9f + hash01(i, 6f) * 1.6f) * px,
                            highlight.copy(alpha = (sin(phase * PI.toFloat()) * 0.38f * flick * s).coerceIn(0f, 1f)))
                    }
                }

                BackdropScene.AURORA -> {
                    auroraSea(c, a, highlight, s, px)
                    fluidBlob(w * (0.50f + 0.14f * sin(a)), h * (-0.04f + 0.04f * cos(b)), w * 1.00f * br, 1.5f, 0.8f, sin(b) * 15f, accent.copy(alpha = 0.24f * s))
                    fluidBlob(w * (0.85f + 0.06f * cos(b)), h * (0.85f + 0.05f * sin(a)), w * 0.85f, 1.2f, 1.0f, -20f + cos(a) * 15f, secondary.copy(alpha = 0.16f * s))
                    fluidBlob(w * (0.15f + 0.08f * sin(b)), h * (0.60f + 0.05f * cos(a)), w * 0.70f, 1.1f, 1.2f, 15f, highlight.copy(alpha = 0.08f * s))
                    silk(h * 0.14f, h * 0.22f, h * 0.045f, 1.3f, a, -4f, accent.copy(alpha = 0.12f * s))
                    silk(h * 0.30f, h * 0.20f, h * 0.040f, 1.0f, -b + 1.5f, 5f, secondary.copy(alpha = 0.10f * s))
                    silk(h * 0.22f, h * 0.12f, h * 0.030f, 2f, -a, 3f, highlight.copy(alpha = 0.07f * s))
                    val cols = 16
                    val cw = w / cols * 1.15f
                    for (k in 0 until cols) {
                        val x = w * (k + 0.5f) / cols
                        val flick = 0.6f + 0.4f * sin(c + k * 0.9f)
                        val top = h * 0.08f + sin(a + k * 0.7f) * h * 0.025f
                        val len = h * (0.24f + 0.08f * sin(b + k * 1.2f))
                        drawRect(
                            Brush.verticalGradient(0f to Color.Transparent, 0.5f to secondary.copy(alpha = 0.09f * s * flick), 0.9f to highlight.copy(alpha = 0.04f * s * flick), 1f to Color.Transparent, startY = top, endY = top + len),
                            topLeft = Offset(x - cw / 2f, top), size = Size(cw, len)
                        )
                    }
                    for (i in 0 until 18) {
                        val tw = 0.5f + 0.5f * sin(c + i * 2.1f)
                        drawCircle(speck.copy(alpha = (0.08f + 0.28f * tw) * s), (0.6f + hash01(i, 7f) * 1.0f) * px, Offset(hash01(i, 8f) * w, hash01(i, 9f) * h * 0.48f))
                    }
                }
            }
        }
    )
}

@Composable
fun CoverPlayBrandTitleText(
    text: String,
    modifier: Modifier = Modifier,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.headlineLarge
) {
    val accent = LocalAppUiSettings.current.accent
    val dynamicGradient = remember(accent) {
        Brush.linearGradient(colors = listOf(accent.copy(alpha = 0.9f), accent, accent.copy(alpha = 0.8f)))
    }
    Column(modifier = modifier) {
        Text(
            text = text,
            style = style.copy(
                brush = dynamicGradient,
                fontWeight = FontWeight.SemiBold
            )
        )
        CoverPlayQuipLine(Quips.dialog(text), modifier = Modifier.padding(top = 2.dp))
    }
}

@Composable
fun CoverPlayCardSurface(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    CoverPlaySolidCard(
        modifier = modifier,
        shape = RoundedCornerShape(if (cornerRadius == 16.dp) CoverPlayTokens.Radius.card else cornerRadius),
        selected = selected,
        onClick = onClick,
        onLongClick = onLongClick,
    ) {
        Box(propagateMinConstraints = true) { content() }
    }
}

@Composable
fun CoverPlayButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: @Composable (() -> Unit)? = null
) {
    val settings = LocalAppUiSettings.current
    val accent = settings.accent
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.98f else 1f,
        animationSpec = tween(100), label = "btnScale"
    )
    val haptic = rememberCoverPlayHaptic()
    val shape = rememberCoverPlayMorphingShape(active = pressed, restPercent = 50f, activePercent = 40f)
    val fill = if (enabled) accent else settings.palette.surfaceRaisedAlt
    val content = if (enabled) accent.onFill() else settings.palette.textTertiary

    CompositionLocalProvider(LocalContentColor provides content) {
        Box(
            modifier = modifier
                .defaultMinSize(minWidth = 64.dp)
                .height(CoverPlayDimens.buttonHeight)
                .graphicsLayer { scaleX = scale; scaleY = scale }
                .clip(shape)
                .background(fill)
                .drawWithContent {
                    drawContent()
                    if (enabled && pressed) drawRect(Color.Black.copy(alpha = 0.10f))
                }
                .clickable(
                    interactionSource = interaction, indication = null,
                    enabled = enabled, role = Role.Button
                ) { haptic(); onClick() }
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                if (icon != null) {
                    icon()
                    Spacer(modifier = Modifier.width(10.dp))
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 0.sp),
                    color = content
                )
            }
        }
    }
}

@Composable
fun CoverPlayGlassIconButton(
    icon: Any,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    backgroundColor: Color? = null,
    useShadowOnly: Boolean = false,
    tint: Color? = null
) {
    val settings = LocalAppUiSettings.current
    if (!useShadowOnly && backgroundColor == null) {
        val toneTint = tint ?: settings.accent
        Box(
            modifier = modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(settings.accent.copy(alpha = if (settings.palette.isDark) 0.16f else 0.12f))
                .aliveClickable(enabled = enabled, pressedScale = 0.9f, haptic = true, onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            when (icon) {
                is ImageVector -> Icon(icon, null, tint = toneTint, modifier = Modifier.size(22.dp))
                is Painter -> Icon(icon, null, tint = toneTint, modifier = Modifier.size(22.dp))
            }
        }
        return
    }
    val finalTint = if (useShadowOnly) (tint ?: Color.White) else (tint ?: settings.palette.textPrimary)
    CoverPlayGlassSurface(
        modifier = modifier.size(48.dp),
        shape = CircleShape,
        overVideo = useShadowOnly,
        tint = backgroundColor,
        elevation = 0.dp,
        enabled = enabled,
        pressedScale = 0.9f,
        onClick = onClick
    ) {
        when (icon) {
            is ImageVector -> Icon(icon, null, tint = finalTint, modifier = Modifier.size(24.dp).align(Alignment.Center))
            is Painter -> Icon(icon, null, tint = finalTint, modifier = Modifier.size(24.dp).align(Alignment.Center))
        }
    }
}

@Composable
fun CoverPlayBrandIconContainer(
    modifier: Modifier = Modifier,
    size: Dp = CoverPlayDimens.iconContainer,
    @Suppress("UNUSED_PARAMETER") cornerRadius: Dp = CoverPlayTokens.Radius.icon,
    active: Boolean = false,
    @Suppress("UNUSED_PARAMETER") breathe: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    val settings = LocalAppUiSettings.current
    val p = settings.palette
    val radius = if (size >= CoverPlayDimens.iconContainerLarge) 14.dp else CoverPlayTokens.Radius.icon
    val fill = when {
        active -> lerp(p.container, settings.accent, 0.20f)
        p.isDark -> p.containerHigh
        else -> lerp(p.containerHighest, settings.accent, 0.10f)
    }
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(radius))
            .background(fill),
        contentAlignment = Alignment.Center,
        content = content
    )
}

@Composable
fun CoverPlayDurationBadge(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = MaterialTheme.typography.captionBadge,
        color = Color.White,
        modifier = modifier
            .glassBadge(LocalAppUiSettings.current.palette.videoGlass.copy(alpha = 0.66f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}

fun qualityLabel(width: Int, height: Int): String {
    val longerEdge = maxOf(width, height)
    return when {
        longerEdge >= 3840 -> "4K"
        longerEdge >= 1920 -> "FHD"
        longerEdge >= 1280 -> "HD"
        else                -> "SD"
    }
}

fun resolutionColor(label: String): Color = when (label) {
    "4K"  -> StatusColors.quality
    else  -> Color(0xFFE6E8F0)
}

@Composable
fun CoverPlayQualityBadge(width: Int, height: Int, modifier: Modifier = Modifier) {
    CoverPlayResolutionBadge(if (width > 0) width else 1920, if (height > 0) height else 1080, modifier, onMedia = false)
}

@Composable
fun CoverPlayAccentDurationBadge(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.captionBadge,
        color = BrandAccent,
        modifier = modifier
            .glassBadge(BrandAccent.copy(alpha = 0.16f), BrandAccent)
            .padding(horizontal = 9.dp, vertical = 3.dp)
    )
}

object BrandAssets {
    @androidx.annotation.DrawableRes
    @Suppress("UNUSED_PARAMETER")
    fun logoFor(theme: AppTheme): Int = com.raman.CoverPlay.R.drawable.ic_launcher_foreground
}

@Composable
fun CoverPlayBrandMark(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    lobes: Int = 8,
    breathe: Boolean = true,
    logoScale: Float = 0.45f
) {
    val ui = LocalAppUiSettings.current
    val accent = ui.accent
    val highlight = ui.highlight
    val tile = accent
    val fill = Brush.linearGradient(listOf(tile, tile))
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Box(Modifier.fillMaxSize().clip(RoundedCornerShape(size * 0.30f)).background(fill))
        Icon(
            painter = painterResource(R.drawable.ic_play_custom),
            contentDescription = "CoverPlay",
            tint = accent.onFill(),
            modifier = Modifier.size(size * logoScale)
        )
    }
}

@Composable
fun CoverPlayBrandPlayOverlay(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    iconOverride: Painter? = null,
    label: String? = null
) {
    val accent = LocalAppUiSettings.current.accent
    val infiniteTransition = rememberInfiniteTransition(label = "playFluid")

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(10000, easing = LinearEasing)),
        label = "rotation"
    )

    val breathing by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "breathing"
    )

    val dynamicRadialGradient = remember(accent) {
        Brush.radialGradient(
            colors = listOf(accent.copy(alpha = 0.85f), accent, accent.copy(alpha = 0.7f))
        )
    }

    Column(
        modifier = modifier.graphicsLayer {
            scaleX = breathing
            scaleY = breathing
        },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(size),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { rotationZ = rotation }
                    .clip(OrganicShape(lobes = 8))
                    .background(dynamicRadialGradient)
                    .liquidGlassDecor(OrganicShape(lobes = 8), LensLook, accent, paintFill = false)
            )

            if (iconOverride != null) {
                Icon(
                    painter = iconOverride,
                    contentDescription = null,
                    tint = accent.onFill(),
                    modifier = Modifier.size(size * 0.48f)
                )
            } else {
                Icon(
                    painter = painterResource(R.drawable.ic_play_custom),
                    contentDescription = "CoverPlay",
                    tint = accent.onFill(),
                    modifier = Modifier.size(size * 0.48f)
                )
            }
        }

        if (label != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.5.sp
                ),
                color = LocalAppUiSettings.current.palette.textPrimary
            )
        }
    }
}

@Composable
fun CoverPlayDialogCloseButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalAppUiSettings.current.palette
    val haptic = rememberCoverPlayHaptic()
    Box(
        modifier = modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(palette.containerHighest)
            .clickable { haptic(); onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_close_custom),
            contentDescription = "Close",
            tint = palette.textSecondary,
            modifier = Modifier.size(14.dp)
        )
    }
}

@Composable
fun CoverPlayDialogHeader(
    title: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null
) {
    CoverPlayDialogTitleBar(title = title, modifier = modifier, trailing = trailing, onClose = onDismiss)
}

@Composable
fun CoverPlayDialog(
    onDismiss: () -> Unit,
    title: String,
    modifier: Modifier = Modifier.widthIn(max = 340.dp).fillMaxWidth(0.86f),
    content: @Composable ColumnScope.() -> Unit
) {
    if (LocalOverVideo.current) {
        CoverPlayPlayerPanel(visible = true, onDismiss = onDismiss, title = title, scrollable = false) { content() }
        return
    }
    CoverPlayDialogShell(onDismiss = onDismiss, modifier = modifier) {
        CoverPlayDialogHeader(title = title, onDismiss = onDismiss)
        Column(
            modifier = Modifier
                .padding(top = 16.dp)
                .weight(1f, fill = false)
        ) {
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoverPlayBottomSheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    skipPartiallyExpanded: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    val overVideo = LocalOverVideo.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = skipPartiallyExpanded)
    val shape = RoundedCornerShape(topStart = CoverPlayTokens.Radius.sheet, topEnd = CoverPlayTokens.Radius.sheet)
    val snapshot = rememberCoverPlayWindowSnapshot(enabled = !overVideo)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.Transparent,
        tonalElevation = 0.dp,
        shape = shape,
        scrimColor = Color.Black.copy(alpha = if (WindowBlurSupported) 0.26f else 0.5f),
        dragHandle = null
    ) {
        CoverPlayDialogBackdrop(blurRadius = 36, dim = 0f)
        CompositionLocalProvider(LocalGlassHaze provides null, LocalGlassBackdrop provides null) {
            val body: @Composable () -> Unit = {
                val st = LocalAppUiSettings.current
                val dark = st.palette.isDark
                CoverPlayGlassSurface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = shape,
                    strong = true,
                    overVideo = overVideo,
                    tint = Color.Transparent
                ) {
                    val blurIn by animateFloatAsState(
                        targetValue = if (snapshot != null) 1f else 0f,
                        animationSpec = tween(360, easing = MotionSpec.EmphasizedEasing),
                        label = "sheetBlurIn"
                    )
                    Box(Modifier.matchParentSize().graphicsLayer { alpha = blurIn }.snapshotGlass(snapshot))
                    Box(
                        Modifier.matchParentSize().background(
                            (if (overVideo && st.palette.isDark) st.palette.surfaceNavy else st.palette.containerHigh)
                                .copy(alpha = 0.94f - 0.18f * blurIn)
                        )
                    )
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .padding(top = 10.dp, bottom = 8.dp)
                                .align(Alignment.CenterHorizontally)
                                .size(width = 32.dp, height = 4.dp)
                                .clip(CircleShape)
                                .background(st.palette.textTertiary.copy(alpha = 0.45f))
                        )
                        Column(
                            modifier = modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp)
                                .padding(bottom = 24.dp)
                        ) {
                            content()
                        }
                    }
                }
            }
            if (overVideo) CoverPlayOverVideoScope(body) else body()
        }
    }
}

@Composable
fun CoverPlayAlertDialog(
    onDismiss: () -> Unit,
    title: String,
    text: String,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable (() -> Unit)? = null
) {
    val settings = LocalAppUiSettings.current
    CoverPlayDialogShell(onDismiss = onDismiss, modifier = Modifier.widthIn(max = 340.dp).fillMaxWidth(0.86f)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = settings.palette.textPrimary
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, lineHeight = 19.sp),
            color = settings.palette.textSecondary,
            modifier = Modifier.weight(1f, fill = false)
        )
        Spacer(Modifier.height(18.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (dismissButton != null) {
                dismissButton()
                Spacer(Modifier.width(8.dp))
            }
            confirmButton()
        }
    }
}

@Composable
fun CoverPlayTopBarAction(icon: Painter, onClick: () -> Unit) {
    CoverPlayGlassIconButton(icon = icon, onClick = onClick)
}

@Composable
fun CoverPlayBloomPlayButton(
    isPlaying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 84.dp,
    iconOverride: Painter? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = MotionSpec.SpatialSpring,
        label = "bloomScale"
    )
    val darkUi = LocalAppUiSettings.current.palette.isDark
    val disc = if (darkUi) Color.White else Color(0xFF111114)
    val glyph = if (darkUi) Color(0xFF0B0B0D) else Color.White
    val bloomHaptic = rememberCoverPlayHaptic(HapticFeedbackType.LongPress)

    Box(
        modifier = modifier
            .size(size + 24.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { bloomHaptic(); onClick() }
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(modifier = Modifier.size(size).clip(CircleShape).background(disc))

        if (iconOverride != null) {
            Icon(
                painter = iconOverride,
                contentDescription = "Action",
                tint = glyph,
                modifier = Modifier.size(size * 0.46f)
            )
        } else {
            AnimatedContent(
                targetState = isPlaying,
                transitionSpec = {
                    (scaleIn(spring(0.7f, 400f)) + fadeIn()).togetherWith(scaleOut(spring(0.7f, 400f)) + fadeOut())
                },
                label = "playPauseMorph"
            ) { playing ->
                Icon(
                    painter = if (playing) painterResource(R.drawable.ic_pause_custom)
                    else painterResource(R.drawable.ic_play_custom),
                    contentDescription = if (playing) "Pause" else "Play",
                    tint = glyph,
                    modifier = Modifier.size(size * 0.46f)
                )
            }
        }
    }
}

@Composable
fun CoverPlayThemeModeSelector(
    selected: ThemeChoice,
    onSelect: (ThemeChoice) -> Unit,
    modifier: Modifier = Modifier
) {
    CoverPlaySegmented(
        options = ThemeChoice.entries.toList(),
        selected = selected,
        label = { it.label },
        onSelect = onSelect,
        modifier = modifier
    )
}

@Composable
fun CoverPlayBloomColorSwatch(
    color: Color,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp
) {
    val haptic = rememberCoverPlayHaptic()
    val scale by animateFloatAsState(
        targetValue = if (selected) 1f else 0.88f,
        animationSpec = tween(180), label = "swatchScale"
    )
    val ring by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = tween(180), label = "swatchRing"
    )
    Box(
        modifier = modifier.size(size + 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(size + 12.dp)
                .graphicsLayer { alpha = ring.coerceIn(0f, 1f); scaleX = 0.8f + 0.2f * ring; scaleY = 0.8f + 0.2f * ring }
                .clip(CircleShape)
                .border(2.dp, color, CircleShape)
        )
        Box(
            modifier = Modifier
                .size(size)
                .graphicsLayer(scaleX = scale, scaleY = scale)
                                .clip(CircleShape)
                .background(color)
                .border(1.dp, LocalAppUiSettings.current.palette.outlineMedium.copy(alpha = 0.6f), CircleShape)
                .clickable { haptic(); onClick() },
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.animation.AnimatedVisibility(
                visible = selected,
                enter = fadeIn(tween(150)),
                exit = fadeOut(tween(120))
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_check_custom),
                    contentDescription = "Selected",
                    tint = color.onFill(),
                    modifier = Modifier.size(size * 0.5f)
                )
            }
        }
    }
}

@Composable
fun CoverPlayPetalBloomDecoration(modifier: Modifier = Modifier, alpha: Float = 0.12f) {
    Box(modifier = modifier.size(0.dp))
}

@Composable
fun CoverPlaySolidSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    withAccentGlow: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    val settings = LocalAppUiSettings.current
    CoverPlayGlassSurface(
        modifier = modifier,
        shape = shape,
        strong = true,
        glow = if (withAccentGlow) settings.accent else null,
        elevation = if (withAccentGlow) 16.dp else 0.dp
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            content()
        }
    }
}

@Composable
fun CoverPlayImageCard(
    painter: Painter,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(32.dp),
    contentDescription: String? = null
) {
    Surface(
        modifier = modifier
            .aspectRatio(1f)
            .shadow(24.dp, shape, spotColor = Color.Black.copy(alpha = 0.45f)),
        shape = shape,
    ) {
        androidx.compose.foundation.Image(
            painter = painter,
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize(),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop
        )
    }
}

@Composable
fun CoverPlayFrostedSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    content: @Composable BoxScope.() -> Unit
) {
    CoverPlayGlassSurface(modifier = modifier, shape = shape, content = content)
}

@Composable
fun CoverPlayListItem(
    title: String,
    subtitle: String,
    leading: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null
) {
    val settings = LocalAppUiSettings.current
    val interaction = remember { MutableInteractionSource() }
    val scale by rememberCoverPlayPressScale(interaction, 0.97f)
    val isPressed by interaction.collectIsPressedAsState()
    val bg by animateColorAsState(
        targetValue = if (isPressed) settings.accent.copy(alpha = 0.10f) else Color.Transparent,
        animationSpec = tween(150),
        label = "listItemBg"
    )
    val haptic = rememberCoverPlayHaptic()
    Surface(
        onClick = { haptic(); onClick() },
        interactionSource = interaction,
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = scale; scaleY = scale },
        shape = RoundedCornerShape(16.dp),
        color = bg
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            leading()
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = settings.palette.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = settings.palette.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (trailing != null) {
                trailing()
            }
        }
    }
}

@Composable
fun CoverPlayScaleButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = MotionSpec.SpatialSpring,
        label = "scale"
    )

    Box(
        modifier = modifier
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center,
        content = content
    )
}

@Composable
fun CoverPlayAnimatedPetalBackground(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize())
}

@Composable
fun CoverPlayControlSheet(
    title: String,
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    CoverPlayDialog(onDismiss = onDismiss, title = title) {
        content()
    }
}

@Composable
fun CoverPlayControlSheetRow(
    icon: Painter,
    label: String,
    onClick: () -> Unit,
    trailing: @Composable (() -> Unit)? = null
) {
    val palette = LocalAppUiSettings.current.palette
    CoverPlayGlassSurface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CoverPlayTokens.Radius.card),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = LocalAppUiSettings.current.accent, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = palette.textPrimary,
                modifier = Modifier.weight(1f)
            )
            if (trailing != null) {
                trailing()
            } else {
                Icon(
                    painter = painterResource(R.drawable.ic_chevron_right_custom),
                    null,
                    tint = palette.textTertiary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun CoverPlayControlSheetGridItem(
    icon: Painter,
    onClick: () -> Unit,
    active: Boolean = false
) {
    val palette = LocalAppUiSettings.current.palette
    val accent = LocalAppUiSettings.current.accent
    CoverPlayGlassSurface(
        modifier = Modifier.fillMaxWidth().height(64.dp),
        shape = RoundedCornerShape(18.dp),
        selected = active,
        onClick = onClick
    ) {
        Icon(
            painter = icon,
            null,
            tint = if (active) accent else palette.textPrimary,
            modifier = Modifier.size(26.dp).align(Alignment.Center)
        )
    }
}

@Composable
fun CoverPlayControlSheetGridItem(
    icon: ImageVector,
    onClick: () -> Unit,
    active: Boolean = false
) = CoverPlayControlSheetGridItem(rememberVectorPainter(icon), onClick, active)

fun Modifier.topScrim(color: Color): Modifier = this.background(
    Brush.verticalGradient(
        listOf(
            color.copy(alpha = 0.92f),
            color.copy(alpha = 0.65f),
            Color.Transparent
        )
    )
)

@Composable
fun CoverPlayGlassCircleButton(
    painter: Painter,
    description: String?,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    rotation: Float = 0f,
    selected: Boolean = false,
    tint: Color = LocalAppUiSettings.current.palette.textPrimary,
    onClick: () -> Unit
) {
    val ui = LocalAppUiSettings.current
    val p = ui.palette
    val fill = if (selected) lerp(p.containerHigh, ui.accent, 0.22f) else p.containerHigh
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(fill)
            .border(1.dp, p.outlineFaint, CircleShape)
            .aliveClickable(pressedScale = 0.94f, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter, description, tint = tint,
            modifier = Modifier.size(size * 0.5f).graphicsLayer { rotationZ = rotation }
        )
    }
}

class BackdropState(val content: GraphicsLayer, val blurred: GraphicsLayer) {
    var origin by mutableStateOf(Offset.Zero)
}

@Composable
fun rememberCoverPlayBackdropState(): BackdropState {
    val c = rememberGraphicsLayer()
    val b = rememberGraphicsLayer()
    return remember(c, b) { BackdropState(c, b) }
}

fun Modifier.backdropSource(state: BackdropState): Modifier = this
    .onGloballyPositioned { state.origin = it.positionInRoot() }
    .drawWithContent {
        state.content.record { this@drawWithContent.drawContent() }
        drawLayer(state.content)
        state.blurred.renderEffect = BlurEffect(64f, 64f, TileMode.Clamp)
        state.blurred.record { drawLayer(state.content) }
    }

@Composable
fun Modifier.backdropBlur(state: BackdropState): Modifier {
    var pos by remember { mutableStateOf(Offset.Zero) }
    return this
        .onGloballyPositioned { pos = it.positionInRoot() }
        .drawBehind {
            translate(state.origin.x - pos.x, state.origin.y - pos.y) { drawLayer(state.blurred) }
        }
}

@Composable
fun CoverPlaySectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    count: Int? = null,
    subtitle: String? = null,
    topPadding: Dp = CoverPlaySpacingTokens.sectionGap,
    onViewAll: (() -> Unit)? = null
) {
    val settings = LocalAppUiSettings.current
    val p = settings.palette

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = CoverPlayDimens.screenHorizontal)
            .padding(top = topPadding)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (settings.showSectionPointer) {
                val rotation = LocalShapeRotation.current
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .graphicsLayer { rotationZ = rotation.value }
                        .background(settings.accent, CoverPlayMaterialShapes.cookie6())
                )
                Spacer(Modifier.width(12.dp))
            }
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = p.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            if (onViewAll != null) {
                Text(
                    text = "See all",
                    style = MaterialTheme.typography.labelLarge,
                    color = settings.accent,
                    modifier = Modifier
                        .heightIn(min = 40.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onViewAll)
                        .padding(horizontal = 12.dp)
                        .wrapContentHeight(Alignment.CenterVertically)
                )
            }
        }
        if (subtitle != null) {
            Spacer(Modifier.height(CoverPlayBaseTokens.Space.s2))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = p.textSecondary
            )
        }
        Spacer(Modifier.height(CoverPlayBaseTokens.Space.s12))
    }
}

@Composable
fun CoverPlayCompactCard(
    onDismiss: () -> Unit,
    width: Dp = 284.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    CoverPlayDialogShell(onDismiss = onDismiss, modifier = Modifier.width(width), content = content)
}

@Composable
fun CoverPlayGlassIconAction(
    icon: androidx.compose.ui.graphics.painter.Painter,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = LocalAppUiSettings.current.accent
) {
    CoverPlayGlassSurface(
        modifier = modifier.size(46.dp),
        shape = CircleShape,
        pressedScale = 0.9f,
        onClick = onClick
    ) {
        Box(Modifier.matchParentSize().background(tint.copy(alpha = 0.10f)))
        Icon(icon, description, tint = tint, modifier = Modifier.size(20.dp).align(Alignment.Center))
    }
}

private val TierGold = Color(0xFFFFC64D)
private val TierGoldDeep = Color(0xFFFF9E3B)
private val NewGreen = Color(0xFF3DDC84)

@Composable
fun CoverPlayResolutionBadge(width: Int, height: Int, modifier: Modifier = Modifier, onMedia: Boolean = true) {
    if (width <= 0 || height <= 0) return
    val st = LocalAppUiSettings.current
    val edge = maxOf(width, height)
    val tier = when { edge >= 3840 -> 3; edge >= 1920 -> 2; edge >= 1280 -> 1; else -> 0 }
    val label = arrayOf("SD", "HD", "FHD", "4K")[tier]
    val base = if (onMedia) Color.White else st.palette.textPrimary
    val gold = if (onMedia || st.palette.isDark) TierGold else Color(0xFFD98200)
    val textColor = when (tier) { 3 -> gold; 2 -> base.copy(alpha = 0.92f); 1 -> base.copy(alpha = 0.72f); else -> base.copy(alpha = 0.52f) }
    val shape = RoundedCornerShape(6.dp)
    val border: Brush? = when (tier) {
        3 -> Brush.linearGradient(listOf(gold, TierGoldDeep.copy(alpha = 0.45f)))
        2 -> Brush.linearGradient(listOf(base.copy(alpha = 0.34f), base.copy(alpha = 0.34f)))
        1 -> Brush.linearGradient(listOf(base.copy(alpha = 0.18f), base.copy(alpha = 0.18f)))
        else -> null
    }
    val fill = if (onMedia) (if (tier == 3) Color(0xFF1A1206).copy(alpha = 0.55f) else Color.Black.copy(alpha = 0.40f)) else Color.Transparent
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = if (tier >= 2) FontWeight.ExtraBold else FontWeight.SemiBold,
            letterSpacing = 0.8.sp
        ),
        color = textColor,
        modifier = modifier
            .clip(shape)
            .background(fill)
            .then(if (border != null) Modifier.border(0.6.dp, border, shape) else Modifier)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}

@Composable
fun CoverPlayNewBadge(modifier: Modifier = Modifier, onMedia: Boolean = true) {
    val shape = RoundedCornerShape(6.dp)
    Row(
        modifier = modifier
            .clip(shape)
            .then(
                if (onMedia) Modifier
                    .background(Color.Black.copy(alpha = 0.40f))
                    .border(0.6.dp, NewGreen.copy(alpha = 0.5f), shape)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                else Modifier
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(Modifier.size(5.dp).background(NewGreen, CircleShape))
        Text(
            text = if (onMedia) "NEW" else "New",
            style = MaterialTheme.typography.captionBadge,
            color = NewGreen
        )
    }
}

@Composable
fun CoverPlayMediaPill(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 0.3.sp),
        color = Color.White.copy(alpha = 0.95f),
        maxLines = 1,
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color.Black.copy(alpha = 0.45f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}

@Composable
fun CoverPlayProgressBar(progress: Float, modifier: Modifier = Modifier, height: Dp = 3.dp) {
    val st = LocalAppUiSettings.current
    Box(modifier.fillMaxWidth().height(height).background(Color.White.copy(alpha = 0.22f))) {
        Box(
            Modifier.fillMaxWidth(progress.coerceIn(0f, 1f)).fillMaxHeight()
                .background(Brush.horizontalGradient(listOf(st.accent.copy(alpha = 0.85f), st.highlight)))
        )
    }
}
