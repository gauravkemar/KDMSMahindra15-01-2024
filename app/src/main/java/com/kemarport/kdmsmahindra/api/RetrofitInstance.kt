package com.kemarport.mahindrakiosk.api

import android.content.Context
import com.kemarport.kdmsmahindra.helper.TokenInterceptor
import com.kemarport.mahindrakiosk.helper.Constants
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class RetrofitInstance internal constructor(private val context: Context) {

    companion object {
        @Volatile
        private var INSTANCE: RetrofitInstance? = null

        fun getInstance(context: Context): RetrofitInstance {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: RetrofitInstance(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    private val retrofit: Retrofit by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val tokenInterceptor by lazy {
            TokenInterceptor(context.applicationContext)
        }

        val client = OkHttpClient.Builder()
            .protocols(listOf(Protocol.HTTP_1_1))
            .addInterceptor(logging)
            .addInterceptor(tokenInterceptor)
            .connectTimeout(100, TimeUnit.SECONDS)
            .readTimeout(100, TimeUnit.SECONDS)
            .writeTimeout(100, TimeUnit.SECONDS)
            .build()

        // Load base URL from SharedPreferences
        val prefs = context.getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE)
        val serverIp = prefs.getString(Constants.KEY_SERVER_IP, null)
        val serverHttp = prefs.getString(Constants.KEY_HTTP, null)
        val baseUrl = "$serverHttp://$serverIp/api/"

        Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }

    fun api(): KDMSAPI = retrofit.create(KDMSAPI::class.java)
}


/*

class RetrofitInstance {

  */
/*  companion object {
        private var baseUrl = ""
        private val retrofit by lazy {
            val logging = HttpLoggingInterceptor()
            logging.setLevel(HttpLoggingInterceptor.Level.BODY)
            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(100, TimeUnit.SECONDS)
                .readTimeout(100, TimeUnit.SECONDS)
                .writeTimeout(100, TimeUnit.SECONDS)
                .build()
            Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
        }

        fun api(baseUrl: String): KDMSAPI {
            this.baseUrl = baseUrl
            return retrofit.create(KDMSAPI::class.java)
        }
    }*//*

  companion object {

      fun create(baseUrl: String): Retrofit {
          val logging = HttpLoggingInterceptor()
          logging.setLevel(HttpLoggingInterceptor.Level.BODY)
          val client = OkHttpClient.Builder()
              .addInterceptor(logging)
              .connectTimeout(100, TimeUnit.SECONDS)
              .readTimeout(100, TimeUnit.SECONDS)
              .writeTimeout(100, TimeUnit.SECONDS)
              .build()

          return Retrofit.Builder()
              .baseUrl(baseUrl)
              .addConverterFactory(GsonConverterFactory.create())
              .client(client)
              .build()
      }

      fun api(baseUrl: String): KDMSAPI {
          val retrofit = create(baseUrl+"/api/")
          return retrofit.create(KDMSAPI::class.java)
      }
  }
}*/
