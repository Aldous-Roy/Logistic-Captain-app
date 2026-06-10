package com.example.logistic_captain.model

import java.time.LocalDateTime

data class LoginRequest(
    val employeeId: String,
    val password: String
)

data class LoginResponse(
    val token: String,
    val tokenType: String = "Bearer",
    val expiresInMs: Long? = null,
    val employeeId: String,
    val name: String,
    val role: String
)

data class ApiResponse<T>(
    val status: String,
    val statusCode: Int,
    val data: T?,
    val message: String? = null
)

data class RouteResponse(
    val id: String,
    val routeNumber: String,
    val status: String,
    val stops: List<StopResponse>
)

data class StopResponse(
    val id: String,
    val stopNumber: Int,
    val address: String,
    val customerName: String,
    val packageCount: Int,
    val status: String, // PENDING, COMPLETED, ATTEMPTED_NO_ACCESS, ATTEMPTED_ABSENT
    val eta: String?
)

data class LocationUpdateRequest(
    val latitude: Double,
    val longitude: Double
)

data class DriverLocationResponse(
    val id: String,
    val driverId: String,
    val latitude: Double,
    val longitude: Double,
    val timestamp: String
)

data class StopStatusUpdateRequest(
    val status: String,
    val notes: String? = null
)

data class PodUploadResponse(
    val id: String,
    val deliveryId: String,
    val imageUrl: String
)
