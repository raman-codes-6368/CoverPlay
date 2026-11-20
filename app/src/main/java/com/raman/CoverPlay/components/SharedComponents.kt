package com.raman.CoverPlay.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.raman.CoverPlay.R
import com.raman.CoverPlay.ui.theme.CoverPlayBrandIconContainer
import com.raman.CoverPlay.ui.theme.CoverPlayDimens
import com.raman.CoverPlay.ui.theme.CoverPlayTokens
import com.raman.CoverPlay.ui.theme.LocalAppUiSettings
import com.raman.CoverPlay.ui.theme.aliveClickable
import com.raman.CoverPlay.ui.theme.floating
import com.raman.CoverPlay.util.rememberCoverPlayHaptic

@Composable
fun CoverPlaySettingsRow(
    icon: Painter,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    active: Boolean = false,
    iconTint: Color = Color.Unspecified,
    titleColor: Color = Color.Unspecified,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null
) {
    val ui = LocalAppUiSettings.current
    val haptic = rememberCoverPlayHaptic()
    val tint = if (iconTint == Color.Unspecified) ui.accent else iconTint
    val titleTint = if (titleColor == Color.Unspecified) ui.palette.textPrimary else titleColor
    val clickable = if (onClick != null) Modifier.aliveClickable(pressedScale = 0.985f) { haptic(); onClick() } else Modifier
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(clickable)
            .heightIn(min = CoverPlayDimens.settingsRowHeight)
            .padding(horizontal = CoverPlayDimens.cardPadding, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CoverPlayBrandIconContainer(active = active, size = CoverPlayDimens.iconContainer) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = titleTint,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (!subtitle.isNullOrEmpty()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = ui.palette.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
        if (trailing != null) {
            Spacer(Modifier.width(12.dp))
            trailing()
        }
    }
}

@Composable
fun CoverPlayRowChevron() {
    Icon(
        painter = painterResource(R.drawable.ic_chevron_right_custom),
        contentDescription = null,
        tint = LocalAppUiSettings.current.palette.textTertiary,
        modifier = Modifier.size(20.dp)
    )
}

@Composable
fun CoverPlayRowValue(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = LocalAppUiSettings.current.accent,
        maxLines = 1
    )
}

@Composable
fun CoverPlaySurfaceGroup(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val ui = LocalAppUiSettings.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(com.raman.CoverPlay.ui.theme.CoverPlayTokens.Radius.largeCard))
            .background(ui.palette.container)
    ) { content() }
}

@Composable
fun navContentBottomPadding(): androidx.compose.ui.unit.Dp {
    val inset = WindowInsets.navigationBars
        .asPaddingValues().calculateBottomPadding()
    val floating = LocalAppUiSettings.current.floatingNav
    return CoverPlayDimens.navHeight + inset + (if (floating) 12.dp else 0.dp) + 16.dp
}
