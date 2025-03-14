package com.example.divisaprovider

import retrofit2.Call
import retrofit2.http.GET

interface ApiService {
    @GET("8ea5b29a23f6bac194ef94dd/latest/MXN")  // 🔹 SOLO la parte final de la URL
    fun getExchangeRates(): Call<ExchangeResponse>
}
