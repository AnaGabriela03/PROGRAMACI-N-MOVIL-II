package com.example.divisaprovider

import android.content.*
import android.database.Cursor
import android.database.MatrixCursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.net.Uri
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class  ExchangeRateProvider : ContentProvider() {
    companion object {

        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI("com.example.divisaprovider", "exchange_rates", 1)
        }
    }

    private lateinit var dbHelper: ExchangeRateDbHelper

    override fun onCreate(): Boolean {
        dbHelper = ExchangeRateDbHelper(context!!)
        return true
    }

    private val scope = CoroutineScope(Dispatchers.IO)

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        val deferredResult = CompletableDeferred<Cursor?>()

        when (uriMatcher.match(uri)) {
            1 -> scope.launch {
                try {
                    val db = dbHelper.readableDatabase
                    val resultCursor = db.query("exchange_rates", projection, selection, selectionArgs, null, null, sortOrder)
                    deferredResult.complete(resultCursor)
                } catch (e: Exception) {
                    deferredResult.completeExceptionally(e)
                }
            }
            else -> throw IllegalArgumentException("URI no soportada: $uri")
        }
        return runBlocking { deferredResult.await() }
    }


    override fun getType(uri: Uri): String? {
        return when (uriMatcher.match(uri)) {
            1 -> "vnd.android.cursor.dir/com.example.divisaprovider.exchange_rates"
            else -> throw IllegalArgumentException("URI desconocida: $uri")
        }
    }
    override fun insert(p0: Uri, p1: ContentValues?): Uri? {
        return  null
    }

    override fun delete(p0: Uri, p1: String?, p2: Array<out String>?): Int {
        return  0
    }

    override fun update(p0: Uri, p1: ContentValues?, p2: String?, p3: Array<out String>?): Int {
        return  0
    }
}
