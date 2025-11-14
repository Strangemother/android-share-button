package me.talofa.app

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
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
    companion object {
        private const val APP_VERSION = "1.0.2"
        private const val USER_AGENT = "talofa.me/$APP_VERSION (Android)"
        
        /**
         * Normalize a URL by adding a protocol if missing
         */
        private fun normalizeUrl(url: String, protocol: String = "https"): String {
            val trimmed = url.trim()
            return when {
                trimmed.startsWith("http://", ignoreCase = true) -> trimmed
                trimmed.startsWith("https://", ignoreCase = true) -> trimmed
                else -> "$protocol://$trimmed"
            }
        }
    }
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()

    /**
     * Fetch configuration from the API endpoint.
     * If no protocol is specified, tries HTTPS first, then HTTP.
     */
    suspend fun fetchConfiguration(apiUrl: String, apiKey: String? = null): ConfigResult = withContext(Dispatchers.IO) {
        val hasProtocol = apiUrl.trim().startsWith("http://", ignoreCase = true) || 
                         apiUrl.trim().startsWith("https://", ignoreCase = true)
        
        if (hasProtocol) {
            // If protocol is explicitly provided, use it directly
            return@withContext tryFetchConfiguration(apiUrl, apiKey)
        } else {
            // No protocol specified - try HTTPS first, then HTTP
            val httpsUrl = normalizeUrl(apiUrl, "https")
            val httpsResult = tryFetchConfiguration(httpsUrl, apiKey)
            
            if (httpsResult is ConfigResult.Success) {
                return@withContext httpsResult
            }
            
            // HTTPS failed, try HTTP
            val httpUrl = normalizeUrl(apiUrl, "http")
            return@withContext tryFetchConfiguration(httpUrl, apiKey)
        }
    }
    
    /**
     * Attempt to fetch configuration from a specific URL
     */
    private suspend fun tryFetchConfiguration(apiUrl: String, apiKey: String?): ConfigResult = withContext(Dispatchers.IO) {
        try {
            val requestBuilder = Request.Builder()
                .url(apiUrl)
                .header("User-Agent", USER_AGENT)

            if (!apiKey.isNullOrEmpty()) {
                requestBuilder.header("X-API-Key", apiKey)
            }

            val request = requestBuilder.get().build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext ConfigResult.Error("HTTP ${response.code}: ${response.message}")
                }

                val body = response.body?.string() ?: ""
                val json = JSONObject(body)

                val name = json.optString("name", "Custom Share")
                val icon = json.optString("icon", "")
                val endpoint = json.optString("endpoint", apiUrl)
                val deliveryKey = json.optString("delivery_key", "")

                ConfigResult.Success(
                    name = name,
                    icon = icon,
                    endpoint = endpoint,
                    deliveryKey = deliveryKey,
                    configUrl = apiUrl  // Return the URL that successfully worked
                )
            }
        } catch (e: IOException) {
            ConfigResult.Error("Network error: ${e.message}")
        } catch (e: Exception) {
            ConfigResult.Error("Error: ${e.message}")
        }
    }

    /**
     * Post group selection for an existing share
     */
    suspend fun postGroupSelection(
        endpoint: String,
        shareId: String,
        groupId: String?,
        groupName: String?,
        deliveryKey: String? = null
    ): ShareResult = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("share_id", shareId)
                
                // If groupId is null, this is a new group - send group_name instead
                if (groupId != null) {
                    put("group_id", groupId)
                } else if (groupName != null) {
                    put("group_name", groupName)
                    put("group_id", JSONObject.NULL)
                }
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = json.toString().toRequestBody(mediaType)

            val requestBuilder = Request.Builder()
                .url(endpoint)
                .header("User-Agent", USER_AGENT)

            if (!deliveryKey.isNullOrEmpty()) {
                requestBuilder.header("X-Delivery-Key", deliveryKey)
            }

            val request = requestBuilder.post(requestBody).build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    ShareResult.Success
                } else {
                    ShareResult.Error("HTTP ${response.code}: ${response.message}")
                }
            }
        } catch (e: IOException) {
            ShareResult.Error("Network error: ${e.message}")
        } catch (e: Exception) {
            ShareResult.Error("Error: ${e.message}")
        }
    }

    /**
     * Post image content using multipart form data
     */
    suspend fun postImageContent(
        endpoint: String,
        imageBytes: ByteArray,
        fileName: String,
        mimeType: String,
        text: String? = null,
        title: String? = null,
        subject: String? = null,
        deliveryKey: String? = null
    ): ShareResult = withContext(Dispatchers.IO) {
        try {
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "image",
                    fileName,
                    imageBytes.toRequestBody(mimeType.toMediaType())
                )
            
            // Add text fields if present
            if (!text.isNullOrEmpty()) {
                requestBody.addFormDataPart("text", text)
            }
            if (!title.isNullOrEmpty()) {
                requestBody.addFormDataPart("title", title)
            }
            if (!subject.isNullOrEmpty()) {
                requestBody.addFormDataPart("subject", subject)
            }
            requestBody.addFormDataPart("type", "image")
            requestBody.addFormDataPart("timestamp", System.currentTimeMillis().toString())
            
            val requestBuilder = Request.Builder()
                .url(endpoint)
                .header("User-Agent", USER_AGENT)

            if (!deliveryKey.isNullOrEmpty()) {
                requestBuilder.header("X-Delivery-Key", deliveryKey)
            }

            val request = requestBuilder.post(requestBody.build()).build()

            client.newCall(request).execute().use { response ->
                when (response.code) {
                    200 -> ShareResult.Success
                    202 -> {
                        // Server requests group selection
                        val body = response.body?.string() ?: "{}"
                        val responseJson = JSONObject(body)
                        val shareId = responseJson.optString("share_id", "")
                        val groupsArray = responseJson.optJSONArray("groups")
                        
                        if (shareId.isEmpty()) {
                            return@withContext ShareResult.Error("No share_id provided")
                        }
                        
                        if (groupsArray != null && groupsArray.length() > 0) {
                            val groups = mutableListOf<Group>()
                            for (i in 0 until groupsArray.length()) {
                                val groupJson = groupsArray.getJSONObject(i)
                                groups.add(
                                    Group(
                                        id = groupJson.getString("id"),
                                        name = groupJson.getString("name"),
                                        icon = groupJson.optString("icon").takeIf { it.isNotEmpty() },
                                        description = groupJson.optString("description").takeIf { it.isNotEmpty() }
                                    )
                                )
                            }
                            ShareResult.GroupSelectionRequired(shareId, groups)
                        } else {
                            ShareResult.Error("No groups provided")
                        }
                    }
                    else -> ShareResult.Error("HTTP ${response.code}: ${response.message}")
                }
            }
        } catch (e: IOException) {
            ShareResult.Error("Network error: ${e.message}")
        } catch (e: Exception) {
            ShareResult.Error("Error: ${e.message}")
        }
    }

    /**
     * Post shared content to the endpoint
     */
    suspend fun postSharedContent(
        endpoint: String,
        text: String,
        title: String? = null,
        subject: String? = null,
        deliveryKey: String? = null,
        contentType: String = "text"
    ): ShareResult = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("text", text)
                if (!title.isNullOrEmpty()) {
                    put("title", title)
                }
                if (!subject.isNullOrEmpty()) {
                    put("subject", subject)
                }
                put("type", contentType)
                put("timestamp", System.currentTimeMillis())
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = json.toString().toRequestBody(mediaType)

            val requestBuilder = Request.Builder()
                .url(endpoint)
                .header("User-Agent", USER_AGENT)

            if (!deliveryKey.isNullOrEmpty()) {
                requestBuilder.header("X-Delivery-Key", deliveryKey)
            }

            val request = requestBuilder.post(requestBody).build()

            client.newCall(request).execute().use { response ->
                when (response.code) {
                    200 -> ShareResult.Success
                    202 -> {
                        // Server requests group selection
                        val body = response.body?.string() ?: "{}"
                        val responseJson = JSONObject(body)
                        val shareId = responseJson.optString("share_id", "")
                        val groupsArray = responseJson.optJSONArray("groups")
                        
                        if (shareId.isEmpty()) {
                            return@withContext ShareResult.Error("No share_id provided")
                        }
                        
                        if (groupsArray != null && groupsArray.length() > 0) {
                            val groups = mutableListOf<Group>()
                            for (i in 0 until groupsArray.length()) {
                                val groupJson = groupsArray.getJSONObject(i)
                                groups.add(
                                    Group(
                                        id = groupJson.getString("id"),
                                        name = groupJson.getString("name"),
                                        icon = groupJson.optString("icon").takeIf { it.isNotEmpty() },
                                        description = groupJson.optString("description").takeIf { it.isNotEmpty() }
                                    )
                                )
                            }
                            ShareResult.GroupSelectionRequired(shareId, groups)
                        } else {
                            ShareResult.Error("No groups provided")
                        }
                    }
                    else -> ShareResult.Error("HTTP ${response.code}: ${response.message}")
                }
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
            val endpoint: String,
            val deliveryKey: String,
            val configUrl: String  // The actual URL that worked (with protocol)
        ) : ConfigResult()
        data class Error(val message: String) : ConfigResult()
    }

    sealed class ShareResult {
        object Success : ShareResult()
        data class GroupSelectionRequired(val shareId: String, val groups: List<Group>) : ShareResult()
        data class Error(val message: String) : ShareResult()
    }
}
