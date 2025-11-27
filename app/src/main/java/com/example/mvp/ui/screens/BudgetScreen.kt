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
import com.example.mvp.data.Property

data class Budget(
    val id: String,
    val propertyId: String?,
    val category: String,
    val monthlyBudget: Float,
    val yearlyBudget: Float,
    val currentSpent: Float,
    val alertThreshold: Float = 0.8f, // Alert when 80% spent
    val isActive: Boolean = true
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    onBack: () -> Unit,
    budgets: List<Budget> = emptyList(),
    properties: List<Property> = emptyList(),
    onCreateBudget: (Budget) -> Unit = {},
    onUpdateBudget: (Budget) -> Unit = {},
    onDeleteBudget: (String) -> Unit = {}
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedBudget by remember { mutableStateOf<Budget?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Budget Management", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showCreateDialog = true }) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add Budget")
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
            // Summary cards
            val totalBudget = budgets.sumOf { it.yearlyBudget.toDouble() }.toFloat()
            val totalSpent = budgets.sumOf { it.currentSpent.toDouble() }.toFloat()
            val remaining = totalBudget - totalSpent
            val percentage = if (totalBudget > 0) (totalSpent / totalBudget) * 100f else 0f
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                BudgetSummaryCard(
                    title = "Total Budget",
                    amount = totalBudget,
                    modifier = Modifier.weight(1f)
                )
                BudgetSummaryCard(
                    title = "Spent",
                    amount = totalSpent,
                    modifier = Modifier.weight(1f),
                    color = if (percentage > 80) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
                BudgetSummaryCard(
                    title = "Remaining",
                    amount = remaining,
                    modifier = Modifier.weight(1f),
                    color = if (remaining < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary
                )
            }
            
            // Progress indicator
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Overall Budget Usage",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${percentage.toInt()}%",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (percentage > 80) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { percentage / 100f },
                        modifier = Modifier.fillMaxWidth(),
                        color = when {
                            percentage > 90 -> MaterialTheme.colorScheme.error
                            percentage > 80 -> MaterialTheme.colorScheme.secondary
                            else -> MaterialTheme.colorScheme.primary
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (budgets.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No Budgets Set",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Create budgets to track spending by category or property",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(budgets) { budget ->
                        BudgetCard(
                            budget = budget,
                            property = properties.find { it.id == budget.propertyId },
                            onClick = { selectedBudget = budget },
                            onDelete = { onDeleteBudget(budget.id) }
                        )
                    }
                }
            }
        }
    }
    
    if (showCreateDialog) {
        CreateBudgetDialog(
            properties = properties,
            onDismiss = { showCreateDialog = false },
            onCreate = { budget ->
                onCreateBudget(budget)
                showCreateDialog = false
            }
        )
    }
    
    selectedBudget?.let { budget ->
        EditBudgetDialog(
            budget = budget,
            properties = properties,
            onDismiss = { selectedBudget = null },
            onUpdate = { updated ->
                onUpdateBudget(updated)
                selectedBudget = null
            }
        )
    }
}

