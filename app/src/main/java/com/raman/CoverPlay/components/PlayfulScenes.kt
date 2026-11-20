package com.raman.CoverPlay.components

import android.widget.Toast
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.raman.CoverPlay.settings.SettingsPage
import com.raman.CoverPlay.ui.theme.AppTheme
import com.raman.CoverPlay.ui.theme.LocalAppUiSettings
import com.raman.CoverPlay.ui.theme.onFill
import com.raman.CoverPlay.util.rememberCoverPlayHapticFeedback
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin
import kotlin.random.Random

val AppTheme.quips: List<String>
    get() = when (this) {
        AppTheme.IRIS_ROYAL -> listOf(
            "Green light. Pick a movie.",
            "Jade you were ready for movie night.",
            "The only green flag in your library.",
            "Dark room, bright screen. Press play."
        )
        AppTheme.NEON_VIOLET -> listOf(
            "Stardust and chill?",
            "Violet you were ready for movie night.",
            "Space: the final frontier of your watchlist.",
            "Houston, we have popcorn."
        )
        AppTheme.CYBER_MAGENTA -> listOf(
            "Roses are red, buffering's a lie, press play, my friend, and watch time fly.",
            "Curtain's up. Don't be a no-show.",
            "Rose-ta Stone of entertainment.",
            "Velvet you'd never ask."
        )
        AppTheme.SOLAR_EMBER -> listOf(
            "Too hot to handle, too good to skip.",
            "Ember-rassingly good picks ahead.",
            "Let it burn. Let it stream.",
            "This watchlist is on fire. Literally."
        )
        AppTheme.HYPER_CYAN -> listOf(
            "Aurora-ly good time ahead.",
            "Cool story, bro. Literally.",
            "Teal you're hooked.",
            "Chill. The lights are doing their thing."
        )
    }

object Humor {
    private val seed = Random.nextInt(1000)
    private var secretIdx = Random.nextInt(100)

    private val heads = listOf(
        listOf("Good morning", "Rise and stream", "Morning, legend"),
        listOf("Good afternoon", "Afternoon delight", "Hey, you"),
        listOf("Good evening", "Showtime", "Evening, cinephile"),
        listOf("Good night", "Night owl mode", "Still up?")
    )

    private val lines = listOf(
        listOf(
            "Coffee loaded. Buffer-free vibes only.",
            "Winter isn't coming. Your watchlist is.",
            "Today's forecast: 100% chance of popcorn.",
            "May the Wi-Fi be with you.",
            "Plot twist: you actually finish a series today.",
            "You had me at \"Play\"."
        ),
        listOf(
            "Lunch break? More like lunch-and-watch.",
            "Productivity is just procrastination with a plan. Press play.",
            "Hakuna Matata means no buffering.",
            "Just one episode. We both know that's a lie.",
            "Reel talk: you've earned a break.",
            "Fast, furious, and fully offline."
        ),
        listOf(
            "Your own library. No ads. Respect.",
            "Wake up, Neo. Your movies are waiting.",
            "Dinner's ready? So is your watchlist.",
            "Lights down. Volume up. Phone on silent.",
            "Golden hour for goldenly encoded files.",
            "Tonight's episode: You vs. The Remote."
        ),
        listOf(
            "It's late. \"One more episode\" is a trap.",
            "Sleep is for the weak. Subtitles are for the wise.",
            "The night is dark and full of terabytes.",
            "Even Batman streams at this hour.",
            "Your pillow called. I told it you're busy.",
            "Insomnia, but make it cinematic."
        )
    )

    private val recent = listOf(
        "Pick up where you left off. No spoilers, promise.",
        "Your last video is still warm. Resume?",
        "Back for more? Classic you.",
        "The plot thickens. Continue?"
    )

    fun greeting(period: Int, tick: Int, hasRecent: Boolean, theme: AppTheme): Pair<String, String> {
        val p = period.coerceIn(0, 3)
        val head = heads[p][(seed + tick) % heads[p].size]
        val pool = lines[p] + theme.quips + (if (hasRecent) recent else emptyList())
        val sub = pool[(seed * 3 + tick * 5 + p) % pool.size]
        return head to sub
    }

