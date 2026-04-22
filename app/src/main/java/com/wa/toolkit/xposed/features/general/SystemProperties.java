package com.wa.toolkit.xposed.features.general;

import androidx.annotation.NonNull;

import com.wa.toolkit.xposed.core.Feature;
import com.wa.toolkit.xposed.core.WppCore;
import com.wa.toolkit.xposed.core.devkit.Unobfuscator;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import com.wa.toolkit.xposed.utils.ReflectionUtils;

import java.util.HashMap;
import java.util.Objects;

public class SystemProperties extends Feature {

    public static HashMap<Integer, Boolean> propsBoolean = new HashMap<>();
    public static HashMap<Integer, Integer> propsInteger = new HashMap<>();

    public SystemProperties(ClassLoader loader, XSharedPreferences preferences) {
        super(loader, preferences);
    }

    @Override
    public void doHook() throws Exception {
        var menuWIcons = prefs.getBoolean("menuwicon", false);
        var newSettings = prefs.getBoolean("novaconfig", false);
        var disableMetaAI = prefs.getBoolean("metaai", false);
        var audio_transcription = prefs.getBoolean("audio_transcription", false);
        var oldStatus = prefs.getBoolean("oldstatus", false);
        var igstatus = prefs.getBoolean("igstatus", false);
        var animationEmojis = prefs.getBoolean("animation_emojis", false);
        var floatingMenu = prefs.getBoolean("floatingmenu", false);

        propsInteger.put(3877, oldStatus ? igstatus ? 2 : 0 : 2);

        propsBoolean.put(18250, false);
        propsBoolean.put(11528, false);

        propsBoolean.put(4497, menuWIcons);
        propsBoolean.put(4023, false);
        propsBoolean.put(14862, newSettings);
        propsInteger.put(18564, newSettings ? 1 : 0);

        propsBoolean.put(2889, floatingMenu);

        // new text composer
        propsBoolean.put(15708, true);

        // change page id
        propsBoolean.put(2358, false);

        // disable contact filter
        propsBoolean.put(7769, false);

        // disable new Media Picker
        propsBoolean.put(9286, false);

        // Instant Video
        propsBoolean.put(3354, true);
        propsBoolean.put(5418, true);
        propsBoolean.put(9051, true);

        // disable new toolbar
        propsBoolean.put(11824, false);
        propsBoolean.put(6481, false);

        // Enable music in Stories
        propsBoolean.put(13591, true);
        propsBoolean.put(10024, true);

        // show all status
        propsBoolean.put(6798, true);

        // auto play emojis settings
        propsBoolean.put(3575, animationEmojis);
        propsBoolean.put(9757, animationEmojis);

        // emojis maps
        propsBoolean.put(10639, animationEmojis);
        propsBoolean.put(12495, animationEmojis);
        propsBoolean.put(11066, animationEmojis);

        propsBoolean.put(7589, true);  // Media select quality
        propsBoolean.put(6972, false); // Media select quality
        propsBoolean.put(5625, true);  // Enable option to autodelete channels media

        propsBoolean.put(8643, true);  // Enable TextStatusComposerActivityV2
        propsBoolean.put(8607, true);  // Enable Dialer keyboard
        propsBoolean.put(9578, false);  // Disable Privacy Checkup (causing crash)
        propsInteger.put(8135, 2);  // Call Filters

        // Enable Translate Message
        propsBoolean.put(9141, true);
        propsBoolean.put(8925, true);

        propsBoolean.put(10380, false); // fix crash bug in Settings/Archived

        propsBoolean.put(0x34b9, true); // Enable Select People in call
        propsBoolean.put(0x351c, true); // Enable new colors style in Text Composer

        // Enable show count until viewed
        propsBoolean.put(0x2289, true);
        propsBoolean.put(0x373f, true);

        // add yours in stories
        propsBoolean.put(0x2ce2, true);
        propsBoolean.put(0x2ce3, true);

        propsBoolean.put(0x345a, true); // new edit profile name

        // new stories selection
        propsBoolean.put(0x32ca, true);
        propsBoolean.put(0x32cb, true);

        // Additional flags from WaEnhancer Others.java
        propsBoolean.put(14022, true); // Enable voice note transcription
        propsBoolean.put(15128, true); // Enable new chat lock UI
        propsBoolean.put(13542, true); // Enable screen sharing
        propsBoolean.put(15234, true); // Enable community tabs
        propsBoolean.put(11234, true); // Enable passkeys

        if (disableMetaAI) {
            propsInteger.put(15535, 0);
            propsBoolean.put(8025, false);
            propsBoolean.put(6251, false);
            propsBoolean.put(8026, false);
            propsBoolean.put(14886, false);
        }

        if (audio_transcription) {
            propsBoolean.put(8632, true);
            propsBoolean.put(2890, true);
            propsBoolean.put(9215, false);
            propsBoolean.put(9216, true);
            propsBoolean.put(6808, true);
            propsBoolean.put(10286, true);
            propsBoolean.put(11596, true);
            propsBoolean.put(13949, true);
        }

        hookProps();
    }

    private void hookProps() throws Exception {
        var methodPropsBoolean = Unobfuscator.loadPropsBooleanMethod(classLoader);
        var dataUsageActivityClass = WppCore.getDataUsageActivityClass(classLoader);
        com.wa.toolkit.xposed.core.FeatureManager.safeHookMethod(methodPropsBoolean, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                var list = ReflectionUtils.findInstancesOfType(param.args, Integer.class);
                int i = (int) list.get(0).second;

                var propValue = propsBoolean.get(i);
                if (propValue != null) {
                    if (i == 4023) {
                        if (ReflectionUtils.isCalledFromClass(dataUsageActivityClass)) return;
                    }
                    param.setResult(propValue);
                }
            }
        });

        var methodPropsInteger = Unobfuscator.loadPropsIntegerMethod(classLoader);
        com.wa.toolkit.xposed.core.FeatureManager.safeHookMethod(methodPropsInteger, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                var list = ReflectionUtils.findInstancesOfType(param.args, Integer.class);
                int i = (int) list.get(0).second;
                var propValue = propsInteger.get(i);
                if (propValue == null) return;
                param.setResult(propValue);
            }
        });
    }

    @NonNull
    @Override
    public String getPluginName() {
        return "SystemProperties";
    }
}
