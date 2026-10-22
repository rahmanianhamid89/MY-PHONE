package com.example.viewmodel

import android.app.Application
import android.content.ComponentName
import android.content.Intent
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import android.os.Build
import android.os.Environment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.BookmarkItem
import com.example.model.ClipboardState
import com.example.model.FileCategory
import com.example.model.FileItem
import com.example.model.SortOption
import com.example.model.StorageStats
import com.example.model.StorageVolumeInfo
import com.example.model.TransferProgressState
import com.example.util.FileConflictAction
import com.example.model.ViewMode
import com.example.util.FileTransferManager
import com.example.util.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

class FileManagerViewModel(application: Application) : AndroidViewModel(application) {

    private val context: Context get() = getApplication<Application>().applicationContext

    private val rootDirectory: File = FileUtils.getPrimaryStorageRoot(context)

    private val _currentDirectory = MutableStateFlow<File>(rootDirectory)
    val currentDirectory: StateFlow<File> = _currentDirectory.asStateFlow()

    private val _breadcrumbs = MutableStateFlow<List<File>>(listOf(rootDirectory))
    val breadcrumbs: StateFlow<List<File>> = _breadcrumbs.asStateFlow()

    private val _files = MutableStateFlow<List<FileItem>>(emptyList())
    val files: StateFlow<List<FileItem>> = _files.asStateFlow()

