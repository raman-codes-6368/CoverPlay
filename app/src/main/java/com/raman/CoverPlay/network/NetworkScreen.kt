package com.raman.CoverPlay.network

import android.content.Context
import android.net.Uri
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.raman.CoverPlay.R
import com.raman.CoverPlay.VideoFile
import com.raman.CoverPlay.components.CoverPlaySelectableCard
import com.raman.CoverPlay.components.CoverPlaySwitch
import com.raman.CoverPlay.formatSize
import com.raman.CoverPlay.ui.theme.*
import com.raman.CoverPlay.ui.theme.BrandAccent
import com.raman.CoverPlay.ui.theme.BrandRed
import com.raman.CoverPlay.ui.theme.CoverPlayBrandIconContainer
import com.raman.CoverPlay.ui.theme.CoverPlayButton
import com.raman.CoverPlay.ui.theme.CoverPlayLoader
import com.raman.CoverPlay.ui.theme.CoverPlayLoaderBadge
import com.raman.CoverPlay.ui.theme.CoverPlaySegmented
import com.raman.CoverPlay.ui.theme.LocalAppUiSettings
import com.raman.CoverPlay.ui.theme.glassFieldFill
import com.raman.CoverPlay.util.rememberCoverPlayHaptic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NetworkNav {
    var activeId by mutableStateOf<String?>(null)
    var path by mutableStateOf("")
}

@Stable
class NetworkController(
    private val context: Context,
    private val scope: CoroutineScope,
    private val nav: NetworkNav,
    private val onPlay: (VideoFile, List<VideoFile>, String) -> Unit
) {
    val connections = mutableStateListOf<NetConnection>().apply { addAll(NetworkStore.load(context)) }
    var active by mutableStateOf<NetConnection?>(null)
    var path by mutableStateOf("")
    var entries by mutableStateOf<List<NetFile>>(emptyList())
    var loading by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)
    var editing by mutableStateOf<NetConnection?>(null)
    var isNew by mutableStateOf(false)
    var deletingId by mutableStateOf<String?>(null)

    val isBrowsing: Boolean get() = active != null

    private fun persist() = NetworkStore.save(context, connections.toList())

    fun load(conn: NetConnection, rel: String) {
        loading = true; error = null
        scope.launch {
            val res = withContext(Dispatchers.IO) { runCatching { NetClients.get(conn).list(rel) } }
            loading = false
            res.onSuccess { entries = it; path = rel; active = conn; nav.activeId = conn.id; nav.path = rel }
                .onFailure {
                    error = it.message ?: "Couldn't open this location"
                    NetClients.dropAll()
                    if (active == null) entries = emptyList()
                }
        }
    }

    fun goUp() {
        val conn = active ?: return
        if (path.isEmpty()) close() else load(conn, path.substringBeforeLast('/', ""))
    }

    fun close() {
        active = null; entries = emptyList(); error = null; path = ""
        nav.activeId = null; nav.path = ""
    }

    fun restore() {
        val id = nav.activeId ?: return
        connections.firstOrNull { it.id == id }?.let { load(it, nav.path) }
    }

    fun startNew() {
        editing = NetConnection(name = "", protocol = NetProtocol.SMB, host = "", port = NetProtocol.SMB.defaultPort)
        isNew = true
    }

    fun startEdit(c: NetConnection) { editing = c; isNew = false }

    fun save(saved: NetConnection) {
        val i = connections.indexOfFirst { it.id == saved.id }
        if (i >= 0) connections[i] = saved else connections.add(saved)
        persist(); editing = null
    }

    fun remove(c: NetConnection) { connections.remove(c); persist(); deletingId = null }

    fun openFile(f: NetFile) {
        val conn = active ?: return
        if (f.isDirectory) { load(conn, f.path); return }
        if (!f.isVideo()) return
        scope.launch {
            val videos = entries.filter { it.isVideo() }
            val mapped = withContext(Dispatchers.IO) {
                runCatching { videos.map { v -> v.toVideoFile(NetClients.get(conn).playUri(v.path), conn) } }
            }
            mapped.onSuccess { list ->
                val picked = list.firstOrNull { it.name == f.name } ?: return@onSuccess
                onPlay(picked, list, conn.name)
            }.onFailure { error = it.message ?: "Couldn't open the file" }
        }
    }
}

