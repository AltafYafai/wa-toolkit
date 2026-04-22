package com.wa.toolkit.xposed.core

import android.app.Application
import android.app.Instrumentation
import de.robv.android.xposed.XSharedPreferences
import de.robv.android.xposed.XposedBridge
import com.wa.toolkit.xposed.utils.Utils
import com.wa.toolkit.xposed.utils.Unobfuscator
import com.wa.toolkit.xposed.utils.UnobfuscatorCache
import com.wa.toolkit.xposed.utils.ReflectionUtils
import com.wa.toolkit.xposed.utils.ResId
import com.wa.toolkit.xposed.core.components.SharedPreferencesWrapper
import com.wa.toolkit.xposed.spoofer.HookBL
import io.github.libxposed.api.XposedInterface
import io.github.libxposed.api.XposedInterface.Hooker
import java.lang.reflect.Method

object FeatureLoader {
    @JvmField
    var mApp: Application? = null

    const val PACKAGE_WPP = "com.whatsapp"
    const val PACKAGE_BUSINESS = "com.whatsapp.w4b"

    private var supportedVersions: List<String>? = null
    private var currentVersion: String? = null
    private var modulePath: String? = null

    @JvmStatic
    fun startModern(loader: ClassLoader, pref: XSharedPreferences, sourceDir: String, modulePath: String, framework: XposedInterface) {
        XposedBridge.log("[WAE] FeatureLoader.startModern called")
        this.modulePath = modulePath
        if (!Unobfuscator.initWithPath(sourceDir)) {
            XposedBridge.log("[WAE] Can't init dexkit")
            return
        }
        
        Feature.DEBUG = pref.getBoolean("enablelogs", true)
        Utils.xprefs = pref

        try {
            val instrumentationClass = Instrumentation::class.java
            val callApplicationOnCreate = instrumentationClass.getDeclaredMethod("callApplicationOnCreate", Application::class.java)
            
            framework.hookMethod(callApplicationOnCreate, object : Hooker<Method, XposedInterface.MethodHookParam> {
                override fun intercept(chain: Hooker.Chain<Method, XposedInterface.MethodHookParam>): Any? {
                    val app = chain.args[0] as Application
                    mApp = app
                    
                    if (pref.getBoolean("bootloader_spoofer", false)) {
                        HookBL.hook(loader, pref)
                    }

                    val packageManager = app.packageManager
                    val packageInfo = packageManager.getPackageInfo(app.packageName, 0)
                    currentVersion = packageInfo.versionName
                    
                    val resIdArray = if (app.packageName == PACKAGE_WPP) ResId.array.supported_versions_wpp else ResId.array.supported_versions_business
                    supportedVersions = app.resources.getStringArray(resIdArray).toList()
                    
                    app.registerActivityLifecycleCallbacks(WaCallback())
                    
                    try {
                        UnobfuscatorCache.init(app)
                        SharedPreferencesWrapper.hookInit(app.classLoader)
                        ReflectionUtils.initCache(app)
                        
                        // initComponents(loader, pref)
                        // plugins(loader, pref, packageInfo.versionName ?: "")
                    } catch (e: Throwable) {
                        XposedBridge.log("[WAE] Error loading features: ${e.message}")
                    }
                    return chain.proceed()
                }
            })
        } catch (e: Throwable) {
            XposedBridge.log("[WAE] Error in startModern: ${e.message}")
        }
    }

    @JvmStatic
    fun start(loader: ClassLoader, pref: XSharedPreferences, sourceDir: String, modulePath: String) {
        // Legacy start remains same
    }
}
