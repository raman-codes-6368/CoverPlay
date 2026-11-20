package com.raman.CoverPlay.settings

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.media3.common.util.UnstableApi
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.videoFrameMillis
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.raman.CoverPlay.CoverPlayTriangleThumbPlaceholder
import com.raman.CoverPlay.FolderSortOrder
import com.raman.CoverPlay.LayoutMode
import com.raman.CoverPlay.R
import com.raman.CoverPlay.Screen
import com.raman.CoverPlay.VideoFile
import com.raman.CoverPlay.VideoSortOrder
import com.raman.CoverPlay.components.CoverPlayCorners
import com.raman.CoverPlay.components.CoverPlayPlaylistCardGrid
import com.raman.CoverPlay.components.CoverPlayPlaylistCardRow
import com.raman.CoverPlay.components.CoverPlayPlaylistEmptyCard
import com.raman.CoverPlay.components.CoverPlayQuipLine
import com.raman.CoverPlay.components.CoverPlayRowChevron
import com.raman.CoverPlay.components.CoverPlayRowValue
import com.raman.CoverPlay.components.CoverPlaySelectableCard
import com.raman.CoverPlay.components.CoverPlaySettingsRow
import com.raman.CoverPlay.components.CoverPlaySlider
import com.raman.CoverPlay.components.CoverPlaySwitch
import com.raman.CoverPlay.components.CoverPlayVideoCardAction
import com.raman.CoverPlay.components.CoverPlayVideoCardRow
import com.raman.CoverPlay.components.GlowSpec
import com.raman.CoverPlay.components.Quips
import com.raman.CoverPlay.components.easterEggTaps
import com.raman.CoverPlay.components.navContentBottomPadding
import com.raman.CoverPlay.components.showSecret
import com.raman.CoverPlay.formatDuration
import com.raman.CoverPlay.onboarding.TutorialPrefs
import com.raman.CoverPlay.player.CoverPlaySeekbar
import com.raman.CoverPlay.player.playerCustomizationPage
import com.raman.CoverPlay.screens.CoverPlayAccentSwatchGrid
import com.raman.CoverPlay.screens.CoverPlayFilterSortSheet
import com.raman.CoverPlay.screens.CoverPlayMainTopAppBar
import com.raman.CoverPlay.screens.CoverPlayVideoFilterSortSheet
import com.raman.CoverPlay.screens.CoverPlayVideoGridItem
import com.raman.CoverPlay.screens.DefaultAccentOptions
import com.raman.CoverPlay.ui.theme.*
import com.raman.CoverPlay.ui.theme.AccentFill
import com.raman.CoverPlay.ui.theme.AppFont
import com.raman.CoverPlay.ui.theme.BrandAccent
import com.raman.CoverPlay.ui.theme.BrandGradient
import com.raman.CoverPlay.ui.theme.CoverPlayAction
import com.raman.CoverPlay.ui.theme.CoverPlayAlertDialog
import com.raman.CoverPlay.ui.theme.CoverPlayBottomSheet
import com.raman.CoverPlay.ui.theme.CoverPlayBrandIconContainer
import com.raman.CoverPlay.ui.theme.CoverPlayBrandTitleText
import com.raman.CoverPlay.ui.theme.CoverPlayButton
import com.raman.CoverPlay.ui.theme.CoverPlayButtonGroup
import com.raman.CoverPlay.ui.theme.CoverPlayCardSurface
import com.raman.CoverPlay.ui.theme.CoverPlayDialog
import com.raman.CoverPlay.ui.theme.CoverPlayDimens
import com.raman.CoverPlay.ui.theme.CoverPlayGlassSurface
import com.raman.CoverPlay.ui.theme.CoverPlayLoader
import com.raman.CoverPlay.ui.theme.CoverPlaySectionHeader
import com.raman.CoverPlay.ui.theme.CoverPlaySegmented
import com.raman.CoverPlay.ui.theme.CoverPlayThemeModeSelector
import com.raman.CoverPlay.ui.theme.CoverPlayTokens
import com.raman.CoverPlay.ui.theme.DynamicColorAvailable
import com.raman.CoverPlay.ui.theme.LocalAppUiSettings
import com.raman.CoverPlay.ui.theme.SeekbarStyle
import com.raman.CoverPlay.ui.theme.SeekbarThumb
import com.raman.CoverPlay.ui.theme.ThemeChoice
import com.raman.CoverPlay.ui.theme.aliveClickable
import com.raman.CoverPlay.ui.theme.glassBadge
import com.raman.CoverPlay.ui.theme.glassFieldFill
import com.raman.CoverPlay.ui.theme.glassSource
import com.raman.CoverPlay.ui.theme.onFill
import com.raman.CoverPlay.ui.theme.segmentedShape
import com.raman.CoverPlay.ui.theme.setAccentColor
import com.raman.CoverPlay.ui.theme.setDynamicColor
import com.raman.CoverPlay.ui.theme.setSystemAmoled
import com.raman.CoverPlay.ui.theme.setThemeChoice
import com.raman.CoverPlay.ui.theme.staggerIn
import com.raman.CoverPlay.util.CoverPlayHaptics
import com.raman.CoverPlay.util.rememberCoverPlayHaptic
import com.raman.CoverPlay.util.rememberCoverPlayHapticFeedback
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

private const val SETTINGS_PREFS = "coverplay_settings_v3"

private const val KEY_DEFAULT_SPEED       = "default_speed"
private const val KEY_RESUME_PLAYBACK     = "resume_playback"
private const val KEY_BRIGHTNESS_GESTURE  = "brightness_gesture"
private const val KEY_VOLUME_GESTURE      = "volume_gesture"
private const val KEY_PINCH_TO_ZOOM       = "pinch_to_zoom"
private const val KEY_BACKGROUND_PLAY     = "background_play"
private const val KEY_RECENTS_KEEP_DAYS   = "recents_keep_days"
private const val KEY_DEFAULT_ASPECT      = "default_aspect"
private const val KEY_ACCENT_COLOR        = "accent_color"
private const val KEY_FONT_SCALE          = "font_scale"
private const val KEY_SHOW_DURATION_BADGE = "show_duration_badge"
private const val KEY_COMPACT_LIST        = "compact_list"
private const val KEY_FLOATING_NAV        = "floating_nav"
private const val KEY_HIDE_TOPBAR         = "hide_topbar_on_scroll"
private const val KEY_SECTION_POINTER    = "section_header_pointer"
private const val KEY_USE_SYSTEM_FONT     = "use_system_font"
private const val KEY_HIDE_THUMBNAILS     = "hide_thumbnails"
private const val KEY_CONFIRM_DELETE      = "confirm_delete"
private const val KEY_HARDWARE_DECODE     = "hardware_decode"
private const val KEY_SKIP_SECONDS        = "skip_seconds"
private const val KEY_AUTOPLAY_NEXT       = "autoplay_next"
private const val KEY_LOOP_VIDEO          = "loop_video"
private const val KEY_SUBTLE_LOCK_TIMER   = "subtle_lock_timer"
private const val KEY_SHOW_SPLASH         = "show_splash"
private const val KEY_SEEK_GESTURE        = "seek_gesture"
private const val KEY_DOUBLE_TAP_PAUSE    = "double_tap_pause"

private const val DEVELOPER_NAME      = "Raman Singh"
private const val DEVELOPER_UPI_ID    = "raman6369@fam"
private const val DEVELOPER_IMAGE_URL = "https://github.com/raman-codes-6368.png"

private fun buildUpiPayString(payeeUpiId: String, payeeName: String): String {
    val encodedName = java.net.URLEncoder.encode(payeeName, "UTF-8")
    return "upi://pay?pa=$payeeUpiId&pn=$encodedName&cu=INR"
}

private fun generateQrBitmap(content: String, sizePx: Int = 512): Bitmap {
    val hints = mapOf(EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.H, EncodeHintType.MARGIN to 0)
    val bitMatrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, sizePx, sizePx, hints)
    val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.RGB_565)
    for (x in 0 until sizePx) for (y in 0 until sizePx)
        bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
    return bitmap
}

fun readSettings(context: Context): Map<String, Any> {
    val prefs = context.getSharedPreferences(SETTINGS_PREFS, Context.MODE_PRIVATE)
    return mapOf(
        KEY_DEFAULT_SPEED      to prefs.getFloat(KEY_DEFAULT_SPEED, 1.0f),
        KEY_RESUME_PLAYBACK    to prefs.getBoolean(KEY_RESUME_PLAYBACK, true),
        KEY_BRIGHTNESS_GESTURE to prefs.getBoolean(KEY_BRIGHTNESS_GESTURE, true),
        KEY_VOLUME_GESTURE     to prefs.getBoolean(KEY_VOLUME_GESTURE, true),
        KEY_PINCH_TO_ZOOM      to prefs.getBoolean(KEY_PINCH_TO_ZOOM, true),
        KEY_BACKGROUND_PLAY    to prefs.getBoolean(KEY_BACKGROUND_PLAY, false),
        KEY_RECENTS_KEEP_DAYS  to prefs.getInt(KEY_RECENTS_KEEP_DAYS, 7),
        KEY_DEFAULT_ASPECT     to prefs.getString(KEY_DEFAULT_ASPECT, "Fit")!!,
        KEY_RECENTS_KEEP_DAYS  to prefs.getInt(KEY_RECENTS_KEEP_DAYS, 7),
        KEY_SKIP_SECONDS       to prefs.getInt(KEY_SKIP_SECONDS, 10),
        KEY_AUTOPLAY_NEXT      to prefs.getBoolean(KEY_AUTOPLAY_NEXT, false),
        KEY_LOOP_VIDEO         to prefs.getBoolean(KEY_LOOP_VIDEO, false),
        KEY_HARDWARE_DECODE    to prefs.getBoolean(KEY_HARDWARE_DECODE, true),
        KEY_SUBTLE_LOCK_TIMER  to prefs.getBoolean(KEY_SUBTLE_LOCK_TIMER, true),
        KEY_HIDE_THUMBNAILS    to prefs.getBoolean(KEY_HIDE_THUMBNAILS, false),
        KEY_SHOW_SPLASH        to prefs.getBoolean(KEY_SHOW_SPLASH, true),
        KEY_SEEK_GESTURE       to prefs.getBoolean(KEY_SEEK_GESTURE, true),
        KEY_DOUBLE_TAP_PAUSE   to prefs.getBoolean(KEY_DOUBLE_TAP_PAUSE, true),
        KEY_CONFIRM_DELETE     to prefs.getBoolean(KEY_CONFIRM_DELETE, true),
        KEY_SHOW_DURATION_BADGE to prefs.getBoolean(KEY_SHOW_DURATION_BADGE, true),
        KEY_COMPACT_LIST       to prefs.getBoolean(KEY_COMPACT_LIST, false)
    )
}

