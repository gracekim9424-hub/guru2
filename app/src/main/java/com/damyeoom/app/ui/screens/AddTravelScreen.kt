package com.damyeoom.app.ui.screens

import android.Manifest
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.damyeoom.app.data.GeocodeRetrofitClient
import com.damyeoom.app.data.UserSession
import com.damyeoom.app.data.database.AppDatabase
import com.damyeoom.app.data.decodePhotos
import com.damyeoom.app.data.encodePhotos
import com.damyeoom.app.data.koreanRegions
import com.damyeoom.app.entity.FriendEntity
import com.damyeoom.app.entity.TravelRecord
import com.damyeoom.app.ui.theme.*
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

// 카메라 촬영 사진을 저장할 임시 URI 생성
private fun createCameraImageUri(context: Context): Uri {
    val imagesDir = File(
        context.cacheDir,
        "images"
    ).apply {
        mkdirs()
    }

    val file = File(
        imagesDir,
        "IMG_${System.currentTimeMillis()}.jpg"
    )

    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
}

// 장소명이나 지역명을 위도·경도로 변환
private suspend fun resolveCoordinates(
    query: String
): Pair<Double?, Double?> {
    if (query.isBlank()) {
        return null to null
    }

    return try {
        val response =
            GeocodeRetrofitClient.instance.getGeocode(
                query = query,
                clientId = GeocodeRetrofitClient.CLIENT_ID,
                clientSecret = GeocodeRetrofitClient.CLIENT_SECRET
            )

        android.util.Log.d(
            "GEOCODE_DEBUG",
            "응답: $response"
        )

        val first = response.addresses.firstOrNull()

        if (first != null) {
            first.y.toDoubleOrNull() to
                    first.x.toDoubleOrNull()
        } else {
            android.util.Log.d(
                "GEOCODE_DEBUG",
                "주소를 찾지 못함: $query"
            )

            null to null
        }
    } catch (e: Exception) {
        android.util.Log.e(
            "GEOCODE_DEBUG",
            "지오코딩 실패",
            e
        )

        null to null
    }
}

// 날짜 표시 형식
private val dateFormat =
    SimpleDateFormat(
        "yyyy-MM-dd",
        Locale.KOREA
    )

// 친구 프로필 색상 목록
private val friendColorPalette = listOf(
    0xFF8FB6D9,
    0xFFF2C6C2,
    0xFFD9CBEF,
    0xFFE0AE68,
    0xFF8B5E3C,
    0xFFB9A6D9,
    0xFF6FBBA6
)

// 처음 표시할 기본 친구 목록
private val defaultFriendSeeds = listOf(
    "민수" to 0xFF8FB6D9,
    "지은" to 0xFFF2C6C2,
    "하늘" to 0xFFD9CBEF,
    "태오" to 0xFFE0AE68
)

