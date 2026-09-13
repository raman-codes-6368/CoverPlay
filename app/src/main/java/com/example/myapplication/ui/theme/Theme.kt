package com.example.myapplication.ui.theme

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

// ─────────────────────────────────────────────────────────────────────────────
//  THEME — CoverPlay Material 3 Expressive (v4, "Iris Bloom")
//
//  Adds a full Light / Dark / AMOLED mode system on top of the existing
//  dynamic-accent + theme-preset machinery, plus an expressive Shapes()
//  object (very round corners everywhere) so stock M3 components pick up
//  the circular/curvy icon-matched language automatically.
// ─────────────────────────────────────────────────────────────────────────────

private const val PREFS_NAME              = "coverplay_settings_v3"
private const val KEY_ACCENT_COLOR        = "accent_color"
private const val KEY_FONT_SCALE          = "font_scale"
private const val KEY_SHOW_DURATION_BADGE = "show_duration_badge"
private const val KEY_COMPACT_LIST        = "compact_list"
private const val KEY_THEME_MODE          = "theme_mode"
private const val KEY_USE_SYSTEM_FONT     = "use_system_font"
private const val KEY_HIDE_THUMBNAILS     = "hide_thumbnails"
private const val KEY_SHOW_SPLASH         = "show_splash"

/** Light / Dark / AMOLED — user-selectable from Settings, persisted like everything else. */
enum class ThemeMode(val label: String) {
    LIGHT("Light"),
    DARK("Dark"),
    AMOLED("AMOLED Black");

    fun palette(): PaletteSet = when (this) {
        LIGHT  -> LightPalette
        DARK   -> DarkPalette
        AMOLED -> AmoledPalette
    }
}

data class AppUiSettings(
    val accent: Color              = BrandPrimary,
    val showDurationBadge: Boolean = true,
    val compactList: Boolean       = false,
    val useSystemFont: Boolean     = false,
    val hideThumbnails: Boolean    = false,
    val showSplash: Boolean        = true,
    val themeMode: ThemeMode       = ThemeMode.DARK,
    val palette: PaletteSet        = DarkPalette
)

val LocalAppUiSettings = compositionLocalOf { AppUiSettings() }

