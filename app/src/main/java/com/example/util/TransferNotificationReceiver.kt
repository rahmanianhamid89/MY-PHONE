package com.example.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.MainActivity

class TransferNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return

        val activityIntent = Intent(context, MainActivity::class.java).apply {
            this.action = action
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }

        context.startActivity(activityIntent)
    }
}
