package com.example.ui.components
import com.example.ui.icons.LucideIcons

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Divider

@Composable
fun UtilitySection(
    onNewTextFile: () -> Unit,
    onOpenPdfSection: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("utility_section")
    ) {
        // Flat Section Header (no background)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = LucideIcons.Wrench,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Utility",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Light,
                fontSize = 17.sp,
                                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Divider(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
            thickness = 1.dp
        )

        // Two columns for Text Editor and PDF Viewer directly on the page (no card background)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Text Editor
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onNewTextFile() }
                    .padding(vertical = 6.dp, horizontal = 4.dp)
                    .testTag("utility_text_editor"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = LucideIcons.FilePen,
                    contentDescription = "Text Editor",
                    tint = Color(0xFF0288D1),
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Text Editor",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Light,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp
                    )
                    Text(
                        fontWeight = FontWeight.Light,
                        text = "Create / edit text",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }

            // PDF Viewer
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onOpenPdfSection() }
                    .padding(vertical = 6.dp, horizontal = 4.dp)
                    .testTag("utility_pdf_viewer"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = LucideIcons.FileType,
                    contentDescription = "PDF Viewer",
                    tint = Color(0xFFE53935),
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        fontWeight = FontWeight.Light,
                        text = "PDF Viewer",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp
                    )
                    Text(
                        fontWeight = FontWeight.Light,
                        text = "Read PDF documents",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
