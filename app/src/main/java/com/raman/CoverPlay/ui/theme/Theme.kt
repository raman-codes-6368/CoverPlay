package com.raman.CoverPlay.ui.theme

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.graphics.drawable.ColorDrawable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private const val PREFS_NAME              = "coverplay_settings_v3"
private const val KEY_ACCENT_COLOR        = "accent_color"
private const val KEY_FONT_SCALE          = "font_scale"
private const val KEY_SHOW_DURATION_BADGE = "show_duration_badge"
private const val KEY_COMPACT_LIST        = "compact_list"
private const val KEY_THEME_MODE          = "theme_mode"
private const val KEY_THEME_CHOICE        = "theme_choice"
private const val KEY_SYSTEM_AMOLED      = "system_amoled"
private const val KEY_USE_SYSTEM_FONT     = "use_system_font"
private const val KEY_HIDE_THUMBNAILS     = "hide_thumbnails"
private const val KEY_FLOATING_NAV        = "floating_nav"
private const val KEY_HIDE_TOPBAR         = "hide_topbar_on_scroll"
private const val KEY_SHOW_SPLASH         = "show_splash"
private const val KEY_DYNAMIC_COLOR       = "dynamic_color"
private const val KEY_SEEKBAR_STYLE       = "seekbar_style"
private const val KEY_SEEKBAR_THUMB       = "seekbar_thumb"
private const val KEY_APP_FONT            = "app_font"

enum class SeekbarStyle(val label: String) {
    SQUIGGLY("Squiggly"), WAVY("Wavy"), FLAT("Line"), THICK("Bold"), DOTTED("Dots")
}

enum class SeekbarThumb(val label: String) {
    CIRCLE("Circle"), BAR("Bar"), DIAMOND("Diamond")
}

enum class ThemeMode(val label: String) {
    LIGHT("Light"),
    DARK("Dark"),
    AMOLED("AMOLED")
}

