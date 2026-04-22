package com.wa.toolkit.ui.screens

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wa.toolkit.R
import com.wa.toolkit.ui.MainViewModel
import com.wa.toolkit.ui.SettingsViewModel
import com.wa.toolkit.utils.HapticUtil
import com.wa.toolkit.activities.AboutActivity
import com.wa.toolkit.App
import com.wa.toolkit.xposed.core.FeatureLoader
import com.wa.toolkit.utils.ConfigUtil
import com.wa.toolkit.ui.preferences.SwitchSetting
import com.wa.toolkit.ui.preferences.CategoryHeader
import android.widget.Toast

@Composable
fun DashboardScreen(
    mainViewModel: MainViewModel,
    onNavigateToCategory: (Int) -> Unit,
    onNavigateToSearch: () -> Unit
) {
    val context = LocalContext.current
    val wppVersion by mainViewModel.wppVersion.collectAsState()
    val isWppActive by mainViewModel.isWppActive.collectAsState()

    val items = listOf(
        DashboardItem(1, stringResource(R.string.privacy), "Stealth, Ghost mode and Anti-Revoke", R.drawable.ic_privacy),
        DashboardItem(3, stringResource(R.string.media), "HD Quality, Call Recording and Auto-OCR", R.drawable.ic_media),
        DashboardItem(2, stringResource(R.string.chat), "AI Translation, Summaries and Enhancements", R.drawable.ic_telegram),
        DashboardItem(8, "Interface", "Separate Groups, Tabs and Theme customization", R.drawable.ic_home_black_24dp),
        DashboardItem(5, "System", "Bootloader Spoofer, Updates and Maintenance", R.drawable.ic_general),
        DashboardItem(6, stringResource(R.string.status), "IG Style Status, Stealth and Downloads", R.drawable.online),
        DashboardItem(7, stringResource(R.string.calls), "Call Blocker and Additional Insights", R.drawable.ic_contacts)
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // Header
        item(span = { GridItemSpan(2) }) {
            DashboardHeader(
                wppVersion = wppVersion,
                isWppActive = isWppActive,
                onSearchClick = {
                    HapticUtil.playClick(context)
                    onNavigateToSearch()
                },
                onBackupClick = { HapticUtil.playClick(context); ConfigUtil.exportConfigs(context) },
                onRestoreClick = { HapticUtil.playClick(context); ConfigUtil.importConfigs(context) },
                onRestartClick = {
                    HapticUtil.playClick(context)
                    App.getInstance().restartApp(FeatureLoader.PACKAGE_WPP)
                    App.getInstance().restartApp(FeatureLoader.PACKAGE_BUSINESS)
                }
            )
        }

        // Grid Items
        items(items) { item ->
            DashboardCard(item = item) {
                HapticUtil.playClick(context)
                onNavigateToCategory(item.id)
            }
        }

        // About Card
        item(span = { GridItemSpan(2) }) {
            AboutCard {
                HapticUtil.playClick(context)
                context.startActivity(Intent(context, AboutActivity::class.java))
            }
        }
    }
}

@Composable
fun DashboardHeader(
    wppVersion: String,
    isWppActive: Boolean,
    onSearchClick: () -> Unit,
    onBackupClick: () -> Unit,
    onRestoreClick: () -> Unit,
    onRestartClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Search Bar (Simple version)
        Surface(
            onClick = onSearchClick,
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Search, contentDescription = null)
                Spacer(Modifier.width(16.dp))
                Text("Search settings...", style = MaterialTheme.typography.bodyLarge)
            }
        }

        // Status Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val isXposedEnabled = com.wa.toolkit.MainActivity.isXposedEnabled()
            StatusCard(
                title = "LSPosed Status",
                subtitle = if (isXposedEnabled) "Active" else "Inactive",
                icon = if (isXposedEnabled) R.drawable.ic_round_check_circle_24 else R.drawable.ic_round_error_outline_24,
                iconTint = if (isXposedEnabled) Color(0xFF4CAF50) else Color(0xFFF44336),
                modifier = Modifier.weight(1f)
            )
            StatusCard(
                title = "WhatsApp",
                subtitle = wppVersion,
                icon = if (isWppActive) R.drawable.ic_round_check_circle_24 else R.drawable.ic_round_error_outline_24,
                iconTint = if (isWppActive) MaterialTheme.colorScheme.primary else Color(0xFFF44336),
                modifier = Modifier.weight(1f)
            )
        }

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ActionButton(text = "Backup", onClick = onBackupClick, modifier = Modifier.weight(1f))
            ActionButton(text = "Restore", onClick = onRestoreClick, modifier = Modifier.weight(1f))
            ActionButton(text = "Restart", onClick = onRestartClick, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun StatusCard(
    title: String,
    subtitle: String,
    icon: Int,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    ElevatedCard(modifier = modifier) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(8.dp))
            Column {
                Text(title, style = MaterialTheme.typography.labelMedium)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, maxLines = 1)
            }
        }
    }
}

@Composable
fun ActionButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 8.dp)
    ) {
        Text(text, fontSize = 12.sp)
    }
}

@Composable
fun DashboardCard(item: DashboardItem, onClick: () -> Unit) {
    OutlinedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(item.icon),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                item.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                item.summary,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun AboutCard(onClick: () -> Unit) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_privacy), // Placeholder icon
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary
            )
            Spacer(Modifier.width(16.dp))
            Column {
                Text("About Whatsapp Toolkit", style = MaterialTheme.typography.titleMedium)
                Text("Emerald Hub v1.0.0-beta", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
