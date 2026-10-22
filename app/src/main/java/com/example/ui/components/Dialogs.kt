package com.example.ui.components
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween

import androidx.compose.foundation.border
import androidx.compose.foundation.Image
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.viewinterop.AndroidView
import android.widget.EditText
import android.widget.Scroller
import android.text.InputType
import android.text.TextWatcher
import android.text.Editable
import android.text.method.ScrollingMovementMethod
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.runtime.produceState
import android.graphics.Matrix
import android.media.MediaScannerConnection
import android.graphics.BitmapFactory
import com.caverock.androidsvg.SVG
import android.graphics.Bitmap
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import kotlin.math.pow
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.model.FileItem
import com.example.model.OpenWithApp
import com.example.util.AlwaysOpenManager
import com.example.util.FileUtils
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import com.example.ui.icons.LucideIcons
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth

@Composable
fun FileDetailsDialog(
    fileItem: FileItem,
    onDismiss: () -> Unit
,
    isCustomTheme: Boolean = false,
    isLightTheme: Boolean = false) {
    val context = LocalContext.current

    var md5Hash by remember { mutableStateOf<String?>(null) }
    var isCalculatingMd5 by remember { mutableStateOf(false) }
    var dirStats by remember { mutableStateOf<Pair<Long, Int>?>(null) }
    var isCalculatingDirStats by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    var copiedMd5 by remember { mutableStateOf(false) }

    var alwaysExpanded by remember { mutableStateOf(false) }
    var alwaysApps by remember { mutableStateOf<List<OpenWithApp>>(emptyList()) }
    var alwaysSelectedPackage by remember { mutableStateOf<String?>(null) }


    LaunchedEffect(alwaysExpanded, fileItem.file) {
        if (alwaysExpanded && !fileItem.isDirectory) {
            val apps = FileUtils.getOpenWithApps(
                context = context,
                file = fileItem.file
            )
            alwaysApps = apps

            val mimeType = FileUtils.getMimeType(fileItem.file)
            val selectedPackage = AlwaysOpenManager.getPackageName(
                context,
                mimeType
            )
            alwaysSelectedPackage = selectedPackage
        }
    }

    Dialog(
        onDismissRequest = onDismiss
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.88f)
                    .border(
                        width = 1.dp,
                        color = if (isCustomTheme)
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        else MaterialTheme.colorScheme.primary,
                        shape = MaterialTheme.shapes.extraLarge
                    ),
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = if (isLightTheme) 0.dp else 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                    imageVector = fileItem.category.icon,
                    contentDescription = null,
                    tint = fileItem.category.color,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "File Details",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DetailRow(title = "Name", value = fileItem.name)
                DetailRow(title = "Category", value = fileItem.category.name)
                DetailRow(title = "Path", value = fileItem.path, isMonospace = true)

                if (fileItem.isDirectory) {
                    DetailRow(title = "Item Count", value = "${fileItem.childCount ?: 0} items")
                    if (dirStats != null) {
                        DetailRow(
                            title = "Total Folder Size",
                            value = "${FileUtils.formatBytes(dirStats!!.first)} (${dirStats!!.second} files)"
                        )
                    } else {
                        Button(
                            onClick = {
                                isCalculatingDirStats = true
                                scope.launch {
                                    dirStats = FileUtils.calculateDirectoryStats(fileItem.file)
                                    isCalculatingDirStats = false
                                }
                            },
                            enabled = !isCalculatingDirStats,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            if (isCalculatingDirStats) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Calculating...")
                            } else {
                                Text("Calculate Folder Size")
                            }
                        }
                    }
                } else {
                    DetailRow(title = "File Size", value = "${fileItem.formattedSize} (${fileItem.size} bytes)")
                }

                DetailRow(title = "Last Modified", value = fileItem.formattedDate)
                DetailRow(
                    title = "Permissions",
                    value = buildString {
                        if (fileItem.canRead) append("Read (R) ")
                        if (fileItem.canWrite) append("Write (W) ")
                    }
                )

                // Always open with section
                if (!fileItem.isDirectory) {
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        alwaysExpanded = !alwaysExpanded
                                    }
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Always",
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (alwaysExpanded) "▲" else "▼",
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            if (alwaysExpanded) {
                                if (alwaysApps.isEmpty()) {
                                    Text(
                                        text = "No compatible apps found",
                                        modifier = Modifier.padding(
                                            start = 10.dp,
                                            end = 10.dp,
                                            bottom = 10.dp
                                        ),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                } else {
                                    alwaysApps.forEach { app ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    val mimeType = FileUtils.getMimeType(fileItem.file)

                                                    if (alwaysSelectedPackage == app.packageName) {
                                                        AlwaysOpenManager.clearAlwaysOpen(
                                                            context,
                                                            mimeType
                                                        )
                                                        alwaysSelectedPackage = null
                                                    } else {
                                                        AlwaysOpenManager.setAlwaysOpen(
                                                            context,
                                                            mimeType,
                                                            app.packageName,
                                                            app.activityName
                                                        )
                                                        alwaysSelectedPackage = app.packageName
                                                    }
                                                }
                                                .padding(
                                                    horizontal = 10.dp,
                                                    vertical = 6.dp
                                                ),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            val drawable = app.icon
                                            val bitmap = if (drawable is android.graphics.drawable.BitmapDrawable) {
                                                drawable.bitmap
                                            } else {
                                                android.graphics.Bitmap.createBitmap(
                                                    drawable.intrinsicWidth.coerceAtLeast(1),
                                                    drawable.intrinsicHeight.coerceAtLeast(1),
                                                    android.graphics.Bitmap.Config.ARGB_8888
                                                ).also { bitmap ->
                                                    val canvas = android.graphics.Canvas(bitmap)
                                                    drawable.setBounds(
                                                        0,
                                                        0,
                                                        canvas.width,
                                                        canvas.height
                                                    )
                                                    drawable.draw(canvas)
                                                }
                                            }

                                            Image(
                                                painter = BitmapPainter(bitmap.asImageBitmap()),
                                                contentDescription = null,
                                                modifier = Modifier.size(36.dp)
                                            )

                                            Spacer(modifier = Modifier.width(10.dp))

                                            Text(
                                                text = app.label,
                                                modifier = Modifier.weight(1f),
                                                maxLines = 1
                                            )

                                            Checkbox(
                                                checked = alwaysSelectedPackage == app.packageName,
                                                onCheckedChange = { checked ->
                                                    val mimeType = FileUtils.getMimeType(fileItem.file)

                                                    if (checked) {
                                                        AlwaysOpenManager.setAlwaysOpen(
                                                            context,
                                                            mimeType,
                                                            app.packageName,
                                                            app.activityName
                                                        )
                                                        alwaysSelectedPackage = app.packageName
                                                    } else if (alwaysSelectedPackage == app.packageName) {
                                                        AlwaysOpenManager.clearAlwaysOpen(
                                                            context,
                                                            mimeType
                                                        )
                                                        alwaysSelectedPackage = null
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                }

                // MD5 Hash section for files
                if (!fileItem.isDirectory) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = LucideIcons.Fingerprint,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "MD5 Checksum",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                if (md5Hash != null) {
                                    IconButton(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(md5Hash!!))
                                            copiedMd5 = true
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (copiedMd5) LucideIcons.Check else LucideIcons.Copy,
                                            contentDescription = "Copy Hash",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            if (md5Hash == null) {
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedButton(
                                    onClick = {
                                        isCalculatingMd5 = true
                                        scope.launch {
                                            md5Hash = FileUtils.calculateMd5(fileItem.file)
                                            isCalculatingMd5 = false
                                        }
                                    },
                                    enabled = !isCalculatingMd5,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    if (isCalculatingMd5) {
                                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Calculating MD5...")
                                    } else {
                                        Text("Calculate MD5 Checksum")
                                    }
                                }
                            } else {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = md5Hash!!,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }

                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Close")
            }
        }
            }
        }
    }
    }

