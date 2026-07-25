package com.damyeoom.app.ui.screens

import android.Manifest
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
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
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.damyeoom.app.data.Friend
import com.damyeoom.app.data.database.AppDatabase
import com.damyeoom.app.data.sampleAddedFriends
import com.damyeoom.app.data.sampleSearchFriends
import com.damyeoom.app.entity.TravelRecord
import com.damyeoom.app.ui.theme.*
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 카메라로 촬영할 사진이 임시 저장될 URI를 생성한다.
 */
private fun createCameraImageUri(context: Context): Uri {
    val imagesDir = File(context.cacheDir, "images").apply {
        mkdirs()
    }

    val imageFile = File(
        imagesDir,
        "IMG_${System.currentTimeMillis()}.jpg"
    )

    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        imageFile
    )
}

@Composable
fun AddTravelScreen(
    placeName: String,
    db: AppDatabase,
    onSelectOnMap: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var friends by remember {
        mutableStateOf(sampleAddedFriends)
    }

    var showFriendPicker by remember {
        mutableStateOf(false)
    }

    val photos = remember {
        mutableStateListOf<Uri>()
    }

    var pendingCameraUri by remember {
        mutableStateOf<Uri?>(null)
    }

    var memoInput by remember {
        mutableStateOf("")
    }

    /*
     * DAO의 getRecordsByRegion()은 Flow가 아니라
     * suspend 함수이므로 collectAsState()를 사용하지 않는다.
     */
    var travelRecords by remember {
        mutableStateOf<List<TravelRecord>>(emptyList())
    }

    /*
     * 화면이 열리거나 placeName이 변경될 때
     * 해당 지역의 여행 기록을 불러온다.
     */
    LaunchedEffect(placeName) {
        travelRecords = db
            .travelRecordDao()
            .getRecordsByRegion(placeName)
    }

    /*
     * 카메라 촬영 결과 처리
     */
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->

        val savedUri = pendingCameraUri

        if (success && savedUri != null) {
            photos.add(savedUri)
        }

        pendingCameraUri = null
    }

    /*
     * 카메라 권한 요청
     */
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->

        if (granted) {
            val uri = createCameraImageUri(context)

            pendingCameraUri = uri
            cameraLauncher.launch(uri)
        }
    }

    /*
     * 갤러리에서 여러 장의 사진 선택
     */
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->

        photos.addAll(uris)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgLight)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(
            modifier = Modifier.height(20.dp)
        )

        /*
         * 선택된 장소 표시 영역
         */
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Place,
                    contentDescription = null,
                    tint = PinRed
                )

                Spacer(
                    modifier = Modifier.width(4.dp)
                )

                Text(
                    text = placeName,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            OutlinedButton(
                onClick = onSelectOnMap,
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    text = "지도에서 선택하기",
                    fontSize = 13.sp
                )
            }
        }

        Spacer(
            modifier = Modifier.height(28.dp)
        )

        /*
         * 사진 추가 영역
         */
        Text(
            text = "여행 사진을 추가해 보세요.",
            fontSize = 15.sp,
            color = TextPrimary
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(ChipWhite)
                    .clickable {
                        cameraPermissionLauncher.launch(
                            Manifest.permission.CAMERA
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.CameraAlt,
                    contentDescription = "사진 촬영",
                    tint = TextPrimary
                )
            }

            Spacer(
                modifier = Modifier.width(12.dp)
            )

            Button(
                onClick = {
                    galleryLauncher.launch(
                        PickVisualMediaRequest(
                            ActivityResultContracts.PickVisualMedia.ImageOnly
                        )
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ButtonDark
                )
            ) {
                Text(
                    text = "앨범에서 추가",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        /*
         * 선택한 사진 미리보기
         */
        if (photos.isNotEmpty()) {
            Spacer(
                modifier = Modifier.height(12.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = photos,
                    key = { uri -> uri.toString() }
                ) { uri ->

                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .clip(RoundedCornerShape(14.dp))
                    ) {
                        AsyncImage(
                            model = uri,
                            contentDescription = "선택된 사진",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(
                                    Color.Black.copy(alpha = 0.6f)
                                )
                                .clickable {
                                    photos.remove(uri)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "사진 삭제",
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(
            modifier = Modifier.height(28.dp)
        )

        /*
         * 친구 선택 영역
         */
        Text(
            text = "누구랑 다녀왔나요?",
            fontSize = 15.sp,
            color = TextPrimary
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = friends,
                key = { friend -> friend.name }
            ) { friend ->

                FriendAvatar(
                    friend = friend
                )
            }

            item {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(ButtonDark)
                        .clickable {
                            showFriendPicker = !showFriendPicker
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "친구 추가",
                        tint = Color.White
                    )
                }
            }
        }

        if (showFriendPicker) {
            Spacer(
                modifier = Modifier.height(12.dp)
            )

            FriendPickerDropdown(
                candidates = sampleSearchFriends.filter { candidate ->
                    friends.none { friend ->
                        friend.name == candidate.name
                    }
                },
                onAdd = { pickedFriend ->
                    friends = friends + pickedFriend
                },
                onDismiss = {
                    showFriendPicker = false
                }
            )
        }

        Spacer(
            modifier = Modifier.height(28.dp)
        )

        /*
         * 여행 메모 입력 영역
         */
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
            onValueChange = { newValue ->
                memoInput = newValue
            },
            placeholder = {
                Text("여행에 대한 기록을 입력해 주세요.")
            },
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = CardGray,
                focusedContainerColor = CardGray,
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = TextPrimary
            )
        )

        Spacer(
            modifier = Modifier.height(10.dp)
        )

        /*
         * 여행 기록 저장 버튼
         */
        Button(
            onClick = {
                val trimmedMemo = memoInput.trim()

                if (trimmedMemo.isNotBlank()) {
                    /*
                     * 현재 Entity는 사진 URI 한 장만 저장할 수 있으므로
                     * 첫 번째로 선택된 사진만 저장한다.
                     */
                    val firstPhotoUri = photos
                        .firstOrNull()
                        ?.toString()

                    val today = SimpleDateFormat(
                        "yyyy-MM-dd",
                        Locale.getDefault()
                    ).format(Date())

                    scope.launch {
                        val newRecord = TravelRecord(
                            region = placeName,
                            visitDate = today,
                            memo = trimmedMemo,
                            imageUri = firstPhotoUri
                        )

                        db.travelRecordDao().insert(newRecord)

                        /*
                         * 저장 후 DB를 다시 조회하여
                         * 화면의 여행 기록 목록을 갱신한다.
                         */
                        travelRecords = db
                            .travelRecordDao()
                            .getRecordsByRegion(placeName)

                        memoInput = ""
                        photos.clear()
                    }
                }
            },
            enabled = memoInput.isNotBlank(),
            modifier = Modifier
                .align(Alignment.End)
                .height(40.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ButtonDark,
                disabledContainerColor = ButtonDisabled
            )
        ) {
            Text(
                text = "저장",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        /*
         * 저장된 여행 기록 목록
         */
        Text(
            text = "여행 기록",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        if (travelRecords.isEmpty()) {
            Text(
                text = "아직 저장된 여행 기록이 없습니다.",
                fontSize = 14.sp,
                color = TextSecondary
            )
        } else {
            travelRecords.forEach { record ->
                TravelRecordItem(
                    record = record
                )

                Spacer(
                    modifier = Modifier.height(14.dp)
                )
            }
        }

        Spacer(
            modifier = Modifier.height(24.dp)
        )
    }
}

/**
 * 저장된 여행 기록 한 개를 표시한다.
 */
@Composable
private fun TravelRecordItem(
    record: TravelRecord
) {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(ChipWhite),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "나",
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
            Text(
                text = record.visitDate,
                fontSize = 12.sp,
                color = TextSecondary
            )

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            Text(
                text = record.memo,
                fontSize = 14.sp,
                color = TextPrimary
            )

            record.imageUri?.let { imageUri ->
                Spacer(
                    modifier = Modifier.height(10.dp)
                )

                AsyncImage(
                    model = imageUri,
                    contentDescription = "여행 기록 사진",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(14.dp))
                )
            }
        }
    }
}

/**
 * 선택된 친구의 원형 아바타를 표시한다.
 */
@Composable
private fun FriendAvatar(
    friend: Friend
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(friend.color),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = friend.name.take(1),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}

/**
 * 친구를 선택하는 드롭다운 영역이다.
 */
@Composable
private fun FriendPickerDropdown(
    candidates: List<Friend>,
    onAdd: (Friend) -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(
                width = 1.dp,
                color = Divider,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "같이 여행한 친구를 골라보세요.",
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
            modifier = Modifier.height(12.dp)
        )

        if (candidates.isEmpty()) {
            Text(
                text = "추가할 수 있는 친구가 없습니다.",
                fontSize = 13.sp,
                color = TextSecondary
            )
        } else {
            candidates.forEach { friend ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onAdd(friend)
                        }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(friend.color)
                    )

                    Spacer(
                        modifier = Modifier.width(12.dp)
                    )

                    Column {
                        Text(
                            text = friend.name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )

                        Text(
                            text = "+ 추가하기",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}