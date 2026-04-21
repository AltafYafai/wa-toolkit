package com.wa.toolkit.xposed.features.others;

import android.app.Service;
import android.content.Context;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.wa.toolkit.xposed.core.Feature;
import com.wa.toolkit.xposed.core.WppCore;
import com.wa.toolkit.xposed.utils.ResId;
import com.wa.toolkit.xposed.utils.Utils;

import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;

public class FloatingChatHeads extends Feature {

    private WindowManager windowManager;
    private View chatHeadView;
    private WindowManager.LayoutParams params;

    public FloatingChatHeads(ClassLoader loader, XSharedPreferences preferences) {
        super(loader, preferences);
    }

    @Override
    public void doHook() throws Throwable {
        if (!prefs.getBoolean("chat_heads", false)) return;
        // Listen for new messages via Tasker feature or similar hook
    }

    public void showChatHead(Context context) {
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        chatHeadView = new ImageView(context);
        ((ImageView) chatHeadView).setImageResource(ResId.drawable.ic_privacy); // Placeholder icon

        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 0;
        params.y = 100;

        chatHeadView.setOnTouchListener(new View.OnTouchListener() {
            private int lastAction;
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private int initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = (int) event.getRawY();
                        lastAction = event.getAction();
                        return true;
                    case MotionEvent.ACTION_UP:
                        if (lastAction == MotionEvent.ACTION_DOWN) {
                            // Open chat logic
                        }
                        lastAction = event.getAction();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        windowManager.updateViewLayout(chatHeadView, params);
                        lastAction = event.getAction();
                        return true;
                }
                return false;
            }
        });

        windowManager.addView(chatHeadView, params);
    }

    @NonNull
    @Override
    public String getPluginName() {
        return "Floating Chat Heads";
    }
}
