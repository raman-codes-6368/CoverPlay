package com.example.myapplication.ui.theme

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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.res.painterResource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import android.view.WindowManager
import com.example.myapplication.R
import kotlin.math.*

// ── Organic Shape for Expressive Motion ──────────────────────────────────────

class OrganicShape(
    private val lobes: Int = 12
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path()
        val cx = size.width / 2f
        val cy = size.height / 2f
        
        // Use 90% of available space to avoid cropping peaks
        val maxPossibleRadius = min(size.width, size.height) / 2f
        val radius = maxPossibleRadius * 0.88f
        
        val points = 180

        for (i in 0..points) {
            val angle = 2f * PI.toFloat() * i / points
            // subtler wave for fluid motion
            val wave = sin(angle * lobes) * radius * 0.05f
            val r = radius + wave
            val x = cx + cos(angle) * r
            val y = cy + sin(angle) * r

            if (i == 0) path.moveTo(x, y)
            else path.lineTo(x, y)
        }
        path.close()
        return Outline.Generic(path)
    }
}

// ── Motion Tokens ────────────────────────────────────────────────────────────

object MotionSpec {
    val SoftSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMediumLow
    )
    val EmphasizedSpring = spring<Float>(
        dampingRatio = 0.75f,
        stiffness = 500f
    )
    val ExpressiveSpring = spring<Float>(
        dampingRatio = 0.65f,
        stiffness = 300f
    )
    val EmphasizedEasing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    const val DURATION_SHORT = 250
    const val DURATION_MEDIUM = 500
    const val DURATION_LONG = 800
}

@Composable
fun CinematicBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val palette = LocalAppUiSettings.current.palette
    val accent = LocalAppUiSettings.current.accent
    
    // ── Infinite Aurora Motion ──────────────────────────────────────────────
    val infinite = rememberInfiniteTransition(label = "aurora")
    
    val rotation1 by infinite.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(24000, easing = LinearEasing)),
        label = "r1"
    )
    val rotation2 by infinite.animateFloat(
        initialValue = 360f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(32000, easing = LinearEasing)),
        label = "r2"
    )
    
    val breathe by infinite.animateFloat(
        initialValue = 0.85f, targetValue = 1.15f,
        animationSpec = infiniteRepeatable(tween(4500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "b1"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.bg)
    ) {
        // Multi-layered ethereal aurora blobs
        Box(
            modifier = Modifier
                .offset(x = (-100).dp, y = (-120).dp)
                .size(500.dp)
                .graphicsLayer { 
                    rotationZ = rotation1
                    scaleX = breathe
                    scaleY = breathe
                }
                .background(
                    Brush.radialGradient(
                        colors = listOf(accent.copy(alpha = 0.08f), Color.Transparent)
                    ),
                    OrganicShape(lobes = 6)
                )
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 150.dp, y = 100.dp)
                .size(600.dp)
                .graphicsLayer { 
                    rotationZ = rotation2
                    scaleX = breathe * 0.9f
                    scaleY = breathe * 0.9f
                }
                .background(
                    Brush.radialGradient(
                        colors = listOf(IconPeriwinkle.copy(alpha = 0.06f), Color.Transparent)
                    ),
                    OrganicShape(lobes = 8)
                )
        )

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(accent.copy(alpha = 0.04f), Color.Transparent),
                        center = Offset(0f, 0f)
                    )
                )
        )

        content()
    }
}

