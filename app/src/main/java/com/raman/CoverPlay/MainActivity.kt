package com.raman.CoverPlay

import android.Manifest
import android.app.Activity
import android.app.PictureInPictureParams
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Typeface
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Rational
import android.view.GestureDetector
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.PixelCopy
import android.view.ScaleGestureDetector
import android.view.SurfaceView
import android.view.TextureView
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.Tracks
import androidx.media3.common.VideoSize
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.SeekParameters
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.extractor.DefaultExtractorsFactory
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.CaptionStyleCompat
import androidx.media3.ui.PlayerView
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.VideoFrameDecoder
import coil.request.ImageRequest
import coil.request.videoFrameMillis
import com.raman.CoverPlay.components.CoverPlayCorruptVideoScene
import com.raman.CoverPlay.components.CoverPlaySlider
import com.raman.CoverPlay.components.CoverPlaySwitch
import com.raman.CoverPlay.data.rescanDeviceFolders
import com.raman.CoverPlay.navigation.NavChrome
import com.raman.CoverPlay.network.CoverPlayStreamScreen
import com.raman.CoverPlay.network.NetRoutingDataSourceFactory
import com.raman.CoverPlay.network.NetworkAuth
import com.raman.CoverPlay.network.NetworkNav
import com.raman.CoverPlay.onboarding.CoverPlayAppTutorial
import com.raman.CoverPlay.onboarding.CoverPlayPlayerTutorial
import com.raman.CoverPlay.onboarding.TutorialPrefs
import com.raman.CoverPlay.playback.PlaybackHost
import com.raman.CoverPlay.player.ChapterReader
import com.raman.CoverPlay.player.CoverPlayGestureIndicator
import com.raman.CoverPlay.player.CoverPlayPlayerLockOverlay
import com.raman.CoverPlay.player.CoverPlayPlayerPanel
import com.raman.CoverPlay.player.CoverPlayPlayerPanelCard
import com.raman.CoverPlay.player.CoverPlayPlayerPanelChip
import com.raman.CoverPlay.player.CoverPlayPlayerPanelDivider
import com.raman.CoverPlay.player.CoverPlayPlayerPanelNavRow
import com.raman.CoverPlay.player.CoverPlayPlayerPanelOptionRow
import com.raman.CoverPlay.player.CoverPlayPlayerPanelSection
import com.raman.CoverPlay.player.CoverPlayPlayerPanelToggleRow
import com.raman.CoverPlay.player.CoverPlaySeekFlash
import com.raman.CoverPlay.player.CoverPlaySeekbar
import com.raman.CoverPlay.player.PlayerButton
import com.raman.CoverPlay.player.PlayerZone
import com.raman.CoverPlay.player.SeekbarPlacement
import com.raman.CoverPlay.player.TransportPlacement
import com.raman.CoverPlay.player.VideoChapter
import com.raman.CoverPlay.player.loadPlayerCustomization
import com.raman.CoverPlay.screens.CoverPlayBottomNavigationBar
import com.raman.CoverPlay.screens.CoverPlayDashboardScreen
import com.raman.CoverPlay.screens.CoverPlayFolderListScreen
import com.raman.CoverPlay.screens.CoverPlaySplashScreen
import com.raman.CoverPlay.screens.CoverPlayVideoListScreen
import com.raman.CoverPlay.screens.VideoThumbnailFetcher
import com.raman.CoverPlay.settings.CoverPlayPlaylistsScreen
import com.raman.CoverPlay.settings.CoverPlaySettingsScreen
import com.raman.CoverPlay.settings.Playlist
import com.raman.CoverPlay.settings.UsageTracker
import com.raman.CoverPlay.settings.readSettings
import com.raman.CoverPlay.ui.theme.*
import com.raman.CoverPlay.ui.theme.AppChrome
import com.raman.CoverPlay.ui.theme.BrandCyan
import com.raman.CoverPlay.ui.theme.CoverPlayBrandMark
import com.raman.CoverPlay.ui.theme.CoverPlayButton
import com.raman.CoverPlay.ui.theme.CoverPlayCardSurface
import com.raman.CoverPlay.ui.theme.CoverPlayCinematicBackground
import com.raman.CoverPlay.ui.theme.CoverPlayCinematicPanel
import com.raman.CoverPlay.ui.theme.CoverPlayDialog
import com.raman.CoverPlay.ui.theme.CoverPlayDialogBackdrop
import com.raman.CoverPlay.ui.theme.CoverPlayDialogCloseButton
import com.raman.CoverPlay.ui.theme.CoverPlayDurationBadge
import com.raman.CoverPlay.ui.theme.CoverPlayGlassSurface
import com.raman.CoverPlay.ui.theme.CoverPlayLoader
import com.raman.CoverPlay.ui.theme.CoverPlayMaterialShapes
import com.raman.CoverPlay.ui.theme.CoverPlayQualityBadge
import com.raman.CoverPlay.ui.theme.CoverPlayRotatingShape
import com.raman.CoverPlay.ui.theme.CoverPlayTheme
import com.raman.CoverPlay.ui.theme.CoverPlayTokens
import com.raman.CoverPlay.ui.theme.DialogBlur
import com.raman.CoverPlay.ui.theme.GlassTokens
import com.raman.CoverPlay.ui.theme.GlassWarmup
import com.raman.CoverPlay.ui.theme.LocalAppUiSettings
import com.raman.CoverPlay.ui.theme.LocalOverVideo
import com.raman.CoverPlay.ui.theme.MotionSpec
import com.raman.CoverPlay.ui.theme.aliveClickable
import com.raman.CoverPlay.ui.theme.glassFieldFill
import com.raman.CoverPlay.ui.theme.onFill
import com.raman.CoverPlay.ui.theme.rememberCoverPlayBackdropState
import com.raman.CoverPlay.ui.theme.rememberCoverPlayChromeScrollConnection
import com.raman.CoverPlay.ui.theme.rememberCoverPlayPressScale
import com.raman.CoverPlay.util.rememberCoverPlayHaptic
import com.raman.CoverPlay.util.rememberCoverPlayHapticFeedback
import dev.chrisbanes.haze.HazeState
import io.github.anilbeesetti.nextlib.media3ext.ffdecoder.NextRenderersFactory
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.math.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import org.json.JSONArray
import org.json.JSONObject

private const val RECENTS_PREFS = "recent_videos"
private const val RECENTS_KEY = "items"

private fun guessSubtitleMimeType(context: Context, uri: Uri): String {
    val displayName = try {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val idx = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (idx >= 0 && cursor.moveToFirst()) cursor.getString(idx) else null
        }
    } catch (_: Exception) {
        null
    } ?: uri.lastPathSegment.orEmpty()

    return when {
        displayName.endsWith(".vtt", ignoreCase = true) -> MimeTypes.TEXT_VTT
        displayName.endsWith(".ass", ignoreCase = true) ||
                displayName.endsWith(".ssa", ignoreCase = true) -> MimeTypes.TEXT_SSA
        displayName.endsWith(".ttml", ignoreCase = true) ||
                displayName.endsWith(".dfxp", ignoreCase = true) -> MimeTypes.APPLICATION_TTML
        else -> MimeTypes.APPLICATION_SUBRIP
    }
}
private const val RECENTS_MAX_ITEMS = 20
private const val RECENTS_KEEP_DAYS = 7L
private const val RECENTS_KEEP_MS = RECENTS_KEEP_DAYS * 24L * 60L * 60L * 1000L

private const val OPENSUBTITLES_API_KEY = "wacgn80dDk8d54nmhTOUSEW67rz4AumW"
private const val OPENSUBTITLES_USER_AGENT = "CoverPlay v3.0.5"

fun formatFileName(name: String): String {
    val nameWithoutExt = name.substringBeforeLast(".")

    val tags = listOf(
        "1080p", "720p", "480p", "2160p", "4k", "uhd", "bluray", "brrip", "webrip", "h264", "x264", "h265", "x265",
        "aac", "dts", "dd5.1", "dual.audio", "hindi", "english", "esub", "subbed", "camrip", "hdtv", "web-dl",
        "web", "rip", "hevc", "xvid", "divx", "dvdrip", "scr", "cam", "ts", "telesync", "pdvd", "workprint"
    )

    var cleaned = nameWithoutExt.replace(".", " ").replace("_", " ").replace("-", " ")

    val yearRegex = Regex("\\b(19|20)\\d{2}\\b")
    val match = yearRegex.find(cleaned)
    val year = match?.value

    if (match != null) {
        cleaned = cleaned.substring(0, match.range.first).trim()
    }

    tags.forEach { tag ->
        cleaned = cleaned.replace(Regex("(?i)\\b$tag\\b"), "")
    }

    cleaned = cleaned.replace(Regex("\\s+"), " ").trim()

    cleaned = cleaned.split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }

    return if (year != null) "$cleaned ($year)" else cleaned.ifEmpty { nameWithoutExt }
}

data class VideoFolder(
    val name: String,
    val path: String,
    val videoCount: Int,
    val size: String
)

data class VideoFile(
    val id: Long,
    val name: String,
    val path: String,
    val duration: Long,
    val size: Long,
    val uri: Uri,
    val width: Int = 0,
    val height: Int = 0,
    val dateAdded: Long = 0
)

enum class Screen {
    HOME, FOLDERS, PLAYLISTS, SETTINGS, FOLDER_VIDEOS, PLAYER, NETWORK, STREAM
}

enum class FolderSortOrder(val label: String) {
    NAME_ASC("Name A–Z"),
    NAME_DESC("Name Z–A"),
    MOST_VIDEOS("Most Videos"),
    FEWEST_VIDEOS("Fewest Videos")
}

enum class LayoutMode { LIST, GRID }

enum class HistorySortOrder(val label: String) {
    RECENT("Most Recent"),
    OLDEST("Oldest First"),
    NAME("Name A–Z"),
    DURATION("Longest Duration")
}

enum class VideoSortOrder(val label: String) {
    NAME_ASC("Name A–Z"),
    NAME_DESC("Name Z–A"),
    DATE_NEWEST("Recently Added"),
    SIZE_DESC("Largest Size"),
    DURATION_DESC("Longest Video")
}

enum class VideoActionDialog {
    MENU, RENAME, DELETE, DETAILS, MOVE, ADD_TO_PLAYLIST
}

@Composable
fun CoverPlayBrandTriangle(modifier: Modifier = Modifier, contentDescription: String? = "CoverPlay") {
    CoverPlayBrandMark(modifier = modifier)
}

@Composable
fun CoverPlayTriangleThumbPlaceholder(modifier: Modifier = Modifier, triangleFraction: Float = 0.32f) {
    Box(
        modifier = modifier.background(LocalAppUiSettings.current.palette.surfaceRaisedAlt),
        contentAlignment = Alignment.Center
    ) {
        CoverPlayBrandTriangle(
            modifier = Modifier.fillMaxSize(triangleFraction),
        )
    }
}

object PipState {
    var canEnterPip by mutableStateOf(false)
    var aspectRatio by mutableStateOf(Rational(16, 9))
    var isInPip by mutableStateOf(false)
}

class MainActivity : ComponentActivity() {
    var onVolumeKeyEvent: ((Int, KeyEvent) -> Boolean)? = null
    var intentVideoUri by mutableStateOf<Uri?>(null)

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            if (onVolumeKeyEvent?.invoke(keyCode, event) == true) return true
        }
        return super.onKeyDown(keyCode, event)
    }

    @UnstableApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent(intent)
        enableEdgeToEdge()
        setContent {
            CoverPlayTheme {
                CoverPlayMainApp(intentVideoUri) { intentVideoUri = null }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        when (intent?.action) {
            Intent.ACTION_VIEW -> intentVideoUri = intent.data
            Intent.ACTION_SEND -> {
                val stream = if (Build.VERSION.SDK_INT >= 33)
                    intent.getParcelableExtra(Intent.EXTRA_STREAM, android.net.Uri::class.java)
                else @Suppress("DEPRECATION") intent.getParcelableExtra<android.net.Uri>(Intent.EXTRA_STREAM)
                intentVideoUri = stream ?: intent.getStringExtra(Intent.EXTRA_TEXT)
                    ?.trim()?.takeIf { it.startsWith("http", true) || it.startsWith("rtsp", true) }
                    ?.let { android.net.Uri.parse(it) }
            }
        }
    }

    fun enterPipModeNow() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        if (!PipState.canEnterPip) return
        val params = PictureInPictureParams.Builder()
            .setAspectRatio(PipState.aspectRatio)
            .build()
        try {
            PipState.isInPip = true
            enterPictureInPictureMode(params)
        } catch (e: Exception) {
            PipState.isInPip = false
            android.util.Log.e("PiP", "enterPictureInPictureMode failed", e)
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (PipState.canEnterPip) enterPipModeNow()
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: Configuration) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        PipState.isInPip = isInPictureInPictureMode
    }
}

@UnstableApi
@Composable
fun CoverPlayMainApp(externalVideoUri: Uri? = null, onConsumed: () -> Unit = {}) {
    val initialShowSplash = LocalAppUiSettings.current.showSplash
    var showSplash by remember { mutableStateOf(initialShowSplash) }

    AnimatedContent(
        targetState = showSplash,
        transitionSpec = {
            fadeIn(animationSpec = tween(500)) togetherWith
                    fadeOut(animationSpec = tween(500))
        },
        label = "SplashTransition"
    ) { isShowingSplash ->
        if (isShowingSplash) {
            CoverPlaySplashScreen(onAnimationFinish = { showSplash = false })
        } else {
            CoverPlayMainContent(externalVideoUri, onConsumed)
        }
    }
}

