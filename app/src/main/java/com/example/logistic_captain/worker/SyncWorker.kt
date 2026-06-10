package com.example.logistic_captain.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.logistic_captain.data.ApiService
import com.example.logistic_captain.data.AppDatabase
import com.example.logistic_captain.data.RetrofitClient
import com.example.logistic_captain.model.LocationUpdateRequest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private val database = AppDatabase.getDatabase(appContext)
    private val apiService = RetrofitClient.apiService

    override suspend fun doWork(): Result {
        return try {
            syncLocations()
            syncPods()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private suspend fun syncLocations() {
        val unsyncedLocations = database.locationDao().getUnsyncedLocations()
        for (location in unsyncedLocations) {
            try {
                val response = apiService.updateLocation(
                    LocationUpdateRequest(location.latitude, location.longitude)
                )
                if (response.isSuccessful) {
                    database.locationDao().markAsSynced(listOf(location.id))
                }
            } catch (e: Exception) {
                // Ignore individual failure, continue with others
            }
        }
    }

    private suspend fun syncPods() {
        val unsyncedPods = database.podDao().getUnsyncedPods()
        for (pod in unsyncedPods) {
            try {
                val file = File(pod.imagePath)
                if (!file.exists()) {
                    database.podDao().markAsSynced(pod.id) // Or handle error
                    continue
                }

                val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
                val stopIdPart = pod.deliveryId.toRequestBody("text/plain".toMediaTypeOrNull())

                val response = apiService.uploadPod(stopIdPart, null, body)
                if (response.isSuccessful) {
                    database.podDao().markAsSynced(pod.id)
                }
            } catch (e: Exception) {
                // Ignore individual failure
            }
        }
    }
}
