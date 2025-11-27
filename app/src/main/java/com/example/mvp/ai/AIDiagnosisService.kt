package com.example.mvp.ai

import com.example.mvp.data.Ticket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject
import java.util.Locale

/**
 * Advanced AI Diagnosis Service that provides intelligent analysis of maintenance tickets.
 * Uses sophisticated keyword-based analysis with pattern matching, context understanding,
 * and professional diagnosis generation.
 */
class AIDiagnosisService {
    
    // Optional: Set your OpenAI API key here or via environment/build config
    // For production, use BuildConfig or secure storage
    private val openAIApiKey: String? = null // Set to your API key if using OpenAI
    
    /**
     * Analyzes a ticket and generates an AI diagnosis
     */
    suspend fun generateDiagnosis(
        title: String,
        description: String,
        category: String,
        priority: String? = null
    ): AIDiagnosisResult = withContext(Dispatchers.IO) {
        try {
            // Try OpenAI first if API key is available
            if (openAIApiKey != null && openAIApiKey.isNotEmpty()) {
                try {
                    return@withContext generateOpenAIDiagnosis(title, description, category, priority)
                } catch (e: Exception) {
                    // Fall back to smart analysis if OpenAI fails
                }
            }
            
            // Use advanced smart keyword-based analysis (works offline)
            generateAdvancedDiagnosis(title, description, category, priority)
        } catch (e: Exception) {
            // Fallback to basic diagnosis
            AIDiagnosisResult(
                diagnosis = "Issue categorized as $category. Professional review recommended for detailed assessment and resolution plan.",
                confidence = 0.5f,
                recommendedContractorTypes = listOf(category),
                estimatedUrgency = priority ?: "Medium",
                suggestedActions = listOf("Review ticket details", "Assign appropriate contractor"),
                estimatedCost = null,
                estimatedTime = null,
                rootCauseAnalysis = null,
                partsNeeded = emptyList(),
                safetyWarnings = emptyList(),
                diyRecommendation = null,
                preventiveMaintenance = emptyList(),
                similarIssuesCount = null,
                environmentalImpact = null,
                warrantyConsiderations = null,
                predictiveMaintenance = null
            )
        }
    }
    
    /**
     * Advanced keyword-based analysis with sophisticated pattern matching
     */
    private fun generateAdvancedDiagnosis(
        title: String,
        description: String,
        category: String,
        priority: String?
    ): AIDiagnosisResult {
        val fullText = "$title $description".lowercase(Locale.US)
        val normalizedCategory = category.lowercase(Locale.US)
        
        // Detect actual issue category from text (may differ from selected category)
        val detectedCategory = detectCategoryFromText(fullText, normalizedCategory)
        val finalCategory = if (detectedCategory != normalizedCategory && detectedCategory.isNotEmpty()) {
            detectedCategory.replaceFirstChar { it.uppercaseChar() }
        } else {
            category
        }
        
        // Comprehensive analysis
        val issueDetails = analyzeIssueDetails(fullText, finalCategory.lowercase(Locale.US))
        val severity = assessSeverity(fullText, priority)
        val urgency = determineUrgency(fullText, priority, severity)
        val confidence = calculateConfidence(fullText, finalCategory.lowercase(Locale.US), issueDetails)
        val contractorTypes = determineContractorTypes(finalCategory.lowercase(Locale.US), fullText, issueDetails)
        val rootCause = analyzeRootCause(fullText, finalCategory.lowercase(Locale.US), issueDetails)
        val suggestedActions = generateComprehensiveActions(finalCategory.lowercase(Locale.US), fullText, urgency, issueDetails)
        val costEstimate = estimateCost(finalCategory.lowercase(Locale.US), severity, issueDetails)
        val timeEstimate = estimateTime(finalCategory.lowercase(Locale.US), severity, issueDetails)
        
        // Enhanced features
        val partsNeeded = identifyPartsNeeded(finalCategory.lowercase(Locale.US), issueDetails)
        val safetyWarnings = generateSafetyWarnings(finalCategory.lowercase(Locale.US), issueDetails, urgency)
        val diyRecommendation = assessDIYFeasibility(finalCategory.lowercase(Locale.US), severity, issueDetails)
        val preventiveMaintenance = suggestPreventiveMaintenance(finalCategory.lowercase(Locale.US), issueDetails)
        val similarIssuesCount = estimateSimilarIssuesCount(finalCategory.lowercase(Locale.US))
        val environmentalImpact = assessEnvironmentalImpact(finalCategory.lowercase(Locale.US), issueDetails)
        val warrantyConsiderations = checkWarrantyConsiderations(finalCategory.lowercase(Locale.US), issueDetails)
        val predictiveMaintenance = generatePredictiveMaintenance(finalCategory.lowercase(Locale.US), issueDetails)
        
        // Build professional diagnosis
        val diagnosis = buildProfessionalDiagnosis(
            finalCategory,
            issueDetails,
            severity,
            urgency,
            rootCause,
            costEstimate,
            timeEstimate,
            partsNeeded,
            safetyWarnings,
            diyRecommendation,
            preventiveMaintenance,
            environmentalImpact,
            warrantyConsiderations,
            predictiveMaintenance
        )
        
        return AIDiagnosisResult(
            diagnosis = diagnosis,
            confidence = confidence,
            recommendedContractorTypes = contractorTypes,
            estimatedUrgency = urgency,
            suggestedActions = suggestedActions,
            estimatedCost = costEstimate,
            estimatedTime = timeEstimate,
            rootCauseAnalysis = rootCause,
            partsNeeded = partsNeeded,
            safetyWarnings = safetyWarnings,
            diyRecommendation = diyRecommendation,
            preventiveMaintenance = preventiveMaintenance,
            similarIssuesCount = similarIssuesCount,
            environmentalImpact = environmentalImpact,
            warrantyConsiderations = warrantyConsiderations,
            predictiveMaintenance = predictiveMaintenance
        )
    }
    
