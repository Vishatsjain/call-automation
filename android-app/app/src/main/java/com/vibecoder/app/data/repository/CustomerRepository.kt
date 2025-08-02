package com.vibecoder.app.data.repository

import com.vibecoder.app.data.dao.CustomerDao
import com.vibecoder.app.data.dao.FollowUpDao
import com.vibecoder.app.models.Customer
import com.vibecoder.app.models.CustomerWithFollowUps
import com.vibecoder.app.models.FollowUp
import com.vibecoder.app.models.SortOption
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomerRepository @Inject constructor(
    private val customerDao: CustomerDao,
    private val followUpDao: FollowUpDao
) {
    
    fun getAllCustomers(): Flow<List<Customer>> = customerDao.getAllCustomers()
    
    fun getCustomersForDate(date: LocalDate): Flow<List<Customer>> = 
        customerDao.getCustomersForDate(date)
    
    fun getCustomersWithFollowUps(): Flow<List<CustomerWithFollowUps>> {
        return combine(
            customerDao.getAllCustomers(),
            followUpDao.getFollowUpCounts()
        ) { customers, followUpCounts ->
            val followUpMap = followUpCounts.associateBy { it.customerId }
            customers.map { customer ->
                CustomerWithFollowUps(
                    customer = customer,
                    followUps = emptyList() // We'll load individual follow-ups when needed
                )
            }
        }
    }
    
    fun getCustomersWithFollowUpsForDate(date: LocalDate): Flow<List<CustomerWithFollowUps>> {
        return combine(
            customerDao.getCustomersForDate(date),
            followUpDao.getFollowUpCounts()
        ) { customers, followUpCounts ->
            val followUpMap = followUpCounts.associateBy { it.customerId }
            customers.map { customer ->
                CustomerWithFollowUps(
                    customer = customer,
                    followUps = emptyList()
                )
            }
        }
    }
    
    suspend fun getCustomerWithFollowUps(customerId: String): CustomerWithFollowUps? {
        val customer = customerDao.getCustomerById(customerId) ?: return null
        return CustomerWithFollowUps(
            customer = customer,
            followUps = emptyList()
        )
    }
    
    fun getFollowUpsForCustomer(customerId: String): Flow<List<FollowUp>> =
        followUpDao.getFollowUpsForCustomer(customerId)
    
    suspend fun insertCustomer(customer: Customer) = customerDao.insertCustomer(customer)
    
    suspend fun updateCustomer(customer: Customer) = customerDao.updateCustomer(customer)
    
    suspend fun deleteCustomer(customer: Customer) {
        followUpDao.deleteFollowUpsForCustomer(customer.id)
        customerDao.deleteCustomer(customer)
    }
    
    suspend fun addFollowUp(followUp: FollowUp) = followUpDao.insertFollowUp(followUp)
    
    suspend fun updateFollowUp(followUp: FollowUp) = followUpDao.updateFollowUp(followUp)
    
    suspend fun deleteFollowUp(followUp: FollowUp) = followUpDao.deleteFollowUp(followUp)
    
    fun getSortedCustomers(sortOption: SortOption): Flow<List<Customer>> {
        return when (sortOption) {
            SortOption.DATE -> customerDao.getAllCustomers()
            SortOption.NAME -> customerDao.getCustomersSortedByName()
            SortOption.AMOUNT -> customerDao.getCustomersSortedByAmount()
            SortOption.FOLLOWUP_COUNT -> {
                // This would require a more complex query joining customers and follow-ups
                customerDao.getAllCustomers()
            }
        }
    }
    
    suspend fun getFollowUpCountForCustomer(customerId: String): Int =
        followUpDao.getFollowUpCountForCustomer(customerId)
    
    suspend fun searchCustomers(query: String): Flow<List<Customer>> =
        customerDao.searchCustomers("%$query%")
    
    suspend fun importData(customers: List<Customer>, followUps: List<FollowUp>) {
        customerDao.insertCustomers(customers)
        followUpDao.insertFollowUps(followUps)
    }
    
    suspend fun clearAllData() {
        followUpDao.deleteAllFollowUps()
        customerDao.deleteAllCustomers()
    }
}