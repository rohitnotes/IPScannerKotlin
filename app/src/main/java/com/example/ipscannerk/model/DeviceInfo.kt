package com.example.ipscannerk.model

import android.util.Log

data class DeviceInfo(
    var ipAddress: String? = null,
    var macAddress: String? = null,
    var vendorName: String? = null,
    var hostname: String? = null,
    var latency: String? = null
) : Comparable<DeviceInfo> {
    override fun compareTo(other: DeviceInfo): Int {
        val otherHostNumber = other.ipAddress!!.split(".").toTypedArray()[3]
        val thisHostNumber = this.ipAddress!!.split(".").toTypedArray()[3]
        return Integer.parseInt(thisHostNumber) - Integer.parseInt(otherHostNumber)
    }
}