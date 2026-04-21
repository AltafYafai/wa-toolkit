package com.wa.toolkit.xposed.utils

import android.content.res.XModuleResources
import com.wa.toolkit.R
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_InitPackageResources
import java.lang.reflect.Field

object ResourceMirror {

    private val drawableFields: Array<Field> by lazy {
        ResId.drawable::class.java.declaredFields.filter { 
            it.name != "INSTANCE" && it.name != "\$stable" 
        }.toTypedArray()
    }

    private val stringFields: Array<Field> by lazy {
        ResId.string::class.java.declaredFields.filter { 
            it.name != "INSTANCE" && it.name != "\$stable" 
        }.toTypedArray()
    }

    private val arrayFields: Array<Field> by lazy {
        ResId.array::class.java.declaredFields.filter { 
            it.name != "INSTANCE" && it.name != "\$stable" 
        }.toTypedArray()
    }

    private val idFields: Array<Field> by lazy {
        ResId.id::class.java.declaredFields.filter { 
            it.name != "INSTANCE" && it.name != "\$stable" 
        }.toTypedArray()
    }

    fun mirror(resParam: XC_InitPackageResources.InitPackageResourcesParam, modRes: XModuleResources) {
        mirrorFields(resParam, modRes, drawableFields, R.drawable::class.java, "drawable")
        mirrorFields(resParam, modRes, stringFields, R.string::class.java, "string")
        mirrorFields(resParam, modRes, arrayFields, R.array::class.java, "array")
        mirrorFields(resParam, modRes, idFields, R.id::class.java, "id")
    }

    private fun mirrorFields(
        resParam: XC_InitPackageResources.InitPackageResourcesParam,
        modRes: XModuleResources,
        fields: Array<Field>,
        rClass: Class<*>,
        type: String
    ) {
        for (field in fields) {
            try {
                val rField = rClass.getField(field.name)
                val resId = resParam.res.addResource(modRes, rField.getInt(null))
                field.isAccessible = true
                field.set(null, resId)
                if (type == "drawable") {
                    XposedBridge.log("[•] Mirrored $type: ${field.name} -> ${Integer.toHexString(resId)}")
                }
            } catch (e: Exception) {
                XposedBridge.log("[•] Failed to mirror $type: ${field.name} - ${e.message}")
            }
        }
    }
}
