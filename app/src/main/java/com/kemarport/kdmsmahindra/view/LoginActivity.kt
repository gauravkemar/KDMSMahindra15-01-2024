package com.kemarport.kdmsmahindra.view

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.kemarport.kdmsmahindra.R
import com.kemarport.kdmsmahindra.databinding.ActivityLoginBinding
import com.kemarport.kdmsmahindra.helper.SessionManager
import com.kemarport.kdmsmahindra.model.login.LoginRequest
import com.kemarport.kdmsmahindra.model.login.LoginResponse
import com.kemarport.kdmsmahindra.repository.KDMSRepository
import com.kemarport.kdmsmahindra.viewmodel.LoginVM
import com.kemarport.kdmsmahindra.viewmodel.LoginVMPF
import com.kemarport.mahindrakiosk.helper.Constants
import com.kemarport.mahindrakiosk.helper.Resource
import com.kemarport.mahindrakiosk.helper.Utils
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.HashMap
import java.net.URL

class LoginActivity : AppCompatActivity() {
    lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: LoginVM
    private lateinit var progress: ProgressDialog
    private lateinit var session: SessionManager
    private lateinit var userDetails: HashMap<String, String?>
    private var baseUrl: String = ""
    private var serverIpSharedPrefText: String? = null
    private var serverHttpPrefText: String? = null
    var installedVersionCode = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setContentView(R.layout.activity_login)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        supportActionBar?.hide()

        Toasty.Config.getInstance()
            .setGravity(Gravity.CENTER)
            .apply()

        progress = ProgressDialog(this)
        progress.setMessage("Please Wait...")
        Log.d("thisisdeviceid", Utils.getDeviceId(this@LoginActivity))

        if (Utils.getSharedPrefs(this@LoginActivity, Constants.SET_ANTENNA_POWER).isNullOrEmpty()) {
            Utils.setSharedPrefs(this@LoginActivity, Constants.SET_ANTENNA_POWER, "120")
        }
        //if (Utils.getSharedPrefs(this@LoginActivity, Constants.KEY_SERVER_IP).isNullOrEmpty()) {
        Utils.setSharedPrefs(this@LoginActivity, Constants.KEY_SERVER_IP, "kydms.kemar.in/service")
        // }
        if (Utils.getSharedPrefs(this@LoginActivity, Constants.KEY_HTTP).isNullOrEmpty()) {
            Utils.setSharedPrefs(this@LoginActivity, Constants.KEY_HTTP, "https")
        }

        val packageManager = applicationContext.packageManager

