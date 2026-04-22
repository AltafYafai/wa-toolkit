package com.wa.toolkit.xposed.features.media;

import android.content.Context;
import android.graphics.Color;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.wa.toolkit.xposed.core.Feature;
import com.wa.toolkit.xposed.core.components.FMessageWpp;
import com.wa.toolkit.xposed.features.listeners.ConversationItemListener;
import com.wa.toolkit.xposed.utils.Utils;

import java.util.concurrent.ConcurrentHashMap;

import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;

public class VoiceTranscription extends Feature {

    private final ConcurrentHashMap<String, String> transcriptCache = new ConcurrentHashMap<>();

    public VoiceTranscription(ClassLoader loader, XSharedPreferences preferences) {
        super(loader, preferences);
    }

    @Override
    public void doHook() throws Throwable {
        if (!prefs.getBoolean("voice_transcription", false)) return;

        ConversationItemListener.conversationListeners.add(new ConversationItemListener.OnConversationItemListener() {
            @Override
            public void onItemBind(FMessageWpp fMessage, ViewGroup view, int position, View convertView) {
                // Media Type 2 = Voice Note, 82 = View Once Voice Note
                int type = fMessage.getMediaType();
                if (type == 2 || type == 82) {
                    showTranscriptionUI(fMessage, view);
                }
            }
        });
    }

    private void showTranscriptionUI(FMessageWpp fMessage, ViewGroup rowView) {
        String msgId = fMessage.getKey().messageID;
        if (transcriptCache.containsKey(msgId)) {
            appendTranscript(rowView, transcriptCache.get(msgId));
            return;
        }

        // Add a "Transcribe" label/button for now as real-time Opus transcription requires decoding
        rowView.post(() -> {
            try {
                int bubbleId = Utils.getID("bubble", "id");
                ViewGroup bubble = rowView.findViewById(bubbleId);
                if (bubble == null) return;
                if (bubble.findViewWithTag("transcription_btn") != null) return;

                Context context = rowView.getContext();
                TextView btn = new TextView(context);
                btn.setTag("transcription_btn");
                btn.setText("[Transcribe]");
                btn.setTextSize(12);
                btn.setTextColor(Color.parseColor("#34B7F1")); // WhatsApp blue
                btn.setPadding(Utils.dipToPixels(8), Utils.dipToPixels(4), Utils.dipToPixels(8), Utils.dipToPixels(4));

                btn.setOnClickListener(v -> {
                    btn.setText("[Transcribing...]");
                    // In a real scenario, we'd feed the Opus buffer to a JNI Whisper model.
                    // For this prototype, we show the placeholder.
                    v.postDelayed(() -> {
                        String dummy = "Transcription feature requires JNI Opus decoder. File path: " + fMessage.getMediaFile().getName();
                        transcriptCache.put(msgId, dummy);
                        appendTranscript(rowView, dummy);
                        bubble.removeView(btn);
                    }, 1000);
                });

                bubble.addView(btn);
            } catch (Exception ignored) {}
        });
    }

    private void appendTranscript(ViewGroup rowView, String text) {
        rowView.post(() -> {
            try {
                int bubbleId = Utils.getID("bubble", "id");
                ViewGroup bubble = rowView.findViewById(bubbleId);
                if (bubble == null) return;
                if (bubble.findViewWithTag("transcript_text") != null) return;

                Context context = rowView.getContext();
                TextView tv = new TextView(context);
                tv.setTag("transcript_text");
                tv.setText("📝 " + text);
                tv.setTextSize(11);
                tv.setTextColor(Color.DKGRAY);
                tv.setPadding(Utils.dipToPixels(8), 0, Utils.dipToPixels(8), Utils.dipToPixels(8));

                bubble.addView(tv);
            } catch (Exception ignored) {}
        });
    }

    @NonNull
    @Override
    public String getPluginName() {
        return "Voice Transcription";
    }
}
