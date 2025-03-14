package com.example.divisaprovider

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class ExchangeRateDbHelper(context: Context) :
    SQLiteOpenHelper(context, "exchange_rates.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(
            "CREATE TABLE exchange_rates (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +  // 🔹 Se usa ID autoincremental para historial
                    "currency TEXT," +
                    "rate REAL," +
                    "timestamp INTEGER)" // 🔹 Se guarda cada actualización con timestamp único
        )
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS exchange_rates")
        onCreate(db)
    }

    fun insertExchangeRates(rates: Map<String, Double>, timestamp: Long) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            rates.forEach { (currency, rate) ->
                val values = ContentValues().apply {
                    put("currency", currency)
                    put("rate", rate)
                    put("timestamp", timestamp) // 🔹 Se guarda el tiempo de actualización como un nuevo registro
                }
                db.insert("exchange_rates", null, values) // 🔹 No se sobrescriben registros, se añaden nuevos
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }
}
