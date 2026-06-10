package com.example.logistic_captain.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.logistic_captain.model.StopResponse
import com.example.logistic_captain.ui.theme.LogisticsBlue
import com.example.logistic_captain.ui.theme.LogisticsOrange

@Composable
fun StopCard(
    stop: StopResponse,
    onNavigateClick: () -> Unit,
    onStatusUpdate: (String) -> Unit,
    onDeliverClick: () -> Unit
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = LogisticsBlue,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "STOP #${stop.stopNumber}",
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        color = LogisticsBlue,
                        letterSpacing = 1.sp
                    )
                }
                StatusChip(status = stop.status)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stop.customerName,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            
            Text(
                text = stop.address,
                fontSize = 14.sp,
                color = Color.DarkGray,
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "PACKAGES", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Text(text = "${stop.packageCount}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = LogisticsBlue)
                }
                
                stop.eta?.let {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "EXPECTED ETA", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Text(text = it, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = LogisticsOrange)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (stop.status == "PENDING") {
                    Button(
                        onClick = onDeliverClick,
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("DELIVER", fontWeight = FontWeight.Bold)
                    }
                }
                
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
                    shape = RoundedCornerShape(8.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp)
                ) {
                    Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("MAPS", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun StatusChip(status: String) {
    val (color, label) = when (status) {
        "PENDING" -> Color(0xFFFFC107) to "PENDING"
        "COMPLETED" -> Color(0xFF4CAF50) to "COMPLETED"
        "ATTEMPTED_NO_ACCESS", "ATTEMPTED_ABSENT" -> Color(0xFFF44336) to "FAILED"
        else -> Color.Gray to status
    }
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}
