package com.raman.CoverPlay.network

import android.content.Context
import android.net.Uri
import android.util.Xml
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.PlaybackException
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.BaseDataSource
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSourceException
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.TransferListener
import com.hierynomus.msdtyp.AccessMask
import com.hierynomus.mssmb2.SMB2CreateDisposition
import com.hierynomus.mssmb2.SMB2Dialect
import com.hierynomus.mssmb2.SMB2ShareAccess
import com.hierynomus.smbj.SMBClient
import com.hierynomus.smbj.SmbConfig
import com.hierynomus.smbj.auth.AuthenticationContext
import com.hierynomus.smbj.connection.Connection
import com.hierynomus.smbj.session.Session
import com.hierynomus.smbj.share.DiskShare
import java.io.IOException
import java.io.InputStream
import java.util.EnumSet
import java.util.UUID
import java.util.concurrent.TimeUnit
import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPReply
import org.json.JSONArray
import org.json.JSONObject
import org.xmlpull.v1.XmlPullParser

enum class NetProtocol(val label: String, val defaultPort: Int) {
    SMB("SMB", 445),
    FTP("FTP", 21),
    WEBDAV("WebDAV", 80)
}

data class NetConnection(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val protocol: NetProtocol,
    val host: String,
    val port: Int,
    val username: String = "",
    val password: String = "",
    val path: String = "",
    val anonymous: Boolean = false,
    val https: Boolean = false
)

data class NetFile(val name: String, val path: String, val isDirectory: Boolean, val size: Long)

private val VIDEO_EXTENSIONS = setOf(
    "mp4", "m4v", "mkv", "avi", "mov", "wmv", "flv", "webm", "mpeg", "mpg", "3gp", "ts", "m2ts", "mts", "ogv", "vob", "divx"
)

fun NetFile.isVideo(): Boolean = !isDirectory && name.substringAfterLast('.', "").lowercase() in VIDEO_EXTENSIONS

object NetworkStore {
    private const val PREFS = "network_connections"
    private const val KEY = "items"

    fun load(context: Context): List<NetConnection> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, null) ?: return emptyList()
        return runCatching {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                NetConnection(
                    id = o.getString("id"),
                    name = o.getString("name"),
                    protocol = NetProtocol.valueOf(o.getString("protocol")),
                    host = o.getString("host"),
                    port = o.getInt("port"),
                    username = o.optString("username"),
                    password = o.optString("password"),
                    path = o.optString("path"),
                    anonymous = o.optBoolean("anonymous"),
                    https = o.optBoolean("https")
                )
            }
        }.getOrDefault(emptyList())
    }

    fun save(context: Context, items: List<NetConnection>) {
        val arr = JSONArray()
        items.forEach {
            arr.put(JSONObject().apply {
                put("id", it.id); put("name", it.name); put("protocol", it.protocol.name)
                put("host", it.host); put("port", it.port); put("username", it.username)
                put("password", it.password); put("path", it.path)
                put("anonymous", it.anonymous); put("https", it.https)
            })
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY, arr.toString()).apply()
        NetClients.dropAll()
    }

    fun find(context: Context, id: String?): NetConnection? = load(context).firstOrNull { it.id == id }
}

object NetworkAuth {
    private val creds = java.util.concurrent.ConcurrentHashMap<String, String>()

    fun register(conn: NetConnection) {
        if (conn.protocol != NetProtocol.WEBDAV) return
        val key = "${conn.host.lowercase()}:${conn.port}"
        if (conn.anonymous || conn.username.isEmpty()) creds.remove(key)
        else creds[key] = Credentials.basic(conn.username, conn.password)
    }

    val interceptor = Interceptor { chain ->
        val req = chain.request()
        val header = if (req.header("Authorization") == null) creds["${req.url.host.lowercase()}:${req.url.port}"] else null
        chain.proceed(if (header != null) req.newBuilder().header("Authorization", header).build() else req)
    }
}

interface NetFileHandle {
    val length: Long
    fun read(position: Long, buffer: ByteArray, offset: Int, length: Int): Int
    fun close()
}

interface NetClient {
    fun list(rel: String): List<NetFile>
    fun openFile(rel: String): NetFileHandle
    fun playUri(rel: String): Uri
    fun close() {}
}

