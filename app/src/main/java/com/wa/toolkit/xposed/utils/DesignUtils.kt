package com.wa.toolkit.xposed.utils

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.content.res.XResources
import android.graphics.*
import android.graphics.drawable.*
import android.graphics.drawable.shapes.RoundRectShape
import android.os.Build
import androidx.core.content.ContextCompat
import de.robv.android.xposed.XposedBridge
import com.wa.toolkit.xposed.utils.PrefUtils
import com.wa.toolkit.xposed.core.WppCore

object DesignUtils {

    private var mPrefs: SharedPreferences? = null

    @SuppressLint("UseCompatLoadingForDrawables")
    @JvmStatic
    fun getDrawable(id: Int): Drawable? {
        return Utils.getApplication().getDrawable(id)
    }

    @JvmStatic
    fun getDrawableByName(name: String): Drawable? {
        val id = Utils.getID(name, "drawable")
        if (id == 0) return null
        return getDrawable(id)
    }

    @JvmStatic
    fun getIconByName(name: String, isTheme: Boolean): Drawable? {
        val id = Utils.getID(name, "drawable")
        if (id == 0) return null
        val icon = getDrawable(id)
        if (isTheme && icon != null) {
            return coloredDrawable(icon, if (isNightMode()) Color.WHITE else Color.BLACK)
        }
        return icon
    }

    @JvmStatic
    fun coloredDrawable(drawable: Drawable, color: Int): Drawable {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            drawable.colorFilter = BlendModeColorFilter(color, BlendMode.SRC_ATOP)
        } else {
            @Suppress("DEPRECATION")
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
        }
        return drawable
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @JvmStatic
    fun alphaDrawable(drawable: Drawable, primaryTextColor: Int, i: Int): Drawable {
        val colored = coloredDrawable(drawable, primaryTextColor)
        colored.alpha = i
        return colored
    }

    @JvmStatic
    fun createDrawable(type: String, color: Int): Drawable {
        when (type) {
            "rc_dialog_bg" -> {
                val border = Utils.dipToPixels(12.0f).toFloat()
                val shapeDrawable = ShapeDrawable(
                    RoundRectShape(floatArrayOf(border, border, border, border, 0f, 0f, 0f, 0f), null, null)
                )
                shapeDrawable.paint.color = color
                return shapeDrawable
            }
            "selector_bg" -> {
                val border = Utils.dipToPixels(18.0f).toFloat()
                val selectorBg = ShapeDrawable(
                    RoundRectShape(floatArrayOf(border, border, border, border, border, border, border, border), null, null)
                )
                selectorBg.paint.color = color
                return selectorBg
            }
            "rc_dotline_dialog" -> {
                val border = Utils.dipToPixels(16.0f).toFloat()
                val shapeDrawable = ShapeDrawable(
                    RoundRectShape(floatArrayOf(border, border, border, border, border, border, border, border), null, null)
                )
                shapeDrawable.paint.color = color
                return shapeDrawable
            }
            "stroke_border" -> {
                val radius = Utils.dipToPixels(18.0f).toFloat()
                val outerRadii = floatArrayOf(radius, radius, radius, radius, radius, radius, radius, radius)
                val roundRectShape = RoundRectShape(outerRadii, null, null)
                val shapeDrawable = ShapeDrawable(roundRectShape)
                val paint = shapeDrawable.paint
                paint.color = Color.TRANSPARENT
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = Utils.dipToPixels(2f).toFloat()
                paint.color = color
                val inset = Utils.dipToPixels(2f)
                return InsetDrawable(shapeDrawable, inset, inset, inset, inset)
            }
        }
        return ColorDrawable(Color.BLACK)
    }

    @JvmStatic
    fun getPrimaryTextColor(): Int {
        val prefs = mPrefs ?: return if (isNightMode()) -0x2 else -0xfffffe
        var textColor = prefs.getInt("text_color", 0)
        if (shouldUseMonetColors()) {
            val monetTextColor = resolveMonetColor(if (isNightMode()) "system_neutral1_100" else "system_neutral1_900")
            if (monetTextColor != 0) {
                textColor = monetTextColor
            }
        }
        if (textColor == 0 || !prefs.getBoolean("changecolor", false)) {
            return if (isNightMode()) -0x2 else -0xfffffe // 0xfffffffe, 0xff000001
        }
        return textColor
    }

    @JvmStatic
    fun getUnSeenColor(): Int {
        val prefs = mPrefs ?: return 0xFF25d366.toInt()
        var primaryColor = prefs.getInt("primary_color", 0)
        if (shouldUseMonetColors()) {
            val monetPrimaryColor = resolveMonetColor(if (isNightMode()) "system_accent1_300" else "system_accent1_600")
            if (monetPrimaryColor != 0) {
                primaryColor = monetPrimaryColor
            }
        }
        if (primaryColor == 0 || !prefs.getBoolean("changecolor", false)) {
            return 0xFF25d366.toInt()
        }
        return primaryColor
    }

    @JvmStatic
    fun getPrimarySurfaceColor(): Int {
        val prefs = mPrefs ?: return if (isNightMode()) 0xff121212.toInt() else -0x2
        var backgroundColor = prefs.getInt("background_color", 0)
        if (shouldUseMonetColors()) {
            val monetBackgroundColor = resolveMonetColor(if (isNightMode()) "system_neutral1_900" else "system_neutral1_10")
            if (monetBackgroundColor != 0) {
                backgroundColor = monetBackgroundColor
            }
        }
        if (backgroundColor == 0 || !prefs.getBoolean("changecolor", false)) {
            return if (isNightMode()) 0xff121212.toInt() else -0x2 // 0xfffffffe
        }
        return backgroundColor
    }

