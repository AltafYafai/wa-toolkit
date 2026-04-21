package com.wa.toolkit.xposed.features.general;

import android.view.View;

import androidx.annotation.NonNull;

import com.wa.toolkit.xposed.core.Feature;
import com.wa.toolkit.xposed.core.devkit.Unobfuscator;
import com.wa.toolkit.xposed.utils.ReflectionUtils;

import org.luckypray.dexkit.query.enums.StringMatchType;

import java.lang.reflect.Modifier;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;

public class ProfileStatus extends Feature {

    public ProfileStatus(ClassLoader loader, XSharedPreferences preferences) {
        super(loader, preferences);
    }

    @Override
    public void doHook() throws Exception {
        if (!prefs.getBoolean("disable_profile_status", false)) return;

        var refreshStatusClass = Unobfuscator.loadRefreshStatusClass(classLoader);
        var photoProfileClass = Unobfuscator.loadWDSProfilePhotoClass(classLoader);
        var convClass = Unobfuscator.loadConversationsFragmentClass(classLoader);
        var jidClass = Unobfuscator.loadJidClass(classLoader);
        var method = ReflectionUtils.findMethodUsingFilter(convClass, m -> m.getParameterCount() > 0 && !Modifier.isStatic(m.getModifiers()) && m.getParameterTypes()[0] == View.class && ReflectionUtils.findIndexOfType(m.getParameterTypes(), jidClass) != -1);
        var field = ReflectionUtils.getFieldByExtendType(convClass, refreshStatusClass);
        
        XposedBridge.hookMethod(method, new XC_MethodHook() {
            private Object backup;

            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                this.backup = field.get(param.thisObject);
                field.set(param.thisObject, null);
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                field.set(param.thisObject, this.backup);
            }
        });


        XposedBridge.hookAllMethods(photoProfileClass, "setStatusIndicatorEnabled", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if ((boolean) param.args[0]) {
                    param.setResult(null);
                }
            }
        });
    }

    @NonNull
    @Override
    public String getPluginName() {
        return "ProfileStatus";
    }
}
