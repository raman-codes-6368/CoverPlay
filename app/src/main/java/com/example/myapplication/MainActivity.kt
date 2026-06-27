package com.example.myapplication

import android.content.res.Configuration
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.derivedStateOf
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentUris
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.media3.datasource.okhttp.OkHttpDataSource
import okhttp3.OkHttpClient
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.*
import android.content.Intent
import androidx.media3.common.MimeTypes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.VideoFrameDecoder
import coil.request.ImageRequest
import coil.request.videoFrameMillis
import com.example.myapplication.ui.theme.BrandBlue
import com.example.myapplication.ui.theme.BrandBlueDeep
import com.example.myapplication.ui.theme.BrandBlueShadow
import com.example.myapplication.ui.theme.BrandHighlight
import com.example.myapplication.ui.theme.BrandIceBlue
import com.example.myapplication.ui.theme.BrandCyan
import com.example.myapplication.ui.theme.BrandGradient
import com.example.myapplication.ui.theme.BrandGradientSoft
import com.example.myapplication.ui.theme.BrandTitleText
import com.example.myapplication.ui.theme.CinematicBackground
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.theme.OutlineFaint
import com.example.myapplication.ui.theme.OutlineMedium
import com.example.myapplication.ui.theme.SurfaceNavy
import com.example.myapplication.ui.theme.SurfaceRaised
import com.example.myapplication.ui.theme.SurfaceRaisedAlt
import com.example.myapplication.ui.theme.TextPrimary
import com.example.myapplication.ui.theme.TextSecondary
import com.example.myapplication.ui.theme.TextTertiary
import io.github.anilbeesetti.nextlib.media3ext.ffdecoder.NextRenderersFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import kotlin.math.log10
import kotlin.math.pow

private const val RECENTS_PREFS = "recent_videos"
private const val RECENTS_KEY = "items"
private const val RECENTS_MAX_ITEMS = 20
private const val RECENTS_KEEP_DAYS = 7L
private const val RECENTS_KEEP_MS = RECENTS_KEEP_DAYS * 24L * 60L * 60L * 1000L

// --- Data Models ---

fun formatFileName(name: String): String {
    // 1. Remove extension
    val nameWithoutExt = name.substringBeforeLast(".")
    
    // 2. Common release tags regex
    val tags = listOf(
        "1080p", "720p", "480p", "2160p", "4k", "uhd", "bluray", "brrip", "webrip", "h264", "x264", "h265", "x265", 
        "aac", "dts", "dd5.1", "dual.audio", "hindi", "english", "esub", "subbed", "camrip", "hdtv"
    )
    
    var cleaned = nameWithoutExt.replace(".", " ")
    
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
    val height: Int = 0
)


enum class Screen {
    HOME, RECENTS, PLAYLISTS, SETTINGS, FOLDER_VIDEOS, PLAYER
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


@Composable
fun BrandTriangle(modifier: Modifier = Modifier, contentDescription: String? = "Play") {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val topPath = Path().apply {
            moveTo(w * 0.20f, h * 0.06f)
            cubicTo(w * 0.20f, 0f, w * 0.29f, -h * 0.02f, w * 0.34f, h * 0.02f)
            lineTo(w * 0.92f, h * 0.40f)
            cubicTo(w * 0.99f, h * 0.45f, w * 0.99f, h * 0.55f, w * 0.92f, h * 0.60f)
            lineTo(w * 0.34f, h * 0.98f)
            cubicTo(w * 0.29f, h * 1.02f, w * 0.20f, h * 1.00f, w * 0.20f, h * 0.94f)
            close()
        }
        val bottomPath = Path().apply {
            moveTo(w * 0.20f, h * 0.50f)
            lineTo(w * 0.62f, h * 0.78f)
            lineTo(w * 0.20f, h * 0.94f)
            close()
        }
        drawPath(
            path = topPath,
            brush = Brush.linearGradient(
                colors = listOf(BrandHighlight, BrandCyan, BrandBlue),
                start = Offset(0f, 0f),
                end = Offset(w, h)
            )
        )
        drawPath(
            path = bottomPath,
            brush = Brush.linearGradient(
                colors = listOf(BrandBlueDeep, BrandBlueShadow),
                start = Offset(w * 0.2f, h * 0.5f),
                end = Offset(w * 0.62f, h * 0.94f)
            ),
            alpha = 0.95f
        )
    }
}

@Composable
fun TriangleThumbPlaceholder(modifier: Modifier = Modifier, triangleFraction: Float = 0.32f) {
    Box(
        modifier = modifier.background(SurfaceRaisedAlt),
        contentAlignment = Alignment.Center
    ) {
        BrandTriangle(
            modifier = Modifier.fillMaxSize(triangleFraction),
        )
    }
}


class MainActivity : ComponentActivity() {
    @UnstableApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainApp()
            }
        }
    }
}

@UnstableApi
@Composable
fun MainApp() {
    val context = LocalContext.current
    var showSplash by remember { mutableStateOf(true) }

    AnimatedContent(
        targetState = showSplash,
        transitionSpec = {
            fadeIn(animationSpec = tween(1000)) togetherWith fadeOut(animationSpec = tween(600))
        },
        label = "SplashTransition"
    ) { isSplash ->
        if (isSplash) {
            SplashScreen(onAnimationFinish = { showSplash = false })
        } else {
            MainContent()
        }
    }
}

@Composable
fun SplashScreen(onAnimationFinish: () -> Unit) {
    var startAnimation by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (startAnimation) 1.2f else 0.8f,
        animationSpec = tween(durationMillis = 1500, delayMillis = 100),
        label = "LogoScale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1200, delayMillis = 200),
        label = "LogoAlpha"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(2500)
        onAnimationFinish()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CinematicBackground(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .size(420.dp)
                    .align(Alignment.Center)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                BrandCyan.copy(alpha = 0.14f),
                                BrandBlueDeep.copy(alpha = 0.10f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            BrandTriangle(
                modifier = Modifier
                    .size(148.dp)
                    .graphicsLayer(scaleX = scale, scaleY = scale, alpha = alpha)
            )
            Spacer(modifier = Modifier.height(28.dp))
            BrandTitleText(
                text = "CoverPlay",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontSize = 38.sp,
                    letterSpacing = 5.sp
                ),
                modifier = Modifier.graphicsLayer(alpha = alpha)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "CINEMATIC PLAYBACK",
                style = MaterialTheme.typography.splashTagline.copy(
                    color = TextTertiary.copy(alpha = alpha)
                ),
                modifier = Modifier.graphicsLayer(alpha = alpha * 0.85f)
            )
        }
    }
}

