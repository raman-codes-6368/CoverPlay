package com.raman.CoverPlay.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.raman.CoverPlay.R
import com.raman.CoverPlay.util.rememberCoverPlayHaptic

@Composable
fun coverPlayTonalFill(): Color {
    val s = LocalAppUiSettings.current
    return lerp(s.palette.containerHigh, s.accent, if (s.palette.isDark) 0.16f else 0.14f)
}

@Composable
fun CoverPlayTonalSurface(
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClick: () -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    val s = LocalAppUiSettings.current
    val haptic = rememberCoverPlayHaptic()
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val shape = rememberCoverPlayMorphingShape(active = pressed || selected, restPercent = 50f, activePercent = 30f)
    val fill by animateColorAsState(
        targetValue = if (selected) s.accent else coverPlayTonalFill(),
        animationSpec = CoverPlaySprings.effects(),
        label = "tonalFill"
    )
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.94f else 1f,
        animationSpec = CoverPlaySprings.spatial(),
        label = "tonalScale"
    )
    Box(
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(shape)
            .background(fill)
            .clickable(interactionSource = source, indication = null, role = Role.Button) { haptic(); onClick() },
        contentAlignment = Alignment.Center,
        content = content
    )
}

@Composable
fun CoverPlayIconButton(
    painter: Painter,
    description: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 38.dp,
    selected: Boolean = false,
    rotation: Float = 0f
) {
    val s = LocalAppUiSettings.current
    val tint by animateColorAsState(
        targetValue = if (selected) s.accent.onFill() else s.palette.textPrimary,
        animationSpec = CoverPlaySprings.effects(),
        label = "iconTint"
    )
    CoverPlayTonalSurface(modifier = modifier.size(size), selected = selected, onClick = onClick) {
        Icon(
            painter = painter,
            contentDescription = description,
            tint = tint,
            modifier = Modifier.size(size * 0.5f).graphicsLayer { rotationZ = rotation }
        )
    }
}

@Composable
fun CoverPlaySearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search",
    focusRequester: FocusRequester? = null,
    onSearch: () -> Unit = {}
) {
    val s = LocalAppUiSettings.current
    val p = s.palette
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(42.dp)
            .clip(CircleShape)
            .background(coverPlayTonalFill())
            .padding(start = 14.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_search_custom),
            contentDescription = null,
            tint = p.textSecondary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(10.dp))
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .weight(1f)
                .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier),
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = p.textPrimary),
            cursorBrush = SolidColor(s.accent),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearch() }),
            decorationBox = { inner ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (query.isEmpty()) {
                        Text(
                            placeholder,
                            style = MaterialTheme.typography.bodyMedium,
                            color = p.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    inner()
                }
            }
        )
        if (query.isNotEmpty()) {
            val haptic = rememberCoverPlayHaptic()
            Box(
                Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .clickable(role = Role.Button) { haptic(); onQueryChange("") },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_close_custom),
                    contentDescription = "Clear",
                    tint = p.textSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
