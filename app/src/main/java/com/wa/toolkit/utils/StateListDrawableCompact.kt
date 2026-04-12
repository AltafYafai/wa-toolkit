package com.wa.toolkit.utils

import android.graphics.drawable.Drawable
import android.graphics.drawable.StateListDrawable
import android.os.Build
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import java.lang.reflect.Method

object StateListDrawableCompact {
    private val mClass = StateListDrawable::class.java

    @JvmStatic
    fun getStateCount(stateListDrawable: StateListDrawable): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            stateListDrawable.stateCount
        } else {
            try {
                val method = XposedHelpers.findMethodBestMatch(mClass, "getStateCount")
                if (method != null) {
                    val invoke = method.invoke(stateListDrawable)
                    if (invoke is Int) {
                        return invoke
                    }
                }
            } catch (e: Exception) {
                XposedBridge.log(e)
            }
            0
        }
    }

    @JvmStatic
    fun getStateDrawable(stateListDrawable: StateListDrawable, i: Int): Drawable? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            stateListDrawable.getStateDrawable(i)
        } else {
            try {
                val method = XposedHelpers.findMethodBestMatch(mClass, "getStateDrawable", Int::class.javaPrimitiveType)
                if (method != null) {
                    val invoke = method.invoke(stateListDrawable, i)
                    if (invoke is Drawable) {
                        return invoke
                    }
                }
            } catch (e: Exception) {
                XposedBridge.log(e)
            }
            null
        }
    }
}
