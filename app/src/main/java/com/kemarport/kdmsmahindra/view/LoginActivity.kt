package com.kemarport.kdmsmahindra.view

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.kemarport.kdmsmahindra.R
import com.kemarport.kdmsmahindra.databinding.ActivityLoginBinding
import com.kemarport.kdmsmahindra.helper.SessionManager
import com.kemarport.kdmsmahindra.model.login.LoginRequest
import com.kemarport.kdmsmahindra.repository.KDMSRepository
import com.kemarport.kdmsmahindra.viewmodel.LoginVM
import com.kemarport.kdmsmahindra.viewmodel.LoginVMPF
import com.kemarport.mahindrakiosk.helper.Constants
import com.kemarport.mahindrakiosk.helper.Resource
import com.kemarport.mahindrakiosk.helper.Utils
import es.dmoral.toasty.Toasty

class LoginActivity : AppCompatActivity() {
    lateinit var binding:ActivityLoginBinding
    private lateinit var viewModel: LoginVM
    private lateinit var progress: ProgressDialog
    private lateinit var session: SessionManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setContentView(R.layout.activity_login)
        binding= DataBindingUtil.setContentView(this,R.layout.activity_login)
        supportActionBar?.hide()

        Toasty.Config.getInstance()
            .setGravity(Gravity.CENTER)
            .apply()

        progress = ProgressDialog(this)
        progress.setMessage("Please Wait...")
        Log.d("thisisdeviceid",Utils.getDeviceId(this@LoginActivity))

        val kymsRepository = KDMSRepository()
        val viewModelProviderFactory = LoginVMPF(application, kymsRepository)
        viewModel = ViewModelProvider(this, viewModelProviderFactory)[LoginVM ::class.java]
        session = SessionManager(this)


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

                            if(resultResponse.roleName.equals("Dealer"))
                            {
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
                                    resultResponse.coordinates,
                                    resultResponse.locationId.toString()
                                )
                                Utils.setSharedPrefsBoolean(this@LoginActivity, Constants.LOGGEDIN, true)
                                startActivity()
                            }
                            else
                            {
                                Toasty.warning(
                                    this@LoginActivity,
                                    "Invalid User",
                                    Toasty.LENGTH_SHORT
                                ).show()
                            }

                         /*   if(resultResponse.roleName.equals("Driver",true) || resultResponse.roleName.equals("Supervisor",true))
                            {
                                startActivity()
                            }
                            else if(resultResponse.roleName.equals("Dealer",true))
                            {

                            }*/

                        } catch (e: Exception) {
                            Toasty.warning(
                                this@LoginActivity,
                                "hello"+e.printStackTrace().toString(),
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

        binding.tvForgotPassword.setOnClickListener {
            startForgetPassActvity()
        }
    }
    fun startActivity()
    {
        startActivity(Intent(this@LoginActivity,HomeActivity::class.java))
        finish()
    }
    fun startForgetPassActvity()
    {
        startActivity(Intent(this@LoginActivity,ForgotPasswordActivity::class.java))
        finish()
    }
   /* fun login()
    {
        try {
            val userId = binding.edUsername.text.toString()
            val pass = binding.edPass.text.toString()
            if (userId.isNotEmpty() && pass.isNotEmpty()) {
                val loginRequest = LoginRequest(Utils.getDeviceId(this@LoginActivity),pass, userId)
                viewModel.login(Constants.BASE_URL_LOCAL, loginRequest)
            }
            else
            {
                Toasty.warning(
                    this@LoginActivity,
                    "please fill the required credentials",
                    Toasty.LENGTH_SHORT
                ).show()
            }
        }catch (e: Exception) {
            Toasty.warning(
                this@LoginActivity,
                e.printStackTrace().toString(),
                Toasty.LENGTH_SHORT
            ).show()
        }
    }*/
   fun login() {
       try {
           // Fetching user credentials from input fields
           val userId = binding.edUsername.text.toString().trim()
           val password = binding.edPass.text.toString().trim()

           // Validate user input
           val validationMessage = validateInput(userId, password)
           if (validationMessage == null) {
               val loginRequest = LoginRequest(Utils.getDeviceId(this@LoginActivity),password, userId )
               viewModel.login(Constants.BASE_URL, loginRequest)
           } else {
               showErrorMessage(validationMessage)
           }
       } catch (e: Exception) {
           showErrorMessage(e.printStackTrace().toString())
       }
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