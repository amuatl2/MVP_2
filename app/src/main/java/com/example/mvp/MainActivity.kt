package com.example.mvp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.mvp.data.*
import com.example.mvp.navigation.Screen
import com.example.mvp.ui.components.HomeBottomNavigation
import com.example.mvp.ui.components.TopNavigationBar
import com.example.mvp.ui.screens.*
import com.example.mvp.ui.theme.MVPTheme
import com.example.mvp.utils.ThemeManager
import com.example.mvp.utils.ExportManager
import com.example.mvp.viewmodel.HomeViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var isDarkMode by remember { mutableStateOf(false) }
            MVPTheme(darkTheme = isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HomeApp(isDarkMode = isDarkMode, onDarkModeChange = { isDarkMode = it })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeApp(
    isDarkMode: Boolean = false,
    onDarkModeChange: (Boolean) -> Unit = {}
) {
    val viewModel: HomeViewModel = viewModel()
    val navController = rememberNavController()
    val context = LocalContext.current

    val currentUser by viewModel.currentUser.collectAsState()
    val tickets by viewModel.tickets.collectAsState()
    val contractors by viewModel.contractors.collectAsState()
    val jobs by viewModel.jobs.collectAsState()
    val authErrorState by viewModel.authError.collectAsState()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        topBar = {
            if (currentUser != null && currentRoute != Screen.Login.route) {
                TopNavigationBar(
                    currentRoute = currentRoute,
                    userRole = currentUser!!.role,
                    onNavigate = { route ->
                        when (route) {
                            "dashboard" -> navController.navigate(Screen.Dashboard.route) {
                                popUpTo(Screen.Dashboard.route) { inclusive = true }
                            }
                            "create_ticket" -> navController.navigate(Screen.CreateTicket.route)
                            "marketplace" -> navController.navigate(Screen.Marketplace.createRoute(null))
                            "ai_diagnosis" -> navController.navigate(Screen.AIDiagnosis.route)
                            "contractor_dashboard" -> navController.navigate(Screen.ContractorDashboard.route)
                            "schedule" -> navController.navigate(Screen.Schedule.createRoute(null))
                            "history" -> navController.navigate(Screen.History.route)
                            "chat" -> navController.navigate(Screen.Chat.route)
                            "notifications" -> navController.navigate(Screen.Notifications.route)
                            "analytics" -> navController.navigate(Screen.Analytics.route)
                            "settings" -> navController.navigate(Screen.Settings.route)
                            "properties" -> navController.navigate(Screen.Properties.route)
                            "maintenance_reminders" -> navController.navigate(Screen.MaintenanceReminders.route)
                            "search" -> navController.navigate(Screen.Search.route)
                            "budget" -> navController.navigate(Screen.Budget.route)
                            "documents" -> navController.navigate(Screen.Documents.route)
                            "enhanced_schedule" -> navController.navigate(Screen.EnhancedSchedule.route)
                            "advanced_analytics" -> navController.navigate(Screen.AdvancedAnalytics.route)
                            "tenant_portal" -> navController.navigate(Screen.TenantPortal.route)
                            "rating" -> {
                                val completedJob = jobs.find { it.status == "completed" }
                                if (completedJob != null) {
                                    navController.navigate(Screen.Rating.createRoute(completedJob.id))
                                } else {
                                    navController.navigate(Screen.History.route)
                                }
                            }
                        }
                    },
                    onLogout = {
                        viewModel.logout()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (currentUser != null && currentRoute != Screen.Login.route) {
                HomeBottomNavigation(
                    currentRoute = currentRoute,
                    userRole = currentUser!!.role,
                    onNavigate = { route ->
                        when (route) {
                            "dashboard" -> navController.navigate(Screen.Dashboard.route) {
                                popUpTo(Screen.Dashboard.route) { inclusive = true }
                            }
                            "create_ticket" -> navController.navigate(Screen.CreateTicket.route)
                            "marketplace" -> navController.navigate(Screen.Marketplace.createRoute(null))
                            "contractor_dashboard" -> navController.navigate(Screen.ContractorDashboard.route)
                            "schedule" -> navController.navigate(Screen.Schedule.createRoute(null))
                            "history" -> navController.navigate(Screen.History.route)
                            "chat" -> navController.navigate(Screen.Chat.route)
                            "notifications" -> navController.navigate(Screen.Notifications.route)
                            "analytics" -> navController.navigate(Screen.Analytics.route)
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Login.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Login.route) {
                LaunchedEffect(currentUser) {
                    if (currentUser != null) {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                }
                
                LoginScreen(
                    onLogin = { email, password, role, remember ->
                        viewModel.login(email, password, role, remember)
                    },
                    onUseDemo = { role ->
                        viewModel.loginWithDemo(role)
                    },
                    onCreateAccount = {
                        navController.navigate(Screen.CreateAccount.route)
                    },
                    authError = authErrorState
                )
            }

            composable(Screen.CreateAccount.route) {
                LaunchedEffect(currentUser) {
                    if (currentUser != null) {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
                
                CreateAccountScreen(
                    onBack = { navController.popBackStack() },
                    onCreateAccount = { name, email, password, role ->
                        viewModel.createAccount(name, email, password, role)
                    },
                    authError = authErrorState
                )
            }

            composable(Screen.Dashboard.route) {
                when (currentUser?.role) {
                    UserRole.TENANT -> TenantDashboardScreen(
                        tickets = tickets,
                        onCreateTicket = {
                            navController.navigate(Screen.CreateTicket.route)
                        },
                        onTicketClick = { ticketId ->
                            navController.navigate(Screen.TicketDetail.createRoute(ticketId))
                        }
                    )
                    UserRole.LANDLORD -> LandlordDashboardScreen(
                        tickets = tickets,
                        onTicketClick = { ticketId ->
                            navController.navigate(Screen.TicketDetail.createRoute(ticketId))
                        },
                        onAIDiagnosis = {
                            navController.navigate(Screen.AIDiagnosis.route)
                        },
                        onMarketplace = {
                            navController.navigate(Screen.Marketplace.route)
                        }
                    )
                    UserRole.CONTRACTOR -> ContractorDashboardScreen(
                        jobs = jobs,
                        onJobClick = { jobId ->
                            navController.navigate(Screen.JobDetail.createRoute(jobId))
                        }
                    )
                    null -> {}
                }
            }

            composable(Screen.CreateTicket.route) {
                CreateTicketScreen(
                    onBack = { navController.popBackStack() },
                    viewModel = viewModel,
                    onSubmit = { title, description, category, priority ->
                        val dateStr = com.example.mvp.utils.DateUtils.getCurrentDateString()
                        val newTicket = Ticket(
                            id = "ticket-${System.currentTimeMillis()}",
                            title = title,
                            description = description,
                            category = category,
                            status = TicketStatus.SUBMITTED,
                            submittedBy = currentUser?.email ?: "",
                            aiDiagnosis = null, // Will be generated by ViewModel
                            createdAt = com.example.mvp.utils.DateUtils.getCurrentDateTimeString(),
                            createdDate = dateStr,
                            priority = priority,
                            ticketNumber = "${System.currentTimeMillis() % 100000}"
                        )
                        viewModel.addTicket(newTicket)
                    }
                )
            }

            composable(
                route = Screen.TicketDetail.route,
                arguments = listOf(navArgument("ticketId") { type = NavType.StringType })
            ) { backStackEntry ->
                val ticketId = backStackEntry.arguments?.getString("ticketId") ?: ""
                val ticket = tickets.find { it.id == ticketId }
                val contractor = ticket?.assignedTo?.let { 
                    contractors.find { c -> c.id == it }
                }

                if (ticket != null) {
                    TicketDetailScreen(
                        ticket = ticket,
                        contractor = contractor,
                        onBack = { navController.popBackStack() },
                        onAssignContractor = {
                            navController.navigate(Screen.Marketplace.createRoute(ticketId))
                        },
                        onScheduleVisit = {
                            navController.navigate(Screen.Schedule.createRoute(ticketId))
                        },
                        userRole = currentUser?.role ?: UserRole.TENANT,
                        currentUserEmail = currentUser?.email,
                        currentUserName = currentUser?.name,
                        onAddMessage = { message ->
                            viewModel.addMessageToTicket(ticketId, message)
                        }
                    )
                }
            }

            composable(
                route = Screen.Marketplace.route,
                arguments = listOf(navArgument("ticketId") { type = NavType.StringType; nullable = true })
            ) { backStackEntry ->
                val ticketId = backStackEntry.arguments?.getString("ticketId")?.takeIf { it != "null" }
                MarketplaceScreen(
                    contractors = contractors,
                    tickets = tickets,
                    onContractorClick = { contractorId ->
                        navController.navigate(Screen.ContractorProfile.createRoute(contractorId))
                    },
                    onAssign = { contractorId ->
                        ticketId?.let {
                            viewModel.assignContractor(ticketId, contractorId)
                            navController.popBackStack()
                        }
                    },
                    onApplyToJob = { jobTicketId ->
                        // For contractors applying to jobs - assign them to the ticket
                        val contractorId = viewModel.getContractorIdForUser(currentUser)
                        if (contractorId != null) {
                            viewModel.assignContractor(jobTicketId, contractorId)
                            navController.popBackStack()
                        }
                    },
                    userRole = currentUser?.role ?: UserRole.TENANT,
                    ticketId = ticketId
                )
            }

            composable(
                route = Screen.ContractorProfile.route,
                arguments = listOf(navArgument("contractorId") { type = NavType.StringType })
            ) { backStackEntry ->
                val contractorId = backStackEntry.arguments?.getString("contractorId") ?: ""
                val contractor = contractors.find { it.id == contractorId }
                if (contractor != null) {
                    // Simple contractor profile view
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 16.dp)
                    ) {
                        Text(
                            text = contractor.name,
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Text(text = contractor.company)
                        Text(text = "Rating: ${contractor.rating}")
                        TextButton(onClick = { navController.popBackStack() }) {
                            Text("Back")
                        }
                    }
                }
            }

            composable(Screen.ContractorDashboard.route) {
                ContractorDashboardScreen(
                    jobs = jobs,
                    tickets = tickets,
                    onJobClick = { jobId ->
                        navController.navigate(Screen.JobDetail.createRoute(jobId))
                    },
                    onApplyToJob = { ticketId ->
                        // Handle job application - could navigate to ticket detail or apply directly
                        navController.navigate(Screen.TicketDetail.createRoute(ticketId))
                    }
                )
            }

            composable(
                route = Screen.JobDetail.route,
                arguments = listOf(navArgument("jobId") { type = NavType.StringType })
            ) { backStackEntry ->
                val jobId = backStackEntry.arguments?.getString("jobId") ?: ""
                val job = jobs.find { it.id == jobId }
                val ticket = job?.let { tickets.find { t -> t.id == it.ticketId } }

                if (job != null && ticket != null) {
                    JobDetailScreen(
                        job = job,
                        ticket = ticket,
                        onBack = { navController.popBackStack() },
                        onComplete = { id ->
                            viewModel.completeJob(id)
                            navController.navigate(Screen.Rating.createRoute(id))
                        },
                        userRole = currentUser?.role ?: UserRole.CONTRACTOR
                    )
                }
            }

            composable(
                route = Screen.Schedule.route,
                arguments = listOf(navArgument("ticketId") { type = NavType.StringType; nullable = true })
            ) { backStackEntry ->
                val routeTicketId = backStackEntry.arguments?.getString("ticketId")?.takeIf { it != "null" }
                ScheduleScreen(
                    tickets = tickets,
                    defaultTicketId = routeTicketId,
                    onBack = { navController.popBackStack() },
                    onConfirm = { date, time, ticketId ->
                        // Handle schedule confirmation - update ticket with scheduled date/time
                        ticketId?.let { id ->
                            viewModel.scheduleTicket(id, date, time)
                        }
                        navController.popBackStack()
                    }
                )
            }

            composable(
                route = Screen.Rating.route,
                arguments = listOf(navArgument("jobId") { type = NavType.StringType })
            ) { backStackEntry ->
                val jobId = backStackEntry.arguments?.getString("jobId") ?: ""
                val job = jobs.find { it.id == jobId }
                val contractor = job?.let { 
                    contractors.find { c -> c.id == it.contractorId }
                }

                RatingScreen(
                    contractor = contractor,
                    completedJobs = jobs.filter { it.status == "completed" },
                    recentRatings = emptyList(), // TODO: Load from data
                    onBack = { 
                        navController.navigate(Screen.History.route) {
                            popUpTo(Screen.Dashboard.route) { inclusive = false }
                        }
                    },
                    onSubmit = { rating, comment ->
                        jobId.let { viewModel.addRating(it, rating) }
                    }
                )
            }

            composable(Screen.History.route) {
                HistoryScreen(
                    tickets = tickets,
                    jobs = jobs,
                    contractors = contractors,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Chat.route) {
                ChatScreen(
                    onBack = { navController.popBackStack() },
                    tickets = tickets,
                    currentUser = currentUser,
                    onTicketClick = { ticketId ->
                        navController.navigate(Screen.TicketDetail.createRoute(ticketId))
                    }
                )
            }
            
            composable(Screen.Notifications.route) {
                NotificationsScreen(
                    notifications = viewModel.getNotifications(),
                    onNotificationClick = { notification ->
                        notification.relatedTicketId?.let {
                            navController.navigate(Screen.TicketDetail.createRoute(it))
                        }
                    },
                    onMarkAllRead = {
                        viewModel.markAllNotificationsRead()
                    }
                )
            }
            
            composable(Screen.Analytics.route) {
                AnalyticsScreen(
                    tickets = tickets,
                    jobs = jobs,
                    onExport = {
                        val analytics = com.example.mvp.data.CostAnalytics(
                            totalSpent = jobs.filter { it.cost != null }.sumOf { it.cost ?: 0 }.toFloat(),
                            averageCostPerTicket = if (jobs.isNotEmpty()) {
                                jobs.filter { it.cost != null }.sumOf { it.cost ?: 0 }.toFloat() / jobs.size
                            } else 0f,
                            costByCategory = emptyMap(),
                            monthlyTrend = emptyList(),
                            topExpenses = emptyList()
                        )
                        val uri = ExportManager.exportAnalyticsToCSV(context, analytics)
                        uri?.let { ExportManager.shareFile(context, it, "Export Analytics") }
                    }
                )
            }
            
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onBack = { navController.popBackStack() },
                    isDarkMode = isDarkMode,
                    onDarkModeToggle = onDarkModeChange,
                    onExportTickets = {
                        val uri = ExportManager.exportTicketsToCSV(context, tickets)
                        uri?.let { ExportManager.shareFile(context, it, "Export Tickets") }
                    },
                    onExportJobs = {
                        val uri = ExportManager.exportJobsToCSV(context, jobs)
                        uri?.let { ExportManager.shareFile(context, it, "Export Jobs") }
                    },
                    onExportAnalytics = {
                        val analytics = com.example.mvp.data.CostAnalytics(
                            totalSpent = jobs.filter { it.cost != null }.sumOf { it.cost ?: 0 }.toFloat(),
                            averageCostPerTicket = if (jobs.isNotEmpty()) {
                                jobs.filter { it.cost != null }.sumOf { it.cost ?: 0 }.toFloat() / jobs.size
                            } else 0f,
                            costByCategory = emptyMap(),
                            monthlyTrend = emptyList(),
                            topExpenses = emptyList()
                        )
                        val uri = ExportManager.exportAnalyticsToCSV(context, analytics)
                        uri?.let { ExportManager.shareFile(context, it, "Export Analytics") }
                    }
                )
            }

            composable(Screen.Properties.route) {
                PropertiesScreen(
                    onBack = { navController.popBackStack() },
                    properties = viewModel.properties.collectAsState().value,
                    onCreateProperty = { property ->
                        viewModel.addProperty(property)
                    },
                    onUpdateProperty = { property ->
                        viewModel.updateProperty(property)
                    },
                    onDeleteProperty = { propertyId ->
                        viewModel.deleteProperty(propertyId)
                    },
                    onPropertyClick = { propertyId ->
                        // Could navigate to property detail screen
                    }
                )
            }
            
            composable(Screen.MaintenanceReminders.route) {
                MaintenanceRemindersScreen(
                    onBack = { navController.popBackStack() },
                    reminders = viewModel.maintenanceReminders.collectAsState().value,
                    onCreateReminder = { reminder ->
                        viewModel.addMaintenanceReminder(reminder)
                    },
                    onUpdateReminder = { reminder ->
                        viewModel.updateMaintenanceReminder(reminder)
                    },
                    onDeleteReminder = { reminderId ->
                        viewModel.deleteMaintenanceReminder(reminderId)
                    },
                    properties = viewModel.properties.collectAsState().value
                )
            }
            
            composable(Screen.Search.route) {
                SearchScreen(
                    onBack = { navController.popBackStack() },
                    tickets = tickets,
                    jobs = jobs,
                    onTicketClick = { ticketId ->
                        navController.navigate(Screen.TicketDetail.createRoute(ticketId))
                    },
                    onJobClick = { jobId ->
                        navController.navigate(Screen.JobDetail.createRoute(jobId))
                    }
                )
            }
            
            composable(Screen.Budget.route) {
                BudgetScreen(
                    onBack = { navController.popBackStack() },
                    budgets = viewModel.budgets.collectAsState().value,
                    properties = viewModel.properties.collectAsState().value,
                    onCreateBudget = { budget ->
                        viewModel.addBudget(budget)
                    },
                    onUpdateBudget = { budget ->
                        viewModel.updateBudget(budget)
                    },
                    onDeleteBudget = { budgetId ->
                        viewModel.deleteBudget(budgetId)
                    }
                )
            }
            
            composable(Screen.Documents.route) {
                DocumentsScreen(
                    onBack = { navController.popBackStack() },
                    documents = viewModel.documents.collectAsState().value,
                    onUploadDocument = { document ->
                        viewModel.addDocument(document)
                    },
                    onDeleteDocument = { documentId ->
                        viewModel.deleteDocument(documentId)
                    }
                )
            }
            
            composable(Screen.EnhancedSchedule.route) {
                EnhancedScheduleScreen(
                    onBack = { navController.popBackStack() },
                    onScheduleAppointment = { ticketId, date, time ->
                        viewModel.scheduleTicket(ticketId, date, time)
                    },
                    onReschedule = { jobId, date, time ->
                        // Handle rescheduling
                    }
                )
            }
            
            composable(Screen.AdvancedAnalytics.route) {
                AdvancedAnalyticsScreen(
                    onBack = { navController.popBackStack() },
                    tickets = tickets,
                    jobs = jobs,
                    contractors = contractors
                )
            }
            
            composable(Screen.TenantPortal.route) {
                TenantPortalScreen(
                    onBack = { navController.popBackStack() },
                    onCreateTicket = {
                        navController.navigate(Screen.CreateTicket.route)
                    },
                    onViewTickets = {
                        navController.navigate(Screen.Dashboard.route)
                    }
                )
            }
            
            composable(
                route = Screen.EnhancedReview.route,
                arguments = listOf(navArgument("jobId") { type = NavType.StringType })
            ) { backStackEntry ->
                val jobId = backStackEntry.arguments?.getString("jobId") ?: ""
                val job = jobs.find { it.id == jobId }
                val contractor = job?.let { contractors.find { c -> c.id == it.contractorId } }
                
                EnhancedReviewScreen(
                    onBack = { navController.popBackStack() },
                    contractor = contractor,
                    jobId = jobId,
                    onSubmitReview = { review ->
                        viewModel.addEnhancedReview(review)
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.AIDiagnosis.route) {
                val ticketsWithAI = tickets.filter { 
                    it.aiDiagnosis != null && it.status == TicketStatus.SUBMITTED 
                }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Column {
                            Text(
                                text = "AI Diagnosis Center",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tickets with AI-powered analysis ready for review",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                    
                    if (ticketsWithAI.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(48.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("🤖", fontSize = 64.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "No AI diagnoses available yet",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "AI diagnoses are automatically generated when tenants create tickets. Check back soon!",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                    
                    items(ticketsWithAI) { ticket ->
                        Card(
                            onClick = {
                                navController.navigate(Screen.TicketDetail.createRoute(ticket.id))
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = ticket.title,
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                color = MaterialTheme.colorScheme.secondaryContainer,
                                                shape = MaterialTheme.shapes.small
                                            ) {
                                                Text(
                                                    text = ticket.category,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                    style = MaterialTheme.typography.labelSmall
                                                )
                                            }
                                            Text(
                                                text = "•",
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                            )
                                            Text(
                                                text = ticket.createdDate ?: ticket.createdAt.split("T").firstOrNull() ?: "",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                            )
                                        }
                                    }
                                    Surface(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = MaterialTheme.shapes.small
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text("🤖", fontSize = 16.sp)
                                            Text(
                                                text = "AI",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = "AI Analysis",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = ticket.aiDiagnosis ?: "No diagnosis available",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            navController.navigate(Screen.TicketDetail.createRoute(ticket.id))
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("View Full Details")
                                    }
                                    OutlinedButton(
                                        onClick = {
                                            navController.navigate(Screen.Marketplace.createRoute(ticket.id))
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Assign Contractor")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
