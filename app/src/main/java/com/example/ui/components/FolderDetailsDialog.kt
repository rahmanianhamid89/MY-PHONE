package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.SystemClock
import android.provider.DocumentsContract
import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.documentfile.provider.DocumentFile
import com.example.model.FileItem
import com.example.ui.icons.LucideIcons
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.LinkedHashSet
import java.util.Locale

private data class FolderTreeStats(
    val fileCount: Long,
    val folderCount: Long,
    val totalBytes: Long,
    val unreadableEntries: Long,
    val lastModified: Long?,
    val createdAt: Long?,
    val lastAccessedAt: Long?,
    val mimeType: String?,
    val canRead: Boolean,
    val canWrite: Boolean,
    val elapsedMillis: Long
)

private class FolderTreeAccumulator {
    var fileCount = 0L
    var folderCount = 0L
    var totalBytes = 0L
    var unreadableEntries = 0L

    fun addFile(size: Long) {
        fileCount++
        val safeSize = size.coerceAtLeast(0L)
        totalBytes = if (Long.MAX_VALUE - totalBytes < safeSize) {
            Long.MAX_VALUE
        } else {
            totalBytes + safeSize
        }
    }

    fun addUnreadable() {
        if (unreadableEntries < Long.MAX_VALUE) unreadableEntries++
    }
}

@Composable
fun FolderDetailsDialog(
    fileItem: FileItem,
    onDismiss: () -> Unit,
    isCustomTheme: Boolean = false,
    isLightTheme: Boolean = false
) {
    val context = LocalContext.current
    var stats by remember(fileItem.path) {
        mutableStateOf<FolderTreeStats?>(null)
    }
    var isScanning by remember(fileItem.path) { mutableStateOf(true) }
    var scanError by remember(fileItem.path) { mutableStateOf<String?>(null) }

    val pathText = remember(fileItem.path, fileItem.file.absolutePath) {
        folderPathForDisplay(fileItem)
    }

    LaunchedEffect(fileItem.path) {
        isScanning = true
        scanError = null
        stats = null
        try {
            stats = scanFolderTree(context, fileItem)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            scanError = error.message ?: "The folder could not be scanned."
        } finally {
            isScanning = false
        }
    }

    val shape = RoundedCornerShape(24.dp)
    val foreground = if (isCustomTheme) Color.White
        else MaterialTheme.colorScheme.onSurface
    val surfaceColor = if (isCustomTheme) {
        Color(0xFF3C0020).copy(alpha = 0.8f)
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh
    }
    val borderColor = if (isCustomTheme) {
        Color.White.copy(alpha = 0.5f)
    } else {
        MaterialTheme.colorScheme.primary
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .heightIn(max = 700.dp)
                .border(0.5.dp, borderColor, shape),
            shape = shape,
            color = surfaceColor,
            contentColor = foreground,
            tonalElevation = if (isLightTheme) 0.dp else 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 700.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = LucideIcons.Folder,
                        contentDescription = null,
                        tint = if (isCustomTheme) Color.White
                            else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = "Folder Details",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = fileItem.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = foreground.copy(alpha = 0.75f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Full Path",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = pathText,
                        modifier = Modifier.weight(1f),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis
                    )
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(
                                Context.CLIPBOARD_SERVICE
                            ) as? ClipboardManager
                            clipboard?.setPrimaryClip(
                                ClipData.newPlainText("Folder path", pathText)
                            )
                            Toast.makeText(
                                context,
                                "Folder path copied",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    ) {
                        Icon(
                            imageVector = LucideIcons.Copy,
                            contentDescription = "Copy folder path",
                            tint = foreground
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))
                HorizontalDivider(
                    color = if (isCustomTheme) {
                        MaterialTheme.colorScheme.outlineVariant
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                )
                Spacer(Modifier.height(8.dp))

                if (isScanning) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = if (isCustomTheme) Color.White
                                else MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Calculating folder details...",
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Scanning all nested folders and files.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                scanError?.let { message ->
                    Text(
                        text = "Scan failed: $message",
                        color = if (isCustomTheme) Color.White
                            else MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                stats?.let { result ->
                    FolderDetailsRow("Total Files", formatCount(result.fileCount))
                    FolderDetailsRow(
                        "Total Subfolders",
                        formatCount(result.folderCount)
                    )

                    val byteCount = NumberFormat
                        .getIntegerInstance(Locale.getDefault())
                        .format(result.totalBytes)
                    val useGigabytes = result.totalBytes >= 1024L * 1024L * 1024L
                    val displaySize = if (useGigabytes) {
                        result.totalBytes.toDouble() / (1024.0 * 1024.0 * 1024.0)
                    } else {
                        result.totalBytes.toDouble() / (1024.0 * 1024.0)
                    }
                    val displayUnit = if (useGigabytes) "GB" else "MB"

                    FolderDetailsRow(
                        "Total Size",
                        "${String.format(Locale.getDefault(), "%.2f", displaySize)} " +
                            "$displayUnit ($byteCount bytes)"
                    )
                    FolderDetailsRow(
                        "Last Modified",
                        formatTimestamp(result.lastModified)
                    )
                    FolderDetailsRow(
                        "Created",
                        formatTimestamp(result.createdAt)
                    )
                    FolderDetailsRow(
                        "Last Accessed",
                        formatTimestamp(result.lastAccessedAt)
                    )
                    FolderDetailsRow(
                        "Type",
                        result.mimeType ?: "Folder"
                    )
                    FolderDetailsRow(
                        "Readable",
                        if (result.canRead) "Yes" else "No"
                    )
                    FolderDetailsRow(
                        "Writable",
                        if (result.canWrite) "Yes" else "No"
                    )
                    FolderDetailsRow(
                        "Scan Duration",
                        String.format(
                            Locale.getDefault(),
                            "%.2f seconds",
                            result.elapsedMillis / 1000.0
                        )
                    )
                    if (result.unreadableEntries > 0L) {
                        FolderDetailsRow(
                            "Unreadable Entries",
                            formatCount(result.unreadableEntries)
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))
                HorizontalDivider(
                    color = if (isCustomTheme) {
                        MaterialTheme.colorScheme.outlineVariant
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Close")
                    }
                }
            }
        }
        }
    }
}

@Composable
private fun FolderDetailsRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            modifier = Modifier.width(124.dp),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = value,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

private fun formatCount(count: Long): String =
    NumberFormat.getIntegerInstance(Locale.getDefault()).format(count)

private fun formatTimestamp(timestamp: Long?): String {
    if (timestamp == null || timestamp <= 0L) return "Not available"
    return try {
        SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss",
            Locale.getDefault()
        ).format(Date(timestamp))
    } catch (_: Exception) {
        "Not available"
    }
}

