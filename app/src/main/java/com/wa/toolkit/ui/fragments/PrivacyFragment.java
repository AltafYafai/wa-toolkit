package com.wa.toolkit.ui.fragments;

import static com.wa.toolkit.preference.ContactPickerPreference.REQUEST_CONTACT_PICKER;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.wa.toolkit.R;
import com.wa.toolkit.preference.ContactPickerPreference;
import com.wa.toolkit.preference.FileSelectPreference;
import com.wa.toolkit.ui.fragments.base.BasePreferenceFragment;
import com.wa.toolkit.xposed.features.general.LiteMode;

public class PrivacyFragment extends BasePreferenceFragment {

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);
        setPreferencesFromResource(R.xml.fragment_privacy, rootKey);

        findPreference("open_deleted_messages").setOnPreferenceClickListener(preference -> {
            startActivity(new Intent(requireContext(), com.wa.toolkit.activities.DeletedMessagesActivity.class));
            return true;
        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        System.out.println("onActivityResult: " + requestCode + " " + resultCode + " " + data);
        if (requestCode == REQUEST_CONTACT_PICKER && resultCode == Activity.RESULT_OK) {
            ContactPickerPreference contactPickerPref = findPreference(data.getStringExtra("key"));
            if (contactPickerPref != null) {
                contactPickerPref.handleActivityResult(requestCode, resultCode, data);
            }
        } else if (requestCode == LiteMode.REQUEST_FOLDER && resultCode == Activity.RESULT_OK) {
            FileSelectPreference fileSelectPreference = findPreference(data.getStringExtra("key"));
            if (fileSelectPreference != null) {
                fileSelectPreference.handleActivityResult(requestCode, resultCode, data);
            }
        }
    }

}
