package com.example.util

import android.content.Context
import com.example.model.FileCategory
import com.example.model.FileItem
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.security.MessageDigest

/**
 * Persistent metadata cache for ordinary filesystem directories.
 * Cached listings can be shown immediately, then reconciled with disk.
 */
object DirectoryMetadataCache {
    private const val VERSION = 2
    private const val CACHE_DIRECTORY = "directory_metadata_cache"

    private fun cacheFile(context: Context, directory: File): File {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(directory.absolutePath.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }

        return File(
            File(context.cacheDir, CACHE_DIRECTORY),
            "$digest.json"
        )
    }

    fun load(context: Context, directory: File): List<FileItem>? {
        val source = cacheFile(context, directory)
        if (!source.isFile) return null

        return try {
            val root = JSONObject(source.readText(Charsets.UTF_8))

            if (root.optInt("version") != VERSION ||
                root.optString("directory") != directory.absolutePath
            ) {
                return null
            }

            val array = root.optJSONArray("items") ?: return null
            val result = mutableListOf<FileItem>()

            for (index in 0 until array.length()) {
                val json = array.optJSONObject(index) ?: continue
                val path = json.optString("path")
                if (path.isBlank()) continue

                val file = File(path)
                val category = try {
                    FileCategory.valueOf(json.optString("category"))
                } catch (_: Exception) {
                    FileUtils.getCategory(file)
                }

                val childCount = if (json.isNull("childCount")) {
                    null
                } else {
                    json.optInt("childCount")
                }

                result.add(
                    FileItem(
                        file = file,
                        name = json.optString("name"),
                        path = path,
                        size = json.optLong("size"),
                        formattedSize = json.optString("formattedSize"),
                        lastModified = json.optLong("lastModified"),
                        formattedDate = json.optString("formattedDate"),
                        isDirectory = json.optBoolean("isDirectory"),
                        extension = json.optString("extension"),
                        childCount = childCount,
                        category = category,
                        canRead = json.optBoolean("canRead", true),
                        canWrite = json.optBoolean("canWrite", true),
                        isHidden = json.optBoolean("isHidden")
                    )
                )
            }

            result
        } catch (_: Exception) {
            null
        }
    }

    fun reconcile(
        context: Context,
        directory: File,
        files: List<File>,
        cachedItems: List<FileItem>?,
        calculateChildCounts: Boolean
    ): List<FileItem> {
        val cachedByPath = cachedItems.orEmpty().associateBy { it.path }

        val result = files.map { file ->
            val path = file.absolutePath
            val cached = cachedByPath[path]
            val isDirectory = file.isDirectory
            val size = if (isDirectory) 0L else file.length()
            val modified = file.lastModified()
            val canRead = file.canRead()
            val canWrite = file.canWrite()

            val unchanged = cached != null &&
                cached.name == file.name &&
                cached.isDirectory == isDirectory &&
                cached.size == size &&
                cached.lastModified == modified &&
                cached.canRead == canRead &&
                cached.canWrite == canWrite &&
                (!isDirectory || !calculateChildCounts || cached.childCount != null)

            if (unchanged) {
                cached!!.copy(file = file)
            } else {
                FileUtils.toFileItem(
                    file,
                    calculateChildCount = calculateChildCounts
                )
            }
        }

        save(context, directory, result)
        return result
    }

    fun save(
        context: Context,
        directory: File,
        items: List<FileItem>
    ) {
        try {
            val destination = cacheFile(context, directory)
            val parent = destination.parentFile ?: return
            if (!parent.exists() && !parent.mkdirs()) return

            val array = JSONArray()

            for (item in items) {
                if (item.virtualZipArchive != null) continue

                val json = JSONObject()
                    .put("name", item.name)
                    .put("path", item.path)
                    .put("size", item.size)
                    .put("formattedSize", item.formattedSize)
                    .put("lastModified", item.lastModified)
                    .put("formattedDate", item.formattedDate)
                    .put("isDirectory", item.isDirectory)
                    .put("extension", item.extension)
                    .put("category", item.category.name)
                    .put("canRead", item.canRead)
                    .put("canWrite", item.canWrite)
                    .put("isHidden", item.isHidden)

                json.put(
                    "childCount",
                    item.childCount ?: JSONObject.NULL
                )
                array.put(json)
            }

            val root = JSONObject()
                .put("version", VERSION)
                .put("directory", directory.absolutePath)
                .put("items", array)

            val temporary = File(parent, destination.name + ".tmp")
            temporary.writeText(root.toString(), Charsets.UTF_8)

            if (!temporary.renameTo(destination)) {
                destination.writeText(root.toString(), Charsets.UTF_8)
                temporary.delete()
            }
        } catch (_: Exception) {
            // Browsing must continue even if the cache cannot be saved.
        }
    }
}
