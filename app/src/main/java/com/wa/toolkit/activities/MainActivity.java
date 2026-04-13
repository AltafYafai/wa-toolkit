package com.wa.toolkit.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.wa.toolkit.R;
import com.wa.toolkit.databinding.ActivityMainBinding;
import com.wa.toolkit.fragments.DashboardFragment;
import com.wa.toolkit.fragments.preferences.CustomizationFragment;
import com.wa.toolkit.fragments.preferences.GeneralFragment;
import com.wa.toolkit.fragments.preferences.MediaFragment;
import com.wa.toolkit.fragments.preferences.PrivacyFragment;
import com.wa.toolkit.fragments.preferences.RecordingsFragment;
import com.wa.toolkit.fragments.HomeFragment;
import com.wa.toolkit.utils.FeatureCatalog;
import com.wa.toolkit.model.SearchableFeature;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private static final String TAG_DASHBOARD = "dashboard";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        if (savedInstanceState == null) {
            showDashboard();
        }

        handleIncomingIntent(getIntent());
    }

    private void showDashboard() {
        DashboardFragment fragment = new DashboardFragment();
        fragment.setOnDashboardItemClickListener(item -> {
            navigateToCategory(item.getId());
        });

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment, TAG_DASHBOARD)
                .commit();
        
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setTitle(R.string.app_name);
    }

    private void navigateToCategory(int id) {
        Fragment fragment;
        String title;

        switch (id) {
            case 0 -> { fragment = new GeneralFragment(); title = getString(R.string.general); }
            case 1 -> { fragment = new PrivacyFragment(); title = getString(R.string.privacy); }
            case 2 -> { fragment = new HomeFragment(); title = getString(R.string.title_home); }
            case 3 -> { fragment = new MediaFragment(); title = getString(R.string.media); }
            case 4 -> { fragment = new CustomizationFragment(); title = getString(R.string.perso); }
            case 5 -> { fragment = new RecordingsFragment(); title = getString(R.string.recordings_manager); }
            default -> { return; }
        }

        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(
                        R.anim.slide_in_right, R.anim.slide_out_left,
                        R.anim.slide_in_right, R.anim.slide_out_left
                )
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(title);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getSupportFragmentManager().popBackStack();
            if (getSupportFragmentManager().getBackStackEntryCount() <= 1) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                getSupportActionBar().setTitle(R.string.app_name);
            }
            return true;
        } else if (item.getItemId() == R.id.menu_search) {
            startActivity(new Intent(this, SearchActivity.class));
            return true;
        } else if (item.getItemId() == R.id.menu_about) {
            startActivity(new Intent(this, AboutActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIncomingIntent(intent);
    }

    private void handleIncomingIntent(Intent intent) {
        if (intent != null && intent.hasExtra("navigate_to_fragment")) {
            int position = intent.getIntExtra("navigate_to_fragment", -1);
            if (position != -1) {
                navigateToCategory(position);
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setTitle(R.string.app_name);
        }
    }
}
