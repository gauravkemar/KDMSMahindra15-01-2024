package com.kemarport.kdmsmahindra.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.kemarport.kdmsmahindra.model.dashboard.RFIDCountResponse
import com.kemarport.kdmsmahindra.model.dashboard.VehicleConfirmationCountResponseItem
import com.kemarport.kdmsmahindra.model.dashboard.VehicleConfirmationResponseItem
import com.kemarport.kdmsmahindra.model.newapi.DashboardGetDeliveredDetailsResponse
import com.kemarport.kdmsmahindra.model.newapi.GetDeliveredCountResponse
import com.kemarport.kdmsmahindra.repository.KDMSRepository
import com.kemarport.mahindrakiosk.helper.Constants
import com.kemarport.mahindrakiosk.helper.Resource
import com.kemarport.mahindrakiosk.helper.Utils
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Response

class DashboardViewModel (
    application: Application,
    private val kdmsRepository: KDMSRepository
) : AndroidViewModel(application) {

    //dashboard
    private val _vehicleConfirmationCountMutableLiveData =
        MutableLiveData<Resource<List<VehicleConfirmationCountResponseItem>?>>()
    val vehicleConfirmationCountLiveData = _vehicleConfirmationCountMutableLiveData

    //dashboard
    private val _vehicleConfirmationMutableLiveData =
        MutableLiveData<Resource<List<VehicleConfirmationResponseItem>?>>()
    val vehicleConfirmationLiveData = _vehicleConfirmationMutableLiveData

    //dahsBoard
    private val _rfidMutableLiveData = MutableLiveData<Resource<RFIDCountResponse?>>()
    val rfidMutableLiveData = _rfidMutableLiveData


    fun getVehicleConfirmationCount(
        token: String,
        baseUrl: String = Constants.BASE_URL_LOCAL,
        dealerCode: String,
        period: Int
    ) {
        viewModelScope.launch {
            fetchVehicleConfirmationCount(token,baseUrl, dealerCode, period)
        }
    }

    fun getVehicleConfirmation(
        token: String,
        baseUrl: String = Constants.BASE_URL_LOCAL,
        dealerCode: String,
        period: Int
    ) {
        viewModelScope.launch {
            fetchVehicleConfirmation(token,baseUrl, dealerCode, period)
        }
    }

    fun getRFIDCount(
        token: String,
        baseUrl: String = Constants.BASE_URL_LOCAL,
        dealerCode: String
    ) {
        viewModelScope.launch {
            fetchRFIDCount(token,baseUrl, dealerCode)
        }
    }

    private suspend fun fetchVehicleConfirmationCount(
        baseUrl: String = Constants.BASE_URL_LOCAL,
        dealerCode: String,
        dealerCode1: String,
        period: Int
    ) {
        _vehicleConfirmationCountMutableLiveData.postValue(Resource.Loading())
        try {
            if (Utils.hasInternetConnection(getApplication())) {
                val response = kdmsRepository.getDealerVehicleConfirmationCount(dealerCode1,baseUrl, period, dealerCode)
                _vehicleConfirmationCountMutableLiveData.postValue(Resource.Success(response.body()))
            } else {
                _vehicleConfirmationCountMutableLiveData.postValue(Resource.Error(Constants.NO_INTERNET))
            }
        } catch (e: Exception) {
            when (e) {
                is Exception -> {
                    _vehicleConfirmationCountMutableLiveData.postValue(Resource.Error("${e.message}"))
                }

                else -> _vehicleConfirmationCountMutableLiveData.postValue(Resource.Error(Constants.CONFIG_ERROR))
            }
        }
    }

    private suspend fun fetchVehicleConfirmation(
        baseUrl: String = Constants.BASE_URL_LOCAL,
        dealerCode: String,
        dealerCode1: String,
        period: Int
    ) {
        _vehicleConfirmationMutableLiveData.postValue(Resource.Loading())
        try {
            if (Utils.hasInternetConnection(getApplication())) {
                val response = kdmsRepository.getDealerVehicleConfirmation(dealerCode1,baseUrl, period, dealerCode)
                _vehicleConfirmationMutableLiveData.postValue(Resource.Success(response.body()))
            } else {
                _vehicleConfirmationMutableLiveData.postValue(Resource.Error(Constants.NO_INTERNET))
            }
        } catch (e: Exception) {
            when (e) {
                is Exception -> {
                    _vehicleConfirmationMutableLiveData.postValue(Resource.Error("${e.message}"))
                }

                else -> _vehicleConfirmationMutableLiveData.postValue(Resource.Error(Constants.CONFIG_ERROR))
            }
        }
    }

    private suspend fun fetchRFIDCount(
        baseUrl: String = Constants.BASE_URL_LOCAL,
        dealerCode: String,
        dealerCode1: String
    ) {
        _rfidMutableLiveData.postValue(Resource.Loading())
        try {
            if (Utils.hasInternetConnection(getApplication())) {
                val response = kdmsRepository.getScanRFIDCount(dealerCode1,baseUrl, dealerCode)
                _rfidMutableLiveData.postValue(Resource.Success(response.body()))
            } else {
                _rfidMutableLiveData.postValue(Resource.Error(Constants.NO_INTERNET))
            }
        } catch (e: Exception) {
            when (e) {
                is Exception -> {
                    _rfidMutableLiveData.postValue(Resource.Error("${e.message}"))
                }

                else -> _rfidMutableLiveData.postValue(Resource.Error(Constants.CONFIG_ERROR))
            }
        }
    }

    ////new api
    val getDeliveredCountMutable: MutableLiveData<Resource<GetDeliveredCountResponse>> = MutableLiveData()

    fun getDeliveredCount(
        token: String,
        baseUrl: String,
    ) {
        viewModelScope.launch {
            safeAPICallGetDeliveredCount(token,baseUrl)
        }
    }
    private suspend fun safeAPICallGetDeliveredCount(
        token: String,
        baseUrl: String,

    ) {
        getDeliveredCountMutable.postValue(Resource.Loading())
        try {
            if (Utils.hasInternetConnection(getApplication())) {
                val response = kdmsRepository.getDeliveredCount(token,baseUrl )
                getDeliveredCountMutable.postValue(handleGetDeliveredCountMutableResponse(response))
            } else {
                getDeliveredCountMutable.postValue(Resource.Error(Constants.NO_INTERNET))
            }
        } catch (t: Throwable) {
            when (t) {
                is Exception -> {
                    getDeliveredCountMutable.postValue(Resource.Error("${t.message}"))
                }
                else -> getDeliveredCountMutable.postValue(Resource.Error(Constants.CONFIG_ERROR))
            }
        }
    }
    private fun handleGetDeliveredCountMutableResponse(response: Response<GetDeliveredCountResponse>): Resource<GetDeliveredCountResponse> {
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


    ////new api
    val getDeliveredDetailsMutable: MutableLiveData<Resource<ArrayList<DashboardGetDeliveredDetailsResponse>>> = MutableLiveData()

    fun getDeliveredDetails(
        token: String,
        baseUrl: String,
    ) {
        viewModelScope.launch {
            safeAPICallGetDeliveredDetails(token,baseUrl)
        }
    }
    private suspend fun safeAPICallGetDeliveredDetails(
        token: String,
        baseUrl: String,
    ) {
        getDeliveredDetailsMutable.postValue(Resource.Loading())
        try {
            if (Utils.hasInternetConnection(getApplication())) {
                val response = kdmsRepository.getDeliveredDetails(token,baseUrl )
                getDeliveredDetailsMutable.postValue(handleGetDeliveredDetailsResponse(response))
            } else {
                getDeliveredDetailsMutable.postValue(Resource.Error(Constants.NO_INTERNET))
            }
        } catch (t: Throwable) {
            when (t) {
                is Exception -> {
                    getDeliveredDetailsMutable.postValue(Resource.Error("${t.message}"))
                }
                else -> getDeliveredDetailsMutable.postValue(Resource.Error(Constants.CONFIG_ERROR))
            }
        }
    }
    private fun handleGetDeliveredDetailsResponse(response: Response<ArrayList<DashboardGetDeliveredDetailsResponse>>):
            Resource<ArrayList<DashboardGetDeliveredDetailsResponse>> {
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