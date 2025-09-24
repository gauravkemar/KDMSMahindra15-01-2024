package com.kemarport.kdmsmahindra.api


import com.kemarport.kdmsmahindra.helper.refreshtoken.RefreshTokenRequest
import com.kemarport.kdmsmahindra.helper.refreshtoken.RefreshTokenResponse
import com.kemarport.mahindrakiosk.helper.Constants
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface RefreshTokenApi {
    @POST(Constants.getRefreshToken)
    fun refreshToken(@Body request: RefreshTokenRequest): Call<RefreshTokenResponse>
}