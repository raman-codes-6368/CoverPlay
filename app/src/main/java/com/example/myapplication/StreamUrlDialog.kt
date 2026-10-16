package com.example.myapplication

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.myapplication.ui.theme.GlassDialogBackdrop
import com.example.myapplication.ui.theme.GlassSurface
import com.example.myapplication.ui.theme.GlowLevel
import com.example.myapplication.ui.theme.IconSky
import com.example.myapplication.ui.theme.LocalAppUiSettings
import com.example.myapplication.ui.theme.LocalGlassBackdrop
import com.example.myapplication.ui.theme.LocalGlassHaze
import com.example.myapplication.ui.theme.PremiumButton
import com.example.myapplication.ui.theme.PremiumDialogCloseButton
import com.example.myapplication.ui.theme.cinematicOverlay
import com.example.myapplication.ui.theme.glassFieldFill
import com.example.myapplication.ui.theme.premiumGlow
import com.example.myapplication.ui.theme.rememberWindowBlurActive
import com.example.myapplication.ui.theme.windowFrost
import kotlinx.coroutines.delay

/** Adds https:// when the user pasted a bare domain, leaves any explicit scheme alone. */
private fun normalizeStreamUrl(raw: String): String {
    val t = raw.trim()
    return if (t.contains("://")) t else "https://$t"
}

/**
 * Stream URL dialog: a compact floating glass card, centred over a blurred + dimmed app.
 * Tapping outside dismisses, the card rides above the keyboard, and the field glows softly
 * while focused (same glow language as switches, sliders and buttons).
 */
