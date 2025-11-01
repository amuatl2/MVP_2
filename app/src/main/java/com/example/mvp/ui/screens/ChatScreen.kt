package com.example.mvp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
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

data class ChatMessage(
    val id: String,
    val text: String,
    val sender: String,
    val timestamp: String,
    val isAI: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    onBack: () -> Unit
) {
    var messages by remember {
        mutableStateOf(
            listOf(
                ChatMessage(
                    id = "1",
                    text = "Hello! I'm your HOME AI Assistant. I can help you with maintenance questions, ticket tracking, contractor recommendations, scheduling, and more. How can I assist you today?",
                    sender = "AI Assistant",
                    timestamp = "Just now",
                    isAI = true
                )
            )
        )
    }
    var newMessage by remember { mutableStateOf("") }
    var isTyping by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    fun generateAIResponse(userMessage: String): String {
        val lowerMessage = userMessage.lowercase()
        return when {
            lowerMessage.contains("maintenance") || lowerMessage.contains("issue") || lowerMessage.contains("problem") -> {
                "I can help you with maintenance issues! You can create a ticket through the 'Create Ticket' tab. Describe your issue, select a category (Plumbing, Electrical, HVAC, or Appliance), and our AI will help diagnose it. What type of issue are you facing?"
            }
            lowerMessage.contains("ticket") || lowerMessage.contains("status") || lowerMessage.contains("update") -> {
                "To check your ticket status, go to your Dashboard and tap on any ticket card. You'll see a status tracker showing: Submitted → Assigned → Scheduled → Completed. You can also view all tickets in the History tab. Need help with a specific ticket?"
            }
            lowerMessage.contains("contractor") || lowerMessage.contains("find") || lowerMessage.contains("recommend") || lowerMessage.contains("assign") -> {
                "To find contractors, navigate to the Marketplace tab. You can filter by category, distance, ratings, and preferred status. I recommend checking contractors with 4.5+ star ratings for the best service. Would you like help finding a contractor for a specific issue?"
            }
            lowerMessage.contains("schedule") || lowerMessage.contains("appointment") || lowerMessage.contains("visit") -> {
                "You can schedule appointments through the Schedule tab. Select an available date from the calendar, choose a time slot that works for you, and confirm your selection. The system will automatically notify all parties. Ready to schedule?"
            }
            lowerMessage.contains("rating") || lowerMessage.contains("review") || lowerMessage.contains("feedback") -> {
                "After a job is completed, you can rate the contractor through the Rating tab. Your feedback helps improve our service and helps other users find reliable contractors. Ratings are based on a 5-star system. Have you completed a job recently?"
            }
            lowerMessage.contains("hello") || lowerMessage.contains("hi") || lowerMessage.contains("hey") -> {
                "Hello! I'm here to help you with HOME platform questions. You can ask me about creating tickets, finding contractors, scheduling appointments, rating contractors, viewing history, or anything else related to property maintenance!"
            }
            lowerMessage.contains("help") || lowerMessage.contains("what can you do") -> {
                "I can help with: ✓ Creating maintenance tickets\n✓ Tracking ticket status\n✓ Finding and recommending contractors\n✓ Scheduling appointments\n✓ Rating contractors\n✓ Viewing maintenance history\n✓ Navigating the HOME platform\n\nWhat would you like to know?"
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
                                text = "AI Assistant",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "HOME Support",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                                    Text("🤖", fontSize = 16.sp)
                                    Text("AI Assistant is typing...", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
                
                items(messages.reversed()) { message ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (message.sender == "You") 
                            Arrangement.End 
                        else 
                            Arrangement.Start
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = when {
                                    message.isAI -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                                    message.sender == "You" -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f)
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
                                        color = if (message.isAI) 
                                            MaterialTheme.colorScheme.primary 
                                        else 
                                            MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = message.text,
                                    style = MaterialTheme.typography.bodyMedium,
                                    lineHeight = 20.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = message.timestamp,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    fontSize = 11.sp
                                )
                            }
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newMessage,
                        onValueChange = { newMessage = it },
                        placeholder = { Text("Ask the AI assistant...") },
                        modifier = Modifier.weight(1f),
                        maxLines = 3,
                        shape = MaterialTheme.shapes.medium
                    )
                    FilledIconButton(
                        onClick = {
                            if (newMessage.isNotBlank()) {
                                val userMessage = newMessage
                                val userMsg = ChatMessage(
                                    id = "${messages.size + 1}",
                                    text = userMessage,
                                    sender = "You",
                                    timestamp = "Now"
                                )
                                messages = messages + userMsg
                                newMessage = ""
                                
                                // Simulate AI typing
                                isTyping = true
                                
                                // Generate AI response after delay
                                CoroutineScope(Dispatchers.Main).launch {
                                    delay(1500) // Typing indicator
                                    isTyping = false
                                    val aiResponse = generateAIResponse(userMessage)
                                    val aiMsg = ChatMessage(
                                        id = "${messages.size + 2}",
                                        text = aiResponse,
                                        sender = "AI Assistant",
                                        timestamp = "Just now",
                                        isAI = true
                                    )
                                    messages = messages + aiMsg
                                    scope.launch {
                                        listState.animateScrollToItem(0)
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
