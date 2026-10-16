package com.example.myapplication

import android.content.Context
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.GlassSurface
import com.example.myapplication.ui.theme.LocalAppUiSettings
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// ═════════════════════════════════════════════════════════════════════════════
//  USAGE STATS — daily time spent in the app (local only, 30 days kept)
// ═════════════════════════════════════════════════════════════════════════════

object UsageTracker {
    private const val PREFS = "coverplay_usage"
    private const val KEEP_DAYS = 30
    private val fmt get() = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    fun dayKey(offsetDays: Int = 0): String {
        val c = Calendar.getInstance()
        c.add(Calendar.DAY_OF_YEAR, offsetDays)
        return fmt.format(c.time)
    }

    fun addTime(context: Context, ms: Long) {
        if (ms <= 0L) return
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val key = dayKey()
        val e = p.edit().putLong(key, p.getLong(key, 0L) + ms)
        // prune old days
        val cutoff = dayKey(-KEEP_DAYS)
        p.all.keys.filter { it < cutoff }.forEach { e.remove(it) }
        e.apply()
    }

    /** Last [days] days, oldest first, ending today. */
    fun lastDays(context: Context, days: Int = 7): List<Pair<Calendar, Long>> {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return (days - 1 downTo 0).map { off ->
            val c = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -off) }
            c to p.getLong(fmt.format(c.time), 0L)
        }
    }
}

private fun fmtSpent(ms: Long): String {
    val totalMin = ms / 60_000L
    val h = totalMin / 60
    val m = totalMin % 60
    return when {
        ms <= 0L -> "0m"
        h > 0 -> "${h}h ${m}m"
        totalMin == 0L -> "<1m"
        else -> "${m}m"
    }
}

@Composable
fun UsageStatsSection(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val s = LocalAppUiSettings.current
    val accent = s.accent
    val hi = s.highlight

    var data by remember { mutableStateOf(UsageTracker.lastDays(context)) }
    LaunchedEffect(Unit) {
        while (true) {
            data = UsageTracker.lastDays(context)
            kotlinx.coroutines.delay(5_000L)
        }
    }

    var selected by remember { mutableIntStateOf(data.lastIndex) }
    val values = data.map { it.second }
    val maxV = (values.maxOrNull() ?: 0L).coerceAtLeast(30 * 60_000L).toFloat()
    val weekTotal = values.sum()
    val activeDays = values.count { it > 0 }.coerceAtLeast(1)
    val avg = weekTotal / activeDays
    val sel = selected.coerceIn(0, data.lastIndex)
    val selMs = values[sel]
    val prev = if (sel > 0) values[sel - 1] else 0L
    val diffPct = if (prev > 0) (((selMs - prev).toFloat() / prev) * 100f).toInt() else null
    val dayName = if (sel == data.lastIndex) "Today"
    else SimpleDateFormat("EEEE", Locale.getDefault()).format(data[sel].first.time)

    val reveal = remember { Animatable(0f) }
    LaunchedEffect(Unit) { reveal.animateTo(1f, tween(900, easing = FastOutSlowInEasing)) }

    GlassSurface(
        modifier = modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
        shape = RoundedCornerShape(22.dp),
        elevation = 3.dp,
        pressedScale = 1f
    ) {
        Column(Modifier.padding(horizontal = 18.dp, vertical = 18.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(
                        dayName.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.4.sp),
                        color = accent
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        fmtSpent(selMs),
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = (-0.4).sp),
                        color = s.palette.textPrimary
                    )
                    Text(
                        if (diffPct != null) "${if (diffPct >= 0) "▲" else "▼"} ${kotlin.math.abs(diffPct)}% vs previous day"
                        else "Time spent watching in CoverPlay",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (diffPct == null) s.palette.textTertiary else if (diffPct >= 0) accent else s.palette.textSecondary
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "DAILY AVG",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.4.sp),
                        color = s.palette.textTertiary
                    )
                    Text(
                        fmtSpent(avg),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = s.palette.textPrimary
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "THIS WEEK",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.4.sp),
                        color = s.palette.textTertiary
                    )
                    Text(
                        fmtSpent(weekTotal),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = s.palette.textPrimary
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            val letters = data.map { SimpleDateFormat("EEE", Locale.getDefault()).format(it.first.time).take(1).uppercase() }
            val selAnim by animateFloatAsState(sel.toFloat(), spring(0.7f, 380f), label = "statSel")
            val gridColor = s.palette.textTertiary.copy(alpha = 0.10f)
            val barIdle = s.palette.textTertiary.copy(alpha = 0.28f)

            Canvas(
                Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .pointerInput(data.size) {
                        detectTapGestures { o ->
                            val slot = size.width / data.size.toFloat()
                            selected = (o.x / slot).toInt().coerceIn(0, data.lastIndex)
                        }
                    }
            ) {
                val n = data.size
                val slot = size.width / n
                val barW = slot * 0.42f
                val labelH = 22.dp.toPx()
                val chartH = size.height - labelH
                // faint guide lines
                for (i in 0..2) {
                    val y = chartH * i / 2f
                    drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
                }
                data.forEachIndexed { i, (_, v) ->
                    val cx = slot * (i + 0.5f)
                    val closeness = (1f - kotlin.math.abs(selAnim - i)).coerceIn(0f, 1f)
                    val frac = (v / maxV).coerceIn(0f, 1f)
                    val h = (chartH * frac * reveal.value).coerceAtLeast(6.dp.toPx() * reveal.value)
                    val top = chartH - h
                    val r = CornerRadius(barW / 2f)
                    if (closeness > 0.01f) {
                        // calm selection: a faint rounded column behind the bar (no glow)
                        drawRoundRect(
                            accent.copy(alpha = 0.09f * closeness),
                            Offset(cx - slot * 0.40f, 0f), Size(slot * 0.80f, chartH),
                            CornerRadius(slot * 0.28f)
                        )
                    }
                    drawRoundRect(barIdle, Offset(cx - barW / 2f, top), Size(barW, h), r)
                    if (closeness > 0.01f) {
                        drawRoundRect(
                            Brush.verticalGradient(listOf(hi.copy(alpha = closeness), accent.copy(alpha = closeness)), startY = top, endY = chartH),
                            Offset(cx - barW / 2f, top), Size(barW, h), r
                        )
                    }
                }
                // average dashed line
                if (avg > 0) {
                    val y = chartH - chartH * (avg / maxV).coerceIn(0f, 1f)
                    drawLine(
                        accent.copy(alpha = 0.40f), Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx(),
                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                    )
                }
            }
            Row(Modifier.fillMaxWidth()) {
                letters.forEachIndexed { i, l ->
                    Text(
                        l,
                        modifier = Modifier.weight(1f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (i == sel) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = if (i == sel) accent else s.palette.textTertiary
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            QuipLine(
                Quips.usage(selMs),
                modifier = Modifier.fillMaxWidth(),
                align = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}