@UnstableApi
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@Composable
fun CoverPlayMainContent(externalVideoUri: Uri? = null, onConsumed: () -> Unit = {}) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("coverplay_settings_v3", android.content.Context.MODE_PRIVATE) }
    var settings by remember { mutableStateOf(readSettings(context)) }

    DisposableEffect(Unit) {
        GlassWarmup.ready = false
        onDispose {}
    }

    LaunchedEffect(Unit) {
        delay(1200L)
        GlassWarmup.ready = true
    }

    DisposableEffect(prefs) {
        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
            settings = readSettings(context)
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    val imageLoader = remember {
        ImageLoader.Builder(context)
            .dispatcher(Dispatchers.IO.limitedParallelism(2))
            .components {
                add(VideoThumbnailFetcher.Factory(context))
                add(VideoFrameDecoder.Factory())
            }
            .memoryCache {
                coil.memory.MemoryCache.Builder(context)
                    .maxSizePercent(0.20)
                    .build()
            }
            .diskCache {
                coil.disk.DiskCache.Builder()
                    .directory(context.cacheDir.resolve("thumbnail_cache"))
                    .maxSizePercent(0.05)
                    .build()
            }
            .bitmapConfig(android.graphics.Bitmap.Config.RGB_565)
            .allowRgb565(true)
            .crossfade(true)
            .build().also { coil.Coil.setImageLoader(it) }
    }

    CompositionLocalProvider(coil.compose.LocalImageLoader provides imageLoader) {
        var currentScreen by remember { mutableStateOf(Screen.HOME) }
        var previousScreen by remember { mutableStateOf<Screen?>(null) }
        var selectedFolder by remember { mutableStateOf<VideoFolder?>(null) }
        var selectedVideo by remember { mutableStateOf<VideoFile?>(null) }
        val networkNav = remember { NetworkNav() }

        val folders = remember { mutableStateListOf<VideoFolder>() }
        val folderVideos = remember { mutableStateListOf<VideoFile>() }
        val recentEntries = remember { mutableStateListOf<RecentEntry>() }
        val allVideos = remember { mutableStateListOf<VideoFile>() }

        var isSearchActive by remember { mutableStateOf(false) }
        var searchQuery by remember { mutableStateOf("") }

        val scope = rememberCoroutineScope()

        val usageOwner = LocalLifecycleOwner.current
        LaunchedEffect(usageOwner) {
            usageOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                var last = System.currentTimeMillis()
                try {
                    while (true) {
                        delay(5_000L)
                        val now = System.currentTimeMillis()
                        UsageTracker.addTime(context, now - last)
                        last = now
                    }
                } finally {
                    UsageTracker.addTime(context, System.currentTimeMillis() - last)
                }
            }
        }

        val keepDaysSetting = settings["recents_keep_days"] as? Int ?: 7
        LaunchedEffect(keepDaysSetting) {
            val cutoff = System.currentTimeMillis() - keepDaysSetting * 24L * 60L * 60L * 1000L
            if (recentEntries.removeAll { it.playedAt < cutoff }) saveRecentEntries(context, recentEntries)
        }

        var showAppTour by remember { mutableStateOf(false) }
        LaunchedEffect(usageOwner) {
            if (!TutorialPrefs.appSeen(context)) {
                usageOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                    delay(1200L)
                    if (currentScreen != Screen.PLAYER && !TutorialPrefs.appSeen(context)) showAppTour = true
                }
            }
        }
        if (showAppTour) {
            CoverPlayAppTutorial(onFinish = { TutorialPrefs.markApp(context); showAppTour = false })
        }

        val permissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            if (permissions.values.all { it }) {
                scope.launch(Dispatchers.IO) {
                    fetchVideoFolders(context, folders)
                    fetchAllVideos(context, allVideos)
                }
            }
        }

        LaunchedEffect(externalVideoUri) {
            if (externalVideoUri != null) {
                val video = createVideoFileFromUri(context, externalVideoUri)
                selectedVideo = video
                addToRecents(context, video, recentEntries)
                isSearchActive = false
                searchQuery = ""
                currentScreen = Screen.PLAYER
                onConsumed()
            }
        }

        LaunchedEffect(Unit) {
            withContext(Dispatchers.IO) {
                val recents = loadRecentEntries(context)
                withContext(Dispatchers.Main) {
                    recentEntries.clear()
                    recentEntries.addAll(recents)
                }

                delay(400)

                val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    arrayOf(Manifest.permission.READ_MEDIA_VIDEO)
                } else {
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                }

                if (permissions.all {
                        ContextCompat.checkSelfPermission(
                            context,
                            it
                        ) == PackageManager.PERMISSION_GRANTED
                    }) {
                    fetchVideoFolders(context, folders)
                    fetchAllVideos(context, allVideos)
                } else {
                    permissionLauncher.launch(permissions)
                }
            }
        }

        val backdrop = rememberCoverPlayBackdropState()
        val hazeState = remember { HazeState() }
        var navCollapsed by remember { mutableStateOf(false) }
        val navScroll = remember {
            object : NestedScrollConnection {
                private var acc = 0f
                override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                    val dy = consumed.y
                    if (dy == 0f) return Offset.Zero
                    if ((dy < 0f) != (acc < 0f)) acc = 0f
                    acc += dy
                    if (acc < -24f) navCollapsed = true
                    else if (acc > 24f) navCollapsed = false
                    return Offset.Zero
                }
            }
        }
        SideEffect { NavChrome.collapsed = navCollapsed }
        LaunchedEffect(navCollapsed) {
            androidx.compose.animation.core.animate(
                initialValue = NavChrome.fold,
                targetValue = if (navCollapsed) 1f else 0f,
                animationSpec = NavChrome.foldSpring
            ) { v, _ -> NavChrome.fold = v }
        }
        LaunchedEffect(currentScreen) { navCollapsed = false; AppChrome.reveal() }

        var rescanning by remember { mutableStateOf(false) }
        val rescanLibraries: () -> Unit = {
            if (!rescanning) {
                rescanning = true
                scope.launch(Dispatchers.IO) {
                    try {
                        rescanDeviceFolders(context)
                        fetchVideoFolders(context, folders)
                        fetchAllVideos(context, allVideos)
                    } catch (_: Exception) {}
                    withContext(Dispatchers.Main) {
                        rescanning = false
                        android.widget.Toast.makeText(context, "Libraries refreshed · ${allVideos.size} videos", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        val openPickedVideo: (Uri) -> Unit = { uri ->
            val video = createVideoFileFromUri(context, uri)
            selectedVideo = video
            addToRecents(context, video, recentEntries)
            isSearchActive = false
            searchQuery = ""
            previousScreen = Screen.HOME
            currentScreen = Screen.PLAYER
        }
        val chromeScroll = rememberCoverPlayChromeScrollConnection()

        var browserTitle by remember { mutableStateOf<String?>(null) }
        var browserSubtitle by remember { mutableStateOf<String?>(null) }

        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { innerPadding ->
            val dlgBlur by animateDpAsState(if (DialogBlur.count > 0) 18.dp else 0.dp, tween(250), label = "dlgBlur")
            val ambientVideoUri = remember(recentEntries.size, selectedVideo) {
                selectedVideo?.uri ?: recentEntries.firstOrNull()?.video?.uri
            }
            CoverPlayCinematicBackground(
                modifier = if (dlgBlur > 0.dp) Modifier.blur(dlgBlur) else Modifier,
                ambientArtUri = ambientVideoUri,
                backdrop = backdrop,
                hazeState = hazeState,
                overlay = {
                    AnimatedVisibility(
                        visible = currentScreen != Screen.PLAYER,
                        enter = slideInVertically(animationSpec = tween(220, easing = FastOutSlowInEasing)) { it } + fadeIn(tween(220)),
                        exit = slideOutVertically(animationSpec = tween(180, easing = FastOutSlowInEasing)) { it } + fadeOut(tween(180)),
                        modifier = Modifier.align(Alignment.BottomCenter)
                    ) {
                        CoverPlayBottomNavigationBar(
                            currentScreen = currentScreen,
                            backdrop = backdrop,
                            hazeState = hazeState,
                            onScreenSelected = {
                                if (it == Screen.HOME) {
                                    selectedFolder = null
                                }
                                isSearchActive = false
                                searchQuery = ""
                                currentScreen = it
                            }
                        )
                    }
                }
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    val topPadding = if (currentScreen == Screen.PLAYER) 0.dp else innerPadding.calculateTopPadding()
                    Box(modifier = Modifier.padding(top = topPadding).nestedScroll(navScroll).nestedScroll(chromeScroll)) {
                        AnimatedContent(
                            targetState = currentScreen,
                            transitionSpec = {
                                val ease = MotionSpec.EmphasizedEasing
                                fun depth(s: Screen): Float = when (s) {
                                    Screen.HOME -> 0f
                                    Screen.FOLDERS -> 1f
                                    Screen.STREAM -> 1f
                                    Screen.FOLDER_VIDEOS -> 0.5f
                                    Screen.PLAYLISTS -> 2f
                                    Screen.SETTINGS -> 3f
                                    Screen.NETWORK -> 1.5f
                                    else -> 0f
                                }
                                val forward = depth(targetState) >= depth(initialState)
                                val isDrill = (initialState == Screen.HOME && targetState == Screen.FOLDER_VIDEOS) ||
                                        (initialState == Screen.FOLDER_VIDEOS && targetState == Screen.HOME) ||
                                        (initialState == Screen.STREAM && targetState == Screen.NETWORK) ||
                                        (initialState == Screen.NETWORK && targetState == Screen.STREAM)

                                when {
                                    targetState == Screen.PLAYER -> (
                                            (fadeIn(tween(280, easing = ease)) + scaleIn(tween(380, easing = ease), initialScale = 0.92f)) togetherWith
                                                    (fadeOut(tween(160)) + scaleOut(tween(300, easing = ease), targetScale = 0.97f))
                                            ).apply { targetContentZIndex = 1f } using SizeTransform(clip = false)

                                    initialState == Screen.PLAYER -> (
                                            (fadeIn(tween(260, easing = ease)) + scaleIn(tween(340, easing = ease), initialScale = 0.97f)) togetherWith
                                                    (fadeOut(tween(180)) + scaleOut(tween(300, easing = ease), targetScale = 0.94f))
                                            ).apply { targetContentZIndex = -1f } using SizeTransform(clip = false)

                                    isDrill && forward -> (
                                            (slideInHorizontally(tween(380, easing = ease)) { it / 4 } + fadeIn(tween(260, easing = ease))) togetherWith
                                                    (slideOutHorizontally(tween(380, easing = ease)) { -it / 6 } + fadeOut(tween(200)))
                                            ).apply { targetContentZIndex = 1f } using SizeTransform(clip = false)

                                    isDrill -> (
                                            (slideInHorizontally(tween(380, easing = ease)) { -it / 6 } + fadeIn(tween(260, easing = ease))) togetherWith
                                                    (slideOutHorizontally(tween(380, easing = ease)) { it / 4 } + fadeOut(tween(200)))
                                            ).apply { targetContentZIndex = -1f } using SizeTransform(clip = false)

                                    else -> (
                                            fadeIn(tween(220, easing = ease)) togetherWith fadeOut(tween(220, easing = ease))
                                            ) using SizeTransform(clip = false)
                                }
                            },
                            label = "ScreenTransition"
                        ) { screen ->
                            when (screen) {
                                Screen.HOME -> {
                                    CoverPlayDashboardScreen(
                                        recentEntries = recentEntries,
                                        allVideos = allVideos,
                                        allFolders = folders,
                                        isSearchActive = isSearchActive,
                                        searchQuery = searchQuery,
                                        onSearchQueryChange = { searchQuery = it },
                                        onToggleSearch = {
                                            isSearchActive = !isSearchActive
                                            if (!isSearchActive) searchQuery = ""
                                        },
                                        onVideoClick = { video: VideoFile ->
                                            selectedVideo = video
                                            addToRecents(context, video, recentEntries)
                                            isSearchActive = false
                                            searchQuery = ""
                                            previousScreen = Screen.HOME
                                            browserTitle = java.io.File(video.path).parentFile?.name ?: "Folder"
                                            browserSubtitle = "Directory"
                                            scope.launch(Dispatchers.IO) {
                                                val folder = java.io.File(video.path).parent ?: ""
                                                if (folder.isNotEmpty()) fetchVideosInFolder(context, folder, folderVideos)
                                            }
                                            currentScreen = Screen.PLAYER
                                        },
                                        onVideoChanged = {
                                            scope.launch(Dispatchers.IO) {
                                                fetchVideoFolders(context, folders)
                                                fetchAllVideos(context, allVideos)
                                            }
                                        },
                                        onFolderClick = { folder ->
                                            selectedFolder = folder
                                            isSearchActive = false
                                            searchQuery = ""
                                            previousScreen = Screen.HOME
                                            currentScreen = Screen.FOLDER_VIDEOS
                                            scope.launch(Dispatchers.IO) {
                                                fetchVideosInFolder(context, folder.path, folderVideos)
                                            }
                                        },
                                        onOpenVideo = openPickedVideo,
                                        onRescan = rescanLibraries,
                                        rescanning = rescanning,
                                        onOpenNetwork = { currentScreen = Screen.STREAM },
                                        onPlayUrl = { url: String ->
                                            selectedVideo = VideoFile(
                                                id = System.currentTimeMillis(),
                                                name = url.substringAfterLast("/").substringBefore("?").ifBlank { "Remote Video" },
                                                path = url,
                                                duration = 0,
                                                size = 0,
                                                uri = Uri.parse(url)
                                            )
                                            previousScreen = Screen.HOME
                                            currentScreen = Screen.PLAYER
                                        }
                                    )
                                }

                                Screen.FOLDERS -> {
                                    BackHandler { currentScreen = Screen.HOME }
                                    CoverPlayFolderListScreen(
                                        folders = folders,
                                        allVideos = allVideos,
                                        isSearchActive = isSearchActive,
                                        searchQuery = searchQuery,
                                        onSearchQueryChange = { searchQuery = it },
                                        onToggleSearch = {
                                            isSearchActive = !isSearchActive
                                            if (!isSearchActive) searchQuery = ""
                                        },
                                        onFolderClick = { folder ->
                                            selectedFolder = folder
                                            isSearchActive = false
                                            searchQuery = ""
                                            previousScreen = Screen.FOLDERS
                                            currentScreen = Screen.FOLDER_VIDEOS
                                            scope.launch(Dispatchers.IO) {
                                                fetchVideosInFolder(context, folder.path, folderVideos)
                                            }
                                        },
                                        onVideoClick = { video: VideoFile ->
                                            selectedVideo = video
                                            addToRecents(context, video, recentEntries)
                                            isSearchActive = false
                                            searchQuery = ""
                                            previousScreen = Screen.FOLDERS
                                            browserTitle = java.io.File(video.path).parentFile?.name ?: "Folder"
                                            browserSubtitle = "Directory"
                                            scope.launch(Dispatchers.IO) {
                                                val folder = java.io.File(video.path).parent ?: ""
                                                if (folder.isNotEmpty()) fetchVideosInFolder(context, folder, folderVideos)
                                            }
                                            currentScreen = Screen.PLAYER
                                        },
                                        onVideoChanged = {
                                            scope.launch(Dispatchers.IO) {
                                                fetchVideoFolders(context, folders)
                                                fetchAllVideos(context, allVideos)
                                            }
                                        }
                                    )
                                }

                                Screen.FOLDER_VIDEOS -> {
                                    BackHandler {
                                        isSearchActive = false
                                        searchQuery = ""
                                        currentScreen = Screen.HOME
                                    }
                                    CoverPlayVideoListScreen(
                                        folderName = selectedFolder?.name ?: "Videos",
                                        videos = folderVideos,
                                        isSearchActive = isSearchActive,
                                        searchQuery = searchQuery,
                                        onSearchQueryChange = { searchQuery = it },
                                        onToggleSearch = {
                                            isSearchActive = !isSearchActive
                                            if (!isSearchActive) searchQuery = ""
                                        },
                                        onVideoClick = { video ->
                                            selectedVideo = video
                                            browserTitle = selectedFolder?.name ?: "Folder"
                                            browserSubtitle = "Directory"
                                            addToRecents(context, video, recentEntries)
                                            isSearchActive = false
                                            searchQuery = ""
                                            previousScreen = Screen.FOLDER_VIDEOS
                                            currentScreen = Screen.PLAYER
                                        },
                                        onBack = {
                                            isSearchActive = false
                                            searchQuery = ""
                                            currentScreen = Screen.HOME
                                        },
                                        allFolders = folders,
                                        onVideoChanged = {
                                            scope.launch(Dispatchers.IO) {
                                                selectedFolder?.let { f ->
                                                    fetchVideosInFolder(context, f.path, folderVideos)
                                                }
                                                fetchVideoFolders(context, folders)
                                                fetchAllVideos(context, allVideos)
                                            }
                                        }
                                    )
                                }

                                Screen.PLAYER -> {
                                    selectedVideo?.let { video ->
                                        CoverPlayVideoPlayerScreen(
                                            video = video,
                                            onBack = {
                                                updateOrientation(context, false)
                                                currentScreen = previousScreen ?: Screen.HOME
                                            },
                                            folderVideos = folderVideos.toList(),
                                            onPlayVideo = { newVideo ->
                                                selectedVideo = newVideo
                                                addToRecents(context, newVideo, recentEntries)
                                            },
                                            settings = settings,
                                            browserTitle = browserTitle,
                                            browserSubtitle = browserSubtitle
                                        )
                                    }
                                }

                                Screen.STREAM -> {
                                    BackHandler { currentScreen = Screen.HOME }
                                    CoverPlayStreamScreen(
                                        onPlayUrl = { url: String ->
                                            selectedVideo = VideoFile(
                                                id = System.currentTimeMillis(),
                                                name = url.substringAfterLast("/").substringBefore("?").ifBlank { "Remote Video" },
                                                path = url,
                                                duration = 0,
                                                size = 0,
                                                uri = Uri.parse(url)
                                            )
                                            previousScreen = Screen.STREAM
                                            currentScreen = Screen.PLAYER
                                        },
                                        networkNav = networkNav,
                                        onPlayNetwork = { video, siblings, title ->
                                            selectedVideo = video
                                            folderVideos.clear()
                                            folderVideos.addAll(siblings)
                                            browserTitle = title
                                            browserSubtitle = "Network"
                                            previousScreen = Screen.STREAM
                                            currentScreen = Screen.PLAYER
                                        }
                                    )
                                }

                                Screen.NETWORK -> {
                                    LaunchedEffect(Unit) { currentScreen = Screen.STREAM }
                                }

                                Screen.PLAYLISTS -> {
                                    BackHandler { currentScreen = Screen.HOME }
                                    CoverPlayPlaylistsScreen(
                                        allVideos = allVideos,
                                        onVideoClick = { video, list, title ->
                                            selectedVideo = video
                                            folderVideos.clear()
                                            folderVideos.addAll(list)
                                            browserTitle = title
                                            browserSubtitle = "Playlist"
                                            addToRecents(context, video, recentEntries)
                                            previousScreen = Screen.PLAYLISTS
                                            currentScreen = Screen.PLAYER
                                        }
                                    )
                                }

                                Screen.SETTINGS -> {
                                    BackHandler { currentScreen = Screen.HOME }
                                    CoverPlaySettingsScreen(
                                        onReplayAppTour = { showAppTour = true },
                                        onClearHistory = {
                                            recentEntries.clear()
                                            context.getSharedPreferences(
                                                "recent_videos",
                                                android.content.Context.MODE_PRIVATE
                                            )
                                                .edit().remove("items").apply()
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

data class RecentEntry(val video: VideoFile, val playedAt: Long)

fun addToRecents(context: Context, video: VideoFile, recentEntries: MutableList<RecentEntry>) {
    recentEntries.removeAll { it.video.id == video.id || it.video.uri == video.uri || it.video.path == video.path }
    recentEntries.add(0, RecentEntry(video, System.currentTimeMillis()))
    while (recentEntries.size > RECENTS_MAX_ITEMS) recentEntries.removeAt(RECENTS_MAX_ITEMS)
    saveRecentEntries(context, recentEntries)
}

fun removeFromRecents(context: Context, videoId: Long, recentEntries: MutableList<RecentEntry>) {
    recentEntries.removeAll { it.video.id == videoId }
    saveRecentEntries(context, recentEntries)
}

fun removeMultipleFromRecents(context: Context, videoIds: List<Long>, recentEntries: MutableList<RecentEntry>) {
    recentEntries.removeAll { it.video.id in videoIds }
    saveRecentEntries(context, recentEntries)
}

fun loadRecentEntries(context: Context): List<RecentEntry> {
    val keepDays = context.getSharedPreferences("coverplay_settings_v3", Context.MODE_PRIVATE).getInt("recents_keep_days", 7)
    val cutoff = System.currentTimeMillis() - keepDays * 24L * 60L * 60L * 1000L
    val prefs = context.getSharedPreferences(RECENTS_PREFS, Context.MODE_PRIVATE)
    val saved = prefs.getString(RECENTS_KEY, null) ?: return emptyList()
    val entries = mutableListOf<RecentEntry>()
    var savedCount = 0

    runCatching {
        val items = JSONArray(saved)
        savedCount = items.length()
        for (i in 0 until items.length()) {
            val item = items.getJSONObject(i)
            val playedAt = item.optLong("lastPlayedAt", 0L)
            if (playedAt < cutoff) continue

            entries.add(
                RecentEntry(
                    video = VideoFile(
                        id = item.optLong("id"),
                        name = item.optString("name"),
                        path = item.optString("path"),
                        duration = item.optLong("duration"),
                        size = item.optLong("size"),
                        uri = Uri.parse(item.optString("uri")),
                        width = item.optInt("width"),
                        height = item.optInt("height")
                    ),
                    playedAt = playedAt
                )
            )
        }
    }

    if (entries.size < savedCount) {
        saveRecentEntries(context, entries)
    }

    return entries.take(RECENTS_MAX_ITEMS)
}

fun saveRecentEntries(context: Context, entries: List<RecentEntry>) {
    val items = JSONArray()
    entries.take(RECENTS_MAX_ITEMS).forEach { entry ->
        items.put(
            JSONObject()
                .put("id", entry.video.id)
                .put("name", entry.video.name)
                .put("path", entry.video.path)
                .put("duration", entry.video.duration)
                .put("size", entry.video.size)
                .put("uri", entry.video.uri.toString())
                .put("width", entry.video.width)
                .put("height", entry.video.height)
                .put("lastPlayedAt", entry.playedAt)
        )
    }
    context.getSharedPreferences(RECENTS_PREFS, Context.MODE_PRIVATE)
        .edit()
        .putString(RECENTS_KEY, items.toString())
        .apply()
}

@UnstableApi
@Composable
fun CoverPlaySmoothPlayerScrubber(
    exoPlayer: androidx.media3.exoplayer.ExoPlayer,
    duration: Long,
    videoUri: Uri? = null,
    chapters: List<VideoChapter> = emptyList(),
    onScrubbingChanged: (Boolean) -> Unit,
    onPositionChanged: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var internalPosition by remember { mutableLongStateOf(exoPlayer.currentPosition) }
    var isDragging by remember { mutableStateOf(false) }
    var seekbarPlaying by remember { mutableStateOf(exoPlayer.isPlaying) }

    LaunchedEffect(exoPlayer, isDragging) {
        if (!isDragging) {
            while (true) {
                seekbarPlaying = exoPlayer.isPlaying
                internalPosition = exoPlayer.currentPosition
                onPositionChanged(internalPosition)
                delay(200L)
            }
        }
    }

    CoverPlaySeekbar(
        position = internalPosition,
        duration = duration,
        isPlaying = seekbarPlaying,
        videoUri = videoUri,
        bufferedPosition = exoPlayer.bufferedPosition,
        chapters = chapters,
        onValueChange = { v ->
            internalPosition = v.toLong()
            onPositionChanged(internalPosition)
        },
        onValueChangeFinished = {
            isDragging = false
            onScrubbingChanged(false)
            exoPlayer.seekTo(internalPosition)
        },
        onSeek = { ms ->
            internalPosition = ms
            onPositionChanged(internalPosition)
        },
        onDragStart = {
            isDragging = true
            onScrubbingChanged(true)
        },
        onDragEnd = {
            isDragging = false
            onScrubbingChanged(false)
        },
        modifier = modifier
    )
}

private val GlassFill = GlassTokens.OverVideoFill
private val GlassBorder = BorderStroke(
    1.dp,
    Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.30f), Color.White.copy(alpha = 0.05f)))
)

private fun speedLabel(speed: Float): String =
    if (speed % 1f == 0f) "${speed.toInt()}x"
    else "${"%.2f".format(speed).trimEnd('0').trimEnd('.')}x"

@Composable
private fun rememberCoverPlayBreath(enabled: Boolean): State<Float> {
    if (!enabled) return remember { mutableFloatStateOf(0f) }
    val t = rememberInfiniteTransition(label = "breath")
    return t.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "breathV"
    )
}

@Composable
private fun CoverPlayMoreSpeedChip(label: String, selected: Boolean, onClick: () -> Unit) = CoverPlayPlayerPanelChip(label, selected, onClick)

@UnstableApi
@Composable
fun CoverPlayVideoPlayerScreen(
    video: VideoFile,
    onBack: () -> Unit,
    folderVideos: List<VideoFile> = emptyList(),
    onPlayVideo: (VideoFile) -> Unit = {},
    settings: Map<String, Any> = emptyMap(),
    browserTitle: String? = null,
    browserSubtitle: String? = null
) {
    CompositionLocalProvider(LocalOverVideo provides true) {
        CoverPlayVideoPlayerScreenContent(video, onBack, folderVideos, onPlayVideo, settings, browserTitle, browserSubtitle)
    }
}

@UnstableApi
@Composable
private fun CoverPlayVideoPlayerScreenContent(
    video: VideoFile,
    onBack: () -> Unit,
    folderVideos: List<VideoFile> = emptyList(),
    onPlayVideo: (VideoFile) -> Unit = {},
    settings: Map<String, Any> = emptyMap(),
    browserTitle: String? = null,
    browserSubtitle: String? = null
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val accent = LocalAppUiSettings.current.accent
    val haptic = rememberCoverPlayHapticFeedback()
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    val defaultSpeed = settings["default_speed"] as? Float ?: 1.0f
    val resumePlayback = settings["resume_playback"] as? Boolean ?: true
    val brightnessGestureEnabled = settings["brightness_gesture"] as? Boolean ?: true
    val volumeGestureEnabled = settings["volume_gesture"] as? Boolean ?: true
    val pinchToZoomEnabled = settings["pinch_to_zoom"] as? Boolean ?: true
    val backgroundPlayEnabled = settings["background_play"] as? Boolean ?: false
    val defaultAspect = settings["default_aspect"] as? String ?: "Fit"
    val autoplayNext = settings["autoplay_next"] as? Boolean ?: false
    val loopVideo = settings["loop_video"] as? Boolean ?: false
    val hardwareDecode = settings["hardware_decode"] as? Boolean ?: true
    val skipSeconds = settings["skip_seconds"] as? Int ?: 10
    val subtleLockTimer = settings["subtle_lock_timer"] as? Boolean ?: true
    val seekGestureEnabled = settings["seek_gesture"] as? Boolean ?: true
    val doubleTapPauseEnabled = settings["double_tap_pause"] as? Boolean ?: true
    val liveSkipSeconds by rememberUpdatedState(skipSeconds)
    val liveDoubleTapPause by rememberUpdatedState(doubleTapPauseEnabled)
    val liveBrightnessGesture by rememberUpdatedState(brightnessGestureEnabled)
    val liveVolumeGesture by rememberUpdatedState(volumeGestureEnabled)
    val liveSeekGesture by rememberUpdatedState(seekGestureEnabled)
    val livePinchToZoom by rememberUpdatedState(pinchToZoomEnabled)
    val pc = remember { loadPlayerCustomization(context) }

    BackHandler {
        onBack()
    }
    var didRetrySoftwareDecode by remember { mutableStateOf(false) }

    val exoPlayer = remember {
        val renderersFactory = NextRenderersFactory(context).apply {
            setExtensionRendererMode(
                if (hardwareDecode) DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON
                else DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER
            )
            setEnableDecoderFallback(true)
        }

        val trackSelector = DefaultTrackSelector(context)

        val extractorsFactory = DefaultExtractorsFactory()
            .setConstantBitrateSeekingEnabled(true)

        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .callTimeout(0, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .addInterceptor(NetworkAuth.interceptor)
            .build()
        val httpDataSourceFactory = OkHttpDataSource.Factory(okHttpClient)
            .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36")

        val dataSourceFactory = DefaultDataSource.Factory(context, NetRoutingDataSourceFactory(context, httpDataSourceFactory))

        ExoPlayer.Builder(context)
            .setRenderersFactory(renderersFactory)
            .setTrackSelector(trackSelector)
            .setMediaSourceFactory(
                DefaultMediaSourceFactory(context, extractorsFactory).setDataSourceFactory(dataSourceFactory)
            )
            .build()
            .apply {
                playWhenReady = true
                setPlaybackSpeed(defaultSpeed)
                repeatMode = if (loopVideo) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
            }
    }

    val scope = rememberCoroutineScope()
    var sleepTimerMinutes by remember { mutableIntStateOf(0) }
    var sleepTimerEndsAt by remember { mutableLongStateOf(0L) }
    var abLoopA by remember { mutableLongStateOf(-1L) }
    var abLoopB by remember { mutableLongStateOf(-1L) }
    var isAudioOnly by remember { mutableStateOf(false) }
    var isMirrored by remember { mutableStateOf(false) }
    var playerViewRef by remember { mutableStateOf<PlayerView?>(null) }
    var subtitleStyleVersion by remember { mutableIntStateOf(0) }
    var hSeekActive by remember { mutableStateOf(false) }
    var hSeekStartPos by remember { mutableLongStateOf(0L) }
    var hSeekTarget by remember { mutableLongStateOf(0L) }

    LaunchedEffect(video.uri) {
        didRetrySoftwareDecode = false
        abLoopA = -1L
        abLoopB = -1L
    }

    var activeVideoPath by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(video.path, folderVideos) {
        activeVideoPath?.let { prevPath ->
            savePlaybackPosition(context, prevPath, exoPlayer.currentPosition, exoPlayer.duration)
        }
        activeVideoPath = video.path

        val currentUris = List(exoPlayer.mediaItemCount) { exoPlayer.getMediaItemAt(it).localConfiguration?.uri }

        val items = if (folderVideos.isEmpty()) listOf(video) else folderVideos
        val folderUris = items.map { it.uri }

        if (currentUris != folderUris) {
            val startIndex = items.indexOfFirst { it.path == video.path }.coerceAtLeast(0)
            val savedPos = if (resumePlayback) {
                context.getSharedPreferences("playback_positions", Context.MODE_PRIVATE)
                    .getLong(video.path, 0L)
            } else 0L

            exoPlayer.setMediaItems(
                items.map {
                    MediaItem.Builder()
                        .setUri(it.uri)
                        .setMediaMetadata(MediaMetadata.Builder().setTitle(formatFileName(it.name)).build())
                        .build()
                },
                startIndex, savedPos
            )
            exoPlayer.prepare()
        } else {
            val targetIndex = items.indexOfFirst { it.path == video.path }.coerceAtLeast(0)
            if (exoPlayer.currentMediaItemIndex != targetIndex) {
                val savedPos = if (resumePlayback) {
                    context.getSharedPreferences("playback_positions", Context.MODE_PRIVATE)
                        .getLong(video.path, 0L)
                } else 0L
                exoPlayer.seekTo(targetIndex, savedPos)
            }
        }
        exoPlayer.play()
    }

    var showPlayerTour by remember { mutableStateOf(!TutorialPrefs.playerSeen(context)) }
    if (showPlayerTour) {
        LaunchedEffect(Unit) { exoPlayer.pause() }
        CoverPlayPlayerTutorial(onFinish = {
            TutorialPrefs.markPlayer(context)
            showPlayerTour = false
            exoPlayer.play()
        })
    }

    var showSubtitleCustomization by remember { mutableStateOf(false) }
    var showOnlineSubtitleSearch by remember { mutableStateOf(false) }
    var showLocalSubtitleDialog by remember { mutableStateOf(false) }
    var showAudioSelector by remember { mutableStateOf(false) }
    var showSubtitleSelector by remember { mutableStateOf(false) }
    var showSpeedSelector by remember { mutableStateOf(false) }
    var showFolderVideos by remember { mutableStateOf(false) }
    var showHUD by remember { mutableStateOf(false) }
    var showExpressiveSheet by remember { mutableStateOf(false) }

    var isFullscreen by remember { mutableStateOf(true) }

    var isForcedLandscape by remember { mutableStateOf(false) }
    var isBackgroundPlayEnabled by remember { mutableStateOf(backgroundPlayEnabled) }

    val originalBrightness = remember {
        context.findActivity()?.window?.attributes?.screenBrightness ?: -1f
    }

    val isDarkTheme = LocalAppUiSettings.current.palette.isDark
    LaunchedEffect(isForcedLandscape) {
        updateOrientation(context, true, isForcedLandscape, isDarkTheme)
    }

    DisposableEffect(Unit) {
        onDispose {
            if (resumePlayback) {
                savePlaybackPosition(context, activeVideoPath ?: video.path, exoPlayer.currentPosition, exoPlayer.duration)
            }
            updateOrientation(context, isFullscreen = false, isDarkTheme = isDarkTheme)
            context.findActivity()?.let { activity ->
                val lp = activity.window.attributes
                lp.screenBrightness = originalBrightness
                activity.window.attributes = lp
            }
        }
    }
    var showControls by remember { mutableStateOf(true) }
    fun attachSubtitle(uri: Uri) {
        val cfg = MediaItem.SubtitleConfiguration.Builder(uri)
            .setMimeType(guessSubtitleMimeType(context, uri))
            .setLanguage("en")
            .setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
            .build()
        val cur = exoPlayer.currentMediaItem ?: return
        val idx = exoPlayer.currentMediaItemIndex
        val pos = exoPlayer.currentPosition
        val updated = cur.buildUpon()
            .setSubtitleConfigurations(cur.localConfiguration?.subtitleConfigurations.orEmpty() + cfg)
            .build()
        exoPlayer.replaceMediaItem(idx, updated)
        exoPlayer.seekTo(idx, pos)
        exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters.buildUpon()
            .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false).build()
        exoPlayer.play()
        showLocalSubtitleDialog = false
        showSubtitleSelector = false
        showOnlineSubtitleSearch = false
        updateOrientation(context, true, isForcedLandscape, isDarkTheme)
        showControls = true
    }
    val subtitlePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            showLocalSubtitleDialog = false
            showSubtitleSelector = false
            if (uri != null) {
                try {
                    context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                } catch (e: Exception) {}
                attachSubtitle(uri)
            } else {
                updateOrientation(context, true, isForcedLandscape, isDarkTheme)
                showControls = true
            }
        }
    )
    val isInPip = PipState.isInPip

    DisposableEffect(Unit) {
        PipState.canEnterPip = true
        onDispose { PipState.canEnterPip = false }
    }
    var isControlsLocked by remember { mutableStateOf(false) }
    var isSidePanelExpanded by remember { mutableStateOf(false) }
    val chapters by produceState<List<VideoChapter>>(emptyList(), video.uri) {
        value = withContext(Dispatchers.IO) { ChapterReader.read(context, video.uri) }
    }
    var isMuted by remember {
        val currentVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        mutableStateOf(currentVol == 0)
    }
    var currentSpeed by remember { mutableFloatStateOf(defaultSpeed) }
    var currentPitch by remember { mutableFloatStateOf(1f) }
    var repeatMode by remember { mutableIntStateOf(Player.REPEAT_MODE_OFF) }
    var isShuffleEnabled by remember { mutableStateOf(false) }
    var playbackError by remember { mutableStateOf<String?>(null) }

    val initialSavedPos = remember(video.path) {
        context.getSharedPreferences("playback_positions", Context.MODE_PRIVATE).getLong(video.path, 0L)
    }
    var showResumePrompt by remember(video.path) {
        mutableStateOf(resumePlayback && initialSavedPos > 5000L)
    }

    LaunchedEffect(showResumePrompt) {
        if (showResumePrompt) {
            delay(6000)
            showResumePrompt = false
        }
    }
    var isLoading by remember { mutableStateOf(true) }
    var isBufferingRaw by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }
    var playbackState by remember { mutableIntStateOf(Player.STATE_IDLE) }
    var position by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(0L) }

    var autoNextSeconds by remember { mutableIntStateOf(5) }
    var autoNextActive by remember { mutableStateOf(false) }

    var itemEnded by remember { mutableStateOf(false) }
    val videoEnded = playbackState == Player.STATE_ENDED || itemEnded
    val upNext = remember(videoEnded, video.id) {
        exoPlayer.nextMediaItemIndex.takeIf { it != C.INDEX_UNSET }?.let { folderVideos.getOrNull(it) }
    }

    LaunchedEffect(videoEnded) {
        if (videoEnded) {
            if (folderVideos.size > 1) showFolderVideos = true
            if (autoplayNext && exoPlayer.hasNextMediaItem()) {
                autoNextSeconds = 5
                autoNextActive = true
                while (autoNextSeconds > 0 && autoNextActive) {
                    delay(1000L)
                    autoNextSeconds -= 1
                }
                if (autoNextSeconds == 0 && autoNextActive) {
                    autoNextActive = false
                    showFolderVideos = false
                    exoPlayer.seekToNextMediaItem()
                    exoPlayer.play()
                }
            }
        } else {
            autoNextActive = false
        }
    }

    var isScrubbing by remember { mutableStateOf(false) }
    var isLongPressing by remember { mutableStateOf(false) }
    var scrubPositionMs by remember { mutableLongStateOf(0L) }

    var lastSeekTimeMs by remember { mutableLongStateOf(0L) }
    val seekSettleWindowMs = 750L

    LaunchedEffect(exoPlayer, isPlaying, video.uri) {
        var msSinceLastSave = 0L
        while (isPlaying) {
            val sinceSeek = System.currentTimeMillis() - lastSeekTimeMs
            if (!isScrubbing && sinceSeek >= seekSettleWindowMs) {
                val currentPos = exoPlayer.currentPosition
                val totalDur = exoPlayer.duration
                if (totalDur > 0) {
                    position = currentPos
                    duration = totalDur
                }
            }
            delay(500)
            msSinceLastSave += 500
            if (resumePlayback && msSinceLastSave >= 5000) {
                msSinceLastSave = 0
                savePlaybackPosition(context, video.path, exoPlayer.currentPosition, exoPlayer.duration)
            }
        }
    }

    LaunchedEffect(exoPlayer, isShuffleEnabled, repeatMode) {
        exoPlayer.shuffleModeEnabled = isShuffleEnabled
        exoPlayer.repeatMode = repeatMode
        exoPlayer.setPauseAtEndOfMediaItems(repeatMode != Player.REPEAT_MODE_ONE)
    }

    var currentVideoSize by remember { mutableStateOf<VideoSize?>(null) }

    LaunchedEffect(currentVideoSize) {
        currentVideoSize?.let { size ->
            if (size.width > 0 && size.height > 0) {
                val rotated = size.unappliedRotationDegrees % 180 != 0
                val w = if (rotated) size.height else size.width
                val h = if (rotated) size.width else size.height
                val ratio = (w.toFloat() / h.toFloat()).coerceIn(1f / 2.39f, 2.39f)
                PipState.aspectRatio = Rational((ratio * 1000).toInt(), 1000)
            }
        }
    }

    val currentOnPlayVideo by rememberUpdatedState(onPlayVideo)
    val currentAutoplayNext by rememberUpdatedState(autoplayNext)
    val currentFolderVideos by rememberUpdatedState(folderVideos)
    val currentVideo by rememberUpdatedState(video)

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                Log.e("PLAYER_ERROR", "Playback failed (code=${error.errorCode}, name=${error.errorCodeName})", error)

                val isDecoderIssue = error.errorCode == PlaybackException.ERROR_CODE_DECODER_INIT_FAILED ||
                        error.errorCode == PlaybackException.ERROR_CODE_DECODING_FAILED ||
                        error.errorCode == PlaybackException.ERROR_CODE_DECODER_QUERY_FAILED

                if (isDecoderIssue && !didRetrySoftwareDecode) {
                    didRetrySoftwareDecode = true
                    val position = exoPlayer.currentPosition
                    val item = exoPlayer.currentMediaItem
                    if (item != null) {
                        Log.d("PLAYER_ERROR", "Retrying with re-selected decoder at position $position")
                        exoPlayer.stop()
                        exoPlayer.setMediaItem(item, position)
                        exoPlayer.prepare()
                        exoPlayer.play()
                        return
                    }
                }

                val detail = error.cause?.message?.takeIf { it.isNotBlank() }
                playbackError = buildString {
                    append(error.errorCodeName.removePrefix("ERROR_CODE_").replace('_', ' '))
                    if (detail != null) {
                        append(": ")
                        append(detail)
                    }
                }
            }

            override fun onPlaybackStateChanged(state: Int) {
                playbackState = state
                isBufferingRaw = state == Player.STATE_BUFFERING
                if (state == Player.STATE_READY) {
                    val d = exoPlayer.duration
                    if (d > 0) duration = d
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                playbackError = null
                itemEnded = false
                val index = exoPlayer.currentMediaItemIndex
                if (index >= 0 && index < currentFolderVideos.size) {
                    val newVideo = currentFolderVideos[index]

                    if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO && !currentAutoplayNext) {
                        exoPlayer.pause()
                    }

                    if (newVideo.path != currentVideo.path) {
                        currentOnPlayVideo(newVideo)
                    }
                }
            }

            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
                if (playing) itemEnded = false
            }

            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                if (reason == Player.PLAY_WHEN_READY_CHANGE_REASON_END_OF_MEDIA_ITEM) itemEnded = true
            }

            override fun onTimelineChanged(timeline: Timeline, reason: Int) {
                val d = exoPlayer.duration
                if (d > 0) duration = d
            }

            override fun onVideoSizeChanged(videoSize: VideoSize) {
                currentVideoSize = videoSize
                if (videoSize.width > 0 && videoSize.height > 0) {
                    val isLandscape = if (videoSize.unappliedRotationDegrees % 180 == 0) {
                        videoSize.width >= videoSize.height
                    } else {
                        videoSize.height >= videoSize.width
                    }
                    if (isForcedLandscape != isLandscape) {
                        isForcedLandscape = isLandscape
                    }
                }
            }
        }
        exoPlayer.addListener(listener)
        if (exoPlayer.duration > 0) duration = exoPlayer.duration

        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    LaunchedEffect(isBackgroundPlayEnabled) {
        if (isBackgroundPlayEnabled) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
            ) notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            PlaybackHost.attach(context, exoPlayer)
        } else {
            PlaybackHost.detach()
        }
    }
    DisposableEffect(Unit) { onDispose { PlaybackHost.detach() } }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    if (!isBackgroundPlayEnabled && !PipState.isInPip) {
                        exoPlayer.pause()
                    }
                }

                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val resizeModes = remember {
        listOf(
            AspectRatioFrameLayout.RESIZE_MODE_FIT to "Fit",
            AspectRatioFrameLayout.RESIZE_MODE_FILL to "Fill",
            AspectRatioFrameLayout.RESIZE_MODE_ZOOM to "Zoom"
        )
    }
    var resizeModeIndex by remember {
        val initial = resizeModes.indexOfFirst { it.second == defaultAspect }.coerceAtLeast(0)
        mutableIntStateOf(initial)
    }

    var videoZoomScale by remember { mutableFloatStateOf(1f) }
    var videoPanX by remember { mutableFloatStateOf(0f) }
    var videoPanY by remember { mutableFloatStateOf(0f) }
    var isZoomGestureActive by remember { mutableStateOf(false) }
    val minZoom = 1f
    val maxZoom = 4f

    LaunchedEffect(video.uri) {
        videoZoomScale = minZoom
        videoPanX = 0f
        videoPanY = 0f
    }

    fun clampPan(
        panX: Float,
        panY: Float,
        scale: Float,
        viewWidth: Float,
        viewHeight: Float
    ): Pair<Float, Float> {
        val maxOffsetX = (viewWidth * (scale - 1f) / 2f).coerceAtLeast(0f)
        val maxOffsetY = (viewHeight * (scale - 1f) / 2f).coerceAtLeast(0f)
        return panX.coerceIn(-maxOffsetX, maxOffsetX) to panY.coerceIn(-maxOffsetY, maxOffsetY)
    }

    var gestureText by remember { mutableStateOf("") }

    var gestureIcon by remember {
        mutableStateOf<androidx.compose.ui.graphics.vector.ImageVector?>(
            null
        )
    }
    var gestureValue by remember { mutableFloatStateOf(0f) }
    var showVerticalBar by remember { mutableStateOf(false) }
    var isBarBrightness by remember { mutableStateOf(false) }

    var seekAnimationToken by remember { mutableIntStateOf(0) }
    var seekAnimationSide by remember { mutableStateOf<String?>(null) }
    var cumulativeSeekAmount by remember { mutableIntStateOf(0) }
    var seekTapX by remember { mutableFloatStateOf(0.5f) }
    var seekTapY by remember { mutableFloatStateOf(0.5f) }

    var gestureDownTime by remember { mutableLongStateOf(-1L) }
    var gestureStartY by remember { mutableFloatStateOf(0f) }
    var gestureStartBrightness by remember { mutableFloatStateOf(0f) }
    var gestureStartVolumeValue by remember { mutableFloatStateOf(0f) }
    var lastVolumeGestureValue by remember {
        val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val sysVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        mutableFloatStateOf(sysVol.toFloat() / maxVol.toFloat())
    }

    var isMultiTouch by remember { mutableStateOf(false) }
    var pinchSessionActive by remember { mutableStateOf(false) }

    val mainActivity = context.findActivity() as? MainActivity
    DisposableEffect(mainActivity) {
        mainActivity?.onVolumeKeyEvent = { keyCode, _ ->
            if (liveVolumeGesture) {
                val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                val currentSysVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)

                var currentUnified = if (currentSysVol >= maxVolume) {
                    lastVolumeGestureValue.coerceIn(1f, 2f)
                } else {
                    currentSysVol.toFloat() / maxVolume.toFloat()
                }

                val step = 0.05f
                if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                    currentUnified = (currentUnified + step).coerceAtMost(2f)
                } else {
                    currentUnified = (currentUnified - step).coerceAtLeast(0f)
                }

                lastVolumeGestureValue = currentUnified
                gestureValue = currentUnified
                isBarBrightness = false
                showVerticalBar = true
                gestureText = if (currentUnified > 1.0f) "Boost ${(currentUnified * 100).toInt()}%" else "Volume ${(currentUnified * 100).toInt()}%"
                gestureIcon = if (currentUnified > 0f) Icons.AutoMirrored.Rounded.VolumeUp
                else Icons.AutoMirrored.Rounded.VolumeOff

                if (currentUnified <= 1.0f) {
                    val targetVolume = (currentUnified * maxVolume).toInt()
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetVolume, 0)
                    exoPlayer.volume = 1.0f
                    isMuted = targetVolume == 0
                } else {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, 0)
                    exoPlayer.volume = currentUnified
                    isMuted = false
                }
                true
            } else false
        }
        onDispose {
            mainActivity?.onVolumeKeyEvent = null
        }
    }

    val scaleGestureDetector = remember {
        ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            var totalScale = 1f
            var hasCycledThisGesture = false
            var lastFocusX = 0f
            var lastFocusY = 0f

            override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
                totalScale = 1f
                hasCycledThisGesture = false
                isZoomGestureActive = false
                lastFocusX = detector.focusX
                lastFocusY = detector.focusY
                return true
            }

            override fun onScale(detector: ScaleGestureDetector): Boolean {
                if (!livePinchToZoom) return false
                showVerticalBar = false
                pinchSessionActive = true

                totalScale *= detector.scaleFactor

                val spanDelta = abs(detector.currentSpan - detector.previousSpan)
                val minSpanDeltaPx = 4f
                if (spanDelta < minSpanDeltaPx && !hasCycledThisGesture) {
                    return true
                }

                if (!hasCycledThisGesture) {
                    if (totalScale > 1.2f) {
                        if (resizeModeIndex < resizeModes.size - 1) {
                            resizeModeIndex++
                            gestureText = resizeModes[resizeModeIndex].second
                            hasCycledThisGesture = true
                            totalScale = 1f
                            return true
                        }
                        hasCycledThisGesture = true
                    } else if (totalScale < 0.8f) {
                        if (resizeModeIndex > 0) {
                            resizeModeIndex--
                            gestureText = resizeModes[resizeModeIndex].second
                            hasCycledThisGesture = true
                            totalScale = 1f
                            return true
                        }
                        hasCycledThisGesture = true
                    } else {
                        return false
                    }
                }

                isZoomGestureActive = true
                val newScale = (videoZoomScale * detector.scaleFactor).coerceIn(minZoom, maxZoom)
                videoZoomScale = newScale

                val focusDeltaX = detector.focusX - lastFocusX
                val focusDeltaY = detector.focusY - lastFocusY
                lastFocusX = detector.focusX
                lastFocusY = detector.focusY

                if (newScale <= minZoom) {
                    videoPanX = 0f
                    videoPanY = 0f
                } else {
                    val screenWidth = context.resources.displayMetrics.widthPixels.toFloat()
                    val screenHeight = context.resources.displayMetrics.heightPixels.toFloat()
                    val (clampedX, clampedY) = clampPan(
                        videoPanX + focusDeltaX,
                        videoPanY + focusDeltaY,
                        newScale,
                        screenWidth,
                        screenHeight
                    )
                    videoPanX = clampedX
                    videoPanY = clampedY
                }
                return true
            }
        })
    }

    val gestureDetector = remember {
        GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent): Boolean = true

            override fun onLongPress(e: MotionEvent) {
                if (isControlsLocked) return
                isLongPressing = true
                showControls = false
                exoPlayer.setPlaybackSpeed(pc.holdSpeed)
                gestureText = pc.holdSpeed.toString().removeSuffix(".0") + "x"
            }

            override fun onScroll(
                e1: MotionEvent?,
                e2: MotionEvent,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                if (e1 == null || scaleGestureDetector.isInProgress || pinchSessionActive || isMultiTouch) {
                    return false
                }

                val screenHeight = context.resources.displayMetrics.heightPixels.toFloat()
                val screenWidth = context.resources.displayMetrics.widthPixels

                val totalDX = abs(e2.x - e1.x)
                val totalDY = abs(e2.y - e1.y)

                if (hSeekActive || (!showVerticalBar && totalDX > totalDY)) {
                    if (!liveSeekGesture) return false
                    if (!hSeekActive) {
                        hSeekActive = true
                        hSeekStartPos = exoPlayer.currentPosition
                    }
                    val total = exoPlayer.duration.takeUnless { it == C.TIME_UNSET } ?: duration
                    val deltaMs = ((e2.x - e1.x) / screenWidth.toFloat() * (pc.swipeSeekSeconds * 1000f)).toLong()
                    hSeekTarget = (hSeekStartPos + deltaMs).coerceIn(0L, if (total > 0L) total else Long.MAX_VALUE)
                    val deltaSec = (hSeekTarget - hSeekStartPos) / 1000L
                    gestureText = (if (deltaSec >= 0) "+" else "−") + abs(deltaSec) + "s  ·  " + formatDuration(hSeekTarget)
                    position = hSeekTarget
                    return true
                }

                if (e1.downTime != gestureDownTime) {
                    gestureDownTime = e1.downTime
                    gestureStartY = e1.y

                    val activity = context.findActivity()
                    val curBrightness = activity?.window?.attributes?.screenBrightness ?: -1f
                    gestureStartBrightness = if (curBrightness < 0f) 0.5f else curBrightness

                    val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                    val sysVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                    gestureStartVolumeValue = if (sysVol >= maxVol) {
                        lastVolumeGestureValue.coerceIn(
                            1f,
                            2f
                        )
                    } else {
                        sysVol.toFloat() / maxVol.toFloat()
                    }

                    gestureValue = if ((e1.x < screenWidth / 2) != pc.swapBrightnessVolume) gestureStartBrightness
                    else gestureStartVolumeValue
                }

                val deltaY = gestureStartY - e2.y

                showVerticalBar = true

                if ((e1.x < screenWidth / 2) != pc.swapBrightnessVolume) {
                    if (!liveBrightnessGesture) return false
                    val newBrightness =
                        (gestureStartBrightness + (deltaY / screenHeight) * 1.8f).coerceIn(0.01f, 1f)
                    val activity = context.findActivity()
                    activity?.let {
                        val lp = it.window.attributes
                        lp.screenBrightness = newBrightness
                        it.window.attributes = lp
                    }
                    gestureText = "Brightness"
                    isBarBrightness = true
                    gestureIcon = null
                    gestureValue = newBrightness
                } else {
                    if (!liveVolumeGesture) return false
                    val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                    val newGestureValue =
                        (gestureStartVolumeValue + (deltaY / screenHeight) * 2.4f).coerceIn(0f, 2f)
                    gestureValue = newGestureValue
                    isBarBrightness = false

                    lastVolumeGestureValue = newGestureValue
                    if (newGestureValue <= 1.0f) {
                        val targetVolume = (newGestureValue * maxVolume).toInt()
                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetVolume, 0)
                        exoPlayer.volume = 1.0f
                    } else {
                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, 0)
                        exoPlayer.volume = newGestureValue
                    }

                    gestureText = if (newGestureValue > 1.0f) "Volume Boost" else "Volume"
                    gestureIcon = if (newGestureValue > 0f) Icons.AutoMirrored.Rounded.VolumeUp
                    else Icons.AutoMirrored.Rounded.VolumeOff
                }
                return true
            }

            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                showControls = !showControls
                return true
            }

            override fun onDoubleTap(e: MotionEvent): Boolean {
                if (videoZoomScale > minZoom) {
                    videoZoomScale = minZoom
                    videoPanX = 0f
                    videoPanY = 0f
                    gestureText = "Zoom reset"
                    return true
                }

                val screenWidth =
                    context.resources.displayMetrics.widthPixels

                if (liveDoubleTapPause && e.x > screenWidth / 3f && e.x < screenWidth * 2f / 3f) {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    if (exoPlayer.isPlaying) { exoPlayer.pause(); gestureText = "PAUSED" }
                    else { exoPlayer.play(); gestureText = "PLAYING" }
                    return true
                }

                val total = exoPlayer.duration.takeUnless {
                    it ==
                            C.TIME_UNSET
                } ?: duration

                val side = if (e.x < screenWidth / 2) "left" else "right"
                val halfW = screenWidth / 2f
                seekTapX = (if (side == "left") e.x / halfW else (e.x - halfW) / halfW).coerceIn(0f, 1f)
                seekTapY = (e.y / context.resources.displayMetrics.heightPixels.toFloat()).coerceIn(0f, 1f)
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                val skipMs = liveSkipSeconds * 1000L
                if (seekAnimationSide != side) {
                    cumulativeSeekAmount = liveSkipSeconds
                } else {
                    cumulativeSeekAmount += liveSkipSeconds
                }
                seekAnimationSide = side
                seekAnimationToken++

                if (side == "left") {
                    val t = (exoPlayer.currentPosition - skipMs)
                        .coerceAtLeast(0L)

                    exoPlayer.seekTo(
                        exoPlayer.currentMediaItemIndex,
                        t
                    )
                    lastSeekTimeMs = System.currentTimeMillis()

                    position = t
                } else {
                    val t = (exoPlayer.currentPosition + skipMs)
                        .coerceAtMost(
                            if (total > 0) total
                            else exoPlayer.currentPosition + skipMs
                        )

                    exoPlayer.seekTo(
                        exoPlayer.currentMediaItemIndex,
                        t
                    )
                    lastSeekTimeMs = System.currentTimeMillis()
                    position = t
                }

                return true
            }
        })
    }

    LaunchedEffect(gestureText, showVerticalBar, gestureValue, isLongPressing, hSeekActive) {
        if ((showVerticalBar || gestureText.isNotEmpty()) && !isLongPressing && !hSeekActive) {
            delay(1000)
            showVerticalBar = false
            gestureText = ""
        }
    }

    LaunchedEffect(seekAnimationToken) {
        if (seekAnimationSide != null) {
            delay(750)
            seekAnimationSide = null
            cumulativeSeekAmount = 0
        }
    }

    LaunchedEffect(
        showControls, isScrubbing, isPlaying, showVerticalBar, isControlsLocked,
        showAudioSelector, showSubtitleSelector, showLocalSubtitleDialog, showOnlineSubtitleSearch,
        showSubtitleCustomization, showSpeedSelector, showHUD, showFolderVideos, showExpressiveSheet
    ) {
        if (showControls && isPlaying && !isScrubbing && !showVerticalBar &&
            !showAudioSelector && !showSubtitleSelector && !showLocalSubtitleDialog &&
            !showOnlineSubtitleSearch && !showSubtitleCustomization && !showSpeedSelector &&
            !showHUD && !showFolderVideos && !showExpressiveSheet && pc.hideAfterMs > 0
        ) {
            delay(pc.hideAfterMs.toLong())
            showControls = false
        }
    }

    LaunchedEffect(sleepTimerEndsAt) {
        if (sleepTimerEndsAt > 0L) {
            val wait = sleepTimerEndsAt - System.currentTimeMillis()
            if (wait > 0L) delay(wait)
            exoPlayer.pause()
            gestureText = "Sleep timer ended"
            sleepTimerMinutes = 0
            sleepTimerEndsAt = 0L
        }
    }

    LaunchedEffect(abLoopA, abLoopB) {
        if (abLoopA >= 0L && abLoopB > abLoopA) {
            while (true) {
                if (exoPlayer.isPlaying && exoPlayer.currentPosition >= abLoopB) exoPlayer.seekTo(abLoopA)
                delay(80)
            }
        }
    }

    fun stepFrame(forward: Boolean) {
        exoPlayer.pause()
        val fps = exoPlayer.videoFormat?.frameRate?.takeIf { it > 0f } ?: 30f
        val stepMs = (1000f / fps).roundToInt().coerceAtLeast(1)
        exoPlayer.setSeekParameters(SeekParameters.EXACT)
        val target = (exoPlayer.currentPosition + if (forward) stepMs else -stepMs).coerceAtLeast(0L)
        exoPlayer.seekTo(target)
        position = target
        lastSeekTimeMs = System.currentTimeMillis()
        gestureText = "Frame ${formatDuration(target)}"
    }

    fun captureFrame() {
        val surface = playerViewRef?.videoSurfaceView
        val name = File(video.path).nameWithoutExtension.ifBlank { "CoverPlay" }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) { gestureText = "Screenshots need Android 10+"; return }
        when (surface) {
            is SurfaceView -> {
                if (surface.width <= 0 || surface.height <= 0) { gestureText = "Screenshot unavailable"; return }
                val bmp = Bitmap.createBitmap(surface.width, surface.height, Bitmap.Config.ARGB_8888)
                PixelCopy.request(surface, bmp, { result ->
                    if (result == PixelCopy.SUCCESS) {
                        scope.launch {
                            val ok = withContext(Dispatchers.IO) { saveScreenshot(context, bmp, name) }
                            gestureText = if (ok) "Screenshot saved" else "Screenshot failed"
                        }
                    } else gestureText = "Screenshot failed"
                }, Handler(Looper.getMainLooper()))
            }
            is TextureView -> {
                val bmp = surface.bitmap
                scope.launch {
                    val ok = bmp != null && withContext(Dispatchers.IO) { saveScreenshot(context, bmp, name) }
                    gestureText = if (ok) "Screenshot saved" else "Screenshot failed"
                }
            }
            else -> gestureText = "Screenshot unavailable"
        }
    }

    val subtitleStyle = remember(subtitleStyleVersion) {
        val sp = context.getSharedPreferences("subtitle_prefs", Context.MODE_PRIVATE)
        SubtitleStyleSpec(
            size = sp.getFloat("font_size", 16f),
            color = sp.getInt("text_color", 0xFFFFFFFF.toInt()),
            bgAlpha = sp.getFloat("bg_alpha", 0.5f),
            bold = sp.getBoolean("bold", false)
        )
    }

    val isGestureOverlayActive = showVerticalBar || isLongPressing || pinchSessionActive

    LaunchedEffect(isBufferingRaw, showVerticalBar) {
        if (isBufferingRaw && !showVerticalBar) {
            delay(500)
            if (isBufferingRaw && !showVerticalBar) isLoading = true
        } else {
            isLoading = false
        }
    }

    val panelOpen = false
    val panelBlur by animateDpAsState(if (panelOpen) 24.dp else 0.dp, tween(320), label = "panelBlur")
    val panelScrim by animateFloatAsState(if (panelOpen) 1f else 0f, tween(320), label = "panelScrim")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (!isLandscape && !LocalAppUiSettings.current.hideThumbnails) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(video.uri)
                    .videoFrameMillis(1000)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = 0.45f }
                    .blur(60.dp)
            )
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)))
        }

        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = videoZoomScale * (if (isMirrored) -1f else 1f)
                    scaleY = videoZoomScale
                    translationX = videoPanX
                    translationY = videoPanY
                }
                .blur(panelBlur),
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false
                    resizeMode = resizeModes[resizeModeIndex].first
                    keepScreenOn = true
                }
            },
            update = { view ->
                view.resizeMode = resizeModes[resizeModeIndex].first
                view.keepScreenOn = isPlaying
                if (playerViewRef !== view) playerViewRef = view
                if (view.tag != subtitleStyle) {
                    view.tag = subtitleStyle
                    view.subtitleView?.apply {
                        setApplyEmbeddedStyles(false)
                        setApplyEmbeddedFontSizes(false)
                        setStyle(
                            CaptionStyleCompat(
                                subtitleStyle.color,
                                android.graphics.Color.argb((subtitleStyle.bgAlpha * 255).toInt(), 0, 0, 0),
                                android.graphics.Color.TRANSPARENT,
                                CaptionStyleCompat.EDGE_TYPE_OUTLINE,
                                android.graphics.Color.BLACK,
                                androidx.core.content.res.ResourcesCompat.getFont(
                                    view.context,
                                    if (subtitleStyle.bold) R.font.google_sans_flex_rounded_bold
                                    else R.font.google_sans_flex_rounded_medium
                                ) ?: if (subtitleStyle.bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
                            )
                        )
                        setFixedTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, subtitleStyle.size)
                    }
                }
            }
        )
        if (panelScrim > 0.01f) {
            Box(
                Modifier.fillMaxSize()
                    .graphicsLayer { alpha = panelScrim }
                    .background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.30f), Color.Black.copy(alpha = 0.62f))))
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInteropFilter { motionEvent ->
                    if (isControlsLocked) {
                        if (motionEvent.action == MotionEvent.ACTION_UP) {
                            showControls = !showControls
                        }
                        return@pointerInteropFilter true
                    }

                    when (motionEvent.actionMasked) {
                        MotionEvent.ACTION_POINTER_DOWN -> {
                            if (motionEvent.pointerCount >= 2) {
                                isMultiTouch = true
                                showVerticalBar = false
                                hSeekActive = false
                            }
                        }
                        MotionEvent.ACTION_POINTER_UP -> {
                            if (motionEvent.pointerCount <= 2) {
                                isMultiTouch = false
                            }
                        }
                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                            isMultiTouch = false
                            pinchSessionActive = false
                        }
                    }

                    scaleGestureDetector.onTouchEvent(motionEvent)
                    gestureDetector.onTouchEvent(motionEvent)

                    if (hSeekActive && (motionEvent.action == MotionEvent.ACTION_UP || motionEvent.action == MotionEvent.ACTION_CANCEL)) {
                        hSeekActive = false
                        if (motionEvent.action == MotionEvent.ACTION_UP) {
                            exoPlayer.seekTo(exoPlayer.currentMediaItemIndex, hSeekTarget)
                            lastSeekTimeMs = System.currentTimeMillis()
                            position = hSeekTarget
                        } else {
                            position = exoPlayer.currentPosition
                        }
                        gestureText = ""
                    }

                    if (motionEvent.action == MotionEvent.ACTION_UP || motionEvent.action == MotionEvent.ACTION_CANCEL) {
                        if (isLongPressing) {
                            isLongPressing = false
                            exoPlayer.setPlaybackSpeed(currentSpeed)
                            gestureText = if (currentSpeed != 1.0f) "${String.format("%.2f", currentSpeed)}x" else ""
                        }
                    }
                    true
                }
        )

        if (!isInPip) {
            val settings = LocalAppUiSettings.current

            AnimatedVisibility(
                visible = showResumePrompt,
                enter = fadeIn(tween(260)) + slideInVertically(tween(320, easing = FastOutSlowInEasing)) { it / 2 },
                exit = fadeOut(tween(200)) + slideOutVertically(tween(240)) { it / 2 },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = if (isLandscape) 118.dp else 132.dp)
            ) {
                CoverPlayGlassSurface(shape = CircleShape, overVideo = true, strong = true, elevation = 12.dp, pressedScale = 1f) {
                    Row(
                        modifier = Modifier.padding(start = 16.dp, end = 5.dp, top = 5.dp, bottom = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Resumed at ${formatDuration(initialSavedPos)}",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold, fontSize = 12.sp, fontFeatureSettings = "tnum"),
                            color = Color.White.copy(alpha = 0.9f),
                            maxLines = 1
                        )
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(lerp(settings.accent, settings.highlight, 0.30f), settings.accent)))
                                .aliveClickable {
                                    exoPlayer.seekTo(0L)
                                    position = 0L
                                    showResumePrompt = false
                                }
                                .padding(horizontal = 14.dp, vertical = 7.dp)
                        ) {
                            Text(
                                "Start over",
                                color = Color.White,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold, fontSize = 12.sp),
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = showControls && !isGestureOverlayActive && !isControlsLocked,
                enter = fadeIn(animationSpec = tween(300, easing = FastOutSlowInEasing)),
                exit = fadeOut(animationSpec = tween(200, easing = FastOutSlowInEasing))
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Box(
                        Modifier.fillMaxWidth().height(80.dp).align(Alignment.TopCenter)
                            .background(Brush.verticalGradient(listOf(Color.Black.copy(0.32f), Color.Transparent)))
                    )
                    Box(
                        Modifier.fillMaxWidth().height(100.dp).align(Alignment.BottomCenter)
                            .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(0.38f))))
                    )

                    val sz = pc.controlSize.button.dp
                    val bd = false
                    val anyExtraOn = isShuffleEnabled || repeatMode != Player.REPEAT_MODE_OFF ||
                            isBackgroundPlayEnabled || isMuted || currentSpeed != 1.0f

                    @Composable
                    fun PlayerButtonView(b: PlayerButton) {
                        when (b) {
                            PlayerButton.BROWSE -> if (folderVideos.size > 1) {
                                CoverPlayChromeIconButton(painterResource(R.drawable.ic_browse_list_custom), "Browse Folder", active = showFolderVideos, size = sz, backdrop = bd) { showFolderVideos = true }
                            }
                            PlayerButton.SUBTITLES -> CoverPlayChromeIconButton(painterResource(R.drawable.ic_subtitles_custom), "Subtitles", active = showSubtitleSelector, size = sz, backdrop = bd) { showSubtitleSelector = true }
                            PlayerButton.AUDIO_TRACK -> CoverPlayChromeIconButton(painterResource(R.drawable.ic_music_custom), "Audio track", active = showAudioSelector, size = sz, backdrop = bd) { showAudioSelector = true }
                            PlayerButton.SPEED -> CoverPlayChromeIconButton(painterResource(R.drawable.ic_speed_custom), "Speed", active = showSpeedSelector || currentSpeed != 1.0f, size = sz, backdrop = bd) { showSpeedSelector = true }
                            PlayerButton.ROTATE -> CoverPlayChromeIconButton(painterResource(R.drawable.ic_orientation_custom), "Rotate", active = isForcedLandscape, size = sz, backdrop = bd) {
                                isForcedLandscape = !isForcedLandscape
                                gestureText = if (isForcedLandscape) "LANDSCAPE" else "AUTO ROTATE"
                            }
                            PlayerButton.ASPECT -> CoverPlayChromeIconButton(
                                painterResource(when (resizeModeIndex) { 0 -> R.drawable.ic_aspect_fit; 1 -> R.drawable.ic_aspect_fill; else -> R.drawable.ic_aspect_zoom }),
                                "Aspect", size = sz, backdrop = bd
                            ) {
                                resizeModeIndex = (resizeModeIndex + 1) % resizeModes.size
                                gestureText = resizeModes[resizeModeIndex].second.uppercase()
                            }
                            PlayerButton.PIP -> CoverPlayChromeIconButton(painterResource(R.drawable.ic_pip_custom), "Picture in picture", size = sz, backdrop = bd) {
                                (context.findActivity() as? MainActivity)?.enterPipModeNow()
                            }
                            PlayerButton.LOCK -> CoverPlayChromeIconButton(painterResource(R.drawable.ic_lock_custom), "Lock", size = sz, backdrop = bd) { isControlsLocked = true }
                            PlayerButton.MORE -> Box {
                                CoverPlayChromeIconButton(painterResource(R.drawable.ic_player_controls), "Player Controls", active = showExpressiveSheet, size = sz, backdrop = bd) {
                                    showExpressiveSheet = true; showControls = false
                                }
                                if (anyExtraOn) {
                                    Box(
                                        Modifier.align(Alignment.TopEnd).offset(x = (-5).dp, y = 5.dp)
                                            .size(8.dp)
                                            .background(Brush.linearGradient(listOf(settings.highlight, accent)), CircleShape)
                                            .border(1.5.dp, Color.Black.copy(alpha = 0.55f), CircleShape)
                                    )
                                }
                            }
                            PlayerButton.REPEAT -> CoverPlayChromeIconButton(
                                painterResource(if (repeatMode == Player.REPEAT_MODE_ONE) R.drawable.ic_repeat_one_custom else R.drawable.ic_repeat_custom),
                                "Repeat", active = repeatMode != Player.REPEAT_MODE_OFF, size = sz, backdrop = bd
                            ) {
                                repeatMode = when (repeatMode) {
                                    Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
                                    Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
                                    else -> Player.REPEAT_MODE_OFF
                                }
                                exoPlayer.repeatMode = repeatMode
                                gestureText = when (repeatMode) { Player.REPEAT_MODE_ALL -> "Repeat All"; Player.REPEAT_MODE_ONE -> "Repeat One"; else -> "Repeat Off" }
                            }
                            PlayerButton.SHUFFLE -> CoverPlayChromeIconButton(painterResource(R.drawable.ic_shuffle_custom), "Shuffle", active = isShuffleEnabled, size = sz, backdrop = bd) {
                                isShuffleEnabled = !isShuffleEnabled
                                gestureText = if (isShuffleEnabled) "Shuffle On" else "Shuffle Off"
                            }
                            PlayerButton.BACKGROUND -> CoverPlayChromeIconButton(
                                painterResource(if (isBackgroundPlayEnabled) R.drawable.ic_headphones_custom else R.drawable.ic_headphones_off_custom),
                                "Background play", active = isBackgroundPlayEnabled, size = sz, backdrop = bd
                            ) {
                                isBackgroundPlayEnabled = !isBackgroundPlayEnabled
                                gestureText = if (isBackgroundPlayEnabled) "Background on" else "Background off"
                            }
                            PlayerButton.MUTE -> CoverPlayChromeIconButton(
                                painterResource(if (isMuted) R.drawable.ic_volume_off_custom else R.drawable.ic_volume_custom),
                                "Mute", active = isMuted, size = sz, backdrop = bd
                            ) {
                                isMuted = !isMuted
                                val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, if (isMuted) 0 else maxVol / 2, 0)
                                gestureText = if (isMuted) "Muted" else "Unmuted"
                            }
                            PlayerButton.AB_LOOP -> CoverPlayChromeIconButton(painterResource(R.drawable.ic_repeat_custom), "A-B loop", active = abLoopA >= 0L, size = sz, backdrop = bd) {
                                val cur = exoPlayer.currentPosition
                                when {
                                    abLoopA < 0L -> { abLoopA = cur; gestureText = "A · ${formatDuration(cur)}" }
                                    abLoopB < 0L -> {
                                        if (cur > abLoopA + 500L) { abLoopB = cur; gestureText = "Looping A–B" }
                                        else gestureText = "Pick a later point for B"
                                    }
                                    else -> { abLoopA = -1L; abLoopB = -1L; gestureText = "A–B loop off" }
                                }
                            }
                            PlayerButton.SLEEP_TIMER -> CoverPlayChromeIconButton(painterResource(R.drawable.ic_stopwatch_custom), "Sleep timer", active = sleepTimerMinutes != 0, size = sz, backdrop = bd) {
                                sleepTimerMinutes = when (sleepTimerMinutes) { 0 -> 15; 15 -> 30; 30 -> 45; 45 -> 60; else -> 0 }
                                sleepTimerEndsAt = if (sleepTimerMinutes == 0) 0L else System.currentTimeMillis() + sleepTimerMinutes * 60_000L
                                gestureText = if (sleepTimerMinutes == 0) "Sleep timer off" else "Sleep in $sleepTimerMinutes min"
                            }
                            PlayerButton.FRAME_BACK -> CoverPlayChromeIconButton(painterResource(R.drawable.ic_prev_custom), "Previous frame", size = sz, backdrop = bd) { stepFrame(false) }
                            PlayerButton.FRAME_FORWARD -> CoverPlayChromeIconButton(painterResource(R.drawable.ic_next_custom), "Next frame", size = sz, backdrop = bd) { stepFrame(true) }
                            PlayerButton.SCREENSHOT -> CoverPlayChromeIconButton(painterResource(R.drawable.ic_thumbnail), "Screenshot", size = sz, backdrop = bd) { captureFrame() }
                            PlayerButton.MIRROR -> CoverPlayChromeIconButton(painterResource(R.drawable.ic_orientation_custom), "Mirror", active = isMirrored, size = sz, backdrop = bd) {
                                isMirrored = !isMirrored
                                gestureText = if (isMirrored) "Mirrored" else "Normal"
                            }
                            PlayerButton.AUDIO_ONLY -> CoverPlayChromeIconButton(painterResource(R.drawable.ic_headphones_custom), "Audio only", active = isAudioOnly, size = sz, backdrop = bd) {
                                isAudioOnly = !isAudioOnly
                                exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters.buildUpon()
                                    .setTrackTypeDisabled(C.TRACK_TYPE_VIDEO, isAudioOnly).build()
                                gestureText = if (isAudioOnly) "Audio only" else "Video on"
                            }
                        }
                    }

                    @Composable
                    fun ZoneRow(list: List<PlayerButton>) {
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            verticalAlignment = Alignment.CenterVertically
                        ) { list.forEach { PlayerButtonView(it) } }
                    }

                    val topEndZone = pc.zone(if (isLandscape) PlayerZone.L_TOP_END else PlayerZone.P_TOP_END)
                    val startZone = pc.zone(if (isLandscape) PlayerZone.L_BOTTOM_START else PlayerZone.P_BOTTOM_START)
                    val endZone = pc.zone(if (isLandscape) PlayerZone.L_BOTTOM_END else PlayerZone.P_BOTTOM_END)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(top = if (isLandscape) 12.dp else 32.dp)
                            .align(Alignment.TopCenter),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CoverPlayChromePill {
                                CoverPlayChromeIconButton(painterResource(R.drawable.ic_back_custom), "Back", size = sz, backdrop = bd, onClick = onBack)
                            }
                            if (pc.showTitle) Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                                Text(
                                    text = formatFileName(video.name),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.SemiBold, fontSize = 16.sp,
                                        shadow = androidx.compose.ui.graphics.Shadow(Color.Black.copy(alpha = 0.6f), blurRadius = 10f)
                                    ),
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.widthIn(max = if (isLandscape) 420.dp else 200.dp)
                                )
                                if (!browserTitle.isNullOrBlank()) {
                                    Text(
                                        text = browserTitle,
                                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.6.sp),
                                        color = Color.White.copy(alpha = 0.62f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.widthIn(max = if (isLandscape) 420.dp else 200.dp)
                                    )
                                }
                            }
                        }
                        Box(Modifier.widthIn(max = if (isLandscape) 420.dp else 200.dp)) { ZoneRow(topEndZone) }
                    }

                    val isEnded = videoEnded
                    @Composable
                    fun Transport() {
                        Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            if (pc.showPrevNext) CoverPlayChromePill {
                                CoverPlayChromeIconButton(painterResource(R.drawable.ic_prev_custom), "Previous", size = sz, backdrop = bd) {
                                    if (exoPlayer.hasPreviousMediaItem()) { exoPlayer.seekToPreviousMediaItem(); gestureText = "PREVIOUS" }
                                    else gestureText = "PREVIOUS"
                                }
                            }
                            CoverPlayHeroPlayButton(isPlaying = isPlaying, isEnded = isEnded, size = pc.controlSize.play.dp) {
                                if (isEnded) { exoPlayer.seekTo(0); exoPlayer.play(); gestureText = "REPLAY" }
                                else if (exoPlayer.isPlaying) { exoPlayer.pause(); gestureText = "PAUSED" } else { exoPlayer.play(); gestureText = "PLAYING" }
                            }
                            if (pc.showPrevNext) CoverPlayChromePill {
                                CoverPlayChromeIconButton(painterResource(R.drawable.ic_next_custom), "Next", size = sz, backdrop = bd) {
                                    if (exoPlayer.hasNextMediaItem()) { exoPlayer.seekToNextMediaItem(); gestureText = "NEXT" }
                                    else gestureText = "NEXT"
                                }
                            }
                        }
                    }

                    @Composable
                    fun SeekRow() {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            if (pc.showTimeLabels) Text(
                                formatDuration(position),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold, fontSize = 12.sp, fontFeatureSettings = "tnum",
                                    shadow = androidx.compose.ui.graphics.Shadow(Color.Black.copy(alpha = 0.6f), blurRadius = 8f)
                                ),
                                color = Color.White,
                                modifier = Modifier.widthIn(min = 40.dp).padding(end = 8.dp)
                            )
                            CoverPlaySmoothPlayerScrubber(
                                exoPlayer = exoPlayer,
                                duration = duration,
                                videoUri = video.uri,
                                chapters = chapters,
                                onScrubbingChanged = { isScrubbing = it; if (it) showControls = true },
                                onPositionChanged = { position = it },
                                modifier = Modifier.weight(1f)
                            )
                            if (pc.showTimeLabels) Text(
                                if (pc.invertDuration) "−" + formatDuration((duration - position).coerceAtLeast(0L)) else formatDuration(duration),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold, fontSize = 12.sp, fontFeatureSettings = "tnum",
                                    shadow = androidx.compose.ui.graphics.Shadow(Color.Black.copy(alpha = 0.6f), blurRadius = 8f)
                                ),
                                color = Color.White.copy(alpha = 0.72f),
                                modifier = Modifier.widthIn(min = 40.dp).padding(start = 8.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.End
                            )
                        }
                    }

                    if (pc.transportPlacement == TransportPlacement.CENTER) {
                        Box(Modifier.align(Alignment.Center)) { Transport() }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(horizontal = 20.dp)
                            .padding(bottom = if (isLandscape) 10.dp else 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (pc.seekbarPlacement == SeekbarPlacement.ABOVE) SeekRow()
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) { ZoneRow(startZone) }
                            if (pc.transportPlacement == TransportPlacement.BOTTOM) Transport()
                            Box(Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) { ZoneRow(endZone) }
                        }
                        if (pc.seekbarPlacement == SeekbarPlacement.BELOW) SeekRow()
                    }

                    @Composable
                    fun EdgeRail(list: List<PlayerButton>, alignment: Alignment, atStart: Boolean) {
                        if (list.isEmpty()) return
                        Column(
                            modifier = Modifier
                                .align(alignment)
                                .padding(
                                    start = if (atStart) 10.dp else 0.dp,
                                    end = if (atStart) 0.dp else 10.dp,
                                    top = if (isLandscape) 64.dp else 96.dp,
                                    bottom = if (isLandscape) 84.dp else 110.dp
                                )
                                .clip(RoundedCornerShape(28.dp))
                                .background(Color.Black.copy(alpha = 0.36f))
                                .verticalScroll(rememberScrollState())
                                .padding(vertical = 6.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) { list.forEach { PlayerButtonView(it) } }
                    }
                    EdgeRail(
                        pc.zone(if (isLandscape) PlayerZone.L_EDGE_START else PlayerZone.P_EDGE_START),
                        Alignment.CenterStart, atStart = true
                    )
                    EdgeRail(
                        pc.zone(if (isLandscape) PlayerZone.L_EDGE_END else PlayerZone.P_EDGE_END),
                        Alignment.CenterEnd, atStart = false
                    )
                }
            }

            playbackError?.let { err ->
                CoverPlayCorruptVideoScene(
                    errorText = err,
                    onRetry = {
                        playbackError = null
                        didRetrySoftwareDecode = false
                        exoPlayer.prepare()
                        exoPlayer.play()
                    },
                    onBack = onBack,
                    modifier = Modifier.fillMaxSize()
                )
            }

            var lastGestureText by remember { mutableStateOf("") }
            if (gestureText.isNotEmpty()) {
                lastGestureText = gestureText
            }

            AnimatedVisibility(
                visible = gestureText.isNotEmpty(),
                enter = fadeIn(tween(150)) + scaleIn(initialScale = 0.85f, animationSpec = spring(0.7f, 500f)) + slideInVertically(spring(0.8f, 500f)) { -it / 2 },
                exit = fadeOut(tween(180)) + scaleOut(targetScale = 0.9f, animationSpec = tween(180)),
                modifier = Modifier.align(BiasAlignment(0f, -0.7f))
            ) {
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.58f))
                        .padding(horizontal = 14.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (lastGestureText.endsWith("x", ignoreCase = true) && lastGestureText.firstOrNull()?.isDigit() == true) {
                        Icon(painterResource(R.drawable.ic_forward_custom), null, tint = Color.White, modifier = Modifier.size(14.dp))
                    }
                    Text(
                        lastGestureText,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold, fontSize = 13.sp, letterSpacing = 0.2.sp),
                        color = Color.White,
                        maxLines = 1
                    )
                }
            }

            CoverPlayPlayerLockOverlay(
                locked = isControlsLocked,
                controlsVisible = showControls,
                showTimer = subtleLockTimer,
                positionMs = position,
                onUnlock = {
                    isControlsLocked = false
                    showControls = true
                    gestureText = "Unlocked"
                },
                modifier = Modifier.fillMaxSize()
            )

            CoverPlayGestureIndicator(
                visible = showVerticalBar,
                isBrightness = isBarBrightness,
                value = gestureValue,
                modifier = Modifier.fillMaxSize(),
                swapSides = pc.swapBrightnessVolume
            )

            Box(modifier = Modifier.fillMaxSize()) {
                CoverPlaySeekFlash(seekAnimationSide == "left", seekAnimationToken, cumulativeSeekAmount, false, Modifier.align(Alignment.CenterStart), seekTapX, seekTapY)
                CoverPlaySeekFlash(seekAnimationSide == "right", seekAnimationToken, cumulativeSeekAmount, true, Modifier.align(Alignment.CenterEnd), seekTapX, seekTapY)
            }
            if (showHUD) CoverPlayStatsHud(video, currentVideoSize, exoPlayer, onDismiss = { showHUD = false })
            CoverPlayTrackSelectionPanel(showAudioSelector, exoPlayer, C.TRACK_TYPE_AUDIO, { showAudioSelector = false })
            CoverPlayTrackSelectionPanel(
                showSubtitleSelector, exoPlayer, C.TRACK_TYPE_TEXT, { showSubtitleSelector = false },
                onAddLocalSubtitle = { showLocalSubtitleDialog = true },
                onSearchOnline = { showOnlineSubtitleSearch = true },
                onCustomize = { showSubtitleCustomization = true }
            )
            if (showSubtitleCustomization) CoverPlaySubtitleCustomizationDialog { showSubtitleCustomization = false; subtitleStyleVersion++ }
            if (showOnlineSubtitleSearch) {
                CoverPlayOnlineSubtitleSearchDialog(video.name, { showOnlineSubtitleSearch = false }) { url ->
                    attachSubtitle(Uri.parse(url))
                }
            }
            if (showLocalSubtitleDialog) {
                CoverPlayLocalSubtitleDialog(
                    video = video,
                    onDismiss = { showLocalSubtitleDialog = false },
                    onPick = { uri -> showLocalSubtitleDialog = false; attachSubtitle(uri) },
                    onBrowse = { showLocalSubtitleDialog = false; subtitlePickerLauncher.launch(arrayOf("*/*")) }
                )
            }
            CoverPlayPlaybackSpeedSidePanel(
                showSpeedSelector, currentSpeed, { currentSpeed = it; exoPlayer.setPlaybackSpeed(it) }, { showSpeedSelector = false },
                pitch = currentPitch,
                onPitchChanged = { currentPitch = it; exoPlayer.playbackParameters = exoPlayer.playbackParameters.withPitch(it) }
            )

            CoverPlayPlayerPanel(
                visible = showFolderVideos && folderVideos.size > 1,
                onDismiss = { showFolderVideos = false; autoNextActive = false },
                title = if (videoEnded && autoNextActive && autoNextSeconds > 0) "Up next"
                else browserTitle ?: "Media Library",
                subtitle = "${folderVideos.size} videos" + (browserSubtitle?.let { " · $it" } ?: ""),
                scrollable = false
            ) {
                if (videoEnded && autoNextActive && autoNextSeconds > 0) {
                    CoverPlayUpNextCard(
                        next = upNext,
                        seconds = autoNextSeconds,
                        total = 5,
                        onPlayNow = {
                            autoNextActive = false
                            showFolderVideos = false
                            exoPlayer.seekToNextMediaItem()
                            exoPlayer.play()
                        },
                        onCancel = { autoNextActive = false }
                    )
                }

                LazyColumn(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    items(folderVideos, key = { it.id }) { item ->
                        val isCurrent = item.id == video.id
                        CoverPlayBrowserVideoRow(
                            video = item,
                            isSelected = isCurrent,
                            onClick = { autoNextActive = false; if (!isCurrent) { showFolderVideos = false; onPlayVideo(item) } }
                        )
                    }
                }
            }

            val iconInfo = androidx.compose.ui.graphics.vector.rememberVectorPainter(androidx.compose.material.icons.Icons.Rounded.Info)
            CoverPlayPlayerPanel(
                visible = showExpressiveSheet,
                onDismiss = { showExpressiveSheet = false },
                title = "Playback settings",
                subtitle = formatFileName(video.name)
            ) {
                CoverPlayPlayerPanelSection("Playback speed") {
                    CoverPlayPlayerPanelCard {
                        Row(
                            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 10.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(0.5f, 0.75f, 1f, 1.25f, 1.5f, 2f, 3f).forEach { s ->
                                CoverPlayMoreSpeedChip(speedLabel(s), abs(currentSpeed - s) < 0.01f) { currentSpeed = s; exoPlayer.setPlaybackSpeed(s) }
                            }
                        }
                        CoverPlayPlayerPanelDivider()
                        CoverPlayPlayerPanelNavRow(painterResource(R.drawable.ic_speed_custom), "Fine-tune speed",
                            value = speedLabel(currentSpeed), active = currentSpeed != 1.0f) {
                            showExpressiveSheet = false; showSpeedSelector = true
                        }
                    }
                }

                CoverPlayPlayerPanelSection("Playback mode") {
                    CoverPlayPlayerPanelCard {
                        CoverPlayPlayerPanelNavRow(
                            painterResource(if (repeatMode == Player.REPEAT_MODE_ONE) R.drawable.ic_repeat_one_custom else R.drawable.ic_repeat_custom),
                            "Repeat",
                            value = when (repeatMode) { Player.REPEAT_MODE_ALL -> "All"; Player.REPEAT_MODE_ONE -> "One"; else -> "Off" },
                            active = repeatMode != Player.REPEAT_MODE_OFF,
                            showChevron = false
                        ) {
                            repeatMode = when (repeatMode) {
                                Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
                                Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
                                else -> Player.REPEAT_MODE_OFF
                            }
                            exoPlayer.repeatMode = repeatMode
                            gestureText = when (repeatMode) { Player.REPEAT_MODE_ALL -> "Repeat All"; Player.REPEAT_MODE_ONE -> "Repeat One"; else -> "Repeat Off" }
                        }
                        CoverPlayPlayerPanelDivider()
                        CoverPlayPlayerPanelToggleRow(painterResource(R.drawable.ic_shuffle_custom), "Shuffle", isShuffleEnabled) {
                            isShuffleEnabled = it
                            gestureText = if (it) "Shuffle On" else "Shuffle Off"
                        }
                        CoverPlayPlayerPanelDivider()
                        CoverPlayPlayerPanelToggleRow(painterResource(if (isBackgroundPlayEnabled) R.drawable.ic_headphones_custom else R.drawable.ic_headphones_off_custom),
                            "Background play", isBackgroundPlayEnabled, subtitle = "Keep audio playing when the app is closed") {
                            isBackgroundPlayEnabled = it
                            gestureText = if (it) "Background on" else "Background off"
                        }
                        CoverPlayPlayerPanelDivider()
                        CoverPlayPlayerPanelToggleRow(painterResource(if (isMuted) R.drawable.ic_volume_off_custom else R.drawable.ic_volume_custom), "Mute", isMuted) {
                            isMuted = it
                            val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, if (it) 0 else maxVol / 2, 0)
                            gestureText = if (it) "Muted" else "Unmuted"
                        }
                    }
                }

                CoverPlayPlayerPanelSection("Tools") {
                    CoverPlayPlayerPanelCard {
                        CoverPlayPlayerPanelNavRow(
                            painterResource(R.drawable.ic_stopwatch_custom), "Sleep timer",
                            value = if (sleepTimerMinutes == 0) "Off" else "$sleepTimerMinutes min",
                            active = sleepTimerMinutes != 0, showChevron = false
                        ) {
                            sleepTimerMinutes = when (sleepTimerMinutes) { 0 -> 15; 15 -> 30; 30 -> 45; 45 -> 60; else -> 0 }
                            sleepTimerEndsAt = if (sleepTimerMinutes == 0) 0L else System.currentTimeMillis() + sleepTimerMinutes * 60_000L
                            gestureText = if (sleepTimerMinutes == 0) "Sleep timer off" else "Sleep in $sleepTimerMinutes min"
                        }
                        CoverPlayPlayerPanelDivider()
                        CoverPlayPlayerPanelNavRow(
                            painterResource(R.drawable.ic_repeat_custom), "A–B loop",
                            value = when {
                                abLoopA < 0L -> "Set A"
                                abLoopB < 0L -> "Set B"
                                else -> "On · tap to clear"
                            },
                            active = abLoopA >= 0L, showChevron = false
                        ) {
                            val cur = exoPlayer.currentPosition
                            when {
                                abLoopA < 0L -> { abLoopA = cur; gestureText = "A · ${formatDuration(cur)}" }
                                abLoopB < 0L -> {
                                    if (cur > abLoopA + 500L) { abLoopB = cur; gestureText = "Looping A–B" }
                                    else gestureText = "Pick a later point for B"
                                }
                                else -> { abLoopA = -1L; abLoopB = -1L; gestureText = "A–B loop off" }
                            }
                        }
                        CoverPlayPlayerPanelDivider()
                        CoverPlayPlayerPanelNavRow(painterResource(R.drawable.ic_prev_custom), "Previous frame", showChevron = false) { stepFrame(false) }
                        CoverPlayPlayerPanelDivider()
                        CoverPlayPlayerPanelNavRow(painterResource(R.drawable.ic_next_custom), "Next frame", showChevron = false) { stepFrame(true) }
                        CoverPlayPlayerPanelDivider()
                        CoverPlayPlayerPanelToggleRow(painterResource(R.drawable.ic_headphones_custom), "Audio only", isAudioOnly, subtitle = "Turn the picture off, keep the sound") {
                            isAudioOnly = it
                            exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters.buildUpon()
                                .setTrackTypeDisabled(C.TRACK_TYPE_VIDEO, it).build()
                            gestureText = if (it) "Audio only" else "Video on"
                        }
                        CoverPlayPlayerPanelDivider()
                        CoverPlayPlayerPanelToggleRow(painterResource(R.drawable.ic_orientation_custom), "Mirror video", isMirrored) {
                            isMirrored = it
                            gestureText = if (it) "Mirrored" else "Normal"
                        }
                        CoverPlayPlayerPanelDivider()
                        CoverPlayPlayerPanelNavRow(painterResource(R.drawable.ic_thumbnail), "Save screenshot", showChevron = false) { captureFrame() }
                    }
                }

                CoverPlayPlayerPanelSection("Media") {
                    CoverPlayPlayerPanelCard {
                        CoverPlayPlayerPanelNavRow(painterResource(R.drawable.ic_music_custom), "Audio track",
                            value = selectedTrackLabel(exoPlayer, C.TRACK_TYPE_AUDIO, "Default")) {
                            showExpressiveSheet = false; showAudioSelector = true
                        }
                        CoverPlayPlayerPanelDivider()
                        CoverPlayPlayerPanelNavRow(painterResource(R.drawable.ic_subtitles_custom), "Subtitles",
                            value = selectedTrackLabel(exoPlayer, C.TRACK_TYPE_TEXT, "Default")) {
                            showExpressiveSheet = false; showSubtitleSelector = true
                        }
                        if (folderVideos.size > 1) {
                            CoverPlayPlayerPanelDivider()
                            CoverPlayPlayerPanelNavRow(painterResource(R.drawable.ic_browse_list_custom), "Playlist", value = "${folderVideos.size} videos") {
                                showExpressiveSheet = false; showFolderVideos = true
                            }
                        }
                        CoverPlayPlayerPanelDivider()
                        CoverPlayPlayerPanelNavRow(painterResource(R.drawable.ic_info_custom), "Media info") { showExpressiveSheet = false; showHUD = true }
                    }
                }

                CoverPlayPlayerPanelSection("Display") {
                    CoverPlayPlayerPanelCard {
                        CoverPlayPlayerPanelToggleRow(painterResource(R.drawable.ic_orientation_custom), "Force landscape", isForcedLandscape) {
                            isForcedLandscape = it
                            gestureText = if (it) "Landscape" else "Auto rotate"
                        }
                        CoverPlayPlayerPanelDivider()
                        CoverPlayPlayerPanelNavRow(
                            painterResource(when (resizeModeIndex) { 0 -> R.drawable.ic_aspect_fit; 1 -> R.drawable.ic_aspect_fill; else -> R.drawable.ic_aspect_zoom }),
                            "Aspect ratio", value = resizeModes[resizeModeIndex].second, showChevron = false
                        ) {
                            resizeModeIndex = (resizeModeIndex + 1) % resizeModes.size
                            gestureText = resizeModes[resizeModeIndex].second
                        }
                        CoverPlayPlayerPanelDivider()
                        CoverPlayPlayerPanelNavRow(painterResource(R.drawable.ic_lock_custom), "Lock screen") {
                            showExpressiveSheet = false; isControlsLocked = true
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CoverPlayUpNextCard(
    next: VideoFile?,
    seconds: Int,
    total: Int,
    onPlayNow: () -> Unit,
    onCancel: () -> Unit
) {
    val accent = LocalAppUiSettings.current.accent
    val prog by animateFloatAsState(seconds / total.toFloat(), tween(1000, easing = LinearEasing), label = "upNextRing")
    val shape = RoundedCornerShape(CoverPlayTokens.Radius.card)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .clip(shape)
            .background(CoverPlayTokens.Palette.elevated)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(Modifier.size(44.dp), contentAlignment = Alignment.Center) {
                androidx.compose.foundation.Canvas(Modifier.fillMaxSize()) {
                    val sw = 4.dp.toPx()
                    val inset = sw / 2f
                    val arcSize = androidx.compose.ui.geometry.Size(size.width - sw, size.height - sw)
                    val tl = androidx.compose.ui.geometry.Offset(inset, inset)
                    drawArc(CoverPlayTokens.Palette.highest, 0f, 360f, false, tl, arcSize,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(sw))
                    drawArc(accent, -90f, 360f * prog, false, tl, arcSize,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(sw, cap = androidx.compose.ui.graphics.StrokeCap.Round))
                }
                Text(
                    "$seconds",
                    color = CoverPlayTokens.Palette.textPrimary,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold, fontFeatureSettings = "tnum")
                )
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    "Playing in ${seconds}s",
                    color = CoverPlayTokens.Palette.textMuted,
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    next?.let { formatFileName(it.name) } ?: "Next video",
                    color = CoverPlayTokens.Palette.textPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                Modifier
                    .weight(1f)
                    .height(38.dp)
                    .clip(CircleShape)
                    .background(accent)
                    .clickable { onPlayNow() },
                contentAlignment = Alignment.Center
            ) {
                Text("Play now", color = accent.onFill(), style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold))
            }
            Box(
                Modifier
                    .weight(1f)
                    .height(38.dp)
                    .clip(CircleShape)
                    .background(CoverPlayTokens.Palette.highest)
                    .clickable { onCancel() },
                contentAlignment = Alignment.Center
            ) {
                Text("Cancel", color = CoverPlayTokens.Palette.textPrimary, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold))
            }
        }
    }
}

