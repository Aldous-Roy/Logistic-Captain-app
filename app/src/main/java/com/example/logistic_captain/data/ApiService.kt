package com.example.logistic_captain.data

import com.example.logistic_captain.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<LoginResponse>>

    @GET("api/route/my-route")
    suspend fun getMyRoute(): Response<ApiResponse<RouteResponse>>

    @POST("api/location/update")
    suspend fun updateLocation(@Body request: LocationUpdateRequest): Response<ApiResponse<DriverLocationResponse>>

    @PATCH("api/stop/{id}/status")
    suspend fun updateStopStatus(
        @Path("id") stopId: String,
        @Body request: StopStatusUpdateRequest
    ): Response<ApiResponse<StopResponse>>

    @POST("api/driver/check-in")
    suspend fun checkIn(@Body location: LocationUpdateRequest): Response<ApiResponse<Unit>>

    @POST("api/driver/check-out")
    suspend fun checkOut(): Response<ApiResponse<Unit>>

    @Multipart
    @POST("api/pod/upload")
    suspend fun uploadPod(
        @Part("deliveryId") deliveryId: okhttp3.RequestBody,
        @Part("customerSignature") signature: okhttp3.RequestBody?,
        @Part file: okhttp3.MultipartBody.Part
    ): Response<ApiResponse<PodUploadResponse>>

    @POST("api/tracking/locations/bulk")
    suspend fun bulkUpdateLocations(@Body locations: List<LocationUpdateRequest>): Response<ApiResponse<Unit>>

    @POST("api/drivers/break")
    suspend fun toggleBreak(@Body request: BreakRequest): Response<ApiResponse<Unit>>

    @GET("api/drivers/me")
    suspend fun getMyProfile(): Response<ApiResponse<DriverProfileResponse>>

    @PUT("api/drivers/me")
    suspend fun updateMyProfile(@Body request: DriverProfileUpdateRequest): Response<ApiResponse<DriverProfileResponse>>
}
