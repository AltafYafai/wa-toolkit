package com.wa.toolkit.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wa.toolkit.R
import com.wa.toolkit.ui.SettingsViewModel
import com.wa.toolkit.ui.preferences.SwitchSetting
import com.wa.toolkit.ui.preferences.StringSwitchSetting
import com.wa.toolkit.ui.preferences.ActionSetting
import com.wa.toolkit.ui.preferences.CategoryHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.privacy)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { CategoryHeader("Anti-Revoke & Security") }
            item {
                SwitchSetting(
                    title = stringResource(R.string.antirevoke),
                    summary = stringResource(R.string.antirevoke_sum),
                    viewModel = viewModel,
                    prefKey = "antirevoke",
                    icon = R.drawable.deleted
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.antirevokestatus),
                    summary = stringResource(R.string.antirevokestatus_sum),
                    viewModel = viewModel,
                    prefKey = "antirevokestatus",
                    icon = R.drawable.ic_round_warning_24
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.antidisappearing),
                    summary = stringResource(R.string.antidisappearing_sum),
                    viewModel = viewModel,
                    prefKey = "antidisappearing",
                    icon = R.drawable.ic_privacy
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.delete_for_everyone_all_messages),
                    summary = stringResource(R.string.delete_for_everyone_all_messages_sum),
                    viewModel = viewModel,
                    prefKey = "revokeallmessages",
                    icon = R.drawable.ic_round_check_circle_24
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.panic_mode),
                    summary = stringResource(R.string.panic_mode_sum),
                    viewModel = viewModel,
                    prefKey = "panic_mode",
                    icon = R.drawable.ic_round_warning_24
                )
            }
            item {
                StringSwitchSetting(
                    title = stringResource(R.string.hide_archived_chat),
                    summary = stringResource(R.string.hide_archived_chat_sum),
                    viewModel = viewModel,
                    prefKey = "typearchive",
                    icon = R.drawable.ic_privacy
                )
            }

            item { CategoryHeader("Stealth & Ghost Mode") }
            item {
                SwitchSetting(
                    title = stringResource(R.string.ghost_mode_title),
                    summary = stringResource(R.string.ghost_mode_sum),
                    viewModel = viewModel,
                    prefKey = "ghostmode",
                    icon = R.drawable.eye_disabled
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.ghostmode),
                    summary = stringResource(R.string.ghostmode_sum),
                    viewModel = viewModel,
                    prefKey = "ghostmode_t",
                    icon = R.drawable.ic_privacy
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.ghostmode_r),
                    summary = stringResource(R.string.ghostmode_sum_r),
                    viewModel = viewModel,
                    prefKey = "ghostmode_r",
                    icon = R.drawable.ic_recording
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.freezelastseen),
                    summary = stringResource(R.string.freezelastseen_sum),
                    viewModel = viewModel,
                    prefKey = "freezelastseen",
                    icon = R.drawable.eye_disabled
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.show_freezeLastSeen_button),
                    summary = stringResource(R.string.show_freezeLastSeen_sum),
                    viewModel = viewModel,
                    prefKey = "show_freezeLastSeen",
                    icon = R.drawable.ic_round_settings_24
                )
            }

            item { CategoryHeader("Alerts & Indicators") }
            item {
                SwitchSetting(
                    title = stringResource(R.string.always_online),
                    summary = stringResource(R.string.always_online_sum),
                    viewModel = viewModel,
                    prefKey = "always_online",
                    icon = R.drawable.online
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.show_toast_on_contact_online),
                    summary = stringResource(R.string.show_toast_on_contact_online_sum),
                    viewModel = viewModel,
                    prefKey = "showonline",
                    icon = R.drawable.online
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.toast_on_delete),
                    summary = stringResource(R.string.toast_on_delete_sum),
                    viewModel = viewModel,
                    prefKey = "toastdeleted",
                    icon = R.drawable.ic_round_warning_24
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.toast_on_viewed_message),
                    summary = stringResource(R.string.toast_on_viewed_message_sum),
                    viewModel = viewModel,
                    prefKey = "toast_viewed_message",
                    icon = R.drawable.eye_enabled
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.show_contact_blocked),
                    summary = stringResource(R.string.show_contact_blocked_sum),
                    viewModel = viewModel,
                    prefKey = "verify_blocked_contact",
                    icon = R.drawable.eye_disabled
                )
            }

            item { CategoryHeader("Advanced Privacy") }
            item {
                SwitchSetting(
                    title = stringResource(R.string.lockedchats_enhancer),
                    summary = stringResource(R.string.lockedchats_enhancer_sum),
                    viewModel = viewModel,
                    prefKey = "lockedchats_enhancer",
                    icon = R.drawable.ic_privacy
                )
            }
            item {
                StringSwitchSetting(
                    title = stringResource(R.string.custom_privacy_per_contact),
                    summary = stringResource(R.string.custom_privacy_per_contact_sum),
                    viewModel = viewModel,
                    prefKey = "custom_privacy_type",
                    icon = R.drawable.ic_person
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.hideread),
                    summary = stringResource(R.string.hideread_sum),
                    viewModel = viewModel,
                    prefKey = "hideread",
                    icon = R.drawable.eye_disabled
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.hideread_group),
                    summary = stringResource(R.string.hideread_group_sum),
                    viewModel = viewModel,
                    prefKey = "hideread_group",
                    icon = R.drawable.eye_disabled
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.hidereceipt),
                    summary = stringResource(R.string.hidereceipt_sum),
                    viewModel = viewModel,
                    prefKey = "hidereceipt",
                    icon = R.drawable.ic_round_check_circle_24
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.view_seen_tick),
                    summary = stringResource(R.string.view_seen_tick_sum),
                    viewModel = viewModel,
                    prefKey = "hide_seen_view",
                    icon = R.drawable.eye_enabled
                )
            }
            item {
                StringSwitchSetting(
                    title = stringResource(R.string.show_button_to_send_blue_tick),
                    summary = stringResource(R.string.show_button_to_send_blue_tick_sum),
                    viewModel = viewModel,
                    prefKey = "seentick",
                    icon = R.drawable.ic_round_check_circle_24
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.hide_once_view_seen),
                    summary = stringResource(R.string.hide_once_view_seen_sum),
                    viewModel = viewModel,
                    prefKey = "hide_once_view_seen",
                    icon = R.drawable.eye_enabled
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.hide_audio_seen),
                    summary = stringResource(R.string.hide_audio_seen_sum),
                    viewModel = viewModel,
                    prefKey = "hide_audio_seen",
                    icon = R.drawable.ic_recording
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.local_vault),
                    summary = stringResource(R.string.local_vault_sum),
                    viewModel = viewModel,
                    prefKey = "local_vault",
                    icon = R.drawable.ic_delete
                )
            }

            item { CategoryHeader("Technical Protection") }
            item {
                SwitchSetting(
                    title = stringResource(R.string.metadata_stripper),
                    summary = stringResource(R.string.metadata_stripper_sum),
                    viewModel = viewModel,
                    prefKey = "metadata_stripper",
                    icon = R.drawable.ic_privacy
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.disable_secure_flag),
                    summary = stringResource(R.string.disable_secure_flag_sum),
                    viewModel = viewModel,
                    prefKey = "disable_secure_flag",
                    icon = R.drawable.ic_privacy
                )
            }
        }
    }
}
