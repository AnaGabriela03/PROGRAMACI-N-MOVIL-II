


package com.example.mapaderegreso

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.*
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.*
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.MapEventsOverlay

class MainActivity : ComponentActivity() {
    private val LOCATION_PERMISSION_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Configuration.getInstance().userAgentValue = packageName

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_CODE
            )
        }

        setContent {
            MapaPantalla()
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapaPantalla() {
    val permission = rememberPermissionState(permission = Manifest.permission.ACCESS_FINE_LOCATION)
    val context = LocalContext.current
    val mapView = remember { MapView(context) }

    var ubicacionUsuario by remember { mutableStateOf<GeoPoint?>(null) }
    var ubicacionCasa by remember { mutableStateOf<GeoPoint?>(null) }
    val marcadorSeleccionado = remember { mutableStateOf<Marker?>(null) }
    val coordenadasSeleccionadas = remember { mutableStateOf<GeoPoint?>(null) }
    var lineaRuta by remember { mutableStateOf<Polyline?>(null) }

    fun guardarCasa(context: Context, punto: GeoPoint) {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        prefs.edit().putFloat("home_lat", punto.latitude.toFloat())
            .putFloat("home_lon", punto.longitude.toFloat()).apply()
    }

    fun obtenerCasa(context: Context): GeoPoint? {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val lat = prefs.getFloat("home_lat", 0f)
        val lon = prefs.getFloat("home_lon", 0f)
        return if (lat != 0f && lon != 0f) GeoPoint(lat.toDouble(), lon.toDouble()) else null
    }

    fun dibujarRuta(mapa: MapView, coords: List<List<Double>>) {
        lineaRuta?.let { mapa.overlays.remove(it) }

        if (coords.isEmpty()) {
            Log.e("Ruta", "Lista de coordenadas vacía")
            return
        }

        val ruta = Polyline().apply {
            setPoints(coords.map { GeoPoint(it[1], it[0]) })
            width = 5f
            color = android.graphics.Color.BLUE
        }

        mapa.overlays.add(ruta)
        lineaRuta = ruta
        mapa.invalidate()
    }

    fun solicitarRuta(origen: String, destino: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val api = obtenerRetrofit().create(ApiClient::class.java)
            val respuesta = api.fetchRoute(
                "APIKEY",
                origen,
                destino
            )
            if (respuesta.isSuccessful) {
                val coords = respuesta.body()?.features?.firstOrNull()?.geometry?.coordinates
                Log.d("Ruta", "Coordenadas obtenidas: $coords")
                withContext(Dispatchers.Main) {
                    if (!coords.isNullOrEmpty()) {
                        dibujarRuta(mapView, coords)
                    }
                }
            } else {
                Log.e("Ruta", "Fallo la respuesta: ${respuesta.errorBody()?.string()}")
            }
        }
    }

    LaunchedEffect(permission.status) {
        if (permission.status.isGranted) {
            obtenerUbicacion(context) { ubicacion ->
                mapView.setTileSource(TileSourceFactory.MAPNIK)
                mapView.setMultiTouchControls(true)

                ubicacionUsuario = GeoPoint(ubicacion.latitude, ubicacion.longitude)
                mapView.controller.setZoom(15.0)
                mapView.controller.setCenter(ubicacionUsuario)

                val icono = ContextCompat.getDrawable(context, R.drawable.ubicacion)!!
                val bmp = (icono as BitmapDrawable).bitmap
                val bmpEscalado = Bitmap.createScaledBitmap(bmp, 20, 20, false)
                val iconDrawable = BitmapDrawable(context.resources, bmpEscalado)

                val marcadorUsuario = Marker(mapView).apply {
                    position = ubicacionUsuario
                    icon = iconDrawable
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    title = "Tú"
                }
                mapView.overlays.add(marcadorUsuario)

                ubicacionCasa = obtenerCasa(context)
                ubicacionCasa?.let {
                    val iconoCasa = ContextCompat.getDrawable(context, R.drawable.hogar)!!
                    val bmpCasa = (iconoCasa as BitmapDrawable).bitmap
                    val iconoCasaEscalado = BitmapDrawable(
                        context.resources,
                        Bitmap.createScaledBitmap(bmpCasa, 20, 20, false)
                    )
                    val marcadorCasa = Marker(mapView).apply {
                        position = it
                        icon = iconoCasaEscalado
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        title = "Mi casa"
                    }
                    mapView.overlays.add(marcadorCasa)
                }

                val eventos = object : org.osmdroid.events.MapEventsReceiver {
                    override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                        p?.let {
                            marcadorSeleccionado.value?.let { mapView.overlays.remove(it) }

                            val marcador = Marker(mapView).apply {
                                position = it
                                icon = iconDrawable
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                title = "Marcador"
                            }
                            marcadorSeleccionado.value = marcador
                            coordenadasSeleccionadas.value = it
                            mapView.overlays.add(marcador)
                            mapView.invalidate()
                        }
                        return true
                    }

                    override fun longPressHelper(p: GeoPoint?) = false
                }

                mapView.overlays.add(MapEventsOverlay(eventos))
                mapView.invalidate()
            }
        }
    }

    Column(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize()) {
            AndroidView({ mapView }, modifier = Modifier.fillMaxSize())

            Button(
                onClick = {
                    coordenadasSeleccionadas.value?.let { punto ->
                        guardarCasa(context, punto)
                        ubicacionCasa = punto

                        mapView.overlays.removeAll {
                            it is Marker && it.title == "Mi casa"
                        }

                        val iconoCasa = ContextCompat.getDrawable(context, R.drawable.hogar)!!
                        val bmpCasa = (iconoCasa as BitmapDrawable).bitmap
                        val iconoCasaEscalado = BitmapDrawable(
                            context.resources,
                            Bitmap.createScaledBitmap(bmpCasa, 20, 20, false)
                        )

                        val marcadorCasa = Marker(mapView).apply {
                            position = punto
                            icon = iconoCasaEscalado
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            title = "Mi casa"
                        }

                        mapView.overlays.add(marcadorCasa)
                        mapView.invalidate()

                        if (ubicacionUsuario != null) {
                            val startStr = "${ubicacionUsuario!!.longitude},${ubicacionUsuario!!.latitude}"
                            val endStr = "${punto.longitude},${punto.latitude}"
                            solicitarRuta(startStr, endStr)
                        }
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(70.dp)
            ) {
                Text("Nueva casa")
            }

            FloatingActionButton(
                onClick = {
                    ubicacionUsuario?.let {
                        mapView.controller.setCenter(it)
                        mapView.controller.setZoom(17.0)
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.MyLocation,
                    contentDescription = "Ubicación actual"
                )
            }
        }
    }
}

@SuppressLint("MissingPermission")
fun obtenerUbicacion(context: Context, callback: (Location) -> Unit) {
    val cliente = LocationServices.getFusedLocationProviderClient(context)
    cliente.lastLocation
        .addOnSuccessListener { location ->
            location?.let { callback(it) }
        }
        .addOnFailureListener {
            Log.e("Ubicación", "No se pudo obtener la ubicación", it)
        }
}

fun obtenerRetrofit() = retrofit2.Retrofit.Builder()
    .baseUrl("https://api.openrouteservice.org/")
    .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
    .build()
