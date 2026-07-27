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

private val categories = listOf("명소", "맛집", "놀거리")

@Composable
fun PlaceRecommendScreen() {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }

    var selectedRegion by remember { mutableStateOf(koreanRegions.first()) }
    var selectedCategory by remember { mutableStateOf(categories.first()) }

    var places by remember { mutableStateOf(listOf<PlaceEntity>()) }

    LaunchedEffect(selectedRegion, selectedCategory) {
        db.placeDao().getPlacesByRegionAndCategory(selectedRegion, selectedCategory).collect {
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
        Text("지역별 추천 여행지", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary)

        Spacer(modifier = Modifier.height(16.dp))

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
                        color = if (isSelected) Color.White else TextPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            categories.forEach { category ->
                val isSelected = category == selectedCategory
                Button(
                    onClick = { selectedCategory = category },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) ButtonDark else CardGray,
                        contentColor = if (isSelected) Color.White else TextPrimary
                    )
                ) {
                    Text(category, fontSize = 13.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (places.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                Text("해당 조건의 여행지가 없습니다.", fontSize = 14.sp, color = TextSecondary)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                items(places) { place ->
                    PlaceCard(place = place)
                }
                item { Spacer(modifier = Modifier.height(20.dp)) }
            }
        }
    }
}

@Composable
private fun PlaceCard(place: PlaceEntity) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardGray)
            .padding(14.dp)
    ) {
        Text(place.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(modifier = Modifier.height(4.dp))
        Text(place.address, fontSize = 12.sp, color = TextSecondary)
        Spacer(modifier = Modifier.height(6.dp))
        Text(place.description, fontSize = 13.sp, color = TextPrimary)
    }
}