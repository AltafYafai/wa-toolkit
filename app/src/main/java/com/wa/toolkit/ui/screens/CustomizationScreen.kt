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
fun CustomizationScreen(
    viewModel: SettingsViewModel,
    onNavigateToThemeManager: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Customization") },
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
            item { CategoryHeader("Theme & Colors") }
            item {
                SwitchSetting(
                    title = "Pure Black Mode",
                    summary = "Use absolute black for OLED screens",
                    viewModel = viewModel,
                    prefKey = "pure_black"
                )
            }
            item {
                SwitchSetting(
                    title = "Haptic Feedback",
                    summary = "Vibrate on UI interactions",
                    viewModel = viewModel,
                    prefKey = "haptic_feedback"
                )
            }
            item {
                Surface(
                    onClick = onNavigateToThemeManager,
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                        Text("Theme Manager", style = MaterialTheme.typography.titleMedium)
                        Text("Manage and edit custom CSS themes", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
            item { CategoryHeader("Home Screen") }
            item {
                SwitchSetting(
                    title = "Home Wallpaper",
                    summary = "Show a custom wallpaper behind chats",
                    viewModel = viewModel,
                    prefKey = "wallpaper"
                )
            }
            item {
                SwitchSetting(
                    title = "Instagram Status",
                    summary = "Show status circles like Instagram",
                    viewModel = viewModel,
                    prefKey = "igstatus"
                )
            }

            item { Spacer(Modifier.height(16.dp)) }
            item { CategoryHeader("Conversation") }
            item {
                SwitchSetting(
                    title = "Admin Icon",
                    summary = "Show special icon for group admins",
                    viewModel = viewModel,
                    prefKey = "admin_grp"
                )
            }
            item {
                SwitchSetting(
                    title = "Custom Bubble Colors",
                    summary = "Change the colors of chat bubbles",
                    viewModel = viewModel,
                    prefKey = "bubble_color"
                )
            }
        }
    }
}
