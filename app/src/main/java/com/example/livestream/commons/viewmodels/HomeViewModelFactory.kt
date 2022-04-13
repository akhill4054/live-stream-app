package com.example.livestream.commons.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.livestream.commons.repositories.AuthRepository
import com.example.livestream.commons.repositories.LiveStreamRepository
import com.example.livestream.commons.repositories.MainRepository
import com.example.livestream.commons.repositories.UserRepository

class HomeViewModelFactory(
    private val mainRepository: MainRepository,
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
    private val streamingsRepository: LiveStreamRepository,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HomeViewModel(
            mainRepository,
            authRepository,
            userRepository,
            streamingsRepository
        ) as T
    }
}