    private val secrets = listOf(
        "🎬 You found the secret reel! Popcorn's on the house.",
        "🕹️ ↑↑↓↓←→←→BA — wrong app, but we respect the effort.",
        "🧙 You shall not pass… without pressing play.",
        "🦖 Life, uh, finds a way… to buffer.",
        "🍿 Achievement unlocked: Professional Procrastinator.",
        "🐇 You followed the white rabbit. It had no videos either."
    )

    fun secret(): String = secrets[secretIdx++ % secrets.size]

    private val noResults = listOf(
        "Nothing here but crickets" to "That search ghosted us. Try another spelling?",
        "404: Video not found" to "Not even in the Upside Down.",
        "Elementary, my dear… nothing" to "Our detective checked every folder.",
        "Houston, we have a problem" to "Zero matches. Check the spelling or folders.",
        "These aren't the videos you're looking for" to "Move along, and try another search.",
        "Gone, like Thanos snapped it" to "Perfectly balanced. Perfectly empty.",
        "Ctrl+F failed successfully" to "Found: nothing. Congratulations."
    )

    private fun searchEgg(q: String): Pair<String, String>? {
        val s = q.trim().lowercase()
        return when {
            s == "42" -> "The answer to everything" to "…but not to your search. Don't panic."
            "rick" in s || "never gonna" in s -> "You just got rickrolled" to "By an empty result list. Respect."
            "matrix" in s || s == "neo" -> "There is no video" to "Follow the white rabbit to a different search."
            "thanos" in s -> "Perfectly balanced" to "As all things should be. Your result was snapped."
            "sherlock" in s -> "Elementary, my dear" to "…and yet the file remains missing."
            "batman" in s -> "Not the video this search deserved" to "Try again. Gotham needs you."
            "barbie" in s -> "Hi Barbie!" to "Hi… nothing. Try a different search."
            "avenger" in s -> "Avengers, assemble!" to "Except the file. It's a no-show."
            "potter" in s || "hogwarts" in s -> "Accio video!" to "Nothing came flying. Wrong spell?"
            "jedi" in s || "star wars" in s -> "Do or do not" to "There is no result. Only try again."
            else -> null
        }
    }

    fun noResults(query: String, bump: Int): Pair<String, String> {
        if (query.isBlank()) {
            return if (bump % 2 == 0) "It's quiet… too quiet" to "No videos here yet. Check your folders."
            else "Empty like my fridge at 2 AM" to "Add some videos and we'll fill this up."
        }
        searchEgg(query)?.let { if (bump == 0) return it }
        val i = (abs(query.lowercase().hashCode()) + bump) % noResults.size
        val base = noResults[i]
        return base.first to base.second
    }

    private val corrupt = listOf(
        "This file is having an existential crisis" to "Container corrupted. It needs therapy, not a player.",
        "Error 404: Plot not found" to "The file is damaged. Even the spoilers are gone.",
        "Sir, this is a corrupted file" to "Not even Gandalf can make this one pass.",
        "Broken like a promise of \"one more episode\"" to "The file got damaged somewhere along the way.",
        "Houston, we've lost the picture" to "This video didn't survive re-entry.",
        "The file ate itself" to "Looks like it took \"binge\" a bit too literally."
    )
    private val codec = listOf(
        "Codec confusion detected" to "This format speaks Klingon. Try another file.",
        "I don't speak this format" to "Even the software decoder gave up. Impressive.",
        "Lost in translation" to "The player and this file are on different wavelengths."
    )
    private val missing = listOf(
        "Ghosted by the file" to "It moved out without leaving a forwarding address.",
        "File not found. Witness protection?" to "It may have been moved or deleted.",
        "Permission denied. Bouncer says no" to "The system won't let us in."
    )
    private val network = listOf(
        "Wi-Fi took a vacation" to "The stream couldn't connect. Try again later.",
        "Buffering… forever" to "The internet said \"brb\" and never came back."
    )

