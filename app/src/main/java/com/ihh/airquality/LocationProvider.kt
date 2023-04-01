package com.ihh.airquality

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.app.ActivityCompat

//gps나 네트워크 위치를 사용해서 위도와 경도를 가져오는 것이 목적
//MainAcitivy에서 context를 인수로 받아옴
class LocationProvider (val context : Context){
    //위도와 경도를 담는 객체
    private var location : Location? = null
    //locationManager : 시스템 위치 서비스에 접근을 제공하는 클래스
    private var locationManager : LocationManager? = null

    init {
        getLocation()
    }

    private fun getLocation() : Location?{

        try {
            locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

            var gpsLocation : Location? = null
            var networkLocation : Location? = null

            //GPS or Network 가 활성화 되었는지 확인

            val isGPSEnabled = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val isNetworkEnabled = locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

            //둘 다 활성화되지 않았다면 null 반환하기기
           if (!isGPSEnabled && !isNetworkEnabled){
                return null
            }
            else{
                //네트워크가 활성화된 경우엔 네트워크를 이용해서 위치를 가져오기
                //mainAcitivity에서 작성했던 것처럼 fine도 아니고 coarse도 아니면 null을 리턴함
               if (ActivityCompat.checkSelfPermission(
                       context,
                       Manifest.permission.ACCESS_FINE_LOCATION
                   ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                       context,
                       Manifest.permission.ACCESS_COARSE_LOCATION
                   ) != PackageManager.PERMISSION_GRANTED
               ) {
                   return null
               }
                if (isNetworkEnabled){
                    networkLocation = locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                }
               if (isGPSEnabled){
                   gpsLocation =locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
               }
               //gps, network 둘 다 활성화가 되어있다면 더 정확한 쪽을 가져옴
               if (gpsLocation != null && networkLocation != null){
                   //gps의 정확도가 더 높으면 gps의 위치를 리턴
                    if(gpsLocation.accuracy > networkLocation.accuracy){
                        location = gpsLocation
                    }else{
                        location = networkLocation

                    }

               }
               //하나만 권한이 있는 경우
               else{
                    if (gpsLocation != null){
                        location = gpsLocation
                    }
                   if (networkLocation != null){
                       location = networkLocation
                   }
               }
           }
            //locationManager가 위의 캐스터 문에서 가져오지 못했다면 catch문으로 빠짐, 에러문 출력
        }catch(e : java.lang.Exception){
            e.printStackTrace()
        }

        return location
    }
        //location을 기반으로 위도를 가져오는 함수
    fun getLocationLatitude(): Double?{

            //location의 값이 null일경우 위도, 경도도 NULL이 들어옴
        return location?.latitude
    }
        //loction을 기반으로 경도를 가져오는 함수
    fun getLocationLongitude() : Double?{
        return location?.longitude
    }

}