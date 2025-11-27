package com.example.mvp.ui.screens

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.fragment.app.FragmentActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mvp.utils.SecurityManager
import com.example.mvp.utils.ThemeManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    isDarkMode: Boolean = false,
    onDarkModeToggle: (Boolean) -> Unit = {},
    onExportTickets: () -> Unit = {},
    onExportJobs: () -> Unit = {},
    onExportAnalytics: () -> Unit = {}
) {
    val context = LocalContext.current
    var showBiometricDialog by remember { mutableStateOf(false) }
    var show2FADialog by remember { mutableStateOf(false) }
    var biometricEnabled by remember { mutableStateOf(SecurityManager.isBiometricEnabled(context)) }
    var twoFAEnabled by remember { mutableStateOf(SecurityManager.is2FAEnabled(context)) }
    val biometricAvailable = remember { SecurityManager.isBiometricAvailable(context) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Appearance Section
            item {
                Text(
                    text = "Appearance",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            item {
                SettingsItem(
                    title = "Dark Mode",
                    description = "Switch between light and dark theme",
                    icon = Icons.Default.Settings,
                    trailing = {
                        Switch(
                            checked = isDarkMode,
                            onCheckedChange = onDarkModeToggle
                        )
                    }
                )
            }
            
            item {
                Divider(modifier = Modifier.padding(vertical = 8.dp))
            }
            
            // Security Section
            item {
                Text(
                    text = "Security",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            item {
                SettingsItem(
                    title = "Biometric Authentication",
                    description = if (biometricAvailable) {
                        "Use fingerprint or face to unlock"
                    } else {
                        "Not available on this device"
                    },
                    icon = Icons.Default.Info,
                    enabled = biometricAvailable,
                    trailing = {
                        Switch(
                            checked = biometricEnabled,
                            onCheckedChange = { enabled ->
                                if (enabled && biometricAvailable) {
                                    showBiometricDialog = true
                                } else {
                                    SecurityManager.setBiometricEnabled(context, enabled)
                                    biometricEnabled = enabled
                                }
                            }
                        )
                    }
                )
            }
            
            item {
                SettingsItem(
                    title = "Two-Factor Authentication",
                    description = "Add an extra layer of security",
                    icon = Icons.Default.Lock,
                    trailing = {
                        Switch(
                            checked = twoFAEnabled,
                            onCheckedChange = { enabled ->
                                SecurityManager.set2FAEnabled(context, enabled)
                                twoFAEnabled = enabled
                                if (enabled) {
                                    show2FADialog = true
                                }
                            }
                        )
                    }
                )
            }
            
            item {
                Divider(modifier = Modifier.padding(vertical = 8.dp))
            }
            
            // Export Section
            item {
                Text(
                    text = "Export Data",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            item {
                SettingsItem(
                    title = "Export Tickets",
                    description = "Export all tickets to CSV",
                    icon = Icons.Default.Share,
                    onClick = onExportTickets
                )
            }
            
            item {
                SettingsItem(
                    title = "Export Jobs",
                    description = "Export all jobs to CSV",
                    icon = Icons.Default.Share,
                    onClick = onExportJobs
                )
            }
            
            item {
                SettingsItem(
                    title = "Export Analytics",
                    description = "Export analytics report to CSV",
                    icon = Icons.Default.Share,
                    onClick = onExportAnalytics
                )
            }
        }
    }
    
    // Biometric Authentication Dialog
    if (showBiometricDialog) {
        AlertDialog(
            onDismissRequest = { showBiometricDialog = false },
            title = { Text("Enable Biometric Authentication") },
            text = {
                Text("You'll need to authenticate with your fingerprint or face to enable this feature.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val activity = context as? FragmentActivity
                        if (activity != null) {
                            SecurityManager.authenticateWithBiometric(
                                activity = activity,
                                onSuccess = {
                                    SecurityManager.setBiometricEnabled(context, true)
                                    biometricEnabled = true
                                    showBiometricDialog = false
                                },
                                onError = { error ->
                                    // Show error
                                    showBiometricDialog = false
                                }
                            )
                        }
                    }
                ) {
                    Text("Enable")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBiometricDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // 2FA Dialog
    if (show2FADialog) {
        var code by remember { mutableStateOf("") }
        val generatedCode = remember { SecurityManager.generate2FACode() }
        
        AlertDialog(
            onDismissRequest = { show2FADialog = false },
            title = { Text("Two-Factor Authentication") },
            text = {
                Column {
                    Text("Your 2FA code is:")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = generatedCode,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Save this code securely. You'll need it for future logins.")
                }
            },
            confirmButton = {
                TextButton(onClick = { show2FADialog = false }) {
                    Text("Got it")
                }
            }
        )
    }
}

@Composable
fun SettingsItem(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    val clickableModifier = if (onClick != null && enabled) {
        modifier.fillMaxWidth().clickable(onClick = onClick)
    } else {
        modifier.fillMaxWidth()
    }
    
    Card(
        modifier = clickableModifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(24.dp),
                tint = if (enabled) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                }
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = if (enabled) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    }
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (enabled) {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    }
                )
            }
            if (trailing != null) {
                Spacer(modifier = Modifier.width(8.dp))
                trailing()
            }
        }
    }
}