@Composable
fun rememberCoverPlayNetworkController(
    nav: NetworkNav,
    onPlay: (VideoFile, List<VideoFile>, String) -> Unit
): NetworkController {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val latestPlay by rememberUpdatedState(onPlay)
    val controller = remember { NetworkController(context, scope, nav) { v, l, t -> latestPlay(v, l, t) } }
    LaunchedEffect(Unit) { controller.restore() }
    return controller
}

private fun NetFile.toVideoFile(uri: Uri, conn: NetConnection) = VideoFile(
    id = (conn.id + path).hashCode().toLong(),
    name = name,
    path = uri.toString(),
    duration = 0L,
    size = size,
    uri = uri
)

fun LazyListScope.networkSection(ctl: NetworkController) {
    ctl.editing?.let { draft ->
        item(key = "net_form") {
            Box(Modifier.padding(horizontal = 20.dp, vertical = 5.dp).animateContentSize()) {
                CoverPlayConnectionForm(
                    initial = draft,
                    isNew = ctl.isNew,
                    onCancel = { ctl.editing = null },
                    onSave = { ctl.save(it) }
                )
            }
        }
    }

    val conn = ctl.active
    if (conn == null) {
        items(ctl.connections, key = { it.id }) { c ->
            Box(Modifier.padding(horizontal = 20.dp, vertical = 5.dp).animateContentSize()) {
                CoverPlayConnectionCard(ctl, c)
            }
        }
        if (ctl.editing == null) {
            item(key = "net_add") {
                Box(Modifier.padding(horizontal = 20.dp, vertical = 5.dp)) {
                    val s = LocalAppUiSettings.current
                    CoverPlaySelectableCard(modifier = Modifier.fillMaxWidth(), onClick = { ctl.startNew() }, cornerRadius = 24.dp) {
                        Row(Modifier.padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                            CoverPlayBrandIconContainer(active = true, size = 40.dp) {
                                Icon(painterResource(R.drawable.ic_plus_custom), null, tint = s.accent, modifier = Modifier.size(22.dp))
                            }
                            Spacer(Modifier.width(14.dp))
                            Column(Modifier.weight(1f)) {
                                Text("Add connection", style = MaterialTheme.typography.titleMedium, color = s.palette.textPrimary)
                                Text("SMB, FTP or WebDAV server", style = MaterialTheme.typography.bodySmall, color = s.palette.textSecondary)
                            }
                        }
                    }
                }
            }
        }
    } else {
        item(key = "net_bar") {
            Box(Modifier.padding(horizontal = 20.dp, vertical = 5.dp)) { CoverPlayBrowserBar(ctl, conn) }
        }
        items(ctl.entries, key = { it.path }) { f ->
            Box(Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) { CoverPlayFileRow(f) { ctl.openFile(f) } }
        }
        if (ctl.loading) {
            item(key = "net_loading") {
                Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) { CoverPlayLoaderBadge() }
            }
        } else if (ctl.entries.isEmpty() && ctl.error == null) {
            item(key = "net_empty") {
                Text(
                    "This folder is empty.",
                    color = LocalAppUiSettings.current.palette.textTertiary,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                )
            }
        }
    }
    ctl.error?.let { msg ->
        item(key = "net_error") {
            Text(msg, color = BrandRed, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp))
        }
    }
    if (conn == null && ctl.loading) {
        item(key = "net_connecting") {
            Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) { CoverPlayLoader() }
        }
    }
}

