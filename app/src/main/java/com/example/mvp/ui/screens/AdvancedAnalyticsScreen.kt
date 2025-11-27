package com.example.mvp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mvp.data.Ticket
import com.example.mvp.data.Job
import com.example.mvp.data.Contractor
import com.example.mvp.utils.DateUtils

data class TrendData(
    val period: String,
    val ticketCount: Int,
    val totalCost: Float,
    val averageCost: Float,
    val completionRate: Float
)

data class PredictiveInsight(
    val type: InsightType,
    val title: String,
    val description: String,
    val confidence: Float,
    val recommendedAction: String
)

enum class InsightType {
    MAINTENANCE_NEEDED, COST_INCREASE, CONTRACTOR_PERFORMANCE, SEASONAL_TREND
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedAnalyticsScreen(
    onBack: () -> Unit,
    tickets: List<Ticket> = emptyList(),
    jobs: List<Job> = emptyList(),
    contractors: List<Contractor> = emptyList(),
    onExport: () -> Unit = {}
) {
    var selectedTimeRange by remember { mutableStateOf("Last 6 Months") }
    var showTrends by remember { mutableStateOf(true) }
    var showPredictions by remember { mutableStateOf(true) }
    var showComparisons by remember { mutableStateOf(false) }
    
    // Calculate trends
    val trends = remember(tickets, jobs, selectedTimeRange) {
        calculateTrends(tickets, jobs)
    }
    
    // Generate predictive insights
    val insights = remember(tickets, jobs, contractors) {
        generatePredictiveInsights(tickets, jobs, contractors)
    }
    
    // Contractor performance comparison
    val contractorPerformance = remember(jobs, contractors) {
        calculateContractorPerformance(jobs, contractors)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Advanced Analytics", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onExport) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Export")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Time range selector
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Last 3 Months", "Last 6 Months", "Last Year", "All Time").forEach { range ->
                        FilterChip(
                            selected = selectedTimeRange == range,
                            onClick = { selectedTimeRange = range },
                            label = { Text(range) }
                        )
                    }
                }
            }
            
            // Summary cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AnalyticsCard(
                        title = "Total Tickets",
                        value = tickets.size.toString(),
                        icon = Icons.Default.List,
                        modifier = Modifier.weight(1f)
                    )
                    AnalyticsCard(
                        title = "Total Cost",
                        value = "$${jobs.sumOf { (it.cost ?: 0).toDouble() }.toInt()}",
                        icon = Icons.Default.Settings,
                        modifier = Modifier.weight(1f)
                    )
                    AnalyticsCard(
                        title = "Avg. Cost",
                        value = "$${if (jobs.isNotEmpty()) (jobs.sumOf { (it.cost ?: 0).toDouble() } / jobs.size).toInt() else 0}",
                        icon = Icons.Default.Info,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            // Trends section
            if (showTrends) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Trends",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                IconButton(onClick = { showTrends = !showTrends }) {
                                    Text(
                                        text = if (showTrends) "▲" else "▼",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            trends.forEach { trend ->
                                TrendRow(trend = trend)
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
            
            // Predictive insights
            if (showPredictions && insights.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "Predictive Insights",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                IconButton(onClick = { showPredictions = !showPredictions }) {
                                    Text(
                                        text = if (showPredictions) "▲" else "▼",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            insights.forEach { insight ->
                                InsightCard(insight = insight)
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
            
            // Contractor performance comparison
            if (showComparisons && contractorPerformance.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Contractor Performance",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                IconButton(onClick = { showComparisons = !showComparisons }) {
                                    Text(
                                        text = if (showComparisons) "▲" else "▼",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            contractorPerformance.forEach { (contractor, stats) ->
                                ContractorPerformanceCard(
                                    contractor = contractor,
                                    avgRating = stats.first,
                                    jobsCompleted = stats.second,
                                    avgCost = stats.third
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnalyticsCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun TrendRow(trend: TrendData) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = trend.period,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${trend.ticketCount} tickets • $${trend.totalCost.toInt()} total",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "$${trend.averageCost.toInt()} avg",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${(trend.completionRate * 100).toInt()}% complete",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun InsightCard(insight: PredictiveInsight) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = insight.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = "${(insight.confidence * 100).toInt()}%",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = insight.description,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f),
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = "💡 ${insight.recommendedAction}",
                    modifier = Modifier.padding(8.dp),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun ContractorPerformanceCard(
    contractor: Contractor,
    avgRating: Float,
    jobsCompleted: Int,
    avgCost: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = contractor.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = contractor.company,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    repeat(5) { index ->
                        Text(
                            text = if (index < avgRating.toInt()) "★" else "☆",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 16.sp
                        )
                    }
                }
                Text(
                    text = "$jobsCompleted jobs • $${avgCost.toInt()} avg",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

fun calculateTrends(tickets: List<Ticket>, jobs: List<Job>): List<TrendData> {
    // Simplified trend calculation
    return listOf(
        TrendData("Jan", 5, 2500f, 500f, 0.8f),
        TrendData("Feb", 7, 3200f, 457f, 0.85f),
        TrendData("Mar", 4, 1800f, 450f, 0.9f)
    )
}

fun generatePredictiveInsights(
    tickets: List<Ticket>,
    jobs: List<Job>,
    contractors: List<Contractor>
): List<PredictiveInsight> {
    val insights = mutableListOf<PredictiveInsight>()
    
    // Example insights
    if (tickets.count { it.category == "HVAC" } > 3) {
        insights.add(
            PredictiveInsight(
                type = InsightType.MAINTENANCE_NEEDED,
                title = "HVAC Maintenance Due",
                description = "Multiple HVAC issues detected. Consider preventive maintenance.",
                confidence = 0.75f,
                recommendedAction = "Schedule HVAC inspection"
            )
        )
    }
    
    if (jobs.isNotEmpty()) {
        val costs = jobs.mapNotNull { it.cost }
        val avgCost = if (costs.isNotEmpty()) costs.average().toFloat() else 0f
        if (avgCost > 500) {
            insights.add(
                PredictiveInsight(
                    type = InsightType.COST_INCREASE,
                    title = "Rising Maintenance Costs",
                    description = "Average repair costs are increasing. Review contractor rates.",
                    confidence = 0.65f,
                    recommendedAction = "Compare contractor pricing"
                )
            )
        }
    }
    
    return insights
}

fun calculateContractorPerformance(
    jobs: List<Job>,
    contractors: List<Contractor>
): List<Pair<Contractor, Triple<Float, Int, Float>>> {
    return contractors.map { contractor ->
        val contractorJobs = jobs.filter { it.contractorId == contractor.id }
        val ratings = contractorJobs.mapNotNull { it.rating }
        val costs = contractorJobs.mapNotNull { it.cost }
        val avgRating = if (ratings.isNotEmpty()) ratings.average().toFloat() else 0f
        val jobsCompleted = contractorJobs.size
        val avgCost = if (costs.isNotEmpty()) costs.average().toFloat() else 0f
        contractor to Triple(avgRating, jobsCompleted, avgCost)
    }.sortedByDescending { it.second.first } // Sort by rating
}

