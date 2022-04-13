package com.example.livestream.commons.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.livestream.commons.data.remote.ApiService

class StreamingVMF(
    private val apiService: ApiService,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return StreamingVM(apiService) as T
    }
}
