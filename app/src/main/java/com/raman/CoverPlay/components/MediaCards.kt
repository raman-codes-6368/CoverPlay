package com.raman.CoverPlay.components

import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.videoFrameMillis
import com.raman.CoverPlay.CoverPlayTriangleThumbPlaceholder
import com.raman.CoverPlay.R
import com.raman.CoverPlay.VideoFile
import com.raman.CoverPlay.formatDuration
import com.raman.CoverPlay.screens.cardTitle
import com.raman.CoverPlay.ui.theme.CoverPlayButton
import com.raman.CoverPlay.ui.theme.CoverPlayMediaPill
import com.raman.CoverPlay.ui.theme.CoverPlayNewBadge
import com.raman.CoverPlay.ui.theme.CoverPlayProgressBar
import com.raman.CoverPlay.ui.theme.CoverPlayResolutionBadge
import com.raman.CoverPlay.ui.theme.LocalAppUiSettings
import com.raman.CoverPlay.ui.theme.skeleton
import com.raman.CoverPlay.ui.theme.staggerIn
import com.raman.CoverPlay.util.rememberCoverPlayHaptic

private val CardRadius = 24.dp
private val CardRadiusCompact = 20.dp

@Composable
private fun cardFill(): Color {
    val p = LocalAppUiSettings.current.palette
    return p.containerHigh.copy(alpha = if (p.isDark) 0.62f else 0.92f)
}

private fun savedPosition(context: Context, video: VideoFile): Long =
    context.getSharedPreferences("playback_positions", Context.MODE_PRIVATE).getLong(video.path, 0L)

private fun watchedFraction(video: VideoFile, savedPos: Long): Float =
    if (video.duration > 0 && savedPos > 0) (savedPos.toFloat() / video.duration.toFloat()).coerceIn(0.02f, 1f) else 0f

@Composable
private fun CoverPlayVideoThumb(
    video: VideoFile,
    modifier: Modifier,
    shape: Shape,
    overlay: @Composable BoxScope.() -> Unit = {}
) {
    val settings = LocalAppUiSettings.current
    val context = LocalContext.current
    var ready by remember(video.path) { mutableStateOf(false) }
    Box(
        modifier = modifier
            .clip(shape)
            .background(settings.palette.containerLowest)
            .skeleton(active = !ready && !settings.hideThumbnails)
    ) {
        if (settings.hideThumbnails) {
            CoverPlayTriangleThumbPlaceholder(modifier = Modifier.fillMaxSize())
        } else {
            AsyncImage(
                model = remember(video.uri) {
                    ImageRequest.Builder(context).data(video.uri).videoFrameMillis(1000).crossfade(true).build()
                },
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                onSuccess = { ready = true }
            )
        }
        overlay()
    }
}

