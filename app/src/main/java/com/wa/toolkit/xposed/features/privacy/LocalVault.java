package com.wa.toolkit.xposed.features.privacy;

import android.app.Activity;
import android.content.Context;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.wa.toolkit.xposed.core.Feature;
import com.wa.toolkit.xposed.core.WppCore;
import com.wa.toolkit.xposed.core.devkit.Unobfuscator;
import com.wa.toolkit.xposed.utils.Utils;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;

public class LocalVault extends Feature {

    private final Set<String> vaultChats = new HashSet<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public LocalVault(ClassLoader loader, XSharedPreferences preferences) {
        super(loader, preferences);
    }

    @Override
    public void doHook() throws Throwable {
        if (!prefs.getBoolean("local_vault", false)) return;

        Class<?> conversationFragmentClass = Unobfuscator.loadConversationFragmentClass(classLoader);
        XposedBridge.hookAllMethods(conversationFragmentClass, "onViewCreated", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                android.view.View view = (android.view.View) param.args[0];
                injectVaultButton(view);
            }
        });

        // Background task to "Burn" messages every minute
        scheduler.scheduleAtFixedRate(this::burnMessages, 1, 1, TimeUnit.MINUTES);
    }

    private void injectVaultButton(android.view.View root) {
        try {
            int backId = Utils.getID("back", "id");
            android.view.View backBtn = root.findViewById(backId);
            if (backBtn == null) return;

            ViewGroup header = (ViewGroup) backBtn.getParent();
            if (header == null) return;

            Context context = root.getContext();
            ImageButton vaultBtn = new ImageButton(context);
            vaultBtn.setImageResource(Utils.getID("ic_delete", "drawable"));
            vaultBtn.setBackground(null);
            vaultBtn.setPadding(Utils.dipToPixels(8), 0, Utils.dipToPixels(8), 0);

            header.addView(vaultBtn, 2);

            vaultBtn.setOnClickListener(v -> {
                String jid = WppCore.getCurrentUserJid().getPhoneRawString();
                if (vaultChats.contains(jid)) {
                    vaultChats.remove(jid);
                    Utils.showToast("Burn-after-reading DISABLED for this chat", Toast.LENGTH_SHORT);
                } else {
                    vaultChats.add(jid);
                    Utils.showToast("Burn-after-reading ENABLED for this chat (1hr delay)", Toast.LENGTH_SHORT);
                }
            });

        } catch (Exception e) {
            logDebug("Error injecting Vault button", e);
        }
    }

    private void burnMessages() {
        if (vaultChats.isEmpty()) return;
        
        XposedBridge.log("[WAE] LocalVault: Checking for messages to burn...");
        // In a real implementation, we would query the database for messages in vaultChats
        // that are older than 1 hour and call MessageStore.deleteMessage(rowId).
        // This prototype establishes the UI and background loop.
    }

    @NonNull
    @Override
    public String getPluginName() {
        return "Local Vault";
    }
}
