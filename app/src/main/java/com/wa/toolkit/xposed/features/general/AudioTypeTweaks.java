package com.wa.toolkit.xposed.features.general;

import androidx.annotation.NonNull;

import com.wa.toolkit.xposed.core.Feature;
import com.wa.toolkit.xposed.core.devkit.Unobfuscator;
import com.wa.toolkit.xposed.utils.ReflectionUtils;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;

public class AudioTypeTweaks extends Feature {

    public AudioTypeTweaks(ClassLoader loader, XSharedPreferences preferences) {
        super(loader, preferences);
    }

    @Override
    public void doHook() throws Exception {
        var audio_type = Integer.parseInt(Utils.getSafeString(prefs, "audio_type", "0"));
        if (audio_type <= 0) return;

        var sendAudioTypeMethod = Unobfuscator.loadSendAudioTypeMethod(classLoader);
        com.wa.toolkit.xposed.core.FeatureManager.safeHookMethod(sendAudioTypeMethod, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                var results = ReflectionUtils.findInstancesOfType(param.args, Integer.class);
                if (results.size() < 2) {
                    log("sendAudioTypeMethod size < 2");
                    return;
                }
                var mediaType = results.get(0);
                var audioType = results.get(1);
                if (mediaType.second != 2 && mediaType.second != 9) return;
                param.args[audioType.first] = audio_type - 1; // 1 = voice notes || 0 = audio voice
            }
        });

        var originFMessageField = Unobfuscator.loadOriginFMessageField(classLoader);
        var forwardAudioTypeMethod = Unobfuscator.loadForwardAudioTypeMethod(classLoader);

        com.wa.toolkit.xposed.core.FeatureManager.safeHookMethod(forwardAudioTypeMethod, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                var fMessage = param.getResult();
                originFMessageField.setAccessible(true);
                originFMessageField.setInt(fMessage, audio_type - 1);
            }
        });
    }

    @NonNull
    @Override
    public String getPluginName() {
        return "AudioTypeTweaks";
    }
}
