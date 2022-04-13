package com.example.livestream.commons.data.models.streamings.chat

data class ChatMessage(
    val uid: String,
    val username: String,
    val name: String,
    val picUrl: String?,
    val message: String,
)