@Composable
private fun CoverPlayChromePill(content: @Composable RowScope.() -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, content = content)
}

@Composable
private fun CoverPlayChromeDivider() {
    Spacer(Modifier.width(2.dp))
}

@Composable
private fun CoverPlayChromeIconButton(
    painter: Painter,
    description: String,
    active: Boolean = false,
    size: androidx.compose.ui.unit.Dp = 36.dp,
    backdrop: Boolean = false,
    onClick: () -> Unit
) {
    val settings = LocalAppUiSettings.current
    val glyph = (size.value * 0.56f).dp
    val interaction = remember { MutableInteractionSource() }
    val press by rememberCoverPlayPressScale(interaction, 0.86f)
    val haptic = rememberCoverPlayHaptic()
    val sel by animateFloatAsState(if (active) 1f else 0f, tween(180), label = "chromeSel")
    val tint = lerp(Color.White, lerp(settings.accent, settings.highlight, 0.55f), sel.coerceIn(0f, 1f))

    Box(
        modifier = Modifier
            .size(size)
            .graphicsLayer { scaleX = press; scaleY = press }
            .clip(CircleShape)
            .then(if (backdrop) Modifier.background(Color.Black.copy(alpha = 0.38f)) else Modifier)
            .clickable(interactionSource = interaction, indication = null) { haptic(); onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (!backdrop && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Icon(
                painter, null, tint = Color.Black.copy(alpha = 0.55f),
                modifier = Modifier.size(glyph).offset(y = 1.dp)
                    .blur(3.dp, androidx.compose.ui.draw.BlurredEdgeTreatment.Unbounded)
            )
        } else if (!backdrop) {
            Box(
                Modifier.size(size * 0.9f).background(
                    Brush.radialGradient(listOf(Color.Black.copy(alpha = 0.22f), Color.Transparent)), CircleShape
                )
            )
        }
        Icon(painter, description, tint = tint, modifier = Modifier.size(glyph))
    }
}

@Composable
private fun CoverPlayHeroPlayButton(
    isPlaying: Boolean,
    isEnded: Boolean,
    size: androidx.compose.ui.unit.Dp = 52.dp,
    onClick: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val scale by rememberCoverPlayPressScale(interaction, 0.92f)
    val haptic = rememberCoverPlayHaptic(HapticFeedbackType.LongPress)
    val accent = LocalAppUiSettings.current.accent
    val ink = Color.White

    Box(
        modifier = Modifier
            .size(size)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(CircleShape)
            .clickable(interactionSource = interaction, indication = null) {
                haptic()
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        CoverPlayRotatingShape(
            shape = CoverPlayMaterialShapes.cookie9(),
            color = accent.copy(alpha = 0.92f),
            periodMillis = if (isPlaying) 22000 else 44000,
            modifier = Modifier.fillMaxSize()
        )
        AnimatedContent(
            targetState = when { isEnded -> 0; isPlaying -> 1; else -> 2 },
            transitionSpec = {
                (fadeIn(tween(140)) + scaleIn(initialScale = 0.7f, animationSpec = tween(140)))
                    .togetherWith(fadeOut(tween(90)) + scaleOut(targetScale = 0.7f, animationSpec = tween(90)))
            },
            label = "heroIcon"
        ) { state ->
            Icon(
                painterResource(
                    when (state) {
                        0 -> R.drawable.ic_replay_custom
                        1 -> R.drawable.ic_pause_custom
                        else -> R.drawable.ic_play_custom
                    }
                ),
                contentDescription = if (state == 1) "Pause" else "Play",
                tint = ink,
                modifier = Modifier.size(size * 0.42f)
            )
        }
    }
}

@Composable
fun CoverPlayBrowserVideoRow(video: VideoFile, isSelected: Boolean, onClick: () -> Unit) {
    val context = LocalContext.current
    val ui = LocalAppUiSettings.current
    val accent = ui.accent
    val haptic = rememberCoverPlayHaptic()
    val shape = RoundedCornerShape(CoverPlayTokens.Radius.compactCard)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (isSelected) accent.copy(alpha = 0.20f) else CoverPlayTokens.Palette.elevated)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { haptic(); onClick() }
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(96.dp, 56.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(CoverPlayTokens.Palette.highest)
        ) {
            if (ui.hideThumbnails) {
                CoverPlayTriangleThumbPlaceholder(modifier = Modifier.fillMaxSize())
            } else {
                AsyncImage(
                    model = ImageRequest.Builder(context).data(video.uri).videoFrameMillis(1000).crossfade(true).build(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            if (isSelected) {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.45f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(painterResource(R.drawable.ic_play_custom), null, tint = Color.White, modifier = Modifier.size(22.dp))
                }
            }
            Box(modifier = Modifier.align(Alignment.BottomEnd).padding(4.dp)) {
                CoverPlayDurationBadge(text = formatDuration(video.duration))
            }
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = formatFileName(video.name),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium),
                color = CoverPlayTokens.Palette.textPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            if (video.width > 0) {
                CoverPlayQualityBadge(width = video.width, height = video.height)
            }
        }
    }
}

@UnstableApi
@Composable
fun CoverPlayPlaybackSpeedSidePanel(
    visible: Boolean,
    currentSpeed: Float,
    onSpeedSelected: (Float) -> Unit,
    onDismiss: () -> Unit,
    pitch: Float = 1f,
    onPitchChanged: ((Float) -> Unit)? = null
) {
    val accent = LocalAppUiSettings.current.accent
    CoverPlayPlayerPanel(visible = visible, onDismiss = onDismiss, title = "Playback speed") {
        CoverPlayPlayerPanelSection("Current") {
            CoverPlayPlayerPanelCard {
                Column(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp)) {
                    Text(
                        speedLabel(currentSpeed), color = CoverPlayTokens.Palette.textPrimary,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold, fontFeatureSettings = "tnum")
                    )
                    CoverPlaySlider(
                        value = currentSpeed,
                        onValueChange = { onSpeedSelected((it * 20f).roundToInt() / 20f) },
                        valueRange = 0.25f..3f
                    )
                }
            }
        }
        CoverPlayPlayerPanelSection("Presets") {
            CoverPlayPlayerPanelCard {
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 10.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(0.5f, 0.75f, 1f, 1.25f, 1.5f, 2f, 3f).forEach { s ->
                        CoverPlayMoreSpeedChip(speedLabel(s), abs(currentSpeed - s) < 0.01f) { onSpeedSelected(s) }
                    }
                }
                CoverPlayPlayerPanelDivider()
                CoverPlayPlayerPanelNavRow(painterResource(R.drawable.ic_stopwatch_custom), "Reset to 1x", showChevron = false) { onSpeedSelected(1.0f) }
            }
        }
        if (onPitchChanged != null) {
            CoverPlayPlayerPanelSection("Pitch") {
                CoverPlayPlayerPanelCard {
                    Column(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp)) {
                        Text(
                            "%.2fx".format(pitch), color = CoverPlayTokens.Palette.textPrimary,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold, fontFeatureSettings = "tnum")
                        )
                        CoverPlaySlider(
                                value = pitch,
                            onValueChange = { onPitchChanged(((it * 20f).roundToInt() / 20f).coerceIn(0.5f, 2f)) },
                            valueRange = 0.5f..2f
                        )
                    }
                    CoverPlayPlayerPanelDivider()
                    CoverPlayPlayerPanelNavRow(painterResource(R.drawable.ic_stopwatch_custom), "Reset pitch", showChevron = false) { onPitchChanged(1.0f) }
                }
            }
        }
    }
}