    fun playbackJoke(error: String, bump: Int): Pair<String, String> {
        if (bump >= 5 && bump % 5 == 0) return "Okay, okay. Stop poking it." to "It's fragile. It's been through a lot."
        val e = error.uppercase()
        val pool = when {
            "DECOD" in e || "UNSUPPORTED" in e || "EXCEEDS" in e -> codec
            "NOT FOUND" in e || "NO PERMISSION" in e || "PERMISSION" in e -> missing
            "NETWORK" in e || "TIMEOUT" in e || "HTTP" in e -> network
            else -> corrupt
        }
        return pool[(abs(error.hashCode()) + bump) % pool.size]
    }
}

fun Modifier.easterEggTaps(taps: Int = 7, onTrigger: () -> Unit): Modifier = composed {
    var count by remember { mutableIntStateOf(0) }
    var last by remember { mutableLongStateOf(0L) }
    this.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
        val now = System.currentTimeMillis()
        count = if (now - last > 1500L) 1 else count + 1
        last = now
        if (count >= taps) {
            count = 0
            onTrigger()
        }
    }
}

fun showSecret(context: android.content.Context, message: String = Humor.secret()) {
    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
}

private fun DrawScope.sparkle(c: Offset, r: Float, color: Color) {
    if (r <= 0.1f) return
    val p = Path().apply {
        moveTo(c.x, c.y - r)
        lineTo(c.x + r * 0.2f, c.y - r * 0.2f)
        lineTo(c.x + r, c.y)
        lineTo(c.x + r * 0.2f, c.y + r * 0.2f)
        lineTo(c.x, c.y + r)
        lineTo(c.x - r * 0.2f, c.y + r * 0.2f)
        lineTo(c.x - r, c.y)
        lineTo(c.x - r * 0.2f, c.y - r * 0.2f)
        close()
    }
    drawPath(p, color)
}

@Composable
fun CoverPlayNoResultsScene(query: String, modifier: Modifier = Modifier.fillMaxWidth()) {
    val s = LocalAppUiSettings.current
    val haptic = rememberCoverPlayHapticFeedback()
    var bump by remember { mutableIntStateOf(0) }
    val copy = remember(query, bump) { Humor.noResults(query, bump) }
    val jump = remember { Animatable(0f) }
    LaunchedEffect(bump) {
        if (bump > 0) {
            jump.snapTo(0f)
            jump.animateTo(1f, tween(650, easing = LinearEasing))
        }
    }

    Column(
        modifier = modifier.padding(horizontal = 28.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CoverPlayDetectiveGhost(
            accent = s.accent,
            highlight = s.highlight,
            secondary = s.secondaryAccent,
            surface = s.palette.containerHighest,
            ink = s.palette.textPrimary,
            jump = jump,
            onTap = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                bump++
            }
        )
        Spacer(Modifier.height(8.dp))
        Crossfade(targetState = copy, animationSpec = tween(350), label = "noResCopy") { (title, sub) ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = s.palette.textPrimary,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = sub,
                    style = MaterialTheme.typography.bodyMedium,
                    color = s.palette.textSecondary,
                    textAlign = TextAlign.Center
                )
                if (query.isNotBlank()) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "\"${query.trim().take(32)}\"",
                        style = MaterialTheme.typography.labelMedium,
                        color = s.highlight,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(s.accent.copy(alpha = 0.14f))
                            .border(1.dp, s.accent.copy(alpha = 0.30f), CircleShape)
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }
            }
        }
    }
}

private fun cookiePath(c: Offset, r: Float, lobes: Int, amp: Float): Path {
    val p = Path()
    val steps = 120
    for (i in 0..steps) {
        val t = (i.toFloat() / steps) * 2f * PI.toFloat()
        val rr = r * (1f + amp * kotlin.math.cos(lobes * t))
        val x = c.x + rr * kotlin.math.cos(t)
        val y = c.y + rr * sin(t)
        if (i == 0) p.moveTo(x, y) else p.lineTo(x, y)
    }
    p.close()
    return p
}

