package com.raman.CoverPlay.navigation

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.offset
import androidx.compose.ui.unit.sp
import com.raman.CoverPlay.ui.theme.LocalAppUiSettings
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

internal class JellyNavItem(
    val label: String,
    val selected: Boolean,
    val onClick: () -> Unit,
    val filledIcon: Int,
    val outlineIcon: Int,
)

private val JellyStandardEasing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
private const val JellySelectedAlpha = 0.17f

@Composable
internal fun CoverPlayJellyNavigationBar(
    items: List<JellyNavItem>,
    accent: Color,
    idleTint: Color,
    modifier: Modifier = Modifier,
    hazeState: HazeState? = null,
    labelsVisible: Boolean = true,
) {
    if (items.isEmpty()) return
    val labelFraction by animateFloatAsState(
        targetValue = if (labelsVisible) 1f else 0f,
        animationSpec = tween(300, easing = JellyStandardEasing),
        label = "jelly_labels",
    )
    val selectedSurface = accent.copy(alpha = JellySelectedAlpha)
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val selectedIndex = items.indexOfFirst { it.selected }
    val visualSelectedIndex = visualNavIndex(selectedIndex, items.size, isRtl)
    val motion = remember(items.size, isRtl) { JellyMotion(visualSelectedIndex, items.size) }
    val currentItems by rememberUpdatedState(items)
    val currentIsRtl by rememberUpdatedState(isRtl)
    val density = LocalDensity.current
    val trackHeight = 48.dp + 16.dp * labelFraction
    val horizontalPadding = 58.dp - 30.dp * labelFraction
    val bottomPad = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 14.dp

    LaunchedEffect(visualSelectedIndex, items.size) { motion.select(visualSelectedIndex) }
    LaunchedEffect(motion.running) {
        if (!motion.running) return@LaunchedEffect
        var previous = withFrameNanos { it }
        while (motion.running) {
            withFrameNanos { now ->
                motion.advance((now - previous) / 1_000_000_000.0)
                previous = now
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = bottomPad)
            .padding(horizontal = horizontalPadding),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 400.dp)
                .fillMaxWidth()
                .height(trackHeight)
                .onSizeChanged { motion.resize(it.width / density.density, it.height / density.density, items.size) }
                .pointerInput(motion, density, items.size, isRtl) {
                    detectJellyTabGestures(motion, density.density, { currentItems }, { currentIsRtl })
                },
        ) {
            Box(Modifier.matchParentSize().graphicsLayer {
                val f = motion.frame
                scaleX = f.trackScale; scaleY = f.trackScale
            }) {
                Box(Modifier.matchParentSize().graphicsLayer {
                    val f = motion.frame
                    transformOrigin = TransformOrigin(
                        if (size.width > 0) f.originX * density.density / size.width else 0.5f, 0.5f,
                    )
                    scaleX = f.trackScaleX
                    translationY = f.trackOffsetY * density.density
                }) {
                    Box(Modifier.matchParentSize().graphicsLayer { translationX = motion.frame.panelOffset * density.density }) {
                        Box(
                            Modifier.matchParentSize()
                                .clip(RoundedCornerShape(50))
                                .drawWithContent {
                                    drawContent()
                                    drawJellyGlow(motion.frame, accent)
                                },
                        ) { CoverPlayGlassBarSurface(hazeState, Modifier.matchParentSize()) }

                        Box(
                            Modifier.matchParentSize().drawWithContent {
                                if (selectedIndex >= 0) {
                                    clipPath(jellyPillPath(motion.frame, items.size), ClipOp.Difference) {
                                        this@drawWithContent.drawContent()
                                    }
                                } else drawContent()
                            },
                        ) { CoverPlayJellyTabRow(items, labelFraction, motion, false, accent, idleTint, Modifier.matchParentSize()) }

                        if (selectedIndex >= 0) {
                            Box(
                                Modifier.matchParentSize()
                                    .clearAndSetSemantics {}
                                    .drawWithContent {
                                        drawJellyPill(motion.frame, items.size, selectedSurface, accent) { drawContent() }
                                    },
                            ) { CoverPlayJellyTabRow(items, labelFraction, motion, true, accent, idleTint, Modifier.matchParentSize()) }
                        }
                        CoverPlayJellyTabTargets(items, motion, Modifier.matchParentSize())
                    }
                }
            }
        }
    }
}

