package com.example.myapplication

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.PictureInPictureParams
import android.content.ContentUris
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Rational
import android.view.GestureDetector
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.LightMode
import androidx.media3.common.C
import androidx.media3.common.MediaItem
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
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.extractor.DefaultExtractorsFactory
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.VideoFrameDecoder
import coil.request.ImageRequest
import coil.request.videoFrameMillis
import com.example.myapplication.ui.theme.*
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import io.github.anilbeesetti.nextlib.media3ext.ffdecoder.NextRenderersFactory
import okhttp3.OkHttpClient
import java.io.File
import java.util.concurrent.TimeUnit
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import androidx.compose.runtime.produceState
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.*

private const val RECENTS_PREFS = "recent_videos"
private const val RECENTS_KEY = "items"

/**
 * Subtitle files come in more than one format (SRT, WebVTT, ASS/SSA, TTML).
 * Previously every subtitle load was hardcoded to APPLICATION_SUBRIP, which
 * makes non-SRT files fail to parse or render garbled. This inspects the
 * file name (from a content:// display name, a local path, or a URL) and
 * picks the matching mime type, falling back to SRT when unknown.
 */
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

// OpenSubtitles REST API v1 (api.opensubtitles.com). The legacy
// rest.opensubtitles.org endpoint was deprecated/shut down, which is why
// online subtitle search stopped working — this replaces it with the
// current API. Get/rotate your key at https://www.opensubtitles.com/en/consumers
private const val OPENSUBTITLES_API_KEY = "f9wGIFAKsW0aegiIE7L1I1WJuQYsCA5Y"
private const val OPENSUBTITLES_USER_AGENT = "CoverPlay v1.0"

// --- Data Models ---

fun formatFileName(name: String): String {
    // 1. Remove extension
    val nameWithoutExt = name.substringBeforeLast(".")

    // 2. Common release tags regex
    val tags = listOf(
        "1080p", "720p", "480p", "2160p", "4k", "uhd", "bluray", "brrip", "webrip", "h264", "x264", "h265", "x265",
        "aac", "dts", "dd5.1", "dual.audio", "hindi", "english", "esub", "subbed", "camrip", "hdtv", "web-dl",
        "web", "rip", "hevc", "xvid", "divx", "dvdrip", "scr", "cam", "ts", "telesync", "pdvd", "workprint"
    )

    var cleaned = nameWithoutExt.replace(".", " ").replace("_", " ").replace("-", " ")

    // 3. Match Year (e.g., 2023)
    val yearRegex = Regex("\\b(19|20)\\d{2}\\b")
    val match = yearRegex.find(cleaned)
    val year = match?.value

    if (match != null) {
        // Strip everything after the year
        cleaned = cleaned.substring(0, match.range.first).trim()
    }

    // 4. Strip common tags case-insensitively
    tags.forEach { tag ->
        cleaned = cleaned.replace(Regex("(?i)\\b$tag\\b"), "")
    }

    // 5. Clean up extra spaces
    cleaned = cleaned.replace(Regex("\\s+"), " ").trim()

    // 6. Capitalize first letter of each word
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
    HOME, FOLDERS, PLAYLISTS, SETTINGS, FOLDER_VIDEOS, PLAYER
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
fun BrandTriangle(modifier: Modifier = Modifier, contentDescription: String? = "CoverPlay") {
    BrandMark(modifier = modifier)
}

@Composable
fun TriangleThumbPlaceholder(modifier: Modifier = Modifier, triangleFraction: Float = 0.32f) {
    Box(
        modifier = modifier.background(LocalAppUiSettings.current.palette.surfaceRaisedAlt),
        contentAlignment = Alignment.Center
    ) {
        BrandTriangle(
            modifier = Modifier.fillMaxSize(triangleFraction),
        )
    }
}


// ── Picture-in-Picture state ────────────────────────────────────────────────
// Small cross-composable holder so VideoPlayerScreen can (a) tell the Activity
// it's currently showing a video and eligible for PiP, (b) report the video's
// aspect ratio for the PiP window, and (c) react when the system actually
// puts the Activity into/out of PiP mode (to hide/show our custom chrome).
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
            MyApplicationTheme {
                MainApp(intentVideoUri) { intentVideoUri = null }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    override fun onStop() {
        super.onStop()
        // Not during rotation / config changes: swapping launcher aliases then can restart the app.
        if (!isChangingConfigurations) AppIconManager.applyPending(this)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_VIEW) {
            intentVideoUri = intent.data
        }
    }

    /** Enters PiP now (used by the in-player PiP button and the home-button hint below). */
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

    // Pressing Home (or switching apps) while a video is playing should slide
    // into PiP instead of just stopping, matching every other video app.
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
fun MainApp(externalVideoUri: Uri? = null, onConsumed: () -> Unit = {}) {
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
            SplashScreen(onAnimationFinish = { showSplash = false })
        } else {
            MainContent(externalVideoUri, onConsumed)
        }
    }
}

@Composable
fun SplashScreen(onAnimationFinish: () -> Unit) {
    val settings = LocalAppUiSettings.current
    val accent = settings.accent
    val highlight = settings.highlight
    var startAnimation by remember { mutableStateOf(false) }

    // ── Ultra-Minimalist High-End Entrance ──────────────────────────────────
    val logoScale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.22f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 180f),
        label = "LogoScale"
    )
    val logoAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label = "LogoAlpha"
    )
    val titleAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(800, delayMillis = 350, easing = FastOutSlowInEasing),
        label = "TitleAlpha"
    )
    val titleOffset by animateFloatAsState(
        targetValue = if (startAnimation) 0f else 24f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 220f),
        label = "TitleOffset"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "shimmerAlpha"
    )

    val barAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(600, delayMillis = 800),
        label = "BarAlpha"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(2300)
        onAnimationFinish()
    }

    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        // Soft ambient background
        AnimatedPetalBackground()

        CinematicBackground(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .size(720.dp)
                    .align(Alignment.Center)
                    .graphicsLayer {
                        alpha = 0.20f * logoAlpha * shimmerAlpha
                        scaleX = 0.7f + (0.3f * logoScale)
                        scaleY = 0.7f + (0.3f * logoScale)
                    }
                    .background(
                        Brush.radialGradient(listOf(accent, highlight.copy(alpha = 0.35f), Color.Transparent)),
                        CircleShape
                    )
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp)
        ) {
            // Hero Brand Element
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.graphicsLayer {
                    alpha = logoAlpha
                    scaleX = logoScale
                    scaleY = logoScale
                }
            ) {
                BrandPlayOverlay(
                    size = 150.dp,
                    modifier = Modifier.shadow(
                        elevation = 48.dp * logoScale,
                        shape = CircleShape,
                        spotColor = accent.copy(alpha = GlowSpec.SPOT)
                    )
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Clean, uncluttered title & single tagline
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.graphicsLayer {
                    alpha = titleAlpha
                    translationY = titleOffset
                }
            ) {
                Text(
                    text = "CoverPlay",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontSize = 42.sp,
                        letterSpacing = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        brush = Brush.linearGradient(
                            listOf(Color.White, Color.White.copy(alpha = 0.92f), accent)
                        ),
                        shadow = androidx.compose.ui.graphics.Shadow(
                            color = accent.copy(alpha = 0.5f),
                            blurRadius = 24f
                        )
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "CINEMATIC MEDIA ENGINE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 4.sp,
                        color = accent.copy(alpha = 0.85f * shimmerAlpha)
                    ),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(72.dp))

            // Minimalist hairline progress
            Box(
                modifier = Modifier
                    .graphicsLayer(alpha = barAlpha)
                    .width(140.dp)
                    .height(2.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.08f))
            ) {
                val progress by infiniteTransition.animateFloat(
                    initialValue = 0f, targetValue = 1f,
                    animationSpec = infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing)),
                    label = "progress"
                )

                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(0.4f)
                        .offset(x = 140.dp * (progress * 1.6f - 0.6f))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color.Transparent, accent, Color.White, accent, Color.Transparent)
                            )
                        )
                )
            }
        }
    }
}