enum class SettingsPage {
    MAIN, APPEARANCE, PLAYBACK, GESTURES, PLAYER_CUSTOMIZE, PRIVACY_HISTORY, ABOUT_DEVELOPER, ABOUT_APP
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoverPlaySettingsScreen(onClearHistory: () -> Unit, onReplayAppTour: () -> Unit = {}) {
    val context = LocalContext.current
    val prefs   = remember { context.getSharedPreferences(SETTINGS_PREFS, Context.MODE_PRIVATE) }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val settingsListState = androidx.compose.foundation.lazy.rememberLazyListState()
    val settingsScrolled by remember {
        derivedStateOf { settingsListState.firstVisibleItemIndex > 0 || settingsListState.firstVisibleItemScrollOffset > 0 }
    }
    val scope = rememberCoroutineScope()

    var currentPage by remember { mutableStateOf(SettingsPage.MAIN) }
    LaunchedEffect(currentPage) { settingsListState.scrollToItem(0) }

    BackHandler(enabled = currentPage != SettingsPage.MAIN) {
        currentPage = SettingsPage.MAIN
    }

    var defaultSpeed     by remember { mutableFloatStateOf(prefs.getFloat(KEY_DEFAULT_SPEED, 1.0f)) }
    var resumePlayback   by remember { mutableStateOf(prefs.getBoolean(KEY_RESUME_PLAYBACK, true)) }
    var autoplayNext     by remember { mutableStateOf(prefs.getBoolean(KEY_AUTOPLAY_NEXT, false)) }
    var loopVideo        by remember { mutableStateOf(prefs.getBoolean(KEY_LOOP_VIDEO, false)) }
    var hardwareDecode   by remember { mutableStateOf(prefs.getBoolean(KEY_HARDWARE_DECODE, true)) }
    var subtleLockTimer  by remember { mutableStateOf(prefs.getBoolean(KEY_SUBTLE_LOCK_TIMER, true)) }
    val skipOptions      = listOf(5, 10, 15, 30)
    var skipSeconds      by remember { mutableIntStateOf(prefs.getInt(KEY_SKIP_SECONDS, 10)) }
    var showSkipSheet    by remember { mutableStateOf(false) }

    var brightnessGesture by remember { mutableStateOf(prefs.getBoolean(KEY_BRIGHTNESS_GESTURE, true)) }
    var volumeGesture     by remember { mutableStateOf(prefs.getBoolean(KEY_VOLUME_GESTURE, true)) }
    var seekGesture       by remember { mutableStateOf(prefs.getBoolean(KEY_SEEK_GESTURE, true)) }
    var doubleTapPause    by remember { mutableStateOf(prefs.getBoolean(KEY_DOUBLE_TAP_PAUSE, true)) }
    var pinchToZoom       by remember { mutableStateOf(prefs.getBoolean(KEY_PINCH_TO_ZOOM, true)) }
    var hapticStrength    by remember { mutableIntStateOf(CoverPlayHaptics.strength(context)) }
    var showHapticSheet   by remember { mutableStateOf(false) }
    var backgroundPlay    by remember { mutableStateOf(prefs.getBoolean(KEY_BACKGROUND_PLAY, false)) }

    val aspectOptions   = listOf("Fit", "Fill", "Zoom")
    var defaultAspect   by remember { mutableStateOf(prefs.getString(KEY_DEFAULT_ASPECT, "Fit") ?: "Fit") }
    var showAspectSheet by remember { mutableStateOf(false) }

    var showDurationBadge by remember { mutableStateOf(prefs.getBoolean(KEY_SHOW_DURATION_BADGE, true)) }
    var compactList     by remember { mutableStateOf(prefs.getBoolean(KEY_COMPACT_LIST, false)) }
    var floatingNav     by remember { mutableStateOf(prefs.getBoolean(KEY_FLOATING_NAV, true)) }
    var hideTopBar      by remember { mutableStateOf(prefs.getBoolean(KEY_HIDE_TOPBAR, false)) }
    var sectionPointer  by remember { mutableStateOf(prefs.getBoolean(KEY_SECTION_POINTER, true)) }
    var useSystemFont   by remember { mutableStateOf(prefs.getBoolean(KEY_USE_SYSTEM_FONT, false)) }
    val currentUiSettings = LocalAppUiSettings.current
    val accentColor     = currentUiSettings.accent
    val themeChoice     = currentUiSettings.themeChoice
    var showSplash      by remember { mutableStateOf(prefs.getBoolean(KEY_SHOW_SPLASH, true)) }

    var fontScale by remember { mutableFloatStateOf(prefs.getFloat(KEY_FONT_SCALE, 1.0f)) }

    var hideThumbnails  by remember { mutableStateOf(prefs.getBoolean(KEY_HIDE_THUMBNAILS, false)) }
    var confirmDelete   by remember { mutableStateOf(prefs.getBoolean(KEY_CONFIRM_DELETE, true)) }

    val keepDaysOptions = listOf(3, 7, 14, 30)
    var keepDays        by remember { mutableIntStateOf(prefs.getInt(KEY_RECENTS_KEEP_DAYS, 7)) }
    var showKeepSheet   by remember { mutableStateOf(false) }
    var showSpeedSheet  by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }

    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    fun shouldShow(label: String, sublabel: String = ""): Boolean {
        if (!isSearchActive || searchQuery.isEmpty()) return true
        return label.contains(searchQuery, ignoreCase = true) || sublabel.contains(searchQuery, ignoreCase = true)
    }

    fun save(block: android.content.SharedPreferences.Editor.() -> Unit) {
        prefs.edit().apply(block).apply()
    }

