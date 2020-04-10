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
    fun getVendorInfoByOuiID(macHeader: String?): LiveData<VendorInfo?>?

    @Query("SELECT OuiId FROM vendor_info LIMIT 1")
    fun checkIsDbEmpty(): LiveData<String>?

}