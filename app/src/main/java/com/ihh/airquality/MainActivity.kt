package com.ihh.airquality

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.ihh.airquality.databinding.ActivityMainBinding
import com.ihh.airquality.retrofit.AirQualityResponse
import com.ihh.airquality.retrofit.AirQualityService
import com.ihh.airquality.retrofit.RetrofitConnection
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    lateinit var locationProvider : LocationProvider
    //권한을 요청받을 상수값
    private val PERMISSION_REQUEST_CODE = 100
    val REQUIRED_PERMISSIONS = arrayOf(
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION
    )

    lateinit var getGPSPermissionLauncher: ActivityResultLauncher<Intent>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkAllPermissions()
        updateUI()

    }

    private fun updateUI(){
        //MainActivity의 context를 넣어서 이를 기반으로 만들어둔 LocationProvider 클래스의 함수를 활용해서 위도, 경도를 저장
        locationProvider = LocationProvider(this@MainActivity)

        //위도, 경도 정보 가져오기
        val latitude : Double? = locationProvider.getLocationLatitude()
        val longtitude : Double? = locationProvider.getLocationLongitude()

        if (latitude != null && longtitude != null){
            //1. 현재 위치를 가져오고 UI를 업데이트
            val address = getCurrentAddress(latitude, longtitude)
            //address가 null이 아닌 경우에 실행
            address?.let {
                //thoroughfare : 역삼1동 2동 등의 주소
                //countryName : 나라 이름
                binding.tvLocationTitle.text = "${it.thoroughfare}"
                binding.tvLocationSubtitle.text = "${it.countryName} ${it.adminArea}"
            }
            //2. 미세먼지 농도를 가져오고 UI를 업데이트
            getAirQualityData(latitude, longtitude)

        }
        //위도, 경도 둘 중 하나라도 null 값이면
        else{
            Toast.makeText(this, "위도, 경도 정보를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show()
        }

    }
    private fun getAirQualityData(latitude: Double, longitude: Double){
        var retrofitAPI = RetrofitConnection.getInstance().create(
            //AirQualityService의 구현체?를 retrofit이 만들어줌
            AirQualityService::class.java
        )

        retrofitAPI.getAirQualityData(
            latitude.toString(),
            longitude.toString(),
            "ab668cb6-f7ea-4beb-8a3b-6e56bea7b7ff"
        ).enqueue(object : Callback<AirQualityResponse>{
            //onResponse : response가 왔을 때 실행되는 함수
            override fun onResponse(
                call: Call<AirQualityResponse>,
                response: Response<AirQualityResponse>
            ) {
                if (response.isSuccessful){
                    Toast.makeText(this@MainActivity, "최신 데이터 업데이트 완료!", Toast.LENGTH_SHORT).show()
                    //가져온 데이터 보기, 가져온 데이터가 null이 아니면 ui에 업데이트
                    response.body()?.let {
                        updateAirUI(it)
                    }

                }else{
                    Toast.makeText(this@MainActivity, "데이터를 가져오는 데 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
            //데이터 호출을 실패했을 때 호출할 함수
            override fun onFailure(call: Call<AirQualityResponse>, t: Throwable) {
                Toast.makeText(this@MainActivity, "데이터를 가져오는 데 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
        )
        //Call : retrofit에서 요청을 처리하는 객체
        //처리하는 방식 -> execute() : 동기 실행, enqueue() : 비동기 실행

    }
    private fun updateAirUI(airQualityData : AirQualityResponse){
        val pollutionData = airQualityData.data.current.pollution

        //수치를 지정
        binding.tvCount.text = pollutionData.aqius.toString()

        //측정된 날짜
        val dateTime = ZonedDateTime.parse(pollutionData.ts).withZoneSameInstant(ZoneId.of("Asia/Seoul")).toLocalDateTime()
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

        binding.tvCheckTime.text = dateTime.format(dateFormatter).toString()

        when(pollutionData.aqius){
            in 0..50->{
                binding.tvTitle.text = "좋음"
                binding.imgBg.setImageResource(R.drawable.bg_good)
            }
            in 51..150->{
                binding.tvTitle.text = "보통"
                binding.imgBg.setImageResource(R.drawable.bg_soso)
            }
            in 151..200->{
                binding.tvTitle.text = "나쁨"
                binding.imgBg.setImageResource(R.drawable.bg_bad)
            }
            else ->{
                binding.tvTitle.text = "매우 나쁨"
                binding.imgBg.setImageResource(R.drawable.bg_worst)
            }

        }
    }

    //지오코딩 : 위도와 경도를 가지고 지명 이름 가져오기
    private fun getCurrentAddress(latitude: Double, longitude:Double) : Address? {
        //지오코더 객체를 생성, getDefault를 활용해 휴대폰의 디폴트 위치를 가져옴(대한민국) -> 헷갈리니 그냥 korea로 변경
        val geoCoder = Geocoder(this, Locale.KOREA)
        //지오코딩을 활용해 위치정보의 지명을 가져올 때는 여러 개를 받음 -> List로 저장
        val addresses : List<Address>?
        //위도, 경도 사용해서 지명 가져오기, 최대 7개까지만 받아오기 -> 어차피 하나만 쓸꺼긴 함
        addresses = try {
            geoCoder.getFromLocation(latitude,longitude, 7)
        } catch (ioException : IOException){
            //값이 들어오지 않은 경우
            Toast.makeText(this, "지오코더 서비스를 이용불가 합니다.", Toast.LENGTH_SHORT).show()
            return null
        } catch (illegalArgumentException : java.lang.IllegalArgumentException){
            //들어온 값이 잘못된 경우
            Toast.makeText(this, "잘못된 위도, 경도 입니다.", Toast.LENGTH_SHORT).show()
            return null
        }

        //위도, 경도 값에는 문제가 없으나 주소가 없는 경우
        if(addresses == null || addresses.size == 0){
            Toast.makeText(this, "주소가 발견되지 않았습니다.", Toast.LENGTH_SHORT).show()
            return null

        }


        return addresses[0]
    }


    private fun checkAllPermissions() {
        //GPS가 꺼져있을 때 함수호출
        if (!isLocationServicesAvailable()) {
            //사용자에게 GPS를 켜달라는 함수
            showDialogForLocationServicesSetting()
        }
        //GPS가 켜져있을때 함수호출
        else {
            isRunTimePermissionsGranted()
        }

    }

    private fun isLocationServicesAvailable(): Boolean {
        //system에서 제공하는 Location서비스를 가져오는데, 이것을 LocationManager로써 사용한다.
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        //GPS위치 권한이나 네트워크 위치 권한이 Enabled 되어있다면 true를 반환함
        return (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        ))
    }

    private fun isRunTimePermissionsGranted() {
        //permission이 있는지 없는지를 확인
        //CoarseLocation은 대략적인 위치를, FineLocation은 정확한 위치를 줌
        //권한이 있는지를 확인하여 bool값을 변수에 저장
        val hasFineLocationPermisson = ContextCompat.checkSelfPermission(
            this@MainActivity,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        )
        val hasCoarseLocationPermisson = ContextCompat.checkSelfPermission(
            this@MainActivity,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )

        //위의 위치 권한이 둘 중하나라도 없으면 사용자에게 다이얼로그를 활용해 권한 요청하는 창이 뜸뜸
        if (hasFineLocationPermisson != PackageManager.PERMISSION_GRANTED || hasCoarseLocationPermisson != PackageManager.PERMISSION_GRANTED) {
            //PERMISSION_REQUEST_CODE를 쓰는 이유 : response를 받아올 때, 어떤 request였는지 id 값을 줌
            //request[ermission : 액티비티에 구현되어있는 onRequestPermissionsResult를 오버라이드 해서 권한을 요청함
            ActivityCompat.requestPermissions(
                this@MainActivity,
                REQUIRED_PERMISSIONS,
                PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        //내가 보낸 request가 맞다면 : requestCode가 내가 보낸 id 값과 일치하고 받을 result의 개수가 내가 요청한 require의 개수와 일치하면(여기선 2개)
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.size == REQUIRED_PERMISSIONS.size) {
            var checkResult = true

            //권한 중에서 내가 못받은 것이 있는지 확인, 있으면 아직 권한(result)을 못 받은 것이므로 false 처리
            for (result in grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    checkResult = false
                    break
                }
            }
            //사용자가 권한을 전부 수락해서 위치 값을 가져올 수 있음
            if (checkResult) {
                //위치 정보를 가져올 때 가져온 위치정보를 토대로 ui를 업데이트 해줌
                updateUI()
            }
            //사용자가 권한을 거부했을 경우 앱을 종료
            else {
                Toast.makeText(
                    this@MainActivity,
                    "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼이션을 허용해주세요",
                    Toast.LENGTH_SHORT
                ).show()
                //앱을 종료
                finish()
            }

        }
    }

    private fun showDialogForLocationServicesSetting() {
        //런처를 활용해 intent의 결과값을 리턴해주며 intent
        getGPSPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ){
            //return 받은 결과값이 ok면
            result ->
            if (result.resultCode == Activity.RESULT_OK){

                if(isLocationServicesAvailable()){
                    isRunTimePermissionsGranted()
                }
                else{
                    Toast.makeText(
                        this@MainActivity,
                        "위치 서비스를 사용할 수 없습니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            }
        }
        val builder : AlertDialog.Builder = AlertDialog.Builder(this@MainActivity)
        builder.setTitle("위치 서비스 비활성화")
        builder.setMessage("위치 서비스가 꺼져있습니다. 설정해야 앱을 사용할 수 있습니다.")
        builder.setCancelable(true)
        builder.setPositiveButton("설정", DialogInterface.OnClickListener{dialogInterface, i ->
            //안드로이드 기기 설정앱의 GPS 설정 페이지로 이동
            //GPS 설정 결과를 가져올 것이므로 launcher를 활용
            val callGPSSettingIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            getGPSPermissionLauncher.launch(callGPSSettingIntent)
        })
        builder.setNegativeButton("취소", DialogInterface.OnClickListener { dialogInterface, i ->
            Toast.makeText(
                this@MainActivity,
                "위치 서비스를 사용할 수 없습니다.",
                Toast.LENGTH_SHORT
            ).show()
            finish()
        })

        builder.create().show()
    }
}