@Composable
private fun CoverPlayDetectiveGhost(
    accent: Color,
    highlight: Color,
    secondary: Color,
    surface: Color,
    ink: Color,
    jump: Animatable<Float, AnimationVector1D>,
    onTap: () -> Unit
) {
    val inf = rememberInfiniteTransition(label = "doodle")
    val spin = inf.animateFloat(0f, 360f, infiniteRepeatable(tween(26_000, easing = LinearEasing)), label = "spin")
    val orbit = inf.animateFloat(0f, 360f, infiniteRepeatable(tween(9_000, easing = LinearEasing)), label = "orbit")
    val bob = inf.animateFloat(0f, 1f, infiniteRepeatable(tween(2200, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "bob")
    val sweep = inf.animateFloat(-1f, 1f, infiniteRepeatable(tween(2600, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "sweep")
    val twinkle = inf.animateFloat(0f, 1f, infiniteRepeatable(tween(1500), RepeatMode.Reverse), label = "tw")

    Box(
        modifier = Modifier
            .size(240.dp, 200.dp)
            .graphicsLayer {
                val j = jump.value
                translationY = -sin(j * PI.toFloat()) * 22.dp.toPx()
                scaleX = 1f + 0.08f * sin(j * 2f * PI.toFloat())
                scaleY = 1f - 0.08f * sin(j * 2f * PI.toFloat())
            }
            .pointerInput(Unit) { detectTapGestures { onTap() } }
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val u = size.height / 200f
            val cx = size.width / 2f
            val cy = 98f * u
            val b = (bob.value - 0.5f)

            drawOval(
                ink.copy(alpha = 0.10f),
                topLeft = Offset(cx - 52f * u * (1f - b * 0.12f), size.height - 14f * u),
                size = Size(104f * u * (1f - b * 0.12f), 10f * u)
            )

            val cookieColor = androidx.compose.ui.graphics.lerp(surface, accent, 0.30f)
            rotate(spin.value, Offset(cx, cy)) {
                drawPath(cookiePath(Offset(cx, cy), 80f * u, 9, 0.055f), cookieColor)
            }
            rotate(-spin.value * 1.4f, Offset(cx, cy)) {
                drawPath(
                    cookiePath(Offset(cx, cy), 58f * u, 6, 0.07f),
                    androidx.compose.ui.graphics.lerp(surface, highlight, 0.28f)
                )
            }

            val oa = Math.toRadians(orbit.value.toDouble()).toFloat()
            val orbitR = 92f * u
            val px = cx + orbitR * kotlin.math.cos(oa)
            val py = cy + orbitR * 0.62f * sin(oa)
            rotate(orbit.value * 2f, Offset(px, py)) {
                drawRoundRect(secondary, Offset(px - 17f * u, py - 7f * u), Size(34f * u, 14f * u), CornerRadius(7f * u))
            }
            val oa2 = oa + PI.toFloat()
            drawCircle(
                highlight,
                7f * u,
                Offset(cx + orbitR * 0.95f * kotlin.math.cos(oa2), cy + orbitR * 0.6f * sin(oa2))
            )

            val tw = twinkle.value
            sparkle(Offset(cx - 104f * u, 46f * u), (6f + 4f * tw) * u, highlight.copy(alpha = 0.45f + 0.5f * tw))
            sparkle(Offset(cx + 102f * u, 36f * u), (5f + 5f * (1f - tw)) * u, secondary.copy(alpha = 0.45f + 0.5f * (1f - tw)))

            val lensR = 34f * u
            val mx = cx + sweep.value * 16f * u
            val my = cy - 6f * u + b * 8f * u
            val dir = Offset(0.7071f, 0.7071f)
            val hStart = Offset(mx + dir.x * lensR, my + dir.y * lensR)
            val hEnd = Offset(mx + dir.x * (lensR + 44f * u), my + dir.y * (lensR + 44f * u))
            drawLine(accent, hStart, hEnd, strokeWidth = 15f * u, cap = StrokeCap.Round)
            drawCircle(surface, lensR, Offset(mx, my))
            drawCircle(accent, lensR, Offset(mx, my), style = Stroke(9f * u))
            drawArc(
                Color.White.copy(alpha = 0.7f), 205f, 45f, false,
                topLeft = Offset(mx - lensR + 11f * u, my - lensR + 11f * u),
                size = Size((lensR - 11f * u) * 2f, (lensR - 11f * u) * 2f),
                style = Stroke(3.5f * u, cap = StrokeCap.Round)
            )
            val qr = 9f * u
            val qc = Offset(mx, my - 5f * u)
            val q = Path().apply {
                arcTo(Rect(qc.x - qr, qc.y - qr, qc.x + qr, qc.y + qr), 180f, 270f, true)
                quadraticTo(qc.x, qc.y + qr + 3f * u, qc.x, qc.y + qr + 8f * u)
            }
            drawPath(q, ink, style = Stroke(4.5f * u, cap = StrokeCap.Round))
            drawCircle(ink, 3f * u, Offset(qc.x, qc.y + qr + 17f * u))
        }
    }
}

@Composable
fun CoverPlayCorruptVideoScene(
    errorText: String,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val s = LocalAppUiSettings.current
    val haptic = rememberCoverPlayHapticFeedback()
    var bump by remember { mutableIntStateOf(0) }
    val joke = remember(errorText, bump) { Humor.playbackJoke(errorText, bump) }

    Box(
        modifier = modifier
            .zIndex(50f)
            .background(Color(0xF2080810))
            .pointerInput(Unit) { detectTapGestures { } },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 28.dp).widthIn(max = 460.dp)
        ) {
            CoverPlayBrokenReel(
                accent = s.accent,
                highlight = s.highlight,
                onTap = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    bump++
                }
            )
            Spacer(Modifier.height(6.dp))
            Crossfade(targetState = joke, animationSpec = tween(350), label = "corruptCopy") { (title, sub) ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = sub,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.68f),
                        textAlign = TextAlign.Center
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            Text(
                text = errorText.take(64),
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.45f),
                textAlign = TextAlign.Center,
                maxLines = 2,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White.copy(alpha = 0.06f))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            )
            Spacer(Modifier.height(22.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Try again",
                    color = s.accent.onFill(),
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(s.accent)
                        .clickable { onRetry() }
                        .padding(horizontal = 26.dp, vertical = 11.dp)
                )
                Text(
                    text = "Go back",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clip(CircleShape)
                        .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                        .clickable { onBack() }
                        .padding(horizontal = 26.dp, vertical = 11.dp)
                )
            }
        }
    }
}

