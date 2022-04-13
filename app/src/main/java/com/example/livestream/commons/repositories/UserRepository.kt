package com.example.livestream.commons.repositories

import android.app.Application
import com.example.livestream.commons.data.models.commons.Result
import com.example.livestream.commons.data.models.commons.User
import com.example.livestream.commons.data.models.user.UpdateUserProfile
import com.example.livestream.commons.data.models.user.UserProfile
import com.example.livestream.commons.data.remote.ApiService
import com.example.livestream.utils.doNetworkCall
import com.example.livestream.utils.mapSuccess
import com.example.livestream.utils.updateUserProfile
import com.example.livestream.utils.user
import com.google.gson.Gson
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

class UserRepository(
    private val application: Application,
    private val apiService: ApiService,
) {

    private val _user =
        MutableSharedFlow<User?>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val user: SharedFlow<User?> = _user.asSharedFlow()

    init {
        _user.tryEmit(application.user)
    }

    suspend fun getUserProfile(): Flow<Result<UserProfile>> {
        return doNetworkCall { apiService.getUserProfile() }
    }

    fun updateUserProfile(user: User) {
        // Persist-user.
        application.updateUserProfile(user)
        _user.tryEmit(user)
    }

    suspend fun updateUserProfile(
        picFile: File?,
        userProfile: UpdateUserProfile.UserProfile
    ): Flow<Result<User>> {
        val picFilePart = if (picFile != null) {
            MultipartBody.Part.createFormData(
                "profile_pic",
                picFile.name,
                RequestBody.create(MediaType.parse("image/*"), picFile),
            )
        } else {
            null
        }
        val userProfileData =
            RequestBody.create(MediaType.parse("application/json"), Gson().toJson(userProfile))

        return doNetworkCall {
            apiService.updateUserProfile(picFilePart = picFilePart, userProfile = userProfileData)
        }.mapSuccess { data ->
            val user = data.user
            updateUserProfile(user = user)
            user
        }
    }

    fun clearUserProfile() {
        application.updateUserProfile(null)
        _user.tryEmit(null)
    }

    companion object {
        @Volatile
        private var INSTANCE: UserRepository? = null

        fun getInstance(application: Application, apiService: ApiService): UserRepository {
            return INSTANCE ?: synchronized(UserRepository::class.java) {
                INSTANCE ?: UserRepository(application, apiService).also {
                    INSTANCE = it
                }
            }
        }
    }
}
