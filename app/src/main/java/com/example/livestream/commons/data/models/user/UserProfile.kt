package com.example.livestream.commons.data.models.user

import com.example.livestream.commons.data.models.commons.User
import com.google.gson.annotations.SerializedName

data class UserProfile(
    @SerializedName("user_profile")
    val user: User?,
)
