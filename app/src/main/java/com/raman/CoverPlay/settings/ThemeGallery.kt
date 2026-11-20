package com.raman.CoverPlay.settings

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.raman.CoverPlay.ui.theme.CoverPlayDimens
import com.raman.CoverPlay.ui.theme.CoverPlayTokens
import com.raman.CoverPlay.ui.theme.DynamicColorAvailable
import com.raman.CoverPlay.ui.theme.LocalAppUiSettings
import com.raman.CoverPlay.ui.theme.ThemeMode
import com.raman.CoverPlay.ui.theme.ThemePreset
import com.raman.CoverPlay.ui.theme.ThemePresets
import com.raman.CoverPlay.ui.theme.seedColorScheme
import com.raman.CoverPlay.ui.theme.wallpaperColorScheme
import com.raman.CoverPlay.util.rememberCoverPlayHaptic

private val seedSchemeCache = HashMap<Pair<Int, ThemeMode>, ColorScheme>()
private fun cachedSeedScheme(seed: Color, mode: ThemeMode): ColorScheme =
    seedSchemeCache.getOrPut(seed.toArgb() to mode) { seedColorScheme(seed, mode) }

@Composable
fun CoverPlayThemeGallery(
    previewMode: ThemeMode,
    dynamicOn: Boolean,
    seed: Color,
    onPickDynamic: () -> Unit,
    onPickPreset: (ThemePreset) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = rememberCoverPlayHaptic()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = CoverPlayDimens.cardPadding),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (DynamicColorAvailable) {
            val scheme = remember(previewMode) { wallpaperColorScheme(context, previewMode) }
            if (scheme != null) {
                CoverPlayThemePreviewCard(
                    label = "Material You",
                    scheme = scheme,
                    selected = dynamicOn,
                    onClick = { haptic(); onPickDynamic() }
                )
            }
        }
        ThemePresets.forEach { preset ->
            val scheme = remember(preset, previewMode) { cachedSeedScheme(preset.seed, previewMode) }
            CoverPlayThemePreviewCard(
                label = preset.label,
                scheme = scheme,
                selected = !dynamicOn && preset.seed.toArgb() == seed.toArgb(),
                onClick = { haptic(); onPickPreset(preset) }
            )
        }
    }
}

@Composable
private fun CoverPlayThemePreviewCard(
    label: String,
    scheme: ColorScheme,
    selected: Boolean,
    onClick: () -> Unit
) {
    val ui = LocalAppUiSettings.current
    val shape = RoundedCornerShape(CoverPlayTokens.Radius.card)
    val ring by animateColorAsState(
        targetValue = if (selected) ui.accent else ui.palette.outlineFaint,
        animationSpec = androidx.compose.animation.core.tween(200), label = "themeRing"
    )
    val lift by animateFloatAsState(
        targetValue = if (selected) 1f else 0.97f,
        animationSpec = androidx.compose.animation.core.tween(200), label = "themeLift"
    )
    Column(
        modifier = Modifier
            .width(100.dp)
            .graphicsLayer { scaleX = lift; scaleY = lift }
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(136.dp)
                .clip(shape)
                .background(scheme.background)
                .border(if (selected) 2.5.dp else 1.dp, ring, shape)
                .padding(10.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Box(
                    Modifier
                        .fillMaxWidth(0.55f)
                        .height(8.dp)
                        .clip(CircleShape)
                        .background(scheme.onSurface.copy(alpha = 0.55f))
                )
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(34.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(scheme.surfaceContainerHigh)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .weight(1f)
                            .height(16.dp)
                            .clip(CircleShape)
                            .background(scheme.primaryContainer)
                    )
                    Box(Modifier.size(16.dp).clip(CircleShape).background(scheme.tertiary))
                }
            }
            Box(
                Modifier
                    .align(Alignment.BottomEnd)
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(if (selected) scheme.primary else scheme.surfaceContainerHighest),
                contentAlignment = Alignment.Center
            ) {
                if (selected) {
                    Icon(Icons.Rounded.Check, null, tint = scheme.onPrimary, modifier = Modifier.size(16.dp))
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) ui.palette.textPrimary else ui.palette.textSecondary,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
