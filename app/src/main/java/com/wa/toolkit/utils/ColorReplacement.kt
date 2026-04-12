package com.wa.toolkit.utils

import android.graphics.PorterDuffColorFilter
import android.view.View
import android.view.ViewGroup
import android.view.ViewStub
import android.widget.ImageView
import android.widget.TextView
import com.wa.toolkit.utils.DrawableColors.replaceColor
import com.wa.toolkit.xposed.utils.DesignUtils
import de.robv.android.xposed.XposedHelpers

object ColorReplacement {
    @JvmStatic
    fun replaceColors(view: View?, colors: HashMap<String, String>) {
        when (view) {
            null -> return
            is ImageView -> Image.replace(view, colors)
            is TextView -> Text.replace(view, colors)
            is ViewGroup -> Group.replace(view, colors)
            is ViewStub -> replaceColor(view.background, colors)
        }
    }

    object Image {
        @JvmStatic
        fun replace(view: ImageView, colors: HashMap<String, String>) {
            replaceColor(view.background, colors)
            val colorFilter = view.colorFilter ?: return
            if (colorFilter is PorterDuffColorFilter) {
                val color = XposedHelpers.callMethod(colorFilter, "getColor") as Int
                val sColor = IColors.toString(color)
                var newColor = colors[sColor]
                if (newColor != null) {
                    view.setColorFilter(IColors.parseColor(newColor))
                } else {
                    if (!sColor.startsWith("#ff") && !sColor.startsWith("#0")) {
                        val sColorSub = sColor.substring(0, 3)
                        newColor = colors[sColor.substring(3)]
                        if (newColor != null) {
                            view.setColorFilter(IColors.parseColor(sColorSub + newColor))
                        }
                    }
                }
            }
        }
    }

    object Text {
        @JvmStatic
        fun replace(view: TextView, colors: HashMap<String, String>) {
            val color = view.currentTextColor
            val sColor = IColors.toString(color)
            if (sColor == "#ffffffff" && !DesignUtils.isNightMode()) {
                return
            }
            replaceColor(view.background, colors)
            var newColor = colors[sColor]
            if (newColor != null) {
                view.setTextColor(IColors.parseColor(newColor))
            } else {
                if (!sColor.startsWith("#ff") && !sColor.startsWith("#0")) {
                    val sColorSub = sColor.substring(0, 3)
                    newColor = colors[sColor.substring(3)]
                    if (newColor != null) {
                        view.setTextColor(IColors.parseColor(sColorSub + newColor))
                    }
                }
            }
        }
    }

    object Group {
        @JvmStatic
        fun replace(view: ViewGroup, colors: HashMap<String, String>) {
            val bg = view.background
            val count = view.childCount
            for (i in 0 until count) {
                val child = view.getChildAt(i)
                replaceColors(child, colors)
            }
            replaceColor(bg, colors)
        }
    }
}
