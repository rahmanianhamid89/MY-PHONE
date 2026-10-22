package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.TransferProgressState

// Windows 11 Signature Colors
private val Win11Green = Color(0xFF0F7B0F)
private val Win11GreenLight = Color(0xFF107C41)
private val Win11GreenSoft = Color(0xFFE8F5E9)
private val Win11PausedAmber = Color(0xFFD97706)
private val Win11SurfaceDark = Color(0xFF202020)
private val Win11SurfaceLight = Color(0xFFF9F9F9)
private val Win11BorderLight = Color(0xFFE5E7EB)

@Composable
fun Windows11TransferDialog(
    state: TransferProgressState,
    onPauseToggle: () -> Unit,
    onCancel: () -> Unit,
    onDismiss: () -> Unit
) {
    if (!state.isVisible) return

    var showMoreDetails by remember { mutableStateOf(true) }

    val animatedProgress by animateFloatAsState(
        targetValue = state.progressFraction,
        animationSpec = tween(durationMillis = 250),
        label = "transfer_progress"
    )

    val graphColor = when {
        state.isCancelled || state.errorMessage != null -> MaterialTheme.colorScheme.error
        state.isPaused -> Win11PausedAmber
        state.isFinished -> Win11Green
        else -> Win11GreenLight
    }

    Dialog(
        onDismissRequest = {
            if (state.isFinished || state.isCancelled) onDismiss()
        },
        properties = DialogProperties(
            dismissOnBackPress = state.isFinished || state.isCancelled,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .shadow(elevation = 20.dp, shape = RoundedCornerShape(14.dp))
                .clip(RoundedCornerShape(14.dp))
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(14.dp)
                )
                .testTag("windows11_transfer_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
            ) {
                // Windows 11 Window Titlebar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(
                                    if (state.isCut) MaterialTheme.colorScheme.tertiaryContainer
                                    else MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(6.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (state.isCut) Icons.Default.ContentCut else Icons.Default.ContentCopy,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (state.isCut) MaterialTheme.colorScheme.onTertiaryContainer
                                else MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = when {
                                    state.isFinished -> if (state.isCut) "انتقال با موفقیت پایان یافت" else "کپی با موفقیت پایان یافت"
                                    state.isCancelled -> "انتقال لغو شد"
                                    state.isPaused -> "انتقال متوقف شده (Paused)"
                                    state.isCut -> "در حال جابجایی ${state.totalFiles} آیتم..."
                                    else -> "در حال کپی ${state.totalFiles} آیتم..."
                                },
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (!state.isFinished && !state.isCancelled) {
                                Text(
                                    text = if (state.isCut) "Moving items" else "Copying items",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Window control buttons
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (!state.isFinished && !state.isCancelled) {
                            IconButton(
                                onClick = onPauseToggle,
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("dialog_pause_button")
                            ) {
                                Icon(
                                    imageVector = if (state.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                    contentDescription = if (state.isPaused) "Resume" else "Pause",
                                    tint = if (state.isPaused) Win11PausedAmber else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        IconButton(
                            onClick = {
                                if (state.isFinished || state.isCancelled) {
                                    onDismiss()
                                } else {
                                    onCancel()
                                }
                            },
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("dialog_close_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                    thickness = 1.dp
                )

                // Dialog Main Body
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Percentage and Status Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = when {
                                state.isFinished -> "100% تکمیل شد"
                                state.isCancelled -> "لغو شد"
                                state.isPaused -> "${state.percentage}% (متوقف شده)"
                                else -> "${state.percentage}% تکمیل شده"
                            },
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = graphColor
                        )

                        Text(
                            text = state.formattedTimeRemaining,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Windows 11 Progress Bar
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .testTag("transfer_progress_bar"),
                        color = graphColor,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Real-Time Speed and Windows 11 Speed Chart
                    Windows11SpeedGraphCard(
                        speedBytesPerSec = state.speedBytesPerSec,
                        peakSpeedBytesPerSec = state.peakSpeedBytesPerSec,
                        formattedCurrentSpeed = state.formattedCurrentSpeed,
                        formattedPeakSpeed = state.formattedPeakSpeed,
                        speedHistory = state.speedHistory,
                        graphColor = graphColor,
                        isPaused = state.isPaused,
                        isFinished = state.isFinished
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // More Details Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { showMoreDetails = !showMoreDetails }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (showMoreDetails) "جزئیات کمتر (Fewer details)" else "جزئیات بیشتر (More details)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Icon(
                            imageVector = if (showMoreDetails) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Windows 11 Detailed Metadata Section
                    AnimatedVisibility(visible = showMoreDetails) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                                thickness = 0.8.dp
                            )

                            // نام فایل (Current File Name)
                            DetailRow(
                                label = "نام فایل:",
                                labelEn = "File name",
                                value = if (state.currentFileName.isNotBlank()) state.currentFileName else if (state.isFinished) "همه فایل‌ها انتقال یافتند" else "-",
                                icon = Icons.Default.InsertDriveFile,
                                isBold = true
                            )

                            // آدرس پیست (Destination / Paste Address)
                            DetailRow(
                                label = "آدرس پیست:",
                                labelEn = "To",
                                value = state.destinationPath.ifBlank { "-" },
                                icon = Icons.Default.Folder,
                                isMonospace = true
                            )

                            if (state.sourcePath.isNotBlank()) {
                                DetailRow(
                                    label = "آدرس مبدأ:",
                                    labelEn = "From",
                                    value = state.sourcePath,
                                    icon = Icons.Default.Folder,
                                    isMonospace = true
                                )
                            }

                            // تعداد فایل (Total files) & فایل باقیمانده (Remaining files)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(modifier = Modifier.weight(1f)) {
                                    DetailMiniCard(
                                        titleFa = "تعداد فایل",
                                        titleEn = "Total files",
                                        value = "${state.totalFiles} فایل"
                                    )
                                }
                                Box(modifier = Modifier.weight(1f)) {
                                    DetailMiniCard(
                                        titleFa = "فایل باقیمانده",
                                        titleEn = "Remaining files",
                                        value = "${state.remainingFiles} فایل",
                                        accentColor = if (state.remainingFiles == 0) Win11Green else null
                                    )
                                }
                            }

                            // حجم باقیمانده (Remaining Size) & حجم انتقال یافته
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(modifier = Modifier.weight(1f)) {
                                    DetailMiniCard(
                                        titleFa = "حجم باقیمانده",
                                        titleEn = "Remaining size",
                                        value = state.formattedRemainingBytes,
                                        subtitle = "از کل ${state.formattedTotalBytes}"
                                    )
                                }
                                Box(modifier = Modifier.weight(1f)) {
                                    DetailMiniCard(
                                        titleFa = "انتقال یافته",
                                        titleEn = "Transferred",
                                        value = state.formattedTransferredBytes,
                                        accentColor = Win11Green
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action Buttons at Bottom
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (state.isFinished || state.isCancelled) {
                            Button(
                                onClick = onDismiss,
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (state.isCancelled) MaterialTheme.colorScheme.error else Win11Green
                                ),
                                modifier = Modifier.testTag("transfer_done_button")
                            ) {
                                Icon(
                                    imageVector = if (state.isCancelled) Icons.Default.Close else Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (state.isCancelled) "بستن" else "پایان (بستن)")
                            }
                        } else {
                            OutlinedButton(
                                onClick = onCancel,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("transfer_cancel_button")
                            ) {
                                Text("انصراف (Cancel)")
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = onPauseToggle,
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (state.isPaused) Win11Green else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (state.isPaused) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.testTag("transfer_pause_resume_button")
                            ) {
                                Icon(
                                    imageVector = if (state.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (state.isPaused) "ادامه (Resume)" else "توقف (Pause)")
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Windows 11 Transfer Speed Chart
 * Renders smooth Bezier curve line, shaded gradient area, grid lines, and speed indicators.
 */
@Composable
private fun Windows11SpeedGraphCard(
    speedBytesPerSec: Long,
    peakSpeedBytesPerSec: Long,
    formattedCurrentSpeed: String,
    formattedPeakSpeed: String,
    speedHistory: List<Float>,
    graphColor: Color,
    isPaused: Boolean,
    isFinished: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("speed_graph_card"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Speed header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = graphColor
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "سرعت انتقال:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isPaused) "متوقف شده (0 KB/s)" else formattedCurrentSpeed,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = graphColor
                    )
                }

                Text(
                    text = "حداکثر: $formattedPeakSpeed",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Canvas Speed Chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(95.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(95.dp)
                        .padding(horizontal = 4.dp, vertical = 6.dp)
                        .testTag("speed_canvas_chart")
                ) {
                    val width = size.width
                    val height = size.height

                    // Draw subtle dashed grid lines (0%, 50%, 100%)
                    val gridColor = Color.Gray.copy(alpha = 0.18f)
                    val dashPathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)

                    // 50% line
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, height * 0.5f),
                        end = Offset(width, height * 0.5f),
                        strokeWidth = 1f,
                        pathEffect = dashPathEffect
                    )
                    // Top line
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, 2f),
                        end = Offset(width, 2f),
                        strokeWidth = 1f,
                        pathEffect = dashPathEffect
                    )
                    // Bottom baseline
                    drawLine(
                        color = gridColor.copy(alpha = 0.35f),
                        start = Offset(0f, height - 1f),
                        end = Offset(width, height - 1f),
                        strokeWidth = 1.5f
                    )

                    val history = if (speedHistory.isEmpty()) listOf(0f) else speedHistory
                    val maxVal = (history.maxOrNull() ?: 1f).coerceAtLeast(0.5f)
                    val pointsCount = history.size

                    // Compute points
                    val points = history.mapIndexed { index, value ->
                        val x = if (pointsCount <= 1) width else (index.toFloat() / (pointsCount - 1)) * width
                        val norm = (value / maxVal).coerceIn(0f, 1f)
                        val y = height - (norm * (height - 8f)) - 4f
                        Offset(x, y)
                    }

                    if (points.isNotEmpty()) {
                        // Smooth Bezier path
                        val strokePath = Path()
                        val fillPath = Path()

                        strokePath.moveTo(points.first().x, points.first().y)
                        fillPath.moveTo(0f, height)
                        fillPath.lineTo(points.first().x, points.first().y)

                        for (i in 0 until points.size - 1) {
                            val p0 = points[i]
                            val p1 = points[i + 1]
                            val midX = (p0.x + p1.x) / 2f
                            strokePath.cubicTo(midX, p0.y, midX, p1.y, p1.x, p1.y)
                            fillPath.cubicTo(midX, p0.y, midX, p1.y, p1.x, p1.y)
                        }

                        fillPath.lineTo(points.last().x, height)
                        fillPath.close()

                        // Gradient fill underneath curve
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    graphColor.copy(alpha = 0.35f),
                                    graphColor.copy(alpha = 0.03f)
                                ),
                                startY = 0f,
                                endY = height
                            )
                        )

                        // Main curve line
                        drawPath(
                            path = strokePath,
                            color = graphColor,
                            style = Stroke(
                                width = 2.5f,
                                cap = StrokeCap.Round
                            )
                        )

                        // Glowing marker on the current point
                        val lastPoint = points.last()
                        drawCircle(
                            color = graphColor.copy(alpha = 0.25f),
                            radius = 7f,
                            center = lastPoint
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 3.5f,
                            center = lastPoint
                        )
                        drawCircle(
                            color = graphColor,
                            radius = 2.2f,
                            center = lastPoint
                        )
                    }
                }

                // Top right and bottom right axis labels
                Text(
                    text = formattedPeakSpeed,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 3.dp, end = 6.dp)
                )

                Text(
                    text = "0 KB/s",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 3.dp, end = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    labelEn: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isBold: Boolean = false,
    isMonospace: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(15.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "$label ($labelEn):",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isBold) FontWeight.SemiBold else FontWeight.Normal,
            fontFamily = if (isMonospace) FontFamily.Monospace else FontFamily.Default,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun DetailMiniCard(
    titleFa: String,
    titleEn: String,
    value: String,
    subtitle: String? = null,
    accentColor: Color? = null
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(
                text = "$titleFa ($titleEn)",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = accentColor ?: MaterialTheme.colorScheme.onSurface
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        }
    }
}
