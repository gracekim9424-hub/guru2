package com.damyeoom.app.ui.screens

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.damyeoom.app.R
import com.damyeoom.app.data.database.AppDatabase
import com.damyeoom.app.data.placeExtras
import com.damyeoom.app.data.resolvePlaceImage
import com.damyeoom.app.entity.PlaceEntity
import com.damyeoom.app.ui.theme.*

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
        // 홈 화면에는 이 6개 장소만 고정으로 보여줍니다 (placeId 기준)
        val featuredIds = listOf(54, 55, 5, 4, 56, 57)
        // 을왕리해수욕장(54), 헤이리 예술마을(55), 한국민속촌(5), 수원화성(4), 광명동굴(56), 화담숲(57)

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

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_south_korea_map),
                contentDescription = "대한민국 지도",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
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