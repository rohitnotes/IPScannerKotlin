package com.example.ipscannerk.repo

import android.app.Application
import androidx.lifecycle.LiveData
import com.example.ipscannerk.interactor.OnOUIDataLoaded
import com.example.ipscannerk.model.local.VendorInfo
import com.example.ipscannerk.model.local.VendorInfoDAO
import com.example.ipscannerk.model.local.VendorInfoDatabase
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class VendorInfoRepository(application: Application) {
    private var mVendorInfoDAO: VendorInfoDAO? = null
    private var executorService: ExecutorService? = null
    private var loadDataListener: OnOUIDataLoaded? = null

    init {
        executorService = Executors.newSingleThreadExecutor()
        val db = VendorInfoDatabase.getInstance(application)
        mVendorInfoDAO = db?.vendorInfoDAO()

    }

    fun checkIsDbEmpty(): LiveData<String>? {
        return mVendorInfoDAO?.checkIsDbEmpty()
    }

    fun testListener(listener: OnOUIDataLoaded){
        listener.onOuiDataLoadedListener()
    }

    fun loadVendorInfoData(vendorInfoList: List<VendorInfo>, listener: OnOUIDataLoaded) {
        executorService?.execute(Runnable {
            mVendorInfoDAO?.insertVendorInfoList(vendorInfoList)
            listener.onOuiDataLoadedListener()
        })
    }
}