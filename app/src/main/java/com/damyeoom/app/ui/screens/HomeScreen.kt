package com.damyeoom.app.ui.screens

import android.net.Uri
import android.os.Bundle
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
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
import com.damyeoom.app.data.decodePhotos
import com.damyeoom.app.data.placeExtras
import com.damyeoom.app.data.resolvePlaceImage
import com.damyeoom.app.entity.PlaceEntity
import com.damyeoom.app.entity.TravelRecord
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// 날씨 API 인증 키
private const val WEATHER_API_KEY =
    "ead6912db6b53ee0abad75f8b60d1900"

// 앱의 메인 홈 화면
@Composable
fun HomeScreen(
    onAddTravelClick: () -> Unit,
    onPlaceClick: (Int) -> Unit,
    onSeeMoreClick: () -> Unit
) {
    // Room DB 설정
    val context = LocalContext.current
    val db = remember {
        AppDatabase.getDatabase(context)
    }

    // 홈에 표시할 추천 장소
    var previewPlaces by remember {
        mutableStateOf(
            listOf<PlaceEntity>()
        )
    }

    // 추천 장소를 DB에서 조회
    LaunchedEffect(Unit) {
        val featuredIds =
            listOf(54, 55, 5, 4, 56, 57)

        db.placeDao()
            .getAllPlaces()
            .collect { allPlaces ->
                previewPlaces =
                    featuredIds.mapNotNull { id ->
                        allPlaces.find { place ->
                            place.placeId == id
                        }
                    }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgLight)
    ) {
        // 상단 로고와 여행지 추가 버튼
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 20.dp,
                    vertical = 20.dp
                ),
            verticalAlignment =
                Alignment.CenterVertically,
            horizontalArrangement =
                Arrangement.SpaceBetween
        ) {
            Image(
                painter = painterResource(
                    id =
                        R.drawable
                            .ic_logo_damyeoom
                ),
                contentDescription =
                    "다녀옴! 로고",
                modifier =
                    Modifier.height(34.dp)
            )

            Button(
                onClick = onAddTravelClick,
                shape =
                    RoundedCornerShape(24.dp),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor =
                            ButtonDark
                    )
            ) {
                Text(
                    text = "여행지 추가하기",
                    fontSize = 14.sp,
                    fontWeight =
                        FontWeight.SemiBold
                )
            }
        }

        // 네이버 지도 영역
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 20.dp)
                .clip(
                    RoundedCornerShape(20.dp)
                )
        ) {
            EmbeddedNaverMap(
                modifier =
                    Modifier.fillMaxSize()
            )
        }

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        // 추천 여행지 제목
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement =
                Arrangement.SpaceBetween,
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Text(
                text =
                    "주변 여행지를 추천해드릴게요!",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            // 추천 장소 전체 화면 이동
            Text(
                text = "더보기",
                fontSize = 13.sp,
                color = TextSecondary,
                modifier = Modifier.clickable {
                    onSeeMoreClick()
                }
            )
        }

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        // 추천 장소 카드 목록
        LazyRow(
            contentPadding =
                PaddingValues(
                    horizontal = 20.dp
                ),
            horizontalArrangement =
                Arrangement.spacedBy(14.dp),
            modifier =
                Modifier.padding(bottom = 20.dp)
        ) {
            items(previewPlaces) { place ->
                PlaceCard(
                    place = place,
                    onClick = {
                        onPlaceClick(
                            place.placeId
                        )
                    }
                )
            }
        }
    }
}

