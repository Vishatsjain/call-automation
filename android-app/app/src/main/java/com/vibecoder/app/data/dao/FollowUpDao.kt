package com.vibecoder.app.data.dao

import androidx.room.*
import com.vibecoder.app.models.FollowUp
import kotlinx.coroutines.flow.Flow

@Dao
interface FollowUpDao {
    
    @Query("SELECT * FROM followups WHERE customerId = :customerId ORDER BY timestamp DESC")
    fun getFollowUpsForCustomer(customerId: String): Flow<List<FollowUp>>
    
    @Query("SELECT COUNT(*) FROM followups WHERE customerId = :customerId")
    suspend fun getFollowUpCountForCustomer(customerId: String): Int
    
    @Query("SELECT * FROM followups WHERE id = :id")
    suspend fun getFollowUpById(id: String): FollowUp?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFollowUp(followUp: FollowUp): Long
    
    @Update
    suspend fun updateFollowUp(followUp: FollowUp)
    
    @Delete
    suspend fun deleteFollowUp(followUp: FollowUp)
    
    @Query("DELETE FROM followups WHERE customerId = :customerId")
    suspend fun deleteFollowUpsForCustomer(customerId: String)
    
    @Query("DELETE FROM followups")
    suspend fun deleteAllFollowUps()
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFollowUps(followUps: List<FollowUp>)
    
    @Query("""
        SELECT * FROM followups 
        WHERE customerId = :customerId 
        ORDER BY timestamp DESC 
        LIMIT :limit
    """)
    suspend fun getLatestFollowUpsForCustomer(customerId: String, limit: Int): List<FollowUp>
    
    @Query("""
        SELECT customerId, COUNT(*) as count 
        FROM followups 
        GROUP BY customerId
    """)
    suspend fun getFollowUpCounts(): List<CustomerFollowUpCount>
}

data class CustomerFollowUpCount(
    val customerId: String,
    val count: Int
)