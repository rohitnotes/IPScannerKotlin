package com.example.ipscannerk.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.ipscannerk.model.local.VendorInfo
import com.example.ipscannerk.repo.VendorInfoRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private var vendorInfo: LiveData<VendorInfo?>? = null
    private var vendorInfoList: MutableLiveData<List<VendorInfo>>? = null
    private val repo = VendorInfoRepository(application)
    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    private var scannedVendor = MutableLiveData<VendorInfo?>()


    suspend fun getVendorInfo(macHeader: String): VendorInfo? {
        return repo.getVendorNameWithMacHeader(macHeader)
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}