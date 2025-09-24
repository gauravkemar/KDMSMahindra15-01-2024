package com.kemarport.kdmsmahindra.helper

import android.content.Context
import android.content.Intent
import com.kemarport.kdmsmahindra.api.RefreshTokenApi
import com.kemarport.kdmsmahindra.helper.refreshtoken.RefreshTokenRequest
import com.kemarport.kdmsmahindra.helper.refreshtoken.RefreshTokenResponse
import com.kemarport.kdmsmahindra.view.LoginActivity

import com.kemarport.mahindrakiosk.helper.Constants
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object TokenManager {
    private val mutex = Mutex()
    private var refreshDeferred: CompletableDeferred<Boolean>? = null

    suspend fun refreshTokenIfNeeded(context: Context, oldToken: String?): String? {
        val prefs = context.getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE)

        return mutex.withLock {
            val currentToken = prefs.getString(Constants.KEY_JWT_TOKEN, null)
            if (currentToken != oldToken) {
                return currentToken // Already refreshed
            }

            // If another refresh is already happening, wait for it
            refreshDeferred?.let {
                val success = it.await()
                return if (success) prefs.getString(Constants.KEY_JWT_TOKEN, null) else null
            }

            // This thread will perform the refresh
            refreshDeferred = CompletableDeferred<Boolean>()

            val refreshToken = prefs.getString(Constants.KEY_REFRESH_TOKEN, null)
            val serverIp = prefs.getString(Constants.KEY_SERVER_IP, null)
            val serverHttp = prefs.getString(Constants.KEY_HTTP, null)
            val baseUrl = "$serverHttp://$serverIp/api/"

            val newToken = refreshAccessToken(refreshToken, baseUrl)

            return if (newToken != null) {
                prefs.edit().putString(Constants.KEY_JWT_TOKEN, newToken.jwtToken).apply()
                prefs.edit().putString(Constants.KEY_REFRESH_TOKEN, newToken.refreshToken).apply()
                refreshDeferred?.complete(true)
                newToken.jwtToken
            } else {
                AuthState.isUnauthorized = true

                prefs.edit().putBoolean(Constants.KEY_ISLOGGEDIN, false).apply()
                redirectToLogin(context)
                refreshDeferred?.complete(false)
                null
            }.also {
                refreshDeferred = null
            }
        }
    }

    private fun refreshAccessToken(refreshToken: String?, baseUrl: String): RefreshTokenResponse? {
        if (refreshToken.isNullOrEmpty()) return null

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder().addInterceptor(logging).build()

        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()

        val refreshTokenApi = retrofit.create(RefreshTokenApi::class.java)

        return try {
            val response = refreshTokenApi.refreshToken(RefreshTokenRequest(refreshToken)).execute()
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) {
            null
        }
    }
    private fun redirectToLogin(context: Context) {
        val intent = Intent(context, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        context.startActivity(intent)
    }
}
