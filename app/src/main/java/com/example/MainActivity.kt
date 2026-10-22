package com.example

import java.io.File
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import java.io.FileOutputStream
import android.util.Log
import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.activity.viewModels
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.draw.blur
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.compose.ui.tooling.preview.Preview
import com.example.ui.FileManagerScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.util.TransferNotificationManager
import com.example.viewmodel.FileManagerViewModel

class MainActivity : ComponentActivity() {

  private val fileManagerViewModel: FileManagerViewModel by viewModels()
    private lateinit var prefs: SharedPreferences

  private var darkTheme by mutableStateOf(false)
  private var themeColor by mutableStateOf(0)
  private var customBackgroundUri by mutableStateOf<File?>(null)
  private var pendingCustomBackgroundUri by mutableStateOf<File?>(null)
  private var customBackgroundBlur by mutableStateOf(0f)
  private var customBackgroundOpacity by mutableStateOf(0f)
    private var homeBackgroundEnabled by mutableStateOf(true)
    private var homeBackgroundFile by mutableStateOf<File?>(null)
    private var homeBackgroundVersion by mutableStateOf(0)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
        prefs = getSharedPreferences("fff_settings", MODE_PRIVATE)
        darkTheme = prefs.getBoolean("dark_theme", false)
        themeColor = prefs.getInt("theme_color", 0)
        customBackgroundBlur = prefs.getFloat("custom_blur", 0f)
        customBackgroundOpacity = prefs.getFloat("custom_dim", 0f)
        homeBackgroundEnabled = prefs.getBoolean("home_background_enabled", true)
        prefs.getString("home_background_path", null)?.let { path ->
            val file = File(path)
            if (file.exists()) homeBackgroundFile = file
        }
        prefs.getString("custom_background_path", null)?.let { path ->
            val file = File(path)
            if (file.exists()) customBackgroundUri = file
        }
    enableEdgeToEdge()

    WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = !darkTheme

    com.example.util.TransferNotificationManager(applicationContext)

    handleTransferNotificationAction(intent)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
      requestPermissions(
        arrayOf(Manifest.permission.POST_NOTIFICATIONS),
        1001
      )
    }

    setContent {
      SideEffect {
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = !darkTheme
      }

      MyApplicationTheme(darkTheme = darkTheme, themeColor = themeColor) {
        Box(modifier = Modifier.fillMaxSize()) {
          if (customBackgroundUri != null) {
            Box(modifier = Modifier.fillMaxSize()) {
              AsyncImage(
                model = customBackgroundUri?.let { "${it.absolutePath}?v=${it.lastModified()}" },
                contentDescription = null,
                modifier = Modifier
                  .fillMaxSize()
                  .blur(customBackgroundBlur.dp),
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
          }

          FileManagerScreen(
            darkTheme = darkTheme,
            viewModel = fileManagerViewModel,
            onThemeChange = { darkTheme = it; prefs.edit().putBoolean("dark_theme", it).apply() },
            onThemeColorChange = { themeColor = it; prefs.edit().putInt("theme_color", it).apply() },
            themeColor = themeColor,
            onCustomImagePick = { startActivityForResult(Intent(Intent.ACTION_GET_CONTENT).apply { type = "image/*"; addCategory(Intent.CATEGORY_OPENABLE) }, 2001) },
            onCustomImageApply = {
              pendingCustomBackgroundUri?.let { file ->
                val appliedFile = File(filesDir, "custom_background.jpg")
                file.inputStream().use { input ->
                  appliedFile.outputStream().use { output ->
                    input.copyTo(output)
                  }
                }
                Log.d("FFF_IMAGE", "APPLIED path=${appliedFile.absolutePath} exists=${appliedFile.exists()} size=${appliedFile.length()}")
                customBackgroundUri = appliedFile
                prefs.edit().putString("custom_background_path", appliedFile.absolutePath).apply()
                pendingCustomBackgroundUri = null
              }
            },
            onCustomImageCancel = { pendingCustomBackgroundUri = null },
            onCustomBlurChange = { customBackgroundBlur = it; prefs.edit().putFloat("custom_blur", it).apply() },
            onCustomOpacityChange = { customBackgroundOpacity = it; prefs.edit().putFloat("custom_dim", it).apply() },
            pendingCustomBackgroundUri = pendingCustomBackgroundUri,
            customBackgroundUri = customBackgroundUri,
            customBackgroundBlur = customBackgroundBlur,
            customBackgroundOpacity = customBackgroundOpacity,
                  homeBackgroundEnabled = homeBackgroundEnabled,
                  onHomeBackgroundPick = {
                      startActivityForResult(
                          Intent(Intent.ACTION_GET_CONTENT).apply {
                              type = "image/*"
                              addCategory(Intent.CATEGORY_OPENABLE)
                          },
                          2002
                      )
                  },
                  homeBackgroundFile = homeBackgroundFile,
                  homeBackgroundVersion = homeBackgroundVersion,
                  onHomeBackgroundChange = {
                      homeBackgroundEnabled = it
                      prefs.edit().putBoolean("home_background_enabled", it).apply()
                  }
          )
        }
      }
    }
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    Log.d("FFF_IMAGE", "RESULT request=$requestCode result=$resultCode uri=${data?.data}")
    if (requestCode == 2001 && resultCode == RESULT_OK) {
      data?.data?.let { uri ->
        try {
          val backgroundFile = File(filesDir, "custom_background_pending.jpg")
          val input = contentResolver.openInputStream(uri)
            ?: throw IllegalStateException("Cannot open selected image")
          input.use {
            FileOutputStream(backgroundFile).use { output ->
              it.copyTo(output)
            }
          }
          if (backgroundFile.exists() && backgroundFile.length() > 0L) {
              pendingCustomBackgroundUri = backgroundFile
          }
        } catch (e: Exception) {
          e.printStackTrace()
        }
      }
    }

  if (requestCode == 2002 && resultCode == RESULT_OK) {
      data?.data?.let { uri ->
        try {
          val homeFile = File(filesDir, "home_background_image")
          contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(homeFile).use { output ->
              input.copyTo(output)
            }
          } ?: throw IllegalStateException("Cannot open selected image")

          if (homeFile.exists() && homeFile.length() > 0L) {
            homeBackgroundFile = homeFile
            homeBackgroundVersion++
            prefs.edit()
              .putString("home_background_path", homeFile.absolutePath)
              .apply()
            Log.d(
              "FFF_IMAGE",
              "HOME APPLIED path=${homeFile.absolutePath} size=${homeFile.length()}"
            )
          }
        } catch (e: Exception) {
          e.printStackTrace()
        }
      }
    }
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    handleTransferNotificationAction(intent)
  }

  private fun handleTransferNotificationAction(intent: Intent?) {
    when (intent?.action) {
      TransferNotificationManager.ACTION_PAUSE_RESUME -> {
        fileManagerViewModel.toggleTransferPause()
      }

      TransferNotificationManager.ACTION_CANCEL -> {
        fileManagerViewModel.cancelTransfer()
      }
    }

    if (intent?.action == TransferNotificationManager.ACTION_PAUSE_RESUME ||
        intent?.action == TransferNotificationManager.ACTION_CANCEL) {
      intent.action = null
    }
  }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
  Text(text = "Hello $name!", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
  MyApplicationTheme { Greeting("Android") }
}
