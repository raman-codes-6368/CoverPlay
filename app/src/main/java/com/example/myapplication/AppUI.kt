package com.example.myapplication

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Size
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.draw.drawBehind
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import androidx.core.graphics.drawable.toDrawable
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.DataSource
import coil.fetch.DrawableResult
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.request.ImageRequest
import coil.request.Options
import coil.request.videoFrameMillis
import coil.size.pxOrElse
import com.example.myapplication.ui.theme.*
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Calendar

// ── Resolution / quality label ─────────────────────────────────────────────
fun formatVideoTitleDate(seconds: Long, duration: Long): String {
    if (seconds == 0L) return if (duration > 0) formatDuration(duration) else "Video"
    val sdf = java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault())
    val dateStr = sdf.format(java.util.Date(seconds * 1000))
    return if (duration > 0) "$dateStr · ${formatDuration(duration)}" else dateStr
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

@Composable
fun QualityBadge(width: Int, height: Int, modifier: Modifier = Modifier) {
    val settings = LocalAppUiSettings.current
    val label = if (width > 0 && height > 0) qualityLabel(width, height) else "FHD"
    val badgeColor = resolutionColor(label)
    val badgeFill = if (settings.palette.isDark) Color(0x990A0D1A) else Color(0xEEEDF0FE)
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
        color = badgeColor,
        modifier = modifier
            .glassBadge(badgeFill, badgeColor)
            .padding(horizontal = 9.dp, vertical = 3.dp)
    )
}

@Composable
fun HierarchyDurationBadge(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.3.sp),
        color = BrandAccent,
        modifier = modifier
            .glassBadge(BrandAccent.copy(alpha = 0.16f), BrandAccent)
            .padding(horizontal = 9.dp, vertical = 3.dp)
    )
}

class VideoThumbnailFetcher(private val data: Any, private val options: Options, private val context: Context) : Fetcher {
    override suspend fun fetch(): FetchResult? {
        if (data is Uri && data.scheme == "content" && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                val width = options.size.width.pxOrElse { 512 }
                val height = options.size.height.pxOrElse { 320 }
                val bitmap = context.contentResolver.loadThumbnail(data, Size(width, height), null)
                return DrawableResult(drawable = bitmap.toDrawable(context.resources), isSampled = true, dataSource = DataSource.DISK)
            } catch (_: Exception) {}
        }
        val retriever = android.media.MediaMetadataRetriever()
        try {
            when (data) {
                is Uri -> retriever.setDataSource(context, data)
                is String -> retriever.setDataSource(data)
                is File -> retriever.setDataSource(data.absolutePath)
                else -> return null
            }
            var bitmap = retriever.getFrameAtTime(1_000_000, android.media.MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
            if (bitmap == null) bitmap = retriever.getFrameAtTime(5_000_000, android.media.MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
            if (bitmap == null) bitmap = retriever.getFrameAtTime(-1)
            if (bitmap != null) {
                return DrawableResult(drawable = bitmap.toDrawable(context.resources), isSampled = true, dataSource = DataSource.DISK)
            }
        } catch (_: Exception) {
        } finally {
            try { retriever.release() } catch (_: Exception) {}
        }
        return null
    }

    class Factory(private val context: Context) : Fetcher.Factory<Any> {
        override fun create(data: Any, options: Options, imageLoader: ImageLoader): Fetcher? {
            if (data is Uri || data is String || data is File) {
                return VideoThumbnailFetcher(data, options, context)
            }
            return null
        }
    }
}

@Composable
private fun NavGlass(
    modifier: Modifier,
    shape: Shape,
    backdrop: BackdropState?,
    hazeState: HazeState? = null,
    accent: Color,
    content: @Composable BoxScope.() -> Unit
) {
    val settings = LocalAppUiSettings.current
    val mode = settings.themeMode

    val glassFill = when (mode) {
        ThemeMode.LIGHT -> Color.White.copy(alpha = 0.68f)
        ThemeMode.AMOLED -> settings.palette.glassBase.copy(alpha = 0.52f)
        ThemeMode.DARK -> settings.palette.glassBase.copy(alpha = 0.44f)
    }

    val rimHi = when (mode) {
        ThemeMode.LIGHT -> Color.White
        ThemeMode.AMOLED -> Color.White.copy(alpha = 0.55f)
        ThemeMode.DARK -> Color.White.copy(alpha = 0.45f)
    }

    Box(
        modifier = modifier
            .shadow(
                elevation = 10.dp,
                shape = shape,
                ambientColor = accent.copy(alpha = GlowSpec.AMBIENT * 0.6f),
                spotColor = accent.copy(alpha = GlowSpec.SPOT * 0.6f)
            )
            .clip(shape)
            .then(
                if (hazeState != null) {
                    Modifier.hazeEffect(
                        state = hazeState,
                        style = dev.chrisbanes.haze.HazeStyle(
                            blurRadius = 18.dp,
                            noiseFactor = 0.015f,
                            tint = dev.chrisbanes.haze.HazeTint(accent.copy(alpha = 0.04f))
                        )
                    )
                } else if (backdrop != null) {
                    Modifier.backdropBlur(backdrop)
                } else {
                    Modifier
                }
            )
            .background(glassFill)
            .background(
                Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = if (mode == ThemeMode.LIGHT) 0.65f else 0.18f),
                        Color.White.copy(alpha = 0.02f)
                    )
                )
            )
            .border(
                1.dp,
                Brush.linearGradient(
                    listOf(rimHi, accent.copy(alpha = 0.35f), Color.White.copy(alpha = 0.10f))
                ),
                shape
            ),
        content = content
    )
}