@Composable
fun BrandTitleText(
    text: String,
    modifier: Modifier = Modifier,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.headlineLarge
) {
    val accent = LocalAppUiSettings.current.accent
    val dynamicGradient = remember(accent) {
        Brush.linearGradient(colors = listOf(IconSky, accent, IconPeriwinkle))
    }
    Text(
        text = text,
        style = style.copy(
            brush = dynamicGradient,
            fontWeight = FontWeight.Black
        ),
        modifier = modifier
    )
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun PremiumCardSurface(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 36.dp, // Very curvy
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val settings = LocalAppUiSettings.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = MotionSpec.EmphasizedSpring,
        label = "cardScale"
    )

    Surface(
        modifier = modifier
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .then(
                if (onClick != null || onLongClick != null) {
                    Modifier.combinedClickable(
                        interactionSource = interactionSource,
                        indication = androidx.compose.foundation.LocalIndication.current,
                        onClick = { onClick?.invoke() },
                        onLongClick = onLongClick
                    )
                } else Modifier
            ),
        shape = RoundedCornerShape(cornerRadius),
        color = if (selected) settings.palette.surfaceRaisedAlt else settings.palette.surfaceRaised,
        tonalElevation = 0.dp,
        border = if (selected) BorderStroke(2.dp, settings.accent) else BorderStroke(1.dp, settings.palette.outlineMedium.copy(alpha = 0.5f)),
        shadowElevation = if (isPressed) 2.dp else 4.dp
    ) {
        content()
    }
}

@Composable
fun PremiumButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: @Composable (() -> Unit)? = null
) {
    val settings = LocalAppUiSettings.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = MotionSpec.EmphasizedSpring,
        label = "btnScale"
    )

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .height(56.dp)
            .graphicsLayer(scaleX = scale, scaleY = scale),
        shape = CircleShape, // Pill shaped
        colors = ButtonDefaults.buttonColors(
            containerColor = settings.accent,
            contentColor = Color.White,
            disabledContainerColor = settings.palette.surfaceRaisedAlt,
            disabledContentColor = settings.palette.textTertiary
        ),
        interactionSource = interactionSource,
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 8.dp,
            pressedElevation = 2.dp
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                icon()
                Spacer(modifier = Modifier.width(10.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
            )
        }
    }
}

@Composable
fun PremiumIconButton(
    icon: Any, // Painter or ImageVector
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    backgroundColor: Color? = null,
    useShadowOnly: Boolean = false, // Added for player screen
    tint: Color? = null
) {
    val settings = LocalAppUiSettings.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1f,
        animationSpec = MotionSpec.EmphasizedSpring,
        label = "iconBtnScale"
    )

    val rotation by animateFloatAsState(
        targetValue = if (isPressed) 12f else 0f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMedium),
        label = "iconBtnRotation"
    )

    Box(
        modifier = modifier
            .size(48.dp)
            .graphicsLayer(scaleX = scale, scaleY = scale, rotationZ = rotation)
            .then(
                if (useShadowOnly) {
                    Modifier.shadow(
                        elevation = 12.dp,
                        shape = CircleShape,
                        spotColor = Color.Black.copy(alpha = 0.8f)
                    )
                } else {
                    Modifier.clip(CircleShape).background(backgroundColor ?: settings.palette.surfaceRaisedAlt)
                }
            )
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        val finalTint = if (useShadowOnly) (tint ?: Color.White) else (tint ?: settings.palette.textPrimary)
        when (icon) {
            is ImageVector -> Icon(icon, null, tint = finalTint, modifier = Modifier.size(24.dp))
            is Painter -> Icon(icon, null, tint = finalTint, modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
fun BrandIconContainer(
    modifier: Modifier = Modifier,
    size: Dp = 52.dp,
    cornerRadius: Dp = 16.dp,
    active: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    val accent = LocalAppUiSettings.current.accent
    val settings = LocalAppUiSettings.current
    val infiniteTransition = rememberInfiniteTransition(label = "iconFluid")
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(9000, easing = LinearEasing)),
        label = "rotation"
    )
    
    val breathing by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(tween(2200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "breathing"
    )

    val dynamicGradient = remember(accent) {
        Brush.linearGradient(
            colors = listOf(IconSky, accent, IconPeriwinkle)
        )
    }

    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                if (active) {
                    scaleX = breathing
                    scaleY = breathing
                }
            },
        contentAlignment = Alignment.Center
    ) {
        if (active) {
            // Rotating Organic Background only
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { rotationZ = rotation }
                    .clip(OrganicShape(lobes = 6))
                    .background(dynamicGradient)
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(cornerRadius))
                    .background(settings.palette.surfaceRaisedAlt)
                    .border(1.2.dp, settings.palette.outlineMedium, RoundedCornerShape(cornerRadius))
            )
        }
        content()
    }
}

