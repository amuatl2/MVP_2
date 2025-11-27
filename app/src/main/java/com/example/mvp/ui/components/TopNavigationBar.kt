package com.example.mvp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mvp.data.UserRole

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopNavigationBar(
    currentRoute: String?,
    userRole: UserRole?,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit
) {
    if (userRole == null) return

    data class NavItem(val label: String, val route: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)
    
    val navItems = when (userRole) {
        UserRole.TENANT -> listOf(
            NavItem("Dashboard", "dashboard", Icons.Default.Home),
            NavItem("Ticket", "create_ticket", Icons.Default.Add),
            NavItem("Chat", "chat", Icons.Default.Email),
            NavItem("Notifications", "notifications", Icons.Default.Notifications),
            NavItem("Search", "search", Icons.Default.Search),
            NavItem("Portal", "tenant_portal", Icons.Default.Info),
            NavItem("History", "history", Icons.Default.Info),
            NavItem("Settings", "settings", Icons.Default.Settings)
        )
        UserRole.LANDLORD -> listOf(
            NavItem("Dashboard", "dashboard", Icons.Default.Home),
            NavItem("AI Diagnosis", "ai_diagnosis", Icons.Default.Info),
            NavItem("Marketplace", "marketplace", Icons.Default.Person),
            NavItem("Properties", "properties", Icons.Default.Home),
            NavItem("Maintenance", "maintenance_reminders", Icons.Default.Build),
            NavItem("Budget", "budget", Icons.Default.Settings),
            NavItem("Analytics", "analytics", Icons.Default.Settings),
            NavItem("Advanced", "advanced_analytics", Icons.Default.Info),
            NavItem("Documents", "documents", Icons.Default.Info),
            NavItem("Search", "search", Icons.Default.Search),
            NavItem("Notifications", "notifications", Icons.Default.Notifications),
            NavItem("History", "history", Icons.Default.Info),
            NavItem("Settings", "settings", Icons.Default.Settings)
        )
        UserRole.CONTRACTOR -> listOf(
            NavItem("Jobs", "contractor_dashboard", Icons.Default.Build),
            NavItem("Schedule", "schedule", Icons.Default.DateRange),
            NavItem("Enhanced", "enhanced_schedule", Icons.Default.DateRange),
            NavItem("Search", "search", Icons.Default.Search),
            NavItem("Notifications", "notifications", Icons.Default.Notifications),
            NavItem("Rating", "rating", Icons.Default.Star),
            NavItem("History", "history", Icons.Default.Info),
            NavItem("Chat", "chat", Icons.Default.Email),
            NavItem("Settings", "settings", Icons.Default.Settings)
        )
    }

    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "HOME",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "HOME",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        actions = {
            HorizontalScrollableRow(
                modifier = Modifier.weight(1f, fill = false)
            ) {
                navItems.forEach { navItem ->
                    TopNavButton(
                        label = navItem.label,
                        icon = navItem.icon,
                        currentRoute = currentRoute,
                        route = navItem.route,
                        onNavigate = onNavigate
                    )
                }
            }
            Spacer(modifier = Modifier.width(4.dp))
            // Logout Button - Always visible, icon only on mobile for space
            IconButton(
                onClick = onLogout,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    Icons.Default.ExitToApp,
                    contentDescription = "Logout",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@Composable
fun TopNavButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    currentRoute: String?,
    route: String,
    onNavigate: (String) -> Unit
) {
    val isSelected = currentRoute?.contains(route) == true
    
    // Use smaller text on mobile to prevent wrapping
    Surface(
        onClick = { onNavigate(route) },
        shape = MaterialTheme.shapes.small,
        color = if (isSelected)
            MaterialTheme.colorScheme.primaryContainer
        else
            MaterialTheme.colorScheme.surface,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(16.dp),
                tint = if (isSelected)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = if (isSelected)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
