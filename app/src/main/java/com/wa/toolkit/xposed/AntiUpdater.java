package com.wa.toolkit.xposed;

import android.content.pm.PackageInstaller;

import com.wa.toolkit.xposed.core.FeatureLoader;
import com.wa.toolkit.xposed.core.FeatureManager;

import java.io.IOException;

import io.github.libxposed.api.XposedModule;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class AntiUpdater {

    private static final XC_MethodHook HOOK = new XC_MethodHook() {
        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            PackageInstaller.SessionParams session = (PackageInstaller.SessionParams) param.args[0];
            String packageName = (String) XposedHelpers.getObjectField(session, "appPackageName");
            if (packageName != null && (packageName.equals(FeatureLoader.PACKAGE_WPP) || packageName.equals(FeatureLoader.PACKAGE_BUSINESS))) {
                param.setThrowable(new IOException("UPDATE LOCKED BY WAENHANCER"));
            }
        }
    };

    public static void hookSession(XC_LoadPackage.LoadPackageParam lpparam) {
        if (lpparam.packageName.equals("android")) return;
        FeatureManager.safeHookAllMethods(PackageInstaller.class, "createSession", HOOK);
    }

    public static void handleModern(XposedModule module) {
        FeatureManager.setXposedModule(module);
        FeatureManager.safeHookAllMethods(PackageInstaller.class, "createSession", HOOK);
    }
}