@UnstableApi
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@Composable
fun MainContent(externalVideoUri: Uri? = null, onConsumed: () -> Unit = {}) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("coverplay_settings_v3", android.content.Context.MODE_PRIVATE) }
    var settings by remember { mutableStateOf(readSettings(context)) }

    // Reset GlassWarmup on startup so live blur only kicks in after UI settles
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
            .build()
    }

    CompositionLocalProvider(coil.compose.LocalImageLoader provides imageLoader) {
        var currentScreen by remember { mutableStateOf(Screen.HOME) }
        var previousScreen by remember { mutableStateOf<Screen?>(null) }
        var selectedFolder by remember { mutableStateOf<VideoFolder?>(null) }
        var selectedVideo by remember { mutableStateOf<VideoFile?>(null) }

        val folders = remember { mutableStateListOf<VideoFolder>() }
        val folderVideos = remember { mutableStateListOf<VideoFile>() }
        val recentEntries = remember { mutableStateListOf<RecentEntry>() }
        val allVideos = remember { mutableStateListOf<VideoFile>() }

        // Global Search State
        var isSearchActive by remember { mutableStateOf(false) }
        var searchQuery by remember { mutableStateOf("") }

        val scope = rememberCoroutineScope()

        // Usage tracking: count time while the app is in the foreground (flushed every 5s).
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

        // Live-apply the History Retention setting to the list already on screen.
        val keepDaysSetting = settings["recents_keep_days"] as? Int ?: 7
        LaunchedEffect(keepDaysSetting) {
            val cutoff = System.currentTimeMillis() - keepDaysSetting * 24L * 60L * 60L * 1000L
            if (recentEntries.removeAll { it.playedAt < cutoff }) saveRecentEntries(context, recentEntries)
        }

        // First-run app tour (waits for the storage-permission prompt to be answered). Replayable from Settings.
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
            AppTutorial(onFinish = { TutorialPrefs.markApp(context); showAppTour = false })
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
            // Was: nested scope.launch(Dispatchers.IO) inside this LaunchedEffect,
            // an extra coroutine hop that also outlived normal cancellation.
            // withContext does the same work on one coroutine, tied to this
            // composable's lifecycle.
            withContext(Dispatchers.IO) {
                // 1. Load recents off the main thread
                val recents = loadRecentEntries(context)
                withContext(Dispatchers.Main) {
                    recentEntries.clear()
                    recentEntries.addAll(recents)
                }

                // Delay the heavy media scan slightly to let the Splash -> Home transition finish smoothly
                delay(400)

                // 2. Fetch media files safely
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

        val backdrop = rememberBackdropState()
        val hazeState = remember { HazeState() }
        var navCollapsed by remember { mutableStateOf(false) }
        val navScroll = remember {
            object : NestedScrollConnection {
                override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                    if (available.y < -6f) navCollapsed = true       // scrolling down -> shrink
                    else if (available.y > 6f) navCollapsed = false  // scrolling up -> grow
                    return Offset.Zero
                }
            }
        }
        LaunchedEffect(currentScreen) { navCollapsed = false }

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
            CinematicBackground(
                modifier = if (dlgBlur > 0.dp) Modifier.blur(dlgBlur) else Modifier,
                ambientArtUri = ambientVideoUri,
                backdrop = backdrop,
                hazeState = hazeState,
                overlay = {
                    if (currentScreen != Screen.PLAYER) {
                        Box(
                            Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(160.dp)
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            Color.Transparent,
                                            LocalAppUiSettings.current.palette.bg.copy(alpha = 0.25f),
                                            LocalAppUiSettings.current.palette.bg.copy(alpha = 0.75f),
                                            LocalAppUiSettings.current.palette.bg.copy(alpha = 0.95f)
                                        )
                                    )
                                )
                        )
                    }
                    AnimatedVisibility(
                        visible = currentScreen != Screen.PLAYER,
                        enter = slideInVertically(animationSpec = tween(200)) { it } + fadeIn(),
                        exit = slideOutVertically(animationSpec = tween(200)) { it } + fadeOut(),
                        modifier = Modifier.align(Alignment.BottomCenter)
                    ) {
                        BottomNavigationBar(
                            currentScreen = currentScreen,
                            collapsed = navCollapsed,
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
                    Box(modifier = Modifier.padding(top = topPadding).nestedScroll(navScroll)) {
                        AnimatedContent(
                            targetState = currentScreen,
                            transitionSpec = {
                                if (targetState == Screen.PLAYER || initialState == Screen.PLAYER) {
                                    // Simple, fast, modern open/close for the player: one quick fade
                                    // + scale curve, same on the way in and out, 150ms. Cheap to
                                    // compose (single tween, no direction-dependent branching) and
                                    // short enough to feel instant.
                                    val fadeSpec = tween<Float>(durationMillis = 150, easing = FastOutSlowInEasing)
                                    val scaleSpec = tween<Float>(durationMillis = 150, easing = FastOutSlowInEasing)

                                    (fadeIn(animationSpec = fadeSpec) +
                                            scaleIn(animationSpec = scaleSpec, initialScale = 0.97f)) togetherWith
                                            (fadeOut(animationSpec = fadeSpec) +
                                                    scaleOut(animationSpec = scaleSpec, targetScale = 0.97f))
                                } else {
                                    // Lightweight, fast, smooth Tab transitions
                                    val tabOrder = listOf(Screen.HOME, Screen.FOLDERS, Screen.PLAYLISTS, Screen.SETTINGS)
                                    fun tabIndex(s: Screen) = tabOrder.indexOf(s).let { if (it == -1) 0 else it }
                                    val goingForward = tabIndex(targetState) >= tabIndex(initialState)

                                    val emph = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)
                                    val fadeSpec = tween<Float>(durationMillis = 300, delayMillis = 50, easing = emph)
                                    val slideSpec = tween<IntOffset>(durationMillis = 380, easing = emph)
                                    val scaleSpec = tween<Float>(durationMillis = 380, easing = emph)
                                    val outFade = tween<Float>(durationMillis = 160)

                                    if (goingForward) {
                                        (slideInHorizontally(animationSpec = slideSpec) { it / 5 } + fadeIn(animationSpec = fadeSpec) + scaleIn(scaleSpec, initialScale = 0.96f)) togetherWith
                                                (slideOutHorizontally(animationSpec = slideSpec) { -it / 8 } + fadeOut(animationSpec = outFade) + scaleOut(scaleSpec, targetScale = 0.98f))
                                    } else {
                                        (slideInHorizontally(animationSpec = slideSpec) { -it / 5 } + fadeIn(animationSpec = fadeSpec) + scaleIn(scaleSpec, initialScale = 0.96f)) togetherWith
                                                (slideOutHorizontally(animationSpec = slideSpec) { it / 8 } + fadeOut(animationSpec = outFade) + scaleOut(scaleSpec, targetScale = 0.98f))
                                    }
                                }
                            },
                            label = "ScreenTransition"
                        ) { screen ->
                            when (screen) {
                                Screen.HOME -> {
                                    DashboardScreen(
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
                                    FolderListScreen(
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
                                        currentScreen = Screen.FOLDERS
                                    }
                                    VideoListScreen(
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
                                            currentScreen = Screen.FOLDERS
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
                                        VideoPlayerScreen(
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

                                Screen.PLAYLISTS -> {
                                    BackHandler { currentScreen = Screen.HOME }
                                    PlaylistsScreen(
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
                                    SettingsScreen(
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
                .put("lastPlayedAt", entry.playedAt)  // preserve the actual play time
        )
    }
    context.getSharedPreferences(RECENTS_PREFS, Context.MODE_PRIVATE)
        .edit()
        .putString(RECENTS_KEY, items.toString())
        .apply()
}

// --- Video Player Screen ---

@UnstableApi
@Composable
fun SmoothPlayerScrubber(
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

    LaunchedEffect(exoPlayer, isDragging) {
        if (!isDragging) {
            while (true) {
                internalPosition = exoPlayer.currentPosition
                onPositionChanged(internalPosition)
                delay(200L) // UI refresh rate for time codes — smoother than 500ms
            }
        }
    }

    PremiumSeekbar(
        position = internalPosition,
        duration = duration,
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
            // Removed exoPlayer.seekTo(ms) to keep main video playing normally
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
private fun ControlTile(
    icon: Painter,
    label: String,
    value: String? = null,
    active: Boolean = false,
    onClick: () -> Unit
) {
    val accent = LocalAppUiSettings.current.accent
    GlassSurface(
        modifier = Modifier.fillMaxWidth().height(84.dp),
        shape = RoundedCornerShape(20.dp),
        overVideo = true,
        selected = active,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, null, tint = Color.White.copy(alpha = if (active) 1f else 0.85f), modifier = Modifier.size(22.dp))
            Spacer(Modifier.height(6.dp))
            Text(label, fontSize = 12.sp, lineHeight = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White, maxLines = 2, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 6.dp))
            if (value != null) {
                Text(value, fontSize = 11.sp, maxLines = 1, color = if (active) accent else Color.White.copy(alpha = 0.55f))
            }
        }
    }
}

@Composable
private fun VideoControlsSheet(
    isLandscape: Boolean,
    onDismiss: () -> Unit,
    content: LazyGridScope.() -> Unit
) {
    val accent = LocalAppUiSettings.current.accent
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        GlassDialogBackdrop(blurRadius = 48, dim = 0.4f)
        var shown by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { shown = true }
        val progress by animateFloatAsState(
            targetValue = if (shown) 1f else 0f,
            animationSpec = spring(dampingRatio = 0.78f, stiffness = 380f),
            label = "controlsSheetIn"
        )
        CinematicPanel(
            modifier = (if (isLandscape) Modifier.fillMaxWidth(0.62f).fillMaxHeight(0.9f)
            else Modifier.fillMaxWidth(0.88f).widthIn(max = 360.dp).heightIn(max = 520.dp))
                .graphicsLayer {
                    alpha = progress.coerceIn(0f, 1f)
                    val sc = 0.92f + 0.08f * progress
                    scaleX = sc; scaleY = sc
                    translationY = (1f - progress) * 48f
                },
            shape = RoundedCornerShape(26.dp)
        ) {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Video controls",
                            style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = Color.White)
                        Spacer(Modifier.height(5.dp))
                        Box(
                            Modifier.width(24.dp).height(2.dp).clip(CircleShape)
                                .background(Brush.horizontalGradient(listOf(accent.copy(alpha = 0.85f), accent, accent)))
                        )
                        Spacer(Modifier.height(4.dp))
                        QuipLine(Quips.dialog("more options"), color = Color.White.copy(alpha = 0.45f))
                    }
                    PremiumDialogCloseButton(onClick = onDismiss)
                }
                Spacer(Modifier.height(10.dp))
                LazyVerticalGrid(
                    columns = GridCells.Fixed(if (isLandscape) 4 else 2),
                    modifier = Modifier.weight(1f, fill = false),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    content = content
                )
            }
        }
    }
}

@Composable
private fun GlassPill(modifier: Modifier = Modifier, content: @Composable RowScope.() -> Unit) {
    GlassSurface(modifier = modifier, shape = CircleShape, overVideo = true) {
        Row(
            Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            content = content
        )
    }
}

/** 0→1→0 ease loop, only ticking while [enabled] so idle buttons cost nothing. */
@Composable
private fun rememberBreath(enabled: Boolean): State<Float> {
    if (!enabled) return remember { mutableFloatStateOf(0f) }
    val t = rememberInfiniteTransition(label = "breath")
    return t.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "breathV"
    )
}

@Composable
private fun GlassIconButton(
    painter: Painter,
    description: String,
    active: Boolean = false,
    activeColor: Color = LocalAppUiSettings.current.accent,
    onClick: () -> Unit
) {
    val settings = LocalAppUiSettings.current
    val interaction = remember { MutableInteractionSource() }
    val scale by rememberPressScale(interaction, 0.82f)
    val haptic = rememberHaptic()

    val sel by animateFloatAsState(
        if (active) 1f else 0f,
        spring(dampingRatio = 0.55f, stiffness = 420f), label = "btnSel"
    )
    val breath = rememberBreath(active || sel > 0.01f)
    val iconTint = lerp(Color.White, lerp(activeColor, settings.highlight, 0.45f), sel.coerceIn(0f, 1f))
    val glow = activeColor

    Box(
        modifier = Modifier
            .size(42.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .drawBehind {
                val a = sel.coerceIn(0f, 1f)
                if (a > 0.01f) {
                    val b = breath.value
                    val r = size.minDimension / 2f
                    val haloR = r * (1.35f + 0.15f * b)
                    // Borderless soft radial ambient glow behind selected icon
                    drawCircle(
                        Brush.radialGradient(
                            listOf(
                                glow.copy(alpha = (0.55f + 0.15f * b) * a),
                                glow.copy(alpha = (0.25f + 0.10f * b) * a),
                                Color.Transparent
                            ),
                            center = center, radius = haloR
                        ),
                        haloR, center
                    )
                }
            }
            .clip(CircleShape)
            .clickable(interactionSource = interaction, indication = null) { haptic(); onClick() },
        contentAlignment = Alignment.Center
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.graphicsLayer {
                val s = 1f + sel.coerceIn(0f, 1f) * (0.04f + 0.09f * breath.value)
                scaleX = s; scaleY = s
            }
        ) {
            // Subtle drop shadow when unselected
            if (sel < 0.99f) {
                Icon(
                    painter, null,
                    tint = Color.Black.copy(alpha = 0.65f * (1f - sel)),
                    modifier = Modifier.size(22.dp).offset(y = 1.dp).blur(3.dp)
                )
            }
            Icon(painter, description, tint = iconTint, modifier = Modifier.size(22.dp))
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
//  PLAYER CHROME PIECES
// ═════════════════════════════════════════════════════════════════════════════

/** Round liquid-glass button used by the top bar and transport row. */
@Composable
private fun GlassCircleButton(
    size: androidx.compose.ui.unit.Dp = 42.dp,
    active: Boolean = false,
    onClick: () -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    val breath = rememberBreath(active)
    val glow = LocalAppUiSettings.current.accent
    val interaction = remember { MutableInteractionSource() }
    val press by rememberPressScale(interaction, 0.84f)
    val haptic = rememberHaptic()
    val sel by animateFloatAsState(if (active) 1f else 0f, spring(0.6f, 420f), label = "ccSel")
    Box(
        modifier = Modifier
            .size(size)
            .graphicsLayer { scaleX = press; scaleY = press }
            .drawBehind {
                val r = this.size.minDimension / 2f
                // soft contact shadow so a bare icon stays legible on any frame
                val sr = r * 1.55f
                drawCircle(
                    Brush.radialGradient(
                        listOf(Color.Black.copy(alpha = 0.34f * (1f - 0.6f * sel)), Color.Black.copy(alpha = 0.12f), Color.Transparent),
                        center = center.copy(y = center.y + r * 0.12f), radius = sr
                    ),
                    sr, center.copy(y = center.y + r * 0.12f)
                )
                // selected: subtle themed glow, no border
                if (sel > 0.01f) {
                    val gr = r * (1.7f + 0.2f * breath.value)
                    drawCircle(
                        Brush.radialGradient(
                            listOf(glow.copy(alpha = (0.50f + 0.12f * breath.value) * sel), glow.copy(alpha = 0.16f * sel), Color.Transparent),
                            center = center, radius = gr
                        ),
                        gr, center
                    )
                }
            }
            .clip(CircleShape)
            .clickable(interactionSource = interaction, indication = null) { haptic(); onClick() },
        contentAlignment = Alignment.Center
    ) {
        Box(
            Modifier.fillMaxSize().graphicsLayer {
                val s = 1f + sel * (0.04f + 0.06f * breath.value)
                scaleX = s; scaleY = s
            },
            contentAlignment = Alignment.Center,
            content = content
        )
    }
}

/** 2×2 dot grid — the "more / options" glyph, drawn so it needs no extra drawable. */
@Composable
private fun MoreGlyph(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val r = size.minDimension * 0.115f
        val g = size.minDimension * 0.25f
        for (dx in listOf(-1f, 1f)) for (dy in listOf(-1f, 1f)) {
            drawCircle(tint, r, androidx.compose.ui.geometry.Offset(center.x + dx * g, center.y + dy * g))
        }
    }
}



@Composable
private fun MoreSpeedChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val settings = LocalAppUiSettings.current
    val accent = settings.accent
    val hi = settings.highlight
    val interaction = remember { MutableInteractionSource() }
    val press by rememberPressScale(interaction, 0.92f)
    val haptic = rememberHaptic()
    val sel by animateFloatAsState(if (selected) 1f else 0f, spring(0.65f, 450f), label = "chipSel")
    val a = sel.coerceIn(0f, 1f)
    val shape = RoundedCornerShape(50)
    Box(
        modifier = Modifier
            .graphicsLayer { scaleX = press * (1f + 0.04f * a); scaleY = press * (1f + 0.04f * a) }
            .drawBehind {
                if (a > 0.01f) drawCircle(
                    Brush.radialGradient(listOf(accent.copy(alpha = 0.38f * a), Color.Transparent), center = center, radius = size.maxDimension * 0.8f),
                    size.maxDimension * 0.8f, center
                )
            }
            .clip(shape)
            .background(Brush.horizontalGradient(listOf(lerp(Color.White.copy(0.08f), accent.copy(0.85f), a), lerp(Color.White.copy(0.06f), accent.copy(0.55f), a))))
            .border(1.dp, lerp(Color.White.copy(0.12f), hi.copy(0.70f), a), shape)
            .clickable(interactionSource = interaction, indication = null) { haptic(); onClick() }
            .padding(horizontal = 11.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            color = lerp(Color.White.copy(0.78f), Color.White, a),
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium, fontFeatureSettings = "tnum")
        )
    }
}




@Composable
private fun PillIcon(painter: Painter, description: String, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val scale by rememberPressScale(interaction, 0.8f)
    val haptic = rememberHaptic()
    Box(
        modifier = Modifier
            .size(36.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(CircleShape)
            .clickable(interactionSource = interaction, indication = null) { haptic(); onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(painter, description, tint = Color.White, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun TransportIcon(painter: Painter, description: String, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val scale by rememberPressScale(interaction, 0.78f)
    val haptic = rememberHaptic()
    Box(
        modifier = Modifier
            .size(44.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(CircleShape)
            .clickable(interactionSource = interaction, indication = null) { haptic(); onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(painter, description, tint = Color.White, modifier = Modifier.size(24.dp))
    }
}

@UnstableApi
@Composable
fun VideoPlayerScreen(
    video: VideoFile,
    onBack: () -> Unit,
    folderVideos: List<VideoFile> = emptyList(),
    onPlayVideo: (VideoFile) -> Unit = {},
    settings: Map<String, Any> = emptyMap(),
    browserTitle: String? = null,
    browserSubtitle: String? = null
) {
    // Everything opened from the player (dialogs, sheets, side panels) uses the dark cinematic glass.
    CompositionLocalProvider(LocalOverVideo provides true) {
        VideoPlayerScreenContent(video, onBack, folderVideos, onPlayVideo, settings, browserTitle, browserSubtitle)
    }
}

@UnstableApi
@Composable
private fun VideoPlayerScreenContent(
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
    val haptic = LocalHapticFeedback.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    // Parse Settings
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

    // Handle back gesture specifically for this screen
    BackHandler {
        onBack()
    }
    // Tracks whether we've already retried the current item with software decoding,
    // so a genuinely broken file doesn't retry-loop forever.
    var didRetrySoftwareDecode by remember { mutableStateOf(false) }

    val exoPlayer = remember {

        // EXTENSION_RENDERER_MODE_ON tries the platform (hardware) decoder first and
        // falls back to nextlib's FFmpeg decoders only if no hardware decoder exists.
        // EXTENSION_RENDERER_MODE_PREFER tries FFmpeg first. Either way,
        // setEnableDecoderFallback makes ExoPlayer keep trying other available
        // decoders (hardware -> software, or vice versa) instead of giving up the
        // moment the first one fails to initialize or decode a frame -- this is what
        // lets exotic/odd files actually play instead of erroring out immediately.
        val renderersFactory = NextRenderersFactory(context).apply {
            setExtensionRendererMode(
                if (hardwareDecode) DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON
                else DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER
            )
            setEnableDecoderFallback(true)
        }

        val trackSelector = DefaultTrackSelector(context)

        // Lenient extractor config: constant-bitrate seeking rescues seeking/duration
        // detection on the many "wild" MP3/AAC files that lack proper seek tables,
        // which otherwise show up as playback or seek failures.
        val extractorsFactory = DefaultExtractorsFactory()
            .setConstantBitrateSeekingEnabled(true)

        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .callTimeout(0, TimeUnit.SECONDS) // no overall cap; let connect/read timeouts govern
            .retryOnConnectionFailure(true)
            .build()
        val httpDataSourceFactory = OkHttpDataSource.Factory(okHttpClient)
            .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36")

        // Wrap the HTTP data source in a DefaultDataSource.Factory to support local files (content://, file://)
        val dataSourceFactory = DefaultDataSource.Factory(context, httpDataSourceFactory)

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

    // Reset the software-decode retry flag whenever the video changes, so each new
    // file gets its own fresh retry attempt.
    LaunchedEffect(video.uri) {
        didRetrySoftwareDecode = false
    }

    val subtitlePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let {
                try {
                    context.contentResolver.takePersistableUriPermission(
                        it,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (e: Exception) {}

                val subtitleConfig = MediaItem.SubtitleConfiguration.Builder(it)
                    .setMimeType(guessSubtitleMimeType(context, it))
                    .setLanguage("en")
                    .setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
                    .build()

                val currentMediaItem = exoPlayer.currentMediaItem
                if (currentMediaItem != null) {
                    val currentSubtitles = currentMediaItem.localConfiguration?.subtitleConfigurations.orEmpty()
                    val newMediaItem = currentMediaItem.buildUpon()
                        .setSubtitleConfigurations(currentSubtitles + subtitleConfig)
                        .build()
                    val currentPos = exoPlayer.currentPosition
                    exoPlayer.setMediaItem(newMediaItem, currentPos)
                    exoPlayer.prepare()
                    exoPlayer.play()
                }
            }
        }
    )

    // Tracks whichever video's position we're responsible for persisting.
    // Needed because switching to a new video inside the same screen
    // instance (via folderVideos / onPlayVideo) does NOT tear down this
    // composable -- only the final onDispose below used to fire, so every
    // video except the very last one watched in a session silently lost
    // its resume position.
    var activeVideoPath by remember { mutableStateOf<String?>(null) }

    // Update player when video or folder content changes
    LaunchedEffect(video.path, folderVideos) {
        // Persist the position of whatever was playing before switching to
        // this new video.
        activeVideoPath?.let { prevPath ->
            savePlaybackPosition(context, prevPath, exoPlayer.currentPosition, exoPlayer.duration)
        }
        activeVideoPath = video.path

        // Load the folder as a playlist so shuffle and repeat-all actually work.
        // We only reload the full list if it differs from what's currently in the player.
        val currentUris = List(exoPlayer.mediaItemCount) { exoPlayer.getMediaItemAt(it).localConfiguration?.uri }

        // Use the current video as a fallback if the folder list isn't ready yet
        val items = if (folderVideos.isEmpty()) listOf(video) else folderVideos
        val folderUris = items.map { it.uri }

        if (currentUris != folderUris) {
            val startIndex = items.indexOfFirst { it.path == video.path }.coerceAtLeast(0)
            val savedPos = if (resumePlayback) {
                context.getSharedPreferences("playback_positions", Context.MODE_PRIVATE)
                    .getLong(video.path, 0L)
            } else 0L

            exoPlayer.setMediaItems(items.map { MediaItem.fromUri(it.uri) }, startIndex, savedPos)
            exoPlayer.prepare()
        } else {
            // Playlist is already correct, just ensure we're on the right item (e.g. if picked from browser)
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

    // First-time player tour (replayable from Settings). Playback is held while it is open.
    var showPlayerTour by remember { mutableStateOf(!TutorialPrefs.playerSeen(context)) }
    if (showPlayerTour) {
        LaunchedEffect(Unit) { exoPlayer.pause() }
        PlayerTutorial(onFinish = {
            TutorialPrefs.markPlayer(context)
            showPlayerTour = false
            exoPlayer.play()
        })
    }

    var showSubtitleCustomization by remember { mutableStateOf(false) }
    var showOnlineSubtitleSearch by remember { mutableStateOf(false) }
    var showFolderVideos by remember { mutableStateOf(false) }
    var showHUD by remember { mutableStateOf(false) }
    var showExpressiveSheet by remember { mutableStateOf(false) }

    var isFullscreen by remember { mutableStateOf(true) }

    // Don't seed this from video.width/video.height: MediaStore reports raw
    // sensor dimensions and ignores the video's rotation metadata, so a
    // portrait video recorded on a landscape-sensor phone (the common case)
    // gets reported as landscape-sized even though it displays portrait.
    // Defaulting to portrait here is the safer starting guess, and
    // onVideoSizeChanged below (which *does* account for rotation) corrects
    // this the moment ExoPlayer actually knows the real, displayed size.
    var isForcedLandscape by remember { mutableStateOf(false) }
    var isBackgroundPlayEnabled by remember { mutableStateOf(backgroundPlayEnabled) }

    // Capture whatever brightness the window had *before* this screen's
    // gesture handler starts overwriting it, so it can be restored on exit.
    val originalBrightness = remember {
        context.findActivity()?.window?.attributes?.screenBrightness ?: -1f
    }

    // Synchronize orientation with state
    val isDarkTheme = LocalAppUiSettings.current.palette.isDark
    LaunchedEffect(isForcedLandscape) {
        updateOrientation(context, true, isForcedLandscape, isDarkTheme)
    }

    // Restore orientation, system bars, and brightness when leaving the
    // player screen. Without this, the activity stays locked to whatever
    // orientation was forced, the system bars remain hidden, and any
    // manual brightness change from the swipe gesture leaks into the
    // rest of the app after navigating away.
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
    val isInPip = PipState.isInPip

    // This screen is the only place PiP makes sense -- tell the Activity it's
    // safe to enter PiP while we're here.
    DisposableEffect(Unit) {
        PipState.canEnterPip = true
        onDispose { PipState.canEnterPip = false }
    }
    // Lock freezes the controls surface in place: gestures and taps no
    // longer toggle showControls or trigger seek/volume/brightness, and
    // only the lock pill itself remains tappable. Standard on MX
    // Player/VLC for watching without accidental touches (in a pocket,
    // handed to a kid, etc.) -- the rest of this screen's gesture/tap
    // handling below checks this flag before acting.
    var isControlsLocked by remember { mutableStateOf(false) }
    var isSidePanelExpanded by remember { mutableStateOf(false) }
    val chapters by produceState<List<VideoChapter>>(emptyList(), video.uri) {
        value = withContext(Dispatchers.IO) { ChapterReader.read(context, video.uri) }
    }
    var showAudioSelector by remember { mutableStateOf(false) }
    var isMuted by remember {
        val currentVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        mutableStateOf(currentVol == 0)
    }
    var showSubtitleSelector by remember { mutableStateOf(false) }
    var showSpeedSelector by remember { mutableStateOf(false) }
    var currentSpeed by remember { mutableFloatStateOf(defaultSpeed) }
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
    // Raw, instantaneous buffering signal straight from ExoPlayer. UI changes
    // that touch the player surface -- toggling resize mode, locking/
    // unlocking (which can detach/reattach the surface), brightness changes
    // -- sometimes make ExoPlayer report STATE_BUFFERING for a single frame
    // even though playback never actually stalls. Reacting to that raw flag
    // directly made the CircularProgressIndicator (a literal circle) flash
    // for a fraction of a second on actions that have nothing to do with
    // buffering. isLoading below is debounced off of this so only buffering
    // that persists past a short grace window actually shows the spinner.
    var isBufferingRaw by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }
    var playbackState by remember { mutableIntStateOf(Player.STATE_IDLE) }
    var position by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(0L) }

    var autoNextSeconds by remember { mutableIntStateOf(5) }
    var autoNextActive by remember { mutableStateOf(false) }

    // End of a video: either the player hit STATE_ENDED (last item) or paused at the end of an item
    // (pauseAtEndOfMediaItems) so we can show the "Up next" countdown before moving on.
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

    // Auto-hide controls logic lives in a single consolidated LaunchedEffect
    // further down (once isControlsLocked/showVerticalBar are in scope) --
    // previously there were two separate auto-hide timers running with
    // different delays (3500ms vs 3000ms) and different exclusion
    // conditions, racing each other and sometimes hiding controls out from
    // under an open dialog.

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
            // Previously the only save points were switching videos or
            // leaving the screen -- if the app got killed (or crashed)
            // mid-playback, whatever progress happened since then was lost
            // entirely. Persisting every ~5s bounds that loss.
            msSinceLastSave += 500
            if (resumePlayback && msSinceLastSave >= 5000) {
                msSinceLastSave = 0
                savePlaybackPosition(context, video.path, exoPlayer.currentPosition, exoPlayer.duration)
            }
        }
    }

    // Single source of truth for this player's lifecycle: all listeners are
    // registered here, and the player is released here too, so creation,
    // listening, and teardown all live in one place instead of being split
    // across several effects that have to stay in sync by convention.
    // Sync shuffle and repeat states to player
    LaunchedEffect(exoPlayer, isShuffleEnabled, repeatMode) {
        exoPlayer.shuffleModeEnabled = isShuffleEnabled
        exoPlayer.repeatMode = repeatMode
        // Pause at the end of each item so the "Up next" countdown can run (not when looping one video).
        exoPlayer.setPauseAtEndOfMediaItems(repeatMode != Player.REPEAT_MODE_ONE)
    }

    var currentVideoSize by remember { mutableStateOf<VideoSize?>(null) }

    // Hand the Activity the video's real aspect ratio so a PiP window isn't
    // letterboxed to a wrong default. Recomputed whenever the reported size
    // (or its rotation) changes.
    LaunchedEffect(currentVideoSize) {
        currentVideoSize?.let { size ->
            if (size.width > 0 && size.height > 0) {
                val rotated = size.unappliedRotationDegrees % 180 != 0
                val w = if (rotated) size.height else size.width
                val h = if (rotated) size.width else size.height
                // PiP aspect ratio is capped by the platform to between 1:2.39 and 2.39:1.
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

                // Some devices' hardware decoders reject a file outright (bad
                // profile/level, odd pixel format, etc.) even with decoder
                // fallback enabled. When that happens, do one automatic retry
                // from the same position with the current media item -- this
                // forces ExoPlayer to re-run decoder selection, which in
                // practice is often enough to land on nextlib's FFmpeg
                // software decoder instead of failing outright. Only one
                // retry per video, so a genuinely broken file still surfaces
                // an error instead of retry-looping forever.
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

                // PlaybackException.localizedMessage on a source/IO failure
                // is often just a generic "Source error" -- it doesn't say
                // *why* the source failed (network unreachable, 404, DNS
                // failure, blocked cleartext HTTP, unsupported container).
                // errorCodeName carries that detail (e.g.
                // ERROR_CODE_IO_NETWORK_CONNECTION_FAILED,
                // ERROR_CODE_IO_BAD_HTTP_STATUS,
                // ERROR_CODE_IO_UNSPECIFIED for a missing INTERNET
                // permission), so surface both instead of just the vague
                // top-level message.
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
                // Keep the global 'selectedVideo' state in sync with the player's
                // internal playlist state. This ensures that the HUD, Title, and
                // Recents update automatically as the playlist advances.
                val index = exoPlayer.currentMediaItemIndex
                if (index >= 0 && index < currentFolderVideos.size) {
                    val newVideo = currentFolderVideos[index]

                    // Respect the Autoplay setting: if the transition was automatic (auto-advance)
                    // but autoplay is disabled, pause immediately.
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

    // Pause playback when the app is backgrounded, unless the user has
    // enabled background play. Note: this is a best-effort toggle, not a
    // real foreground-service implementation. Android can still kill the
    // player at any time once the app is backgrounded since there's no
    // notification keeping it alive — there are no guarantees on how
    // long playback will continue.
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

    // Continuous pinch-zoom + pan on the video surface itself (separate
    // from resizeModeIndex, which only cycles Fit/Fill/Zoom). A quick pinch
    // still cycles aspect ratio as before; sustaining the pinch past that
    // first step engages free-form zoom for the rest of that gesture.
    var videoZoomScale by remember { mutableFloatStateOf(1f) }
    var videoPanX by remember { mutableFloatStateOf(0f) }
    var videoPanY by remember { mutableFloatStateOf(0f) }
    var isZoomGestureActive by remember { mutableStateOf(false) }
    val minZoom = 1f
    val maxZoom = 4f

    // Reset zoom/pan whenever the video changes -- otherwise zoom from a
    // previous video (e.g. after tapping into the next item in
    // folderVideos) carried straight into the new one, which looked like
    // zoom being "stuck" rather than a per-video setting.
    LaunchedEffect(video.uri) {
        videoZoomScale = minZoom
        videoPanX = 0f
        videoPanY = 0f
    }

    // Clamp pan so the zoomed video can't be dragged entirely off-screen.
    // Half of the "extra" size beyond the original bounds is the max
    // allowed offset in each direction, in either axis.
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

    // Each double-tap gets a unique token (even on the same side), so that
    // tapping the same side again before the previous animation finishes
    // still restarts it. A plain "left"/"right" string would be equal to
    // its previous value on a repeat tap, and LaunchedEffect wouldn't
    // relaunch — so the fade/expand animation would silently fail to replay.
    var seekAnimationToken by remember { mutableIntStateOf(0) }
    var seekAnimationSide by remember { mutableStateOf<String?>(null) }
    var cumulativeSeekAmount by remember { mutableIntStateOf(0) }
    // Where inside the tapped half (0..1) the last double-tap landed — the seek flash ripples from here.
    var seekTapX by remember { mutableFloatStateOf(0.5f) }
    var seekTapY by remember { mutableFloatStateOf(0.5f) }

    // Gesture tracking — stored by downTime (ms) so they survive Android's
    // MotionEvent recycling/pooling that makes reference-equality (===) unreliable.
    var gestureDownTime by remember { mutableLongStateOf(-1L) }
    var gestureStartY by remember { mutableFloatStateOf(0f) }   // e1.y at finger-down
    var gestureStartBrightness by remember { mutableFloatStateOf(0f) }
    var gestureStartVolumeValue by remember { mutableFloatStateOf(0f) }
    // Tracks the last committed 0.0–2.0 volume level across gestures.
    // ExoPlayer clamps its own .volume to 0–1, so we can't read boost back from it.
    var lastVolumeGestureValue by remember {
        val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val sysVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        mutableFloatStateOf(sysVol.toFloat() / maxVol.toFloat())
    }

    // ── Multi-pointer tracking ──────────────────────────────────────────
    // ScaleGestureDetector.isInProgress only flips true *after* onScaleBegin
    // fires, which itself only fires once the detector decides a real pinch
    // has started. In the gap between "second finger touches down" and
    // "detector confirms it's a pinch," the single-finger GestureDetector
    // was still receiving the same stream and could carry on (or even start)
    // a brightness/volume swipe from the first finger's motion. That's the
    // overlap: a vertical swipe gesture and a pinch gesture running at once.
    //
    // isMultiTouch flips true the instant a second pointer goes down --
    // immediately, not after the detector's own confirmation delay -- and
    // is the single source of truth both gestures check against.
    var isMultiTouch by remember { mutableStateOf(false) }
    // Once a pinch has actually moved enough to commit to a zoom/aspect
    // gesture, this stays true until both fingers lift, so a brief
    // single-finger residue (e.g. one finger lifting a frame before the
    // other) can't reopen the brightness/volume swipe mid-gesture.
    var pinchSessionActive by remember { mutableStateOf(false) }

    val mainActivity = context.findActivity() as? MainActivity
    DisposableEffect(mainActivity) {
        mainActivity?.onVolumeKeyEvent = { keyCode, _ ->
            if (volumeGestureEnabled) {
                val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                val currentSysVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)

                var currentUnified = if (currentSysVol >= maxVolume) {
                    lastVolumeGestureValue.coerceIn(1f, 2f)
                } else {
                    currentSysVol.toFloat() / maxVolume.toFloat()
                }

                val step = 0.05f // 5% step
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
                if (!pinchToZoomEnabled) return false
                // A two-finger touch is unambiguously a pinch from the
                // moment it's recognized -- shut down any brightness/volume
                // swipe that was running on the first finger right away,
                // and keep it shut for the rest of this pinch session even
                // if a finger briefly lifts (handled by pinchSessionActive
                // below, cleared only on full pointer-up).
                showVerticalBar = false
                pinchSessionActive = true

                totalScale *= detector.scaleFactor

                // First threshold crossing in this gesture: behave exactly
                // like before and step the aspect-ratio cycle once.
                //
                // Deadzone fix: detector.scaleFactor reports a value on
                // *every* onScale call, including the tiny sub-pixel jitter
                // that happens while a slow pinch is still settling into a
                // direction. Multiplying that noise into totalScale could
                // cross the 1.2 / 0.8 thresholds from accumulated jitter
                // alone, well before the fingers had moved a deliberate
                // distance -- which read as the aspect ratio "jumping"
                // erratically instead of responding smoothly to an
                // intentional pinch. Requiring the current span to differ
                // from the gesture's starting span by a real minimum
                // amount (not just the ratio) filters that out: tiny
                // jitter never accumulates a meaningful span delta, but a
                // genuine pinch crosses it within the first few frames of
                // real finger movement.
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
                        // Already at the last resize mode (Zoom) — instead
                        // of doing nothing, let this same continued pinch
                        // fall through into continuous zoom below.
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

                // Continuous zoom: applies for the rest of this gesture
                // once the single-step cycle above has already happened
                // (or had nowhere left to go).
                isZoomGestureActive = true
                val newScale = (videoZoomScale * detector.scaleFactor).coerceIn(minZoom, maxZoom)
                videoZoomScale = newScale

                // Let the pinch's focal point also drag the video around
                // (two-finger pan while zoomed), not just resize it.
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
                exoPlayer.setPlaybackSpeed(2.0f)
                gestureText = "2x"
            }

            override fun onScroll(
                e1: MotionEvent?,
                e2: MotionEvent,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                // Was: only `scaleGestureDetector.isInProgress`, which is
                // false during the brief window between a second finger
                // touching down and the detector officially confirming a
                // pinch. In that window this single-finger scroll could
                // still start (or continue) a brightness/volume swipe,
                // which then kept running alongside the pinch once it took
                // over -- the overlap. pinchSessionActive flips true the
                // instant onScale first runs, and isMultiTouch flips true
                // the instant a second pointer is detected at the touch
                // dispatcher, closing that window from both ends.
                if (e1 == null || scaleGestureDetector.isInProgress || pinchSessionActive || isMultiTouch) {
                    return false
                }

                val screenHeight = context.resources.displayMetrics.heightPixels.toFloat()
                val screenWidth = context.resources.displayMetrics.widthPixels

                // Guard: only consider vertical-dominant swipes
                val totalDX = abs(e2.x - e1.x)
                val totalDY = abs(e2.y - e1.y)
                if (totalDY < totalDX) return false

                // Capture gesture-start values exactly once per finger-down,
                // identified by downTime so MotionEvent recycling can't fool us.
                if (e1.downTime != gestureDownTime) {
                    gestureDownTime = e1.downTime
                    gestureStartY = e1.y

                    // Brightness — read the live window attribute right now
                    val activity = context.findActivity()
                    val curBrightness = activity?.window?.attributes?.screenBrightness ?: -1f
                    gestureStartBrightness = if (curBrightness < 0f) 0.5f else curBrightness

                    // Volume — unified 0.0–2.0 scale:
                    //   0.0–1.0 = system volume 0%–100%
                    //   1.0–2.0 = ExoPlayer amplification 100%–200%
                    val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                    val sysVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                    gestureStartVolumeValue = if (sysVol >= maxVol) {
                        lastVolumeGestureValue.coerceIn(
                            1f,
                            2f
                        )  // carry over boost — ExoPlayer clamps to 1 so we track it ourselves
                    } else {
                        sysVol.toFloat() / maxVol.toFloat()
                    }

                    // Seed the bar at the current level so it never jumps on first touch
                    gestureValue = if (e1.x < screenWidth / 2) gestureStartBrightness
                    else gestureStartVolumeValue
                }

                // deltaY: positive = finger moved UP = increase value
                // We always measure from the original finger-down Y so the
                // relationship between finger position and bar level is 1-to-1
                // and never drifts or snaps mid-gesture.
                val deltaY = gestureStartY - e2.y

                showVerticalBar = true

                if (e1.x < screenWidth / 2) {
                    if (!brightnessGestureEnabled) return false
                    // ── Brightness (left half) ────────────────────────────────
                    // Full screen height = 0→100% brightness (comfortable 1:1 feel)
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
                    if (!volumeGestureEnabled) return false
                    // ── Volume (right half) ───────────────────────────────────
                    // Full screen height = 0→200% (0.0→2.0). The 0.9 factor
                    // gives a slightly generous range — same feel as MX/VLC.
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
                // If zoomed in, double-tap resets zoom instead of seeking —
                // otherwise there'd be no obvious way to get back to 1x.
                if (videoZoomScale > minZoom) {
                    videoZoomScale = minZoom
                    videoPanX = 0f
                    videoPanY = 0f
                    gestureText = "Zoom reset"
                    return true
                }

                val screenWidth =
                    context.resources.displayMetrics.widthPixels
                val total = exoPlayer.duration.takeUnless {
                    it ==
                            C.TIME_UNSET
                } ?: duration

                val side = if (e.x < screenWidth / 2) "left" else "right"
                val halfW = screenWidth / 2f
                seekTapX = (if (side == "left") e.x / halfW else (e.x - halfW) / halfW).coerceIn(0f, 1f)
                seekTapY = (e.y / context.resources.displayMetrics.heightPixels.toFloat()).coerceIn(0f, 1f)
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                val skipMs = skipSeconds * 1000L
                if (seekAnimationSide != side) {
                    cumulativeSeekAmount = skipSeconds
                } else {
                    cumulativeSeekAmount += skipSeconds
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

    // Unified hide-timer for the vertical bar + its center gesture text.
    // These used to run on two separate LaunchedEffects with different keys
    // and different durations (bar: keyed on gestureValue, 1000ms / text:
    // keyed on the gestureText string itself, 2000ms). "Brightness"/"Volume"
    // don't change as strings while scrolling, so the text's effect never
    // restarted mid-swipe and could time out and vanish while the bar was
    // still visible -- and on release the two cleared at different moments
    // instead of together. Keying one effect on every value that can change
    // during a gesture (text, bar visibility, and the live value) guarantees
    // both always restart and hide in sync.
    LaunchedEffect(gestureText, showVerticalBar, gestureValue, isLongPressing) {
        if ((showVerticalBar || gestureText.isNotEmpty()) && !isLongPressing) {
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
        showAudioSelector, showSubtitleSelector, showSpeedSelector, showHUD, showFolderVideos, showExpressiveSheet
    ) {
        if (showControls && isPlaying && !isScrubbing && !showVerticalBar &&
            !showAudioSelector && !showSubtitleSelector && !showSpeedSelector &&
            !showHUD && !showFolderVideos && !showExpressiveSheet
        ) {
            delay(3000L)
            showControls = false
        }
    }

    // True while an in-progress touch gesture (brightness/volume swipe,
    // long-press 2x speed, or an active pinch) owns the screen. Modern
    // players hide the persistent chrome (top bar, side lock/rotate,
    // bottom bar) while one of these is active so it doesn't visually
    // compete with the gesture's own feedback indicator, then let it
    // reappear once the gesture ends.
    val isGestureOverlayActive = showVerticalBar || isLongPressing || pinchSessionActive

    // Debounce the loading spinner: only treat it as "really buffering" if
    // the raw signal is still true after a short grace window. Momentary
    // surface hiccups from resize-mode changes, lock/unlock, etc. clear
    // within a frame or two and never reach this delay, so the spinner
    // circle no longer flashes for those. Genuine buffering (network stalls,
    // seeks into unbuffered ranges) lasts well past 250ms and still shows it.
    LaunchedEffect(isBufferingRaw, showVerticalBar) {
        if (isBufferingRaw && !showVerticalBar) {
            delay(500)
            if (isBufferingRaw && !showVerticalBar) isLoading = true
        } else {
            isLoading = false
        }
    }

    // In-tree windows (speed, folders, HUD) blur + dim the live video behind them (liquid-glass backdrop).
    val panelOpen = false   // panels are in-tree side/bottom sheets now; the video stays sharp and playing
    val panelBlur by animateDpAsState(if (panelOpen) 24.dp else 0.dp, tween(320), label = "panelBlur")
    val panelScrim by animateFloatAsState(if (panelOpen) 1f else 0f, tween(320), label = "panelScrim")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // ── Cinematic Blurred Background ─────────────────────────────
        // When not in forced landscape, use the video colors to fill the void
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
            // Extra darkening scrim
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)))
        }

        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = videoZoomScale
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

                    // Update isMultiTouch *before* feeding either detector,
                    // and as early in the dispatch as possible -- on the
                    // raw ACTION_POINTER_DOWN/UP actions, not on anything
                    // either GestureDetector or ScaleGestureDetector decides
                    // to report. This is what closes the timing gap: by the
                    // time onScroll or onScale run for this same event,
                    // isMultiTouch already reflects the true finger count.
                    when (motionEvent.actionMasked) {
                        MotionEvent.ACTION_POINTER_DOWN -> {
                            if (motionEvent.pointerCount >= 2) {
                                isMultiTouch = true
                                // A second finger landing always means
                                // "this is no longer a single-finger
                                // brightness/volume swipe" -- close the bar
                                // immediately rather than waiting for the
                                // scale detector to confirm a pinch.
                                showVerticalBar = false
                            }
                        }
                        MotionEvent.ACTION_POINTER_UP -> {
                            // pointerCount here still includes the finger
                            // that's lifting; once it's down to 2, the
                            // *other* remaining pointer is the only one
                            // left after this event resolves.
                            if (motionEvent.pointerCount <= 2) {
                                isMultiTouch = false
                            }
                        }
                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                            // All fingers off the glass -- fully close out
                            // the pinch session so the next single-finger
                            // touch is free to start a fresh swipe.
                            isMultiTouch = false
                            pinchSessionActive = false
                        }
                    }

                    scaleGestureDetector.onTouchEvent(motionEvent)
                    gestureDetector.onTouchEvent(motionEvent)

                    // Handle lift-to-reset 2x speed
                    if (motionEvent.action == MotionEvent.ACTION_UP || motionEvent.action == MotionEvent.ACTION_CANCEL) {
                        if (isLongPressing) {
                            isLongPressing = false
                            exoPlayer.setPlaybackSpeed(currentSpeed)
                            // Only show speed if not resetting to 1x
                            gestureText = if (currentSpeed != 1.0f) "${String.format("%.2f", currentSpeed)}x" else ""
                        }
                    }
                    true
                }
        )

        if (!isInPip) {
            val settings = LocalAppUiSettings.current

            // Resume / Start Over pill — sits just above the seekbar (clear of title, top bar and video centre)
            AnimatedVisibility(
                visible = showResumePrompt,
                enter = fadeIn(tween(260)) + slideInVertically(tween(320, easing = FastOutSlowInEasing)) { it / 2 },
                exit = fadeOut(tween(200)) + slideOutVertically(tween(240)) { it / 2 },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = if (isLandscape) 118.dp else 132.dp)
            ) {
                GlassSurface(shape = CircleShape, overVideo = true, strong = true, elevation = 12.dp, pressedScale = 1f) {
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
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, fontSize = 11.5.sp),
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
                    // ── Scrims: minimal top & bottom subtle gradient overlay ──
                    Box(
                        Modifier.fillMaxWidth().height(80.dp).align(Alignment.TopCenter)
                            .background(Brush.verticalGradient(listOf(Color.Black.copy(0.32f), Color.Transparent)))
                    )
                    Box(
                        Modifier.fillMaxWidth().height(100.dp).align(Alignment.BottomCenter)
                            .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(0.38f))))
                    )

                    // ── Top bar: [back] title ········ [ pip | lock | controls ] ──
                    val anyExtraOn = isShuffleEnabled || repeatMode != Player.REPEAT_MODE_OFF ||
                            isBackgroundPlayEnabled || isMuted || currentSpeed != 1.0f
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
                            ChromePill {
                                ChromeIconButton(painterResource(R.drawable.ic_back_custom), "Back", size = 36.dp, onClick = onBack)
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
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

                        ChromePill {
                            ChromeIconButton(painterResource(R.drawable.ic_pip_custom), "Picture in picture", size = 36.dp) {
                                (context.findActivity() as? MainActivity)?.enterPipModeNow()
                            }
                            if (isLandscape) {
                                ChromeDivider()
                                ChromeIconButton(painterResource(R.drawable.ic_lock_custom), "Lock", size = 36.dp) { isControlsLocked = true }
                            }
                            ChromeDivider()
                            Box {
                                ChromeIconButton(painterResource(R.drawable.ic_player_controls), "Player Controls", active = showExpressiveSheet, size = 36.dp) {
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
                        }
                    }

                    // Middle right: rotate (portrait only — in landscape it lives in the bottom-right group)
                    if (!isLandscape) Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 16.dp)
                    ) {
                        ChromePill {
                            ChromeIconButton(
                                painterResource(R.drawable.ic_orientation_custom),
                                "Rotate",
                                active = isForcedLandscape,
                                size = 36.dp
                            ) {
                                isForcedLandscape = !isForcedLandscape
                                gestureText = if (isForcedLandscape) "LANDSCAPE" else "AUTO ROTATE"
                            }
                        }
                    }

                    // ── Bottom group: seekbar on the scrim + ONE glass transport dock ──
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(horizontal = 20.dp)
                            .padding(bottom = if (isLandscape) 10.dp else 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // [elapsed] ───●─── [total]
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                formatDuration(position),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold, fontSize = 12.sp, fontFeatureSettings = "tnum",
                                    shadow = androidx.compose.ui.graphics.Shadow(Color.Black.copy(alpha = 0.6f), blurRadius = 8f)
                                ),
                                color = Color.White,
                                modifier = Modifier.widthIn(min = 40.dp).padding(end = 8.dp)
                            )
                            SmoothPlayerScrubber(
                                exoPlayer = exoPlayer,
                                duration = duration,
                                videoUri = video.uri,
                                chapters = chapters,
                                onScrubbingChanged = { isScrubbing = it; if (it) showControls = true },
                                onPositionChanged = { position = it },
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                formatDuration(duration),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold, fontSize = 12.sp, fontFeatureSettings = "tnum",
                                    shadow = androidx.compose.ui.graphics.Shadow(Color.Black.copy(alpha = 0.6f), blurRadius = 8f)
                                ),
                                color = Color.White.copy(alpha = 0.72f),
                                modifier = Modifier.widthIn(min = 40.dp).padding(start = 8.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.End
                            )
                        }

                        val isEnded = videoEnded
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Left: browse (+ subtitles & audio in landscape) in ONE glass pill
                            Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                                val hasBrowse = folderVideos.size > 1
                                if (hasBrowse || isLandscape) {
                                    ChromePill {
                                        if (hasBrowse) {
                                            ChromeIconButton(painterResource(R.drawable.ic_browse_list_custom), "Browse Folder", active = showFolderVideos, size = 36.dp) {
                                                showFolderVideos = true
                                            }
                                        }
                                        if (isLandscape) {
                                            if (hasBrowse) ChromeDivider()
                                            ChromeIconButton(painterResource(R.drawable.ic_subtitles_custom), "Subtitles", active = showSubtitleSelector, size = 36.dp) {
                                                showSubtitleSelector = true
                                            }
                                            ChromeDivider()
                                            ChromeIconButton(painterResource(R.drawable.ic_music_custom), "Audio track", active = showAudioSelector, size = 36.dp) {
                                                showAudioSelector = true
                                            }
                                        }
                                    }
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                ChromePill {
                                    ChromeIconButton(painterResource(R.drawable.ic_prev_custom), "Previous", size = 36.dp) {
                                        if (exoPlayer.hasPreviousMediaItem()) { exoPlayer.seekToPreviousMediaItem(); gestureText = "PREVIOUS" }
                                        else gestureText = "PREVIOUS"
                                    }
                                }
                                HeroPlayButton(isPlaying = isPlaying, isEnded = isEnded) {
                                    if (isEnded) { exoPlayer.seekTo(0); exoPlayer.play(); gestureText = "REPLAY" }
                                    else if (exoPlayer.isPlaying) { exoPlayer.pause(); gestureText = "PAUSED" } else { exoPlayer.play(); gestureText = "PLAYING" }
                                }
                                ChromePill {
                                    ChromeIconButton(painterResource(R.drawable.ic_next_custom), "Next", size = 36.dp) {
                                        if (exoPlayer.hasNextMediaItem()) { exoPlayer.seekToNextMediaItem(); gestureText = "NEXT" }
                                        else gestureText = "NEXT"
                                    }
                                }
                            }

                            // Right: speed + rotate (landscape) + aspect
                            Box(Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                                ChromePill {
                                    if (isLandscape) {
                                        ChromeIconButton(painterResource(R.drawable.ic_speed_custom), "Speed", active = showSpeedSelector || currentSpeed != 1.0f, size = 36.dp) {
                                            showSpeedSelector = true
                                        }
                                        ChromeDivider()
                                        ChromeIconButton(
                                            painterResource(R.drawable.ic_orientation_custom), "Rotate",
                                            active = isForcedLandscape, size = 36.dp
                                        ) {
                                            isForcedLandscape = !isForcedLandscape
                                            gestureText = if (isForcedLandscape) "LANDSCAPE" else "AUTO ROTATE"
                                        }
                                        ChromeDivider()
                                    }
                                    ChromeIconButton(
                                        painterResource(when (resizeModeIndex) { 0 -> R.drawable.ic_aspect_fit; 1 -> R.drawable.ic_aspect_fill; else -> R.drawable.ic_aspect_zoom }),
                                        "Aspect", size = 36.dp
                                    ) {
                                        resizeModeIndex = (resizeModeIndex + 1) % resizeModes.size
                                        gestureText = resizeModes[resizeModeIndex].second.uppercase()
                                    }
                                }
                            }
                        }
                    }
                }
            }




            // Corrupt / unplayable file: joke screen with retry
            playbackError?.let { err ->
                CorruptVideoScene(
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

            // Gesture Feedback Pill (Always on top when active)
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
                    if (lastGestureText.equals("2x", ignoreCase = true)) {
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

            // Tap to Unlock - Subtle Rotating Lock
            AnimatedVisibility(
                visible = isControlsLocked && showControls,
                enter = fadeIn(tween(400)) + scaleIn(spring(stiffness = Spring.StiffnessLow)),
                exit = fadeOut(tween(300)) + scaleOut(),
                modifier = Modifier.align(Alignment.CenterEnd).padding(end = 32.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .aliveClickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            isControlsLocked = false
                            showControls = true
                        },
                    contentAlignment = Alignment.Center
                ) {
                    BrandPlayOverlay(
                        size = 52.dp,
                        iconOverride = painterResource(R.drawable.ic_lock_custom)
                    )
                }
            }

            // Lock Screen Timer (Bottom Left) - Subtle
            AnimatedVisibility(
                visible = isControlsLocked && subtleLockTimer,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.BottomStart).padding(start = 24.dp, bottom = 24.dp)
            ) {
                Text(
                    formatDuration(position),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = Color.White.copy(alpha = 0.35f)
                )
            }

            // Gesture bars: brightness (left) / volume (right)
            GestureIndicator(
                visible = showVerticalBar,
                isBrightness = isBarBrightness,
                value = gestureValue,
                modifier = Modifier.fillMaxSize()
            )

            // Overlays & Flashing
            Box(modifier = Modifier.fillMaxSize()) {
                PremiumSeekFlash(seekAnimationSide == "left", seekAnimationToken, cumulativeSeekAmount, false, Modifier.align(Alignment.CenterStart), seekTapX, seekTapY)
                PremiumSeekFlash(seekAnimationSide == "right", seekAnimationToken, cumulativeSeekAmount, true, Modifier.align(Alignment.CenterEnd), seekTapX, seekTapY)
            }
            if (showHUD) PremiumHUD(video, currentVideoSize, exoPlayer, onDismiss = { showHUD = false })
            TrackSelectionPanel(showAudioSelector, exoPlayer, C.TRACK_TYPE_AUDIO, { showAudioSelector = false })
            TrackSelectionPanel(
                showSubtitleSelector, exoPlayer, C.TRACK_TYPE_TEXT, { showSubtitleSelector = false },
                onAddLocalSubtitle = { subtitlePickerLauncher.launch(arrayOf("*/*")) },
                onSearchOnline = { showOnlineSubtitleSearch = true },
                onCustomize = { showSubtitleCustomization = true }
            )
            if (showSubtitleCustomization) SubtitleCustomizationDialog { showSubtitleCustomization = false }
            if (showOnlineSubtitleSearch) {
                OnlineSubtitleSearchDialog(video.name, { showOnlineSubtitleSearch = false }) { url ->
                    val subtitleUri = Uri.parse(url)
                    val subtitleConfig = MediaItem.SubtitleConfiguration.Builder(subtitleUri)
                        .setMimeType(guessSubtitleMimeType(context, subtitleUri))
                        .setLanguage("en")
                        .setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
                        .build()
                    val currentMediaItem = exoPlayer.currentMediaItem
                    if (currentMediaItem != null) {
                        val currentSubtitles = currentMediaItem.localConfiguration?.subtitleConfigurations.orEmpty()
                        val newMediaItem = currentMediaItem.buildUpon()
                            .setSubtitleConfigurations(currentSubtitles + subtitleConfig)
                            .build()
                        val currentPos = exoPlayer.currentPosition
                        exoPlayer.setMediaItem(newMediaItem, currentPos)
                        exoPlayer.prepare()
                        exoPlayer.play()
                    }
                }
            }
            PlaybackSpeedSidePanel(showSpeedSelector, currentSpeed, { currentSpeed = it; exoPlayer.setPlaybackSpeed(it) }, { showSpeedSelector = false })

            // ── Folder browser ──
            PlayerPanel(
                visible = showFolderVideos && folderVideos.size > 1,
                onDismiss = { showFolderVideos = false; autoNextActive = false },
                title = if (videoEnded && autoNextActive && autoNextSeconds > 0) "Up next"
                else browserTitle ?: "Media Library",
                subtitle = "${folderVideos.size} videos" + (browserSubtitle?.let { " · $it" } ?: ""),
                scrollable = false
            ) {
                if (videoEnded && autoNextActive && autoNextSeconds > 0) {
                    UpNextCard(
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
                        BrowserVideoRowExpressive(
                            video = item,
                            isSelected = isCurrent,
                            onClick = { autoNextActive = false; if (!isCurrent) { showFolderVideos = false; onPlayVideo(item) } }
                        )
                    }
                }
            }

            // ── Playback settings (every extra control lives here) ──
            val iconInfo = androidx.compose.ui.graphics.vector.rememberVectorPainter(androidx.compose.material.icons.Icons.Rounded.Info)
            PlayerPanel(
                visible = showExpressiveSheet,
                onDismiss = { showExpressiveSheet = false },
                title = "Playback settings",
                subtitle = formatFileName(video.name)
            ) {
                PanelSection("Playback speed") {
                    PanelCard {
                        Row(
                            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 10.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(0.5f, 0.75f, 1f, 1.25f, 1.5f, 2f, 3f).forEach { s ->
                                MoreSpeedChip(speedLabel(s), abs(currentSpeed - s) < 0.01f) { currentSpeed = s; exoPlayer.setPlaybackSpeed(s) }
                            }
                        }
                        PanelDivider()
                        PanelNavRow(painterResource(R.drawable.ic_speed_custom), "Fine-tune speed",
                            value = speedLabel(currentSpeed), active = currentSpeed != 1.0f) {
                            showExpressiveSheet = false; showSpeedSelector = true
                        }
                    }
                }

                PanelSection("Playback mode") {
                    PanelCard {
                        PanelNavRow(
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
                        PanelDivider()
                        PanelToggleRow(painterResource(R.drawable.ic_shuffle_custom), "Shuffle", isShuffleEnabled) {
                            isShuffleEnabled = it
                            gestureText = if (it) "Shuffle On" else "Shuffle Off"
                        }
                        PanelDivider()
                        PanelToggleRow(painterResource(if (isBackgroundPlayEnabled) R.drawable.ic_headphones_custom else R.drawable.ic_headphones_off_custom),
                            "Background play", isBackgroundPlayEnabled, subtitle = "Keep audio playing when the app is closed") {
                            isBackgroundPlayEnabled = it
                            gestureText = if (it) "Background on" else "Background off"
                        }
                        PanelDivider()
                        PanelToggleRow(painterResource(if (isMuted) R.drawable.ic_volume_off_custom else R.drawable.ic_volume_custom), "Mute", isMuted) {
                            isMuted = it
                            val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, if (it) 0 else maxVol / 2, 0)
                            gestureText = if (it) "Muted" else "Unmuted"
                        }
                    }
                }

                PanelSection("Media") {
                    PanelCard {
                        PanelNavRow(painterResource(R.drawable.ic_music_custom), "Audio track",
                            value = selectedTrackLabel(exoPlayer, C.TRACK_TYPE_AUDIO, "Default")) {
                            showExpressiveSheet = false; showAudioSelector = true
                        }
                        PanelDivider()
                        PanelNavRow(painterResource(R.drawable.ic_subtitles_custom), "Subtitles",
                            value = selectedTrackLabel(exoPlayer, C.TRACK_TYPE_TEXT, "Default")) {
                            showExpressiveSheet = false; showSubtitleSelector = true
                        }
                        if (folderVideos.size > 1) {
                            PanelDivider()
                            PanelNavRow(painterResource(R.drawable.ic_browse_list_custom), "Playlist", value = "${folderVideos.size} videos") {
                                showExpressiveSheet = false; showFolderVideos = true
                            }
                        }
                        PanelDivider()
                        PanelNavRow(painterResource(R.drawable.ic_info_custom), "Media info") { showExpressiveSheet = false; showHUD = true }
                    }
                }

                PanelSection("Display") {
                    PanelCard {
                        PanelToggleRow(painterResource(R.drawable.ic_orientation_custom), "Force landscape", isForcedLandscape) {
                            isForcedLandscape = it
                            gestureText = if (it) "Landscape" else "Auto rotate"
                        }
                        PanelDivider()
                        PanelNavRow(
                            painterResource(when (resizeModeIndex) { 0 -> R.drawable.ic_aspect_fit; 1 -> R.drawable.ic_aspect_fill; else -> R.drawable.ic_aspect_zoom }),
                            "Aspect ratio", value = resizeModes[resizeModeIndex].second, showChevron = false
                        ) {
                            resizeModeIndex = (resizeModeIndex + 1) % resizeModes.size
                            gestureText = resizeModes[resizeModeIndex].second
                        }
                        PanelDivider()
                        PanelNavRow(painterResource(R.drawable.ic_lock_custom), "Lock screen") {
                            showExpressiveSheet = false; isControlsLocked = true
                        }
                    }
                }
            }
        } // end if (!isInPip)
    }
}

