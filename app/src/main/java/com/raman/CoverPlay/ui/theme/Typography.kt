package com.raman.CoverPlay.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp
import com.raman.CoverPlay.R

val GoogleSansFlexRounded = FontFamily(
    Font(R.font.google_sans_flex_rounded_light,     weight = FontWeight.Light),
    Font(R.font.google_sans_flex_rounded_regular,   weight = FontWeight.Normal),
    Font(R.font.google_sans_flex_rounded_medium,    weight = FontWeight.Medium),
    Font(R.font.google_sans_flex_rounded_semibold,  weight = FontWeight.SemiBold),
    Font(R.font.google_sans_flex_rounded_bold,      weight = FontWeight.Bold),
    Font(R.font.google_sans_flex_rounded_extrabold, weight = FontWeight.ExtraBold),
)

val InterFontFamily = FontFamily(
    Font(R.font.inter_regular,  weight = FontWeight.Light),
    Font(R.font.inter_regular,  weight = FontWeight.Normal),
    Font(R.font.inter_medium,   weight = FontWeight.Medium),
    Font(R.font.inter_semibold, weight = FontWeight.SemiBold),
    Font(R.font.inter_semibold, weight = FontWeight.Bold),
    Font(R.font.inter_semibold, weight = FontWeight.ExtraBold),
)

val PlusJakartaFontFamily = FontFamily(
    Font(R.font.plus_jakarta_sans_light,     weight = FontWeight.Light),
    Font(R.font.plus_jakarta_sans_regular,   weight = FontWeight.Normal),
    Font(R.font.plus_jakarta_sans_medium,    weight = FontWeight.Medium),
    Font(R.font.plus_jakarta_sans_semibold,  weight = FontWeight.SemiBold),
    Font(R.font.plus_jakarta_sans_bold,      weight = FontWeight.Bold),
    Font(R.font.plus_jakarta_sans_extrabold, weight = FontWeight.ExtraBold),
)

enum class AppFont(val label: String, val tagline: String, val family: FontFamily) {
    GOOGLE_SANS_FLEX("Google Sans Flex", "Rounded, friendly · default", GoogleSansFlexRounded),
    INTER("Inter", "Neutral and crisp", InterFontFamily),
    PLUS_JAKARTA("Plus Jakarta Sans", "Warm and geometric", PlusJakartaFontFamily),
    SYSTEM("System", "Your phone's font", FontFamily.Default);

    companion object {
        fun fromName(name: String?): AppFont = entries.firstOrNull { it.name == name } ?: GOOGLE_SANS_FLEX
    }
}

val HeadingFontFamily = GoogleSansFlexRounded
val ContentFontFamily = GoogleSansFlexRounded
val SingleAppFontFamily = GoogleSansFlexRounded

private val CenteredLines = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.None
)

private fun role(
    weight: FontWeight,
    size: Int,
    lineHeight: Int,
    tracking: Float,
    tabular: Boolean = false,
    family: FontFamily = GoogleSansFlexRounded
) = TextStyle(
    fontFamily = family,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = tracking.sp,
    lineHeightStyle = CenteredLines,
    fontFeatureSettings = if (tabular) "tnum" else null
)

fun cinematicTypography(font: AppFont? = null): Typography {
    val family = font?.family ?: GoogleSansFlexRounded
    return Typography(
    displayLarge  = role(FontWeight.Medium, 40, 46, -1.0f, tabular = true, family = family),
    displayMedium = role(FontWeight.Medium, 34, 40, -0.8f, tabular = true, family = family),
    displaySmall  = role(FontWeight.Medium, 30, 36, -0.6f, tabular = true, family = family),

    headlineLarge  = role(FontWeight.Bold,     28, 34, -0.5f, family = family),
    headlineMedium = role(FontWeight.SemiBold, 24, 30, -0.3f, family = family),
    headlineSmall  = role(FontWeight.SemiBold, 20, 26, -0.2f, family = family),

    titleLarge  = role(FontWeight.SemiBold, 17, 23, -0.1f, family = family),
    titleMedium = role(FontWeight.Medium,   15, 21, 0.0f, family = family),
    titleSmall  = role(FontWeight.Medium,   14, 20, 0.0f, family = family),

    bodyLarge  = role(FontWeight.Normal, 15, 22, 0.05f, family = family),
    bodyMedium = role(FontWeight.Normal, 14, 20, 0.05f, family = family),
    bodySmall  = role(FontWeight.Normal, 12, 17, 0.1f, family = family),

    labelLarge  = role(FontWeight.SemiBold, 14, 20, 0.1f, family = family),
    labelMedium = role(FontWeight.Medium,   12, 16, 0.2f, family = family),
    labelSmall  = role(FontWeight.Medium,   11, 14, 0.25f, tabular = true, family = family),
)
}

val CinematicTypography: Typography get() = cinematicTypography()

val Typography.labelXs: TextStyle get() = labelSmall
val Typography.labelSm: TextStyle get() = labelSmall
val Typography.bodySm: TextStyle get() = bodySmall
val Typography.bodyMd: TextStyle get() = bodyMedium
val Typography.bodyLg: TextStyle get() = bodyLarge
val Typography.titleSm: TextStyle get() = titleSmall
val Typography.titleMd: TextStyle get() = titleMedium
val Typography.titleLg: TextStyle get() = titleLarge
val Typography.displaySm: TextStyle get() = displaySmall
val Typography.displayMd: TextStyle get() = displayMedium

val Typography.sectionOverline: TextStyle
    get() = labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp, lineHeight = 16.sp)

val Typography.screenTitle: TextStyle
    get() = headlineLarge.copy(letterSpacing = (-0.5).sp)

val Typography.captionBadge: TextStyle
    get() = labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.3.sp)

val Typography.splashTagline: TextStyle
    get() = bodySmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 3.5.sp)

val Typography.timeCode: TextStyle
    get() = labelMedium.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 0.2.sp, fontFeatureSettings = "tnum")

val Typography.gestureFeedback: TextStyle
    get() = labelLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.2.sp, fontFeatureSettings = "tnum")
