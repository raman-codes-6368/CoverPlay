package com.raman.CoverPlay.player

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.raman.CoverPlay.R
import com.raman.CoverPlay.components.CoverPlaySwitch
import com.raman.CoverPlay.settings.Playlist
import com.raman.CoverPlay.ui.theme.LocalAppUiSettings
import com.raman.CoverPlay.util.rememberCoverPlayHaptic

private const val PC_PREFS = "coverplay_settings_v3"

private const val PC_LAYOUT_VERSION = 2

enum class PlayerButton(val label: String, val icon: Int) {
    BROWSE("Playlist", R.drawable.ic_browse_list_custom),
    SUBTITLES("Subtitles", R.drawable.ic_subtitles_custom),
    AUDIO_TRACK("Audio track", R.drawable.ic_music_custom),
    SPEED("Playback speed", R.drawable.ic_speed_custom),
    ROTATE("Rotate", R.drawable.ic_orientation_custom),
    ASPECT("Aspect ratio", R.drawable.ic_aspect_fit),
    PIP("Picture in picture", R.drawable.ic_pip_custom),
    LOCK("Lock controls", R.drawable.ic_lock_custom),
    REPEAT("Repeat", R.drawable.ic_repeat_custom),
    SHUFFLE("Shuffle", R.drawable.ic_shuffle_custom),
    BACKGROUND("Background play", R.drawable.ic_headphones_custom),
    MUTE("Mute", R.drawable.ic_volume_custom),
    AB_LOOP("A–B loop", R.drawable.ic_repeat_custom),
    SLEEP_TIMER("Sleep timer", R.drawable.ic_stopwatch_custom),
    FRAME_BACK("Previous frame", R.drawable.ic_prev_custom),
    FRAME_FORWARD("Next frame", R.drawable.ic_next_custom),
    SCREENSHOT("Screenshot", R.drawable.ic_thumbnail),
    MIRROR("Mirror video", R.drawable.ic_orientation_custom),
    AUDIO_ONLY("Audio only", R.drawable.ic_headphones_custom),
    MORE("Playback settings", R.drawable.ic_player_controls);
}

enum class PlayerZone(val label: String, val landscape: Boolean, val key: String, val default: List<PlayerButton>) {
    L_TOP_END("Top right", true, "pc_l_top_end", listOf(PlayerButton.PIP, PlayerButton.LOCK, PlayerButton.MORE)),
    L_BOTTOM_START("Bottom left", true, "pc_l_bottom_start", listOf(PlayerButton.BROWSE, PlayerButton.SUBTITLES, PlayerButton.AUDIO_TRACK)),
    L_BOTTOM_END("Bottom right", true, "pc_l_bottom_end", listOf(PlayerButton.SPEED, PlayerButton.ASPECT, PlayerButton.ROTATE)),
    P_TOP_END("Top right", false, "pc_p_top_end", listOf(PlayerButton.PIP, PlayerButton.MORE)),
    P_BOTTOM_START("Bottom left", false, "pc_p_bottom_start", listOf(PlayerButton.BROWSE)),
    P_BOTTOM_END("Bottom right", false, "pc_p_bottom_end", listOf(PlayerButton.ROTATE)),

    L_EDGE_START("Left edge (middle)", true, "pc_l_edge_start", emptyList()),
    L_EDGE_END("Right edge (middle)", true, "pc_l_edge_end", emptyList()),
    P_EDGE_START("Left edge (middle)", false, "pc_p_edge_start", emptyList()),
    P_EDGE_END("Right edge (middle)", false, "pc_p_edge_end", emptyList());
}

enum class SeekbarPlacement(val label: String) { ABOVE("Above buttons"), BELOW("Below buttons") }
enum class TransportPlacement(val label: String) { BOTTOM("In bottom row"), CENTER("Centre of screen") }
enum class ControlSize(val label: String, val button: Int, val play: Int) {
    COMPACT("Compact", 32, 44), DEFAULT("Default", 36, 52), LARGE("Large", 44, 64)
}

