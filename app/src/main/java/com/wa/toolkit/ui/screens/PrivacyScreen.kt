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
import com.wa.toolkit.ui.preferences.*

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

            item { CategoryHeader("Presence & Alerts") }
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

            item { CategoryHeader("Interactive Privacy") }
            item {
                SwitchSetting(
                    title = stringResource(R.string.stealth_typing),
                    summary = stringResource(R.string.stealth_typing_sum),
                    viewModel = viewModel,
                    prefKey = "stealth_typing",
                    icon = R.drawable.ic_privacy
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.blueonreply),
                    summary = stringResource(R.string.blueonreply_sum),
                    viewModel = viewModel,
                    prefKey = "blueonreply",
                    icon = R.drawable.eye_enabled
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
                    title = stringResource(R.string.hide_audio_seen),
                    summary = stringResource(R.string.hide_audio_seen_sum),
                    viewModel = viewModel,
                    prefKey = "hide_audio_seen",
                    icon = R.drawable.ic_recording
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
                    title = stringResource(R.string.local_vault),
                    summary = stringResource(R.string.local_vault_sum),
                    viewModel = viewModel,
                    prefKey = "local_vault",
                    icon = R.drawable.ic_delete
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.call_blocker),
                    summary = stringResource(R.string.call_blocker_sum),
                    viewModel = viewModel,
                    prefKey = "call_privacy",
                    icon = R.drawable.eye_disabled
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
