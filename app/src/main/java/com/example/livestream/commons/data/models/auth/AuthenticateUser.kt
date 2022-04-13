package com.example.livestream.commons.data.models.auth

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class AuthenticateUser {

    data class Request(
        @SerializedName("token")
        val token: String,
    )

    data class Response(
        @SerializedName("token")
        val token: String,
        @SerializedName("user")
        val userPrefillData: UserPrefillData,
    )
}

class UserPrefillData(
    val email: String?,
    val name: String?,
    val phone: String?,
): Serializable
