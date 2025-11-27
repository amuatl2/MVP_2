package com.example.mvp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import com.example.mvp.data.Ticket
import com.example.mvp.data.User
import com.example.mvp.data.UserRole
import com.example.mvp.utils.DateUtils

data class ChatMessage(
    val id: String,
    val text: String,
    val sender: String,
    val senderEmail: String? = null,
    val timestamp: String,
    val isAI: Boolean = false,
    val isSystem: Boolean = false,
    val relatedTicketId: String? = null
)

enum class ChatMode {
    AI_ASSISTANT,
    TICKET_CHAT,
    GENERAL
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    onBack: () -> Unit,
    tickets: List<Ticket> = emptyList(),
    currentUser: User? = null,
    onTicketClick: ((String) -> Unit)? = null
) {
    var chatMode by remember { mutableStateOf(ChatMode.AI_ASSISTANT) }
    var selectedTicket by remember { mutableStateOf<Ticket?>(null) }
    var messages by remember {
        mutableStateOf(
            listOf(
                ChatMessage(
                    id = "1",
                    text = "Hello! I'm your HOME AI Assistant. I can help you with maintenance questions, ticket tracking, contractor recommendations, scheduling, and more. How can I assist you today?",
                    sender = "AI Assistant",
                    timestamp = DateUtils.getCurrentDateTimeString(),
                    isAI = true
                )
            )
        )
    }
    var newMessage by remember { mutableStateOf("") }
    var isTyping by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Load ticket messages if in ticket chat mode
    LaunchedEffect(selectedTicket) {
        if (selectedTicket != null && chatMode == ChatMode.TICKET_CHAT) {
            val ticketMessages = selectedTicket!!.messages.map { msg ->
                ChatMessage(
                    id = msg.id,
                    text = msg.text,
                    sender = msg.senderName,
                    senderEmail = msg.senderEmail,
                    timestamp = msg.timestamp,
                    isAI = false,
                    relatedTicketId = selectedTicket!!.id
                )
            }
            messages = ticketMessages.ifEmpty {
                listOf(
                    ChatMessage(
                        id = "init",
                        text = "Start a conversation about this ticket: ${selectedTicket!!.title}",
                        sender = "System",
                        timestamp = DateUtils.getCurrentDateTimeString(),
                        isSystem = true,
                        relatedTicketId = selectedTicket!!.id
                    )
                )
            }
        }
    }

    fun generateAIResponse(userMessage: String, userRole: UserRole?): String {
        val lowerMessage = userMessage.lowercase()
        return when {
            lowerMessage.contains("maintenance") || lowerMessage.contains("issue") || lowerMessage.contains("problem") -> {
                "I can help you with maintenance issues! You can create a ticket through the 'Create Ticket' tab. Describe your issue, select a category (Plumbing, Electrical, HVAC, or Appliance), and our AI will help diagnose it. What type of issue are you facing?"
            }
            lowerMessage.contains("ticket") || lowerMessage.contains("status") || lowerMessage.contains("update") -> {
                val ticketCount = tickets.size
                "You have $ticketCount ticket${if (ticketCount != 1) "s" else ""} in the system. To check ticket status, go to your Dashboard and tap on any ticket card. You'll see a status tracker showing: Submitted → Assigned → Scheduled → Completed. Need help with a specific ticket?"
            }
            lowerMessage.contains("contractor") || lowerMessage.contains("find") || lowerMessage.contains("recommend") -> {
                if (userRole == UserRole.LANDLORD) {
                    "To find contractors, navigate to the Marketplace tab. You can filter by category, distance, ratings, and preferred status. I recommend checking contractors with 4.5+ star ratings for the best service. Would you like help finding a contractor for a specific issue?"
                } else {
                    "Contractors are assigned by landlords through the Marketplace. You can view contractor information once they're assigned to your ticket. Need help with something else?"
                }
            }
            lowerMessage.contains("schedule") || lowerMessage.contains("appointment") -> {
                "You can schedule appointments through the Schedule tab. Select an available date from the calendar, choose a time slot that works for you, and confirm your selection. The system will automatically notify all parties. Ready to schedule?"
            }
            lowerMessage.contains("cost") || lowerMessage.contains("price") || lowerMessage.contains("estimate") -> {
                "Cost estimates are provided in the AI diagnosis for each ticket. You can also view detailed cost analytics in the Analytics screen (available to landlords). Estimates vary based on issue severity and category."
            }
            lowerMessage.contains("ai") || lowerMessage.contains("diagnosis") -> {
                "Our AI automatically analyzes every ticket you create, providing detailed diagnosis, cost estimates, safety warnings, parts needed, and preventive maintenance tips. Landlords can view all AI diagnoses in the AI Diagnosis Center."
            }
            lowerMessage.contains("hello") || lowerMessage.contains("hi") || lowerMessage.contains("hey") -> {
                "Hello! I'm here to help you with HOME platform questions. You can ask me about creating tickets, finding contractors, scheduling appointments, rating contractors, viewing history, or anything else related to property maintenance!"
            }
            lowerMessage.contains("help") || lowerMessage.contains("what can you do") -> {
                "I can help with:\n✓ Creating maintenance tickets\n✓ Tracking ticket status\n✓ Finding and recommending contractors\n✓ Scheduling appointments\n✓ Rating contractors\n✓ Viewing maintenance history\n✓ Cost estimates and analytics\n✓ Navigating the HOME platform\n\nWhat would you like to know?"
            }
            else -> {
                "Thank you for your message! As the HOME AI Assistant, I specialize in helping with ticket management, contractor selection, scheduling, and platform navigation. Feel free to ask specific questions about maintenance issues, ticket status, contractor recommendations, or how to use any feature of the app."
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = androidx.compose.foundation.shape.CircleShape,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🤖", fontSize = 20.sp)
                            }
                        }
                        Column {
                            Text(
                                text = when (chatMode) {
                                    ChatMode.AI_ASSISTANT -> "AI Assistant"
                                    ChatMode.TICKET_CHAT -> selectedTicket?.title ?: "Ticket Chat"
                                    ChatMode.GENERAL -> "Chat"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = when (chatMode) {
                                    ChatMode.AI_ASSISTANT -> "HOME Support"
                                    ChatMode.TICKET_CHAT -> "Ticket Discussion"
                                    ChatMode.GENERAL -> "General Chat"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Mode switcher
                    if (tickets.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                chatMode = when (chatMode) {
                                    ChatMode.AI_ASSISTANT -> ChatMode.TICKET_CHAT
                                    ChatMode.TICKET_CHAT -> ChatMode.AI_ASSISTANT
                                    ChatMode.GENERAL -> ChatMode.AI_ASSISTANT
                                }
                                if (chatMode == ChatMode.TICKET_CHAT && selectedTicket == null) {
                                    selectedTicket = tickets.firstOrNull()
                                }
                            }
                        ) {
                            val switchIcon = if (chatMode == ChatMode.AI_ASSISTANT) Icons.Default.Email else Icons.Default.Info
                            Icon(
                                imageVector = switchIcon,
                                contentDescription = "Switch Mode"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Ticket selector for ticket chat mode
            if (chatMode == ChatMode.TICKET_CHAT && tickets.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Select Ticket:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium
                        )
                        tickets.take(3).forEach { ticket ->
                            FilterChip(
                                selected = selectedTicket?.id == ticket.id,
                                onClick = { selectedTicket = ticket },
                                label = { Text(ticket.title.take(15) + if (ticket.title.length > 15) "..." else "") }
                            )
                        }
                    }
                }
            }

            // Messages
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                state = listState,
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                reverseLayout = true
            ) {
                if (isTyping) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                ),
                                modifier = Modifier.widthIn(max = 280.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Text("AI Assistant is typing...", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
                
                items(messages.reversed()) { message ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (message.sender == "You" || (message.senderEmail == currentUser?.email)) 
                            Arrangement.End 
                        else 
                            Arrangement.Start
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = when {
                                    message.isSystem -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    message.isAI -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                                    message.sender == "You" || message.senderEmail == currentUser?.email -> 
                                        MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f)
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                }
                            ),
                            modifier = Modifier.widthIn(max = 300.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (message.isAI) {
                                        Surface(
                                            color = MaterialTheme.colorScheme.primary,
                                            shape = androidx.compose.foundation.shape.CircleShape,
                                            modifier = Modifier.size(18.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("🤖", fontSize = 10.sp)
                                            }
                                        }
                                    }
                                    Text(
                                        text = message.sender,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = when {
                                            message.isAI -> MaterialTheme.colorScheme.primary
                                            message.isSystem -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                            else -> MaterialTheme.colorScheme.onSurface
                                        }
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = message.text,
                                    style = MaterialTheme.typography.bodyMedium,
                                    lineHeight = 20.sp,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = DateUtils.formatTimestamp(message.timestamp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                        fontSize = 11.sp
                                    )
                                    if (message.relatedTicketId != null && onTicketClick != null) {
                                        TextButton(
                                            onClick = { onTicketClick(message.relatedTicketId) },
                                            modifier = Modifier.padding(0.dp)
                                        ) {
                                            Text(
                                                text = "View Ticket",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Quick Actions (for AI Assistant mode)
            if (chatMode == ChatMode.AI_ASSISTANT) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuickActionChip(
                            text = "Create Ticket",
                            icon = "📋",
                            onClick = { newMessage = "How do I create a ticket?" }
                        )
                        QuickActionChip(
                            text = "Check Status",
                            icon = "📊",
                            onClick = { newMessage = "How do I check ticket status?" }
                        )
                        if (currentUser?.role == UserRole.LANDLORD) {
                            QuickActionChip(
                                text = "Find Contractor",
                                icon = "👷",
                                onClick = { newMessage = "How do I find contractors?" }
                            )
                        }
                    }
                }
            }

            // Input
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newMessage,
                            onValueChange = { newMessage = it },
                            placeholder = { 
                                Text(
                                    when (chatMode) {
                                        ChatMode.AI_ASSISTANT -> "Ask the AI assistant..."
                                        ChatMode.TICKET_CHAT -> "Message about this ticket..."
                                        ChatMode.GENERAL -> "Type a message..."
                                    }
                                )
                            },
                            modifier = Modifier.weight(1f),
                            maxLines = 3,
                            shape = MaterialTheme.shapes.medium,
                            leadingIcon = if (chatMode == ChatMode.TICKET_CHAT) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Attach",
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            } else null
                        )
                        FilledIconButton(
                            onClick = {
                                if (newMessage.isNotBlank()) {
                                    val userMessage = newMessage
                                    val userMsg = ChatMessage(
                                        id = "${System.currentTimeMillis()}",
                                        text = userMessage,
                                        sender = currentUser?.name ?: "You",
                                        senderEmail = currentUser?.email,
                                        timestamp = DateUtils.getCurrentDateTimeString()
                                    )
                                    messages = messages + userMsg
                                    newMessage = ""
                                    
                                    // Handle ticket chat vs AI chat
                                    if (chatMode == ChatMode.TICKET_CHAT && selectedTicket != null) {
                                        // In ticket chat, message would be saved to ticket
                                        // For now, just add to local messages
                                        scope.launch {
                                            listState.animateScrollToItem(0)
                                        }
                                    } else if (chatMode == ChatMode.AI_ASSISTANT) {
                                        // Simulate AI typing
                                        isTyping = true
                                        
                                        // Generate AI response after delay
                                        CoroutineScope(Dispatchers.Main).launch {
                                            delay(1500) // Typing indicator
                                            isTyping = false
                                            val aiResponse = generateAIResponse(userMessage, currentUser?.role)
                                            val aiMsg = ChatMessage(
                                                id = "${System.currentTimeMillis()}",
                                                text = aiResponse,
                                                sender = "AI Assistant",
                                                timestamp = DateUtils.getCurrentDateTimeString(),
                                                isAI = true
                                            )
                                            messages = messages + aiMsg
                                            scope.launch {
                                                listState.animateScrollToItem(0)
                                            }
                                        }
                                    }
                                }
                            },
                            enabled = newMessage.isNotBlank() && !isTyping,
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Send",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionChip(
    text: String,
    icon: String,
    onClick: () -> Unit
) {
    FilterChip(
        selected = false,
        onClick = onClick,
        label = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(icon, fontSize = 14.sp)
                Text(text, style = MaterialTheme.typography.labelSmall)
            }
        }
    )
}