data class PlayerCustomization(
    val layout: Map<PlayerZone, List<PlayerButton>> = PlayerZone.entries.associateWith { it.default },
    val seekbarPlacement: SeekbarPlacement = SeekbarPlacement.ABOVE,
    val transportPlacement: TransportPlacement = TransportPlacement.BOTTOM,
    val showTimeLabels: Boolean = true,
    val invertDuration: Boolean = false,
    val showPrevNext: Boolean = true,
    val showTitle: Boolean = true,
    val controlSize: ControlSize = ControlSize.DEFAULT,
    val hideAfterMs: Int = 3000,
    val holdSpeed: Float = 2f,
    val swapBrightnessVolume: Boolean = false,
    val swipeSeekSeconds: Int = 120,
) {
    fun zone(z: PlayerZone): List<PlayerButton> = layout[z] ?: z.default
    fun withZone(z: PlayerZone, list: List<PlayerButton>) = copy(layout = layout + (z to list))
    fun zonesFor(landscape: Boolean) = PlayerZone.entries.filter { it.landscape == landscape }
    fun mainZones(landscape: Boolean) = zonesFor(landscape).take(3)
    fun unassigned(landscape: Boolean): List<PlayerButton> {
        val used = mainZones(landscape).flatMap { zone(it) }.toSet()
        return PlayerButton.entries.filter { it !in used }
    }
}

private fun parseButtons(csv: String?, fallback: List<PlayerButton>): List<PlayerButton> {
    if (csv == null) return fallback
    val seen = HashSet<PlayerButton>()
    return csv.split(',').mapNotNull { n -> PlayerButton.entries.firstOrNull { it.name == n.trim() } }.filter { seen.add(it) }
}

fun loadPlayerCustomization(context: Context): PlayerCustomization {
    val p = context.getSharedPreferences(PC_PREFS, Context.MODE_PRIVATE)
    if (p.getInt("pc_layout_v", 0) < PC_LAYOUT_VERSION) {
        p.edit().apply {
            PlayerZone.entries.forEach { remove(it.key) }
            putInt("pc_layout_v", PC_LAYOUT_VERSION)
        }.apply()
    }
    val layout = HashMap<PlayerZone, List<PlayerButton>>()
    for (landscape in listOf(true, false)) {
        val used = HashSet<PlayerButton>()
        for (z in PlayerZone.entries.filter { it.landscape == landscape }) {
            layout[z] = if (z.default.isEmpty()) emptyList()
            else parseButtons(p.getString(z.key, null), z.default).filter { used.add(it) }
        }
    }
    return PlayerCustomization(
        layout = layout,
        seekbarPlacement = SeekbarPlacement.entries.firstOrNull { it.name == p.getString("pc_seek_pos", null) } ?: SeekbarPlacement.ABOVE,
        transportPlacement = TransportPlacement.entries.firstOrNull { it.name == p.getString("pc_transport", null) } ?: TransportPlacement.BOTTOM,
        showTimeLabels = p.getBoolean("pc_time_labels", true),
        invertDuration = p.getBoolean("pc_invert_duration", false),
        showPrevNext = p.getBoolean("pc_prev_next", true),
        showTitle = p.getBoolean("pc_show_title", true),
        controlSize = ControlSize.entries.firstOrNull { it.name == p.getString("pc_control_size", null) } ?: ControlSize.DEFAULT,
        hideAfterMs = p.getInt("pc_hide_after", 3000),
        holdSpeed = p.getFloat("pc_hold_speed", 2f),
        swapBrightnessVolume = p.getBoolean("pc_swap_bv", false),
        swipeSeekSeconds = p.getInt("pc_swipe_seek", 120),
    )
}

fun savePlayerCustomization(context: Context, c: PlayerCustomization) {
    context.getSharedPreferences(PC_PREFS, Context.MODE_PRIVATE).edit().apply {
        PlayerZone.entries.forEach { putString(it.key, c.zone(it).joinToString(",") { b -> b.name }) }
        putInt("pc_layout_v", PC_LAYOUT_VERSION)
        putString("pc_seek_pos", c.seekbarPlacement.name)
        putString("pc_transport", c.transportPlacement.name)
        putBoolean("pc_time_labels", c.showTimeLabels)
        putBoolean("pc_invert_duration", c.invertDuration)
        putBoolean("pc_prev_next", c.showPrevNext)
        putBoolean("pc_show_title", c.showTitle)
        putString("pc_control_size", c.controlSize.name)
        putInt("pc_hide_after", c.hideAfterMs)
        putFloat("pc_hold_speed", c.holdSpeed)
        putBoolean("pc_swap_bv", c.swapBrightnessVolume)
        putInt("pc_swipe_seek", c.swipeSeekSeconds)
    }.apply()
}

