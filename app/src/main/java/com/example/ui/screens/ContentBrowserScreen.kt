package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.AppScreen
import com.example.ui.SwasthyaViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentBrowserScreen(
    viewModel: SwasthyaViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currentUser by viewModel.currentUser.collectAsState()
    val navPath by viewModel.navPath.collectAsState()
    val children by viewModel.currentChildren.collectAsState()
    val userPermissions by viewModel.userPermissions.collectAsState()
    val userRequests by viewModel.userRequests.collectAsState()
    val offlineDownloads by viewModel.offlineDownloads.collectAsState()

    val isOwner = currentUser?.role == UserRole.OWNER

    // Owner Add / Edit Item Dialog State
    var showAddDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<ContentItemEntity?>(null) }
    var itemToDelete by remember { mutableStateOf<ContentItemEntity?>(null) }

    val currentParent = navPath.lastOrNull()
    val currentYearTag = navPath.firstOrNull()?.yearTag ?: "BAMS 1st Year"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = currentParent?.title ?: "Study Materials",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = currentYearTag,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            val handled = viewModel.navigateUpHierarchy()
                            if (!handled) onBack()
                        },
                        modifier = Modifier.testTag("browser_back_btn")
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isOwner) {
                        FilledTonalButton(
                            onClick = { showAddDialog = true },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("owner_add_item_top_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Content", fontSize = 12.sp)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            if (isOwner) {
                ExtendedFloatingActionButton(
                    onClick = { showAddDialog = true },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Add Folder / PDF") },
                    modifier = Modifier.testTag("owner_fab_add")
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Breadcrumbs Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.clickable { onBack() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Years", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                navPath.forEachIndexed { index, pathItem ->
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(16.dp).padding(horizontal = 2.dp)
                    )
                    val isLast = index == navPath.lastIndex
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isLast) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                        modifier = Modifier.clickable { viewModel.jumpToBreadcrumb(index) }
                    ) {
                        Text(
                            text = pathItem.title,
                            fontSize = 12.sp,
                            fontWeight = if (isLast) FontWeight.Bold else FontWeight.Normal,
                            color = if (isLast) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Items List
            if (children.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.FolderOpen,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No items in this section yet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isOwner) "Tap 'Add Content' to upload chapters, PDFs, PYQs, or question banks."
                            else "Content is being updated by the Owner. Check back soon.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        if (isOwner) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { showAddDialog = true }) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Add Study Material")
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(children, key = { it.id }) { item ->
                        var hasAccess by remember { mutableStateOf(false) }

                        LaunchedEffect(item.id, userPermissions, currentUser?.id) {
                            hasAccess = viewModel.hasAccessToItem(item)
                        }

                        val pendingRequest = userRequests.firstOrNull { it.contentItemId == item.id && it.status == RequestStatus.PENDING }
                        val isDownloaded = offlineDownloads.any { it.contentItemId == item.id }

                        ContentItemRow(
                            item = item,
                            hasAccess = hasAccess,
                            pendingRequest = pendingRequest,
                            isDownloaded = isDownloaded,
                            isOwner = isOwner,
                            onItemClick = {
                                if (item.type in listOf(ContentType.SUBJECT, ContentType.FOLDER, ContentType.SUBFOLDER, ContentType.CHAPTER)) {
                                    // Drill down
                                    viewModel.drillDown(item)
                                } else {
                                    // It's a study file (PDF, PYQ, Question Bank, Short Note)
                                    if (hasAccess) {
                                        viewModel.openSecureReader(item)
                                    } else {
                                        // Open Access Request Dialog
                                        viewModel.openAccessRequestDialog(item)
                                    }
                                }
                            },
                            onEdit = { editingItem = item },
                            onDelete = { itemToDelete = item }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(72.dp))
                    }
                }
            }
        }
    }

    // Owner Add / Edit Item Dialog
    if (showAddDialog || editingItem != null) {
        val itemToEdit = editingItem
        OwnerContentEditorDialog(
            initialItem = itemToEdit,
            parentId = currentParent?.id,
            yearTag = currentYearTag,
            subjectTag = if (currentParent?.type == ContentType.SUBJECT) currentParent.title else (currentParent?.subjectTag ?: ""),
            onDismiss = {
                showAddDialog = false
                editingItem = null
            },
            onSave = { savedItem ->
                viewModel.saveContentItem(savedItem)
                showAddDialog = false
                editingItem = null
                Toast.makeText(context, "Saved: ${savedItem.title}", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Delete Confirmation Dialog
    if (itemToDelete != null) {
        val target = itemToDelete!!
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text("Delete Content Item") },
            text = { Text("Are you sure you want to delete '${target.title}'? All nested folders and materials inside will also be removed.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteContentItem(target.id)
                        itemToDelete = null
                        Toast.makeText(context, "Deleted: ${target.title}", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun ContentItemRow(
    item: ContentItemEntity,
    hasAccess: Boolean,
    pendingRequest: AccessRequestEntity?,
    isDownloaded: Boolean,
    isOwner: Boolean,
    onItemClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isFolderLike = item.type in listOf(ContentType.SUBJECT, ContentType.FOLDER, ContentType.SUBFOLDER, ContentType.CHAPTER)

    val (typeIcon, iconTint, typeLabel) = when (item.type) {
        ContentType.SUBJECT -> Triple(Icons.Default.School, MaterialTheme.colorScheme.primary, "Subject")
        ContentType.FOLDER, ContentType.SUBFOLDER -> Triple(Icons.Default.Folder, Color(0xFFD97706), "Folder")
        ContentType.CHAPTER -> Triple(Icons.Default.Bookmark, MaterialTheme.colorScheme.tertiary, "Chapter")
        ContentType.PDF_NOTE -> Triple(Icons.Default.PictureAsPdf, Color(0xFFC0392B), "PDF Notes")
        ContentType.QUESTION_BANK -> Triple(Icons.Default.HelpOutline, Color(0xFFE67E22), "Question Bank")
        ContentType.PYQ -> Triple(Icons.Default.HistoryEdu, Color(0xFF2980B9), "PYQ Paper")
        ContentType.SHORT_NOTE -> Triple(Icons.Default.NoteAlt, Color(0xFF27AE60), "Short Notes")
        ContentType.IMAGE -> Triple(Icons.Default.Image, Color(0xFF8E44AD), "Diagram")
        ContentType.YEAR -> Triple(Icons.Default.DateRange, MaterialTheme.colorScheme.primary, "Year")
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick() }
            .testTag("content_item_${item.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon container
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = iconTint.copy(alpha = 0.12f),
                modifier = Modifier.size(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(typeIcon, contentDescription = null, tint = iconTint, modifier = Modifier.size(26.dp))
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Info Column
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    if (isDownloaded) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            Icons.Default.CloudDone,
                            contentDescription = "Downloaded Offline",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                if (item.description.isNotEmpty()) {
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Metadata Row: Type pill + Price/Free pill + Status
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = typeLabel,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    if (!isFolderLike) {
                        if (item.price == 0) {
                            Surface(
                                color = Color(0xFFE8F5E9),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "FREE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1B873F),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        } else {
                            Surface(
                                color = if (hasAccess) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = if (hasAccess) "₹${item.price} • UNLOCKED" else "₹${item.price}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (hasAccess) Color(0xFF1B873F) else Color(0xFFE65100),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        if (pendingRequest != null) {
                            Surface(
                                color = Color(0xFFE3F2FD),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "Approval Pending",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1976D2),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Action / Status Indicator
            if (isFolderLike) {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = "Open Folder",
                    tint = MaterialTheme.colorScheme.outline
                )
            } else {
                if (hasAccess) {
                    FilledTonalButton(
                        onClick = onItemClick,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("read_btn_${item.id}")
                    ) {
                        Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Read", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                } else if (pendingRequest != null) {
                    FilledTonalButton(
                        onClick = onItemClick,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color(0xFFE3F2FD),
                            contentColor = Color(0xFF1565C0)
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Pending", fontSize = 11.sp)
                    }
                } else {
                    Button(
                        onClick = onItemClick,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("unlock_btn_${item.id}")
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Unlock", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Owner 3-Dots Menu
            if (isOwner) {
                var showMenu by remember { mutableStateOf(false) }
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Options")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit / Rename / Price") },
                            onClick = {
                                showMenu = false
                                onEdit()
                            },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete Item", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                showMenu = false
                                onDelete()
                            },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerContentEditorDialog(
    initialItem: ContentItemEntity?,
    parentId: String?,
    yearTag: String,
    subjectTag: String,
    onDismiss: () -> Unit,
    onSave: (ContentItemEntity) -> Unit
) {
    var title by remember { mutableStateOf(initialItem?.title ?: "") }
    var description by remember { mutableStateOf(initialItem?.description ?: "") }
    var selectedType by remember { mutableStateOf(initialItem?.type ?: ContentType.PDF_NOTE) }
    var priceText by remember { mutableStateOf((initialItem?.price ?: 99).toString()) }
    var isFree by remember { mutableStateOf((initialItem?.price ?: 99) == 0) }
    var pageCountText by remember { mutableStateOf((initialItem?.pageCount ?: 4).toString()) }
    var summaryText by remember { mutableStateOf(initialItem?.contentSummary ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialItem == null) "Add Study Material" else "Edit Content Details") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("editor_title_input")
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Short Description") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                // Type selector
                Text("Content Type:", style = MaterialTheme.typography.labelMedium)
                val typeOptions = listOf(
                    ContentType.PDF_NOTE to "PDF Notes",
                    ContentType.CHAPTER to "Chapter",
                    ContentType.FOLDER to "Folder",
                    ContentType.QUESTION_BANK to "Question Bank",
                    ContentType.PYQ to "PYQ Papers",
                    ContentType.SHORT_NOTE to "Short Notes",
                    ContentType.SUBJECT to "Subject"
                )

                Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    typeOptions.forEach { (type, label) ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(label, fontSize = 11.sp) },
                            modifier = Modifier.padding(end = 6.dp)
                        )
                    }
                }

                // Pricing Controls (Only for leaf items)
                if (selectedType !in listOf(ContentType.SUBJECT, ContentType.FOLDER, ContentType.SUBFOLDER, ContentType.CHAPTER)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Make this item FREE", style = MaterialTheme.typography.bodyMedium)
                        Switch(
                            checked = isFree,
                            onCheckedChange = {
                                isFree = it
                                if (it) priceText = "0"
                            }
                        )
                    }

                    if (!isFree) {
                        OutlinedTextField(
                            value = priceText,
                            onValueChange = { priceText = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Price in INR (₹) *") },
                            leadingIcon = { Text("₹", fontWeight = FontWeight.Bold) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("editor_price_input")
                        )
                    }

                    OutlinedTextField(
                        value = pageCountText,
                        onValueChange = { pageCountText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Page Count") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = summaryText,
                        onValueChange = { summaryText = it },
                        label = { Text("Key Clinical Takeaway / Shloka Reference") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) return@Button
                    val finalPrice = if (isFree) 0 else (priceText.toIntOrNull() ?: 0)
                    val finalPages = pageCountText.toIntOrNull() ?: 1

                    val item = initialItem?.copy(
                        title = title.trim(),
                        description = description.trim(),
                        type = selectedType,
                        price = finalPrice,
                        isLockedByDefault = finalPrice > 0,
                        pageCount = finalPages,
                        contentSummary = summaryText.trim(),
                        updatedAt = System.currentTimeMillis()
                    ) ?: ContentItemEntity(
                        id = "item_" + java.util.UUID.randomUUID().toString().take(8),
                        title = title.trim(),
                        description = description.trim(),
                        type = selectedType,
                        parentId = parentId,
                        yearTag = yearTag,
                        subjectTag = subjectTag,
                        price = finalPrice,
                        isLockedByDefault = finalPrice > 0,
                        pageCount = finalPages,
                        contentSummary = summaryText.trim()
                    )
                    onSave(item)
                },
                modifier = Modifier.testTag("save_content_btn")
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
