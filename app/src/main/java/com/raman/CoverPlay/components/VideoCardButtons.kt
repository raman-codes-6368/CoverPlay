package com.raman.CoverPlay.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.raman.CoverPlay.R
import com.raman.CoverPlay.ui.theme.LocalAppUiSettings
import com.raman.CoverPlay.ui.theme.aliveClickable

@Composable
fun CoverPlayTonalButton(
    icon: androidx.compose.ui.graphics.painter.Painter,
    description: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    iconSize: Dp = 20.dp,
) {
    val ui = LocalAppUiSettings.current
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(ui.accent.copy(alpha = if (ui.palette.isDark) 0.16f else 0.12f))
            .aliveClickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, description, tint = ui.accent, modifier = Modifier.size(iconSize))
    }
}

@Composable
fun CoverPlayProgressPlayButton(
    progress: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
) {
    val ui = LocalAppUiSettings.current
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(ui.accent.copy(alpha = if (ui.palette.isDark) 0.16f else 0.12f))
            .aliveClickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (progress > 0f) {
            val accent = ui.accent
            Canvas(Modifier.size(size)) {
                val w = 2.5.dp.toPx()
                val inset = w / 2f + 1.dp.toPx()
                val arc = Size(this.size.width - inset * 2, this.size.height - inset * 2)
                drawArc(accent.copy(alpha = 0.25f), 0f, 360f, false, Offset(inset, inset), arc, style = Stroke(w))
                drawArc(accent, -90f, 360f * progress.coerceIn(0f, 1f), false, Offset(inset, inset), arc, style = Stroke(w, cap = StrokeCap.Round))
            }
        }
        Icon(painterResource(R.drawable.ic_play_custom), "Play", tint = ui.accent, modifier = Modifier.size(size * 0.44f))
    }
}
