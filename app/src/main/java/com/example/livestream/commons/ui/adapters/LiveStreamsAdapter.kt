package com.example.livestream.commons.ui.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.livestream.R
import com.example.livestream.commons.data.models.commons.LiveStream
import com.example.livestream.databinding.ItemLiveStreamBinding
import com.example.livestream.utils.dp
import com.example.livestream.utils.getThumbnailPlaceholder
import com.example.livestream.utils.setProfilePic
import com.example.livestream.utils.toReadableDateTime

class LiveStreamsAdapter(private val interactions: Interactions) :
    ListAdapter<LiveStream, LiveStreamsAdapter.LiveStreamVH>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LiveStreamVH {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemLiveStreamBinding.inflate(
            layoutInflater,
            parent,
            false,
        )
        return LiveStreamVH(binding)
    }

    override fun onBindViewHolder(holder: LiveStreamVH, position: Int) {
        holder.bind(getItem(position))
    }

    inner class LiveStreamVH(
        private val binding: ItemLiveStreamBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        private lateinit var data: LiveStream

        init {
            with(binding) {
                // Click listeners.
                root.setOnClickListener {
                    interactions.onStreamingTapped(data)
                }
            }
        }

        @SuppressLint("SetTextI18n")
        fun bind(data: LiveStream) {
            this.data = data

            with(binding) {
                llLiveIndicator.isInvisible = !data.isLive
                if (!data.isLive) {
                    tvScheduleDateTime.text = root.context.getString(
                        R.string.live_on_s,
                        data.scheduledTimestamp.toReadableDateTime(),
                    )
                }

                tvScheduleDateTime.isVisible = !data.isLive
                ivStreamerProfilePic.borderWidth = if (data.isLive) {
                    0.dp(root.context)
                } else {
                    1.2.dp(root.context)
                }

                Glide.with(ivThumbnail)
                    .load(data.thumbnail)
                    .placeholder(root.context.getThumbnailPlaceholder())
                    .error(root.context.getThumbnailPlaceholder())
                    .into(ivThumbnail)

                tvTitle.text = data.title
                data.streamer.let {
                    ivStreamerProfilePic.setProfilePic(it.picUrl)
                    tvStreamerUsername.text = it.username
                    tvStreamerFollowers.text = "${it.followersCount} followers"
                }

                tvLikesCount.text = data.likes.toString()
                tvViewCount.text = "${data.views} views"
                tvPopularity.text = "${data.popularity ?: 0}%"
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<LiveStream>() {
        override fun areItemsTheSame(
            oldItem: LiveStream,
            newItem: LiveStream
        ): Boolean = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: LiveStream,
            newItem: LiveStream
        ): Boolean = oldItem == newItem
    }

    interface Interactions {
        fun onStreamingTapped(data: LiveStream)
    }
}
