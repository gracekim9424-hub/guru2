package com.example.app

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.app.data.database.AppDatabase
import com.example.app.entity.TravelRecord
import kotlinx.coroutines.launch
import com.example.app.entity.PlaceEntity

class MainActivity : AppCompatActivity() {

    private var editingRecord: TravelRecord? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )
            insets
        }

        val spinnerRegion = findViewById<Spinner>(R.id.spinnerRegion)
        val editVisitDate = findViewById<EditText>(R.id.editVisitDate)
        val editMemo = findViewById<EditText>(R.id.editMemo)
        val btnSave = findViewById<Button>(R.id.btnSave)
        val layoutRecords = findViewById<LinearLayout>(R.id.layoutRecords)

        val regions = listOf(
            "서울", "경기", "인천", "강원",
            "충북", "충남", "대전", "세종",
            "전북", "전남", "광주",
            "경북", "경남", "대구", "부산", "울산",
            "제주"
        )

        spinnerRegion.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            regions
        )

        val db = AppDatabase.getDatabase(this)
        val travelDao = db.travelRecordDao()
        val placeDao = db.placeDao()

        lifecycleScope.launch {
            if (placeDao.getPlaceCount() == 0) {
                val samplePlaces = listOf(
                    PlaceEntity(
                        placeId = 1,
                        region = "서울",
                        category = "명소",
                        name = "경복궁",
                        address = "서울 종로구 사직로 161",
                        description = "조선 시대의 대표적인 궁궐",
                        latitude = 37.5796,
                        longitude = 126.9770,
                        imageUrl = null
                    ),
                    PlaceEntity(
                        placeId = 2,
                        region = "서울",
                        category = "먹거리",
                        name = "광장시장",
                        address = "서울 종로구 창경궁로 88",
                        description = "다양한 전통 먹거리를 즐길 수 있는 시장",
                        latitude = 37.5700,
                        longitude = 126.9996,
                        imageUrl = null
                    ),
                    PlaceEntity(
                        placeId = 3,
                        region = "서울",
                        category = "놀거리",
                        name = "롯데월드",
                        address = "서울 송파구 올림픽로 240",
                        description = "실내외 놀이시설이 있는 테마파크",
                        latitude = 37.5111,
                        longitude = 127.0982,
                        imageUrl = null
                    )
                )

                placeDao.insertAll(samplePlaces)
            }
        }

        fun loadRecords() {
            lifecycleScope.launch {
                val records = travelDao.getAllRecords()

                layoutRecords.removeAllViews()

                records.forEach { record ->
                    val row = LinearLayout(this@MainActivity).apply {
                        orientation = LinearLayout.HORIZONTAL
                        setPadding(0, 12, 0, 12)
                    }

                    val recordText = TextView(this@MainActivity).apply {
                        text = "${record.region} / ${record.visitDate}\n${record.memo}"
                        textSize = 16f
                        layoutParams = LinearLayout.LayoutParams(
                            0,
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            1f
                        )
                    }

                    val editButton = Button(this@MainActivity).apply {
                        text = "수정"

                        setOnClickListener {
                            editingRecord = record

                            val regionIndex = regions.indexOf(record.region)
                            if (regionIndex >= 0) {
                                spinnerRegion.setSelection(regionIndex)
                            }

                            editVisitDate.setText(record.visitDate)
                            editMemo.setText(record.memo)
                            btnSave.text = "수정 완료"
                        }
                    }

                    val deleteButton = Button(this@MainActivity).apply {
                        text = "삭제"

                        setOnClickListener {
                            lifecycleScope.launch {
                                travelDao.delete(record)

                                if (editingRecord?.id == record.id) {
                                    editingRecord = null
                                    btnSave.text = "저장"
                                    editVisitDate.text.clear()
                                    editMemo.text.clear()
                                    spinnerRegion.setSelection(0)
                                }

                                Toast.makeText(
                                    this@MainActivity,
                                    "여행 기록이 삭제되었습니다.",
                                    Toast.LENGTH_SHORT
                                ).show()

                                loadRecords()
                            }
                        }
                    }

                    row.addView(recordText)
                    row.addView(editButton)
                    row.addView(deleteButton)
                    layoutRecords.addView(row)
                }
            }
        }

        btnSave.setOnClickListener {
            val region = spinnerRegion.selectedItem.toString()
            val visitDate = editVisitDate.text.toString().trim()
            val memo = editMemo.text.toString().trim()

            if (visitDate.isBlank()) {
                Toast.makeText(
                    this,
                    "방문 날짜를 입력해주세요.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val currentRecord = editingRecord

                if (currentRecord == null) {
                    travelDao.insert(
                        TravelRecord(
                            region = region,
                            visitDate = visitDate,
                            memo = memo,
                            imageUri = null,
                            latitude = null,
                            longitude = null
                        )
                    )

                    Toast.makeText(
                        this@MainActivity,
                        "여행 기록이 저장되었습니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    travelDao.update(
                        currentRecord.copy(
                            region = region,
                            visitDate = visitDate,
                            memo = memo
                        )
                    )

                    editingRecord = null
                    btnSave.text = "저장"

                    Toast.makeText(
                        this@MainActivity,
                        "여행 기록이 수정되었습니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                editVisitDate.text.clear()
                editMemo.text.clear()
                spinnerRegion.setSelection(0)

                loadRecords()
            }
        }

        loadRecords()
    }
}