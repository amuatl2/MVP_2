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
import com.example.mvp.data.MaintenanceReminder
import com.example.mvp.data.ReminderFrequency
import com.example.mvp.utils.DateUtils
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceRemindersScreen(
    onBack: () -> Unit,
    reminders: List<MaintenanceReminder> = emptyList(),
    onCreateReminder: (MaintenanceReminder) -> Unit = {},
    onUpdateReminder: (MaintenanceReminder) -> Unit = {},
    onDeleteReminder: (String) -> Unit = {},
    properties: List<com.example.mvp.data.Property> = emptyList()
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedReminder by remember { mutableStateOf<MaintenanceReminder?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Maintenance Reminders", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showCreateDialog = true }) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add Reminder")
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
            if (reminders.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No Reminders Yet",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Create recurring maintenance reminders to stay on top of property upkeep",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = { showCreateDialog = true }) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Create Reminder")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(reminders) { reminder ->
                        ReminderCard(
                            reminder = reminder,
                            onClick = { selectedReminder = reminder },
                            onComplete = {
                                val calendar = Calendar.getInstance()
                                val nextDue = when (reminder.frequency) {
                                    ReminderFrequency.WEEKLY -> {
                                        calendar.add(Calendar.WEEK_OF_YEAR, 1)
                                        DateUtils.getCurrentDateString()
                                    }
                                    ReminderFrequency.MONTHLY -> {
                                        calendar.add(Calendar.MONTH, 1)
                                        DateUtils.getCurrentDateString()
                                    }
                                    ReminderFrequency.QUARTERLY -> {
                                        calendar.add(Calendar.MONTH, 3)
                                        DateUtils.getCurrentDateString()
                                    }
                                    ReminderFrequency.BIANNUAL -> {
                                        calendar.add(Calendar.MONTH, 6)
                                        DateUtils.getCurrentDateString()
                                    }
                                    ReminderFrequency.ANNUAL -> {
                                        calendar.add(Calendar.YEAR, 1)
                                        DateUtils.getCurrentDateString()
                                    }
                                    ReminderFrequency.CUSTOM -> reminder.nextDueDate
                                }
                                onUpdateReminder(
                                    reminder.copy(
                                        lastCompletedDate = DateUtils.getCurrentDateString(),
                                        nextDueDate = nextDue
                                    )
                                )
                            },
                            onDelete = { onDeleteReminder(reminder.id) }
                        )
                    }
                }
            }
        }
    }
    
    if (showCreateDialog) {
        CreateReminderDialog(
            properties = properties,
            onDismiss = { showCreateDialog = false },
            onCreate = { reminder ->
                onCreateReminder(reminder)
                showCreateDialog = false
            }
        )
    }
    
    selectedReminder?.let { reminder ->
        EditReminderDialog(
            reminder = reminder,
            properties = properties,
            onDismiss = { selectedReminder = null },
            onUpdate = { updated ->
                onUpdateReminder(updated)
                selectedReminder = null
            }
        )
    }
}

