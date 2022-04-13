package com.example.livestream.commons.data.models.streamings

import com.example.livestream.commons.data.models.commons.LiveStream
import com.google.gson.annotations.SerializedName

class WatchLiveStream(
    @SerializedName("is_streamer")
    val isStreamer: Boolean,
    @SerializedName("streaming_details")
    val streamingDetails: LiveStream,
    @SerializedName("ag_creds")
    val rtcCredentials: RtcCredentials?,
) {

    data class RtcCredentials(
        @SerializedName("ag_uid")
        val agUid: Int,
        @SerializedName("channel_name")
        val channelName: String,
        @SerializedName("rtc_token")
        val rtcToken: String,
        @SerializedName("rtm_token")
        val rtmToken: String,
    )
}
