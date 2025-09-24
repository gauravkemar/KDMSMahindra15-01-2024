package com.kemarport.kdmsmahindra.view

import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import com.kemarport.kdmsmahindra.R
import com.kemarport.kdmsmahindra.databinding.ActivityAdminSettingsBinding
import com.kemarport.kdmsmahindra.helper.SessionManager
import com.kemarport.mahindrakiosk.helper.Constants
import com.kemarport.mahindrakiosk.helper.Utils
import es.dmoral.toasty.Toasty


class AdminSettingsActivity : AppCompatActivity() {
    lateinit var binding: ActivityAdminSettingsBinding
    private var builder: AlertDialog.Builder? = null
    private var alert: AlertDialog? = null
    private var serverIpSharedPrefText: String? = null
    private var serverHttpPrefText: String? = null
    //private var portSharedPrefText: Int? = 0
    private lateinit var session: SessionManager
    private lateinit var user: HashMap<String, String?>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        binding = DataBindingUtil.setContentView(this, R.layout.activity_admin_settings)
        session = SessionManager(this)
        user = session.getUserDetails()
        serverIpSharedPrefText = user["serverIp"].toString()
        serverHttpPrefText = user["http"].toString()
        binding.edServerIp.setText(serverIpSharedPrefText)
        if (serverHttpPrefText.toString() == "null") {
            binding.edHttp.setText("")
        } else {
            binding.edHttp.setText(serverHttpPrefText.toString())
        }
        binding.btnSave.setOnClickListener {
            val serverIp = binding.edServerIp.text.toString().trim()
            var edHttp = binding.edHttp.text.toString().trim()
            if (serverIp == "" || edHttp == "") {
                if (serverIp == "" && edHttp == "") {
                    Toast.makeText(
                        this,
                        "Please enter all values!!",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.edServerIp.error = "Please enter Domain Name"
                    //binding.edPort.error = "Please enter value"
                    binding.edHttp.error = "Please enter value"
                } else if (serverIp == "") {
                    Toast.makeText(this, "Please Enter ServerIP!!", Toast.LENGTH_SHORT)
                        .show()
                    binding.edServerIp.error = "Please enter Domain Name"
                } else if (edHttp == "") {
                    Toast.makeText(
                        this,
                        "Please Enter Http/Https Number!!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                showDialog(serverIp, edHttp)

                //if (serverIp != serverIpSharedPref || portNumber != portSharedPrefText) {
                //}

            }

        }

        binding.edAntennaPower.setText(Utils.getSharedPrefs(this@AdminSettingsActivity, Constants.SET_ANTENNA_POWER))
        binding.setAntennaPower.setOnClickListener {
            checkinputAntennaPower()
        }
    }
    private fun checkinputAntennaPower() {
        val url: String = binding.edAntennaPower.getText().toString().trim()
        if (url.isEmpty() || url.equals("")) {
            binding.edAntennaPower.setError("Please enter ip address")
        } else {
            Utils.setSharedPrefs(this@AdminSettingsActivity, Constants.SET_ANTENNA_POWER, url )
            Toasty.success(this@AdminSettingsActivity,"Antenna Power Updated.",Toasty.LENGTH_SHORT)
                .show()
        }
    }
    private fun showDialog(
        serverIp: String?,
        http: String?,
    ) {
        builder = AlertDialog.Builder(this)
        builder!!.setMessage("Changes will take effect after Re-Login!")
            .setCancelable(false)
            .setPositiveButton("Yes") { dialog: DialogInterface?, id: Int ->
                session.saveAdminDetails(serverIp, http)
                startActivity(Intent(this, LoginActivity::class.java))
                this@AdminSettingsActivity?.finishAffinity()
            }
            .setNegativeButton("No, Continue") { dialog: DialogInterface, id: Int ->
                dialog.cancel()
                binding.edServerIp.setText(serverIpSharedPrefText)
            }
        alert = builder!!.create()
        alert!!.show()
    }
}