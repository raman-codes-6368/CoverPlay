package com.example.myapplication.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ═════════════════════════════════════════════════════════════════════════════
//  COVERPLAY — "Iris Bloom" Expressive Palette (v8.0)
//
//  Pulled directly from the app icon: the periwinkle→royal-blue flower petals
//  wrapping a white play triangle. Every mode below (Light / Dark / AMOLED)
//  is built around the same two anchor hues so the brand reads identically
//  everywhere, only the canvas around it changes.
// ═════════════════════════════════════════════════════════════════════════════

// ── Icon-derived brand hues ──────────────────────────────────────────────────
val IconPeriwinkle   = Color(0xFF9DB4F5)   // light petal highlight
val IconIndigo       = Color(0xFF3654E0)   // petal core
val IconRoyalBlue    = Color(0xFF1E3AC7)   // deep petal shadow
val IconSky          = Color(0xFF5B7CFA)   // mid petal
val IconWhite        = Color(0xFFFFFFFF)   // play triangle

// ── Brand roles (used across all theme modes) ────────────────────────────────
val BrandPrimary      = IconIndigo
val BrandSecondary    = IconSky
val BrandTertiary     = Color(0xFF00C2D1)   // cyan pop, complements indigo
val BrandAccent       = IconPeriwinkle      // expressive accent for badges/glows
val BrandOrange       = Color(0xFFFF8C3B)   // 4K / quality accent
val BrandMagenta      = Color(0xFFB23BFF)

// Aliases kept for compatibility with existing call sites
val BrandBlue         = BrandPrimary
val BrandRed          = Color(0xFFFF4D5E)
val BrandHighlight    = Color(0xFFFFFFFF)
val BrandIceBlue      = Color(0xFFE7ECFF)
val BrandGold         = Color(0xFFFFC64D)
val BrandGoldLight    = Color(0xFFFFF0CE)
val BrandViolet       = Color(0xFF8A6BFF)
val BrandCyan         = BrandTertiary

// ── Brand gradients (petal → petal, matching icon rotation) ─────────────────
val BrandGradient = Brush.linearGradient(
    colors = listOf(IconSky, IconIndigo, IconPeriwinkle)
)
val BrandGradientSoft = Brush.linearGradient(
    colors = listOf(IconSky.copy(alpha = 0.85f), IconIndigo.copy(alpha = 0.85f))
)
val BrandGradientRadial = Brush.radialGradient(
    colors = listOf(IconPeriwinkle, IconIndigo, IconRoyalBlue)
)

// ═════════════════════════════════════════════════════════════════════════════
//  DARK — "Obsidian Bloom" (default cinematic dark, not pure black)
// ═════════════════════════════════════════════════════════════════════════════
val DarkBgBase                = Color(0xFF0A0B14)
val DarkSurfaceNavy           = Color(0xFF0F1220)
val DarkSurfaceRaised         = Color(0xFF161A2C)
val DarkSurfaceRaisedAlt      = Color(0xFF1E2338)
val DarkSurfaceContainerLowest  = Color(0xFF07080F)
val DarkSurfaceContainerLow     = Color(0xFF0F1220)
val DarkSurfaceContainer        = Color(0xFF151931)
val DarkSurfaceContainerHigh    = Color(0xFF1D2340)
val DarkSurfaceContainerHighest = Color(0xFF262C4D)
val DarkOutlineFaint          = Color(0xFF1C2138)
val DarkOutlineMedium         = Color(0xFF2E3559)
val DarkOutlineBright         = Color(0xFF4A548C)
val DarkTextPrimary           = Color(0xFFF1F2FC)
val DarkTextSecondary         = Color(0xFFBEC3E6)
val DarkTextTertiary          = Color(0xFF8489B3)

// ═════════════════════════════════════════════════════════════════════════════
//  AMOLED — "True Black Bloom" (battery-saving pure black canvas)
// ═════════════════════════════════════════════════════════════════════════════
val AmoledBgBase              = Color(0xFF000000)
val AmoledSurfaceNavy         = Color(0xFF000000)
val AmoledSurfaceRaised       = Color(0xFF0A0A0F)
val AmoledSurfaceRaisedAlt    = Color(0xFF12121C)
val AmoledSurfaceContainerLowest  = Color(0xFF000000)
val AmoledSurfaceContainerLow     = Color(0xFF060609)
val AmoledSurfaceContainer        = Color(0xFF0C0C14)
val AmoledSurfaceContainerHigh    = Color(0xFF14141F)
val AmoledSurfaceContainerHighest = Color(0xFF1C1C2A)
val AmoledOutlineFaint        = Color(0xFF15151F)
val AmoledOutlineMedium       = Color(0xFF23233A)
val AmoledOutlineBright       = Color(0xFF3C3C63)
val AmoledTextPrimary         = Color(0xFFF5F5FF)
val AmoledTextSecondary       = Color(0xFFB8B8DA)
val AmoledTextTertiary        = Color(0xFF7A7AA0)

