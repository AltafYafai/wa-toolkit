package com.wa.toolkit.xposed.utils;

import androidx.annotation.NonNull;

import com.wa.toolkit.BuildConfig;

import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;

public class PrefUtils {

    private static XSharedPreferences pref;

    @Deprecated
    public static XC_InitPackageResources.InitPackageResourcesParam ResParam = null;

    @NonNull
    public static XSharedPreferences getPref() {
        if (pref == null) {
            String prefName = BuildConfig.APPLICATION_ID + "_preferences";
            pref = new XSharedPreferences(BuildConfig.APPLICATION_ID, prefName);
            pref.makeWorldReadable();
            pref.reload();
        }
        return pref;
    }
}
