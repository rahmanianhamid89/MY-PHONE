package com.example.util

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import java.util.zip.ZipFile
import net.lingala.zip4j.ZipFile as Zip4jFile

object VirtualZipUtils {

    private val passwordCache = mutableMapOf<String, CharArray>()

    data class Entry(
        val name: String,
        val path: String,
        val isDirectory: Boolean,
        val size: Long,
        val lastModified: Long
    )

    fun verifyAndSetPassword(zipFile: File, password: String): Boolean {
        if (!zipFile.exists() || !zipFile.isFile) return false

        return try {
            val zip = Zip4jFile(zipFile)
            zip.setPassword(password.toCharArray())

            val fileHeader = zip.fileHeaders.firstOrNull { !it.isDirectory }
            if (fileHeader == null) {
                passwordCache[zipFile.absolutePath] = password.toCharArray()
                true
            } else {
                zip.getInputStream(fileHeader).use { input ->
                    input.read()
                }
                passwordCache[zipFile.absolutePath] = password.toCharArray()
                true
            }
        } catch (_: Exception) {
            false
        }
    }

    fun clearPassword(zipFile: File) {
        passwordCache.remove(zipFile.absolutePath)?.fill('\u0000')
    }

    private fun getPassword(zipFile: File): CharArray? {
        return passwordCache[zipFile.absolutePath]?.copyOf()
    }

    private fun isEncrypted(zipFile: File): Boolean {
        return try {
            Zip4jFile(zipFile).isEncrypted
        } catch (_: Exception) {
            false
        }
    }

    fun list(zipFile: File, directoryPath: String = ""): List<Entry> {
        if (!zipFile.exists() || !zipFile.isFile) return emptyList()

        val password = getPassword(zipFile)

        if (isEncrypted(zipFile)) {
            if (password == null) return emptyList()

            return try {
                val prefix = directoryPath
                    .trim('/')
                    .let { if (it.isEmpty()) "" else "$it/" }

                val result = linkedMapOf<String, Entry>()

                val zip = Zip4jFile(zipFile)
                zip.setPassword(password)

                for (entry in zip.fileHeaders) {
                    val name = entry.fileName.trim('/')
                    if (name.isEmpty() || !name.startsWith(prefix)) continue

                    val remainder = name.removePrefix(prefix)
                    if (remainder.isEmpty()) continue

                    val slashIndex = remainder.indexOf('/')

                    if (slashIndex >= 0) {
                        val childName = remainder.substring(0, slashIndex)
                        val childPath = prefix + childName

                        result.putIfAbsent(
                            childPath,
                            Entry(
                                name = childName,
                                path = childPath,
                                isDirectory = true,
                                size = 0L,
                                lastModified = entry.lastModifiedTimeEpoch.takeIf { it > 0L } ?: 0L
                            )
                        )
                    } else {
                        result[name] = Entry(
                            name = remainder,
                            path = name,
                            isDirectory = entry.isDirectory,
                            size = if (entry.isDirectory) 0L else entry.uncompressedSize.coerceAtLeast(0L),
                            lastModified = entry.lastModifiedTimeEpoch.takeIf { it > 0L } ?: 0L
                        )
                    }
                }

                result.values.toList()
            } catch (_: Exception) {
                emptyList()
            }
        }

        val prefix = directoryPath
            .trim('/')
            .let { if (it.isEmpty()) "" else "$it/" }

        val result = linkedMapOf<String, Entry>()

        ZipFile(zipFile).use { zip ->
            val entries = zip.entries()

            while (entries.hasMoreElements()) {
                val entry = entries.nextElement()
                val name = entry.name.trim('/')

                if (name.isEmpty() || !name.startsWith(prefix)) continue

                val remainder = name.removePrefix(prefix)
                if (remainder.isEmpty()) continue

                val slashIndex = remainder.indexOf('/')

                if (slashIndex >= 0) {
                    val childName = remainder.substring(0, slashIndex)
                    val childPath = prefix + childName

                    result.putIfAbsent(
                        childPath,
                        Entry(
                            name = childName,
                            path = childPath,
                            isDirectory = true,
                            size = 0L,
                            lastModified = entry.time.takeIf { it > 0L } ?: 0L
                        )
                    )
                } else {
                    result[name] = Entry(
                        name = remainder,
                        path = name,
                        isDirectory = entry.isDirectory,
                        size = if (entry.isDirectory) 0L else entry.size.coerceAtLeast(0L),
                        lastModified = entry.time.takeIf { it > 0L } ?: 0L
                    )
                }
            }
        }

        return result.values.toList()
    }

