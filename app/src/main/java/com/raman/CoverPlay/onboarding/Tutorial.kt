package com.raman.CoverPlay.onboarding

import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.raman.CoverPlay.R
import com.raman.CoverPlay.ui.theme.CoverPlayAction
import com.raman.CoverPlay.ui.theme.CoverPlayButtonGroup
import com.raman.CoverPlay.ui.theme.CoverPlayDialogBackdrop
import com.raman.CoverPlay.ui.theme.CoverPlayMaterialShapes
import com.raman.CoverPlay.ui.theme.CoverPlayRotatingShape
import com.raman.CoverPlay.ui.theme.CoverPlaySprings
import com.raman.CoverPlay.ui.theme.LocalAppUiSettings
import com.raman.CoverPlay.ui.theme.LocalGlassBackdrop
import com.raman.CoverPlay.ui.theme.LocalGlassHaze
import com.raman.CoverPlay.ui.theme.onFill
import com.raman.CoverPlay.ui.theme.rememberCoverPlayWindowBlurActive
import com.raman.CoverPlay.ui.theme.withContrastOn
import com.raman.CoverPlay.util.rememberCoverPlayHapticFeedback
import kotlin.math.PI
import kotlin.math.sin

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

private enum class Art { HERO, NAV, LIST, SWIPE, DOUBLE_TAP, PINCH, HOLD, CONTROLS, LOCK }

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
            "Swipe up or down on the left half for brightness and on the right half for volume, which goes all the way to 200% boost. A capsule slider shows the level on the opposite side so your thumb never hides it.",
            "This one goes to 11. Actually, 200.", Art.SWIPE
        ),
        TourStep(
            "⏩", "DOUBLE-TAP", "Double-tap to skip",
            "Double-tap the left side to jump back and the right side to jump ahead. Keep tapping to stack skips. Double-tap the middle to play or pause. Skip length and the middle tap are in Settings.",
            "Skip the boring bit. We won't tell.", Art.DOUBLE_TAP
        ),
        TourStep(
            "🔍", "PINCH", "Pinch to zoom",
            "Pinch with two fingers to zoom into the picture and drag to pan. Double-tap while zoomed to snap back to normal.",
            "Zoom. Enhance. Enhance.", Art.PINCH
        ),
        TourStep(
            "🚀", "HOLD", "Hold for fast forward",
            "Press and hold anywhere to watch at 2× (you can change the speed in Settings) while your finger is down. Let go and normal speed returns instantly.",
            "Gotta go fast.", Art.HOLD
        ),
        TourStep(
            "🎛️", "CONTROLS", "Everything else is one tap away",
            "Browse, subtitles and audio tracks sit bottom-left. Speed, aspect ratio and rotate sit bottom-right. Picture in picture, lock and playback settings live top-right. Move any of them in Settings, Player Customization.",
            "Behind the sliders lives the good stuff.", Art.CONTROLS
        ),
        TourStep(
            "🔒", "LOCK", "Lock it, pocket it",
            "Tap the lock to freeze every gesture and button, perfect for little fingers. Tap the screen once to reveal the unlock button.",
            "Toddler-proof. Mostly.", Art.LOCK
        ),
        TourStep(
            "▶️", "RESUME", "It remembers for you",
            "Close a video and CoverPlay saves your spot. Next time, it offers to resume. Autoplay and loop can be switched on in Settings.",
            "Just one more episode. Sure.", Art.HERO
        )
    )
}

@Composable
fun CoverPlayAppTutorial(onFinish: () -> Unit) = CoverPlayTourDialog(TourContent.app, "Let's go", onFinish)

@Composable
fun CoverPlayPlayerTutorial(onFinish: () -> Unit) = CoverPlayTourDialog(TourContent.player, "Start watching", onFinish)

