package com.damyeoom.app.data

// 장소 상세 화면에 표시할 추가 정보
data class PlaceExtra(
    val cardImageRes: String? = null,
    val detailImageRes: String? = null,
    val openingHours: String? = null,
    val phoneNumber: String? = null,
    val recommendedFor: String? = null
)

// 장소 ID별 추가 정보
val placeExtras: Map<Int, PlaceExtra> = mapOf(

    // 수원화성
    4 to PlaceExtra(
        cardImageRes = "place_suwon_hwaseong",
        detailImageRes = "place_suwon",
        openingHours = "09:00 - 18:00",
        phoneNumber = "031-290-3600",
        recommendedFor = "역사 탐방을 좋아하는 분"
    ),

    // 한국민속촌
    5 to PlaceExtra(
        cardImageRes = "place_hanok_village",
        detailImageRes = "place_hanok",
        openingHours = "09:30 - 18:30",
        phoneNumber = "031-288-0000",
        recommendedFor = "가족, 외국인 친구와 함께"
    ),

    // 을왕리해수욕장
    54 to PlaceExtra(
        cardImageRes = "place_eulwangri",
        detailImageRes = "place_eul",
        openingHours = "상시 개방 (해수욕 개장: 매년 7~8월)",
        phoneNumber = "032-832-3031",
        recommendedFor = "여름 바캉스, 가족 나들이"
    ),

    // 헤이리 예술마을
    55 to PlaceExtra(
        cardImageRes = "place_heyri",
        detailImageRes = "place_heyrivillage",
        openingHours = "마을 상시 개방 (사무국 평일 09:00-18:00)",
        phoneNumber = "031-946-8551",
        recommendedFor = "예술/전시를 좋아하는 분, 데이트"
    ),

    // 광명동굴
    56 to PlaceExtra(
        cardImageRes = "place_gwangmyeong_cave",
        detailImageRes = "place_cave",
        openingHours = "화~일 09:00 - 18:00 (월요일 휴관)",
        phoneNumber = "070-4277-8902",
        recommendedFor = "이색 체험, 사진 스팟"
    ),

    // 화담숲
    57 to PlaceExtra(
        cardImageRes = "place_hwadamsup",
        detailImageRes = "place_forest",
        openingHours = "화~일 09:00 - 18:00 (월요일 휴장)",
        phoneNumber = "031-8026-6666",
        recommendedFor = "자연 산책, 가족 나들이"
    )
)