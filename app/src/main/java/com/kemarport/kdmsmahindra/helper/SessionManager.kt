package com.kemarport.kdmsmahindra.helper

import android.content.Context
import android.content.SharedPreferences
import com.kemarport.mahindrakiosk.helper.Constants

class SessionManager(context: Context) {
    // Shared Preferences
    var sharedPrefer: SharedPreferences

    // Editor for Shared preferences
    var editor: SharedPreferences.Editor
    // Context
    var context: Context
    // Shared Pref mode
    var PRIVATE_MODE = 0
    // Constructor
    init {
        this.context = context
        sharedPrefer = context.getSharedPreferences(Constants.SHARED_PREF, PRIVATE_MODE)
        editor = sharedPrefer.edit()
    }

    /**
     * Call this method on/after login to store the details in session
     */
    fun createLoginSession(
        firstName: String?,
        lastName: String?,
        email: String?,
        mobileNumber: String?,
        isVerified: String?,
        userName: String?,
        jwtToken: String?,
        refreshToken: String?,
        roleName:String?,
        dealerCode:String?,
        coordinates:String?,
        locationId:String?
    ) {

        //editor.putString(KEY_USERID, userId)
        editor.putString(Constants.KEY_USER_NAME, userName)
        editor.putString(Constants.KEY_USER_FIRST_NAME, firstName)
        editor.putString(Constants.KEY_USER_LAST_NAME, lastName)
        editor.putString(Constants.KEY_USER_EMAIL, email)
        editor.putString(Constants.KEY_USER_MOBILE_NUMBER, mobileNumber)
        editor.putString(Constants.KEY_USER_IS_VERIFIED, isVerified)
        editor.putString(Constants.COORDINATES, coordinates)
        editor.putString(Constants.DEALER_CODE,dealerCode)
        editor.putString(Constants.LOCATION_ID,locationId)


        //editor.putString(KEY_RDT_ID, rdtId)
        //editor.putString(KEY_TERMINAL, terminal)
        //editor.putBoolean(KEY_ISLOGGEDIN, true)
        editor.putString(Constants.KEY_JWT_TOKEN, jwtToken)
        editor.putString(Constants.KEY_REFRESH_TOKEN, refreshToken)
        editor.putString(Constants.ROLE_NAME, roleName)

        // commit changes
        editor.commit()
    }

    fun logoutUser() {
        editor.putBoolean(Constants.KEY_ISLOGGEDIN, false)
        editor.commit()
    }

    /**
     * Call this method anywhere in the project to Get the stored session data
     */
    /*fun getUserDetails(): HashMap<String, String?> {
        val user = HashMap<String, String?>()
        user["userId"] = sharedPrefer.getString(KEY_USER_ID, null)
        user["userName"] = sharedPrefer.getString(KEY_USER_NAME, null)
        user["rdtId"] = sharedPrefer.getString(KEY_RDT_ID, null)
        user["terminal"] = sharedPrefer.getString(KEY_TERMINAL, null)
        user["jwtToken"] = sharedPrefer.getString(KEY_JWT_TOKEN, null)
        user["refreshToken"] = sharedPrefer.getString(KEY_REFRESH_TOKEN, null)
        return user
    }*/
    fun getUserDetails(): HashMap<String, String?> {
        val user = HashMap<String, String?>()
        user[Constants.KEY_USER_NAME] = sharedPrefer.getString(Constants.KEY_USER_NAME, null)
        user[Constants.DEALER_CODE] = sharedPrefer.getString(Constants.DEALER_CODE, null)
        user[Constants.KEY_JWT_TOKEN] = sharedPrefer.getString(Constants.KEY_JWT_TOKEN, null)
        user[Constants.ROLE_NAME] = sharedPrefer.getString(Constants.ROLE_NAME, null)
        user[Constants.LOCATION_ID] = sharedPrefer.getString(Constants.LOCATION_ID, null)

        return user
    }

  /*  fun getHeaderDetails(): HashMap<String, String?> {
        val user_header = HashMap<String, String?>()
        user_header["UserId"] = sharedPrefer.getString(KEY_USERID, null)
        user_header["RDTId"] = sharedPrefer.getString(KEY_RDT_ID, null)
        user_header["TerminalId"] = sharedPrefer.getString(KEY_TERMINAL, null)
        user_header["Token"] = sharedPrefer.getString(KEY_JWT_TOKEN, null)
        return user_header
    }
*/
    fun isAlreadyLoggedIn(): HashMap<String, Boolean> {
        val user = HashMap<String, Boolean>()
        user["isLoggedIn"] = sharedPrefer.getBoolean(Constants.KEY_ISLOGGEDIN, false)
        return user
    }

    fun getAdminDetails(): HashMap<String, String?> {
        val admin = HashMap<String, String?>()
        admin["serverIp"] = sharedPrefer.getString(KEY_SERVER_IP, null)
        admin["port"] = sharedPrefer.getString(KEY_PORT, null)
        return admin
    }

    fun getToken(): String{
        val token = sharedPrefer.getString(Constants.KEY_JWT_TOKEN, null)
        return token?:""
    }

    fun getRole(): String{
        val role = sharedPrefer.getString(Constants.ROLE_NAME, null)
        return role?:""
    }

    fun saveAdminDetails(serverIp: String?, portNumber: String?) {
        editor.putString(KEY_SERVER_IP, serverIp)
        editor.putString(KEY_PORT, portNumber)
        editor.putBoolean(Constants.KEY_ISLOGGEDIN, false)
        editor.commit()
    }

    fun clearSharedPrefs() {
        editor.clear()
        editor.commit()
    }

    companion object {
        private const val PREF_NAME = "shared_pref"
        //const val KEY_USERID = Constants.USER_ID








       // const val KEY_RDT_ID = Constants.RDT_ID
        //const val KEY_TERMINAL = Constants.TERMINAL_ID


        //Admin Shared Prefs
        const val KEY_SERVER_IP = "serverIp"
        const val KEY_PORT = "port"
    }

}


