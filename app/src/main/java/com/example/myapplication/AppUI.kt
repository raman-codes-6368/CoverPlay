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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.videoFrameMillis
import com.example.myapplication.ui.theme.*

class VideoThumbnailFetcher(
    private val uri: Uri,
    private val options: Options,
    private val context: android.content.Context
) : Fetcher {
    override suspend fun fetch(): FetchResult? {
        return try {
            val width = options.size.width.pxOrElse { 512 }
            val height = options.size.height.pxOrElse { 320 }
            val bitmap = context.contentResolver.loadThumbnail(uri, Size(width, height), null)
            DrawableResult(
                drawable = bitmap.toDrawable(context.resources),
                isSampled = true,
                dataSource = DataSource.DISK
            )
        } catch (e: Exception) {
            null
        }
    }

    class Factory(private val context: android.content.Context) : Fetcher.Factory<Uri> {
        override fun create(data: Uri, options: Options, imageLoader: ImageLoader): Fetcher? {
            // loadThumbnail is API 29+; below that, return null so this
            // request falls through to the next registered component
            // (VideoFrameDecoder) instead of being claimed here.
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return null
            if (data.scheme != "content") return null
            return VideoThumbnailFetcher(data, options, context)
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// BOTTOM NAV
// ═════════════════════════════════════════════════════════════════════════════

@Composable
fun BottomNavigationBar(currentScreen: Screen, onScreenSelected: (Screen) -> Unit) {
    val items = listOf(
        NavItem(Screen.HOME,      "Home",      painterResource(R.drawable.ic_home_custom)),
        NavItem(Screen.RECENTS,   "History",   painterResource(R.drawable.ic_recent_custom)),
        NavItem(Screen.PLAYLISTS, "Playlists", painterResource(R.drawable.ic_playlist_custom)),
        NavItem(Screen.SETTINGS,  "Settings",  painterResource(R.drawable.ic_settings_custom))
    )

    Box(
        modifier = Modifier
            .navigationBarsPadding()
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp),
            color = NavGlassSurface,
            shape = RoundedCornerShape(50.dp),
            border = BorderStroke(
                1.dp,
                Brush.linearGradient(
                    colors = listOf(
                        BrandCyan.copy(alpha = 0.22f),
                        OutlineFaint,
                        BrandVioletWhisper.copy(alpha = 0.12f)
                    )
                )
            ),
            shadowElevation = 28.dp,
            tonalElevation = 0.dp
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    val isSelected = when (item.screen) {
                        Screen.HOME -> currentScreen == Screen.HOME || currentScreen == Screen.FOLDER_VIDEOS
                        else        -> currentScreen == item.screen
                    }
                    NavPill(
                        item = item,
                        isSelected = isSelected,
                        onClick = { onScreenSelected(item.screen) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

private data class NavItem(val screen: Screen, val label: String, val icon: Painter)

@Composable
private fun NavPill(item: NavItem, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val iconTint by animateColorAsState(
        targetValue = if (isSelected) BrandCyan else TextTertiary,
        animationSpec = tween(400), label = "navTint"
    )
    val pillAlpha by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = tween(400),
        label = "navPill"
    )
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.08f else 1f,
        animationSpec = tween(400),
        label = "navScale"
    )
    Box(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(26.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (pillAlpha > 0f) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer(alpha = pillAlpha)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                BrandBlueDeep.copy(alpha = 0.45f),
                                BrandCyan.copy(alpha = 0.15f)
                            )
                        ),
                        RoundedCornerShape(26.dp)
                    )
                    .border(
                        1.dp,
                        Brush.linearGradient(
                            colors = listOf(
                                BrandCyan.copy(alpha = 0.45f),
                                Color.Transparent,
                                BrandVioletWhisper.copy(alpha = 0.15f)
                            )
                        ),
                        RoundedCornerShape(26.dp)
                    )
            )
            // Subtle Glow
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(alpha = pillAlpha * 0.4f)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(BrandCyan.copy(alpha = 0.4f), Color.Transparent)
                        )
                    )
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.graphicsLayer(scaleX = scale, scaleY = scale)
        ) {
            Icon(
                painter = item.icon,
                contentDescription = item.label,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
            AnimatedVisibility(
                visible = isSelected,
                enter = fadeIn(tween(250)) + slideInVertically { it / 2 },
                exit  = fadeOut(tween(200))
            ) {
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = BrandCyan,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

@Composable
fun BrandedTopBarTitle(text: String, showIcon: Boolean = true) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (showIcon) {
            BrandTriangle(modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(10.dp))
        }
        BrandTitleText(text = text)
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// TOP APP BAR
// ═════════════════════════════════════════════════════════════════════════════

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
    onShowFilter: () -> Unit = {}
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(isSearchActive) { if (isSearchActive) focusRequester.requestFocus() }

    TopAppBar(
        navigationIcon = {
            if (showBackButton && !isSearchActive) {
                PremiumTopBarAction(
                    icon = painterResource(R.drawable.ic_back_custom),
                    onClick = onBack
                )
            }
        },
        title = {
            if (isSearchActive) {
                BasicTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 12.dp)
                        .height(42.dp)
                        .focusRequester(focusRequester),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                    cursorBrush = SolidColor(BrandCyan),
                    singleLine = true,
                    decorationBox = { innerTextField ->
                        Surface(
                            color = SurfaceRaised.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(21.dp),
                            border = BorderStroke(1.dp, OutlineMedium),
                            tonalElevation = 0.dp
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(painterResource(R.drawable.ic_search_custom), null, tint = TextSecondary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(modifier = Modifier.weight(1f)) {
                                    if (searchQuery.isEmpty()) {
                                        Text("Search videos…", color = TextTertiary, style = MaterialTheme.typography.bodyLarge)
                                    }
                                    innerTextField()
                                }
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(
                                        onClick = { onSearchQueryChange("") },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(painterResource(R.drawable.ic_close_custom), "Clear", tint = TextSecondary, modifier = Modifier.size(18.dp))
                                    }
                                }
                                IconButton(
                                    onClick = onToggleSearch,
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        painter = if (searchQuery.isEmpty()) painterResource(R.drawable.ic_close_custom) else rememberVectorPainter(Icons.Default.Check),
                                        contentDescription = "Close",
                                        tint = BrandCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                )
            } else {
                BrandedTopBarTitle(text = title, showIcon = !showBackButton)
            }
        },
        actions = {
            if (!isSearchActive) {
                Row(
                    modifier = Modifier.padding(end = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PremiumTopBarAction(
                        icon = painterResource(R.drawable.ic_search_custom),
                        onClick = onToggleSearch
                    )
                    if (!showBackButton) {
                        PremiumTopBarAction(
                            icon = if (currentLayout == LayoutMode.LIST) painterResource(R.drawable.ic_list_custom) else painterResource(R.drawable.ic_grid_custom),
                            onClick = onToggleLayout
                        )
                        PremiumTopBarAction(
                            icon = painterResource(R.drawable.ic_filter_custom),
                            onClick = onShowFilter
                        )
                    }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor         = Color.Transparent,
            scrolledContainerColor = Color.Transparent
        )
    )
}

@Composable
fun PremiumTopBarAction(icon: Painter, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.size(38.dp),
        color = NavGlassSurface,
        shape = CircleShape,
        border = BorderStroke(
            1.dp,
            Brush.linearGradient(
                colors = listOf(
                    BrandCyan.copy(alpha = 0.22f),
                    OutlineFaint,
                    BrandVioletWhisper.copy(alpha = 0.12f)
                )
            )
        )
    ) {
        Box(contentAlignment = Alignment.Center) {
            // Glow
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(BrandCyan.copy(alpha = 0.4f), Color.Transparent)
                        )
                    )
            )
            Icon(icon, null, tint = TextPrimary, modifier = Modifier.size(18.dp))
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// FOLDER LIST SCREEN (Home)
// ═════════════════════════════════════════════════════════════════════════════

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
    var showUrlDialog   by remember { mutableStateOf(false) }

    // Long-press menu state for videos surfaced in search results here on
    // Home -- same action sheet/dialogs as inside a folder, since it's the
    // same VideoItem row and the same underlying file.
    var actionVideo by remember { mutableStateOf<VideoFile?>(null) }
    var activeDialog by remember { mutableStateOf<VideoActionDialog?>(null) }

    val filteredFolders by remember(searchQuery, sortOrder, folders) {
        derivedStateOf {
            val base = if (searchQuery.isEmpty()) folders
            else folders.filter { it.name.contains(searchQuery, ignoreCase = true) }
            when (sortOrder) {
                FolderSortOrder.NAME_ASC      -> base.sortedBy { it.name.lowercase() }
                FolderSortOrder.NAME_DESC     -> base.sortedByDescending { it.name.lowercase() }
                FolderSortOrder.MOST_VIDEOS   -> base.sortedByDescending { it.videoCount }
                FolderSortOrder.FEWEST_VIDEOS -> base.sortedBy { it.videoCount }
            }
        }
    }

    val filteredVideos by remember(searchQuery, allVideos) {
        derivedStateOf {
            if (searchQuery.isEmpty()) emptyList()
            else allVideos.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }
    }

    if (showFilterSheet) {
        FilterSortSheet(
            currentSort    = sortOrder,
            currentLayout  = layoutMode,
            onSortSelected = { sortOrder = it },
            onLayoutSelected = { layoutMode = it },
            onDismiss      = { showFilterSheet = false }
        )
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

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            MainTopAppBar(
                title               = "CoverPlay",
                isSearchActive      = isSearchActive,
                searchQuery         = searchQuery,
                onSearchQueryChange = onSearchQueryChange,
                onToggleSearch      = onToggleSearch,
                currentLayout       = layoutMode,
                onToggleLayout      = { layoutMode = if (layoutMode == LayoutMode.LIST) LayoutMode.GRID else LayoutMode.LIST },
                onShowFilter        = { showFilterSheet = true }
            )

            when {
                isSearchActive && searchQuery.isNotEmpty() -> {
                    LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)) {
                        if (filteredVideos.isNotEmpty()) {
                            item {
                                SectionLabel("Videos")
                            }
                            items(filteredVideos, key = { it.id }) { video ->
                                VideoItem(
                                    video,
                                    onClick = { onVideoClick(video) },
                                    onLongClick = { actionVideo = video }
                                )
                                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = OutlineFaint)
                            }
                        }
                        if (filteredFolders.isNotEmpty()) {
                            item { SectionLabel("Folders") }
                            items(filteredFolders, key = { it.path }) { folder ->
                                FolderItem(folder, onClick = { onFolderClick(folder) })
                                Spacer(modifier = Modifier.height(10.dp))
                            }
                        }
                        if (filteredVideos.isEmpty() && filteredFolders.isEmpty()) {
                            item {
                                EmptyState(
                                    painter = painterResource(R.drawable.ic_search_custom),
                                    title    = "No Results",
                                    subtitle = "Nothing matched \"$searchQuery\""
                                )
                            }
                        }
                    }
                }

                layoutMode == LayoutMode.GRID -> {
                    LazyVerticalGrid(
                        columns             = GridCells.Fixed(2),
                        contentPadding      = PaddingValues(16.dp, 16.dp, 16.dp, 80.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement   = Arrangement.spacedBy(12.dp),
                        modifier              = Modifier.fillMaxSize()
                    ) {
                        items(filteredFolders, key = { it.path }) { folder ->
                            FolderGridItem(folder, onClick = { onFolderClick(folder) })
                        }
                    }
                }

                else -> {
                    if (filteredFolders.isEmpty()) {
                        EmptyState(
                            title    = "No Video Folders Found",
                            subtitle = "Add videos to your device storage and they'll appear here"
                        ) {
                            PremiumFolderIcon(modifier = Modifier.size(44.dp))
                        }
                    } else {
                        LazyColumn(contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 80.dp)) {
                            items(filteredFolders, key = { it.path }) { folder ->
                                FolderItem(folder, onClick = { onFolderClick(folder) })
                                Spacer(modifier = Modifier.height(10.dp))
                            }
                        }
                    }
                }
            }
        }

        // Play URL Button (Floating Action Button style)
        Surface(
            onClick = { showUrlDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 20.dp)
                .size(56.dp),
            color = Color.Transparent,
            shape = CircleShape
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(BrandBlueDeep, BrandCyan)
                        ),
                        CircleShape
                    )
                    .border(
                        1.dp,
                        Brush.linearGradient(
                            colors = listOf(BrandHighlight.copy(alpha = 0.5f), Color.Transparent)
                        ),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Glow
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(BrandCyan.copy(alpha = 0.4f), Color.Transparent)
                            )
                        )
                )
                Icon(painterResource(R.drawable.ic_url_custom), "Play URL", tint = Color.White, modifier = Modifier.size(24.dp))
            }
        }
    }

    VideoActionsHost(
        actionVideo   = actionVideo,
        activeDialog  = activeDialog,
        allFolders    = folders,
        onDismiss     = { actionVideo = null; activeDialog = null },
        onPickDialog  = { activeDialog = it },
        onVideoChanged = onVideoChanged
    )
}