/** "Up next" card at the top of the browse panel: countdown ring, next title, Play now / Cancel. */
@Composable
private fun UpNextCard(
    next: VideoFile?,
    seconds: Int,
    total: Int,
    onPlayNow: () -> Unit,
    onCancel: () -> Unit
) {
    val s = LocalAppUiSettings.current
    val accent = s.accent
    val hi = s.highlight
    val prog by animateFloatAsState(seconds / total.toFloat(), tween(1000, easing = LinearEasing), label = "upNextRing")
    val shape = RoundedCornerShape(18.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .clip(shape)
            .background(Brush.linearGradient(listOf(accent.copy(alpha = 0.30f), Color.White.copy(alpha = 0.06f))))
            .border(1.dp, Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.24f), Color.White.copy(alpha = 0.04f))), shape)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.size(46.dp), contentAlignment = Alignment.Center) {
                androidx.compose.foundation.Canvas(Modifier.fillMaxSize()) {
                    val sw = 4.dp.toPx()
                    val inset = sw / 2f
                    val arcSize = androidx.compose.ui.geometry.Size(size.width - sw, size.height - sw)
                    val tl = androidx.compose.ui.geometry.Offset(inset, inset)
                    drawArc(Color.White.copy(alpha = 0.16f), 0f, 360f, false, tl, arcSize,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(sw))
                    drawArc(lerp(accent, hi, 0.35f), -90f, 360f * prog, false, tl, arcSize,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(sw, cap = androidx.compose.ui.graphics.StrokeCap.Round))
                }
                Text(
                    "$seconds",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, fontFeatureSettings = "tnum")
                )
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    "PLAYING IN ${seconds}s",
                    color = lerp(accent, hi, 0.55f),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp, fontSize = 10.sp)
                )
                Text(
                    next?.let { formatFileName(it.name) } ?: "Next video",
                    color = Color.White,
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
                    .height(36.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(lerp(accent, hi, 0.30f), accent)))
                    .aliveClickable { onPlayNow() },
                contentAlignment = Alignment.Center
            ) {
                Text("Play now", color = Color.White, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
            }
            Box(
                Modifier
                    .weight(1f)
                    .height(36.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.12f))
                    .aliveClickable { onCancel() },
                contentAlignment = Alignment.Center
            ) {
                Text("Cancel", color = Color.White, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
            }
        }
    }
}

