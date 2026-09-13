package com.example.myapplication

import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.material3.ExperimentalMaterial3Api
import android.net.Uri
import android.os.Build
import android.util.Size
import android.app.Activity
import java.io.File
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.graphics.drawable.toDrawable
import coil.ImageLoader
import coil.decode.DataSource
import coil.fetch.DrawableResult
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.request.Options
import coil.size.pxOrElse
import coil.memory.MemoryCache
import coil.disk.DiskCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.derivedStateOf
import android.content.Context
import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import kotlinx.coroutines.delay
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.ui.PlayerView
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.PlaylistPlay
import androidx.compose.material.icons.automirrored.outlined.PlaylistPlay
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ViewList
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.util.UnstableApi
import com.example.myapplication.ui.theme.*
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.videoFrameMillis

// ── Resolution / quality label ─────────────────────────────────────────────
fun qualityLabel(width: Int, height: Int): String {
    val longerEdge = maxOf(width, height)
    return when {
        longerEdge >= 3840 -> "4K"
        longerEdge >= 1920 -> "FHD"
        longerEdge >= 1280 -> "HD"
        else                -> "SD"
    }
}

@Composable
fun QualityBadge(width: Int, height: Int, modifier: Modifier = Modifier) {
    val label = qualityLabel(width, height)
    val is4K = label == "4K"

    Surface(
        color = if (is4K) BrandOrange.copy(alpha = 0.15f) else BrandCyan.copy(alpha = 0.12f),
        shape = CircleShape,
        modifier = modifier,
        border = BorderStroke(1.dp, if (is4K) BrandOrange.copy(alpha = 0.5f) else BrandCyan.copy(alpha = 0.3f))
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp
            ),
            color = if (is4K) BrandOrange else BrandCyan,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun HierarchyDurationBadge(text: String, modifier: Modifier = Modifier) {
    Surface(
        color = BrandAccent.copy(alpha = 0.12f),
        shape = CircleShape,
        modifier = modifier,
        border = BorderStroke(1.dp, BrandAccent.copy(alpha = 0.3f))
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.3.sp
            ),
            color = BrandAccent,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

class VideoThumbnailFetcher(private val uri: Uri, private val options: Options, private val context: Context) : Fetcher {
    override suspend fun fetch(): FetchResult? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val width = options.size.width.pxOrElse { 512 }; val height = options.size.height.pxOrElse { 320 }
                val bitmap = context.contentResolver.loadThumbnail(uri, Size(width, height), null)
                DrawableResult(drawable = bitmap.toDrawable(context.resources), isSampled = true, dataSource = DataSource.DISK)
            } else null
        } catch (e: Exception) { null }
    }
    class Factory(private val context: Context) : Fetcher.Factory<Uri> {
        override fun create(data: Uri, options: Options, imageLoader: ImageLoader): Fetcher? {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || data.scheme != "content") return null

            // If the request specifically asks for a frame (scrubbing), let
            // the real VideoFrameDecoder handle it instead of the system
            // loadThumbnail() which only gives us frame 0.
            if (options.parameters.values().keys.any { it.contains("video_frame") }) {
                return null
            }

            return VideoThumbnailFetcher(data, options, context)
        }
    }
}

@Composable
fun BottomNavigationBar(currentScreen: Screen, onScreenSelected: (Screen) -> Unit) {
    val items = listOf(
        NavItem(Screen.HOME,      "Home",      R.drawable.ic_nav_home_filled,      R.drawable.ic_nav_home_unfilled),
        NavItem(Screen.FOLDERS,   "Folders",   R.drawable.ic_nav_folder_filled,    R.drawable.ic_nav_folder_unfilled),
        NavItem(Screen.PLAYLISTS, "Playlists", R.drawable.ic_nav_playlist_filled,  R.drawable.ic_nav_playlist_unfilled),
        NavItem(Screen.SETTINGS,  "Settings",  R.drawable.ic_nav_settings_filled,  R.drawable.ic_nav_settings_unfilled)
    )

    val settings = LocalAppUiSettings.current
    val haptic = LocalHapticFeedback.current

    Box(
        modifier = Modifier
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 24.dp)
            .fillMaxWidth()
            .height(64.dp),
        contentAlignment = Alignment.Center
    ) {
        // 1. Shadow & Blur Layer (Halo effect around the bar)
        Box(
            modifier = Modifier
                .matchParentSize()
                .shadow(
                    elevation = 28.dp,
                    shape = CircleShape,
                    spotColor = Color.Black.copy(alpha = 0.4f),
                    ambientColor = Color.Black.copy(alpha = 0.2f)
                )
                .blur(radius = 16.dp)
                .background(settings.palette.surfaceRaisedAlt.copy(alpha = 0.6f), CircleShape)
        )

        // 2. Main Navigation Bar Layer
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(settings.palette.surfaceRaisedAlt.copy(alpha = 0.94f), CircleShape)
                .border(1.dp, settings.palette.outlineMedium.copy(alpha = 0.4f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .padding(4.dp)
                    .fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    val isSelected = when (item.screen) {
                        Screen.FOLDERS -> currentScreen == Screen.FOLDERS || currentScreen == Screen.FOLDER_VIDEOS
                        else -> currentScreen == item.screen
                    }

                    val bg by animateColorAsState(
                        targetValue = if (isSelected) settings.accent else Color.Transparent,
                        animationSpec = tween(400, easing = FastOutSlowInEasing),
                        label = "navBg"
                    )
                    val contentColor by animateColorAsState(
                        targetValue = if (isSelected) Color.White else settings.palette.textSecondary,
                        animationSpec = tween(400),
                        label = "navContent"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(CircleShape)
                            .background(bg)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onScreenSelected(item.screen)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        val iconScale by animateFloatAsState(
                            targetValue = if (isSelected) 1.15f else 1.0f,
                            animationSpec = spring(dampingRatio = 0.8f, stiffness = 350f),
                            label = "iconScale"
                        )
                        Icon(
                            painter = painterResource(if (isSelected) item.filledIcon else item.outlineIcon),
                            contentDescription = item.label,
                            tint = contentColor,
                            modifier = Modifier
                                .size(24.dp)
                                .graphicsLayer(scaleX = iconScale, scaleY = iconScale)
                        )
                    }
                }
            }
        }
    }
}

private data class NavItem(val screen: Screen, val label: String, val filledIcon: Int, val outlineIcon: Int)

