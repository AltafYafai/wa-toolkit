package com.wa.toolkit.xposed.features.others;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.app.AlertDialog;

import androidx.annotation.NonNull;

import com.wa.toolkit.BuildConfig;
import com.wa.toolkit.xposed.core.Feature;
import com.wa.toolkit.xposed.core.WppCore;
import com.wa.toolkit.xposed.core.components.AlertDialogWpp;
import com.wa.toolkit.xposed.utils.DesignUtils;
import com.wa.toolkit.xposed.utils.ResId;
import com.wa.toolkit.xposed.utils.Utils;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedHelpers;

public class MenuHome extends Feature {

    public static HashSet<HomeMenuItem> menuItems = new LinkedHashSet<>();


    public MenuHome(@NonNull ClassLoader classLoader, @NonNull XSharedPreferences preferences) {
        super(classLoader, preferences);
    }

    @Override
    public void doHook() throws Throwable {
        hookMenu();
        // Consolidate all items into a single main menu entry
        menuItems.add(this::InsertEmeraldMenu);
    }

    private void InsertEmeraldMenu(Menu menu, Activity activity) {
        // Emerald Hub Main Menu Entry
        MenuItem emeraldMenu = menu.add(0, 9999, 0, "💚 " + activity.getString(ResId.string.app_name));
        emeraldMenu.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        
        var iconDraw = DesignUtils.getDrawableByName("ic_settings");
        if (iconDraw != null) {
            iconDraw.setTint(DesignUtils.getPrimaryTextColor());
            emeraldMenu.setIcon(iconDraw);
        }

        emeraldMenu.setOnMenuItemClickListener(item -> {
            showEmeraldQuickActions(activity);
            return true;
        });
    }

    private void showEmeraldQuickActions(Activity activity) {
        List<String> options = new ArrayList<>();
        List<Runnable> actions = new ArrayList<>();

        // 1. Open Emerald Hub
        options.add("Emerald Hub Settings");
        actions.add(() -> {
            Intent intent = activity.getPackageManager().getLaunchIntentForPackage(BuildConfig.APPLICATION_ID);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(intent);
        });

        // 2. Automation Manager
        options.add("Automation Manager");
        actions.add(() -> {
            AutomationUI.showAutomationManager(activity);
        });

        // 3. Ghost Mode
        boolean ghostmode = WppCore.getPrivBoolean("ghostmode", false);
        options.add("Ghost Mode: " + (ghostmode ? "ON" : "OFF"));
        actions.add(() -> {
            new AlertDialogWpp(activity)
                .setTitle(activity.getString(ResId.string.ghost_mode_s, (ghostmode ? "ON" : "OFF")))
                .setMessage(activity.getString(ResId.string.ghost_mode_message))
                .setPositiveButton(activity.getString(ResId.string.disable), (dialog, which) -> {
                    WppCore.setPrivBoolean("ghostmode", false);
                    Utils.doRestart(activity);
                })
                .setNegativeButton(activity.getString(ResId.string.enable), (dialog, which) -> {
                    WppCore.setPrivBoolean("ghostmode", true);
                    Utils.doRestart(activity);
                }).show();
        });

        // 4. DND Mode
        boolean dndmode = WppCore.getPrivBoolean("dndmode", false);
        options.add("DND Mode: " + (dndmode ? "ON" : "OFF"));
        actions.add(() -> {
            if (!dndmode) {
                new AlertDialogWpp(activity)
                        .setTitle(activity.getString(ResId.string.dnd_mode_title))
                        .setMessage(activity.getString(ResId.string.dnd_message))
                        .setPositiveButton(activity.getString(ResId.string.activate), (dialog, which) -> {
                            WppCore.setPrivBoolean("dndmode", true);
                            Utils.doRestart(activity);
                        })
                        .setNegativeButton(activity.getString(ResId.string.cancel), (dialog, which) -> dialog.dismiss())
                        .create().show();
            } else {
                WppCore.setPrivBoolean("dndmode", false);
                Utils.doRestart(activity);
            }
        });

        // 5. Freeze Last Seen
        boolean freezelastseen = WppCore.getPrivBoolean("freezelastseen", false);
        options.add("Freeze Last Seen: " + (freezelastseen ? "ON" : "OFF"));
        actions.add(() -> {
            if (!freezelastseen) {
                new AlertDialogWpp(activity)
                        .setTitle(activity.getString(ResId.string.freezelastseen_title))
                        .setMessage(activity.getString(ResId.string.freezelastseen_message))
                        .setPositiveButton(activity.getString(ResId.string.activate), (dialog, which) -> {
                            WppCore.setPrivBoolean("freezelastseen", true);
                            Utils.doRestart(activity);
                        })
                        .setNegativeButton(activity.getString(ResId.string.cancel), (dialog, which) -> dialog.dismiss())
                        .create().show();
            } else {
                WppCore.setPrivBoolean("freezelastseen", false);
                Utils.doRestart(activity);
            }
        });

        // 6. Restart WhatsApp
        options.add("Restart WhatsApp");
        actions.add(() -> Utils.doRestart(activity));

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("💚 Emerald Quick Actions");
        builder.setItems(options.toArray(new String[0]), (dialog, which) -> {
            actions.get(which).run();
        });
        builder.show();
    }

    private void hookMenu() {
        com.wa.toolkit.xposed.core.FeatureManager.safeFindAndHookMethod(WppCore.getHomeActivityClass(classLoader), "onCreateOptionsMenu", Menu.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                var menu = (Menu) param.args[0];
                var activity = (Activity) param.thisObject;
                for (var menuItem : MenuHome.menuItems) {
                    menuItem.addMenu(menu, activity);
                }
            }
        });
    }

    @NonNull
    @Override
    public String getPluginName() {
        return "Menu Home";
    }

    public interface HomeMenuItem {

        void addMenu(Menu menu, Activity activity);

    }
}