// 홈 화면에 네이버 지도를 표시
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EmbeddedNaverMap(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner =
        LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    // 네이버 MapView 객체
    val mapView = remember {
        MapView(context)
    }

    // 불러온 네이버 지도 객체
    var naverMapRef by remember {
        mutableStateOf<NaverMap?>(null)
    }

    // 여행 사진 마커 목록
    val photoMarkers = remember {
        mutableStateListOf<Marker>()
    }

    // 선택된 여행 기록
    var selectedRecord by remember {
        mutableStateOf<TravelRecord?>(null)
    }

    // 여행 기록 삭제 상태
    var showDeleteConfirmDialog by remember {
        mutableStateOf(false)
    }
    var isDeletingRecord by remember {
        mutableStateOf(false)
    }

    // 장소 검색 상태
    var searchQuery by remember {
        mutableStateOf("")
    }
    var isSearching by remember {
        mutableStateOf(false)
    }
    var searchErrorMessage by remember {
        mutableStateOf<String?>(null)
    }

    // 검색 지역의 날씨 정보
    var forecasts by remember {
        mutableStateOf<List<DailyForecast>>(
            emptyList()
        )
    }

    // 검색 위치 마커
    val searchMarker = remember {
        mutableStateOf<Marker?>(null)
    }

    // DB에 저장된 여행 기록 마커 갱신
    fun refreshPhotoMarkers() {
        val map =
            naverMapRef ?: return

        photoMarkers.forEach { marker ->
            marker.map = null
        }

        photoMarkers.clear()

        loadPhotoMarkersFromDb(
            naverMap = map,
            context = context,
            scope = scope,
            onRecordClick = { record ->
                selectedRecord = record
            },
            onMarkersLoaded = {
                    loadedMarkers ->
                photoMarkers.addAll(
                    loadedMarkers
                )
            }
        )
    }

    // 검색한 장소의 위치와 날씨 조회
    fun runSearch() {
        val query =
            searchQuery.trim()
        val map =
            naverMapRef

        if (
            query.isBlank() ||
            map == null
        ) {
            return
        }

        isSearching = true
        searchErrorMessage = null
        forecasts = emptyList()

        scope.launch {
            try {
                // 검색어를 좌표로 변환
                val geoResponse =
                    GeocodeRetrofitClient
                        .instance
                        .getGeocode(
                            query = query,
                            clientId =
                                GeocodeRetrofitClient
                                    .CLIENT_ID,
                            clientSecret =
                                GeocodeRetrofitClient
                                    .CLIENT_SECRET
                        )

                val address =
                    geoResponse
                        .addresses
                        .firstOrNull()

                if (address == null) {
                    searchErrorMessage =
                        "위치를 찾을 수 없어요. 정확한 지역명으로 검색해보세요."
                    return@launch
                }

                val latitude =
                    address.y.toDoubleOrNull()

                val longitude =
                    address.x.toDoubleOrNull()

                if (
                    latitude == null ||
                    longitude == null
                ) {
                    searchErrorMessage =
                        "위치 좌표를 불러오지 못했어요."
                    return@launch
                }

                // 기존 검색 마커 제거
                searchMarker.value?.map =
                    null

                // 검색 위치 마커 표시
                val marker =
                    Marker().apply {
                        position = LatLng(
                            latitude,
                            longitude
                        )
                        captionText = query
                        this.map = map
                    }

                searchMarker.value =
                    marker

                // 검색 위치로 지도 이동
                map.moveCamera(
                    CameraUpdate
                        .toCameraPosition(
                            CameraPosition(
                                LatLng(
                                    latitude,
                                    longitude
                                ),
                                11.0
                            )
                        )
                        .animate(
                            CameraAnimation.Easing
                        )
                )

                // 검색 지역의 3일 날씨 조회
                val forecastResponse =
                    WeatherRetrofitClient
                        .weatherApi
                        .getForecast(
                            lat = latitude,
                            lon = longitude,
                            apiKey =
                                WEATHER_API_KEY
                        )

                forecasts =
                    forecastResponse
                        .toDailyForecasts(
                            days = 3
                        )
            } catch (e: Exception) {
                android.util.Log.e(
                    "Weather",
                    "지역 검색 또는 날씨 조회 실패",
                    e
                )

                searchErrorMessage =
                    "위치 또는 날씨 정보를 불러오지 못했어요."
            } finally {
                isSearching = false
            }
        }
    }

    Box(
        modifier = modifier
    ) {
        // Compose 안에 네이버 MapView 표시
        AndroidView(
            modifier =
                Modifier.fillMaxSize(),
            factory = {
                mapView
            }
        )

        // Compose 생명주기와 MapView 연결
        DisposableEffect(
            lifecycleOwner
        ) {
            val observer =
                LifecycleEventObserver {
                        _,
                        event ->

                    when (event) {
                        Lifecycle.Event.ON_CREATE -> {
                            mapView.onCreate(
                                Bundle()
                            )
                        }

                        Lifecycle.Event.ON_START -> {
                            mapView.onStart()
                        }

                        Lifecycle.Event.ON_RESUME -> {
                            mapView.onResume()
                            refreshPhotoMarkers()
                        }

                        Lifecycle.Event.ON_PAUSE -> {
                            mapView.onPause()
                        }

                        Lifecycle.Event.ON_STOP -> {
                            mapView.onStop()
                        }

                        Lifecycle.Event.ON_DESTROY -> {
                            mapView.onDestroy()
                        }

                        else -> Unit
                    }
                }

            lifecycleOwner.lifecycle
                .addObserver(observer)

            onDispose {
                lifecycleOwner.lifecycle
                    .removeObserver(observer)
            }
        }

        // 네이버 지도 초기 설정
        LaunchedEffect(mapView) {
            mapView.getMapAsync { naverMap ->
                // 대한민국 중심으로 카메라 설정
                naverMap.cameraPosition =
                    CameraPosition(
                        LatLng(
                            36.5,
                            127.8
                        ),
                        6.5
                    )

                // 주요 관광지 마커 표시
                addAttractionMarkers(
                    naverMap
                )

                naverMapRef = naverMap

                // 저장된 여행 기록 마커 표시
                refreshPhotoMarkers()
            }
        }

        // 지도 위 검색창
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(
                        RoundedCornerShape(
                            16.dp
                        )
                    )
                    .background(Color.White)
                    .padding(
                        horizontal = 12.dp,
                        vertical = 4.dp
                    ),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                    },
                    placeholder = {
                        Text(
                            "지역/장소를 검색해보세요"
                        )
                    },
                    singleLine = true,
                    modifier =
                        Modifier.weight(1f),
                    colors =
                        OutlinedTextFieldDefaults
                            .colors(
                                unfocusedBorderColor =
                                    Color.Transparent,
                                focusedBorderColor =
                                    Color.Transparent,
                                unfocusedContainerColor =
                                    Color.White,
                                focusedContainerColor =
                                    Color.White
                            )
                )

                // 장소 검색 버튼
                IconButton(
                    onClick = {
                        runSearch()
                    },
                    enabled = !isSearching
                ) {
                    if (isSearching) {
                        CircularProgressIndicator(
                            modifier =
                                Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector =
                                Icons.Filled.Search,
                            contentDescription =
                                "검색"
                        )
                    }
                }
            }

            // 검색 오류 메시지
            searchErrorMessage?.let {
                    message ->
                Spacer(
                    modifier =
                        Modifier.height(8.dp)
                )

                Box(
                    modifier = Modifier
                        .clip(
                            RoundedCornerShape(
                                12.dp
                            )
                        )
                        .background(Color.White)
                        .padding(
                            horizontal = 14.dp,
                            vertical = 10.dp
                        )
                ) {
                    Text(
                        text = message,
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }
            }
        }

        // 검색 지역의 3일 날씨 표시
        if (forecasts.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .align(
                        Alignment.BottomCenter
                    )
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {
                val labels =
                    listOf(
                        "오늘",
                        "내일",
                        "모레"
                    )

                forecasts.forEachIndexed {
                        index,
                        forecast ->

                    WeatherDayCard(
                        modifier =
                            Modifier.weight(1f),
                        label =
                            labels.getOrElse(
                                index
                            ) {
                                forecast.date
                            },
                        forecast = forecast
                    )
                }
            }
        }
    }

    // 여행 기록 마커 선택 시 상세창 표시
    selectedRecord?.let { record ->
        ModalBottomSheet(
            onDismissRequest = {
                if (!isDeletingRecord) {
                    selectedRecord = null
                    showDeleteConfirmDialog =
                        false
                }
            },
            containerColor = BgLight,
            dragHandle = {
                BottomSheetDefaults
                    .DragHandle()
            }
        ) {
            TravelRecordBottomSheet(
                record = record,
                isDeleting =
                    isDeletingRecord,
                onDeleteClick = {
                    showDeleteConfirmDialog =
                        true
                },
                onCloseClick = {
                    selectedRecord = null
                    showDeleteConfirmDialog =
                        false
                }
            )
        }
    }

    // 여행 기록 삭제 확인창
    if (showDeleteConfirmDialog) {
        val recordToDelete =
            selectedRecord

        AlertDialog(
            onDismissRequest = {
                if (!isDeletingRecord) {
                    showDeleteConfirmDialog =
                        false
                }
            },
            title = {
                Text(
                    text =
                        "여행 기록을 삭제할까요?",
                    fontWeight =
                        FontWeight.Bold
                )
            },
            text = {
                Column {
                    val displayName =
                        recordToDelete
                            ?.placeName
                            ?.takeIf {
                                it.isNotBlank()
                            }
                            ?: recordToDelete
                                ?.region
                            ?: "선택한 여행 기록"

                    Text(
                        text = displayName,
                        fontWeight =
                            FontWeight.SemiBold
                    )

                    Spacer(
                        modifier =
                            Modifier.height(8.dp)
                    )

                    Text(
                        text =
                            "삭제하면 지도 핀과 여행 기록이 함께 삭제되며 복구할 수 없습니다.",
                        color = TextSecondary
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog =
                            false
                    },
                    enabled =
                        !isDeletingRecord
                ) {
                    Text("취소")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val targetRecord =
                            recordToDelete
                                ?: return@TextButton

                        if (isDeletingRecord) {
                            return@TextButton
                        }

                        isDeletingRecord = true

                        scope.launch {
                            try {
                                // 여행 기록 DB 삭제
                                withContext(
                                    Dispatchers.IO
                                ) {
                                    AppDatabase
                                        .getDatabase(
                                            context
                                        )
                                        .travelRecordDao()
                                        .delete(
                                            targetRecord
                                        )
                                }

                                showDeleteConfirmDialog =
                                    false
                                selectedRecord =
                                    null

                                // 지도 마커 다시 조회
                                refreshPhotoMarkers()
                            } catch (
                                e: Exception
                            ) {
                                android.util.Log.e(
                                    "TravelDelete",
                                    "여행 기록 삭제 실패",
                                    e
                                )
                            } finally {
                                isDeletingRecord =
                                    false
                            }
                        }
                    },
                    enabled =
                        !isDeletingRecord
                ) {
                    if (isDeletingRecord) {
                        CircularProgressIndicator(
                            modifier =
                                Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "삭제",
                            color = PinRed,
                            fontWeight =
                                FontWeight.Bold
                        )
                    }
                }
            }
        )
    }
}

