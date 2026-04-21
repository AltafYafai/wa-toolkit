package com.wa.toolkit.xposed.features.others;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.wa.toolkit.xposed.core.Feature;
import com.wa.toolkit.xposed.core.WppCore;
import com.wa.toolkit.xposed.core.components.FMessageWpp;
import com.wa.toolkit.xposed.core.devkit.Unobfuscator;
import com.wa.toolkit.xposed.features.listeners.ConversationItemListener;
import com.wa.toolkit.xposed.utils.DesignUtils;
import com.wa.toolkit.xposed.utils.ReflectionUtils;
import com.wa.toolkit.xposed.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;

public class SmartReply extends Feature {

    private String lastIncomingMessage = "";
    private LinearLayout suggestionsLayout;
    private HorizontalScrollView scrollView;

    public SmartReply(ClassLoader loader, XSharedPreferences preferences) {
        super(loader, preferences);
    }

    @Override
    public void doHook() throws Throwable {
        if (!prefs.getBoolean("smart_reply", false)) return;

        Class<?> conversationFragmentClass = Unobfuscator.loadConversationFragmentClass(classLoader);

        XposedBridge.hookAllMethods(conversationFragmentClass, "onViewCreated", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                View view = (View) param.args[0];
                setupSmartReplyUI(view);
            }
        });

        ConversationItemListener.conversationListeners.add(new ConversationItemListener.OnConversationItemListener() {
            @Override
            public void onItemBind(FMessageWpp fMessage, ViewGroup view, int position, View convertView) throws Throwable {
                if (!fMessage.getKey().isFromMe && fMessage.getMessageStr() != null) {
                    lastIncomingMessage = fMessage.getMessageStr();
                    updateSuggestions();
                }
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void setupSmartReplyUI(View root) {
        try {
            int entryId = Utils.getID("entry", "id");
            EditText entry = root.findViewById(entryId);
            if (entry == null) return;

            ViewGroup parent = (ViewGroup) entry.getParent();
            if (parent == null) return;

            Context context = root.getContext();
            scrollView = new HorizontalScrollView(context);
            scrollView.setHorizontalScrollBarEnabled(false);
            
            suggestionsLayout = new LinearLayout(context);
            suggestionsLayout.setOrientation(LinearLayout.HORIZONTAL);
            suggestionsLayout.setPadding(Utils.dipToPixels(8), 0, Utils.dipToPixels(8), 0);
            scrollView.addView(suggestionsLayout);

            updateSuggestions();

            // Insert above the input field
            int index = parent.indexOfChild(entry);
            parent.addView(scrollView, index, new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, 
                    Utils.dipToPixels(48)
            ));

        } catch (Exception e) {
            logDebug("Error setting up Smart Reply UI", e);
        }
    }

    private void updateSuggestions() {
        if (suggestionsLayout == null) return;

        suggestionsLayout.post(() -> {
            suggestionsLayout.removeAllViews();
            List<String> suggestions = generateSuggestions(lastIncomingMessage);

            for (String suggestion : suggestions) {
                Button btn = createSuggestionButton(suggestionsLayout.getContext(), suggestion);
                suggestionsLayout.addView(btn);
            }
        });
    }

    private List<String> generateSuggestions(String context) {
        List<String> suggestions = new ArrayList<>();
        if (context == null || context.isEmpty()) {
            suggestions.add("Hello!");
            suggestions.add("How are you?");
            suggestions.add("Hey");
        } else {
            String lower = context.toLowerCase();
            if (lower.contains("how are you")) {
                suggestions.add("I'm good, thanks!");
                suggestions.add("Doing well, you?");
            } else if (lower.contains("where")) {
                suggestions.add("On my way!");
                suggestions.add("Almost there.");
            } else if (lower.contains("thanks") || lower.contains("thank")) {
                suggestions.add("You're welcome!");
                suggestions.add("No problem.");
            } else {
                suggestions.add("OK");
                suggestions.add("Sounds good!");
                suggestions.add("I'll let you know.");
            }
        }
        return suggestions;
    }

    private Button createSuggestionButton(Context context, String text) {
        Button btn = new Button(context);
        btn.setText(text);
        btn.setAllCaps(false);
        btn.setTextSize(13);
        
        var lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Utils.dipToPixels(36)
        );
        lp.setMargins(Utils.dipToPixels(4), Utils.dipToPixels(6), Utils.dipToPixels(4), Utils.dipToPixels(6));
        btn.setLayoutParams(lp);

        int bgColor = DesignUtils.getPrimarySurfaceColor();
        int textColor = DesignUtils.getPrimaryTextColor();
        
        btn.setBackground(DesignUtils.createDrawable("selector_bg", bgColor));
        btn.setTextColor(textColor);
        btn.setPadding(Utils.dipToPixels(12), 0, Utils.dipToPixels(12), 0);

        btn.setOnClickListener(v -> {
            Activity activity = WppCore.getCurrentActivity();
            if (activity != null) {
                EditText entry = activity.findViewById(Utils.getID("entry", "id"));
                if (entry != null) {
                    entry.setText(text);
                    entry.setSelection(text.length());
                }
            }
        });
        
        return btn;
    }

    @NonNull
    @Override
    public String getPluginName() {
        return "Smart Reply";
    }
}
