package com.example.mvp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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

data class EnhancedReview(
    val id: String,
    val contractorId: String,
    val ticketId: String,
    val jobId: String?,
    val rating: Float, // Overall rating 1-5
    val qualityRating: Float, // Work quality 1-5
    val timelinessRating: Float, // On-time completion 1-5
    val communicationRating: Float, // Communication 1-5
    val valueRating: Float, // Value for money 1-5
    val comment: String,
    val photos: List<String> = emptyList(),
    val reviewerEmail: String,
    val reviewerName: String,
    val timestamp: String,
    val contractorResponse: String? = null,
    val contractorResponseTimestamp: String? = null,
    val isVerified: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedReviewScreen(
    onBack: () -> Unit,
    contractor: Contractor?,
    ticketId: String?,
    jobId: String?,
    onSubmitReview: (EnhancedReview) -> Unit = {},
    existingReviews: List<EnhancedReview> = emptyList()
) {
    var overallRating by remember { mutableStateOf(0f) }
    var qualityRating by remember { mutableStateOf(0f) }
    var timelinessRating by remember { mutableStateOf(0f) }
    var communicationRating by remember { mutableStateOf(0f) }
    var valueRating by remember { mutableStateOf(0f) }
    var comment by remember { mutableStateOf("") }
    var reviewPhotos by remember { mutableStateOf<List<String>>(emptyList()) }
    var showExistingReviews by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Review Contractor", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (existingReviews.isNotEmpty()) {
                        IconButton(onClick = { showExistingReviews = !showExistingReviews }) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "View Reviews"
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            contractor?.let {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
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
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            repeat(5) { index ->
                                Text(
                                    text = if (index < it.rating.toInt()) "★" else "☆",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 20.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${it.rating} (${it.completedJobs} jobs)",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
            
            // Overall Rating
            RatingSection(
                title = "Overall Rating",
                rating = overallRating,
                onRatingChange = { overallRating = it }
            )
            
            // Detailed Ratings
            RatingSection(
                title = "Work Quality",
                rating = qualityRating,
                onRatingChange = { qualityRating = it }
            )
            
            RatingSection(
                title = "Timeliness",
                rating = timelinessRating,
                onRatingChange = { timelinessRating = it }
            )
            
            RatingSection(
                title = "Communication",
                rating = communicationRating,
                onRatingChange = { communicationRating = it }
            )
            
            RatingSection(
                title = "Value for Money",
                rating = valueRating,
                onRatingChange = { valueRating = it }
            )
            
            // Comment
            OutlinedTextField(
                value = comment,
                onValueChange = { comment = it },
                label = { Text("Write your review (optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4,
                maxLines = 8
            )
            
            // Photo upload (placeholder)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Add Photos (Optional)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Upload photos of the completed work",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(onClick = { /* TODO: Implement photo upload */ }) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Photos")
                    }
                }
            }
            
            // Submit button
            Button(
                onClick = {
                    if (overallRating > 0 && contractor != null && ticketId != null) {
                        val review = EnhancedReview(
                            id = "review-${System.currentTimeMillis()}",
                            contractorId = contractor.id,
                            ticketId = ticketId,
                            jobId = jobId,
                            rating = overallRating,
                            qualityRating = qualityRating,
                            timelinessRating = timelinessRating,
                            communicationRating = communicationRating,
                            valueRating = valueRating,
                            comment = comment,
                            photos = reviewPhotos,
                            reviewerEmail = "", // Will be set by ViewModel
                            reviewerName = "", // Will be set by ViewModel
                            timestamp = com.example.mvp.utils.DateUtils.getCurrentDateTimeString(),
                            isVerified = false
                        )
                        onSubmitReview(review)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = overallRating > 0 && contractor != null && ticketId != null
            ) {
                Text("Submit Review", fontSize = 16.sp)
            }
        }
        
        if (showExistingReviews && existingReviews.isNotEmpty()) {
            ExistingReviewsDialog(
                reviews = existingReviews,
                onDismiss = { showExistingReviews = false }
            )
        }
    }
}

@Composable
fun RatingSection(
    title: String,
    rating: Float,
    onRatingChange: (Float) -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(5) { index ->
                IconButton(
                    onClick = { onRatingChange((index + 1).toFloat()) },
                    modifier = Modifier.size(40.dp)
                ) {
                    Text(
                        text = if (index < rating.toInt()) "★" else "☆",
                        style = MaterialTheme.typography.headlineMedium,
                        color = if (index < rating.toInt()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        fontSize = 32.sp
                    )
                }
            }
            if (rating > 0) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${rating.toInt()}.0",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExistingReviewsDialog(
    reviews: List<EnhancedReview>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("All Reviews (${reviews.size})", fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(reviews) { review ->
                    ReviewCard(review = review)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun ReviewCard(review: EnhancedReview) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = review.reviewerName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    repeat(5) { index ->
                        Text(
                            text = if (index < review.rating.toInt()) "★" else "☆",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 16.sp
                        )
                    }
                }
            }
            if (review.comment.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = review.comment,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Quality: ${review.qualityRating.toInt()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Text(
                    text = "•",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Text(
                    text = "Timely: ${review.timelinessRating.toInt()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Text(
                    text = "•",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Text(
                    text = review.timestamp.split("T").firstOrNull() ?: "",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            review.contractorResponse?.let { response ->
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = "Contractor Response:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = response,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

