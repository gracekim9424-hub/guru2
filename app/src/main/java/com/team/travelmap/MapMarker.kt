package com.team.travelmap

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.damyeoom.app.data.database.AppDatabase
import com.damyeoom.app.data.decodePhotos
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class Attraction(val name: String, val lat: Double, val lon: Double)

val defaultAttractions = listOf(
    Attraction("경복궁", 37.5796, 126.9770),
    Attraction("남산타워", 37.5512, 126.9882)
)

fun addAttractionMarkers(naverMap: NaverMap, attractions: List<Attraction> = defaultAttractions) {
    attractions.forEach { spot ->
        val marker = Marker()
        marker.position = LatLng(spot.lat, spot.lon)
        marker.captionText = spot.name
        marker.iconTintColor = android.graphics.Color.RED
        marker.map = naverMap
    }
}

fun loadPhotoMarkersFromDb(naverMap: NaverMap, context: Context, scope: CoroutineScope) {
    scope.launch {
        val records = withContext(Dispatchers.IO) {
            AppDatabase.getDatabase(context).travelRecordDao().getAllRecords()
        }
        records.forEach { record ->
            val lat = record.latitude
            val lon = record.longitude
            if (lat != null && lon != null) {
                val firstPhoto = decodePhotos(record.imageUri).firstOrNull()
                addPhotoMarker(naverMap, context, lat, lon, firstPhoto)
            }
        }
    }
}

fun addPhotoMarker(naverMap: NaverMap, context: Context, lat: Double, lon: Double, photoUri: String?) {
    val marker = Marker()
    marker.position = LatLng(lat, lon)

    if (!photoUri.isNullOrEmpty()) {
        try {
            val bitmap = context.contentResolver.openInputStream(Uri.parse(photoUri))?.use {
                BitmapFactory.decodeStream(it)
            }
            bitmap?.let {
                val resized = Bitmap.createScaledBitmap(it, 120, 120, false)
                marker.icon = OverlayImage.fromBitmap(resized)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    marker.map = naverMap
}