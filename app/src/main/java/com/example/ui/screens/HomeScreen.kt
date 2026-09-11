package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ContentItemEntity
import com.example.data.model.UserRole
import com.example.ui.AppScreen
import com.example.ui.SwasthyaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: SwasthyaViewModel,
    onOpenYear: (ContentItemEntity) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenOwnerDashboard: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val years by viewModel.years.collectAsState()
    val pendingRequestsCount by viewModel.pendingRequestsCount.collectAsState()
    val offlineDownloads by viewModel.offlineDownloads.collectAsState()
    val userRequests by viewModel.userRequests.collectAsState()
    val notifications by viewModel.notifications.collectAsState()

    val isOwner = currentUser?.role == UserRole.OWNER
    val pendingUserRequestsCount = userRequests.count { it.status.name == "PENDING" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Spa,
                                    contentDescription = "Swasthya Notes Logo",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Swasthya Notes",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "BAMS Secure Study Platform",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                },
                actions = {
                    // Search Action
                    IconButton(
                        onClick = onOpenSearch,
                        modifier = Modifier.testTag("home_search_btn")
                    ) {
                        Icon(Icons.Default.Search, contentDescription = "Search Notes")
                    }

                    // Notifications Action with badge
                    BadgedBox(
                        badge = {
                            if (notifications.isNotEmpty()) {
                                Badge { Text("${notifications.size}") }
                            }
                        }
                    ) {
                        IconButton(onClick = onOpenNotifications, modifier = Modifier.testTag("home_notifications_btn")) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                        }
                    }

                    // Profile / Role Switcher Action
                    IconButton(onClick = onOpenProfile, modifier = Modifier.testTag("home_profile_btn")) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Profile")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))

                // Welcome & Role Banner
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Namaste, ${currentUser?.name ?: "Student"}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isOwner) "Administrator / Content Director" else (currentUser?.bamsYear ?: "BAMS Scholar"),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Role Switcher Chip for immediate testing
                            FilterChip(
                                selected = isOwner,
                                onClick = {
                                    if (isOwner) {
                                        viewModel.switchUserRole(UserRole.STUDENT)
                                    } else {
                                        viewModel.switchUserRole(UserRole.OWNER)
                                    }
                                },
                                label = {
                                    Text(
                                        text = if (isOwner) "Role: Owner (Admin)" else "Role: Student",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        if (isOwner) Icons.Default.AdminPanelSettings else Icons.Default.School,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                },
                                modifier = Modifier.testTag("role_toggle_chip")
                            )
                        }

                        // Owner Alert Banner
                        if (isOwner && pendingRequestsCount > 0) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onOpenOwnerDashboard() }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.NotificationImportant,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "$pendingRequestsCount Pending Access Requests to approve",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Icon(
                                        Icons.Default.ArrowForward,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }

                        // Student Pending Requests Banner
                        if (!isOwner && pendingUserRequestsCount > 0) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onOpenProfile() }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.HourglassTop,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "$pendingUserRequestsCount request sent to Owner (awaiting manual approval)",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Quick Search Bar
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenSearch() }
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                        .testTag("home_search_bar")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 13.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Search by Year, Subject, Chapter, or PDF...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            // Section Header: The 3 Main Categories
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Curriculum Academic Years",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "3 Categories",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // 3 Main Categories as strictly requested:
            // - BAMS 1st Year
            // - BAMS 2nd Year
            // - BAMS 3rd Year
            items(years) { yearItem ->
                YearCategoryCard(
                    year = yearItem,
                    onTap = { onOpenYear(yearItem) }
                )
            }

            // Quick Shortcuts: Offline Vault & Approved Material
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Quick Study Vault",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Offline Vault Card
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onOpenProfile() }
                            .testTag("offline_vault_shortcut"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.DownloadDone,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Offline Vault",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "${offlineDownloads.size} Encrypted PDFs",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Text(
                                text = "AES-256 Storage",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Owner Dashboard Shortcut (if Owner) or Access Status
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                if (isOwner) onOpenOwnerDashboard() else onOpenProfile()
                            }
                            .testTag("owner_dashboard_shortcut"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isOwner) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    if (isOwner) Icons.Default.DashboardCustomize else Icons.Default.VerifiedUser,
                                    contentDescription = null,
                                    tint = if (isOwner) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isOwner) "Owner Panel" else "My Approvals",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (isOwner) "Manage & Approve" else "View Active Notes",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Text(
                                text = if (isOwner) "$pendingRequestsCount Pending" else "DRM Protected",
                                fontSize = 10.sp,
                                color = if (isOwner && pendingRequestsCount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun YearCategoryCard(
    year: ContentItemEntity,
    onTap: () -> Unit
) {
    val (yearNumber, accentColor, gradientBrush, subjectPreview) = when (year.id) {
        "year_1" -> Quadruple(
            "1st Year",
            Color(0xFF006C51), // Emerald
            Brush.linearGradient(listOf(Color(0xFF006C51), Color(0xFF0F3B2E))),
            listOf("Padartha Vigyana", "Samhita (Ashtanga Hridaya)", "Rachana Sharira", "Kriya Sharira")
        )
        "year_2" -> Quadruple(
            "2nd Year",
            Color(0xFF845400), // Amber
            Brush.linearGradient(listOf(Color(0xFF7A5900), Color(0xFF473300))),
            listOf("Dravyaguna Vijnana", "Rasashastra & BK", "Agadtantra", "Roga Nidana")
        )
        else -> Quadruple(
            "3rd Year",
            Color(0xFF1E5B6A), // Teal Navy
            Brush.linearGradient(listOf(Color(0xFF1E5B6A), Color(0xFF0E323A))),
            listOf("Charaka Samhita", "Swasthavritta & Yoga", "Panchakarma Vijnana")
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onTap() }
            .testTag("year_card_${year.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header with rich gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(gradientBrush)
                    .padding(horizontal = 20.dp, vertical = 18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.MenuBook,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = year.title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "B.A.M.S. Undergraduate Curriculum",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }

                    Surface(
                        color = Color.White.copy(alpha = 0.22f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = "Open Year",
                            tint = Color.White,
                            modifier = Modifier
                                .padding(8.dp)
                                .size(20.dp)
                        )
                    }
                }
            }

            // Subject highlights & description inside card body
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = year.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Core Subject Folders:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Subject pills
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    subjectPreview.forEach { subjectName ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Folder,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = subjectName,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PDFs • Question Banks • PYQs • Short Notes",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )

                    TextButton(
                        onClick = onTap,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("Explore Content", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}

data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
