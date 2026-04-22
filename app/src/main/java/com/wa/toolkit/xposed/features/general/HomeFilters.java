package com.wa.toolkit.xposed.features.general;

import android.view.View;

import androidx.annotation.NonNull;

import com.wa.toolkit.xposed.core.Feature;
import com.wa.toolkit.xposed.core.devkit.Unobfuscator;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class HomeFilters extends Feature {

    public HomeFilters(ClassLoader loader, XSharedPreferences preferences) {
        super(loader, preferences);
    }

    @Override
    public void doHook() throws Exception {
        if (prefs.getBoolean("filterseen", false)) return;

        // Note: These props were part of disableHomeFilters in Others.java
        // They might need to be moved to a shared PropManager if other features need them.
        SystemProperties.propsBoolean.put(15345, true);
        SystemProperties.propsBoolean.put(13546, false);
        SystemProperties.propsBoolean.put(13408, true);

        Class<?> filterView = Unobfuscator.loadChatFilterView(classLoader);
        XposedBridge.hookAllConstructors(filterView, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                var view = (View) param.thisObject;
                view.setVisibility(View.GONE);
                com.wa.toolkit.xposed.core.FeatureManager.safeFindAndHookMethod(View.class, "setVisibility", int.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        if (view == param.thisObject && (int) param.args[0] != View.GONE) {
                            param.setResult(View.GONE);
                        }
                    }
                });
            }
        });
    }

    @NonNull
    @Override
    public String getPluginName() {
        return "HomeFilters";
    }
}
