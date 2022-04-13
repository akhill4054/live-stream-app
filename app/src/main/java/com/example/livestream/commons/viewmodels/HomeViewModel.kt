package com.example.livestream.commons.viewmodels

import androidx.lifecycle.*
import com.example.livestream.commons.data.models.commons.LiveStream
import com.example.livestream.commons.data.models.commons.Result
import com.example.livestream.commons.repositories.AuthRepository
import com.example.livestream.commons.repositories.LiveStreamRepository
import com.example.livestream.commons.repositories.MainRepository
import com.example.livestream.commons.repositories.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class HomeViewModel(
    private val mainRepository: MainRepository,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val streamingsRepository: LiveStreamRepository,
) : ViewModel() {

    val user = userRepository.user.asLiveData(viewModelScope.coroutineContext)

    private val _recommendedLiveStreams = MutableLiveData<Result<List<LiveStream>>>()
    val recommendedLiveStreams: LiveData<Result<List<LiveStream>>> = _recommendedLiveStreams

    init {
        getRecommendedLiveStreams()
    }

    fun getRecommendedLiveStreams() {
        viewModelScope.launch(Dispatchers.IO) {
            mainRepository.getRecommendedLiveStreams().collect { result ->
                _recommendedLiveStreams.postValue(result)
            }
        }
    }

    fun signOut() {
        authRepository.signOut()
    }
}
