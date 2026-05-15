package com.example.cartmart.core

import android.content.Context
import com.example.cartmart.network.ApiUser
import com.example.cartmart.network.AuthResponse
import com.google.gson.Gson

class SessionManager(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    val token: String?
        get() = prefs.getString(KEY_TOKEN, null)

    val user: ApiUser?
        get() = prefs.getString(KEY_USER, null)?.let { raw ->
            runCatching { gson.fromJson(raw, ApiUser::class.java) }.getOrNull()
        }

    val isLoggedIn: Boolean
        get() = !token.isNullOrBlank() && user != null

    fun saveSession(response: AuthResponse) {
        prefs.edit()
            .putString(KEY_TOKEN, response.token)
            .putString(KEY_USER, gson.toJson(response.user))
            .apply()
    }

    fun updateUser(user: ApiUser) {
        prefs.edit().putString(KEY_USER, gson.toJson(user)).apply()
    }

    fun clear() {
        prefs.edit().remove(KEY_TOKEN).remove(KEY_USER).apply()
    }

    private companion object {
        const val PREFS_NAME = "cartmart_session"
        const val KEY_TOKEN = "token"
        const val KEY_USER = "user"
    }
}
