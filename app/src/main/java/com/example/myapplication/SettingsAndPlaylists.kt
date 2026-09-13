package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.rounded.PlaylistAdd
import androidx.compose.material.icons.automirrored.rounded.PlaylistPlay
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.example.myapplication.ui.theme.*
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.media3.common.util.UnstableApi
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.videoFrameMillis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.roundToInt

// ═════════════════════════════════════════════════════════════════════════════
// SETTINGS SCREEN
// ═════════════════════════════════════════════════════════════════════════════

private const val SETTINGS_PREFS = "coverplay_settings_v3"

// Keys — playback
private const val KEY_DEFAULT_SPEED       = "default_speed"
private const val KEY_RESUME_PLAYBACK     = "resume_playback"
private const val KEY_BRIGHTNESS_GESTURE  = "brightness_gesture"
private const val KEY_VOLUME_GESTURE      = "volume_gesture"
private const val KEY_PINCH_TO_ZOOM       = "pinch_to_zoom"
private const val KEY_BACKGROUND_PLAY     = "background_play"
private const val KEY_RECENTS_KEEP_DAYS   = "recents_keep_days"
private const val KEY_DEFAULT_ASPECT      = "default_aspect"
// Keys — appearance
private const val KEY_ACCENT_COLOR        = "accent_color"
private const val KEY_FONT_SCALE          = "font_scale"
private const val KEY_SHOW_DURATION_BADGE = "show_duration_badge"
private const val KEY_COMPACT_LIST        = "compact_list"
private const val KEY_USE_SYSTEM_FONT     = "use_system_font"
// Keys — privacy & storage
private const val KEY_HIDE_THUMBNAILS     = "hide_thumbnails"
private const val KEY_CONFIRM_DELETE      = "confirm_delete"
// Keys — advanced playback
private const val KEY_HARDWARE_DECODE     = "hardware_decode"
private const val KEY_SKIP_SECONDS        = "skip_seconds"
private const val KEY_AUTOPLAY_NEXT       = "autoplay_next"
private const val KEY_LOOP_VIDEO          = "loop_video"
private const val KEY_SUBTLE_LOCK_TIMER   = "subtle_lock_timer"
private const val KEY_SHOW_SPLASH         = "show_splash"

private const val DEVELOPER_NAME      = "Raman Singh"
private const val DEVELOPER_UPI_ID    = "7982360571@fam"
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
        KEY_SKIP_SECONDS       to prefs.getInt(KEY_SKIP_SECONDS, 10),
        KEY_AUTOPLAY_NEXT      to prefs.getBoolean(KEY_AUTOPLAY_NEXT, false),
        KEY_LOOP_VIDEO         to prefs.getBoolean(KEY_LOOP_VIDEO, false),
        KEY_HARDWARE_DECODE    to prefs.getBoolean(KEY_HARDWARE_DECODE, true),
        KEY_SUBTLE_LOCK_TIMER  to prefs.getBoolean(KEY_SUBTLE_LOCK_TIMER, true),
        KEY_HIDE_THUMBNAILS    to prefs.getBoolean(KEY_HIDE_THUMBNAILS, false),
        KEY_SHOW_SPLASH        to prefs.getBoolean(KEY_SHOW_SPLASH, true),
        KEY_CONFIRM_DELETE     to prefs.getBoolean(KEY_CONFIRM_DELETE, true),
        KEY_SHOW_DURATION_BADGE to prefs.getBoolean(KEY_SHOW_DURATION_BADGE, true),
        KEY_COMPACT_LIST       to prefs.getBoolean(KEY_COMPACT_LIST, false)
    )
}

