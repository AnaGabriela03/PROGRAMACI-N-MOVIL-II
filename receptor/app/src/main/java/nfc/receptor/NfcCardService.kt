package nfc.receptor

import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import android.util.Log

class NfcCardService : HostApduService() {

    companion object {
        private val STATUS_OK = byteArrayOf(0x90.toByte(), 0x00.toByte())
        private val STATUS_ERR = byteArrayOf(0x6F.toByte(), 0x00.toByte())
    }

    object PayloadHolder { @Volatile var bytes = """{"monto":123}""".toByteArray() }

    override fun processCommandApdu(cmd: ByteArray?, extras: Bundle?): ByteArray {
        // SELECT AID comienza con 00 A4 04 00
        val isSelect = cmd?.take(4)
            ?.toByteArray()
            ?.contentEquals(byteArrayOf(0x00, 0xA4.toByte(), 0x04, 0x00)) ?: false

        return if (isSelect) {
            Log.d("HCE", "SELECT recibido; respondiendo payload")
            PayloadHolder.bytes + STATUS_OK
        } else STATUS_ERR
    }

    override fun onDeactivated(reason: Int) {
        Log.d("HCE", "Link terminado: $reason")
    }
}