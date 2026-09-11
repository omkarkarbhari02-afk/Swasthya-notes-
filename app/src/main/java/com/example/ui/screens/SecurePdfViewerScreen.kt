package com.example.ui.screens

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.view.WindowManager
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ContentItemEntity
import com.example.ui.SwasthyaViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurePdfViewerScreen(
    item: ContentItemEntity,
    viewModel: SwasthyaViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val scope = rememberCoroutineScope()
    val currentUser by viewModel.currentUser.collectAsState()

    var pageBitmaps by remember { mutableStateOf<List<Bitmap>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isDownloadingOffline by remember { mutableStateOf(false) }
    var isDownloadedOffline by remember { mutableStateOf(false) }

    // Check offline status
    val offlineDownloads by viewModel.offlineDownloads.collectAsState()
    LaunchedEffect(offlineDownloads, item.id) {
        isDownloadedOffline = offlineDownloads.any { it.contentItemId == item.id }
    }

    // CRITICAL SECURITY REQUIREMENT:
    // Block screenshots and screen recording with Android WindowManager FLAG_SECURE
    DisposableEffect(Unit) {
        activity?.window?.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    // Load and render PDF pages in background using native PdfRenderer
    LaunchedEffect(item.id) {
        isLoading = true
        errorMessage = null
        try {
            val pdfBytes = viewModel.getDecryptedPdfBytes(item)
            if (pdfBytes == null) {
                errorMessage = "Unable to decrypt or access this document. Access may have been revoked."
                isLoading = false
                return@LaunchedEffect
            }

            withContext(Dispatchers.IO) {
                val tempFile = File(context.cacheDir, "secure_render_${item.id}.pdf")
                FileOutputStream(tempFile).use { it.write(pdfBytes) }

                val pfd = ParcelFileDescriptor.open(tempFile, ParcelFileDescriptor.MODE_READ_ONLY)
                val renderer = PdfRenderer(pfd)
                val bitmaps = mutableListOf<Bitmap>()

                val renderScale = 2.0f // High-DPI crisp rendering
                for (i in 0 until renderer.pageCount) {
                    val page = renderer.openPage(i)
                    val width = (page.width * renderScale).toInt()
                    val height = (page.height * renderScale).toInt()
                    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    bitmaps.add(bitmap)
                    page.close()
                }

                renderer.close()
                pfd.close()
                tempFile.delete() // Clean up temporary decrypted file immediately

                withContext(Dispatchers.Main) {
                    pageBitmaps = bitmaps
                    isLoading = false
                }
            }
        } catch (e: Exception) {
            errorMessage = "Failed to render secure PDF: ${e.localizedMessage}"
            isLoading = false
        }
    }

    // Watermark string showing student's credentials
    val studentName = currentUser?.name ?: "Student"
    val studentEmail = currentUser?.email ?: "student@swasthyanotes.com"
    val watermarkText = "CONFIDENTIAL • $studentName • $studentEmail • SWASTHYA NOTES PROTECTED"

    // Zoom and Pan state
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
        scale = (scale * zoomChange).coerceIn(1f, 3.5f)
        offset = if (scale == 1f) Offset.Zero else offset + panChange
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "DRM PROTECTED",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${pageBitmaps.size} Pages • Screenshot Blocked",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("viewer_back_btn")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Reset Zoom Button
                    if (scale > 1f) {
                        IconButton(onClick = { scale = 1f; offset = Offset.Zero }) {
                            Icon(Icons.Default.ZoomOutMap, contentDescription = "Reset Zoom")
                        }
                    }

                    // Offline Download Button
                    IconButton(
                        onClick = {
                            if (isDownloadedOffline) {
                                Toast.makeText(context, "Already saved securely in offline vault", Toast.LENGTH_SHORT).show()
                            } else {
                                isDownloadingOffline = true
                                viewModel.downloadItemOffline(item) { success ->
                                    isDownloadingOffline = false
                                    if (success) {
                                        Toast.makeText(context, "Encrypted with AES-256 & saved for offline reading!", Toast.LENGTH_LONG).show()
                                    } else {
                                        Toast.makeText(context, "Offline download failed or access restricted", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        },
                        modifier = Modifier.testTag("download_offline_btn")
                    ) {
                        if (isDownloadingOffline) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(
                                if (isDownloadedOffline) Icons.Default.CloudDone else Icons.Default.DownloadForOffline,
                                contentDescription = "Download for Offline",
                                tint = if (isDownloadedOffline) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFF202422))
        ) {
            when {
                isLoading -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Decrypting Secure PDF...",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Verifying AES-256 keys and watermarks",
                            color = Color.LightGray,
                            fontSize = 12.sp
                        )
                    }
                }
                errorMessage != null -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Access Restricted",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.LightGray
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(onClick = onBack) {
                            Text("Go Back")
                        }
                    }
                }
                else -> {
                    val textMeasurer = rememberTextMeasurer()

                    // Document Reader Scrollable Pages
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                translationX = offset.x
                                translationY = offset.y
                            }
                            .transformable(state = transformableState)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        items(pageBitmaps.size) { index ->
                            val bitmap = pageBitmaps[index]

                            Card(
                                shape = RoundedCornerShape(8.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentHeight()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .drawWithContent {
                                            drawContent() // Draw PDF page bitmap first

                                            // Draw diagonal security watermarks across the page
                                            val stepX = 260.dp.toPx()
                                            val stepY = 180.dp.toPx()
                                            val textStyle = TextStyle(
                                                color = Color(0x38BA1A1A), // Light transparent red
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )

                                            for (x in -200..size.width.toInt() + 200 step stepX.toInt()) {
                                                for (y in -200..size.height.toInt() + 200 step stepY.toInt()) {
                                                    rotate(degrees = -30f, pivot = Offset(x.toFloat(), y.toFloat())) {
                                                        drawText(
                                                            textMeasurer = textMeasurer,
                                                            text = watermarkText,
                                                            topLeft = Offset(x.toFloat(), y.toFloat()),
                                                            style = textStyle
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                ) {
                                    Image(
                                        bitmap = bitmap.asImageBitmap(),
                                        contentDescription = "PDF Page ${index + 1}",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .wrapContentHeight()
                                    )
                                }
                            }

                            // Page Counter pill
                            Surface(
                                color = Color.Black.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Text(
                                    text = "Page ${index + 1} of ${pageBitmaps.size}",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
                                )
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }
                }
            }
        }
    }
}
