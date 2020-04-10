package com.example.ipscannerk.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.ipscannerk.model.local.VendorInfoDAO
import java.lang.IllegalArgumentException

/**
 * We'll use this later as we migrate from AndroidViewModel
 * */
class SplashViewModelFactory(
    private val dataSource: VendorInfoDAO,
    private val application: Application
): ViewModelProvider.Factory{

    @SuppressWarnings("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SplashViewModel::class.java)){
//            return SplashViewModel(dataSource, application) as T
        }

        throw IllegalArgumentException("unknown ViewModel Class")
    }
}