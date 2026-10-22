package com.example.ui.components

import com.example.ui.icons.LucideIcons
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun BreathingActionIcon(
    imageVector: ImageVector,
    contentDescription: String,
    enabled: Boolean,
    color: Color
) {
    var breathe by remember { mutableStateOf(false) }

    LaunchedEffect(enabled) {
        if (enabled) {
            while (true) {
                breathe = !breathe
                delay(900)
            }
        } else {
            breathe = false
        }
    }

    val targetColor = if (enabled) {
        color.copy(alpha = if (breathe) 1f else 0.65f)
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
    }

    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(
            durationMillis = 850,
            easing = FastOutSlowInEasing
        ),
        label = "action_icon_breathing"
    )

    Icon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        tint = animatedColor,
        modifier = Modifier.size(22.dp)
    )
}

@Composable
private fun ActionItem(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    strongerDarkDivider: Boolean,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(
            enabled = enabled,
            onClick = onClick,
            modifier = Modifier.size(44.dp)
        ) {
            content()
        }

        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurface.copy(
                alpha = if (enabled) 0.9f else 0.05f
            ),
            fontSize = 9.sp,
            lineHeight = 10.sp,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Clip,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 1.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .width(36.dp)
                .height(1.dp)
                .background(
                    if (strongerDarkDivider) {
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.90f)
                    } else if (MaterialTheme.colorScheme.onSurface.luminance() < 0.5f) {
                        Color.Black
                    } else {
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)
                    }
                )
        )

        Spacer(modifier = Modifier.height(3.dp))
    }
}

@Composable
fun FileActionBar(
    selectionCount: Int,
    isSingleSelectionDirectory: Boolean = false,
    isSelectedZip: Boolean = false,
    onOpenWith: () -> Unit,
    onSelectAll: () -> Unit,
    onZip: () -> Unit,
    onBookmark: () -> Unit,
    onShare: () -> Unit,
    onCopy: () -> Unit,
    onCut: () -> Unit,
    onRename: () -> Unit,
    onDetails: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    strongerDarkDivider: Boolean = false
) {
    val enabled = selectionCount > 0
    val singleSelection = selectionCount == 1

    AnimatedVisibility(
        visible = enabled,
        modifier = modifier,
        enter = slideInHorizontally(initialOffsetX = { it }),
        exit = slideOutHorizontally(targetOffsetX = { it })
    ) {
        Column(
            modifier = Modifier
                .width(66.dp)
                .background(
                    MaterialTheme.colorScheme.surfaceContainerHigh,
                    RoundedCornerShape(18.dp)
                )
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 3.dp, vertical = 5.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            ActionItem("Open\nWith", singleSelection && !isSingleSelectionDirectory, onOpenWith, strongerDarkDivider) {
                BreathingActionIcon(
                    LucideIcons.ExternalLink,
                    "Open With",
                    singleSelection && !isSingleSelectionDirectory,
                    Color(0xFFAB47BC)
                )
            }

            ActionItem("Select\nAll", enabled, onSelectAll, strongerDarkDivider) {
                BreathingActionIcon(
                    LucideIcons.ListChecks, "Select All", enabled, Color(0xFF42A5F5)
                )
            }

            ActionItem(if (isSelectedZip) "Unzip" else "ZIP", enabled, onZip, strongerDarkDivider) {
                BreathingActionIcon(
                    LucideIcons.FolderArchive, if (isSelectedZip) "Unzip" else "ZIP", enabled, Color(0xFFFF9800)
                )
            }

            ActionItem("Bookmark", singleSelection && isSingleSelectionDirectory, onBookmark, strongerDarkDivider) {
                BreathingActionIcon(
                    LucideIcons.BookmarkPlus,
                    "Bookmark",
                    singleSelection && isSingleSelectionDirectory,
                    Color(0xFFFFC107)
                )
            }

            ActionItem("Share", enabled, onShare, strongerDarkDivider) {
                BreathingActionIcon(
                    LucideIcons.Share2, "Share", enabled, Color(0xFF4CAF50)
                )
            }

            ActionItem("Copy", enabled, onCopy, strongerDarkDivider) {
                BreathingActionIcon(
                    LucideIcons.Copy, "Copy", enabled, Color(0xFF26C6DA)
                )
            }

            ActionItem("Cut", enabled, onCut, strongerDarkDivider) {
                BreathingActionIcon(
                    LucideIcons.Scissors, "Cut", enabled, Color(0xFFFFB300)
                )
            }

            ActionItem("Rename", singleSelection, onRename, strongerDarkDivider) {
                BreathingActionIcon(
                    LucideIcons.Edit3, "Rename", singleSelection, Color(0xFFEC407A)
                )
            }

            ActionItem("Details", enabled, onDetails, strongerDarkDivider) {
                BreathingActionIcon(
                    LucideIcons.Info, "Details", enabled, Color(0xFF29B6F6)
                )
            }

            ActionItem("Delete", enabled, onDelete, strongerDarkDivider) {
                Icon(
                    imageVector = LucideIcons.Trash2,
                    contentDescription = "Delete",
                    tint = if (enabled) {
                        Color(0xFFFF0000)
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                    },
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
