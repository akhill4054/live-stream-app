package com.example.livestream.commons.repositories

import com.example.livestream.commons.data.models.commons.LiveStream
import com.example.livestream.commons.data.models.commons.Result
import com.example.livestream.commons.data.remote.ApiService
import com.example.livestream.utils.doNetworkCall
import kotlinx.coroutines.flow.Flow

class MainRepository(private val apiService: ApiService) {

    suspend fun getRecommendedLiveStreams(): Flow<Result<List<LiveStream>>> {
        return doNetworkCall { apiService.getRecommendedLiveStreams() }
    }

    companion object {
        @Volatile
        private var INSTANCE: MainRepository? = null

        fun getInstance(apiService: ApiService): MainRepository {
            return INSTANCE ?: synchronized(UserRepository::class.java) {
                INSTANCE ?: MainRepository(apiService).also {
                    INSTANCE = it
                }
            }
        }
    }
}