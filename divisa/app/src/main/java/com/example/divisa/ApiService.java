package com.example.divisa;  // Ajusta este paquete al que estés utilizando

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    @GET("latest/MXN")
    Call<ExchangeResponse> getExchangeRate();
}
