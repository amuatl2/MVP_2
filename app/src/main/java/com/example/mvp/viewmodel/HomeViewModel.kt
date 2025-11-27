package com.example.mvp.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import com.example.mvp.auth.FirebaseAuthManager
import com.example.mvp.data.*
import com.example.mvp.ai.AIDiagnosisService
import java.util.Locale

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val dataRepository = DataRepository(application)
    private val firebaseRepository = FirebaseRepository()
    private val aiDiagnosisService = AIDiagnosisService()
    private var useFirebase = false
    
    init {
        // Check if Firebase is initialized by trying to get Firestore instance
        useFirebase = try {
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    // All tickets (for landlords to see all)
    private val _allTickets = MutableStateFlow<List<Ticket>>(emptyList())
    
    // Filtered tickets based on user role
    private val _filteredTickets = MutableStateFlow<List<Ticket>>(emptyList())
    val tickets: StateFlow<List<Ticket>> = _filteredTickets.asStateFlow()
    
    private val _contractors = MutableStateFlow(MockData.mockContractors)
    val contractors: StateFlow<List<Contractor>> = _contractors.asStateFlow()
    
    // Notifications
    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications.asStateFlow()

    // Properties
    private val _properties = MutableStateFlow<List<Property>>(emptyList())
    val properties: StateFlow<List<Property>> = _properties.asStateFlow()
    
    // Maintenance Reminders
    private val _maintenanceReminders = MutableStateFlow<List<MaintenanceReminder>>(emptyList())
    val maintenanceReminders: StateFlow<List<MaintenanceReminder>> = _maintenanceReminders.asStateFlow()
    
    // Budgets
    private val _budgets = MutableStateFlow<List<Budget>>(emptyList())
    val budgets: StateFlow<List<Budget>> = _budgets.asStateFlow()
    
    // Documents
    private val _documents = MutableStateFlow<List<Document>>(emptyList())
    val documents: StateFlow<List<Document>> = _documents.asStateFlow()
    
    // Enhanced Reviews
    private val _enhancedReviews = MutableStateFlow<List<EnhancedReview>>(emptyList())
    val enhancedReviews: StateFlow<List<EnhancedReview>> = _enhancedReviews.asStateFlow()
    
    // Dark mode preference
    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    // All jobs (for contractors to see all)
    private val _allJobs = MutableStateFlow<List<Job>>(emptyList())
    
    // Filtered jobs based on user role
    private val _filteredJobs = MutableStateFlow<List<Job>>(emptyList())
    val jobs: StateFlow<List<Job>> = _filteredJobs.asStateFlow()
    
    init {
        // Update filtered tickets when user or all tickets change
        viewModelScope.launch {
            combine(
                _currentUser,
                _allTickets
            ) { user, allTickets ->
                if (user != null) {
                    when (user.role) {
                        UserRole.TENANT -> {
                            // Tenants see only their own tickets
                            allTickets.filter { it.submittedBy == user.email }
                        }
                        UserRole.LANDLORD -> {
                            // Landlords see all tickets
                            allTickets
                        }
                        UserRole.CONTRACTOR -> {
                            // Contractors see tickets assigned to them
                            val contractorId = getContractorIdForUser(user)
                            allTickets.filter { 
                                it.assignedTo == contractorId || it.assignedContractor == contractorId
                            }
                        }
                    }
                } else {
                    emptyList()
                }
            }.collect { filtered ->
                _filteredTickets.value = filtered
            }
        }
        
        // Update filtered jobs when user or all jobs change
        viewModelScope.launch {
            combine(
                _currentUser,
                _allJobs
            ) { user, allJobs ->
                if (user != null && user.role == UserRole.CONTRACTOR) {
                    val contractorId = getContractorIdForUser(user)
                    allJobs.filter { it.contractorId == contractorId }
                } else {
                    emptyList()
                }
            }.collect { filtered ->
                _filteredJobs.value = filtered
            }
        }
        
        // Load saved user and data
        viewModelScope.launch {
            if (useFirebase) {
                // Try Firebase Auth first
                try {
                    val currentFirebaseUser = FirebaseAuthManager.getCurrentUser()
                    if (currentFirebaseUser != null) {
                        val userId = currentFirebaseUser.uid
                        val user = firebaseRepository.getUser(userId)
                        if (user != null) {
                            _currentUser.value = user
                            loadUserData(user)
                        }
                    }
                } catch (e: Exception) {
                    // Firebase not initialized
                }
            } else {
                // Use DataStore
                val savedUser = dataRepository.getCurrentUser()
                if (savedUser != null) {
                    _currentUser.value = savedUser
                    loadUserData(savedUser)
                } else {
                    // Check if user is already logged in (Firebase persistence)
                    try {
                        val currentFirebaseUser = FirebaseAuthManager.getCurrentUser()
                        if (currentFirebaseUser != null) {
                            val user = User(
                                email = currentFirebaseUser.email ?: "",
                                role = UserRole.TENANT, // Default, can be enhanced
                                name = currentFirebaseUser.displayName ?: currentFirebaseUser.email?.split("@")?.first() ?: "User"
                            )
                            _currentUser.value = user
                            dataRepository.saveCurrentUser(user)
                            loadUserData(user)
                        }
                    } catch (e: Exception) {
                        // Firebase not initialized - continue without pre-login
                    }
                }
            }
        }
        
        // Set up real-time listeners if using Firebase
        if (useFirebase) {
            viewModelScope.launch {
                firebaseRepository.observeTickets().collect { tickets ->
                    _allTickets.value = tickets
                }
            }
            
            viewModelScope.launch {
                firebaseRepository.observeJobs().collect { jobs ->
                    _allJobs.value = jobs
                }
            }
            
            // Load contractors once (they don't change as frequently)
            viewModelScope.launch {
                _contractors.value = firebaseRepository.getAllContractors()
            }
        }
    }

    var rememberMe by mutableStateOf(false)
        private set

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    fun login(email: String, password: String, role: UserRole, remember: Boolean) {
        viewModelScope.launch {
            _authError.value = null
            
            // Validate input
            if (email.isBlank() || password.isBlank()) {
                _authError.value = "Please enter both email and password"
                return@launch
            }
            
            // If Firebase is not configured, use simulation mode
            if (!useFirebase) {
                val registeredUser = dataRepository.getRegisteredUser(email)
                if (registeredUser == null) {
                    _authError.value = "No account found with this email. Please create an account first."
                    return@launch
                }
                
                if (registeredUser.password != password) {
                    _authError.value = "Incorrect password. Please try again."
                    return@launch
                }
                
                val user = User(
                    email = registeredUser.email,
                    role = registeredUser.role,
                    name = registeredUser.name
                )
                _currentUser.value = user
                dataRepository.saveCurrentUser(user)
                rememberMe = remember
                loadUserData(user)
                return@launch
            }
            
            // Try Firebase authentication with real credentials
            val firebaseResult = FirebaseAuthManager.signInWithEmailAndPassword(email, password)
            
            firebaseResult.fold(
                onSuccess = { firebaseUser ->
                    // Firebase authentication successful
                    // Try to load user data from Firestore, or create new user
                    val userId = firebaseUser.uid
                    val savedUser = firebaseRepository.getUser(userId)
                    
                    val user = savedUser ?: User(
                        email = firebaseUser.email ?: email,
                        role = role,
                        name = firebaseUser.displayName ?: email.split("@").first()
                    )
                    
                    // Update role if it changed
                    val finalUser = if (savedUser != null && savedUser.role != role) {
                        user.copy(role = role)
                    } else {
                        user
                    }
                    
                    _currentUser.value = finalUser
                    firebaseRepository.saveUser(finalUser)
                    rememberMe = remember
                    loadUserData(finalUser)
                },
                onFailure = { error ->
                    // Check if this is a credential error
                    val errorMessage = error.message ?: ""
                    val configurationMissing = isFirebaseConfigurationMissing(errorMessage)
                    val isCredentialError = errorMessage.contains("credential", ignoreCase = true) ||
                                          errorMessage.contains("invalid", ignoreCase = true) ||
                                          errorMessage.contains("user-not-found", ignoreCase = true) ||
                                          errorMessage.contains("wrong-password", ignoreCase = true) ||
                                          errorMessage.contains("INVALID_LOGIN_CREDENTIALS", ignoreCase = true) ||
                                          errorMessage.contains("ERROR_INVALID_EMAIL", ignoreCase = true) ||
                                          errorMessage.contains("ERROR_WRONG_PASSWORD", ignoreCase = true)
                    
                    if (configurationMissing) {
                        useFirebase = false
                        val registeredUser = dataRepository.getRegisteredUser(email)
                        if (registeredUser == null) {
                            _authError.value = "No account found with this email. Please create an account first."
                            return@launch
                        }
                        
                        if (registeredUser.password != password) {
                            _authError.value = "Incorrect password. Please try again."
                            return@launch
                        }
                        
                        val user = User(
                            email = registeredUser.email,
                            role = registeredUser.role,
                            name = registeredUser.name
                        )
                        _currentUser.value = user
                        dataRepository.saveCurrentUser(user)
                        rememberMe = remember
                        loadUserData(user)
                    } else if (isCredentialError) {
                        // Real credential error - provide helpful message
                        if (errorMessage.contains("user-not-found", ignoreCase = true)) {
                            _authError.value = "No account found with this email. Please create an account first."
                        } else if (errorMessage.contains("wrong-password", ignoreCase = true) || 
                                  errorMessage.contains("ERROR_WRONG_PASSWORD", ignoreCase = true)) {
                            _authError.value = "Incorrect password. Please try again."
                        } else {
                            _authError.value = "Invalid email or password. Please check your credentials."
                        }
                    } else {
                        // Other Firebase error
                        _authError.value = "Login failed: ${errorMessage.take(100)}"
                    }
                }
            )
        }
    }
    
    fun loginWithDemo(role: UserRole) {
        viewModelScope.launch {
            _authError.value = null
            useFirebase = false
            
            val demoUser = User(
                email = "demo-${role.name.lowercase()}@homeapp.dev",
                role = role,
                name = when (role) {
                    UserRole.TENANT -> "Terry Tenant"
                    UserRole.LANDLORD -> "Lana Landlord"
                    UserRole.CONTRACTOR -> "Casey Contractor"
                }
            )
            
            _currentUser.value = demoUser
            dataRepository.saveCurrentUser(demoUser)
            rememberMe = true
            loadUserData(demoUser)
        }
    }
    
    private suspend fun loadUserData(user: User) {
        if (useFirebase) {
            // Load from Firebase
            _allTickets.value = firebaseRepository.getAllTickets()
            _allJobs.value = firebaseRepository.getAllJobs()
            _contractors.value = firebaseRepository.getAllContractors()
        } else {
            // Load from DataStore
            _allTickets.value = dataRepository.getAllTickets()
            
            when (user.role) {
                UserRole.CONTRACTOR -> {
                    _allJobs.value = dataRepository.getAllJobs()
                }
                else -> {
                    _allJobs.value = emptyList()
                }
            }
        }
    }

    fun createAccount(name: String, email: String, password: String, role: UserRole) {
        viewModelScope.launch {
            _authError.value = null
            
            // Validate input
            if (name.isBlank() || email.isBlank() || password.isBlank()) {
                _authError.value = "Please fill in all fields"
                return@launch
            }
            
            if (password.length < 6) {
                _authError.value = "Password must be at least 6 characters"
                return@launch
            }
            
            // If Firebase is not configured, use simulation mode
            if (!useFirebase) {
                val existingUser = dataRepository.getRegisteredUser(email)
                if (existingUser != null) {
                    _authError.value = "An account with this email already exists. Please login instead."
                    return@launch
                }
                
                val user = User(
                    email = email,
                    role = role,
                    name = name
                )
                _currentUser.value = user
                dataRepository.saveCurrentUser(user)
                dataRepository.saveRegisteredUser(
                    LocalCredential(
                        email = email,
                        password = password,
                        name = name,
                        role = role
                    )
                )
                loadUserData(user)
                return@launch
            }
            
            // Try Firebase account creation
            val firebaseResult = FirebaseAuthManager.createUserWithEmailAndPassword(email, password)
            
            firebaseResult.fold(
                onSuccess = { firebaseUser ->
                    // Account created successfully in Firebase
                    val user = User(
                        email = firebaseUser.email ?: email,
                        role = role,
                        name = name
                    )
                    _currentUser.value = user
                    firebaseRepository.saveUser(user)
                    loadUserData(user)
                },
                onFailure = { error ->
                    val errorMessage = error.message ?: ""
                    val configurationMissing = isFirebaseConfigurationMissing(errorMessage)
                    
                    if (configurationMissing) {
                        useFirebase = false
                        val existingUser = dataRepository.getRegisteredUser(email)
                        if (existingUser != null) {
                            _authError.value = "An account with this email already exists. Please login instead."
                            return@fold
                        }
                        
                        val user = User(
                            email = email,
                            role = role,
                            name = name
                        )
                        _currentUser.value = user
                        dataRepository.saveCurrentUser(user)
                        dataRepository.saveRegisteredUser(
                            LocalCredential(
                                email = email,
                                password = password,
                                name = name,
                                role = role
                            )
                        )
                        loadUserData(user)
                    } else if (errorMessage.contains("email-already-in-use", ignoreCase = true) ||
                              errorMessage.contains("ERROR_EMAIL_ALREADY_IN_USE", ignoreCase = true)) {
                        _authError.value = "An account with this email already exists. Please login instead."
                    } else if (errorMessage.contains("weak-password", ignoreCase = true) ||
                              errorMessage.contains("ERROR_WEAK_PASSWORD", ignoreCase = true)) {
                        _authError.value = "Password is too weak. Please use a stronger password."
                    } else if (errorMessage.contains("invalid-email", ignoreCase = true) ||
                              errorMessage.contains("ERROR_INVALID_EMAIL", ignoreCase = true)) {
                        _authError.value = "Invalid email address. Please enter a valid email."
                    } else {
                        _authError.value = "Account creation failed: ${errorMessage.take(100)}"
                    }
                }
            )
        }
    }

    private fun isFirebaseConfigurationMissing(errorMessage: String?): Boolean {
        if (errorMessage.isNullOrBlank()) return false
        val normalized = errorMessage.lowercase(Locale.US)
        val sanitized = normalized.replace(Regex("[^a-z0-9_]"), "")
        return normalized.contains("firebaseapp") ||
                normalized.contains("not initialized") ||
                normalized.contains("missing default app") ||
                normalized.contains("configuration not found") ||
                sanitized.contains("configuration_not_found") ||
                sanitized.contains("configurationnotfound")
    }

    fun logout() {
        viewModelScope.launch {
            FirebaseAuthManager.signOut()
            _currentUser.value = null
            _allTickets.value = emptyList()
            _allJobs.value = emptyList()
        }
    }


    fun addTicket(ticket: Ticket) {
        viewModelScope.launch {
            // Generate AI diagnosis if not already present
            val ticketWithAI = if (ticket.aiDiagnosis.isNullOrEmpty()) {
                val aiResult = aiDiagnosisService.generateDiagnosis(
                    title = ticket.title,
                    description = ticket.description,
                    category = ticket.category,
                    priority = ticket.priority
                )
                ticket.copy(aiDiagnosis = aiResult.diagnosis)
            } else {
                ticket
            }
            
            if (useFirebase) {
                firebaseRepository.saveTicket(ticketWithAI)
                // Real-time listener will update _allTickets
            } else {
                val updatedTickets = _allTickets.value + ticketWithAI
                _allTickets.value = updatedTickets
                val currentUser = _currentUser.value
                if (currentUser != null) {
                    dataRepository.saveTickets(currentUser.email, updatedTickets)
                }
            }
            
            // Create notification for landlords
            if (_currentUser.value?.role == UserRole.TENANT) {
                createNotification(
                    title = "New Ticket Created",
                    message = "Ticket '${ticket.title}' has been submitted and is ready for review",
                    type = NotificationType.TICKET_CREATED,
                    relatedTicketId = ticketWithAI.id
                )
            }
        }
    }
    
    /**
     * Generate AI diagnosis for a ticket (can be called before submission)
     */
    suspend fun generateAIDiagnosis(
        title: String,
        description: String,
        category: String,
        priority: String?
    ): com.example.mvp.ai.AIDiagnosisResult {
        return aiDiagnosisService.generateDiagnosis(title, description, category, priority)
    }

    fun updateTicket(id: String, updates: Ticket) {
        viewModelScope.launch {
            if (useFirebase) {
                firebaseRepository.saveTicket(updates)
                // Real-time listener will update _allTickets
            } else {
                val updatedTickets = _allTickets.value.map { if (it.id == id) updates else it }
                _allTickets.value = updatedTickets
                val currentUser = _currentUser.value
                if (currentUser != null) {
                    dataRepository.saveTickets(currentUser.email, updatedTickets)
                }
            }
        }
    }

    fun assignContractor(ticketId: String, contractorId: String) {
        viewModelScope.launch {
            val ticket = _allTickets.value.find { it.id == ticketId }
            if (ticket != null && ticket.assignedTo == null) {
                updateTicket(
                    ticketId,
                    ticket.copy(
                        assignedTo = contractorId,
                        assignedContractor = contractorId,
                        status = TicketStatus.ASSIGNED
                    )
                )
                // Create job if it doesn't exist
                val existingJob = _allJobs.value.find { it.ticketId == ticketId }
                if (existingJob == null) {
                    val newJob = Job(
                        id = "job${_allJobs.value.size + 1}",
                        ticketId = ticketId,
                        contractorId = contractorId,
                        propertyAddress = "123 Main St, Apt 4B",
                        issueType = ticket.category,
                        date = com.example.mvp.utils.DateUtils.getCurrentDateString(),
                        status = "assigned"
                    )
                    if (useFirebase) {
                        firebaseRepository.saveJob(newJob)
                        // Real-time listener will update _allJobs
                    } else {
                        _allJobs.value = _allJobs.value + newJob
                        val currentUser = _currentUser.value
                        if (currentUser != null) {
                            dataRepository.saveJobs(currentUser.email, _allJobs.value)
                        }
                    }
                    
                    // Create notification
                    createNotification(
                        title = "Contractor Assigned",
                        message = "Contractor has been assigned to ticket '${ticket.title}'",
                        type = NotificationType.TICKET_ASSIGNED,
                        relatedTicketId = ticketId
                    )
                }
            }
        }
    }

    fun completeJob(jobId: String) {
        viewModelScope.launch {
            val job = _allJobs.value.find { it.id == jobId }
            if (job != null) {
                val updatedJob = job.copy(status = "completed")
                if (useFirebase) {
                    firebaseRepository.saveJob(updatedJob)
                    // Real-time listener will update _allJobs
                } else {
                    _allJobs.value = _allJobs.value.map { if (it.id == jobId) updatedJob else it }
                    val currentUser = _currentUser.value
                    if (currentUser != null) {
                        dataRepository.saveJobs(currentUser.email, _allJobs.value)
                    }
                }
                
                val ticket = _allTickets.value.find { it.id == job.ticketId }
                if (ticket != null) {
                    // Update ticket status - this will save all tickets
                    updateTicket(
                        ticket.id,
                        ticket.copy(status = TicketStatus.COMPLETED, completedDate = com.example.mvp.utils.DateUtils.getCurrentDateString())
                    )
                    
                    // Create notification
                    createNotification(
                        title = "Job Completed",
                        message = "Job for ticket '${ticket.title}' has been completed",
                        type = NotificationType.JOB_COMPLETED,
                        relatedTicketId = ticket.id,
                        relatedJobId = jobId
                    )
                }
            }
        }
    }

    fun addRating(jobId: String, rating: Float) {
        viewModelScope.launch {
            val job = _allJobs.value.find { it.id == jobId }
            if (job != null) {
                val updatedJob = job.copy(rating = rating)
                if (useFirebase) {
                    firebaseRepository.saveJob(updatedJob)
                    // Real-time listener will update _allJobs
                } else {
                    _allJobs.value = _allJobs.value.map { if (it.id == jobId) updatedJob else it }
                    val currentUser = _currentUser.value
                    if (currentUser != null) {
                        dataRepository.saveJobs(currentUser.email, _allJobs.value)
                    }
                }
                
                // Also update the ticket rating
                val ticket = _allTickets.value.find { t -> t.id == job.ticketId }
                ticket?.let { t ->
                    updateTicket(t.id, t.copy(rating = rating))
                }
                
                // Update contractor's average rating
                val contractor = _contractors.value.find { it.id == job.contractorId }
                if (contractor != null) {
                    // Calculate new average rating
                    val contractorJobs = _allJobs.value.filter { it.contractorId == contractor.id && it.rating != null }
                    val averageRating = if (contractorJobs.isNotEmpty()) {
                        contractorJobs.mapNotNull { it.rating }.average().toFloat()
                    } else {
                        rating
                    }
                    
                    val updatedContractor = contractor.copy(
                        rating = averageRating,
                        completedJobs = contractor.completedJobs + 1
                    )
                    
                    _contractors.value = _contractors.value.map { 
                        if (it.id == contractor.id) updatedContractor else it 
                    }
                    
                    // Save updated contractor to Firebase if available
                    if (useFirebase) {
                        firebaseRepository.saveContractor(updatedContractor)
                    }
                }
            }
        }
    }

    fun scheduleTicket(ticketId: String, date: String, time: String) {
        viewModelScope.launch {
            val ticket = _allTickets.value.find { it.id == ticketId }
            if (ticket != null) {
                val scheduledDateTime = "$date $time"
                updateTicket(
                    ticketId,
                    ticket.copy(
                        status = TicketStatus.SCHEDULED,
                        scheduledDate = scheduledDateTime
                    )
                )
            }
        }
    }
    
    fun addMessageToTicket(ticketId: String, message: Message) {
        viewModelScope.launch {
            val ticket = _allTickets.value.find { it.id == ticketId }
            if (ticket != null) {
                val updatedMessages = ticket.messages + message
                updateTicket(
                    ticketId,
                    ticket.copy(messages = updatedMessages)
                )
            }
        }
    }

    fun getContractorIdForUser(user: User?): String? {
        if (user?.role != UserRole.CONTRACTOR) return null
        // Try to match contractor by name or email
        return _contractors.value.find { 
            it.name.contains(user.name, ignoreCase = true) || 
            it.name.contains(user.email.split("@").first(), ignoreCase = true)
        }?.id ?: _contractors.value.firstOrNull()?.id
    }
    
    // Notification methods
    fun getNotifications(): List<Notification> {
        return _notifications.value.sortedByDescending { it.timestamp }
    }
    
    fun markAllNotificationsRead() {
        viewModelScope.launch {
            _notifications.value = _notifications.value.map { it.copy(isRead = true) }
        }
    }
    
    private fun createNotification(
        title: String,
        message: String,
        type: NotificationType,
        relatedTicketId: String? = null,
        relatedJobId: String? = null
    ) {
        viewModelScope.launch {
            val notification = Notification(
                id = "notif-${System.currentTimeMillis()}",
                title = title,
                message = message,
                type = type,
                timestamp = com.example.mvp.utils.DateUtils.getCurrentDateTimeString(),
                isRead = false,
                relatedTicketId = relatedTicketId,
                relatedJobId = relatedJobId
            )
            _notifications.value = _notifications.value + notification
        }
    }
    
    // Property management methods
    fun addProperty(property: Property) {
        viewModelScope.launch {
            val updatedProperties = _properties.value + property
            _properties.value = updatedProperties
            if (!useFirebase) {
                dataRepository.saveProperties(_currentUser.value?.email ?: "", updatedProperties)
            }
        }
    }
    
    fun updateProperty(property: Property) {
        viewModelScope.launch {
            val updatedProperties = _properties.value.map { 
                if (it.id == property.id) property else it 
            }
            _properties.value = updatedProperties
            if (!useFirebase) {
                dataRepository.saveProperties(_currentUser.value?.email ?: "", updatedProperties)
            }
        }
    }
    
    fun deleteProperty(propertyId: String) {
        viewModelScope.launch {
            val updatedProperties = _properties.value.filter { it.id != propertyId }
            _properties.value = updatedProperties
            if (!useFirebase) {
                dataRepository.saveProperties(_currentUser.value?.email ?: "", updatedProperties)
            }
        }
    }
    
    // Maintenance reminder methods
    fun addMaintenanceReminder(reminder: MaintenanceReminder) {
        viewModelScope.launch {
            val updatedReminders = _maintenanceReminders.value + reminder
            _maintenanceReminders.value = updatedReminders
            if (!useFirebase) {
                dataRepository.saveMaintenanceReminders(_currentUser.value?.email ?: "", updatedReminders)
            }
        }
    }
    
    fun updateMaintenanceReminder(reminder: MaintenanceReminder) {
        viewModelScope.launch {
            val updatedReminders = _maintenanceReminders.value.map { 
                if (it.id == reminder.id) reminder else it 
            }
            _maintenanceReminders.value = updatedReminders
            if (!useFirebase) {
                dataRepository.saveMaintenanceReminders(_currentUser.value?.email ?: "", updatedReminders)
            }
        }
    }
    
    fun deleteMaintenanceReminder(reminderId: String) {
        viewModelScope.launch {
            val updatedReminders = _maintenanceReminders.value.filter { it.id != reminderId }
            _maintenanceReminders.value = updatedReminders
            if (!useFirebase) {
                dataRepository.saveMaintenanceReminders(_currentUser.value?.email ?: "", updatedReminders)
            }
        }
    }
    
    // Budget methods
    fun addBudget(budget: Budget) {
        viewModelScope.launch {
            if (!useFirebase) {
                val updatedBudgets = _budgets.value + budget
                _budgets.value = updatedBudgets
                _currentUser.value?.let { user ->
                    dataRepository.saveBudgets(user.email, updatedBudgets)
                }
            }
        }
    }
    
    fun updateBudget(budget: Budget) {
        viewModelScope.launch {
            if (!useFirebase) {
                val updatedBudgets = _budgets.value.map { 
                    if (it.id == budget.id) budget else it
                }
                _budgets.value = updatedBudgets
                _currentUser.value?.let { user ->
                    dataRepository.saveBudgets(user.email, updatedBudgets)
                }
            }
        }
    }
    
    fun deleteBudget(budgetId: String) {
        viewModelScope.launch {
            if (!useFirebase) {
                val updatedBudgets = _budgets.value.filter { it.id != budgetId }
                _budgets.value = updatedBudgets
                _currentUser.value?.let { user ->
                    dataRepository.saveBudgets(user.email, updatedBudgets)
                }
            }
        }
    }
    
    // Document methods
    fun addDocument(document: Document) {
        viewModelScope.launch {
            if (!useFirebase) {
                val updatedDocuments = _documents.value + document
                _documents.value = updatedDocuments
                _currentUser.value?.let { user ->
                    dataRepository.saveDocuments(user.email, updatedDocuments)
                }
            }
        }
    }
    
    fun deleteDocument(documentId: String) {
        viewModelScope.launch {
            if (!useFirebase) {
                val updatedDocuments = _documents.value.filter { it.id != documentId }
                _documents.value = updatedDocuments
                _currentUser.value?.let { user ->
                    dataRepository.saveDocuments(user.email, updatedDocuments)
                }
            }
        }
    }
    
    // Enhanced Review methods
    fun addEnhancedReview(review: EnhancedReview) {
        viewModelScope.launch {
            if (!useFirebase) {
                val updatedReviews = _enhancedReviews.value + review
                _enhancedReviews.value = updatedReviews
                _currentUser.value?.let { user ->
                    dataRepository.saveEnhancedReviews(user.email, updatedReviews)
                }
            }
        }
    }
    
    fun updateEnhancedReview(review: EnhancedReview) {
        viewModelScope.launch {
            if (!useFirebase) {
                val updatedReviews = _enhancedReviews.value.map { 
                    if (it.id == review.id) review else it
                }
                _enhancedReviews.value = updatedReviews
                _currentUser.value?.let { user ->
                    dataRepository.saveEnhancedReviews(user.email, updatedReviews)
                }
            }
        }
    }
    
    // Dark mode toggle
    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }
    
    fun setDarkMode(isDark: Boolean) {
        _isDarkMode.value = isDark
    }
    
    init {
        // Generate notifications when tickets are created/updated
        viewModelScope.launch {
            _allTickets.collect { tickets ->
                // This would ideally track changes, but for now we'll generate notifications
                // when tickets are added/updated through the existing methods
            }
        }
        
        // Load properties and reminders
        viewModelScope.launch {
            if (!useFirebase) {
                _currentUser.value?.let { user ->
                    _properties.value = dataRepository.getProperties(user.email)
                    _maintenanceReminders.value = dataRepository.getMaintenanceReminders(user.email)
                }
            }
        }
    }
}

