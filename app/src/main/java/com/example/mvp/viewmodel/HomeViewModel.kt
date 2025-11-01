package com.example.mvp.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.mvp.auth.FirebaseAuthManager
import com.example.mvp.data.*

class HomeViewModel : ViewModel() {
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _tickets = MutableStateFlow(MockData.mockTickets)
    val tickets: StateFlow<List<Ticket>> = _tickets.asStateFlow()

    private val _contractors = MutableStateFlow(MockData.mockContractors)
    val contractors: StateFlow<List<Contractor>> = _contractors.asStateFlow()

    private val _jobs = MutableStateFlow(MockData.mockJobs)
    val jobs: StateFlow<List<Job>> = _jobs.asStateFlow()

    var rememberMe by mutableStateOf(false)
        private set

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    fun login(email: String, password: String, role: UserRole, remember: Boolean) {
        viewModelScope.launch {
            _authError.value = null
            // For MVP prototype: Use Firebase if available, otherwise simulate
            val firebaseResult = FirebaseAuthManager.signInWithEmailAndPassword(email, password)
            
            firebaseResult.fold(
                onSuccess = { firebaseUser ->
                    // Firebase authentication successful
                    _currentUser.value = User(
                        email = firebaseUser.email ?: email,
                        role = role,
                        name = firebaseUser.displayName ?: email.split("@").first()
                    )
                    rememberMe = remember
                },
                onFailure = { error ->
                    // If Firebase fails (not configured), use simulation for prototype
                    if (error.message?.contains("FirebaseApp") == true || 
                        error.message?.contains("not initialized") == true) {
                        // Firebase not configured - use simulation
                        _currentUser.value = User(
                            email = email,
                            role = role,
                            name = email.split("@").first()
                        )
                        rememberMe = remember
                    } else {
                        // Real Firebase error
                        _authError.value = error.message ?: "Authentication failed"
                    }
                }
            )
        }
    }

    fun createAccount(name: String, email: String, password: String, role: UserRole) {
        viewModelScope.launch {
            _authError.value = null
            val firebaseResult = FirebaseAuthManager.createUserWithEmailAndPassword(email, password)
            
            firebaseResult.fold(
                onSuccess = { firebaseUser ->
                    _currentUser.value = User(
                        email = firebaseUser.email ?: email,
                        role = role,
                        name = name
                    )
                },
                onFailure = { error ->
                    if (error.message?.contains("FirebaseApp") == true) {
                        // Firebase not configured - simulate
                        _currentUser.value = User(
                            email = email,
                            role = role,
                            name = name
                        )
                    } else {
                        _authError.value = error.message ?: "Account creation failed"
                    }
                }
            )
        }
    }

    fun logout() {
        FirebaseAuthManager.signOut()
        _currentUser.value = null
    }

    init {
        // Check if user is already logged in (Firebase persistence)
        // Use try-catch to prevent crash if Firebase not initialized
        try {
            val currentFirebaseUser = FirebaseAuthManager.getCurrentUser()
            if (currentFirebaseUser != null) {
                _currentUser.value = User(
                    email = currentFirebaseUser.email ?: "",
                    role = UserRole.TENANT, // Default, can be enhanced
                    name = currentFirebaseUser.displayName ?: currentFirebaseUser.email?.split("@")?.first() ?: "User"
                )
            }
        } catch (e: Exception) {
            // Firebase not initialized - continue without pre-login
        }
    }

    fun addTicket(ticket: Ticket) {
        _tickets.value = _tickets.value + ticket
    }

    fun updateTicket(id: String, updates: Ticket) {
        _tickets.value = _tickets.value.map { if (it.id == id) updates else it }
    }

    fun assignContractor(ticketId: String, contractorId: String) {
        val ticket = _tickets.value.find { it.id == ticketId }
        if (ticket != null) {
            updateTicket(
                ticketId,
                ticket.copy(assignedTo = contractorId, status = TicketStatus.ASSIGNED)
            )
            // Create job
            _jobs.value = _jobs.value + Job(
                id = "job${_jobs.value.size + 1}",
                ticketId = ticketId,
                contractorId = contractorId,
                propertyAddress = "123 Main St, Apt 4B",
                issueType = ticket.category,
                date = java.time.LocalDate.now().toString(),
                status = "assigned"
            )
        }
    }

    fun completeJob(jobId: String) {
        val job = _jobs.value.find { it.id == jobId }
        if (job != null) {
            _jobs.value = _jobs.value.map { if (it.id == jobId) it.copy(status = "completed") else it }
            val ticket = _tickets.value.find { it.id == job.ticketId }
            if (ticket != null) {
                updateTicket(
                    ticket.id,
                    ticket.copy(status = TicketStatus.COMPLETED, completedDate = java.time.LocalDate.now().toString())
                )
            }
        }
    }

    fun addRating(jobId: String, rating: Float) {
        _jobs.value = _jobs.value.map { if (it.id == jobId) it.copy(rating = rating) else it }
    }
}