// 여행 기록을 작성하는 화면
@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class
)
@Composable
fun AddTravelScreen(
    placeName: String
) {
    // DB와 코루틴 설정
    val context = LocalContext.current
    val db = remember {
        AppDatabase.getDatabase(context)
    }
    val scope = rememberCoroutineScope()

    // 선택한 여행 지역
    var selectedRegion by remember {
        mutableStateOf(
            if (placeName in koreanRegions) {
                placeName
            } else {
                koreanRegions.first()
            }
        )
    }

    // 방문 날짜 상태
    var selectedDateMillis by remember {
        mutableStateOf(System.currentTimeMillis())
    }
    var showDatePicker by remember {
        mutableStateOf(false)
    }

    // 현재 로그인한 사용자 ID
    val currentUserId =
        UserSession.currentUserId.value

    // 친구 관련 상태
    var friends by remember {
        mutableStateOf<List<FriendEntity>>(
            emptyList()
        )
    }
    var showAddFriendDialog by remember {
        mutableStateOf(false)
    }
    var newFriendName by remember {
        mutableStateOf("")
    }
    var addFriendError by remember {
        mutableStateOf<String?>(null)
    }
    var friendPendingDelete by remember {
        mutableStateOf<FriendEntity?>(null)
    }
    var isFriendSaving by remember {
        mutableStateOf(false)
    }

    // 사진 관련 상태
    val photos = remember {
        mutableStateListOf<Uri>()
    }
    var pendingCameraUri by remember {
        mutableStateOf<Uri?>(null)
    }

    // 여행 기록 입력 상태
    var titleInput by remember {
        mutableStateOf("")
    }
    var memoInput by remember {
        mutableStateOf("")
    }
    var isSaving by remember {
        mutableStateOf(false)
    }

    // 저장된 여행 기록 목록
    val records = remember {
        mutableStateListOf<TravelRecord>()
    }

    // 선택 지역의 여행 기록 다시 조회
    suspend fun refreshRecords() {
        val all =
            db.travelRecordDao().getAllRecords()

        records.clear()

        records.addAll(
            all.filter {
                it.region == selectedRegion
            }
        )
    }

    // 현재 사용자의 친구 목록 조회
    suspend fun refreshFriends() {
        val userId =
            currentUserId ?: return

        val preferences =
            context.getSharedPreferences(
                "friend_seed_preferences",
                Context.MODE_PRIVATE
            )

        val seedKey =
            "seeded_user_$userId"

        var loadedFriends =
            db.friendDao().getFriends(userId)

        // 친구가 없으면 기본 친구 데이터 추가
        if (
            loadedFriends.isEmpty() &&
            !preferences.getBoolean(
                seedKey,
                false
            )
        ) {
            defaultFriendSeeds.forEach {
                    (name, colorArgb) ->

                db.friendDao().insert(
                    FriendEntity(
                        userId = userId,
                        name = name,
                        colorArgb = colorArgb
                    )
                )
            }

            preferences
                .edit()
                .putBoolean(seedKey, true)
                .apply()

            loadedFriends =
                db.friendDao().getFriends(userId)
        }

        friends = loadedFriends
    }

    // 지역 변경 시 여행 기록 갱신
    LaunchedEffect(selectedRegion) {
        refreshRecords()
    }

    // 로그인 사용자 변경 시 친구 목록 갱신
    LaunchedEffect(currentUserId) {
        refreshFriends()
    }

    // 카메라 촬영 실행
    val cameraLauncher =
        rememberLauncherForActivityResult(
            contract =
                ActivityResultContracts.TakePicture()
        ) { success ->
            if (
                success &&
                pendingCameraUri != null
            ) {
                photos.add(
                    pendingCameraUri!!
                )
            }
        }

    // 카메라 권한 요청
    val cameraPermissionLauncher =
        rememberLauncherForActivityResult(
            contract =
                ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                val uri =
                    createCameraImageUri(context)

                pendingCameraUri = uri
                cameraLauncher.launch(uri)
            }
        }

    // 앨범에서 여러 사진 선택
    val galleryLauncher =
        rememberLauncherForActivityResult(
            contract =
                ActivityResultContracts
                    .PickMultipleVisualMedia()
        ) { uris ->
            photos.addAll(uris)
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgLight)
            .verticalScroll(
                rememberScrollState()
            )
            .padding(horizontal = 20.dp)
    ) {
        Spacer(
            modifier = Modifier.height(20.dp)
        )

        // 지역 선택
        Row(
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.Place,
                contentDescription = null,
                tint = PinRed
            )

            Spacer(
                modifier = Modifier.width(4.dp)
            )

            Text(
                text = "지역",
                fontSize = 14.sp,
                color = TextSecondary
            )
        }

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        LazyRow(
            horizontalArrangement =
                Arrangement.spacedBy(8.dp)
        ) {
            items(koreanRegions) { region ->
                val isSelected =
                    region == selectedRegion

                Box(
                    modifier = Modifier
                        .clip(
                            RoundedCornerShape(
                                20.dp
                            )
                        )
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
                        color =
                            if (isSelected) {
                                Color.White
                            } else {
                                TextPrimary
                            },
                        fontWeight =
                            if (isSelected) {
                                FontWeight.SemiBold
                            } else {
                                FontWeight.Normal
                            }
                    )
                }
            }
        }

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        // 방문 날짜 선택
        Text(
            text = "방문 날짜",
            fontSize = 14.sp,
            color = TextSecondary
        )

        Spacer(
            modifier = Modifier.height(6.dp)
        )

        OutlinedButton(
            onClick = {
                showDatePicker = true
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                dateFormat.format(
                    java.util.Date(
                        selectedDateMillis
                    )
                )
            )
        }

        // 날짜 선택 창
        if (showDatePicker) {
            val datePickerState =
                rememberDatePickerState(
                    initialSelectedDateMillis =
                        selectedDateMillis
                )

            DatePickerDialog(
                onDismissRequest = {
                    showDatePicker = false
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            datePickerState
                                .selectedDateMillis
                                ?.let {
                                    selectedDateMillis = it
                                }

                            showDatePicker = false
                        }
                    ) {
                        Text("확인")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showDatePicker = false
                        }
                    ) {
                        Text("취소")
                    }
                }
            ) {
                DatePicker(
                    state = datePickerState
                )
            }
        }

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        // 장소명 입력
        Text(
            text = "장소명",
            fontSize = 14.sp,
            color = TextSecondary
        )

        Spacer(
            modifier = Modifier.height(6.dp)
        )

        OutlinedTextField(
            value = titleInput,
            onValueChange = {
                titleInput = it
            },
            placeholder = {
                Text("예: 명동교자, 경복궁")
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
            colors =
                OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor =
                        CardGray,
                    focusedContainerColor =
                        CardGray,
                    unfocusedBorderColor =
                        Color.Transparent,
                    focusedBorderColor =
                        TextPrimary
                )
        )

        Spacer(
            modifier = Modifier.height(28.dp)
        )

        // 사진 추가 영역
        Text(
            text = "여행 사진을 추가해 보세요.",
            fontSize = 15.sp,
            color = TextPrimary
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        Row(
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            // 카메라 촬영 버튼
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(ChipWhite)
                    .clickable {
                        cameraPermissionLauncher
                            .launch(
                                Manifest.permission.CAMERA
                            )
                    },
                contentAlignment =
                    Alignment.Center
            ) {
                Icon(
                    Icons.Filled.CameraAlt,
                    contentDescription = "사진 촬영",
                    tint = TextPrimary
                )
            }

            Spacer(
                modifier = Modifier.width(12.dp)
            )

            // 앨범 사진 선택 버튼
            Button(
                onClick = {
                    galleryLauncher.launch(
                        PickVisualMediaRequest(
                            ActivityResultContracts
                                .PickVisualMedia
                                .ImageOnly
                        )
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape =
                    RoundedCornerShape(24.dp),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = ButtonDark
                    )
            ) {
                Text(
                    text = "앨범에서 추가",
                    fontSize = 15.sp,
                    fontWeight =
                        FontWeight.SemiBold
                )
            }
        }

        // 선택한 사진 미리보기
        if (photos.isNotEmpty()) {
            Spacer(
                modifier = Modifier.height(12.dp)
            )

            LazyRow(
                horizontalArrangement =
                    Arrangement.spacedBy(10.dp)
            ) {
                items(photos) { uri ->
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .clip(
                                RoundedCornerShape(
                                    14.dp
                                )
                            )
                    ) {
                        AsyncImage(
                            model = uri,
                            contentDescription =
                                "선택된 사진",
                            contentScale =
                                ContentScale.Crop,
                            modifier =
                                Modifier.fillMaxSize()
                        )

                        // 선택 사진 삭제 버튼
                        Box(
                            modifier = Modifier
                                .align(
                                    Alignment.TopEnd
                                )
                                .padding(4.dp)
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(
                                    Color.Black.copy(
                                        alpha = 0.6f
                                    )
                                )
                                .clickable {
                                    photos.remove(uri)
                                },
                            contentAlignment =
                                Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.Close,
                                contentDescription =
                                    "삭제",
                                tint = Color.White,
                                modifier =
                                    Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(
            modifier = Modifier.height(28.dp)
        )

        // 친구 목록
        Text(
            text = "누구랑 다녀왔나요?",
            fontSize = 15.sp,
            color = TextPrimary
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        LazyRow(
            horizontalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = friends,
                key = {
                    it.id
                }
            ) { friend ->
                FriendAvatar(
                    friend = friend,
                    onLongClick = {
                        friendPendingDelete =
                            friend
                    }
                )
            }

            // 친구 추가 버튼
            item {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(ButtonDark)
                        .clickable {
                            newFriendName = ""
                            addFriendError = null
                            showAddFriendDialog =
                                true
                        },
                    contentAlignment =
                        Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription =
                            "친구 추가",
                        tint = Color.White
                    )
                }
            }
        }

        // 친구 추가 입력 카드
        if (showAddFriendDialog) {
            Spacer(
                modifier = Modifier.height(12.dp)
            )

            AddFriendCard(
                name = newFriendName,
                errorMessage = addFriendError,
                isSaving = isFriendSaving,
                onNameChange = {
                    newFriendName = it
                    addFriendError = null
                },
                onAdd = {
                    val userId =
                        currentUserId

                    val normalizedName =
                        newFriendName
                            .trim()
                            .replace(
                                Regex("\\s+"),
                                " "
                            )

                    when {
                        userId == null -> {
                            addFriendError =
                                "로그인 정보를 확인할 수 없습니다."
                        }

                        normalizedName.isBlank() -> {
                            addFriendError =
                                "친구 이름을 입력해주세요."
                        }

                        normalizedName.length > 20 -> {
                            addFriendError =
                                "친구 이름은 20자 이하로 입력해주세요."
                        }

                        friends.any {
                            it.name.equals(
                                normalizedName,
                                ignoreCase = true
                            )
                        } -> {
                            addFriendError =
                                "이미 추가된 친구입니다."
                        }

                        else -> {
                            isFriendSaving = true

                            scope.launch {
                                try {
                                    val colorArgb =
                                        friendColorPalette[
                                            friends.size %
                                                    friendColorPalette.size
                                        ]

                                    val insertedId =
                                        db.friendDao()
                                            .insert(
                                                FriendEntity(
                                                    userId =
                                                        userId,
                                                    name =
                                                        normalizedName,
                                                    colorArgb =
                                                        colorArgb
                                                )
                                            )

                                    if (
                                        insertedId == -1L
                                    ) {
                                        addFriendError =
                                            "이미 추가된 친구입니다."
                                    } else {
                                        refreshFriends()
                                        showAddFriendDialog =
                                            false
                                        newFriendName = ""
                                        addFriendError =
                                            null
                                    }
                                } catch (
                                    e: Exception
                                ) {
                                    android.util.Log.e(
                                        "FriendAdd",
                                        "친구 추가 실패",
                                        e
                                    )

                                    addFriendError =
                                        "친구를 추가하지 못했습니다."
                                } finally {
                                    isFriendSaving =
                                        false
                                }
                            }
                        }
                    }
                },
                onDismiss = {
                    showAddFriendDialog = false
                    newFriendName = ""
                    addFriendError = null
                }
            )
        }

        Spacer(
            modifier = Modifier.height(28.dp)
        )

        // 여행 메모 입력
        Text(
            text = "간단히 여행에 대해 기록해 보세요.",
            fontSize = 15.sp,
            color = TextPrimary
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        OutlinedTextField(
            value = memoInput,
            onValueChange = {
                memoInput = it
            },
            placeholder = {
                Text("~")
            },
            shape =
                RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp),
            colors =
                OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor =
                        CardGray,
                    focusedContainerColor =
                        CardGray,
                    unfocusedBorderColor =
                        Color.Transparent,
                    focusedBorderColor =
                        TextPrimary
                )
        )

        Spacer(
            modifier = Modifier.height(10.dp)
        )

        // 취소 및 저장 버튼
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.End
        ) {
            OutlinedButton(
                onClick = {
                    titleInput = ""
                    memoInput = ""
                    photos.clear()
                },
                modifier = Modifier.height(40.dp),
                shape =
                    RoundedCornerShape(20.dp)
            ) {
                Text(
                    text = "취소",
                    fontSize = 14.sp
                )
            }

            Spacer(
                modifier = Modifier.width(8.dp)
            )

            Button(
                onClick = {
                    val userId =
                        UserSession.currentUserId.value

                    if (
                        memoInput.isNotBlank() &&
                        userId != null &&
                        !isSaving
                    ) {
                        isSaving = true

                        scope.launch {
                            // 장소명을 좌표로 변환
                            val query =
                                if (titleInput.isBlank()) {
                                    selectedRegion
                                } else {
                                    "$selectedRegion ${titleInput.trim()}"
                                }

                            val (
                                geoLat,
                                geoLng
                            ) = resolveCoordinates(query)

                            // 여행 기록 객체 생성
                            val record =
                                TravelRecord(
                                    userId = userId,
                                    region =
                                        selectedRegion,
                                    placeName =
                                        titleInput.trim(),
                                    visitDate =
                                        dateFormat.format(
                                            java.util.Date(
                                                selectedDateMillis
                                            )
                                        ),
                                    memo =
                                        memoInput.trim(),
                                    imageUri =
                                        encodePhotos(
                                            photos
                                        ),
                                    latitude = geoLat,
                                    longitude = geoLng
                                )

                            // 여행 기록 DB 저장
                            db.travelRecordDao()
                                .insert(record)

                            refreshRecords()

                            titleInput = ""
                            memoInput = ""
                            photos.clear()
                            isSaving = false
                        }
                    }
                },
                enabled =
                    memoInput.isNotBlank() &&
                            !isSaving,
                modifier =
                    Modifier.height(40.dp),
                shape =
                    RoundedCornerShape(20.dp),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor =
                            ButtonDark,
                        disabledContainerColor =
                            ButtonDisabled
                    )
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier =
                            Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                } else {
                    Text(
                        text = "저장",
                        fontSize = 14.sp,
                        fontWeight =
                            FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        // 저장된 여행 기록 목록
        Text(
            text = "여행 기록",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        records
            .sortedByDescending {
                it.createdAt
            }
            .forEach { record ->
                TravelRecordItem(
                    record = record,
                    isMine =
                        record.userId ==
                                UserSession
                                    .currentUserId
                                    .value,
                    onDelete = {
                        scope.launch {
                            db.travelRecordDao()
                                .delete(record)

                            refreshRecords()
                        }
                    }
                )

                Spacer(
                    modifier =
                        Modifier.height(14.dp)
                )
            }

        Spacer(
            modifier = Modifier.height(24.dp)
        )
    }

    // 친구 삭제 확인 창
    friendPendingDelete?.let { friend ->
        AlertDialog(
            onDismissRequest = {
                if (!isFriendSaving) {
                    friendPendingDelete = null
                }
            },
            title = {
                Text(
                    text = "친구를 삭제할까요?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    "${friend.name}님을 친구 목록에서 삭제합니다."
                )
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        friendPendingDelete = null
                    },
                    enabled = !isFriendSaving
                ) {
                    Text("취소")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (isFriendSaving) {
                            return@TextButton
                        }

                        isFriendSaving = true

                        scope.launch {
                            try {
                                db.friendDao()
                                    .delete(friend)

                                refreshFriends()

                                friendPendingDelete =
                                    null
                            } finally {
                                isFriendSaving =
                                    false
                            }
                        }
                    },
                    enabled = !isFriendSaving
                ) {
                    if (isFriendSaving) {
                        CircularProgressIndicator(
                            modifier =
                                Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "삭제",
                            color = PinRed,
                            fontWeight =
                                FontWeight.Bold
                        )
                    }
                }
            }
        )
    }
}