@Composable
private fun CoverPlayBrokenReel(accent: Color, highlight: Color, onTap: () -> Unit) {
    val inf = rememberInfiniteTransition(label = "reel")
    val spin = inf.animateFloat(
        0f, 360f,
        infiniteRepeatable(keyframes {
            durationMillis = 3200
            0f at 0
            70f at 400 using FastOutSlowInEasing
            70f at 900
            150f at 1300 using FastOutSlowInEasing
            128f at 1500
            128f at 2100
            360f at 3200 using FastOutSlowInEasing
        }),
        label = "spin"
    )
    val wobble = inf.animateFloat(-3f, 3f, infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "wob")
    val wave = inf.animateFloat(0f, (2 * PI).toFloat(), infiniteRepeatable(tween(2200, easing = LinearEasing)), label = "wave")
    val smoke = inf.animateFloat(0f, 1f, infiniteRepeatable(tween(2400, easing = LinearEasing)), label = "smoke")

    Canvas(
        Modifier
            .size(240.dp, 210.dp)
            .pointerInput(Unit) { detectTapGestures { onTap() } }
    ) {
        val u = size.height / 210f
        val c = Offset(size.width / 2f, 92f * u)
        val r = 58f * u
        val red = Color(0xFFFF4D5E)

        drawCircle(
            brush = Brush.radialGradient(listOf(red.copy(alpha = 0.22f), Color.Transparent), center = c, radius = 130f * u),
            radius = 130f * u, center = c
        )

        val strip = Path().apply {
            val sx = c.x - 8f * u
            val sy = c.y + r - 4f * u
            moveTo(sx, sy)
            cubicTo(
                sx + 6f * u, sy + 30f * u,
                sx + 56f * u + sin(wave.value) * 8f * u, sy + 20f * u,
                sx + 70f * u + sin(wave.value + 1f) * 10f * u, sy + 58f * u
            )
        }
        drawPath(strip, Color(0xFF2C2C3A), style = Stroke(22f * u, cap = StrokeCap.Butt))
        drawPath(strip, Color.White.copy(alpha = 0.45f), style = Stroke(12f * u, pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f * u, 9f * u))))

        rotate(wobble.value, c) {
            drawCircle(Brush.radialGradient(listOf(Color(0xFF3A3A4C), Color(0xFF1A1A24)), center = c, radius = r), r, c)
            drawCircle(accent.copy(alpha = 0.85f), r, c, style = Stroke(5f * u))
            rotate(spin.value, c) {
                for (i in 0 until 6) {
                    val ang = (i * 60f) * PI.toFloat() / 180f
                    drawCircle(Color(0xFF07070C), 12f * u, Offset(c.x + kotlin.math.cos(ang) * 34f * u, c.y + sin(ang) * 34f * u))
                }
            }
            drawCircle(Color(0xFF2A2A38), 22f * u, c)
            drawCircle(highlight.copy(alpha = 0.6f), 22f * u, c, style = Stroke(1.5f * u))
            for (sx in listOf(-8f, 8f)) {
                val ex = c.x + sx * u
                val ey = c.y - 4f * u
                drawLine(Color.White, Offset(ex - 3.5f * u, ey - 3.5f * u), Offset(ex + 3.5f * u, ey + 3.5f * u), 2.2f * u, StrokeCap.Round)
                drawLine(Color.White, Offset(ex - 3.5f * u, ey + 3.5f * u), Offset(ex + 3.5f * u, ey - 3.5f * u), 2.2f * u, StrokeCap.Round)
            }
            val mouth = Path().apply {
                moveTo(c.x - 8f * u, c.y + 10f * u)
                for (k in 1..4) {
                    lineTo(c.x - 8f * u + k * 4f * u, c.y + (if (k % 2 == 0) 10f else 7f) * u)
                }
            }
            drawPath(mouth, Color.White, style = Stroke(2f * u, cap = StrokeCap.Round))

            val crack = Path().apply {
                moveTo(c.x + 30f * u, c.y - 52f * u)
                lineTo(c.x + 22f * u, c.y - 34f * u)
                lineTo(c.x + 33f * u, c.y - 24f * u)
                lineTo(c.x + 20f * u, c.y - 10f * u)
            }
            drawPath(crack, red, style = Stroke(3f * u, cap = StrokeCap.Round))
            drawPath(crack, Color.White.copy(alpha = 0.7f), style = Stroke(1f * u, cap = StrokeCap.Round))

            rotate(-32f, Offset(c.x - 30f * u, c.y + 30f * u)) {
                drawRoundRect(Color(0xFFF2D3AE), Offset(c.x - 56f * u, c.y + 22f * u), Size(52f * u, 16f * u), CornerRadius(8f * u))
                for (k in 0 until 3) for (j in 0 until 2) {
                    drawCircle(Color(0xFFD9B386), 1.2f * u, Offset(c.x - 38f * u + k * 9f * u, c.y + 27f * u + j * 6f * u))
                }
            }
        }

        for (i in 0 until 3) {
            val p = (smoke.value + i / 3f) % 1f
            drawCircle(
                Color.White.copy(alpha = (1f - p) * 0.22f),
                radius = (5f + 10f * p) * u,
                center = Offset(c.x - 26f * u + i * 26f * u + sin(p * 6f + i) * 6f * u, c.y - r - 4f * u - p * 42f * u)
            )
        }
    }
}

