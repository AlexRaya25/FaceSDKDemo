package com.rayadev.facesdkdemo.domain.model

import android.graphics.Bitmap

data class MainUiState(
    val selfieBitmap: Bitmap? = null,
    val galleryBitmap: Bitmap? = null,
    val similarityPercent: Double? = null,
    val isLoading: Boolean = false,
    val canCompare: Boolean = false
)