package com.example.myapplication

import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.myapplication.ui.theme.GlassDialogBackdrop
import com.example.myapplication.ui.theme.GlassSurface
import com.example.myapplication.ui.theme.GlowLevel
import com.example.myapplication.ui.theme.LocalAppUiSettings
import com.example.myapplication.ui.theme.LocalGlassBackdrop
import com.example.myapplication.ui.theme.LocalGlassHaze
import com.example.myapplication.ui.theme.PremiumButton
import com.example.myapplication.ui.theme.cinematicOverlay
import com.example.myapplication.ui.theme.premiumGlow
import com.example.myapplication.ui.theme.rememberWindowBlurActive
import com.example.myapplication.ui.theme.windowFrost
import kotlin.math.PI
import kotlin.math.sin

// ═════════════════════════════════════════════════════════════════════════════
//  TUTORIAL — two guided tours: one for the app, one for the video player.
//
//  • Shown automatically the first time (app tour on first launch, player tour
//    the first time a video opens).
//  • Replayable any time from Settings → About App.
//  • Swipe, tap the dots, or use Next / Back. Back button or Skip dismisses it.
// ═════════════════════════════════════════════════════════════════════════════

object TutorialPrefs {
    private const val PREFS = "coverplay_tutorial"
    private const val KEY_APP = "app_tour_seen_v1"
    private const val KEY_PLAYER = "player_tour_seen_v1"

    private fun p(c: Context) = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun appSeen(c: Context): Boolean = p(c).getBoolean(KEY_APP, false)
    fun playerSeen(c: Context): Boolean = p(c).getBoolean(KEY_PLAYER, false)
    fun markApp(c: Context) = p(c).edit().putBoolean(KEY_APP, true).apply()
    fun markPlayer(c: Context) = p(c).edit().putBoolean(KEY_PLAYER, true).apply()
    fun resetApp(c: Context) = p(c).edit().putBoolean(KEY_APP, false).apply()
    fun resetPlayer(c: Context) = p(c).edit().putBoolean(KEY_PLAYER, false).apply()
}

private enum class Art { HERO, NAV, LIST, SWIPE, DOUBLE_TAP, PINCH, HOLD, CONTROLS }

private data class TourStep(
    val emoji: String,
    val tag: String,
    val title: String,
    val body: String,
    val quip: String,
    val art: Art
)

private object TourContent {
    val app = listOf(
        TourStep(
            "🎬", "WELCOME", "Welcome to CoverPlay",
            "A fast, private, beautiful player for the videos already on your phone. No accounts, no ads, no drama.",
            "Popcorn not included. Yet.", Art.HERO
        ),
        TourStep(
            "🧭", "NAVIGATE", "Four tabs. Zero confusion.",
            "Home, Folders, Playlists and Settings live in the bar at the bottom. It shrinks out of your way while you scroll and grows back when you scroll up.",
            "Even Frodo found the way with fewer maps.", Art.NAV
        ),
        TourStep(
            "🏠", "HOME", "Pick up right where you left off",
            "Home keeps your recent videos with resume progress, quick filters, and a Stream-a-link button for playing straight from a URL. Tap the magnifier to search anything.",
            "Winter isn't coming. Your watchlist is.", Art.LIST
        ),
        TourStep(
            "📁", "FOLDERS", "Every folder, found automatically",
            "CoverPlay scans your storage and groups videos by folder. Open one, then use the sort and layout button to switch between list and grid.",
            "Marie Kondo would approve.", Art.LIST
        ),
        TourStep(
            "✋", "LONG-PRESS", "Long-press for superpowers",
            "Press and hold any video to rename it, move it, add it to a playlist or delete it. Tap a row in the list view to peek at a live preview first.",
            "With great power comes great responsibility.", Art.LIST
        ),
        TourStep(
            "🎵", "PLAYLISTS", "Playlists, your way",
            "Create playlists, drop videos in from the long-press menu, and play them back to back. Your library, your order.",
            "Your mixtape, but make it HD.", Art.LIST
        ),
        TourStep(
            "🎨", "PERSONALIZE", "Make it unmistakably yours",
            "Pick one of five themes, light or dark, and let the launcher icon follow along. Playback speed, seek length, gestures, history and more are all in Settings.",
            "Iris you'd pick a theme already.", Art.NAV
        ),
        TourStep(
            "🤫", "SECRETS", "Psst. There are secrets.",
            "Puns, pop-culture nods and hidden easter eggs are scattered around the app. Tap titles a few times, poke the empty screens, and search for something famous.",
            "We said nothing. You saw nothing.", Art.HERO
        )
    )

