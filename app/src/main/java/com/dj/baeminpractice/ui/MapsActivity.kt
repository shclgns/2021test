package com.dj.baeminpractice.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.dj.baeminpractice.R
import com.google.android.gms.location.*
import com.google.android.gms.maps.*

import com.google.android.gms.maps.model.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener,
    GoogleMap.OnInfoWindowClickListener {

    val permission = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION,android.Manifest.permission.ACCESS_COARSE_LOCATION)
    val PERM_FLAG =99

    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        if(isPermitted()){
            startProcess()

        }else{
            ActivityCompat.requestPermissions(this,permission,PERM_FLAG) //권한을 요청한다
        }
    }

    fun isPermitted():Boolean{ //권한 승인 확인
        for (perm in permission){
            if (ContextCompat.checkSelfPermission(this,perm) !=  PackageManager.PERMISSION_GRANTED){
                return false
            }
        }
        return true
    }

    fun startProcess(){
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        Log.d("로케이션","스타트 프로세스")
        mapFragment.getMapAsync(this)
        Log.d("로케이션","겟맵어싱크")
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        Log.d("로케이션","온맵래디")

        // 여기서 가게 로케이션 파싱 이후 가게위치를 마커로 표시 후
        // 마커 온클릭 리스너 만들기
        var market_latitude : Double = 37.52487 //  파싱한 위도 값
        var market_longitutde : Double = 126.92723 //  파싱한 경도 값
        var market_name : String = "가게명" // 파싱한 가게명
        var index : Double = 0.00013

        for (i in 1.. 10){


            var marketLocation = LatLng(market_latitude+index,market_longitutde)

            index += 0.00128

            // 여기서 반복문을 통해서 로케이션 마커를 찍어줌
            val market_marker = MarkerOptions()
                .position(marketLocation)
                .title(market_name)

            mMap.addMarker(market_marker)


        }
        // 마커 클릭 이벤트 처리
        mMap.setOnMarkerClickListener(this)

        // title click event
        mMap.setOnInfoWindowClickListener(this)

        // 카메라 세팅
        val market_camera = CameraPosition.Builder()
            .target(LatLng(37.52488,126.92723))
            .zoom(15.0f)
            .build()
        val camera = CameraUpdateFactory.newCameraPosition(market_camera)
        mMap.moveCamera(camera)

        // 밑에있는 마커 리스너에서 어떻게할지 정하기
        mMap.setOnMarkerClickListener(this)


        """
            밑에있는 내용은 현재 자기 자신 위치 맵에 동기화
        
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)// 이거 설정해줘야함
        setUpdateLocationListener() // 현재위치 뿌려주기
               
        """.trimIndent()

    }
    override fun onInfoWindowClick(marker: Marker?) {
        Toast.makeText(this, (marker?.title ?: "please check title" ) +"\n"+ marker?.position, Toast.LENGTH_SHORT).show()

        startActivity(Intent(this@MapsActivity , Market::class.java)) // 마켓 액티비티로 이동
    }

    override fun onMarkerClick(marker: Marker?): Boolean {
        Toast.makeText(this, (marker?.title ?: "please check title" ) +"\n"+ marker?.position, Toast.LENGTH_SHORT).show()

        // 파람을 풋 액스트라를 통해서 보낸다음 거기에 맞는 마켓정보를 액티비티로 띄어준다.

//        startActivity(Intent(this@MapsActivity , Market::class.java)) // 마켓 액티비티로 이동
        return false
    }

    lateinit var fusedLocationClient:FusedLocationProviderClient // 좌표를 가져올때 고려해야할 사항이 있는데 배터리 소모 와 정확도 같은걸 자동으로 해주는 역할을함
    lateinit var locationCallback:LocationCallback

    fun setUpdateLocationListener(){
        Log.d("로케이션","셋업로케이션리스너")
        val locationRequest =LocationRequest()// 이런 함수들은 gradle에서 location 가져와야함
        locationRequest.run{
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY //GPS 와 네트워크 둘다사용
            interval =10000 // 1초에 한번씩 가져오겠다

        }
        locationCallback = object : LocationCallback(){
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult?.let{
                    for ((i,location) in it.locations.withIndex()){
//                        Log.d("로케이션","$i ${location.latitude} ,${location.longitude}")
                        setLastLocation(location)
                    }
                }
            }
        }
        //로케이션 요청 함수 호출 ( locationRequest, locationCallback) 이 파라매터를 사용함
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationClient.requestLocationUpdates(locationRequest,locationCallback, Looper.myLooper())
    }

    fun setLastLocation(location: Location){
        Log.d("로케이션","셋 라스트로케이션")
        val myLocation =LatLng(location.latitude,location.longitude)
        val markerOption =MarkerOptions()
            .position(myLocation)
            .title("i'm here")
        val cameraOption = CameraPosition.Builder()
            .target(myLocation)
            .zoom(15.0f)
            .build()
        val camera = CameraUpdateFactory.newCameraPosition(cameraOption)
        mMap.addMarker(markerOption)
        mMap.moveCamera(camera)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode){
            PERM_FLAG ->{
                var check =true
                for (grant in grantResults){
                    if(grant != PackageManager.PERMISSION_GRANTED){
                        check =false
                        break
                    }
                }
                if (check){
                    startProcess()
                }else{
                    Toast.makeText(this,"권한을 승인 해야지만 앱을 사용할 수 있습니다.",Toast.LENGTH_LONG).show()
                    finish()
                }

            }
        }
    }
    fun getDescriptorFromDrawable(drawableId: Int): BitmapDescriptor {
        var bitmapDrawable: BitmapDrawable

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { //버전별로 동작하는 코드가 다름
            bitmapDrawable = getDrawable(drawableId) as BitmapDrawable
        } else {
            bitmapDrawable = resources.getDrawable(drawableId) as BitmapDrawable
        }
        // 마커 크기 변환
        val scaledBitmap = Bitmap.createScaledBitmap(bitmapDrawable.bitmap, 50 , 80, false)

        return BitmapDescriptorFactory.fromBitmap(scaledBitmap)
    }




}


