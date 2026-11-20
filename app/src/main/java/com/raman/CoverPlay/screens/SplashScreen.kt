package com.raman.CoverPlay.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raman.CoverPlay.ui.theme.LocalAppUiSettings
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val BrandBlueTop = Color(0xFF5688FD)
private val BrandBlueBottom = Color(0xFF4575F4)
private val BrandYellow = Color(0xFFFFD333)
private val BrandRed = Color(0xFFFF7365)
private val BrandBadge = Color(0xFFF9FAFF)

private class MarkProgress {
    val card = Animatable(0f)
    val yellow = Animatable(0f)
    val red = Animatable(0f)
    val badge = Animatable(0f)
    val play = Animatable(0f)
    val squiggle = Animatable(0f)
    val bar = Animatable(0f)
}

@Composable
fun CoverPlaySplashScreen(onAnimationFinish: () -> Unit) {
    val ui = LocalAppUiSettings.current
    val p = ui.palette

    val mark = remember { MarkProgress() }
    val glow = remember { Animatable(0f) }
    val title = remember { Animatable(0f) }
    val tagline = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch {
            glow.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 1400,
                    easing = FastOutSlowInEasing
                )
            )
        }

        launch {
            mark.card.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = 0.72f,
                    stiffness = 260f
                )
            )
        }

        launch {
            delay(160)
            mark.yellow.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = 0.65f,
                    stiffness = 250f
                )
            )
        }

        launch {
            delay(280)
            mark.red.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = 0.65f,
                    stiffness = 250f
                )
            )
        }

        launch {
            delay(420)
            mark.badge.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = 0.58f,
                    stiffness = 210f
                )
            )
        }

        launch {
            delay(600)
            mark.play.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = 0.55f,
                    stiffness = 350f
                )
            )
        }

        launch {
            delay(740)

            mark.squiggle.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 550,
                    easing = FastOutSlowInEasing
                )
            )

            mark.bar.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 300,
                    easing = FastOutSlowInEasing
                )
            )
        }

        launch {
            delay(820)
            title.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 550,
                    easing = FastOutSlowInEasing
                )
            )
        }

        launch {
            delay(1050)
            tagline.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 650,
                    easing = FastOutSlowInEasing
                )
            )
        }

        delay(2500)
        onAnimationFinish()
    }

    val idle = rememberInfiniteTransition(label = "coverPlayIdle")

    val spin = idle.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 30_000,
                easing = LinearEasing
            )
        ),
        label = "badgeRotation"
    )

    val wave = idle.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 2600,
                easing = LinearEasing
            )
        ),
        label = "waveMotion"
    )

    val bob = idle.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 2600,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logoFloat"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(p.bg),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(460.dp)
                .graphicsLayer {
                    alpha = glow.value *
                            (if (p.isDark) 0.26f else 0.12f
                                    )
                }
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            BrandBlueTop.copy(alpha = 0.8f),
                            BrandBlueTop.copy(alpha = 0.25f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CoverPlayMark(
                mark = mark,
                spin = spin,
                wave = wave,
                modifier = Modifier
                    .size(176.dp)
                    .graphicsLayer {
                        translationY = bob.value * 2.dp.toPx()
                    }
            )

            Spacer(Modifier.height(28.dp))

            Row(
                modifier = Modifier.graphicsLayer {
                    alpha = title.value.coerceIn(0f, 1f)
                    translationY =
                        (1f - title.value) * 12.dp.toPx()
                }
            ) {
                val style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                )

                Text(
                    text = "Cover",
                    style = style,
                    color = p.textPrimary
                )

                Text(
                    text = "Play",
                    style = style,
                    color = BrandBlueTop
                )
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = "CINEMATIC MEDIA ENGINE",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 2.2.sp
                ),
                color = p.textSecondary,
                modifier = Modifier.graphicsLayer {
                    alpha = tagline.value.coerceIn(0f, 1f) * 0.8f
                    translationY = (1f - tagline.value) * 5.dp.toPx()
                }
            )
        }
    }
}

private fun badgePath(): Path = Path().apply {
    val n = 240

    for (i in 0..n) {
        val t = (i.toFloat() / n) * 2f * PI.toFloat()
        val r = 34f + 3.6f * cos(12f * t)

        val x = r * cos(t)
        val y = r * sin(t)

        if (i == 0) {
            moveTo(x, y)
        } else {
            lineTo(x, y)
        }
    }

    close()
}

