package com.wa.toolkit.ui.fragments;

import android.os.Bundle;
import androidx.annotation.Nullable;
import com.wa.toolkit.R;
import com.wa.toolkit.ui.fragments.base.BasePreferenceFragment;

public class StatusFragment extends BasePreferenceFragment {
    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);
        setPreferencesFromResource(R.xml.fragment_status, rootKey);
    }
}