object NetClients {
    private val cache = HashMap<String, NetClient>()

    @Synchronized
    fun get(conn: NetConnection): NetClient = cache.getOrPut(conn.id) {
        when (conn.protocol) {
            NetProtocol.SMB -> SmbNetClient(conn)
            NetProtocol.FTP -> FtpNetClient(conn)
            NetProtocol.WEBDAV -> WebDavNetClient(conn)
        }
    }

    @Synchronized
    fun dropAll() {
        cache.values.forEach { runCatching { it.close() } }
        cache.clear()
    }
}

private fun sortFiles(list: List<NetFile>) =
    list.sortedWith(compareByDescending<NetFile> { it.isDirectory }.thenBy { it.name.lowercase() })

private fun joinPath(base: String, rel: String): String {
    val b = base.trim('/'); val r = rel.trim('/')
    return when {
        b.isEmpty() -> r
        r.isEmpty() -> b
        else -> "$b/$r"
    }
}

fun cpnetUri(connId: String, rel: String): Uri =
    Uri.Builder().scheme("cpnet").authority(connId).path("/" + rel.trim('/')).build()

private class WebDavNetClient(private val conn: NetConnection) : NetClient {
    private val http = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private fun base() = (if (conn.https) "https" else "http") + "://${conn.host}:${conn.port}"

    private fun url(rel: String): String {
        val full = joinPath(conn.path, rel)
        val encoded = full.split('/').filter { it.isNotEmpty() }.joinToString("/") { Uri.encode(it) }
        return base() + "/" + encoded
    }

    override fun playUri(rel: String): Uri {
        NetworkAuth.register(conn)
        return Uri.parse(url(rel))
    }

    override fun openFile(rel: String): NetFileHandle = throw UnsupportedOperationException("WebDAV streams over HTTP")

    override fun list(rel: String): List<NetFile> {
        NetworkAuth.register(conn)
        val target = url(rel).let { if (it.endsWith("/")) it else "$it/" }
        val builder = Request.Builder().url(target)
            .header("Depth", "1")
            .method("PROPFIND", "".toRequestBody(null))
        if (!conn.anonymous && conn.username.isNotEmpty()) builder.header("Authorization", Credentials.basic(conn.username, conn.password))
        http.newCall(builder.build()).execute().use { resp ->
            if (resp.code == 401) throw IOException("Authentication failed")
            if (resp.code != 207 && !resp.isSuccessful) throw IOException("Server replied ${resp.code}")
            val body = resp.body?.byteStream() ?: return emptyList()
            return sortFiles(parse(body, rel))
        }
    }

    private fun parse(stream: InputStream, rel: String): List<NetFile> {
        val parser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true)
        parser.setInput(stream, "UTF-8")
        val out = ArrayList<NetFile>()
        var href = ""; var length = 0L; var dir = false
        var inResponse = false
        var first = true
        var ev = parser.eventType
        while (ev != XmlPullParser.END_DOCUMENT) {
            if (ev == XmlPullParser.START_TAG) {
                when (parser.name.lowercase()) {
                    "response" -> { inResponse = true; href = ""; length = 0L; dir = false }
                    "href" -> if (inResponse) href = parser.nextText()
                    "getcontentlength" -> if (inResponse) length = parser.nextText().trim().toLongOrNull() ?: 0L
                    "collection" -> if (inResponse) dir = true
                }
            } else if (ev == XmlPullParser.END_TAG && parser.name.equals("response", true)) {
                inResponse = false
                if (first) {
                    first = false
                } else {
                    val name = Uri.decode(href.trimEnd('/').substringAfterLast('/'))
                    if (name.isNotEmpty()) out += NetFile(name, joinPath(rel, name), dir, length)
                }
            }
            ev = parser.next()
        }
        return out
    }
}

