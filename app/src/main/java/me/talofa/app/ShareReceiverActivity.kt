package me.talofa.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

/**
 * Activity that handles incoming share intents
 */
class ShareReceiverActivity : AppCompatActivity() {
    private lateinit var configManager: ConfigManager
    private val apiClient = ApiClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        configManager = ConfigManager(this)

        if (!configManager.isConfigured()) {
            Toast.makeText(this, "Please configure the app first", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        handleShareIntent()
    }

    private fun handleShareIntent() {
        when (intent?.action) {
            Intent.ACTION_SEND -> {
                if (intent.type == "text/plain" || intent.type?.startsWith("text/") == true) {
                    handleTextShare()
                } else {
                    Toast.makeText(this, "Unsupported content type", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            else -> {
                finish()
            }
        }
    }

    private fun handleTextShare() {
        val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
        val sharedTitle = intent.getStringExtra(Intent.EXTRA_TITLE)
        val sharedSubject = intent.getStringExtra(Intent.EXTRA_SUBJECT)

        if (sharedText.isNullOrEmpty()) {
            Toast.makeText(this, "No content to share", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        Toast.makeText(this, R.string.sharing, Toast.LENGTH_SHORT).show()

        lifecycleScope.launch {
            val endpoint = configManager.postEndpoint ?: run {
                Toast.makeText(
                    this@ShareReceiverActivity,
                    "No endpoint configured",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
                return@launch
            }

            val deliveryKey = configManager.deliveryKey
            
            when (val result = apiClient.postSharedContent(
                endpoint = endpoint,
                text = sharedText,
                title = sharedTitle,
                subject = sharedSubject,
                deliveryKey = deliveryKey
            )) {
                is ApiClient.ShareResult.Success -> {
                    Toast.makeText(
                        this@ShareReceiverActivity,
                        R.string.share_success,
                        Toast.LENGTH_SHORT
                    ).show()
                }
                is ApiClient.ShareResult.Error -> {
                    Toast.makeText(
                        this@ShareReceiverActivity,
                        getString(R.string.share_error, result.message),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            finish()
        }
    }
}
