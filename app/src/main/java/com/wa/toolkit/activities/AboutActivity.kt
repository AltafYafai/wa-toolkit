package com.wa.toolkit.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.wa.toolkit.BuildConfig
import com.wa.toolkit.activities.base.BaseActivity
import com.wa.toolkit.databinding.ActivityAboutBinding

class AboutActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }
        
        binding.appVersion.text = "Version ${BuildConfig.VERSION_NAME}"

        binding.btnTelegram.setOnClickListener {
            openUrl("https://t.me/TheEmeraldHub")
        }

        binding.btnGithub.setOnClickListener {
            openUrl("https://github.com/altafyafai7/whatsapp-toolkit")
        }
    }

    private fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (ignored: Exception) {
        }
    }
}
