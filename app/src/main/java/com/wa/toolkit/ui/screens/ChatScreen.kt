package com.wa.toolkit.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wa.toolkit.ui.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chat Settings") },
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
            item { CategoryHeader("Message Protection") }
            item {
                SwitchSetting(
                    title = "Anti-Disappearing",
                    summary = "Prevent messages from being automatically deleted by timer",
                    viewModel = viewModel,
                    prefKey = "antidisappearing"
                )
            }
            item {
                SwitchSetting(
                    title = "Edited Message History",
                    summary = "Show the history of edited messages",
                    viewModel = viewModel,
                    prefKey = "antieditmessages"
                )
            }

            item { Spacer(Modifier.height(16.dp)) }
            item { CategoryHeader("Safe Interaction") }
            item {
                SwitchSetting(
                    title = "Verify Blocked Contact",
                    summary = "Show alert if you try to message a blocked contact",
                    viewModel = viewModel,
                    prefKey = "verify_blocked_contact"
                )
            }
            item {
                SwitchSetting(
                    title = "Confirm Sticker Send",
                    summary = "Ask for confirmation before sending a sticker",
                    viewModel = viewModel,
                    prefKey = "alertsticker"
                )
            }

            item { Spacer(Modifier.height(16.dp)) }
            item { CategoryHeader("Chat Enhancements") }
            item {
                SwitchSetting(
                    title = "Disable Pin Limit",
                    summary = "Pin more than 3 chats to the top",
                    viewModel = viewModel,
                    prefKey = "pinnedlimit"
                )
            }
            item {
                SwitchSetting(
                    title = "Remove Forward Limit",
                    summary = "Forward messages to more than 5 chats",
                    viewModel = viewModel,
                    prefKey = "removeforwardlimit"
                )
            }
            item {
                SwitchSetting(
                    title = "Hide Forwarded Tag",
                    summary = "Don't show 'Forwarded' label on sent messages",
                    viewModel = viewModel,
                    prefKey = "hidetag"
                )
            }
            item {
                SwitchSetting(
                    title = "Delete for Everyone Always",
                    summary = "Enable 'Delete for Everyone' for all your messages",
                    viewModel = viewModel,
                    prefKey = "revokeallmessages"
                )
            }

            item { Spacer(Modifier.height(16.dp)) }
            item { CategoryHeader("Interactions & Tools") }
            item {
                SwitchSetting(
                    title = "Double-Tap to React",
                    summary = "Double-tap a message to quickly react",
                    viewModel = viewModel,
                    prefKey = "doubletap2like",
                    defaultValue = true
                )
            }
            item {
                SwitchSetting(
                    title = "Google Translate",
                    summary = "Translate messages directly in chat",
                    viewModel = viewModel,
                    prefKey = "google_translate"
                )
            }
            item {
                SwitchSetting(
                    title = "Smart Reply",
                    summary = "AI-powered quick reply suggestions",
                    viewModel = viewModel,
                    prefKey = "smart_reply"
                )
            }
        }
    }
}
