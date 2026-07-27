package com.team.travelmap

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.damyeoom.app.data.database.AppDatabase
import com.damyeoom.app.data.decodePhotos
import com.damyeoom.app.entity.TravelRecord
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class Attraction(
    val name: String,
    val lat: Double,
    val lon: Double
)

val defaultAttractions = listOf(
    Attraction("경복궁", 37.5796, 126.9770),
    Attraction("남산타워", 37.5512, 126.9882)
)

/**
 * 기본 추천 장소 마커
 *
 * 이 마커에는 삭제 기능을 넣지 않는다.
 */
fun addAttractionMarkers(
    naverMap: NaverMap,
    attractions: List<Attraction> = defaultAttractions
) {
    attractions.forEach { spot ->
        Marker().apply {
            position = LatLng(spot.lat, spot.lon)
            captionText = spot.name
            iconTintColor = android.graphics.Color.RED
            map = naverMap
        }
    }
}

/**
 * Room에 저장된 사용자의 여행 기록을 불러와 마커로 표시한다.
 *
 * onRecordClick:
 * 사용자가 자신의 여행 기록 마커를 눌렀을 때
 * 해당 TravelRecord를 HomeScreen에 전달한다.
 */
fun loadPhotoMarkersFromDb(
    naverMap: NaverMap,
    context: Context,
    scope: CoroutineScope,
    onRecordClick: (TravelRecord) -> Unit = {},
    onMarkersLoaded: (List<Marker>) -> Unit = {}
) {
    scope.launch {
        val records = withContext(Dispatchers.IO) {
            AppDatabase
                .getDatabase(context)
                .travelRecordDao()
                .getAllRecords()
        }

        val markers = records.mapNotNull { record ->
            val latitude = record.latitude
            val longitude = record.longitude

            if (latitude == null || longitude == null) {
                null
            } else {
                val firstPhoto = decodePhotos(record.imageUri).firstOrNull()

                addPhotoMarker(
                    naverMap = naverMap,
                    context = context,
                    record = record,
                    latitude = latitude,
                    longitude = longitude,
                    photoUri = firstPhoto,
                    onRecordClick = onRecordClick
                )
            }
        }

        onMarkersLoaded(markers)
    }
}

/**
 * 사용자가 등록한 여행 기록 마커를 생성한다.
 */
private fun addPhotoMarker(
    naverMap: NaverMap,
    context: Context,
    record: TravelRecord,
    latitude: Double,
    longitude: Double,
    photoUri: String?,
    onRecordClick: (TravelRecord) -> Unit
): Marker {
    val marker = Marker().apply {
        position = LatLng(latitude, longitude)

        // 사진이 없을 때도 장소를 알아볼 수 있도록 표시
        captionText = record.placeName.ifBlank { record.region }
    }

    if (!photoUri.isNullOrBlank()) {
        try {
            val bitmap = context.contentResolver
                .openInputStream(Uri.parse(photoUri))
                ?.use { inputStream ->
                    BitmapFactory.decodeStream(inputStream)
                }

            bitmap?.let {
                val resizedBitmap = Bitmap.createScaledBitmap(
                    it,
                    120,
                    120,
                    false
                )

                marker.icon = OverlayImage.fromBitmap(resizedBitmap)
            }
        } catch (e: Exception) {
            android.util.Log.e(
                "MapMarker",
                "마커 사진을 불러오지 못했습니다.",
                e
            )
        }
    }

    // 사용자가 등록한 기록 핀에만 클릭 이벤트가 들어간다.
    marker.setOnClickListener {
        onRecordClick(record)
        true
    }

    marker.map = naverMap
    return marker
}