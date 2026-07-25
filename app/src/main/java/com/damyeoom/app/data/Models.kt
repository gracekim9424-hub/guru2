package com.damyeoom.app.data

import androidx.annotation.DrawableRes
import com.damyeoom.app.R
import androidx.compose.ui.graphics.Color

data class RecommendedPlace(
    val name: String,
    val tags: List<String>,
    @DrawableRes val imageRes: Int
)

data class Friend(
    val name: String,
    val color: Color,
    val alreadyAdded: Boolean = false
)

data class TravelPost(
    val authorName: String,
    val content: String,
    val timestamp: String
)

val sampleRecommendedPlaces = listOf(
    RecommendedPlace("을왕리해수욕장", listOf("인천", "바다"), R.drawable.place_eulwangri),
    RecommendedPlace("헤이리 예술마을", listOf("파주", "예술"), R.drawable.place_heyri),
    RecommendedPlace("한국민속촌", listOf("용인", "체험"), R.drawable.place_hanok_village),
    RecommendedPlace("수원화성", listOf("수원", "야경"), R.drawable.place_suwon_hwaseong),
    RecommendedPlace("광명동굴", listOf("광명", "체험"), R.drawable.place_gwangmyeong_cave),
    RecommendedPlace("화담숲", listOf("광주", "자연"), R.drawable.place_hwadamsup),
)

val sampleAddedFriends = listOf(
    Friend("민수", Color(0xFF8FB6D9)),
    Friend("지은", Color(0xFFF2C6C2)),
    Friend("하늘", Color(0xFFD9CBEF)),
    Friend("태오", Color(0xFFE0AE68)),
)

val sampleSearchFriends = listOf(
    Friend("수지", Color(0xFF8B5E3C)),
    Friend("효리", Color(0xFFB9A6D9)),
    Friend("유나", Color(0xFF6FBBA6)),
    Friend("영희", Color(0xFF8FB6D9)),
)

val sampleTravelPosts = listOf(
    TravelPost("지은", "여기 진짜 좋았어! 다음에 또 가고 싶다 🚗", "2026.07.10"),
    TravelPost("하늘", "날씨 좋을 때 가면 최고일 듯", "2026.07.12"),
)