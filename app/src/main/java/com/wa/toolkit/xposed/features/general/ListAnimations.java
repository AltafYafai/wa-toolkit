package com.wa.toolkit.xposed.features.general;

import android.annotation.SuppressLint;
import android.view.View;

import androidx.annotation.NonNull;

import com.wa.toolkit.xposed.core.Feature;
import com.wa.toolkit.xposed.core.devkit.Unobfuscator;
import com.wa.toolkit.xposed.utils.AnimationUtil;
import com.wa.toolkit.xposed.utils.ReflectionUtils;
import com.wa.toolkit.xposed.utils.Utils;

import java.util.Objects;
import java.util.Properties;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;

public class ListAnimations extends Feature {

    private final Properties properties;

    public ListAnimations(ClassLoader loader, XSharedPreferences preferences) {
        super(loader, preferences);
        this.properties = Utils.getProperties(prefs, "custom_css", null); // Assuming custom_css is where home_list_animation is stored
    }

    @Override
    public void doHook() throws Exception {
        var animation = prefs.getString("animation_list", "default");

        var onChangeStatus = Unobfuscator.loadOnChangeStatus(classLoader);
        var field1 = Unobfuscator.loadViewHolderField1(classLoader);
        var absViewHolderClass = Unobfuscator.loadAbsViewHolder(classLoader);

        XposedBridge.hookMethod(onChangeStatus, new XC_MethodHook() {
            @Override
            @SuppressLint("ResourceType")
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                var viewHolder = field1.get(param.thisObject);
                var viewField = ReflectionUtils.findFieldUsingFilter(absViewHolderClass, field -> field.getType() == View.class);
                var view = (View) viewField.get(viewHolder);
                if (!Objects.equals(animation, "default")) {
                    view.startAnimation(AnimationUtil.getAnimation(animation));
                } else if (properties.containsKey("home_list_animation")) {
                    var animation = AnimationUtil.getAnimation(properties.getProperty("home_list_animation"));
                    if (animation != null) {
                        view.startAnimation(animation);
                    }
                }
            }
        });
    }

    @NonNull
    @Override
    public String getPluginName() {
        return "ListAnimations";
    }
}
