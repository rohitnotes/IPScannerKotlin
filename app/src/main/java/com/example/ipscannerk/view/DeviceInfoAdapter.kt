package com.example.ipscannerk.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.ipscannerk.R
import com.example.ipscannerk.model.DeviceInfo

class DeviceInfoAdapter : RecyclerView.Adapter<DeviceInfoAdapter.DeviceInfoViewHolder>() {
    private var deviceInfoList: MutableList<DeviceInfo> = mutableListOf()

    fun setupDeviceInfoListData(deviceInfoList: MutableList<DeviceInfo>) {
        this.deviceInfoList = deviceInfoList
        notifyDataSetChanged()
    }

    inner class DeviceInfoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvVendorName = itemView.findViewById<TextView>(R.id.tv_vendor_name)
        private val tvHostname = itemView.findViewById<TextView>(R.id.tv_hostname)
        private val tvIpAddress = itemView.findViewById<TextView>(R.id.tv_ip_address)
        private val tvMACAddress = itemView.findViewById<TextView>(R.id.tv_mac_address)
        private val tvLatency = itemView.findViewById<TextView>(R.id.tv_latency)
        fun bind(deviceInfo: DeviceInfo) {
            tvVendorName.text = deviceInfo.vendorName
            tvHostname.text = deviceInfo.hostname
            tvIpAddress.text = deviceInfo.ipAddress
            tvMACAddress.text = deviceInfo.macAddress
            tvLatency.text = deviceInfo.latency
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceInfoViewHolder {
        return DeviceInfoViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_device,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return deviceInfoList.size ?: 0
    }

    override fun onBindViewHolder(holder: DeviceInfoViewHolder, position: Int) {
        val deviceInfo = deviceInfoList[position]
        holder.bind(deviceInfo)
    }
}