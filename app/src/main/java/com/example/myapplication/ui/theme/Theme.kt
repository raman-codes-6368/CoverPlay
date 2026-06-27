package com.example.myapplication.ui.theme

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.myapplication.ThemePreset  // defined in SettingsAndPlaylists.kt
import com.example.myapplication.CardStyle    // defined in SettingsAndPlaylists.kt

// ── SharedPreference keys (must match SettingsAndPlaylists.kt) ────────────────
private const val PREFS_NAME             = "coverplay_settings"
private const val KEY_ACCENT_COLOR       = "accent_color"
private const val KEY_FONT_SCALE         = "font_scale"
private const val KEY_THEME_PRESET       = "theme_preset"
private const val KEY_CARD_STYLE         = "card_style"
private const val KEY_SHOW_DURATION_BADGE = "show_duration_badge"
private const val KEY_COMPACT_LIST        = "compact_list"

// ── Composition-local holder for UI settings consumed across the app ──────────
/**
 * Snapshot of appearance settings that change at runtime.
 * Provided by [MyApplicationTheme] so any composable can read them without
 * drilling parameters or accessing SharedPreferences directly.
 */
data class AppUiSettings(
    val accent: Color          = BrandCyan,
    val cardStyle: CardStyle   = CardStyle.GLASS,
    val showDurationBadge: Boolean = true,
    val compactList: Boolean   = false
)

val LocalAppUiSettings = compositionLocalOf { AppUiSettings() }

