@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.raman.CoverPlay.ui.theme

import android.graphics.Bitmap
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raman.CoverPlay.util.rememberCoverPlayHaptic
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.random.Random
import kotlinx.coroutines.launch
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi

val LocalShapeRotation = staticCompositionLocalOf<State<Float>> { mutableStateOf(0f) }

object CoverPlaySprings {
    fun <T> spatial(): SpringSpec<T> = spring(dampingRatio = 0.62f, stiffness = 420f)

    fun <T> effects(): SpringSpec<T> = spring(dampingRatio = 1f, stiffness = 1400f)
}

object CoverPlayMaterialShapes {
    @Composable fun cookie9(): Shape = MaterialShapes.Cookie9Sided.toShape()
    @Composable fun cookie6(): Shape = MaterialShapes.Cookie6Sided.toShape()
    @Composable fun clover4(): Shape = MaterialShapes.Clover4Leaf.toShape()
    @Composable fun sunny(): Shape = MaterialShapes.Sunny.toShape()
    @Composable fun softBurst(): Shape = MaterialShapes.SoftBurst.toShape()
    @Composable fun flower(): Shape = MaterialShapes.Flower.toShape()
    @Composable fun pill(): Shape = MaterialShapes.Pill.toShape()
    @Composable fun cookie12(): Shape = MaterialShapes.Cookie12Sided.toShape()
    @Composable fun clover8(): Shape = MaterialShapes.Clover8Leaf.toShape()
    @Composable fun burst(): Shape = MaterialShapes.Burst.toShape()
    @Composable fun puffy(): Shape = MaterialShapes.Puffy.toShape()
    @Composable fun diamond(): Shape = MaterialShapes.Diamond.toShape()
    @Composable fun gem(): Shape = MaterialShapes.Gem.toShape()
    @Composable fun pentagon(): Shape = MaterialShapes.Pentagon.toShape()
    @Composable fun arch(): Shape = MaterialShapes.Arch.toShape()
    @Composable fun oval(): Shape = MaterialShapes.Oval.toShape()
    @Composable fun ghostish(): Shape = MaterialShapes.Ghostish.toShape()
}

@Composable
fun CoverPlayLoader(
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    color: Color = LocalAppUiSettings.current.accent
) {
    LoadingIndicator(modifier = modifier.size(size), color = color)
}

@Composable
fun CoverPlayLoaderBadge(
    modifier: Modifier = Modifier,
    size: Dp = 64.dp
) {
    val accent = LocalAppUiSettings.current.accent
    ContainedLoadingIndicator(
        modifier = modifier.size(size),
        containerColor = accent.copy(alpha = 0.16f),
        indicatorColor = accent
    )
}

@Composable
fun rememberCoverPlayMorphingShape(
    active: Boolean,
    restPercent: Float = 50f,
    activePercent: Float = 30f
): Shape {
    val p by animateFloatAsState(
        targetValue = if (active) activePercent else restPercent,
        animationSpec = CoverPlaySprings.spatial(),
        label = "morphShape"
    )
    return RoundedCornerShape(CornerSize(p.coerceIn(0f, 50f)))
}

@Composable
fun CoverPlayShapeBadge(
    shape: Shape,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    container: Color = LocalAppUiSettings.current.accent.copy(alpha = 0.18f),
    rotating: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    val clock = LocalShapeRotation.current
    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .matchParentSize()
                .graphicsLayer { if (rotating) rotationZ = clock.value }
                .clip(shape)
                .background(container)
        )
        content()
    }
}

class CoverPlayAction(
    val onClick: () -> Unit,
    val label: String? = null,
    val icon: (@Composable (tint: Color) -> Unit)? = null,
    val primary: Boolean = false,
    val flex: Boolean = false
)