internal fun visualNavIndex(logicalIndex: Int, count: Int, isRtl: Boolean): Int =
    if (logicalIndex in 0 until count && isRtl) count - 1 - logicalIndex else logicalIndex

internal fun logicalNavIndex(visualIndex: Int, count: Int, isRtl: Boolean): Int =
    if (visualIndex in 0 until count && isRtl) count - 1 - visualIndex else visualIndex

private suspend fun PointerInputScope.detectJellyTabGestures(
    motion: JellyMotion,
    density: Float,
    currentItems: () -> List<JellyNavItem>,
    isRtl: () -> Boolean,
) {
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
        motion.begin(down.position.x / density, down.position.y / density)
        var claimed = false
        var finished = false
        try {
            while (true) {
                val event = awaitPointerEvent(PointerEventPass.Initial)
                if (!motion.dragging) { finished = true; break }
                val change = event.changes.firstOrNull { it.id == down.id } ?: break
                if (event.changes.count { it.pressed } > 1) break
                val delta = change.position - down.position
                if (max(abs(delta.x), abs(delta.y)) > viewConfiguration.touchSlop) claimed = true
                if (claimed) change.consume()
                awaitPointerEvent(PointerEventPass.Main)
                if (!motion.dragging) { finished = true; break }
                if (change.isConsumed && !claimed) break
                motion.drag(delta.x / density, delta.y / density)
                if (!change.pressed) {
                    val visualIndex = motion.finish()
                    val list = currentItems()
                    val logicalIndex = logicalNavIndex(visualIndex, list.size, isRtl())
                    finished = true
                    list.getOrNull(logicalIndex)?.onClick?.invoke()
                    change.consume()
                    break
                }
            }
        } finally {
            if (!finished) {
                val list = currentItems()
                motion.cancel(visualNavIndex(list.indexOfFirst { it.selected }, list.size, isRtl()))
            }
        }
    }
}

