package com.example.divisa

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SyncExchangeRatesWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        // Crear Retrofit e instanciar el API
        val apiService = RetrofitClient.getApiService()
        val call = apiService.getExchangeRate()

        return try {
            // Hacer la llamada de forma sincrónica
            val response = call.execute()
            if (response.isSuccessful) {
                val exchangeResponse = response.body()
                val ratesMap = exchangeResponse?.rates ?: emptyMap()

                // Obtener la última hora de actualización
                val dbHelper = ExchangeRateDbHelper(applicationContext)
                val lastUpdateTime = dbHelper.getLastUpdateTime()

                val timestampToUse = lastUpdateTime ?: System.currentTimeMillis()
                // Guardar los resultados en la base de datos solo si es válido
                dbHelper.insertRates(ratesMap, timestampToUse)

                // Enviamos el succes
                Result.success()
            } else {
                // Si la respuesta es un error, reintenta
                Result.retry()
            }
        } catch (e: Exception) {
            // En caso de error retornamos failure
            Result.failure()
        }
    }
}