// ═════════════════════════════════════════════════════════════════════════════
// PREMIUM FOLDER ICON — Canvas-drawn, gradient-filled, no external drawable
//
// Faithful Compose translation of the brand's frosted-glass folder mark:
// obsidian glass body, aurora-teal + violet caustic light pooling inside,
// a frosted refraction band along the top rim, a diagonal specular sweep,
// and a continuous platinum → aurora-teal → ice-blue → violet rim stroke.
// The rim and caustic accent breathe with the user's chosen accent colour
// (LocalAppUiSettings.accent) so it stays consistent across theme presets,
// the same way BrandIconContainer / PremiumCardSurface already do.
// Scales cleanly from small list chips up to large grid tiles.
// ═════════════════════════════════════════════════════════════════════════════

@Composable
fun PremiumFolderIcon(modifier: Modifier = Modifier) {
    val accent = LocalAppUiSettings.current.accent
    androidx.compose.foundation.Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        // Original artwork is on a 64x64 grid — scale every coordinate by sx/sy.
        val sx = w / 64f
        val sy = h / 64f
        fun px(x: Float) = x * sx
        fun py(y: Float) = y * sy
        fun pt(x: Float, y: Float) = androidx.compose.ui.geometry.Offset(px(x), py(y))

        // ── Unified folder silhouette (single source of truth, like the SVG clipPath) ──
        val folderPath = androidx.compose.ui.graphics.Path().apply {
            moveTo(px(4f), py(18f))
            cubicTo(px(4f), py(15.2f), px(6.2f), py(13f), px(9f), py(13f))
            lineTo(px(22.5f), py(13f))
            cubicTo(px(23.6f), py(13f), px(24.7f), py(13.5f), px(25.4f), py(14.4f))
            lineTo(px(28.6f), py(18.4f))
            cubicTo(px(29.3f), py(19.3f), px(30.4f), py(19.8f), px(31.5f), py(19.8f))
            lineTo(px(55f), py(19.8f))
            cubicTo(px(57.8f), py(19.8f), px(60f), py(22f), px(60f), py(24.8f))
            lineTo(px(60f), py(49f))
            cubicTo(px(60f), py(51.8f), px(57.8f), py(54f), px(55f), py(54f))
            lineTo(px(9f), py(54f))
            cubicTo(px(6.2f), py(54f), px(4f), py(51.8f), px(4f), py(49f))
            close()
        }

        // ── Deep obsidian glass body fill ───────────────────────────────────
        drawPath(
            path = folderPath,
            brush = androidx.compose.ui.graphics.Brush.linearGradient(
                colors = listOf(
                    SurfaceRaisedAlt.copy(alpha = 0.92f),  // #212A3B-ish
                    SurfaceRaised.copy(alpha = 0.88f),     // #141923
                    BgBase.copy(alpha = 0.96f)             // #07090E
                ),
                start = pt(6f, 14f),
                end   = pt(58f, 56f)
            )
        )

        // Everything below is clipped to the folder silhouette, mirroring the SVG's <clipPath>.
        clipPath(folderPath) {
            // Aurora caustic light pooling (cool accent-tinted, top-left)
            drawRect(
                brush = androidx.compose.ui.graphics.Brush.radialGradient(
                    colors = listOf(accent.copy(alpha = 0.35f), accent.copy(alpha = 0.10f), accent.copy(alpha = 0f)),
                    center = pt(16f, 12.8f),
                    radius = px(54.4f)
                ),
                size = androidx.compose.ui.geometry.Size(w, h)
            )
            // Warm violet caustic, bottom-right
            drawRect(
                brush = androidx.compose.ui.graphics.Brush.radialGradient(
                    colors = listOf(BrandVioletWhisper.copy(alpha = 0.22f), BrandVioletWhisper.copy(alpha = 0f)),
                    center = pt(54.4f, 60.8f),
                    radius = px(44.8f)
                ),
                size = androidx.compose.ui.geometry.Size(w, h)
            )

            // File tabs peeking out from inside (depth cue)
            drawRoundRect(
                color = BrandIceBlue.copy(alpha = 0.35f),
                topLeft = pt(14f, 20.5f),
                size = androidx.compose.ui.geometry.Size(px(36f), py(2f)),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(px(1f), py(1f))
            )
            drawRoundRect(
                color = BrandHighlight.copy(alpha = 0.18f),
                topLeft = pt(18f, 23f),
                size = androidx.compose.ui.geometry.Size(px(30f), py(2f)),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(px(1f), py(1f))
            )

            // Frosted top band — refraction along the upper rim
            drawRect(
                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(
                        BrandHighlight.copy(alpha = 0.55f),
                        BrandHighlight.copy(alpha = 0.08f),
                        BrandHighlight.copy(alpha = 0f)
                    ),
                    startY = py(18f),
                    endY   = py(32f)
                ),
                topLeft = pt(0f, 18f),
                size = androidx.compose.ui.geometry.Size(w, py(14f))
            )

            // Diagonal specular sweep across the glass surface
            val specPath = androidx.compose.ui.graphics.Path().apply {
                moveTo(px(8f), py(20f))
                lineTo(px(34f), py(20f))
                lineTo(px(24f), py(50f))
                lineTo(px(4f), py(50f))
                close()
            }
            drawPath(
                path = specPath,
                brush = androidx.compose.ui.graphics.Brush.linearGradient(
                    colors = listOf(
                        BrandHighlight.copy(alpha = 0f),
                        BrandHighlight.copy(alpha = 0.18f),
                        BrandHighlight.copy(alpha = 0f)
                    ),
                    start = pt(8f, 20f),
                    end   = pt(40f, 44f)
                )
            )

            // Hairline platinum highlight tracing the inside of the top edge
            val innerRim = androidx.compose.ui.graphics.Path().apply {
                moveTo(px(6f), py(21.5f))
                cubicTo(px(6f), py(19.8f), px(7.3f), py(18.5f), px(9f), py(18.5f))
                lineTo(px(31.5f), py(18.5f))
                cubicTo(px(32.9f), py(18.5f), px(34.3f), py(17.9f), px(35.2f), py(16.8f))
            }
            drawPath(
                path = innerRim,
                color = BrandHighlight.copy(alpha = 0.45f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = py(0.6f),
                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                )
            )

            // Bottom-edge dark glass weight
            drawRect(
                color = BgBase.copy(alpha = 0.35f),
                topLeft = pt(4f, 46f),
                size = androidx.compose.ui.geometry.Size(px(56f), py(8f))
            )

            // Tiny corner sparkle
            drawCircle(color = BrandHighlight, radius = px(0.6f), center = pt(12f, 24f))
            drawCircle(color = BrandHighlight.copy(alpha = 0.15f), radius = px(1.6f), center = pt(12f, 24f))
        }

        // ── Continuous outer rim — platinum → accent → ice-blue → violet ───
        drawPath(
            path = folderPath,
            brush = androidx.compose.ui.graphics.Brush.linearGradient(
                colors = listOf(
                    BrandHighlight.copy(alpha = 0.95f),
                    accent.copy(alpha = 0.85f),
                    BrandIceBlue.copy(alpha = 0.6f),
                    BrandVioletWhisper.copy(alpha = 0.5f)
                ),
                start = pt(4f, 14f),
                end   = pt(60f, 54f)
            ),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = py(1f))
        )

        // Inner hairline for a sense of glass thickness
        val thicknessPath = androidx.compose.ui.graphics.Path().apply {
            moveTo(px(5.5f), py(18.5f))
            cubicTo(px(5.5f), py(16.5f), px(7f), py(15f), px(9f), py(15f))
            lineTo(px(22f), py(15f))
            cubicTo(px(22.8f), py(15f), px(23.6f), py(15.4f), px(24.1f), py(16f))
            lineTo(px(27.5f), py(20.3f))
            cubicTo(px(28.3f), py(21.3f), px(29.5f), py(21.8f), px(30.8f), py(21.8f))
            lineTo(px(54.6f), py(21.8f))
            cubicTo(px(56.6f), py(21.8f), px(58.2f), py(23.4f), px(58.2f), py(25.4f))
            lineTo(px(58.2f), py(48.5f))
        }
        drawPath(
            path = thicknessPath,
            color = BrandHighlight.copy(alpha = 0.18f),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = py(0.5f))
        )
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// FOLDER ITEMS
// ═════════════════════════════════════════════════════════════════════════════

