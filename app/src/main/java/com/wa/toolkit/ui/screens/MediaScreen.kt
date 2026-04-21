package com.wa.toolkit.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
fun MediaScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Media Settings") },
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
            item { CategoryHeader("Media Download") }
            item {
                SwitchSetting(
                    title = "Save View Once",
                    summary = "Automatically save 'View Once' media to gallery",
                    viewModel = viewModel,
                    prefKey = "downloadviewonce"
                )
            }
            item {
                SwitchSetting(
                    title = "Profile Picture Downloader",
                    summary = "Enable downloading of contact profile pictures",
                    viewModel = viewModel,
                    prefKey = "download_profile"
                )
            }

            item { Spacer(Modifier.height(16.dp)) }
            item { CategoryHeader("Quality & Size") }
            item {
                SwitchSetting(
                    title = "Unlimited File Size",
                    summary = "Remove restrictions on outgoing file sizes",
                    viewModel = viewModel,
                    prefKey = "unlimited_file_size"
                )
            }
            
            item {
                SliderSetting(
                    title = "Video Size Limit (MB)",
                    viewModel = viewModel,
                    prefKey = "video_limit_size",
                    range = 30f..90f,
                    defaultValue = 40
                )
            }
        }
    }
}

@Composable
fun SliderSetting(
    title: String,
    viewModel: SettingsViewModel,
    prefKey: String,
    range: ClosedFloatingPointRange<Float>,
    defaultValue: Int
) {
    val value by viewModel.getInt(prefKey, defaultValue).collectAsState(initial = defaultValue)

    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text("${value}MB", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            }
            Slider(
                value = value.toFloat(),
                onValueChange = { viewModel.setInt(prefKey, it.toInt()) },
                valueRange = range,
                steps = ((range.endInclusive - range.start) / 10).toInt() - 1
            )
        }
    }
}
