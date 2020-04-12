package com.example.ipscannerk

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.net.NetworkInfo.DetailedState
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ipscannerk.databinding.ActivityMainBinding
import com.example.ipscannerk.model.DeviceInfo
import com.example.ipscannerk.view.DeviceInfoAdapter
import com.example.ipscannerk.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.FileReader
import java.io.IOException
import java.net.*
import java.util.*

// TODO : Create settings to set the reachable timeout when scanning (create new Settings Activity)
// TODO : Create Export to CSV feature
class MainActivity : AppCompatActivity() {
    private val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
    private var deviceList: MutableList<DeviceInfo> = mutableListOf()
    private var isScanning = false
    private var selfDeviceInfo: DeviceInfo? = null
    private lateinit var viewModel: MainViewModel
    private lateinit var adapter: DeviceInfoAdapter
    private lateinit var binding: ActivityMainBinding
    @SuppressLint("HardwareIds")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setSupportActionBar(toolbar)

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        adapter = DeviceInfoAdapter()
        binding.contentMain.rvDeviceInfo.layoutManager = LinearLayoutManager(this)
        binding.contentMain.rvDeviceInfo.adapter = adapter
        displaySSIDName()


        binding.contentMain.btnScanNetwork.setOnClickListener {
            deviceList.clear()
            //hold the scan button, or use it as cancel. This is the default state
            isScanning = true
            binding.contentMain.btnScanNetwork.text = getString(R.string.cancel)
            binding.contentMain.btnScanNetwork.isEnabled = false

            CoroutineScope(Default).launch {
                //get self
                val selfDevice = getSelfDeviceInfo()
                deviceList.add(selfDevice)
                val selfIpTypedArray = selfDevice.ipAddress!!.split(".").toTypedArray()
                val subnet = "${selfIpTypedArray[0]}.${selfIpTypedArray[1]}.${selfIpTypedArray[2]}"
                Log.e("SelfIP", subnet)

                //start to scan the Subnet
                scanAddresses(subnet)
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            R.id.action_export_csv -> {
                val builder: AlertDialog.Builder = AlertDialog.Builder(this)
                builder.setTitle("Confirm Export to CSV")
                    .setMessage("This will create CSV file containing list of connected device information to your phone. Please confirm that you are going to export the data as CSV file?")

                builder.setCancelable(true)
                    .setNegativeButton(android.R.string.no) { _, _ ->
                        Toast.makeText(this, "Export to CSV cancelled", Toast.LENGTH_SHORT).show()
                    }.setPositiveButton("Confirm") { _, _ ->
                        Toast.makeText(this, "Export to CSV Clicked", Toast.LENGTH_SHORT).show()
                    }
                builder.show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun getMacAddress(): String {
        try {
            val all = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (nif in all) {
                if (!nif.name.equals("wlan0", ignoreCase = true)) continue
                val macBytes = nif.hardwareAddress ?: return ""
                val res1 = StringBuilder()
                for (b in macBytes) {
                    res1.append(String.format("%02X:", b))
                }
                if (res1.isNotEmpty()) {
                    res1.deleteCharAt(res1.length - 1)
                }
                return res1.toString()
            }
        } catch (ex: Exception) {
        }
        return "02:00:00:00:00:00"
    }


    private suspend fun loadingUiState() {
        withContext(Main) {
            binding.contentMain.tvProgressDescription.text =
                getString(R.string.listing_connected_devices)
            binding.contentMain.pbScanNetwork.progress = 0
            binding.contentMain.pbScanNetwork.isIndeterminate = true
            binding.contentMain.pbScanNetwork.animate()
        }
    }

    private suspend fun loadingUiStateAfterScan() {
        withContext(Main) {
            binding.contentMain.pbScanNetwork.isIndeterminate = false
            binding.contentMain.pbScanNetwork.max = 100
            binding.contentMain.pbScanNetwork.progress = 100
            deviceList.sort()
            adapter.setupDeviceInfoListData(deviceList)
            binding.contentMain.tvProgressDescription.text =
                getString(R.string.connected_device_listed)
            isScanning = false
            binding.contentMain.btnScanNetwork.text = getString(R.string.scan)
            binding.contentMain.btnScanNetwork.isEnabled = true
        }
    }

    private suspend fun scanAddresses(subnet: String) {
        scanNetwork(subnet)
        var bufferedReader: BufferedReader? = null
        try {
            bufferedReader = BufferedReader(FileReader("/proc/net/arp"))
            var line: String? = bufferedReader.readLine()
            loadingUiState()
            while (line != null) {
                val splitted =
                    line.split((" +").toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                if (splitted.size >= 4) {
                    val ip = splitted[0]
                    val mac = splitted[3]
                    if (mac.matches(("..:..:..:..:..:..").toRegex())) {
                        if (mac != "00:00:00:00:00:00") {
                            val macHeader = mac.replace(":", "-").substring(0, 8).toUpperCase()
                            val result = viewModel.getVendorInfo(macHeader)
                            val latency = getLatencyOfIp(ip)

                            deviceList.add(
                                DeviceInfo(
                                    ip,
                                    mac,
                                    result?.vendorName,
                                    "host",
                                    latency
                                )
                            )
                        }
                    }
                }
                line = bufferedReader.readLine()
            }
            loadingUiStateAfterScan()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                bufferedReader?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun getLatencyOfIp(host: String): String {
        val TAG = "getLatencyOfIp"
        var timeOfPing: Long = 100
        val startTime = System.currentTimeMillis()
        val runtime = Runtime.getRuntime()
        var ipProcess: Process? = null
        try {
            ipProcess = runtime.exec("/system/bin/ping -c 1 $host")
            ipProcess.waitFor()
            timeOfPing = (System.currentTimeMillis() - startTime)
            Log.e(TAG, "run: pingtime is $timeOfPing")
        } catch (e: IOException) {
            Log.e(TAG, "run: IOException $e")
            e.printStackTrace()
        } catch (e: InterruptedException) {
            Log.e(TAG, "run: InterruptedException $e")
            e.printStackTrace()
        }

        return "$timeOfPing ms"
    }

    private fun scanNetwork(subnet: String) {
        val startHostIp = 0
        val endHostIp = 254
        binding.contentMain.pbScanNetwork.isIndeterminate = false
        binding.contentMain.pbScanNetwork.max = 254
        binding.contentMain.tvProgressDescription.text =
            getString(R.string.scanning_connected_devices)
        for (i in startHostIp..endHostIp) {
            val host = subnet + "." + (i + 1)
            try {
                if (InetAddress.getByName(host).isReachable(10)) println("$host is reachable") else println(
                    "$host is not reachable"
                )
            } catch (e: UnknownHostException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            binding.contentMain.pbScanNetwork.progress = i
        }
        binding.contentMain.tvProgressDescription.text = getString(R.string.scanning_complete)
    }

    /**
     * ask Location Permission for getting SSID Name
     */
    private fun displaySSIDName() {
        if (ContextCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
        ) { //Permission already granted, check the SSID
            binding.contentMain.tvSsidName.text = getWifiName()
        } else { //Location Permission has not been granted
//provide additional rationale to the user if permission was not granted and the user would benefit from additional context for the use of the permission
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this@MainActivity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                Toast.makeText(
                    this@MainActivity,
                    "Location Permission is needed to get SSID Name",
                    Toast.LENGTH_SHORT
                ).show()
                ActivityCompat.requestPermissions(
                    this@MainActivity,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
                ) //ask the permission again :)
            } else {
                ActivityCompat.requestPermissions(
                    this@MainActivity,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
                ) //ask the permission again :)
            }
        }
    }

    private fun getWifiName(): String {
        var ssidName = "SSID Name"
        val manager =
            applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (manager.isWifiEnabled) {
            val wifiInfo = manager.connectionInfo
            if (wifiInfo != null) {
                val state =
                    WifiInfo.getDetailedStateOf(wifiInfo.supplicantState)
                if (state == DetailedState.CONNECTED || state == DetailedState.OBTAINING_IPADDR) {
                    ssidName = wifiInfo.ssid
                }
            }
        }
        return ssidName
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            //Received permission result for ACCESS_FINE_LOCATION
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                binding.contentMain.tvSsidName.text = getWifiName()
            } else {
                // ACCESS_FINE_LOCATION permission was denied, so we cannot get the SSID Name
                Toast.makeText(
                    this@MainActivity,
                    "Please grant the location permission so we can check the SSID",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun getLocalDeviceInfo(): InetAddress? {
        try {
            val en = NetworkInterface.getNetworkInterfaces()
            while (en.hasMoreElements()) {
                val netInterface = en.nextElement()
                val enumIpAddress = netInterface.inetAddresses
                while (enumIpAddress.hasMoreElements()) {
                    val inetAddress = enumIpAddress.nextElement()
                    if (!inetAddress.isLoopbackAddress && inetAddress is Inet4Address) {
                        return inetAddress
                    }
                }
            }
        } catch (ex: SocketException) {
            ex.printStackTrace()
        }
        return null
    }

    private suspend fun getSelfDeviceInfo(): DeviceInfo {
        //get self IP
        val selfMac = getMacAddress()
        val selfMacHeader =
            selfMac.replace(":", "-").substring(0, 8).toUpperCase(Locale.getDefault())
        val selfDeviceInfo = getLocalDeviceInfo()
        val vendorName = viewModel.getVendorInfo(selfMacHeader)?.vendorName
        return DeviceInfo(
            selfDeviceInfo?.hostName,
            selfMac,
            vendorName,
            selfDeviceInfo?.canonicalHostName,
            "0 ms"
        )
    }
}
