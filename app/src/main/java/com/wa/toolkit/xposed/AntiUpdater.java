package com.wa.toolkit.xposed;

import android.content.pm.PackageInstaller;

import com.wa.toolkit.xposed.core.FeatureLoader;

import java.io.IOException;
import java.lang.reflect.Method;

import io.github.libxposed.api.XposedInterface;
import io.github.libxposed.api.XposedModule;

public class AntiUpdater {

    public static void hookPackage(XposedModule.PackageReadyParam param, XposedInterface framework) {
        try {
            Method createSession = PackageInstaller.class.getDeclaredMethod("createSession", PackageInstaller.SessionParams.class);
            framework.hookMethod(createSession, chain -> {
                PackageInstaller.SessionParams session = (PackageInstaller.SessionParams) chain.getArgs()[0];
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
