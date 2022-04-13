package com.example.livestream.commons.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.livestream.commons.data.models.commons.Result
import com.example.livestream.commons.data.models.streamings.ScheduleLiveStream
import com.example.livestream.commons.repositories.LiveStreamRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.io.File

class ScheduleLiveStreamVM(
    private val liveStreamRepository: LiveStreamRepository,
) : ViewModel() {

    private val _scheduleStatus = MutableLiveData<Result<ScheduleLiveStream.Response>>()
    val scheduleStatus: LiveData<Result<ScheduleLiveStream.Response>> = _scheduleStatus

    fun scheduleLiveStream(
        thumbnailFile: File,
        details: ScheduleLiveStream.StreamingDetails,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            liveStreamRepository.scheduleLiveStream(thumbnailFile, details).collect { result ->
                _scheduleStatus.postValue(result)
            }
        }
    }

    fun startLiveStream(
        thumbnailFile: File,
        details: ScheduleLiveStream.StreamingDetails,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            liveStreamRepository.startLiveStream(thumbnailFile, details).collect { result ->
                _scheduleStatus.postValue(result)
            }
        }
    }

    fun editScheduledLiveStream(
        streamingId: String,
        thumbnailFile: File? = null,
        details: ScheduleLiveStream.StreamingDetails,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            liveStreamRepository.editScheduledLiveStream(streamingId, thumbnailFile, details)
                .collect { result ->
                    _scheduleStatus.postValue(result)
                }
        }
    }
}
