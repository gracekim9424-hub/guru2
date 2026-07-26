package com.team.travelmap

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.util.FusedLocationSource
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope
import com.team.travelmap.weather.RetrofitClient
import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import android.content.ContentValues
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.content.Intent
import com.team.travelmap.weather.toDailyForecasts
import com.naver.maps.map.overlay.Marker
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.overlay.OverlayImage
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// TODO: 실제 AppDatabase 클래스가 있는 패키지 경로로 수정하세요.
// 예: import com.team.travelmap.db.AppDatabase
import com.damyeoom.app.database.AppDatabase

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var locationSource: FusedLocationSource
    private lateinit var naverMap: NaverMap
    // FusedLocationProviderClient 변수 선언
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentMarker: Marker? = null

    // 권한 요청 팝업 콜백 정의
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineLocationGranted || coarseLocationGranted) {
            // 권한 승인 시 현재 위치 가져오기
            getCurrentLocation()
        } else {
            Toast.makeText(this, "위치 권한이 거부되어 현재 위치를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    data class Attraction(val name: String, val lat: Double, val lon: Double)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 네이버 지도 클라이언트 인증
        com.naver.maps.map.NaverMapSdk.getInstance(this).client =
            com.naver.maps.map.NaverMapSdk.NcpKeyClient("1p93zfjw09")

        setContentView(R.layout.activity_main)

        // FusedLocationProviderClient 초기화
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // 위치 권한 체크 시작
        checkLocationPermission()

        // 네이버 지도 전용 FusedLocationSource 생성 (권한 팝업 자동 처리)
        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)

        // MapFragment 객체 불러오기
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map_fragment) as MapFragment?
            ?: MapFragment.newInstance().also {
                supportFragmentManager.beginTransaction().add(R.id.map_fragment, it).commit()
            }

        // 지도가 준비되면 onMapReady 실행
        mapFragment.getMapAsync(this)

        // 캡처 후 공유 버튼 (activity_main.xml에 해당 id의 버튼이 있어야 함)
        // findViewById<Button>(R.id.btn_share_map).setOnClickListener {
        //     captureAndShare()
        // }
    }

    // 위치 권한 확인 함수
    private fun checkLocationPermission() {
        val finePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarsePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)

        if (finePermission == PackageManager.PERMISSION_GRANTED || coarsePermission == PackageManager.PERMISSION_GRANTED) {
            // 이미 권한이 있는 경우
            getCurrentLocation()
        } else {
            // 권한이 없으면 사용자에게 권한 요청 팝업 띄우기
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // 실제 GPS 현재 위치(위도, 경도) 가져오기 함수
    private fun getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val currentLat = location.latitude  // 현재 위도
                    val currentLon = location.longitude // 현재 경도

                    Toast.makeText(this, "현재 위치: $currentLat, $currentLon", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "현재 위치 정보를 가져올 수 없습니다. GPS를 켜주세요.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // 지도가 준비되면 호출되는 콜백 (하나만 존재해야 함)
    override fun onMapReady(naverMap: NaverMap) {
        this.naverMap = naverMap

        // 지도에 위치 소스 연결
        naverMap.locationSource = locationSource

        // 현재 위치 버튼 활성화 및 위치 추적 모드 설정
        val uiSettings = naverMap.uiSettings
        uiSettings.isLocationButtonEnabled = true
        naverMap.locationTrackingMode = LocationTrackingMode.Follow

        // 카메라가 움직일 때마다 그 위치의 날씨만 갱신 (마커 로딩은 여기서 하지 않음)
        naverMap.addOnCameraChangeListener { _, _ ->
            val center = naverMap.cameraPosition.target
            fetchWeather(center.latitude, center.longitude)
        }

        // 지도 클릭 시 핀 표시 + RecordActivity로 좌표 전달
        naverMap.setOnMapClickListener { _, coord ->
            currentMarker?.map = null

            val marker = Marker()
            marker.position = coord
            marker.map = naverMap
            currentMarker = marker

            val intent = Intent(this, RecordActivity::class.java)
            intent.putExtra(IntentKeys.EXTRA_LATITUDE, coord.latitude)
            intent.putExtra(IntentKeys.EXTRA_LONGITUDE, coord.longitude)
            startActivity(intent)
        }

        // 지도가 준비된 직후 딱 한 번만 마커 로딩 (카메라 이동 리스너 안에 넣지 않기!)
        loadPhotoMarkersFromDb()
        addAttractionMarkers(
            listOf(
                // TODO: 실제 명소 목록으로 교체
                Attraction("경복궁", 37.5796, 126.9770),
                Attraction("남산타워", 37.5512, 126.9882)
            )
        )
    }

    // 위치 권한 승인/거부 결과 처리
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (locationSource.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
            if (!locationSource.isActivated) { // 권한 거부 시 추적 끄기
                naverMap.locationTrackingMode = LocationTrackingMode.None
            }
            return
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun fetchWeather(lat: Double, lon: Double) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.weatherApi.getCurrentWeather(
                    lat = lat,
                    lon = lon,
                    apiKey = "ead6912db6b53ee0abad75f8b60d1900"
                )
                val temp = response.main.temp
                val desc = response.weather.firstOrNull()?.description
                // 예: "서울 / 23.5도 / 구름 조금"
                println("${response.name} / ${temp}도 / $desc")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun fetchWeeklyWeather(lat: Double, lon: Double) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.weatherApi.getForecast(
                    lat = lat,
                    lon = lon,
                    apiKey = "ead6912db6b53ee0abad75f8b60d1900"
                )
                val dailyList = response.toDailyForecasts(days = 3)
                dailyList.forEach {
                    println("${it.date} / 최저 ${it.minTemp}도 ~ 최고 ${it.maxTemp}도 / ${it.description}")
                }
                // TODO: 체크리스트 로직에 dailyList 넘기기
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // 지도 캡처 (단독 저장만 하고 싶을 때 사용)
    private fun captureMapSnapshot() {
        naverMap.takeSnapshot { bitmap ->
            saveBitmapToGallery(bitmap)
        }
    }

    // 갤러리 저장
    private fun saveBitmapToGallery(bitmap: Bitmap): Uri? {
        val filename = "travelmap_${System.currentTimeMillis()}.jpg"
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/TravelMap")
        }

        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        uri?.let {
            contentResolver.openOutputStream(it)?.use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            }
            Toast.makeText(this, "갤러리에 저장되었습니다.", Toast.LENGTH_SHORT).show()
        }
        return uri
    }

    // 공유 기능
    private fun shareImage(uri: Uri) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(shareIntent, "공유하기"))
    }

    // 캡처 -> 저장 -> 공유를 한 번에 실행
    private fun captureAndShare() {
        naverMap.takeSnapshot { bitmap ->
            val uri = saveBitmapToGallery(bitmap)
            uri?.let { shareImage(it) }
        }
    }

    // 명소 핀 표시
    private fun addAttractionMarkers(attractions: List<Attraction>) {
        attractions.forEach { spot ->
            val marker = Marker()
            marker.position = LatLng(spot.lat, spot.lon)
            marker.captionText = spot.name          // 핀 아래 이름 표시
            marker.iconTintColor = android.graphics.Color.RED  // 명소는 빨간색으로 구분
            marker.map = naverMap
        }
    }

    // Room DB에서 사용자가 등록한 기록(사진 포함) 불러오기
    // TODO: travelRecordDao(), getAllRecords(), record.latitude/longitude/photoUri는
    //       실제 TravelRecordDao / TravelRecord 파일의 함수명/필드명으로 교체하세요.
    private fun loadPhotoMarkersFromDb() {
        lifecycleScope.launch {
            val records = withContext(Dispatchers.IO) {
                AppDatabase.getDatabase(applicationContext).travelRecordDao().getAll()
            }
            records.forEach { record ->
                addPhotoMarker(record.latitude, record.longitude, record.imageUri)
            }
        }
    }

    // 사용자 등록 사진을 마커 아이콘으로 표시
    private fun addPhotoMarker(lat: Double, lon: Double, photoUri: String?) {
        val marker = Marker()
        marker.position = LatLng(lat, lon)

        if (!photoUri.isNullOrEmpty()) {
            try {
                val bitmap = contentResolver.openInputStream(Uri.parse(photoUri))?.use {
                    BitmapFactory.decodeStream(it)
                }
                bitmap?.let {
                    val resized = Bitmap.createScaledBitmap(it, 120, 120, false)
                    marker.icon = OverlayImage.fromBitmap(resized)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // 사진 로드 실패 시 기본 핀으로 표시
            }
        }
        marker.map = naverMap
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }
}