package com.example.livestream.commons.data.models.commons

import com.google.gson.annotations.SerializedName

data class LiveStream(
    @SerializedName("id")
    val id: String,
    @SerializedName("thumbnail")
    val thumbnail: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("desc")
    val description: String,
    @SerializedName("likes")
    val likes: Int,
    @SerializedName("views")
    val views: Int,
    @SerializedName("dislikes")
    val dislikes: Int,
    @SerializedName("is_live")
    val isLive: Boolean,
    @SerializedName("custom_tags")
    val customTags: List<String>?,
    @SerializedName("tags")
    val tags: List<String>?,
    @SerializedName("joined_pople")
    val joinedPeopleCount: Int,
    @SerializedName("popularity")
    val popularity: Int?,
    @SerializedName("is_liked")
    val isLiked: Boolean?,
    @SerializedName("is_disliked")
    val isDisliked: Boolean?,
    @SerializedName("scheduled_datetime")
    val scheduledTimestamp: Long,
    @SerializedName("streamer")
    val streamer: Streamer,
)
