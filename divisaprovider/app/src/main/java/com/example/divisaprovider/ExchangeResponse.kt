package com.example.divisaprovider

import com.google.gson.annotations.SerializedName

data class ExchangeResponse(
    @SerializedName("base_code") val base: String,
    @SerializedName("conversion_rates") val rates: Map<String, Double>
)