/** Bare group of icon buttons — no glass, no fill. Legibility comes from each icon's soft shadow. */
@Composable
private fun ChromePill(content: @Composable RowScope.() -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, content = content)
}

@Composable
private fun ChromeDivider() {
    Spacer(Modifier.width(2.dp))
}

/** Bare icon with a soft drop shadow. Selected = tinted with the theme colour (no glow, no halo). */
@Composable
private fun ChromeIconButton(
    painter: Painter,
    description: String,
    active: Boolean = false,
    size: androidx.compose.ui.unit.Dp = 36.dp,
    onClick: () -> Unit
) {
    val settings = LocalAppUiSettings.current
    val interaction = remember { MutableInteractionSource() }
    val press by rememberPressScale(interaction, 0.86f)
    val haptic = rememberHaptic()
    val sel by animateFloatAsState(if (active) 1f else 0f, tween(180), label = "chromeSel")
    val tint = lerp(Color.White, lerp(settings.accent, settings.highlight, 0.55f), sel.coerceIn(0f, 1f))

    Box(
        modifier = Modifier
            .size(size)
            .graphicsLayer { scaleX = press; scaleY = press }
            .clip(CircleShape)
            .clickable(interactionSource = interaction, indication = null) { haptic(); onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Icon(
                painter, null, tint = Color.Black.copy(alpha = 0.55f),
                modifier = Modifier.size(20.dp).offset(y = 1.dp)
                    .blur(3.dp, androidx.compose.ui.draw.BlurredEdgeTreatment.Unbounded)
            )
        } else {
            Box(
                Modifier.size(size * 0.9f).background(
                    Brush.radialGradient(listOf(Color.Black.copy(alpha = 0.22f), Color.Transparent)), CircleShape
                )
            )
        }
        Icon(painter, description, tint = tint, modifier = Modifier.size(20.dp))
    }
}

