package com.raman.CoverPlay.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.raman.CoverPlay.ui.theme.CoverPlaySolidCard
import com.raman.CoverPlay.ui.theme.CoverPlayTokens
import com.raman.CoverPlay.ui.theme.LocalAppUiSettings
import com.raman.CoverPlay.ui.theme.SurfaceContainerHigh
import com.raman.CoverPlay.ui.theme.TextSecondary
import com.raman.CoverPlay.ui.theme.onFill

@Composable
fun CoverPlayChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leading: (@Composable () -> Unit)? = null
) {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = tween(100),
        label = "chipScale"
    )

    val accentNow = LocalAppUiSettings.current.accent

    val fill by animateColorAsState(
        targetValue = if (selected) accentNow else SurfaceContainerHigh,
        animationSpec = tween(200),
        label = "chipFill"
    )

    val content by animateColorAsState(
        targetValue = if (selected) accentNow.onFill() else TextSecondary,
        animationSpec = tween(200),
        label = "chipContent"
    )

    val haptic = LocalHapticFeedback.current
    val shape = CircleShape

    Row(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
            .background(fill)
            .clickable(
                interactionSource = source,
                indication = null,
                onClick = {
                    haptic.performHapticFeedback(
                        HapticFeedbackType.LongPress
                    )
                    onClick()
                }
            )
            .heightIn(min = 36.dp)
            .padding(horizontal = 16.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leading != null) {
            leading()
            androidx.compose.foundation.layout.Spacer(
                Modifier.padding(start = 6.dp)
            )
        }

        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = content,
            maxLines = 1
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CoverPlaySelectableCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    selected: Boolean = false,
    cornerRadius: Dp = CoverPlayTokens.Radius.card,
    content: @Composable BoxScope.() -> Unit
) {
    CoverPlaySolidCard(
        modifier = modifier,
        shape = RoundedCornerShape(cornerRadius),
        onClick = onClick,
        onLongClick = onLongClick,
        selected = selected,
        content = content
    )
}
