package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.SwasthyaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SwasthyaViewModel,
    onBack: () -> Unit
) {
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings & Security") },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("settings_back_btn")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Appearance
            Text("Appearance", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode, contentDescription = null)
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text("Dark Theme", fontWeight = FontWeight.SemiBold)
                            Text(if (isDarkMode) "Botanical dark mode" else "Ayurvedic emerald light mode", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { viewModel.toggleDarkMode() },
                        modifier = Modifier.testTag("dark_mode_switch")
                    )
                }
            }

            // Security & DRM Information
            Text("DRM & Digital Copyright Protection", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SecurityBullet(
                        icon = Icons.Default.Lock,
                        title = "AES-256 Offline Storage",
                        desc = "All offline study notes are encrypted with AES-256 and stored strictly inside private application sandbox. External file managers and PCs cannot open them."
                    )
                    SecurityBullet(
                        icon = Icons.Default.Block,
                        title = "Screenshot & Recording Blocked",
                        desc = "Android WindowManager.LayoutParams.FLAG_SECURE is engaged whenever protected PDF notes are being viewed."
                    )
                    SecurityBullet(
                        icon = Icons.Default.BrandingWatermark,
                        title = "Dynamic Student Watermark",
                        desc = "Every page displays the registered student's full name and email diagonally across the content to deter unauthorized distribution."
                    )
                    SecurityBullet(
                        icon = Icons.Default.Share,
                        title = "No Export / Print / Sharing",
                        desc = "The custom in-app viewer disables direct system sharing, printing, or copying text."
                    )
                }
            }

            // About Application
            Text("About Application", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Swasthya Notes - BAMS Study Portal", fontWeight = FontWeight.Bold)
                    Text("Version 1.0.0 (Production Build)", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                    Text(
                        "Designed exclusively for B.A.M.S. students to access accredited Ayurvedic study notes, question banks, and solved papers with manual payment approval.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Logout Action
            Button(
                onClick = { viewModel.logout() },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer),
                modifier = Modifier.fillMaxWidth().testTag("logout_btn"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Logout, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log Out of Swasthya Notes")
            }
        }
    }
}

@Composable
fun SecurityBullet(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, desc: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Text(desc, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
