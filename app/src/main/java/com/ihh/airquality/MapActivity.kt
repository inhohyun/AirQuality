package com.ihh.airquality

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.ihh.airquality.databinding.ActivityMapBinding

//onMapReadyCallback : 지도가 준비되었을 때 실행되는 콜백
class MapActivity : AppCompatActivity() , OnMapReadyCallback{
    lateinit var binding : ActivityMapBinding
    private var mMap : GoogleMap? = null
    var currentLat : Double = 0.0
    var currentLng : Double = 0.0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //MainActivity에서 putExtra로 넘겨준 데이터를 받기
        currentLat = intent.getDoubleExtra("currentLat", 0.0)
        currentLng = intent.getDoubleExtra("currentLng", 0.0)

        //mapActivity에 만든 Fragment의 변수 만들어주기기
       val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?

        //callback을 this로 전달할 수 있는 이유? : MapActivity에서 onMapReadyCallback 인터페이스가 구현되어있기 때문
        //getMapAsync : 지도가 준비되면 callback이 실행될 수 있도록 비동기적으로 실행해주는 것(결국 지도를 불러오는 역할)
        mapFragment?.getMapAsync(this)
    }

    //지도가 준비되면 onMapReady 함수가 실행됨됨
   override fun onMapReady(googleMap: GoogleMap) {

       mMap = googleMap

        //mMap이 null이 아니면 실행
        mMap?.let {
            //위도와 경도에 따른 현재 위치를 반환
            val currentLocation = LatLng(currentLat, currentLng)
            //줌 기능을 어디까지 가능하게 할 것인가
            it.setMaxZoomPreference(20.0f)
            it.setMinZoomPreference(12.0f)
            //현재 위치로 카메라 이동시키기
            it.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 16f))
        }
    }
}