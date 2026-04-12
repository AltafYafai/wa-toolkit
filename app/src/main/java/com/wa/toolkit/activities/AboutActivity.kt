package com.wa.toolkit.activities

import android.os.Bundle
import com.wa.toolkit.activities.base.BaseActivity
import com.wa.toolkit.databinding.ActivityAboutBinding

class AboutActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}
