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
import com.damyeoom.app.data.Friend
import com.damyeoom.app.data.database.AppDatabase          // ← 추가
import com.damyeoom.app.data.sampleAddedFriends
import com.damyeoom.app.data.sampleSearchFriends
import com.damyeoom.app.entity.TravelRecord                 // ← 추가 (실제 패키지 경로 확인 필요)
import com.damyeoom.app.ui.theme.*
import kotlinx.coroutines.launch                             // ← 추가
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

private fun createCameraImageUri(context: Context): Uri {
    val imagesDir = File(context.cacheDir, "images").apply { mkdirs() }
    val file = File(imagesDir, "IMG_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}

@Composable
fun AddTravelScreen(
    placeName: String,
    db: AppDatabase,                                         // ← 추가된 파라미터
    onSelectOnMap: () -> Unit = {},
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()                      // ← 추가

    var friends by remember { mutableStateOf(sampleAddedFriends) }
    var showFriendPicker by remember { mutableStateOf(false) }

    val photos = remember { mutableStateListOf<Uri>() }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    var memoInput by remember { mutableStateOf("") }

    // 기존 sampleTravelPosts 기반 posts 리스트 → 실제 DB 조회로 교체
    // ⚠ TravelRecordDao에 placeName으로 조회하는 메서드가 없다면
    //    getAll() 등 현재 있는 메서드명으로 바꿔주세요.
    val travelRecords by db.travelRecordDao().getAllByPlace(placeName)
        .collectAsState(initial = emptyList())

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && pendingCameraUri != null) {
            photos.add(pendingCameraUri!!)
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val uri = createCameraImageUri(context)
            pendingCameraUri = uri
            cameraLauncher.launch(uri)
        }
    }

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
        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Place, contentDescription = null, tint = PinRed)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = placeName, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
            OutlinedButton(
                onClick = onSelectOnMap,
                shape = RoundedCornerShape(20.dp),
            ) {
                Text("지도에서 선택하기", fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text("여행 사진을 추가해 보세요.", fontSize = 15.sp, color = TextPrimary)
        Spacer(modifier = Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(ChipWhite)
                    .clickable {
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.CameraAlt, contentDescription = "사진 촬영", tint = TextPrimary)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Button(
                onClick = {
                    galleryLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ButtonDark)
            ) {
                Text("앨범에서 추가", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        if (photos.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(photos) { uri ->
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
                                .background(Color.Black.copy(alpha = 0.6f))
                                .clickable { photos.remove(uri) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = "삭제",
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text("누구랑 다녀왔나요?", fontSize = 15.sp, color = TextPrimary)
        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(friends) { friend ->
                FriendAvatar(friend = friend)
            }
            item {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(ButtonDark)
                        .clickable { showFriendPicker = !showFriendPicker },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "친구 추가", tint = Color.White)
                }
            }
        }

        if (showFriendPicker) {
            Spacer(modifier = Modifier.height(12.dp))
            FriendPickerDropdown(
                candidates = sampleSearchFriends.filter { c -> friends.none { it.name == c.name } },
                onAdd = { picked -> friends = friends + picked },
                onDismiss = { showFriendPicker = false }
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text("간단히 여행에 대해 기록해 보세요.", fontSize = 15.sp, color = TextPrimary)
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = memoInput,
            onValueChange = { memoInput = it },
            placeholder = { Text("~") },
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

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = {
                if (memoInput.isNotBlank()) {
                    val trimmedMemo = memoInput.trim()
                    val photoUriString = photos.firstOrNull()?.toString()  // ⚠ 사진 여러 장 저장 구조는 별도 논의 필요

                    scope.launch {
                        db.travelRecordDao().insert(
                            TravelRecord(
                                placeName = placeName,      // ⚠ 실제 필드명 확인 필요
                                memo = trimmedMemo,          // ⚠ 실제 필드명 확인 필요
                                photoUri = photoUriString    // ⚠ 실제 필드명 확인 필요
                            )
                        )
                    }
                    memoInput = ""
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
            Text("저장", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text("여행 기록", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(modifier = Modifier.height(12.dp))

        // 기존 posts.forEach → travelRecords.forEach 로 교체
        travelRecords.forEach { record ->
            TravelRecordItem(record = record)
            Spacer(modifier = Modifier.height(14.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// 기존 TravelPostItem(post: TravelPost) → TravelRecord용으로 교체
// ⚠ record.memo, record.timestamp 등은 실제 TravelRecord 필드명에 맞춰 조정 필요
@Composable
private fun TravelRecordItem(record: TravelRecord) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(ChipWhite),
            contentAlignment = Alignment.Center
        ) {
            Text("나", fontWeight = FontWeight.Bold, color = TextPrimary)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(record.memo, fontSize = 14.sp, color = TextPrimary)  // ⚠ 필드명 확인 필요
        }
    }
}

@Composable
private fun FriendAvatar(friend: Friend) {
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
            .border(1.dp, Divider, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("같이 여행한 친구를 골라보세요.", fontSize = 14.sp, color = TextSecondary)
            Icon(
                Icons.Filled.Close,
                contentDescription = "닫기",
                tint = TextSecondary,
                modifier = Modifier
                    .size(20.dp)
                    .clickable { onDismiss() }
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        candidates.forEach { friend ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onAdd(friend) }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(friend.color)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(friend.name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Text("+ 추가하기", fontSize = 12.sp, color = TextSecondary)
                }
            }
        }
    }
}