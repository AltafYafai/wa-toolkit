package com.wa.toolkit.xposed.core

import android.content.Context
import com.wa.toolkit.BuildConfig
import dalvik.system.DexFile
import java.io.File
import java.util.*
import java.util.Enumeration
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.ConcurrentHashMap
import java.lang.reflect.Member
import java.lang.reflect.Modifier
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XSharedPreferences
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers

object FeatureManager {
    private val features = mutableListOf<Class<out Feature>>()
    private val errors = Vector<FeatureLoader.ErrorItem>()
    private val loadTimes = Vector<String>()
    private val hookRegistry = ConcurrentHashMap<Member, MutableList<Any>>()

    @JvmStatic
    var xposedModule: Any? = null

    fun register(clazz: Class<out Feature>) {
        features.add(clazz)
    }

    @Synchronized
    @JvmStatic
    fun safeHookMethod(method: Member?, callback: XC_MethodHook): Any? {
        if (method == null) return null
        
        val module = xposedModule
        if (module is io.github.libxposed.api.XposedModule && method is java.lang.reflect.Method) {
            try {
                XposedBridge.log("Using Modern Hook for ${method.name}")
                val hook = module.hook(method).intercept { chain ->
                    val param = XposedHelpers.newInstance(XC_MethodHook.MethodHookParam::class.java) as XC_MethodHook.MethodHookParam
                    param.method = method
                    param.thisObject = chain.thisObject
                    param.args = chain.args.toTypedArray()

                    // Call beforeHookedMethod via reflection since it's protected
                    val beforeMethod = XC_MethodHook::class.java.getDeclaredMethod("beforeHookedMethod", XC_MethodHook.MethodHookParam::class.java)
                    beforeMethod.isAccessible = true
                    beforeMethod.invoke(callback, param)

                    if (param.hasThrowable()) {
                        throw param.throwable
                    }

                    // Check for early return
                    val returnEarlyField = XC_MethodHook.MethodHookParam::class.java.getDeclaredField("returnEarly")
                    returnEarlyField.isAccessible = true
                    if (returnEarlyField.getBoolean(param)) {
                        return@intercept param.result
                    }

                    val result = chain.proceed()
                    param.result = result

                    // Call afterHookedMethod via reflection
                    val afterMethod = XC_MethodHook::class.java.getDeclaredMethod("afterHookedMethod", XC_MethodHook.MethodHookParam::class.java)
                    afterMethod.isAccessible = true
                    afterMethod.invoke(callback, param)

                    if (param.hasThrowable()) {
                        throw param.throwable
                    }

                    param.result
                }
                hookRegistry.computeIfAbsent(method) { mutableListOf() }.add(hook)
                return hook
            } catch (e: Throwable) {
                XposedBridge.log("Failed to hook method (Modern) ${method.name}: ${e.message}")
                // Fallback to legacy if possible (might still fail)
            }
        }

        return try {
            val unhook = XposedBridge.hookMethod(method, callback)
            hookRegistry.computeIfAbsent(method) { mutableListOf() }.add(unhook)
            unhook
        } catch (e: Throwable) {
            XposedBridge.log("Failed to hook method (Legacy) ${method.name}: ${e.message}")
            null
        }
    }

    @Synchronized
    @JvmStatic
    fun safeFindAndHookMethod(clazz: Class<*>, methodName: String, vararg parameterTypesAndCallback: Any?): Any? {
        return try {
            val callback = parameterTypesAndCallback.last() as XC_MethodHook
            val parameterTypes = parameterTypesAndCallback.take(parameterTypesAndCallback.size - 1).toTypedArray()
            val method = XposedHelpers.findMethodExact(clazz, methodName, *parameterTypes)
            safeHookMethod(method, callback)
        } catch (e: Throwable) {
            XposedBridge.log("Failed to find and hook method ${clazz.name}#$methodName: ${e.message}")
            null
        }
    }

    @Synchronized
    @JvmStatic
    fun safeFindAndHookMethod(className: String, loader: ClassLoader, methodName: String, vararg parameterTypesAndCallback: Any?): Any? {
        return try {
            val clazz = XposedHelpers.findClass(className, loader)
            safeFindAndHookMethod(clazz, methodName, *parameterTypesAndCallback)
        } catch (e: Throwable) {
            XposedBridge.log("Failed to find and hook method $className#$methodName: ${e.message}")
            null
        }
    }

    @Synchronized
    @JvmStatic
    fun safeHookAllConstructors(clazz: Class<*>, callback: XC_MethodHook): List<Any> {
        val hooks = mutableListOf<Any>()
        clazz.declaredConstructors.forEach { constructor ->
            val hook = safeHookMethod(constructor, callback)
            if (hook != null) hooks.add(hook)
        }
        return hooks
    }

    fun registerAll(classes: Array<Class<out Any>>) {
        classes.forEach { 
            if (Feature::class.java.isAssignableFrom(it)) {
                @Suppress("UNCHECKED_CAST")
                register(it as Class<out Feature>)
            }
        }
    }

    fun discoverFeatures(path: String, loader: ClassLoader) {
        try {
            val dexFile = DexFile(path)
            val entries = dexFile.entries()
            while (entries.hasMoreElements()) {
                val className = entries.nextElement()
                if (className.startsWith("com.wa.toolkit.xposed.features.")) {
                    try {
                        val clazz = Class.forName(className, false, loader)
                        if (Feature::class.java.isAssignableFrom(clazz) && !java.lang.reflect.Modifier.isAbstract(clazz.modifiers)) {
                            @Suppress("UNCHECKED_CAST")
                            register(clazz as Class<out Feature>)
                        }
                    } catch (e: ClassNotFoundException) {
                        // Ignore classes that cannot be loaded
                    } catch (e: NoClassDefFoundError) {
                         // Ignore classes that have missing dependencies
                    }
                }
            }
        } catch (e: Exception) {
            XposedBridge.log("Failed to dynamically discover features: " + e.message)
        }
    }


    fun loadAll(loader: ClassLoader, pref: XSharedPreferences, versionWpp: String): List<FeatureLoader.ErrorItem> {
        errors.clear()
        loadTimes.clear()
        
        val executorService = Executors.newWorkStealingPool(minOf(Runtime.getRuntime().availableProcessors(), 4))
        
        features.forEach { clazz ->
            CompletableFuture.runAsync({
                val startTime = System.currentTimeMillis()
                try {
                    val constructor = clazz.getConstructor(ClassLoader::class.java, XSharedPreferences::class.java)
                    val feature = constructor.newInstance(loader, pref) as Feature
                    feature.doHook()
                } catch (e: Throwable) {
                    XposedBridge.log(e)
                    val error = FeatureLoader.ErrorItem().apply {
                        pluginName = clazz.simpleName
                        whatsAppVersion = versionWpp
                        moduleVersion = BuildConfig.VERSION_NAME
                        message = e.message
                        this.error = e.stackTrace
                            .filter { !it.className.startsWith("android") && !it.className.startsWith("com.android") }
                            .joinToString(prefix = "[", postfix = "]") { it.toString() }
                    }
                    errors.add(error)
                }
                val loadTime = System.currentTimeMillis() - startTime
                loadTimes.add("* Loaded Plugin ${clazz.simpleName} in ${loadTime}ms")
            }, executorService)
        }
        
        executorService.shutdown()
        executorService.awaitTermination(15, TimeUnit.SECONDS)
        
        if (Feature.DEBUG) {
            loadTimes.forEach { XposedBridge.log(it) }
        }
        
        return errors.toList()
    }
}
