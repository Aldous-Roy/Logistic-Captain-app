package com.example.logistic_captain.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.logistic_captain.model.StopResponse
import com.example.logistic_captain.ui.theme.PremiumGreen
import com.example.logistic_captain.ui.theme.PremiumGreenLight
import com.example.logistic_captain.ui.theme.TextDark

@Composable
fun StopCard(
    stop: StopResponse,
    onNavigateClick: () -> Unit,
    onStatusUpdate: (String) -> Unit,
    onDeliverClick: () -> Unit
) {
    val context = LocalContext.current
    var isExpanded by remember { mutableStateOf(false) }
    var showExceptionDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stop.customerName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        StopStatusChip(status = stop.status)
                    }
                }
                
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Expand Actions",
                    tint = PremiumGreenLight.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Address Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = PremiumGreen,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stop.address,
                    fontSize = 14.sp,
                    color = PremiumGreenLight,
                    fontWeight = FontWeight.Medium
                )
            }

            // ETA and Package details Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                stop.eta?.let { etaTime ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = PremiumGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ETA: $etaTime",
                            fontSize = 14.sp,
                            color = PremiumGreenLight,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Inventory,
                        contentDescription = null,
                        tint = PremiumGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${stop.packageCount} package${if (stop.packageCount > 1) "s" else ""}",
                        fontSize = 14.sp,
                        color = PremiumGreenLight,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Expanded Actions
            if (isExpanded || stop.status == "PENDING") {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // MAPS / NAVIGATE button
                    OutlinedButton(
                        onClick = {
                            val gmmIntentUri = Uri.parse("google.navigation:q=${stop.address}")
                            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                                setPackage("com.google.android.apps.maps")
                            }
                            try {
                                context.startActivity(mapIntent)
                            } catch (e: Exception) {
                                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(stop.address)}"))
                                context.startActivity(browserIntent)
                            }
                            onNavigateClick()
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PremiumGreen)
                    ) {
                        Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("MAPS", fontWeight = FontWeight.Bold)
                    }

                    if (stop.status == "PENDING" || stop.status == "IN_PROGRESS") {
                        // EXCEPTION button
                        OutlinedButton(
                            onClick = { showExceptionDialog = true },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFC62828)),
                            border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFC62828)))
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("FAIL", fontWeight = FontWeight.Bold)
                        }

                        // DELIVER button
                        Button(
                            onClick = onDeliverClick,
                            modifier = Modifier.weight(1f).height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PremiumGreen),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("DELIVER", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showExceptionDialog) {
        AlertDialog(
            onDismissRequest = { showExceptionDialog = false },
            title = { Text("Mark Stop as Failed") },
            text = {
                Column {
                    Text("Select a reason for the delivery failure:")
                    Spacer(modifier = Modifier.height(16.dp))
                    val reasons = listOf("ATTEMPTED_NO_ACCESS", "ATTEMPTED_ABSENT")
                    reasons.forEach { reason ->
                        Button(
                            onClick = {
                                onStatusUpdate(reason)
                                showExceptionDialog = false
                            },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray.copy(alpha = 0.1f), contentColor = TextDark)
                        ) {
                            Text(reason.replace("_", " "))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showExceptionDialog = false }) {
                    Text("CANCEL")
                }
            }
        )
    }
}

@Composable
fun StopStatusChip(status: String) {
    val (bgColor, textColor, label) = when (status) {
        "COMPLETED" -> Triple(Color(0xFFE2F4E3), Color(0xFF2E7D32), "completed")
        "PENDING" -> Triple(Color(0xFFFFF3E0), Color(0xFFE65100), "pending")
        "IN_PROGRESS" -> Triple(Color(0xFFE8F5E9), Color(0xFF2E7D32), "in progress")
        "ATTEMPTED_NO_ACCESS" -> Triple(Color(0xFFFFEBEE), Color(0xFFC62828), "no access")
        "ATTEMPTED_ABSENT" -> Triple(Color(0xFFFFEBEE), Color(0xFFC62828), "absent")
        else -> Triple(Color(0xFFFFEBEE), Color(0xFFC62828), status.lowercase())
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
