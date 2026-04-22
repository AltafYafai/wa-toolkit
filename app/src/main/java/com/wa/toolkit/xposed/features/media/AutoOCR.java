package com.wa.toolkit.xposed.features.media;

import android.content.Context;
import android.graphics.Color;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.wa.toolkit.xposed.core.Feature;
import com.wa.toolkit.xposed.core.components.FMessageWpp;
import com.wa.toolkit.xposed.features.listeners.ConversationItemListener;
import com.wa.toolkit.xposed.utils.Utils;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;

import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;

public class AutoOCR extends Feature {

    private final ConcurrentHashMap<String, String> ocrCache = new ConcurrentHashMap<>();

    public AutoOCR(ClassLoader loader, XSharedPreferences preferences) {
        super(loader, preferences);
    }

    @Override
    public void doHook() throws Throwable {
        if (!prefs.getBoolean("auto_ocr", false)) return;

        ConversationItemListener.conversationListeners.add(new ConversationItemListener.OnConversationItemListener() {
            @Override
            public void onItemBind(FMessageWpp fMessage, ViewGroup view, int position, View convertView) {
                // Media Type 1 = Image, 42 = View Once Image
                int type = fMessage.getMediaType();
                if (type == 1 || type == 42) {
                    processImageOCR(fMessage, view);
                }
            }
        });
    }

    private void processImageOCR(FMessageWpp fMessage, ViewGroup rowView) {
        String msgId = fMessage.getKey().messageID;
        if (ocrCache.containsKey(msgId)) {
            showOcrResult(rowView, ocrCache.get(msgId));
            return;
        }

        File imageFile = fMessage.getMediaFile();
        if (imageFile == null || !imageFile.exists()) return;

        try {
            InputImage image = InputImage.fromFilePath(rowView.getContext(), android.net.Uri.fromFile(imageFile));
            var recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

            recognizer.process(image)
                    .addOnSuccessListener(visionText -> {
                        String text = visionText.getText();
                        if (text != null && !text.isEmpty()) {
                            ocrCache.put(msgId, text);
                            showOcrResult(rowView, text);
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Silent fail
                    });
        } catch (Exception e) {
            // Error loading image
        }
    }

    private void showOcrResult(ViewGroup rowView, String text) {
        rowView.post(() -> {
            try {
                int bubbleId = Utils.getID("bubble", "id");
                ViewGroup bubble = rowView.findViewById(bubbleId);
                if (bubble == null) return;

                // Check if already added
                if (bubble.findViewWithTag("ocr_text") != null) return;

                Context context = rowView.getContext();
                TextView ocrView = new TextView(context);
                ocrView.setTag("ocr_text");
                ocrView.setText("[OCR]: " + text);
                ocrView.setTextSize(12);
                ocrView.setTextColor(Color.GRAY);
                ocrView.setPadding(Utils.dipToPixels(8), Utils.dipToPixels(4), Utils.dipToPixels(8), Utils.dipToPixels(4));

                bubble.addView(ocrView);
            } catch (Exception ignored) {}
        });
    }

    @NonNull
    @Override
    public String getPluginName() {
        return "Auto OCR";
    }
}
