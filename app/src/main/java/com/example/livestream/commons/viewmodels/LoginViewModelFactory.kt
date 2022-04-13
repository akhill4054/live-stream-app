package com.example.livestream.commons.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.livestream.commons.repositories.AuthRepository
import com.example.livestream.commons.repositories.UserRepository

class LoginViewModelFactory(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LoginViewModel(authRepository, userRepository) as T
    }
}
