package com.raman.CoverPlay.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.raman.CoverPlay.ui.theme.CoverPlayDimens
import com.raman.CoverPlay.ui.theme.LocalAppUiSettings
import com.raman.CoverPlay.ui.theme.floating
import com.raman.CoverPlay.ui.theme.glassBlur
import com.raman.CoverPlay.ui.theme.rememberCoverPlayHasLiveBlur
import com.raman.CoverPlay.util.rememberCoverPlayHaptic
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

internal class NavBarItem(
    val label: String,
    val selected: Boolean,
    val onClick: () -> Unit,
    val filledIcon: Int,
    val outlineIcon: Int,
)

@Composable
internal fun CoverPlayAppNavBar(
    items: List<NavBarItem>,
    accent: Color,
    idleTint: Color,
    modifier: Modifier = Modifier,
    labelsVisible: Boolean = true,
    floating: Boolean = false,
    fold: () -> Float = { NavChrome.fold },
) {
    if (items.isEmpty()) return
    val palette = LocalAppUiSettings.current.palette
    val barShape = if (floating) RoundedCornerShape(32.dp) else RectangleShape
    val live = rememberCoverPlayHasLiveBlur()
    val selectedIndex = items.indexOfFirst { it.selected }.coerceAtLeast(0)
    val indicatorColor = androidx.compose.ui.graphics.lerp(palette.container, accent, 0.22f)
    val density = androidx.compose.ui.platform.LocalDensity.current
    val indicatorW = with(density) { 68.dp.toPx() }
    val vPadRest = with(density) { 13.dp.toPx() }
    val vPadFold = with(density) { 8.dp.toPx() }
    val edgeColor = palette.outlineFaint
    val foldNow: () -> Float = { if (floating) fold() else fold().coerceIn(0f, 1f) }

    val left = remember { Animatable(selectedIndex + 0.5f) }
    val right = remember { Animatable(selectedIndex + 0.5f) }
    var lastIndex by remember { mutableStateOf(selectedIndex) }
    LaunchedEffect(selectedIndex) {
        if (selectedIndex == lastIndex) return@LaunchedEffect
        val target = selectedIndex + 0.5f
        val movingRight = target > left.value
        lastIndex = selectedIndex
        launch { left.animateTo(target, spring(dampingRatio = 0.9f, stiffness = if (movingRight) 380f else 700f)) }
        launch { right.animateTo(target, spring(dampingRatio = 0.9f, stiffness = if (movingRight) 700f else 380f)) }
    }

    Box(
        modifier
            .fillMaxWidth()
            .then(if (floating) Modifier.navigationBarsPadding().padding(start = CoverPlayDimens.navHorizontalPadding, end = CoverPlayDimens.navHorizontalPadding, bottom = 12.dp) else Modifier),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            Modifier
                .layout { measurable, constraints ->
                    val frac = if (floating) 1f - 0.36f * foldNow() else 1f
                    val w = (constraints.maxWidth * frac).roundToInt().coerceIn(0, constraints.maxWidth)
                    val p = measurable.measure(constraints.copy(minWidth = w, maxWidth = w))
                    layout(constraints.maxWidth, p.height) { p.placeRelative((constraints.maxWidth - w) / 2, 0) }
                }
                .shadow(
                    elevation = if (floating) 8.dp else 0.dp,
                    shape = barShape,
                    ambientColor = Color.Black.copy(alpha = 0.20f),
                    spotColor = Color.Black.copy(alpha = 0.25f)
                )
                .clip(barShape)
                .background(palette.container)
                .then(
                    if (live) Modifier.glassBlur(20.dp, palette.container.copy(alpha = 0.90f))
                    else Modifier
                )
                .then(
                    if (floating) Modifier.border(1.dp, edgeColor, barShape)
                    else Modifier.drawBehind { drawRect(edgeColor, size = Size(size.width, 1.dp.toPx())) }
                )
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .then(if (floating) Modifier else Modifier.navigationBarsPadding())
                    .padding(horizontal = 8.dp)
                    .layout { measurable, constraints ->
                        val f = foldNow()
                        val vp = (vPadRest + (vPadFold - vPadRest) * f).coerceAtLeast(0f).roundToInt()
                        val p = measurable.measure(constraints)
                        layout(p.width, p.height + 2 * vp) { p.placeRelative(0, vp) }
                    }
                    .drawBehind {
                        val n = items.size
                        val tabW = size.width / n
                        val w = minOf(indicatorW, tabW - 8f)
                        val l = left.value * tabW - w / 2f
                        val r = right.value * tabW + w / 2f
                        if (r > l) {
                            val h = 32.dp.toPx()
                            drawRoundRect(
                                color = indicatorColor,
                                topLeft = Offset(l, 0f),
                                size = Size(r - l, h),
                                cornerRadius = CornerRadius(h / 2f, h / 2f)
                            )
                        }
                    }
                    .selectableGroup(),
                verticalAlignment = Alignment.Top
            ) {
                items.forEach { item ->
                    CoverPlayNavBarTab(
                        item = item,
                        selectedTint = accent,
                        idleTint = idleTint,
                        labelsVisible = labelsVisible,
                        fold = foldNow,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun CoverPlayNavBarTab(
    item: NavBarItem,
    selectedTint: Color,
    idleTint: Color,
    labelsVisible: Boolean,
    fold: () -> Float,
    modifier: Modifier = Modifier,
) {
    val haptic = rememberCoverPlayHaptic()
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()

    val press by animateFloatAsState(if (pressed) 0.95f else 1f, androidx.compose.animation.core.tween(120), label = "navPress")
    val tint by animateColorAsState(if (item.selected) selectedTint else idleTint, androidx.compose.animation.core.tween(200), label = "navTint")

    Column(
        modifier
            .selectable(
                selected = item.selected,
                interactionSource = interaction,
                indication = null,
                role = Role.Tab,
                onClick = { haptic(); item.onClick() }
            )
            .graphicsLayer { scaleX = press; scaleY = press },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(Modifier.fillMaxWidth().height(32.dp), contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(if (item.selected) item.filledIcon else item.outlineIcon),
                contentDescription = item.label,
                tint = tint,
                modifier = Modifier.size(24.dp)
            )
        }
        if (labelsVisible) {
            Text(
                text = item.label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (item.selected) FontWeight.SemiBold else FontWeight.Medium,
                color = tint,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .layout { measurable, constraints ->
                        val p = measurable.measure(constraints)
                        val h = (p.height * (1f - fold())).coerceIn(0f, p.height * 1.25f).roundToInt()
                        layout(p.width, h) { p.placeRelative(0, 0) }
                    }
                    .clipToBounds()
                    .graphicsLayer { alpha = (1f - fold() * 1.6f).coerceIn(0f, 1f) }
                    .padding(top = 4.dp)
            )
        }
    }
}
