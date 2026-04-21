package com.wa.toolkit.xposed.core

import android.app.Activity
import android.app.Application
import android.app.Instrumentation
import android.content.*
import android.content.pm.PackageInfo
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.wa.toolkit.App
import com.wa.toolkit.BuildConfig
import com.wa.toolkit.UpdateChecker
import com.wa.toolkit.xposed.core.components.AlertDialogWpp
import com.wa.toolkit.xposed.core.components.FMessageWpp
import com.wa.toolkit.xposed.core.components.SharedPreferencesWrapper
import com.wa.toolkit.xposed.core.components.WaContactWpp
import com.wa.toolkit.xposed.core.devkit.Unobfuscator
import com.wa.toolkit.xposed.core.devkit.UnobfuscatorCache
import com.wa.toolkit.xposed.features.customization.*
import com.wa.toolkit.xposed.features.general.*
import com.wa.toolkit.xposed.features.listeners.ContactItemListener
import com.wa.toolkit.xposed.features.listeners.ConversationItemListener
import com.wa.toolkit.xposed.features.listeners.MenuStatusListener
import com.wa.toolkit.xposed.features.media.*
import com.wa.toolkit.xposed.features.others.*
import com.wa.toolkit.xposed.features.privacy.*
import com.wa.toolkit.xposed.spoofer.HookBL
import com.wa.toolkit.xposed.utils.DesignUtils
import com.wa.toolkit.xposed.utils.ReflectionUtils
import com.wa.toolkit.xposed.utils.ResId
import com.wa.toolkit.xposed.utils.Utils
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XSharedPreferences
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.stream.Collectors

object FeatureLoader {
    @JvmField
    var mApp: Application? = null

    const val PACKAGE_WPP = "com.whatsapp"
    const val PACKAGE_BUSINESS = "com.whatsapp.w4b"

    private val list = ArrayList<ErrorItem>()
    private var supportedVersions: List<String>? = null
    private var currentVersion: String? = null

    @JvmStatic
    fun start(loader: ClassLoader, pref: XSharedPreferences, sourceDir: String) {
        XposedBridge.log("[WAE] FeatureLoader.start called with sourceDir: $sourceDir")
        if (!Unobfuscator.initWithPath(sourceDir)) {
            XposedBridge.log("[WAE] Can't init dexkit")
            return
        }
        XposedBridge.log("[WAE] DexKit initialized successfully")
        
        Feature.DEBUG = pref.getBoolean("enablelogs", true)
        XposedBridge.log("[WAE] Feature.DEBUG: ${Feature.DEBUG}")
        
        Utils.xprefs = pref

        XposedHelpers.findAndHookMethod(Instrumentation::class.java, "callApplicationOnCreate", Application::class.java,
            object : XC_MethodHook() {
                @Throws(Throwable::class)
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val app = param.args[0] as Application
                    mApp = app
                    XposedBridge.log("[WAE] callApplicationOnCreate before: ${app.packageName}")

                    // Inject Bootloader Spoofer
                    if (pref.getBoolean("bootloader_spoofer", false)) {
                        HookBL.hook(loader, pref)
                        XposedBridge.log("[WAE] Bootloader Spoofer is Injected")
                    }

                    val packageManager = app.packageManager
                    pref.registerOnSharedPreferenceChangeListener { _, _ -> 
                        XposedBridge.log("[WAE] Preferences changed, reloading...")
                        pref.reload() 
                    }
                    
                    val packageInfo = packageManager.getPackageInfo(app.packageName, 0)
                    XposedBridge.log("[WAE] WhatsApp Version: ${packageInfo.versionName}")
                    currentVersion = packageInfo.versionName
                    
                    val resIdArray = if (app.packageName == PACKAGE_WPP) ResId.array.supported_versions_wpp else ResId.array.supported_versions_business
                    supportedVersions = app.resources.getStringArray(resIdArray).toList()
                    
                    app.registerActivityLifecycleCallbacks(WaCallback())
                    registerReceivers()
                    
                    try {
                        val startTime = System.currentTimeMillis()
                        XposedBridge.log("[WAE] Initializing components...")
                        UnobfuscatorCache.init(app)
                        SharedPreferencesWrapper.hookInit(app.classLoader)
                        ReflectionUtils.initCache(app)
                        
                        val isSupported = supportedVersions?.any { s -> 
                            packageInfo.versionName?.startsWith(s.replace(".xx", "")) == true
                        } ?: false
                        
                        XposedBridge.log("[WAE] isSupported: $isSupported")

                        if (!isSupported) {
                            XposedBridge.log("[WAE] Version not supported, disabling expiration...")
                            disableExpirationVersion(app.classLoader)
                            if (!pref.getBoolean("bypass_version_check", false)) {
                                val msg = "Unsupported version: ${packageInfo.versionName}\nOnly the function of ignoring the expiration of the WhatsApp version has been applied!"
                                throw Exception(msg)
                            }
                        }
                        
                        XposedBridge.log("[WAE] Loading components and plugins...")
                        initComponents(loader, pref)
                        plugins(loader, pref, packageInfo.versionName ?: "")
                        sendEnabledBroadcast(app)
                        
                        val loadTime = System.currentTimeMillis() - startTime
                        XposedBridge.log("[WAE] Loaded Hooks in ${loadTime}ms")
                    } catch (e: Throwable) {
                        XposedBridge.log("[WAE] Error loading features: ${e.message}")
                        XposedBridge.log(e)
                        val error = ErrorItem().apply {
                            pluginName = "MainFeatures[Critical]"
                            whatsAppVersion = packageInfo.versionName ?: ""
                            moduleVersion = BuildConfig.VERSION_NAME
                            message = e.message
                            this.error = e.stackTrace
                                .filter { !it.className.startsWith("android") && !it.className.startsWith("com.android") }
                                .joinToString(prefix = "[", postfix = "]") { it.toString() }
                        }
                        list.add(error)
                    }
                }
            }
        )

