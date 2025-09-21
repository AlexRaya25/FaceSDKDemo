package com.rayadev.facesdkdemo.domain.model

import android.graphics.Bitmap

sealed class UiEvent {
    data class ShowSnackbar(val message: String) : UiEvent()
    data class LaunchFaceCapture(val mode: CaptureMode) : UiEvent()
    data class LaunchFaceComparison(val selfie: Bitmap, val gallery: Bitmap) : UiEvent()
}