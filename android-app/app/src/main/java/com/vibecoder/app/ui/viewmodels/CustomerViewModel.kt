package com.vibecoder.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vibecoder.app.data.repository.CustomerRepository
import com.vibecoder.app.models.Customer
import com.vibecoder.app.models.CustomerWithFollowUps
import com.vibecoder.app.models.FollowUp
import com.vibecoder.app.models.SortOption
import com.vibecoder.app.utils.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import javax.inject.Inject

@HiltViewModel
class CustomerViewModel @Inject constructor(
    private val customerRepository: CustomerRepository
) : ViewModel() {

    private val _sortOption = MutableStateFlow(SortOption.DATE)
    val sortOption: StateFlow<SortOption> = _sortOption.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Get customers for today (main use case)
    val todayCustomers: StateFlow<List<CustomerWithFollowUps>> = 
        customerRepository.getCustomersWithFollowUpsForDate(DateUtils.getCurrentDate())
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    // Get all customers with sorting
    val allCustomers: StateFlow<List<CustomerWithFollowUps>> = 
        combine(
            customerRepository.getCustomersWithFollowUps(),
            _sortOption
        ) { customers, sort ->
            when (sort) {
                SortOption.DATE -> customers.sortedBy { it.customer.promiseDate }
                SortOption.NAME -> customers.sortedBy { it.customer.name }
                SortOption.AMOUNT -> customers.sortedByDescending { it.customer.amount }
                SortOption.FOLLOWUP_COUNT -> customers.sortedByDescending { it.followUpCount }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun setSortOption(option: SortOption) {
        _sortOption.value = option
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun addCustomer(
        name: String,
        phoneNumber: String,
        amount: Double,
        promiseDate: LocalDate,
        notes: String = "",
        nameEditable: Boolean = true,
        phoneEditable: Boolean = true,
        amountEditable: Boolean = true,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val customer = Customer(
                    name = name.trim(),
                    phoneNumber = phoneNumber.trim(),
                    amount = amount,
                    promiseDate = promiseDate,
                    notes = notes.trim(),
                    nameEditable = nameEditable,
                    phoneEditable = phoneEditable,
                    amountEditable = amountEditable
                )
                customerRepository.insertCustomer(customer)
                onSuccess()
            } catch (e: Exception) {
                val errorMsg = "Failed to add customer: ${e.message}"
                _errorMessage.value = errorMsg
                onError(errorMsg)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateCustomer(
        customer: Customer,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val updatedCustomer = customer.copy(updatedAt = System.currentTimeMillis())
                customerRepository.updateCustomer(updatedCustomer)
                onSuccess()
            } catch (e: Exception) {
                val errorMsg = "Failed to update customer: ${e.message}"
                _errorMessage.value = errorMsg
                onError(errorMsg)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteCustomer(
        customer: Customer,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                customerRepository.deleteCustomer(customer)
                onSuccess()
            } catch (e: Exception) {
                val errorMsg = "Failed to delete customer: ${e.message}"
                _errorMessage.value = errorMsg
                onError(errorMsg)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addFollowUp(
        customerId: String,
        notes: String,
        nextPromiseDate: LocalDate? = null,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val followUp = FollowUp(
                    customerId = customerId,
                    notes = notes.trim(),
                    nextPromiseDate = nextPromiseDate
                )
                customerRepository.addFollowUp(followUp)
                
                // Update customer's promise date if new date provided
                nextPromiseDate?.let { newDate ->
                    val customer = customerRepository.getCustomerWithFollowUps(customerId)?.customer
                    customer?.let {
                        val updatedCustomer = it.copy(
                            promiseDate = newDate,
                            updatedAt = System.currentTimeMillis()
                        )
                        customerRepository.updateCustomer(updatedCustomer)
                    }
                }
                
                onSuccess()
            } catch (e: Exception) {
                val errorMsg = "Failed to add follow-up: ${e.message}"
                _errorMessage.value = errorMsg
                onError(errorMsg)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getFollowUpsForCustomer(customerId: String): Flow<List<FollowUp>> {
        return customerRepository.getFollowUpsForCustomer(customerId)
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun refreshData() {
        // The StateFlow will automatically refresh due to the repository flows
        viewModelScope.launch {
            _isLoading.value = true
            kotlinx.coroutines.delay(500) // Small delay for visual feedback
            _isLoading.value = false
        }
    }
}