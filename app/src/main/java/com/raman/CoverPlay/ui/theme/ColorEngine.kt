package com.raman.CoverPlay.ui.theme

import android.content.Context
import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

val DefaultSeedColor = Color(0xFFA9BEFF)

object BrandDark {
    val background = Color(0xFF101116)
    val surface = Color(0xFF191B23)
    val elevated = Color(0xFF222531)
    val outline = Color(0xFF343744)
    val accent = Color(0xFFA9BEFF)
    val secondary = Color(0xFF899BCB)
    val textPrimary = Color(0xFFF2F3F8)
    val textSecondary = Color(0xFFA0A3B0)
}

object StatusColors {
    val success = Color(0xFF6BDBA0)
    val quality = Color(0xFFFFC857)
}

class ThemePreset(val id: String, val label: String, val seed: Color)

val ThemePresets: List<ThemePreset> = listOf(
    ThemePreset("lavender", "Lavender", Color(0xFFA9BEFF)),
    ThemePreset("ocean", "Ocean", Color(0xFF0B6BCB)),
    ThemePreset("mint", "Mint", Color(0xFF00897B)),
    ThemePreset("forest", "Forest", Color(0xFF2E7D32)),
    ThemePreset("amber", "Amber", Color(0xFFF2A900)),
    ThemePreset("sunset", "Sunset", Color(0xFFE8590C)),
    ThemePreset("rose", "Rose", Color(0xFFC2185B)),
    ThemePreset("slate", "Slate", Color(0xFF5C6F7B)),
)

enum class ThemeChoice(val label: String) {
    SYSTEM("System"),
    LIGHT("Light"),
    DARK("Dark"),
    AMOLED("AMOLED");

    companion object {
        fun fromName(name: String?): ThemeChoice = entries.firstOrNull { it.name == name } ?: SYSTEM
    }
}

val DynamicColorAvailable: Boolean get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

private class Hsl(val h: Float, val s: Float, val l: Float)

private fun Color.toHsl(): Hsl {
    val r = red
    val g = green
    val b = blue
    val mx = max(r, max(g, b))
    val mn = min(r, min(g, b))
    val l = (mx + mn) / 2f
    val d = mx - mn
    if (d < 1e-5f) return Hsl(0f, 0f, l)
    val s = d / (1f - abs(2f * l - 1f)).coerceAtLeast(1e-5f)
    var h = when {
        mx == r -> ((g - b) / d) % 6f
        mx == g -> (b - r) / d + 2f
        else -> (r - g) / d + 4f
    } * 60f
    if (h < 0f) h += 360f
    return Hsl(h, s.coerceIn(0f, 1f), l)
}

private fun hsl(h: Float, s: Float, l: Float): Color {
    val hh = ((h % 360f) + 360f) % 360f
    val ll = l.coerceIn(0f, 1f)
    val c = (1f - abs(2f * ll - 1f)) * s.coerceIn(0f, 1f)
    val x = c * (1f - abs((hh / 60f) % 2f - 1f))
    val m = ll - c / 2f
    val r: Float
    val g: Float
    val b: Float
    when {
        hh < 60f -> { r = c; g = x; b = 0f }
        hh < 120f -> { r = x; g = c; b = 0f }
        hh < 180f -> { r = 0f; g = c; b = x }
        hh < 240f -> { r = 0f; g = x; b = c }
        hh < 300f -> { r = x; g = 0f; b = c }
        else -> { r = c; g = 0f; b = x }
    }
    return Color(
        (r + m).coerceIn(0f, 1f),
        (g + m).coerceIn(0f, 1f),
        (b + m).coerceIn(0f, 1f)
    )
}

private class SurfaceLadder(
    val bg: Color, val lowest: Color, val low: Color, val container: Color,
    val high: Color, val highest: Color,
    val outline: Color, val outlineVariant: Color,
    val onSurface: Color, val onSurfaceVariant: Color,
    val surfaceVariant: Color
)

