package nfc.receptor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { EmulatorScreen() }
    }
}

@Composable
fun EmulatorScreen() {
    var amount by remember { mutableStateOf("123") }

    Scaffold { pad ->
        Column(
            Modifier
                .padding(pad)
                .padding(24.dp)
                .fillMaxSize()
        ) {
            Text("Aplicacion Emisor", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Monto a transmitir") },
                singleLine = true
            )

            Spacer(Modifier.height(16.dp))

            Button(onClick = {
                NfcCardService.PayloadHolder.bytes =
                    """$:${amount.toIntOrNull() ?: 0}""".toByteArray()
            }) { Text("Actualizar pago") }

            Spacer(Modifier.height(32.dp))
        }
    }
}