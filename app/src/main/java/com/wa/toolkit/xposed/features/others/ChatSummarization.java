package com.wa.toolkit.xposed.features.others;

import android.app.Activity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.wa.toolkit.xposed.core.Feature;
import com.wa.toolkit.xposed.core.WppCore;
import com.wa.toolkit.xposed.core.db.MessageStore;
import com.wa.toolkit.xposed.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedHelpers;

public class ChatSummarization extends Feature {

    public ChatSummarization(ClassLoader loader, XSharedPreferences preferences) {
        super(loader, preferences);
    }

    @Override
    public void doHook() throws Throwable {
        if (!prefs.getBoolean("chat_summarization", false)) return;

        MenuHome.menuItems.add((menu, activity) -> {
            if (WppCore.getCurrentConversation() != null) {
                var item = menu.add(0, 0, 0, "Summarize Chat");
                item.setOnMenuItemClickListener(i -> {
                    summarizeCurrentChat(activity);
                    return true;
                });
            }
        });
    }

    private void summarizeCurrentChat(Activity activity) {
        try {
            String chatTitle = WppCore.getCurrentChatTitle();
            // Extract last 20 messages for summarization
            List<String> messages = extractLastMessages(20);
            
            if (messages.isEmpty()) {
                Utils.showToast("No messages found to summarize", Toast.LENGTH_SHORT);
                return;
            }

            String context = TextUtils.join("\n", messages);
            // In a real implementation, this string would be sent to an LLM
            // For now, we show a mock summary
            Utils.showToast("Summarizing " + chatTitle + "...", Toast.LENGTH_LONG);
            
            // Mock summary result
            String summary = "Summary of " + chatTitle + ": Discussion about the upcoming meeting and dinner plans.";
            
            WppCore.getCurrentActivity().runOnUiThread(() -> {
                com.wa.toolkit.xposed.core.components.AlertDialogWpp dialog = new com.wa.toolkit.xposed.core.components.AlertDialogWpp(activity);
                dialog.setTitle("Chat Summary");
                dialog.setMessage(summary);
                dialog.setPositiveButton("Copy", (d, w) -> {
                    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) activity.getSystemService(android.content.Context.CLIPBOARD_SERVICE);
                    android.content.ClipData clip = android.content.ClipData.newPlainText("Chat Summary", summary);
                    clipboard.setPrimaryClip(clip);
                });
                dialog.setNegativeButton("Close", null);
                dialog.show();
            });

        } catch (Exception e) {
            logDebug("Error summarizing chat", e);
        }
    }

    private List<String> extractLastMessages(int limit) {
        List<String> messages = new ArrayList<>();
        // Logic to query local WhatsApp DB or in-memory message store
        // (Simplified placeholder)
        return messages;
    }

    @NonNull
    @Override
    public String getPluginName() {
        return "Chat Summarization";
    }
}
