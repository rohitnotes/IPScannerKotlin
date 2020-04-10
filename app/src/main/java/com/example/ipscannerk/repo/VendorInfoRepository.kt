package com.example.ipscannerk.repo

import android.app.Application
import com.example.ipscannerk.model.local.VendorInfo
import com.example.ipscannerk.model.local.VendorInfoDAO
import com.example.ipscannerk.model.local.VendorInfoDatabase
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class VendorInfoRepository(application: Application) {
    private var mVendorInfoDAO: VendorInfoDAO? = null
    private var executorService: ExecutorService? = null

    init {
        executorService = Executors.newSingleThreadExecutor()
        val db = VendorInfoDatabase.getInstance(application)
        mVendorInfoDAO = db?.vendorInfoDAO()
    }

    fun loadVendorInfoData(vendorInfoList: List<VendorInfo>) {
        executorService?.execute(Runnable{
            mVendorInfoDAO?.insertVendorInfoList(vendorInfoList)
        })

    }
}