object Quips {
    private val seed = Random.nextInt(1000)

    private fun pick(list: List<String>, key: String, bump: Int = 0): String =
        list[(abs(key.lowercase().hashCode()) + seed + bump) % list.size]

    private val generic = listOf(
        "Just a friendly dialog. Be nice to it.",
        "Pop-up with personality.",
        "Plot twist: a dialog appears.",
        "No cameras were harmed in the making of this window.",
        "Fun fact: you're reading the fine print."
    )

    fun dialog(title: String): String {
        val t = title.lowercase()
        val list = when {
            "rename" in t -> listOf("Naming things is hard. Ask any programmer.", "Choose wisely. Future you will search for this.", "Rosebud? Final answer?")
            "delete" in t -> listOf("Thanos-level decision. No take-backs.", "Hasta la vista, video.", "Gone faster than your free trial.")
            "detail" in t || "info" in t -> listOf("Everything except the plot.", "Elementary, my dear Watson.", "The nerdy bits, for the cinephile.")
            "move" in t -> listOf("Moving day! No boxes required.", "Real estate, but for MP4s.", "Pack your bags, little file.")
            "new playlist" in t -> listOf("Name it something cool. \"Untitled 7\" doesn't count.", "Every legend starts with an empty list.")
            "playlist" in t -> listOf("Your mixtape, but make it HD.", "Curating like a boss.", "Another one for the collection.")
            "clear" in t -> listOf("Burn the evidence. We won't tell.", "Memory wipe, Men in Black style.", "What happens in CoverPlay… stays nowhere now.")
            "error" in t || "oops" in t -> listOf("Even Batman fumbles the Batarang sometimes.", "Houston, we have a minor problem.", "Don't panic. Bring a towel.")
            "speed" in t -> listOf("Gotta go fast. Or slow. We don't judge.", "Time is relative. Ask Einstein.", "Slow-mo for dramatic effect.")
            "aspect" in t -> listOf("Fit, fill or zoom. Marie Kondo picks fit.", "Shape up. Your video, your rules.")
            "retention" in t || "history" in t -> listOf("Short-term memory, long-term vibes.", "How long should we remember you? Forever, ideally.")
            "seek" in t || "interval" in t -> listOf("Skip the filler. Never skip the ending.", "Fast-forward through the boring bits.")
            "subtitle" in t || "caption" in t -> listOf("Because \"what did they say?\" is not a plot.", "Reading is just listening with extra steps.")
            "audio" in t || "track" in t -> listOf("Choose your fighter. Er, language.", "Hear, hear!")
            "stream" in t || "url" in t || "link" in t -> listOf("Paste a link. Pray to the Wi-Fi gods.", "Streaming: because storage is a myth.")
            "sort" in t || "filter" in t || "layout" in t || "view" in t -> listOf("Order from chaos. Your library, your rules.", "Sort it like it's hot.")
            "theme" in t || "appearance" in t -> listOf("Dress your player. Drip responsibly.")
            "file action" in t -> listOf("Pick a power. Any power.", "Choose your own adventure, file edition.")
            "control" in t || "more" in t || "option" in t -> listOf("The control room. Please don't press the red button.", "Behind the three dots lives the good stuff.")
            "folder" in t || "browse" in t || "up next" in t -> listOf("Ctrl+F, but with feelings.", "Choose your next adventure.")
            "online" in t || "search" in t -> listOf("Searching the internet so you don't have to.", "Seek and ye shall find. Probably.")
            else -> generic
        }
        return pick(list, title)
    }

