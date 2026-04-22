package com.wa.toolkit

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.wa.toolkit.ui.MainViewModel
import com.wa.toolkit.ui.SettingsViewModel
import com.wa.toolkit.ui.SearchViewModel
import com.wa.toolkit.ui.ThemeViewModel
import com.wa.toolkit.ui.screens.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.wa.toolkit.ui.theme.AppTheme
import com.wa.toolkit.model.SearchableFeature
import com.wa.toolkit.utils.FilePicker

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return SettingsViewModel((application as App).preferenceRepository) as T
            }
        }
    }
    private val searchViewModel: SearchViewModel by viewModels()
    private val themeViewModel: ThemeViewModel by viewModels()
    private var statusReceiver: BroadcastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        FilePicker.registerFilePicker(this)
        
        setupStatusReceiver()
        checkWpp()

        setContent {
            val colorMode by settingsViewModel.colorMode.collectAsState()
            val colorPreset by settingsViewModel.colorPreset.collectAsState()

            AppTheme(
                colorMode = colorMode,
                colorPreset = colorPreset
            ) {
                MainScreen(viewModel, settingsViewModel, searchViewModel, themeViewModel)
            }
        }
    }

    private fun setupStatusReceiver() {
        val intentFilter = IntentFilter("${BuildConfig.APPLICATION_ID}.RECEIVER_WPP")
        statusReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val version = intent.getStringExtra("VERSION")
                viewModel.updateStatus(version, true)
            }
        }
        ContextCompat.registerReceiver(
            this,
            statusReceiver!!,
            intentFilter,
            ContextCompat.RECEIVER_EXPORTED
        )
    }

    private fun checkWpp() {
        val checkWpp = Intent("${BuildConfig.APPLICATION_ID}.CHECK_WPP")
        // Send to standard WhatsApp
        sendBroadcast(checkWpp.apply { setPackage("com.whatsapp") })
        // Send to WhatsApp Business
        sendBroadcast(checkWpp.apply { setPackage("com.whatsapp.w4b") })
    }

    override fun onDestroy() {
        statusReceiver?.let { unregisterReceiver(it) }
        super.onDestroy()
    }

    companion object {
        @androidx.annotation.Keep
        @JvmStatic
        fun isXposedEnabled(): Boolean {
            android.util.Log.d("WAE", "isXposedEnabled called")
            return false
        }
    }
}

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    settingsViewModel: SettingsViewModel,
    searchViewModel: SearchViewModel,
    themeViewModel: ThemeViewModel
) {
    val navController = rememberNavController()
    
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("dashboard") {
                DashboardScreen(
                    mainViewModel = viewModel,
                    onNavigateToCategory = { id ->
                        when (id) {
                            1 -> navController.navigate("privacy")
                            2 -> navController.navigate("chat")
                            3 -> navController.navigate("media")
                            5 -> navController.navigate("tools")
                            6 -> navController.navigate("status")
                            7 -> navController.navigate("calls")
                            8 -> navController.navigate("customization")
                            else -> {}
                        }
                    },
                    onNavigateToSearch = { navController.navigate("search") }
                )
            }
            composable("chat") {
                ChatScreen(
                    viewModel = settingsViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("search") {
                SearchScreen(
                    viewModel = searchViewModel,
                    onNavigateToFeature = { feature ->
                        when (feature.fragmentType) {
                            SearchableFeature.FragmentType.PRIVACY -> navController.navigate("privacy")
                            SearchableFeature.FragmentType.MEDIA -> navController.navigate("media")
                            SearchableFeature.FragmentType.STATUS -> navController.navigate("status")
                            SearchableFeature.FragmentType.CALLS -> navController.navigate("calls")
                            SearchableFeature.FragmentType.CUSTOMIZATION -> navController.navigate("customization")
                            SearchableFeature.FragmentType.GENERAL_HOME -> navController.navigate("tools")
                            SearchableFeature.FragmentType.GENERAL_HOMESCREEN -> navController.navigate("tools")
                            SearchableFeature.FragmentType.GENERAL_CONVERSATION -> navController.navigate("chat")
                            else -> navController.navigate("dashboard")
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable("privacy") {
                PrivacyScreen(
                    viewModel = settingsViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("media") {
                MediaScreen(
                    viewModel = settingsViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("status") {
                StatusScreen(
                    viewModel = settingsViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("calls") {
                CallsScreen(
                    viewModel = settingsViewModel,
                    onNavigateToRecordings = { navController.navigate("recordings") },
                    onBack = { navController.popBackStack() }
                )
            }
            composable("recordings") {
                RecordingsScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            composable("tools") {
                ToolsScreen(
                    viewModel = settingsViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("customization") {
                CustomizationScreen(
                    viewModel = settingsViewModel,
                    onNavigateToThemeManager = { navController.navigate("themes") },
                    onBack = { navController.popBackStack() }
                )
            }
            composable("themes") {
                ThemeScreen(
                    viewModel = themeViewModel,
                    onEditTheme = { themeName -> navController.navigate("theme_editor/$themeName") },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                "theme_editor/{themeName}",
                arguments = listOf(androidx.navigation.navArgument("themeName") { type = androidx.navigation.NavType.StringType })
            ) { backStackEntry ->
                val themeName = backStackEntry.arguments?.getString("themeName") ?: ""
                ThemeEditorScreen(
                    themeName = themeName,
                    viewModel = themeViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