@Composable
private fun DetailRow(title: String, value: String, isMonospace: Boolean = false) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            fontFamily = if (isMonospace) FontFamily.Monospace else FontFamily.Default,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextViewerDialog(
    file: File,
    content: String,
    onEdit: () -> Unit,
    onClose: () -> Unit
) {
    val scrollState = rememberScrollState()

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 1f)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = file.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${content.lines().size} lines • ${content.length} characters",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onClose) {
                            Icon(
                                LucideIcons.X,
                                contentDescription = "Close"
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = onEdit) {
                            Icon(
                                LucideIcons.Edit3,
                                contentDescription = "Edit"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(16.dp)
                ) {
                    Text(
                        text = content,
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 20.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextEditorDialog(
    file: File,
    initialContent: String,
    onSave: (String) -> Unit,
    onSaveAs: (String, String) -> Unit,
    onClose: () -> Unit
) {
    var content by remember { mutableStateOf(initialContent) }
    var saveAsDialogVisible by remember { mutableStateOf(false) }
    var saveAsFileName by remember { mutableStateOf(file.name) }
    val editorTextColor = MaterialTheme.colorScheme.onBackground.toArgb()
    val editorHintColor = MaterialTheme.colorScheme.onSurfaceVariant.toArgb()
    var selectionActive by remember { mutableStateOf(false) }
    var editorView by remember { mutableStateOf<EditText?>(null) }
    val density = LocalDensity.current

    LaunchedEffect(selectionActive, editorView) {
        while (selectionActive) {
            val editor = editorView
            if (editor != null && editor.hasFocus() && editor.layout != null) {
                val endOffset = editor.selectionEnd
                if (endOffset >= 0) {
                    val line = editor.layout.getLineForOffset(endOffset)
                    val lineBottom = editor.layout.getLineBottom(line) - editor.scrollY
                    val bottomLimit = editor.height - with(density) { 68.dp.roundToPx() }

                    if (lineBottom >= bottomLimit) {
                        editor.scrollBy(0, with(density) { 12.dp.roundToPx() })
                    }
                }
            }
            kotlinx.coroutines.delay(50)
        }
    }


    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 1f)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = file.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${content.lines().size} lines • ${content.length} characters",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onClose) {
                            Icon(LucideIcons.X, contentDescription = "Close")
                        }
                    },
                    actions = {
                        var saveMenuExpanded by remember { mutableStateOf(false) }

                        Box(
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Button(
                                onClick = { saveMenuExpanded = true },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    LucideIcons.Save,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Save")
                            }

                            androidx.compose.material3.DropdownMenu(
                                expanded = saveMenuExpanded,
                                onDismissRequest = { saveMenuExpanded = false }
                            ) {
                                androidx.compose.material3.DropdownMenuItem(
                                    text = { Text("Save") },
                                    onClick = {
                                        saveMenuExpanded = false
                                        onSave(content)
                                    }
                                )

                                androidx.compose.material3.DropdownMenuItem(
                                    text = { Text("Save As") },
                                    onClick = {
                                        saveMenuExpanded = false
                                        saveAsFileName = file.name
                                        saveAsDialogVisible = true
                                    }
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )

                if (saveAsDialogVisible) {
                    AlertDialog(
                        onDismissRequest = { saveAsDialogVisible = false },
                        title = { Text("Save As") },
                        text = {
                            OutlinedTextField(
                                value = saveAsFileName,
                                onValueChange = { saveAsFileName = it },
                                label = { Text("File name") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    val name = saveAsFileName.trim()
                                    if (name.isNotEmpty()) {
                                        saveAsDialogVisible = false
                                        onSaveAs(name, content)
                                    }
                                }
                            ) {
                                Text("Save")
                            }
                        },
                        dismissButton = {
                            androidx.compose.material3.TextButton(
                                onClick = { saveAsDialogVisible = false }
                            ) {
                                Text("Cancel")
                            }
                        }
                    )
                }

                AndroidView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(12.dp),
                    factory = { context ->
                        object : EditText(context) {
                            override fun onSelectionChanged(selStart: Int, selEnd: Int) {
                                super.onSelectionChanged(selStart, selEnd)
                                selectionActive = selStart >= 0 && selEnd >= 0 && selStart != selEnd
                            }
                        }.apply {
                            editorView = this
                            gravity = android.view.Gravity.TOP or android.view.Gravity.START
                            setText(initialContent)
                            setSelection(0)
                            inputType = InputType.TYPE_CLASS_TEXT or
                                InputType.TYPE_TEXT_FLAG_MULTI_LINE or
                                InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                            setSingleLine(false)
                            setScroller(
                                Scroller(context).apply {
                                    setFriction(0.005f)
                                }
                            )
                            setHorizontallyScrolling(false)
                            isVerticalScrollBarEnabled = true
                            overScrollMode = android.view.View.OVER_SCROLL_ALWAYS
                            typeface = android.graphics.Typeface.MONOSPACE
                            textSize = 14f
                            setPadding(24, 16, 24, 16)
                            setBackgroundColor(android.graphics.Color.TRANSPARENT)
                            setTextColor(editorTextColor)
                            setHintTextColor(editorHintColor)
                            hint = "Enter text here..."

                            addTextChangedListener(object : TextWatcher {
                                override fun beforeTextChanged(
                                    s: CharSequence?,
                                    start: Int,
                                    count: Int,
                                    after: Int
                                ) = Unit

                                override fun onTextChanged(
                                    s: CharSequence?,
                                    start: Int,
                                    before: Int,
                                    count: Int
                                ) {
                                    if (s != null && s.toString() != content) {
                                        content = s.toString()
                                    }
                                }

                                override fun afterTextChanged(s: Editable?) = Unit
                            })
                        }
                    },
                    update = { editText ->
                        // Text changes are handled directly by EditText/TextWatcher.
                        // Avoid setText() here because it is expensive for large files.
                    }
                )

                if (selectionActive) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Drag selection to the bottom to scroll",
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

fun applyColorAdjustments(
    source: Bitmap,
    brightness: Float,
    contrast: Float,
    saturation: Float,
    exposure: Float
): Bitmap {
    val output = source.copy(source.config ?: Bitmap.Config.ARGB_8888, true)

    val brightnessValue = brightness * 255f
    val contrastScale = 1f + contrast
    val contrastTranslate = 128f * (1f - contrastScale)
    val exposureScale = 2.0.pow(exposure.toDouble()).toFloat()

    val matrix = ColorMatrix()

    matrix.set(
        floatArrayOf(
            contrastScale * exposureScale, 0f, 0f, 0f, brightnessValue + contrastTranslate,
            0f, contrastScale * exposureScale, 0f, 0f, brightnessValue + contrastTranslate,
            0f, 0f, contrastScale * exposureScale, 0f, brightnessValue + contrastTranslate,
            0f, 0f, 0f, 1f, 0f
        )
    )

    val saturationMatrix = ColorMatrix()
    saturationMatrix.setSaturation(1f + saturation)

    matrix.postConcat(saturationMatrix)

    val canvas = android.graphics.Canvas(output)
    val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        colorFilter = ColorMatrixColorFilter(matrix)
    }

    canvas.drawBitmap(source, 0f, 0f, paint)

    return output
}


@Composable
fun ImagePreviewDialog(
    initialFile: File,
    imageFiles: List<File>,
    onClose: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var file by remember { mutableStateOf(initialFile) }
    var currentImageFiles by remember { mutableStateOf(imageFiles) }
    var currentImageIndex by remember(file, currentImageFiles) {
        mutableStateOf(
            currentImageFiles.indexOfFirst { it.absolutePath == file.absolutePath }
                .coerceAtLeast(0)
        )
    }
    var swipeStartX by remember { mutableStateOf(0f) }
    var slideDirection by remember { mutableStateOf(0) }

    fun showImageAt(index: Int) {
        if (index in currentImageFiles.indices && index != currentImageIndex) {
            slideDirection = if (index > currentImageIndex) 1 else -1
            currentImageIndex = index
            file = currentImageFiles[index]
        }
    }

    var showImageDetails by remember(file) { mutableStateOf(false) }
    var showDeleteConfirmation by remember(file) { mutableStateOf(false) }
    var showSaveDiscardDialog by remember(file) { mutableStateOf(false) }
    var showDrawApplyDiscardDialog by remember(file) { mutableStateOf(false) }

    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var drawSessionBitmap by remember(file) { mutableStateOf<Bitmap?>(null) }
    var drawSessionUndoStack by remember(file) { mutableStateOf<List<Bitmap>?>(null) }
    var drawSessionRedoStack by remember(file) { mutableStateOf<List<Bitmap>?>(null) }
    var animateImageSlide by remember { mutableStateOf(false) }
    val undoStack = remember(file) { mutableStateOf<List<Bitmap>>(emptyList()) }
    val redoStack = remember(file) { mutableStateOf<List<Bitmap>>(emptyList()) }

    var scale by remember(file) { mutableStateOf(1f) }
    var offsetX by remember(file) { mutableStateOf(0f) }
    var offsetY by remember(file) { mutableStateOf(0f) }

    var showAdjustments by remember(file) { mutableStateOf(false) }
    var showImageControls by remember(file) { mutableStateOf(true) }
    var adjustmentBase by remember(file) { mutableStateOf<Bitmap?>(null) }
    var brightness by remember(file) { mutableStateOf(0f) }
    var contrast by remember(file) { mutableStateOf(0f) }
    var saturation by remember(file) { mutableStateOf(0f) }
    var exposure by remember(file) { mutableStateOf(0f) }
    var cropViewWidth by remember(file) { mutableStateOf(0f) }
    var cropViewHeight by remember(file) { mutableStateOf(0f) }

    var showDraw by remember(file) { mutableStateOf(false) }
    var drawMode by remember(file) { mutableStateOf(false) }
    var drawColor by remember(file) { mutableStateOf(Color.White) }
    var drawStrokeWidth by remember(file) { mutableStateOf(6f) }

    var drawColorPickerActive by remember(file) { mutableStateOf(false) }
    var previousDrawColor by remember(file) { mutableStateOf(Color.White) }

    data class DrawPath(
        val points: List<Offset>,
        val color: Color,
        val strokeWidth: Float
    )

    data class TextOverlay(
        val text: String,
        val position: Offset,
        val rotation: Float,
        val fontSize: Float,
        val color: Color,
        val width: Float = 320f,
        val height: Float = 96f
    )

    val drawPaths = remember(file) { mutableStateListOf<DrawPath>() }
    var activeDrawPoints by remember(file) { mutableStateOf<List<Offset>>(emptyList()) }

    val textOverlays = remember(file) { mutableStateListOf<TextOverlay>() }

    data class DrawSnapshot(
        val paths: List<DrawPath>,
        val texts: List<TextOverlay>
    )

    val drawUndoStack = remember(file) {
        mutableStateOf<List<DrawSnapshot>>(emptyList())
    }
    val drawRedoStack = remember(file) {
        mutableStateOf<List<DrawSnapshot>>(emptyList())
    }

    fun saveDrawSnapshot() {
        drawUndoStack.value = drawUndoStack.value + DrawSnapshot(
            paths = drawPaths.toList(),
            texts = textOverlays.toList()
        )
        drawRedoStack.value = emptyList()
    }

    var selectedTextIndex by remember(file) { mutableStateOf<Int?>(null) }
    var textInput by remember(file) { mutableStateOf("") }
    var textFontSize by remember(file) { mutableStateOf(48f) }
    var textColor by remember(file) { mutableStateOf(Color.White) }

    var showTextColorPicker by remember(file) { mutableStateOf(false) }
    var previousTextColor by remember(file) { mutableStateOf(Color.White) }
    var pendingTextColor by remember(file) { mutableStateOf(Color.White) }

    var textHue by remember(file) { mutableStateOf(0f) }
    var textSaturation by remember(file) { mutableStateOf(0f) }
    var textValue by remember(file) { mutableStateOf(1f) }

    var pickerHue by remember(file) { mutableStateOf(0f) }
    var pickerSaturation by remember(file) { mutableStateOf(0f) }
    var pickerValue by remember(file) { mutableStateOf(1f) }
    var pendingPickerColor by remember(file) { mutableStateOf(Color.White) }

    val textFocusRequester = remember(file) { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    fun addDrawPath() {
        if (activeDrawPoints.size >= 2) {
            saveDrawSnapshot()
            drawPaths.add(
                DrawPath(
                    points = activeDrawPoints,
                    color = drawColor,
                    strokeWidth = drawStrokeWidth
                )
            )
        }
        activeDrawPoints = emptyList()
    }

    fun addTextOverlay() {
        val center = Offset(
            cropViewWidth / 2f,
            cropViewHeight / 2f
        )

        saveDrawSnapshot()

        textOverlays.add(
            TextOverlay(
                text = "",
                position = center,
                rotation = 0f,
                fontSize = textFontSize,
                color = textColor
            )
        )

        selectedTextIndex = textOverlays.lastIndex
        textInput = ""
    }

    LaunchedEffect(selectedTextIndex) {
        if (selectedTextIndex != null) {
            textFocusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    fun clearDrawObjects() {
        if (drawPaths.isNotEmpty() || textOverlays.isNotEmpty()) {
            saveDrawSnapshot()
        }
        drawPaths.clear()
        activeDrawPoints = emptyList()
        textOverlays.clear()
        selectedTextIndex = null
    }

    fun previewDrawObjects(source: Bitmap): Bitmap {
        if (drawPaths.isEmpty() && textOverlays.isEmpty()) return source

        val output = source.copy(
            source.config ?: Bitmap.Config.ARGB_8888,
            true
        )

        val canvas = android.graphics.Canvas(output)

        val scaleX = output.width.toFloat() / cropViewWidth.coerceAtLeast(1f)
        val scaleY = output.height.toFloat() / cropViewHeight.coerceAtLeast(1f)

        drawPaths.forEach { pathData ->
            if (pathData.points.size >= 2) {
                val paint = android.graphics.Paint(
                    android.graphics.Paint.ANTI_ALIAS_FLAG
                ).apply {
                    color = pathData.color.toArgb()
                    style = android.graphics.Paint.Style.STROKE
                    strokeWidth = pathData.strokeWidth * scaleX
                    strokeCap = android.graphics.Paint.Cap.ROUND
                    strokeJoin = android.graphics.Paint.Join.ROUND
                }

                val path = android.graphics.Path()
                pathData.points.forEachIndexed { index, point ->
                    val x = point.x * scaleX
                    val y = point.y * scaleY
                    if (index == 0) {
                        path.moveTo(x, y)
                    } else {
                        path.lineTo(x, y)
                    }
                }

                canvas.drawPath(path, paint)
            }
        }

        textOverlays.forEach { item ->
            val paint = android.graphics.Paint(
                android.graphics.Paint.ANTI_ALIAS_FLAG
            ).apply {
                color = item.color.toArgb()
                textSize = item.fontSize * scaleX
                typeface = android.graphics.Typeface.DEFAULT
                textAlign = android.graphics.Paint.Align.CENTER
            }

            canvas.save()
            canvas.rotate(
                item.rotation,
                item.position.x * scaleX,
                item.position.y * scaleY
            )
            canvas.drawText(
                item.text,
                item.position.x * scaleX,
                item.position.y * scaleY,
                paint
            )
            canvas.restore()
        }

        return output
    }

    fun restoreDrawSnapshot(snapshot: DrawSnapshot) {
        drawPaths.clear()
        drawPaths.addAll(snapshot.paths)

        textOverlays.clear()
        textOverlays.addAll(snapshot.texts)

        selectedTextIndex = null
        textInput = ""
    }

    fun commitDrawObjects() {
        if (drawPaths.isEmpty() && textOverlays.isEmpty()) return

        val current = bitmap ?: return

        undoStack.value = undoStack.value +
            current.copy(
                current.config ?: Bitmap.Config.ARGB_8888,
                true
            )

        redoStack.value = emptyList()

        bitmap = previewDrawObjects(current)

        drawPaths.clear()
        activeDrawPoints = emptyList()
        textOverlays.clear()
        selectedTextIndex = null
        textInput = ""

        drawUndoStack.value = emptyList()
        drawRedoStack.value = emptyList()
    }

    fun applyAndCloseDraw() {
        commitDrawObjects()

        drawPaths.clear()
        textOverlays.clear()
        activeDrawPoints = emptyList()
        selectedTextIndex = null
        textInput = ""

        drawUndoStack.value = emptyList()
        drawRedoStack.value = emptyList()

        drawMode = false
        showDraw = false
        drawSessionBitmap = null
        drawSessionUndoStack = null
        drawSessionRedoStack = null
    }

    fun discardDrawSession() {
        drawSessionBitmap?.let { original ->
            bitmap = original.copy(
                original.config ?: Bitmap.Config.ARGB_8888,
                true
            )
        }

        drawSessionUndoStack?.let { undoStack.value = it }
        drawSessionRedoStack?.let { redoStack.value = it }

        drawPaths.clear()
        textOverlays.clear()
        activeDrawPoints = emptyList()
        selectedTextIndex = null
        textInput = ""

        drawUndoStack.value = emptyList()
        drawRedoStack.value = emptyList()

        drawMode = false
        showDraw = false
        showDrawApplyDiscardDialog = false

        drawSessionBitmap = null
        drawSessionUndoStack = null
        drawSessionRedoStack = null
    }

    fun previewAdjustments() {
        val base = adjustmentBase ?: return

        val maxPreviewSize = 1200
        val scale = minOf(
            1f,
            maxPreviewSize.toFloat() / maxOf(base.width, base.height).toFloat()
        )

        val preview = if (scale < 1f) {
            Bitmap.createScaledBitmap(
                base,
                (base.width * scale).toInt(),
                (base.height * scale).toInt(),
                true
            )
        } else {
            base
        }

        bitmap = applyColorAdjustments(
            source = preview,
            brightness = brightness,
            contrast = contrast,
            saturation = saturation,
            exposure = exposure
        )
    }

    val cropPreviewScale = 0.9f

    var cropMode by remember(file) { mutableStateOf(false) }
    var cropStart by remember(file) { mutableStateOf<Offset?>(null) }
    var cropEnd by remember(file) { mutableStateOf<Offset?>(null) }

    androidx.compose.runtime.LaunchedEffect(
        cropMode,
        bitmap,
        cropViewWidth,
        cropViewHeight
    ) {
        if (
            !cropMode ||
            cropStart != null ||
            cropEnd != null ||
            cropViewWidth <= 0f ||
            cropViewHeight <= 0f
        ) {
            return@LaunchedEffect
        }

        val source = bitmap ?: return@LaunchedEffect
        val imageAspect = source.width.toFloat() / source.height.toFloat()
        val viewAspect = cropViewWidth / cropViewHeight

        val displayedWidth: Float
        val displayedHeight: Float
        val imageLeft: Float
        val imageTop: Float

        if (imageAspect > viewAspect) {
            displayedWidth = cropViewWidth * cropPreviewScale
            displayedHeight = displayedWidth / imageAspect
            imageLeft = (cropViewWidth - displayedWidth) / 2f
            imageTop = (cropViewHeight - displayedHeight) / 2f
        } else {
            displayedHeight = cropViewHeight * cropPreviewScale
            displayedWidth = displayedHeight * imageAspect
            imageTop = (cropViewHeight - displayedHeight) / 2f
            imageLeft = (cropViewWidth - displayedWidth) / 2f
        }

        val marginX = displayedWidth * 0.1f
        val marginY = displayedHeight * 0.1f

        cropStart = Offset(
            imageLeft + marginX,
            imageTop + marginY
        )
        cropEnd = Offset(
            imageLeft + displayedWidth - marginX,
            imageTop + displayedHeight - marginY
        )
    }

    LaunchedEffect(file) {
        bitmap = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            runCatching {
                if (file.extension.equals("svg", ignoreCase = true)) {
                    val picture = SVG.getFromInputStream(file.inputStream()).renderToPicture()
                    val sourceWidth = picture.width.coerceAtLeast(1)
                    val sourceHeight = picture.height.coerceAtLeast(1)
                    val factor = minOf(
                        1f,
                        4096f / sourceWidth,
                        4096f / sourceHeight
                    )
                    val width = (sourceWidth * factor).toInt().coerceAtLeast(1)
                    val height = (sourceHeight * factor).toInt().coerceAtLeast(1)
                    Bitmap.createBitmap(
                        width,
                        height,
                        Bitmap.Config.ARGB_8888
                    ).also { output ->
                        android.graphics.Canvas(output).drawPicture(
                            picture,
                            android.graphics.Rect(0, 0, width, height)
                        )
                    }
                } else {
                    BitmapFactory.decodeFile(file.absolutePath)
                }
            }.getOrNull()
        }
        undoStack.value = emptyList()
        redoStack.value = emptyList()
        scale = 1f
        offsetX = 0f
        offsetY = 0f
    }

    fun applyTransform(transform: (Bitmap) -> Bitmap) {
        val current = bitmap ?: return

        undoStack.value = undoStack.value + current.copy(current.config ?: Bitmap.Config.ARGB_8888, true)
        redoStack.value = emptyList()

        bitmap = transform(current)
    }

    fun rotateBitmap(source: Bitmap): Bitmap {
        val matrix = Matrix().apply {
            postRotate(90f)
        }
        return Bitmap.createBitmap(
            source,
            0,
            0,
            source.width,
            source.height,
            matrix,
            true
        )
    }

    fun flipHorizontal(source: Bitmap): Bitmap {
        val matrix = Matrix().apply {
            postScale(-1f, 1f)
        }
        return Bitmap.createBitmap(
            source,
            0,
            0,
            source.width,
            source.height,
            matrix,
            true
        )
    }

    fun flipVertical(source: Bitmap): Bitmap {
        val matrix = Matrix().apply {
            postScale(1f, -1f)
        }
        return Bitmap.createBitmap(
            source,
            0,
            0,
            source.width,
            source.height,
            matrix,
            true
        )
    }

    fun undo() {
        if (drawUndoStack.value.isNotEmpty()) {
            val current = DrawSnapshot(
                paths = drawPaths.toList(),
                texts = textOverlays.toList()
            )

            drawRedoStack.value = drawRedoStack.value + current

            val previous = drawUndoStack.value.last()
            drawUndoStack.value = drawUndoStack.value.dropLast(1)

            restoreDrawSnapshot(previous)
            return
        }

        val current = bitmap ?: return
        val previous = undoStack.value.lastOrNull() ?: return

        redoStack.value = redoStack.value +
            current.copy(current.config ?: Bitmap.Config.ARGB_8888, true)

        undoStack.value = undoStack.value.dropLast(1)
        bitmap = previous
    }

    fun redo() {
        if (drawRedoStack.value.isNotEmpty()) {
            val current = DrawSnapshot(
                paths = drawPaths.toList(),
                texts = textOverlays.toList()
            )

            drawUndoStack.value = drawUndoStack.value + current

            val next = drawRedoStack.value.last()
            drawRedoStack.value = drawRedoStack.value.dropLast(1)

            restoreDrawSnapshot(next)
            return
        }

        val current = bitmap ?: return
        val next = redoStack.value.lastOrNull() ?: return

        undoStack.value = undoStack.value.dropLast(1)
        undoStack.value = undoStack.value +
            current.copy(current.config ?: Bitmap.Config.ARGB_8888, true)

        bitmap = next
    }

    fun cropBitmap(source: Bitmap, start: Offset, end: Offset, viewWidth: Float, viewHeight: Float): Bitmap {
        val left = minOf(start.x, end.x).coerceIn(0f, viewWidth)
        val right = maxOf(start.x, end.x).coerceIn(0f, viewWidth)
        val top = minOf(start.y, end.y).coerceIn(0f, viewHeight)
        val bottom = maxOf(start.y, end.y).coerceIn(0f, viewHeight)

        val cropWidth = right - left
        val cropHeight = bottom - top
        if (cropWidth < 10f || cropHeight < 10f) return source

        val imageAspect = source.width.toFloat() / source.height.toFloat()
        val viewAspect = viewWidth / viewHeight

        val displayedWidth: Float
        val displayedHeight: Float
        val imageLeft: Float
        val imageTop: Float

        if (imageAspect > viewAspect) {
            displayedWidth = viewWidth * cropPreviewScale
            displayedHeight = displayedWidth / imageAspect
            imageLeft = (viewWidth - displayedWidth) / 2f
            imageTop = (viewHeight - displayedHeight) / 2f
        } else {
            displayedHeight = viewHeight * cropPreviewScale
            displayedWidth = displayedHeight * imageAspect
            imageTop = (viewHeight - displayedHeight) / 2f
            imageLeft = (viewWidth - displayedWidth) / 2f
        }

        val cropLeft = ((left - imageLeft) / displayedWidth * source.width)
            .coerceIn(0f, source.width.toFloat())
        val cropTop = ((top - imageTop) / displayedHeight * source.height)
            .coerceIn(0f, source.height.toFloat())
        val cropRight = ((right - imageLeft) / displayedWidth * source.width)
            .coerceIn(0f, source.width.toFloat())
        val cropBottom = ((bottom - imageTop) / displayedHeight * source.height)
            .coerceIn(0f, source.height.toFloat())

        val x = cropLeft.toInt()
        val y = cropTop.toInt()
        val width = (cropRight - cropLeft).toInt()
        val height = (cropBottom - cropTop).toInt()

        if (width < 1 || height < 1) return source

        return Bitmap.createBitmap(source, x, y, width, height)
    }

    fun saveBitmap() {
        val baseBitmap = bitmap ?: return
        val current = previewDrawObjects(baseBitmap)

        val format = when (file.extension.lowercase()) {
            "png" -> Bitmap.CompressFormat.PNG
            "webp" -> Bitmap.CompressFormat.WEBP_LOSSLESS
            else -> Bitmap.CompressFormat.JPEG
        }

        val quality = if (format == Bitmap.CompressFormat.PNG) 100 else 95
        val extension = file.extension.ifEmpty { "jpg" }
        val baseName = file.nameWithoutExtension

        var counter = 1
        var outputFile = File(file.parentFile, "${baseName}_edited.$extension")

        while (outputFile.exists()) {
            outputFile = File(
                file.parentFile,
                "${baseName}_edited_${counter++}.$extension"
            )
        }

        try {
            FileOutputStream(outputFile).use { output ->
                if (!current.compress(format, quality, output)) {
                    throw Exception("Image compression failed")
                }
            }
            MediaScannerConnection.scanFile(context, arrayOf(outputFile.absolutePath), null, null)

            android.widget.Toast.makeText(
                context,
                "Saved successfully",
                android.widget.Toast.LENGTH_SHORT
            ).show()

            undoStack.value = emptyList()
            redoStack.value = emptyList()
            drawUndoStack.value = emptyList()
            drawRedoStack.value = emptyList()
            drawPaths.clear()
            activeDrawPoints = emptyList()
            textOverlays.clear()
            selectedTextIndex = null
            textInput = ""
        } catch (_: Exception) {
            android.widget.Toast.makeText(
                context,
                "Failed to save image",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }

    if (showSaveDiscardDialog) {
        Dialog(
            onDismissRequest = { showSaveDiscardDialog = false }
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier
                        .wrapContentWidth()
                        .wrapContentHeight(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF3C0020).copy(alpha = 0.8f),
                    tonalElevation = 0.dp
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            "Save changes?",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text("Do you want to save the changes made to this image?", color = Color.White)

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            androidx.compose.material3.TextButton(
                                onClick = {
                                    showSaveDiscardDialog = false
                                    onClose()
                                }
                            ) {
                                Text("Discard", color = Color.White)
                            }

                            androidx.compose.material3.TextButton(
                                onClick = {
                                    showSaveDiscardDialog = false
                                    saveBitmap()
                                    onClose()
                                }
                            ) {
                                Text("Save", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDrawApplyDiscardDialog) {
        Dialog(
            onDismissRequest = {
                showDrawApplyDiscardDialog = false
            }
        ) {
            Surface(
                modifier = Modifier
                    .wrapContentWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF3C0020).copy(alpha = 0.8f),
                tonalElevation = 0.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        "Draw changes",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Apply the drawing changes or discard them?", color = Color.White)

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        androidx.compose.material3.TextButton(
                            onClick = {
                                discardDrawSession()
                            }
                        ) {
                            Text("Discard", color = Color.White)
                        }

                        androidx.compose.material3.TextButton(
                            onClick = {
                                showDrawApplyDiscardDialog = false
                                applyAndCloseDraw()
                            }
                        ) {
                            Text("Apply", color = Color.White)
                        }
                    }
                }
            }
        }
    }

    if (showImageDetails) {
        Dialog(
            onDismissRequest = { showImageDetails = false }
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier
                        .wrapContentWidth()
                        .wrapContentHeight(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        "Image Details",
                        style = MaterialTheme.typography.headlineSmall
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Format: ${file.extension.uppercase()}")
                    Text("File name: ${file.name}")
                    Text(
                        "Dimensions: ${
                            bitmap?.let { "${it.width} × ${it.height}" } ?: "Unknown"
                        }"
                    )
                    Text("Path: ${file.absolutePath}")
                    Text("File size: ${file.length() / 1024} KB")

                    Spacer(modifier = Modifier.height(16.dp))

                    androidx.compose.material3.TextButton(
                        onClick = { showImageDetails = false },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("OK")
                    }
                }
            }
        }
        }
    }

    if (showDeleteConfirmation) {
        Dialog(
            onDismissRequest = { showDeleteConfirmation = false }
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier
                        .wrapContentWidth()
                        .wrapContentHeight(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        "Delete Image",
                        style = MaterialTheme.typography.headlineSmall
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Are you sure you want to delete \"${file.name}\"?")

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        androidx.compose.material3.TextButton(
                            onClick = { showDeleteConfirmation = false }
                        ) {
                            Text("Cancel")
                        }

                        androidx.compose.material3.TextButton(
                            onClick = {
                                showDeleteConfirmation = false
                                scope.launch {
                                    if (FileUtils.deleteRecursively(file)) {
                                        val deletedIndex = currentImageIndex
                                        val updatedImages = currentImageFiles.filterNot {
                                            it.absolutePath == file.absolutePath
                                        }

                                        if (updatedImages.isEmpty()) {
                                            onClose()
                                        } else {
                                            val nextIndex =
                                                deletedIndex.coerceAtMost(updatedImages.lastIndex)
                                            val nextFile = updatedImages[nextIndex]

                                            currentImageFiles = updatedImages
                                            currentImageIndex = nextIndex
                                            file = nextFile
                                            showDeleteConfirmation = false
                                        }
                                    } else {
                                        android.widget.Toast.makeText(
                                            context,
                                            "Failed to delete image",
                                            android.widget.Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        ) {
                            Text("Delete", color = Color(0xFFFF5252))
                        }
                    }
                }
            }
        }
        }
    }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (showImageControls) {
                                Color.Black.copy(alpha = 0.7f)
                            } else {
                                Color.Transparent
                            }
                        )
                        .padding(12.dp)
                        .graphicsLayer { alpha = if (showImageControls) 1f else 0f },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                Text(
                    text = file.name,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = {
                        FileUtils.shareFile(context, file)
                    }
                ) {
                    Icon(
                        imageVector = LucideIcons.Share2,
                        contentDescription = "Share",
                        tint = Color.White
                    )
                }

                IconButton(onClick = { showImageDetails = true }) {
                    Icon(
                        imageVector = LucideIcons.Info,
                        contentDescription = "Details",
                        tint = Color.White
                    )
                }

                IconButton(onClick = { showDeleteConfirmation = true }) {
                    Icon(
                        imageVector = LucideIcons.Trash2,
                        contentDescription = "Delete",
                        tint = Color(0xFFFF5252)
                    )
                }

                IconButton(
                      onClick = {
                          val hasChanges =
                              undoStack.value.isNotEmpty() ||
                              drawUndoStack.value.isNotEmpty() ||
                              drawPaths.isNotEmpty() ||
                              textOverlays.isNotEmpty() ||
                              activeDrawPoints.isNotEmpty()

                          if (hasChanges) {
                              showSaveDiscardDialog = true
                          } else {
                              onClose()
                          }
                      }
                  ) {
                    Icon(
                        imageVector = LucideIcons.X,
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }
            }
            }

            

            if (bitmap != null) {
                val currentBitmap = bitmap!!
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .onSizeChanged { size ->
                            cropViewWidth = size.width.toFloat()
                            cropViewHeight = size.height.toFloat()
                        }
                        .pointerInput(
                            file,
                            currentImageIndex,
                            imageFiles,
                            scale,
                            cropMode,
                            drawMode,
                            undoStack.value,
                            drawUndoStack.value,
                            drawPaths,
                            textOverlays,
                            activeDrawPoints
                        ) {
                            val hasChanges =
                                undoStack.value.isNotEmpty() ||
                                drawUndoStack.value.isNotEmpty() ||
                                drawPaths.isNotEmpty() ||
                                textOverlays.isNotEmpty() ||
                                activeDrawPoints.isNotEmpty()

                            if (!hasChanges && !cropMode && !drawMode && scale <= 1f) {
                                awaitEachGesture {
                                    val down = awaitFirstDown(requireUnconsumed = false)
                                    var totalDragX = 0f
                                    var totalDragY = 0f
                                    var dragging = false

                                    while (true) {
                                        val event = awaitPointerEvent()
                                        val change = event.changes.firstOrNull() ?: break

                                        if (!change.pressed) break

                                        val dx = change.position.x - down.position.x
                                        val dy = change.position.y - down.position.y

                                        totalDragX = dx
                                        totalDragY = dy

                                        if (!dragging &&
                                            kotlin.math.abs(dx) > 24f &&
                                            kotlin.math.abs(dx) > kotlin.math.abs(dy)
                                        ) {
                                            dragging = true
                                        }

                                        if (dragging) {
                                            change.consume()
                                        }
                                    }

                                    if (dragging && kotlin.math.abs(totalDragX) >= 120f) {
                                        if (totalDragX < 0f) {
                                            showImageAt(currentImageIndex + 1)
                                        } else {
                                            showImageAt(currentImageIndex - 1)
                                        }
                                    } else if (!dragging &&
                                        kotlin.math.abs(totalDragX) < 24f &&
                                        kotlin.math.abs(totalDragY) < 24f
                                    ) {
                                        showImageControls = !showImageControls
                                    }

                                    swipeStartX = 0f
                                }
                            }
                        }
                        .pointerInput(currentBitmap, cropMode, drawMode) {
                            if (cropMode) {
                                var activeHandle = -1

                                val boxWidth = size.width.toFloat()
                                val boxHeight = size.height.toFloat()
                                val imageAspect =
                                    currentBitmap.width.toFloat() / currentBitmap.height
                                val viewAspect = boxWidth / boxHeight

                                val imageLeft: Float
                                val imageTop: Float
                                val imageRight: Float
                                val imageBottom: Float

                                if (imageAspect > viewAspect) {
                                    val displayedWidth = boxWidth * cropPreviewScale
                                    val displayedHeight = displayedWidth / imageAspect
                                    imageLeft = (boxWidth - displayedWidth) / 2f
                                    imageRight = imageLeft + displayedWidth
                                    imageTop = (boxHeight - displayedHeight) / 2f
                                    imageBottom = imageTop + displayedHeight
                                } else {
                                    val displayedHeight = boxHeight * cropPreviewScale
                                    val displayedWidth = displayedHeight * imageAspect
                                    imageTop = (boxHeight - displayedHeight) / 2f
                                    imageBottom = imageTop + displayedHeight
                                    imageLeft = (boxWidth - displayedWidth) / 2f
                                    imageRight = imageLeft + displayedWidth
                                }

                                detectDragGestures(
                                    onDragStart = { touch ->
                                        val start = cropStart
                                        val end = cropEnd
                                        activeHandle = -1

                                        if (start != null && end != null) {
                                            val left = minOf(start.x, end.x)
                                            val right = maxOf(start.x, end.x)
                                            val top = minOf(start.y, end.y)
                                            val bottom = maxOf(start.y, end.y)
                                            val midX = (left + right) / 2f
                                            val midY = (top + bottom) / 2f

                                            val handles = listOf(
                                                Offset(left, top),
                                                Offset(midX, top),
                                                Offset(right, top),
                                                Offset(right, midY),
                                                Offset(right, bottom),
                                                Offset(midX, bottom),
                                                Offset(left, bottom),
                                                Offset(left, midY)
                                            )

                                            activeHandle = handles.indexOfFirst {
                                                (it - touch).getDistance() <= 48f
                                            }
                                        }
                                    },
                                    onDrag = { change, _ ->
                                        change.consume()

                                        if (activeHandle == -1) {
                                            return@detectDragGestures
                                        }

                                        val start = cropStart
                                        val end = cropEnd

                                        if (start != null && end != null) {
                                            var left = minOf(start.x, end.x)
                                            var right = maxOf(start.x, end.x)
                                            var top = minOf(start.y, end.y)
                                            var bottom = maxOf(start.y, end.y)

                                            val x = change.position.x.coerceIn(
                                                imageLeft,
                                                imageRight
                                            )
                                            val y = change.position.y.coerceIn(
                                                imageTop,
                                                imageBottom
                                            )
                                            val minSize = 10f

                                            when (activeHandle) {
                                                0 -> {
                                                    left = x.coerceIn(imageLeft, right - minSize)
                                                    top = y.coerceIn(imageTop, bottom - minSize)
                                                }
                                                1 -> {
                                                    top = y.coerceIn(imageTop, bottom - minSize)
                                                }
                                                2 -> {
                                                    right = x.coerceIn(left + minSize, imageRight)
                                                    top = y.coerceIn(imageTop, bottom - minSize)
                                                }
                                                3 -> {
                                                    right = x.coerceIn(left + minSize, imageRight)
                                                }
                                                4 -> {
                                                    right = x.coerceIn(left + minSize, imageRight)
                                                    bottom = y.coerceIn(top + minSize, imageBottom)
                                                }
                                                5 -> {
                                                    bottom = y.coerceIn(top + minSize, imageBottom)
                                                }
                                                6 -> {
                                                    left = x.coerceIn(imageLeft, right - minSize)
                                                    bottom = y.coerceIn(top + minSize, imageBottom)
                                                }
                                                7 -> {
                                                    left = x.coerceIn(imageLeft, right - minSize)
                                                }
                                            }

                                            cropStart = Offset(left, top)
                                            cropEnd = Offset(right, bottom)
                                        }
                                    },
                                    onDragEnd = {
                                        activeHandle = -1
                                    },
                                    onDragCancel = {
                                        activeHandle = -1
                                    }
                                )
                            } else if (drawMode && showDraw) {
                                detectDragGestures(
                                    onDragStart = { start ->
                                        activeDrawPoints = listOf(start)
                                    },
                                    onDrag = { change, _ ->
                                        activeDrawPoints =
                                            activeDrawPoints + change.position
                                    },
                                    onDragEnd = {
                                        addDrawPath()
                                    },
                                    onDragCancel = {
                                        activeDrawPoints = emptyList()
                                    }
                                )
                            } else {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    scale = (scale * zoom).coerceIn(1f, 6f)

                                    if (scale > 1f && cropViewWidth > 0f && cropViewHeight > 0f) {
                                        val imageWidth = currentBitmap.width.toFloat()
                                        val imageHeight = currentBitmap.height.toFloat()
                                        val imageAspect = imageWidth / imageHeight
                                        val viewAspect = cropViewWidth / cropViewHeight

                                        val displayedWidth: Float
                                        val displayedHeight: Float

                                        if (imageAspect > viewAspect) {
                                            displayedWidth = cropViewWidth
                                            displayedHeight = cropViewWidth / imageAspect
                                        } else {
                                            displayedHeight = cropViewHeight
                                            displayedWidth = cropViewHeight * imageAspect
                                        }

                                        val scaledWidth = displayedWidth * scale
                                        val scaledHeight = displayedHeight * scale

                                        val maxOffsetX =
                                            maxOf(0f, (scaledWidth - cropViewWidth) / 2f)
                                        val maxOffsetY =
                                            maxOf(0f, (scaledHeight - cropViewHeight) / 2f)

                                        offsetX = (offsetX + pan.x)
                                            .coerceIn(-maxOffsetX, maxOffsetX)

                                        offsetY = (offsetY + pan.y)
                                            .coerceIn(-maxOffsetY, maxOffsetY)
                                    } else {
                                        offsetX = 0f
                                        offsetY = 0f
                                    }
                                }
                            }
                        }
                ) {
                    LaunchedEffect(currentBitmap) {
                        if (slideDirection != 0) {
                            kotlinx.coroutines.delay(320)
                            slideDirection = 0
                        }
                    }

                    AnimatedContent(
                        targetState = currentBitmap,
                        transitionSpec = {
                            if (slideDirection > 0) {
                                slideInHorizontally(
                                    animationSpec = tween(280),
                                    initialOffsetX = { it }
                                ) togetherWith slideOutHorizontally(
                                    animationSpec = tween(280),
                                    targetOffsetX = { -it }
                                )
                            } else if (slideDirection < 0) {
                                slideInHorizontally(
                                    animationSpec = tween(280),
                                    initialOffsetX = { -it }
                                ) togetherWith slideOutHorizontally(
                                    animationSpec = tween(280),
                                    targetOffsetX = { it }
                                )
                            } else {
                                slideInHorizontally(
                                    animationSpec = tween(0),
                                    initialOffsetX = { 0 }
                                ) togetherWith slideOutHorizontally(
                                    animationSpec = tween(0),
                                    targetOffsetX = { 0 }
                                )
                            }
                        },
                        label = "image_slide"
                    ) { animatedBitmap ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    val previewScale = if (cropMode) cropPreviewScale else 1f
                                    scaleX = scale * previewScale
                                    scaleY = scale * previewScale
                                    translationX = offsetX
                                    translationY = offsetY
                                }
                                .drawBehind {
                                    if (animatedBitmap.hasAlpha()) {
                                    val cellSize = 32.dp.toPx()
                                    val light = Color(0xFFE0E0E0)
                                    val dark = Color(0xFFB8B8B8)

                                    val imageAspect =
                                        animatedBitmap.width.toFloat() / animatedBitmap.height.toFloat()
                                    val viewAspect = size.width / size.height

                                    val imageWidth: Float
                                    val imageHeight: Float

                                    if (imageAspect > viewAspect) {
                                        imageWidth = size.width
                                        imageHeight = size.width / imageAspect
                                    } else {
                                        imageHeight = size.height
                                        imageWidth = size.height * imageAspect
                                    }

                                    val left = (size.width - imageWidth) / 2f
                                    val top = (size.height - imageHeight) / 2f

                                    clipRect(
                                        left = left,
                                        top = top,
                                        right = left + imageWidth,
                                        bottom = top + imageHeight
                                    ) {
                                        drawRect(light)

                                        val columns = (imageWidth / cellSize).toInt() + 1
                                        val rows = (imageHeight / cellSize).toInt() + 1

                                        for (row in 0 until rows) {
                                            for (column in 0 until columns) {
                                                if ((row + column) % 2 == 0) {
                                                    drawRect(
                                                        color = dark,
                                                        topLeft = androidx.compose.ui.geometry.Offset(
                                                            left + column * cellSize,
                                                            top + row * cellSize
                                                        ),
                                                        size = androidx.compose.ui.geometry.Size(
                                                            cellSize,
                                                            cellSize
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    }
                                
                                    }
                                }
                        ) {
                            androidx.compose.foundation.Image(
                                bitmap = animatedBitmap.asImageBitmap(),
                                contentDescription = file.name,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxSize()
                            )
                        }
                    }

                    if (showDraw) {
                        androidx.compose.foundation.Canvas(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            drawPaths.forEach { pathData ->
                                if (pathData.points.size >= 2) {
                                    val path = androidx.compose.ui.graphics.Path()

                                    pathData.points.forEachIndexed { index, point ->
                                        if (index == 0) {
                                            path.moveTo(point.x, point.y)
                                        } else {
                                            path.lineTo(point.x, point.y)
                                        }
                                    }

                                    drawPath(
                                        path = path,
                                        color = pathData.color,
                                        style = Stroke(
                                            width = pathData.strokeWidth,
                                            cap = androidx.compose.ui.graphics.StrokeCap.Round,
                                            join = androidx.compose.ui.graphics.StrokeJoin.Round
                                        )
                                    )
                                }
                            }

                            if (activeDrawPoints.size >= 2) {
                                val path = androidx.compose.ui.graphics.Path()

                                activeDrawPoints.forEachIndexed { index, point ->
                                    if (index == 0) {
                                        path.moveTo(point.x, point.y)
                                    } else {
                                        path.lineTo(point.x, point.y)
                                    }
                                }

                                drawPath(
                                    path = path,
                                    color = drawColor,
                                    style = Stroke(
                                        width = drawStrokeWidth,
                                        cap = androidx.compose.ui.graphics.StrokeCap.Round,
                                        join = androidx.compose.ui.graphics.StrokeJoin.Round
                                    )
                                )
                            }
                        }
                    }

                    if (showDraw && textOverlays.isNotEmpty()) {
                        textOverlays.forEachIndexed { index, item ->
                            val isSelected = selectedTextIndex == index

                            androidx.compose.foundation.layout.Box(
                                modifier = Modifier
                                    .offset {
                                        androidx.compose.ui.unit.IntOffset(
                                            (item.position.x - item.width / 2f).toInt(),
                                            (item.position.y - item.height / 2f).toInt()
                                        )
                                    }
                                    .width(
                                        with(LocalDensity.current) {
                                            item.width.toDp()
                                        }
                                    )
                                    .height(
                                        with(LocalDensity.current) {
                                            item.height.toDp()
                                        }
                                    )
                                    .pointerInput(index) {
                                        detectDragGestures(
                                            onDragStart = {
                                                selectedTextIndex = index
                                            },
                                            onDrag = { change, dragAmount ->
                                                change.consume()

                                                if (index in textOverlays.indices) {
                                                    val current = textOverlays[index]
                                                    textOverlays[index] =
                                                        current.copy(
                                                            position =
                                                                current.position + dragAmount
                                                        )
                                                }
                                            }
                                        )
                                    }
                            ) {
                                if (isSelected) {
                                    BasicTextField(
                                        value = item.text,
                                        onValueChange = { value ->
                                            if (index in textOverlays.indices) {
                                                textOverlays[index] =
                                                    textOverlays[index].copy(text = value)
                                            }
                                        },
                                        textStyle = TextStyle(
                                            color = item.color,
                                            fontSize = with(LocalDensity.current) {
                                                item.fontSize.toSp()
                                            },
                                            fontWeight = FontWeight.Normal,
                                            textAlign =
                                                androidx.compose.ui.text.style.TextAlign.Center
                                        ),
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .focusRequester(textFocusRequester)
                                            .wrapContentHeight(
                                                androidx.compose.ui.Alignment.CenterVertically
                                            )
                                            .padding(
                                                start = 8.dp,
                                                end = 8.dp
                                            ),
                                        singleLine = false
                                    )
                                } else {
                                    androidx.compose.material3.Text(
                                        text = item.text,
                                        color = item.color,
                                        fontSize = with(LocalDensity.current) {
                                            item.fontSize.toSp()
                                        },
                                        fontWeight = FontWeight.Normal,
                                        textAlign =
                                            androidx.compose.ui.text.style.TextAlign.Center,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .wrapContentHeight(
                                                androidx.compose.ui.Alignment.CenterVertically
                                            )
                                            .padding(
                                                start = 8.dp,
                                                end = 8.dp
                                            )
                                    )
                                }
                            }
                        }
                    }

                    if (cropMode && cropStart != null && cropEnd != null) {
                        val start = cropStart!!
                        val end = cropEnd!!

                        androidx.compose.foundation.Canvas(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            val left = minOf(start.x, end.x)
                            val right = maxOf(start.x, end.x)
                            val top = minOf(start.y, end.y)
                            val bottom = maxOf(start.y, end.y)
                            val midX = (left + right) / 2f
                            val midY = (top + bottom) / 2f

                            drawRect(
                                color = Color.White,
                                topLeft = Offset(left, top),
                                size = androidx.compose.ui.geometry.Size(
                                    right - left,
                                    bottom - top
                                ),
                                style = Stroke(width = 3f)
                            )

                            val handles = listOf(
                                Offset(left, top),
                                Offset(midX, top),
                                Offset(right, top),
                                Offset(right, midY),
                                Offset(right, bottom),
                                Offset(midX, bottom),
                                Offset(left, bottom),
                                Offset(left, midY)
                            )

                            handles.forEach { handle ->
                                drawCircle(
                                    color = Color.Black,
                                    radius = 11f,
                                    center = handle
                                )
                                drawCircle(
                                    color = Color.White,
                                    radius = 8f,
                                    center = handle
                                )
                            }
                        }
                    }
                }
            }

            if (showDraw) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF242424))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        "Draw",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        TextButton(
                            onClick = {
                                drawMode = true
                                showDraw = true
                            },
                            modifier = Modifier
                                .background(
                                    if (drawMode) Color(0xFF555555) else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                        ) {
                            Text("Freehand", color = Color.White)
                        }

                        TextButton(
                            onClick = {
                                drawMode = false
                                showDraw = true
                                addTextOverlay()
                            },
                            modifier = Modifier
                                .background(
                                    if (!drawMode) Color(0xFF555555) else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                        ) {
                            Text("Add Text", color = Color.White)
                        }

                        TextButton(
                            onClick = {
                                clearDrawObjects()
                            }
                        ) {
                            Text("Clear", color = Color.White)
                        }
                    }

                    if (!drawMode) {
                        Text(
                            "Font Size: ${textFontSize.toInt()}",
                            color = Color.White
                        )

                        Slider(
                            value = textFontSize,
                            onValueChange = { newSize ->
                                textFontSize = newSize

                                selectedTextIndex?.let { index ->
                                    if (index in textOverlays.indices) {
                                        val current = textOverlays[index]
                                        val scale = newSize / current.fontSize

                                        val newWidth = (current.width * scale)
                                            .coerceIn(80f, 1600f)

                                        val newHeight = (current.height * scale)
                                            .coerceIn(48f, 900f)

                                        textOverlays[index] =
                                            current.copy(
                                                fontSize = newSize,
                                                width = newWidth,
                                                height = newHeight
                                            )
                                    }
                                }
                            },
                            valueRange = 16f..300f
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = {
                                    previousTextColor = textColor
                                    pendingTextColor = textColor
                                    showTextColorPicker = true
                                }
                            ) {
                                Text("Color", color = Color.White)
                            }
                        }
                                        } else {
                        Text(
                            "Thickness",
                            color = Color.White
                        )

                        Slider(
                            value = drawStrokeWidth,
                            onValueChange = { drawStrokeWidth = it },
                            valueRange = 1f..30f
                        )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(
                                    onClick = {
                                        previousDrawColor = drawColor
                                        pendingPickerColor = drawColor
                                        drawColorPickerActive = true
                                    }
                                ) {
                                    Text("Color", color = Color.White)
                                }
                            }
                    }
                }
            }

            if (showTextColorPicker) {
        AlertDialog(
            onDismissRequest = {
                showTextColorPicker = false
            },
            title = {
                Text("Text Color")
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    androidx.compose.foundation.Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .pointerInput(Unit) {
                                detectTapGestures { position ->
                                    val canvasWidth =
                                        size.width.toFloat().coerceAtLeast(1f)
                                    val canvasHeight =
                                        size.height.toFloat().coerceAtLeast(1f)

                                    val x = position.x.coerceIn(0f, canvasWidth)
                                    val y = position.y.coerceIn(0f, canvasHeight)

                                    textSaturation =
                                        (x / canvasWidth).coerceIn(0f, 1f)

                                    textValue =
                                        (1f - y / canvasHeight).coerceIn(0f, 1f)

                                    pendingTextColor = Color.hsv(
                                        textHue,
                                        textSaturation,
                                        textValue
                                    )
                                }
                            }
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDrag = { change, _ ->
                                        val canvasWidth =
                                            size.width.toFloat().coerceAtLeast(1f)
                                        val canvasHeight =
                                            size.height.toFloat().coerceAtLeast(1f)

                                        val x = change.position.x
                                            .coerceIn(0f, canvasWidth)
                                        val y = change.position.y
                                            .coerceIn(0f, canvasHeight)

                                        textSaturation =
                                            (x / canvasWidth)
                                                .coerceIn(0f, 1f)

                                        textValue =
                                            (1f - y / canvasHeight)
                                                .coerceIn(0f, 1f)

                                        pendingTextColor = Color.hsv(
                                            textHue,
                                            textSaturation,
                                            textValue
                                        )
                                    }
                                )
                            }
                    ) {
                        val hueColor = Color.hsv(textHue, 1f, 1f)

                        drawRect(
                            color = hueColor,
                            size = size
                        )

                        drawRect(
                            brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                listOf(
                                    Color.White,
                                    Color.Transparent
                                )
                            ),
                            size = size
                        )

                        drawRect(
                            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                listOf(
                                    Color.Transparent,
                                    Color.Black
                                )
                            ),
                            size = size
                        )

                        drawCircle(
                            color = Color.White,
                            radius = 10f,
                            center = Offset(
                                textSaturation * size.width,
                                (1f - textValue) * size.height
                            )
                        )

                        drawCircle(
                            color = Color.Black,
                            radius = 12f,
                            center = Offset(
                                textSaturation * size.width,
                                (1f - textValue) * size.height
                            ),
                            style = Stroke(width = 2f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Slider(
                        value = textHue,
                        onValueChange = {
                            textHue = it
                            pendingTextColor = Color.hsv(
                                textHue,
                                textSaturation,
                                textValue
                            )
                        },
                        valueRange = 0f..360f
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(androidx.compose.foundation.shape.CircleShape)
                                    .background(previousTextColor)
                            )
                            Text("Previous")
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(androidx.compose.foundation.shape.CircleShape)
                                    .background(pendingTextColor)
                            )
                            Text("Selected")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        textColor = pendingTextColor

                        selectedTextIndex?.let { index ->
                            if (index in textOverlays.indices) {
                                textOverlays[index] =
                                    textOverlays[index].copy(
                                        color = pendingTextColor
                                    )
                            }
                        }

                        showTextColorPicker = false
                    }
                ) {
                    Text("Apply")
                }
            }
        )
    }

    if (drawColorPickerActive) {
        AlertDialog(
            onDismissRequest = {
                drawColorPickerActive = false
            },
            title = {
                Text("Draw Color")
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    androidx.compose.foundation.Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .pointerInput(Unit) {
                                detectTapGestures { position ->
                                    val canvasWidth =
                                        size.width.toFloat().coerceAtLeast(1f)
                                    val canvasHeight =
                                        size.height.toFloat().coerceAtLeast(1f)

                                    val x = position.x.coerceIn(0f, canvasWidth)
                                    val y = position.y.coerceIn(0f, canvasHeight)

                                    pickerSaturation =
                                        (x / canvasWidth).coerceIn(0f, 1f)

                                    pickerValue =
                                        (1f - y / canvasHeight).coerceIn(0f, 1f)

                                    pendingPickerColor = Color.hsv(
                                        pickerHue,
                                        pickerSaturation,
                                        pickerValue
                                    )
                                }
                            }
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDrag = { change, _ ->
                                        val canvasWidth =
                                            size.width.toFloat().coerceAtLeast(1f)
                                        val canvasHeight =
                                            size.height.toFloat().coerceAtLeast(1f)

                                        val x = change.position.x
                                            .coerceIn(0f, canvasWidth)
                                        val y = change.position.y
                                            .coerceIn(0f, canvasHeight)

                                        pickerSaturation =
                                            (x / canvasWidth).coerceIn(0f, 1f)

                                        pickerValue =
                                            (1f - y / canvasHeight)
                                                .coerceIn(0f, 1f)

                                        pendingPickerColor = Color.hsv(
                                            pickerHue,
                                            pickerSaturation,
                                            pickerValue
                                        )
                                    }
                                )
                            }
                    ) {
                        val hueColor = Color.hsv(
                            pickerHue,
                            1f,
                            1f
                        )

                        drawRect(
                            color = hueColor,
                            size = size
                        )

                        drawRect(
                            brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                listOf(
                                    Color.White,
                                    Color.Transparent
                                )
                            ),
                            size = size
                        )

                        drawRect(
                            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                listOf(
                                    Color.Transparent,
                                    Color.Black
                                )
                            ),
                            size = size
                        )

                        drawCircle(
                            color = Color.White,
                            radius = 10f,
                            center = Offset(
                                pickerSaturation * size.width,
                                (1f - pickerValue) * size.height
                            )
                        )

                        drawCircle(
                            color = Color.Black,
                            radius = 12f,
                            center = Offset(
                                pickerSaturation * size.width,
                                (1f - pickerValue) * size.height
                            ),
                            style = Stroke(width = 2f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Slider(
                        value = pickerHue,
                        onValueChange = {
                            pickerHue = it
                            pendingPickerColor = Color.hsv(
                                pickerHue,
                                pickerSaturation,
                                pickerValue
                            )
                        },
                        valueRange = 0f..360f
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(
                                        androidx.compose.foundation.shape.CircleShape
                                    )
                                    .background(previousDrawColor)
                            )
                            Text("Previous")
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(
                                        androidx.compose.foundation.shape.CircleShape
                                    )
                                    .background(pendingPickerColor)
                            )
                            Text("Selected")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        drawColor = pendingPickerColor
                        drawColorPickerActive = false
                    }
                ) {
                    Text("Apply")
                }
            }
        )
    }

    if (showAdjustments) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF242424))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("Adjustments", color = Color.White, fontWeight = FontWeight.Bold)
                    Text("Brightness", color = Color.White)
                    Slider(
                        value = brightness,
                        onValueChange = {
                            brightness = it
                            previewAdjustments()
                        },
                        valueRange = -1f..1f
                    )
                    Text("Contrast", color = Color.White)
                    Slider(
                        value = contrast,
                        onValueChange = {
                            contrast = it
                            previewAdjustments()
                        },
                        valueRange = -1f..1f
                    )
                    Text("Saturation", color = Color.White)
                    Slider(
                        value = saturation,
                        onValueChange = {
                            saturation = it
                            previewAdjustments()
                        },
                        valueRange = -1f..1f
                    )
                    Text("Exposure", color = Color.White)
                    Slider(
                        value = exposure,
                        onValueChange = {
                            exposure = it
                            previewAdjustments()
                        },
                        valueRange = -1f..1f
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = {
                            brightness = 0f
                            contrast = 0f
                            saturation = 0f
                            exposure = 0f
                            adjustmentBase?.let { base ->
                                bitmap = base.copy(
                                    base.config ?: Bitmap.Config.ARGB_8888,
                                    true
                                )
                            }
                        }) { Text("Reset", color = Color.White) }
                        TextButton(onClick = {
                            adjustmentBase?.let { base ->
                                bitmap = base.copy(
                                    base.config ?: Bitmap.Config.ARGB_8888,
                                    true
                                )
                            }

                            brightness = 0f
                            contrast = 0f
                            saturation = 0f
                            exposure = 0f
                            adjustmentBase = null
                            showAdjustments = false
                        }) { Text("Cancel", color = Color.White) }
                        TextButton(onClick = {
                            val base = adjustmentBase
                            val current = bitmap

                            if (base != null && current != null) {
                                val adjusted = applyColorAdjustments(
                                    source = base,
                                    brightness = brightness,
                                    contrast = contrast,
                                    saturation = saturation,
                                    exposure = exposure
                                )

                                val finalBitmap = adjusted

                                undoStack.value = undoStack.value +
                                    base.copy(
                                        base.config ?: Bitmap.Config.ARGB_8888,
                                        true
                                    )
                                redoStack.value = emptyList()
                                bitmap = finalBitmap
                            }

                            adjustmentBase = null
                            showAdjustments = false
                        }) { Text("Apply", color = Color.White) }
                    }
                }
            }

            Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(114.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 50.dp)
                    .height(64.dp)
                    .background(
                        if (showImageControls) {
                            Color(0xFF242424)
                        } else {
                            Color.Transparent
                        }
                    )
                    .graphicsLayer {
                        alpha = if (showImageControls) 1f else 0f
                    }
                    .horizontalScroll(
                        androidx.compose.foundation.rememberScrollState()
                    ),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                    IconButton(
                        modifier = Modifier.size(40.dp),
                        enabled = if (showDraw) {
                            drawUndoStack.value.isNotEmpty()
                        } else {
                            undoStack.value.isNotEmpty()
                        },
                        onClick = { undo() }
                    ) {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            painter = painterResource(id = com.example.R.drawable.undo),
                            contentDescription = "Undo",
                            tint = if (
                                if (showDraw) {
                                    drawUndoStack.value.isNotEmpty()
                                } else {
                                    undoStack.value.isNotEmpty()
                                }
                            ) {
                                Color.White
                            } else {
                                Color.Gray
                            }
                        )
                    }

                    IconButton(
                        modifier = Modifier.size(40.dp),
                        enabled = if (showDraw) {
                            drawRedoStack.value.isNotEmpty()
                        } else {
                            redoStack.value.isNotEmpty()
                        },
                        onClick = { redo() }
                    ) {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            painter = painterResource(id = com.example.R.drawable.redo),
                            contentDescription = "Redo",
                            tint = if (
                                if (showDraw) {
                                    drawRedoStack.value.isNotEmpty()
                                } else {
                                    redoStack.value.isNotEmpty()
                                }
                            ) {
                                Color.White
                            } else {
                                Color.Gray
                            }
                        )
                    }

                    IconButton(
                        modifier = Modifier.size(40.dp),
                        enabled = bitmap != null,
                        onClick = {
                            applyTransform(::rotateBitmap)
                            scale = 1f
                            offsetX = 0f
                            offsetY = 0f
                        }
                    ) {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            painter = painterResource(id = com.example.R.drawable.rotate_cw_square),
                            contentDescription = "Rotate",
                            tint = Color.White
                        )
                    }

                    IconButton(
                        modifier = Modifier.size(40.dp),
                        enabled = bitmap != null,
                        onClick = {
                            applyTransform(::flipHorizontal)
                            scale = 1f
                            offsetX = 0f
                            offsetY = 0f
                        }
                    ) {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            painter = painterResource(id = com.example.R.drawable.square_centerline_dashed_horizontal),
                            contentDescription = "Flip Horizontal",
                            tint = Color.White
                        )
                    }

                    IconButton(
                        modifier = Modifier.size(40.dp),
                        enabled = bitmap != null,
                        onClick = {
                            applyTransform(::flipVertical)
                            scale = 1f
                            offsetX = 0f
                            offsetY = 0f
                        }
                    ) {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            painter = painterResource(id = com.example.R.drawable.square_centerline_dashed_vertical),
                            contentDescription = "Flip Vertical",
                            tint = Color.White
                        )
                    }

                    IconButton(
                        modifier = Modifier.size(40.dp),
                        enabled = bitmap != null,
                        onClick = {
                            if (cropMode && cropStart != null && cropEnd != null) {
                                val start = cropStart!!
                                val end = cropEnd!!
                                if (cropViewWidth > 0f && cropViewHeight > 0f) {
                                    applyTransform { source ->
                                        cropBitmap(
                                            source,
                                            start,
                                            end,
                                            cropViewWidth,
                                            cropViewHeight
                                        )
                                    }
                                }

                                cropMode = false
                                cropStart = null
                                cropEnd = null
                                scale = 1f
                                offsetX = 0f
                                offsetY = 0f
                            } else {
                                if (!cropMode) {
                                    scale = 1f
                                    offsetX = 0f
                                    offsetY = 0f
                                }
                                cropMode = !cropMode
                                cropStart = null
                                cropEnd = null
                            }
                        }
                    ) {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            painter = painterResource(id = com.example.R.drawable.crop),
                            contentDescription = "Crop",
                            tint = if (cropMode) MaterialTheme.colorScheme.primary else Color.White
                        )
                    }

                    IconButton(
                        modifier = Modifier.size(40.dp),
                        enabled = bitmap != null,
                        onClick = {
                            if (showDraw || drawMode) {
                                applyAndCloseDraw()
                            } else {
                                drawSessionBitmap = bitmap?.copy(
                                    bitmap?.config ?: Bitmap.Config.ARGB_8888,
                                    true
                                )
                                drawSessionUndoStack = undoStack.value
                                drawSessionRedoStack = redoStack.value

                                drawPaths.clear()
                                textOverlays.clear()
                                activeDrawPoints = emptyList()
                                selectedTextIndex = null
                                textInput = ""
                                drawUndoStack.value = emptyList()
                                drawRedoStack.value = emptyList()

                                cropMode = false
                                cropStart = null
                                cropEnd = null
                                drawMode = true
                                showDraw = true
                                showAdjustments = false
                            }
                        }
                    ) {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            painter = painterResource(id = com.example.R.drawable.draw),
                            contentDescription = "Draw",
                            tint = Color.White
                        )
                    }

                    if (cropMode && cropStart != null && cropEnd != null) {
                        androidx.compose.material3.TextButton(
                            onClick = {
                                val start = cropStart!!
                                val end = cropEnd!!
                                if (cropViewWidth > 0f && cropViewHeight > 0f) {
                                    applyTransform { source ->
                                        cropBitmap(
                                            source,
                                            start,
                                            end,
                                            cropViewWidth,
                                            cropViewHeight
                                        )
                                    }
                                }
                                cropMode = false
                                cropStart = null
                                cropEnd = null
                                scale = 1f
                                offsetX = 0f
                                offsetY = 0f
                            }
                        ) {
                            Text("Apply", color = Color.White)
                        }
                    }

                    if (showDraw || drawMode) {
                        androidx.compose.material3.TextButton(
                            onClick = { applyAndCloseDraw() }
                        ) {
                            Text("Apply", color = Color.White)
                        }
                    }

                    if (cropMode || drawMode || showDraw) {
                    IconButton(
                        modifier = Modifier.size(40.dp),
                        onClick = {
                            if (showDraw || drawMode) {
                                showDrawApplyDiscardDialog = true
                            } else {
                                cropMode = false
                                cropStart = null
                                cropEnd = null
                            }
                        }
                    ) {
                        Icon(
                            imageVector = LucideIcons.X,
                            contentDescription = "Cancel Crop or Draw",
                            tint = Color.White
                        )
                    }
                }

                IconButton(
                        modifier = Modifier.size(40.dp),
                        enabled = bitmap != null,
                        onClick = {
                            if (!showAdjustments) {
                                adjustmentBase = bitmap?.copy(
                                    bitmap?.config ?: Bitmap.Config.ARGB_8888,
                                    true
                                )
                                brightness = 0f
                                contrast = 0f
                                saturation = 0f
                                exposure = 0f
                                }
                            showAdjustments = !showAdjustments
                        }
                    ) {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            painter = painterResource(id = com.example.R.drawable.adjustments),
                            contentDescription = "Adjustments",
                            tint = Color.White
                        )
                    }

                IconButton(
                        modifier = Modifier.size(40.dp),
                        enabled = bitmap != null,
                        onClick = { saveBitmap() }
                    ) {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            imageVector = LucideIcons.Save,
                            contentDescription = "Save",
                            tint = Color.White
                        )
                    }
            }

            }
        }
        Spacer(modifier = Modifier.height(0.dp))
    }
}

