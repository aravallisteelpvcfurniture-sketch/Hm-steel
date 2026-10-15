package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppTab
import com.example.ui.MainViewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settings by viewModel.settings.collectAsState()
            val currentTab by viewModel.currentTab.collectAsState()
            val visitors by viewModel.visitors.collectAsState()
            val estimates by viewModel.estimates.collectAsState()

            // System back handler: if not on Dashboard, back returns to Dashboard
            BackHandler(enabled = currentTab != AppTab.DASHBOARD) {
                viewModel.selectTab(AppTab.DASHBOARD)
            }

            MyApplicationTheme(
                darkTheme = settings.isDarkMode,
                dynamicColor = settings.useDynamicColors
            ) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = {
                                Column {
                                    Text(
                                        text = "MarketScope",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    )
                                    Text(
                                        text = currentTab.title,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            actions = {
                                if (currentTab != AppTab.GALLERY) {
                                    IconButton(
                                        onClick = { viewModel.selectTab(AppTab.GALLERY) },
                                        modifier = Modifier.testTag("top_gallery_btn")
                                    ) {
                                        Icon(Icons.Default.Collections, contentDescription = "Gallery")
                                    }
                                }
                                if (currentTab != AppTab.CATALOG) {
                                    IconButton(
                                        onClick = { viewModel.selectTab(AppTab.CATALOG) },
                                        modifier = Modifier.testTag("top_catalog_btn")
                                    ) {
                                        Icon(Icons.Default.Category, contentDescription = "Catalog")
                                    }
                                }
                                if (currentTab != AppTab.SETTINGS) {
                                    IconButton(
                                        onClick = { viewModel.selectTab(AppTab.SETTINGS) },
                                        modifier = Modifier.testTag("top_settings_btn")
                                    ) {
                                        Icon(Icons.Default.Menu, contentDescription = "Menu & Settings")
                                    }
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            modifier = Modifier.testTag("bottom_nav_bar")
                        ) {
                            NavigationBarItem(
                                selected = currentTab == AppTab.DASHBOARD,
                                onClick = { viewModel.selectTab(AppTab.DASHBOARD) },
                                icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                                label = { Text("Overview", fontSize = 11.sp) },
                                modifier = Modifier.testTag("nav_tab_dashboard")
                            )

                            NavigationBarItem(
                                selected = currentTab == AppTab.VISITORS,
                                onClick = { viewModel.selectTab(AppTab.VISITORS) },
                                icon = {
                                    BadgedBox(
                                        badge = {
                                            if (visitors.isNotEmpty()) {
                                                Badge { Text("${visitors.size}") }
                                            }
                                        }
                                    ) {
                                        Icon(Icons.Default.People, contentDescription = "Visitors")
                                    }
                                },
                                label = { Text("Visitors", fontSize = 11.sp) },
                                modifier = Modifier.testTag("nav_tab_visitors")
                            )

                            NavigationBarItem(
                                selected = currentTab == AppTab.MEASUREMENTS,
                                onClick = { viewModel.selectTab(AppTab.MEASUREMENTS) },
                                icon = { Icon(Icons.Default.Straighten, contentDescription = "Measurements") },
                                label = { Text("Measure", fontSize = 11.sp) },
                                modifier = Modifier.testTag("nav_tab_measurements")
                            )

                            NavigationBarItem(
                                selected = currentTab == AppTab.ESTIMATES,
                                onClick = { viewModel.selectTab(AppTab.ESTIMATES) },
                                icon = {
                                    BadgedBox(
                                        badge = {
                                            if (estimates.isNotEmpty()) {
                                                Badge { Text("${estimates.size}") }
                                            }
                                        }
                                    ) {
                                        Icon(Icons.Default.ReceiptLong, contentDescription = "Estimates")
                                    }
                                },
                                label = { Text("Estimates", fontSize = 11.sp) },
                                modifier = Modifier.testTag("nav_tab_estimates")
                            )

                            NavigationBarItem(
                                selected = currentTab == AppTab.SETTINGS || currentTab == AppTab.CATALOG || currentTab == AppTab.GALLERY,
                                onClick = { viewModel.selectTab(AppTab.SETTINGS) },
                                icon = { Icon(Icons.Default.Menu, contentDescription = "Menu & Settings") },
                                label = { Text("Menu", fontSize = 11.sp) },
                                modifier = Modifier.testTag("nav_tab_menu")
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (currentTab) {
                            AppTab.DASHBOARD -> DashboardScreen(
                                viewModel = viewModel,
                                onNavigate = { tab -> viewModel.selectTab(tab) }
                            )
                            AppTab.VISITORS -> VisitorsScreen(
                                viewModel = viewModel
                            )
                            AppTab.MEASUREMENTS -> MeasurementScreen(
                                viewModel = viewModel
                            )
                            AppTab.ESTIMATES -> EstimateScreen(
                                viewModel = viewModel
                            )
                            AppTab.CATALOG -> CatalogScreen(
                                viewModel = viewModel,
                                onNavigate = { tab -> viewModel.selectTab(tab) }
                            )
                            AppTab.GALLERY -> GalleryScreen(
                                viewModel = viewModel
                            )
                            AppTab.SETTINGS -> MenuSettingsScreen(
                                viewModel = viewModel,
                                onNavigate = { tab -> viewModel.selectTab(tab) }
                            )
                        }
                    }
                }
            }
        }
    }
}
