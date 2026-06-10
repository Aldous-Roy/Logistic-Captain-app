package com.example.logistic_captain.ui.dashboard

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.logistic_captain.data.RouteRepository
import com.example.logistic_captain.model.RouteResponse
import kotlinx.coroutines.launch

class DashboardViewModel(private val repository: RouteRepository) : ViewModel() {

    var route by mutableStateOf<RouteResponse?>(null)
    var isShiftStarted by mutableStateOf(false)
    var isOnBreak by mutableStateOf(false)
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    fun loadRoute() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val response = repository.getMyRoute()
                if (response.isSuccessful) {
                    route = response.body()?.data
                } else {
                    errorMessage = "Failed to load route"
                }
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }

    fun startShift(lat: Double, lng: Double) {
        viewModelScope.launch {
            isLoading = true
            try {
                val response = repository.checkIn(lat, lng)
                if (response.isSuccessful) {
                    isShiftStarted = true
                    loadRoute()
                }
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }

    fun endShift() {
        viewModelScope.launch {
            isLoading = true
            try {
                val response = repository.checkOut()
                if (response.isSuccessful) {
                    isShiftStarted = false
                    route = null
                }
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }

    fun toggleBreak() {
        isOnBreak = !isOnBreak
        // In a real app, notify backend here
    }

    fun updateStopStatus(stopId: String, status: String) {
        viewModelScope.launch {
            try {
                val response = repository.updateStopStatus(stopId, status, null)
                if (response.isSuccessful) {
                    loadRoute() // Refresh
                }
            } catch (e: Exception) {
                errorMessage = e.message
            }
        }
    }
}
