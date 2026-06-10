package com.example.logistic_captain.ui.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.logistic_captain.data.AuthRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class LoginViewModel(private val repository: AuthRepository) : ViewModel() {

    var employeeId by mutableStateOf("")
    var pin by mutableStateOf("")
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    private val _loginSuccess = MutableSharedFlow<Boolean>()
    val loginSuccess = _loginSuccess.asSharedFlow()

    fun onLoginClick() {
        if (employeeId.isBlank() || pin.isBlank()) {
            errorMessage = "Please enter both Employee ID and PIN"
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val response = repository.login(employeeId, pin)
                if (response.isSuccessful && response.body()?.status == "success") {
                    // Save token (using DataStore or SharedPreferences - to be implemented)
                    _loginSuccess.emit(true)
                } else {
                    errorMessage = response.body()?.message ?: "Login failed. Please check your credentials."
                }
            } catch (e: Exception) {
                errorMessage = "Network error: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
}