    /**
     * Detect category from text content (may override user selection if more accurate)
     */
    private fun detectCategoryFromText(text: String, selectedCategory: String): String {
        val categoryScores = mutableMapOf<String, Int>()
        
        // Plumbing indicators
        val plumbingKeywords = listOf("water", "pipe", "leak", "drain", "faucet", "toilet", "sink", 
            "shower", "plumb", "sewer", "clog", "overflow", "drip", "flush", "hot water", "cold water",
            "water pressure", "backup", "sewage", "bathroom", "kitchen sink")
        categoryScores["plumbing"] = plumbingKeywords.count { text.contains(it, ignoreCase = true) }
        
        // Electrical indicators
        val electricalKeywords = listOf("power", "electric", "outlet", "switch", "circuit", "wire", 
            "light", "breaker", "fuse", "spark", "shock", "flicker", "outage", "voltage", "amp",
            "gfci", "electrical panel", "wiring", "socket", "bulb", "lamp")
        categoryScores["electrical"] = electricalKeywords.count { text.contains(it, ignoreCase = true) }
        
        // HVAC indicators
        val hvacKeywords = listOf("heat", "cool", "air", "hvac", "thermostat", "furnace", "ac", 
            "air conditioning", "heating", "ventilation", "duct", "filter", "temperature", "humidity",
            "air flow", "blower", "compressor", "refrigerant", "radiator")
        categoryScores["hvac"] = hvacKeywords.count { text.contains(it, ignoreCase = true) }
        
        // Appliance indicators
        val applianceKeywords = listOf("appliance", "washer", "dryer", "dishwasher", "refrigerator", 
            "oven", "stove", "microwave", "garbage disposal", "range", "freezer", "ice maker",
            "washing machine", "clothes dryer")
        categoryScores["appliance"] = applianceKeywords.count { text.contains(it, ignoreCase = true) }
        
        // Find category with highest score
        val bestCategory = categoryScores.maxByOrNull { it.value }
        return if (bestCategory != null && bestCategory.value > 2) {
            bestCategory.key
        } else {
            selectedCategory
        }
    }
    
    /**
     * Analyze specific issue details from text
     */
    private fun analyzeIssueDetails(text: String, category: String): IssueDetails {
        val details = IssueDetails()
        
        when (category) {
            "plumbing" -> {
                details.isLeak = text.contains("leak", ignoreCase = true) || 
                                 text.contains("drip", ignoreCase = true) ||
                                 text.contains("water coming", ignoreCase = true)
                details.isClog = text.contains("clog", ignoreCase = true) || 
                                text.contains("backup", ignoreCase = true) ||
                                text.contains("won't drain", ignoreCase = true) ||
                                text.contains("slow drain", ignoreCase = true)
                details.isNoWater = text.contains("no water", ignoreCase = true) ||
                                   text.contains("water not working", ignoreCase = true) ||
                                   text.contains("no pressure", ignoreCase = true)
                details.isOverflow = text.contains("overflow", ignoreCase = true) ||
                                    text.contains("flooding", ignoreCase = true)
                details.location = when {
                    text.contains("bathroom", ignoreCase = true) -> "Bathroom"
                    text.contains("kitchen", ignoreCase = true) -> "Kitchen"
                    text.contains("basement", ignoreCase = true) -> "Basement"
                    text.contains("toilet", ignoreCase = true) -> "Bathroom/Toilet"
                    else -> "Unknown location"
                }
            }
            "electrical" -> {
                details.isNoPower = text.contains("no power", ignoreCase = true) ||
                                  text.contains("outlet not working", ignoreCase = true) ||
                                  text.contains("circuit", ignoreCase = true)
                details.isFlickering = text.contains("flicker", ignoreCase = true) ||
                                      text.contains("blink", ignoreCase = true)
                details.isSpark = text.contains("spark", ignoreCase = true) ||
                                 text.contains("smoke", ignoreCase = true) ||
                                 text.contains("burn", ignoreCase = true)
                details.isShock = text.contains("shock", ignoreCase = true) ||
                                 text.contains("tingle", ignoreCase = true)
            }
            "hvac" -> {
                details.isNoHeat = text.contains("no heat", ignoreCase = true) ||
                                   text.contains("heating not working", ignoreCase = true) ||
                                   text.contains("cold", ignoreCase = true) && text.contains("house", ignoreCase = true)
                details.isNoCool = text.contains("no cool", ignoreCase = true) ||
                                  text.contains("ac not working", ignoreCase = true) ||
                                  text.contains("air conditioning", ignoreCase = true) && 
                                  (text.contains("not", ignoreCase = true) || text.contains("broken", ignoreCase = true))
                details.isNoAir = text.contains("no air", ignoreCase = true) ||
                                 text.contains("air not coming", ignoreCase = true)
                details.isLoud = text.contains("loud", ignoreCase = true) ||
                                text.contains("noise", ignoreCase = true) ||
                                text.contains("rattling", ignoreCase = true)
            }
            "appliance" -> {
                details.isNotWorking = text.contains("not working", ignoreCase = true) ||
                                      text.contains("broken", ignoreCase = true) ||
                                      text.contains("won't start", ignoreCase = true)
                details.isLeaking = text.contains("leak", ignoreCase = true) && 
                                    (text.contains("washer", ignoreCase = true) || 
                                     text.contains("dishwasher", ignoreCase = true))
                details.isMakingNoise = text.contains("noise", ignoreCase = true) ||
                                       text.contains("loud", ignoreCase = true) ||
                                       text.contains("grinding", ignoreCase = true)
            }
        }
        
        // Detect emergency keywords
        details.isEmergency = text.contains("emergency", ignoreCase = true) ||
                             text.contains("urgent", ignoreCase = true) ||
                             text.contains("flood", ignoreCase = true) ||
                             text.contains("fire", ignoreCase = true) ||
                             text.contains("gas leak", ignoreCase = true) ||
                             text.contains("smoke", ignoreCase = true) ||
                             text.contains("dangerous", ignoreCase = true)
        
        return details
    }
    
