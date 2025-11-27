package com.example.mvp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.example.mvp.data.Contractor
import com.example.mvp.utils.DateUtils
import java.util.Calendar

data class TimeSlot(
    val id: String,
    val startTime: String, // HH:mm format
    val endTime: String,
    val isAvailable: Boolean = true,
    val isBooked: Boolean = false
)

data class ContractorAvailability(
    val contractorId: String,
    val date: String, // yyyy-MM-dd
    val availableSlots: List<TimeSlot>,
    val isAvailable: Boolean = true
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedScheduleScreen(
    onBack: () -> Unit,
    ticketId: String?,
    contractor: Contractor?,
    contractorAvailability: List<ContractorAvailability> = emptyList(),
    onScheduleAppointment: (String, String, String) -> Unit = { _, _, _ -> }, // date, startTime, endTime
    onReschedule: (String, String, String) -> Unit = { _, _, _ -> }
) {
    var selectedDate by remember { mutableStateOf<String?>(null) }
    var selectedTimeSlot by remember { mutableStateOf<TimeSlot?>(null) }
    var showSuggestions by remember { mutableStateOf(false) }
    
    val calendar = Calendar.getInstance()
    val today = DateUtils.getCurrentDateString()
    
    // Generate next 30 days
    val availableDates = remember {
        (0..30).map { daysAhead ->
            calendar.apply {
                time = java.util.Date()
                add(Calendar.DAY_OF_YEAR, daysAhead)
            }
            DateUtils.getCurrentDateString() // Simplified - would use actual date calculation
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Schedule Appointment", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (contractor != null) {
                        IconButton(onClick = { showSuggestions = !showSuggestions }) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Suggestions"
                            )
                        }
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
            contractor?.let {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = it.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = it.company,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            
            if (showSuggestions) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Suggested Times",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Based on contractor availability and your preferences, we recommend:",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        // Suggested times would be calculated based on contractor availability
                        Text(
                            text = "• Tomorrow at 10:00 AM - 12:00 PM",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "• Next Monday at 2:00 PM - 4:00 PM",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            Text(
                text = "Select Date",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            
            // Date selector
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(availableDates.take(14)) { date ->
                    DateChip(
                        date = date,
                        isSelected = selectedDate == date,
                        onClick = { selectedDate = date }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (selectedDate != null) {
                Text(
                    text = "Available Time Slots",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                
                // Generate time slots (9 AM to 5 PM, hourly)
                val timeSlots = remember(selectedDate) {
                    (9..16).map { hour ->
                        TimeSlot(
                            id = "slot-$hour",
                            startTime = String.format("%02d:00", hour),
                            endTime = String.format("%02d:00", hour + 1),
                            isAvailable = true // Would check against contractor availability
                        )
                    }
                }
                
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(timeSlots) { slot ->
                        TimeSlotCard(
                            slot = slot,
                            isSelected = selectedTimeSlot?.id == slot.id,
                            onClick = { selectedTimeSlot = slot }
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Select a date to view available time slots",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
        
        // Confirm button
        if (selectedDate != null && selectedTimeSlot != null) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            selectedDate = null
                            selectedTimeSlot = null
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            if (selectedDate != null && selectedTimeSlot != null) {
                                onScheduleAppointment(
                                    selectedDate!!,
                                    selectedTimeSlot!!.startTime,
                                    selectedTimeSlot!!.endTime
                                )
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Confirm Appointment")
                    }
                }
            }
        }
    }
}

@Composable
fun DateChip(
    date: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Text(
                    text = date.split("-").lastOrNull() ?: "",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = getDayName(date),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    )
}

@Composable
fun TimeSlotCard(
    slot: TimeSlot,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else if (!slot.isAvailable) {
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(
                2.dp,
                MaterialTheme.colorScheme.primary
            )
        } else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Column {
                    Text(
                        text = "${slot.startTime} - ${slot.endTime}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (!slot.isAvailable) {
                        Text(
                            text = "Not Available",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

fun getDayName(date: String): String {
    // Simplified - would parse date and get day name
    return "Mon" // Placeholder
}

