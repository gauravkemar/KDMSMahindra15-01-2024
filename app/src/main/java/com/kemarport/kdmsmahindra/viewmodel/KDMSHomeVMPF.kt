package com.kemarport.kdmsmahindra.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kemarport.kdmsmahindra.repository.KDMSRepository

class KDMSHomeVMPF (
    private val application: Application,
    private val kdmsRepository: KDMSRepository,
) : ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return KDMSHomeVM(application, kdmsRepository) as T
    }
}