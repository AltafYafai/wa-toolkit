package com.wa.toolkit.xposed.core;

import android.app.Application;
import android.app.Instrumentation;

import com.wa.toolkit.xposed.core.devkit.Unobfuscator;

import java.lang.reflect.Method;

import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import io.github.libxposed.api.XposedModule;

public class FeatureLoaderBridge {

    public static void startModern(ClassLoader loader, XSharedPreferences pref, String sourceDir, String modulePath, XposedModule module) {
        XposedBridge.log("[WAE] FeatureLoaderBridge.startModern called");
        
        if (!Unobfuscator.initWithPath(sourceDir)) {
            XposedBridge.log("[WAE] Can't init dexkit");
            return;
        }

        try {
            Method callApplicationOnCreate = Instrumentation.class.getDeclaredMethod("callApplicationOnCreate", Application.class);
            module.hook(callApplicationOnCreate).intercept(chain -> {
                Application app = (Application) chain.getArgs().get(0);
                
                FeatureLoader.initFromBridge(app, loader, pref, modulePath);
                
                return chain.proceed();
            });
        } catch (Throwable t) {
            XposedBridge.log("[WAE] Error in FeatureLoaderBridge: " + t.getMessage());
        }
    }
}
