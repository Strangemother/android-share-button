package com.sharebutton.app

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Handles API communication
 */
class ApiClient {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Fetch configuration from the API endpoint
     */
    suspend fun fetchConfiguration(apiUrl: String): ConfigResult = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(apiUrl)
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext ConfigResult.Error("HTTP ${response.code}: ${response.message}")
                }

                val body = response.body?.string() ?: ""
                val json = JSONObject(body)

                val name = json.optString("name", "Custom Share")
                val icon = json.optString("icon", "")
                val endpoint = json.optString("endpoint", apiUrl)

                ConfigResult.Success(
                    name = name,
                    icon = icon,
                    endpoint = endpoint
                )
            }
        } catch (e: IOException) {
            ConfigResult.Error("Network error: ${e.message}")
        } catch (e: Exception) {
            ConfigResult.Error("Error: ${e.message}")
        }
    }

    /**
     * Post shared content to the endpoint
     */
    suspend fun postSharedContent(
        endpoint: String,
        content: String,
        contentType: String = "text"
    ): ShareResult = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("content", content)
                put("type", contentType)
                put("timestamp", System.currentTimeMillis())
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = json.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url(endpoint)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext ShareResult.Error("HTTP ${response.code}: ${response.message}")
                }

                ShareResult.Success
            }
        } catch (e: IOException) {
            ShareResult.Error("Network error: ${e.message}")
        } catch (e: Exception) {
            ShareResult.Error("Error: ${e.message}")
        }
    }

    sealed class ConfigResult {
        data class Success(
            val name: String,
            val icon: String,
            val endpoint: String
        ) : ConfigResult()
        data class Error(val message: String) : ConfigResult()
    }

    sealed class ShareResult {
        object Success : ShareResult()
        data class Error(val message: String) : ShareResult()
    }
}