@Composable
fun PlayUrlDialog(onDismiss: () -> Unit, onPlay: (String) -> Unit) {
    val context = LocalContext.current
    val clipboard = remember { context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager }
    val ui = LocalAppUiSettings.current
    val accent = ui.accent
    val dark = ui.palette.isDark
    val blurActive = rememberWindowBlurActive()

    var url by remember { mutableStateOf("") }
    val fieldInteraction = remember { MutableInteractionSource() }
    val focused by fieldInteraction.collectIsFocusedAsState()
    val focusAnim by animateFloatAsState(if (focused) 1f else 0f, tween(200), label = "urlFocus")
    val focusRequester = remember { FocusRequester() }
    val canPlay = url.isNotBlank()
    val submit = { if (canPlay) onPlay(normalizeStreamUrl(url)) }

    LaunchedEffect(Unit) {
        delay(280)
        runCatching { focusRequester.requestFocus() }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        GlassDialogBackdrop(blurRadius = 52, dim = if (blurActive) 0.42f else 0.62f)
        CompositionLocalProvider(LocalGlassHaze provides null, LocalGlassBackdrop provides null) {
            var shown by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) { shown = true }
            val p by animateFloatAsState(
                targetValue = if (shown) 1f else 0f,
                animationSpec = spring(dampingRatio = 0.76f, stiffness = 340f), label = "urlIn"
            )
            val shape = RoundedCornerShape(30.dp)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onDismiss() }
                    .imePadding()
                    .padding(horizontal = 22.dp, vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                GlassSurface(
                    modifier = Modifier
                        .widthIn(max = 400.dp)
                        .fillMaxWidth()
                        .graphicsLayer {
                            alpha = p.coerceIn(0f, 1f)
                            val sc = 0.92f + 0.08f * p
                            scaleX = sc; scaleY = sc
                            translationY = (1f - p) * 48f
                        }
                        .premiumGlow(accent, GlowLevel.Subtle, p, corner = 30.dp)
                        .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { },
                    shape = shape,
                    strong = true,
                    tint = Color.Transparent,
                    pressedScale = 1f
                ) {
                    Box(Modifier.matchParentSize().windowFrost(dark, accent, blurActive))
                    Box(Modifier.matchParentSize().cinematicOverlay(accent, ui.highlight, dark))

                    Column(Modifier.padding(start = 22.dp, end = 22.dp, top = 22.dp, bottom = 22.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier
                                    .size(48.dp)
                                    .premiumGlow(accent, GlowLevel.Medium, corner = 16.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Brush.linearGradient(listOf(IconSky, accent)))
                                    .background(Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.30f), Color.Transparent))),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(painterResource(R.drawable.ic_url_custom), null, tint = Color.White, modifier = Modifier.size(22.dp))
                            }
                            Spacer(Modifier.width(14.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    "Stream a link",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                                    color = ui.palette.textPrimary,
                                    maxLines = 1
                                )
                                Text(
                                    "Play a video straight from the network",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ui.palette.textSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            PremiumDialogCloseButton(onClick = onDismiss)
                        }

                        Spacer(Modifier.height(10.dp))
                        QuipLine(Quips.dialog("stream link"))
                        Spacer(Modifier.height(14.dp))

                        val fieldShape = RoundedCornerShape(18.dp)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .premiumGlow(accent, GlowLevel.Subtle, focusAnim, corner = 18.dp)
                                .clip(fieldShape)
                                .background(glassFieldFill())
                                .border(
                                    1.dp,
                                    Brush.linearGradient(
                                        listOf(
                                            accent.copy(alpha = 0.22f + 0.65f * focusAnim),
                                            Color.White.copy(alpha = if (dark) 0.10f else 0.35f)
                                        )
                                    ),
                                    fieldShape
                                )
                                .padding(start = 16.dp, end = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painterResource(R.drawable.ic_url_custom), null,
                                tint = if (focused) accent else ui.palette.textTertiary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            BasicTextField(
                                value = url,
                                onValueChange = { url = it },
                                modifier = Modifier.weight(1f).focusRequester(focusRequester),
                                singleLine = true,
                                interactionSource = fieldInteraction,
                                textStyle = MaterialTheme.typography.bodyLarge.copy(color = ui.palette.textPrimary, fontSize = 15.sp),
                                cursorBrush = SolidColor(accent),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Go),
                                keyboardActions = KeyboardActions(onGo = { submit() }),
                                decorationBox = { inner ->
                                    Box(contentAlignment = Alignment.CenterStart) {
                                        if (url.isEmpty()) {
                                            Text(
                                                "Paste or type a video link",
                                                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
                                                color = ui.palette.textTertiary,
                                                maxLines = 1
                                            )
                                        }
                                        inner()
                                    }
                                }
                            )
                            if (url.isEmpty()) {
                                GlassSurface(
                                    modifier = Modifier.height(38.dp),
                                    shape = CircleShape,
                                    pressedScale = 0.94f,
                                    onClick = {
                                        val clip = clipboard.primaryClip
                                        if (clip != null && clip.itemCount > 0) {
                                            url = clip.getItemAt(0).coerceToText(context).toString().trim()
                                        }
                                    }
                                ) {
                                    Text(
                                        "Paste",
                                        modifier = Modifier.align(Alignment.Center).padding(horizontal = 16.dp),
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                        color = accent
                                    )
                                }
                            } else {
                                GlassSurface(
                                    modifier = Modifier.size(36.dp),
                                    shape = CircleShape,
                                    pressedScale = 0.9f,
                                    onClick = { url = "" }
                                ) {
                                    Icon(
                                        painterResource(R.drawable.ic_close_custom), "Clear",
                                        tint = ui.palette.textSecondary,
                                        modifier = Modifier.size(14.dp).align(Alignment.Center)
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(10.dp))
                        Text(
                            "Works with direct links, HLS (.m3u8) and DASH (.mpd) streams.",
                            modifier = Modifier.padding(horizontal = 4.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = ui.palette.textTertiary
                        )

                        Spacer(Modifier.height(22.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            GlassSurface(
                                modifier = Modifier.weight(1f).height(54.dp),
                                shape = CircleShape,
                                onClick = onDismiss
                            ) {
                                Text(
                                    "Cancel",
                                    modifier = Modifier.align(Alignment.Center),
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = ui.palette.textPrimary
                                )
                            }
                            PremiumButton(
                                text = "Play",
                                onClick = { submit() },
                                enabled = canPlay,
                                modifier = Modifier.weight(1f).height(54.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}