@Composable
private fun CoverPlayConnectionCard(ctl: NetworkController, c: NetConnection) {
    val s = LocalAppUiSettings.current
    val confirming = ctl.deletingId == c.id
    CoverPlaySelectableCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = { if (!confirming) ctl.load(c, "") },
        onLongClick = { ctl.startEdit(c) },
        cornerRadius = 24.dp
    ) {
        Column {
            Row(Modifier.padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                CoverPlayBrandIconContainer(active = true, size = 40.dp) {
                    Icon(painterResource(R.drawable.ic_url_custom), null, tint = s.accent, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(c.name, style = MaterialTheme.typography.titleMedium, color = s.palette.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(
                        "${c.protocol.label} · ${c.host}:${c.port}",
                        style = MaterialTheme.typography.bodySmall, color = s.palette.textSecondary,
                        maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                }
                TextButton(onClick = { ctl.startEdit(c) }) {
                    Text("Edit", color = s.palette.textSecondary, fontWeight = FontWeight.SemiBold)
                }
                TextButton(onClick = { ctl.deletingId = if (confirming) null else c.id }) {
                    Icon(painterResource(R.drawable.ic_trash_custom), "Remove", tint = s.palette.textTertiary, modifier = Modifier.size(18.dp))
                }
            }
            if (confirming) {
                Column(Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp)) {
                    Text(
                        "Remove \"${c.name}\"? Files on the server are not touched.",
                        style = MaterialTheme.typography.bodySmall,
                        color = s.palette.textSecondary
                    )
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { ctl.deletingId = null }) { Text("Cancel", color = s.palette.textTertiary) }
                        TextButton(onClick = { ctl.remove(c) }) { Text("Remove", color = BrandRed, fontWeight = FontWeight.SemiBold) }
                    }
                }
            }
        }
    }
}

@Composable
private fun CoverPlayBrowserBar(ctl: NetworkController, conn: NetConnection) {
    val s = LocalAppUiSettings.current
    val where = if (ctl.path.isEmpty()) conn.name else conn.name + " / " + ctl.path.replace("/", " / ")
    Row(
        Modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .background(s.palette.containerHigh)
            .border(1.dp, s.palette.outlineFaint, CircleShape)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val navHaptic = rememberCoverPlayHaptic()
        Box(
            Modifier.size(44.dp).clip(CircleShape).background(s.accent.copy(alpha = 0.18f)).clickable { navHaptic(); ctl.goUp() },
            contentAlignment = Alignment.Center
        ) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Up", tint = s.accent, modifier = Modifier.size(20.dp)) }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(where, style = MaterialTheme.typography.titleSmall, color = s.palette.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("${conn.protocol.label} · ${conn.host}", style = MaterialTheme.typography.bodySmall, color = s.palette.textSecondary, maxLines = 1)
        }
        Box(
            Modifier.size(44.dp).clip(CircleShape).clickable { navHaptic(); ctl.close() },
            contentAlignment = Alignment.Center
        ) { Icon(Icons.Rounded.Close, "Disconnect", tint = s.palette.textSecondary, modifier = Modifier.size(20.dp)) }
    }
}

@Composable
private fun CoverPlayFileRow(f: NetFile, onClick: () -> Unit) {
    val s = LocalAppUiSettings.current
    CoverPlaySelectableCard(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            CoverPlayBrandIconContainer(active = f.isDirectory || f.isVideo(), size = 40.dp) {
                Icon(
                    painterResource(if (f.isDirectory) R.drawable.ic_network else R.drawable.ic_play_custom),
                    null,
                    tint = if (f.isDirectory || f.isVideo()) s.accent else s.palette.textTertiary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(f.name, style = MaterialTheme.typography.titleSmall, color = s.palette.textPrimary, maxLines = 2, overflow = TextOverflow.Ellipsis)
                if (!f.isDirectory) Text(formatSize(f.size), style = MaterialTheme.typography.bodySmall, color = s.palette.textTertiary)
            }
        }
    }
}