    /**
     * Assess severity level (1-5 scale)
     */
    private fun assessSeverity(text: String, priority: String?): Int {
        if (priority?.lowercase(Locale.US) == "urgent") return 5
        if (text.contains("emergency", ignoreCase = true) ||
            text.contains("flood", ignoreCase = true) ||
            text.contains("fire", ignoreCase = true) ||
            text.contains("gas leak", ignoreCase = true)) return 5
        
        if (text.contains("no water", ignoreCase = true) ||
            text.contains("no power", ignoreCase = true) ||
            text.contains("no heat", ignoreCase = true) ||
            text.contains("overflow", ignoreCase = true) ||
            text.contains("leaking", ignoreCase = true)) return 4
        
        if (text.contains("not working", ignoreCase = true) ||
            text.contains("broken", ignoreCase = true) ||
            text.contains("damaged", ignoreCase = true)) return 3
        
        if (text.contains("slow", ignoreCase = true) ||
            text.contains("minor", ignoreCase = true) ||
            text.contains("small", ignoreCase = true)) return 2
        
        return 1
    }
    
    /**
     * Determine urgency level
     */
    private fun determineUrgency(text: String, priority: String?, severity: Int): String {
        if (priority?.lowercase(Locale.US) == "urgent" || severity >= 5) return "Urgent"
        if (priority?.lowercase(Locale.US) == "high" || severity >= 4) return "High"
        if (priority?.lowercase(Locale.US) == "low" || severity <= 2) return "Low"
        return "Medium"
    }
    
    /**
     * Calculate confidence score (0.0-1.0)
     */
    private fun calculateConfidence(text: String, category: String, issueDetails: IssueDetails): Float {
        val categoryKeywords = getCategoryKeywords(category)
        val matches = categoryKeywords.count { text.contains(it, ignoreCase = true) }
        val baseConfidence = 0.65f
        val matchBonus = (matches.toFloat() / categoryKeywords.size.coerceAtLeast(1)).coerceAtMost(0.25f)
        
        // Boost confidence if specific issue details detected
        val detailBonus = when {
            issueDetails.isEmergency -> 0.1f
            issueDetails.isLeak || issueDetails.isClog || issueDetails.isNoPower || 
            issueDetails.isNoHeat || issueDetails.isNotWorking -> 0.05f
            else -> 0f
        }
        
        return (baseConfidence + matchBonus + detailBonus).coerceIn(0.5f, 0.95f)
    }
    
    /**
     * Determine contractor types needed
     */
    private fun determineContractorTypes(category: String, text: String, issueDetails: IssueDetails): List<String> {
        val types = mutableListOf<String>()
        
        // Primary contractor
        when (category) {
            "plumbing" -> types.add("Plumbing")
            "electrical" -> types.add("Electrical")
            "hvac" -> types.add("HVAC")
            "appliance" -> types.add("Appliance Repair")
            else -> types.add("General Maintenance")
        }
        
        // Detect if multiple contractor types needed
        if (text.contains("plumb", ignoreCase = true) && category != "plumbing") {
            types.add("Plumbing")
        }
        if (text.contains("electr", ignoreCase = true) && category != "electrical") {
            types.add("Electrical")
        }
        if ((text.contains("hvac", ignoreCase = true) || text.contains("heat", ignoreCase = true) || 
             text.contains("cool", ignoreCase = true)) && category != "hvac") {
            types.add("HVAC")
        }
        if (text.contains("appliance", ignoreCase = true) && category != "appliance") {
            types.add("Appliance Repair")
        }
        
        // If emergency, may need multiple specialists
        if (issueDetails.isEmergency && types.size == 1) {
            if (text.contains("water", ignoreCase = true)) types.add("Plumbing")
            if (text.contains("electr", ignoreCase = true)) types.add("Electrical")
        }
        
        return types.distinct()
    }
    