// 선택한 여행 기록의 상세 정보창
@Composable
private fun TravelRecordBottomSheet(
    record: TravelRecord,
    isDeleting: Boolean,
    onDeleteClick: () -> Unit,
    onCloseClick: () -> Unit
) {
    // 저장된 첫 번째 사진 가져오기
    val firstPhoto =
        remember(record.imageUri) {
            decodePhotos(
                record.imageUri
            ).firstOrNull()
        }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 28.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment =
                Alignment.Top
        ) {
            // 여행 대표 사진
            if (!firstPhoto.isNullOrBlank()) {
                AsyncImage(
                    model =
                        Uri.parse(firstPhoto),
                    contentDescription =
                        "여행 기록 사진",
                    contentScale =
                        ContentScale.Crop,
                    modifier = Modifier
                        .size(88.dp)
                        .clip(
                            RoundedCornerShape(
                                16.dp
                            )
                        )
                )

                Spacer(
                    modifier =
                        Modifier.width(14.dp)
                )
            }

            // 장소명과 방문 날짜
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text =
                        record.placeName
                            .ifBlank {
                                record.region
                            },
                    fontSize = 20.sp,
                    fontWeight =
                        FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(
                    modifier =
                        Modifier.height(5.dp)
                )

                Text(
                    text =
                        "${record.region} · ${record.visitDate}",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }
        }

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        Text(
            text = "여행 한줄평",
            fontSize = 13.sp,
            color = TextSecondary
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        // 여행 메모
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(16.dp)
                )
                .background(CardGray)
                .padding(16.dp)
        ) {
            Text(
                text = record.memo.ifBlank {
                    "작성한 여행 기록이 없습니다."
                },
                fontSize = 15.sp,
                color = TextPrimary,
                lineHeight = 22.sp
            )
        }

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        // 여행 기록 삭제 버튼
        Button(
            onClick = onDeleteClick,
            enabled = !isDeleting,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape =
                RoundedCornerShape(16.dp),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = PinRed,
                    disabledContainerColor =
                        PinRed.copy(
                            alpha = 0.5f
                        )
                )
        ) {
            Icon(
                imageVector =
                    Icons.Filled.Delete,
                contentDescription = null
            )

            Spacer(
                modifier = Modifier.width(8.dp)
            )

            Text(
                text = "여행 기록 삭제",
                fontSize = 15.sp,
                fontWeight =
                    FontWeight.Bold
            )
        }

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        // 상세창 닫기
        TextButton(
            onClick = onCloseClick,
            enabled = !isDeleting,
            modifier =
                Modifier.fillMaxWidth()
        ) {
            Text(
                text = "닫기",
                color = TextSecondary
            )
        }
    }
}

