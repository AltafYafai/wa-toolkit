package com.wa.toolkit.xposed.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.*
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.TypedValue
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.documentfile.provider.DocumentFile
import com.wa.toolkit.App
import com.wa.toolkit.WppXposed
import io.github.libxposed.api.XposedModule
import com.wa.toolkit.xposed.core.FeatureLoader
import com.wa.toolkit.xposed.core.WppCore
import com.wa.toolkit.xposed.core.components.FMessageWpp
import de.robv.android.xposed.XSharedPreferences
import de.robv.android.xposed.XposedBridge
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.regex.Pattern

object Utils {

    private val executorService: ExecutorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
    
    @JvmField
    var xprefs: XSharedPreferences? = null
    
    private val ids = HashMap<String, Int>()

    @JvmStatic
    fun init(loader: ClassLoader?) {
        val context = getApplication()
        val notificationManager = NotificationManagerCompat.from(context)
        val channel = NotificationChannel("watoolkit", "Whatsapp Toolkit", NotificationManager.IMPORTANCE_HIGH)
        notificationManager.createNotificationChannel(channel)
    }

    @JvmStatic
    fun getApplication(): Application {
        return FeatureLoader.mApp ?: App.getInstance()
    }

    @JvmStatic
    fun getExecutor(): ExecutorService {
        return executorService
    }

    @JvmStatic
    fun doRestart(context: Context): Boolean {
        val packageManager = context.packageManager
        val intent = packageManager.getLaunchIntentForPackage(context.packageName) ?: return false
        val componentName = intent.component
        val mainIntent = Intent.makeRestartActivityTask(componentName)
        mainIntent.setPackage(context.packageName)
        context.startActivity(mainIntent)
        Runtime.getRuntime().exit(0)
        return true
    }

    /**
     * Retrieves the resource ID by name and type.
     * Uses caching to improve performance for repeated lookups.
     *
     * @param name The resource name to look up
     * @param type The resource type (e.g., "id", "drawable", "layout", "string")
     * @return The resource ID or -1 if not found or an error occurred
     */
    @SuppressLint("DiscouragedApi")
    @JvmStatic
    fun getID(name: String?, type: String?): Int {
        if (name.isNullOrEmpty() || type.isNullOrEmpty()) {
            return -1
        }

        val key = "${type}_$name"

        synchronized(ids) {
            val cachedId = ids[key]
            if (cachedId != null) return cachedId
        }

        return try {
            val app = getApplication()
            val context = app.applicationContext
            val id = context.resources.getIdentifier(name, type, app.packageName)

            synchronized(ids) {
                ids[key] = id
            }

            id
        } catch (e: Exception) {
            XposedBridge.log("Error getting resource ID: type=$type, name=$name, error: ${e.message}")
            -1
        }
    }

