package com.example.livestream.commons.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.livestream.commons.repositories.AuthRepository
import com.example.livestream.commons.repositories.UserRepository

class ProfileViewModelFactory(
    private val userRepository: UserRepository,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ProfileViewModel(userRepository) as T
    }
}
