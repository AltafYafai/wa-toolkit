package com.wa.toolkit.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainViewModel : ViewModel() {
    private val _wppVersion = MutableStateFlow("Not Detected")
    val wppVersion: StateFlow<String> = _wppVersion.asStateFlow()

    private val _isWppActive = MutableStateFlow(false)
    val isWppActive: StateFlow<Boolean> = _isWppActive.asStateFlow()

    fun updateStatus(version: String?, active: Boolean) {
        _wppVersion.value = version ?: "Not Detected"
        _isWppActive.value = active
    }
}
