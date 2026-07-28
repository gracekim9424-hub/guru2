package com.damyeoom.app.data

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.damyeoom.app.R

// 추천 장소 화면에 사용하는 데이터
data class RecommendedPlace(
    val name: String,
    val tags: List<String>,
    @DrawableRes val imageRes: Int
)

// 친구 정보에 사용하는 데이터
data class Friend(
    val name: String,
    val color: Color,
    val alreadyAdded: Boolean = false
)

// 여행 게시글 화면에 사용하는 데이터
data class TravelPost(
    val authorName: String,
    val content: String,
    val timestamp: String
)

// 추천 장소 샘플 데이터
val sampleRecommendedPlaces = listOf(
    RecommendedPlace(
        "을왕리해수욕장",
        listOf("인천", "바다"),
        R.drawable.place_eulwangri
    ),
    RecommendedPlace(
        "헤이리 예술마을",
        listOf("파주", "예술"),
        R.drawable.place_heyri
    ),
    RecommendedPlace(
        "한국민속촌",
        listOf("용인", "체험"),
        R.drawable.place_hanok_village
    ),
    RecommendedPlace(
        "수원화성",
        listOf("수원", "야경"),
        R.drawable.place_suwon_hwaseong
    ),
    RecommendedPlace(
        "광명동굴",
        listOf("광명", "체험"),
        R.drawable.place_gwangmyeong_cave
    ),
    RecommendedPlace(
        "화담숲",
        listOf("광주", "자연"),
        R.drawable.place_hwadamsup
    )
)

// 이미 추가된 친구 샘플 데이터
val sampleAddedFriends = listOf(
    Friend("민수", Color(0xFF8FB6D9)),
    Friend("지은", Color(0xFFF2C6C2)),
    Friend("하늘", Color(0xFFD9CBEF)),
    Friend("태오", Color(0xFFE0AE68))
)

// 친구 검색 샘플 데이터
val sampleSearchFriends = listOf(
    Friend("수지", Color(0xFF8B5E3C)),
    Friend("효리", Color(0xFFB9A6D9)),
    Friend("유나", Color(0xFF6FBBA6)),
    Friend("영희", Color(0xFF8FB6D9))
)