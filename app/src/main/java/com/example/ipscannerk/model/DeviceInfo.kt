package com.example.ipscannerk.model

data class DeviceInfo(
    var ipAddress: String? = null,
    var macAddress: String? = null,
    var vendorName: String? = null,
    var hostname: String? = null,
    var latency: String? = null
)