// ── Expressive shape system — circular & curvy across every M3 slot ─────────
val CoverPlayShapes = Shapes(
    extraSmall = RoundedCornerShape(12.dp),
    small      = RoundedCornerShape(16.dp),
    medium     = RoundedCornerShape(24.dp),
    large      = RoundedCornerShape(32.dp),
    extraLarge = RoundedCornerShape(48.dp),
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val prefs   = remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }

    var accentColorInt    by remember { mutableStateOf(prefs.getInt(KEY_ACCENT_COLOR, BrandPrimary.toArgb())) }
    var fontScale         by remember { mutableStateOf(prefs.getFloat(KEY_FONT_SCALE, 1.0f)) }
    var showDurationBadge by remember { mutableStateOf(prefs.getBoolean(KEY_SHOW_DURATION_BADGE, true)) }
    var compactList       by remember { mutableStateOf(prefs.getBoolean(KEY_COMPACT_LIST, false)) }
    var themeModeName     by remember { mutableStateOf(prefs.getString(KEY_THEME_MODE, ThemeMode.DARK.name) ?: ThemeMode.DARK.name) }
    var useSystemFont     by remember { mutableStateOf(prefs.getBoolean(KEY_USE_SYSTEM_FONT, false)) }
    var hideThumbnails    by remember { mutableStateOf(prefs.getBoolean(KEY_HIDE_THUMBNAILS, false)) }
    var showSplash        by remember { mutableStateOf(prefs.getBoolean(KEY_SHOW_SPLASH, true)) }

    DisposableEffect(prefs) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { p, key ->
            when (key) {
                KEY_ACCENT_COLOR        -> accentColorInt     = p.getInt(key, BrandPrimary.toArgb())
                KEY_FONT_SCALE          -> fontScale          = p.getFloat(key, 1.0f)
                KEY_SHOW_DURATION_BADGE -> showDurationBadge  = p.getBoolean(key, true)
                KEY_COMPACT_LIST        -> compactList        = p.getBoolean(key, false)
                KEY_THEME_MODE          -> themeModeName      = p.getString(key, ThemeMode.DARK.name) ?: ThemeMode.DARK.name
                KEY_USE_SYSTEM_FONT     -> useSystemFont      = p.getBoolean(key, false)
                KEY_HIDE_THUMBNAILS     -> hideThumbnails     = p.getBoolean(key, false)
                KEY_SHOW_SPLASH         -> showSplash         = p.getBoolean(key, true)
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    val dynamicAccent = Color(accentColorInt)
    val themeMode = ThemeMode.entries.firstOrNull { it.name == themeModeName } ?: ThemeMode.DARK
    val palette = themeMode.palette()

    val colorScheme = if (palette.isDark) {
        darkColorScheme(
            primary                 = dynamicAccent,
            onPrimary               = Color.White,
            primaryContainer        = dynamicAccent.copy(alpha = 0.28f),
            onPrimaryContainer      = BrandIceBlue,

            secondary               = BrandSecondary,
            onSecondary             = Color.White,
            secondaryContainer      = BrandSecondary.copy(alpha = 0.22f),
            onSecondaryContainer    = BrandIceBlue,

            tertiary                = BrandTertiary,
            onTertiary              = Color.Black,
            tertiaryContainer       = BrandTertiary.copy(alpha = 0.22f),
            onTertiaryContainer     = BrandGoldLight,

            background              = palette.bg,
            onBackground            = palette.textPrimary,

            surface                 = palette.surfaceNavy,
            onSurface               = palette.textPrimary,
            surfaceVariant          = palette.surfaceRaised,
            onSurfaceVariant        = palette.textSecondary,
            surfaceTint             = dynamicAccent,

            surfaceContainerLowest  = palette.containerLowest,
            surfaceContainerLow     = palette.containerLow,
            surfaceContainer        = palette.container,
            surfaceContainerHigh    = palette.containerHigh,
            surfaceContainerHighest = palette.containerHighest,

            inverseSurface          = LightPalette.surfaceNavy,
            inverseOnSurface        = LightPalette.textPrimary,
            inversePrimary          = IconSky,

            outline                 = palette.outlineMedium,
            outlineVariant          = palette.outlineFaint,

            error                   = BrandRed,
            onError                 = Color.White,
            errorContainer          = BrandRed.copy(alpha = 0.25f),
            onErrorContainer        = Color(0xFFFFD5DE),

            scrim                   = Color.Black.copy(alpha = 0.65f),
        )
    } else {
        lightColorScheme(
            primary                 = dynamicAccent,
            onPrimary               = Color.White,
            primaryContainer        = dynamicAccent.copy(alpha = 0.14f),
            onPrimaryContainer      = IconRoyalBlue,

            secondary               = BrandSecondary,
            onSecondary             = Color.White,
            secondaryContainer      = BrandSecondary.copy(alpha = 0.14f),
            onSecondaryContainer    = IconRoyalBlue,

            tertiary                = BrandTertiary,
            onTertiary              = Color.White,
            tertiaryContainer       = BrandTertiary.copy(alpha = 0.14f),
            onTertiaryContainer     = Color(0xFF00464C),

            background              = palette.bg,
            onBackground            = palette.textPrimary,

            surface                 = palette.surfaceNavy,
            onSurface               = palette.textPrimary,
            surfaceVariant          = palette.surfaceRaisedAlt,
            onSurfaceVariant        = palette.textSecondary,
            surfaceTint             = dynamicAccent,

            surfaceContainerLowest  = palette.containerLowest,
            surfaceContainerLow     = palette.containerLow,
            surfaceContainer        = palette.container,
            surfaceContainerHigh    = palette.containerHigh,
            surfaceContainerHighest = palette.containerHighest,

            inverseSurface          = DarkPalette.surfaceNavy,
            inverseOnSurface        = DarkPalette.textPrimary,
            inversePrimary          = IconIndigo,

            outline                 = palette.outlineMedium,
            outlineVariant          = palette.outlineFaint,

            error                   = BrandRed,
            onError                 = Color.White,
            errorContainer          = Color(0xFFFFDADE),
            onErrorContainer        = Color(0xFF5C0014),

            scrim                   = Color.Black.copy(alpha = 0.45f),
        )
    }

    val baseTypography = if (useSystemFont) Typography() else CinematicTypography
    val scaledTypography = remember(fontScale, baseTypography) {
        baseTypography.copy(
            displayLarge   = baseTypography.displayLarge.copy(fontSize   = baseTypography.displayLarge.fontSize   * fontScale),
            displayMedium  = baseTypography.displayMedium.copy(fontSize  = baseTypography.displayMedium.fontSize  * fontScale),
            displaySmall   = baseTypography.displaySmall.copy(fontSize   = baseTypography.displaySmall.fontSize   * fontScale),
            headlineLarge  = baseTypography.headlineLarge.copy(fontSize  = baseTypography.headlineLarge.fontSize  * fontScale),
            headlineMedium = baseTypography.headlineMedium.copy(fontSize = baseTypography.headlineMedium.fontSize * fontScale),
            headlineSmall  = baseTypography.headlineSmall.copy(fontSize  = baseTypography.headlineSmall.fontSize  * fontScale),
            titleLarge     = baseTypography.titleLarge.copy(fontSize     = baseTypography.titleLarge.fontSize     * fontScale),
            titleMedium    = baseTypography.titleMedium.copy(fontSize    = baseTypography.titleMedium.fontSize    * fontScale),
            titleSmall     = baseTypography.titleSmall.copy(fontSize     = baseTypography.titleSmall.fontSize     * fontScale),
            bodyLarge      = baseTypography.bodyLarge.copy(fontSize      = baseTypography.bodyLarge.fontSize      * fontScale),
            bodyMedium     = baseTypography.bodyMedium.copy(fontSize     = baseTypography.bodyMedium.fontSize     * fontScale),
            bodySmall      = baseTypography.bodySmall.copy(fontSize      = baseTypography.bodySmall.fontSize      * fontScale),
            labelLarge     = baseTypography.labelLarge.copy(fontSize     = baseTypography.labelLarge.fontSize     * fontScale),
            labelMedium    = baseTypography.labelMedium.copy(fontSize    = baseTypography.labelMedium.fontSize    * fontScale),
            labelSmall     = baseTypography.labelSmall.copy(fontSize     = baseTypography.labelSmall.fontSize     * fontScale),
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        // Use a persistent SideEffect to sync the status bar with the current palette
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = palette.bg.toArgb()

            val insetsController = WindowCompat.getInsetsController(window, view)
            // Flawless adaptation: status bar contents are dark (black) in Light mode,
            // and light (white) in Dark/AMOLED modes.
            insetsController.isAppearanceLightStatusBars = !palette.isDark
            insetsController.isAppearanceLightNavigationBars = !palette.isDark
        }
    }

    val uiSettings = AppUiSettings(
    accent            = dynamicAccent,
    showDurationBadge = showDurationBadge,
    compactList       = compactList,
    useSystemFont     = useSystemFont,
    hideThumbnails    = hideThumbnails,
    showSplash        = showSplash,
    themeMode         = themeMode,
    palette           = palette
)

    CompositionLocalProvider(LocalAppUiSettings provides uiSettings) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography  = scaledTypography,
            shapes      = CoverPlayShapes,
            content     = content
        )
    }
}

/** Persist a new theme mode (call from Settings). */
fun setThemeMode(context: Context, mode: ThemeMode) {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit().putString(KEY_THEME_MODE, mode.name).apply()
}