    val player = listOf(
        TourStep(
            "🍿", "PLAYER", "Welcome to the theatre",
            "Tap anywhere on the video to show or hide the controls. Everything below works with your fingers, no menus needed.",
            "Lights. Camera. Fingers.", Art.HERO
        ),
        TourStep(
            "☀️", "SWIPE", "Slide for light and sound",
            "Swipe up or down on the left half for brightness. Swipe on the right half for volume, which goes all the way to 200% boost.",
            "This one goes to 11. Actually, 200.", Art.SWIPE
        ),
        TourStep(
            "⏩", "DOUBLE-TAP", "Double-tap to skip",
            "Double-tap the left side to jump back, the right side to jump ahead. Keep tapping to stack skips. You can change the interval in Settings.",
            "Skip the boring bit. We won't tell.", Art.DOUBLE_TAP
        ),
        TourStep(
            "🔍", "PINCH", "Pinch to zoom",
            "Pinch with two fingers to zoom into the picture and drag to pan. Double-tap while zoomed to snap back to normal.",
            "Zoom. Enhance. Enhance.", Art.PINCH
        ),
        TourStep(
            "🚀", "HOLD", "Hold for double speed",
            "Press and hold anywhere to watch at 2× while your finger is down. Let go and normal speed returns instantly.",
            "Gotta go fast.", Art.HOLD
        ),
        TourStep(
            "🎛️", "CONTROLS", "Everything else is one tap away",
            "Lock the screen, rotate, keep audio playing in the background, switch audio tracks and subtitles, change speed or aspect ratio, and see media info from the controls and the more menu.",
            "Behind the three dots lives the good stuff.", Art.CONTROLS
        ),
        TourStep(
            "▶️", "RESUME", "It remembers for you",
            "Close a video and CoverPlay saves your spot. Next time, it offers to resume. Autoplay and loop can be switched on in Settings.",
            "Just one more episode. Sure.", Art.HERO
        )
    )
}

@Composable
fun AppTutorial(onFinish: () -> Unit) = TourDialog(TourContent.app, "Let's go", onFinish)

@Composable
fun PlayerTutorial(onFinish: () -> Unit) = TourDialog(TourContent.player, "Start watching", onFinish)

