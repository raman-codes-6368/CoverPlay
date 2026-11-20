package com.raman.CoverPlay.screens

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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toDrawable
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
import com.raman.CoverPlay.CoverPlayTriangleThumbPlaceholder
import com.raman.CoverPlay.FolderSortOrder
import com.raman.CoverPlay.LayoutMode
import com.raman.CoverPlay.R
import com.raman.CoverPlay.RecentEntry
import com.raman.CoverPlay.Screen
import com.raman.CoverPlay.VideoActionDialog
import com.raman.CoverPlay.VideoFile
import com.raman.CoverPlay.VideoFolder
import com.raman.CoverPlay.VideoSortOrder
import com.raman.CoverPlay.components.CoverPlayChip
import com.raman.CoverPlay.components.CoverPlayNoResultsScene
import com.raman.CoverPlay.components.CoverPlayQuipLine
import com.raman.CoverPlay.components.CoverPlaySelectableCard
import com.raman.CoverPlay.components.CoverPlayVideoCardGrid
import com.raman.CoverPlay.components.CoverPlayVideoCardRow
import com.raman.CoverPlay.components.Humor
import com.raman.CoverPlay.components.Quips
import com.raman.CoverPlay.components.easterEggTaps
import com.raman.CoverPlay.components.navContentBottomPadding
import com.raman.CoverPlay.components.showSecret
import com.raman.CoverPlay.data.FileOpResult
import com.raman.CoverPlay.data.deleteVideoFile
import com.raman.CoverPlay.data.moveVideoFile
import com.raman.CoverPlay.data.renameVideoFile
import com.raman.CoverPlay.formatDuration
import com.raman.CoverPlay.formatFileName
import com.raman.CoverPlay.formatSize
import com.raman.CoverPlay.navigation.CoverPlayAppNavBar
import com.raman.CoverPlay.navigation.CoverPlayHomeQuickActions
import com.raman.CoverPlay.navigation.NavBarItem
import com.raman.CoverPlay.network.CoverPlayPlayUrlDialog
import com.raman.CoverPlay.removeFromRecents
import com.raman.CoverPlay.settings.CoverPlayAddToPlaylistDialog
import com.raman.CoverPlay.settings.Playlist
import com.raman.CoverPlay.ui.theme.*
import com.raman.CoverPlay.ui.theme.AccentSwatches
import com.raman.CoverPlay.ui.theme.AppChrome
import com.raman.CoverPlay.ui.theme.BackdropState
import com.raman.CoverPlay.ui.theme.BrandAccent
import com.raman.CoverPlay.ui.theme.BrandRed
import com.raman.CoverPlay.ui.theme.CoverPlayAlertDialog
import com.raman.CoverPlay.ui.theme.CoverPlayBottomSheet
import com.raman.CoverPlay.ui.theme.CoverPlayBrandIconContainer
import com.raman.CoverPlay.ui.theme.CoverPlayCardSurface
import com.raman.CoverPlay.ui.theme.CoverPlayDialog
import com.raman.CoverPlay.ui.theme.CoverPlayDialogActionRow
import com.raman.CoverPlay.ui.theme.CoverPlayDialogButton
import com.raman.CoverPlay.ui.theme.CoverPlayDialogButtonRow
import com.raman.CoverPlay.ui.theme.CoverPlayDialogButtonStyle
import com.raman.CoverPlay.ui.theme.CoverPlayDialogCloseButton
import com.raman.CoverPlay.ui.theme.CoverPlayDialogDivider
import com.raman.CoverPlay.ui.theme.CoverPlayDialogInfoRow
import com.raman.CoverPlay.ui.theme.CoverPlayDialogQuickTile
import com.raman.CoverPlay.ui.theme.CoverPlayDialogTitleBar
import com.raman.CoverPlay.ui.theme.CoverPlayDimens
import com.raman.CoverPlay.ui.theme.CoverPlayGlassSurface
import com.raman.CoverPlay.ui.theme.CoverPlayIconButton
import com.raman.CoverPlay.ui.theme.CoverPlayLoaderBadge
import com.raman.CoverPlay.ui.theme.CoverPlayMaterialShapes
import com.raman.CoverPlay.ui.theme.CoverPlayMediaPill
import com.raman.CoverPlay.ui.theme.CoverPlayNewBadge
import com.raman.CoverPlay.ui.theme.CoverPlayProgressBar
import com.raman.CoverPlay.ui.theme.CoverPlayProvideArtTint
import com.raman.CoverPlay.ui.theme.CoverPlayResolutionBadge
import com.raman.CoverPlay.ui.theme.CoverPlayScrollAwareTopBar
import com.raman.CoverPlay.ui.theme.CoverPlaySearchField
import com.raman.CoverPlay.ui.theme.CoverPlaySectionHeader
import com.raman.CoverPlay.ui.theme.CoverPlaySolidCard
import com.raman.CoverPlay.ui.theme.CoverPlaySpacingTokens
import com.raman.CoverPlay.ui.theme.CoverPlayTokens
import com.raman.CoverPlay.ui.theme.CoverPlayTonalSurface
import com.raman.CoverPlay.ui.theme.IconIndigo
import com.raman.CoverPlay.ui.theme.LocalAppUiSettings
import com.raman.CoverPlay.ui.theme.LocalGlassHaze
import com.raman.CoverPlay.ui.theme.LocalShapeRotation
import com.raman.CoverPlay.ui.theme.MotionSpec
import com.raman.CoverPlay.ui.theme.ThemeMode
import com.raman.CoverPlay.ui.theme.accentGlow
import com.raman.CoverPlay.ui.theme.aliveClickable
import com.raman.CoverPlay.ui.theme.chromeFade
import com.raman.CoverPlay.ui.theme.coverPlayDialogFieldColors
import com.raman.CoverPlay.ui.theme.floating
import com.raman.CoverPlay.ui.theme.glassBadge
import com.raman.CoverPlay.ui.theme.glassSource
import com.raman.CoverPlay.ui.theme.onFill
import com.raman.CoverPlay.ui.theme.sectionOverline
import com.raman.CoverPlay.ui.theme.setAccentColor
import com.raman.CoverPlay.ui.theme.skeleton
import com.raman.CoverPlay.ui.theme.staggerIn
import com.raman.CoverPlay.util.rememberCoverPlayHaptic
import com.raman.CoverPlay.util.rememberCoverPlayHapticFeedback
import dev.chrisbanes.haze.HazeState
import java.io.File
import java.util.Calendar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

fun formatVideoTitleDate(seconds: Long, duration: Long): String {
    if (seconds == 0L) return if (duration > 0) formatDuration(duration) else "Video"
    val sdf = java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault())
    val dateStr = sdf.format(java.util.Date(seconds * 1000))
    return if (duration > 0) "$dateStr · ${formatDuration(duration)}" else dateStr
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

@OptIn(dev.chrisbanes.haze.ExperimentalHazeApi::class)
@Composable
fun CoverPlayBottomNavigationBar(
    currentScreen: Screen,
    onScreenSelected: (Screen) -> Unit,
    collapsed: Boolean = false,
    backdrop: BackdropState? = null,
    hazeState: HazeState? = LocalGlassHaze.current
) {
    val tabs = listOf(
        NavItem(Screen.HOME,      "Home",      R.drawable.ic_nav_home_filled,     R.drawable.ic_nav_home_unfilled),
        NavItem(Screen.STREAM,    "Stream",    R.drawable.ic_stream_filled,   R.drawable.ic_stream_unfilled),
        NavItem(Screen.PLAYLISTS, "Playlists", R.drawable.ic_nav_playlist_filled, R.drawable.ic_nav_playlist_unfilled),
        NavItem(Screen.SETTINGS,  "Settings",  R.drawable.ic_nav_settings_filled, R.drawable.ic_nav_settings_unfilled)
    )
    val settings = LocalAppUiSettings.current
    val haptic = rememberCoverPlayHapticFeedback()
    val selectedIndex = tabs.indexOfFirst { tab ->
        when (tab.screen) {
            Screen.STREAM -> currentScreen == Screen.STREAM || currentScreen == Screen.NETWORK
            Screen.HOME -> currentScreen == Screen.HOME || currentScreen == Screen.FOLDER_VIDEOS
            else -> currentScreen == tab.screen
        }
    }
    val items = tabs.mapIndexed { index, tab ->
        NavBarItem(
            label = tab.label,
            selected = index == selectedIndex,
            onClick = { onScreenSelected(tab.screen) },
            filledIcon = tab.filledIcon,
            outlineIcon = tab.outlineIcon
        )
    }
    CoverPlayAppNavBar(
        items = items,
        accent = settings.accent,
        idleTint = settings.palette.textSecondary,
        labelsVisible = true,
        floating = settings.floatingNav
    )
}

