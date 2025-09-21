package com.rayadev.facesdkdemo.data.sdk

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import com.rayadev.facesdkdemo.domain.model.CaptureMode
import com.rayadev.facesdkdemo.domain.model.Result
import com.regula.facesdk.FaceSDK
import com.regula.facesdk.configuration.LivenessConfiguration
import com.regula.facesdk.enums.ImageType
import com.regula.facesdk.enums.LivenessStatus
import com.regula.facesdk.enums.LivenessType
import com.regula.facesdk.model.MatchFacesImage
import com.regula.facesdk.request.MatchFacesRequest

object FaceSdkManager {

    fun initialize(context: Context, onComplete: (Result<Unit>) -> Unit) {
        try {
            FaceSDK.Instance().initialize(context) { status, exception ->
                if (status) {
                    onComplete(Result.Success(Unit))
                } else {
                    onComplete(Result.Error("SDK initialization failed", exception))
                }
            }
        } catch (e: Exception) {
            onComplete(Result.Error("Exception during SDK initialization", e))
        }
    }

    fun deinitialize() {
        try {
            FaceSDK.Instance().deinitialize()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun captureFace(
        activity: Activity,
        mode: CaptureMode,
        onResult: (Result<Bitmap>) -> Unit) {
        try {
            val builder = LivenessConfiguration.Builder()

            if (mode == CaptureMode.LIVENESS_PASSIVE) {
                builder.setType(LivenessType.PASSIVE)
            }
            val config = builder.build()

            FaceSDK.Instance().startLiveness(activity, config) { response ->
                if (response == null) {
                    onResult(Result.Canceled)
                    return@startLiveness
                }

                if (response.exception != null) {
                    onResult(Result.Error("Liveness error", response.exception))
                    return@startLiveness
                }

                if (response.liveness != LivenessStatus.PASSED) {
                    onResult(Result.Error("Liveness check failed (status=${response.liveness})"))
                    return@startLiveness
                }

                val bitmap: Bitmap? = response.bitmap
                if (bitmap != null) {
                    onResult(Result.Success(bitmap))
                } else {
                    onResult(Result.Error("No face image returned from liveness"))
                }
            }
        } catch (e: Exception) {
            onResult(Result.Error("Exception during face capture", e))
        }
    }

    fun compareFaces(
        activity: Activity,
        selfieBitmap: Bitmap,
        galleryBitmap: Bitmap,
        onResult: (Result<Double>) -> Unit
    ) {
        val request = MatchFacesRequest(
            listOf(
                MatchFacesImage(selfieBitmap, ImageType.LIVE),
                MatchFacesImage(galleryBitmap, ImageType.PRINTED)
            )
        )

        FaceSDK.Instance().matchFaces(activity, request) { response ->
            when {
                response.exception != null -> {
                    onResult(Result.Error("Face comparison error", response.exception))
                }
                response.results.isEmpty() -> {
                    onResult(Result.Error("No comparison result returned"))
                }
                else -> {
                    val similarity = response.results.firstOrNull()?.similarity ?: 0.0
                    onResult(Result.Success(similarity * 100.0))
                }
            }
        }
    }
}
