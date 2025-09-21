package com.rayadev.facesdkdemo.ui.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rayadev.facesdkdemo.domain.model.CaptureMode
import com.rayadev.facesdkdemo.domain.model.MainUiState
import com.rayadev.facesdkdemo.domain.model.Result
import com.rayadev.facesdkdemo.domain.model.UiEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FaceViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow: SharedFlow<UiEvent> = _eventFlow.asSharedFlow()

    fun onSelfieCaptureClicked(mode: CaptureMode) {
        viewModelScope.launch {
            _eventFlow.emit(UiEvent.LaunchFaceCapture(mode))
        }
    }

    fun onCompareClicked() {
        val currentState = _uiState.value
        val selfie = currentState.selfieBitmap
        val gallery = currentState.galleryBitmap

        if (selfie != null && gallery != null) {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true) }
                _eventFlow.emit(UiEvent.LaunchFaceComparison(selfie, gallery))
            }
        }
    }

    fun onResetClicked() {
        _uiState.value = MainUiState()
    }

    fun onSdkInitialized(result: Result<Unit>) {
        if (result is Result.Error) {
            viewModelScope.launch {
                _eventFlow.emit(UiEvent.ShowSnackbar("SDK initialization failed. The app will not work."))
            }
        }
    }

    fun onFaceCaptured(result: Result<Bitmap>) {
        _uiState.update { it.copy(isLoading = false) }
        when (result) {
            is Result.Success -> _uiState.update {
                val newState = it.copy(selfieBitmap = result.value)
                newState.copy(canCompare = newState.selfieBitmap != null && newState.galleryBitmap != null)
            }
            is Result.Error -> viewModelScope.launch {
                _eventFlow.emit(UiEvent.ShowSnackbar(result.message ?: "Failed to capture face"))
            }
            else -> {}
        }
    }

    fun onGalleryImageSelected(bitmap: Bitmap?) {
        if (bitmap != null) {
            _uiState.update {
                val newState = it.copy(galleryBitmap = bitmap)
                newState.copy(canCompare = newState.selfieBitmap != null && newState.galleryBitmap != null)
            }
        } else {
            viewModelScope.launch {
                _eventFlow.emit(UiEvent.ShowSnackbar("Failed to load image from gallery"))
            }
        }
    }

    fun onFacesCompared(result: Result<Double>) {
        _uiState.update { it.copy(isLoading = false) }
        when (result) {
            is Result.Success -> _uiState.update {
                it.copy(similarityPercent = result.value)
            }
            is Result.Error -> viewModelScope.launch {
                _eventFlow.emit(UiEvent.ShowSnackbar(result.message ?: "Failed to compare faces"))
            }
            else -> {}
        }
    }
}