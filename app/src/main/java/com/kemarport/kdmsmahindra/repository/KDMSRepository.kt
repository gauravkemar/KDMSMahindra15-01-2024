package com.kemarport.kdmsmahindra.repository

import com.kemarport.kdmsmahindra.model.login.LoginRequest
import com.kemarport.kdmsmahindra.model.newapi.ConfirmDealerVehicleDeliveryRequest
import com.kemarport.kdmsmahindra.model.newapi.VerifyDealerVehicleRequest
import com.kemarport.mahindrakiosk.api.RetrofitInstance
import retrofit2.http.Header

class KDMSRepository {
    suspend fun login(
        baseUrl: String,
        loginRequest: LoginRequest
    ) = RetrofitInstance.api(baseUrl).login(loginRequest)

    //////
    suspend fun getDealerVehicleConfirmationCount(
        token:String,
        baseUrl: String,
        period: Int,
        dealerCode: String
    ) = RetrofitInstance.api(baseUrl).getDealerVehicleConfirmationCount(token,period, dealerCode)

    suspend fun getDealerVehicleConfirmation(
        token:String,
        baseUrl: String,
        period: Int,
        dealerCode: String
    ) = RetrofitInstance.api(baseUrl).getDealerVehicleConfirmation(token,period, dealerCode)

    suspend fun getScanRFIDCount(
        token:String,
        baseUrl: String,
        dealerCode: String
    ) = RetrofitInstance.api(baseUrl).getDealerVehicleScanRFIDCount(token,dealerCode)


    //new api

    suspend fun postVerifyDealerVehicle(
        @Header("Authorization") bearerToken: String,
        baseUrl: String,
        verifyDealerVehicleRequest: VerifyDealerVehicleRequest
    ) = RetrofitInstance.api(baseUrl).postVerifyDealerVehicle(bearerToken,verifyDealerVehicleRequest)

    suspend fun postConfirmDealerVehicleDelivery(
        @Header("Authorization") bearerToken: String,
        baseUrl: String,
        confirmDealerVehicleDeliveryRequest: ConfirmDealerVehicleDeliveryRequest
    ) = RetrofitInstance.api(baseUrl).postConfirmDealerVehicleDelivery(bearerToken,confirmDealerVehicleDeliveryRequest)
    suspend fun getDeliveredCount(
        @Header("Authorization") bearerToken: String,
        baseUrl: String,
    ) = RetrofitInstance.api(baseUrl).getDeliveredCount(bearerToken)

    suspend fun getDeliveredDetails(
        @Header("Authorization") bearerToken: String,
        baseUrl: String,
    ) = RetrofitInstance.api(baseUrl).getDeliveredDetails(bearerToken)

}