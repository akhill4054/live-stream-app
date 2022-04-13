package com.example.livestream.commons.ui.dialoges

import android.content.Context
import android.view.View
import android.view.WindowManager
import android.widget.PopupWindow

abstract class BaseCustomPopupMenu(context: Context) {
    private val popupWindow = PopupWindow(context)

    protected abstract val view: View

    private fun setupPopupMenu() {
        popupWindow.run {
            isFocusable = true
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            contentView = view
            elevation = 3F
            setBackgroundDrawable(null)
        }
    }

    open fun setupOnClickListener(listener: (v: View) -> Unit): BaseCustomPopupMenu {
        return this
    }

    open fun show(anchorView: View) {
        setupPopupMenu()
        popupWindow.showAsDropDown(anchorView)
    }

    fun dismiss() {
        popupWindow.dismiss()
    }
}