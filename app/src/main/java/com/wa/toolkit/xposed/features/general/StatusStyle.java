package com.wa.toolkit.xposed.features.general;

import androidx.annotation.NonNull;

import com.wa.toolkit.xposed.core.Feature;
import com.wa.toolkit.xposed.core.devkit.Unobfuscator;

import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;

public class StatusStyle extends Feature {

    public StatusStyle(ClassLoader loader, XSharedPreferences preferences) {
        super(loader, preferences);
    }

    @Override
    public void doHook() throws Exception {
        var oldStatus = prefs.getBoolean("oldstatus", false);
        var status_style = Integer.parseInt(prefs.getString("status_style", "0"));
        
        var retStatusStyle = Unobfuscator.loadStatusStyleMethod(classLoader);
        com.wa.toolkit.xposed.core.FeatureManager.safeHookMethod(retStatusStyle, XC_MethodReplacement.returnConstant(status_style));
        
        status_style = oldStatus ? 0 : status_style;
        SystemProperties.propsInteger.put(9973, 1);
        SystemProperties.propsBoolean.put(6285, true);
        SystemProperties.propsInteger.put(8522, status_style);
        SystemProperties.propsInteger.put(8521, status_style);
    }

    @NonNull
    @Override
    public String getPluginName() {
        return "StatusStyle";
    }
}
