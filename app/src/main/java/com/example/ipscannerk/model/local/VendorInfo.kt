package com.example.ipscannerk.model.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vendor_info")
data class VendorInfo(
    @PrimaryKey var ouiId: String,
    var vendorName: String
)