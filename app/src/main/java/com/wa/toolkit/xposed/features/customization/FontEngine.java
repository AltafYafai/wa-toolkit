package com.wa.toolkit.xposed.features.customization;

import android.graphics.Typeface;
import android.os.Environment;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.wa.toolkit.xposed.core.Feature;

import java.io.File;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class FontEngine extends Feature {

    private Typeface customTypeface;
    private float fontScale = 1.0f;

    public FontEngine(ClassLoader loader, XSharedPreferences preferences) {
        super(loader, preferences);
    }

    @Override
    public void doHook() throws Throwable {
        String fontPath = prefs.getString("custom_font_path", "");
        fontScale = prefs.getFloat("custom_font_scale", 1.0f);
        
        if (fontPath.isEmpty()) return;

        File fontFile = new File(fontPath);
        if (!fontFile.exists()) return;

        try {
            customTypeface = Typeface.createFromFile(fontFile);
        } catch (Exception e) {
            logDebug("Failed to load custom font", e);
            return;
        }

        XposedHelpers.findAndHookMethod(TextView.class, "setTypeface", Typeface.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (customTypeface != null) {
                    param.args[0] = customTypeface;
                }
            }
            
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (fontScale != 1.0f) {
                    TextView textView = (TextView) param.thisObject;
                    Object tag = textView.getTag(com.wa.toolkit.R.id.tag_font_scale);
                    if (tag == null) {
                        float originalSize = textView.getTextSize();
                        textView.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, originalSize * fontScale);
                        textView.setTag(com.wa.toolkit.R.id.tag_font_scale, fontScale);
                    }
                }
            }
        });

        XposedHelpers.findAndHookMethod(TextView.class, "setTypeface", Typeface.class, int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (customTypeface != null) {
                    param.args[0] = customTypeface;
                }
            }
            
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (fontScale != 1.0f) {
                    TextView textView = (TextView) param.thisObject;
                    Object tag = textView.getTag(com.wa.toolkit.R.id.tag_font_scale);
                    if (tag == null) {
                        float originalSize = textView.getTextSize();
                        textView.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, originalSize * fontScale);
                        textView.setTag(com.wa.toolkit.R.id.tag_font_scale, fontScale);
                    }
                }
            }
        });
        
        XposedHelpers.findAndHookMethod(Typeface.class, "create", String.class, int.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (customTypeface != null) {
                    param.setResult(customTypeface);
                }
            }
        });
    }

    @NonNull
    @Override
    public String getPluginName() {
        return "Font Engine";
    }
}
