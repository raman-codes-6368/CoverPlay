package com.raman.CoverPlay.settings

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
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
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raman.CoverPlay.R
import com.raman.CoverPlay.components.CoverPlayQuipLine
import com.raman.CoverPlay.components.Quips
import com.raman.CoverPlay.ui.theme.CoverPlayMaterialShapes
import com.raman.CoverPlay.ui.theme.CoverPlayShapeBadge
import com.raman.CoverPlay.ui.theme.LocalAppUiSettings
import com.raman.CoverPlay.ui.theme.onFill
import com.raman.CoverPlay.util.rememberCoverPlayHapticFeedback
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

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
        val cutoff = dayKey(-KEEP_DAYS)
        p.all.keys.filter { it < cutoff }.forEach { e.remove(it) }
        e.apply()
    }

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
fun CoverPlayUsageStatsSection(modifier: Modifier = Modifier, embedded: Boolean = false) {
    val context = LocalContext.current
    val s = LocalAppUiSettings.current
    val accent = s.accent
    val hi = s.highlight
    val haptic = rememberCoverPlayHapticFeedback()

    var data by remember { mutableStateOf(UsageTracker.lastDays(context)) }
    LaunchedEffect(Unit) {
        while (true) {
            data = UsageTracker.lastDays(context)
            kotlinx.coroutines.delay(5_000L)
        }
    }

    var isExpanded by remember { mutableStateOf(false) }
    var selected by remember { mutableIntStateOf(data.lastIndex) }
    val values = data.map { it.second }
    val todayMs = values.lastOrNull() ?: 0L
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
    LaunchedEffect(isExpanded) {
        if (isExpanded) {
            reveal.snapTo(0f)
            reveal.animateTo(1f, tween(600, easing = FastOutSlowInEasing))
        }
    }

    val best = values.maxOrNull() ?: 0L
    val tonal = s.palette.containerHigh.copy(alpha = if (s.palette.isDark) 0.62f else 0.92f)
    val chevron by animateFloatAsState(if (isExpanded) 180f else 0f, spring(dampingRatio = 0.6f, stiffness = 400f), label = "statsChevron")

    CoverPlayUsageCardShell(
        embedded = embedded,
        modifier = modifier,
        fill = tonal,
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            isExpanded = !isExpanded
        }
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                CoverPlayShapeBadge(
                    shape = CoverPlayMaterialShapes.cookie9(),
                    size = 52.dp,
                    container = accent.copy(alpha = 0.22f)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_graph),
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text("Watch stats", style = MaterialTheme.typography.titleMedium, color = s.palette.textPrimary)
                    Text(
                        "Today ${fmtSpent(todayMs)}  ·  Week ${fmtSpent(weekTotal)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = s.palette.textSecondary
                    )
                }
                Box(
                    Modifier.size(36.dp).clip(CircleShape).background(accent.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_chevron_down_custom),
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = accent,
                        modifier = Modifier.size(20.dp).rotate(chevron)
                    )
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn(tween(250)) + expandVertically(spring(dampingRatio = 0.75f, stiffness = 420f)),
                exit = fadeOut(tween(180)) + shrinkVertically(spring(dampingRatio = 1f, stiffness = 500f))
            ) {
                Column(Modifier.padding(top = 18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)) {
                        CoverPlayStatChip(dayName, accent.copy(alpha = 0.18f), accent)
                        if (diffPct != null) {
                            CoverPlayStatChip(
                                "${if (diffPct >= 0) "▲" else "▼"} ${kotlin.math.abs(diffPct)}%",
                                s.palette.containerHighest,
                                if (diffPct >= 0) accent else s.palette.textSecondary
                            )
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        fmtSpent(selMs),
                        style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold, letterSpacing = (-1).sp),
                        color = s.palette.textPrimary
                    )
                    Text(
                        if (diffPct != null) "vs the previous day" else "Time spent watching in CoverPlay",
                        style = MaterialTheme.typography.bodySmall,
                        color = s.palette.textTertiary
                    )

                    Spacer(Modifier.height(16.dp))

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)) {
                        CoverPlayStatTile("Daily avg", fmtSpent(avg), accent.copy(alpha = 0.16f), RoundedCornerShape(28.dp), Modifier.weight(1f))
                        CoverPlayStatTile("This week", fmtSpent(weekTotal), hi.copy(alpha = 0.18f), RoundedCornerShape(topStart = 28.dp, topEnd = 10.dp, bottomEnd = 28.dp, bottomStart = 10.dp), Modifier.weight(1f))
                        CoverPlayStatTile("Best day", fmtSpent(best), s.secondaryAccent.copy(alpha = 0.18f), RoundedCornerShape(topStart = 10.dp, topEnd = 28.dp, bottomEnd = 10.dp, bottomStart = 28.dp), Modifier.weight(1f))
                    }

                    Spacer(Modifier.height(18.dp))

                    val letters = data.map { SimpleDateFormat("EEE", Locale.getDefault()).format(it.first.time).take(1).uppercase() }
                    val selAnim by animateFloatAsState(sel.toFloat(), spring(0.6f, 380f), label = "statSel")
                    val barIdle = accent.copy(alpha = 0.20f)

                    Canvas(
                        Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .pointerInput(data.size) {
                                detectTapGestures { o ->
                                    val slot = size.width / data.size.toFloat()
                                    selected = (o.x / slot).toInt().coerceIn(0, data.lastIndex)
                                }
                            }
                    ) {
                        val n = data.size
                        val slot = size.width / n
                        val barW = slot * 0.58f
                        val chartH = size.height
                        data.forEachIndexed { i, (_, v) ->
                            val cx = slot * (i + 0.5f)
                            val closeness = (1f - kotlin.math.abs(selAnim - i)).coerceIn(0f, 1f)
                            val frac = (v / maxV).coerceIn(0f, 1f)
                            val h = (chartH * frac * reveal.value).coerceAtLeast(barW * reveal.value)
                            drawRoundRect(
                                androidx.compose.ui.graphics.lerp(barIdle, accent, closeness),
                                Offset(cx - barW / 2f, chartH - h), Size(barW, h), CornerRadius(barW / 2f)
                            )
                        }
                        if (avg > 0) {
                            val y = chartH - chartH * (avg / maxV).coerceIn(0f, 1f)
                            drawLine(
                                s.palette.textTertiary.copy(alpha = 0.45f), Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx(),
                                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(8f, 10f))
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth()) {
                        letters.forEachIndexed { i, l ->
                            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                Box(
                                    Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(if (i == sel) accent else Color.Transparent)
                                        .androidxClickable { selected = i },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        l,
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = if (i == sel) FontWeight.Bold else FontWeight.Medium),
                                        color = if (i == sel) accent.onFill() else s.palette.textTertiary
                                    )
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    CoverPlayQuipLine(
                        Quips.usage(selMs),
                        modifier = Modifier.fillMaxWidth(),
                        align = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun CoverPlayStatChip(text: String, bg: Color, fg: Color) {
    Text(
        text,
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
        color = fg,
        modifier = Modifier.clip(CircleShape).background(bg).padding(horizontal = 12.dp, vertical = 5.dp)
    )
}

@Composable
private fun CoverPlayStatTile(label: String, value: String, bg: Color, shape: androidx.compose.ui.graphics.Shape, modifier: Modifier) {
    val s = LocalAppUiSettings.current
    Column(
        modifier = modifier.clip(shape).background(bg).padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = s.palette.textSecondary, maxLines = 1)
        Spacer(Modifier.height(2.dp))
        Text(
            value,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = s.palette.textPrimary,
            maxLines = 1
        )
    }
}

@Composable
private fun CoverPlayUsageCardShell(
    embedded: Boolean,
    modifier: Modifier,
    fill: Color,
    onClick: () -> Unit,
    content: @Composable androidx.compose.foundation.layout.BoxScope.() -> Unit
) {
    if (embedded) {
        androidx.compose.foundation.layout.Box(
            modifier = modifier
                .fillMaxWidth()
                .animateContentSize(spring(dampingRatio = 0.8f, stiffness = 500f))
                .androidxClickable(onClick),
            content = content
        )
    } else {
        androidx.compose.foundation.layout.Box(
            modifier = modifier
                .padding(horizontal = 20.dp, vertical = 4.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(32.dp))
                .background(fill)
                .animateContentSize(spring(dampingRatio = 0.8f, stiffness = 500f))
                .androidxClickable(onClick),
            content = content
        )
    }
}

private fun Modifier.androidxClickable(onClick: () -> Unit): Modifier = composed {
    this.clickable(
        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
        indication = null,
        onClick = onClick
    )
}
