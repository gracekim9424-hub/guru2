package com.damyeoom.app.ui.screens

import android.os.Bundle
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.damyeoom.app.data.database.AppDatabase
import com.damyeoom.app.data.placeExtras
import com.damyeoom.app.data.resolvePlaceImage
import com.damyeoom.app.entity.PlaceEntity
import com.damyeoom.app.ui.theme.*
import com.naver.maps.map.MapView
import com.team.travelmap.addAttractionMarkers
import com.team.travelmap.loadPhotoMarkersFromDb

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

        // 정적 이미지 대신 실제 네이버 지도 (핀 포함)
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

    AndroidView(modifier = modifier, factory = { mapView })

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> mapView.onCreate(Bundle())
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
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
            // 처음에 한반도 전체가 보이도록 카메라 위치 설정
            naverMap.cameraPosition = com.naver.maps.map.CameraPosition(
                com.naver.maps.geometry.LatLng(36.5, 127.8),
                6.5
            )
            addAttractionMarkers(naverMap)
            loadPhotoMarkersFromDb(naverMap, context, scope)
        }
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