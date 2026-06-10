package com.example.logistic_captain.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.logistic_captain.ui.components.StopCard
import com.example.logistic_captain.ui.theme.PremiumGreen
import com.example.logistic_captain.ui.theme.PremiumGreenLight
import com.example.logistic_captain.ui.theme.CreamBackground
import com.example.logistic_captain.ui.theme.TextDark
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onLogout: () -> Unit,
    onStartShift: () -> Unit,
    onDeliverClick: (String) -> Unit
) {
    var activeTab by remember { mutableStateOf("Home") }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") } // All, Pending, Completed

    LaunchedEffect(Unit) {
        viewModel.loadRoute()
    }

    val stops = viewModel.route?.stops ?: emptyList()
    val totalStops = stops.size
    val completedStops = stops.count { it.status == "COMPLETED" }
    val pendingStops = stops.count { it.status == "PENDING" || it.status == "IN_PROGRESS" }
    val failedStops = stops.count { it.status.startsWith("ATTEMPTED") }
    
    val progressFraction = if (totalStops > 0) completedStops.toFloat() / totalStops else 0f
    val progressPercentage = (progressFraction * 100).toInt()

    val formattedDate = remember {
        val sdf = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
        sdf.format(Date())
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                val tabs = listOf(
                    Triple("Home", Icons.Default.Home, "Home"),
                    Triple("Route", Icons.Default.Map, "Route"),
                    Triple("Alerts", Icons.Default.Notifications, "Alerts"),
                    Triple("Profile", Icons.Default.Person, "Profile")
                )
                
                tabs.forEach { (tabName, icon, label) ->
                    val isSelected = activeTab == tabName
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { activeTab = tabName },
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PremiumGreen,
                            selectedTextColor = PremiumGreen,
                            unselectedIconColor = PremiumGreenLight.copy(alpha = 0.6f),
                            unselectedTextColor = PremiumGreenLight.copy(alpha = 0.6f),
                            indicatorColor = PremiumGreen.copy(alpha = 0.1f)
                        )
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(CreamBackground)
        ) {
            when (activeTab) {
                "Home" -> {
                    // Header Block
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                            .background(PremiumGreen)
                            .padding(top = 24.dp, bottom = 28.dp, start = 24.dp, end = 24.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // User Avatar Circle
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(CircleShape)
                                            .background(PremiumGreenLight.copy(alpha = 0.3f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "JS",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 20.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text(
                                            text = "Welcome back,",
                                            color = Color.White.copy(alpha = 0.8f),
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = "John Smith",
                                            color = Color.White,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                // Notification Bell Icon with Badge
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .clickable { /* Notifications */ },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Notifications,
                                        contentDescription = "Notifications",
                                        tint = Color.White,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    // Red Badge "3"
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(top = 4.dp, end = 4.dp)
                                            .size(18.dp)
                                            .clip(CircleShape)
                                            .background(Color.Red),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "3",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                text = formattedDate,
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Main Scrollable Area
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp),
                        contentPadding = PaddingValues(top = 24.dp, bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Shift controls (Start/End Shift status)
                        item {
                            ShiftStatusCard(
                                isShiftStarted = viewModel.isShiftStarted,
                                isOnBreak = viewModel.isOnBreak,
                                onStartShift = {
                                    viewModel.startShift(0.0, 0.0)
                                    onStartShift()
                                },
                                onToggleBreak = { viewModel.toggleBreak() }
                            )
                        }

                        if (viewModel.isShiftStarted) {
                            // Progress Card
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(20.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White)
                                ) {
                                    Column(modifier = Modifier.padding(20.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Today's Progress",
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = PremiumGreen
                                            )
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = null,
                                                    tint = PremiumGreen,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "$progressPercentage%",
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = PremiumGreen
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(16.dp))

                                        LinearProgressIndicator(
                                            progress = { progressFraction },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(10.dp)
                                                .clip(RoundedCornerShape(5.dp)),
                                            color = PremiumGreen,
                                            trackColor = CreamBackground
                                        )

                                        Spacer(modifier = Modifier.height(12.dp))

                                        Text(
                                            text = "$completedStops of $totalStops deliveries completed",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = PremiumGreenLight
                                        )
                                    }
                                }
                            }

                            // Stats Grid
                            item {
                                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                        StatsCard(
                                            title = "Today's Deliveries",
                                            value = totalStops.toString(),
                                            icon = Icons.Default.Inventory,
                                            iconBgColor = Color(0xFFFFECE0),
                                            iconTint = Color(0xFFE65100),
                                            modifier = Modifier.weight(1f)
                                        )
                                        StatsCard(
                                            title = "Completed Stops",
                                            value = completedStops.toString(),
                                            icon = Icons.Default.CheckCircle,
                                            iconBgColor = Color(0xFFE2F4E3),
                                            iconTint = Color(0xFF2E7D32),
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                        StatsCard(
                                            title = "Pending Stops",
                                            value = pendingStops.toString(),
                                            icon = Icons.Default.AccessTime,
                                            iconBgColor = Color(0xFFFFF3E0),
                                            iconTint = Color(0xFFEF6C00),
                                            modifier = Modifier.weight(1f)
                                        )
                                        StatsCard(
                                            title = "Failed Stops",
                                            value = failedStops.toString(),
                                            icon = Icons.Default.Cancel,
                                            iconBgColor = Color(0xFFFFEBEE),
                                            iconTint = Color(0xFFC62828),
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }

                            // View Route List Button
                            item {
                                Button(
                                    onClick = { activeTab = "Route" },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = PremiumGreen),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Map,
                                        contentDescription = null,
                                        tint = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "View Route List",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }

                "Route" -> {
                    // Header Area for Route List
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                            .background(PremiumGreen)
                            .padding(top = 24.dp, bottom = 28.dp, start = 24.dp, end = 24.dp)
                    ) {
                        Column {
                            Text(
                                text = "Route List",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$totalStops stops scheduled for today",
                                fontSize = 15.sp,
                                color = Color.White.copy(alpha = 0.8f),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Route List content
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp)
                    ) {
                        Spacer(modifier = Modifier.height(20.dp))

                        // Search Bar
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search by name or address...", color = PremiumGreenLight.copy(alpha = 0.6f)) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = PremiumGreen) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextDark,
                                unfocusedTextColor = TextDark,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Filter Chips
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val filters = listOf("All", "Pending", "Completed")
                            filters.forEach { filter ->
                                val isSelected = selectedFilter == filter
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) PremiumGreen else Color(0xFFE6E5C0))
                                        .clickable { selectedFilter = filter }
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = filter,
                                        color = if (isSelected) Color.White else PremiumGreen,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (viewModel.isLoading) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = PremiumGreen)
                            }
                        } else {
                            val filteredStops = stops.filter { stop ->
                                val matchesSearch = stop.customerName.contains(searchQuery, ignoreCase = true) ||
                                                    stop.address.contains(searchQuery, ignoreCase = true)
                                val matchesFilter = when (selectedFilter) {
                                    "Pending" -> stop.status == "PENDING" || stop.status == "IN_PROGRESS"
                                    "Completed" -> stop.status == "COMPLETED"
                                    else -> true
                                }
                                matchesSearch && matchesFilter
                            }

                            if (filteredStops.isEmpty()) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("No stops match criteria.", color = PremiumGreenLight)
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(bottom = 24.dp)
                                ) {
                                    items(filteredStops) { stop ->
                                        StopCard(
                                            stop = stop,
                                            onNavigateClick = { /* Navigate */ },
                                            onStatusUpdate = { status -> viewModel.updateStopStatus(stop.id, status) },
                                            onDeliverClick = { onDeliverClick(stop.id) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                "Alerts" -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(64.dp), tint = PremiumGreen)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No alerts at the moment.", color = PremiumGreenLight, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                "Profile" -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(PremiumGreen),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("JS", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("John Smith", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextDark)
                            Text("Driver ID: EMP-19348", fontSize = 14.sp, color = PremiumGreenLight)
                            
                            Spacer(modifier = Modifier.height(40.dp))
                            
                            Button(
                                onClick = onLogout,
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text("Logout", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatsCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBgColor: Color,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = value, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = TextDark)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = title, fontSize = 13.sp, color = PremiumGreenLight, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun ShiftStatusCard(
    isShiftStarted: Boolean,
    isOnBreak: Boolean,
    onStartShift: () -> Unit,
    onToggleBreak: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isShiftStarted) Color(0xFF4CAF50) else Color.Gray)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isShiftStarted) "ON DUTY" else "OFF DUTY",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (isShiftStarted) Color(0xFF4CAF50) else Color.Gray,
                        letterSpacing = 1.sp
                    )
                }
                
                Text(
                    text = if (isShiftStarted) "Shift in Progress" else "Ready to start?",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = PremiumGreen,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            if (isShiftStarted) {
                Button(
                    onClick = onToggleBreak,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isOnBreak) Color(0xFF4CAF50) else PremiumGreenLight
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    Text(if (isOnBreak) "RESUME" else "BREAK", fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = onStartShift,
                    colors = ButtonDefaults.buttonColors(containerColor = PremiumGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("START SHIFT", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