enum class BackdropScene {
    BLOOM,
    NEBULA,
    VELVET,
    HORIZON,
    AURORA
}

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
    val tone: Color,
    val elevatedDark: Color,
    val cardDark: Color,
    val focusDark: Color
) {
    IRIS_ROYAL(
        id = "iris_royal",
        label = "Jade",
        tagline = "Deep green-black · one jade light",
        scene = BackdropScene.AURORA,
        primaryAccent = Color(0xFF1FE58A),
        lightAccent = Color(0xFF08894F),
        highlight = Color(0xFF7BF08D),
        secondaryAccent = Color(0xFF0BBF9A),
        bgDark = Color(0xFF0B0F0D),
        bgLight = Color(0xFFF4F7F5),
        tone = Color(0xFF22D37C),
        elevatedDark = Color(0xFF141D18),
        cardDark = Color(0xFF16251D),
        focusDark = Color(0xFF153A2C)
    ),
    NEON_VIOLET(
        id = "neon_violet",
        label = "Violet",
        tagline = "Soft orchid on deep ink",
        scene = BackdropScene.AURORA,
        primaryAccent = Color(0xFFB45CFF),
        lightAccent = Color(0xFF8E24AA),
        highlight = Color(0xFFCE93D8),
        secondaryAccent = Color(0xFFB75AFF),
        bgDark = Color(0xFF0D0D0F),
        bgLight = Color(0xFFF8F5FA),
        tone = Color(0xFFAB47BC),
        elevatedDark = Color(0xFF1A1A1E),
        cardDark = Color(0xFF1F1A24),
        focusDark = Color(0xFF2D1A3D)
    ),
    CYBER_MAGENTA(
        id = "cyber_magenta",
        label = "Rose Gold",
        tagline = "Warm rose over plum-black",
        scene = BackdropScene.AURORA,
        primaryAccent = Color(0xFFFF5FA2),
        lightAccent = Color(0xFFC2185B),
        highlight = Color(0xFFFFB37A),
        secondaryAccent = Color(0xFFB75AFF),
        bgDark = Color(0xFF100C0F),
        bgLight = Color(0xFFFAF5F7),
        tone = Color(0xFFEC70A9),
        elevatedDark = Color(0xFF1F161D),
        cardDark = Color(0xFF281A24),
        focusDark = Color(0xFF442037)
    ),
    SOLAR_EMBER(
        id = "solar_ember",
        label = "Gold",
        tagline = "A single warm glow",
        scene = BackdropScene.AURORA,
        primaryAccent = Color(0xFFFFB81F),
        lightAccent = Color(0xFF9A6200),
        highlight = Color(0xFFFFD45C),
        secondaryAccent = Color(0xFFF0B93A),
        bgDark = Color(0xFF0F0E0B),
        bgLight = Color(0xFFFAF7F0),
        tone = Color(0xFFE8A91C),
        elevatedDark = Color(0xFF1D1A14),
        cardDark = Color(0xFF262116),
        focusDark = Color(0xFF3D2D1A)
    ),
    HYPER_CYAN(
        id = "hyper_cyan",
        label = "Arctic Blue",
        tagline = "Cool blue on night graphite",
        scene = BackdropScene.AURORA,
        primaryAccent = Color(0xFF2E8BFF),
        lightAccent = Color(0xFF1560D6),
        highlight = Color(0xFF4DE3FF),
        secondaryAccent = Color(0xFF4D55E8),
        bgDark = Color(0xFF0B0E14),
        bgLight = Color(0xFFF3F6FA),
        tone = Color(0xFF3185F5),
        elevatedDark = Color(0xFF141A24),
        cardDark = Color(0xFF161E2A),
        focusDark = Color(0xFF172844)
    );

    fun accentFor(mode: ThemeMode): Color = if (mode == ThemeMode.LIGHT) lightAccent else primaryAccent

    fun highlightFor(mode: ThemeMode): Color =
        if (mode == ThemeMode.LIGHT) lerp(lightAccent, highlight, 0.30f) else highlight

    fun secondaryFor(mode: ThemeMode): Color =
        if (mode == ThemeMode.LIGHT) lerp(secondaryAccent, Color.Black, 0.12f) else secondaryAccent

    fun palette(mode: ThemeMode): PaletteSet =
        seedColorScheme(primaryAccent, mode).toPaletteSet(mode != ThemeMode.LIGHT)

    fun swatchBrush(): Brush = Brush.linearGradient(
        listOf(highlight, primaryAccent, lerp(primaryAccent, Color.Black, 0.35f))
    )

    fun gradient(): Brush = Brush.linearGradient(listOf(highlight, primaryAccent, secondaryAccent))

    fun radialGradient(): Brush = Brush.radialGradient(
        listOf(highlight, primaryAccent, primaryAccent.copy(alpha = 0.7f))
    )
}

val ExtraAccentSwatches: List<Color> = listOf(
    Color(0xFFFF6B5E),
    Color(0xFF38BDF8),
    Color(0xFFA3E635),
    Color(0xFFFF8C3B),
    Color(0xFFEF4444)
)

val AccentSwatches: List<Color> = listOf(DefaultSeedColor) + AppTheme.entries.map { it.primaryAccent } + ExtraAccentSwatches

class AccentRoles(val accent: Color, val highlight: Color, val secondary: Color)

fun accentRolesFor(color: Color, mode: ThemeMode): AccentRoles {
    val trio = seedColorScheme(color, mode).toAccentTrio()
    return AccentRoles(trio.accent, trio.highlight, trio.secondary)
}

private fun readThemeChoice(prefs: SharedPreferences): ThemeChoice = when {
    prefs.contains(KEY_THEME_CHOICE) -> ThemeChoice.fromName(prefs.getString(KEY_THEME_CHOICE, null))
    prefs.contains(KEY_THEME_MODE) -> when (prefs.getString(KEY_THEME_MODE, null)) {
        ThemeMode.LIGHT.name -> ThemeChoice.LIGHT
        ThemeMode.AMOLED.name -> ThemeChoice.AMOLED
        else -> ThemeChoice.DARK
    }
    else -> ThemeChoice.SYSTEM
}

const val KEY_APP_THEME = "key_app_theme"
const val KEY_SECTION_POINTER = "section_header_pointer"

