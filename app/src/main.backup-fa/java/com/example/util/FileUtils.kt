package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.provider.Settings
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.model.FileCategory
import com.example.model.FileItem
import com.example.model.StorageStats
import com.example.model.StorageVolumeInfo
import android.os.storage.StorageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

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
        val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun toFileItem(file: File): FileItem {
        val isDir = file.isDirectory
        val ext = if (isDir) "" else file.extension
        val childCount = if (isDir) {
            try {
                file.list()?.size ?: 0
            } catch (e: Exception) {
                0
            }
        } else null

        val size = if (isDir) 0L else file.length()
        val formattedSize = if (isDir) {
            val count = childCount ?: 0
            if (count == 1) "1 item" else "$count items"
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
            // Check secondary storage devices via Context
            val extDirs = context.getExternalFilesDirs(null)
            for (dir in extDirs) {
                if (dir != null) {
                    val root = getRootOfVolume(dir)
                    if (root != null && root.absolutePath != primaryRoot.absolutePath) {
                        val isUsb = isUsbPath(root.absolutePath)
                        val name = if (isUsb) "USB OTG Storage" else "SD Card Storage"
                        val stats = getStorageStats(root)
                        // Don't add duplicate paths
                        if (list.none { it.path == root.absolutePath }) {
                            list.add(
                                StorageVolumeInfo(
                                    name = name,
                                    path = root.absolutePath,
                                    file = root,
                                    isRemovable = true,
                                    isUsbOtg = isUsb,
                                    stats = stats,
                                    isMounted = root.canRead()
                                )
                            )
                        }
                    }
                }
            }

            // Also check /mnt/media_rw, /storage, /mnt/usb
            val candidateDirs = listOf(
                File("/storage"),
                File("/mnt"),
                File("/mnt/usb"),
                File("/mnt/media_rw")
            )
            for (candidate in candidateDirs) {
                if (candidate.exists() && candidate.isDirectory) {
                    candidate.listFiles()?.forEach { sub ->
                        val subPath = sub.absolutePath
                        val isSelf = subPath == primaryRoot.absolutePath ||
                                subPath.contains("self") ||
                                subPath.contains("emulated")
                        if (!isSelf && sub.canRead() && list.none { it.path == subPath }) {
                            val isUsb = isUsbPath(subPath)
                            val name = if (isUsb) "USB OTG Drive (${sub.name})" else "External Storage (${sub.name})"
                            val stats = getStorageStats(sub)
                            list.add(
                                StorageVolumeInfo(
                                    name = name,
                                    path = subPath,
                                    file = sub,
                                    isRemovable = true,
                                    isUsbOtg = isUsb,
                                    stats = stats,
                                    isMounted = true
                                )
                            )
                        }
                    }
                }
            }
        } catch (_: Exception) {}

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

    suspend fun zipFiles(files: List<File>, destinationZip: File): Boolean = withContext(Dispatchers.IO) {
        try {
            ZipOutputStream(BufferedOutputStream(FileOutputStream(destinationZip))).use { zos ->
                for (file in files) {
                    addFileToZip(file, "", zos)
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun addFileToZip(file: File, parentPath: String, zos: ZipOutputStream) {
        val entryName = if (parentPath.isEmpty()) file.name else "$parentPath/${file.name}"
        if (file.isDirectory) {
            val dirEntry = ZipEntry("$entryName/")
            zos.putNextEntry(dirEntry)
            zos.closeEntry()
            file.listFiles()?.forEach { child ->
                addFileToZip(child, entryName, zos)
            }
        } else {
            val entry = ZipEntry(entryName)
            zos.putNextEntry(entry)
            BufferedInputStream(FileInputStream(file)).use { bis ->
                bis.copyTo(zos)
            }
            zos.closeEntry()
        }
    }

    suspend fun unzipArchive(zipFile: File, outputDirectory: File): Boolean = withContext(Dispatchers.IO) {
        try {
            if (!outputDirectory.exists()) {
                outputDirectory.mkdirs()
            }
            ZipInputStream(BufferedInputStream(FileInputStream(zipFile))).use { zis ->
                var entry: ZipEntry? = zis.nextEntry
                val buffer = ByteArray(8192)
                while (entry != null) {
                    val newFile = File(outputDirectory, entry.name)
                    // Security check against zip slip
                    if (!newFile.canonicalPath.startsWith(outputDirectory.canonicalPath)) {
                        throw SecurityException("Zip Slip detected: ${entry.name}")
                    }
                    if (entry.isDirectory) {
                        newFile.mkdirs()
                    } else {
                        newFile.parentFile?.mkdirs()
                        FileOutputStream(newFile).use { fos ->
                            var len: Int
                            while (zis.read(buffer).also { len = it } > 0) {
                                fos.write(buffer, 0, len)
                            }
                        }
                    }
                    zis.closeEntry()
                    entry = zis.nextEntry
                }
            }
            true
        } catch (e: Exception) {
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

    fun openFile(context: Context, file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
            val ext = file.extension.lowercase(Locale.ROOT)
            val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext)
                ?: when (ext) {
                    "apk" -> "application/vnd.android.package-archive"
                    "pdf" -> "application/pdf"
                    "txt", "log", "md", "json", "xml" -> "text/plain"
                    "zip" -> "application/zip"
                    else -> "*/*"
                }

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

            context.startActivity(Intent.createChooser(intent, "Open file with:").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
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
