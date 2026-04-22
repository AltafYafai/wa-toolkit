package com.wa.toolkit.xposed.features.general;

import android.view.Menu;
import android.view.View;

import androidx.annotation.NonNull;

import com.wa.toolkit.xposed.core.Feature;
import com.wa.toolkit.xposed.core.WppCore;
import com.wa.toolkit.xposed.core.devkit.Unobfuscator;
import com.wa.toolkit.xposed.utils.Utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Objects;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class SearchCustomization extends Feature {

    public SearchCustomization(ClassLoader loader, XSharedPreferences preferences) {
        super(loader, preferences);
    }

    @Override
    public void doHook() throws Exception {
        var filterChats = prefs.getString("chatfilter", "2");

        Method searchbar = Unobfuscator.loadViewAddSearchBarMethod(classLoader);
        var searchBarID = Utils.getID("my_search_bar", "id");

        com.wa.toolkit.xposed.core.FeatureManager.safeHookMethod(searchbar, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                View view = null;
                if (param.args[0] instanceof View) {
                    view = (View) param.args[0];
                } else {
                    var auxFace = ((Method) param.method).getParameterTypes()[0];
                    var method = ReflectionUtils.findMethodUsingFilter(auxFace, m -> m.getReturnType() == View.class);
                    if (method != null) {
                        var currentActivity = WppCore.getCurrentActivity();
                        view = (View) method.invoke(param.args[0], currentActivity);
                    }
                }

                if (view != null && (view.getId() == searchBarID || view.findViewById(searchBarID) != null) && !Objects.equals(filterChats, "2")) {
                    param.setResult(null);
                }
            }
        });

        try {
            if (!Objects.equals(filterChats, "2")) {
                var loadMySearchBar = Unobfuscator.loadMySearchBarMethod(classLoader);
                com.wa.toolkit.xposed.core.FeatureManager.safeHookMethod(loadMySearchBar, XC_MethodReplacement.DO_NOTHING);
            }
        } catch (Exception ignored) {
        }


        try {
            Method addSeachBar = Unobfuscator.loadAddOptionSearchBarMethod(classLoader);
            com.wa.toolkit.xposed.core.FeatureManager.safeHookMethod(addSeachBar, new XC_MethodHook() {
                private Object homeActivity;
                private Field pageIdField;
                private int originPageId;

                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if (!Objects.equals(filterChats, "1"))
                        return;
                    homeActivity = param.thisObject;
                    if (Modifier.isStatic(param.method.getModifiers())) {
                        homeActivity = param.args[0];
                    }
                    pageIdField = Unobfuscator.loadHomePageIdField(classLoader);
                    originPageId = 0;
                    if (pageIdField.getType() == int.class) {
                        originPageId = pageIdField.getInt(homeActivity);
                        pageIdField.setInt(homeActivity, 1);
                    }
                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    if (originPageId != 0) {
                        pageIdField.setInt(homeActivity, originPageId);
                    }
                }
            });
        } catch (Throwable ignored) {
        }

        com.wa.toolkit.xposed.core.FeatureManager.safeFindAndHookMethod(WppCore.getHomeActivityClass(classLoader), "onPrepareOptionsMenu", Menu.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                var menu = (Menu) param.args[0];
                var item = menu.findItem(Utils.getID("menuitem_search", "id"));
                if (item != null) {
                    item.setVisible(Objects.equals(filterChats, "1"));
                }
            }
        });
    }

    @NonNull
    @Override
    public String getPluginName() {
        return "SearchCustomization";
    }

    private static class ReflectionUtils {
        public static Method findMethodUsingFilter(Class<?> clazz, java.util.function.Predicate<Method> filter) {
            for (Method m : clazz.getDeclaredMethods()) {
                if (filter.test(m)) return m;
            }
            return null;
        }
    }
}