private fun selectedTrackLabel(player: ExoPlayer, type: Int, fallback: String): String {
    if (type == C.TRACK_TYPE_TEXT && player.trackSelectionParameters.disabledTrackTypes.contains(C.TRACK_TYPE_TEXT)) return "Off"
    for (g in player.currentTracks.groups) {
        if (g.type != type) continue
        for (i in 0 until g.length) {
            if (g.isTrackSelected(i)) {
                val f = g.getTrackFormat(i)
                return f.label ?: f.language?.uppercase() ?: fallback
            }
        }
    }
    return fallback
}

@UnstableApi
@Composable
fun CoverPlayTrackSelectionPanel(
    visible: Boolean,
    player: ExoPlayer,
    trackType: Int,
    onDismiss: () -> Unit,
    onAddLocalSubtitle: () -> Unit = {},
    onSearchOnline: (() -> Unit)? = null,
    onCustomize: (() -> Unit)? = null
) {
    val isAudio = trackType == C.TRACK_TYPE_AUDIO
    CoverPlayPlayerPanel(visible = visible, onDismiss = onDismiss, title = if (isAudio) "Audio track" else "Subtitles") {
        val groups = player.currentTracks.groups.filter { it.type == trackType }
        val textOff = player.trackSelectionParameters.disabledTrackTypes.contains(C.TRACK_TYPE_TEXT)
        CoverPlayPlayerPanelSection(if (isAudio) "Available tracks" else "Subtitle track") {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (!isAudio) {
                    CoverPlayPlayerPanelOptionRow("Off", "No subtitles", selected = textOff) {
                        player.trackSelectionParameters = player.trackSelectionParameters.buildUpon()
                            .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true).build()
                        onDismiss()
                    }
                }
                if (groups.isEmpty() && isAudio) {
                    Text("No audio tracks found", color = CoverPlayTokens.Palette.textSecondary, modifier = Modifier.padding(start = 4.dp))
                }
                groups.forEach { group ->
                    for (i in 0 until group.length) {
                        val track = group.getTrackFormat(i)
                        val selected = group.isTrackSelected(i) && (isAudio || !textOff)
                        val sub = listOfNotNull(
                            track.language?.uppercase(),
                            if (isAudio && track.channelCount > 0) "${track.channelCount}ch" else null
                        ).joinToString(" · ").ifEmpty { null }
                        CoverPlayPlayerPanelOptionRow(track.label ?: (if (isAudio) "Track ${i + 1}" else "Subtitle ${i + 1}"), sub, selected) {
                            player.trackSelectionParameters = player.trackSelectionParameters.buildUpon()
                                .apply { if (!isAudio) setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false) }
                                .setOverrideForType(TrackSelectionOverride(group.mediaTrackGroup, i))
                                .build()
                            onDismiss()
                        }
                    }
                }
            }
        }
        if (!isAudio) {
            CoverPlayPlayerPanelSection("More") {
                CoverPlayPlayerPanelCard {
                    CoverPlayPlayerPanelNavRow(painterResource(R.drawable.ic_plus_custom), "Add local subtitle") { onDismiss(); onAddLocalSubtitle() }
                    if (onSearchOnline != null) {
                        CoverPlayPlayerPanelDivider()
                        CoverPlayPlayerPanelNavRow(painterResource(R.drawable.ic_search_custom), "Search online") { onDismiss(); onSearchOnline() }
                    }
                    if (onCustomize != null) {
                        CoverPlayPlayerPanelDivider()
                        CoverPlayPlayerPanelNavRow(painterResource(R.drawable.ic_subtitles_custom), "Appearance") { onDismiss(); onCustomize() }
                    }
                }
            }
        }
    }
}