private data class NavItem(val screen: Screen, val label: String, val filledIcon: Int, val outlineIcon: Int)

@Composable
fun CoverPlayAppearanceGlassPanel(
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

    CoverPlayGlassSurface(
        modifier = modifier.width(280.dp),
        shape = RoundedCornerShape(CoverPlayTokens.Radius.sheet),
        blur = true,
        elevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "ACCENT COLOR",
                    style = MaterialTheme.typography.sectionOverline,
                    color = accent
                )
                Spacer(Modifier.weight(1f))
                if (accentName != null) {
                    Text(
                        accentName,
                        style = MaterialTheme.typography.labelSmall,
                        color = settings.palette.textTertiary
                    )
                }
            }

            CoverPlayAccentSwatchGrid(
                options = accentOptions,
                selected = settings.accent,
                swatchSize = 34.dp,
                perRow = 5,
                onPick = { color ->
                    setAccentColor(context, color)
                    onAccentChange(color)
                }
            )
        }
    }
}

@Composable
fun CoverPlayAccentSwatchGrid(
    options: List<Color>,
    selected: Color,
    swatchSize: androidx.compose.ui.unit.Dp,
    perRow: Int,
    onPick: (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    val swatchHaptic = rememberCoverPlayHaptic()
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        options.chunked(perRow).forEach { rowColors ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                rowColors.forEach { color ->
                    val isSelected = color.toArgb() == selected.toArgb()
                    val sc by animateFloatAsState(if (isSelected) 1f else 0.92f, androidx.compose.animation.core.tween(180), label = "swatch")
                    Box(
                        modifier = Modifier
                            .size(swatchSize)
                            .graphicsLayer { scaleX = sc; scaleY = sc }
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                if (isSelected) 2.dp else 1.dp,
                                if (isSelected) LocalAppUiSettings.current.palette.textPrimary else LocalAppUiSettings.current.palette.outlineMedium,
                                CircleShape
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { swatchHaptic(); onPick(color) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) Icon(Icons.Rounded.Check, null, tint = color.onFill(), modifier = Modifier.size(swatchSize / 2))
                    }
                }
                repeat(perRow - rowColors.size) { Spacer(Modifier.size(swatchSize)) }
            }
        }
    }
}

val DefaultAccentOptions: List<Color> = AccentSwatches

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
fun CoverPlayGreetingHeader(
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
    val haptic = rememberCoverPlayHapticFeedback()
    val scope = rememberCoroutineScope()

    var now by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000L)
            now = System.currentTimeMillis()
        }
    }
    val cal = remember(now) { Calendar.getInstance().apply { timeInMillis = now } }
    val period = periodFor(cal.get(Calendar.HOUR_OF_DAY))

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

    var show by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { show = true }
    val emph = remember { CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f) }
    val a1 by animateFloatAsState(if (show) 1f else 0f, tween(650, easing = emph), label = "g1")
    val a2 by animateFloatAsState(if (show) 1f else 0f, tween(650, delayMillis = 110, easing = emph), label = "g2")
    val a3 by animateFloatAsState(if (show) 1f else 0f, tween(650, delayMillis = 220, easing = emph), label = "g3")

    val pulse = remember { Animatable(1f) }
    val shimmer = remember { Animatable(0f) }

    var eggCount by remember { mutableIntStateOf(0) }
    var eggLast by remember { mutableLongStateOf(0L) }
    fun eggHit(n: Int) {
        val t = System.currentTimeMillis()
        eggCount = if (t - eggLast > 1500L) n else eggCount + n
        eggLast = t
        if (eggCount >= 7) { eggCount = 0; showSecret(context) }
    }

    val gradient = Brush.linearGradient(listOf(accent, androidx.compose.ui.graphics.lerp(accent, Color.White, 0.45f)))

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
            Row(
                modifier = Modifier
                    .graphicsLayer {
                        alpha = a1
                        translationY = (1f - a1) * 10.dp.toPx()
                    }
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.14f))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val rotation = LocalShapeRotation.current
                Box(
                    Modifier
                        .size(10.dp)
                        .graphicsLayer { rotationZ = rotation.value }
                        .background(accent, CoverPlayMaterialShapes.cookie6())
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = dateText,
                    style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 0.6.sp),
                    color = accent
                )
            }
            Spacer(Modifier.height(10.dp))
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
                    style = MaterialTheme.typography.headlineLarge,
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
                    style = MaterialTheme.typography.bodyLarge,
                    color = settings.palette.textSecondary,
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

