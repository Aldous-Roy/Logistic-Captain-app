package com.example.logistic_captain

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.logistic_captain.data.AuthRepository
import com.example.logistic_captain.data.RetrofitClient
import com.example.logistic_captain.data.RouteRepository
import com.example.logistic_captain.service.LocationService
import com.example.logistic_captain.ui.dashboard.DashboardScreen
import com.example.logistic_captain.ui.dashboard.DashboardViewModel
import com.example.logistic_captain.ui.login.LoginScreen
import com.example.logistic_captain.ui.login.LoginViewModel
import com.example.logistic_captain.ui.pod.PodScreen
import com.example.logistic_captain.ui.pod.PodViewModel
import com.example.logistic_captain.ui.theme.LogisticCaptainTheme
import com.example.logistic_captain.worker.SyncWorker
import androidx.work.*
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        // Permissions handled
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        requestPermissions()

        val apiService = RetrofitClient.apiService
        val authRepository = AuthRepository(apiService)
        val routeRepository = RouteRepository(apiService)
        
        val loginViewModel = LoginViewModel(authRepository)
        val dashboardViewModel = DashboardViewModel(routeRepository)
        val podViewModel = PodViewModel(apiService)

        setupSyncWorker()

        setContent {
            LogisticCaptainTheme {
                AppNavigation(
                    loginViewModel = loginViewModel,
                    dashboardViewModel = dashboardViewModel,
                    podViewModel = podViewModel,
                    onStartTracking = { startTrackingService() },
                    onStopTracking = { stopTrackingService() }
                )
            }
        }
    }

    private fun requestPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.CAMERA
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        
        val missingPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            requestPermissionLauncher.launch(missingPermissions.toTypedArray())
        }
    }

    private fun startTrackingService() {
        val intent = Intent(this, LocationService::class.java)
        startForegroundService(intent)
    }

    private fun stopTrackingService() {
        stopService(Intent(this, LocationService::class.java))
    }

    private fun setupSyncWorker() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "SyncData",
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }
}

@Composable
fun AppNavigation(
    loginViewModel: LoginViewModel,
    dashboardViewModel: DashboardViewModel,
    podViewModel: PodViewModel,
    onStartTracking: () -> Unit,
    onStopTracking: () -> Unit
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                viewModel = loginViewModel,
                onLoginSuccess = {
                    navController.navigate("dashboard") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        composable("dashboard") {
            DashboardScreen(
                viewModel = dashboardViewModel,
                onLogout = {
                    onStopTracking()
                    navController.navigate("login") {
                        popUpTo("dashboard") { inclusive = true }
                    }
                },
                onStartShift = {
                    onStartTracking()
                },
                onDeliverClick = { stopId ->
                    navController.navigate("pod/$stopId")
                }
            )
        }
        composable("pod/{stopId}") { backStackEntry ->
            val stopId = backStackEntry.arguments?.getString("stopId") ?: ""
            PodScreen(
                stopId = stopId,
                viewModel = podViewModel,
                onBack = { navController.popBackStack() },
                onSuccess = {
                    navController.popBackStack()
                    dashboardViewModel.loadRoute()
                }
            )
        }
    }
}
