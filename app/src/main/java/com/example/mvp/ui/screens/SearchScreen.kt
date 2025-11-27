package com.example.mvp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.mvp.data.TicketStatus
import com.example.mvp.data.Job
import com.example.mvp.utils.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onBack: () -> Unit,
    tickets: List<Ticket> = emptyList(),
    jobs: List<Job> = emptyList(),
    onTicketClick: (String) -> Unit = {},
    onJobClick: (String) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(SearchFilter.ALL) }
    var showFilters by remember { mutableStateOf(false) }
    var dateRangeStart by remember { mutableStateOf("") }
    var dateRangeEnd by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var selectedStatus by remember { mutableStateOf<TicketStatus?>(null) }
    
    val filteredResults = remember(searchQuery, selectedFilter, dateRangeStart, dateRangeEnd, selectedCategory, selectedStatus) {
        val query = searchQuery.lowercase()
        val results = when (selectedFilter) {
            SearchFilter.ALL -> {
                val ticketResults = tickets.filter { ticket ->
                    (query.isEmpty() || ticket.title.lowercase().contains(query) || 
                     ticket.description.lowercase().contains(query) ||
                     ticket.category.lowercase().contains(query) ||
                     ticket.submittedBy.lowercase().contains(query)) &&
                    (selectedCategory == null || ticket.category == selectedCategory) &&
                    (selectedStatus == null || ticket.status == selectedStatus) &&
                    (dateRangeStart.isEmpty() || ticket.createdDate?.let { it >= dateRangeStart } != false) &&
                    (dateRangeEnd.isEmpty() || ticket.createdDate?.let { it <= dateRangeEnd } != false)
                }
                val jobResults = jobs.filter { job ->
                    (query.isEmpty() || job.issueType.lowercase().contains(query) ||
                     job.propertyAddress.lowercase().contains(query)) &&
                    (dateRangeStart.isEmpty() || job.date >= dateRangeStart) &&
                    (dateRangeEnd.isEmpty() || job.date <= dateRangeEnd)
                }
                ticketResults.map { SearchResult.TicketResult(it) } + 
                jobResults.map { SearchResult.JobResult(it) }
            }
            SearchFilter.TICKETS -> {
                tickets.filter { ticket ->
                    (query.isEmpty() || ticket.title.lowercase().contains(query) || 
                     ticket.description.lowercase().contains(query) ||
                     ticket.category.lowercase().contains(query) ||
                     ticket.submittedBy.lowercase().contains(query)) &&
                    (selectedCategory == null || ticket.category == selectedCategory) &&
                    (selectedStatus == null || ticket.status == selectedStatus) &&
                    (dateRangeStart.isEmpty() || ticket.createdDate?.let { it >= dateRangeStart } != false) &&
                    (dateRangeEnd.isEmpty() || ticket.createdDate?.let { it <= dateRangeEnd } != false)
                }.map { SearchResult.TicketResult(it) }
            }
            SearchFilter.JOBS -> {
                jobs.filter { job ->
                    (query.isEmpty() || job.issueType.lowercase().contains(query) ||
                     job.propertyAddress.lowercase().contains(query)) &&
                    (dateRangeStart.isEmpty() || job.date >= dateRangeStart) &&
                    (dateRangeEnd.isEmpty() || job.date <= dateRangeEnd)
                }.map { SearchResult.JobResult(it) }
            }
        }
        results.sortedByDescending { result ->
            when (result) {
                is SearchResult.TicketResult -> result.ticket.createdAt
                is SearchResult.JobResult -> result.job.date
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Search", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showFilters = !showFilters }) {
                        Icon(
                            imageVector = if (showFilters) Icons.Default.Close else Icons.Default.Info,
                            contentDescription = "Filters"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search tickets, jobs, descriptions...") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true
            )
            
            // Filter chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SearchFilter.values().forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter.name.lowercase().replaceFirstChar { it.uppercase() }) }
                    )
                }
            }
            
            // Advanced filters
            if (showFilters) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Advanced Filters",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = dateRangeStart,
                                onValueChange = { dateRangeStart = it },
                                label = { Text("Start Date (YYYY-MM-DD)") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = dateRangeEnd,
                                onValueChange = { dateRangeEnd = it },
                                label = { Text("End Date (YYYY-MM-DD)") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        
                        if (selectedFilter == SearchFilter.ALL || selectedFilter == SearchFilter.TICKETS) {
                            val categories = listOf("Plumbing", "Electrical", "HVAC", "Appliance", "General Maintenance")
                            var categoryExpanded by remember { mutableStateOf(false) }
                            
                            Box {
                                OutlinedTextField(
                                    value = selectedCategory ?: "All Categories",
                                    onValueChange = { },
                                    label = { Text("Category") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { categoryExpanded = true },
                                    readOnly = true,
                                    trailingIcon = {
                                        IconButton(onClick = { categoryExpanded = true }) {
                                            Text("▼")
                                        }
                                    }
                                )
                                DropdownMenu(
                                    expanded = categoryExpanded,
                                    onDismissRequest = { categoryExpanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("All Categories") },
                                        onClick = {
                                            selectedCategory = null
                                            categoryExpanded = false
                                        }
                                    )
                                    categories.forEach { cat ->
                                        DropdownMenuItem(
                                            text = { Text(cat) },
                                            onClick = {
                                                selectedCategory = cat
                                                categoryExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                            
                            var statusExpanded by remember { mutableStateOf(false) }
                            Box {
                                OutlinedTextField(
                                    value = selectedStatus?.name ?: "All Statuses",
                                    onValueChange = { },
                                    label = { Text("Status") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { statusExpanded = true },
                                    readOnly = true,
                                    trailingIcon = {
                                        IconButton(onClick = { statusExpanded = true }) {
                                            Text("▼")
                                        }
                                    }
                                )
                                DropdownMenu(
                                    expanded = statusExpanded,
                                    onDismissRequest = { statusExpanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("All Statuses") },
                                        onClick = {
                                            selectedStatus = null
                                            statusExpanded = false
                                        }
                                    )
                                    TicketStatus.values().forEach { status ->
                                        DropdownMenuItem(
                                            text = { Text(status.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                            onClick = {
                                                selectedStatus = status
                                                statusExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // Results
            Text(
                text = "${filteredResults.size} result${if (filteredResults.size != 1) "s" else ""}",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredResults) { result ->
                    when (result) {
                        is SearchResult.TicketResult -> {
                            TicketSearchCard(
                                ticket = result.ticket,
                                onClick = { onTicketClick(result.ticket.id) }
                            )
                        }
                        is SearchResult.JobResult -> {
                            JobSearchCard(
                                job = result.job,
                                onClick = { onJobClick(result.job.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

enum class SearchFilter {
    ALL, TICKETS, JOBS
}

sealed class SearchResult {
    data class TicketResult(val ticket: Ticket) : SearchResult()
    data class JobResult(val job: Job) : SearchResult()
}

@Composable
fun TicketSearchCard(
    ticket: Ticket,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = ticket.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    color = when (ticket.status) {
                        TicketStatus.COMPLETED -> MaterialTheme.colorScheme.tertiaryContainer
                        TicketStatus.ASSIGNED -> MaterialTheme.colorScheme.primaryContainer
                        TicketStatus.SCHEDULED -> MaterialTheme.colorScheme.secondaryContainer
                        else -> MaterialTheme.colorScheme.errorContainer
                    },
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = ticket.status.name.lowercase().replaceFirstChar { it.uppercase() },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = ticket.description.take(150) + if (ticket.description.length > 150) "..." else "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(8.dp))
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
    }
}

@Composable
fun JobSearchCard(
    job: Job,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = job.issueType,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    color = when (job.status.lowercase()) {
                        "completed" -> MaterialTheme.colorScheme.tertiaryContainer
                        "in progress" -> MaterialTheme.colorScheme.primaryContainer
                        else -> MaterialTheme.colorScheme.secondaryContainer
                    },
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = job.status.lowercase().replaceFirstChar { it.uppercase() },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = job.propertyAddress,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Date: ${job.date}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                job.cost?.let { cost ->
                    Text(
                        text = "•",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "Cost: $$cost",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

