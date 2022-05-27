package com.example.livestream.streaming.ui

import android.Manifest
import android.animation.Animator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import androidx.lifecycle.lifecycleScope
import com.example.livestream.R
import com.example.livestream.commons.data.models.commons.Result
import com.example.livestream.commons.data.models.commons.User
import com.example.livestream.commons.data.models.streamings.WatchLiveStream
import com.example.livestream.commons.data.models.streamings.chat.ChatMessage
import com.example.livestream.commons.data.remote.AppApiClient
import com.example.livestream.commons.repositories.UserRepository
import com.example.livestream.commons.viewmodels.StreamingVM
import com.example.livestream.commons.viewmodels.StreamingVMF
import com.example.livestream.databinding.ActivityStreamingBinding
import com.example.livestream.login.ui.LoginActivity.Companion.launchLogin
import com.example.livestream.streaming.ui.adapters.ChatMessagesAdapter
import com.example.livestream.utils.*
import com.example.livestream.utils.AnimationUtils.Companion.spin
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import io.agora.rtc.Constants
import io.agora.rtc.IRtcEngineEventHandler
import io.agora.rtc.RtcEngine
import io.agora.rtc.models.ClientRoleOptions
import io.agora.rtc.video.VideoCanvas
import io.agora.rtm.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class StreamingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStreamingBinding

    private lateinit var rtcEngine: RtcEngine

    private val streamingVMF by lazy {
        val apiService = AppApiClient.getInstance(applicationContext).apiService
        StreamingVMF(apiService)
    }

    private val streamingVM: StreamingVM by lazy {
        ViewModelProvider(this, streamingVMF).get()
    }

    private val userRepository by lazy {
        val apiService = AppApiClient.getInstance(applicationContext).apiService
        UserRepository.getInstance(application, apiService)
    }

    private val streamingId: String by lazy { intent.getStringExtra(ARG_STREAMING_ID)!! }

    private var isStreamer = false
    private var rtcUid: Int = 0
    private lateinit var rtcToken: String
    private lateinit var rtcChannelName: String

    private var likesCount = 0
    private var isLiked: Boolean? = null
    private var isDisliked: Boolean? = null

    private val rtcEventHandler: IRtcEngineEventHandler by lazy {
        object : IRtcEngineEventHandler() {

            override fun onUserJoined(uid: Int, elapsed: Int) {
                runOnUiThread {
                    setupRemoteVideo(uid)
                    Timber.d("onUserJoined: uid: $uid")
                }
            }

            override fun onUserOffline(uid: Int, reason: Int) {
                runOnUiThread {
                    binding.streamingProgress.isVisible = true
                    Timber.d("onUserOffline: uid: $uid")
                }
            }
        }
    }

    // RTM
    private val rtmClient by lazy {
        RtmClient.createInstance(
            baseContext,
            getString(R.string.agora_app_id),
            object : RtmClientListener {
                override fun onConnectionStateChanged(p0: Int, p1: Int) {
                }

                override fun onMessageReceived(p0: RtmMessage, p1: String) {
                }

                override fun onImageMessageReceivedFromPeer(p0: RtmImageMessage?, p1: String?) {
                }

                override fun onFileMessageReceivedFromPeer(p0: RtmFileMessage?, p1: String?) {
                }

                override fun onMediaUploadingProgress(p0: RtmMediaOperationProgress?, p1: Long) {
                }

                override fun onMediaDownloadingProgress(p0: RtmMediaOperationProgress?, p1: Long) {
                }

                override fun onTokenExpired() {
                }

                override fun onPeersOnlineStatusChanged(p0: MutableMap<String, Int>?) {
                }
            })
    }

    private var rtmChannel: RtmChannel? = null

    private val gson by lazy { Gson() }

    private lateinit var chatMessagesAdapter: ChatMessagesAdapter

    private var isUserMuted = true
    private var isCameraEnabled = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStreamingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        streamingVM.watchLiveStream(streamingId)
        setupUI()
        subscribeObservers()
    }

    private fun setupUI() {
        // Make activity full-screen.
        if (Build.VERSION.SDK_INT >= 30) {
            window.insetsController?.systemBarsBehavior =
                WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            // Hide both the status bar and the navigation bar
            window.insetsController?.hide(WindowInsetsCompat.Type.systemBars())
        } else {
            // Remember that you should never show the action bar if the
            // status bar is hidden, so hide that too if necessary.
            actionBar?.hide()
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                    or View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }

        with(binding) {
            chatMessagesAdapter = ChatMessagesAdapter()
            binding.rvChatMessages.adapter = chatMessagesAdapter

            flVideoContainer.layoutParams = flVideoContainer.layoutParams.apply {
                val sHeight = Resources.getSystem().displayMetrics.heightPixels
                val sWidth = Resources.getSystem().displayMetrics.widthPixels

                height = if (sWidth > sHeight) {
                    sHeight
                } else {
                    sWidth
                } - 8.dp(this@StreamingActivity)
            }

            btnNavigateUp.setOnClickListener {
                onBackPressed()
            }

            tvBtnShowChatWindow.setOnClickListener {
                showChatWindow()
            }
            btnHideChat.setOnClickListener {
                if (clEnterChatMessage.isVisible) {
                    showEnterChatWindow(false)
                } else {
                    showChatWindow(false)
                }
            }

            // Streamer controls.
            btnCamera.setOnClickListener {
                toggleCamera()
            }
            btnMute.setOnClickListener {
                toggleMute()
            }
            btnSwitchCamera.setOnClickListener {
                switchCamera()
            }
        }
    }

    private fun subscribeObservers() {
        observeResult(
            streamingVM.watchDetails,
            skipDisableLoadingOnSuccess = true,
            onSuccess = { data ->
                handleStreamingDetailsSuccess(data)
            },
            onLoading = { isLoading ->
                binding.streamingProgress.isVisible = isLoading
            },
            onFailure = { e ->
                binding.root.buildSnackbarForError(message = e.message) {
                    streamingVM.watchLiveStream(streamingId)
                }.show()
            }
        )
        lifecycleScope.launchWhenResumed {
            userRepository.user.collect {
                setupUiPostUserUpdate(user)
            }
        }
        observeResult(
            streamingVM.likeLiveStream,
            onSuccess = { data ->
                showShortToast(data.message)
            },
            onFailure = { e ->
                binding.root.buildSnackbarForError(e.message)
            }
        )
    }

    @SuppressLint("SetTextI18n")
    private fun handleStreamingDetailsSuccess(data: WatchLiveStream) {
        with(binding) {
            // Setup UI.
            data.streamingDetails.let {
                llDetails.isVisible = true

                tvTitle.text = it.title
                tvStreamingDesc.text = it.description

                if (it.customTags?.isNotEmpty() == true) {
                    for (tag in it.customTags) {
                        val chip = Chip(cgCustomTags.context)
                        chip.text = tag
                        cgCustomTags.addView(chip)
                    }
                }

                tvLikesCount.text = "${it.likes}"
                tvViewsCount.text = "${it.views} views"
                tvPopularity.text = "${it.popularity ?: 0}%"

                likesCount = it.likes
                isLiked = it.isLiked ?: false
                isDisliked = it.isDisliked ?: false

                setupLikeButtonIcon(isLiked!!, isDisliked!!)

                if (!it.isLive) {
                    tvScheduleDateTime.isVisible = true
                    tvScheduleDateTime.text =
                        "Will be live on ${it.scheduledTimestamp.toReadableDateTime()}"
                }

                if (data.isStreamer) {
                    llDetails.alpha = 0.7F
                    btnCamera.updateMarginInConstraintLayout(bottom = 12)

                    btnEndStreaming.isVisible = true
                    btnEndStreaming.setOnClickListener {
                        endLiveStream()
                    }
                } else {
                    llDetails.updateMarginInConstraintLayout(bottom = 12)
                }
            }

            val rtcCredentials = data.rtcCredentials

            if (rtcCredentials != null) {
                isStreamer = data.isStreamer
                rtcUid = data.rtcCredentials.agUid
                rtcToken = data.rtcCredentials.rtcToken
                rtcChannelName = data.rtcCredentials.channelName

                initializeAndJoinChannel()
                initializeRtm(rtcCredentials.rtmToken, rtcUid.toString(), rtcChannelName)
            }
        }
    }

    private fun showChatWindow(show: Boolean = true) {
        with(binding) {
            tvBtnShowChatWindow.alpha =
                if (show) 1F
                else 0F
            tvBtnShowChatWindow.clearAnimation()
            tvBtnShowChatWindow.animate().apply {
                interpolator = AccelerateInterpolator()
                alpha(if (show) 0F else 1F)
            }.start()

            clStreamingChatWindow.isVisible = true
            clStreamingChatWindow.clearAnimation()

            clStreamingChatWindow.post {
                clStreamingChatWindow.translationX =
                    if (show) clStreamingChatWindow.width.toFloat()
                    else 0F
                clStreamingChatWindow.post {
                    clStreamingChatWindow.animate().apply {
                        interpolator = AccelerateDecelerateInterpolator()
                        translationX(
                            if (show) 0F
                            else clStreamingChatWindow.width.toFloat()
                        )
                    }.start()
                }
            }
        }
    }

    private fun showEnterChatWindow(show: Boolean = true) {
        with(binding) {
            clEnterChatMessage.isVisible = true
            clEnterChatMessage.clearAnimation()

            if (!show) {
                etChatMessage.hideKeyboard()
            }

            clEnterChatMessage.post {
                clEnterChatMessage.translationY =
                    if (show) clStreamingChatWindow.height.toFloat()
                    else 0F
                clEnterChatMessage.animate().apply {
                    translationYBy(
                        if (show) -clEnterChatMessage.translationY
                        else clStreamingChatWindow.height.toFloat()
                    )
                    translationY(
                        if (show) 0F
                        else clStreamingChatWindow.height.toFloat()
                    )
                    interpolator = AccelerateInterpolator()
                    setListener(object : Animator.AnimatorListener {
                        override fun onAnimationStart(animation: Animator?, isReverse: Boolean) {
                        }

                        override fun onAnimationStart(animation: Animator?) {
                        }

                        override fun onAnimationEnd(animation: Animator?, isReverse: Boolean) {
                        }

                        override fun onAnimationEnd(animation: Animator?) {
                            if (!show) {
                                clEnterChatMessage.isVisible = false
                            } else {
                                etChatMessage.showKeyboard()
                            }
                        }

                        override fun onAnimationCancel(animation: Animator?) {
                        }

                        override fun onAnimationRepeat(animation: Animator?) {
                        }
                    })
                }.start()
            }
        }
    }

    private fun setupLikeButtonIcon(isLiked: Boolean, isDisliked: Boolean) {
        with(binding) {
            btnLike.setImageResource(
                if (isLiked) R.drawable.ic_like_filled
                else R.drawable.ic_like_outlined_24
            )
            btnDislike.setImageResource(
                if (isDisliked) R.drawable.ic_dislike_filled_24
                else R.drawable.ic_dislike_outlined_24
            )

            btnLike.colorFilter = PorterDuffColorFilter(
                ContextCompat.getColor(
                    this@StreamingActivity, if (isLiked) {
                        R.color.blue_500
                    } else {
                        R.color.bluey_grey_80
                    }
                ),
                PorterDuff.Mode.SRC_IN
            )
            btnDislike.colorFilter = PorterDuffColorFilter(
                ContextCompat.getColor(
                    this@StreamingActivity, if (isDisliked) {
                        R.color.blue_500
                    } else {
                        R.color.bluey_grey_80
                    }
                ),
                PorterDuff.Mode.SRC_IN
            )
        }
    }

    private fun toggleCamera() {
        isCameraEnabled = !isCameraEnabled
        rtcEngine.enableLocalVideo(isCameraEnabled)

        binding.btnCamera.setImageResource(
            if (isCameraEnabled) R.drawable.ic_baseline_linked_camera_24
            else R.drawable.ic_camera_disabled_24
        )
    }

    private fun toggleMute() {
        isUserMuted = !isUserMuted
        rtcEngine.muteLocalAudioStream(isUserMuted)

        binding.btnMute.setImageResource(
            if (isUserMuted) R.drawable.ic_baseline_mic_off_24
            else R.drawable.ic_baseline_mic_24
        )
    }

    private fun switchCamera() {
        binding.btnSwitchCamera.spin()
        rtcEngine.switchCamera()
    }

    private fun initializeAndJoinChannel() {
        try {
            rtcEngine =
                RtcEngine.create(baseContext, getString(R.string.agora_app_id), rtcEventHandler)
        } catch (e: Exception) {
            Timber.e(e)
            binding.root.showSnackbarForError(e.message)
        }

        rtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING)

        val clientRoleOptions = ClientRoleOptions()
        clientRoleOptions.audienceLatencyLevel = Constants.AUDIENCE_LATENCY_LEVEL_LOW_LATENCY
        rtcEngine.enableVideo()

        // UI.
        binding.gpStreamerControls.isVisible = isStreamer
        binding.streamingProgress.isVisible = !isStreamer

        if (isStreamer) {
            if (!hasPermissions(REQUESTED_PERMISSIONS)) {
                requestDeniedPermissions(REQUESTED_PERMISSIONS, RC_VIDEO_PERMISSION)
                return
            }

            isUserMuted = false
            isCameraEnabled = true

            rtcEngine.setClientRole(Constants.CLIENT_ROLE_BROADCASTER, clientRoleOptions)

            val surfaceView = RtcEngine.CreateRendererView(baseContext)
            surfaceView.setZOrderMediaOverlay(true)
            binding.flVideoContainer.addView(surfaceView)

            rtcEngine.setupLocalVideo(
                VideoCanvas(
                    surfaceView, VideoCanvas.RENDER_MODE_FIT,
                    rtcUid,
                )
            )
        } else {
            rtcEngine.setClientRole(Constants.CLIENT_ROLE_AUDIENCE, clientRoleOptions)
        }

        rtcEngine.joinChannel(rtcToken, rtcChannelName, "", rtcUid)
    }

    private fun initializeRtm(token: String, uid: String, channelName: String) {
        rtmClient.login(token, uid, object : ResultCallback<Void> {
            override fun onSuccess(p0: Void?) {
                runOnUiThread {
                    initializeRtmChannel(channelName)
                }
            }

            override fun onFailure(p0: ErrorInfo?) {
                runOnUiThread {
                    Timber.e(p0?.toString())
                    showShortToast(p0?.errorDescription ?: "Error initializing RTC.")
                }
            }
        })
    }

    private fun initializeRtmChannel(channelName: String) {
        try {
            rtmChannel = rtmClient.createChannel(channelName, object : RtmChannelListener {
                override fun onMemberCountUpdated(p0: Int) {

                }

                override fun onAttributesUpdated(p0: MutableList<RtmChannelAttribute>?) {

                }

                override fun onMessageReceived(p0: RtmMessage, p1: RtmChannelMember) {
                    Timber.d("Received message: ${p0.text}")

                    if (p0.text == "%%%STREAMING_ENDED%%%") {
                        showLongToast("Streaming ended by the host.")
                        setResult(2006, Intent().apply {
                            putExtra("streaming_id", streamingId)
                        })
                        finish()
                        return
                    }
                    val message = gson.fromJson(p0.text, ChatMessage::class.java)
                    addNewChatMessage(message)
                }

                override fun onImageMessageReceived(p0: RtmImageMessage?, p1: RtmChannelMember?) {
                }

                override fun onFileMessageReceived(p0: RtmFileMessage?, p1: RtmChannelMember?) {
                }

                override fun onMemberJoined(p0: RtmChannelMember?) {
                }

                override fun onMemberLeft(p0: RtmChannelMember?) {
                }
            })
        } catch (e: Exception) {
            Timber.e(e)
        }

        rtmChannel!!.join(object : ResultCallback<Void> {
            override fun onSuccess(p0: Void?) {
                runOnUiThread {
                    binding.tvSendChatMessage.alpha = 1F
                    binding.tvSendChatMessage.isEnabled = true
                }
            }

            override fun onFailure(p0: ErrorInfo?) {
                Timber.e(p0?.errorDescription)
            }
        })
    }

    private fun setupUiPostUserUpdate(user: User?) {
        with(binding) {
            if (user != null) {
                civProfilePic.setProfilePic(user.picUrl)

                etChatMessage.addTextChangedListener(afterTextChanged = { text ->
                    if (text.isNullOrEmpty()) {
                        btnSendChatMessage.isEnabled = false
                        btnSendChatMessage.alpha = 0.7F
                    } else {
                        btnSendChatMessage.isEnabled = true
                        btnSendChatMessage.alpha = 1F
                    }
                })

                btnSendChatMessage.isEnabled = false

                btnSendChatMessage.setOnClickListener {
                    sendChatMessage(user)
                }

                tvSendChatMessage.setOnClickListener {
                    showEnterChatWindow()
                }

                btnLike.setOnClickListener {
                    if (isLiked == null) return@setOnClickListener

                    isLiked = !isLiked!!

                    if (isLiked == false) likesCount--
                    else likesCount++

                    if (isDisliked!!) isDisliked = false
                    setupLikeButtonIcon(isLiked!!, isDisliked!!)

                    streamingVM.likeLiveStream(streamingId)
                    tvLikesCount.text = "$likesCount"
                }
                btnDislike.setOnClickListener {
                    if (isLiked == null) return@setOnClickListener
                    isDisliked = !isDisliked!!

                    if (isDisliked == true && isLiked == true) likesCount--

                    if (isLiked!!) isLiked = false
                    setupLikeButtonIcon(isLiked!!, isDisliked!!)

                    streamingVM.dislikeLiveStream(streamingId)
                    tvLikesCount.text = "$likesCount"
                }
            } else {
                tvSendChatMessage.setOnClickListener {
                    launchLogin()
                }
                btnLike.setOnClickListener {
                    launchLogin()
                }
                btnDislike.setOnClickListener {
                    launchLogin()
                }
            }
        }
    }

    private fun addNewChatMessage(message: ChatMessage) {
        with(binding) {
            tvNoMessages.isVisible = false
            chatMessagesAdapter.addItem(message)
            rvChatMessages.scrollToPosition(0)
        }
    }

    private fun sendChatMessage(user: User) {
        with(binding) {
            val rtmMessage = rtmClient.createMessage()

            val message = etChatMessage.text.toString()

            val chatMessage = ChatMessage(
                uid = user.uid,
                username = user.username,
                name = user.name!!,
                picUrl = user.picUrl,
                message = message,
            )

            rtmMessage.text = gson.toJson(chatMessage)

            showSendMessageProgress()

            rtmChannel?.sendMessage(rtmMessage, object : ResultCallback<Void> {
                override fun onSuccess(p0: Void?) {
                    runOnUiThread {
                        etChatMessage.text = null
                        showSendMessageProgress(false)
                        showEnterChatWindow(false)
                        binding.etChatMessage.hideKeyboard()
                        addNewChatMessage(chatMessage)
                    }
                }

                override fun onFailure(p0: ErrorInfo?) {
                    runOnUiThread {
                        Timber.e(p0?.errorDescription)
                        showShortToast("Something went wrong, couldn't send the message.")
                        binding.etChatMessage.hideKeyboard()
                        showSendMessageProgress(false)
                    }
                }
            }) ?: {
                showShortToast("Not connected to RTM server.")
                showSendMessageProgress(false)
            }
        }
    }

    private fun endLiveStream() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Confirm Action")
            .setMessage("Are you sure you want to end the streaming?")
            .setPositiveButton("End") { dialog, _ ->
                // End streaming.
                lifecycleScope.launch(Dispatchers.IO) {
                    doNetworkCall {
                        AppApiClient.getInstance(applicationContext).apiService.endLiveStream(streamingId)
                    }.collect {
                        withContext(Dispatchers.Main) {
                            when(it) {
                                is Result.Success -> {
                                    showShortToast("Live stream deleted.")

                                    val rtmMessage = rtmClient.createMessage()
                                    rtmMessage.text = "%%%STREAMING_ENDED%%%"

                                    rtmChannel?.sendMessage(rtmMessage, object : ResultCallback<Void> {
                                        override fun onSuccess(p0: Void?) {
                                            runOnUiThread {
                                                showShortToast("Live stream ended.")
                                                setResult(2006, Intent().apply {
                                                    putExtra("streaming_id", streamingId)
                                                })
                                            }
                                            finish()
                                        }

                                        override fun onFailure(p0: ErrorInfo?) {
                                            runOnUiThread {
                                                showShortToast("Something went wrong, couldn't end the streaming.")
                                                binding.streamingProgress.isVisible = false
                                            }
                                        }
                                    }) ?: kotlin.run {
                                        showShortToast("Something went wrong, couldn't end the streaming. Code: 1161")
                                        finish()
                                    }
                                }
                                is Result.Loading -> {
                                    binding.streamingProgress.isVisible = true
                                }
                                is Result.Failure -> {
                                    binding.streamingProgress.isVisible = false
                                    binding.root.buildSnackbarForError(message = it.e.message)
                                        .show()
                                }
                            }
                        }
                    }
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showSendMessageProgress(isLoading: Boolean = true) {
        with(binding) {
            progressSendMessage.isVisible = isLoading
            btnSendChatMessage.isInvisible = isLoading
        }
    }

    private fun setupRemoteVideo(uid: Int) {
        val surfaceView = RtcEngine.CreateRendererView(baseContext)
        surfaceView.setZOrderMediaOverlay(true)
        binding.flVideoContainer.addView(surfaceView)

        binding.streamingProgress.isVisible = false

        rtcEngine.setupRemoteVideo(
            VideoCanvas(
                surfaceView, VideoCanvas.RENDER_MODE_FIT,
                uid,
            )
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == RC_VIDEO_PERMISSION) {
            if (hasPermissions(REQUESTED_PERMISSIONS)) {
                initializeAndJoinChannel()
            } else {
                Snackbar.make(
                    binding.root,
                    "Audio and camera permissions are needed in order to join/start the streaming.",
                    Snackbar.LENGTH_INDEFINITE
                ).apply {
                    setAction("Grant permissions") {
                        requestDeniedPermissions(REQUESTED_PERMISSIONS, RC_VIDEO_PERMISSION)
                    }
                }.show()
            }
        }
    }

    override fun onBackPressed() {
        if (binding.clEnterChatMessage.isVisible) {
            showEnterChatWindow(false)
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        try {
            rtcEngine.leaveChannel()
            RtcEngine.destroy()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        private const val RC_VIDEO_PERMISSION = 101

        private const val ARG_STREAMING_ID = "arg_streaming_id"

        private val REQUESTED_PERMISSIONS = arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA
        )

        fun Context.launchStreaming(streamingId: String) {
            Intent(this, StreamingActivity::class.java).run {
                putExtra(ARG_STREAMING_ID, streamingId)
                startActivity(this)
            }
        }
    }
}
