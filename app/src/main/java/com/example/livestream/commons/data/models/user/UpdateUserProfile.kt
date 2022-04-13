package com.example.livestream.commons.data.models.user

import com.example.livestream.commons.data.models.commons.User
import com.google.gson.annotations.SerializedName

class UpdateUserProfile {

    data class Request(
        @SerializedName("user_profile")
        val userProfile: UserProfile,
    )

    data class UserProfile(
        @SerializedName("name")
        val name: String?,
        @SerializedName("username")
        val username: String?,
        @SerializedName("email")
        val email: String?,
        @SerializedName("phone")
        val phone: String?,
        @SerializedName("age")
        val age: Int?,
        @SerializedName("sex")
        val sex: String?,
        @SerializedName("bio")
        val bio: String?,
    )

    data class Response(
        @SerializedName("user_profile")
        val user: User,
    )
}
