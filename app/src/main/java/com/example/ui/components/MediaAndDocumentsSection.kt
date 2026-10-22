package com.example.ui.components
import androidx.compose.ui.graphics.Color
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FileCategory
import androidx.compose.material3.Divider

@Composable
fun MediaAndDocumentsSection(
    selectedCategory: FileCategory?,
    onCategorySelected: (FileCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    // Only media and document items (no folders category as requested)
    val mediaItems = listOf(
        Pair(FileCategory.DOCUMENT, "Documents"),
        Pair(FileCategory.IMAGE, "Images"),
        Pair(FileCategory.VIDEO, "Videos"),
        Pair(FileCategory.AUDIO, "Audio"),
        Pair(FileCategory.ARCHIVE, "Archives"),
        Pair(FileCategory.APK, "APK Files")
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("media_and_documents_section")
    ) {
        // Flat Section Header (no background)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = LucideIcons.Images,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Media & Documents",
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
        val chunkedItems = mediaItems.chunked(2)
        chunkedItems.forEach { rowItems ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                for ((cat, title) in rowItems) {
                    val isSelected = selectedCategory == cat
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onCategorySelected(cat) }
                            .padding(vertical = 6.dp, horizontal = 4.dp)
                            .testTag("media_category_${cat.name}"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = cat.icon,
                            contentDescription = title,
                            tint = if (isSelected) MaterialTheme.colorScheme.primary else cat.color,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected) FontWeight.Light else FontWeight.Light,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            fontSize = 14.sp
                        )
                    }
                }

                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
