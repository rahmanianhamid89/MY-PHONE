package com.example.ui.components

import androidx.compose.ui.graphics.luminance
import com.example.ui.icons.LucideIcons

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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
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
    onDismiss: () -> Unit,
    isCustomTheme: Boolean = false,
    isLightTheme: Boolean = false
) {
    if (!state.isVisible) return


    val animatedProgress by animateFloatAsState(
        targetValue = state.progressFraction,
        animationSpec = tween(durationMillis = 250),
        label = "transfer_progress"
    )

    val graphColor = if (isCustomTheme) {
        Color(0xFFA30068)
    } else {
        when {
            state.isCancelled || state.errorMessage != null -> MaterialTheme.colorScheme.error
            state.isPaused -> MaterialTheme.colorScheme.tertiary
            state.isFinished -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.primary
        }
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
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.94f)
                .shadow(elevation = 20.dp, shape = RoundedCornerShape(14.dp))
                .clip(RoundedCornerShape(14.dp))
                .border(
                    width = 1.dp,
                    color = if (isCustomTheme) MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(14.dp)
                )
                .testTag("windows11_transfer_dialog"),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = if (isCustomTheme) 0.dp else 6.dp
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
                                imageVector = if (state.isCut) LucideIcons.Scissors else LucideIcons.Copy,
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
                                    state.isFinished -> if (state.isCut) "Transfer completed successfully" else "Copy completed successfully"
                                    state.isCancelled -> "Transfer cancelled"
                                    state.isPaused -> "Transfer Paused"
                                    state.isCut -> "Moving ${state.totalFiles} items..."
                                    else -> "Copying ${state.totalFiles} items..."
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
                                    imageVector = if (state.isPaused) LucideIcons.Play else LucideIcons.Pause,
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
                                imageVector = LucideIcons.X,
                                contentDescription = "Close",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                HorizontalDivider(
                    color = if (isCustomTheme) MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.primary,
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
                                state.isFinished -> "100% Completed"
                                state.isCancelled -> "Cancelled"
                                state.isPaused -> "${state.percentage}% (Paused)"
                                else -> "${state.percentage}% Completed"
                            },
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isCustomTheme) Color.White else graphColor
                        )

                        Text(
                            text = state.formattedTimeRemaining,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))


                    Spacer(modifier = Modifier.height(14.dp))

                    // Real-Time Speed and Windows 11 Speed Chart
                    Windows11SpeedGraphCard(
                        speedBytesPerSec = state.speedBytesPerSec,
                        peakSpeedBytesPerSec = state.peakSpeedBytesPerSec,
                        formattedCurrentSpeed = state.formattedCurrentSpeed,
                        formattedPeakSpeed = state.formattedPeakSpeed,
                        speedHistory = state.speedHistory,
                        progress = state.percentage / 100f,
                        graphColor = graphColor,
                        isCustomTheme = isCustomTheme,
                        isLightTheme = isLightTheme,
                        isPaused = state.isPaused,
                        isFinished = state.isFinished
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Transfer details — always visible.
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        HorizontalDivider(
                            color = if (isCustomTheme) MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.primary,
                            thickness = 1.dp
                        )

                        DetailRow(
                            label = "Name",
                            labelEn = "File name",
                            value = if (state.currentFileName.isNotBlank()) {
                                state.currentFileName
                            } else if (state.isFinished) {
                                "All files transferred"
                            } else {
                                "-"
                            },
                            icon = LucideIcons.File,
                            isBold = true
                        )

                        DetailRow(
                            label = "Transfer files",
                            labelEn = "Transfer files",
                            value = "${state.completedFiles.coerceAtLeast(1)}/${state.totalFiles}",
                            icon = LucideIcons.File
                        )

                        DetailRow(
                            label = "Transfer size",
                            labelEn = "Transfer size",
                            value = "${state.formattedTransferredBytes} / ${state.formattedTotalBytes}",
                            icon = LucideIcons.HardDrive
                        )
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
                                    containerColor = if (state.isCancelled) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.testTag("transfer_done_button")
                            ) {
                                Icon(
                                    imageVector = if (state.isCancelled) LucideIcons.X else LucideIcons.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (state.isCancelled) "Close" else "Finish (Close)")
                            }
                        } else {
                            OutlinedButton(
                                onClick = onCancel,
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isCustomTheme) MaterialTheme.colorScheme.outline
                                    else MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.testTag("transfer_cancel_button")
                            ) {
                                Text("Cancel")
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = onPauseToggle,
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isCustomTheme) {
                                Color(0xFF3C0020)
                            } else if (state.isPaused) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                                    contentColor = if (state.isPaused) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                border = if (isCustomTheme) null else androidx.compose.foundation.BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.testTag("transfer_pause_resume_button")
                            ) {
                                Icon(
                                    imageVector = if (state.isPaused) LucideIcons.Play else LucideIcons.Pause,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (state.isPaused) "Resume" else "Pause")
                            }
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
    progress: Float,
    graphColor: Color,
    isCustomTheme: Boolean,
    isLightTheme: Boolean,
    isPaused: Boolean,
    isFinished: Boolean
) {
    val useBlackSpeedOverlay = !isCustomTheme && (
        isLightTheme ||
        MaterialTheme.colorScheme.surface.luminance() > 0.5f
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("speed_graph_card"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        border = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Speed labels are displayed inside the graph.

            Spacer(modifier = Modifier.height(10.dp))

            // Canvas Speed Chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(95.dp)
                    .clip(RoundedCornerShape(6.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(95.dp)
                        .padding(horizontal = 4.dp, vertical = 6.dp)
                        .background(if (isCustomTheme) Color(0xFF1E000E) else MaterialTheme.colorScheme.surface)
                )

                val chartGridColor = MaterialTheme.colorScheme.primary
                val chartProgressColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(95.dp)
                        .padding(horizontal = 4.dp, vertical = 6.dp)
                        .testTag("speed_canvas_chart")
                ) {
                    val width = size.width
                    val height = size.height

                      // Chart grid
                      val gridColor = chartGridColor.copy(alpha = 0.5f)
                      val horizontalLines = 4
                      val verticalLines = 8

                      for (i in 1 until horizontalLines) {
                          val y = height * i / horizontalLines
                          drawLine(
                              color = gridColor,
                              start = Offset(0f, y),
                              end = Offset(width, y),
                              strokeWidth = 1f
                          )
                      }

                      for (i in 1 until verticalLines) {
                          val x = width * i / verticalLines
                          drawLine(
                              color = gridColor,
                              start = Offset(x, 0f),
                              end = Offset(x, height),
                              strokeWidth = 1f
                          )
                      }

                      // Theme-aware outline; preserve white in Custom theme
                    drawRect(
                        color = if (isCustomTheme) Color.White else chartGridColor,
                        topLeft = Offset(0f, 0f),
                        size = androidx.compose.ui.geometry.Size(width, height),
                        style = Stroke(width = 1.5f)
                    )

                    // Windows 11 style transfer graph
                      // The chart stays full width; the green filled area follows copy progress.
                      val progressWidth = width * progress.coerceIn(0f, 1f)
                      if (progressWidth > 0f) {
                          drawRect(
                              color = chartProgressColor,
                              topLeft = Offset(0f, 0f),
                              size = androidx.compose.ui.geometry.Size(progressWidth, height)
                          )
                      }

                      // Very subtle baseline.
                      drawLine(
                          color = graphColor.copy(alpha = 0.18f),
                          start = Offset(0f, height - 1f),
                          end = Offset(width, height - 1f),
                          strokeWidth = 1f
                      )

                      val history = if (speedHistory.isEmpty()) listOf(0f) else speedHistory
                      val maxVal = (history.maxOrNull() ?: 1f).coerceAtLeast(0.5f)
                      val pointsCount = history.size

                      // Keep the speed line and curve inside the third chart row from the bottom.
                      val speedRangeTop = height / 4f + 2.dp.toPx()
                      val speedRangeBottom = height / 2f - 2.dp.toPx()

                      // Full-width live speed line.
                      // Its entire horizontal line moves vertically with the current speed.
                      val currentSpeed = history.lastOrNull() ?: 0f
                      val currentNorm = (currentSpeed / maxVal).coerceIn(0f, 1f)
                      val currentY = speedRangeBottom - currentNorm * (speedRangeBottom - speedRangeTop)

                      drawLine(
                          color = if (useBlackSpeedOverlay) Color.Black else Color.White,
                          start = Offset(0f, currentY),
                          end = Offset(width, currentY),
                          strokeWidth = 2.2f,
                          cap = StrokeCap.Round
                      )

                      // Current speed label moves with the horizontal speed line.
                      drawContext.canvas.nativeCanvas.apply {
                          val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                              color = (if (useBlackSpeedOverlay) Color.Black else Color.White).toArgb()
                              textSize = 11.sp.toPx()
                              typeface = android.graphics.Typeface.DEFAULT_BOLD
                          }
                          val label = if (isPaused) "Speed : 0 KB/s" else "Speed : $formattedCurrentSpeed"
                          val labelWidth = paint.measureText(label)
                          drawText(
                              label,
                              width - labelWidth - 6.dp.toPx(),
                              currentY - 5.dp.toPx(),
                              paint
                          )
                      }

                      // Speed curve follows the completed portion of the transfer.
                      val points = history.mapIndexed { index, value ->
                          val x = if (pointsCount <= 1) {
                              progressWidth
                          } else {
                              (index.toFloat() / (pointsCount - 1)) * progressWidth
                          }

                          val norm = (value / maxVal).coerceIn(0f, 1f)
                          val y = speedRangeBottom - norm * (speedRangeBottom - speedRangeTop)

                          Offset(x, y)
                      }

                      if (points.isNotEmpty()) {
                          val strokePath = Path()
                          val fillPath = Path()

                          strokePath.moveTo(
                              points.first().x,
                              points.first().y
                          )

                          fillPath.moveTo(0f, height)
                          fillPath.lineTo(
                              points.first().x,
                              points.first().y
                          )

                          for (i in 0 until points.size - 1) {
                              val p0 = points[i]
                              val p1 = points[i + 1]
                              val midX = (p0.x + p1.x) / 2f

                              strokePath.cubicTo(
                                  midX, p0.y,
                                  midX, p1.y,
                                  p1.x, p1.y
                              )

                              fillPath.cubicTo(
                                  midX, p0.y,
                                  midX, p1.y,
                                  p1.x, p1.y
                              )
                          }

                          fillPath.lineTo(progressWidth, height)
                          fillPath.close()

                          // Soft filled area beneath the live speed curve.
                          drawPath(
                              path = fillPath,
                              color = graphColor
                          )

                      
                          // SPEED_OVERLAY_TOP_LAYER
                          drawLine(
                              color = if (useBlackSpeedOverlay) Color.Black else Color.White,
                              start = Offset(0f, currentY),
                              end = Offset(width, currentY),
                              strokeWidth = 2.2f,
                              cap = StrokeCap.Round
                          )

                          drawContext.canvas.nativeCanvas.apply {
                              val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                                  color = (if (useBlackSpeedOverlay) Color.Black else Color.White).toArgb()
                                  textSize = 11.sp.toPx()
                                  typeface = android.graphics.Typeface.DEFAULT_BOLD
                              }
                              val label = if (isPaused) "Speed : 0 KB/s" else "Speed : $formattedCurrentSpeed"
                              val labelWidth = paint.measureText(label)
                              drawText(
                                  label,
                                  width - labelWidth - 6.dp.toPx(),
                                  currentY - 5.dp.toPx(),
                                  paint
                              )
                          }
}
                }

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
            text = "$label:",
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
