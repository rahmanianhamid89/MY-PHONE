package com.example.model
import com.example.ui.icons.LucideIcons

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
    ALL("All", "All", LucideIcons.Folder, ColorFolder),
    FOLDER("Folders", "Folders", LucideIcons.Folder, ColorFolder),
    IMAGE("Images", "Images", LucideIcons.Image, ColorImage),
    VIDEO("Videos", "Videos", LucideIcons.FileVideo, ColorVideo),
    AUDIO("Audio", "Audio", LucideIcons.FileAudio, ColorAudio),
    DOCUMENT("Documents", "Documents", LucideIcons.FileText, ColorDocument),
    APK("APKs", "APKs", LucideIcons.FileCode, ColorApk),
    ARCHIVE("Archives (ZIP)", "Archives", LucideIcons.FolderArchive, ColorArchive),
    OTHER("Other", "Other", LucideIcons.File, ColorOther)
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
    val isHidden: Boolean = false,
    val virtualZipArchive: File? = null,
    val virtualZipEntryPath: String? = null
)

enum class SortOption(val titleFa: String, val titleEn: String) {
    NAME_ASC("Name (A-Z)", "Name (A-Z)"),
    NAME_DESC("Name (Z-A)", "Name (Z-A)"),
    DATE_DESC("Newest first", "Newest first"),
    DATE_ASC("Oldest first", "Oldest first"),
    SIZE_DESC("Largest size", "Largest size"),
    SIZE_ASC("Smallest size", "Smallest size"),
    TYPE_ASC("Type (A-Z)", "Type (A-Z)")
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
