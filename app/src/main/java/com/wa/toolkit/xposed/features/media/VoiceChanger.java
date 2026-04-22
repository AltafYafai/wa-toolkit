package com.wa.toolkit.xposed.features.media;

import android.app.Activity;
import android.content.Context;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.wa.toolkit.xposed.core.Feature;
import com.wa.toolkit.xposed.core.WppCore;
import com.wa.toolkit.xposed.core.components.AlertDialogWpp;
import com.wa.toolkit.xposed.core.devkit.Unobfuscator;
import com.wa.toolkit.xposed.utils.Utils;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;

public class VoiceChanger extends Feature {

    public VoiceChanger(ClassLoader loader, XSharedPreferences preferences) {
        super(loader, preferences);
    }

    @Override
    public void doHook() throws Throwable {
        if (!prefs.getBoolean("voice_changer", false)) return;

        Class<?> conversationFragmentClass = Unobfuscator.loadConversationFragmentClass(classLoader);
        XposedBridge.hookAllMethods(conversationFragmentClass, "onViewCreated", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                android.view.View view = (android.view.View) param.args[0];
                injectVoiceChangerButton(view);
            }
        });
    }

    private void injectVoiceChangerButton(android.view.View root) {
        try {
            int voiceBtnId = Utils.getID("voice_note_btn", "id");
            android.view.View voiceBtn = root.findViewById(voiceBtnId);
            if (voiceBtn == null) return;

            ViewGroup parent = (ViewGroup) voiceBtn.getParent();
            if (parent == null) return;

            Context context = root.getContext();
            ImageButton changerBtn = new ImageButton(context);
            changerBtn.setImageResource(Utils.getID("ic_recording", "drawable"));
            changerBtn.setBackground(null);
            changerBtn.setPadding(Utils.dipToPixels(4), 0, Utils.dipToPixels(4), 0);

            parent.addView(changerBtn, parent.indexOfChild(voiceBtn));

            changerBtn.setOnClickListener(v -> showChangerDialog(context));

        } catch (Exception e) {
            logDebug("Error injecting Voice Changer button", e);
        }
    }

    private void showChangerDialog(Context context) {
        Activity activity = WppCore.getCurrentActivity();
        if (activity == null) return;

        SeekBar pitchBar = new SeekBar(context);
        pitchBar.setMax(200);
        pitchBar.setProgress(100);

        new AlertDialogWpp(activity)
                .setTitle("Voice Changer (Pitch)")
                .setView(pitchBar)
                .setPositiveButton("Set", (dialog, which) -> {
                    int pitch = pitchBar.getProgress();
                    Utils.showToast("Pitch set to " + pitch + "%. (Prototype: DSP engine requires JNI)", Toast.LENGTH_SHORT);
                })
                .setNegativeButton("Reset", null)
                .show();
    }

    @NonNull
    @Override
    public String getPluginName() {
        return "Voice Changer";
    }
}
