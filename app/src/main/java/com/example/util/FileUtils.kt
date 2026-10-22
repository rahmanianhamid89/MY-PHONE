package com.example.util

import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.provider.Settings
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.model.FileCategory
import com.example.model.OpenWithApp
import com.example.model.FileItem
import com.example.model.StorageStats
import com.example.model.StorageVolumeInfo
import android.os.storage.StorageManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.IOException
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import net.lingala.zip4j.ZipFile

object FileUtils {

    private val imageExtensions = setOf("jpg", "jpeg", "png", "webp", "gif", "bmp", "svg", "heic")
    private val videoExtensions = setOf("mp4", "mkv", "webm", "avi", "3gp", "mov", "flv", "ts")
    private val audioExtensions = setOf("mp3", "wav", "ogg", "m4a", "flac", "aac", "opus", "amr")
    private val documentExtensions = setOf(
        "pdf", "doc", "docx", "txt", "log", "md", "json", "xml", "csv",
        "html", "xls", "xlsx", "ppt", "pptx", "epub", "kt", "java", "py", "js"
    )
    private val textExtensions = setOf(
        "txt", "log", "md", "json", "xml", "csv", "html", "htm",
        "kt", "java", "py", "js", "ts", "css", "ini", "properties", "gradle", "sh", "yaml", "yml"
    )
    private val apkExtensions = setOf("apk", "xapk", "apks")
    private val archiveExtensions = setOf("zip", "rar", "7z", "tar", "gz", "bz2", "xz")

    fun getCategory(file: File): FileCategory {
        if (file.isDirectory) return FileCategory.FOLDER
        val ext = file.extension.lowercase(Locale.ROOT)
        return when {
            imageExtensions.contains(ext) -> FileCategory.IMAGE
            videoExtensions.contains(ext) -> FileCategory.VIDEO
            audioExtensions.contains(ext) -> FileCategory.AUDIO
            documentExtensions.contains(ext) -> FileCategory.DOCUMENT
            apkExtensions.contains(ext) -> FileCategory.APK
            archiveExtensions.contains(ext) -> FileCategory.ARCHIVE
            else -> FileCategory.OTHER
        }
    }

    fun isTextFile(file: File): Boolean {
        if (file.isDirectory) return false
        val ext = file.extension.lowercase(Locale.ROOT)
        return textExtensions.contains(ext)
    }

    fun isImageFile(file: File): Boolean {
        if (file.isDirectory) return false
        val ext = file.extension.lowercase(Locale.ROOT)
        return imageExtensions.contains(ext)
    }

    fun isZipFile(file: File): Boolean {
        if (file.isDirectory) return false
        return file.extension.equals("zip", ignoreCase = true)
    }

    fun isPdfFile(file: File): Boolean {
        if (file.isDirectory) return false
        return file.extension.equals("pdf", ignoreCase = true)
    }

    fun isVideoFile(file: File): Boolean {
        if (file.isDirectory) return false
        val ext = file.extension.lowercase(Locale.ROOT)
        return videoExtensions.contains(ext)
    }

    fun isApkFile(file: File): Boolean {
        if (file.isDirectory) return false
        return file.extension.equals("apk", ignoreCase = true)
    }

    fun formatBytes(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
        val index = digitGroups.coerceIn(0, units.size - 1)
        val value = bytes / Math.pow(1024.0, index.toDouble())
        return String.format(Locale.US, "%.1f %s", value, units[index])
    }

