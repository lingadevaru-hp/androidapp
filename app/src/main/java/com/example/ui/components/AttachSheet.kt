package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.PortfolioTheme

enum class AttachOption {
    IMAGES,
    CAMERA,
    FILES
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttachPickerSheet(
    onPick: (AttachOption) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = PortfolioTheme.colors.surface,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Attach",
                style = PortfolioTheme.typography.titleMedium.copy(color = PortfolioTheme.colors.text),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
            )
            AttachRow(
                icon = Icons.Default.Image,
                label = "Images",
                sub = "Pick a photo from gallery",
                testTag = "attach_option_images",
                onClick = { onPick(AttachOption.IMAGES) }
            )
            AttachRow(
                icon = Icons.Default.PhotoCamera,
                label = "Camera",
                sub = "Take a photo now",
                testTag = "attach_option_camera",
                onClick = { onPick(AttachOption.CAMERA) }
            )
            AttachRow(
                icon = Icons.Default.Description,
                label = "Files",
                sub = "Choose a document from this device",
                testTag = "attach_option_files",
                onClick = { onPick(AttachOption.FILES) }
            )
        }
    }
}

@Composable
private fun AttachRow(
    icon: ImageVector,
    label: String,
    sub: String,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 12.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = PortfolioTheme.colors.accent,
            modifier = Modifier.size(22.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(
                text = label,
                style = PortfolioTheme.typography.bodyMedium.copy(color = PortfolioTheme.colors.text)
            )
            Text(
                text = sub,
                style = PortfolioTheme.typography.labelSmall.copy(color = PortfolioTheme.colors.textDim)
            )
        }
    }
}