// ─────────────────────────────────────────────────────────────────────────────
//  Dialog shell + pager
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TourDialog(steps: List<TourStep>, finishLabel: String, onFinish: () -> Unit) {
    val ui = LocalAppUiSettings.current
    val accent = ui.accent
    val haptic = LocalHapticFeedback.current
    val cfg = LocalConfiguration.current
    val landscape = cfg.screenWidthDp > cfg.screenHeightDp
    val blurActive = rememberWindowBlurActive()

    var index by remember { mutableIntStateOf(0) }
    var dir by remember { mutableIntStateOf(1) }
    var closing by remember { mutableStateOf(false) }
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { shown = true }

    val appear by animateFloatAsState(
        targetValue = if (shown && !closing) 1f else 0f,
        animationSpec = tween(if (closing) 240 else 460, easing = FastOutSlowInEasing),
        label = "tourAppear",
        finishedListener = { if (closing) onFinish() }
    )

    fun go(to: Int) {
        if (to !in steps.indices || to == index) return
        dir = if (to > index) 1 else -1
        index = to
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }

    fun finish() {
        if (!closing) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            closing = true
        }
    }

    Dialog(
        onDismissRequest = { finish() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
            dismissOnClickOutside = false
        )
    ) {
        GlassDialogBackdrop(blurRadius = 44, dim = if (blurActive) 0.46f else 0.70f)
        CompositionLocalProvider(LocalGlassHaze provides null, LocalGlassBackdrop provides null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = appear }
                    .background(
                        Brush.radialGradient(
                            listOf(accent.copy(alpha = 0.20f), Color.Black.copy(alpha = 0.42f))
                        )
                    )
                    .pointerInput(Unit) { detectTapGestures { } }
                    .pointerInput(index) {
                        var total = 0f
                        detectHorizontalDragGestures(
                            onDragStart = { total = 0f },
                            onDragEnd = {
                                if (total < -90f) go(index + 1) else if (total > 90f) go(index - 1)
                            },
                            onHorizontalDrag = { _, d -> total += d }
                        )
                    }
                    .padding(horizontal = 18.dp, vertical = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                val shape = RoundedCornerShape(32.dp)
                GlassSurface(
                    modifier = Modifier
                        .widthIn(max = if (landscape) 660.dp else 420.dp)
                        .fillMaxWidth()
                        .graphicsLayer {
                            val sc = 0.94f + 0.06f * appear
                            scaleX = sc; scaleY = sc
                            translationY = (1f - appear) * 40f
                        }
                        .premiumGlow(accent, GlowLevel.Subtle, appear, corner = 32.dp),
                    shape = shape,
                    strong = true,
                    tint = Color.Transparent,
                    pressedScale = 1f
                ) {
                    Box(Modifier.matchParentSize().windowFrost(true, accent, blurActive))
                    Box(Modifier.matchParentSize().cinematicOverlay(accent, ui.highlight, true))

                    Column(
                        Modifier
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 22.dp, vertical = 20.dp)
                    ) {
                        val last = index == steps.lastIndex

                        // header: step counter + skip
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "${index + 1} / ${steps.size}",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold, letterSpacing = 1.6.sp
                                ),
                                color = accent
                            )
                            Spacer(Modifier.weight(1f))
                            if (!last) {
                                Text(
                                    "Skip",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                    color = Color.White.copy(alpha = 0.62f),
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .clickable { finish() }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                        Spacer(Modifier.height(10.dp))

                        AnimatedContent(
                            targetState = index,
                            transitionSpec = {
                                val d = dir
                                val emph = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)
                                (slideInHorizontally(tween(420, easing = emph)) { it * d / 4 } +
                                        fadeIn(tween(320, delayMillis = 60))) togetherWith
                                        (slideOutHorizontally(tween(260)) { -it * d / 4 } + fadeOut(tween(160)))
                            },
                            label = "tourStep"
                        ) { i ->
                            val step = steps[i]
                            if (landscape) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    ArtStage(step, Modifier.width(210.dp).height(170.dp))
                                    Spacer(Modifier.width(20.dp))
                                    StepText(step, Modifier.weight(1f))
                                }
                            } else {
                                Column {
                                    ArtStage(step, Modifier.fillMaxWidth().height(160.dp))
                                    Spacer(Modifier.height(18.dp))
                                    StepText(step, Modifier.fillMaxWidth())
                                }
                            }
                        }

                        Spacer(Modifier.height(18.dp))

                        // footer: dots + back / next
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                steps.indices.forEach { i ->
                                    val w by animateDpAsState(
                                        if (i == index) 22.dp else 6.dp,
                                        spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMedium),
                                        label = "dot$i"
                                    )
                                    Box(
                                        Modifier
                                            .height(6.dp)
                                            .width(w)
                                            .clip(CircleShape)
                                            .background(if (i == index) accent else Color.White.copy(alpha = 0.22f))
                                            .clickable(
                                                interactionSource = remember { MutableInteractionSource() },
                                                indication = null
                                            ) { go(i) }
                                    )
                                }
                            }
                            if (index > 0) {
                                Text(
                                    "Back",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                    color = Color.White.copy(alpha = 0.72f),
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .clickable { go(index - 1) }
                                        .padding(horizontal = 14.dp, vertical = 10.dp)
                                )
                            }
                            PremiumButton(
                                text = if (last) finishLabel else "Next",
                                onClick = { if (last) finish() else go(index + 1) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StepText(step: TourStep, modifier: Modifier = Modifier) {
    val ui = LocalAppUiSettings.current
    val accent = ui.accent
    Column(modifier) {
        Text(
            step.tag,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 2.sp),
            color = ui.highlight
        )
        Spacer(Modifier.height(6.dp))
        Text(
            step.title,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
            color = Color.White
        )
        Spacer(Modifier.height(8.dp))
        Text(
            step.body,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.74f)
        )
        Spacer(Modifier.height(14.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(accent.copy(alpha = 0.14f))
                .border(1.dp, accent.copy(alpha = 0.30f), RoundedCornerShape(16.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text("✨", fontSize = 13.sp)
            Spacer(Modifier.width(8.dp))
            Text(
                step.quip,
                style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic, fontWeight = FontWeight.Medium),
                color = ui.highlight
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Animated illustrations — drawn, themed, and in motion
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ArtStage(step: TourStep, modifier: Modifier = Modifier) {
    val ui = LocalAppUiSettings.current
    val accent = ui.accent
    val hi = ui.highlight
    val sec = ui.secondaryAccent
    val shape = RoundedCornerShape(24.dp)

    val inf = rememberInfiniteTransition(label = "tourArt")
    val t by inf.animateFloat(0f, 1f, infiniteRepeatable(tween(2600, easing = LinearEasing)), label = "t")
    val pulse by inf.animateFloat(
        0.93f, 1.07f,
        infiniteRepeatable(tween(1500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse"
    )
    val four by inf.animateFloat(0f, 4f, infiniteRepeatable(tween(5200, easing = LinearEasing)), label = "four")

    Box(
        modifier = modifier
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(accent.copy(alpha = 0.24f), sec.copy(alpha = 0.10f), Color.Transparent)
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.10f), shape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val c = center
            val minD = size.minDimension
            val tau = (2 * PI).toFloat()
            when (step.art) {
                Art.HERO -> {
                    val base = minD * 0.20f
                    for (i in 0..2) {
                        val p = (t + i / 3f) % 1f
                        drawCircle(
                            accent.copy(alpha = (1f - p) * 0.45f),
                            base + p * minD * 0.34f, c, style = Stroke(2.dp.toPx())
                        )
                    }
                    drawCircle(
                        Brush.radialGradient(listOf(hi.copy(alpha = 0.95f), accent), center = c, radius = base * 1.5f),
                        base * pulse, c
                    )
                }

                Art.NAV -> {
                    val bw = size.width * 0.78f
                    val bh = 46.dp.toPx()
                    val left = (size.width - bw) / 2f
                    val top = size.height / 2f - bh / 2f - 8.dp.toPx()
                    drawRoundRect(Color.White.copy(alpha = 0.08f), Offset(left, top), Size(bw, bh), CornerRadius(bh / 2f))
                    drawRoundRect(
                        Color.White.copy(alpha = 0.18f), Offset(left, top), Size(bw, bh),
                        CornerRadius(bh / 2f), style = Stroke(1.dp.toPx())
                    )
                    val active = four.toInt().coerceIn(0, 3)
                    for (i in 0..3) {
                        val cx = left + bw * (i + 0.5f) / 4f
                        val cy = top + bh / 2f
                        if (i == active) {
                            drawCircle(accent.copy(alpha = 0.30f), 21.dp.toPx() * pulse, Offset(cx, cy))
                            drawCircle(accent, 9.dp.toPx(), Offset(cx, cy))
                        } else {
                            drawCircle(Color.White.copy(alpha = 0.34f), 5.dp.toPx(), Offset(cx, cy))
                        }
                    }
                }

                Art.LIST -> {
                    val rowH = 34.dp.toPx()
                    val gap = 10.dp.toPx()
                    val total = rowH * 3 + gap * 2
                    val top0 = (size.height - total) / 2f
                    val w = size.width * 0.80f
                    val left = (size.width - w) / 2f
                    val hot = four.toInt() % 3
                    for (i in 0..2) {
                        val y = top0 + i * (rowH + gap)
                        val r = CornerRadius(12.dp.toPx())
                        drawRoundRect(Color.White.copy(alpha = 0.07f), Offset(left, y), Size(w, rowH), r)
                        drawRoundRect(
                            accent.copy(alpha = if (i == hot) 0.85f else 0.40f),
                            Offset(left + 6.dp.toPx(), y + 5.dp.toPx()),
                            Size(44.dp.toPx(), rowH - 10.dp.toPx()), CornerRadius(8.dp.toPx())
                        )
                        drawRoundRect(
                            Color.White.copy(alpha = 0.32f),
                            Offset(left + 58.dp.toPx(), y + 9.dp.toPx()),
                            Size(w * 0.46f, 5.dp.toPx()), CornerRadius(3.dp.toPx())
                        )
                        drawRoundRect(
                            Color.White.copy(alpha = 0.16f),
                            Offset(left + 58.dp.toPx(), y + 19.dp.toPx()),
                            Size(w * 0.28f, 4.dp.toPx()), CornerRadius(3.dp.toPx())
                        )
                        if (i == hot) {
                            drawRoundRect(
                                accent.copy(alpha = 0.9f), Offset(left, y), Size(w, rowH), r,
                                style = Stroke(1.5.dp.toPx())
                            )
                        }
                    }
                }

                Art.SWIPE -> {
                    drawLine(
                        Color.White.copy(alpha = 0.18f), Offset(c.x, 14.dp.toPx()), Offset(c.x, size.height - 14.dp.toPx()),
                        strokeWidth = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                    )
                    val ph = sin(t * tau)
                    val trackTop = size.height * 0.34f
                    val trackBot = size.height * 0.84f
                    for (side in 0..1) {
                        val x = size.width * (0.25f + 0.5f * side)
                        drawLine(
                            Color.White.copy(alpha = 0.20f), Offset(x, trackTop), Offset(x, trackBot),
                            strokeWidth = 4.dp.toPx(), cap = StrokeCap.Round
                        )
                        val mid = (trackTop + trackBot) / 2f
                        val y = mid + ph * (trackBot - trackTop) / 2f * (if (side == 0) -1f else 1f)
                        drawCircle(accent.copy(alpha = 0.32f), 15.dp.toPx(), Offset(x, y))
                        drawCircle(accent, 7.dp.toPx(), Offset(x, y))
                    }
                }

                Art.DOUBLE_TAP -> {
                    drawLine(
                        Color.White.copy(alpha = 0.18f), Offset(c.x, 14.dp.toPx()), Offset(c.x, size.height - 14.dp.toPx()),
                        strokeWidth = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                    )
                    val leftActive = t < 0.5f
                    val p = if (leftActive) t * 2f else (t - 0.5f) * 2f
                    val cx = size.width * (if (leftActive) 0.25f else 0.75f)
                    drawCircle(accent.copy(alpha = (1f - p) * 0.55f), 10.dp.toPx() + p * 38.dp.toPx(), Offset(cx, c.y), style = Stroke(3.dp.toPx()))
                    drawCircle(accent.copy(alpha = (1f - p) * 0.28f), 6.dp.toPx() + p * 22.dp.toPx(), Offset(cx, c.y))
                    drawCircle(accent, 7.dp.toPx(), Offset(cx, c.y))
                }

                Art.PINCH -> {
                    val ph = (sin(t * tau) + 1f) / 2f
                    val d = 16.dp.toPx() + 42.dp.toPx() * ph
                    val ux = 0.8f
                    val uy = -0.6f
                    val p1 = Offset(c.x + ux * d, c.y + uy * d)
                    val p2 = Offset(c.x - ux * d, c.y - uy * d)
                    drawLine(
                        accent.copy(alpha = 0.45f), p1, p2, strokeWidth = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f))
                    )
                    for (p in listOf(p1, p2)) {
                        drawCircle(accent.copy(alpha = 0.30f), 16.dp.toPx(), p)
                        drawCircle(accent, 7.dp.toPx(), p)
                    }
                }

                Art.HOLD -> {
                    val r = minD * 0.30f
                    drawCircle(Color.White.copy(alpha = 0.10f), r, c, style = Stroke(6.dp.toPx()))
                    drawArc(
                        accent, -90f, 360f * t, false,
                        topLeft = Offset(c.x - r, c.y - r), size = Size(r * 2f, r * 2f),
                        style = Stroke(6.dp.toPx(), cap = StrokeCap.Round)
                    )
                    drawCircle(accent.copy(alpha = 0.10f + 0.20f * t), r - 8.dp.toPx(), c)
                }

                Art.CONTROLS -> {
                    drawCircle(accent.copy(alpha = 0.10f), minD * 0.42f * pulse, c)
                }
            }
        }

        // glyph / label overlays that sit on top of the drawing
        when (step.art) {
            Art.HERO -> Text(step.emoji, fontSize = 38.sp, modifier = Modifier.graphicsLayer {
                scaleX = pulse; scaleY = pulse
            })

            Art.NAV -> Row(
                Modifier.fillMaxWidth(0.78f).align(Alignment.Center).offset(y = 28.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("Home", "Folders", "Playlists", "Settings").forEach {
                    Text(
                        it, modifier = Modifier.weight(1f), textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.SemiBold),
                        color = Color.White.copy(alpha = 0.62f), maxLines = 1
                    )
                }
            }

            Art.SWIPE -> Row(Modifier.fillMaxSize().padding(top = 12.dp)) {
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("☀️", fontSize = 24.sp)
                    Text("Brightness", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.65f))
                }
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🔊", fontSize = 24.sp)
                    Text("Volume", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.65f))
                }
            }

            Art.DOUBLE_TAP -> Row(Modifier.fillMaxSize().padding(bottom = 14.dp), verticalAlignment = Alignment.Bottom) {
                Text("⏪  Back", Modifier.weight(1f), textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.7f))
                Text("Ahead  ⏩", Modifier.weight(1f), textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.7f))
            }

            Art.PINCH -> Text("🔍", fontSize = 22.sp, modifier = Modifier.align(Alignment.TopEnd).padding(14.dp))

            Art.HOLD -> Text(
                "2×", color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Black,
                modifier = Modifier.graphicsLayer { scaleX = pulse; scaleY = pulse }
            )

            Art.CONTROLS -> Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf("🔒", "🎧", "💬", "⚙️").forEachIndexed { i, g ->
                    val on = four.toInt().coerceIn(0, 3) == i
                    Box(
                        Modifier
                            .size(46.dp)
                            .graphicsLayer { val s = if (on) 1.12f else 1f; scaleX = s; scaleY = s }
                            .clip(CircleShape)
                            .background(if (on) accent.copy(alpha = 0.55f) else Color.White.copy(alpha = 0.09f))
                            .border(1.dp, Color.White.copy(alpha = if (on) 0.35f else 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) { Text(g, fontSize = 20.sp) }
                }
            }

            else -> Unit
        }
    }
}