    fun materializeEntry(
        context: Context,
        zipFile: File,
        entryPath: String
    ): File? {
        if (!zipFile.exists() || !zipFile.isFile) return null

        val target = entryPath.trim('/')
        if (target.isEmpty()) return null

        val cacheDir = File(context.cacheDir, "virtual_zip")
        if (!cacheDir.exists() && !cacheDir.mkdirs()) return null

        val digest = MessageDigest.getInstance("SHA-256")
            .digest(
                "${zipFile.absolutePath}|${zipFile.lastModified()}|${zipFile.length()}|$target"
                    .toByteArray()
            )
            .joinToString("") { "%02x".format(it) }

        val extension = File(target).extension
        val safeName = if (extension.isNotBlank()) {
            "$digest.$extension"
        } else {
            digest
        }

        val outputFile = File(cacheDir, safeName)

        if (outputFile.exists() && outputFile.isFile && outputFile.length() > 0L) {
            return outputFile
        }

        val password = getPassword(zipFile)

        return try {
            if (isEncrypted(zipFile)) {
                if (password == null) return null

                val zip = Zip4jFile(zipFile)
                zip.setPassword(password)

                val entry = zip.getFileHeader(target)
                    ?: zip.fileHeaders.firstOrNull { it.fileName.trim('/') == target }
                    ?: return null

                if (entry.isDirectory) return null

                zip.getInputStream(entry).use { input ->
                    FileOutputStream(outputFile).use { output ->
                        input.copyTo(output)
                    }
                }
            } else {
                ZipFile(zipFile).use { zip ->
                    val entry = zip.getEntry(target)
                        ?: zip.entries().asSequence()
                            .firstOrNull { it.name.trim('/') == target }
                        ?: return null

                    if (entry.isDirectory) return null

                    zip.getInputStream(entry).use { input ->
                        FileOutputStream(outputFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                }
            }

            outputFile.takeIf { it.exists() && it.isFile }
        } catch (_: Exception) {
            outputFile.delete()
            null
        }
    }

    fun findEntry(zipFile: File, entryPath: String): Entry? {
        val target = entryPath.trim('/')
        if (target.isEmpty()) return null

        val password = getPassword(zipFile)

        if (isEncrypted(zipFile)) {
            if (password == null) return null

            return try {
                val zip = Zip4jFile(zipFile)
                zip.setPassword(password)

                val entry = zip.getFileHeader(target)
                    ?: zip.fileHeaders.firstOrNull { it.fileName.trim('/') == target }

                entry?.let {
                    Entry(
                        name = target.substringAfterLast('/'),
                        path = target,
                        isDirectory = it.isDirectory,
                        size = if (it.isDirectory) 0L else it.uncompressedSize.coerceAtLeast(0L),
                        lastModified = it.lastModifiedTimeEpoch.takeIf { time -> time > 0L } ?: 0L
                    )
                }
            } catch (_: Exception) {
                null
            }
        }

        ZipFile(zipFile).use { zip ->
            val entry = zip.getEntry(target)
                ?: zip.entries().asSequence()
                    .firstOrNull { it.name.trim('/') == target }

            return entry?.let {
                Entry(
                    name = target.substringAfterLast('/'),
                    path = target,
                    isDirectory = it.isDirectory,
                    size = if (it.isDirectory) 0L else it.size.coerceAtLeast(0L),
                    lastModified = it.time.takeIf { time -> time > 0L } ?: 0L
                )
            }
        }
    }
}
