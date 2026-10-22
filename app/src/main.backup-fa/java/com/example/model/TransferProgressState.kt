package com.example.model

import com.example.util.FileUtils
import java.io.File
import java.util.Locale

data class TransferProgressState(
    val isVisible: Boolean = false,
    val isCut: Boolean = false,
    val isPaused: Boolean = false,
    val isFinished: Boolean = false,
    val isCancelled: Boolean = false,
    val currentFileName: String = "",
    val destinationPath: String = "",
    val sourcePath: String = "",
    val totalFiles: Int = 0,
    val completedFiles: Int = 0,
    val totalBytes: Long = 0L,
    val transferredBytes: Long = 0L,
    val speedBytesPerSec: Long = 0L,
    val speedHistory: List<Float> = emptyList(), // in MB/s for the Windows 11 chart
    val peakSpeedBytesPerSec: Long = 0L,
    val estimatedSecondsRemaining: Long = 0L,
    val errorMessage: String? = null
) {
    val remainingFiles: Int
        get() = (totalFiles - completedFiles).coerceAtLeast(0)

    val remainingBytes: Long
        get() = (totalBytes - transferredBytes).coerceAtLeast(0L)

    val progressFraction: Float
        get() = if (totalBytes > 0L) {
            (transferredBytes.toDouble() / totalBytes.toDouble()).toFloat().coerceIn(0f, 1f)
        } else if (totalFiles > 0) {
            (completedFiles.toFloat() / totalFiles.toFloat()).coerceIn(0f, 1f)
        } else 0f

    val percentage: Int
        get() = (progressFraction * 100).toInt()

    val formattedCurrentSpeed: String
        get() = formatSpeed(speedBytesPerSec)

    val formattedPeakSpeed: String
        get() = formatSpeed(peakSpeedBytesPerSec)

    val formattedTransferredBytes: String
        get() = FileUtils.formatBytes(transferredBytes)

    val formattedTotalBytes: String
        get() = FileUtils.formatBytes(totalBytes)

    val formattedRemainingBytes: String
        get() = FileUtils.formatBytes(remainingBytes)

    val formattedTimeRemaining: String
        get() {
            if (isFinished) return "تکمیل شد"
            if (isPaused) return "متوقف شده"
            if (estimatedSecondsRemaining <= 0L || speedBytesPerSec <= 0L) return "در حال محاسبه زمان..."
            val mins = estimatedSecondsRemaining / 60
            val secs = estimatedSecondsRemaining % 60
            return when {
                mins > 60 -> String.format(Locale.getDefault(), "بیش از %d ساعت", mins / 60)
                mins > 0 -> String.format(Locale.getDefault(), "حدود %d دقیقه و %d ثانیه", mins, secs)
                else -> String.format(Locale.getDefault(), "حدود %d ثانیه", secs)
            }
        }

    private fun formatSpeed(bytesPerSec: Long): String {
        if (bytesPerSec <= 0) return "0 KB/s"
        val kb = bytesPerSec / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0
        return when {
            gb >= 1.0 -> String.format(Locale.US, "%.1f GB/s", gb)
            mb >= 1.0 -> String.format(Locale.US, "%.1f MB/s", mb)
            else -> String.format(Locale.US, "%.0f KB/s", kb)
        }
    }
}
