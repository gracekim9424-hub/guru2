# 다녀옴! (Damyeoom)

피그마 시안 기반으로 만든 국내 여행 다이어리 앱 UI입니다.
**Kotlin + Jetpack Compose + Navigation Compose**로 작성되었습니다.

## 포함된 화면
- `LoginScreen` — 로그인/회원가입 (이메일, 비밀번호)
- `HomeScreen` — 대한민국 지도 영역 + "여행지 추가하기" + 주변 여행지 추천 카드 리스트
- `AddTravelScreen` — 여행지 상세 입력: 사진 추가(카메라/앨범), 동행자 선택(친구 검색 드롭다운 포함), 여행 메모
- `PlaceDetailScreen` — 추천 카드를 눌렀을 때 이동하는 장소 상세 화면(핀 + 장소명)

## 프로젝트 구조
```
app/src/main/java/com/damyeoom/app/
 ├─ MainActivity.kt
 ├─ data/Models.kt              # RecommendedPlace, Friend 샘플 데이터
 └─ ui/
     ├─ DamyeoomNavGraph.kt     # 화면 간 네비게이션(login → home → add_travel / place_detail)
     ├─ theme/                  # Color.kt, Type.kt, Theme.kt (디자인 색상 팔레트)
     └─ screens/                # 화면별 Composable
```

## 여는 방법 (Android Studio)
1. Android Studio 최신 버전(Koala 이상 권장) 실행
2. **File > Open** → 압축 해제한 `damyeoom` 폴더 선택
3. Gradle Sync가 자동으로 시작됩니다. (Gradle wrapper jar가 없다는 안내가 뜨면
   "OK, use Gradle from: (기본 설치본)"을 선택하거나, 상단 메뉴에서
   **File > Sync Project with Gradle Files**를 눌러 wrapper를 재생성하세요.)
4. 에뮬레이터 또는 실기기를 연결한 뒤 ▶ Run 버튼 실행

## 다음에 이어서 작업하면 좋은 부분
- `HomeScreen`의 지도 영역은 현재 플레이스홀더 박스입니다. 실제 지도(구글맵 SDK 또는
  커스텀 SVG 지도)로 교체하면 시안과 완전히 동일해집니다.
- 사진 추가 버튼에 실제 갤러리/카메라 인텐트(`ActivityResultContracts.PickVisualMedia`,
  `TakePicture`) 연결이 필요합니다.
- 로그인/회원가입은 현재 UI만 구현되어 있으며, 실제 인증 로직(Firebase Auth 등)은
  별도로 연결해야 합니다.
- 색상 값은 `ui/theme/Color.kt`에 모아뒀으니 시안과 다른 부분이 있으면 여기서
  한 번에 조정할 수 있습니다.
