package com.example.myapplication.ui.theme

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.graphics.drawable.ColorDrawable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

// ─────────────────────────────────────────────────────────────────────────────
//  THEME — CoverPlay Material 3 Expressive (v5)
//
//  Five complete themes. Each one owns its ENTIRE palette (backgrounds,
//  surfaces, containers, outlines, text, glass, gradients) in Dark / AMOLED /
//  Light, plus a name, a tagline and an animated backdrop scene — so switching
//  theme changes everything, and nothing from another theme (or the old Iris
//  blue) can bleed through.
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
    AMOLED("AMOLED")
}

/** Animated backdrop identity — each theme paints its own scene behind the whole app. */
enum class BackdropScene {
    BLOOM,    // drifting petal glows + dewdrop ripples
    NEBULA,   // nebula band + twinkling stardust + shooting stars
    VELVET,   // corner glows + swaying curtain folds + falling rose petals
    HORIZON,  // glowing sun + rotating rays + rising embers
    AURORA    // light ribbons + hanging shimmer + twinkling polar stars
}

/** 5 full themes: unique name, backdrop, highlights and a complete tinted palette per mode. */
enum class AppTheme(
    val id: String,
    val label: String,
    val tagline: String,
    val scene: BackdropScene,
    val primaryAccent: Color,
    val lightAccent: Color,
    val highlight: Color,
    val secondaryAccent: Color,
    val bgDark: Color,
    val bgLight: Color,
    /** Mid-tone of the theme's hue; all dark surfaces are mixed from bg → tone. */
    val tone: Color
) {
    IRIS_ROYAL(
        id = "iris_royal",
        label = "Midnight Iris",
        tagline = "Iris you'd press play · petals in an indigo night",
        scene = BackdropScene.BLOOM,
        primaryAccent = Color(0xFF4A63F5),
        lightAccent = Color(0xFF3A52E0),
        highlight = Color(0xFFAFC2FF),
        secondaryAccent = Color(0xFF5B9CFF),
        bgDark = Color(0xFF070A1F),
        bgLight = Color(0xFFF5F6FD),
        tone = Color(0xFF3A4BC8)
    ),
    NEON_VIOLET(
        id = "neon_violet",
        label = "Ultraviolet Dusk",
        tagline = "Stardust & chill · shooting stars over a violet nebula",
        scene = BackdropScene.NEBULA,
        primaryAccent = Color(0xFFA15CFF),
        lightAccent = Color(0xFF8A3FE8),
        highlight = Color(0xFFE2CCFF),
        secondaryAccent = Color(0xFFD16BFF),
        bgDark = Color(0xFF0F0819),
        bgLight = Color(0xFFFAF4FF),
        tone = Color(0xFF7A33D6)
    ),
    CYBER_MAGENTA(
        id = "cyber_magenta",
        label = "Velvet Rose",
        tagline = "Curtain's up · crimson light through velvet drapes",
        scene = BackdropScene.VELVET,
        primaryAccent = Color(0xFFE91E63),
        lightAccent = Color(0xFFD81B60),
        highlight = Color(0xFFFFB2D1),
        secondaryAccent = Color(0xFFFF5252),
        bgDark = Color(0xFF16080F),
        bgLight = Color(0xFFFFF4F7),
        tone = Color(0xFFB3224F)
    ),
    SOLAR_EMBER(
        id = "solar_ember",
        label = "Solar Ember",
        tagline = "Too hot to handle · a golden horizon and rising embers",
        scene = BackdropScene.HORIZON,
        primaryAccent = Color(0xFFFF7A00),
        lightAccent = Color(0xFFE86A00),
        highlight = Color(0xFFFFD180),
        secondaryAccent = Color(0xFFFFAB00),
        bgDark = Color(0xFF120A05),
        bgLight = Color(0xFFFFF7EE),
        tone = Color(0xFFB4560F)
    ),
    HYPER_CYAN(
        id = "hyper_cyan",
        label = "Arctic Aurora",
        tagline = "Cool story, bro · northern lights over a teal sea",
        scene = BackdropScene.AURORA,
        primaryAccent = Color(0xFF00B8C4),
        lightAccent = Color(0xFF00929E),
        highlight = Color(0xFFA7FFEB),
        secondaryAccent = Color(0xFF1DE9B6),
        bgDark = Color(0xFF04100F),
        bgLight = Color(0xFFF0FAF8),
        tone = Color(0xFF0E9C94)
    );

    fun accentFor(mode: ThemeMode): Color = if (mode == ThemeMode.LIGHT) lightAccent else primaryAccent

    /** Light mode needs a deeper highlight so accent-coloured text/icons keep contrast on white. */
    fun highlightFor(mode: ThemeMode): Color =
        if (mode == ThemeMode.LIGHT) lerp(lightAccent, highlight, 0.30f) else highlight

    fun secondaryFor(mode: ThemeMode): Color =
        if (mode == ThemeMode.LIGHT) lerp(secondaryAccent, Color.Black, 0.12f) else secondaryAccent

    private val palettes: Map<ThemeMode, PaletteSet> by lazy {
        ThemeMode.entries.associateWith { buildPalette(it, this) }
    }

    fun palette(mode: ThemeMode): PaletteSet = palettes.getValue(mode)

    /** Theme swatch / logo gradient: one hue, light → core → deep (never a two-colour mix). */
    fun swatchBrush(): Brush = Brush.linearGradient(
        listOf(highlight, primaryAccent, lerp(primaryAccent, Color.Black, 0.35f))
    )

    fun gradient(): Brush = Brush.linearGradient(listOf(highlight, primaryAccent, secondaryAccent))

    fun radialGradient(): Brush = Brush.radialGradient(
        listOf(highlight, primaryAccent, primaryAccent.copy(alpha = 0.7f))
    )
}

