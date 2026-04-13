package com.wa.toolkit.ui.fragments;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import com.wa.toolkit.R;
import com.wa.toolkit.ui.fragments.base.BasePreferenceFragment;

public class CallsFragment extends BasePreferenceFragment {
    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);
        setPreferencesFromResource(R.xml.fragment_calls, rootKey);

        Preference viewRecordings = findPreference("view_recordings");
        if (viewRecordings != null) {
            viewRecordings.setOnPreferenceClickListener(preference -> {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new RecordingsFragment())
                        .addToBackStack(null)
                        .commit();
                return true;
            });
        }
    }
}