@Composable
fun SignatureTitle(title: String) {
    val settings = LocalAppUiSettings.current
    if (title == "CoverPlay") {
        var startAnim by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { startAnim = true }

        val alpha by animateFloatAsState(
            targetValue = if (startAnim) 1f else 0f,
            animationSpec = tween(800)
        )
        val translateY by animateDpAsState(
            targetValue = if (startAnim) 0.dp else (-8).dp,
            animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f)
        )
        val scale by animateFloatAsState(
            targetValue = if (startAnim) 1f else 0.92f,
            animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f)
        )

        Row(
            modifier = Modifier.graphicsLayer {
                this.alpha = alpha
                this.translationY = translateY.toPx()
                this.scaleX = scale
                this.scaleY = scale
            },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Cover",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = settings.palette.textPrimary
            )

            // "Play" part with slightly delayed entrance
            val playAlpha by animateFloatAsState(
                targetValue = if (startAnim) 1f else 0f,
                animationSpec = tween(600, delayMillis = 250)
            )
            val playOffset by animateDpAsState(
                targetValue = if (startAnim) 0.dp else 6.dp,
                animationSpec = spring(dampingRatio = 0.6f, stiffness = 200f)
            )

            Text(
                text = "Play",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = settings.accent,
                modifier = Modifier.graphicsLayer {
                    this.alpha = playAlpha
                    this.translationX = playOffset.toPx()
                }
            )
        }
    } else {
        Text(title, fontWeight = FontWeight.Bold, color = settings.palette.textPrimary)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopAppBar(
    title: String,
    isSearchActive: Boolean = false,
    searchQuery: String = "",
    onSearchQueryChange: (String) -> Unit = {},
    onToggleSearch: () -> Unit = {},
    showBackButton: Boolean = false,
    onBack: () -> Unit = {},
    currentLayout: LayoutMode = LayoutMode.LIST,
    onToggleLayout: () -> Unit = {},
    onShowFilter: () -> Unit = {},
    showExtraActions: Boolean = true,
    isScrolled: Boolean = false
) {
    val focusRequester = remember { FocusRequester() }
    val settings = LocalAppUiSettings.current
    val haptic = LocalHapticFeedback.current

    Box(modifier = Modifier.fillMaxWidth()) {
        // 1. Premium Glassmorphic Layer
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    if (isSearchActive) settings.palette.bg 
                    else settings.palette.bg.copy(alpha = 0.88f)
                )
                .then(
                    if (isScrolled && !isSearchActive) {
                        Modifier.shadow(
                            elevation = 8.dp,
                            spotColor = Color(0xFF000014).copy(alpha = 0.1f),
                            ambientColor = Color.Transparent
                        )
                    } else Modifier
                )
        )

        // 2. Refined Hairline Border (Visible on scroll)
        if (isScrolled && !isSearchActive) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.8.dp)
                    .align(Alignment.BottomCenter)
                    .background(settings.palette.outlineMedium.copy(alpha = 0.15f))
            )
        }

        TopAppBar(
            title = {
                AnimatedContent(
                    targetState = isSearchActive,
                    transitionSpec = {
                        if (targetState) {
                            (fadeIn(tween(400)) + slideInHorizontally { it / 3 } + expandHorizontally(expandFrom = Alignment.End, animationSpec = spring(0.75f, 400f))) togetherWith
                                    (fadeOut(tween(300)) + slideOutHorizontally { -it / 2 })
                        } else {
                            (fadeIn(tween(400)) + slideInHorizontally { -it / 3 }) togetherWith
                                    (fadeOut(tween(300)) + slideOutHorizontally { it / 2 } + shrinkHorizontally(shrinkTowards = Alignment.End))
                        }
                    },
                    label = "searchExpansion"
                ) { active ->
                    if (active) {
                        LaunchedEffect(Unit) { focusRequester.requestFocus() }
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChange,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .focusRequester(focusRequester)
                                .background(settings.palette.surfaceRaisedAlt.copy(alpha = 0.6f), CircleShape)
                                .padding(horizontal = 20.dp),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(color = settings.palette.textPrimary),
                            cursorBrush = SolidColor(settings.accent),
                            singleLine = true,
                            decorationBox = { innerTextField ->
                                Box(contentAlignment = Alignment.CenterStart) {
                                    if (searchQuery.isEmpty()) {
                                        Text("Search videos...", color = settings.palette.textTertiary, style = MaterialTheme.typography.bodyMedium)
                                    }
                                    innerTextField()
                                }
                            }
                        )
                    } else {
                        SignatureTitle(title)
                    }
                }
            },
            navigationIcon = {
                if (showBackButton && !isSearchActive) {
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onBack()
                    }) {
                        Icon(painterResource(R.drawable.ic_back_custom), null, tint = settings.palette.textPrimary)
                    }
                }
            },
            actions = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Morphing Search icon
                    val rotation by animateFloatAsState(
                        targetValue = if (isSearchActive) 90f else 0f,
                        animationSpec = spring(dampingRatio = 0.7f, stiffness = 300f)
                    )

                    val interactionSource = remember { MutableInteractionSource() }
                    val isPressed by interactionSource.collectIsPressedAsState()
                    val pressRotation by animateFloatAsState(
                        targetValue = if (isPressed) 45f else 0f,
                        animationSpec = spring(dampingRatio = 0.4f, stiffness = Spring.StiffnessLow),
                        label = "searchPressRotation"
                    )

                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onToggleSearch()
                        },
                        interactionSource = interactionSource
                    ) {
                        Icon(
                            painter = painterResource(if (isSearchActive) R.drawable.ic_close_custom else R.drawable.ic_search_custom),
                            contentDescription = "Search",
                            modifier = Modifier.graphicsLayer { rotationZ = rotation + pressRotation },
                            tint = if (isSearchActive) settings.palette.textSecondary else settings.palette.textPrimary
                        )
                    }

                    if (!isSearchActive && showExtraActions) {
                        IconButton(onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onToggleLayout()
                        }) {
                            Icon(painterResource(if (currentLayout == LayoutMode.LIST) R.drawable.ic_grid_custom else R.drawable.ic_list_custom), null, tint = settings.palette.textPrimary)
                        }
                        IconButton(onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onShowFilter()
                        }) {
                            Icon(painterResource(R.drawable.ic_filter_custom), null, tint = settings.palette.textPrimary)
                        }
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
        )
    }
}

