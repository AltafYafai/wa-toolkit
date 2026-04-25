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

    public AutomationSuite(ClassLoader loader, XSharedPreferences preferences) {
        super(loader, preferences);
        this.store = AutomationStore.getInstance(Utils.getApplication());
    }

    @Override
    public void doHook() throws Throwable {
        XposedBridge.log("[WAE] AutomationSuite initialized");
        
        if (prefs.getBoolean("auto_reply_enabled", false)) {
            XposedBridge.log("[WAE] Auto-Reply enabled");
            setupAutoReply();
        }

        if (prefs.getBoolean("scheduler_enabled", false)) {
            XposedBridge.log("[WAE] Scheduler enabled");
            startScheduler();
        }
    }

    private void setupAutoReply() {
        ConversationItemListener.conversationListeners.add(new ConversationItemListener.OnConversationItemListener() {
            @Override
            public void onItemBind(FMessageWpp fMessage, android.view.ViewGroup view, int position, android.view.View convertView) throws Throwable {
                if (fMessage.getKey().isFromMe) return;

                String incomingText = fMessage.getMessageStr();
                if (incomingText == null || incomingText.isEmpty()) return;

                List<AutomationStore.AutoReply> replies = store.getEnabledAutoReplies();
                for (AutomationStore.AutoReply reply : replies) {
                    if (shouldReply(incomingText, reply)) {
                        String jid = fMessage.getKey().remoteJid.getPhoneRawString();
                        XposedBridge.log("[WAE] Auto-replying to " + jid + " with: " + reply.content);
                        sendMessageMainThread(WppCore.stripJID(jid), reply.content);
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
                    XposedBridge.log("[WAE] Scheduler Timer Error: " + e.getMessage());
                }
            }
        }, 5000, 60000); // Start after 5s, check every minute
        XposedBridge.log("[WAE] Scheduler Timer started");
    }

    private void checkScheduledMessages() {
        long now = System.currentTimeMillis();
        List<AutomationStore.ScheduledMessage> pending = store.getPendingMessages(now);

        if (!pending.isEmpty()) {
            XposedBridge.log("[WAE] Found " + pending.size() + " pending scheduled messages");
        }

        for (AutomationStore.ScheduledMessage msg : pending) {
            XposedBridge.log("[WAE] Processing scheduled message " + msg.id + " to " + msg.jid);
            sendMessageMainThread(WppCore.stripJID(msg.jid), msg.content);
            // We update status immediately to avoid re-sending, assuming sendMessageMainThread starts the process
            store.updateMessageStatus(msg.id, 1); 
        }
    }

    private void sendMessageMainThread(final String number, final String message) {
        mainHandler.post(() -> {
            try {
                XposedBridge.log("[WAE] Sending message via Main Thread to: " + number);
                WppCore.sendMessage(number, message);
            } catch (Exception e) {
                XposedBridge.log("[WAE] Failed to send message on Main Thread: " + e.getMessage());
            }
        });
    }

    public static void bulkSend(String message, List<String> jids) {
        // Bulk send should also ideally use Main Thread for each message or a shared handler
        for (String jid : jids) {
            WppCore.sendMessage(WppCore.stripJID(jid), message);
            try {
                Thread.sleep(800); 
            } catch (InterruptedException ignored) {}
        }
    }

    @NonNull
    @Override
    public String getPluginName() {
        return "Automation Suite";
    }
}
