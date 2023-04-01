package com.ihh.airquality.retrofit

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitConnection {
    companion object{
        private const val BASE_URL = "https://api.airvisual.com/v2/"
        private var INSTANCE: Retrofit? = null

        fun getInstance(): Retrofit{
            if(INSTANCE == null){
                //converterFactory란 : 서버에서 온 json 응답을 만든 data class 객체로 자동으로 변환해줌(gson이 이에 해당함)
                INSTANCE = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            }

            //이미 INSTANCE 객체가 있으면 바로 반환, NonNullalble이니깐 !!처리
           return INSTANCE!!
        }
    }
}