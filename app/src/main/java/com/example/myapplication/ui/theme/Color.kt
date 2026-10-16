package com.example.myapplication.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

// ═════════════════════════════════════════════════════════════════════════════
//  COVERPLAY COLOR SYSTEM (v9) — one source of truth: the active AppTheme
//
//  Every colour that used to be a hard-coded "Iris blue" constant (IconSky,
//  IconPeriwinkle, BrandAccent, BrandGradient, BgBase, TextPrimary …) is now a
//  live getter backed by [LiveTheme]. MyApplicationTheme pushes the active
//  theme + mode into LiveTheme before any content composes, so legacy call
//  sites everywhere in the app recolour automatically — no blue can leak.
//
//  Per-theme palettes themselves are built in Theme.kt (AppTheme.palette()).
// ═════════════════════════════════════════════════════════════════════════════

/** Snapshot-state holder for the active theme's roles. Reads recompose automatically. */
object LiveTheme {
    var accent by mutableStateOf(Color(0xFF4660F0))
    var highlight by mutableStateOf(Color(0xFFA9BCFF))
    var secondary by mutableStateOf(Color(0xFF00C2D1))
    var palette by mutableStateOf(AppTheme.IRIS_ROYAL.palette(ThemeMode.DARK))

    /** Pushes a theme/mode in. Only writes on change so it never invalidates needlessly. */
    fun sync(theme: AppTheme, mode: ThemeMode) {
        val a = theme.accentFor(mode)
        val h = theme.highlightFor(mode)
        val s = theme.secondaryFor(mode)
        val p = theme.palette(mode)
        if (accent != a) accent = a
        if (highlight != h) highlight = h
        if (secondary != s) secondary = s
        if (palette !== p) palette = p
    }
}

// ── Theme-derived "icon" hues (names kept so existing call sites compile) ───
val IconPeriwinkle: Color get() = LiveTheme.highlight                                        // light petal / sheen
val IconIndigo: Color get() = LiveTheme.accent                                               // petal core
val IconRoyalBlue: Color get() = lerp(LiveTheme.accent, Color.Black, 0.45f)                  // deep petal shadow
val IconSky: Color get() = lerp(LiveTheme.accent, LiveTheme.highlight, 0.35f)                // mid petal
val IconWhite = Color(0xFFFFFFFF)

// ── Brand roles ─────────────────────────────────────────────────────────────
val BrandPrimary: Color get() = LiveTheme.accent
val BrandSecondary: Color get() = IconSky
val BrandTertiary: Color get() = LiveTheme.secondary
val BrandAccent: Color get() = LiveTheme.highlight
val BrandMagenta: Color get() = LiveTheme.secondary
val BrandOrange = Color(0xFFFF8C3B)   // 4K / quality accent (semantic, theme independent)
val BrandViolet = Color(0xFF8A6BFF)

val BrandBlue: Color get() = BrandPrimary
val BrandRed = Color(0xFFFF4D5E)      // error / destructive (semantic)
val BrandHighlight = Color(0xFFFFFFFF)
val BrandIceBlue: Color get() = lerp(Color.White, LiveTheme.highlight, 0.22f)
val BrandGold = Color(0xFFFFC64D)
val BrandGoldLight = Color(0xFFFFF0CE)
val BrandCyan: Color get() = LiveTheme.secondary

// ── Brand gradients (follow the active theme) ───────────────────────────────
val BrandGradient: Brush
    get() = Brush.linearGradient(listOf(IconSky, IconIndigo, IconPeriwinkle))
val BrandGradientSoft: Brush
    get() = Brush.linearGradient(listOf(IconSky.copy(alpha = 0.85f), IconIndigo.copy(alpha = 0.85f)))
val BrandGradientRadial: Brush
    get() = Brush.radialGradient(listOf(IconPeriwinkle, IconIndigo, IconRoyalBlue))

// ── Current-mode aliases (live, palette-backed) ─────────────────────────────
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
val BrandBlueShadow: Color get() = LiveTheme.accent.copy(alpha = 0.33f)

val InverseSurface = Color(0xFF16161A)
val InverseOnSurface = Color(0xFFF4F4F6)
val InversePrimary: Color get() = IconSky

val ShadowAmbient = Color.Black.copy(alpha = 0.55f)
val ShadowSpot = Color.Black.copy(alpha = 0.45f)

/** Bundles every role Theme.kt needs so each ThemeMode is a single object literal. */
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
    /** Opaque theme-tinted base for glass panes (cards, nav, chips). */
    val glassBase: Color = bg,
    /** Dark theme-tinted base for glass that sits on top of video frames. */
    val videoGlass: Color = Color(0xFF0A0A0A)
)