@Composable
private fun CoverPlayMark(
    mark: MarkProgress,
    spin: State<Float>,
    wave: State<Float>,
    modifier: Modifier = Modifier
) {
    val badge = remember { badgePath() }

    val play = remember {
        Path().apply {
            moveTo(85.5f, 67.5f)
            lineTo(114f, 84f)
            lineTo(85.5f, 100.5f)
            close()
        }
    }

    val squiggle = remember { Path() }
    val segment = remember { Path() }
    val measure = remember { PathMeasure() }

    val cardBrush = remember {
        Brush.verticalGradient(
            colors = listOf(
                BrandBlueTop,
                BrandBlueBottom
            ),
            startY = 17f,
            endY = 175f
        )
    }

    Canvas(modifier = modifier) {
        val k = size.width / 192f

        withTransform({
            scale(k, k, pivot = Offset.Zero)
        }) {
            val centre = Offset(96f, 96f)

            val r = mark.red.value

            if (r > 0.02f) {
                withTransform({
                    scale(
                        0.5f + 0.5f * r,
                        1f,
                        pivot = centre
                    )
                }) {
                    drawRoundRect(
                        color = BrandRed.copy(
                            alpha = (r * 3f).coerceIn(0f, 1f)
                        ),
                        topLeft = Offset(9f, 62f),
                        size = Size(174f, 69f),
                        cornerRadius = CornerRadius(22f)
                    )
                }
            }

            val y = mark.yellow.value

            if (y > 0.02f) {
                withTransform({
                    scale(
                        0.65f + 0.35f * y,
                        1f,
                        pivot = centre
                    )
                }) {
                    drawRoundRect(
                        color = BrandYellow.copy(
                            alpha = (y * 3f).coerceIn(0f, 1f)
                        ),
                        topLeft = Offset(26f, 46f),
                        size = Size(140f, 101f),
                        cornerRadius = CornerRadius(24f)
                    )
                }
            }

            val c = mark.card.value

            withTransform({
                scale(
                    0.72f + 0.28f * c,
                    0.72f + 0.28f * c,
                    pivot = centre
                )
            }) {
                drawRoundRect(
                    brush = cardBrush,
                    topLeft = Offset(46f, 17f),
                    size = Size(100f, 158f),
                    cornerRadius = CornerRadius(34f),
                    alpha = c.coerceIn(0f, 1f)
                )
            }

            val b = mark.badge.value

            if (b > 0.01f) {
                withTransform({
                    translate(96f, 82f)
                    scale(b, b, pivot = Offset.Zero)
                    rotate(
                        (1f - b) * -140f + spin.value,
                        pivot = Offset.Zero
                    )
                }) {
                    drawPath(
                        path = badge,
                        color = BrandBadge
                    )
                }
            }

            val pl = mark.play.value

            if (pl > 0.01f) {
                withTransform({
                    scale(
                        pl,
                        pl,
                        pivot = Offset(96f, 84f)
                    )
                }) {
                    drawPath(
                        path = play,
                        color = BrandBlueBottom
                    )

                    drawPath(
                        path = play,
                        color = BrandBlueBottom,
                        style = Stroke(
                            width = 7f,
                            join = StrokeJoin.Round
                        )
                    )
                }
            }

            val sq = mark.squiggle.value

            if (sq > 0.01f) {
                squiggle.reset()

                var x = 58f

                while (x <= 97f) {
                    val yy = 152f + 3.2f * sin(
                        (x - 58f) / 14f * 2f * PI.toFloat() - wave.value
                    )

                    if (x == 58f) {
                        squiggle.moveTo(x, yy)
                    } else {
                        squiggle.lineTo(x, yy)
                    }

                    x += 0.5f
                }

                measure.setPath(squiggle, false)
                segment.reset()

                measure.getSegment(
                    startDistance = 0f,
                    stopDistance = measure.length * sq,
                    destination = segment,
                    startWithMoveTo = true
                )

                drawPath(
                    path = segment,
                    color = Color.White,
                    style = Stroke(
                        width = 4.2f,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }

            val bar = mark.bar.value

            if (bar > 0.01f) {
                drawLine(
                    color = Color.White,
                    start = Offset(102f, 146f),
                    end = Offset(102f, 158f),
                    strokeWidth = 3.6f,
                    cap = StrokeCap.Round,
                    alpha = bar.coerceIn(0f, 1f)
                )

                drawLine(
                    color = Color.White,
                    start = Offset(107f, 152f),
                    end = Offset(107f + 27f * bar, 152f),
                    strokeWidth = 4f,
                    cap = StrokeCap.Round,
                    alpha = 0.55f
                )
            }
        }
    }
}
