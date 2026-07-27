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
import com.damyeoom.app.data.UserSession
import com.damyeoom.app.data.database.AppDatabase
import com.damyeoom.app.data.koreanRegions
import com.damyeoom.app.data.sampleAddedFriends
import com.damyeoom.app.data.sampleSearchFriends
import com.damyeoom.app.entity.TravelRecord
import com.damyeoom.app.ui.theme.*
import com.google.gson.Gson
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import com.damyeoom.app.data.encodePhotos
import com.damyeoom.app.data.decodePhotos

private fun createCameraImageUri(context: Context): Uri {
    val imagesDir = File(context.cacheDir, "images").apply { mkdirs() }
    val file = File(imagesDir, "IMG_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}


private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTravelScreen(
    placeName: String,
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val scope = rememberCoroutineScope()

    var selectedRegion by remember {
        mutableStateOf(if (placeName in koreanRegions) placeName else koreanRegions.first())
    }

    var selectedDateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }

    var friends by remember { mutableStateOf(sampleAddedFriends) }
    var showFriendPicker by remember { mutableStateOf(false) }

    val photos = remember { mutableStateListOf<Uri>() }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    var titleInput by remember { mutableStateOf("") }
    var memoInput by remember { mutableStateOf("") }
    val records = remember { mutableStateListOf<TravelRecord>() }

    suspend fun refreshRecords() {
        val all = db.travelRecordDao().getAllRecords()
        records.clear()
        records.addAll(all.filter { it.region == selectedRegion })
    }

    LaunchedEffect(selectedRegion) { refreshRecords() }

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

        // 지역 선택 (칩)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Place, contentDescription = null, tint = PinRed)
            Spacer(modifier = Modifier.width(4.dp))
            Text("지역", fontSize = 14.sp, color = TextSecondary)
        }
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(koreanRegions) { region ->
                val isSelected = region == selectedRegion
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) ButtonDark else CardGray)
                        .clickable { selectedRegion = region }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = region,
                        fontSize = 13.sp,
                        color = if (isSelected) Color.White else TextPrimary,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 방문 날짜 선택
        Text("방문 날짜", fontSize = 14.sp, color = TextSecondary)
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedButton(
            onClick = { showDatePicker = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(dateFormat.format(java.util.Date(selectedDateMillis)))
        }

        if (showDatePicker) {
            val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDateMillis)
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { selectedDateMillis = it }
                        showDatePicker = false
                    }) { Text("확인") }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) { Text("취소") }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 장소명 (이제 DB의 placeName 칼럼에 바로 저장됨)
        Text("장소명", fontSize = 14.sp, color = TextSecondary)
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = titleInput,
            onValueChange = { titleInput = it },
            placeholder = { Text("예: 명동교자, 경복궁") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = CardGray,
                focusedContainerColor = CardGray,
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = TextPrimary
            )
        )

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
                modifier = Modifier.weight(1f).height(48.dp),
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
                    Box(modifier = Modifier.size(88.dp).clip(RoundedCornerShape(14.dp))) {
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
                            Icon(Icons.Filled.Close, contentDescription = "삭제", tint = Color.White, modifier = Modifier.size(12.dp))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text("누구랑 다녀왔나요?", fontSize = 15.sp, color = TextPrimary)
        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(friends) { friend -> FriendAvatar(friend = friend) }
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
            modifier = Modifier.fillMaxWidth().height(110.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = CardGray,
                focusedContainerColor = CardGray,
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = TextPrimary
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            OutlinedButton(
                onClick = {
                    titleInput = ""
                    memoInput = ""
                    photos.clear()
                },
                modifier = Modifier.height(40.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("취소", fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    val userId = UserSession.currentUserId.value
                    if (memoInput.isNotBlank() && userId != null) {
                        scope.launch {
                            val record = TravelRecord(
                                userId = userId,
                                region = selectedRegion,
                                placeName = titleInput.trim(),
                                visitDate = dateFormat.format(java.util.Date(selectedDateMillis)),
                                memo = memoInput.trim(),
                                imageUri = encodePhotos(photos)
                            )
                            db.travelRecordDao().insert(record)
                            refreshRecords()
                            titleInput = ""
                            memoInput = ""
                            photos.clear()
                        }
                    }
                },
                enabled = memoInput.isNotBlank(),
                modifier = Modifier.height(40.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ButtonDark,
                    disabledContainerColor = ButtonDisabled
                )
            ) {
                Text("저장", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text("여행 기록", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(modifier = Modifier.height(12.dp))

        records.sortedByDescending { it.createdAt }.forEach { record ->
            TravelRecordItem(
                record = record,
                isMine = record.userId == UserSession.currentUserId.value,
                onDelete = {
                    scope.launch {
                        db.travelRecordDao().delete(record)
                        refreshRecords()
                    }
                }
            )
            Spacer(modifier = Modifier.height(14.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun TravelRecordItem(record: TravelRecord, isMine: Boolean, onDelete: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier.size(36.dp).clip(CircleShape).background(ChipWhite),
            contentAlignment = Alignment.Center
        ) {
            Text(if (isMine) "나" else "친구", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (record.placeName.isNotBlank()) {
                    Text(record.placeName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text(record.visitDate, fontSize = 11.sp, color = TextSecondary)
            }
            Spacer(modifier = Modifier.height(3.dp))
            Text(record.memo, fontSize = 14.sp, color = TextPrimary)

            val photoUris = decodePhotos(record.imageUri)
            if (photoUris.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(photoUris) { uriStr ->
                        AsyncImage(
                            model = Uri.parse(uriStr),
                            contentDescription = "기록 사진",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(72.dp).clip(RoundedCornerShape(10.dp))
                        )
                    }
                }
            }
        }
        if (isMine) {
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "삭제", tint = TextSecondary)
            }
        }
    }
}

@Composable
private fun FriendAvatar(friend: Friend) {
    Box(
        modifier = Modifier.size(48.dp).clip(CircleShape).background(friend.color),
        contentAlignment = Alignment.Center
    ) {
        Text(text = friend.name.take(1), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
                modifier = Modifier.size(20.dp).clickable { onDismiss() }
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        candidates.forEach { friend ->
            Row(
                modifier = Modifier.fillMaxWidth().clickable { onAdd(friend) }.padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(friend.color))
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(friend.name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Text("+ 추가하기", fontSize = 12.sp, color = TextSecondary)
                }
            }
        }
    }
}