    private val _isLoading = MutableStateFlow<Boolean>(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _searchQuery = MutableStateFlow<String>("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<FileCategory?>(null)
    val selectedCategory: StateFlow<FileCategory?> = _selectedCategory.asStateFlow()

    private val _sortOption = MutableStateFlow<SortOption>(SortOption.NAME_ASC)
    val sortOption: StateFlow<SortOption> = _sortOption.asStateFlow()

    private val _viewMode = MutableStateFlow<ViewMode>(ViewMode.LIST)
    val viewMode: StateFlow<ViewMode> = _viewMode.asStateFlow()

    private val _showHiddenFiles = MutableStateFlow<Boolean>(false)
    val showHiddenFiles: StateFlow<Boolean> = _showHiddenFiles.asStateFlow()

    private val _selectedPaths = MutableStateFlow<Set<String>>(emptySet())
    val selectedPaths: StateFlow<Set<String>> = _selectedPaths.asStateFlow()

    private val _clipboard = MutableStateFlow<ClipboardState>(ClipboardState())
    val clipboard: StateFlow<ClipboardState> = _clipboard.asStateFlow()

    private val _storageStats = MutableStateFlow<StorageStats>(StorageStats())
    val storageStats: StateFlow<StorageStats> = _storageStats.asStateFlow()

    private val _storageVolumes = MutableStateFlow<List<StorageVolumeInfo>>(emptyList())
    val storageVolumes: StateFlow<List<StorageVolumeInfo>> = _storageVolumes.asStateFlow()

    private val _bookmarks = MutableStateFlow<List<BookmarkItem>>(emptyList())
    val bookmarks: StateFlow<List<BookmarkItem>> = _bookmarks.asStateFlow()

    private val _isBrowsingFiles = MutableStateFlow<Boolean>(false)
    val isBrowsingFiles: StateFlow<Boolean> = _isBrowsingFiles.asStateFlow()

    private val _hasStoragePermission = MutableStateFlow<Boolean>(true)
    val hasStoragePermission: StateFlow<Boolean> = _hasStoragePermission.asStateFlow()

    // Dialog state holders
    private val _showCreateFolderDialog = MutableStateFlow<Boolean>(false)
    val showCreateFolderDialog: StateFlow<Boolean> = _showCreateFolderDialog.asStateFlow()

    private val _showCreateFileDialog = MutableStateFlow<Boolean>(false)
    val showCreateFileDialog: StateFlow<Boolean> = _showCreateFileDialog.asStateFlow()

    private val _fileToRename = MutableStateFlow<FileItem?>(null)
    val fileToRename: StateFlow<FileItem?> = _fileToRename.asStateFlow()

    private val _filesToDelete = MutableStateFlow<List<FileItem>?>(null)
    val filesToDelete: StateFlow<List<FileItem>?> = _filesToDelete.asStateFlow()

    private val _fileForDetails = MutableStateFlow<FileItem?>(null)
    val fileForDetails: StateFlow<FileItem?> = _fileForDetails.asStateFlow()

    private val _fileForTextEdit = MutableStateFlow<File?>(null)
    val fileForTextEdit: StateFlow<File?> = _fileForTextEdit.asStateFlow()

    private val _textEditContent = MutableStateFlow<String>("")
    val textEditContent: StateFlow<String> = _textEditContent.asStateFlow()

    private val _fileForImagePreview = MutableStateFlow<File?>(null)
    val fileForImagePreview: StateFlow<File?> = _fileForImagePreview.asStateFlow()

    private val _fileForVideoPlayer = MutableStateFlow<File?>(null)
    val fileForVideoPlayer: StateFlow<File?> = _fileForVideoPlayer.asStateFlow()

    private val _fileForPdfViewer = MutableStateFlow<File?>(null)
    val fileForPdfViewer: StateFlow<File?> = _fileForPdfViewer.asStateFlow()

    private val _filesToZip = MutableStateFlow<List<File>?>(null)
    val filesToZip: StateFlow<List<File>?> = _filesToZip.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private val _fileConflict = MutableStateFlow<Pair<File, File>?>(null)
    val fileConflict: StateFlow<Pair<File, File>?> = _fileConflict.asStateFlow()

    private var conflictResolver: ((FileConflictAction) -> Unit)? = null

    // Windows 11 Transfer Progress Manager
    private val transferManager = FileTransferManager()
    val transferState: StateFlow<TransferProgressState> = transferManager.transferState

    fun toggleTransferPause() {
        if (transferManager.transferState.value.isPaused) {
            transferManager.resume()
        } else {
            transferManager.pause()
        }
    }

    fun cancelTransfer() {
        transferManager.cancel()
        _statusMessage.value = "انتقال فایل‌ها لغو شد"
        loadCurrentDirectory()
    }

    fun dismissTransferDialog() {
        transferManager.dismiss()
    }

    init {
        checkPermission()
        viewModelScope.launch {
            FileUtils.createSampleFilesIfEmpty(rootDirectory)
            loadDefaultBookmarks()
            refreshStorageStats()
            loadCurrentDirectory()
        }
    }

    private fun loadDefaultBookmarks() {
        val list = mutableListOf<BookmarkItem>()
        val downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val documents = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        val pictures = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val movies = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
        val music = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
        val dcim = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)

        val candidates = listOf(
            Triple("Downloads", downloads, "DOWNLOADS"),
            Triple("Documents", documents, "DOCUMENTS"),
            Triple("Pictures", pictures, "PICTURES"),
            Triple("DCIM / Camera", dcim, "PICTURES"),
            Triple("Movies", movies, "MOVIES"),
            Triple("Music", music, "MUSIC")
        )

        for ((name, dir, iconType) in candidates) {
            if (dir != null && dir.exists()) {
                list.add(BookmarkItem(name = name, path = dir.absolutePath, file = dir, isDirectory = true, iconType = iconType))
            }
        }

        // Fallback if none exist yet
        if (list.isEmpty()) {
            list.add(BookmarkItem(name = "Main Storage", path = rootDirectory.absolutePath, file = rootDirectory, isDirectory = true, iconType = "FOLDER"))
        }

        _bookmarks.value = list
    }

    fun addBookmark(file: File) {
        val current = _bookmarks.value.toMutableList()
        if (current.none { it.path == file.absolutePath }) {
            current.add(BookmarkItem(name = file.name, path = file.absolutePath, file = file, isDirectory = file.isDirectory))
            _bookmarks.value = current
            _statusMessage.value = "Bookmark added: ${file.name}"
        }
    }

    fun addCustomBookmark(name: String, path: String) {
        val file = File(path)
        val current = _bookmarks.value.toMutableList()
        if (current.none { it.path == path }) {
            current.add(
                BookmarkItem(
                    name = name.ifBlank { file.name.ifEmpty { "Bookmark" } },
                    path = path,
                    file = file,
                    isDirectory = file.isDirectory
                )
            )
            _bookmarks.value = current
            _statusMessage.value = "Bookmark added: $name"
        } else {
            _statusMessage.value = "Bookmark already exists"
        }
    }

    fun removeBookmark(path: String) {
        _bookmarks.value = _bookmarks.value.filter { it.path != path }
        _statusMessage.value = "Bookmark removed"
    }

    fun removeBookmark(bookmark: BookmarkItem) {
        removeBookmark(bookmark.path)
    }

    fun loadStorageVolumes() {
        refreshStorageStats()
    }

    fun checkPermission() {
        val granted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
        _hasStoragePermission.value = granted
    }

    fun refreshStorageStats() {
        viewModelScope.launch {
            val stats = withContext(Dispatchers.IO) {
                FileUtils.getStorageStats(context)
            }
            _storageStats.value = stats
            val volumes = withContext(Dispatchers.IO) {
                FileUtils.getAllStorageVolumes(context)
            }
            _storageVolumes.value = volumes
        }
    }

    fun openStorageVolume(volume: StorageVolumeInfo) {
        navigateTo(volume.file)
        _isBrowsingFiles.value = true
    }

    fun openBookmark(bookmark: BookmarkItem) {
        if (bookmark.isDirectory) {
            navigateTo(bookmark.file)
            _isBrowsingFiles.value = true
        } else {
            openFileItem(FileUtils.toFileItem(bookmark.file), context)
        }
    }

    fun exitFileBrowser(): Boolean {
        if (_isBrowsingFiles.value) {
            val parentResult = navigateToParent()
            if (!parentResult) {
                _isBrowsingFiles.value = false
            }
            return true
        }
        return false
    }

    fun returnToHome() {
        _isBrowsingFiles.value = false
        _selectedCategory.value = null
        _selectedPaths.value = emptySet()
        _searchQuery.value = ""
    }

    fun loadCurrentDirectory() {
        viewModelScope.launch {
            _isLoading.value = true
            val dir = _currentDirectory.value
            val category = _selectedCategory.value

            val items = withContext(Dispatchers.IO) {
                if (category != null && category != FileCategory.ALL) {
                    // Collect items of specific category
                    findFilesByCategory(rootDirectory, category)
                } else {
                    val rawFiles = dir.listFiles()?.toList() ?: emptyList()
                    android.util.Log.d(
                        "FileManagerDebug",
                        "DIR=${dir.absolutePath} rawFiles=${rawFiles.size} files=${rawFiles.count { !it.isDirectory }} dirs=${rawFiles.count { it.isDirectory }}"
                    )
                    rawFiles.map { FileUtils.toFileItem(it) }
                }
            }

            _files.value = sortAndFilter(items)
            android.util.Log.d(
                "FileManagerDebug",
                "FINAL dir=${dir.absolutePath} items=${items.size} final=${_files.value.size} " +
                    "files=${_files.value.count { !it.isDirectory }} dirs=${_files.value.count { it.isDirectory }}"
            )
            updateBreadcrumbs(dir)
            _isLoading.value = false
        }
    }

    private fun findFilesByCategory(root: File, category: FileCategory): List<FileItem> {
        val result = mutableListOf<FileItem>()
        fun scan(file: File, depth: Int) {
            if (depth > 4 || result.size > 200) return
            val children = file.listFiles() ?: return
            for (child in children) {
                if (child.isDirectory) {
                    if (!child.name.startsWith(".")) {
                        scan(child, depth + 1)
                    }
                } else {
                    if (FileUtils.getCategory(child) == category) {
                        result.add(FileUtils.toFileItem(child))
                    }
                }
            }
        }
        scan(root, 0)
        return result
    }

    private fun updateBreadcrumbs(dir: File) {
        val list = mutableListOf<File>()
        var curr: File? = dir
        while (curr != null) {
            list.add(0, curr)
            if (curr == rootDirectory || curr.parentFile == null) break
            curr = curr.parentFile
        }
        _breadcrumbs.value = list
    }

    private fun sortAndFilter(raw: List<FileItem>): List<FileItem> {
        var list = raw

        // Filter hidden files
        if (!_showHiddenFiles.value) {
            list = list.filter { !it.isHidden }
        }

        // Filter query
        val q = _searchQuery.value.trim().lowercase(Locale.ROOT)
        if (q.isNotEmpty()) {
            list = list.filter { it.name.lowercase(Locale.ROOT).contains(q) }
        }

        // Sort: Always keep directories on top when sorting
        val comparator = when (_sortOption.value) {
            SortOption.NAME_ASC -> compareBy<FileItem>({ !it.isDirectory }, { it.name.lowercase() })
            SortOption.NAME_DESC -> compareBy<FileItem>({ !it.isDirectory }, { it.name.lowercase() }).reversed()
            SortOption.DATE_DESC -> compareByDescending<FileItem> { it.lastModified }
            SortOption.DATE_ASC -> compareBy<FileItem> { it.lastModified }
            SortOption.SIZE_DESC -> compareByDescending<FileItem> { it.size }
            SortOption.SIZE_ASC -> compareBy<FileItem> { it.size }
        }

        return list.sortedWith(comparator)
    }

    fun navigateTo(directory: File) {
        if (!directory.exists() || !directory.isDirectory) return
        _currentDirectory.value = directory
        _selectedCategory.value = null
        _selectedPaths.value = emptySet()
        _searchQuery.value = ""
        _isBrowsingFiles.value = true
        loadCurrentDirectory()
    }

    fun navigateToParent(): Boolean {
        val curr = _currentDirectory.value
        if (curr == rootDirectory || curr.parentFile == null) {
            return false
        }
        navigateTo(curr.parentFile!!)
        return true
    }

    fun setCategory(category: FileCategory?) {
        _selectedCategory.value = category
        _selectedPaths.value = emptySet()
        if (category != null) {
            _isBrowsingFiles.value = true
        }
        loadCurrentDirectory()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        if (query.isNotBlank()) {
            _isBrowsingFiles.value = true
        }
        loadCurrentDirectory()
    }

    fun setSortOption(sort: SortOption) {
        _sortOption.value = sort
        loadCurrentDirectory()
    }

    fun setViewMode(mode: ViewMode) {
        _viewMode.value = mode
    }

    fun toggleViewMode() {
        _viewMode.value = if (_viewMode.value == ViewMode.LIST) ViewMode.GRID else ViewMode.LIST
    }

    fun toggleShowHiddenFiles() {
        _showHiddenFiles.value = !_showHiddenFiles.value
        loadCurrentDirectory()
    }

    fun toggleSelectFile(fileItem: FileItem) {
        val current = _selectedPaths.value.toMutableSet()
        if (current.contains(fileItem.path)) {
            current.remove(fileItem.path)
        } else {
            current.add(fileItem.path)
        }
        _selectedPaths.value = current
    }

    fun selectAll() {
        _selectedPaths.value = _files.value.map { it.path }.toSet()
    }

    fun clearSelection() {
        _selectedPaths.value = emptySet()
    }

    fun copySelected() {
        val selectedFiles = _files.value.filter { _selectedPaths.value.contains(it.path) }.map { it.file }
        if (selectedFiles.isNotEmpty()) {
            _clipboard.value = ClipboardState(selectedFiles, isCut = false)
            _statusMessage.value = "${selectedFiles.size} items copied"
            clearSelection()
        }
    }

    fun cutSelected() {
        val selectedFiles = _files.value.filter { _selectedPaths.value.contains(it.path) }.map { it.file }
        if (selectedFiles.isNotEmpty()) {
            _clipboard.value = ClipboardState(selectedFiles, isCut = true)
            _statusMessage.value = "${selectedFiles.size} items cut"
            clearSelection()
        }
    }

    fun copyItem(fileItem: FileItem) {
        _clipboard.value = ClipboardState(listOf(fileItem.file), isCut = false)
        _statusMessage.value = "\"${fileItem.name}\" copied"
    }

    fun cutItem(fileItem: FileItem) {
        _clipboard.value = ClipboardState(listOf(fileItem.file), isCut = true)
        _statusMessage.value = "\"${fileItem.name}\" cut"
    }

    fun clearClipboard() {
        _clipboard.value = ClipboardState()
    }

    fun pasteClipboard() {
        val clip = _clipboard.value
        if (!clip.hasItems) return
        val targetDir = _currentDirectory.value

        viewModelScope.launch {
            transferManager.executeTransfer(
                items = clip.items,
                targetDir = targetDir,
                isCut = clip.isCut,
                onConflict = { source, destination ->
                    val result = CompletableDeferred<FileConflictAction>()
                    _fileConflict.value = source to destination
                    conflictResolver = { action ->
                        _fileConflict.value = null
                        conflictResolver = null
                        result.complete(action)
                    }
                    result.await()
                }
            ) { successCount ->
                _clipboard.value = ClipboardState()
                _statusMessage.value = if (clip.isCut) {
                    "$successCount آیتم با موفقیت جابجا شد"
                } else {
                    "$successCount آیتم با موفقیت کپی شد"
                }
                refreshStorageStats()
                loadCurrentDirectory()
            }
        }
    }

    fun resolveFileConflict(action: FileConflictAction) {
        conflictResolver?.invoke(action)
    }

    fun createFolder(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val target = File(_currentDirectory.value, name.trim())
            val created = withContext(Dispatchers.IO) {
                target.mkdirs()
            }
            if (created) {
                _statusMessage.value = "Folder \"$name\" created"
                loadCurrentDirectory()
            } else {
                _statusMessage.value = "Failed to create folder"
            }
            _showCreateFolderDialog.value = false
        }
    }

