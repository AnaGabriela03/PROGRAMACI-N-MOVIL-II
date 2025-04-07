package com.example.mapaderegreso

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiClient {

    @GET("v2/directions/driving-car")
    suspend fun fetchRoute(
        @Query("api_key") key: String,
        @Query("start") origin: String,
        @Query("end") destination: String
    ): Response<DireccionRespuesta>
}