/**
 * Builds a complete, hue-consistent palette. Every surface/outline/text colour is mixed from the
 * theme's own bg / tone / highlight, so there is no neutral-blue leftover anywhere.
 */
private fun buildPalette(mode: ThemeMode, t: AppTheme): PaletteSet {
    val hi = t.highlight
    val tone = t.tone
    return when (mode) {
        ThemeMode.DARK -> {
            val bg = t.bgDark
            PaletteSet(
                bg = bg,
                surfaceNavy = lerp(bg, tone, 0.09f),
                surfaceRaised = lerp(bg, tone, 0.14f),
                surfaceRaisedAlt = lerp(bg, tone, 0.21f),
                containerLowest = lerp(bg, Color.Black, 0.40f),
                containerLow = lerp(bg, tone, 0.07f),
                container = lerp(bg, tone, 0.12f),
                containerHigh = lerp(bg, tone, 0.19f),
                containerHighest = lerp(bg, tone, 0.26f),
                outlineFaint = lerp(bg, tone, 0.20f),
                outlineMedium = lerp(bg, tone, 0.36f),
                outlineBright = lerp(bg, hi, 0.40f),
                textPrimary = lerp(Color.White, hi, 0.07f),
                textSecondary = lerp(Color(0xFFCFCFCF), hi, 0.22f),
                textTertiary = lerp(Color(0xFF8E8E8E), hi, 0.25f),
                isDark = true,
                glassBase = lerp(bg, tone, 0.13f),
                videoGlass = lerp(Color.Black, tone, 0.10f)
            )
        }
        ThemeMode.AMOLED -> {
            val bg = Color.Black
            PaletteSet(
                bg = bg,
                surfaceNavy = bg,
                surfaceRaised = lerp(bg, tone, 0.055f),
                surfaceRaisedAlt = lerp(bg, tone, 0.09f),
                containerLowest = bg,
                containerLow = lerp(bg, tone, 0.035f),
                container = lerp(bg, tone, 0.06f),
                containerHigh = lerp(bg, tone, 0.10f),
                containerHighest = lerp(bg, tone, 0.15f),
                outlineFaint = lerp(bg, tone, 0.12f),
                outlineMedium = lerp(bg, tone, 0.22f),
                outlineBright = lerp(bg, hi, 0.35f),
                textPrimary = Color.White,
                textSecondary = lerp(Color(0xFFB8B8B8), hi, 0.16f),
                textTertiary = lerp(Color(0xFF7C7C7C), hi, 0.20f),
                isDark = true,
                glassBase = lerp(bg, tone, 0.05f),
                videoGlass = lerp(Color.Black, tone, 0.06f)
            )
        }
        ThemeMode.LIGHT -> {
            val bg = t.bgLight
            PaletteSet(
                bg = bg,
                surfaceNavy = Color.White,
                surfaceRaised = Color.White,
                surfaceRaisedAlt = lerp(bg, hi, 0.22f),
                containerLowest = Color.White,
                containerLow = lerp(bg, hi, 0.14f),
                container = lerp(bg, hi, 0.22f),
                containerHigh = lerp(bg, hi, 0.32f),
                containerHighest = lerp(bg, hi, 0.42f),
                outlineFaint = lerp(bg, hi, 0.45f),
                outlineMedium = lerp(bg, t.lightAccent, 0.22f),
                outlineBright = lerp(bg, t.lightAccent, 0.42f),
                textPrimary = lerp(Color(0xFF15151A), tone, 0.10f),
                textSecondary = lerp(Color(0xFF52525A), tone, 0.25f),
                textTertiary = lerp(Color(0xFF80808A), tone, 0.22f),
                isDark = false,
                glassBase = lerp(Color.White, hi, 0.12f),
                videoGlass = lerp(Color.Black, tone, 0.10f)
            )
        }
    }
}

