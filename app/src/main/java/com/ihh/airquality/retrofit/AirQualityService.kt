package com.ihh.airquality.retrofit

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface AirQualityService {
    //위도, 경도를 기반으로 미세먼지 농도를 가져올 것이므로 GET 메소드만 구현해주면 됨
    //Call : retrofit에서 요청을 처리하는 객체

    @GET("nearest_city")
    fun getAirQualityData(@Query("lat") lat : String, @Query("lon") lon : String, @Query("key") key : String) : Call<AirQualityResponse>


}