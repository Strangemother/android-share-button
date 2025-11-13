package me.talofa.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.io.InputStream

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
                when {
                    intent.type == "text/plain" || intent.type?.startsWith("text/") == true -> {
                        handleTextShare()
                    }
                    intent.type?.startsWith("image/") == true -> {
                        handleImageShare()
                    }
                    else -> {
                        Toast.makeText(this, "Unsupported content type", Toast.LENGTH_SHORT).show()
                        finish()
                    }
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

        postContent(sharedText, sharedTitle, sharedSubject)
    }
    
    private fun handleImageShare() {
        val imageUri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
        val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
        val sharedTitle = intent.getStringExtra(Intent.EXTRA_TITLE)
        val sharedSubject = intent.getStringExtra(Intent.EXTRA_SUBJECT)

        if (imageUri == null) {
            Toast.makeText(this, "No image to share", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        Toast.makeText(this, R.string.sharing, Toast.LENGTH_SHORT).show()

        postImageContent(imageUri, sharedText, sharedTitle, sharedSubject)
    }
    
    private fun postContent(
        text: String,
        title: String?,
        subject: String?
    ) {
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
                text = text,
                title = title,
                subject = subject,
                deliveryKey = deliveryKey
            )) {
                is ApiClient.ShareResult.Success -> {
                    Toast.makeText(
                        this@ShareReceiverActivity,
                        R.string.share_success,
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
                is ApiClient.ShareResult.GroupSelectionRequired -> {
                    // Show bottom sheet for group selection
                    showGroupSelectionBottomSheet(result.shareId, result.groups)
                }
                is ApiClient.ShareResult.Error -> {
                    Toast.makeText(
                        this@ShareReceiverActivity,
                        getString(R.string.share_error, result.message),
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                }
            }
        }
    }
    
    private fun postImageContent(
        imageUri: Uri,
        text: String?,
        title: String?,
        subject: String?
    ) {
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
            
            // Read image data
            val inputStream: InputStream? = try {
                contentResolver.openInputStream(imageUri)
            } catch (e: Exception) {
                Toast.makeText(
                    this@ShareReceiverActivity,
                    "Failed to read image: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                finish()
                return@launch
            }
            
            if (inputStream == null) {
                Toast.makeText(
                    this@ShareReceiverActivity,
                    "Failed to read image",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
                return@launch
            }
            
            val imageBytes = inputStream.use { it.readBytes() }
            val mimeType = contentResolver.getType(imageUri) ?: "image/jpeg"
            val fileName = "image.${mimeType.substringAfter("/")}"
            
            when (val result = apiClient.postImageContent(
                endpoint = endpoint,
                imageBytes = imageBytes,
                fileName = fileName,
                mimeType = mimeType,
                text = text,
                title = title,
                subject = subject,
                deliveryKey = deliveryKey
            )) {
                is ApiClient.ShareResult.Success -> {
                    Toast.makeText(
                        this@ShareReceiverActivity,
                        R.string.share_success,
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
                is ApiClient.ShareResult.GroupSelectionRequired -> {
                    // Show bottom sheet for group selection
                    showGroupSelectionBottomSheet(result.shareId, result.groups)
                }
                is ApiClient.ShareResult.Error -> {
                    Toast.makeText(
                        this@ShareReceiverActivity,
                        getString(R.string.share_error, result.message),
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                }
            }
        }
    }
    
    private fun showGroupSelectionBottomSheet(
        shareId: String,
        groups: List<Group>
    ) {
        val bottomSheet = GroupSelectionBottomSheet()
            .setGroups(groups)
            .setOnGroupSelectedListener { selectedGroup ->
                // Post group selection with share_id
                // If group.id is null, it's a new group - pass the name instead
                postGroupSelection(shareId, selectedGroup.id, selectedGroup.name)
            }
        
        // Finish activity if bottom sheet is dismissed without selection
        bottomSheet.setOnDismissListener {
            if (!isFinishing) {
                finish()
            }
        }
        
        bottomSheet.show(supportFragmentManager, "GroupSelectionBottomSheet")
    }
    
    private fun postGroupSelection(shareId: String, groupId: String?, groupName: String) {
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
            
            when (val result = apiClient.postGroupSelection(
                endpoint = endpoint,
                shareId = shareId,
                groupId = groupId,
                groupName = if (groupId == null) groupName else null,
                deliveryKey = deliveryKey
            )) {
                is ApiClient.ShareResult.Success -> {
                    Toast.makeText(
                        this@ShareReceiverActivity,
                        R.string.share_success,
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
                is ApiClient.ShareResult.Error -> {
                    Toast.makeText(
                        this@ShareReceiverActivity,
                        getString(R.string.share_error, result.message),
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                }
                is ApiClient.ShareResult.GroupSelectionRequired -> {
                    // This shouldn't happen in group selection response
                    Toast.makeText(
                        this@ShareReceiverActivity,
                        "Unexpected response",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            }
        }
    }
}
