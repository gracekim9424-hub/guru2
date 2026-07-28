package com.team.travelmap.weather

// 화면에 표시할 날짜별 날씨 정보
data class DailyForecast(
    val date: String,
    val minTemp: Double,
    val maxTemp: Double,
    val description: String
)

// 시간대별 예보를 날짜별 예보로 변환
fun ForecastResponse.toDailyForecasts(
    days: Int = 3
): List<DailyForecast> {
    return list
        // 같은 날짜의 예보끼리 묶기
        .groupBy {
            it.dt_txt.substring(0, 10)
        }
        .entries

        // 요청한 날짜 수만 사용
        .take(days)

        // 날짜별 최저·최고 기온 계산
        .map { (date, items) ->
            DailyForecast(
                date = date,

                minTemp =
                    items.minOf {
                        it.main.temp_min
                    },

                maxTemp =
                    items.maxOf {
                        it.main.temp_max
                    },

                // 정오 날씨를 대표 날씨로 사용
                description =
                    items.find {
                        it.dt_txt.contains(
                            "12:00:00"
                        )
                    }
                        ?.weather
                        ?.firstOrNull()
                        ?.description
                        ?: items
                            .first()
                            .weather
                            .firstOrNull()
                            ?.description
                        ?: ""
            )
        }
}