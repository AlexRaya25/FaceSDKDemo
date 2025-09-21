package com.rayadev.facesdkdemo

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.rayadev.facesdkdemo.data.sdk.FaceSdkManager
import com.rayadev.facesdkdemo.domain.model.CaptureMode
import com.rayadev.facesdkdemo.domain.model.Result
import com.rayadev.facesdkdemo.domain.model.UiEvent
import com.rayadev.facesdkdemo.ui.MainScreen
import com.rayadev.facesdkdemo.ui.viewmodel.FaceViewModel
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {

    private val viewModel: FaceViewModel by viewModels()

    private var hasCameraPermission by mutableStateOf(false)
    private var hasStoragePermission by mutableStateOf(false)
    private val requiredStoragePermission =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            Manifest.permission.READ_MEDIA_IMAGES
        else
            Manifest.permission.READ_EXTERNAL_STORAGE

    private val requestPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            hasCameraPermission = permissions[Manifest.permission.CAMERA] == true
            hasStoragePermission = permissions[requiredStoragePermission] == true

            if (!hasCameraPermission || !hasStoragePermission) {
                viewModel.onSdkInitialized(
                    Result.Error("Permissions denied. Camera and storage are required.")
                )
            }
        }

    private val pickImage =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null && hasStoragePermission) {
                val bitmap = uriToBitmap(uri)
                viewModel.onGalleryImageSelected(bitmap)
            } else {
                viewModel.onSdkInitialized(Result.Error("Storage permission denied or image not selected."))
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestPermissions.launch(arrayOf(Manifest.permission.CAMERA, requiredStoragePermission))

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            FaceSdkManager.initialize(this, viewModel::onSdkInitialized)
            hasCameraPermission = true
        } else {
            hasCameraPermission = false
        }

        setContent {
            MaterialTheme {
                val uiState by viewModel.uiState.collectAsState()
                val snackbarHostState = remember { SnackbarHostState() }

                LaunchedEffect(key1 = true) {
                    viewModel.eventFlow.collectLatest { event ->
                        when (event) {
                            is UiEvent.ShowSnackbar -> {
                                snackbarHostState.showSnackbar(
                                    message = event.message,
                                    duration = SnackbarDuration.Short
                                )
                            }
                            is UiEvent.LaunchFaceCapture -> {
                                if (hasCameraPermission) {
                                    FaceSdkManager.captureFace(this@MainActivity, event.mode, viewModel::onFaceCaptured)
                                } else {
                                    viewModel.onSdkInitialized(Result.Error("Camera permission denied."))
                                }
                            }
                            is UiEvent.LaunchFaceComparison -> {
                                FaceSdkManager.compareFaces(
                                    this@MainActivity,
                                    event.selfie,
                                    event.gallery,
                                    viewModel::onFacesCompared
                                )
                            }
                        }
                    }
                }

                Surface(modifier = Modifier.fillMaxSize()) {
                    MainScreen(
                        state = uiState,
                        snackbarHostState = snackbarHostState,
                        onCapturePassive = {
                            if (hasCameraPermission) viewModel.onSelfieCaptureClicked(CaptureMode.LIVENESS_PASSIVE)
                            else viewModel.onSdkInitialized(Result.Error("Camera permission denied."))
                        },
                        onCaptureActive = {
                            if (hasCameraPermission) viewModel.onSelfieCaptureClicked(CaptureMode.LIVENESS_ACTIVE)
                            else viewModel.onSdkInitialized(Result.Error("Camera permission denied."))
                        },
                        onPickGallery = {
                            if (hasStoragePermission) pickImage.launch("image/*")
                            else viewModel.onSdkInitialized(Result.Error("Storage permission denied."))
                        },
                        onCompare = viewModel::onCompareClicked,
                        onReset = viewModel::onResetClicked
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        FaceSdkManager.deinitialize()
    }

    private fun uriToBitmap(uri: Uri): Bitmap {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(contentResolver, uri)
            ImageDecoder.decodeBitmap(source)
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(contentResolver, uri)
        }
    }
}
