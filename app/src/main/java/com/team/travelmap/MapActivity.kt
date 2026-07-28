package com.team.travelmap

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.FusedLocationSource
import com.team.travelmap.weather.RetrofitClient
import kotlinx.coroutines.launch
import com.damyeoom.app.R

// 네이버 지도와 위치 기능을 제공하는 Activity
class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    // 현재 위치를 지도에 연결하는 객체
    private lateinit var locationSource: FusedLocationSource

    // 네이버 지도 객체
    private lateinit var naverMap: NaverMap

    // 기기의 현재 위치를 가져오는 객체
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // 사용자가 선택한 위치 마커
    private var currentMarker: Marker? = null

    // DB에서 불러온 여행 기록 마커 목록
    private val recordMarkers = mutableListOf<Marker>()

    // 위치 권한 요청 결과 처리
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->

            val fineLocationGranted =
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false

            val coarseLocationGranted =
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

            if (fineLocationGranted || coarseLocationGranted) {
                getCurrentLocation()
            } else {
                Toast.makeText(
                    this,
                    "위치 권한이 거부되어 현재 위치를 불러올 수 없습니다.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 지도 화면 레이아웃 설정
        setContentView(R.layout.activity_main)

        // 현재 위치 서비스 초기화
        fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(this)

        checkLocationPermission()

        locationSource =
            FusedLocationSource(
                this,
                LOCATION_PERMISSION_REQUEST_CODE
            )

        // 네이버 지도 Fragment 생성
        val mapFragment =
            supportFragmentManager
                .findFragmentById(R.id.map_fragment) as MapFragment?
                ?: MapFragment.newInstance().also {
                    supportFragmentManager
                        .beginTransaction()
                        .add(R.id.map_fragment, it)
                        .commit()
                }

        mapFragment.getMapAsync(this)
    }

    // 위치 권한 보유 여부 확인
    private fun checkLocationPermission() {
        val finePermission =
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )

        val coarsePermission =
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )

        if (
            finePermission == PackageManager.PERMISSION_GRANTED ||
            coarsePermission == PackageManager.PERMISSION_GRANTED
        ) {
            getCurrentLocation()
        } else {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // 기기의 현재 위치 조회
    private fun getCurrentLocation() {
        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->

                    if (location != null) {
                        Toast.makeText(
                            this,
                            "현재 위치: ${location.latitude}, ${location.longitude}",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this,
                            "현재 위치 정보를 가져올 수 없습니다. GPS를 켜주세요.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }

    // 네이버 지도 준비 완료 후 실행
    override fun onMapReady(naverMap: NaverMap) {
        this.naverMap = naverMap

        // 처음에는 대한민국 전체가 보이도록 설정
        naverMap.cameraPosition =
            com.naver.maps.map.CameraPosition(
                LatLng(36.5, 127.8),
                6.5
            )

        // 현재 위치 기능 연결
        naverMap.locationSource = locationSource

        val uiSettings = naverMap.uiSettings
        uiSettings.isLocationButtonEnabled = true

        naverMap.locationTrackingMode =
            LocationTrackingMode.Follow

        // 지도 중심이 바뀌면 해당 위치의 날씨 조회
        naverMap.addOnCameraChangeListener { _, _ ->
            val center = naverMap.cameraPosition.target

            fetchWeather(
                center.latitude,
                center.longitude
            )
        }

        // 지도 클릭 시 마커를 추가하고 기록 화면으로 이동
        naverMap.setOnMapClickListener { _, coord ->
            currentMarker?.map = null

            val marker = Marker()
            marker.position = coord
            marker.map = naverMap
            currentMarker = marker

            val intent =
                Intent(
                    this,
                    RecordActivity::class.java
                )

            intent.putExtra(
                IntentKeys.EXTRA_LATITUDE,
                coord.latitude
            )

            intent.putExtra(
                IntentKeys.EXTRA_LONGITUDE,
                coord.longitude
            )

            startActivity(intent)
        }

        // 여행 기록과 추천 장소 마커 표시
        refreshRecordMarkers()
        addAttractionMarkers(naverMap)
    }

    // 화면으로 돌아올 때 마커 다시 조회
    override fun onResume() {
        super.onResume()

        if (::naverMap.isInitialized) {
            refreshRecordMarkers()
        }
    }

    // DB의 여행 기록 마커 갱신
    private fun refreshRecordMarkers() {
        recordMarkers.forEach {
            it.map = null
        }

        recordMarkers.clear()

        loadPhotoMarkersFromDb(
            naverMap,
            applicationContext,
            lifecycleScope
        ) { markers ->
            recordMarkers.addAll(markers)
        }
    }

    // 위치 권한 요청 결과를 지도 SDK에 전달
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (
            locationSource.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
            )
        ) {
            if (!locationSource.isActivated) {
                naverMap.locationTrackingMode =
                    LocationTrackingMode.None
            }

            return
        }

        super.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults
        )
    }

    // 선택 위치의 현재 날씨 조회
    private fun fetchWeather(
        lat: Double,
        lon: Double
    ) {
        lifecycleScope.launch {
            try {
                val response =
                    RetrofitClient.weatherApi
                        .getCurrentWeather(
                            lat = lat,
                            lon = lon,
                            apiKey = "ead6912db6b53ee0abad75f8b60d1900"
                        )

                val temp = response.main.temp
                val desc =
                    response.weather
                        .firstOrNull()
                        ?.description

                println(
                    "${response.name} / ${temp}도 / $desc"
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // 현재 지도를 이미지로 저장
    private fun captureMapSnapshot() {
        naverMap.takeSnapshot { bitmap ->
            saveBitmapToGallery(bitmap)
        }
    }

    // 지도 이미지를 갤러리에 저장
    private fun saveBitmapToGallery(
        bitmap: Bitmap
    ): Uri? {
        val filename =
            "travelmap_${System.currentTimeMillis()}.jpg"

        val contentValues =
            ContentValues().apply {
                put(
                    MediaStore.Images.Media.DISPLAY_NAME,
                    filename
                )
                put(
                    MediaStore.Images.Media.MIME_TYPE,
                    "image/jpeg"
                )
                put(
                    MediaStore.Images.Media.RELATIVE_PATH,
                    Environment.DIRECTORY_PICTURES + "/TravelMap"
                )
            }

        val uri =
            contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )

        uri?.let {
            contentResolver
                .openOutputStream(it)
                ?.use { outputStream ->
                    bitmap.compress(
                        Bitmap.CompressFormat.JPEG,
                        100,
                        outputStream
                    )
                }

            Toast.makeText(
                this,
                "갤러리에 저장되었습니다.",
                Toast.LENGTH_SHORT
            ).show()
        }

        return uri
    }

    // 저장된 이미지를 다른 앱으로 공유
    private fun shareImage(uri: Uri) {
        val shareIntent =
            Intent(Intent.ACTION_SEND).apply {
                type = "image/jpeg"

                putExtra(
                    Intent.EXTRA_STREAM,
                    uri
                )

                addFlags(
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }

        startActivity(
            Intent.createChooser(
                shareIntent,
                "공유하기"
            )
        )
    }

    // 지도 화면을 저장한 뒤 바로 공유
    private fun captureAndShare() {
        naverMap.takeSnapshot { bitmap ->
            val uri =
                saveBitmapToGallery(bitmap)

            uri?.let {
                shareImage(it)
            }
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE =
            1000
    }
}