private data class SubtitleStyleSpec(val size: Float, val color: Int, val bgAlpha: Float, val bold: Boolean)

fun saveScreenshot(context: Context, bitmap: Bitmap, baseName: String): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return false
    val resolver = context.contentResolver
    val values = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, "${baseName}_${System.currentTimeMillis()}.png")
        put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CoverPlay")
        put(MediaStore.Images.Media.IS_PENDING, 1)
    }
    val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return false
    return try {
        resolver.openOutputStream(uri)?.use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        values.clear()
        values.put(MediaStore.Images.Media.IS_PENDING, 0)
        resolver.update(uri, values, null, null)
        true
    } catch (e: Exception) {
        runCatching { resolver.delete(uri, null, null) }
        false
    }
}

fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}

private const val RESUME_MIN_POSITION_MS = 5_000L
private const val RESUME_NEAR_END_FRACTION = 0.95f

fun savePlaybackPosition(context: Context, path: String, positionMs: Long, durationMs: Long) {
    val prefs = context.getSharedPreferences("playback_positions", Context.MODE_PRIVATE)
    if (durationMs <= 0) return
    val isNearEnd = positionMs >= durationMs * RESUME_NEAR_END_FRACTION
    if (positionMs < RESUME_MIN_POSITION_MS || isNearEnd) {
        prefs.edit().remove(path).apply()
    } else {
        prefs.edit().putLong(path, positionMs).apply()
    }
}

