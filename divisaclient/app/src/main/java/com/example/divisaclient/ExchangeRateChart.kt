package com.example.divisaclient

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ExchangeRateChart(selectedCurrency: String, startDateTime: String, endDateTime: String) {
    val context = LocalContext.current
    val repository = remember { ExchangeRateRepository(context) }
    var chartData by remember { mutableStateOf(emptyList<Entry>()) }
    var isLoading by remember { mutableStateOf(true) }
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    // Convertir fechas a timestamps
    val startTimestamp = dateFormat.parse(startDateTime)?.time ?: 0
    val endTimestamp = dateFormat.parse(endDateTime)?.time ?: Long.MAX_VALUE

    //  Se ejecuta cada vez que cambia la moneda, la fecha o la hora
    LaunchedEffect(selectedCurrency, startDateTime, endDateTime) {
        isLoading = true

        val rates = repository.getExchangeRates()
            .filter { it.first == selectedCurrency } // Filtra por moneda
            .filter { dateFormat.parse(it.third)?.time in startTimestamp..endTimestamp } // Filtra por fecha
            .mapIndexed { index, data -> Entry(index.toFloat(), data.second.toFloat()) }

        chartData = rates
        isLoading = false

        if (chartData.isEmpty()) {
            Log.e("ExchangeRateChart", " No hay datos para $selectedCurrency en este rango")
        } else {
            Log.d("ExchangeRateChart", "Datos cargados para $selectedCurrency: $chartData")
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        if (isLoading) {
            CircularProgressIndicator()
        } else if (chartData.isEmpty()) {
            Text("No hay datos disponibles para $selectedCurrency en este rango", style = MaterialTheme.typography.bodyMedium)
        } else {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    LineChart(ctx).apply {
                        description = Description().apply { text = "Variación de $selectedCurrency" }

                        val dataSet = LineDataSet(chartData, "Valor en MXN").apply {
                            color = android.graphics.Color.MAGENTA
                            valueTextSize = 12f
                            setDrawValues(true)
                            setValueTextColor(android.graphics.Color.BLUE)
                            valueFormatter = object : ValueFormatter() {
                                override fun getPointLabel(entry: Entry?): String {
                                    return entry?.y?.toString() ?: ""
                                }
                            }
                        }

                        data = LineData(dataSet)
                        invalidate() //  Se fuerza la actualización del gráfico
                    }
                },
                update = { chart ->
                    val dataSet = LineDataSet(chartData, "Valor en MXN").apply {
                        color = android.graphics.Color.MAGENTA
                        valueTextSize = 12f
                        setDrawValues(true)
                        setValueTextColor(android.graphics.Color.BLUE)
                        valueFormatter = object : ValueFormatter() {
                            override fun getPointLabel(entry: Entry?): String {
                                return entry?.y?.toString() ?: ""
                            }
                        }
                    }

                    chart.data = LineData(dataSet)
                    chart.invalidate() //  Se asegura de redibujar el gráfico al cambiar la moneda o las fechas
                }
            )
        }
    }
}
