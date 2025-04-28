package NFC.conectividadandroidnfc

import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.io.IOException
import java.nio.ByteBuffer

class MainActivity : ComponentActivity() {

    private lateinit var nfcAdapter: NfcAdapter
    private val aid = "F012345678"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        setContent { ReaderScreen(::latestMessage) }
    }

    private var latestMessage by mutableStateOf("Acerca el otro teléfono")

    override fun onResume() {
        super.onResume()
        enableReaderMode()
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter.disableReaderMode(this)
    }

    private fun enableReaderMode() {
        val flags = NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_NFC_B
        nfcAdapter.enableReaderMode(this, { tag -> handleTag(tag) }, flags, null)
    }

    private fun handleTag(tag: Tag) {
        try {
            val isoDep = IsoDep.get(tag) ?: return
            isoDep.connect()

            val selectApdu = buildSelectApdu(aid)
            val result = isoDep.transceive(selectApdu)
            isoDep.close()

            val sw = result.takeLast(2).toByteArray()
            val payload = result.dropLast(2).toByteArray()

            if (sw.contentEquals(byteArrayOf(0x90.toByte(), 0x00.toByte()))) {
                latestMessage = "Recibido: ${payload.toString(Charsets.UTF_8)}"
            } else latestMessage = "SW=${sw.joinToString { "%02X".format(it) }}"
        } catch (e: IOException) {
            latestMessage = "Error: ${e.message}"
            Log.e("Reader", e.message ?: "error")
        }
    }

    private fun buildSelectApdu(aidHex: String): ByteArray {
        val aidBytes = aidHex.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
        return ByteBuffer.allocate(6 + aidBytes.size).apply {
            put(0x00)           // CLA
            put(0xA4.toByte())  // INS
            put(0x04)           // P1
            put(0x00)           // P2
            put(aidBytes.size.toByte())
            put(aidBytes)
            put(0x00)           // Le
        }.array()
    }
}

@Composable
fun ReaderScreen(messageProvider: () -> String) {
    val msg by remember { derivedStateOf { messageProvider() } }
    Scaffold { pad ->
        Column(
            Modifier
                .padding(pad)
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(msg, style = MaterialTheme.typography.headlineSmall)
        }
    }
}