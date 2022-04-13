package com.example.livestream.schedule.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import com.bumptech.glide.Glide
import com.example.livestream.R
import com.example.livestream.commons.data.models.streamings.ScheduleLiveStream
import com.example.livestream.commons.data.remote.AppApiClient
import com.example.livestream.commons.repositories.LiveStreamRepository
import com.example.livestream.commons.viewmodels.ScheduleLiveStreamVM
import com.example.livestream.commons.viewmodels.ScheduleLiveStreamVMF
import com.example.livestream.databinding.ActivityScheduleLiveStreamBinding
import com.example.livestream.streaming.ui.StreamingActivity.Companion.launchStreaming
import com.example.livestream.utils.*
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.io.File
import java.net.URI

class ScheduleLiveStreamActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScheduleLiveStreamBinding

    private val scheduleLiveStreamVMF by lazy {
        val apiService = AppApiClient.getInstance(applicationContext).apiService
        ScheduleLiveStreamVMF(
            liveStreamRepository = LiveStreamRepository.getInstance(apiService),
        )
    }

    private val scheduleLiveStreamVM: ScheduleLiveStreamVM by lazy {
        ViewModelProvider(this, scheduleLiveStreamVMF).get()
    }

    private val isDirectLiveMode: Boolean by lazy {
        intent.getBooleanExtra(ARG_IS_DIRECT_LIVE_MODE, false)
    }

    private var pickedThumbnailFile: File? = null
    private var pickedScheduleTimestampInUTC: Long? = null

    private var isEditMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityScheduleLiveStreamBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        subscribeObservers()
    }

    private fun setupUI() {
        with(binding) {
            if (isDirectLiveMode) {
                tvToolbarTitle.setText(R.string.enter_live_stream_details)
                btnScheduleLiveStream.setText(R.string.start_live_stream)
                btnScheduleLiveStream.setIconResource(R.drawable.ic_go_live_24)

                tvScheduleTime.isVisible = false
                etDateTimePicker.isVisible = false
            } else {
                tvToolbarTitle.setText(R.string.schedule_live_stream)
                btnScheduleLiveStream.setText(R.string.schedule_live_stream)
                btnScheduleLiveStream.setIconResource(R.drawable.ic_schedule_24)
            }

            // Click listeners.
            btnNavigateUp.setOnClickListener {
                finish()
            }
            btnSelectImage.setOnClickListener {
                ImagePicker.with(this@ScheduleLiveStreamActivity)
                    .crop(18F, 9F)
                    .maxResultSize(1024, 1024)
                    .start(RC_THUMBNAIL_IMAGE)
            }
            etDateTimePicker.setOnClickListener {
                pickScheduleDateTime()
            }
            btnScheduleLiveStream.setOnClickListener {
                scheduledLiveStream()
            }
        }
    }

    private fun subscribeObservers() {
        observeResult(
            scheduleLiveStreamVM.scheduleStatus,
            onSuccess = { data ->
                if (data.streamingDetails != null) {
                    launchStreaming(streamingId = data.streamingDetails.id)
                } else {
                    showLongToast(data.message)
                }
                finish()
            },
            onLoading = { isLoading ->
                binding.progressBar.isInvisible = !isLoading
            },
            onFailure = { e ->
                binding.root.buildSnackbarForError(e.message).show()
            }
        )
    }

    private fun pickScheduleDateTime() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Pick a date")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        datePicker.addOnPositiveButtonClickListener { unixDate ->
            datePicker.dismissAllowingStateLoss()
            val timePicker =
                MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_12H)
                    .setHour(12)
                    .setMinute(10)
                    .setTitleText("Select stream start time")
                    .build()

            timePicker.addOnPositiveButtonClickListener {
                val unixTimestamp =
                    (unixDate / 1000) + (timePicker.hour * 60 * 60) + (timePicker.minute * 60)
                val unixTimestampInUTC = unixTimestamp - offsetFromUTCInSeconds

                // Update UI.
                binding.etDateTimePicker.setText(unixTimestampInUTC.toReadableDateTime())
                timePicker.dismissAllowingStateLoss()
                // Set data.
                pickedScheduleTimestampInUTC = unixTimestampInUTC
            }

            timePicker.show(supportFragmentManager, "MaterialTimePicker")
        }

        datePicker.show(supportFragmentManager, "MaterialDatePicker")
    }

    private fun scheduledLiveStream() {
        with(binding) {
            val title = etTitle.text.toString()
            val description = etDesc.text.toString()
            val customTags = etCustomTags.text.toString()

            when {
                !isEditMode && pickedThumbnailFile == null -> {
                    binding.root.buildSnackbarForError(
                        message = "Please pick an image for the thumbnail."
                    ).show()
                }
                title.isEmpty() -> {
                    etTitle.error = "Please enter a title"
                    etTitle.showKeyboard()
                }
                description.isEmpty() -> {
                    etDesc.error = "Please fill-in the description."
                    etDesc.showKeyboard()
                }
                !isDirectLiveMode && pickedScheduleTimestampInUTC == null -> {
                    binding.root.buildSnackbarForError(
                        "Please select a date time to schedule your live stream."
                    ).show()
                }
                else -> {
                    val filledDetails = ScheduleLiveStream.StreamingDetails(
                        title = title,
                        description = description,
                        scheduledTimestamp = pickedScheduleTimestampInUTC,
                        customTags = customTags,
                    )

                    if (isDirectLiveMode) {
                        scheduleLiveStreamVM.startLiveStream(
                            thumbnailFile = pickedThumbnailFile!!,
                            details = filledDetails,
                        )
                    } else {
                        scheduleLiveStreamVM.scheduleLiveStream(
                            thumbnailFile = pickedThumbnailFile!!,
                            details = filledDetails,
                        )
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_THUMBNAIL_IMAGE && resultCode == RESULT_OK) {
            val uri: Uri = data?.data!!

            pickedThumbnailFile = File(URI(uri.toString()))

            // Set image.
            Glide.with(this)
                .load(uri)
                .into(binding.ivThumbnail)

            binding.tvPickAThumbnailMessage.isGone = true
            binding.ivDummyThumbnail.isGone = true
        }
    }

    companion object {
        private const val RC_THUMBNAIL_IMAGE = 101

        private const val ARG_IS_DIRECT_LIVE_MODE = "arg_is_direct_live_model"
        private const val ARG_SCHEDULED_LIVE_STREAM = "arg_scheduled_live_stream"

        fun Context.launchScheduleLiveStream(isDirectLiveMode: Boolean = false) {
            Intent(this, ScheduleLiveStreamActivity::class.java).run {
                putExtra(ARG_IS_DIRECT_LIVE_MODE, isDirectLiveMode)
                startActivity(this)
            }
        }
    }
}
