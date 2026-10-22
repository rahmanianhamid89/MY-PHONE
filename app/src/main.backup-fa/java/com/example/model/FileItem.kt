package com.example.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.ui.theme.ColorApk
import com.example.ui.theme.ColorArchive
import com.example.ui.theme.ColorAudio
import com.example.ui.theme.ColorDocument
import com.example.ui.theme.ColorFolder
import com.example.ui.theme.ColorImage
import com.example.ui.theme.ColorOther
import com.example.ui.theme.ColorVideo
import java.io.File

enum class FileCategory(
    val titleFa: String,
    val titleEn: String,
    val icon: ImageVector,
    val color: Color
) {
    ALL("همه", "All", Icons.Default.Folder, ColorFolder),
    FOLDER("پوشه‌ها", "Folders", Icons.Default.Folder, ColorFolder),
    IMAGE("تصاویر", "Images", Icons.Default.Image, ColorImage),
    VIDEO("ویدیوها", "Videos", Icons.Default.VideoFile, ColorVideo),
    AUDIO("موزیک و صوت", "Audio", Icons.Default.AudioFile, ColorAudio),
    DOCUMENT("اسناد و متون", "Documents", Icons.Default.Description, ColorDocument),
    APK("فایل‌های نصبی", "APKs", Icons.Default.Android, ColorApk),
    ARCHIVE("فشرده (ZIP)", "Archives", Icons.Default.FolderZip, ColorArchive),
    OTHER("سایر", "Other", Icons.Default.InsertDriveFile, ColorOther)
}

data class FileItem(
    val file: File,
    val name: String,
    val path: String,
    val size: Long,
    val formattedSize: String,
    val lastModified: Long,
    val formattedDate: String,
    val isDirectory: Boolean,
    val extension: String,
    val childCount: Int? = null,
    val category: FileCategory,
    val canRead: Boolean = true,
    val canWrite: Boolean = true,
    val isHidden: Boolean = false
)

enum class SortOption(val titleFa: String, val titleEn: String) {
    NAME_ASC("نام (صعودی)", "Name (A-Z)"),
    NAME_DESC("نام (نزولی)", "Name (Z-A)"),
    DATE_DESC("جدیدترین", "Newest first"),
    DATE_ASC("قدیمی‌ترین", "Oldest first"),
    SIZE_DESC("بزرگ‌ترین حجم", "Largest size"),
    SIZE_ASC("کوچک‌ترین حجم", "Smallest size")
}

enum class ViewMode {
    LIST,
    GRID
}

data class StorageStats(
    val totalBytes: Long = 0L,
    val freeBytes: Long = 0L,
    val usedBytes: Long = 0L,
    val usedPercent: Float = 0f,
    val formattedUsed: String = "",
    val formattedTotal: String = "",
    val formattedFree: String = ""
)

data class StorageVolumeInfo(
    val name: String,
    val path: String,
    val file: File,
    val isRemovable: Boolean,
    val isUsbOtg: Boolean,
    val stats: StorageStats,
    val isMounted: Boolean = true
)

data class BookmarkItem(
    val name: String,
    val path: String,
    val file: File,
    val isDirectory: Boolean = true,
    val iconType: String = "FOLDER"
)

data class ClipboardState(
    val items: List<File> = emptyList(),
    val isCut: Boolean = false
) {
    val hasItems: Boolean get() = items.isNotEmpty()
}