@Composable
private fun CoverPlayJellyTabRow(
    items: List<JellyNavItem>,
    labelFraction: Float,
    motion: JellyMotion,
    active: Boolean,
    accent: Color,
    idleTint: Color,
    modifier: Modifier,
) {
    val color = if (active) accent else idleTint
    val iconSize = 25.dp
    val labelHeight = 16.dp
    Row(modifier.padding(4.dp).clearAndSetSemantics {}, verticalAlignment = Alignment.CenterVertically) {
        items.forEach { item ->
            Box(
                Modifier.weight(1f).fillMaxHeight().graphicsLayer {
                    val s = if (active) motion.frame.contentScale else 1f
                    scaleX = s; scaleY = s
                },
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(Modifier.size(iconSize).graphicsLayer { translationY = 2.dp.toPx() * labelFraction }) {
                        Icon(
                            painter = painterResource(if (active) item.filledIcon else item.outlineIcon),
                            contentDescription = null,
                            modifier = Modifier.size(iconSize),
                            tint = color,
                        )
                    }
                    Box(Modifier.height(labelHeight * labelFraction).fillMaxWidth().clipToBounds().alpha(labelFraction)) {
                        Text(
                            text = item.label,
                            color = color,
                            style = TextStyle(
                                fontSize = 11.sp,
                                lineHeight = 14.sp,
                                letterSpacing = 0.2.sp,
                                fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium,
                                textAlign = TextAlign.Center,
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CoverPlayJellyTabTargets(items: List<JellyNavItem>, motion: JellyMotion, modifier: Modifier) {
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    Row(modifier.padding(horizontal = 4.dp).selectableGroup()) {
        items.forEachIndexed { index, item ->
            val visualIndex = visualNavIndex(index, items.size, isRtl)
            val click = { motion.select(visualIndex); item.onClick() }
            Box(
                Modifier.weight(1f).fillMaxHeight()
                    .selectable(selected = item.selected, role = Role.Tab, interactionSource = null, indication = null, onClick = click)
                    .clearAndSetSemantics {
                        role = Role.Tab
                        selected = item.selected
                        contentDescription = item.label
                        onClick { click(); true }
                    },
            )
        }
    }
}

internal data class JellyFrame(
    val position: Float,
    val pillScaleX: Float = 1f,
    val pillScaleY: Float = 1f,
    val contentScale: Float = 1f,
    val panelOffset: Float = 0f,
    val trackScale: Float = 1f,
    val trackScaleX: Float = 1f,
    val trackOffsetY: Float = 0f,
    val originX: Float = 0f,
    val glowY: Float = 0f,
    val glowOpacity: Float = 0f,
)

internal class JellySpring(var value: Double, private val stiffness: Double, private val dampingRatio: Double) {
    var velocity = 0.0
    fun isAtRest(target: Double) = abs(value - target) < 0.0001 && abs(velocity) < 0.0001
    fun snapTo(target: Double) { value = target; velocity = 0.0 }
    fun advance(target: Double, seconds: Double) {
        if (isAtRest(target)) { snapTo(target); return }
        val displacement = value - target
        val frequency = sqrt(stiffness)
        if (dampingRatio == 1.0) {
            val decay = exp(-frequency * seconds)
            val coefficient = velocity + frequency * displacement
            value = target + (displacement + coefficient * seconds) * decay
            velocity = (velocity - frequency * coefficient * seconds) * decay
        } else {
            val damping = dampingRatio * frequency
            val damped = frequency * sqrt(1 - dampingRatio * dampingRatio)
            val decay = exp(-damping * seconds)
            val cosine = cos(damped * seconds)
            val sine = sin(damped * seconds)
            val positionCoefficient = (velocity + damping * displacement) / damped
            val velocityCoefficient = (damping * velocity + stiffness * displacement) / damped
            value = target + decay * (displacement * cosine + positionCoefficient * sine)
            velocity = decay * (velocity * cosine - velocityCoefficient * sine)
        }
    }
}

private fun jellyRubberBand(distance: Double, dimension: Double): Double {
    if (distance == 0.0 || dimension <= 0.0) return 0.0
    val damped = (1 - 1 / (abs(distance) * 0.14 / dimension + 1)) * dimension
    return if (distance < 0) -damped else damped
}

private fun jellyPanelOffset(rawOffset: Double, width: Double): Float {
    if (width <= 0 || rawOffset == 0.0) return 0f
    val fraction = (rawOffset / width).coerceIn(-1.0, 1.0)
    val x = abs(fraction)
    var low = 0.0
    var high = 1.0
    var p = x
    repeat(10) {
        val bx = p * p * (3 * (1 - p) * 0.58 + p)
        if (bx < x) low = p else high = p
        p = (low + high) / 2
    }
    val eased = p * p * (3 * (1 - p) + p)
    return ((if (fraction < 0) -4 else 4) * eased).toFloat()
}

@Stable
internal class JellyMotion(initialIndex: Int, count: Int) {
    private val position = JellySpring(initialIndex.coerceAtLeast(0).toDouble(), 1000.0, 1.0)
    private val velocity = JellySpring(0.0, 300.0, 0.5)
    private val press = JellySpring(0.0, 1000.0, 1.0)
    private val scaleX = JellySpring(1.0, 250.0, 0.6)
    private val scaleY = JellySpring(1.0, 250.0, 0.7)
    private val panel = JellySpring(0.0, 300.0, 1.0)
    private val distortionDamping = 18.0 / (2 * sqrt(240.0 * 0.9))
    private val trackY = JellySpring(0.0, 240.0 / 0.9, distortionDamping)
    private val trackX = JellySpring(1.0, 240.0 / 0.9, distortionDamping)
    private val trackPress = JellySpring(1.0, 240.0 / 0.9, distortionDamping)
    private val glow = JellySpring(0.0, 240.0 / 0.9, distortionDamping)
    private var target = position.value
    private var pressTarget = 0.0
    private var shapeTarget = 1.0
    private var releasePending = false
    private var downX = 0.0
    private var downY = 0.0
    private var dragStartTarget = target
    private var dragStartPanel = 0.0
    private var dragStartY = 0.0
    private var movedDistance = 0.0
    private var originX = 0.0
    private var width = 0.0
    private var height = 64.0
    private var tabCount = count
    private val maxIndex get() = (tabCount - 1).coerceAtLeast(0)
    private val tabWidth get() = ((width - 8) / tabCount.coerceAtLeast(1)).coerceAtLeast(0.0)

    var dragging = false
        private set
    var running by mutableStateOf(false)
        private set
    var frame by mutableStateOf(JellyFrame(position.value.toFloat()))
        private set

    fun resize(width: Float, height: Float, count: Int) {
        this.width = width.toDouble()
        this.height = height.toDouble()
        tabCount = count
        target = target.coerceIn(0.0, maxIndex.toDouble())
        if (!dragging) originX = this.width / 2
        publish()
    }

    fun select(index: Int) {
        dragging = false
        if (index >= 0) target = index.coerceAtMost(maxIndex).toDouble()
        releasePending = true
        pressTarget = 0.0
        shapeTarget = 1.0
        running = true
    }

    fun begin(x: Float, y: Float) {
        downX = x.toDouble()
        downY = y.toDouble().coerceIn(0.0, height)
        originX = downX.coerceIn(0.0, width)
        dragStartY = trackY.value
        movedDistance = 0.0
        if (tabWidth > 0) target = indexAt(downX).toDouble()
        dragStartTarget = target
        dragStartPanel = panel.value
        dragging = true
        releasePending = false
        pressTarget = 1.0
        shapeTarget = 1.3
        panel.velocity = 0.0
        running = true
    }

    fun drag(x: Float, y: Float) {
        if (!dragging || tabWidth <= 0) return
        val dx = x.toDouble()
        val dy = y.toDouble()
        target = (dragStartTarget + dx / tabWidth).coerceIn(0.0, maxIndex.toDouble())
        panel.snapTo(dragStartPanel + dx)
        trackY.snapTo(dragStartY + jellyRubberBand(dy, height) * 0.25)
        trackX.snapTo(1 - (abs(dy) / 700).coerceAtMost(1.0) * 0.08)
        originX = (downX + dx).coerceIn(0.0, width)
        movedDistance = max(movedDistance, max(abs(dx), abs(dy)))
        publish()
    }

    fun finish(): Int {
        val index = if (movedDistance < 4 && tabWidth > 0) indexAt(downX)
        else floor(target + 0.5).toInt().coerceIn(0, maxIndex)
        dragging = false
        panel.velocity = 0.0
        target = index.toDouble()
        releasePending = true
        running = true
        return index
    }

    fun cancel(selectedIndex: Int) {
        dragging = false
        panel.velocity = 0.0
        select(selectedIndex)
    }

    fun advance(seconds: Double) {
        val delta = seconds.coerceIn(0.0, 0.064)
        target = target.coerceIn(0.0, maxIndex.toDouble())
        position.advance(target, delta)
        velocity.advance(if (dragging && maxIndex > 0) position.velocity / maxIndex else 0.0, delta)
        if (!dragging) panel.advance(0.0, delta)
        if (releasePending && abs(position.value - target) < max(1, maxIndex) * 0.025) {
            releasePending = false
            pressTarget = 0.0
            shapeTarget = 1.0
        }
        press.advance(pressTarget, delta)
        scaleX.advance(shapeTarget, delta)
        scaleY.advance(shapeTarget, delta)
        trackPress.advance(if (dragging) 1.025 else 1.0, delta)
        glow.advance(if (dragging) 1.0 else 0.0, delta)
        if (!dragging) {
            trackY.advance(0.0, delta)
            trackX.advance(1.0, delta)
            if (trackX.isAtRest(1.0)) originX = width / 2
        }
        publish()
        running = dragging || releasePending || !position.isAtRest(target) || !velocity.isAtRest(0.0) ||
                !press.isAtRest(0.0) || !scaleX.isAtRest(1.0) || !scaleY.isAtRest(1.0) ||
                !panel.isAtRest(0.0) || !trackY.isAtRest(0.0) || !trackX.isAtRest(1.0) ||
                !trackPress.isAtRest(1.0) || !glow.isAtRest(0.0)
    }

    private fun indexAt(x: Double): Int = floor((x - 4) / tabWidth).toInt().coerceIn(0, maxIndex)

    private fun publish() {
        val speed = velocity.value / 10
        frame = JellyFrame(
            position = position.value.toFloat(),
            pillScaleX = (scaleX.value / (1 - (speed * 0.75).coerceIn(-0.2, 0.2))).toFloat(),
            pillScaleY = (scaleY.value * (1 - (speed * 0.25).coerceIn(-0.2, 0.2))).toFloat(),
            contentScale = (1 + 0.2 * press.value).toFloat(),
            panelOffset = jellyPanelOffset(panel.value, width),
            trackScale = trackPress.value.toFloat(),
            trackScaleX = trackX.value.toFloat(),
            trackOffsetY = trackY.value.toFloat(),
            originX = originX.toFloat(),
            glowY = downY.toFloat(),
            glowOpacity = glow.value.toFloat().coerceIn(0f, 1f),
        )
    }
}

private fun DrawScope.drawJellyGlow(frame: JellyFrame, color: Color) {
    if (frame.glowOpacity <= 0f || color.alpha <= 0f) return
    val a = 0.15f * frame.glowOpacity * color.alpha
    drawRect(
        brush = Brush.radialGradient(
            0f to color.copy(alpha = a),
            0.45f to color.copy(alpha = a * 0.43f),
            1f to color.copy(alpha = 0f),
            center = Offset(frame.originX.dp.toPx(), frame.glowY.dp.toPx()),
            radius = 300.dp.toPx(),
        ),
        topLeft = Offset(-48.dp.toPx(), -16.dp.toPx()),
        size = Size(size.width + 96.dp.toPx(), size.height + 32.dp.toPx()),
    )
}

private fun DrawScope.jellyPillPath(frame: JellyFrame, count: Int): Path {
    val inset = 4.dp.toPx()
    val tabWidth = (size.width - 2 * inset) / count
    val itemHeight = size.height - 2 * inset
    val centerX = inset + (frame.position + 0.5f) * tabWidth
    val halfWidth = tabWidth * frame.pillScaleX / 2
    val halfHeight = itemHeight * frame.pillScaleY / 2
    val radius = min(tabWidth, itemHeight) / 2
    return Path().apply {
        addRoundRect(
            RoundRect(
                left = centerX - halfWidth,
                top = size.height / 2 - halfHeight,
                right = centerX + halfWidth,
                bottom = size.height / 2 + halfHeight,
                cornerRadius = CornerRadius(radius * frame.pillScaleX, radius * frame.pillScaleY),
            ),
        )
    }
}

private fun DrawScope.drawJellyPill(frame: JellyFrame, count: Int, surface: Color, glow: Color, content: () -> Unit) {
    clipPath(jellyPillPath(frame, count)) {
        drawRect(
            color = surface,
            topLeft = Offset(-48.dp.toPx(), -16.dp.toPx()),
            size = Size(size.width + 96.dp.toPx(), size.height + 32.dp.toPx()),
        )
        drawJellyGlow(frame, glow)
        content()
    }
}

private val GlassSurfaceColor = Color(0xFF0B0B0F)

private const val GlassBarShader = """
uniform shader backdrop;
uniform float2 resolution;
uniform float density;
uniform float outset;
uniform float glowStrength;

half3 sampleLight(float2 position, float2 tangent) {
    float2 spread = tangent * density * 10.0;
    return backdrop.eval(position).rgb * 0.5
        + backdrop.eval(position - spread).rgb * 0.25
        + backdrop.eval(position + spread).rgb * 0.25;
}

half4 main(float2 position) {
    float2 halfSize = resolution * 0.5 - outset;
    float radius = halfSize.y;
    float2 local = position - resolution * 0.5;
    float2 capsule = float2(max(abs(local.x) - halfSize.x + radius, 0.0), local.y);
    float distanceToCenter = length(capsule);
    float distanceToEdge = distanceToCenter - radius;
    float coverage = 1.0 - smoothstep(-0.5, 0.5, distanceToEdge);
    if (coverage <= 0.0) return half4(0.0);

    float2 normal = float2(capsule.x * sign(local.x), capsule.y)
        / max(distanceToCenter, 0.001);
    float2 tangent = float2(-normal.y, normal.x);
    float depth = max(-distanceToEdge, 0.0) / density;
    half3 surface = mix(backdrop.eval(position).rgb, half3(28.0, 28.0, 30.0) / 255.0, 0.55);
    if (depth >= 16.0 || glowStrength <= 0.0) return half4(surface * coverage, coverage);

    float rim = exp(-0.0565 * depth - 0.0322 * depth * depth);
    float upperLight = 0.18 + 0.82 * pow(max(-normal.y, 0.0), 0.65);
    float bend = pow(rim, 0.18);

    half3 redLight = sampleLight(position - normal * density * 16.0 * bend, tangent);
    half3 greenLight = sampleLight(position - normal * density * 62.0 * bend, tangent);
    half3 blueLight = sampleLight(position - normal * density * 57.0 * bend, tangent);
    half3 refracted = half3(redLight.r, greenLight.g, blueLight.b);
    half luminance = dot(refracted, half3(0.2126, 0.7152, 0.0722));
    refracted = clamp(mix(half3(luminance), refracted, 1.25), 0.0, 1.0);

    half3 sheen = half3(0.1735, 0.0529, 0.0184) + refracted * half3(0.0953, 0.3152, 0.3822);
    half3 color = surface + sheen * rim * upperLight * glowStrength;
    float highlight = exp(-pow((depth - 0.35) / 0.42, 2.0));
    float highlightLight = 0.12 + 0.88 * sqrt(max((1.0 - normal.y) * 0.5, 0.0));
    color += half3(0.25) * highlight * highlightLight * glowStrength;
    return half4(clamp(color, 0.0, 1.0) * coverage, coverage);
}
"""

@Composable
private fun CoverPlayGlassBarSurface(hazeState: HazeState?, modifier: Modifier = Modifier, glowStrength: Float = 1f) {
    val palette = LocalAppUiSettings.current.palette
    val ink = remember(palette) { lerp(palette.surfaceNavy, palette.bg, 0.45f) }
    Box(
        modifier
            .then(if (hazeState != null && hazeState.blurEnabled) Modifier.jellyBarBackdrop(hazeState) else Modifier)
            .drawWithCache {
                val fill = ink.copy(alpha = if (hazeState != null && hazeState.blurEnabled) 0.78f else 0.97f)
                val top = Brush.verticalGradient(
                    0f to Color.White.copy(alpha = 0.07f), 0.55f to Color.Transparent, endY = size.height
                )
                val edge = Brush.verticalGradient(
                    listOf(Color.White.copy(alpha = 0.20f), Color.White.copy(alpha = 0.05f))
                )
                val w = 1.dp.toPx()
                onDrawBehind {
                    drawRect(fill)
                    drawRect(top)
                    drawRoundRect(
                        brush = edge,
                        topLeft = Offset(w / 2, w / 2),
                        size = Size(size.width - w, size.height - w),
                        cornerRadius = CornerRadius((size.height - w) / 2),
                        style = Stroke(w),
                    )
                }
            },
    )
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
private fun CoverPlayRefractedGlassBar(hazeState: HazeState, modifier: Modifier, glowStrength: Float) {
    val shader = remember { RuntimeShader(GlassBarShader) }
    Box(
        modifier
            .layout { measurable, constraints ->
                val outset = 24.dp.roundToPx()
                val placeable = measurable.measure(constraints.offset(outset * 2, outset * 2))
                layout(placeable.width - outset * 2, placeable.height - outset * 2) {
                    placeable.place(-outset, -outset)
                }
            }
            .graphicsLayer {
                shader.setFloatUniform("resolution", size.width, size.height)
                shader.setFloatUniform("density", density)
                shader.setFloatUniform("outset", 24.dp.roundToPx().toFloat())
                shader.setFloatUniform("glowStrength", glowStrength)
                renderEffect = RenderEffect.createRuntimeShaderEffect(shader, "backdrop").asComposeRenderEffect()
            }
            .jellyBarBackdrop(hazeState),
    )
}

private fun Modifier.jellyBarBackdrop(hazeState: HazeState): Modifier = hazeEffect(state = hazeState) {
    blurRadius = 24.dp
    backgroundColor = GlassSurfaceColor
    tints = listOf(HazeTint(GlassSurfaceColor.copy(alpha = 0.72f)))
    noiseFactor = 0.035f
}
