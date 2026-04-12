package com.wa.toolkit.xposed.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Pair
import de.robv.android.xposed.XposedHelpers
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Method

@Suppress("unused")
object ReflectionUtils {

    private var cachePrefs: SharedPreferences? = null

    /**
     * Initialize the SharedPreferences for caching reflection results
     *
     * @param context Application context
     */
    @JvmStatic
    fun initCache(context: Context) {
        if (cachePrefs == null) {
            cachePrefs = context.getSharedPreferences("UnobfuscatorCache", Context.MODE_PRIVATE)
        }
    }

    @JvmField
    val primitiveClasses: Map<String, Class<*>> = mapOf(
        "byte" to Byte::class.javaPrimitiveType!!,
        "short" to Short::class.javaPrimitiveType!!,
        "int" to Int::class.javaPrimitiveType!!,
        "long" to Long::class.javaPrimitiveType!!,
        "float" to Float::class.javaPrimitiveType!!,
        "boolean" to Boolean::class.javaPrimitiveType!!
    )

    @JvmStatic
    fun findClass(className: String, classLoader: ClassLoader?): Class<*> {
        val primitive = primitiveClasses[className]
        if (primitive != null) return primitive
        return XposedHelpers.findClass(className, classLoader)
    }

    @JvmStatic
    fun findMethodUsingFilter(clazz: Class<*>, predicate: (Method) -> Boolean): Method {
        var currentClass: Class<*>? = clazz
        while (currentClass != null) {
            val result = currentClass.declaredMethods.find(predicate)
            if (result != null) return result
            currentClass = currentClass.superclass
        }
        throw RuntimeException("Method not found")
    }

    @JvmStatic
    fun findAllMethodsUsingFilter(clazz: Class<*>, predicate: (Method) -> Boolean): Array<Method> {
        var currentClass: Class<*>? = clazz
        while (currentClass != null) {
            val results = currentClass.declaredMethods.filter(predicate)
            if (results.isNotEmpty()) return results.toTypedArray()
            currentClass = currentClass.superclass
        }
        throw RuntimeException("Method not found")
    }

    @JvmStatic
    fun findFieldUsingFilter(clazz: Class<*>, predicate: (Field) -> Boolean): Field {
        var currentClass: Class<*>? = clazz
        while (currentClass != null) {
            val result = currentClass.declaredFields.find(predicate)
            if (result != null) return result
            currentClass = currentClass.superclass
        }
        throw RuntimeException("Field not found")
    }

    @JvmStatic
    fun findAllConstructorsUsingFilter(clazz: Class<*>, predicate: (Constructor<*>) -> Boolean): Array<Constructor<*>> {
        var currentClass: Class<*>? = clazz
        while (currentClass != null) {
            val results = currentClass.declaredConstructors.filter(predicate)
            if (results.isNotEmpty()) return results.toTypedArray()
            currentClass = currentClass.superclass
        }
        return emptyArray()
    }

    @JvmStatic
    fun findConstructorUsingFilter(clazz: Class<*>, predicate: (Constructor<*>) -> Boolean): Constructor<*> {
        var currentClass: Class<*>? = clazz
        while (currentClass != null) {
            val result = currentClass.declaredConstructors.find(predicate)
            if (result != null) return result
            currentClass = currentClass.superclass
        }
        throw RuntimeException("Constructor not found")
    }

    @JvmStatic
    fun findAllFieldsUsingFilter(clazz: Class<*>, predicate: (Field) -> Boolean): Array<Field> {
        var currentClass: Class<*>? = clazz
        while (currentClass != null) {
            val results = currentClass.declaredFields.filter(predicate)
            if (results.isNotEmpty()) return results.toTypedArray()
            currentClass = currentClass.superclass
        }
        return emptyArray()
    }