@Composable
private fun CoverPlayConnectionForm(initial: NetConnection, isNew: Boolean, onCancel: () -> Unit, onSave: (NetConnection) -> Unit) {
    val palette = LocalAppUiSettings.current.palette
    var name by remember { mutableStateOf(initial.name) }
    var protocol by remember { mutableStateOf(initial.protocol) }
    var host by remember { mutableStateOf(initial.host) }
    var port by remember { mutableStateOf(initial.port.toString()) }
    var user by remember { mutableStateOf(initial.username) }
    var pass by remember { mutableStateOf(initial.password) }
    var path by remember { mutableStateOf(initial.path) }
    var anonymous by remember { mutableStateOf(initial.anonymous) }
    var https by remember { mutableStateOf(initial.https) }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = BrandAccent,
        unfocusedBorderColor = palette.outlineMedium,
        focusedContainerColor = glassFieldFill(),
        unfocusedContainerColor = glassFieldFill(),
        cursorColor = BrandAccent
    )
    @Composable
    fun field(value: String, label: String, onChange: (String) -> Unit, number: Boolean = false, secret: Boolean = false, hint: String? = null) {
        OutlinedTextField(
            value = value, onValueChange = onChange, singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(label) },
            placeholder = hint?.let { { Text(it) } },
            shape = RoundedCornerShape(16.dp), colors = fieldColors,
            visualTransformation = if (secret) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = if (number) KeyboardType.Number else if (secret) KeyboardType.Password else KeyboardType.Text)
        )
        Spacer(Modifier.height(8.dp))
    }

    CoverPlaySelectableCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().padding(18.dp)) {
            Text(
                if (isNew) "New connection" else "Edit connection",
                style = MaterialTheme.typography.titleLarge,
                color = palette.textPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(14.dp))
            CoverPlaySegmented(
                options = NetProtocol.values().toList(),
                selected = protocol,
                label = { it.label },
                onSelect = { p ->
                    if (port == protocol.defaultPort.toString()) port = p.defaultPort.toString()
                    protocol = p
                }
            )
            Spacer(Modifier.height(12.dp))
            field(name, "Name", { name = it })
            field(host, "Host / IP", { host = it }, hint = "192.168.1.10")
            field(port, "Port", { port = it.filter(Char::isDigit) }, number = true)
            field(
                path,
                when (protocol) { NetProtocol.SMB -> "Share name"; else -> "Start folder (optional)" },
                { path = it },
                hint = if (protocol == NetProtocol.SMB) "Media" else "/"
            )
            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Anonymous", color = palette.textPrimary, modifier = Modifier.weight(1f))
                CoverPlaySwitch(checked = anonymous, onCheckedChange = { anonymous = it })
            }
            if (!anonymous) {
                Spacer(Modifier.height(6.dp))
                field(user, "Username", { user = it })
                field(pass, "Password", { pass = it }, secret = true)
            }
            if (protocol == NetProtocol.WEBDAV) {
                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Use HTTPS", color = palette.textPrimary, modifier = Modifier.weight(1f))
                    CoverPlaySwitch(
                        checked = https,
                        onCheckedChange = {
                            https = it
                            if (port == "80" && it) port = "443" else if (port == "443" && !it) port = "80"
                        }
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onCancel) { Text("Cancel", color = palette.textTertiary, fontWeight = FontWeight.SemiBold) }
                Spacer(Modifier.width(16.dp))
                CoverPlayButton(
                    text = "Save",
                    enabled = host.isNotBlank() && port.toIntOrNull() != null && (protocol != NetProtocol.SMB || path.isNotBlank()),
                    onClick = {
                        onSave(
                            initial.copy(
                                name = name.ifBlank { host },
                                protocol = protocol,
                                host = host.trim(),
                                port = port.toInt(),
                                username = if (anonymous) "" else user,
                                password = if (anonymous) "" else pass,
                                path = path.trim(),
                                anonymous = anonymous,
                                https = https
                            )
                        )
                    }
                )
            }
        }
    }
}
