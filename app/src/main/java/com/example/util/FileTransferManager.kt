package com.example.util

import android.content.Context
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
}
