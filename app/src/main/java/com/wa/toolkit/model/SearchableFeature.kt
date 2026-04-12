package com.wa.toolkit.model

/**
 * Data model representing a searchable feature in the WhatsappToolkit app.
 * Each feature corresponds to a preference item that can be found via search.
 */
data class SearchableFeature(
    val key: String,
    val title: String,
    val summary: String? = null,
    val category: Category,
    val fragmentType: FragmentType,
    val parentKey: String? = null,
    val searchTags: List<String> = emptyList()
) {

    enum class Category(val displayName: String) {
        GENERAL("General"),
        GENERAL_HOME("General"),
        GENERAL_HOMESCREEN("General"),
        GENERAL_CONVERSATION("General"),
        PRIVACY("Privacy"),
        MEDIA("Media"),
        CUSTOMIZATION("Customization"),
        RECORDINGS("Recordings"),
        HOME_ACTIONS("Home")
    }

    enum class FragmentType(val position: Int) {
        GENERAL(0),
        PRIVACY(1),
        HOME(2),
        MEDIA(3),
        CUSTOMIZATION(4),
        RECORDINGS(5),
        ACTIVITY(99)
    }

    /**
     * Check if this feature matches the search query.
     * Performs case-insensitive matching against title, summary, and tags.
     */
    fun matches(query: String?): Boolean {
        if (query.isNullOrBlank()) {
            return false
        }

        val lowerQuery = query.lowercase().trim()

        // Check title
        if (title.lowercase().contains(lowerQuery)) {
            return true
        }

        // Check summary
        if (summary?.lowercase()?.contains(lowerQuery) == true) {
            return true
        }

        // Check tags
        for (tag in searchTags) {
            if (tag.lowercase().contains(lowerQuery)) {
                return true
            }
        }

        // Check category
        if (category.displayName.lowercase().contains(lowerQuery)) {
            return true
        }

        return false
    }

    override fun toString(): String {
        return "SearchableFeature(key='$key', title='$title', category=$category)"
    }
}