@androidx.media3.common.util.UnstableApi
@Composable
fun DashboardScreen(
    recentEntries: List<RecentEntry>,
    allVideos: List<VideoFile>,
    allFolders: List<VideoFolder>,
    isSearchActive: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onToggleSearch: () -> Unit,
    onVideoClick: (VideoFile) -> Unit,
    onVideoChanged: () -> Unit = {},
    onPlayUrl: (String) -> Unit = {}
) {
    var actionVideo by remember { mutableStateOf<VideoFile?>(null) }
    var activeDialog by remember { mutableStateOf<VideoActionDialog?>(null) }
    var expandedVideoId by remember { mutableStateOf<Long?>(null) }

    var sortOrder      by remember { mutableStateOf(VideoSortOrder.DATE_NEWEST) }
    var layoutMode     by remember { mutableStateOf(LayoutMode.LIST) }
    var showUrlDialog  by remember { mutableStateOf(false) }
    var showFilterSheet by remember { mutableStateOf(false) }
    
    val scrollState = androidx.compose.foundation.lazy.rememberLazyListState()
    val isScrolled by remember {
        derivedStateOf { scrollState.firstVisibleItemIndex > 0 || scrollState.firstVisibleItemScrollOffset > 0 }
    }

    val filteredVideos by remember(allVideos, searchQuery, sortOrder) {
        derivedStateOf {
            val base = if (searchQuery.isEmpty()) allVideos
            else allVideos.filter { it.name.contains(searchQuery, ignoreCase = true) }

            when (sortOrder) {
                VideoSortOrder.NAME_ASC -> base.sortedBy { it.name.lowercase() }
                VideoSortOrder.NAME_DESC -> base.sortedByDescending { it.name.lowercase() }
                VideoSortOrder.DATE_NEWEST -> base.reversed()
                VideoSortOrder.SIZE_DESC -> base.sortedByDescending { it.size }
                VideoSortOrder.DURATION_DESC -> base.sortedByDescending { it.duration }
            }
        }
    }

    if (showUrlDialog) {
        PlayUrlDialog(
            onDismiss = { showUrlDialog = false },
            onPlay = { url ->
                showUrlDialog = false
                onPlayUrl(url)
            }
        )
    }

    if (showFilterSheet) {
        VideoFilterSortSheet(
            currentSort = sortOrder,
            currentLayout = layoutMode,
            onSortSelected = { sortOrder = it },
            onLayoutSelected = { layoutMode = it },
            onDismiss = { showFilterSheet = false }
        )
    }

    val settings = LocalAppUiSettings.current

    var showContent by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(100)
        showContent = true
    }

    Scaffold(
        topBar = {
            MainTopAppBar(
                title = "CoverPlay",
                isSearchActive = isSearchActive,
                searchQuery = searchQuery,
                onSearchQueryChange = onSearchQueryChange,
                onToggleSearch = onToggleSearch,
                currentLayout = layoutMode,
                onToggleLayout = { layoutMode = if (layoutMode == LayoutMode.LIST) LayoutMode.GRID else LayoutMode.LIST },
                onShowFilter = { showFilterSheet = true },
                showExtraActions = false,
                isScrolled = isScrolled
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                state = scrollState,
                modifier = Modifier.fillMaxSize().padding(bottom = padding.calculateBottomPadding()),
                contentPadding = PaddingValues(top = padding.calculateTopPadding(), bottom = 100.dp)
            ) {
                if (!isSearchActive || searchQuery.isEmpty()) {
                    item {
                        AnimatedVisibility(
                            visible = showContent,
                            enter = slideInVertically { 40 } + fadeIn(),
                            label = "urlEntrance"
                        ) {
                            val haptic = LocalHapticFeedback.current
                            Surface(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth()
                                    .clip(CircleShape)
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        showUrlDialog = true
                                    },
                                color = settings.palette.surfaceRaisedAlt,
                                border = BorderStroke(1.dp, settings.palette.outlineMedium),
                                shape = CircleShape
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    BrandIconContainer(active = true, size = 44.dp, cornerRadius = 14.dp) {
                                        Icon(Icons.Rounded.Link, null, tint = Color.White)
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text("Stream URL", style = MaterialTheme.typography.titleMedium, color = settings.palette.textPrimary, fontWeight = FontWeight.Black)
                                        Text("Play remote links", style = MaterialTheme.typography.bodySmall, color = settings.palette.textSecondary)
                                    }
                                }
                            }
                        }
                    }

                    if (recentEntries.isNotEmpty()) {
                        item {
                            AnimatedVisibility(
                                visible = showContent,
                                enter = slideInVertically { 60 } + fadeIn(tween(600, delayMillis = 100)),
                                label = "recentsEntrance"
                            ) {
                                Column {
                                    SectionLabel("Continue Watching")
                                    LazyRow(
                                        contentPadding = PaddingValues(horizontal = 16.dp),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        itemsIndexed(recentEntries, key = { _, it -> it.video.id }) { index, entry ->
                                            val entryAnimation = remember { Animatable(0f) }
                                            LaunchedEffect(Unit) {
                                                delay(index * 100L)
                                                entryAnimation.animateTo(1f, animationSpec = MotionSpec.EmphasizedSpring)
                                            }
                                            RecentVideoCard(
                                                video = entry.video,
                                                onClick = { onVideoClick(entry.video) },
                                                modifier = Modifier.graphicsLayer {
                                                    scaleX = 0.8f + (entryAnimation.value * 0.2f)
                                                    scaleY = 0.8f + (entryAnimation.value * 0.2f)
                                                    alpha = entryAnimation.value
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    item {
                        AnimatedVisibility(
                            visible = showContent,
                            enter = slideInVertically { 80 } + fadeIn(tween(600, delayMillis = 200)),
                            label = "libraryEntrance"
                        ) {
                            SectionLabel("Library")
                        }
                    }
                }

                if (filteredVideos.isEmpty()) {
                    item {
                        EmptyState(
                            title = "No Videos Found",
                            subtitle = "Try a different search or check your folders"
                        ) {
                            Icon(painterResource(R.drawable.ic_search_custom), null, modifier = Modifier.size(56.dp), tint = LocalAppUiSettings.current.palette.textTertiary)
                        }
                    }
                } else {
                    if (layoutMode == LayoutMode.LIST) {
                        itemsIndexed(filteredVideos, key = { _, it -> it.id }) { index, video ->
                            val entryAnimation = remember { Animatable(0f) }
                            LaunchedEffect(Unit) {
                                delay(index * 50L)
                                entryAnimation.animateTo(1f, animationSpec = MotionSpec.SoftSpring)
                            }
                            Box(modifier = Modifier.graphicsLayer {
                                translationY = 50f * (1f - entryAnimation.value)
                                alpha = entryAnimation.value
                            }) {
                                VideoItem(
                                    video = video,
                                    isExpanded = expandedVideoId == video.id,
                                    onToggle = {
                                        expandedVideoId = if (expandedVideoId == video.id) null else video.id
                                    },
                                    onClick = { onVideoClick(video) },
                                    onLongClick = { actionVideo = video }
                                )
                            }
                        }
                    } else {
                        item {
                            FlowRow(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                maxItemsInEachRow = 2
                            ) {
                                for (video in filteredVideos) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        VideoGridItem(video = video, onClick = { onVideoClick(video) }, onLongClick = { actionVideo = video })
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Top Gradient Fade
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(settings.palette.bg, Color.Transparent)
                        )
                    )
            )
        }
    }
    VideoActionsHost(actionVideo = actionVideo, activeDialog = activeDialog, allFolders = allFolders, onDismiss = { actionVideo = null; activeDialog = null }, onPickDialog = { activeDialog = it }, onVideoChanged = { expandedVideoId = null; onVideoChanged() }, onPlayVideo = onVideoClick)
}

@androidx.media3.common.util.UnstableApi
@Composable
fun RecentVideoCard(video: VideoFile, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val settings = LocalAppUiSettings.current
    val haptic = LocalHapticFeedback.current
    val prefs = context.getSharedPreferences("playback_positions", Context.MODE_PRIVATE)
    val savedPos = prefs.getLong(video.path, 0L)
    val progress = if (video.duration > 0) savedPos.toFloat() / video.duration.toFloat() else 0f

    PremiumCardSurface(
        modifier = modifier.width(280.dp).height(180.dp),
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        cornerRadius = 36.dp
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
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
            Box(
                modifier = Modifier.fillMaxSize().background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                        startY = 0.3f
                    )
                )
            )
            if (LocalAppUiSettings.current.showDurationBadge) {
                DurationBadge(
                    text = formatDuration(video.duration),
                    modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
                )
            }
            Column(modifier = Modifier.align(Alignment.BottomStart).padding(20.dp)) {
                Text(
                    text = formatFileName(video.name),
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                    color = settings.accent,
                    trackColor = settings.palette.outlineFaint.copy(alpha = 0.5f)
                )
            }
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                BrandPlayOverlay(size = 48.dp)
            }
        }
    }
}

@Composable
fun VideoGridItem(video: VideoFile, onClick: () -> Unit, onLongClick: () -> Unit = {}) {
    val context = LocalContext.current
    PremiumCardSurface(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        onLongClick = onLongClick,
        cornerRadius = 28.dp
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().aspectRatio(16/9f)) {
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
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                    BrandPlayOverlay(size = 36.dp)
                }
                if (LocalAppUiSettings.current.showDurationBadge) {
                    DurationBadge(
                        text = formatDuration(video.duration),
                        modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp)
                    )
                }
            }
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = formatFileName(video.name),
                    style = MaterialTheme.typography.bodyMedium,
                    color = LocalAppUiSettings.current.palette.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (video.width > 0) {
                        QualityBadge(width = video.width, height = video.height)
                    }
                }
            }
        }
    }
}

