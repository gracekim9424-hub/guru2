package com.damyeoom.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.damyeoom.app.data.database.AppDatabase
import com.damyeoom.app.data.koreanRegions
import com.damyeoom.app.entity.PlaceEntity
import com.damyeoom.app.ui.theme.*

// 장소 카테고리 목록
private val categories = listOf(
    "명소",
    "맛집",
    "놀거리"
)

// 지역별 추천 장소 화면
@Composable
fun PlaceRecommendScreen() {
    // Room DB 설정
    val context = LocalContext.current
    val db = remember {
        AppDatabase.getDatabase(context)
    }

    // 선택된 지역과 카테고리
    var selectedRegion by remember {
        mutableStateOf(koreanRegions.first())
    }

    var selectedCategory by remember {
        mutableStateOf(categories.first())
    }

    // 조회한 장소 목록
    var places by remember {
        mutableStateOf(listOf<PlaceEntity>())
    }

    // 선택 조건이 바뀌면 장소 목록 다시 조회
    LaunchedEffect(
        selectedRegion,
        selectedCategory
    ) {
        db.placeDao()
            .getPlacesByRegionAndCategory(
                selectedRegion,
                selectedCategory
            )
            .collect {
                places = it
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgLight)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "지역별 추천 여행지",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 지역 선택 목록
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(koreanRegions) { region ->
                val isSelected =
                    region == selectedRegion

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (isSelected) {
                                ButtonDark
                            } else {
                                CardGray
                            }
                        )
                        .clickable {
                            selectedRegion = region
                        }
                        .padding(
                            horizontal = 16.dp,
                            vertical = 8.dp
                        )
                ) {
                    Text(
                        text = region,
                        fontSize = 13.sp,
                        color = if (isSelected) {
                            Color.White
                        } else {
                            TextPrimary
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 카테고리 선택 버튼
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { category ->
                val isSelected =
                    category == selectedCategory

                Button(
                    onClick = {
                        selectedCategory = category
                    },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor =
                            if (isSelected) {
                                ButtonDark
                            } else {
                                CardGray
                            },
                        contentColor =
                            if (isSelected) {
                                Color.White
                            } else {
                                TextPrimary
                            }
                    )
                ) {
                    Text(
                        text = category,
                        fontSize = 13.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 조회된 장소가 없는 경우
        if (places.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "해당 조건의 여행지가 없습니다.",
                    fontSize = 14.sp,
                    color = TextSecondary
                )
            }
        } else {
            // 추천 장소 목록 표시
            LazyColumn(
                verticalArrangement =
                    Arrangement.spacedBy(14.dp)
            ) {
                items(places) { place ->
                    PlaceCard(place = place)
                }

                item {
                    Spacer(
                        modifier = Modifier.height(20.dp)
                    )
                }
            }
        }
    }
}

// 장소 한 개를 카드 형태로 표시
@Composable
private fun PlaceCard(
    place: PlaceEntity
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardGray)
            .padding(14.dp)
    ) {
        Text(
            text = place.name,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = place.address,
            fontSize = 12.sp,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = place.description,
            fontSize = 13.sp,
            color = TextPrimary
        )
    }
}