    @JvmStatic
    fun generatePrimaryColorDrawable(drawable: Drawable?): Drawable? {
        if (drawable == null) return null
        val prefs = mPrefs ?: return null
        var primaryColorInt = prefs.getInt("primary_color", 0)
        if (shouldUseMonetColors()) {
            val monetPrimaryColor = resolveMonetColor(if (isNightMode()) "system_accent1_300" else "system_accent1_600")
            if (monetPrimaryColor != 0) {
                primaryColorInt = monetPrimaryColor
            }
        }
        if (primaryColorInt != 0 && prefs.getBoolean("changecolor", false)) {
            val bitmap = drawableToBitmap(drawable)
            val color = getDominantColor(bitmap)
            val replacedBitmap = replaceColor(bitmap, color, primaryColorInt, 120.0)
            return BitmapDrawable(Utils.getApplication().resources, replacedBitmap)
        }
        return null
    }

    @JvmStatic
    fun setReplacementDrawable(name: String, replacement: Drawable) {
        val resParam = PrefUtils.ResParam ?: return
        resParam.res.setReplacement(Utils.getApplication().packageName, "drawable", name,
            object : XResources.DrawableLoader() {
                override fun newDrawable(res: XResources, id: Int): Drawable {
                    return replacement
                }
            }
        )
    }

    @JvmStatic
    fun isNightMode(): Boolean {
        return if (WppCore.getDefaultTheme() <= 0) isNightModeBySystem() else WppCore.getDefaultTheme() == 2
    }

    @JvmStatic
    fun isNightModeBySystem(): Boolean {
        return (Utils.getApplication().resources.configuration.uiMode and 48) == 32
    }

    @JvmStatic
    fun setPrefs(prefs: SharedPreferences) {
        mPrefs = prefs
    }

    @JvmStatic
    fun isValidColor(primaryColor: String?): Boolean {
        return try {
            Color.parseColor(primaryColor)
            true
        } catch (e: Exception) {
            false
        }
    }

    @JvmStatic
    fun checkSystemColor(color: String): String {
        if (isValidColor(color)) {
            return color
        }
        try {
            if (color.startsWith("color_")) {
                val idColor = color.replace("color_", "")
                val colorRes = android.R.color::class.java.getField(idColor).getInt(null)
                if (colorRes != -1) {
                    return "#" + Integer.toHexString(ContextCompat.getColor(Utils.getApplication(), colorRes))
                }
            }
        } catch (e: Exception) {
            XposedBridge.log("Error: $e")
        }
        return "0"
    }

    private fun shouldUseMonetColors(): Boolean {
        val prefs = mPrefs ?: return false
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return false
        if (!prefs.getBoolean("changecolor", false)) return false
        return "monet" == prefs.getString("changecolor_mode", "manual")
    }

    private fun resolveMonetColor(resourceName: String): Int {
        val color = checkSystemColor("color_$resourceName")
        if (!isValidColor(color)) return 0
        return try {
            Color.parseColor(color)
        } catch (ignored: Exception) {
            0
        }
    }

    @JvmStatic
    fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }

        val bitmap = Bitmap.createBitmap(
            if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 1,
            if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 1,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)

        return bitmap
    }

    @JvmStatic
    fun getDominantColor(bitmap: Bitmap): Int {
        val colorCountMap = HashMap<Int, Int>()

        for (y in 0 until bitmap.height) {
            for (x in 0 until bitmap.width) {
                val color = bitmap.getPixel(x, y)
                if (Color.alpha(color) > 0) {
                    colorCountMap[color] = (colorCountMap[color] ?: 0) + 1
                }
            }
        }

        return colorCountMap.entries.maxByOrNull { it.value }?.key ?: Color.BLACK
    }

    @JvmStatic
    fun colorDistance(color1: Int, color2: Int): Double {
        val r1 = Color.red(color1)
        val g1 = Color.green(color1)
        val b1 = Color.blue(color1)

        val r2 = Color.red(color2)
        val g2 = Color.green(color2)
        val b2 = Color.blue(color2)

        return Math.sqrt(Math.pow((r1 - r2).toDouble(), 2.0) + Math.pow((g1 - g2).toDouble(), 2.0) + Math.pow((b1 - b2).toDouble(), 2.0))
    }

    @JvmStatic
    fun replaceColor(bitmap: Bitmap, oldColor: Int, newColor: Int, threshold: Double): Bitmap {
        val newBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)

        for (y in 0 until newBitmap.height) {
            for (x in 0 until newBitmap.width) {
                val currentColor = newBitmap.getPixel(x, y)
                if (colorDistance(currentColor, oldColor) < threshold) {
                    newBitmap.setPixel(x, y, newColor)
                }
            }
        }

        return newBitmap
    }

    @JvmStatic
    fun resizeDrawable(icon: Drawable, width: Int, height: Int): Drawable {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        icon.setBounds(0, 0, canvas.width, canvas.height)
        icon.draw(canvas)
        return BitmapDrawable(Utils.getApplication().resources, bitmap)
    }
}
map)
    }
}
