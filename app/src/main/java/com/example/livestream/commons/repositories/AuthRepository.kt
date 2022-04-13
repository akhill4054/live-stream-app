package com.example.livestream.commons.repositories

import android.app.Application
import com.example.livestream.commons.data.models.auth.AuthenticateUser
import com.example.livestream.commons.data.models.auth.UserPrefillData
import com.example.livestream.commons.data.models.commons.Result
import com.example.livestream.commons.data.remote.ApiService
import com.example.livestream.utils.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class AuthRepository(
    private val application: Application,
    private val userRepository: UserRepository,
    private val apiService: ApiService,
) {
    private val _isAuthenticated =
        MutableSharedFlow<Boolean>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val isAuthenticated = _isAuthenticated.asSharedFlow()

    init {
        _isAuthenticated.tryEmit(getIsAuthenticated())
    }

    fun getIsAuthenticated(): Boolean = application.authToken != null

    suspend fun authenticateUser(firebaseAuthToken: String): Flow<Result<UserPrefillData>> {
        val requestBody = AuthenticateUser.Request(token = firebaseAuthToken)

        return doNetworkCall { apiService.authenticateUser(requestBody) }
            .mapSuccess { data ->
                val token = data.token
                application.putAuthToken(token)
                _isAuthenticated.tryEmit(token.isNotEmpty())
                data.userPrefillData
            }
    }

    fun signOut() {
        // Sign-out from Firebase.
        FirebaseAuth.getInstance().signOut()
        // Sign-out from Google.
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .build()
        val googleSignInClient = GoogleSignIn.getClient(application, gso)
        googleSignInClient.signOut()
        // Clear user-profile.
        userRepository.clearUserProfile()
        // Delete auth-token.
        application.putAuthToken(null)
    }

    companion object {
        @Volatile
        private var INSTANCE: AuthRepository? = null

        fun getInstance(
            application: Application,
            userRepository: UserRepository,
            apiService: ApiService,
        ): AuthRepository {
            return INSTANCE ?: synchronized(AuthRepository::class.java) {
                INSTANCE ?: AuthRepository(application, userRepository, apiService).also {
                    INSTANCE = it
                }
            }
        }
    }
}
