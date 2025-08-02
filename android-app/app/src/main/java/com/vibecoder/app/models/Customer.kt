package com.vibecoder.app.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import java.util.UUID

@Entity(tableName = "customers")
@Serializable
data class Customer(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val phoneNumber: String,
    val amount: Double,
    val promiseDate: LocalDate,
    val notes: String = "",
    val nameEditable: Boolean = true,
    val phoneEditable: Boolean = true,
    val amountEditable: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "followups")
@Serializable
data class FollowUp(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val customerId: String,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val nextPromiseDate: LocalDate? = null
)

data class CustomerWithFollowUps(
    val customer: Customer,
    val followUps: List<FollowUp> = emptyList()
) {
    val followUpCount: Int get() = followUps.size
    
    val statusColor: FollowUpStatus get() = when (followUpCount) {
        in 0..5 -> FollowUpStatus.LOW
        in 6..10 -> FollowUpStatus.MEDIUM
        in 11..20 -> FollowUpStatus.HIGH
        else -> FollowUpStatus.CRITICAL
    }
}

enum class FollowUpStatus {
    LOW,      // 0-5 follow-ups: Light color
    MEDIUM,   // 6-10 follow-ups: Slightly darker  
    HIGH,     // 11-20 follow-ups: Even darker
    CRITICAL  // 20+ follow-ups: Dark red
}

enum class SortOption(val displayName: String) {
    DATE("Date-wise"),
    NAME("Alphabetical"),
    AMOUNT("Amount"),
    FOLLOWUP_COUNT("Follow-up Count")
}

@Serializable
data class NotificationTime(
    val id: String = UUID.randomUUID().toString(),
    val hour: Int,
    val minute: Int,
    val isEnabled: Boolean = true
) {
    val timeString: String get() = String.format("%02d:%02d", hour, minute)
}

data class ExportData(
    val customers: List<Customer>,
    val followUps: List<FollowUp>,
    val exportedAt: Long = System.currentTimeMillis()
)