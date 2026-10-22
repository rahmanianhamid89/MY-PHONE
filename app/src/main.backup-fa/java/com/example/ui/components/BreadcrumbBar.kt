package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File

@Composable
fun BreadcrumbBar(
    breadcrumbs: List<File>,
    onHomeClick: () -> Unit,
    onNavigate: (File) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    // Auto-scroll to end when path changes
    LaunchedEffect(breadcrumbs) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag("breadcrumb_bar"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Home Icon at the very beginning of the address bar; tapping it navigates to Home
        IconButton(
            onClick = onHomeClick,
            modifier = Modifier
                .size(36.dp)
                .testTag("breadcrumb_home_button")
        ) {
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = "Home",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
        }

        // Separator between Home and the path segments
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.outline
        )

        // 2. The rest of the address path written horizontally
        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(scrollState),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            breadcrumbs.forEachIndexed { index, file ->
                val isLast = index == breadcrumbs.size - 1
                val displayName = if (index == 0) "Internal Storage" else file.name

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onNavigate(file) }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                        .testTag("breadcrumb_item_$index")
                ) {
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isLast) FontWeight.Bold else FontWeight.Normal,
                        color = if (isLast) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                }

                if (!isLast) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "/",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}