    /**
     * Analyze root cause
     */
    private fun analyzeRootCause(text: String, category: String, issueDetails: IssueDetails): String? {
        val causes = mutableListOf<String>()
        
        when (category) {
            "plumbing" -> {
                if (issueDetails.isLeak) {
                    causes.add("Possible causes: Worn seals, pipe corrosion, loose connections, or high water pressure")
                }
                if (issueDetails.isClog) {
                    causes.add("Likely causes: Accumulated debris, grease buildup, tree roots, or foreign objects")
                }
                if (issueDetails.isNoWater) {
                    causes.add("Possible causes: Shut-off valve closed, pipe blockage, pressure regulator failure, or main supply issue")
                }
            }
            "electrical" -> {
                if (issueDetails.isNoPower) {
                    causes.add("Possible causes: Tripped circuit breaker, blown fuse, faulty outlet, or wiring issue")
                }
                if (issueDetails.isSpark) {
                    causes.add("Likely causes: Loose connections, damaged wiring, overloaded circuit, or faulty device")
                }
                if (issueDetails.isFlickering) {
                    causes.add("Possible causes: Loose bulb, voltage fluctuations, faulty switch, or wiring problems")
                }
            }
            "hvac" -> {
                if (issueDetails.isNoHeat || issueDetails.isNoCool) {
                    causes.add("Possible causes: Thermostat malfunction, filter blockage, refrigerant leak, compressor failure, or electrical issue")
                }
                if (issueDetails.isLoud) {
                    causes.add("Likely causes: Worn bearings, loose components, debris in system, or motor issues")
                }
            }
            "appliance" -> {
                if (issueDetails.isNotWorking) {
                    causes.add("Possible causes: Power supply issue, control board failure, mechanical component failure, or sensor malfunction")
                }
                if (issueDetails.isLeaking) {
                    causes.add("Likely causes: Worn seals, damaged hoses, clogged drain, or pump failure")
                }
            }
        }
        
        return causes.firstOrNull()
    }
    
    /**
     * Generate comprehensive suggested actions
     */
    private fun generateComprehensiveActions(
        category: String,
        text: String,
        urgency: String,
        issueDetails: IssueDetails
    ): List<String> {
        val actions = mutableListOf<String>()
        
        // Emergency actions first
        if (issueDetails.isEmergency) {
            actions.add("⚠️ If immediate danger exists, evacuate area and call emergency services")
            if (text.contains("gas", ignoreCase = true)) {
                actions.add("Turn off gas supply if safe to do so")
            }
            if (text.contains("water", ignoreCase = true) && issueDetails.isLeak) {
                actions.add("Turn off main water supply immediately")
            }
        }
        
        // Category-specific actions
        when (category) {
            "plumbing" -> {
                if (issueDetails.isLeak) {
                    actions.add("Turn off water supply to affected area if possible")
                    actions.add("Place container/bucket to catch water and prevent damage")
                    actions.add("Document the leak location and severity with photos")
                }
                if (issueDetails.isClog) {
                    actions.add("Avoid using affected fixture to prevent overflow")
                    actions.add("Do not use chemical drain cleaners (may cause damage)")
                }
                if (issueDetails.isOverflow) {
                    actions.add("Turn off water supply immediately")
                    actions.add("Remove standing water if safe")
                }
                actions.add("Assign licensed plumber for professional assessment")
            }
            "electrical" -> {
                if (issueDetails.isSpark || issueDetails.isShock) {
                    actions.add("Turn off power at circuit breaker if safe to do so")
                    actions.add("Do not touch affected outlet/switch")
                    actions.add("Unplug all devices from affected circuit")
                }
                if (issueDetails.isNoPower) {
                    actions.add("Check circuit breaker panel for tripped breakers")
                    actions.add("Test other outlets to isolate issue")
                }
                actions.add("Assign licensed electrician - electrical work requires professional expertise")
            }
            "hvac" -> {
                if (issueDetails.isNoHeat || issueDetails.isNoCool) {
                    actions.add("Check thermostat settings and batteries")
                    actions.add("Check circuit breaker for HVAC system")
                    actions.add("Inspect air filter - replace if dirty")
                }
                if (issueDetails.isLoud) {
                    actions.add("Turn off system to prevent further damage")
                }
                actions.add("Assign certified HVAC technician for diagnosis and repair")
            }
            "appliance" -> {
                if (issueDetails.isNotWorking) {
                    actions.add("Unplug appliance if safe to do so")
                    actions.add("Check warranty status before repair")
                    actions.add("Do not attempt DIY repairs on electrical appliances")
                }
                if (issueDetails.isLeaking) {
                    actions.add("Turn off water supply to appliance if applicable")
                    actions.add("Remove standing water to prevent damage")
                }
                actions.add("Assign appliance repair specialist")
            }
            else -> {
                actions.add("Review issue details thoroughly")
                actions.add("Assign appropriate contractor based on issue type")
            }
        }
        
        // Urgency-based actions
        if (urgency == "Urgent") {
            actions.add("Prioritize for immediate response (within 24 hours)")
        } else if (urgency == "High") {
            actions.add("Schedule assessment within 48 hours")
        }
        
        return actions
    }
    
    /**
     * Estimate repair cost range
     */
    private fun estimateCost(category: String, severity: Int, issueDetails: IssueDetails): String? {
        val baseCosts = when (category) {
            "plumbing" -> when {
                issueDetails.isEmergency -> "$500-$2,000"
                severity >= 4 -> "$200-$800"
                severity >= 3 -> "$150-$500"
                else -> "$100-$300"
            }
            "electrical" -> when {
                issueDetails.isEmergency -> "$400-$1,500"
                severity >= 4 -> "$200-$600"
                severity >= 3 -> "$150-$400"
                else -> "$100-$250"
            }
            "hvac" -> when {
                issueDetails.isEmergency -> "$300-$1,200"
                severity >= 4 -> "$200-$800"
                severity >= 3 -> "$150-$500"
                else -> "$100-$300"
            }
            "appliance" -> when {
                issueDetails.isEmergency -> "$200-$800"
                severity >= 4 -> "$150-$500"
                severity >= 3 -> "$100-$400"
                else -> "$75-$250"
            }
            else -> "$150-$500"
        }
        
        return "Estimated cost: $baseCosts (actual cost may vary based on diagnosis)"
    }
    
