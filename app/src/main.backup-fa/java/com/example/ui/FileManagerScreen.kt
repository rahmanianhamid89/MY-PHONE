package com.example.ui

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import kotlinx.coroutines.launch
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.FileConflictDialog
import com.example.util.FileConflictAction
import com.example.model.BookmarkItem
import com.example.model.FileCategory
import com.example.model.SortOption
import com.example.model.ViewMode
import com.example.ui.components.AddFolderBookmarkDialog
import com.example.ui.components.BookmarksSection
import com.example.ui.components.BottomActionBar
import com.example.ui.components.BreadcrumbBar
import com.example.ui.components.CloudSection
import com.example.ui.components.CreateFileDialog
import com.example.ui.components.CreateFolderDialog
import com.example.ui.components.DeleteBookmarkDialog
import com.example.ui.components.DeleteConfirmDialog
import com.example.ui.components.FileDetailsDialog
import com.example.ui.components.FileGridItem
import com.example.ui.components.FileListItem
import com.example.ui.components.ImagePreviewDialog
import com.example.ui.components.MediaAndDocumentsSection
import com.example.ui.components.PdfViewerDialog
import com.example.ui.components.RenameDialog
import com.example.ui.components.StorageVolumeCard
import com.example.ui.components.TextEditorDialog
import com.example.ui.components.UtilitySection
import com.example.ui.components.VideoPlayerDialog
import com.example.ui.components.Windows11TransferDialog
import com.example.ui.components.ZipDialog
import com.example.util.FileUtils
import com.example.viewmodel.FileManagerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileManagerScreen(
    viewModel: FileManagerViewModel = viewModel()
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = androidx.compose.runtime.rememberCoroutineScope()

    val currentDirectory by viewModel.currentDirectory.collectAsStateWithLifecycle()
    val breadcrumbs by viewModel.breadcrumbs.collectAsStateWithLifecycle()
    val files by viewModel.files.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val sortOption by viewModel.sortOption.collectAsStateWithLifecycle()
    val viewMode by viewModel.viewMode.collectAsStateWithLifecycle()
    val showHiddenFiles by viewModel.showHiddenFiles.collectAsStateWithLifecycle()
    val selectedPaths by viewModel.selectedPaths.collectAsStateWithLifecycle()
    val clipboard by viewModel.clipboard.collectAsStateWithLifecycle()
    val storageStats by viewModel.storageStats.collectAsStateWithLifecycle()
    val hasStoragePermission by viewModel.hasStoragePermission.collectAsStateWithLifecycle()
    val isBrowsingFiles by viewModel.isBrowsingFiles.collectAsStateWithLifecycle()
    val storageVolumes by viewModel.storageVolumes.collectAsStateWithLifecycle()
    val bookmarks by viewModel.bookmarks.collectAsStateWithLifecycle()

    // Dialog states
    val showCreateFolderDialog by viewModel.showCreateFolderDialog.collectAsStateWithLifecycle()
    val showCreateFileDialog by viewModel.showCreateFileDialog.collectAsStateWithLifecycle()
    val fileToRename by viewModel.fileToRename.collectAsStateWithLifecycle()
    val filesToDelete by viewModel.filesToDelete.collectAsStateWithLifecycle()
    val fileForDetails by viewModel.fileForDetails.collectAsStateWithLifecycle()
    val fileForTextEdit by viewModel.fileForTextEdit.collectAsStateWithLifecycle()
    val textEditContent by viewModel.textEditContent.collectAsStateWithLifecycle()
    val fileForImagePreview by viewModel.fileForImagePreview.collectAsStateWithLifecycle()
    val filesToZip by viewModel.filesToZip.collectAsStateWithLifecycle()
    val fileForPdfViewer by viewModel.fileForPdfViewer.collectAsStateWithLifecycle()
    val fileForVideoPlayer by viewModel.fileForVideoPlayer.collectAsStateWithLifecycle()
    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()
    val transferState by viewModel.transferState.collectAsStateWithLifecycle()
    val fileConflict by viewModel.fileConflict.collectAsStateWithLifecycle()

    var isSearchExpanded by remember { mutableStateOf(false) }
    var sortMenuExpanded by remember { mutableStateOf(false) }
    var viewMenuExpanded by remember { mutableStateOf(false) }
    var fabMenuExpanded by remember { mutableStateOf(false) }
    var bookmarkToDelete by remember { mutableStateOf<BookmarkItem?>(null) }
    var showAddCurrentFolderBookmarkDialog by remember { mutableStateOf(false) }

    // Observe lifecycle: whenever the app is resumed (e.g. returning from system All Files Access settings), re-check permission
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.checkPermission()
                viewModel.refreshStorageStats()
                viewModel.loadStorageVolumes()
                viewModel.loadCurrentDirectory()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Permission launcher for older Android
    val legacyPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        viewModel.checkPermission()
        viewModel.loadCurrentDirectory()
    }

    fun requestFullStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                    data = Uri.parse("package:${context.packageName}")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            }
        } else {
            legacyPermissionLauncher.launch(
                arrayOf(
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            )
        }
    }

    // Handle back button: if browsing files or category, return to parent or home
    BackHandler(enabled = isBrowsingFiles || selectedCategory != null || isSearchExpanded) {
        when {
            isSearchExpanded -> {
                isSearchExpanded = false
                viewModel.setSearchQuery("")
            }
            selectedCategory != null -> {
                viewModel.setCategory(null)
                viewModel.exitFileBrowser()
            }
            breadcrumbs.size > 1 -> {
                viewModel.navigateToParent()
            }
            else -> {
                viewModel.exitFileBrowser()
            }
        }
    }

    // Show status messages
    LaunchedEffect(statusMessage) {
        statusMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissStatusMessage()
        }
    }

    val isSelectionMode = selectedPaths.isNotEmpty()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    if (isSearchExpanded) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.setSearchQuery(it) },
                            placeholder = { Text("Search files & folders...", fontSize = 14.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear search")
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("search_text_field")
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isBrowsingFiles || selectedCategory != null) {
                                IconButton(
                                    onClick = {
                                        if (breadcrumbs.size > 1 && selectedCategory == null) {
                                            viewModel.navigateToParent()
                                        } else {
                                            viewModel.exitFileBrowser()
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = Icons.Default.FolderOpen,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(26.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                            }

                            Column {
                                Text(
                                    text = if (isBrowsingFiles) {
                                        if (selectedCategory != null) selectedCategory!!.titleEn else currentDirectory.name.ifEmpty { "Files" }
                                    } else {
                                        "File Manager"
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isBrowsingFiles) {
                                        if (selectedCategory != null) "${files.size} items found" else "${files.size} items"
                                    } else {
                                        "Professional Storage & Media"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            try {
                                context.startActivity(
                                    Intent().setComponent(
                                        android.content.ComponentName(
                                            "com.google.android.documentsui",
                                            "com.android.documentsui.files.FilesActivity"
                                        )
                                    )
                                )
                            } catch (e: Exception) {
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        "Unable to open system Files"
                                    )
                                }
                            }
                        },
                        modifier = Modifier.testTag("open_system_files_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = "System Files"
                        )
                    }

                    IconButton(
                        onClick = {
                            isSearchExpanded = !isSearchExpanded
                            if (!isSearchExpanded) {
                                viewModel.setSearchQuery("")
                            }
                        },
                        modifier = Modifier.testTag("toggle_search_button")
                    ) {
                        Icon(
                            imageVector = if (isSearchExpanded) Icons.Default.Clear else Icons.Default.Search,
                            contentDescription = "Search"
                        )
                    }

                    if (isBrowsingFiles) {
                        // Sort menu
                        Box {
                            IconButton(
                                onClick = { sortMenuExpanded = true },
                                modifier = Modifier.testTag("sort_menu_button")
                            ) {
                                Icon(imageVector = Icons.Default.Sort, contentDescription = "Sort")
                            }

                            DropdownMenu(
                                expanded = sortMenuExpanded,
                                onDismissRequest = { sortMenuExpanded = false }
                            ) {
                                SortOption.values().forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option.titleEn) },
                                        trailingIcon = {
                                            if (sortOption == option) {
                                                Icon(
                                                    Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        },
                                        onClick = {
                                            viewModel.setSortOption(option)
                                            sortMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // View Mode Selection
                        Box {
                            IconButton(
                                onClick = { viewMenuExpanded = true },
                                modifier = Modifier.testTag("toggle_view_mode_button")
                            ) {
                                Icon(
                                    imageVector = if (viewMode == ViewMode.LIST) Icons.Default.ViewList else Icons.Default.GridView,
                                    contentDescription = "View Mode"
                                )
                            }

                            DropdownMenu(
                                expanded = viewMenuExpanded,
                                onDismissRequest = { viewMenuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("نمایش لیستی (List)") },
                                    leadingIcon = {
                                        Icon(Icons.Default.ViewList, contentDescription = null)
                                    },
                                    trailingIcon = {
                                        if (viewMode == ViewMode.LIST) {
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    },
                                    onClick = {
                                        viewModel.setViewMode(ViewMode.LIST)
                                        viewMenuExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("نمایش شبکه‌ای (Grid)") },
                                    leadingIcon = {
                                        Icon(Icons.Default.GridView, contentDescription = null)
                                    },
                                    trailingIcon = {
                                        if (viewMode == ViewMode.GRID) {
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    },
                                    onClick = {
                                        viewModel.setViewMode(ViewMode.GRID)
                                        viewMenuExpanded = false
                                    }
                                )
                            }
                        }

                        // Hidden files toggle
                        IconButton(
                            onClick = { viewModel.toggleShowHiddenFiles() },
                            modifier = Modifier.testTag("toggle_hidden_files_button")
                        ) {
                            Icon(
                                imageVector = if (showHiddenFiles) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Hidden Files",
                                tint = if (showHiddenFiles) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        // Refresh button on main screen
                        IconButton(
                            onClick = {
                                viewModel.refreshStorageStats()
                                viewModel.loadCurrentDirectory()
                            },
                            modifier = Modifier.testTag("refresh_home_button")
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                AnimatedVisibility(visible = fabMenuExpanded) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        FloatingActionButton(
                            onClick = {
                                fabMenuExpanded = false
                                viewModel.requestCreateFolder()
                            },
                            shape = CircleShape,
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("fab_create_folder")
                        ) {
                            Icon(Icons.Default.CreateNewFolder, contentDescription = "Create Folder")
                        }

                        FloatingActionButton(
                            onClick = {
                                fabMenuExpanded = false
                                viewModel.requestCreateFile()
                            },
                            shape = CircleShape,
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("fab_create_file")
                        ) {
                            Icon(Icons.Default.NoteAdd, contentDescription = "Create Text File")
                        }
                    }
                }

                FloatingActionButton(
                    onClick = { fabMenuExpanded = !fabMenuExpanded },
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("fab_main_add")
                ) {
                    Icon(
                        imageVector = if (fabMenuExpanded) Icons.Default.Clear else Icons.Default.Add,
                        contentDescription = "New Actions"
                    )
                }
            }
        },
        bottomBar = {
            BottomActionBar(
                selectedCount = selectedPaths.size,
                clipboardState = clipboard,
                onSelectAll = { viewModel.selectAll() },
                onClearSelection = { viewModel.clearSelection() },
                onCopySelected = { viewModel.copySelected() },
                onCutSelected = { viewModel.cutSelected() },
                onDeleteSelected = {
                    val itemsToDelete = files.filter { selectedPaths.contains(it.path) }
                    viewModel.confirmDelete(itemsToDelete)
                },
                onShareSelected = {
                    val filesToShare = files.filter { selectedPaths.contains(it.path) }.map { it.file }
                    FileUtils.shareMultipleFiles(context, filesToShare)
                },
                onZipSelected = {
                    val filesToZipList = files.filter { selectedPaths.contains(it.path) }.map { it.file }
                    viewModel.requestZip(filesToZipList)
                },
                onPasteClipboard = { viewModel.pasteClipboard() },
                onClearClipboard = { viewModel.clearClipboard() }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Permission Banner if missing permission
            if (!hasStoragePermission) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "All Files Access Required",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = "Grant full storage permissions to browse and manage internal storage and USB OTG drives.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        androidx.compose.material3.Button(
                            onClick = { requestFullStoragePermission() },
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Grant")
                        }
                    }
                }
            }

            if (!isBrowsingFiles && searchQuery.isEmpty() && selectedCategory == null) {
                // ============================================================
                // HOME DASHBOARD (Single Tab Structure as specified):
                // 1. Bookmarks section
                // 2. Files section (Internal Storage & USB Storage with filled Capsule opposite drive name and stats underneath)
                // 3. Media & Documents section (no folder chips)
                // 4. Utility section (Text & PDF)
                // 5. Cloud section
                // ============================================================
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 80.dp)
                        .testTag("home_dashboard")
                ) {
                    // 1. Bookmarks Section (Flat, 2-column layout, long click to delete, + to add folder)
                    BookmarksSection(
                        bookmarks = bookmarks,
                        onBookmarkClick = { bookmark ->
                            viewModel.openBookmark(bookmark)
                        },
                        onBookmarkLongClick = { bookmark ->
                            bookmarkToDelete = bookmark
                        },
                        onAddBookmarkClick = {
                            showAddCurrentFolderBookmarkDialog = true
                        }
                    )

                    // 2. Files & Storage Section: Internal Storage always visible; USB/OTG drives appear when connected; small font underneath
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .testTag("files_storage_section")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Files & Storage",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Filter volumes: always show internal storage, and only connected removable/USB drives
                        val displayVolumes = if (storageVolumes.isNotEmpty()) {
                            storageVolumes.filter { volume ->
                                !volume.isRemovable || volume.file.exists()
                            }
                        } else {
                            emptyList()
                        }

                        if (displayVolumes.isNotEmpty()) {
                            displayVolumes.forEach { volume ->
                                StorageVolumeCard(
                                    volume = volume,
                                    onClick = { viewModel.openStorageVolume(volume) },
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        } else {
                            // Fallback default volume card (Internal Storage)
                            val fallbackVolume = com.example.model.StorageVolumeInfo(
                                name = "Internal Storage",
                                path = android.os.Environment.getExternalStorageDirectory().absolutePath,
                                file = android.os.Environment.getExternalStorageDirectory(),
                                isRemovable = false,
                                isUsbOtg = false,
                                stats = storageStats
                            )
                            StorageVolumeCard(
                                volume = fallbackVolume,
                                onClick = { viewModel.openStorageVolume(fallbackVolume) },
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }

                    // 3. Media & Documents Section (without folder section as requested)
                    MediaAndDocumentsSection(
                        selectedCategory = selectedCategory,
                        onCategorySelected = { category ->
                            viewModel.setCategory(category)
                        }
                    )

                    // 4. Utility Section (Text and PDF)
                    UtilitySection(
                        onNewTextFile = {
                            viewModel.requestCreateFile()
                        },
                        onOpenPdfSection = {
                            viewModel.setCategory(FileCategory.DOCUMENT)
                        }
                    )

                    // 5. Cloud Section
                    CloudSection(
                        onConnectCloud = { provider ->
                            scope.launch {
                                snackbarHostState.showSnackbar("Cloud service ($provider) is ready for configuration")
                            }
                        }
                    )
                }
            } else {
                // ============================================================
                // FILE BROWSER / SEARCH / CATEGORY VIEW
                // ============================================================
                // Breadcrumbs path bar (visible when browsing normal directory structure)
                if (selectedCategory == null) {
                    BreadcrumbBar(
                        breadcrumbs = breadcrumbs,
                        onHomeClick = { viewModel.returnToHome() },
                        onNavigate = { file -> viewModel.navigateTo(file) }
                    )
                }

                // File items list/grid or Empty state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    
if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else if (files.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (searchQuery.isNotEmpty()) {
                                    "No files matching your search"
                                } else if (selectedCategory != null) {
                                    "No files found in ${selectedCategory!!.titleEn}"
                                } else {
                                    "This folder is empty"
                                },
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        if (viewMode == ViewMode.LIST) {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(bottom = 80.dp)
                            ) {
                                items(
                                    items = files,
                                    key = { it.path }
                                ) { fileItem ->
                                    val isSelected = selectedPaths.contains(fileItem.path)
                                    FileListItem(
                                        fileItem = fileItem,
                                        isSelected = isSelected,
                                        isSelectionMode = isSelectionMode,
                                        onClick = {
                                            viewModel.openFileItem(fileItem, context)
                                        },
                                        onLongClick = {
                                            viewModel.toggleSelectFile(fileItem)
                                        },
                                        onSelectToggle = {
                                            viewModel.toggleSelectFile(fileItem)
                                        },
                                        onCopy = { viewModel.copyItem(fileItem) },
                                        onCut = { viewModel.cutItem(fileItem) },
                                        onRename = { viewModel.requestRename(fileItem) },
                                        onDelete = { viewModel.confirmDelete(listOf(fileItem)) },
                                        onShare = { FileUtils.shareFile(context, fileItem.file) },
                                        onDetails = { viewModel.showDetails(fileItem) },
                                        onZip = { viewModel.requestZip(listOf(fileItem.file)) },
                                        onExtractZip = { viewModel.extractZip(fileItem.file) },
                                        onOpenWith = { FileUtils.openFile(context, fileItem.file) },
                                        onAddBookmark = if (fileItem.isDirectory) {
                                            {
                                                viewModel.addCustomBookmark(
                                                    name = fileItem.name,
                                                    path = fileItem.path
                                                )
                                            }
                                        } else null
                                    )
                                }
                            }
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Adaptive(minSize = 100.dp),
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(start = 10.dp, top = 6.dp, end = 10.dp, bottom = 88.dp)
                            ) {
                                items(
                                    items = files,
                                    key = { it.path }
                                ) { fileItem ->
                                    val isSelected = selectedPaths.contains(fileItem.path)
                                    FileGridItem(
                                        fileItem = fileItem,
                                        isSelected = isSelected,
                                        isSelectionMode = isSelectionMode,
                                        onClick = {
                                            viewModel.openFileItem(fileItem, context)
                                        },
                                        onLongClick = {
                                            viewModel.toggleSelectFile(fileItem)
                                        },
                                        onSelectToggle = {
                                            viewModel.toggleSelectFile(fileItem)
                                        },
                                        onCopy = { viewModel.copyItem(fileItem) },
                                        onCut = { viewModel.cutItem(fileItem) },
                                        onRename = { viewModel.requestRename(fileItem) },
                                        onDelete = { viewModel.confirmDelete(listOf(fileItem)) },
                                        onShare = { FileUtils.shareFile(context, fileItem.file) },
                                        onDetails = { viewModel.showDetails(fileItem) },
                                        onZip = { viewModel.requestZip(listOf(fileItem.file)) },
                                        onExtractZip = { viewModel.extractZip(fileItem.file) },
                                        onOpenWith = { FileUtils.openFile(context, fileItem.file) },
                                        onAddBookmark = if (fileItem.isDirectory) {
                                            {
                                                viewModel.addCustomBookmark(
                                                    name = fileItem.name,
                                                    path = fileItem.path
                                                )
                                            }
                                        } else null
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    if (showCreateFolderDialog) {
        CreateFolderDialog(
            onConfirm = { viewModel.createFolder(it) },
            onDismiss = { viewModel.cancelCreateFolder() }
        )
    }

    if (showCreateFileDialog) {
        CreateFileDialog(
            onConfirm = { name, content -> viewModel.createFile(name, content) },
            onDismiss = { viewModel.cancelCreateFile() }
        )
    }

    fileToRename?.let { item ->
        RenameDialog(
            currentName = item.name,
            onConfirm = { newName -> viewModel.rename(item.file, newName) },
            onDismiss = { viewModel.cancelRename() }
        )
    }

    filesToDelete?.let { items ->
        DeleteConfirmDialog(
            items = items,
            onConfirm = { viewModel.deleteConfirmed() },
            onDismiss = { viewModel.cancelDelete() }
        )
    }

    fileForDetails?.let { item ->
        FileDetailsDialog(
            fileItem = item,
            onDismiss = { viewModel.closeDetails() }
        )
    }

    fileForTextEdit?.let { file ->
        TextEditorDialog(
            file = file,
            initialContent = textEditContent,
            onSave = { updated -> viewModel.saveTextContent(updated) },
            onClose = { viewModel.closeTextEditor() }
        )
    }

    fileForImagePreview?.let { file ->
        ImagePreviewDialog(
            file = file,
            onClose = { viewModel.closeImagePreview() }
        )
    }

    // Native PDF Viewer Dialog
    fileForPdfViewer?.let { file ->
        PdfViewerDialog(
            file = file,
            onClose = { viewModel.closePdfViewer() }
        )
    }

    // Native Video Player Dialog
    fileForVideoPlayer?.let { file ->
        VideoPlayerDialog(
            file = file,
            onClose = { viewModel.closeVideoPlayer() }
        )
    }

    filesToZip?.let { fileList ->
        val defaultName = if (fileList.size == 1) {
            "${fileList[0].nameWithoutExtension}.zip"
        } else {
            "archive_${System.currentTimeMillis()}.zip"
        }
        ZipDialog(
            defaultName = defaultName,
            onConfirm = { zipName -> viewModel.zipSelected(zipName) },
            onDismiss = { viewModel.cancelZip() }
        )
    }

    // Delete Bookmark Confirmation Dialog (triggered on long press on bookmark in Bookmarks section)
    bookmarkToDelete?.let { bookmark ->
        DeleteBookmarkDialog(
            bookmarkName = bookmark.name,
            onConfirm = {
                viewModel.removeBookmark(bookmark)
                bookmarkToDelete = null
            },
            onDismiss = {
                bookmarkToDelete = null
            }
        )
    }

    // Add Folder to Bookmarks Dialog
    if (showAddCurrentFolderBookmarkDialog) {
        val currentFolder = currentDirectory
        AddFolderBookmarkDialog(
            defaultName = currentFolder.name.ifEmpty { "My Folder" },
            folderPath = currentFolder.absolutePath,
            onConfirm = { customName ->
                viewModel.addCustomBookmark(name = customName, path = currentFolder.absolutePath)
                showAddCurrentFolderBookmarkDialog = false
            },
            onDismiss = {
                showAddCurrentFolderBookmarkDialog = false
            }
        )
    }

    // Windows 11 File Transfer Dialog
    Windows11TransferDialog(
        state = transferState,
        onPauseToggle = { viewModel.toggleTransferPause() },
        onCancel = { viewModel.cancelTransfer() },
        onDismiss = { viewModel.dismissTransferDialog() }
    )

    // Dialogs
    fileConflict?.let { (source, destination) ->
        FileConflictDialog(
            source = source,
            destination = destination,
            onOverwrite = {
                viewModel.resolveFileConflict(
                    com.example.util.FileConflictAction.OVERWRITE
                )
            },
            onSkip = {
                viewModel.resolveFileConflict(
                    com.example.util.FileConflictAction.SKIP
                )
            },
            onRename = {
                viewModel.resolveFileConflict(
                    com.example.util.FileConflictAction.RENAME
                )
            }
        )
    }
}
