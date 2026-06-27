package com.example.myapplication.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ── Animated ambient background ───────────────────────────────────────────────

@Composable
fun CinematicBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    // Pull the current theme preset's background colour so the ambient glows
    // match even after the user switches themes.
    val bg      = MaterialTheme.colorScheme.background
    val primary = MaterialTheme.colorScheme.primary

    val gradient1 = remember(bg) {
        Brush.radialGradient(colors = listOf(BrandBlueDeep.copy(alpha = 0.42f), BrandBlueDeep.copy(alpha = 0.12f), Color.Transparent))
    }
    val gradient2 = remember(primary) {
        Brush.radialGradient(colors = listOf(primary.copy(alpha = 0.10f), Color.Transparent))
    }
    val gradient3 = remember(primary) {
        Brush.radialGradient(colors = listOf(primary.copy(alpha = 0.10f), BrandIceBlue.copy(alpha = 0.04f), Color.Transparent))
    }
    val gradient4 = remember {
        Brush.radialGradient(colors = listOf(BrandBlueDeep.copy(alpha = 0.06f), Color.Transparent))
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bg)           // ← live preset background
    ) {
        Box(modifier = Modifier.align(Alignment.TopStart).offset(x = (-96).dp, y = (-72).dp).size(360.dp).background(gradient1))
        Box(modifier = Modifier.align(Alignment.TopEnd).offset(x = 48.dp, y = 120.dp).size(240.dp).background(gradient2))
        Box(modifier = Modifier.align(Alignment.BottomEnd).offset(x = 72.dp, y = 96.dp).size(320.dp).background(gradient3))
        Box(modifier = Modifier.align(Alignment.Center).size(520.dp).background(gradient4))
        content()
    }
}

// ── Brand text ────────────────────────────────────────────────────────────────

@Composable
fun BrandTitleText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.headlineMedium
) {
    Text(text = text, modifier = modifier, style = style.copy(color = Color.White))
}

// ── Card surface — respects CardStyle from LocalAppUiSettings ─────────────────

@Composable
fun PremiumCardSurface(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    selected: Boolean = false,
    content: @Composable () -> Unit
) {
    val uiSettings = LocalAppUiSettings.current
    val accent      = uiSettings.accent
    val shape       = remember(cornerRadius) { RoundedCornerShape(cornerRadius) }

    when (uiSettings.cardStyle) {

        com.example.myapplication.CardStyle.FLAT -> {
            // Borderless, no elevation — just a very subtle surface tint
            Surface(
                modifier  = modifier,
                shape     = shape,
                color     = if (selected) accent.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface,
                tonalElevation  = 0.dp,
                shadowElevation = 0.dp,
                content   = { Box { content() } }
            )
        }

        com.example.myapplication.CardStyle.OUTLINED -> {
            // Crisp 1dp accent border, transparent fill
            Surface(
                modifier = modifier,
                shape    = shape,
                color    = if (selected) accent.copy(alpha = 0.06f) else Color.Transparent,
                border   = BorderStroke(1.dp, if (selected) accent.copy(alpha = 0.75f) else accent.copy(alpha = 0.30f)),
                tonalElevation  = 0.dp,
                shadowElevation = if (selected) 4.dp else 0.dp,
                content  = { Box { content() } }
            )
        }

        else -> {
            // GLASS (default) — original frosted look using dynamic accent
            val borderBrush = remember(selected, accent) {
                if (selected) Brush.linearGradient(colors = listOf(accent.copy(alpha = 0.65f), BrandIceBlue.copy(alpha = 0.35f)))
                else          Brush.linearGradient(colors = listOf(OutlineMedium, OutlineFaint))
            }
            val bgBrush = remember(selected, accent) {
                if (selected) Brush.linearGradient(colors = listOf(accent.copy(alpha = 0.06f), Color.Transparent))
                else          BrandGradientSoft
            }

            Surface(
                modifier        = modifier,
                shape           = shape,
                color           = if (selected) BrandBlueDeep.copy(alpha = 0.28f) else GlassSurface,
                border          = BorderStroke(1.dp, borderBrush),
                tonalElevation  = 0.dp,
                shadowElevation = if (selected) 8.dp else 0.dp
            ) {
                Box(modifier = Modifier.background(bgBrush)) { content() }
            }
        }
    }
}

// ── Icon container ────────────────────────────────────────────────────────────

@Composable
fun BrandIconContainer(
    modifier: Modifier = Modifier,
    size: Dp = 60.dp,
    cornerRadius: Dp = 18.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val accent = LocalAppUiSettings.current.accent
    val bgBrush = remember(accent) {
        Brush.linearGradient(colors = listOf(accent.copy(alpha = 0.18f), BrandBlueDeep.copy(alpha = 0.22f), BrandVioletWhisper.copy(alpha = 0.08f)))
    }
    val borderBrush = remember(accent) {
        Brush.linearGradient(colors = listOf(accent.copy(alpha = 0.35f), OutlineFaint))
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(cornerRadius))
            .background(bgBrush)
            .border(width = 1.dp, brush = borderBrush, shape = RoundedCornerShape(cornerRadius)),
        contentAlignment = Alignment.Center,
        content = content
    )
}

// ── Duration badge — hidden when showDurationBadge is false ──────────────────

@Composable
fun DurationBadge(
    text: String,
    modifier: Modifier = Modifier
) {
    // Honour the user's "Duration Badge" toggle — if they turned it off, render nothing.
    if (!LocalAppUiSettings.current.showDurationBadge) return

    Surface(
        modifier = modifier,
        color    = Color.Black.copy(alpha = 0.72f),
        shape    = RoundedCornerShape(8.dp),
        border   = BorderStroke(0.5.dp, OutlineFaint)
    ) {
        Text(
            text     = text,
            color    = BrandHighlight,
            style    = MaterialTheme.typography.captionBadge,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
        )
    }
}

// ── Play button overlay ───────────────────────────────────────────────────────

@Composable
fun BrandPlayOverlay(
    modifier: Modifier = Modifier,
    size: Dp = 32.dp
) {
    val accent = LocalAppUiSettings.current.accent
    val bgBrush = remember(accent) {
        Brush.radialGradient(colors = listOf(accent.copy(alpha = 0.28f), BrandBlueDeep.copy(alpha = 0.55f)))
    }
    val borderBrush = remember(accent) {
        Brush.linearGradient(colors = listOf(BrandHighlight.copy(alpha = 0.55f), accent.copy(alpha = 0.35f)))
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(bgBrush)
            .border(width = 1.dp, brush = borderBrush, shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Box(modifier = Modifier.size(size * 0.42f)) {
            com.example.myapplication.BrandTriangle(modifier = Modifier.fillMaxSize())
        }
    }
}