private fun surfaceLadder(hue: Float, sat: Float, mode: ThemeMode): SurfaceLadder {
    fun n(l: Float, sMul: Float = 1f) = hsl(hue, sat * sMul, l)
    return when (mode) {
        ThemeMode.LIGHT -> SurfaceLadder(
            bg = n(0.925f), lowest = Color.White, low = n(0.985f, 0.6f), container = n(0.968f),
            high = n(0.996f, 0.4f), highest = n(0.948f),
            outline = n(0.42f, 0.6f), outlineVariant = n(0.835f, 0.8f),
            onSurface = n(0.09f), onSurfaceVariant = n(0.27f, 0.7f),
            surfaceVariant = n(0.90f)
        )
        ThemeMode.AMOLED -> SurfaceLadder(
            bg = Color.Black, lowest = Color.Black, low = n(0.050f), container = n(0.080f),
            high = n(0.118f), highest = n(0.165f),
            outline = n(0.58f, 0.7f), outlineVariant = n(0.26f, 0.7f),
            onSurface = n(0.93f), onSurfaceVariant = n(0.80f, 0.7f),
            surfaceVariant = n(0.20f)
        )
        ThemeMode.DARK -> SurfaceLadder(
            bg = n(0.068f), lowest = n(0.042f), low = n(0.098f), container = n(0.128f),
            high = n(0.170f), highest = n(0.220f),
            outline = n(0.60f, 0.7f), outlineVariant = n(0.29f, 0.7f),
            onSurface = n(0.93f), onSurfaceVariant = n(0.80f, 0.7f),
            surfaceVariant = n(0.22f)
        )
    }
}

private fun ColorScheme.withLadder(l: SurfaceLadder): ColorScheme = copy(
    background = l.bg, onBackground = l.onSurface,
    surface = l.bg, onSurface = l.onSurface,
    surfaceVariant = l.surfaceVariant, onSurfaceVariant = l.onSurfaceVariant,
    surfaceTint = Color.Transparent,
    surfaceContainerLowest = l.lowest, surfaceContainerLow = l.low, surfaceContainer = l.container,
    surfaceContainerHigh = l.high, surfaceContainerHighest = l.highest,
    inverseSurface = l.onSurface, inverseOnSurface = l.bg,
    outline = l.outline, outlineVariant = l.outlineVariant,
    scrim = Color.Black
)

private fun brandColorScheme(mode: ThemeMode): ColorScheme {
    val b = BrandDark
    return when (mode) {
        ThemeMode.LIGHT -> lightColorScheme(
            primary = Color(0xFF4A5FA8), onPrimary = Color.White,
            primaryContainer = Color(0xFFDCE4FF), onPrimaryContainer = Color(0xFF0F1E4D),
            inversePrimary = b.accent,
            secondary = Color(0xFF5A6A94), onSecondary = Color.White,
            secondaryContainer = Color(0xFFDDE3F6), onSecondaryContainer = Color(0xFF16203D),
            tertiary = Color(0xFF6A5A9C), onTertiary = Color.White,
            tertiaryContainer = Color(0xFFE9DEFF), onTertiaryContainer = Color(0xFF221648)
        ).withLadder(
            SurfaceLadder(
                bg = Color(0xFFF1F2F8), lowest = Color.White, low = Color(0xFFF7F8FC),
                container = Color(0xFFFFFFFF), high = Color(0xFFFFFFFF), highest = Color(0xFFE7E9F3),
                outline = Color(0xFF70758A), outlineVariant = Color(0xFFD9DBE6),
                onSurface = Color(0xFF14151C), onSurfaceVariant = Color(0xFF4F5365),
                surfaceVariant = Color(0xFFE3E5F0)
            )
        )
        ThemeMode.DARK, ThemeMode.AMOLED -> {
            val amoled = mode == ThemeMode.AMOLED
            darkColorScheme(
                primary = b.accent, onPrimary = Color(0xFF1A2D63),
                primaryContainer = Color(0xFF2A3B73), onPrimaryContainer = Color(0xFFDCE4FF),
                inversePrimary = Color(0xFF4A5FA8),
                secondary = b.secondary, onSecondary = Color(0xFF1E2A4A),
                secondaryContainer = Color(0xFF2D3858), onSecondaryContainer = Color(0xFFD5DEFA),
                tertiary = Color(0xFFC9B8F2), onTertiary = Color(0xFF2E1F5A),
                tertiaryContainer = Color(0xFF45367A), onTertiaryContainer = Color(0xFFE9DEFF),
                error = Color(0xFFFFB4AB), onError = Color(0xFF690005),
                errorContainer = Color(0xFF93000A), onErrorContainer = Color(0xFFFFDAD6)
            ).withLadder(
                if (amoled) SurfaceLadder(
                    bg = Color.Black, lowest = Color.Black, low = Color(0xFF0A0B0F),
                    container = Color(0xFF121319), high = Color(0xFF1B1D26), highest = Color(0xFF242732),
                    outline = Color(0xFF4C5062), outlineVariant = Color(0xFF2C2F3B),
                    onSurface = b.textPrimary, onSurfaceVariant = b.textSecondary,
                    surfaceVariant = Color(0xFF1B1D26)
                ) else SurfaceLadder(
                    bg = b.background, lowest = Color(0xFF0C0D11), low = Color(0xFF15171D),
                    container = b.surface, high = b.elevated, highest = Color(0xFF2B2E3C),
                    outline = Color(0xFF4C5062), outlineVariant = b.outline,
                    onSurface = b.textPrimary, onSurfaceVariant = b.textSecondary,
                    surfaceVariant = b.elevated
                )
            )
        }
    }
}

