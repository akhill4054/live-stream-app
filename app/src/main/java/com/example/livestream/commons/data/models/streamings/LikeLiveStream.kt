package com.example.livestream.commons.data.models.streamings

import com.google.gson.annotations.SerializedName

class LikeLiveStream {

    data class Response(
        @SerializedName("message")
        val message: String,
    )
}
