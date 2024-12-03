package com.example.mobile_dev_project

import User
import android.content.Context
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import org.osmdroid.views.overlay.Marker
import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import android.util.Log
import org.osmdroid.views.overlay.Polygon
import kotlin.math.cos
import kotlin.math.sin
import android.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color as ComposeColor

@Composable
fun MapScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val markers = remember { mutableStateListOf<Marker>() }
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    var radius by remember { mutableStateOf(5000.0) } // 5km default

    Configuration.getInstance().load(context, context.getSharedPreferences("osm", Context.MODE_PRIVATE))

    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(13.0)
            val antwerpen = GeoPoint(51.2213, 4.4051)
            controller.setCenter(antwerpen)
        }
    }

    // Voeg de radius cirkel toe
    val circle = remember {
        Polygon().apply {
            points = createCirclePoints(GeoPoint(51.2213, 4.4051), radius)
            fillPaint.color = Color.argb(50, 0, 255, 0) // Licht groene vulling
            outlinePaint.color = Color.rgb(0, 200, 0)   // Donkerder groene rand
            outlinePaint.strokeWidth = 2f
        }
    }

    LaunchedEffect(Unit) {
        mapView.overlays.add(circle)  // Voeg cirkel toe voor de markers
        db.collection("users")
            .get()
            .addOnSuccessListener { result ->
                Log.d("MapScreen", "Aantal gebruikers gevonden: ${result.size()}")
                for (document in result) {
                    val user = document.toObject<User>()
                    Log.d("MapScreen", "User: ${user.username}, City: ${user.city}")
                    // Alleen gebruikers met een stad toevoegen
                    if (user.city.isNotEmpty()) {
                        Log.d("MapScreen", "Adding marker for user: ${user.username} in ${user.city}")
                        // Bepaal de coördinaten op basis van de stad
                        val geoPoint = when (user.city.lowercase().trim()) {
                            "antwerpen", "deurne" -> {
                                // Verschillende locaties voor Antwerpen en deelgemeenten
                                when (user.city.lowercase().trim()) {
                                    "deurne" -> GeoPoint(51.2304, 4.4621)  // Deurne coördinaten
                                    else -> GeoPoint(51.2213 + (Math.random() - 0.5) * 0.01, 
                                                   4.4051 + (Math.random() - 0.5) * 0.01)  // Random spreiding rond Antwerpen
                                }
                            }
                            "gent" -> GeoPoint(51.0543 + (Math.random() - 0.5) * 0.01, 
                                              3.7174 + (Math.random() - 0.5) * 0.01)
                            "brussel" -> GeoPoint(50.8503 + (Math.random() - 0.5) * 0.01, 
                                                 4.3517 + (Math.random() - 0.5) * 0.01)
                            "mechelen" -> GeoPoint(51.0259 + (Math.random() - 0.5) * 0.01, 
                                                  4.4776 + (Math.random() - 0.5) * 0.01)
                            "leuven" -> GeoPoint(50.8798 + (Math.random() - 0.5) * 0.01, 
                                                4.7005 + (Math.random() - 0.5) * 0.01)
                            else -> {
                                Log.d("MapScreen", "Onbekende stad: ${user.city}")
                                null
                            }
                        }

                        geoPoint?.let { point ->
                            val marker = Marker(mapView).apply {
                                position = point
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                title = user.username
                                snippet = "${user.street} ${user.houseNumber}\n${user.postalCode} ${user.city}"
                            }
                            markers.add(marker)
                            mapView.overlays.add(marker)
                            Log.d("MapScreen", "Marker added for ${user.username}")
                        }
                    } else {
                        Log.d("MapScreen", "Skipping user ${user.username} - no city")
                    }
                }
                mapView.invalidate()
            }
            .addOnFailureListener { e ->
                Log.e("MapScreen", "Error loading users", e)
            }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { mapView },
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .background(ComposeColor.White.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                .padding(16.dp)
        ) {
            Text("Radius: ${(radius/1000).toInt()}km")
            Slider(
                value = radius.toFloat(),
                onValueChange = { 
                    radius = it.toDouble()
                    circle.points = createCirclePoints(GeoPoint(51.2213, 4.4051), radius)
                    mapView.invalidate()
                },
                valueRange = 1000f..20000f, // 1km tot 20km
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}

// Helper functie om cirkel punten te maken
private fun createCirclePoints(center: GeoPoint, radius: Double): ArrayList<GeoPoint> {
    val points = ArrayList<GeoPoint>()
    val numberOfPoints = 60 // Aantal punten voor de cirkel
    
    for (i in 0 until numberOfPoints) {
        val angle = Math.PI * 2 * i / numberOfPoints
        val dx = radius * cos(angle)
        val dy = radius * sin(angle)
        
        // Convert dx/dy to lat/lon offsets (approximation)
        val latOffset = dy / 111320.0 // 1 degree = 111.32km
        val lonOffset = dx / (111320.0 * cos(Math.toRadians(center.latitude)))
        
        points.add(GeoPoint(
            center.latitude + latOffset,
            center.longitude + lonOffset
        ))
    }
    return points
}

