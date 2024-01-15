package com.kemarport.mahindrakiosk.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class RetrofitInstance {

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
    }*/
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
          val retrofit = create(baseUrl)
          return retrofit.create(KDMSAPI::class.java)
      }
  }
}