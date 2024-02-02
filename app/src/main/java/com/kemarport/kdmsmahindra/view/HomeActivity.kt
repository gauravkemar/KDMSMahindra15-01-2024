package com.kemarport.kdmsmahindra.view

import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.kemarport.kdmsmahindra.R
import com.kemarport.kdmsmahindra.databinding.ActivityHomeBinding
import com.kemarport.kdmsmahindra.helper.SessionManager

class HomeActivity : AppCompatActivity(){
lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
lateinit var binding:ActivityHomeBinding
    private lateinit var session: SessionManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=DataBindingUtil.setContentView(this,R.layout.activity_home)
        session = SessionManager(this)
        setSupportActionBar(binding.homeToolbar)
        actionBarDrawerToggle = ActionBarDrawerToggle(this, binding.myDrawerLayout, R.string.nav_open, R.string.nav_close)
        binding.myDrawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.isDrawerIndicatorEnabled = true
        actionBarDrawerToggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val drawable = ContextCompat.getDrawable(this, R.drawable.ic_hamburger_white)
        drawable?.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
        binding.homeToolbar.navigationIcon = drawable
        if (savedInstanceState == null) {
            val defaultFragment = DashboardNewFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, defaultFragment)
                .commit()
        }
        binding.homeNavDrawer.setNavigationItemSelectedListener { menuItem ->
            // Handle item selection here
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    val fragment = DashboardNewFragment()
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .commit()
                }
                R.id.vin_confirmation -> {
                    startActivity(Intent(this@HomeActivity, MainActivity::class.java))
                }
                R.id.change_password -> {
                    startActivity(Intent(this@HomeActivity, ChangePasswordActivity::class.java))
                }
                R.id.logout -> {
                    showLogoutDialog()
                }
                // Add more cases for other menu items if needed
            }

            // Close the drawer after item selection
            binding.myDrawerLayout.closeDrawers()
            true
        }
        binding.vinConfirmationBtn.setOnClickListener {
            startVinActivity()
        }
    }
    private fun startVinActivity(){
        startActivity(Intent(this@HomeActivity,MainActivity::class.java))
    }
    private fun logout(){
        session.logoutUser()
        startActivity(Intent(this@HomeActivity, LoginActivity::class.java))
        finish()
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            true
        } else super.onOptionsItemSelected(item)
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
}