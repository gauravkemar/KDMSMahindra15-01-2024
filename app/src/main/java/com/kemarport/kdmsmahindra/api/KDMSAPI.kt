package com.kemarport.mahindrakiosk.api


import com.kemarport.kdmsmahindra.model.*
import com.kemarport.kdmsmahindra.model.changepassword.ChangePasswordRequest
import com.kemarport.kdmsmahindra.model.changepassword.ChangePasswordResponse
import com.kemarport.kdmsmahindra.model.dashboard.RFIDCountResponse
import com.kemarport.kdmsmahindra.model.dashboard.VehicleConfirmationCountResponseItem
import com.kemarport.kdmsmahindra.model.dashboard.VehicleConfirmationResponseItem
import com.kemarport.kdmsmahindra.model.forgotpassword.ForgotPasswordRequest
import com.kemarport.kdmsmahindra.model.forgotpassword.ForgotPasswordResponse
import com.kemarport.kdmsmahindra.model.forgotpassword.ResetPasswordRequest
import com.kemarport.kdmsmahindra.model.login.LoginRequest
import com.kemarport.kdmsmahindra.model.login.LoginResponse
import com.kemarport.kdmsmahindra.model.newapi.ConfirmDealerVehicleDeliveryRequest
import com.kemarport.kdmsmahindra.model.newapi.ConfirmDealerVehicleDeliveryResponse
import com.kemarport.kdmsmahindra.model.newapi.DashboardGetDeliveredDetailsResponse
import com.kemarport.kdmsmahindra.model.newapi.GetDeliveredCountResponse
import com.kemarport.kdmsmahindra.model.newapi.VerifyDealerVehicleRequest
import com.kemarport.kdmsmahindra.model.newapi.VerifyDealerVehicleResponse
import com.kemarport.mahindrakiosk.helper.Constants
import com.kemarport.mahindrakiosk.helper.GeneralResponse

import retrofit2.Response
import retrofit2.http.*


interface KDMSAPI {

/*
 @POST(Constants.LOGIN_URL)
 suspend fun generalDemo(
  @Body
  generalRequest: GeneralRequest
 ): Response<GeneralResponse>

*/

 @POST(Constants.LOGIN_URL)
 suspend fun login(
  @Body
  loginRequest: LoginRequest
 ): Response<LoginResponse>







 //Merging from Vivek
 @GET(Constants.VEHICLE_CONFIRMATION_COUNT)
 suspend fun getDealerVehicleConfirmationCount(
  @Header("Authorization") bearerToken: String,
  @Query("Period") period: Int,
  @Query("DealerCode") dealerCode: String
 ): Response<List<VehicleConfirmationCountResponseItem>>

 @GET(Constants.VEHICLE_CONFIRMATION_DASH)
 suspend fun getDealerVehicleConfirmation(
  @Header("Authorization") bearerToken: String,
  @Query("Period") period: Int,
  @Query("DealerCode") dealerCode: String
 ): Response<List<VehicleConfirmationResponseItem>>

 @GET(Constants.VEHICLE_RFID_COUNT)
 suspend fun getDealerVehicleScanRFIDCount(
  @Header("Authorization") bearerToken: String,
  @Query("DealerCode") dealerCode: String
 ): Response<RFIDCountResponse>


 //new api
 @POST(Constants.VERIFY_DEALER_VEHICLE)
 suspend fun postVerifyDealerVehicle(
  @Header("Authorization") bearerToken: String,
  @Body
  verifyDealerVehicleRequest: VerifyDealerVehicleRequest
 ): Response<VerifyDealerVehicleResponse>

 @POST(Constants.CONFIRM_DEALER_VEHICLE_DELIVERY)
 suspend fun postConfirmDealerVehicleDelivery(
  @Header("Authorization") bearerToken: String,
  @Body
  confirmDealerVehicleDeliveryRequest: ConfirmDealerVehicleDeliveryRequest
 ): Response<ConfirmDealerVehicleDeliveryResponse>

 @GET(Constants.GET_DELIVERED_COUNT)
 suspend fun getDeliveredCount(
  @Header("Authorization") bearerToken: String,
 ): Response<GetDeliveredCountResponse>


 @GET(Constants.GET_DELIVERED_DETAILS)
 suspend fun getDeliveredDetails(
  @Header("Authorization") bearerToken: String,
 ): Response<ArrayList<DashboardGetDeliveredDetailsResponse>>


 @POST(Constants.CHANGE_PASSWORD)
 suspend fun changePassword(
  @Header("Authorization") bearerToken: String,
  @Body
  changePasswordRequest: ChangePasswordRequest,
 ): Response<ChangePasswordResponse>

 @POST(Constants.FORGOT_PASSWORD)
 suspend fun forgotPassword(
  @Body
  forgotPasswordRequest: ForgotPasswordRequest,
 ): Response<ForgotPasswordResponse>

 @POST(Constants.RESET_PASSWORD)
 suspend fun resetPassword(
  @Body
  resetPasswordRequest: ResetPasswordRequest,
 ): Response<GeneralResponse>
}