package com.example.util

import android.content.Context

object AlwaysOpenManager {

    private const val PREFS_NAME = "always_open_preferences"

    fun setAlwaysOpen(
        context: Context,
        mimeType: String,
        packageName: String,
        activityName: String
    ) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString("${mimeType}_package", packageName)
            .putString("${mimeType}_activity", activityName)
            .apply()
    }

    fun clearAlwaysOpen(
        context: Context,
        mimeType: String
    ) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove("${mimeType}_package")
            .remove("${mimeType}_activity")
            .apply()
    }

    fun getPackageName(
        context: Context,
        mimeType: String
    ): String? {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString("${mimeType}_package", null)
    }

    fun getActivityName(
        context: Context,
        mimeType: String
    ): String? {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString("${mimeType}_activity", null)
    }
}
