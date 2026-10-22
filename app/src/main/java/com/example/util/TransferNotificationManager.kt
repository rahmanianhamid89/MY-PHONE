package com.example.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.model.TransferProgressState

class TransferNotificationManager(
    private val context: Context
) {

    private var lastNotificationUpdateTime = 0L

    companion object {
        const val CHANNEL_ID = "file_transfer"
        const val NOTIFICATION_ID = 4101

        const val ACTION_PAUSE_RESUME =
            "com.example.fff.ACTION_PAUSE_RESUME"
        const val ACTION_CANCEL =
            "com.example.fff.ACTION_CANCEL"
    }

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "File transfers",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "File transfer progress"
                }
            )
        }
    }

    fun showOrUpdate(state: TransferProgressState) {
        val now = System.currentTimeMillis()
        if (state.isVisible &&
            !state.isFinished &&
            !state.isCancelled &&
            now - lastNotificationUpdateTime < 500L
        ) {
            return
        }
        lastNotificationUpdateTime = now

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        if (!state.isVisible || state.isFinished || state.isCancelled) {
            notificationManager.cancel(NOTIFICATION_ID)
            return
        }

        val progress = if (state.totalBytes > 0L) {
            ((state.transferredBytes.toDouble() / state.totalBytes.toDouble()) * 10000.0)
                .coerceIn(0.0, 10000.0)
                .toInt()
        } else {
            0
        }

        val percentage = progress / 100

        val actionIntent = Intent(context, TransferNotificationReceiver::class.java)

        val pauseResumeIntent = PendingIntent.getBroadcast(
            context,
            1001,
            actionIntent.setAction(ACTION_PAUSE_RESUME),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val cancelIntent = PendingIntent.getBroadcast(
            context,
            1002,
            Intent(context, TransferNotificationReceiver::class.java)
                .setAction(ACTION_CANCEL),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val pauseResumeTitle = if (state.isPaused) {
            "Resume"
        } else {
            "Pause"
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(com.example.R.drawable.ic_notification_folder)
            .setContentTitle("File transfer")
            .setContentText("$percentage% • ${state.completedFiles}/${state.totalFiles} • ${state.formattedTransferredBytes} / ${state.formattedTotalBytes} • ${state.currentFileName}")
            .setProgress(10000, progress, false)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(
                PendingIntent.getActivity(
                    context,
                    1003,
                    Intent(context, MainActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )
            .addAction(
                0,
                pauseResumeTitle,
                pauseResumeIntent
            )
            .addAction(
                0,
                "Cancel",
                cancelIntent
            )

        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    fun cancel() {
        notificationManager.cancel(NOTIFICATION_ID)
    }
}
