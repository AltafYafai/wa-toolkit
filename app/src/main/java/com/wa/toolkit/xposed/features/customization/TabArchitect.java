package com.wa.toolkit.xposed.features.customization;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.wa.toolkit.xposed.core.Feature;
import com.wa.toolkit.xposed.core.WppCore;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class TabArchitect extends Feature {

    public TabArchitect(ClassLoader loader, XSharedPreferences preferences) {
        super(loader, preferences);
    }

    @Override
    public void doHook() throws Throwable {
        boolean hideChannels = prefs.getBoolean("hide_channels_tab", false);
        boolean hideCommunities = prefs.getBoolean("hide_communities_tab", false);
        boolean hideStatus = prefs.getBoolean("hide_status_tab", false);

        if (!hideChannels && !hideCommunities && !hideStatus) return;

        Class<?> homeActivityClass = WppCore.getHomeActivityClass(classLoader);
        
        XposedBridge.hookAllMethods(homeActivityClass, "onResume", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Activity activity = (Activity) param.thisObject;
                // Attempt to find and hide tab views dynamically
                // This is a generic approach; specific ID-based hiding might be needed for different WA versions
                hideTabs(activity, hideChannels, hideCommunities, hideStatus);
            }
        });
    }

    private void hideTabs(Activity activity, boolean channels, boolean communities, boolean status) {
        try {
            // Placeholder for tab hiding logic
            // In modern WA, tabs are often in a BottomNavigationView or a TabLayout
            // We can search for these views and hide specific children
            View root = activity.getWindow().getDecorView();
            // ... discovery and hiding logic ...
        } catch (Exception e) {
            logDebug("Error re-architecting tabs", e);
        }
    }

    @NonNull
    @Override
    public String getPluginName() {
        return "Tab Architect";
    }
}
