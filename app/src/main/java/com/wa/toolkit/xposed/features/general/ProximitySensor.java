package com.wa.toolkit.xposed.features.general;

import android.os.PowerManager;

import androidx.annotation.NonNull;

import com.wa.toolkit.xposed.core.Feature;
import com.wa.toolkit.xposed.core.devkit.Unobfuscator;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;

public class ProximitySensor extends Feature {

    public ProximitySensor(ClassLoader loader, XSharedPreferences preferences) {
        super(loader, preferences);
    }

    @Override
    public void doHook() throws Exception {
        var disable_sensor_proximity = prefs.getBoolean("disable_sensor_proximity", false);
        var proximity_audios = prefs.getBoolean("proximity_audios", false);

        if (disable_sensor_proximity) {
            XposedBridge.hookAllMethods(PowerManager.class, "newWakeLock", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if (param.args[0].equals(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK)) {
                        param.setResult(null);
                    }
                }
            });
        }

        if (proximity_audios) {
            var classes = Unobfuscator.loadProximitySensorListenerClasses(classLoader);
            for (var cls : classes) {
                XposedBridge.hookAllMethods(cls, "onSensorChanged", XC_MethodReplacement.DO_NOTHING);
            }
        }
    }

    @NonNull
    @Override
    public String getPluginName() {
        return "ProximitySensor";
    }
}
