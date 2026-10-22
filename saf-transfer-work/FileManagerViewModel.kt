package com.example.viewmodel

import android.app.Application
import android.content.ComponentName
import android.content.Intent
import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import android.os.storage.StorageManager
import android.os.storage.StorageVolume
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
import com.example.util.DirectoryMetadataCache
import com.example.util.AlwaysOpenManager
import com.example.util.VirtualZipUtils
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.Locale
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.exception.ZipException

class FileManagerViewModel(application: Application) : AndroidViewModel(application) {

    private val context: Context get() = getApplication<Application>().applicationContext

    private val rootDirectory: File = FileUtils.getPrimaryStorageRoot(context)

    private val safStorageManager = com.example.util.SafStorageManager(context)
    private val currentSafDirectory = MutableStateFlow<DocumentFile?>(null)
    private val safDirectoryStack = mutableListOf<DocumentFile>()

    fun saveSafTreePermission(key: String, uri: Uri, flags: Int): Boolean {
        return safStorageManager.saveTreePermission(key, uri, flags)
    }

    fun openSafTree(key: String): Boolean {
        val tree = safStorageManager.getTree(key) ?: return false
        if (!tree.isDirectory) return false

        safDirectoryStack.clear()
        safDirectoryStack.add(tree)
        currentSafDirectory.value = tree
        _virtualZipArchive.value = null
        _virtualZipPath.value = null
        _selectedCategory.value = null
        _selectedPaths.value = emptySet()
        _searchQuery.value = ""
        _files.value = emptyList()
        _directoryContentsReady.value = false
        _isBrowsingFiles.value = true
        loadCurrentDirectory(deferChildCounts = true)
        clearSelection()
        return true
    }

    fun navigateSafTo(document: DocumentFile) {
        if (!document.isDirectory) return

        val index = safDirectoryStack.indexOfFirst { it.uri == document.uri }
        if (index >= 0) {
            while (safDirectoryStack.lastIndex > index) {
                safDirectoryStack.removeAt(safDirectoryStack.lastIndex)
            }
        } else {
            safDirectoryStack.add(document)
        }

        currentSafDirectory.value = document
        _selectedPaths.value = emptySet()
        _searchQuery.value = ""
        _files.value = emptyList()
        _directoryContentsReady.value = false
        loadCurrentDirectory(deferChildCounts = true)
        clearSelection()
    }

