package com.example.mvp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mvp.data.CostAnalytics
import com.example.mvp.data.Ticket
import com.example.mvp.data.Job
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

@Composable
fun AnalyticsScreen(
    tickets: List<Ticket> = emptyList(),
    jobs: List<Job> = emptyList(),
    onExport: () -> Unit = {}
) {
    val analytics = calculateAnalytics(tickets, jobs)
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Cost Analytics",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Track spending and maintenance trends",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            IconButton(onClick = onExport) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Export"
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Summary Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Total Spent",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "$${String.format("%.2f", analytics.totalSpent)}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Avg per Ticket",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "$${String.format("%.2f", analytics.averageCostPerTicket)}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
        
        // Cost by Category
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Cost by Category",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                analytics.costByCategory.forEach { (category, cost) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = category,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "$${String.format("%.2f", cost)}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    HorizontalDivider()
                }
            }
        }
        
        // Top Expenses
        if (analytics.topExpenses.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Top Expenses",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    analytics.topExpenses.take(5).forEachIndexed { index, expense ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = MaterialTheme.shapes.small
                                ) {
                                    Text(
                                        text = "${index + 1}",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                                Column {
                                    Text(
                                        text = expense.description,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = expense.category,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                            Text(
                                text = "$${String.format("%.2f", expense.amount)}",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (index < analytics.topExpenses.size - 1) {
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

private fun calculateAnalytics(tickets: List<Ticket>, jobs: List<Job>): CostAnalytics {
    val completedJobs = jobs.filter { it.status == "completed" && it.cost != null }
    val totalSpent = completedJobs.sumOf { it.cost ?: 0 }.toFloat()
    val ticketCount = completedJobs.size
    val averageCost = if (ticketCount > 0) totalSpent / ticketCount else 0f
    
    // Cost by category
    val costByCategory = mutableMapOf<String, Float>()
    completedJobs.forEach { job ->
        val ticket = tickets.find { it.id == job.ticketId }
        val category = ticket?.category ?: "Unknown"
        costByCategory[category] = (costByCategory[category] ?: 0f) + (job.cost ?: 0).toFloat()
    }
    
    // Top expenses
    val topExpenses = completedJobs
        .mapNotNull { job ->
            val ticket = tickets.find { it.id == job.ticketId }
            if (ticket != null && job.cost != null) {
                com.example.mvp.data.ExpenseItem(
                    description = ticket.title,
                    amount = job.cost.toFloat(),
                    category = ticket.category,
                    date = job.date
                )
            } else null
        }
        .sortedByDescending { it.amount }
    
    // Monthly trend (simplified)
    val monthlyTrend = emptyList<com.example.mvp.data.MonthlyCost>()
    
    return CostAnalytics(
        totalSpent = totalSpent,
        averageCostPerTicket = averageCost,
        costByCategory = costByCategory,
        monthlyTrend = monthlyTrend,
        topExpenses = topExpenses
    )
}