@Composable
fun DurationBadge(
    text: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color.Black.copy(alpha = 0.8f),
        shape = CircleShape,
        modifier = modifier
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.Black
            ),
            color = Color.White,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

/**
 * Expressive quality label resolver.
 */
fun qualityLabel(width: Int, height: Int): String {
    val longerEdge = maxOf(width, height)
    return when {
        longerEdge >= 3840 -> "4K"
        longerEdge >= 1920 -> "FHD"
        longerEdge >= 1280 -> "HD"
        else                -> "SD"
    }
}

/**
 * Expressive Quality Badge component with hierarchy coloring.
 */
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

/**
 * Expressive Duration Badge with hierarchy coloring.
 */
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

@Composable
fun BrandPlayOverlay(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    iconOverride: Painter? = null,
    label: String? = null
) {
    val accent = LocalAppUiSettings.current.accent
    val infiniteTransition = rememberInfiniteTransition(label = "playFluid")
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(10000, easing = LinearEasing)),
        label = "rotation"
    )
    
    val breathing by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "breathing"
    )

    val dynamicRadialGradient = remember(accent) {
        Brush.radialGradient(
            colors = listOf(IconPeriwinkle, accent, IconIndigo)
        )
    }

    Column(
        modifier = modifier.graphicsLayer {
            scaleX = breathing
            scaleY = breathing
        },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(size),
            contentAlignment = Alignment.Center
        ) {
            // Rotating Organic Aura only
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { rotationZ = rotation }
                    .clip(OrganicShape(lobes = 8))
                    .background(dynamicRadialGradient)
            )
            
            if (iconOverride != null) {
                Icon(
                    painter = iconOverride,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.5f)
                )
            } else {
                Icon(
                    painter = painterResource(R.drawable.ic_play_custom),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.55f)
                )
            }
        }
        
        if (label != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp
                ),
                color = Color.White
            )
        }
    }
}

@Composable
fun PremiumDialogCloseButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val textSecondary = LocalAppUiSettings.current.palette.textSecondary
    val surfaceRaisedAlt = LocalAppUiSettings.current.palette.surfaceRaisedAlt
    
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val rotation by animateFloatAsState(
        targetValue = if (isPressed) 90f else 0f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMedium),
        label = "closeRotation"
    )

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        color = surfaceRaisedAlt,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.size(36.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painter = androidx.compose.ui.res.painterResource(R.drawable.ic_close_custom),
                contentDescription = "Close",
                tint = textSecondary,
                modifier = Modifier
                    .size(20.dp)
                    .graphicsLayer { rotationZ = rotation }
            )
        }
    }
}

@Composable
fun PremiumDialogHeader(
    title: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null
) {
    val textPrimary = LocalAppUiSettings.current.palette.textPrimary
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Black,
                color = textPrimary
            )
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (trailing != null) {
                trailing()
                Spacer(modifier = Modifier.width(16.dp))
            }
            PremiumDialogCloseButton(onClick = onDismiss)
        }
    }
}

