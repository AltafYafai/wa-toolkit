package com.wa.toolkit;

import android.annotation.SuppressLint;
import android.content.ContextWrapper;
import android.content.res.XModuleResources;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import com.wa.toolkit.MainActivity;
import com.wa.toolkit.xposed.AntiUpdater;
import com.wa.toolkit.xposed.bridge.ScopeHook;
import com.wa.toolkit.xposed.core.FeatureLoader;
import com.wa.toolkit.xposed.downgrade.Patch;
import com.wa.toolkit.xposed.utils.ResId;

import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class WppXposed implements IXposedHookLoadPackage, IXposedHookInitPackageResources, IXposedHookZygoteInit {

    private static XSharedPreferences pref;
    private String MODULE_PATH;
    public static XC_InitPackageResources.InitPackageResourcesParam ResParam;

    @NonNull
    public static XSharedPreferences getPref() {
        if (pref == null) {
            pref = new XSharedPreferences(BuildConfig.APPLICATION_ID, BuildConfig.APPLICATION_ID + "_preferences");
            pref.makeWorldReadable();
            pref.reload();
        }
        return pref;
    }

    @SuppressLint("WorldReadableFiles")
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        var packageName = lpparam.packageName;
        var classLoader = lpparam.classLoader;

        if (packageName.contains("com.wa.toolkit")) {
            XposedBridge.log("[•] Hooking toolkit app: " + packageName);
            try {
                Class<?> mainActivity = XposedHelpers.findClass("com.wa.toolkit.MainActivity", lpparam.classLoader);
                XposedHelpers.findAndHookMethod(mainActivity, "isXposedEnabled", new XC_MethodReplacement() {
                    @Override
                    protected Object replaceHookedMethod(MethodHookParam param) {
                        return true;
                    }
                });

                // Also hook the Companion method just in case
                try {
                    Class<?> companion = XposedHelpers.findClass("com.wa.toolkit.MainActivity$Companion", lpparam.classLoader);
                    XposedHelpers.findAndHookMethod(companion, "isXposedEnabled", new XC_MethodReplacement() {
                        @Override
                        protected Object replaceHookedMethod(MethodHookParam param) {
                            return true;
                        }
                    });
                } catch (Throwable ignored) {}

                XposedBridge.log("[✓] isXposedEnabled hooked successfully");
                
                String prefManager = PreferenceManager.class.getName();
                XC_MethodReplacement worldReadable = XC_MethodReplacement.returnConstant(ContextWrapper.MODE_WORLD_READABLE);
                
                XposedHelpers.findAndHookMethod(prefManager, lpparam.classLoader, "getDefaultSharedPreferencesMode", worldReadable);
                XposedHelpers.findAndHookMethod(prefManager, lpparam.classLoader, "getSharedPreferencesMode", worldReadable);

                // Force MODE_WORLD_READABLE at Context level
                XposedHelpers.findAndHookMethod("android.app.ContextImpl", lpparam.classLoader, "getSharedPreferences", String.class, int.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        String name = (String) param.args[0];
                        if (name.contains("preferences") || name.contains(BuildConfig.APPLICATION_ID)) {
                            param.args[1] = ContextWrapper.MODE_WORLD_READABLE;
                        }
                    }
                });
                
            } catch (Throwable t) {
                XposedBridge.log("[!] Error hooking toolkit app: " + t.getMessage());
                XposedBridge.log(t);
            }
            return;
        }

        if (!packageName.equals(FeatureLoader.PACKAGE_WPP) && !packageName.equals(FeatureLoader.PACKAGE_BUSINESS)) {
            return;
        }

        AntiUpdater.hookSession(lpparam);

        Patch.handleLoadPackage(lpparam, getPref());

        ScopeHook.hook(lpparam);

        //  AndroidPermissions.hook(lpparam); in tests
        if ((packageName.equals(FeatureLoader.PACKAGE_WPP) && App.isOriginalPackage()) || packageName.equals(FeatureLoader.PACKAGE_BUSINESS)) {
            XposedBridge.log("[•] This package: " + lpparam.packageName);

            // Load features
            FeatureLoader.start(classLoader, getPref(), lpparam.appInfo.sourceDir);
        }
    }

    @Override
    public void handleInitPackageResources(XC_InitPackageResources.InitPackageResourcesParam resparam) throws Throwable {
        var packageName = resparam.packageName;

        if (!packageName.equals(FeatureLoader.PACKAGE_WPP) && !packageName.equals(FeatureLoader.PACKAGE_BUSINESS))
            return;

        XModuleResources modRes = XModuleResources.createInstance(MODULE_PATH, resparam.res);
        ResParam = resparam;

        XposedBridge.log("[•] Mirroring resources for " + packageName);
        com.wa.toolkit.xposed.utils.ResourceMirror.INSTANCE.mirror(resparam, modRes);
    }

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        MODULE_PATH = startupParam.modulePath;
    }

}
