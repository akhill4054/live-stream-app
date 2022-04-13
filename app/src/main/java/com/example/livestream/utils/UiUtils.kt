package com.example.livestream.utils

import android.animation.TimeInterpolator
import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.livestream.R
import com.google.android.material.snackbar.Snackbar

/**
 * @param top,
 * @param bottom,
 * @param left,
 * @param right, margin values in dp.
 * */
fun View.updateMarginInConstraintLayout(
    top: Number? = null,
    bottom: Number? = null,
    left: Number? = null,
    right: Number? = null,
) {
    layoutParams = (layoutParams as ConstraintLayout.LayoutParams).apply {
        setMargins(
            left?.dp(context) ?: leftMargin,
            top?.dp(context) ?: topMargin,
            right?.dp(context) ?: rightMargin,
            bottom?.dp(context) ?: bottomMargin,
        )
    }
}

// Provides pixel value for of numbers specified as dp.
fun Number.dp(context: Context): Int {
    return context.dpToPixels(this)
}

fun Context.dpToPixels(value: Number): Int {
    val r = this.resources
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        value.toFloat(),
        r.displayMetrics
    ).toInt()
}

fun Activity.getStatusBarHeight(): Int {
    val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
    return if (resourceId > 0) resources.getDimensionPixelSize(resourceId)
    else Rect().apply { window.decorView.getWindowVisibleDisplayFrame(this) }.top
}

private var thumbnailPlaceholder: Drawable? = null

fun Context.getThumbnailPlaceholder(): Drawable {
    return thumbnailPlaceholder ?: ColorDrawable(
        ContextCompat.getColor(
            this, R.color.secondary_white_3,
        )
    ).also { thumbnailPlaceholder = it }
}


fun View.buildSnackbarForError(
    message: String? = null,
    duration: Int = Snackbar.LENGTH_LONG,
    actionTile: String? = null,
    actionListener: (() -> Unit)? = null,
): Snackbar {
    return Snackbar.make(
        this,
        message ?: context.getString(R.string.error_typical),
        duration,
    ).apply {
        view.setBackgroundColor(ContextCompat.getColor(context, R.color.red_700))
        actionTile?.let {
            setAction(actionTile) {
                actionListener?.let { it() }
            }
        }
    }
}


fun View.showSnackbarForError(message: String? = null) {
    buildSnackbarForError(message).show()
}


fun ImageView.setProfilePic(picUrl: String?) {
    picUrl?.let {
        Glide.with(this)
            .load(picUrl)
            .error(R.drawable.ic_profile_placeholder)
            .placeholder(R.drawable.ic_profile_placeholder)
            .into(this)
    }
}


fun Context.showShortToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun Context.showLongToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

fun EditText.showKeyboard() {
    requestFocus()
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(this, InputMethodManager.SHOW_FORCED)
    setSelection(text.length)
}

fun EditText.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(this.windowToken, 0)
}


class AnimationUtils {
    companion object {
        const val ANIM_SPEED_QUICK = 400L
        const val ANIM_SPEED_IDEAL = 700L

        fun View.spin(
            duration: Long = ANIM_SPEED_IDEAL,
            interpolator: TimeInterpolator? = null
        ) {
            animate().apply {
                rotation(0F)
                rotationBy(360F)
                interpolator?.let {
                    this.interpolator = interpolator
                }
                this.duration = duration
            }.start()
        }
    }
}
