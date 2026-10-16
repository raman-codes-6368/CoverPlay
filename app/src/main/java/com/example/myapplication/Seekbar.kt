package com.example.myapplication

import android.content.Context
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.view.TextureView
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.SeekParameters
import com.example.myapplication.ui.theme.LocalAppUiSettings
import com.example.myapplication.ui.theme.rememberHaptic
import kotlinx.coroutines.launch
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.platform.LocalDensity

// ═════════════════════════════════════════════════════════════════════════════
//  CHAPTERS — read embedded chapter markers (MP4 "chpl" and MKV Chapters)
// ═════════════════════════════════════════════════════════════════════════════

data class VideoChapter(val startMs: Long, val title: String)

object ChapterReader {
    fun read(context: Context, uri: Uri): List<VideoChapter> {
        val out = mutableListOf<VideoChapter>()
        runCatching {
            val pfd = context.contentResolver.openFileDescriptor(uri, "r") ?: return emptyList()
            ParcelFileDescriptor.AutoCloseInputStream(pfd).use { ins ->
                val ch = ins.channel
                val size = ch.size()
                val head = ByteBuffer.allocate(4)
                ch.read(head, 0)
                if ((head.get(0).toInt() and 0xFF) == 0x1A && (head.get(1).toInt() and 0xFF) == 0x45) {
                    readMkv(ch, size, out)
                } else {
                    readMp4(ch, size, out)
                }
            }
        }
        return out.filter { it.startMs >= 0L }.sortedBy { it.startMs }.distinctBy { it.startMs }
    }

    // ── MP4 / MOV: moov > udta > chpl ───────────────────────────────────────
    private fun readMp4(ch: FileChannel, size: Long, out: MutableList<VideoChapter>) {
        val moov = findBox(ch, 0L, size, "moov") ?: return
        val udta = findBox(ch, moov.first, moov.second, "udta") ?: return
        val chpl = findBox(ch, udta.first, udta.second, "chpl") ?: return
        var p = chpl.first + 8L // version/flags + reserved
        val count = readBytes(ch, p, 1).get(0).toInt() and 0xFF
        p += 1
        repeat(count) {
            if (p + 9 > chpl.second) return
            val t = readBytes(ch, p, 8).getLong(0)
            p += 8
            val len = readBytes(ch, p, 1).get(0).toInt() and 0xFF
            p += 1
            val title = String(readBytes(ch, p, len).array(), Charsets.UTF_8)
            p += len
            out.add(VideoChapter(t / 10_000L, title))
        }
    }

    private fun findBox(ch: FileChannel, start: Long, end: Long, type: String): Pair<Long, Long>? {
        var p = start
        while (p + 8 <= end) {
            val hdr = readBytes(ch, p, 8)
            var sz = hdr.getInt(0).toLong() and 0xFFFFFFFFL
            val t = String(hdr.array(), 4, 4, Charsets.ISO_8859_1)
            var hs = 8
            if (sz == 1L) { sz = readBytes(ch, p + 8, 8).getLong(0); hs = 16 }
            else if (sz == 0L) sz = end - p
            if (sz < hs) return null
            if (t == type) return Pair(p + hs, minOf(p + sz, end))
            p += sz
        }
        return null
    }

    // ── MKV / WebM: Segment > Chapters > EditionEntry > ChapterAtom ─────────
    private fun readMkv(ch: FileChannel, size: Long, out: MutableList<VideoChapter>) {
        var p = 0L
        while (p < size) {
            val id = readVint(ch, p, true) ?: return
            val sz = readVint(ch, p + id[1], false) ?: return
            val ds = p + id[1] + sz[1]
            val de = if (sz[0] < 0) size else minOf(size, ds + sz[0])
            if (id[0] == 0x18538067L) { walkSegment(ch, ds, de, out); return }
            p = de
        }
    }

    private fun walkSegment(ch: FileChannel, s: Long, e: Long, out: MutableList<VideoChapter>) {
        var found = false
        children(ch, s, e) { id, ds, de ->
            if (!found && id == 0x1043A770L) {
                found = true
                children(ch, ds, de) { eid, eds, ede ->
                    if (eid == 0x45B9L) children(ch, eds, ede) { aid, ads, ade ->
                        if (aid == 0xB6L) parseAtom(ch, ads, ade, out)
                    }
                }
            }
        }
    }