    /**
     * Estimate repair time
     */
    private fun estimateTime(category: String, severity: Int, issueDetails: IssueDetails): String? {
        val timeEstimate = when {
            issueDetails.isEmergency -> "1-4 hours"
            severity >= 4 -> "2-6 hours"
            severity >= 3 -> "1-3 hours"
            else -> "30 minutes - 2 hours"
        }
        
        return "Estimated repair time: $timeEstimate (may require follow-up visit for parts)"
    }
    
    /**
     * Identify parts/materials needed for repair
     */
    private fun identifyPartsNeeded(category: String, issueDetails: IssueDetails): List<String> {
        val parts = mutableListOf<String>()
        
        when (category) {
            "plumbing" -> {
                if (issueDetails.isLeak) {
                    parts.add("Pipe sealant or replacement pipe")
                    parts.add("Pipe fittings/connectors")
                    parts.add("Plumber's tape")
                }
                if (issueDetails.isClog) {
                    parts.add("Drain snake/auger")
                    parts.add("Drain cleaner (if appropriate)")
                }
                parts.add("Pipe wrench set")
            }
            "electrical" -> {
                if (issueDetails.isNoPower) {
                    parts.add("Circuit breaker (if needed)")
                    parts.add("Electrical wire (if replacement needed)")
                }
                if (issueDetails.isSpark) {
                    parts.add("Outlet/switch replacement")
                    parts.add("Wire connectors")
                }
                parts.add("Electrical tape")
                parts.add("Wire strippers")
            }
            "hvac" -> {
                if (issueDetails.isNoHeat || issueDetails.isNoCool) {
                    parts.add("HVAC filter")
                    parts.add("Thermostat (if faulty)")
                }
                if (issueDetails.isLoud) {
                    parts.add("Motor bearings (if needed)")
                }
                parts.add("Refrigerant (if leak detected)")
            }
            "appliance" -> {
                parts.add("Replacement parts (model-specific)")
                parts.add("Appliance-specific tools")
            }
        }
        
        return parts.distinct()
    }
    
    /**
     * Generate safety warnings
     */
    private fun generateSafetyWarnings(category: String, issueDetails: IssueDetails, urgency: String): List<String> {
        val warnings = mutableListOf<String>()
        
        when (category) {
            "plumbing" -> {
                if (issueDetails.isLeak) {
                    warnings.add("⚠️ Water damage risk - Turn off water supply immediately if safe")
                    warnings.add("⚠️ Risk of mold growth if water not removed within 24-48 hours")
                }
                if (issueDetails.isOverflow) {
                    warnings.add("⚠️ Electrical hazard - Keep water away from outlets and appliances")
                }
            }
            "electrical" -> {
                warnings.add("⚠️ ELECTRICAL HAZARD - Do not attempt DIY repairs on electrical systems")
                if (issueDetails.isSpark || issueDetails.isShock) {
                    warnings.add("⚠️ FIRE RISK - Turn off power at circuit breaker immediately")
                    warnings.add("⚠️ Do not use affected outlet/switch until repaired")
                }
                warnings.add("⚠️ Only licensed electricians should perform electrical work")
            }
            "hvac" -> {
                if (issueDetails.isLoud) {
                    warnings.add("⚠️ Turn off system to prevent further damage")
                }
                warnings.add("⚠️ Carbon monoxide risk if gas furnace - ensure proper ventilation")
            }
            "appliance" -> {
                warnings.add("⚠️ Unplug appliance before any inspection or repair")
                if (issueDetails.isLeaking) {
                    warnings.add("⚠️ Water damage risk - remove standing water immediately")
                }
            }
        }
        
        if (urgency == "Urgent") {
            warnings.add(0, "🚨 URGENT: This issue requires immediate professional attention")
        }
        
        return warnings
    }
    
    /**
     * Assess if issue is suitable for DIY
     */
    private fun assessDIYFeasibility(category: String, severity: Int, issueDetails: IssueDetails): String? {
        // Never recommend DIY for electrical or high-severity issues
        if (category == "electrical" || severity >= 4 || issueDetails.isEmergency) {
            return "NOT RECOMMENDED: This issue requires professional expertise. DIY attempts may void warranties, create safety hazards, or cause further damage. Professional service strongly recommended."
        }
        
        return when (category) {
            "plumbing" -> {
                if (issueDetails.isClog && severity <= 2) {
                    "POSSIBLE: Simple drain clogs may be addressable with a plunger or drain snake, but professional assessment is recommended to prevent damage."
                } else {
                    "NOT RECOMMENDED: Plumbing issues often require specialized tools and knowledge. Professional service recommended to prevent water damage."
                }
            }
            "hvac" -> {
                "NOT RECOMMENDED: HVAC systems require specialized knowledge and tools. Professional service required for proper diagnosis and repair."
            }
            "appliance" -> {
                if (severity <= 2) {
                    "CONDITIONAL: Minor appliance issues may be DIY-friendly if under warranty or with proper tools. Check warranty status first - DIY may void coverage."
                } else {
                    "NOT RECOMMENDED: Appliance repairs often require specialized parts and knowledge. Professional service recommended."
                }
            }
            else -> null
        }
    }
    
