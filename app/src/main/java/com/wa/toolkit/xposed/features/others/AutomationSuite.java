package com.wa.toolkit.xposed.features.others;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;

import com.wa.toolkit.xposed.core.Feature;
import com.wa.toolkit.xposed.core.WppCore;
import com.wa.toolkit.xposed.core.components.FMessageWpp;
import com.wa.toolkit.xposed.core.db.AutomationStore;
import com.wa.toolkit.xposed.features.listeners.ConversationItemListener;
import com.wa.toolkit.xposed.utils.Utils;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;

public class AutomationSuite extends Feature {

    private AutomationStore store;
    private Timer schedulerTimer;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private static final String TAG = "[WAE-Scheduler] ";

    public AutomationSuite(ClassLoader loader, XSharedPreferences preferences) {
        super(loader, preferences);
    }

    @Override
    public void doHook() throws Throwable {
        XposedBridge.log(TAG + "AutomationSuite initialized");
        
        // Start scheduler timer regardless of initial pref state
        // We will check the pref inside the timer task to allow dynamic enabling
        startScheduler();

        // Auto-reply needs to be hooked at startup if enabled
        if (prefs.getBoolean("auto_reply_enabled", false)) {
            XposedBridge.log(TAG + "Auto-Reply enabled at startup");
            setupAutoReply();
        }
    }

    private void setupAutoReply() {
        ConversationItemListener.conversationListeners.add(new ConversationItemListener.OnConversationItemListener() {
            @Override
            public void onItemBind(FMessageWpp fMessage, android.view.ViewGroup view, int position, android.view.View convertView) throws Throwable {
                if (fMessage.getKey().isFromMe) return;

                String incomingText = fMessage.getMessageStr();
                if (incomingText == null || incomingText.isEmpty()) return;

                // Ensure store is ready
                if (store == null) store = AutomationStore.getInstance(Utils.getApplication());
                
                List<AutomationStore.AutoReply> replies = store.getEnabledAutoReplies();
                for (AutomationStore.AutoReply reply : replies) {
                    if (shouldReply(incomingText, reply)) {
                        String jid = fMessage.getKey().remoteJid.getPhoneRawString();
                        XposedBridge.log(TAG + "Auto-replying to " + jid);
                        sendMessageMainThread(jid, reply.content);
                        break;
                    }
                }
            }
        });
    }

    private boolean shouldReply(String incomingText, AutomationStore.AutoReply reply) {
        String keyword = reply.keyword.toLowerCase();
        String text = incomingText.toLowerCase();

        switch (reply.matchType) {
            case 0: // Contains
                return text.contains(keyword);
            case 1: // Exact
                return text.equals(keyword);
            case 2: // StartsWith
                return text.startsWith(keyword);
            default:
                return false;
        }
    }

    private void startScheduler() {
        if (schedulerTimer != null) schedulerTimer.cancel();
        schedulerTimer = new Timer();
        schedulerTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    checkScheduledMessages();
                } catch (Exception e) {
                    XposedBridge.log(TAG + "Task Error: " + e.getMessage());
                }
            }
        }, 10000, 60000); // Check every minute, start after 10s
        XposedBridge.log(TAG + "Background timer started");
    }

    private void checkScheduledMessages() {
        // 1. Reload preferences from disk to pick up changes from the UI app
        prefs.reload();
        if (!prefs.getBoolean("scheduler_enabled", false)) {
            return;
        }

        // 2. Ensure we have a context and store
        if (store == null) {
            try {
                store = AutomationStore.getInstance(Utils.getApplication());
            } catch (Exception e) {
                XposedBridge.log(TAG + "Failed to get Store/Context: " + e.getMessage());
                return;
            }
        }

        // 3. Find pending messages
        long now = System.currentTimeMillis();
        List<AutomationStore.ScheduledMessage> pending = store.getPendingMessages(now);

        if (!pending.isEmpty()) {
            XposedBridge.log(TAG + "Found " + pending.size() + " messages to send");
            for (AutomationStore.ScheduledMessage msg : pending) {
                XposedBridge.log(TAG + "Scheduling send for ID " + msg.id + " to " + msg.jid);
                sendMessageMainThread(msg.jid, msg.content);
                store.updateMessageStatus(msg.id, 1); // Mark as sent/processing
            }
        }
    }

    private void sendMessageMainThread(final String jid, final String message) {
        mainHandler.post(() -> {
            try {
                XposedBridge.log(TAG + "Executing send on UI Thread for: " + jid);
                WppCore.sendMessage(jid, message);
            } catch (Exception e) {
                XposedBridge.log(TAG + "Send Error on UI Thread: " + e.getMessage());
            }
        });
    }

    @NonNull
    @Override
    public String getPluginName() {
        return "Automation Suite";
    }
}
