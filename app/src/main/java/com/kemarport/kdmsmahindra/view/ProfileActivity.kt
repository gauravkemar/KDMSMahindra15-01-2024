package com.kemarport.kdmsmahindra.view

import android.content.Intent
import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import com.kemarport.kdmsmahindra.R
import com.kemarport.kdmsmahindra.databinding.ActivityProfileBinding
import com.kemarport.kdmsmahindra.helper.SessionManager
import com.kemarport.mahindrakiosk.helper.Constants
import com.kemarport.mahindrakiosk.helper.Utils

class ProfileActivity : AppCompatActivity() {
    lateinit var binding:ActivityProfileBinding
    private lateinit var session: SessionManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding=DataBindingUtil.setContentView(this,R.layout.activity_profile)
        session = SessionManager(this)
        setSupportActionBar(binding.profileToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        setTextViewValue(binding.tvFirstNameValue, Constants.KEY_USER_FIRST_NAME)
        setTextViewValue(binding.tvLastNameValue, Constants.KEY_USER_LAST_NAME)
        setTextViewValue(binding.tvDealerCodeValue, Constants.DEALER_CODE)
        setTextViewValue(binding.tvDealerNameValue, Constants.KEY_DEALER_NAME)
        setTextViewValue(binding.tvEmailValue, Constants.KEY_USER_EMAIL)

        binding.mcvProfile6.setOnClickListener {
            showLogoutDialog()
        }
    }
    fun setTextViewValue(textView: TextView, key: String) {
        textView.text = Utils.getSharedPrefs(this@ProfileActivity, key).takeIf { !it.isNullOrEmpty() } ?: "N/A"
    }
    private fun showLogoutDialog() {
        val builder = AlertDialog.Builder(this)

        builder.setTitle("Logout")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Yes") { dialog, which ->
                logout()
            }
            .setNegativeButton("Cancel") { dialog, which ->

                dialog.dismiss()
            }
            .setCancelable(false)

        val dialog = builder.create()
        dialog.show()
    }
    private fun logout(){
        session.logoutUser()
        startActivity(Intent(this@ProfileActivity, LoginActivity::class.java))

        finish()
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