package com.wa.toolkit.xposed.features.general;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.wa.toolkit.xposed.core.Feature;
import com.wa.toolkit.xposed.core.WppCore;
import com.wa.toolkit.xposed.core.components.FMessageWpp;
import com.wa.toolkit.xposed.core.devkit.Unobfuscator;
import com.wa.toolkit.xposed.utils.ResId;
import com.wa.toolkit.xposed.utils.Utils;

import org.json.JSONObject;
import org.luckypray.dexkit.query.enums.StringMatchType;

import java.util.concurrent.CompletableFuture;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import okhttp3.OkHttpClient;

public class CallInformation extends Feature {

    public CallInformation(ClassLoader loader, XSharedPreferences preferences) {
        super(loader, preferences);
    }

    @Override
    public void doHook() throws Exception {
        if (!prefs.getBoolean("call_info", false)) return;

        var clsCallEventCallback = Unobfuscator.findFirstClassUsingName(classLoader, StringMatchType.EndsWith, "VoiceServiceEventCallback");
        Class<?> clsWamCall = Unobfuscator.findFirstClassUsingName(classLoader, StringMatchType.EndsWith, "WamCall");

        XposedBridge.hookAllMethods(clsCallEventCallback, "fieldstatsReady", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (clsWamCall.isInstance(param.args[0])) {

                    Object callinfo = XposedHelpers.callMethod(param.thisObject, "getCallInfo");
                    if (callinfo == null) return;
                    var userJid = new FMessageWpp.UserJid(XposedHelpers.callMethod(callinfo, "getPeerJid"));
                    if (userJid.isNull()) return;
                    CompletableFuture.runAsync(() -> {
                        try {
                            showCallInformation(param.args[0], userJid);
                        } catch (Exception e) {
                            logDebug(e);
                        }
                    });
                }
            }
        });
    }

    private void showCallInformation(Object wamCall, FMessageWpp.UserJid userJid) throws Exception {
        if (userJid.isGroup()) return;
        var sb = new StringBuilder();
        var contact = WppCore.getContactName(userJid);
        var number = userJid.getPhoneNumber();
        if (!TextUtils.isEmpty(contact))
            sb.append(String.format(Utils.getApplication().getString(ResId.string.contact_s), contact)).append("\n");
        sb.append(String.format(Utils.getApplication().getString(ResId.string.phone_number_s), number)).append("\n");
        var ip = (String) XposedHelpers.getObjectField(wamCall, "callPeerIpStr");
        if (ip != null) {
            var client = new OkHttpClient.Builder().build();
            var url = "http://ip-api.com/json/" + ip;
            var request = new okhttp3.Request.Builder().url(url).build();
            var content = client.newCall(request).execute().body().string();
            var json = new JSONObject(content);
            var country = json.getString("country");
            var city = json.getString("city");
            sb.append(String.format(Utils.getApplication().getString(ResId.string.country_s), country)).append("\n").append(String.format(Utils.getApplication().getString(ResId.string.city_s), city)).append("\n").append(String.format(Utils.getApplication().getString(ResId.string.ip_s), ip)).append("\n");
        }
        var platform = (String) XposedHelpers.getObjectField(wamCall, "callPeerPlatform");
        if (platform != null)
            sb.append(String.format(Utils.getApplication().getString(ResId.string.platform_s), platform)).append("\n");
        var wppVersion = (String) XposedHelpers.getObjectField(wamCall, "callPeerAppVersion");
        if (wppVersion != null)
            sb.append(String.format(Utils.getApplication().getString(ResId.string.wpp_version_s), wppVersion)).append("\n");
        Utils.showNotification(Utils.getApplication().getString(ResId.string.call_information), sb.toString());
    }

    @NonNull
    @Override
    public String getPluginName() {
        return "CallInformation";
    }
}
