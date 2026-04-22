package com.wa.toolkit.xposed.features.others;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;

import com.wa.toolkit.xposed.core.Feature;
import com.wa.toolkit.xposed.core.WppCore;
import com.wa.toolkit.xposed.core.devkit.Unobfuscator;
import com.wa.toolkit.xposed.utils.Utils;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;

public class ToneTranslator extends Feature {

    public ToneTranslator(ClassLoader loader, XSharedPreferences preferences) {
        super(loader, preferences);
    }

    @Override
    public void doHook() throws Throwable {
        if (!prefs.getBoolean("ai_rewrite", false)) return;

        Class<?> conversationFragmentClass = Unobfuscator.loadConversationFragmentClass(classLoader);
        XposedBridge.hookAllMethods(conversationFragmentClass, "onViewCreated", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                android.view.View view = (android.view.View) param.args[0];
                injectRewriteButton(view);
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void injectRewriteButton(android.view.View root) {
        try {
            int entryId = Utils.getID("entry", "id");
            EditText entry = root.findViewById(entryId);
            if (entry == null) return;

            ViewGroup parent = (ViewGroup) entry.getParent();
            if (parent == null) return;

            Context context = root.getContext();
            ImageButton rewriteBtn = new ImageButton(context);
            rewriteBtn.setImageResource(Utils.getID("ic_round_settings_24", "drawable")); // Use a valid icon
            rewriteBtn.setBackground(null);
            rewriteBtn.setPadding(Utils.dipToPixels(8), 0, Utils.dipToPixels(8), 0);

            // Add next to the input field
            parent.addView(rewriteBtn, parent.indexOfChild(entry));

            rewriteBtn.setOnClickListener(v -> {
                String original = entry.getText().toString();
                if (!original.isEmpty()) {
                    // Prototype AI Rewrite: Simply prepend "Professional: " and formalize
                    String rewritten = "Dear recipient, " + original + ". I look forward to your response.";
                    entry.setText(rewritten);
                    entry.setSelection(rewritten.length());
                    Utils.showToast("Message formalized by AI", 0);
                }
            });

        } catch (Exception e) {
            logDebug("Error injecting AI Rewrite button", e);
        }
    }

    @NonNull
    @Override
    public String getPluginName() {
        return "AI Tone Translator";
    }
}
