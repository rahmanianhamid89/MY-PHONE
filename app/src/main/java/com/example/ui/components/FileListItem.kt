package com.example.ui.components

import android.content.Context
import com.example.ui.icons.LucideIcons

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.Alignment
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalContext
import com.example.model.FileCategory
import com.example.model.FileItem
import com.example.util.AlwaysOpenManager
import com.example.util.FileUtils
import com.example.util.PersistentThumbnailCache
import androidx.compose.foundation.layout.height
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import android.graphics.BlurMaskFilter
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb

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
    onFolderDetails: (() -> Unit)? = null,
    onZip: () -> Unit,
    onExtractZip: () -> Unit,
    onOpenWith: () -> Unit,
    onAddBookmark: (() -> Unit)? = null,
    animateLongNames: Boolean = true,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var menuExpanded by remember { mutableStateOf(false) }

    val context = LocalContext.current

    var alwaysAppIcon by remember(fileItem.file) {
        mutableStateOf<android.graphics.drawable.Drawable?>(null)
    }

    DisposableEffect(fileItem.file) {
        fun updateAlwaysIcon() {
            alwaysAppIcon = if (!fileItem.isDirectory) {
                val mimeType = FileUtils.getMimeType(fileItem.file)
                val packageName = AlwaysOpenManager.getPackageName(context, mimeType)

                if (!packageName.isNullOrBlank()) {
                    try {
                        context.packageManager.getApplicationIcon(packageName)
                    } catch (_: Exception) {
                        null
                    }
                } else {
                    null
                }
            } else {
                null
            }
        }

        updateAlwaysIcon()

        val prefs = context.getSharedPreferences(
            "always_open_preferences",
            Context.MODE_PRIVATE
        )

        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
            updateAlwaysIcon()
        }

        prefs.registerOnSharedPreferenceChangeListener(listener)

        onDispose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }
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
                    onLongClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onLongClick()
            }
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
            Box(modifier = Modifier.fillMaxWidth()) {
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
                    if (fileItem.file.extension.equals("apk", ignoreCase = true)) {
                        val packageInfo = remember(fileItem.file.absolutePath, fileItem.file.lastModified()) {
                            context.packageManager.getPackageArchiveInfo(
                                fileItem.file.absolutePath,
                                android.content.pm.PackageManager.GET_META_DATA
                            )
                        }
                        val apkIcon = remember(fileItem.file.absolutePath, fileItem.file.lastModified()) {
                            packageInfo?.applicationInfo?.let { appInfo ->
                                try {
                                    appInfo.sourceDir = fileItem.file.absolutePath
                                    appInfo.publicSourceDir = fileItem.file.absolutePath
                                    appInfo.loadIcon(context.packageManager)
                                } catch (_: Exception) {
                                    null
                                }
                            }
                        }

                        if (apkIcon != null) {
                            AndroidView(
                                factory = { context ->
                                    android.widget.ImageView(context).apply {
                                        scaleType = android.widget.ImageView.ScaleType.CENTER_INSIDE
                                        setImageDrawable(apkIcon)
                                    }
                                },
                                modifier = Modifier.size(40.dp)
                            )
                        } else {
                            Icon(
                                imageVector = fileItem.category.icon,
                                contentDescription = fileItem.category.titleFa,
                                tint = fileItem.category.color,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    } else if (fileItem.category == FileCategory.IMAGE) {
                        val context = LocalContext.current
                        val thumbnail by produceState<java.io.File?>(
                            initialValue = null,
                            key1 = fileItem.file.absolutePath,
                            key2 = fileItem.file.lastModified(),
                            key3 = fileItem.virtualZipEntryPath
                        ) {
                            val imageFile = if (fileItem.virtualZipArchive != null) {
                                com.example.util.VirtualZipUtils.materializeEntry(
                                    context = context,
                                    zipFile = fileItem.virtualZipArchive,
                                    entryPath = fileItem.virtualZipEntryPath.orEmpty()
                                )
                            } else {
                                fileItem.file
                            }

                            value = imageFile?.let {
                                PersistentThumbnailCache.get(context, it)
                            }
                        }

                        AsyncImage(
                            model = thumbnail,
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
                                modifier = Modifier
                                    .offset(y = if (fileItem.isDirectory) 8.dp else 5.dp)
                                    .fillMaxWidth(0.5f)
                                    .then(
                                if (animateLongNames) {
                                    Modifier.basicMarquee(
                                        iterations = 1,
                                        initialDelayMillis = 200,
                                        repeatDelayMillis = 200,
                                        velocity = 90.dp
                                    )
                                } else {
                                    Modifier
                                }
                            ),
                                text = buildAnnotatedString {
                                    if (fileItem.isDirectory) {
                                        withStyle(SpanStyle(color = MaterialTheme.colorScheme.onSurface)) {
                                            append(fileItem.name)
                                        }
                                    } else {
                                        val dotIndex = fileItem.name.lastIndexOf(".")
                                        if (dotIndex > 0 && dotIndex < fileItem.name.length - 1) {
                                            append(fileItem.name.substring(0, dotIndex))
                                            withStyle(SpanStyle(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f))) {
                                                append(fileItem.name.substring(dotIndex))
                                            }
                                        } else {
                                            append(fileItem.name)
                                        }
                                    }
                                },
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (fileItem.isDirectory) FontWeight.Normal else FontWeight.Light,
                                maxLines = if (animateLongNames) 1 else Int.MAX_VALUE,
                                softWrap = !animateLongNames,
                                overflow = if (animateLongNames) TextOverflow.Ellipsis else TextOverflow.Clip
                            )
                            
                            if (fileItem.isHidden) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "(Hidden)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline,
                                    fontSize = 10.sp
                                )
                            }
                        }



                      Row(
                          modifier = Modifier.fillMaxWidth(),
                          verticalAlignment = Alignment.CenterVertically
                      ) {
                          if (fileItem.isDirectory) {
                              Text(
                                  text = if (fileItem.childCount == 0) "Empty" else " ",
                                  modifier = Modifier.offset(y = 9.dp),
                                  style = MaterialTheme.typography.labelSmall,
                                  fontWeight = FontWeight.Light,
                                  color = fileItem.category.color,
                                  fontSize = 10.sp
                              )
                          }

                          alwaysAppIcon?.let { drawable ->
                              val bitmap = if (drawable is android.graphics.drawable.BitmapDrawable) {
                                  drawable.bitmap
                              } else {
                                  android.graphics.Bitmap.createBitmap(
                                      drawable.intrinsicWidth.coerceAtLeast(1),
                                      drawable.intrinsicHeight.coerceAtLeast(1),
                                      android.graphics.Bitmap.Config.ARGB_8888
                                  ).also { bitmap ->
                                      val canvas = android.graphics.Canvas(bitmap)
                                      drawable.setBounds(
                                          0,
                                          0,
                                          canvas.width,
                                          canvas.height
                                      )
                                      drawable.draw(canvas)
                                  }
                              }

                              Image(
                                  painter = BitmapPainter(bitmap.asImageBitmap()),
                                  contentDescription = null,
                                  contentScale = ContentScale.Fit,
                                  modifier = Modifier
                                      .offset(y = 4.dp)
                                      .size(14.dp)
                              )
                          }

                          Spacer(modifier = Modifier.weight(1f))

                          Row(
                              modifier = Modifier.offset(y = 4.dp),
                              verticalAlignment = Alignment.CenterVertically,
                              horizontalArrangement = Arrangement.spacedBy(8.dp)
                          ) {
                              Text(
                                  text = fileItem.formattedDate,
                                  modifier = Modifier.offset(y = 9.dp),
                                  fontWeight = FontWeight.Light,
                                  style = MaterialTheme.typography.bodySmall,
                                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                                  fontSize = 11.sp
                              )
                              Text(
                                  text = "•",
                                  modifier = Modifier.offset(y = 9.dp),
                                  style = MaterialTheme.typography.bodySmall,
                                  color = MaterialTheme.colorScheme.outline
                              )
                          }

                          Spacer(modifier = Modifier.width(8.dp))

                          Text(
                              text = fileItem.formattedSize,
                              modifier = Modifier.offset(y = 13.dp),
                              style = MaterialTheme.typography.bodySmall,
                              fontWeight = FontWeight.Normal,
                              color = MaterialTheme.colorScheme.onSurfaceVariant,
                              fontSize = 11.sp
                          )
                    
                      }
                }

            // Options menu button
            Box {
                  if (false) {
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("file_options_${fileItem.name}")
                ) {
                    Icon(
                        imageVector = LucideIcons.EllipsisVertical,
                        contentDescription = "More Options",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  }
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
                            Icon(LucideIcons.ExternalLink, contentDescription = null)
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
                                Icon(LucideIcons.Edit3, contentDescription = null)
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
                                Icon(LucideIcons.FolderArchive, contentDescription = null)
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
                                Icon(LucideIcons.FolderArchive, contentDescription = null)
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
                                Icon(LucideIcons.BookmarkPlus, contentDescription = null)
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
                            Icon(LucideIcons.Share2, contentDescription = null)
                        },
                        onClick = {
                            menuExpanded = false
                            onShare()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Copy") },
                        leadingIcon = {
                            Icon(LucideIcons.Copy, contentDescription = null)
                        },
                        onClick = {
                            menuExpanded = false
                            onCopy()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Cut") },
                        leadingIcon = {
                            Icon(LucideIcons.Scissors, contentDescription = null)
                        },
                        onClick = {
                            menuExpanded = false
                            onCut()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Rename") },
                        leadingIcon = {
                            Icon(LucideIcons.Edit3, contentDescription = null)
                        },
                        onClick = {
                            menuExpanded = false
                            onRename()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Details & MD5") },
                        leadingIcon = {
                            Icon(LucideIcons.Info, contentDescription = null)
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
                                LucideIcons.Trash2,
                                contentDescription = null,
                                tint = Color(0xFFFF0000)
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
            if (fileItem.isDirectory && onFolderDetails != null) {
                IconButton(
                    onClick = onFolderDetails,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 3.dp, end = 6.dp)
                        .offset(y = 14.dp)
                        .size(24.dp)
                ) {
                    Icon(
                        imageVector = LucideIcons.Info,
                        contentDescription = "Folder details",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            }
    }
        val glowColor = MaterialTheme.colorScheme.primary
        Box(
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp)
                .fillMaxWidth()
                .height(9.dp)
                .drawBehind {
                    val y = size.height / 2f
                    drawIntoCanvas { canvas ->
                        val glowPaint = android.graphics.Paint(
                            android.graphics.Paint.ANTI_ALIAS_FLAG
                        ).apply {
                            color = glowColor.copy(alpha = 0.60f).toArgb()
                            style = android.graphics.Paint.Style.STROKE
                            strokeWidth = 1.dp.toPx()
                            maskFilter = android.graphics.BlurMaskFilter(
                                2.5.dp.toPx(),
                                android.graphics.BlurMaskFilter.Blur.NORMAL
                            )
                        }
                        canvas.nativeCanvas.drawLine(0f, y, size.width, y, glowPaint)
                        glowPaint.maskFilter = null
                        glowPaint.color = glowColor.copy(alpha = 0.10f).toArgb()
                        glowPaint.strokeWidth = 0.5.dp.toPx()
                        canvas.nativeCanvas.drawLine(0f, y, size.width, y, glowPaint)
                    }
                }
        )
    }
}
