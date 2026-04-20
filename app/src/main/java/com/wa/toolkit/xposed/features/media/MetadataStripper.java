package com.wa.toolkit.xposed.features.media;

import android.media.ExifInterface;

import androidx.annotation.NonNull;

import com.wa.toolkit.xposed.core.Feature;

import java.io.IOException;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class MetadataStripper extends Feature {

    public MetadataStripper(ClassLoader loader, XSharedPreferences preferences) {
        super(loader, preferences);
    }

    @Override
    public void doHook() throws Throwable {
        if (!prefs.getBoolean("metadata_stripper", false)) return;

        XposedBridge.hookAllConstructors(ExifInterface.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                stripMetadata((ExifInterface) param.thisObject);
            }
        });

        XposedHelpers.findAndHookMethod(ExifInterface.class, "saveAttributes", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                stripMetadata((ExifInterface) param.thisObject);
            }
        });
    }

    private void stripMetadata(ExifInterface exif) {
        try {
            // List of sensitive tags to strip
            String[] tags = {
                    ExifInterface.TAG_GPS_LATITUDE,
                    ExifInterface.TAG_GPS_LONGITUDE,
                    ExifInterface.TAG_GPS_LATITUDE_REF,
                    ExifInterface.TAG_GPS_LONGITUDE_REF,
                    ExifInterface.TAG_GPS_ALTITUDE,
                    ExifInterface.TAG_GPS_ALTITUDE_REF,
                    ExifInterface.TAG_GPS_TIMESTAMP,
                    ExifInterface.TAG_GPS_DATESTAMP,
                    ExifInterface.TAG_GPS_PROCESSING_METHOD,
                    ExifInterface.TAG_MAKE,
                    ExifInterface.TAG_MODEL,
                    ExifInterface.TAG_DATETIME,
                    ExifInterface.TAG_DATETIME_DIGITIZED,
                    ExifInterface.TAG_DATETIME_ORIGINAL,
                    ExifInterface.TAG_USER_COMMENT,
                    ExifInterface.TAG_SOFTWARE
            };

            for (String tag : tags) {
                exif.setAttribute(tag, null);
            }
        } catch (Exception e) {
            logDebug("Error stripping metadata", e);
        }
    }

    @NonNull
    @Override
    public String getPluginName() {
        return "Metadata Stripper";
    }
}
