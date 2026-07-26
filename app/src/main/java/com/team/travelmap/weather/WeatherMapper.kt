package com.team.travelmap.weather

data class DailyForecast(
    val date: String,
    val minTemp: Double,
    val maxTemp: Double,
    val description: String
)

fun ForecastResponse.toDailyForecasts(days: Int = 3): List<DailyForecast> {
    return list
        .groupBy { it.dt_txt.substring(0, 10) } // "2026-07-24"
        .entries
        .take(days)
        .map { (date, items) ->
            DailyForecast(
                date = date,
                minTemp = items.minOf { it.main.temp_min },
                maxTemp = items.maxOf { it.main.temp_max },
                // 정오(12:00) 근처 데이터를 대표 날씨로 사용
                description = items.find { it.dt_txt.contains("12:00:00") }?.weather?.firstOrNull()?.description
                    ?: items.first().weather.firstOrNull()?.description ?: ""
            )
        }
}