fun updateOrientation(context: Context, isFullscreen: Boolean, isLandscape: Boolean = true, isDarkTheme: Boolean = true) {
    val activity = context.findActivity()
    activity?.let {
        val window = it.window
        val decorView = window.decorView
        val controller = WindowInsetsControllerCompat(window, decorView)

        if (isFullscreen) {
            it.requestedOrientation = if (isLandscape)
                ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            else
                ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
            WindowCompat.setDecorFitsSystemWindows(window, false)
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                window.attributes.layoutInDisplayCutoutMode =
                    android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        } else {
            it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            WindowCompat.setDecorFitsSystemWindows(window, true)
            controller.show(WindowInsetsCompat.Type.systemBars())

            controller.isAppearanceLightStatusBars = !isDarkTheme
            controller.isAppearanceLightNavigationBars = !isDarkTheme

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                window.attributes.layoutInDisplayCutoutMode =
                    android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT
            }
        }
    }
}

fun createVideoFileFromUri(context: Context, uri: Uri): VideoFile {
    var name = uri.lastPathSegment ?: "External Video"
    var size = 0L
    var duration = 0L
    var width = 0
    var height = 0
    var path = uri.toString()

    if (uri.scheme == "content") {
        val projection = arrayOf(
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.WIDTH,
            MediaStore.Video.Media.HEIGHT,
            MediaStore.Video.Media.DATA
        )
        context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)) ?: name
                size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE))
                duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION))
                width = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.WIDTH))
                height = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.HEIGHT))
                path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)) ?: path
            }
        }
    } else if (uri.scheme == "file") {
        path = uri.path ?: path
        name = uri.lastPathSegment ?: name
        val file = File(path)
        if (file.exists()) {
            size = file.length()
        }
    }

    return VideoFile(
        id = System.currentTimeMillis(),
        name = name,
        path = path,
        duration = duration,
        size = size,
        uri = uri,
        width = width,
        height = height
    )
}