    private fun createSafFileItems(directory: DocumentFile): List<FileItem> {
        return try {
            directory.listFiles().map { document ->
                val name = document.name ?: document.uri.lastPathSegment ?: "Unknown"
                val isDirectory = document.isDirectory
                val size = if (isDirectory) 0L else document.length().coerceAtLeast(0L)
                val modified = document.lastModified()
                val pseudoFile = File(name)

                FileItem(
                    file = pseudoFile,
                    name = name,
                    path = "saf:${document.uri}",
                    size = size,
                    formattedSize = if (isDirectory) "" else FileUtils.formatBytes(size),
                    lastModified = modified,
                    formattedDate = if (modified > 0L) FileUtils.formatDate(modified) else "",
                    isDirectory = isDirectory,
                    extension = if (isDirectory) "" else pseudoFile.extension,
                    childCount = null,
                    category = if (isDirectory) FileCategory.FOLDER else FileUtils.getCategory(pseudoFile),
                    canRead = document.canRead(),
                    canWrite = document.canWrite(),
                    isHidden = name.startsWith("."),
                    documentUri = document.uri.toString()
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }


    private var storageVolumeCallbackCompat: StorageVolumeCallbackCompat? = null

    private val storageManager: StorageManager =
        context.getSystemService(Context.STORAGE_SERVICE) as StorageManager

    private val _currentDirectory = MutableStateFlow<File>(rootDirectory)
    val currentDirectory: StateFlow<File> = _currentDirectory.asStateFlow()

    private val _breadcrumbs = MutableStateFlow<List<File>>(listOf(rootDirectory))
    val breadcrumbs: StateFlow<List<File>> = _breadcrumbs.asStateFlow()

    private val _virtualZipArchive = MutableStateFlow<File?>(null)
    val virtualZipArchive: StateFlow<File?> = _virtualZipArchive.asStateFlow()

    private val _virtualZipPath = MutableStateFlow<String?>(null)
    val virtualZipPath: StateFlow<String?> = _virtualZipPath.asStateFlow()

    private val _files = MutableStateFlow<List<FileItem>>(emptyList())
    val files: StateFlow<List<FileItem>> = _files.asStateFlow()

    private val _isLoading = MutableStateFlow<Boolean>(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _directoryContentsReady = MutableStateFlow(false)
    private var directoryLoadJob: Job? = null
    private var directoryLoadRequestId: Long = 0L
    val directoryContentsReady: StateFlow<Boolean> = _directoryContentsReady.asStateFlow()

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

    private val _fileForTextViewer = MutableStateFlow<File?>(null)
    val fileForTextViewer: StateFlow<File?> = _fileForTextViewer.asStateFlow()
    private val _textViewerContent = MutableStateFlow<String>("")
    val textViewerContent: StateFlow<String> = _textViewerContent.asStateFlow()

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

    private val _archiveProgress = MutableStateFlow<Float?>(null)
    val archiveProgress: StateFlow<Float?> = _archiveProgress.asStateFlow()

    private val _archiveTitle = MutableStateFlow<String?>(null)
    val archiveTitle: StateFlow<String?> = _archiveTitle.asStateFlow()

    private val _deleteProgress = MutableStateFlow<Float?>(null)
    val deleteProgress: StateFlow<Float?> = _deleteProgress.asStateFlow()

    private var deleteJob: kotlinx.coroutines.Job? = null
private val _archivePasswordZip = MutableStateFlow<File?>(null)
val archivePasswordZip: StateFlow<File?> = _archivePasswordZip.asStateFlow()

private val _virtualZipPasswordFile = MutableStateFlow<File?>(null)
val virtualZipPasswordFile: StateFlow<File?> = _virtualZipPasswordFile.asStateFlow()


    private var archiveJob: kotlinx.coroutines.Job? = null

    private var conflictResolver: ((FileConflictAction) -> Unit)? = null
    private var applyConflictActionToAll: FileConflictAction? = null

    // Windows 11 Transfer Progress Manager
    private val transferManager = FileTransferManager(context)
    val transferState: StateFlow<TransferProgressState> = transferManager.transferState

    private val transferNotificationManager =
        com.example.util.TransferNotificationManager(context)

    fun toggleTransferPause() {
        if (transferManager.transferState.value.isPaused) {
            transferManager.resume()
        } else {
            transferManager.pause()
        }
    }

    fun cancelTransfer() {
        transferManager.cancel()
        _statusMessage.value = "File transfer Cancelled"
        loadCurrentDirectory()
                clearSelection()
    }

    fun dismissTransferDialog() {
        transferManager.dismiss()
    }

    init {
        checkPermission()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            storageVolumeCallbackCompat =
                StorageVolumeCallbackCompat(context, storageManager) {
                    refreshStorageStats()
                }.also { it.register() }
        }

        viewModelScope.launch {
            transferState.collectLatest { state ->
                transferNotificationManager.showOrUpdate(state)
            }
        }

        viewModelScope.launch {
            FileUtils.createSampleFilesIfEmpty(rootDirectory)
            loadDefaultBookmarks()
            refreshStorageStats()
            loadCurrentDirectory()
                clearSelection()
        }
    }

    override fun onCleared() {
        storageVolumeCallbackCompat?.unregister()
        storageVolumeCallbackCompat = null
        super.onCleared()
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

        val prefs = context.getSharedPreferences("bookmark_preferences", Context.MODE_PRIVATE)

        for ((name, dir, iconType) in candidates) {
            if (dir != null && dir.exists() && !prefs.getBoolean("removed_${dir.absolutePath}", false)) {
                list.add(BookmarkItem(name = name, path = dir.absolutePath, file = dir, isDirectory = true, iconType = iconType))
            }
        }

        // Load user-created bookmarks
        // If a custom bookmark points to a default bookmark path,
        // replace the default item instead of creating a duplicate.
        val customPrefix = "custom_"
        prefs.all
            .filter { it.key.startsWith(customPrefix) && it.value is String }
            .forEach { (key, value) ->
                val path = key.removePrefix(customPrefix)
                val file = File(path)

                if (file.exists() && !prefs.getBoolean("removed_$path", false)) {
                    val existingIndex = list.indexOfFirst { it.path == path }

                    val customBookmark = BookmarkItem(
                        name = value as String,
                        path = path,
                        file = file,
                        isDirectory = file.isDirectory,
                        iconType = when {
                                File(
                                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                                    "Camera"
                                ).absolutePath == path -> "CAMERA"
                                existingIndex >= 0 -> list[existingIndex].iconType
                                else -> "FOLDER"
                            }
                    )

                    if (existingIndex >= 0) {
                        list[existingIndex] = customBookmark
                    } else {
                        list.add(customBookmark)
                    }
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
            val iconType = when (path) {
                File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "Camera").absolutePath -> "CAMERA"
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath -> "DOWNLOADS"
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).absolutePath -> "DOCUMENTS"
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).absolutePath -> "PICTURES"
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).absolutePath -> "PICTURES"
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).absolutePath -> "MOVIES"
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).absolutePath -> "MUSIC"
                else -> "FOLDER"
            }

            current.add(
                BookmarkItem(
                    name = name.ifBlank { file.name.ifEmpty { "Bookmark" } },
                    path = path,
                    file = file,
                    isDirectory = file.isDirectory,
                    iconType = iconType
                )
            )

            context.getSharedPreferences("bookmark_preferences", Context.MODE_PRIVATE)
                .edit()
                .remove("removed_$path")
                .putString("custom_$path", name.ifBlank { file.name.ifEmpty { "Bookmark" } })
                .apply()