@Composable
fun BottomNavigationBar(
    currentScreen: Screen,
    onScreenSelected: (Screen) -> Unit,
    collapsed: Boolean = false,
    backdrop: BackdropState? = null,
    hazeState: HazeState? = LocalGlassHaze.current
) {
    val tabs = listOf(
        NavItem(Screen.HOME,      "Home",      R.drawable.ic_nav_home_filled,     R.drawable.ic_nav_home_unfilled),
        NavItem(Screen.FOLDERS,   "Folders",   R.drawable.ic_nav_folder_filled,   R.drawable.ic_nav_folder_unfilled),
        NavItem(Screen.PLAYLISTS, "Playlists", R.drawable.ic_nav_playlist_filled, R.drawable.ic_nav_playlist_unfilled)
    )
    val settingsItem = NavItem(Screen.SETTINGS, "Settings", R.drawable.ic_nav_settings_filled, R.drawable.ic_nav_settings_unfilled)
    val settings = LocalAppUiSettings.current
    val haptic = LocalHapticFeedback.current
    val accent = settings.accent
    val isDark = settings.palette.isDark

    val scale by animateFloatAsState(if (collapsed) 0.88f else 1f, spring(0.80f, 380f), label = "navScale")
    val settingsSelected = currentScreen == Screen.SETTINGS

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (hazeState != null) {
                    Modifier.hazeEffect(state = hazeState) {
                        style = HazeStyle(
                            blurRadius = 24.dp,
                            noiseFactor = 0.01f,
                            tint = HazeTint(settings.palette.bg.copy(alpha = if (isDark) 0.60f else 0.82f))
                        )
                        progressive = HazeProgressive.verticalGradient(
                            startIntensity = 0f,
                            endIntensity = 1f
                        )
                    }
                } else Modifier
            )
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color.Transparent,
                        settings.palette.bg.copy(alpha = if (isDark) 0.40f else 0.65f),
                        settings.palette.bg.copy(alpha = if (isDark) 0.88f else 0.96f)
                    )
                )
            ),
        contentAlignment = Alignment.BottomCenter
    ) {
        Row(
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 18.dp, vertical = 12.dp)
                .fillMaxWidth()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    transformOrigin = TransformOrigin(0.5f, 1f)
                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // ── Main Glass Pill (3 Tabs) ──
            NavGlass(Modifier.weight(1f).height(62.dp), CircleShape, backdrop, hazeState, accent) {
                Row(Modifier.padding(4.dp).fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                    tabs.forEach { item ->
                        val isSelected = when (item.screen) {
                            Screen.FOLDERS -> currentScreen == Screen.FOLDERS || currentScreen == Screen.FOLDER_VIDEOS
                            else -> currentScreen == item.screen
                        }
                        val weight by animateFloatAsState(if (isSelected) 1.5f else 0.8f, spring(0.85f, 380f), label = "navW")
                        val selection by animateFloatAsState(if (isSelected) 1f else 0f, tween(300, easing = FastOutSlowInEasing), label = "navSel")
                        val contentColor by animateColorAsState(if (isSelected) Color.White else settings.palette.textSecondary, tween(250), label = "navC")
                        val interaction = remember { MutableInteractionSource() }
                        val pressed by interaction.collectIsPressedAsState()
                        val pressScale by animateFloatAsState(if (pressed) 0.92f else 1f, spring(0.55f, 520f), label = "navP")

                        Box(
                            modifier = Modifier
                                .weight(weight.coerceAtLeast(0.1f))
                                .fillMaxHeight()
                                .graphicsLayer { scaleX = pressScale; scaleY = pressScale }
                                .clip(CircleShape)
                                .clickable(interactionSource = interaction, indication = null) {
                                    if (!isSelected) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onScreenSelected(item.screen)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                Modifier.matchParentSize().graphicsLayer { alpha = selection }
                                    .liquidLens(accent)
                            )
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.padding(horizontal = 6.dp)) {
                                Icon(
                                    painter = painterResource(if (isSelected) item.filledIcon else item.outlineIcon),
                                    contentDescription = item.label, tint = contentColor,
                                    modifier = Modifier.size(22.dp).graphicsLayer { val sc = 1f + 0.08f * selection; scaleX = sc; scaleY = sc }
                                )
                                AnimatedVisibility(
                                    visible = isSelected && !collapsed,
                                    enter = fadeIn(tween(200, delayMillis = 60)) + expandHorizontally(spring(0.85f, 400f)),
                                    exit = fadeOut(tween(100)) + shrinkHorizontally(spring(0.9f, 500f))
                                ) {
                                    Row {
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            text = item.label,
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, fontSize = 12.sp),
                                            color = Color.White,
                                            maxLines = 1,
                                            softWrap = false,
                                            overflow = TextOverflow.Clip
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── Settings Button ──
            val sInteraction = remember { MutableInteractionSource() }
            val sPressed by sInteraction.collectIsPressedAsState()
            val sPress by animateFloatAsState(if (sPressed) 0.90f else 1f, spring(0.55f, 520f), label = "setP")
            val settingsSel by animateFloatAsState(if (settingsSelected) 1f else 0f, tween(300, easing = FastOutSlowInEasing), label = "setSel")

            NavGlass(
                Modifier.size(62.dp).graphicsLayer { scaleX = sPress; scaleY = sPress }
                    .clickable(interactionSource = sInteraction, indication = null) {
                        if (!settingsSelected) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onScreenSelected(Screen.SETTINGS)
                    },
                CircleShape, backdrop, hazeState, accent
            ) {
                Box(
                    Modifier.matchParentSize().graphicsLayer { alpha = settingsSel }.liquidLens(accent)
                )
                Icon(
                    painter = painterResource(if (settingsSelected) settingsItem.filledIcon else settingsItem.outlineIcon),
                    contentDescription = "Settings",
                    tint = if (settingsSelected) Color.White else settings.palette.textSecondary,
                    modifier = Modifier.size(22.dp).align(Alignment.Center)
                )
            }
        }
    }
}

private data class NavItem(val screen: Screen, val label: String, val filledIcon: Int, val outlineIcon: Int)

private data class ModeOption(val mode: ThemeMode, val label: String, val icon: ImageVector)

/**
 * Compact liquid-glass appearance panel: one segmented mode selector + one swatch row.
 * Drawn in the SAME window as the screen (not a Popup) so the live Haze blur works.
 */
@Composable
fun AppearanceGlassPanel(
    currentMode: ThemeMode,
    accent: Color,
    accentOptions: List<Color>,
    onThemeModeChange: (ThemeMode) -> Unit,
    onAccentChange: (Color) -> Unit,
    modifier: Modifier = Modifier,
    accentName: String? = null
) {
    val settings = LocalAppUiSettings.current
    val context = LocalContext.current
    val options = remember {
        listOf(
            ModeOption(ThemeMode.DARK, "Dark", Icons.Rounded.DarkMode),
            ModeOption(ThemeMode.LIGHT, "Light", Icons.Rounded.LightMode),
            ModeOption(ThemeMode.AMOLED, "AMOLED", Icons.Rounded.Contrast)
        )
    }

    GlassSurface(
        modifier = modifier.width(280.dp),
        shape = RoundedCornerShape(26.dp),
        blur = true,
        elevation = 20.dp,
        glow = accent
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "APPEARANCE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.8.sp
                    ),
                    color = accent
                )

                Spacer(Modifier.weight(1f))

                Text(
                    accentName ?: settings.appTheme.label,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium),
                    color = settings.palette.textTertiary
                )
            }

            // ── Segmented mode selector with a sliding accent glass lens ──
            val index = options.indexOfFirst { it.mode == currentMode }.coerceAtLeast(0)
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = if (settings.palette.isDark) 0.06f else 0.35f))
                    .border(1.dp, Color.White.copy(alpha = if (settings.palette.isDark) 0.10f else 0.5f), CircleShape)
                    .padding(3.dp)
            ) {
                val segW = maxWidth / options.size
                val lensX by animateDpAsState(segW * index, spring(0.75f, 380f), label = "modeLens")
                Box(
                    Modifier
                        .offset(x = lensX)
                        .width(segW)
                        .fillMaxHeight()
                        .liquidLens(accent)
                )
                Row(Modifier.fillMaxSize()) {
                    options.forEach { o ->
                        val selected = o.mode == currentMode
                        val tint by animateColorAsState(
                            if (selected) Color.White else settings.palette.textSecondary,
                            tween(200), label = "modeTint"
                        )
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(CircleShape)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { onThemeModeChange(o.mode) },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(o.icon, null, tint = tint, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(
                                o.label,
                                style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                                color = tint,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // ── 5 Full Expressive Themes swatches ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppTheme.entries.forEach { appTheme ->
                    val selected = settings.appTheme == appTheme
                    val sc by animateFloatAsState(if (selected) 1.15f else 0.88f, spring(0.6f, 450f), label = "swatch")
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .graphicsLayer { scaleX = sc; scaleY = sc }
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(appTheme.highlight, appTheme.primaryAccent, appTheme.secondaryAccent)))
                            .border(
                                if (selected) 2.dp else 1.dp,
                                if (selected) Color.White else Color.White.copy(alpha = 0.22f),
                                CircleShape
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                setAppTheme(context, appTheme)
                                onAccentChange(appTheme.primaryAccent)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (selected) Icon(Icons.Rounded.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

/** Accent colors offered in the Appearance popup (pulled from the brand palette). */
val DefaultAccentOptions: List<Color> = AppTheme.entries.map { it.primaryAccent }

private enum class DayPeriod(val greeting: String, val line: String) {
    MORNING("Good morning", "Start your day with something good"),
    AFTERNOON("Good afternoon", "Take a break and press play"),
    EVENING("Good evening", "Unwind with something you love"),
    NIGHT("Good night", "Settle in for a late-night watch")
}

private fun periodFor(hour: Int): DayPeriod = when (hour) {
    in 5..11 -> DayPeriod.MORNING
    in 12..16 -> DayPeriod.AFTERNOON
    in 17..20 -> DayPeriod.EVENING
    else -> DayPeriod.NIGHT
}

@Composable
fun GreetingHeader(
    modifier: Modifier = Modifier,
    userName: String? = null,
    hasRecent: Boolean = false,
    onThemeModeChange: (ThemeMode) -> Unit = {},
    accentOptions: List<Color> = DefaultAccentOptions,
    onAccentChange: (Color) -> Unit = {}
) {
    val settings = LocalAppUiSettings.current
    val accent = settings.accent
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    // Re-check the time every minute so the greeting flips on its own.
    var now by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000L)
            now = System.currentTimeMillis()
        }
    }
    val cal = remember(now) { Calendar.getInstance().apply { timeInMillis = now } }
    val period = periodFor(cal.get(Calendar.HOUR_OF_DAY))

    // Rotating witty copy: auto-advances every 14 s, or instantly on a double tap (which also restarts the timer).
    var tick by remember { mutableStateOf(0) }
    var manualNonce by remember { mutableStateOf(0) }
    LaunchedEffect(manualNonce) {
        while (true) {
            delay(14_000L)
            tick++
        }
    }
    val line = remember(period, tick, hasRecent, settings.appTheme) {
        Humor.greeting(period.ordinal, tick, hasRecent, settings.appTheme)
    }
    val dateText = remember(now) {
        java.text.SimpleDateFormat("EEEE, MMM d", java.util.Locale.getDefault())
            .format(java.util.Date(now)).uppercase()
    }

    // Staggered entrance
    var show by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { show = true }
    val emph = remember { CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f) }
    val a1 by animateFloatAsState(if (show) 1f else 0f, tween(650, easing = emph), label = "g1")
    val a2 by animateFloatAsState(if (show) 1f else 0f, tween(650, delayMillis = 110, easing = emph), label = "g2")
    val a3 by animateFloatAsState(if (show) 1f else 0f, tween(650, delayMillis = 220, easing = emph), label = "g3")

    // Double-tap feedback: a soft press-and-release pulse + a one-shot accent shimmer under the text.
    val pulse = remember { Animatable(1f) }
    val shimmer = remember { Animatable(0f) }

    // Hidden easter egg: 7 quick taps (a double tap counts as two).
    var eggCount by remember { mutableIntStateOf(0) }
    var eggLast by remember { mutableLongStateOf(0L) }
    fun eggHit(n: Int) {
        val t = System.currentTimeMillis()
        eggCount = if (t - eggLast > 1500L) n else eggCount + n
        eggLast = t
        if (eggCount >= 7) { eggCount = 0; showSecret(context) }
    }

    val gradient = Brush.linearGradient(listOf(accent, accent.copy(alpha = 0.55f), Color.White))

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            Modifier
                .weight(1f)
                .graphicsLayer {
                    scaleX = pulse.value
                    scaleY = pulse.value
                    transformOrigin = TransformOrigin(0f, 0.5f)
                }
                .drawBehind {
                    val p = shimmer.value
                    if (p > 0.001f) {
                        val w = size.width
                        drawRect(
                            Brush.horizontalGradient(
                                0f to Color.Transparent,
                                0.5f to accent.copy(alpha = 0.16f * kotlin.math.sin(p * Math.PI.toFloat())),
                                1f to Color.Transparent,
                                startX = -w * 0.4f + w * 1.8f * p, endX = w * 0.4f + w * 1.8f * p
                            )
                        )
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { eggHit(1) },
                        onDoubleTap = {
                            eggHit(2)
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            tick++
                            manualNonce++
                            scope.launch {
                                launch {
                                    pulse.animateTo(0.965f, tween(90, easing = FastOutSlowInEasing))
                                    pulse.animateTo(1f, spring(dampingRatio = 0.45f, stiffness = 380f))
                                }
                                shimmer.snapTo(0f)
                                shimmer.animateTo(1f, tween(750, easing = FastOutSlowInEasing))
                                shimmer.snapTo(0f)
                            }
                        }
                    )
                }
        ) {
            Text(
                text = dateText,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.6.sp
                ),
                color = settings.palette.textTertiary,
                modifier = Modifier.graphicsLayer {
                    alpha = a1
                    translationY = (1f - a1) * 10.dp.toPx()
                }
            )
            Spacer(Modifier.height(4.dp))
            AnimatedContent(
                targetState = line.first,
                transitionSpec = {
                    ((fadeIn(tween(520, 110, easing = emph)) + slideInVertically(tween(520, 110, easing = emph)) { it / 3 }) togetherWith
                            (fadeOut(tween(220)) + slideOutVertically(tween(260, easing = emph)) { -it / 4 }))
                        .using(SizeTransform(clip = false))
                },
                label = "greetHead"
            ) { headline ->
                Text(
                    text = buildAnnotatedString {
                        append(headline)
                        if (!userName.isNullOrBlank()) {
                            append(",\n")
                            withStyle(SpanStyle(brush = gradient)) { append(userName) }
                        }
                    },
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 24.sp,
                        lineHeight = 30.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.4).sp
                    ),
                    color = settings.palette.textPrimary,
                    modifier = Modifier.graphicsLayer {
                        alpha = a2
                        translationY = (1f - a2) * 14.dp.toPx()
                    }
                )
            }
            Spacer(Modifier.height(6.dp))
            AnimatedContent(
                targetState = line.second,
                transitionSpec = {
                    ((fadeIn(tween(520, 200, easing = emph)) + slideInVertically(tween(520, 200, easing = emph)) { it / 2 }) togetherWith
                            (fadeOut(tween(200)) + slideOutVertically(tween(240, easing = emph)) { -it / 3 }))
                        .using(SizeTransform(clip = false))
                },
                label = "greetSub"
            ) { sub ->
                Text(
                    text = sub,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, fontWeight = FontWeight.Normal),
                    color = settings.palette.textTertiary,
                    modifier = Modifier.graphicsLayer {
                        alpha = a3
                        translationY = (1f - a3) * 14.dp.toPx()
                    }
                )
            }
        }
    }
}

private val DiceOvershoot = Easing { t ->
    val s = 1.35f
    val x = t - 1f
    x * x * ((s + 1f) * x + s) + 1f
}

/**
 * Premium dice: lifts off, tumbles on all three axes with a springy overshoot, lands with a
 * ripple ring — the new theme is applied mid-tumble so the change feels like the dice caused it.
 */
@Composable
fun DiceRollButton(size: androidx.compose.ui.unit.Dp = 38.dp, onRoll: () -> Unit) {
    val settings = LocalAppUiSettings.current
    val accent = settings.accent
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val roll = remember { Animatable(0f) }
    var rolling by remember { mutableStateOf(false) }
    var dir by remember { mutableStateOf(1f) }
    val painter = painterResource(R.drawable.ic_random_dice)
    val idleTint = settings.palette.textPrimary

    Box(Modifier.size(size), contentAlignment = Alignment.Center) {
        // landing ripple
        Box(
            Modifier
                .matchParentSize()
                .graphicsLayer {
                    val p = roll.value
                    val k = ((p - 0.55f) / 0.45f).coerceIn(0f, 1f)
                    alpha = if (k > 0f) (1f - k) * 0.75f else 0f
                    val sc = 1f + 0.85f * k
                    scaleX = sc; scaleY = sc
                }
                .border(1.5.dp, accent, CircleShape)
        )
        GlassSurface(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer {
                    val p = roll.value
                    val arc = kotlin.math.sin(Math.PI.toFloat() * p)
                    val sc = 1f + 0.30f * arc
                    scaleX = sc; scaleY = sc
                    translationY = -12.dp.toPx() * arc
                },
            shape = CircleShape,
            elevation = 3.dp,
            blur = true,
            pressedScale = 0.92f,
            onClick = {
                if (!rolling) {
                    rolling = true
                    dir = -dir
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    scope.launch {
                        launch {
                            delay(520L)
                            onRoll()
                        }
                        roll.snapTo(0f)
                        roll.animateTo(1f, tween(1150, easing = LinearEasing))
                        roll.snapTo(0f)
                        rolling = false
                    }
                }
            }
        ) {
            Icon(
                painter = painter,
                contentDescription = "Randomize appearance",
                tint = androidx.compose.ui.graphics.lerp(idleTint, accent, kotlin.math.sin(Math.PI.toFloat() * roll.value)),
                modifier = Modifier
                    .size(size * 0.5f)
                    .align(Alignment.Center)
                    .graphicsLayer {
                        val e = DiceOvershoot.transform(roll.value)
                        cameraDistance = 14f * density
                        rotationX = 540f * e * dir
                        rotationY = 720f * e * dir
                        rotationZ = 180f * e
                    }
            )
        }
    }
}

