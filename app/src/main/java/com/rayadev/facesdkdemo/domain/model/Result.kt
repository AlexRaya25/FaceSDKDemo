package com.rayadev.facesdkdemo.domain.model

sealed class Result<out T> {
    data class Success<out T>(val value: T) : Result<T>()
    data class Error(val message: String? = null, val exception: Exception? = null) : Result<Nothing>()
    object Canceled : Result<Nothing>()
}
