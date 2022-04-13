package com.example.livestream.commons.data.models.commons

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("uid")
    val uid: String,
    @SerializedName("username")
    val username: String,
    @SerializedName("name")
    val name: String?,
    @SerializedName("pic_url")
    val picUrl: String?,
    @SerializedName("email")
    val email: String,
    @SerializedName("phone")
    val phone: String?,
    @SerializedName("bio")
    val bio: String?,
    @SerializedName("followers_count")
    val followersCount: Int?,
    @SerializedName("age")
    val age: Int,
    @SerializedName("sex")
    val sex: String?,
) {
    val readableSex: String?
        get() = when (sex) {
            "m" -> "Male"
            "f" -> "Female"
            "o" -> "Other"
            else -> null
        }

    companion object {

        fun getCodedSex(sex: String?): String? =
            when (sex) {
                "Male" -> "m"
                "Female" -> "f"
                "Other" -> "o"
                else -> null
            }
    }
}