    /**
     * Suggest preventive maintenance
     */
    private fun suggestPreventiveMaintenance(category: String, issueDetails: IssueDetails): List<String> {
        val suggestions = mutableListOf<String>()
        
        when (category) {
            "plumbing" -> {
                suggestions.add("Schedule annual plumbing inspection")
                suggestions.add("Regularly clean drains to prevent buildup")
                suggestions.add("Check for leaks during seasonal inspections")
                suggestions.add("Maintain water pressure within recommended range")
                if (issueDetails.isClog) {
                    suggestions.add("Avoid pouring grease or food waste down drains")
                }
            }
            "electrical" -> {
                suggestions.add("Annual electrical safety inspection recommended")
                suggestions.add("Test GFCI outlets monthly")
                suggestions.add("Avoid overloading circuits")
                suggestions.add("Replace old wiring if property is 30+ years old")
            }
            "hvac" -> {
                suggestions.add("Replace HVAC filters every 1-3 months")
                suggestions.add("Schedule bi-annual HVAC maintenance (spring/fall)")
                suggestions.add("Clean ducts every 3-5 years")
                suggestions.add("Check thermostat batteries annually")
            }
            "appliance" -> {
                suggestions.add("Follow manufacturer's maintenance schedule")
                suggestions.add("Clean appliance filters regularly")
                suggestions.add("Check appliance warranties and service contracts")
            }
        }
        
        return suggestions
    }
    
    /**
     * Estimate similar issues count (simulated)
     */
    private fun estimateSimilarIssuesCount(category: String): Int {
        // Simulated based on common issue frequencies
        return when (category.lowercase(Locale.US)) {
            "plumbing" -> (3..8).random()
            "electrical" -> (2..6).random()
            "hvac" -> (4..10).random()
            "appliance" -> (5..12).random()
            else -> (2..5).random()
        }
    }
    
    /**
     * Assess environmental impact
     */
    private fun assessEnvironmentalImpact(category: String, issueDetails: IssueDetails): String? {
        return when (category) {
            "plumbing" -> {
                if (issueDetails.isLeak) {
                    "ENVIRONMENTAL IMPACT: Water leaks waste an average of 10,000 gallons per year per household. Prompt repair conserves water resources and reduces utility costs."
                } else null
            }
            "hvac" -> {
                "ENVIRONMENTAL IMPACT: Efficient HVAC operation reduces energy consumption. Proper maintenance can improve efficiency by 15-20%, reducing carbon footprint."
            }
            "electrical" -> {
                "ENVIRONMENTAL IMPACT: Electrical issues can lead to energy waste. Proper repairs improve efficiency and reduce unnecessary power consumption."
            }
            else -> null
        }
    }
    
    /**
     * Check warranty considerations
     */
    private fun checkWarrantyConsiderations(category: String, issueDetails: IssueDetails): String? {
        return when (category) {
            "appliance" -> {
                "WARRANTY CHECK: Verify appliance warranty status before repair. DIY repairs or unauthorized service may void manufacturer warranty. Check warranty documentation or contact manufacturer."
            }
            "hvac" -> {
                "WARRANTY CHECK: HVAC systems often have manufacturer warranties (typically 5-10 years). Verify warranty status before proceeding with repairs. Professional service may be required to maintain warranty coverage."
            }
            else -> null
        }
    }
    
    /**
     * Generate predictive maintenance suggestions
     */
    private fun generatePredictiveMaintenance(category: String, issueDetails: IssueDetails): String? {
        return when (category) {
            "plumbing" -> {
                if (issueDetails.isClog) {
                    "PREDICTIVE MAINTENANCE: Recurring clogs may indicate deeper issues (tree roots, pipe damage). Consider professional camera inspection to identify underlying problems before they escalate."
                } else if (issueDetails.isLeak) {
                    "PREDICTIVE MAINTENANCE: Multiple leaks may indicate aging plumbing system. Consider comprehensive plumbing assessment and potential system upgrade to prevent future failures."
                } else null
            }
            "hvac" -> {
                "PREDICTIVE MAINTENANCE: Regular HVAC maintenance can prevent 80% of system failures. Schedule preventive maintenance to extend system life and reduce emergency repair costs."
            }
            "electrical" -> {
                if (issueDetails.isNoPower) {
                    "PREDICTIVE MAINTENANCE: Frequent circuit trips may indicate overloaded circuits or outdated electrical panel. Consider electrical panel upgrade assessment to prevent future issues."
                } else null
            }
            else -> null
        }
    }
    