@Composable
fun SignatureTitle(title: String) {
    val settings = LocalAppUiSettings.current
    if (title == "CoverPlay") {
        var startAnim by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { startAnim = true }

        val alpha by animateFloatAsState(
            targetValue = if (startAnim) 1f else 0f,
            animationSpec = tween(800)
        )
        val translateY by animateDpAsState(
            targetValue = if (startAnim) 0.dp else (-8).dp,
            animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f)
        )
        val scale by animateFloatAsState(
            targetValue = if (startAnim) 1f else 0.92f,
            animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f)
        )

        Row(
            modifier = Modifier.graphicsLayer {
                this.alpha = alpha
                this.translationY = translateY.toPx()
                this.scaleX = scale
                this.scaleY = scale
            },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Cover",
                style = TopBarTitleStyle,
                color = settings.palette.textPrimary
            )

            // "Play" part with slightly delayed entrance
            val playAlpha by animateFloatAsState(
                targetValue = if (startAnim) 1f else 0f,
                animationSpec = tween(600, delayMillis = 250)
            )
            val playOffset by animateDpAsState(
                targetValue = if (startAnim) 0.dp else 6.dp,
                animationSpec = spring(dampingRatio = 0.6f, stiffness = 200f)
            )

            Text(
                text = "Play",
                style = TopBarTitleStyle,
                color = settings.accent,
                modifier = Modifier.graphicsLayer {
                    this.alpha = playAlpha
                    this.translationX = playOffset.toPx()
                }
            )
        }
    } else {
        Text(
            text = title,
            style = TopBarTitleStyle,
            color = settings.palette.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/** Compact, confident title — present but never louder than the content below it. */
private val TopBarTitleStyle: androidx.compose.ui.text.TextStyle
    @Composable get() = MaterialTheme.typography.headlineMedium.copy(
        fontSize = 20.sp,
        lineHeight = 26.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.3).sp
    )

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopAppBar(
    title: String,
    isSearchActive: Boolean = false,
    searchQuery: String = "",
    onSearchQueryChange: (String) -> Unit = {},
    onToggleSearch: () -> Unit = {},
    showBackButton: Boolean = false,
    onBack: () -> Unit = {},
    currentLayout: LayoutMode = LayoutMode.LIST,
    onToggleLayout: () -> Unit = {},
    onShowFilter: () -> Unit = {},
    showExtraActions: Boolean = true,
    isScrolled: Boolean = false,
    showDice: Boolean = false,
    hazeState: HazeState? = LocalGlassHaze.current
) {
    val focusRequester = remember { FocusRequester() }
    val settings = LocalAppUiSettings.current
    val haptic = LocalHapticFeedback.current
    val fadeZone = 24.dp
    val backdropAlpha by animateFloatAsState(
        targetValue = if (isScrolled) 1f else 0f,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "barBackdrop"
    )

    Box(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .layout { measurable, constraints ->
                    val extra = fadeZone.roundToPx()
                    val placeable = measurable.measure(
                        Constraints.fixed(constraints.maxWidth, constraints.maxHeight + extra)
                    )
                    layout(constraints.maxWidth, constraints.maxHeight) {
                        placeable.place(0, 0)
                    }
                }
                .graphicsLayer { alpha = backdropAlpha }
        ) {
            if (hazeState != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .hazeEffect(state = hazeState) {
                            style = HazeStyle(
                                blurRadius = 16.dp,
                                noiseFactor = 0.01f,
                                tint = HazeTint(settings.palette.bg.copy(alpha = 0.60f))
                            )
                            progressive = HazeProgressive.verticalGradient(
                                startIntensity = 1f,
                                endIntensity = 0f,
                                easing = EaseInOut
                            )
                        }
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.00f to settings.palette.bg.copy(alpha = 0.90f),
                                0.50f to settings.palette.bg.copy(alpha = 0.60f),
                                0.82f to settings.palette.bg.copy(alpha = 0.20f),
                                1.00f to Color.Transparent
                            )
                        )
                    )
            )
        }

        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(1.dp)
                .graphicsLayer { alpha = backdropAlpha }
                .background(
                    Brush.horizontalGradient(
                        listOf(Color.Transparent, settings.accent.copy(alpha = 0.10f), settings.palette.textTertiary.copy(alpha = 0.08f), Color.Transparent)
                    )
                )
        )

        TopAppBar(
            title = {
                AnimatedContent(
                    targetState = isSearchActive,
                    transitionSpec = {
                        if (targetState) {
                            (fadeIn(tween(400)) + slideInHorizontally { it / 3 } + expandHorizontally(
                                expandFrom = Alignment.End,
                                animationSpec = spring(0.75f, 400f)
                            )) togetherWith
                                    (fadeOut(tween(300)) + slideOutHorizontally { -it / 2 })
                        } else {
                            (fadeIn(tween(400)) + slideInHorizontally { -it / 3 }) togetherWith
                                    (fadeOut(tween(300)) + slideOutHorizontally { it / 2 } + shrinkHorizontally(
                                        shrinkTowards = Alignment.End
                                    ))
                        }
                    },
                    label = "searchExpansion"
                ) { active ->
                    if (active) {
                        LaunchedEffect(Unit) { focusRequester.requestFocus() }
                        GlassSurface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .padding(end = 12.dp),
                            shape = CircleShape,
                            blur = false,
                            elevation = 2.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_search_custom),
                                    contentDescription = null,
                                    tint = settings.palette.textSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                BasicTextField(
                                    value = searchQuery,
                                    onValueChange = onSearchQueryChange,
                                    modifier = Modifier
                                        .weight(1f)
                                        .focusRequester(focusRequester),
                                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                                        color = settings.palette.textPrimary,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    cursorBrush = SolidColor(settings.accent),
                                    singleLine = true,
                                    decorationBox = { innerTextField ->
                                        Box(contentAlignment = Alignment.CenterStart) {
                                            if (searchQuery.isEmpty()) {
                                                Text(
                                                    "Search videos...",
                                                    color = settings.palette.textTertiary,
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                            }
                                            innerTextField()
                                        }
                                    }
                                )
                                if (searchQuery.isNotEmpty()) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        painter = painterResource(R.drawable.ic_close_custom),
                                        contentDescription = "Clear",
                                        tint = settings.palette.textSecondary,
                                        modifier = Modifier
                                            .size(16.dp)
                                            .aliveClickable { onSearchQueryChange("") }
                                    )
                                }
                            }
                        }
                    } else {
                        val titleCtx = LocalContext.current
                        Box(Modifier.easterEggTaps(7) { showSecret(titleCtx, Quips.tabSecret(title)) }) { SignatureTitle(title) }
                    }
                }
            },
            navigationIcon = {
                if (showBackButton && !isSearchActive) {
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onBack()
                    }) {
                        Icon(
                            painterResource(R.drawable.ic_back_custom),
                            null,
                            tint = settings.palette.textPrimary
                        )
                    }
                }
            },
            actions = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(end = 10.dp)
                ) {
                    val rotation by animateFloatAsState(
                        targetValue = if (isSearchActive) 90f else 0f,
                        animationSpec = spring(dampingRatio = 0.7f, stiffness = 300f),
                        label = "searchRot"
                    )
                    GlassCircleButton(
                        painter = painterResource(if (isSearchActive) R.drawable.ic_close_custom else R.drawable.ic_search_custom),
                        description = "Search",
                        rotation = rotation,
                        size = 38.dp,
                        onClick = onToggleSearch
                    )
                    if (!isSearchActive && showDice) {
                        val context = LocalContext.current
                        DiceRollButton(size = 38.dp) {
                            val nextTheme = AppTheme.entries.filter { it != settings.appTheme }.random()
                            setThemeMode(context, ThemeMode.entries.random())
                            setAppTheme(context, nextTheme)
                        }
                    }
                    if (!isSearchActive && showExtraActions) {
                        GlassCircleButton(
                            painter = painterResource(if (currentLayout == LayoutMode.LIST) R.drawable.ic_grid_custom else R.drawable.ic_list_custom),
                            description = "Layout", size = 38.dp, onClick = onToggleLayout
                        )
                        GlassCircleButton(
                            painter = painterResource(R.drawable.ic_filter_custom),
                            description = "Filter", size = 38.dp, onClick = onShowFilter
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                scrolledContainerColor = Color.Transparent
            )
        )
    }
}

