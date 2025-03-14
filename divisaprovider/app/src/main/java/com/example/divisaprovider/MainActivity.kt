package com.example.divisaprovider

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.work.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        scheduleWork() // 🔄 Programar actualización automática cada 1 minuto

        setContent {
            ExchangeRateUI()
        }
    }

    private fun scheduleWork() {
        val workRequest = OneTimeWorkRequestBuilder<SyncExchangeRatesWorker>()
            .setInitialDelay(15, TimeUnit.MINUTES) // ⏳ Ejecutar cada 1 minuto
            .build()

        WorkManager.getInstance(this).enqueue(workRequest)

        WorkManager.getInstance(this).getWorkInfoByIdLiveData(workRequest.id).observe(this) { workInfo ->
            if (workInfo != null && workInfo.state == WorkInfo.State.SUCCEEDED) {
                scheduleWork() // 🔁 Reprogramar la tarea después de completarse
            }
        }
    }
}

@Composable
fun ExchangeRateUI() {
    val context = LocalContext.current
    val dbHelper = remember { ExchangeRateDbHelper(context) }
    val rates = remember { mutableStateListOf<Triple<String, Double, String>>() }

    LaunchedEffect(Unit) {
        val cursor = dbHelper.readableDatabase.query(
            "exchange_rates",
            arrayOf("currency", "rate", "timestamp"),
            null, null, null, null, "timestamp DESC"
        )

        cursor.use {
            while (it.moveToNext()) {
                val currency = it.getString(it.getColumnIndexOrThrow("currency"))
                val rate = it.getDouble(it.getColumnIndexOrThrow("rate"))
                val timestamp = it.getLong(it.getColumnIndexOrThrow("timestamp"))

                val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
                val formattedDate = dateFormat.format(Date(timestamp))

                rates.add(Triple(currency, rate, formattedDate))
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Historial de Tasas de Cambio", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(rates) { (currency, rate, datetime) ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Moneda: $currency", style = MaterialTheme.typography.bodyLarge)
                        Text("Tasa: $rate", style = MaterialTheme.typography.bodyMedium)
                        Text("Fecha: $datetime", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
