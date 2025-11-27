package com.example.mvp.ui.screens

import android.Manifest
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import android.provider.MediaStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mvp.viewmodel.HomeViewModel
import com.example.mvp.ai.AIDiagnosisResult
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info

@Composable
fun CreateTicketScreen(
    onBack: () -> Unit,
    onSubmit: (String, String, String, String) -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("Medium") }
    var aiDiagnosisResult by remember { mutableStateOf<AIDiagnosisResult?>(null) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var submitted by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    if (submitted) {
        SuccessScreen(
            message = "Ticket Submitted Successfully!",
            subtitle = "Your maintenance request has been created and will be reviewed soon.",
            onBack = onBack
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Create Maintenance Ticket",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Report a maintenance issue for your property",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            TextButton(onClick = onBack) {
                Text("Cancel")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // AI-Powered Diagnosis Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("✨", fontSize = 32.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "AI-Powered Diagnosis",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Our AI will automatically analyze your issue and suggest the best category and contractor type.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Issue Details",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Provide information about the maintenance issue.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title *") },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Brief description of the issue") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description *") },
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            placeholder = { Text("Provide detailed information about the problem...") },
            maxLines = 5
        )

        Spacer(modifier = Modifier.height(16.dp))

        var expanded by remember { mutableStateOf(false) }
        var priorityExpanded by remember { mutableStateOf(false) }
        val categories = listOf("Plumbing", "Electrical", "HVAC", "Appliance", "General Maintenance")
        val priorities = listOf("Low", "Medium", "High", "Urgent")
        
        // Trigger AI analysis when category, description, or priority changes
        LaunchedEffect(category, description, priority) {
            if (category.isNotEmpty() && description.length > 10) {
                isAnalyzing = true
                try {
                    val result = viewModel.generateAIDiagnosis(title, description, category, priority)
                    aiDiagnosisResult = result
                } catch (e: Exception) {
                    // Error generating diagnosis - will use fallback
                } finally {
                    isAnalyzing = false
                }
            } else {
                aiDiagnosisResult = null
            }
        }

        Box {
            OutlinedTextField(
                value = category,
                onValueChange = { },
                label = { Text("Category *") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = true },
                readOnly = true,
                placeholder = { Text("Select category") },
                trailingIcon = {
                    IconButton(onClick = { expanded = true }) {
                        Text("▼")
                    }
                }
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                categories.forEach { cat ->
                    DropdownMenuItem(
                        text = { Text(cat) },
                        onClick = {
                            category = cat
                            expanded = false
                        }
                    )
                }
            }
        }

        // AI Diagnosis Preview
        if (isAnalyzing) {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "AI is analyzing your issue...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        } else if (aiDiagnosisResult != null && category.isNotEmpty() && description.length > 10) {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("🤖", fontSize = 24.sp)
                        Text(
                            text = "AI Diagnosis Preview",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "${(aiDiagnosisResult!!.confidence * 100).toInt()}% confidence",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                    Text(
                        text = aiDiagnosisResult!!.diagnosis,
                        style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            lineHeight = 18.sp
                        )
                    }
                    
                    // Root Cause Analysis
                    aiDiagnosisResult!!.rootCauseAnalysis?.let { rootCause ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Root Cause Analysis:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = rootCause,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 4.dp, top = 2.dp)
                        )
                    }
                    
                    // Safety Warnings
                    if (aiDiagnosisResult!!.safetyWarnings.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "⚠️ Safety Warnings:",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                                aiDiagnosisResult!!.safetyWarnings.take(2).forEach { warning ->
                                    Text(
                                        text = "• $warning",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                    
                    // Parts Needed
                    if (aiDiagnosisResult!!.partsNeeded.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Parts/Materials Needed:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        aiDiagnosisResult!!.partsNeeded.take(3).forEach { part ->
                            Text(
                                text = "• $part",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                            )
                        }
                    }
                    
                    // DIY Recommendation
                    aiDiagnosisResult!!.diyRecommendation?.let { diy ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "🔧 DIY Assessment:",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                Text(
                                    text = diy,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                    
                    // Preventive Maintenance
                    if (aiDiagnosisResult!!.preventiveMaintenance.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "💡 Preventive Maintenance Tips:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        aiDiagnosisResult!!.preventiveMaintenance.take(2).forEach { tip ->
                            Text(
                                text = "• $tip",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                            )
                        }
                    }
                    
                    // Cost and Time Estimates
                    if (aiDiagnosisResult!!.estimatedCost != null || aiDiagnosisResult!!.estimatedTime != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            aiDiagnosisResult!!.estimatedCost?.let { cost ->
                                Surface(
                                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                                    shape = MaterialTheme.shapes.small
                                ) {
                                    Text(
                                        text = "💰 $cost",
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                            aiDiagnosisResult!!.estimatedTime?.let { time ->
                                Surface(
                                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
                                    shape = MaterialTheme.shapes.small
                                ) {
                                    Text(
                                        text = "⏱️ $time",
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }
                            }
                        }
                    }
                    
                    // Suggested Actions
                    if (aiDiagnosisResult!!.suggestedActions.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Recommended Actions:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                        aiDiagnosisResult!!.suggestedActions.take(3).forEach { action ->
                            Text(
                                text = "• $action",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                            )
                        }
                        if (aiDiagnosisResult!!.suggestedActions.size > 3) {
                            Text(
                                text = "• ...and ${aiDiagnosisResult!!.suggestedActions.size - 3} more",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Priority Dropdown
        Box {
            OutlinedTextField(
                value = priority,
                onValueChange = { },
                label = { Text("Priority") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { priorityExpanded = true },
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { priorityExpanded = true }) {
                        Text("▼")
                    }
                }
            )
            DropdownMenu(
                expanded = priorityExpanded,
                onDismissRequest = { priorityExpanded = false }
            ) {
                priorities.forEach { pri ->
                    DropdownMenuItem(
                        text = { Text(pri) },
                        onClick = {
                            priority = pri
                            priorityExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Attachments (Optional)",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))

        var uploadedFiles by remember { mutableStateOf<List<Uri>>(emptyList()) }
        
        var imageUri by remember { mutableStateOf<Uri?>(null) }
        
        val cameraLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.TakePicture()
        ) { success ->
            if (success && imageUri != null) {
                uploadedFiles = uploadedFiles + imageUri!!
            }
        }
        
        val imagePickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            uri?.let {
                uploadedFiles = uploadedFiles + it
            }
        }
        
        val permissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                val newImageUri = android.content.ContentValues().apply {
                    put(MediaStore.Images.Media.TITLE, "HOME_Photo_${System.currentTimeMillis()}")
                }.let {
                    context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, it)
                }
                if (newImageUri != null) {
                    imageUri = newImageUri
                    cameraLauncher.launch(newImageUri)
                }
            }
        }
        
        Card(
            onClick = {
                // Check permission and launch camera
                when {
                    ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                        val newImageUri = android.content.ContentValues().apply {
                            put(MediaStore.Images.Media.TITLE, "HOME_Photo_${System.currentTimeMillis()}")
                        }.let {
                            context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, it)
                        }
                        if (newImageUri != null) {
                            imageUri = newImageUri
                            cameraLauncher.launch(newImageUri)
                        }
                    }
                    else -> {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            border = androidx.compose.foundation.BorderStroke(
                2.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "⬆️",
                    fontSize = 48.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Click to upload photos or videos",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "PNG, JPG, MP4 up to 10MB",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (uploadedFiles.isNotEmpty()) {
                        Text(
                            text = "${uploadedFiles.size} photo(s) uploaded",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Spacer(modifier = Modifier)
                    }
                    TextButton(onClick = {
                        imagePickerLauncher.launch("image/*")
                    }) {
                        Text("Choose from Gallery", fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (title.isNotEmpty() && description.isNotEmpty() && category.isNotEmpty()) {
                    onSubmit(title, description, category, priority)
                    submitted = true
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = title.isNotEmpty() && description.isNotEmpty() && category.isNotEmpty()
        ) {
            Text("Submit Ticket", fontSize = 16.sp)
        }
    }
}

@Composable
fun SuccessScreen(
    message: String,
    subtitle: String,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            color = MaterialTheme.colorScheme.tertiaryContainer,
            shape = MaterialTheme.shapes.large,
            modifier = Modifier.size(80.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text("✓", fontSize = 48.sp, color = MaterialTheme.colorScheme.onTertiaryContainer)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onBack) {
            Text("Back to Dashboard")
        }
    }
}

