package com.example.livestream.login.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import com.example.livestream.R
import com.example.livestream.commons.data.models.auth.UserPrefillData
import com.example.livestream.commons.data.remote.AppApiClient
import com.example.livestream.commons.repositories.AuthRepository
import com.example.livestream.commons.repositories.UserRepository
import com.example.livestream.commons.viewmodels.LoginViewModel
import com.example.livestream.commons.viewmodels.LoginViewModelFactory
import com.example.livestream.databinding.ActivityLoginBinding
import com.example.livestream.profile.ui.EditProfileActivity.Companion.launchEditProfile
import com.example.livestream.utils.buildSnackbarForError
import com.example.livestream.utils.observeResult
import com.firebase.ui.auth.AuthMethodPickerLayout
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import timber.log.Timber

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private val loginViewModelFactory by lazy {
        val apiService = AppApiClient.getInstance(applicationContext).apiService
        val userRepository = UserRepository.getInstance(application, apiService)

        LoginViewModelFactory(
            authRepository = AuthRepository.getInstance(
                application,
                userRepository,
                apiService
            ),
            userRepository = userRepository,
        )
    }

    private val loginViewModel: LoginViewModel by lazy {
        ViewModelProvider(this, loginViewModelFactory).get()
    }

    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract()
    ) { res ->
        this.onSignInResult(res)
    }

    private var userPrefillData: UserPrefillData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        subscribeObservers()
    }

    private fun setupUI() {
        launchAuthUI()
    }

    private fun subscribeObservers() {
        observeResult(
            loginViewModel.authVerificationStatus,
            onSuccess = {
                this.userPrefillData = it
                // Check for user-profile.
                loginViewModel.getUserprofile()
            },
            onLoading = { isLoading ->
                if (isLoading) {
                    binding.progress.isVisible = true
                }
            },
            onFailure = { e ->
                handleError(e.message)
            }
        )

        observeResult(
            loginViewModel.userProfile,
            onSuccess = { user ->
                if (user == null) {
                    launchEditProfile(userPrefillData)
                } // else; Sign-in completed.
                finish()
            },
            onLoading = {},
            onFailure = { e ->
                handleError(e.message)
            }
        )
    }

    private fun launchAuthUI() {
        // Choose authentication providers
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.PhoneBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build(),
        )

        val customAuthUiLayout = AuthMethodPickerLayout.Builder(R.layout.layout_firebase_auth_ui)
            .setEmailButtonId(R.id.btnSignInWithEmail)
            .setPhoneButtonId(R.id.btnSignInWithPhone)
            .setGoogleButtonId(R.id.btnSignInWithGoogle)
            .build()

        // Create and launch sign-in intent
        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setTheme(R.style.FirebaseUICustomTheme)
            .setAuthMethodPickerLayout(customAuthUiLayout)
            .build()

        signInLauncher.launch(signInIntent)
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        val response = result.idpResponse

        if (result.resultCode == RESULT_OK) {
            // Successfully signed in.
            val user = FirebaseAuth.getInstance().currentUser

            if (user != null) {
                binding.progress.isVisible = true

                user.getIdToken(true).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Send token to backend.
                        val token = task.result!!.token!!
                        Timber.d("token: $token")

                        loginViewModel.authenticateUser(firebaseAuthToken = token)
                    } else {
                        handleError()
                    }
                }
            } else {
                handleError()
            }
        } else {
            // Sign in failed. If response is null the user canceled the
            // sign-in flow using the back button. Otherwise check
            // response.getError().getErrorCode() and handle the error.
            when {
                response == null -> {
                    // Cancelled by user.
                    finish()
                }
                response.error?.errorCode == ErrorCodes.NO_NETWORK -> {
                    handleError(getString(R.string.error_no_internet))
                }
                else -> {
                    handleError()
                }
            }
        }
    }

    private fun handleError(message: String? = null) {
        binding.progress.isVisible = false
        binding.coolRoot.buildSnackbarForError(
            message ?: getString(R.string.error_typical),
            actionTile = "Retry",
            duration = Snackbar.LENGTH_INDEFINITE,
            actionListener = { launchAuthUI() },
        ).show()
    }

    companion object {

        fun Context.launchLogin() {
            Intent(this, LoginActivity::class.java).run {
                startActivity(this)
            }
        }
    }
}
