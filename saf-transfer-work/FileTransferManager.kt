package com.example.util

import android.content.Context
import com.example.model.ClipboardSafItem
import androidx.documentfile.provider.DocumentFile
import android.webkit.MimeTypeMap
import android.net.Uri
import android.media.MediaScannerConnection

import com.example.model.TransferProgressState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.concurrent.atomic.AtomicBoolean

enum class FileConflictAction {
    OVERWRITE,
    SKIP,
    RENAME
}

class FileTransferManager(private val context: Context) {

    private val _transferState = MutableStateFlow(TransferProgressState())
    val transferState: StateFlow<TransferProgressState> = _transferState.asStateFlow()

    private val isPaused = AtomicBoolean(false)
    private val isCancelled = AtomicBoolean(false)

    fun pause() {
        isPaused.set(true)
        _transferState.value = _transferState.value.copy(
            isPaused = true,
            speedBytesPerSec = 0L
        )
    }

    fun resume() {
        isPaused.set(false)
        _transferState.value = _transferState.value.copy(
            isPaused = false
        )
    }

    fun cancel() {
        isCancelled.set(true)
        _transferState.value = _transferState.value.copy(
            isCancelled = true,
            isPaused = false,
            speedBytesPerSec = 0L
        )
    }

    fun dismiss() {
        _transferState.value = TransferProgressState(isVisible = false)
    }

