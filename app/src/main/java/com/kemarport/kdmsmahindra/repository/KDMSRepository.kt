package com.kemarport.kdmsmahindra.repository

import android.app.Activity
import com.kemarport.kdmsmahindra.model.changepassword.ChangePasswordRequest
import com.kemarport.kdmsmahindra.model.forgotpassword.ForgotPasswordRequest
import com.kemarport.kdmsmahindra.model.forgotpassword.ResetPasswordRequest
import com.kemarport.kdmsmahindra.model.login.LoginRequest
import com.kemarport.kdmsmahindra.model.newapi.ConfirmDealerVehicleDeliveryRequest
import com.kemarport.kdmsmahindra.model.newapi.VerifyDealerVehicleRequest
import com.kemarport.kdmsmahindra.model.setgeofence.PostGeofenceCoordinatesRequest
import com.kemarport.mahindrakiosk.api.RetrofitInstance
import retrofit2.http.Body

import retrofit2.http.Header

class KDMSRepository {
    suspend fun login(
        context: Activity,
        loginRequest: LoginRequest,
    ) = RetrofitInstance(context = context).api().login(loginRequest)

    suspend fun getAppDetails(
        context: Activity,
    ) = RetrofitInstance(context = context).api().getAppDetails()

    //////
    suspend fun getDealerVehicleConfirmationCount(
        context: Activity,
        period: Int,
        dealerCode: String,
    ) = RetrofitInstance(context = context).api()
        .getDealerVehicleConfirmationCount(period, dealerCode)

    suspend fun getDealerVehicleConfirmation(
        context: Activity,
        period: Int,
        dealerCode: String,
    ) = RetrofitInstance(context = context).api().getDealerVehicleConfirmation(period, dealerCode)

    suspend fun getScanRFIDCount(
        context: Activity,
        dealerCode: String,
    ) = RetrofitInstance(context = context).api().getDealerVehicleScanRFIDCount(dealerCode)


    //new api

    suspend fun postVerifyDealerVehicle(
        context: Activity,
        verifyDealerVehicleRequest: VerifyDealerVehicleRequest,
    ) = RetrofitInstance(context = context).api()
        .postVerifyDealerVehicle(verifyDealerVehicleRequest)

    suspend fun postConfirmDealerVehicleDelivery(
        context: Activity,
        confirmDealerVehicleDeliveryRequest: ConfirmDealerVehicleDeliveryRequest,
    ) = RetrofitInstance(context = context).api()
        .postConfirmDealerVehicleDelivery(confirmDealerVehicleDeliveryRequest)

    suspend fun getDeliveredCount(
        context: Activity,
    ) = RetrofitInstance(context = context).api().getDeliveredCount()

    suspend fun getDeliveredDetails(
        context: Activity,
    ) = RetrofitInstance(context = context).api().getDeliveredDetails()

    suspend fun changePassword(
        context: Activity,
        changePasswordRequest: ChangePasswordRequest,
    ) = RetrofitInstance(context = context).api().changePassword(changePasswordRequest)

    suspend fun forgotPassword(
        context: Activity,
        forgotPasswordRequest: ForgotPasswordRequest,
    ) = RetrofitInstance(context = context).api().forgotPassword(forgotPasswordRequest)

    suspend fun resetPassword(
        context: Activity,
        resetPasswordRequest: ResetPasswordRequest,
    ) = RetrofitInstance(context = context).api().resetPassword(resetPasswordRequest)


    suspend fun getGeofenceByDealer(
        context: Activity,
        @Header("dealerCode") dealerCode: String,
    ) = RetrofitInstance(context = context).api().getGeofenceByDealer(dealerCode)

    suspend fun postGeofenceCoordinates(
        context: Activity,
        @Body
        postGeofenceCoordinatesRequest: PostGeofenceCoordinatesRequest,
    ) = RetrofitInstance(context = context).api()
        .postGeofenceCoordinates(postGeofenceCoordinatesRequest)


}