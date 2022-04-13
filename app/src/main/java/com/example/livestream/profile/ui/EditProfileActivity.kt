package com.example.livestream.profile.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import com.bumptech.glide.Glide
import com.example.livestream.R
import com.example.livestream.commons.data.models.auth.UserPrefillData
import com.example.livestream.commons.data.models.commons.User
import com.example.livestream.commons.data.models.user.UpdateUserProfile
import com.example.livestream.commons.data.remote.AppApiClient
import com.example.livestream.commons.repositories.UserRepository
import com.example.livestream.commons.viewmodels.ProfileViewModel
import com.example.livestream.commons.viewmodels.ProfileViewModelFactory
import com.example.livestream.databinding.ActivityEditProfileBinding
import com.example.livestream.utils.buildSnackbarForError
import com.example.livestream.utils.observeResult
import com.example.livestream.utils.setProfilePic
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.net.URI

class EditProfileActivity : AppCompatActivity() {

    private val profileViewModelFactory by lazy {
        val apiService = AppApiClient.getInstance(applicationContext).apiService
        val userRepository = UserRepository.getInstance(application, apiService)
        ProfileViewModelFactory(userRepository)
    }

    private val profileViewModel: ProfileViewModel by lazy {
        ViewModelProvider(this, profileViewModelFactory).get()
    }

    private var isCreateProfileMode = false

    private lateinit var binding: ActivityEditProfileBinding

    private var pickedPicFile: File? = null

    private val prefillData: UserPrefillData? by lazy {
        intent.getSerializableExtra(ARG_USER_PREFILL_DATA) as? UserPrefillData
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        subscribeObservers()
    }

    private fun setupUI() {
        with(binding) {
            prefillData?.let {
                etName.setText(it.name)
                etEmail.setText(it.email)
                etPhoneNumber.setText(it.phone)
            }

            val genderOptions = arrayOf("Male", "Female", "Other")

            // Profile pic.
            binding.ivProfilePic.setOnClickListener { pickProfileImage() }
            binding.btnEditProfilePic.setOnClickListener { pickProfileImage() }

            btnGenderSelection.setOnClickListener {
                MaterialAlertDialogBuilder(this@EditProfileActivity)
                    .setTitle(getString(R.string.select_gender))
                    .setItems(genderOptions) { dialog, which ->
                        btnGenderSelection.setText(genderOptions[which])
                        dialog.dismiss()
                    }
                    .show()
            }

            btnNavigateUp.setOnClickListener { finish() }

            btnSaveProfile.setOnClickListener {
                val filledAge = etAge.text.toString()
                val age =
                    if (filledAge.isNotEmpty()) filledAge.toInt()
                    else null

                val profileData = UpdateUserProfile.UserProfile(
                    name = etName.text.toString(),
                    username = etUsername.text.toString(),
                    email = etEmail.text.toString(),
                    phone = etPhoneNumber.text.toString(),
                    age = age,
                    sex = User.getCodedSex(btnGenderSelection.text.toString()),
                    bio = etBio.text.toString(),
                )

                profileViewModel.updateUserProfile(
                    picFile = pickedPicFile,
                    userProfile = profileData,
                )
            }
        }
    }

    private fun subscribeObservers() {
        profileViewModel.user.observe(this) { user ->
            with(binding) {
                if (user == null) {
                    tvToolbarTitle.text = getString(R.string.create_profile)
                    isCreateProfileMode = true
                } else {
                    tvToolbarTitle.text = getString(R.string.edit_profile)

                    ivProfilePic.setProfilePic(user.picUrl)

                    etName.setText(user.name)
                    etUsername.setText(user.username)
                    etBio.setText(user.bio)
                    etEmail.setText(user.email)
                    etPhoneNumber.setText(user.phone)
                    etAge.setText(user.age.toString())
                    btnGenderSelection.setText(user.readableSex)
                }
            }
        }

        observeResult(
            profileViewModel.profileUpdate,
            onSuccess = {
                if (isCreateProfileMode) {
                    finish()
                } else {
                    Snackbar.make(
                        binding.root,
                        "Profile updated successfully.",
                        Snackbar.LENGTH_SHORT,
                    ).show()
                }
            },
            onLoading = { binding.progressBar.isInvisible = !it },
            onFailure = { e ->
                binding.root.buildSnackbarForError(e.message).show()
            }
        )
    }

    private fun pickProfileImage() {
        ImagePicker.with(this)
            .crop(1F, 1F)
            .maxResultSize(512, 512)
            .start(RC_PROFILE_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_PROFILE_IMAGE && resultCode == RESULT_OK) {
            val uri: Uri = data?.data!!

            pickedPicFile = File(URI(uri.toString()))

            // Set image.
            Glide.with(this)
                .load(uri)
                .into(binding.ivProfilePic)
        }
    }

    companion object {
        private const val RC_PROFILE_IMAGE = 10001

        private const val ARG_USER_PREFILL_DATA = "arg_user_prefill_data"

        fun Context.launchEditProfile(prefillData: UserPrefillData? = null) {
            Intent(this, EditProfileActivity::class.java).run {
                putExtra(ARG_USER_PREFILL_DATA, prefillData)
                startActivity(this)
            }
        }
    }
}
