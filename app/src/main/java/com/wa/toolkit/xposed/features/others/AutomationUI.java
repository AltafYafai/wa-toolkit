package com.wa.toolkit.xposed.features.others;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.wa.toolkit.xposed.core.Feature;
import com.wa.toolkit.xposed.core.WppCore;
import com.wa.toolkit.xposed.core.db.AutomationStore;
import com.wa.toolkit.xposed.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedHelpers;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;

public class AutomationUI extends Feature {

    private AutomationStore store;

    public AutomationUI(ClassLoader loader, XSharedPreferences preferences) {
        super(loader, preferences);
        this.store = AutomationStore.getInstance(Utils.getApplication());
    }

    @Override
    public void doHook() throws Throwable {
        hookConversationMenu();
        registerBroadcastReceiver();
    }

    private void registerBroadcastReceiver() {
        Context app = Utils.getApplication();
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, android.content.Intent intent) {
                Activity activity = com.wa.toolkit.xposed.core.ActivityStateRegistry.getCurrentActivity();
                if (activity != null) {
                    showAutomationManager(activity);
                }
            }
        };
        IntentFilter filter = new IntentFilter("com.wa.toolkit.OPEN_AUTOMATION_MANAGER");
        
        if (android.os.Build.VERSION.SDK_INT >= 34) {
            app.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            app.registerReceiver(receiver, filter);
        }
    }

    private void hookConversationMenu() {
        com.wa.toolkit.xposed.core.FeatureManager.safeFindAndHookMethod(
                "com.whatsapp.conversation.ConversationHelper", classLoader,
                "A00", // This is a common obfuscated name for menu creation, might need adjustment
                Activity.class, Menu.class, int.class,
                new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Activity activity = (Activity) param.args[0];
                Menu menu = (Menu) param.args[1];
                
                MenuItem item = menu.add(0, 0, 0, "⏰ Schedule Message");
                item.setOnMenuItemClickListener(menuItem -> {
                    showScheduleDialog(activity);
                    return true;
                });
            }
        });
        
        // Alternative hook for modern WA
        com.wa.toolkit.xposed.core.FeatureManager.safeFindAndHookMethod(
                "com.whatsapp.Conversation", classLoader,
                "onCreateOptionsMenu", Menu.class,
                new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Activity activity = (Activity) param.thisObject;
                Menu menu = (Menu) param.args[0];
                if (menu.findItem(12345) != null) return; // Prevent duplicates

                MenuItem item = menu.add(0, 12345, 0, "⏰ Schedule Message");
                item.setOnMenuItemClickListener(menuItem -> {
                    showScheduleDialog(activity);
                    return true;
                });
            }
        });
    }

    private void showScheduleDialog(Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Schedule Message");

        LinearLayout layout = new LinearLayout(activity);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);

        final EditText input = new EditText(activity);
        input.setHint("Enter message...");
        layout.addView(input);

        final Calendar calendar = Calendar.getInstance();
        final TextView timeText = new TextView(activity);
        timeText.setText("Selected: " + new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(calendar.getTime()));
        timeText.setPadding(0, 20, 0, 20);
        layout.addView(timeText);

        layout.setOnClickListener(v -> {
            new DatePickerDialog(activity, (view, year, month, dayOfMonth) -> {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                
                new TimePickerDialog(activity, (view1, hourOfDay, minute) -> {
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minute);
                    timeText.setText("Selected: " + new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(calendar.getTime()));
                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
                
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        builder.setView(layout);

        builder.setPositiveButton("Schedule", (dialog, which) -> {
            String message = input.getText().toString();
            if (message.isEmpty()) return;

            String jid = getJidFromActivity(activity);
            if (jid != null) {
                store.addScheduledMessage(jid, message, calendar.getTimeInMillis());
                Toast.makeText(activity, "Message scheduled!", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showAutomationManager(Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Automation Manager");

        ScrollView scrollView = new ScrollView(activity);
        LinearLayout container = new LinearLayout(activity);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(40, 20, 40, 20);

        // --- Scheduled Section ---
        TextView schedHeader = new TextView(activity);
        schedHeader.setText("📅 Scheduled Messages");
        schedHeader.setTextSize(18);
        schedHeader.setPadding(0, 20, 0, 10);
        container.addView(schedHeader);

        List<AutomationStore.ScheduledMessage> pending = store.getPendingMessages(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 365);
        for (AutomationStore.ScheduledMessage msg : pending) {
            TextView tv = new TextView(activity);
            String date = new SimpleDateFormat("MM/dd HH:mm", Locale.getDefault()).format(new Date(msg.time));
            tv.setText("[" + date + "] To " + msg.jid + ":\n" + msg.content);
            tv.setPadding(0, 10, 0, 10);
            container.addView(tv);
        }

        // --- Auto Reply Section ---
        TextView replyHeader = new TextView(activity);
        replyHeader.setText("\n🤖 Auto-Reply Rules");
        replyHeader.setTextSize(18);
        replyHeader.setPadding(0, 20, 0, 10);
        container.addView(replyHeader);

        container.addView(createAddAutoReplyButton(activity));

        List<AutomationStore.AutoReply> replies = store.getEnabledAutoReplies();
        for (AutomationStore.AutoReply reply : replies) {
            TextView tv = new TextView(activity);
            tv.setText("If contains '" + reply.keyword + "' -> '" + reply.content + "'");
            tv.setPadding(0, 10, 0, 10);
            container.addView(tv);
        }

        scrollView.addView(container);
        builder.setView(scrollView);
        builder.setPositiveButton("Close", null);
        builder.show();
    }

    private TextView createAddAutoReplyButton(Activity activity) {
        TextView btn = new TextView(activity);
        btn.setText("[+ Add Rule]");
        btn.setTextColor(Color.BLUE);
        btn.setPadding(0, 10, 0, 10);
        btn.setOnClickListener(v -> {
            showAddReplyDialog(activity);
        });
        return btn;
    }

    private void showAddReplyDialog(Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("New Auto-Reply");

        LinearLayout layout = new LinearLayout(activity);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);

        final EditText keyword = new EditText(activity);
        keyword.setHint("Keyword...");
        layout.addView(keyword);

        final EditText reply = new EditText(activity);
        reply.setHint("Reply message...");
        layout.addView(reply);

        builder.setView(layout);
        builder.setPositiveButton("Save", (dialog, which) -> {
            String kw = keyword.getText().toString();
            String re = reply.getText().toString();
            if (!kw.isEmpty() && !re.isEmpty()) {
                store.addAutoReply(kw, re, 0);
                Toast.makeText(activity, "Rule saved!", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private String getJidFromActivity(Activity activity) {
        try {
            // This depends on WA version, trying a few common fields
            Object contact = XposedHelpers.getObjectField(activity, "A0G"); // Common for Conversation
            if (contact != null) {
                return XposedHelpers.getObjectField(contact, "A02").toString(); // JID field
            }
        } catch (Exception e) {
            try {
                // Fallback attempt
                Intent intent = activity.getIntent();
                return intent.getStringExtra("jid");
            } catch (Exception ignored) {}
        }
        return null;
    }

    @NonNull
    @Override
    public String getPluginName() {
        return "Automation UI";
    }
}
