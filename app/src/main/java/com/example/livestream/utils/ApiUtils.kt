package com.example.livestream.utils

import com.example.livestream.commons.data.models.commons.Result
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import retrofit2.HttpException
import retrofit2.Response

suspend inline fun <T> doNetworkCall(
    crossinline call: suspend () -> Response<T>
): Flow<Result<T>> =
    flow<Result<T>> {
        emit(Result.Loading())

        val response = call.invoke()

        if (response.isSuccessful) {
            emit(Result.Success(data = response.body()!!))
        } else {
            throw HttpException(response)
        }
    }.catch { e ->
        emit(Result.Failure(e.parseError()))
    }.flowOn(Dispatchers.IO)


fun Throwable.parseError(): Throwable {
    if (this is HttpException) {
        try {
            val body = this.response()!!.errorBody()!!.string()
            val parsedError = Gson().fromJson(body, ErrorMessage::class.java)
            return Exception(parsedError.message)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    return this
}

data class ErrorMessage(
    @SerializedName("message")
    val message: String,
)


inline fun <T, R> Flow<Result<T>>.mapSuccess(crossinline onSuccess: (data: T) -> R): Flow<Result<R>> {
    return map { result ->
        when (result) {
            is Result.Success -> Result.Success(data = onSuccess(result.data))
            is Result.Failure -> Result.Failure(e = result.e)
            is Result.Loading -> Result.Loading()
        }
    }
}
