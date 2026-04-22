package com.wa.toolkit.xposed.features.general;

import android.view.View;

import androidx.annotation.NonNull;

import com.wa.toolkit.xposed.core.Feature;
import com.wa.toolkit.xposed.utils.Utils;

import java.util.ArrayList;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedHelpers;

public class FilterItems extends Feature {

    public FilterItems(ClassLoader loader, XSharedPreferences preferences) {
        super(loader, preferences);
    }

    @Override
    public void doHook() throws Exception {
        var filter_items = prefs.getString("filter_items", null);
        if (filter_items == null || !prefs.getBoolean("custom_filters", true)) return;

        var items = filter_items.split("\n");
        var idsFilter = new ArrayList<Integer>();
        for (String item : items) {
            var id = Utils.getID(item, "id");
            if (id > 0) {
                idsFilter.add(id);
            }
        }
        com.wa.toolkit.xposed.core.FeatureManager.safeFindAndHookMethod(View.class, "invalidate", boolean.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                var view = (View) param.thisObject;
                var id = view.getId();
                if (id > 0 && idsFilter.contains(id) && view.getVisibility() == View.VISIBLE) {
                    view.setVisibility(View.GONE);
                }
            }
        });
    }

    @NonNull
    @Override
    public String getPluginName() {
        return "FilterItems";
    }
}