    suspend fun executeTransfer(
        items: List<File>,
        targetDir: File,
        isCut: Boolean,
        onConflict: suspend (source: File, destination: File) -> FileConflictAction,
        onComplete: (successCount: Int) -> Unit
    ) = withContext(Dispatchers.IO) {
        if (items.isEmpty()) return@withContext

        isPaused.set(false)
        isCancelled.set(false)

        // 1. Scan files and compute total bytes & file count
        var totalBytes = 0L
        var totalFiles = 0
        val allTasks = mutableListOf<Pair<File, File>>() // Pair<Source, Destination>

        fun scan(source: File, destFolder: File) {
            val dest = File(destFolder, source.name)
            if (source.isDirectory) {
                val children = source.listFiles()
                if (children.isNullOrEmpty()) {
                    // Empty folder
                    allTasks.add(source to dest)
                    totalFiles++
                } else {
                    for (child in children) {
                        scan(child, dest)
                    }
                }
            } else {
                allTasks.add(source to dest)
                totalFiles++
                totalBytes += source.length()
            }
        }

        for (item in items) {
            scan(item, targetDir)
        }

        if (totalFiles == 0) totalFiles = items.size

        val speedHistory = mutableListOf(0f)
        var transferredBytes = 0L
        var completedFiles = 0
        var peakSpeedBytesPerSec = 0L
        val sourceParent = items.firstOrNull()?.parent ?: ""

        _transferState.value = TransferProgressState(
            isVisible = true,
            isCut = isCut,
            isPaused = false,
            isFinished = false,
            isCancelled = false,
            currentFileName = items.firstOrNull()?.name ?: "",
            destinationPath = targetDir.absolutePath,
            sourcePath = sourceParent,
            totalFiles = totalFiles,
            completedFiles = 0,
            totalBytes = totalBytes,
            transferredBytes = 0L,
            speedBytesPerSec = 0L,
            speedHistory = listOf(0f),
            peakSpeedBytesPerSec = 0L,
            estimatedSecondsRemaining = 0L
        )

        var lastSampleTime = System.currentTimeMillis()
        var lastSampleBytes = 0L
        var successCount = 0

        // 2. Perform Transfer
        val buffer = ByteArray(64 * 1024) // 64 KB buffer

        for ((source, dest) in allTasks) {
            if (isCancelled.get()) break

            completedFiles++
            _transferState.value = _transferState.value.copy(
                currentFileName = source.name,
                sourcePath = source.parent ?: sourceParent,
                completedFiles = completedFiles
            )

            if (source.isDirectory) {
                if (!dest.exists()) dest.mkdirs()
                continue
            }

            // Handle existing destination before copying.
            var actualDest = dest


            if (source.isFile && actualDest.exists()) {
                when (onConflict(source, actualDest)) {
                    FileConflictAction.OVERWRITE -> {
                        // Keep the original destination.
                            if (source.canonicalPath == actualDest.canonicalPath) {
                                continue
                            }
                    }

                    FileConflictAction.SKIP -> {
                        transferredBytes += source.length()
                        continue
                    }

                    FileConflictAction.RENAME -> {
                        val parent = actualDest.parentFile ?: targetDir
                        val name = actualDest.name
                        val dot = name.lastIndexOf('.')
                        val base = if (dot > 0) name.substring(0, dot) else name
                        val extension = if (dot > 0) name.substring(dot) else ""

                        var index = 1
                        var candidate: File
                        do {
                            candidate = File(parent, "$base ($index)$extension")
                            index++
                        } while (candidate.exists())

                        actualDest = candidate
                    }
                }
            }

            // Ensure destination directory exists
            actualDest.parentFile?.let { if (!it.exists()) it.mkdirs() }

            var currentFileCopied = 0L
            var errorOccurred = false

            try {
                FileInputStream(source).use { input ->
                    FileOutputStream(actualDest).use { output ->
                        var bytesRead: Int
                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            if (isCancelled.get()) {
                                break
                            }

                            // Handle Pause
                            while (isPaused.get() && !isCancelled.get()) {
                                delay(120)
                            }
                            if (isCancelled.get()) break

                            output.write(buffer, 0, bytesRead)
                            transferredBytes += bytesRead
                            currentFileCopied += bytesRead

                            val now = System.currentTimeMillis()
                            val elapsed = now - lastSampleTime
                            if (elapsed >= 150) {
                                val deltaBytes = transferredBytes - lastSampleBytes
                                val speed = ((deltaBytes.toDouble() / elapsed.toDouble()) * 1000.0).toLong()
                                if (speed > peakSpeedBytesPerSec) {
                                    peakSpeedBytesPerSec = speed
                                }
                                val speedMB = (speed / (1024f * 1024f)).coerceAtLeast(0f)
                                speedHistory.add(speedMB)

                                val remainingBytes = (totalBytes - transferredBytes).coerceAtLeast(0L)
                                val estSeconds = if (speed > 0) remainingBytes / speed else 0L

                                _transferState.value = _transferState.value.copy(
                                    transferredBytes = transferredBytes,
                                    speedBytesPerSec = speed,
                                    peakSpeedBytesPerSec = peakSpeedBytesPerSec,
                                    speedHistory = speedHistory.toList(),
                                    estimatedSecondsRemaining = estSeconds
                                )

                                lastSampleTime = now
                                lastSampleBytes = transferredBytes
                            }
                        }

                        // Force completed file data to the USB storage before starting the next file.
                        output.flush()
                        output.fd.sync()
                    }
                }

                // Make copied media files immediately visible to Gallery and other media apps.
                val ext = actualDest.extension.lowercase()
                val isMediaFile = ext in setOf(
                    "jpg", "jpeg", "png", "webp", "gif", "bmp", "heic",
                    "mp4", "mkv", "webm", "avi", "3gp", "mov", "flv", "ts"
                )

                if (isMediaFile && actualDest.exists()) {
                    MediaScannerConnection.scanFile(
                        context,
                        arrayOf(actualDest.absolutePath),
                        null,
                        null
                    )
                }
            } catch (e: Exception) {
                errorOccurred = true
                e.printStackTrace()
            }

            if (isCancelled.get()) {
                // Delete incomplete destination file
                if (actualDest.exists()) actualDest.delete()
                break
            }

            if (!errorOccurred) {
                successCount++

                if (isCut) {
                    // In cut mode, delete source after successful copy
                    try {
                        source.delete()
                    } catch (_: Exception) {}
                }

                // If small files transferred too fast, update speed history smoothly
                val now = System.currentTimeMillis()
                val elapsed = (now - lastSampleTime).coerceAtLeast(1L)
                val deltaBytes = transferredBytes - lastSampleBytes
                val currentSpeed = if (deltaBytes > 0) ((deltaBytes.toDouble() / elapsed) * 1000.0).toLong() else _transferState.value.speedBytesPerSec
                if (currentSpeed > peakSpeedBytesPerSec) peakSpeedBytesPerSec = currentSpeed
                val speedMB = (currentSpeed / (1024f * 1024f)).coerceAtLeast(0.1f)
                speedHistory.add(speedMB)

                _transferState.value = _transferState.value.copy(
                    completedFiles = completedFiles,
                    transferredBytes = transferredBytes,
                    speedBytesPerSec = currentSpeed,
                    peakSpeedBytesPerSec = peakSpeedBytesPerSec,
                    speedHistory = speedHistory.toList()
                )
                lastSampleTime = now
                lastSampleBytes = transferredBytes
            }
        }

        // Clean up empty directories in isCut mode
        if (isCut && !isCancelled.get()) {
            for (item in items) {
                if (item.isDirectory && item.exists()) {
                    try {
                        item.deleteRecursively()
                    } catch (_: Exception) {}
                }
            }
        }

