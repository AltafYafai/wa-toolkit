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
fun StatusScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Status Settings") },
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
            item { CategoryHeader("Status Interaction") }
            item {
                SwitchSetting(
                    title = "Download Status",
                    summary = "Save any status directly to your local gallery",
                    viewModel = viewModel,
                    prefKey = "downloadstatus"
                )
            }
            item {
                SwitchSetting(
                    title = "Copy Status Caption",
                    summary = "One-tap to copy status text to clipboard",
                    viewModel = viewModel,
                    prefKey = "copystatus"
                )
            }
            item {
                SwitchSetting(
                    title = "Stealth Status View",
                    summary = "View statuses without sending a seen receipt",
                    viewModel = viewModel,
                    prefKey = "statusseen"
                )
            }

            item { Spacer(Modifier.height(16.dp)) }
            item { CategoryHeader("Status Playback") }
            item {
                SwitchSetting(
                    title = "Disable Auto-Next",
                    summary = "Disable automatic transition to the next story",
                    viewModel = viewModel,
                    prefKey = "autonext_status"
                )
            }
        }
    }
}
