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

// 추천 관광지 좌표 정보
data class Attraction(
    val name: String,
    val lat: Double,
    val lon: Double
)

// 기본 추천 관광지 목록
val defaultAttractions = listOf(
    Attraction(
        "경복궁",
        37.5796,
        126.9770
    ),
    Attraction(
        "남산타워",
        37.5512,
        126.9882
    )
)

// 기본 추천 장소를 지도 마커로 표시
fun addAttractionMarkers(
    naverMap: NaverMap,
    attractions: List<Attraction> =
        defaultAttractions
) {
    attractions.forEach { spot ->
        Marker().apply {
            position =
                LatLng(
                    spot.lat,
                    spot.lon
                )

            captionText = spot.name

            iconTintColor =
                android.graphics.Color.RED

            map = naverMap
        }
    }
}

// Room DB의 여행 기록을 지도 마커로 표시
fun loadPhotoMarkersFromDb(
    naverMap: NaverMap,
    context: Context,
    scope: CoroutineScope,
    onRecordClick: (TravelRecord) -> Unit = {},
    onMarkersLoaded: (List<Marker>) -> Unit = {}
) {
    scope.launch {
        // DB에서 여행 기록 조회
        val records =
            withContext(Dispatchers.IO) {
                AppDatabase
                    .getDatabase(context)
                    .travelRecordDao()
                    .getAllRecords()
            }

        // 좌표가 있는 기록만 마커로 생성
        val markers =
            records.mapNotNull { record ->
                val latitude =
                    record.latitude

                val longitude =
                    record.longitude

                if (
                    latitude == null ||
                    longitude == null
                ) {
                    null
                } else {
                    val firstPhoto =
                        decodePhotos(
                            record.imageUri
                        ).firstOrNull()

                    addPhotoMarker(
                        naverMap = naverMap,
                        context = context,
                        record = record,
                        latitude = latitude,
                        longitude = longitude,
                        photoUri = firstPhoto,
                        onRecordClick =
                            onRecordClick
                    )
                }
            }

        onMarkersLoaded(markers)
    }
}

// 사용자가 등록한 여행 기록 마커 생성
private fun addPhotoMarker(
    naverMap: NaverMap,
    context: Context,
    record: TravelRecord,
    latitude: Double,
    longitude: Double,
    photoUri: String?,
    onRecordClick: (TravelRecord) -> Unit
): Marker {
    val marker =
        Marker().apply {
            position =
                LatLng(
                    latitude,
                    longitude
                )

            // 사진이 없어도 장소명을 표시
            captionText =
                record.placeName.ifBlank {
                    record.region
                }
        }

    // 여행 사진이 있으면 마커 이미지로 사용
    if (!photoUri.isNullOrBlank()) {
        try {
            val bitmap =
                context.contentResolver
                    .openInputStream(
                        Uri.parse(photoUri)
                    )
                    ?.use { inputStream ->
                        BitmapFactory.decodeStream(
                            inputStream
                        )
                    }

            bitmap?.let {
                val resizedBitmap =
                    Bitmap.createScaledBitmap(
                        it,
                        120,
                        120,
                        false
                    )

                marker.icon =
                    OverlayImage.fromBitmap(
                        resizedBitmap
                    )
            }
        } catch (e: Exception) {
            android.util.Log.e(
                "MapMarker",
                "마커 사진을 불러오지 못했습니다.",
                e
            )
        }
    }

    // 여행 기록 마커 클릭 처리
    marker.setOnClickListener {
        onRecordClick(record)
        true
    }

    marker.map = naverMap

    return marker
}