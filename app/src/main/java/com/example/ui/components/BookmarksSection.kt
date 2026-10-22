package com.example.ui.components
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.ui.graphics.Color
import com.example.ui.icons.LucideIcons

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BookmarkItem
import com.example.model.FileCategory
import androidx.compose.material3.Divider

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BookmarksSection(
    bookmarks: List<BookmarkItem>,
    onBookmarkClick: (BookmarkItem) -> Unit,
    onBookmarkLongClick: (BookmarkItem) -> Unit,
    onAddBookmarkClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("bookmarks_section")
    ) {
        // Section Header: simple and flat, no card background
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = LucideIcons.Bookmark,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Bookmarks",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Light,
                    fontSize = 17.sp,
                                color = MaterialTheme.colorScheme.onSurface
            )
            }


        }

        Divider(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
            thickness = 1.dp
        )

        // Two-column layout rows without card background
        val chunkedBookmarks = bookmarks.chunked(2)
        chunkedBookmarks.forEachIndexed { rowIndex, rowItems ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                for (item in rowItems) {
                    val icon = when (item.iconType) {
                        "DOWNLOADS" -> LucideIcons.Download
                        "DOCUMENTS" -> LucideIcons.FileText
                        "PICTURES" -> LucideIcons.Image
                        "MOVIES" -> LucideIcons.FileVideo
                        "MUSIC" -> LucideIcons.FileAudio
                        else -> LucideIcons.Folder
                    }

                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .combinedClickable(
                                onClick = { onBookmarkClick(item) },
                                onLongClick = { onBookmarkLongClick(item) }
                            )
                            .padding(vertical = 6.dp, horizontal = 4.dp)
                            .testTag("bookmark_item_${item.name}"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val iconTint = when (item.iconType) {
                        "PICTURES" -> FileCategory.IMAGE.color
                        "MOVIES" -> FileCategory.VIDEO.color
                        "MUSIC" -> FileCategory.AUDIO.color
                        "DOCUMENTS" -> FileCategory.DOCUMENT.color
                        else -> FileCategory.FOLDER.color
                    }
                    if (item.iconType == "CAMERA") {
                        Image(
                            painter = painterResource(id = R.drawable.camera),
                            contentDescription = item.name,
                            colorFilter = ColorFilter.tint(iconTint),
                            modifier = Modifier.size(22.dp)
                        )
                    } else {
                        Icon(
                            imageVector = icon,
                            contentDescription = item.name,
                            tint = iconTint,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = item.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Light,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp
                            )
                        }

                    }
                }

                // If odd number in last row, fill remaining column to keep alignment
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        if (bookmarks.isEmpty()) {
            Text(
                text = "No bookmarks added yet. Tap + to add current folder.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}
