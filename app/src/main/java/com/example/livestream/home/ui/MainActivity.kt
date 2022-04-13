package com.example.livestream.home.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import com.bumptech.glide.Glide
import com.example.livestream.R
import com.example.livestream.commons.data.models.commons.LiveStream
import com.example.livestream.commons.data.models.commons.User
import com.example.livestream.commons.data.remote.AppApiClient
import com.example.livestream.commons.repositories.AuthRepository
import com.example.livestream.commons.repositories.LiveStreamRepository
import com.example.livestream.commons.repositories.MainRepository
import com.example.livestream.commons.repositories.UserRepository
import com.example.livestream.commons.ui.adapters.LiveStreamsAdapter
import com.example.livestream.commons.ui.dialoges.ProfilePopUpMenu
import com.example.livestream.commons.viewmodels.HomeViewModel
import com.example.livestream.commons.viewmodels.HomeViewModelFactory
import com.example.livestream.databinding.ActivityMainBinding
import com.example.livestream.login.ui.LoginActivity.Companion.launchLogin
import com.example.livestream.profile.ui.EditProfileActivity
import com.example.livestream.schedule.ui.ScheduleLiveStreamActivity.Companion.launchScheduleLiveStream
import com.example.livestream.streaming.ui.StreamingActivity.Companion.launchStreaming
import com.example.livestream.utils.buildSnackbarForError
import com.example.livestream.utils.observeResult
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity(), LiveStreamsAdapter.Interactions {

    private lateinit var binding: ActivityMainBinding

    private lateinit var recommendedStreamsAdapter: LiveStreamsAdapter

    private val homeViewModelFactory: HomeViewModelFactory by lazy {
        val apiService = AppApiClient.getInstance(applicationContext).apiService

        val mainRepository = MainRepository.getInstance(apiService)
        val userRepository = UserRepository.getInstance(application, apiService)
        val streamingsRepository = LiveStreamRepository.getInstance(apiService)
        val authRepository = AuthRepository.getInstance(
            application,
            userRepository,
            apiService
        )

        HomeViewModelFactory(
            mainRepository = mainRepository,
            authRepository = authRepository,
            userRepository = userRepository,
            streamingsRepository = streamingsRepository,
        )
    }

    private val homeViewModel: HomeViewModel by lazy {
        ViewModelProvider(this, homeViewModelFactory).get()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_LiveStream)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        subscribeObservers()
    }

    private fun setupUI() {
        with(binding) {
            recommendedStreamsAdapter = LiveStreamsAdapter(this@MainActivity)
            rvLiveStreams.adapter = recommendedStreamsAdapter

            srfLiveStreams.setOnRefreshListener {
                homeViewModel.getRecommendedLiveStreams()
            }
        }
    }

    private fun subscribeObservers() {
        homeViewModel.user.observe(this) { user ->
            setupProfileMenu(user)
        }
        observeResult(
            homeViewModel.recommendedLiveStreams,
            onSuccess = { data ->
                binding.gpEmptySearchResult.isVisible = data.isEmpty()
                recommendedStreamsAdapter.submitList(data)
            },
            onFailure = { e ->
                binding.root.buildSnackbarForError(
                    message = e.message,
                    duration = Snackbar.LENGTH_INDEFINITE,
                    actionTile = "Retry",
                ) {
                    homeViewModel.getRecommendedLiveStreams()
                }.show()
            },
            onLoading = { isLoading ->
                if (isLoading) {
                    binding.gpEmptySearchResult.isVisible = false
                } else {
                    binding.srfLiveStreams.isRefreshing = false
                    binding.progressBar.isVisible = false
                }
                if (recommendedStreamsAdapter.itemCount == 0) {
                    binding.progressBar.isVisible = isLoading
                }
            },
        )
    }

    private fun setupProfileMenu(user: User?) {
        with(binding) {
            if (user != null) {
                user.picUrl?.let {
                    Glide.with(iBtnProfile)
                        .load(it)
                        .placeholder(R.drawable.ic_profile_placeholder)
                        .error(R.drawable.ic_profile_placeholder)
                        .into(iBtnProfile)
                }

                iBtnProfile.setOnClickListener {
                    ProfilePopUpMenu(this@MainActivity)
                        .setupOnClickListener { v ->
                            when (v.id) {
                                R.id.llEditProfile -> {
                                    Intent(this@MainActivity, EditProfileActivity::class.java).run {
                                        startActivity(this)
                                    }
                                }
                                // TODO: Implement this screen.
//                        R.id.llViewScheduledStreams -> {}
                                R.id.llSignOut -> {
                                    homeViewModel.signOut()
                                }
                            }
                        }.show(it)
                }

                btnScheduleLiveStream.isVisible = true
                btnGoLive.isVisible = true

                btnGoLive.setOnClickListener {
                    launchScheduleLiveStream(isDirectLiveMode = true)
                }
                btnScheduleLiveStream.setOnClickListener {
                    launchScheduleLiveStream()
                }
            } else {
                btnScheduleLiveStream.isGone = true
                btnGoLive.isGone = true

                iBtnProfile.setImageResource(R.drawable.ic_profile_placeholder)
                iBtnProfile.setOnClickListener {
                    launchLogin()
                }
            }
        }
    }

    override fun onStreamingTapped(data: LiveStream) {
        launchStreaming(data.id)
    }
}
