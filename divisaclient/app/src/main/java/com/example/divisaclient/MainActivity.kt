package com.example.divisaclient

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ExchangeRateScreen()
        }
    }
}

@Composable
fun ExchangeRateScreen() {
    val context = LocalContext.current
    val repository = remember { ExchangeRateRepository(context) }
    var rates by remember { mutableStateOf(emptyList<Triple<String, Double, String>>()) }
    var selectedCurrency by remember { mutableStateOf("USD") }
    var expanded by remember { mutableStateOf(false) }

    // Estado para fechas y horas seleccionadas
    var startDateTime by remember { mutableStateOf(getCurrentDateTime()) }
    var endDateTime by remember { mutableStateOf(getCurrentDateTime()) }

    LaunchedEffect(Unit) {
        rates = repository.getExchangeRates()
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Seleccione una moneda para graficar", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        // Selector de moneda
        Box {
            Button(onClick = { expanded = true }) {
                Text(selectedCurrency)
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                rates.distinctBy { it.first }.forEach { (currency, _, _) ->
                    DropdownMenuItem(
                        onClick = {
                            selectedCurrency = currency
                            expanded = false
                        },
                        text = { Text(currency) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Selectores de fecha y hora
        DateTimeSelector("Antes", startDateTime) { newDateTime -> startDateTime = newDateTime }
        Spacer(modifier = Modifier.height(8.dp))
        DateTimeSelector("Después", endDateTime) { newDateTime -> endDateTime = newDateTime }

        Spacer(modifier = Modifier.height(16.dp))

        // Gráfico filtrado por fecha y hora
        ExchangeRateChart(selectedCurrency, startDateTime, endDateTime)
    }
}

/**
 *  Componente de Selector de Fecha y Hora
 */
@Composable
fun DateTimeSelector(label: String, dateTime: String, onDateTimeSelected: (String) -> Unit) {
    val context = LocalContext.current

    Button(
        onClick = {
            val calendar = Calendar.getInstance()
            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

            DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    TimePickerDialog(
                        context,
                        { _, hour, minute ->
                            calendar.set(year, month, dayOfMonth, hour, minute)
                            val selectedDateTime = dateFormat.format(calendar.time)
                            onDateTimeSelected(selectedDateTime)
                        },
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE),
                        true
                    ).show()
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("$label: $dateTime")
    }
}

/**
 *  Función para obtener la fecha y hora actuales en formato "dd/MM/yyyy HH:mm"
 */
fun getCurrentDateTime(): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date())
}
