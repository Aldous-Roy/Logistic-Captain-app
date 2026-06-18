package com.example.logistic_captain.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
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
        if (unsyncedLocations.isEmpty()) return

        // Batch locations into one request
        val locationRequests = unsyncedLocations.map { 
            LocationUpdateRequest(it.latitude, it.longitude) 
        }

        try {
            val response = apiService.bulkUpdateLocations(locationRequests)
            if (response.isSuccessful) {
                database.locationDao().markAsSynced(unsyncedLocations.map { it.id })
            }
        } catch (e: Exception) {
            // Log error
        }
    }

    private suspend fun syncPods() {
        val unsyncedPods = database.podDao().getUnsyncedPods()
        for (pod in unsyncedPods) {
            try {
                val file = File(pod.imagePath)
                if (!file.exists()) {
                    database.podDao().markAsSynced(pod.id)
                    continue
                }

                val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
                val stopIdPart = pod.deliveryId.toRequestBody("text/plain".toMediaTypeOrNull())
                
                // Add signature if available
                val signaturePart = pod.signature?.let {
                    it.toRequestBody("text/plain".toMediaTypeOrNull())
                }

                val response = apiService.uploadPod(stopIdPart, signaturePart, body)
                if (response.isSuccessful) {
                    database.podDao().markAsSynced(pod.id)
                }
            } catch (e: Exception) {
                // Ignore individual failure
            }
        }
    }
}