    /**
     * Build professional diagnosis text
     */
    private fun buildProfessionalDiagnosis(
        category: String,
        issueDetails: IssueDetails,
        severity: Int,
        urgency: String,
        rootCause: String?,
        costEstimate: String?,
        timeEstimate: String?,
        partsNeeded: List<String>,
        safetyWarnings: List<String>,
        diyRecommendation: String?,
        preventiveMaintenance: List<String>,
        environmentalImpact: String?,
        warrantyConsiderations: String?,
        predictiveMaintenance: String?
    ): String {
        val diagnosis = StringBuilder()
        
        // Professional header
        diagnosis.append("PROFESSIONAL DIAGNOSIS REPORT\n")
        diagnosis.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n")
        
        // Issue identification section
        diagnosis.append("ISSUE IDENTIFICATION:\n")
        when {
            issueDetails.isEmergency -> {
                diagnosis.append("⚠️ EMERGENCY SITUATION DETECTED\n")
                diagnosis.append("This issue requires immediate professional intervention to prevent property damage or safety hazards.\n\n")
            }
            issueDetails.isLeak -> {
                diagnosis.append("Water Leak Detected\n")
                if (issueDetails.location != "Unknown location") {
                    diagnosis.append("Location: ${issueDetails.location}\n")
                }
                diagnosis.append("Water leakage can cause significant property damage if not addressed promptly.\n\n")
            }
            issueDetails.isClog -> {
                diagnosis.append("Drainage System Blockage Identified\n")
                diagnosis.append("Complete or partial obstruction preventing normal water flow through drainage system.\n\n")
            }
            issueDetails.isNoWater -> {
                diagnosis.append("Water Supply Interruption\n")
                diagnosis.append("Complete loss of water service affecting property functionality.\n\n")
            }
            issueDetails.isNoPower -> {
                diagnosis.append("Electrical Power Failure\n")
                diagnosis.append("Loss of electrical service requiring immediate professional assessment for safety.\n\n")
            }
            issueDetails.isNoHeat || issueDetails.isNoCool -> {
                diagnosis.append("HVAC System Malfunction\n")
                diagnosis.append("Heating or cooling system failure affecting climate control and comfort.\n\n")
            }
            issueDetails.isNotWorking -> {
                diagnosis.append("Equipment/Appliance Failure\n")
                diagnosis.append("Complete malfunction preventing normal operation of equipment.\n\n")
            }
            else -> {
                diagnosis.append("$category Maintenance Issue\n")
                diagnosis.append("General maintenance concern requiring professional evaluation.\n\n")
            }
        }
        
        // Severity assessment
        diagnosis.append("SEVERITY ASSESSMENT:\n")
        val severityText = when (severity) {
            5 -> "CRITICAL - Immediate intervention required to prevent safety hazards or extensive damage"
            4 -> "HIGH - Significant impact on property functionality, requires prompt attention"
            3 -> "MODERATE - Noticeable impact on operations, should be addressed within standard timeframe"
            2 -> "LOW - Minor inconvenience, can be scheduled for routine maintenance"
            else -> "MINOR - Cosmetic or non-critical issue, can be addressed during regular service"
        }
        diagnosis.append("$severityText\n\n")
        
        // Root cause analysis
        rootCause?.let {
            diagnosis.append("ROOT CAUSE ANALYSIS:\n")
            diagnosis.append("$it\n\n")
        }
        
        // Professional recommendations
        diagnosis.append("RECOMMENDED ACTION:\n")
        val contractorName = when (category.lowercase(Locale.US)) {
            "plumbing" -> "licensed plumber"
            "electrical" -> "certified electrician"
            "hvac" -> "certified HVAC technician"
            "appliance" -> "appliance repair specialist"
            else -> "qualified maintenance professional"
        }
        diagnosis.append("Engage a $contractorName for comprehensive assessment and repair. ")
        if (urgency == "Urgent") {
            diagnosis.append("Due to the urgent nature of this issue, immediate professional evaluation is strongly recommended.\n\n")
        } else {
            diagnosis.append("Schedule assessment at earliest convenience.\n\n")
        }
        
        // Cost and time estimates
        if (costEstimate != null || timeEstimate != null) {
            diagnosis.append("ESTIMATED RESOLUTION:\n")
            costEstimate?.let {
                diagnosis.append("$it\n")
            }
            timeEstimate?.let {
                diagnosis.append("$it\n")
            }
            diagnosis.append("\n")
        }
        
        // Safety warnings
        if (safetyWarnings.isNotEmpty()) {
            diagnosis.append("SAFETY WARNINGS:\n")
            safetyWarnings.forEach { warning ->
                diagnosis.append("$warning\n")
            }
            diagnosis.append("\n")
        }
        
        // Parts needed
        if (partsNeeded.isNotEmpty()) {
            diagnosis.append("PARTS/MATERIALS NEEDED:\n")
            partsNeeded.forEach { part ->
                diagnosis.append("• $part\n")
            }
            diagnosis.append("\n")
        }
        
        // DIY recommendation
        diyRecommendation?.let {
            diagnosis.append("DIY ASSESSMENT:\n")
            diagnosis.append("$it\n\n")
        }
        
        // Preventive maintenance
        if (preventiveMaintenance.isNotEmpty()) {
            diagnosis.append("PREVENTIVE MAINTENANCE RECOMMENDATIONS:\n")
            preventiveMaintenance.forEach { suggestion ->
                diagnosis.append("• $suggestion\n")
            }
            diagnosis.append("\n")
        }
        
        // Predictive maintenance
        predictiveMaintenance?.let {
            diagnosis.append("$it\n\n")
        }
        
        // Environmental impact
        environmentalImpact?.let {
            diagnosis.append("$it\n\n")
        }
        
        // Warranty considerations
        warrantyConsiderations?.let {
            diagnosis.append("$it\n\n")
        }
        
        // Urgency note
        if (urgency == "Urgent") {
            diagnosis.append("⚠️ PRIORITY ALERT: This issue has been classified as URGENT and requires immediate attention to prevent escalation or property damage.\n")
        }
        
        return diagnosis.toString()
    }
    
    /**
     * OpenAI API integration (optional, requires API key)
     */
    private suspend fun generateOpenAIDiagnosis(
        title: String,
        description: String,
        category: String,
        priority: String?
    ): AIDiagnosisResult {
        val prompt = """
            You are a professional maintenance diagnosis expert. Analyze this maintenance request:
            
            Title: $title
            Category: $category
            Description: $description
            Priority: ${priority ?: "Not specified"}
            
            Provide a comprehensive diagnosis in JSON format with:
            {
                "diagnosis": "Detailed professional diagnosis",
                "contractorType": "Recommended contractor type",
                "urgency": "Urgent/High/Medium/Low",
                "actions": ["Action 1", "Action 2", ...],
                "rootCause": "Likely root cause analysis",
                "estimatedCost": "Cost range estimate",
                "estimatedTime": "Time estimate"
            }
        """.trimIndent()
        
        val connection = URL("https://api.openai.com/v1/chat/completions").openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("Authorization", "Bearer $openAIApiKey")
        connection.doOutput = true
        connection.connectTimeout = 10000
        connection.readTimeout = 10000
        
        val requestBody = JSONObject().apply {
            put("model", "gpt-3.5-turbo")
            put("messages", listOf(
                mapOf("role" to "user", "content" to prompt)
            ))
            put("temperature", 0.7)
            put("max_tokens", 500)
        }
        
        connection.outputStream.use { it.write(requestBody.toString().toByteArray()) }
        
        val responseCode = connection.responseCode
        if (responseCode == HttpURLConnection.HTTP_OK) {
            val response = connection.inputStream.bufferedReader().use { it.readText() }
            val jsonResponse = JSONObject(response)
            val choices = jsonResponse.getJSONArray("choices")
            val message = choices.getJSONObject(0).getJSONObject("message")
            val content = message.getString("content")
            
            return parseOpenAIResponse(content, category, priority)
        } else {
            throw Exception("OpenAI API error: $responseCode")
        }
    }
    
