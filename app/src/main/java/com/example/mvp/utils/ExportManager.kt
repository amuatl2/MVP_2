package com.example.mvp.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.mvp.data.Ticket
import com.example.mvp.data.Job
import com.example.mvp.data.CostAnalytics
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

object ExportManager {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
    
    /**
     * Export tickets to CSV
     */
    fun exportTicketsToCSV(context: Context, tickets: List<Ticket>, fileName: String? = null): Uri? {
        return try {
            val file = File(context.getExternalFilesDir(null), fileName ?: "tickets_${dateTimeFormat.format(Date())}.csv")
            FileWriter(file).use { writer ->
                // Header
                writer.append("Ticket ID,Title,Category,Status,Submitted By,Created Date,Priority,Assigned Contractor\n")
                
                // Data
                tickets.forEach { ticket ->
                    writer.append("${ticket.id},")
                    writer.append("\"${ticket.title.replace("\"", "\"\"")}\",")
                    writer.append("${ticket.category},")
                    writer.append("${ticket.status.name},")
                    writer.append("${ticket.submittedBy},")
                    writer.append("${ticket.createdDate ?: ticket.createdAt.split("T").firstOrNull() ?: ""},")
                    writer.append("${ticket.priority ?: "N/A"},")
                    writer.append("${ticket.assignedContractor ?: "N/A"}\n")
                }
            }
            Uri.fromFile(file)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Export jobs to CSV
     */
    fun exportJobsToCSV(context: Context, jobs: List<Job>, fileName: String? = null): Uri? {
        return try {
            val file = File(context.getExternalFilesDir(null), fileName ?: "jobs_${dateTimeFormat.format(Date())}.csv")
            FileWriter(file).use { writer ->
                // Header
                writer.append("Job ID,Ticket ID,Contractor ID,Property Address,Issue Type,Date,Status,Cost,Duration,Rating\n")
                
                // Data
                jobs.forEach { job ->
                    writer.append("${job.id},")
                    writer.append("${job.ticketId},")
                    writer.append("${job.contractorId},")
                    writer.append("\"${job.propertyAddress.replace("\"", "\"\"")}\",")
                    writer.append("${job.issueType},")
                    writer.append("${job.date},")
                    writer.append("${job.status},")
                    writer.append("${job.cost ?: "N/A"},")
                    writer.append("${job.duration ?: "N/A"},")
                    writer.append("${job.rating ?: "N/A"}\n")
                }
            }
            Uri.fromFile(file)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Export analytics to CSV
     */
    fun exportAnalyticsToCSV(context: Context, analytics: CostAnalytics, fileName: String? = null): Uri? {
        return try {
            val file = File(context.getExternalFilesDir(null), fileName ?: "analytics_${dateTimeFormat.format(Date())}.csv")
            FileWriter(file).use { writer ->
                writer.append("Analytics Report\n")
                writer.append("Generated: ${dateTimeFormat.format(Date())}\n\n")
                
                writer.append("Summary\n")
                writer.append("Total Spent,${analytics.totalSpent}\n")
                writer.append("Average Cost Per Ticket,${analytics.averageCostPerTicket}\n\n")
                
                writer.append("Cost by Category\n")
                writer.append("Category,Amount\n")
                analytics.costByCategory.forEach { (category, amount) ->
                    writer.append("$category,$amount\n")
                }
                writer.append("\n")
                
                writer.append("Monthly Trends\n")
                writer.append("Month,Year,Total Cost,Ticket Count\n")
                analytics.monthlyTrend.forEach { monthly ->
                    writer.append("${monthly.month},${monthly.year},${monthly.totalCost},${monthly.ticketCount}\n")
                }
                writer.append("\n")
                
                writer.append("Top Expenses\n")
                writer.append("Description,Amount,Category,Date\n")
                analytics.topExpenses.forEach { expense ->
                    writer.append("\"${expense.description.replace("\"", "\"\"")}\",${expense.amount},${expense.category},${expense.date}\n")
                }
            }
            Uri.fromFile(file)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Share file via intent
     */
    fun shareFile(context: Context, uri: Uri, title: String) {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, uri)
            type = "text/csv"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, title))
    }
    
    /**
     * Generate simple text report
     */
    fun generateTextReport(
        tickets: List<Ticket>,
        jobs: List<Job>,
        startDate: String? = null,
        endDate: String? = null
    ): String {
        val report = StringBuilder()
        report.append("HOME Maintenance Report\n")
        report.append("Generated: ${dateTimeFormat.format(Date())}\n")
        report.append("=".repeat(50)).append("\n\n")
        
        if (startDate != null || endDate != null) {
            report.append("Date Range: ${startDate ?: "Start"} to ${endDate ?: "End"}\n\n")
        }
        
        report.append("Tickets Summary\n")
        report.append("-".repeat(50)).append("\n")
        report.append("Total Tickets: ${tickets.size}\n")
        report.append("Submitted: ${tickets.count { it.status.name == "SUBMITTED" }}\n")
        report.append("Assigned: ${tickets.count { it.status.name == "ASSIGNED" }}\n")
        report.append("Scheduled: ${tickets.count { it.status.name == "SCHEDULED" }}\n")
        report.append("Completed: ${tickets.count { it.status.name == "COMPLETED" }}\n\n")
        
        report.append("Jobs Summary\n")
        report.append("-".repeat(50)).append("\n")
        report.append("Total Jobs: ${jobs.size}\n")
        report.append("Completed: ${jobs.count { it.status.lowercase() == "completed" }}\n")
        val totalCost = jobs.mapNotNull { it.cost }.sumOf { it.toDouble() }.toInt()
        report.append("Total Cost: $$totalCost\n\n")
        
        report.append("Recent Tickets\n")
        report.append("-".repeat(50)).append("\n")
        tickets.take(10).forEach { ticket ->
            report.append("• ${ticket.title} (${ticket.category}) - ${ticket.status.name}\n")
        }
        
        return report.toString()
    }
}

