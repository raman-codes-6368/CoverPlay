package com.raman.CoverPlay.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.raman.CoverPlay.ui.theme.CoverPlayBaseTokens
import com.raman.CoverPlay.ui.theme.CoverPlayShapeTokens
import com.raman.CoverPlay.ui.theme.CoverPlaySolidCard
import com.raman.CoverPlay.ui.theme.CoverPlayTokens
import com.raman.CoverPlay.ui.theme.LocalAppUiSettings
import com.raman.CoverPlay.ui.theme.OutlineFaint

object CoverPlayCorners {
    val tag = CoverPlayBaseTokens.Radius.xs
    val inner = CoverPlayBaseTokens.Radius.md
    val tile = CoverPlayTokens.Radius.compactCard
    val poster = CoverPlayTokens.Radius.compactCard
    val card = CoverPlayTokens.Radius.card
    val compactCard = CoverPlayTokens.Radius.compactCard
    val dialog = 28.dp
    val sheet = CoverPlayTokens.Radius.sheet
}

enum class CoverPlayCardTier { Hero, Primary, Utility }

fun Modifier.posterLift(
    pressed: Boolean,
    enabled: Boolean = true
): Modifier = composed {
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) 0.985f else 1f,
        animationSpec = spring(dampingRatio = 0.9f, stiffness = 600f),
        label = "posterLiftScale"
    )
    this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

fun Modifier.coverPlayShimmer(
    enabled: Boolean = true,
    shimmerColor: Color = Color.Unspecified
): Modifier = composed {
    if (!enabled) return@composed this
    val sweep = if (shimmerColor != Color.Unspecified) shimmerColor
    else if (com.raman.CoverPlay.ui.theme.LocalAppUiSettings.current.palette.isDark) Color.White.copy(alpha = 0.10f)
    else Color.Black.copy(alpha = 0.06f)
    val transition = rememberInfiniteTransition(label = "shimmerTransition")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    this.drawWithContent {
        drawContent()
        val brush = Brush.linearGradient(
            colors = listOf(
                Color.Transparent,
                sweep,
                Color.Transparent,
            ),
            start = Offset(translateAnim - 200f, translateAnim - 200f),
            end = Offset(translateAnim, translateAnim)
        )
        drawRect(brush = brush)
    }
}

fun Modifier.heroStretchOverscroll(
    scrollOffset: Float = 0f
): Modifier = this.graphicsLayer {
    if (scrollOffset < 0) {
        val stretch = 1f + (-scrollOffset / 1000f).coerceAtMost(0.15f)
        scaleX = stretch
        scaleY = stretch
    }
}

@Composable
fun CoverPlayElevatedCard(
    modifier: Modifier = Modifier,
    tier: CoverPlayCardTier = CoverPlayCardTier.Primary,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    CoverPlaySolidCard(
        modifier = modifier,
        shape = RoundedCornerShape(CoverPlayCorners.card),
        onClick = onClick,
        onLongClick = onLongClick,
        selected = selected,
        raised = tier == CoverPlayCardTier.Hero,
        content = content
    )
}

@Composable
fun CoverPlayTonalCard(
    modifier: Modifier = Modifier,
    shape: Shape = CoverPlayShapeTokens.card,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    CoverPlaySolidCard(modifier = modifier, shape = shape, onClick = onClick, onLongClick = onLongClick, selected = selected, content = content)
}

@Composable
fun CoverPlayPosterCard(
    modifier: Modifier = Modifier,
    shape: Shape = CoverPlayShapeTokens.poster,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    CoverPlaySolidCard(modifier = modifier, shape = shape, onClick = onClick, onLongClick = onLongClick, selected = selected, raised = false, content = content)
}

@Composable
fun CoverPlayCardDivider(inset: Dp = 0.dp, modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxWidth()
            .padding(start = inset)
            .height(1.dp)
            .background(OutlineFaint)
    )
}
