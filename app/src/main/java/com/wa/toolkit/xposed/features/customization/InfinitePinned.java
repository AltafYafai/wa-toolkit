package com.wa.toolkit.xposed.features.customization;

import androidx.annotation.NonNull;

import com.wa.toolkit.xposed.core.Feature;
import com.wa.toolkit.xposed.core.devkit.Unobfuscator;

import java.lang.reflect.Method;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;

public class InfinitePinned extends Feature {

    public InfinitePinned(ClassLoader loader, XSharedPreferences preferences) {
        super(loader, preferences);
    }

    @Override
    public void doHook() throws Throwable {
        if (!prefs.getBoolean("disable_pinned_limit", false)) return;

        try {
            Method limitMethod = Unobfuscator.loadSetPinnedLimitMethod(classLoader);
            XposedBridge.hookMethod(limitMethod, XC_MethodReplacement.returnConstant(999));
        } catch (Exception e) {
            logDebug("Error hooking Pinned Limit", e);
        }
    }

    @NonNull
    @Override
    public String getPluginName() {
        return "Infinite Pinned Chats";
    }
}
