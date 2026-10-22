package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.util.FileUtils
import com.example.util.PdfRendererHelper
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfViewerDialog(
    file: File,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    var totalPages by remember { mutableIntStateOf(0) }
    var currentPage by remember { mutableIntStateOf(0) }
    var currentBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }

    // Load page count
    LaunchedEffect(file) {
        isLoading = true
        loadError = null
        try {
            val count = PdfRendererHelper.getPdfPageCount(file)
            totalPages = count
            if (count > 0) {
                currentPage = 0
                val bmp = PdfRendererHelper.renderPdfPage(file, 0)
                currentBitmap = bmp
            } else {
                loadError = "Unable to open or render this PDF file."
            }
        } catch (e: Exception) {
            loadError = "Failed to load PDF: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    // When currentPage changes
    LaunchedEffect(currentPage, totalPages) {
        if (totalPages > 0) {
            isLoading = true
            currentBitmap = PdfRendererHelper.renderPdfPage(file, currentPage)
            isLoading = false
        }
    }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .testTag("pdf_viewer_dialog"),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = file.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            if (totalPages > 0) {
                                Text(
                                    text = "Page ${currentPage + 1} of $totalPages",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onClose, modifier = Modifier.testTag("pdf_close_button")) {
                            Icon(Icons.Default.Close, contentDescription = "Close PDF")
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { FileUtils.shareFile(context, file) },
                            modifier = Modifier.testTag("pdf_share_button")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Share PDF")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        isLoading -> {
                            CircularProgressIndicator(modifier = Modifier.size(48.dp))
                        }
                        loadError != null -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PictureAsPdf,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(56.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = loadError!!,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        currentBitmap != null -> {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                shadowElevation = 4.dp,
                                modifier = Modifier.clip(RoundedCornerShape(8.dp))
                            ) {
                                Image(
                                    bitmap = currentBitmap!!.asImageBitmap(),
                                    contentDescription = "PDF Page ${currentPage + 1}",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }

                // Bottom navigation controls for pagination
                if (totalPages > 1) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 3.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    if (currentPage > 0) currentPage--
                                },
                                enabled = currentPage > 0,
                                modifier = Modifier.testTag("pdf_prev_page")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Previous Page"
                                )
                            }

                            Text(
                                text = "Page ${currentPage + 1} / $totalPages",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )

                            IconButton(
                                onClick = {
                                    if (currentPage < totalPages - 1) currentPage++
                                },
                                enabled = currentPage < totalPages - 1,
                                modifier = Modifier.testTag("pdf_next_page")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Next Page"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