private fun newFtp(conn: NetConnection, soTimeoutMs: Int = 60_000): FTPClient {
    val c = FTPClient()
    c.controlEncoding = "UTF-8"
    c.connectTimeout = 15_000
    c.defaultTimeout = soTimeoutMs
    c.connect(conn.host, conn.port)
    if (!FTPReply.isPositiveCompletion(c.replyCode)) { c.disconnect(); throw IOException("FTP server refused the connection") }
    val ok = if (conn.anonymous) c.login("anonymous", "") else c.login(conn.username, conn.password)
    if (!ok) { c.disconnect(); throw IOException("Login failed — check username and password") }
    c.setFileType(FTP.BINARY_FILE_TYPE)
    c.enterLocalPassiveMode()
    runCatching { c.sendCommand("OPTS UTF8 ON") }
    c.bufferSize = 64 * 1024
    c.soTimeout = soTimeoutMs
    return c
}

private fun ftpPath(conn: NetConnection, rel: String): String {
    val p = joinPath(conn.path, rel)
    return "/$p"
}

private class FtpNetClient(private val conn: NetConnection) : NetClient {
    override fun playUri(rel: String): Uri = cpnetUri(conn.id, rel)

    override fun list(rel: String): List<NetFile> {
        val c = newFtp(conn)
        try {
            return sortFiles(
                c.listFiles(ftpPath(conn, rel)).orEmpty()
                    .filter { it.name != "." && it.name != ".." }
                    .map { NetFile(it.name, joinPath(rel, it.name), it.isDirectory, it.size) }
            )
        } finally {
            runCatching { c.logout() }; runCatching { c.disconnect() }
        }
    }

    override fun openFile(rel: String): NetFileHandle {
        val path = ftpPath(conn, rel)
        val size = newFtp(conn).let { c ->
            try { c.listFiles(path)?.firstOrNull { !it.isDirectory }?.size ?: -1L }
            finally { runCatching { c.disconnect() } }
        }
        if (size < 0) throw IOException("File not found")
        return object : NetFileHandle {
            override val length = size
            private var client: FTPClient? = null
            private var stream: InputStream? = null
            private var pos = -1L

            private fun reopen(at: Long) {
                closeStream()
                val c = newFtp(conn, 120_000)
                c.setRestartOffset(at)
                val s = c.retrieveFileStream(path)
                if (s == null) { runCatching { c.disconnect() }; throw IOException("FTP could not open the file (reply ${c.replyCode})") }
                client = c; stream = s; pos = at
            }

            private fun closeStream() {
                runCatching { stream?.close() }
                runCatching { client?.disconnect() }
                stream = null; client = null; pos = -1L
            }

            override fun read(position: Long, buffer: ByteArray, offset: Int, length: Int): Int {
                if (stream == null || pos != position) reopen(position)
                val n = stream!!.read(buffer, offset, length)
                if (n > 0) pos += n
                return n
            }

            override fun close() = closeStream()
        }
    }
}

private class SmbNetClient(private val conn: NetConnection) : NetClient {
    private var client: SMBClient? = null
    private var connection: Connection? = null
    private var session: Session? = null
    private var share: DiskShare? = null

    override fun playUri(rel: String): Uri = cpnetUri(conn.id, rel)

    @Synchronized
    private fun share(): DiskShare {
        share?.let { if (it.isConnected) return it }
        close()
        val shareName = conn.path.trim('/')
        if (shareName.isEmpty()) throw IOException("Share name required (e.g. Media)")
        if (shareName.contains('/')) throw IOException("Use only the share name; open folders after connecting")
        val cfg = SmbConfig.builder()
            .withTimeout(30, TimeUnit.SECONDS)
            .withSoTimeout(35, TimeUnit.SECONDS)
            .withDialects(SMB2Dialect.SMB_3_1_1, SMB2Dialect.SMB_3_0_2, SMB2Dialect.SMB_3_0, SMB2Dialect.SMB_2_1, SMB2Dialect.SMB_2_0_2)
            .withDfsEnabled(false)
            .withMultiProtocolNegotiate(true)
            .withSigningRequired(false)
            .withEncryptData(false)
            .build()
        val c = SMBClient(cfg)
        val conx = c.connect(conn.host, conn.port)
        val auth = if (conn.anonymous) AuthenticationContext.anonymous()
        else AuthenticationContext(conn.username, conn.password.toCharArray(), null)
        val sess = conx.authenticate(auth)
        val s = sess.connectShare(shareName) as? DiskShare ?: throw IOException("'$shareName' is not a disk share")
        client = c; connection = conx; session = sess; share = s
        return s
    }

