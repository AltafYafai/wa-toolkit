package com.wa.toolkit.xposed.utils

import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import java.nio.charset.StandardCharsets
import java.util.*

object DebugUtils {
    @JvmStatic
    fun debugFields(cls: Class<*>?, thisObject: Any?) {
        if (cls == null) return
        XposedBridge.log("------------------------------------")
        XposedBridge.log("DEBUG FIELDS: Class ${cls.name} -> Object $thisObject")
        for (field in cls.declaredFields) {
            try {
                field.isAccessible = true
                val name = field.name
                var value = field.get(thisObject)
                if (value != null && value::class.java.isArray) {
                    value = (value as? Array<*>)?.contentToString() ?: value.toString()
                }
                XposedBridge.log("FIELD: $name -> TYPE: ${field.type.name} -> VALUE: $value")
            } catch (ignored: Exception) {
            }
        }
    }

    @JvmStatic
    fun debugAllMethods(className: String, methodName: String, printMethods: Boolean, printFields: Boolean, printArgs: Boolean, printTrace: Boolean) {
        val clazz = XposedHelpers.findClass(className, Utils.getApplication().classLoader)
        XposedBridge.hookAllMethods(clazz, methodName, getDebugMethodHook(printMethods, printFields, printArgs, printTrace))
    }

    @JvmStatic
    fun getDebugMethodHook(printMethods: Boolean, printFields: Boolean, printArgs: Boolean, printTrace: Boolean): XC_MethodHook {
        return object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun afterHookedMethod(param: MethodHookParam) {
                XposedBridge.log("-----------------HOOKED DEBUG START-----------------------------")
                XposedBridge.log("DEBUG CLASS: ${param.method.declaringClass.name}->${param.method.name}: ${param.thisObject}")

                if (printArgs) {
                    debugArgs(param.args)
                    XposedBridge.log("Return value: ${param.result?.javaClass?.name} -> VALUE: ${param.result}")
                }

                if (printFields) {
                    debugFields(if (param.thisObject == null) param.method.declaringClass else param.thisObject.javaClass, param.thisObject)
                }

                if (printMethods) {
                    debugMethods(if (param.thisObject == null) param.method.declaringClass else param.thisObject.javaClass, param.thisObject)
                }

                if (printTrace) {
                    for (trace in Thread.currentThread().stackTrace) {
                        XposedBridge.log("TRACE: $trace")
                    }
                }

                XposedBridge.log("-----------------HOOKED DEBUG END-----------------------------\n\n")
            }
        }
    }

    @JvmStatic
    fun debugArgs(args: Array<Any?>) {
        for (i in args.indices) {
            XposedBridge.log("ARG[$i]: ${args[i]?.javaClass?.name} -> VALUE: ${parseValue(args[i])}")
        }
    }

    @JvmStatic
    fun parseValue(value: Any?): String {
        val sb = StringBuilder()
        if (value == null) return "null"
        
        when (value) {
            is List<*> -> {
                sb.append("List[")
                for (item in value) {
                    sb.append(parseValue(item)).append(", ")
                }
                sb.append("]")
            }
            is Map<*, *> -> {
                sb.append("Map[")
                for ((key, v) in value) {
                    sb.append(key).append(": ").append(parseValue(v)).append(" ")
                }
                sb.append("]")
            }
            is ByteArray -> {
                try {
                    sb.append(String(value, StandardCharsets.UTF_8))
                } catch (ignored: Exception) {
                }
            }
            else -> {
                sb.append(value)
            }
        }
        return sb.toString()
    }

    @JvmStatic
    fun debugMethods(cls: Class<*>?, thisObject: Any?) {
        if (cls == null) return
        XposedBridge.log("DEBUG METHODS: Class ${cls.name}")
        for (method in cls.declaredMethods) {
            if (method.parameterCount > 0 || method.returnType == Void.TYPE) continue
            try {
                method.isAccessible = true
                XposedBridge.log("METHOD: ${method.name} -> VALUE: ${method.invoke(thisObject)}")
            } catch (ignored: Exception) {
            }
        }
    }

    @JvmStatic
    fun debugObject(srj: Any?) {
        if (srj == null) return
        XposedBridge.log("DEBUG OBJECT: ${srj.javaClass.name}")
        debugFields(srj.javaClass, srj)
        debugMethods(srj.javaClass, srj)
    }
}
