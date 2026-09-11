package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ContentItemEntity
import com.example.data.model.ContentType
import com.example.ui.SwasthyaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SwasthyaViewModel,
    onBack: () -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val userPermissions by viewModel.userPermissions.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var selectedFilter by remember { mutableStateOf("ALL") }

    val filteredResults = remember(searchResults, selectedFilter) {
        when (selectedFilter) {
            "YEAR_1" -> searchResults.filter { it.yearTag == "BAMS 1st Year" }
            "YEAR_2" -> searchResults.filter { it.yearTag == "BAMS 2nd Year" }
            "YEAR_3" -> searchResults.filter { it.yearTag == "BAMS 3rd Year" }
            "FREE" -> searchResults.filter { it.price == 0 }
            "PDFS" -> searchResults.filter { it.type == ContentType.PDF_NOTE }
            else -> searchResults
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = { Text("Search Year, Subject, Chapter, or PDF...") },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_input_field"),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = MaterialTheme.colorScheme.primary
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("search_back_btn")) {
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
        ) {
            // Filter Chips Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedFilter == "ALL",
                    onClick = { selectedFilter = "ALL" },
                    label = { Text("All (${searchResults.size})") }
                )
                FilterChip(
                    selected = selectedFilter == "YEAR_1",
                    onClick = { selectedFilter = "YEAR_1" },
                    label = { Text("1st Year") }
                )
                FilterChip(
                    selected = selectedFilter == "YEAR_2",
                    onClick = { selectedFilter = "YEAR_2" },
                    label = { Text("2nd Year") }
                )
                FilterChip(
                    selected = selectedFilter == "YEAR_3",
                    onClick = { selectedFilter = "YEAR_3" },
                    label = { Text("3rd Year") }
                )
                FilterChip(
                    selected = selectedFilter == "FREE",
                    onClick = { selectedFilter = "FREE" },
                    label = { Text("Free Content") }
                )
                FilterChip(
                    selected = selectedFilter == "PDFS",
                    onClick = { selectedFilter = "PDFS" },
                    label = { Text("PDF Notes Only") }
                )
            }

            if (searchQuery.isBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Search BAMS Academic Notes",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Type any keyword: Padartha, Samhita, Marma, Dravyaguna, Charaka, Jwara, Basti...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else if (filteredResults.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.SearchOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "No results found for '$searchQuery'",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Try searching by subject name or year tag.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredResults, key = { it.id }) { item ->
                        var hasAccess by remember { mutableStateOf(false) }

                        LaunchedEffect(item.id, userPermissions, currentUser?.id) {
                            hasAccess = viewModel.hasAccessToItem(item)
                        }

                        SearchResultItemCard(
                            item = item,
                            hasAccess = hasAccess,
                            onClick = {
                                if (item.type in listOf(ContentType.SUBJECT, ContentType.FOLDER, ContentType.CHAPTER, ContentType.YEAR)) {
                                    viewModel.drillDown(item)
                                    viewModel.navigateTo(com.example.ui.AppScreen.ContentBrowser)
                                } else {
                                    if (hasAccess) {
                                        viewModel.openSecureReader(item)
                                    } else {
                                        viewModel.openAccessRequestDialog(item)
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SearchResultItemCard(
    item: ContentItemEntity,
    hasAccess: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("search_result_${item.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        when (item.type) {
                            ContentType.PDF_NOTE -> Icons.Default.PictureAsPdf
                            ContentType.QUESTION_BANK -> Icons.Default.HelpOutline
                            ContentType.PYQ -> Icons.Default.HistoryEdu
                            ContentType.SHORT_NOTE -> Icons.Default.NoteAlt
                            ContentType.CHAPTER -> Icons.Default.Bookmark
                            ContentType.SUBJECT -> Icons.Default.School
                            else -> Icons.Default.Folder
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${item.yearTag} • ${item.subjectTag.ifEmpty { "Ayurveda" }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
                if (item.contentSummary.isNotEmpty()) {
                    Text(
                        text = item.contentSummary,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (item.price == 0 || hasAccess) {
                Surface(
                    color = Color(0xFFE8F5E9),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = if (item.price == 0) "FREE" else "UNLOCKED",
                        color = Color(0xFF1B873F),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            } else {
                Surface(
                    color = Color(0xFFFFF3E0),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "₹${item.price}",
                        color = Color(0xFFE65100),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}
