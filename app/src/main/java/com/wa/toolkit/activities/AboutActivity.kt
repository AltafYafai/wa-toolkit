package com.wa.toolkit.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wa.toolkit.BuildConfig
import com.wa.toolkit.R
import com.wa.toolkit.UpdateChecker
import com.wa.toolkit.ui.theme.AppTheme
import de.robv.android.xposed.XposedBridge

class AboutActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                AboutScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About") },
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Icon(
                    painter = painterResource(R.drawable.ic_privacy), // Placeholder logo
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(100.dp)
                )
            }
            
            item {
                Text(
                    text = "Whatsapp Toolkit",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Version ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { openUrl(context, "https://t.me/TheEmeraldHub") }) {
                        Text("Telegram")
                    }
                    Button(onClick = { openUrl(context, "https://github.com/altafyafai7/whatsapp-toolkit") }) {
                        Text("GitHub")
                    }
                }
            }

            item {
                OutlinedButton(
                    onClick = { openUrl(context, "https://coindrop.to/suvojeet_sengupta") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Support Development (Donate)")
                }
            }

            item {
                Button(
                    onClick = { 
                        val activity = context as? android.app.Activity
                        if (activity != null) {
                            Toast.makeText(context, "Checking for updates...", Toast.LENGTH_SHORT).show()
                            Thread(UpdateChecker(activity)).start()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Check for Updates")
                }
            }
            
            item {
                Text(
                    text = "Developed by AltafYafai",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(top = 32.dp)
                )
            }
        }
    }
}

private fun openUrl(context: android.content.Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    } catch (e: Exception) {
        XposedBridge.log("AboutActivity: Failed to open URL: $url")
        Toast.makeText(context, "Could not open link", Toast.LENGTH_SHORT).show()
    }
}
