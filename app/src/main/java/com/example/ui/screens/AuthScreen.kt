package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserRole
import com.example.ui.SwasthyaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    viewModel: SwasthyaViewModel
) {
    val context = LocalContext.current
    var isSignUp by remember { mutableStateOf(false) }

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(UserRole.STUDENT) }
    var selectedYear by remember { mutableStateOf("BAMS 1st Year") }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Brand Logo
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Spa,
                        contentDescription = "Swasthya Notes Logo",
                        tint = Color.White,
                        modifier = Modifier.size(42.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Swasthya Notes",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Accredited B.A.M.S. Digital Study Portal",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Tab for Sign In / Sign Up
            TabRow(
                selectedTabIndex = if (isSignUp) 1 else 0,
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = !isSignUp,
                    onClick = { isSignUp = false },
                    text = { Text("Sign In", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = isSignUp,
                    onClick = { isSignUp = true },
                    text = { Text("Sign Up", fontWeight = FontWeight.Bold) }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (isSignUp) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Full Name *") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text("Select Curriculum Year:", style = MaterialTheme.typography.labelSmall)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("BAMS 1st Year", "BAMS 2nd Year", "BAMS 3rd Year").forEach { yr ->
                                FilterChip(
                                    selected = selectedYear == yr,
                                    onClick = { selectedYear = yr },
                                    label = { Text(yr.replace("BAMS ", ""), fontSize = 11.sp) }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it.trim() },
                        label = { Text("Email or Phone Number *") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("auth_email_input")
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password *") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("auth_password_input")
                    )

                    if (!isSignUp) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = {
                                Toast.makeText(context, "Password reset instructions sent to $email", Toast.LENGTH_LONG).show()
                            }) {
                                Text("Forgot Password?", fontSize = 12.sp)
                            }
                        }
                    }

                    Button(
                        onClick = {
                            val userEmail = if (email.contains("@")) email else "$email@swasthyanotes.com"
                            val userName = if (name.isNotBlank()) name else userEmail.substringBefore("@").replaceFirstChar { it.uppercase() }
                            viewModel.login(userEmail, phone, selectedRole, selectedYear, userName)
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("auth_submit_btn"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (isSignUp) "Create Account" else "Sign In")
                    }

                    // Google Login button
                    OutlinedButton(
                        onClick = {
                            viewModel.login("google.student@swasthyanotes.com", "", UserRole.STUDENT, "BAMS 1st Year", "Google Verified Student")
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.AccountCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Continue with Google")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Quick Demo Role Switcher for seamless testing
            Text(
                text = "⚡ Instant Testing Accounts:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.outline
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        viewModel.switchUserRole(UserRole.STUDENT)
                        viewModel.navigateTo(com.example.ui.AppScreen.Home)
                    },
                    modifier = Modifier.weight(1f).testTag("quick_login_student"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                ) {
                    Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Student Login", fontSize = 12.sp)
                }

                Button(
                    onClick = {
                        viewModel.switchUserRole(UserRole.OWNER)
                        viewModel.navigateTo(com.example.ui.AppScreen.Home)
                    },
                    modifier = Modifier.weight(1f).testTag("quick_login_owner"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                ) {
                    Icon(Icons.Default.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Owner Login", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
