package com.example.mvp.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object CreateAccount : Screen("create_account")
    object Dashboard : Screen("dashboard")
    object CreateTicket : Screen("create_ticket")
    object TicketDetail : Screen("ticket_detail/{ticketId}") {
        fun createRoute(ticketId: String) = "ticket_detail/$ticketId"
    }
    object Marketplace : Screen("marketplace/{ticketId}") {
        fun createRoute(ticketId: String? = null) = if (ticketId != null) "marketplace/$ticketId" else "marketplace/null"
    }
    object ContractorProfile : Screen("contractor_profile/{contractorId}") {
        fun createRoute(contractorId: String) = "contractor_profile/$contractorId"
    }
    object ContractorDashboard : Screen("contractor_dashboard")
    object JobDetail : Screen("job_detail/{jobId}") {
        fun createRoute(jobId: String) = "job_detail/$jobId"
    }
    object Schedule : Screen("schedule/{ticketId}") {
        fun createRoute(ticketId: String? = null) = if (ticketId != null) "schedule/$ticketId" else "schedule/null"
    }
    object Rating : Screen("rating/{jobId}") {
        fun createRoute(jobId: String) = "rating/$jobId"
    }
    object History : Screen("history")
    object Chat : Screen("chat")
    object AIDiagnosis : Screen("ai_diagnosis")
}

