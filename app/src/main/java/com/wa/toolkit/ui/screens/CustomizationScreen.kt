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
    onNavigateToThemeManager: () -> Unit,
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
            item { CategoryHeader("Theme Management") }
            item {
                ActionSetting(
                    title = "Theme Manager",
                    summary = "Import, export and create custom color themes",
                    onClick = onNavigateToThemeManager,
                    icon = R.drawable.ic_round_settings_24
                )
            }

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

            item { CategoryHeader("Toolbar & Navigation") }
            item {
                SwitchSetting(
                    title = stringResource(R.string.show_menu_buttons_as_icons),
                    summary = stringResource(R.string.show_menu_buttons_as_icons_sum),
                    viewModel = viewModel,
                    prefKey = "buttonaction",
                    icon = R.drawable.ic_round_settings_24
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.showname),
                    summary = stringResource(R.string.showname_sum),
                    viewModel = viewModel,
                    prefKey = "shownamehome",
                    icon = R.drawable.ic_person
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.showbio),
                    summary = stringResource(R.string.showbio_sum),
                    viewModel = viewModel,
                    prefKey = "showbiohome",
                    icon = R.drawable.ic_person
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.show_dnd_button),
                    summary = stringResource(R.string.show_dnd_button_sum),
                    viewModel = viewModel,
                    prefKey = "show_dndmode",
                    icon = R.drawable.ic_round_warning_24
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.enable_new_chat_button),
                    summary = stringResource(R.string.enable_new_chat_button_sum),
                    viewModel = viewModel,
                    prefKey = "newchat",
                    icon = R.drawable.ic_contacts
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.enable_restart_button),
                    summary = stringResource(R.string.enable_restart_button_sum),
                    viewModel = viewModel,
                    prefKey = "restartbutton",
                    icon = R.drawable.ic_round_settings_24
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.enable_wa_enhancer_button),
                    summary = stringResource(R.string.enable_wa_enhancer_button_sum),
                    viewModel = viewModel,
                    prefKey = "open_wae",
                    icon = R.drawable.ic_round_settings_24
                )
            }

            item { CategoryHeader("Conversation List") }
            item {
                SwitchSetting(
                    title = stringResource(R.string.show_online_dot_in_conversation_list),
                    summary = stringResource(R.string.show_online_dot_in_conversation_list_sum),
                    viewModel = viewModel,
                    prefKey = "dotonline",
                    icon = R.drawable.online
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.show_online_last_seen_in_conversation_list),
                    summary = stringResource(R.string.show_online_last_seen_in_conversation_list_sum),
                    viewModel = viewModel,
                    prefKey = "showonlinetext",
                    icon = R.drawable.online
                )
            }
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
            item {
                SwitchSetting(
                    title = stringResource(R.string.enable_facebook_style_for_status),
                    summary = stringResource(R.string.enable_facebook_style_for_status_sum),
                    viewModel = viewModel,
                    prefKey = "facebook_style_status",
                    icon = R.drawable.preview_eye
                )
            }

            item { CategoryHeader("Message Bubbles") }
            item {
                SwitchSetting(
                    title = stringResource(R.string.custom_bubble_color),
                    summary = stringResource(R.string.custom_bubble_color_sum),
                    viewModel = viewModel,
                    prefKey = "bubble_color",
                    icon = R.drawable.ic_round_check_circle_24
                )
            }
            item {
                ActionSetting(
                    title = stringResource(R.string.bubble_left),
                    summary = "Pick color for received messages",
                    onClick = { /* Color picker not implemented in compose yet */ },
                    icon = R.drawable.edit2
                )
            }
            item {
                ActionSetting(
                    title = stringResource(R.string.bubble_right),
                    summary = "Pick color for sent messages",
                    onClick = { /* Color picker not implemented in compose yet */ },
                    icon = R.drawable.edit2
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
        }
    }
}