    val pageTitle = when (currentPage) {
        SettingsPage.MAIN            -> "Settings"
        SettingsPage.APPEARANCE      -> "Appearance"
        SettingsPage.PLAYBACK        -> "Playback Engine"
        SettingsPage.GESTURES        -> "Gestures & Controls"
        SettingsPage.PLAYER_CUSTOMIZE -> "Player Customization"
        SettingsPage.PRIVACY_HISTORY -> "Privacy & History"
        SettingsPage.ABOUT_DEVELOPER -> "About Developer"
        SettingsPage.ABOUT_APP       -> "About App"
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
            containerColor = Color.Transparent,
            topBar = {
                CoverPlayMainTopAppBar(
                    title = pageTitle,
                    isSearchActive = isSearchActive,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    onToggleSearch = {
                        isSearchActive = !isSearchActive
                        if (!isSearchActive) searchQuery = ""
                    },
                    onBack = {
                        if (currentPage != SettingsPage.MAIN) currentPage = SettingsPage.MAIN
                    },
                    showBackButton = currentPage != SettingsPage.MAIN,
                    showExtraActions = false,
                    isScrolled = settingsScrolled
                )
            }
        ) { padding ->
            AnimatedContent(
                targetState = currentPage,
                transitionSpec = {
                    val emph = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)
                    val dirv = if (targetState != SettingsPage.MAIN) 1 else -1
                    ((slideInHorizontally(tween(440, easing = emph)) { dirv * it / 3 } +
                            fadeIn(tween(320, 60, easing = emph)) +
                            scaleIn(tween(440, easing = emph), initialScale = 0.96f)) togetherWith
                            (slideOutHorizontally(tween(320, easing = emph)) { -dirv * it / 5 } +
                                    fadeOut(tween(180)) +
                                    scaleOut(tween(320), targetScale = 0.97f)))
                },
                label = "SettingsNav",
                modifier = Modifier.fillMaxSize()
            ) { targetPage ->
                LazyColumn(
                    state = settingsListState,
                    modifier = Modifier.glassSource().fillMaxSize(),
                    contentPadding = PaddingValues(top = padding.calculateTopPadding(), bottom = padding.calculateBottomPadding() + navContentBottomPadding())
                ) {
                    when (targetPage) {
                        SettingsPage.MAIN -> {
                            item {
                                Text(
                                    text = "Customize appearance, playback and privacy",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = LocalAppUiSettings.current.palette.textSecondary,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = CoverPlayDimens.screenHorizontal)
                                        .padding(top = 4.dp)
                                )
                            }

                            item { CoverPlaySettingsSectionHeader("Categories") }

                            val catEntries = listOf(
                                CategoryEntry(R.drawable.ic_palette_custom, "Appearance", "Themes, accents, and layout", "Themes, accent colors, and layout", SettingsPage.APPEARANCE),
                                CategoryEntry(R.drawable.ic_play_outline, "Playback Engine", "Speed, seeking, background", "Speed, seeking, background", SettingsPage.PLAYBACK),
                                CategoryEntry(R.drawable.ic_gestures_and_controls, "Gestures & Controls", "Swipe and pinch controls", "Swipe and pinch controls", SettingsPage.GESTURES),
                                CategoryEntry(R.drawable.ic_player_controls, "Player Customization", "Button layout, seekbar position, controls", "Button layout, seekbar position, control size, hide time, hold speed", SettingsPage.PLAYER_CUSTOMIZE),
                                CategoryEntry(R.drawable.ic_history_custom, "Privacy & History", "Watch history & privacy", "Watch history & privacy", SettingsPage.PRIVACY_HISTORY),
                                CategoryEntry(R.drawable.ic_developer, "About Developer", "Developer card & GitHub link", "Developer card & GitHub link", SettingsPage.ABOUT_DEVELOPER),
                                CategoryEntry(R.drawable.ic_info_custom, "About App", "Version, feedback & updates", "Version, feedback & updates", SettingsPage.ABOUT_APP)
                            ).filter { shouldShow(it.label, it.search) }
                            itemsIndexed(catEntries, key = { _, c -> c.label }) { i, c ->
                                CoverPlaySettingsCategoryCard(painterResource(c.icon), c.label, c.sub, i, catEntries.size) { currentPage = c.page }
                            }
                        }

                        SettingsPage.APPEARANCE -> {
                            item { Spacer(modifier = Modifier.height(16.dp)) }
                            if (shouldShow("Visual Style") || shouldShow("Accent Color")) {
                                settingsCard("Visual Style") {
                                    if (shouldShow("Show Splash Screen")) {
                                        item {
                                            CoverPlaySettingsToggleRow(
                                                icon = painterResource(R.drawable.ic_rocket_outline),
                                                label = "Show Splash Screen",
                                                sublabel = "Brand startup animation",
                                                checked = showSplash,
                                                onCheckedChange = {
                                                    showSplash = it
                                                    save { putBoolean(KEY_SHOW_SPLASH, it) }
                                                }
                                            )
                                        }
                                    }

                                    if (shouldShow("Theme Mode", "System, Light, Dark or AMOLED")) {
                                        item {
                                            Column(modifier = Modifier.fillMaxWidth()) {
                                                CoverPlaySettingsRow(
                                                    icon = painterResource(R.drawable.ic_moon_custom),
                                                    title = "Theme Mode",
                                                    subtitle = when (themeChoice) {
                                                        ThemeChoice.SYSTEM -> "Follows your device's setting"
                                                        ThemeChoice.LIGHT -> "Always light"
                                                        ThemeChoice.DARK -> "Always dark"
                                                        ThemeChoice.AMOLED -> "Always dark, on pure black"
                                                    }
                                                )
                                                Box(Modifier.padding(start = CoverPlayDimens.cardPadding, end = CoverPlayDimens.cardPadding, bottom = CoverPlayDimens.cardPadding)) {
                                                    CoverPlayThemeModeSelector(
                                                        selected = themeChoice,
                                                        onSelect = { choice -> setThemeChoice(context, choice) }
                                                    )
                                                }
                                            }
                                        }
                                        if (themeChoice == ThemeChoice.SYSTEM) {
                                            item {
                                                CoverPlaySettingsToggleRow(
                                                    icon = painterResource(R.drawable.ic_moon_custom),
                                                    label = "Pure black when dark",
                                                    sublabel = if (currentUiSettings.systemAmoled) "AMOLED black when your device is dark" else "Regular dark when your device is dark",
                                                    checked = currentUiSettings.systemAmoled,
                                                    onCheckedChange = { setSystemAmoled(context, it) }
                                                )
                                            }
                                        }
                                    }

                                    if (DynamicColorAvailable && shouldShow("Material You", "Apply system wallpaper colors dynamic theme")) {
                                        item {
                                            CoverPlaySettingsToggleRow(
                                                icon = painterResource(R.drawable.ic_palette_custom),
                                                label = "Material You",
                                                sublabel = if (currentUiSettings.dynamicColor) "Using your system wallpaper colors" else "Apply your system's Material You colors",
                                                checked = currentUiSettings.dynamicColor,
                                                onCheckedChange = { setDynamicColor(context, it) }
                                            )
                                        }
                                    }

                                    if (shouldShow("Color Theme", "Material You wallpaper colors and ready-made palettes")) {
                                        item {
                                            Column(modifier = Modifier.fillMaxWidth().padding(bottom = CoverPlayDimens.cardPadding)) {
                                                CoverPlaySettingsRow(
                                                    icon = painterResource(R.drawable.ic_palette_custom),
                                                    title = "Color Theme",
                                                    subtitle = if (currentUiSettings.dynamicColor) "Colors follow your wallpaper" else "A palette built from one color"
                                                )
                                                CoverPlayThemeGallery(
                                                    previewMode = currentUiSettings.themeMode,
                                                    dynamicOn = currentUiSettings.dynamicColor,
                                                    seed = currentUiSettings.seedColor,
                                                    onPickDynamic = { setDynamicColor(context, true) },
                                                    onPickPreset = { preset -> setAccentColor(context, preset.seed) }
                                                )
                                                Spacer(modifier = Modifier.height(20.dp))
                                                Text(
                                                    "Accent color",
                                                    modifier = Modifier.padding(horizontal = CoverPlayDimens.cardPadding),
                                                    style = MaterialTheme.typography.labelLarge,
                                                    color = LocalAppUiSettings.current.palette.textSecondary
                                                )
                                                Spacer(modifier = Modifier.height(12.dp))
                                                CoverPlayAccentSwatchGrid(
                                                    modifier = Modifier.padding(horizontal = CoverPlayDimens.cardPadding),
                                                    options = DefaultAccentOptions,
                                                    selected = if (currentUiSettings.dynamicColor) Color.Transparent else currentUiSettings.seedColor,
                                                    swatchSize = 40.dp,
                                                    perRow = 6,
                                                    onPick = { setAccentColor(context, it) }
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            if (shouldShow("UI Elements") || shouldShow("Duration Badges") || shouldShow("Compact Mode")) {
                                settingsCard("UI Elements") {
                                    if (shouldShow("Duration Badges", "Show runtime on thumbnails")) {
                                        item {
                                            CoverPlaySettingsToggleRow(
                                                icon = painterResource(R.drawable.ic_stopwatch_custom),
                                                label = "Duration Badges",
                                                sublabel = "Show runtime on thumbnails",
                                                checked = showDurationBadge,
                                                onCheckedChange = {
                                                    showDurationBadge = it
                                                    save { putBoolean(KEY_SHOW_DURATION_BADGE, it) }
                                                }
                                            )
                                        }
                                    }
                                    if (shouldShow("Compact Mode", "See more content at once")) {
                                        item {
                                            CoverPlaySettingsToggleRow(
                                                icon = painterResource(R.drawable.ic_list_custom),
                                                label = "Compact Mode",
                                                sublabel = "See more content at once",
                                                checked = compactList,
                                                onCheckedChange = {
                                                    compactList = it
                                                    save { putBoolean(KEY_COMPACT_LIST, it) }
                                                }
                                            )
                                        }
                                    }
                                    if (shouldShow("Floating Navigation Bar", "Floating or resting bottom bar")) {
                                        item {
                                            CoverPlaySettingsToggleRow(
                                                icon = painterResource(R.drawable.ic_grid_custom),
                                                label = "Floating Navigation Bar",
                                                sublabel = if (floatingNav) "Floating pill" else "Resting on the bottom edge",
                                                checked = floatingNav,
                                                onCheckedChange = {
                                                    floatingNav = it
                                                    save { putBoolean(KEY_FLOATING_NAV, it) }
                                                }
                                            )
                                        }
                                    }
                                    if (shouldShow("Section header pointer", "Rotating shape beside section titles")) {
                                        item {
                                            CoverPlaySettingsToggleRow(
                                                icon = painterResource(R.drawable.ic_grid_custom),
                                                label = "Section Header Pointer",
                                                sublabel = if (sectionPointer) "Rotating shape beside titles" else "Off",
                                                checked = sectionPointer,
                                                onCheckedChange = {
                                                    sectionPointer = it
                                                    save { putBoolean(KEY_SECTION_POINTER, it) }
                                                }
                                            )
                                        }
                                    }
                                    if (shouldShow("Top Bar Behavior", "Stay on screen or hide while scrolling")) {
                                        item {
                                            CoverPlaySettingsToggleRow(
                                                icon = painterResource(R.drawable.ic_chevron_down_custom),
                                                label = "Hide Top Bar on Scroll",
                                                sublabel = if (hideTopBar) "Hides while scrolling down" else "Stays on screen",
                                                checked = hideTopBar,
                                                onCheckedChange = {
                                                    hideTopBar = it
                                                    save { putBoolean(KEY_HIDE_TOPBAR, it) }
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            if (shouldShow("Typography") || shouldShow("Text Scaling") || shouldShow("System Font")) {
                                settingsCard("Typography") {
                                    if (shouldShow("Font", "Typeface: Google Sans Flex, Inter, Plus Jakarta Sans, System") || shouldShow("System Font")) {
                                        item {
                                            Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp)) {
                                                Text("Font", style = MaterialTheme.typography.titleMedium, color = LocalAppUiSettings.current.palette.textPrimary)
                                                Text(
                                                    "Applies to every screen in the app",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = LocalAppUiSettings.current.palette.textTertiary
                                                )
                                                Spacer(Modifier.height(12.dp))
                                                CoverPlayFontPicker(
                                                    selected = currentUiSettings.appFont,
                                                    onSelect = { f -> save { putString("app_font", f.name) } }
                                                )
                                            }
                                        }
                                    }
                                    if (shouldShow("Text Scaling")) {
                                        item {
                                            val rowHaptic = rememberCoverPlayHaptic(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                            val isDefaultScale = kotlin.math.abs(fontScale - 1f) < 0.004f
                                            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    CoverPlayBrandIconContainer(size = 40.dp, cornerRadius = CoverPlayCorners.tile) {
                                                        Icon(painterResource(R.drawable.ic_text_scaling), null, tint = accentColor, modifier = Modifier.size(22.dp))
                                                    }
                                                    Spacer(modifier = Modifier.width(18.dp))
                                                    Text("Text Scaling", style = MaterialTheme.typography.titleMedium, color = LocalAppUiSettings.current.palette.textPrimary, modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                                                    AnimatedVisibility(
                                                        visible = !isDefaultScale,
                                                        enter = fadeIn() + scaleIn(),
                                                        exit = fadeOut() + scaleOut()
                                                    ) {
                                                        Row(
                                                            modifier = Modifier
                                                                .padding(end = 8.dp)
                                                                .clip(CircleShape)
                                                                .background(accentColor.copy(alpha = 0.14f))
                                                                .clickable {
                                                                    rowHaptic()
                                                                    fontScale = 1f
                                                                    save { putFloat(KEY_FONT_SCALE, 1f) }
                                                                }
                                                                .padding(horizontal = 10.dp, vertical = 5.dp),
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Icon(Icons.Rounded.Refresh, null, tint = accentColor, modifier = Modifier.size(14.dp))
                                                            Spacer(Modifier.width(4.dp))
                                                            Text("Reset", style = MaterialTheme.typography.labelMedium, color = accentColor, fontWeight = FontWeight.SemiBold)
                                                        }
                                                    }
                                                    Box(modifier = Modifier.glassBadge(accentColor.copy(alpha = 0.16f), accentColor, RoundedCornerShape(CoverPlayCorners.inner))) {
                                                        Text(
                                                            "${(fontScale * 100).roundToInt()}%",
                                                            style = MaterialTheme.typography.labelMedium,
                                                            color = accentColor,
                                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                                            fontWeight = FontWeight.SemiBold
                                                        )
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(8.dp))
                                                CoverPlaySlider(
                                                    value = fontScale,
                                                    onValueChange = { fontScale = it },
                                                    onValueChangeFinished = { save { putFloat(KEY_FONT_SCALE, fontScale) } },
                                                    valueRange = 0.8f..1.3f,
                                                    detents = listOf(1f)
                                                )
                                                Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                                    Text("Smaller", style = MaterialTheme.typography.labelSmall, color = LocalAppUiSettings.current.palette.textTertiary)
                                                    Text("Default", style = MaterialTheme.typography.labelSmall, color = LocalAppUiSettings.current.palette.textTertiary)
                                                    Text("Larger", style = MaterialTheme.typography.labelSmall, color = LocalAppUiSettings.current.palette.textTertiary)
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            if (shouldShow("Seekbar") || shouldShow("Seekbar Style") || shouldShow("Seekbar Handle")) {
                                settingsCard("Seekbar") {
                                    item {
                                        var demoPos by remember { mutableStateOf(38_000L) }
                                        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp)) {
                                            Text("Preview — drag to try it", style = MaterialTheme.typography.labelMedium, color = LocalAppUiSettings.current.palette.textTertiary)
                                            Spacer(Modifier.height(6.dp))
                                            CoverPlaySeekbar(
                                                position = demoPos,
                                                duration = 100_000L,
                                                onValueChange = { demoPos = it.toLong() },
                                                onValueChangeFinished = {},
                                                isPlaying = true,
                                                onSurface = true
                                            )
                                        }
                                    }
                                    item {
                                        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp)) {
                                            Text("Track shape", style = MaterialTheme.typography.titleMedium, color = LocalAppUiSettings.current.palette.textPrimary)
                                            Spacer(Modifier.height(10.dp))
                                            CoverPlaySegmented(
                                                options = SeekbarStyle.entries.toList(),
                                                selected = currentUiSettings.seekbarStyle,
                                                label = { it.label },
                                                onSelect = { save { putString("seekbar_style", it.name) } },
                                                height = 44.dp
                                            )
                                        }
                                    }
                                    item {
                                        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp)) {
                                            Text("Handle", style = MaterialTheme.typography.titleMedium, color = LocalAppUiSettings.current.palette.textPrimary)
                                            Spacer(Modifier.height(10.dp))
                                            CoverPlaySegmented(
                                                options = SeekbarThumb.entries.toList(),
                                                selected = currentUiSettings.seekbarThumb,
                                                label = { it.label },
                                                onSelect = { save { putString("seekbar_thumb", it.name) } },
                                                height = 44.dp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        SettingsPage.PLAYBACK -> {
                            if (shouldShow("Core Playback") || shouldShow("Default Speed") || shouldShow("Seek Interval") || shouldShow("Resume Progress") || shouldShow("Autoplay Next") || shouldShow("Loop Video")) {
                                settingsCard("Core Playback") {
                                    if (shouldShow("Default Speed")) {
                                        item {
                                            CoverPlaySettingsClickRow(
                                                icon = painterResource(R.drawable.ic_speed_outline),
                                                label = "Default Speed",
                                                value = formatSpeed(defaultSpeed),
                                                onClick = { showSpeedSheet = true }
                                            )
                                        }
                                    }
                                    if (shouldShow("Seek Interval", "${skipSeconds}s")) {
                                        item {
                                            CoverPlaySettingsClickRow(
                                                icon = painterResource(R.drawable.ic_forward_outline),
                                                label = "Seek Interval",
                                                value = "${skipSeconds}s",
                                                onClick = { showSkipSheet = true }
                                            )
                                        }
                                    }
                                    if (shouldShow("Resume Progress")) {
                                        item {
                                            CoverPlaySettingsToggleRow(
                                                icon = painterResource(R.drawable.ic_play_outline),
                                                label = "Resume Progress",
                                                checked = resumePlayback,
                                                onCheckedChange = {
                                                    resumePlayback = it
                                                    save { putBoolean(KEY_RESUME_PLAYBACK, it) }
                                                }
                                            )
                                        }
                                    }
                                    if (shouldShow("Autoplay Next")) {
                                        item {
                                            CoverPlaySettingsToggleRow(
                                                icon = painterResource(R.drawable.ic_next_outline),
                                                label = "Autoplay Next",
                                                checked = autoplayNext,
                                                onCheckedChange = {
                                                    autoplayNext = it
                                                    save { putBoolean(KEY_AUTOPLAY_NEXT, it) }
                                                }
                                            )
                                        }
                                    }
                                    if (shouldShow("Loop Video")) {
                                        item {
                                            CoverPlaySettingsToggleRow(
                                                icon = painterResource(R.drawable.ic_repeat_custom),
                                                label = "Loop Video",
                                                checked = loopVideo,
                                                onCheckedChange = {
                                                    loopVideo = it
                                                    save { putBoolean(KEY_LOOP_VIDEO, it) }
                                                }
                                            )
                                        }
                                    }
                                    if (shouldShow("Lock Screen Timer", "Show subtle timer when controls locked")) {
                                        item {
                                            CoverPlaySettingsToggleRow(
                                                icon = painterResource(R.drawable.ic_stopwatch_custom),
                                                label = "Lock Screen Timer",
                                                sublabel = "Show subtle timer when controls locked",
                                                checked = subtleLockTimer,
                                                onCheckedChange = {
                                                    subtleLockTimer = it
                                                    save { putBoolean(KEY_SUBTLE_LOCK_TIMER, it) }
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            if (shouldShow("Engine Settings") || shouldShow("Hardware Decoding") || shouldShow("Background Play")) {
                                settingsCard("Engine Settings") {
                                    if (shouldShow("Hardware Decoding", "Smoother playback using GPU")) {
                                        item {
                                            CoverPlaySettingsToggleRow(
                                                icon = painterResource(R.drawable.ic_code_circle_custom),
                                                label = "Hardware Decoding",
                                                sublabel = "Smoother playback using GPU",
                                                checked = hardwareDecode,
                                                onCheckedChange = {
                                                    hardwareDecode = it
                                                    save { putBoolean(KEY_HARDWARE_DECODE, it) }
                                                }
                                            )
                                        }
                                    }
                                    if (shouldShow("Background Play", "Continue audio when screen off")) {
                                        item {
                                            CoverPlaySettingsToggleRow(
                                                icon = painterResource(R.drawable.ic_pip_custom),
                                                label = "Background Play",
                                                sublabel = "Continue audio when screen off",
                                                checked = backgroundPlay,
                                                onCheckedChange = {
                                                    backgroundPlay = it
                                                    save { putBoolean(KEY_BACKGROUND_PLAY, it) }
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            if (shouldShow("Display") || shouldShow("Default Aspect Ratio")) {
                                settingsCard("Display") {
                                    if (shouldShow("Default Aspect Ratio", defaultAspect)) {
                                        item {
                                            CoverPlaySettingsClickRow(
                                                icon = painterResource(R.drawable.ic_orientation_custom),
                                                label = "Default Aspect Ratio",
                                                value = defaultAspect,
                                                onClick = { showAspectSheet = true }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        SettingsPage.GESTURES -> {
                            if (shouldShow("Swipe Gestures") || shouldShow("Brightness Swipe") || shouldShow("Volume Swipe")) {
                                settingsCard("Swipe Gestures") {
                                    if (shouldShow("Brightness Swipe")) {
                                        item {
                                            CoverPlaySettingsToggleRow(
                                                icon = painterResource(R.drawable.ic_brightness_outline),
                                                label = "Brightness Swipe",
                                                sublabel = "Swipe on left side of screen",
                                                checked = brightnessGesture,
                                                onCheckedChange = {
                                                    brightnessGesture = it
                                                    save { putBoolean(KEY_BRIGHTNESS_GESTURE, it) }
                                                }
                                            )
                                        }
                                    }
                                    if (shouldShow("Volume Swipe")) {
                                        item {
                                            CoverPlaySettingsToggleRow(
                                                icon = painterResource(R.drawable.ic_volume_custom),
                                                label = "Volume Swipe",
                                                sublabel = "Swipe on right side of screen",
                                                checked = volumeGesture,
                                                onCheckedChange = {
                                                    volumeGesture = it
                                                    save { putBoolean(KEY_VOLUME_GESTURE, it) }
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            if (shouldShow("Seek & Tap") || shouldShow("Swipe to Seek") || shouldShow("Double-tap to Pause")) {
                                settingsCard("Seek & Tap") {
                                    if (shouldShow("Swipe to Seek")) {
                                        item {
                                            CoverPlaySettingsToggleRow(
                                                icon = painterResource(R.drawable.ic_forward_outline),
                                                label = "Swipe to Seek",
                                                sublabel = "Drag left or right to scrub",
                                                checked = seekGesture,
                                                onCheckedChange = {
                                                    seekGesture = it
                                                    save { putBoolean(KEY_SEEK_GESTURE, it) }
                                                }
                                            )
                                        }
                                    }
                                    if (shouldShow("Double-tap to Pause")) {
                                        item {
                                            CoverPlaySettingsToggleRow(
                                                icon = painterResource(R.drawable.ic_play_outline),
                                                label = "Double-tap to Pause",
                                                sublabel = "Double-tap the middle of the screen",
                                                checked = doubleTapPause,
                                                onCheckedChange = {
                                                    doubleTapPause = it
                                                    save { putBoolean(KEY_DOUBLE_TAP_PAUSE, it) }
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            if (shouldShow("Touch") || shouldShow("Pinch to Zoom") || shouldShow("Haptic Strength")) {
                                settingsCard("Touch") {
                                    if (shouldShow("Pinch to Zoom", "Freely zoom video with two fingers")) {
                                        item {
                                            CoverPlaySettingsToggleRow(
                                                icon = painterResource(R.drawable.ic_maximize),
                                                label = "Pinch to Zoom",
                                                sublabel = "Freely zoom video with two fingers",
                                                checked = pinchToZoom,
                                                onCheckedChange = {
                                                    pinchToZoom = it
                                                    save { putBoolean(KEY_PINCH_TO_ZOOM, it) }
                                                }
                                            )
                                        }
                                    }
                                    if (shouldShow("Haptic Strength", "Vibration feedback intensity")) {
                                        item {
                                            CoverPlaySettingsClickRow(
                                                icon = painterResource(R.drawable.ic_mobile),
                                                label = "Haptic Strength",
                                                value = CoverPlayHaptics.label(hapticStrength),
                                                onClick = { showHapticSheet = true }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        SettingsPage.PLAYER_CUSTOMIZE -> {
                            playerCustomizationPage()
                        }

                        SettingsPage.PRIVACY_HISTORY -> {
                            if (shouldShow("Library Privacy") || shouldShow("Hide Thumbnails")) {
                                settingsCard("Library Privacy") {
                                    if (shouldShow("Hide Thumbnails", "Privacy-focused browsing")) {
                                        item {
                                            CoverPlaySettingsToggleRow(
                                                icon = painterResource(R.drawable.ic_thumbnail),
                                                label = "Hide Thumbnails",
                                                sublabel = "Privacy-focused browsing",
                                                checked = hideThumbnails,
                                                onCheckedChange = {
                                                    hideThumbnails = it
                                                    save { putBoolean(KEY_HIDE_THUMBNAILS, it) }
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            if (shouldShow("History & Data") || shouldShow("Retention Period") || shouldShow("Confirm Deletion") || shouldShow("Clear All History")) {
                                settingsCard("History & Data") {
                                    if (shouldShow("Retention Period", "$keepDays Days")) {
                                        item {
                                            CoverPlaySettingsClickRow(
                                                icon = painterResource(R.drawable.ic_history_custom),
                                                label = "Retention Period",
                                                value = "$keepDays Days",
                                                onClick = { showKeepSheet = true }
                                            )
                                        }
                                    }
                                    if (shouldShow("Confirm Deletion")) {
                                        item {
                                            CoverPlaySettingsToggleRow(
                                                icon = painterResource(R.drawable.ic_confirm),
                                                label = "Confirm Deletion",
                                                sublabel = "Show confirmation before deleting files",
                                                checked = confirmDelete,
                                                onCheckedChange = {
                                                    confirmDelete = it
                                                    save { putBoolean(KEY_CONFIRM_DELETE, it) }
                                                }
                                            )
                                        }
                                    }
                                    if (shouldShow("Clear All History")) {
                                        item {
                                            CoverPlaySettingsClickRow(
                                                icon = painterResource(R.drawable.ic_trash_custom),
                                                label = "Clear All History",
                                                value = "",
                                                tint = MaterialTheme.colorScheme.error,
                                                onClick = { showClearDialog = true }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        SettingsPage.ABOUT_DEVELOPER -> {
                            if (shouldShow("Developer Profile")) {
                                item { CoverPlaySettingsSectionHeader("Developer Profile") }
                                item {
                                    CoverPlayDeveloperCard(context = context, onViewGithub = {
                                        val intent = Intent(Intent.ACTION_VIEW, "https://github.com/raman-codes-6368".toUri())
                                        context.startActivity(intent)
                                    })
                                }
                            }
                        }

                        SettingsPage.ABOUT_APP -> {
                            if (shouldShow("App Information") || shouldShow("Version") || shouldShow("For Feedback") || shouldShow("Check for Updates")) {
                                settingsCard("App Information") {
                                    if (shouldShow("Version", "3.0.5")) {
                                        item {
                                            Box(
                                                modifier = Modifier.easterEggTaps(7) {
                                                    showSecret(context, "🛠️ Developer mode unlocked… CoverPlay v3.0.5")
                                                }
                                            ) {
                                                CoverPlaySettingsInfoRow(
                                                    icon = painterResource(R.drawable.ic_info_custom),
                                                    label = "Version",
                                                    value = "3.0.5"
                                                )
                                            }
                                        }
                                    }
                                    if (shouldShow("For Feedback", "880ramansingh@gmail.com")) {
                                        item {
                                            CoverPlaySettingsFeedbackRow(
                                                icon = painterResource(R.drawable.ic_feedback),
                                                label = "For Feedback",
                                                email = "880ramansingh@gmail.com",
                                                onClick = {
                                                    runCatching {
                                                        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:880ramansingh@gmail.com"))
                                                        intent.putExtra(Intent.EXTRA_SUBJECT, "CoverPlay App Feedback")
                                                        context.startActivity(intent)
                                                    }
                                                }
                                            )
                                        }
                                    }
                                    if (shouldShow("Check for Updates")) {
                                        item {
                                            CoverPlaySettingsUpdateRow(
                                                icon = painterResource(R.drawable.ic_updates),
                                                label = "Check for Updates",
                                                status = "v3.0.5 (Latest)",
                                                onClick = {
                                                    android.widget.Toast.makeText(context, Quips.update(), android.widget.Toast.LENGTH_SHORT).show()
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                            if (shouldShow("Tutorial") || shouldShow("Replay app tour") || shouldShow("Replay player tour")) {
                                settingsCard("Tutorial") {
                                    if (shouldShow("Replay app tour", "Walk through CoverPlay again")) {
                                        item {
                                            CoverPlaySettingsClickRow(
                                                icon = painterResource(R.drawable.ic_info_custom),
                                                label = "Replay app tour",
                                                value = "",
                                                onClick = { onReplayAppTour() }
                                            )
                                        }
                                    }
                                    if (shouldShow("Replay player tour", "Gestures and controls guide")) {
                                        item {
                                            CoverPlaySettingsClickRow(
                                                icon = painterResource(R.drawable.ic_play_outline),
                                                label = "Replay player tour",
                                                value = "",
                                                onClick = {
                                                    TutorialPrefs.resetPlayer(context)
                                                    android.widget.Toast.makeText(context, "Open any video to replay the player tour. Lights, camera, fingers!", android.widget.Toast.LENGTH_LONG).show()
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    item {
                        CoverPlayQuipLine(
                            Quips.settingsPage(targetPage),
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 24.dp),
                            align = TextAlign.Center
                        )
                    }
                }
            }
        }
    }

    if (showHapticSheet) {
        CoverPlayBottomSheet(onDismiss = { showHapticSheet = false }) {
            val view = androidx.compose.ui.platform.LocalView.current
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                CoverPlayBrandTitleText("Haptic Strength")
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Vibration feedback on taps and gestures",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(28.dp))
                Text(
                    text = CoverPlayHaptics.label(hapticStrength),
                    style = MaterialTheme.typography.displayMedium,
                    color = accentColor,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(32.dp))
                CoverPlaySlider(
                    value = hapticStrength.toFloat(),
                    onValueChange = { hapticStrength = (it / 5f).roundToInt() * 5 },
                    onValueChangeFinished = {
                        CoverPlayHaptics.setStrength(context, hapticStrength)
                        CoverPlayHaptics.perform(view)
                    },
                    valueRange = 0f..100f
                )
                Spacer(modifier = Modifier.height(32.dp))
                CoverPlayButton(
                    text = "Reset to default",
                    onClick = {
                        hapticStrength = CoverPlayHaptics.DEFAULT_STRENGTH
                        CoverPlayHaptics.setStrength(context, hapticStrength)
                        CoverPlayHaptics.perform(view)
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    if (showSpeedSheet) {
        CoverPlayBottomSheet(onDismiss = { showSpeedSheet = false }) {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                CoverPlayBrandTitleText("Playback Speed")
                Spacer(modifier = Modifier.height(32.dp))
                Text(text = formatSpeed(defaultSpeed), style = MaterialTheme.typography.displayMedium, color = accentColor, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(40.dp))
                CoverPlaySlider(
                    value = defaultSpeed,
                    onValueChange = { defaultSpeed = (it * 4f).roundToInt() / 4f },
                    onValueChangeFinished = { save { putFloat(KEY_DEFAULT_SPEED, defaultSpeed) } },
                    valueRange = 0.25f..4.0f
                )
                Spacer(modifier = Modifier.height(32.dp))
                CoverPlayButton(text = "Reset to 1.0x", onClick = { defaultSpeed = 1.0f; save { putFloat(KEY_DEFAULT_SPEED, 1.0f) }; showSpeedSheet = false }, modifier = Modifier.fillMaxWidth())
            }
        }
    }

    if (showAspectSheet) {
        CoverPlayBottomSheet(onDismiss = { showAspectSheet = false }) {
            Column(modifier = Modifier.fillMaxWidth()) {
                CoverPlayBrandTitleText("Aspect Ratio")
                Spacer(modifier = Modifier.height(24.dp))
                aspectOptions.forEach { option ->
                    val selected = defaultAspect == option
                    CoverPlaySelectableCard(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        selected = selected,
                        onClick = {
                            defaultAspect = option
                            save { putString(KEY_DEFAULT_ASPECT, option) }
                            showAspectSheet = false
                        }
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(option, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium, color = if(selected) accentColor else LocalAppUiSettings.current.palette.textPrimary, fontWeight = FontWeight.SemiBold)
                            if (selected) Icon(painterResource(R.drawable.ic_check_custom), null, tint = accentColor)
                        }
                    }
                }
            }
        }
    }

    if (showKeepSheet) {
        CoverPlayBottomSheet(onDismiss = { showKeepSheet = false }) {
            Column(modifier = Modifier.fillMaxWidth()) {
                CoverPlayBrandTitleText("History Retention")
                Spacer(modifier = Modifier.height(24.dp))
                keepDaysOptions.forEach { days ->
                    val selected = keepDays == days
                    CoverPlaySelectableCard(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        selected = selected,
                        onClick = {
                            keepDays = days
                            save { putInt(KEY_RECENTS_KEEP_DAYS, days) }
                            showKeepSheet = false
                        }
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("$days Days", modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium, color = if(selected) accentColor else LocalAppUiSettings.current.palette.textPrimary, fontWeight = FontWeight.SemiBold)
                            if (selected) Icon(painterResource(R.drawable.ic_check_custom), null, tint = accentColor)
                        }
                    }
                }
            }
        }
    }

    if (showSkipSheet) {
        CoverPlayBottomSheet(onDismiss = { showSkipSheet = false }) {
            Column(modifier = Modifier.fillMaxWidth()) {
                CoverPlayBrandTitleText("Seek Interval")
                Spacer(modifier = Modifier.height(24.dp))
                skipOptions.forEach { seconds ->
                    val selected = skipSeconds == seconds
                    CoverPlaySelectableCard(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        selected = selected,
                        onClick = {
                            skipSeconds = seconds
                            save { putInt(KEY_SKIP_SECONDS, seconds) }
                            showSkipSheet = false
                        }
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("$seconds Seconds", modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium, color = if(selected) accentColor else LocalAppUiSettings.current.palette.textPrimary, fontWeight = FontWeight.SemiBold)
                            if (selected) Icon(painterResource(R.drawable.ic_check_custom), null, tint = accentColor)
                        }
                    }
                }
            }
        }
    }

    if (showClearDialog) {
        CoverPlayAlertDialog(
            onDismiss = { showClearDialog = false },
            title = "Clear History",
            text = "Are you sure? This will remove all items from your continue watching list.",
            confirmButton = {
                CoverPlayButton(text = "Clear All", onClick = { onClearHistory(); showClearDialog = false })
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) { Text("Cancel", color = LocalAppUiSettings.current.palette.textSecondary, fontWeight = FontWeight.SemiBold) }
            }
        )
    }
}

@Composable
private fun CoverPlayFontPicker(selected: AppFont, onSelect: (AppFont) -> Unit) {
    val fonts = AppFont.entries
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        fonts.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { f ->
                    CoverPlayFontCard(f, f == selected, { onSelect(f) }, Modifier.weight(1f))
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun CoverPlayFontCard(font: AppFont, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val ui = LocalAppUiSettings.current
    val haptic = rememberCoverPlayHaptic()
    val shape = RoundedCornerShape(CoverPlayTokens.Radius.input)
    Column(
        modifier = modifier
            .clip(shape)
            .background(if (selected) androidx.compose.ui.graphics.lerp(ui.palette.container, ui.accent, 0.18f) else ui.palette.container)
            .border(if (selected) 1.5.dp else 1.dp, if (selected) ui.accent else ui.palette.outlineFaint, shape)
            .aliveClickable { haptic(); onClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Text(
            "Aa",
            fontFamily = font.family,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            color = if (selected) ui.accent else ui.palette.textPrimary
        )
        Spacer(Modifier.height(2.dp))
        Text(font.label, fontFamily = font.family, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = ui.palette.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(font.tagline, fontFamily = font.family, fontSize = 11.sp, color = ui.palette.textTertiary, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun CoverPlaySettingsCategoryCard(
    icon: Painter,
    label: String,
    sublabel: String,
    index: Int,
    count: Int,
    onClick: () -> Unit
) {
    val ui = LocalAppUiSettings.current
    val shape = segmentedShape(index, count)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = CoverPlayDimens.screenHorizontal, vertical = 1.5.dp)
            .staggerIn(index)
            .clip(shape)
            .background(ui.palette.container)
    ) {
        CoverPlaySettingsRow(icon = icon, title = label, subtitle = sublabel, active = true, onClick = onClick) { CoverPlayRowChevron() }
    }
}

private class CardRows {
    val rows = mutableListOf<@Composable () -> Unit>()
    fun item(content: @Composable () -> Unit) { rows += content }
}

private fun LazyListScope.settingsCard(title: String, content: CardRows.() -> Unit) {
    val c = CardRows().apply(content)
    if (c.rows.isEmpty()) return
    item(key = "hdr-$title", contentType = "settingsHeader") { CoverPlaySettingsSectionHeader(title) }
    c.rows.forEachIndexed { i, row ->
        item(key = "row-$title-$i", contentType = "settingsRow") {
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = CoverPlayDimens.screenHorizontal)
                    .padding(top = if (i > 0) 3.dp else 0.dp)
            ) {
                CoverPlaySettingsSegment(index = i, count = c.rows.size) { row() }
            }
        }
    }
}

@Composable
private fun CoverPlaySettingsSegment(index: Int, count: Int, content: @Composable () -> Unit) {
    val ui = LocalAppUiSettings.current
    val shape = segmentedShape(index, count)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clip(shape)
            .background(ui.palette.container)
    ) { content() }
}

private data class CategoryEntry(val icon: Int, val label: String, val sub: String, val search: String, val page: SettingsPage)

@Composable
private fun CoverPlaySettingsGlassGroup(content: @Composable ColumnScope.() -> Unit) {
    val ui = LocalAppUiSettings.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = CoverPlayDimens.screenHorizontal)
            .clip(RoundedCornerShape(CoverPlayTokens.Radius.largeCard))
            .background(ui.palette.container),
        content = content
    )
}

@Composable
private fun CoverPlaySettingsCategoryRow(icon: Painter, label: String, sublabel: String, onClick: () -> Unit) {
    CoverPlaySettingsRow(icon = icon, title = label, subtitle = sublabel, active = true, onClick = onClick) { CoverPlayRowChevron() }
}

@Composable
fun CoverPlayDeveloperCard(context: Context, onViewGithub: () -> Unit) {
    val accent = LocalAppUiSettings.current.accent
    var qrBitmap by remember { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }
    LaunchedEffect(DEVELOPER_UPI_ID, DEVELOPER_NAME) {
        withContext(Dispatchers.IO) {
            try { qrBitmap = generateQrBitmap(buildUpiPayString(DEVELOPER_UPI_ID, DEVELOPER_NAME)).asImageBitmap() } catch (_: Exception) {}
        }
    }
    CoverPlaySelectableCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
        Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .shadow(GlowSpec.Elevation, CircleShape, ambientColor = accent.copy(alpha = GlowSpec.AMBIENT), spotColor = accent.copy(alpha = GlowSpec.SPOT))
                        .clip(CircleShape)
                        .border(2.dp, BrandGradient, CircleShape)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context).data(DEVELOPER_IMAGE_URL).crossfade(true).build(),
                        contentDescription = "Developer Profile",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        DEVELOPER_NAME,
                        style = MaterialTheme.typography.titleLarge,
                        color = LocalAppUiSettings.current.palette.textPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "Independent Developer",
                        style = MaterialTheme.typography.bodySmall,
                        color = LocalAppUiSettings.current.palette.textTertiary
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Surface(
                color = Color.White.copy(alpha = 0.98f),
                shape = RoundedCornerShape(CoverPlayCorners.card),
                modifier = Modifier
                    .size(200.dp)
                    .border(1.5.dp, accent.copy(alpha = 0.4f), RoundedCornerShape(CoverPlayCorners.card))
                    .shadow(GlowSpec.Elevation, RoundedCornerShape(CoverPlayCorners.card), ambientColor = accent.copy(alpha = GlowSpec.AMBIENT), spotColor = accent.copy(alpha = GlowSpec.SPOT))
            ) {
                Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                    if (qrBitmap != null) {
                        androidx.compose.foundation.Image(
                            bitmap = qrBitmap!!,
                            contentDescription = "UPI QR code",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        CoverPlayLoader(size = 34.dp, color = accent)
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                "Support via UPI",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp),
                color = accent,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            val ui = LocalAppUiSettings.current
            val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
            val haptic = rememberCoverPlayHapticFeedback()
            Surface(
                onClick = {
                    clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(DEVELOPER_UPI_ID))
                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                    android.widget.Toast.makeText(context, "UPI ID copied: $DEVELOPER_UPI_ID", android.widget.Toast.LENGTH_SHORT).show()
                },
                shape = CircleShape,
                color = ui.palette.containerHigh,
                border = BorderStroke(1.dp, ui.palette.outlineFaint),
                modifier = Modifier.clip(CircleShape)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        DEVELOPER_UPI_ID,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = ui.palette.textPrimary
                    )
                    Icon(
                        painter = painterResource(R.drawable.ic_copy_custom),
                        contentDescription = "Copy UPI ID",
                        tint = ui.palette.textSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            val rowShape = RoundedCornerShape(CoverPlayCorners.card)
            val ghInteraction = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
            val ghHaptic = rememberCoverPlayHaptic()
            val ghPressed by ghInteraction.collectIsPressedAsState()
            val ghScale by androidx.compose.animation.core.animateFloatAsState(if (ghPressed) 0.97f else 1f, label = "ghPress")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer { scaleX = ghScale; scaleY = ghScale }
                    .clip(rowShape)
                    .background(ui.palette.containerHigh)
                    .clickable(interactionSource = ghInteraction, indication = null) { ghHaptic(); onViewGithub() }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(CoverPlayCorners.inner))
                        .background(com.raman.CoverPlay.ui.theme.AccentFill),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(painterResource(R.drawable.ic_github), null, tint = LocalAppUiSettings.current.accent.onFill(), modifier = Modifier.size(22.dp))
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "GitHub Profile",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = ui.palette.textPrimary,
                        maxLines = 1
                    )
                    Text(
                        "raman-codes-6368",
                        style = MaterialTheme.typography.bodySmall,
                        color = ui.palette.textTertiary,
                        maxLines = 1
                    )
                }
                Icon(
                    painterResource(R.drawable.ic_chevron_right_custom), null,
                    tint = ui.palette.textTertiary, modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun CoverPlaySettingsSectionHeader(title: String) {
    CoverPlaySectionHeader(title = title)
}

@Composable
private fun CoverPlaySettingsDivider() {
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = LocalAppUiSettings.current.palette.outlineFaint)
}

@Composable
private fun CoverPlaySettingsToggleRow(
    icon: Painter,
    label: String,
    sublabel: String = "",
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    CoverPlaySettingsRow(
        icon = icon, title = label, subtitle = sublabel,
        onClick = { onCheckedChange(!checked) }
    ) { CoverPlaySwitch(checked = checked, onCheckedChange = onCheckedChange) }
}

@Composable
private fun CoverPlaySettingsToggleRow(
    icon: ImageVector,
    label: String,
    sublabel: String = "",
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) = CoverPlaySettingsToggleRow(rememberVectorPainter(icon), label, sublabel, checked, onCheckedChange)

@Composable
private fun CoverPlaySettingsClickRow(
    icon: Painter,
    label: String,
    value: String,
    tint: Color = Color.Unspecified,
    onClick: () -> Unit
) {
    val isError = tint != Color.Unspecified && tint == MaterialTheme.colorScheme.error
    CoverPlaySettingsRow(
        icon = icon, title = label, iconTint = tint,
        titleColor = if (isError) tint else Color.Unspecified,
        onClick = onClick
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (value.isNotEmpty()) { CoverPlayRowValue(value); Spacer(Modifier.width(8.dp)) }
            CoverPlayRowChevron()
        }
    }
}

@Composable
private fun CoverPlaySettingsClickRow(
    icon: ImageVector,
    label: String,
    value: String,
    tint: Color = Color.Unspecified,
    onClick: () -> Unit
) = CoverPlaySettingsClickRow(rememberVectorPainter(icon), label, value, tint, onClick)

@Composable
private fun CoverPlaySettingsInfoRow(icon: Painter, label: String, value: String) {
    CoverPlaySettingsRow(icon = icon, title = label) {
        Text(
            value, style = MaterialTheme.typography.bodyMedium,
            color = LocalAppUiSettings.current.palette.textSecondary, maxLines = 1
        )
    }
}

@Composable
private fun CoverPlaySettingsFeedbackRow(
    icon: Painter,
    label: String,
    email: String,
    onClick: () -> Unit
) {
    val accent = LocalAppUiSettings.current.accent
    CoverPlaySettingsRow(icon = icon, title = label, subtitle = email, onClick = onClick) {
        Text(
            text = "Send email",
            style = MaterialTheme.typography.labelMedium,
            color = accent
        )
    }
}

@Composable
private fun CoverPlaySettingsUpdateRow(
    icon: Painter,
    label: String,
    status: String,
    onClick: () -> Unit
) {
    CoverPlaySettingsRow(icon = icon, title = label, subtitle = status, onClick = onClick) { CoverPlayRowChevron() }
}

private fun formatSpeed(speed: Float): String =
    if (speed == speed.toLong().toFloat()) "${speed.toInt()}.0×" else "${speed}×"

private const val PLAYLISTS_PREFS = "coverplay_playlists"
private const val PLAYLISTS_KEY   = "playlists"

data class Playlist(val id: String, val name: String, val videoPaths: List<String>)

fun loadPlaylists(context: Context): List<Playlist> {
    val prefs = context.getSharedPreferences(PLAYLISTS_PREFS, Context.MODE_PRIVATE)
    val raw   = prefs.getString(PLAYLISTS_KEY, null) ?: return emptyList()
    val list  = mutableListOf<Playlist>()
    runCatching {
        val arr = JSONArray(raw)
        for (i in 0 until arr.length()) {
            val obj     = arr.getJSONObject(i)
            val paths   = mutableListOf<String>()
            val pathArr = obj.optJSONArray("paths") ?: JSONArray()
            for (j in 0 until pathArr.length()) paths.add(pathArr.getString(j))
            list.add(Playlist(id = obj.getString("id"), name = obj.getString("name"), videoPaths = paths))
        }
    }
    return list
}

fun savePlaylists(context: Context, playlists: List<Playlist>) {
    val arr = JSONArray()
    playlists.forEach { pl ->
        val paths = JSONArray()
        pl.videoPaths.forEach { paths.put(it) }
        arr.put(JSONObject().put("id", pl.id).put("name", pl.name).put("paths", paths))
    }
    context.getSharedPreferences(PLAYLISTS_PREFS, Context.MODE_PRIVATE).edit().putString(PLAYLISTS_KEY, arr.toString()).apply()
}

@OptIn(ExperimentalMaterial3Api::class)
@UnstableApi
@Composable
fun CoverPlayPlaylistsScreen(
    allVideos: List<VideoFile>,
    onVideoClick: (VideoFile, List<VideoFile>, String) -> Unit
) {
    val context = LocalContext.current
    val playlists = remember { mutableStateListOf<Playlist>().also { it.addAll(loadPlaylists(context)) } }
    var openPlaylist     by remember { mutableStateOf<Playlist?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var newPlaylistName  by remember { mutableStateOf("") }
    var addToPlaylist    by remember { mutableStateOf<Playlist?>(null) }
    var deleteTarget     by remember { mutableStateOf<Playlist?>(null) }
    fun persist() { savePlaylists(context, playlists) }

    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var layoutMode by remember { mutableStateOf(LayoutMode.LIST) }
    var showFilterSheet by remember { mutableStateOf(false) }
    var sortOrder by remember { mutableStateOf(FolderSortOrder.NAME_ASC) }
    var videoSortOrder by remember { mutableStateOf(VideoSortOrder.NAME_ASC) }
    val plListState = androidx.compose.foundation.lazy.rememberLazyListState()
    val plGridState = androidx.compose.foundation.lazy.grid.rememberLazyGridState()
    LaunchedEffect(openPlaylist?.id) { plListState.scrollToItem(0); plGridState.scrollToItem(0) }
    val plScrolled by remember {
        derivedStateOf {
            if (layoutMode == LayoutMode.LIST) plListState.firstVisibleItemIndex > 0 || plListState.firstVisibleItemScrollOffset > 0
            else plGridState.firstVisibleItemIndex > 0 || plGridState.firstVisibleItemScrollOffset > 0
        }
    }

    if (showFilterSheet) {
        if (openPlaylist != null) {
            CoverPlayVideoFilterSortSheet(
                currentSort = videoSortOrder,
                currentLayout = layoutMode,
                onSortSelected = { videoSortOrder = it },
                onLayoutSelected = { layoutMode = it },
                onDismiss = { showFilterSheet = false }
            )
        } else {
            CoverPlayFilterSortSheet(
                currentSort = sortOrder,
                currentLayout = layoutMode,
                onSortSelected = { sortOrder = it },
                onLayoutSelected = { layoutMode = it },
                onDismiss = { showFilterSheet = false }
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (openPlaylist != null) {
            val pl = openPlaylist!!
            val videos by remember(pl.videoPaths, allVideos) {
                derivedStateOf {
                    val pathSet = pl.videoPaths.toHashSet()
                    allVideos.filter { it.path in pathSet }
                }
            }
            val filteredVideos by remember(videos, searchQuery, videoSortOrder) {
                derivedStateOf {
                    val base = if (searchQuery.isEmpty()) videos
                    else videos.filter { it.name.contains(searchQuery, ignoreCase = true) }

                    when (videoSortOrder) {
                        VideoSortOrder.NAME_ASC -> base.sortedBy { it.name.lowercase() }
                        VideoSortOrder.NAME_DESC -> base.sortedByDescending { it.name.lowercase() }
                        VideoSortOrder.DATE_NEWEST -> base.reversed()
                        VideoSortOrder.SIZE_DESC -> base.sortedByDescending { it.size }
                        VideoSortOrder.DURATION_DESC -> base.sortedByDescending { it.duration }
                    }
                }
            }

            Scaffold(
                containerColor = Color.Transparent,
                topBar = {
                    CoverPlayMainTopAppBar(
                        title = pl.name,
                        isSearchActive = isSearchActive,
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        onToggleSearch = {
                            isSearchActive = !isSearchActive
                            if (!isSearchActive) searchQuery = ""
                        },
                        showBackButton = true,
                        onBack = {
                            openPlaylist = null
                            isSearchActive = false
                            searchQuery = ""
                        },
                        currentLayout = layoutMode,
                        onToggleLayout = { layoutMode = if (layoutMode == LayoutMode.LIST) LayoutMode.GRID else LayoutMode.LIST },
                        onShowFilter = { showFilterSheet = true },
                        isScrolled = plScrolled
                    )
                }
            ) { padding ->
                val topPad = padding.calculateTopPadding()
                val bottomPad = padding.calculateBottomPadding() + navContentBottomPadding()
                val actionBar: @Composable () -> Unit = {
                    CoverPlayButtonGroup(
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 8.dp),
                        actions = listOf(
                            CoverPlayAction(
                                label = "Play All",
                                primary = true,
                                flex = true,
                                icon = { tint -> Icon(painterResource(R.drawable.ic_play_custom), null, tint = tint, modifier = Modifier.size(20.dp)) },
                                onClick = { if (videos.isNotEmpty()) onVideoClick(videos.first(), videos, pl.name) }
                            ),
                            CoverPlayAction(
                                icon = { tint -> Icon(painterResource(R.drawable.ic_shuffle_custom), null, tint = tint, modifier = Modifier.size(24.dp)) },
                                onClick = { if (videos.isNotEmpty()) onVideoClick(videos.random(), videos, pl.name) }
                            ),
                            CoverPlayAction(
                                icon = { tint -> Icon(painterResource(R.drawable.ic_plus_custom), null, tint = tint, modifier = Modifier.size(24.dp)) },
                                onClick = { addToPlaylist = pl }
                            )
                        )
                    )
                }
                val removeVideo: (VideoFile) -> Unit = { video ->
                    val updated = pl.copy(videoPaths = pl.videoPaths.filter { it != video.path })
                    val idx = playlists.indexOfFirst { it.id == pl.id }
                    if (idx >= 0) { playlists[idx] = updated; openPlaylist = updated; persist() }
                }

                if (videos.isEmpty()) {
                    Column(modifier = Modifier.fillMaxSize().padding(top = topPad)) {
                        actionBar()
                        CoverPlayPlaylistEmptyState(message = "This playlist is currently empty.") { addToPlaylist = pl }
                    }
                } else if (layoutMode == LayoutMode.LIST) {
                    LazyColumn(
                        state = plListState,
                        modifier = Modifier.glassSource().fillMaxSize(),
                        contentPadding = PaddingValues(top = topPad, bottom = bottomPad)
                    ) {
                        item(key = "actions") { actionBar() }
                        item(key = "header") { CoverPlaySectionHeader("Videos", count = videos.size, topPadding = 4.dp) }
                        items(filteredVideos, key = { it.id }) { video ->
                            Column {
                                CoverPlayPlaylistVideoRow(
                                    video = video,
                                    onClick = { onVideoClick(video, videos, pl.name) },
                                    onRemove = { removeVideo(video) }
                                )
                            }
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        state = plGridState,
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.glassSource().fillMaxSize(),
                        contentPadding = PaddingValues(top = topPad, bottom = bottomPad),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item(key = "actions", span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) { actionBar() }
                        item(key = "header", span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                            CoverPlaySectionHeader("Videos", count = videos.size, topPadding = 4.dp)
                        }
                        items(filteredVideos.size, key = { filteredVideos[it].id }) { index ->
                            val video = filteredVideos[index]
                            Box(modifier = Modifier.padding(start = if (index % 2 == 0) 16.dp else 6.dp, end = if (index % 2 == 0) 6.dp else 16.dp)) {
                                CoverPlayVideoGridItem(video = video, onClick = { onVideoClick(video, videos, pl.name) })
                            }
                        }
                    }
                }
            }
        } else {
            val filteredPlaylists by remember(playlists, searchQuery, sortOrder) {
                derivedStateOf {
                    val base = if (searchQuery.isEmpty()) playlists
                    else playlists.filter { it.name.contains(searchQuery, ignoreCase = true) }

                    when (sortOrder) {
                        FolderSortOrder.NAME_ASC -> base.sortedBy { it.name.lowercase() }
                        FolderSortOrder.NAME_DESC -> base.sortedByDescending { it.name.lowercase() }
                        FolderSortOrder.MOST_VIDEOS -> base.sortedByDescending { it.videoPaths.size }
                        FolderSortOrder.FEWEST_VIDEOS -> base.sortedBy { it.videoPaths.size }
                    }
                }
            }

            Scaffold(
                containerColor = Color.Transparent,
                topBar = {
                    CoverPlayMainTopAppBar(
                        title = "Playlists",
                        isSearchActive = isSearchActive,
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        onToggleSearch = {
                            isSearchActive = !isSearchActive
                            if (!isSearchActive) searchQuery = ""
                        },
                        currentLayout = layoutMode,
                        onToggleLayout = { layoutMode = if (layoutMode == LayoutMode.LIST) LayoutMode.GRID else LayoutMode.LIST },
                        onShowFilter = { showFilterSheet = true },
                        isScrolled = plScrolled
                    )
                }
            ) { padding ->
                val topPad = padding.calculateTopPadding()
                val bottomPad = padding.calculateBottomPadding() + navContentBottomPadding()
                val actionBar: @Composable () -> Unit = {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CoverPlayButton(
                            text = "New Playlist",
                            onClick = { showCreateDialog = true },
                            icon = { Icon(painterResource(R.drawable.ic_playlist_add_custom), null, tint = LocalAppUiSettings.current.accent.onFill(), modifier = Modifier.size(22.dp)) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                if (playlists.isEmpty()) {
                    Column(modifier = Modifier.fillMaxSize().padding(top = topPad)) {
                        actionBar()
                        CoverPlayPlaylistEmptyState(message = "Organize your library by creating your first playlist.") { showCreateDialog = true }
                    }
                } else if (layoutMode == LayoutMode.LIST) {
                    LazyColumn(
                        state = plListState,
                        modifier = Modifier.glassSource().fillMaxSize(),
                        contentPadding = PaddingValues(top = topPad, bottom = bottomPad)
                    ) {
                        item(key = "actions") { actionBar() }
                        item(key = "header") { CoverPlaySectionHeader("Your Playlists", count = playlists.size, topPadding = 4.dp) }
                        items(filteredPlaylists, key = { it.id }) { pl ->
                            val thumbVideo = remember(pl.videoPaths, allVideos) {
                                val pathSet = pl.videoPaths.toHashSet()
                                allVideos.firstOrNull { it.path in pathSet }
                            }
                            Column {
                                CoverPlayPlaylistCard(
                                    playlist = pl,
                                    thumbVideo = thumbVideo,
                                    onClick = { openPlaylist = pl },
                                    onDelete = { deleteTarget = pl }
                                )
                            }
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        state = plGridState,
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.glassSource().fillMaxSize(),
                        contentPadding = PaddingValues(top = topPad, bottom = bottomPad),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item(key = "actions", span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) { actionBar() }
                        item(key = "header", span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                            CoverPlaySectionHeader("Your Playlists", count = playlists.size, topPadding = 4.dp)
                        }
                        items(filteredPlaylists.size, key = { filteredPlaylists[it].id }) { index ->
                            val pl = filteredPlaylists[index]
                            val thumbVideo = remember(pl.videoPaths, allVideos) {
                                val pathSet = pl.videoPaths.toHashSet()
                                allVideos.firstOrNull { it.path in pathSet }
                            }
                            Box(modifier = Modifier.padding(start = if (index % 2 == 0) 16.dp else 6.dp, end = if (index % 2 == 0) 6.dp else 16.dp)) {
                                CoverPlayPlaylistCardGrid(
                                    name = pl.name,
                                    count = pl.videoPaths.size,
                                    thumbVideo = thumbVideo,
                                    onClick = { openPlaylist = pl },
                                    onLongClick = { deleteTarget = pl }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CoverPlayDialog(onDismiss = { showCreateDialog = false; newPlaylistName = "" }, title = "New Playlist") {
            OutlinedTextField(
                value = newPlaylistName,
                onValueChange = { newPlaylistName = it },
                placeholder = { Text("Name your playlist...", color = LocalAppUiSettings.current.palette.textTertiary) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LocalAppUiSettings.current.accent,
                    unfocusedBorderColor = LocalAppUiSettings.current.palette.outlineMedium,
                    focusedContainerColor = glassFieldFill(),
                    unfocusedContainerColor = glassFieldFill(),
                    focusedTextColor = LocalAppUiSettings.current.palette.textPrimary,
                    unfocusedTextColor = LocalAppUiSettings.current.palette.textPrimary,
                    cursorColor = LocalAppUiSettings.current.accent
                ),
                shape = RoundedCornerShape(CoverPlayCorners.dialog),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(32.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { showCreateDialog = false; newPlaylistName = "" }) { Text("Cancel", color = LocalAppUiSettings.current.palette.textSecondary, fontWeight = FontWeight.SemiBold) }
                Spacer(modifier = Modifier.width(12.dp))
                CoverPlayButton(
                    enabled = newPlaylistName.isNotBlank(),
                    text = "Create",
                    onClick = {
                        val pl = Playlist(id = System.currentTimeMillis().toString(), name = newPlaylistName.trim(), videoPaths = emptyList())
                        playlists.add(0, pl)
                        persist()
                        showCreateDialog = false
                        newPlaylistName = ""
                        openPlaylist = pl
                    }
                )
            }
        }
    }

    if (deleteTarget != null) {
        CoverPlayAlertDialog(
            onDismiss = { deleteTarget = null },
            title = "Delete Playlist?",
            text = "Are you sure you want to remove \"${deleteTarget!!.name}\"?",
            confirmButton = {
                CoverPlayButton(text = "Delete", onClick = {
                    playlists.removeAll { it.id == deleteTarget!!.id }
                    persist()
                    deleteTarget = null
                })
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) { Text("Cancel", color = LocalAppUiSettings.current.palette.textSecondary, fontWeight = FontWeight.SemiBold) }
            }
        )
    }

    if (addToPlaylist != null) {
        val pl = addToPlaylist!!
        CoverPlayBottomSheet(onDismiss = { addToPlaylist = null }) {
            Column(modifier = Modifier.fillMaxWidth()) {
                CoverPlayBrandTitleText("Add to Playlist")
                Spacer(modifier = Modifier.height(10.dp))
                Text("${pl.videoPaths.size} media already included", style = MaterialTheme.typography.bodySmall, color = LocalAppUiSettings.current.palette.textTertiary)
                Spacer(modifier = Modifier.height(32.dp))
                val available = remember(pl.videoPaths, allVideos) { val pathSet = pl.videoPaths.toHashSet(); allVideos.filter { it.path !in pathSet } }
                if (available.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        Text("Library already fully included.", style = MaterialTheme.typography.bodyLarge, color = LocalAppUiSettings.current.palette.textSecondary, textAlign = TextAlign.Center)
                    }
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 480.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(available, key = { it.id }) { video ->
                            CoverPlayGlassSurface(
                                onClick = {
                                    val updated = pl.copy(videoPaths = pl.videoPaths + video.path)
                                    val idx = playlists.indexOfFirst { it.id == pl.id }
                                    if (idx >= 0) { playlists[idx] = updated; addToPlaylist = updated; if (openPlaylist?.id == pl.id) openPlaylist = updated; persist() }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(CoverPlayCorners.card)
                            ) {
                                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Box(modifier = Modifier.size(80.dp, 50.dp).clip(RoundedCornerShape(CoverPlayCorners.inner)).background(LocalAppUiSettings.current.palette.surfaceRaisedAlt)) {
                                        if (LocalAppUiSettings.current.hideThumbnails) {
                                            CoverPlayTriangleThumbPlaceholder(modifier = Modifier.fillMaxSize())
                                        } else {
                                            AsyncImage(model = remember(video.uri) { ImageRequest.Builder(context).data(video.uri).videoFrameMillis(1000).crossfade(true).build() }, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                        }
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = video.name, style = MaterialTheme.typography.bodyMedium, color = LocalAppUiSettings.current.palette.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold)
                                        Text(text = formatDuration(video.duration), style = MaterialTheme.typography.labelSmall, color = LocalAppUiSettings.current.palette.textTertiary)
                                    }
                                    Icon(painterResource(R.drawable.ic_plus_custom), null, tint = LocalAppUiSettings.current.accent, modifier = Modifier.size(24.dp))
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                CoverPlayButton(text = "Close", onClick = { addToPlaylist = null }, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun CoverPlayPlaylistCard(playlist: Playlist, thumbVideo: VideoFile?, onClick: () -> Unit, onDelete: () -> Unit) {
    CoverPlayPlaylistCardRow(
        name = playlist.name,
        count = playlist.videoPaths.size,
        thumbVideo = thumbVideo,
        onClick = onClick,
        onDelete = onDelete
    )
}

@Composable
private fun CoverPlayPlaylistVideoRow(video: VideoFile, onClick: () -> Unit, onRemove: () -> Unit) {
    CoverPlayVideoCardRow(
        video = video,
        onClick = onClick,
        trailing = {
            CoverPlayVideoCardAction(
                painter = painterResource(R.drawable.ic_minus_custom),
                description = "Remove from playlist",
                onClick = onRemove
            )
        }
    )
}

@Composable
private fun CoverPlayPlaylistEmptyState(message: String, onAction: () -> Unit) {
    CoverPlayPlaylistEmptyCard(
        title = "Empty playlist",
        message = message,
        actionLabel = "Add videos",
        onAction = onAction
    )
}

@Composable
fun CoverPlayAddToPlaylistDialog(video: VideoFile, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val playlists = remember { mutableStateListOf<Playlist>().also { it.addAll(loadPlaylists(context)) } }

    CoverPlayBottomSheet(onDismiss = onDismiss) {
        Text("Add to Playlist", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, color = LocalAppUiSettings.current.palette.textPrimary)
        CoverPlayQuipLine(Quips.dialog("add to playlist"), modifier = Modifier.padding(top = 2.dp))
        Spacer(modifier = Modifier.height(22.dp))

        if (playlists.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                Text("No playlists found. Create one in the Playlists tab.", style = MaterialTheme.typography.bodyLarge, color = LocalAppUiSettings.current.palette.textSecondary, textAlign = TextAlign.Center)
            }
        } else {
            LazyColumn(
                modifier = Modifier.heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(playlists) { pl ->
                    val isAlreadyIn = pl.videoPaths.contains(video.path)
                    CoverPlayCardSurface(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = CoverPlayCorners.card,
                        onClick = {
                            if (!isAlreadyIn) {
                                val updated = pl.copy(videoPaths = pl.videoPaths + video.path)
                                val idx = playlists.indexOfFirst { it.id == pl.id }
                                if (idx >= 0) {
                                    playlists[idx] = updated
                                    savePlaylists(context, playlists)
                                    onDismiss()
                                }
                            }
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CoverPlayBrandIconContainer(active = true, cornerRadius = CoverPlayCorners.tile, size = 40.dp) {
                                Icon(painterResource(R.drawable.ic_playlist_custom), null, tint = LocalAppUiSettings.current.accent)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(pl.name, color = LocalAppUiSettings.current.palette.textPrimary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                Text("${pl.videoPaths.size} Videos", style = MaterialTheme.typography.labelSmall, color = LocalAppUiSettings.current.palette.textSecondary)
                            }
                            if (isAlreadyIn) {
                                Icon(painterResource(R.drawable.ic_check_custom), null, tint = BrandAccent, modifier = Modifier.size(18.dp))
                            } else {
                                Icon(painterResource(R.drawable.ic_chevron_right_custom), null, tint = LocalAppUiSettings.current.palette.textTertiary.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        CoverPlayButton(text = "Close", onClick = onDismiss, modifier = Modifier.fillMaxWidth())
    }
}
