package com.wa.toolkit.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast
import androidx.preference.PreferenceManager
import com.wa.toolkit.App
import com.wa.toolkit.R
import com.wa.toolkit.xposed.core.FeatureLoader
import com.wa.toolkit.xposed.utils.Utils
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import rikka.core.util.IOUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.HashSet
import java.util.Locale
import java.util.Objects

object ConfigUtil {

    private fun getJsonObject(prefs: SharedPreferences): JSONObject {
        val entries = prefs.all
        val jsonObject = JSONObject()
        for (entry in entries.entries) {
            val type = JSONObject()
            var keyValue = entry.value
            if (keyValue is HashSet<*>) {
                keyValue = JSONArray(ArrayList(keyValue))
            }
            type.put("type", keyValue?.javaClass?.simpleName ?: "null")
            type.put("value", keyValue)
            jsonObject.put(entry.key, type)
        }
        return jsonObject
    }

    fun exportConfigs(context: Context) {
        if (FilePicker.fileSalve == null) {
            Toast.makeText(context, "FilePicker not initialized", Toast.LENGTH_SHORT).show()
            return
        }
        FilePicker.setOnUriPickedListener(object : FilePicker.OnUriPickedListener {
            override fun onUriPicked(uri: android.net.Uri) {
                try {
                    context.contentResolver.openOutputStream(uri)?.use { output ->
                        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
                        val jsonObject = getJsonObject(prefs)
                        output.write(jsonObject.toString(4).toByteArray())
                    }
                    Toast.makeText(context, context.getString(R.string.configs_saved), Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                }
            }
        })
        val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US)
        val formattedDate = dateFormat.format(Date())
        FilePicker.fileSalve?.launch("whatsapp_toolkit_configs_$formattedDate.json")
    }

    fun importConfigs(context: Context) {
        if (FilePicker.fileCapture == null) {
            Toast.makeText(context, "FilePicker not initialized", Toast.LENGTH_SHORT).show()
            return
        }
        FilePicker.setOnUriPickedListener(object : FilePicker.OnUriPickedListener {
            override fun onUriPicked(uri: android.net.Uri) {
                try {
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        val data = IOUtils.toString(input)
                        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
                        val jsonObject = JSONObject(data)
                        
                        // Clear existing
                        prefs.edit().clear().apply()
                        
                        val keys = jsonObject.keys()
                        while (keys.hasNext()) {
                            val keyName = keys.next()
                            var value = jsonObject.get(keyName)
                            var type = value.javaClass.simpleName
                            
                            if (value is JSONObject) {
                                type = value.getString("type")
                                value = value.get("value")
                            }

                            when (type) {
                                "JSONArray" -> {
                                    val jsonArray = value as JSONArray
                                    val hashSet = HashSet<String>()
                                    for (i in 0 until jsonArray.length()) {
                                        hashSet.add(jsonArray.getString(i))
                                    }
                                    prefs.edit().putStringSet(keyName, hashSet).apply()
                                }
                                "String" -> prefs.edit().putString(keyName, value as String).apply()
                                "Boolean" -> prefs.edit().putBoolean(keyName, value as Boolean).apply()
                                "Integer" -> prefs.edit().putInt(keyName, value as Int).apply()
                                "Long" -> prefs.edit().putLong(keyName, value as Long).apply()
                                "Double", "Float" -> prefs.edit().putFloat(keyName, value.toString().toFloat()).apply()
                            }
                        }
                    }
                    Toast.makeText(context, context.getString(R.string.configs_imported), Toast.LENGTH_SHORT).show()
                    restartApps()
                } catch (e: Exception) {
                    Log.e("importConfigs", e.message, e)
                    Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                }
            }
        })
        FilePicker.fileCapture?.launch(arrayOf("application/json"))
    }

    fun resetConfigs(context: Context) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.edit().clear().apply()
        restartApps()
        Utils.showToast(context.getString(R.string.configs_reset), Toast.LENGTH_SHORT)
    }

    private fun restartApps() {
        App.getInstance().restartApp(FeatureLoader.PACKAGE_WPP)
        App.getInstance().restartApp(FeatureLoader.PACKAGE_BUSINESS)
    }
}