    fun formatDate(timestamp: Long): String {
        if (timestamp <= 0) return "-"
        val sdf = SimpleDateFormat("yyyy/MM/dd - HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun toFileItem(file: File, calculateChildCount: Boolean = true): FileItem {
        val isDir = file.isDirectory
        val ext = if (isDir) "" else file.extension
        val childCount = if (isDir && calculateChildCount) {
            try {
                file.list()?.size ?: 0
            } catch (e: Exception) {
                0
            }
        } else null

        val size = if (isDir) 0L else file.length()
          val formattedSize = if (isDir) {
              ""
          } else {
              formatBytes(size)
          }

        return FileItem(
            file = file,
            name = file.name,
            path = file.absolutePath,
            size = size,
            formattedSize = formattedSize,
            lastModified = file.lastModified(),
            formattedDate = formatDate(file.lastModified()),
            isDirectory = isDir,
            extension = ext,
            childCount = childCount,
            category = getCategory(file),
            canRead = file.canRead(),
            canWrite = file.canWrite(),
            isHidden = file.name.startsWith(".")
        )
    }

    fun getStorageStats(file: File): StorageStats {
        return try {
            val stat = StatFs(file.path)
            val blockSize = stat.blockSizeLong
            val totalBlocks = stat.blockCountLong
            val availableBlocks = stat.availableBlocksLong

            val totalBytes = totalBlocks * blockSize
            val freeBytes = availableBlocks * blockSize
            val usedBytes = (totalBytes - freeBytes).coerceAtLeast(0L)
            val percent = if (totalBytes > 0) (usedBytes.toFloat() / totalBytes.toFloat()) else 0f

            StorageStats(
                totalBytes = totalBytes,
                freeBytes = freeBytes,
                usedBytes = usedBytes,
                usedPercent = percent,
                formattedUsed = formatBytes(usedBytes),
                formattedTotal = formatBytes(totalBytes),
                formattedFree = formatBytes(freeBytes)
            )
        } catch (e: Exception) {
            StorageStats()
        }
    }

    fun getStorageStats(context: Context): StorageStats {
        return getStorageStats(getPrimaryStorageRoot(context))
    }

    fun getAllStorageVolumes(context: Context): List<StorageVolumeInfo> {
        val list = mutableListOf<StorageVolumeInfo>()
        val primaryRoot = getPrimaryStorageRoot(context)
        val primaryStats = getStorageStats(primaryRoot)

        // Always show Internal Storage
        list.add(
            StorageVolumeInfo(
                name = "Internal Storage",
                path = primaryRoot.absolutePath,
                file = primaryRoot,
                isRemovable = false,
                isUsbOtg = false,
                stats = primaryStats,
                isMounted = true
            )
        )

        try {
            val storageManager =
                context.getSystemService(Context.STORAGE_SERVICE) as StorageManager

            var usbIndex = 0

            for (volume in storageManager.storageVolumes) {
                if (volume.isPrimary || !volume.isRemovable) continue

                val directory = volume.directory ?: continue
                if (!directory.exists() || !directory.canRead()) continue
                if (directory.absolutePath == primaryRoot.absolutePath) continue

                val description =
                    volume.getDescription(context).lowercase(Locale.ROOT)

                val path = directory.absolutePath
                val lowerPath = path.lowercase(Locale.ROOT)

                // Keep SD cards hidden, but do not require "usb"/"otg"
                // in the description or mount path.
                val isSdCard = description.contains("sd card") ||
                        description.contains("sdcard") ||
                        description.contains("memory card")

                if (isSdCard) continue

                if (list.any { it.path == path }) continue

                usbIndex++

                list.add(
                    StorageVolumeInfo(
                        name = "USB OTG",
                        path = path,
                        file = directory,
                        isRemovable = true,
                        isUsbOtg = true,
                        stats = getStorageStats(directory),
                        isMounted = true
                    )
                )
            }
        } catch (_: Exception) {
        }

        return list
    }

    private fun getRootOfVolume(file: File): File? {
        var current: File? = file
        while (current != null && current.parentFile != null) {
            val parent = current.parentFile
            if (parent != null && (parent.absolutePath == "/storage" || parent.absolutePath == "/mnt" || parent.absolutePath == "/mnt/media_rw")) {
                return current
            }
            current = parent
        }
        return null
    }

    private fun isUsbPath(path: String): Boolean {
        val lower = path.lowercase(Locale.ROOT)
        return lower.contains("usb") || lower.contains("otg") || lower.contains("media_rw") || lower.contains("sda") || lower.contains("sdb")
    }

    fun getPrimaryStorageRoot(context: Context): File {
        val ext = Environment.getExternalStorageDirectory()
        return if (ext != null && ext.exists() && ext.canRead()) {
            ext
        } else {
            context.getExternalFilesDir(null) ?: context.filesDir
        }
    }

    suspend fun calculateMd5(file: File): String = withContext(Dispatchers.IO) {
        try {
            val digest = MessageDigest.getInstance("MD5")
            val buffer = ByteArray(8192)
            FileInputStream(file).use { fis ->
                var read: Int
                while (fis.read(buffer).also { read = it } > 0) {
                    digest.update(buffer, 0, read)
                }
            }
            val md5Bytes = digest.digest()
            val sb = java.lang.StringBuilder()
            for (b in md5Bytes) {
                sb.append(String.format("%02x", b))
            }
            sb.toString()
        } catch (e: Exception) {
            "Error calculating hash: ${e.message}"
        }
    }

    suspend fun calculateDirectoryStats(directory: File): Pair<Long, Int> = withContext(Dispatchers.IO) {
        var totalSize = 0L
        var fileCount = 0
        fun traverse(dir: File) {
            dir.listFiles()?.forEach { f ->
                if (f.isDirectory) {
                    traverse(f)
                } else {
                    totalSize += f.length()
                    fileCount++
                }
            }
        }
        traverse(directory)
        Pair(totalSize, fileCount)
    }

    suspend fun copyFileOrDir(source: File, targetDir: File): Boolean = withContext(Dispatchers.IO) {
        try {
            val dest = File(targetDir, source.name)
            if (source.isDirectory) {
                if (!dest.exists()) dest.mkdirs()
                source.listFiles()?.forEach { child ->
                    copyFileOrDir(child, dest)
                }
            } else {
                source.inputStream().use { input ->
                    dest.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

            }
            true
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            false
        }
    }

    suspend fun moveFileOrDir(source: File, targetDir: File): Boolean = withContext(Dispatchers.IO) {
        try {
            val dest = File(targetDir, source.name)
            if (source.renameTo(dest)) {
                true
            } else {
                val copied = copyFileOrDir(source, targetDir)
                if (copied) {
                    deleteRecursively(source)
                    true
                } else {
                    false
                }
            }
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteRecursively(file: File): Boolean = withContext(Dispatchers.IO) {
        try {
            file.deleteRecursively()
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteRecursivelyWithProgress(
        files: List<File>,
        onProgress: (Float) -> Unit
    ): Int = withContext(Dispatchers.IO) {
        suspend fun countEntries(file: File): Long {
            currentCoroutineContext().ensureActive()
            if (!file.exists()) return 0L

            var count = 1L
            if (file.isDirectory) {
                file.listFiles()?.forEach { child ->
                    count += countEntries(child)
                }
            }
            return count
        }

        var totalEntries = 0L
        files.forEach { file ->
            totalEntries += countEntries(file)
        }

        if (totalEntries == 0L) {
            onProgress(1f)
            return@withContext 0
        }

        var processedEntries = 0L

        suspend fun deleteEntry(file: File): Boolean {
            currentCoroutineContext().ensureActive()
            if (!file.exists()) {
                processedEntries++
                onProgress(
                    (processedEntries.toFloat() / totalEntries).coerceIn(0f, 1f)
                )
                return true
            }

            var success = true
            if (file.isDirectory) {
                val children = file.listFiles()
                if (children == null) {
                    success = false
                } else {
                    for (child in children) {
                        if (!deleteEntry(child)) success = false
                    }
                }
            }

            if (success || !file.exists()) {
                if (file.delete()) {
                    processedEntries++
                    onProgress(
                        (processedEntries.toFloat() / totalEntries).coerceIn(0f, 1f)
                    )
                    return true
                }
                success = false
            }

            if (!success && file.exists()) {
                processedEntries++
                onProgress(
                    (processedEntries.toFloat() / totalEntries).coerceIn(0f, 1f)
                )
            }
            return false
        }

        var deletedCount = 0
        try {
            for (file in files) {
                currentCoroutineContext().ensureActive()
                if (deleteEntry(file)) deletedCount++
            }
            onProgress(1f)
            deletedCount
        } catch (e: CancellationException) {
            throw e
        }
    }

    suspend fun zipFiles(
        files: List<File>,
        destinationZip: File,
        onProgress: (fileName: String, transferredBytes: Long, totalBytes: Long) -> Unit = { _, _, _ -> }
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            fun totalSize(file: File): Long {
                return if (file.isFile) {
                    file.length()
                } else {
                    file.listFiles()?.sumOf { totalSize(it) } ?: 0L
                }
            }

            val totalBytes = files.sumOf { totalSize(it) }
            var transferredBytes = 0L

            onProgress("", 0L, totalBytes)

            ZipOutputStream(
                BufferedOutputStream(FileOutputStream(destinationZip))
            ).use { zos ->
                for (file in files) {
                    currentCoroutineContext().ensureActive()
                    transferredBytes = addFileToZip(
                        file,
                        "",
                        zos,
                        transferredBytes,
                        totalBytes,
                        onProgress
                    )
                }
            }

            onProgress("", totalBytes, totalBytes)
            true
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun addFileToZip(
        file: File,
        parentPath: String,
        zos: ZipOutputStream,
        transferredBytes: Long,
        totalBytes: Long,
        onProgress: (fileName: String, transferredBytes: Long, totalBytes: Long) -> Unit
    ): Long {
        val entryName = if (parentPath.isEmpty()) {
            file.name
        } else {
            "$parentPath/${file.name}"
        }

        if (file.isDirectory) {
            val dirEntry = ZipEntry("$entryName/")
            zos.putNextEntry(dirEntry)
            zos.closeEntry()

            var currentBytes = transferredBytes

            file.listFiles()?.forEach { child ->
                currentCoroutineContext().ensureActive()
                currentBytes = addFileToZip(
                    child,
                    entryName,
                    zos,
                    currentBytes,
                    totalBytes,
                    onProgress
                )
            }

            return currentBytes
        }

        val entry = ZipEntry(entryName)
        zos.putNextEntry(entry)

        var currentBytes = transferredBytes
        var lastProgressTime = System.currentTimeMillis()
        val buffer = ByteArray(64 * 1024)

        BufferedInputStream(FileInputStream(file)).use { input ->
            while (true) {
                currentCoroutineContext().ensureActive()
                val bytesRead = input.read(buffer)
                if (bytesRead <= 0) break

                zos.write(buffer, 0, bytesRead)
                currentBytes += bytesRead

                val now = System.currentTimeMillis()
                if (now - lastProgressTime >= 150L) {
                    onProgress(
                        file.name,
                        currentBytes,
                        totalBytes
                    )
                    lastProgressTime = now
                }
            }
        }

        onProgress(
            file.name,
            currentBytes,
            totalBytes
        )

        zos.closeEntry()
        return currentBytes
    }

    suspend fun unzipArchive(zipFile: File, outputDirectory: File): Boolean =
        withContext(Dispatchers.IO) {
            try {
                if (!zipFile.exists() || !zipFile.isFile) {
                    return@withContext false
                }

                if (!outputDirectory.exists() && !outputDirectory.mkdirs()) {
                    return@withContext false
                }

                val outputCanonical = outputDirectory.canonicalFile

                ZipInputStream(
                    BufferedInputStream(FileInputStream(zipFile))
                ).use { zis ->

                    val buffer = ByteArray(64 * 1024)
                    var entry = zis.nextEntry

                    while (entry != null) {
                        val entryName = entry!!.name

                        val newFile = File(outputCanonical, entryName)
                        val newFileCanonical = newFile.canonicalFile

                        if (newFileCanonical != outputCanonical &&
                            !newFileCanonical.path.startsWith(
                                outputCanonical.path + File.separator
                            )
                        ) {
                            throw SecurityException("Zip Slip detected: $entryName")
                        }

                        if (entry!!.isDirectory) {
                            if (!newFileCanonical.exists() &&
                                !newFileCanonical.mkdirs()
                            ) {
                                throw IOException(
                                    "Failed to create directory: ${newFileCanonical.path}"
                                )
                            }
                        } else {
                            newFileCanonical.parentFile?.let { parent ->
                                if (!parent.exists() && !parent.mkdirs()) {
                                    throw IOException(
                                        "Failed to create directory: ${parent.path}"
                                    )
                                }
                            }

                            FileOutputStream(newFileCanonical).use { fos ->
                                while (true) {
                                    val len = zis.read(buffer)
                                    if (len <= 0) break
                                    fos.write(buffer, 0, len)
                                }
                                fos.flush()
                            }
                        }

                        zis.closeEntry()
                        entry = zis.nextEntry
                    }
                }

                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }

    suspend fun unzipArchiveWithPassword(
        zipFile: File,
        outputDirectory: File,
        password: String,
        onProgress: (transferredBytes: Long, totalBytes: Long) -> Unit = { _, _ -> }
    ): Boolean =
        withContext(Dispatchers.IO) {
            try {
                if (!zipFile.exists() || !zipFile.isFile) {
                    return@withContext false
                }

                if (!outputDirectory.exists() && !outputDirectory.mkdirs()) {
                    return@withContext false
                }

                val encryptedZip = ZipFile(zipFile)
                encryptedZip.setPassword(password.toCharArray())

                onProgress(0L, zipFile.length())
                encryptedZip.extractAll(outputDirectory.absolutePath)
                onProgress(zipFile.length(), zipFile.length())

                true
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }

    suspend fun readText(file: File, maxLength: Int = 1_000_000): String = withContext(Dispatchers.IO) {
        try {
            if (file.length() > maxLength) {
                file.bufferedReader().use { reader ->
                    val chars = CharArray(maxLength)
                    val read = reader.read(chars, 0, maxLength)
                    String(chars, 0, read) + "\n\n... [File too large for full preview] ..."
                }
            } else {
                file.readText()
            }
        } catch (e: Exception) {
            "Error reading text file: ${e.message}"
        }
    }

    suspend fun writeText(file: File, content: String): Boolean = withContext(Dispatchers.IO) {
        try {
            file.writeText(content)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getMimeType(file: File): String {
        val ext = file.extension.lowercase(Locale.ROOT)
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext)
            ?: when (ext) {
                "apk" -> "application/vnd.android.package-archive"
                "pdf" -> "application/pdf"
                "txt", "log", "md", "json", "xml" -> "text/plain"
                "zip" -> "application/zip"
                else -> "*/*"
            }
    }

    fun getOpenWithApps(
        context: Context,
        file: File
    ): List<OpenWithApp> {
        val mimeType = getMimeType(file)
        if (mimeType == "*/*") return emptyList()

        val intent = Intent(Intent.ACTION_VIEW).apply {
            type = mimeType
            addCategory(Intent.CATEGORY_DEFAULT)
        }

        return context.packageManager
            .queryIntentActivities(intent, 0)
            .map { resolveInfo ->
                OpenWithApp(
                    packageName = resolveInfo.activityInfo.packageName,
                    activityName = resolveInfo.activityInfo.name,
                    label = resolveInfo.loadLabel(context.packageManager).toString(),
                    icon = resolveInfo.loadIcon(context.packageManager)
                )
            }
            .distinctBy { "${it.packageName}/${it.activityName}" }
            .sortedBy { it.label.lowercase(Locale.ROOT) }
    }

    fun openFileWithApp(
        context: Context,
        file: File,
        packageName: String,
        activityName: String
    ) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
            val mimeType = getMimeType(file)

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                component = android.content.ComponentName(packageName, activityName)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Unable to open file with selected app: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun openFile(context: Context, file: File) {
        try {
            val mimeType = getMimeType(file)
            val alwaysPackage = AlwaysOpenManager.getPackageName(context, mimeType)
            val alwaysActivity = AlwaysOpenManager.getActivityName(context, mimeType)

            if (
                !alwaysPackage.isNullOrBlank() &&
                !alwaysActivity.isNullOrBlank()
            ) {
                openFileWithApp(
                    context = context,
                    file = file,
                    packageName = alwaysPackage,
                    activityName = alwaysActivity
                )
                return
            }
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
            val ext = file.extension.lowercase(Locale.ROOT)

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            // If APK, check install permission on Android O+
            if (ext == "apk" && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!context.packageManager.canRequestPackageInstalls()) {
                    Toast.makeText(
                        context,
                        "Please grant permission to install unknown apps in Android Settings",
                        Toast.LENGTH_LONG
                    ).show()
                    val settingsIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                        data = Uri.parse("package:${context.packageName}")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(settingsIntent)
                    return
                }
            }

            if (ext == "apk") {
                context.startActivity(intent)
            } else {
                context.startActivity(Intent.createChooser(intent, "Open file with:").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
            }
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "No application found to open this file: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun shareFile(context: Context, file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
            val ext = file.extension.lowercase(Locale.ROOT)
            val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext) ?: "*/*"

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Share file").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (e: Exception) {
            Toast.makeText(context, "Share error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareMultipleFiles(context: Context, files: List<File>) {
        try {
            val uris = ArrayList<Uri>()
            for (f in files) {
                if (!f.isDirectory) {
                    uris.add(FileProvider.getUriForFile(context, "${context.packageName}.provider", f))
                }
            }
            if (uris.isEmpty()) {
                Toast.makeText(context, "No files selected to share", Toast.LENGTH_SHORT).show()
                return
            }
            val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                type = "*/*"
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Share multiple files").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (e: Exception) {
            Toast.makeText(context, "Share error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    suspend fun createSampleFilesIfEmpty(root: File) = withContext(Dispatchers.IO) {
        try {
            val documentsDir = File(root, "Documents")
            if (!documentsDir.exists()) documentsDir.mkdirs()

            val welcomeFile = File(documentsDir, "Getting_Started.txt")
            if (!welcomeFile.exists()) {
                welcomeFile.writeText(
                    """
                    Welcome to File Manager Pro!
                    ---------------------------------------
                    Features:
                    1. Fast browsing and search across files and directories
                    2. Categorized view: Images, Videos, Audio, Documents, APKs, Archives
                    3. Built-in Text Editor with live editing and save
                    4. Built-in Video Player and PDF Viewer
                    5. Full USB OTG and External Storage detection
                    6. Bookmarks for quick folder access
                    7. Compression (ZIP) and extraction
                    8. File details, MD5 checksum calculation, and batch operations
                    """.trimIndent()
                )
            }

            val samplePdfNote = File(documentsDir, "Sample_Document.txt")
            if (!samplePdfNote.exists()) {
                samplePdfNote.writeText(
                    """
                    File Manager Pro - Utility & Documents Notes:
                    This section lets you quickly access all your text documents, PDFs, and notes.
                    You can edit text files directly or open them with external tools.
                    """.trimIndent()
                )
            }

            val notesFile = File(documentsDir, "project_notes.json")
            if (!notesFile.exists()) {
                notesFile.writeText(
                    """
                    {
                      "appName": "File Manager Pro",
                      "version": "1.0.0",
                      "features": [
                        "USB OTG Storage",
                        "Video Player",
                        "PDF Viewer",
                        "Text Editor",
                        "Zip/Unzip",
                        "Cloud Integration"
                      ],
                      "nativeAndroid": true
                    }
                    """.trimIndent()
                )
            }

            val downloadDir = File(root, "Download")
            if (!downloadDir.exists()) downloadDir.mkdirs()

            val sampleApkNote = File(downloadDir, "About_APK_Install.txt")
            if (!sampleApkNote.exists()) {
                sampleApkNote.writeText(
                    """
                    To install apps with the .apk extension:
                    Tap on any APK file to trigger the Android installer directly.
                    Ensure 'Install unknown apps' permission is enabled in Android Settings.
                    """.trimIndent()
                )
            }
        } catch (_: Exception) {}
    }
}
