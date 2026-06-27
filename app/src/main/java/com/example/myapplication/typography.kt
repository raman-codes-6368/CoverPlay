package com.example.myapplication.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.myapplication.R

// ── Plus Jakarta Sans Font Family ───────────────────────────────────────────
// Download Plus Jakarta Sans from Google Fonts (it's open-source / OFL).
// Place the following .ttf files in res/font/ (rename to snake_case):
//
//   plus_jakarta_sans_extralight.ttf   (200)
//   plus_jakarta_sans_light.ttf        (300)
//   plus_jakarta_sans_regular.ttf      (400)
//   plus_jakarta_sans_medium.ttf       (500)
//   plus_jakarta_sans_semibold.ttf     (600)
//   plus_jakarta_sans_bold.ttf         (700)
//   plus_jakarta_sans_extrabold.ttf    (800)
//
// Unlike Inclusive Sans, Plus Jakarta Sans ships all weights natively, so
// every FontWeight slot gets its real cut — no faking, no synthesis artefacts.
// The hierarchy below leverages this: Display/Headline text uses ExtraBold (800)
// for commanding visual presence, Titles use Bold/SemiBold, body reads cleanly
// in Regular, and Labels stay Medium for legibility at small sizes.
// ─────────────────────────────────────────────────────────────────────────────
val PlusJakartaSans = FontFamily(
    Font(R.font.plus_jakarta_sans_extralight, weight = FontWeight.ExtraLight),
    Font(R.font.plus_jakarta_sans_light,      weight = FontWeight.Light),
    Font(R.font.plus_jakarta_sans_regular,    weight = FontWeight.Normal),
    Font(R.font.plus_jakarta_sans_medium,     weight = FontWeight.Medium),
    Font(R.font.plus_jakarta_sans_semibold,   weight = FontWeight.SemiBold),
    Font(R.font.plus_jakarta_sans_bold,       weight = FontWeight.Bold),
    Font(R.font.plus_jakarta_sans_extrabold,  weight = FontWeight.ExtraBold),
)

// ── Typography Definition ───────────────────────────────────────────────────
// Hierarchy strategy:
//   Displays  → ExtraBold (800), tight tracking — dominant hero labels
//   Headlines → Bold (700), slightly open — strong section anchors
//   Titles    → SemiBold (600), neutral tracking — card/list titles
//   Body      → Regular (400), readable rhythm — descriptions, metadata
//   Labels    → Medium (500), wide tracking — badges, tabs, chips, overlines
//
// Plus Jakarta Sans is a geometric humanist sans — it reads confidently at
// large sizes and cleanly at small ones, making it ideal for a media player
// UI that needs to look premium at every scale (thumbnail chips → full-screen
// overlays).
// ─────────────────────────────────────────────────────────────────────────────

