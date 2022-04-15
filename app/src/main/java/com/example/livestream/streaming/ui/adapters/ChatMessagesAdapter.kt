package com.example.livestream.streaming.ui.adapters

import android.annotation.SuppressLint
import android.text.SpannableString
import android.text.Spanned
import android.text.style.TextAppearanceSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.livestream.R
import com.example.livestream.commons.data.models.streamings.chat.ChatMessage
import com.example.livestream.databinding.ItemLiveStreamChatMessageBinding
import com.example.livestream.utils.setProfilePic

class ChatMessagesAdapter : RecyclerView.Adapter<ChatMessagesAdapter.ChatMessageVH>() {

    private val messages = mutableListOf<ChatMessage>()

    override fun getItemCount(): Int = messages.size

    fun addItem(message: ChatMessage) {
        messages.add(0, message)
        notifyItemInserted(0)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun clear() {
        messages.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatMessageVH {
        val binding = ItemLiveStreamChatMessageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ChatMessageVH(binding)
    }

    override fun onBindViewHolder(holder: ChatMessageVH, position: Int) {
        holder.bind(messages[position])
    }

    inner class ChatMessageVH(
        private val binding: ItemLiveStreamChatMessageBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(data: ChatMessage) {
            with(binding) {
                circleImageView.setProfilePic(data.picUrl)
                tvUsernameMessage.text = SpannableString("${data.username} ${data.message}").apply {
                    setSpan(
                        TextAppearanceSpan(root.context, R.style.TextLiveChatItemUsername),
                        0,
                        data.username.length,
                        Spanned.SPAN_INCLUSIVE_EXCLUSIVE,
                    )
                    setSpan(
                        TextAppearanceSpan(root.context, R.style.TextLiveChatItemMessage),
                        data.username.length,
                        this.length,
                        Spanned.SPAN_INCLUSIVE_EXCLUSIVE,
                    )
                }
            }
        }
    }
}
