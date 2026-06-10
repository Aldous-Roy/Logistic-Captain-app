package com.example.logistic_captain.data

import com.example.logistic_captain.model.LoginRequest
import com.example.logistic_captain.model.LoginResponse
import com.example.logistic_captain.model.ApiResponse
import retrofit2.Response

class AuthRepository(private val apiService: ApiService) {
    suspend fun login(employeeId: String, pin: String): Response<ApiResponse<LoginResponse>> {
        return apiService.login(LoginRequest(employeeId, pin))
    }
}
