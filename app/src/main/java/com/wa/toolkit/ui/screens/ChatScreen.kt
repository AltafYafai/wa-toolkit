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
import com.wa.toolkit.ui.preferences.CategoryHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.chat)) },
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
            item { CategoryHeader("Message Enhancements") }
            item {
                SwitchSetting(
                    title = stringResource(R.string.show_edited_message_history),
                    summary = stringResource(R.string.show_edited_message_history_sum),
                    viewModel = viewModel,
                    prefKey = "antieditmessages",
                    icon = R.drawable.edit2
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.remove_see_more_button),
                    summary = stringResource(R.string.remove_see_more_button_),
                    viewModel = viewModel,
                    prefKey = "removeseemore",
                    icon = R.drawable.ic_round_check_circle_24
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.hidetag),
                    summary = stringResource(R.string.hidetag_sum),
                    viewModel = viewModel,
                    prefKey = "hidetag",
                    icon = R.drawable.ic_privacy
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.stamp_copied_messages),
                    summary = stringResource(R.string.stamp_copied_messages_sum),
                    viewModel = viewModel,
                    prefKey = "stamp_copied_message",
                    icon = R.drawable.ic_round_check_circle_24
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.disable_default_emojis),
                    summary = stringResource(R.string.disable_default_emojis_sum),
                    viewModel = viewModel,
                    prefKey = "disable_defemojis",
                    icon = R.drawable.ic_round_settings_24
                )
            }

            item { CategoryHeader("Limits & Controls") }
            item {
                SwitchSetting(
                    title = stringResource(R.string.disable_pinned_limit),
                    summary = stringResource(R.string.disable_pinned_limit_sum),
                    viewModel = viewModel,
                    prefKey = "pinnedlimit",
                    icon = R.drawable.ic_dashboard_black_24dp
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.removeforwardlimit),
                    summary = stringResource(R.string.removeforwardlimit_sum),
                    viewModel = viewModel,
                    prefKey = "removeforwardlimit",
                    icon = R.drawable.ic_round_check_circle_24
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.enable_confirmation_to_send_sticker),
                    summary = stringResource(R.string.enable_confirmation_to_send_sticker_sum),
                    viewModel = viewModel,
                    prefKey = "alertsticker",
                    icon = R.drawable.ic_round_warning_24
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.force_english),
                    summary = "Override system language for WhatsApp UI",
                    viewModel = viewModel,
                    prefKey = "force_english",
                    icon = R.drawable.ic_round_settings_24
                )
            }

            item { CategoryHeader("Safety & Interaction") }
            item {
                SwitchSetting(
                    title = stringResource(R.string.double_click_to_react),
                    summary = stringResource(R.string.double_click_to_like_sum),
                    viewModel = viewModel,
                    prefKey = "doubletap2like",
                    defaultValue = true,
                    icon = R.drawable.ic_round_check_circle_24
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

            item { CategoryHeader("Contextual Tools") }
            item {
                SwitchSetting(
                    title = stringResource(R.string.google_translate),
                    summary = stringResource(R.string.google_translate_sum),
                    viewModel = viewModel,
                    prefKey = "google_translate",
                    icon = R.drawable.ic_privacy
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.smart_reply),
                    summary = stringResource(R.string.smart_reply_sum),
                    viewModel = viewModel,
                    prefKey = "smart_reply",
                    icon = R.drawable.ic_round_check_circle_24
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.chat_summarization),
                    summary = stringResource(R.string.chat_summarization_sum),
                    viewModel = viewModel,
                    prefKey = "chat_summarization",
                    icon = R.drawable.ic_round_check_circle_24
                )
            }
        }
    }
}
