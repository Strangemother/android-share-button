package me.talofa.app

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import me.talofa.app.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var configManager: ConfigManager
    private val apiClient = ApiClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configManager = ConfigManager(this)

        setupListeners()
        loadConfiguration()
        handleDeepLink(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let { handleDeepLink(it) }
    }

    /**
     * Handle deep link from QR code scan
     * Supports formats:
     * - talofa://setup?url=https://example.com/api/config&key=optional-api-key
     * - https://talofa.me/setup?url=https://example.com/api/config&key=optional-api-key
     */
    private fun handleDeepLink(intent: Intent) {
        if (intent.action == Intent.ACTION_VIEW) {
            intent.data?.let { uri ->
                // Extract URL parameter
                val configUrl = uri.getQueryParameter("url")
                val apiKey = uri.getQueryParameter("key")
                
                if (!configUrl.isNullOrEmpty()) {
                    // Populate the fields
                    binding.apiEndpointEditText.setText(configUrl)
                    if (!apiKey.isNullOrEmpty()) {
                        binding.apiKeyEditText.setText(apiKey)
                    }
                    
                    // Show a toast to indicate deep link was received
                    Toast.makeText(
                        this,
                        "Config URL loaded from QR code",
                        Toast.LENGTH_SHORT
                    ).show()
                    
                    // Auto-trigger setup
                    setupConfiguration(configUrl, apiKey)
                }
            }
        }
    }

    private fun setupListeners() {
        binding.learnMoreTextView.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://talofa.me"))
            startActivity(intent)
        }

        binding.setupButton.setOnClickListener {
            val apiUrl = binding.apiEndpointEditText.text?.toString()?.trim()
            if (apiUrl.isNullOrEmpty()) {
                Toast.makeText(this, R.string.enter_url, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val apiKey = binding.apiKeyEditText.text?.toString()?.trim()
            setupConfiguration(apiUrl, apiKey)
        }

        binding.testButton.setOnClickListener {
            testShare()
        }
    }

    private fun loadConfiguration() {
        if (configManager.isConfigured()) {
            // Load the saved API URL and API key into the text fields
            val savedApiUrl = configManager.apiUrl
            if (!savedApiUrl.isNullOrEmpty()) {
                binding.apiEndpointEditText.setText(savedApiUrl)
            }
            val savedApiKey = configManager.apiKey
            if (!savedApiKey.isNullOrEmpty()) {
                binding.apiKeyEditText.setText(savedApiKey)
            }
            displayConfiguration()
        }
    }

    private fun setupConfiguration(apiUrl: String, apiKey: String?) {
        binding.setupButton.isEnabled = false
        lifecycleScope.launch {
            try {
                when (val result = apiClient.fetchConfiguration(apiUrl, apiKey)) {
                    is ApiClient.ConfigResult.Success -> {
                        // Save the actual URL that worked (with protocol)
                        configManager.apiUrl = result.configUrl
                        configManager.apiKey = apiKey
                        configManager.shareName = result.name
                        configManager.iconUrl = result.icon
                        configManager.postEndpoint = result.endpoint
                        configManager.deliveryKey = result.deliveryKey

                        Toast.makeText(
                            this@MainActivity,
                            R.string.setup_success,
                            Toast.LENGTH_SHORT
                        ).show()

                        displayConfiguration()
                    }
                    is ApiClient.ConfigResult.Error -> {
                        Toast.makeText(
                            this@MainActivity,
                            getString(R.string.setup_error, result.message),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } finally {
                binding.setupButton.isEnabled = true
            }
        }
    }

    private fun displayConfiguration() {
        val name = configManager.shareName ?: "Custom Share"
        val endpoint = configManager.postEndpoint ?: ""
        val iconUrl = configManager.iconUrl

        binding.statusTextView.text = getString(R.string.setup_success)
        binding.configNameTextView.text = getString(R.string.configured_name, name)
        binding.configNameTextView.visibility = View.VISIBLE

        binding.configEndpointTextView.text = getString(R.string.configured_endpoint, endpoint)
        binding.configEndpointTextView.visibility = View.VISIBLE

        binding.testButton.visibility = View.VISIBLE

        // Load icon if URL is provided
        if (!iconUrl.isNullOrEmpty()) {
            loadIcon(iconUrl)
        }
    }

    private fun loadIcon(iconUrl: String) {
        lifecycleScope.launch {
            try {
                val bitmap = withContext(Dispatchers.IO) {
                    val client = OkHttpClient()
                    val request = Request.Builder().url(iconUrl).build()
                    client.newCall(request).execute().use { response ->
                        if (response.isSuccessful) {
                            response.body?.byteStream()?.let { stream ->
                                BitmapFactory.decodeStream(stream)
                            }
                        } else {
                            null
                        }
                    }
                }

                if (bitmap != null) {
                    binding.iconImageView.setImageBitmap(bitmap)
                    binding.iconImageView.visibility = View.VISIBLE
                }
            } catch (e: IOException) {
                // Ignore icon loading errors
            }
        }
    }

    private fun testShare() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "This is a test share from Share Button app")
            putExtra(Intent.EXTRA_TITLE, "Test Share")
        }
        startActivity(Intent.createChooser(intent, "Test Share"))
    }
}
