package com.wa.toolkit.xposed.features.others;

import android.app.Activity;
import android.content.Context;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;

import com.wa.toolkit.xposed.core.Feature;
import com.wa.toolkit.xposed.core.WppCore;
import com.wa.toolkit.xposed.core.components.AlertDialogWpp;
import com.wa.toolkit.xposed.core.devkit.Unobfuscator;
import com.wa.toolkit.xposed.utils.Utils;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;

public class GroupSummarizer extends Feature {

    public GroupSummarizer(ClassLoader loader, XSharedPreferences preferences) {
        super(loader, preferences);
    }

    @Override
    public void doHook() throws Throwable {
        if (!prefs.getBoolean("group_tldr", false)) return;

        Class<?> conversationFragmentClass = Unobfuscator.loadConversationFragmentClass(classLoader);
        XposedBridge.hookAllMethods(conversationFragmentClass, "onViewCreated", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                android.view.View view = (android.view.View) param.args[0];
                injectSummarizeButton(view);
            }
        });
    }

    private void injectSummarizeButton(android.view.View root) {
        try {
            // Find a good place in the header, maybe near the search icon or call button
            int backId = Utils.getID("back", "id");
            android.view.View backBtn = root.findViewById(backId);
            if (backBtn == null) return;

            ViewGroup header = (ViewGroup) backBtn.getParent();
            if (header == null) return;

            Context context = root.getContext();
            ImageButton summaryBtn = new ImageButton(context);
            summaryBtn.setImageResource(Utils.getID("ic_home_black_24dp", "drawable")); // Use a "Home/Summary" icon
            summaryBtn.setBackground(null);
            summaryBtn.setPadding(Utils.dipToPixels(8), 0, Utils.dipToPixels(8), 0);

            header.addView(summaryBtn, 1); // Add after back button

            summaryBtn.setOnClickListener(v -> showSummaryDialog(context));

        } catch (Exception e) {
            logDebug("Error injecting Summarize button", e);
        }
    }

    private void showSummaryDialog(Context context) {
        Activity activity = WppCore.getCurrentActivity();
        if (activity == null) return;

        new AlertDialogWpp(activity)
                .setTitle("Group TL;DR")
                .setMessage("AI Analysis of last 50 messages:\n\n• Everyone is planning a meetup on Friday.\n• John mentioned bringing drinks.\n• Sarah is running late.\n\n(Prototype: Integration with local LLM pending)")
                .setPositiveButton("Cool", null)
                .show();
    }

    @NonNull
    @Override
    public String getPluginName() {
        return "Group Summarizer";
    }
}
