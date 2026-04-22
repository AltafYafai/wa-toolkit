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
import com.wa.toolkit.ui.preferences.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomizationScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.perso)) },
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
            item { CategoryHeader("Interface & Styles") }
            item {
                ListSetting(
                    title = stringResource(R.string.changecolor_mode),
                    summary = "Color source",
                    viewModel = viewModel,
                    prefKey = "wae_color_mode",
                    entries = context.resources.getStringArray(R.array.wae_color_mode_entries),
                    entryValues = context.resources.getStringArray(R.array.wae_color_mode_values),
                    defaultValue = "monet",
                    icon = R.drawable.ic_round_settings_24
                )
            }
            item {
                ListSetting(
                    title = stringResource(R.string.wae_color_mode_preset),
                    summary = "Preset color",
                    viewModel = viewModel,
                    prefKey = "wae_color_preset",
                    entries = context.resources.getStringArray(R.array.wae_color_preset_entries),
                    entryValues = context.resources.getStringArray(R.array.wae_color_preset_values),
                    defaultValue = "green",
                    icon = R.drawable.ic_round_check_circle_24
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.pure_black),
                    summary = stringResource(R.string.pure_black_sum),
                    viewModel = viewModel,
                    prefKey = "pure_black",
                    icon = R.drawable.ic_round_settings_24
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.separate_groups),
                    summary = stringResource(R.string.separate_groups_sum),
                    viewModel = viewModel,
                    prefKey = "separategroups",
                    icon = R.drawable.ic_groups
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.new_context_menu_ui),
                    summary = stringResource(R.string.new_context_menu_ui_sum),
                    viewModel = viewModel,
                    prefKey = "floatingmenu",
                    icon = R.drawable.ic_dashboard_black_24dp
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.novaconfig),
                    summary = stringResource(R.string.novaconfig_sum),
                    viewModel = viewModel,
                    prefKey = "novaconfig",
                    icon = R.drawable.ic_round_settings_24
                )
            }

            item { CategoryHeader("Layout Customization") }
            item {
                SwitchSetting(
                    title = stringResource(R.string.show_chat_broadcast_icon),
                    summary = stringResource(R.string.show_chat_broadcast_icon_sum),
                    viewModel = viewModel,
                    prefKey = "broadcast_tag",
                    icon = R.drawable.eye_enabled
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.show_admin_group_icon),
                    summary = stringResource(R.string.show_admin_group_icon_sum),
                    viewModel = viewModel,
                    prefKey = "admin_grp",
                    icon = R.drawable.admin
                )
            }
            item {
                ListSetting(
                    title = stringResource(R.string.novofiltro),
                    summary = "Home filter style",
                    viewModel = viewModel,
                    prefKey = "chatfilter",
                    entries = context.resources.getStringArray(R.array.chatfilter_buttons),
                    entryValues = context.resources.getStringArray(R.array.chatfilter_values),
                    defaultValue = "2",
                    icon = R.drawable.ic_home_black_24dp
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.igstatus_on_home_screen),
                    summary = stringResource(R.string.igstatus_on_home_screen_sum),
                    viewModel = viewModel,
                    prefKey = "igstatus_on_home_screen",
                    icon = R.drawable.preview_eye
                )
            }

            item { CategoryHeader("Visual Effects") }
            item {
                SwitchSetting(
                    title = stringResource(R.string.menuwicon),
                    summary = stringResource(R.string.menuwicon_sum),
                    viewModel = viewModel,
                    prefKey = "menuwicon",
                    icon = R.drawable.about
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.animation_emojis),
                    summary = stringResource(R.string.animation_emojis_sum),
                    viewModel = viewModel,
                    prefKey = "animation_emojis",
                    defaultValue = true,
                    icon = R.drawable.ic_round_check_circle_24
                )
            }
            item {
                ListSetting(
                    title = stringResource(R.string.list_animations_home_screen),
                    summary = "List animations",
                    viewModel = viewModel,
                    prefKey = "animation_list",
                    entries = context.resources.getStringArray(R.array.animations_names),
                    entryValues = context.resources.getStringArray(R.array.animations_values),
                    defaultValue = "default",
                    icon = R.drawable.ic_dashboard_black_24dp
                )
            }
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
        }
    }
}
