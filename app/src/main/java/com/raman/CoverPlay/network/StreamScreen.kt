package com.raman.CoverPlay.network

import android.content.ClipboardManager
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.raman.CoverPlay.R
import com.raman.CoverPlay.VideoFile
import com.raman.CoverPlay.components.navContentBottomPadding
import com.raman.CoverPlay.settings.CoverPlayUsageStatsSection
import com.raman.CoverPlay.ui.theme.AppChrome
import com.raman.CoverPlay.ui.theme.CoverPlayBrandIconContainer
import com.raman.CoverPlay.ui.theme.CoverPlayButton
import com.raman.CoverPlay.ui.theme.CoverPlayDimens
import com.raman.CoverPlay.ui.theme.CoverPlayScrollAwareTopBar
import com.raman.CoverPlay.ui.theme.CoverPlaySectionHeader
import com.raman.CoverPlay.ui.theme.CoverPlaySolidCard
import com.raman.CoverPlay.ui.theme.CoverPlaySpacingTokens
import com.raman.CoverPlay.ui.theme.CoverPlayTokens
import com.raman.CoverPlay.ui.theme.LocalAppUiSettings
import com.raman.CoverPlay.ui.theme.chromeFade
import com.raman.CoverPlay.ui.theme.glassSource
import com.raman.CoverPlay.ui.theme.screenTitle
import com.raman.CoverPlay.util.rememberCoverPlayHaptic

@Composable
fun CoverPlayStreamScreen(
    onPlayUrl: (String) -> Unit,
    networkNav: NetworkNav,
    onPlayNetwork: (VideoFile, List<VideoFile>, String) -> Unit,
) {
    val settings = LocalAppUiSettings.current
    val listState = rememberLazyListState()
    val isScrolled by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0 }
    }
    val network = rememberCoverPlayNetworkController(networkNav, onPlayNetwork)
    BackHandler(enabled = network.isBrowsing) { network.goUp() }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CoverPlayScrollAwareTopBar(visible = AppChrome.visible || !isScrolled) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .chromeFade(scrolled = isScrolled)
                        .statusBarsPadding()
                        .padding(horizontal = CoverPlaySpacingTokens.screenHorizontal, vertical = 14.dp)
                ) {
                    Text(
                        "Stream",
                        style = MaterialTheme.typography.screenTitle,
                        color = settings.palette.textPrimary
                    )
                }
            }
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier.glassSource().fillMaxSize(),
            contentPadding = PaddingValues(top = padding.calculateTopPadding(), bottom = navContentBottomPadding())
        ) {
            item(key = "stream_hero") {
                Column {
                    CoverPlaySectionHeader(title = "Stream URL", subtitle = "Paste a link and play it right here", topPadding = 8.dp)
                    Box(Modifier.padding(horizontal = CoverPlayDimens.screenHorizontal)) {
                        CoverPlayStreamHero(onPlayUrl = onPlayUrl)
                    }
                }
            }
            item(key = "stream_network_header") {
                CoverPlaySectionHeader(
                    title = "Network",
                    subtitle = if (network.isBrowsing) "Browsing a server" else "SMB, FTP and WebDAV servers"
                )
            }
            networkSection(network)
            item(key = "stream_stats") {
                Column {
                    CoverPlaySectionHeader(title = "Stats", subtitle = "Your watching at a glance")
                    CoverPlayUsageStatsSection()
                }
            }
        }
    }
}

@Composable
private fun CoverPlayStreamHero(onPlayUrl: (String) -> Unit) {
    val s = LocalAppUiSettings.current
    val p = s.palette
    val context = LocalContext.current
    val clipboard = remember { context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager }
    var url by remember { mutableStateOf("") }
    val canPlay = url.isNotBlank()
    val submit = { if (canPlay) onPlayUrl(normalizeStreamUrl(url)) }
    CoverPlaySolidCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(CoverPlayTokens.Radius.largeCard)) {
        Column(Modifier.fillMaxWidth().padding(CoverPlayDimens.cardPadding)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CoverPlayBrandIconContainer(active = true, size = CoverPlayDimens.iconContainer) {
                    Icon(painterResource(R.drawable.ic_url_custom), null, tint = s.accent, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text("Stream a link", style = MaterialTheme.typography.titleLarge, color = p.textPrimary)
                    Text(
                        "Watch without downloading",
                        style = MaterialTheme.typography.bodySmall,
                        color = p.textSecondary
                    )
                }
            }
            Spacer(Modifier.height(14.dp))

            val inputShape = RoundedCornerShape(CoverPlayTokens.Radius.input)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(CoverPlayDimens.buttonHeight)
                    .clip(inputShape)
                    .background(p.containerHigh)
                    .border(1.dp, p.outlineFaint, inputShape)
                    .padding(start = 16.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = url,
                    onValueChange = { url = it },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = p.textPrimary),
                    cursorBrush = SolidColor(s.accent),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Go),
                    keyboardActions = KeyboardActions(onGo = { submit() }),
                    decorationBox = { inner ->
                        Box(contentAlignment = Alignment.CenterStart) {
                            if (url.isEmpty()) {
                                Text(
                                    "Paste or type a video link",
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
                val pasteHaptic = rememberCoverPlayHaptic()
                Box(
                    Modifier
                        .height(36.dp)
                        .clip(CircleShape)
                        .background(androidx.compose.ui.graphics.lerp(p.containerHigh, s.accent, 0.22f))
                        .clickable {
                            pasteHaptic()
                            if (url.isEmpty()) {
                                val clip = clipboard.primaryClip
                                if (clip != null && clip.itemCount > 0) {
                                    url = clip.getItemAt(0).coerceToText(context).toString().trim()
                                }
                            } else url = ""
                        }
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (url.isEmpty()) "Paste" else "Clear",
                        style = MaterialTheme.typography.labelLarge,
                        color = s.accent
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("HTTP", "HLS", "DASH", "RTSP").forEach { kind ->
                    Text(
                        kind,
                        style = MaterialTheme.typography.labelSmall,
                        color = p.textSecondary,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(p.containerHigh)
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            CoverPlayButton(
                text = "Play",
                onClick = { submit() },
                enabled = canPlay,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