/** Soft inset circle sitting inside the transport dock (prev / next / playlist / aspect). */
@Composable
private fun DockChip(
    size: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val press by rememberPressScale(interaction, 0.84f)
    val haptic = rememberHaptic()
    Box(
        modifier = Modifier
            .size(size)
            .graphicsLayer { scaleX = press; scaleY = press }
            .clip(CircleShape)
            .background(Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.14f), Color.White.copy(alpha = 0.05f))))
            .border(1.dp, Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.22f), Color.White.copy(alpha = 0.03f))), CircleShape)
            .clickable(interactionSource = interaction, indication = null) { haptic(); onClick() },
        contentAlignment = Alignment.Center,
        content = content
    )
}

@Composable
private fun HeroPlayButton(
    isPlaying: Boolean,
    isEnded: Boolean,
    onClick: () -> Unit
) {
    val playInteraction = remember { MutableInteractionSource() }
    val playScale by rememberPressScale(playInteraction, 0.88f)
    val playHaptic = rememberHaptic(HapticFeedbackType.LongPress)
    val settings = LocalAppUiSettings.current
    val heroAccent = settings.accent
    val heroHi = settings.highlight
    val heroBreath = rememberBreath(isPlaying && !isEnded)
    val organic = remember { OrganicShape(lobes = 8) }

    // The rotating organic aura the play button always had.
    val aura = rememberInfiniteTransition(label = "heroAura")
    val rotation by aura.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(10000, easing = LinearEasing)),
        label = "heroRotation"
    )
    val auraBrush = remember(heroAccent, heroHi) {
        Brush.radialGradient(listOf(lerp(heroAccent, heroHi, 0.30f), heroAccent, heroAccent.copy(alpha = 0.78f)))
    }

    Box(
        modifier = Modifier
            .size(60.dp)
            .graphicsLayer { scaleX = playScale; scaleY = playScale }
            .drawBehind {
                // contact shadow
                val sc = center.copy(y = center.y + 4.dp.toPx())
                val sr = size.minDimension * 0.82f
                drawCircle(Brush.radialGradient(listOf(Color.Black.copy(alpha = 0.34f), Color.Transparent), center = sc, radius = sr), sr, sc)
                // accent halo while playing
                if (isPlaying && !isEnded) {
                    val gr = size.minDimension * (0.80f + 0.10f * heroBreath.value)
                    drawCircle(
                        Brush.radialGradient(listOf(heroAccent.copy(alpha = 0.32f + 0.14f * heroBreath.value), Color.Transparent), center = center, radius = gr),
                        gr, center
                    )
                }
            }
            .clickable(interactionSource = playInteraction, indication = null) {
                playHaptic()
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    rotationZ = rotation
                    val s = 1f + 0.03f * heroBreath.value
                    scaleX = s; scaleY = s
                }
                .clip(organic)
                .background(auraBrush)
                .liquidGlassDecor(organic, LensLook, heroAccent, paintFill = false)
        )
        AnimatedContent(
            targetState = when { isEnded -> 0; isPlaying -> 1; else -> 2 },
            transitionSpec = {
                (fadeIn(tween(160)) + scaleIn(initialScale = 0.6f, animationSpec = spring(0.6f, 600f)))
                    .togetherWith(fadeOut(tween(100)) + scaleOut(targetScale = 0.6f, animationSpec = tween(100)))
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
                tint = Color.White,
                modifier = Modifier.size(27.dp)
            )
        }
    }
}