@Composable
fun CoverPlayButtonGroup(
    actions: List<CoverPlayAction>,
    modifier: Modifier = Modifier,
    height: Dp = 56.dp,
    spacing: Dp = 8.dp
) {
    val s = LocalAppUiSettings.current
    val haptic = rememberCoverPlayHaptic()
    var pressedIdx by remember { mutableIntStateOf(-1) }

    Row(
        modifier = modifier.fillMaxWidth().height(height),
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        actions.forEachIndexed { i, a ->
            val source = remember { MutableInteractionSource() }
            val pressed by source.collectIsPressedAsState()
            LaunchedEffect(pressed) {
                if (pressed) pressedIdx = i else if (pressedIdx == i) pressedIdx = -1
            }

            val baseWidth = if (a.label == null) height else height + 28.dp
            val widthDelta = when {
                pressedIdx == i -> 14.dp
                pressedIdx >= 0 -> (-4).dp
                else -> 0.dp
            }
            val animatedWidth by animateDpAsState(
                targetValue = baseWidth + widthDelta,
                animationSpec = CoverPlaySprings.spatial(),
                label = "groupItemWidth"
            )
            val shape = rememberCoverPlayMorphingShape(active = pressed, restPercent = 50f, activePercent = 28f)
            val fill by animateColorAsState(
                targetValue = if (a.primary) s.accent else s.palette.containerHigh,
                animationSpec = CoverPlaySprings.effects(),
                label = "groupItemFill"
            )
            val content = if (a.primary) s.accent.onFill() else s.palette.textPrimary

            Box(
                modifier = (if (a.flex) Modifier.weight(1f) else Modifier.width(animatedWidth))
                    .height(height)
                    .clip(shape)
                    .background(fill)
                    .clickable(
                        interactionSource = source,
                        indication = null,
                        role = Role.Button
                    ) { haptic(); a.onClick() }
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    a.icon?.invoke(content)
                    if (a.icon != null && a.label != null) Spacer(Modifier.width(10.dp))
                    if (a.label != null) {
                        Text(
                            text = a.label,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 0.sp),
                            color = content,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun <T> CoverPlaySegmented(
    options: List<T>,
    selected: T,
    label: (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 48.dp
) {
    val s = LocalAppUiSettings.current
    val p = s.palette
    val haptic = rememberCoverPlayHaptic()
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

    val count = options.size.coerceAtLeast(1)
    val selectedIndex = options.indexOf(selected).coerceAtLeast(0)
    val inset = 4.dp
    val insetPx = with(density) { inset.toPx() }

    var widthPx by remember { mutableIntStateOf(0) }
    val segmentPx = if (widthPx > 0) (widthPx - 2f * insetPx) / count else 0f

    val pos = remember { Animatable(selectedIndex.toFloat()) }
    var dragging by remember { mutableStateOf(false) }
    LaunchedEffect(selectedIndex, count) {
        if (!dragging) pos.animateTo(selectedIndex.toFloat(), CoverPlaySprings.spatial())
    }
    val grow by animateFloatAsState(
        if (dragging) 1.02f else 1f, CoverPlaySprings.spatial(), label = "segGrow"
    )

    val currentOnSelect by rememberUpdatedState(onSelect)
    val currentOptions by rememberUpdatedState(options)
    val currentSelectedIndex by rememberUpdatedState(selectedIndex)

    val track = lerp(p.containerHigh, if (p.isDark) Color.Black else p.textPrimary, if (p.isDark) 0.38f else 0.07f)
    val pillColor = s.accent
    val onPill = pillColor.onFill()
    val idle = p.textSecondary

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(CircleShape)
            .background(track)
            .border(1.dp, p.outlineFaint.copy(alpha = if (p.isDark) 0.55f else 0.9f), CircleShape)
            .onSizeChanged { widthPx = it.width }
            .pointerInput(count, segmentPx) {
                detectTapGestures { offset ->
                    if (segmentPx <= 0f) return@detectTapGestures
                    val idx = ((offset.x - insetPx) / segmentPx).toInt().coerceIn(0, count - 1)
                    if (idx != currentSelectedIndex) {
                        haptic()
                        currentOnSelect(currentOptions[idx])
                    }
                }
            }
            .pointerInput(count, segmentPx) {
                var lastIdx = -1
                detectHorizontalDragGestures(
                    onDragStart = { _ -> dragging = true; lastIdx = pos.value.roundToInt() },
                    onDragCancel = {
                        dragging = false
                        scope.launch { pos.animateTo(currentSelectedIndex.toFloat(), CoverPlaySprings.spatial()) }
                    },
                    onDragEnd = {
                        dragging = false
                        val target = pos.value.roundToInt().coerceIn(0, count - 1)
                        scope.launch { pos.animateTo(target.toFloat(), CoverPlaySprings.spatial()) }
                        if (target != currentSelectedIndex) currentOnSelect(currentOptions[target])
                    },
                    onHorizontalDrag = { change, dx ->
                        change.consume()
                        if (segmentPx > 0f) {
                            val next = (pos.value + dx / segmentPx).coerceIn(0f, (count - 1).toFloat())
                            scope.launch { pos.snapTo(next) }
                            val idx = next.roundToInt()
                            if (idx != lastIdx) { lastIdx = idx; haptic() }
                        }
                    }
                )
            }
    ) {
        if (segmentPx > 0f) {
            Box(
                Modifier
                    .offset { IntOffset((insetPx + pos.value * segmentPx).roundToInt(), insetPx.roundToInt()) }
                    .width(with(density) { segmentPx.toDp() })
                    .height(height - inset * 2)
                    .graphicsLayer { scaleX = grow; scaleY = grow }
                    .clip(CircleShape)
                    .background(pillColor)
            )
        }
        Row(Modifier.fillMaxSize().padding(horizontal = inset)) {
            options.forEachIndexed { i, option ->
                val cover = (1f - abs(pos.value - i)).coerceIn(0f, 1f)
                Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                    Text(
                        text = label(option),
                        style = when {
                            count <= 3 -> MaterialTheme.typography.labelLarge
                            count == 4 -> MaterialTheme.typography.labelMedium
                            else -> MaterialTheme.typography.labelSmall
                        }.copy(
                            fontWeight = if (cover > 0.5f) FontWeight.SemiBold else FontWeight.Medium
                        ),
                        color = lerp(idle, onPill, cover),
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CoverPlayFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = LocalAppUiSettings.current.accent,
    contentColor: Color = LocalAppUiSettings.current.accent.onFill(),
    size: Dp = 56.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val haptic = rememberCoverPlayHaptic()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val shape = rememberCoverPlayMorphingShape(active = isPressed, restPercent = 50f, activePercent = 40f)
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = androidx.compose.animation.core.tween(100),
        label = "fabScale"
    )

    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
            .background(containerColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button
            ) {
                haptic()
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.runtime.CompositionLocalProvider(
            androidx.compose.material3.LocalContentColor provides contentColor
        ) { content() }
    }
}

@Composable
fun CoverPlayCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = LocalAppUiSettings.current.palette.containerHigh,
    content: @Composable BoxScope.() -> Unit
) {
    val haptic = rememberCoverPlayHaptic()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val shape = rememberCoverPlayMorphingShape(active = isPressed, restPercent = 32f, activePercent = 20f)
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = CoverPlaySprings.spatial(),
        label = "cardScale"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
            .background(containerColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button
            ) {
                haptic()
                onClick()
            },
        content = content
    )
}

@Composable
fun CoverPlayRotatingShape(
    shape: Shape,
    modifier: Modifier = Modifier,
    color: Color = LocalAppUiSettings.current.accent.copy(alpha = 0.24f),
    periodMillis: Int = 14000,
    extraRotation: Float = 0f,
    reverse: Boolean = false
) {
    val clock = LocalShapeRotation.current
    val turns = (24_000f / periodMillis.coerceAtLeast(1000)).let { if (it < 1f) 1 else it.toInt() }
    val dir = if (reverse) -1f else 1f
    Box(
        modifier
            .graphicsLayer { rotationZ = clock.value * turns * dir + extraRotation }
            .clip(shape)
            .background(color)
    )
}

@Composable
fun rememberCoverPlayGrainBrush(): Brush = remember {
    val side = 128
    val rnd = Random(7)
    val px = IntArray(side * side) {
        val a = rnd.nextInt(0, 8)
        if (rnd.nextBoolean()) (a shl 24) or 0xFFFFFF else (a shl 24)
    }
    val bmp = Bitmap.createBitmap(px, side, side, Bitmap.Config.ARGB_8888)
    ShaderBrush(ImageShader(bmp.asImageBitmap(), TileMode.Repeated, TileMode.Repeated))
}

@Composable
fun CoverPlayShapeBackdrop(modifier: Modifier = Modifier) {
    val s = LocalAppUiSettings.current
    val dark = s.palette.isDark
    val baseBg = s.palette.bg

    val artColor = AmbientTint.color
    val washTarget = if (artColor.isSpecified) lerp(s.accent, artColor, 0.65f) else s.accent
    val wash by animateColorAsState(
        washTarget,
        animationSpec = androidx.compose.animation.core.tween(600, easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)),
        label = "backdropWash"
    )
    val washAlpha = if (dark) 0.07f else 0.06f

    Canvas(modifier.fillMaxSize()) {
        drawRect(baseBg)
        drawRect(
            Brush.verticalGradient(
                0f to wash.copy(alpha = washAlpha),
                1f to Color.Transparent,
                startY = 0f,
                endY = size.height * 0.34f
            ),
            size = androidx.compose.ui.geometry.Size(size.width, size.height * 0.34f)
        )
    }
}
