package com.wa.toolkit.ui.preferences

import android.content.Context
import android.util.AttributeSet
import androidx.preference.PreferenceCategory
import com.wa.toolkit.R

class CardPreferenceCategory @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : PreferenceCategory(context, attrs) {

    init {
        layoutResource = R.layout.preference_category_card
    }
}
