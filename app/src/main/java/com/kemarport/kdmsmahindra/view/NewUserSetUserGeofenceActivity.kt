package com.kemarport.kdmsmahindra.view

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationManager
import android.media.AudioManager
import android.media.ToneGenerator
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Spinner
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.kemarport.kdmsmahindra.R
import com.kemarport.kdmsmahindra.adapter.LocationSpinnerAdapter
import com.kemarport.kdmsmahindra.databinding.ActivityNewUserSetUserGeofenceBinding
import com.kemarport.kdmsmahindra.helper.SessionManager
import com.kemarport.kdmsmahindra.model.setgeofence.GetGeofenceByDealer
import com.kemarport.kdmsmahindra.model.setgeofence.PostGeofenceCoordinatesRequest
import com.kemarport.kdmsmahindra.repository.KDMSRepository
import com.kemarport.kdmsmahindra.viewmodel.LoginVM
import com.kemarport.kdmsmahindra.viewmodel.LoginVMPF
import com.kemarport.kdmsmahindra.viewmodel.SetFirstTimeGeofenceViewModel
import com.kemarport.kdmsmahindra.viewmodel.SetFirstTimeGeofenceViewModelFactory
import com.kemarport.mahindrakiosk.helper.Constants
import com.kemarport.mahindrakiosk.helper.Resource
import es.dmoral.toasty.Toasty
import java.util.HashMap

class NewUserSetUserGeofenceActivity : AppCompatActivity(), OnMapReadyCallback {
    lateinit var binding: ActivityNewUserSetUserGeofenceBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLocation: Location? = null

    var currentMarker: Marker? = null
    private var flagCurrentLoc = true
    private lateinit var googleMap: GoogleMap
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001


    ///direction


    private var userName: String? = ""
    private var runningLoad: Int = 250
    private var dealerCode: String = ""
    private var locationId: Int = 0
    private var DeviceIp: String? = ""
    private lateinit var locationProg: ProgressDialog


    private lateinit var userDetails: HashMap<String, String?>
    private lateinit var session: SessionManager
    private lateinit var viewModel: SetFirstTimeGeofenceViewModel
    private lateinit var progress: ProgressDialog

