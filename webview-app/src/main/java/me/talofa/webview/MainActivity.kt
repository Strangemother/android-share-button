package me.talofa.webview

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import org.json.JSONObject

class MainActivity : ComponentActivity() {
    private lateinit var webView: WebView
    private lateinit var prefs: SharedPreferences
    
    companion object {
        private const val PREFS_NAME = "webview_settings"
        private const val KEY_SITE_URL = "site_url"
        private const val KEY_CUSTOM_SCHEME = "custom_scheme"
    }
    
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        
        // Handle deep link configuration
        handleDeepLink(intent)
        
        // Get configured URL or use default
        val siteUrl = prefs.getString(KEY_SITE_URL, BuildConfig.DEFAULT_URL) ?: BuildConfig.DEFAULT_URL
        
        webView = WebView(this).apply {
            settings.apply {
                // Enable JavaScript (required for canvas)
                javaScriptEnabled = true
                
                // Enable DOM storage
                domStorageEnabled = true
                
                // Enable database
                databaseEnabled = true
                
                // Enable file access
                allowFileAccess = true
                allowContentAccess = true
                
                // Enable WebGL and hardware acceleration
                mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                
                // Cache settings for better performance
                cacheMode = WebSettings.LOAD_DEFAULT
                
                // Viewport settings for responsive design
                useWideViewPort = true
                loadWithOverviewMode = true
                
                // Enable zooming if needed (can disable for app-like feel)
                builtInZoomControls = false
                displayZoomControls = false
            }
            
            // Inject JavaScript interface - accessible as window.Android in your web page
            addJavascriptInterface(WebAppInterface(this@MainActivity), "Android")
            
            // Handle page navigation within WebView
            webViewClient = WebViewClient()
            
            // Handle JavaScript alerts, confirms, etc.
            webChromeClient = WebChromeClient()
            
            // Load your site
            loadUrl(siteUrl)
        }
        
        setContentView(webView)
        
        // Handle back button to navigate web history
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) {
                    webView.goBack()
                } else {
                    finish()
                }
            }
        })
    }
    
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let { handleDeepLink(it) }
    }
    
    override fun onDestroy() {
        webView.destroy()
        super.onDestroy()
    }
    
    /**
     * Handle deep link configuration
     * Formats:
     *   webview://setup?url=https://your-site.com
     *   webview://setup?url=https://your-site.com&scheme=myapp123
     *   [custom-scheme]://setup?url=https://your-site.com
     */
    private fun handleDeepLink(intent: Intent) {
        if (intent.action == Intent.ACTION_VIEW) {
            intent.data?.let { uri ->
                val url = uri.getQueryParameter("url")
                val customScheme = uri.getQueryParameter("scheme")
                
                if (!url.isNullOrEmpty()) {
                    // Save the URL
                    prefs.edit().putString(KEY_SITE_URL, url).apply()
                    
                    // Save custom scheme if provided
                    if (!customScheme.isNullOrEmpty() && customScheme.matches(Regex("^[a-z0-9]{4,16}$"))) {
                        prefs.edit().putString(KEY_CUSTOM_SCHEME, customScheme).apply()
                        Toast.makeText(
                            this,
                            "Configured!\nURL: $url\nNew scheme: $customScheme://",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(
                            this,
                            "Site URL configured: $url",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    
                    // Reload the WebView with new URL
                    webView.loadUrl(url)
                }
            }
        }
    }
    
    /**
     * JavaScript Interface - Methods callable from JavaScript
     * Access in JavaScript as: window.Android.methodName()
     */
    class WebAppInterface(private val context: Context) {
        
        /**
         * Vibrate the phone
         * JavaScript: Android.vibrate(200) // milliseconds
         * JavaScript: Android.vibrate(0.5) // 0.0 to 1.0 (converts to 0-500ms)
         */
        @JavascriptInterface
        fun vibrate(strength: Float) {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            
            // Convert 0.0-1.0 range to milliseconds (0-500ms)
            val duration = if (strength <= 1.0f) {
                (strength * 500).toLong()
            } else {
                strength.toLong()
            }
            
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(duration)
            }
        }
        
        /**
         * Show a toast message
         * JavaScript: Android.toast("Hello from web!")
         */
        @JavascriptInterface
        fun toast(message: String) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
        
        /**
         * Handle generic events with JSON data
         * JavaScript: Android.postMessage(JSON.stringify({ event: 'vibrate', strength: 0.5 }))
         */
        @JavascriptInterface
        fun postMessage(jsonString: String) {
            try {
                val json = JSONObject(jsonString)
                val event = json.optString("event")
                
                when (event) {
                    "vibrate" -> {
                        val strength = json.optDouble("strength", 0.5).toFloat()
                        vibrate(strength)
                    }
                    "toast" -> {
                        val message = json.optString("message", "")
                        toast(message)
                    }
                    // Add more custom events here
                    else -> {
                        toast("Unknown event: $event")
                    }
                }
            } catch (e: Exception) {
                toast("Error parsing message: ${e.message}")
            }
        }
        
        /**
         * Get device info
         * JavaScript: const info = Android.getDeviceInfo()
         */
        @JavascriptInterface
        fun getDeviceInfo(): String {
            val info = JSONObject().apply {
                put("manufacturer", android.os.Build.MANUFACTURER)
                put("model", android.os.Build.MODEL)
                put("androidVersion", android.os.Build.VERSION.RELEASE)
                put("sdkVersion", android.os.Build.VERSION.SDK_INT)
            }
            return info.toString()
        }
        
        /**
         * Get current configured site URL
         * JavaScript: const url = Android.getSiteUrl()
         */
        @JavascriptInterface
        fun getSiteUrl(): String {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getString(KEY_SITE_URL, BuildConfig.DEFAULT_URL) ?: BuildConfig.DEFAULT_URL
        }
        
        /**
         * Get current custom scheme
         * JavaScript: const scheme = Android.getScheme()
         */
        @JavascriptInterface
        fun getScheme(): String {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getString(KEY_CUSTOM_SCHEME, BuildConfig.DEFAULT_SCHEME) ?: BuildConfig.DEFAULT_SCHEME
        }
        
        /**
         * Get app configuration as JSON
         * JavaScript: const config = JSON.parse(Android.getConfig())
         */
        @JavascriptInterface
        fun getConfig(): String {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val config = JSONObject().apply {
                put("siteUrl", prefs.getString(KEY_SITE_URL, BuildConfig.DEFAULT_URL))
                put("scheme", prefs.getString(KEY_CUSTOM_SCHEME, BuildConfig.DEFAULT_SCHEME))
                put("deepLink", "${prefs.getString(KEY_CUSTOM_SCHEME, BuildConfig.DEFAULT_SCHEME)}://setup")
            }
            return config.toString()
        }
    }
}
