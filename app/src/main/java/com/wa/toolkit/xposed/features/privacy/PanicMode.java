package com.wa.toolkit.xposed.features.privacy;

import android.app.Activity;
import android.view.KeyEvent;

import androidx.annotation.NonNull;

import com.wa.toolkit.xposed.core.Feature;
import com.wa.toolkit.xposed.core.WppCore;
import com.wa.toolkit.utils.Utils;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;

public class PanicMode extends Feature {

    private long lastVolumeUpTime = 0;
    private long lastVolumeDownTime = 0;
    private static final long PANIC_THRESHOLD_MS = 3000;

    public PanicMode(ClassLoader loader, XSharedPreferences preferences) {
        super(loader, preferences);
    }

    @Override
    public void doHook() throws Throwable {
        if (!prefs.getBoolean("panic_mode", false)) return;

        XposedBridge.hookAllMethods(Activity.class, "onKeyDown", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                int keyCode = (int) param.args[0];
                KeyEvent event = (KeyEvent) param.args[1];

                if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                    lastVolumeUpTime = System.currentTimeMillis();
                } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                    lastVolumeDownTime = System.currentTimeMillis();
                }

                if (Math.abs(lastVolumeUpTime - lastVolumeDownTime) < 500) {
                    if (event.getEventTime() - event.getDownTime() > PANIC_THRESHOLD_MS) {
                        triggerPanicMode((Activity) param.thisObject);
                    }
                }
            }
        });
    }

    private void triggerPanicMode(Activity activity) {
        XposedBridge.log("Panic Mode Triggered!");
        // Panic Mode actions:
        // 1. Force close WhatsApp
        // 2. Clear recent tasks
        // 3. (Optional) Switch to a dummy account if multi-account is supported
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    @NonNull
    @Override
    public String getPluginName() {
        return "Panic Mode";
    }
}
