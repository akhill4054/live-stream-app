package com.example.livestream.commons.data.models.commons

import com.google.gson.annotations.SerializedName

data class Streamer(
    @SerializedName("pic_url")
    val picUrl: String?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("username")
    val username: String,
    @SerializedName("followers_count")
    val followersCount: Int,
)
