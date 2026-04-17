package com.wa.toolkit.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import com.google.android.material.slider.Slider;
import com.wa.toolkit.R;

public class FloatSeekBarPreference extends Preference {

    private float minValue;
    private float maxValue;
    private float valueSpacing;
    private String format;

    private Slider seekbar;
    private TextView textView;

    private float defaultValue = 0F;
    private float newValue = 0F;

    public FloatSeekBarPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    public FloatSeekBarPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public FloatSeekBarPreference(Context context, AttributeSet attrs) {
        this(context, attrs, androidx.preference.R.attr.seekBarPreferenceStyle);
    }

    public FloatSeekBarPreference(Context context) {
        this(context, null);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray ta, int index) {
        defaultValue = ta.getFloat(index, 0F);
        return defaultValue;
    }

    @Override
    protected void onSetInitialValue(Object defaultValue) {
        newValue = getPersistedFloat((defaultValue instanceof Float) ? (Float) defaultValue : this.defaultValue);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        holder.itemView.setClickable(false);
        seekbar = (Slider) holder.findViewById(R.id.seekbar);
        textView = (TextView) holder.findViewById(R.id.seekbar_value);

        if (seekbar != null) {
            // Safety checks to prevent Slider crashes
            float min = minValue;
            float max = maxValue;
            if (min >= max) {
                max = min + 1.0f;
            }

            seekbar.setValueFrom(min);
            seekbar.setValueTo(max);
            seekbar.setStepSize(valueSpacing > 0 ? valueSpacing : 1.0f);
            
            float valueToSet = Math.max(min, Math.min(max, newValue));
            seekbar.setValue(valueToSet);
            seekbar.setEnabled(isEnabled());

            seekbar.addOnChangeListener((slider, value, fromUser) -> {
                if (fromUser) {
                    textView.setText(String.format(format, value));
                }
            });

            seekbar.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
                @Override
                public void onStartTrackingTouch(@NonNull Slider slider) {
                    // Not used
                }

                @Override
                public void onStopTrackingTouch(@NonNull Slider slider) {
                    persistFloat(slider.getValue());
                }
            });
        }

        textView.setText(String.format(format, newValue));
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        setWidgetLayoutResource(R.layout.pref_float_seekbar);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.FloatSeekBarPreference, defStyleAttr, defStyleRes);
        minValue = ta.getFloat(R.styleable.FloatSeekBarPreference_minValue, 0F);
        maxValue = ta.getFloat(R.styleable.FloatSeekBarPreference_maxValue, 1F);
        valueSpacing = ta.getFloat(R.styleable.FloatSeekBarPreference_valueSpacing, .1F);
        format = ta.getString(R.styleable.FloatSeekBarPreference_format);
        if (format == null) {
            format = "%3.1f";
        }
        ta.recycle();
    }

    public float getValue() {
        return (seekbar != null) ? seekbar.getValue() : 0F;
    }

    public void setValue(float value) {
        newValue = value;
        persistFloat(value);
        notifyChanged();
    }
}