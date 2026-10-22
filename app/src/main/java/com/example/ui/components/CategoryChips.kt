package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import com.example.model.FileCategory

@Composable
fun CategoryChips(
    selectedCategory: FileCategory?,
    onCategorySelected: (FileCategory?) -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = listOf(
        null, // All files (directory view)
        FileCategory.APK,
        FileCategory.DOCUMENT,
        FileCategory.IMAGE,
        FileCategory.VIDEO,
        FileCategory.AUDIO,
        FileCategory.ARCHIVE
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("category_chips_row"),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        categories.forEach { category ->
            val isSelected = selectedCategory == category
            val title = category?.titleFa ?: "Folders"
            val icon = category?.icon ?: FileCategory.ALL.icon
            val categoryColor = category?.color ?: MaterialTheme.colorScheme.primary

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) {
                    categoryColor.copy(alpha = 0.2f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                },
                tonalElevation = if (isSelected) 4.dp else 0.dp,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        onCategorySelected(if (isSelected && category != null) null else category)
                    }
                    .testTag("category_chip_${category?.name ?: "ALL"}")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (isSelected) categoryColor else categoryColor.copy(alpha = 0.2f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = if (isSelected) Color.White else categoryColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Light,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