fun fetchAllVideos(context: Context, videos: MutableList<VideoFile>) {
    val tempVideos = mutableListOf<VideoFile>()
    val projection = arrayOf(
        MediaStore.Video.Media._ID,
        MediaStore.Video.Media.DISPLAY_NAME,
        MediaStore.Video.Media.DATA,
        MediaStore.Video.Media.DURATION,
        MediaStore.Video.Media.SIZE,
        MediaStore.Video.Media.WIDTH,
        MediaStore.Video.Media.HEIGHT,
        MediaStore.Video.Media.DATE_ADDED
    )

    val cursor = context.contentResolver.query(
        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
        projection,
        null,
        null,
        "${MediaStore.Video.Media.DATE_ADDED} DESC"
    )

    cursor?.use {
        val idCol = it.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
        val nameCol = it.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
        val pathCol = it.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
        val durCol = it.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
        val sizeCol = it.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
        val widthCol = it.getColumnIndexOrThrow(MediaStore.Video.Media.WIDTH)
        val heightCol = it.getColumnIndexOrThrow(MediaStore.Video.Media.HEIGHT)
        val dateCol = it.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)

        while (it.moveToNext()) {
            val id = it.getLong(idCol)
            val name = it.getString(nameCol)
            val path = it.getString(pathCol)
            val duration = it.getLong(durCol)
            val size = it.getLong(sizeCol)
            val width = it.getInt(widthCol)
            val height = it.getInt(heightCol)
            val dateAdded = it.getLong(dateCol)
            val contentUri =
                ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)

            tempVideos.add(VideoFile(id, name, path, duration, size, contentUri, width, height, dateAdded))
        }
    }
    videos.clear()
    videos.addAll(tempVideos)
}

fun fetchVideoFolders(context: Context, folders: MutableList<VideoFolder>) {
    val tempFoldersList = mutableListOf<VideoFolder>()
    val projection = arrayOf(
        MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
        MediaStore.Video.Media.DATA
    )

    val cursor = context.contentResolver.query(
        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
        projection,
        null,
        null,
        null
    )

    val tempFoldersMap = mutableMapOf<String, Pair<String, Int>>()

    cursor?.use {
        val bucketColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)
        val dataColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)

        while (it.moveToNext()) {
            val bucketName = it.getString(bucketColumn) ?: "Internal"
            val filePath = it.getString(dataColumn)
            val folderPath = File(filePath).parent ?: ""

            val current = tempFoldersMap[bucketName] ?: Pair(folderPath, 0)
            tempFoldersMap[bucketName] = Pair(current.first, current.second + 1)
        }
    }

    tempFoldersMap.forEach { (name, info) ->
        tempFoldersList.add(VideoFolder(name, info.first, info.second, "Local"))
    }
    folders.clear()
    folders.addAll(tempFoldersList)
}

fun fetchVideosInFolder(context: Context, folderPath: String, videos: MutableList<VideoFile>) {
    val tempVideos = mutableListOf<VideoFile>()

    if (folderPath == "/" || folderPath == "/all" || folderPath.equals("all media", ignoreCase = true)) {
        fetchAllVideos(context, tempVideos)
        videos.clear()
        videos.addAll(tempVideos)
        return
    }

    if (folderPath == "/recent" || folderPath.equals("recent videos", ignoreCase = true) || folderPath.equals("recent", ignoreCase = true)) {
        fetchAllVideos(context, tempVideos)
        val sortedByDate = tempVideos.sortedByDescending { if (it.dateAdded > 0) it.dateAdded else it.id }
        videos.clear()
        videos.addAll(sortedByDate.take(30))
        return
    }

    if (folderPath == "/download" || folderPath == "/downloads" || folderPath.equals("downloads", ignoreCase = true) || folderPath.equals("download", ignoreCase = true)) {
        fetchAllVideos(context, tempVideos)
        val matching = tempVideos.filter {
            it.path.contains("download", ignoreCase = true) || it.path.contains("downloads", ignoreCase = true)
        }
        videos.clear()
        videos.addAll(matching.ifEmpty { tempVideos })
        return
    }

    if (folderPath == "/camera" || folderPath.equals("camera", ignoreCase = true)) {
        fetchAllVideos(context, tempVideos)
        val matching = tempVideos.filter {
            it.path.contains("camera", ignoreCase = true) || it.path.contains("dcim", ignoreCase = true)
        }
        videos.clear()
        videos.addAll(matching.ifEmpty { tempVideos })
        return
    }

    val projection = arrayOf(
        MediaStore.Video.Media._ID,
        MediaStore.Video.Media.DISPLAY_NAME,
        MediaStore.Video.Media.DATA,
        MediaStore.Video.Media.DURATION,
        MediaStore.Video.Media.SIZE,
        MediaStore.Video.Media.WIDTH,
        MediaStore.Video.Media.HEIGHT,
        MediaStore.Video.Media.DATE_ADDED
    )

    val selection = "${MediaStore.Video.Media.DATA} LIKE ?"
    val selectionArgs = arrayOf("$folderPath/%")

    val cursor = context.contentResolver.query(
        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
        projection,
        selection,
        selectionArgs,
        "${MediaStore.Video.Media.DATE_ADDED} DESC"
    )

    cursor?.use {
        val idCol = it.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
        val nameCol = it.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
        val pathCol = it.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
        val durCol = it.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
        val sizeCol = it.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
        val widthCol = it.getColumnIndexOrThrow(MediaStore.Video.Media.WIDTH)
        val heightCol = it.getColumnIndexOrThrow(MediaStore.Video.Media.HEIGHT)
        val dateCol = it.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)

        while (it.moveToNext()) {
            val id = it.getLong(idCol)
            val name = it.getString(nameCol)
            val path = it.getString(pathCol)
            val duration = it.getLong(durCol)
            val size = it.getLong(sizeCol)
            val width = it.getInt(widthCol)
            val height = it.getInt(heightCol)
            val dateAdded = it.getLong(dateCol)
            val contentUri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)

            tempVideos.add(VideoFile(id, name, path, duration, size, contentUri, width, height, dateAdded))
        }
    }

    if (tempVideos.isEmpty()) {
        val normalizedFolder = runCatching { File(folderPath).canonicalPath }.getOrDefault(folderPath)
        fetchAllVideos(context, tempVideos)
        val inFolder = tempVideos.filter {
            runCatching {
                val parent = File(it.path).parentFile?.canonicalPath ?: ""
                parent == normalizedFolder || parent.startsWith(normalizedFolder)
            }.getOrDefault(false)
        }
        if (inFolder.isNotEmpty()) {
            tempVideos.clear()
            tempVideos.addAll(inFolder)
        }
    }

    videos.clear()
    videos.addAll(tempVideos)
}

