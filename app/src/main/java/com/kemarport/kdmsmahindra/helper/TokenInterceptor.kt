package com.kemarport.kdmsmahindra.helper

import android.content.Context
import android.content.Intent
import com.kemarport.mahindrakiosk.helper.Constants
import com.kemarport.mahindrakiosk.helper.Constants.LOGIN_URL
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import kotlin.math.pow

object AuthState {
    @Volatile var isUnauthorized = false
}

class TokenInterceptor(
    private val context: Context
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val prefs = context.getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE)
        var accessToken = prefs.getString(Constants.KEY_JWT_TOKEN, null)

        val originalRequest = chain.request()
        val isLoginEndpoint = originalRequest.url.toString()
            .contains(LOGIN_URL, ignoreCase = true)

        val requestWithToken = if (isLoginEndpoint) {
            originalRequest
        } else {
            originalRequest.newBuilder()
                .header("Authorization", accessToken ?: "")
                .build()
        }

        var response = chain.proceed(requestWithToken)

        if (isLoginEndpoint || response.code != 401) {
            return response
        }

        response.close()

        runBlocking {
            val newToken = TokenManager.refreshTokenIfNeeded(context, accessToken)
            if (newToken != null) {
                accessToken = newToken
            } else {
                AuthState.isUnauthorized = true
                return@runBlocking
            }
        }

        val newRequest = originalRequest.newBuilder()
            .removeHeader("Authorization")
            .addHeader("Authorization", accessToken ?: "")
            .build()

        return chain.proceed(newRequest)
    }

}





/*
object AuthState {
    @Volatile var isUnauthorized = false
}

class TokenInterceptor(
    private val context: Context
) : Interceptor {

    private val lock = Object()
    @Volatile private var isRefreshing = false

    var PRIVATE_MODE = 0

    override fun intercept(chain: Interceptor.Chain): Response {
        val prefs = context.applicationContext.getSharedPreferences(Constants.SHARED_PREF, PRIVATE_MODE)
        var accessToken = prefs.getString(Constants.KEY_JWT_TOKEN, null)
        val refreshToken = prefs.getString(Constants.KEY_REFRESH_TOKEN, null)

        val serverIp = prefs.getString(Constants.KEY_SERVER_IP, null)
        val serverHttp = prefs.getString(Constants.KEY_HTTP, null)
        val baseUrl = "$serverHttp://$serverIp/service/api/"

        val originalRequest = chain.request()
        val requestUrl = originalRequest.url.toString()

        // Skip adding Authorization for login
        val isLoginEndpoint = requestUrl.contains("UserManagement/authenticate", ignoreCase = true)

        val requestWithToken = if (isLoginEndpoint) {
            originalRequest
        } else {
            originalRequest.newBuilder()
                .header("Authorization", accessToken ?: "")
                .build()
        }

        var response = chain.proceed(requestWithToken)

        // If login call or successful call, just return
        if (isLoginEndpoint || response.code != 401) {
            return response
        }

        // Handle 401 - refresh token
        response.close()

        synchronized(lock) {
            if (!isRefreshing) {
                isRefreshing = true
                try {
                    val newToken = refreshAccessToken(refreshToken, baseUrl)
                    if (newToken != null) {
                        prefs.edit().putString(Constants.KEY_JWT_TOKEN, newToken.jwtToken).apply()
                        prefs.edit().putString(Constants.KEY_REFRESH_TOKEN, newToken.refreshToken).apply()
                        accessToken = newToken.jwtToken
                    } else {
                        AuthState.isUnauthorized = true
                        redirectToLogin(context.applicationContext)

                        return response.newBuilder()
                            .code(401)
                            .message("Unauthorized - Token Refresh Failed")
                            .body("".toResponseBody(null))
                            .build()
                    }
                } finally {
                    isRefreshing = false
                    lock.notifyAll() // ✅ Always notify waiting threads
                }
            } else {
                try {
                    lock.wait() // ✅ Proper wait until refresh is done
                    accessToken = prefs.getString(Constants.KEY_JWT_TOKEN, null)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                    return response.newBuilder()
                        .code(401)
                        .message("Interrupted while waiting for token refresh")
                        .body("".toResponseBody(null))
                        .build()
                }
            }
        }

        val newRequest = originalRequest.newBuilder()
            .removeHeader("Authorization")
            .addHeader("Authorization", accessToken ?: "")
            .build()

        return chain.proceed(newRequest)
    }

    private fun refreshAccessToken(refreshToken: String?, baseUrl: String): RefreshTokenResponse? {
        if (refreshToken.isNullOrEmpty()) return null

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()

        val refreshTokenApi = retrofit.create(RefreshTokenApi::class.java)
        return try {
            val response = refreshTokenApi.refreshToken(RefreshTokenRequest(refreshToken)).execute()
            if (response.isSuccessful) {
                response.body()
            } else null
        } catch (e: Exception) {
            null
        }
    }
    fun redirectToLogin(context: Context) {

        val prefs = context.getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(Constants.KEY_ISLOGGEDIN, false).apply()

        val intent = Intent(context, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        context.startActivity(intent)
    }
}
*/


