package com.raman.CoverPlay.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp

data class CustomThemeColors(
    val first: Int = 0x25D995,
    val second: Int = 0x116B4B,
    val third: Int = 0x34D399,
) {
    init {
        require(listOf(first, second, third).all { it in 0..0xFFFFFF })
    }

    val colors: List<Int> get() = listOf(first, second, third)
    val colorList: List<Color> get() = colors.map { Color(it or 0xFF000000.toInt()) }

    val isSolid: Boolean get() = first == second && second == third

    fun withColor(index: Int, color: Int): CustomThemeColors = when (index) {
        0 -> copy(first = color)
        1 -> copy(second = color)
        2 -> copy(third = color)
        else -> this
    }

    fun encode(): String = colors.joinToString(",") { "#" + it.toString(16).padStart(6, '0').uppercase() }

    companion object {
        val Default = CustomThemeColors()

        fun solid(color: Int): CustomThemeColors = CustomThemeColors(color, color, color)

        fun decode(value: String?): CustomThemeColors {
            val colors = value?.split(",")?.mapNotNull { parseHexColor(it) } ?: return Default
            return if (colors.size == 3) CustomThemeColors(colors[0], colors[1], colors[2]) else Default
        }
    }
}

fun parseHexColor(value: String): Int? {
    val hex = value.trim().removePrefix("#")
    if (hex.length != 6 || hex.any { it !in '0'..'9' && it.uppercaseChar() !in 'A'..'F' }) return null
    return hex.toIntOrNull(16)
}

fun formatHexColor(color: Int): String = "#" + color.toString(16).padStart(6, '0').uppercase()

object CoverPlayTokens {
    object Radius {
        val badge = 6.dp
        val chip = 999.dp
        val control = 14.dp
        val input = 18.dp
        val thumbnail = 14.dp
        val icon = 12.dp
        val compactCard = 16.dp
        val card = 20.dp
        val largeCard = 24.dp
        val hero = 24.dp
        val sheet = 28.dp
        val full = 999.dp
    }

    object Space {
        val s4 = 4.dp
        val s8 = 8.dp
        val s12 = 12.dp
        val s16 = 16.dp
        val s20 = 20.dp
        val s24 = 24.dp
        val s32 = 32.dp

        val x1 = 4.dp
        val x2 = 8.dp
        val x3 = 12.dp
        val x4 = 16.dp
        val x5 = 20.dp
        val x6 = 24.dp
        val section = 28.dp
    }

    object Palette {
        val backgroundPrimary: Color get() = LiveTheme.palette.bg
        val backgroundSecondary: Color get() = LiveTheme.palette.containerLow
        val background: Color get() = LiveTheme.palette.bg
        val surface: Color get() = LiveTheme.palette.container
        val elevated: Color get() = LiveTheme.palette.containerHigh
        val interactive: Color get() = LiveTheme.palette.containerHighest
        val highest: Color get() = LiveTheme.palette.containerHighest

        val primaryEmerald: Color get() = LiveTheme.accent
        val darkEmerald: Color get() = LiveTheme.secondary

        val textPrimary: Color get() = LiveTheme.palette.textPrimary
        val textSecondary: Color get() = LiveTheme.palette.textSecondary
        val textMuted: Color get() = LiveTheme.palette.textTertiary

        val border: Color get() = LiveTheme.palette.outlineFaint
        val borderStrong: Color get() = LiveTheme.palette.outlineMedium

        val error = Color(0xFFF87171)
        val warning = Color(0xFFFBBF24)
    }
}

object CoverPlayDimens {
    val screenHorizontal = 20.dp
    val sectionSpacing = 28.dp
    val cardPadding = 16.dp
    val settingsRowHeight = 76.dp
    val iconContainer = 40.dp
    val iconContainerLarge = 48.dp
    val buttonHeight = 52.dp
    val minTouchTarget = 48.dp
    val navHeight = 76.dp
    val navHorizontalPadding = 20.dp
}

object CoverPlayBaseTokens {
    object Radius {
        val none = 0.dp
        val xs = 4.dp
        val sm = 6.dp
        val md = 8.dp
        val lg = 12.dp
        val xl = 14.dp
        val xxl = 20.dp
        val full = 999.dp
    }

