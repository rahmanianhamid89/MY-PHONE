package com.example.ui.components
import com.example.ui.icons.LucideIcons

import android.media.MediaPlayer
import android.net.Uri
import android.widget.MediaController
import android.widget.VideoView
import android.content.SharedPreferences
import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import android.media.AudioManager
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.LocalTextStyle
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.util.FileUtils
import java.io.File
import kotlin.math.roundToInt

private val PersianSubtitleFont = FontFamily(
    Font(com.example.R.font.persian)
)

private fun formatVideoTime(milliseconds: Int): String {
    val totalSeconds = (milliseconds / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}

private fun calculateVideoDisplayRect(
    videoWidth: Int,
    videoHeight: Int,
    containerWidth: Int,
    containerHeight: Int
): androidx.compose.ui.geometry.Rect {
    if (
        videoWidth <= 0 ||
        videoHeight <= 0 ||
        containerWidth <= 0 ||
        containerHeight <= 0
    ) {
        return androidx.compose.ui.geometry.Rect(
            0f,
            0f,
            containerWidth.toFloat(),
            containerHeight.toFloat()
        )
    }

    val videoAspect = videoWidth.toFloat() / videoHeight.toFloat()
    val containerAspect = containerWidth.toFloat() / containerHeight.toFloat()

    val displayWidth: Float
    val displayHeight: Float

    if (videoAspect > containerAspect) {
        displayWidth = containerWidth.toFloat()
        displayHeight = displayWidth / videoAspect
    } else {
        displayHeight = containerHeight.toFloat()
        displayWidth = displayHeight * videoAspect
    }

    val left = (containerWidth - displayWidth) / 2f
    val top = (containerHeight - displayHeight) / 2f

    return androidx.compose.ui.geometry.Rect(
        left,
        top,
        left + displayWidth,
        top + displayHeight
    )
}

private data class SubtitleCue(
    val startMs: Long,
    val endMs: Long,
    val text: String
)

private fun parseSubtitleTime(value: String): Long {
    val normalized = value.trim().replace(',', '.')
    val parts = normalized.split(":")
    return when (parts.size) {
        3 -> {
            val hours = parts[0].toLongOrNull() ?: 0L
            val minutes = parts[1].toLongOrNull() ?: 0L
            val seconds = parts[2].toDoubleOrNull() ?: 0.0
            ((hours * 3600 + minutes * 60) * 1000 + seconds * 1000).toLong()
        }
        2 -> {
            val minutes = parts[0].toLongOrNull() ?: 0L
            val seconds = parts[1].toDoubleOrNull() ?: 0.0
            (minutes * 60 * 1000 + seconds * 1000).toLong()
        }
        else -> 0L
    }
}

private fun parseSubtitleFile(file: File): List<SubtitleCue> {
    if (!file.exists() || !file.isFile) return emptyList()

    val lines = file.readLines(Charsets.UTF_8)
    val cues = mutableListOf<SubtitleCue>()
    var index = 0

    while (index < lines.size) {
        val line = lines[index].trim()

        if (line.contains("-->")) {
            val parts = line.split("-->")
            if (parts.size >= 2) {
                val startMs = parseSubtitleTime(parts[0])
                val endMs = parseSubtitleTime(parts[1].trim().split(" ").first())

                val textLines = mutableListOf<String>()
                index++

                while (index < lines.size && lines[index].isNotBlank()) {
                    textLines.add(lines[index].trim())
                    index++
                }

                if (textLines.isNotEmpty()) {
                    cues.add(
                        SubtitleCue(
                            startMs = startMs,
                            endMs = endMs,
                            text = textLines.joinToString("\n")
                        )
                    )
                }
            }
        }

        index++
    }

    return cues
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoPlayerDialog(
    file: File,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val videoPositionPrefs = remember {
        context.getSharedPreferences("fff_video_positions", android.content.Context.MODE_PRIVATE)
    }
    val videoPositionKey = remember(file.absolutePath) {
        "position_" + file.absolutePath
    }

    var isBuffering by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }
    var videoView by remember { mutableStateOf<VideoView?>(null) }
    var videoWidth by remember { mutableStateOf(0) }
    var videoHeight by remember { mutableStateOf(0) }
    var videoContainerWidth by remember { mutableStateOf(0) }
    var videoContainerHeight by remember { mutableStateOf(0) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var isMuted by remember { mutableStateOf(false) }
    var playbackSpeed by remember { mutableStateOf(1.0f) }
    var speedMenuExpanded by remember { mutableStateOf(false) }
    var subtitleMenuExpanded by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }
    var doubleTapIconVisible by remember { mutableStateOf(false) }
    var doubleTapIconPlaying by remember { mutableStateOf(false) }
    var doubleTapSeekDirection by remember { mutableStateOf(0) }
    var currentPosition by remember { mutableStateOf(0) }
    var videoDuration by remember { mutableStateOf(0) }
    var isSeeking by remember { mutableStateOf(false) }
    var seekTargetPosition by remember { mutableStateOf<Int?>(null) }
    var controlsVisible by remember { mutableStateOf(true) }
    var isLandscape by remember { mutableStateOf(false) }
    var subtitleFiles by remember { mutableStateOf<List<File>>(emptyList()) }
    var selectedSubtitleFile by remember { mutableStateOf<File?>(null) }
    var subtitleCues by remember { mutableStateOf<List<SubtitleCue>>(emptyList()) }
    var currentSubtitleText by remember { mutableStateOf("") }
var subtitleFontFamily by remember { mutableStateOf<androidx.compose.ui.text.font.FontFamily>(androidx.compose.ui.text.font.FontFamily.Default) }
var subtitleFontName by remember { mutableStateOf("Default") }
var subtitleFontMenuExpanded by remember { mutableStateOf(false) }
var subtitleFontSizeMenuExpanded by remember { mutableStateOf(false) }
var subtitleColor by remember { mutableStateOf(Color.White) }
var subtitleColorName by remember { mutableStateOf("White") }
var subtitleColorMenuExpanded by remember { mutableStateOf(false) }
var subtitleShadowRadius by remember { mutableStateOf(0f) }
var subtitleShadowName by remember { mutableStateOf("None") }
var subtitleFontSize by remember { mutableStateOf(24f) }
var subtitleFontSizeName by remember { mutableStateOf("Medium") }
var subtitlePositionY by remember { mutableStateOf(0f) }
var subtitleShadowMenuExpanded by remember { mutableStateOf(false) }
var subtitlePositiveDelay by remember { mutableStateOf(0L) }
var subtitleNegativeDelay by remember { mutableStateOf(0L) }

    val subtitleSettingsPrefs = remember {
        context.getSharedPreferences(
            "fff_subtitle_settings",
            android.content.Context.MODE_PRIVATE
        )
    }

    LaunchedEffect(Unit) {
        subtitlePositionY = subtitleSettingsPrefs.getFloat(
            "subtitle_position_y",
            0f
        )

        subtitleFontName = subtitleSettingsPrefs.getString(
            "font_name",
            "Default"
        ) ?: "Default"

        subtitleFontFamily = when (subtitleFontName) {
            "Sans Serif" -> androidx.compose.ui.text.font.FontFamily.SansSerif
            "Serif" -> androidx.compose.ui.text.font.FontFamily.Serif
            "Monospace" -> androidx.compose.ui.text.font.FontFamily.Monospace
            "Persian" -> PersianSubtitleFont
            else -> androidx.compose.ui.text.font.FontFamily.Default
        }

        subtitleFontSizeName = subtitleSettingsPrefs.getString(
            "font_size_name",
            "Medium"
        ) ?: "Medium"

        subtitleFontSize = when (subtitleFontSizeName) {
            "Small" -> 18f
            "Large" -> 30f
            "Extra Large" -> 36f
            else -> 24f
        }

        subtitleColorName = subtitleSettingsPrefs.getString(
            "color_name",
            "White"
        ) ?: "White"

        subtitleColor = when (subtitleColorName) {
            "Yellow" -> Color.Yellow
            "Cyan" -> Color.Cyan
            "Green" -> Color.Green
            "Red" -> Color.Red
            else -> Color.White
        }

        subtitleShadowName = subtitleSettingsPrefs.getString(
            "shadow_name",
            "None"
        ) ?: "None"

        subtitleShadowRadius = when (subtitleShadowName) {
            "Small" -> 2f
            "Medium" -> 4f
            "Strong" -> 7f
            else -> 0f
        }
    }

    LaunchedEffect(subtitlePositionY) {
        subtitleSettingsPrefs.edit()
            .putFloat("subtitle_position_y", subtitlePositionY)
            .apply()
    }

    LaunchedEffect(file) {
        val parent = file.parentFile
        val baseName = file.nameWithoutExtension

        subtitleFiles = parent?.listFiles()
            ?.filter { subtitle ->
                subtitle.isFile &&
                    (subtitle.extension.equals("srt", true) ||
                     subtitle.extension.equals("vtt", true)) &&
                    subtitle.nameWithoutExtension.equals(baseName, true)
            }
            ?.sortedBy { it.name.lowercase() }
            ?: emptyList()
        
        selectedSubtitleFile = subtitleFiles.firstOrNull()
    }

    LaunchedEffect(selectedSubtitleFile) {
        subtitleCues = selectedSubtitleFile?.let { parseSubtitleFile(it) } ?: emptyList()
        currentSubtitleText = ""
    }

    fun saveVideoPosition() {
        videoView?.let { view ->
            if (view.duration > 0 && view.currentPosition > 0) {
                videoPositionPrefs.edit()
                    .putInt(videoPositionKey, view.currentPosition)
                    .apply()
            }
        }
    }

    LaunchedEffect(videoView) {
        while (true) {
            videoView?.let { view ->
                currentPosition = view.currentPosition

                val subtitleDelayMs = subtitlePositiveDelay - subtitleNegativeDelay
                val adjustedSubtitlePosition = currentPosition.toLong() - subtitleDelayMs

                currentSubtitleText = subtitleCues
                    .firstOrNull { cue ->
                        adjustedSubtitlePosition >= cue.startMs &&
                            adjustedSubtitlePosition <= cue.endMs
                    }
                    ?.text
                    ?: ""

                if (view.duration > 0) {
                    videoDuration = view.duration
                }
                isPlaying = view.isPlaying
            }
            kotlinx.coroutines.delay(250)
        }
    }

    LaunchedEffect(playbackSpeed) {
        mediaPlayer?.let { mp ->
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                mp.playbackParams = mp.playbackParams.setSpeed(playbackSpeed)
            }
        }
    }
    var gestureStartY by remember { mutableStateOf(0f) }
    var gestureStartValue by remember { mutableStateOf(0f) }
    var gestureIndicatorSide by remember { mutableStateOf(0) }
    var gestureIndicatorLevel by remember { mutableStateOf(0f) }

    Dialog(
        onDismissRequest = {
            saveVideoPosition()
            onClose()
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        val dialogView = LocalView.current

        DisposableEffect(dialogView, isLandscape) {
            val dialogWindow =
                (dialogView.parent as? DialogWindowProvider)?.window

            dialogView.post {
                dialogWindow?.let { window ->
                    WindowCompat.setDecorFitsSystemWindows(window, false)
                    window.addFlags(
                        android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                    )
                    window.addFlags(
                        android.view.WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                    )
                    window.setGravity(android.view.Gravity.CENTER)
                      window.statusBarColor = android.graphics.Color.TRANSPARENT
                    window.decorView.setBackgroundColor(android.graphics.Color.BLACK)
                        window.setBackgroundDrawable(
                            android.graphics.drawable.ColorDrawable(android.graphics.Color.BLACK)
                        )
                      window.navigationBarColor = android.graphics.Color.TRANSPARENT
                      WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false
                      WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightNavigationBars = false
                    window.setLayout(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    window.attributes = window.attributes.apply {
                        height = android.view.ViewGroup.LayoutParams.MATCH_PARENT
                        y = 0
                    }
                }
            }

            onDispose { }
        }

        LaunchedEffect(controlsVisible) {
            val activityWindow = (context as? Activity)?.window
            activityWindow?.let { window ->
                WindowCompat.getInsetsController(
                    window,
                    window.decorView
                ).apply {
                    if (controlsVisible) {
                        show(androidx.core.view.WindowInsetsCompat.Type.statusBars())
                    } else {
                        hide(androidx.core.view.WindowInsetsCompat.Type.statusBars())
                    }
                }
            }
        }

        DisposableEffect(Unit) {
            val activityWindow = (context as? Activity)?.window
            val previousStatusBarColor =
                activityWindow?.statusBarColor

            activityWindow?.statusBarColor =
                android.graphics.Color.BLACK

            activityWindow?.let { window ->
                WindowCompat.getInsetsController(
                    window,
                    window.decorView
                ).isAppearanceLightStatusBars = false
            }

            onDispose {
                activityWindow?.statusBarColor =
                    previousStatusBarColor
                        ?: android.graphics.Color.BLACK

                (context as? Activity)?.requestedOrientation =
                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxSize()
                
                .testTag("video_player_dialog"),
            color = Color.Black
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (controlsVisible) {
                    TopAppBar(
                        modifier = Modifier.zIndex(10f),
                    title = {
                        Text(
                            text = file.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                saveVideoPosition()
                                onClose()
                            },
                            modifier = Modifier.testTag("video_close_button")
                        ) {
                            Icon(
                                LucideIcons.X,
                                contentDescription = "Close Video",
                                tint = Color.White
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { FileUtils.shareFile(context, file) },
                            modifier = Modifier.testTag("video_share_button")
                        ) {
                            Icon(
                                LucideIcons.Share2,
                                contentDescription = "Share Video",
                                tint = Color.White
                            )
                        }
                        IconButton(
                            onClick = {
                                mediaPlayer?.let { mp ->
                                    isMuted = !isMuted
                                    if (isMuted) {
                                        mp.setVolume(0f, 0f)
                                    } else {
                                        mp.setVolume(1f, 1f)
                                    }
                                }
                            },
                            modifier = Modifier.testTag("video_mute_button")
                        ) {
                            Icon(
                                painter = painterResource(
                                    id = if (isMuted) {
                                        com.example.R.drawable.volume_2
                                    } else {
                                        com.example.R.drawable.volume_x
                                    }
                                ),
                                contentDescription = if (isMuted) {
                                    "Unmute Video"
                                } else {
                                    "Mute Video"
                                },
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                val activity = context as? Activity
                                activity?.let {
                                    isLandscape = !isLandscape
                                    it.requestedOrientation =
                                        if (isLandscape) {
                                            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                                        } else {
                                            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                                        }
                                }
                            },
                            modifier = Modifier.testTag("video_rotate_button")
                        ) {
                            Icon(
    painter = painterResource(id = com.example.R.drawable.rotate_3d),
    contentDescription = "Rotate Video",
    tint = Color.White,
    modifier = Modifier.size(24.dp)
)
                        }

                        Box(
                            modifier = Modifier.wrapContentSize(Alignment.TopEnd)
                        ) {
                            IconButton(
                                onClick = { subtitleMenuExpanded = !subtitleMenuExpanded },
                                modifier = Modifier.testTag("video_subtitle_button")
                            ) {
                                Text(
                                    text = "CC",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            DropdownMenu(
                                expanded = subtitleMenuExpanded,
                                onDismissRequest = {
                                    subtitleMenuExpanded = false
                                    subtitleFontMenuExpanded = false
                                    subtitleFontSizeMenuExpanded = false
                                    subtitleColorMenuExpanded = false
                                    subtitleShadowMenuExpanded = false
                                },
                                containerColor = Color(0xFF131B2E),
                                tonalElevation = 0.dp
                            ) {
                                DropdownMenuItem(
                                        colors = androidx.compose.material3.MenuDefaults.itemColors(
                                            textColor = Color.White
                                        ),
                                    text = {
                                        Text(
                                            if (selectedSubtitleFile != null) "Off" else "On",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    },
                                    onClick = {
                                        selectedSubtitleFile =
                                            if (selectedSubtitleFile != null) {
                                                null
                                            } else {
                                                subtitleFiles.firstOrNull()
                                            }
                                        subtitleFontSizeMenuExpanded = false
                                        subtitleMenuExpanded = false
                                    }
                                )

                                HorizontalDivider(color = Color(0xFF334155))

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {

                                    // Positive delay
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton(
                                            onClick = {
                                                subtitlePositiveDelay += 100L
                                                if (subtitlePositiveDelay > 0L) {
                                                    subtitleNegativeDelay = 0L
                                                }
                                            }
                                        ) {
                                            Text(
                                                "+",
                                                color = Color.Yellow,
                                                fontSize = 22.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        OutlinedTextField(
                                            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White,
                                                focusedContainerColor = Color(0xFF131B2E),
                                                unfocusedContainerColor = Color(0xFF131B2E),
                                                focusedBorderColor = Color(0xFF334155),
                                                unfocusedBorderColor = Color(0xFF334155),
                                                focusedLabelColor = Color.White,
                                                unfocusedLabelColor = Color.LightGray,
                                                focusedPrefixColor = Color.White,
                                                unfocusedPrefixColor = Color.White,
                                                focusedPlaceholderColor = Color.LightGray,
                                                unfocusedPlaceholderColor = Color.LightGray
                                            ),
                                                                                        value = if (subtitlePositiveDelay == 0L) "" else "${subtitlePositiveDelay}",
                                            onValueChange = { value ->
                                                val number = value.toLongOrNull()
                                                if (number != null || value.isEmpty()) {
                                                    subtitlePositiveDelay = number ?: 0L
                                                    if (subtitlePositiveDelay > 0L) {
                                                        subtitleNegativeDelay = 0L
                                                    }
                                                }
                                            },
                                            label = { Text("Positive Delay (ms)", fontSize = 11.sp, maxLines = 1) },
                                            prefix = { Text("+") },
                                            placeholder = { Text("0") },
                                            singleLine = true,
            textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                                            keyboardOptions = KeyboardOptions(
                                                keyboardType = KeyboardType.Number
                                            ),
                                            modifier = Modifier.width(120.dp)
                                        )

                                        IconButton(
                                            onClick = {
                                                subtitlePositiveDelay =
                                                    (subtitlePositiveDelay - 100L).coerceAtLeast(0L)
                                            }
                                        ) {
                                            Text(
                                                "−",
                                                color = Color.Yellow,
                                                fontSize = 22.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Negative delay
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton(
                                            onClick = {
                                                subtitleNegativeDelay += 100L
                                                if (subtitleNegativeDelay > 0L) {
                                                    subtitlePositiveDelay = 0L
                                                }
                                            }
                                        ) {
                                            Text(
                                                "+",
                                                color = Color.Yellow,
                                                fontSize = 22.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        OutlinedTextField(
                                            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White,
                                                focusedContainerColor = Color(0xFF131B2E),
                                                unfocusedContainerColor = Color(0xFF131B2E),
                                                focusedBorderColor = Color(0xFF334155),
                                                unfocusedBorderColor = Color(0xFF334155),
                                                focusedLabelColor = Color.White,
                                                unfocusedLabelColor = Color.LightGray,
                                                focusedPrefixColor = Color.White,
                                                unfocusedPrefixColor = Color.White,
                                                focusedPlaceholderColor = Color.LightGray,
                                                unfocusedPlaceholderColor = Color.LightGray
                                            ),
                                                                                        value = if (subtitleNegativeDelay == 0L) "" else "${subtitleNegativeDelay}",
                                            onValueChange = { value ->
                                                val number = value.toLongOrNull()
                                                if (number != null || value.isEmpty()) {
                                                    subtitleNegativeDelay = number ?: 0L
                                                    if (subtitleNegativeDelay > 0L) {
                                                        subtitlePositiveDelay = 0L
                                                    }
                                                }
                                            },
                                            label = { Text("Negative Delay (ms)", fontSize = 11.sp, maxLines = 1) },
                                            prefix = { Text("−") },
                                            placeholder = { Text("0") },
                                            singleLine = true,
            textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                                            keyboardOptions = KeyboardOptions(
                                                keyboardType = KeyboardType.Number
                                            ),
                                            modifier = Modifier.width(120.dp)
                                        )

                                        IconButton(
                                            onClick = {
                                                subtitleNegativeDelay =
                                                    (subtitleNegativeDelay - 100L).coerceAtLeast(0L)
                                            }
                                        ) {
                                            Text(
                                                "−",
                                                color = Color.Yellow,
                                                fontSize = 22.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                HorizontalDivider(color = Color(0xFF334155))
                                
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        "Subtitle Position",
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            "DOWN",
                                            color = Color.White,
                                            fontSize = 12.sp
                                        )

                                        Slider(
                                            value = subtitlePositionY,
                                            onValueChange = {
                                                subtitlePositionY = it
                                            },
                                            valueRange = -200f..200f,
                                            modifier = Modifier.width(120.dp)
                                        )

                                        Text(
                                            "UP",
                                            color = Color.White,
                                            fontSize = 12.sp
                                        )
                                    }
                                }

                                HorizontalDivider(color = Color(0xFF334155))

                                DropdownMenuItem(
                                        colors = androidx.compose.material3.MenuDefaults.itemColors(
                                            textColor = Color.White
                                        ),
                                    modifier = Modifier.background(
                                        if (subtitleFontMenuExpanded)
                                            Color.Gray.copy(alpha = 0.50f)
                                        else
                                            Color.Transparent
                                    ),
                                    text = {
                                        Text(
                                            "Font",
                                            fontWeight = FontWeight.Bold
                                        )
                                    },
                                    onClick = {
                                        subtitleFontMenuExpanded = !subtitleFontMenuExpanded
                                        subtitleFontSizeMenuExpanded = false
                                        subtitleColorMenuExpanded = false
                                        subtitleShadowMenuExpanded = false
                                    }
                                )

                                if (subtitleFontMenuExpanded) {
                                    DropdownMenuItem(
                                        colors = androidx.compose.material3.MenuDefaults.itemColors(
                                            textColor = Color.White
                                        ),
                                        modifier = Modifier.background(
                                            if (subtitleFontMenuExpanded)
                                                Color.Gray.copy(alpha = 0.50f)
                                            else
                                                Color.Transparent
                                        ),
                                        text = { Text("Default") },
                                        onClick = {
                                            subtitleFontFamily =
                                                androidx.compose.ui.text.font.FontFamily.Default
                                            subtitleFontName = "Default"
                                            subtitleSettingsPrefs.edit()
                                                .putString("font_name", "Default")
                                                .apply()
                                            subtitleFontMenuExpanded = false
                                        }
                                    )

                                    DropdownMenuItem(
                                        colors = androidx.compose.material3.MenuDefaults.itemColors(
                                            textColor = Color.White
                                        ),
                                        modifier = Modifier.background(
                                            if (subtitleFontMenuExpanded)
                                                Color.Gray.copy(alpha = 0.50f)
                                            else
                                                Color.Transparent
                                        ),
                                        text = { Text("Sans Serif") },
                                        onClick = {
                                            subtitleFontFamily =
                                                androidx.compose.ui.text.font.FontFamily.SansSerif
                                            subtitleFontName = "Sans Serif"
                                            subtitleSettingsPrefs.edit()
                                                .putString("font_name", "Sans Serif")
                                                .apply()
                                            subtitleFontMenuExpanded = false
                                        }
                                    )

                                    DropdownMenuItem(
                                        colors = androidx.compose.material3.MenuDefaults.itemColors(
                                            textColor = Color.White
                                        ),
                                        modifier = Modifier.background(
                                            if (subtitleFontMenuExpanded)
                                                Color.Gray.copy(alpha = 0.50f)
                                            else
                                                Color.Transparent
                                        ),
                                        text = { Text("Serif") },
                                        onClick = {
                                            subtitleFontFamily =
                                                androidx.compose.ui.text.font.FontFamily.Serif
                                            subtitleFontName = "Serif"
                                            subtitleSettingsPrefs.edit()
                                                .putString("font_name", "Serif")
                                                .apply()
                                            subtitleFontMenuExpanded = false
                                        }
                                    )

                                    DropdownMenuItem(
                                        colors = androidx.compose.material3.MenuDefaults.itemColors(
                                            textColor = Color.White
                                        ),
                                        modifier = Modifier.background(
                                            if (subtitleFontMenuExpanded)
                                                Color.Gray.copy(alpha = 0.50f)
                                            else
                                                Color.Transparent
                                        ),
                                        text = { Text("Monospace") },
                                        onClick = {
                                            subtitleFontFamily =
                                                androidx.compose.ui.text.font.FontFamily.Monospace
                                            subtitleFontName = "Monospace"
                                            subtitleSettingsPrefs.edit()
                                                .putString("font_name", "Monospace")
                                                .apply()
                                            subtitleFontMenuExpanded = false
                                        }
                                    )

                                    DropdownMenuItem(
                                        colors = androidx.compose.material3.MenuDefaults.itemColors(
                                            textColor = Color.White
                                        ),
                                        modifier = Modifier.background(
                                            if (subtitleFontMenuExpanded)
                                                Color.Gray.copy(alpha = 0.50f)
                                            else
                                                Color.Transparent
                                        ),
                                        text = { Text("Persian") },
                                        onClick = {
                                            subtitleFontFamily = PersianSubtitleFont
                                            subtitleFontName = "Persian"
                                            subtitleSettingsPrefs.edit()
                                                .putString("font_name", "Persian")
                                                .apply()
                                            subtitleFontMenuExpanded = false
                                        }
                                    )
                                }

                                HorizontalDivider(color = Color(0xFF334155))

                                DropdownMenuItem(
                                        colors = androidx.compose.material3.MenuDefaults.itemColors(
                                            textColor = Color.White
                                        ),
                                    modifier = Modifier.background(
                                        if (subtitleFontSizeMenuExpanded)
                                            Color.Gray.copy(alpha = 0.50f)
                                        else
                                            Color.Transparent
                                    ),
                                    text = {
                                        Text(
                                            "Font Size",
                                            fontWeight = FontWeight.Bold
                                        )
                                    },
                                    onClick = {
                                        subtitleFontSizeMenuExpanded = !subtitleFontSizeMenuExpanded
                                        subtitleFontMenuExpanded = false
                                        subtitleColorMenuExpanded = false
                                        subtitleShadowMenuExpanded = false
                                    }
                                )

                                if (subtitleFontSizeMenuExpanded) {
                                    DropdownMenuItem(
                                        colors = androidx.compose.material3.MenuDefaults.itemColors(
                                            textColor = Color.White
                                        ),
                                        modifier = Modifier.background(
                                            if (subtitleFontSizeMenuExpanded)
                                                Color.Gray.copy(alpha = 0.50f)
                                            else
                                                Color.Transparent
                                        ),
                                        text = { Text("Small") },
                                        onClick = {
                                            subtitleFontSize = 18f
                                            subtitleFontSizeName = "Small"
                                            subtitleSettingsPrefs.edit()
                                                .putString("font_size_name", "Small")
                                                .apply()
                                            subtitleFontSizeMenuExpanded = false
                                        }
                                    )

                                    DropdownMenuItem(
                                        colors = androidx.compose.material3.MenuDefaults.itemColors(
                                            textColor = Color.White
                                        ),
                                        modifier = Modifier.background(
                                            if (subtitleFontSizeMenuExpanded)
                                                Color.Gray.copy(alpha = 0.50f)
                                            else
                                                Color.Transparent
                                        ),
                                        text = { Text("Medium") },
                                        onClick = {
                                            subtitleFontSize = 24f
                                            subtitleFontSizeName = "Medium"
                                            subtitleSettingsPrefs.edit()
                                                .putString("font_size_name", "Medium")
                                                .apply()
                                            subtitleFontSizeMenuExpanded = false
                                        }
                                    )

                                    DropdownMenuItem(
                                        colors = androidx.compose.material3.MenuDefaults.itemColors(
                                            textColor = Color.White
                                        ),
                                        modifier = Modifier.background(
                                            if (subtitleFontSizeMenuExpanded)
                                                Color.Gray.copy(alpha = 0.50f)
                                            else
                                                Color.Transparent
                                        ),
                                        text = { Text("Large") },
                                        onClick = {
                                            subtitleFontSize = 30f
                                            subtitleFontSizeName = "Large"
                                            subtitleSettingsPrefs.edit()
                                                .putString("font_size_name", "Large")
                                                .apply()
                                            subtitleFontSizeMenuExpanded = false
                                        }
                                    )

                                    DropdownMenuItem(
                                        colors = androidx.compose.material3.MenuDefaults.itemColors(
                                            textColor = Color.White
                                        ),
                                        modifier = Modifier.background(
                                            if (subtitleFontSizeMenuExpanded)
                                                Color.Gray.copy(alpha = 0.50f)
                                            else
                                                Color.Transparent
                                        ),
                                        text = { Text("Extra Large") },
                                        onClick = {
                                            subtitleFontSize = 36f
                                            subtitleFontSizeName = "Extra Large"
                                            subtitleSettingsPrefs.edit()
                                                .putString("font_size_name", "Extra Large")
                                                .apply()
                                            subtitleFontSizeMenuExpanded = false
                                        }
                                    )
                                }

                                HorizontalDivider(color = Color(0xFF334155))

                                DropdownMenuItem(
                                        colors = androidx.compose.material3.MenuDefaults.itemColors(
                                            textColor = Color.White
                                        ),
                                    modifier = Modifier.background(
                                        if (subtitleColorMenuExpanded)
                                            Color.Gray.copy(alpha = 0.50f)
                                        else
                                            Color.Transparent
                                    ),
                                    text = {
                                        Text(
                                            "Color",
                                            fontWeight = FontWeight.Bold
                                        )
                                    },
                                    onClick = {
                                        subtitleColorMenuExpanded = !subtitleColorMenuExpanded
                                        subtitleFontMenuExpanded = false
                                        subtitleFontSizeMenuExpanded = false
                                        subtitleShadowMenuExpanded = false
                                    }
                                )

                                if (subtitleColorMenuExpanded) {
                                    DropdownMenuItem(
                                        colors = androidx.compose.material3.MenuDefaults.itemColors(
                                            textColor = Color.White
                                        ),
                                        modifier = Modifier.background(
                                            if (subtitleColorMenuExpanded)
                                                Color.Gray.copy(alpha = 0.50f)
                                            else
                                                Color.Transparent
                                        ),
                                        text = { Text("White") },
                                        onClick = {
                                            subtitleColor = Color.White
                                            subtitleColorName = "White"
                                            subtitleSettingsPrefs.edit()
                                                .putString("color_name", "White")
                                                .apply()
                                            subtitleColorMenuExpanded = false
                                        }
                                    )

                                    DropdownMenuItem(
                                        colors = androidx.compose.material3.MenuDefaults.itemColors(
                                            textColor = Color.White
                                        ),
                                        modifier = Modifier.background(
                                            if (subtitleColorMenuExpanded)
                                                Color.Gray.copy(alpha = 0.50f)
                                            else
                                                Color.Transparent
                                        ),
                                        text = { Text("Yellow") },
                                        onClick = {
                                            subtitleColor = Color.Yellow
                                            subtitleColorName = "Yellow"
                                            subtitleSettingsPrefs.edit()
                                                .putString("color_name", "Yellow")
                                                .apply()
                                            subtitleColorMenuExpanded = false
                                        }
                                    )

                                    DropdownMenuItem(
                                        colors = androidx.compose.material3.MenuDefaults.itemColors(
                                            textColor = Color.White
                                        ),
                                        modifier = Modifier.background(
                                            if (subtitleColorMenuExpanded)
                                                Color.Gray.copy(alpha = 0.50f)
                                            else
                                                Color.Transparent
                                        ),
                                        text = { Text("Cyan") },
                                        onClick = {
                                            subtitleColor = Color.Cyan
                                            subtitleColorName = "Cyan"
                                            subtitleSettingsPrefs.edit()
                                                .putString("color_name", "Cyan")
                                                .apply()
                                            subtitleColorMenuExpanded = false
                                        }
                                    )

                                    DropdownMenuItem(
                                        colors = androidx.compose.material3.MenuDefaults.itemColors(
                                            textColor = Color.White
                                        ),
                                        modifier = Modifier.background(
                                            if (subtitleColorMenuExpanded)
                                                Color.Gray.copy(alpha = 0.50f)
                                            else
                                                Color.Transparent
                                        ),
                                        text = { Text("Green") },
                                        onClick = {
                                            subtitleColor = Color.Green
                                            subtitleColorName = "Green"
                                            subtitleSettingsPrefs.edit()
                                                .putString("color_name", "Green")
                                                .apply()
                                            subtitleColorMenuExpanded = false
                                        }
                                    )

                                    DropdownMenuItem(
                                        colors = androidx.compose.material3.MenuDefaults.itemColors(
                                            textColor = Color.White
                                        ),
                                        modifier = Modifier.background(
                                            if (subtitleColorMenuExpanded)
                                                Color.Gray.copy(alpha = 0.50f)
                                            else
                                                Color.Transparent
                                        ),
                                        text = { Text("Red") },
                                        onClick = {
                                            subtitleColor = Color.Red
                                            subtitleColorName = "Red"
                                            subtitleSettingsPrefs.edit()
                                                .putString("color_name", "Red")
                                                .apply()
                                            subtitleColorMenuExpanded = false
                                        }
                                    )
                                }

                                HorizontalDivider(color = Color(0xFF334155))

                                DropdownMenuItem(
                                        colors = androidx.compose.material3.MenuDefaults.itemColors(
                                            textColor = Color.White
                                        ),
                                    modifier = Modifier.background(
                                        if (subtitleShadowMenuExpanded)
                                            Color.Gray.copy(alpha = 0.50f)
                                        else
                                            Color.Transparent
                                    ),
                                    text = {
                                        Text(
                                            "Shadow",
                                            fontWeight = FontWeight.Bold
                                        )
                                    },
                                    onClick = {
                                        subtitleShadowMenuExpanded = !subtitleShadowMenuExpanded
                                        subtitleFontMenuExpanded = false
                                        subtitleFontSizeMenuExpanded = false
                                        subtitleColorMenuExpanded = false
                                    }
                                )

                                if (subtitleShadowMenuExpanded) {
                                    DropdownMenuItem(
                                        colors = androidx.compose.material3.MenuDefaults.itemColors(
                                            textColor = Color.White
                                        ),
                                        modifier = Modifier.background(
                                            if (subtitleShadowMenuExpanded)
                                                Color.Gray.copy(alpha = 0.50f)
                                            else
                                                Color.Transparent
                                        ),
                                        text = { Text("None") },
                                        onClick = {
                                            subtitleShadowRadius = 0f
                                            subtitleShadowName = "None"
                                            subtitleSettingsPrefs.edit()
                                                .putString("shadow_name", "None")
                                                .apply()
                                            subtitleShadowMenuExpanded = false
                                        }
                                    )

                                    DropdownMenuItem(
                                        colors = androidx.compose.material3.MenuDefaults.itemColors(
                                            textColor = Color.White
                                        ),
                                        modifier = Modifier.background(
                                            if (subtitleShadowMenuExpanded)
                                                Color.Gray.copy(alpha = 0.50f)
                                            else
                                                Color.Transparent
                                        ),
                                        text = { Text("Small") },
                                        onClick = {
                                            subtitleShadowRadius = 2f
                                            subtitleShadowName = "Small"
                                            subtitleSettingsPrefs.edit()
                                                .putString("shadow_name", "Small")
                                                .apply()
                                            subtitleShadowMenuExpanded = false
                                        }
                                    )

                                    DropdownMenuItem(
                                        colors = androidx.compose.material3.MenuDefaults.itemColors(
                                            textColor = Color.White
                                        ),
                                        modifier = Modifier.background(
                                            if (subtitleShadowMenuExpanded)
                                                Color.Gray.copy(alpha = 0.50f)
                                            else
                                                Color.Transparent
                                        ),
                                        text = { Text("Medium") },
                                        onClick = {
                                            subtitleShadowRadius = 4f
                                            subtitleShadowName = "Medium"
                                            subtitleSettingsPrefs.edit()
                                                .putString("shadow_name", "Medium")
                                                .apply()
                                            subtitleShadowMenuExpanded = false
                                        }
                                    )

                                    DropdownMenuItem(
                                        colors = androidx.compose.material3.MenuDefaults.itemColors(
                                            textColor = Color.White
                                        ),
                                        modifier = Modifier.background(
                                            if (subtitleShadowMenuExpanded)
                                                Color.Gray.copy(alpha = 0.50f)
                                            else
                                                Color.Transparent
                                        ),
                                        text = { Text("Strong") },
                                        onClick = {
                                            subtitleShadowRadius = 7f
                                            subtitleShadowName = "Strong"
                                            subtitleSettingsPrefs.edit()
                                                .putString("shadow_name", "Strong")
                                                .apply()
                                            subtitleShadowMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Box(
                            modifier = Modifier.wrapContentSize(Alignment.TopEnd)
                        ) {
                            IconButton(
                                onClick = { speedMenuExpanded = !speedMenuExpanded },
                                modifier = Modifier.testTag("video_speed_button")
                            ) {
                                Text(
                                    text = "${playbackSpeed}x",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            DropdownMenu(
                                expanded = speedMenuExpanded,
                                onDismissRequest = { speedMenuExpanded = false },
                                containerColor = Color(0xFF131B2E),
                                tonalElevation = 0.dp
                            ) {
                                listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { speed ->
                                    DropdownMenuItem(
                                        text = {
                                            Text("${speed}x", color = Color.White)
                                        },
                                        onClick = {
                                            playbackSpeed = speed
                                            speedMenuExpanded = false
                                        },
                                        colors = androidx.compose.material3.MenuDefaults.itemColors(
                                            textColor = Color.White
                                        )
                                    )
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .zIndex(-1f)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onTap = { controlsVisible = !controlsVisible },
                                onDoubleTap = { offset ->
                                    videoView?.let { view ->
                                        if (offset.x < size.width / 3f) {
                                            doubleTapSeekDirection = -1
                                            doubleTapIconVisible = true
                                            view.seekTo(
                                                (view.currentPosition - 15_000)
                                                    .coerceAtLeast(0)
                                            )
                                        } else if (offset.x < size.width * 2f / 3f) {
                                            doubleTapSeekDirection = 0
                                            if (view.isPlaying) {
                                                view.pause()
                                                isPlaying = false
                                                doubleTapIconPlaying = false
                                            } else {
                                                view.start()
                                                isPlaying = true
                                                doubleTapIconPlaying = true
                                            }
                                            doubleTapIconVisible = true
                                        } else {
                                            doubleTapSeekDirection = 1
                                            doubleTapIconVisible = true
                                            val duration = view.duration
                                            val target = view.currentPosition + 15_000
                                            view.seekTo(
                                                if (duration > 0) {
                                                    target.coerceAtMost(duration)
                                                } else {
                                                    target
                                                }
                                            )
                                        }
                                    }
                                }
                            )
                        }

                        .pointerInput(Unit) {
                            detectHorizontalDragGestures(
                                onHorizontalDrag = { change, dragAmount ->
                                    videoView?.let { view ->
                                        val duration = view.duration
                                        if (duration > 0) {
                                            val seekAmount = (dragAmount * 100).toInt()
                                            val target = (view.currentPosition + seekAmount)
                                                .coerceIn(0, duration)
                                            view.seekTo(target)
                                            currentPosition = target
                                            isSeeking = true
                                            seekTargetPosition = target
                                        }
                                    }
                                    change.consume()
                                }
                            )
                        }
                        .pointerInput(Unit) {
                            detectVerticalDragGestures(
                                  onDragStart = { offset ->
                                      gestureStartY = offset.y
                                      gestureIndicatorSide = if (offset.x < size.width / 2f) 1 else 2
                                      gestureStartValue = if (offset.x < size.width / 2f) {
                                          val activity = context as? Activity
                                          activity?.window?.attributes?.screenBrightness
                                              ?.takeIf { it >= 0f }
                                              ?: 0.5f
                                      } else {
                                          val audioManager =
                                              context.getSystemService(AudioManager::class.java)
                                          val maxVolume =
                                              audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                                          if (maxVolume > 0) {
                                              audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                                                  .toFloat() / maxVolume
                                          } else {
                                              0f
                                          }
                                      }
                                  },
                                    onVerticalDrag = { change, _ ->
                                        val height = size.height.toFloat()
                                        if (height > 0f) {
                                            val delta =
                                                (gestureStartY - change.position.y) / height
                                            val newLevel =
                                                (gestureStartValue + delta).coerceIn(0f, 1f)

                                            gestureIndicatorLevel = newLevel

                                            if (change.position.x < size.width / 2f) {
                                                val activity = context as? Activity
                                                activity?.window?.attributes?.let { attributes ->
                                                    attributes.screenBrightness =
                                                        newLevel.coerceAtLeast(0.05f)
                                                    activity.window.attributes = attributes
                                                }
                                            } else {
                                                val audioManager =
                                                    context.getSystemService(AudioManager::class.java)
                                                val maxVolume =
                                                    audioManager.getStreamMaxVolume(
                                                        AudioManager.STREAM_MUSIC
                                                    )
                                                val newVolume =
                                                    (newLevel * maxVolume)
                                                        .toInt()
                                                        .coerceIn(0, maxVolume)

                                                audioManager.setStreamVolume(
                                                    AudioManager.STREAM_MUSIC,
                                                    newVolume,
                                                    0
                                                )
                                            }

                                            change.consume()
                                        }
                                    },
                                    onDragEnd = {
                                        gestureIndicatorSide = 0
                                    },
                                    onDragCancel = {
                                        gestureIndicatorSide = 0
                                    }
                                )
                            }
                        )
                    Image(
                        painter = painterResource(id = com.example.R.drawable.aaa),
                        contentDescription = null,
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier.fillMaxSize()
                    )
                    LaunchedEffect(doubleTapIconVisible) {
                        if (doubleTapIconVisible) {
                            kotlinx.coroutines.delay(700)
                            doubleTapIconVisible = false
                        }
                    }

                    if (doubleTapIconVisible) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .zIndex(10f),
                            contentAlignment = Alignment.Center
                        ) {
                            when (doubleTapSeekDirection) {
                                -1 -> {
                                    Text(
                                        text = "−15",
                                        color = Color.White.copy(alpha = 0.5f),
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .align(Alignment.CenterStart)
                                            .padding(start = 48.dp)
                                    )
                                }

                                0 -> {
                                    Icon(
                                        imageVector = if (doubleTapIconPlaying) {
                                            LucideIcons.Play
                                        } else {
                                            LucideIcons.Pause
                                        },
                                        contentDescription = null,
                                        tint = Color.White.copy(alpha = 0.5f),
                                        modifier = Modifier.size(72.dp)
                                    )
                                }

                                1 -> {
                                    Text(
                                        text = "+15",
                                        color = Color.White.copy(alpha = 0.5f),
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .align(Alignment.CenterEnd)
                                            .padding(end = 48.dp)
                                    )
                                }
                            }
                        }
                    }

                    if (hasError) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Icon(
                                imageVector = LucideIcons.FileVideo,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Unable to play this video codec or format.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.LightGray
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .onSizeChanged { size ->
                                    videoContainerWidth = size.width
                                    videoContainerHeight = size.height
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            AndroidView(
                                factory = { ctx ->
                                    VideoView(ctx).apply {
                                        videoView = this
                                        translationX = 0f
                                        translationY = 0f

                                        val mediaController = MediaController(ctx)
                                        mediaController.setAnchorView(this)
                                        setVideoURI(Uri.fromFile(file))
                                        setOnPreparedListener { mp: MediaPlayer ->
                                            mediaPlayer = mp

                                            mp.setOnVideoSizeChangedListener { _, width, height ->
                                                videoWidth = width
                                                videoHeight = height
                                            }
                                            isMuted = false
                                            isBuffering = false
                                            mp.isLooping = false
                                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                                                mp.playbackParams = mp.playbackParams.setSpeed(playbackSpeed)
                                            }
                                            requestLayout()

                                            val savedPosition = videoPositionPrefs
                                                .getInt(videoPositionKey, 0)

                                            if (savedPosition > 0 && mp.duration > 0) {
                                                seekTo(
                                                    savedPosition.coerceAtMost(
                                                        (mp.duration - 1000).coerceAtLeast(0)
                                                    )
                                                )
                                            }

                                            start()
                                            isPlaying = true
                                        }
                                        setOnErrorListener { _, _, _ ->
                                            isBuffering = false
                                            hasError = true
                                            true
                                        }
                                    }
                                },
                                update = { view ->
                                    view.translationX = 0f
                                    view.translationY = 0f
                                },
                                modifier = Modifier.fillMaxSize()
                            )

                            if (
                                currentSubtitleText.isNotBlank() &&
                                videoWidth > 0 &&
                                videoHeight > 0 &&
                                videoContainerWidth > 0 &&
                                videoContainerHeight > 0
                            ) {
                                val videoRect = calculateVideoDisplayRect(
                                    videoWidth = videoWidth,
                                    videoHeight = videoHeight,
                                    containerWidth = videoContainerWidth,
                                    containerHeight = videoContainerHeight
                                )

                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .zIndex(5f)
                                        .offset {
                                            androidx.compose.ui.unit.IntOffset(
                                                videoRect.left.roundToInt(),
                                                videoRect.top.roundToInt()
                                            )
                                        }
                                        .width(with(androidx.compose.ui.platform.LocalDensity.current) {
                                            videoRect.width.toDp()
                                        })
                                        .height(with(androidx.compose.ui.platform.LocalDensity.current) {
                                            videoRect.height.toDp()
                                        }),
                                    contentAlignment = Alignment.BottomCenter
                                ) {
                                    Text(
                                        text = currentSubtitleText,
                                        textAlign = TextAlign.Center,
                                        color = subtitleColor,
                                        fontSize = subtitleFontSize.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = subtitleFontFamily,
                                        style = androidx.compose.ui.text.TextStyle(
                                            shadow = androidx.compose.ui.graphics.Shadow(
                                                color = Color.Black,
                                                blurRadius = subtitleShadowRadius
                                            )
                                        ),
                                        modifier = Modifier
                                            .offset(y = (-subtitlePositionY).dp)
                                            .padding(
                                                start = 24.dp,
                                                end = 24.dp,
                                                bottom = 24.dp
                                            )
                                    )
                                }
                            }

                        }


                          if (controlsVisible) {
Column(
                              modifier = Modifier
                                  .align(Alignment.BottomCenter)
                                  .fillMaxWidth()
                                  .padding(horizontal = 24.dp, vertical = 16.dp)
                          ) {
                              Row(
                                  modifier = Modifier.fillMaxWidth(),
                                  verticalAlignment = Alignment.CenterVertically
                              ) {
                                  Text(
                                      text = formatVideoTime(currentPosition),
                                      color = Color.White,
                                      style = MaterialTheme.typography.labelSmall
                                  )

                                  Slider(
                                      value = if (videoDuration > 0) {
                                          currentPosition.toFloat()
                                              .coerceIn(0f, videoDuration.toFloat())
                                      } else {
                                          0f
                                      },
                                      valueRange = 0f..videoDuration.toFloat(),
                                      onValueChange = { value ->
                                          if (videoDuration > 0) {
                                              isSeeking = true
                                              currentPosition = value.toInt().coerceIn(0, videoDuration)
                                              seekTargetPosition = currentPosition
                                              videoView?.seekTo(currentPosition)
                                          }
                                      },
                                      onValueChangeFinished = {
                                          videoView?.seekTo(currentPosition)
                                          seekTargetPosition = currentPosition
                                      },
                                      enabled = videoDuration > 0,
                                      modifier = Modifier
                                          .weight(1f)
                                          .padding(horizontal = 8.dp)
                                  )

                                  Text(
                                      text = formatVideoTime(videoDuration),
                                      color = Color.White,
                                      style = MaterialTheme.typography.labelSmall
                                  )
                              }

                              Row(
                                  modifier = Modifier
                                      .fillMaxWidth()
                                      .padding(bottom = 48.dp),
                                  horizontalArrangement = Arrangement.Center,
                                  verticalAlignment = Alignment.CenterVertically
                              ) {
                              IconButton(
                                  onClick = {
                                      videoView?.let { view ->
                                          view.seekTo(
                                              (view.currentPosition - 15_000).coerceAtLeast(0)
                                          )
                                      }
                                  }
                              ) {
                                  Text(
                                      text = "-15",
                                      color = Color.White,
                                      fontWeight = FontWeight.Bold
                                  )
                              }

                              IconButton(
                                  onClick = {
                                      videoView?.let { view ->
                                          if (view.isPlaying) {
                                              view.pause()
                                              isPlaying = false
                                              videoPositionPrefs.edit()
                                                  .putInt(videoPositionKey, view.currentPosition)
                                                  .apply()
                                          } else {
                                              view.start()
                                              isPlaying = true
                                          }
                                      }
                                  },
                                  modifier = Modifier.testTag("video_play_pause_button")
                              ) {
                                  Icon(
                                      imageVector = if (isPlaying) {
                                          LucideIcons.Pause
                                      } else {
                                          LucideIcons.Play
                                      },
                                      contentDescription = if (isPlaying) {
                                          "Pause Video"
                                      } else {
                                          "Play Video"
                                      },
                                      tint = Color.White,
                                      modifier = Modifier.size(32.dp)
                                  )
                              }

                              IconButton(
                                  onClick = {
                                      videoView?.let { view ->
                                          val duration = view.duration
                                          val target = view.currentPosition + 15_000
                                          view.seekTo(
                                              if (duration > 0) target.coerceAtMost(duration) else target
                                          )
                                      }
                                  }
                              ) {
                                  Text(
                                      text = "+15",
                                      color = Color.White,
                                      fontWeight = FontWeight.Bold
                                  )
                              }
                          }

                            }
}
                        if (gestureIndicatorSide == 1) {
                            Column(
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .padding(end = 24.dp)
                                      .background(Color.Black.copy(alpha = 0.5f))
                                      .padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Brightness",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .width(8.dp)
                                        .height(160.dp)
                                        .background(Color.White.copy(alpha = 0.25f))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .fillMaxWidth()
                                            .fillMaxHeight(gestureIndicatorLevel)
                                            .background(Color.White)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "${(gestureIndicatorLevel * 100).roundToInt()}%",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (gestureIndicatorSide == 2) {
                            Column(
                                modifier = Modifier
                                    .align(Alignment.CenterStart)
                                    .padding(start = 24.dp)
                                      .background(Color.Black.copy(alpha = 0.5f))
                                      .padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Volume",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .width(8.dp)
                                        .height(160.dp)
                                        .background(Color.White.copy(alpha = 0.25f))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .fillMaxWidth()
                                            .fillMaxHeight(gestureIndicatorLevel)
                                            .background(Color.White)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "${(gestureIndicatorLevel * 100).roundToInt()}%",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (isBuffering) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(48.dp),
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
