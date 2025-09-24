package com.kemarport.kdmsmahindra.viewmodel

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.kemarport.kdmsmahindra.model.setgeofence.GetGeofenceByDealer
import com.kemarport.kdmsmahindra.model.setgeofence.PostGeofenceCoordinatesRequest
import com.kemarport.kdmsmahindra.model.setgeofence.PostGeofenceCoordinatesResponse
import com.kemarport.kdmsmahindra.repository.KDMSRepository
import com.kemarport.mahindrakiosk.helper.Constants
import com.kemarport.mahindrakiosk.helper.Resource
import com.kemarport.mahindrakiosk.helper.Utils
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Response
import retrofit2.http.Header

class SetFirstTimeGeofenceViewModel (
    application: Application,
    private val kymsRepository: KDMSRepository
) : AndroidViewModel(application) {
    ///get all dealers
    val getGeofenceByDealerMutable: MutableLiveData<Resource<ArrayList<GetGeofenceByDealer>>> =
        MutableLiveData()

    fun getGeofenceByDealer(
        context: Activity,
        dealerCode: String,
    ) {
        viewModelScope.launch {
            safeAPICallGetGeofenceByDealer(context,dealerCode)
        }
    }

    private suspend fun safeAPICallGetGeofenceByDealer(
        context: Activity,
        dealerCode: String,
    ) {
        getGeofenceByDealerMutable.postValue(Resource.Loading())
        try {
            if (Utils.hasInternetConnection(getApplication())) {
                val response = kymsRepository.getGeofenceByDealer(context,dealerCode)
                getGeofenceByDealerMutable.postValue(handleGetAllActiveDealersResponse(response))
            } else {
                getGeofenceByDealerMutable.postValue(Resource.Error(Constants.NO_INTERNET))
            }
        } catch (t: Throwable) {
            when (t) {
                is Exception -> {
                    getGeofenceByDealerMutable.postValue(Resource.Error("${t.message}"))
                }
                else -> getGeofenceByDealerMutable.postValue(Resource.Error(Constants.CONFIG_ERROR))
            }
        }
    }

    private fun handleGetAllActiveDealersResponse(response: Response<ArrayList<GetGeofenceByDealer>>): Resource<ArrayList<GetGeofenceByDealer>>? {
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

    ///post child locations
    val postGeofenceCoordinatesMutable: MutableLiveData<Resource<PostGeofenceCoordinatesResponse>> =
        MutableLiveData()

    fun postGeofenceCoordinates(
        context: Activity,
        postGeofenceCoordinatesRequest: PostGeofenceCoordinatesRequest
    ) {
        viewModelScope.launch {
            safeAPICallPostGeofenceCoordinates(context,postGeofenceCoordinatesRequest)
        }
    }

    private suspend fun safeAPICallPostGeofenceCoordinates(
        context: Activity,
        postGeofenceCoordinatesRequest: PostGeofenceCoordinatesRequest
    ) {
        postGeofenceCoordinatesMutable.postValue(Resource.Loading())
        try {
            if (Utils.hasInternetConnection(getApplication())) {
                val response = kymsRepository.postGeofenceCoordinates(context,postGeofenceCoordinatesRequest)
                postGeofenceCoordinatesMutable.postValue(handlePostGeofenceCoordinatesResponse(response))
            } else {
                postGeofenceCoordinatesMutable.postValue(Resource.Error(Constants.NO_INTERNET))
            }
        } catch (t: Throwable) {
            when (t) {
                is Exception -> {
                    postGeofenceCoordinatesMutable.postValue(Resource.Error("${t.message}"))
                }
                else -> postGeofenceCoordinatesMutable.postValue(Resource.Error(Constants.CONFIG_ERROR))
            }
        }
    }

    private fun handlePostGeofenceCoordinatesResponse(response: Response<PostGeofenceCoordinatesResponse>):
            Resource<PostGeofenceCoordinatesResponse>? {
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