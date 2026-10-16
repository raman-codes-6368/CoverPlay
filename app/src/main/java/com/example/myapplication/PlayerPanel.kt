package com.example.myapplication.ui.theme

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import com.example.myapplication.QuipLine
import com.example.myapplication.Quips
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R

// ═════════════════════════════════════════════════════════════════════════════
//  PLAYER PANEL — the ONE window every player screen uses.
//
//  Landscape: slides in from the RIGHT edge, fixed width, full height.
//  Portrait : slides up from the BOTTOM, full width.
//  It lives in the player's own composition (no Dialog / no new window), so the
//  video keeps playing, un-blurred, underneath a light edge scrim.
// ═════════════════════════════════════════════════════════════════════════════

private val PanelInk = Color(0xFF080A14)

@Composable
fun PlayerPanel(
    visible: Boolean,
    onDismiss: () -> Unit,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    /** false → content gets a bounded, non-scrolling slot (for content that has its own LazyColumn / scroll). */
    scrollable: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    val settings = LocalAppUiSettings.current
    val accent = settings.accent
    val hi = settings.highlight
    val cfg = LocalConfiguration.current
    val landscape = cfg.orientation == Configuration.ORIENTATION_LANDSCAPE

    // Compose hidden first, flip on next frame → the entrance animation always plays.
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(visible) { shown = visible }
    BackHandler(enabled = visible, onBack = onDismiss)

    Box(modifier.fillMaxSize()) {
        // light edge scrim — the video stays visible and keeps playing
        AnimatedVisibility(shown, enter = fadeIn(tween(240)), exit = fadeOut(tween(200))) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        if (landscape) Brush.horizontalGradient(listOf(Color.Black.copy(0.10f), Color.Black.copy(0.55f)))
                        else Brush.verticalGradient(listOf(Color.Black.copy(0.08f), Color.Black.copy(0.55f)))
                    )
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onDismiss() }
            )
        }

        val ease = tween<androidx.compose.ui.unit.IntOffset>(340, easing = FastOutSlowInEasing)
        AnimatedVisibility(
            visible = shown,
            modifier = Modifier.align(if (landscape) Alignment.CenterEnd else Alignment.BottomCenter),
            enter = (if (landscape) slideInHorizontally(ease) { it } else slideInVertically(ease) { it }) + fadeIn(tween(200)),
            exit = (if (landscape) slideOutHorizontally(tween(260)) { it } else slideOutVertically(tween(260)) { it }) + fadeOut(tween(200))
        ) {
            val shape = if (landscape) RoundedCornerShape(topStart = 22.dp, bottomStart = 22.dp)
            else RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)
            val panelSize = if (landscape) Modifier.width((cfg.screenWidthDp * 0.30f).coerceIn(280f, 340f).dp).fillMaxHeight()
            else Modifier.fillMaxWidth().heightIn(max = (cfg.screenHeightDp * 0.6f).dp)
            Box(
                panelSize
                    .shadow(24.dp, shape, ambientColor = accent.copy(alpha = GlowSpec.AMBIENT), spotColor = accent.copy(alpha = GlowSpec.SPOT))
                    .clip(shape)
                    .background(
                        Brush.linearGradient(
                            listOf(
                                lerp(PanelInk, accent, 0.46f).copy(alpha = 0.97f),
                                lerp(PanelInk, accent, 0.16f).copy(alpha = 0.97f),
                                PanelInk.copy(alpha = 0.98f)
                            )
                        )
                    )
                    .drawBehind {
                        val r1 = size.width * 0.9f
                        val c1 = Offset(size.width, 0f)
                        drawCircle(Brush.radialGradient(listOf(hi.copy(alpha = 0.26f), Color.Transparent), center = c1, radius = r1), r1, c1)
                        val r2 = size.width * 0.8f
                        val c2 = Offset(0f, size.height)
                        drawCircle(Brush.radialGradient(listOf(accent.copy(alpha = 0.22f), Color.Transparent), center = c2, radius = r2), r2, c2)
                    }
                    .border(1.dp, Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.30f), Color.White.copy(alpha = 0.04f))), shape)
                    // swallow taps so they never reach the scrim / video gestures
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { }
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .then(if (landscape) Modifier.fillMaxHeight() else Modifier.navigationBarsPadding())
                ) {
                    if (!landscape) {
                        Box(Modifier.fillMaxWidth().padding(top = 10.dp), contentAlignment = Alignment.Center) {
                            Box(Modifier.size(40.dp, 4.dp).background(Color.White.copy(alpha = 0.28f), CircleShape))
                        }
                    }
                    if (title.isNotBlank()) {
                        Row(
                            Modifier.fillMaxWidth().padding(start = 16.dp, end = 12.dp, top = if (landscape) 12.dp else 8.dp, bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(title, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, fontSize = 17.sp))
                                if (!subtitle.isNullOrBlank()) {
                                    Text(subtitle, color = Color.White.copy(alpha = 0.55f), maxLines = 1, overflow = TextOverflow.Ellipsis,
                                        style = MaterialTheme.typography.bodySmall)
                                } else {
                                    QuipLine(Quips.dialog(title), color = Color.White.copy(alpha = 0.45f))
                                }
                            }
                            PremiumDialogCloseButton(onClick = onDismiss)
                        }
                        Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(1.dp).background(Color.White.copy(alpha = 0.08f)))
                    } else Spacer(Modifier.height(8.dp))

                    if (scrollable) {
                        Column(
                            Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState())
                                .padding(horizontal = 12.dp).padding(bottom = 12.dp),
                            content = content
                        )
                    } else {
                        Column(Modifier.weight(1f, fill = false).padding(horizontal = 12.dp).padding(top = 8.dp, bottom = 12.dp), content = content)
                    }
                }
            }
        }
    }
}

