package com.example.livestream.commons.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.livestream.commons.data.models.commons.Result
import com.example.livestream.commons.data.models.streamings.LikeLiveStream
import com.example.livestream.commons.data.models.streamings.WatchLiveStream
import com.example.livestream.commons.data.remote.ApiService
import com.example.livestream.utils.doNetworkCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class StreamingVM(
    private val apiService: ApiService,
) : ViewModel() {

    private val _watchDetails = MutableLiveData<Result<WatchLiveStream>>()
    val watchDetails: LiveData<Result<WatchLiveStream>> = _watchDetails

    private val _likeLiveStream = MutableLiveData<Result<LikeLiveStream.Response>>()
    val likeLiveStream: LiveData<Result<LikeLiveStream.Response>> = _likeLiveStream

    fun watchLiveStream(streamingId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            doNetworkCall { apiService.watchLiveStream(streamingId) }
                .collect { _watchDetails.postValue(it) }
        }
    }

    fun likeLiveStream(streamingId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            doNetworkCall { apiService.likeLiveStream(streamingId) }
                .collect { _likeLiveStream.postValue(it) }
        }
    }

    fun dislikeLiveStream(streamingId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            doNetworkCall { apiService.dislikeLiveStream(streamingId) }
                .collect { _likeLiveStream.postValue(it) }
        }
    }
}