@Composable
internal fun CoverPlayVideoCardAction(
    painter: Painter,
    description: String,
    onClick: () -> Unit,
    tint: Color = LocalAppUiSettings.current.palette.textSecondary,
    size: Dp = 36.dp
) {
    val settings = LocalAppUiSettings.current
    val haptic = rememberCoverPlayHaptic()
    val source = remember { MutableInteractionSource() }
    Box(
        Modifier
            .size(size)
            .clip(CircleShape)
            .background(if (settings.palette.isDark) Color.White.copy(alpha = 0.07f) else Color.Black.copy(alpha = 0.05f))
            .clickable(interactionSource = source, indication = null) { haptic(); onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(painter, description, tint = tint, modifier = Modifier.size(size * 0.5f))
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CoverPlayVideoCardRow(
    video: VideoFile,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: () -> Unit = {},
    trailing: (@Composable () -> Unit)? = null
) {
    val settings = LocalAppUiSettings.current
    val context = LocalContext.current
    val tap = rememberCoverPlayHaptic()
    val longTap = rememberCoverPlayHaptic(HapticFeedbackType.LongPress)
    val compact = settings.compactList
    val savedPos = remember(video.path) { savedPosition(context, video) }
    val progress = watchedFraction(video, savedPos)
    val shape = RoundedCornerShape(if (compact) CardRadiusCompact else CardRadius)
    val thumbW = if (compact) 96.dp else 124.dp
    val thumbH = if (compact) 56.dp else 72.dp

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .staggerIn(0)
            .clip(shape)
            .background(cardFill())
            .combinedClickable(
                onClick = { tap(); onClick() },
                onLongClick = { longTap(); onLongClick() }
            )
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CoverPlayVideoThumb(
            video = video,
            modifier = Modifier.size(thumbW, thumbH),
            shape = RoundedCornerShape(if (compact) 14.dp else 16.dp)
        ) {
            if (settings.showDurationBadge) {
                CoverPlayMediaPill(
                    formatDuration(video.duration),
                    Modifier.align(Alignment.BottomEnd).padding(6.dp).padding(bottom = if (progress > 0f) 3.dp else 0.dp)
                )
            }
            if (progress > 0f) CoverPlayProgressBar(progress, Modifier.align(Alignment.BottomCenter), height = 3.dp)
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
            Text(
                text = remember(video.path, video.name, video.dateAdded) { cardTitle(video) },
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = settings.palette.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CoverPlayResolutionBadge(video.width, video.height, onMedia = false)
                if (progress == 0f) CoverPlayNewBadge(onMedia = false)
            }
        }
        Spacer(Modifier.width(8.dp))
        if (trailing != null) trailing() else {
            CoverPlayVideoCardAction(rememberVectorPainter(Icons.Rounded.MoreVert), "File operations", onClick = onLongClick)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CoverPlayVideoCardGrid(
    video: VideoFile,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: () -> Unit = {}
) {
    val settings = LocalAppUiSettings.current
    val context = LocalContext.current
    val tap = rememberCoverPlayHaptic()
    val longTap = rememberCoverPlayHaptic(HapticFeedbackType.LongPress)
    val savedPos = remember(video.path) { savedPosition(context, video) }
    val progress = watchedFraction(video, savedPos)
    val shape = RoundedCornerShape(CardRadius)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .staggerIn(0)
            .clip(shape)
            .background(cardFill())
            .combinedClickable(
                onClick = { tap(); onClick() },
                onLongClick = { longTap(); onLongClick() }
            )
            .padding(8.dp)
    ) {
        CoverPlayVideoThumb(
            video = video,
            modifier = Modifier.fillMaxWidth().aspectRatio(16 / 9f),
            shape = RoundedCornerShape(18.dp)
        ) {
            Box(
                Modifier.fillMaxSize().background(
                    Brush.verticalGradient(0f to Color.Black.copy(alpha = 0.18f), 0.4f to Color.Transparent, 1f to Color.Black.copy(alpha = 0.42f))
                )
            )
            Row(Modifier.fillMaxWidth().align(Alignment.TopStart).padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                CoverPlayResolutionBadge(video.width, video.height)
                Spacer(Modifier.weight(1f))
                if (savedPos == 0L) CoverPlayNewBadge()
            }
            if (settings.showDurationBadge) {
                CoverPlayMediaPill(
                    formatDuration(video.duration),
                    Modifier.align(Alignment.BottomEnd).padding(8.dp).padding(bottom = if (progress > 0f) 3.dp else 0.dp)
                )
            }
            if (progress > 0f) CoverPlayProgressBar(progress, Modifier.align(Alignment.BottomCenter), height = 3.dp)
        }
        Column(Modifier.padding(start = 6.dp, end = 6.dp, top = 10.dp, bottom = 4.dp)) {
            Text(
                text = remember(video.path, video.name, video.dateAdded) { cardTitle(video) },
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = settings.palette.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun CoverPlayPlaylistStack(
    thumbVideo: VideoFile?,
    count: Int,
    modifier: Modifier,
    thumbHeight: Dp,
    corner: Dp
) {
    val settings = LocalAppUiSettings.current
    val accent = settings.accent
    val peek = 10.dp
    Box(modifier.height(thumbHeight + peek)) {
        Box(
            Modifier.align(Alignment.TopCenter).fillMaxWidth(0.70f).height(thumbHeight)
                .clip(RoundedCornerShape(corner)).background(accent.copy(alpha = 0.16f))
        )
        Box(
            Modifier.align(Alignment.TopCenter).padding(top = 5.dp).fillMaxWidth(0.85f).height(thumbHeight)
                .clip(RoundedCornerShape(corner)).background(accent.copy(alpha = 0.28f))
        )
        val shape = RoundedCornerShape(corner)
        if (thumbVideo != null) {
            CoverPlayVideoThumb(
                video = thumbVideo,
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(thumbHeight),
                shape = shape
            ) {
                Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.16f)))
                CoverPlayMediaPill(
                    "$count",
                    Modifier.align(Alignment.BottomEnd).padding(6.dp)
                )
            }
        } else {
            Box(
                Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(thumbHeight)
                    .clip(shape).background(androidx.compose.ui.graphics.lerp(settings.palette.containerHighest, accent, 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(painterResource(R.drawable.ic_playlist_custom), null, tint = accent, modifier = Modifier.size(28.dp))
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CoverPlayPlaylistCardRow(
    name: String,
    count: Int,
    thumbVideo: VideoFile?,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val settings = LocalAppUiSettings.current
    val tap = rememberCoverPlayHaptic()
    val longTap = rememberCoverPlayHaptic(HapticFeedbackType.LongPress)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .staggerIn(0)
            .clip(RoundedCornerShape(CardRadius))
            .background(cardFill())
            .combinedClickable(onClick = { tap(); onClick() }, onLongClick = { longTap(); onDelete() })
            .padding(start = 10.dp, end = 10.dp, top = 10.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CoverPlayPlaylistStack(thumbVideo, count, Modifier.width(124.dp), thumbHeight = 64.dp, corner = 14.dp)
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = settings.palette.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = if (count == 1) "1 video" else "$count videos",
                style = MaterialTheme.typography.labelMedium,
                color = settings.palette.textSecondary
            )
        }
        Spacer(Modifier.width(8.dp))
        CoverPlayVideoCardAction(
            painter = painterResource(R.drawable.ic_trash_custom),
            description = "Delete playlist",
            onClick = onDelete,
            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.85f)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CoverPlayPlaylistCardGrid(
    name: String,
    count: Int,
    thumbVideo: VideoFile?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: () -> Unit = {}
) {
    val settings = LocalAppUiSettings.current
    val tap = rememberCoverPlayHaptic()
    val longTap = rememberCoverPlayHaptic(HapticFeedbackType.LongPress)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .staggerIn(0)
            .clip(RoundedCornerShape(CardRadius))
            .background(cardFill())
            .combinedClickable(onClick = { tap(); onClick() }, onLongClick = { longTap(); onLongClick() })
            .padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 10.dp)
    ) {
        CoverPlayPlaylistStack(thumbVideo, count, Modifier.fillMaxWidth(), thumbHeight = 84.dp, corner = 16.dp)
        Column(Modifier.padding(start = 2.dp, end = 2.dp, top = 10.dp)) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = settings.palette.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = if (count == 1) "1 video" else "$count videos",
                style = MaterialTheme.typography.labelMedium,
                color = settings.palette.textSecondary
            )
        }
    }
}

@Composable
fun CoverPlayPlaylistEmptyCard(
    title: String,
    message: String,
    actionLabel: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    val settings = LocalAppUiSettings.current
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 40.dp, vertical = 24.dp)
        ) {
            CoverPlayPlaylistStack(null, 0, Modifier.width(150.dp), thumbHeight = 84.dp, corner = 18.dp)
            Spacer(Modifier.height(24.dp))
            Text(
                title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                color = settings.palette.textPrimary
            )
            Spacer(Modifier.height(8.dp))
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                color = settings.palette.textSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))
            com.raman.CoverPlay.ui.theme.CoverPlayButton(text = actionLabel, onClick = onAction)
        }
    }
}
