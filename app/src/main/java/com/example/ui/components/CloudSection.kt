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
fun CloudSection(
    onConnectCloud: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val cloudServices = listOf(
        Triple("Google Drive", "Cloud Storage", Color(0xFF4285F4)),
        Triple("OneDrive", "Microsoft Cloud", Color(0xFF0078D4)),
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("cloud_section")
    ) {
        // Flat Section Header (no background)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = LucideIcons.Cloud,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Cloud",
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

        // Two-column layout in rows directly on the screen without card backgrounds
        val chunkedServices = cloudServices.chunked(2)
        chunkedServices.forEach { rowItems ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                for ((name, subtitle, color) in rowItems) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onConnectCloud(name) }
                            .padding(vertical = 6.dp, horizontal = 4.dp)
                            .testTag("cloud_item_$name"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (name.contains("FTP")) LucideIcons.Network else LucideIcons.Cloudy,
                            contentDescription = name,
                            tint = color,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Light,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp
                            )
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }

                    }
                }

                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