    fun settingsPage(page: SettingsPage): String = when (page) {
        SettingsPage.MAIN -> "Tweak responsibly. With great settings comes great screen time."
        SettingsPage.APPEARANCE -> "Dress your player. Drip responsibly."
        SettingsPage.PLAYBACK -> "Under the hood: hamsters, coffee and codecs."
        SettingsPage.GESTURES -> "Swipe right on good settings."
        SettingsPage.PLAYER_CUSTOMIZE -> "Your player, your rules. Move things around."
        SettingsPage.PRIVACY_HISTORY -> "What happens in CoverPlay stays in CoverPlay."
        SettingsPage.ABOUT_DEVELOPER -> "Made with caffeine, curiosity and a suspicious amount of Kotlin."
        SettingsPage.ABOUT_APP -> "Version 3.0.5: now with 12% more easter eggs. (Unverified.)"
    }

    fun empty(title: String): String = pick(
        when {
            "folder" in title.lowercase() -> listOf("Dust bunnies are not videos.", "Searching high and low. Mostly low.")
            "history" in title.lowercase() -> listOf("A blank slate. Press play and make history.", "Nothing here. Your secrets are safe.")
            "playlist" in title.lowercase() -> listOf("An empty list is just potential.", "Every great mixtape started blank.")
            else -> listOf("Nothing to see here. Move along.", "The silence is deafening.")
        }, title
    )