enum class SettingsPage {
    MAIN, APPEARANCE, PLAYBACK, GESTURES, PRIVACY_HISTORY, ABOUT
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onClearHistory: () -> Unit) {
    val context = LocalContext.current
    val prefs   = remember { context.getSharedPreferences(SETTINGS_PREFS, Context.MODE_PRIVATE) }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val scope = rememberCoroutineScope()

    var currentPage by remember { mutableStateOf(SettingsPage.MAIN) }

    // ── Internal Navigation Back Handler ──────────────────────────────────
    BackHandler(enabled = currentPage != SettingsPage.MAIN) {
        currentPage = SettingsPage.MAIN
    }

    // ── Playback ──────────────────────────────────────────────────────────────
    var defaultSpeed     by remember { mutableFloatStateOf(prefs.getFloat(KEY_DEFAULT_SPEED, 1.0f)) }
    var resumePlayback   by remember { mutableStateOf(prefs.getBoolean(KEY_RESUME_PLAYBACK, true)) }
    var autoplayNext     by remember { mutableStateOf(prefs.getBoolean(KEY_AUTOPLAY_NEXT, false)) }
    var loopVideo        by remember { mutableStateOf(prefs.getBoolean(KEY_LOOP_VIDEO, false)) }
    var hardwareDecode   by remember { mutableStateOf(prefs.getBoolean(KEY_HARDWARE_DECODE, true)) }
    var subtleLockTimer  by remember { mutableStateOf(prefs.getBoolean(KEY_SUBTLE_LOCK_TIMER, true)) }
    val skipOptions      = listOf(5, 10, 15, 30)
    var skipSeconds      by remember { mutableIntStateOf(prefs.getInt(KEY_SKIP_SECONDS, 10)) }
    var showSkipSheet    by remember { mutableStateOf(false) }

    // ── Gestures ──────────────────────────────────────────────────────────────
    var brightnessGesture by remember { mutableStateOf(prefs.getBoolean(KEY_BRIGHTNESS_GESTURE, true)) }
    var volumeGesture     by remember { mutableStateOf(prefs.getBoolean(KEY_VOLUME_GESTURE, true)) }
    var pinchToZoom       by remember { mutableStateOf(prefs.getBoolean(KEY_PINCH_TO_ZOOM, true)) }
    var backgroundPlay    by remember { mutableStateOf(prefs.getBoolean(KEY_BACKGROUND_PLAY, false)) }

    // ── Display ───────────────────────────────────────────────────────────────
    val aspectOptions   = listOf("Fit", "Fill", "Zoom")
    var defaultAspect   by remember { mutableStateOf(prefs.getString(KEY_DEFAULT_ASPECT, "Fit") ?: "Fit") }
    var showAspectSheet by remember { mutableStateOf(false) }

    // ── Appearance ────────────────────────────────────────────────────────────
    val accentOptions   = listOf(BrandPrimary, BrandSecondary, BrandTertiary, BrandAccent, BrandMagenta)
    var accentColor     by remember { mutableStateOf(Color(prefs.getInt(KEY_ACCENT_COLOR, BrandPrimary.toArgb()))) }
    var showDurationBadge by remember { mutableStateOf(prefs.getBoolean(KEY_SHOW_DURATION_BADGE, true)) }
    var compactList     by remember { mutableStateOf(prefs.getBoolean(KEY_COMPACT_LIST, false)) }
    var useSystemFont   by remember { mutableStateOf(prefs.getBoolean(KEY_USE_SYSTEM_FONT, false)) }
    val currentUiSettings = LocalAppUiSettings.current
    var themeMode       by remember { mutableStateOf(currentUiSettings.themeMode) }
    var showSplash      by remember { mutableStateOf(prefs.getBoolean(KEY_SHOW_SPLASH, true)) }

    // ── Typography ────────────────────────────────────────────────────────────
    var fontScale by remember { mutableFloatStateOf(prefs.getFloat(KEY_FONT_SCALE, 1.0f)) }

    // ── Privacy & Storage ─────────────────────────────────────────────────────
    var hideThumbnails  by remember { mutableStateOf(prefs.getBoolean(KEY_HIDE_THUMBNAILS, false)) }
    var confirmDelete   by remember { mutableStateOf(prefs.getBoolean(KEY_CONFIRM_DELETE, true)) }

    // ── History ───────────────────────────────────────────────────────────────
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
        SettingsPage.PRIVACY_HISTORY -> "Privacy & History"
        SettingsPage.ABOUT           -> "About CoverPlay"
    }

    CinematicBackground {
        Scaffold(
            modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
            containerColor = Color.Transparent,
            topBar = {
                MainTopAppBar(
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
                    showExtraActions = false
                )
            }
        ) { padding ->
            AnimatedContent(
                targetState = currentPage,
                transitionSpec = {
                    if (targetState != SettingsPage.MAIN) {
                        (slideInHorizontally { it } + fadeIn()).togetherWith(slideOutHorizontally { -it } + fadeOut())
                    } else {
                        (slideInHorizontally { -it } + fadeIn()).togetherWith(slideOutHorizontally { it } + fadeOut())
                    }
                },
                label = "SettingsNav",
                modifier = Modifier.fillMaxSize().padding(padding)
            ) { targetPage ->
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 140.dp)
                ) {
                    when (targetPage) {
                        SettingsPage.MAIN -> {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        BrandIconContainer(size = 80.dp, cornerRadius = 24.dp) {
                                            Icon(
                                                painter = painterResource(R.drawable.ic_settings_custom),
                                                contentDescription = null,
                                                tint = accentColor,
                                                modifier = Modifier.size(40.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = "Preferences",
                                            style = MaterialTheme.typography.headlineSmall,
                                            color = LocalAppUiSettings.current.palette.textPrimary,
                                            fontWeight = FontWeight.Black
                                        )
                                        Text(
                                            text = "Configure your premium experience",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = LocalAppUiSettings.current.palette.textTertiary
                                        )
                                    }
                                }
                            }
                            
                            item { SettingsSectionHeader("Categories") }
                            
                            if (shouldShow("Appearance", "Themes, accent colors, and layout")) {
                                item {
                                    SettingsCategoryCard(
                                        icon = painterResource(R.drawable.ic_palette_custom),
                                        label = "Appearance",
                                        sublabel = "Themes, accent colors, and layout",
                                        onClick = { currentPage = SettingsPage.APPEARANCE }
                                    )
                                }
                            }
                            if (shouldShow("Playback Engine", "Speed, seeking, and background play")) {
                                item {
                                    SettingsCategoryCard(
                                        icon = painterResource(R.drawable.ic_play_custom),
                                        label = "Playback Engine",
                                        sublabel = "Speed, seeking, and background play",
                                        onClick = { currentPage = SettingsPage.PLAYBACK }
                                    )
                                }
                            }
                            if (shouldShow("Gestures & Controls", "Swipe controls and pinch to zoom")) {
                                item {
                                    SettingsCategoryCard(
                                        icon = painterResource(R.drawable.ic_volume_custom),
                                        label = "Gestures & Controls",
                                        sublabel = "Swipe controls and pinch to zoom",
                                        onClick = { currentPage = SettingsPage.GESTURES }
                                    )
                                }
                            }
                            if (shouldShow("Privacy & History", "Watch history and library privacy")) {
                                item {
                                    SettingsCategoryCard(
                                        icon = painterResource(R.drawable.ic_history_custom),
                                        label = "Privacy & History",
                                        sublabel = "Watch history and library privacy",
                                        onClick = { currentPage = SettingsPage.PRIVACY_HISTORY }
                                    )
                                }
                            }
                            if (shouldShow("About", "App version and developer info")) {
                                item {
                                    SettingsCategoryCard(
                                        icon = painterResource(R.drawable.ic_info_custom),
                                        label = "About",
                                        sublabel = "App version and developer info",
                                        onClick = { currentPage = SettingsPage.ABOUT }
                                    )
                                }
                            }
                        }

                        SettingsPage.APPEARANCE -> {
                            if (shouldShow("Visual Style") || shouldShow("Accent Color") || shouldShow("Appearance Mode")) {
                                item { SettingsSectionHeader("Visual Style") }

                                if (shouldShow("Accent Color", "Personalize UI highlights")) {
                                    item {
                                        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                BrandIconContainer(size = 44.dp, cornerRadius = 14.dp) {
                                                    Icon(painterResource(R.drawable.ic_palette_custom), null, tint = accentColor, modifier = Modifier.size(22.dp))
                                                }
                                                Spacer(modifier = Modifier.width(18.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text("Accent Color", style = MaterialTheme.typography.titleMedium, color = LocalAppUiSettings.current.palette.textPrimary, fontWeight = FontWeight.Bold)
                                                    Text("Personalize UI highlights", style = MaterialTheme.typography.bodySmall, color = LocalAppUiSettings.current.palette.textSecondary)
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(18.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                accentOptions.forEach { color ->
                                                    BloomColorSwatch(
                                                        color = color,
                                                        selected = accentColor == color,
                                                        onClick = {
                                                            accentColor = color
                                                            save { putInt(KEY_ACCENT_COLOR, color.toArgb()) }
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                if (shouldShow("Show Splash Screen")) {
                                    item {
                                        SettingsToggleRow(
                                            icon = Icons.Rounded.RocketLaunch,
                                            label = "Show Splash Screen",
                                            sublabel = "Play brand animation on startup",
                                            checked = showSplash,
                                            onCheckedChange = {
                                                showSplash = it
                                                save { putBoolean(KEY_SHOW_SPLASH, it) }
                                            }
                                        )
                                    }
                                }

                                if (shouldShow("Appearance Mode", "Light, Dark, or true-black AMOLED")) {
                                    item {
                                        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                BrandIconContainer(size = 44.dp, cornerRadius = 14.dp) {
                                                    Icon(painterResource(R.drawable.ic_moon_custom), null, tint = accentColor, modifier = Modifier.size(22.dp))
                                                }
                                                Spacer(modifier = Modifier.width(18.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text("Appearance Mode", style = MaterialTheme.typography.titleMedium, color = LocalAppUiSettings.current.palette.textPrimary, fontWeight = FontWeight.Bold)
                                                    Text("Light, Dark, or true-black AMOLED", style = MaterialTheme.typography.bodySmall, color = LocalAppUiSettings.current.palette.textSecondary)
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(14.dp))
                                            ThemeModeSelector(
                                                selected = themeMode,
                                                onSelect = {
                                                    themeMode = it
                                                    setThemeMode(context, it)
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            if (shouldShow("UI Elements") || shouldShow("Duration Badges") || shouldShow("Compact Mode")) {
                                item { SettingsSectionHeader("UI Elements") }
                                if (shouldShow("Duration Badges", "Show runtime on thumbnails")) {
                                    item {
                                        SettingsToggleRow(
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
                                        SettingsToggleRow(
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
                            }

                            if (shouldShow("Typography") || shouldShow("Text Scaling") || shouldShow("System Font")) {
                                item { SettingsSectionHeader("Typography") }
                                if (shouldShow("System Font", "Apply phone's default font")) {
                                    item {
                                        SettingsToggleRow(
                                            icon = painterResource(R.drawable.ic_typography_custom),
                                            label = "Use System Font",
                                            sublabel = "Apply phone's default font",
                                            checked = useSystemFont,
                                            onCheckedChange = {
                                                useSystemFont = it
                                                save { putBoolean(KEY_USE_SYSTEM_FONT, it) }
                                            }
                                        )
                                    }
                                }
                                if (shouldShow("Text Scaling")) {
                                    item {
                                        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 14.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                BrandIconContainer(size = 44.dp, cornerRadius = 14.dp) {
                                                    Text("Aa", style = MaterialTheme.typography.titleMedium, color = accentColor, fontWeight = FontWeight.Black)
                                                }
                                                Spacer(modifier = Modifier.width(18.dp))
                                                Text("Text Scaling", style = MaterialTheme.typography.titleMedium, color = LocalAppUiSettings.current.palette.textPrimary, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                                                Surface(
                                                    color = accentColor.copy(alpha = 0.12f),
                                                    shape = RoundedCornerShape(10.dp),
                                                    border = BorderStroke(1.dp, accentColor.copy(alpha = 0.3f))
                                                ) {
                                                    Text(
                                                        "${(fontScale * 100).toInt()}%",
                                                        style = MaterialTheme.typography.labelMedium,
                                                        color = accentColor,
                                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                                        fontWeight = FontWeight.Black
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Slider(
                                                value = fontScale,
                                                onValueChange = { fontScale = it },
                                                onValueChangeFinished = { save { putFloat(KEY_FONT_SCALE, fontScale) } },
                                                valueRange = 0.85f..1.15f,
                                                colors = SliderDefaults.colors(
                                                    thumbColor = accentColor,
                                                    activeTrackColor = accentColor,
                                                    inactiveTrackColor = LocalAppUiSettings.current.palette.outlineMedium
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        SettingsPage.PLAYBACK -> {
                            if (shouldShow("Core Playback") || shouldShow("Default Speed") || shouldShow("Seek Interval") || shouldShow("Resume Progress") || shouldShow("Autoplay Next") || shouldShow("Loop Video")) {
                                item { SettingsSectionHeader("Core Playback") }
                                if (shouldShow("Default Speed")) {
                                    item {
                                        SettingsClickRow(
                                            icon = painterResource(R.drawable.ic_speed_custom),
                                            label = "Default Speed",
                                            value = formatSpeed(defaultSpeed),
                                            onClick = { showSpeedSheet = true }
                                        )
                                    }
                                }
                                if (shouldShow("Seek Interval", "${skipSeconds}s")) {
                                    item {
                                        SettingsClickRow(
                                            icon = painterResource(R.drawable.ic_forward_custom),
                                            label = "Seek Interval",
                                            value = "${skipSeconds}s",
                                            onClick = { showSkipSheet = true }
                                        )
                                    }
                                }
                                if (shouldShow("Resume Progress")) {
                                    item {
                                        SettingsToggleRow(
                                            icon = painterResource(R.drawable.ic_play_custom),
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
                                        SettingsToggleRow(
                                            icon = painterResource(R.drawable.ic_next_custom),
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
                                        SettingsToggleRow(
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
                                        SettingsToggleRow(
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

                            if (shouldShow("Engine Settings") || shouldShow("Hardware Decoding") || shouldShow("Background Play")) {
                                item { SettingsSectionHeader("Engine Settings") }
                                if (shouldShow("Hardware Decoding", "Smoother playback using GPU")) {
                                    item {
                                        SettingsToggleRow(
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
                                        SettingsToggleRow(
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

                            if (shouldShow("Display") || shouldShow("Default Aspect Ratio")) {
                                item { SettingsSectionHeader("Display") }
                                if (shouldShow("Default Aspect Ratio", defaultAspect)) {
                                    item {
                                        SettingsClickRow(
                                            icon = painterResource(R.drawable.ic_orientation_custom),
                                            label = "Default Aspect Ratio",
                                            value = defaultAspect,
                                            onClick = { showAspectSheet = true }
                                        )
                                    }
                                }
                            }
                        }

                        SettingsPage.GESTURES -> {
                            if (shouldShow("Swipe Gestures") || shouldShow("Brightness Swipe") || shouldShow("Volume Swipe")) {
                                item { SettingsSectionHeader("Swipe Gestures") }
                                if (shouldShow("Brightness Swipe")) {
                                    item {
                                        SettingsToggleRow(
                                            icon = painterResource(R.drawable.ic_brightness_custom),
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
                                        SettingsToggleRow(
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

                            if (shouldShow("Touch") || shouldShow("Pinch to Zoom")) {
                                item { SettingsSectionHeader("Touch") }
                                if (shouldShow("Pinch to Zoom", "Freely zoom video with two fingers")) {
                                    item {
                                        SettingsToggleRow(
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
                            }
                        }

                        SettingsPage.PRIVACY_HISTORY -> {
                            if (shouldShow("Library Privacy") || shouldShow("Hide Thumbnails")) {
                                item { SettingsSectionHeader("Library Privacy") }
                                if (shouldShow("Hide Thumbnails", "Privacy-focused browsing")) {
                                    item {
                                        SettingsToggleRow(
                                            icon = painterResource(R.drawable.ic_description_custom),
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

                            if (shouldShow("History & Data") || shouldShow("Retention Period") || shouldShow("Confirm Deletion") || shouldShow("Clear All History")) {
                                item { SettingsSectionHeader("History & Data") }
                                if (shouldShow("Retention Period", "$keepDays Days")) {
                                    item {
                                        SettingsClickRow(
                                            icon = painterResource(R.drawable.ic_history_custom),
                                            label = "Retention Period",
                                            value = "$keepDays Days",
                                            onClick = { showKeepSheet = true }
                                        )
                                    }
                                }
                                if (shouldShow("Confirm Deletion")) {
                                    item {
                                        SettingsToggleRow(
                                            icon = painterResource(R.drawable.ic_trash_custom),
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
                                        SettingsClickRow(
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

                        SettingsPage.ABOUT -> {
                            if (shouldShow("Developer")) {
                                item { SettingsSectionHeader("Developer") }
                                item {
                                    DeveloperPremiumCard(context = context, onViewGithub = {
                                        val intent = Intent(Intent.ACTION_VIEW, "https://github.com/raman-codes-6368".toUri())
                                        context.startActivity(intent)
                                    })
                                }
                            }
                            if (shouldShow("App Information") || shouldShow("Version") || shouldShow("Build Variant")) {
                                item { SettingsSectionHeader("App Information") }
                                if (shouldShow("Version", "2.0.0")) {
                                    item {
                                        SettingsInfoRow(
                                            icon = painterResource(R.drawable.ic_info_custom),
                                            label = "Version",
                                            value = "2.0.0"
                                        )
                                    }
                                }
                                if (shouldShow("For Feedback", "880ramansingh@gmail.com")) {
                                    item {
                                        SettingsInfoRow(
                                            icon = painterResource(R.drawable.ic_feedback),
                                            label = "For Feedback",
                                            value = "880ramansingh@gmail.com"
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    if (showSpeedSheet) {
        PremiumBottomSheet(onDismiss = { showSpeedSheet = false }) {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                BrandTitleText("Playback Speed")
                Spacer(modifier = Modifier.height(32.dp))
                Text(text = formatSpeed(defaultSpeed), style = MaterialTheme.typography.displayMedium, color = accentColor, fontWeight = FontWeight.Black)
                Spacer(modifier = Modifier.height(40.dp))
                Slider(
                    value = defaultSpeed,
                    onValueChange = { defaultSpeed = (it * 4f).roundToInt() / 4f },
                    onValueChangeFinished = { save { putFloat(KEY_DEFAULT_SPEED, defaultSpeed) } },
                    valueRange = 0.25f..4.0f,
                    colors = SliderDefaults.colors(thumbColor = accentColor, activeTrackColor = accentColor)
                )
                Spacer(modifier = Modifier.height(32.dp))
                PremiumButton(text = "Reset to 1.0x", onClick = { defaultSpeed = 1.0f; save { putFloat(KEY_DEFAULT_SPEED, 1.0f) }; showSpeedSheet = false }, modifier = Modifier.fillMaxWidth())
            }
        }
    }

    if (showAspectSheet) {
        PremiumBottomSheet(onDismiss = { showAspectSheet = false }) {
            Column(modifier = Modifier.fillMaxWidth()) {
                BrandTitleText("Aspect Ratio")
                Spacer(modifier = Modifier.height(24.dp))
                aspectOptions.forEach { option ->
                    val selected = defaultAspect == option
                    PremiumCardSurface(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        cornerRadius = 20.dp,
                        selected = selected,
                        onClick = {
                            defaultAspect = option
                            save { putString(KEY_DEFAULT_ASPECT, option) }
                            showAspectSheet = false
                        }
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(option, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium, color = if(selected) accentColor else LocalAppUiSettings.current.palette.textPrimary, fontWeight = FontWeight.Black)
                            if (selected) Icon(painterResource(R.drawable.ic_check_custom), null, tint = accentColor)
                        }
                    }
                }
            }
        }
    }

    if (showKeepSheet) {
        PremiumBottomSheet(onDismiss = { showKeepSheet = false }) {
            Column(modifier = Modifier.fillMaxWidth()) {
                BrandTitleText("History Retention")
                Spacer(modifier = Modifier.height(24.dp))
                keepDaysOptions.forEach { days ->
                    val selected = keepDays == days
                    PremiumCardSurface(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        cornerRadius = 20.dp,
                        selected = selected,
                        onClick = {
                            keepDays = days
                            save { putInt(KEY_RECENTS_KEEP_DAYS, days) }
                            showKeepSheet = false
                        }
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("$days Days", modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium, color = if(selected) accentColor else LocalAppUiSettings.current.palette.textPrimary, fontWeight = FontWeight.Black)
                            if (selected) Icon(painterResource(R.drawable.ic_check_custom), null, tint = accentColor)
                        }
                    }
                }
            }
        }
    }

    if (showSkipSheet) {
        PremiumBottomSheet(onDismiss = { showSkipSheet = false }) {
            Column(modifier = Modifier.fillMaxWidth()) {
                BrandTitleText("Seek Interval")
                Spacer(modifier = Modifier.height(24.dp))
                skipOptions.forEach { seconds ->
                    val selected = skipSeconds == seconds
                    PremiumCardSurface(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        cornerRadius = 20.dp,
                        selected = selected,
                        onClick = {
                            skipSeconds = seconds
                            save { putInt(KEY_SKIP_SECONDS, seconds) }
                            showSkipSheet = false
                        }
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("$seconds Seconds", modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium, color = if(selected) accentColor else LocalAppUiSettings.current.palette.textPrimary, fontWeight = FontWeight.Black)
                            if (selected) Icon(painterResource(R.drawable.ic_check_custom), null, tint = accentColor)
                        }
                    }
                }
            }
        }
    }

    if (showClearDialog) {
        PremiumAlertDialog(
            onDismiss = { showClearDialog = false },
            title = "Clear History",
            text = "Are you sure? This will remove all items from your continue watching list.",
            confirmButton = {
                PremiumButton(text = "Clear All", onClick = { onClearHistory(); showClearDialog = false })
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) { Text("Cancel", color = LocalAppUiSettings.current.palette.textSecondary, fontWeight = FontWeight.Bold) }
            }
        )
    }
}

// ── Settings category components ──────────────────────────────────────────────

@Composable
private fun SettingsCategoryCard(
    icon: Painter,
    label: String,
    sublabel: String,
    onClick: () -> Unit
) {
    val accent = LocalAppUiSettings.current.accent
    val textPrimary = LocalAppUiSettings.current.palette.textPrimary
    val textTertiary = LocalAppUiSettings.current.palette.textTertiary
    
    PremiumCardSurface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        onClick = onClick,
        cornerRadius = 24.dp
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BrandIconContainer(size = 52.dp, cornerRadius = 16.dp) {
                Icon(
                    painter = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(modifier = Modifier.width(20.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    color = textPrimary,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = sublabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = textTertiary
                )
            }
            Icon(
                painter = painterResource(R.drawable.ic_chevron_right_custom),
                contentDescription = null,
                tint = textTertiary.copy(alpha = 0.5f),
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

// ── Developer card ────────────────────────────────────────────────────────────

@Composable
fun DeveloperPremiumCard(context: Context, onViewGithub: () -> Unit) {
    val accent = LocalAppUiSettings.current.accent
    var qrBitmap by remember { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }
    LaunchedEffect(DEVELOPER_UPI_ID, DEVELOPER_NAME) {
        withContext(Dispatchers.IO) {
            try { qrBitmap = generateQrBitmap(buildUpiPayString(DEVELOPER_UPI_ID, DEVELOPER_NAME)).asImageBitmap() } catch (_: Exception) {}
        }
    }
    PremiumCardSurface(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
        cornerRadius = 36.dp
    ) {
        Column(modifier = Modifier.padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .border(2.dp, BrandGradient, CircleShape)
                        .shadow(16.dp, CircleShape, spotColor = accent)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context).data(DEVELOPER_IMAGE_URL).crossfade(true).build(),
                        contentDescription = "Developer Profile",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.width(22.dp))
                Column {
                    Text(
                        DEVELOPER_NAME,
                        style = MaterialTheme.typography.titleLarge,
                        color = LocalAppUiSettings.current.palette.textPrimary,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        "Independent Developer",
                        style = MaterialTheme.typography.bodySmall,
                        color = LocalAppUiSettings.current.palette.textTertiary
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
            Surface(
                color = Color.White.copy(alpha = 0.98f),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .size(200.dp)
                    .border(1.5.dp, accent.copy(alpha = 0.4f), RoundedCornerShape(28.dp))
                    .shadow(24.dp, RoundedCornerShape(28.dp), spotColor = accent.copy(alpha = 0.25f))
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
                        CircularProgressIndicator(modifier = Modifier.size(28.dp), color = accent, strokeWidth = 3.dp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                "Support via UPI",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp),
                color = accent,
                fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.height(32.dp))
            PremiumButton(
                text = "GitHub Profile",
                onClick = onViewGithub,
                modifier = Modifier.fillMaxWidth(),
                icon = { Icon(painterResource(R.drawable.ic_list_custom), null, tint = Color.White, modifier = Modifier.size(20.dp)) }
            )
        }
    }
}

// ── Settings row components ───────────────────────────────────────────────────

@Composable
private fun SettingsSectionHeader(title: String) {
    val accent = LocalAppUiSettings.current.accent
    Row(modifier = Modifier.padding(start = 24.dp, top = 36.dp, bottom = 14.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.width(5.dp).height(18.dp).clip(CircleShape).background(accent))
        Spacer(modifier = Modifier.width(14.dp))
        Text(text = title.uppercase(), style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 2.sp), color = accent, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp), color = LocalAppUiSettings.current.palette.outlineFaint)
}

@Composable
private fun SettingsToggleRow(
    icon: Painter,
    label: String,
    sublabel: String = "",
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 24.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val accent = LocalAppUiSettings.current.accent
        BrandIconContainer(size = 48.dp, cornerRadius = 14.dp) {
            Icon(icon, null, tint = accent, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.width(20.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = MaterialTheme.typography.titleMedium, color = LocalAppUiSettings.current.palette.textPrimary, fontWeight = FontWeight.Bold)
            if (sublabel.isNotEmpty()) Text(text = sublabel, style = MaterialTheme.typography.bodySmall, color = LocalAppUiSettings.current.palette.textTertiary)
        }
        Icon(
            painter = painterResource(if (checked) R.drawable.ic_toggle_right_custom else R.drawable.ic_toggle_left_custom),
            contentDescription = if (checked) "On" else "Off",
            tint = if (checked) accent else LocalAppUiSettings.current.palette.textTertiary.copy(alpha = 0.6f),
            modifier = Modifier.size(44.dp)
        )
    }
}

@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    label: String,
    sublabel: String = "",
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) = SettingsToggleRow(rememberVectorPainter(icon), label, sublabel, checked, onCheckedChange)

@Composable
private fun SettingsClickRow(
    icon: Painter,
    label: String,
    value: String,
    tint: Color = Color.Unspecified,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val accent = LocalAppUiSettings.current.accent
        val finalTint = if (tint == Color.Unspecified) accent else tint
        BrandIconContainer(size = 48.dp, cornerRadius = 14.dp) {
            Icon(icon, null, tint = finalTint, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.width(20.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = if (tint == MaterialTheme.colorScheme.error) tint else LocalAppUiSettings.current.palette.textPrimary,
            modifier = Modifier.weight(1f),
            fontWeight = FontWeight.Bold
        )
        if (value.isNotEmpty()) {
            Text(text = value, style = MaterialTheme.typography.bodyMedium, color = accent.copy(alpha = 0.8f), fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.width(10.dp))
        }
        Icon(painter = painterResource(R.drawable.ic_chevron_right_custom), contentDescription = null, tint = LocalAppUiSettings.current.palette.textTertiary.copy(alpha = 0.5f), modifier = Modifier.size(22.dp))
    }
}

@Composable
private fun SettingsClickRow(
    icon: ImageVector,
    label: String,
    value: String,
    tint: Color = Color.Unspecified,
    onClick: () -> Unit
) = SettingsClickRow(rememberVectorPainter(icon), label, value, tint, onClick)

@Composable
private fun SettingsInfoRow(icon: Painter, label: String, value: String) {
    val accent = LocalAppUiSettings.current.accent
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
        BrandIconContainer(size = 48.dp, cornerRadius = 14.dp) {
            Icon(icon, null, tint = accent, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.width(20.dp))
        Text(label, style = MaterialTheme.typography.titleMedium, color = LocalAppUiSettings.current.palette.textPrimary, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = LocalAppUiSettings.current.palette.textSecondary, fontWeight = FontWeight.Medium)
    }
}

private fun formatSpeed(speed: Float): String =
    if (speed == speed.toLong().toFloat()) "${speed.toInt()}.0×" else "${speed}×"

// ═════════════════════════════════════════════════════════════════════════════
// PLAYLISTS SCREEN (RE-DESIGNED v4.0)
// ═════════════════════════════════════════════════════════════════════════════

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
fun PlaylistsScreen(
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

    if (showFilterSheet) {
        if (openPlaylist != null) {
            VideoFilterSortSheet(
                currentSort = videoSortOrder,
                currentLayout = layoutMode,
                onSortSelected = { videoSortOrder = it },
                onLayoutSelected = { layoutMode = it },
                onDismiss = { showFilterSheet = false }
            )
        } else {
            FilterSortSheet(
                currentSort = sortOrder,
                currentLayout = layoutMode,
                onSortSelected = { sortOrder = it },
                onLayoutSelected = { layoutMode = it },
                onDismiss = { showFilterSheet = false }
            )
        }
    }

    CinematicBackground {
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

            Column(modifier = Modifier.fillMaxSize()) {
                MainTopAppBar(
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
                    onShowFilter = { showFilterSheet = true }
                )

                // Active Playlist Action Bar
                Row(
                    modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PremiumButton(
                        text = "Play All",
                        onClick = { if(videos.isNotEmpty()) onVideoClick(videos.first(), videos, pl.name) },
                        icon = { Icon(painterResource(R.drawable.ic_play_custom), null, tint = Color.White, modifier = Modifier.size(20.dp)) },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    PremiumIconButton(
                        icon = painterResource(R.drawable.ic_shuffle_custom),
                        onClick = {
                            if(videos.isNotEmpty()) {
                                onVideoClick(videos.random(), videos, pl.name)
                            }
                        }
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    PremiumIconButton(
                        icon = painterResource(R.drawable.ic_plus_custom),
                        onClick = { addToPlaylist = pl }
                    )
                }

                if (videos.isEmpty()) {
                    PlaylistEmptyState(message = "This playlist is currently empty.") { addToPlaylist = pl }
                } else {
                    if (layoutMode == LayoutMode.LIST) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 140.dp)
                        ) {
                            items(filteredVideos, key = { it.id }) { video ->
                                PlaylistVideoRow(
                                    video = video,
                                    onClick = { onVideoClick(video, videos, pl.name) },
                                    onRemove = {
                                        val updated = pl.copy(videoPaths = pl.videoPaths.filter { it != video.path })
                                        val idx = playlists.indexOfFirst { it.id == pl.id }
                                        if (idx >= 0) { playlists[idx] = updated; openPlaylist = updated; persist() }
                                    }
                                )
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = LocalAppUiSettings.current.palette.outlineFaint)
                            }
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 140.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(filteredVideos, key = { it.id }) { video ->
                                VideoGridItem(video = video, onClick = { onVideoClick(video, videos, pl.name) })
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

            Column(modifier = Modifier.fillMaxSize()) {
                MainTopAppBar(
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
                    onShowFilter = { showFilterSheet = true }
                )

                // Global Playlist Actions
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PremiumButton(
                        text = "New Playlist",
                        onClick = { showCreateDialog = true },
                        icon = { Icon(painterResource(R.drawable.ic_playlist_add_custom), null, tint = Color.White, modifier = Modifier.size(22.dp)) },
                        modifier = Modifier.weight(1f)
                    )
                }

                if (playlists.isEmpty()) {
                    PlaylistEmptyState(message = "Organize your library by creating your first playlist.") { showCreateDialog = true }
                } else {
                    if (layoutMode == LayoutMode.LIST) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 140.dp)
                        ) {
                            items(filteredPlaylists, key = { it.id }) { pl ->
                                val thumbVideo = remember(pl.videoPaths, allVideos) {
                                    val pathSet = pl.videoPaths.toHashSet()
                                    allVideos.firstOrNull { it.path in pathSet }
                                }
                                PlaylistCard(
                                    playlist = pl,
                                    thumbVideo = thumbVideo,
                                    onClick = { openPlaylist = pl },
                                    onDelete = { deleteTarget = pl }
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 140.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(filteredPlaylists, key = { it.id }) { pl ->
                                FolderGridItem(
                                    folder = VideoFolder(pl.name, pl.id, pl.videoPaths.size, ""),
                                    onClick = { openPlaylist = pl }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // --- Dialogs ---

    if (showCreateDialog) {
        PremiumDialog(onDismiss = { showCreateDialog = false; newPlaylistName = "" }, title = "New Playlist") {
            OutlinedTextField(
                value = newPlaylistName,
                onValueChange = { newPlaylistName = it },
                placeholder = { Text("Name your playlist...", color = LocalAppUiSettings.current.palette.textTertiary) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = LocalAppUiSettings.current.palette.outlineMedium,
                    focusedTextColor = LocalAppUiSettings.current.palette.textPrimary,
                    unfocusedTextColor = LocalAppUiSettings.current.palette.textPrimary,
                    cursorColor = LocalAppUiSettings.current.accent
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(32.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { showCreateDialog = false; newPlaylistName = "" }) { Text("Cancel", color = LocalAppUiSettings.current.palette.textSecondary, fontWeight = FontWeight.Bold) }
                Spacer(modifier = Modifier.width(12.dp))
                PremiumButton(
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
        PremiumAlertDialog(
            onDismiss = { deleteTarget = null },
            title = "Delete Playlist?",
            text = "Are you sure you want to remove \"${deleteTarget!!.name}\"?",
            confirmButton = {
                PremiumButton(text = "Delete", onClick = {
                    playlists.removeAll { it.id == deleteTarget!!.id }
                    persist()
                    deleteTarget = null
                })
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) { Text("Cancel", color = LocalAppUiSettings.current.palette.textSecondary, fontWeight = FontWeight.Bold) }
            }
        )
    }

    if (addToPlaylist != null) {
        val pl = addToPlaylist!!
        PremiumBottomSheet(onDismiss = { addToPlaylist = null }) {
            Column(modifier = Modifier.fillMaxWidth()) {
                BrandTitleText("Add to Playlist")
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
                            Surface(
                                onClick = {
                                    val updated = pl.copy(videoPaths = pl.videoPaths + video.path)
                                    val idx = playlists.indexOfFirst { it.id == pl.id }
                                    if (idx >= 0) { playlists[idx] = updated; addToPlaylist = updated; if (openPlaylist?.id == pl.id) openPlaylist = updated; persist() }
                                },
                                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)),
                                color = Color.White.copy(alpha = 0.08f),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Box(modifier = Modifier.size(80.dp, 50.dp).clip(RoundedCornerShape(12.dp)).background(LocalAppUiSettings.current.palette.surfaceRaisedAlt)) {
                                        if (LocalAppUiSettings.current.hideThumbnails) {
                                            TriangleThumbPlaceholder(modifier = Modifier.fillMaxSize())
                                        } else {
                                            AsyncImage(model = remember(video.uri) { ImageRequest.Builder(context).data(video.uri).videoFrameMillis(1000).crossfade(true).build() }, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                        }
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = video.name, style = MaterialTheme.typography.bodyMedium, color = LocalAppUiSettings.current.palette.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Bold)
                                        Text(text = formatDuration(video.duration), style = MaterialTheme.typography.labelSmall, color = LocalAppUiSettings.current.palette.textTertiary)
                                    }
                                    Icon(painterResource(R.drawable.ic_plus_custom), null, tint = LocalAppUiSettings.current.accent, modifier = Modifier.size(24.dp))
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                PremiumButton(text = "Close", onClick = { addToPlaylist = null }, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun PlaylistCard(playlist: Playlist, thumbVideo: VideoFile?, onClick: () -> Unit, onDelete: () -> Unit) {
    val context = LocalContext.current
    val accent = LocalAppUiSettings.current.accent

    PremiumCardSurface(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        cornerRadius = 32.dp
    ) {
        Row(modifier = Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            // Visual Stack — Multi-layered icon or thumbnail
            Box(
                modifier = Modifier
                    .size(110.dp, 70.dp)
                    .shadow(12.dp, RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .background(LocalAppUiSettings.current.palette.surfaceRaisedAlt)
                    .border(1.2.dp, Brush.verticalGradient(listOf(Color.White.copy(0.2f), Color.Transparent)), RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (thumbVideo != null) {
                    if (LocalAppUiSettings.current.hideThumbnails) {
                        TriangleThumbPlaceholder(modifier = Modifier.fillMaxSize())
                    } else {
                        AsyncImage(
                            model = remember(thumbVideo.uri) {
                                ImageRequest.Builder(context).data(thumbVideo.uri).videoFrameMillis(1000).crossfade(true).build()
                            },
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                        Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(Color.Black.copy(0.4f)).border(1.dp, Color.White.copy(0.3f), CircleShape), contentAlignment = Alignment.Center) {
                            Icon(painterResource(R.drawable.ic_playlist_custom), null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                } else {
                    Icon(painterResource(R.drawable.ic_playlist_custom), null, tint = accent.copy(alpha = 0.6f), modifier = Modifier.size(36.dp))
                }
            }

            Spacer(modifier = Modifier.width(22.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = playlist.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = LocalAppUiSettings.current.palette.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(color = accent.copy(0.12f), shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, accent.copy(0.25f))) {
                        Text(
                            text = "${playlist.videoPaths.size} ITEMS",
                            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                            color = accent,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            PremiumIconButton(
                icon = painterResource(R.drawable.ic_trash_custom),
                onClick = onDelete,
                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                backgroundColor = Color.Transparent,
                modifier = Modifier.size(40.dp)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PlaylistVideoRow(video: VideoFile, onClick: () -> Unit, onRemove: () -> Unit) {
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = MotionSpec.SoftSpring,
        label = "itemScale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .clip(RoundedCornerShape(24.dp))
            .combinedClickable(
                onClick = onClick,
                interactionSource = interactionSource,
                indication = null
            )
            .background(LocalAppUiSettings.current.palette.surfaceRaised.copy(alpha = 0.5f))
            .border(0.8.dp, LocalAppUiSettings.current.palette.outlineFaint, RoundedCornerShape(24.dp))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp, 60.dp)
                    .shadow(6.dp, RoundedCornerShape(14.dp))
                    .clip(RoundedCornerShape(14.dp))
                    .background(LocalAppUiSettings.current.palette.surfaceRaisedAlt)
                    .border(1.dp, Color.White.copy(0.12f), RoundedCornerShape(14.dp))
            ) {
                if (LocalAppUiSettings.current.hideThumbnails) {
                    TriangleThumbPlaceholder(modifier = Modifier.fillMaxSize())
                } else {
                    AsyncImage(
                        model = remember(video.uri) {
                            ImageRequest.Builder(context).data(video.uri).videoFrameMillis(1000).crossfade(true).build()
                        },
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                    Icon(painterResource(R.drawable.ic_play_custom), null, tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(24.dp))
                }
                if (LocalAppUiSettings.current.showDurationBadge) {
                    DurationBadge(
                        text = formatDuration(video.duration),
                        modifier = Modifier.align(Alignment.BottomEnd).padding(4.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(18.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = formatFileName(video.name),
                    style = MaterialTheme.typography.bodyLarge,
                    color = LocalAppUiSettings.current.palette.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = formatSize(video.size), style = MaterialTheme.typography.labelSmall, color = LocalAppUiSettings.current.palette.textSecondary, fontWeight = FontWeight.Bold)
                    if (video.width > 0) {
                        QualityBadge(width = video.width, height = video.height)
                    }
                }
            }
            PremiumIconButton(
                icon = painterResource(R.drawable.ic_minus_custom),
                onClick = onRemove,
                tint = LocalAppUiSettings.current.palette.textTertiary.copy(alpha = 0.6f),
                backgroundColor = Color.Transparent,
                modifier = Modifier.size(36.dp)
            )
        }
    }
}

@Composable
private fun PlaylistEmptyState(message: String, onAction: () -> Unit) {
    val accent = LocalAppUiSettings.current.accent
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(60.dp)) {
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .background(Brush.radialGradient(listOf(accent.copy(alpha = 0.15f), Color.Transparent)), CircleShape)
                    .border(1.2.dp, Brush.linearGradient(listOf(accent.copy(alpha = 0.35f), LocalAppUiSettings.current.palette.outlineFaint)), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(painterResource(R.drawable.ic_playlist_custom), null, tint = accent.copy(alpha = 0.8f), modifier = Modifier.size(52.dp))
            }
            Spacer(modifier = Modifier.height(36.dp))
            Text(text = "Empty Playlist", style = MaterialTheme.typography.headlineSmall, color = LocalAppUiSettings.current.palette.textPrimary, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(14.dp))
            Text(text = message, style = MaterialTheme.typography.bodyLarge, color = LocalAppUiSettings.current.palette.textSecondary, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(44.dp))
            PremiumButton(text = "Add Content", onClick = onAction, modifier = Modifier.fillMaxWidth(0.85f))
        }
    }
}

@Composable
fun AddToPlaylistDialog(video: VideoFile, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val playlists = remember { mutableStateListOf<Playlist>().also { it.addAll(loadPlaylists(context)) } }

    PremiumBottomSheet(onDismiss = onDismiss) {
        Text("Add to Playlist", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = LocalAppUiSettings.current.palette.textPrimary)
        Spacer(modifier = Modifier.height(24.dp))
        
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
                    PremiumCardSurface(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 20.dp,
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
                            BrandIconContainer(active = true, cornerRadius = 12.dp, size = 40.dp) {
                                Icon(painterResource(R.drawable.ic_playlist_custom), null, tint = Color.White)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(pl.name, color = LocalAppUiSettings.current.palette.textPrimary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                                Text("${pl.videoPaths.size} Videos", style = MaterialTheme.typography.labelSmall, color = LocalAppUiSettings.current.palette.textSecondary)
                            }
                            if (isAlreadyIn) {
                                Icon(Icons.Rounded.Check, null, tint = BrandAccent, modifier = Modifier.size(18.dp))
                            } else {
                                Icon(painterResource(R.drawable.ic_chevron_right_custom), null, tint = LocalAppUiSettings.current.palette.textTertiary.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        PremiumButton(text = "Close", onClick = onDismiss, modifier = Modifier.fillMaxWidth())
    }
}
