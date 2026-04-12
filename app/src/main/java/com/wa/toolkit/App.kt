package com.wa.toolkit

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.preference.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class App : Application() {

    companion object {
        private var instance: App? = null
        private val executorService: ExecutorService = Executors.newCachedThreadPool()
        private val mainHandler = Handler(Looper.getMainLooper())

        @JvmStatic
        fun showRequestStoragePermission(activity: Activity) {
            val builder = MaterialAlertDialogBuilder(activity)
            builder.setTitle(R.string.storage_permission)
            builder.setMessage(R.string.permission_storage)
            builder.setPositiveButton(R.string.allow) { _, _ ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val intent = Intent(android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent.data = Uri.fromParts("package", activity.packageName, null)
                    activity.startActivity(intent)
                } else {
                    ActivityCompat.requestPermissions(
                        activity,
                        arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE),
                        0
                    )
                }
            }
            builder.setNegativeButton(R.string.deny) { dialog, _ -> dialog.dismiss() }
            builder.show()
        }

        @JvmStatic
        fun setThemeMode(mode: Int) {
            when (mode) {
                0 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                1 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                2 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        @JvmStatic
        fun getInstance(): App {
            return instance!!
        }

        @JvmStatic
        fun getExecutorService(): ExecutorService {
            return executorService
        }

        @JvmStatic
        fun getMainHandler(): Handler {
            return mainHandler
        }

        @JvmStatic
        fun changeLanguage(context: Context) {
            val force = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("force_english", false)
            val locale = if (force) Locale.ENGLISH else Locale.getDefault()
            Locale.setDefault(locale)
            val res = context.resources
            val config = res.configuration
            config.setLocale(locale)
            @Suppress("DEPRECATION")
            res.updateConfiguration(config, res.displayMetrics)
        }

        @JvmStatic
        fun getWhatsappToolkitFolder(): File {
            val download = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val whatsappToolkitFolder = File(download, "WhatsappToolkit")
            if (!whatsappToolkitFolder.exists()) whatsappToolkitFolder.mkdirs()
            return whatsappToolkitFolder
        }

        @JvmStatic
        fun isOriginalPackage(): Boolean {
            return BuildConfig.APPLICATION_ID == "com.wa.toolkit"
        }
    }

    @SuppressLint("ApplySharedPref")
    override fun onCreate() {
        super.onCreate()
        instance = this
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val modeStr = sharedPreferences.getString("thememode", "0") ?: "0"
        val mode = modeStr.toIntOrNull() ?: 0
        setThemeMode(mode)
        changeLanguage(this)
    }

    fun restartApp(packageWpp: String) {
        val intent = Intent("${BuildConfig.APPLICATION_ID}.WHATSAPP.RESTART")
        intent.putExtra("PKG", packageWpp)
        sendBroadcast(intent)
    }
}
