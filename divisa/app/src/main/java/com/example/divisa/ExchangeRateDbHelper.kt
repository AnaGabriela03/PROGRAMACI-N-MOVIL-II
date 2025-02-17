package com.example.divisa

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.util.concurrent.TimeUnit

class ExchangeRateDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "exchange_rates.db"
        private const val DATABASE_VERSION = 1
        const val TABLE_NAME = "rates"
        const val COLUMN_CURRENCY = "currency"
        const val COLUMN_RATE = "rate"
        const val COLUMN_TIMESTAMP = "timestamp"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTableQuery = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_CURRENCY TEXT,
                $COLUMN_RATE REAL,
                $COLUMN_TIMESTAMP INTEGER
            )
        """.trimIndent()
        db?.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        val dropTableQuery = "DROP TABLE IF EXISTS $TABLE_NAME"
        db?.execSQL(dropTableQuery)
        onCreate(db)
    }


    // Insertar tasas y guardar el timestamp de la última actualización
    fun insertRates(rates: Map<String, Double>, timestamp: Long) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            rates.forEach { (currency, rate) ->
                val values = ContentValues().apply {
                    put(COLUMN_CURRENCY, currency)
                    put(COLUMN_RATE, rate)
                    put(COLUMN_TIMESTAMP, timestamp)
                }
                db.insert(TABLE_NAME, null, values)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    // Obtener las tasas de cambio desde la base de datos
    fun getRates(): List<Pair<String, Double>> {
        val db = readableDatabase
        val rates = mutableListOf<Pair<String, Double>>()
        val cursor: Cursor? = db.query(
            TABLE_NAME,
            arrayOf(COLUMN_CURRENCY, COLUMN_RATE),
            null, null, null, null,
            "$COLUMN_TIMESTAMP DESC"
        )

        cursor?.use {
            while (it.moveToNext()) {
                val currencyColumnIndex = it.getColumnIndex(COLUMN_CURRENCY)
                val rateColumnIndex = it.getColumnIndex(COLUMN_RATE)

                if (currencyColumnIndex != -1 && rateColumnIndex != -1) {
                    val currency = it.getString(currencyColumnIndex)
                    val rate = it.getDouble(rateColumnIndex)
                    rates.add(currency to rate)
                }
            }
        }
        return rates
    }

    // Obtener la última hora de actualización
    fun getLastUpdateTime(): Long? {
        val db = readableDatabase
        val query = "SELECT MAX($COLUMN_TIMESTAMP) FROM $TABLE_NAME"
        val cursor: Cursor? = db.rawQuery(query, null)
        var lastUpdateTime: Long? = null

        cursor?.use {
            if (it.moveToFirst()) {
                lastUpdateTime = it.getLong(0) // Obtiene el timestamp más reciente
            }
        }
        return lastUpdateTime
    }

    // Calcular la siguiente hora de actualización
    fun getNextUpdateTime(lastUpdateTime: Long?): Long? {
        return lastUpdateTime?.plus(TimeUnit.HOURS.toMillis(1)) // Sumar 1 hora al último timestamp
    }
}
