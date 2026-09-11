package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.AppScreen
import com.example.ui.SwasthyaViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentProfileScreen(
    viewModel: SwasthyaViewModel,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val userPermissions by viewModel.userPermissions.collectAsState()
    val userRequests by viewModel.userRequests.collectAsState()
    val offlineDownloads by viewModel.offlineDownloads.collectAsState()
    val appSettings by viewModel.appSettings.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Approved (${userPermissions.size})", "Offline (${offlineDownloads.size})", "Requests (${userRequests.size})")

    val isOwner = currentUser?.role == UserRole.OWNER
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile & Study Vault") },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("profile_back_btn")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onOpenSettings, modifier = Modifier.testTag("profile_settings_btn")) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // User Info Header Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = if (isOwner) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.size(54.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = (currentUser?.name ?: "S").take(1).uppercase(),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = currentUser?.name ?: "Student",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = currentUser?.email ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Text(
                                text = if (isOwner) "Role: Owner (Admin)" else "Enrolled: ${currentUser?.bamsYear}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(12.dp))

                    // Switch Role Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Current Role: ${if (isOwner) "Owner (Admin)" else "Student"}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Tap switch to test other role",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }

                        Button(
                            onClick = {
                                if (isOwner) {
                                    viewModel.switchUserRole(UserRole.STUDENT)
                                    Toast.makeText(context, "Switched to Student Role", Toast.LENGTH_SHORT).show()
                                } else {
                                    viewModel.switchUserRole(UserRole.OWNER)
                                    Toast.makeText(context, "Switched to Owner (Admin) Role", Toast.LENGTH_SHORT).show()
                                }
                            },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("switch_role_btn")
                        ) {
                            Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isOwner) "Switch to Student" else "Switch to Owner")
                        }
                    }
                }
            }

            // Tab Row
            TabRow(selectedTabIndex = selectedTab, modifier = Modifier.fillMaxWidth()) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }

            when (selectedTab) {
                // Approved Materials
                0 -> {
                    if (userPermissions.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("No approved materials yet", fontWeight = FontWeight.Bold)
                                Text("Request access to notes from the curriculum browser.", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(userPermissions, key = { it.id }) { perm ->
                                Card(
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF1B873F))
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Approved Access", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text("Item ID: ${perm.contentItemId}", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                                            Text("Type: ${perm.accessType.name}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                        }
                                        Surface(color = Color(0xFFE8F5E9), shape = RoundedCornerShape(4.dp)) {
                                            Text("ACTIVE", fontSize = 10.sp, color = Color(0xFF1B873F), fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Offline Vault
                1 -> {
                    if (offlineDownloads.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.CloudDownload, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Offline Vault Empty", fontWeight = FontWeight.Bold)
                                Text("Download notes for offline reading directly in the PDF viewer.", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(offlineDownloads, key = { it.contentItemId }) { download ->
                                Card(
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.CloudDone, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(download.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text("AES-256 Encrypted Private Storage", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                            Text("Downloaded on ${dateFormat.format(Date(download.downloadedAt))}", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                                        }
                                        IconButton(onClick = { viewModel.deleteOfflineDownload(download.contentItemId) }) {
                                            Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Requests History
                2 -> {
                    if (userRequests.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("No requests placed", fontWeight = FontWeight.Bold)
                                Text("When you submit an access request, its status will be tracked here.", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(userRequests, key = { it.id }) { req ->
                                Card(
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(req.contentTitle, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.weight(1f))
                                            Surface(
                                                color = when (req.status) {
                                                    RequestStatus.APPROVED -> Color(0xFFE8F5E9)
                                                    RequestStatus.PENDING -> Color(0xFFFFF3E0)
                                                    RequestStatus.NEED_SCREENSHOT -> Color(0xFFE3F2FD)
                                                    RequestStatus.REJECTED -> Color(0xFFFFEBEE)
                                                },
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = req.status.name.replace('_', ' '),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = when (req.status) {
                                                        RequestStatus.APPROVED -> Color(0xFF1B873F)
                                                        RequestStatus.PENDING -> Color(0xFFE65100)
                                                        RequestStatus.NEED_SCREENSHOT -> Color(0xFF1565C0)
                                                        RequestStatus.REJECTED -> Color(0xFFC62828)
                                                    },
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text("UTR: ${req.utrNumber} • Paid: ₹${req.pricePaid}", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                                        if (req.ownerNote.isNotEmpty()) {
                                            Text("Owner Note: ${req.ownerNote}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