@UnstableApi
@Composable
fun CoverPlaySubtitleCustomizationDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    val prefs = remember { context.getSharedPreferences("subtitle_prefs", android.content.Context.MODE_PRIVATE) }

    var fontSize by remember { mutableFloatStateOf(prefs.getFloat("font_size", 16f)) }
    var textColor by remember { mutableIntStateOf(prefs.getInt("text_color", 0xFFFFFFFF.toInt())) }
    var bgAlpha by remember { mutableFloatStateOf(prefs.getFloat("bg_alpha", 0.5f)) }
    var boldEnabled by remember { mutableStateOf(prefs.getBoolean("bold", false)) }

    val colorOptions = listOf(
        0xFFFFFFFF.toInt() to "White",
        0xFFFFFF00.toInt() to "Yellow",
        0xFF00FFFF.toInt() to "Cyan",
        0xFFFF8C00.toInt() to "Orange",
        0xFFFF4444.toInt() to "Red"
    )

    fun savePrefs() {
        prefs.edit()
            .putFloat("font_size", fontSize)
            .putInt("text_color", textColor)
            .putFloat("bg_alpha", bgAlpha)
            .putBoolean("bold", boldEnabled)
            .apply()
    }

    CoverPlayDialog(
        onDismiss = { savePrefs(); onDismiss() },
        title = "Subtitle Appearance",
        modifier = Modifier.fillMaxWidth(if (isLandscape) 0.6f else 0.95f)
    ) {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            CoverPlayGlassSurface(
                modifier = Modifier.fillMaxWidth().height(120.dp),
                shape = RoundedCornerShape(CoverPlayTokens.Radius.sheet),
                overVideo = true,
                tint = Color.Black.copy(alpha = bgAlpha.coerceAtLeast(0.3f))
            ) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Sample subtitle text",
                        color = Color(textColor),
                        fontSize = fontSize.sp,
                        fontWeight = if (boldEnabled) FontWeight.ExtraBold else FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Font Size", style = MaterialTheme.typography.labelLarge, color = LocalAppUiSettings.current.palette.textSecondary)
                        Text("${fontSize.toInt()} sp", style = MaterialTheme.typography.labelMedium, color = CoverPlayTokens.Palette.textPrimary)
                    }
                    CoverPlaySlider(
                        value = fontSize,
                        onValueChange = { fontSize = it },
                        valueRange = 12f..32f
                    )
                }

                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Background Opacity", style = MaterialTheme.typography.labelLarge, color = LocalAppUiSettings.current.palette.textSecondary)
                        Text("${(bgAlpha * 100).toInt()}%", style = MaterialTheme.typography.labelMedium, color = CoverPlayTokens.Palette.textPrimary)
                    }
                    CoverPlaySlider(
                        value = bgAlpha,
                        onValueChange = { bgAlpha = it },
                        valueRange = 0f..1f
                    )
                }

                Column {
                    Text("Text Color", style = MaterialTheme.typography.labelLarge, color = LocalAppUiSettings.current.palette.textSecondary)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        colorOptions.forEach { (colorInt, _) ->
                            val isSelected = textColor == colorInt
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(colorInt))
                                    .border(
                                        if (isSelected) 3.dp else 1.dp,
                                        if (isSelected) CoverPlayTokens.Palette.textPrimary else Color.Transparent,
                                        CircleShape
                                    )
                                    .aliveClickable { textColor = colorInt },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(painterResource(R.drawable.ic_check_custom), null, tint = Color(colorInt).onFill(), modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Bold Style", style = MaterialTheme.typography.labelLarge, color = LocalAppUiSettings.current.palette.textSecondary)

                    CoverPlaySwitch(checked = boldEnabled, onCheckedChange = { boldEnabled = it }, compact = true)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            CoverPlayButton(
                text = "Save Settings",
                onClick = { savePrefs(); onDismiss() },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@UnstableApi
@Composable
fun CoverPlayOnlineSubtitleSearchDialog(
    videoTitle: String,
    onDismiss: () -> Unit,
    onSubtitleSelected: (String) -> Unit
) {
    val configuration = LocalConfiguration.current
    val accent = LocalAppUiSettings.current.accent
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    var searchQuery by remember { mutableStateOf(videoTitle.substringBeforeLast(".")) }
    var isSearching by remember { mutableStateOf(false) }
    var searchError by remember { mutableStateOf<String?>(null) }
    var results by remember { mutableStateOf<List<SubtitleResult>>(emptyList()) }
    var hasSearched by remember { mutableStateOf(false) }
    var resolvingFileId by remember { mutableStateOf<Int?>(null) }
    var downloadError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun doSearch() {
        if (searchQuery.isBlank()) return
        isSearching = true
        searchError = null
        downloadError = null
        hasSearched = true
        scope.launch(Dispatchers.IO) {
            try {
                val encoded = java.net.URLEncoder.encode(searchQuery.trim(), "UTF-8")
                val url = java.net.URL("https://api.opensubtitles.com/api/v1/subtitles?query=$encoded&languages=en")
                val conn = url.openConnection() as java.net.HttpURLConnection
                conn.requestMethod = "GET"
                conn.setRequestProperty("Api-Key", OPENSUBTITLES_API_KEY)
                conn.setRequestProperty("User-Agent", OPENSUBTITLES_USER_AGENT)
                conn.setRequestProperty("Accept", "application/json")
                conn.connectTimeout = 8000
                conn.readTimeout = 8000

                val code = conn.responseCode
                if (code !in 200..299) {
                    val err = conn.errorStream?.bufferedReader()?.readText()
                    throw Exception(
                        when (code) {
                            401, 403 -> "Invalid or missing OpenSubtitles API key"
                            429 -> "Rate limited by OpenSubtitles — try again shortly"
                            else -> "Search failed (HTTP $code)${if (!err.isNullOrBlank()) ": $err" else ""}"
                        }
                    )
                }

                val response = conn.inputStream.bufferedReader().readText()
                val json = JSONObject(response)
                val data = json.optJSONArray("data") ?: JSONArray()
                val parsed = mutableListOf<SubtitleResult>()
                for (i in 0 until minOf(data.length(), 30)) {
                    val attrs = data.getJSONObject(i).optJSONObject("attributes") ?: continue
                    val files = attrs.optJSONArray("files")
                    val firstFile = files?.optJSONObject(0)
                    val fileId = firstFile?.optInt("file_id", -1) ?: -1
                    if (fileId <= 0) continue

                    val lang = attrs.optString("language", "en")
                    val releaseName = attrs.optString(
                        "release",
                        firstFile?.optString("file_name") ?: "Unknown"
                    )
                    val rating = attrs.optDouble("ratings", 0.0)
                    val downloads = attrs.optInt("download_count", 0)
                    parsed.add(SubtitleResult(lang, releaseName, fileId, rating, downloads))
                }

                val sorted = parsed.sortedByDescending { it.downloads }

                withContext(Dispatchers.Main) {
                    results = sorted
                    isSearching = false
                    if (sorted.isEmpty()) searchError = "No subtitles found for \"${searchQuery.trim()}\""
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    searchError = "Search failed: ${e.message ?: "Network error"}"
                    isSearching = false
                }
            }
        }
    }

    suspend fun resolveDownloadLink(fileId: Int): String = withContext(Dispatchers.IO) {
        val url = java.net.URL("https://api.opensubtitles.com/api/v1/download")
        val conn = url.openConnection() as java.net.HttpURLConnection
        conn.requestMethod = "POST"
        conn.doOutput = true
        conn.setRequestProperty("Api-Key", OPENSUBTITLES_API_KEY)
        conn.setRequestProperty("Content-Type", "application/json")
        conn.setRequestProperty("User-Agent", OPENSUBTITLES_USER_AGENT)
        conn.setRequestProperty("Accept", "application/json")
        conn.connectTimeout = 8000
        conn.readTimeout = 8000
        val body = JSONObject().apply { put("file_id", fileId) }.toString()
        conn.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }

        val code = conn.responseCode
        if (code !in 200..299) {
            val err = (if (code >= 400) conn.errorStream else conn.inputStream)
                ?.bufferedReader()?.readText()
            throw Exception(
                when (code) {
                    406 -> "Daily OpenSubtitles download limit reached"
                    401, 403 -> "Invalid or missing OpenSubtitles API key"
                    else -> "Download failed (HTTP $code)${if (!err.isNullOrBlank()) ": $err" else ""}"
                }
            )
        }
        val resp = JSONObject(conn.inputStream.bufferedReader().readText())
        resp.getString("link")
    }

    fun selectSubtitle(sub: SubtitleResult) {
        if (resolvingFileId != null) return
        resolvingFileId = sub.fileId
        downloadError = null
        scope.launch {
            try {
                val link = resolveDownloadLink(sub.fileId)
                resolvingFileId = null
                onSubtitleSelected(link)
                onDismiss()
            } catch (e: Exception) {
                resolvingFileId = null
                downloadError = e.message ?: "Couldn't fetch that subtitle"
            }
        }
    }

    CoverPlayDialog(
        onDismiss = onDismiss,
        title = "Cloud Subtitles",
        modifier = Modifier.fillMaxWidth(if (isLandscape) 0.6f else 0.95f)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Movie or Show Title", color = LocalAppUiSettings.current.palette.textTertiary) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LocalAppUiSettings.current.accent,
                        unfocusedBorderColor = LocalAppUiSettings.current.palette.outlineMedium,
                        focusedContainerColor = glassFieldFill(),
                        unfocusedContainerColor = glassFieldFill(),
                        cursorColor = LocalAppUiSettings.current.accent,
                        focusedTextColor = LocalAppUiSettings.current.palette.textPrimary,
                        unfocusedTextColor = LocalAppUiSettings.current.palette.textPrimary
                    ),
                    shape = RoundedCornerShape(14.dp)
                )

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isSearching) LocalAppUiSettings.current.palette.surfaceRaisedAlt else LocalAppUiSettings.current.palette.containerHigh)
                        .clickable(enabled = !isSearching) { doSearch() },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSearching) {
                        CoverPlayLoader(size = 26.dp, color = LocalAppUiSettings.current.accent)
                    } else {
                        Icon(painterResource(R.drawable.ic_search_custom), null, tint = LocalAppUiSettings.current.palette.textPrimary, modifier = Modifier.size(22.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(if (isLandscape) 12.dp else 20.dp))
            HorizontalDivider(color = LocalAppUiSettings.current.palette.outlineFaint)

            Box(modifier = Modifier.heightIn(min = 100.dp, max = if (isLandscape) 160.dp else 360.dp)) {
                when {
                    searchError != null -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(searchError!!, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                        }
                    }
                    results.isEmpty() && !isSearching -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                if (hasSearched) "No results found" else "Enter a title to search",
                                color = LocalAppUiSettings.current.palette.textTertiary
                            )
                        }
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            items(results) { sub ->
                                val isResolvingThis = resolvingFileId == sub.fileId
                                CoverPlayCardSurface(
                                    onClick = { selectSubtitle(sub) },
                                    modifier = Modifier.fillMaxWidth(),
                                    cornerRadius = 20.dp
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(46.dp)
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(LocalAppUiSettings.current.palette.containerHigh),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                painterResource(R.drawable.ic_subtitles_custom),
                                                null,
                                                tint = accent,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(16.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                sub.releaseName,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = LocalAppUiSettings.current.palette.textPrimary,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                Surface(
                                                    color = BrandCyan.copy(alpha = 0.15f),
                                                    shape = RoundedCornerShape(6.dp),
                                                    border = BorderStroke(0.5.dp, BrandCyan.copy(alpha = 0.4f))
                                                ) {
                                                    Text(
                                                        sub.language.uppercase(),
                                                        color = BrandCyan,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                }
                                                Text("★ ${sub.rating}", color = LocalAppUiSettings.current.palette.textSecondary, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                                                Text("${sub.downloads} DLs", color = LocalAppUiSettings.current.palette.textTertiary, style = MaterialTheme.typography.labelSmall)
                                            }
                                        }

                                        if (isResolvingThis) {
                                            CoverPlayLoader(size = 30.dp, color = accent)
                                        } else {
                                            Icon(painterResource(R.drawable.ic_download_custom), null, tint = LocalAppUiSettings.current.palette.textTertiary.copy(alpha = 0.6f), modifier = Modifier.size(20.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (downloadError != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    downloadError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            CoverPlayButton(
                text = "Close",
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

data class SubtitleResult(
    val language: String,
    val releaseName: String,
    val fileId: Int,
    val rating: Double,
    val downloads: Int
)

fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs / 1000
    val seconds = totalSeconds % 60
    val minutes = (totalSeconds / 60) % 60
    val hours = totalSeconds / 3600
    return if (hours > 0) String.format("%d:%02d:%02d", hours, minutes, seconds)
    else String.format("%d:%02d", minutes, seconds)
}

fun formatSize(size: Long): String {
    if (size <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (log10(size.toDouble()) / log10(1024.0)).toInt()
    return String.format(
        "%.1f %s",
        size / 1024.0.pow(digitGroups.toDouble()),
        units[digitGroups]
    )
}

@UnstableApi
@Composable
fun CoverPlayStatsHud(
    video: VideoFile,
    videoSize: VideoSize?,
    exoPlayer: ExoPlayer,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isLandscape =
        configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    var liveVideoFormat by remember { mutableStateOf(exoPlayer.videoFormat) }

    var retrieverBitrateKbps by remember { mutableIntStateOf(0) }

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onVideoSizeChanged(size: VideoSize) {
                liveVideoFormat = exoPlayer.videoFormat
            }

            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY) {
                    liveVideoFormat = exoPlayer.videoFormat
                }
            }

            override fun onTracksChanged(tracks: Tracks) {
                liveVideoFormat = exoPlayer.videoFormat
            }
        }
        exoPlayer.addListener(listener)
        onDispose { exoPlayer.removeListener(listener) }
    }

    LaunchedEffect(video.uri) {
        withContext(Dispatchers.IO) {
            try {
                val retriever = android.media.MediaMetadataRetriever()
                retriever.setDataSource(context, video.uri)
                val bitrateStr = retriever.extractMetadata(
                    android.media.MediaMetadataRetriever.METADATA_KEY_BITRATE
                )
                retriever.release()
                val totalKbps = bitrateStr?.toIntOrNull()?.div(1000) ?: 0
                if (totalKbps > 0) retrieverBitrateKbps = totalKbps
            } catch (_: Exception) {
            }
        }
    }

    val fmt = liveVideoFormat
    val fmtBitrateKbps = if (fmt != null && fmt.bitrate > 0) fmt.bitrate / 1000 else 0
    val bitrateKbps = when {
        fmtBitrateKbps > 0 -> fmtBitrateKbps
        retrieverBitrateKbps > 0 -> retrieverBitrateKbps
        video.duration > 0L -> ((video.size * 8L) / video.duration).toInt()
        else -> 0
    }

    CoverPlayPlayerPanel(visible = true, onDismiss = onDismiss, title = "Media info", subtitle = formatFileName(video.name)) {
        CoverPlayPlayerPanelSection("Video") {
            CoverPlayPlayerPanelCard {
                CoverPlayHUDInfoItem("Resolution", videoSize?.let { "${it.width} × ${it.height}" } ?: "Detecting…")
                CoverPlayPlayerPanelDivider()
                CoverPlayHUDInfoItem("Codec", fmt?.sampleMimeType?.substringAfterLast("/")?.uppercase() ?: "Detecting…")
                if (fmt != null && fmt.frameRate > 0f) {
                    CoverPlayPlayerPanelDivider()
                    CoverPlayHUDInfoItem(
                        "Frame rate",
                        if (fmt.frameRate % 1f == 0f) "${fmt.frameRate.toInt()} fps" else String.format("%.3f fps", fmt.frameRate)
                    )
                }
                CoverPlayPlayerPanelDivider()
                CoverPlayHUDInfoItem(
                    "Bitrate",
                    when {
                        bitrateKbps >= 1000 -> String.format("%.2f Mbps", bitrateKbps / 1000f)
                        bitrateKbps > 0 -> "$bitrateKbps kbps"
                        else -> "Calculating…"
                    }
                )
            }
        }
        CoverPlayPlayerPanelSection("File") {
            CoverPlayPlayerPanelCard {
                CoverPlayHUDInfoItem("Format", video.path.substringAfterLast(".").uppercase())
                CoverPlayPlayerPanelDivider()
                CoverPlayHUDInfoItem("Size", formatSize(video.size))
                CoverPlayPlayerPanelDivider()
                CoverPlayHUDInfoItem("Duration", formatDuration(video.duration))
            }
        }
        val badges = buildList {
            if (videoSize != null && videoSize.width >= 3840) add("4K Ultra HD")
            else if (videoSize != null && videoSize.width >= 1920) add("Full HD")
            if (video.duration > 3600000) add("Feature length")
        }
        if (badges.isNotEmpty()) {
            Row(Modifier.padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                badges.forEach { CoverPlayHUDChip(it) }
            }
        }
    }
}

@Composable
private fun CoverPlayHUDInfoItem(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = 48.dp).padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = CoverPlayTokens.Palette.textSecondary,
            modifier = Modifier.weight(1f)
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, fontFeatureSettings = "tnum"),
            color = CoverPlayTokens.Palette.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun CoverPlayHUDChip(text: String) {
    val accent = LocalAppUiSettings.current.accent
    Box(
        Modifier
            .clip(CircleShape)
            .background(accent.copy(alpha = 0.16f))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(text, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold), color = accent)
    }
}

@Composable
fun CoverPlayLocalSubtitleDialog(
    video: VideoFile,
    onDismiss: () -> Unit,
    onPick: (Uri) -> Unit,
    onBrowse: () -> Unit
) {
    val exts = setOf("srt", "vtt", "ass", "ssa", "ttml", "dfxp")
    val base = File(video.path).nameWithoutExtension
    val files = remember(video.path) {
        runCatching {
            File(video.path).parentFile?.listFiles()
                ?.filter { it.isFile && it.extension.lowercase() in exts }
                ?.sortedWith(compareByDescending<File> { it.nameWithoutExtension.startsWith(base) }.thenBy { it.name.lowercase() })
                .orEmpty()
        }.getOrDefault(emptyList())
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        CoverPlayDialogBackdrop(blurRadius = 36, dim = 0.45f)
        CoverPlayCinematicPanel(
            modifier = Modifier
                .widthIn(max = 360.dp)
                .fillMaxWidth(0.85f)
                .padding(16.dp),
            shape = RoundedCornerShape(CoverPlayTokens.Radius.sheet)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Local Subtitles",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = CoverPlayTokens.Palette.textPrimary
                    )
                    CoverPlayDialogCloseButton(onClick = onDismiss)
                }

                Column(
                    modifier = Modifier
                        .heightIn(max = 220.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (files.isEmpty()) {
                        Text(
                            "No subtitle files found near this video.",
                            style = MaterialTheme.typography.bodySmall,
                            color = CoverPlayTokens.Palette.textSecondary,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        files.forEach { f ->
                            CoverPlayPlayerPanelOptionRow(
                                title = f.name,
                                subtitle = "${f.extension.uppercase()} file",
                                selected = false,
                                onClick = { onPick(Uri.fromFile(f)) }
                            )
                        }
                    }
                }

                CoverPlayButton(
                    text = "Browse file manager",
                    onClick = onBrowse,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
