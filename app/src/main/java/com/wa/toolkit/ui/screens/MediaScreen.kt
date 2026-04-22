package com.wa.toolkit.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wa.toolkit.R
import com.wa.toolkit.ui.SettingsViewModel
import com.wa.toolkit.ui.preferences.SwitchSetting
import com.wa.toolkit.ui.preferences.StringSwitchSetting
import com.wa.toolkit.ui.preferences.ListSetting
import com.wa.toolkit.ui.preferences.SliderSetting
import com.wa.toolkit.ui.preferences.CategoryHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.media)) },
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
            item { CategoryHeader("Download & Capture") }
            item {
                SwitchSetting(
                    title = stringResource(R.string.statusdowload),
                    summary = stringResource(R.string.statusdowload_sum),
                    viewModel = viewModel,
                    prefKey = "downloadstatus",
                    icon = R.drawable.download
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.enable_copy_status),
                    summary = stringResource(R.string.enable_copy_status_sum),
                    viewModel = viewModel,
                    prefKey = "copystatus",
                    icon = R.drawable.edit2
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.downloadviewonce),
                    summary = stringResource(R.string.downloadviewonce_sum),
                    viewModel = viewModel,
                    prefKey = "downloadviewonce",
                    icon = R.drawable.download
                )
            }
            item {
                SwitchSetting(
                    title = "Download Profile Picture",
                    summary = "Add a download button when viewing profile pictures",
                    viewModel = viewModel,
                    prefKey = "download_profile",
                    icon = R.drawable.ic_person
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.local_download),
                    summary = "Save downloads to a specific local folder",
                    viewModel = viewModel,
                    prefKey = "download_local",
                    icon = R.drawable.ic_dashboard_black_24dp
                )
            }

            item { CategoryHeader("Image & Video Quality") }
            item {
                SwitchSetting(
                    title = stringResource(R.string.imagequality),
                    summary = stringResource(R.string.imagequality_sum),
                    viewModel = viewModel,
                    prefKey = "imagequality",
                    icon = R.drawable.ic_image
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.videoquality),
                    summary = stringResource(R.string.videoquality_sum),
                    viewModel = viewModel,
                    prefKey = "videoquality",
                    icon = R.drawable.ic_media
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.send_video_in_real_resolution),
                    summary = stringResource(R.string.send_video_in_real_resolution_sum),
                    viewModel = viewModel,
                    prefKey = "video_real_resolution",
                    icon = R.drawable.ic_media
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.send_video_in_60fps),
                    summary = stringResource(R.string.send_video_in_60fps_sum),
                    viewModel = viewModel,
                    prefKey = "video_maxfps",
                    icon = R.drawable.ic_media
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.lossless_status),
                    summary = stringResource(R.string.lossless_status_sum),
                    viewModel = viewModel,
                    prefKey = "lossless_status",
                    icon = R.drawable.eye_enabled
                )
            }
            item {
                ListSetting(
                    title = "Upload Resolution",
                    summary = "Choose default quality for media uploads",
                    viewModel = viewModel,
                    prefKey = "media_quality",
                    entries = context.resources.getStringArray(R.array.media_quality_entries),
                    entryValues = context.resources.getStringArray(R.array.media_quality_values),
                    defaultValue = "0",
                    icon = R.drawable.ic_image
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.unlimited_file_size),
                    summary = stringResource(R.string.unlimited_file_size_sum),
                    viewModel = viewModel,
                    prefKey = "unlimited_file_size",
                    icon = R.drawable.ic_dashboard_black_24dp
                )
            }
            item {
                SliderSetting(
                    title = "Video Size Limit",
                    summary = "MB",
                    viewModel = viewModel,
                    prefKey = "video_limit_size",
                    defaultValue = 40,
                    minValue = 30f,
                    maxValue = 90f,
                    icon = R.drawable.ic_dashboard_black_24dp
                )
            }

            item { CategoryHeader("Voice Notes & Audio") }
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
                StringSwitchSetting(
                    title = stringResource(R.string.send_audio_as_voice_audio_note),
                    summary = stringResource(R.string.send_audio_as_voice_audio_note_sum),
                    viewModel = viewModel,
                    prefKey = "audio_type",
                    trueValue = "2", // Default to Voice Note when toggled
                    icon = R.drawable.ic_recording
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.audio_transcription),
                    summary = stringResource(R.string.audio_transcription_sum),
                    viewModel = viewModel,
                    prefKey = "audio_transcription",
                    icon = R.drawable.edit2
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.voice_changer),
                    summary = stringResource(R.string.voice_changer_sum),
                    viewModel = viewModel,
                    prefKey = "voice_changer",
                    icon = R.drawable.ic_recording
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.disable_the_proximity_sensor),
                    summary = stringResource(R.string.disable_the_proximity_sensor_sum),
                    viewModel = viewModel,
                    prefKey = "disable_sensor_proximity",
                    icon = R.drawable.ic_privacy
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.disable_audio_sensor),
                    summary = stringResource(R.string.disable_audio_sensor_sum),
                    viewModel = viewModel,
                    prefKey = "proximity_audios",
                    icon = R.drawable.ic_privacy
                )
            }

            item { CategoryHeader("Enhanced Experience") }
            item {
                SwitchSetting(
                    title = stringResource(R.string.enable_media_preview),
                    summary = stringResource(R.string.enable_media_preview_sum),
                    viewModel = viewModel,
                    prefKey = "media_preview",
                    defaultValue = true,
                    icon = R.drawable.preview_eye
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.auto_ocr),
                    summary = stringResource(R.string.auto_ocr_sum),
                    viewModel = viewModel,
                    prefKey = "auto_ocr",
                    icon = R.drawable.ic_image
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.status_analytics),
                    summary = stringResource(R.string.status_analytics_sum),
                    viewModel = viewModel,
                    prefKey = "status_analytics",
                    icon = R.drawable.eye_enabled
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.disable_auto_status),
                    summary = stringResource(R.string.disable_auto_status_sum),
                    viewModel = viewModel,
                    prefKey = "autonext_status",
                    icon = R.drawable.ic_pause
                )
            }
            item {
                SwitchSetting(
                    title = stringResource(R.string.toast_on_viewed_status),
                    summary = stringResource(R.string.toast_on_viewed_status_sum),
                    viewModel = viewModel,
                    prefKey = "toast_viewed_status",
                    icon = R.drawable.eye_enabled
                )
            }
        }
    }
}
