package com.ridevision.app.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.HistoryToggleOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ridevision.app.ui.components.HeaderBar
import com.ridevision.app.ui.screens.ComplaintHistoryScreen
import com.ridevision.app.ui.screens.ReportPotholeScreen
import com.ridevision.app.ui.screens.SafeRouteScreen
import com.ridevision.app.ui.screens.UserProfileScreen
import com.ridevision.app.ui.theme.EmeraldBackground
import com.ridevision.app.ui.theme.EmeraldSurfaceLowest
import com.ridevision.app.ui.theme.MintSecondaryContainer
import com.ridevision.app.ui.theme.OnGoldPrimary
import com.ridevision.app.ui.theme.OutlineColor
import com.ridevision.app.ui.theme.RadiantGoldPrimary
import com.ridevision.app.ui.theme.TextOnSurfaceVariant
import com.ridevision.app.ui.viewmodel.AppTab
import com.ridevision.app.ui.viewmodel.RideVisionViewModel

@Composable
fun RideVisionApp(
    viewModel: RideVisionViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentTab by viewModel.currentTab.collectAsState()

    // Listen for feedback toast events
    LaunchedEffect(Unit) {
        viewModel.userFeedback.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        containerColor = EmeraldBackground,
        topBar = {
            HeaderBar(
                onProfileClick = { viewModel.setTab(AppTab.PROFILE) },
                modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = EmeraldSurfaceLowest.copy(alpha = 0.95f),
                tonalElevation = 8.dp,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                // Tab 1: Report
                NavigationBarItem(
                    selected = currentTab == AppTab.REPORT,
                    onClick = { viewModel.setTab(AppTab.REPORT) },
                    icon = { Icon(Icons.Default.PhotoCamera, contentDescription = "Report") },
                    label = { Text("Report", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = OnGoldPrimary,
                        selectedTextColor = RadiantGoldPrimary,
                        indicatorColor = RadiantGoldPrimary,
                        unselectedIconColor = TextOnSurfaceVariant,
                        unselectedTextColor = TextOnSurfaceVariant
                    )
                )

                // Tab 2: Safe Route
                NavigationBarItem(
                    selected = currentTab == AppTab.SAFE_ROUTE,
                    onClick = { viewModel.setTab(AppTab.SAFE_ROUTE) },
                    icon = { Icon(Icons.Default.Route, contentDescription = "Safe Route") },
                    label = { Text("Safe Route", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = OnGoldPrimary,
                        selectedTextColor = RadiantGoldPrimary,
                        indicatorColor = RadiantGoldPrimary,
                        unselectedIconColor = TextOnSurfaceVariant,
                        unselectedTextColor = TextOnSurfaceVariant
                    )
                )

                // Tab 3: History
                NavigationBarItem(
                    selected = currentTab == AppTab.HISTORY,
                    onClick = { viewModel.setTab(AppTab.HISTORY) },
                    icon = { Icon(Icons.Default.HistoryToggleOff, contentDescription = "History") },
                    label = { Text("History", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = OnGoldPrimary,
                        selectedTextColor = RadiantGoldPrimary,
                        indicatorColor = RadiantGoldPrimary,
                        unselectedIconColor = TextOnSurfaceVariant,
                        unselectedTextColor = TextOnSurfaceVariant
                    )
                )

                // Tab 4: Profile
                NavigationBarItem(
                    selected = currentTab == AppTab.PROFILE,
                    onClick = { viewModel.setTab(AppTab.PROFILE) },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = OnGoldPrimary,
                        selectedTextColor = RadiantGoldPrimary,
                        indicatorColor = RadiantGoldPrimary,
                        unselectedIconColor = TextOnSurfaceVariant,
                        unselectedTextColor = TextOnSurfaceVariant
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                AppTab.REPORT -> ReportPotholeScreen(viewModel = viewModel)
                AppTab.SAFE_ROUTE -> SafeRouteScreen(viewModel = viewModel)
                AppTab.HISTORY -> ComplaintHistoryScreen(viewModel = viewModel)
                AppTab.PROFILE -> UserProfileScreen(viewModel = viewModel)
            }
        }
    }
}