val CinematicTypography = Typography(

    // ── Display ─────────────────────────────────────────────────────────────
    // Used for splash hero text, full-screen title overlays.
    // ExtraBold + tight tracking = cinematic punch.
    displayLarge = TextStyle(
        fontFamily    = PlusJakartaSans,
        fontWeight    = FontWeight.ExtraBold,
        fontSize      = 56.sp,
        lineHeight    = 60.sp,
        letterSpacing = (-1.0).sp
    ),
    displayMedium = TextStyle(
        fontFamily    = PlusJakartaSans,
        fontWeight    = FontWeight.ExtraBold,
        fontSize      = 44.sp,
        lineHeight    = 48.sp,
        letterSpacing = (-0.75).sp
    ),
    displaySmall = TextStyle(
        fontFamily    = PlusJakartaSans,
        fontWeight    = FontWeight.ExtraBold,
        fontSize      = 36.sp,
        lineHeight    = 40.sp,
        letterSpacing = (-0.5).sp
    ),

    // ── Headline ────────────────────────────────────────────────────────────
    // Screen-level section titles, folder names, playlist headers.
    // Bold keeps authority without the heavy mass of ExtraBold at this size.
    headlineLarge = TextStyle(
        fontFamily    = PlusJakartaSans,
        fontWeight    = FontWeight.Bold,
        fontSize      = 30.sp,
        lineHeight    = 36.sp,
        letterSpacing = (-0.5).sp
    ),
    headlineMedium = TextStyle(
        fontFamily    = PlusJakartaSans,
        fontWeight    = FontWeight.Bold,
        fontSize      = 26.sp,
        lineHeight    = 32.sp,
        letterSpacing = (-0.3).sp
    ),
    headlineSmall = TextStyle(
        fontFamily    = PlusJakartaSans,
        fontWeight    = FontWeight.Bold,
        fontSize      = 22.sp,
        lineHeight    = 28.sp,
        letterSpacing = (-0.2).sp
    ),

    // ── Title ───────────────────────────────────────────────────────────────
    // Video card titles, bottom-sheet headers, dialog titles.
    // SemiBold feels authoritative without competing with Headline above it.
    titleLarge = TextStyle(
        fontFamily    = PlusJakartaSans,
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 20.sp,
        lineHeight    = 26.sp,
        letterSpacing = (-0.15).sp
    ),
    titleMedium = TextStyle(
        fontFamily    = PlusJakartaSans,
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 16.sp,
        lineHeight    = 22.sp,
        letterSpacing = 0.sp
    ),
    titleSmall = TextStyle(
        fontFamily    = PlusJakartaSans,
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 14.sp,
        lineHeight    = 20.sp,
        letterSpacing = 0.sp
    ),

    // ── Body ────────────────────────────────────────────────────────────────
    // File metadata, settings descriptions, playlist item subtitles.
    // Regular weight at comfortable leading — designed for sustained reading.
    bodyLarge = TextStyle(
        fontFamily    = PlusJakartaSans,
        fontWeight    = FontWeight.Normal,
        fontSize      = 16.sp,
        lineHeight    = 24.sp,
        letterSpacing = 0.15.sp
    ),
    bodyMedium = TextStyle(
        fontFamily    = PlusJakartaSans,
        fontWeight    = FontWeight.Normal,
        fontSize      = 14.sp,
        lineHeight    = 20.sp,
        letterSpacing = 0.1.sp
    ),
    bodySmall = TextStyle(
        fontFamily    = PlusJakartaSans,
        fontWeight    = FontWeight.Normal,
        fontSize      = 12.sp,
        lineHeight    = 17.sp,
        letterSpacing = 0.2.sp
    ),

    // ── Label ───────────────────────────────────────────────────────────────
    // Navigation tabs, chips, icon button labels, form field labels.
    // Medium weight + wide tracking gives premium small-text legibility.
    labelLarge = TextStyle(
        fontFamily    = PlusJakartaSans,
        fontWeight    = FontWeight.Medium,
        fontSize      = 14.sp,
        lineHeight    = 20.sp,
        letterSpacing = 0.4.sp
    ),
    labelMedium = TextStyle(
        fontFamily    = PlusJakartaSans,
        fontWeight    = FontWeight.Medium,
        fontSize      = 12.sp,
        lineHeight    = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily    = PlusJakartaSans,
        fontWeight    = FontWeight.Medium,
        fontSize      = 11.sp,
        lineHeight    = 15.sp,
        letterSpacing = 0.6.sp
    )
)

// ── Custom Extensions ────────────────────────────────────────────────────────

/**
 * All-caps overline above a section (e.g. "RECENTLY PLAYED", "ALL VIDEOS").
 * SemiBold + wide tracking for a premium badge-like quality at tiny sizes.
 */
val Typography.sectionOverline: TextStyle
    get() = TextStyle(
        fontFamily    = PlusJakartaSans,
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 11.sp,
        lineHeight    = 16.sp,
        letterSpacing = 1.5.sp
    )

/**
 * Duration / resolution chip inside thumbnails (e.g. "4K · 1:24:30").
 * Bold at 10sp ensures it punches through even on busy thumbnails.
 */
val Typography.captionBadge: TextStyle
    get() = TextStyle(
        fontFamily    = PlusJakartaSans,
        fontWeight    = FontWeight.Bold,
        fontSize      = 10.sp,
        lineHeight    = 13.sp,
        letterSpacing = 0.3.sp
    )

/**
 * Splash screen tagline — ExtraBold + extreme tracking for a cinematic,
 * wordmark-like presence at the opening moment of the app.
 */
val Typography.splashTagline: TextStyle
    get() = TextStyle(
        fontFamily    = PlusJakartaSans,
        fontWeight    = FontWeight.ExtraBold,
        fontSize      = 13.sp,
        lineHeight    = 18.sp,
        letterSpacing = 4.0.sp
    )

/**
 * Seekbar time codes — Medium weight with tabular-feel tracking keeps
 * digits stable as they change during playback.
 * Kept at 11sp so the chips sit neatly beside the scrubber without
 * overwhelming the player controls.
 */
val Typography.timeCode: TextStyle
    get() = TextStyle(
        fontFamily    = PlusJakartaSans,
        fontWeight    = FontWeight.Medium,
        fontSize      = 11.sp,
        lineHeight    = 14.sp,
        letterSpacing = 0.4.sp
    )

/**
 * Gesture feedback overlay text (seek ±10s, volume %, brightness %).
 * SemiBold ensures it's instantly legible over video without being heavy.
 */
val Typography.gestureFeedback: TextStyle
    get() = TextStyle(
        fontFamily    = PlusJakartaSans,
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 14.sp,
        lineHeight    = 18.sp,
        letterSpacing = 0.2.sp
    )