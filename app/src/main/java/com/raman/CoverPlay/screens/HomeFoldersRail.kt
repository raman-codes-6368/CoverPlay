package com.raman.CoverPlay.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.raman.CoverPlay.R
import com.raman.CoverPlay.VideoFolder
import com.raman.CoverPlay.components.CoverPlaySelectableCard
import com.raman.CoverPlay.ui.theme.CoverPlayBrandIconContainer
import com.raman.CoverPlay.ui.theme.CoverPlayDimens
import com.raman.CoverPlay.ui.theme.CoverPlaySectionHeader
import com.raman.CoverPlay.ui.theme.LocalAppUiSettings

@Composable
fun CoverPlayHomeFoldersRail(
    folders: List<VideoFolder>,
    onFolderClick: (VideoFolder) -> Unit,
) {
    Column {
        CoverPlaySectionHeader(title = "Folders", count = folders.size, topPadding = CoverPlayDimens.sectionSpacing)
        LazyRow(
            contentPadding = PaddingValues(horizontal = CoverPlayDimens.screenHorizontal),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(folders, key = { it.path }) { folder ->
                CoverPlayHomeFolderCard(folder = folder, onClick = { onFolderClick(folder) })
            }
        }
    }
}

@Composable
private fun CoverPlayHomeFolderCard(folder: VideoFolder, onClick: () -> Unit) {
    val s = LocalAppUiSettings.current
    CoverPlaySelectableCard(onClick = onClick, modifier = Modifier.width(148.dp)) {
        Column(Modifier.fillMaxWidth().padding(CoverPlayDimens.cardPadding)) {
            CoverPlayBrandIconContainer(active = true, size = CoverPlayDimens.iconContainer) {
                Icon(painterResource(R.drawable.ic_folder_custom), null, tint = s.accent, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.height(12.dp))
            Text(
                folder.name,
                style = MaterialTheme.typography.titleMedium,
                color = s.palette.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Text(
                "${folder.videoCount} ${if (folder.videoCount == 1) "video" else "videos"}",
                style = MaterialTheme.typography.bodySmall,
                color = s.palette.textSecondary,
                maxLines = 1
            )
        }
    }
}