@androidx.media3.common.util.UnstableApi
@Composable
fun DashboardScreen(
    recentEntries: List<RecentEntry>,
    allVideos: List<VideoFile>,
    allFolders: List<VideoFolder>,
    isSearchActive: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onToggleSearch: () -> Unit,
    onVideoClick: (VideoFile) -> Unit,
    onVideoChanged: () -> Unit = {},
    onPlayUrl: (String) -> Unit = {},
    userName: String? = null,
    onThemeModeChange: (ThemeMode) -> Unit = {},
    accentOptions: List<Color> = DefaultAccentOptions,
    onAccentChange: (Color) -> Unit = {}
) {
    var actionVideo by remember { mutableStateOf<VideoFile?>(null) }
    var activeDialog by remember { mutableStateOf<VideoActionDialog?>(null) }
    var expandedVideoId by remember { mutableStateOf<Long?>(null) }

    var sortOrder      by remember { mutableStateOf(VideoSortOrder.DATE_NEWEST) }
    var layoutMode     by remember { mutableStateOf(LayoutMode.LIST) }
    var showUrlDialog  by remember { mutableStateOf(false) }
    var showFilterSheet by remember { mutableStateOf(false) }
    var selectedQuickFilter by remember { mutableStateOf("For you") }

    val context = LocalContext.current
    val scrollState = androidx.compose.foundation.lazy.rememberLazyListState()
    val isScrolled by remember {
        derivedStateOf { scrollState.firstVisibleItemIndex > 0 || scrollState.firstVisibleItemScrollOffset > 0 }
    }

    val filteredVideos by remember(allVideos, searchQuery, sortOrder, selectedQuickFilter) {
        derivedStateOf {
            val prefs = context.getSharedPreferences("playback_positions", Context.MODE_PRIVATE)
            val baseSearch = if (searchQuery.isEmpty()) allVideos
            else allVideos.filter { it.name.contains(searchQuery, ignoreCase = true) }

            val base = when (selectedQuickFilter) {
                "New" -> baseSearch.filter { prefs.getLong(it.path, 0L) == 0L }
                "Quick" -> baseSearch.filter { it.duration in 1L..120_000L }
                "4K" -> baseSearch.filter { it.width >= 3840 || it.height >= 2160 }
                "Long" -> baseSearch.filter { it.duration >= 600_000L }
                else -> rankSuggested(baseSearch, prefs)
            }

            if (selectedQuickFilter == "For you") return@derivedStateOf base

            when (sortOrder) {
                VideoSortOrder.NAME_ASC -> base.sortedBy { it.name.lowercase() }
                VideoSortOrder.NAME_DESC -> base.sortedByDescending { it.name.lowercase() }
                VideoSortOrder.DATE_NEWEST -> base.reversed()
                VideoSortOrder.SIZE_DESC -> base.sortedByDescending { it.size }
                VideoSortOrder.DURATION_DESC -> base.sortedByDescending { it.duration }
            }
        }
    }

    if (showUrlDialog) {
        PlayUrlDialog(
            onDismiss = { showUrlDialog = false },
            onPlay = { url ->
                showUrlDialog = false
                onPlayUrl(url)
            }
        )
    }

    if (showFilterSheet) {
        VideoFilterSortSheet(
            currentSort = sortOrder,
            currentLayout = layoutMode,
            onSortSelected = { sortOrder = it },
            onLayoutSelected = { layoutMode = it },
            onDismiss = { showFilterSheet = false }
        )
    }

    val settings = LocalAppUiSettings.current

    Scaffold(
        topBar = {
            MainTopAppBar(
                title = "CoverPlay",
                showDice = true,
                isSearchActive = isSearchActive,
                searchQuery = searchQuery,
                onSearchQueryChange = onSearchQueryChange,
                onToggleSearch = onToggleSearch,
                currentLayout = layoutMode,
                onToggleLayout = { layoutMode = if (layoutMode == LayoutMode.LIST) LayoutMode.GRID else LayoutMode.LIST },
                onShowFilter = { showFilterSheet = true },
                showExtraActions = false,
                isScrolled = isScrolled
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                state = scrollState,
                modifier = Modifier.glassSource().fillMaxSize().padding(bottom = padding.calculateBottomPadding()),
                contentPadding = PaddingValues(top = padding.calculateTopPadding(), bottom = padding.calculateBottomPadding() + 140.dp)
            ) {
                if (!isSearchActive || searchQuery.isEmpty()) {
                    item {
                        GreetingHeader(
                            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 2.dp),
                            userName = userName,
                            hasRecent = recentEntries.isNotEmpty(),
                            onThemeModeChange = onThemeModeChange,
                            accentOptions = accentOptions,
                            onAccentChange = onAccentChange
                        )
                    }
                    item {
                        Column {
                            SectionLabel("Stream URL")
                            val haptic = LocalHapticFeedback.current
                            PremiumCardSurface(
                                modifier = Modifier
                                    .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 4.dp)
                                    .fillMaxWidth(),
                                cornerRadius = 22.dp,
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    showUrlDialog = true
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    BrandIconContainer(active = true, size = 38.dp, cornerRadius = 12.dp) {
                                        Icon(painterResource(R.drawable.ic_url_custom), null, tint = Color.White, modifier = Modifier.size(20.dp))
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column {
                                        Text("Stream URL", style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold), color = settings.palette.textPrimary)
                                        Text("Play remote network links", style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp), color = settings.palette.textTertiary)
                                    }
                                }
                            }
                        }
                    }

                    if (recentEntries.isNotEmpty()) {
                        item {
                            Column {
                                SectionLabel("Continue Watching", recentEntries.size)
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(recentEntries, key = { it.video.id }) { entry ->
                                        RecentVideoCard(
                                            video = entry.video,
                                            onClick = { onVideoClick(entry.video) },
                                            onRemove = {
                                                removeFromRecents(context, entry.video.id, recentEntries as MutableList<RecentEntry>)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                    item {
                        Column {
                            SectionLabel("Stats")
                            UsageStatsSection()
                        }
                    }
                    item {
                        Column {
                            SectionHeader(title = "Suggested", count = filteredVideos.size, subtitle = "Smart picks from your library")
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("For you", "New", "Quick", "4K", "Long").forEach { chip ->
                                    item {
                                        GlassChip(
                                            onClick = { selectedQuickFilter = chip },
                                            selected = selectedQuickFilter == chip
                                        ) {
                                            Text(
                                                text = chip,
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                                color = if (selectedQuickFilter == chip) Color.White else settings.palette.textSecondary,
                                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                val heroVideo = if (selectedQuickFilter == "For you" && searchQuery.isEmpty()) filteredVideos.firstOrNull() else null
                val listVideos = if (heroVideo != null) filteredVideos.drop(1) else filteredVideos
                val isScrollInProgress = scrollState.isScrollInProgress
                if (heroVideo != null) {
                    item(key = "suggested_hero_${heroVideo.id}") {
                        SuggestedHero(video = heroVideo, onClick = { onVideoClick(heroVideo) }, onLongClick = { actionVideo = heroVideo })
                    }
                }
                if (filteredVideos.isEmpty()) {
                    item {
                        NoResultsScene(
                            query = searchQuery,
                            modifier = Modifier.fillMaxWidth().heightIn(min = 420.dp)
                        )
                    }
                } else {
                    if (layoutMode == LayoutMode.LIST) {
                        items(listVideos, key = { it.id }) { video ->
                            VideoItem(
                                video = video,
                                isExpanded = expandedVideoId == video.id,
                                onToggle = {
                                    expandedVideoId = if (expandedVideoId == video.id) null else video.id
                                },
                                onClick = { onVideoClick(video) },
                                onLongClick = { actionVideo = video },
                                isScrollInProgress = isScrollInProgress
                            )
                        }
                    } else {
                        items(listVideos.chunked(2), key = { pair -> pair.first().id }) { rowVideos ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 7.dp),
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                for (video in rowVideos) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        VideoGridItem(video = video, onClick = { onVideoClick(video) }, onLongClick = { actionVideo = video })
                                    }
                                }
                                if (rowVideos.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
            VideoActionsHost(actionVideo = actionVideo, activeDialog = activeDialog, allFolders = allFolders, onDismiss = { actionVideo = null; activeDialog = null }, onPickDialog = { activeDialog = it }, onVideoChanged = { expandedVideoId = null; onVideoChanged() }, onPlayVideo = onVideoClick)
        }
    }
}

@androidx.media3.common.util.UnstableApi
@Composable
fun RecentVideoCard(
    video: VideoFile,
    onClick: () -> Unit,
    onRemove: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settings = LocalAppUiSettings.current
    val haptic = LocalHapticFeedback.current
    val prefs = context.getSharedPreferences("playback_positions", Context.MODE_PRIVATE)
    val savedPos = remember(video.path) { prefs.getLong(video.path, 0L) }
    val progress = if (video.duration > 0 && savedPos > 0) (savedPos.toFloat() / video.duration.toFloat()).coerceIn(0.03f, 1f) else 0f
    val leftMs = (video.duration - savedPos).coerceAtLeast(0L)
    val leftMin = (leftMs / 60000L).toInt()
    val leftStr = if (leftMin >= 60) "${leftMin / 60}h ${leftMin % 60}m left" else "${leftMin.coerceAtLeast(1)}m left"

    PremiumCardSurface(
        modifier = modifier.width(236.dp).staggerIn(0),
        onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); onClick() },
        onLongClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); onRemove() },
        cornerRadius = 22.dp
    ) {
        Column {
            Box(Modifier.fillMaxWidth().aspectRatio(16 / 9f)) {
                if (settings.hideThumbnails) {
                    TriangleThumbPlaceholder(modifier = Modifier.fillMaxSize())
                } else {
                    AsyncImage(
                        model = ImageRequest.Builder(context).data(video.uri).videoFrameMillis(1000).crossfade(true).build(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Box(
                    Modifier.fillMaxSize().background(
                        Brush.verticalGradient(
                            0f to Color.Black.copy(alpha = 0.28f),
                            0.35f to Color.Transparent,
                            1f to Color.Black.copy(alpha = 0.62f)
                        )
                    )
                )
                // top: resolution tier (left) · dismiss (right)
                Row(
                    Modifier.fillMaxWidth().align(Alignment.TopStart).padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ResolutionMark(video.width, video.height)
                    Spacer(Modifier.weight(1f))
                    Box(
                        Modifier.size(20.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.34f)).aliveClickable { onRemove() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(painterResource(R.drawable.ic_close_custom), "Remove from history", tint = Color.White.copy(alpha = 0.85f), modifier = Modifier.size(10.dp))
                    }
                }
                // bottom: play affordance (left) · time left / duration (right)
                Row(
                    Modifier.fillMaxWidth().align(Alignment.BottomStart).padding(start = 10.dp, end = 10.dp, bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(Modifier.weight(1f))
                    MediaPill(if (savedPos > 0L) leftStr else formatDuration(video.duration))
                }
                if (progress > 0f) SleekProgress(progress, Modifier.align(Alignment.BottomCenter))
            }
            Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                Text(
                    text = remember(video.path, video.name) { cardTitle(video) },
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
                    color = settings.palette.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (savedPos > 0L) {
                        Text(
                            "Resume ${formatDuration(savedPos)}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = settings.accent
                        )
                    } else {
                        NewMark(onMedia = false)
                    }
                }
            }
        }
    }
}

@Composable
fun VideoGridItem(video: VideoFile, onClick: () -> Unit, onLongClick: () -> Unit = {}) {
    val context = LocalContext.current
    val settings = LocalAppUiSettings.current
    val prefs = context.getSharedPreferences("playback_positions", Context.MODE_PRIVATE)
    val savedPos = remember(video.path) { prefs.getLong(video.path, 0L) }
    val progress = if (video.duration > 0 && savedPos > 0) (savedPos.toFloat() / video.duration.toFloat()).coerceIn(0.04f, 1f) else 0f

    PremiumCardSurface(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        onLongClick = onLongClick,
        cornerRadius = 28.dp
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().aspectRatio(16/9f)) {
                if (LocalAppUiSettings.current.hideThumbnails) {
                    TriangleThumbPlaceholder(modifier = Modifier.fillMaxSize())
                } else {
                    AsyncImage(
                        model = ImageRequest.Builder(context).data(video.uri).videoFrameMillis(1000).crossfade(true).build(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Row(Modifier.fillMaxWidth().align(Alignment.TopStart).padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    ResolutionMark(video.width, video.height)
                    Spacer(Modifier.weight(1f))
                    if (savedPos == 0L) NewMark()
                }
                if (LocalAppUiSettings.current.showDurationBadge) {
                    DurationBadge(
                        text = formatDuration(video.duration),
                        modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp)
                    )
                }
                if (progress > 0f) SleekProgress(progress, Modifier.align(Alignment.BottomCenter))
            }
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                Text(
                    text = remember(video.path, video.name, video.dateAdded) { cardTitle(video) },
                    style = MaterialTheme.typography.bodyMedium,
                    color = LocalAppUiSettings.current.palette.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(2.dp))
                val isMovie = isMovieOrSeries(video)
                Text(
                    text = if (isMovie) {
                        if (progress > 0f) "${(progress * 100).toInt()}% watched · ${formatSize(video.size)}" else formatSize(video.size)
                    } else {
                        if (progress > 0f) "${(progress * 100).toInt()}% watched · ${formatFileName(video.name)}" else formatFileName(video.name)
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = LocalAppUiSettings.current.palette.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun FolderGridItem(folder: VideoFolder, thumbVideo: VideoFile? = null, totalSizeStr: String = "", onClick: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    PremiumCardSurface(
        modifier = Modifier.fillMaxWidth().aspectRatio(1f),
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        cornerRadius = 20.dp
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (thumbVideo != null && !LocalAppUiSettings.current.hideThumbnails) {
                AsyncImage(
                    model = ImageRequest.Builder(context).data(thumbVideo.uri).videoFrameMillis(1000).crossfade(true).build(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f)))
            }
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                BrandIconContainer(active = true, size = 56.dp, cornerRadius = 16.dp) {
                    Icon(painterResource(R.drawable.ic_folder_custom), null, tint = Color.White, modifier = Modifier.size(28.dp))
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = folder.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (totalSizeStr.isNotEmpty()) "${folder.videoCount} Videos · $totalSizeStr" else "${folder.videoCount} Videos",
                    style = MaterialTheme.typography.labelSmall,
                    color = LocalAppUiSettings.current.palette.textSecondary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@androidx.media3.common.util.UnstableApi
@Composable
fun FolderListScreen(
    folders: List<VideoFolder>,
    allVideos: List<VideoFile>,
    isSearchActive: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onToggleSearch: () -> Unit,
    onFolderClick: (VideoFolder) -> Unit,
    onVideoClick: (VideoFile) -> Unit,
    onVideoChanged: () -> Unit = {},
    onPlayUrl: (String) -> Unit = {}
) {
    var sortOrder      by remember { mutableStateOf(FolderSortOrder.NAME_ASC) }
    var layoutMode     by remember { mutableStateOf(LayoutMode.LIST) }
    var showFilterSheet by remember { mutableStateOf(false) }

    val scrollState = androidx.compose.foundation.lazy.rememberLazyListState()
    val isScrolled by remember {
        derivedStateOf { scrollState.firstVisibleItemIndex > 0 || scrollState.firstVisibleItemScrollOffset > 0 }
    }

    if (showFilterSheet) {
        FilterSortSheet(
            currentSort = sortOrder,
            currentLayout = layoutMode,
            onSortSelected = { sortOrder = it },
            onLayoutSelected = { layoutMode = it },
            onDismiss = { showFilterSheet = false }
        )
    }

    Scaffold(
        topBar = {
            MainTopAppBar(
                title = "Folders",
                isSearchActive = isSearchActive,
                searchQuery = searchQuery,
                onSearchQueryChange = onSearchQueryChange,
                onToggleSearch = onToggleSearch,
                currentLayout = layoutMode,
                onToggleLayout = { layoutMode = if (layoutMode == LayoutMode.LIST) LayoutMode.GRID else LayoutMode.LIST },
                onShowFilter = { showFilterSheet = true },
                isScrolled = isScrolled
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        if (folders.isEmpty()) {
            EmptyState(title = "No Folders", subtitle = "Scanning for videos...") {
                CircularProgressIndicator(color = BrandAccent)
            }
        } else {
            val filteredFolders by remember(folders, searchQuery, sortOrder) {
                derivedStateOf {
                    val base = if (searchQuery.isEmpty()) folders
                    else folders.filter { it.name.contains(searchQuery, ignoreCase = true) }

                    when (sortOrder) {
                        FolderSortOrder.NAME_ASC -> base.sortedBy { it.name.lowercase() }
                        FolderSortOrder.NAME_DESC -> base.sortedByDescending { it.name.lowercase() }
                        FolderSortOrder.MOST_VIDEOS -> base.sortedByDescending { it.videoCount }
                        FolderSortOrder.FEWEST_VIDEOS -> base.sortedBy { it.videoCount }
                    }
                }
            }
            Box(modifier = Modifier.fillMaxSize()) {
                if (filteredFolders.isEmpty() && searchQuery.isNotEmpty()) {
                    NoResultsScene(query = searchQuery, modifier = Modifier.fillMaxSize().padding(padding))
                }
                if (layoutMode == LayoutMode.LIST) {
                    LazyColumn(
                        state = scrollState,
                        modifier = Modifier.glassSource().fillMaxSize().padding(bottom = padding.calculateBottomPadding()),
                        contentPadding = PaddingValues(top = padding.calculateTopPadding(), bottom = padding.calculateBottomPadding() + 128.dp)
                    ) {
                        if (searchQuery.isEmpty()) {
                            item {
                                val downloadsCount = remember(allVideos) { allVideos.count { it.path.contains("download", ignoreCase = true) } }
                                val cameraCount = remember(allVideos) { allVideos.count { it.path.contains("camera", ignoreCase = true) || it.path.contains("dcim", ignoreCase = true) } }
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    item {
                                        GlassChip(
                                            onClick = { onFolderClick(VideoFolder("All Media", "/", allVideos.size, "")) }
                                        ) {
                                            Text(
                                                text = "All",
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                color = LocalAppUiSettings.current.palette.textPrimary,
                                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                            )
                                        }
                                    }
                                    item {
                                        GlassChip(onClick = { onFolderClick(VideoFolder("Recent Videos", "/recent", minOf(allVideos.size, 30), "")) }) {
                                            Text(
                                                text = "Recent",
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                color = LocalAppUiSettings.current.palette.textPrimary,
                                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                            )
                                        }
                                    }
                                    if (downloadsCount > 0) {
                                        item {
                                            GlassChip(onClick = { onFolderClick(VideoFolder("Downloads", "/download", downloadsCount, "")) }) {
                                                Text(
                                                    text = "Downloads",
                                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = LocalAppUiSettings.current.palette.textPrimary,
                                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                                )
                                            }
                                        }
                                    }
                                    if (cameraCount > 0) {
                                        item {
                                            GlassChip(onClick = { onFolderClick(VideoFolder("Camera", "/camera", cameraCount, "")) }) {
                                                Text(
                                                    text = "Camera",
                                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = LocalAppUiSettings.current.palette.textPrimary,
                                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        itemsIndexed(filteredFolders, key = { _, it -> it.path }) { index, folder ->
                            val entryAnimation = remember { Animatable(0f) }
                            LaunchedEffect(Unit) {
                                delay(index * 30L)
                                entryAnimation.animateTo(1f, animationSpec = MotionSpec.SoftSpring)
                            }
                            val fVideos = remember(folder.path, allVideos) {
                                allVideos.filter { File(it.path).parent == folder.path || it.path.startsWith(folder.path) }
                            }
                            val thumbVideo = fVideos.firstOrNull()
                            val fSizeStr = remember(fVideos) { formatSize(fVideos.sumOf { it.size }) }

                            ListRowSurface(
                                modifier = Modifier
                                    .graphicsLayer {
                                        translationY = 30f * (1f - entryAnimation.value)
                                        alpha = entryAnimation.value
                                    },
                                onClick = { onFolderClick(folder) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (thumbVideo != null && !LocalAppUiSettings.current.hideThumbnails) {
                                        Box(
                                            modifier = Modifier
                                                .size(52.dp)
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(LocalAppUiSettings.current.palette.surfaceRaisedAlt)
                                        ) {
                                            val context = LocalContext.current
                                            AsyncImage(
                                                model = remember(thumbVideo.uri) {
                                                    ImageRequest.Builder(context).data(thumbVideo.uri).videoFrameMillis(1000).crossfade(true).build()
                                                },
                                                contentDescription = null,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                    } else {
                                        BrandIconContainer(active = true, cornerRadius = 14.dp, size = 52.dp) {
                                            Icon(painterResource(R.drawable.ic_folder_custom), null, tint = Color.White, modifier = Modifier.size(24.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = folder.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = LocalAppUiSettings.current.palette.textPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${folder.videoCount} Videos · $fSizeStr",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = LocalAppUiSettings.current.palette.textSecondary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Icon(
                                        painterResource(R.drawable.ic_chevron_right_custom),
                                        null,
                                        tint = LocalAppUiSettings.current.palette.textTertiary.copy(alpha = 0.6f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        state = androidx.compose.foundation.lazy.grid.rememberLazyGridState(),
                        modifier = Modifier.glassSource().fillMaxSize().padding(bottom = padding.calculateBottomPadding()),
                        contentPadding = PaddingValues(top = padding.calculateTopPadding(), start = 16.dp, end = 16.dp, bottom = padding.calculateBottomPadding() + 128.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (searchQuery.isEmpty()) {
                            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                                val downloadsCount = remember(allVideos) { allVideos.count { it.path.contains("download", ignoreCase = true) } }
                                val cameraCount = remember(allVideos) { allVideos.count { it.path.contains("camera", ignoreCase = true) || it.path.contains("dcim", ignoreCase = true) } }
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    item {
                                        GlassChip(onClick = { onFolderClick(VideoFolder("All Media", "/", allVideos.size, "")) }) {
                                            Text("All", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = LocalAppUiSettings.current.palette.textPrimary, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                                        }
                                    }
                                    item {
                                        GlassChip(onClick = { onFolderClick(VideoFolder("Recent Videos", "/recent", minOf(allVideos.size, 30), "")) }) {
                                            Text("Recent", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = LocalAppUiSettings.current.palette.textPrimary, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                                        }
                                    }
                                    if (downloadsCount > 0) {
                                        item {
                                            GlassChip(onClick = { onFolderClick(VideoFolder("Downloads", "/download", downloadsCount, "")) }) {
                                                Text("Downloads", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = LocalAppUiSettings.current.palette.textPrimary, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                                            }
                                        }
                                    }
                                    if (cameraCount > 0) {
                                        item {
                                            GlassChip(onClick = { onFolderClick(VideoFolder("Camera", "/camera", cameraCount, "")) }) {
                                                Text("Camera", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = LocalAppUiSettings.current.palette.textPrimary, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        items(filteredFolders, key = { it.path }) { folder ->
                            val fVideos = remember(folder.path, allVideos) {
                                allVideos.filter { File(it.path).parent == folder.path || it.path.startsWith(folder.path) }
                            }
                            val thumbVideo = fVideos.firstOrNull()
                            val fSizeStr = remember(fVideos) { formatSize(fVideos.sumOf { it.size }) }
                            FolderGridItem(folder = folder, thumbVideo = thumbVideo, totalSizeStr = fSizeStr, onClick = { onFolderClick(folder) })
                        }
                    }
                }
            }
        }
    }
}

@androidx.media3.common.util.UnstableApi
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun VideoItem(
    video: VideoFile,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    isScrollInProgress: Boolean = false
) {
    val settings = LocalAppUiSettings.current
    val isDark = settings.palette.isDark
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    var frameRate by remember(video.uri) { mutableStateOf<String?>(null) }
    LaunchedEffect(isExpanded) {
        if (isExpanded && frameRate == null) {
            withContext(Dispatchers.IO) {
                try {
                    val retriever = android.media.MediaMetadataRetriever()
                    retriever.setDataSource(context, video.uri)
                    val fps = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE)
                        ?: retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_FRAME_COUNT)?.let { count ->
                            retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)?.let { dur ->
                                val d = dur.toFloat()
                                if (d > 0) (count.toFloat() / (d / 1000f)).toInt().toString() else null
                            }
                        }
                    frameRate = fps?.let {
                        val f = it.toFloatOrNull()
                        if (f != null) String.format("%.1f FPS", f) else "$it FPS"
                    } ?: "30 FPS"
                    retriever.release()
                } catch (e: Exception) { frameRate = "30 FPS" }
            }
        }
    }

    AnimatedContent(
        targetState = isExpanded,
        transitionSpec = {
            (fadeIn(tween(250, easing = FastOutSlowInEasing)) +
                    scaleIn(
                        initialScale = 0.96f,
                        animationSpec = spring(dampingRatio = 0.85f, stiffness = 380f)
                    ))
                .togetherWith(
                    fadeOut(tween(180, easing = FastOutSlowInEasing)) +
                            scaleOut(targetScale = 0.96f, animationSpec = tween(180))
                )
        },
        label = "videoCardMorph"
    ) { isExpanded ->
        VideoRowShell(
            isExpanded = isExpanded,
            onClick = {
                if (isExpanded) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onToggle()
                } else {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                }
            },
            onLongClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onLongClick()
            }
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (isExpanded) {
                    var player by remember { mutableStateOf<ExoPlayer?>(null) }
                    var videoAlpha by remember { mutableFloatStateOf(0f) }
                    val animatedAlpha by animateFloatAsState(targetValue = videoAlpha, animationSpec = tween(700), label = "videoFade")

                    var videoWidth by remember { mutableIntStateOf(video.width) }
                    var videoHeight by remember { mutableIntStateOf(video.height) }
                    var videoDuration by remember { mutableLongStateOf(video.duration) }

                    LaunchedEffect(isExpanded) {
                        if (isExpanded && (videoWidth == 0 || videoDuration == 0L)) {
                            withContext(Dispatchers.IO) {
                                try {
                                    val retriever = android.media.MediaMetadataRetriever()
                                    retriever.setDataSource(context, video.uri)
                                    val w = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toInt() ?: 0
                                    val h = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toInt() ?: 0
                                    val d = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: 0L
                                    videoWidth = w
                                    videoHeight = h
                                    videoDuration = d
                                    retriever.release()
                                } catch (e: Exception) {}
                            }
                        }
                    }

                    DisposableEffect(video.uri) {
                        val exoPlayer = ExoPlayer.Builder(context).build().apply {
                            setMediaItem(MediaItem.fromUri(video.uri))
                            volume = 0f
                            repeatMode = Player.REPEAT_MODE_ALL
                            playWhenReady = true
                            addListener(object : Player.Listener {
                                override fun onPlaybackStateChanged(state: Int) {
                                    if (state == Player.STATE_READY) videoAlpha = 1f
                                }
                            })
                            prepare()
                        }
                        player = exoPlayer
                        onDispose { exoPlayer.release(); player = null }
                    }

                    LaunchedEffect(isScrollInProgress, player) {
                        if (isScrollInProgress) {
                            player?.pause()
                        } else {
                            if (isExpanded) player?.play()
                        }
                    }

                    Column(modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 14.dp)) {
                        // ── Hero: live preview, soft cinematic fade, title + one quiet caption on the glass ──
                        val heroShape = RoundedCornerShape(18.dp)
                        val resTier = if (videoWidth > 0 && videoHeight > 0) qualityLabel(videoWidth, videoHeight) else "—"
                        val fpsLabel = (frameRate ?: "30 FPS").replace(" FPS", "")
                        val dimsLabel = if (videoWidth > 0 && videoHeight > 0) "${videoWidth}×${videoHeight}" else null
                        val dateLabel = remember(video.path, video.dateAdded) { videoDateTitle(video) }
                        val folderLabel = remember(video.path) { File(video.path).parentFile?.name ?: "Folder" }
                        val expPrefs = remember { context.getSharedPreferences("playback_positions", Context.MODE_PRIVATE) }
                        val resumePos = remember(video.path) { expPrefs.getLong(video.path, 0L) }
                        val resumeFrac = if (videoDuration > 0 && resumePos > 0) (resumePos.toFloat() / videoDuration.toFloat()).coerceIn(0.02f, 1f) else 0f

                        // Hero opens like a lid: fades, scales up and settles from the top edge.
                        val reveal = remember { Animatable(0f) }
                        LaunchedEffect(Unit) {
                            kotlinx.coroutines.delay(90)
                            reveal.animateTo(1f, tween(560, easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)))
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16 / 9f)
                                .graphicsLayer {
                                    val v = reveal.value
                                    alpha = v
                                    val sc = 0.93f + 0.07f * v
                                    scaleX = sc; scaleY = sc
                                    translationY = (1f - v) * -18f
                                    transformOrigin = TransformOrigin(0.5f, 0f)
                                }
                                .shadow(14.dp, heroShape, ambientColor = settings.accent.copy(alpha = 0.18f), spotColor = Color.Black.copy(alpha = 0.45f))
                                .clip(heroShape)
                                .background(if (isDark) Color.Black.copy(0.3f) else settings.palette.bg)
                        ) {
                            if (settings.hideThumbnails) {
                                TriangleThumbPlaceholder(modifier = Modifier.fillMaxSize())
                            } else {
                                AsyncImage(
                                    model = ImageRequest.Builder(context).data(video.uri).videoFrameMillis(1000).crossfade(true).build(),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                AndroidView(
                                    factory = { ctx ->
                                        (android.view.LayoutInflater.from(ctx)
                                            .inflate(R.layout.coverplay_texture_player, null) as PlayerView).apply {
                                            useController = false
                                            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                                            this.player = player
                                        }
                                    },
                                    update = { view ->
                                        if (view.player != player) view.player = player
                                        view.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                                    },
                                    modifier = Modifier.fillMaxSize().graphicsLayer { alpha = animatedAlpha }
                                )
                            }

                            // Cinematic fade: light at the top for the badge, deeper at the bottom for the title.
                            Box(
                                Modifier.fillMaxSize().background(
                                    Brush.verticalGradient(
                                        0f to Color.Black.copy(alpha = 0.30f),
                                        0.38f to Color.Transparent,
                                        0.55f to Color.Black.copy(alpha = 0.10f),
                                        1f to Color.Black.copy(alpha = 0.82f)
                                    )
                                )
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter).padding(start = 12.dp, end = 4.dp, top = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ResolutionMark(videoWidth, videoHeight)
                                Spacer(Modifier.weight(1f))
                                IconButton(onClick = onToggle, modifier = Modifier.size(40.dp)) {
                                    Icon(
                                        painterResource(R.drawable.ic_chevron_up_custom), "Collapse",
                                        tint = Color.White.copy(alpha = 0.65f)
                                    )
                                }
                            }

                            Column(
                                Modifier.align(Alignment.BottomStart).padding(start = 14.dp, end = 14.dp, bottom = 14.dp)
                            ) {
                                Text(
                                    text = video.name,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontSize = 16.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.1).sp
                                    ),
                                    color = Color.White,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(Modifier.height(3.dp))
                                Text(
                                    text = listOfNotNull(dimsLabel, folderLabel, dateLabel).joinToString("  ·  "),
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    color = Color.White.copy(alpha = 0.72f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            if (resumeFrac > 0f) SleekProgress(resumeFrac, Modifier.align(Alignment.BottomCenter))
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        MetaStatRow(
                            listOf(
                                MetaStatItem(painterResource(R.drawable.ic_stopwatch_custom), "TIME", formatDuration(videoDuration)),
                                MetaStatItem(painterResource(R.drawable.ic_code_circle_custom), "RES", resTier),
                                MetaStatItem(painterResource(R.drawable.ic_frame_rate), "FPS", fpsLabel),
                                MetaStatItem(painterResource(R.drawable.ic_description_custom), "SIZE", formatSize(video.size))
                            ),
                            modifier = Modifier.staggerIn(3)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Primary Action: "Play" + folder + more
                        Row(
                            modifier = Modifier.fillMaxWidth().staggerIn(5),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            PremiumButton(
                                text = if (resumePos > 0L) "Resume ${formatDuration(resumePos)}" else "Play",
                                onClick = onClick,
                                modifier = Modifier.weight(1f),
                                icon = { Icon(Icons.Rounded.PlayArrow, null, tint = Color.White, modifier = Modifier.size(22.dp)) }
                            )
                            QuietIconButton(onClick = onLongClick) {
                                Icon(
                                    Icons.Rounded.MoreVert,
                                    contentDescription = "More",
                                    tint = settings.palette.textPrimary,
                                    modifier = Modifier.size(20.dp).align(Alignment.Center)
                                )
                            }
                        }
                    }
                } else {
                    // Collapsed Normal Row (~72dp height)
                    val prefs = context.getSharedPreferences("playback_positions", Context.MODE_PRIVATE)
                    val savedPos = remember(video.path) { prefs.getLong(video.path, 0L) }
                    val progress = if (video.duration > 0 && savedPos > 0) (savedPos.toFloat() / video.duration.toFloat()).coerceIn(0.02f, 1f) else 0f

                    Row(
                        modifier = Modifier.height(if (settings.compactList) 56.dp else 72.dp).padding(horizontal = 10.dp, vertical = if (settings.compactList) 5.dp else 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(if (settings.compactList) 68.dp else 88.dp, if (settings.compactList) 42.dp else 56.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isDark) settings.palette.surfaceRaisedAlt else settings.palette.bg)
                        ) {
                            if (settings.hideThumbnails) {
                                TriangleThumbPlaceholder(modifier = Modifier.fillMaxSize())
                            } else {
                                AsyncImage(
                                    model = ImageRequest.Builder(context).data(video.uri).videoFrameMillis(1000).crossfade(true).build(),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            // Duration badge ONLY — NO play button overlay
                            if (settings.showDurationBadge) {
                                DurationBadge(
                                    text = formatDuration(video.duration),
                                    modifier = Modifier.align(Alignment.BottomEnd).padding(4.dp)
                                )
                            }
                            if (progress > 0f) {
                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(3.dp),
                                    color = settings.accent,
                                    trackColor = Color.Black.copy(alpha = 0.4f)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(
                            modifier = Modifier
                                .weight(1f)
                        ) {
                            Text(
                                text = remember(video.path, video.name, video.dateAdded) { cardTitle(video) },
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = settings.palette.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(5.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (savedPos == 0L) NewMark(onMedia = false)
                                else Text(
                                    "${(progress * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = settings.accent
                                )
                                ResolutionMark(video.width, video.height, onMedia = false)
                                Text(
                                    formatSize(video.size),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = settings.palette.textTertiary,
                                    maxLines = 1
                                )
                            }
                        }
                        IconButton(onClick = onToggle) {
                            Icon(
                                painter = painterResource(R.drawable.ic_chevron_down_custom),
                                contentDescription = "Expand",
                                tint = settings.palette.textTertiary
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Collapsed = quiet list row; expanded = the same solid-glass card used by Stream URL and Stats. */
@Composable
private fun VideoRowShell(
    isExpanded: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    content: @Composable () -> Unit
) {
    if (isExpanded) {
        // "Stack" opening: two ghost cards fan out from underneath the card as it opens,
        // like a deck being dealt. Drawn behind the card, so it costs no extra layout.
        val st = LocalAppUiSettings.current
        var open by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { open = true }
        val stack by animateFloatAsState(
            targetValue = if (open) 1f else 0f,
            animationSpec = spring(dampingRatio = 0.74f, stiffness = 210f),
            label = "cardStack"
        )
        val layerFill = st.palette.surfaceRaised
        val layerEdge = st.accent
        PremiumCardSurface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 8.dp + 16.dp * stack)
                .staggerIn(0)
                .drawBehind {
                    val p = stack.coerceIn(0f, 1f)
                    val corner = 22.dp.toPx()
                    for (i in 2 downTo 1) {
                        val inset = 14.dp.toPx() * i
                        val drop = 8.dp.toPx() * i * stack
                        val top = size.height - corner
                        val w = size.width - inset * 2f
                        val h = corner + drop
                        val a = (0.78f - 0.28f * (i - 1)) * p
                        drawRoundRect(
                            layerFill.copy(alpha = a),
                            androidx.compose.ui.geometry.Offset(inset, top),
                            androidx.compose.ui.geometry.Size(w, h),
                            androidx.compose.ui.geometry.CornerRadius(corner)
                        )
                        drawRoundRect(
                            layerEdge.copy(alpha = 0.16f * p),
                            androidx.compose.ui.geometry.Offset(inset, top),
                            androidx.compose.ui.geometry.Size(w, h),
                            androidx.compose.ui.geometry.CornerRadius(corner),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(1.dp.toPx())
                        )
                    }
                },
            cornerRadius = 22.dp,
            onClick = onClick,
            onLongClick = onLongClick
        ) { content() }
    } else {
        ListRowSurface(
            modifier = Modifier.fillMaxWidth().staggerIn(0),
            onClick = onClick,
            onLongClick = onLongClick
        ) { content() }
    }
}

// ── Suggested: smart ranking + a premium "Top pick" hero ─────────────────────────────────────────

/** Ranks the library for "For you": unwatched + fresh first, a nudge for unfinished, a daily shuffle so it feels alive. */
private fun rankSuggested(videos: List<VideoFile>, prefs: android.content.SharedPreferences): List<VideoFile> {
    if (videos.isEmpty()) return videos
    val day = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_YEAR)
    val newest = videos.maxOf { it.dateAdded }
    val oldest = videos.minOf { it.dateAdded }
    val span = (newest - oldest).coerceAtLeast(1L).toFloat()
    return videos.sortedByDescending { v ->
        val saved = prefs.getLong(v.path, 0L)
        val frac = if (v.duration > 0L && saved > 0L) saved.toFloat() / v.duration.toFloat() else 0f
        var score = 0f
        if (saved == 0L) score += 40f
        score += 30f * ((v.dateAdded - oldest).toFloat() / span)
        if (v.duration in 60_000L..600_000L) score += 10f
        if (v.width >= 3840 || v.height >= 2160) score += 8f
        if (frac >= 0.95f) score -= 50f else if (frac >= 0.05f) score += 12f
        score += ((v.id.hashCode() * 31 + day * 17) and 0xFF) / 255f * 15f
        score
    }
}

private fun suggestionReason(v: VideoFile, savedPos: Long): String {
    val frac = if (v.duration > 0L && savedPos > 0L) savedPos.toFloat() / v.duration.toFloat() else 0f
    val ageDays = (System.currentTimeMillis() / 1000L - v.dateAdded) / 86_400L
    return when {
        frac >= 0.5f -> "Almost finished"
        frac > 0f -> "Pick up again"
        v.dateAdded > 0L && ageDays <= 7L -> "Fresh arrival"
        v.width >= 3840 || v.height >= 2160 -> "4K showcase"
        v.duration in 1L..120_000L -> "Quick watch"
        else -> "Not watched yet"
    }
}

@Composable
private fun SuggestedHero(video: VideoFile, onClick: () -> Unit, onLongClick: () -> Unit) {
    val context = LocalContext.current
    val settings = LocalAppUiSettings.current
    val haptic = LocalHapticFeedback.current
    val prefs = remember { context.getSharedPreferences("playback_positions", Context.MODE_PRIVATE) }
    val savedPos = remember(video.path) { prefs.getLong(video.path, 0L) }
    val reason = remember(video.path, savedPos) { suggestionReason(video, savedPos) }
    val progress = if (video.duration > 0L && savedPos > 0L) (savedPos.toFloat() / video.duration.toFloat()).coerceIn(0.03f, 1f) else 0f

    PremiumCardSurface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp).staggerIn(0),
        cornerRadius = 22.dp,
        onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); onClick() },
        onLongClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); onLongClick() }
    ) {
        Box(Modifier.fillMaxWidth().aspectRatio(16 / 9f)) {
            if (settings.hideThumbnails) {
                TriangleThumbPlaceholder(modifier = Modifier.fillMaxSize())
            } else {
                AsyncImage(
                    model = ImageRequest.Builder(context).data(video.uri).videoFrameMillis(1000).crossfade(true).build(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            Box(
                Modifier.fillMaxSize().background(
                    Brush.verticalGradient(
                        0f to Color.Black.copy(alpha = 0.30f),
                        0.40f to Color.Transparent,
                        1f to Color.Black.copy(alpha = 0.82f)
                    )
                )
            )
            Row(
                Modifier.fillMaxWidth().align(Alignment.TopStart).padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier.clip(CircleShape).background(settings.accent.copy(alpha = 0.92f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        "TOP PICK",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp),
                        color = Color.White
                    )
                }
                Spacer(Modifier.weight(1f))
                ResolutionMark(video.width, video.height)
            }
            Column(
                Modifier.align(Alignment.BottomStart).padding(start = 16.dp, end = 76.dp, bottom = 16.dp)
            ) {
                Text(
                    reason.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.4.sp),
                    color = settings.highlight
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = remember(video.path, video.name) { cardTitle(video) },
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.1).sp),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "${formatDuration(video.duration)}  ·  ${formatSize(video.size)}",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = Color.White.copy(alpha = 0.72f),
                    maxLines = 1
                )
            }
            BrandMark(
                size = 52.dp,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 14.dp, bottom = 14.dp)
                    .shadow(12.dp, CircleShape, ambientColor = settings.accent.copy(alpha = 0.25f), spotColor = settings.accent.copy(alpha = 0.50f))
            )
            if (progress > 0f) SleekProgress(progress, Modifier.align(Alignment.BottomCenter))
        }
    }
}

/** Glass action button matching MetaStatRow design for consistency inside expanded video cards. */
@Composable
private fun QuietIconButton(onClick: () -> Unit, content: @Composable BoxScope.() -> Unit) {
    val st = LocalAppUiSettings.current
    val tintBase = if (st.palette.isDark) Color.White else IconIndigo
    val shape = RoundedCornerShape(18.dp)
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(shape)
            .background(Brush.verticalGradient(listOf(tintBase.copy(alpha = 0.07f), tintBase.copy(alpha = 0.03f))))
            .border(0.8.dp, Brush.verticalGradient(listOf(tintBase.copy(alpha = 0.18f), tintBase.copy(alpha = 0.05f))), shape)
            .aliveClickable { onClick() },
        contentAlignment = Alignment.Center,
        content = content
    )
}

@Composable
fun VideoDetailCard(icon: Painter, text: String, color: Color = LocalAppUiSettings.current.accent, modifier: Modifier = Modifier) {
    val settings = LocalAppUiSettings.current
    GlassSurface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = settings.palette.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun VideoDetailMinimal(icon: Painter, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = LocalAppUiSettings.current.accent.copy(alpha = 0.7f), modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = LocalAppUiSettings.current.palette.textSecondary
        )
    }
}


@Composable
fun VideoCompactDetail(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = LocalAppUiSettings.current.palette.textTertiary, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.labelSmall, color = LocalAppUiSettings.current.palette.textSecondary, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun VideoDetailItem(label: String, value: String, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(LocalAppUiSettings.current.palette.surfaceRaisedAlt), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = BrandAccent, modifier = Modifier.size(16.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = LocalAppUiSettings.current.palette.textTertiary)
            Text(value, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = LocalAppUiSettings.current.palette.textPrimary)
        }
    }
}

fun formatDate(seconds: Long): String {
    if (seconds == 0L) return "Unknown"
    val sdf = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(seconds * 1000))
}


@androidx.media3.common.util.UnstableApi
@Composable
fun VideoListScreen(
    folderName: String,
    videos: List<VideoFile>,
    isSearchActive: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onToggleSearch: () -> Unit,
    onVideoClick: (VideoFile) -> Unit,
    onBack: () -> Unit,
    allFolders: List<VideoFolder> = emptyList(),
    onVideoChanged: () -> Unit = {}
) {
    var actionVideo by remember { mutableStateOf<VideoFile?>(null) }
    var activeDialog by remember { mutableStateOf<VideoActionDialog?>(null) }
    var expandedVideoId by remember { mutableStateOf<Long?>(null) }

    var sortOrder      by remember { mutableStateOf(VideoSortOrder.NAME_ASC) }
    var layoutMode     by remember { mutableStateOf(LayoutMode.LIST) }
    var showFilterSheet by remember { mutableStateOf(false) }

    val scrollState = androidx.compose.foundation.lazy.rememberLazyListState()
    val isScrolled by remember {
        derivedStateOf { scrollState.firstVisibleItemIndex > 0 || scrollState.firstVisibleItemScrollOffset > 0 }
    }

    val filtered by remember(videos, searchQuery, sortOrder) {
        derivedStateOf {
            val base = if (searchQuery.isEmpty()) videos
            else videos.filter { it.name.contains(searchQuery, ignoreCase = true) }

            when (sortOrder) {
                VideoSortOrder.NAME_ASC -> base.sortedBy { it.name.lowercase() }
                VideoSortOrder.NAME_DESC -> base.sortedByDescending { it.name.lowercase() }
                VideoSortOrder.DATE_NEWEST -> base.reversed()
                VideoSortOrder.SIZE_DESC -> base.sortedByDescending { it.size }
                VideoSortOrder.DURATION_DESC -> base.sortedByDescending { it.duration }
            }
        }
    }

    if (showFilterSheet) {
        VideoFilterSortSheet(
            currentSort = sortOrder,
            currentLayout = layoutMode,
            onSortSelected = { sortOrder = it },
            onLayoutSelected = { layoutMode = it },
            onDismiss = { showFilterSheet = false }
        )
    }

    Scaffold(
        topBar = {
            MainTopAppBar(
                title = folderName,
                isSearchActive = isSearchActive,
                searchQuery = searchQuery,
                onSearchQueryChange = onSearchQueryChange,
                onToggleSearch = onToggleSearch,
                showBackButton = true,
                onBack = onBack,
                currentLayout = layoutMode,
                onToggleLayout = { layoutMode = if (layoutMode == LayoutMode.LIST) LayoutMode.GRID else LayoutMode.LIST },
                onShowFilter = { showFilterSheet = true },
                isScrolled = isScrolled
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (layoutMode == LayoutMode.LIST) {
                val isScrollInProgress = scrollState.isScrollInProgress
                LazyColumn(
                    state = scrollState,
                    modifier = Modifier.glassSource().fillMaxSize().padding(bottom = padding.calculateBottomPadding()),
                    contentPadding = PaddingValues(top = padding.calculateTopPadding(), bottom = padding.calculateBottomPadding() + 128.dp)
                ) {
                    itemsIndexed(filtered, key = { _, it -> it.id }) { index, video ->
                        val entryAnimation = remember { Animatable(0f) }
                        LaunchedEffect(Unit) {
                            delay(index * 40L)
                            entryAnimation.animateTo(1f, animationSpec = MotionSpec.SoftSpring)
                        }
                        Box(modifier = Modifier.graphicsLayer {
                            translationY = 40f * (1f - entryAnimation.value)
                            alpha = entryAnimation.value
                        }) {
                            VideoItem(
                                video = video,
                                isExpanded = expandedVideoId == video.id,
                                onToggle = {
                                    expandedVideoId = if (expandedVideoId == video.id) null else video.id
                                },
                                onClick = { onVideoClick(video) },
                                onLongClick = { actionVideo = video },
                                isScrollInProgress = isScrollInProgress
                            )
                        }

                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    state = androidx.compose.foundation.lazy.grid.rememberLazyGridState(),
                    modifier = Modifier.glassSource().fillMaxSize().padding(bottom = padding.calculateBottomPadding()),
                    contentPadding = PaddingValues(top = padding.calculateTopPadding(), start = 16.dp, end = 16.dp, bottom = padding.calculateBottomPadding() + 128.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filtered, key = { it.id }) { video ->
                        VideoGridItem(video = video, onClick = { onVideoClick(video) }, onLongClick = { actionVideo = video })
                    }
                }
            }

        }
    }
    VideoActionsHost(actionVideo = actionVideo, activeDialog = activeDialog, allFolders = allFolders, onDismiss = { actionVideo = null; activeDialog = null }, onPickDialog = { activeDialog = it }, onVideoChanged = { expandedVideoId = null; onVideoChanged() }, onPlayVideo = onVideoClick)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSortSheet(currentSort: FolderSortOrder, currentLayout: LayoutMode, onSortSelected: (FolderSortOrder) -> Unit, onLayoutSelected: (LayoutMode) -> Unit, onDismiss: () -> Unit) {
    PremiumBottomSheet(onDismiss = onDismiss) {
        Text("Sort Options", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, color = LocalAppUiSettings.current.palette.textPrimary)
        QuipLine(Quips.dialog("sort options"), modifier = Modifier.padding(top = 2.dp))
        Spacer(modifier = Modifier.height(22.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            FolderSortOrder.entries.forEach { order ->
                val isSelected = order == currentSort
                PremiumCardSurface(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 20.dp,
                    selected = isSelected,
                    onClick = { onSortSelected(order); onDismiss() }
                ) {
                    Row(modifier = Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(order.label, style = MaterialTheme.typography.titleMedium, color = if (isSelected) BrandAccent else LocalAppUiSettings.current.palette.textPrimary, fontWeight = if(isSelected) FontWeight.ExtraBold else FontWeight.Medium)
                        if (isSelected) Icon(painterResource(R.drawable.ic_radio_on_custom), null, tint = BrandAccent, modifier = Modifier.size(24.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoFilterSortSheet(currentSort: VideoSortOrder, currentLayout: LayoutMode, onSortSelected: (VideoSortOrder) -> Unit, onLayoutSelected: (LayoutMode) -> Unit, onDismiss: () -> Unit) {
    PremiumBottomSheet(onDismiss = onDismiss) {
        Text("Order & Layout", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, color = LocalAppUiSettings.current.palette.textPrimary)
        QuipLine(Quips.dialog("order layout sort"), modifier = Modifier.padding(top = 2.dp))
        Spacer(modifier = Modifier.height(22.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            VideoSortOrder.entries.forEach { order ->
                val isSelected = order == currentSort
                PremiumCardSurface(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 20.dp,
                    selected = isSelected,
                    onClick = { onSortSelected(order); onDismiss() }
                ) {
                    Row(modifier = Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(order.label, style = MaterialTheme.typography.titleMedium, color = if (isSelected) BrandAccent else LocalAppUiSettings.current.palette.textPrimary, fontWeight = if(isSelected) FontWeight.ExtraBold else FontWeight.Medium)
                        if (isSelected) Icon(painterResource(R.drawable.ic_radio_on_custom), null, tint = BrandAccent, modifier = Modifier.size(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun VideoActionsHost(actionVideo: VideoFile?, activeDialog: VideoActionDialog?, allFolders: List<VideoFolder>, onDismiss: () -> Unit, onPickDialog: (VideoActionDialog) -> Unit, onVideoChanged: () -> Unit, onPlayVideo: (VideoFile) -> Unit) {
    val video = actionVideo ?: return; val context = LocalContext.current; var pendingRetry by remember { mutableStateOf<(() -> Unit)?>(null) }
    val confirmDeleteOn = remember { context.getSharedPreferences("coverplay_settings_v3", Context.MODE_PRIVATE).getBoolean("confirm_delete", true) }
    val consentLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // On Android 11+, if the user just approved a DELETE request, the file is already gone.
            // We just need to hide the menu and refresh the list.
            if (activeDialog == VideoActionDialog.DELETE) {
                onDismiss()
                onVideoChanged()
            } else {
                pendingRetry?.invoke()
            }
        }
        // Cancelled the system prompt while deleting without our own dialog: close the host so it never gets stuck.
        if (result.resultCode != Activity.RESULT_OK && !confirmDeleteOn && activeDialog == VideoActionDialog.DELETE) onDismiss()
        pendingRetry = null
    }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    fun handleResult(result: FileOpResult, onSuccess: () -> Unit, retry: () -> Unit) {
        when (result) {
            is FileOpResult.Success -> onSuccess()
            is FileOpResult.Error -> errorMessage = result.message
            is FileOpResult.NeedsUserConsent -> {
                pendingRetry = retry
                consentLauncher.launch(androidx.activity.result.IntentSenderRequest.Builder(result.intentSender).build())
            }
        }
    }
    when (activeDialog ?: VideoActionDialog.MENU) {
        VideoActionDialog.MENU -> VideoActionMenuSheet(video = video, onDismiss = onDismiss, onRename = { onPickDialog(VideoActionDialog.RENAME) }, onDelete = { onPickDialog(VideoActionDialog.DELETE) }, onMove = { onPickDialog(VideoActionDialog.MOVE) }, onAddToPlaylist = { onPickDialog(VideoActionDialog.ADD_TO_PLAYLIST) })
        VideoActionDialog.RENAME -> RenameDialog(video = video, onDismiss = onDismiss, onRename = { newName -> handleResult(renameVideoFile(context, video, newName), onSuccess = { onDismiss(); onVideoChanged() }, retry = { onPickDialog(VideoActionDialog.RENAME) }) })
        VideoActionDialog.DELETE -> {
            val doDelete = { handleResult(deleteVideoFile(context, video), onSuccess = { onDismiss(); onVideoChanged() }, retry = { onPickDialog(VideoActionDialog.DELETE) }) }
            if (confirmDeleteOn) DeleteDialog(video = video, onDismiss = onDismiss, onDelete = { doDelete() })
            else LaunchedEffect(video.id) { doDelete() }
        }
        VideoActionDialog.MOVE -> MoveToFolderDialog(video = video, allFolders = allFolders, onDismiss = onDismiss, onMove = { folderPath -> handleResult(moveVideoFile(context, video, folderPath), onSuccess = { onDismiss(); onVideoChanged() }, retry = { onPickDialog(VideoActionDialog.MOVE) }) })
        VideoActionDialog.ADD_TO_PLAYLIST -> AddToPlaylistDialog(video = video, onDismiss = onDismiss)
        VideoActionDialog.DETAILS -> DetailsDialog(video = video, onDismiss = onDismiss)
        else -> PlaceholderScreen(activeDialog?.name ?: "Action")
    }
    if (errorMessage != null) {
        PremiumAlertDialog(onDismiss = { errorMessage = null }, title = "Error", text = errorMessage!!, confirmButton = { TextButton(onClick = { errorMessage = null; if (!confirmDeleteOn && activeDialog == VideoActionDialog.DELETE) onDismiss() }) { Text("OK") } })
    }
}

@Composable
fun RenameDialog(video: VideoFile, onDismiss: () -> Unit, onRename: (String) -> Unit) {
    var name by remember { mutableStateOf(video.name.substringBeforeLast(".")) }
    PremiumDialog(onDismiss = onDismiss, title = "Rename Media") {
        androidx.compose.material3.OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Filename") },
            shape = RoundedCornerShape(16.dp),
            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BrandAccent,
                unfocusedBorderColor = LocalAppUiSettings.current.palette.outlineMedium,
                focusedContainerColor = glassFieldFill(),
                unfocusedContainerColor = glassFieldFill(),
                cursorColor = BrandAccent
            )
        )
        Spacer(modifier = Modifier.height(32.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = LocalAppUiSettings.current.palette.textTertiary, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(16.dp))
            PremiumButton(text = "Rename", onClick = { onRename(name) })
        }
    }
}

@Composable
fun DeleteDialog(video: VideoFile, onDismiss: () -> Unit, onDelete: () -> Unit) {
    PremiumDialog(onDismiss = onDismiss, title = "Delete Media?") {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(BrandRed.copy(alpha = 0.08f)).padding(16.dp)
        ) {
            Icon(painterResource(R.drawable.ic_warning_custom), null, tint = BrandRed, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text("This will permanently remove the file from your storage.", color = LocalAppUiSettings.current.palette.textPrimary, style = MaterialTheme.typography.bodyMedium)
        }
        Spacer(modifier = Modifier.height(32.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = LocalAppUiSettings.current.palette.textTertiary, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(16.dp))
            val deleteHaptic = LocalHapticFeedback.current
            PremiumScaleButton(
                onClick = { deleteHaptic.performHapticFeedback(HapticFeedbackType.LongPress); onDelete() },
                modifier = Modifier
                    .height(48.dp)
                    .shadow(12.dp, CircleShape, ambientColor = BrandRed.copy(alpha = 0.25f), spotColor = BrandRed.copy(alpha = 0.55f))
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(BrandRed, BrandRed.copy(alpha = 0.82f))))
                    .padding(horizontal = 28.dp)
            ) {
                Text("Delete", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}
@Composable
fun MoveToFolderDialog(video: VideoFile, allFolders: List<VideoFolder>, onDismiss: () -> Unit, onMove: (String) -> Unit) {
    PremiumBottomSheet(onDismiss = onDismiss) {
        Text("Move to Folder", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, color = LocalAppUiSettings.current.palette.textPrimary)
        QuipLine(Quips.dialog("move to folder"), modifier = Modifier.padding(top = 2.dp))
        Spacer(modifier = Modifier.height(22.dp))
        LazyColumn(
            modifier = Modifier.heightIn(max = 400.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(allFolders) { folder ->
                PremiumCardSurface(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 20.dp,
                    onClick = { onMove(folder.path) }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BrandIconContainer(active = true, cornerRadius = 12.dp, size = 40.dp) {
                            Icon(painterResource(R.drawable.ic_folder_custom), null, tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(folder.name, color = LocalAppUiSettings.current.palette.textPrimary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                            Text("${folder.videoCount} Videos", style = MaterialTheme.typography.labelSmall, color = LocalAppUiSettings.current.palette.textSecondary)
                        }
                        Icon(painterResource(R.drawable.ic_chevron_right_custom), null, tint = LocalAppUiSettings.current.palette.textTertiary.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}
@Composable
fun DetailsDialog(video: VideoFile, onDismiss: () -> Unit) {
    PremiumDialog(onDismiss = onDismiss, title = "Media Details") {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            DetailItemExpressive(painterResource(R.drawable.ic_description_custom), "Name", video.name)
            DetailItemExpressive(painterResource(R.drawable.ic_folder_custom), "Path", video.path)
            DetailItemExpressive(painterResource(R.drawable.ic_storage_custom), "Size", formatSize(video.size))
            DetailItemExpressive(painterResource(R.drawable.ic_stopwatch_custom), "Duration", formatDuration(video.duration))
            DetailItemExpressive(painterResource(R.drawable.ic_calendar_custom), "Date Added", formatDate(video.dateAdded))
        }
        Spacer(modifier = Modifier.height(24.dp))
        PremiumButton(text = "Close", onClick = onDismiss, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
fun DetailItemExpressive(icon: Painter, label: String, value: String) {
    Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(LocalAppUiSettings.current.palette.textPrimary.copy(alpha = 0.05f)), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = BrandAccent, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label.uppercase(), style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp, fontWeight = FontWeight.Bold), color = LocalAppUiSettings.current.palette.textTertiary)
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = LocalAppUiSettings.current.palette.textPrimary)
        }
    }
}
@Composable
private fun VideoActionMenuSheet(video: VideoFile, onDismiss: () -> Unit, onRename: () -> Unit, onDelete: () -> Unit, onMove: () -> Unit, onAddToPlaylist: () -> Unit) {
    val context = LocalContext.current
    val settings = LocalAppUiSettings.current
    val accent = settings.accent
    // Same mechanism as the Appearance card: a Popup (no full-screen dialog, no dim) so the
    // video cards stay visible behind a live-blurred glass card.
    val center = remember {
        object : PopupPositionProvider {
            override fun calculatePosition(anchorBounds: IntRect, windowSize: IntSize, layoutDirection: LayoutDirection, popupContentSize: IntSize) =
                IntOffset((windowSize.width - popupContentSize.width) / 2, (windowSize.height - popupContentSize.height) / 2)
        }
    }
    Popup(popupPositionProvider = center, onDismissRequest = onDismiss, properties = PopupProperties(focusable = true)) {
        var shown by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { shown = true }
        androidx.compose.animation.AnimatedVisibility(
            visible = shown,
            enter = fadeIn(tween(160)) + scaleIn(spring(0.75f, 420f), initialScale = 0.88f),
            exit = fadeOut(tween(120))
        ) {
            GlassSurface(
                modifier = Modifier.width(280.dp),
                shape = RoundedCornerShape(26.dp),
                blur = true,
                glow = accent
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                "FILE ACTIONS",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.8.sp),
                                color = accent
                            )
                            Spacer(Modifier.height(3.dp))
                            Text(
                                text = formatFileName(video.name),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
                                color = settings.palette.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        PremiumDialogCloseButton(onClick = onDismiss)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        GlassIconAction(painterResource(R.drawable.ic_share), "Share", onClick = {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "video/*"
                                putExtra(Intent.EXTRA_STREAM, video.uri)
                            }
                            context.startActivity(Intent.createChooser(intent, "Share Video"))
                            onDismiss()
                        })
                        GlassIconAction(painterResource(R.drawable.ic_rename_custom), "Rename", onClick = onRename)
                        GlassIconAction(painterResource(R.drawable.ic_playlist_add_custom), "Add to playlist", onClick = onAddToPlaylist)
                        GlassIconAction(painterResource(R.drawable.ic_move_custom), "Move to folder", onClick = onMove)
                        GlassIconAction(painterResource(R.drawable.ic_trash_custom), "Delete", onClick = onDelete, tint = BrandRed)
                    }
                    QuipLine(Quips.dialog("file actions"), modifier = Modifier.fillMaxWidth(), align = TextAlign.Center)
                }
            }
        }
    }
}

@Composable
private fun LivePreviewTag() {
    val t = rememberInfiniteTransition(label = "liveDot")
    val a = t.animateFloat(0.35f, 1f, infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "liveDotA")
    val accent = LocalAppUiSettings.current.accent
    Row(
        modifier = Modifier
            .glassBadge(Color.Black.copy(alpha = 0.45f), Color.White)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            Modifier.size(7.dp)
                .graphicsLayer { alpha = a.value; val sc = 0.8f + 0.4f * a.value; scaleX = sc; scaleY = sc }
                .background(accent, CircleShape)
        )
        Text(
            "PREVIEW",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp),
            color = Color.White
        )
    }
}

@Composable
private fun IconValueGlassChip(
    icon: Painter,
    value: String,
    color: Color = LocalAppUiSettings.current.palette.textPrimary,
    modifier: Modifier = Modifier
) {
    GlassSurface(
        modifier = modifier,
        shape = CircleShape,
        elevation = 2.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                tint = LocalAppUiSettings.current.accent,
                modifier = Modifier.size(15.dp)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                ),
                color = color,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String, count: Int? = null) {
    SectionHeader(title = text, count = count)
}

private val seriesRegex = Regex("""(?i)\bS\d{1,2}[ ._-]?E\d{1,3}\b|\b\d{1,2}x\d{2,3}\b|\bseason[ ._-]?\d+|\bepisode[ ._-]?\d+""")
private val releaseRegex = Regex("""(?i)\b(19|20)\d{2}\b|\b(2160p|1080p|720p|480p|bluray|brrip|webrip|web-dl|hdtv|dvdrip|x264|x265|hevc)\b""")
fun isMovieOrSeries(video: VideoFile): Boolean {
    val name = video.name
    val path = video.path.lowercase()
    if (seriesRegex.containsMatchIn(name) || releaseRegex.containsMatchIn(name)) return true
    if (path.contains("movie") || path.contains("series") || path.contains("show") || path.contains("cinema") || path.contains("tv")) return true
    if (video.duration > 600_000L) return true
    return false
}
fun cardTitle(video: VideoFile): String =
    if (isMovieOrSeries(video)) formatFileName(video.name) else videoDateTitle(video)

/** Card title: the date only. Falls back to the file's modified time when MediaStore has none. */
fun videoDateTitle(video: VideoFile): String {
    if (video.dateAdded > 0L) return formatDate(video.dateAdded)
    val modified = runCatching { File(video.path).lastModified() }.getOrDefault(0L)
    return if (modified > 0L) formatDate(modified / 1000) else formatDate(0L)
}

private data class MetaStatItem(
    val icon: Painter,
    val label: String,
    val value: String
)

/** Premium metadata strip: one glass container, evenly split, with fading hairline dividers. */
@Composable
private fun MetaStatRow(items: List<MetaStatItem>, modifier: Modifier = Modifier) {
    val settings = LocalAppUiSettings.current
    val tintBase = if (settings.palette.isDark) Color.White else IconIndigo
    val shape = RoundedCornerShape(18.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Brush.verticalGradient(listOf(tintBase.copy(alpha = 0.07f), tintBase.copy(alpha = 0.03f))))
            .border(0.8.dp, Brush.verticalGradient(listOf(tintBase.copy(alpha = 0.18f), tintBase.copy(alpha = 0.05f))), shape)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp, horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { i, stat ->
                if (i > 0) {
                    Box(
                        Modifier
                            .width(1.dp)
                            .height(34.dp)
                            .background(Brush.verticalGradient(listOf(Color.Transparent, tintBase.copy(alpha = 0.16f), Color.Transparent)))
                    )
                }
                Column(
                    modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        painter = stat.icon,
                        contentDescription = null,
                        tint = settings.accent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = stat.value,
                        style = MaterialTheme.typography.titleSmall.copy(fontSize = 13.sp, fontWeight = FontWeight.SemiBold),
                        color = settings.palette.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = stat.label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.2.sp
                        ),
                        color = settings.palette.textTertiary,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
@Composable
fun EmptyState(title: String, subtitle: String, icon: @Composable () -> Unit) { Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { icon(); Spacer(modifier = Modifier.height(16.dp)); Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold); Text(subtitle, color = LocalAppUiSettings.current.palette.textSecondary); Spacer(modifier = Modifier.height(10.dp)); QuipLine(Quips.empty(title), align = TextAlign.Center) } } }
@androidx.media3.common.util.UnstableApi
@Composable
fun RecentsScreen(
    entries: List<RecentEntry>,
    isSearchActive: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onToggleSearch: () -> Unit,
    onVideoClick: (VideoFile) -> Unit,
    onRemoveEntry: (Long) -> Unit,
    onClearDay: (List<Long>) -> Unit
) {
    val filtered by remember(entries, searchQuery) {
        derivedStateOf {
            if (searchQuery.isEmpty()) entries
            else entries.filter { it.video.name.contains(searchQuery, ignoreCase = true) }
        }
    }

    val scrollState = androidx.compose.foundation.lazy.rememberLazyListState()
    val isScrolled by remember {
        derivedStateOf { scrollState.firstVisibleItemIndex > 0 || scrollState.firstVisibleItemScrollOffset > 0 }
    }

    var expandedVideoId by remember { mutableStateOf<Long?>(null) }
    Scaffold(
        topBar = {
            MainTopAppBar(
                title = "History",
                isSearchActive = isSearchActive,
                searchQuery = searchQuery,
                onSearchQueryChange = onSearchQueryChange,
                onToggleSearch = onToggleSearch,
                showBackButton = true,
                onBack = { /* Handled by navigation */ },
                isScrolled = isScrolled
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        if (filtered.isEmpty() && searchQuery.isNotEmpty()) {
            NoResultsScene(query = searchQuery, modifier = Modifier.fillMaxSize().padding(padding))
        } else if (filtered.isEmpty()) {
            EmptyState(title = "No History", subtitle = "Your recently played videos appear here") {
                Icon(painterResource(R.drawable.ic_history_custom), null, modifier = Modifier.size(56.dp), tint = LocalAppUiSettings.current.palette.textTertiary)
            }
        } else {
            val isScrollInProgress = scrollState.isScrollInProgress
            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    state = scrollState,
                    modifier = Modifier.glassSource().fillMaxSize().padding(bottom = padding.calculateBottomPadding()),
                    contentPadding = PaddingValues(top = padding.calculateTopPadding(), bottom = padding.calculateBottomPadding() + 128.dp)
                ) {
                    itemsIndexed(filtered, key = { _, it -> it.video.id }) { index, entry ->
                        val entryAnimation = remember { Animatable(0f) }
                        LaunchedEffect(Unit) {
                            delay(index * 40L)
                            entryAnimation.animateTo(1f, animationSpec = MotionSpec.SoftSpring)
                        }
                        Box(modifier = Modifier.graphicsLayer {
                            translationX = -30f * (1f - entryAnimation.value)
                            alpha = entryAnimation.value
                        }) {
                            VideoItem(
                                video = entry.video,
                                isExpanded = expandedVideoId == entry.video.id,
                                onToggle = {
                                    expandedVideoId = if (expandedVideoId == entry.video.id) null else entry.video.id
                                },
                                onClick = { onVideoClick(entry.video) },
                                isScrollInProgress = isScrollInProgress
                            )
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun PlaceholderScreen(name: String) { Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(name) } }