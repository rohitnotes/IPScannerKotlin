package com.example.ipscannerk.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.ipscannerk.interactor.OnOUIDataLoaded
import com.example.ipscannerk.model.local.VendorInfo
import com.example.ipscannerk.repo.VendorInfoRepository

class SplashViewModel(application: Application) : AndroidViewModel(application) {
    val repo = VendorInfoRepository(application)
    private var vendorDBReady: LiveData<String>? = null
    fun isDatabaseEmpty(): LiveData<String>?{
        vendorDBReady = repo.checkIsDbEmpty()
        return vendorDBReady
    }

    fun loadOuiData(vendorInfoList: List<VendorInfo>, listener: OnOUIDataLoaded){
        repo.loadVendorInfoData(vendorInfoList, listener)
    }

}