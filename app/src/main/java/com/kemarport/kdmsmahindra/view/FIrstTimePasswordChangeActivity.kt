package com.kemarport.kdmsmahindra.view

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.kemarport.kdmsmahindra.R
import com.kemarport.kdmsmahindra.databinding.ActivityFirstTimePasswordChangeBinding
import com.kemarport.kdmsmahindra.helper.SessionManager
import com.kemarport.kdmsmahindra.model.changepassword.ChangePasswordRequest
import com.kemarport.kdmsmahindra.repository.KDMSRepository
import com.kemarport.kdmsmahindra.viewmodel.LoginVM
import com.kemarport.kdmsmahindra.viewmodel.LoginVMPF
import com.kemarport.mahindrakiosk.helper.Constants
import com.kemarport.mahindrakiosk.helper.Resource
import es.dmoral.toasty.Toasty
import java.util.HashMap

class FIrstTimePasswordChangeActivity : AppCompatActivity() {
    lateinit var binding: ActivityFirstTimePasswordChangeBinding

    private lateinit var userDetails: HashMap<String, String?>
    private lateinit var session: SessionManager
    private lateinit var viewModel: LoginVM
    private var userName: String? = ""
    private var token: String? = ""
    private lateinit var progress: ProgressDialog
    private var baseUrl: String = ""
    private var serverIpSharedPrefText: String? = null
    private var serverHttpPrefText: String? = null
    var edPassword = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this@FIrstTimePasswordChangeActivity,
            R.layout.activity_first_time_password_change
        )
        setSupportActionBar(binding.changePasswordToolbar)

        edPassword = intent.getStringExtra("userPass").toString()
        progress = ProgressDialog(this)
        progress.setMessage("Loading...")
        session = SessionManager(this)
        userDetails = session.getUserDetails()
        userName = userDetails[Constants.KEY_USER_NAME].toString()
        token = userDetails[Constants.KEY_JWT_TOKEN].toString()
        serverIpSharedPrefText = userDetails!![Constants.KEY_SERVER_IP].toString()
        serverHttpPrefText = userDetails!![Constants.KEY_HTTP].toString()
        baseUrl = "$serverHttpPrefText://$serverIpSharedPrefText"
        val kdmsRepository = KDMSRepository()
        val viewModelProviderFactory =
            LoginVMPF(application, kdmsRepository)
        viewModel =
            ViewModelProvider(this, viewModelProviderFactory)[LoginVM::class.java]

        viewModel.changePasswordMutableLiveData.observe(this) { response ->
            when (response) {
                is Resource.Success -> {
                    logout()
                    hideProgressBar()
                    response.data?.let { resultResponse ->
                        var message = resultResponse.message

                        if (message.isNotEmpty() || message != null || message != "null") {
                            Toasty.error(
                                this@FIrstTimePasswordChangeActivity,

                                "Message: $message"
                            ).show()
                        }
                    }
                }

                is Resource.Error -> {
                    hideProgressBar()
                    response.message?.let { resultResponse ->
                        Toasty.error(
                            this@FIrstTimePasswordChangeActivity,
                            "Error Message: $resultResponse"
                        ).show()
                        if (resultResponse == "Session Expired ! Please relogin" || resultResponse == "Authentication token expired" ||
                            resultResponse == Constants.CONFIG_ERROR
                        ) {
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
            }
        }
        binding.btnSubmit.setOnClickListener {
            showConfirmDialog()
        }


        binding.edNewPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not needed for this example
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Not needed for this example
            }

            override fun afterTextChanged(s: Editable?) {
                val inputText = s.toString()
                updatePasswordCriteria(inputText)
            }
        })
        binding.btnClear.setOnClickListener {
            clearTextFields()
        }
    }

    private fun clearTextFields() {
        binding.edNewPassword.setText("")
        binding.edConfirmPassword.setText("")
    }

    private fun logout() {
        session.logoutUser()
        startActivity(Intent(this@FIrstTimePasswordChangeActivity, LoginActivity::class.java))
        finish()
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

        binding.tvPassRequirement.visibility =
            if (input.isNotEmpty() && criteriaStringBuilder.toString().trim().isNotEmpty()) {
                View.VISIBLE
            } else {
                View.GONE
            }

        binding.tvPassRequirement.setText(criteriaStringBuilder.toString().trim())

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

    private fun showConfirmDialog() {
        AlertDialog.Builder(this)
            .setTitle("Change Password")
            .setMessage("Are you sure? Would you like to Change the password?")
            .setCancelable(true)
            .setPositiveButton("Submit") { dialog, _ ->
                try {
                    changePassSubmit()
                } catch (e: Exception) {
                    Toasty.error(
                        this@FIrstTimePasswordChangeActivity,
                        "This is exception " + e.toString()
                    ).show()
                }

            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()

    }

    private fun changePassSubmit() {
        // Fetching user credentials from input fields

        val edNewPassword = binding.edNewPassword.text.toString().trim()
        val edConfirmPassword = binding.edConfirmPassword.text.toString().trim()

        // Validate user input
        val validationMessage = validateInput(edNewPassword, edConfirmPassword)
        if (validationMessage == null) {
            userName
                ?.let {
                    ChangePasswordRequest(edConfirmPassword, edPassword, edNewPassword, it)
                }
                ?.let {
                    viewModel.changePassword(this@FIrstTimePasswordChangeActivity, it)
                }

        } else {
            Toasty.error(
                this@FIrstTimePasswordChangeActivity,
                validationMessage
            ).show()
        }
    }

    private fun validateInput(newPass: String, confirmPass: String): String? {
        return when {
            !isValidInput(newPass) -> "Invalid password format!!."
            newPass.isEmpty() -> "Please enter the new password"
            confirmPass.isEmpty() -> "Please confirm the new password"
            newPass.length < 6 -> "New password should have at least 6 characters"
            newPass != confirmPass -> "New password and confirm password do not match"
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