package com.wa.toolkit.xposed.features.privacy;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.wa.toolkit.xposed.core.Feature;
import com.wa.toolkit.xposed.core.WppCore;
import com.wa.toolkit.xposed.core.components.AlertDialogWpp;
import com.wa.toolkit.xposed.core.devkit.Unobfuscator;
import com.wa.toolkit.xposed.utils.DesignUtils;
import com.wa.toolkit.xposed.utils.Utils;

import java.lang.reflect.Method;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;

public class StealthTyping extends Feature {

    public StealthTyping(ClassLoader loader, XSharedPreferences preferences) {
        super(loader, preferences);
    }

    @Override
    public void doHook() throws Throwable {
        // 1. Intercept "Typing..." indicators if toggle is ON
        Method ghostMethod = Unobfuscator.loadGhostModeMethod(classLoader);
        XposedBridge.hookMethod(ghostMethod, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (prefs.getBoolean("stealth_typing", false)) {
                    param.setResult(null);
                }
            }
        });

        // 2. Inject Sandbox Button into ConversationFragment
        Class<?> conversationFragmentClass = Unobfuscator.loadConversationFragmentClass(classLoader);
        XposedBridge.hookAllMethods(conversationFragmentClass, "onViewCreated", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (!prefs.getBoolean("stealth_typing", false)) return;
                android.view.View view = (android.view.View) param.args[0];
                injectSandboxButton(view);
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void injectSandboxButton(android.view.View root) {
        try {
            int entryId = Utils.getID("entry", "id");
            EditText entry = root.findViewById(entryId);
            if (entry == null) return;

            ViewGroup parent = (ViewGroup) entry.getParent();
            if (parent == null) return;

            Context context = root.getContext();
            ImageButton sandboxBtn = new ImageButton(context);
            sandboxBtn.setImageResource(Utils.getID("ic_privacy", "drawable")); // Use a privacy icon
            sandboxBtn.setBackground(null);
            sandboxBtn.setPadding(Utils.dipToPixels(8), 0, Utils.dipToPixels(8), 0);

            // Add next to the input field
            parent.addView(sandboxBtn, parent.indexOfChild(entry));

            sandboxBtn.setOnClickListener(v -> openSandboxDialog(context, entry));

        } catch (Exception e) {
            logDebug("Error injecting Stealth Typing button", e);
        }
    }

    private void openSandboxDialog(Context context, EditText entry) {
        Activity activity = WppCore.getCurrentActivity();
        if (activity == null) return;

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(Utils.dipToPixels(16), Utils.dipToPixels(16), Utils.dipToPixels(16), Utils.dipToPixels(16));

        EditText sandboxInput = new EditText(context);
        sandboxInput.setHint("Type privately here...");
        sandboxInput.setMinLines(3);
        sandboxInput.setGravity(android.view.Gravity.TOP);
        layout.addView(sandboxInput);

        new AlertDialogWpp(activity)
                .setTitle("Stealth Typing Sandbox")
                .setView(layout)
                .setPositiveButton("Send", (dialog, which) -> {
                    String text = sandboxInput.getText().toString();
                    if (!text.isEmpty()) {
                        entry.setText(text);
                        // Trigger the Send button click in WhatsApp
                        int sendId = Utils.getID("send", "id");
                        android.view.View sendBtn = activity.findViewById(sendId);
                        if (sendBtn != null) {
                            sendBtn.performClick();
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @NonNull
    @Override
    public String getPluginName() {
        return "Stealth Typing";
    }
}
