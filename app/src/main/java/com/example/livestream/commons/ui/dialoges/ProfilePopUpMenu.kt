package com.example.livestream.commons.ui.dialoges

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import com.example.livestream.databinding.DialogProfilePopupBinding

class ProfilePopUpMenu(context: Context) : BaseCustomPopupMenu(context) {
    private val binding = DialogProfilePopupBinding.inflate(
        LayoutInflater.from(context),
        null,
        false
    )

    override val view: View get() = binding.root

    override fun setupOnClickListener(listener: (v: View) -> Unit): BaseCustomPopupMenu {
        with(binding) {
            llEditProfile.setOnClickListener { listener(it); dismiss() }
            llViewScheduledStreams.setOnClickListener { listener(it); dismiss() }
            llSignOut.setOnClickListener { listener(it); dismiss() }
        }
        return super.setupOnClickListener(listener)
    }
}