@Composable
private fun CoverPlayTourDialog(steps: List<TourStep>, finishLabel: String, onFinish: () -> Unit) {
    val ui = LocalAppUiSettings.current
    val accent = ui.accent
    val pal = ui.palette
    val haptic = rememberCoverPlayHapticFeedback()
    val cfg = LocalConfiguration.current
    val landscape = cfg.screenWidthDp > cfg.screenHeightDp
    val blurActive = rememberCoverPlayWindowBlurActive()

    var index by remember { mutableIntStateOf(0) }
    var dir by remember { mutableIntStateOf(1) }
    var closing by remember { mutableStateOf(false) }
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { shown = true }

    val appear by animateFloatAsState(
        targetValue = if (shown && !closing) 1f else 0f,
        animationSpec = tween(if (closing) 220 else 360, easing = FastOutSlowInEasing),
        label = "tourAppear",
        finishedListener = { if (closing) onFinish() }
    )
    val pop by animateFloatAsState(
        targetValue = if (shown && !closing) 1f else 0f,
        animationSpec = CoverPlaySprings.spatial(),
        label = "tourPop"
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
        CoverPlayDialogBackdrop(blurRadius = 44, dim = if (blurActive) 0.46f else 0.70f)
        CompositionLocalProvider(LocalGlassHaze provides null, LocalGlassBackdrop provides null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = appear }
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
                val cardColor = pal.container.copy(alpha = if (blurActive) 0.94f else 1f)
                Box(
                    modifier = Modifier
                        .widthIn(max = if (landscape) 660.dp else 420.dp)
                        .fillMaxWidth()
                        .graphicsLayer {
                            val sc = 0.9f + 0.1f * pop
                            scaleX = sc; scaleY = sc
                            translationY = (1f - pop) * 60f
                        }
                        .clip(RoundedCornerShape(32.dp))
                        .background(cardColor)
                ) {
                    Column(
                        Modifier
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp, vertical = 18.dp)
                    ) {
                        val last = index == steps.lastIndex

                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier
                                    .clip(CircleShape)
                                    .background(accent.copy(alpha = 0.16f))
                                    .padding(horizontal = 12.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    "${index + 1} of ${steps.size}",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.SemiBold, letterSpacing = 0.6.sp
                                    ),
                                    color = accent.withContrastOn(cardColor)
                                )
                            }
                            Spacer(Modifier.weight(1f))
                            if (!last) {
                                Text(
                                    "Skip",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                    color = pal.textSecondary,
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .clickable { finish() }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                        Spacer(Modifier.height(12.dp))

                        AnimatedContent(
                            targetState = index,
                            transitionSpec = {
                                val d = dir
                                val emph = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)
                                (slideInHorizontally(tween(420, easing = emph)) { it * d / 4 } +
                                        fadeIn(tween(320, delayMillis = 60)) +
                                        scaleIn(initialScale = 0.96f, animationSpec = tween(420, easing = emph))) togetherWith
                                        (slideOutHorizontally(tween(260)) { -it * d / 4 } + fadeOut(tween(160)) +
                                                scaleOut(targetScale = 0.98f, animationSpec = tween(260)))
                            },
                            label = "tourStep"
                        ) { i ->
                            val step = steps[i]
                            if (landscape) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CoverPlayArtStage(step, Modifier.width(210.dp).height(170.dp))
                                    Spacer(Modifier.width(20.dp))
                                    CoverPlayStepText(step, cardColor, Modifier.weight(1f))
                                }
                            } else {
                                Column {
                                    CoverPlayArtStage(step, Modifier.fillMaxWidth().height(164.dp))
                                    Spacer(Modifier.height(18.dp))
                                    CoverPlayStepText(step, cardColor, Modifier.fillMaxWidth())
                                }
                            }
                        }

                        Spacer(Modifier.height(18.dp))

                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            steps.indices.forEach { i ->
                                val w by animateDpAsState(
                                    if (i == index) 28.dp else 8.dp,
                                    CoverPlaySprings.spatial(),
                                    label = "dot$i"
                                )
                                Box(
                                    Modifier
                                        .padding(horizontal = 3.dp)
                                        .height(8.dp)
                                        .width(w)
                                        .clip(CircleShape)
                                        .background(if (i == index) accent else pal.textPrimary.copy(alpha = 0.16f))
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) { go(i) }
                                )
                            }
                        }
                        Spacer(Modifier.height(16.dp))

                        val nextIcon: @Composable (Color) -> Unit = { tint ->
                            Icon(
                                painterResource(R.drawable.ic_chevron_right_custom), null,
                                tint = tint, modifier = Modifier.size(20.dp)
                            )
                        }
                        val actions = buildList {
                            if (index > 0) add(CoverPlayAction(onClick = { go(index - 1) }, label = "Back"))
                            add(
                                CoverPlayAction(
                                    onClick = { if (last) finish() else go(index + 1) },
                                    label = if (last) finishLabel else "Next",
                                    primary = true,
                                    flex = true,
                                    icon = if (last) null else nextIcon
                                )
                            )
                        }
                        CoverPlayButtonGroup(actions = actions, height = 52.dp)
                    }
                }
            }
        }
    }
}

