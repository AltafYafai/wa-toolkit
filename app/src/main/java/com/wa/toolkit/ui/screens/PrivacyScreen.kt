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
import androidx.compose.ui.unit.dp
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

@Composable
fun CategoryHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun SwitchSetting(
    title: String,
    summary: String,
    viewModel: SettingsViewModel,
    prefKey: String,
    defaultValue: Boolean = false
) {
    val checked by viewModel.getBoolean(prefKey, defaultValue).collectAsState()

    Surface(
        onClick = { viewModel.toggleBoolean(prefKey, checked) },
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(summary, style = MaterialTheme.typography.bodySmall)
            }
            Switch(
                checked = checked,
                onCheckedChange = { viewModel.toggleBoolean(prefKey, checked) }
            )
        }
    }
}
