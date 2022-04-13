package com.example.livestream.commons.data.models.commons

sealed class Result<T> {
    class Loading<T> : Result<T>()

    class Success<T>(val data: T) : Result<T>()

    class Failure<T>(val e: Throwable) : Result<T>()

    val isSuccess: Boolean get() = this is Success
}
