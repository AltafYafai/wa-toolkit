package com.wa.toolkit.utils

import android.graphics.Color

object IColors {
    val colors = HashMap<String, String>()
    val alphacolors = HashMap<String, String>()
    val backgroundColors = HashMap<String, String>()
    val primaryColors = HashMap<String, String>()
    val textColors = HashMap<String, String>()

    @JvmStatic
    fun parseColor(str: String): Int {
        return Color.parseColor(str)
    }

    @JvmStatic
    fun toString(i: Int): String {
        var color = Integer.toHexString(i)
        if (color.length == 7) {
            color = "0$color"
        } else if (color.length == 1) {
            color = "00000000"
        }
        return "#$color"
    }

    @JvmStatic
    fun getFromIntColor(color: Int, colors: HashMap<String, String>): Int {
        val sColor = toString(color)
        var newColor = colors[sColor]
        if (newColor != null && newColor.length == 9) {
            return parseColor(newColor)
        } else {
            if (!sColor.startsWith("#ff")) {
                val sColorSub = sColor.substring(0, 3)
                newColor = colors[sColor.substring(3)]
                if (newColor != null) {
                    return parseColor(sColorSub + newColor)
                }
            }
        }
        return color
    }

    @JvmStatic
    fun initColors() {
        primaryColors.clear()
        textColors.clear()
        backgroundColors.clear()
        colors.clear()

        // primary colors
        primaryColors["00a884"] = "00a884"
        primaryColors["1da457"] = "1da457"
        primaryColors["21c063"] = "21c063"
        primaryColors["d9fdd3"] = "d9fdd3"
        primaryColors["#ff00a884"] = "#ff00a884"
        primaryColors["#ff1da457"] = "#ff1da457"
        primaryColors["#ff21c063"] = "#ff21c063"
        primaryColors["#ff1daa61"] = "#ff1daa61"
        primaryColors["#ff25d366"] = "#ff25d366"
        primaryColors["#ffd9fdd3"] = "#ffd9fdd3"
        primaryColors["#ff1b864b"] = "#ff1b864b"

        primaryColors["#ff144d37"] = "#ff144d37"
        primaryColors["#ff1b8755"] = "#ff1b8755"
        primaryColors["#ff15603e"] = "#ff15603e"
        primaryColors["#ff103529"] = "#c0103529"

        // text colors
        textColors["#ffeaedee"] = "#ffeaedee"
        textColors["#fff7f8fa"] = "#fff7f8fa"

        // background colors
        backgroundColors["0b141a"] = "0a1014"
        backgroundColors["#ff0b141a"] = "#ff111b21"
        backgroundColors["#ff111b21"] = "#ff111b21"
        backgroundColors["#ff000000"] = "#ff000000"
        backgroundColors["#ff0a1014"] = "#ff0a1014"
        backgroundColors["#ff10161a"] = "#ff10161a"
        backgroundColors["#ff12181c"] = "#ff12181c"
        backgroundColors["#ff20272b"] = "#ff20272b"

        // Alpha colors
        alphacolors["#ff15603e"] = "#8015603e"
    }
}
