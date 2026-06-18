package com.example.logistic_captain.data

import com.example.logistic_captain.model.*
import retrofit2.Response

class RouteRepository(private val apiService: ApiService) {
    suspend fun getMyRoute(): Response<ApiResponse<RouteResponse>> {
        return apiService.getMyRoute()
    }

    suspend fun updateStopStatus(stopId: String, status: String, notes: String?): Response<ApiResponse<StopResponse>> {
        return apiService.updateStopStatus(stopId, StopStatusUpdateRequest(status, notes))
    }

    suspend fun checkIn(lat: Double, lng: Double): Response<ApiResponse<Unit>> {
        return apiService.checkIn(LocationUpdateRequest(lat, lng))
    }

    suspend fun checkOut(): Response<ApiResponse<Unit>> {
        return apiService.checkOut()
    }

    suspend fun toggleBreak(isOnBreak: Boolean): Response<ApiResponse<Unit>> {
        return apiService.toggleBreak(BreakRequest(isOnBreak))
    }
}
