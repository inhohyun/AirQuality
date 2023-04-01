package com.ihh.airquality

import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.ihh.airquality.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    lateinit var binding : ActivityMainBinding
    //권한을 요청받을 상수값
    private val PERMISSION_REQUEST_CODE = 100
    val REQUIRED_PERMISSIONS = arrayOf(
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION
    )

    lateinit var getGPSPermissionLauncher : ActivityResultLauncher<Intent>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkAllPermissions()


    }
    private fun checkAllPermissions(){
        //GPS가 꺼져있을 때 함수호출
        if(!isLocationServicesAvailable()){
            //사용자에게 GPS를 켜달라는 함수
            showDialogForLocationServicesSetting()
        }
        //GPS가 켜져있을때 함수호출
        else{
            isRunTimePermissionsGranted()
        }

    }
    private fun isLocationServicesAvailable() : Boolean{
        //system에서 제공하는 Location서비스를 가져오는데, 이것을 LocationManager로써 사용한다.
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        //GPS위치 권한이나 네트워크 위치 권한이 Enabled 되어있다면 true를 반환함
        return (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
    }

    private fun isRunTimePermissionsGranted(){
        //permission이 있는지 없는지를 확인
        //CoarseLocation은 대략적인 위치를, FineLocation은 정확한 위치를 줌
        //권한이 있는지를 확인하여 bool값을 변수에 저장
        val hasFineLocationPermisson = ContextCompat.checkSelfPermission(this@MainActivity, android.Manifest.permission.ACCESS_FINE_LOCATION)
        val hasCoarseLocationPermisson = ContextCompat.checkSelfPermission(this@MainActivity, android.Manifest.permission.ACCESS_COARSE_LOCATION)

        //위의 위치 권한이 둘 중하나라도 없으면 사용자에게 다이얼로그를 활용해 권한 요청하는 창이 뜸뜸
       if(hasFineLocationPermisson != PackageManager.PERMISSION_GRANTED || hasCoarseLocationPermisson != PackageManager.PERMISSION_GRANTED){
            //PERMISSION_REQUEST_CODE를 쓰는 이유 : response를 받아올 때, 어떤 request였는지 id 값을 줌
            //request[ermission : 액티비티에 구현되어있는 onRequestPermissionsResult를 오버라이드 해서 권한을 요청함
            ActivityCompat.requestPermissions(this@MainActivity, REQUIRED_PERMISSIONS, PERMISSION_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        //내가 보낸 request가 맞다면 : requestCode가 내가 보낸 id 값과 일치하고 받을 result의 개수가 내가 요청한 require의 개수와 일치하면(여기선 2개)
        if(requestCode == PERMISSION_REQUEST_CODE && grantResults.size == REQUIRED_PERMISSIONS.size){
            var checkResult = true

            //권한 중에서 내가 못받은 것이 있는지 확인, 있으면 아직 권한(result)을 못 받은 것이므로 false 처리
            for(result in grantResults){
                if (result != PackageManager.PERMISSION_GRANTED){
                    checkResult = false
                    break
                }
            }
            //사용자가 권한을 전부 수락해서 위치 값을 가져올 수 있음
            if(checkResult){

            }
            //사용자가 권한을 거부했을 경우 앱을 종료
            else{
                Toast.makeText(this@MainActivity, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼이션을 허용해주세요", Toast.LENGTH_SHORT).show()
                //앱을 종료
                finish()
            }

        }
    }

    private fun showDialogForLocationServicesSetting(){

    }
}