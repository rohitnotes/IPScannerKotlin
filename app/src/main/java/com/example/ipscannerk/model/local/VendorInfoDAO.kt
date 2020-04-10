package com.example.ipscannerk.model.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query


@Dao
interface VendorInfoDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertVendorInfoList(vendorInfoList: List<VendorInfo>)

    @Query("SELECT * FROM vendor_info WHERE  ouiID =:macHeader LIMIT 1")
    suspend fun getVendorInfoByOuiIDSuspend(macHeader: String?): VendorInfo

    @Query("SELECT ouiId FROM vendor_info LIMIT 1")
    fun checkIsDbEmpty(): LiveData<String>?

}