package com.example.myapplication.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ─────────────────────────────────────────────────────────────
//  CINEMATIC DARK PALETTE
//  Inspired by: Apple TV, Letterboxd, Arc dark, Linear.
//  Philosophy: deep obsidian base, cool blue undertones,
//  icy aurora-teal accent, platinum highlights. Nothing pure
//  black, nothing pure white — everything tinted for depth.
// ─────────────────────────────────────────────────────────────

// ── Base surfaces (cool obsidian, layered) ──────────────────
val BgBase            = Color(0xFF07090E)   // app background — deep obsidian, slight blue
val SurfaceNavy       = Color(0xFF0C1018)   // bottom sheets, nav bar
val SurfaceRaised     = Color(0xFF141923)   // cards, chips (resting)
val SurfaceRaisedAlt  = Color(0xFF1B2230)   // hovered/elevated cards, thumbnails

// ── Outlines / dividers (low-contrast, never harsh) ─────────
val OutlineFaint      = Color(0x14FFFFFF)   // 8%  white — hairlines
val OutlineMedium     = Color(0x24FFFFFF)   // 14% white — separators

// ── Text ramp (warm-tinted whites, never pure) ──────────────
val TextPrimary       = Color(0xFFF2F4F8)   // headings
val TextSecondary     = Color(0xFFB4BCCC)   // body
val TextTertiary      = Color(0xFF6B7488)   // meta, captions, disabled

// ── Brand accent: aurora teal + platinum highlight ──────────
//  Sampled from the CoverPlay logo triangle gradient:
//  platinum highlight → aurora teal → ice blue → violet whisper.
val BrandCyan         = Color(0xFF7FE3D4)   // primary accent — logo face
val BrandHighlight    = Color(0xFFE8F4FF)   // platinum sweep — logo highlight
val BrandIceBlue      = Color(0xFF6FB8D9)   // mid gradient stop
val BrandVioletWhisper= Color(0xFF8A7FE3)   // soft violet tail — logo shadow edge
val BrandBlue         = Color(0xFF4A7FB8)   // mid-tone UI chrome
val BrandBlueDeep     = Color(0xFF1E3A5F)   // deep navy — logo shadow fill
val BrandBlueShadow   = Color(0x807FE3D4)   // glow halo (50% accent)
val BrandTriangle     = Color(0xFF7FE3D4)   // play-triangle tint = accent

// ── Glass / elevated surfaces ───────────────────────────────
val GlassSurface      = SurfaceRaised.copy(alpha = 0.72f)
val NavGlassSurface   = SurfaceRaised.copy(alpha = 0.88f)

// ── Signature gradients (use sparingly: hero, CTA, focus) ───
val BrandGradient = Brush.linearGradient(
    colors = listOf(
        BrandCyan,
        BrandIceBlue,
        BrandVioletWhisper
    )
)

val BrandGradientSoft = Brush.linearGradient(
    colors = listOf(
        Color(0xFF1B2230),
        Color(0xFF141923)
    )
)
