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
import com.wa.toolkit.ui.preferences.SwitchSetting
import com.wa.toolkit.ui.preferences.ActionSetting
import com.wa.toolkit.ui.preferences.CategoryHeader

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val content = inputStream?.bufferedReader()?.use { it.readText() }
                if (content != null) {
                    viewModel.setString("bootloader_spoofer_xml", content)
                    Toast.makeText(context, "Keybox XML loaded successfully", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error loading XML: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.tools)) },
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
            item { CategoryHeader("Appearance & Display") }
            item {
                ListSetting(
                    title = stringResource(R.string.theme_mode),
                    summary = stringResource(R.string.theme_mode_sum),
                    viewModel = viewModel,
                    prefKey = "thememode",
                    entries = context.resources.getStringArray(R.array.thememode_entries),
                    entryValues = context.resources.getStringArray(R.array.thememode_values),
                    defaultValue = "0",
                    icon = R.drawable.ic_round_settings_24
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.change_default_dpi),
                    summary = stringResource(R.string.change_default_dpi_sum),
                    viewModel = viewModel,
                    prefKey = "change_dpi",
                    icon = R.drawable.ic_dashboard_black_24dp
                )
            }

            item { CategoryHeader("Time & Formatting") }
            item {
                SwitchSetting(
                    title = stringResource(R.string.ampm),
                    summary = "Use 12-hour format for message timestamps",
                    viewModel = viewModel,
                    prefKey = "ampm",
                    icon = R.drawable.ic_round_settings_24
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.segundosnahora),
                    summary = stringResource(R.string.segundosnahora_sum),
                    viewModel = viewModel,
                    prefKey = "segundos",
                    icon = R.drawable.ic_round_settings_24
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.textonahora),
                    summary = stringResource(R.string.textonahora_sum),
                    viewModel = viewModel,
                    prefKey = "secondstotime",
                    icon = R.drawable.ic_round_settings_24
                )
            }

            item { CategoryHeader("Automation & Integration") }
            item {
                SwitchSetting(
                    title = stringResource(R.string.enable_tasker_automation),
                    summary = stringResource(R.string.enable_tasker_automation_sum),
                    viewModel = viewModel,
                    prefKey = "tasker",
                    icon = R.drawable.ic_round_settings_24
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.bootloader_spoofer),
                    summary = stringResource(R.string.bootloader_spoofer_sum),
                    viewModel = viewModel,
                    prefKey = "bootloader_spoofer",
                    icon = R.drawable.ic_privacy
                )
            }
            item {
                SwitchSetting(
                    title = "Use Custom Keybox",
                    summary = "Use your own bootloader attestation keys",
                    viewModel = viewModel,
                    prefKey = "bootloader_spoofer_custom",
                    icon = R.drawable.ic_round_settings_24
                )
            }
            item {
                ActionSetting(
                    title = "Import Keybox XML",
                    summary = "Select an .xml file containing attestation keys",
                    onClick = { launcher.launch("text/xml") },
                    icon = R.drawable.ic_round_check_circle_24
                )
            }

            item { CategoryHeader("System & Maintenance") }

            item {
                SwitchSetting(
                    title = stringResource(R.string.voice_transcription),
                    summary = stringResource(R.string.voice_transcription_sum),
                    viewModel = viewModel,
                    prefKey = "voice_transcription",
                    icon = R.drawable.ic_recording
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.ai_rewrite),
                    summary = stringResource(R.string.ai_rewrite_sum),
                    viewModel = viewModel,
                    prefKey = "ai_rewrite",
                    icon = R.drawable.ic_privacy
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.group_tldr),
                    summary = stringResource(R.string.group_tldr_sum),
                    viewModel = viewModel,
                    prefKey = "group_tldr",
                    icon = R.drawable.ic_home_black_24dp
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.disable_metaai),
                    summary = stringResource(R.string.disable_metaai_sum),
                    viewModel = viewModel,
                    prefKey = "metaai",
                    icon = R.drawable.ic_round_bug_report_24
                )
            }

            item { CategoryHeader("System & Maintenance") }
            item {
                SwitchSetting(
                    title = stringResource(R.string.update_check),
                    summary = stringResource(R.string.update_check_sum),
                    viewModel = viewModel,
                    prefKey = "update_check",
                    defaultValue = true,
                    icon = R.drawable.ic_round_check_circle_24
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.disable_whatsapp_expiration),
                    summary = stringResource(R.string.disable_whatsapp_expiration_sum),
                    viewModel = viewModel,
                    prefKey = "disable_expiration",
                    icon = R.drawable.ic_round_warning_24
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.disable_version_check),
                    summary = stringResource(R.string.disable_version_check_sum),
                    viewModel = viewModel,
                    prefKey = "bypass_version_check",
                    icon = R.drawable.ic_round_bug_report_24
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.lite_mode),
                    summary = stringResource(R.string.lite_mode_sum),
                    viewModel = viewModel,
                    prefKey = "lite_mode",
                    icon = R.drawable.ic_dashboard_black_24dp
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.verbose_logs),
                    summary = "Log technical details for debugging",
                    viewModel = viewModel,
                    prefKey = "enablelogs",
                    defaultValue = true,
                    icon = R.drawable.ic_round_bug_report_24
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.enable_spy),
                    summary = stringResource(R.string.enable_spy_sum),
                    viewModel = viewModel,
                    prefKey = "enable_spy",
                    icon = R.drawable.ic_round_bug_report_24
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.force_restore_backup),
                    summary = stringResource(R.string.force_restore_backup_summary),
                    viewModel = viewModel,
                    prefKey = "force_restore_backup_feature",
                    icon = R.drawable.ic_round_check_circle_24
                )
            }
        }
    }
}
