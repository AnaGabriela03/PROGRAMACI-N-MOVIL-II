package com.example.divisa

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.work.*
import com.example.divisa.ui.theme.DivisaTheme
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Configurar WorkManager para sincronizar cada hora
        val syncWorkRequest = PeriodicWorkRequestBuilder<SyncExchangeRatesWorker>(15, TimeUnit.MINUTES)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "sync_rates",
            ExistingPeriodicWorkPolicy.KEEP,
            syncWorkRequest
        )

        setContent {
            DivisaTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

                    // Recuperar las tasas de cambio desde la base de datos SQLite
                    val dbHelper = ExchangeRateDbHelper(applicationContext)
                    val rates = dbHelper.getRates()

                    // Obtener la última hora de actualización
                    val lastUpdateTime = dbHelper.getLastUpdateTime()

                    // Calcular la siguiente hora de actualización
                    val nextUpdateTime = dbHelper.getNextUpdateTime(lastUpdateTime)

                    // Convertir las horas a formato legible
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
                    val lastUpdateFormatted = lastUpdateTime?.let { dateFormat.format(Date(it)) } ?: "No disponible"
                    val nextUpdateFormatted = nextUpdateTime?.let { dateFormat.format(Date(it)) } ?: "No disponible"

                    // Convertir el mapa de tasas en el formato de texto sin corchetes
                    val ratesText = if (rates.isNotEmpty()) {
                        rates.joinToString("\n") { (currency, rate) -> "\"$currency\": $rate" }
                    } else {
                        "No hay tasas disponibles"
                    }

                    // Imprimir en la consola (Logcat)
                    Log.d("ExchangeRates", "Última actualización: $lastUpdateFormatted")
                    Log.d("ExchangeRates", "Próxima actualización: $nextUpdateFormatted")
                    Log.d("ExchangeRates", "Tasas de cambio:\n$ratesText")

                    // Agregar Scroll
                    val scrollState = rememberScrollState()  // Estado para el scroll
                    Column(modifier = Modifier
                        .padding(innerPadding)
                        .verticalScroll(scrollState)) {

                        // Mostrar las horas de actualización primero
                        Text(
                            text = "Última actualización: $lastUpdateFormatted",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 8.dp, end = 8.dp)
                        )
                        Text(
                            text = "Próxima actualización: $nextUpdateFormatted",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 8.dp, end = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Mostrar las tasas de cambio sin corchetes y con salto de línea
                        Text(
                            text = "1 MXN=",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = ratesText,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 8.dp, end = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    DivisaTheme {
        val dummyRates = listOf("MXN" to 1.0, "AED" to 0.1788, "AFN" to 3.5722, "ALL" to 4.6505)
        val ratesText = dummyRates.joinToString("\n") { (currency, rate) -> "\"$currency\": $rate" }

        Text(text = ratesText)
    }
}
