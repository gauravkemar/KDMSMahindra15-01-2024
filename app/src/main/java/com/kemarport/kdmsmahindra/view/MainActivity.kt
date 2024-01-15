package com.kemarport.kdmsmahindra.view


import android.Manifest
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.location.LocationManager
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.text.Html
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import com.kemarport.kdmsmahindra.R
import com.kemarport.kdmsmahindra.databinding.ActivityMainBinding
import com.kemarport.kdmsmahindra.helper.RFIDHandler
import com.kemarport.kdmsmahindra.helper.SessionManager
import com.kemarport.kdmsmahindra.model.newapi.ConfirmDealerVehicleDeliveryRequest
import com.kemarport.kdmsmahindra.model.newapi.VerifyDealerVehicleRequest
import com.kemarport.kdmsmahindra.repository.KDMSRepository
import com.kemarport.kdmsmahindra.viewmodel.KDMSHomeVM
import com.kemarport.kdmsmahindra.viewmodel.KDMSHomeVMPF
import com.kemarport.mahindrakiosk.helper.Constants
import com.kemarport.mahindrakiosk.helper.Resource
import com.kemarport.mahindrakiosk.helper.Utils
import com.symbol.emdk.EMDKManager
import com.symbol.emdk.EMDKResults
import com.symbol.emdk.barcode.*
import com.symbol.emdk.barcode.Scanner
import com.zebra.rfid.api3.TagData
import es.dmoral.toasty.Toasty
import java.text.SimpleDateFormat
import kotlin.random.Random
import java.util.*


