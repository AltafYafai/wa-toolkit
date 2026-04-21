package com.wa.toolkit.xposed.core

import android.content.Context
import com.wa.toolkit.BuildConfig
import dalvik.system.DexFile
import java.io.File
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.ConcurrentHashMap
import java.lang.reflect.Member
import java.lang.reflect.Modifier
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XSharedPreferences
import de.robv.android.xposed.XposedBridge

object FeatureManager {
    private val features = mutableListOf<Class<out Feature>>()
    private val errors = Vector<FeatureLoader.ErrorItem>()
    private val loadTimes = Vector<String>()
    private val hookRegistry = ConcurrentHashMap<Member, MutableList<XC_MethodHook.Unhook>>()

    fun register(clazz: Class<out Feature>) {
        features.add(clazz)
    }

    @Synchronized
    fun safeHookMethod(method: Member?, callback: XC_MethodHook): XC_MethodHook.Unhook? {
        if (method == null) return null
        return try {
            val unhook = XposedBridge.hookMethod(method, callback)
            hookRegistry.computeIfAbsent(method) { mutableListOf() }.add(unhook)
            unhook
        } catch (e: Throwable) {
            XposedBridge.log("Failed to hook method ${method.name}: ${e.message}")
            null
        }
    }

    fun registerAll(classes: Array<Class<out Any>>) {
        classes.forEach { 
            if (Feature::class.java.isAssignableFrom(it)) {
                @Suppress("UNCHECKED_CAST")
                register(it as Class<out Feature>)
            }
        }
    }

    fun discoverFeatures(context: Context) {
        try {
            val apkPath = context.applicationInfo.sourceDir
            val dexFile = DexFile(apkPath)
            val entries = dexFile.entries()
            while (entries.hasMoreElements()) {
                val className = entries.nextElement()
                if (className.startsWith("com.wa.toolkit.xposed.features.")) {
                    try {
                        val clazz = Class.forName(className, false, context.classLoader)
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
                    val feature = constructor.newInstance(loader, pref)
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
