package com.wa.toolkit.utils

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import android.content.pm.PackageManager

object ContactHelper {

    @JvmStatic
    fun getContactName(context: Context, jid: String?): String? {
        if (jid == null) return null
        if (context.checkSelfPermission(android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            return null
        }

        var phoneNumber = jid.replace("@s.whatsapp.net", "").replace("@g.us", "")
        if (phoneNumber.contains("@")) {
            phoneNumber = phoneNumber.split("@")[0]
        }

        return try {
            val uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber))
            val projection = arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME)

            context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    cursor.getString(0)
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