@Composable
private fun CoverPlayStepText(step: TourStep, card: Color, modifier: Modifier = Modifier) {
    val ui = LocalAppUiSettings.current
    val accent = ui.accent
    val pal = ui.palette
    Column(modifier) {
        Text(
            step.tag,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 2.sp),
            color = accent.withContrastOn(card)
        )
        Spacer(Modifier.height(6.dp))
        Text(
            step.title,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
            color = pal.textPrimary
        )
        Spacer(Modifier.height(8.dp))
        Text(
            step.body,
            style = MaterialTheme.typography.bodyMedium,
            color = pal.textSecondary
        )
        Spacer(Modifier.height(14.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(CircleShape)
                .background(accent.copy(alpha = 0.14f))
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Text("✨", fontSize = 13.sp)
            Spacer(Modifier.width(8.dp))
            Text(
                step.quip,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = pal.textPrimary
            )
        }
    }
}

@Composable
private fun CoverPlayArtStage(step: TourStep, modifier: Modifier = Modifier) {
    val accent = LocalAppUiSettings.current.accent
    val cookie12 = CoverPlayMaterialShapes.cookie12()

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(28.dp))
            .background(accent.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center
    ) {
        CoverPlayRotatingShape(
            shape = cookie12,
            modifier = Modifier.size(150.dp).graphicsLayer { scaleX = 1.3f; scaleY = 1.3f },
            color = accent.copy(alpha = 0.12f),
            periodMillis = 24000
        )
        when (step.art) {
            Art.HERO -> CoverPlayArtHero(step.emoji)
            Art.NAV -> CoverPlayArtNav()
            Art.LIST -> CoverPlayArtList()
            Art.SWIPE -> CoverPlayArtSwipe()
            Art.DOUBLE_TAP -> CoverPlayArtDoubleTap()
            Art.PINCH -> CoverPlayArtPinch()
            Art.HOLD -> CoverPlayArtHold()
            Art.CONTROLS -> CoverPlayArtControls()
            Art.LOCK -> CoverPlayArtLock()
        }
    }
}

@Composable
private fun rememberCoverPlayLoop(millis: Int): Float {
    val inf = rememberInfiniteTransition(label = "tourLoop")
    val t by inf.animateFloat(0f, 1f, infiniteRepeatable(tween(millis, easing = LinearEasing)), label = "t")
    return t
}

@Composable
private fun CoverPlayCookieBadge(size: androidx.compose.ui.unit.Dp, content: @Composable () -> Unit) {
    val accent = LocalAppUiSettings.current.accent
    val cookie = CoverPlayMaterialShapes.cookie9()
    Box(Modifier.size(size), contentAlignment = Alignment.Center) {
        CoverPlayRotatingShape(shape = cookie, modifier = Modifier.fillMaxSize(), color = accent, periodMillis = 12000)
        content()
    }
}