fun seedColorScheme(seed: Color, mode: ThemeMode): ColorScheme {
    if (seed.copy(alpha = 1f) == DefaultSeedColor) return brandColorScheme(mode)
    val hsl = seed.toHsl()
    val h = hsl.h
    val hue = if (hsl.s < 0.08f) 220f else h
    val s = hsl.s.coerceIn(0.42f, 0.88f)
    val dark = mode != ThemeMode.LIGHT
    val tertiaryHue = hue + 60f

    fun p(l: Float) = hsl(hue, s, l)
    fun sec(l: Float) = hsl(hue, s * 0.34f, l)
    fun ter(l: Float) = hsl(tertiaryHue, s * 0.80f, l)

    val ladder = surfaceLadder(hue, if (dark) 0.14f else 0.18f, mode)
    val base = if (dark) darkColorScheme() else lightColorScheme()
    val withRoles = if (dark) {
        base.copy(
            primary = p(0.78f), onPrimary = p(0.18f),
            primaryContainer = p(0.28f), onPrimaryContainer = p(0.90f),
            inversePrimary = p(0.40f),
            secondary = sec(0.78f), onSecondary = sec(0.20f),
            secondaryContainer = sec(0.30f), onSecondaryContainer = sec(0.90f),
            tertiary = ter(0.78f), onTertiary = ter(0.20f),
            tertiaryContainer = ter(0.30f), onTertiaryContainer = ter(0.90f),
            error = Color(0xFFFFB4AB), onError = Color(0xFF690005),
            errorContainer = Color(0xFF93000A), onErrorContainer = Color(0xFFFFDAD6)
        )
    } else {
        base.copy(
            primary = p(0.40f), onPrimary = Color.White,
            primaryContainer = p(0.90f), onPrimaryContainer = p(0.14f),
            inversePrimary = p(0.80f),
            secondary = sec(0.40f), onSecondary = Color.White,
            secondaryContainer = sec(0.90f), onSecondaryContainer = sec(0.14f),
            tertiary = ter(0.40f), onTertiary = Color.White,
            tertiaryContainer = ter(0.90f), onTertiaryContainer = ter(0.14f),
            error = Color(0xFFBA1A1A), onError = Color.White,
            errorContainer = Color(0xFFFFDAD6), onErrorContainer = Color(0xFF410002)
        )
    }
    return withRoles.withLadder(ladder)
}

fun wallpaperColorScheme(context: Context, mode: ThemeMode): ColorScheme? {
    if (!DynamicColorAvailable) return null
    val scheme = if (mode == ThemeMode.LIGHT) dynamicLightColorScheme(context) else dynamicDarkColorScheme(context)
    val surf = scheme.surfaceContainer.toHsl()
    val hue = if (surf.s < 0.04f) scheme.primary.toHsl().h else surf.h
    val sat = surf.s.coerceIn(0.10f, 0.22f)
    return scheme
        .withLadder(surfaceLadder(hue, sat, mode))
}

fun ColorScheme.toPaletteSet(dark: Boolean): PaletteSet = PaletteSet(
    bg = background,
    surfaceNavy = surfaceContainer,
    surfaceRaised = surfaceContainerHigh,
    surfaceRaisedAlt = surfaceContainerHighest,
    containerLowest = surfaceContainerLowest,
    containerLow = surfaceContainerLow,
    container = surfaceContainer,
    containerHigh = surfaceContainerHigh,
    containerHighest = surfaceContainerHighest,
    outlineFaint = outlineVariant,
    outlineMedium = outline,
    outlineBright = lerp(outline, onSurface, 0.30f),
    textPrimary = onSurface,
    textSecondary = onSurfaceVariant,
    textTertiary = lerp(onSurfaceVariant, surface, 0.30f),
    isDark = dark,
    glassBase = surfaceContainer,
    videoGlass = lerp(background, Color.Black, 0.6f)
)

class AccentTrio(val accent: Color, val highlight: Color, val secondary: Color)

fun ColorScheme.toAccentTrio(): AccentTrio = AccentTrio(
    accent = primary,
    highlight = tertiary,
    secondary = lerp(primary, secondary, 0.55f)
)