        try {
            val androidId: String =
                Settings.Secure.getString(this.contentResolver, Settings.Secure.ANDROID_ID)
            Log.d("android device id", androidId)
            // Get the package info for the app
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            val versionCode = packageInfo.versionCode
            // Retrieve the version code and version name
            val versionName = packageInfo.versionName
            installedVersionCode = versionCode
            // binding.tvBuildNo.setText(installedVersionCode.toString())
            binding.tvVersion1.setText(versionName.toString())
            binding.tvDeviceId.setText(androidId)
            // Log or display the version information

            Log.d("Version", "Version Name: $versionName")
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        Log.d("Version", "Version Code: $installedVersionCode")


        val kymsRepository = KDMSRepository()
        val viewModelProviderFactory = LoginVMPF(application, kymsRepository)
        viewModel = ViewModelProvider(this, viewModelProviderFactory)[LoginVM::class.java]
        session = SessionManager(this)
        userDetails = session.getUserDetails()


        serverIpSharedPrefText = userDetails!![Constants.KEY_SERVER_IP].toString()
        serverHttpPrefText = userDetails!![Constants.KEY_HTTP].toString()
        baseUrl = "$serverHttpPrefText://$serverIpSharedPrefText"


        binding.tvDeviceId.setText("${Utils.getDeviceId(this@LoginActivity)}")



        binding.btnLogin.setOnClickListener {
            login()
            //startActivity(Intent(this@LoginActivity,VinRfidMappingActivity::class.java))
        }
        viewModel.loginMutableLiveData.observe(this) { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    response.data?.let { resultResponse ->
                        try {

                            if (resultResponse.roleName.equals("Dealer")) {
                                val missing = getMissingFields(resultResponse)
                                if (missing.isEmpty()) {
                                    session.createLoginSession(
                                        resultResponse.firstName,
                                        resultResponse.lastName,
                                        resultResponse.email,
                                        resultResponse.mobileNumber.toString(),
                                        resultResponse.isVerified.toString(),
                                        resultResponse.userName,
                                        resultResponse.jwtToken,
                                        resultResponse.refreshToken,
                                        resultResponse.roleName,
                                        resultResponse.dealerCode,
                                        resultResponse.dealerCoordinates?.joinToString(";"),
                                        resultResponse.locationId.toString(),
                                        resultResponse.dealerName
                                    )


                                    val dealerCoordinates =
                                        resultResponse.dealerCoordinates ?: emptyList()

                                    if (dealerCoordinates.isNotEmpty()) {
                                        // save only valid ones
                                        session.saveDealerCoordinates(
                                            this@LoginActivity,
                                            dealerCoordinates
                                        )
                                    } else {
                                        Log.d("Login", "No dealer coordinates found")
                                    }

                                    if (resultResponse.isPasswordReset) {

                                        if (resultResponse.locationsWithoutCoordinates == 0) {

                                            navigation(
                                                HomeActivity::class.java,
                                                "",
                                                "",
                                                ""
                                            )
                                            Utils.setSharedPrefsBoolean(
                                                this@LoginActivity,
                                                Constants.LOGGEDIN,
                                                true
                                            )

                                        }
                                        /*   else
                                           {

                                               navigation(
                                                   NewUserSetUserGeofenceActivity::class.java,
                                                   "",
                                                   resultResponse.userName,
                                                   resultResponse.dealerCode

                                               )
                                           }*/

                                    } else {
                                        var edPassword = binding.edPass.text.toString().trim()
                                        navigation(
                                            FIrstTimePasswordChangeActivity::class.java,
                                            edPassword,
                                            "",
                                            "",
                                        )

                                    }


                                } else {
                                    Log.e(
                                        "LoginSession",
                                        "Missing fields: ${missing.joinToString()}"
                                    )
                                    Toasty.warning(
                                        this@LoginActivity,
                                        "Login Failed : Missing : ${missing.joinToString()}",
                                        Toasty.LENGTH_SHORT
                                    ).show()
                                }
                            } else {
                                Toasty.warning(
                                    this@LoginActivity,
                                    "Invalid User",
                                    Toasty.LENGTH_SHORT
                                ).show()
                            }
                        } catch (e: Exception) {
                            Toasty.warning(
                                this@LoginActivity,
                                "hello" + e.printStackTrace().toString(),
                                Toasty.LENGTH_SHORT
                            ).show()
                        }

                    }
                }

                is Resource.Error -> {
                    hideProgressBar()
                    response.message?.let { errorMessage ->
                        Toasty.error(
                            this@LoginActivity,
                            "Login failed - \nError Message: $errorMessage"
                        ).show()
                    }
                }

                is Resource.Loading -> {
                    showProgressBar()
                }
            }
        }
        viewModel.getAppDetailsMutableLiveData.observe(this) { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    try {
                        when (response.data?.statusCode) {
                            Constants.HTTP_OK -> {
                                response.data.responseObject?.let { resultResponse ->
                                    if (resultResponse.apkVersion > installedVersionCode) {
                                        showUpdateDialog(
                                            resultResponse.apkFileUrl,
                                            resultResponse.isMandatory
                                        )
                                    }
                                }
                            }

                            Constants.HTTP_ERROR -> {
                                Toasty.error(
                                    this,
                                    response.data.errorMessage.toString(),
                                    Toasty.LENGTH_LONG
                                ).show()
                            }

                            else -> {
                                Toasty.error(
                                    this,
                                    "Submission Failed, statusCode - ${response.data?.statusCode}",
                                    Toasty.LENGTH_LONG
                                ).show()
                            }
                        }
                    } catch (e: Exception) {

                    }

                }

                is Resource.Error -> {

                    hideProgressBar()
                    response.message?.let { errorMessage ->
                        Toasty.error(this@LoginActivity, errorMessage).show()
                    }
                }

                is Resource.Loading -> {
                    showProgressBar()
                }
            }
        }

        binding.tvForgotPassword.setOnClickListener {
            startForgetPassActvity()
        }
    }

    fun getMissingFields(response: LoginResponse): List<String> {
        val missing = mutableListOf<String>()
        with(response) {
            if (jwtToken == null) missing.add("jwtToken")
            if (refreshToken == null) missing.add("refreshToken")
            if (dealerCode == null) missing.add("dealerCode")
            if (locationId == null) missing.add("locationId")
            if (dealerName == null) missing.add("dealerName")
        }
        return missing
    }

    override fun onResume() {
        super.onResume()
        try {
            viewModel.getAppDetails(this@LoginActivity)
        } catch (e: Exception) {
            Toasty.warning(
                this,
                Constants.EXCEPTION_ERROR,
                Toasty.LENGTH_SHORT
            ).show()
        }
    }

    private fun navigation(
        activityClass: Class<out Activity>,
        edPassword: String,
        userName: String,
        dealerCode: String,
    ) {
        if (edPassword.isNotEmpty()) {
            val intent = Intent(this@LoginActivity, activityClass)
            intent.putExtra("userPass", edPassword)
            startActivity(intent)
        } else {
            val intent = Intent(this@LoginActivity, activityClass)
            intent.putExtra("userName", userName)
            intent.putExtra("dealerCode", dealerCode)
            startActivity(intent)
        }

    }

    fun startForgetPassActvity() {
        startActivity(Intent(this@LoginActivity, ForgotPasswordActivity::class.java))
        finish()
    }

    private fun showUpdateDialog(appUrl: String, isMandatory: Boolean) {
        val dialogBuilder = AlertDialog.Builder(this)
            .setTitle("Update Available")
            .setMessage("A new version of the app is available. Do you want to update?")
            .setCancelable(false)
            .setPositiveButton("Update") { dialog, _ ->
                dialog.dismiss()
                val destinationFolder =
                    getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.absolutePath
                        ?: return@setPositiveButton
                GlobalScope.launch(Dispatchers.IO) {
                    downloadFileNew(appUrl, destinationFolder)
                }
            }

        if (!isMandatory) {
            dialogBuilder.setNegativeButton("Skip") { dialog, _ -> dialog.dismiss() }
        }

        dialogBuilder.show()
    }

    private fun downloadFileNew(url: String, destinationFolder: String) {
        try {
            val fileUrl = URL(url)
            val fileName = fileUrl.file.substring(fileUrl.file.lastIndexOf("/") + 1)
            val destinationFile = File(destinationFolder, fileName)

            val connection = fileUrl.openConnection()
            connection.connect()

            val inputStream = connection.getInputStream()
            val outputStream = FileOutputStream(destinationFile)
            val buffer = ByteArray(1024)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }
            outputStream.close()
            inputStream.close()

            runOnUiThread {
                showMessageDialog("Update downloaded successfully")
                installApkFile(destinationFile)
            }
        } catch (e: Exception) {
            runOnUiThread {
                showMessageDialog("Error downloading file: ${e.message}")
            }
        }
    }

    private fun installApkFile(apkFile: File) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val apkUri = FileProvider.getUriForFile(this, "${packageName}.provider", apkFile)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
        } else {
            val apkUri = Uri.fromFile(apkFile)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
        }
    }

    private fun showMessageDialog(message: String) {
        AlertDialog.Builder(this)
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    fun login() {
        try {
            // Fetching user credentials from input fields
            val userId = binding.edUsername.text.toString().trim()
            val password = binding.edPass.text.toString().trim()
            if (userId == "admin" && password == "Pass@123") {
                startAdmin()
            } else {
                val validationMessage = validateInput(userId, password)
                if (validationMessage == null) {
                    //Utils.getDeviceId(this@LoginActivity)
                    val loginRequest =
                        LoginRequest(Utils.getDeviceId(this@LoginActivity), password, userId)
                    viewModel.login(this@LoginActivity, loginRequest)
                } else {
                    showErrorMessage(validationMessage)
                }
            }
            // Validate user input

        } catch (e: Exception) {
            showErrorMessage(e.printStackTrace().toString())
        }
    }

    fun startAdmin() {
        startActivity(Intent(this@LoginActivity, AdminSettingsActivity::class.java))
        finish()
    }

    private fun validateInput(userId: String, password: String): String? {
        return when {
            userId.isEmpty() || password.isEmpty() -> "Please enter valid credentials"
            userId.length < 5 -> "Please enter at least 5 characters for the username"
            password.length < 6 -> "Please enter a password with more than 6 characters"
            else -> null
        }
    }

    private fun showErrorMessage(message: String) {
        Toasty.warning(this@LoginActivity, message, Toasty.LENGTH_SHORT).show()
    }

    private fun showProgressBar() {
        progress.show()
    }

    private fun hideProgressBar() {
        progress.cancel()
    }
}