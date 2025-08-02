package com.vibecoder.app.data.dao

import androidx.room.*
import com.vibecoder.app.models.Customer
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

@Dao
interface CustomerDao {
    
    @Query("SELECT * FROM customers ORDER BY promiseDate ASC, name ASC")
    fun getAllCustomers(): Flow<List<Customer>>
    
    @Query("SELECT * FROM customers WHERE promiseDate = :date ORDER BY name ASC")
    fun getCustomersForDate(date: LocalDate): Flow<List<Customer>>
    
    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getCustomerById(id: String): Customer?
    
    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getCustomersSortedByName(): Flow<List<Customer>>
    
    @Query("SELECT * FROM customers ORDER BY amount DESC")
    fun getCustomersSortedByAmount(): Flow<List<Customer>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: Customer): Long
    
    @Update
    suspend fun updateCustomer(customer: Customer)
    
    @Delete
    suspend fun deleteCustomer(customer: Customer)
    
    @Query("DELETE FROM customers WHERE id = :customerId")
    suspend fun deleteCustomerById(customerId: String)
    
    @Query("SELECT COUNT(*) FROM customers")
    suspend fun getCustomerCount(): Int
    
    @Query("DELETE FROM customers")
    suspend fun deleteAllCustomers()
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomers(customers: List<Customer>)
    
    @Query("SELECT * FROM customers WHERE name LIKE :searchQuery OR phoneNumber LIKE :searchQuery")
    fun searchCustomers(searchQuery: String): Flow<List<Customer>>
}