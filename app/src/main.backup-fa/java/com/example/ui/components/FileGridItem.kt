package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.FileCategory
import com.example.model.FileItem
import com.example.util.FileUtils

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileGridItem(
    fileItem: FileItem,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onSelectToggle: () -> Unit,
    onOptionsClick: (() -> Unit)? = null,
    onCopy: (() -> Unit)? = null,
    onCut: (() -> Unit)? = null,
    onRename: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    onShare: (() -> Unit)? = null,
    onDetails: (() -> Unit)? = null,
    onZip: (() -> Unit)? = null,
    onExtractZip: (() -> Unit)? = null,
    onOpenWith: (() -> Unit)? = null,
    onAddBookmark: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val itemAlpha = if (fileItem.isHidden) 0.40f else 1.0f

    Card(
        modifier = modifier
            .padding(4.dp)
            .alpha(itemAlpha)
            .clip(RoundedCornerShape(10.dp))
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
            .testTag("file_grid_item_${fileItem.name}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            } else {
                Color.Transparent
            }
        ),
        border = BorderStroke(
            0.6.dp,
            if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icon / Thumbnail area (clean, no tinted box background)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.15f),
                contentAlignment = Alignment.Center
            ) {
                if (fileItem.category == FileCategory.IMAGE) {
                    AsyncImage(
                        model = fileItem.file,
                        contentDescription = fileItem.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(8.dp))
                    )
                } else {
                    Icon(
                        imageVector = fileItem.category.icon,
                        contentDescription = fileItem.category.titleFa,
                        tint = fileItem.category.color,
                        modifier = Modifier.size(44.dp)
                    )
                }

                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "انتخاب شده",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // File / Folder Name
            Text(
                text = fileItem.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth()
            )

            if (fileItem.isHidden) {
                Text(
                    text = "(مخفی)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    fontSize = 9.sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Size and More Options
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = fileItem.formattedSize,
                    style = MaterialTheme.typography.bodySmall,
                    color = fileItem.category.color,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal
                )

                Box {
                    IconButton(
                        onClick = {
                            if (onDetails != null || onCopy != null || onRename != null) {
                                menuExpanded = true
                            } else {
                                onOptionsClick?.invoke()
                            }
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "گزینه‌ها",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        if (onOpenWith != null) {
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
                        }

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

                        if (onRename != null) {
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
                        }

                        if (onCopy != null) {
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
                        }

                        if (onCut != null) {
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
                        }

                        if (onShare != null) {
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
                        }

                        if (fileItem.extension.lowercase() == "zip" && onExtractZip != null) {
                            DropdownMenuItem(
                                text = { Text("Extract (Unzip)") },
                                leadingIcon = {
                                    Icon(Icons.Default.FolderZip, contentDescription = null)
                                },
                                onClick = {
                                    menuExpanded = false
                                    onExtractZip()
                                }
                            )
                        } else if (onZip != null) {
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

                        if (onDetails != null) {
                            DropdownMenuItem(
                                text = { Text("Details") },
                                leadingIcon = {
                                    Icon(Icons.Default.Info, contentDescription = null)
                                },
                                onClick = {
                                    menuExpanded = false
                                    onDetails()
                                }
                            )
                        }

                        if (onDelete != null) {
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
        }
    }
}
