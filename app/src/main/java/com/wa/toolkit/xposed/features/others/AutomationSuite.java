package com.wa.toolkit.xposed.features.others;

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

    public AutomationSuite(ClassLoader loader, XSharedPreferences preferences) {
        super(loader, preferences);
        this.store = AutomationStore.getInstance(Utils.getApplication());
    }

    @Override
    public void doHook() throws Throwable {
        if (prefs.getBoolean("auto_reply_enabled", false)) {
            setupAutoReply();
        }

        if (prefs.getBoolean("scheduler_enabled", false)) {
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
                        WppCore.sendMessage(WppCore.stripJID(jid), reply.content);
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
                checkScheduledMessages();
            }
        }, 0, 60000); // Check every minute
    }

    private void checkScheduledMessages() {
        long now = System.currentTimeMillis();
        List<AutomationStore.ScheduledMessage> pending = store.getPendingMessages(now);

        for (AutomationStore.ScheduledMessage msg : pending) {
            try {
                WppCore.sendMessage(WppCore.stripJID(msg.jid), msg.content);
                store.updateMessageStatus(msg.id, 1); // Sent
            } catch (Exception e) {
                store.updateMessageStatus(msg.id, 2); // Failed
                XposedBridge.log("[WAE] Failed to send scheduled message: " + e.getMessage());
            }
        }
    }

    public static void bulkSend(String message, List<String> jids) {
        for (String jid : jids) {
            WppCore.sendMessage(WppCore.stripJID(jid), message);
            try {
                Thread.sleep(500); // Small delay to avoid rate limiting/flagging
            } catch (InterruptedException ignored) {}
        }
    }

    @NonNull
    @Override
    public String getPluginName() {
        return "Automation Suite";
    }
}
