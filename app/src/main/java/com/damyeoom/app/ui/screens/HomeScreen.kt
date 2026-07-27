package com.damyeoom.app.ui.screens

import android.os.Bundle
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import coil.compose.AsyncImage
import com.damyeoom.app.R
import com.damyeoom.app.data.GeocodeRetrofitClient
import com.damyeoom.app.data.database.AppDatabase
import com.damyeoom.app.data.placeExtras
import com.damyeoom.app.data.resolvePlaceImage
import com.damyeoom.app.entity.PlaceEntity
import com.damyeoom.app.ui.theme.*
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraAnimation
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Marker
import com.team.travelmap.addAttractionMarkers
import com.team.travelmap.loadPhotoMarkersFromDb
import com.team.travelmap.weather.DailyForecast
import com.team.travelmap.weather.RetrofitClient as WeatherRetrofitClient
import com.team.travelmap.weather.toDailyForecasts
import kotlinx.coroutines.launch

// TODO: 실제 OpenWeatherMap API 키로 교체 (이미 다른 화면에서 쓰고 있으면 그 값 그대로 재사용)
private const val WEATHER_API_KEY = "ead6912db6b53ee0abad75f8b60d1900"

@Composable
fun HomeScreen(
    onAddTravelClick: () -> Unit,
    onPlaceClick: (Int) -> Unit,
    onSeeMoreClick: () -> Unit
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    var previewPlaces by remember { mutableStateOf(listOf<PlaceEntity>()) }

    LaunchedEffect(Unit) {
        val featuredIds = listOf(54, 55, 5, 4, 56, 57)
        db.placeDao().getAllPlaces().collect { all ->
            previewPlaces = featuredIds.mapNotNull { id -> all.find { it.placeId == id } }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgLight)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_logo_damyeoom),
                contentDescription = "다녀옴! 로고",
                modifier = Modifier.height(34.dp)
            )
            Button(
                onClick = onAddTravelClick,
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ButtonDark)
            ) {
                Text("여행지 추가하기", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        // 정적 이미지 대신 실제 네이버 지도 (핀 포함 + 검색 + 날씨)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(20.dp))
        ) {
            EmbeddedNaverMap(modifier = Modifier.fillMaxSize())
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "주변 여행지를 추천해드릴게요!",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "더보기",
                fontSize = 13.sp,
                color = TextSecondary,
                modifier = Modifier.clickable { onSeeMoreClick() }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.padding(bottom = 20.dp)
        ) {
            items(previewPlaces) { place ->
                PlaceCard(place = place, onClick = { onPlaceClick(place.placeId) })
            }
        }
    }
}