@Composable
fun CoverPlayDiceRollButton(size: androidx.compose.ui.unit.Dp = 38.dp, onRoll: () -> Unit) {
    val settings = LocalAppUiSettings.current
    val accent = settings.accent
    val haptic = rememberCoverPlayHapticFeedback()
    val scope = rememberCoroutineScope()
    val roll = remember { Animatable(0f) }
    var rolling by remember { mutableStateOf(false) }
    var dir by remember { mutableStateOf(1f) }
    val painter = painterResource(R.drawable.ic_random_dice)
    val idleTint = settings.palette.textPrimary

    Box(Modifier.size(size), contentAlignment = Alignment.Center) {
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
        CoverPlayTonalSurface(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer {
                    val p = roll.value
                    val arc = kotlin.math.sin(Math.PI.toFloat() * p)
                    val sc = 1f + 0.30f * arc
                    scaleX = sc; scaleY = sc
                    translationY = -12.dp.toPx() * arc
                },
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
fun CoverPlaySignatureTitle(title: String) {
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

private val TopBarTitleStyle: androidx.compose.ui.text.TextStyle
    @Composable get() = MaterialTheme.typography.displaySmall.copy(
        fontSize = 26.sp,
        lineHeight = 32.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.7).sp
    )

@OptIn(ExperimentalMaterial3Api::class, dev.chrisbanes.haze.ExperimentalHazeApi::class)
@Composable
fun CoverPlayMainTopAppBar(
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
    hazeState: HazeState? = LocalGlassHaze.current,
    bottomContent: (@Composable ColumnScope.() -> Unit)? = null
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    val keyboard = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current
    val settings = LocalAppUiSettings.current

    val searchP by animateFloatAsState(
        targetValue = if (isSearchActive) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.86f, stiffness = 520f),
        label = "searchProgress"
    )

    LaunchedEffect(isSearchActive) {
        if (isSearchActive) {
            withFrameNanos { }
            withFrameNanos { }
            runCatching { focusRequester.requestFocus() }
            keyboard?.show()
        } else {
            focusManager.clearFocus()
            keyboard?.hide()
        }
    }

    CoverPlayScrollAwareTopBar(visible = AppChrome.visible || isSearchActive || !isScrolled) {
        Column(Modifier.fillMaxWidth().chromeFade(scrolled = isScrolled, hazeState = hazeState)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(TopAppBarDefaults.windowInsets)
                    .height(56.dp)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnimatedVisibility(
                    visible = showBackButton && !isSearchActive,
                    enter = fadeIn(tween(160)) + expandHorizontally(tween(200), expandFrom = Alignment.Start),
                    exit = fadeOut(tween(120)) + shrinkHorizontally(tween(160), shrinkTowards = Alignment.Start)
                ) {
                    CoverPlayIconButton(
                        painter = painterResource(R.drawable.ic_back_custom),
                        description = "Back",
                        modifier = Modifier.padding(end = 10.dp),
                        onClick = onBack
                    )
                }

                Box(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (searchP < 0.99f) {
                        val titleCtx = LocalContext.current
                        Box(
                            Modifier
                                .padding(start = 4.dp)
                                .graphicsLayer {
                                    alpha = (1f - searchP * 1.6f).coerceIn(0f, 1f)
                                    translationX = -20.dp.toPx() * searchP
                                }
                                .easterEggTaps(7) { showSecret(titleCtx, Quips.tabSecret(title)) }
                        ) { CoverPlaySignatureTitle(title) }
                    }
                    if (isSearchActive || searchP > 0.01f) {
                        CoverPlaySearchField(
                            query = searchQuery,
                            onQueryChange = onSearchQueryChange,
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .graphicsLayer {
                                    alpha = searchP.coerceIn(0f, 1f)
                                    transformOrigin = TransformOrigin(1f, 0.5f)
                                    val sc = 0.88f + 0.12f * searchP
                                    scaleX = sc
                                },
                            placeholder = "Search videos",
                            focusRequester = focusRequester,
                            onSearch = { keyboard?.hide() }
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CoverPlayIconButton(
                        painter = painterResource(if (isSearchActive) R.drawable.ic_close_custom else R.drawable.ic_search_custom),
                        description = if (isSearchActive) "Close search" else "Search",
                        rotation = 90f * searchP,
                        onClick = onToggleSearch
                    )
                    AnimatedVisibility(
                        visible = !isSearchActive,
                        enter = fadeIn(tween(180, delayMillis = 60)) + expandHorizontally(tween(220), expandFrom = Alignment.End),
                        exit = fadeOut(tween(100)) + shrinkHorizontally(tween(180), shrinkTowards = Alignment.End)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (showDice) {
                                val context = LocalContext.current
                                CoverPlayDiceRollButton(size = 38.dp) {
                                    setAccentColor(context, DefaultAccentOptions.filter { it.toArgb() != settings.accent.toArgb() }.random())
                                }
                            }
                            if (showExtraActions) {
                                CoverPlayIconButton(
                                    painter = painterResource(if (currentLayout == LayoutMode.LIST) R.drawable.ic_grid_custom else R.drawable.ic_list_custom),
                                    description = "Layout",
                                    onClick = onToggleLayout
                                )
                                CoverPlayIconButton(
                                    painter = painterResource(R.drawable.ic_filter_custom),
                                    description = "Filter",
                                    onClick = onShowFilter
                                )
                            }
                        }
                    }
                }
            }
            bottomContent?.invoke(this)
        }
    }
}

@androidx.media3.common.util.UnstableApi
@Composable
fun CoverPlayDashboardScreen(
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
    onOpenNetwork: () -> Unit = {},
    onFolderClick: (VideoFolder) -> Unit = {},
    onOpenVideo: (android.net.Uri) -> Unit = {},
    onRescan: () -> Unit = {},
    rescanning: Boolean = false,
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

    val libraryVideos by remember(allVideos, searchQuery, sortOrder) {
        derivedStateOf {
            val base = if (searchQuery.isEmpty()) allVideos
            else allVideos.filter { it.name.contains(searchQuery, ignoreCase = true) }
            when (sortOrder) {
                VideoSortOrder.NAME_ASC -> base.sortedBy { it.name.lowercase() }
                VideoSortOrder.NAME_DESC -> base.sortedByDescending { it.name.lowercase() }
                VideoSortOrder.DATE_NEWEST -> base.reversed()
                VideoSortOrder.SIZE_DESC -> base.sortedByDescending { it.size }
                VideoSortOrder.DURATION_DESC -> base.sortedByDescending { it.duration }
            }
        }
    }

    val suggestions by remember(allVideos, selectedQuickFilter) {
        derivedStateOf {
            val prefs = context.getSharedPreferences("playback_positions", Context.MODE_PRIVATE)
            val ranked = when (selectedQuickFilter) {
                "New" -> allVideos.filter { prefs.getLong(it.path, 0L) == 0L }.sortedByDescending { it.dateAdded }
                "Quick" -> allVideos.filter { it.duration in 1L..120_000L }.sortedByDescending { it.dateAdded }
                "4K" -> allVideos.filter { it.width >= 3840 || it.height >= 2160 }.sortedByDescending { it.dateAdded }
                "Long" -> allVideos.filter { it.duration >= 600_000L }.sortedByDescending { it.duration }
                else -> {
                    val recentIds = recentEntries.map { it.video.id }.toSet()
                    rankSuggested(allVideos.filter { it.id !in recentIds }.ifEmpty { allVideos }, prefs)
                }
            }
            diversifyPicks(ranked, limit = 10)
        }
    }

    if (showUrlDialog) {
        CoverPlayPlayUrlDialog(
            onDismiss = { showUrlDialog = false },
            onPlay = { url ->
                showUrlDialog = false
                onPlayUrl(url)
            }
        )
    }

    if (showFilterSheet) {
        CoverPlayVideoFilterSortSheet(
            currentSort = sortOrder,
            currentLayout = layoutMode,
            onSortSelected = { sortOrder = it },
            onLayoutSelected = { layoutMode = it },
            onDismiss = { showFilterSheet = false }
        )
    }

    val settings = LocalAppUiSettings.current

    CoverPlayProvideArtTint(if (settings.hideThumbnails) null else recentEntries.firstOrNull()?.video?.uri)

    Scaffold(
        topBar = {
            CoverPlayMainTopAppBar(
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
        floatingActionButton = {
            CoverPlayHomeQuickActions(
                visible = true,
                rescanning = rescanning,
                onOpenVideo = onOpenVideo,
                onRescan = onRescan
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                state = scrollState,
                modifier = Modifier.glassSource().fillMaxSize().padding(bottom = padding.calculateBottomPadding()),
                contentPadding = PaddingValues(top = padding.calculateTopPadding(), bottom = padding.calculateBottomPadding() + navContentBottomPadding())
            ) {
                val browsing = !isSearchActive || searchQuery.isEmpty()
                val heroVideo = if (browsing && selectedQuickFilter == "For you") suggestions.firstOrNull() else null
                val carouselVideos = if (heroVideo != null) suggestions.drop(1) else suggestions
                val isScrollInProgress = scrollState.isScrollInProgress
                val suggestedSubtitle = when (selectedQuickFilter) {
                    "New" -> "Not watched yet"
                    "Quick" -> "Under two minutes"
                    "4K" -> "Highest resolution first"
                    "Long" -> "Settle in for a while"
                    else -> "Fresh, unfinished and worth a watch"
                }

                if (browsing) {
                    item(key = "greeting") {
                        CoverPlayGreetingHeader(
                            modifier = Modifier.padding(start = CoverPlayDimens.screenHorizontal, end = CoverPlayDimens.screenHorizontal, top = 0.dp, bottom = 4.dp),
                            userName = userName,
                            hasRecent = recentEntries.isNotEmpty(),
                            onThemeModeChange = onThemeModeChange,
                            accentOptions = accentOptions,
                            onAccentChange = onAccentChange
                        )
                    }
                    if (allFolders.isNotEmpty()) {
                        item(key = "folders") {
                            CoverPlayHomeFoldersRail(folders = allFolders, onFolderClick = onFolderClick)
                        }
                    }
                    if (recentEntries.isNotEmpty()) {
                        item(key = "continue") {
                            Column {
                                CoverPlaySectionLabel("Continue Watching", recentEntries.size)
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = CoverPlayDimens.screenHorizontal),
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    items(recentEntries, key = { it.video.id }) { entry ->
                                        CoverPlayRecentVideoCard(
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
                    if (allVideos.isNotEmpty()) {
                        item(key = "suggested_header") {
                            Column {
                                CoverPlaySectionHeader(title = "Suggested", subtitle = suggestedSubtitle)
                                LazyRow(
                                    contentPadding = PaddingValues(start = CoverPlayDimens.screenHorizontal, end = CoverPlayDimens.screenHorizontal, bottom = 12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf("For you", "New", "Quick", "4K", "Long").forEach { chip ->
                                        item(key = chip) {
                                            CoverPlayChip(
                                                label = chip,
                                                selected = selectedQuickFilter == chip,
                                                onClick = { selectedQuickFilter = chip }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        if (heroVideo != null) {
                            item(key = "suggested_hero_${heroVideo.id}") {
                                CoverPlaySuggestedHero(video = heroVideo, onClick = { onVideoClick(heroVideo) }, onLongClick = { actionVideo = heroVideo })
                            }
                        }
                        if (carouselVideos.isNotEmpty()) {
                            item(key = "suggested_row") {
                                key(selectedQuickFilter) {
                                    CoverPlaySuggestedCarousel(
                                        videos = carouselVideos,
                                        onClick = { onVideoClick(it) },
                                        onLongClick = { actionVideo = it },
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                }
                            }
                        } else if (heroVideo == null) {
                            item(key = "suggested_empty") {
                                Text(
                                    "Nothing matches this filter yet",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = settings.palette.textTertiary,
                                    modifier = Modifier.padding(horizontal = CoverPlayDimens.screenHorizontal, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }

                if (!browsing) {
                    item(key = "library_header") {
                        CoverPlaySectionHeader(
                            title = "Results",
                            count = libraryVideos.size,
                            subtitle = null
                        )
                    }
                    if (libraryVideos.isEmpty()) {
                        item(key = "library_empty") {
                            CoverPlayNoResultsScene(
                                query = searchQuery,
                                modifier = Modifier.fillMaxWidth().heightIn(min = 420.dp)
                            )
                        }
                    } else if (layoutMode == LayoutMode.LIST) {
                        items(libraryVideos, key = { it.id }) { video ->
                            CoverPlayVideoItem(
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
                        items(libraryVideos.chunked(2), key = { pair -> pair.first().id }) { rowVideos ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = CoverPlayDimens.screenHorizontal, vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                for (video in rowVideos) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        CoverPlayVideoGridItem(video = video, onClick = { onVideoClick(video) }, onLongClick = { actionVideo = video })
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
            CoverPlayVideoActionsHost(actionVideo = actionVideo, activeDialog = activeDialog, allFolders = allFolders, onDismiss = { actionVideo = null; activeDialog = null }, onPickDialog = { activeDialog = it }, onVideoChanged = { expandedVideoId = null; onVideoChanged() }, onPlayVideo = onVideoClick)
        }
    }
}

@Composable
private fun CoverPlayHomeSourcesRow(onStreamUrl: () -> Unit, onNetwork: () -> Unit) {
    val tapBuzz = rememberCoverPlayHaptic()
    Column {
        CoverPlaySectionLabel("Sources")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = CoverPlaySpacingTokens.screenHorizontal)
                .height(androidx.compose.foundation.layout.IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CoverPlaySourceTile(
                icon = painterResource(R.drawable.ic_url_custom),
                title = "Stream URL",
                subtitle = "Play a link",
                badge = CoverPlayMaterialShapes.cookie9(),
                modifier = Modifier.weight(1f).fillMaxHeight()
            ) {
                tapBuzz()
                onStreamUrl()
            }
            CoverPlaySourceTile(
                icon = painterResource(R.drawable.ic_network),
                title = "Network",
                subtitle = "SMB, FTP, WebDAV",
                badge = CoverPlayMaterialShapes.clover4(),
                modifier = Modifier.weight(1f).fillMaxHeight()
            ) {
                tapBuzz()
                onNetwork()
            }
        }
    }
}

@Composable
private fun CoverPlaySourceTile(
    icon: Painter,
    title: String,
    subtitle: String,
    badge: Shape,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val settings = LocalAppUiSettings.current
    CoverPlaySelectableCard(modifier = modifier, onClick = onClick) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            CoverPlayBrandIconContainer(active = true, size = CoverPlayDimens.iconContainer) {
                Icon(icon, null, tint = settings.accent, modifier = Modifier.size(22.dp))
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = settings.palette.textPrimary, maxLines = 1)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = settings.palette.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@androidx.media3.common.util.UnstableApi
@Composable
fun CoverPlayRecentVideoCard(
    video: VideoFile,
    onClick: () -> Unit,
    onRemove: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settings = LocalAppUiSettings.current
    val haptic = rememberCoverPlayHapticFeedback()
    val tapBuzz = rememberCoverPlayHaptic()
    val prefs = context.getSharedPreferences("playback_positions", Context.MODE_PRIVATE)
    val savedPos = remember(video.path) { prefs.getLong(video.path, 0L) }
    val progress = if (video.duration > 0 && savedPos > 0) (savedPos.toFloat() / video.duration.toFloat()).coerceIn(0.03f, 1f) else 0f
    val leftMs = (video.duration - savedPos).coerceAtLeast(0L)
    val leftMin = (leftMs / 60000L).toInt()
    val leftStr = if (leftMin >= 60) "${leftMin / 60}h ${leftMin % 60}m left" else "${leftMin.coerceAtLeast(1)}m left"

    var thumbReady by remember(video.path) { mutableStateOf(false) }
    val shape = RoundedCornerShape(28.dp)
    val tonalFill = settings.palette.containerHigh.copy(alpha = if (settings.palette.isDark) 0.62f else 0.92f)
    var started by remember(video.path) { mutableStateOf(false) }
    LaunchedEffect(video.path) { started = true }
    val shownProgress by animateFloatAsState(
        targetValue = if (started) progress else 0f,
        animationSpec = androidx.compose.animation.core.spring(dampingRatio = 0.7f, stiffness = 120f),
        label = "continueProgress"
    )

    Column(
        modifier = modifier
            .width(288.dp)
            .staggerIn(0)
            .clip(shape)
            .background(tonalFill)
            .combinedClickable(
                onClick = { tapBuzz(); onClick() },
                onLongClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); onRemove() }
            )
            .padding(8.dp)
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(16 / 9f)
                .clip(RoundedCornerShape(22.dp))
                .background(settings.palette.containerLowest)
                .skeleton(active = !thumbReady && !settings.hideThumbnails)
        ) {
            if (settings.hideThumbnails) {
                CoverPlayTriangleThumbPlaceholder(modifier = Modifier.fillMaxSize())
            } else {
                AsyncImage(
                    model = ImageRequest.Builder(context).data(video.uri).videoFrameMillis(1000).crossfade(true).build(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    onSuccess = { thumbReady = true }
                )
            }
            Box(
                Modifier.fillMaxSize().background(
                    Brush.verticalGradient(
                        0f to Color.Black.copy(alpha = 0.22f),
                        0.4f to Color.Transparent,
                        1f to Color.Black.copy(alpha = 0.50f)
                    )
                )
            )
            Row(
                Modifier.fillMaxWidth().align(Alignment.TopStart).padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CoverPlayResolutionBadge(video.width, video.height)
                Spacer(Modifier.weight(1f))
                Box(
                    Modifier.size(30.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.38f)).aliveClickable { onRemove() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(painterResource(R.drawable.ic_close_custom), "Remove from history", tint = Color.White.copy(alpha = 0.92f), modifier = Modifier.size(12.dp))
                }
            }
            CoverPlayMediaPill(
                if (savedPos > 0L) leftStr else formatDuration(video.duration),
                Modifier.align(Alignment.BottomEnd).padding(10.dp)
            )
        }
        Column(Modifier.padding(start = 8.dp, end = 8.dp, top = 12.dp, bottom = 6.dp)) {
            Text(
                text = remember(video.path, video.name) { cardTitle(video) },
                style = MaterialTheme.typography.titleMedium,
                color = settings.palette.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(10.dp))
            if (progress > 0f) {
                Box(Modifier.fillMaxWidth().height(6.dp).clip(CircleShape).background(settings.accent.copy(alpha = 0.18f))) {
                    Box(Modifier.fillMaxWidth(shownProgress.coerceIn(0f, 1f)).fillMaxHeight().clip(CircleShape).background(settings.accent))
                }
                Spacer(Modifier.height(8.dp))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (savedPos > 0L) {
                    Text(
                        "Resume ${formatDuration(savedPos)}",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = settings.accent
                    )
                } else {
                    CoverPlayNewBadge(onMedia = false)
                }
                Spacer(Modifier.weight(1f))
                Text(
                    formatDuration(video.duration),
                    style = MaterialTheme.typography.labelMedium,
                    color = settings.palette.textTertiary
                )
            }
        }
    }
}

@Composable
fun CoverPlayVideoGridItem(video: VideoFile, onClick: () -> Unit, onLongClick: () -> Unit = {}) {
    CoverPlayVideoCardGrid(video = video, onClick = onClick, onLongClick = onLongClick)
}

@Composable
fun CoverPlayFolderGridItem(folder: VideoFolder, thumbVideo: VideoFile? = null, totalSizeStr: String = "", onClick: () -> Unit) {
    val haptic = rememberCoverPlayHapticFeedback()
    val context = LocalContext.current
    CoverPlaySelectableCard(
        modifier = Modifier.fillMaxWidth().aspectRatio(1f),
        onClick = onClick
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
                CoverPlayBrandIconContainer(
                    active = true, size = 56.dp, cornerRadius = 16.dp,
                    modifier = Modifier.accentGlow(LocalAppUiSettings.current.accent, radius = 22.dp, maxAlpha = 0.34f)
                ) {
                    Icon(painterResource(R.drawable.ic_folder_custom), null, tint = Color.White, modifier = Modifier.size(28.dp))
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = folder.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
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
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun CoverPlayFolderQuickChips(
    allVideos: List<VideoFile>,
    downloadsCount: Int,
    cameraCount: Int,
    contentPadding: PaddingValues,
    onFolderClick: (VideoFolder) -> Unit
) {
    LazyRow(contentPadding = contentPadding, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item { CoverPlayChip("All", false, { onFolderClick(VideoFolder("All Media", "/", allVideos.size, "")) }) }
        item { CoverPlayChip("Recent", false, { onFolderClick(VideoFolder("Recent Videos", "/recent", minOf(allVideos.size, 30), "")) }) }
        if (downloadsCount > 0) item { CoverPlayChip("Downloads", false, { onFolderClick(VideoFolder("Downloads", "/download", downloadsCount, "")) }) }
        if (cameraCount > 0) item { CoverPlayChip("Camera", false, { onFolderClick(VideoFolder("Camera", "/camera", cameraCount, "")) }) }
    }
}

@androidx.media3.common.util.UnstableApi
@Composable
fun CoverPlayFolderListScreen(
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
    val gridState = androidx.compose.foundation.lazy.grid.rememberLazyGridState()
    val isScrolled by remember {
        derivedStateOf { scrollState.firstVisibleItemIndex > 0 || scrollState.firstVisibleItemScrollOffset > 0 }
    }

    if (showFilterSheet) {
        CoverPlayFilterSortSheet(
            currentSort = sortOrder,
            currentLayout = layoutMode,
            onSortSelected = { sortOrder = it },
            onLayoutSelected = { layoutMode = it },
            onDismiss = { showFilterSheet = false }
        )
    }

    Scaffold(
        topBar = {
            CoverPlayMainTopAppBar(
                title = "Folders",
                isSearchActive = isSearchActive,
                searchQuery = searchQuery,
                onSearchQueryChange = onSearchQueryChange,
                onToggleSearch = onToggleSearch,
                currentLayout = layoutMode,
                onToggleLayout = { layoutMode = if (layoutMode == LayoutMode.LIST) LayoutMode.GRID else LayoutMode.LIST },
                onShowFilter = { showFilterSheet = true },
                isScrolled = isScrolled,
                bottomContent = if (searchQuery.isEmpty()) {
                    {
                        val downloadsCount = remember(allVideos) { allVideos.count { it.path.contains("download", ignoreCase = true) } }
                        val cameraCount = remember(allVideos) { allVideos.count { it.path.contains("camera", ignoreCase = true) || it.path.contains("dcim", ignoreCase = true) } }
                        CoverPlayFolderQuickChips(allVideos, downloadsCount, cameraCount, PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 12.dp), onFolderClick)
                    }
                } else null
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        if (folders.isEmpty()) {
            CoverPlayEmptyState(title = "No Folders", subtitle = "Scanning for videos...") {
                CoverPlayLoaderBadge()
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
                    CoverPlayNoResultsScene(query = searchQuery, modifier = Modifier.fillMaxSize().padding(padding))
                }
                if (layoutMode == LayoutMode.LIST) {
                    LazyColumn(
                        state = scrollState,
                        modifier = Modifier.glassSource().fillMaxSize().padding(bottom = padding.calculateBottomPadding()),
                        contentPadding = PaddingValues(top = padding.calculateTopPadding(), bottom = padding.calculateBottomPadding() + navContentBottomPadding())
                    ) {
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

                            CoverPlaySelectableCard(
                                modifier = Modifier
                                    .padding(horizontal = 16.dp, vertical = 4.dp)
                                    .fillMaxWidth()
                                    .graphicsLayer {
                                        translationY = 30f * (1f - entryAnimation.value)
                                        alpha = entryAnimation.value
                                    },
                                onClick = { onFolderClick(folder) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (thumbVideo != null && !LocalAppUiSettings.current.hideThumbnails) {
                                        Box(
                                            modifier = Modifier
                                                .size(56.dp)
                                                .clip(RoundedCornerShape(12.dp))
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
                                        CoverPlayBrandIconContainer(active = true, cornerRadius = 12.dp, size = 56.dp) {
                                            Icon(painterResource(R.drawable.ic_folder_custom), null, tint = Color.White, modifier = Modifier.size(24.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = folder.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = LocalAppUiSettings.current.palette.textPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${folder.videoCount} Videos · $fSizeStr",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = LocalAppUiSettings.current.palette.textSecondary,
                                            fontWeight = FontWeight.SemiBold
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
                        state = gridState,
                        modifier = Modifier.glassSource().fillMaxSize().padding(bottom = padding.calculateBottomPadding()),
                        contentPadding = PaddingValues(top = padding.calculateTopPadding(), start = 16.dp, end = 16.dp, bottom = padding.calculateBottomPadding() + navContentBottomPadding()),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (searchQuery.isEmpty()) {
                            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                                val downloadsCount = remember(allVideos) { allVideos.count { it.path.contains("download", ignoreCase = true) } }
                                val cameraCount = remember(allVideos) { allVideos.count { it.path.contains("camera", ignoreCase = true) || it.path.contains("dcim", ignoreCase = true) } }
                                CoverPlayFolderQuickChips(allVideos, downloadsCount, cameraCount, PaddingValues(bottom = 4.dp), onFolderClick)
                            }
                        }
                        items(filteredFolders, key = { it.path }) { folder ->
                            val fVideos = remember(folder.path, allVideos) {
                                allVideos.filter { File(it.path).parent == folder.path || it.path.startsWith(folder.path) }
                            }
                            val thumbVideo = fVideos.firstOrNull()
                            val fSizeStr = remember(fVideos) { formatSize(fVideos.sumOf { it.size }) }
                            CoverPlayFolderGridItem(folder = folder, thumbVideo = thumbVideo, totalSizeStr = fSizeStr, onClick = { onFolderClick(folder) })
                        }
                    }
                }
            }
        }
    }
}

@androidx.media3.common.util.UnstableApi
@Composable
fun CoverPlayVideoItem(
    video: VideoFile,
    isExpanded: Boolean = false,
    onToggle: () -> Unit = {},
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    isScrollInProgress: Boolean = false
) {
    CoverPlayVideoCardRow(video = video, onClick = onClick, onLongClick = onLongClick)
}

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
        if (v.duration in 1L..10_000L) score -= 15f
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
        v.duration >= 3_600_000L -> "Movie night"
        else -> "Not watched yet"
    }
}

private fun diversifyPicks(ranked: List<VideoFile>, limit: Int, perFolder: Int = 2): List<VideoFile> {
    val counts = HashMap<String, Int>()
    val out = ArrayList<VideoFile>(limit)
    for (v in ranked) {
        val folder = v.path.substringBeforeLast('/', "")
        val c = counts[folder] ?: 0
        if (c < perFolder) { out.add(v); counts[folder] = c + 1; if (out.size >= limit) return out }
    }
    for (v in ranked) { if (out.size >= limit) break; if (v !in out) out.add(v) }
    return out
}

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun CoverPlaySuggestedCarousel(
    videos: List<VideoFile>,
    onClick: (VideoFile) -> Unit,
    onLongClick: (VideoFile) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settings = LocalAppUiSettings.current
    val haptic = rememberCoverPlayHapticFeedback()
    val tapBuzz = rememberCoverPlayHaptic()
    val prefs = remember { context.getSharedPreferences("playback_positions", Context.MODE_PRIVATE) }
    val state = rememberCarouselState { videos.size }
    val itemWidth = 248.dp

    HorizontalMultiBrowseCarousel(
        state = state,
        preferredItemWidth = itemWidth,
        modifier = modifier.fillMaxWidth().height(204.dp),
        itemSpacing = 10.dp,
        contentPadding = PaddingValues(horizontal = CoverPlayDimens.screenHorizontal)
    ) { index ->
        val video = videos[index]
        val savedPos = remember(video.path) { prefs.getLong(video.path, 0L) }
        val reason = remember(video.path, savedPos) { suggestionReason(video, savedPos) }
        val progress = if (video.duration > 0L && savedPos > 0L) (savedPos.toFloat() / video.duration.toFloat()).coerceIn(0.03f, 1f) else 0f

        Box(
            Modifier
                .fillMaxSize()
                .maskClip(RoundedCornerShape(CoverPlayTokens.Radius.largeCard))
                .background(settings.palette.containerHigh)
                .combinedClickable(
                    onClick = { tapBuzz(); onClick(video) },
                    onLongClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); onLongClick(video) }
                )
        ) {
            if (settings.hideThumbnails) {
                CoverPlayTriangleThumbPlaceholder(modifier = Modifier.fillMaxSize())
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
                        0.28f to Color.Transparent,
                        0.50f to Color.Transparent,
                        1f to Color.Black.copy(alpha = 0.80f)
                    )
                )
            )
            CoverPlaySuggestedPill(
                text = reason,
                dot = settings.accent,
                modifier = Modifier.align(Alignment.TopStart).padding(start = 14.dp, top = 14.dp)
            )
            Column(
                Modifier
                    .align(Alignment.BottomStart)
                    .requiredWidth(itemWidth)
                    .padding(start = 20.dp, end = 20.dp, bottom = if (progress > 0f) 20.dp else 16.dp)
            ) {
                Text(
                    text = remember(video.path, video.name) { cardTitle(video) },
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = formatDuration(video.duration),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.78f),
                    maxLines = 1
                )
            }
            if (progress > 0f) CoverPlayProgressBar(progress, Modifier.align(Alignment.BottomCenter))
        }
    }
}

@Composable
private fun CoverPlaySuggestedPill(text: String, modifier: Modifier = Modifier, dot: Color? = null) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.52f))
            .padding(horizontal = 11.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (dot != null) {
            Box(Modifier.size(7.dp).clip(CircleShape).background(dot))
            Spacer(Modifier.width(7.dp))
        }
        Text(
            text,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = Color.White,
            maxLines = 1
        )
    }
}

@Composable
private fun CoverPlaySuggestedHero(video: VideoFile, onClick: () -> Unit, onLongClick: () -> Unit) {
    val context = LocalContext.current
    val settings = LocalAppUiSettings.current
    val p = settings.palette
    val haptic = rememberCoverPlayHapticFeedback()
    val tapBuzz = rememberCoverPlayHaptic()
    val prefs = remember { context.getSharedPreferences("playback_positions", Context.MODE_PRIVATE) }
    val savedPos = remember(video.path) { prefs.getLong(video.path, 0L) }
    val reason = remember(video.path, savedPos) { suggestionReason(video, savedPos) }
    val progress = if (video.duration > 0L && savedPos > 0L) (savedPos.toFloat() / video.duration.toFloat()).coerceIn(0.03f, 1f) else 0f

    CoverPlaySolidCard(
        modifier = Modifier.fillMaxWidth().padding(horizontal = CoverPlayDimens.screenHorizontal, vertical = 4.dp).staggerIn(0),
        shape = RoundedCornerShape(CoverPlayTokens.Radius.largeCard),
        onClick = { tapBuzz(); onClick() },
        onLongClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); onLongClick() }
    ) {
        Column(Modifier.padding(8.dp)) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(16 / 9f)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                if (settings.hideThumbnails) {
                    CoverPlayTriangleThumbPlaceholder(modifier = Modifier.fillMaxSize())
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
                            0.30f to Color.Transparent,
                            0.70f to Color.Transparent,
                            1f to Color.Black.copy(alpha = 0.55f)
                        )
                    )
                )
                Row(
                    Modifier.fillMaxWidth().align(Alignment.TopStart).padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CoverPlaySuggestedPill(text = "Top pick", dot = settings.accent)
                    Spacer(Modifier.weight(1f))
                    CoverPlayResolutionBadge(video.width, video.height)
                }
                CoverPlaySuggestedPill(
                    text = formatDuration(video.duration),
                    modifier = Modifier.align(Alignment.BottomStart).padding(start = 12.dp, bottom = if (progress > 0f) 16.dp else 12.dp)
                )
                if (progress > 0f) CoverPlayProgressBar(progress, Modifier.align(Alignment.BottomCenter))
            }
            Row(
                Modifier.fillMaxWidth().padding(start = 12.dp, end = 4.dp, top = 12.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        reason.uppercase(),
                        style = MaterialTheme.typography.sectionOverline,
                        color = settings.accent,
                        maxLines = 1
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = remember(video.path, video.name) { cardTitle(video) },
                        style = MaterialTheme.typography.titleLarge,
                        color = p.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = if (progress > 0f) {
                            val leftMin = ((video.duration - savedPos).coerceAtLeast(0L) / 60_000L).toInt()
                            val left = if (leftMin >= 60) "${leftMin / 60}h ${leftMin % 60}m left" else "${leftMin.coerceAtLeast(1)}m left"
                            left
                        } else formatDuration(video.duration),
                        style = MaterialTheme.typography.labelMedium,
                        color = p.textSecondary,
                        maxLines = 1
                    )
                }
                Spacer(Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CoverPlayMaterialShapes.cookie9())
                        .background(settings.accent),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PlayArrow,
                        contentDescription = "Play ${cardTitle(video)}",
                        tint = settings.accent.onFill(),
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CoverPlayVideoCardIconButton(onClick: () -> Unit, content: @Composable BoxScope.() -> Unit) {
    val st = LocalAppUiSettings.current
    val tintBase = if (st.palette.isDark) Color.White else IconIndigo
    val shape = RoundedCornerShape(CoverPlayTokens.Radius.compactCard)
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(shape)
            .background(LocalAppUiSettings.current.palette.containerHigh)
            .aliveClickable { onClick() },
        contentAlignment = Alignment.Center,
        content = content
    )
}

@Composable
fun CoverPlayVideoDetailCard(icon: Painter, text: String, color: Color = LocalAppUiSettings.current.accent, modifier: Modifier = Modifier) {
    val settings = LocalAppUiSettings.current
    CoverPlayGlassSurface(
        modifier = modifier,
        shape = RoundedCornerShape(CoverPlayTokens.Radius.compactCard)
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
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = settings.palette.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun CoverPlayVideoDetailMinimal(icon: Painter, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = LocalAppUiSettings.current.accent.copy(alpha = 0.7f), modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = LocalAppUiSettings.current.palette.textSecondary
        )
    }
}

@Composable
fun CoverPlayVideoCompactDetail(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = LocalAppUiSettings.current.palette.textTertiary, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.labelSmall, color = LocalAppUiSettings.current.palette.textSecondary, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun CoverPlayVideoDetailItem(label: String, value: String, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(LocalAppUiSettings.current.palette.surfaceRaisedAlt), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = BrandAccent, modifier = Modifier.size(16.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = LocalAppUiSettings.current.palette.textTertiary)
            Text(value, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold), color = LocalAppUiSettings.current.palette.textPrimary)
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
fun CoverPlayVideoListScreen(
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
    val gridState = androidx.compose.foundation.lazy.grid.rememberLazyGridState()
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
        CoverPlayVideoFilterSortSheet(
            currentSort = sortOrder,
            currentLayout = layoutMode,
            onSortSelected = { sortOrder = it },
            onLayoutSelected = { layoutMode = it },
            onDismiss = { showFilterSheet = false }
        )
    }

    Scaffold(
        topBar = {
            CoverPlayMainTopAppBar(
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
            if (filtered.isEmpty() && searchQuery.isNotEmpty()) {
                CoverPlayNoResultsScene(query = searchQuery, modifier = Modifier.fillMaxSize().padding(padding))
            } else if (layoutMode == LayoutMode.LIST) {
                val isScrollInProgress = scrollState.isScrollInProgress
                LazyColumn(
                    state = scrollState,
                    modifier = Modifier.glassSource().fillMaxSize().padding(bottom = padding.calculateBottomPadding()),
                    contentPadding = PaddingValues(top = padding.calculateTopPadding(), bottom = padding.calculateBottomPadding() + navContentBottomPadding())
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
                            CoverPlayVideoItem(
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
                    state = gridState,
                    modifier = Modifier.glassSource().fillMaxSize().padding(bottom = padding.calculateBottomPadding()),
                    contentPadding = PaddingValues(top = padding.calculateTopPadding(), start = 16.dp, end = 16.dp, bottom = padding.calculateBottomPadding() + navContentBottomPadding()),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filtered, key = { it.id }) { video ->
                        CoverPlayVideoGridItem(video = video, onClick = { onVideoClick(video) }, onLongClick = { actionVideo = video })
                    }
                }
            }
        }
    }
    CoverPlayVideoActionsHost(actionVideo = actionVideo, activeDialog = activeDialog, allFolders = allFolders, onDismiss = { actionVideo = null; activeDialog = null }, onPickDialog = { activeDialog = it }, onVideoChanged = { expandedVideoId = null; onVideoChanged() }, onPlayVideo = onVideoClick)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoverPlayFilterSortSheet(currentSort: FolderSortOrder, currentLayout: LayoutMode, onSortSelected: (FolderSortOrder) -> Unit, onLayoutSelected: (LayoutMode) -> Unit, onDismiss: () -> Unit) {
    CoverPlayBottomSheet(onDismiss = onDismiss) {
        Text("Sort Options", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, color = LocalAppUiSettings.current.palette.textPrimary)
        CoverPlayQuipLine(Quips.dialog("sort options"), modifier = Modifier.padding(top = 2.dp))
        Spacer(modifier = Modifier.height(22.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            FolderSortOrder.entries.forEach { order ->
                val isSelected = order == currentSort
                CoverPlayCardSurface(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 20.dp,
                    selected = isSelected,
                    onClick = { onSortSelected(order); onDismiss() }
                ) {
                    Row(modifier = Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(order.label, style = MaterialTheme.typography.titleMedium, color = if (isSelected) BrandAccent else LocalAppUiSettings.current.palette.textPrimary, fontWeight = if(isSelected) FontWeight.SemiBold else FontWeight.Normal)
                        if (isSelected) Icon(painterResource(R.drawable.ic_radio_on_custom), null, tint = BrandAccent, modifier = Modifier.size(24.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoverPlayVideoFilterSortSheet(currentSort: VideoSortOrder, currentLayout: LayoutMode, onSortSelected: (VideoSortOrder) -> Unit, onLayoutSelected: (LayoutMode) -> Unit, onDismiss: () -> Unit) {
    CoverPlayBottomSheet(onDismiss = onDismiss) {
        Text("Order & Layout", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, color = LocalAppUiSettings.current.palette.textPrimary)
        CoverPlayQuipLine(Quips.dialog("order layout sort"), modifier = Modifier.padding(top = 2.dp))
        Spacer(modifier = Modifier.height(22.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            VideoSortOrder.entries.forEach { order ->
                val isSelected = order == currentSort
                CoverPlayCardSurface(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 20.dp,
                    selected = isSelected,
                    onClick = { onSortSelected(order); onDismiss() }
                ) {
                    Row(modifier = Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(order.label, style = MaterialTheme.typography.titleMedium, color = if (isSelected) BrandAccent else LocalAppUiSettings.current.palette.textPrimary, fontWeight = if(isSelected) FontWeight.SemiBold else FontWeight.Normal)
                        if (isSelected) Icon(painterResource(R.drawable.ic_radio_on_custom), null, tint = BrandAccent, modifier = Modifier.size(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun CoverPlayVideoActionsHost(actionVideo: VideoFile?, activeDialog: VideoActionDialog?, allFolders: List<VideoFolder>, onDismiss: () -> Unit, onPickDialog: (VideoActionDialog) -> Unit, onVideoChanged: () -> Unit, onPlayVideo: (VideoFile) -> Unit) {
    val video = actionVideo ?: return; val context = LocalContext.current; var pendingRetry by remember { mutableStateOf<(() -> Unit)?>(null) }
    val confirmDeleteOn = remember { context.getSharedPreferences("coverplay_settings_v3", Context.MODE_PRIVATE).getBoolean("confirm_delete", true) }
    val consentLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            if (activeDialog == VideoActionDialog.DELETE) {
                onDismiss()
                onVideoChanged()
            } else {
                pendingRetry?.invoke()
            }
        }
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
        VideoActionDialog.MENU -> CoverPlayVideoActionMenuSheet(video = video, onDismiss = onDismiss, onRename = { onPickDialog(VideoActionDialog.RENAME) }, onDelete = { onPickDialog(VideoActionDialog.DELETE) }, onMove = { onPickDialog(VideoActionDialog.MOVE) }, onAddToPlaylist = { onPickDialog(VideoActionDialog.ADD_TO_PLAYLIST) }, onDetails = { onPickDialog(VideoActionDialog.DETAILS) })
        VideoActionDialog.RENAME -> CoverPlayRenameDialog(video = video, onDismiss = onDismiss, onRename = { newName -> handleResult(renameVideoFile(context, video, newName), onSuccess = { onDismiss(); onVideoChanged() }, retry = { onPickDialog(VideoActionDialog.RENAME) }) })
        VideoActionDialog.DELETE -> {
            val doDelete = { handleResult(deleteVideoFile(context, video), onSuccess = { onDismiss(); onVideoChanged() }, retry = { onPickDialog(VideoActionDialog.DELETE) }) }
            if (confirmDeleteOn) CoverPlayDeleteDialog(video = video, onDismiss = onDismiss, onDelete = { doDelete() })
            else LaunchedEffect(video.id) { doDelete() }
        }
        VideoActionDialog.MOVE -> CoverPlayMoveToFolderDialog(video = video, allFolders = allFolders, onDismiss = onDismiss, onMove = { folderPath -> handleResult(moveVideoFile(context, video, folderPath), onSuccess = { onDismiss(); onVideoChanged() }, retry = { onPickDialog(VideoActionDialog.MOVE) }) })
        VideoActionDialog.ADD_TO_PLAYLIST -> CoverPlayAddToPlaylistDialog(video = video, onDismiss = onDismiss)
        VideoActionDialog.DETAILS -> CoverPlayDetailsDialog(video = video, onDismiss = onDismiss)
        else -> CoverPlayPlaceholderScreen(activeDialog?.name ?: "Action")
    }
    if (errorMessage != null) {
        CoverPlayAlertDialog(onDismiss = { errorMessage = null }, title = "Error", text = errorMessage!!, confirmButton = { TextButton(onClick = { errorMessage = null; if (!confirmDeleteOn && activeDialog == VideoActionDialog.DELETE) onDismiss() }) { Text("OK") } })
    }
}

@Composable
fun CoverPlayRenameDialog(video: VideoFile, onDismiss: () -> Unit, onRename: (String) -> Unit) {
    val ui = LocalAppUiSettings.current
    val extension = remember(video.name) { video.name.substringAfterLast('.', "").let { if (it.isEmpty()) "" else ".$it" } }
    var name by remember { mutableStateOf(video.name.substringBeforeLast(".")) }
    val canRename = name.isNotBlank()
    CoverPlayDialog(onDismiss = onDismiss, title = "Rename") {
        androidx.compose.material3.OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("File name") },
            suffix = if (extension.isNotEmpty()) {
                { Text(extension, color = ui.palette.textTertiary) }
            } else null,
            shape = RoundedCornerShape(16.dp),
            colors = coverPlayDialogFieldColors()
        )
        Spacer(Modifier.height(24.dp))
        CoverPlayDialogButtonRow(
            confirmText = "Rename",
            onConfirm = { if (canRename) onRename(name.trim()) },
            onCancel = onDismiss
        )
    }
}

@Composable
fun CoverPlayDeleteDialog(video: VideoFile, onDismiss: () -> Unit, onDelete: () -> Unit) {
    val ui = LocalAppUiSettings.current
    val haptic = rememberCoverPlayHapticFeedback()
    CoverPlayDialog(onDismiss = onDismiss, title = "Delete video?") {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                Modifier.size(64.dp).clip(CircleShape).background(BrandRed.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(painterResource(R.drawable.ic_trash_custom), null, tint = BrandRed, modifier = Modifier.size(28.dp))
            }
            Spacer(Modifier.height(16.dp))
            Text(
                formatFileName(video.name),
                color = ui.palette.textPrimary,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "It will be permanently removed from your device. This can't be undone.",
                color = ui.palette.textSecondary,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
        Spacer(Modifier.height(24.dp))
        CoverPlayDialogButtonRow(
            confirmText = "Delete",
            confirmStyle = CoverPlayDialogButtonStyle.Destructive,
            onConfirm = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); onDelete() },
            onCancel = onDismiss
        )
    }
}

@Composable
fun CoverPlayMoveToFolderDialog(video: VideoFile, allFolders: List<VideoFolder>, onDismiss: () -> Unit, onMove: (String) -> Unit) {
    CoverPlayBottomSheet(onDismiss = onDismiss) {
        CoverPlayDialogTitleBar(title = "Move to folder", kicker = "File operations", subtitle = formatFileName(video.name), onClose = onDismiss)
        Spacer(Modifier.height(16.dp))
        LazyColumn(modifier = Modifier.heightIn(max = 420.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            items(allFolders) { folder ->
                CoverPlayDialogActionRow(
                    icon = painterResource(R.drawable.ic_folder_custom),
                    label = folder.name,
                    description = "${folder.videoCount} videos",
                    onClick = { onMove(folder.path) }
                )
            }
        }
    }
}

@Composable
fun CoverPlayDetailsDialog(video: VideoFile, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val ui = LocalAppUiSettings.current
    CoverPlayDialog(onDismiss = onDismiss, title = "Details") {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(88.dp, 56.dp).clip(RoundedCornerShape(10.dp)).background(ui.palette.containerLowest)) {
                if (ui.hideThumbnails) CoverPlayTriangleThumbPlaceholder(modifier = Modifier.fillMaxSize())
                else AsyncImage(
                    model = ImageRequest.Builder(context).data(video.uri).videoFrameMillis(1000).crossfade(true).build(),
                    contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(cardTitle(video), style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = ui.palette.textPrimary, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(4.dp))
                CoverPlayResolutionBadge(video.width, video.height, onMedia = false)
            }
        }
        Spacer(Modifier.height(10.dp))
        CoverPlayDialogDivider()
        Column(Modifier.verticalScroll(rememberScrollState())) {
            CoverPlayDialogInfoRow(painterResource(R.drawable.ic_description_custom), "Name", video.name)
            CoverPlayDialogInfoRow(painterResource(R.drawable.ic_folder_custom), "Location", video.path)
            CoverPlayDialogInfoRow(painterResource(R.drawable.ic_storage_custom), "Size", formatSize(video.size))
            CoverPlayDialogInfoRow(painterResource(R.drawable.ic_stopwatch_custom), "Duration", formatDuration(video.duration))
            if (video.width > 0 && video.height > 0)
                CoverPlayDialogInfoRow(painterResource(R.drawable.ic_aspect_fit), "Resolution", "${video.width} × ${video.height}")
            CoverPlayDialogInfoRow(painterResource(R.drawable.ic_calendar_custom), "Date added", formatDate(video.dateAdded), showDivider = false)
        }
        Spacer(Modifier.height(16.dp))
        CoverPlayDialogButton("Close", onDismiss, Modifier.fillMaxWidth(), CoverPlayDialogButtonStyle.Secondary)
    }
}

@Composable
fun CoverPlayDetailItemRow(icon: Painter, label: String, value: String) = CoverPlayDialogInfoRow(icon, label, value, showDivider = false)

@Composable
private fun CoverPlayVideoActionMenuSheet(
    video: VideoFile,
    onDismiss: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    onMove: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onDetails: () -> Unit
) {
    val context = LocalContext.current
    val ui = LocalAppUiSettings.current
    val summary = remember(video.path, video.duration, video.height) {
        listOfNotNull(
            formatDuration(video.duration),
            video.height.takeIf { it > 0 }?.let { "${it}p" }
        ).joinToString("  ·  ")
    }
    CoverPlayBottomSheet(onDismiss = onDismiss) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(104.dp, 60.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(ui.palette.containerLowest)
            ) {
                if (ui.hideThumbnails) CoverPlayTriangleThumbPlaceholder(modifier = Modifier.fillMaxSize())
                else AsyncImage(
                    model = ImageRequest.Builder(context).data(video.uri).videoFrameMillis(1000).crossfade(true).build(),
                    contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    cardTitle(video),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = ui.palette.textPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (summary.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text(summary, style = MaterialTheme.typography.labelMedium, color = ui.palette.textTertiary, maxLines = 1)
                }
            }
            Spacer(Modifier.width(8.dp))
            CoverPlayDialogCloseButton(onClick = onDismiss)
        }
        Spacer(Modifier.height(20.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            CoverPlayDialogQuickTile(painterResource(R.drawable.ic_share), "Share", modifier = Modifier.weight(1f), onClick = {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "video/*"
                    putExtra(Intent.EXTRA_STREAM, video.uri)
                }
                context.startActivity(Intent.createChooser(intent, "Share video"))
                onDismiss()
            })
            CoverPlayDialogQuickTile(painterResource(R.drawable.ic_rename_custom), "Rename", modifier = Modifier.weight(1f), onClick = onRename)
            CoverPlayDialogQuickTile(painterResource(R.drawable.ic_playlist_add_custom), "Playlist", modifier = Modifier.weight(1f), onClick = onAddToPlaylist)
            CoverPlayDialogQuickTile(painterResource(R.drawable.ic_move_custom), "Move", modifier = Modifier.weight(1f), onClick = onMove)
        }
        Spacer(Modifier.height(8.dp))
        CoverPlayDialogDivider()
        Spacer(Modifier.height(6.dp))
        CoverPlayDialogActionRow(painterResource(R.drawable.ic_info_custom), "Details", description = "Size, location and format", onClick = onDetails)
        CoverPlayDialogActionRow(painterResource(R.drawable.ic_trash_custom), "Delete", description = "Remove permanently", onClick = onDelete, destructive = true)
    }
}

@Composable
private fun CoverPlayLivePreviewTag() {
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
            style = MaterialTheme.typography.sectionOverline,
            color = Color.White
        )
    }
}

@Composable
private fun CoverPlayIconValueGlassChip(
    icon: Painter,
    value: String,
    color: Color = LocalAppUiSettings.current.palette.textPrimary,
    modifier: Modifier = Modifier
) {
    CoverPlayGlassSurface(
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
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = color,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun CoverPlaySectionLabel(text: String, count: Int? = null) {
    CoverPlaySectionHeader(title = text, count = count)
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

@Composable
private fun CoverPlayMetaStatRow(items: List<MetaStatItem>, modifier: Modifier = Modifier) {
    val settings = LocalAppUiSettings.current
    val tintBase = if (settings.palette.isDark) Color.White else IconIndigo
    val shape = RoundedCornerShape(CoverPlayTokens.Radius.card)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(LocalAppUiSettings.current.palette.containerHigh)
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
                            .background(tintBase.copy(alpha = 0.10f))
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
                        style = MaterialTheme.typography.titleSmall,
                        color = settings.palette.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = stat.label,
                        style = MaterialTheme.typography.sectionOverline,
                        color = settings.palette.textTertiary,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
@Composable
fun CoverPlayEmptyState(title: String, subtitle: String, icon: @Composable () -> Unit) { Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { icon(); Spacer(modifier = Modifier.height(16.dp)); Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold); Text(subtitle, color = LocalAppUiSettings.current.palette.textSecondary); Spacer(modifier = Modifier.height(10.dp)); CoverPlayQuipLine(Quips.empty(title), align = TextAlign.Center) } } }
@androidx.media3.common.util.UnstableApi
@Composable
fun CoverPlayRecentsScreen(
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
            CoverPlayMainTopAppBar(
                title = "History",
                isSearchActive = isSearchActive,
                searchQuery = searchQuery,
                onSearchQueryChange = onSearchQueryChange,
                onToggleSearch = onToggleSearch,
                showBackButton = true,
                onBack = {  },
                isScrolled = isScrolled
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        if (filtered.isEmpty() && searchQuery.isNotEmpty()) {
            CoverPlayNoResultsScene(query = searchQuery, modifier = Modifier.fillMaxSize().padding(padding))
        } else if (filtered.isEmpty()) {
            CoverPlayEmptyState(title = "No History", subtitle = "Your recently played videos appear here") {
                Icon(painterResource(R.drawable.ic_history_custom), null, modifier = Modifier.size(56.dp), tint = LocalAppUiSettings.current.palette.textTertiary)
            }
        } else {
            val isScrollInProgress = scrollState.isScrollInProgress
            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    state = scrollState,
                    modifier = Modifier.glassSource().fillMaxSize().padding(bottom = padding.calculateBottomPadding()),
                    contentPadding = PaddingValues(top = padding.calculateTopPadding(), bottom = padding.calculateBottomPadding() + navContentBottomPadding())
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
                            CoverPlayVideoItem(
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
fun CoverPlayPlaceholderScreen(name: String) { Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(name) } }
