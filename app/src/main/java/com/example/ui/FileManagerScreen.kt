package com.example.ui

import com.example.model.FileItem

import android.view.Gravity
import android.view.WindowManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider


import androidx.compose.foundation.border
import androidx.compose.foundation.layout.offset
import com.example.ui.components.BreathingActionIcon
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Typography
import android.content.Intent
import android.net.Uri
import java.io.File
import android.os.Build
import android.provider.Settings
import kotlin.math.roundToInt
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.AlertDialog
import androidx.compose.ui.draw.clip
import androidx.compose.material3.Button
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material3.TextButton
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import com.example.ui.theme.ThemeBlue
import com.example.ui.theme.ThemeGreen
import com.example.ui.theme.ThemePurple
import com.example.ui.theme.ThemeOrange
import com.example.ui.theme.ThemeRose

import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Slider
import coil.compose.AsyncImage
import coil.request.ImageRequest

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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.TransformOrigin
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
import com.example.ui.components.FileActionBar
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
import com.example.ui.components.FolderDetailsDialog
import com.example.ui.components.FileGridItem
import com.example.ui.components.FileListItem
import com.example.ui.components.ImagePreviewDialog
import com.example.ui.components.MediaAndDocumentsSection
import com.example.ui.components.PdfViewerDialog
import com.example.ui.components.RenameDialog
import com.example.ui.components.StorageVolumeCard
import com.example.ui.components.TextEditorDialog
import com.example.ui.components.TextViewerDialog
import com.example.ui.components.UtilitySection
import com.example.ui.components.VideoPlayerDialog
import com.example.ui.components.Windows11TransferDialog
import com.example.ui.components.ZipDialog
import com.example.util.FileUtils
import com.example.viewmodel.FileManagerViewModel
import androidx.compose.material3.Divider
import com.example.ui.icons.LucideIcons
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.zIndex
import androidx.compose.material3.ButtonDefaults
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileManagerScreen(
    darkTheme: Boolean = false,
    viewModel: FileManagerViewModel = viewModel(),
    onThemeChange: (Boolean) -> Unit = {},
    onThemeColorChange: (Int) -> Unit = {},
    themeColor: Int = 0,
    onCustomImagePick: () -> Unit = {},
    onCustomBlurChange: (Float) -> Unit = {},
    onCustomImageApply: () -> Unit = {}, onCustomImageCancel: () -> Unit = {},
    onCustomOpacityChange: (Float) -> Unit = {}, pendingCustomBackgroundUri: File? = null, customBackgroundUri: File? = null, customBackgroundBlur: Float = 0f, customBackgroundOpacity: Float = 1f,
    homeBackgroundEnabled: Boolean = true,
    onHomeBackgroundPick: () -> Unit = {},
    homeBackgroundFile: File? = null,
    homeBackgroundVersion: Int = 0,
    onHomeBackgroundChange: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val listScrollState = androidx.compose.foundation.lazy.rememberLazyListState()
    val gridScrollState = androidx.compose.foundation.lazy.grid.rememberLazyGridState()

    val currentDirectory by viewModel.currentDirectory.collectAsStateWithLifecycle()
    val breadcrumbs by viewModel.breadcrumbs.collectAsStateWithLifecycle()
    val virtualZipArchive by viewModel.virtualZipArchive.collectAsStateWithLifecycle()
    val virtualZipPath by viewModel.virtualZipPath.collectAsStateWithLifecycle()

    LaunchedEffect(
        currentDirectory.absolutePath,
        virtualZipArchive?.absolutePath,
        virtualZipPath
    ) {
        listScrollState.scrollToItem(0)
        gridScrollState.scrollToItem(0)
    }
    val files by viewModel.files.collectAsStateWithLifecycle()
    val imageFiles = remember(files) { files.filter { !it.isDirectory && FileUtils.isImageFile(it.file) } }
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val directoryContentsReady by viewModel.directoryContentsReady.collectAsStateWithLifecycle()
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
    val fileForTextViewer by viewModel.fileForTextViewer.collectAsStateWithLifecycle()
    val textViewerContent by viewModel.textViewerContent.collectAsStateWithLifecycle()
    val fileForTextEdit by viewModel.fileForTextEdit.collectAsStateWithLifecycle()
    val textEditContent by viewModel.textEditContent.collectAsStateWithLifecycle()
    val fileForImagePreview by viewModel.fileForImagePreview.collectAsStateWithLifecycle()
    val filesToZip by viewModel.filesToZip.collectAsStateWithLifecycle()
    val fileForPdfViewer by viewModel.fileForPdfViewer.collectAsStateWithLifecycle()
    val fileForVideoPlayer by viewModel.fileForVideoPlayer.collectAsStateWithLifecycle()
    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()
    val transferState by viewModel.transferState.collectAsStateWithLifecycle()
    val fileConflict by viewModel.fileConflict.collectAsStateWithLifecycle()
val archiveProgress by viewModel.archiveProgress.collectAsStateWithLifecycle()
val archiveTitle by viewModel.archiveTitle.collectAsStateWithLifecycle()
val deleteProgress by viewModel.deleteProgress.collectAsStateWithLifecycle()
    val archivePasswordZip by viewModel.archivePasswordZip.collectAsStateWithLifecycle()
    val virtualZipPasswordFile by viewModel.virtualZipPasswordFile.collectAsStateWithLifecycle()

    var isSearchExpanded by remember { mutableStateOf(false) }
    var sortMenuExpanded by remember { mutableStateOf(false) }
    var showMultiSelectionDetails by remember { mutableStateOf(false) }
    var folderDetailsTarget by remember { mutableStateOf<FileItem?>(null) }
    var viewMenuExpanded by remember { mutableStateOf(false) }
    val listNamePrefs = remember(context) {
        context.getSharedPreferences("fff_settings", android.content.Context.MODE_PRIVATE)
    }
    var animateListNames by remember {
        mutableStateOf(listNamePrefs.getBoolean("animate_list_names", true))
    }
    var settingsMenuExpanded by remember { mutableStateOf(false) }
    var customThemeDialogVisible by remember { mutableStateOf(false) }
    var bookmarkToDelete by remember { mutableStateOf<BookmarkItem?>(null) }
    var showAddCurrentFolderBookmarkDialog by remember { mutableStateOf(false) }

    LaunchedEffect(transferState.isFinished, transferState.isCancelled) {
        if (transferState.isFinished || transferState.isCancelled) {
            viewModel.dismissTransferDialog()
        }
    }

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

    // Handle back button: normal folders and virtual ZIP folders
    BackHandler(
        enabled = isBrowsingFiles ||
            selectedCategory != null ||
            isSearchExpanded ||
            virtualZipArchive != null
    ) {
        when {
            selectedPaths.isNotEmpty() -> {
                viewModel.clearSelection()
            }

            isSearchExpanded -> {
                isSearchExpanded = false
                viewModel.setSearchQuery("")
            }

            selectedCategory != null -> {
                viewModel.setCategory(null)
                viewModel.exitFileBrowser()
            }

            virtualZipArchive != null -> {
                // ZIP navigation has its own virtual path hierarchy.
                // navigateToParent() handles both:
                // ZIP subfolder -> parent ZIP folder
                // ZIP root -> normal filesystem folder
                viewModel.navigateToParent()
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

    val photoMode = homeBackgroundEnabled && homeBackgroundFile != null

    Box(modifier = Modifier.fillMaxSize()) {
        if (photoMode) {
            AsyncImage(
                model = homeBackgroundFile?.let { file ->
                    ImageRequest.Builder(context)
                        .data(file)
                        .memoryCacheKey("home_background_${homeBackgroundVersion}_${file.lastModified()}_${file.length()}")
                        .diskCacheKey("home_background_${homeBackgroundVersion}_${file.lastModified()}_${file.length()}")
                        .crossfade(false)
                        .build()
                },
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = customBackgroundOpacity))
            )
        }

        PhotoUiTheme(enabled = photoMode) {
            Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    if (isSearchExpanded) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { viewModel.setSearchQuery(it) },
                                placeholder = { Text("Search files & folders...", fontSize = 12.sp) },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                            Icon(LucideIcons.X, contentDescription = "Clear search")
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .testTag("search_text_field")
                            )
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isBrowsingFiles || selectedCategory != null) {
                                IconButton(
                                    onClick = {
                                        when {
                                            selectedPaths.isNotEmpty() -> {
                                                viewModel.clearSelection()
                                            }
                                            selectedCategory != null -> {
                                                viewModel.setCategory(null)
                                                viewModel.exitFileBrowser()
                                            }
                                            virtualZipArchive != null -> {
                                                viewModel.navigateToParent()
                                            }
                                            breadcrumbs.size > 1 -> {
                                                viewModel.navigateToParent()
                                            }
                                            else -> {
                                                viewModel.exitFileBrowser()
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = LucideIcons.ArrowLeft,
                                        contentDescription = "Back",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            } else {
                                Text(
                                    text = "MY PHONE",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                },
                actions = {

                    if (!isSearchExpanded) {
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
                            imageVector = LucideIcons.FolderOpen,
                            contentDescription = "System Files"
                        )
                    }
                    }

                    if (isBrowsingFiles && !isSearchExpanded) {
                        IconButton(
                            onClick = { viewModel.requestCreateFolder() },
                            modifier = Modifier.testTag("top_create_folder_button")
                        ) {
                            BreathingActionIcon(
                                imageVector = LucideIcons.FolderPlus,
                                contentDescription = "Create Folder",
                                enabled = true,
                                color = FileCategory.FOLDER.color
                            )
                        }

                        IconButton(
                            onClick = { viewModel.requestCreateFile() },
                            modifier = Modifier.testTag("top_create_file_button")
                        ) {
                            BreathingActionIcon(
                                imageVector = LucideIcons.FilePlus,
                                contentDescription = "Create Text File",
                                enabled = true,
                                color = FileCategory.DOCUMENT.color
                            )
                        }
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
                            imageVector = if (isSearchExpanded) LucideIcons.X else LucideIcons.Search,
                            contentDescription = "Search"
                        )
                    }

                    if (isBrowsingFiles && !isSearchExpanded) {
                        // Sort menu
                        Box {
                            IconButton(
                                onClick = { sortMenuExpanded = true },
                                modifier = Modifier.testTag("sort_menu_button")
                            ) {
                                Icon(imageVector = LucideIcons.ListFilter, contentDescription = "Sort")
                            }

                            DropdownMenu(
                                expanded = sortMenuExpanded,
                                onDismissRequest = { sortMenuExpanded = false },
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                            ) {
                                SortOption.values().forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option.titleEn, color = if (photoMode) Color.White else MaterialTheme.colorScheme.onSurface) },
                                        trailingIcon = {
                                            if (sortOption == option) {
                                                Icon(
                                                    LucideIcons.Check,
                                                    contentDescription = null,
                                                    tint = if (photoMode) Color.White else MaterialTheme.colorScheme.primary
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
                                    imageVector = if (viewMode == ViewMode.LIST) LucideIcons.List else LucideIcons.Grid2x2,
                                    contentDescription = "View Mode"
                                )
                            }

                            DropdownMenu(
                                expanded = viewMenuExpanded,
                                onDismissRequest = { viewMenuExpanded = false },
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                            ) {
                                DropdownMenuItem(
                                    text = { Text("List view (List)", color = if (photoMode) Color.White else MaterialTheme.colorScheme.onSurface) },
                                    leadingIcon = {
                                        Icon(
                                            LucideIcons.List,
                                            contentDescription = null,
                                            tint = if (photoMode) Color.White else MaterialTheme.colorScheme.onSurface
                                        )
                                    },
                                    trailingIcon = {
                                        if (viewMode == ViewMode.LIST) {
                                            Icon(
                                                LucideIcons.Check,
                                                contentDescription = null,
                                                tint = if (photoMode) Color.White else MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    },
                                    onClick = {
                                        viewModel.setViewMode(ViewMode.LIST)
                                        viewMenuExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Grid view (Grid)", color = if (photoMode) Color.White else MaterialTheme.colorScheme.onSurface) },
                                    leadingIcon = {
                                        Icon(
                                            LucideIcons.Grid2x2,
                                            contentDescription = null,
                                            tint = if (photoMode) Color.White else MaterialTheme.colorScheme.onSurface
                                        )
                                    },
                                    trailingIcon = {
                                        if (viewMode == ViewMode.GRID) {
                                            Icon(
                                                LucideIcons.Check,
                                                contentDescription = null,
                                                tint = if (photoMode) Color.White else MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    },
                                    onClick = {
                                        viewModel.setViewMode(ViewMode.GRID)
                                        viewMenuExpanded = false
                                    }
                                )
                                if (viewMode == ViewMode.LIST) {
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                if (animateListNames) "Line wrap" else "Animation",
                                                color = if (photoMode) Color.White
                                                    else MaterialTheme.colorScheme.onSurface
                                            )
                                        },
                                        onClick = {
                                            val newValue = !animateListNames
                                            animateListNames = newValue
                                            listNamePrefs.edit()
                                                .putBoolean("animate_list_names", newValue)
                                                .apply()
                                            viewMenuExpanded = false
                                        }
                                    )
                                }

                            }
                        }


                    } else if (!isSearchExpanded) {
                        // Refresh button on main screen
                        IconButton(
                            onClick = {
                                viewModel.refreshStorageStats()
                                viewModel.loadCurrentDirectory()
                            },
                            modifier = Modifier.testTag("refresh_home_button")
                        ) {
                            Icon(imageVector = LucideIcons.RefreshCw, contentDescription = "Refresh")
                        }
                    }
                
                        if (!isSearchExpanded) {
                        // Settings
                        Box {
                            IconButton(
                                onClick = { settingsMenuExpanded = true },
                                modifier = Modifier.testTag("settings_button")
                            ) {
                                Icon(
                                    imageVector = LucideIcons.Settings,
                                    contentDescription = "Settings",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier
                                        .size(24.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = settingsMenuExpanded,
                                onDismissRequest = { settingsMenuExpanded = false },
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Hidden Files", color = if (photoMode) Color.White else MaterialTheme.colorScheme.onSurface) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = if (showHiddenFiles) {
                                                LucideIcons.Eye
                                            } else {
                                                LucideIcons.EyeOff
                                            },
                                            contentDescription = "Hidden Files",
                                            tint = if (photoMode) {
                                                Color.White
                                            } else if (showHiddenFiles) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                            }
                                        )
                                    },
                                    onClick = {
                                        viewModel.toggleShowHiddenFiles()
                                        settingsMenuExpanded = false
                                    }
                                )

    DropdownMenuItem(
                                    text = { Text("Light", color = if (photoMode) Color.White else MaterialTheme.colorScheme.onSurface) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Filled.LightMode,
                                            contentDescription = "Light",
                                            tint = if (photoMode) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    },
                                    onClick = {
                                        onThemeChange(false)
                                        settingsMenuExpanded = false
                                    }
                                )

                                DropdownMenuItem(
                                    text = { Text("Dark", color = if (photoMode) Color.White else MaterialTheme.colorScheme.onSurface) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Filled.DarkMode,
                                            contentDescription = "Dark",
                                            tint = if (photoMode) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    },
                                    onClick = {
                                        onThemeChange(true)
                                        settingsMenuExpanded = false
                                    }
                                )

                                DropdownMenuItem(
                                    modifier = Modifier.background(if (themeColor == 0) ThemeBlue.copy(alpha = 0.18f) else Color.Transparent),
                                    text = { Text("Blue", color = if (photoMode) Color.White else MaterialTheme.colorScheme.onSurface) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Filled.Circle,
                                            contentDescription = "Blue",
                                            tint = ThemeBlue
                                        )
                                    },
                                    onClick = {
                                        onThemeColorChange(0)
                                        settingsMenuExpanded = false
                                    }
                                )

                                DropdownMenuItem(
                                    modifier = Modifier.background(if (themeColor == 1) ThemeGreen.copy(alpha = 0.18f) else Color.Transparent),
                                    text = { Text("Green", color = if (photoMode) Color.White else MaterialTheme.colorScheme.onSurface) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Filled.Circle,
                                            contentDescription = "Green",
                                            tint = ThemeGreen
                                        )
                                    },
                                    onClick = {
                                        onThemeColorChange(1)
                                        settingsMenuExpanded = false
                                    }
                                )

                                DropdownMenuItem(
                                    modifier = Modifier.background(if (themeColor == 2) ThemePurple.copy(alpha = 0.18f) else Color.Transparent),
                                    text = { Text("Purple", color = if (photoMode) Color.White else MaterialTheme.colorScheme.onSurface) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Filled.Circle,
                                            contentDescription = "Purple",
                                            tint = ThemePurple
                                        )
                                    },
                                    onClick = {
                                        onThemeColorChange(2)
                                        settingsMenuExpanded = false
                                    }
                                )

                                DropdownMenuItem(
                                    modifier = Modifier.background(if (themeColor == 3) ThemeOrange.copy(alpha = 0.18f) else Color.Transparent),
                                    text = { Text("Orange", color = if (photoMode) Color.White else MaterialTheme.colorScheme.onSurface) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Filled.Circle,
                                            contentDescription = "Orange",
                                            tint = ThemeOrange
                                        )
                                    },
                                    onClick = {
                                        onThemeColorChange(3)
                                        settingsMenuExpanded = false
                                    }
                                )

                                DropdownMenuItem(
                                    modifier = Modifier.background(if (themeColor == 4) ThemeRose.copy(alpha = 0.18f) else Color.Transparent),
                                    text = { Text("Rose", color = if (photoMode) Color.White else MaterialTheme.colorScheme.onSurface) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Filled.Circle,
                                            contentDescription = "Rose",
                                            tint = ThemeRose
                                        )
                                    },
                                    onClick = {
                                        onThemeColorChange(4)
                                        settingsMenuExpanded = false
                                    }
                                )

                                DropdownMenuItem(
                                    text = { Text("Custom", color = if (photoMode) Color.White else MaterialTheme.colorScheme.onSurface) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Filled.Image,
                                            contentDescription = "Custom",
                                            tint = if (photoMode) Color.White else MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    onClick = {
                                        customThemeDialogVisible = true
                                        settingsMenuExpanded = false
                                    }
                                )
                            }
                        }
                }
},
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
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

                    Spacer(modifier = Modifier.height(17.dp))

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
                                imageVector = LucideIcons.Folder,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Files & Storage",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = MaterialTheme.colorScheme.onSurface
            )
                        }

                        Divider(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
                            thickness = 1.dp
                        )

                        // Show only Internal Storage and connected USB OTG drives.
                        // Other removable/external storage volumes are hidden.
                        val displayVolumes = storageVolumes.filter { volume ->
                            !volume.isRemovable || (volume.isUsbOtg && volume.file.exists())
                        }

                        if (displayVolumes.isNotEmpty()) {
                            displayVolumes.forEach { volume ->
                                StorageVolumeCard(
                                    volume = volume,
                                    onClick = { viewModel.openStorageVolume(volume) },
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    isLightTheme = !darkTheme && !photoMode,
                                    isCustomTheme = photoMode
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
                                modifier = Modifier.padding(vertical = 4.dp),
                                isLightTheme = !darkTheme && !photoMode,
                                isCustomTheme = photoMode
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(17.dp))

                    // 3. Media & Documents Section (without folder section as requested)
                    MediaAndDocumentsSection(
                        selectedCategory = selectedCategory,
                        onCategorySelected = { category ->
                            viewModel.setCategory(category)
                        }
                    )

                    Spacer(modifier = Modifier.height(17.dp))

                    // 4. Utility Section (Text and PDF)
                    UtilitySection(
                        onNewTextFile = {
                            viewModel.requestCreateFile()
                        },
                        onOpenPdfSection = {
                            viewModel.setCategory(FileCategory.DOCUMENT)
                        }
                    )

                    Spacer(modifier = Modifier.height(17.dp))

                    // 5. Cloud Section
                    CloudSection(
                        onConnectCloud = { provider ->
                            val uri = when (provider) {
                                "Google Drive" -> Uri.parse("https://drive.google.com/drive/")
                                "OneDrive" -> Uri.parse("ms-onedrive://")
                                else -> null
                            }

                            if (uri != null) {
                                context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                            }
                        }
                    )
                    }
            } else {
                // ============================================================
                // FILE BROWSER / SEARCH / CATEGORY VIEW
                // ============================================================
                // Selected files
                val selectedItems = files.filter { selectedPaths.contains(it.path) }
                val selectedItem = selectedItems.singleOrNull()
                val selectedZip = selectedItem?.let { item ->
                    item.virtualZipArchive == null &&
                        item.file.isFile &&
                        item.file.extension.equals("zip", ignoreCase = true)
                } == true

                // Breadcrumbs path bar (visible when browsing normal directory structure)
                if (selectedCategory == null) {
                    BreadcrumbBar(
                        breadcrumbs = breadcrumbs,
                        isUsbPath = storageVolumes.any { volume ->
                            volume.isUsbOtg &&
                                (currentDirectory.absoluteFile.normalize().path ==
                                    volume.file.absoluteFile.normalize().path ||
                                 currentDirectory.absoluteFile.normalize().path.startsWith(
                                    volume.file.absoluteFile.normalize().path.trimEnd('/') + "/"
                                 ))
                        },
                        virtualZipName = virtualZipArchive?.name,
                        virtualZipPath = virtualZipPath,
                        onVirtualZipNavigate = { path -> viewModel.navigateVirtualZipTo(path) },
                        onVirtualZipExit = { viewModel.navigateToParent() },
                        onHomeClick = { viewModel.returnToHome() },
                        onNavigate = { file -> viewModel.navigateTo(file) },
                        pasteVisible = clipboard.hasItems && !selectedZip,
                        pasteCount = clipboard.items.size,
                        onPaste = { viewModel.pasteClipboard() },
                        onCancelPaste = { viewModel.clearClipboard() },
                        extractZipVisible = selectedZip,
                        onExtractZip = {
                            selectedItem?.let { viewModel.requestArchivePassword(it.file) }
                        },
                        onCancelExtractZip = {
                            viewModel.clearSelection()
                        }
                    )
                }


                // File items list/grid or Empty state
                val folderAnimationKey =
                    currentDirectory.absolutePath +
                        "|" +
                        (selectedCategory?.titleEn ?: "")

                val folderListScaleY =
                    remember { androidx.compose.animation.core.Animatable(1f) }
LaunchedEffect(folderAnimationKey) {
                    folderListScaleY.snapTo(0f)
                    androidx.compose.runtime.snapshotFlow { directoryContentsReady }
                        .first { it }
                    folderListScaleY.animateTo(
                        targetValue = 1f,
                        animationSpec = androidx.compose.animation.core.tween(550)
                    )
                    viewModel.calculateCurrentDirectoryChildCounts()
                }

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
                                imageVector = LucideIcons.FolderOpen,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(17.dp))
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
                                state = listScrollState,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer {
                                        scaleY = folderListScaleY.value
                                        transformOrigin = TransformOrigin(0.5f, 0f)
                                    },
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
                    onDetails = {
                        if (selectedPaths.size == 1) {
                            selectedItem?.let { viewModel.showDetails(it) }
                        } else if (selectedPaths.size > 1) {
                            showMultiSelectionDetails = true
                        }
                    },
                                        onFolderDetails = { if (fileItem.isDirectory) folderDetailsTarget = fileItem },
                                        onZip = { viewModel.requestZip(listOf(fileItem.file)) },
                        onExtractZip = { viewModel.requestArchivePassword(fileItem.file) },
                                        onOpenWith = { FileUtils.openFile(context, fileItem.file) },
                                        onAddBookmark = if (fileItem.isDirectory) {
                                            {
                                                viewModel.addCustomBookmark(
                                                    name = fileItem.name,
                                                    path = fileItem.path
                                                )
                                            }
                                        } else null,
                                        animateLongNames = animateListNames
                                    )
                                }
                            }
                            VerticalFastScrollbar(
                                state = listScrollState,
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .fillMaxHeight()
                            )
                        } else {
                            LazyVerticalGrid(
                                state = gridScrollState,
                                columns = GridCells.Adaptive(minSize = 100.dp),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer {
                                        scaleY = folderListScaleY.value
                                        transformOrigin = TransformOrigin(0.5f, 0f)
                                    },
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
                    onDetails = {
                        if (selectedPaths.size == 1) {
                            selectedItem?.let { viewModel.showDetails(it) }
                        } else if (selectedPaths.size > 1) {
                            showMultiSelectionDetails = true
                        }
                    },
                                        onFolderDetails = { if (fileItem.isDirectory) folderDetailsTarget = fileItem },
                                        onExtractZip = { viewModel.requestArchivePassword(fileItem.file) },
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
                

                        FileActionBar(
                            modifier = Modifier.align(Alignment.CenterEnd).offset(y = (-50).dp),
                            strongerDarkDivider = darkTheme && !photoMode,
                            selectionCount = selectedPaths.size,
                        isSingleSelectionDirectory = selectedItem?.isDirectory == true,
                        isSelectedZip = selectedZip,
                        onOpenWith = {
                            selectedItem?.let { FileUtils.openFile(context, it.file) }
                        },
                        onSelectAll = {
                            viewModel.selectAll()
                        },
                        onZip = {
                            if (selectedItem?.extension?.equals("zip", ignoreCase = true) == true) {
                                viewModel.requestArchivePassword(selectedItem.file)
                            } else {
                                viewModel.requestZip(selectedItems.map { it.file })
                            }
                        },
                        onBookmark = {
                            selectedItem?.takeIf { it.isDirectory }?.let {
                                viewModel.addCustomBookmark(
                                    name = it.name,
                                    path = it.path
                                )
                            }
                        },
                        onShare = {
                            FileUtils.shareMultipleFiles(
                                context,
                                selectedItems.map { it.file }
                            )
                        },
                        onCopy = { viewModel.copySelected() },
                        onCut = { viewModel.cutSelected() },
                        onRename = {
                            selectedItem?.let { viewModel.requestRename(it) }
                        },
                        onDetails = {
                            if (selectedPaths.size == 1) {
                                selectedItem?.let { viewModel.showDetails(it) }
                            } else if (selectedPaths.size > 1) {
                                showMultiSelectionDetails = true
                            }
                        },
                        onDelete = {
                            viewModel.confirmDelete(selectedItems)
                        }
                    )
}
            }
        }
    }


    if (showCreateFolderDialog) {
        CreateFolderDialog(
            onConfirm = { viewModel.createFolder(it) },
            onDismiss = { viewModel.cancelCreateFolder() },
            isCustomTheme = photoMode,
            isLightTheme = !darkTheme && !photoMode
        )
    }

    if (showCreateFileDialog) {
        CreateFileDialog(
            onConfirm = { name, content -> viewModel.createFile(name, content) },
            onDismiss = { viewModel.cancelCreateFile() },
            isCustomTheme = photoMode,
            isLightTheme = !darkTheme && !photoMode
        )
    }

    fileToRename?.let { item ->
        RenameDialog(
            currentName = item.name,
            onConfirm = { newName -> viewModel.rename(item.file, newName) },
            onDismiss = { viewModel.cancelRename() },
            isCustomTheme = photoMode,
            isLightTheme = !darkTheme && !photoMode
        )
    }

    filesToDelete?.let { items ->
        DeleteConfirmDialog(
            items = items,
            onConfirm = { viewModel.deleteConfirmed() },
            onDismiss = { viewModel.cancelDelete() },
            isCustomTheme = photoMode,
            isLightTheme = !darkTheme && !photoMode
        )
        }
    }

    folderDetailsTarget?.let { folder ->
        FolderDetailsDialog(
            fileItem = folder,
            onDismiss = { folderDetailsTarget = null },
            isCustomTheme = photoMode,
            isLightTheme = !darkTheme && !photoMode
        )
    }

    if (showMultiSelectionDetails) {
        val multiSelectedItems = files.filter { selectedPaths.contains(it.path) }
        val totalSize = multiSelectedItems.sumOf { it.size }

        Dialog(
            onDismissRequest = { showMultiSelectionDetails = false }
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(0.88f)
                        .border(
                            width = 1.dp,
                            color = if (photoMode) {
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                            shape = MaterialTheme.shapes.extraLarge
                        ),
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    tonalElevation = if (!darkTheme && !photoMode) 0.dp else 6.dp
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Selection Details",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text("Selected items: ${multiSelectedItems.size}")
                        Text("Total size: ${FileUtils.formatBytes(totalSize)}")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = { showMultiSelectionDetails = false }
                            ) {
                                Text("OK")
                            }
                        }
                    }
                }
            }
        }
    }

    fileForDetails?.let { item ->
        FileDetailsDialog(
            fileItem = item,
            onDismiss = { viewModel.closeDetails() },
            isCustomTheme = photoMode,
            isLightTheme = !darkTheme && !photoMode
        )
    }

    fileForTextViewer?.let { file ->
        TextViewerDialog(
            file = file,
            content = textViewerContent,
            onEdit = { viewModel.openTextEditorFromViewer() },
            onClose = { viewModel.closeTextViewer() }
        )
    }

    fileForTextEdit?.let { file ->
        TextEditorDialog(
            file = file,
            initialContent = textEditContent,
            onSave = { updated -> viewModel.saveTextContent(updated) },
            onSaveAs = { name, updated ->
                viewModel.saveTextContentAs(name, updated)
            },
            onClose = { viewModel.closeTextEditor() }
        )
    }

    fileForImagePreview?.let { file ->
        ImagePreviewDialog(
            initialFile = file,
            imageFiles = imageFiles.map { it.file },
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
            onConfirm = { zipName ->
                viewModel.zipSelected(zipName)
                viewModel.cancelZip()
            },
            onDismiss = { viewModel.cancelZip() },
            isCustomTheme = photoMode,
            isLightTheme = !darkTheme && !photoMode
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
            },
            isCustomTheme = photoMode,
            isLightTheme = !darkTheme && !photoMode
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
    var transferDialogDelayPassed by remember { mutableStateOf(false) }
    androidx.compose.runtime.LaunchedEffect(transferState.isVisible, fileConflict) {
        transferDialogDelayPassed = false
        if (transferState.isVisible && fileConflict == null) {
            kotlinx.coroutines.delay(300)
            transferDialogDelayPassed = true
        }
    }
    if (fileConflict == null && transferState.isVisible && transferDialogDelayPassed) {
        Windows11TransferDialog(
            state = transferState,
            onPauseToggle = { viewModel.toggleTransferPause() },
            onCancel = { viewModel.cancelTransfer() },
            onDismiss = { viewModel.dismissTransferDialog() },
            isCustomTheme = photoMode,
            isLightTheme = !darkTheme && !photoMode
        )
    }

    // Simple ZIP / Extract Progress Dialog
archiveProgress?.let { progress ->
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .border(
                        width = 1.dp,
                        color = if (photoMode) {
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                        shape = MaterialTheme.shapes.extraLarge
                    ),
                shape = MaterialTheme.shapes.extraLarge,
                tonalElevation = if (photoMode) 0.dp else 6.dp,
                color = if (photoMode) {
                    Color(0xFF3C0020).copy(alpha = 0.8f)
                } else if (!darkTheme) {
                    MaterialTheme.colorScheme.surfaceContainerHigh
                } else {
                    MaterialTheme.colorScheme.surface
                }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = archiveTitle ?: "Archive operation",
                        style = MaterialTheme.typography.headlineSmall
                    )

                    LinearProgressIndicator(
                        progress = { progress.coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "${(progress.coerceIn(0f, 1f) * 100).toInt()}%",
                        modifier = Modifier.align(Alignment.End)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = { viewModel.cancelArchiveOperation() }
                        ) {
                            Text("Cancel")
                        }
                    }
                }
            }
        }
    }
}

    // Delete Progress Dialog
    deleteProgress?.let { progress ->
        Dialog(
            onDismissRequest = {},
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp)
                        .border(
                            width = 1.dp,
                            color = if (photoMode) {
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                            shape = MaterialTheme.shapes.extraLarge
                        ),
                    shape = MaterialTheme.shapes.extraLarge,
                    tonalElevation = if (photoMode) 0.dp else 6.dp,
                    color = if (photoMode) {
                        Color(0xFF3C0020).copy(alpha = 0.8f)
                    } else if (!darkTheme) {
                        MaterialTheme.colorScheme.surfaceContainerHigh
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Deleting files",
                            style = MaterialTheme.typography.headlineSmall
                        )

                        LinearProgressIndicator(
                            progress = { progress.coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text(
                            text = "${(progress.coerceIn(0f, 1f) * 100).toInt()}%",
                            modifier = Modifier.align(Alignment.End)
                        )
                    }
                }
            }
        }
    }

    archivePasswordZip?.let { zipFile ->
        var password by remember(zipFile) { mutableStateOf("") }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(100f)
                .background(Color.Black.copy(alpha = 0.32f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}
                ),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .border(
                        width = 0.5.dp,
                        color = if (photoMode) {
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                        shape = MaterialTheme.shapes.extraLarge
                    ),
                shape = MaterialTheme.shapes.extraLarge,
                tonalElevation = if (photoMode) 0.dp else 6.dp,
                color = if (photoMode) {
                    Color(0xFF3C0020).copy(alpha = 0.8f)
                } else {
                    MaterialTheme.colorScheme.surface
                }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "ZIP Password",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        singleLine = true,
                        colors = if (photoMode) {
                            androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                        } else if (!darkTheme) {
                            androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            )
                        } else {
                            androidx.compose.material3.OutlinedTextFieldDefaults.colors()
                        },
                        visualTransformation = PasswordVisualTransformation()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = { viewModel.cancelArchivePassword() }
                        ) {
                            Text("Cancel")
                        }
                        TextButton(
                            onClick = {
                                if (password.isBlank()) {
                                    viewModel.extractZip(zipFile)
                                } else {
                                    viewModel.extractZipWithPassword(zipFile, password)
                                }
                            }
                        ) {
                            Text("Extract")
                        }
                    }
                }
            }
        }
    }

    virtualZipPasswordFile?.let { zipFile ->
        var password by remember(zipFile) { mutableStateOf("") }

        Dialog(
            onDismissRequest = { viewModel.cancelVirtualZipPassword() },
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp)
                    .border(
                        width = 0.5.dp,
                        color = if (photoMode) {
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                        shape = MaterialTheme.shapes.extraLarge
                    ),
                    shape = MaterialTheme.shapes.extraLarge,
                    tonalElevation = if (photoMode) 0.dp else 6.dp,
                    color = if (photoMode) {
                        Color(0xFF3C0020).copy(alpha = 0.8f)
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "ZIP Password",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password") },
                            singleLine = true,
                            colors = if (photoMode) {
                                androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                )
                            } else if (!darkTheme) {
                                androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                )
                            } else {
                                androidx.compose.material3.OutlinedTextFieldDefaults.colors()
                            },
                            visualTransformation = PasswordVisualTransformation()
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = { viewModel.cancelVirtualZipPassword() }
                            ) {
                                Text("Cancel")
                            }
                            TextButton(
                                onClick = {
                                    if (password.isNotBlank()) {
                                        viewModel.openPasswordProtectedZip(zipFile, password)
                                    }
                                }
                            ) {
                                Text("Open")
                            }
                        }
                    }
                }
            }
        }
    }

    if (customThemeDialogVisible) {
        MaterialTheme(
            colorScheme = if (photoMode) lightColorScheme(
                primary = Color.White,
                onPrimary = Color.Black,
                secondary = Color.White,
                onSecondary = Color.Black,
                surface = Color(0xFF3C0020).copy(alpha = 0.8f),
                onSurface = Color.White,
                background = Color.Transparent,
                onBackground = Color.White,
                surfaceContainerHigh = Color(0xFF3C0020).copy(alpha = 0.8f),
                surfaceVariant = Color.Black.copy(alpha = 0.35f),
                onSurfaceVariant = Color.White.copy(alpha = 0.9f),
                outline = Color.White.copy(alpha = 0.5f)
            ) else MaterialTheme.colorScheme
        ) {
            AlertDialog(
modifier = Modifier.border(
    width = 0.5.dp,
    color = when {
        photoMode -> Color.White.copy(alpha = 0.5f)
        darkTheme -> MaterialTheme.colorScheme.primary
        else -> Color.Transparent
    },
    shape = MaterialTheme.shapes.extraLarge
),
                containerColor = when {
                    photoMode -> Color(0xFF3C0020).copy(alpha = 0.8f)
                    darkTheme -> MaterialTheme.colorScheme.surfaceContainerHigh
                    else -> MaterialTheme.colorScheme.surfaceContainerHigh
                },
            onDismissRequest = {
                onCustomImageCancel()
                customThemeDialogVisible = false
            },
            confirmButton = {},
            title = {
                Text("Photo Theme")
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = LucideIcons.Image,
                                contentDescription = "Home Background",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Home Background")
                        }
                        IconButton(
                            onClick = {
                                onHomeBackgroundChange(!homeBackgroundEnabled)
                            }
                        ) {
                            Icon(
                                imageVector = if (homeBackgroundEnabled) {
                                    Icons.Filled.Visibility
                                } else {
                                    Icons.Filled.VisibilityOff
                                },
                                contentDescription = if (homeBackgroundEnabled) "Disable Home Background" else "Enable Home Background",
                                tint = if (homeBackgroundEnabled) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }

                    if (homeBackgroundFile != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(RoundedCornerShape(12.dp))
                        ) {
                            AsyncImage(
                                model = homeBackgroundFile?.let { file ->
                                    ImageRequest.Builder(context)
                                        .data(file)
                                        .memoryCacheKey("home_background_preview_${homeBackgroundVersion}_${file.lastModified()}_${file.length()}")
                                        .diskCacheKey("home_background_preview_${homeBackgroundVersion}_${file.lastModified()}_${file.length()}")
                                        .crossfade(false)
                                        .build()
                                },
                                contentDescription = "Home Background preview",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Color.Black.copy(alpha = customBackgroundOpacity)
                                    )
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No picture selected")
                        }
                    }

                    Button(
                        onClick = { onHomeBackgroundPick() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = if (photoMode) {
                            ButtonDefaults.buttonColors(
                                containerColor = Color.White.copy(alpha = 0.15f),
                                contentColor = Color.White
                            )
                        } else {
                            ButtonDefaults.buttonColors()
                        }) {
                        Icon(
                            imageVector = Icons.Filled.Image,
                            contentDescription = null
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Choose Picture")
                    }

                    Text("Dim: ${(customBackgroundOpacity * 100f).roundToInt()}%")

                    Slider(
                        value = customBackgroundOpacity,
                        onValueChange = onCustomOpacityChange,
                        valueRange = 0f..1f,
                        steps = 99
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onCustomImageCancel()
                        customThemeDialogVisible = false
                    }
                ) {
                    Text("Close")
                }
            }
        )
    }

        }
    }

    // Dialogs
    fileConflict?.let { (source, destination) ->
        FileConflictDialog(
            source = source,
            destination = destination,
                isCustomTheme = photoMode,
            onOverwrite = { applyToAll ->
                viewModel.resolveFileConflict(
                    com.example.util.FileConflictAction.OVERWRITE,
                    applyToAll
                )
            },
            onSkip = { applyToAll ->
                viewModel.resolveFileConflict(
                    com.example.util.FileConflictAction.SKIP,
                    applyToAll
                )
            },
            onRename = { applyToAll ->
                viewModel.resolveFileConflict(
                    com.example.util.FileConflictAction.RENAME,
                    applyToAll
                )
            }
        )
    }
}

}

