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
            // Note: Patch and ScopeHook are now using modern API. We need legacy wrappers or to revert them
            // to support this legacy fallback.
            return;
        }

        if (!packageName.equals(com.wa.toolkit.xposed.core.FeatureLoader.PACKAGE_WPP) && !packageName.equals(com.wa.toolkit.xposed.core.FeatureLoader.PACKAGE_BUSINESS)) {
            return;
        }
        
        boolean isWpp = packageName.equals(com.wa.toolkit.xposed.core.FeatureLoader.PACKAGE_WPP);
        boolean isBusiness = packageName.equals(com.wa.toolkit.xposed.core.FeatureLoader.PACKAGE_BUSINESS);

        if (isWpp || isBusiness) {
            com.wa.toolkit.xposed.core.FeatureLoader.start(classLoader, WppXposed.getPref(), lpparam.appInfo.sourceDir, MODULE_PATH);
        }
    }

    @Override
    public void handleInitPackageResources(XC_InitPackageResources.InitPackageResourcesParam resparam) throws Throwable {
        WppXposed.ResParam = resparam;
        String packageName = resparam.packageName;
        if (!packageName.equals(com.wa.toolkit.xposed.core.FeatureLoader.PACKAGE_WPP) && !packageName.equals(com.wa.toolkit.xposed.core.FeatureLoader.PACKAGE_BUSINESS)) {
            return;
        }
        
        try {
            // Modern LibXposed removed resource hooks. The legacy wrapper might still support it.
            // com.wa.toolkit.xposed.utils.ResourceMirror.INSTANCE.mirror(resparam, null);
        } catch (Throwable t) {
            // Ignore
        }
    }
}