// ── Theme ─────────────────────────────────────────────────────────────────────

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val prefs   = remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }

    // Read all reactive settings from prefs, updating whenever any key changes.
    var accentColorInt      by remember { mutableStateOf(prefs.getInt(KEY_ACCENT_COLOR, BrandCyan.toArgb())) }
    var fontScale           by remember { mutableStateOf(prefs.getFloat(KEY_FONT_SCALE, 1.0f)) }
    var themePresetName     by remember { mutableStateOf(prefs.getString(KEY_THEME_PRESET, ThemePreset.CINEMATIC.name) ?: ThemePreset.CINEMATIC.name) }
    var cardStyleName       by remember { mutableStateOf(prefs.getString(KEY_CARD_STYLE, CardStyle.GLASS.name) ?: CardStyle.GLASS.name) }
    var showDurationBadge   by remember { mutableStateOf(prefs.getBoolean(KEY_SHOW_DURATION_BADGE, true)) }
    var compactList         by remember { mutableStateOf(prefs.getBoolean(KEY_COMPACT_LIST, false)) }

    DisposableEffect(prefs) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { p, key ->
            when (key) {
                KEY_ACCENT_COLOR        -> accentColorInt    = p.getInt(key, BrandCyan.toArgb())
                KEY_FONT_SCALE          -> fontScale          = p.getFloat(key, 1.0f)
                KEY_THEME_PRESET        -> themePresetName    = p.getString(key, ThemePreset.CINEMATIC.name) ?: ThemePreset.CINEMATIC.name
                KEY_CARD_STYLE          -> cardStyleName       = p.getString(key, CardStyle.GLASS.name) ?: CardStyle.GLASS.name
                KEY_SHOW_DURATION_BADGE -> showDurationBadge   = p.getBoolean(key, true)
                KEY_COMPACT_LIST        -> compactList          = p.getBoolean(key, false)
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    // Resolve current theme preset (drives background + surface colours)
    val preset = ThemePreset.fromName(themePresetName)

    // The accent from prefs takes priority so custom swatch picks inside a
    // preset are preserved; selecting a new preset writes both keys together.
    val dynamicAccent = Color(accentColorInt)

    // Build the Material colour scheme from the active preset's surfaces + the
    // dynamic accent so every Material3 component automatically inherits both.
    val colorScheme = darkColorScheme(
        primary            = dynamicAccent,
        onPrimary          = Color(0xFF06221E),
        primaryContainer   = dynamicAccent.copy(alpha = 0.15f),
        onPrimaryContainer = dynamicAccent,

        secondary          = BrandBlue,
        onSecondary        = TextPrimary,

        // Use preset-specific background & surface so they change with the theme.
        background         = preset.bg,
        onBackground       = TextPrimary,

        surface            = preset.surface,
        onSurface          = TextPrimary,
        surfaceVariant     = preset.surface.copy(alpha = 0.7f),
        onSurfaceVariant   = TextSecondary,

        outline            = OutlineMedium,
        outlineVariant     = OutlineFaint,

        error              = Color(0xFFFF6B7A),
        onError            = Color(0xFF1A0408),
    )

    // Scale typography uniformly across all type roles
    val scaledTypography = remember(fontScale) {
        CinematicTypography.copy(
            displayLarge  = CinematicTypography.displayLarge.copy(fontSize  = CinematicTypography.displayLarge.fontSize  * fontScale),
            displayMedium = CinematicTypography.displayMedium.copy(fontSize = CinematicTypography.displayMedium.fontSize * fontScale),
            displaySmall  = CinematicTypography.displaySmall.copy(fontSize  = CinematicTypography.displaySmall.fontSize  * fontScale),
            headlineLarge = CinematicTypography.headlineLarge.copy(fontSize = CinematicTypography.headlineLarge.fontSize * fontScale),
            headlineMedium= CinematicTypography.headlineMedium.copy(fontSize= CinematicTypography.headlineMedium.fontSize* fontScale),
            headlineSmall = CinematicTypography.headlineSmall.copy(fontSize = CinematicTypography.headlineSmall.fontSize * fontScale),
            titleLarge    = CinematicTypography.titleLarge.copy(fontSize    = CinematicTypography.titleLarge.fontSize    * fontScale),
            titleMedium   = CinematicTypography.titleMedium.copy(fontSize   = CinematicTypography.titleMedium.fontSize   * fontScale),
            titleSmall    = CinematicTypography.titleSmall.copy(fontSize    = CinematicTypography.titleSmall.fontSize    * fontScale),
            bodyLarge     = CinematicTypography.bodyLarge.copy(fontSize     = CinematicTypography.bodyLarge.fontSize     * fontScale),
            bodyMedium    = CinematicTypography.bodyMedium.copy(fontSize    = CinematicTypography.bodyMedium.fontSize    * fontScale),
            bodySmall     = CinematicTypography.bodySmall.copy(fontSize     = CinematicTypography.bodySmall.fontSize     * fontScale),
            labelLarge    = CinematicTypography.labelLarge.copy(fontSize    = CinematicTypography.labelLarge.fontSize    * fontScale),
            labelMedium   = CinematicTypography.labelMedium.copy(fontSize   = CinematicTypography.labelMedium.fontSize   * fontScale),
            labelSmall    = CinematicTypography.labelSmall.copy(fontSize    = CinematicTypography.labelSmall.fontSize    * fontScale),
        )
    }

    // Edge-to-edge: transparent status bar, nav bar follows the preset background.
    // We always force isAppearanceLightStatusBars = false (white icons) so the
    // status bar text/icons remain visible on our dark surfaces even when the
    // device is set to light system theme.
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor     = Color.Transparent.toArgb()
            window.navigationBarColor = preset.bg.toArgb()
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars  = false  // always white status-bar icons
            insetsController.isAppearanceLightNavigationBars = false  // always white nav-bar icons
        }
    }

    // Assemble the UI-settings snapshot for the whole composition tree
    val uiSettings = AppUiSettings(
        accent            = dynamicAccent,
        cardStyle         = CardStyle.entries.firstOrNull { it.name == cardStyleName } ?: CardStyle.GLASS,
        showDurationBadge = showDurationBadge,
        compactList       = compactList
    )

    CompositionLocalProvider(LocalAppUiSettings provides uiSettings) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography  = scaledTypography,
            content     = content
        )
    }
}