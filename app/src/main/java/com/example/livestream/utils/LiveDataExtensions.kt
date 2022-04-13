package com.example.livestream.utils

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import com.example.livestream.commons.data.models.commons.Result

inline fun <T> LifecycleOwner.observeResult(
    liveData: LiveData<Result<T>>,
    crossinline onSuccess: (data: T) -> Unit,
    crossinline onLoading: (isLoading: Boolean) -> Unit = {},
    crossinline onFailure: (e: Throwable) -> Unit = {},
    skipDisableLoadingOnSuccess: Boolean = false,
    skipDisableLoadingOnFailure: Boolean = false
) {
    liveData.observe(this) { result ->
        when (result) {
            is Result.Success -> {
                onSuccess(result.data)
                if (!skipDisableLoadingOnSuccess) onLoading(false)
            }
            is Result.Failure -> {
                onFailure(result.e)
                if (!skipDisableLoadingOnFailure) onLoading(false)
            }
            is Result.Loading -> {
                onLoading(true)
            }
        }
    }
}
