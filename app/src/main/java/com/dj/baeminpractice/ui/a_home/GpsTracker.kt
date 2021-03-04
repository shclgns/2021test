package com.dj.baeminpractice.ui.a_home

import android.Manifest
import android.app.Service
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.core.content.ContextCompat
import kotlin.properties.Delegates


class GpsTracker(context: Context):  LocationListener, Service() {
    private val mContext:Context
    internal var location: Location? =null
    internal var latitude by Delegates.notNull<Double>()
    internal var longitude by Delegates.notNull<Double>()

    private val MIN_DISTANCE_CHANGE_FOR_UPDATES:Float = 10F
    private val MIN_TIME_BW_UPDATES = (1000 * 60 * 1).toLong()


    protected lateinit var locationManager: LocationManager

    init{
        this.mContext = context
        getLocation()
    }

    fun getLocation(): Location? {
        try
        {

            locationManager = mContext.getSystemService(LOCATION_SERVICE) as LocationManager
            val isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            Log.d("@@@", "locationManger 초기화 값+$locationManager" )
            if (!isGPSEnabled && !isNetworkEnabled)
            {
            }
            else
            { Log.d("@@@", "gps랑 네트워크 연결됨" )
                val hasFineLocationPermission = ContextCompat.checkSelfPermission(mContext,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                val hasCoarseLocationPermission = ContextCompat.checkSelfPermission(mContext,
                    Manifest.permission.ACCESS_COARSE_LOCATION)
                if ((hasFineLocationPermission == PackageManager.PERMISSION_GRANTED && hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED))
                {}
                else
                    return null
                if (isNetworkEnabled)
                {Log.d("@@@", "네트워크 연결됨" )
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this)
                    if (locationManager != null)
                    {
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                        if (location != null)
                        {
                            latitude = location!!.getLatitude()
                            longitude = location!!.getLongitude()
                        }
                    }
                }
                if (isGPSEnabled)
                {
                    Log.d("@@@", "gps 연결됨" )
                    Log.d("@@@", "$isNetworkEnabled" )
                    Log.d("@@@", "null 일때 location 값은+$location" )
                    if (location == null)
                    {   Log.d("@@@", "null 일때 location 값은+$location" )
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this)
                        if (locationManager != null)
                        {   Log.d("@@@", "null 아닐때 location 값은+$location" )
                            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                            if (location != null)
                            {
                                latitude = location!!.getLatitude()
                                longitude = location!!.getLongitude()
                            }
                        }
                    }
                    else{
                        Log.d("@@@", "null 일때 location 값은+$location" )
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this)
                        if (locationManager != null)
                        {   Log.d("@@@", "null 아닐때 location 값은+$location" )
                            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                            if (location != null)
                            {
                                latitude = location!!.getLatitude()
                                longitude = location!!.getLongitude()
                            }
                        }
                    }
                }
            }
        }
        catch (e:Exception) {
            Log.d("@@@", "" + e.toString())
        }
        return location
    }
    fun getLatitude():Double {
        if (location != null)
        {
            latitude = location!!.getLatitude()
        }
        return latitude
    }
    fun getLongitude():Double {
        if (location != null)
        {
            longitude = location!!.getLongitude()
        }
        return longitude
    }
    override fun onLocationChanged(location:Location) {}
    override fun onProviderDisabled(provider:String) {}
    override fun onProviderEnabled(provider:String) {}
    override fun onStatusChanged(provider:String, status:Int, extras: Bundle) {}
    override fun onBind(arg0: Intent): IBinder? {
        return null
    }
    fun stopUsingGPS() {
        if (locationManager != null)
        {
            locationManager.removeUpdates(this@GpsTracker)
        }
    }

}