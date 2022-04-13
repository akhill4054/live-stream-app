package com.example.livestream.commons.repositories

import com.example.livestream.commons.data.models.commons.Result
import com.example.livestream.commons.data.models.streamings.ScheduleLiveStream
import com.example.livestream.commons.data.remote.ApiService
import com.example.livestream.utils.doNetworkCall
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

class LiveStreamRepository(private val apiService: ApiService) {

    suspend fun scheduleLiveStream(
        thumbnailFile: File,
        details: ScheduleLiveStream.StreamingDetails,
    ): Flow<Result<ScheduleLiveStream.Response>> {
        val thumbnailFilePart =
            MultipartBody.Part.createFormData(
                "thumbnail",
                thumbnailFile.name,
                RequestBody.create(MediaType.parse("image/*"), thumbnailFile),
            )
        val liveStreamDetails =
            RequestBody.create(MediaType.parse("application/json"), Gson().toJson(details))

        return doNetworkCall {
            apiService.scheduleLiveStream(
                thumbnail = thumbnailFilePart,
                streamingDetails = liveStreamDetails,
            )
        }
    }

    suspend fun startLiveStream(
        thumbnailFile: File,
        details: ScheduleLiveStream.StreamingDetails,
    ): Flow<Result<ScheduleLiveStream.Response>> {
        val thumbnailFilePart =
            MultipartBody.Part.createFormData(
                "thumbnail",
                thumbnailFile.name,
                RequestBody.create(MediaType.parse("image/*"), thumbnailFile),
            )
        val liveStreamDetails =
            RequestBody.create(MediaType.parse("application/json"), Gson().toJson(details))

        return doNetworkCall {
            apiService.startLiveStream(
                thumbnail = thumbnailFilePart,
                streamingDetails = liveStreamDetails,
            )
        }
    }

    suspend fun editScheduledLiveStream(
        streamingId: String,
        thumbnailFile: File? = null,
        details: ScheduleLiveStream.StreamingDetails,
    ): Flow<Result<ScheduleLiveStream.Response>> {
        val thumbnailFilePart = if (thumbnailFile != null) {
            MultipartBody.Part.createFormData(
                "thumbnail",
                thumbnailFile.name,
                RequestBody.create(MediaType.parse("image/*"), thumbnailFile),
            )
        } else {
            null
        }
        val liveStreamDetails =
            RequestBody.create(MediaType.parse("application/json"), Gson().toJson(details))

        return doNetworkCall {
            apiService.editScheduleLiveStream(
                streamingId = streamingId,
                thumbnail = thumbnailFilePart,
                streamingDetails = liveStreamDetails,
            )
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: LiveStreamRepository? = null

        fun getInstance(apiService: ApiService): LiveStreamRepository {
            return INSTANCE ?: synchronized(UserRepository::class.java) {
                INSTANCE ?: LiveStreamRepository(apiService).also {
                    INSTANCE = it
                }
            }
        }
    }
}
