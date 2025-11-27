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

data class QuickAction(
    val id: String,
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val onClick: () -> Unit
)

data class MaintenanceTemplate(
    val id: String,
    val title: String,
    val category: String,
    val description: String,
    val suggestedPriority: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TenantPortalScreen(
    onBack: () -> Unit,
    onCreateTicket: () -> Unit = {},
    onViewTickets: () -> Unit = {},
    onViewHistory: () -> Unit = {},
    onContactSupport: () -> Unit = {}
) {
    var showFAQ by remember { mutableStateOf(false) }
    var showTemplates by remember { mutableStateOf(false) }
    var selectedTemplate by remember { mutableStateOf<MaintenanceTemplate?>(null) }
    
    val quickActions = remember {
        listOf(
            QuickAction(
                id = "create_ticket",
                title = "Report Issue",
                description = "Create a new maintenance ticket",
                icon = Icons.Default.AddCircle,
                onClick = onCreateTicket
            ),
            QuickAction(
                id = "view_tickets",
                title = "My Tickets",
                description = "View all your maintenance tickets",
                icon = Icons.Default.List,
                onClick = onViewTickets
            ),
            QuickAction(
                id = "history",
                title = "History",
                description = "View completed maintenance",
                icon = Icons.Default.Info,
                onClick = onViewHistory
            ),
            QuickAction(
                id = "support",
                title = "Contact Support",
                description = "Get help from support team",
                icon = Icons.Default.Info,
                onClick = onContactSupport
            )
        )
    }
    
    val templates = remember {
        listOf(
            MaintenanceTemplate(
                id = "plumbing_leak",
                title = "Water Leak",
                category = "Plumbing",
                description = "Water leaking from pipe, faucet, or fixture",
                suggestedPriority = "High"
            ),
            MaintenanceTemplate(
                id = "electrical_outlet",
                title = "Electrical Outlet Not Working",
                category = "Electrical",
                description = "Outlet not providing power",
                suggestedPriority = "Medium"
            ),
            MaintenanceTemplate(
                id = "hvac_no_heat",
                title = "No Heat/Cooling",
                category = "HVAC",
                description = "Heating or cooling system not working",
                suggestedPriority = "High"
            ),
            MaintenanceTemplate(
                id = "appliance_broken",
                title = "Appliance Not Working",
                category = "Appliance",
                description = "Refrigerator, dishwasher, or other appliance malfunctioning",
                suggestedPriority = "Medium"
            )
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tenant Portal", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showFAQ = !showFAQ }) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = "FAQ")
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
            // Quick Actions
            item {
                Text(
                    text = "Quick Actions",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            items(quickActions.chunked(2)) { actionRow ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    actionRow.forEach { action ->
                        QuickActionCard(
                            action = action,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            
            // Maintenance Templates
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Common Issues",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = { showTemplates = !showTemplates }) {
                        Text(if (showTemplates) "Hide" else "Show All")
                    }
                }
            }
            
            if (showTemplates) {
                items(templates) { template ->
                    TemplateCard(
                        template = template,
                        onClick = { selectedTemplate = template }
                    )
                }
            } else {
                item {
                    templates.take(2).forEach { template ->
                        TemplateCard(
                            template = template,
                            onClick = { selectedTemplate = template }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
            
            // FAQ Section
            if (showFAQ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Frequently Asked Questions",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            FAQItem(
                                question = "How do I report a maintenance issue?",
                                answer = "Tap 'Report Issue' and fill out the form with details about the problem. Include photos if possible."
                            )
                            FAQItem(
                                question = "How long does it take to fix an issue?",
                                answer = "Response time varies by priority. Emergency issues are addressed within 24 hours, while routine maintenance may take 3-5 business days."
                            )
                            FAQItem(
                                question = "Can I track my maintenance request?",
                                answer = "Yes! View your tickets in 'My Tickets' to see the status and updates."
                            )
                            FAQItem(
                                question = "What should I do in an emergency?",
                                answer = "For emergencies like water leaks or electrical hazards, call the emergency maintenance line immediately."
                            )
                        }
                    }
                }
            }
        }
    }
    
    selectedTemplate?.let { template ->
        TemplateDialog(
            template = template,
            onDismiss = { selectedTemplate = null },
            onUseTemplate = {
                // Navigate to create ticket with template data
                onCreateTicket()
                selectedTemplate = null
            }
        )
    }
}

@Composable
fun QuickActionCard(
    action: QuickAction,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = action.onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = action.icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = action.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = action.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun TemplateCard(
    template: MaintenanceTemplate,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = template.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = template.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = template.category,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                    Surface(
                        color = when (template.suggestedPriority.lowercase()) {
                            "high" -> MaterialTheme.colorScheme.errorContainer
                            "medium" -> MaterialTheme.colorScheme.secondaryContainer
                            else -> MaterialTheme.colorScheme.tertiaryContainer
                        },
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = template.suggestedPriority,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Use Template",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateDialog(
    template: MaintenanceTemplate,
    onDismiss: () -> Unit,
    onUseTemplate: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Use Template: ${template.title}", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = template.description,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "This will pre-fill a new ticket with this information. You can edit it before submitting.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        },
        confirmButton = {
            Button(onClick = onUseTemplate) {
                Text("Use Template")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun FAQItem(
    question: String,
    answer: String
) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        onClick = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = question,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = if (expanded) "▲" else "▼",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = answer,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
}

