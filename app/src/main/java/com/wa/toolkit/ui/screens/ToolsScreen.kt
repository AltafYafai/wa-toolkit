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
fun ToolsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tools & UI Settings") },
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
            item { CategoryHeader("Interface & Layout") }
            item {
                SwitchSetting(
                    title = "Floating Home Menu",
                    summary = "Modern centered action menu",
                    viewModel = viewModel,
                    prefKey = "floatingmenu"
                )
            }
            item {
                SwitchSetting(
                    title = "Experimental Settings UI",
                    summary = "Redesigned settings navigation",
                    viewModel = viewModel,
                    prefKey = "novaconfig"
                )
            }

            item { Spacer(Modifier.height(16.dp)) }
            item { CategoryHeader("Visual Enhancements") }
            item {
                SwitchSetting(
                    title = "Menu Icons",
                    summary = "Show icons next to menu items",
                    viewModel = viewModel,
                    prefKey = "menuwicon"
                )
            }
            item {
                SwitchSetting(
                    title = "Animated Emojis",
                    summary = "Smooth animated emoji interactions",
                    viewModel = viewModel,
                    prefKey = "animation_emojis"
                )
            }

            item { Spacer(Modifier.height(16.dp)) }
            item { CategoryHeader("Intelligence") }
            item {
                SwitchSetting(
                    title = "Disable Meta AI",
                    summary = "Completely hide Meta AI integration",
                    viewModel = viewModel,
                    prefKey = "metaai"
                )
            }
            item {
                SwitchSetting(
                    title = "Audio Transcription",
                    summary = "Enable native voice note transcripts",
                    viewModel = viewModel,
                    prefKey = "audio_transcription"
                )
            }
        }
    }
}
