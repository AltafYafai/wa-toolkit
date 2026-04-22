package com.wa.toolkit.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wa.toolkit.R
import com.wa.toolkit.ui.SettingsViewModel
import com.wa.toolkit.ui.preferences.SwitchSetting
import com.wa.toolkit.ui.preferences.ListSetting
import com.wa.toolkit.ui.preferences.CategoryHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.status)) },
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
            item { CategoryHeader("Status Interaction") }
            item {
                SwitchSetting(
                    title = stringResource(R.string.statusdowload),
                    summary = stringResource(R.string.statusdowload_sum),
                    viewModel = viewModel,
                    prefKey = "downloadstatus",
                    icon = R.drawable.download
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.enable_copy_status),
                    summary = stringResource(R.string.enable_copy_status_sum),
                    viewModel = viewModel,
                    prefKey = "copystatus",
                    icon = R.drawable.edit2
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.hidestatusview),
                    summary = stringResource(R.string.hidestatusview_sum),
                    viewModel = viewModel,
                    prefKey = "statusseen",
                    icon = R.drawable.eye_enabled
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.toast_on_viewed_status),
                    summary = stringResource(R.string.toast_on_viewed_status_sum),
                    viewModel = viewModel,
                    prefKey = "toast_viewed_status",
                    icon = R.drawable.eye_enabled
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.status_analytics),
                    summary = stringResource(R.string.status_analytics_sum),
                    viewModel = viewModel,
                    prefKey = "status_analytics",
                    icon = R.drawable.eye_enabled
                )
            }

            item { CategoryHeader("Status Playback") }
            item {
                SwitchSetting(
                    title = stringResource(R.string.disable_auto_status),
                    summary = stringResource(R.string.disable_auto_status_sum),
                    viewModel = viewModel,
                    prefKey = "autonext_status",
                    icon = R.drawable.ic_pause
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.old_statuses),
                    summary = stringResource(R.string.old_statuses_sum),
                    viewModel = viewModel,
                    prefKey = "oldstatus",
                    icon = R.drawable.ic_dashboard_black_24dp
                )
            }
            item {
                ListSetting(
                    title = "Status UI Style",
                    summary = "Change how statuses are displayed",
                    viewModel = viewModel,
                    prefKey = "status_style",
                    entries = context.resources.getStringArray(R.array.status_style_entries),
                    entryValues = context.resources.getStringArray(R.array.status_style_values),
                    defaultValue = "0",
                    icon = R.drawable.ic_round_settings_24
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.vertical_status),
                    summary = stringResource(R.string.vertical_status_sum),
                    viewModel = viewModel,
                    prefKey = "vertical_status",
                    icon = R.drawable.ic_dashboard_black_24dp
                )
            }

            item { CategoryHeader("Privacy & Appearance") }
            item {
                SwitchSetting(
                    title = stringResource(R.string.disable_status_in_the_profile_photo),
                    summary = stringResource(R.string.disable_status_in_the_profile_photo_sum),
                    viewModel = viewModel,
                    prefKey = "disable_profile_status",
                    icon = R.drawable.ic_person
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.enable_facebook_style_for_status),
                    summary = stringResource(R.string.enable_facebook_style_for_status_sum),
                    viewModel = viewModel,
                    prefKey = "facebook_style_status",
                    icon = R.drawable.preview_eye
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.custom_colors_for_text_status),
                    summary = stringResource(R.string.custom_colors_for_text_status_sum),
                    viewModel = viewModel,
                    prefKey = "statuscomposer",
                    icon = R.drawable.edit2
                )
            }
        }
    }
}
