package com.example.mvp.data

data class User(
    val email: String,
    val role: UserRole,
    val name: String
)

data class LocalCredential(
    val email: String,
    val password: String,
    val name: String,
    val role: UserRole
)

enum class UserRole {
    TENANT, LANDLORD, CONTRACTOR
}

data class Message(
    val id: String,
    val text: String,
    val senderEmail: String,
    val senderName: String,
    val timestamp: String
)

data class Ticket(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val status: TicketStatus,
    val submittedBy: String,
    val assignedTo: String? = null,
    val aiDiagnosis: String? = null,
    val photos: List<String> = emptyList(),
    val createdAt: String,
    val scheduledDate: String? = null,
    val completedDate: String? = null,
    val rating: Float? = null,
    val assignedContractor: String? = null,
    val createdDate: String? = null,
    val priority: String? = null,
    val ticketNumber: String? = null,
    val messages: List<Message> = emptyList()
)

enum class TicketStatus {
    SUBMITTED, ASSIGNED, SCHEDULED, COMPLETED
}

data class Contractor(
    val id: String,
    val name: String,
    val company: String,
    val specialization: List<String>,
    val rating: Float,
    val distance: Float,
    val preferred: Boolean,
    val completedJobs: Int
)

data class Job(
    val id: String,
    val ticketId: String,
    val contractorId: String,
    val propertyAddress: String,
    val issueType: String,
    val date: String,
    val status: String,
    val cost: Int? = null,
    val duration: Int? = null,
    val rating: Float? = null
)

data class Notification(
    val id: String,
    val title: String,
    val message: String,
    val type: NotificationType,
    val timestamp: String,
    val isRead: Boolean = false,
    val relatedTicketId: String? = null,
    val relatedJobId: String? = null
)

enum class NotificationType {
    TICKET_CREATED,
    TICKET_ASSIGNED,
    TICKET_UPDATED,
    JOB_COMPLETED,
    SCHEDULE_REMINDER,
    MAINTENANCE_REMINDER,
    CONTRACTOR_MESSAGE,
    SYSTEM_ALERT
}

data class MaintenanceReminder(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val frequency: ReminderFrequency,
    val nextDueDate: String,
    val lastCompletedDate: String? = null,
    val propertyId: String? = null,
    val isActive: Boolean = true
)

enum class ReminderFrequency {
    WEEKLY,
    MONTHLY,
    QUARTERLY,
    BIANNUAL,
    ANNUAL,
    CUSTOM
}

data class Property(
    val id: String,
    val name: String,
    val address: String,
    val type: PropertyType,
    val ownerEmail: String,
    val tenantEmails: List<String> = emptyList(),
    val createdAt: String,
    val healthScore: Float? = null // 0-100 score based on maintenance history
)

enum class PropertyType {
    APARTMENT,
    HOUSE,
    CONDO,
    COMMERCIAL,
    OTHER
}

data class CostAnalytics(
    val totalSpent: Float,
    val averageCostPerTicket: Float,
    val costByCategory: Map<String, Float>,
    val monthlyTrend: List<MonthlyCost>,
    val topExpenses: List<ExpenseItem>
)

data class MonthlyCost(
    val month: String,
    val year: Int,
    val totalCost: Float,
    val ticketCount: Int
)

data class ExpenseItem(
    val description: String,
    val amount: Float,
    val category: String,
    val date: String
)

data class Budget(
    val id: String,
    val propertyId: String?,
    val category: String,
    val monthlyBudget: Float,
    val yearlyBudget: Float,
    val currentSpent: Float,
    val alertThreshold: Float = 0.8f, // Alert when 80% spent
    val isActive: Boolean = true
)

data class Document(
    val id: String,
    val name: String,
    val type: DocumentType,
    val url: String,
    val uploadedBy: String,
    val uploadedDate: String,
    val relatedTicketId: String? = null,
    val relatedPropertyId: String? = null,
    val size: Long? = null, // in bytes
    val description: String? = null
)

enum class DocumentType {
    INVOICE,
    RECEIPT,
    WARRANTY,
    CONTRACT,
    PHOTO,
    OTHER
}

data class EnhancedReview(
    val id: String,
    val contractorId: String,
    val ticketId: String,
    val jobId: String?,
    val rating: Float, // Overall rating 1-5
    val qualityRating: Float, // Work quality 1-5
    val timelinessRating: Float, // On-time completion 1-5
    val communicationRating: Float, // Communication 1-5
    val valueRating: Float, // Value for money 1-5
    val comment: String,
    val photos: List<String> = emptyList(),
    val reviewerEmail: String = "",
    val reviewerName: String = "",
    val timestamp: String = "",
    val contractorResponse: String? = null,
    val contractorResponseTimestamp: String? = null,
    val isVerified: Boolean = false,
    val createdAt: String = "",
    val updatedAt: String? = null
)

