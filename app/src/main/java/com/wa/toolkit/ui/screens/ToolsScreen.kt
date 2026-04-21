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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tools & UI Settings") },
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item { CategoryHeader("Interface & Layout") }
            item {
                SwitchSetting(
                    title = "Floating Home Menu",
                    summary = "Modern centered action menu",
                    viewModel = viewModel,
                    prefKey = "floatingmenu"
                )
            }
            item {
                SwitchSetting(
                    title = "Experimental Settings UI",
                    summary = "Redesigned settings navigation",
                    viewModel = viewModel,
                    prefKey = "novaconfig"
                )
            }
            item {
                SwitchSetting(
                    title = "Menu Icons",
                    summary = "Show icons next to menu items",
                    viewModel = viewModel,
                    prefKey = "menuwicon"
                )
            }

            item { Spacer(Modifier.height(16.dp)) }
            item { CategoryHeader(stringResource(R.string.tab_architect)) }
            item {
                SwitchSetting(
                    title = stringResource(R.string.hide_channels),
                    summary = "Hide the Channels/Updates tab from home",
                    viewModel = viewModel,
                    prefKey = "hide_channels_tab"
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.hide_communities),
                    summary = "Hide the Communities tab from home",
                    viewModel = viewModel,
                    prefKey = "hide_communities_tab"
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.hide_status),
                    summary = "Hide the Status/Stories tab from home",
                    viewModel = viewModel,
                    prefKey = "hide_status_tab"
                )
            }

            item { Spacer(Modifier.height(16.dp)) }
            item { CategoryHeader("Profile & Home") }
            item {
                SwitchSetting(
                    title = stringResource(R.string.showname),
                    summary = stringResource(R.string.showname_sum),
                    viewModel = viewModel,
                    prefKey = "shownamehome"
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.showbio),
                    summary = stringResource(R.string.showbio_sum),
                    viewModel = viewModel,
                    prefKey = "showbiohome"
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.disable_status_in_the_profile_photo),
                    summary = stringResource(R.string.disable_status_in_the_profile_photo_sum),
                    viewModel = viewModel,
                    prefKey = "disable_profile_status"
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.separate_groups),
                    summary = stringResource(R.string.separate_groups_sum),
                    viewModel = viewModel,
                    prefKey = "separategroups"
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.new_ui_group_filter),
                    summary = stringResource(R.string.new_ui_group_filter_sum),
                    viewModel = viewModel,
                    prefKey = "filtergroups"
                )
            }

            item { Spacer(Modifier.height(16.dp)) }
            item { CategoryHeader("Indicators & Alerts") }
            item {
                SwitchSetting(
                    title = stringResource(R.string.show_online_dot_in_conversation_list),
                    summary = stringResource(R.string.show_online_dot_in_conversation_list_sum),
                    viewModel = viewModel,
                    prefKey = "dotonline"
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.show_online_last_seen_in_conversation_list),
                    summary = stringResource(R.string.show_online_last_seen_in_conversation_list_sum),
                    viewModel = viewModel,
                    prefKey = "showonlinetext"
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.show_toast_on_contact_online),
                    summary = stringResource(R.string.show_toast_on_contact_online_sum),
                    viewModel = viewModel,
                    prefKey = "show_toast_on_contact_online"
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.old_statuses),
                    summary = stringResource(R.string.old_statuses_sum),
                    viewModel = viewModel,
                    prefKey = "oldstatus"
                )
            }

            item { Spacer(Modifier.height(16.dp)) }
            item { CategoryHeader("System Tweaks") }
            item {
                SwitchSetting(
                    title = stringResource(R.string.lite_mode),
                    summary = stringResource(R.string.lite_mode_sum),
                    viewModel = viewModel,
                    prefKey = "lite_mode"
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.enable_spy),
                    summary = stringResource(R.string.enable_spy_sum),
                    viewModel = viewModel,
                    prefKey = "enable_spy"
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.bootloader_spoofer),
                    summary = stringResource(R.string.bootloader_spoofer_sum),
                    viewModel = viewModel,
                    prefKey = "bootloader_spoofer"
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.enable_tasker_automation),
                    summary = stringResource(R.string.enable_tasker_automation_sum),
                    viewModel = viewModel,
                    prefKey = "tasker"
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.verbose_logs),
                    summary = "Enable detailed logging for debugging",
                    viewModel = viewModel,
                    prefKey = "enablelogs"
                )
            }

            item { Spacer(Modifier.height(16.dp)) }
            item { CategoryHeader("Intelligence") }
            item {
                SwitchSetting(
                    title = "Disable Meta AI",
                    summary = "Completely hide Meta AI integration",
                    viewModel = viewModel,
                    prefKey = "metaai"
                )
            }
            item {
                SwitchSetting(
                    title = "Audio Transcription",
                    summary = "Enable native voice note transcripts",
                    viewModel = viewModel,
                    prefKey = "audio_transcription"
                )
            }
        }
    }
}
