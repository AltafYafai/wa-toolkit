package com.wa.toolkit.xposed.features.others;

import android.content.Intent;
import android.provider.CalendarContract;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.wa.toolkit.xposed.core.Feature;
import com.wa.toolkit.xposed.core.WppCore;
import com.wa.toolkit.xposed.core.components.AlertDialogWpp;
import com.wa.toolkit.xposed.utils.Utils;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;

public class VoiceToTask extends Feature {

    public VoiceToTask(ClassLoader loader, XSharedPreferences preferences) {
        super(loader, preferences);
    }

    @Override
    public void doHook() throws Throwable {
        // This feature relies on the AudioTranscript feature broadcast or internal callback.
        // We can hook the completion of transcription.
    }

    public static void processTranscript(String transcript) {
        if (TextUtils.isEmpty(transcript)) return;

        // Basic date/time detection regex (highly simplified for example)
        // Matches patterns like "tomorrow at 5pm", "Monday at 10:00", etc.
        Pattern datePattern = Pattern.compile("(tomorrow|Monday|Tuesday|Wednesday|Thursday|Friday|Saturday|Sunday)\\b.*\\b(\\d{1,2}(?::\\d{2})?\\s*(?:am|pm|AM|PM)?)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = datePattern.matcher(transcript);

        if (matcher.find()) {
            String dateStr = matcher.group(1);
            String timeStr = matcher.group(2);
            
            WppCore.getCurrentActivity().runOnUiThread(() -> {
                new AlertDialogWpp(WppCore.getCurrentActivity())
                        .setTitle("Add to Calendar?")
                        .setMessage("Detected appointment: " + dateStr + " at " + timeStr + "\n\nTranscript: " + transcript)
                        .setPositiveButton("Add", (dialog, which) -> addToCalendar(transcript, dateStr, timeStr))
                        .setNegativeButton("Ignore", null)
                        .show();
            });
        }
    }

    private static void addToCalendar(String description, String date, String time) {
        Calendar beginTime = Calendar.getInstance();
        // Simplified parsing logic
        if (date.equalsIgnoreCase("tomorrow")) {
            beginTime.add(Calendar.DAY_OF_YEAR, 1);
        }
        
        Intent intent = new Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.Events.TITLE, "WhatsApp Task")
                .putExtra(CalendarContract.Events.DESCRIPTION, description)
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTime.getTimeInMillis())
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        
        Utils.getApplication().startActivity(intent);
    }

    @NonNull
    @Override
    public String getPluginName() {
        return "Voice to Task";
    }
}
