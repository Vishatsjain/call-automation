package com.vibecoder.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vibecoder.app.data.repository.CustomerRepository
import com.vibecoder.app.data.repository.PreferencesRepository
import com.vibecoder.app.models.ExportData
import com.vibecoder.app.models.NotificationTime
import com.vibecoder.app.utils.FileUtils
import com.vibecoder.app.workers.NotificationScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val customerRepository: CustomerRepository,
    private val notificationScheduler: NotificationScheduler,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val isDarkMode: StateFlow<Boolean> = preferencesRepository.isDarkMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val notificationTimes: StateFlow<List<NotificationTime>> = preferencesRepository.notificationTimes
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    fun toggleDarkMode() {
        viewModelScope.launch {
            val currentMode = isDarkMode.value
            preferencesRepository.setDarkMode(!currentMode)
        }
    }

    fun addNotificationTime(
        hour: Int,
        minute: Int,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                val newTime = NotificationTime(hour = hour, minute = minute)
                preferencesRepository.addNotificationTime(newTime)
                
                // Reschedule notifications
                val updatedTimes = notificationTimes.value + newTime
                notificationScheduler.scheduleNotifications(updatedTimes)
                
                onSuccess()
            } catch (e: Exception) {
                val errorMsg = "Failed to add notification time: ${e.message}"
                _errorMessage.value = errorMsg
                onError(errorMsg)
            }
        }
    }

    fun removeNotificationTime(
        timeId: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                preferencesRepository.removeNotificationTime(timeId)
                notificationScheduler.cancelNotification(timeId)
                
                // Reschedule remaining notifications
                val updatedTimes = notificationTimes.value.filter { it.id != timeId }
                notificationScheduler.scheduleNotifications(updatedTimes)
                
                onSuccess()
            } catch (e: Exception) {
                val errorMsg = "Failed to remove notification time: ${e.message}"
                _errorMessage.value = errorMsg
                onError(errorMsg)
            }
        }
    }

    fun updateNotificationTime(
        updatedTime: NotificationTime,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                preferencesRepository.updateNotificationTime(updatedTime)
                
                // Reschedule notifications
                notificationScheduler.scheduleNotifications(notificationTimes.value)
                
                onSuccess()
            } catch (e: Exception) {
                val errorMsg = "Failed to update notification time: ${e.message}"
                _errorMessage.value = errorMsg
                onError(errorMsg)
            }
        }
    }

    fun exportDataToCSV(
        onSuccess: (Uri) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                // Get all data
                val customers = mutableListOf<Customer>()
                val followUps = mutableListOf<FollowUp>()
                
                customerRepository.getAllCustomers().first().forEach { customer ->
                    customers.add(customer)
                    customerRepository.getFollowUpsForCustomer(customer.id).first().forEach { followUp ->
                        followUps.add(followUp)
                    }
                }
                
                val exportData = ExportData(customers, followUps)
                val uri = FileUtils.exportToCSV(context, exportData)
                
                if (uri != null) {
                    _successMessage.value = "Data exported successfully"
                    onSuccess(uri)
                } else {
                    val errorMsg = "Failed to export data"
                    _errorMessage.value = errorMsg
                    onError(errorMsg)
                }
            } catch (e: Exception) {
                val errorMsg = "Export failed: ${e.message}"
                _errorMessage.value = errorMsg
                onError(errorMsg)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun exportDataToExcel(
        onSuccess: (Uri) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                // Get all data
                val customers = mutableListOf<Customer>()
                val followUps = mutableListOf<FollowUp>()
                
                customerRepository.getAllCustomers().first().forEach { customer ->
                    customers.add(customer)
                    customerRepository.getFollowUpsForCustomer(customer.id).first().forEach { followUp ->
                        followUps.add(followUp)
                    }
                }
                
                val exportData = ExportData(customers, followUps)
                val uri = FileUtils.exportToExcel(context, exportData)
                
                if (uri != null) {
                    _successMessage.value = "Data exported successfully"
                    onSuccess(uri)
                } else {
                    val errorMsg = "Failed to export data"
                    _errorMessage.value = errorMsg
                    onError(errorMsg)
                }
            } catch (e: Exception) {
                val errorMsg = "Export failed: ${e.message}"
                _errorMessage.value = errorMsg
                onError(errorMsg)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun importDataFromFile(
        uri: Uri,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                val exportData = when {
                    uri.toString().endsWith(".csv", ignoreCase = true) -> 
                        FileUtils.importFromCSV(context, uri)
                    uri.toString().endsWith(".xlsx", ignoreCase = true) -> 
                        FileUtils.importFromExcel(context, uri)
                    else -> null
                }
                
                if (exportData != null) {
                    customerRepository.importData(exportData.customers, exportData.followUps)
                    _successMessage.value = "Data imported successfully"
                    onSuccess()
                } else {
                    val errorMsg = "Failed to import data. Invalid file format."
                    _errorMessage.value = errorMsg
                    onError(errorMsg)
                }
            } catch (e: Exception) {
                val errorMsg = "Import failed: ${e.message}"
                _errorMessage.value = errorMsg
                onError(errorMsg)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearAllData(
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                customerRepository.clearAllData()
                _successMessage.value = "All data cleared successfully"
                onSuccess()
            } catch (e: Exception) {
                val errorMsg = "Failed to clear data: ${e.message}"
                _errorMessage.value = errorMsg
                onError(errorMsg)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }
}

// Extension to get first element from Flow synchronously
private suspend fun <T> Flow<T>.first(): T {
    var result: T? = null
    collect { value ->
        result = value
        return@collect
    }
    return result!!
}