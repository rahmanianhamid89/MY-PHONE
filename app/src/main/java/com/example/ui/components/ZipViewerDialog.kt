package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.icons.LucideIcons
import com.example.util.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

private data class ZipViewerItem(
    val name: String,
    val isDirectory: Boolean,
    val size: Long
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZipViewerDialog(
    file: File,
    onClose: () -> Unit
) {
    var items by remember(file) {
        mutableStateOf<List<ZipViewerItem>>(emptyList())
    }
    var isLoading by remember(file) {
        mutableStateOf(true)
    }
    var errorMessage by remember(file) {
        mutableStateOf<String?>(null)
    }

    LaunchedEffect(file) {
        isLoading = true
        errorMessage = null

        try {
            items = withContext(Dispatchers.IO) {
                val result = mutableListOf<ZipViewerItem>()

                ZipInputStream(
                    BufferedInputStream(FileInputStream(file))
                ).use { zis ->
                    var entry: ZipEntry? = zis.nextEntry

                    while (entry != null) {
                        result.add(
                            ZipViewerItem(
                                name = entry.name,
                                isDirectory = entry.isDirectory,
                                size = entry.size.coerceAtLeast(0L)
                            )
                        )

                        zis.closeEntry()
                        entry = zis.nextEntry
                    }
                }

                result
            }
        } catch (e: Exception) {
            errorMessage = e.message ?: "Unable to read ZIP archive"
            items = emptyList()
        } finally {
            isLoading = false
        }
    }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
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
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            if (!isLoading && errorMessage == null) {
                                Text(
                                    text = "${items.size} items • ${FileUtils.formatBytes(file.length())}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onClose) {
                            Icon(
                                LucideIcons.X,
                                contentDescription = "Close"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )

                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    errorMessage != null -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Unable to open archive\n\n$errorMessage",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    items.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Archive is empty",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(
                                items = items,
                                key = { "${it.name}:${it.isDirectory}" }
                            ) { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            horizontal = 16.dp,
                                            vertical = 10.dp
                                        ),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (item.isDirectory) {
                                            LucideIcons.Folder
                                        } else {
                                            LucideIcons.File
                                        },
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )

                                    Spacer(
                                        modifier = Modifier.width(12.dp)
                                    )

                                    Column(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = item.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        if (!item.isDirectory) {
                                            Text(
                                                text = FileUtils.formatBytes(item.size),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
