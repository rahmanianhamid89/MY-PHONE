package com.example.viewmodel

import android.content.Context
import android.os.Build
import android.os.storage.StorageManager
import android.os.storage.StorageVolume
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.R)
internal class StorageVolumeCallbackCompat(
    private val context: Context,
    private val storageManager: StorageManager,
    private val onChanged: () -> Unit
) {
    private val callback = object : StorageManager.StorageVolumeCallback() {
        override fun onStateChanged(volume: StorageVolume) {
            onChanged()
        }
    }

    fun register() {
        storageManager.registerStorageVolumeCallback(context.mainExecutor, callback)
    }

    fun unregister() {
        storageManager.unregisterStorageVolumeCallback(callback)
    }
}
