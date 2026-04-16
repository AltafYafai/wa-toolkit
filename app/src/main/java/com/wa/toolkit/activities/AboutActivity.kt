package com.wa.toolkit.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.wa.toolkit.BuildConfig
import com.wa.toolkit.UpdateChecker
import com.wa.toolkit.activities.base.BaseActivity
import com.wa.toolkit.databinding.ActivityAboutBinding
import com.wa.toolkit.xposed.utils.Utils
import de.robv.android.xposed.XposedBridge

class AboutActivity : BaseActivity() {

    private lateinit var binding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
    }

    private fun setupUI() {
        binding.toolbar.setNavigationOnClickListener { finish() }
        
        // Use string template for cleaner concatenation
        binding.appVersion.text = "Version ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"

        binding.btnTelegram.setOnClickListener {
            openUrl("https://t.me/TheEmeraldHub")
        }

        binding.btnGithub.setOnClickListener {
            openUrl("https://github.com/altafyafai7/whatsapp-toolkit")
        }

        // Add donation link
        binding.btnSupport.setOnClickListener {
            openUrl("https://coindrop.to/suvojeet_sengupta")
        }

        // Setup Update Check button
        binding.btnCheckUpdate.setOnClickListener {
            checkUpdates()
        }
    }

    private fun checkUpdates() {
        Toast.makeText(this, "Checking for updates...", Toast.LENGTH_SHORT).show()
        Thread(UpdateChecker(this)).start()
    }

    private fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            XposedBridge.log("AboutActivity: Failed to open URL: $url")
            Toast.makeText(this, "Could not open link", Toast.LENGTH_SHORT).show()
        }
    }
}