    override fun list(rel: String): List<NetFile> {
        val s = share()
        return sortFiles(
            s.list(rel.trim('/').replace('/', '\\'))
                .filter { it.fileName != "." && it.fileName != ".." && !it.fileName.endsWith("$") }
                .map {
                    val dir = it.fileAttributes and 0x10L != 0L
                    NetFile(it.fileName, joinPath(rel, it.fileName), dir, if (dir) 0L else it.endOfFile)
                }
        )
    }

    override fun openFile(rel: String): NetFileHandle {
        val s = share()
        val file = s.openFile(
            rel.trim('/').replace('/', '\\'),
            EnumSet.of(AccessMask.GENERIC_READ),
            null,
            SMB2ShareAccess.ALL,
            SMB2CreateDisposition.FILE_OPEN,
            null
        )
        val size = file.fileInformation.standardInformation.endOfFile
        return object : NetFileHandle {
            override val length = size
            override fun read(position: Long, buffer: ByteArray, offset: Int, length: Int): Int =
                file.read(buffer, position, offset, length)
            override fun close() { runCatching { file.close() } }
        }
    }

    @Synchronized
    override fun close() {
        runCatching { share?.close() }; runCatching { session?.close() }
        runCatching { connection?.close() }; runCatching { client?.close() }
        share = null; session = null; connection = null; client = null
    }
}

@OptIn(UnstableApi::class)
class NetDataSource(private val context: Context) : BaseDataSource( true) {
    private var uri: Uri? = null
    private var handle: NetFileHandle? = null
    private var position = 0L
    private var remaining = 0L
    private var opened = false

    override fun open(dataSpec: DataSpec): Long {
        uri = dataSpec.uri
        transferInitializing(dataSpec)
        try {
            val conn = NetworkStore.find(context, dataSpec.uri.host)
                ?: throw IOException("Network connection no longer exists")
            val h = NetClients.get(conn).openFile(dataSpec.uri.path.orEmpty().trim('/'))
            handle = h
            position = dataSpec.position
            val available = h.length - position
            if (available < 0) throw DataSourceException(PlaybackException.ERROR_CODE_IO_READ_POSITION_OUT_OF_RANGE)
            remaining = if (dataSpec.length != C.LENGTH_UNSET.toLong()) minOf(dataSpec.length, available) else available
        } catch (e: IOException) {
            throw e
        } catch (e: Exception) {
            throw IOException(e.message ?: "Network error", e)
        }
        opened = true
        transferStarted(dataSpec)
        return remaining
    }

    override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
        if (length == 0) return 0
        if (remaining == 0L) return C.RESULT_END_OF_INPUT
        val n = try {
            handle!!.read(position, buffer, offset, minOf(remaining, length.toLong()).toInt())
        } catch (e: IOException) { throw e }
        catch (e: Exception) { throw IOException(e.message ?: "Network read failed", e) }
        if (n <= 0) return C.RESULT_END_OF_INPUT
        position += n; remaining -= n
        bytesTransferred(n)
        return n
    }

    override fun getUri(): Uri? = uri

    override fun close() {
        uri = null
        runCatching { handle?.close() }
        handle = null
        if (opened) { opened = false; transferEnded() }
    }
}

@OptIn(UnstableApi::class)
class NetRoutingDataSourceFactory(
    private val context: Context,
    private val fallback: DataSource.Factory
) : DataSource.Factory {
    override fun createDataSource(): DataSource = object : DataSource {
        private val http = fallback.createDataSource()
        private val net = NetDataSource(context.applicationContext)
        private var active: DataSource? = null

        override fun addTransferListener(l: TransferListener) { http.addTransferListener(l); net.addTransferListener(l) }
        override fun open(dataSpec: DataSpec): Long {
            active = if (dataSpec.uri.scheme == "cpnet") net else http
            return active!!.open(dataSpec)
        }
        override fun read(buffer: ByteArray, offset: Int, length: Int): Int = active!!.read(buffer, offset, length)
        override fun getUri(): Uri? = active?.uri
        override fun getResponseHeaders(): Map<String, List<String>> = active?.responseHeaders ?: emptyMap()
        override fun close() { active?.close(); active = null }
    }
}
