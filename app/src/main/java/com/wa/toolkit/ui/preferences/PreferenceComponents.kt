package com.wa.toolkit.ui.preferences

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wa.toolkit.ui.SettingsViewModel

@Composable
fun CategoryHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
        fontWeight = FontWeight.Bold
    )
}

@Composable
fun SwitchSetting(
    title: String,
    summary: String,
    viewModel: SettingsViewModel,
    prefKey: String,
    defaultValue: Boolean = false,
    icon: Int? = null
) {
    val checked by viewModel.getBoolean(prefKey, defaultValue).collectAsState(initial = defaultValue)

    ElevatedCard(
        onClick = { viewModel.toggleBoolean(prefKey, checked) },
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(16.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(summary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(
                checked = checked,
                onCheckedChange = { viewModel.toggleBoolean(prefKey, checked) }
            )
        }
    }
}

@Composable
fun ActionSetting(
    title: String,
    summary: String,
    onClick: () -> Unit,
    icon: Int? = null
) {
    ElevatedCard(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(16.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(summary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun ListSetting(
    title: String,
    summary: String,
    viewModel: SettingsViewModel,
    prefKey: String,
    entries: Array<String>,
    entryValues: Array<String>,
    defaultValue: String,
    icon: Int? = null
) {
    val currentValue by viewModel.getString(prefKey, defaultValue).collectAsState(initial = defaultValue)
    var showDialog by remember { mutableStateOf(false) }

    ElevatedCard(
        onClick = { showDialog = true },
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(16.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                val displayValue = entries.getOrNull(entryValues.indexOf(currentValue)) ?: currentValue
                Text(displayValue, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(title) },
            text = {
                Column {
                    entries.forEachIndexed { index, entry ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setString(prefKey, entryValues[index])
                                    showDialog = false
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = currentValue == entryValues[index],
                                onClick = {
                                    viewModel.setString(prefKey, entryValues[index])
                                    showDialog = false
                                }
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(entry)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SliderSetting(
    title: String,
    summary: String,
    viewModel: SettingsViewModel,
    prefKey: String,
    defaultValue: Int,
    minValue: Float,
    maxValue: Float,
    icon: Int? = null
) {
    val currentValue by viewModel.getInt(prefKey, defaultValue).collectAsState(initial = defaultValue)
    
    ElevatedCard(
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) {
                    Icon(
                        painter = painterResource(icon),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(16.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleMedium)
                    Text("$currentValue $summary", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Slider(
                value = currentValue.toFloat(),
                onValueChange = { viewModel.setInt(prefKey, it.toInt()) },
                valueRange = minValue..maxValue,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