    @JvmStatic
    fun dipToPixels(dipValue: Float): Int {
        val metrics = FeatureLoader.mApp?.resources?.displayMetrics ?: getApplication().resources.displayMetrics
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, metrics).toInt()
    }

    @JvmStatic
    fun getMyNumber(): String {
        val app = getApplication()
        return app.getSharedPreferences("${app.packageName}_preferences_light", Context.MODE_PRIVATE)
            .getString("ph", "") ?: ""
    }

    @JvmStatic
    fun getDateTimeFromMillis(timestamp: Long): String {
        return SimpleDateFormat("dd/MM/yyyy hh:mm:ss a", Locale.getDefault()).format(Date(timestamp))
    }

    @SuppressLint("SdCardPath")
    @JvmStatic
    @Throws(Exception::class)
    fun getDestination(name: String): String {
        val prefs = xprefs ?: throw Exception("Preferences not initialized")
        if (prefs.getBoolean("lite_mode", false)) {
            val folder = WppCore.getPrivString("download_folder", null) ?: throw Exception("Download Folder is not selected!")
            val documentFile = DocumentFile.fromTreeUri(getApplication(), Uri.parse(folder)) ?: throw Exception("Invalid folder URI")
            val wppFolder = getURIFolderByName(documentFile, "WhatsApp", true) ?: throw Exception("WhatsApp folder not found")
            getURIFolderByName(wppFolder, name, true) ?: throw Exception("Folder not found!")
            return "$folder/WhatsApp/$name"
        }
        val folder = PrefUtils.getPref().getString("download_local", "/sdcard/Download") ?: "/sdcard/Download"
        val waFolder = File(folder, "WhatsApp")
        val filePath = File(waFolder, name)
        try {
            WppCore.getClientBridge().createDir(filePath.absolutePath)
        } catch (ignored: Exception) {
        }
        return filePath.absolutePath + "/"
    }

    @JvmStatic
    fun getURIFolderByName(documentFile: DocumentFile?, folderName: String, createDir: Boolean): DocumentFile? {
        if (documentFile == null) return null
        
        val files = documentFile.listFiles()
        for (file in files) {
            if (file.name == folderName) {
                return file
            }
        }
        
        return if (createDir) {
            documentFile.createDirectory(folderName)
        } else {
            null
        }
    }

    @JvmStatic
    fun copyFile(srcFile: File?, destFolder: String, name: String): String {
        if (srcFile == null || !srcFile.exists()) return "File not found or is null"

        val prefs = xprefs ?: return "Preferences not initialized"
        if (prefs.getBoolean("lite_mode", false)) {
            return try {
                val folder = WppCore.getPrivString("download_folder", null) ?: return "Download folder not selected"
                var documentFolder = DocumentFile.fromTreeUri(getApplication(), Uri.parse(folder)) ?: return "Invalid folder URI"
                val relativeDestFolder = destFolder.replace("$folder/", "")
                for (f in relativeDestFolder.split("/").filter { it.isNotEmpty() }) {
                    documentFolder = getURIFolderByName(documentFolder, f, false) ?: return "Failed to get folder: $f"
                }
                val newFile = documentFolder.createFile("*/*", name) ?: return "Failed to create destination file"

                val contentResolver = getApplication().contentResolver
                FileInputStream(srcFile).use { input ->
                    contentResolver.openOutputStream(newFile.uri)?.use { output ->
                        input.copyTo(output)
                        ""
                    } ?: "Failed to open output stream"
                }
            } catch (e: Exception) {
                XposedBridge.log(e)
                e.message ?: "Unknown error"
            }
        } else {
            val destFile = File(destFolder, name)
            return try {
                FileInputStream(srcFile).use { input ->
                    WppCore.getClientBridge().openFile(destFile.absolutePath, true)?.use { pfd ->
                        FileOutputStream(pfd.fileDescriptor).use { output ->
                            input.copyTo(output)
                            scanFile(destFile)
                            ""
                        }
                    } ?: "Failed to open parcel file descriptor"
                }
            } catch (e: Exception) {
                XposedBridge.log(e)
                e.message ?: "Unknown error"
            }
        }
    }

    @JvmStatic
    fun showToast(message: String, length: Int) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            Toast.makeText(getApplication(), message, length).show()
        } else {
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(getApplication(), message, length).show()
            }
        }
    }

    @JvmStatic
    fun setToClipboard(string: String) {
        val clipboard = getApplication().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("label", string)
        clipboard.setPrimaryClip(clip)
    }

    @JvmStatic
    fun generateName(userJid: FMessageWpp.UserJid, fileFormat: String): String {
        val contactName = WppCore.getContactName(userJid)
        val number = userJid.phoneRawString
        val dateStr = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault()).format(Date())
        return "${toValidFileName(contactName)}_${number}_$dateStr.$fileFormat"
    }

    @JvmStatic
    fun toValidFileName(input: String): String {
        return input.replace("[:\\\\/*\"?|<>']".toRegex(), " ")
    }

    @JvmStatic
    fun scanFile(file: File) {
        MediaScannerConnection.scanFile(
            getApplication(),
            arrayOf(file.absolutePath),
            arrayOf(MimeTypeUtils.getMimeTypeFromExtension(file.absolutePath))
        ) { _, _ -> }
    }

    @JvmStatic
    fun getProperties(prefs: XSharedPreferences, key: String, checkKey: String?): Properties {
        val properties = Properties()
        if (checkKey != null && !prefs.getBoolean(checkKey, false)) return properties
        
        val text = prefs.getString(key, "") ?: ""
        val pattern = Pattern.compile("^/\\*\\s*(.*?)\\s*\\*/", Pattern.DOTALL)
        val matcher = pattern.matcher(text)

        if (matcher.find()) {
            val propertiesText = matcher.group(1) ?: ""
            val lines = propertiesText.split("\\s*\\n\\s*".toRegex())

            for (line in lines) {
                if (line.isBlank()) continue
                val keyValue = line.split("\\s*=\\s*".toRegex(), 2)
                if (keyValue.size == 2) {
                    val skey = keyValue[0].trim()
                    val value = keyValue[1].trim().replace("^\"|\"$".toRegex(), "")
                    properties[skey] = value
                }
            }
        }

        return properties
    }

    @JvmStatic
    fun tryParseInt(value: String?, default: Int): Int {
        return value?.trim()?.toIntOrNull() ?: default
    }

    @SuppressLint("PrivateApi")
    @JvmStatic
    fun getApplicationByReflect(): Application {
        return try {
            val activityThreadClass = Class.forName("android.app.ActivityThread")
            val thread = activityThreadClass.getMethod("currentActivityThread").invoke(null)
            val app = activityThreadClass.getMethod("getApplication").invoke(thread) as? Application
            app ?: throw NullPointerException("Application not initialized")
        } catch (e: Exception) {
            e.printStackTrace()
            throw NullPointerException("Application not initialized")
        }
    }

    @JvmStatic
    fun <T> binderLocalScope(block: () -> T): T {
        val identity = Binder.clearCallingIdentity()
        return try {
            block()
        } finally {
            Binder.restoreCallingIdentity(identity)
        }
    }

    @JvmStatic
    fun getAuthorFromCss(code: String?): String? {
        if (code == null) return null
        val match = Pattern.compile("author\\s*=\\s*(.*?)\n").matcher(code)
        return if (match.find()) match.group(1) else null
    }

    @SuppressLint("MissingPermission")
    @JvmStatic
    fun showNotification(title: String, content: String) {
        val context = getApplication()
        val notificationManager = NotificationManagerCompat.from(context)
        val channel = NotificationChannel("watoolkit", "Whatsapp Toolkit", NotificationManager.IMPORTANCE_HIGH)
        notificationManager.createNotificationChannel(channel)
        
        val notification = NotificationCompat.Builder(context, "watoolkit")
            .setSmallIcon(android.R.mipmap.sym_def_app_icon)
            .setContentTitle(title)
            .setContentText(content)
            .setAutoCancel(true)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            
        notificationManager.notify(Random().nextInt(), notification.build())
    }

    @JvmStatic
    fun openLink(mActivity: Activity, url: String) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        mActivity.startActivity(browserIntent)
    }
}
ent)
    }
}
