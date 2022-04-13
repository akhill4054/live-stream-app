package com.example.livestream.commons.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.livestream.commons.repositories.AuthRepository
import com.example.livestream.commons.repositories.LiveStreamRepository
import com.example.livestream.commons.repositories.UserRepository

class ScheduleLiveStreamVMF(
    private val liveStreamRepository: LiveStreamRepository,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ScheduleLiveStreamVM(liveStreamRepository) as T
    }
}