        if (isCancelled.get()) {
            _transferState.value = _transferState.value.copy(
                isCancelled = true,
                isPaused = false,
                speedBytesPerSec = 0L
            )
        } else {
            // Final completion state
            speedHistory.add(0f)

            _transferState.value = _transferState.value.copy(
                isFinished = true,
                isPaused = false,
                completedFiles = totalFiles,
                transferredBytes = totalBytes,
                speedBytesPerSec = 0L,
                speedHistory = speedHistory.toList(),
                estimatedSecondsRemaining = 0L
            )
        }

        onComplete(successCount)
    }

    suspend fun executeMixedTransfer(
        items: List<File>,
        safItems: List<ClipboardSafItem>,
        targetDir: File?,
        targetSafDir: DocumentFile?,
        isCut: Boolean,
        onConflict: suspend (File, File) -> FileConflictAction,
        onComplete: (Int) -> Unit
    ) = withContext(Dispatchers.IO) {
        if (items.isEmpty() && safItems.isEmpty()) return@withContext
        if ((targetDir == null) == (targetSafDir == null)) {
            _transferState.value = TransferProgressState(
                isVisible = true,
                isFinished = true,
                errorMessage = "Invalid transfer destination"
            )
            onComplete(0)
            return@withContext
        }

        isPaused.set(false)
        isCancelled.set(false)

        data class Source(
            val file: File?,
            val document: DocumentFile?,
            val name: String,
            val treeUri: String
        )

        fun findDocument(root: DocumentFile, uri: String): DocumentFile? {
            if (root.uri.toString() == uri) return root
            if (!root.isDirectory) return null
            val children = try { root.listFiles() } catch (_: Exception) { emptyArray() }
            for (child in children) {
                val result = findDocument(child, uri)
                if (result != null) return result
            }
            return null
        }

        val sources = mutableListOf<Source>()
        items.forEach { sources.add(Source(it, null, it.name, "")) }
        safItems.forEach { item ->
            val uri = Uri.parse(item.uri)
            val doc = try { DocumentFile.fromSingleUri(context, uri) } catch (_: Exception) { null }
            sources.add(Source(null, doc, item.name, item.treeUri))
        }

        fun resolve(source: Source): DocumentFile? {
            if (source.file != null) return null
            val wanted = safItems.firstOrNull {
                it.name == source.name && it.treeUri == source.treeUri
            } ?: return source.document
            if (source.treeUri.isBlank()) return source.document
            val root = try {
                DocumentFile.fromTreeUri(context, Uri.parse(source.treeUri))
            } catch (_: Exception) { null }
            return root?.let { findDocument(it, wanted.uri) } ?: source.document
        }

        fun countFile(file: File): Int {
            if (!file.isDirectory) return 1
            val children = file.listFiles() ?: return 1
            if (children.isEmpty()) return 1
            return children.sumOf { countFile(it) }
        }

        fun countDoc(doc: DocumentFile): Int {
            if (!doc.isDirectory) return 1
            val children = try { doc.listFiles() } catch (_: Exception) { emptyArray() }
            if (children.isEmpty()) return 1
            return children.sumOf { countDoc(it) }
        }

        val total = sources.sumOf { src ->
            if (src.file != null) countFile(src.file)
            else resolve(src)?.let { countDoc(it) } ?: 1
        }.coerceAtLeast(1)

        var completed = 0
        var succeeded = 0
        var failed = false
        var transferred = 0L
        val buffer = ByteArray(64 * 1024)

        _transferState.value = TransferProgressState(
            isVisible = true,
            isCut = isCut,
            isFinished = false,
            currentFileName = sources.firstOrNull()?.name.orEmpty(),
            destinationPath = targetSafDir?.uri?.toString() ?: targetDir!!.absolutePath,
            sourcePath = "Mixed storage",
            totalFiles = total
        )

        suspend fun copyNode(
            srcFile: File?,
            srcDoc: DocumentFile?,
            name: String,
            dstFileParent: File?,
            dstDocParent: DocumentFile?
        ): Boolean {
            if (isCancelled.get()) return false
            while (isPaused.get() && !isCancelled.get()) delay(100)
            if (isCancelled.get()) return false

            val directory = srcFile?.isDirectory ?: (srcDoc?.isDirectory == true)
            var finalName = name
            var dstFile = dstFileParent?.let { File(it, finalName) }
            var dstDoc = dstDocParent?.findFile(finalName)
            var exists = dstFile?.exists() == true || dstDoc != null

            if (exists) {
                when (onConflict(File(name), File(finalName))) {
                    FileConflictAction.SKIP -> return false
                    FileConflictAction.OVERWRITE -> {
                        val removed = if (dstFile != null) dstFile.deleteRecursively()
                        else try { dstDoc?.delete() == true } catch (_: Exception) { false }
                        if (!removed) return false
                    }
                    FileConflictAction.RENAME -> {
                        val dot = if (directory) -1 else finalName.lastIndexOf('.')
                        val base = if (dot > 0) finalName.substring(0, dot) else finalName
                        val ext = if (dot > 0) finalName.substring(dot) else ""
                        var i = 1
                        do {
                            finalName = "$base ($i)$ext"
                            dstFile = dstFileParent?.let { File(it, finalName) }
                            dstDoc = dstDocParent?.findFile(finalName)
                            exists = dstFile?.exists() == true || dstDoc != null
                            i++
                        } while (exists && i < 10000)
                        if (exists) return false
                    }
                }
            }

            if (directory) {
                val newFileDir = if (dstFileParent != null) {
                    val d = File(dstFileParent, finalName)
                    if (!d.exists() && !d.mkdirs()) return false
                    if (!d.isDirectory) return false
                    d
                } else null
                val newDocDir = if (dstDocParent != null) {
                    try { dstDocParent.createDirectory(finalName) } catch (_: Exception) { null }
                } else null
                if (dstFileParent != null && newFileDir == null) return false
                if (dstDocParent != null && newDocDir == null) return false

                val fileChildren = if (srcFile != null) srcFile.listFiles() ?: return false else emptyArray()
                val docChildren = if (srcDoc != null) {
                    try { srcDoc.listFiles() } catch (_: Exception) { return false }
                } else emptyArray()

                var ok = true
                for (child in fileChildren) {
                    if (!copyNode(child, null, child.name, newFileDir, newDocDir)) ok = false
                }
                for (child in docChildren) {
                    if (!copyNode(null, child, child.name ?: continue, newFileDir, newDocDir)) ok = false
                }
                completed++
                _transferState.value = _transferState.value.copy(
                    completedFiles = completed,
                    currentFileName = finalName
                )
                return ok
            }

            val input = try {
                if (srcFile != null) srcFile.inputStream()
                else srcDoc?.uri?.let { context.contentResolver.openInputStream(it) }
            } catch (_: Exception) { null } ?: return false

            val output = try {
                if (dstFileParent != null) {
                    if (!dstFileParent.exists() && !dstFileParent.mkdirs()) return false
                    val out = File(dstFileParent, finalName)
                    dstFile = out
                    out.outputStream()
                } else {
                    val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                        finalName.substringAfterLast('.', "").lowercase()
                    ) ?: "application/octet-stream"
                    val created = dstDocParent?.createFile(mime, finalName) ?: return false
                    dstDoc = created
                    context.contentResolver.openOutputStream(created.uri, "wt")
                }
            } catch (_: Exception) { null } ?: return false

            try {
                input.use { ins ->
                    output.use { outs ->
                        while (true) {
                            if (isCancelled.get()) return false
                            while (isPaused.get() && !isCancelled.get()) delay(100)
                            if (isCancelled.get()) return false
                            val n = ins.read(buffer)
                            if (n < 0) break
                            outs.write(buffer, 0, n)
                            transferred += n
                            _transferState.value = _transferState.value.copy(
                                currentFileName = finalName,
                                transferredBytes = transferred
                            )
                        }
                        outs.flush()
                    }
                }
            } catch (_: Exception) {
                try { dstFile?.delete() } catch (_: Exception) {}
                try { dstDoc?.delete() } catch (_: Exception) {}
                return false
            }

            completed++
            _transferState.value = _transferState.value.copy(completedFiles = completed)
            return true
        }

        for (source in sources) {
            if (isCancelled.get()) break
            val doc = resolve(source)
            if (source.file == null && doc == null) {
                failed = true
                continue
            }
            if (copyNode(source.file, doc, source.name, targetDir, targetSafDir)) succeeded++
            else failed = true
        }

        if (isCut && !isCancelled.get() && !failed && succeeded == sources.size) {
            for (source in sources) {
                try {
                    if (source.file != null) {
                        if (!source.file.deleteRecursively()) failed = true
                    } else if (resolve(source)?.delete() != true) {
                        failed = true
                    }
                } catch (_: Exception) { failed = true }
            }
        }

        _transferState.value = _transferState.value.copy(
            isFinished = true,
            isCancelled = isCancelled.get(),
            isPaused = false,
            completedFiles = completed.coerceAtMost(total),
            transferredBytes = transferred,
            speedBytesPerSec = 0L,
            errorMessage = when {
                isCancelled.get() -> "Transfer cancelled; source files were preserved"
                failed -> "Some items failed or were skipped; cut sources were preserved"
                else -> null
            }
        )
        onComplete(succeeded)
    }

}