    fun gesture(text: String): String? {
        val t = text.trim().lowercase()
        return when {
            t == "2x" -> "Gotta go fast."
            t == "volume boost" -> "This one goes to 11."
            t == "muted" -> "Silence of the Lambs."
            t == "paused" -> "Intermission. Popcorn break."
            t == "replay" -> "Encore!"
            t == "no next video" -> "The end. Or is it?"
            t == "no previous video" -> "Nothing behind you. Look forward."
            t == "shuffle on" -> "Chaos mode: engaged."
            t == "repeat one" -> "Again! Again!"
            t == "zoom: reset" -> "Back to reality."
            t == "lock landscape" -> "Widescreen or bust."
            t == "background play on" -> "Ears only. Eyes elsewhere."
            t.startsWith("speed:") -> {
                val v = t.removePrefix("speed:").trim().removeSuffix("x").toFloatOrNull()
                when {
                    v == null -> null
                    v < 0.8f -> "Slow-mo, Matrix style."
                    v >= 2f -> "Chipmunk mode."
                    else -> null
                }
            }
            else -> null
        }
    }

    fun usage(ms: Long): String = when {
        ms <= 0L -> "Zero minutes. Suspiciously healthy."
        ms < 30 * 60_000L -> "Light viewing. The sun thanks you."
        ms < 2 * 3_600_000L -> "Solid session. Popcorn well spent."
        ms < 5 * 3_600_000L -> "Dedicated cinephile detected."
        else -> "Touch grass. We'll still be here."
    }

    private val updates = listOf(
        "CoverPlay v3.0.5 is up to date. Faster than your Wi-Fi.",
        "Latest version confirmed. Nothing to download, go watch something.",
        "You're current. Unlike your watchlist."
    )
    private var updateIdx = Random.nextInt(updates.size)
    fun update(): String = updates[updateIdx++ % updates.size]

    fun tabSecret(title: String): String = when (title.lowercase()) {
        "folders" -> "📁 Secret folder found. It's empty. Plot twist."
        "playlists" -> "🎵 The only list where \"shuffle\" is a lifestyle."
        "settings" -> "⚙️ You tweaked the tweaker. Tweak-ception."
        "history", "recents" -> "🕰️ Back to the Future? Great Scott!"
        "preferences" -> "🛠️ Developer mode unlocked… just kidding."
        else -> Humor.secret()
    }
}

@Composable
fun CoverPlayQuipLine(
    text: String?,
    modifier: Modifier = Modifier,
    color: Color = LocalAppUiSettings.current.palette.textTertiary,
    align: TextAlign = TextAlign.Start
) {
    if (text.isNullOrBlank()) return
    Text(
        text = text,
        modifier = modifier,
        color = color,
        textAlign = align,
        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium)
    )
}