@Composable
fun FolderItem(folder: VideoFolder, onClick: () -> Unit) {
    PremiumCardSurface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick),
        cornerRadius = 24.dp,
        content = {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                BrandIconContainer {
                    PremiumFolderIcon(modifier = Modifier.size(28.dp))
                }
                Spacer(modifier = Modifier.width(18.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text     = folder.name,
                        style    = MaterialTheme.typography.titleLarge,
                        color    = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text  = "${folder.videoCount} ${if (folder.videoCount == 1) "Video" else "Videos"}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextTertiary
                        )
                        if (folder.size.isNotEmpty()) {
                            Box(modifier = Modifier.size(3.dp).background(TextTertiary.copy(alpha = 0.4f), CircleShape))
                            Text(text = folder.size, style = MaterialTheme.typography.bodyMedium, color = TextTertiary)
                        }
                    }
                }
                Icon(Icons.Default.ChevronRight, null, tint = TextTertiary, modifier = Modifier.size(20.dp))
            }
        }
    )
}

@Composable
fun FolderGridItem(folder: VideoFolder, onClick: () -> Unit) {
    PremiumCardSurface(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(28.dp))
            .clickable(onClick = onClick),
        cornerRadius = 28.dp,
        content = {
            Column(
                modifier                = Modifier.padding(16.dp),
                horizontalAlignment     = Alignment.CenterHorizontally,
                verticalArrangement     = Arrangement.Center
            ) {
                BrandIconContainer(cornerRadius = 20.dp) {
                    PremiumFolderIcon(modifier = Modifier.size(30.dp))
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text      = folder.name,
                    style     = MaterialTheme.typography.titleMedium,
                    color     = MaterialTheme.colorScheme.onSurface,
                    maxLines  = 1,
                    overflow  = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text  = "${folder.videoCount} Videos",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextTertiary
                )
            }
        }
    )
}