private fun folderPathForDisplay(item: FileItem): String {
    if (!item.path.startsWith("saf:", ignoreCase = true)) {
        return item.file.absolutePath
    }

    val uri = try {
        Uri.parse(item.path.substring(4))
    } catch (_: Exception) {
        return item.path
    }

    val documentId = try {
        DocumentsContract.getDocumentId(uri)
    } catch (_: Exception) {
        try {
            DocumentsContract.getTreeDocumentId(uri)
        } catch (_: Exception) {
            return uri.toString()
        }
    }

    val separator = documentId.indexOf(':')
    if (separator < 0) return uri.toString()

    val volumeId = documentId.substring(0, separator)
    val relativePath = documentId.substring(separator + 1).trim('/')
    val volumeRoot = if (volumeId.equals("primary", ignoreCase = true)) {
        "/storage/emulated/0"
    } else {
        "/storage/$volumeId"
    }

    return if (relativePath.isBlank()) {
        volumeRoot
    } else {
        "$volumeRoot/$relativePath"
    }
}

private suspend fun scanFolderTree(
    context: Context,
    item: FileItem
): FolderTreeStats = withContext(Dispatchers.IO) {
    val startTime = SystemClock.elapsedRealtime()
    val accumulator = FolderTreeAccumulator()
    var modifiedAt: Long? = null
    var createdAt: Long? = null
    var accessedAt: Long? = null
    var mimeType: String? = "Directory"
    var canRead = false
    var canWrite = false

    if (item.path.startsWith("saf:", ignoreCase = true)) {
        val root = resolveSafFolder(context, item.path.substring(4))
            ?: throw IOException(
                "Unable to access this USB folder. Check the granted storage permission."
            )

        if (!root.isDirectory) {
            throw IOException("The selected item is not a directory.")
        }

        modifiedAt = root.lastModified().takeIf { it > 0L }
        mimeType = root.type ?: "Directory"
        canRead = root.canRead()
        canWrite = root.canWrite()

        val visited = HashSet<String>()

        fun visit(directory: DocumentFile) {
            if (!visited.add(directory.uri.toString())) return

            val children = try {
                directory.listFiles()
            } catch (_: Exception) {
                accumulator.addUnreadable()
                return
            }

            for (child in children) {
                try {
                    if (child.isDirectory) {
                        accumulator.folderCount++
                        if (child.canRead()) {
                            visit(child)
                        } else {
                            accumulator.addUnreadable()
                        }
                    } else {
                        accumulator.addFile(child.length())
                    }
                } catch (_: Exception) {
                    accumulator.addUnreadable()
                }
            }
        }

        if (root.canRead()) {
            visit(root)
        } else {
            accumulator.addUnreadable()
        }
    } else {
        val requested = File(item.path)
        val root = if (requested.isDirectory) requested else item.file

        if (!root.exists() || !root.isDirectory) {
            throw IOException("The selected folder is not accessible.")
        }

        modifiedAt = root.lastModified().takeIf { it > 0L }
        mimeType = "Directory"
        canRead = root.canRead()
        canWrite = root.canWrite()

        val attributes = readBasicAttributes(root)
        createdAt = attributes?.creationTime()?.toMillis()?.takeIf { it > 0L }
        accessedAt = attributes?.lastAccessTime()?.toMillis()?.takeIf { it > 0L }

        val visited = HashSet<String>()

        fun visit(directory: File) {
            val key = try {
                directory.canonicalPath
            } catch (_: Exception) {
                directory.absolutePath
            }

            if (!visited.add(key)) return

            val children = directory.listFiles()
            if (children == null) {
                accumulator.addUnreadable()
                return
            }

            for (child in children) {
                try {
                    if (child.isDirectory) {
                        accumulator.folderCount++
                        if (child.canRead()) {
                            visit(child)
                        } else {
                            accumulator.addUnreadable()
                        }
                    } else {
                        accumulator.addFile(child.length())
                    }
                } catch (_: Exception) {
                    accumulator.addUnreadable()
                }
            }
        }

        if (root.canRead()) {
            visit(root)
        } else {
            accumulator.addUnreadable()
        }
    }

    FolderTreeStats(
        fileCount = accumulator.fileCount,
        folderCount = accumulator.folderCount,
        totalBytes = accumulator.totalBytes,
        unreadableEntries = accumulator.unreadableEntries,
        lastModified = modifiedAt,
        createdAt = createdAt,
        lastAccessedAt = accessedAt,
        mimeType = mimeType,
        canRead = canRead,
        canWrite = canWrite,
        elapsedMillis = (SystemClock.elapsedRealtime() - startTime).coerceAtLeast(0L)
    )
}

