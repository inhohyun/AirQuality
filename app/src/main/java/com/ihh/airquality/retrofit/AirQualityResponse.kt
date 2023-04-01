package com.ihh.airquality.retrofit

data class AirQualityResponse(
    //서버에서 받은 데이터를 데이터클래스에 담아서 직접 사용
    val `data`: Data,
    val status: String
) {
    data class Data(
        val city: String,
        val country: String,
        val current: Current,
        val location: Location,
        val state: String
    ) {
        data class Current(
            val pollution: Pollution,
            val weather: Weather
        ) {
            data class Pollution(
                val aqicn: Int,
                val aqius: Int,
                val maincn: String,
                val mainus: String,
                val ts: String
            )

            data class Weather(
                val hu: Int,
                val ic: String,
                val pr: Int,
                val tp: Int,
                val ts: String,
                val wd: Int,
                val ws: Double
            )
        }

        data class Location(
            val coordinates: List<Double>,
            val type: String
        )
    }
}