// ── building blocks ──────────────────────────────────────────────────────────

@Composable
fun PanelSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxWidth().padding(top = 12.dp)) {
        Text(
            title, color = Color.White.copy(alpha = 0.85f),
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 12.5.sp),
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
        )
        content()
    }
}

/** The long rounded card every option row lives in. */
@Composable
fun PanelCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.09f), Color.White.copy(alpha = 0.045f))))
            .border(1.dp, Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.15f), Color.White.copy(alpha = 0.03f))), shape),
        content = content
    )
}

@Composable
fun PanelDivider() {
    Box(Modifier.fillMaxWidth().padding(start = 52.dp, end = 12.dp).height(1.dp).background(Color.White.copy(alpha = 0.07f)))
}

@Composable
private fun PanelIconTile(icon: Painter, active: Boolean) {
    val s = LocalAppUiSettings.current
    val a by animateFloatAsState(if (active) 1f else 0f, spring(0.7f, 450f), label = "tile")
    Box(
        Modifier
            .size(30.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        lerp(Color.White.copy(0.09f), s.accent.copy(0.92f), a),
                        lerp(Color.White.copy(0.06f), s.accent.copy(0.92f), a)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, tint = lerp(Color.White.copy(0.85f), Color.White, a), modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun PanelRow(
    icon: Painter?,
    title: String,
    subtitle: String?,
    active: Boolean,
    onClick: () -> Unit,
    trailing: @Composable () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val press by animateFloatAsState(if (pressed) 1f else 0f, tween(120), label = "rowPress")
    val haptic = rememberHaptic()
    Row(
        Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.07f * press))
            .clickable(interactionSource = interaction, indication = null) { haptic(); onClick() }
            .heightIn(min = 46.dp)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (icon != null) PanelIconTile(icon, active)
        Column(Modifier.weight(1f)) {
            Text(title, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 13.sp))
            if (!subtitle.isNullOrBlank()) {
                Text(subtitle, color = Color.White.copy(alpha = 0.55f), maxLines = 1, overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.5.sp, lineHeight = 13.sp))
            }
        }
        trailing()
    }
}

/** Row that opens something / cycles a value. [value] shows muted on the right. */
@Composable
fun PanelNavRow(
    icon: Painter?,
    title: String,
    value: String? = null,
    active: Boolean = false,
    showChevron: Boolean = true,
    onClick: () -> Unit
) {
    val hi = LocalAppUiSettings.current.highlight
    PanelRow(icon, title, null, active, onClick) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            if (value != null) {
                Text(value, color = if (active) hi else Color.White.copy(alpha = 0.55f), maxLines = 1, overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium),
                    modifier = Modifier.widthIn(max = 110.dp))
            }
            if (showChevron) {
                Icon(painterResource(R.drawable.ic_chevron_right_custom), null, tint = Color.White.copy(alpha = 0.4f), modifier = Modifier.size(15.dp))
            }
        }
    }
}

@Composable
fun PanelToggleRow(
    icon: Painter?,
    title: String,
    checked: Boolean,
    subtitle: String? = null,
    onCheckedChange: (Boolean) -> Unit
) {
    PanelRow(icon, title, subtitle, checked, { onCheckedChange(!checked) }) { PremiumSwitch(checked, null, compact = true) }
}

/** Stand-alone selectable card (audio / subtitle tracks). Selected = accent fill, rim and check badge. */
@Composable
fun PanelOptionRow(title: String, subtitle: String? = null, selected: Boolean, onClick: () -> Unit) {
    val s = LocalAppUiSettings.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val press by animateFloatAsState(if (pressed) 0.97f else 1f, spring(0.6f, 500f), label = "optPress")
    val a by animateFloatAsState(if (selected) 1f else 0f, spring(0.7f, 420f), label = "optSel")
    val haptic = rememberHaptic()
    val shape = RoundedCornerShape(14.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = press; scaleY = press }
            .clip(shape)
            .background(
                Brush.horizontalGradient(
                    listOf(lerp(Color.White.copy(0.08f), s.accent.copy(0.58f), a), lerp(Color.White.copy(0.05f), s.accent.copy(0.22f), a))
                )
            )
            .border(1.dp, lerp(Color.White.copy(0.12f), s.highlight.copy(0.75f), a), shape)
            .clickable(interactionSource = interaction, indication = null) { haptic(); onClick() }
            .heightIn(min = 48.dp)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold, fontSize = 13.sp))
            if (!subtitle.isNullOrBlank()) {
                Text(subtitle, color = Color.White.copy(alpha = 0.6f), maxLines = 1, style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.5.sp, lineHeight = 13.sp))
            }
        }
        Box(
            Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.9f * a))
                .border(1.5.dp, lerp(Color.White.copy(0.28f), Color.Transparent, a), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (a > 0.05f) Icon(painterResource(R.drawable.ic_check_custom), null, tint = s.accent, modifier = Modifier.size(12.dp).graphicsLayer { alpha = a })
        }
    }
}