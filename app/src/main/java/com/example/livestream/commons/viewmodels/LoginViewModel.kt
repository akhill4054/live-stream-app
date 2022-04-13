package com.example.livestream.commons.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.livestream.commons.data.models.auth.UserPrefillData
import com.example.livestream.commons.data.models.commons.Result
import com.example.livestream.commons.data.models.commons.User
import com.example.livestream.commons.repositories.AuthRepository
import com.example.livestream.commons.repositories.UserRepository
import com.example.livestream.utils.mapSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _authVerificationStatus = MutableLiveData<Result<UserPrefillData>>()
    val authVerificationStatus: LiveData<Result<UserPrefillData>> = _authVerificationStatus

    private val _userProfile = MutableLiveData<Result<User?>>()
    val userProfile: LiveData<Result<User?>> = _userProfile

    fun authenticateUser(firebaseAuthToken: String) {
        viewModelScope.launch(Dispatchers.IO) {
            authRepository.authenticateUser(firebaseAuthToken).collect { result ->
                _authVerificationStatus.postValue(result)
            }
        }
    }

    fun getUserprofile() {
        viewModelScope.launch(Dispatchers.IO) {
            userRepository.getUserProfile().mapSuccess { data ->
                val user = data.user

                if (user != null) {
                    // Save user.
                    userRepository.updateUserProfile(user)
                }
                user
            }.collect { result ->
                _userProfile.postValue(result)
            }
        }
    }
}
