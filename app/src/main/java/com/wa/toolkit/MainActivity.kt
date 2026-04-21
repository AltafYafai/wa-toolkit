package com.wa.toolkit

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
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
import com.wa.toolkit.ui.screens.DashboardScreen
import com.wa.toolkit.ui.screens.PrivacyScreen
import com.wa.toolkit.ui.screens.MediaScreen
import com.wa.toolkit.ui.screens.SearchScreen
import com.wa.toolkit.ui.screens.StatusScreen
import com.wa.toolkit.ui.screens.CallsScreen
import com.wa.toolkit.ui.screens.ToolsScreen
import com.wa.toolkit.ui.screens.CustomizationScreen
import com.wa.toolkit.ui.theme.AppTheme
import com.wa.toolkit.model.SearchableFeature

class MainActivity : ComponentActivity() {

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
    private var statusReceiver: BroadcastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setupStatusReceiver()
        checkWpp()

        setContent {
            AppTheme {
                MainScreen(viewModel, settingsViewModel, searchViewModel)
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
        sendBroadcast(checkWpp)
    }

    override fun onDestroy() {
        statusReceiver?.let { unregisterReceiver(it) }
        super.onDestroy()
    }

    companion object {
        @JvmStatic
        fun isXposedEnabled(): Boolean = false
    }
}

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    settingsViewModel: SettingsViewModel,
    searchViewModel: SearchViewModel
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
                            SearchableFeature.FragmentType.GENERAL_CONVERSATION -> navController.navigate("tools")
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
                    onNavigateToThemeManager = { /* TODO: Navigate to Theme Manager */ },
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
