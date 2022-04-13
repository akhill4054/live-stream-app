package com.example.livestream.commons.viewmodels

import androidx.lifecycle.*
import com.example.livestream.commons.data.models.commons.Result
import com.example.livestream.commons.data.models.commons.User
import com.example.livestream.commons.data.models.user.UpdateUserProfile
import com.example.livestream.commons.repositories.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.io.File

class ProfileViewModel(
    private val userRepository: UserRepository,
) : ViewModel() {
    val user =
        userRepository.user.asLiveData(viewModelScope.coroutineContext)

    private val _profileUpdate = MutableLiveData<Result<User>>()
    val profileUpdate: LiveData<Result<User>> = _profileUpdate

    fun updateUserProfile(picFile: File?, userProfile: UpdateUserProfile.UserProfile) {
        viewModelScope.launch(Dispatchers.IO) {
            userRepository.updateUserProfile(picFile, userProfile).collect {
                _profileUpdate.postValue(it)
            }
        }
    }
}
