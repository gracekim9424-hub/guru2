package com.damyeoom.app.data

// 지역 선택 칩에 쓰는 목록입니다.
// ⚠️ 여기 적힌 이름이 PlaceEntity.region 에 실제로 저장된 값과
// 정확히 똑같아야 추천 장소 조회가 됩니다. 팀원분한테 실제 값 확인 필요.
val koreanRegions: List<String> = listOf(
    "서울", "인천", "경기", "강원",
    "대전", "충남", "충북", "세종",
    "광주", "전남", "전북",
    "대구", "경북",
    "부산", "울산", "경남",
    "제주"
)