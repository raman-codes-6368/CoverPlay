package com.example.myapplication

import android.app.Activity
import android.app.RecoverableSecurityException
import android.content.Context
import android.content.IntentSender
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.videoFrameMillis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.myapplication.ui.theme.*
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import org.json.JSONArray
import org.json.JSONObject

// ═════════════════════════════════════════════════════════════════════════════
// SETTINGS SCREEN
// ═════════════════════════════════════════════════════════════════════════════

private const val SETTINGS_PREFS = "coverplay_settings"

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
private const val KEY_THEME_PRESET        = "theme_preset"
private const val KEY_CARD_STYLE          = "card_style"
private const val KEY_SHOW_DURATION_BADGE = "show_duration_badge"
private const val KEY_COMPACT_LIST        = "compact_list"
// Keys — privacy & storage
private const val KEY_HIDE_THUMBNAILS     = "hide_thumbnails"
private const val KEY_CONFIRM_DELETE      = "confirm_delete"
// Keys — advanced playback
private const val KEY_HARDWARE_DECODE     = "hardware_decode"
private const val KEY_SKIP_SECONDS        = "skip_seconds"
private const val KEY_AUTOPLAY_NEXT       = "autoplay_next"
private const val KEY_LOOP_VIDEO          = "loop_video"

private const val DEVELOPER_NAME      = "Raman Singh"
private const val DEVELOPER_UPI_ID    = "7982360571@fam"
private const val DEVELOPER_IMAGE_URL = "https://github.com/raman-codes-6368.png"

// ── Theme presets ─────────────────────────────────────────────────────────────
enum class ThemePreset(
    val label: String,
    val description: String,
    val accent: Color,
    val bg: Color,
    val surface: Color
) {
    CINEMATIC(
        label = "Cinematic",
        description = "Deep obsidian · Aurora teal",
        accent  = Color(0xFF7FE3D4),
        bg      = Color(0xFF07090E),
        surface = Color(0xFF141923)
    ),
    MIDNIGHT(
        label = "Midnight",
        description = "Pure black · Electric violet",
        accent  = Color(0xFF8A7FE3),
        bg      = Color(0xFF060608),
        surface = Color(0xFF0F0F18)
    ),
    AMBER(
        label = "Ember",
        description = "Dark coal · Warm amber",
        accent  = Color(0xFFFFB347),
        bg      = Color(0xFF0C0A07),
        surface = Color(0xFF1A1610)
    ),
    ROSE(
        label = "Rose",
        description = "Charcoal · Soft rose gold",
        accent  = Color(0xFFE8859A),
        bg      = Color(0xFF0D090A),
        surface = Color(0xFF1C1316)
    ),
    OCEAN(
        label = "Ocean",
        description = "Abyss · Ice blue",
        accent  = Color(0xFF6FB8D9),
        bg      = Color(0xFF050C12),
        surface = Color(0xFF0D1A24)
    );

    companion object {
        fun fromName(name: String) = entries.firstOrNull { it.name == name } ?: CINEMATIC
    }
}

