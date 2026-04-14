package com.wa.toolkit.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.wa.toolkit.R;
import com.wa.toolkit.activities.base.BaseActivity;
import com.wa.toolkit.fragments.DashboardFragment;
import com.wa.toolkit.ui.fragments.CustomizationFragment;
import com.wa.toolkit.ui.fragments.GeneralFragment;
import com.wa.toolkit.ui.fragments.MediaFragment;
import com.wa.toolkit.ui.fragments.PrivacyFragment;
import com.wa.toolkit.ui.fragments.RecordingsFragment;
import com.wa.toolkit.ui.fragments.HomeFragment;
import com.wa.toolkit.ui.fragments.StatusFragment;
import com.wa.toolkit.ui.fragments.CallsFragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import androidx.core.content.ContextCompat;
import com.wa.toolkit.BuildConfig;
import com.wa.toolkit.utils.FilePicker;
import com.wa.toolkit.xposed.core.FeatureLoader;

public class MainActivity extends BaseActivity {

    private static final String TAG_DASHBOARD = "dashboard";
    private String wppVersion = "Not Detected";
    private boolean isWppActive = false;
    private BroadcastReceiver statusReceiver;

    public interface OnStatusUpdateListener {
        void onStatusUpdate(String version, boolean active);
    }

    private OnStatusUpdateListener statusListener;

    public void setOnStatusUpdateListener(OnStatusUpdateListener listener) {
        this.statusListener = listener;
        if (listener != null) {
            listener.onStatusUpdate(wppVersion, isWppActive);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FilePicker.registerFilePicker(this);
        setupStatusReceiver();

        if (savedInstanceState == null) {
            showDashboard();
        }

        handleIncomingIntent(getIntent());
        checkWpp();
    }

    private void setupStatusReceiver() {
        var intentFilter = new IntentFilter(BuildConfig.APPLICATION_ID + ".RECEIVER_WPP");
        statusReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                wppVersion = intent.getStringExtra("VERSION");
                isWppActive = true;
                if (statusListener != null) {
                    statusListener.onStatusUpdate(wppVersion, isWppActive);
                }
            }
        };
        ContextCompat.registerReceiver(this, statusReceiver, intentFilter, ContextCompat.RECEIVER_EXPORTED);
    }

    private void checkWpp() {
        Intent checkWpp = new Intent(BuildConfig.APPLICATION_ID + ".CHECK_WPP");
        sendBroadcast(checkWpp);
    }

    @Override
    protected void onDestroy() {
        if (statusReceiver != null) {
            unregisterReceiver(statusReceiver);
        }
        super.onDestroy();
    }

    public static boolean isXposedEnabled() {
        return false;
    }

    public String getWhatsAppVersion() {
        try {
            return getPackageManager().getPackageInfo(FeatureLoader.PACKAGE_WPP, 0).versionName;
        } catch (Exception e) {
            try {
                return getPackageManager().getPackageInfo(FeatureLoader.PACKAGE_BUSINESS, 0).versionName;
            } catch (Exception e2) {
                return "Not Installed";
            }
        }
    }

    public boolean isWhatsAppRunning() {
        // This is usually handled via the broadcast receiver in HomeFragment
        // For the dashboard, we can just check if the package is installed
        try {
            getPackageManager().getPackageInfo(FeatureLoader.PACKAGE_WPP, 0);
            return true;
        } catch (Exception e) {
            try {
                getPackageManager().getPackageInfo(FeatureLoader.PACKAGE_BUSINESS, 0);
                return true;
            } catch (Exception e2) {
                return false;
            }
        }
    }

    private void showDashboard() {
        DashboardFragment fragment = new DashboardFragment();
        fragment.setOnDashboardItemClickListener(item -> {
            navigateToCategory(item.getId());
        });

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment, TAG_DASHBOARD)
                .commit();
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setTitle(R.string.app_name);
        }
    }

    private void navigateToCategory(int id, Intent intent) {
        Fragment fragment;
        String title;

        switch (id) {
            case 1 -> { fragment = new PrivacyFragment(); title = getString(R.string.privacy); }
            case 2 -> { fragment = new GeneralFragment.ConversationGeneralPreference(); title = "Chat"; }
            case 3 -> { fragment = new MediaFragment(); title = getString(R.string.media); }
            case 4 -> { fragment = new CustomizationFragment(); title = getString(R.string.perso); }
            case 5 -> { fragment = new GeneralFragment.HomeGeneralPreference(); title = "Tools"; }
            case 8 -> { fragment = new GeneralFragment.HomeScreenGeneralPreference(); title = getString(R.string.home_screen); }
            case 6 -> { fragment = new StatusFragment(); title = getString(R.string.status); }
            case 7 -> { fragment = new CallsFragment(); title = getString(R.string.calls); }
            default -> { return; }
        }

        // Pass search arguments if any
        if (intent != null && intent.hasExtra("scroll_to_preference")) {
            Bundle args = new Bundle();
            args.putString("scroll_to_preference", intent.getStringExtra("scroll_to_preference"));
            if (intent.hasExtra("parent_preference")) {
                args.putString("parent_preference", intent.getStringExtra("parent_preference"));
            }
            fragment.setArguments(args);
        }

        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(
                        R.anim.slide_in_right, R.anim.slide_out_left,
                        R.anim.slide_in_right, R.anim.slide_out_left
                )
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(title);
        }
    }

    private void handleIncomingIntent(Intent intent) {
        if (intent != null && intent.hasExtra("navigate_to_fragment")) {
            int position = intent.getIntExtra("navigate_to_fragment", -1);
            if (position != -1) {
                navigateToCategory(position, intent);
            }
        }
    }

    private void navigateToCategory(int id) {
        navigateToCategory(id, null);
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.header_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getSupportFragmentManager().popBackStack();
            if (getSupportFragmentManager().getBackStackEntryCount() <= 1) {
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                    getSupportActionBar().setTitle(R.string.app_name);
                }
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIncomingIntent(intent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                getSupportActionBar().setTitle(R.string.app_name);
            }
        }
    }
}
