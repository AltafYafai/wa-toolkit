package com.wa.toolkit;

import android.annotation.SuppressLint;
import android.content.ContextWrapper;
import android.content.pm.ApplicationInfo;

import androidx.annotation.NonNull;

import com.wa.toolkit.xposed.AntiUpdater;
import com.wa.toolkit.xposed.bridge.ScopeHook;
import com.wa.toolkit.xposed.core.FeatureLoader;
import com.wa.toolkit.xposed.downgrade.Patch;

import java.lang.reflect.Method;
import java.util.Objects;

import de.robv.android.xposed.XSharedPreferences;
import io.github.libxposed.XposedModule;

public class WppXposed extends XposedModule {

    private static XSharedPreferences pref;
    private String MODULE_PATH;

    public WppXposed(@NonNull XposedInterface base, @NonNull ModuleLoadedParam param) {
        super(base, param);
        MODULE_PATH = param.getBundlePath();
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

        if (packageName.contains("com.wa.toolkit")) {
            try {
                Class<?> mainActivity = classLoader.loadClass("com.wa.toolkit.MainActivity");
                Method isXposedEnabled = mainActivity.getDeclaredMethod("isXposedEnabled");
                getFramework().hookMethod(isXposedEnabled, chain -> true);

                try {
                    Class<?> companion = classLoader.loadClass("com.wa.toolkit.MainActivity$Companion");
                    Method isXposedEnabledCompanion = companion.getDeclaredMethod("isXposedEnabled");
                    getFramework().hookMethod(isXposedEnabledCompanion, chain -> true);
                } catch (Throwable ignored) {}

                // Force MODE_WORLD_READABLE at Context level
                Class<?> contextImpl = classLoader.loadClass("android.app.ContextImpl");
                Method getSharedPreferences = contextImpl.getDeclaredMethod("getSharedPreferences", String.class, int.class);
                getFramework().hookMethod(getSharedPreferences, chain -> {
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

        if (packageName.equals("android") || packageName.equals("com.android.providers.settings")) {
            // Note: Patch and ScopeHook need to be updated to support API 101 or we use a wrapper
            // For now, I'll pass the framework to them if I update them
            Patch.handlePackage(param, getPref(), getFramework());
            ScopeHook.handlePackage(param, getFramework());
            return;
        }

        if (!packageName.equals(FeatureLoader.PACKAGE_WPP) && !packageName.equals(FeatureLoader.PACKAGE_BUSINESS)) {
            return;
        }

        AntiUpdater.hookPackage(param, getFramework());

        Patch.handlePackage(param, getPref(), getFramework());

        ScopeHook.handlePackage(param, getFramework());

        boolean isWpp = packageName.equals(FeatureLoader.PACKAGE_WPP);
        boolean isBusiness = packageName.equals(FeatureLoader.PACKAGE_BUSINESS);
        
        // App.isOriginalPackage() might need access to current context or we keep it as is if it's static
        // Assuming App.isOriginalPackage() is available
        
        if (isWpp || isBusiness) {
            // Load features
            // FeatureLoader needs to be updated to support API 101
            ApplicationInfo appInfo = param.getApplicationInfo();
            FeatureLoader.startModern(classLoader, getPref(), appInfo.sourceDir, MODULE_PATH, getFramework());
        }
    }
}