@UnstableApi
@Composable
fun MainContent() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("coverplay_settings", android.content.Context.MODE_PRIVATE) }
    var settings by remember { mutableStateOf(readSettings(context)) }
    
    DisposableEffect(prefs) {
        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
            settings = readSettings(context)
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components {
                add(VideoThumbnailFetcher.Factory(context))
                add(VideoFrameDecoder.Factory())
            }
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

        LaunchedEffect(Unit) {
            recentEntries.clear()
            recentEntries.addAll(loadRecentEntries(context))
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

        LaunchedEffect(Unit) {
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
                withContext(Dispatchers.IO) {
                    fetchVideoFolders(context, folders)
                    fetchAllVideos(context, allVideos)
                }
            } else {
                permissionLauncher.launch(permissions)
            }
        }

        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                if (currentScreen != Screen.PLAYER) {
                    BottomNavigationBar(
                        currentScreen = currentScreen,
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
        ) { innerPadding ->
            CinematicBackground {
                Box(modifier = Modifier.padding(if (currentScreen == Screen.PLAYER) PaddingValues(0.dp) else innerPadding)) {
                    AnimatedContent(
                        targetState = currentScreen,
                        transitionSpec = {
                            if (targetState == Screen.PLAYER || initialState == Screen.PLAYER) {
                                // High-performance cinematic transition for the player.
                                // No bounce and a higher stiffness so this settles quickly
                                // and doesn't keep animating frames while ExoPlayer/PlayerView
                                // are doing their (synchronous, main-thread) setup work --
                                // a low-stiffness bouncy spring here was visibly competing
                                // with that init cost and showing up as a stutter.
                                val springSpec = spring<Float>(
                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                )
                                val tweenSpec = tween<IntOffset>(
                                    durationMillis = 500,
                                    easing = FastOutSlowInEasing
                                )
                                val fadeSpec = tween<Float>(durationMillis = 400)

                                if (targetState == Screen.PLAYER) {
                                    // Slide UP from bottom + Scale UP + Fade IN
                                    (slideInVertically(
                                        initialOffsetY = { it },
                                        animationSpec = tweenSpec


                                    ) +
                                            scaleIn(
                                                initialScale = 0.95f,
                                                animationSpec = springSpec
                                            ) +
                                            fadeIn(animationSpec = fadeSpec)) togetherWith
                                            (scaleOut(
                                                targetScale = 0.98f,
                                                animationSpec = fadeSpec
                                            ) +
                                                    fadeOut(animationSpec = fadeSpec))
                                } else {
                                    // Slide DOWN to bottom + Scale DOWN + Fade OUT
                                    fadeIn(animationSpec = fadeSpec) togetherWith
                                            (slideOutVertically(
                                                targetOffsetY = { it },
                                                animationSpec = tweenSpec
                                            ) +
                                                    scaleOut(
                                                        targetScale = 0.95f,
                                                        animationSpec = springSpec
                                                    ) +
                                                    fadeOut(animationSpec = fadeSpec))
                                }
                            } else {
                                // Regular tab switches use a smooth fade
                                fadeIn(animationSpec = tween(400)) togetherWith fadeOut(
                                    animationSpec = tween(400)
                                )
                            }
                        },
                        label = "ScreenTransition"
                    ) { screen ->
                        when (screen) {
                            Screen.HOME -> {
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
                                        previousScreen = Screen.HOME
                                        currentScreen = Screen.FOLDER_VIDEOS
                                        // Fire-and-forget on IO: the screen transition starts
                                        // immediately, the list fills in as soon as the query
                                        // returns instead of freezing the tap until it's done.
                                        scope.launch(Dispatchers.IO) {
                                            fetchVideosInFolder(context, folder.path, folderVideos)
                                        }
                                    },
                                    onVideoClick = { video ->
                                        selectedVideo = video
                                        addToRecents(context, video, recentEntries)
                                        isSearchActive = false
                                        searchQuery = ""
                                        previousScreen = Screen.HOME
                                        // Load folder peers so the in-player browser works
                                        scope.launch(Dispatchers.IO) {
                                            val folder = java.io.File(video.path).parent ?: ""
                                            if (folder.isNotEmpty()) fetchVideosInFolder(context, folder, folderVideos)
                                        }
                                        currentScreen = Screen.PLAYER
                                    },
                                    // Rename/delete/move all complete before this fires,
                                    // so a fresh MediaStore query here picks up the change
                                    // immediately -- folders (counts can shift after a
                                    // move) and allVideos (search results, names) both
                                    // need to reflect it.
                                    onVideoChanged = {
                                        scope.launch(Dispatchers.IO) {
                                            fetchVideoFolders(context, folders)
                                            fetchAllVideos(context, allVideos)
                                        }
                                    },
                                    onPlayUrl = { url ->
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

                            Screen.FOLDER_VIDEOS -> {
                                BackHandler {
                                    isSearchActive = false
                                    searchQuery = ""
                                    currentScreen = Screen.HOME
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
                                        settings = settings
                                    )
                                }
                            }

                            Screen.RECENTS -> {
                                BackHandler { currentScreen = Screen.HOME }
                                RecentsScreen(
                                    entries = recentEntries,
                                    isSearchActive = isSearchActive,
                                    searchQuery = searchQuery,
                                    onSearchQueryChange = { searchQuery = it },
                                    onToggleSearch = {
                                        isSearchActive = !isSearchActive
                                        if (!isSearchActive) searchQuery = ""
                                    },
                                    onVideoClick = { video ->
                                        selectedVideo = video
                                        addToRecents(context, video, recentEntries)
                                        previousScreen = Screen.RECENTS
                                        isSearchActive = false
                                        searchQuery = ""
                                        scope.launch(Dispatchers.IO) {
                                            val folder = java.io.File(video.path).parent ?: ""
                                            if (folder.isNotEmpty()) fetchVideosInFolder(context, folder, folderVideos)
                                        }
                                        currentScreen = Screen.PLAYER
                                    },
                                    onRemoveEntry = { id ->
                                        removeFromRecents(context, id, recentEntries)
                                    },
                                    onClearDay = { ids ->
                                        removeMultipleFromRecents(context, ids, recentEntries)
                                    }
                                )
                            }

                            Screen.PLAYLISTS -> {
                                BackHandler { currentScreen = Screen.HOME }
                                PlaylistsScreen(
                                    allVideos = allVideos,
                                    onVideoClick = { video ->
                                        selectedVideo = video
                                        addToRecents(context, video, recentEntries)
                                        previousScreen = Screen.PLAYLISTS
                                        scope.launch(Dispatchers.IO) {
                                            val folder = java.io.File(video.path).parent ?: ""
                                            if (folder.isNotEmpty()) fetchVideosInFolder(context, folder, folderVideos)
                                        }
                                        currentScreen = Screen.PLAYER
                                    }
                                )
                            }

                            Screen.SETTINGS -> {
                                BackHandler { currentScreen = Screen.HOME }
                                SettingsScreen(
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
    val cutoff = System.currentTimeMillis() - RECENTS_KEEP_MS
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

@Composable
fun PremiumSeekbar(
    position: Long,
    duration: Long,
    bufferedPosition: Long = 0L,           // optional: gray "buffered" segment
    onValueChange: (Float) -> Unit,        // called during drag with raw ms float
    onValueChangeFinished: () -> Unit,     // called on finger-up
    onSeek: (Long) -> Unit = {},           // NEW: live seek while dragging
    onDragStart: () -> Unit = {},          // NEW: finger down — hide other controls
    onDragEnd: () -> Unit = {},            // NEW: finger up — show other controls
    modifier: Modifier = Modifier
) {
    val fraction =
        if (duration > 0) (position.toFloat() / duration.toFloat()).coerceIn(0f, 1f) else 0f
    val bufferedFraction =
        if (duration > 0) (bufferedPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f) else 0f

    var isDragging by remember { mutableStateOf(false) }
    var trackWidthPx by remember { mutableIntStateOf(1) }

    // Animated track height and thumb scale
    val trackHeight by animateDpAsState(
        targetValue = if (isDragging) 6.dp else 4.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "trackH"
    )
    val thumbScale by animateFloatAsState(
        targetValue = if (isDragging) 1.35f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "thumbScale"
    )

    // Touch-target area: 44 dp tall, track drawn in centre
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .onSizeChanged { trackWidthPx = it.width }
            .pointerInput(duration) {
                awaitEachGesture {
                    // Wait for the initial finger-down
                    val down = awaitFirstDown(requireUnconsumed = false)
                    down.consume()

                    isDragging = true
                    onDragStart()

                    // Seek to the tapped position immediately
                    val tapFraction = (down.position.x / trackWidthPx).coerceIn(0f, 1f)
                    val tapMs = (tapFraction * duration).toLong()
                    onValueChange(tapMs.toFloat())
                    onSeek(tapMs)

                    // Track subsequent pointer events
                    var lastSeekMs = tapMs
                    do {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull() ?: break
                        if (change.pressed) {
                            change.consume()
                            val dragFraction = (change.position.x / trackWidthPx).coerceIn(0f, 1f)
                            val dragMs = (dragFraction * duration).toLong()
                            onValueChange(dragMs.toFloat())
                            // Throttle live seek to ~60 ms to avoid flooding ExoPlayer
                            if (kotlin.math.abs(dragMs - lastSeekMs) > 60L) {
                                onSeek(dragMs)
                                lastSeekMs = dragMs
                            }
                        } else {
                            change.consume()
                            // Finger lifted — commit the final position
                            val finalFraction = (change.position.x / trackWidthPx).coerceIn(0f, 1f)
                            val finalMs = (finalFraction * duration).toLong()
                            onValueChange(finalMs.toFloat())
                            onSeek(finalMs)
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
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(trackHeight)
        ) {
            val trackH = trackHeight.toPx()
            val cornerR = CornerRadius(trackH / 2, trackH / 2)
            val totalW = size.width
            val centerY = size.height / 2f

            // ── Background track ─────────────────────────────────────
            drawRoundRect(
                color = Color.White.copy(alpha = 0.15f),
                topLeft = Offset(0f, centerY - trackH / 2),
                size = Size(totalW, trackH),
                cornerRadius = cornerR
            )

            // ── Buffered ghost track ──────────────────────────────────
            if (bufferedFraction > fraction) {
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.22f),
                    topLeft = Offset(0f, centerY - trackH / 2),
                    size = Size(totalW * bufferedFraction, trackH),
                    cornerRadius = cornerR
                )
            }

            // ── Active / played track — brand gradient ────────────────
            if (fraction > 0f) {
                val activeW = totalW * fraction
                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(BrandBlueDeep, BrandIceBlue, BrandCyan, BrandHighlight),
                        startX = 0f,
                        endX = totalW          // gradient spans full width so colour is consistent
                    ),
                    topLeft = Offset(0f, centerY - trackH / 2),
                    size = Size(activeW, trackH),
                    cornerRadius = cornerR
                )
            }

            // ── Thumb ─────────────────────────────────────────────────
            val thumbX = (totalW * fraction).coerceIn(0f, totalW)
            val thumbBaseRadius = 7.dp.toPx()
            val thumbR = thumbBaseRadius * thumbScale

            // Glow halo (only while dragging)
            if (isDragging) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            BrandCyan.copy(alpha = 0.35f),
                            BrandCyan.copy(alpha = 0.10f),
                            Color.Transparent
                        ),
                        center = Offset(thumbX, centerY),
                        radius = thumbR * 2.6f
                    ),
                    radius = thumbR * 2.6f,
                    center = Offset(thumbX, centerY)
                )
            }

            // Outer ring (thin brand border)
            drawCircle(
                brush = Brush.linearGradient(
                    colors = listOf(BrandHighlight, BrandCyan),
                    start = Offset(thumbX - thumbR, centerY - thumbR),
                    end = Offset(thumbX + thumbR, centerY + thumbR)
                ),
                radius = thumbR,
                center = Offset(thumbX, centerY)
            )

            // Inner fill — platinum white
            drawCircle(
                color = BrandHighlight,
                radius = thumbR * 0.65f,
                center = Offset(thumbX, centerY)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// CHANGES NEEDED in VideoPlayerScreen
// ─────────────────────────────────────────────────────────────────────────────
//
// 1.  LIVE SEEK while dragging  ─────────────────────────────────────────────
//
//     In the PremiumSeekbar call (around line 1592), add onSeek and the two
//     drag-lifecycle callbacks:
//
//         PremiumSeekbar(
//             position              = displayPos,
//             duration              = duration,
//             bufferedPosition      = exoPlayer.bufferedPosition,          // ← add
//             onValueChange         = { v ->
//                 isScrubbing = true
//                 scrubPositionMs = v.toLong()
//             },
//             onValueChangeFinished = {
//                 val total = exoPlayer.duration
//                     .takeUnless { it == C.TIME_UNSET } ?: duration
//                 val target = scrubPositionMs.coerceIn(0L, maxOf(0L, total))
//                 exoPlayer.seekTo(exoPlayer.currentMediaItemIndex, target)
//                 lastSeekTimeMs = System.currentTimeMillis()
//                 position   = target
//                 isScrubbing = false
//             },
//             onSeek = { ms ->                                              // ← add
//                 val total = exoPlayer.duration
//                     .takeUnless { it == C.TIME_UNSET } ?: duration
//                 val target = ms.coerceIn(0L, maxOf(0L, total))
//                 exoPlayer.seekTo(exoPlayer.currentMediaItemIndex, target)
//                 lastSeekTimeMs = System.currentTimeMillis()
//                 scrubPositionMs = target
//             },
//             onDragStart = { isScrubbing = true },                        // ← add
//             onDragEnd   = { isScrubbing = false },                       // ← add
//             modifier = Modifier.weight(1f)
//         )
//
//
// 2.  HIDE other controls while scrubbing  ──────────────────────────────────
//
//     The outer AnimatedVisibility gate is:
//         AnimatedVisibility(visible = showControls && !isControlsLocked) { ... }
//
//     Inside that block there are three child regions:
//       A.  Top bar Row (back + title + shuffle/repeat/bg-play icons)
//       B.  Centre Box (lock button, rotation button, play/pause, error)
//       C.  Bottom Column (audio/subtitle row + seekbar+timer row)
//
//     Wrap A and B in:
//         AnimatedVisibility(
//             visible = !isScrubbing,
//             enter = fadeIn(tween(200)),
//             exit  = fadeOut(tween(150))
//         ) { ... }
//
//     Inside C, wrap ONLY the "Audio/Subtitle and Speed/Resize controls row"
//     (the Row before the seekbar) in the same AnimatedVisibility.
//     The seekbar+time Row itself is NOT wrapped — it always shows.
//
//     Result: while dragging the seekbar, or while adjusting volume/
//     brightness via the swipe gesture, only the seekbar row (and the
//     gesture bar itself) stay visible. All other UI fades out smoothly.
//     (Implemented below: these AnimatedVisibility blocks are gated on
//     `!isScrubbing && !showVerticalBar`.)
//
//
// 3.  Keep showControls alive during scrub  ─────────────────────────────────
//
//     The existing LaunchedEffect auto-hides controls after 3 s:
//         LaunchedEffect(showControls) {
//             if (showControls) { delay(3000); showControls = false }
//         }
//
//     Change it to:
//         LaunchedEffect(showControls, isScrubbing) {
//             if (showControls && !isScrubbing) {
//                 delay(3000)
//                 showControls = false
//             }
//         }
//
//     Also ensure showControls = true when a drag starts (so the bar is
//     always visible even if controls were already hidden):
//         onDragStart = {
//             isScrubbing  = true
//             showControls = true        // ← keep the seekbar row on-screen
//         },


@UnstableApi
@Composable
fun SmoothPlayerScrubber(
    exoPlayer: androidx.media3.exoplayer.ExoPlayer,
    duration: Long,
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
                delay(500L) // UI refresh rate for time codes
            }
        }
    }

    PremiumSeekbar(
        position = internalPosition,
        duration = duration,
        bufferedPosition = exoPlayer.bufferedPosition,
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
            exoPlayer.seekTo(ms)
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

@UnstableApi
@Composable
fun VideoPlayerScreen(
    video: VideoFile,
    onBack: () -> Unit,
    folderVideos: List<VideoFile> = emptyList(),
    onPlayVideo: (VideoFile) -> Unit = {},
    settings: Map<String, Any> = emptyMap()
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
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

    // Handle back gesture specifically for this screen
    BackHandler {
        onBack()
    }
    val exoPlayer = remember {
        
        val renderersFactory = NextRenderersFactory(context).apply {
            setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON)
        }

        val trackSelector = DefaultTrackSelector(context)


        val okHttpClient = OkHttpClient()
        val httpDataSourceFactory = OkHttpDataSource.Factory(okHttpClient)
            .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36")

        // Wrap the HTTP data source in a DefaultDataSource.Factory to support local files (content://, file://)
        val dataSourceFactory = DefaultDataSource.Factory(context, httpDataSourceFactory)

        ExoPlayer.Builder(context)
            .setRenderersFactory(renderersFactory)
            .setTrackSelector(trackSelector)
            .setMediaSourceFactory(DefaultMediaSourceFactory(context).setDataSourceFactory(dataSourceFactory))
            .build()
            .apply {
                playWhenReady = true
                setPlaybackSpeed(defaultSpeed)
            }
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
                    .setMimeType(MimeTypes.APPLICATION_SUBRIP)
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

    // Update player when video changes
    LaunchedEffect(video.uri) {
        exoPlayer.setMediaItem(MediaItem.fromUri(video.uri))
        if (resumePlayback) {
            val savedPos = context.getSharedPreferences("playback_positions", Context.MODE_PRIVATE)
                .getLong(video.path, 0L)
            if (savedPos > 0) {
                exoPlayer.seekTo(savedPos)
            }
        }
        exoPlayer.prepare()
        exoPlayer.play()
    }

    var showSubtitleCustomization by remember { mutableStateOf(false) }
    var showOnlineSubtitleSearch by remember { mutableStateOf(false) }
    var showFolderVideos by remember { mutableStateOf(false) }
    var showHUD by remember { mutableStateOf(false) }

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
    LaunchedEffect(isForcedLandscape) {
        updateOrientation(context, true, isForcedLandscape)
    }

    // Restore orientation, system bars, and brightness when leaving the
    // player screen. Without this, the activity stays locked to whatever
    // orientation was forced, the system bars remain hidden, and any
    // manual brightness change from the swipe gesture leaks into the
    // rest of the app after navigating away.
    DisposableEffect(Unit) {
        onDispose {
            if (resumePlayback && exoPlayer.duration > 0) {
                context.getSharedPreferences("playback_positions", Context.MODE_PRIVATE)
                    .edit().putLong(video.path, exoPlayer.currentPosition).apply()
            }
            updateOrientation(context, isFullscreen = false)
            context.findActivity()?.let { activity ->
                val lp = activity.window.attributes
                lp.screenBrightness = originalBrightness
                activity.window.attributes = lp
            }
        }
    }
    var showControls by remember { mutableStateOf(true) }
    // Lock freezes the controls surface in place: gestures and taps no
    // longer toggle showControls or trigger seek/volume/brightness, and
    // only the lock pill itself remains tappable. Standard on MX
    // Player/VLC for watching without accidental touches (in a pocket,
    // handed to a kid, etc.) -- the rest of this screen's gesture/tap
    // handling below checks this flag before acting.
    var isControlsLocked by remember { mutableStateOf(false) }
    var showAudioSelector by remember { mutableStateOf(false) }
    var showSubtitleSelector by remember { mutableStateOf(false) }
    var showSpeedSelector by remember { mutableStateOf(false) }
    var currentSpeed by remember { mutableFloatStateOf(defaultSpeed) }
    var repeatMode by remember { mutableIntStateOf(Player.REPEAT_MODE_OFF) }
    var isShuffleEnabled by remember { mutableStateOf(false) }
    var playbackError by remember { mutableStateOf<String?>(null) }
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

    var isScrubbing by remember { mutableStateOf(false) }
    var isLongPressing by remember { mutableStateOf(false) }
    var scrubPositionMs by remember { mutableLongStateOf(0L) }

    var lastSeekTimeMs by remember { mutableLongStateOf(0L) }
    val seekSettleWindowMs = 750L

    // Auto-hide controls logic
    LaunchedEffect(showControls, isScrubbing, isPlaying, showAudioSelector, showSubtitleSelector, showSpeedSelector) {
        if (showControls && !isScrubbing && isPlaying && !showAudioSelector && !showSubtitleSelector && !showSpeedSelector) {
            delay(3500L)
            showControls = false
        }
    }

    LaunchedEffect(exoPlayer, isPlaying) {
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
    }

    var currentVideoSize by remember { mutableStateOf<VideoSize?>(null) }

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                Log.e("PLAYER_ERROR", "Playback failed (code=${error.errorCode}, name=${error.errorCodeName})", error)
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
                Log.d(
                    "PLAYER_DEBUG",
                    "state=$state duration=${exoPlayer.duration} " +
                            "seekable=${exoPlayer.isCurrentMediaItemSeekable} " +
                            "position=${exoPlayer.currentPosition}"
                )
                isBufferingRaw = state == Player.STATE_BUFFERING
                if (state == Player.STATE_READY) {
                    val d = exoPlayer.duration
                    if (d > 0) duration = d
                }
            }

            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
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
                    if (!isBackgroundPlayEnabled) {
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

    // Gesture tracking — stored by downTime (ms) so they survive Android's
    // MotionEvent recycling/pooling that makes reference-equality (===) unreliable.
    var gestureDownTime by remember { mutableLongStateOf(-1L) }
    var gestureStartY by remember { mutableFloatStateOf(0f) }   // e1.y at finger-down
    var gestureStartBrightness by remember { mutableFloatStateOf(0f) }
    var gestureStartVolumeValue by remember { mutableFloatStateOf(0f) }
    // Tracks the last committed 0.0–2.0 volume level across gestures.
    // ExoPlayer clamps its own .volume to 0–1, so we can't read boost back from it.
    var lastVolumeGestureValue by remember { mutableFloatStateOf(0f) }

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
                val spanDelta = kotlin.math.abs(detector.currentSpan - detector.previousSpan)
                val minSpanDeltaPx = 4f
                if (spanDelta < minSpanDeltaPx && !hasCycledThisGesture) {
                    return true
                }

                if (!hasCycledThisGesture) {
                    if (totalScale > 1.2f) {
                        if (resizeModeIndex < resizeModes.size - 1) {
                            resizeModeIndex++
                            gestureText = "Aspect Ratio: ${resizeModes[resizeModeIndex].second}"
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
                            gestureText = "Aspect Ratio: ${resizeModes[resizeModeIndex].second}"
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
                gestureText = "2x Speed"
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
                val totalDX = kotlin.math.abs(e2.x - e1.x)
                val totalDY = kotlin.math.abs(e2.y - e1.y)
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
                showControls = true

                if (e1.x < screenWidth / 2) {
                    if (!brightnessGestureEnabled) return false
                    // ── Brightness (left half) ────────────────────────────────
                    // Full screen height = 0→100% brightness (comfortable 1:1 feel)
                    val newBrightness =
                        (gestureStartBrightness + deltaY / screenHeight).coerceIn(0.01f, 1f)
                    val activity = context.findActivity()
                    activity?.let {
                        val lp = it.window.attributes
                        lp.screenBrightness = newBrightness
                        it.window.attributes = lp
                    }
                    gestureText = "Brightness"
                    isBarBrightness = true
                    gestureIcon = Icons.Default.LightMode
                    gestureValue = newBrightness

                } else {
                    if (!volumeGestureEnabled) return false
                    // ── Volume (right half) ───────────────────────────────────
                    // Full screen height = 0→200% (0.0→2.0). The 0.9 factor
                    // gives a slightly generous range — same feel as MX/VLC.
                    val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                    val newGestureValue =
                        (gestureStartVolumeValue + (deltaY / screenHeight) * 0.9f).coerceIn(0f, 2f)
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
                    gestureIcon = if (newGestureValue > 0f) Icons.AutoMirrored.Filled.VolumeUp
                    else Icons.AutoMirrored.Filled.VolumeOff
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
                    gestureText = "Zoom: Reset"
                    return true
                }

                val screenWidth =
                    context.resources.displayMetrics.widthPixels
                val total = exoPlayer.duration.takeUnless {
                    it ==
                            C.TIME_UNSET
                } ?: duration

                val side = if (e.x < screenWidth / 2) "left" else "right"
                if (seekAnimationSide != side) {
                    cumulativeSeekAmount = 10
                } else {
                    cumulativeSeekAmount += 10
                }
                seekAnimationSide = side
                seekAnimationToken++

                if (side == "left") {
                    val t = (exoPlayer.currentPosition - 10_000L)
                        .coerceAtLeast(0L)

                    exoPlayer.seekTo(
                        exoPlayer.currentMediaItemIndex,
                        t
                    )
                    lastSeekTimeMs = System.currentTimeMillis()

                    position = t
                } else {
                    val t = (exoPlayer.currentPosition + 10_000L)
                        .coerceAtMost(
                            if (total > 0) total
                            else exoPlayer.currentPosition + 10_000L
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

    // Reset the hide timer whenever the bar is shown OR the value changes (movement).
    // This ensures the bar stays visible as long as the user is actively scrolling
    // and only starts the 2-second countdown after the last movement.
    LaunchedEffect(showVerticalBar, gestureValue) {
        if (showVerticalBar) {
            delay(2000)
            showVerticalBar = false
        }
    }

    // Same logic for the center gesture text (e.g., "Aspect Ratio: Fit")
    LaunchedEffect(gestureText, isLongPressing) {
        if (gestureText.isNotEmpty() && !isLongPressing) {
            delay(2000)
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

    LaunchedEffect(showControls, isScrubbing, showVerticalBar) {
        if (showControls && !isScrubbing && !showVerticalBar) {
            delay(3000)
            showControls = false
        }
    }

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = videoZoomScale
                    scaleY = videoZoomScale
                    translationX = videoPanX
                    translationY = videoPanY
                },
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

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInteropFilter { motionEvent ->
                    if (isControlsLocked) {
                        // While locked, this full-screen layer sits above the
                        // unlock pill in Z-order and intercepts touches first.
                        //
                        // Two cases on ACTION_UP:
                        //  • Pill is hidden (!showControls): consume the tap
                        //    here to reveal the pill, but don't unlock yet.
                        //    The pill's clickable never fires so there is no
                        //    race between "show pill" and "unlock".
                        //  • Pill is visible (showControls): pass the touch
                        //    through (return false) so it reaches the pill's
                        //    clickable, which sets isControlsLocked = false
                        //    and shows the main controls. Consuming here would
                        //    swallow the tap and the unlock would never happen.
                        //
                        // All other events (DOWN, MOVE, CANCEL) are always
                        // consumed so gestures can't accidentally fire while
                        // locked.
                        if (motionEvent.action == MotionEvent.ACTION_UP) {
                            if (!showControls) {
                                showControls = true
                                return@pointerInteropFilter true
                            } else {
                                // Pill is visible — let the tap fall through to it.
                                return@pointerInteropFilter false
                            }
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
                            gestureText = "Speed: ${String.format("%.2f", currentSpeed)}x"
                        }
                    }
                    true
                }
        )

        AnimatedVisibility(
            visible = showControls && !isControlsLocked,
            enter = fadeIn(animationSpec = tween(300, easing = FastOutSlowInEasing)),
            exit = fadeOut(animationSpec = tween(400, easing = FastOutSlowInEasing))
        ) {
            // Cinematic scrim: top and bottom gradients so controls always
            // read clearly over any video content without a solid overlay.
            Box(modifier = Modifier.fillMaxSize()) {

                // Top scrim
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .align(Alignment.TopCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.72f),
                                    Color.Transparent
                                )
                            )
                        )
                )
                // Bottom scrim
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.78f)
                                )
                            )
                        )
                )

                // ── Top bar: back + title | shuffle + repeat + bg-play ──
                AnimatedVisibility(
                    visible = !isScrubbing && !showVerticalBar,
                    enter = fadeIn(tween(200)),
                    exit = fadeOut(tween(150)),
                    modifier = Modifier.align(Alignment.TopCenter)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Back button — glass pill
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            colors = listOf(
                                                BrandBlueDeep.copy(alpha = 0.55f),
                                                SurfaceNavy.copy(alpha = 0.40f)
                                            )
                                        )
                                    )
                                    .border(
                                        1.dp,
                                        Brush.linearGradient(
                                            listOf(BrandCyan.copy(alpha = 0.30f), OutlineFaint)
                                        ),
                                        CircleShape
                                    )
                                    .clickable(onClick = onBack),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_back_custom),
                                    contentDescription = "Back",
                                    tint = BrandHighlight,
                                    modifier = Modifier.size(21.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = formatFileName(video.name),
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = TextPrimary,
                                maxLines = 1,
                                modifier = Modifier.basicMarquee(
                                    iterations = Int.MAX_VALUE,
                                    velocity = 40.dp
                                )
                            )
                        }

                        // Shuffle / Repeat / Background-play / Folder — compact glass chip row
                        Surface(
                            color = SurfaceNavy.copy(alpha = 0.52f),
                            shape = RoundedCornerShape(50.dp),
                            border = BorderStroke(
                                0.5.dp,
                                Brush.linearGradient(
                                    listOf(BrandCyan.copy(alpha = 0.22f), OutlineFaint)
                                )
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Shuffle
                                val shuffleActive = isShuffleEnabled
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (shuffleActive)
                                                Brush.radialGradient(
                                                    listOf(
                                                        BrandCyan.copy(alpha = 0.28f),
                                                        BrandBlueDeep.copy(alpha = 0.18f)
                                                    )
                                                )
                                            else
                                                Brush.radialGradient(
                                                    listOf(
                                                        Color.Transparent,
                                                        Color.Transparent
                                                    )
                                                )
                                        )
                                        .clickable {
                                            isShuffleEnabled = !isShuffleEnabled
                                            exoPlayer.shuffleModeEnabled = isShuffleEnabled
                                            gestureText =
                                                if (isShuffleEnabled) "Shuffle: ON" else "Shuffle: OFF"
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_shuffle_custom),
                                        contentDescription = "Shuffle",
                                        tint = if (shuffleActive) BrandCyan else Color.White.copy(
                                            alpha = 0.75f
                                        ),
                                        modifier = Modifier.size(21.dp)
                                    )
                                }

                                // Repeat
                                val repeatActive = repeatMode != Player.REPEAT_MODE_OFF
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (repeatActive)
                                                Brush.radialGradient(
                                                    listOf(
                                                        BrandCyan.copy(alpha = 0.28f),
                                                        BrandBlueDeep.copy(alpha = 0.18f)
                                                    )
                                                )
                                            else
                                                Brush.radialGradient(
                                                    listOf(
                                                        Color.Transparent,
                                                        Color.Transparent
                                                    )
                                                )
                                        )
                                        .clickable {
                                            repeatMode = when (repeatMode) {
                                                Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
                                                Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
                                                else -> Player.REPEAT_MODE_OFF
                                            }
                                            exoPlayer.repeatMode = repeatMode
                                            gestureText = when (repeatMode) {
                                                Player.REPEAT_MODE_ALL -> "Repeat: ALL"
                                                Player.REPEAT_MODE_ONE -> "Repeat: ONE"
                                                else -> "Repeat: OFF"
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    val iconRes =
                                        if (repeatMode == Player.REPEAT_MODE_ONE) R.drawable.ic_repeat_one_custom else R.drawable.ic_repeat_custom
                                    Icon(
                                        painter = painterResource(iconRes),
                                        contentDescription = "Repeat",
                                        tint = if (repeatActive) BrandCyan else Color.White.copy(
                                            alpha = 0.75f
                                        ),
                                        modifier = Modifier.size(21.dp)
                                    )
                                }

                                // Background Play
                                val bgActive = isBackgroundPlayEnabled
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (bgActive)
                                                Brush.radialGradient(
                                                    listOf(
                                                        BrandCyan.copy(alpha = 0.28f),
                                                        BrandBlueDeep.copy(alpha = 0.18f)
                                                    )
                                                )
                                            else
                                                Brush.radialGradient(
                                                    listOf(
                                                        Color.Transparent,
                                                        Color.Transparent
                                                    )
                                                )
                                        )
                                        .clickable {
                                            isBackgroundPlayEnabled = !isBackgroundPlayEnabled
                                            gestureText =
                                                if (isBackgroundPlayEnabled) "Background Play: ON" else "Background Play: OFF"
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = if (bgActive) painterResource(R.drawable.ic_background_play_custom) else painterResource(
                                            R.drawable.ic_background_play_off_custom
                                        ),
                                        contentDescription = "Background Play",
                                        tint = if (bgActive) BrandCyan else Color.White.copy(alpha = 0.75f),
                                        modifier = Modifier.size(21.dp)
                                    )
                                }

                                // Folder Browse
                                if (folderVideos.size > 1) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (showFolderVideos)
                                                    Brush.radialGradient(
                                                        listOf(
                                                            BrandCyan.copy(alpha = 0.28f),
                                                            BrandBlueDeep.copy(alpha = 0.18f)
                                                        )
                                                    )
                                                else
                                                    Brush.radialGradient(
                                                        listOf(Color.Transparent, Color.Transparent)
                                                    )
                                            )
                                            .clickable { showFolderVideos = !showFolderVideos },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_browse_list_custom),
                                            contentDescription = "Browse Folder",
                                            tint = if (showFolderVideos) BrandCyan else Color.White.copy(alpha = 0.75f),
                                            modifier = Modifier.size(21.dp)
                                        )
                                    }
                                }

                                // Info / HUD Toggle
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (showHUD)
                                                Brush.radialGradient(
                                                    listOf(
                                                        BrandCyan.copy(alpha = 0.28f),
                                                        BrandBlueDeep.copy(alpha = 0.18f)
                                                    )
                                                )
                                            else
                                                Brush.radialGradient(
                                                    listOf(Color.Transparent, Color.Transparent)
                                                )
                                        )
                                        .clickable { showHUD = !showHUD },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.info_circle),
                                        contentDescription = "HUD",
                                        tint = if (showHUD) BrandCyan else Color.White.copy(alpha = 0.85f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // ── Centre: Lock (left) | Play/Pause (centre) | Rotation (right) ──
                AnimatedVisibility(
                    visible = !isScrubbing && !showVerticalBar,
                    enter = fadeIn(tween(200)),
                    exit = fadeOut(tween(150)),
                    modifier = Modifier.fillMaxSize()
                ) {
                    val configuration = LocalConfiguration.current
                    val isLandscape =
                        configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
                    val edgePadding = if (isLandscape) 48.dp else 16.dp

                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        // Lock — themed glass pill (left)
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(start = edgePadding)
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            BrandBlueDeep.copy(alpha = 0.60f),
                                            SurfaceNavy.copy(alpha = 0.45f)
                                        )
                                    )
                                )
                                .border(
                                    1.dp,
                                    Brush.linearGradient(
                                        listOf(BrandCyan.copy(alpha = 0.28f), OutlineFaint)
                                    ),
                                    CircleShape
                                )
                                .clickable { isControlsLocked = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painterResource(R.drawable.ic_lock_open_custom),
                                contentDescription = "Lock controls",
                                tint = Color.White.copy(alpha = 0.85f),
                                modifier = Modifier.size(21.dp)
                            )
                        }

                        // Play/Pause — signature large brand button
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            BrandBlueDeep.copy(alpha = 0.70f),
                                            SurfaceNavy.copy(alpha = 0.55f)
                                        )
                                    )
                                )
                                .border(
                                    1.dp,
                                    Brush.linearGradient(
                                        listOf(BrandCyan.copy(alpha = 0.35f), OutlineFaint)
                                    ),
                                    CircleShape
                                )
                                .clickable {
                                    if (exoPlayer.isPlaying) exoPlayer.pause()
                                    else {
                                        if (exoPlayer.playbackState == Player.STATE_ENDED) {
                                            exoPlayer.seekTo(0)
                                        }
                                        exoPlayer.play()
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            // Pulse animation for the play icon
                            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                            val pulseScale by infiniteTransition.animateFloat(
                                initialValue = 1f,
                                targetValue = if (!isPlaying) 1.08f else 1f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(1200, easing = FastOutSlowInEasing),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "scale"
                            )

                            Icon(
                                painter = if (isPlaying) painterResource(R.drawable.ic_pause_custom)
                                else painterResource(R.drawable.ic_play_custom),
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                tint = BrandHighlight,
                                modifier = Modifier
                                    .size(34.dp)
                                    .graphicsLayer {
                                        scaleX = pulseScale
                                        scaleY = pulseScale
                                    }
                            )
                        }

                        // Rotation — themed glass pill (right)
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = edgePadding)
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            BrandBlueDeep.copy(alpha = 0.60f),
                                            SurfaceNavy.copy(alpha = 0.45f)
                                        )
                                    )
                                )
                                .border(
                                    1.dp,
                                    Brush.linearGradient(
                                        listOf(BrandCyan.copy(alpha = 0.28f), OutlineFaint)
                                    ),
                                    CircleShape
                                )
                                .clickable {
                                    isForcedLandscape = !isForcedLandscape
                                    gestureText =
                                        if (isForcedLandscape) "Orientation: Landscape" else "Orientation: Portrait"
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painterResource(R.drawable.ic_orientation_custom),
                                contentDescription = "Orientation",
                                tint = Color.White.copy(alpha = 0.85f),
                                modifier = Modifier.size(21.dp)
                            )
                        }


                    }
                }
                // ── Bottom: utility row + seekbar + time chips ──
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 14.dp)
                        .padding(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val displayPos = position
                    var showRemaining by remember { mutableStateOf(false) }

                    // ── Utility row: audio + subs (left) | aspect + speed (right) ──
                    AnimatedVisibility(
                        visible = !isScrubbing && !showVerticalBar,
                        enter = fadeIn(tween(200)),
                        exit = fadeOut(tween(150))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Left pill: Audio + Subtitles
                            Surface(
                                color = SurfaceNavy.copy(alpha = 0.52f),
                                shape = RoundedCornerShape(50.dp),
                                border = BorderStroke(0.5.dp, OutlineFaint)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .clickable { showAudioSelector = true },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(painterResource(R.drawable.ic_music_custom), null, tint = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(21.dp))
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .clickable { showSubtitleSelector = true },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(painterResource(R.drawable.ic_subtitles_custom), null, tint = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(21.dp))
                                    }
                                }
                            }

                            // Right pill: Aspect ratio + Speed
                            Surface(
                                color = SurfaceNavy.copy(alpha = 0.52f),
                                shape = RoundedCornerShape(50.dp),
                                border = BorderStroke(0.5.dp, OutlineFaint)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .clickable {
                                                resizeModeIndex = (resizeModeIndex + 1) % resizeModes.size
                                                gestureText = "Aspect Ratio: ${resizeModes[resizeModeIndex].second}"
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        val iconRes = when (resizeModeIndex) {
                                            0 -> R.drawable.ic_aspect_fit
                                            1 -> R.drawable.ic_aspect_fill
                                            else -> R.drawable.ic_aspect_zoom
                                        }
                                        Icon(painterResource(iconRes), null, tint = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(21.dp))
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .clickable { showSpeedSelector = true },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(painterResource(R.drawable.ic_speed_custom), null, tint = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(21.dp))
                                    }
                                }
                            }
                        }
                    }

                    // ── Seekbar + time chips ──
                    AnimatedVisibility(
                        visible = !showVerticalBar,
                        enter = fadeIn(tween(200)),
                        exit = fadeOut(tween(150))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                color = BrandBlueDeep.copy(alpha = 0.50f),
                                shape = RoundedCornerShape(20.dp),
                                border = BorderStroke(0.5.dp, BrandCyan.copy(alpha = 0.18f))
                            ) {
                                Text(
                                    text = formatDuration(displayPos),
                                    style = MaterialTheme.typography.timeCode.copy(color = BrandHighlight),
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                                )
                            }

                            SmoothPlayerScrubber(
                                exoPlayer = exoPlayer,
                                duration = duration,
                                onScrubbingChanged = { isScrubbing = it; if (it) showControls = true },
                                onPositionChanged = { position = it },
                                modifier = Modifier.weight(1f)
                            )

                            Surface(
                                modifier = Modifier.clickable { showRemaining = !showRemaining },
                                color = if (showRemaining) BrandCyan.copy(alpha = 0.22f) else BrandBlueDeep.copy(alpha = 0.50f),
                                shape = RoundedCornerShape(20.dp),
                                border = BorderStroke(0.5.dp, BrandCyan.copy(alpha = if (showRemaining) 0.55f else 0.18f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(1.dp)
                                ) {
                                    if (showRemaining) Text("\u2212", color = BrandCyan, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.W700))
                                    Text(
                                        text = if (showRemaining) formatDuration((duration - displayPos).coerceAtLeast(0L)) else formatDuration(duration),
                                        style = MaterialTheme.typography.timeCode.copy(color = if (showRemaining) BrandCyan else BrandHighlight)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Center Gesture/Action Feedback
        AnimatedVisibility(
            visible = gestureText.isNotEmpty() && gestureText != "Brightness" && gestureText != "Volume",
            enter = fadeIn(animationSpec = tween(180, easing = FastOutSlowInEasing)) +
                    scaleIn(
                        initialScale = 0.88f,
                        animationSpec = tween(200, easing = FastOutSlowInEasing)
                    ),
            exit = fadeOut(animationSpec = tween(240, easing = FastOutSlowInEasing)),
            modifier = Modifier.align(BiasAlignment(0f, -0.7f))
        ) {
            // ---> FIX: Freeze the text during the exit animation <---
            var retainedText by remember { mutableStateOf("") }
            LaunchedEffect(gestureText) {
                if (gestureText.isNotEmpty()) retainedText = gestureText
            }
            // Use gestureText if available, otherwise fall back to the frozen text
            val displayText = if (gestureText.isNotEmpty()) gestureText else retainedText

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(30.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                BrandBlueDeep.copy(alpha = 0.72f),
                                SurfaceNavy.copy(alpha = 0.60f)
                            )
                        )
                    )
                    .border(
                        1.dp,
                        Brush.linearGradient(
                            listOf(
                                BrandCyan.copy(alpha = 0.40f),
                                BrandIceBlue.copy(alpha = 0.20f)
                            )
                        ),
                        RoundedCornerShape(30.dp)
                    )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // ---> FIX: Calculate the icon using the frozen text <---
                    val iconPainter = when {
                        displayText.contains("Aspect") -> {
                            val res = when (resizeModeIndex) {
                                0 -> R.drawable.ic_aspect_fit
                                1 -> R.drawable.ic_aspect_fill
                                else -> R.drawable.ic_aspect_zoom
                            }
                            painterResource(res)
                        }

                        displayText.contains("Zoom") -> painterResource(R.drawable.ic_aspect_zoom)
                        displayText.contains("Background") -> if (isBackgroundPlayEnabled) rememberVectorPainter(
                            Icons.Default.Headset
                        ) else rememberVectorPainter(Icons.Default.HeadsetOff)

                        else -> null
                    }
                    if (iconPainter != null) {
                        Icon(
                            iconPainter,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = BrandCyan
                        )
                    }
                    Text(
                        // ---> FIX: Display the frozen text <---
                        text = displayText,
                        style = MaterialTheme.typography.gestureFeedback,
                        color = BrandHighlight
                    )
                }
            }
        }

        // Locked state: everything else is hidden and gestures are inert
        // (see the pointerInteropFilter gate above); only this pill is
        // shown, faded with the same showControls timer so a tap brings it
        // up and it auto-hides again rather than staying glued to the
        // screen. Tapping the pill itself is the only way to unlock.
        AnimatedVisibility(
            visible = isControlsLocked && showControls,
            enter = fadeIn(animationSpec = tween(220, easing = FastOutSlowInEasing)) +
                    scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(240, easing = FastOutSlowInEasing)
                    ),
            exit = fadeOut(animationSpec = tween(300, easing = FastOutSlowInEasing)),
            modifier = Modifier.align(BiasAlignment(0f, -0.7f))
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(30.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                BrandBlueDeep.copy(alpha = 0.72f),
                                SurfaceNavy.copy(alpha = 0.60f)
                            )
                        )
                    )
                    .border(
                        1.dp,
                        Brush.linearGradient(
                            listOf(
                                BrandCyan.copy(alpha = 0.40f),
                                BrandIceBlue.copy(alpha = 0.20f)
                            )
                        ),
                        RoundedCornerShape(30.dp)
                    )
                    .clickable {
                        isControlsLocked = false
                        showControls = true
                    }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        painterResource(R.drawable.ic_lock_custom),
                        contentDescription = null,
                        tint = BrandCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        "Tap to unlock",
                        style = MaterialTheme.typography.labelLarge,
                        color = BrandHighlight
                    )
                }
            }
        }

        // Small persistent timestamp in the corner while locked, so the
        // screen isn't completely uninformative even with controls hidden
        // -- same idea as MX Player's locked-state clock/position chip.
        AnimatedVisibility(
            visible = isControlsLocked,
            enter = fadeIn(animationSpec = tween(350, easing = FastOutSlowInEasing)),
            exit = fadeOut(animationSpec = tween(350, easing = FastOutSlowInEasing)),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 20.dp, bottom = 24.dp)
        ) {
            Text(
                text = formatDuration(position),
                color = BrandHighlight.copy(alpha = 0.50f),
                style = MaterialTheme.typography.labelMedium
            )
        }

        AnimatedVisibility(
            visible = showVerticalBar,
            enter = fadeIn(animationSpec = tween(180, easing = FastOutSlowInEasing)),
            exit = fadeOut(animationSpec = tween(260, easing = FastOutSlowInEasing))
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // The raw gestureValue updates in coarse, uneven steps because
                // it's driven directly by onScroll's per-frame touch deltas —
                // applying it straight to the Canvas/text made the fill and
                // the percentage label visibly stutter on fast swipes. We
                // still use gestureValue immediately for the *actual* system
                // brightness/volume call (so audio/screen response stays
                // instant), but everything the eye sees here tracks a
                // critically-damped spring instead, so the bar glides even
                // when the underlying input is jumpy.
                val animatedGestureValue by animateFloatAsState(
                    targetValue = gestureValue,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    ),
                    label = "gestureValueSmooth"
                )
                val isBoost = !isBarBrightness && animatedGestureValue > 1.0f
                val displayPct = (animatedGestureValue * 100).toInt()
                // Compact bar dimensions
                val barHeight = 150.dp
                val barWidth = 5.dp

                // Glass pill container — narrower and tighter than before
                Box(
                    modifier = Modifier
                        .align(if (isBarBrightness) Alignment.CenterEnd else Alignment.CenterStart)
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    BrandBlueDeep.copy(alpha = 0.80f),
                                    SurfaceNavy.copy(alpha = 0.68f)
                                )
                            )
                        )
                        .border(
                            0.8.dp,
                            Brush.linearGradient(
                                if (isBarBrightness)
                                    listOf(
                                        BrandHighlight.copy(alpha = 0.38f),
                                        BrandIceBlue.copy(alpha = 0.18f)
                                    )
                                else if (isBoost)
                                    listOf(
                                        BrandCyan.copy(alpha = 0.50f),
                                        BrandBlueDeep.copy(alpha = 0.22f)
                                    )
                                else
                                    listOf(BrandCyan.copy(alpha = 0.32f), OutlineFaint)
                            ),
                            RoundedCornerShape(24.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 12.dp)
                        .width(42.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Compact glowing icon
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = if (isBarBrightness)
                                            listOf(
                                                BrandHighlight.copy(alpha = 0.16f),
                                                Color.Transparent
                                            )
                                        else if (isBoost)
                                            listOf(
                                                BrandCyan.copy(alpha = 0.24f),
                                                BrandBlueDeep.copy(alpha = 0.08f)
                                            )
                                        else
                                            listOf(
                                                BrandCyan.copy(alpha = 0.16f),
                                                Color.Transparent
                                            )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = when {
                                    isBarBrightness -> painterResource(R.drawable.ic_brightness_custom)
                                    animatedGestureValue <= 0f -> painterResource(R.drawable.ic_volume_off_custom)
                                    else -> painterResource(R.drawable.ic_volume_custom)
                                },
                                contentDescription = null,
                                tint = if (isBarBrightness) BrandHighlight else BrandCyan,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        // Compact percentage label
                        Text(
                            text = "$displayPct%",
                            style = MaterialTheme.typography.labelSmall.copy(
                                brush = if (isBarBrightness)
                                    Brush.linearGradient(listOf(BrandHighlight, BrandIceBlue))
                                else if (isBoost)
                                    Brush.linearGradient(listOf(BrandCyan, BrandBlue))
                                else
                                    Brush.linearGradient(listOf(BrandHighlight, BrandCyan))
                            ),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        // Track + fill — gradient fills, drawn Canvas bottom-up
                        Canvas(
                            modifier = Modifier
                                .width(barWidth)
                                .height(barHeight)
                        ) {
                            val w = size.width
                            val h = size.height
                            val radius = w / 2f

                            // Track (faint background)
                            drawRoundRect(
                                color = Color.White.copy(alpha = 0.12f),
                                size = androidx.compose.ui.geometry.Size(w, h),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius)
                            )

                            if (isBarBrightness) {
                                // Brightness: platinum→ice-blue gradient fill
                                val fillH =
                                    (h * animatedGestureValue.coerceIn(0f, 1f)).coerceAtLeast(
                                        radius * 2
                                    )
                                drawRoundRect(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(BrandHighlight, BrandIceBlue),
                                        startY = h - fillH,
                                        endY = h
                                    ),
                                    topLeft = androidx.compose.ui.geometry.Offset(
                                        0f,
                                        h - fillH
                                    ),
                                    size = androidx.compose.ui.geometry.Size(w, fillH),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(
                                        radius
                                    )
                                )
                            } else {
                                // Volume base layer — teal gradient 0→100%
                                val baseFraction = animatedGestureValue.coerceIn(0f, 1f)
                                val boostFraction = (animatedGestureValue - 1f).coerceIn(0f, 1f)

                                if (baseFraction > 0f) {
                                    val baseH = (h * baseFraction).coerceAtLeast(radius * 2)
                                    drawRoundRect(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(BrandCyan, BrandIceBlue),
                                            startY = h - baseH,
                                            endY = h
                                        ),
                                        topLeft = androidx.compose.ui.geometry.Offset(
                                            0f,
                                            h - baseH
                                        ),
                                        size = androidx.compose.ui.geometry.Size(w, baseH),
                                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(
                                            radius
                                        )
                                    )
                                }

                                // Boost layer — violet→cyan overlay (100→200%)
                                if (boostFraction > 0f) {
                                    val boostH = (h * boostFraction).coerceAtLeast(radius * 2)
                                    drawRoundRect(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(BrandBlue, BrandCyan),
                                            startY = h - boostH,
                                            endY = h
                                        ),
                                        topLeft = androidx.compose.ui.geometry.Offset(
                                            0f,
                                            h - boostH
                                        ),
                                        size = androidx.compose.ui.geometry.Size(w, boostH),
                                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(
                                            radius
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            PremiumSeekFlash(
                visible = seekAnimationSide == "left",
                token = seekAnimationToken,
                cumulativeSeconds = cumulativeSeekAmount,
                isForward = false,
                modifier = Modifier.align(Alignment.CenterStart)
            )
            PremiumSeekFlash(
                visible = seekAnimationSide == "right",
                token = seekAnimationToken,
                cumulativeSeconds = cumulativeSeekAmount,
                isForward = true,
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }

        if (showHUD) {
            PremiumHUD(
                video = video,
                videoSize = currentVideoSize,
                exoPlayer = exoPlayer,
                onDismiss = { showHUD = false }
            )
        }
        
        if (isLandscape) {
            TrackSelectionSidePanel(
                visible = showAudioSelector,
                player = exoPlayer,
                trackType = C.TRACK_TYPE_AUDIO,
                onDismiss = { showAudioSelector = false }
            )

            TrackSelectionSidePanel(
                visible = showSubtitleSelector,
                player = exoPlayer,
                trackType = C.TRACK_TYPE_TEXT,
                onDismiss = { showSubtitleSelector = false },
                onAddLocalSubtitle = { subtitlePickerLauncher.launch(arrayOf("*/*")) },
                onOpenOnlineSearch = { showOnlineSubtitleSearch = true },
                onOpenCustomization = { showSubtitleCustomization = true }
            )
        } else {
            if (showAudioSelector) {
                TrackSelectionDialogPortrait(
                    player = exoPlayer,
                    trackType = C.TRACK_TYPE_AUDIO,
                    onDismiss = { showAudioSelector = false }
                )
            }
            if (showSubtitleSelector) {
                TrackSelectionDialogPortrait(
                    player = exoPlayer,
                    trackType = C.TRACK_TYPE_TEXT,
                    onDismiss = { showSubtitleSelector = false },
                    onAddLocalSubtitle = { subtitlePickerLauncher.launch(arrayOf("*/*")) },
                    onOpenOnlineSearch = { showOnlineSubtitleSearch = true },
                    onOpenCustomization = { showSubtitleCustomization = true }
                )
            }
        }

        if (showSubtitleCustomization) {
            SubtitleCustomizationDialog(
                onDismiss = { showSubtitleCustomization = false }
            )
        }

        if (showOnlineSubtitleSearch) {
            OnlineSubtitleSearchDialog(
                videoTitle = video.name,
                onDismiss = { showOnlineSubtitleSearch = false },
                onSubtitleSelected = { url ->
                    val subtitleConfig = MediaItem.SubtitleConfiguration.Builder(Uri.parse(url))
                        .setMimeType(MimeTypes.APPLICATION_SUBRIP)
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
            )
        }

        PlaybackSpeedSidePanel(
            visible = showSpeedSelector,
            currentSpeed = currentSpeed,
            onSpeedSelected = { speed ->
                currentSpeed = speed
                exoPlayer.setPlaybackSpeed(speed)
            },
            onDismiss = { showSpeedSelector = false }
        )

        // ── Folder video browser panel ─────────────────────────────────
        // Slides in from the right as an overlay. Shows all videos in the
        // same folder; tapping one switches the player without navigating
        // back. The panel auto-closes after selection or on a second tap
        // of the list icon.
        AnimatedVisibility(
            visible = showFolderVideos && folderVideos.size > 1,
            enter = if (isLandscape) {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(320, easing = FastOutSlowInEasing)
                ) + fadeIn(tween(280))
            } else {
                slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(320, easing = FastOutSlowInEasing)
                ) + fadeIn(tween(280))
            },
            exit = if (isLandscape) {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(260, easing = FastOutSlowInEasing)
                ) + fadeOut(tween(220))
            } else {
                slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(260, easing = FastOutSlowInEasing)
                ) + fadeOut(tween(220))
            },
            modifier = if (isLandscape) Modifier.align(Alignment.CenterEnd) else Modifier.align(Alignment.BottomCenter)
        ) {
            Box(
                modifier = if (isLandscape) {
                    Modifier
                        .fillMaxWidth(0.35f)
                        .fillMaxHeight()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    SurfaceNavy.copy(alpha = 0.90f),
                                    BgBase.copy(alpha = 0.98f)
                                )
                            )
                        )
                        .border(
                            1.dp,
                            Brush.linearGradient(
                                listOf(BrandCyan.copy(alpha = 0.22f), OutlineFaint)
                            ),
                            RectangleShape
                        )
                } else {
                    Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.55f)
                        .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    SurfaceNavy.copy(alpha = 0.97f),
                                    BgBase.copy(alpha = 0.99f)
                                )
                            )
                        )
                        .border(
                            1.dp,
                            Brush.linearGradient(
                                listOf(BrandCyan.copy(alpha = 0.22f), OutlineFaint)
                            ),
                            RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                        )
                }
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    if (!isLandscape) {
                        // Handle + title
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp, bottom = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp, 4.dp)
                                    .background(OutlineMedium, RoundedCornerShape(2.dp))
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = if (isLandscape) 16.dp else 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        // Themed Close button on the left
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            BrandBlueDeep.copy(alpha = 0.60f),
                                            SurfaceNavy.copy(alpha = 0.45f)
                                        )
                                    )
                                )
                                .border(
                                    1.dp,
                                    Brush.linearGradient(
                                        listOf(BrandCyan.copy(alpha = 0.28f), OutlineFaint)
                                    ),
                                    CircleShape
                                )
                                .clickable { showFolderVideos = false },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painterResource(R.drawable.ic_close_custom),
                                contentDescription = "Close",
                                tint = Color.White.copy(alpha = 0.85f),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        val folderName = remember(video.path) {
                            java.io.File(video.path).parentFile?.name ?: "Folder"
                        }

                        Column {
                            Text(
                                folderName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            if (!isLandscape) {
                                Text(
                                    "${folderVideos.size} videos",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextTertiary
                                )
                            }
                        }
                    }

                    HorizontalDivider(
                        color = OutlineFaint,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )

                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(folderVideos, key = { it.id }) { item ->
                            val isCurrentVideo = item.id == video.id
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable(enabled = !isCurrentVideo) {
                                        showFolderVideos = false
                                        onPlayVideo(item)
                                    },
                                color = if (isCurrentVideo)
                                    BrandBlueDeep.copy(alpha = 0.35f)
                                else
                                    Color.Transparent,
                                border = if (isCurrentVideo)
                                    BorderStroke(1.dp, BrandCyan.copy(alpha = 0.5f))
                                else null,
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Mini thumbnail
                                    Box(
                                        modifier = Modifier
                                            .size(if (isLandscape) 60.dp else 72.dp, if (isLandscape) 36.dp else 44.dp)
                                            .clip(RoundedCornerShape(if (isLandscape) 8.dp else 10.dp))
                                            .background(SurfaceRaisedAlt)
                                    ) {
                                        AsyncImage(
                                            model = remember(item.uri) {
                                                ImageRequest.Builder(context)
                                                    .data(item.uri)
                                                    .videoFrameMillis(1000)
                                                    .crossfade(true)
                                                    .build()
                                            },
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                        if (isCurrentVideo) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(BrandCyan.copy(alpha = 0.25f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    Icons.Default.PlayArrow,
                                                    null,
                                                    tint = BrandCyan,
                                                    modifier = Modifier.size(if (isLandscape) 14.dp else 18.dp)
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.name,
                                            style = if (isLandscape) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
                                            color = if (isCurrentVideo) BrandCyan else TextPrimary,
                                            fontWeight = if (isCurrentVideo) FontWeight.SemiBold else FontWeight.Normal,
                                            maxLines = if (isLandscape) 1 else 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Text(
                                            formatDuration(item.duration),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextTertiary
                                        )
                                    }
                                    if (isCurrentVideo) {
                                        Icon(
                                            painterResource(R.drawable.ic_volume_custom),
                                            null,
                                            tint = BrandCyan,
                                            modifier = Modifier.size(16.dp).padding(start = 4.dp)
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
}

@UnstableApi
@Composable
fun TrackSelectionDialogPortrait(
    player: ExoPlayer,
    trackType: Int,
    onDismiss: () -> Unit,
    onAddLocalSubtitle: () -> Unit = {},
    onOpenOnlineSearch: () -> Unit = {},
    onOpenCustomization: () -> Unit = {}
) {
    val tracks = player.currentTracks
    val radioColors = RadioButtonDefaults.colors(
        selectedColor = BrandCyan,
        unselectedColor = TextTertiary
    )
    val title = if (trackType == C.TRACK_TYPE_AUDIO) "Audio Tracks" else "Subtitles"

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = BiasAlignment(0f, 0.6f)) {
            PremiumCardSurface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .padding(16.dp),
                cornerRadius = 28.dp
            ) {
                Column(
                    modifier = Modifier
                        .background(SurfaceNavy.copy(alpha = 0.95f))
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            title,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(SurfaceRaisedAlt)
                                .clickable { onDismiss() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(painterResource(R.drawable.ic_close_custom), null, tint = TextSecondary, modifier = Modifier.size(12.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = OutlineFaint)

                    LazyColumn(
                        modifier = Modifier.heightIn(max = 240.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        if (trackType == C.TRACK_TYPE_AUDIO) {
                            val audioGroups = tracks.groups.filter { it.type == C.TRACK_TYPE_AUDIO }
                            if (audioGroups.isEmpty()) {
                                item {
                                    Text("No audio tracks", style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                                }
                            } else {
                                audioGroups.forEach { group ->
                                    for (i in 0 until group.length) {
                                        val track = group.getTrackFormat(i)
                                        val isSelected = group.isTrackSelected(i)
                                        item {
                                            TrackSelectionItemCompact(
                                                label = "${track.language?.uppercase() ?: "UNK"} - ${track.label ?: "Track ${i + 1}"}",
                                                isSelected = isSelected,
                                                onClick = {
                                                    player.trackSelectionParameters = player.trackSelectionParameters
                                                        .buildUpon()
                                                        .setOverrideForType(TrackSelectionOverride(group.mediaTrackGroup, i))
                                                        .build()
                                                    onDismiss()
                                                },
                                                radioColors = radioColors
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            val textGroups = tracks.groups.filter { it.type == C.TRACK_TYPE_TEXT }
                            item {
                                TrackSelectionItemCompact(
                                    label = "None",
                                    isSelected = player.trackSelectionParameters.disabledTrackTypes.contains(C.TRACK_TYPE_TEXT),
                                    onClick = {
                                        player.trackSelectionParameters = player.trackSelectionParameters
                                            .buildUpon()
                                            .setDisabledTrackTypes(setOf(C.TRACK_TYPE_TEXT))
                                            .build()
                                        onDismiss()
                                    },
                                    radioColors = radioColors
                                )
                            }
                            textGroups.forEach { group ->
                                for (i in 0 until group.length) {
                                    val track = group.getTrackFormat(i)
                                    val isSelected = group.isTrackSelected(i) && !player.trackSelectionParameters.disabledTrackTypes.contains(C.TRACK_TYPE_TEXT)
                                    item {
                                        TrackSelectionItemCompact(
                                            label = "${track.language?.uppercase() ?: "UNK"} - ${track.label ?: "Sub ${i + 1}"}",
                                            isSelected = isSelected,
                                            onClick = {
                                                player.trackSelectionParameters = player.trackSelectionParameters
                                                    .buildUpon()
                                                    .setDisabledTrackTypes(emptySet())
                                                    .setOverrideForType(TrackSelectionOverride(group.mediaTrackGroup, i))
                                                    .build()
                                                onDismiss()
                                            },
                                            radioColors = radioColors
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (trackType == C.TRACK_TYPE_TEXT) {
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = OutlineFaint)
                        Spacer(modifier = Modifier.height(12.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            SubtitleOptionRowCompact(Icons.Default.Add, "Add Local", onClick = { onDismiss(); onAddLocalSubtitle() })
                            SubtitleOptionRowCompact(Icons.Default.CloudDownload, "Online Search", onClick = { onDismiss(); onOpenOnlineSearch() })
                            SubtitleOptionRowCompact(painterResource(R.drawable.ic_settings_custom), "Settings", onClick = { onDismiss(); onOpenCustomization() })
                        }
                    }
                }
            }
        }
    }
}

@UnstableApi
@Composable
fun TrackSelectionSidePanel(
    visible: Boolean,
    player: ExoPlayer,
    trackType: Int,
    onDismiss: () -> Unit,
    onAddLocalSubtitle: () -> Unit = {},
    onOpenOnlineSearch: () -> Unit = {},
    onOpenCustomization: () -> Unit = {}
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    val tracks = player.currentTracks
    val radioColors = RadioButtonDefaults.colors(
        selectedColor = BrandCyan,
        unselectedColor = TextTertiary
    )
    val title = if (trackType == C.TRACK_TYPE_AUDIO) "Audio Tracks" else "Subtitles"

    AnimatedVisibility(
        visible = visible,
        enter = slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300)) + fadeIn(),
        exit = slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300)) + fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) { detectTapGestures(onTap = { onDismiss() }) },
            contentAlignment = Alignment.CenterEnd
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(if (isLandscape) 0.32f else 0.82f)
                    .clickable(enabled = false) { },
                color = SurfaceNavy.copy(alpha = 0.85f),
                border = BorderStroke(0.5.dp, OutlineFaint),
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            title,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            BrandBlueDeep.copy(alpha = 0.60f),
                                            SurfaceNavy.copy(alpha = 0.45f)
                                        )
                                    )
                                )
                                .border(
                                    1.dp,
                                    Brush.linearGradient(
                                        listOf(BrandCyan.copy(alpha = 0.28f), OutlineFaint)
                                    ),
                                    CircleShape
                                )
                                .clickable { onDismiss() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painterResource(R.drawable.ic_close_custom),
                                contentDescription = "Close",
                                tint = Color.White.copy(alpha = 0.85f),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = OutlineFaint)

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        if (trackType == C.TRACK_TYPE_AUDIO) {
                            val audioGroups = tracks.groups.filter { it.type == C.TRACK_TYPE_AUDIO }
                            if (audioGroups.isEmpty()) {
                                item {
                                    Text("No audio tracks", style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                                }
                            } else {
                                audioGroups.forEach { group ->
                                    for (i in 0 until group.length) {
                                        val track = group.getTrackFormat(i)
                                        val isSelected = group.isTrackSelected(i)
                                        item {
                                            TrackSelectionItemCompact(
                                                label = "${track.language?.uppercase() ?: "UNK"} - ${track.label ?: "Track ${i + 1}"}",
                                                isSelected = isSelected,
                                                onClick = {
                                                    player.trackSelectionParameters = player.trackSelectionParameters
                                                        .buildUpon()
                                                        .setOverrideForType(TrackSelectionOverride(group.mediaTrackGroup, i))
                                                        .build()
                                                    onDismiss()
                                                },
                                                radioColors = radioColors
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            val textGroups = tracks.groups.filter { it.type == C.TRACK_TYPE_TEXT }
                            item {
                                TrackSelectionItemCompact(
                                    label = "None",
                                    isSelected = player.trackSelectionParameters.disabledTrackTypes.contains(C.TRACK_TYPE_TEXT),
                                    onClick = {
                                        player.trackSelectionParameters = player.trackSelectionParameters
                                            .buildUpon()
                                            .setDisabledTrackTypes(setOf(C.TRACK_TYPE_TEXT))
                                            .build()
                                        onDismiss()
                                    },
                                    radioColors = radioColors
                                )
                            }
                            textGroups.forEach { group ->
                                for (i in 0 until group.length) {
                                    val track = group.getTrackFormat(i)
                                    val isSelected = group.isTrackSelected(i) && !player.trackSelectionParameters.disabledTrackTypes.contains(C.TRACK_TYPE_TEXT)
                                    item {
                                        TrackSelectionItemCompact(
                                            label = "${track.language?.uppercase() ?: "UNK"} - ${track.label ?: "Sub ${i + 1}"}",
                                            isSelected = isSelected,
                                            onClick = {
                                                player.trackSelectionParameters = player.trackSelectionParameters
                                                    .buildUpon()
                                                    .setDisabledTrackTypes(emptySet())
                                                    .setOverrideForType(TrackSelectionOverride(group.mediaTrackGroup, i))
                                                    .build()
                                                onDismiss()
                                            },
                                            radioColors = radioColors
                                        )
                                    }
                                }
                            }
                            
                            // Options INSIDE the LazyColumn
                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                                HorizontalDivider(color = OutlineFaint)
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                            item {
                                SubtitleOptionRowCompact(Icons.Default.Add, "Add Local", onClick = { onDismiss(); onAddLocalSubtitle() })
                            }
                            item {
                                SubtitleOptionRowCompact(Icons.Default.CloudDownload, "Online Search", onClick = { onDismiss(); onOpenOnlineSearch() })
                            }
                            item {
                                SubtitleOptionRowCompact(painterResource(R.drawable.ic_settings_custom), "Settings", onClick = { onDismiss(); onOpenCustomization() })
                            }
                        }
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
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) BrandBlueDeep.copy(alpha = 0.20f) else Color.Transparent,
        border = if (isSelected) BorderStroke(1.dp, BrandCyan.copy(alpha = 0.35f)) else null
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(selected = isSelected, onClick = null, colors = radioColors, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = if (isSelected) BrandCyan else TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SubtitleOptionRowCompact(
    icon: Any,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        color = SurfaceRaised.copy(alpha = 0.45f),
        border = BorderStroke(1.dp, OutlineFaint)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(BrandCyan.copy(alpha = 0.08f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                when (icon) {
                    is ImageVector -> Icon(icon, null, tint = BrandCyan, modifier = Modifier.size(14.dp))
                    is Painter -> Icon(icon, null, tint = BrandCyan, modifier = Modifier.size(14.dp))
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(label, style = MaterialTheme.typography.bodySmall, color = Color.White)
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
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(300)) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(300)) + fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) { detectTapGestures(onTap = { onDismiss() }) },
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(if (isLandscape) 0.65f else 1f)
                    .padding(if (isLandscape) 20.dp else 0.dp)
                    .clickable(enabled = false) { },
                color = SurfaceNavy.copy(alpha = 0.90f),
                shape = if (isLandscape) RoundedCornerShape(28.dp) else RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                border = BorderStroke(1.dp, OutlineFaint)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Playback Speed", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("${String.format("%.2f", currentSpeed)}x", style = MaterialTheme.typography.titleMedium, color = BrandCyan, fontWeight = FontWeight.Black)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.SlowMotionVideo, null, tint = TextTertiary, modifier = Modifier.size(18.dp))
                        Slider(
                            value = currentSpeed,
                            onValueChange = onSpeedSelected,
                            valueRange = 0.25f..2.0f,
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(
                                thumbColor = BrandCyan,
                                activeTrackColor = BrandCyan,
                                inactiveTrackColor = SurfaceRaisedAlt
                            )
                        )
                        Icon(painterResource(R.drawable.ic_speed_custom), null, tint = BrandCyan, modifier = Modifier.size(18.dp))
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(0.25f, 0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f).forEach { speed ->
                            val isSelected = Math.abs(currentSpeed - speed) < 0.01f
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onSpeedSelected(speed) },
                                color = if (isSelected) BrandCyan.copy(alpha = 0.15f) else SurfaceRaised.copy(alpha = 0.5f),
                                border = BorderStroke(1.dp, if (isSelected) BrandCyan.copy(alpha = 0.5f) else OutlineFaint),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "${speed}x",
                                    color = if (isSelected) BrandCyan else Color.White,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
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
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = SurfaceRaised.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, OutlineFaint)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(BrandCyan.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                when (icon) {
                    is ImageVector -> Icon(icon, null, tint = BrandCyan, modifier = Modifier.size(18.dp))
                    is Painter -> Icon(icon, null, tint = BrandCyan, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                label,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White
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
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) BrandBlueDeep.copy(alpha = 0.25f) else Color.Transparent,
        border = if (isSelected) BorderStroke(1.8.dp, BrandCyan.copy(alpha = 0.8f)) else null
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
                color = if (isSelected) BrandCyan else MaterialTheme.colorScheme.onSurface,
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

fun updateOrientation(context: Context, isFullscreen: Boolean, isLandscape: Boolean = true) {
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

            // Restore default cutout mode
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                window.attributes.layoutInDisplayCutoutMode =
                    android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT
            }
        }
    }
}

// --- Helpers & Data Fetching ---

fun fetchAllVideos(context: Context, videos: MutableList<VideoFile>) {
    videos.clear()
    val projection = arrayOf(
        MediaStore.Video.Media._ID,
        MediaStore.Video.Media.DISPLAY_NAME,
        MediaStore.Video.Media.DATA,
        MediaStore.Video.Media.DURATION,
        MediaStore.Video.Media.SIZE,
        MediaStore.Video.Media.WIDTH,
        MediaStore.Video.Media.HEIGHT
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

        while (it.moveToNext()) {
            val id = it.getLong(idCol)
            val name = it.getString(nameCol)
            val path = it.getString(pathCol)
            val duration = it.getLong(durCol)
            val size = it.getLong(sizeCol)
            val width = it.getInt(widthCol)
            val height = it.getInt(heightCol)
            val contentUri =
                ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)

            videos.add(VideoFile(id, name, path, duration, size, contentUri, width, height))
        }
    }
}

fun fetchVideoFolders(context: Context, folders: MutableList<VideoFolder>) {
    folders.clear()
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

    val tempFolders = mutableMapOf<String, Pair<String, Int>>()

    cursor?.use {
        val bucketColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)
        val dataColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)

        while (it.moveToNext()) {
            val bucketName = it.getString(bucketColumn) ?: "Internal"
            val filePath = it.getString(dataColumn)
            val folderPath = File(filePath).parent ?: ""

            val current = tempFolders[bucketName] ?: Pair(folderPath, 0)
            tempFolders[bucketName] = Pair(current.first, current.second + 1)
        }
    }

    tempFolders.forEach { (name, info) ->
        folders.add(VideoFolder(name, info.first, info.second, "Local"))
    }
}

fun fetchVideosInFolder(context: Context, folderPath: String, videos: MutableList<VideoFile>) {
    videos.clear()
    val projection = arrayOf(
        MediaStore.Video.Media._ID,
        MediaStore.Video.Media.DISPLAY_NAME,
        MediaStore.Video.Media.DATA,
        MediaStore.Video.Media.DURATION,
        MediaStore.Video.Media.SIZE,
        MediaStore.Video.Media.WIDTH,
        MediaStore.Video.Media.HEIGHT
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

        while (it.moveToNext()) {
            val id = it.getLong(idCol)
            val name = it.getString(nameCol)
            val path = it.getString(pathCol)
            val duration = it.getLong(durCol)
            val size = it.getLong(sizeCol)
            val width = it.getInt(widthCol)
            val height = it.getInt(heightCol)
            val contentUri =
                ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)

            videos.add(VideoFile(id, name, path, duration, size, contentUri, width, height))
        }
    }
}

/**
 * Double-tap seek feedback — YouTube/MX Player style.
 *
 * No card, no border, no boxed chip: just a soft directional gradient wash
 * bleeding in from the edge of the screen, with a ripple of chevrons and a
 * time label floating directly on top of the glow. This mirrors how
 * YouTube/MX Player render their seek flash, themed in the app's aurora
 * teal / violet-whisper / platinum palette instead of flat white.
 *
 * Smoothness comes from layering two animations instead of one abrupt
 * fade-in/out:
 *  - `wash`: an Animatable that eases in fast and decays slowly, so the
 *    gradient blooms instantly on tap but melts away gradually rather than
 *    cutting off.
 *  - `ripple`: a continuously-restarting expand+fade pulse per tap (driven
 *    by `token`) layered under the chevrons, exactly like the concentric
 *    "ping" rings YouTube uses, but tinted with the brand gradient.
 *
 * Repeat taps (same side, before the previous animation settles) restart
 * `wash` from its *current* value rather than snapping to 0, so stacking
 * taps feels continuous instead of stuttering between two static states.
 */
@Composable
fun PremiumSeekFlash(
    visible: Boolean,
    token: Int,
    cumulativeSeconds: Int,
    isForward: Boolean,
    modifier: Modifier = Modifier
) {
    val wash = remember { Animatable(0f) }
    val ripple = remember { Animatable(0f) }

    LaunchedEffect(token, visible) {
        if (token > 0 && visible) {
            launch {
                wash.animateTo(1f, animationSpec = tween(120, easing = FastOutSlowInEasing))
                wash.animateTo(0f, animationSpec = tween(800, easing = FastOutSlowInEasing))
            }
            ripple.snapTo(0f)
            ripple.animateTo(1f, animationSpec = tween(650, easing = FastOutSlowInEasing))
        } else {
            // Kill animation if side switches
            launch { wash.animateTo(0f, tween(300)) }
            launch { ripple.animateTo(0f, tween(300)) }
        }
    }

    if (wash.value <= 0.001f && ripple.value <= 0.001f) return

    val edgeAlign = if (isForward) Alignment.CenterEnd else Alignment.CenterStart
    val sign = if (isForward) 1f else -1f

    Box(
        modifier = modifier
            .fillMaxHeight()
            .fillMaxWidth(0.45f),
        contentAlignment = edgeAlign
    ) {
        // ── Gradient wash, bleeding in from the screen edge ──────────────
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = if (isForward) {
                            listOf(
                                Color.Transparent,
                                BrandCyan.copy(alpha = 0.06f * wash.value),
                                BrandBlueDeep.copy(alpha = 0.16f * wash.value)
                            )
                        } else {
                            listOf(
                                BrandBlueDeep.copy(alpha = 0.16f * wash.value),
                                BrandCyan.copy(alpha = 0.06f * wash.value),
                                Color.Transparent
                            )
                        }
                    )
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 48.dp)
        ) {
            // ── Chevron ripple with custom playback icons ──────────────────
            Box(
                modifier = Modifier.size(width = 100.dp, height = 64.dp),
                contentAlignment = Alignment.Center
            ) {
                // Background expanding halo
                Box(
                    modifier = Modifier
                        .size(56.dp * ripple.value)
                        .graphicsLayer { alpha = (1f - ripple.value) * 0.4f * wash.value }
                        .background(BrandCyan.copy(alpha = 0.3f), CircleShape)
                )

                // Moving icon(s)
                val order = if (isForward) listOf(0, 1, 2) else listOf(2, 1, 0)
                order.forEach { i ->
                    val stagger = i * 0.15f
                    val local = ((ripple.value - stagger) / (1f - stagger)).coerceIn(0f, 1f)
                    val travel = local * 28.dp.value * sign
                    val alpha = (1f - local).coerceIn(0f, 1f) * wash.value

                    Icon(
                        painter = painterResource(if (isForward) R.drawable.ic_next_custom else R.drawable.ic_prev_custom),
                        contentDescription = null,
                        tint = BrandHighlight,
                        modifier = Modifier
                            .size(32.dp)
                            .offset(x = travel.dp)
                            .graphicsLayer { this.alpha = alpha }
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "${cumulativeSeconds}s",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.W800,
                    letterSpacing = (-0.3).sp,
                    brush = Brush.linearGradient(
                        listOf(Color.White, BrandCyan)
                    )
                ),
                modifier = Modifier.graphicsLayer {
                    alpha = wash.value
                    translationY = (1f - wash.value) * 8f
                }
            )
        }
    }
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

    androidx.compose.ui.window.Dialog(
        onDismissRequest = { savePrefs(); onDismiss() },
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        PremiumCardSurface(
            modifier = Modifier
                .fillMaxWidth(if (isLandscape) 0.6f else 0.95f)
                .padding(horizontal = 16.dp)
                .heightIn(max = configuration.screenHeightDp.dp * 0.95f),
            cornerRadius = 32.dp
        ) {
            Column(
                modifier = Modifier
                    .background(SurfaceNavy.copy(alpha = 0.95f))
                    .verticalScroll(rememberScrollState())
                    .padding(if (isLandscape) 16.dp else 24.dp),
                verticalArrangement = Arrangement.spacedBy(if (isLandscape) 12.dp else 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Subtitle Appearance",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White
                    )
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        BrandBlueDeep.copy(alpha = 0.60f),
                                        SurfaceNavy.copy(alpha = 0.45f)
                                    )
                                )
                            )
                            .border(
                                1.dp,
                                Brush.linearGradient(
                                    listOf(BrandCyan.copy(alpha = 0.28f), OutlineFaint)
                                ),
                                CircleShape
                            )
                            .clickable { savePrefs(); onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painterResource(R.drawable.ic_close_custom),
                            contentDescription = "Close",
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                HorizontalDivider(color = OutlineFaint)

                // Preview Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.Black.copy(alpha = bgAlpha.coerceAtLeast(0.2f)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, OutlineFaint)
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Sample subtitle text",
                            color = Color(textColor),
                            fontSize = fontSize.sp,
                            fontWeight = if (boldEnabled) FontWeight.Bold else FontWeight.Normal,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge
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
                            Text("Font Size", style = MaterialTheme.typography.labelLarge, color = TextSecondary)
                            Text("${fontSize.toInt()} sp", style = MaterialTheme.typography.labelMedium, color = BrandCyan)
                        }
                        Slider(
                            value = fontSize,
                            onValueChange = { fontSize = it },
                            valueRange = 12f..32f,
                            colors = SliderDefaults.colors(thumbColor = BrandCyan, activeTrackColor = BrandCyan)
                        )
                    }

                    // Opacity
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Background Opacity", style = MaterialTheme.typography.labelLarge, color = TextSecondary)
                            Text("${(bgAlpha * 100).toInt()}%", style = MaterialTheme.typography.labelMedium, color = BrandCyan)
                        }
                        Slider(
                            value = bgAlpha,
                            onValueChange = { bgAlpha = it },
                            valueRange = 0f..1f,
                            colors = SliderDefaults.colors(thumbColor = BrandCyan, activeTrackColor = BrandCyan)
                        )
                    }

                    // Colors
                    Column {
                        Text("Text Color", style = MaterialTheme.typography.labelLarge, color = TextSecondary)
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
                                            if (isSelected) BrandCyan else Color.Transparent,
                                            CircleShape
                                        )
                                        .clickable { textColor = colorInt },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(Icons.Default.Check, null, tint = Color.Black, modifier = Modifier.size(16.dp))
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
                        Text("Bold Style", style = MaterialTheme.typography.labelLarge, color = TextSecondary)
                        
                        // Custom Toggle using icons
                        val toggleIcon = if (boldEnabled) painterResource(R.drawable.ic_toggle_right_custom) 
                                         else painterResource(R.drawable.ic_toggle_left_custom)
                        val toggleTint by animateColorAsState(
                            targetValue = if (boldEnabled) BrandCyan else TextTertiary.copy(alpha = 0.6f),
                            animationSpec = tween(300),
                            label = "boldToggleTint"
                        )
                        
                        Icon(
                            painter = toggleIcon,
                            contentDescription = if (boldEnabled) "On" else "Off",
                            tint = toggleTint,
                            modifier = Modifier
                                .size(44.dp)
                                .clickable { boldEnabled = !boldEnabled }
                        )
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
}

@UnstableApi
@Composable
fun OnlineSubtitleSearchDialog(
    videoTitle: String,
    onDismiss: () -> Unit,
    onSubtitleSelected: (String) -> Unit
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    var searchQuery by remember { mutableStateOf(videoTitle.substringBeforeLast(".")) }
    var isSearching by remember { mutableStateOf(false) }
    var searchError by remember { mutableStateOf<String?>(null) }
    var results by remember { mutableStateOf<List<SubtitleResult>>(emptyList()) }
    var hasSearched by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun doSearch() {
        if (searchQuery.isBlank()) return
        isSearching = true
        searchError = null
        hasSearched = true
        scope.launch(Dispatchers.IO) {
            try {
                // OpenSubtitles REST API v1 — free, no API key required for basic search
                val encoded = java.net.URLEncoder.encode(searchQuery.trim(), "UTF-8")
                val url = java.net.URL("https://rest.opensubtitles.org/search/query-$encoded/sublanguageid-eng")
                val conn = url.openConnection() as java.net.HttpURLConnection
                conn.setRequestProperty("User-Agent", "CoverPlay v1.0")
                conn.connectTimeout = 8000
                conn.readTimeout = 8000
                val response = conn.inputStream.bufferedReader().readText()
                val json = org.json.JSONArray(response)
                val parsed = mutableListOf<SubtitleResult>()
                for (i in 0 until minOf(json.length(), 30)) {
                    val obj = json.getJSONObject(i)
                    val lang = obj.optString("LanguageName", "Unknown")
                    val releaseName = obj.optString("MovieReleaseName", obj.optString("SubFileName", "Unknown"))
                    val downloadUrl = obj.optString("SubDownloadLink", "")
                    val rating = obj.optDouble("SubRating", 0.0)
                    val downloads = obj.optInt("SubDownloadsCnt", 0)
                    if (downloadUrl.isNotEmpty()) {
                        parsed.add(SubtitleResult(lang, releaseName, downloadUrl, rating, downloads))
                    }
                }
                withContext(Dispatchers.Main) {
                    results = parsed
                    isSearching = false
                    if (parsed.isEmpty()) searchError = "No subtitles found for \"${searchQuery.trim()}\""
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    searchError = "Search failed: ${e.message ?: "Network error"}"
                    isSearching = false
                }
            }
        }
    }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        PremiumCardSurface(
            modifier = Modifier
                .fillMaxWidth(if (isLandscape) 0.6f else 0.95f)
                .padding(horizontal = 8.dp)
                .heightIn(max = configuration.screenHeightDp.dp * 0.95f),
            cornerRadius = 32.dp
        ) {
            Column(
                modifier = Modifier
                    .background(SurfaceNavy.copy(alpha = 0.95f))
                    .padding(if (isLandscape) 16.dp else 24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Cloud Subtitles",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White
                        )
                        Text(
                            "Searching OpenSubtitles.org",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextTertiary
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        BrandBlueDeep.copy(alpha = 0.60f),
                                        SurfaceNavy.copy(alpha = 0.45f)
                                    )
                                )
                            )
                            .border(
                                1.dp,
                                Brush.linearGradient(
                                    listOf(BrandCyan.copy(alpha = 0.28f), OutlineFaint)
                                ),
                                CircleShape
                            )
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painterResource(R.drawable.ic_close_custom),
                            contentDescription = "Close",
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(if (isLandscape) 12.dp else 20.dp))

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
                        placeholder = { Text("Movie or Show Title", color = TextTertiary) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandCyan,
                            unfocusedBorderColor = OutlineMedium,
                            cursorColor = BrandCyan,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp)
                    )
                    
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSearching) SurfaceRaisedAlt else BrandCyan.copy(alpha = 0.15f))
                            .clickable(enabled = !isSearching) { doSearch() },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSearching) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = BrandCyan, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Search, null, tint = BrandCyan, modifier = Modifier.size(22.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(if (isLandscape) 12.dp else 20.dp))
                HorizontalDivider(color = OutlineFaint)

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
                                    color = TextTertiary
                                )
                            }
                        }
                        else -> {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(vertical = 12.dp)
                            ) {
                                items(results) { sub ->
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                onSubtitleSelected(sub.downloadUrl)
                                                onDismiss()
                                            },
                                        shape = RoundedCornerShape(12.dp),
                                        color = SurfaceRaised.copy(alpha = 0.4f),
                                        border = BorderStroke(1.dp, OutlineFaint)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(14.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(sub.releaseName, color = Color.White, style = MaterialTheme.typography.bodyMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                                    Text(sub.language.uppercase(), color = BrandCyan, style = MaterialTheme.typography.labelSmall)
                                                    Text("★ ${sub.rating}", color = TextTertiary, style = MaterialTheme.typography.labelSmall)
                                                    Text("${sub.downloads} DLs", color = TextTertiary, style = MaterialTheme.typography.labelSmall)
                                                }
                                            }
                                            Icon(Icons.Default.Download, null, tint = BrandCyan, modifier = Modifier.size(20.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
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
}

data class SubtitleResult(
    val language: String,
    val releaseName: String,
    val downloadUrl: String,
    val rating: Double,
    val downloads: Int
)

fun formatDuration(durationMs: Long): String {
    val seconds = (durationMs / 1000) % 60
    val minutes = (durationMs / (1000 * 60)) % 60
    val hours = durationMs / (1000 * 60 * 60)
    return if (hours > 0) String.format("%dh %02dm %02ds", hours, minutes, seconds)
    else if (minutes > 0) String.format("%dm %02ds", minutes, seconds)
    else String.format("%ds", seconds)
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
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    // --- Reactive format state ---
    // exoPlayer.videoFormat is a snapshot; it can be null when the HUD opens
    // (player still buffering) and will never update the UI on its own once
    // composed. We track it in state so the HUD re-renders the moment the
    // format becomes available or changes (e.g. adaptive quality switch).
    var liveVideoFormat by remember { mutableStateOf(exoPlayer.videoFormat) }

    // MediaMetadataRetriever-based bitrate fallback for containers where
    // ExoPlayer reports Format.NO_VALUE (MKV, AVI, some MP4 variants that
    // don't embed per-track bitrate in the container header). Computed once
    // on a background thread so it never blocks composition.
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
            } catch (_: Exception) {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                // Intercept all touches so they don't fall through to gestures (double-tap, swipe)
                // but a single tap on the background can dismiss.
                detectTapGestures(onTap = { onDismiss() })
            }
            .padding(16.dp),
        contentAlignment = if (isLandscape) Alignment.CenterEnd else Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .width(if (isLandscape) 260.dp else 280.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            SurfaceNavy.copy(alpha = 0.92f),
                            BrandBlueDeep.copy(alpha = 0.85f)
                        )
                    )
                )
                .border(
                    1.dp,
                    Brush.linearGradient(
                        listOf(BrandCyan.copy(alpha = 0.4f), Color.Transparent)
                    ),
                    RoundedCornerShape(24.dp)
                )
                .clickable(enabled = false) { } // prevent dismiss when clicking inside
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "CoverPlay Intelligence",
                    style = MaterialTheme.typography.sectionOverline.copy(
                        color = BrandHighlight,
                        letterSpacing = 1.5.sp
                    )
                )

                // Premium Close Button
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    BrandBlueDeep.copy(alpha = 0.60f),
                                    SurfaceNavy.copy(alpha = 0.45f)
                                )
                            )
                        )
                        .border(
                            1.dp,
                            Brush.linearGradient(
                                listOf(BrandCyan.copy(alpha = 0.28f), OutlineFaint)
                            ),
                            CircleShape
                        )
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_close_custom),
                        contentDescription = "Close",
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            HorizontalDivider(color = BrandCyan.copy(alpha = 0.2f))

            // Metadata Grid - Wrapped in Column with Scroll if needed
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HUDInfoItem("Resolution", videoSize?.let { "${it.width} × ${it.height}" } ?: "Detecting...")
                HUDInfoItem("Format", video.path.substringAfterLast(".").uppercase())
                HUDInfoItem("File Size", formatSize(video.size))

                // Stream info — reads from reactive liveVideoFormat state so the
                // HUD updates automatically if the format resolves after opening.
                val fmt = liveVideoFormat
                if (fmt != null) {
                    HUDInfoItem(
                        "Codec",
                        fmt.sampleMimeType?.substringAfterLast("/")?.uppercase() ?: "Unknown"
                    )

                    // Frame rate: keep one decimal place so 23.976, 29.97, 59.94
                    // are shown correctly instead of being truncated to wrong integers.
                    if (fmt.frameRate > 0f) {
                        val fpsText = if (fmt.frameRate % 1f == 0f) {
                            "${fmt.frameRate.toInt()} FPS"
                        } else {
                            String.format("%.3g FPS", fmt.frameRate)
                        }
                        HUDInfoItem("Frame Rate", fpsText)
                    }

                    // Bitrate fallback chain:
                    //  1. ExoPlayer track format bitrate (most accurate — set by demuxer)
                    //  2. MediaMetadataRetriever total bitrate (available for more containers)
                    //  3. Estimate from file size ÷ duration (always available as a last resort)
                    val fmtBitrateKbps = if (fmt.bitrate > 0) fmt.bitrate / 1000 else 0
                    val bitrateKbps = when {
                        fmtBitrateKbps > 0 -> fmtBitrateKbps
                        retrieverBitrateKbps > 0 -> retrieverBitrateKbps
                        video.duration > 0L -> ((video.size * 8L) / video.duration).toInt()
                        else -> 0
                    }
                    if (bitrateKbps > 0) {
                        val bitrateText = if (bitrateKbps >= 1000) {
                            String.format("%.2f Mbps", bitrateKbps / 1000f)
                        } else {
                            "$bitrateKbps kbps"
                        }
                        HUDInfoItem("Bitrate", bitrateText)
                    } else {
                        HUDInfoItem("Bitrate", "Calculating...")
                    }
                } else {
                    HUDInfoItem("Codec", "Detecting...")
                    HUDInfoItem("Frame Rate", "Detecting...")
                    HUDInfoItem("Bitrate", "Detecting...")
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Status Badges
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (videoSize != null && videoSize.width >= 3840) {
                    HUDChip("4K ULTRA HD", BrandHighlight)
                } else if (videoSize != null && videoSize.width >= 1920) {
                    HUDChip("FULL HD", BrandCyan)
                }

                if (video.duration > 3600000) {
                    HUDChip("FEATURE", BrandIceBlue)
                }
            }
        }
    }
}

@Composable
private fun HUDInfoItem(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextTertiary)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun HUDChip(text: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(0.5.dp, color.copy(alpha = 0.5f))
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            fontWeight = FontWeight.Bold
        )
    }
}