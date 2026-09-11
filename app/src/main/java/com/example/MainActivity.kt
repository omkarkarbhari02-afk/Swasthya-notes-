package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AppScreen
import com.example.ui.SwasthyaViewModel
import com.example.ui.components.AccessRequestDialog
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: SwasthyaViewModel = viewModel()
            val isDarkMode by viewModel.isDarkMode.collectAsState()
            val currentScreen by viewModel.currentScreen.collectAsState()
            val activeReaderItem by viewModel.activeReaderItem.collectAsState()
            val accessRequestItem by viewModel.accessRequestItem.collectAsState()
            val appSettings by viewModel.appSettings.collectAsState()

            val ownerUpiId = appSettings["owner_upi_id"] ?: "swasthya.notes@oksbi"
            val ownerUpiName = appSettings["owner_upi_name"] ?: "Swasthya Notes BAMS Portal"

            // System Back Handler
            BackHandler(enabled = currentScreen != AppScreen.Home && currentScreen != AppScreen.Auth) {
                when (currentScreen) {
                    AppScreen.ContentBrowser -> {
                        val handled = viewModel.navigateUpHierarchy()
                        if (!handled) viewModel.navigateTo(AppScreen.Home)
                    }
                    AppScreen.SecurePdfViewer -> viewModel.closeSecureReader()
                    else -> viewModel.navigateTo(AppScreen.Home)
                }
            }

            MyApplicationTheme(darkTheme = isDarkMode) {
                Crossfade(targetState = currentScreen, label = "screen_transition") { screen ->
                    when (screen) {
                        AppScreen.Auth -> AuthScreen(viewModel = viewModel)
                        AppScreen.Home -> HomeScreen(
                            viewModel = viewModel,
                            onOpenYear = { yearItem -> viewModel.openYear(yearItem) },
                            onOpenSearch = { viewModel.navigateTo(AppScreen.Search) },
                            onOpenProfile = { viewModel.navigateTo(AppScreen.StudentProfile) },
                            onOpenNotifications = { viewModel.navigateTo(AppScreen.Notifications) },
                            onOpenOwnerDashboard = { viewModel.navigateTo(AppScreen.OwnerDashboard) }
                        )
                        AppScreen.ContentBrowser -> ContentBrowserScreen(
                            viewModel = viewModel,
                            onBack = {
                                val handled = viewModel.navigateUpHierarchy()
                                if (!handled) viewModel.navigateTo(AppScreen.Home)
                            }
                        )
                        AppScreen.Search -> SearchScreen(
                            viewModel = viewModel,
                            onBack = { viewModel.navigateTo(AppScreen.Home) }
                        )
                        AppScreen.OwnerDashboard -> OwnerDashboardScreen(
                            viewModel = viewModel,
                            onBack = { viewModel.navigateTo(AppScreen.Home) }
                        )
                        AppScreen.StudentProfile -> StudentProfileScreen(
                            viewModel = viewModel,
                            onBack = { viewModel.navigateTo(AppScreen.Home) },
                            onOpenSettings = { viewModel.navigateTo(AppScreen.Settings) }
                        )
                        AppScreen.Notifications -> NotificationsScreen(
                            viewModel = viewModel,
                            onBack = { viewModel.navigateTo(AppScreen.Home) }
                        )
                        AppScreen.Settings -> SettingsScreen(
                            viewModel = viewModel,
                            onBack = { viewModel.navigateTo(AppScreen.StudentProfile) }
                        )
                        AppScreen.SecurePdfViewer -> {
                            activeReaderItem?.let { item ->
                                SecurePdfViewerScreen(
                                    item = item,
                                    viewModel = viewModel,
                                    onBack = { viewModel.closeSecureReader() }
                                )
                            } ?: run {
                                viewModel.navigateTo(AppScreen.ContentBrowser)
                            }
                        }
                    }
                }

                // Global Access Request Modal Dialog (Triggered when student clicks a locked note)
                accessRequestItem?.let { item ->
                    AccessRequestDialog(
                        item = item,
                        ownerUpiId = ownerUpiId,
                        ownerUpiName = ownerUpiName,
                        onDismiss = { viewModel.closeAccessRequestDialog() },
                        onSubmitRequest = { utr, screenshotUri, message ->
                            viewModel.requestAccess(
                                item = item,
                                utrNumber = utr,
                                screenshotUri = screenshotUri,
                                message = message,
                                onComplete = { }
                            )
                        }
                    )
                }
            }
        }
    }
}
