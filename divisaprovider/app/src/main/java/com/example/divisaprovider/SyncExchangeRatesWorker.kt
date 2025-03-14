package com.example.divisaprovider

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class SyncExchangeRatesWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    override fun doWork(): Result {
        Log.d("SyncExchangeRatesWorker", "🔄 Obteniendo tasas de cambio de la API...")

        val dbHelper = ExchangeRateDbHelper(applicationContext)

        RetrofitClient.apiService.getExchangeRates().enqueue(object : Callback<ExchangeResponse> {
            override fun onResponse(call: Call<ExchangeResponse>, response: Response<ExchangeResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    Log.d("SyncExchangeRatesWorker", "✅ Datos recibidos correctamente.")

                    val exchangeResponse = response.body()
                    val rates = exchangeResponse?.rates ?: emptyMap()
                    val timestamp = System.currentTimeMillis()

                    // 📅 Convertir timestamp a formato legible
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
                    val formattedDate = dateFormat.format(Date(timestamp))

                    Log.d("SyncExchangeRatesWorker", "🕒 Fecha de actualización: $formattedDate")
                    Log.d("SyncExchangeRatesWorker", "💾 Guardando tasas en la base de datos...")

                    dbHelper.insertExchangeRates(rates, timestamp) // ✅ Se guardan múltiples registros

                    Log.d("SyncExchangeRatesWorker", "✅ Datos guardados en SQLite.")

                    // 🔹 Imprimir valores de divisas en Logcat con la fecha
                    rates.forEach { (currency, rate) ->
                        Log.d("SyncExchangeRatesWorker", "🕒 $formattedDate | 1 MXN = $rate $currency")
                    }
                } else {
                    Log.e("SyncExchangeRatesWorker", "❌ Error en la respuesta de la API: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<ExchangeResponse>, t: Throwable) {
                Log.e("SyncExchangeRatesWorker", "❌ Error al obtener datos de la API: ${t.message}")
            }
        })

        return Result.success()
    }
}