    private fun parseAtom(ch: FileChannel, s: Long, e: Long, out: MutableList<VideoChapter>) {
        var startNs = -1L
        var title = ""
        children(ch, s, e) { id, ds, de ->
            when (id) {
                0x91L -> { startNs = readUInt(ch, ds, (de - ds).toInt()) }
                0x80L -> children(ch, ds, de) { cid, cds, cde ->
                    if (cid == 0x85L && title.isEmpty()) {
                        title = String(readBytes(ch, cds, (cde - cds).toInt().coerceIn(0, 200)).array(), Charsets.UTF_8)
                    }
                }
                0xB6L -> { parseAtom(ch, ds, de, out) }
                else -> {}
            }
        }
        if (startNs >= 0L) out.add(VideoChapter(startNs / 1_000_000L, title))
    }

    private fun children(ch: FileChannel, s: Long, e: Long, f: (id: Long, ds: Long, de: Long) -> Unit) {
        var p = s
        while (p < e) {
            val id = readVint(ch, p, true) ?: return
            val sz = readVint(ch, p + id[1], false) ?: return
            val ds = p + id[1] + sz[1]
            val de = if (sz[0] < 0) e else minOf(e, ds + sz[0])
            f(id[0], ds, de)
            if (de <= p) return
            p = de
        }
    }

    private fun readBytes(ch: FileChannel, pos: Long, n: Int): ByteBuffer {
        val b = ByteBuffer.allocate(maxOf(n, 1))
        var read = 0
        while (read < n) {
            val r = ch.read(b, pos + read)
            if (r <= 0) break
            read += r
        }
        return b
    }

    private fun readUInt(ch: FileChannel, pos: Long, n: Int): Long {
        val b = readBytes(ch, pos, n.coerceIn(1, 8))
        var v = 0L
        for (i in 0 until n.coerceIn(1, 8)) v = (v shl 8) or (b.get(i).toLong() and 0xFF)
        return v
    }

    /** Returns [value, byteLength] or null. keepMarker=true for element IDs. Size -1 = unknown. */
    private fun readVint(ch: FileChannel, pos: Long, keepMarker: Boolean): LongArray? {
        val first = readBytes(ch, pos, 1).get(0).toInt() and 0xFF
        if (first == 0) return null
        val len = Integer.numberOfLeadingZeros(first) - 24 + 1
        val b = readBytes(ch, pos, len)
        var v = if (keepMarker) first.toLong() else (first and ((1 shl (8 - len)) - 1)).toLong()
        for (i in 1 until len) v = (v shl 8) or (b.get(i).toLong() and 0xFF)
        if (!keepMarker && v == (1L shl (7 * len)) - 1L) v = -1L
        return longArrayOf(v, len.toLong())
    }
}

// ═════════════════════════════════════════════════════════════════════════════
//  PREMIUM SEEKBAR — thin groove, chapter dots, live video preview while scrubbing
// ═════════════════════════════════════════════════════════════════════════════

