package com.wa.toolkit.xposed.features.general;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.wa.toolkit.listeners.OnMultiClickListener;
import com.wa.toolkit.xposed.core.Feature;
import com.wa.toolkit.xposed.core.WppCore;
import com.wa.toolkit.xposed.core.components.FMessageWpp;
import com.wa.toolkit.xposed.core.devkit.Unobfuscator;
import com.wa.toolkit.xposed.features.listeners.ConversationItemListener;
import com.wa.toolkit.xposed.utils.Utils;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;

public class DoubleTapReaction extends Feature {

    public DoubleTapReaction(ClassLoader loader, XSharedPreferences preferences) {
        super(loader, preferences);
    }

    @Override
    public void doHook() throws Exception {
        if (!prefs.getBoolean("doubletap2like", false)) return;

        var emoji = prefs.getString("doubletap2like_emoji", "👍");
        var conversationRowClass = Unobfuscator.loadConversationRowClass(classLoader);

        XposedBridge.hookAllConstructors(conversationRowClass, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                var viewGroup = (ViewGroup) param.thisObject;
                viewGroup.setOnTouchListener(null);
            }
        });

        ConversationItemListener.conversationListeners.add(new ConversationItemListener.OnConversationItemListener() {
            @Override
            public void onItemBind(FMessageWpp fMessage, ViewGroup view, int position, View convertView) {
                var onMultiClickListener = new OnMultiClickListener(2, 500) {
                    @Override
                    public void onMultiClick(View view) {
                        var reactionView = (ViewGroup) view.findViewById(Utils.getID("reactions_bubble_layout", "id"));
                        if (reactionView != null && reactionView.getVisibility() == View.VISIBLE) {
                            for (int i = 0; i < reactionView.getChildCount(); i++) {
                                if (reactionView.getChildAt(i) instanceof TextView textView) {
                                    if (textView.getText().toString().contains(emoji)) {
                                        WppCore.sendReaction("", fMessage.getObject());
                                        return;
                                    }
                                }
                            }
                        }
                        WppCore.sendReaction(emoji, fMessage.getObject());
                    }
                };
                view.setOnClickListener(onMultiClickListener);
            }
        });
    }

    @NonNull
    @Override
    public String getPluginName() {
        return "DoubleTapReaction";
    }
}
