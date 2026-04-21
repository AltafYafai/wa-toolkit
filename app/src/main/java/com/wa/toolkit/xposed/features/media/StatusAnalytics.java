package com.wa.toolkit.xposed.features.media;

import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.wa.toolkit.xposed.core.Feature;
import com.wa.toolkit.xposed.core.WppCore;
import com.wa.toolkit.xposed.core.components.FMessageWpp;
import com.wa.toolkit.xposed.core.db.MessageHistory;
import com.wa.toolkit.xposed.core.db.MessageStore;
import com.wa.toolkit.xposed.core.devkit.Unobfuscator;
import com.wa.toolkit.xposed.features.general.Tasker;
import com.wa.toolkit.xposed.utils.ReflectionUtils;
import com.wa.toolkit.xposed.utils.ResId;
import com.wa.toolkit.xposed.utils.Utils;

import org.luckypray.dexkit.query.enums.StringMatchType;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;

public class StatusAnalytics extends Feature {

    public StatusAnalytics(@NonNull ClassLoader classLoader, @NonNull XSharedPreferences preferences) {
        super(classLoader, preferences);
    }

    @Override
    public void doHook() throws Throwable {
        if (!prefs.getBoolean("status_analytics", false)) return;

        var onInsertReceipt = Unobfuscator.loadOnInsertReceipt(classLoader);

        XposedBridge.hookMethod(onInsertReceipt, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                processReceipt(param);
            }
        });
    }

    private void processReceipt(XC_MethodHook.MethodHookParam param) throws Exception {
        Collection collection;
        if (!(param.args[0] instanceof Collection)) {
            collection = Collections.singleton(param.args[0]);
        } else {
            collection = (Collection) param.args[0];
        }
        var jidClass = Unobfuscator.findFirstClassUsingName(classLoader, StringMatchType.EndsWith, "jid.Jid");
        for (var messageStatusUpdateReceipt : collection) {
            var fieldByType = ReflectionUtils.getFieldByType(messageStatusUpdateReceipt.getClass(), int.class);
            var fieldId = ReflectionUtils.getFieldByType(messageStatusUpdateReceipt.getClass(), long.class);
            var fieldByUserJid = ReflectionUtils.getFieldByExtendType(messageStatusUpdateReceipt.getClass(), jidClass);
            var fieldMessage = ReflectionUtils.getFieldByExtendType(messageStatusUpdateReceipt.getClass(), FMessageWpp.TYPE);
            
            int type = fieldByType.getInt(messageStatusUpdateReceipt);
            long id = fieldId.getLong(messageStatusUpdateReceipt);
            
            if (type != 13) continue; // 13 is status view receipt
            
            var userJid = new FMessageWpp.UserJid(fieldByUserJid.get(messageStatusUpdateReceipt));
            AtomicReference<Object> fmessage = new AtomicReference<>();
            try {
                fmessage.set(fieldMessage.get(messageStatusUpdateReceipt));
            } catch (Exception ignored) {
            }

            CompletableFuture.runAsync(() -> {
                var contactName = WppCore.getContactName(userJid);
                var rowId = id;
                if (TextUtils.isEmpty(contactName)) contactName = userJid.getPhoneNumber();

                var sql = MessageStore.getInstance().getDatabase();
                String messageId = "";

                if (fmessage.get() != null) {
                    var fMessageWpp = new FMessageWpp(fmessage.get());
                    rowId = fMessageWpp.getRowId();
                    messageId = fMessageWpp.getKey().messageID;
                }

                checkAndLog(sql, rowId, messageId, userJid.getPhoneRawString());
            });
        }
    }

    private synchronized void checkAndLog(SQLiteDatabase sql, long id, String messageId, String rawJid) {
        try (var cursor = sql.query("message", new String[]{"participant_hash", "key_id"}, "_id = ?", new String[]{String.valueOf(id)}, null, null, null)) {
            if (!cursor.moveToNext()) return;

            var participantHash = cursor.getString(cursor.getColumnIndexOrThrow("participant_hash"));
            if (participantHash != null) {
                // It's a status view
                String finalMessageId = messageId.isEmpty() ? cursor.getString(cursor.getColumnIndexOrThrow("key_id")) : messageId;
                MessageHistory.getInstance().insertStatusView(rawJid, finalMessageId, System.currentTimeMillis());
                
                var contactName = WppCore.getContactName(new FMessageWpp.UserJid(rawJid));
                Tasker.sendTaskerEvent(contactName, WppCore.stripJID(rawJid), "viewed_status");
                
                logDebug("Status viewed by " + rawJid + " for message " + finalMessageId);
            }
        } catch (Exception e) {
            XposedBridge.log(e);
        }
    }

    @NonNull
    @Override
    public String getPluginName() {
        return "Status Analytics";
    }
}
