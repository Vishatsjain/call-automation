package com.vibecoder.app.ui.screens

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.vibecoder.app.R
import com.vibecoder.app.models.NotificationTime
import com.vibecoder.app.ui.components.AddNotificationTimeDialog
import com.vibecoder.app.ui.components.ConfirmationDialog
import com.vibecoder.app.ui.components.LoadingIndicator
import com.vibecoder.app.ui.viewmodels.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val notificationTimes by viewModel.notificationTimes.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()
    
    var showAddTimeDialog by remember { mutableStateOf(false) }
    var showClearDataDialog by remember { mutableStateOf(false) }
    var showExportOptions by remember { mutableStateOf(false) }
    
    // File picker launchers
    val exportCSVLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.exportDataToCSV(
                onSuccess = { uri ->
                    // Share the exported file
                    val shareIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_STREAM, uri)
                        type = "text/csv"
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share CSV Export"))
                }
            )
        }
    }
    
    val exportExcelLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.exportDataToExcel(
                onSuccess = { uri ->
                    // Share the exported file
                    val shareIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_STREAM, uri)
                        type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share Excel Export"))
                }
            )
        }
    }
    
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.importDataFromFile(it) }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = stringResource(R.string.nav_settings),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        if (isLoading) {
            LoadingIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Dark Mode Section
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = stringResource(R.string.settings_dark_mode),
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }
                                
                                Switch(
                                    checked = isDarkMode,
                                    onCheckedChange = { viewModel.toggleDarkMode() }
                                )
                            }
                        }
                    }
                }
                
                // Notification Times Section
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = stringResource(R.string.settings_notification_times),
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }
                                
                                IconButton(
                                    onClick = { showAddTimeDialog = true }
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.settings_add_notification_time))
                                }
                            }
                            
                            if (notificationTimes.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                notificationTimes.forEach { time ->
                                    NotificationTimeItem(
                                        time = time,
                                        onDelete = { viewModel.removeNotificationTime(time.id) }
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Data Management Section
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = 12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Storage,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Data Management",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                            
                            // Export Data
                            OutlinedButton(
                                onClick = { showExportOptions = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.FileDownload, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(R.string.action_export))
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Import Data
                            OutlinedButton(
                                onClick = { importLauncher.launch("*/*") },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.FileUpload, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(R.string.action_import))
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Clear All Data
                            OutlinedButton(
                                onClick = { showClearDataDialog = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Icon(Icons.Default.DeleteForever, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Clear All Data")
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Dialogs
    if (showAddTimeDialog) {
        AddNotificationTimeDialog(
            onTimeSelected = { hour, minute ->
                viewModel.addNotificationTime(hour, minute)
                showAddTimeDialog = false
            },
            onDismiss = { showAddTimeDialog = false }
        )
    }
    
    if (showClearDataDialog) {
        ConfirmationDialog(
            title = "Clear All Data",
            message = "Are you sure you want to delete all customers and follow-ups? This action cannot be undone.",
            onConfirm = {
                viewModel.clearAllData()
                showClearDataDialog = false
            },
            onDismiss = { showClearDataDialog = false }
        )
    }
    
    if (showExportOptions) {
        AlertDialog(
            onDismissRequest = { showExportOptions = false },
            title = { Text("Export Format") },
            text = { Text("Choose the format for exporting your data:") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExportOptions = false
                        exportExcelLauncher.launch(Intent())
                    }
                ) {
                    Text("Excel")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showExportOptions = false
                        exportCSVLauncher.launch(Intent())
                    }
                ) {
                    Text("CSV")
                }
            }
        )
    }
    
    // Show messages
    LaunchedEffect(errorMessage, successMessage) {
        // In a real app, you'd show these in a Snackbar
        if (errorMessage != null || successMessage != null) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearMessages()
        }
    }
}

@Composable
private fun NotificationTimeItem(
    time: NotificationTime,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = time.timeString,
            style = MaterialTheme.typography.bodyLarge
        )
        
        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete time",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}