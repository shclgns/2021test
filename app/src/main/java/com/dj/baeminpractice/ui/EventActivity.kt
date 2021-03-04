package com.dj.baeminpractice.ui

import android.Manifest
import android.content.ContentValues
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import com.dj.baeminpractice.R
import com.dj.baeminpractice.server.RequestHttpURLConnection
import com.dj.baeminpractice.ui.a_home.GpsTracker
import kotlinx.android.synthetic.main.activity_event.*
import okhttp3.internal.wait
import java.io.IOException
import java.util.*


class EventActivity : AppCompatActivity() {

    val GPS_ENABLE_REQUEST_CODE = 2001
    val PERMISSIONS_REQUEST_CODE = 99
    val REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    inner class NetworkTask(url:String, values: ContentValues?): AsyncTask<Void, Void, String>() {
        private val url:String
        private lateinit var values: ContentValues
        init{
            Log.d("@@@@", "네트워크테스크생성자도착")

            this.url = url
            Log.d("@@@@","$url")
            if (values ==null){
                if (values != null) {
                    this.values = values
                }
            }else{
                this.values = values
            }

            Log.d("@@@@","$url")
            Log.d("@@@@","$values")
        }

        override fun doInBackground(vararg params: Void?): String {
            Log.d("@@@@", "두인함수도착")
            val result:String // 요청 결과를 저장할 변수.
            Log.d("@@@@","$values")
            val requestHttpURLConnection = RequestHttpURLConnection()
            Log.d("@@@@", "request 넘어갈수있어?")
            Log.d("@@@@","$values")
            result = requestHttpURLConnection.request(url, values) // 해당 URL로 부터 결과물을 얻어온다.
            return result
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            //doInBackground()로 부터 리턴된 값이 onPostExecute()의 매개변수로 넘어오므로 s를 출력한다.
            tv_outPut.setText(result)
        }

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event)
        iv_close.setOnClickListener {
            finish()
        }
        if (!checkLocationServicesStatus()) {

            showDialogForLocationServiceSetting();
        }else {

            checkRunTimePermission();
        }
        adr_btn.setOnClickListener{
            var gpsTracker = GpsTracker(this@EventActivity)

            var longitude: Double = gpsTracker.getLongitude()
            var latitude: Double = gpsTracker.getLatitude()

            var address: String = getCurrentAddress(latitude, longitude)
            findViewById<TextView>(R.id.tv_address).setText(address)

//            Toast.makeText(
//                this@EventActivity,
//                "현재위치 \n위도 " + latitude + "\n경도 " + longitude,
//                Toast.LENGTH_LONG
//            ).show();

        }
        // URL 설정.
        // URL 설정.
        server_btn.setOnClickListener {
            val id = login_et_id.getText().toString().trim()
            val pw = login_et_pw.getText().toString()
            /* DB 대조 */
            val values = ContentValues()
            values.put("id", id)
            values.put("pw", pw)
            val url="http://15.164.142.204:3000/"
            val networkTask = NetworkTask(url, values)
            networkTask.execute()
        }

    }
    fun checkRunTimePermission() {
        //런타임 퍼미션 처리
        // 1. 위치 퍼미션을 가지고 있는지 체크합니다.
        val hasFineLocationPermission = ContextCompat.checkSelfPermission(
            this@EventActivity,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val hasCoarseLocationPermission = ContextCompat.checkSelfPermission(
            this@EventActivity,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if ((hasFineLocationPermission == PackageManager.PERMISSION_GRANTED && hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED))
        {
            // 2. 이미 퍼미션을 가지고 있다면
            // ( 안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식합니다.)
            // 3. 위치 값을 가져올 수 있음
        }
        else
        { //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.
            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this@EventActivity,
                    REQUIRED_PERMISSIONS[0]
                ))
            {
                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
                Toast.makeText(this@EventActivity, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.", Toast.LENGTH_LONG).show()
                // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(
                    this@EventActivity, REQUIRED_PERMISSIONS,
                    PERMISSIONS_REQUEST_CODE
                )
            }
            else
            {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(
                    this@EventActivity, REQUIRED_PERMISSIONS,
                    PERMISSIONS_REQUEST_CODE
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        permsRequestCode: Int,
        permissions: Array<String>,
        grandResults: IntArray
    ) {
        if (permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.size == REQUIRED_PERMISSIONS.size)
        {
            // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신되었다면
            var check_result = true
            // 모든 퍼미션을 허용했는지 체크합니다.
            for (result in grandResults)
            {
                if (result != PackageManager.PERMISSION_GRANTED)
                {
                    check_result = false
                    break
                }
            }
            if (check_result)
            {}//위치 값을 가져올 수 있음
            else
            {
                // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱을 종료합니다.2 가지 경우가 있습니다.
                if ((ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        REQUIRED_PERMISSIONS[0]
                    ) || ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        REQUIRED_PERMISSIONS[1]
                    )))
                {
                    Toast.makeText(
                        this@EventActivity,
                        "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요.",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                }
                else
                {
                    Toast.makeText(
                        this@EventActivity,
                        "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다. ",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }


    fun getCurrentAddress(latitude: Double, longitude: Double): String {
        var geocoder = Geocoder(this, Locale.getDefault())
        var addresses: List<Address>
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 100)
        } catch (ioException: IOException) //네트워크 문제
        {
            Toast.makeText(this, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show()
            showDialogForLocationServiceSetting(); return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException: IllegalArgumentException) {
            Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            showDialogForLocationServiceSetting(); return "잘못된 GPS 좌표";
        }

        if (addresses == null) //|| addresses.size() == 0
        {
            Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show();
            showDialogForLocationServiceSetting(); return "주소 미발견";
        }

        var address = addresses.get(0)
        return address.getAddressLine(0).toString() + "\n";
    }

    //여기부터는 GPS 활성화를 위한 메소드들
    private fun showDialogForLocationServiceSetting() {
        val builder = AlertDialog.Builder(this@EventActivity)
        builder.setTitle("위치 서비스 비활성화")
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n" + "위치 설정을 수정하실래요?")
        builder.setCancelable(true)
        builder.setPositiveButton("설정", object : DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface, id: Int) {
                val callGPSSettingIntent =
                    Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE)
            }
        })
        builder.setNegativeButton("취소", object : DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface, id: Int) {
                dialog.cancel()
            }
        })
        builder.create().show();
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            GPS_ENABLE_REQUEST_CODE ->
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {
                        Log.d("@@@", "onActivityResult : GPS 활성화 되있음")
                        checkRunTimePermission()
                        return
                    }
                }

        }
    }

    fun checkLocationServicesStatus():Boolean {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        return (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        ))
    }
}