@Composable
fun PremiumDialog(
    onDismiss: () -> Unit,
    title: String,
    modifier: Modifier = Modifier.fillMaxWidth(0.92f),
    content: @Composable ColumnScope.() -> Unit
) {
    val surfaceNavy = LocalAppUiSettings.current.palette.surfaceNavy
    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = modifier,
        confirmButton = {},
        title = { PremiumDialogHeader(title = title, onDismiss = onDismiss) },
        text = {
            Column(modifier = Modifier.padding(top = 16.dp)) {
                content()
            }
        },
        shape = RoundedCornerShape(36.dp),
        containerColor = surfaceNavy
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumBottomSheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    skipPartiallyExpanded: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    val surfaceNavy = LocalAppUiSettings.current.palette.surfaceNavy
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = skipPartiallyExpanded)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = surfaceNavy,
        shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
        scrimColor = Color.Black.copy(alpha = 0.65f),
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 44.dp)
        ) {
            content()
        }
    }
}

@Composable
fun PremiumAlertDialog(
    onDismiss: () -> Unit,
    title: String,
    text: String,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable (() -> Unit)? = null
) {
    val settings = LocalAppUiSettings.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Black) },
        text = { Text(text, color = settings.palette.textSecondary) },
        confirmButton = confirmButton,
        dismissButton = dismissButton,
        shape = RoundedCornerShape(36.dp),
        containerColor = settings.palette.surfaceNavy
    )
}

@Composable
fun PremiumTopBarAction(icon: Painter, onClick: () -> Unit) {
    PremiumIconButton(icon = icon, onClick = onClick)
}

// ── Expressive additions (v4, "Iris Bloom") ─────────────────────────────────
// Circular / curvy components that lean into Material 3 Expressive motion
// and the icon's blooming-petal identity.

/**
 * Large circular play button with a soft brand-gradient ring and a bloom
 * of light behind it — the hero control on the player screen. Petals of
 * the app icon are echoed via the rotating soft glow.
 */
@Composable
fun BloomPlayButton(
    isPlaying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 84.dp,
    iconOverride: Painter? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1f,
        animationSpec = MotionSpec.ExpressiveSpring,
        label = "bloomScale"
    )
    val infinite = rememberInfiniteTransition(label = "bloomRotate")
    val baseRotation by infinite.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(9000, easing = LinearEasing)),
        label = "bloomRotation"
    )
    
    val breathing by infinite.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(tween(2200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "breathing"
    )

    // Interaction bump
    val bumpRotation = remember { Animatable(0f) }
    LaunchedEffect(isPlaying) {
        bumpRotation.animateTo(
            targetValue = bumpRotation.value + 20f,
            animationSpec = spring(dampingRatio = 0.65f, stiffness = 300f)
        )
    }

    Box(
        modifier = modifier
            .size(size + 24.dp)
            .graphicsLayer {
                scaleX = scale * breathing
                scaleY = scale * breathing
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        // Rotating organic shape background
        Box(
            modifier = Modifier
                .size(size)
                .graphicsLayer { rotationZ = baseRotation + bumpRotation.value }
                .clip(OrganicShape(lobes = 8))
                .background(BrandGradient)
        )

        if (iconOverride != null) {
            Icon(
                painter = iconOverride,
                contentDescription = "Action",
                tint = Color.White,
                modifier = Modifier.size(size * 0.5f)
            )
        } else {
            AnimatedContent(
                targetState = isPlaying,
                transitionSpec = {
                    (scaleIn(spring(0.7f, 400f)) + fadeIn()).togetherWith(scaleOut(spring(0.7f, 400f)) + fadeOut())
                },
                label = "playPauseMorph"
            ) { playing ->
                Icon(
                    painter = if (playing) painterResource(R.drawable.ic_pause_custom)
                    else painterResource(R.drawable.ic_play_custom),
                    contentDescription = if (playing) "Pause" else "Play",
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.5f)
                )
            }
        }
    }
}

/**
 * Fully circular, pill-collapsing segmented control for picking a
 * [com.example.myapplication.ui.theme.ThemeMode] (Light / Dark / AMOLED).
 * Selected segment gets a brand-gradient pill that springs between options.
 */