    object Space {
        val hairline = 0.5.dp
        val s1 = 1.dp
        val s2 = 2.dp
        val s4 = 4.dp
        val s8 = 8.dp
        val s10 = 10.dp
        val s12 = 12.dp
        val s14 = 14.dp
        val s16 = 16.dp
        val s18 = 18.dp
        val s20 = 20.dp
        val s24 = 24.dp
        val s28 = 28.dp
        val s32 = 32.dp
    }
}

object CoverPlayShapeTokens {
    val card = RoundedCornerShape(CoverPlayTokens.Radius.card)
    val compactCard = RoundedCornerShape(CoverPlayTokens.Radius.compactCard)
    val sheet = RoundedCornerShape(CoverPlayTokens.Radius.sheet)
    val dialog = RoundedCornerShape(CoverPlayTokens.Radius.largeCard)
    val button = RoundedCornerShape(CoverPlayTokens.Radius.control)
    val chip = CircleShape
    val poster = RoundedCornerShape(CoverPlayTokens.Radius.thumbnail)
    val avatar = CircleShape
    val playerPanel = RoundedCornerShape(CoverPlayTokens.Radius.sheet)
    val navItemShape = RoundedCornerShape(14.dp)
}

object CoverPlaySpacingTokens {
    val screenHorizontal = CoverPlayDimens.screenHorizontal
    val screenTop = 10.dp
    val screenBottom = 18.dp
    val sectionGap = CoverPlayDimens.sectionSpacing
    val listGap = 12.dp
    val railGap = 14.dp
    val cardPadding = CoverPlayDimens.cardPadding
    val cardPaddingCompact = 12.dp
    val controlGap = 8.dp
    val dialogPadding = 20.dp
    val sheetPadding = 20.dp
}

object CoverPlayBorderTokens {
    val borderSubtle: Color get() = LiveTheme.palette.outlineFaint
    val borderDefault: Color get() = LiveTheme.palette.outlineFaint
    val borderStrong: Color get() = LiveTheme.palette.outlineMedium
}

object LiveTheme {
    var accent by mutableStateOf(DefaultSeedColor)
    var highlight by mutableStateOf(Color(0xFFB69DF8))
    var secondary by mutableStateOf(Color(0xFF8F86A8))
    var customColors by mutableStateOf(CustomThemeColors.Default)
    var palette by mutableStateOf(AppTheme.IRIS_ROYAL.palette(ThemeMode.DARK))

    var darkPalette by mutableStateOf(AppTheme.IRIS_ROYAL.palette(ThemeMode.DARK))
    var darkAccent by mutableStateOf(DefaultSeedColor)
    var darkHighlight by mutableStateOf(Color(0xFFB69DF8))
    var darkSecondary by mutableStateOf(Color(0xFF8F86A8))

    fun sync(a: Color, h: Color, s: Color, p: PaletteSet) {
        if (accent != a) accent = a
        if (highlight != h) highlight = h
        if (secondary != s) secondary = s
        if (palette !== p) palette = p
    }

    fun syncDark(a: Color, h: Color, s: Color, p: PaletteSet) {
        if (darkAccent != a) darkAccent = a
        if (darkHighlight != h) darkHighlight = h
        if (darkSecondary != s) darkSecondary = s
        if (darkPalette !== p) darkPalette = p
    }
}

fun contrastRatio(a: Color, b: Color): Float {
    val la = a.luminance()
    val lb = b.luminance()
    return (maxOf(la, lb) + 0.05f) / (minOf(la, lb) + 0.05f)
}

fun Color.onFill(): Color {
    val fill = this.copy(alpha = 1f)
    val dark = lerp(fill, Color.Black, 0.88f)
    val vsWhite = contrastRatio(fill, Color.White)
    val vsDark = contrastRatio(fill, dark)
    return if (vsWhite >= 4.5f || vsWhite >= vsDark) Color.White else dark
}

fun Color.onCoverPlayForeground(): Color = onFill()
fun Color.forWhiteContent(minContrast: Float = 4.5f): Color = this