@Composable
fun BudgetSummaryCard(
    title: String,
    amount: Float,
    modifier: Modifier = Modifier,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$${String.format("%.2f", amount)}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun BudgetCard(
    budget: Budget,
    property: Property?,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val percentage = if (budget.yearlyBudget > 0) (budget.currentSpent / budget.yearlyBudget) * 100f else 0f
    val isOverBudget = budget.currentSpent > budget.yearlyBudget
    val isNearLimit = percentage >= (budget.alertThreshold * 100)
    
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isOverBudget -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                isNearLimit -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                else -> MaterialTheme.colorScheme.surface
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
                        text = budget.category,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    property?.let {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = it.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Budget",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "$${String.format("%.2f", budget.yearlyBudget)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Spent",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "$${String.format("%.2f", budget.currentSpent)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            isOverBudget -> MaterialTheme.colorScheme.error
                            isNearLimit -> MaterialTheme.colorScheme.secondary
                            else -> MaterialTheme.colorScheme.primary
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LinearProgressIndicator(
                progress = { (percentage / 100f).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth(),
                color = when {
                    isOverBudget -> MaterialTheme.colorScheme.error
                    isNearLimit -> MaterialTheme.colorScheme.secondary
                    else -> MaterialTheme.colorScheme.primary
                }
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${percentage.toInt()}% used",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                if (isOverBudget) {
                    Surface(
                        color = MaterialTheme.colorScheme.error,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = "OVER BUDGET",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onError
                        )
                    }
                } else if (isNearLimit) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondary,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = "NEAR LIMIT",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondary
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateBudgetDialog(
    properties: List<Property>,
    onDismiss: () -> Unit,
    onCreate: (Budget) -> Unit
) {
    var category by remember { mutableStateOf("") }
    var monthlyBudget by remember { mutableStateOf("") }
    var yearlyBudget by remember { mutableStateOf("") }
    var selectedPropertyId by remember { mutableStateOf<String?>(null) }
    var propertyExpanded by remember { mutableStateOf(false) }
    
    val categories = listOf("Plumbing", "Electrical", "HVAC", "Appliance", "General Maintenance", "All Categories")
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Budget", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category *") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                if (properties.isNotEmpty()) {
                    Box {
                        OutlinedTextField(
                            value = properties.find { it.id == selectedPropertyId }?.name ?: "All Properties",
                            onValueChange = { },
                            label = { Text("Property (Optional)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { propertyExpanded = true },
                            readOnly = true,
                            trailingIcon = {
                                IconButton(onClick = { propertyExpanded = true }) {
                                    Text("▼")
                                }
                            }
                        )
                        DropdownMenu(
                            expanded = propertyExpanded,
                            onDismissRequest = { propertyExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("All Properties") },
                                onClick = {
                                    selectedPropertyId = null
                                    propertyExpanded = false
                                }
                            )
                            properties.forEach { property ->
                                DropdownMenuItem(
                                    text = { Text(property.name) },
                                    onClick = {
                                        selectedPropertyId = property.id
                                        propertyExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                
                OutlinedTextField(
                    value = monthlyBudget,
                    onValueChange = { monthlyBudget = it },
                    label = { Text("Monthly Budget ($) *") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = yearlyBudget,
                    onValueChange = { yearlyBudget = it },
                    label = { Text("Yearly Budget ($) *") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val monthly = monthlyBudget.toFloatOrNull() ?: 0f
                    val yearly = yearlyBudget.toFloatOrNull() ?: 0f
                    if (category.isNotEmpty() && (monthly > 0 || yearly > 0)) {
                        onCreate(
                            Budget(
                                id = "budget-${System.currentTimeMillis()}",
                                propertyId = selectedPropertyId,
                                category = category,
                                monthlyBudget = monthly,
                                yearlyBudget = yearly,
                                currentSpent = 0f
                            )
                        )
                    }
                },
                enabled = category.isNotEmpty() && 
                    (monthlyBudget.toFloatOrNull() ?: 0f) > 0 || 
                    (yearlyBudget.toFloatOrNull() ?: 0f) > 0
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
fun EditBudgetDialog(
    budget: Budget,
    properties: List<Property>,
    onDismiss: () -> Unit,
    onUpdate: (Budget) -> Unit
) {
    var category by remember { mutableStateOf(budget.category) }
    var monthlyBudget by remember { mutableStateOf(budget.monthlyBudget.toString()) }
    var yearlyBudget by remember { mutableStateOf(budget.yearlyBudget.toString()) }
    var selectedPropertyId by remember { mutableStateOf(budget.propertyId) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Budget", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category *") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = monthlyBudget,
                    onValueChange = { monthlyBudget = it },
                    label = { Text("Monthly Budget ($) *") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = yearlyBudget,
                    onValueChange = { yearlyBudget = it },
                    label = { Text("Yearly Budget ($) *") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val monthly = monthlyBudget.toFloatOrNull() ?: 0f
                    val yearly = yearlyBudget.toFloatOrNull() ?: 0f
                    if (category.isNotEmpty() && (monthly > 0 || yearly > 0)) {
                        onUpdate(
                            budget.copy(
                                category = category,
                                monthlyBudget = monthly,
                                yearlyBudget = yearly
                            )
                        )
                    }
                },
                enabled = category.isNotEmpty() && 
                    (monthlyBudget.toFloatOrNull() ?: 0f) > 0 || 
                    (yearlyBudget.toFloatOrNull() ?: 0f) > 0
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

