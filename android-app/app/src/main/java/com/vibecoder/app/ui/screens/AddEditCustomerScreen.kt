package com.vibecoder.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.vibecoder.app.R
import com.vibecoder.app.models.Customer
import com.vibecoder.app.ui.components.DatePickerDialog
import com.vibecoder.app.ui.components.LoadingIndicator
import com.vibecoder.app.ui.viewmodels.CustomerViewModel
import com.vibecoder.app.utils.DateUtils
import com.vibecoder.app.utils.PhoneUtils
import kotlinx.datetime.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCustomerScreen(
    navController: NavController,
    customerId: String? = null,
    viewModel: CustomerViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val isEditing = customerId != null
    val isLoading by viewModel.isLoading.collectAsState()
    
    // Form state
    var name by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var promiseDate by remember { mutableStateOf(DateUtils.getCurrentDate()) }
    var notes by remember { mutableStateOf("") }
    var nameEditable by remember { mutableStateOf(true) }
    var phoneEditable by remember { mutableStateOf(true) }
    var amountEditable by remember { mutableStateOf(true) }
    
    // UI state
    var showDatePicker by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var amountError by remember { mutableStateOf<String?>(null) }
    
    // Load customer data if editing
    LaunchedEffect(customerId) {
        if (customerId != null) {
            // In a real implementation, you'd load the customer from the repository
            // For now, we'll use placeholder data
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.navigateUp() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            
            Text(
                text = if (isEditing) "Edit Customer" else stringResource(R.string.nav_add_customer),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Name Field
        OutlinedTextField(
            value = name,
            onValueChange = { 
                name = it
                nameError = null
            },
            label = { Text(stringResource(R.string.label_name)) },
            enabled = nameEditable,
            isError = nameError != null,
            supportingText = nameError?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth()
        )
        
        // Name Editable Toggle
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Switch(
                checked = nameEditable,
                onCheckedChange = { nameEditable = it }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (nameEditable) stringResource(R.string.toggle_edit_info) else stringResource(R.string.toggle_lock_info),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Phone Number Field
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { 
                phoneNumber = it
                phoneError = null
            },
            label = { Text(stringResource(R.string.label_phone)) },
            enabled = phoneEditable,
            isError = phoneError != null,
            supportingText = phoneError?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth()
        )
        
        // Phone Editable Toggle
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Switch(
                checked = phoneEditable,
                onCheckedChange = { phoneEditable = it }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (phoneEditable) stringResource(R.string.toggle_edit_info) else stringResource(R.string.toggle_lock_info),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Amount Field
        OutlinedTextField(
            value = amount,
            onValueChange = { 
                amount = it
                amountError = null
            },
            label = { Text(stringResource(R.string.label_amount)) },
            enabled = amountEditable,
            isError = amountError != null,
            supportingText = amountError?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )
        
        // Amount Editable Toggle
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Switch(
                checked = amountEditable,
                onCheckedChange = { amountEditable = it }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (amountEditable) stringResource(R.string.toggle_edit_info) else stringResource(R.string.toggle_lock_info),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Promise Date Field
        OutlinedTextField(
            value = DateUtils.formatDateForDisplay(promiseDate),
            onValueChange = { },
            label = { Text(stringResource(R.string.label_promise_date)) },
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { showDatePicker = true }) {
                    Icon(Icons.Default.DateRange, contentDescription = "Select date")
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Notes Field
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text(stringResource(R.string.label_notes)) },
            minLines = 3,
            maxLines = 5,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Save Button
        Button(
            onClick = {
                // Validate form
                var hasErrors = false
                
                if (name.isBlank()) {
                    nameError = context.getString(R.string.error_name_required)
                    hasErrors = true
                }
                
                if (phoneNumber.isBlank()) {
                    phoneError = context.getString(R.string.error_phone_required)
                    hasErrors = true
                } else if (!PhoneUtils.isValidPhoneNumber(phoneNumber)) {
                    phoneError = context.getString(R.string.error_invalid_phone)
                    hasErrors = true
                }
                
                if (amount.isBlank()) {
                    amountError = context.getString(R.string.error_amount_required)
                    hasErrors = true
                } else {
                    try {
                        amount.toDouble()
                    } catch (e: NumberFormatException) {
                        amountError = context.getString(R.string.error_invalid_amount)
                        hasErrors = true
                    }
                }
                
                if (!hasErrors) {
                    if (isEditing && customerId != null) {
                        // Update existing customer
                        val customer = Customer(
                            id = customerId,
                            name = name.trim(),
                            phoneNumber = phoneNumber.trim(),
                            amount = amount.toDouble(),
                            promiseDate = promiseDate,
                            notes = notes.trim(),
                            nameEditable = nameEditable,
                            phoneEditable = phoneEditable,
                            amountEditable = amountEditable
                        )
                        viewModel.updateCustomer(
                            customer = customer,
                            onSuccess = { navController.navigateUp() }
                        )
                    } else {
                        // Add new customer
                        viewModel.addCustomer(
                            name = name.trim(),
                            phoneNumber = phoneNumber.trim(),
                            amount = amount.toDouble(),
                            promiseDate = promiseDate,
                            notes = notes.trim(),
                            nameEditable = nameEditable,
                            phoneEditable = phoneEditable,
                            amountEditable = amountEditable,
                            onSuccess = { navController.navigateUp() }
                        )
                    }
                }
            },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(
                    text = if (isEditing) stringResource(R.string.action_update) else stringResource(R.string.action_save)
                )
            }
        }
        
        // Cancel Button
        if (isEditing) {
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedButton(
                onClick = { navController.navigateUp() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    }
    
    // Date Picker Dialog
    if (showDatePicker) {
        DatePickerDialog(
            initialDate = promiseDate,
            onDateSelected = { date ->
                promiseDate = date
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}