package com.wa.toolkit.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.wa.toolkit.model.SearchableFeature
import com.wa.toolkit.utils.FeatureCatalog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SearchViewModel(application: Application) : AndroidViewModel(application) {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<SearchableFeature>>(emptyList())
    val searchResults: StateFlow<List<SearchableFeature>> = _searchResults.asStateFlow()

    private val allFeatures: List<SearchableFeature> by lazy {
        FeatureCatalog.getAllFeatures(getApplication())
    }

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
        updateSearchResults(newQuery)
    }

    private fun updateSearchResults(query: String) {
        viewModelScope.launch {
            if (query.isBlank()) {
                _searchResults.value = allFeatures
            } else {
                _searchResults.value = FeatureCatalog.search(getApplication(), query)
            }
        }
    }

    init {
        // Initialize with all features
        _searchResults.value = allFeatures
    }
}
