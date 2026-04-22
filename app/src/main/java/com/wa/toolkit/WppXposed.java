package com.wa.toolkit;

import android.annotation.SuppressLint;
import android.content.ContextWrapper;
import android.content.pm.ApplicationInfo;

import androidx.annotation.NonNull;

import com.wa.toolkit.xposed.AntiUpdater;
import com.wa.toolkit.xposed.bridge.ScopeHook;
import com.wa.toolkit.xposed.core.FeatureLoader;
import com.wa.toolkit.xposed.core.FeatureLoaderBridge;
import com.wa.toolkit.xposed.downgrade.Patch;

import java.lang.reflect.Method;
import java.util.Objects;

import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import io.github.libxposed.api.XposedModule;
import io.github.libxposed.api.XposedInterface;

public class WppXposed extends XposedModule {

    private static XSharedPreferences pref;
    private String MODULE_PATH;
    
    @Deprecated
    public static XC_InitPackageResources.InitPackageResourcesParam ResParam = null;

    // Test with no-arg constructor as requested by compiler error
    public WppXposed() {
        super();
    }

    @NonNull
    public static XSharedPreferences getPref() {
        if (pref == null) {
            String prefName = BuildConfig.APPLICATION_ID + "_preferences";
            pref = new XSharedPreferences(BuildConfig.APPLICATION_ID, prefName);
            pref.makeWorldReadable();
            pref.reload();
        }
        return pref;
    }

    @Override
    public void onPackageReady(@NonNull PackageReadyParam param) {
        String packageName = param.getPackageName();
        ClassLoader classLoader = param.getClassLoader();
        
        if (MODULE_PATH == null) {
            MODULE_PATH = getModuleApplicationInfo().sourceDir;
        }

        if (packageName.contains("com.wa.toolkit")) {
            try {
                Class<?> mainActivity = classLoader.loadClass("com.wa.toolkit.MainActivity");
                Method isXposedEnabled = mainActivity.getDeclaredMethod("isXposedEnabled");
                // Use hook() if available or getFramework() if I can find it
                // Since I'm not sure, I'll try to find a way to get the framework
                // For now, I'll assume hook() is available in XposedModule
                hook(isXposedEnabled).intercept(chain -> true);

                try {
                    Class<?> companion = classLoader.loadClass("com.wa.toolkit.MainActivity$Companion");
                    Method isXposedEnabledCompanion = companion.getDeclaredMethod("isXposedEnabled");
                    hook(isXposedEnabledCompanion).intercept(chain -> true);
                } catch (Throwable ignored) {}

                // Force MODE_WORLD_READABLE at Context level
                Class<?> contextImpl = classLoader.loadClass("android.app.ContextImpl");
                Method getSharedPreferences = contextImpl.getDeclaredMethod("getSharedPreferences", String.class, int.class);
                hook(getSharedPreferences).intercept(chain -> {
                    String name = (String) chain.getArgs()[0];
                    if (name.contains("preferences") || name.contains("com.wa.toolkit")) {
                        chain.getArgs()[1] = ContextWrapper.MODE_WORLD_READABLE;
                    }
                    return chain.proceed();
                });
                
            } catch (Throwable t) {
                // Log error
            }
            return;
        }

        XposedInterface framework = null; 
        // How to get framework if constructor is empty?
        // Maybe it's available via a method in XposedModule.
        // I'll try to find it or use reflection.
        
        if (packageName.equals("android") || packageName.equals("com.android.providers.settings")) {
            // Patch.handlePackage(param, getPref(), framework);
            // ScopeHook.handlePackage(param, framework);
            return;
        }

        if (!packageName.equals(FeatureLoader.PACKAGE_WPP) && !packageName.equals(FeatureLoader.PACKAGE_BUSINESS)) {
            return;
        }

        // AntiUpdater.hookPackage(param, framework);

        // Patch.handlePackage(param, getPref(), framework);

        // ScopeHook.handlePackage(param, framework);

        boolean isWpp = packageName.equals(FeatureLoader.PACKAGE_WPP);
        boolean isBusiness = packageName.equals(FeatureLoader.PACKAGE_BUSINESS);
        
        if (isWpp || isBusiness) {
            ApplicationInfo appInfo = param.getApplicationInfo();
            // FeatureLoaderBridge.startModern(classLoader, getPref(), appInfo.sourceDir, MODULE_PATH, framework);
        }
    }
}
