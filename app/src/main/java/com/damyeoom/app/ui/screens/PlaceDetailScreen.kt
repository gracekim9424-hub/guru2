package com.damyeoom.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.damyeoom.app.data.database.AppDatabase
import com.damyeoom.app.data.placeExtras
import com.damyeoom.app.data.resolvePlaceImage
import com.damyeoom.app.entity.PlaceEntity
import com.damyeoom.app.ui.theme.*

@Composable
fun PlaceDetailScreen(
    placeId: Int,
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    var place by remember { mutableStateOf<PlaceEntity?>(null) }

    LaunchedEffect(placeId) {
        place = db.placeDao().getPlaceById(placeId)
    }

    val current = place

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgLight)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Icon(
            Icons.Filled.ArrowBack,
            contentDescription = "뒤로가기",
            tint = TextPrimary,
            modifier = Modifier.size(24.dp).clip(RoundedCornerShape(12.dp))
        )

        if (current == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = ButtonDark)
            }
            return@Column
        }

        val extra = placeExtras[current.placeId]

        Spacer(modifier = Modifier.height(16.dp))

        // 장소명 (더 크게)
        Text(
            text = current.name,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 지역/카테고리 칩
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(current.region, current.category).forEach { tag ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(CardGray)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(tag, fontSize = 12.sp, color = TextSecondary)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 주소
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Place, contentDescription = null, tint = PinRed, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(current.address, fontSize = 14.sp, color = TextSecondary)
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 대표 사진 (더 크게)
        val imageModel = resolvePlaceImage(context, extra?.detailImageRes ?: current.imageUrl)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(CardGray)
        ) {
            if (imageModel != null) {
                AsyncImage(
                    model = imageModel,
                    contentDescription = current.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 설명 (글자 크게, 줄 제한 없이 전체 표시)
        Text(
            text = current.description,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(20.dp))

        Divider(color = Divider, thickness = 1.dp)

        Spacer(modifier = Modifier.height(16.dp))

        // 부가 설명
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            extra?.openingHours?.let {
                InfoRow(label = "운영시간", value = it)
            }
            extra?.phoneNumber?.let {
                InfoRow(label = "전화번호", value = it)
            }
            extra?.recommendedFor?.let {
                InfoRow(label = "추천", value = it)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row {
        Text(
            text = label,
            fontSize = 13.sp,
            color = TextSecondary,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(76.dp)
        )
        Text(
            text = value,
            fontSize = 13.sp,
            color = TextSecondary
        )
    }
}