    @JvmStatic
    fun findMethodUsingFilterIfExists(clazz: Class<*>, predicate: (Method) -> Boolean): Method? {
        var currentClass: Class<*>? = clazz
        while (currentClass != null) {
            val result = currentClass.declaredMethods.find(predicate)
            if (result != null) return result
            currentClass = currentClass.superclass
        }
        return null
    }

    @JvmStatic
    fun findFieldUsingFilterIfExists(clazz: Class<*>, predicate: (Field) -> Boolean): Field? {
        var currentClass: Class<*>? = clazz
        while (currentClass != null) {
            val result = currentClass.declaredFields.find(predicate)
            if (result != null) return result
            currentClass = currentClass.superclass
        }
        return null
    }

    @JvmStatic
    fun isOverridden(method: Method): Boolean {
        return try {
            val superclass = method.declaringClass.superclass ?: return false
            val parentMethod = superclass.getMethod(method.name, *method.parameterTypes)
            parentMethod != method
        } catch (e: NoSuchMethodException) {
            false
        }
    }

    @JvmStatic
    fun getFieldsByExtendType(cls: Class<*>, type: Class<*>): List<Field> {
        return cls.fields.filter { type.isAssignableFrom(it.type) }
    }

    @JvmStatic
    fun getFieldsByType(cls: Class<*>, type: Class<*>): List<Field> {
        return cls.fields.filter { type == it.type }
    }

    @JvmStatic
    fun getFieldByExtendType(cls: Class<*>?, className: String?): Field? {
        if (cls == null || className == null) return null
        return getFieldByExtendType(cls, findClass(className, cls.classLoader))
    }

    @JvmStatic
    fun getFieldByExtendType(cls: Class<*>, type: Class<*>): Field? {
        val prefs = cachePrefs
        if (prefs == null) {
            return cls.fields.find { type.isAssignableFrom(it.type) }
        }

        val cacheKey = "field_cache_${cls.name}_${type.name}"
        val cachedFieldName = prefs.getString(cacheKey, null)
        if (cachedFieldName != null) {
            try {
                return cls.getField(cachedFieldName)
            } catch (e: NoSuchFieldException) {
                prefs.edit().remove(cacheKey).commit()
            }
        }

        val field = cls.fields.find { type.isAssignableFrom(it.type) }
        if (field != null && field.declaringClass == cls) {
            prefs.edit().putString(cacheKey, field.name).commit()
        }

        return field
    }

    @JvmStatic
    fun getFieldByType(cls: Class<*>?, className: String?): Field? {
        if (cls == null || className == null) return null
        return getFieldByType(cls, findClass(className, cls.classLoader))
    }

    @JvmStatic
    fun getFieldByType(cls: Class<*>, type: Class<*>): Field? {
        val prefs = cachePrefs
        if (prefs == null) {
            return cls.fields.find { type == it.type }
        }

        val cacheKey = "field_cache_direct_${cls.name}_${type.name}"
        val cachedFieldName = prefs.getString(cacheKey, null)
        if (cachedFieldName != null) {
            try {
                return cls.getField(cachedFieldName)
            } catch (e: NoSuchFieldException) {
                prefs.edit().remove(cacheKey).apply()
            }
        }

        val field = cls.fields.find { type == it.type }
        if (field != null && field.declaringClass == cls) {
            prefs.edit().putString(cacheKey, field.name).apply()
        }

        return field
    }

    @JvmStatic
    fun callMethod(method: Method, instance: Any?, vararg args: Any?): Any? {
        return try {
            val count = method.parameterCount
            var finalArgs = args
            if (count != args.size) {
                val newArgs = initArray(method.parameterTypes)
                System.arraycopy(args, 0, newArgs, 0, Math.min(args.size, count))
                finalArgs = newArgs
            }
            method.invoke(instance, *finalArgs)
        } catch (e: Exception) {
            null
        }
    }

    @JvmStatic
    fun initArray(parameterTypes: Array<Class<*>>): Array<Any?> {
        val args = arrayOfNulls<Any>(parameterTypes.size)
        for (i in parameterTypes.indices) {
            args[i] = getDefaultValue(parameterTypes[i])
        }
        return args
    }