        XposedHelpers.findAndHookMethod(WppCore.getHomeActivityClass(loader), "onCreate", Bundle::class.java,
            object : XC_MethodHook() {
                @Throws(Throwable::class)
                override fun afterHookedMethod(param: MethodHookParam) {
                    if (list.isNotEmpty()) {
                        val activity = param.thisObject as Activity
                        val msg = list.joinToString("\n") { "${it.pluginName} - ${it.message}" }

                        AlertDialogWpp(activity)
                            .setTitle(activity.getString(ResId.string.error_detected))
                            .setMessage("${activity.getString(ResId.string.version_error)}$msg\n\nCurrent Version: $currentVersion\nSupported Versions:\n${supportedVersions?.joinToString("\n")}")
                            .setPositiveButton(activity.getString(ResId.string.copy_to_clipboard)) { dialog, _ ->
                                val clipboard = mApp?.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                val clip = ClipData.newPlainText("text", list.joinToString("\n") { it.toString() })
                                clipboard?.setPrimaryClip(clip)
                                Toast.makeText(mApp, ResId.string.copied_to_clipboard, Toast.LENGTH_SHORT).show()
                                dialog.dismiss()
                            }
                            .show()
                    }
                }
            }
        )
    }

    @JvmStatic
    @Throws(Exception::class)
    fun disableExpirationVersion(classLoader: ClassLoader) {
        val expirationClass = Unobfuscator.loadExpirationClass(classLoader)
        val method = ReflectionUtils.findMethodUsingFilter(expirationClass) { m -> 
            m.returnType == Date::class.java 
        }
        XposedBridge.hookMethod(method, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: MethodHookParam) {
                val calendar = Calendar.getInstance()
                calendar.set(2099, 11, 31) // December is 11 in Calendar
                param.result = calendar.time
            }
        })
    }

    private fun initComponents(loader: ClassLoader, pref: XSharedPreferences) {
        FMessageWpp.initialize(loader)
        WppCore.Initialize(loader, pref)
        DesignUtils.setPrefs(pref)
        Utils.init(loader)
        AlertDialogWpp.initDialog(loader)
        WaContactWpp.initialize(loader)
        WppCore.addListenerActivity { activity, state ->
            if (state == WppCore.ActivityChangeState.ChangeType.RESUMED) {
                checkUpdate(activity)
            }

            /*
            if (App.isOriginalPackage() && pref.getBoolean("update_check", true)) {
                if (activity.javaClass.simpleName == "HomeActivity" && state == WppCore.ActivityChangeState.ChangeType.RESUMED) {
                    XposedBridge.log("[WAE] Scheduling update check in 2 seconds...")
                    activity.window.decorView.postDelayed({
                        XposedBridge.log("[WAE] Launching UpdateChecker now")
                        CompletableFuture.runAsync(UpdateChecker(activity))
                    }, 2000)
                }
            }
            */
        }
    }

    private fun checkUpdate(activity: Activity) {
        if (WppCore.getPrivBoolean("need_restart", false)) {
            WppCore.setPrivBoolean("need_restart", false)
            try {
                AlertDialogWpp(activity)
                    .setMessage(activity.getString(ResId.string.restart_wpp))
                    .setPositiveButton(activity.getString(ResId.string.yes)) { _, _ ->
                        if (!Utils.doRestart(activity)) {
                            Toast.makeText(activity, "Unable to rebooting activity", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .setNegativeButton(activity.getString(ResId.string.no), null)
                    .show()
            } catch (ignored: Throwable) {
            }
        }
    }

    private fun registerReceivers() {
        val restartReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (context.packageName == intent.getStringExtra("PKG")) {
                    val appName = context.packageManager.getApplicationLabel(context.applicationInfo)
                    Toast.makeText(context, "${context.getString(ResId.string.rebooting)} $appName...", Toast.LENGTH_SHORT).show()
                    if (!Utils.doRestart(context)) {
                        Toast.makeText(context, "Unable to rebooting $appName", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        mApp?.let { ContextCompat.registerReceiver(it, restartReceiver, IntentFilter("${BuildConfig.APPLICATION_ID}.WHATSAPP.RESTART"), ContextCompat.RECEIVER_EXPORTED) }

        val wppReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                sendEnabledBroadcast(context)
            }
        }
        mApp?.let { ContextCompat.registerReceiver(it, wppReceiver, IntentFilter("${BuildConfig.APPLICATION_ID}.CHECK_WPP"), ContextCompat.RECEIVER_EXPORTED) }

        val restartManualReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                WppCore.setPrivBoolean("need_restart", true)
            }
        }
        mApp?.let { ContextCompat.registerReceiver(it, restartManualReceiver, IntentFilter("${BuildConfig.APPLICATION_ID}.MANUAL_RESTART"), ContextCompat.RECEIVER_EXPORTED) }
    }

    private fun sendEnabledBroadcast(context: Context) {
        try {
            val wppIntent = Intent("${BuildConfig.APPLICATION_ID}.RECEIVER_WPP").apply {
                putExtra("VERSION", context.packageManager.getPackageInfo(context.packageName, 0).versionName)
                putExtra("PKG", context.packageName)
                setPackage(BuildConfig.APPLICATION_ID)
            }
            context.sendBroadcast(wppIntent)
        } catch (ignored: Exception) {
        }
    }

    private fun plugins(loader: ClassLoader, pref: XSharedPreferences, versionWpp: String) {
        XposedBridge.log("Discovering Plugins")
        mApp?.let { FeatureManager.discoverFeatures(it) }
        
        XposedBridge.log("Loading Plugins")
        list.addAll(FeatureManager.loadAll(loader, pref, versionWpp))
    }

    class ErrorItem {
        var pluginName: String? = null
        var whatsAppVersion: String? = null
        var error: String? = null
        var moduleVersion: String? = null
        var message: String? = null

        override fun toString(): String {
            return "pluginName='$pluginName'\nmoduleVersion='$moduleVersion'\nwhatsAppVersion='$whatsAppVersion'\nMessage=$message\nerror='$error'"
        }
    }
}