@Composable
fun ThemeModeSelector(
    selected: ThemeMode,
    onSelect: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val settings = LocalAppUiSettings.current
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = CircleShape,
        color = settings.palette.surfaceRaised,
        border = BorderStroke(1.dp, settings.palette.outlineMedium)
    ) {
        Row(modifier = Modifier.padding(4.dp)) {
            ThemeMode.entries.forEach { mode ->
                val isSelected = mode == selected
                val bg by animateColorAsState(
                    targetValue = if (isSelected) settings.accent else Color.Transparent,
                    label = "modeBg"
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(CircleShape)
                        .background(bg)
                        .clickable { onSelect(mode) }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = mode.label,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium
                        ),
                        color = if (isSelected) Color.White else settings.palette.textSecondary
                    )
                }
            }
        }
    }
}

/**
 * Expressive circular swatch used for accent-color pickers — a filled
 * blob that "blooms" outward (extra ring + scale) when selected.
 */
@Composable
fun BloomColorSwatch(
    color: Color,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1f else 0.82f,
        animationSpec = MotionSpec.ExpressiveSpring, label = "swatchScale"
    )
    Box(
        modifier = modifier.size(size + 12.dp),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Box(
                modifier = Modifier
                    .size(size + 12.dp)
                    .clip(CircleShape)
                    .border(2.dp, color, CircleShape)
            )
        }
        Box(
            modifier = Modifier
                .size(size)
                .graphicsLayer(scaleX = scale, scaleY = scale)
                .clip(CircleShape)
                .background(color)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Icon(
                    painter = painterResource(R.drawable.ic_check_custom),
                    contentDescription = "Selected",
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.5f)
                )
            }
        }
    }
}

/** Curvy blob-shaped background decoration for empty states / splash, echoing the icon's petals. */
@Composable
fun PetalBloomDecoration(modifier: Modifier = Modifier, alpha: Float = 0.12f) {
    Box(
        modifier = modifier
            .size(260.dp)
            .graphicsLayer(alpha = alpha)
            .background(
                Brush.radialGradient(listOf(IconSky, IconIndigo, Color.Transparent)),
                RoundedCornerShape(50)
            )
    )
}

@Composable
fun PremiumSolidSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(36.dp),
    withAccentGlow: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    val settings = LocalAppUiSettings.current
    Surface(
        modifier = modifier,
        shape = shape,
        color = settings.palette.surfaceNavy,
        tonalElevation = 6.dp,
        border = BorderStroke(1.dp, settings.palette.outlineMedium)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            content()
        }
    }
}

/**
 * Expressive media slider with a thick track and a "blooming" thumb.
 * The thumb scales up when interacting, and the track is chunky and soft.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpressiveSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    accentColor: Color = LocalAppUiSettings.current.accent
) {
    Slider(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        valueRange = valueRange,
        thumb = {
            Surface(
                modifier = Modifier.size(18.dp),
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 4.dp,
                border = BorderStroke(4.dp, accentColor)
            ) {}
        },
        track = { sliderState ->
            val fraction = (value - valueRange.start) / (valueRange.endInclusive - valueRange.start)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.15f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction)
                        .fillMaxHeight()
                        .background(accentColor)
                )
            }
        }
    )
}

/**
 * Curvy, high-elevation card specifically for album artwork.
 * Uses a deep shadow and soft corners to feel "premium".
 */
@Composable
fun PremiumImageCard(
    painter: Painter,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(32.dp),
    contentDescription: String? = null
) {
    Surface(
        modifier = modifier
            .aspectRatio(1f)
            .shadow(24.dp, shape, spotColor = Color.Black.copy(alpha = 0.45f)),
        shape = shape,
    ) {
        androidx.compose.foundation.Image(
            painter = painter,
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize(),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop
        )
    }
}

/**
 * Semi-transparent "glass" surface for overlays or mini-player backgrounds.
 */
@Composable
fun GlassmorphicSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(28.dp),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(Color.White.copy(alpha = 0.06f))
            .border(1.dp, Color.White.copy(alpha = 0.1f), shape)
    ) {
        content()
    }
}