    fun createFile(name: String, content: String = "") {
        if (name.isBlank()) return
        viewModelScope.launch {
            val target = File(_currentDirectory.value, name.trim())
            val created = withContext(Dispatchers.IO) {
                try {
                    target.writeText(content)
                    true
                } catch (e: Exception) {
                    false
                }
            }
            if (created) {
                _statusMessage.value = "File \"$name\" created"
                loadCurrentDirectory()
            } else {
                _statusMessage.value = "Failed to create file"
            }
            _showCreateFileDialog.value = false
        }
    }

    fun rename(file: File, newName: String) {
        if (newName.isBlank() || file.name == newName.trim()) {
            _fileToRename.value = null
            return
        }
        viewModelScope.launch {
            val dest = File(file.parentFile, newName.trim())
            val success = withContext(Dispatchers.IO) {
                file.renameTo(dest)
            }
            if (success) {
                _statusMessage.value = "Renamed successfully"
                loadCurrentDirectory()
            } else {
                _statusMessage.value = "Failed to rename"
            }
            _fileToRename.value = null
        }
    }

    fun confirmDelete(files: List<FileItem>) {
        _filesToDelete.value = files
    }

    fun deleteConfirmed() {
        val targets = _filesToDelete.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            var deletedCount = 0
            withContext(Dispatchers.IO) {
                for (item in targets) {
                    if (FileUtils.deleteRecursively(item.file)) {
                        deletedCount++
                    }
                }
            }
            _filesToDelete.value = null
            clearSelection()
            _statusMessage.value = "$deletedCount items deleted"
            refreshStorageStats()
            loadCurrentDirectory()
        }
    }

    fun cancelDelete() {
        _filesToDelete.value = null
    }

    fun zipSelected(zipName: String) {
        val files = _filesToZip.value ?: return
        val finalName = if (zipName.endsWith(".zip", ignoreCase = true)) zipName else "$zipName.zip"
        val dest = File(_currentDirectory.value, finalName)

        viewModelScope.launch {
            _isLoading.value = true
            val success = withContext(Dispatchers.IO) {
                FileUtils.zipFiles(files, dest)
            }
            _filesToZip.value = null
            clearSelection()
            if (success) {
                _statusMessage.value = "Archive \"$finalName\" created successfully"
                loadCurrentDirectory()
            } else {
                _statusMessage.value = "Failed to create archive"
            }
            _isLoading.value = false
        }
    }

    fun extractZip(zipFile: File) {
        val targetDirName = zipFile.nameWithoutExtension
        val destDir = File(_currentDirectory.value, targetDirName)
        viewModelScope.launch {
            _isLoading.value = true
            val success = withContext(Dispatchers.IO) {
                FileUtils.unzipArchive(zipFile, destDir)
            }
            if (success) {
                _statusMessage.value = "Archive extracted to \"$targetDirName\""
                loadCurrentDirectory()
            } else {
                _statusMessage.value = "Failed to extract ZIP archive"
            }
            _isLoading.value = false
        }
    }

    fun openFileItem(fileItem: FileItem, context: Context) {
        val file = fileItem.file
        when {
            fileItem.isDirectory -> {
                if (file.absolutePath == "/storage/emulated/0/Android/data") {
                    context.startActivity(
                        Intent().setComponent(
                            ComponentName(
                                "com.google.android.documentsui",
                                "com.android.documentsui.files.FilesActivity"
                            )
                        )
                    )
                } else {
                    navigateTo(file)
                    _isBrowsingFiles.value = true
                }
            }
            FileUtils.isPdfFile(file) -> {
                _fileForPdfViewer.value = file
            }
            FileUtils.isVideoFile(file) -> {
                _fileForVideoPlayer.value = file
            }
            FileUtils.isTextFile(file) -> {
                viewModelScope.launch {
                    val content = FileUtils.readText(file)
                    _textEditContent.value = content
                    _fileForTextEdit.value = file
                }
            }
            FileUtils.isImageFile(file) -> {
                _fileForImagePreview.value = file
            }
            FileUtils.isZipFile(file) -> {
                extractZip(file)
            }
            else -> {
                FileUtils.openFile(context, file)
            }
        }
    }

    fun openPdfFile(file: File) {
        _fileForPdfViewer.value = file
    }

    fun openVideoFile(file: File) {
        _fileForVideoPlayer.value = file
    }

    fun closePdfViewer() {
        _fileForPdfViewer.value = null
    }

    fun closeVideoPlayer() {
        _fileForVideoPlayer.value = null
    }

    fun saveTextContent(newContent: String) {
        val file = _fileForTextEdit.value ?: return
        viewModelScope.launch {
            val saved = FileUtils.writeText(file, newContent)
            if (saved) {
                _statusMessage.value = "File saved successfully"
                loadCurrentDirectory()
            } else {
                _statusMessage.value = "Failed to save file"
            }
            _fileForTextEdit.value = null
        }
    }

    fun closeTextEditor() {
        _fileForTextEdit.value = null
        _textEditContent.value = ""
    }

    fun closeImagePreview() {
        _fileForImagePreview.value = null
    }

    fun showDetails(fileItem: FileItem) {
        _fileForDetails.value = fileItem
    }

    fun closeDetails() {
        _fileForDetails.value = null
    }

    fun requestRename(fileItem: FileItem) {
        _fileToRename.value = fileItem
    }

    fun cancelRename() {
        _fileToRename.value = null
    }

    fun requestCreateFolder() {
        _showCreateFolderDialog.value = true
    }

    fun cancelCreateFolder() {
        _showCreateFolderDialog.value = false
    }

    fun requestCreateFile() {
        _showCreateFileDialog.value = true
    }

    fun cancelCreateFile() {
        _showCreateFileDialog.value = false
    }

    fun requestZip(files: List<File>) {
        _filesToZip.value = files
    }

    fun cancelZip() {
        _filesToZip.value = null
    }

    fun dismissStatusMessage() {
        _statusMessage.value = null
    }
}
