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
import com.wa.toolkit.xposed.utils.PrefUtils;

import java.lang.reflect.Method;
import java.util.Objects;

import de.robv.android.xposed.XSharedPreferences;
import io.github.libxposed.api.XposedModule;

public class WppXposed extends XposedModule {

    private String MODULE_PATH;

    public WppXposed() {
        super();
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
                hook(isXposedEnabled).intercept(chain -> true);

                try {
                    Class<?> companion = classLoader.loadClass("com.wa.toolkit.MainActivity$Companion");
                    Method isXposedEnabledCompanion = companion.getDeclaredMethod("isXposedEnabled");
                    hook(isXposedEnabledCompanion).intercept(chain -> true);
                } catch (Throwable ignored) {}

                Class<?> contextImpl = classLoader.loadClass("android.app.ContextImpl");
                Method getSharedPreferences = contextImpl.getDeclaredMethod("getSharedPreferences", String.class, int.class);
                hook(getSharedPreferences).intercept(chain -> {
                    String name = (String) chain.getArgs().get(0);
                    if (name.contains("preferences") || name.contains("com.wa.toolkit")) {
                        chain.getArgs().set(1, ContextWrapper.MODE_WORLD_READABLE);
                    }
                    return chain.proceed();
                });
                
            } catch (Throwable t) {
                // Log error
            }
            return;
        }

        if (packageName.equals("android") || packageName.equals("com.android.providers.settings")) {
            Patch.handlePackage(param, PrefUtils.getPref(), this);
            ScopeHook.handlePackage(param, this);
            return;
        }

        if (!packageName.equals(FeatureLoader.PACKAGE_WPP) && !packageName.equals(FeatureLoader.PACKAGE_BUSINESS)) {
            return;
        }

        AntiUpdater.hookPackage(param, this);

        Patch.handlePackage(param, PrefUtils.getPref(), this);

        ScopeHook.handlePackage(param, this);

        boolean isWpp = packageName.equals(FeatureLoader.PACKAGE_WPP);
        boolean isBusiness = packageName.equals(FeatureLoader.PACKAGE_BUSINESS);
        
        if (isWpp || isBusiness) {
            com.wa.toolkit.xposed.core.FeatureManager.setXposedModule(this);
            ApplicationInfo appInfo = param.getApplicationInfo();
            FeatureLoaderBridge.startModern(classLoader, PrefUtils.getPref(), appInfo.sourceDir, MODULE_PATH, this);
        }
    }
}
