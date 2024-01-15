package com.kemarport.kdmsmahindra.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.kemarport.kdmsmahindra.model.newapi.ConfirmDealerVehicleDeliveryRequest
import com.kemarport.kdmsmahindra.model.newapi.ConfirmDealerVehicleDeliveryResponse
import com.kemarport.kdmsmahindra.model.newapi.VerifyDealerVehicleRequest
import com.kemarport.kdmsmahindra.model.newapi.VerifyDealerVehicleResponse

import com.kemarport.kdmsmahindra.repository.KDMSRepository
import com.kemarport.mahindrakiosk.helper.Constants
import com.kemarport.mahindrakiosk.helper.GeneralResponse
import com.kemarport.mahindrakiosk.helper.Resource
import com.kemarport.mahindrakiosk.helper.Utils
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Response

class KDMSHomeVM (
    application: Application,
    private val kdmsRepository: KDMSRepository
) : AndroidViewModel(application) {
/*    val validateVinMutable: MutableLiveData<Resource<ValidateVinResponse>> = MutableLiveData()

    fun validateVin(
        token: String,
        baseUrl: String,
        vin: String
    ) {
        viewModelScope.launch {
            safeAPICallValidateVin(token,baseUrl, vin)
        }
    }
    private suspend fun safeAPICallValidateVin(token: String,baseUrl: String, vin: String) {
        validateVinMutable.postValue(Resource.Loading())
        try {
            if (Utils.hasInternetConnection(getApplication())) {
                val response = kdmsRepository.validateVin(token,baseUrl, vin)
                validateVinMutable.postValue(handleValidateVinResponse(response))
            } else {
                validateVinMutable.postValue(Resource.Error(Constants.NO_INTERNET))
            }
        } catch (t: Throwable) {
            when (t) {
                is Exception -> {
                    validateVinMutable.postValue(Resource.Error("${t.message}"))
                }
                else -> validateVinMutable.postValue(Resource.Error(Constants.CONFIG_ERROR))
            }
        }
    }
    private fun handleValidateVinResponse(response: Response<ValidateVinResponse>): Resource<ValidateVinResponse> {
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
                errorMessage = it.getString("message")
            }
        }
        return Resource.Error(errorMessage)
    }
/////////

    val storeVinDealerCodeDetailsLocalMutable: MutableLiveData<Resource<GeneralResponse>> = MutableLiveData()

    fun storeVinDealerCodeLocal(
        token: String,
        baseUrl: String,
        vinRequest: ConfirmVinRequest
    ) {
        viewModelScope.launch {
            safeAPICallStoreVinDealerCodeLocal(token,baseUrl, vinRequest)
        }
    }
    private suspend fun safeAPICallStoreVinDealerCodeLocal(token: String,baseUrl: String, vinRequest: ConfirmVinRequest) {
        storeVinDealerCodeDetailsLocalMutable.postValue(Resource.Loading())
        try {
            if (Utils.hasInternetConnection(getApplication())) {
                val response = kdmsRepository.storeVinDealerCodeDetailsLocal(token,baseUrl, vinRequest)
                storeVinDealerCodeDetailsLocalMutable.postValue(handleStoreVinDealerCodeDetailsLocalResponse(response))
            } else {
                storeVinDealerCodeDetailsLocalMutable.postValue(Resource.Error(Constants.NO_INTERNET))
            }
        } catch (t: Throwable) {
            when (t) {
                is Exception -> {
                    storeVinDealerCodeDetailsLocalMutable.postValue(Resource.Error("${t.message}"))
                }
                else -> storeVinDealerCodeDetailsLocalMutable.postValue(Resource.Error(Constants.CONFIG_ERROR))
            }
        }
    }
    private fun handleStoreVinDealerCodeDetailsLocalResponse(response: Response<GeneralResponse>): Resource<GeneralResponse> {
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
                errorMessage = it.getString("message")
            }
        }
        return Resource.Error(errorMessage)
    }

    */
    //new api

    val postVerifyDealerVehicleMutable: MutableLiveData<Resource<VerifyDealerVehicleResponse>> = MutableLiveData()

    fun postVerifyDealerVehicle(
        token: String,
        baseUrl: String,
        verifyDealerVehicleRequest: VerifyDealerVehicleRequest
    ) {
        viewModelScope.launch {
            safeAPICallPostVerifyDealerVehicle(token,baseUrl, verifyDealerVehicleRequest)
        }
    }
    private suspend fun safeAPICallPostVerifyDealerVehicle(
        token: String,
        baseUrl: String,
        verifyDealerVehicleRequest: VerifyDealerVehicleRequest
    ) {
        postVerifyDealerVehicleMutable.postValue(Resource.Loading())
        try {
            if (Utils.hasInternetConnection(getApplication())) {
                val response = kdmsRepository.postVerifyDealerVehicle(token,baseUrl, verifyDealerVehicleRequest)
                postVerifyDealerVehicleMutable.postValue(handlePostVerifyDealerVehicleResponse(response))
            } else {
                postVerifyDealerVehicleMutable.postValue(Resource.Error(Constants.NO_INTERNET))
            }
        } catch (t: Throwable) {
            when (t) {
                is Exception -> {
                    postVerifyDealerVehicleMutable.postValue(Resource.Error("${t.message}"))
                }
                else -> postVerifyDealerVehicleMutable.postValue(Resource.Error(Constants.CONFIG_ERROR))
            }
        }
    }
    private fun handlePostVerifyDealerVehicleResponse(response: Response<VerifyDealerVehicleResponse>): Resource<VerifyDealerVehicleResponse> {
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
/////////

    val postConfirmDealerVehicleDeliveryMutable: MutableLiveData<Resource<ConfirmDealerVehicleDeliveryResponse>> = MutableLiveData()

    fun postConfirmDealerVehicleDelivery(
        token: String,
        baseUrl: String,
        confirmDealerVehicleDeliveryRequest: ConfirmDealerVehicleDeliveryRequest
    ) {
        viewModelScope.launch {
            safeAPICallPostConfirmDealerVehicleDelivery(token,baseUrl, confirmDealerVehicleDeliveryRequest)
        }
    }
    private suspend fun safeAPICallPostConfirmDealerVehicleDelivery(
        token: String,
        baseUrl: String,
        confirmDealerVehicleDeliveryRequest: ConfirmDealerVehicleDeliveryRequest
    ) {
        postConfirmDealerVehicleDeliveryMutable.postValue(Resource.Loading())
        try {
            if (Utils.hasInternetConnection(getApplication())) {
                val response = kdmsRepository.postConfirmDealerVehicleDelivery(token,baseUrl, confirmDealerVehicleDeliveryRequest)
                postConfirmDealerVehicleDeliveryMutable.postValue(handlepostConfirmDealerVehicleDeliveryResponse(response))
            } else {
                postConfirmDealerVehicleDeliveryMutable.postValue(Resource.Error(Constants.NO_INTERNET))
            }
        } catch (t: Throwable) {
            when (t) {
                is Exception -> {
                    postConfirmDealerVehicleDeliveryMutable.postValue(Resource.Error("${t.message}"))
                }
                else -> postConfirmDealerVehicleDeliveryMutable.postValue(Resource.Error(Constants.CONFIG_ERROR))
            }
        }
    }
    private fun handlepostConfirmDealerVehicleDeliveryResponse(response: Response<ConfirmDealerVehicleDeliveryResponse>)
    : Resource<ConfirmDealerVehicleDeliveryResponse> {
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

}