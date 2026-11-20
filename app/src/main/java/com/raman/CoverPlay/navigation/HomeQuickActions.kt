package com.raman.CoverPlay.navigation

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.raman.CoverPlay.R
import com.raman.CoverPlay.components.navContentBottomPadding
import com.raman.CoverPlay.ui.theme.CoverPlayFab
import com.raman.CoverPlay.ui.theme.LocalAppUiSettings

object NavChrome {
    var collapsed by mutableStateOf(false)

    var fold by androidx.compose.runtime.mutableFloatStateOf(0f)

    val foldSpring = androidx.compose.animation.core.spring<Float>(dampingRatio = 1f, stiffness = 380f)
}

@Composable
fun CoverPlayHomeQuickActions(
    visible: Boolean,
    rescanning: Boolean,
    onOpenVideo: (Uri) -> Unit,
    onRescan: () -> Unit,
    modifier: Modifier = Modifier,
    bottomInset: androidx.compose.ui.unit.Dp = navContentBottomPadding(),
) {
    val ui = LocalAppUiSettings.current
    val context = LocalContext.current

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (_: Exception) {}
            onOpenVideo(uri)
        }
    }

    val spin = rememberInfiniteTransition(label = "rescanSpin")
    val angle by spin.animateFloat(
        0f, 360f, infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Restart), label = "rescanAngle"
    )

    AnimatedVisibility(
        visible = visible && !NavChrome.collapsed,
        modifier = modifier,
        enter = slideInVertically(tween(220, easing = androidx.compose.animation.core.FastOutSlowInEasing)) { it } + fadeIn(tween(160)),
        exit = slideOutVertically(tween(180, easing = androidx.compose.animation.core.FastOutSlowInEasing)) { it } + fadeOut(tween(120)),
    ) {
        Column(
            modifier = Modifier.padding(bottom = bottomInset),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CoverPlayFab(
                onClick = { picker.launch(arrayOf("video/*")) },
                size = 44.dp,
                containerColor = ui.palette.containerHigh,
                contentColor = ui.palette.textPrimary,
            ) {
                Icon(painterResource(R.drawable.ic_open_video_custom), "Open a video file")
            }
            CoverPlayFab(
                onClick = { if (!rescanning) onRescan() },
                size = 52.dp,
                containerColor = androidx.compose.ui.graphics.lerp(ui.palette.containerHigh, ui.accent, 0.28f),
                contentColor = ui.accent,
            ) {
                Icon(
                    painterResource(R.drawable.ic_rescan_custom),
                    "Rescan libraries",
                    modifier = Modifier.graphicsLayer { rotationZ = if (rescanning) angle else 0f }
                )
            }
        }
    }
}
