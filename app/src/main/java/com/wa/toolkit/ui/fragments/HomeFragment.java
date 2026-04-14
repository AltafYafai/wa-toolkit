package com.wa.toolkit.ui.fragments;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.PreferenceManager;

import com.wa.toolkit.App;
import com.wa.toolkit.BuildConfig;
import com.wa.toolkit.R;
import com.wa.toolkit.activities.MainActivity;
import com.wa.toolkit.databinding.FragmentHomeBinding;
import com.wa.toolkit.ui.fragments.base.BaseFragment;
import com.wa.toolkit.utils.ConfigUtil;
import com.wa.toolkit.utils.FilePicker;
import com.wa.toolkit.xposed.core.FeatureLoader;
import com.wa.toolkit.xposed.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;

import rikka.core.util.IOUtils;

public class HomeFragment extends BaseFragment {

    private FragmentHomeBinding binding;
    private BroadcastReceiver wppReceiver;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        var intentFilter = new IntentFilter(BuildConfig.APPLICATION_ID + ".RECEIVER_WPP");
        wppReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                if (binding == null) return;
                try {
                    if (FeatureLoader.PACKAGE_WPP.equals(intent.getStringExtra("PKG")))
                        receiverBroadcastWpp(context, intent);
                    else
                        receiverBroadcastBusiness(context, intent);
                } catch (Exception ignored) {
                }
            }
        };
        ContextCompat.registerReceiver(requireContext(), wppReceiver, intentFilter, ContextCompat.RECEIVER_EXPORTED);
    }

    @Override
    public void onDestroy() {
        if (wppReceiver != null) {
            requireContext().unregisterReceiver(wppReceiver);
        }
        super.onDestroy();
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);

        checkStateWpp(requireActivity());

        binding.rebootBtn.setOnClickListener(view -> {
            animateClick(view);
            App.getInstance().restartApp(FeatureLoader.PACKAGE_WPP);
            disableWpp(requireActivity());
        });

        binding.rebootBtn2.setOnClickListener(view -> {
            animateClick(view);
            App.getInstance().restartApp(FeatureLoader.PACKAGE_BUSINESS);
            disableBusiness(requireActivity());
        });

        binding.exportBtn.setOnClickListener(view -> {
            animateClick(view);
            ConfigUtil.INSTANCE.exportConfigs(requireContext());
        });

        binding.importBtn.setOnClickListener(view -> {
            animateClick(view);
            ConfigUtil.INSTANCE.importConfigs(requireContext());
        });

        binding.resetBtn.setOnClickListener(view -> {
            animateClick(view);
            ConfigUtil.INSTANCE.resetConfigs(requireContext());
        });

        startCardAnimations();

        return binding.getRoot();
    }

    private void startCardAnimations() {
        var slideUp = AnimationUtils.loadAnimation(getContext(), R.anim.slide_up);
        var fadeIn = AnimationUtils.loadAnimation(getContext(), R.anim.fade_in);

        binding.status.startAnimation(slideUp);

        binding.status2.postDelayed(() -> {
            var anim = AnimationUtils.loadAnimation(getContext(), R.anim.slide_up);
            binding.status2.startAnimation(anim);
        }, 100);

        binding.status3.postDelayed(() -> {
            var anim = AnimationUtils.loadAnimation(getContext(), R.anim.slide_up);
            binding.status3.startAnimation(anim);
        }, 200);

        binding.infoCard.postDelayed(() -> {
            binding.infoCard.startAnimation(fadeIn);
        }, 300);
    }

    private void animateClick(View view) {
        var scaleIn = AnimationUtils.loadAnimation(getContext(), R.anim.scale_in);
        view.startAnimation(scaleIn);
    }

    @SuppressLint("StringFormatInvalid")
    private void receiverBroadcastBusiness(Context context, Intent intent) {
        binding.statusTitle3.setText(R.string.business_in_background);
        var version = intent.getStringExtra("VERSION");
        var supported_list = Arrays.asList(context.getResources().getStringArray(R.array.supported_versions_business));
        
        android.util.TypedValue typedValue = new android.util.TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.colorPrimary, typedValue, true);
        int colorPrimary = typedValue.data;
        int colorError = ContextCompat.getColor(context, R.color.gradient_start_error);

        if (version != null && supported_list.stream().anyMatch(s -> version.startsWith(s.replace(".xx", "")))) {
            binding.statusSummary3.setText(getString(R.string.version_s, version));
            binding.statusIcon3.setImageTintList(android.content.res.ColorStateList.valueOf(colorPrimary));
        } else {
            binding.statusSummary3.setText(getString(R.string.version_s_not_listed, version));
            binding.statusIcon3.setImageTintList(android.content.res.ColorStateList.valueOf(colorError));
        }
        binding.rebootBtn2.setVisibility(View.VISIBLE);
        binding.statusSummary3.setVisibility(View.VISIBLE);
        binding.statusIcon3.setImageResource(R.drawable.ic_round_check_circle_24);
    }

    @SuppressLint("StringFormatInvalid")
    private void receiverBroadcastWpp(Context context, Intent intent) {
        binding.statusTitle2.setText(R.string.whatsapp_in_background);
        var version = intent.getStringExtra("VERSION");
        var supported_list = Arrays.asList(context.getResources().getStringArray(R.array.supported_versions_wpp));

        android.util.TypedValue typedValue = new android.util.TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.colorPrimary, typedValue, true);
        int colorPrimary = typedValue.data;
        int colorError = ContextCompat.getColor(context, R.color.gradient_start_error);

        if (version != null && supported_list.stream().anyMatch(s -> version.startsWith(s.replace(".xx", "")))) {
            binding.statusSummary1.setText(getString(R.string.version_s, version));
            binding.statusIcon2.setImageTintList(android.content.res.ColorStateList.valueOf(colorPrimary));
        } else {
            binding.statusSummary1.setText(getString(R.string.version_s_not_listed, version));
            binding.statusIcon2.setImageTintList(android.content.res.ColorStateList.valueOf(colorError));
        }
        binding.rebootBtn.setVisibility(View.VISIBLE);
        binding.statusSummary1.setVisibility(View.VISIBLE);
        binding.statusIcon2.setImageResource(R.drawable.ic_round_check_circle_24);
    }

    @SuppressLint("StringFormatInvalid")
    private void checkStateWpp(FragmentActivity activity) {

        android.util.TypedValue typedValue = new android.util.TypedValue();
        activity.getTheme().resolveAttribute(android.R.attr.colorPrimary, typedValue, true);
        int colorPrimary = typedValue.data;
        int colorError = ContextCompat.getColor(activity, R.color.gradient_start_error);

        if (MainActivity.isXposedEnabled()) {
            binding.statusIcon.setImageResource(R.drawable.ic_round_check_circle_24);
            binding.statusIcon.setImageTintList(android.content.res.ColorStateList.valueOf(colorPrimary));
            binding.statusTitle.setText(R.string.module_enabled);
            binding.statusSummary.setText(String.format(getString(R.string.version_s), BuildConfig.VERSION_NAME));
        } else {
            binding.statusIcon.setImageResource(R.drawable.ic_round_error_outline_24);
            binding.statusIcon.setImageTintList(android.content.res.ColorStateList.valueOf(colorError));
            binding.statusTitle.setText(R.string.module_disabled);
            binding.statusSummary.setVisibility(View.GONE);
        }
        if (isInstalled(FeatureLoader.PACKAGE_WPP) && App.isOriginalPackage()) {
            disableWpp(activity);
        } else {
            binding.status2.setVisibility(View.GONE);
        }

        if (isInstalled(FeatureLoader.PACKAGE_BUSINESS)) {
            disableBusiness(activity);
        } else {
            binding.status3.setVisibility(View.GONE);
        }
        checkWpp(activity);
        binding.deviceName.setText(Build.MANUFACTURER);
        binding.sdk.setText(String.valueOf(Build.VERSION.SDK_INT));
        binding.modelName.setText(Build.DEVICE);
        if (App.isOriginalPackage()) {
            binding.listWpp.setText(Arrays.toString(activity.getResources().getStringArray(R.array.supported_versions_wpp)));
        } else {
            binding.listWppTitle.setVisibility(View.GONE);
            binding.listWpp.setVisibility(View.GONE);
        }
        binding.listBusiness.setText(Arrays.toString(activity.getResources().getStringArray(R.array.supported_versions_business)));
    }

    private boolean isInstalled(String packageWpp) {
        try {
            App.getInstance().getPackageManager().getPackageInfo(packageWpp, 0);
            return true;
        } catch (Exception ignored) {
        }
        return false;
    }

    private void disableBusiness(FragmentActivity activity) {
        int colorError = ContextCompat.getColor(activity, R.color.gradient_start_error);
        binding.statusIcon3.setImageResource(R.drawable.ic_round_error_outline_24);
        binding.statusIcon3.setImageTintList(android.content.res.ColorStateList.valueOf(colorError));
        binding.statusTitle3.setText(R.string.business_is_not_running_or_has_not_been_activated_in_lsposed);
        binding.statusSummary3.setVisibility(View.GONE);
        binding.rebootBtn2.setVisibility(View.GONE);
    }

    private void disableWpp(FragmentActivity activity) {
        int colorError = ContextCompat.getColor(activity, R.color.gradient_start_error);
        binding.statusIcon2.setImageResource(R.drawable.ic_round_error_outline_24);
        binding.statusIcon2.setImageTintList(android.content.res.ColorStateList.valueOf(colorError));
        binding.statusTitle2.setText(R.string.whatsapp_is_not_running_or_has_not_been_activated_in_lsposed);
        binding.statusSummary1.setVisibility(View.GONE);
        binding.rebootBtn.setVisibility(View.GONE);
    }

    private static void checkWpp(FragmentActivity activity) {
        Intent checkWpp = new Intent(BuildConfig.APPLICATION_ID + ".CHECK_WPP");
        activity.sendBroadcast(checkWpp);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}