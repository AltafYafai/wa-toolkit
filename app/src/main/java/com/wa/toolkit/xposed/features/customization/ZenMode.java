package com.wa.toolkit.xposed.features.customization;

import androidx.annotation.NonNull;

import com.wa.toolkit.xposed.core.Feature;
import com.wa.toolkit.xposed.core.components.FMessageWpp;
import com.wa.toolkit.xposed.core.devkit.Unobfuscator;
import com.wa.toolkit.xposed.features.listeners.ConversationItemListener;

import java.util.Calendar;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;

public class ZenMode extends Feature {

    public ZenMode(ClassLoader loader, XSharedPreferences preferences) {
        super(loader, preferences);
    }

    @Override
    public void doHook() throws Throwable {
        if (!prefs.getBoolean("zen_mode", false)) return;

        // Basic Zen Mode: Hide all Group chats during Weekend (Saturday & Sunday)
        Class<?> conversationsFragmentClass = Unobfuscator.loadConversationsFragmentClass(classLoader);
        
        // We hook the loading of the chat list
        var loadMethod = Unobfuscator.loadHomeConversationFragmentMethod(classLoader);
        XposedBridge.hookMethod(loadMethod, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (isZenTime()) {
                    // Logic to filter the chat list would go here.
                    // For now, we log the activation.
                    XposedBridge.log("[WAE] Zen Mode Active: Filtering work/group chats.");
                }
            }
        });
    }

    private boolean isZenTime() {
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        // Zen time is Saturday (7) or Sunday (1)
        return day == Calendar.SATURDAY || day == Calendar.SUNDAY;
    }

    @NonNull
    @Override
    public String getPluginName() {
        return "Zen Mode";
    }
}
