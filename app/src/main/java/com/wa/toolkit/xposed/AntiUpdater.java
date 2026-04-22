package com.wa.toolkit.xposed;

import android.content.pm.PackageInstaller;

import com.wa.toolkit.xposed.core.FeatureLoader;

import java.io.IOException;
import java.lang.reflect.Method;

import io.github.libxposed.api.XposedModule;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class AntiUpdater {

    public static void hookSession(XC_LoadPackage.LoadPackageParam lpparam) {
        if (lpparam.packageName.equals("android")) return;
        XposedBridge.hookAllMethods(PackageInstaller.class, "createSession", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                PackageInstaller.SessionParams session = (PackageInstaller.SessionParams) param.args[0];
                String packageName = (String) XposedHelpers.getObjectField(session, "appPackageName");
                if (packageName != null && (packageName.equals(FeatureLoader.PACKAGE_WPP) || packageName.equals(FeatureLoader.PACKAGE_BUSINESS))) {
                    param.setThrowable(new IOException("UPDATE LOCKED BY WAENHANCER"));
                }
            }
        });
    }

    public static void hookPackage(XposedModule.PackageReadyParam param, XposedModule module) {
        try {
            Method createSession = PackageInstaller.class.getDeclaredMethod("createSession", PackageInstaller.SessionParams.class);
            module.hook(createSession).intercept(chain -> {
                PackageInstaller.SessionParams session = (PackageInstaller.SessionParams) chain.getArgs().get(0);
                java.lang.reflect.Field field = PackageInstaller.SessionParams.class.getDeclaredField("appPackageName");
                field.setAccessible(true);
                String packageName = (String) field.get(session);
                
                if (packageName != null && (packageName.equals(FeatureLoader.PACKAGE_WPP) || packageName.equals(FeatureLoader.PACKAGE_BUSINESS))) {
                    throw new IOException("UPDATE LOCKED BY WAENHANCER");
                }
                return chain.proceed();
            });
        } catch (Throwable t) {
            // Log error
        }
    }
}
