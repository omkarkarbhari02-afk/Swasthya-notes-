package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.ContentItemEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccessRequestDialog(
    item: ContentItemEntity,
    ownerUpiId: String,
    ownerUpiName: String,
    onDismiss: () -> Unit,
    onSubmitRequest: (utrNumber: String, screenshotUri: String, message: String) -> Unit
) {
    val context = LocalContext.current
    var utrNumber by remember { mutableStateOf("") }
    var userMessage by remember { mutableStateOf("") }
    var selectedScreenshotName by remember { mutableStateOf("payment_proof_${System.currentTimeMillis().toString().takeLast(4)}.jpg") }
    var hasAttachedScreenshot by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    var submissionSuccess by remember { mutableStateOf(false) }

    val upiPaymentUri = "upi://pay?pa=$ownerUpiId&pn=${ownerUpiName.replace(" ", "%20")}&am=${item.price}&tn=SwasthyaNotes_${item.id}"
    val qrBitmap = remember(upiPaymentUri) {
        QrCodeGenerator.generateUpiQrBitmap(upiPaymentUri, 480)
    }

    Dialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .testTag("access_request_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Request Content Access",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_request_dialog")) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                if (submissionSuccess) {
                    // Success View
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(32.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(72.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "Request Submitted Successfully",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Your request has been sent to the Owner. Access will only be granted after manual approval.",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Content remains locked until the Owner verifies your payment and activates your access.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(28.dp))

                        Button(
                            onClick = onDismiss,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("dismiss_success_btn")
                        ) {
                            Text("Done")
                        }
                    }
                } else {
                    // Scrollable Payment & Request Form
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Price Banner
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Study Material Fee",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    Text(
                                        text = "One-Time Access",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                                    )
                                }
                                Text(
                                    text = "₹${item.price}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // QR Code Box
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Scan Owner QR to Pay via GPay / PhonePe / Paytm",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.DarkGray,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Image(
                                    bitmap = qrBitmap.asImageBitmap(),
                                    contentDescription = "Owner UPI QR Code",
                                    modifier = Modifier
                                        .size(180.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = ownerUpiName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // UPI ID Copy Row
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Owner UPI ID",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                    Text(
                                        text = ownerUpiId,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                FilledTonalButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("Owner UPI ID", ownerUpiId)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "UPI ID copied to clipboard!", Toast.LENGTH_SHORT).show()
                                    },
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("copy_upi_btn")
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Copy")
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Payment Instructions
                        Text(
                            text = "Payment Instructions:",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        val steps = listOf(
                            "1. Scan the QR code or copy UPI ID into any UPI app.",
                            "2. Complete payment of ₹${item.price}.",
                            "3. Copy the 12-digit UTR / Bank Reference Number from payment receipt.",
                            "4. Attach screenshot of payment and submit request below."
                        )
                        steps.forEach { step ->
                            Text(
                                text = step,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Form: UTR Number
                        OutlinedTextField(
                            value = utrNumber,
                            onValueChange = { utrNumber = it.trim() },
                            label = { Text("UTR / Transaction ID (12 Digits) *") },
                            leadingIcon = { Icon(Icons.Default.ConfirmationNumber, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("utr_input_field")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Screenshot proof picker
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (hasAttachedScreenshot) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    hasAttachedScreenshot = true
                                    Toast.makeText(context, "Payment screenshot attached: $selectedScreenshotName", Toast.LENGTH_SHORT).show()
                                }
                                .testTag("attach_screenshot_btn")
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    if (hasAttachedScreenshot) Icons.Default.CheckCircle else Icons.Default.AddPhotoAlternate,
                                    contentDescription = null,
                                    tint = if (hasAttachedScreenshot) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (hasAttachedScreenshot) "Screenshot Attached" else "Upload Payment Screenshot *",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = if (hasAttachedScreenshot) selectedScreenshotName else "Tap to attach screenshot of UPI transaction",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                                if (hasAttachedScreenshot) {
                                    IconButton(onClick = { hasAttachedScreenshot = false }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Optional Message
                        OutlinedTextField(
                            value = userMessage,
                            onValueChange = { userMessage = it },
                            label = { Text("Optional Message for Owner") },
                            placeholder = { Text("e.g. Paid via Google Pay for BAMS study notes") },
                            maxLines = 3,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("optional_message_input")
                        )

                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    // Action Button
                    Button(
                        onClick = {
                            if (utrNumber.isBlank()) {
                                Toast.makeText(context, "Please enter the UTR / Transaction ID", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (!hasAttachedScreenshot) {
                                Toast.makeText(context, "Please attach payment screenshot", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            isSubmitting = true
                            onSubmitRequest(
                                utrNumber,
                                selectedScreenshotName,
                                userMessage
                            )
                            isSubmitting = false
                            submissionSuccess = true
                        },
                        enabled = !isSubmitting,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("submit_access_request_btn"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Request Access",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}