// 날짜별 날씨 카드
@Composable
private fun WeatherDayCard(
    modifier: Modifier = Modifier,
    label: String,
    forecast: DailyForecast
) {
    Column(
        modifier = modifier
            .clip(
                RoundedCornerShape(14.dp)
            )
            .background(Color.White)
            .padding(10.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(
            modifier = Modifier.height(4.dp)
        )

        Text(
            text = forecast.description,
            fontSize = 11.sp,
            color = TextSecondary
        )

        Spacer(
            modifier = Modifier.height(4.dp)
        )

        Text(
            text =
                "${forecast.minTemp.toInt()}° / ${forecast.maxTemp.toInt()}°",
            fontSize = 13.sp,
            fontWeight =
                FontWeight.SemiBold,
            color = TextPrimary
        )
    }
}

// 홈 화면 추천 장소 카드
@Composable
private fun PlaceCard(
    place: PlaceEntity,
    onClick: () -> Unit
) {
    val context = LocalContext.current

    // 장소별 추가 정보
    val extra =
        placeExtras[place.placeId]

    // 인터넷 또는 로컬 이미지 불러오기
    val imageModel =
        resolvePlaceImage(
            context,
            extra?.cardImageRes
                ?: place.imageUrl
        )

    Column(
        modifier = Modifier
            .width(140.dp)
            .clickable {
                onClick()
            }
    ) {
        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(
                    RoundedCornerShape(18.dp)
                )
                .background(CardGray)
        ) {
            // 장소 대표 이미지
            if (imageModel != null) {
                AsyncImage(
                    model = imageModel,
                    contentDescription =
                        place.name,
                    contentScale =
                        ContentScale.Crop,
                    modifier =
                        Modifier.fillMaxSize()
                )
            }

            // 지역 및 카테고리 표시
            Row(
                modifier =
                    Modifier.padding(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(
                            RoundedCornerShape(
                                8.dp
                            )
                        )
                        .background(
                            Color.White.copy(
                                alpha = 0.9f
                            )
                        )
                        .padding(
                            horizontal = 8.dp,
                            vertical = 4.dp
                        )
                ) {
                    Text(
                        text = place.region,
                        fontSize = 11.sp,
                        color = TextPrimary
                    )
                }

                Spacer(
                    modifier =
                        Modifier.width(6.dp)
                )

                Box(
                    modifier = Modifier
                        .clip(
                            RoundedCornerShape(
                                8.dp
                            )
                        )
                        .background(
                            Color.White.copy(
                                alpha = 0.9f
                            )
                        )
                        .padding(
                            horizontal = 8.dp,
                            vertical = 4.dp
                        )
                ) {
                    Text(
                        text = place.category,
                        fontSize = 11.sp,
                        color = TextPrimary
                    )
                }
            }
        }

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        // 장소 이름
        Text(
            text = place.name,
            fontSize = 14.sp,
            fontWeight =
                FontWeight.SemiBold,
            color = TextPrimary
        )
    }
}