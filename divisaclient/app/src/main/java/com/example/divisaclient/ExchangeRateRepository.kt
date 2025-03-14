package com.example.divisaclient

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

class ExchangeRateRepository(private val context: Context) {
    companion object {
        private const val PROVIDER_URI = "content://com.example.divisaprovider/exchange_rates"
    }

    fun getExchangeRates(): List<Triple<String, Double, String>> {
        val rates = mutableListOf<Triple<String, Double, String>>()

        val cursor: Cursor? = try {
            context.contentResolver.query(
                Uri.parse(PROVIDER_URI),
                arrayOf("currency", "rate", "timestamp"),
                null, null, "timestamp DESC"
            )
        } catch (e: Exception) {
            Log.e("ExchangeRateRepository", "Error al consultar ContentProvider: ${e.message}")
            null
        }

        cursor?.use {
            while (it.moveToNext()) {
                val currency = it.getString(it.getColumnIndexOrThrow("currency"))
                val rate = it.getDouble(it.getColumnIndexOrThrow("rate"))
                val timestamp = it.getLong(it.getColumnIndexOrThrow("timestamp"))

                val formattedDate = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date(timestamp))

                // Se agregan los datos a la lista
                rates.add(Triple(currency, rate, formattedDate))
                Log.d("ExchangeRateRepository", "Carga exitosa: $currency - $rate - $formattedDate")
            }
        }

        if (rates.isEmpty()) {
            Log.e("ExchangeRateRepository", " No se encontraron datos en la base de datos")
        }

        return rates
    }
}
