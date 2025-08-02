package com.vibecoder.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.vibecoder.app.R
import com.vibecoder.app.models.CustomerWithFollowUps
import com.vibecoder.app.models.SortOption
import com.vibecoder.app.ui.components.CustomerCard
import com.vibecoder.app.ui.components.EmptyState
import com.vibecoder.app.ui.components.LoadingIndicator
import com.vibecoder.app.ui.components.SortOptionsDialog
import com.vibecoder.app.ui.viewmodels.CustomerViewModel
import com.vibecoder.app.utils.DateUtils
import com.vibecoder.app.utils.PhoneUtils

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun CustomerListScreen(
    navController: NavController,
    viewModel: CustomerViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val callPermissionState = rememberPermissionState(android.Manifest.permission.CALL_PHONE)
    
    val todayCustomers by viewModel.todayCustomers.collectAsState()
    val allCustomers by viewModel.allCustomers.collectAsState()
    val sortOption by viewModel.sortOption.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    
    var showSortDialog by remember { mutableStateOf(false) }
    var showAllCustomers by remember { mutableStateOf(false) }
    var isInCallMode by remember { mutableStateOf(false) }
    var currentCallCustomerIndex by remember { mutableStateOf(0) }
    var showCallDialog by remember { mutableStateOf(false) }
    var selectedCustomer by remember { mutableStateOf<CustomerWithFollowUps?>(null) }
    
    val customersToShow = if (showAllCustomers) allCustomers else todayCustomers
    val todayDate = DateUtils.getCurrentDate()
    
    // Handle error messages
    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            // Show error snackbar or dialog
            viewModel.clearError()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (showAllCustomers) "All Customers" else "Today's Calls",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                if (!showAllCustomers) {
                    Text(
                        text = DateUtils.formatDateForDisplay(todayDate),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Row {
                // Toggle between today and all customers
                TextButton(
                    onClick = { showAllCustomers = !showAllCustomers }
                ) {
                    Text(if (showAllCustomers) "Today Only" else "View All")
                }
                
                // Sort button
                IconButton(onClick = { showSortDialog = true }) {
                    Icon(Icons.Default.Sort, contentDescription = stringResource(R.string.cd_sort_customers))
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Customer count
        Text(
            text = "${customersToShow.size} customers",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Content
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                isLoading -> {
                    LoadingIndicator(modifier = Modifier.align(Alignment.Center))
                }
                customersToShow.isEmpty() -> {
                    EmptyState(
                        message = if (showAllCustomers) "No customers added yet" else stringResource(R.string.message_no_customers_today),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(
                            items = customersToShow,
                            key = { it.customer.id }
                        ) { customerWithFollowUps ->
                            CustomerCard(
                                customerWithFollowUps = customerWithFollowUps,
                                onCallClick = { customer ->
                                    selectedCustomer = customer
                                    if (callPermissionState.status.isGranted) {
                                        showCallDialog = true
                                    } else {
                                        callPermissionState.launchPermissionRequest()
                                    }
                                },
                                onEditClick = { customer ->
                                    navController.navigate("edit_customer/${customer.customer.id}")
                                },
                                onDeleteClick = { customer ->
                                    viewModel.deleteCustomer(customer.customer)
                                }
                            )
                        }
                    }
                }
            }
            
            // Floating Action Button for call mode (only show for today's customers)
            if (!showAllCustomers && todayCustomers.isNotEmpty()) {
                FloatingActionButton(
                    onClick = {
                        if (!isInCallMode) {
                            // Start call mode
                            if (callPermissionState.status.isGranted) {
                                isInCallMode = true
                                currentCallCustomerIndex = 0
                                selectedCustomer = todayCustomers[0]
                                showCallDialog = true
                            } else {
                                callPermissionState.launchPermissionRequest()
                            }
                        } else {
                            // End call mode
                            isInCallMode = false
                            showCallDialog = false
                            selectedCustomer = null
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .size(64.dp),
                    shape = CircleShape,
                    containerColor = if (isInCallMode) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = if (isInCallMode) Icons.Default.CallEnd else Icons.Default.Call,
                        contentDescription = if (isInCallMode) stringResource(R.string.action_end_call) else stringResource(R.string.action_start_call),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
    
    // Sort Options Dialog
    if (showSortDialog) {
        SortOptionsDialog(
            currentSortOption = sortOption,
            onSortOptionSelected = { option ->
                viewModel.setSortOption(option)
                showSortDialog = false
            },
            onDismiss = { showSortDialog = false }
        )
    }
    
    // Call Dialog
    selectedCustomer?.let { customer ->
        if (showCallDialog) {
            CallDialog(
                customer = customer,
                onCallClick = {
                    PhoneUtils.makePhoneCall(context, customer.customer.phoneNumber)
                },
                onUpdateClick = { notes, nextDate ->
                    viewModel.addFollowUp(
                        customerId = customer.customer.id,
                        notes = notes,
                        nextPromiseDate = nextDate,
                        onSuccess = {
                            // Auto move to next customer in call mode
                            if (isInCallMode && todayCustomers.isNotEmpty()) {
                                currentCallCustomerIndex = (currentCallCustomerIndex + 1) % todayCustomers.size
                                selectedCustomer = todayCustomers[currentCallCustomerIndex]
                            } else {
                                showCallDialog = false
                                selectedCustomer = null
                            }
                        }
                    )
                },
                onDismiss = {
                    if (!isInCallMode) {
                        showCallDialog = false
                        selectedCustomer = null
                    }
                }
            )
        }
    }
    
    // Handle permission result
    LaunchedEffect(callPermissionState.status) {
        if (callPermissionState.status.isGranted && selectedCustomer != null) {
            showCallDialog = true
        }
    }
}