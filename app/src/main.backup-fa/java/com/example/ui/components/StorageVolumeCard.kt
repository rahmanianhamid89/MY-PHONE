package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.StorageVolumeInfo
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.ElectricBlue

@Composable
fun StorageVolumeCard(
    volume: StorageVolumeInfo,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val stats = volume.stats
    val animatedPercent by animateFloatAsState(
        targetValue = stats.usedPercent.coerceIn(0f, 1f),
        label = "storage_capsule_percent"
    )

    val icon = if (volume.isUsbOtg) Icons.Default.Usb else Icons.Default.SdStorage
    val accentColor = if (volume.isUsbOtg) CyanAccent else ElectricBlue

    // Clean, flat item directly on the page background (no heavy card container)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp)
            .testTag("storage_volume_item_${volume.name}")
    ) {
        // Top row: Drive Icon + Name, and right opposite is the Capsule progress bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = volume.name,
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = volume.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Storage capsule opposite the drive name
            StorageCapsule(
                usedPercent = animatedPercent,
                accentColor = accentColor,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Drive information directly underneath with fine/small font (Used, Free/Usable, Total)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 32.dp, end = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Used: ${stats.formattedUsed.ifEmpty { "0 B" }}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )

            Text(
                text = "Free: ${stats.formattedFree.ifEmpty { "0 B" }}",
                style = MaterialTheme.typography.bodySmall,
                color = accentColor,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp
            )

            Text(
                text = "Total: ${stats.formattedTotal.ifEmpty { "0 B" }}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun StorageCapsule(
    usedPercent: Float,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val percentageInt = (usedPercent * 100).toInt().coerceIn(0, 100)

    // Capsule pill with filled progress and percentage label
    Box(
        modifier = modifier
            .width(84.dp)
            .height(24.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
    ) {
        // Filled fill width
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction = usedPercent.coerceIn(0f, 1f))
                .height(24.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (usedPercent > 0.9f) MaterialTheme.colorScheme.error else accentColor
                )
        )

        // Percentage text centered inside capsule
        Text(
            text = "$percentageInt%",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = if (usedPercent > 0.5f) Color.White else MaterialTheme.colorScheme.onSurface,
            fontSize = 10.sp,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}
