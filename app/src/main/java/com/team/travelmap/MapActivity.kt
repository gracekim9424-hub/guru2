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
import com.damyeoom.app.data.database.AppDatabase
import com.damyeoom.app.data.decodePhotos
import com.damyeoom.app.R

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var locationSource: FusedLocationSource
    private lateinit var naverMap: NaverMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentMarker: Marker? = null
    private val recordMarkers = mutableListOf<Marker>()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineLocationGranted || coarseLocationGranted) {
            getCurrentLocation()
        } else {
            Toast.makeText(this, "위치 권한이 거부되어 현재 위치를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        checkLocationPermission()
        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map_fragment) as MapFragment?
            ?: MapFragment.newInstance().also {
                supportFragmentManager.beginTransaction().add(R.id.map_fragment, it).commit()
            }

        mapFragment.getMapAsync(this)
    }

    private fun checkLocationPermission() {
        val finePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarsePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)

        if (finePermission == PackageManager.PERMISSION_GRANTED || coarsePermission == PackageManager.PERMISSION_GRANTED) {
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

    private fun getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    Toast.makeText(this, "현재 위치: ${location.latitude}, ${location.longitude}", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "현재 위치 정보를 가져올 수 없습니다. GPS를 켜주세요.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onMapReady(naverMap: NaverMap) {
        this.naverMap = naverMap

        // 처음에 한반도 전체가 보이도록
        naverMap.cameraPosition = com.naver.maps.map.CameraPosition(
            com.naver.maps.geometry.LatLng(36.5, 127.8),
            6.5
        )

        naverMap.locationSource = locationSource
        val uiSettings = naverMap.uiSettings
        uiSettings.isLocationButtonEnabled = true
        naverMap.locationTrackingMode = LocationTrackingMode.Follow

        naverMap.addOnCameraChangeListener { _, _ ->
            val center = naverMap.cameraPosition.target
            fetchWeather(center.latitude, center.longitude)
        }

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

        refreshRecordMarkers()
        addAttractionMarkers(naverMap)
    }

    override fun onResume() {
        super.onResume()
        if (::naverMap.isInitialized) {
            refreshRecordMarkers()
        }
    }

    private fun refreshRecordMarkers() {
        recordMarkers.forEach { it.map = null }
        recordMarkers.clear()
        loadPhotoMarkersFromDb(naverMap, applicationContext, lifecycleScope) { markers ->
            recordMarkers.addAll(markers)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (locationSource.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
            if (!locationSource.isActivated) {
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
                println("${response.name} / ${temp}도 / $desc")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun captureMapSnapshot() {
        naverMap.takeSnapshot { bitmap ->
            saveBitmapToGallery(bitmap)
        }
    }

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

    private fun shareImage(uri: Uri) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(shareIntent, "공유하기"))
    }

    private fun captureAndShare() {
        naverMap.takeSnapshot { bitmap ->
            val uri = saveBitmapToGallery(bitmap)
            uri?.let { shareImage(it) }
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }
}