fun LazyListScope.playerCustomizationPage() {
    item { CoverPlayPlayerCustomizationContent() }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CoverPlayPlayerCustomizationContent() {
    val context = LocalContext.current
    val ui = LocalAppUiSettings.current
    var cfg by remember { mutableStateOf(loadPlayerCustomization(context)) }
    var editing by remember { mutableStateOf<PlayerZone?>(null) }
    fun update(n: PlayerCustomization) { cfg = n; savePlayerCustomization(context, n) }

    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        CoverPlayPlayerCustomizeHeader("Landscape controls")
        CoverPlayPlayerCustomizeCard {
            Column {
                cfg.mainZones(true).forEachIndexed { i, z ->
                    if (i > 0) CoverPlayPlayerCustomizeDivider()
                    CoverPlayPlayerCustomizeZoneRow(z, cfg.zone(z)) { editing = z }
                }
            }
        }
        CoverPlayPlayerCustomizeHeader("Portrait controls")
        CoverPlayPlayerCustomizeCard {
            Column {
                cfg.mainZones(false).forEachIndexed { i, z ->
                    if (i > 0) CoverPlayPlayerCustomizeDivider()
                    CoverPlayPlayerCustomizeZoneRow(z, cfg.zone(z)) { editing = z }
                }
            }
        }

        CoverPlayPlayerCustomizeHeader("Seekbar")
        CoverPlayPlayerCustomizeCard {
            Column {
                CoverPlayPlayerCustomizeChoice("Seekbar position", SeekbarPlacement.entries, cfg.seekbarPlacement, { it.label }) { update(cfg.copy(seekbarPlacement = it)) }
                CoverPlayPlayerCustomizeDivider()
                CoverPlayPlayerCustomizeChoice("Play controls", TransportPlacement.entries, cfg.transportPlacement, { it.label }) { update(cfg.copy(transportPlacement = it)) }
                CoverPlayPlayerCustomizeDivider()
                CoverPlayPlayerCustomizeToggle("Show time labels", null, cfg.showTimeLabels) { update(cfg.copy(showTimeLabels = it)) }
                CoverPlayPlayerCustomizeDivider()
                CoverPlayPlayerCustomizeToggle("Show remaining time", null, cfg.invertDuration) { update(cfg.copy(invertDuration = it)) }
                CoverPlayPlayerCustomizeDivider()
                CoverPlayPlayerCustomizeToggle("Previous / next buttons", null, cfg.showPrevNext) { update(cfg.copy(showPrevNext = it)) }
            }
        }

        CoverPlayPlayerCustomizeHeader("Appearance")
        CoverPlayPlayerCustomizeCard {
            Column {
                CoverPlayPlayerCustomizeChoice("Control size", ControlSize.entries, cfg.controlSize, { it.label }) { update(cfg.copy(controlSize = it)) }
                CoverPlayPlayerCustomizeDivider()
                CoverPlayPlayerCustomizeToggle("Show video title", null, cfg.showTitle) { update(cfg.copy(showTitle = it)) }
                CoverPlayPlayerCustomizeDivider()
                CoverPlayPlayerCustomizeChoice("Hide controls after", listOf(2000, 3000, 5000, 8000, 0), cfg.hideAfterMs, { if (it == 0) "Never" else "${it / 1000}s" }) { update(cfg.copy(hideAfterMs = it)) }
            }
        }

        CoverPlayPlayerCustomizeHeader("Gestures")
        CoverPlayPlayerCustomizeCard {
            Column {
                CoverPlayPlayerCustomizeChoice("Hold to speed up", listOf(1.5f, 2f, 3f, 4f), cfg.holdSpeed, { "${it.toString().removeSuffix(".0")}x" }) { update(cfg.copy(holdSpeed = it)) }
                CoverPlayPlayerCustomizeDivider()
                CoverPlayPlayerCustomizeChoice("Swipe to seek range", listOf(60, 120, 240), cfg.swipeSeekSeconds, { "${it}s" }) { update(cfg.copy(swipeSeekSeconds = it)) }
                CoverPlayPlayerCustomizeDivider()
                CoverPlayPlayerCustomizeToggle("Swap brightness & volume", null, cfg.swapBrightnessVolume) { update(cfg.copy(swapBrightnessVolume = it)) }
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(
            "Seekbar shape and handle are in Appearance.",
            style = MaterialTheme.typography.bodySmall,
            color = ui.palette.textTertiary,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }

    editing?.let { zone ->
        CoverPlayPlayerCustomizeZoneEditor(zone = zone, cfg = cfg, onChange = { update(it) }, onDismiss = { editing = null })
    }
}

@Composable
private fun CoverPlayPlayerCustomizeZoneEditor(zone: PlayerZone, cfg: PlayerCustomization, onChange: (PlayerCustomization) -> Unit, onDismiss: () -> Unit) {
    val ui = LocalAppUiSettings.current
    val haptic = rememberCoverPlayHaptic()
    val list = cfg.zone(zone)
    val available = cfg.unassigned(zone.landscape)

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Column(Modifier.fillMaxSize().background(ui.palette.bg).padding(top = 8.dp)) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                CoverPlayPlayerCustomizeIconAction(Icons.AutoMirrored.Rounded.ArrowBack, "Back", ui.palette.textPrimary, onDismiss)
                Text(
                    "${if (zone.landscape) "Landscape" else "Portrait"} · ${zone.label}",
                    style = MaterialTheme.typography.titleLarge, color = ui.palette.textPrimary,
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                )
                Text(
                    "Reset", style = MaterialTheme.typography.labelLarge, color = ui.accent,
                    modifier = Modifier.clip(CircleShape)
                        .clickable { haptic(); onChange(cfg.withZoneReset(zone)) }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                )
            }
            Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp).padding(bottom = 24.dp)) {
                Text(
                    "Add or remove buttons, and use the arrows to change their order.",
                    style = MaterialTheme.typography.bodySmall, color = ui.palette.textTertiary,
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                )
                CoverPlayPlayerCustomizeHeader("On screen")
                CoverPlayPlayerCustomizeCard {
                    if (list.isEmpty()) {
                        Text("Nothing here yet", style = MaterialTheme.typography.bodyMedium, color = ui.palette.textTertiary, modifier = Modifier.padding(16.dp))
                    } else Column {
                        list.forEachIndexed { i, b ->
                            if (i > 0) CoverPlayPlayerCustomizeDivider()
                            Row(Modifier.fillMaxWidth().padding(start = 16.dp, end = 4.dp, top = 2.dp, bottom = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(painterResource(b.icon), null, tint = ui.palette.textPrimary, modifier = Modifier.size(22.dp))
                                Text(b.label, style = MaterialTheme.typography.bodyLarge, color = ui.palette.textPrimary, modifier = Modifier.weight(1f).padding(horizontal = 14.dp))
                                if (i > 0) CoverPlayPlayerCustomizeIconAction(Icons.Rounded.KeyboardArrowUp, "Move up", ui.palette.textSecondary) {
                                    onChange(cfg.withZone(zone, list.toMutableList().also { it.removeAt(i); it.add(i - 1, b) }))
                                } else Spacer(Modifier.size(48.dp))
                                if (i < list.lastIndex) CoverPlayPlayerCustomizeIconAction(Icons.Rounded.KeyboardArrowDown, "Move down", ui.palette.textSecondary) {
                                    onChange(cfg.withZone(zone, list.toMutableList().also { it.removeAt(i); it.add(i + 1, b) }))
                                } else Spacer(Modifier.size(48.dp))
                                CoverPlayPlayerCustomizeIconAction(Icons.Rounded.Remove, "Remove", ui.palette.textSecondary) { onChange(cfg.withZone(zone, list - b)) }
                            }
                        }
                    }
                }
                CoverPlayPlayerCustomizeHeader("Available")
                CoverPlayPlayerCustomizeCard {
                    if (available.isEmpty()) {
                        Text("Every control is already placed", style = MaterialTheme.typography.bodyMedium, color = ui.palette.textTertiary, modifier = Modifier.padding(16.dp))
                    } else Column {
                        available.forEachIndexed { i, b ->
                            if (i > 0) CoverPlayPlayerCustomizeDivider()
                            Row(Modifier.fillMaxWidth().padding(start = 16.dp, end = 4.dp, top = 2.dp, bottom = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(painterResource(b.icon), null, tint = ui.palette.textSecondary, modifier = Modifier.size(22.dp))
                                Text(b.label, style = MaterialTheme.typography.bodyLarge, color = ui.palette.textSecondary, modifier = Modifier.weight(1f).padding(horizontal = 14.dp))
                                CoverPlayPlayerCustomizeIconAction(Icons.Rounded.Add, "Add", ui.accent) { onChange(cfg.withZone(zone, list + b)) }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun PlayerCustomization.withZoneReset(zone: PlayerZone): PlayerCustomization {
    var n = this
    mainZones(zone.landscape).forEach { z -> if (z != zone) n = n.withZone(z, n.zone(z) - zone.default.toSet()) }
    return n.withZone(zone, zone.default)
}

@Composable
private fun CoverPlayPlayerCustomizeHeader(title: String) {
    val p = LocalAppUiSettings.current
    Text(
        title, style = MaterialTheme.typography.titleSmall, color = p.accent, fontWeight = FontWeight.SemiBold,
        modifier = Modifier.fillMaxWidth().padding(top = 20.dp, bottom = 8.dp, start = 4.dp)
    )
}

@Composable
private fun CoverPlayPlayerCustomizeCard(content: @Composable () -> Unit) {
    val p = LocalAppUiSettings.current.palette
    val shape = RoundedCornerShape(22.dp)
    Box(Modifier.fillMaxWidth().clip(shape).background(p.container).border(BorderStroke(1.dp, p.outlineFaint), shape)) { content() }
}

@Composable
private fun CoverPlayPlayerCustomizeDivider() {
    HorizontalDivider(Modifier.padding(horizontal = 16.dp), thickness = 1.dp, color = LocalAppUiSettings.current.palette.outlineFaint)
}

@Composable
private fun CoverPlayPlayerCustomizeIconAction(icon: ImageVector, description: String, tint: androidx.compose.ui.graphics.Color, onClick: () -> Unit) {
    val haptic = rememberCoverPlayHaptic()
    Box(
        Modifier.size(48.dp).clip(CircleShape).clickable { haptic(); onClick() },
        contentAlignment = Alignment.Center
    ) { Icon(icon, description, tint = tint, modifier = Modifier.size(22.dp)) }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CoverPlayPlayerCustomizeZoneRow(zone: PlayerZone, buttons: List<PlayerButton>, onEdit: () -> Unit) {
    val p = LocalAppUiSettings.current.palette
    Column(Modifier.fillMaxWidth().clickable(onClick = onEdit).padding(start = 16.dp, end = 4.dp, top = 4.dp, bottom = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(zone.label, style = MaterialTheme.typography.bodyLarge, color = p.textPrimary, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
            CoverPlayPlayerCustomizeIconAction(Icons.Rounded.Edit, "Edit ${zone.label}", p.textSecondary, onEdit)
        }
        if (buttons.isEmpty()) {
            Text("None", style = MaterialTheme.typography.bodyMedium, color = p.textTertiary, modifier = Modifier.padding(end = 12.dp))
        } else FlowRow(horizontalArrangement = Arrangement.spacedBy(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(end = 12.dp)) {
            buttons.forEach { Icon(painterResource(it.icon), it.label, tint = p.textSecondary, modifier = Modifier.size(22.dp)) }
        }
    }
}

@Composable
private fun CoverPlayPlayerCustomizeToggle(label: String, sub: String?, checked: Boolean, onChange: (Boolean) -> Unit) {
    val p = LocalAppUiSettings.current.palette
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyLarge, color = p.textPrimary)
            if (sub != null) Text(sub, style = MaterialTheme.typography.bodySmall, color = p.textTertiary)
        }
        Spacer(Modifier.width(12.dp))
        CoverPlaySwitch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
private fun <T> CoverPlayPlayerCustomizeChoice(label: String, options: List<T>, selected: T, text: (T) -> String, onSelect: (T) -> Unit) {
    val ui = LocalAppUiSettings.current
    var open by remember { mutableStateOf(false) }
    Box {
        Row(
            Modifier.fillMaxWidth().clickable { open = true }.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = MaterialTheme.typography.bodyLarge, color = ui.palette.textPrimary, modifier = Modifier.weight(1f))
            Text(text(selected), style = MaterialTheme.typography.bodyMedium, color = ui.accent)
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            options.forEach { o ->
                DropdownMenuItem(
                    text = { Text(text(o), color = if (o == selected) ui.accent else ui.palette.textPrimary) },
                    onClick = { open = false; onSelect(o) }
                )
            }
        }
    }
}
