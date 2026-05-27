package com.beautystock.network

import android.content.Context

object GoogleAuthConfigManager {

    private const val PREFS_NAME = "google_auth_config"
    private const val KEY_WEB_CLIENT_ID = "web_client_id"

    fun getWebClientId(context: Context, fallback: String): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_WEB_CLIENT_ID, fallback)?.trim().orEmpty().ifBlank { fallback }
    }

    fun saveWebClientId(context: Context, webClientId: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_WEB_CLIENT_ID, webClientId.trim())
            .apply()
    }
}