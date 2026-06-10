package com.example.logistic_captain.ui.pod

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.logistic_captain.data.ApiService
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

class PodViewModel(private val apiService: ApiService) : ViewModel() {

    var photoUri by mutableStateOf<Uri?>(null)
    var signatureBitmap by mutableStateOf<Bitmap?>(null)
    var isUploading by mutableStateOf(false)
    var uploadSuccess by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    fun uploadPod(context: Context, stopId: String) {
        val uri = photoUri ?: return
        
        viewModelScope.launch {
            isUploading = true
            errorMessage = null
            try {
                val file = getFileFromUri(context, uri)
                val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
                val stopIdPart = stopId.toRequestBody("text/plain".toMediaTypeOrNull())

                // Optional signature can be added here as another Part
                
                val response = apiService.uploadPod(stopIdPart, null, body)
                if (response.isSuccessful) {
                    uploadSuccess = true
                } else {
                    errorMessage = "Upload failed: ${response.message()}"
                }
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isUploading = false
            }
        }
    }

    private fun getFileFromUri(context: Context, uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)
        val file = File(context.cacheDir, "pod_photo.jpg")
        val outputStream = FileOutputStream(file)
        inputStream?.copyTo(outputStream)
        return file
    }
}
