package com.example.mobile_dev_project

import User
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
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
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import android.view.MotionEvent
import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject

@Composable
fun MapScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val markers = remember { mutableStateListOf<Marker>() }
    
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    
    if (auth.currentUser == null) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Je moet ingelogd zijn om de kaart te bekijken")
        }
        return
    }

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

    LaunchedEffect(Unit) {
        db.collection("users")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val user = document.toObject<User>()
                    // Alleen gebruikers met een stad toevoegen
                    if (user.city.isNotEmpty()) {
                        // Bepaal de coördinaten op basis van de stad
                        val geoPoint = when (user.city.lowercase()) {
                            "antwerpen" -> GeoPoint(51.2213, 4.4051)
                            "gent" -> GeoPoint(51.0543, 3.7174)
                            "brussel" -> GeoPoint(50.8503, 4.3517)
                            "mechelen" -> GeoPoint(51.0259, 4.4776)
                            "leuven" -> GeoPoint(50.8798, 4.7005)
                            else -> null
                        }

                        geoPoint?.let { point ->
                            val marker = Marker(mapView).apply {
                                position = point
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                title = user.username
                                snippet = "${user.street} ${user.houseNumber}\n${user.postalCode} ${user.city}"
                            }

                            marker.setOnMarkerClickListener { marker, _ ->
                                marker.showInfoWindow()
                                true
                            }

                            markers.add(marker)
                            mapView.overlays.add(marker)
                        }
                    }
                }
                mapView.invalidate()
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

    AndroidView(
        factory = { mapView },
        modifier = modifier
    )
}

