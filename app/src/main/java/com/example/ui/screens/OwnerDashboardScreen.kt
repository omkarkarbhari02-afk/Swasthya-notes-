package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.SwasthyaViewModel
import com.example.ui.components.QrCodeGenerator
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerDashboardScreen(
    viewModel: SwasthyaViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val ownerTab by viewModel.ownerDashboardTab.collectAsState()

    val pendingRequests by viewModel.pendingRequests.collectAsState()
    val allRequests by viewModel.allRequests.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val totalUsers by viewModel.totalUsersCount.collectAsState()
    val totalPdfs by viewModel.totalPdfsCount.collectAsState()
    val totalDownloads by viewModel.totalDownloadsCount.collectAsState()
    val appSettings by viewModel.appSettings.collectAsState()

    val tabs = listOf("Approvals", "Users", "Pricing & UPI", "Broadcast", "Analytics")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Owner Admin Dashboard",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Content, Manual Approvals & Portal Management",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("owner_back_btn")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Scrollable Tab Row
            PrimaryScrollableTabRow(
                selectedTabIndex = ownerTab,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = ownerTab == index,
                        onClick = { viewModel.setOwnerDashboardTab(index) },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(title, fontWeight = if (ownerTab == index) FontWeight.Bold else FontWeight.Normal)
                                if (index == 0 && pendingRequests.isNotEmpty()) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "${pendingRequests.size}",
                                                color = Color.White,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    )
                }
            }

            when (ownerTab) {
                0 -> AccessRequestsApprovalTab(
                    pendingRequests = pendingRequests,
                    allRequests = allRequests,
                    onApprove = { req, accessType, days, note ->
                        viewModel.approveRequestDirect(req, accessType, days, note)
                        Toast.makeText(context, "Access Granted for ${req.userName}!", Toast.LENGTH_SHORT).show()
                    },
                    onReject = { req, reason, reupload ->
                        viewModel.rejectRequest(req, reason, reupload)
                        Toast.makeText(context, if (reupload) "Requested new screenshot" else "Request declined", Toast.LENGTH_SHORT).show()
                    }
                )
                1 -> UsersManagementTab(
                    users = allUsers,
                    onToggleSuspend = { viewModel.toggleUserSuspension(it) },
                    onDeleteUser = { viewModel.deleteUser(it.id) }
                )
                2 -> PricingAndUpiSettingsTab(
                    settings = appSettings,
                    onSaveSetting = { key, value ->
                        viewModel.updateAppSetting(key, value)
                        Toast.makeText(context, "Setting updated successfully", Toast.LENGTH_SHORT).show()
                    }
                )
                3 -> BroadcastNotificationsTab(
                    onSendNotification = { title, msg, target ->
                        viewModel.sendBroadcastNotification(title, msg, target)
                        Toast.makeText(context, "Broadcast notification sent to $target!", Toast.LENGTH_SHORT).show()
                    }
                )
                4 -> AnalyticsSummaryTab(
                    totalUsers = totalUsers,
                    totalPdfs = totalPdfs,
                    totalDownloads = totalDownloads,
                    totalRequests = allRequests.size,
                    approvedRequests = allRequests.count { it.status == RequestStatus.APPROVED }
                )
            }
        }
    }
}

