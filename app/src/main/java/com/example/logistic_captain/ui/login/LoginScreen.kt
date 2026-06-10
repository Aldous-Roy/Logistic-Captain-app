package com.example.logistic_captain.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.logistic_captain.ui.theme.PremiumGreen
import com.example.logistic_captain.ui.theme.PremiumGreenLight
import com.example.logistic_captain.ui.theme.CreamBackground
import com.example.logistic_captain.ui.theme.TextDark

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: () -> Unit
) {
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loginSuccess.collect { success ->
            if (success) onLoginSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Premium Package Logo (Green Square with rounded corners and package icon)
            Surface(
                modifier = Modifier.size(90.dp),
                shape = RoundedCornerShape(24.dp),
                color = PremiumGreen
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Inventory,
                        contentDescription = "App Logo",
                        modifier = Modifier.size(44.dp),
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Welcome Back",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = PremiumGreen,
                letterSpacing = (-0.5).sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Sign in to start your delivery shift",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = PremiumGreenLight
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Employee ID Input Section
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Employee ID",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = PremiumGreen,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                OutlinedTextField(
                    value = viewModel.employeeId,
                    onValueChange = { viewModel.employeeId = it },
                    placeholder = { Text("Enter your employee ID", color = PremiumGreenLight.copy(alpha = 0.6f)) },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = PremiumGreen) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextDark,
                        unfocusedTextColor = TextDark,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        cursorColor = PremiumGreen,
                        errorBorderColor = MaterialTheme.colorScheme.error
                    )
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // PIN Input Section
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "PIN",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = PremiumGreen,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                OutlinedTextField(
                    value = viewModel.pin,
                    onValueChange = { viewModel.pin = it },
                    placeholder = { Text("Enter your PIN", color = PremiumGreenLight.copy(alpha = 0.6f)) },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = PremiumGreen) },
                    trailingIcon = {
                        val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, contentDescription = null, tint = PremiumGreenLight)
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextDark,
                        unfocusedTextColor = TextDark,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        cursorColor = PremiumGreen,
                        errorBorderColor = MaterialTheme.colorScheme.error
                    )
                )
            }

            viewModel.errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            Button(
                onClick = { viewModel.onLoginClick() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PremiumGreen,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                enabled = !viewModel.isLoading
            ) {
                if (viewModel.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Text("Sign In", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.height(28.dp))

            // Footer Links
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Need Help?",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = PremiumGreen
                )
                Text(
                    text = "   |   ",
                    fontSize = 14.sp,
                    color = PremiumGreenLight.copy(alpha = 0.5f)
                )
                Text(
                    text = "Forgot PIN?",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = PremiumGreen
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Text(
                text = "Last-Mile Delivery v2.0 © 2026",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = PremiumGreenLight.copy(alpha = 0.8f)
            )
        }
    }
}