data class AppUiSettings(
    val appTheme: AppTheme          = AppTheme.IRIS_ROYAL,
    val accent: Color              = AppTheme.IRIS_ROYAL.primaryAccent,
    val highlight: Color           = AppTheme.IRIS_ROYAL.highlight,
    val secondaryAccent: Color     = AppTheme.IRIS_ROYAL.secondaryAccent,
    val showDurationBadge: Boolean = true,
    val compactList: Boolean       = false,
    val useSystemFont: Boolean     = false,
    val appFont: AppFont           = AppFont.GOOGLE_SANS_FLEX,
    val hideThumbnails: Boolean    = false,
    val showSplash: Boolean        = true,
    val floatingNav: Boolean       = false,
    val seekbarStyle: SeekbarStyle = SeekbarStyle.SQUIGGLY,
    val seekbarThumb: SeekbarThumb = SeekbarThumb.CIRCLE,
    val hideTopBarOnScroll: Boolean = false,
    val showSectionPointer: Boolean = true,
    val themeMode: ThemeMode       = ThemeMode.DARK,
    val palette: PaletteSet        = AppTheme.IRIS_ROYAL.palette(ThemeMode.DARK),
    val themeChoice: ThemeChoice   = ThemeChoice.SYSTEM,
    val dynamicColor: Boolean      = false,
    val systemAmoled: Boolean      = false,
    val seedColor: Color           = DefaultSeedColor
)

val LocalAppUiSettings = compositionLocalOf { AppUiSettings() }

val CoverPlayShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small      = RoundedCornerShape(14.dp),
    medium     = RoundedCornerShape(CoverPlayTokens.Radius.card),
    large      = RoundedCornerShape(CoverPlayTokens.Radius.largeCard),
    extraLarge = RoundedCornerShape(CoverPlayTokens.Radius.sheet),
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CoverPlayTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val prefs   = remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }

    var appThemeName      by remember { mutableStateOf(prefs.getString(KEY_APP_THEME, AppTheme.IRIS_ROYAL.name) ?: AppTheme.IRIS_ROYAL.name) }
    var fontScale         by remember { mutableStateOf(prefs.getFloat(KEY_FONT_SCALE, 1.0f)) }
    var showDurationBadge by remember { mutableStateOf(prefs.getBoolean(KEY_SHOW_DURATION_BADGE, true)) }
    var compactList       by remember { mutableStateOf(prefs.getBoolean(KEY_COMPACT_LIST, false)) }
    var accentArgb        by remember { mutableStateOf(prefs.getInt(KEY_ACCENT_COLOR, DefaultSeedColor.toArgb())) }
    var themeChoiceName   by remember { mutableStateOf(readThemeChoice(prefs).name) }
    var systemAmoled      by remember { mutableStateOf(prefs.getBoolean(KEY_SYSTEM_AMOLED, false)) }
    var useSystemFont     by remember { mutableStateOf(prefs.getBoolean(KEY_USE_SYSTEM_FONT, false)) }
    fun readAppFont(p: android.content.SharedPreferences): String =
        p.getString(KEY_APP_FONT, null) ?: if (p.getBoolean(KEY_USE_SYSTEM_FONT, false)) AppFont.SYSTEM.name else AppFont.GOOGLE_SANS_FLEX.name
    var appFontName       by remember { mutableStateOf(readAppFont(prefs)) }
    var hideThumbnails    by remember { mutableStateOf(prefs.getBoolean(KEY_HIDE_THUMBNAILS, false)) }
    var showSplash        by remember { mutableStateOf(prefs.getBoolean(KEY_SHOW_SPLASH, true)) }
    var floatingNav       by remember { mutableStateOf(prefs.getBoolean(KEY_FLOATING_NAV, true)) }
    var seekbarStyleName  by remember { mutableStateOf(prefs.getString(KEY_SEEKBAR_STYLE, SeekbarStyle.SQUIGGLY.name) ?: SeekbarStyle.SQUIGGLY.name) }
    var seekbarThumbName  by remember { mutableStateOf(prefs.getString(KEY_SEEKBAR_THUMB, SeekbarThumb.CIRCLE.name) ?: SeekbarThumb.CIRCLE.name) }
    var dynamicColor      by remember { mutableStateOf(prefs.getBoolean(KEY_DYNAMIC_COLOR, false)) }
    var hideTopBar        by remember { mutableStateOf(prefs.getBoolean(KEY_HIDE_TOPBAR, false)) }
    var sectionPointer    by remember { mutableStateOf(prefs.getBoolean(KEY_SECTION_POINTER, true)) }

    DisposableEffect(prefs) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { p, key ->
            when (key) {
                KEY_APP_THEME           -> appThemeName       = p.getString(key, AppTheme.IRIS_ROYAL.name) ?: AppTheme.IRIS_ROYAL.name
                KEY_ACCENT_COLOR        -> accentArgb         = p.getInt(key, DefaultSeedColor.toArgb())
                KEY_FONT_SCALE          -> fontScale          = p.getFloat(key, 1.0f)
                KEY_SHOW_DURATION_BADGE -> showDurationBadge  = p.getBoolean(key, true)
                KEY_COMPACT_LIST        -> compactList        = p.getBoolean(key, false)
                KEY_THEME_CHOICE, KEY_THEME_MODE -> themeChoiceName = readThemeChoice(p).name
                KEY_SYSTEM_AMOLED       -> systemAmoled       = p.getBoolean(key, false)
                KEY_USE_SYSTEM_FONT     -> useSystemFont      = p.getBoolean(key, false)
                KEY_APP_FONT            -> appFontName        = readAppFont(p)
                KEY_HIDE_THUMBNAILS     -> hideThumbnails     = p.getBoolean(key, false)
                KEY_SHOW_SPLASH         -> showSplash         = p.getBoolean(key, true)
                KEY_FLOATING_NAV        -> floatingNav        = p.getBoolean(key, true)
                KEY_SEEKBAR_STYLE       -> seekbarStyleName   = p.getString(key, SeekbarStyle.SQUIGGLY.name) ?: SeekbarStyle.SQUIGGLY.name
                KEY_SEEKBAR_THUMB       -> seekbarThumbName   = p.getString(key, SeekbarThumb.CIRCLE.name) ?: SeekbarThumb.CIRCLE.name
                KEY_DYNAMIC_COLOR       -> dynamicColor       = p.getBoolean(key, false)
                KEY_HIDE_TOPBAR         -> hideTopBar         = p.getBoolean(key, false)
                KEY_SECTION_POINTER     -> sectionPointer     = p.getBoolean(key, true)
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    val currentAppTheme = AppTheme.entries.firstOrNull { it.name == appThemeName } ?: AppTheme.IRIS_ROYAL

    val systemDark = isSystemInDarkTheme()
    val themeChoice = ThemeChoice.fromName(themeChoiceName)
    val themeMode = when (themeChoice) {
        ThemeChoice.SYSTEM -> when {
            !systemDark -> ThemeMode.LIGHT
            systemAmoled -> ThemeMode.AMOLED
            else -> ThemeMode.DARK
        }
        ThemeChoice.LIGHT  -> ThemeMode.LIGHT
        ThemeChoice.DARK   -> ThemeMode.DARK
        ThemeChoice.AMOLED -> ThemeMode.AMOLED
    }

    val useWallpaper = dynamicColor && DynamicColorAvailable
    val configuration = LocalConfiguration.current
    val colorScheme = remember(themeMode, useWallpaper, accentArgb, configuration) {
        (if (useWallpaper) wallpaperColorScheme(context, themeMode) else null)
            ?: seedColorScheme(Color(accentArgb), themeMode)
    }
    val darkMode = if (themeMode == ThemeMode.LIGHT) ThemeMode.DARK else themeMode
    val darkScheme = remember(darkMode, useWallpaper, accentArgb, configuration) {
        if (themeMode != ThemeMode.LIGHT) colorScheme
        else (if (useWallpaper) wallpaperColorScheme(context, darkMode) else null)
            ?: seedColorScheme(Color(accentArgb), darkMode)
    }
    val palette = remember(colorScheme, themeMode) { colorScheme.toPaletteSet(themeMode != ThemeMode.LIGHT) }
    val darkPalette = remember(darkScheme) { darkScheme.toPaletteSet(true) }
    val trio = remember(colorScheme) { colorScheme.toAccentTrio() }
    val darkTrio = remember(darkScheme) { darkScheme.toAccentTrio() }
    val accent = trio.accent
    val highlight = trio.highlight
    val secondaryAccent = trio.secondary

    LiveTheme.sync(accent, highlight, secondaryAccent, palette)
    LiveTheme.syncDark(darkTrio.accent, darkTrio.highlight, darkTrio.secondary, darkPalette)

    val appFont = AppFont.fromName(appFontName)
    val baseTypography = remember(appFont) { cinematicTypography(appFont) }
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

    CoverPlaySkeletonClockEffect()

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = view.context.findHostWindow() ?: return@SideEffect
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = palette.bg.toArgb()
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
        appFont           = appFont,
        hideThumbnails    = hideThumbnails,
        showSplash        = showSplash,
        floatingNav       = floatingNav,
        seekbarStyle      = SeekbarStyle.entries.firstOrNull { it.name == seekbarStyleName } ?: SeekbarStyle.SQUIGGLY,
        seekbarThumb      = SeekbarThumb.entries.firstOrNull { it.name == seekbarThumbName } ?: SeekbarThumb.CIRCLE,
        hideTopBarOnScroll = hideTopBar,
        showSectionPointer = sectionPointer,
        themeMode         = themeMode,
        palette           = palette,
        themeChoice       = themeChoice,
        dynamicColor      = useWallpaper,
        systemAmoled      = systemAmoled,
        seedColor         = Color(accentArgb)
    )

    val rotationClock = rememberInfiniteTransition(label = "shapeRotation")
    val shapeRotation = rotationClock.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(24_000, easing = LinearEasing)),
        label = "shapeRotationDeg"
    )

    CompositionLocalProvider(
        LocalAppUiSettings provides uiSettings,
        LocalShapeRotation provides shapeRotation
    ) {
        MaterialExpressiveTheme(
            colorScheme  = colorScheme,
            motionScheme = MotionScheme.standard(),
            typography   = scaledTypography,
            shapes       = CoverPlayShapes,
            content      = content
        )
    }
}

