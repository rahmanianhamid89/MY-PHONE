package com.example.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.ContentCut
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.FileCategory
import com.example.model.FileItem
import com.example.util.FileUtils

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileListItem(
    fileItem: FileItem,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onSelectToggle: () -> Unit,
    onCopy: () -> Unit,
    onCut: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit,
    onDetails: () -> Unit,
    onZip: () -> Unit,
    onExtractZip: () -> Unit,
    onOpenWith: () -> Unit,
    onAddBookmark: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val itemAlpha = if (fileItem.isHidden) 0.40f else 1.0f

    Column(
        modifier = modifier
            .fillMaxWidth()
            .alpha(itemAlpha)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = {
                        if (isSelectionMode) {
                            onSelectToggle()
                        } else {
                            onClick()
                        }
                    },
                    onLongClick = onLongClick
                )
                .testTag("file_item_${fileItem.name}"),
            shape = RoundedCornerShape(0.dp),
            color = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            } else {
                Color.Transparent
            },
            tonalElevation = 0.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isSelectionMode) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onSelectToggle() },
                        modifier = Modifier.testTag("checkbox_${fileItem.name}")
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }

                // Thumbnail or Icon (clean, no tinted box background)
                Box(
                    modifier = Modifier.size(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (fileItem.category == FileCategory.IMAGE) {
                        AsyncImage(
                            model = fileItem.file,
                            contentDescription = fileItem.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(6.dp))
                        )
                    } else {
                        Icon(
                            imageVector = fileItem.category.icon,
                            contentDescription = fileItem.category.titleFa,
                            tint = fileItem.category.color,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = fileItem.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (fileItem.isHidden) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "(مخفی)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline,
                                fontSize = 10.sp
                            )
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = fileItem.formattedDate,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = fileItem.formattedSize,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Normal,
                            color = fileItem.category.color,
                            fontSize = 11.sp
                        )
                    }
                }

            // Options menu button
            Box {
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("file_options_${fileItem.name}")
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More Options",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(if (FileUtils.isApkFile(fileItem.file)) "Install App (APK)" else "Open With...")
                        },
                        leadingIcon = {
                            Icon(Icons.Default.OpenInNew, contentDescription = null)
                        },
                        onClick = {
                            menuExpanded = false
                            onOpenWith()
                        }
                    )

                    if (FileUtils.isTextFile(fileItem.file)) {
                        DropdownMenuItem(
                            text = { Text("Edit Text") },
                            leadingIcon = {
                                Icon(Icons.Outlined.Edit, contentDescription = null)
                            },
                            onClick = {
                                menuExpanded = false
                                onClick()
                            }
                        )
                    }

                    if (FileUtils.isZipFile(fileItem.file)) {
                        DropdownMenuItem(
                            text = { Text("Extract ZIP") },
                            leadingIcon = {
                                Icon(Icons.Default.FolderZip, contentDescription = null)
                            },
                            onClick = {
                                menuExpanded = false
                                onExtractZip()
                            }
                        )
                    } else {
                        DropdownMenuItem(
                            text = { Text("Compress to ZIP") },
                            leadingIcon = {
                                Icon(Icons.Default.FolderZip, contentDescription = null)
                            },
                            onClick = {
                                menuExpanded = false
                                onZip()
                            }
                        )
                    }

                    HorizontalDivider()

                    if (fileItem.isDirectory && onAddBookmark != null) {
                        DropdownMenuItem(
                            text = { Text("Add to Bookmarks") },
                            leadingIcon = {
                                Icon(Icons.Default.BookmarkAdd, contentDescription = null)
                            },
                            onClick = {
                                menuExpanded = false
                                onAddBookmark()
                            }
                        )
                    }

                    DropdownMenuItem(
                        text = { Text("Share") },
                        leadingIcon = {
                            Icon(Icons.Default.Share, contentDescription = null)
                        },
                        onClick = {
                            menuExpanded = false
                            onShare()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Copy") },
                        leadingIcon = {
                            Icon(Icons.Outlined.ContentCopy, contentDescription = null)
                        },
                        onClick = {
                            menuExpanded = false
                            onCopy()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Cut") },
                        leadingIcon = {
                            Icon(Icons.Outlined.ContentCut, contentDescription = null)
                        },
                        onClick = {
                            menuExpanded = false
                            onCut()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Rename") },
                        leadingIcon = {
                            Icon(Icons.Default.DriveFileRenameOutline, contentDescription = null)
                        },
                        onClick = {
                            menuExpanded = false
                            onRename()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Details & MD5") },
                        leadingIcon = {
                            Icon(Icons.Default.Info, contentDescription = null)
                        },
                        onClick = {
                            menuExpanded = false
                            onDetails()
                        }
                    )

                    HorizontalDivider()

                    DropdownMenuItem(
                        text = {
                            Text("Delete", color = MaterialTheme.colorScheme.error)
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
        HorizontalDivider(
            modifier = Modifier.padding(start = 70.dp, end = 16.dp),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.38f),
            thickness = 0.7.dp
        )
    }
}
