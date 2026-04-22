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
import com.wa.toolkit.ui.preferences.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallsScreen(
    viewModel: SettingsViewModel,
    onNavigateToRecordings: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.calls)) },
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
            item { CategoryHeader("Call Recording") }
            item {
                SwitchSetting(
                    title = stringResource(R.string.call_recording_enable),
                    summary = stringResource(R.string.call_recording_enable_sum),
                    viewModel = viewModel,
                    prefKey = "call_recording_enable",
                    icon = R.drawable.ic_recording
                )
            }
            item {
                ActionSetting(
                    title = "Manage Recordings",
                    summary = "Browse and play your recorded conversations",
                    onClick = onNavigateToRecordings,
                    icon = R.drawable.ic_play
                )
            }

            item { CategoryHeader("Call Control") }
            item {
                SwitchSetting(
                    title = stringResource(R.string.call_blocker),
                    summary = stringResource(R.string.call_blocker_sum),
                    viewModel = viewModel,
                    prefKey = "call_privacy",
                    icon = R.drawable.eye_disabled
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.additional_call_information),
                    summary = stringResource(R.string.additional_call_information_sum),
                    viewModel = viewModel,
                    prefKey = "call_info",
                    icon = R.drawable.ic_round_warning_24
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.selection_of_call_type),
                    summary = stringResource(R.string.selection_of_call_type_sum),
                    viewModel = viewModel,
                    prefKey = "calltype",
                    icon = R.drawable.ic_contacts
                )
            }
        }
    }
}
