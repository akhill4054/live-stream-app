package com.example.livestream.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import com.example.livestream.commons.data.models.commons.User
import com.google.gson.Gson

private val prefGson by lazy { Gson() }

private var SHARED_PREF_INSTANCE: SharedPreferences? = null

/** Get thread-safe single instance of shared-pref. */
val Context.sharedPref: SharedPreferences get() = SHARED_PREF_INSTANCE ?: synchronized(this@sharedPref) {
        return SHARED_PREF_INSTANCE ?: this.applicationContext.getSharedPreferences(
            "${this.packageName}.SHARED_PREFS_FILE", Context.MODE_PRIVATE
        ).also {
            SHARED_PREF_INSTANCE = it
        }
    }

fun SharedPreferences.putString(key: String, value: String) {
    edit().putString(key, value).apply()
}

@SuppressLint("ApplySharedPref")
fun SharedPreferences.commitString(key: String, value: String?) {
    edit().putString(key, value).commit()
}

private const val PREF_KEY_USER_PROFILE = "pref_key_user_profile"
private const val PREF_KEY_AUTH_TOKEN = "pref_key_auth_token"

val Context.user: User? get() = prefGson.fromJson(
        sharedPref.getString(PREF_KEY_USER_PROFILE, null),
        User::class.java
    )

fun Context.requireUserProfile(): User =
    user ?: throw IllegalStateException("User profile is not available.")

fun Context.updateUserProfile(user: User?) {
    val userProfileJson: String = prefGson.toJson(user)
    sharedPref.putString(PREF_KEY_USER_PROFILE, userProfileJson)
}

val Context.authToken: String? get() = sharedPref.getString(PREF_KEY_AUTH_TOKEN, "")

fun Context.putAuthToken(token: String?) {
    sharedPref.commitString(PREF_KEY_AUTH_TOKEN, token)
}
