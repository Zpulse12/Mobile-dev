package com.example.mobile_dev_project

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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

@Composable
fun MapScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    val markers = remember { mutableStateListOf<Marker>() }
    
    Configuration.getInstance().load(context, context.getSharedPreferences("osm", Context.MODE_PRIVATE))
    
    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(13.0)
            
            val antwerpen = GeoPoint(51.2213, 4.4051)
            controller.setCenter(antwerpen)

            overlays.add(object : org.osmdroid.views.overlay.Overlay() {
                override fun onSingleTapConfirmed(e: MotionEvent, mapView: MapView): Boolean {
                    val projection = mapView.projection
                    val tapPoint = projection.fromPixels(e.x.toInt(), e.y.toInt())
                    val geoPoint = GeoPoint(tapPoint.latitude, tapPoint.longitude)
                    
                    val newMarker = Marker(mapView).apply {
                        position = geoPoint
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        title = "Locatie"
                        snippet = "Lat: ${geoPoint.latitude}\nLon: ${geoPoint.longitude}"
                    }
                    
                    newMarker.setOnMarkerClickListener { marker, _ ->
                        marker.showInfoWindow()
                        true
                    }
                    
                    markers.add(newMarker)
                    mapView.overlays.add(newMarker)
                    mapView.invalidate()
                    
                    return true
                }
            })
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

