package com.wa.toolkit;

import android.annotation.SuppressLint;

import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class WppXposedLegacy implements IXposedHookLoadPackage, IXposedHookInitPackageResources, IXposedHookZygoteInit {

    private String MODULE_PATH;

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        MODULE_PATH = startupParam.modulePath;
    }

    @SuppressLint("WorldReadableFiles")
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        // Using the legacy entry point but calling into the modern module logic
        // This requires io.github.libxposed:legacy to bridge the modern XposedModule
        
        // Because we are using legacy framework to load, we don't have the modern XposedInterface
        // readily available. We need to instantiate and run our modern module logic.
        // However, WppXposed expects an XposedInterface. 
        // If the user's framework is actually legacy (or an LSPosed that only sees legacy), 
        // the modern API classes might not even be provided by the framework at runtime.
        
        // Let's fallback the logic here directly or use the FeatureLoader's legacy start method
        // which was already built for this.
        
        String packageName = lpparam.packageName;
        ClassLoader classLoader = lpparam.classLoader;
        
        if (MODULE_PATH == null || MODULE_PATH.isEmpty()) {
            try {
                android.content.Context systemContext = (android.content.Context) de.robv.android.xposed.XposedHelpers.callMethod(de.robv.android.xposed.XposedHelpers.callStaticMethod(de.robv.android.xposed.XposedHelpers.findClass("android.app.ActivityThread", null), "currentActivityThread"), "getSystemContext");
                MODULE_PATH = systemContext.getPackageManager().getApplicationInfo(BuildConfig.APPLICATION_ID, 0).sourceDir;
            } catch (Throwable t) {
                de.robv.android.xposed.XposedBridge.log("[WAE] Failed to get MODULE_PATH via ActivityThread: " + t.getMessage());
            }
        }

        if (packageName.contains("com.wa.toolkit")) {
            de.robv.android.xposed.XposedHelpers.findAndHookMethod("com.wa.toolkit.MainActivity", classLoader, "isXposedEnabled", de.robv.android.xposed.XC_MethodReplacement.returnConstant(true));
            try {
                de.robv.android.xposed.XposedHelpers.findAndHookMethod("com.wa.toolkit.MainActivity$Companion", classLoader, "isXposedEnabled", de.robv.android.xposed.XC_MethodReplacement.returnConstant(true));
            } catch (Throwable ignored) {}
            
            de.robv.android.xposed.XposedHelpers.findAndHookMethod("android.app.ContextImpl", classLoader, "getSharedPreferences", String.class, int.class, new de.robv.android.xposed.XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    String name = (String) param.args[0];
                    if (name.contains("preferences") || name.contains("com.wa.toolkit")) {
                        param.args[1] = android.content.ContextWrapper.MODE_WORLD_READABLE;
                    }
                }
            });
            return;
        }

        if (packageName.equals("android") || packageName.equals("com.android.providers.settings")) {
            com.wa.toolkit.xposed.downgrade.Patch.handleLoadPackage(lpparam, com.wa.toolkit.xposed.utils.PrefUtils.getPref());
            com.wa.toolkit.xposed.bridge.ScopeHook.hook(lpparam);
            return;
        }

        if (!packageName.equals(com.wa.toolkit.xposed.core.FeatureLoader.PACKAGE_WPP) && !packageName.equals(com.wa.toolkit.xposed.core.FeatureLoader.PACKAGE_BUSINESS)) {
            return;
        }
        
        com.wa.toolkit.xposed.AntiUpdater.hookSession(lpparam);
        com.wa.toolkit.xposed.downgrade.Patch.handleLoadPackage(lpparam, com.wa.toolkit.xposed.utils.PrefUtils.getPref());
        com.wa.toolkit.xposed.bridge.ScopeHook.hook(lpparam);

        boolean isWpp = packageName.equals(com.wa.toolkit.xposed.core.FeatureLoader.PACKAGE_WPP);
        boolean isBusiness = packageName.equals(com.wa.toolkit.xposed.core.FeatureLoader.PACKAGE_BUSINESS);


        if (isWpp || isBusiness) {
            com.wa.toolkit.xposed.core.FeatureLoader.start(classLoader, com.wa.toolkit.xposed.utils.PrefUtils.getPref(), lpparam.appInfo.sourceDir, MODULE_PATH);
        }
    }

    @Override
    public void handleInitPackageResources(XC_InitPackageResources.InitPackageResourcesParam resparam) throws Throwable {
        com.wa.toolkit.xposed.utils.PrefUtils.ResParam = resparam;
        String packageName = resparam.packageName;
        if (!packageName.equals(com.wa.toolkit.xposed.core.FeatureLoader.PACKAGE_WPP) && !packageName.equals(com.wa.toolkit.xposed.core.FeatureLoader.PACKAGE_BUSINESS)) {
            return;
        }
        
        try {
            // Modern LibXposed removed resource hooks. The legacy wrapper might still support it.
            if (MODULE_PATH != null) {
                com.wa.toolkit.xposed.utils.ResourceMirror.INSTANCE.mirror(resparam, android.content.res.XModuleResources.createInstance(MODULE_PATH, resparam.res));
            }
        } catch (Throwable t) {
            de.robv.android.xposed.XposedBridge.log("[WAE] Failed to mirror resources: " + t.getMessage());
        }
    }
}
