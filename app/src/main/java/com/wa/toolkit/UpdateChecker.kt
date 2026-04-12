package com.wa.toolkit

import android.app.Activity
import com.wa.toolkit.xposed.core.WppCore
import com.wa.toolkit.xposed.core.components.AlertDialogWpp
import com.wa.toolkit.xposed.utils.Utils
import de.robv.android.xposed.XposedBridge
import io.noties.markwon.Markwon
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class UpdateChecker(private val mActivity: Activity) : Runnable {

    companion object {
        private const val LATEST_RELEASE_API = ""
        private const val RELEASE_TAG_PREFIX = "debug-"
        private const val TELEGRAM_UPDATE_URL = ""

        @Volatile
        private var httpClient: OkHttpClient? = null

        @Synchronized
        private fun getHttpClient(): OkHttpClient {
            return httpClient ?: OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build().also { httpClient = it }
        }
    }

    override fun run() {
        try {
            val request = Request.Builder()
                .url(LATEST_RELEASE_API)
                .build()

            var hash = ""
            var changelog = ""
            var publishedAt = ""

            getHttpClient().newCall(request).execute().use { response ->
                if (!response.isSuccessful) return

                val content = response.body?.string() ?: return
                val release = JSONObject(content)
                val tagName = release.optString("tag_name", "")

                if (tagName.isBlank() || !tagName.startsWith(RELEASE_TAG_PREFIX)) {
                    return
                }

                hash = tagName.substring(RELEASE_TAG_PREFIX.length).trim()
                changelog = release.optString("body", "No changelog available.").trim()
                publishedAt = release.optString("published_at", "")
            }

            if (hash.isBlank()) return

            val appInfo = mActivity.packageManager.getPackageInfo(BuildConfig.APPLICATION_ID, 0)
            val isNewVersion = !appInfo.versionName.lowercase().contains(hash.lowercase().trim())
            val isIgnored = WppCore.getPrivString("ignored_version", "") == hash

            if (isNewVersion && !isIgnored) {
                mActivity.runOnUiThread {
                    showUpdateDialog(hash, changelog, publishedAt)
                }
            }
        } catch (e: Exception) {
            XposedBridge.log(e)
        }
    }

    private fun showUpdateDialog(hash: String, changelog: String, publishedAt: String) {
        try {
            val markwon = Markwon.create(mActivity)
            val dialog = AlertDialogWpp(mActivity)

            // Format the published date
            val formattedDate = formatPublishedDate(publishedAt)

            // Build simple message with version and date
            val message = StringBuilder()
            message.append("📦 **Version:** `").append(hash).append("`\n")
            if (formattedDate.isNotEmpty()) {
                message.append("📅 **Released:** ").append(formattedDate).append("\n")
            }
            message.append("\n### What's New\n\n").append(changelog)

            dialog.setTitle("🎉 New Update Available!")
            dialog.setMessage(markwon.toMarkdown(message.toString()))
            dialog.setNegativeButton("Ignore") { dialog1, _ ->
                WppCore.setPrivString("ignored_version", hash)
                dialog1.dismiss()
            }
            dialog.setPositiveButton("Update Now") { dialog1, _ ->
                Utils.openLink(mActivity, TELEGRAM_UPDATE_URL)
                dialog1.dismiss()
            }
            dialog.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun formatPublishedDate(isoDate: String?): String {
        if (isoDate.isNullOrEmpty()) return ""

        return try {
            // Parse ISO 8601 date
            val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
            isoFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = isoFormat.parse(isoDate)

            if (date != null) {
                // Format to readable date
                val displayFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)
                displayFormat.format(date)
            } else ""
        } catch (e: Exception) {
            XposedBridge.log(e)
            ""
        }
    }
}
