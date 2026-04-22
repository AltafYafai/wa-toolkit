package com.wa.toolkit.xposed.features.others;

import androidx.annotation.NonNull;

import com.wa.toolkit.xposed.core.Feature;
import com.wa.toolkit.xposed.core.devkit.Unobfuscator;
import com.wa.toolkit.xposed.utils.ReflectionUtils;

import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class ChatFilters extends Feature {
    public ChatFilters(@NonNull ClassLoader classLoader, @NonNull XSharedPreferences preferences) {
        super(classLoader, preferences);
    }

    @Override
    public void doHook() throws Throwable {
        if (!prefs.getBoolean("separategroups", false)) return;

        var filterAdaperClass = Unobfuscator.loadFilterAdaperClass(classLoader);
        XposedBridge.hookAllConstructors(filterAdaperClass, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                var list = ReflectionUtils.findInstancesOfType(param.args, List.class);
                if (!list.isEmpty()) {
                    var argResult = list.get(0);
                    var newList = new ArrayList<Object>(argResult.second);
                    newList.removeIf(item -> {
                        try {
                            var field = Unobfuscator.loadFilterItemNameField(classLoader);
                            var name = field.get(item);
                            return name == null || name.equals("CONTACTS_FILTER") || name.equals("GROUP_FILTER");
                        } catch (Exception e) {
                            return false;
                        }
                    });
                    param.args[argResult.first] = newList;
                }
            }
        });
        var methodSetFilter = ReflectionUtils.findMethodUsingFilter(filterAdaperClass, method -> method.getParameterCount() == 1 && method.getParameterTypes()[0].equals(int.class));

        com.wa.toolkit.xposed.core.FeatureManager.safeHookMethod(methodSetFilter, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                var index = (int) param.args[0];
                var field = ReflectionUtils.getFieldByType(methodSetFilter.getDeclaringClass(), List.class);
                var list = (List) field.get(param.thisObject);
                if (list == null || index >= list.size()) {
                    param.setResult(null);
                }
            }
        });
    }

    @NonNull
    @Override
    public String getPluginName() {
        return "Chat Filters";
    }
}