class MainActivity : AppCompatActivity(), EMDKManager.EMDKListener, Scanner.StatusListener,
    Scanner.DataListener, RFIDHandler.ResponseHandlerInterface
 {
    lateinit var binding: ActivityMainBinding
    private lateinit var progress: ProgressDialog
    private var dealerCode: String? = ""
    private var token: String? = ""
    private var userRoleCheck: String? = ""
    private var userName: String? = ""
    private var locationId: String? = ""
    private lateinit var dealerDetails: HashMap<String, String?>
    lateinit var coordinates: ArrayList<LatLng>
    var coordinatePref = ""
    private lateinit var session: SessionManager

    //viewmodel
    private lateinit var viewModel: KDMSHomeVM


    //////location
    fun isLocationEnabled(): Boolean {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    fun openLocationSettings() {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        startActivity(intent)
    }

    //rfid
    var TAG = "MainActivity"
    var rfidHandler: RFIDHandler? = null
    private fun initReader() {
        rfidHandler = RFIDHandler()
        rfidHandler!!.init(this)
    }
    var isRFIDInit = false
    var isBarcodeInit = false
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    var isLocationAvailable = false
    private val locationRequest: LocationRequest = LocationRequest.create().apply {
        interval = 1000 // Update interval in milliseconds
        fastestInterval = 1000 // Fastest update interval in milliseconds
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }
    private val LOCATION_PROVIDER_CHECK_INTERVAL = 1000
    private val locationProviderCheckHandler = Handler()
    private val locationProviderCheckRunnable = object : Runnable {
        override fun run() {
            checkLocationProviderStatus()
            locationProviderCheckHandler.postDelayed(
                this,
                LOCATION_PROVIDER_CHECK_INTERVAL.toLong()
            )
        }
    }
    private val LOCATION_PERMISSION_REQUEST_CODE = 123
    private var currentLatitude: Double = 0.0
    private var currentLongitude: Double = 0.0
    private lateinit var locationManager: LocationManager
    var isGPSEnabled = false
    var isNetworkEnabled = false


    ////scanner
     var resumeFlag = false
     var emdkManager: EMDKManager? = null
     var barcodeManager: BarcodeManager? = null
     var scanner: Scanner? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setSupportActionBar(binding.homeToolbar)
        supportActionBar?.title = "VIN Confirmation"
        progress = ProgressDialog(this)
        progress.setMessage("Please Wait...")
        session = SessionManager(this)

        dealerDetails = session.getUserDetails()
        dealerCode = dealerDetails[Constants.DEALER_CODE]
        token = dealerDetails[Constants.KEY_JWT_TOKEN]
        userRoleCheck = dealerDetails[Constants.ROLE_NAME]
        userName = dealerDetails[Constants.KEY_USER_NAME]
        locationId = dealerDetails[Constants.LOCATION_ID]

        binding.radioGroup.check(binding.radioBtn2.getId())
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        Toasty.Config.getInstance()
            .setGravity(Gravity.CENTER)
            .apply()
        //location
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        isGPSEnabled = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
        isNetworkEnabled = locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        coordinatePref = Utils.getSharedPrefs(this@MainActivity, Constants.COORDINATES).toString()
        coordinates = parseStringToList(coordinatePref)
        checkLocationPermission()
        startLocationProviderCheck()

        val color = Color.parseColor("#004f8c") // Replace with your desired color
        val grey = Color.parseColor("#9A9A9A")
        //viewmodel
        val kdmsRepository = KDMSRepository()
        val viewModelProviderFactory = KDMSHomeVMPF(application, kdmsRepository)
        viewModel = ViewModelProvider(this, viewModelProviderFactory)[KDMSHomeVM::class.java]
        viewModel.postVerifyDealerVehicleMutable.observe(this) { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    response.data?.let { resultResponse ->
                       /* Toasty.success(this, response.data.responseMessage, Toasty.LENGTH_SHORT)
                                    .show()*/
                        var message=resultResponse.message
                        var status=resultResponse.status
                        if(message!=null)
                        {
                            Toasty.warning(this, resultResponse.message, Toasty.LENGTH_SHORT)
                                .show()
                        }

                        if(resultResponse !=null)
                        {
                            val rfid=binding.edVinScan.text.toString().trim()
                            binding.tvVinValue.setText(resultResponse.vin)
                            binding.tvModelCodeValue.setText(resultResponse.modelCode)
                            binding.tvColorValue.setText(resultResponse.colorDescription)
                            binding.tvEngineNoValue.setText(resultResponse.engineNo)
                            /*binding.btnSubmit.setOnClickListener {
                                confirmVin(resultResponse.responseObject.vin)
                                //submitVin("")
                            }*/
                            ViewCompat.setBackgroundTintList(
                                binding.btnSubmit,
                                ColorStateList.valueOf(color)
                            )

                            binding.btnSubmit.isEnabled = true
                            binding.btnSubmit.setOnClickListener {
                                //confirmVin(resultResponse.responseObject.vin)
                                confirmVin(rfid)
                                //submitVin("")
                            }
                        }

                    }
                }
                is Resource.Error -> {
                    hideProgressBar()
                    clearText()
                    ViewCompat.setBackgroundTintList(
                        binding.btnSubmit,
                        ColorStateList.valueOf(grey)
                    )

                    binding.btnSubmit.isEnabled = false
                    response.message?.let { resultResponse ->
                        Toasty.error(this, resultResponse, Toasty.LENGTH_SHORT).show()
                        if (resultResponse == "Session Expired ! Please relogin" || resultResponse == "Authentication token expired" ||
                            resultResponse == Constants.CONFIG_ERROR) {
                            showCustomDialog(
                                "Session Expired",
                                "Please re-login to continue"
                            )
                        }

                    }
                }
                is Resource.Loading -> {
                    showProgressBar()
                }
                else -> {

                }
            }
        }

        viewModel.postConfirmDealerVehicleDeliveryMutable .observe(this) { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()

                    response.data?.let { resultResponse ->
                     /*   if(resultResponse.responseMessage!=null)
                        {
                            Toasty.success(this, resultResponse.responseMessage, Toasty.LENGTH_SHORT)
                                .show()
                        }
                        else
                        {
                            Toasty.success(this, resultResponse.errorMessage, Toasty.LENGTH_SHORT)
                                .show()
                        }*/
                        var status=resultResponse.status
                        if (status=="Failed")
                        {
                            Toasty.error(this, resultResponse.message, Toasty.LENGTH_SHORT)
                                .show()
                        }
                        else if (status=="Success"){
                            Toasty.success(this, resultResponse.message, Toasty.LENGTH_SHORT)
                                .show()
                        }
                        else
                        {
                            Toasty.warning(this, resultResponse.message, Toasty.LENGTH_SHORT)
                                .show()
                        }


                        clearText()
                    }

                }
                is Resource.Error -> {
                    hideProgressBar()
                    ViewCompat.setBackgroundTintList(
                        binding.btnSubmit,
                        ColorStateList.valueOf(grey)
                    )
                    binding.btnSubmit.isEnabled = false
                    response.message?.let { resultResponse ->
                        Toasty.error(this, resultResponse, Toasty.LENGTH_SHORT).show()
                        if (resultResponse == "Session Expired ! Please relogin" || resultResponse == "Authentication token expired" ||
                            resultResponse == Constants.CONFIG_ERROR) {
                            showCustomDialog(
                                "Session Expired",
                                "Please re-login to continue"
                            )
                        }
                    }
                    response.message?.let { resultResponse ->
                        Toasty.error(this, resultResponse, Toasty.LENGTH_SHORT).show()

                    }
                }
                is Resource.Loading -> {
                    showProgressBar()
                }
                else -> {

                }
            }
        }



        binding.clearText.setOnClickListener {
                clearText()
        }

        if (Build.MANUFACTURER.contains("Zebra Technologies") || Build.MANUFACTURER.contains("Motorola Solutions")) {

            binding.radioGroup.visibility = View.VISIBLE
        } else {

            binding.radioGroup.visibility = View.GONE
        }
        if (Build.MANUFACTURER.contains("Zebra Technologies") || Build.MANUFACTURER.contains("Motorola Solutions")) {
            defaultBarcode()
        }

        binding.radioGroup.setOnCheckedChangeListener { buttonView, selected ->
            if (Build.MANUFACTURER.contains("Zebra Technologies") || Build.MANUFACTURER.contains("Motorola Solutions")) {
                if (selected == binding.radioBtn1.getId()) {
                    isRFIDInit = true
                    isBarcodeInit = false
                    deInitScanner()
                    Thread.sleep(1000)
                    initReader()
                } else if (selected == binding.radioBtn2.getId()) {

                    setDefaultScanner()
                }


            }
        }

    }
     private fun logout(){
         session.logoutUser()
         startActivity(Intent(this@MainActivity, LoginActivity::class.java))
         finish()
     }
     fun showCustomDialog(title: String?, message: String?) {
         var alertDialog: AlertDialog? = null
         val builder: AlertDialog.Builder
         if (title.equals(""))
             builder = AlertDialog.Builder(this)
                 .setMessage(Html.fromHtml(message))
                 .setIcon(android.R.drawable.ic_dialog_alert)
                 .setPositiveButton("Okay") { dialogInterface, which ->
                     alertDialog?.dismiss()
                 }
         else if (message.equals(""))
             builder = AlertDialog.Builder(this)
                 .setTitle(title)
                 .setIcon(android.R.drawable.ic_dialog_alert)
                 .setPositiveButton("Okay") { dialogInterface, which ->
                     alertDialog?.dismiss()
                 }
         else
             builder = AlertDialog.Builder(this)
                 .setTitle(title)
                 .setMessage(message)
                 .setIcon(android.R.drawable.ic_dialog_alert)
                 .setPositiveButton("Okay") { dialogInterface, which ->
                     if (title.equals("Session Expired")) {
                         logout()
                     } else {
                         alertDialog?.dismiss()
                     }
                 }
         alertDialog = builder.create()
         alertDialog.setCancelable(false)
         alertDialog.show()
     }
     private fun defaultBarcode(){
         isRFIDInit = true
         isBarcodeInit = false
         deInitScanner()
         Thread.sleep(1000)
         initReader()
     }
     private fun setDefaultScanner(){
         isRFIDInit = false
         isBarcodeInit = true
         rfidHandler!!.onPause()
         rfidHandler!!.onDestroy()
         Thread.sleep(1000)
         val results2 = EMDKManager.getEMDKManager(this@MainActivity, this)
         if (results2.statusCode != EMDKResults.STATUS_CODE.SUCCESS) {
             Log.e(TAG, "EMDKManager object request failed!")
         } else {
             Log.e(
                 TAG,
                 "EMDKManager object initialization is   in   progress......."
             )
         }
     }

    //////location
    private fun startLocationProviderCheck() {
        locationProviderCheckHandler.postDelayed(
            locationProviderCheckRunnable,
            LOCATION_PROVIDER_CHECK_INTERVAL.toLong()
        )
    }

    private fun stopLocationProviderCheck() {
        locationProviderCheckHandler.removeCallbacks(locationProviderCheckRunnable)
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
            checkLocationProviderStatus()
        }
    }

    private fun checkLocationProviderStatus() {
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // Location provider is turned off, update UI accordingly
           /* currentLatitude = 0.0
            currentLongitude = 0.0*/
           /* runOnUiThread(Runnable {
                binding.indicator.setImageResource(R.drawable.ic_circl_red)
            })*/
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            null
        )
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            // Handle location updates here
            for (location in locationResult.locations) {
                currentLatitude = location.latitude
                currentLongitude = location.longitude
                println("$currentLatitude-la,   $currentLongitude - lo")
                isLocationAvailable = true
                updateUIWithLocation(currentLatitude, currentLongitude, isLocationAvailable)
            }
        }
    }

    private fun updateUIWithLocation(
        latitude: Double,
        longitude: Double,
        isLocationAvailable: Boolean
    ) {
        // Update your UI elements with the new latitude and longitude values
        // For example, display them on TextViews or use them in calculations.
        if (isLocationAvailable) {

            if (PolyUtil.containsLocation(LatLng(latitude, longitude), coordinates, false)) {
                runOnUiThread {
                    binding.indicator.setImageResource(R.drawable.ic_circl_green)
                }
            } else {
                runOnUiThread {
                    binding.indicator.setImageResource(R.drawable.ic_circl_red)
                }
            }
        } else {
            runOnUiThread {
                binding.indicator.setImageResource(R.drawable.ic_circl_red)
            }
        }
    }

    // Handle permission request result
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, start location updates
                startLocationUpdates()
            } else {
                // Permission denied, handle this case
            }
        }
    }

    fun parseStringToList(inputString: String): ArrayList<LatLng> {
        val regex = Regex("\\((-?\\d+\\.\\d+),(-?\\d+\\.\\d+)\\)")
        val matches = regex.findAll(inputString)
        val latLngList = ArrayList<LatLng>()
        for (match in matches) {
            val (latitudeStr, longitudeStr) = match.destructured
            val latitude = latitudeStr.toDouble()
            val longitude = longitudeStr.toDouble()
            val latLng = LatLng(latitude, longitude)
            latLngList.add(latLng)
        }
        return latLngList
    }

    //viewmodel
    fun confirmVin(vin: String) {
        try {
            if (isLocationEnabled()) {
                checkLocationPermission()
            } else {
                openLocationSettings()
            }
            //val vinId = binding.edVinScan.text.toString()
            if (containsLocation(LatLng(currentLatitude, currentLongitude), coordinates, false)) {
               /* Toasty.success(
                    this@MainActivity,
                    "You are inside geofence",
                    Toasty.LENGTH_SHORT
                ).show()*/
                if (vin.isNotEmpty()) {
                   /* viewModel.validateVinConfirm(Constants.DMS_BASE_URL,
                        DMSVehicleConfirmationRequest(dealerCode!!,"Delivered",createUniqueIDWithDateTime(),vin))*/
                 /*   locationId?.let {
                        ConfirmDealerVehicleDeliveryRequest("$currentLatitude,$currentLongitude",dealerCode!!,
                            "Delivered",userName!!,vin,
                            it.toInt())
                    }?.let {
                        viewModel.postConfirmDealerVehicleDelivery (token!!, Constants.BASE_URL_LOCAL,
                            it
                        )
                    }*/
                    viewModel.postConfirmDealerVehicleDelivery (token!!, Constants.BASE_URL_LOCAL,
                        ConfirmDealerVehicleDeliveryRequest("$currentLatitude,$currentLongitude",dealerCode!!,
                            "Delivered",userName!!,"",vin,
                            locationId!!.toInt()))
                } else {
                    Toasty.warning(
                        this@MainActivity,
                        "please fill the required credentials",
                        Toasty.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toasty.error(
                    this@MainActivity,
                    "You are Not Inside geofence",
                    Toasty.LENGTH_SHORT
                ).show()
            }
        } catch (e: Exception) {
            Toasty.warning(
                this@MainActivity,
                e.printStackTrace().toString(),
                Toasty.LENGTH_SHORT
            ).show()
        }
    }

    fun submitVinBarcode(data: String?) {
        try {
          /*  if (isLocationEnabled()) {
                checkLocationPermission()
            } else {
                openLocationSettings()
            }*/
            if (containsLocation(LatLng(currentLatitude, currentLongitude), coordinates, false)) {
                viewModel.postVerifyDealerVehicle (token!!, Constants.BASE_URL_LOCAL, VerifyDealerVehicleRequest(data.toString(),"" ))
               /* viewModel.getVehicleInformation(Constants.DMS_BASE_URL,
                    GetVehicleInformationRequest(dealerCode!!,createUniqueIDWithDateTime(),data.toString()))*/
                runOnUiThread(Runnable {
                    binding.edVinScan.setText(data)
                })

            } else {
                runOnUiThread(Runnable {
                    Toasty.warning(
                        this@MainActivity,
                        "You are Not Inside geofence",
                        Toasty.LENGTH_SHORT
                    ).show()
                })

            }
        } catch (e: Exception) {
            runOnUiThread(Runnable {
                Toasty.warning(
                    this@MainActivity,
                    e.printStackTrace().toString(),
                    Toasty.LENGTH_SHORT
                ).show()
            })

        }
    }
     fun submitVinRFID(data: String?) {
        try {
            if (containsLocation(LatLng(currentLatitude, currentLongitude), coordinates, false)) {
                viewModel.postVerifyDealerVehicle (token!!, Constants.BASE_URL_LOCAL
                    ,VerifyDealerVehicleRequest(data.toString(),""))
          /*      viewModel.getVehicleInformation(Constants.DMS_BASE_URL,
                    GetVehicleInformationRequest(dealerCode!!,createUniqueIDWithDateTime(),data.toString()))*/
                runOnUiThread(Runnable {
                    binding.edVinScan.setText(data)
                })

            } else {
                runOnUiThread(Runnable {
                    Toasty.warning(
                        this@MainActivity,
                        "You are Not Inside geofence",
                        Toasty.LENGTH_SHORT
                    ).show()
                })

            }
        } catch (e: Exception) {
            runOnUiThread(Runnable {
                Toasty.warning(
                    this@MainActivity,
                    e.printStackTrace().toString(),
                    Toasty.LENGTH_SHORT
                ).show()
            })

        }
    }

    fun clearText() {
        val grey = Color.parseColor("#9A9A9A")
        binding.edVinScan.setText("")
        binding.tvVinValue.setText("")
        binding.tvModelCodeValue.setText("")
        binding.tvColorValue.setText("")
        binding.tvEngineNoValue.setText("")
        ViewCompat.setBackgroundTintList(
            binding.btnSubmit,
            ColorStateList.valueOf(grey)
        )
        binding.btnSubmit.isEnabled = false

    }

    private fun showProgressBar() {
        progress.show()
    }

    private fun hideProgressBar() {
        progress.cancel()
    }

    fun containsLocation(point: LatLng?, polygon: List<LatLng?>?, geodesic: Boolean): Boolean {
        return PolyUtil.containsLocation(point, polygon, geodesic)
    }

     override fun onStart() {
         super.onStart()
         if (resumeFlag) {
             resumeFlag = false
             if (Build.MANUFACTURER.contains("Zebra Technologies") || Build.MANUFACTURER.contains("Motorola Solutions")) {
                 //initBarcodeManager()
                 //initScanner()
             }
         }
     }
    ///Emdk scanner
    override fun onResume() {
        super.onResume()
        if (resumeFlag) {
            resumeFlag = false
            if (Build.MANUFACTURER.contains("Zebra Technologies") || Build.MANUFACTURER.contains("Motorola Solutions")) {
                //initBarcodeManager()
                //initScanner()
            }
        }
    }
    override fun handleTagdata(tagData: Array<TagData>) {
        val sb = StringBuilder()
        sb.append(tagData[0].tagID)
        runOnUiThread {
            var tagDataFromScan = tagData[0].tagID
            //binding.tvBarcode.setText(tagDataFromScan)
            Log.e(TAG, "RFID Data : $tagDataFromScan")
            stopInventory()
        }
        submitVinRFID(tagData[0].tagID.toString())
    }
  override fun onDestroy() {
      super.onDestroy()
      binding.unbind()
      if (isRFIDInit) {
          rfidHandler!!.onDestroy()
      }
      if (isBarcodeInit) {
          deInitScanner()
      }
      stopLocationProviderCheck()
  }

     override fun onPostResume() {
         super.onPostResume()
         if (isRFIDInit) {
             val status = rfidHandler!!.onResume()
             Toast.makeText(this@MainActivity, status, Toast.LENGTH_SHORT).show()
         }

     }
     override fun onPause() {
         super.onPause()
         if (isRFIDInit) {
             rfidHandler!!.onPause()
         }
         if (isBarcodeInit) {
             deInitScanner()
         }
         resumeFlag = true
     }
    fun performInventory() {
        rfidHandler!!.performInventory()
    }

    fun stopInventory() {
        rfidHandler!!.stopInventory()
    }
    override fun handleTriggerPress(pressed: Boolean) {
        if (pressed) {
            performInventory()
        } else stopInventory()

    }
     fun createUniqueIDWithDateTime(): String {
         val currentTime = Date()
         val formatter = SimpleDateFormat("yyyyMMddHHmmss")
         val formattedDateTime = formatter.format(currentTime)
         val randomPart = Random.nextInt(1000) // Add randomness to avoid collisions
         return "$formattedDateTime$randomPart"
     }


//new emd
override fun onOpened(emdkManager: EMDKManager?) {
    if (Build.MANUFACTURER.contains("Zebra Technologies") || Build.MANUFACTURER.contains("Motorola Solutions")) {
        this.emdkManager = emdkManager
        //initBarcodeManager()
        //initScanner()
    }
}

     override fun onClosed() {
         if (emdkManager != null) {
             emdkManager!!.release()
             emdkManager = null
         }
     }

     fun initBarcodeManager() {
         barcodeManager = emdkManager!!.getInstance(EMDKManager.FEATURE_TYPE.BARCODE) as BarcodeManager
         if (barcodeManager == null) {
             Toast.makeText(
                 this@MainActivity,
                 "Barcode scanning is not supported.",
                 Toast.LENGTH_LONG
             ).show()
             finish()
         }
     }

     fun initScanner() {
         if (scanner == null) {
             barcodeManager =
                 emdkManager?.getInstance(EMDKManager.FEATURE_TYPE.BARCODE) as BarcodeManager
             scanner = barcodeManager!!.getDevice(BarcodeManager.DeviceIdentifier.DEFAULT)
             scanner?.addDataListener(this)
             scanner?.addStatusListener(this)
             scanner?.triggerType = Scanner.TriggerType.HARD
             try {
                 scanner?.enable()
             } catch (e: ScannerException) {
                 e.printStackTrace()
             }
         }
     }
     fun deInitScanner() {
         if (scanner != null) {
             try {
                 scanner!!.release()
             } catch (e: Exception) {
             }
             scanner = null
         }
     }

     override fun onData(scanDataCollection: ScanDataCollection?) {
         var dataStr: String? = ""
         if (scanDataCollection != null && scanDataCollection.result == ScannerResults.SUCCESS) {
             val scanData = scanDataCollection.scanData
             for (data in scanData) {
                 val barcodeData = data.data
                 val labelType = data.labelType
                 dataStr = barcodeData
             }
             submitVinBarcode(dataStr)
             runOnUiThread(Runnable {
                 binding.edVinScan.setText(dataStr)
             })
         }
     }

     override fun onStatus(statusData: StatusData) {
         val state = statusData.state
         var statusStr = ""
         when (state) {
             StatusData.ScannerStates.IDLE -> {
                 statusStr = statusData.friendlyName + " is   enabled and idle..."
                 setConfig()
                 try {
                     scanner!!.read()
                 } catch (e: ScannerException) {
                 }
             }
             StatusData.ScannerStates.WAITING -> statusStr = "Scanner is waiting for trigger press..."
             StatusData.ScannerStates.SCANNING -> statusStr = "Scanning..."
             StatusData.ScannerStates.DISABLED -> {}
             StatusData.ScannerStates.ERROR -> statusStr = "An error has occurred."
             else -> {}
         }
         setStatusText(statusStr)
     }

     private fun setConfig() {
         if (scanner != null) {
             try {
                 val config = scanner!!.config
                 if (config.isParamSupported("config.scanParams.decodeHapticFeedback")) {
                     config.scanParams.decodeHapticFeedback = true
                 }
                 scanner!!.config = config
             } catch (e: ScannerException) {
                 Log.e(TAG, e.message!!)
             }
         }
     }

     fun setStatusText(msg: String) {
         Log.e(TAG, "StatusText: $msg")
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
}


/*
binding.clearText.setOnClickListener {
    */
/*  try {
          val vinId = binding.edVinScan.text.toString()*//*

    clearText()
    */
/*    if (containsLocation(LatLng(currentLatitude, currentLongitude), coordinates, false)) {
            viewModel.validateVin(token!!, Constants.BASE_URL, vinId)
          *//*
*/
/*  runOnUiThread(Runnable {
                    binding.edVinScan.setText(data)
                })*//*
*/
/*
                //viewModel.validateVinConfirm(token!!,Constants.BASE_URL, dealerCode!!,vinId)
            } else {
                runOnUiThread(Runnable {
                    Toasty.warning(
                        this@MainActivity,
                        "You are Not Inside geofence",
                        Toasty.LENGTH_SHORT
                    ).show()
                })

            }
        } catch (e: Exception) {
            runOnUiThread(Runnable {
                Toasty.warning(
                    this@MainActivity,
                    e.printStackTrace().toString(),
                    Toasty.LENGTH_SHORT
                ).show()
            })

        }*//*

}*/
