package com.sharebutton.app

import android.content.Context
import android.content.SharedPreferences

/**
 * Manages the configuration for the share button
 */
class ConfigManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "share_button_config"
        private const val KEY_API_URL = "api_url"
        private const val KEY_SHARE_NAME = "share_name"
        private const val KEY_ICON_URL = "icon_url"
        private const val KEY_POST_ENDPOINT = "post_endpoint"
    }

    var apiUrl: String?
        get() = prefs.getString(KEY_API_URL, null)
        set(value) = prefs.edit().putString(KEY_API_URL, value).apply()

    var shareName: String?
        get() = prefs.getString(KEY_SHARE_NAME, null)
        set(value) = prefs.edit().putString(KEY_SHARE_NAME, value).apply()

    var iconUrl: String?
        get() = prefs.getString(KEY_ICON_URL, null)
        set(value) = prefs.edit().putString(KEY_ICON_URL, value).apply()

    var postEndpoint: String?
        get() = prefs.getString(KEY_POST_ENDPOINT, null)
        set(value) = prefs.edit().putString(KEY_POST_ENDPOINT, value).apply()

    fun isConfigured(): Boolean {
        return !postEndpoint.isNullOrEmpty()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}