private fun readBasicAttributes(file: File): BasicFileAttributes? {
    if (Build.VERSION.SDK_INT < 26) return null
    return try {
        Files.readAttributes(file.toPath(), BasicFileAttributes::class.java)
    } catch (_: Exception) {
        null
    }
}

private fun resolveSafFolder(
    context: Context,
    uriString: String
): DocumentFile? {
    val targetUri = try {
        Uri.parse(uriString)
    } catch (_: Exception) {
        return null
    }

    val targetDocumentId = try {
        DocumentsContract.getDocumentId(targetUri)
    } catch (_: Exception) {
        try {
            DocumentsContract.getTreeDocumentId(targetUri)
        } catch (_: Exception) {
            return null
        }
    }

    val candidates = LinkedHashSet<Uri>()

    try {
        context.contentResolver.persistedUriPermissions.forEach {
            candidates.add(it.uri)
        }
    } catch (_: Exception) {
        // Continue with stored tree URIs.
    }

    try {
        val preferences = context.getSharedPreferences(
            "fff_saf_storage",
            Context.MODE_PRIVATE
        )
        preferences.all.values.forEach { value ->
            if (value is String) {
                try {
                    val uri = Uri.parse(value)
                    if (uri.scheme == "content") candidates.add(uri)
                } catch (_: Exception) {
                    // Ignore non-URI preference values.
                }
            }
        }
    } catch (_: Exception) {
        // Persisted URI permissions are still considered.
    }

    candidates.add(targetUri)

    for (treeUri in candidates) {
        try {
            if (!DocumentsContract.isTreeUri(treeUri)) continue
            if (treeUri.authority != targetUri.authority) continue

            val treeDocumentId = DocumentsContract.getTreeDocumentId(treeUri)
            val isDescendant = if (treeDocumentId.endsWith(":")) {
                targetDocumentId.startsWith(treeDocumentId)
            } else {
                targetDocumentId == treeDocumentId ||
                    targetDocumentId.startsWith("$treeDocumentId/")
            }
            if (!isDescendant) continue

            var current: DocumentFile? =
                DocumentFile.fromTreeUri(context, treeUri)
            if (current == null) continue

            val remainder = targetDocumentId
                .removePrefix(treeDocumentId)
                .trim('/')

            if (remainder.isNotEmpty()) {
                for (segment in remainder.split('/')) {
                    current = current?.findFile(segment)
                    if (current == null) break
                }
            }

            if (current != null && current.isDirectory) return current
        } catch (_: Exception) {
            // Try another saved tree permission.
        }
    }

    return null
}