// 친구 이름 입력 카드
@Composable
private fun AddFriendCard(
    name: String,
    errorMessage: String?,
    isSaving: Boolean,
    onNameChange: (String) -> Unit,
    onAdd: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(
                RoundedCornerShape(16.dp)
            )
            .background(Color.White)
            .border(
                width = 1.dp,
                color = Divider,
                shape =
                    RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.SpaceBetween,
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Text(
                text =
                    "같이 여행한 친구를 추가해보세요.",
                fontSize = 14.sp,
                color = TextSecondary
            )

            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "닫기",
                tint = TextSecondary,
                modifier = Modifier
                    .size(20.dp)
                    .clickable {
                        onDismiss()
                    }
            )
        }

        Spacer(
            modifier = Modifier.height(14.dp)
        )

        // 친구 이름 입력
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            placeholder = {
                Text("친구 이름을 입력해주세요.")
            },
            singleLine = true,
            isError = errorMessage != null,
            supportingText = {
                errorMessage?.let { message ->
                    Text(message)
                }
            },
            shape =
                RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
            colors =
                OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor =
                        CardGray,
                    focusedContainerColor =
                        CardGray,
                    unfocusedBorderColor =
                        Color.Transparent,
                    focusedBorderColor =
                        TextPrimary,
                    errorContainerColor =
                        CardGray
                )
        )

        Spacer(
            modifier = Modifier.height(10.dp)
        )

        // 친구 추가 및 취소 버튼
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.End,
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            TextButton(
                onClick = onDismiss,
                enabled = !isSaving
            ) {
                Text(
                    text = "취소",
                    color = TextSecondary
                )
            }

            Spacer(
                modifier = Modifier.width(4.dp)
            )

            Button(
                onClick = onAdd,
                enabled =
                    !isSaving &&
                            name.isNotBlank(),
                shape =
                    RoundedCornerShape(20.dp),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor =
                            ButtonDark,
                        disabledContainerColor =
                            ButtonDisabled
                    )
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier =
                            Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                } else {
                    Text(
                        text = "추가",
                        fontWeight =
                            FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

// 저장된 여행 기록 한 개 표시
@Composable
private fun TravelRecordItem(
    record: TravelRecord,
    isMine: Boolean,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        // 작성자 표시
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(ChipWhite),
            contentAlignment =
                Alignment.Center
        ) {
            Text(
                text =
                    if (isMine) {
                        "나"
                    } else {
                        "친구"
                    },
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        Spacer(
            modifier = Modifier.width(10.dp)
        )

        Column(
            modifier = Modifier.weight(1f)
        ) {
            // 장소명과 날짜
            Row(
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                if (
                    record.placeName.isNotBlank()
                ) {
                    Text(
                        text = record.placeName,
                        fontSize = 14.sp,
                        fontWeight =
                            FontWeight.Bold,
                        color = TextPrimary
                    )

                    Spacer(
                        modifier =
                            Modifier.width(6.dp)
                    )
                }

                Text(
                    text = record.visitDate,
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }

            Spacer(
                modifier = Modifier.height(3.dp)
            )

            // 여행 메모
            Text(
                text = record.memo,
                fontSize = 14.sp,
                color = TextPrimary
            )

            // 저장된 사진 목록
            val photoUris =
                decodePhotos(record.imageUri)

            if (photoUris.isNotEmpty()) {
                Spacer(
                    modifier =
                        Modifier.height(8.dp)
                )

                LazyRow(
                    horizontalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {
                    items(photoUris) { uriStr ->
                        AsyncImage(
                            model =
                                Uri.parse(uriStr),
                            contentDescription =
                                "기록 사진",
                            contentScale =
                                ContentScale.Crop,
                            modifier = Modifier
                                .size(72.dp)
                                .clip(
                                    RoundedCornerShape(
                                        10.dp
                                    )
                                )
                        )
                    }
                }
            }
        }

        // 본인 기록 삭제 버튼
        if (isMine) {
            IconButton(
                onClick = onDelete
            ) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "삭제",
                    tint = TextSecondary
                )
            }
        }
    }
}

// 친구 프로필 원형 아이콘
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FriendAvatar(
    friend: FriendEntity,
    onLongClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(
                Color(friend.colorArgb)
            )
            .combinedClickable(
                onClick = {},
                onLongClick = onLongClick
            ),
        contentAlignment =
            Alignment.Center
    ) {
        Text(
            text = friend.name.take(1),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}