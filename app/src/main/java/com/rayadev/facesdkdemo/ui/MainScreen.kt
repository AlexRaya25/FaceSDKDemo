package com.rayadev.facesdkdemo.ui

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rayadev.facesdkdemo.domain.model.MainUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    state: MainUiState,
    snackbarHostState: SnackbarHostState,
    onCapturePassive: () -> Unit,
    onCaptureActive: () -> Unit,
    onPickGallery: () -> Unit,
    onCompare: () -> Unit,
    onReset: () -> Unit
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FaceCard(bitmap = state.selfieBitmap, label = "Selfie")
                FaceCard(bitmap = state.galleryBitmap, label = "Gallery")
            }

            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(60.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 4.dp
                )
            }

            state.similarityPercent?.let {
                SimilarityCard(similarity = it)
            }

            Spacer(modifier = Modifier.weight(1f))

            ActionButtons(
                onCaptureActive = onCaptureActive,
                onCapturePassive = onCapturePassive,
                onPickGallery = onPickGallery,
                onCompare = onCompare,
                onReset = onReset,
                isCompareEnabled = state.canCompare && !state.isLoading
            )
        }
    }
}

@Composable
fun FaceCard(bitmap: Bitmap?, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = label,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text(
                    text = "Empty",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(text = label, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge)
    }
}


@Composable
fun SimilarityCard(similarity: Double) {
    val (bgColor, textColor) = when {
        similarity > 80 -> Color(0xFF2E7D32) to Color.White
        similarity > 50 -> Color(0xFFFFA000) to Color.Black
        else -> Color(0xFFC62828) to Color.White
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Similarity: ${"%.2f".format(similarity)}%",
                color = textColor,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}


@Composable
fun ActionButtons(
    onCaptureActive: () -> Unit,
    onCapturePassive: () -> Unit,
    onPickGallery: () -> Unit,
    onCompare: () -> Unit,
    onReset: () -> Unit,
    isCompareEnabled: Boolean
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        StyledButton(text = "1. Capture (Active)", onClick =  onCaptureActive)
        StyledButton(text = "1. Capture (Passive)", onClick =  onCapturePassive)
        StyledButton(text = "2. Select from Gallery", onClick = onPickGallery)
        StyledButton(
            text = "3. Compare Faces",
            onClick = onCompare,
            enabled = isCompareEnabled
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        StyledButton(text = "Reset", onClick = onReset, backgroundColor = MaterialTheme.colorScheme.secondary)
    }
}


@Composable
fun StyledButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    backgroundColor: Color = MaterialTheme.colorScheme.primary
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(text = text, fontSize = 16.sp)
    }
}
