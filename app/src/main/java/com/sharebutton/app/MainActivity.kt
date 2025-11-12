package com.sharebutton.app

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.sharebutton.app.databinding.ActivityMainBinding
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
    }

    private fun setupListeners() {
        binding.setupButton.setOnClickListener {
            val apiUrl = binding.apiEndpointEditText.text?.toString()?.trim()
            if (apiUrl.isNullOrEmpty()) {
                Toast.makeText(this, R.string.enter_url, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            setupConfiguration(apiUrl)
        }

        binding.testButton.setOnClickListener {
            testShare()
        }
    }

    private fun loadConfiguration() {
        if (configManager.isConfigured()) {
            displayConfiguration()
        }
    }

    private fun setupConfiguration(apiUrl: String) {
        binding.setupButton.isEnabled = false
        lifecycleScope.launch {
            try {
                when (val result = apiClient.fetchConfiguration(apiUrl)) {
                    is ApiClient.ConfigResult.Success -> {
                        configManager.apiUrl = apiUrl
                        configManager.shareName = result.name
                        configManager.iconUrl = result.icon
                        configManager.postEndpoint = result.endpoint

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