@Composable
fun FolderGridItem(folder: VideoFolder, onClick: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    PremiumCardSurface(
        modifier = Modifier.fillMaxWidth().aspectRatio(1f),
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        cornerRadius = 32.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            BrandIconContainer(active = true, size = 64.dp, cornerRadius = 24.dp) {
                Icon(painterResource(R.drawable.ic_folder_custom), null, tint = Color.White, modifier = Modifier.size(32.dp))
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = folder.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            Text(
                text = "${folder.videoCount} Videos",
                style = MaterialTheme.typography.labelSmall,
                color = LocalAppUiSettings.current.palette.textSecondary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@androidx.media3.common.util.UnstableApi
@Composable
fun FolderListScreen(
    folders: List<VideoFolder>,
    allVideos: List<VideoFile>,
    isSearchActive: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onToggleSearch: () -> Unit,
    onFolderClick: (VideoFolder) -> Unit,
    onVideoClick: (VideoFile) -> Unit,
    onVideoChanged: () -> Unit = {},
    onPlayUrl: (String) -> Unit = {}
) {
    var sortOrder      by remember { mutableStateOf(FolderSortOrder.NAME_ASC) }
    var layoutMode     by remember { mutableStateOf(LayoutMode.LIST) }
    var showFilterSheet by remember { mutableStateOf(false) }

    val scrollState = androidx.compose.foundation.lazy.rememberLazyListState()
    val isScrolled by remember {
        derivedStateOf { scrollState.firstVisibleItemIndex > 0 || scrollState.firstVisibleItemScrollOffset > 0 }
    }

    if (showFilterSheet) {
        FilterSortSheet(
            currentSort = sortOrder,
            currentLayout = layoutMode,
            onSortSelected = { sortOrder = it },
            onLayoutSelected = { layoutMode = it },
            onDismiss = { showFilterSheet = false }
        )
    }

    Scaffold(
        topBar = {
            MainTopAppBar(
                title = "Folders",
                isSearchActive = isSearchActive,
                searchQuery = searchQuery,
                onSearchQueryChange = onSearchQueryChange,
                onToggleSearch = onToggleSearch,
                currentLayout = layoutMode,
                onToggleLayout = { layoutMode = if (layoutMode == LayoutMode.LIST) LayoutMode.GRID else LayoutMode.LIST },
                onShowFilter = { showFilterSheet = true },
                isScrolled = isScrolled
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        if (folders.isEmpty()) {
            EmptyState(title = "No Folders", subtitle = "Scanning for videos...") {
                CircularProgressIndicator(color = BrandAccent)
            }
        } else {
            val filteredFolders by remember(folders, searchQuery, sortOrder) {
                derivedStateOf {
                    val base = if (searchQuery.isEmpty()) folders
                    else folders.filter { it.name.contains(searchQuery, ignoreCase = true) }

                    when (sortOrder) {
                        FolderSortOrder.NAME_ASC -> base.sortedBy { it.name.lowercase() }
                        FolderSortOrder.NAME_DESC -> base.sortedByDescending { it.name.lowercase() }
                        FolderSortOrder.MOST_VIDEOS -> base.sortedByDescending { it.videoCount }
                        FolderSortOrder.FEWEST_VIDEOS -> base.sortedBy { it.videoCount }
                    }
                }
            }
            Box(modifier = Modifier.fillMaxSize()) {
                if (layoutMode == LayoutMode.LIST) {
                    LazyColumn(
                        state = scrollState,
                        modifier = Modifier.fillMaxSize().padding(bottom = padding.calculateBottomPadding()),
                        contentPadding = PaddingValues(top = padding.calculateTopPadding(), bottom = 100.dp)
                    ) {
                        itemsIndexed(filteredFolders, key = { _, it -> it.path }) { index, folder ->
                            val entryAnimation = remember { Animatable(0f) }
                            LaunchedEffect(Unit) {
                                delay(index * 30L)
                                entryAnimation.animateTo(1f, animationSpec = MotionSpec.SoftSpring)
                            }
                            PremiumCardSurface(
                                modifier = Modifier
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                                    .fillMaxWidth()
                                    .graphicsLayer {
                                        translationY = 30f * (1f - entryAnimation.value)
                                        alpha = entryAnimation.value
                                    },
                                cornerRadius = 24.dp,
                                onClick = { onFolderClick(folder) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    BrandIconContainer(active = true, cornerRadius = 16.dp, size = 48.dp) {
                                        Icon(painterResource(R.drawable.ic_folder_custom), null, tint = Color.White)
                                    }
                                    Spacer(modifier = Modifier.width(20.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = folder.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Black,
                                            color = LocalAppUiSettings.current.palette.textPrimary
                                        )
                                        Text(
                                            text = "${folder.videoCount} Videos",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = LocalAppUiSettings.current.palette.textSecondary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Icon(
                                        painterResource(R.drawable.ic_chevron_right_custom),
                                        null,
                                        tint = LocalAppUiSettings.current.palette.textTertiary.copy(alpha = 0.6f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        state = androidx.compose.foundation.lazy.grid.rememberLazyGridState(),
                        modifier = Modifier.fillMaxSize().padding(bottom = padding.calculateBottomPadding()),
                        contentPadding = PaddingValues(top = padding.calculateTopPadding(), start = 16.dp, end = 16.dp, bottom = 100.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(filteredFolders, key = { it.path }) { folder ->
                            FolderGridItem(folder = folder, onClick = { onFolderClick(folder) })
                        }
                    }
                }

                // Top Gradient Fade
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(LocalAppUiSettings.current.palette.bg, Color.Transparent)
                            )
                        )
                )
            }
        }
    }
}

@androidx.media3.common.util.UnstableApi
@Composable
fun VideoItem(
    video: VideoFile,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {}
) {
    val settings = LocalAppUiSettings.current
    val isDark = settings.palette.isDark
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    var frameRate by remember(video.uri) { mutableStateOf<String?>(null) }
    LaunchedEffect(isExpanded) {
        if (isExpanded && frameRate == null) {
            withContext(Dispatchers.IO) {
                try {
                    val retriever = android.media.MediaMetadataRetriever()
                    retriever.setDataSource(context, video.uri)
                    val fps = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE)
                        ?: retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_FRAME_COUNT)?.let { count ->
                            retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)?.let { dur ->
                                val d = dur.toFloat()
                                if (d > 0) (count.toFloat() / (d / 1000f)).toInt().toString() else null
                            }
                        }
                    // Clean Frame Rate: 1 decimal place
                    frameRate = fps?.let {
                        val f = it.toFloatOrNull()
                        if (f != null) String.format("%.1f FPS", f) else "$it FPS"
                    } ?: "30 FPS"
                    retriever.release()
                } catch (e: Exception) { frameRate = "30 FPS" }
            }
        }
    }

    // Expressive Smooth expansion
    val collapsedHeight = if (settings.compactList) 76.dp else 92.dp

    val surfaceModifier = if (isExpanded) {
        Modifier
            .padding(horizontal = 14.dp, vertical = if (settings.compactList) 4.dp else 6.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onToggle()
            }
    } else {
        Modifier
            .padding(horizontal = 14.dp, vertical = if (settings.compactList) 4.dp else 6.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .combinedClickable(
                onClick = {
                    if (isExpanded) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onToggle()
                    } else {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onClick()
                    }
                },
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClick()
                }
            )
    }

    Surface(
        modifier = surfaceModifier,
        color = if (isDark) settings.palette.surfaceRaisedAlt.copy(alpha = if(isExpanded) 0.98f else 0.7f)
        else settings.palette.surfaceRaisedAlt,
        shape = RoundedCornerShape(32.dp),
        border = BorderStroke(
            if(isExpanded) 2.dp else 1.dp,
            if(isExpanded) settings.accent.copy(alpha = 0.8f) else settings.palette.outlineMedium.copy(alpha = 0.4f)
        ),
        tonalElevation = 0.dp,
        shadowElevation = if (isExpanded) 24.dp else (if (isDark) 4.dp else 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().animateContentSize(
            animationSpec = spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessLow)
        )) {
            // ── Top Section ──────────────────────────────────────────────
            if (isExpanded) {
                // Autoplay Preview Player
                var player by remember { mutableStateOf<ExoPlayer?>(null) }
                var videoAlpha by remember { mutableStateOf(0f) }
                val animatedAlpha by animateFloatAsState(targetValue = videoAlpha, animationSpec = tween(1000), label = "videoFade")

                // Technical data state
                var videoWidth by remember { mutableIntStateOf(video.width) }
                var videoHeight by remember { mutableIntStateOf(video.height) }
                var videoDuration by remember { mutableLongStateOf(video.duration) }

                LaunchedEffect(isExpanded) {
                    if (isExpanded && (videoWidth == 0 || videoDuration == 0L)) {
                        withContext(Dispatchers.IO) {
                            try {
                                val retriever = android.media.MediaMetadataRetriever()
                                retriever.setDataSource(context, video.uri)
                                val w = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toInt() ?: 0
                                val h = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toInt() ?: 0
                                val d = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: 0L
                                videoWidth = w
                                videoHeight = h
                                videoDuration = d
                                retriever.release()
                            } catch (e: Exception) {}
                        }
                    }
                }

                DisposableEffect(video.uri) {
                    val exoPlayer = ExoPlayer.Builder(context).build().apply {
                        setMediaItem(MediaItem.fromUri(video.uri))
                        volume = 0f // Muted autoplay
                        repeatMode = Player.REPEAT_MODE_ALL
                        playWhenReady = true
                        addListener(object : Player.Listener {
                            override fun onPlaybackStateChanged(state: Int) {
                                if (state == Player.STATE_READY) videoAlpha = 1f
                            }
                        })
                        prepare()
                    }
                    player = exoPlayer
                    onDispose { exoPlayer.release(); player = null }
                }

                Column(modifier = Modifier.padding(20.dp)) {
                    // Header: Title + Collapse
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = formatFileName(video.name),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-0.5).sp
                            ),
                            color = settings.palette.textPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = onToggle, modifier = Modifier.size(32.dp)) {
                            Icon(painterResource(R.drawable.ic_chevron_up_custom), null, tint = settings.palette.textTertiary)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Large Hero Preview
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16/9f)
                            .clip(RoundedCornerShape(24.dp))
                            .background(if (isDark) Color.Black.copy(0.3f) else settings.palette.bg)
                    ) {
                        if (settings.hideThumbnails) {
                            TriangleThumbPlaceholder(modifier = Modifier.fillMaxSize())
                        } else {
                            AsyncImage(
                                model = ImageRequest.Builder(context).data(video.uri).videoFrameMillis(1000).crossfade(true).build(),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )

                            AndroidView(
                                factory = { ctx ->
                                    PlayerView(ctx).apply {
                                        useController = false
                                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                                        this.player = player
                                    }
                                },
                                update = { view -> view.player = player },
                                modifier = Modifier.fillMaxSize().graphicsLayer { alpha = animatedAlpha }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Clean Detail Rows with Separate Identity
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            VideoDetailCard(painterResource(R.drawable.ic_storage_custom), formatSize(video.size), BrandBlue, Modifier.weight(1f))
                            VideoDetailCard(painterResource(R.drawable.ic_stopwatch_custom), formatDuration(videoDuration), BrandMagenta, Modifier.weight(1f))
                            if (videoWidth > 0) {
                                VideoDetailCard(painterResource(R.drawable.ic_frame_rate), qualityLabel(videoWidth, videoHeight), BrandOrange, Modifier.weight(1f))
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            VideoDetailCard(painterResource(R.drawable.ic_calendar_custom), formatDate(video.dateAdded), BrandSecondary, Modifier.weight(1.2f))
                            VideoDetailCard(painterResource(R.drawable.ic_code_circle_custom), frameRate ?: "30 FPS", BrandCyan, Modifier.weight(1f))
                            VideoDetailCard(painterResource(R.drawable.ic_description_custom), video.path.substringAfterLast(".", "UNK").uppercase(), BrandViolet, Modifier.weight(0.8f))
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Primary Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PremiumButton(
                            text = "Play Media Now",
                            onClick = onClick,
                            modifier = Modifier.weight(1f).height(56.dp),
                            icon = { Icon(Icons.Rounded.PlayArrow, null, tint = Color.White) }
                        )

                        // File Location Button
                        Surface(
                            onClick = {
                                try {
                                    val file = File(video.path)
                                    val parentFile = file.parentFile
                                    if (parentFile != null) {
                                        // Modern Android Intent for showing file location
                                        val intent = Intent(Intent.ACTION_VIEW)
                                        val builder = android.os.StrictMode.VmPolicy.Builder()
                                        android.os.StrictMode.setVmPolicy(builder.build())
                                        intent.setDataAndType(Uri.fromFile(parentFile), "resource/folder")
                                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                        context.startActivity(intent)
                                    }
                                } catch (e: Exception) {
                                    android.widget.Toast.makeText(context, "Location: ${video.path}", android.widget.Toast.LENGTH_LONG).show()
                                }
                            },
                            color = settings.palette.surfaceRaisedAlt,
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier.size(56.dp),
                            border = BorderStroke(1.dp, settings.palette.outlineMedium.copy(alpha = 0.5f))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Rounded.Folder,
                                    contentDescription = "Location",
                                    tint = settings.accent,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            } else {
                // Collapsed Row
                val rowHeight = if (settings.compactList) 76.dp else 92.dp
                Row(
                    modifier = Modifier.height(rowHeight).padding(if (settings.compactList) 8.dp else 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(if (settings.compactList) 90.dp else 110.dp, if (settings.compactList) 60.dp else 68.dp)
                            .clip(RoundedCornerShape(if (settings.compactList) 14.dp else 18.dp))
                            .background(if (isDark) settings.palette.surfaceRaisedAlt else settings.palette.bg)
                    ) {
                        if (settings.hideThumbnails) {
                            TriangleThumbPlaceholder(modifier = Modifier.fillMaxSize())
                        } else {
                            AsyncImage(
                                model = ImageRequest.Builder(context).data(video.uri).videoFrameMillis(1000).crossfade(true).build(),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                            BrandPlayOverlay(size = if (settings.compactList) 28.dp else 36.dp)
                        }
                        if (settings.showDurationBadge) {
                            DurationBadge(
                                text = formatDuration(video.duration),
                                modifier = Modifier.align(Alignment.BottomEnd).padding(if (settings.compactList) 4.dp else 6.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(if (settings.compactList) 14.dp else 20.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = formatFileName(video.name),
                            style = MaterialTheme.typography.titleMedium,
                            color = settings.palette.textPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(text = formatSize(video.size), style = MaterialTheme.typography.labelSmall, color = LocalAppUiSettings.current.palette.textSecondary, fontWeight = FontWeight.Bold)
                            if (video.width > 0) {
                                QualityBadge(width = video.width, height = video.height)
                            }
                        }
                    }
                    IconButton(onClick = onToggle) {
                        Icon(painterResource(R.drawable.ic_chevron_down_custom), null, tint = settings.palette.textTertiary)
                    }
                }
            }

            // Redesigned: Minimal details are now part of the expanded block above
        }
    }
}

@Composable
fun VideoDetailCard(icon: Painter, text: String, color: Color = LocalAppUiSettings.current.accent, modifier: Modifier = Modifier) {
    val settings = LocalAppUiSettings.current
    Surface(
        modifier = modifier,
        color = color.copy(alpha = 0.08f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                color = settings.palette.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun VideoDetailMinimal(icon: Painter, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = LocalAppUiSettings.current.accent.copy(alpha = 0.7f), modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = LocalAppUiSettings.current.palette.textSecondary
        )
    }
}


@Composable
fun VideoCompactDetail(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = LocalAppUiSettings.current.palette.textTertiary, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.labelSmall, color = LocalAppUiSettings.current.palette.textSecondary, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun VideoDetailItem(label: String, value: String, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(LocalAppUiSettings.current.palette.surfaceRaisedAlt), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = BrandAccent, modifier = Modifier.size(16.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = LocalAppUiSettings.current.palette.textTertiary)
            Text(value, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = LocalAppUiSettings.current.palette.textPrimary)
        }
    }
}

fun formatDate(seconds: Long): String {
    if (seconds == 0L) return "Unknown"
    val sdf = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(seconds * 1000))
}


@androidx.media3.common.util.UnstableApi
@Composable
fun VideoListScreen(
    folderName: String,
    videos: List<VideoFile>,
    isSearchActive: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onToggleSearch: () -> Unit,
    onVideoClick: (VideoFile) -> Unit,
    onBack: () -> Unit,
    allFolders: List<VideoFolder> = emptyList(),
    onVideoChanged: () -> Unit = {}
) {
    var actionVideo by remember { mutableStateOf<VideoFile?>(null) }
    var activeDialog by remember { mutableStateOf<VideoActionDialog?>(null) }
    var expandedVideoId by remember { mutableStateOf<Long?>(null) }

    var sortOrder      by remember { mutableStateOf(VideoSortOrder.NAME_ASC) }
    var layoutMode     by remember { mutableStateOf(LayoutMode.LIST) }
    var showFilterSheet by remember { mutableStateOf(false) }

    val scrollState = androidx.compose.foundation.lazy.rememberLazyListState()
    val isScrolled by remember {
        derivedStateOf { scrollState.firstVisibleItemIndex > 0 || scrollState.firstVisibleItemScrollOffset > 0 }
    }

    val filtered by remember(videos, searchQuery, sortOrder) {
        derivedStateOf {
            val base = if (searchQuery.isEmpty()) videos
            else videos.filter { it.name.contains(searchQuery, ignoreCase = true) }

            when (sortOrder) {
                VideoSortOrder.NAME_ASC -> base.sortedBy { it.name.lowercase() }
                VideoSortOrder.NAME_DESC -> base.sortedByDescending { it.name.lowercase() }
                VideoSortOrder.DATE_NEWEST -> base.reversed()
                VideoSortOrder.SIZE_DESC -> base.sortedByDescending { it.size }
                VideoSortOrder.DURATION_DESC -> base.sortedByDescending { it.duration }
            }
        }
    }

    if (showFilterSheet) {
        VideoFilterSortSheet(
            currentSort = sortOrder,
            currentLayout = layoutMode,
            onSortSelected = { sortOrder = it },
            onLayoutSelected = { layoutMode = it },
            onDismiss = { showFilterSheet = false }
        )
    }

    Scaffold(
        topBar = {
            MainTopAppBar(
                title = folderName,
                isSearchActive = isSearchActive,
                searchQuery = searchQuery,
                onSearchQueryChange = onSearchQueryChange,
                onToggleSearch = onToggleSearch,
                showBackButton = true,
                onBack = onBack,
                currentLayout = layoutMode,
                onToggleLayout = { layoutMode = if (layoutMode == LayoutMode.LIST) LayoutMode.GRID else LayoutMode.LIST },
                onShowFilter = { showFilterSheet = true },
                isScrolled = isScrolled
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (layoutMode == LayoutMode.LIST) {
                LazyColumn(
                    state = scrollState,
                    modifier = Modifier.fillMaxSize().padding(bottom = padding.calculateBottomPadding()),
                    contentPadding = PaddingValues(top = padding.calculateTopPadding(), bottom = 100.dp)
                ) {
                    itemsIndexed(filtered, key = { _, it -> it.id }) { index, video ->
                        val entryAnimation = remember { Animatable(0f) }
                        LaunchedEffect(Unit) {
                            delay(index * 40L)
                            entryAnimation.animateTo(1f, animationSpec = MotionSpec.SoftSpring)
                        }
                        Box(modifier = Modifier.graphicsLayer {
                            translationY = 40f * (1f - entryAnimation.value)
                            alpha = entryAnimation.value
                        }) {
                            VideoItem(
                                video = video,
                                isExpanded = expandedVideoId == video.id,
                                onToggle = {
                                    expandedVideoId = if (expandedVideoId == video.id) null else video.id
                                },
                                onClick = { onVideoClick(video) },
                                onLongClick = { actionVideo = video }
                            )
                        }

                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    state = androidx.compose.foundation.lazy.grid.rememberLazyGridState(),
                    modifier = Modifier.fillMaxSize().padding(bottom = padding.calculateBottomPadding()),
                    contentPadding = PaddingValues(top = padding.calculateTopPadding(), start = 16.dp, end = 16.dp, bottom = 100.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filtered, key = { it.id }) { video ->
                        VideoGridItem(video = video, onClick = { onVideoClick(video) }, onLongClick = { actionVideo = video })
                    }
                }
            }

            // Top Gradient Fade
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(LocalAppUiSettings.current.palette.bg, Color.Transparent)
                        )
                    )
            )
        }
    }
    VideoActionsHost(actionVideo = actionVideo, activeDialog = activeDialog, allFolders = allFolders, onDismiss = { actionVideo = null; activeDialog = null }, onPickDialog = { activeDialog = it }, onVideoChanged = { expandedVideoId = null; onVideoChanged() }, onPlayVideo = onVideoClick)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSortSheet(currentSort: FolderSortOrder, currentLayout: LayoutMode, onSortSelected: (FolderSortOrder) -> Unit, onLayoutSelected: (LayoutMode) -> Unit, onDismiss: () -> Unit) {
    PremiumBottomSheet(onDismiss = onDismiss) {
        Text("Sort Options", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = LocalAppUiSettings.current.palette.textPrimary)
        Spacer(modifier = Modifier.height(24.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            FolderSortOrder.entries.forEach { order ->
                val isSelected = order == currentSort
                PremiumCardSurface(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 20.dp,
                    selected = isSelected,
                    onClick = { onSortSelected(order); onDismiss() }
                ) {
                    Row(modifier = Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(order.label, style = MaterialTheme.typography.titleMedium, color = if (isSelected) BrandAccent else LocalAppUiSettings.current.palette.textPrimary, fontWeight = if(isSelected) FontWeight.ExtraBold else FontWeight.Medium)
                        if (isSelected) Icon(painterResource(R.drawable.ic_radio_on_custom), null, tint = BrandAccent, modifier = Modifier.size(24.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoFilterSortSheet(currentSort: VideoSortOrder, currentLayout: LayoutMode, onSortSelected: (VideoSortOrder) -> Unit, onLayoutSelected: (LayoutMode) -> Unit, onDismiss: () -> Unit) {
    PremiumBottomSheet(onDismiss = onDismiss) {
        Text("Order & Layout", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = LocalAppUiSettings.current.palette.textPrimary)
        Spacer(modifier = Modifier.height(24.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            VideoSortOrder.entries.forEach { order ->
                val isSelected = order == currentSort
                PremiumCardSurface(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 20.dp,
                    selected = isSelected,
                    onClick = { onSortSelected(order); onDismiss() }
                ) {
                    Row(modifier = Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(order.label, style = MaterialTheme.typography.titleMedium, color = if (isSelected) BrandAccent else LocalAppUiSettings.current.palette.textPrimary, fontWeight = if(isSelected) FontWeight.ExtraBold else FontWeight.Medium)
                        if (isSelected) Icon(painterResource(R.drawable.ic_radio_on_custom), null, tint = BrandAccent, modifier = Modifier.size(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun VideoActionsHost(actionVideo: VideoFile?, activeDialog: VideoActionDialog?, allFolders: List<VideoFolder>, onDismiss: () -> Unit, onPickDialog: (VideoActionDialog) -> Unit, onVideoChanged: () -> Unit, onPlayVideo: (VideoFile) -> Unit) {
    val video = actionVideo ?: return; val context = LocalContext.current; var pendingRetry by remember { mutableStateOf<(() -> Unit)?>(null) }
    val consentLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // On Android 11+, if the user just approved a DELETE request, the file is already gone.
            // We just need to hide the menu and refresh the list.
            if (activeDialog == VideoActionDialog.DELETE) {
                onDismiss()
                onVideoChanged()
            } else {
                pendingRetry?.invoke()
            }
        }
        pendingRetry = null
    }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    fun handleResult(result: FileOpResult, onSuccess: () -> Unit, retry: () -> Unit) {
        when (result) {
            is FileOpResult.Success -> onSuccess()
            is FileOpResult.Error -> errorMessage = result.message
            is FileOpResult.NeedsUserConsent -> {
                pendingRetry = retry
                consentLauncher.launch(androidx.activity.result.IntentSenderRequest.Builder(result.intentSender).build())
            }
        }
    }
    when (activeDialog ?: VideoActionDialog.MENU) {
        VideoActionDialog.MENU -> VideoActionMenuSheet(video = video, onDismiss = onDismiss, onRename = { onPickDialog(VideoActionDialog.RENAME) }, onDelete = { onPickDialog(VideoActionDialog.DELETE) }, onMove = { onPickDialog(VideoActionDialog.MOVE) }, onAddToPlaylist = { onPickDialog(VideoActionDialog.ADD_TO_PLAYLIST) })
        VideoActionDialog.RENAME -> RenameDialog(video = video, onDismiss = onDismiss, onRename = { newName -> handleResult(renameVideoFile(context, video, newName), onSuccess = { onDismiss(); onVideoChanged() }, retry = { onPickDialog(VideoActionDialog.RENAME) }) })
        VideoActionDialog.DELETE -> DeleteDialog(video = video, onDismiss = onDismiss, onDelete = { handleResult(deleteVideoFile(context, video), onSuccess = { onDismiss(); onVideoChanged() }, retry = { onPickDialog(VideoActionDialog.DELETE) }) })
        VideoActionDialog.MOVE -> MoveToFolderDialog(video = video, allFolders = allFolders, onDismiss = onDismiss, onMove = { folderPath -> handleResult(moveVideoFile(context, video, folderPath), onSuccess = { onDismiss(); onVideoChanged() }, retry = { onPickDialog(VideoActionDialog.MOVE) }) })
        VideoActionDialog.ADD_TO_PLAYLIST -> AddToPlaylistDialog(video = video, onDismiss = onDismiss)
        VideoActionDialog.DETAILS -> DetailsDialog(video = video, onDismiss = onDismiss)
        else -> PlaceholderScreen(activeDialog?.name ?: "Action")
    }
    if (errorMessage != null) {
        PremiumAlertDialog(onDismiss = { errorMessage = null }, title = "Error", text = errorMessage!!, confirmButton = { TextButton(onClick = { errorMessage = null }) { Text("OK") } })
    }
}

@Composable
fun RenameDialog(video: VideoFile, onDismiss: () -> Unit, onRename: (String) -> Unit) {
    var name by remember { mutableStateOf(video.name.substringBeforeLast(".")) }
    PremiumDialog(onDismiss = onDismiss, title = "Rename Media") {
        androidx.compose.material3.OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Filename") },
            shape = RoundedCornerShape(16.dp),
            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BrandAccent,
                unfocusedBorderColor = LocalAppUiSettings.current.palette.outlineMedium,
                cursorColor = BrandAccent
            )
        )
        Spacer(modifier = Modifier.height(32.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = LocalAppUiSettings.current.palette.textTertiary, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(16.dp))
            PremiumButton(text = "Rename", onClick = { onRename(name) })
        }
    }
}

@Composable
fun DeleteDialog(video: VideoFile, onDismiss: () -> Unit, onDelete: () -> Unit) {
    PremiumDialog(onDismiss = onDismiss, title = "Delete Media?") {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(BrandRed.copy(alpha = 0.08f)).padding(16.dp)
        ) {
            Icon(painterResource(R.drawable.ic_warning_custom), null, tint = BrandRed, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text("This will permanently remove the file from your storage.", color = LocalAppUiSettings.current.palette.textPrimary, style = MaterialTheme.typography.bodyMedium)
        }
        Spacer(modifier = Modifier.height(32.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = LocalAppUiSettings.current.palette.textTertiary, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Surface(
                onClick = onDelete,
                color = BrandRed,
                shape = CircleShape,
                modifier = Modifier.height(44.dp)
            ) {
                Box(modifier = Modifier.padding(horizontal = 24.dp), contentAlignment = Alignment.Center) {
                    Text("Delete", color = Color.White, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}
@Composable
fun MoveToFolderDialog(video: VideoFile, allFolders: List<VideoFolder>, onDismiss: () -> Unit, onMove: (String) -> Unit) {
    PremiumBottomSheet(onDismiss = onDismiss) {
        Text("Move to Folder", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = LocalAppUiSettings.current.palette.textPrimary)
        Spacer(modifier = Modifier.height(24.dp))
        LazyColumn(
            modifier = Modifier.heightIn(max = 400.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(allFolders) { folder ->
                PremiumCardSurface(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 20.dp,
                    onClick = { onMove(folder.path) }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BrandIconContainer(active = true, cornerRadius = 12.dp, size = 40.dp) {
                            Icon(painterResource(R.drawable.ic_folder_custom), null, tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(folder.name, color = LocalAppUiSettings.current.palette.textPrimary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                            Text("${folder.videoCount} Videos", style = MaterialTheme.typography.labelSmall, color = LocalAppUiSettings.current.palette.textSecondary)
                        }
                        Icon(painterResource(R.drawable.ic_chevron_right_custom), null, tint = LocalAppUiSettings.current.palette.textTertiary.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}
@Composable
fun DetailsDialog(video: VideoFile, onDismiss: () -> Unit) {
    PremiumDialog(onDismiss = onDismiss, title = "Media Details") {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            DetailItemExpressive(painterResource(R.drawable.ic_description_custom), "Name", video.name)
            DetailItemExpressive(painterResource(R.drawable.ic_folder_custom), "Path", video.path)
            DetailItemExpressive(painterResource(R.drawable.ic_storage_custom), "Size", formatSize(video.size))
            DetailItemExpressive(painterResource(R.drawable.ic_stopwatch_custom), "Duration", formatDuration(video.duration))
            DetailItemExpressive(painterResource(R.drawable.ic_calendar_custom), "Date Added", formatDate(video.dateAdded))
        }
        Spacer(modifier = Modifier.height(24.dp))
        PremiumButton(text = "Close", onClick = onDismiss, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
fun DetailItemExpressive(icon: Painter, label: String, value: String) {
    Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(LocalAppUiSettings.current.palette.textPrimary.copy(alpha = 0.05f)), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = BrandAccent, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label.uppercase(), style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp, fontWeight = FontWeight.Bold), color = LocalAppUiSettings.current.palette.textTertiary)
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = LocalAppUiSettings.current.palette.textPrimary)
        }
    }
}
@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun VideoActionMenuSheet(video: VideoFile, onDismiss: () -> Unit, onRename: () -> Unit, onDelete: () -> Unit, onMove: () -> Unit, onAddToPlaylist: () -> Unit) {
    val context = LocalContext.current
    PremiumBottomSheet(onDismiss = onDismiss) {
        Text(
            text = formatFileName(video.name),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            color = LocalAppUiSettings.current.palette.textPrimary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(24.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            VideoActionRow(painterResource(R.drawable.ic_share), "Share Media", onClick = {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "video/*"
                    putExtra(Intent.EXTRA_STREAM, video.uri)
                }
                context.startActivity(Intent.createChooser(intent, "Share Video"))
                onDismiss()
            })
            VideoActionRow(painterResource(R.drawable.ic_rename_custom), "Rename File", onClick = onRename)
            VideoActionRow(painterResource(R.drawable.ic_playlist_add_custom), "Add to Playlist", onClick = onAddToPlaylist)
            VideoActionRow(painterResource(R.drawable.ic_move_custom), "Move to Folder", onClick = onMove)
            Spacer(modifier = Modifier.height(8.dp))
            VideoActionRow(painterResource(R.drawable.ic_trash_custom), "Delete Permanently", tint = BrandRed, bg = BrandRed.copy(alpha = 0.1f), onClick = onDelete)
        }
    }
}

@Composable
private fun VideoActionRow(icon: Painter, label: String, tint: Color = BrandAccent, bg: Color? = null, onClick: () -> Unit) {
    PremiumCardSurface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 20.dp
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(bg ?: tint.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = tint, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(label, style = MaterialTheme.typography.titleMedium, color = if(tint == BrandRed) BrandRed else LocalAppUiSettings.current.palette.textPrimary, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.weight(1f))
            Icon(painterResource(R.drawable.ic_chevron_right_custom), null, tint = LocalAppUiSettings.current.palette.textTertiary.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
        }
    }
}
@Composable
private fun SectionLabel(text: String) {
    val accent = LocalAppUiSettings.current.accent
    val dynamicGradient = remember(accent) {
        Brush.linearGradient(colors = listOf(IconSky, accent, IconPeriwinkle))
    }

    val infinite = rememberInfiniteTransition(label = "labelBloom")
    val breathe by infinite.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "breathe"
    )

    Row(
        modifier = Modifier.padding(start = 24.dp, top = 32.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(6.dp)
                .height(24.dp)
                .graphicsLayer { scaleY = breathe }
                .clip(CircleShape)
                .background(dynamicGradient)
                .shadow(12.dp, CircleShape, spotColor = accent)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelLarge.copy(
                color = LocalAppUiSettings.current.palette.textPrimary,
                letterSpacing = 3.sp,
                fontWeight = FontWeight.Black
            )
        )
    }
}
@Composable
fun EmptyState(title: String, subtitle: String, icon: @Composable () -> Unit) { Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { icon(); Spacer(modifier = Modifier.height(16.dp)); Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold); Text(subtitle, color = LocalAppUiSettings.current.palette.textSecondary) } } }
@Composable
fun PlayUrlDialog(onDismiss: () -> Unit, onPlay: (String) -> Unit) {
    val context = LocalContext.current
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
    var url by remember { mutableStateOf("") }

    PremiumDialog(onDismiss = onDismiss, title = "Play URL") {
        TextField(
            value = url,
            onValueChange = { url = it },
            placeholder = { Text("https://...") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = BrandAccent,
                cursorColor = BrandAccent
            )
        )
        Spacer(modifier = Modifier.height(24.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = {
                val clip = clipboardManager.primaryClip
                if (clip != null && clip.itemCount > 0) {
                    url = clip.getItemAt(0).text.toString()
                }
            }) {
                Text("Paste Link", color = BrandAccent, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(12.dp))
            PremiumButton(text = "Play", onClick = { onPlay(url) }, enabled = url.isNotBlank())
        }
    }
}
@androidx.media3.common.util.UnstableApi
@Composable
fun RecentsScreen(
    entries: List<RecentEntry>,
    isSearchActive: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onToggleSearch: () -> Unit,
    onVideoClick: (VideoFile) -> Unit,
    onRemoveEntry: (Long) -> Unit,
    onClearDay: (List<Long>) -> Unit
) {
    val filtered by remember(entries, searchQuery) {
        derivedStateOf {
            if (searchQuery.isEmpty()) entries
            else entries.filter { it.video.name.contains(searchQuery, ignoreCase = true) }
        }
    }

    val scrollState = androidx.compose.foundation.lazy.rememberLazyListState()
    val isScrolled by remember {
        derivedStateOf { scrollState.firstVisibleItemIndex > 0 || scrollState.firstVisibleItemScrollOffset > 0 }
    }

    var expandedVideoId by remember { mutableStateOf<Long?>(null) }
    Scaffold(
        topBar = {
            MainTopAppBar(
                title = "History",
                isSearchActive = isSearchActive,
                searchQuery = searchQuery,
                onSearchQueryChange = onSearchQueryChange,
                onToggleSearch = onToggleSearch,
                showBackButton = true,
                onBack = { /* Handled by navigation */ },
                isScrolled = isScrolled
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        if (filtered.isEmpty()) {
            EmptyState(title = "No History", subtitle = "Your recently played videos appear here") {
                Icon(painterResource(R.drawable.ic_history_custom), null, modifier = Modifier.size(56.dp), tint = LocalAppUiSettings.current.palette.textTertiary)
            }
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    state = scrollState,
                    modifier = Modifier.fillMaxSize().padding(bottom = padding.calculateBottomPadding()),
                    contentPadding = PaddingValues(top = padding.calculateTopPadding(), bottom = 100.dp)
                ) {
                    itemsIndexed(filtered, key = { _, it -> it.video.id }) { index, entry ->
                        val entryAnimation = remember { Animatable(0f) }
                        LaunchedEffect(Unit) {
                            delay(index * 40L)
                            entryAnimation.animateTo(1f, animationSpec = MotionSpec.SoftSpring)
                        }
                        Box(modifier = Modifier.graphicsLayer {
                            translationX = -30f * (1f - entryAnimation.value)
                            alpha = entryAnimation.value
                        }) {
                            VideoItem(
                                video = entry.video,
                                isExpanded = expandedVideoId == entry.video.id,
                                onToggle = {
                                    expandedVideoId = if (expandedVideoId == entry.video.id) null else entry.video.id
                                },
                                onClick = { onVideoClick(entry.video) }
                            )
                        }
                    }
                }

                // Top Gradient Fade
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(LocalAppUiSettings.current.palette.bg, Color.Transparent)
                            )
                        )
                )
            }
        }
    }
}
@Composable
fun PlaceholderScreen(name: String) { Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(name) } }