@Composable
fun ReminderCard(
    reminder: MaintenanceReminder,
    onClick: () -> Unit,
    onComplete: () -> Unit,
    onDelete: () -> Unit
) {
    val isOverdue = remember {
        val today = Calendar.getInstance()
        val dueDate = DateUtils.parseDate(reminder.nextDueDate)
        dueDate != null && dueDate.before(today.time)
    }
    
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isOverdue) {
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = reminder.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = reminder.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                if (isOverdue) {
                    Surface(
                        color = MaterialTheme.colorScheme.error,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = "OVERDUE",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onError
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = reminder.frequency.name.lowercase().replaceFirstChar { it.uppercase() },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                Text(
                    text = "•",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Text(
                    text = "Due: ${reminder.nextDueDate}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            
            reminder.lastCompletedDate?.let { lastCompleted ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Last completed: $lastCompleted",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onComplete,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Mark Complete")
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateReminderDialog(
    properties: List<com.example.mvp.data.Property>,
    onDismiss: () -> Unit,
    onCreate: (MaintenanceReminder) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var frequency by remember { mutableStateOf(ReminderFrequency.MONTHLY) }
    var selectedPropertyId by remember { mutableStateOf<String?>(null) }
    var frequencyExpanded by remember { mutableStateOf(false) }
    var propertyExpanded by remember { mutableStateOf(false) }
    
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.MONTH, 1)
    var nextDueDate by remember { mutableStateOf(DateUtils.getCurrentDateString()) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Maintenance Reminder", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title *") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
                
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Frequency dropdown
                Box {
                    OutlinedTextField(
                        value = frequency.name.lowercase().replaceFirstChar { it.uppercase() },
                        onValueChange = { },
                        label = { Text("Frequency *") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { frequencyExpanded = true },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { frequencyExpanded = true }) {
                                Text("▼")
                            }
                        }
                    )
                    DropdownMenu(
                        expanded = frequencyExpanded,
                        onDismissRequest = { frequencyExpanded = false }
                    ) {
                        ReminderFrequency.values().forEach { freq ->
                            DropdownMenuItem(
                                text = { Text(freq.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                onClick = {
                                    frequency = freq
                                    frequencyExpanded = false
                                }
                            )
                        }
                    }
                }
                
                OutlinedTextField(
                    value = nextDueDate,
                    onValueChange = { nextDueDate = it },
                    label = { Text("Next Due Date (YYYY-MM-DD) *") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotEmpty() && nextDueDate.isNotEmpty()) {
                        onCreate(
                            MaintenanceReminder(
                                id = "reminder-${System.currentTimeMillis()}",
                                title = title,
                                description = description,
                                category = category,
                                frequency = frequency,
                                nextDueDate = nextDueDate,
                                lastCompletedDate = null,
                                propertyId = selectedPropertyId,
                                isActive = true
                            )
                        )
                    }
                },
                enabled = title.isNotEmpty() && nextDueDate.isNotEmpty()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditReminderDialog(
    reminder: MaintenanceReminder,
    properties: List<com.example.mvp.data.Property>,
    onDismiss: () -> Unit,
    onUpdate: (MaintenanceReminder) -> Unit
) {
    var title by remember { mutableStateOf(reminder.title) }
    var description by remember { mutableStateOf(reminder.description) }
    var category by remember { mutableStateOf(reminder.category) }
    var frequency by remember { mutableStateOf(reminder.frequency) }
    var nextDueDate by remember { mutableStateOf(reminder.nextDueDate) }
    var frequencyExpanded by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Reminder", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title *") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
                
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Box {
                    OutlinedTextField(
                        value = frequency.name.lowercase().replaceFirstChar { it.uppercase() },
                        onValueChange = { },
                        label = { Text("Frequency *") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { frequencyExpanded = true },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { frequencyExpanded = true }) {
                                Text("▼")
                            }
                        }
                    )
                    DropdownMenu(
                        expanded = frequencyExpanded,
                        onDismissRequest = { frequencyExpanded = false }
                    ) {
                        ReminderFrequency.values().forEach { freq ->
                            DropdownMenuItem(
                                text = { Text(freq.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                onClick = {
                                    frequency = freq
                                    frequencyExpanded = false
                                }
                            )
                        }
                    }
                }
                
                OutlinedTextField(
                    value = nextDueDate,
                    onValueChange = { nextDueDate = it },
                    label = { Text("Next Due Date (YYYY-MM-DD) *") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotEmpty() && nextDueDate.isNotEmpty()) {
                        onUpdate(
                            reminder.copy(
                                title = title,
                                description = description,
                                category = category,
                                frequency = frequency,
                                nextDueDate = nextDueDate
                            )
                        )
                    }
                },
                enabled = title.isNotEmpty() && nextDueDate.isNotEmpty()
            ) {
                Text("Update")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