const val KEY_APP_THEME = "key_app_theme"

data class AppUiSettings(
    val appTheme: AppTheme          = AppTheme.IRIS_ROYAL,
    val accent: Color              = AppTheme.IRIS_ROYAL.primaryAccent,
    val highlight: Color           = AppTheme.IRIS_ROYAL.highlight,
    val secondaryAccent: Color     = AppTheme.IRIS_ROYAL.secondaryAccent,
    val showDurationBadge: Boolean = true,
    val compactList: Boolean       = false,
    val useSystemFont: Boolean     = false,
    val hideThumbnails: Boolean    = false,
    val showSplash: Boolean        = true,
    val themeMode: ThemeMode       = ThemeMode.DARK,
    val palette: PaletteSet        = AppTheme.IRIS_ROYAL.palette(ThemeMode.DARK)
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

    var appThemeName      by remember { mutableStateOf(prefs.getString(KEY_APP_THEME, AppTheme.IRIS_ROYAL.name) ?: AppTheme.IRIS_ROYAL.name) }
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
                KEY_APP_THEME           -> appThemeName       = p.getString(key, AppTheme.IRIS_ROYAL.name) ?: AppTheme.IRIS_ROYAL.name
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

    val currentAppTheme = AppTheme.entries.firstOrNull { it.name == appThemeName } ?: AppTheme.IRIS_ROYAL
    val themeMode = ThemeMode.entries.firstOrNull { it.name == themeModeName } ?: ThemeMode.DARK
    val palette = currentAppTheme.palette(themeMode)
    val accent = currentAppTheme.accentFor(themeMode)
    val highlight = currentAppTheme.highlightFor(themeMode)
    val secondaryAccent = currentAppTheme.secondaryFor(themeMode)
    val soft = lerp(accent, highlight, 0.35f)

    // Push the active theme into the live colour roles BEFORE any content composes, so every
    // legacy reference (BrandAccent, IconSky, BrandGradient, BgBase …) resolves to this theme.
    LiveTheme.sync(currentAppTheme, themeMode)

    val colorScheme = if (palette.isDark) {
        val onContainer = lerp(Color.White, highlight, 0.20f)
        darkColorScheme(
            primary                 = accent,
            onPrimary               = Color.White,
            primaryContainer        = lerp(palette.container, accent, 0.38f),
            onPrimaryContainer      = onContainer,

            secondary               = soft,
            onSecondary             = Color.White,
            secondaryContainer      = lerp(palette.container, soft, 0.28f),
            onSecondaryContainer    = onContainer,

            tertiary                = secondaryAccent,
            onTertiary              = Color.Black,
            tertiaryContainer       = lerp(palette.container, secondaryAccent, 0.28f),
            onTertiaryContainer     = onContainer,

            background              = palette.bg,
            onBackground            = palette.textPrimary,

            surface                 = palette.surfaceNavy,
            onSurface               = palette.textPrimary,
            surfaceVariant          = palette.surfaceRaised,
            onSurfaceVariant        = palette.textSecondary,
            surfaceTint             = accent,

            surfaceContainerLowest  = palette.containerLowest,
            surfaceContainerLow     = palette.containerLow,
            surfaceContainer        = palette.container,
            surfaceContainerHigh    = palette.containerHigh,
            surfaceContainerHighest = palette.containerHighest,

            inverseSurface          = Color(0xFFF1F1F4),
            inverseOnSurface        = Color(0xFF16161A),
            inversePrimary          = soft,

            outline                 = palette.outlineMedium,
            outlineVariant          = palette.outlineFaint,

            error                   = BrandRed,
            onError                 = Color.White,
            errorContainer          = BrandRed.copy(alpha = 0.25f),
            onErrorContainer        = Color(0xFFFFD5DE),

            scrim                   = Color.Black.copy(alpha = 0.65f),
        )
    } else {
        val onContainer = lerp(accent, Color.Black, 0.55f)
        lightColorScheme(
            primary                 = accent,
            onPrimary               = Color.White,
            primaryContainer        = lerp(palette.bg, accent, 0.18f),
            onPrimaryContainer      = onContainer,

            secondary               = soft,
            onSecondary             = Color.White,
            secondaryContainer      = lerp(palette.bg, soft, 0.20f),
            onSecondaryContainer    = onContainer,

            tertiary                = secondaryAccent,
            onTertiary              = Color.White,
            tertiaryContainer       = lerp(palette.bg, secondaryAccent, 0.20f),
            onTertiaryContainer     = lerp(secondaryAccent, Color.Black, 0.60f),

            background              = palette.bg,
            onBackground            = palette.textPrimary,

            surface                 = palette.surfaceNavy,
            onSurface               = palette.textPrimary,
            surfaceVariant          = palette.surfaceRaisedAlt,
            onSurfaceVariant        = palette.textSecondary,
            surfaceTint             = accent,

            surfaceContainerLowest  = palette.containerLowest,
            surfaceContainerLow     = palette.containerLow,
            surfaceContainer        = palette.container,
            surfaceContainerHigh    = palette.containerHigh,
            surfaceContainerHighest = palette.containerHighest,

            inverseSurface          = Color(0xFF1A1A1E),
            inverseOnSurface        = Color(0xFFF1F1F4),
            inversePrimary          = soft,

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
        SideEffect {
            val window = view.context.findHostWindow() ?: return@SideEffect
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = palette.bg.toArgb()
            // Window background matches the theme so no off-colour flash shows during transitions.
            window.setBackgroundDrawable(ColorDrawable(palette.bg.toArgb()))

            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !palette.isDark
            insetsController.isAppearanceLightNavigationBars = !palette.isDark
        }
    }

    val uiSettings = AppUiSettings(
        appTheme          = currentAppTheme,
        accent            = accent,
        highlight         = highlight,
        secondaryAccent   = secondaryAccent,
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

/** Safely unwraps ContextWrappers to the hosting Activity window (a bare `as Activity` cast can crash). */
private fun Context.findHostWindow(): android.view.Window? {
    var c: Context? = this
    while (c is android.content.ContextWrapper) {
        if (c is Activity) return c.window
        c = c.baseContext
    }
    return null
}

/** Persist a new theme mode (call from Settings). */
fun setThemeMode(context: Context, mode: ThemeMode) {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit().putString(KEY_THEME_MODE, mode.name).apply()
}

/** Persist a new full app theme (call from Appearance popup or Settings). */
fun setAppTheme(context: Context, theme: AppTheme) {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putString(KEY_APP_THEME, theme.name)
        .putInt(KEY_ACCENT_COLOR, theme.primaryAccent.toArgb())
        .apply()
    com.example.myapplication.AppIconManager.onThemeChanged(context, theme)
}

/** Persist a new accent color (call from the Appearance popup or Settings). */
fun setAccentColor(context: Context, color: Color) {
    val matchingTheme = AppTheme.entries.firstOrNull { it.primaryAccent == color || it.lightAccent == color } ?: AppTheme.IRIS_ROYAL
    setAppTheme(context, matchingTheme)
}