@Composable
fun FileConflictDialog(
    source: File,
    destination: File,
    isCustomTheme: Boolean = false,
    onOverwrite: (Boolean) -> Unit,
    onSkip: (Boolean) -> Unit,
    onRename: (Boolean) -> Unit
) {
    var applyToAll by remember { mutableStateOf(false) }
    Dialog(
        onDismissRequest = { onSkip(applyToAll) }
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.88f)
                    .border(
                        width = 0.5.dp,
                        color = if (isCustomTheme) {
                            Color.White.copy(alpha = 0.5f)
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                        shape = MaterialTheme.shapes.extraLarge
                    ),
                shape = MaterialTheme.shapes.extraLarge,
                color = if (isCustomTheme) {
                    Color(0xFF3C0020).copy(alpha = 0.8f)
                } else {
                    MaterialTheme.colorScheme.surfaceContainerHigh
                },
                contentColor = if (isCustomTheme) Color.White else MaterialTheme.colorScheme.onSurface,
                        tonalElevation = if (isCustomTheme) 0.dp else 6.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = LucideIcons.TriangleAlert,
                            contentDescription = null,
                            tint = if (isCustomTheme) Color.White else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            "File already exists",
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }

                    Text(
                        "A file with the same name already exists in this folder."
                    )

                    Text(
                        destination.name,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = applyToAll,
                            onCheckedChange = { applyToAll = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = if (isCustomTheme) Color.White else MaterialTheme.colorScheme.primary,
                                uncheckedColor = if (isCustomTheme) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                checkmarkColor = if (isCustomTheme) Color(0xFF3C0020) else MaterialTheme.colorScheme.onPrimary
                            )
                        )
                        Text("Apply to all conflicts")
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = { onSkip(applyToAll) }
                        ) {
                            Text("Skip", color = if (isCustomTheme) Color.White else MaterialTheme.colorScheme.primary)
                        }

                        TextButton(
                            onClick = { onRename(applyToAll) }
                        ) {
                            Text("Rename", color = if (isCustomTheme) Color.White else MaterialTheme.colorScheme.primary)
                        }

                        Button(
                            onClick = { onOverwrite(applyToAll) },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isCustomTheme) Color.White else MaterialTheme.colorScheme.primary,
                                contentColor = if (isCustomTheme) Color(0xFF3C0020) else MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text("Overwrite")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CreateFolderDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
