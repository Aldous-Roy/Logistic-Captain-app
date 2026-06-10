package com.example.logistic_captain.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.logistic_captain.ui.components.StopCard
import com.example.logistic_captain.ui.theme.LogisticsBlue
import com.example.logistic_captain.ui.theme.LogisticsOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onLogout: () -> Unit,
    onStartShift: () -> Unit,
    onDeliverClick: (String) -> Unit
) {
    LaunchedEffect(Unit) {
        viewModel.loadRoute()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "LOGISTIC CAPTAIN",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = LogisticsBlue,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = { /* TODO: Add Notifications */ }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF8F9FA))
        ) {
            // Header Stats Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                    .background(LogisticsBlue)
                    .padding(bottom = 24.dp, start = 16.dp, end = 16.dp)
            ) {
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

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                if (viewModel.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = LogisticsOrange)
                    }
                } else if (viewModel.route == null || viewModel.route?.stops?.isEmpty() == true) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("No stops assigned for today.", color = Color.Gray, fontWeight = FontWeight.Medium)
                            Text("Check back later or contact dispatch.", color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = "TODAY'S ROUTE",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = LogisticsBlue.copy(alpha = 0.6f)
                            )
                            Text(
                                text = viewModel.route?.routeNumber ?: "N/A",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = LogisticsBlue
                            )
                        }
                        
                        Surface(
                            color = LogisticsOrange.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "${viewModel.route?.stops?.size ?: 0} STOPS",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                color = LogisticsOrange,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp)

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(viewModel.route!!.stops) { stop ->
                            StopCard(
                                stop = stop,
                                onNavigateClick = { /* Launch Google Maps Intent */ },
                                onStatusUpdate = { status -> viewModel.updateStopStatus(stop.id, status) },
                                onDeliverClick = { onDeliverClick(stop.id) }
                            )
                        }
                    }
                }
            }
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
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                    color = LogisticsBlue,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            if (isShiftStarted) {
                Button(
                    onClick = onToggleBreak,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isOnBreak) Color(0xFF4CAF50) else LogisticsOrange
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    Icon(
                        if (isOnBreak) Icons.Default.PlayArrow else Icons.Default.Pause, 
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isOnBreak) "RESUME" else "BREAK", fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = onStartShift,
                    colors = ButtonDefaults.buttonColors(containerColor = LogisticsOrange),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("START SHIFT", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
