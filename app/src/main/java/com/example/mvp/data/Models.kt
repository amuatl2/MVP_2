package com.example.mvp.data

data class User(
    val email: String,
    val role: UserRole,
    val name: String
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

