package com.raman.CoverPlay.player

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raman.CoverPlay.R
import com.raman.CoverPlay.Screen
import com.raman.CoverPlay.formatDuration
import com.raman.CoverPlay.ui.theme.CoverPlayMaterialShapes
import com.raman.CoverPlay.ui.theme.CoverPlayRotatingShape
import com.raman.CoverPlay.ui.theme.LocalAppUiSettings
import com.raman.CoverPlay.ui.theme.onFill
import com.raman.CoverPlay.ui.theme.rememberCoverPlayPressScale
import com.raman.CoverPlay.util.rememberCoverPlayHaptic
import kotlin.math.PI
import kotlin.math.sin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val OverlayScrim = Color.Black.copy(alpha = 0.52f)

@Composable
fun CoverPlayGestureIndicator(
    visible: Boolean,
    isBrightness: Boolean,
    value: Float,
    modifier: Modifier = Modifier,
    swapSides: Boolean = false
) {
    val ui = LocalAppUiSettings.current
    val accent = ui.accent
    val onAccent = accent.onFill()
    val boostColor = ui.highlight
    val cookie = CoverPlayMaterialShapes.cookie9()

    var onRight by remember { mutableStateOf(isBrightness != swapSides) }
    var brightnessMode by remember { mutableStateOf(isBrightness) }
    if (visible) { onRight = isBrightness != swapSides; brightnessMode = isBrightness }

    val animated by animateFloatAsState(value, spring(1f, 900f), label = "gbFill")
    val fill = animated.coerceIn(0f, 1f)
    val boost = (animated - 1f).coerceIn(0f, 1f)
    val percent = (animated * 100f).toInt().coerceAtLeast(0)

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(120)) +
                scaleIn(initialScale = 0.82f, animationSpec = spring(0.62f, 420f)) +
                slideInHorizontally(spring(0.7f, 420f)) { if (onRight) it / 2 else -it / 2 },
        exit = fadeOut(tween(200)) +
                scaleOut(targetScale = 0.9f, animationSpec = tween(200)) +
                slideOutHorizontally(tween(200)) { if (onRight) it / 3 else -it / 3 },
        modifier = modifier
    ) {
        Box(Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .align(if (onRight) Alignment.CenterEnd else Alignment.CenterStart)
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(OverlayScrim)
                    .padding(horizontal = 10.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "$percent",
                    color = if (boost > 0f) boostColor else Color.White,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold, fontSize = 14.sp, fontFeatureSettings = "tnum"
                    )
                )

                Box(
                    Modifier
                        .width(46.dp)
                        .height(170.dp)
                        .clip(RoundedCornerShape(23.dp))
                        .background(Color.White.copy(alpha = 0.16f))
                ) {
                    Box(
                        Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .fillMaxHeight(fill)
                            .clip(RoundedCornerShape(23.dp))
                            .background(accent)
                    ) {
                        if (fill > 0.06f) {
                            Box(
                                Modifier
                                    .align(Alignment.TopCenter)
                                    .padding(top = 7.dp)
                                    .width(16.dp)
                                    .height(4.dp)
                                    .clip(CircleShape)
                                    .background(onAccent.copy(alpha = 0.6f))
                            )
                        }
                    }
                    if (boost > 0.001f) {
                        Box(
                            Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .fillMaxHeight(boost)
                                .clip(RoundedCornerShape(23.dp))
                                .background(boostColor)
                        )
                    }
                }

                Box(Modifier.size(42.dp), contentAlignment = Alignment.Center) {
                    CoverPlayRotatingShape(
                        shape = cookie, modifier = Modifier.fillMaxSize(),
                        color = accent, periodMillis = 12000
                    )
                    AnimatedContent(
                        targetState = if (brightnessMode) 0 else if (percent > 0) 1 else 2,
                        transitionSpec = {
                            (fadeIn(tween(140)) + scaleIn(initialScale = 0.5f, animationSpec = spring(0.6f, 500f)))
                                .togetherWith(fadeOut(tween(90)) + scaleOut(targetScale = 0.5f))
                        },
                        label = "gbIcon"
                    ) { state ->
                        Icon(
                            painterResource(
                                when (state) {
                                    0 -> R.drawable.ic_brightness_custom
                                    1 -> R.drawable.ic_volume_custom
                                    else -> R.drawable.ic_volume_off_custom
                                }
                            ),
                            contentDescription = null, tint = onAccent, modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

private class SeekFlashLatch {
    var secs = 0
    var x = 0.5f
    var y = 0.5f
}

@Composable
fun CoverPlaySeekFlash(
    visible: Boolean,
    token: Int,
    cumulativeSeconds: Int,
    isForward: Boolean,
    modifier: Modifier = Modifier,
    tapXFraction: Float = 0.5f,
    tapYFraction: Float = 0.5f
) {
    val ripple = remember { Animatable(1f) }
    val press = remember { Animatable(1f) }
    val sweep = remember { Animatable(0f) }
    val show by animateFloatAsState(
        if (visible) 1f else 0f,
        tween(if (visible) 100 else 240, easing = FastOutSlowInEasing), label = "seekShow"
    )

    val latch = remember { SeekFlashLatch() }
    if (cumulativeSeconds > 0) latch.secs = cumulativeSeconds
    if (visible) { latch.x = tapXFraction; latch.y = tapYFraction }

    LaunchedEffect(token, visible) {
        if (token > 0 && visible) {
            coroutineScope {
                launch {
                    ripple.snapTo(0f)
                    ripple.animateTo(1f, tween(520, easing = FastOutSlowInEasing))
                }
                launch {
                    press.snapTo(0.94f)
                    press.animateTo(1f, spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = 600f))
                }
                sweep.snapTo(0f)
                sweep.animateTo(1f, tween(480, easing = LinearEasing))
            }
        } else {
            sweep.snapTo(0f)
        }
    }

    if (show <= 0.001f) return

    Box(
        modifier = modifier
            .fillMaxHeight()
            .fillMaxWidth(0.5f)
            .clipToBounds()
            .graphicsLayer { alpha = show },
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val progress = ripple.value
            if (progress in 0.001f..0.999f) {
                val origin = Offset(latch.x * size.width, latch.y * size.height)
                val radius = size.height * (0.25f + 0.45f * progress)
                drawCircle(Color.White.copy(alpha = 0.14f * (1f - progress)), radius, origin)
            }
        }

        Row(
            modifier = Modifier
                .graphicsLayer { scaleX = press.value; scaleY = press.value }
                .clip(CircleShape)
                .background(OverlayScrim)
                .padding(horizontal = 18.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy((-8).dp), verticalAlignment = Alignment.CenterVertically) {
                for (i in 0 until 2) {
                    val order = if (isForward) i else 1 - i
                    Icon(
                        painter = painterResource(if (isForward) R.drawable.ic_forward_custom else R.drawable.ic_rewind_custom),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .size(18.dp)
                            .graphicsLayer {
                                val phase = (sweep.value * 1.4f - order * 0.25f).coerceIn(0f, 1f)
                                alpha = 0.4f + 0.6f * sin(phase * PI.toFloat()).coerceIn(0f, 1f)
                            }
                    )
                }
            }
            Spacer(Modifier.width(10.dp))
            Text(
                text = (if (isForward) "+" else "−") + latch.secs + "s",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold, fontSize = 17.sp, fontFeatureSettings = "tnum"
                )
            )
        }
    }
}

@Composable
fun CoverPlayPlayerLockOverlay(
    locked: Boolean,
    controlsVisible: Boolean,
    showTimer: Boolean,
    positionMs: Long,
    onUnlock: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ui = LocalAppUiSettings.current
    val accent = ui.accent
    val onAccent = accent.onFill()
    val cookie = CoverPlayMaterialShapes.cookie9()

    var toast by remember { mutableStateOf(false) }
    LaunchedEffect(locked) {
        if (locked) {
            toast = true; delay(1700); toast = false
        } else {
            toast = false
        }
    }

    Box(modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = locked && toast,
            enter = fadeIn(tween(160)) +
                    scaleIn(initialScale = 0.8f, animationSpec = spring(0.62f, 420f)) +
                    slideInVertically(spring(0.7f, 420f)) { -it },
            exit = fadeOut(tween(200)) + scaleOut(targetScale = 0.9f, animationSpec = tween(200)) +
                    slideOutVertically(tween(200)) { -it / 2 },
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 28.dp)
        ) {
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(accent)
                    .padding(start = 6.dp, end = 18.dp, top = 6.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(Modifier.size(34.dp), contentAlignment = Alignment.Center) {
                    CoverPlayRotatingShape(
                        shape = cookie, modifier = Modifier.fillMaxSize(),
                        color = onAccent.copy(alpha = 0.16f), periodMillis = 12000
                    )
                    Icon(painterResource(R.drawable.ic_lock_custom), null, tint = onAccent, modifier = Modifier.size(16.dp))
                }
                Text(
                    "Screen locked",
                    color = onAccent,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }

        AnimatedVisibility(
            visible = locked && controlsVisible,
            enter = fadeIn(tween(200)) + scaleIn(initialScale = 0.6f, animationSpec = spring(0.62f, 420f)),
            exit = fadeOut(tween(160)) + scaleOut(targetScale = 0.8f, animationSpec = tween(160)),
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 28.dp)
        ) {
            var unlocking by remember { mutableStateOf(false) }
            val interaction = remember { MutableInteractionSource() }
            val press by rememberCoverPlayPressScale(interaction, 0.88f)
            val haptic = rememberCoverPlayHaptic(HapticFeedbackType.LongPress)
            LaunchedEffect(unlocking) {
                if (unlocking) { delay(260); onUnlock(); unlocking = false }
            }
            val bump by animateFloatAsState(
                if (unlocking) 1.14f else 1f, spring(0.5f, 500f), label = "unlockBump"
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .graphicsLayer { scaleX = press * bump; scaleY = press * bump }
                    .clip(RoundedCornerShape(28.dp))
                    .clickable(interactionSource = interaction, indication = null) {
                        if (!unlocking) { haptic(); unlocking = true }
                    }
                    .padding(6.dp)
            ) {
                Box(Modifier.size(64.dp), contentAlignment = Alignment.Center) {
                    CoverPlayRotatingShape(
                        shape = cookie, modifier = Modifier.fillMaxSize(),
                        color = accent, periodMillis = 12000
                    )
                    AnimatedContent(
                        targetState = unlocking,
                        transitionSpec = {
                            (fadeIn(tween(120)) + scaleIn(initialScale = 0.5f, animationSpec = spring(0.55f, 520f)))
                                .togetherWith(fadeOut(tween(90)) + scaleOut(targetScale = 0.5f))
                        },
                        label = "unlockGlyph"
                    ) { open ->
                        Icon(
                            painterResource(if (open) R.drawable.ic_lock_open_custom else R.drawable.ic_lock_custom),
                            contentDescription = "Unlock",
                            tint = onAccent,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "Tap to unlock",
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(OverlayScrim)
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                )
            }
        }

        AnimatedVisibility(
            visible = locked && showTimer,
            enter = fadeIn(tween(240)),
            exit = fadeOut(tween(200)),
            modifier = Modifier.align(Alignment.BottomStart).padding(start = 24.dp, bottom = 24.dp)
        ) {
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.30f))
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(painterResource(R.drawable.ic_lock_custom), null, tint = Color.White.copy(alpha = 0.55f), modifier = Modifier.size(12.dp))
                Text(
                    formatDuration(positionMs),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold, fontFeatureSettings = "tnum"),
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        }
    }
}