@Composable
private fun EmbeddedNaverMap(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val mapView = remember { MapView(context) }

    // 현재 지도에 찍힌 "내 기록" 마커들을 계속 추적 (다시 그릴 때 먼저 지워야 중첩 방지)
    var naverMapRef by remember { mutableStateOf<NaverMap?>(null) }
    val photoMarkers = remember { mutableStateListOf<Marker>() }

    // 검색 관련 상태
    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var searchErrorMessage by remember { mutableStateOf<String?>(null) }
    var forecasts by remember { mutableStateOf<List<DailyForecast>>(emptyList()) }
    var searchedPlaceName by remember { mutableStateOf<String?>(null) }
    val searchMarker = remember { mutableStateOf<Marker?>(null) }

    fun refreshPhotoMarkers() {
        val map = naverMapRef ?: return
        photoMarkers.forEach { it.map = null }
        photoMarkers.clear()
        loadPhotoMarkersFromDb(map, context, scope) { loaded ->
            photoMarkers.addAll(loaded)
        }
    }

    fun runSearch() {
        val query = searchQuery.trim()
        var map = naverMapRef
        if (query.isBlank() || map == null) return

        isSearching = true
        searchErrorMessage = null
        forecasts = emptyList()

        scope.launch {
            try {
                // 1. 검색어로 좌표 조회
                val geoResponse = GeocodeRetrofitClient.instance.getGeocode(
                    query = query,
                    clientId = GeocodeRetrofitClient.CLIENT_ID,
                    clientSecret = GeocodeRetrofitClient.CLIENT_SECRET
                )
                val address = geoResponse.addresses.firstOrNull()

                if (address == null) {
                    searchErrorMessage = "위치를 찾을 수 없어요. 정확한 지역명으로 검색해보세요."
                    isSearching = false
                    return@launch
                }

                val lat = address.y.toDouble()
                val lng = address.x.toDouble()

                // 2. 지도 카메라 이동 + 검색 위치 마커 표시
                searchMarker.value?.map = null
                val marker = Marker().apply {
                    position = LatLng(lat, lng)
                    captionText = query
                    map = map
                }
                searchMarker.value = marker
                map!!.moveCamera(
                    CameraUpdate.toCameraPosition(CameraPosition(LatLng(lat, lng), 11.0))
                        .animate(CameraAnimation.Easing)
                )
                searchedPlaceName = query

                // 3. 해당 좌표의 날씨 예보(5일/3시간) 조회 후 오늘/내일/모레로 변환
                val forecastResponse = WeatherRetrofitClient.weatherApi.getForecast(
                    lat = lat,
                    lon = lng,
                    apiKey = WEATHER_API_KEY
                )
                forecasts = forecastResponse.toDailyForecasts(days = 3)
            } catch (e: Exception) {
                android.util.Log.e("Weather", "날씨 조회 실패", e)
                searchErrorMessage = "날씨 정보를 불러오지 못했어요."
            } finally {
                isSearching = false
            }
        }
    }

    Box(modifier = modifier) {
        AndroidView(modifier = Modifier.fillMaxSize(), factory = { mapView })

        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_CREATE -> mapView.onCreate(Bundle())
                    Lifecycle.Event.ON_START -> mapView.onStart()
                    Lifecycle.Event.ON_RESUME -> {
                        mapView.onResume()
                        refreshPhotoMarkers()
                    }
                    Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                    Lifecycle.Event.ON_STOP -> mapView.onStop()
                    Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                    else -> {}
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
        }

        LaunchedEffect(mapView) {
            mapView.getMapAsync { naverMap ->
                naverMap.cameraPosition = CameraPosition(LatLng(36.5, 127.8), 6.5)
                addAttractionMarkers(naverMap)
                naverMapRef = naverMap
                refreshPhotoMarkers()
            }
        }

        // 검색창 (지도 위 상단)
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("지역/장소를 검색해보세요") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent,
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White
                    )
                )
                IconButton(onClick = { runSearch() }, enabled = !isSearching) {
                    if (isSearching) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Filled.Search, contentDescription = "검색")
                    }
                }
            }

            searchErrorMessage?.let { msg ->
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(msg, fontSize = 13.sp, color = TextSecondary)
                }
            }
        }

        // 날씨 카드 (지도 위 하단)
        if (forecasts.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val labels = listOf("오늘", "내일", "모레")
                forecasts.forEachIndexed { index, forecast ->
                    WeatherDayCard(
                        modifier = Modifier.weight(1f),
                        label = labels.getOrElse(index) { forecast.date },
                        forecast = forecast
                    )
                }
            }
        }
    }
}

@Composable
private fun WeatherDayCard(modifier: Modifier = Modifier, label: String, forecast: DailyForecast) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .padding(10.dp)
    ) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(modifier = Modifier.height(4.dp))
        Text(forecast.description, fontSize = 11.sp, color = TextSecondary)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "${forecast.minTemp.toInt()}° / ${forecast.maxTemp.toInt()}°",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
    }
}

@Composable
private fun PlaceCard(place: PlaceEntity, onClick: () -> Unit) {
    val context = LocalContext.current
    val extra = placeExtras[place.placeId]
    val imageModel = resolvePlaceImage(context, extra?.cardImageRes ?: place.imageUrl)

    Column(
        modifier = Modifier
            .width(140.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(CardGray)
        ) {
            if (imageModel != null) {
                AsyncImage(
                    model = imageModel,
                    contentDescription = place.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Row(modifier = Modifier.padding(10.dp)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.9f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(text = place.region, fontSize = 11.sp, color = TextPrimary)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.9f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(text = place.category, fontSize = 11.sp, color = TextPrimary)
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = place.name,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
    }
}