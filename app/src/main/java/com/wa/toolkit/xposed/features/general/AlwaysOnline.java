package com.wa.toolkit.xposed.features.general;

import androidx.annotation.NonNull;

import com.wa.toolkit.xposed.core.Feature;
import com.wa.toolkit.xposed.core.devkit.Unobfuscator;

import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;

public class AlwaysOnline extends Feature {

    public AlwaysOnline(ClassLoader loader, XSharedPreferences preferences) {
        super(loader, preferences);
    }

    @Override
    public void doHook() throws Exception {
        if (!prefs.getBoolean("always_online", false)) return;
        
        var stateChange = Unobfuscator.loadStateChangeMethod(classLoader);
        XposedBridge.hookMethod(stateChange, XC_MethodReplacement.DO_NOTHING);
    }

    @NonNull
    @Override
    public String getPluginName() {
        return "AlwaysOnline";
    }
}
