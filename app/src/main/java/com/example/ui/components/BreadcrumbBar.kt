package com.example.ui.components
import com.example.ui.icons.LucideIcons

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.withFrameNanos
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File

@Composable
fun BreadcrumbBar(
    breadcrumbs: List<File>,
    virtualZipName: String? = null,
    virtualZipPath: String? = null,
    onVirtualZipNavigate: (String) -> Unit = {},
    onVirtualZipExit: () -> Unit = {},
    onHomeClick: () -> Unit,
    onNavigate: (File) -> Unit,
    pasteVisible: Boolean = false,
    pasteCount: Int = 0,
    onPaste: () -> Unit = {},
    onCancelPaste: () -> Unit = {},
    extractZipVisible: Boolean = false,
    onExtractZip: () -> Unit = {},
    onCancelExtractZip: () -> Unit = {},
    isUsbPath: Boolean = false,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val usbRootIndex = if (isUsbPath && breadcrumbs.isNotEmpty()) 0 else -1
    val displayedBreadcrumbs = breadcrumbs

    // Auto-scroll to end after the new path has been laid out
    LaunchedEffect(breadcrumbs) {
        withFrameNanos { }
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                shape = RoundedCornerShape(12.dp)
            )
            .testTag("breadcrumb_bar"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Home Icon at the very beginning of the address bar; tapping it navigates to Home
        IconButton(
            onClick = onHomeClick,
            modifier = Modifier
                .size(36.dp)
                .testTag("breadcrumb_home_button")
        ) {
            Icon(
                imageVector = LucideIcons.House,
                contentDescription = "Home",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
        }

        // Separator between Home and the path segments
        Icon(
            imageVector = LucideIcons.ChevronRight,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.outline
        )

        // 2. The rest of the address path written horizontally
        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(scrollState),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            displayedBreadcrumbs.forEachIndexed { index, file ->
                val isLast = index == displayedBreadcrumbs.size - 1
                val displayName = when {
                    index == 0 && usbRootIndex == 0 -> "USB OTG"
                    index == 0 -> "Internal Storage"
                    else -> file.name
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onNavigate(file) }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                        .testTag("breadcrumb_item_$index")
                ) {
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isLast && virtualZipName == null) FontWeight.Bold else FontWeight.Normal,
                        color = if (isLast && virtualZipName == null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                }

                Icon(
                    imageVector = LucideIcons.ChevronRight,
                    contentDescription = "/",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
            }

            if (virtualZipName != null) {
                val zipPath = virtualZipPath.orEmpty()
                val zipParts = zipPath
                    .trim('/')
                    .split('/')
                    .filter { it.isNotBlank() }

                val zipRootIsLast = zipParts.isEmpty()

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable {
                            if (zipRootIsLast) {
                                onVirtualZipExit()
                            } else {
                                onVirtualZipNavigate("")
                            }
                        }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                        .testTag("breadcrumb_zip_root")
                ) {
                    Text(
                        text = virtualZipName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (zipRootIsLast) FontWeight.Bold else FontWeight.Normal,
                        color = if (zipRootIsLast) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                }

                zipParts.forEachIndexed { index, part ->
                    Icon(
                        imageVector = LucideIcons.ChevronRight,
                        contentDescription = "/",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )

                    val targetPath = zipParts.take(index + 1).joinToString("/")
                    val isLast = index == zipParts.lastIndex

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onVirtualZipNavigate(targetPath) }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                            .testTag("breadcrumb_zip_item_$index")
                    ) {
                        Text(
                            text = part,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isLast) FontWeight.Bold else FontWeight.Normal,
                            color = if (isLast) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        if (extractZipVisible) {
            var extractBlink by remember { mutableStateOf(false) }

            androidx.compose.runtime.LaunchedEffect(extractZipVisible) {
                while (extractZipVisible) {
                    extractBlink = !extractBlink
                    kotlinx.coroutines.delay(700)
                }
            }

            androidx.compose.material3.Surface(
                modifier = Modifier
                    .height(36.dp)
                    .padding(vertical = 4.dp)
                    .alpha(if (extractBlink) 1f else 0.55f)
                    .clickable { onExtractZip() },
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFFFB000)
            ) {
                Row(
                    modifier = Modifier.padding(start = 9.dp, end = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "EXTRACT ZIP FILE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    IconButton(
                        onClick = onCancelExtractZip,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = LucideIcons.X,
                            contentDescription = "Cancel extract",
                            modifier = Modifier.size(14.dp),
                            tint = Color.Black
                        )
                    }
                }
            }
        } else if (pasteVisible) {
            var pasteBlink by remember { mutableStateOf(false) }

            androidx.compose.runtime.LaunchedEffect(pasteVisible) {
                while (pasteVisible) {
                    pasteBlink = !pasteBlink
                    kotlinx.coroutines.delay(700)
                }
            }

            androidx.compose.material3.Surface(
                modifier = Modifier
                    .height(36.dp)
                    .padding(vertical = 4.dp)
                    .alpha(if (pasteBlink) 1f else 0.55f)
                    .clickable { onPaste() },
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFFFB000)
            ) {
                Row(
                    modifier = Modifier.padding(start = 9.dp, end = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PASTE HERE $pasteCount",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    IconButton(
                        onClick = onCancelPaste,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = LucideIcons.X,
                            contentDescription = "Cancel paste",
                            modifier = Modifier.size(14.dp),
                            tint = Color.Black
                        )
                    }
                }
            }
        }
    }
}