/**
 * An expressive list item with soft corners and clear hierarchy.
 */
@Composable
fun PremiumListItem(
    title: String,
    subtitle: String,
    leading: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null
) {
    val settings = LocalAppUiSettings.current
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            leading()
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = settings.palette.textPrimary,
                    maxLines = 1
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = settings.palette.textSecondary,
                    maxLines = 1
                )
            }
            if (trailing != null) {
                trailing()
            }
        }
    }
}

/**
 * A generic wrapper that adds the "Expressive" springy scale-down effect 
 * to any clickable content. Perfect for custom buttons or cards.
 */
@Composable
fun PremiumScaleButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = MotionSpec.ExpressiveSpring,
        label = "scale"
    )

    Box(
        modifier = modifier
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center,
        content = content
    )
}

/**
 * A background with drifting, scaling "petals" that move in a soft, ethereal way.
 * Matches the "Iris Bloom" identity.
 */
@Composable
fun AnimatedPetalBackground(modifier: Modifier = Modifier) {
    val infinite = rememberInfiniteTransition(label = "petalMotion")
    val scale1 by infinite.animateFloat(
        initialValue = 0.8f, targetValue = 1.1f,
        animationSpec = infiniteRepeatable(tween(10000, easing = LinearEasing), RepeatMode.Reverse),
        label = "p1"
    )
    val scale2 by infinite.animateFloat(
        initialValue = 1.05f, targetValue = 0.75f,
        animationSpec = infiniteRepeatable(tween(12000, easing = LinearEasing), RepeatMode.Reverse),
        label = "p2"
    )

    Box(modifier = modifier.fillMaxSize()) {
        PetalBloomDecoration(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 120.dp, y = (-60).dp)
                .graphicsLayer(scaleX = scale1, scaleY = scale1, rotationZ = 35f),
            alpha = 0.07f
        )
        PetalBloomDecoration(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-100).dp, y = 140.dp)
                .graphicsLayer(scaleX = scale2, scaleY = scale2, rotationZ = -15f),
            alpha = 0.05f
        )
    }
}

/**
 * Expressive sheet for player controls.
 */
@Composable
fun ExpressiveControlSheet(
    title: String,
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    PremiumDialog(onDismiss = onDismiss, title = title) {
        content()
    }
}

/**
 * A compact, expressive row for control sheets.
 */
@Composable
fun ControlSheetRow(
    icon: Painter,
    label: String,
    onClick: () -> Unit,
    trailing: @Composable (() -> Unit)? = null
) {
    val palette = LocalAppUiSettings.current.palette
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = palette.surfaceRaised.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = LocalAppUiSettings.current.accent, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = palette.textPrimary,
                modifier = Modifier.weight(1f)
            )
            if (trailing != null) {
                trailing()
            } else {
                Icon(
                    painter = painterResource(R.drawable.ic_chevron_right_custom),
                    null,
                    tint = palette.textTertiary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun ControlSheetGridItem(
    icon: Painter,
    onClick: () -> Unit,
    active: Boolean = false
) {
    val palette = LocalAppUiSettings.current.palette
    val accent = LocalAppUiSettings.current.accent
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(64.dp),
        shape = RoundedCornerShape(18.dp),
        color = if (active) accent.copy(alpha = 0.15f) else palette.surfaceRaised.copy(alpha = 0.5f),
        border = if (active) BorderStroke(1.5.dp, accent.copy(alpha = 0.5f)) else BorderStroke(1.dp, palette.outlineFaint)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painter = icon,
                null,
                tint = if (active) accent else palette.textPrimary,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

@Composable
fun ControlSheetGridItem(
    icon: ImageVector,
    onClick: () -> Unit,
    active: Boolean = false
) = ControlSheetGridItem(rememberVectorPainter(icon), onClick, active)

