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

// 선택한 장소의 상세 정보를 보여주는 화면
@Composable
fun PlaceDetailScreen(
    placeId: Int,
    onBackClick: () -> Unit = {}
) {
    // Room DB 설정
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }

    // 조회한 장소 정보
    var place by remember {
        mutableStateOf<PlaceEntity?>(null)
    }

    // 장소 ID에 해당하는 정보 조회
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

        // 뒤로가기 아이콘
        Icon(
            Icons.Filled.ArrowBack,
            contentDescription = "뒤로가기",
            tint = TextPrimary,
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(12.dp))
        )

        // 장소 정보 로딩 표시
        if (current == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = ButtonDark
                )
            }

            return@Column
        }

        // 장소별 추가 정보
        val extra = placeExtras[current.placeId]

        Spacer(modifier = Modifier.height(16.dp))

        // 장소 이름
        Text(
            text = current.name,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 지역과 카테고리 표시
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(
                current.region,
                current.category
            ).forEach { tag ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(CardGray)
                        .padding(
                            horizontal = 10.dp,
                            vertical = 5.dp
                        )
                ) {
                    Text(
                        text = tag,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 장소 주소 표시
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.Place,
                contentDescription = null,
                tint = PinRed,
                modifier = Modifier.size(18.dp)
            )

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = current.address,
                fontSize = 14.sp,
                color = TextSecondary
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 장소 대표 이미지 불러오기
        val imageModel = resolvePlaceImage(
            context,
            extra?.detailImageRes ?: current.imageUrl
        )

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

        // 장소 설명
        Text(
            text = current.description,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(20.dp))

        Divider(
            color = Divider,
            thickness = 1.dp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 운영시간, 전화번호, 추천 대상 표시
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            extra?.openingHours?.let {
                InfoRow(
                    label = "운영시간",
                    value = it
                )
            }

            extra?.phoneNumber?.let {
                InfoRow(
                    label = "전화번호",
                    value = it
                )
            }

            extra?.recommendedFor?.let {
                InfoRow(
                    label = "추천",
                    value = it
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// 장소 부가 정보를 한 줄로 표시
@Composable
private fun InfoRow(
    label: String,
    value: String
) {
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