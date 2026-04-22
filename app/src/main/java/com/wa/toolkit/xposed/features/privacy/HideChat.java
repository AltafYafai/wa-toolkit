package com.wa.toolkit.xposed.features.privacy;

import android.view.View;

import androidx.annotation.NonNull;

import com.wa.toolkit.xposed.core.Feature;
import com.wa.toolkit.xposed.core.devkit.Unobfuscator;
import com.wa.toolkit.xposed.utils.ReflectionUtils;
import com.wa.toolkit.xposed.utils.Utils;

import java.util.Objects;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;

public class HideChat extends Feature {

    public HideChat(@NonNull ClassLoader loader, @NonNull XSharedPreferences preferences) {
        super(loader, preferences);
    }

    @Override
    public void doHook() throws Throwable {

        if (!Objects.equals(Utils.getSafeString(prefs, "typearchive", "0"), "0")) {

            var loadArchiveChatClass = Unobfuscator.loadArchiveChatClass(classLoader);

            var setVisibilityMethod = View.class.getDeclaredMethod("setVisibility", int.class);
            var viewField = ReflectionUtils.getFieldByType(loadArchiveChatClass, View.class);

            XposedBridge.hookAllConstructors(loadArchiveChatClass, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Object thiz = param.thisObject;
                    com.wa.toolkit.xposed.core.FeatureManager.safeHookMethod(setVisibilityMethod, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            Object view = viewField.get(thiz);
                            if (view != param.thisObject) return;
                            param.args[0] = View.GONE;
                        }
                    });
                }
            });

        }

    }

    @NonNull
    @Override
    public String getPluginName() {
        return "Hide Chats";
    }
}
