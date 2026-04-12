package com.wa.toolkit.utils

import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.NinePatch
import android.graphics.Paint
import android.graphics.drawable.*
import com.wa.toolkit.xposed.core.devkit.Unobfuscator
import com.wa.toolkit.xposed.utils.ReflectionUtils
import com.wa.toolkit.xposed.utils.Utils
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers

object DrawableColors {

    private val ninePatchs = HashMap<Bitmap, Int>()
    private var mMaterialShapeDrawableClass: Class<*>? = null

    @JvmStatic
    fun replaceColor(drawable: Drawable?, colors: HashMap<String, String>) {
        if (drawable == null) return
        
        when (drawable) {
            is StateListDrawable -> {
                val count = StateListDrawableCompact.getStateCount(drawable)
                for (i in 0 until count) {
                    val stateDrawable = StateListDrawableCompact.getStateDrawable(drawable, i)
                    if (stateDrawable != null) {
                        replaceColor(stateDrawable, colors)
                    }
                }
            }
            is GradientDrawable -> {
                val gradientColors = drawable.colors
                if (gradientColors != null) {
                    var modified = false
                    for (i in gradientColors.indices) {
                        val color = gradientColors[i]
                        val newColor = IColors.getFromIntColor(color, colors)
                        if (color != newColor) {
                            gradientColors[i] = newColor
                            modified = true
                        }
                    }
                    if (modified) {
                        drawable.colors = gradientColors
                    }
                }
            }
            is DrawableWrapper -> {
                replaceColor(drawable.drawable, colors)
            }
            is NinePatchDrawable -> {
                val color = getNinePatchDrawableColor(drawable)
                val newColor = IColors.getFromIntColor(color, colors)
                if (color != newColor) {
                    drawable.setTintList(ColorStateList.valueOf(newColor))
                }
            }
            is ColorDrawable -> {
                val color = getColorDrawableColor(drawable)
                val newColor = IColors.getFromIntColor(color, colors)
                if (newColor != color) {
                    drawable.color = newColor
                }
            }
            is ShapeDrawable -> {
                val color = getShapeDrawableColor(drawable)
                val newColor = IColors.getFromIntColor(color, colors)
                if (color != newColor) {
                    drawable.paint.color = newColor
                }
            }
            is LevelListDrawable -> {
                val count = XposedHelpers.callMethod(drawable, "getNumberOfLevels") as Int
                for (i in 0 until count) {
                    val levelDrawable = XposedHelpers.callMethod(drawable, "getDrawable", i) as? Drawable
                    if (levelDrawable != null) {
                        replaceColor(levelDrawable, colors)
                    }
                }
            }
            is TransitionDrawable -> {
                val count = drawable.numberOfLayers
                for (i in 0 until count) {
                    val layerDrawable = drawable.getDrawable(i)
                    if (layerDrawable != null) {
                        replaceColor(layerDrawable, colors)
                    }
                }
            }
            is LayerDrawable -> {
                val layerState = drawable.constantState
                val mChildren = XposedHelpers.getObjectField(layerState, "mChildren") as Array<*>
                for (childDrawable in mChildren) {
                    if (childDrawable != null) {
                        val d = XposedHelpers.getObjectField(childDrawable, "mDrawable") as? Drawable
                        if (d != null) {
                            replaceColor(d, colors)
                        }
                    }
                }
            }
            is DrawableContainer -> {
                val containerState = drawable.constantState
                val drawables = XposedHelpers.getObjectField(containerState, "mDrawables") as Array<Drawable?>
                for (d in drawables) {
                    if (d != null) {
                        replaceColor(d, colors)
                    }
                }
            }
            else -> {
                val materialCls = getMaterialShapeDrawable()
                if (materialCls != null && materialCls.isInstance(drawable)) {
                    val state = drawable.constantState as Drawable.ConstantState
                    val colorStateListFields = ReflectionUtils.findAllFieldsUsingFilter(materialCls) { field ->
                        field.type == ColorStateList::class.java
                    }
                    for (colorStateField in colorStateListFields) {
                        val colorStateList = ReflectionUtils.getObjectField(colorStateField, state) as? ColorStateList
                        if (colorStateList == null) continue
                        val color = colorStateList.defaultColor
                        val newColor = IColors.getFromIntColor(color, colors)
                        if (color != newColor) {
                            val colorStateListNew = ColorStateList.valueOf(newColor)
                            ReflectionUtils.setObjectField(colorStateField, state, colorStateListNew)
                        }
                    }
                    val paintFields = ReflectionUtils.getFieldsByType(materialCls, Paint::class.java)
                    for (paintField in paintFields) {
                        val paint = ReflectionUtils.getObjectField(paintField, drawable) as? Paint ?: continue
                        val color = paint.color
                        val newColor = IColors.getFromIntColor(color, colors)
                        if (color != newColor) {
                            paint.color = newColor
                        }
                    }
                }
            }
        }
    }

    private fun getMaterialShapeDrawable(): Class<*>? {
        if (mMaterialShapeDrawableClass == null) {
            try {
                mMaterialShapeDrawableClass = Unobfuscator.loadMaterialShapeDrawableClass(Utils.getApplication().classLoader)
            } catch (e: Exception) {
                return null
            }
        }
        return mMaterialShapeDrawableClass
    }

    @JvmStatic
    fun getColor(drawable: Drawable?): Int {
        if (drawable == null) return 0

        return when (drawable) {
            is ColorDrawable -> getColorDrawableColor(drawable)
            is ShapeDrawable -> getShapeDrawableColor(drawable)
            is RippleDrawable -> getRippleDrawableColor(drawable)
            is NinePatchDrawable -> getNinePatchDrawableColor(drawable)
            is InsetDrawable -> getInsetDrawableColor(drawable)
            else -> 0
        }
    }

    private fun getInsetDrawableColor(insetDrawable: InsetDrawable): Int {
        val mDrawable = XposedHelpers.getObjectField(insetDrawable, "mDrawable") as? Drawable
        return getColor(mDrawable)
    }

    @JvmStatic
    fun getNinePatchDrawableColor(ninePatchDrawable: NinePatchDrawable): Int {
        val state = ninePatchDrawable.constantState
        val ninePatch = XposedHelpers.getObjectField(state, "mNinePatch") as NinePatch
        val bitmap = ninePatch.bitmap
        val corSalva = ninePatchs[bitmap]
        if (corSalva != null) return corSalva

        val contagemCores = HashMap<Int, Int>()
        var corMaisFrequente = 0
        var contagemMaxima = 0

        for (x in 0 until bitmap.width) {
            for (y in 0 until bitmap.height) {
                val cor = bitmap.getPixel(x, y)
                val contagemAtual = (contagemCores[cor] ?: 0) + 1
                contagemCores[cor] = contagemAtual

                if (contagemAtual > contagemMaxima) {
                    corMaisFrequente = cor
                    contagemMaxima = contagemAtual
                }
            }
        }
        ninePatchs[bitmap] = corMaisFrequente
        return corMaisFrequente
    }

    private fun getRippleDrawableColor(rippleDrawable: RippleDrawable): Int {
        val state = rippleDrawable.constantState
        return try {
            val color = XposedHelpers.getObjectField(state, "mColor") as ColorStateList
            color.defaultColor
        } catch (e: Exception) {
            XposedBridge.log(e)
            0
        }
    }

    @JvmStatic
    fun getColorDrawableColor(colorDrawable: ColorDrawable): Int {
        return colorDrawable.color
    }

    @JvmStatic
    fun getShapeDrawableColor(shapeDrawable: ShapeDrawable): Int {
        return shapeDrawable.paint.color
    }
}
