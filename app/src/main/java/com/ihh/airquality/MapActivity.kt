package com.ihh.airquality

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
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


        setButton()
    }
    private fun setButton(){
        binding.btnCheckHere.setOnClickListener {
            mMap?.let {
                val intent = Intent() //데이터를 넣기 위한 intent


                intent.putExtra("latitude", it.cameraPosition.target.latitude) // 카메라에 따른 위도 정보 보내기
                intent.putExtra("longitude", it.cameraPosition.target.longitude) // 카메라에 따른 경도 정보 보내기
                setResult(Activity.RESULT_OK, intent) //MainActivity에서 result가 RESULT_OK(RESULT_CANCELED 등으로 보내면 안됨)여야 정보를 받아오는 것으로 처리했으므로 RESULT_OK와 intent의 정보를 result로 보낸다.
                // mapAcitivuty를 끄기만 해도 backStack에 있는 MainActivity로 이동할 것이기 때문에 따로 intent할 필요 없음, A -> B -> A(x)
                finish()
            }
        }

        //현재위치로 이동하는 버튼 구현
        binding.fabCurrentLocation.setOnClickListener {
            val locationProvider = LocationProvider(this@MapActivity)

            //locationProvider를 이용해 현재 위도, 경도 값 가져오기
            val latitude = locationProvider.getLocationLatitude()
            val longitude = locationProvider.getLocationLongitude()

            //현재 위치로 카메라 이동시키기기
           mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(latitude!!, longitude!!), 16f))
            setMarker()
        }
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
            setMarker()
        }
    }

    private fun setMarker(){
        //지도가 있어야 마커를 실행할 수 있기 때문에 null이 아닐때문 실행하도록 설정
        mMap?.let{
            //지도 위의 마커를 모두 지워주고 시작(내가 설정한 위치만 뜨도록 초기화)
            it.clear()

            val markerOption = MarkerOptions()
            //카메라의 위치에 마커 띄우기
            markerOption.position(it.cameraPosition.target)
            markerOption.title("마커 위치")
            //마커를 변수화
            val marker = it.addMarker(markerOption)
            //사용자에 따른 카메라의 이동에 마커도 반응하도록 설정
            it.setOnCameraIdleListener {
            marker?.let { marker ->
                //이때 it은 mMap을 뜻함(람다식 활용)
               marker.position = it.cameraPosition.target
            }

            }
        }
    }
}