private fun Context.findHostWindow(): android.view.Window? {
    var c: Context? = this
    while (c is android.content.ContextWrapper) {
        if (c is Activity) return c.window
        c = c.baseContext
    }
    return null
}

fun setThemeChoice(context: Context, choice: ThemeChoice) {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit().putString(KEY_THEME_CHOICE, choice.name).apply()
}

fun setSystemAmoled(context: Context, enabled: Boolean) {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit().putBoolean(KEY_SYSTEM_AMOLED, enabled).apply()
}

fun setDynamicColor(context: Context, enabled: Boolean) {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit().putBoolean(KEY_DYNAMIC_COLOR, enabled).apply()
}

fun setAppTheme(context: Context, theme: AppTheme) {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putString(KEY_APP_THEME, theme.name)
        .putInt(KEY_ACCENT_COLOR, theme.primaryAccent.toArgb())
        .putBoolean(KEY_DYNAMIC_COLOR, false)
        .apply()
}

fun setAccentColor(context: Context, color: Color) {
    val preset = AppTheme.entries.firstOrNull { it.primaryAccent.toArgb() == color.toArgb() }
    if (preset != null) {
        setAppTheme(context, preset)
    } else {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_ACCENT_COLOR, color.toArgb())
            .putBoolean(KEY_DYNAMIC_COLOR, false)
            .apply()
    }
}

fun setThemeMode(context: Context, mode: ThemeMode) {
    setThemeChoice(
        context,
        when (mode) {
            ThemeMode.LIGHT -> ThemeChoice.LIGHT
            ThemeMode.DARK -> ThemeChoice.DARK
            ThemeMode.AMOLED -> ThemeChoice.AMOLED
        }
    )
}