fun Color.withContrastOn(background: Color, minContrast: Float = 4.5f): Color {
    var c = this
    var i = 0
    while (contrastRatio(c, background) < minContrast && i < 24) {
        c = lerp(c, Color.Black, 0.06f)
        i++
    }
    return c
}

val IconPeriwinkle: Color get() = LiveTheme.highlight
val IconIndigo: Color get() = LiveTheme.accent
val IconRoyalBlue: Color get() = lerp(LiveTheme.accent, Color.Black, 0.45f)
val IconSky: Color get() = LiveTheme.accent
val IconWhite = Color(0xFFFFFFFF)

val BrandPrimary: Color get() = LiveTheme.accent
val BrandSecondary: Color get() = LiveTheme.highlight
val BrandTertiary: Color get() = LiveTheme.secondary
val BrandAccent: Color get() = LiveTheme.highlight
val BrandMagenta: Color get() = LiveTheme.secondary
val BrandOrange = Color(0xFFF97316)
val BrandViolet: Color get() = LiveTheme.accent

val BrandBlue: Color get() = BrandPrimary
val BrandRed = Color(0xFFF87171)
val BrandHighlight = Color(0xFFFFFFFF)
val BrandIceBlue: Color get() = Color(0xFFE0E7FF)
val BrandGold = StatusColors.quality
val StatusSuccess = StatusColors.success
val BrandGoldLight = Color(0xFFFEF3C7)
val BrandCyan: Color get() = LiveTheme.accent

val AccentFill: Color get() = LiveTheme.accent.forWhiteContent()

val BrandGradient: Brush
    get() = Brush.linearGradient(listOf(LiveTheme.accent, LiveTheme.highlight))

val BrandGradientSoft: Brush
    get() = Brush.linearGradient(listOf(LiveTheme.accent, LiveTheme.accent.copy(alpha = 0.80f)))

val BrandGradientRadial: Brush
    get() = Brush.radialGradient(listOf(LiveTheme.accent, LiveTheme.accent.copy(alpha = 0.6f)))

val BgBase: Color get() = LiveTheme.palette.bg
val SurfaceNavy: Color get() = LiveTheme.palette.surfaceNavy
val SurfaceRaised: Color get() = LiveTheme.palette.surfaceRaised
val SurfaceRaisedAlt: Color get() = LiveTheme.palette.surfaceRaisedAlt
val SurfaceContainerLowest: Color get() = LiveTheme.palette.containerLowest
val SurfaceContainerLow: Color get() = LiveTheme.palette.containerLow
val SurfaceContainer: Color get() = LiveTheme.palette.container
val SurfaceContainerHigh: Color get() = LiveTheme.palette.containerHigh
val SurfaceContainerHighest: Color get() = LiveTheme.palette.containerHighest
val OutlineFaint: Color get() = LiveTheme.palette.outlineFaint
val OutlineMedium: Color get() = LiveTheme.palette.outlineMedium
val OutlineBright: Color get() = LiveTheme.palette.outlineBright
val OutlineActive: Color get() = LiveTheme.accent
val TextPrimary: Color get() = LiveTheme.palette.textPrimary
val TextSecondary: Color get() = LiveTheme.palette.textSecondary
val TextTertiary: Color get() = LiveTheme.palette.textTertiary

val BrandBlueDeep: Color get() = LiveTheme.palette.bg
val BrandBlueShadow: Color get() = LiveTheme.accent.copy(alpha = 0.20f)

val InverseSurface = Color(0xFFF2F5F3)
val InverseOnSurface = Color(0xFF0B0B0F)
val InversePrimary: Color get() = LiveTheme.accent

val ShadowAmbient = Color.Black.copy(alpha = 0.40f)
val ShadowSpot = Color.Black.copy(alpha = 0.30f)

data class PaletteSet(
    val bg: Color,
    val surfaceNavy: Color,
    val surfaceRaised: Color,
    val surfaceRaisedAlt: Color,
    val containerLowest: Color,
    val containerLow: Color,
    val container: Color,
    val containerHigh: Color,
    val containerHighest: Color,
    val outlineFaint: Color,
    val outlineMedium: Color,
    val outlineBright: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val isDark: Boolean,
    val glassBase: Color = bg,
    val videoGlass: Color = Color(0xFF0B0B0F)
)