    private fun parseOpenAIResponse(content: String, category: String, priority: String?): AIDiagnosisResult {
        return try {
            val json = JSONObject(content)
            val baseResult = generateAdvancedDiagnosis("", content, category, priority)
            AIDiagnosisResult(
                diagnosis = json.optString("diagnosis", content),
                confidence = 0.9f,
                recommendedContractorTypes = listOf(json.optString("contractorType", category)),
                estimatedUrgency = json.optString("urgency", priority ?: "Medium"),
                suggestedActions = json.optJSONArray("actions")?.let { actionsArray ->
                    (0 until actionsArray.length()).map { index -> 
                        actionsArray.getString(index) 
                    }
                } ?: baseResult.suggestedActions,
                estimatedCost = json.optString("estimatedCost", null).takeIf { it.isNotEmpty() } ?: baseResult.estimatedCost,
                estimatedTime = json.optString("estimatedTime", null).takeIf { it.isNotEmpty() } ?: baseResult.estimatedTime,
                rootCauseAnalysis = json.optString("rootCause", null).takeIf { it.isNotEmpty() } ?: baseResult.rootCauseAnalysis,
                partsNeeded = baseResult.partsNeeded,
                safetyWarnings = baseResult.safetyWarnings,
                diyRecommendation = baseResult.diyRecommendation,
                preventiveMaintenance = baseResult.preventiveMaintenance,
                similarIssuesCount = baseResult.similarIssuesCount,
                environmentalImpact = baseResult.environmentalImpact,
                warrantyConsiderations = baseResult.warrantyConsiderations,
                predictiveMaintenance = baseResult.predictiveMaintenance
            )
        } catch (e: Exception) {
            // Fallback to advanced analysis
            generateAdvancedDiagnosis("", content, category, priority)
        }
    }
    
    private fun getCategoryKeywords(category: String): List<String> {
        return when (category.lowercase(Locale.US)) {
            "plumbing" -> listOf("water", "pipe", "leak", "drain", "faucet", "toilet", "sink", 
                "shower", "plumb", "sewer", "clog", "overflow", "drip", "flush", "hot water", 
                "cold water", "water pressure", "backup", "sewage", "bathroom", "kitchen sink")
            "electrical" -> listOf("power", "electric", "outlet", "switch", "circuit", "wire", 
                "light", "breaker", "fuse", "spark", "shock", "flicker", "outage", "voltage", 
                "amp", "gfci", "electrical panel", "wiring", "socket", "bulb", "lamp")
            "hvac" -> listOf("heat", "cool", "air", "hvac", "thermostat", "furnace", "ac", 
                "air conditioning", "heating", "ventilation", "duct", "filter", "temperature", 
                "humidity", "air flow", "blower", "compressor", "refrigerant", "radiator")
            "appliance" -> listOf("appliance", "washer", "dryer", "dishwasher", "refrigerator", 
                "oven", "stove", "microwave", "garbage disposal", "range", "freezer", 
                "ice maker", "washing machine", "clothes dryer")
            "general maintenance" -> listOf("maintenance", "repair", "fix", "service", "issue", 
                "problem", "broken", "damaged", "not working")
            else -> listOf("maintenance", "repair", "issue", "problem")
        }
    }
    
    /**
     * Internal data class for issue details
     */
    private data class IssueDetails(
        var isLeak: Boolean = false,
        var isClog: Boolean = false,
        var isNoWater: Boolean = false,
        var isOverflow: Boolean = false,
        var isNoPower: Boolean = false,
        var isFlickering: Boolean = false,
        var isSpark: Boolean = false,
        var isShock: Boolean = false,
        var isNoHeat: Boolean = false,
        var isNoCool: Boolean = false,
        var isNoAir: Boolean = false,
        var isLoud: Boolean = false,
        var isNotWorking: Boolean = false,
        var isLeaking: Boolean = false,
        var isMakingNoise: Boolean = false,
        var isEmergency: Boolean = false,
        var location: String = "Unknown location"
    )
}

/**
 * Result of AI diagnosis analysis with enhanced features
 */
data class AIDiagnosisResult(
    val diagnosis: String,
    val confidence: Float,
    val recommendedContractorTypes: List<String>,
    val estimatedUrgency: String,
    val suggestedActions: List<String>,
    val estimatedCost: String? = null,
    val estimatedTime: String? = null,
    val rootCauseAnalysis: String? = null,
    val partsNeeded: List<String> = emptyList(),
    val safetyWarnings: List<String> = emptyList(),
    val diyRecommendation: String? = null,
    val preventiveMaintenance: List<String> = emptyList(),
    val similarIssuesCount: Int? = null,
    val environmentalImpact: String? = null,
    val warrantyConsiderations: String? = null,
    val predictiveMaintenance: String? = null
)