// Shared header used by every player dialog/side-panel


@Composable
fun TrackSelectionItemExpressive(label: String, isSelected: Boolean, onClick: () -> Unit) {
    val accent = LocalAppUiSettings.current.accent
    val palette = LocalAppUiSettings.current.palette
    GlassRow(
        onClick = onClick,
        selected = isSelected,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = if(isSelected) Color.White else palette.textSecondary,
                fontWeight = if(isSelected) FontWeight.Bold else FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = accent,
                    unselectedColor = palette.outlineMedium
                )
            )
        }
    }
}

@Composable
fun BrowserVideoRowExpressive(video: VideoFile, isSelected: Boolean, onClick: () -> Unit) {
    val context = LocalContext.current
    val accent = LocalAppUiSettings.current.accent
    GlassRow(
        onClick = onClick,
        selected = isSelected,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp, 60.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(LocalAppUiSettings.current.palette.surfaceRaisedAlt)
            ) {
                if (LocalAppUiSettings.current.hideThumbnails) {
                    TriangleThumbPlaceholder(modifier = Modifier.fillMaxSize())
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
                        modifier = Modifier.fillMaxSize().background(accent.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        BrandMark(size = 34.dp)
                    }
                }
                Box(modifier = Modifier.align(Alignment.BottomEnd).padding(6.dp)) {
                    DurationBadge(text = formatDuration(video.duration))
                }
            }
            Spacer(modifier = Modifier.width(18.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = formatFileName(video.name),
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.ExtraBold),
                    color = if (isSelected) Color.White else LocalAppUiSettings.current.palette.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = formatSize(video.size),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) Color.White.copy(alpha = 0.7f) else LocalAppUiSettings.current.palette.textTertiary
                    )
                    if (video.width > 0) {
                        QualityBadge(width = video.width, height = video.height)
                    }
                }
            }
        }
    }
}