/*
package com.example.slfastener.helper

import android.app.Activity
import android.content.Intent
import android.util.Log
import com.example.demorfidapp.helper.Constants
import com.example.slfastener.api.RefreshTokenApi
import com.example.slfastener.helper.refreshtoken.RefreshTokenRequest
import com.example.slfastener.helper.refreshtoken.RefreshTokenResponse
import com.example.slfastener.view.login.LoginActivity
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object AuthState {
    @Volatile
    var isUnauthorized = false
}

class TokenInterceptor(
    private val context: Activity,
) : Interceptor {

    var PRIVATE_MODE = 0
    override fun intercept(chain: Interceptor.Chain): Response {
        var baseUrl: String = ""
        var serverIpSharedPrefText: String? = null
        var serverHttpPrefText: String? = null

        val sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREF, PRIVATE_MODE)
        var accessToken = sharedPreferences.getString(Constants.KEY_JWT_TOKEN, null)
        var refreshToken = sharedPreferences.getString(Constants.KEY_REFRESH_TOKEN, null)

        Log.e("currentJwtToken", accessToken.toString())
        Log.e("currentRefreshToken", refreshToken.toString())

        serverIpSharedPrefText = sharedPreferences.getString(Constants.KEY_SERVER_IP, null)
        serverHttpPrefText = sharedPreferences.getString(Constants.KEY_HTTP, null)
        baseUrl = "$serverHttpPrefText://$serverIpSharedPrefText/service/api/"

        // Add the Access Token to the request
        val originalRequest = chain.request()
        val requestBuilder = originalRequest.newBuilder()

        if (!accessToken.isNullOrEmpty()) {
            requestBuilder.addHeader("Authorization", "$accessToken")
        }

        val request = requestBuilder.build()
        val response = chain.proceed(request)

        Log.e("ResponseFromToken", "response.toString()")

        // If response is 401 (Unauthorized), refresh the token
        if (response.code == 401) {
            //response.close()
            synchronized(this) {
                val newAccessToken = refreshAccessToken(refreshToken, baseUrl)

                if (newAccessToken != null) {
                    // Save new access token
                    sharedPreferences.edit()
                        .putString(Constants.KEY_JWT_TOKEN, newAccessToken.jwtToken).apply()
                    sharedPreferences.edit()
                        .putString(Constants.KEY_REFRESH_TOKEN, newAccessToken.refreshToken).apply()
                    accessToken = newAccessToken.jwtToken
                    refreshToken = newAccessToken.refreshToken
                    response.close()
                    val newRequest = originalRequest.newBuilder()
                        .removeHeader("Authorization")
                        .addHeader("Authorization", "${newAccessToken.jwtToken}")
                        .build()
                    //Log.e("ResponseFromNewToken",response.toString())
                    return chain.proceed(newRequest)
                } else {
                    Log.e("ResponseFromNewToken", "NavigateCalled")
                    AuthState.isUnauthorized = true // <-- Add this line

                }
            }
        }

        return response
    }

    private fun refreshAccessToken(refreshToken: String?, baseUrl: String): RefreshTokenResponse? {
        if (refreshToken.isNullOrEmpty()) return null

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .protocols(listOf(Protocol.HTTP_1_1))
            .addInterceptor(logging)
            .connectTimeout(100, TimeUnit.SECONDS)
            .readTimeout(100, TimeUnit.SECONDS)
            .writeTimeout(100, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()

        val refreshTokenApi = retrofit.create(RefreshTokenApi::class.java)
        val response = refreshTokenApi.refreshToken(RefreshTokenRequest(refreshToken)).execute()

        return if (response.isSuccessful) {
            val body = response.body()
            // Return the response body which contains both the accessToken and refreshToken
            RefreshTokenResponse(
                jwtToken = body?.jwtToken ?: "",
                refreshToken = body?.refreshToken ?: ""
            )
        } else {
            null
        }
    }

    */
/*  private fun refreshAccessToken(refreshToken: String?, baseUrl: String): String? {
          if (refreshToken.isNullOrEmpty()) return null
          val logging = HttpLoggingInterceptor().apply {
              level = HttpLoggingInterceptor.Level.BODY
          }
          val client = OkHttpClient.Builder()
              .protocols(listOf(Protocol.HTTP_1_1))
              .addInterceptor(logging)
              .connectTimeout(100, TimeUnit.SECONDS)
              .readTimeout(100, TimeUnit.SECONDS)
              .writeTimeout(100, TimeUnit.SECONDS)
              .build()
          val retrofit = Retrofit.Builder()
              .baseUrl(baseUrl)
              .addConverterFactory(GsonConverterFactory.create())
              .client(client)
              .build()

          val refreshTokenApi = retrofit.create(RefreshTokenApi::class.java)
          val response = refreshTokenApi.refreshToken(RefreshTokenRequest(refreshToken)).execute()

          return if (response.isSuccessful) {
              response.body()?.jwtToken
          } else {
              null
          }
      }
  *//*


    private fun navigateToLogin(context: Activity) {

        val sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREF, PRIVATE_MODE)
        sharedPreferences.edit().clear().apply()

        val intent = Intent(context, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        context.startActivity(intent)

    }
}*/