@Composable
fun AccessRequestsApprovalTab(
    pendingRequests: List<AccessRequestEntity>,
    allRequests: List<AccessRequestEntity>,
    onApprove: (AccessRequestEntity, AccessType, Int?, String) -> Unit,
    onReject: (AccessRequestEntity, String, Boolean) -> Unit
) {
    var filterStatus by remember { mutableStateOf<RequestStatus?>(RequestStatus.PENDING) }
    var selectedRequestForAction by remember { mutableStateOf<AccessRequestEntity?>(null) }
    var showApproveModal by remember { mutableStateOf(false) }
    var showRejectModal by remember { mutableStateOf(false) }

    val displayedRequests = remember(allRequests, filterStatus) {
        if (filterStatus == null) allRequests
        else allRequests.filter { it.status == filterStatus }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Filter pills
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = filterStatus == RequestStatus.PENDING,
                onClick = { filterStatus = RequestStatus.PENDING },
                label = { Text("Pending (${pendingRequests.size})") },
                modifier = Modifier.testTag("filter_pending_chip")
            )
            FilterChip(
                selected = filterStatus == RequestStatus.APPROVED,
                onClick = { filterStatus = RequestStatus.APPROVED },
                label = { Text("Approved") }
            )
            FilterChip(
                selected = filterStatus == null,
                onClick = { filterStatus = null },
                label = { Text("All (${allRequests.size})") }
            )
        }

        if (displayedRequests.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.CheckCircleOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "No requests in this view",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Pending access requests submitted by students will appear here for manual verification.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(displayedRequests, key = { it.id }) { req ->
                    AccessRequestCard(
                        request = req,
                        onApproveClick = {
                            selectedRequestForAction = req
                            showApproveModal = true
                        },
                        onRejectClick = {
                            selectedRequestForAction = req
                            showRejectModal = true
                        }
                    )
                }
                item { Spacer(modifier = Modifier.height(30.dp)) }
            }
        }
    }

    // Approve Dialog with Lifetime vs Temporary grant
    if (showApproveModal && selectedRequestForAction != null) {
        val req = selectedRequestForAction!!
        var selectedAccessType by remember { mutableStateOf(AccessType.LIFETIME) }
        var expiryDays by remember { mutableStateOf("365") }
        var approvalNote by remember { mutableStateOf("Verified UTR payment.") }

        AlertDialog(
            onDismissRequest = { showApproveModal = false },
            title = { Text("Approve Access for ${req.userName}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Material: ${req.contentTitle}\nUTR: ${req.utrNumber}\nAmount: ₹${req.pricePaid}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text("Grant Duration:", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = selectedAccessType == AccessType.LIFETIME,
                            onClick = { selectedAccessType = AccessType.LIFETIME },
                            label = { Text("Lifetime") }
                        )
                        FilterChip(
                            selected = selectedAccessType == AccessType.TEMPORARY,
                            onClick = { selectedAccessType = AccessType.TEMPORARY },
                            label = { Text("Temporary") }
                        )
                        FilterChip(
                            selected = selectedAccessType == AccessType.FREE_GRANT,
                            onClick = { selectedAccessType = AccessType.FREE_GRANT },
                            label = { Text("Free Grant") }
                        )
                    }

                    if (selectedAccessType == AccessType.TEMPORARY) {
                        OutlinedTextField(
                            value = expiryDays,
                            onValueChange = { expiryDays = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Validity in Days (e.g. 30, 90, 365)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    OutlinedTextField(
                        value = approvalNote,
                        onValueChange = { approvalNote = it },
                        label = { Text("Owner Note / Remark") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val days = if (selectedAccessType == AccessType.TEMPORARY) expiryDays.toIntOrNull() else null
                        onApprove(req, selectedAccessType, days, approvalNote)
                        showApproveModal = false
                    },
                    modifier = Modifier.testTag("confirm_grant_access_btn")
                ) {
                    Text("Grant Access")
                }
            },
            dismissButton = {
                TextButton(onClick = { showApproveModal = false }) { Text("Cancel") }
            }
        )
    }

    // Reject Dialog
    if (showRejectModal && selectedRequestForAction != null) {
        val req = selectedRequestForAction!!
        var rejectionReason by remember { mutableStateOf("Payment screenshot or UTR number could not be verified.") }
        var askReupload by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showRejectModal = false },
            title = { Text("Decline Request") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Student: ${req.userName} (${req.userEmail})")

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Ask student for another screenshot", style = MaterialTheme.typography.bodySmall)
                        Switch(checked = askReupload, onCheckedChange = { askReupload = it })
                    }

                    OutlinedTextField(
                        value = rejectionReason,
                        onValueChange = { rejectionReason = it },
                        label = { Text("Reason for Student") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onReject(req, rejectionReason, askReupload)
                        showRejectModal = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(if (askReupload) "Request Resubmission" else "Decline")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRejectModal = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun AccessRequestCard(
    request: AccessRequestEntity,
    onApproveClick: () -> Unit,
    onRejectClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }
    val formattedDate = remember(request.requestedAt) { dateFormat.format(Date(request.requestedAt)) }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth().testTag("request_card_${request.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Student details + Status badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = request.userName.take(1).uppercase(),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = request.userName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = request.userEmail,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                Surface(
                    color = when (request.status) {
                        RequestStatus.APPROVED -> Color(0xFFE8F5E9)
                        RequestStatus.PENDING -> Color(0xFFFFF3E0)
                        RequestStatus.NEED_SCREENSHOT -> Color(0xFFE3F2FD)
                        RequestStatus.REJECTED -> Color(0xFFFFEBEE)
                    },
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = request.status.name.replace('_', ' '),
                        color = when (request.status) {
                            RequestStatus.APPROVED -> Color(0xFF1B873F)
                            RequestStatus.PENDING -> Color(0xFFE65100)
                            RequestStatus.NEED_SCREENSHOT -> Color(0xFF1565C0)
                            RequestStatus.REJECTED -> Color(0xFFC62828)
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            // Requested Content Info
            Text(
                text = "Requested Material:",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
            Text(
                text = request.contentTitle,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "${request.contentYear} • Fee: ₹${request.pricePaid}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Payment verification details: UTR and Screenshot proof
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("UTR / Ref Number:", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                        Text(request.utrNumber, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Screenshot Attachment:", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Attachment, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(request.screenshotUri.ifEmpty { "payment_slip.jpg" }, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    if (request.message.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Message: \"${request.message}\"", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Time: $formattedDate", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                }
            }

            // Action Buttons for Pending Requests
            if (request.status == RequestStatus.PENDING) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onRejectClick,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Decline")
                    }
                    Button(
                        onClick = onApproveClick,
                        modifier = Modifier.weight(1f).testTag("approve_btn_${request.id}")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Grant Access")
                    }
                }
            } else if (request.ownerNote.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Owner Note: ${request.ownerNote}", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
            }
        }
    }
}

@Composable
fun UsersManagementTab(
    users: List<UserEntity>,
    onToggleSuspend: (UserEntity) -> Unit,
    onDeleteUser: (UserEntity) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredUsers = remember(users, searchQuery) {
        if (searchQuery.isBlank()) users
        else users.filter { it.name.contains(searchQuery, ignoreCase = true) || it.email.contains(searchQuery, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search students by name or email...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("user_search_input")
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Registered Students (${filteredUsers.size})",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(filteredUsers, key = { it.id }) { user ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (user.role == UserRole.OWNER) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    if (user.role == UserRole.OWNER) Icons.Default.AdminPanelSettings else Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(user.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                if (user.isSuspended) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(color = Color(0xFFFFEBEE), shape = RoundedCornerShape(4.dp)) {
                                        Text("SUSPENDED", fontSize = 9.sp, color = Color.Red, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp))
                                    }
                                }
                            }
                            Text(user.email, fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                            Text("${user.role.name} • ${user.bamsYear}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                        }

                        if (user.role != UserRole.OWNER) {
                            IconButton(onClick = { onToggleSuspend(user) }) {
                                Icon(
                                    if (user.isSuspended) Icons.Default.LockOpen else Icons.Default.Block,
                                    contentDescription = "Suspend/Unsuspend",
                                    tint = if (user.isSuspended) MaterialTheme.colorScheme.primary else Color(0xFFE65100)
                                )
                            }
                            IconButton(onClick = { onDeleteUser(user) }) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PricingAndUpiSettingsTab(
    settings: Map<String, String>,
    onSaveSetting: (String, String) -> Unit
) {
    var upiId by remember(settings) { mutableStateOf(settings["owner_upi_id"] ?: "swasthya.notes@oksbi") }
    var upiName by remember(settings) { mutableStateOf(settings["owner_upi_name"] ?: "Swasthya Notes BAMS Portal") }
    var bundleYear1 by remember(settings) { mutableStateOf(settings["price_bundle_year1"] ?: "599") }
    var bundleYear2 by remember(settings) { mutableStateOf(settings["price_bundle_year2"] ?: "699") }
    var bundleYear3 by remember(settings) { mutableStateOf(settings["price_bundle_year3"] ?: "799") }

    val qrBitmap = remember(upiId, upiName) {
        QrCodeGenerator.generateUpiQrBitmap("upi://pay?pa=$upiId&pn=${upiName.replace(" ", "%20")}", 400)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Owner UPI & QR Code Settings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    "All student access requests will pay to this UPI ID and QR code.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )

                OutlinedTextField(
                    value = upiId,
                    onValueChange = { upiId = it.trim() },
                    label = { Text("Owner UPI ID *") },
                    leadingIcon = { Icon(Icons.Default.Payment, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("owner_upi_id_input")
                )

                OutlinedTextField(
                    value = upiName,
                    onValueChange = { upiName = it },
                    label = { Text("Account Holder / Business Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        onSaveSetting("owner_upi_id", upiId)
                        onSaveSetting("owner_upi_name", upiName)
                    },
                    modifier = Modifier.fillMaxWidth().testTag("save_upi_settings_btn")
                ) {
                    Text("Update UPI & QR Code")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text("Active QR Code Preview:", style = MaterialTheme.typography.labelSmall)
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = "QR Code Preview",
                        modifier = Modifier
                            .size(160.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                    )
                }
            }
        }

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Complete Year Bundle Pricing", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    "Prices shown to students for unlocking an entire year of notes and PYQs.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )

                OutlinedTextField(
                    value = bundleYear1,
                    onValueChange = { bundleYear1 = it.filter { ch -> ch.isDigit() } },
                    label = { Text("BAMS 1st Year Complete Bundle (₹)") },
                    leadingIcon = { Text("₹", fontWeight = FontWeight.Bold) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = bundleYear2,
                    onValueChange = { bundleYear2 = it.filter { ch -> ch.isDigit() } },
                    label = { Text("BAMS 2nd Year Complete Bundle (₹)") },
                    leadingIcon = { Text("₹", fontWeight = FontWeight.Bold) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = bundleYear3,
                    onValueChange = { bundleYear3 = it.filter { ch -> ch.isDigit() } },
                    label = { Text("BAMS 3rd Year Complete Bundle (₹)") },
                    leadingIcon = { Text("₹", fontWeight = FontWeight.Bold) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        onSaveSetting("price_bundle_year1", bundleYear1)
                        onSaveSetting("price_bundle_year2", bundleYear2)
                        onSaveSetting("price_bundle_year3", bundleYear3)
                    },
                    modifier = Modifier.fillMaxWidth().testTag("save_bundle_pricing_btn")
                ) {
                    Text("Save Bundle Prices")
                }
            }
        }
    }
}

@Composable
fun BroadcastNotificationsTab(
    onSendNotification: (title: String, message: String, target: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var selectedTarget by remember { mutableStateOf("ALL") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Send Announcement via FCM", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(
            "Broadcast study alerts, syllabus updates, or exam tips to registered BAMS students.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline
        )

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Notification Title *") },
            placeholder = { Text("e.g. New Dravyaguna PYQs Solved") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("notif_title_input")
        )

        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            label = { Text("Notification Content *") },
            placeholder = { Text("Enter detailed notification message for students...") },
            maxLines = 4,
            modifier = Modifier.fillMaxWidth().testTag("notif_msg_input")
        )

        Text("Target Audience:", style = MaterialTheme.typography.labelMedium)
        val targetOptions = listOf(
            "ALL" to "All Users",
            "BAMS 1st Year" to "1st Year Only",
            "BAMS 2nd Year" to "2nd Year Only",
            "BAMS 3rd Year" to "3rd Year Only",
            "APPROVED" to "Approved Students"
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            targetOptions.take(3).forEach { (target, label) ->
                FilterChip(
                    selected = selectedTarget == target,
                    onClick = { selectedTarget = target },
                    label = { Text(label, fontSize = 11.sp) }
                )
            }
        }

        Button(
            onClick = {
                if (title.isNotBlank() && message.isNotBlank()) {
                    onSendNotification(title.trim(), message.trim(), selectedTarget)
                    title = ""
                    message = ""
                }
            },
            modifier = Modifier.fillMaxWidth().testTag("send_broadcast_btn")
        ) {
            Icon(Icons.Default.Send, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Send Notification")
        }
    }
}

@Composable
fun AnalyticsSummaryTab(
    totalUsers: Int,
    totalPdfs: Int,
    totalDownloads: Int,
    totalRequests: Int,
    approvedRequests: Int
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Portal Analytics & Performance", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "Total Users",
                value = "$totalUsers",
                icon = Icons.Default.People,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Total PDFs & PYQs",
                value = "$totalPdfs",
                icon = Icons.Default.MenuBook,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "Offline Downloads",
                value = "$totalDownloads",
                icon = Icons.Default.CloudDone,
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Access Requests",
                value = "$totalRequests",
                icon = Icons.Default.ReceiptLong,
                color = Color(0xFFD97706),
                modifier = Modifier.weight(1f)
            )
        }

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Approval Conversion", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                val percentage = if (totalRequests > 0) (approvedRequests * 100 / totalRequests) else 100
                Text(
                    text = "$approvedRequests of $totalRequests requests approved ($percentage%)",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { if (totalRequests > 0) approvedRequests.toFloat() / totalRequests else 0f },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))
                )
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
            Text(text = title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
        }
    }
}