@Composable
private fun TrackSelectionItemCompact(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    radioColors: RadioButtonColors
) {
    val accent = LocalAppUiSettings.current.accent
    GlassRow(
        onClick = onClick,
        selected = isSelected,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Premium Custom Radio Indicator
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) accent.copy(alpha = 0.2f) else Color.Transparent)
                    .border(
                        1.5.dp,
                        if (isSelected) accent else Color.White.copy(alpha = 0.2f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(Color.White, CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isSelected) Color.White else LocalAppUiSettings.current.palette.textSecondary,
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (isSelected) {
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    painter = painterResource(R.drawable.ic_check_custom),
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun SubtitleOptionRowCompact(
    icon: Any,
    label: String,
    onClick: () -> Unit
) {
    GlassSurface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White.copy(alpha = 0.05f)),
                contentAlignment = Alignment.Center
            ) {
                when (icon) {
                    is ImageVector -> Icon(icon, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    is Painter -> Icon(icon, null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                label,
                style = MaterialTheme.typography.titleSmall,
                color = LocalAppUiSettings.current.palette.textPrimary,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                painterResource(R.drawable.ic_chevron_right_custom),
                null,
                tint = LocalAppUiSettings.current.palette.textTertiary.copy(alpha = 0.5f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@UnstableApi
@Composable
fun PlaybackSpeedSidePanel(
    visible: Boolean,
    currentSpeed: Float,
    onSpeedSelected: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    val accent = LocalAppUiSettings.current.accent
    PlayerPanel(visible = visible, onDismiss = onDismiss, title = "Playback speed") {
        PanelSection("Current") {
            PanelCard {
                Column(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp)) {
                    Text(
                        speedLabel(currentSpeed), color = Color.White,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold, fontFeatureSettings = "tnum")
                    )
                    PremiumSlider(
                        onDark = true,
                        value = currentSpeed,
                        onValueChange = { onSpeedSelected((it * 20f).roundToInt() / 20f) },
                        valueRange = 0.25f..3f
                    )
                }
            }
        }
        PanelSection("Presets") {
            PanelCard {
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 10.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(0.5f, 0.75f, 1f, 1.25f, 1.5f, 2f, 3f).forEach { s ->
                        MoreSpeedChip(speedLabel(s), abs(currentSpeed - s) < 0.01f) { onSpeedSelected(s) }
                    }
                }
                PanelDivider()
                PanelNavRow(painterResource(R.drawable.ic_stopwatch_custom), "Reset to 1x", showChevron = false) { onSpeedSelected(1.0f) }
            }
        }
    }
}

/** Label of the currently selected track of [type] (for the value shown on a settings row). */
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

/** Audio / subtitle chooser: one long selectable card per track, in the shared player panel. */
@UnstableApi
@Composable
fun TrackSelectionPanel(
    visible: Boolean,
    player: ExoPlayer,
    trackType: Int,
    onDismiss: () -> Unit,
    onAddLocalSubtitle: () -> Unit = {},
    onSearchOnline: (() -> Unit)? = null,
    onCustomize: (() -> Unit)? = null
) {
    val isAudio = trackType == C.TRACK_TYPE_AUDIO
    PlayerPanel(visible = visible, onDismiss = onDismiss, title = if (isAudio) "Audio track" else "Subtitles") {
        val groups = player.currentTracks.groups.filter { it.type == trackType }
        val textOff = player.trackSelectionParameters.disabledTrackTypes.contains(C.TRACK_TYPE_TEXT)
        PanelSection(if (isAudio) "Available tracks" else "Subtitle track") {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (!isAudio) {
                    PanelOptionRow("Off", "No subtitles", selected = textOff) {
                        player.trackSelectionParameters = player.trackSelectionParameters.buildUpon()
                            .setDisabledTrackTypes(setOf(C.TRACK_TYPE_TEXT)).build()
                        onDismiss()
                    }
                }
                if (groups.isEmpty() && isAudio) {
                    Text("No audio tracks found", color = Color.White.copy(alpha = 0.55f), modifier = Modifier.padding(start = 4.dp))
                }
                groups.forEach { group ->
                    for (i in 0 until group.length) {
                        val track = group.getTrackFormat(i)
                        val selected = group.isTrackSelected(i) && (isAudio || !textOff)
                        val sub = listOfNotNull(
                            track.language?.uppercase(),
                            if (isAudio && track.channelCount > 0) "${track.channelCount}ch" else null
                        ).joinToString(" · ").ifEmpty { null }
                        PanelOptionRow(track.label ?: (if (isAudio) "Track ${i + 1}" else "Subtitle ${i + 1}"), sub, selected) {
                            player.trackSelectionParameters = player.trackSelectionParameters.buildUpon()
                                .apply { if (!isAudio) setDisabledTrackTypes(emptySet()) }
                                .setOverrideForType(TrackSelectionOverride(group.mediaTrackGroup, i))
                                .build()
                            onDismiss()
                        }
                    }
                }
            }
        }
        if (!isAudio) {
            PanelSection("More") {
                PanelCard {
                    PanelNavRow(painterResource(R.drawable.ic_plus_custom), "Add local subtitle") { onDismiss(); onAddLocalSubtitle() }
                    if (onSearchOnline != null) {
                        PanelDivider()
                        PanelNavRow(painterResource(R.drawable.ic_search_custom), "Search online") { onDismiss(); onSearchOnline() }
                    }
                    if (onCustomize != null) {
                        PanelDivider()
                        PanelNavRow(painterResource(R.drawable.ic_subtitles_custom), "Appearance") { onDismiss(); onCustomize() }
                    }
                }
            }
        }
    }
}


@Composable
private fun SubtitleOptionRow(
    icon: Any,
    label: String,
    onClick: () -> Unit
) {
    GlassSurface(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(BrandBlue.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                when (icon) {
                    is ImageVector -> Icon(icon, null, tint = BrandBlue, modifier = Modifier.size(18.dp))
                    is Painter -> Icon(icon, null, tint = BrandBlue, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                label,
                style = MaterialTheme.typography.bodyLarge,
                color = LocalAppUiSettings.current.palette.textPrimary
            )
        }
    }
}

@Composable
private fun TrackSelectionItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    radioColors: RadioButtonColors
) {
    GlassRow(
        onClick = onClick,
        selected = isSelected,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(selected = isSelected, onClick = null, colors = radioColors)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isSelected) BrandBlue else LocalAppUiSettings.current.palette.textPrimary,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
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

// ── Resume playback position ────────────────────────────────────────────────
// A video that's barely started isn't worth resuming into, and one that's
// basically over should play back from the top next time rather than
// seeking straight to (or past) the end -- so both cases clear any stored
// value instead of writing one.
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

            // Allow drawing in the notch/cutout area
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                window.attributes.layoutInDisplayCutoutMode =
                    android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        } else {
            it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            WindowCompat.setDecorFitsSystemWindows(window, true)
            controller.show(WindowInsetsCompat.Type.systemBars())

            // Fix visibility in light mode: ensure icons are dark if the theme is light
            controller.isAppearanceLightStatusBars = !isDarkTheme
            controller.isAppearanceLightNavigationBars = !isDarkTheme

            // Restore default cutout mode
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
        id = System.currentTimeMillis(), // Temporary ID
        name = name,
        path = path,
        duration = duration,
        size = size,
        uri = uri,
        width = width,
        height = height
    )
}

// --- Helpers & Data Fetching ---

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
        null
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

@UnstableApi
@Composable
fun SubtitleCustomizationDialog(onDismiss: () -> Unit) {
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

    PremiumDialog(
        onDismiss = { savePrefs(); onDismiss() },
        title = "Subtitle Appearance",
        modifier = Modifier.fillMaxWidth(if (isLandscape) 0.6f else 0.95f)
    ) {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Preview Card
            GlassSurface(
                modifier = Modifier.fillMaxWidth().height(120.dp),
                shape = RoundedCornerShape(24.dp),
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

            // Controls
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Font Size
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Font Size", style = MaterialTheme.typography.labelLarge, color = LocalAppUiSettings.current.palette.textSecondary)
                        Text("${fontSize.toInt()} sp", style = MaterialTheme.typography.labelMedium, color = Color.White)
                    }
                    PremiumSlider(
                        onDark = true,
                        value = fontSize,
                        onValueChange = { fontSize = it },
                        valueRange = 12f..32f
                    )
                }

                // Opacity
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Background Opacity", style = MaterialTheme.typography.labelLarge, color = LocalAppUiSettings.current.palette.textSecondary)
                        Text("${(bgAlpha * 100).toInt()}%", style = MaterialTheme.typography.labelMedium, color = Color.White)
                    }
                    PremiumSlider(
                        onDark = true,
                        value = bgAlpha,
                        onValueChange = { bgAlpha = it },
                        valueRange = 0f..1f
                    )
                }

                // Colors
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
                                        if (isSelected) Color.White else Color.Transparent,
                                        CircleShape
                                    )
                                    .aliveClickable { textColor = colorInt },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(painterResource(R.drawable.ic_check_custom), null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }

                // Bold
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Bold Style", style = MaterialTheme.typography.labelLarge, color = LocalAppUiSettings.current.palette.textSecondary)

                    PremiumSwitch(checked = boldEnabled, onCheckedChange = { boldEnabled = it }, compact = true)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            PremiumButton(
                text = "Save Settings",
                onClick = { savePrefs(); onDismiss() },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@UnstableApi
@Composable
fun OnlineSubtitleSearchDialog(
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
    // Tracks which result row is currently resolving its download link, so we
    // can show a spinner on that one item and block double-taps.
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
                // OpenSubtitles REST API v1 (api.opensubtitles.com). The old
                // rest.opensubtitles.org search endpoint was shut down —
                // this is the current, maintained replacement and requires
                // an API key registered as a "consumer" on opensubtitles.com.
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

                // Best matches first
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

    // Resolves a subtitle file_id to a real, directly-downloadable .srt URL.
    // OpenSubtitles v1 requires this second call — search results only give
    // you a file_id, not a download link — and each link is single-use /
    // short-lived, so it must be fetched right before playback.
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

    // Shared handler used by both the row tap and the download icon tap.
    fun selectSubtitle(sub: SubtitleResult) {
        if (resolvingFileId != null) return // already resolving one, ignore taps
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

    PremiumDialog(
        onDismiss = onDismiss,
        title = "Cloud Subtitles",
        modifier = Modifier.fillMaxWidth(if (isLandscape) 0.6f else 0.95f)
    ) {
        Column {
            // Search Bar
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
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = LocalAppUiSettings.current.palette.outlineMedium,
                        focusedContainerColor = glassFieldFill(),
                        unfocusedContainerColor = glassFieldFill(),
                        cursorColor = Color.White,
                        focusedTextColor = LocalAppUiSettings.current.palette.textPrimary,
                        unfocusedTextColor = LocalAppUiSettings.current.palette.textPrimary
                    ),
                    shape = RoundedCornerShape(14.dp)
                )

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isSearching) LocalAppUiSettings.current.palette.surfaceRaisedAlt else Color.White.copy(alpha = 0.08f))
                        .clickable(enabled = !isSearching) { doSearch() },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSearching) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Icon(painterResource(R.drawable.ic_search_custom), null, tint = Color.White, modifier = Modifier.size(22.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(if (isLandscape) 12.dp else 20.dp))
            HorizontalDivider(color = LocalAppUiSettings.current.palette.outlineFaint)

            // Results
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
                                PremiumCardSurface(
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
                                                .background(Color.White.copy(alpha = 0.08f)),
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
                                                fontWeight = FontWeight.Bold
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
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                                Text("★ ${sub.rating}", color = LocalAppUiSettings.current.palette.textSecondary, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                                Text("${sub.downloads} DLs", color = LocalAppUiSettings.current.palette.textTertiary, style = MaterialTheme.typography.labelSmall)
                                            }
                                        }

                                        if (isResolvingThis) {
                                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = accent, strokeWidth = 2.dp)
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
            PremiumButton(
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
fun PremiumHUD(
    video: VideoFile,
    videoSize: VideoSize?,
    exoPlayer: ExoPlayer,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isLandscape =
        configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    // --- Reactive format state ---
    var liveVideoFormat by remember { mutableStateOf(exoPlayer.videoFormat) }

    var retrieverBitrateKbps by remember { mutableIntStateOf(0) }

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onVideoSizeChanged(size: VideoSize) {
                // Format is guaranteed to be non-null once video size is known
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

    // Launch the retriever fallback in the background whenever the video changes
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

    PlayerPanel(visible = true, onDismiss = onDismiss, title = "", scrollable = false) {
        Box(Modifier.fillMaxWidth().weight(1f, fill = false)) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "CoverPlay Intelligence",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = BrandMagenta,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp
                            )
                        )
                        Text(
                            "MEDIA ENGINE HUD",
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        QuipLine("Nerd stats. Because you asked nicely.", color = Color.White.copy(alpha = 0.45f))
                    }

                    PremiumDialogCloseButton(onClick = onDismiss)
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.12f))

                // Metadata Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.heightIn(max = 400.dp)
                ) {
                    item { HUDInfoItem("Resolution", videoSize?.let { "${it.width} × ${it.height}" } ?: "Detecting...") }
                    item { HUDInfoItem("Format", video.path.substringAfterLast(".").uppercase()) }
                    item { HUDInfoItem("File Size", formatSize(video.size)) }

                    val fmt = liveVideoFormat
                    if (fmt != null) {
                        item { HUDInfoItem("Codec", fmt.sampleMimeType?.substringAfterLast("/")?.uppercase() ?: "Unknown") }

                        if (fmt.frameRate > 0f) {
                            val fpsText = if (fmt.frameRate % 1f == 0f) "${fmt.frameRate.toInt()} FPS"
                            else String.format("%.3f FPS", fmt.frameRate)
                            item { HUDInfoItem("Frame Rate", fpsText) }
                        }

                        val fmtBitrateKbps = if (fmt.bitrate > 0) fmt.bitrate / 1000 else 0
                        val bitrateKbps = when {
                            fmtBitrateKbps > 0 -> fmtBitrateKbps
                            retrieverBitrateKbps > 0 -> retrieverBitrateKbps
                            video.duration > 0L -> ((video.size * 8L) / video.duration).toInt()
                            else -> 0
                        }
                        if (bitrateKbps > 0) {
                            val bitrateText = if (bitrateKbps >= 1000) String.format("%.2f Mbps", bitrateKbps / 1000f)
                            else "$bitrateKbps kbps"
                            item { HUDInfoItem("Bitrate", bitrateText) }
                        } else {
                            item { HUDInfoItem("Bitrate", "Calculating...") }
                        }
                    } else {
                        item { HUDInfoItem("Codec", "Detecting...") }
                        item { HUDInfoItem("Frame Rate", "Detecting...") }
                        item { HUDInfoItem("Bitrate", "Detecting...") }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Status Badges
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (videoSize != null && videoSize.width >= 3840) {
                        HUDChip("4K ULTRA HD", Color.White)
                    } else if (videoSize != null && videoSize.width >= 1920) {
                        HUDChip("FULL HD", Color.White.copy(alpha = 0.8f))
                    }

                    if (video.duration > 3600000) {
                        HUDChip("FEATURE", Color.White.copy(alpha = 0.7f))
                    }
                }
            }
        }
    }
}

@Composable
private fun HUDInfoItem(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            label.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            ),
            color = LocalAppUiSettings.current.palette.textTertiary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = LocalAppUiSettings.current.palette.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun HUDChip(text: String, color: Color) {
    Box(modifier = Modifier.glassBadge(color.copy(alpha = 0.18f), color, RoundedCornerShape(8.dp))) {
        Text(
            text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            fontWeight = FontWeight.Bold
        )
    }
}