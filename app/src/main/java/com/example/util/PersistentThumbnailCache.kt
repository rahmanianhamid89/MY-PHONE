package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.caverock.androidsvg.SVG
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap

object PersistentThumbnailCache {

    private const val MAX_SIZE = 512

    private val memoryCache = ConcurrentHashMap<String, File>()

    suspend fun get(context: Context, source: File): File? =
        withContext(Dispatchers.IO) {
            if (!source.exists() || !source.isFile) return@withContext null

            val key = buildKey(source)

            memoryCache[key]?.let { cached ->
                if (cached.exists() && cached.length() > 0L) {
                    return@withContext cached
                } else {
                    memoryCache.remove(key)
                }
            }

            val directory = File(context.cacheDir, "thumbnail_cache")
            if (!directory.exists() && !directory.mkdirs()) return@withContext null

            val cached = File(directory, "$key.jpg")

            if (cached.exists() && cached.length() > 0L) {
                memoryCache[key] = cached
                return@withContext cached
            }

            val created = createThumbnail(source, cached)

            if (created != null) {
                memoryCache[key] = created
            }

            created
        }

    private fun buildKey(file: File): String {
        val value = "${file.absolutePath}|${file.length()}|${file.lastModified()}"
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(value.toByteArray())

        return digest.joinToString("") { "%02x".format(it) }
    }

    private fun createThumbnail(source: File, destination: File): File? {
        val bitmap = if (source.extension.equals("svg", ignoreCase = true)) {
            runCatching {
                val picture = source.inputStream().use {
                    SVG.getFromInputStream(it).renderToPicture()
                }
                val sourceWidth = picture.width.coerceAtLeast(1)
                val sourceHeight = picture.height.coerceAtLeast(1)
                val scale = minOf(
                    1f,
                    MAX_SIZE.toFloat() / sourceWidth,
                    MAX_SIZE.toFloat() / sourceHeight
                )
                val width = (sourceWidth * scale).toInt().coerceAtLeast(1)
                val height = (sourceHeight * scale).toInt().coerceAtLeast(1)
                Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).also {
                    android.graphics.Canvas(it).drawPicture(
                        picture,
                        android.graphics.Rect(0, 0, width, height)
                    )
                }
            }.getOrNull() ?: return null
        } else {
            val bounds = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }

            BitmapFactory.decodeFile(source.absolutePath, bounds)

            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
                return null
            }

            val sample = calculateSampleSize(
                bounds.outWidth,
                bounds.outHeight
            )

            val options = BitmapFactory.Options().apply {
                inSampleSize = sample
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }

            BitmapFactory.decodeFile(source.absolutePath, options) ?: return null
        }

        val thumbnailBitmap = if (bitmap.hasAlpha()) {
            val result = Bitmap.createBitmap(
                bitmap.width,
                bitmap.height,
                Bitmap.Config.ARGB_8888
            )

            val canvas = android.graphics.Canvas(result)
            canvas.drawColor(android.graphics.Color.rgb(190, 190, 190))
            canvas.drawBitmap(bitmap, 0f, 0f, null)
            bitmap.recycle()
            result
        } else {
            bitmap
        }

        return try {
            val temporary = File(destination.parentFile, "${destination.name}.tmp")

            FileOutputStream(temporary).use { output ->
                if (!thumbnailBitmap.compress(Bitmap.CompressFormat.JPEG, 88, output)) {
                    return null
                }
            }

            if (!temporary.renameTo(destination)) {
                temporary.delete()
                return null
            }

            destination
        } finally {
            thumbnailBitmap.recycle()
        }
    }

    private fun calculateSampleSize(width: Int, height: Int): Int {
        var sample = 1
        var currentWidth = width
        var currentHeight = height

        while (currentWidth / 2 >= MAX_SIZE &&
            currentHeight / 2 >= MAX_SIZE
        ) {
            currentWidth /= 2
            currentHeight /= 2
            sample *= 2
        }

        return sample
    }
}
