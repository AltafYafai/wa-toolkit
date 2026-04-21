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
fun CallsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Call Settings") },
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
            item { CategoryHeader("Call Recording") }
            item {
                SwitchSetting(
                    title = "Enable Call Recording",
                    summary = "Record all incoming and outgoing voice calls",
                    viewModel = viewModel,
                    prefKey = "call_recording"
                )
            }
            
            item { Spacer(Modifier.height(16.dp)) }
            item { CategoryHeader("Call Control") }
            item {
                SwitchSetting(
                    title = "Call Blocker",
                    summary = "Block specific contacts from calling you",
                    viewModel = viewModel,
                    prefKey = "call_privacy"
                )
            }
            item {
                SwitchSetting(
                    title = "Caller Insights",
                    summary = "Show caller location and technical details",
                    viewModel = viewModel,
                    prefKey = "call_info"
                )
            }
        }
    }
}