    lateinit var locationList: ArrayList<GetGeofenceByDealer>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_new_user_set_user_geofence)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        progress = ProgressDialog(this)
        progress.setMessage("Loading...")

        locationProg = ProgressDialog(this)
        locationProg.setMessage("Please wait,\nFetching Location Ready...")
        locationProg.setCancelable(false)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationList = ArrayList()
        userName = intent.getStringExtra("userName").toString()
        dealerCode = intent.getStringExtra("dealerCode").toString()

        try {
            binding.dealerCode.setText(dealerCode)
            binding.userName.setText(userName)

        } catch (e: Exception) {
        }

        Log.e("thisisisi", "${userName} - ${dealerCode}")

        try {
            val androidId: String =
                Settings.Secure.getString(this.contentResolver, Settings.Secure.ANDROID_ID)

            DeviceIp = androidId
            binding.tvDeviceId.setText(androidId)

        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        requestLocationEnable()
        showProgressBarLocation()
        checkLocationPermission()

        val kdmsRepository = KDMSRepository()
        val viewModelProviderFactory =
            SetFirstTimeGeofenceViewModelFactory(application, kdmsRepository)
        viewModel =
            ViewModelProvider(
                this,
                viewModelProviderFactory
            )[SetFirstTimeGeofenceViewModel::class.java]

        viewModel.getGeofenceByDealerMutable.observe(this) { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    response.data?.let { resultResponse ->
                        if (resultResponse.size > 0) {
                           // runningLoad = resultResponse.get(0).runningLoad

                            locationList.addAll(
                                resultResponse.filter { it.coordinates.isNullOrBlank() }
                            )

                            val autoComplete = findViewById<AutoCompleteTextView>(R.id.tvSpinnerLocation)
                            val adapter = ArrayAdapter(
                                this,
                                R.layout.spinner_layout,
                                locationList.map { it.displayName }
                            )
                            autoComplete.setAdapter(adapter)
                            autoComplete.setOnItemClickListener { _, _, position, _ ->
                                val selectedItem = locationList[position]
                                locationId = selectedItem.locationId
                            }
                        }
                    }
                }

                is Resource.Error -> {
                    hideProgressBar()
                    response.message?.let { resultResponse ->
                        Toasty.error(
                            this@NewUserSetUserGeofenceActivity,
                            "Error : $resultResponse"
                        ).show()
                    }
                }

                is Resource.Loading -> {
                    showProgressBar()
                }
            }
        }

        viewModel.postGeofenceCoordinatesMutable.observe(this) { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    startActivityLoginActivity()
                    response.data?.let { resultResponse ->

                        var edResponseMessage = resultResponse.message.toString().trim()
                        if (edResponseMessage.isNotEmpty()) {
                            Toasty.success(
                                this@NewUserSetUserGeofenceActivity,
                                "$resultResponse"
                            ).show()
                        }
                    }
                }

                is Resource.Error -> {
                    hideProgressBar()
                    response.message?.let { resultResponse ->
                        Toasty.error(
                            this@NewUserSetUserGeofenceActivity,
                            "Error : $resultResponse"
                        ).show()
                    }
                }

                is Resource.Loading -> {
                    showProgressBar()
                }
            }
        }


        getDealerFence()

        binding.btnRecenter.setOnClickListener {
            recentr()
        }
        /////direction
        requestLocation()

        binding.btnSubmit.setOnClickListener {
            submitUserLocation()
        }
    }
    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request location permission
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            // Permission is granted, start location updates
            startLocationUpdates()

        }
    }
    private fun startActivityLoginActivity() {
        var intent = Intent(this@NewUserSetUserGeofenceActivity, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun submitUserLocation() {
        try {
            // Validate inputs first
            if (currentLocation == null) {
                Toasty.warning(
                    this@NewUserSetUserGeofenceActivity,
                    "Current location is not available"
                ).show()
                return
            }

            if (dealerCode.isBlank()) {
                Toasty.warning(
                    this@NewUserSetUserGeofenceActivity,
                    "Dealer code is missing"
                ).show()
                return
            }

            if (locationId == 0) {
                Toasty.warning(
                    this@NewUserSetUserGeofenceActivity,
                    "Location ID is missing"
                ).show()
                return
            }

            if (runningLoad == 0) {
                Toasty.warning(
                    this@NewUserSetUserGeofenceActivity,
                    "Running load is missing"
                ).show()
                return
            }

            // If all checks passed, call API
            viewModel.postGeofenceCoordinates(
                this@NewUserSetUserGeofenceActivity,
                postGeofenceCoordinatesRequest = PostGeofenceCoordinatesRequest(
                    DeviceIp = "",
                    coordinates = "${currentLocation?.latitude},${currentLocation?.longitude}",
                    dealerCode = dealerCode,
                    locationId = locationId,
                    runningLoad = runningLoad,
                )
            )

        } catch (e: Exception) {
            Toasty.error(
                this@NewUserSetUserGeofenceActivity,
                "Error : ${e.message}"
            ).show()
        }
    }

    private fun getDealerFence() {
        try {
            viewModel.getGeofenceByDealer(
                this@NewUserSetUserGeofenceActivity,
                dealerCode = dealerCode
            )

        } catch (e: Exception) {
            Toasty.error(
                this@NewUserSetUserGeofenceActivity,
                "Error : ${e.message}"
            ).show()
        }
    }


    fun showProgressBar() {
        progress.show()
    }

    fun hideProgressBar() {
        progress.cancel()
    }

    fun showProgressBarLocation() {
        locationProg.show()
    }

    fun hideProgressBarLocation() {
        locationProg.cancel()
    }

    override fun onPause() {
        super.onPause()

        stopLocationUpdates()
    }

    override fun onStop() {
        super.onStop()
        stopLocationUpdates()
    }

    override fun onResume() {
        super.onResume()
        requestLocationEnable()
        showProgressBarLocation()
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 500)
            .setWaitForAccurateLocation(true)
            .setMinUpdateIntervalMillis(500)
            .setMaxUpdateDelayMillis(500)
            .build()
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
        flagCurrentLoc = true
        //direction

    }
    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 500)
            .setWaitForAccurateLocation(true)
            .setMinUpdateIntervalMillis(500)
            .setMaxUpdateDelayMillis(500)
            .build()
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            null
        )
    }
    override fun onDestroy() {
        super.onDestroy()
        stopLocationUpdates()
    }

    ////map
    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap!!.isBuildingsEnabled = false
        runOnUiThread {
            recentr()
        }
    }

    //location code (fused/gps/network)
    private fun requestLocationEnable() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            showAlertMessage()
        }
    }

    private fun showAlertMessage() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("The location permission is disabled. Do you want to enable it?")
            .setCancelable(false)
            .setPositiveButton("Yes") { _, _ ->
                startActivityForResult(
                    Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS),
                    10
                )
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.cancel()
                finish()
            }
        val alert: AlertDialog = builder.create()
        alert.show()
    }

    private fun requestLocation() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
            .setWaitForAccurateLocation(true)
            .setMinUpdateIntervalMillis(1000)
            .setMaxUpdateDelayMillis(1000)
            .build()

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )

    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val location = locationResult.lastLocation
            if (location != null) {
                hideProgressBarLocation()
                Log.e("currentLocNewFusedGPS", location.toString())
                updateLocation(location)

            }
        }
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun updateLocation(location: Location) {
        currentLocation = location

        googleMap.animateCamera(
            CameraUpdateFactory.newCameraPosition(
                CameraPosition.Builder()
                    .target(
                        LatLng(location.latitude, location.longitude)
                    )
                    .tilt(35f)
                    .zoom(25f)
                    .build()
            )
        )
        if (flagCurrentLoc) {
            recentr()
            flagCurrentLoc = false
        }
        if (::googleMap.isInitialized) {
            drawLines()
        }
    }

    fun recentr() {
        currentLocation?.let {
            val currentPos = LatLng(currentLocation!!.latitude, currentLocation!!.longitude)
            if (currentPos != null && googleMap != null) {
                googleMap!!.moveCamera(CameraUpdateFactory.newLatLng(currentPos))
                googleMap!!.animateCamera(CameraUpdateFactory.zoomTo(25f))
                println("(${currentLocation!!.latitude}, ${currentLocation!!.longitude}")
            }
        }
    }

    private fun drawLines() {
        googleMap.clear()
        currentLocation?.let {

            addCursorMarker(LatLng(currentLocation!!.latitude, currentLocation!!.longitude))
        }
    }

    //adding cursor to location
    private fun addCursorMarker(position: LatLng) {
        val markerBitmap = generateLocationIcon()

        currentMarker = googleMap.addMarker(
            MarkerOptions()
                .position(position)
                .title("Current Position")
                .icon(markerBitmap?.let { BitmapDescriptorFactory.fromBitmap(it) })

        )

    }

    fun generateLocationIcon(): Bitmap? {
        val height = 50
        val width = 50
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.nav_icon)
        return Bitmap.createScaledBitmap(bitmap, width, height, false)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}

