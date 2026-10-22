package com.example.util

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.documentfile.provider.DocumentFile

class SafStorageManager(context: Context) {

    private val appContext = context.applicationContext
    private val resolver: ContentResolver = appContext.contentResolver
    private val preferences = appContext.getSharedPreferences(
        "fff_saf_storage",
        Context.MODE_PRIVATE
    )

    fun saveTreePermission(key: String, uri: Uri, flags: Int): Boolean {
        val grantedFlags = flags and (
            Intent.FLAG_GRANT_READ_URI_PERMISSION or
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )

        return try {
            resolver.takePersistableUriPermission(uri, grantedFlags)
            preferences.edit().putString(key, uri.toString()).commit()
        } catch (_: SecurityException) {
            false
        } catch (_: IllegalArgumentException) {
            false
        }
    }

    fun getTreeUri(key: String): Uri? {
        val value = preferences.getString(key, null) ?: return null
        return try {
            Uri.parse(value)
        } catch (_: Exception) {
            null
        }
    }

    fun getTree(key: String): DocumentFile? {
        val uri = getTreeUri(key) ?: return null
        return try {
            DocumentFile.fromTreeUri(appContext, uri)
        } catch (_: SecurityException) {
            null
        }
    }

    fun hasWritePermission(uri: Uri): Boolean {
        return resolver.persistedUriPermissions.any {
            it.uri == uri && it.isWritePermission
        }
    }

    fun listChildren(treeKey: String): List<DocumentFile> {
        return try {
            getTree(treeKey)?.listFiles()?.toList() ?: emptyList()
        } catch (_: SecurityException) {
            emptyList()
        }
    }

    fun createDirectory(parent: DocumentFile, name: String): DocumentFile? {
        if (!parent.isDirectory || !parent.canWrite() || name.isBlank()) {
            return null
        }
        return try {
            parent.findFile(name) ?: parent.createDirectory(name)
        } catch (_: SecurityException) {
            null
        }
    }

    fun createFile(
        parent: DocumentFile,
        mimeType: String,
        name: String
    ): DocumentFile? {
        if (!parent.isDirectory || !parent.canWrite() || name.isBlank()) {
            return null
        }
        return try {
            parent.findFile(name) ?: parent.createFile(mimeType, name)
        } catch (_: SecurityException) {
            null
        }
    }

    fun rename(document: DocumentFile, newName: String): Boolean {
        if (!document.canWrite() || newName.isBlank()) return false
        return try {
            document.renameTo(newName)
        } catch (_: SecurityException) {
            false
        }
    }

    fun delete(document: DocumentFile): Boolean {
        if (!document.canWrite()) return false
        return try {
            document.delete()
        } catch (_: SecurityException) {
            false
        }
    }

    fun openInputStream(uri: Uri) = resolver.openInputStream(uri)

    fun openOutputStream(uri: Uri) = resolver.openOutputStream(uri, "wt")
}