@Composable
private fun PhotoUiTheme(
    enabled: Boolean,
    content: @Composable () -> Unit
) {
    if (!enabled) {
        content()
        return
    }

    val photoColorScheme = lightColorScheme(
        primary = Color.White,
        onPrimary = Color.Black,
        secondary = Color.White,
        onSecondary = Color.Black,
        background = Color.Transparent,
        onBackground = Color.White,
        surface = Color.Transparent,
        onSurface = Color.White,
        surfaceContainerHigh = Color(0xFF3C0020).copy(alpha = 0.8f),
        surfaceVariant = Color.Black.copy(alpha = 0.35f),
        onSurfaceVariant = Color.White.copy(alpha = 0.9f),
        outline = Color.White.copy(alpha = 0.55f),
        error = Color(0xFFFF5252),
        onError = Color.White,
        errorContainer = Color.Black.copy(alpha = 0.55f),
        onErrorContainer = Color.White
    )

    MaterialTheme(
        colorScheme = photoColorScheme,
        typography = com.example.ui.theme.Typography,
        content = content
    )
}


@Composable
private fun VerticalFastScrollbar(
    state: androidx.compose.foundation.lazy.LazyListState,
    modifier: Modifier = Modifier
) {
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val dragFraction = androidx.compose.runtime.remember(state) {
        androidx.compose.runtime.mutableStateOf(0f)
    }

    val trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.14f)
    val thumbColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f)

    Canvas(
        modifier = modifier
            .width(16.dp)
            .pointerInput(state) {
                val pointerHeight = size.height.toFloat().coerceAtLeast(1f)
                val minThumbPx = 30.dp.toPx()

                detectVerticalDragGestures(
                    onDragStart = {
                        val info = state.layoutInfo
                        val total = info.totalItemsCount
                        val visible = info.visibleItemsInfo.size.coerceAtLeast(1)
                        val maxFirstIndex = (total - visible).coerceAtLeast(0)

                        dragFraction.value =
                            if (maxFirstIndex > 0) {
                                (state.firstVisibleItemIndex.toFloat() / maxFirstIndex)
                                    .coerceIn(0f, 1f)
                            } else {
                                0f
                            }
                    },
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()

                        val info = state.layoutInfo
                        val total = info.totalItemsCount
                        val visible = info.visibleItemsInfo.size.coerceAtLeast(1)
                        val maxFirstIndex = (total - visible).coerceAtLeast(0)

                        if (total > visible && maxFirstIndex > 0) {
                            val estimatedThumb = (
                                pointerHeight * visible / total.toFloat()
                            ).coerceIn(
                                minThumbPx.coerceAtMost(pointerHeight),
                                pointerHeight
                            )

                            val travel = (pointerHeight - estimatedThumb).coerceAtLeast(1f)

                            dragFraction.value = (
                                dragFraction.value + dragAmount / travel
                            ).coerceIn(0f, 1f)

                            val targetIndex = (
                                dragFraction.value * maxFirstIndex
                            ).toInt().coerceIn(0, maxFirstIndex)

                            scope.launch {
                                state.scrollToItem(targetIndex)
                            }
                        }
                    }
                )
            }
    ) {
        val info = state.layoutInfo
        val total = info.totalItemsCount
        val visible = info.visibleItemsInfo.size.coerceAtLeast(1)

        if (total <= visible || size.height <= 0f) {
            return@Canvas
        }

        val minThumb = 30.dp.toPx().coerceAtMost(size.height)
        val thumbHeight = (
            size.height * visible / total.toFloat()
        ).coerceIn(minThumb, size.height)

        val maxFirstIndex = (total - visible).coerceAtLeast(0)
        val fraction = if (maxFirstIndex > 0) {
            (state.firstVisibleItemIndex.toFloat() / maxFirstIndex).coerceIn(0f, 1f)
        } else {
            0f
        }

        val thumbTop = (size.height - thumbHeight) * fraction

        // The track reaches both ends without a bottom frame or container.
        drawRoundRect(
            color = trackColor,
            topLeft = Offset(size.width - 5.dp.toPx(), 0f),
            size = androidx.compose.ui.geometry.Size(
                2.dp.toPx(),
                size.height
            ),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(1.dp.toPx())
        )

        drawRoundRect(
            color = thumbColor,
            topLeft = Offset(size.width - 6.dp.toPx(), thumbTop),
            size = androidx.compose.ui.geometry.Size(
                4.dp.toPx(),
                thumbHeight
            ),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx())
        )
    }
}

