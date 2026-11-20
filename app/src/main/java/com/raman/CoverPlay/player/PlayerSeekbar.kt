package com.raman.CoverPlay.player

import android.content.Context
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.view.TextureView
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.SeekParameters
import com.raman.CoverPlay.formatDuration
import com.raman.CoverPlay.ui.theme.LocalAppUiSettings
import com.raman.CoverPlay.ui.theme.SeekbarStyle
import com.raman.CoverPlay.ui.theme.SeekbarThumb
import com.raman.CoverPlay.util.rememberCoverPlayHaptic
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

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

    private fun readMp4(ch: FileChannel, size: Long, out: MutableList<VideoChapter>) {
        val moov = findBox(ch, 0L, size, "moov") ?: return
        val udta = findBox(ch, moov.first, moov.second, "udta") ?: return
        val chpl = findBox(ch, udta.first, udta.second, "chpl") ?: return
        var p = chpl.first + 8L
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

@OptIn(UnstableApi::class)
@Composable
fun CoverPlaySeekbar(
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
    isPlaying: Boolean = false,
    onSurface: Boolean = false,
    modifier: Modifier = Modifier
) {
    val settings = LocalAppUiSettings.current
    val accent = settings.accent
    val ink = if (onSurface) settings.palette.textPrimary else Color.White
    val haptic = rememberCoverPlayHaptic(HapticFeedbackType.TextHandleMove)

    var isDragging by remember { mutableStateOf(false) }
    var trackWidthPx by remember { mutableIntStateOf(1) }
    var dragFraction by remember { mutableFloatStateOf(0f) }

    val playFraction = if (duration > 0) (position.toFloat() / duration.toFloat()).coerceIn(0f, 1f) else 0f
    val fraction = if (isDragging) dragFraction else playFraction
    val bufferedFraction = if (duration > 0) (bufferedPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f) else 0f
    val marks = remember(chapters, duration) { chapters.filter { it.startMs > 0L && it.startMs < duration } }

    val seekStyle = settings.seekbarStyle
    val seekThumb = if (seekStyle == SeekbarStyle.SQUIGGLY) SeekbarThumb.BAR else settings.seekbarThumb
    val trackHeight by animateDpAsState(
        if (seekStyle == SeekbarStyle.THICK) (if (isDragging) 16.dp else 12.dp)
        else if (seekStyle == SeekbarStyle.SQUIGGLY) (if (isDragging) 4.dp else 5.dp)
        else if (isDragging) 6.dp else 4.dp,
        spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMedium), label = "trackH"
    )
    val thumbSize by animateDpAsState(
        if (isDragging) 18.dp else 12.dp,
        spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMedium), label = "thumb"
    )
    val dragAnim = animateFloatAsState(
        if (isDragging) 1f else 0f,
        spring(dampingRatio = 0.6f, stiffness = 380f), label = "drag"
    )

    val waveAmp by animateFloatAsState(
        targetValue = if (isPlaying && !isDragging && (seekStyle == SeekbarStyle.WAVY || seekStyle == SeekbarStyle.SQUIGGLY)) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 140f), label = "waveAmp"
    )
    val wavePhase = remember { mutableFloatStateOf(0f) }
    val waving = waveAmp > 0.01f
    LaunchedEffect(waving) {
        if (waving) {
            var last = 0L
            val twoPi = 2f * PI.toFloat()
            while (true) {
                withFrameNanos { now ->
                    if (last != 0L) {
                        wavePhase.floatValue = (wavePhase.floatValue + (now - last) / 1_000_000_000f * twoPi * 0.8f) % twoPi
                    }
                    last = now
                }
            }
        }
    }

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
            val rail = if (seekStyle == SeekbarStyle.THICK) CornerRadius(trackH * 0.34f, trackH * 0.34f) else CornerRadius(trackH / 2f, trackH / 2f)
            val drag = dragAnim.value
            val activeW = (totalW * fraction).coerceIn(0f, totalW)
            val centre = Offset(activeW, cy)

            if (seekStyle == SeekbarStyle.DOTTED) {
                val dr = (2.2f + 0.8f * drag).dp.toPx()
                val step = 11.dp.toPx()
                var dx = dr
                while (dx <= totalW - dr + 0.5f) {
                    val c = when {
                        dx <= activeW -> accent
                        dx <= totalW * bufferedFraction -> ink.copy(alpha = 0.40f)
                        else -> ink.copy(alpha = 0.22f)
                    }
                    drawCircle(c, dr, Offset(dx, cy))
                    dx += step
                }
            } else if (seekStyle == SeekbarStyle.SQUIGGLY) {
                val waveLen = 32.dp.toPx()
                val amp = waveAmp * 2.6.dp.toPx()
                val fade = waveLen * 1.5f
                val shift = wavePhase.floatValue / (2f * PI.toFloat()) * waveLen
                val capR = trackH / 2f
                fun yAt(x: Float): Float {
                    val coeff = ((activeW + fade / 2f - x) / fade).coerceIn(0f, 1f)
                    return cy + amp * coeff * sin(2f * PI.toFloat() * (x + shift) / waveLen)
                }
                val wave = Path()
                var wx = capR
                val endX = (totalW - capR).coerceAtLeast(capR)
                wave.moveTo(wx, yAt(wx))
                while (wx < endX) {
                    wx = minOf(wx + 2.dp.toPx(), endX)
                    wave.lineTo(wx, yAt(wx))
                }
                val squiggle = Stroke(width = trackH, cap = StrokeCap.Round, join = StrokeJoin.Round)
                val clipH = amp + trackH + 2.dp.toPx()
                clipRect(left = 0f, top = cy - clipH, right = activeW, bottom = cy + clipH) {
                    drawPath(wave, accent, style = squiggle)
                }
                clipRect(left = activeW, top = cy - clipH, right = totalW, bottom = cy + clipH) {
                    drawPath(wave, accent.copy(alpha = 0.30f), style = squiggle)
                }
            } else {
                val wavy = seekStyle == SeekbarStyle.WAVY
                val gapPx = if (wavy) trackH * 0.9f else 0f
                val grooveStart = if (wavy) (activeW + gapPx).coerceAtMost(totalW) else 0f
                if (totalW - grooveStart > 0.5f) {
                    drawRoundRect(ink.copy(alpha = 0.22f), Offset(grooveStart, top), Size(totalW - grooveStart, trackH), rail)
                    if (bufferedFraction > fraction) {
                        val bx = maxOf(grooveStart, 0f)
                        val bw = totalW * bufferedFraction - bx
                        if (bw > 0.5f) drawRoundRect(ink.copy(alpha = 0.30f), Offset(bx, top), Size(bw, trackH), rail)
                    }
                }
                val ampPx = waveAmp * 2.6.dp.toPx()
                if (activeW > 0.5f && ampPx > 0.15f) {
                    val startX = trackH / 2f
                    val endX = (activeW - trackH / 2f).coerceAtLeast(startX)
                    val k = 2f * PI.toFloat() / 30.dp.toPx()
                    val taper = 22.dp.toPx()
                    val ph = wavePhase.floatValue
                    fun yAt(x: Float): Float {
                        val edge = minOf((x - startX) / taper, (endX - x) / taper).coerceIn(0f, 1f)
                        return cy + ampPx * edge * sin(k * x - ph)
                    }
                    val wave = Path()
                    var wx = startX
                    wave.moveTo(wx, yAt(wx))
                    while (wx < endX) {
                        wx = minOf(wx + 2.dp.toPx(), endX)
                        wave.lineTo(wx, yAt(wx))
                    }
                    drawPath(
                        path = wave,
                        color = accent,
                        style = Stroke(width = trackH, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                } else if (activeW > 0.5f) {
                    if (seekStyle != SeekbarStyle.WAVY) drawRoundRect(
                        accent.copy(alpha = 0.14f),
                        Offset(0f, top - 1.dp.toPx()), Size(activeW, trackH + 2.dp.toPx()),
                        CornerRadius((trackH + 2.dp.toPx()) / 2f)
                    )
                    if (drag > 0.01f && seekStyle != SeekbarStyle.WAVY) {
                        drawRoundRect(
                            accent.copy(alpha = 0.22f * drag),
                            Offset(0f, top - 1.5.dp.toPx() * drag), Size(activeW, trackH + 3.dp.toPx() * drag),
                            CornerRadius((trackH + 3.dp.toPx() * drag) / 2f)
                        )
                    }
                    drawRoundRect(
                        color = accent,
                        topLeft = Offset(0f, top), size = Size(activeW, trackH), cornerRadius = rail
                    )
                }
            }
            marks.forEachIndexed { i, ch ->
                val x = totalW * (ch.startMs.toFloat() / duration.toFloat())
                val near = drag > 0.01f && i == chapterIdx
                val r = (1.8f + 0.8f * drag + if (near) 1.2f * drag else 0f).dp.toPx()
                drawCircle(Color.Black.copy(alpha = 0.35f), r + 1.dp.toPx(), Offset(x, cy))
                drawCircle(if (near) accent else ink.copy(alpha = if (x <= activeW) 1f else 0.85f), r, Offset(x, cy))
            }
            val tr = thumbSize.toPx() / 2f
            when (seekThumb) {
                SeekbarThumb.CIRCLE -> {
                    val haloR = tr * (1.8f + 0.35f * drag)
                    drawCircle(accent.copy(alpha = 0.16f + 0.10f * drag), haloR, centre)
                    drawCircle(Color.Black.copy(alpha = 0.30f), tr + 1.dp.toPx(), centre)
                    drawCircle(Color.White, tr, centre)
                    drawCircle(accent, tr * (0.42f - 0.12f * drag), centre)
                    if (drag > 0.01f) drawCircle(accent.copy(alpha = drag), tr - 0.75.dp.toPx(), centre, style = Stroke(1.5.dp.toPx()))
                }
                SeekbarThumb.BAR -> {
                    val bw = (4f + 2f * drag).dp.toPx()
                    val bh = (24f + 8f * drag).dp.toPx()
                    val bs = Size(bw, bh)
                    val bt = Offset(centre.x - bw / 2f, cy - bh / 2f)
                    drawRoundRect(Color.Black.copy(alpha = 0.30f), bt - Offset(1.dp.toPx(), 1.dp.toPx()), Size(bw + 2.dp.toPx(), bh + 2.dp.toPx()), CornerRadius(bw))
                    drawRoundRect(if (seekStyle == SeekbarStyle.SQUIGGLY) accent else ink, bt, bs, CornerRadius(bw / 2f))
                }
                SeekbarThumb.DIAMOND -> {
                    val ds = tr * (1.55f + 0.25f * drag)
                    rotate(45f, centre) {
                        drawRoundRect(Color.Black.copy(alpha = 0.30f), Offset(centre.x - ds / 2f - 1.dp.toPx(), cy - ds / 2f - 1.dp.toPx()), Size(ds + 2.dp.toPx(), ds + 2.dp.toPx()), CornerRadius(ds * 0.30f))
                        drawRoundRect(Color.White, Offset(centre.x - ds / 2f, cy - ds / 2f), Size(ds, ds), CornerRadius(ds * 0.28f))
                        drawRoundRect(accent, Offset(centre.x - ds * 0.18f, cy - ds * 0.18f), Size(ds * 0.36f, ds * 0.36f), CornerRadius(ds * 0.10f))
                    }
                }
            }
        }

        val previewAlpha by animateFloatAsState(if (isDragging) 1f else 0f, tween(180), label = "pvA")
        val previewY by animateDpAsState(
            if (isDragging) (-36).dp else (-28).dp,
            spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow), label = "pvY"
        )
        if (previewAlpha > 0.01f && videoUri != null) {
            val cardW = 112.dp
            val cardH = 63.dp
            val cardShape = RoundedCornerShape(16.dp)
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
                            .clip(cardShape)
                            .background(Color.Black)
                    ) {
                        CoverPlaySeekPreviewFrame(videoUri, targetMs, cardW, cardH)
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
                            color = accent,
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

@OptIn(UnstableApi::class)
@Composable
private fun CoverPlaySeekPreviewFrame(videoUri: Uri, targetMs: Long, w: androidx.compose.ui.unit.Dp, h: androidx.compose.ui.unit.Dp) {
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
