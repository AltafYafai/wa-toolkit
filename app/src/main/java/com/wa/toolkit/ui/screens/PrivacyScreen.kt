package com.wa.toolkit.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wa.toolkit.R
import com.wa.toolkit.ui.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Privacy Settings") },
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
            item { CategoryHeader("Anti-Revoke & Security") }
            item {
                SwitchSetting(
                    title = "Anti-Revoke",
                    summary = "Keep messages even after they are deleted for everyone",
                    viewModel = viewModel,
                    prefKey = "antirevoke"
                )
            }
            item {
                SwitchSetting(
                    title = "Notify on Deletion",
                    summary = "Show a real-time alert when a message is revoked",
                    viewModel = viewModel,
                    prefKey = "toastdeleted"
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.metadata_stripper),
                    summary = stringResource(R.string.metadata_stripper_sum),
                    viewModel = viewModel,
                    prefKey = "metadata_stripper"
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.panic_mode),
                    summary = stringResource(R.string.panic_mode_sum),
                    viewModel = viewModel,
                    prefKey = "panic_mode"
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.disable_secure_flag),
                    summary = stringResource(R.string.disable_secure_flag_sum),
                    viewModel = viewModel,
                    prefKey = "disable_secure_flag"
                )
            }
            
            item { Spacer(Modifier.height(16.dp)) }
            item { CategoryHeader("Presence Control") }
            item {
                SwitchSetting(
                    title = "Freeze Last Seen",
                    summary = "Freeze your last seen timestamp at the current moment",
                    viewModel = viewModel,
                    prefKey = "freezelastseen"
                )
            }
            item {
                SwitchSetting(
                    title = "Always Online",
                    summary = "Show you as online even when WhatsApp is closed",
                    viewModel = viewModel,
                    prefKey = "always_online"
                )
            }

            item { Spacer(Modifier.height(16.dp)) }
            item { CategoryHeader("Interactive Privacy") }
            item {
                SwitchSetting(
                    title = stringResource(R.string.stealth_typing),
                    summary = stringResource(R.string.stealth_typing_sum),
                    viewModel = viewModel,
                    prefKey = "stealth_typing"
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.blueonreply),
                    summary = stringResource(R.string.blueonreply_sum),
                    viewModel = viewModel,
                    prefKey = "blueonreply"
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.local_vault),
                    summary = stringResource(R.string.local_vault_sum),
                    viewModel = viewModel,
                    prefKey = "local_vault"
                )
            }
            item {
                SwitchSetting(
                    title = "Double-Tap to React",
                    summary = "Double-tap a message to react with a custom emoji",
                    viewModel = viewModel,
                    prefKey = "doubletap2like"
                )
            }
            item {
                SwitchSetting(
                    title = "Stealth View Once",
                    summary = "View 'View Once' media unlimited times privately",
                    viewModel = viewModel,
                    prefKey = "hide_once_view_seen"
                )
            }
            item {
                SwitchSetting(
                    title = "Hide Voice Note Seen",
                    summary = "Play voice notes without showing the played receipt",
                    viewModel = viewModel,
                    prefKey = "hide_audio_seen"
                )
            }
        }
    }
}