,
    isCustomTheme: Boolean = false,
    isLightTheme: Boolean = false) {
    var name by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.88f)
                    .border(
                        width = 1.dp,
                        color = if (isCustomTheme)
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        else MaterialTheme.colorScheme.primary,
                        shape = MaterialTheme.shapes.extraLarge
                    ),
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = if (isLightTheme) 0.dp else 6.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            LucideIcons.Folder,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            "Create New Folder",
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Folder Name") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (isCustomTheme) {
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                            unfocusedBorderColor = if (isCustomTheme) {
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                            errorBorderColor = if (isCustomTheme) {
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            } else {
                                MaterialTheme.colorScheme.primary
                            }
                        ),
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = { onConfirm(name) },
                            enabled = name.isNotBlank(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Create")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CreateFileDialog(
    onConfirm: (name: String, content: String) -> Unit,
    onDismiss: () -> Unit
,
    isCustomTheme: Boolean = false,
    isLightTheme: Boolean = false) {
    var name by remember { mutableStateOf("new_note.txt") }
    var content by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.88f)
                    .border(
                        width = 1.dp,
                        color = if (isCustomTheme)
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        else MaterialTheme.colorScheme.primary,
                        shape = MaterialTheme.shapes.extraLarge
                    ),
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = if (isLightTheme) 0.dp else 6.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            LucideIcons.FileText,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            "Create New Text File",
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("File Name (e.g. note.txt)") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (isCustomTheme) Color.White else MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = if (isCustomTheme) Color.White else MaterialTheme.colorScheme.primary,
                            errorBorderColor = if (isCustomTheme) Color.White else MaterialTheme.colorScheme.primary
                        ),
                    )

                    androidx.compose.material3.Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isCustomTheme) Color.White
                            else MaterialTheme.colorScheme.primary
                        )
                    ) {
                        androidx.compose.ui.viewinterop.AndroidView(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 100.dp),
                            factory = { context ->
                                android.widget.EditText(context).apply {
                                    hint = "Initial Content (Optional)"
                                    gravity = android.view.Gravity.TOP or android.view.Gravity.START
                                    setPadding(24, 20, 24, 16)
                                    background = null
                                    minLines = 3
                                    isSingleLine = false
                                    inputType = android.text.InputType.TYPE_CLASS_TEXT or
                                        android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE or
                                        android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
                                    setTextColor(android.graphics.Color.BLACK)
                                    setHintTextColor(android.graphics.Color.GRAY)
                                    addTextChangedListener(object : android.text.TextWatcher {
                                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                                            content = s?.toString() ?: ""
                                        }
                                        override fun afterTextChanged(s: android.text.Editable?) {}
                                    })
                                    setText(content)
                                }
                            },
                            update = { editText ->
                                if (editText.text.toString() != content) {
                                    editText.setText(content)
                                    editText.setSelection(editText.text.length)
                                }
                            }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = { onConfirm(name, content) },
                            enabled = name.isNotBlank(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Create")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RenameDialog(
    currentName: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
,
    isCustomTheme: Boolean = false,
    isLightTheme: Boolean = false) {
    var name by remember {
        val extensionIndex = currentName.lastIndexOf('.')
        val selectionEnd =
            if (extensionIndex > 0) extensionIndex else currentName.length

        mutableStateOf(
            TextFieldValue(
                text = currentName,
                selection = TextRange(0, selectionEnd)
            )
        )
    }

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    Dialog(
        onDismissRequest = onDismiss
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.88f)
                    .border(
                        width = 1.dp,
                        color = if (isCustomTheme)
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        else MaterialTheme.colorScheme.primary,
                        shape = MaterialTheme.shapes.extraLarge
                    ),
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = if (isLightTheme) 0.dp else 6.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Rename",
                        style = MaterialTheme.typography.headlineSmall
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("New Name") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (isCustomTheme) {
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                            unfocusedBorderColor = if (isCustomTheme) {
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                            errorBorderColor = if (isCustomTheme) {
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            } else {
                                MaterialTheme.colorScheme.primary
                            }
                        ),
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = { onConfirm(name.text) },
                            enabled = name.text.isNotBlank() && name.text != currentName,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Confirm")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DeleteConfirmDialog(
    items: List<FileItem>,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
,
    isCustomTheme: Boolean = false,
    isLightTheme: Boolean = false) {
    Dialog(
        onDismissRequest = onDismiss
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.88f)
                    .border(
                        width = 1.dp,
                        color = if (isCustomTheme)
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        else MaterialTheme.colorScheme.primary,
                        shape = MaterialTheme.shapes.extraLarge
                    ),
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = if (isLightTheme) 0.dp else 6.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Confirm Deletion",
                        style = MaterialTheme.typography.headlineSmall
                    )

                    Text(
                        if (items.size == 1) {
                            "Are you sure you want to delete \"${items[0].name}\"? This action cannot be undone."
                        } else {
                            "Are you sure you want to delete ${items.size} selected items? This action cannot be undone."
                        }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = onConfirm,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFF0000)
                            )
                        ) {
                            Text("Delete")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ZipDialog(
    defaultName: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
,
    isCustomTheme: Boolean = false,
    isLightTheme: Boolean = false) {
    var name by remember { mutableStateOf(defaultName) }

    Dialog(
        onDismissRequest = onDismiss
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.88f)
                    .border(
                        width = 1.dp,
                        color = if (isCustomTheme)
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        else MaterialTheme.colorScheme.primary,
                        shape = MaterialTheme.shapes.extraLarge
                    ),
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = if (isLightTheme) 0.dp else 6.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            LucideIcons.FolderArchive,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            "Create ZIP Archive",
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Archive Name") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (isCustomTheme) {
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                            unfocusedBorderColor = if (isCustomTheme) {
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                            errorBorderColor = if (isCustomTheme) {
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            } else {
                                MaterialTheme.colorScheme.primary
                            }
                        ),
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = { onConfirm(name) },
                            enabled = name.isNotBlank(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Create")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DeleteBookmarkDialog(
    bookmarkName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
,
    isCustomTheme: Boolean = false,
    isLightTheme: Boolean = false) {
    Dialog(
        onDismissRequest = onDismiss
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.88f)
                    .border(
                        width = 1.dp,
                        color = if (isCustomTheme)
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        else MaterialTheme.colorScheme.primary,
                        shape = MaterialTheme.shapes.extraLarge
                    ),
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            LucideIcons.Trash2,
                            contentDescription = null,
                            tint = Color(0xFFFF0000),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            "Delete Bookmark",
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }

                    Text(
                        "Are you sure you want to delete \"$bookmarkName\"?"
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = onConfirm,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFF0000)
                            )
                        ) {
                            Text("Delete")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddFolderBookmarkDialog(
    defaultName: String = "My Folder",
    folderPath: String = "",
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var bookmarkName by remember { mutableStateOf(defaultName) }

    Dialog(
        onDismissRequest = onDismiss
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(0.88f),
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            LucideIcons.Folder,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            "Add to Bookmarks",
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }

                    Text(
                        "Add folder to your bookmarks for quick access:",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    if (folderPath.isNotEmpty()) {
                        Text(
                            text = folderPath,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    OutlinedTextField(
                        value = bookmarkName,
                        onValueChange = { bookmarkName = it },
                        label = { Text("Bookmark Name") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.primary,
                            errorBorderColor = MaterialTheme.colorScheme.primary
                        ),
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = {
                                onConfirm(bookmarkName.ifBlank { defaultName })
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Add Bookmark")
                        }
                    }
                }
            }
        }
    }
}