// ═════════════════════════════════════════════════════════════════════════════
// FILTER / SORT SHEET
// ═════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSortSheet(
    currentSort: FolderSortOrder,
    currentLayout: LayoutMode,
    onSortSelected: (FolderSortOrder) -> Unit,
    onLayoutSelected: (LayoutMode) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        containerColor   = SurfaceNavy,
        shape            = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        tonalElevation   = 0.dp,
        dragHandle = {
            Box(modifier = Modifier.padding(vertical = 12.dp).size(40.dp, 4.dp).background(OutlineMedium, CircleShape))
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 48.dp)
        ) {
            BrandTitleText("Library Settings", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(28.dp))

            Text("Sort Folders By", style = MaterialTheme.typography.sectionOverline.copy(color = TextSecondary))
            Spacer(modifier = Modifier.height(12.dp))

            FolderSortOrder.entries.forEach { order ->
                val isSelected = order == currentSort
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(RoundedCornerShape(16.dp))
                        .clickable { onSortSelected(order); onDismiss() },
                    color  = if (isSelected) BrandBlueDeep.copy(alpha = 0.2f) else Color.Transparent,
                    border = if (isSelected) BorderStroke(1.dp, BrandCyan.copy(alpha = 0.5f)) else null,
                    shape  = RoundedCornerShape(16.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(order.label, style = MaterialTheme.typography.titleMedium, color = if (isSelected) BrandCyan else Color.White)
                        if (isSelected) Icon(Icons.Default.Check, null, tint = BrandCyan, modifier = Modifier.size(20.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("View Mode", style = MaterialTheme.typography.sectionOverline.copy(color = TextSecondary))
            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf(LayoutMode.LIST to Icons.AutoMirrored.Filled.List, LayoutMode.GRID to Icons.Default.GridView).forEach { (mode, icon) ->
                    val isSelected = currentLayout == mode
                    Surface(
                        modifier = Modifier.weight(1f).clip(RoundedCornerShape(20.dp)).clickable { onLayoutSelected(mode); onDismiss() },
                        color  = if (isSelected) BrandBlueDeep.copy(alpha = 0.2f) else SurfaceRaised.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, if (isSelected) BrandCyan.copy(alpha = 0.5f) else OutlineFaint),
                        shape  = RoundedCornerShape(20.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(icon, null, tint = if (isSelected) BrandCyan else TextSecondary, modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(if (mode == LayoutMode.LIST) "List" else "Grid", style = MaterialTheme.typography.labelLarge, color = if (isSelected) BrandCyan else TextSecondary)
                        }
                    }
                }
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// VIDEO LIST SCREEN (inside a folder)
// ═════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryFilterSortSheet(
    currentSort: HistorySortOrder,
    currentLayout: LayoutMode,
    onSortSelected: (HistorySortOrder) -> Unit,
    onLayoutSelected: (LayoutMode) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        containerColor   = SurfaceNavy,
        shape            = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        tonalElevation   = 0.dp,
        dragHandle = { BottomSheetHandle() }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 48.dp)
        ) {
            BrandTitleText("History Settings", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(28.dp))

            Text("Sort History By", style = MaterialTheme.typography.sectionOverline.copy(color = TextSecondary))
            Spacer(modifier = Modifier.height(12.dp))

            HistorySortOrder.entries.forEach { order ->
                val isSelected = order == currentSort
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(RoundedCornerShape(16.dp))
                        .clickable { onSortSelected(order); onDismiss() },
                    color  = if (isSelected) BrandBlueDeep.copy(alpha = 0.2f) else Color.Transparent,
                    border = if (isSelected) BorderStroke(1.dp, BrandCyan.copy(alpha = 0.5f)) else null,
                    shape  = RoundedCornerShape(16.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(order.label, style = MaterialTheme.typography.titleMedium, color = if (isSelected) BrandCyan else Color.White)
                        if (isSelected) Icon(Icons.Default.Check, null, tint = BrandCyan, modifier = Modifier.size(20.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("View Mode", style = MaterialTheme.typography.sectionOverline.copy(color = TextSecondary))
            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf(LayoutMode.LIST to painterResource(R.drawable.ic_list_custom), LayoutMode.GRID to painterResource(R.drawable.ic_grid_custom)).forEach { (mode, painter) ->
                    val isSelected = currentLayout == mode
                    Surface(
                        modifier = Modifier.weight(1f).clip(RoundedCornerShape(20.dp)).clickable { onLayoutSelected(mode); onDismiss() },
                        color  = if (isSelected) BrandBlueDeep.copy(alpha = 0.2f) else SurfaceRaised.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, if (isSelected) BrandCyan.copy(alpha = 0.5f) else OutlineFaint),
                        shape  = RoundedCornerShape(20.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(painter, null, tint = if (isSelected) BrandCyan else TextSecondary, modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(if (mode == LayoutMode.LIST) "List" else "Grid", style = MaterialTheme.typography.labelLarge, color = if (isSelected) BrandCyan else TextSecondary)
                        }
                    }
                }
            }
        }
    }
}

private @Composable fun BottomSheetHandle() {
    Box(modifier = Modifier.padding(vertical = 12.dp).size(40.dp, 4.dp).background(OutlineMedium, CircleShape))
}
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
    // All known folders, so "Move to another folder" can offer real
    // destinations -- this app derives folders from MediaStore buckets
    // rather than walking the filesystem, so the destination list is
    // exactly what Home already shows, nothing new to fetch.
    allFolders: List<VideoFolder> = emptyList(),
    // Fired after a rename/delete/move actually succeeds on disk, so the
    // caller (MainActivity) can refresh its MediaStore-backed lists. The
    // sheet/dialogs below handle the file I/O themselves; this is purely
    // "tell the parent its cached lists are now stale."
    onVideoChanged: () -> Unit = {}
) {
    val filteredVideos by remember(videos, searchQuery) {
        derivedStateOf {
            if (searchQuery.isEmpty()) videos
            else videos.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }
    }

    // Which video the long-press menu (and any follow-up dialog) is acting on.
    var actionVideo by remember { mutableStateOf<VideoFile?>(null) }
    var activeDialog by remember { mutableStateOf<VideoActionDialog?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        MainTopAppBar(
            title               = folderName,
            isSearchActive      = isSearchActive,
            searchQuery         = searchQuery,
            onSearchQueryChange = onSearchQueryChange,
            onToggleSearch      = onToggleSearch,
            showBackButton      = true,
            onBack              = onBack
        )

        if (filteredVideos.isEmpty()) {
            EmptyState(
                painter  = if (searchQuery.isEmpty()) painterResource(R.drawable.ic_play_custom) else painterResource(R.drawable.ic_search_custom),
                title    = if (searchQuery.isEmpty()) "No Videos" else "No Results",
                subtitle = if (searchQuery.isEmpty()) "This folder has no videos" else "Nothing matched \"$searchQuery\""
            )
        } else {
            LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)) {
                items(filteredVideos, key = { it.id }) { video ->
                    VideoItem(
                        video,
                        onClick = { onVideoClick(video) },
                        onLongClick = { actionVideo = video }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = OutlineFaint)
                }
            }
        }
    }

    VideoActionsHost(
        actionVideo   = actionVideo,
        activeDialog  = activeDialog,
        allFolders    = allFolders,
        onDismiss     = { actionVideo = null; activeDialog = null },
        onPickDialog  = { activeDialog = it },
        onVideoChanged = onVideoChanged
    )
}

// ═════════════════════════════════════════════════════════════════════════════
// VIDEO ITEM  (used in folder list + search results)
// ═════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VideoItem(video: VideoFile, onClick: () -> Unit, onLongClick: () -> Unit = {}) {
    val context = LocalContext.current
    Surface(
        modifier       = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        shape          = RoundedCornerShape(20.dp),
        color          = Color.Transparent,
        tonalElevation = 0.dp
    ) {
        Row(modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            // Thumbnail
            Box(
                modifier = Modifier
                    .size(130.dp, 80.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceRaisedAlt)
                    .border(1.dp, OutlineFaint, RoundedCornerShape(16.dp))
            ) {
                AsyncImage(
                    model = remember(video.uri) {
                        ImageRequest.Builder(context)
                            .data(video.uri)
                            .videoFrameMillis(1000) // fallback path only, see VideoThumbnailFetcher
                            .crossfade(true)
                            .build()
                    },
                    contentDescription = null,
                    modifier     = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Play overlay
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                    BrandPlayOverlay(size = 30.dp)
                }
                // Duration badge
                DurationBadge(
                    text = formatDuration(video.duration),
                    modifier = Modifier.align(Alignment.BottomEnd).padding(6.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text     = formatFileName(video.name),
                    style    = MaterialTheme.typography.titleMedium,
                    color    = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    lineHeight = 20.sp,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(formatSize(video.size), style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                    if (video.width > 0 && video.height > 0) {
                        Box(modifier = Modifier.size(3.dp).background(TextTertiary.copy(alpha = 0.4f), CircleShape))
                        val is4K = video.height >= 2160
                        val resLabel = when {
                            is4K -> "4K"
                            video.height >= 1080 -> "1080p"
                            video.height >= 720  -> "720p"
                            else                 -> "${video.height}p"
                        }
                        Surface(
                            color  = if (is4K) BrandCyan.copy(alpha = 0.15f) else BrandBlueDeep.copy(alpha = 0.25f),
                            shape  = RoundedCornerShape(5.dp),
                            border = BorderStroke(1.dp, if (is4K) BrandCyan.copy(alpha = 0.6f) else BrandCyan.copy(alpha = 0.25f))
                        ) {
                            Text(
                                text = resLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (is4K) BrandCyan else BrandCyan.copy(alpha = 0.8f),
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                                fontWeight = if (is4K) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// VIDEO ACTIONS  (long-press menu: rename / delete / details / move / playlist)
// ═════════════════════════════════════════════════════════════════════════════

/** Which follow-up dialog is currently showing for the long-pressed video, if any. */
enum class VideoActionDialog {
    MENU, RENAME, DELETE, DETAILS, MOVE, ADD_TO_PLAYLIST
}

/**
 * Owns the whole long-press flow for a single video: the action menu sheet,
 * and whichever follow-up dialog the user picks. Lives as one host so
 * VideoListScreen (and anywhere else VideoItem is used) just needs to set
 * `actionVideo` on long-press and render this once.
 */
@Composable
fun VideoActionsHost(
    actionVideo: VideoFile?,
    activeDialog: VideoActionDialog?,
    allFolders: List<VideoFolder>,
    onDismiss: () -> Unit,
    onPickDialog: (VideoActionDialog) -> Unit,
    onVideoChanged: () -> Unit
) {
    val video = actionVideo ?: return
    val context = LocalContext.current

    // Launches the system consent dialog for scoped-storage write/delete
    // requests (RecoverableSecurityException / createWriteRequest /
    // createDeleteRequest all surface here). On success, simply replays the
    // same intent that's pending -- the caller stores it so the retry knows
    // what to do.
    var pendingRetry by remember { mutableStateOf<(() -> Unit)?>(null) }
    val consentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            pendingRetry?.invoke()
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
                consentLauncher.launch(
                    androidx.activity.result.IntentSenderRequest.Builder(result.intentSender).build()
                )
            }
        }
    }

    when (activeDialog ?: VideoActionDialog.MENU) {
        VideoActionDialog.MENU -> {
            VideoActionMenuSheet(
                video = video,
                onDismiss = onDismiss,
                onRename = { onPickDialog(VideoActionDialog.RENAME) },
                onDelete = { onPickDialog(VideoActionDialog.DELETE) },
                onDetails = { onPickDialog(VideoActionDialog.DETAILS) },
                onMove = { onPickDialog(VideoActionDialog.MOVE) },
                onAddToPlaylist = { onPickDialog(VideoActionDialog.ADD_TO_PLAYLIST) }
            )
        }

        VideoActionDialog.RENAME -> {
            RenameVideoDialog(
                video = video,
                onDismiss = onDismiss,
                onConfirm = { newName ->
                    fun attempt() {
                        val result = renameVideoFile(context, video, newName)
                        handleResult(result, onSuccess = { onVideoChanged(); onDismiss() }, retry = ::attempt)
                    }
                    attempt()
                }
            )
        }

        VideoActionDialog.DELETE -> {
            DeleteVideoDialog(
                video = video,
                onDismiss = onDismiss,
                onConfirm = {
                    val result = deleteVideoFile(context, video)
                    // Unlike rename/move, a delete's consent flow IS the
                    // deletion -- createDeleteRequest's contract says the
                    // system performs ContentResolver.delete() itself once
                    // the user approves, so on retry we just report success
                    // rather than calling deleteVideoFile again against a
                    // URI that no longer exists.
                    handleResult(
                        result,
                        onSuccess = { onVideoChanged(); onDismiss() },
                        retry = { onVideoChanged(); onDismiss() }
                    )
                }
            )
        }

        VideoActionDialog.DETAILS -> {
            VideoDetailsDialog(video = video, onDismiss = onDismiss)
        }

        VideoActionDialog.MOVE -> {
            MoveToFolderSheet(
                video = video,
                folders = allFolders,
                onDismiss = onDismiss,
                onConfirm = { destination ->
                    fun attempt() {
                        val result = moveVideoFile(context, video, destination.path)
                        handleResult(result, onSuccess = { onVideoChanged(); onDismiss() }, retry = ::attempt)
                    }
                    attempt()
                }
            )
        }

        VideoActionDialog.ADD_TO_PLAYLIST -> {
            AddVideoToPlaylistSheet(
                video = video,
                onDismiss = onDismiss
            )
        }
    }

    if (errorMessage != null) {
        AlertDialog(
            onDismissRequest = { errorMessage = null },
            containerColor   = SurfaceRaised,
            shape = RoundedCornerShape(28.dp),
            title = { Text("Couldn't complete that", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White) },
            text  = { Text(errorMessage ?: "", style = MaterialTheme.typography.bodyMedium, color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = { errorMessage = null }) {
                    Text("OK", color = BrandCyan, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun VideoActionMenuSheet(
    video: VideoFile,
    onDismiss: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    onDetails: () -> Unit,
    onMove: () -> Unit,
    onAddToPlaylist: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        containerColor   = SurfaceNavy,
        shape            = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        tonalElevation   = 0.dp,
        dragHandle = {
            Box(modifier = Modifier.padding(vertical = 12.dp).size(40.dp, 4.dp).background(OutlineMedium, CircleShape))
        }
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
            // Header: thumbnail-less compact row with the video's name, so
            // it's unambiguous which file the menu below is acting on.
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceRaisedAlt),
                    contentAlignment = Alignment.Center
                ) {
                    // Glow for header icon
                    Box(
                        modifier = Modifier.fillMaxSize().background(
                            Brush.radialGradient(
                                colors = listOf(BrandCyan.copy(alpha = 0.2f), Color.Transparent)
                            )
                        )
                    )
                    Icon(painterResource(R.drawable.ic_play_custom), null, tint = BrandCyan, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = video.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = OutlineFaint)
            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                VideoActionRow(painterResource(R.drawable.ic_rename_custom), "Rename", onClick = onRename)
                VideoActionRow(painterResource(R.drawable.ic_playlist_add_custom), "Add to Playlist", onClick = onAddToPlaylist)
                VideoActionRow(painterResource(R.drawable.ic_move_custom), "Move to Folder", onClick = onMove)
                VideoActionRow(painterResource(R.drawable.ic_info_custom), "Details", onClick = onDetails)
                VideoActionRow(
                    painterResource(R.drawable.ic_trash_custom),
                    "Delete",
                    tint = MaterialTheme.colorScheme.error,
                    onClick = onDelete
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun VideoActionRow(
    painter: Painter,
    label: String,
    tint: Color? = null,
    onClick: () -> Unit
) {
    val labelColor = tint ?: Color.White
    val iconColor = tint ?: TextSecondary
    val accent = tint ?: BrandCyan

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = SurfaceRaised.copy(alpha = 0.4f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, OutlineFaint)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accent.copy(alpha = 0.08f))
                    .border(1.dp, accent.copy(alpha = 0.12f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Glow
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(accent.copy(alpha = 0.25f), Color.Transparent)
                            )
                        )
                )
                Icon(painter, null, tint = iconColor, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(label, style = MaterialTheme.typography.titleMedium, color = labelColor)
        }
    }
}

// ── Rename ──────────────────────────────────────────────────────────────────

@Composable
private fun RenameVideoDialog(
    video: VideoFile,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val nameWithoutExt = remember(video.path) { File(video.path).nameWithoutExtension }
    var text by remember { mutableStateOf(nameWithoutExt) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = SurfaceRaised,
        shape = RoundedCornerShape(28.dp),
        title = { Text("Rename Video", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White) },
        text = {
            Column {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = BrandCyan,
                        unfocusedBorderColor = OutlineMedium,
                        focusedTextColor     = Color.White,
                        unfocusedTextColor   = Color.White,
                        cursorColor          = BrandCyan
                    )
                )
                val ext = File(video.path).extension
                if (ext.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(".$ext", style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = text.isNotBlank() && text.trim() != nameWithoutExt,
                onClick = { onConfirm(text.trim()) }
            ) {
                Text("Rename", color = BrandCyan, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
        }
    )
}

// ── Delete ──────────────────────────────────────────────────────────────────

@Composable
private fun DeleteVideoDialog(
    video: VideoFile,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = SurfaceRaised,
        shape = RoundedCornerShape(28.dp),
        title = {
            Text("Delete \"${video.name}\"?", style = MaterialTheme.typography.titleLarge, color = Color.White)
        },
        text = {
            Text(
                "This permanently deletes the video file from your device. This can't be undone.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Delete", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = BrandCyan) }
        }
    )
}

// ── Details ─────────────────────────────────────────────────────────────────

@Composable
private fun VideoDetailsDialog(
    video: VideoFile,
    onDismiss: () -> Unit
) {
    val resolution = if (video.width > 0 && video.height > 0) "${video.width} × ${video.height}" else "Unknown"
    val dateModified = remember(video.path) {
        runCatching {
            val f = File(video.path)
            if (f.exists()) {
                java.text.SimpleDateFormat("MMM d, yyyy 'at' h:mm a", java.util.Locale.getDefault())
                    .format(java.util.Date(f.lastModified()))
            } else null
        }.getOrNull() ?: "Unknown"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = SurfaceRaised,
        shape = RoundedCornerShape(28.dp),
        title = { Text("Details", style = MaterialTheme.typography.titleLarge, color = Color.White) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                DetailRow("Name", video.name)
                DetailRow("Location", video.path)
                DetailRow("Size", formatSize(video.size))
                DetailRow("Duration", formatDuration(video.duration))
                DetailRow("Resolution", resolution)
                DetailRow("Last Modified", dateModified)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close", color = BrandCyan) }
        }
    )
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.sectionOverline)
        Spacer(modifier = Modifier.height(2.dp))
        Text(value, style = MaterialTheme.typography.bodyMedium, color = Color.White)
    }
}

// ── Move to folder ───────────────────────────────────────────────────────────

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun MoveToFolderSheet(
    video: VideoFile,
    folders: List<VideoFolder>,
    onDismiss: () -> Unit,
    onConfirm: (VideoFolder) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val currentFolderPath = remember(video.path) { File(video.path).parent }
    val destinations = remember(folders, currentFolderPath) {
        folders.filter { it.path != currentFolderPath }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        containerColor   = SurfaceNavy,
        shape            = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        tonalElevation   = 0.dp,
        dragHandle = {
            Box(modifier = Modifier.padding(vertical = 12.dp).size(40.dp, 4.dp).background(OutlineMedium, CircleShape))
        }
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 48.dp)) {
            Text(
                "Move \"${video.name}\"",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text("Choose a destination folder", style = MaterialTheme.typography.bodySmall, color = TextTertiary)
            Spacer(modifier = Modifier.height(16.dp))

            if (destinations.isEmpty()) {
                Text(
                    "No other folders available to move into.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)
                )
            } else {
                LazyColumn(modifier = Modifier.heightIn(max = 440.dp)) {
                    items(destinations, key = { it.path }) { folder ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onConfirm(folder) }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(SurfaceRaisedAlt),
                                contentAlignment = Alignment.Center
                            ) {
                                PremiumFolderIcon(modifier = Modifier.size(24.dp))
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(folder.name, style = MaterialTheme.typography.titleMedium, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text("${folder.videoCount} ${if (folder.videoCount == 1) "Video" else "Videos"}", style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                            }
                            Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, null, tint = TextTertiary, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }
    }
}

// ── Add to playlist ──────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddVideoToPlaylistSheet(
    video: VideoFile,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val playlists = remember { mutableStateListOf<Playlist>().also { it.addAll(loadPlaylists(context)) } }
    var showCreateField by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    fun persist() = savePlaylists(context, playlists)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        containerColor   = SurfaceNavy,
        shape            = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        tonalElevation   = 0.dp,
        dragHandle = {
            Box(modifier = Modifier.padding(vertical = 12.dp).size(40.dp, 4.dp).background(OutlineMedium, CircleShape))
        }
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 48.dp)) {
            Text(
                "Add to Playlist",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                video.name,
                style = MaterialTheme.typography.bodySmall,
                color = TextTertiary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (playlists.isEmpty() && !showCreateField) {
                Text(
                    "You don't have any playlists yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                )
            } else {
                LazyColumn(modifier = Modifier.heightIn(max = 360.dp)) {
                    items(playlists, key = { it.id }) { pl ->
                        val alreadyIn = video.path in pl.videoPaths
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = !alreadyIn) {
                                    val idx = playlists.indexOfFirst { it.id == pl.id }
                                    if (idx >= 0) {
                                        playlists[idx] = pl.copy(videoPaths = pl.videoPaths + video.path)
                                        persist()
                                    }
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(SurfaceRaisedAlt),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.AutoMirrored.Filled.PlaylistPlay, null, tint = BrandCyan, modifier = Modifier.size(22.dp))
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(pl.name, style = MaterialTheme.typography.titleMedium, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text("${pl.videoPaths.size} video${if (pl.videoPaths.size == 1) "" else "s"}", style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                            }
                            if (alreadyIn) {
                                Icon(Icons.Default.Check, null, tint = BrandCyan, modifier = Modifier.size(20.dp))
                            } else {
                                Icon(Icons.Default.Add, null, tint = TextSecondary, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = OutlineFaint)
            Spacer(modifier = Modifier.height(12.dp))

            if (showCreateField) {
                OutlinedTextField(
                    value = newPlaylistName,
                    onValueChange = { newPlaylistName = it },
                    placeholder = { Text("Playlist name", color = TextTertiary) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = BrandCyan,
                        unfocusedBorderColor = OutlineMedium,
                        focusedTextColor     = Color.White,
                        unfocusedTextColor   = Color.White,
                        cursorColor          = BrandCyan
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextButton(onClick = { showCreateField = false; newPlaylistName = "" }) {
                        Text("Cancel", color = TextSecondary)
                    }
                    Button(
                        enabled = newPlaylistName.isNotBlank(),
                        onClick = {
                            val pl = Playlist(
                                id = System.currentTimeMillis().toString(),
                                name = newPlaylistName.trim(),
                                videoPaths = listOf(video.path)
                            )
                            playlists.add(0, pl)
                            persist()
                            showCreateField = false
                            newPlaylistName = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
                    ) {
                        Text("Create & Add")
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showCreateField = true }
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, null, tint = BrandCyan, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(14.dp))
                    Text("Create New Playlist", style = MaterialTheme.typography.titleMedium, color = BrandCyan)
                }
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// RECENTS SCREEN
// ═════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun RecentsScreen(
    entries: List<RecentEntry>,
    isSearchActive: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onToggleSearch: () -> Unit,
    onVideoClick: (VideoFile) -> Unit,
    onRemoveEntry: (Long) -> Unit = {},
    onClearDay: (List<Long>) -> Unit = {}
) {
    var layoutMode by remember { mutableStateOf(LayoutMode.LIST) }
    var sortOrder by remember { mutableStateOf(HistorySortOrder.RECENT) }
    var showFilterSheet by remember { mutableStateOf(false) }

    val totalDuration = remember(entries) {
        entries.sumOf { it.video.duration }
    }

    val sortedEntries by remember(entries, searchQuery, sortOrder) {
        derivedStateOf {
            val filtered = if (searchQuery.isEmpty()) entries
            else entries.filter { it.video.name.contains(searchQuery, ignoreCase = true) }

            when (sortOrder) {
                HistorySortOrder.RECENT -> filtered.sortedByDescending { it.playedAt }
                HistorySortOrder.OLDEST -> filtered.sortedBy { it.playedAt }
                HistorySortOrder.NAME -> filtered.sortedBy { it.video.name.lowercase() }
                HistorySortOrder.DURATION -> filtered.sortedByDescending { it.video.duration }
            }
        }
    }

    val groupedEntries = remember(sortedEntries) {
        derivedStateOf {
            sortedEntries.groupBy { dayLabel(it.playedAt) }
        }
    }.value

    if (showFilterSheet) {
        HistoryFilterSortSheet(
            currentSort = sortOrder,
            currentLayout = layoutMode,
            onSortSelected = { sortOrder = it },
            onLayoutSelected = { layoutMode = it },
            onDismiss = { showFilterSheet = false }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        MainTopAppBar(
            title = "History",
            isSearchActive = isSearchActive,
            searchQuery = searchQuery,
            onSearchQueryChange = onSearchQueryChange,
            onToggleSearch = onToggleSearch,
            currentLayout = layoutMode,
            onToggleLayout = { layoutMode = if (layoutMode == LayoutMode.LIST) LayoutMode.GRID else LayoutMode.LIST },
            onShowFilter = { showFilterSheet = true }
        )

        when {
            entries.isEmpty() -> {
                EmptyState(
                    painter = painterResource(R.drawable.ic_recent_custom),
                    title = "No History",
                    subtitle = "Videos you watch will appear here"
                )
            }
            sortedEntries.isEmpty() && searchQuery.isNotEmpty() -> {
                EmptyState(
                    painter = painterResource(R.drawable.ic_search_custom),
                    title = "No Results",
                    subtitle = "Nothing matched \"$searchQuery\""
                )
            }
            else -> {
                LazyColumn(contentPadding = PaddingValues(bottom = 100.dp)) {
                    // History Stats Header
                    if (searchQuery.isEmpty()) {
                        item {
                            HistoryStatsHeader(entries.size, totalDuration)
                        }
                    }
// ...

                    groupedEntries.forEach { (label, dayEntries) ->
                        item(key = label) {
                            HistoryDayHeader(
                                label = label,
                                onClearDay = { onClearDay(dayEntries.map { it.video.id }) }
                            )
                        }

                        if (layoutMode == LayoutMode.LIST) {
                            items(dayEntries, key = { "${it.video.id}_${it.playedAt}" }) { entry ->
                                RecentVideoItem(
                                    video = entry.video,
                                    playedAt = entry.playedAt,
                                    onClick = { onVideoClick(entry.video) },
                                    onRemove = { onRemoveEntry(entry.video.id) }
                                )
                            }
                        } else {
                            // Grid view within the day
                            item {
                                FlowRow(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    maxItemsInEachRow = 2
                                ) {
                                    dayEntries.forEach { entry ->
                                        Box(modifier = Modifier.weight(1f)) {
                                            RecentVideoGridItem(
                                                video = entry.video,
                                                playedAt = entry.playedAt,
                                                onClick = { onVideoClick(entry.video) },
                                                onRemove = { onRemoveEntry(entry.video.id) }
                                            )
                                        }
                                    }
                                    // Fill the gap if odd number of items
                                    if (dayEntries.size % 2 != 0) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryStatsHeader(count: Int, totalDuration: Long) {
    PremiumCardSurface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        cornerRadius = 24.dp
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(BrandCyan.copy(alpha = 0.1f), CircleShape)
                    .border(1.dp, BrandCyan.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_recent_custom),
                    contentDescription = null,
                    tint = BrandCyan,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "$count Videos Watched",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
                Text(
                    text = "Total library time: ${formatDuration(totalDuration)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary
                )
            }
        }
    }
}

@Composable
private fun HistoryDayHeader(
    label: String,
    onClearDay: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = BgBase.copy(alpha = 0.95f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(14.dp)
                        .background(
                            Brush.verticalGradient(listOf(BrandHighlight, BrandCyan)),
                            RoundedCornerShape(2.dp)
                        )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium.copy(
                        brush = Brush.linearGradient(listOf(Color.White, BrandHighlight, BrandCyan))
                    )
                )
            }

            Text(
                text = "Clear",
                style = MaterialTheme.typography.labelMedium,
                color = BrandCyan.copy(alpha = 0.7f),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onClearDay() }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
fun RecentVideoItem(
    video: VideoFile,
    playedAt: Long = 0L,
    onClick: () -> Unit,
    onRemove: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val timeStr = remember(playedAt) {
        if (playedAt <= 0L) ""
        else java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault()).format(java.util.Date(playedAt))
    }

    Surface(
        onClick        = onClick,
        modifier       = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape          = RoundedCornerShape(20.dp),
        color          = GlassSurface,
        border         = BorderStroke(1.dp, OutlineFaint),
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail
            Box(
                modifier = Modifier
                    .size(120.dp, 75.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(SurfaceRaisedAlt)
                    .border(0.5.dp, OutlineFaint, RoundedCornerShape(14.dp))
            ) {
                AsyncImage(
                    model = remember(video.uri) {
                        ImageRequest.Builder(context)
                            .data(video.uri)
                            .videoFrameMillis(1000)
                            .crossfade(true)
                            .build()
                    },
                    contentDescription = null,
                    modifier     = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    BrandPlayOverlay(size = 28.dp)
                }
                DurationBadge(
                    text = formatDuration(video.duration),
                    modifier = Modifier.align(Alignment.BottomEnd).padding(6.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = formatFileName(video.name),
                    style = MaterialTheme.typography.titleMedium, // Removed the bold override
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    lineHeight = 22.sp, // Matched the new typography line height for better spacing
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (timeStr.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = TextTertiary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = timeStr,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextTertiary
                            )
                        }
                    }
                    Text(
                        text = formatSize(video.size),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextTertiary
                    )
                }
            }

            if (onRemove != null) {
                IconButton(onClick = onRemove) {
                    Icon(
                        painter = painterResource(R.drawable.ic_close_custom),
                        contentDescription = "Remove",
                        tint = TextTertiary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun RecentVideoGridItem(
    video: VideoFile,
    playedAt: Long = 0L,
    onClick: () -> Unit,
    onRemove: (() -> Unit)? = null
) {
    val context = LocalContext.current
    PremiumCardSurface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick),
        cornerRadius = 24.dp,
        content = {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                        .background(SurfaceRaisedAlt)
                ) {
                    AsyncImage(
                        model = remember(video.uri) {
                            ImageRequest.Builder(context)
                                .data(video.uri)
                                .videoFrameMillis(1000)
                                .crossfade(true)
                                .build()
                        },
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                        BrandPlayOverlay(size = 32.dp)
                    }
                    DurationBadge(
                        text = formatDuration(video.duration),
                        modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp)
                    )

                    if (onRemove != null) {
                        IconButton(
                            onClick = onRemove,
                            modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)
                        ) {
                            Surface(
                                color = Color.Black.copy(alpha = 0.5f),
                                shape = CircleShape,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_close_custom),
                                        contentDescription = "Remove",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = formatFileName(video.name),
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White,
                        maxLines = 2,
                        lineHeight = 18.sp,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = formatSize(video.size),
                            style = MaterialTheme.typography.labelSmall,
                            color = TextTertiary
                        )
                        if (video.width > 0 && video.height > 0) {
                            Box(modifier = Modifier.size(2.dp).background(TextTertiary.copy(alpha = 0.4f), CircleShape))
                            val is4K = video.height >= 2160
                            val resLabel = if (is4K) "4K" else if (video.height >= 1080) "1080p" else "${video.height}p"

                            Surface(
                                color = if (is4K) BrandCyan.copy(alpha = 0.15f) else BrandBlueDeep.copy(alpha = 0.25f),
                                shape = RoundedCornerShape(4.dp),
                                border = BorderStroke(0.5.dp, if (is4K) BrandCyan.copy(alpha = 0.6f) else BrandCyan.copy(alpha = 0.25f))
                            ) {
                                Text(
                                    text = resLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    color = if (is4K) BrandCyan else BrandCyan.copy(alpha = 0.8f),
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 0.dp),
                                    fontWeight = if (is4K) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }
    )
}

private fun dayLabel(timestamp: Long): String {
    val date = java.util.Date(timestamp)
    val now = java.util.Calendar.getInstance()
    val then = java.util.Calendar.getInstance().apply { time = date }

    return when {
        now.get(java.util.Calendar.YEAR) == then.get(java.util.Calendar.YEAR) &&
                now.get(java.util.Calendar.DAY_OF_YEAR) == then.get(java.util.Calendar.DAY_OF_YEAR) -> "Today"

        now.get(java.util.Calendar.YEAR) == then.get(java.util.Calendar.YEAR) &&
                now.get(java.util.Calendar.DAY_OF_YEAR) - then.get(java.util.Calendar.DAY_OF_YEAR) == 1 -> "Yesterday"

        else -> java.text.SimpleDateFormat("MMMM d, yyyy", java.util.Locale.getDefault()).format(date)
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// SHARED HELPERS
// ═════════════════════════════════════════════════════════════════════════════

@Composable
fun PremiumButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier),
        color    = Color.Transparent,
        border   = BorderStroke(
            1.dp,
            if (enabled) {
                Brush.linearGradient(
                    colors = listOf(
                        BrandCyan.copy(alpha = 0.55f),
                        BrandIceBlue.copy(alpha = 0.35f),
                        BrandVioletWhisper.copy(alpha = 0.25f)
                    )
                )
            } else {
                Brush.linearGradient(listOf(OutlineFaint, OutlineFaint))
            }
        ),
        shape    = RoundedCornerShape(20.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    if (enabled) {
                        Brush.linearGradient(
                            colors = listOf(
                                BrandBlueDeep.copy(alpha = 0.45f),
                                SurfaceRaised.copy(alpha = 0.85f)
                            )
                        )
                    } else {
                        Brush.linearGradient(listOf(SurfaceRaised, SurfaceRaised))
                    }
                )
                .padding(horizontal = 24.dp, vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (enabled) BrandHighlight else TextTertiary
            )
        }
    }
}

@Composable
fun PremiumIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    tint: Color = Color.White
) {
    Surface(
        modifier = modifier.size(48.dp).clip(CircleShape).clickable(onClick = onClick),
        color    = SurfaceRaised.copy(alpha = 0.6f),
        border   = BorderStroke(1.dp, OutlineFaint),
        shape    = CircleShape
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription, tint = tint, modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Row(
        modifier = Modifier.padding(start = 20.dp, top = 24.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(14.dp)
                .background(
                    Brush.verticalGradient(listOf(BrandHighlight, BrandCyan)),
                    RoundedCornerShape(2.dp)
                )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text          = text.uppercase(),
            style         = MaterialTheme.typography.labelMedium.copy(
                brush = Brush.linearGradient(listOf(Color.White, BrandHighlight, BrandCyan))
            ),
            fontWeight    = FontWeight.Bold,
            letterSpacing = 0.9.sp
        )
    }
}

@Composable
fun EmptyState(painter: Painter, title: String, subtitle: String) {
    EmptyState(title = title, subtitle = subtitle) {
        Icon(painter, null, tint = BrandCyan.copy(alpha = 0.75f), modifier = Modifier.size(44.dp))
    }
}

/**
 * Overload that takes a composable icon slot instead of a flat-tinted
 * Painter. Use this for full-colour brand icons like [PremiumFolderIcon]
 * whose gradient would be destroyed by a solid tint.
 */
@Composable
fun EmptyState(title: String, subtitle: String, icon: @Composable () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(40.dp)) {
            Box(
                modifier = Modifier
                    .size(112.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                BrandCyan.copy(alpha = 0.12f),
                                BrandBlueDeep.copy(alpha = 0.08f),
                                Color.Transparent
                            )
                        )
                    )
                    .border(
                        1.dp,
                        Brush.linearGradient(
                            colors = listOf(
                                BrandCyan.copy(alpha = 0.25f),
                                OutlineFaint
                            )
                        ),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                icon()
            }
            Spacer(modifier = Modifier.height(22.dp))
            BrandTitleText(
                text = title,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = TextTertiary, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun PlayUrlDialog(
    onDismiss: () -> Unit,
    onPlay: (String) -> Unit
) {
    var url by remember { mutableStateOf("") }
    val trimmed = url.trim()
    val scheme = remember(trimmed) { Uri.parse(trimmed).scheme?.lowercase() }
    val schemeError = trimmed.isNotEmpty() && scheme !in setOf("http", "https")

    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        PremiumCardSurface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            cornerRadius = 32.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(28.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Icon
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(BrandCyan.copy(alpha = 0.15f), Color.Transparent)
                            )
                        )
                        .border(1.dp, OutlineFaint, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_url_custom),
                        contentDescription = null,
                        tint = BrandCyan,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Stream from URL",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Paste a video link to play it directly",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextTertiary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(28.dp))

                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    placeholder = { Text("https://example.com/video.mp4", color = TextTertiary) },
                    singleLine = true,
                    isError = schemeError,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandCyan,
                        unfocusedBorderColor = OutlineMedium,
                        focusedContainerColor = SurfaceRaised.copy(alpha = 0.3f),
                        unfocusedContainerColor = SurfaceRaised.copy(alpha = 0.3f),
                        cursorColor = BrandCyan,
                        errorBorderColor = MaterialTheme.colorScheme.error
                    ),
                    shape = RoundedCornerShape(16.dp)
                )

                if (schemeError) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Link must start with http:// or https://",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Cancel", color = TextSecondary, fontWeight = FontWeight.Bold)
                    }

                    PremiumButton(
                        text = "Play Now",
                        onClick = { onPlay(trimmed) },
                        enabled = trimmed.isNotEmpty() && !schemeError,
                        modifier = Modifier.weight(1.3f)
                    )
                }
            }
        }
    }
}

// Keep PlaceholderScreen so MainActivity.kt still compiles while you
// wire up the real screens from SettingsAndPlaylists.kt
@Composable
fun PlaceholderScreen(name: String) {
    EmptyState(painter = rememberVectorPainter(Icons.Default.Construction), title = name, subtitle = "Coming soon")
}