@Composable
private fun CoverPlayArtHero(emoji: String) {
    val accent = LocalAppUiSettings.current.accent
    val t = rememberCoverPlayLoop(2600)
    val inf = rememberInfiniteTransition(label = "heroPulse")
    val pulse by inf.animateFloat(
        0.94f, 1.06f,
        infiniteRepeatable(tween(1500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse"
    )
    Box(Modifier.size(150.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            for (i in 0..2) {
                val p = (t + i / 3f) % 1f
                drawCircle(
                    accent.copy(alpha = (1f - p) * 0.40f),
                    size.minDimension * (0.28f + 0.22f * p), center, style = Stroke(2.dp.toPx())
                )
            }
        }
        Box(Modifier.graphicsLayer { scaleX = pulse; scaleY = pulse }) {
            CoverPlayCookieBadge(84.dp) { Text(emoji, fontSize = 34.sp) }
        }
    }
}

@Composable
private fun CoverPlayArtNav() {
    val ui = LocalAppUiSettings.current
    val accent = ui.accent
    val ink = ui.palette.textPrimary
    val four = rememberCoverPlayLoop(5200) * 4f
    val inf = rememberInfiniteTransition(label = "navPulse")
    val pulse by inf.animateFloat(
        0.93f, 1.07f,
        infiniteRepeatable(tween(1500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse"
    )
    Box(Modifier.fillMaxSize()) {
        Canvas(Modifier.fillMaxSize()) {
            val bw = size.width * 0.78f
            val bh = 46.dp.toPx()
            val left = (size.width - bw) / 2f
            val top = size.height / 2f - bh / 2f - 8.dp.toPx()
            drawRoundRect(ink.copy(alpha = 0.10f), Offset(left, top), Size(bw, bh), CornerRadius(bh / 2f))
            val active = four.toInt().coerceIn(0, 3)
            for (i in 0..3) {
                val cx = left + bw * (i + 0.5f) / 4f
                val cy = top + bh / 2f
                if (i == active) {
                    drawRoundRect(
                        accent.copy(alpha = 0.30f),
                        Offset(cx - 22.dp.toPx() * pulse, cy - 14.dp.toPx()),
                        Size(44.dp.toPx() * pulse, 28.dp.toPx()), CornerRadius(14.dp.toPx())
                    )
                    drawCircle(accent, 7.dp.toPx(), Offset(cx, cy))
                } else {
                    drawCircle(ink.copy(alpha = 0.30f), 4.dp.toPx(), Offset(cx, cy))
                }
            }
        }
        Row(
            Modifier.fillMaxWidth(0.78f).align(Alignment.Center).offset(y = 28.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf("Home", "Folders", "Playlists", "Settings").forEach {
                Text(
                    it, modifier = Modifier.weight(1f), textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
                    color = ui.palette.textSecondary, maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun CoverPlayArtList() {
    val ui = LocalAppUiSettings.current
    val accent = ui.accent
    val ink = ui.palette.textPrimary
    val four = rememberCoverPlayLoop(5200) * 4f
    Canvas(Modifier.fillMaxSize()) {
        val rowH = 34.dp.toPx()
        val gap = 10.dp.toPx()
        val total = rowH * 3 + gap * 2
        val top0 = (size.height - total) / 2f
        val w = size.width * 0.80f
        val left = (size.width - w) / 2f
        val hot = four.toInt() % 3
        for (i in 0..2) {
            val y = top0 + i * (rowH + gap)
            val r = CornerRadius(14.dp.toPx())
            drawRoundRect(ink.copy(alpha = if (i == hot) 0.12f else 0.07f), Offset(left, y), Size(w, rowH), r)
            drawRoundRect(
                accent.copy(alpha = if (i == hot) 0.95f else 0.45f),
                Offset(left + 6.dp.toPx(), y + 5.dp.toPx()),
                Size(44.dp.toPx(), rowH - 10.dp.toPx()), CornerRadius(9.dp.toPx())
            )
            drawRoundRect(
                ink.copy(alpha = 0.30f),
                Offset(left + 58.dp.toPx(), y + 9.dp.toPx()),
                Size(w * 0.46f, 5.dp.toPx()), CornerRadius(3.dp.toPx())
            )
            drawRoundRect(
                ink.copy(alpha = 0.16f),
                Offset(left + 58.dp.toPx(), y + 19.dp.toPx()),
                Size(w * 0.28f, 4.dp.toPx()), CornerRadius(3.dp.toPx())
            )
        }
    }
}

@Composable
private fun CoverPlayMiniGestureBar(icon: Int, label: String, level: Float) {
    val ui = LocalAppUiSettings.current
    val accent = ui.accent
    val onAccent = accent.onFill()
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(
            Modifier
                .width(30.dp).height(66.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(ui.palette.textPrimary.copy(alpha = 0.12f))
        ) {
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .fillMaxHeight(level.coerceIn(0f, 1f))
                    .clip(RoundedCornerShape(15.dp))
                    .background(accent)
            )
        }
        CoverPlayCookieBadge(30.dp) { Icon(painterResource(icon), null, tint = onAccent, modifier = Modifier.size(15.dp)) }
        Text(label, style = MaterialTheme.typography.labelSmall, color = ui.palette.textSecondary)
    }
}

@Composable
private fun CoverPlayArtSwipe() {
    val t = rememberCoverPlayLoop(3200)
    val ph = (sin(t * 2f * PI.toFloat()) + 1f) / 2f
    Row(
        Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CoverPlayMiniGestureBar(R.drawable.ic_brightness_custom, "Brightness", 0.15f + 0.75f * ph)
        CoverPlayMiniGestureBar(R.drawable.ic_volume_custom, "Volume", 0.15f + 0.75f * (1f - ph))
    }
}

@Composable
private fun CoverPlayArtDoubleTap() {
    val ui = LocalAppUiSettings.current
    val accent = ui.accent
    val onAccent = accent.onFill()
    val t = rememberCoverPlayLoop(2600)
    Row(Modifier.fillMaxSize()) {
        for (side in 0..1) {
            val forward = side == 1
            val active = (t < 0.5f) == (side == 0)
            val p = if (!active) 0f else if (t < 0.5f) t * 2f else (t - 0.5f) * 2f
            val shape = if (forward)
                RoundedCornerShape(topStartPercent = 50, topEndPercent = 0, bottomEndPercent = 0, bottomStartPercent = 50)
            else
                RoundedCornerShape(topStartPercent = 0, topEndPercent = 50, bottomEndPercent = 50, bottomStartPercent = 0)
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(shape)
                    .background(accent.copy(alpha = if (active) 0.30f * (1f - 0.7f * p) else 0.05f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.graphicsLayer {
                        alpha = if (active) 1f else 0.40f
                        val s = if (active) 0.9f + 0.1f * (p * 4f).coerceAtMost(1f) else 0.9f
                        scaleX = s; scaleY = s
                    }
                ) {
                    CoverPlayCookieBadge(46.dp) {
                        Icon(
                            painterResource(if (forward) R.drawable.ic_forward_custom else R.drawable.ic_rewind_custom),
                            null, tint = onAccent, modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        if (forward) "+10 sec" else "−10 sec",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = ui.palette.textPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun CoverPlayArtPinch() {
    val ui = LocalAppUiSettings.current
    val accent = ui.accent
    val t = rememberCoverPlayLoop(2600)
    Canvas(Modifier.fillMaxSize()) {
        val ph = (sin(t * 2f * PI.toFloat()) + 1f) / 2f
        val d = 16.dp.toPx() + 42.dp.toPx() * ph
        val ux = 0.8f
        val uy = -0.6f
        val p1 = Offset(center.x + ux * d, center.y + uy * d)
        val p2 = Offset(center.x - ux * d, center.y - uy * d)
        drawLine(
            accent.copy(alpha = 0.50f), p1, p2, strokeWidth = 3.dp.toPx(), cap = StrokeCap.Round,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 14f))
        )
        for (p in listOf(p1, p2)) {
            drawCircle(accent.copy(alpha = 0.28f), 18.dp.toPx(), p)
            drawCircle(accent, 8.dp.toPx(), p)
        }
    }
}

@Composable
private fun CoverPlayArtHold() {
    val ui = LocalAppUiSettings.current
    val accent = ui.accent
    val t = rememberCoverPlayLoop(2600)
    val inf = rememberInfiniteTransition(label = "holdPulse")
    val pulse by inf.animateFloat(
        0.95f, 1.05f,
        infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse"
    )
    Box(Modifier.size(130.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val r = size.minDimension * 0.42f
            val sw = 8.dp.toPx()
            drawCircle(ui.palette.textPrimary.copy(alpha = 0.12f), r, center, style = Stroke(sw))
            drawArc(
                accent, -90f, 360f * t, false,
                topLeft = Offset(center.x - r, center.y - r), size = Size(r * 2f, r * 2f),
                style = Stroke(sw, cap = StrokeCap.Round)
            )
        }
        Box(Modifier.graphicsLayer { scaleX = pulse; scaleY = pulse }) {
            CoverPlayCookieBadge(72.dp) {
                Text("2×", color = accent.onFill(), fontSize = 24.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun CoverPlayControlChip(icon: Int, on: Boolean) {
    val ui = LocalAppUiSettings.current
    val accent = ui.accent
    val scale by animateFloatAsState(if (on) 1.12f else 1f, CoverPlaySprings.spatial(), label = "chipScale")
    Box(
        Modifier.size(50.dp).graphicsLayer { scaleX = scale; scaleY = scale },
        contentAlignment = Alignment.Center
    ) {
        if (on) {
            CoverPlayCookieBadge(50.dp) {
                Icon(painterResource(icon), null, tint = accent.onFill(), modifier = Modifier.size(22.dp))
            }
        } else {
            Box(
                Modifier.size(44.dp).clip(CircleShape).background(ui.palette.textPrimary.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(painterResource(icon), null, tint = ui.palette.textSecondary, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun CoverPlayArtControls() {
    val four = rememberCoverPlayLoop(5200) * 4f
    val on = four.toInt().coerceIn(0, 3)
    val icons = listOf(
        R.drawable.ic_subtitles_custom, R.drawable.ic_music_custom,
        R.drawable.ic_speed_custom, R.drawable.ic_player_controls
    )
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
        icons.forEachIndexed { i, ic -> CoverPlayControlChip(ic, on == i) }
    }
}

@Composable
private fun CoverPlayArtLock() {
    val ui = LocalAppUiSettings.current
    val accent = ui.accent
    val t = rememberCoverPlayLoop(3200)
    val open = t > 0.55f
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        CoverPlayCookieBadge(72.dp) {
            AnimatedContent(
                targetState = open,
                transitionSpec = {
                    (fadeIn(tween(140)) + scaleIn(initialScale = 0.5f, animationSpec = spring(0.55f, 520f)))
                        .togetherWith(fadeOut(tween(90)) + scaleOut(targetScale = 0.5f))
                },
                label = "lockGlyph"
            ) { o ->
                Icon(
                    painterResource(if (o) R.drawable.ic_lock_open_custom else R.drawable.ic_lock_custom),
                    null, tint = accent.onFill(), modifier = Modifier.size(30.dp)
                )
            }
        }
        Text(
            if (open) "Unlocked" else "Tap to unlock",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = ui.palette.textPrimary,
            modifier = Modifier
                .clip(CircleShape)
                .background(ui.palette.textPrimary.copy(alpha = 0.10f))
                .padding(horizontal = 12.dp, vertical = 5.dp)
        )
    }
}
