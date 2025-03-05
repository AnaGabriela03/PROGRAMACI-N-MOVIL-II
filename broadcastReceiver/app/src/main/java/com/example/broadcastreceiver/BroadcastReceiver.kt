package com.example.broadcastreceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsManager
import android.telephony.TelephonyManager
import android.util.Log

class BroadcastReceiver : BroadcastReceiver() {
    val tag:String ="Receiver"
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            val estado = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
            val numeroEntrante = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

            if (estado == TelephonyManager.EXTRA_STATE_RINGING) {
                Log.d(tag, "Llamada entrante de: $numeroEntrante")

                val sharedPreferences = context?.getSharedPreferences("datos", Context.MODE_PRIVATE)
                val numeroGuardado = sharedPreferences?.getString("numero", "")
                val mensaje = sharedPreferences?.getString("mensaje", "")
                Log.d(tag, numeroGuardado + mensaje + "")

                if (numeroEntrante == numeroGuardado) {
                    Log.d(tag, "¡Número detectado! Enviando SMS...")
                    enviarSMS(numeroEntrante, mensaje ?: "", context)
                }
            }
        }
    }
    private fun enviarSMS(numero: String?, mensaje: String, context: Context?) {
        if (numero != null && mensaje.isNotEmpty() && context != null) {
            try {
                val smsManager = SmsManager.getDefault()
                smsManager.sendTextMessage(numero, null, mensaje, null, null)
                Log.d(tag, "SMS enviado correctamente a $numero")
            } catch (e: Exception) {
                Log.e(tag, "Error al enviar SMS: ${e.message}")
            }
        }
    }
}