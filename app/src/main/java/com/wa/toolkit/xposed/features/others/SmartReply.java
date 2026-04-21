package com.wa.toolkit.xposed.features.others;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.wa.toolkit.xposed.core.Feature;
import com.wa.toolkit.xposed.core.WppCore;
import com.wa.toolkit.xposed.utils.DesignUtils;
import com.wa.toolkit.xposed.utils.ReflectionUtils;
import com.wa.toolkit.xposed.utils.Utils;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;

public class SmartReply extends Feature {

    public SmartReply(ClassLoader loader, XSharedPreferences preferences) {
        super(loader, preferences);
    }

    @Override
    public void doHook() throws Throwable {
        if (!prefs.getBoolean("smart_reply", false)) return;

        Class<?> conversationFragmentClass = com.wa.toolkit.xposed.core.devkit.Unobfuscator.loadConversationFragmentClass(classLoader);

        XposedBridge.hookAllMethods(conversationFragmentClass, "onViewCreated", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                View view = (View) param.args[0];
                setupSmartReplyUI(view);
            }
        });
    }

    private void setupSmartReplyUI(View root) {
        try {
            int entryId = Utils.getID("entry", "id");
            EditText entry = root.findViewById(entryId);
            if (entry == null) return;

            ViewGroup parent = (ViewGroup) entry.getParent();
            if (parent == null) return;

            Context context = root.getContext();
            HorizontalScrollView scrollView = new HorizontalScrollView(context);
            LinearLayout layout = new LinearLayout(context);
            layout.setOrientation(LinearLayout.HORIZONTAL);
            scrollView.addView(layout);

            // Add some mock AI suggestions (In a real scenario, these would come from an LLM)
            String[] suggestions = {"Sounds good!", "I'll be there.", "Can't wait!", "Thanks!", "No problem."};

            for (String suggestion : suggestions) {
                Button btn = new Button(context);
                btn.setText(suggestion);
                btn.setAllCaps(false);
                btn.setTextSize(12);
                btn.setOnClickListener(v -> {
                    entry.setText(suggestion);
                    entry.setSelection(suggestion.length());
                });
                layout.addView(btn);
            }

            // Insert above the input field
            int index = parent.indexOfChild(entry);
            parent.addView(scrollView, index, new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, 
                    Utils.dipToPixels(40)
            ));

        } catch (Exception e) {
            logDebug("Error setting up Smart Reply UI", e);
        }
    }

    @NonNull
    @Override
    public String getPluginName() {
        return "Smart Reply";
    }
}
