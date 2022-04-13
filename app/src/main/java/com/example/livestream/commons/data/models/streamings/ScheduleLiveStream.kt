package com.example.livestream.commons.data.models.streamings

import com.example.livestream.commons.data.models.commons.LiveStream
import com.google.gson.annotations.SerializedName

class ScheduleLiveStream {

    class StreamingDetails(
        @SerializedName("title")
        val title: String,
        @SerializedName("desc")
        val description: String,
        @SerializedName("tags")
        val tags: List<Int> = emptyList(),
        @SerializedName("custom_tags")
        val customTags: String? = null,
        @SerializedName("scheduled_timestamp")
        val scheduledTimestamp: Long? = null,
    )

    data class Response(
        @SerializedName("message")
        val message: String,
        @SerializedName("streaming_details")
        val streamingDetails: LiveStream?,
    )
}