// ── Card styles ────────────────────────────────────────────────────────────────
enum class CardStyle(val label: String, val description: String) {
    GLASS("Glass",    "Frosted card with gradient border"),
    FLAT("Flat",      "Minimal, borderless rows"),
    OUTLINED("Lined", "Crisp 1dp accent outline")
}

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
        KEY_HIDE_THUMBNAILS    to prefs.getBoolean(KEY_HIDE_THUMBNAILS, false),
        KEY_CONFIRM_DELETE     to prefs.getBoolean(KEY_CONFIRM_DELETE, true),
        KEY_SHOW_DURATION_BADGE to prefs.getBoolean(KEY_SHOW_DURATION_BADGE, true),
        KEY_COMPACT_LIST       to prefs.getBoolean(KEY_COMPACT_LIST, false)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onClearHistory: () -> Unit) {
    val context = LocalContext.current
    val prefs   = remember { context.getSharedPreferences(SETTINGS_PREFS, Context.MODE_PRIVATE) }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    // ── Playback ──────────────────────────────────────────────────────────────
    var defaultSpeed     by remember { mutableFloatStateOf(prefs.getFloat(KEY_DEFAULT_SPEED, 1.0f)) }
    var resumePlayback   by remember { mutableStateOf(prefs.getBoolean(KEY_RESUME_PLAYBACK, true)) }
    var autoplayNext     by remember { mutableStateOf(prefs.getBoolean(KEY_AUTOPLAY_NEXT, false)) }
    var loopVideo        by remember { mutableStateOf(prefs.getBoolean(KEY_LOOP_VIDEO, false)) }
    var hardwareDecode   by remember { mutableStateOf(prefs.getBoolean(KEY_HARDWARE_DECODE, true)) }
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
    val accentOptions   = listOf(BrandCyan, BrandVioletWhisper, BrandBlue, BrandIceBlue,
        Color(0xFFFFB347), Color(0xFFE8859A), Color(0xFF7BC67E), Color(0xFFFF8A65))
    var accentColor     by remember { mutableStateOf(Color(prefs.getInt(KEY_ACCENT_COLOR, BrandCyan.toArgb()))) }
    var themePreset     by remember { mutableStateOf(ThemePreset.fromName(prefs.getString(KEY_THEME_PRESET, ThemePreset.CINEMATIC.name) ?: ThemePreset.CINEMATIC.name)) }
    var showThemeSheet  by remember { mutableStateOf(false) }
    var cardStyle       by remember { mutableStateOf(CardStyle.entries.firstOrNull { it.name == prefs.getString(KEY_CARD_STYLE, CardStyle.GLASS.name) } ?: CardStyle.GLASS) }
    var showCardSheet   by remember { mutableStateOf(false) }
    var showDurationBadge by remember { mutableStateOf(prefs.getBoolean(KEY_SHOW_DURATION_BADGE, true)) }
    var compactList     by remember { mutableStateOf(prefs.getBoolean(KEY_COMPACT_LIST, false)) }

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

    fun save(block: android.content.SharedPreferences.Editor.() -> Unit) {
        prefs.edit().apply(block).apply()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { BrandedTopBarTitle(text = "Settings", showIcon = true) },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = SurfaceNavy.copy(alpha = 0.95f)
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = padding.calculateTopPadding(), bottom = 32.dp)
        ) {

            // ── SUPPORT THE DEVELOPER ─────────────────────────────────────────
            item { SettingsSectionHeader("Developer") }
            item {
                DeveloperPremiumCard(context = context, onViewGithub = {
                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://github.com/raman-codes-6368"))
                    context.startActivity(intent)
                })
            }
            item { SettingsDivider() }

            // ── APPEARANCE ────────────────────────────────────────────────────
            item { SettingsSectionHeader("Appearance") }

            // Theme preset picker
            item {
                SettingsClickRow(
                    icon  = rememberVectorPainter(Icons.Default.Palette),
                    label = "Theme",
                    value = themePreset.label,
                    onClick = { showThemeSheet = true }
                )
            }

            // Accent colour swatches
            item {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SettingsIconBox(
                            brush = Brush.linearGradient(listOf(accentColor.copy(alpha = 0.15f), BrandBlueDeep.copy(alpha = 0.12f)))
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_brightness_custom),
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Accent Color", style = MaterialTheme.typography.titleMedium, color = Color.White)
                            Text("Personalise UI highlights", style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    // 8-colour swatch grid, 2 rows of 4
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        listOf(accentOptions.take(4), accentOptions.drop(4)).forEach { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                row.forEach { color ->
                                    val isSelected = accentColor == color
                                    val scale by animateFloatAsState(if (isSelected) 1.15f else 1f, label = "swatch_scale")
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .scale(scale)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(color)
                                            .border(
                                                width = if (isSelected) 2.5.dp else 0.dp,
                                                color = Color.White,
                                                shape = RoundedCornerShape(14.dp)
                                            )
                                            .clickable {
                                                accentColor = color
                                                save { putInt(KEY_ACCENT_COLOR, color.toArgb()) }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Card style
            item {
                SettingsClickRow(
                    icon  = rememberVectorPainter(Icons.Default.Style),
                    label = "Card Style",
                    value = cardStyle.label,
                    onClick = { showCardSheet = true }
                )
            }

            // Show duration badge toggle
            item {
                SettingsToggleRow(
                    icon     = rememberVectorPainter(Icons.Default.Timer),
                    label    = "Duration Badge",
                    sublabel = "Show runtime chip on thumbnails",
                    checked  = showDurationBadge,
                    onCheckedChange = {
                        showDurationBadge = it
                        save { putBoolean(KEY_SHOW_DURATION_BADGE, it) }
                    }
                )
            }

            // Compact list mode
            item {
                SettingsToggleRow(
                    icon     = rememberVectorPainter(Icons.AutoMirrored.Filled.List),
                    label    = "Compact List",
                    sublabel = "Smaller rows, more videos visible",
                    checked  = compactList,
                    onCheckedChange = {
                        compactList = it
                        save { putBoolean(KEY_COMPACT_LIST, it) }
                    }
                )
            }

            // Font scale slider
            item {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SettingsIconBox(
                            brush = Brush.linearGradient(listOf(BrandCyan.copy(alpha = 0.1f), BrandBlueDeep.copy(alpha = 0.1f)))
                        ) {
                            Text("Aa", style = MaterialTheme.typography.titleMedium, color = BrandCyan)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Font Scale", style = MaterialTheme.typography.titleMedium, color = Color.White, modifier = Modifier.weight(1f))
                        Surface(
                            color = BrandCyan.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, BrandCyan.copy(alpha = 0.3f))
                        ) {
                            Text(
                                "${(fontScale * 100).toInt()}%",
                                style = MaterialTheme.typography.labelMedium,
                                color = BrandCyan,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Slider(
                        value = fontScale,
                        onValueChange = { fontScale = it },
                        onValueChangeFinished = { save { putFloat(KEY_FONT_SCALE, fontScale) } },
                        valueRange = 0.8f..1.2f,
                        colors = SliderDefaults.colors(thumbColor = BrandCyan, activeTrackColor = BrandCyan, inactiveTrackColor = OutlineMedium)
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("80%", style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                        Text("120%", style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                    }
                }
            }

            item { SettingsDivider() }

            // ── PLAYBACK ──────────────────────────────────────────────────────
            item { SettingsSectionHeader("Playback") }

            item {
                SettingsClickRow(
                    icon  = painterResource(R.drawable.ic_speed_custom),
                    label = "Default Speed",
                    value = formatSpeed(defaultSpeed),
                    onClick = { showSpeedSheet = true }
                )
            }
            item {
                SettingsClickRow(
                    icon  = rememberVectorPainter(Icons.Default.FastForward),
                    label = "Skip Duration",
                    value = "${skipSeconds}s",
                    onClick = { showSkipSheet = true }
                )
            }
            item {
                SettingsToggleRow(
                    icon    = painterResource(R.drawable.ic_play),
                    label   = "Resume Where Left Off",
                    checked = resumePlayback,
                    onCheckedChange = {
                        resumePlayback = it
                        save { putBoolean(KEY_RESUME_PLAYBACK, it) }
                    }
                )
            }
            item {
                SettingsToggleRow(
                    icon     = rememberVectorPainter(Icons.Default.SkipNext),
                    label    = "Autoplay Next Video",
                    sublabel = "Play next in folder when video ends",
                    checked  = autoplayNext,
                    onCheckedChange = {
                        autoplayNext = it
                        save { putBoolean(KEY_AUTOPLAY_NEXT, it) }
                    }
                )
            }
            item {
                SettingsToggleRow(
                    icon     = rememberVectorPainter(Icons.Default.Loop),
                    label    = "Loop Video",
                    sublabel = "Repeat single video continuously",
                    checked  = loopVideo,
                    onCheckedChange = {
                        loopVideo = it
                        save { putBoolean(KEY_LOOP_VIDEO, it) }
                    }
                )
            }
            item {
                SettingsToggleRow(
                    icon     = rememberVectorPainter(Icons.Default.Memory),
                    label    = "Hardware Decoding",
                    sublabel = "Use GPU decoder for smoother playback",
                    checked  = hardwareDecode,
                    onCheckedChange = {
                        hardwareDecode = it
                        save { putBoolean(KEY_HARDWARE_DECODE, it) }
                    }
                )
            }
            item { SettingsDivider() }

            // ── GESTURES ──────────────────────────────────────────────────────
            item { SettingsSectionHeader("Gestures") }

            item {
                SettingsToggleRow(
                    icon     = painterResource(R.drawable.ic_brightness),
                    label    = "Brightness Swipe",
                    sublabel = "Swipe right side up/down",
                    checked  = brightnessGesture,
                    onCheckedChange = {
                        brightnessGesture = it
                        save { putBoolean(KEY_BRIGHTNESS_GESTURE, it) }
                    }
                )
            }
            item {
                SettingsToggleRow(
                    icon     = painterResource(R.drawable.ic_volume),
                    label    = "Volume Swipe",
                    sublabel = "Swipe left side up/down",
                    checked  = volumeGesture,
                    onCheckedChange = {
                        volumeGesture = it
                        save { putBoolean(KEY_VOLUME_GESTURE, it) }
                    }
                )
            }
            item {
                SettingsToggleRow(
                    icon     = painterResource(R.drawable.ic_aspect_zoom),
                    label    = "Pinch to Zoom",
                    sublabel = "Cycle aspect ratio & free zoom",
                    checked  = pinchToZoom,
                    onCheckedChange = {
                        pinchToZoom = it
                        save { putBoolean(KEY_PINCH_TO_ZOOM, it) }
                    }
                )
            }
            item {
                SettingsToggleRow(
                    icon     = painterResource(R.drawable.ic_background_play),
                    label    = "Background Playback",
                    sublabel = "Continue when app is minimised",
                    checked  = backgroundPlay,
                    onCheckedChange = {
                        backgroundPlay = it
                        save { putBoolean(KEY_BACKGROUND_PLAY, it) }
                    }
                )
            }
            item { SettingsDivider() }

            // ── DISPLAY ───────────────────────────────────────────────────────
            item { SettingsSectionHeader("Display") }

            item {
                SettingsClickRow(
                    icon = when (defaultAspect) {
                        "Fit"  -> painterResource(R.drawable.ic_aspect_fit)
                        "Fill" -> painterResource(R.drawable.ic_aspect_fill)
                        else   -> painterResource(R.drawable.ic_aspect_zoom)
                    },
                    label   = "Default Aspect Ratio",
                    value   = defaultAspect,
                    onClick = { showAspectSheet = true }
                )
            }
            item { SettingsDivider() }

            // ── PRIVACY & STORAGE ─────────────────────────────────────────────
            item { SettingsSectionHeader("Privacy & Storage") }

            item {
                SettingsToggleRow(
                    icon     = rememberVectorPainter(Icons.Default.HideImage),
                    label    = "Hide Thumbnails",
                    sublabel = "Show placeholder instead of video frames",
                    checked  = hideThumbnails,
                    onCheckedChange = {
                        hideThumbnails = it
                        save { putBoolean(KEY_HIDE_THUMBNAILS, it) }
                    }
                )
            }
            item {
                SettingsToggleRow(
                    icon     = rememberVectorPainter(Icons.Default.DeleteForever),
                    label    = "Confirm Before Delete",
                    sublabel = "Show dialog before removing files",
                    checked  = confirmDelete,
                    onCheckedChange = {
                        confirmDelete = it
                        save { putBoolean(KEY_CONFIRM_DELETE, it) }
                    }
                )
            }

            // ── HISTORY ───────────────────────────────────────────────────────
            item { SettingsDivider() }
            item { SettingsSectionHeader("Watch History") }

            item {
                SettingsClickRow(
                    icon    = painterResource(R.drawable.ic_recent_custom),
                    label   = "Keep Recents For",
                    value   = "$keepDays days",
                    onClick = { showKeepSheet = true }
                )
            }
            item {
                SettingsClickRow(
                    icon    = painterResource(R.drawable.ic_trash_custom),
                    label   = "Clear Watch History",
                    value   = "",
                    tint    = MaterialTheme.colorScheme.error,
                    onClick = { showClearDialog = true }
                )
            }
            item { SettingsDivider() }

            // ── ABOUT ─────────────────────────────────────────────────────────
            item { SettingsSectionHeader("About") }

            item {
                SettingsInfoRow(
                    icon  = rememberVectorPainter(Icons.Default.Person),
                    label = "Developer",
                    value = "Raman Singh"
                )
            }
            item {
                SettingsInfoRow(
                    icon  = painterResource(R.drawable.ic_info_custom),
                    label = "Version",
                    value = "1.0.0"
                )
            }
            item {
                SettingsClickRow(
                    icon    = painterResource(R.drawable.ic_list_custom),
                    label   = "GitHub Source",
                    value   = "raman-codes-6368",
                    onClick = {
                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://github.com/raman-codes-6368"))
                        context.startActivity(intent)
                    }
                )
            }
        }
    }

    // ── Bottom Sheets & Dialogs ───────────────────────────────────────────────

    // Theme preset sheet
    if (showThemeSheet) {
        ModalBottomSheet(
            onDismissRequest = { showThemeSheet = false },
            containerColor   = SurfaceNavy,
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            dragHandle = { BottomSheetHandle() }
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 48.dp)) {
                Text("Choose Theme", style = MaterialTheme.typography.headlineMedium, color = Color.White)
                Text("Sets colour palette across the whole app", style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                Spacer(modifier = Modifier.height(20.dp))
                ThemePreset.entries.forEach { preset ->
                    val selected = themePreset == preset
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .clickable {
                                themePreset = preset
                                save {
                                    putString(KEY_THEME_PRESET, preset.name)
                                    putInt(KEY_ACCENT_COLOR, preset.accent.toArgb())
                                }
                                // Also update in-memory accent so UI refreshes immediately
                                accentColor = preset.accent
                                showThemeSheet = false
                            },
                        color  = if (selected) preset.bg.copy(alpha = 0.6f) else SurfaceRaised.copy(alpha = 0.4f),
                        border = BorderStroke(1.5.dp, if (selected) preset.accent.copy(alpha = 0.7f) else OutlineFaint),
                        shape  = RoundedCornerShape(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Mini colour preview
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        Brush.linearGradient(listOf(preset.bg, preset.surface, preset.accent.copy(alpha = 0.5f)))
                                    )
                                    .border(1.dp, preset.accent.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(preset.label, style = MaterialTheme.typography.titleMedium, color = if (selected) preset.accent else Color.White)
                                Text(preset.description, style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                            }
                            if (selected) Icon(Icons.Default.Check, null, tint = preset.accent, modifier = Modifier.size(22.dp))
                        }
                    }
                }
            }
        }
    }

    // Card style sheet
    if (showCardSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCardSheet = false },
            containerColor   = SurfaceNavy,
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            dragHandle = { BottomSheetHandle() }
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 48.dp)) {
                Text("Card Style", style = MaterialTheme.typography.headlineMedium, color = Color.White)
                Spacer(modifier = Modifier.height(20.dp))
                CardStyle.entries.forEach { style ->
                    val selected = cardStyle == style
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                cardStyle = style
                                save { putString(KEY_CARD_STYLE, style.name) }
                                showCardSheet = false
                            },
                        color  = if (selected) BrandBlueDeep.copy(alpha = 0.2f) else Color.Transparent,
                        border = if (selected) BorderStroke(1.dp, BrandCyan.copy(alpha = 0.5f)) else null,
                        shape  = RoundedCornerShape(16.dp)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text(style.label, style = MaterialTheme.typography.titleMedium, color = if (selected) BrandCyan else Color.White)
                                Text(style.description, style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                            }
                            if (selected) Icon(Icons.Default.Check, null, tint = BrandCyan, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    }

    // Skip-seconds sheet
    if (showSkipSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSkipSheet = false },
            containerColor   = SurfaceNavy,
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            dragHandle = { BottomSheetHandle() }
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 48.dp)) {
                Text("Skip Duration", style = MaterialTheme.typography.headlineMedium, color = Color.White)
                Text("How far double-tap or button skips", style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                Spacer(modifier = Modifier.height(20.dp))
                skipOptions.forEach { secs ->
                    val selected = skipSeconds == secs
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(RoundedCornerShape(16.dp))
                            .clickable { skipSeconds = secs; save { putInt(KEY_SKIP_SECONDS, secs) }; showSkipSheet = false },
                        color  = if (selected) BrandBlueDeep.copy(alpha = 0.2f) else Color.Transparent,
                        border = if (selected) BorderStroke(1.dp, BrandCyan.copy(alpha = 0.5f)) else null,
                        shape  = RoundedCornerShape(16.dp)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${secs}s", style = MaterialTheme.typography.titleMedium, color = if (selected) BrandCyan else Color.White)
                            if (selected) Icon(Icons.Default.Check, null, tint = BrandCyan, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    }

    // Speed sheet
    if (showSpeedSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSpeedSheet = false },
            containerColor   = SurfaceNavy,
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            dragHandle = { BottomSheetHandle() }
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 48.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Default Playback Speed", style = MaterialTheme.typography.headlineMedium, color = Color.White)
                Spacer(modifier = Modifier.height(24.dp))
                Surface(shape = RoundedCornerShape(16.dp), color = BrandBlueDeep.copy(alpha = 0.2f), border = BorderStroke(1.dp, BrandCyan.copy(alpha = 0.4f))) {
                    Text(text = formatSpeed(defaultSpeed), style = MaterialTheme.typography.headlineMedium, color = BrandCyan, modifier = Modifier.padding(horizontal = 28.dp, vertical = 12.dp))
                }
                Spacer(modifier = Modifier.height(24.dp))
                Slider(
                    value = defaultSpeed,
                    onValueChange = { defaultSpeed = (Math.round(it * 4f) / 4f).toFloat() },
                    onValueChangeFinished = { save { putFloat(KEY_DEFAULT_SPEED, defaultSpeed) } },
                    valueRange = 0.25f..2.0f,
                    steps = 6,
                    colors = SliderDefaults.colors(thumbColor = BrandCyan, activeTrackColor = BrandCyan)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { speed ->
                        val selected = defaultSpeed == speed
                        Surface(
                            modifier = Modifier.clip(RoundedCornerShape(20.dp)).clickable { defaultSpeed = speed; save { putFloat(KEY_DEFAULT_SPEED, speed) } },
                            color = if (selected) BrandBlueDeep.copy(alpha = 0.25f) else SurfaceRaised.copy(alpha = 0.5f),
                            border = BorderStroke(1.dp, if (selected) BrandCyan.copy(alpha = 0.6f) else OutlineFaint),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(formatSpeed(speed), style = MaterialTheme.typography.labelMedium, color = if (selected) BrandCyan else TextSecondary, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp))
                        }
                    }
                }
            }
        }
    }

    // Aspect ratio sheet
    if (showAspectSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAspectSheet = false },
            containerColor   = SurfaceNavy,
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            dragHandle = { BottomSheetHandle() }
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 48.dp)) {
                Text("Default Aspect Ratio", style = MaterialTheme.typography.headlineMedium, color = Color.White)
                Spacer(modifier = Modifier.height(20.dp))
                aspectOptions.forEach { option ->
                    val selected = defaultAspect == option
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(RoundedCornerShape(16.dp))
                            .clickable { defaultAspect = option; save { putString(KEY_DEFAULT_ASPECT, option) }; showAspectSheet = false },
                        color  = if (selected) BrandBlueDeep.copy(alpha = 0.2f) else Color.Transparent,
                        border = if (selected) BorderStroke(1.dp, BrandCyan.copy(alpha = 0.5f)) else null,
                        shape  = RoundedCornerShape(16.dp)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text(text = option, style = MaterialTheme.typography.titleMedium, color = if (selected) BrandCyan else Color.White)
                                Text(text = when (option) {
                                    "Fit"  -> "Black bars, full video visible"
                                    "Fill" -> "Stretch to fill screen"
                                    else   -> "Crop to fill, no black bars"
                                }, style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                            }
                            if (selected) Icon(Icons.Default.Check, null, tint = BrandCyan, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    }

    // Keep-days sheet
    if (showKeepSheet) {
        ModalBottomSheet(
            onDismissRequest = { showKeepSheet = false },
            containerColor   = SurfaceNavy,
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            dragHandle = { BottomSheetHandle() }
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 48.dp)) {
                Text("Keep Recents For", style = MaterialTheme.typography.headlineMedium, color = Color.White)
                Spacer(modifier = Modifier.height(20.dp))
                keepDaysOptions.forEach { days ->
                    val selected = keepDays == days
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(RoundedCornerShape(16.dp))
                            .clickable { keepDays = days; save { putInt(KEY_RECENTS_KEEP_DAYS, days) }; showKeepSheet = false },
                        color  = if (selected) BrandBlueDeep.copy(alpha = 0.2f) else Color.Transparent,
                        border = if (selected) BorderStroke(1.dp, BrandCyan.copy(alpha = 0.5f)) else null,
                        shape  = RoundedCornerShape(16.dp)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("$days days", style = MaterialTheme.typography.titleMedium, color = if (selected) BrandCyan else Color.White)
                            if (selected) Icon(Icons.Default.Check, null, tint = BrandCyan, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            containerColor   = SurfaceRaised,
            shape = RoundedCornerShape(28.dp),
            title = { Text("Clear Watch History?", style = MaterialTheme.typography.titleLarge, color = Color.White) },
            text  = { Text("All recently played videos will be removed. This cannot be undone.", style = MaterialTheme.typography.bodyMedium, color = TextSecondary) },
            confirmButton = { TextButton(onClick = { onClearHistory(); showClearDialog = false }) { Text("Clear", color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { showClearDialog = false }) { Text("Cancel", color = BrandCyan) } }
        )
    }
}

// ── Developer card ────────────────────────────────────────────────────────────

@Composable
fun DeveloperPremiumCard(context: Context, onViewGithub: () -> Unit) {
    var qrBitmap by remember { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }
    LaunchedEffect(DEVELOPER_UPI_ID, DEVELOPER_NAME) {
        withContext(kotlinx.coroutines.Dispatchers.IO) {
            try { qrBitmap = generateQrBitmap(buildUpiPayString(DEVELOPER_UPI_ID, DEVELOPER_NAME)).asImageBitmap() } catch (_: Exception) {}
        }
    }
    PremiumCardSurface(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp), cornerRadius = 28.dp) {
        Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(68.dp).clip(CircleShape).border(2.dp, Brush.linearGradient(listOf(BrandCyan, BrandHighlight)), CircleShape)) {
                    AsyncImage(model = ImageRequest.Builder(context).data(DEVELOPER_IMAGE_URL).crossfade(true).build(), contentDescription = "Developer Profile", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                }
                Spacer(modifier = Modifier.width(18.dp))
                Column {
                    Text(DEVELOPER_NAME, style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
                    Text("Help me keep the player ad-free", style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Surface(color = Color.White, shape = RoundedCornerShape(20.dp), modifier = Modifier.size(170.dp).border(1.dp, BrandCyan.copy(alpha = 0.3f), RoundedCornerShape(20.dp))) {
                Box(modifier = Modifier.padding(14.dp), contentAlignment = Alignment.Center) {
                    if (qrBitmap != null) {
                        androidx.compose.foundation.Image(bitmap = qrBitmap!!, contentDescription = "UPI QR code", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
                    } else {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = BrandCyan, strokeWidth = 2.dp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text("Scan to Support via UPI", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            Spacer(modifier = Modifier.height(28.dp))
            PremiumButton(text = "View GitHub Profile", onClick = onViewGithub, modifier = Modifier.fillMaxWidth())
        }
    }
}

// ── Settings row components ───────────────────────────────────────────────────

@Composable
private fun SettingsSectionHeader(title: String) {
    val accent = LocalAppUiSettings.current.accent
    Row(modifier = Modifier.padding(start = 20.dp, top = 28.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.width(2.5.dp).height(14.dp).background(accent, RoundedCornerShape(1.dp)))
        Spacer(modifier = Modifier.width(10.dp))
        Text(text = title.uppercase(), style = MaterialTheme.typography.sectionOverline, color = accent)
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp), color = OutlineFaint)
}

/**
 * Reusable icon container for settings rows — consistent 48×48dp box with
 * a subtle gradient background so icons breathe at any size inside.
 * Uses the live accent from [LocalAppUiSettings] so it updates with theme/swatch changes.
 */
@Composable
private fun SettingsIconBox(
    brush: Brush? = null,   // pass null to auto-derive from current accent
    content: @Composable BoxScope.() -> Unit
) {
    val accent = LocalAppUiSettings.current.accent
    val resolvedBrush = brush ?: Brush.linearGradient(
        listOf(accent.copy(alpha = 0.12f), BrandBlueDeep.copy(alpha = 0.10f))
    )
    Box(
        modifier = Modifier
            .size(48.dp)
            .background(resolvedBrush, RoundedCornerShape(14.dp))
            .border(1.dp, OutlineFaint, RoundedCornerShape(14.dp)),
        contentAlignment = Alignment.Center,
        content = content
    )
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
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val accent = LocalAppUiSettings.current.accent
        SettingsIconBox {
            Icon(icon, null, tint = accent, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = MaterialTheme.typography.titleMedium, color = Color.White)
            if (sublabel.isNotEmpty()) Text(text = sublabel, style = MaterialTheme.typography.bodySmall, color = TextTertiary)
        }
        // Custom toggle icon — sized up to 40dp so it's clearly tappable
        Icon(
            painter = painterResource(if (checked) R.drawable.ic_toggle_right_custom else R.drawable.ic_toggle_left_custom),
            contentDescription = if (checked) "On" else "Off",
            tint = if (checked) accent else TextTertiary,
            modifier = Modifier.size(40.dp)
        )
    }
}

// Overload accepting ImageVector so callers don't need rememberVectorPainter everywhere
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
    tint: Color = BrandCyan,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val accent = LocalAppUiSettings.current.accent
        SettingsIconBox {
            Icon(icon, null, tint = if (tint == BrandCyan) accent else tint, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = if (tint == MaterialTheme.colorScheme.error) tint else Color.White,
            modifier = Modifier.weight(1f)
        )
        if (value.isNotEmpty()) {
            Text(text = value, style = MaterialTheme.typography.bodyMedium, color = TextTertiary)
            Spacer(modifier = Modifier.width(8.dp))
        }
        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(20.dp))
    }
}

// Overload accepting ImageVector
@Composable
private fun SettingsClickRow(
    icon: ImageVector,
    label: String,
    value: String,
    tint: Color = BrandCyan,
    onClick: () -> Unit
) = SettingsClickRow(rememberVectorPainter(icon), label, value, tint, onClick)

@Composable
private fun SettingsInfoRow(icon: Painter, label: String, value: String) {
    val accent = LocalAppUiSettings.current.accent
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
        SettingsIconBox {
            Icon(icon, null, tint = accent, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(label, style = MaterialTheme.typography.titleMedium, color = Color.White, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
    }
}

@Composable
private fun BottomSheetHandle() {
    Box(modifier = Modifier.padding(vertical = 12.dp).size(40.dp, 4.dp).background(OutlineMedium, CircleShape))
}

private fun formatSpeed(speed: Float): String =
    if (speed == speed.toLong().toFloat()) "${speed.toInt()}.0×" else "${speed}×"

// ═════════════════════════════════════════════════════════════════════════════
// PLAYLISTS SCREEN
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
@Composable
fun PlaylistsScreen(allVideos: List<VideoFile>, onVideoClick: (VideoFile) -> Unit) {
    val context = LocalContext.current
    val playlists = remember { mutableStateListOf<Playlist>().also { it.addAll(loadPlaylists(context)) } }
    var openPlaylist     by remember { mutableStateOf<Playlist?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var newPlaylistName  by remember { mutableStateOf("") }
    var addToPlaylist    by remember { mutableStateOf<Playlist?>(null) }
    var deleteTarget     by remember { mutableStateOf<Playlist?>(null) }
    fun persist() { savePlaylists(context, playlists) }

    if (openPlaylist != null) {
        val pl = openPlaylist!!
        val videos = remember(pl.videoPaths, allVideos) {
            val pathSet = pl.videoPaths.toHashSet()
            allVideos.filter { it.path in pathSet }
        }
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                navigationIcon = { PremiumTopBarAction(icon = painterResource(R.drawable.ic_back_custom), onClick = { openPlaylist = null }) },
                title = { BrandedTopBarTitle(text = pl.name, showIcon = false) },
                actions = { IconButton(onClick = { addToPlaylist = pl }) { Icon(Icons.Default.Add, "Add videos", tint = MaterialTheme.colorScheme.onSurfaceVariant) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent, scrolledContainerColor = Color.Transparent)
            )
            if (videos.isEmpty()) { PlaylistEmptyState(message = "No videos in this playlist yet") { addToPlaylist = pl } }
            else {
                LazyColumn(contentPadding = PaddingValues(16.dp)) {
                    items(videos, key = { it.id }) { video ->
                        PlaylistVideoRow(video = video, onClick = { onVideoClick(video) }, onRemove = {
                            val updated = pl.copy(videoPaths = pl.videoPaths.filter { it != video.path })
                            val idx = playlists.indexOfFirst { it.id == pl.id }
                            if (idx >= 0) { playlists[idx] = updated; openPlaylist = updated; persist() }
                        })
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 4.dp), color = OutlineFaint)
                    }
                }
            }
        }
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { BrandedTopBarTitle(text = "Playlists", showIcon = true) },
                actions = { IconButton(onClick = { showCreateDialog = true }) { Icon(Icons.Default.Add, "New playlist", tint = MaterialTheme.colorScheme.onSurfaceVariant) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent, scrolledContainerColor = Color.Transparent)
            )
            if (playlists.isEmpty()) { PlaylistEmptyState(message = "No playlists yet") { showCreateDialog = true } }
            else {
                LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)) {
                    items(playlists, key = { it.id }) { pl ->
                        val videoCount = pl.videoPaths.size
                        val thumbVideo = remember(pl.videoPaths, allVideos) {
                            val pathSet = pl.videoPaths.toHashSet()
                            allVideos.firstOrNull { it.path in pathSet }
                        }
                        PlaylistCard(playlist = pl, videoCount = videoCount, thumbVideo = thumbVideo, onClick = { openPlaylist = pl }, onDelete = { deleteTarget = pl })
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false; newPlaylistName = "" },
            containerColor   = SurfaceRaised,
            shape = RoundedCornerShape(28.dp),
            title = { Text("New Playlist", style = MaterialTheme.typography.titleLarge, color = Color.White) },
            text  = { OutlinedTextField(value = newPlaylistName, onValueChange = { newPlaylistName = it }, placeholder = { Text("Playlist name", color = TextTertiary) }, singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandCyan, unfocusedBorderColor = OutlineMedium, focusedTextColor = Color.White, unfocusedTextColor = Color.White, cursorColor = BrandCyan), modifier = Modifier.fillMaxWidth()) },
            confirmButton = { TextButton(enabled = newPlaylistName.isNotBlank(), onClick = { val pl = Playlist(id = System.currentTimeMillis().toString(), name = newPlaylistName.trim(), videoPaths = emptyList()); playlists.add(0, pl); persist(); showCreateDialog = false; newPlaylistName = ""; openPlaylist = pl }) { Text("Create", color = BrandCyan) } },
            dismissButton = { TextButton(onClick = { showCreateDialog = false; newPlaylistName = "" }) { Text("Cancel", color = TextSecondary) } }
        )
    }

    if (deleteTarget != null) {
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            containerColor   = SurfaceRaised,
            shape = RoundedCornerShape(28.dp),
            title = { Text("Delete \"${deleteTarget!!.name}\"?", style = MaterialTheme.typography.titleLarge, color = Color.White) },
            text  = { Text("The playlist will be deleted. Your video files won't be affected.", style = MaterialTheme.typography.bodyMedium, color = TextSecondary) },
            confirmButton = { TextButton(onClick = { playlists.removeAll { it.id == deleteTarget!!.id }; persist(); deleteTarget = null }) { Text("Delete", color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text("Cancel", color = BrandCyan) } }
        )
    }

    if (addToPlaylist != null) {
        val pl = addToPlaylist!!
        ModalBottomSheet(onDismissRequest = { addToPlaylist = null }, containerColor = SurfaceNavy, shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp), dragHandle = { BottomSheetHandle() }) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 48.dp)) {
                Text("Add Videos to \"${pl.name}\"", style = MaterialTheme.typography.headlineMedium, color = Color.White)
                Spacer(modifier = Modifier.height(4.dp))
                Text("${pl.videoPaths.size} videos already in playlist", style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                Spacer(modifier = Modifier.height(16.dp))
                val available = remember(pl.videoPaths, allVideos) { val pathSet = pl.videoPaths.toHashSet(); allVideos.filter { it.path !in pathSet } }
                if (available.isEmpty()) {
                    Text("All your videos are already in this playlist.", style = MaterialTheme.typography.bodyMedium, color = TextSecondary, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp))
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 440.dp)) {
                        items(available, key = { it.id }) { video ->
                            Row(modifier = Modifier.fillMaxWidth().clickable { val updated = pl.copy(videoPaths = pl.videoPaths + video.path); val idx = playlists.indexOfFirst { it.id == pl.id }; if (idx >= 0) { playlists[idx] = updated; addToPlaylist = updated; if (openPlaylist?.id == pl.id) openPlaylist = updated; persist() } }.padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                                Box(modifier = Modifier.size(72.dp, 44.dp).clip(RoundedCornerShape(10.dp)).background(SurfaceRaisedAlt).border(1.dp, OutlineFaint, RoundedCornerShape(10.dp))) {
                                    AsyncImage(model = remember(video.uri) { ImageRequest.Builder(context).data(video.uri).videoFrameMillis(1000).crossfade(true).build() }, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                }
                                Column(modifier = Modifier.weight(1f)) { Text(text = video.name, style = MaterialTheme.typography.bodyMedium, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis); Text(text = formatDuration(video.duration), style = MaterialTheme.typography.labelSmall, color = TextTertiary) }
                                Icon(Icons.Default.Add, "Add", tint = BrandCyan, modifier = Modifier.size(20.dp))
                            }
                            HorizontalDivider(color = OutlineFaint)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaylistCard(playlist: Playlist, videoCount: Int, thumbVideo: VideoFile?, onClick: () -> Unit, onDelete: () -> Unit) {
    val context = LocalContext.current
    PremiumCardSurface(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).clickable(onClick = onClick), cornerRadius = 24.dp) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(90.dp, 56.dp).clip(RoundedCornerShape(14.dp)).background(SurfaceRaisedAlt).border(1.dp, OutlineFaint, RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                if (thumbVideo != null) { AsyncImage(model = remember(thumbVideo.uri) { ImageRequest.Builder(context).data(thumbVideo.uri).videoFrameMillis(1000).crossfade(true).build() }, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop) }
                else { Icon(painterResource(R.drawable.ic_list_custom), null, tint = TextTertiary, modifier = Modifier.size(28.dp)) }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = playlist.name, style = MaterialTheme.typography.titleMedium, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(text = "$videoCount videos", style = MaterialTheme.typography.bodySmall, color = TextTertiary)
            }
            IconButton(onClick = onDelete) { Icon(painterResource(R.drawable.ic_trash_custom), "Delete", tint = TextTertiary, modifier = Modifier.size(20.dp)) }
        }
    }
}

@Composable
private fun PlaylistVideoRow(video: VideoFile, onClick: () -> Unit, onRemove: () -> Unit) {
    val context = LocalContext.current
    Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 10.dp, horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(118.dp, 72.dp).clip(RoundedCornerShape(14.dp)).background(SurfaceRaisedAlt).border(1.dp, OutlineFaint, RoundedCornerShape(14.dp))) {
            AsyncImage(model = remember(video.uri) { ImageRequest.Builder(context).data(video.uri).videoFrameMillis(1000).crossfade(true).build() }, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            Surface(modifier = Modifier.align(Alignment.BottomEnd).padding(5.dp), color = Color.Black.copy(alpha = 0.65f), shape = RoundedCornerShape(6.dp)) {
                Text(formatDuration(video.duration), color = Color.White, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp))
            }
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = video.name, style = MaterialTheme.typography.titleMedium, color = Color.White, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(formatSize(video.size), style = MaterialTheme.typography.bodySmall, color = TextTertiary)
        }
        IconButton(onClick = onRemove) { Icon(painterResource(R.drawable.ic_close_custom), "Remove", tint = TextTertiary, modifier = Modifier.size(20.dp)) }
    }
}

@Composable
private fun PlaylistEmptyState(message: String, onAction: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(40.dp)) {
            Box(modifier = Modifier.size(100.dp).background(Brush.radialGradient(listOf(BrandCyan.copy(alpha = 0.1f), Color.Transparent))), contentAlignment = Alignment.Center) {
                Icon(painterResource(R.drawable.ic_list_custom), null, tint = BrandCyan.copy(alpha = 0.6f), modifier = Modifier.size(48.dp))
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(text = message, style = MaterialTheme.typography.headlineSmall, color = Color.White, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(32.dp))
            PremiumButton(text = "Get Started", onClick = onAction, modifier = Modifier.fillMaxWidth(0.7f))
        }
    }
}