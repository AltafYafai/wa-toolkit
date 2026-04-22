package com.wa.toolkit.xposed.utils

import android.content.Context
import android.content.res.Resources

object ResId {

    object drawable {
        @JvmField var eye_disabled: Int = 0
        @JvmField var eye_enabled: Int = 0
        @JvmField var admin: Int = 0
        @JvmField var preview_eye: Int = 0
        @JvmField var refresh: Int = 0
        @JvmField var ghost_disabled: Int = 0
        @JvmField var ghost_enabled: Int = 0
        @JvmField var airplane_enabled: Int = 0
        @JvmField var airplane_disabled: Int = 0
        @JvmField var online: Int = 0
        @JvmField var deleted: Int = 0
        @JvmField var download: Int = 0
        @JvmField var camera: Int = 0
        @JvmField var edit2: Int = 0
        @JvmField var ic_privacy: Int = 0
        @JvmField var user_foreground: Int = 0
        @JvmField var ic_groups: Int = 0
    }

    object string {
        @JvmField var edited_history: Int = 0
        @JvmField var dnd_message: Int = 0
        @JvmField var dnd_mode_title: Int = 0
        @JvmField var freezelastseen_message: Int = 0
        @JvmField var freezelastseen_title: Int = 0
        @JvmField var activate: Int = 0
        @JvmField var cancel: Int = 0
        @JvmField var message_original: Int = 0
        @JvmField var new_chat: Int = 0
        @JvmField var number_with_country_code: Int = 0
        @JvmField var message: Int = 0
        @JvmField var download: Int = 0
        @JvmField var error_when_saving_try_again: Int = 0
        @JvmField var msg_text_status_not_downloadable: Int = 0
        @JvmField var saved_to: Int = 0
        @JvmField var restart_whatsapp: Int = 0
        @JvmField var restart_wpp: Int = 0
        @JvmField var send_blue_tick: Int = 0
        @JvmField var sending_read_blue_tick: Int = 0
        @JvmField var send: Int = 0
        @JvmField var send_sticker: Int = 0
        @JvmField var do_you_want_to_send_sticker: Int = 0
        @JvmField var whatsapp_call: Int = 0
        @JvmField var phone_call: Int = 0
        @JvmField var yes: Int = 0
        @JvmField var no: Int = 0
        @JvmField var version_error: Int = 0
        @JvmField var copy_to_clipboard: Int = 0
        @JvmField var copied_to_clipboard: Int = 0
        @JvmField var error_detected: Int = 0
        @JvmField var rebooting: Int = 0
        @JvmField var deleted_status: Int = 0
        @JvmField var deleted_message: Int = 0
        @JvmField var toast_online: Int = 0
        @JvmField var message_removed_on: Int = 0
        @JvmField var loading: Int = 0
        @JvmField var delete_for_me: Int = 0
        @JvmField var share_as_status: Int = 0
        @JvmField var viewed_your_status: Int = 0
        @JvmField var viewed_your_message: Int = 0
        @JvmField var select_status_type: Int = 0
        @JvmField var open_camera: Int = 0
        @JvmField var edit_text: Int = 0
        @JvmField var select_a_color: Int = 0
        @JvmField var read_all_mark_as_read: Int = 0
        @JvmField var grant_permission: Int = 0
        @JvmField var expiration: Int = 0
        @JvmField var deleted_a_message_in_group: Int = 0
        @JvmField var allow: Int = 0
        @JvmField var invalid_folder: Int = 0
        @JvmField var uri_permission: Int = 0
        @JvmField var ghost_mode: Int = 0
        @JvmField var ghost_mode_message: Int = 0
        @JvmField var disable: Int = 0
        @JvmField var enable: Int = 0
        @JvmField var ghost_mode_s: Int = 0
        @JvmField var starting_cache: Int = 0
        @JvmField var bridge_error: Int = 0
        @JvmField var not_available: Int = 0
        @JvmField var app_name: Int = 0
        @JvmField var hideread: Int = 0
        @JvmField var hidestatusview: Int = 0
        @JvmField var hidereceipt: Int = 0
        @JvmField var ghostmode: Int = 0
        @JvmField var ghostmode_r: Int = 0
        @JvmField var custom_privacy: Int = 0
        @JvmField var custom_privacy_sum: Int = 0
        @JvmField var block_call: Int = 0
        @JvmField var contact_s: Int = 0
        @JvmField var phone_number_s: Int = 0
        @JvmField var country_s: Int = 0
        @JvmField var city_s: Int = 0
        @JvmField var ip_s: Int = 0
        @JvmField var platform_s: Int = 0
        @JvmField var wpp_version_s: Int = 0
        @JvmField var call_information: Int = 0
        @JvmField var ask_download_folder: Int = 0
        @JvmField var download_folder_permission: Int = 0
        @JvmField var no_contact_with_custom_privacy: Int = 0
        @JvmField var select_contacts: Int = 0
        @JvmField var download_not_available: Int = 0
        @JvmField var block_not_detected: Int = 0
        @JvmField var possible_block_detected: Int = 0
        @JvmField var checking_if_the_contact_is_blocked: Int = 0
        @JvmField var block_unverified: Int = 0
        @JvmField var warning_restore: Int = 0
        @JvmField var force_restore_backup_experimental: Int = 0
        @JvmField var force_restore_backup: Int = 0
        @JvmField var contact_probably_not_added: Int = 0
    }

    object id {
        @JvmField var tag_font_scale: Int = 0
    }

    object array {
        @JvmField var supported_versions_wpp: Int = 0
        @JvmField var supported_versions_business: Int = 0
    }

    @JvmStatic
    fun init(context: Context) {
        val packageName = context.packageName
        val res = context.resources
        
        mirrorFields(drawable::class.java, packageName, res, "drawable")
        mirrorFields(string::class.java, packageName, res, "string")
        mirrorFields(id::class.java, packageName, res, "id")
        mirrorFields(array::class.java, packageName, res, "array")
    }

    private fun mirrorFields(clazz: Class<*>, packageName: String, res: Resources, type: String) {
        for (field in clazz.declaredFields) {
            if (field.name == "INSTANCE" || field.name == "\$stable") continue
            try {
                val id = res.getIdentifier(field.name, type, packageName)
                if (id != 0) {
                    field.isAccessible = true
                    field.set(null, id)
                }
            } catch (e: Exception) {
                // Ignore
            }
        }
    }
}
