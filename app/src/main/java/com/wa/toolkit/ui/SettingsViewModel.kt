package com.wa.toolkit.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wa.toolkit.data.PreferenceRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val repository: PreferenceRepository) : ViewModel() {

    fun getBoolean(key: String, defaultValue: Boolean): StateFlow<Boolean> {
        return repository.getBoolean(key, defaultValue)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), defaultValue)
    }

    fun getString(key: String, defaultValue: String): StateFlow<String> {
        return repository.getString(key, defaultValue)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), defaultValue)
    }

    fun getInt(key: String, defaultValue: Int): StateFlow<Int> {
        return repository.getInt(key, defaultValue)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), defaultValue)
    }

    val colorMode = repository.getString("wae_color_mode", "preset")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "preset")
        
    val colorPreset = repository.getString("wae_color_preset", "green")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "green")

    fun toggleBoolean(key: String, currentValue: Boolean) {
        viewModelScope.launch {
            repository.setBoolean(key, !currentValue)
        }
    }

    fun setString(key: String, value: String) {
        viewModelScope.launch {
            repository.setString(key, value)
        }
    }

    fun setInt(key: String, value: Int) {
        viewModelScope.launch {
            repository.setInt(key, value)
        }
    }
}
