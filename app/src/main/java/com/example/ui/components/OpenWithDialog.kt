package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.material3.ListItem
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import android.graphics.drawable.BitmapDrawable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.model.OpenWithApp

@Composable
fun OpenWithDialog(
    apps: List<OpenWithApp>,
    onAppSelected: (OpenWithApp, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var always by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Open With")
        },
        text = {
            Column {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(
                        items = apps,
                        key = { "${it.packageName}/${it.activityName}" }
                    ) { app ->
                        ListItem(
                            leadingContent = {
                                val drawable = app.icon
                                val bitmap = if (drawable is BitmapDrawable) {
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

                                androidx.compose.foundation.Image(
                                    painter = BitmapPainter(bitmap.asImageBitmap()),
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp)
                                )
                            },
                            headlineContent = {
                                Text(app.label)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onAppSelected(app, always)
                                }
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = always,
                        onCheckedChange = { always = it }
                    )
                    Text("Always")
                }
            }
        },
        confirmButton = {},
        dismissButton = {}
    )
}