// ═════════════════════════════════════════════════════════════════════════════
//  LIGHT — "Cloud Bloom" (soft periwinkle-tinted light surfaces)
// ═════════════════════════════════════════════════════════════════════════════
val LightBgBase               = Color(0xFFF6F7FE)
val LightSurfaceNavy          = Color(0xFFFFFFFF)
val LightSurfaceRaised        = Color(0xFFFFFFFF)
val LightSurfaceRaisedAlt     = Color(0xFFEDF0FE)
val LightSurfaceContainerLowest  = Color(0xFFFFFFFF)
val LightSurfaceContainerLow     = Color(0xFFF3F4FD)
val LightSurfaceContainer        = Color(0xFFEDEFFC)
val LightSurfaceContainerHigh    = Color(0xFFE4E7FA)
val LightSurfaceContainerHighest = Color(0xFFD9DDF6)
val LightOutlineFaint         = Color(0xFFE3E5F5)
val LightOutlineMedium        = Color(0xFFCBCFED)
val LightOutlineBright        = Color(0xFFAAB0E6)
val LightTextPrimary          = Color(0xFF14162B)
val LightTextSecondary        = Color(0xFF4A4E76)
val LightTextTertiary         = Color(0xFF7C80A6)

// ── Current-mode aliases (rebound by MyApplicationTheme via CompositionLocal;
//    kept as plain top-level vals too so any code still referencing the old
//    unprefixed names — e.g. BgBase, TextPrimary — compiles against the dark
//    palette by default). ───────────────────────────────────────────────────
val BgBase                    = DarkBgBase
val SurfaceNavy               = DarkSurfaceNavy
val SurfaceRaised             = DarkSurfaceRaised
val SurfaceRaisedAlt          = DarkSurfaceRaisedAlt
val SurfaceContainerLowest    = DarkSurfaceContainerLowest
val SurfaceContainerLow       = DarkSurfaceContainerLow
val SurfaceContainer          = DarkSurfaceContainer
val SurfaceContainerHigh      = DarkSurfaceContainerHigh
val SurfaceContainerHighest   = DarkSurfaceContainerHighest
val OutlineFaint              = DarkOutlineFaint
val OutlineMedium             = DarkOutlineMedium
val OutlineBright             = DarkOutlineBright
val OutlineActive             = BrandPrimary
val TextPrimary               = DarkTextPrimary
val TextSecondary             = DarkTextSecondary
val TextTertiary              = DarkTextTertiary

val BrandBlueDeep             = DarkBgBase
val BrandBlueShadow           = Color(0x552B45E8)

val InverseSurface            = LightTextPrimary
val InverseOnSurface          = LightBgBase
val InversePrimary            = IconSky

val ShadowAmbient             = Color.Black.copy(alpha = 0.55f)
val ShadowSpot                = Color.Black.copy(alpha = 0.45f)

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
    val isDark: Boolean
)

val DarkPalette = PaletteSet(
    bg = DarkBgBase, surfaceNavy = DarkSurfaceNavy, surfaceRaised = DarkSurfaceRaised,
    surfaceRaisedAlt = DarkSurfaceRaisedAlt, containerLowest = DarkSurfaceContainerLowest,
    containerLow = DarkSurfaceContainerLow, container = DarkSurfaceContainer,
    containerHigh = DarkSurfaceContainerHigh, containerHighest = DarkSurfaceContainerHighest,
    outlineFaint = DarkOutlineFaint, outlineMedium = DarkOutlineMedium, outlineBright = DarkOutlineBright,
    textPrimary = DarkTextPrimary, textSecondary = DarkTextSecondary, textTertiary = DarkTextTertiary,
    isDark = true
)

val AmoledPalette = PaletteSet(
    bg = AmoledBgBase, surfaceNavy = AmoledSurfaceNavy, surfaceRaised = AmoledSurfaceRaised,
    surfaceRaisedAlt = AmoledSurfaceRaisedAlt, containerLowest = AmoledSurfaceContainerLowest,
    containerLow = AmoledSurfaceContainerLow, container = AmoledSurfaceContainer,
    containerHigh = AmoledSurfaceContainerHigh, containerHighest = AmoledSurfaceContainerHighest,
    outlineFaint = AmoledOutlineFaint, outlineMedium = AmoledOutlineMedium, outlineBright = AmoledOutlineBright,
    textPrimary = AmoledTextPrimary, textSecondary = AmoledTextSecondary, textTertiary = AmoledTextTertiary,
    isDark = true
)

val LightPalette = PaletteSet(
    bg = LightBgBase, surfaceNavy = LightSurfaceNavy, surfaceRaised = LightSurfaceRaised,
    surfaceRaisedAlt = LightSurfaceRaisedAlt, containerLowest = LightSurfaceContainerLowest,
    containerLow = LightSurfaceContainerLow, container = LightSurfaceContainer,
    containerHigh = LightSurfaceContainerHigh, containerHighest = LightSurfaceContainerHighest,
    outlineFaint = LightOutlineFaint, outlineMedium = LightOutlineMedium, outlineBright = LightOutlineBright,
    textPrimary = LightTextPrimary, textSecondary = LightTextSecondary, textTertiary = LightTextTertiary,
    isDark = false
)