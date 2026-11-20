package com.raman.CoverPlay.player

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raman.CoverPlay.R
import com.raman.CoverPlay.components.CoverPlaySwitch
import com.raman.CoverPlay.ui.theme.CoverPlayTokens
import com.raman.CoverPlay.ui.theme.LocalAppUiSettings
import com.raman.CoverPlay.ui.theme.onFill

private object PlayerPanelStyle {
    val surface get() = CoverPlayTokens.Palette.surface
    val card get() = CoverPlayTokens.Palette.elevated
    val raised get() = CoverPlayTokens.Palette.highest
    val hairline get() = CoverPlayTokens.Palette.border
    val textPrimary get() = CoverPlayTokens.Palette.textPrimary
    val textSecondary get() = CoverPlayTokens.Palette.textSecondary
    val textMuted get() = CoverPlayTokens.Palette.textMuted
    val sheetRadius = 24.dp
    val cardRadius = 16.dp
    val rowRadius = 14.dp
}

@Composable
fun CoverPlayPlayerPanel(
    visible: Boolean,
    onDismiss: () -> Unit,
    title: String,
    subtitle: String? = null,
    scrollable: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    if (visible) {
        BackHandler { onDismiss() }
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(200)),
        exit = fadeOut(tween(180))
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onDismiss() }
            )

            val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

            AnimatedVisibility(
                visible = visible,
                enter = if (isLandscape) {
                    slideInHorizontally(animationSpec = tween(280, easing = FastOutSlowInEasing)) { it } + fadeIn(tween(200))
                } else {
                    slideInVertically(animationSpec = tween(280, easing = FastOutSlowInEasing)) { it } + fadeIn(tween(200))
                },
                exit = if (isLandscape) {
                    slideOutHorizontally(animationSpec = tween(220, easing = FastOutSlowInEasing)) { it } + fadeOut(tween(180))
                } else {
                    slideOutVertically(animationSpec = tween(220, easing = FastOutSlowInEasing)) { it } + fadeOut(tween(180))
                },
                modifier = if (isLandscape) {
                    Modifier
                        .align(Alignment.CenterEnd)
                        .fillMaxHeight()
                        .width(310.dp)
                        .widthIn(max = 340.dp)
                } else {
                    Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .fillMaxHeight(0.50f)
                        .wrapContentHeight(Alignment.Bottom)
                }
            ) {
                val panelShape = if (isLandscape) {
                    RoundedCornerShape(topStart = PlayerPanelStyle.sheetRadius, bottomStart = PlayerPanelStyle.sheetRadius)
                } else {
                    RoundedCornerShape(topStart = PlayerPanelStyle.sheetRadius, topEnd = PlayerPanelStyle.sheetRadius)
                }

                Box(
                    modifier = (if (isLandscape) Modifier.fillMaxSize() else Modifier.fillMaxWidth())
                        .clip(panelShape)
                        .background(PlayerPanelStyle.surface)
                ) {
                    Column(
                        modifier = (if (isLandscape) Modifier.fillMaxSize() else Modifier.fillMaxWidth())
                            .navigationBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                                Text(
                                    title,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 18.sp
                                    ),
                                    color = PlayerPanelStyle.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (!subtitle.isNullOrBlank()) {
                                    Text(
                                        subtitle,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 12.sp
                                        ),
                                        color = PlayerPanelStyle.textMuted,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }

                            CoverPlayPlayerPanelCloseButton(onClick = onDismiss)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Column(
                            modifier = Modifier
                                .weight(1f, fill = isLandscape)
                                .then(if (scrollable) Modifier.verticalScroll(rememberScrollState()) else Modifier),
                            content = content
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CoverPlayPlayerPanelCloseButton(onClick: () -> Unit) {
    val haptic = LocalHapticFeedback.current

    Box(
        Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(PlayerPanelStyle.raised)
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painterResource(R.drawable.ic_close_custom),
            "Close",
            tint = PlayerPanelStyle.textSecondary,
            modifier = Modifier.size(14.dp)
        )
    }
}

@Composable
fun CoverPlayPlayerPanelSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(Modifier.fillMaxWidth().padding(top = 12.dp)) {
        Text(
            title.uppercase(),
            color = PlayerPanelStyle.textMuted,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.8.sp,
                fontSize = 11.sp
            ),
            modifier = Modifier.padding(start = 6.dp, bottom = 6.dp)
        )
        content()
    }
}

@Composable
fun CoverPlayPlayerPanelCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(PlayerPanelStyle.cardRadius)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(PlayerPanelStyle.card),
        content = content
    )
}

@Composable
fun CoverPlayPlayerPanelDivider() {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(start = 60.dp, end = 16.dp)
            .height(1.dp)
            .background(PlayerPanelStyle.hairline.copy(alpha = 0.7f))
    )
}

