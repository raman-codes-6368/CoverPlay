package com.raman.CoverPlay.ui.theme

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.raman.CoverPlay.R

enum class CoverPlayDialogButtonStyle { Primary, Secondary, Destructive }

@Composable
fun CoverPlayDialogDivider(modifier: Modifier = Modifier, inset: androidx.compose.ui.unit.Dp = 0.dp) {
    Box(
        modifier
            .fillMaxWidth()
            .padding(start = inset)
            .height(1.dp)
            .background(LocalAppUiSettings.current.palette.outlineFaint)
    )
}

@Composable
fun CoverPlayDialogTitleBar(
    title: String,
    modifier: Modifier = Modifier,
    kicker: String? = null,
    subtitle: String? = null,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    onClose: (() -> Unit)? = null,
) {
    val ui = LocalAppUiSettings.current
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        if (leading != null) {
            leading()
            Spacer(Modifier.width(14.dp))
        }
        Column(Modifier.weight(1f)) {
            if (kicker != null) {
                Text(
                    kicker.uppercase(),
                    style = MaterialTheme.typography.sectionOverline,
                    color = ui.accent
                )
                Spacer(Modifier.height(3.dp))
            }
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                color = ui.palette.textPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle != null) {
                Spacer(Modifier.height(2.dp))
                Text(
                    subtitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = ui.palette.textTertiary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        if (trailing != null) {
            Spacer(Modifier.width(8.dp))
            trailing()
        }
        if (onClose != null) {
            Spacer(Modifier.width(8.dp))
            CoverPlayDialogCloseButton(onClick = onClose)
        }
    }
}

@Composable
fun CoverPlayDialogIconButton(icon: Painter, description: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val ui = LocalAppUiSettings.current
    val shape = RoundedCornerShape(50)
    Box(
        modifier
            .size(32.dp)
            .clip(shape)
            .background(ui.accent.copy(alpha = 0.14f))
            .clickable(role = Role.Button, onClickLabel = description, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, description, tint = ui.accent, modifier = Modifier.size(16.dp))
    }
}

@Composable
fun CoverPlayDialogActionRow(
    icon: Painter,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    description: String? = null,
    destructive: Boolean = false,
    showChevron: Boolean = true,
) {
    val ui = LocalAppUiSettings.current
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(
        if (pressed) 0.98f else 1f, spring(Spring.DampingRatioNoBouncy, Spring.StiffnessMedium), label = "actionRowScale"
    )
    val tone = if (destructive) BrandRed else ui.accent
    val shape = RoundedCornerShape(CoverPlayTokens.Radius.compactCard)
    Row(
        modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(shape)
            .background(if (pressed) LocalAppUiSettings.current.palette.textPrimary.copy(alpha = 0.07f) else Color.Transparent)
            .clickable(interactionSource = source, indication = null, role = Role.Button, onClick = onClick)
            .heightIn(min = 56.dp)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(tone.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = tone, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                label,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = if (destructive) BrandRed else ui.palette.textPrimary,
                maxLines = 1
            )
            if (description != null) {
                Text(
                    description,
                    style = MaterialTheme.typography.labelMedium,
                    color = ui.palette.textTertiary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        if (showChevron) {
            Icon(
                painterResource(R.drawable.ic_chevron_right_custom), null,
                tint = ui.palette.textTertiary.copy(alpha = 0.6f), modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun CoverPlayDialogQuickTile(
    icon: Painter,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val ui = LocalAppUiSettings.current
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(
        if (pressed) 0.94f else 1f, spring(Spring.DampingRatioNoBouncy, Spring.StiffnessMedium), label = "quickTileScale"
    )
    Column(
        modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(RoundedCornerShape(CoverPlayTokens.Radius.compactCard))
            .clickable(interactionSource = source, indication = null, role = Role.Button, onClick = onClick)
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(60.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(ui.palette.containerHighest.copy(alpha = if (pressed) 1f else 0.78f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = ui.accent, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.height(8.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
            color = ui.palette.textSecondary,
            maxLines = 1
        )
    }
}

@Composable
fun CoverPlayDialogInfoRow(icon: Painter, label: String, value: String, showDivider: Boolean = true) {
    val ui = LocalAppUiSettings.current
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(vertical = 11.dp), verticalAlignment = Alignment.Top) {
            Icon(icon, null, tint = ui.accent, modifier = Modifier.padding(top = 2.dp).size(18.dp))
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    label.uppercase(),
                    style = MaterialTheme.typography.sectionOverline,
                    color = ui.palette.textTertiary
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    value,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = ui.palette.textPrimary
                )
            }
        }
        if (showDivider) CoverPlayDialogDivider(inset = 32.dp)
    }
}

@Composable
fun CoverPlayDialogButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: CoverPlayDialogButtonStyle = CoverPlayDialogButtonStyle.Primary,
    enabled: Boolean = true,
) {
    val ui = LocalAppUiSettings.current
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(
        if (pressed) 0.97f else 1f, spring(Spring.DampingRatioNoBouncy, Spring.StiffnessMedium), label = "dialogBtnScale"
    )
    val shape = RoundedCornerShape(CoverPlayTokens.Radius.full)
    val fill: Color
    val content: Color
    when (style) {
        CoverPlayDialogButtonStyle.Primary -> { fill = ui.accent; content = ui.accent.onFill() }
        CoverPlayDialogButtonStyle.Destructive -> { fill = BrandRed; content = BrandRed.onFill() }
        CoverPlayDialogButtonStyle.Secondary -> { fill = ui.palette.containerHighest; content = ui.palette.textPrimary }
    }
    Box(
        modifier
            .heightIn(min = 48.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale; alpha = if (enabled) 1f else 0.45f }
            .clip(shape)
            .background(fill)
            .clickable(interactionSource = source, indication = null, enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = content,
            maxLines = 1
        )
    }
}

fun segmentedShape(
    index: Int,
    count: Int,
    outer: androidx.compose.ui.unit.Dp = 24.dp,
    inner: androidx.compose.ui.unit.Dp = 6.dp,
): androidx.compose.ui.graphics.Shape = when {
    count <= 1 -> RoundedCornerShape(outer)
    index == 0 -> RoundedCornerShape(topStart = outer, topEnd = outer, bottomStart = inner, bottomEnd = inner)
    index == count - 1 -> RoundedCornerShape(topStart = inner, topEnd = inner, bottomStart = outer, bottomEnd = outer)
    else -> RoundedCornerShape(inner)
}

@Composable
fun CoverPlayDialogSegmentButton(
    text: String,
    onClick: () -> Unit,
    index: Int,
    count: Int,
    modifier: Modifier = Modifier,
    style: CoverPlayDialogButtonStyle = CoverPlayDialogButtonStyle.Primary,
) {
    val ui = LocalAppUiSettings.current
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(
        if (pressed) 0.98f else 1f, spring(Spring.DampingRatioNoBouncy, Spring.StiffnessMedium), label = "segBtnScale"
    )
    val (fill, content) = when (style) {
        CoverPlayDialogButtonStyle.Primary -> ui.accent to ui.accent.onFill()
        CoverPlayDialogButtonStyle.Destructive -> BrandRed to BrandRed.onFill()
        CoverPlayDialogButtonStyle.Secondary -> ui.palette.containerHighest to ui.palette.textPrimary
    }
    Box(
        modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(segmentedShape(index, count, outer = 22.dp, inner = 6.dp))
            .background(fill)
            .clickable(interactionSource = source, indication = null, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = content,
            maxLines = 1
        )
    }
}

@Composable
fun CoverPlayDialogButtonRow(
    confirmText: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    cancelText: String = "Cancel",
    confirmStyle: CoverPlayDialogButtonStyle = CoverPlayDialogButtonStyle.Primary,
) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(3.dp)) {
        CoverPlayDialogSegmentButton(confirmText, onConfirm, index = 0, count = 2, style = confirmStyle)
        CoverPlayDialogSegmentButton(cancelText, onCancel, index = 1, count = 2, style = CoverPlayDialogButtonStyle.Secondary)
    }
}

@Composable
fun coverPlayDialogFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = LocalAppUiSettings.current.accent,
    unfocusedBorderColor = Color.Transparent,
    focusedContainerColor = LocalAppUiSettings.current.palette.containerHighest,
    unfocusedContainerColor = LocalAppUiSettings.current.palette.containerHighest,
    cursorColor = LocalAppUiSettings.current.accent,
    focusedTextColor = LocalAppUiSettings.current.palette.textPrimary,
    unfocusedTextColor = LocalAppUiSettings.current.palette.textPrimary,
    focusedLabelColor = LocalAppUiSettings.current.accent,
    unfocusedLabelColor = LocalAppUiSettings.current.palette.textTertiary,
)