@OptIn(UnstableApi::class)
@Composable
fun PremiumSeekbar(
    position: Long,
    duration: Long,
    videoUri: Uri? = null,
    bufferedPosition: Long = 0L,
    chapters: List<VideoChapter> = emptyList(),
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit,
    onSeek: (Long) -> Unit = {},
    onDragStart: () -> Unit = {},
    onDragEnd: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val settings = LocalAppUiSettings.current
    val accent = settings.accent
    val glowHi = settings.highlight
    val haptic = rememberHaptic(HapticFeedbackType.TextHandleMove)

    var isDragging by remember { mutableStateOf(false) }
    var trackWidthPx by remember { mutableIntStateOf(1) }
    var dragFraction by remember { mutableFloatStateOf(0f) }

    val playFraction = if (duration > 0) (position.toFloat() / duration.toFloat()).coerceIn(0f, 1f) else 0f
    val fraction = if (isDragging) dragFraction else playFraction
    val bufferedFraction = if (duration > 0) (bufferedPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f) else 0f
    val marks = remember(chapters, duration) { chapters.filter { it.startMs > 0L && it.startMs < duration } }

    val trackHeight by animateDpAsState(
        if (isDragging) 4.5.dp else 3.dp,
        spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMedium), label = "trackH"
    )
    val thumbSize by animateDpAsState(
        if (isDragging) 15.dp else 10.dp,
        spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMedium), label = "thumb"
    )
    val dragAnim = animateFloatAsState(
        if (isDragging) 1f else 0f,
        spring(dampingRatio = 0.6f, stiffness = 380f), label = "drag"
    )

    val targetMs = (fraction * duration).toLong().coerceIn(0L, maxOf(0L, duration))
    val chapterIdx = marks.indexOfLast { it.startMs <= targetMs }
    LaunchedEffect(chapterIdx, isDragging) { if (isDragging && marks.isNotEmpty()) haptic() }
    val chapterTitle = if (marks.isEmpty()) null else
        (chapters.lastOrNull { it.startMs <= targetMs }?.title?.takeIf { it.isNotBlank() })

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .onSizeChanged { trackWidthPx = it.width }
            .pointerInput(duration) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    down.consume()
                    isDragging = true
                    onDragStart()
                    val w = trackWidthPx.coerceAtLeast(1)
                    dragFraction = (down.position.x / w).coerceIn(0f, 1f)
                    onValueChange(dragFraction * duration)
                    onSeek((dragFraction * duration).toLong())

                    var lastSeekMs = (dragFraction * duration).toLong()
                    do {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull() ?: break
                        change.consume()
                        val f = (change.position.x / w).coerceIn(0f, 1f)
                        dragFraction = f
                        val ms = (f * duration).toLong()
                        onValueChange(ms.toFloat())
                        if (change.pressed) {
                            if (abs(ms - lastSeekMs) > 40L) { onSeek(ms); lastSeekMs = ms }
                        } else {
                            onSeek(ms)
                            onValueChangeFinished()
                            break
                        }
                    } while (true)

                    isDragging = false
                    onDragEnd()
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxWidth().height(40.dp)) {
            val trackH = trackHeight.toPx()
            val totalW = size.width
            val cy = size.height / 2f
            val top = cy - trackH / 2f
            val rail = CornerRadius(trackH / 2f, trackH / 2f)
            val drag = dragAnim.value
            val activeW = (totalW * fraction).coerceIn(0f, totalW)
            val centre = Offset(activeW, cy)

            // groove
            drawRoundRect(Color.White.copy(alpha = 0.22f), Offset(0f, top), Size(totalW, trackH), rail)
            // buffered
            if (bufferedFraction > fraction) {
                drawRoundRect(Color.White.copy(alpha = 0.30f), Offset(0f, top), Size(totalW * bufferedFraction, trackH), rail)
            }
            // active glow + gradient
            if (activeW > 0.5f) {
                if (drag > 0.01f) {
                    drawRoundRect(
                        accent.copy(alpha = 0.22f * drag),
                        Offset(0f, top - 1.5.dp.toPx() * drag), Size(activeW, trackH + 3.dp.toPx() * drag),
                        CornerRadius((trackH + 3.dp.toPx() * drag) / 2f)
                    )
                }
                drawRoundRect(
                    brush = Brush.horizontalGradient(listOf(accent, glowHi), startX = 0f, endX = activeW.coerceAtLeast(1f)),
                    topLeft = Offset(0f, top), size = Size(activeW, trackH), cornerRadius = rail
                )
            }
            // chapter dots
            marks.forEachIndexed { i, ch ->
                val x = totalW * (ch.startMs.toFloat() / duration.toFloat())
                val near = drag > 0.01f && i == chapterIdx
                val r = (1.8f + 0.8f * drag + if (near) 1.2f * drag else 0f).dp.toPx()
                drawCircle(Color.Black.copy(alpha = 0.35f), r + 1.dp.toPx(), Offset(x, cy))
                drawCircle(if (near) glowHi else Color.White.copy(alpha = if (x <= activeW) 1f else 0.85f), r, Offset(x, cy))
            }
            // thumb
            val tr = thumbSize.toPx() / 2f
            val haloR = tr * (1.8f + 0.35f * drag)
            drawCircle(
                Brush.radialGradient(listOf(accent.copy(alpha = 0.30f + 0.15f * drag), Color.Transparent), center = centre, radius = haloR),
                haloR, centre
            )
            drawCircle(Color.Black.copy(alpha = 0.30f), tr + 1.dp.toPx(), centre)
            drawCircle(Color.White, tr, centre)
            if (drag > 0.01f) drawCircle(accent.copy(alpha = drag), tr - 0.75.dp.toPx(), centre, style = Stroke(1.5.dp.toPx()))
        }

        // ── Live preview card ─────────────────────────────────────────────
        val previewAlpha by animateFloatAsState(if (isDragging) 1f else 0f, tween(180), label = "pvA")
        // Anchored from the bottom of the seekbar box, so the gap to the track stays
        // constant no matter how tall the label under the thumbnail gets.
        val previewY by animateDpAsState(
            if (isDragging) (-36).dp else (-28).dp,
            spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow), label = "pvY"
        )
        if (previewAlpha > 0.01f && videoUri != null) {
            val cardW = 112.dp
            val cardH = 63.dp          // 16:9, compact
            val cardShape = RoundedCornerShape(8.dp)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset {
                        IntOffset(
                            x = (trackWidthPx * fraction - cardW.toPx() / 2f).toInt()
                                .coerceIn(0, (trackWidthPx - cardW.toPx()).toInt().coerceAtLeast(0)),
                            y = previewY.roundToPx()
                        )
                    }
                    .graphicsLayer {
                        alpha = previewAlpha
                        val sc = 0.92f + 0.08f * previewAlpha
                        scaleX = sc; scaleY = sc
                        transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 1f)
                    }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(cardW, cardH)
                            .shadow(10.dp, cardShape, spotColor = Color.Black.copy(alpha = 0.6f))
                            .clip(cardShape)
                            .background(Color.Black)
                            .border(
                                1.dp,
                                Brush.linearGradient(listOf(Color.White.copy(alpha = 0.55f), accent.copy(alpha = 0.55f))),
                                cardShape
                            )
                    ) {
                        SeekPreviewFrame(videoUri, targetMs, cardW, cardH)
                        // time chip sits inside the thumbnail, bottom-centre
                        Box(
                            Modifier.fillMaxWidth().height(cardH / 2).align(Alignment.BottomCenter)
                                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.65f))))
                        )
                        Text(
                            formatDuration(targetMs),
                            color = Color.White,
                            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 4.dp),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, fontFeatureSettings = "tnum")
                        )
                    }
                    if (chapterTitle != null) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            chapterTitle,
                            color = glowHi,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier
                                .widthIn(max = cardW + 40.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.Black.copy(alpha = 0.55f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

/** A tiny paused ExoPlayer that seeks to [targetMs]: shows the real frame for any playable format. */
@OptIn(UnstableApi::class)
@Composable
private fun SeekPreviewFrame(videoUri: Uri, targetMs: Long, w: androidx.compose.ui.unit.Dp, h: androidx.compose.ui.unit.Dp) {
    val context = LocalContext.current
    var aspect by remember { mutableFloatStateOf(16f / 9f) }
    val player = remember(videoUri) {
        ExoPlayer.Builder(context, DefaultRenderersFactory(context).setEnableDecoderFallback(true)).build().apply {
            volume = 0f
            setSeekParameters(SeekParameters.CLOSEST_SYNC)
            setMediaItem(MediaItem.fromUri(videoUri))
            playWhenReady = false
            prepare()
        }
    }
    DisposableEffect(player) {
        val l = object : Player.Listener {
            override fun onVideoSizeChanged(videoSize: VideoSize) {
                if (videoSize.width > 0 && videoSize.height > 0) {
                    aspect = videoSize.width * videoSize.pixelWidthHeightRatio / videoSize.height
                }
            }
        }
        player.addListener(l)
        onDispose { player.removeListener(l); player.release() }
    }
    LaunchedEffect(targetMs, player) { player.seekTo(targetMs) }

    // Cover-fit the video inside the card
    val cardA = w.value / h.value
    val dw = if (aspect >= cardA) h * aspect else w
    val dh = if (aspect >= cardA) h else w / aspect
    Box(Modifier.fillMaxSize().clipToBounds(), contentAlignment = Alignment.Center) {
        AndroidView(
            factory = { ctx -> TextureView(ctx).also { player.setVideoTextureView(it) } },
            modifier = Modifier.requiredSize(dw, dh)
        )
    }
}

// ═════════════════════════════════════════════════════════════════════════════
//  GESTURE INDICATOR — slim brightness / volume pill
//
//  Shown on the side OPPOSITE the swipe (brightness → right, volume → left).
//  A small glass pill: icon, hair-thin glowing track, value.
// ═════════════════════════════════════════════════════════════════════════════

@Composable
fun GestureIndicator(visible: Boolean, isBrightness: Boolean, value: Float, modifier: Modifier = Modifier) {
    val settings = LocalAppUiSettings.current
    val accent = settings.accent
    val hi = settings.highlight

    // Latch side + mode while visible so the exit animation never jumps sides.
    var onRight by remember { mutableStateOf(isBrightness) }
    var brightnessMode by remember { mutableStateOf(isBrightness) }
    if (visible) { onRight = isBrightness; brightnessMode = isBrightness }

    // Critically damped spring: tracks the finger tightly but never jitters.
    val animated by animateFloatAsState(value, spring(Spring.DampingRatioNoBouncy, 1100f), label = "gbFill")
    val fill = animated.coerceIn(0f, 1f)
    val boost = (animated - 1f).coerceIn(0f, 1f)
    val percent = (animated * 100).toInt()
    val boostColor = Color(0xFFFF4DA6)
    val pillShape = RoundedCornerShape(22.dp)

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(120)) +
                scaleIn(initialScale = 0.9f, animationSpec = spring(0.8f, 600f)) +
                slideInHorizontally(spring(0.85f, 600f)) { if (onRight) it / 3 else -it / 3 },
        exit = fadeOut(tween(220)) +
                scaleOut(targetScale = 0.94f, animationSpec = tween(220)) +
                slideOutHorizontally(tween(220)) { if (onRight) it / 3 else -it / 3 },
        modifier = modifier
    ) {
        Box(Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .align(if (onRight) Alignment.CenterEnd else Alignment.CenterStart)
                    .padding(horizontal = 22.dp)
                    .clip(pillShape)
                    .background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.46f), Color.Black.copy(alpha = 0.32f))))
                    .border(1.dp, Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.30f), Color.White.copy(alpha = 0.05f))), pillShape)
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedContent(
                    targetState = if (brightnessMode) 0 else if (percent > 0) 1 else 2,
                    transitionSpec = { (fadeIn(tween(140)) + scaleIn(initialScale = 0.6f)).togetherWith(fadeOut(tween(100)) + scaleOut(targetScale = 0.6f)) },
                    label = "gbIcon"
                ) { state ->
                    Icon(
                        painterResource(when (state) { 0 -> R.drawable.ic_brightness_custom; 1 -> R.drawable.ic_volume_custom; else -> R.drawable.ic_volume_off_custom }),
                        null, tint = Color.White, modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(Modifier.height(10.dp))

                Canvas(Modifier.width(8.dp).height(150.dp)) {
                    val w = size.width
                    val h = size.height
                    val corner = CornerRadius(w / 2f)
                    val fh = h * fill
                    val top = h - fh
                    // groove
                    drawRoundRect(Color.White.copy(alpha = 0.16f), cornerRadius = corner)
                    if (fh > 0.5f) {
                        // soft glow
                        val g = 3.dp.toPx()
                        drawRoundRect(
                            accent.copy(alpha = 0.30f),
                            Offset(-g, top - g * 0.5f), Size(w + g * 2f, fh + g),
                            CornerRadius(w / 2f + g)
                        )
                        drawRoundRect(
                            Brush.verticalGradient(listOf(hi, accent), startY = top, endY = h),
                            Offset(0f, top), Size(w, fh), corner
                        )
                    }
                    if (boost > 0f) {
                        val bh = h * boost
                        drawRoundRect(boostColor, Offset(0f, h - bh), Size(w, bh), corner)
                    }
                }

                Spacer(Modifier.height(10.dp))
                Text(
                    "$percent",
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 0.sp, fontFeatureSettings = "tnum"
                    )
                )
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
//  DOUBLE-TAP SEEK FLASH — clean, no boxes, no lines
//
//  • a soft light bloom blooms from the exact tap point and fades
//  • three chevrons light up in sequence in the seek direction
//  • the running total rolls like an odometer (+10 → +20 → +30 …)
// ═════════════════════════════════════════════════════════════════════════════

private class SeekLatch {
    var secs = 0
    var x = 0.5f
    var y = 0.5f
}

@Composable
fun PremiumSeekFlash(
    visible: Boolean,
    token: Int,
    cumulativeSeconds: Int,
    isForward: Boolean,
    modifier: Modifier = Modifier,
    tapXFraction: Float = 0.5f,
    tapYFraction: Float = 0.5f
) {
    val settings = LocalAppUiSettings.current
    val accent = settings.accent
    val density = LocalDensity.current

    val bloom = remember { Animatable(0f) }
    val sweep = remember { Animatable(0f) }
    val pop = remember { Animatable(1f) }
    val nudge = remember { Animatable(0f) }
    val show by animateFloatAsState(
        if (visible) 1f else 0f,
        tween(if (visible) 90 else 240, easing = FastOutSlowInEasing), label = "seekShow"
    )

    val latch = remember { SeekLatch() }
    if (cumulativeSeconds > 0) latch.secs = cumulativeSeconds
    if (visible) { latch.x = tapXFraction; latch.y = tapYFraction }

    val nudgePx = with(density) { 10.dp.toPx() } * (if (isForward) 1f else -1f)

    LaunchedEffect(token, visible) {
        if (token > 0 && visible) {
            kotlinx.coroutines.coroutineScope {
                launch {
                    bloom.animateTo(1f, tween(90, easing = FastOutSlowInEasing))
                    bloom.animateTo(0f, tween(700, easing = FastOutSlowInEasing))
                }
                launch {
                    pop.snapTo(0.92f)
                    pop.animateTo(1f, spring(dampingRatio = 0.5f, stiffness = 520f))
                }
                launch {
                    nudge.snapTo(nudgePx)
                    nudge.animateTo(0f, spring(dampingRatio = 0.6f, stiffness = 420f))
                }
                sweep.snapTo(0f)
                sweep.animateTo(1f, tween(620, easing = LinearEasing))
            }
        } else {
            bloom.animateTo(0f, tween(220))
            sweep.snapTo(0f)
        }
    }

    val active by remember { derivedStateOf { show > 0.001f || bloom.value > 0.001f } }
    if (!active) return

    Box(
        modifier = modifier.fillMaxHeight().fillMaxWidth(0.5f),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val b = bloom.value
            if (b > 0.001f) {
                // soft light bloom at the tap point — filled, edgeless
                val o = Offset(latch.x * size.width, latch.y * size.height)
                val r = size.height * (0.42f + 0.18f * (1f - b))
                drawCircle(
                    Brush.radialGradient(
                        listOf(Color.White.copy(alpha = 0.20f * b), accent.copy(alpha = 0.10f * b), Color.Transparent),
                        center = o, radius = r
                    ),
                    r, o
                )
            }
            // gentle shade under the text so it reads on bright frames
            val c = Offset(size.width / 2f, size.height / 2f)
            val sr = size.minDimension * 0.30f
            drawCircle(
                Brush.radialGradient(listOf(Color.Black.copy(alpha = 0.30f * show), Color.Transparent), center = c, radius = sr),
                sr, c
            )
        }

        Column(
            modifier = Modifier.graphicsLayer {
                val s = pop.value
                scaleX = s; scaleY = s
                translationX = nudge.value
                alpha = show
            },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy((-7).dp), verticalAlignment = Alignment.CenterVertically) {
                for (i in 0 until 3) {
                    val idx = if (isForward) i else 2 - i
                    Icon(
                        painter = painterResource(if (isForward) R.drawable.ic_forward_custom else R.drawable.ic_rewind_custom),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .size(22.dp)
                            .graphicsLayer {
                                val phase = (sweep.value * 1.5f - idx * 0.22f).coerceIn(0f, 1f)
                                val lit = sin(phase * PI.toFloat()).coerceIn(0f, 1f)
                                alpha = 0.30f + 0.70f * lit
                            }
                    )
                }
            }
            Spacer(Modifier.height(2.dp))
            AnimatedContent(
                targetState = latch.secs,
                transitionSpec = {
                    (slideInVertically(spring(0.85f, 700f)) { it / 2 } + fadeIn(tween(110)))
                        .togetherWith(slideOutVertically(tween(140)) { -it / 2 } + fadeOut(tween(100))) using SizeTransform(clip = false)
                },
                label = "seekNum"
            ) { secs ->
                Text(
                    text = (if (isForward) "+" else "−") + secs,
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold, fontSize = 26.sp, letterSpacing = (-0.3).sp,
                        fontFeatureSettings = "tnum",
                        shadow = Shadow(Color.Black.copy(alpha = 0.5f), blurRadius = 12f)
                    )
                )
            }
            Text(
                "SEC",
                color = Color.White.copy(alpha = 0.70f),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 2.sp,
                    shadow = Shadow(Color.Black.copy(alpha = 0.5f), blurRadius = 8f)
                )
            )
        }
    }
}