@Composable
fun CoverPlayPlayerPanelChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val accent = LocalAppUiSettings.current.accent
    val haptic = LocalHapticFeedback.current

    val fill by animateColorAsState(
        targetValue = if (selected) accent else PlayerPanelStyle.raised,
        animationSpec = tween(180),
        label = "panelChipFill"
    )

    val ink by animateColorAsState(
        targetValue = if (selected) accent.onFill() else PlayerPanelStyle.textSecondary,
        animationSpec = tween(180),
        label = "panelChipInk"
    )

    Box(
        Modifier
            .clip(RoundedCornerShape(CoverPlayTokens.Radius.full))
            .background(fill)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            }
            .heightIn(min = 34.dp)
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            color = ink,
            maxLines = 1,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                fontFeatureSettings = "tnum"
            )
        )
    }
}

@Composable
private fun CoverPlayPlayerPanelIconTile(icon: Painter, active: Boolean) {
    val accent = LocalAppUiSettings.current.accent

    val fill by animateColorAsState(
        targetValue = if (active) accent.copy(alpha = 0.18f) else PlayerPanelStyle.raised,
        animationSpec = tween(180),
        label = "tileFill"
    )

    val tint by animateColorAsState(
        targetValue = if (active) accent else PlayerPanelStyle.textSecondary,
        animationSpec = tween(180),
        label = "tileTint"
    )

    Box(
        Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(fill),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun CoverPlayPlayerPanelRow(
    icon: Painter?,
    title: String,
    subtitle: String?,
    active: Boolean,
    onClick: () -> Unit,
    trailing: @Composable () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()

    val press by animateFloatAsState(
        targetValue = if (pressed) 1f else 0f,
        animationSpec = tween(100),
        label = "rowPress"
    )

    val haptic = LocalHapticFeedback.current

    Row(
        Modifier
            .fillMaxWidth()
            .background(PlayerPanelStyle.textPrimary.copy(alpha = 0.07f * press))
            .clickable(
                interactionSource = interaction,
                indication = null
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            }
            .heightIn(min = 48.dp)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (icon != null) {
            CoverPlayPlayerPanelIconTile(icon, active)
        }

        Column(Modifier.weight(1f)) {
            Text(
                title,
                color = PlayerPanelStyle.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium
                )
            )

            if (!subtitle.isNullOrBlank()) {
                Text(
                    subtitle,
                    color = PlayerPanelStyle.textMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        lineHeight = 15.sp
                    )
                )
            }
        }

        trailing()
    }
}

@Composable
fun CoverPlayPlayerPanelNavRow(
    icon: Painter?,
    title: String,
    value: String? = null,
    active: Boolean = false,
    showChevron: Boolean = true,
    onClick: () -> Unit
) {
    val accent = LocalAppUiSettings.current.accent

    CoverPlayPlayerPanelRow(icon, title, null, active, onClick) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (value != null) {
                Text(
                    value,
                    color = if (active) accent else PlayerPanelStyle.textMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 13.sp,
                        fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium
                    ),
                    modifier = Modifier.widthIn(max = 110.dp)
                )
            }

            if (showChevron) {
                Icon(
                    painterResource(R.drawable.ic_chevron_right_custom),
                    null,
                    tint = PlayerPanelStyle.textMuted,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun CoverPlayPlayerPanelToggleRow(
    icon: Painter?,
    title: String,
    checked: Boolean,
    subtitle: String? = null,
    onCheckedChange: (Boolean) -> Unit
) {
    CoverPlayPlayerPanelRow(icon, title, subtitle, checked, { onCheckedChange(!checked) }) {
        CoverPlaySwitch(checked, null, compact = true)
    }
}

@Composable
fun CoverPlayPlayerPanelOptionRow(
    title: String,
    subtitle: String? = null,
    selected: Boolean,
    onClick: () -> Unit
) {
    val accent = LocalAppUiSettings.current.accent
    val haptic = LocalHapticFeedback.current

    val fill by animateColorAsState(
        targetValue = if (selected) accent.copy(alpha = 0.22f) else PlayerPanelStyle.card,
        animationSpec = tween(180),
        label = "optFill"
    )

    val shape = RoundedCornerShape(PlayerPanelStyle.rowRadius)

    Row(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(fill)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            }
            .heightIn(min = 52.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                title,
                color = if (selected) PlayerPanelStyle.textPrimary else PlayerPanelStyle.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
                )
            )

            if (!subtitle.isNullOrBlank()) {
                Text(
                    subtitle,
                    color = PlayerPanelStyle.textMuted,
                    maxLines = 1,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        lineHeight = 15.sp
                    )
                )
            }
        }

        if (selected) {
            Box(
                Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(accent),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painterResource(R.drawable.ic_check_custom),
                    null,
                    tint = accent.onFill(),
                    modifier = Modifier.size(12.dp)
                )
            }
        } else {
            Box(
                Modifier
                    .size(20.dp)
                    .border(1.5.dp, PlayerPanelStyle.hairline, CircleShape)
            )
        }
    }
}