    @JvmStatic
    fun getDefaultValue(paramType: Class<*>): Any? {
        return when (paramType) {
            Int::class.javaPrimitiveType, Int::class.java -> 0
            Long::class.javaPrimitiveType, Long::class.java -> 0L
            Double::class.javaPrimitiveType, Double::class.java -> 0.0
            Boolean::class.javaPrimitiveType, Boolean::class.java -> false
            else -> null
        }
    }

    @JvmStatic
    fun getObjectField(field: Field, thisObject: Any?): Any? {
        return try {
            field.get(thisObject)
        } catch (e: Exception) {
            null
        }
    }

    @JvmStatic
    fun findIndexOfType(args: Array<Any?>, type: Class<*>): Int {
        for (i in args.indices) {
            val arg = args[i] ?: continue
            if (arg is Class<*>) {
                if (type.isAssignableFrom(arg)) return i
                continue
            }
            if (type.isInstance(arg)) return i
        }
        return -1
    }

    @JvmStatic
    fun <T> findInstancesOfType(args: Array<Any?>, type: Class<T>): List<Pair<Int, T>> {
        val result = mutableListOf<Pair<Int, T>>()
        for (i in args.indices) {
            val arg = args[i]
            if (arg == null || arg is Class<*>) continue

            if (type.isInstance(arg)) {
                result.add(Pair(i, type.cast(arg)!!))
            }
        }
        return result
    }

    @JvmStatic
    fun <T> findClassesOfType(args: Array<Class<*>>, type: Class<T>): List<Pair<Int, Class<out T>>> {
        val result = mutableListOf<Pair<Int, Class<out T>>>()
        for (i in args.indices) {
            val arg = args[i]
            if (type.isAssignableFrom(arg)) {
                @Suppress("UNCHECKED_CAST")
                result.add(Pair(i, arg as Class<out T>))
            }
        }
        return result
    }

    @JvmStatic
    fun <T> getArg(args: Array<Any?>, typeClass: Class<T>, i: Int): T? {
        val list = findInstancesOfType(args, typeClass)
        if (list.size <= i) return null
        return list[i].second
    }

    @JvmStatic
    fun isCalledFromString(contains: String): Boolean {
        val trace = Thread.currentThread().stackTrace
        val text = trace.contentToString()
        return text.contains(contains)
    }

    @JvmStatic
    fun isCalledFromStrings(vararg contains: String): Boolean {
        val trace = Thread.currentThread().stackTrace
        val text = trace.contentToString()
        for (s in contains) {
            if (text.contains(s)) return true
        }
        return false
    }

    @JvmStatic
    fun isClassSimpleNameString(aClass: Class<*>, s: String): Boolean {
        return try {
            var cls: Class<*>? = aClass
            while (cls != null) {
                if (cls.simpleName == s) return true
                if (cls.name.startsWith("android.widget.") || cls.name.startsWith("android.view.")) return false
                cls = cls.superclass
            }
            false
        } catch (ignored: Exception) {
            false
        }
    }

    @JvmStatic
    fun isCalledFromClass(cls: Class<*>): Boolean {
        val trace = Thread.currentThread().stackTrace
        for (stackTraceElement in trace) {
            if (stackTraceElement.className == cls.name) return true
        }
        return false
    }

    @JvmStatic
    fun isCalledFromMethod(method: Method): Boolean {
        val trace = Thread.currentThread().stackTrace
        val className = method.declaringClass.name
        val methodName = method.name
        for (stackTraceElement in trace) {
            if (stackTraceElement.className == className && stackTraceElement.methodName == methodName) return true
        }
        return false
    }

    @JvmStatic
    fun setObjectField(field: Field, instance: Any?, value: Any?) {
        try {
            field.set(instance, value)
        } catch (ignored: Exception) {
        }
    }
}
