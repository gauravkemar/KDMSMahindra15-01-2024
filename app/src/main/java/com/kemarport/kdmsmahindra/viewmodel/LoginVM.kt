package com.kemarport.kdmsmahindra.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.kemarport.kdmsmahindra.model.changepassword.ChangePasswordRequest
import com.kemarport.kdmsmahindra.model.changepassword.ChangePasswordResponse
import com.kemarport.kdmsmahindra.model.forgotpassword.ForgotPasswordRequest
import com.kemarport.kdmsmahindra.model.forgotpassword.ForgotPasswordResponse
import com.kemarport.kdmsmahindra.model.forgotpassword.ResetPasswordRequest
import com.kemarport.kdmsmahindra.model.login.LoginRequest
import com.kemarport.kdmsmahindra.model.login.LoginResponse
import com.kemarport.kdmsmahindra.repository.KDMSRepository
import com.kemarport.mahindrakiosk.helper.Constants
import com.kemarport.mahindrakiosk.helper.GeneralResponse
import com.kemarport.mahindrakiosk.helper.Resource
import com.kemarport.mahindrakiosk.helper.Utils

import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Response

class LoginVM(
    application: Application,
    private val kdmsRepository: KDMSRepository
) : AndroidViewModel(application) {
    val loginMutableLiveData: MutableLiveData<Resource<LoginResponse>> = MutableLiveData()

    fun login(
        baseUrl: String,
        loginRequest: LoginRequest
    ) {
        viewModelScope.launch {
            safeAPICallDtmsLogin(baseUrl, loginRequest)
        }
    }

    private fun handleDtmsUserLoginResponse(response: Response<LoginResponse>): Resource<LoginResponse> {
        var errorMessage = ""
        if (response.isSuccessful) {
            response.body()?.let { Response ->
                return Resource.Success(Response)
            }
        } else if (response.errorBody() != null) {
            val errorObject = response.errorBody()?.let {
                JSONObject(it.charStream().readText())
            }
            errorObject?.let {
                errorMessage = it.getString(Constants.HTTP_ERROR_MESSAGE)
            }
        }
        return Resource.Error(errorMessage)
    }

    private suspend fun safeAPICallDtmsLogin(baseUrl: String, loginRequest: LoginRequest) {
        loginMutableLiveData.postValue(Resource.Loading())
        try {
            if (Utils.hasInternetConnection(getApplication())) {
                val response = kdmsRepository.login(baseUrl, loginRequest)
                loginMutableLiveData.postValue(handleDtmsUserLoginResponse(response))
            } else {
                loginMutableLiveData.postValue(Resource.Error(Constants.NO_INTERNET))
            }
        } catch (t: Throwable) {
            when (t) {
                is Exception -> {
                    loginMutableLiveData.postValue(Resource.Error("${t.message}"))
                }

                else -> loginMutableLiveData.postValue(Resource.Error(Constants.CONFIG_ERROR))
            }
        }
    }

    /////////changepasword Api
    val changePasswordMutableLiveData: MutableLiveData<Resource<ChangePasswordResponse>> =
        MutableLiveData()

    fun changePassword(
        token:String,
        baseUrl: String,
        changePasswordRequest: ChangePasswordRequest
    ) {
        viewModelScope.launch {
            safeAPICallChangePasswordDetails(token,baseUrl, changePasswordRequest)
        }
    }

    private suspend fun safeAPICallChangePasswordDetails(
        token:String,
        baseUrl: String,
        changePasswordRequest: ChangePasswordRequest
    ) {
        changePasswordMutableLiveData.postValue(Resource.Loading())
        try {
            if (Utils.hasInternetConnection(getApplication())) {
                val response = kdmsRepository.changePassword(token,baseUrl, changePasswordRequest)
                changePasswordMutableLiveData.postValue(handleChangePasswordResponse(response))
            } else {
                changePasswordMutableLiveData.postValue(Resource.Error(Constants.NO_INTERNET))
            }
        } catch (t: Throwable) {
            when (t) {
                is Exception -> {
                    changePasswordMutableLiveData.postValue(Resource.Error("${t.message}"))
                }

                else -> changePasswordMutableLiveData.postValue(Resource.Error(Constants.CONFIG_ERROR))
            }
        }
    }

    private fun handleChangePasswordResponse(response: Response<ChangePasswordResponse>): Resource<ChangePasswordResponse> {
        var errorMessage = ""
        if (response.isSuccessful) {
            response.body()?.let { response ->
                return Resource.Success(response)
            }
        } else if (response.errorBody() != null) {
            val errorObject = response.errorBody()?.let {
                JSONObject(it.charStream().readText())
            }
            errorObject?.let {
                errorMessage = it.getString(Constants.HTTP_ERROR_MESSAGE)
            }
        }
        return Resource.Error(errorMessage)
    }


    /////////forgotpasword Api
    val forgotPasswordMutableLiveData: MutableLiveData<Resource<ForgotPasswordResponse>> =
        MutableLiveData()

    fun forgotPassword(
        baseUrl: String,
        forgotPasswordRequest: ForgotPasswordRequest
    ) {
        viewModelScope.launch {
            safeAPICallForgotPasswordDetails(baseUrl, forgotPasswordRequest)
        }
    }

    private suspend fun safeAPICallForgotPasswordDetails(
        baseUrl: String,
        forgotPasswordRequest: ForgotPasswordRequest
    ) {
        forgotPasswordMutableLiveData.postValue(Resource.Loading())
        try {
            if (Utils.hasInternetConnection(getApplication())) {
                val response = kdmsRepository.forgotPassword(baseUrl, forgotPasswordRequest)
                forgotPasswordMutableLiveData.postValue(handleForgotPasswordResponse(response))
            } else {
                forgotPasswordMutableLiveData.postValue(Resource.Error(Constants.NO_INTERNET))
            }
        } catch (t: Throwable) {
            when (t) {
                is Exception -> {
                    forgotPasswordMutableLiveData.postValue(Resource.Error("${t.message}"))
                }

                else -> forgotPasswordMutableLiveData.postValue(Resource.Error(Constants.CONFIG_ERROR))
            }
        }
    }

    private fun handleForgotPasswordResponse(response: Response<ForgotPasswordResponse>): Resource<ForgotPasswordResponse> {
        var errorMessage = ""
        if (response.isSuccessful) {
            response.body()?.let { response ->
                return Resource.Success(response)
            }
        } else if (response.errorBody() != null) {
            val errorObject = response.errorBody()?.let {
                JSONObject(it.charStream().readText())
            }
            errorObject?.let {
                errorMessage = it.getString(Constants.HTTP_ERROR_MESSAGE)
            }
        }
        return Resource.Error(errorMessage)
    }

    /////////resetpasword Api
    val resetPasswordMutableLiveData: MutableLiveData<Resource<GeneralResponse>> =
        MutableLiveData()

    fun resetPassword(
        baseUrl: String,
        resetPasswordRequest: ResetPasswordRequest
    ) {
        viewModelScope.launch {
            safeAPICallResetPasswordDetails(baseUrl, resetPasswordRequest)
        }
    }

    private suspend fun safeAPICallResetPasswordDetails(
        baseUrl: String,
        resetPasswordRequest: ResetPasswordRequest
    ) {
        resetPasswordMutableLiveData.postValue(Resource.Loading())
        try {
            if (Utils.hasInternetConnection(getApplication())) {
                val response = kdmsRepository.resetPassword(baseUrl, resetPasswordRequest)
                resetPasswordMutableLiveData.postValue(handleResetPasswordResponse(response))
            } else {
                resetPasswordMutableLiveData.postValue(Resource.Error(Constants.NO_INTERNET))
            }
        } catch (t: Throwable) {
            when (t) {
                is Exception -> {
                    resetPasswordMutableLiveData.postValue(Resource.Error("${t.message}"))
                }

                else -> resetPasswordMutableLiveData.postValue(Resource.Error(Constants.CONFIG_ERROR))
            }
        }
    }

    private fun handleResetPasswordResponse(response: Response<GeneralResponse>): Resource<GeneralResponse> {
        var errorMessage = ""
        if (response.isSuccessful) {
            response.body()?.let { response ->
                return Resource.Success(response)
            }
        } else if (response.errorBody() != null) {
            val errorObject = response.errorBody()?.let {
                JSONObject(it.charStream().readText())
            }
            errorObject?.let {
                errorMessage = it.getString(Constants.HTTP_ERROR_MESSAGE)
            }
        }
        return Resource.Error(errorMessage)
    }

}