            _bookmarks.value = current
            _statusMessage.value = "Bookmark added: $name"
        } else {
            _statusMessage.value = "Bookmark already exists"
        }
    }

    fun removeBookmark(path: String) {
        _bookmarks.value = _bookmarks.value.filter { it.path != path }

        context.getSharedPreferences("bookmark_preferences", Context.MODE_PRIVATE)
            .edit()
            .remove("custom_$path")
            .putBoolean("removed_$path", true)
            .apply()

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
        _virtualZipArchive.value?.let { VirtualZipUtils.clearPassword(it) }
        _virtualZipPasswordFile.value = null
        _virtualZipArchive.value = null
        _virtualZipPath.value = null
        _isBrowsingFiles.value = false
        _selectedCategory.value = null
        _selectedPaths.value = emptySet()
        _searchQuery.value = ""
    }


    fun calculateCurrentDirectoryChildCounts() {
        viewModelScope.launch {
            val snapshot = _files.value
            val snapshotPaths = snapshot.map { it.path }

            val counts = withContext(Dispatchers.IO) {
                snapshot.mapNotNull { item ->
                    if (item.isDirectory && item.virtualZipArchive == null) {
                        item.path to try {
                            item.file.list()?.size ?: 0
                        } catch (_: Exception) {
                            0
                        }
                    } else null
                }.toMap()
            }

            if (_files.value.map { it.path } == snapshotPaths) {
                val updated = _files.value.map { item ->
                    if (item.isDirectory && item.virtualZipArchive == null) {
                        item.copy(childCount = counts[item.path] ?: 0)
                    } else item
                }
                _files.value = updated

                val directory = _currentDirectory.value
                if (_virtualZipArchive.value == null &&
                    (_selectedCategory.value == null ||
                        _selectedCategory.value == FileCategory.ALL) &&
                    _searchQuery.value.isBlank()
                ) {
                    withContext(Dispatchers.IO) {
                        DirectoryMetadataCache.save(context, directory, updated)
                    }
                }
            }
        }
    }

    fun loadCurrentDirectory(deferChildCounts: Boolean = false) {
        val requestId = ++directoryLoadRequestId
        _directoryContentsReady.value = false
        directoryLoadJob?.cancel()
    directoryLoadJob = viewModelScope.launch {
            if (requestId != directoryLoadRequestId) return@launch
            val showLoading = _files.value.isEmpty()
            if (showLoading) {
                _isLoading.value = true
            }
            val dir = _currentDirectory.value
            val category = _selectedCategory.value
            val useDirectoryCache = _virtualZipArchive.value == null &&
                (category == null || category == FileCategory.ALL) &&
                _searchQuery.value.isBlank()

            val cachedItems = if (useDirectoryCache) {
                withContext(Dispatchers.IO) {
                    DirectoryMetadataCache.load(context, dir)
                }
            } else null

            if (cachedItems != null &&
                _currentDirectory.value.absolutePath == dir.absolutePath &&
                _virtualZipArchive.value == null &&
                _searchQuery.value.isBlank()
            ) {
                currentCoroutineContext().ensureActive()
                if (requestId != directoryLoadRequestId) return@launch
                _files.value = sortAndFilter(cachedItems)
                updateBreadcrumbs(dir)
                _directoryContentsReady.value = true
                _isLoading.value = false
            }

            val items = withContext(Dispatchers.IO) {
                val zipArchive = _virtualZipArchive.value
                if (zipArchive != null) {
                    val zipPath = _virtualZipPath.value.orEmpty()
                    VirtualZipUtils.list(zipArchive, zipPath).map { entry ->
                        val virtualPath = if (zipPath.isBlank()) {
                            "${zipArchive.absolutePath}/${entry.path}"
                        } else {
                            "${zipArchive.absolutePath}/$zipPath/${entry.path}"
                        }

                        FileItem(
                            file = zipArchive,
                            name = entry.name,
                            path = virtualPath,
                            size = entry.size,
                            formattedSize = if (entry.isDirectory) "" else FileUtils.formatBytes(entry.size),
                            lastModified = entry.lastModified,
                            formattedDate = if (entry.lastModified > 0L) {
                                FileUtils.formatDate(entry.lastModified)
                            } else {
                                ""
                            },
                            isDirectory = entry.isDirectory,
                            extension = if (entry.isDirectory) "" else File(entry.name).extension,
                            childCount = null,
                            category = if (entry.isDirectory) {
                                FileCategory.FOLDER
                            } else {
                                FileUtils.getCategory(File(entry.name))
                            },
                            canRead = true,
                            canWrite = false,
                            isHidden = entry.name.startsWith("."),
                            virtualZipArchive = zipArchive,
                            virtualZipEntryPath = entry.path
                        )
                    }
                } else if (currentSafDirectory.value != null) {
                        createSafFileItems(currentSafDirectory.value!!)
} else if (category != null && category != FileCategory.ALL) {
                    // Collect items of specific category
                    findFilesByCategory(rootDirectory, category, deferChildCounts)
                } else {
                    val rawFiles = dir.listFiles()?.toList() ?: emptyList()
                    android.util.Log.d(
                        "FileManagerDebug",
                        "DIR=${dir.absolutePath} rawFiles=${rawFiles.size} files=${rawFiles.count { !it.isDirectory }} dirs=${rawFiles.count { it.isDirectory }}"
                    )
                    if (useDirectoryCache) {
                        DirectoryMetadataCache.reconcile(
                            context = context,
                            directory = dir,
                            files = rawFiles,
                            cachedItems = cachedItems,
                            calculateChildCounts = !deferChildCounts
                        )
                    } else {
                        rawFiles.map {
                            FileUtils.toFileItem(
                                it,
                                calculateChildCount = !deferChildCounts
                            )
                        }
                    }
                }
            }

            currentCoroutineContext().ensureActive()
            if (requestId != directoryLoadRequestId) return@launch
            _files.value = sortAndFilter(items)
            _directoryContentsReady.value = true
            android.util.Log.d(
                "FileManagerDebug",
                "FINAL dir=${dir.absolutePath} items=${items.size} final=${_files.value.size} " +
                    "files=${_files.value.count { !it.isDirectory }} dirs=${_files.value.count { it.isDirectory }}"
            )
            updateBreadcrumbs(dir)
            _isLoading.value = false
        }
    }

    private fun findFilesByCategory(
        root: File,
        category: FileCategory,
        deferChildCounts: Boolean = false
    ): List<FileItem> {
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
                        result.add(FileUtils.toFileItem(child, calculateChildCount = !deferChildCounts))
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
            SortOption.SIZE_DESC -> compareBy<FileItem> { !it.isDirectory }
                .thenByDescending { it.size }
            SortOption.SIZE_ASC -> compareBy<FileItem> { !it.isDirectory }
                .thenBy { it.size }
            SortOption.TYPE_ASC -> compareBy<FileItem> { !it.isDirectory }
                .thenBy {
                    if (it.isDirectory) "" else File(it.name).extension.lowercase(Locale.ROOT)
                }
                .thenBy { it.name.lowercase(Locale.ROOT) }
        }

        return list.sortedWith(comparator)
    }

    fun navigateTo(directory: File) {
        if (!directory.exists() || !directory.isDirectory) return
        currentSafDirectory.value = null
        safDirectoryStack.clear()

        _virtualZipArchive.value?.let { VirtualZipUtils.clearPassword(it) }
        _virtualZipPasswordFile.value = null
        _virtualZipArchive.value = null
        _virtualZipPath.value = null

        _currentDirectory.value = directory
    _files.value = emptyList()
    _directoryContentsReady.value = false
        _selectedCategory.value = null
        _selectedPaths.value = emptySet()
        _searchQuery.value = ""
        _isBrowsingFiles.value = true
        loadCurrentDirectory(deferChildCounts = true)
        clearSelection()
    }

    fun navigateVirtualZipTo(path: String) {
        if (_virtualZipArchive.value == null) return

        _virtualZipPath.value = path.trim('/')
        _selectedPaths.value = emptySet()
        _searchQuery.value = ""
        loadCurrentDirectory()
        clearSelection()
    }

    fun navigateToParent(): Boolean {
        val zipArchive = _virtualZipArchive.value
        if (zipArchive != null) {
            val currentZipPath = _virtualZipPath.value.orEmpty()

            if (currentZipPath.isNotBlank()) {
                val parentPath = currentZipPath.substringBeforeLast("/", "")
                _virtualZipPath.value = parentPath
                _selectedPaths.value = emptySet()
                _searchQuery.value = ""
                loadCurrentDirectory()
                clearSelection()
                return true
            }

            val zipParent = zipArchive.parentFile

            _virtualZipArchive.value = null
            _virtualZipPath.value = null
            _selectedPaths.value = emptySet()
            _searchQuery.value = ""

            if (zipParent != null && zipParent.exists() && zipParent.isDirectory) {
                _currentDirectory.value = zipParent
                _isBrowsingFiles.value = true
            }

            loadCurrentDirectory()
            clearSelection()
            return true
        }

        if (currentSafDirectory.value != null) {
            if (safDirectoryStack.size > 1) {
                safDirectoryStack.removeAt(safDirectoryStack.lastIndex)
                currentSafDirectory.value = safDirectoryStack.last()
                _selectedPaths.value = emptySet()
                _searchQuery.value = ""
                loadCurrentDirectory(deferChildCounts = true)
                clearSelection()
                return true
            }
            currentSafDirectory.value = null
            safDirectoryStack.clear()
            _files.value = emptyList()
            _directoryContentsReady.value = false
            _isBrowsingFiles.value = false
            return true
        }

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
                clearSelection()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        if (query.isNotBlank()) {
            _isBrowsingFiles.value = true
        }
        loadCurrentDirectory()
                clearSelection()
    }

    fun setSortOption(sort: SortOption) {
        _sortOption.value = sort
        loadCurrentDirectory()
                clearSelection()
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
                clearSelection()
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
        val allPaths = _files.value.map { it.path }.toSet()
        _selectedPaths.value =
            if (allPaths.isNotEmpty() && _selectedPaths.value == allPaths) {
                emptySet()
            } else {
                allPaths
            }
    }

    fun clearSelection() {
        _selectedPaths.value = emptySet()
    }

    fun copySelected() {
        val selectedItems = _files.value.filter {
            _selectedPaths.value.contains(it.path)
        }
        if (selectedItems.isEmpty()) return

        val safItems = selectedItems.mapNotNull { item ->
            item.documentUri?.let { uri ->
                com.example.model.ClipboardSafItem(
                    uri = uri,
                    name = item.name,
                    isDirectory = item.isDirectory,
                    treeUri = safDirectoryStack.firstOrNull()?.uri?.toString().orEmpty()
                )
            }
        }

        viewModelScope.launch {
            val selectedFiles = withContext(Dispatchers.IO) {
                selectedItems.mapNotNull { item ->
                    if (item.documentUri != null) {
                        null
                    } else if (item.virtualZipArchive != null) {
                        VirtualZipUtils.materializeEntry(
                            context = context,
                            zipFile = item.virtualZipArchive,
                            entryPath = item.virtualZipEntryPath.orEmpty()
                        )
                    } else {
                        item.file
                    }
                }
            }

            if (selectedFiles.isNotEmpty() || safItems.isNotEmpty()) {
                _clipboard.value = ClipboardState(
                    items = selectedFiles,
                    isCut = false,
                    safItems = safItems
                )
                _statusMessage.value =
                    "${selectedFiles.size + safItems.size} items copied"
                clearSelection()
            } else {
                _statusMessage.value = "Failed to copy selected items"
            }
        }
    }

    fun cutSelected() {
        val selectedItems = _files.value.filter {
            _selectedPaths.value.contains(it.path)
        }

        val selectedFiles = selectedItems
            .filter { it.documentUri == null }
            .map { it.file }

        val safItems = selectedItems.mapNotNull { item ->
            item.documentUri?.let { uri ->
                com.example.model.ClipboardSafItem(
                    uri = uri,
                    name = item.name,
                    isDirectory = item.isDirectory,
                    treeUri = safDirectoryStack.firstOrNull()?.uri?.toString().orEmpty()
                )
            }
        }

        if (selectedFiles.isNotEmpty() || safItems.isNotEmpty()) {
            _clipboard.value = ClipboardState(
                items = selectedFiles,
                isCut = true,
                safItems = safItems
            )
            _statusMessage.value =
                "${selectedFiles.size + safItems.size} items cut"
            clearSelection()
        }
    }

    fun copyItem(fileItem: FileItem) {
        if (fileItem.virtualZipArchive != null) {
            viewModelScope.launch {
                val materializedFile = withContext(Dispatchers.IO) {
                    VirtualZipUtils.materializeEntry(
                        context = context,
                        zipFile = fileItem.virtualZipArchive,
                        entryPath = fileItem.virtualZipEntryPath.orEmpty()
                    )
                }

                if (materializedFile != null) {
                    _clipboard.value = ClipboardState(
                        items = listOf(materializedFile),
                        isCut = false
                    )
                    _statusMessage.value = "\"${fileItem.name}\" copied"
                } else {
                    _statusMessage.value = "Failed to copy ZIP entry"
                }
            }
        } else if (fileItem.documentUri != null) {
            _clipboard.value = ClipboardState(
                isCut = false,
                safItems = listOf(
                    com.example.model.ClipboardSafItem(
                        uri = fileItem.documentUri,
                        name = fileItem.name,
                        isDirectory = fileItem.isDirectory,
                        treeUri = safDirectoryStack.firstOrNull()?.uri?.toString().orEmpty()
                    )
                )
            )
            _statusMessage.value = "\"${fileItem.name}\" copied"
        } else {
            _clipboard.value = ClipboardState(
                items = listOf(fileItem.file),
                isCut = false
            )
            _statusMessage.value = "\"${fileItem.name}\" copied"
        }
    }

    fun cutItem(fileItem: FileItem) {
        if (fileItem.documentUri != null) {
            _clipboard.value = ClipboardState(
                isCut = true,
                safItems = listOf(
                    com.example.model.ClipboardSafItem(
                        uri = fileItem.documentUri,
                        name = fileItem.name,
                        isDirectory = fileItem.isDirectory,
                        treeUri = safDirectoryStack.firstOrNull()?.uri?.toString().orEmpty()
                    )
                )
            )
        } else {
            _clipboard.value = ClipboardState(
                items = listOf(fileItem.file),
                isCut = true
            )
        }
        _statusMessage.value = "\"${fileItem.name}\" cut"
    }

    fun clearClipboard() {
        _clipboard.value = ClipboardState()
    }

    fun pasteClipboard() {
        val clip = _clipboard.value
        if (!clip.hasItems) return

        if (clip.safItems.isNotEmpty()) {
            _statusMessage.value = "SAF transfer support is not enabled yet"
            return
        }

        val targetDir = _currentDirectory.value
        viewModelScope.launch {
            applyConflictActionToAll = null

            transferManager.executeTransfer(
                items = clip.items,
                targetDir = targetDir,
                isCut = clip.isCut,
                onConflict = { source, destination ->
                    applyConflictActionToAll?.let { action ->
                        return@executeTransfer action
                    }

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
                applyConflictActionToAll = null
                _clipboard.value = ClipboardState()
                _statusMessage.value = if (clip.isCut) {
                    "$successCount items moved successfully"
                } else {
                    "$successCount items copied successfully"
                }
                refreshStorageStats()
                loadCurrentDirectory()
                clearSelection()
            }
        }
    }

    fun resolveFileConflict(
        action: FileConflictAction,
        applyToAll: Boolean = false
    ) {
        if (applyToAll) {
            applyConflictActionToAll = action
        }
        conflictResolver?.invoke(action)
    }

    fun createFolder(name: String) {
        val trimmedName = name.trim()
        if (trimmedName.isBlank()) return

        viewModelScope.launch {
            val safDirectory = currentSafDirectory.value
            val created = withContext(Dispatchers.IO) {
                if (safDirectory != null) {
                    safStorageManager.createDirectory(safDirectory, trimmedName) != null
                } else {
                    File(_currentDirectory.value, trimmedName).mkdirs()
                }
            }

            _statusMessage.value = if (created) {
                "Folder \"$trimmedName\" created"
            } else {
                "Failed to create folder"
            }

            if (created) {
                loadCurrentDirectory(deferChildCounts = safDirectory != null)
                clearSelection()
            }
            _showCreateFolderDialog.value = false
        }
    }

    fun createFile(name: String, content: String = "") {
        val trimmedName = name.trim()
        if (trimmedName.isBlank()) return

        viewModelScope.launch {
            val safDirectory = currentSafDirectory.value
            val created = withContext(Dispatchers.IO) {
                try {
                    if (safDirectory != null) {
                        val document = safStorageManager.createFile(
                            safDirectory,
                            "text/plain",
                            trimmedName
                        ) ?: return@withContext false

                        safStorageManager.openOutputStream(document.uri)?.use {
                            it.write(content.toByteArray(Charsets.UTF_8))
                        } ?: return@withContext false
                        true
                    } else {
                        File(_currentDirectory.value, trimmedName).writeText(content)
                        true
                    }
                } catch (_: Exception) {
                    false
                }
            }

            _statusMessage.value = if (created) {
                "File \"$trimmedName\" created"
            } else {
                "Failed to create file"
            }

            if (created) {
                loadCurrentDirectory(deferChildCounts = safDirectory != null)
                clearSelection()
            }
            _showCreateFileDialog.value = false
        }
    }

    fun rename(file: File, newName: String) {
        val trimmedName = newName.trim()
        if (trimmedName.isBlank() || file.name == trimmedName) {
            _fileToRename.value = null
            return
        }

        viewModelScope.launch {
            val safDirectory = currentSafDirectory.value
            val success = withContext(Dispatchers.IO) {
                if (safDirectory != null) {
                    try {
                        val document = safDirectory.findFile(file.name)
                        document != null &&
                            safStorageManager.rename(document, trimmedName)
                    } catch (_: Exception) {
                        false
                    }
                } else {
                    try {
                        val parent = file.parentFile ?: return@withContext false
                        file.renameTo(File(parent, trimmedName))
                    } catch (_: Exception) {
                        false
                    }
                }
            }

            _statusMessage.value = if (success) {
                "Renamed successfully"
            } else {
                "Failed to rename"
            }

            _fileToRename.value = null
            if (success) {
                clearSelection()
                loadCurrentDirectory(deferChildCounts = safDirectory != null)
            }
        }
    }

    fun confirmDelete(files: List<FileItem>) {
        _filesToDelete.value = files
    }

    private fun deleteSafDocumentRecursively(
        document: androidx.documentfile.provider.DocumentFile
    ): Boolean {
        return try {
            if (document.isDirectory) {
                for (child in document.listFiles()) {
                    if (!deleteSafDocumentRecursively(child)) return false
                }
            }
            safStorageManager.delete(document)
        } catch (_: Exception) {
            false
        }
    }

    fun deleteConfirmed() {
        val targets = _filesToDelete.value ?: return
        if (deleteJob?.isActive == true) return

        _filesToDelete.value = null
        _deleteProgress.value = 0f

        deleteJob = viewModelScope.launch {
            try {
                var deletedCount = 0
                var failedCount = 0

                for ((index, item) in targets.withIndex()) {
                    currentCoroutineContext().ensureActive()

                    val success = withContext(Dispatchers.IO) {
                        try {
                            val uriText = item.documentUri
                            if (uriText != null) {
                                val uri = android.net.Uri.parse(uriText)
                                val document =
                                    androidx.documentfile.provider.DocumentFile
                                        .fromSingleUri(context, uri)
                                document != null && deleteSafDocumentRecursively(document)
                            } else {
                                FileUtils.deleteRecursivelyWithProgress(listOf(item.file)) {
                                    progress ->
                                    _deleteProgress.value =
                                        (index + progress) / targets.size.coerceAtLeast(1)
                                } > 0
                            }
                        } catch (_: Exception) {
                            false
                        }
                    }

                    if (success) deletedCount++ else failedCount++
                    _deleteProgress.value =
                        (index + 1).toFloat() / targets.size.coerceAtLeast(1)
                }

                _statusMessage.value = when {
                    failedCount == 0 -> "$deletedCount items deleted"
                    deletedCount == 0 -> "Failed to delete files"
                    else -> "$deletedCount items deleted; $failedCount failed"
                }

                clearSelection()
                refreshStorageStats()
                loadCurrentDirectory(deferChildCounts = currentSafDirectory.value != null)
            } catch (e: CancellationException) {
                _statusMessage.value = "Delete cancelled"
                throw e
            } catch (_: Exception) {
                _statusMessage.value = "Failed to delete files"
            } finally {
                _deleteProgress.value = null
                deleteJob = null
                clearSelection()
            }
        }
    }

    fun cancelDelete() {
        if (deleteJob?.isActive == true) {
            deleteJob?.cancel()
        } else {
            _filesToDelete.value = null
        }
    }

    private suspend fun awaitArchiveConflict(
        source: File,
        destination: File
    ): FileConflictAction {
        val result = CompletableDeferred<FileConflictAction>()

        _fileConflict.value = source to destination
        conflictResolver = { action ->
            _fileConflict.value = null
            conflictResolver = null
            result.complete(action)
        }

        return result.await()
    }

    private fun renamedArchiveDestination(file: File): File {
        val parent = file.parentFile ?: _currentDirectory.value
        val name = file.name
        val dot = name.lastIndexOf('.')
        val base = if (dot > 0) name.substring(0, dot) else name
        val extension = if (dot > 0) name.substring(dot) else ""

        var index = 1
        var candidate: File
        do {
            candidate = File(parent, "$base ($index)$extension")
            index++
        } while (candidate.exists())

        return candidate
    }

    fun cancelArchivePassword() {
        _archivePasswordZip.value = null
    }

    fun requestArchivePassword(zipFile: File) {
        _archivePasswordZip.value = zipFile
    }

    fun cancelVirtualZipPassword() {
        _virtualZipPasswordFile.value = null
    }

    fun requestVirtualZipPassword(zipFile: File) {
        _virtualZipPasswordFile.value = zipFile
    }

    fun openPasswordProtectedZip(zipFile: File, password: String) {
        viewModelScope.launch {
            val verified = withContext(Dispatchers.IO) {
                VirtualZipUtils.verifyAndSetPassword(zipFile, password)
            }

            if (!verified) {
                _statusMessage.value = "Incorrect ZIP password"
                return@launch
            }

            _virtualZipPasswordFile.value = null
            _virtualZipArchive.value = zipFile
            _virtualZipPath.value = ""
            _selectedCategory.value = null
            _selectedPaths.value = emptySet()
            _searchQuery.value = ""
            _isBrowsingFiles.value = true
            loadCurrentDirectory()
            clearSelection()
        }
    }

    fun extractZipWithPassword(zipFile: File, password: String) {
        _archivePasswordZip.value = null
        archiveJob?.cancel()

        archiveJob = viewModelScope.launch {
            var targetDirName = zipFile.nameWithoutExtension
            var destDir = File(_currentDirectory.value, targetDirName)
            var tempDir: File? = null

            try {
                if (destDir.exists()) {
                    when (awaitArchiveConflict(zipFile, destDir)) {
                        FileConflictAction.OVERWRITE -> Unit
                        FileConflictAction.SKIP -> {
                            clearSelection()
                            archiveJob = null
                            return@launch
                        }
                        FileConflictAction.RENAME -> {
                            destDir = renamedArchiveDestination(destDir)
                            targetDirName = destDir.name
                        }
                    }
                }

                tempDir = File(destDir.parentFile, ".${destDir.name}.extracting")

                if (tempDir.exists()) {
                    tempDir.deleteRecursively()
                }

                _archiveTitle.value = "Extract ZIP File"
                _archiveProgress.value = 0f

                val success = FileUtils.unzipArchiveWithPassword(
                    zipFile,
                    tempDir,
                    password
                ) { transferredBytes, totalBytes ->
                    if (totalBytes > 0L) {
                        _archiveProgress.value =
                            (transferredBytes.toFloat() / totalBytes.toFloat())
                                .coerceIn(0f, 1f)
                    }
                }

                if (!success) {
                    tempDir.deleteRecursively()
                    _statusMessage.value = "Incorrect ZIP password"
                    _archivePasswordZip.value = zipFile
                    return@launch
                }

                currentCoroutineContext().ensureActive()

                withContext(Dispatchers.IO) {
                    if (destDir.exists() && !destDir.deleteRecursively()) {
                        throw IOException("Failed to replace existing destination")
                    }
                    if (!tempDir.renameTo(destDir)) {
                        throw IOException("Failed to finalize extracted archive")
                    }
                }

                _archiveProgress.value = 1f
                _statusMessage.value = "Archive extracted to \"$targetDirName\""
                loadCurrentDirectory()
                clearSelection()
            } catch (e: CancellationException) {
                withContext(Dispatchers.IO) {
                    tempDir?.deleteRecursively()
                }
                throw e
            } catch (e: Exception) {
                withContext(Dispatchers.IO) {
                    tempDir?.deleteRecursively()
                }
                _statusMessage.value = "Failed to extract ZIP archive"
            } finally {
                _archiveProgress.value = null
                _archiveTitle.value = null
                if (archiveJob === currentCoroutineContext()[Job]) {
                    archiveJob = null
                }
            }
        }
    }

    fun cancelArchiveOperation() {
        archiveJob?.cancel()
        archiveJob = null
        _fileConflict.value = null
        conflictResolver = null
        applyConflictActionToAll = null
        _archiveProgress.value = null
        _archiveTitle.value = null
        _isLoading.value = false
        clearSelection()
    }

    fun zipSelected(zipName: String) {
        val files = _filesToZip.value ?: return

        // Close the ZIP name dialog immediately.
        _filesToZip.value = null

        val requestedName =
            if (zipName.endsWith(".zip", ignoreCase = true)) zipName else "$zipName.zip"

        archiveJob?.cancel()
        archiveJob = viewModelScope.launch {
            var dest = File(_currentDirectory.value, requestedName)

            if (dest.exists()) {
                when (awaitArchiveConflict(files.firstOrNull() ?: dest, dest)) {
                    FileConflictAction.OVERWRITE -> Unit
                    FileConflictAction.SKIP -> {
                        clearSelection()
                        archiveJob = null
                        return@launch
                    }
                    FileConflictAction.RENAME -> {
                        dest = renamedArchiveDestination(dest)
                    }
                }
            }

            val finalName = dest.name
            val tempDest = File(dest.parentFile, ".${dest.name}.part")

            try {
                if (tempDest.exists()) {
                    tempDest.deleteRecursively()
                }

                _archiveTitle.value = "Create ZIP"
                _archiveProgress.value = 0f

                val success = withContext(Dispatchers.IO) {
                    FileUtils.zipFiles(files, tempDest) { _, transferredBytes, totalBytes ->
                        if (totalBytes > 0L) {
                            _archiveProgress.value =
                                (transferredBytes.toFloat() / totalBytes.toFloat())
                                    .coerceIn(0f, 1f)
                        }
                    }
                }

                if (!success) {
                    tempDest.deleteRecursively()
                    _statusMessage.value = "Failed to create archive"
                    return@launch
                }

                currentCoroutineContext().ensureActive()

                withContext(Dispatchers.IO) {
                    if (dest.exists() && !dest.deleteRecursively()) {
                        throw IOException("Failed to replace existing archive")
                    }

                    if (!tempDest.renameTo(dest)) {
                        throw IOException("Failed to finalize archive")
                    }
                }

                _archiveProgress.value = 1f
                _statusMessage.value = "Archive \"$finalName\" created successfully"
                loadCurrentDirectory()
                clearSelection()
            } catch (e: CancellationException) {
                withContext(Dispatchers.IO) {
                    tempDest.deleteRecursively()
                }
                throw e
            } catch (e: Exception) {
                withContext(Dispatchers.IO) {
                    tempDest.deleteRecursively()
                }
                _statusMessage.value = "Failed to create archive"
            } finally {
                _archiveProgress.value = null
                _archiveTitle.value = null
                if (archiveJob === currentCoroutineContext()[Job]) {
                    archiveJob = null
                }
            }
        }
    }

    fun extractZip(zipFile: File) {
        archiveJob?.cancel()

        archiveJob = viewModelScope.launch {
            var targetDirName = zipFile.nameWithoutExtension
            var destDir = File(_currentDirectory.value, targetDirName)

            if (destDir.exists()) {
                when (awaitArchiveConflict(zipFile, destDir)) {
                    FileConflictAction.OVERWRITE -> Unit
                    FileConflictAction.SKIP -> {
                        clearSelection()
                        archiveJob = null
                        return@launch
                    }
                    FileConflictAction.RENAME -> {
                        destDir = renamedArchiveDestination(destDir)
                        targetDirName = destDir.name
                    }
                }
            }

            val tempDir = File(destDir.parentFile, ".${destDir.name}.extracting")

            try {
                if (tempDir.exists()) {
                    tempDir.deleteRecursively()
                }

                _archiveTitle.value = "Extract ZIP File"
                _archiveProgress.value = 0f

                val success = FileUtils.unzipArchive(zipFile, tempDir)

                if (!success) {
                    tempDir.deleteRecursively()
                    _archivePasswordZip.value = zipFile
                    return@launch
                }

                currentCoroutineContext().ensureActive()

                withContext(Dispatchers.IO) {
                    if (destDir.exists() && !destDir.deleteRecursively()) {
                        throw IOException("Failed to replace existing destination")
                    }

                    if (!tempDir.renameTo(destDir)) {
                        throw IOException("Failed to finalize extracted archive")
                    }
                }

                _archiveProgress.value = 1f
                _statusMessage.value = "Archive extracted to \"$targetDirName\""
                loadCurrentDirectory()
                clearSelection()
            } catch (e: CancellationException) {
                withContext(Dispatchers.IO) {
                    tempDir.deleteRecursively()
                }
                throw e
            } catch (e: Exception) {
                withContext(Dispatchers.IO) {
                    tempDir.deleteRecursively()
                }
                _statusMessage.value = "Failed to extract ZIP archive"
            } finally {
                _archiveProgress.value = null
                _archiveTitle.value = null
                if (archiveJob === currentCoroutineContext()[Job]) {
                    archiveJob = null
                }
            }
        }
    }

    fun openFileItem(fileItem: FileItem, context: Context) {
        val file = fileItem.file

        val documentUriString = fileItem.documentUri
        if (documentUriString != null) {
            val document = try {
                currentSafDirectory.value
                    ?.listFiles()
                    ?.firstOrNull { it.uri.toString() == documentUriString }
            } catch (_: SecurityException) {
                null
            }

            if (document == null) {
                _statusMessage.value = "Unable to access this storage item"
                return
            }

            if (document.isDirectory) {
                navigateSafTo(document)
            } else {
                try {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(
                            document.uri,
                            context.contentResolver.getType(document.uri) ?: "*/*"
                        )
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(intent)
                } catch (_: Exception) {
                    _statusMessage.value = "No app can open this file"
                }
            }
            return
        }

        if (fileItem.virtualZipArchive != null) {
            if (fileItem.isDirectory) {
                _virtualZipArchive.value = fileItem.virtualZipArchive
                _virtualZipPath.value = fileItem.virtualZipEntryPath.orEmpty()
                _selectedCategory.value = null
                _selectedPaths.value = emptySet()
                _searchQuery.value = ""
                _isBrowsingFiles.value = true
                loadCurrentDirectory()
                clearSelection()
            } else {
                val zipArchive = fileItem.virtualZipArchive
                val entryPath = fileItem.virtualZipEntryPath.orEmpty()

                viewModelScope.launch {
                    val cachedFile = withContext(Dispatchers.IO) {
                        VirtualZipUtils.materializeEntry(
                            context = context,
                            zipFile = zipArchive,
                            entryPath = entryPath
                        )
                    }

                    if (cachedFile != null) {
                        val cachedItem = fileItem.copy(
                            file = cachedFile,
                            path = cachedFile.absolutePath,
                            virtualZipArchive = null,
                            virtualZipEntryPath = null
                        )
                        openFileItem(cachedItem, context)
                    } else {
                        _statusMessage.value = "Failed to open ZIP entry"
                    }
                }
            }
            return
        }

        if (FileUtils.isZipFile(file)) {
            val encrypted = try {
                ZipFile(file).isEncrypted
            } catch (_: Exception) {
                false
            }

            if (encrypted) {
                requestVirtualZipPassword(file)
                return
            }

            _virtualZipArchive.value = file
            _virtualZipPath.value = ""
            _selectedCategory.value = null
            _selectedPaths.value = emptySet()
            _searchQuery.value = ""
            _isBrowsingFiles.value = true
            loadCurrentDirectory()
            clearSelection()
            return
        }

        if (!fileItem.isDirectory) {
            val mimeType = FileUtils.getMimeType(file)
            val alwaysPackage = AlwaysOpenManager.getPackageName(context, mimeType)
            val alwaysActivity = AlwaysOpenManager.getActivityName(context, mimeType)

            if (
                !alwaysPackage.isNullOrBlank() &&
                !alwaysActivity.isNullOrBlank()
            ) {
                FileUtils.openFileWithApp(
                    context = context,
                    file = file,
                    packageName = alwaysPackage,
                    activityName = alwaysActivity
                )
                return
            }
        }

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
                    _textViewerContent.value = content
                    _fileForTextViewer.value = file
                }
            }
            FileUtils.isImageFile(file) -> {
                _fileForImagePreview.value = file
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

    fun openTextEditorFromViewer() {
        val file = _fileForTextViewer.value ?: return
        _textEditContent.value = _textViewerContent.value
        _fileForTextViewer.value = null
        _fileForTextEdit.value = file
    }

    fun closeTextViewer() {
        _fileForTextViewer.value = null
        _textViewerContent.value = ""
    }

    fun saveTextContent(newContent: String) {
        val file = _fileForTextEdit.value ?: return
        viewModelScope.launch {
            val saved = FileUtils.writeText(file, newContent)
            if (saved) {
                _statusMessage.value = "File saved successfully"
                loadCurrentDirectory()
                clearSelection()
            } else {
                _statusMessage.value = "Failed to save file"
            }
            _fileForTextEdit.value = null
        }
    }

    fun saveTextContentAs(newName: String, newContent: String) {
        val sourceFile = _fileForTextEdit.value ?: return
        val trimmedName = newName.trim()

        if (trimmedName.isBlank()) {
            _statusMessage.value = "File name cannot be empty"
            return
        }

        viewModelScope.launch {
            val targetFile = File(sourceFile.parentFile, trimmedName)
            val saved = withContext(Dispatchers.IO) {
                try {
                    targetFile.writeText(newContent)
                    true
                } catch (e: Exception) {
                    false
                }
            }

            if (saved) {
                _statusMessage.value = "File saved successfully"
                loadCurrentDirectory()
                clearSelection()
                _fileForTextEdit.value = null
            } else {
                _statusMessage.value = "Failed to save file"
            }
        }
    }

    fun closeTextEditor() {
        _fileForTextEdit.value = null
        _textEditContent.value = ""
    }

    fun closeImagePreview() {
        _fileForImagePreview.value = null
        loadCurrentDirectory()
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
