package com.ali.mapapps.activity

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.ali.mapapps.R
import com.ali.mapapps.Util.Cv
import com.ali.mapapps.Util.Helper
import com.ali.mapapps.network.RetrofitClient
import com.ali.mapapps.network.response.WeatherResponse
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import kotlinx.android.synthetic.main.activity_weather.*
import retrofit2.Call
import retrofit2.Response
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class WeatherActivity : AppCompatActivity(),OnMapReadyCallback{

    private lateinit var mMap: GoogleMap
    var locationManager:LocationManager?=null
    var lati = 0.0
    var longt = 0.0
    companion object {
        const val REQUEST_CODE = 0xABC
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather)
        /*val mapFragment = supportFragmentManager.findFragmentById(R.id.cardmap) as SupportMapFragment
        mapFragment.getMapAsync(this)*/
    }

    override fun onResume() {
        super.onResume()
        getPermission()
        val date = Helper.getDate()
        tvDate.setText(date)
    }

    fun getWeatherDetails(localityName:String){
        val isConnected = Helper.isConnected(WeatherActivity@this)
        if(isConnected){
            val call = RetrofitClient.getInstance().myApi.getWeather(localityName,Cv.API_KEY)
            call.enqueue(object:retrofit2.Callback<WeatherResponse>{
                override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                    Toast.makeText(applicationContext,"fail",Toast.LENGTH_SHORT).show()
                }
                override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                    if(response.isSuccessful){
                        tvDate.visibility = View.VISIBLE
                        val body = response.body()
                        tvTemperature.setText(String.format("%.0f",Helper.kelvintoCelcius(body!!.main.temp_max).toDouble())+getString(R.string.degree_celcius))
                        tvFeelslike.setText("Feels like "+String.format("%.0f",Helper.kelvintoCelcius(body!!.main.feels_like).toDouble())+getString(R.string.degree_celcius))
                        tvHumidity.setText(body!!.main.humidity.toString()+"%")
                        tvPressure.setText(body!!.main.pressure.toString()+" mBar")
                        tvWindSpeed.setText((body!!.wind.speed*3.6).toString()+" km/h")
                        val visible = body!!.visibility / 1000.0
                        tvVisibility.setText(visible.toString()+" Km")
                        tvLocation.setText(Helper.getLocation(applicationContext,body.coord.lat,body.coord.lon,body.sys.country))
                        tvDescription.setText(body.weather[0].main)

                        val date = Date(body.sys.sunrise.toLong())
                        val format: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
                        format.setTimeZone(TimeZone.getTimeZone("PST"))
                        Toast.makeText(applicationContext,format.format(date),Toast.LENGTH_SHORT).show()
                    }else{
                        Toast.makeText(applicationContext,"no",Toast.LENGTH_SHORT).show()
                    }
                }
            })
        }else{
            AlertDialog.Builder(this)
                .setTitle("No Internet Connection")
                .setPositiveButton("Ok"){dialog: DialogInterface?, which: Int ->
                    dialog!!.dismiss()
                }
                .show()
        }
    }

    fun getPermission(){
        if(ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            locationManager = applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val isGPSEnabled = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
            if(isGPSEnabled){
                val locationGPS = locationManager!!.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if(locationGPS!=null){
                    lati = locationGPS.latitude
                    longt = locationGPS.longitude
                    getGeoLocation()
                    //Toast.makeText(applicationContext,lati.toString()+" "+longt.toString(),Toast.LENGTH_SHORT).show()
                }else{
                    AlertDialog.Builder(this)
                        .setTitle("Couldn't get the your location.Want to get the report of Arrah?")
                        .setPositiveButton("Yes"){dialog: DialogInterface?, which: Int ->
                            dialog!!.dismiss()
                            getWeatherDetails("arrah")
                        }
                        .setNegativeButton("No"){dialog: DialogInterface?, which: Int ->
                            dialog!!.dismiss()
                        }
                        .show()
                }
            }else{
                Toast.makeText(applicationContext,"Please enable GPS for better result",Toast.LENGTH_SHORT).show()
                getWeatherDetails("arrah");
            }
        }else{
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION,android.Manifest.permission.ACCESS_COARSE_LOCATION),
                REQUEST_CODE)
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode) {
            REQUEST_CODE -> {
                if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return
                    }
                    locationManager = applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                    val isNetworkEnabled = locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                    val isGpsEnabled = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
                    if(isGpsEnabled){
                        val locationGPS = locationManager!!.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                        lati = locationGPS!!.latitude
                        longt = locationGPS!!.longitude
                        getGeoLocation()
                        //Toast.makeText(applicationContext,lati.toString()+" "+longt.toString(),Toast.LENGTH_SHORT).show()
                    }else{
                        Toast.makeText(applicationContext,"Please Enbled the GPS",Toast.LENGTH_SHORT).show()
                    }
                }else{
                    val dialogBuilder = AlertDialog.Builder(this)
                    dialogBuilder.setTitle("Ali Hasan")
                    dialogBuilder.setMessage("Please grant the permission for better result")
                    dialogBuilder.setPositiveButton("Ok") { dialogInterface: DialogInterface, i: Int ->
                        Toast.makeText(applicationContext,"Ok",Toast.LENGTH_SHORT).show()
                    }
                    dialogBuilder.create().show()
                }
            }
        }
    }


    fun getGeoLocation(){
        val geocoder = Geocoder(this, Locale.getDefault())
        val addresses = geocoder.getFromLocation(lati,longt,1)
        val locality_name = addresses[0].locality
        //Toast.makeText(applicationContext,locality_name,Toast.LENGTH_SHORT).show()
        getWeatherDetails(locality_name)
    }

    override fun onMapReady(p0: GoogleMap?) {
        mMap = p0!!
        mMap?.uiSettings.isScrollGesturesEnabled = false
        mMap.setOnMapClickListener {
            startActivity(Intent(WeatherActivity@this,MapsActivity::class.java))
        }
    }
}

