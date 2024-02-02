package com.kemarport.kdmsmahindra.view

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.kemarport.kdmsmahindra.R
import com.kemarport.kdmsmahindra.databinding.ActivityForgotPasswordBinding
import com.kemarport.kdmsmahindra.helper.SessionManager
import com.kemarport.kdmsmahindra.model.forgotpassword.ForgotPasswordRequest
import com.kemarport.kdmsmahindra.model.forgotpassword.ResetPasswordRequest
import com.kemarport.kdmsmahindra.repository.KDMSRepository
import com.kemarport.kdmsmahindra.viewmodel.LoginVM
import com.kemarport.kdmsmahindra.viewmodel.LoginVMPF
import com.kemarport.mahindrakiosk.helper.Constants
import com.kemarport.mahindrakiosk.helper.Resource
import es.dmoral.toasty.Toasty
import java.util.HashMap

class ForgotPasswordActivity : AppCompatActivity() {
    lateinit var binding: ActivityForgotPasswordBinding

    //get values from session
    private lateinit var userDetails: HashMap<String, String?>
    private lateinit var session: SessionManager
    private lateinit var viewModel: LoginVM
    private var userName: String? = ""
    private lateinit var progress: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_forgot_password)

        binding.forgetPasswordToolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.white))
        setSupportActionBar(binding.forgetPasswordToolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        progress = ProgressDialog(this)
        progress.setMessage("Loading...")
        session = SessionManager(this)
        userDetails = session.getUserDetails()
        userName = userDetails[Constants.KEY_USER_NAME].toString()
        val kdmsRepository = KDMSRepository()
        val viewModelProviderFactory =
            LoginVMPF(application, kdmsRepository)
        viewModel =
            ViewModelProvider(this, viewModelProviderFactory)[LoginVM::class.java]

        viewModel.forgotPasswordMutableLiveData.observe(this) { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    binding.clTop.visibility = View.GONE
                    binding.clBot.visibility = View.VISIBLE
                    response.data?.let { resultResponse ->
                        var errorMessage = resultResponse.errorMessage
                        var responseMessage = resultResponse.forgetPasswordOTP
                        if (errorMessage != null) {
                            Toasty.error(
                                this@ForgotPasswordActivity,
                                "Error Message: $errorMessage"
                            ).show()
                        } /*else if (responseMessage.isNotEmpty()) {
                            Toasty.success(
                                this@ForgotPasswordActivity,
                                "Success Message: $responseMessage"
                            ).show()
                        }*/

                    }
                }

                is Resource.Error -> {
                    hideProgressBar()
                    binding.clTop.visibility = View.VISIBLE
                    binding.clBot.visibility = View.GONE
                    response.message?.let { resultResponse ->
                        Toasty.error(
                            this@ForgotPasswordActivity,
                            "Error Message: $resultResponse"
                        ).show()
                        /*  if (resultResponse == "Session Expired ! Please relogin" || resultResponse == "Authentication token expired" ||
                              resultResponse == Constants.CONFIG_ERROR) {
                              showCustomDialog(
                                  "Session Expired",
                                  "Please re-login to continue"
                              )
                          }*/
                    }
                }

                is Resource.Loading -> {
                    showProgressBar()
                }
            }
        }
        viewModel.resetPasswordMutableLiveData.observe(this) { response ->
            when (response) {
                is Resource.Success -> {
                    logout()
                    hideProgressBar()
                    response.data?.let { resultResponse ->
                        var errorMessage = resultResponse.errorMessage
                        var responseMessage = resultResponse.responseMessage
                         if ( errorMessage!=null ) {
                             Toasty.error(
                                 this@ForgotPasswordActivity,
                                 "Error Message: $errorMessage"
                             ).show()
                         }
                    }
                }

                is Resource.Error -> {
                    hideProgressBar()
                    response.message?.let { resultResponse ->
                        Toasty.error(
                            this@ForgotPasswordActivity,
                            "Error Message: $resultResponse"
                        ).show()
                        /* if (resultResponse == "Session Expired ! Please relogin" || resultResponse == "Authentication token expired" ||
                             resultResponse == Constants.CONFIG_ERROR) {
                             showCustomDialog(
                                 "Session Expired",
                                 "Please re-login to continue"
                             )
                         }*/
                    }
                }

                is Resource.Loading -> {
                    showProgressBar()
                }
            }
        }
        binding.btnSubmit.setOnClickListener {
            submit()
        }
        binding.btnClear.setOnClickListener {
            clearBtn()
        }
        binding.edNewPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not needed for this example
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Not needed for this example
            }

            override fun afterTextChanged(s: Editable?) {
                // Validate the entered text
               /* val inputText = s.toString()
                if (isValidInput(inputText)) {
                    // The input is valid
                    binding.edNewPassword.error = null
                } else {
                    // The input is not valid, show an error
                    binding.edNewPassword.error = "Invalid input. Please follow the specified criteria."
                }*/
                val inputText = s.toString()
                updatePasswordCriteria(inputText)
            }
        })

    }

    private fun isValidInput(input: String): Boolean {
        val regex = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#$%^&+=]).{6,}$"
        return input.matches(regex.toRegex())
    }
    private fun updatePasswordCriteria(input: String) {

        val containsUpperCase = input.any { it.isUpperCase() }
        val containsLowerCase = input.any { it.isLowerCase() }
        val containsDigit = input.any { it.isDigit() }
        val containsSpecialCharacter = input.any { it in "@#$%^&+=" }
        val hasValidLength = input.length >= 6

        val criteriaStringBuilder = StringBuilder()

        if (!containsUpperCase) {
            criteriaStringBuilder.append("* should contain A-Z\n")
        }
        if (!containsLowerCase) {
            criteriaStringBuilder.append("* should contain a-z\n")
        }
        if (!containsDigit) {
            criteriaStringBuilder.append("* should contain 0-9\n")
        }
        if (!containsSpecialCharacter) {
            criteriaStringBuilder.append("* should contain special characters (@#$%^&+=)\n")
        }
        if (!hasValidLength) {
            criteriaStringBuilder.append("* should contain least 6 characters\n")
        }

        binding.tvPassRequirement.visibility = if (input.isNotEmpty() && criteriaStringBuilder.toString().trim().isNotEmpty()) {
            View.VISIBLE
        } else {
            View.GONE
        }
        binding.tvPassRequirement.setText(criteriaStringBuilder.toString().trim())

    }

    private fun logout() {
        session.logoutUser()
        startActivity(Intent(this@ForgotPasswordActivity, LoginActivity::class.java))
        finish()
    }

    private fun submit() {
        try {
            if (binding.clTop.visibility == View.VISIBLE) {
                forgotPass()
            } else if (binding.clBot.visibility == View.VISIBLE) {
                showConfirmDialog()
            }
        } catch (e: Exception) {

        }
    }

    private fun clearBtn() {
        try {
            if (binding.clTop.visibility == View.VISIBLE) {
                clearTop()
            } else if (binding.clBot.visibility == View.VISIBLE) {
                clearBtm()
            }
        } catch (e: Exception) {

        }
    }

    private fun clearTop() {
        binding.edEmail.setText("")
        binding.edUserName.setText("")
    }

    private fun clearBtm() {
        binding.edNewPassword.setText("")
        binding.edToken.setText("")
        binding.edConfirmPassword.setText("")


    }

    private fun forgotPass() {
        try {
            var emailId = binding.edEmail.text.toString().trim()
            var userName = binding.edUserName.text.toString().trim()

            val validateForgotPassInput = validateForgotPassInput(emailId, userName)
            if (validateForgotPassInput == null) {
                viewModel.forgotPassword(
                    Constants.BASE_URL,
                    ForgotPasswordRequest("", userName)
                )
            } else {
                Toasty.error(
                    this@ForgotPasswordActivity,
                    validateForgotPassInput,
                ).show()
            }
        } catch (e: Exception) {
            Toasty.error(
                this@ForgotPasswordActivity,
                "Exception Message: $e"
            ).show()
        }
    }

    private fun resetPass() {
        try {
            var token = binding.edToken.text.toString().trim()
            var newPassword = binding.edNewPassword.text.toString().trim()
            var confirmPassword = binding.edConfirmPassword.text.toString().trim()

            val validateForgotPassInput =
                validateInput(token, newPassword, confirmPassword)
            if (validateForgotPassInput == null) {
                viewModel.resetPassword(
                    Constants.BASE_URL,
                    ResetPasswordRequest(confirmPassword, newPassword, token)
                )
            } else {
                Toasty.error(
                    this@ForgotPasswordActivity,
                    validateForgotPassInput,
                ).show()
            }
        } catch (e: Exception) {
            Toasty.error(
                this@ForgotPasswordActivity,
                "Exception Message: $e"
            ).show()
        }
    }

    private fun showConfirmDialog() {
        AlertDialog.Builder(this)
            .setTitle("Reset Password")
            .setMessage("Are you sure? Would you like to Reset the password?")
            .setCancelable(true)
            .setPositiveButton("Submit") { dialog, _ ->
                try {
                    resetPass()
                } catch (e: Exception) {
                    Toasty.error(
                        this@ForgotPasswordActivity,
                        "This is exception " + e.toString()
                    ).show()
                }

            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()

    }

    private fun validateInput(currentPass: String, newPass: String, confirmPass: String): String? {
        return when {
            !isValidInput(newPass)->"Invalid password format!!."
            currentPass.isEmpty() -> "Please enter the old password"
            newPass.isEmpty() -> "Please enter the new password"
            confirmPass.isEmpty() -> "Please confirm the new password"
            currentPass.length < 6 -> "Old password should have at least 6 characters"
            newPass.length < 6 -> "New password should have at least 6 characters"
            newPass != confirmPass -> "New password and confirm password do not match"
            else -> null
        }
    }

    private fun validateForgotPassInput(emailId: String, UserName: String): String? {
        return when {
            //emailId.isEmpty() -> "Please enter the required Credentials!"
            UserName.isEmpty() -> "Please enter the required Credentials!"
            else -> null
        }
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
    fun showProgressBar() {
        progress.show()
    }

    fun hideProgressBar() {
        progress.cancel()
    }
}