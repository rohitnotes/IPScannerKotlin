package com.example.ipscannerk

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.NetworkInfo.DetailedState
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.example.ipscannerk.databinding.ActivityMainBinding
import com.example.ipscannerk.model.DeviceInfo
import kotlinx.android.synthetic.main.activity_main.*
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.FileReader
import java.io.IOException
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.regex.Pattern


class MainActivity : AppCompatActivity() {
    private val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
    private var deviceList: MutableList<DeviceInfo> = mutableListOf()
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setSupportActionBar(toolbar)
        displaySSIDName()
        //get self IP


        binding.contentMain.btnScanNetwork.setOnClickListener {
            //start to scan the IP Address
            scanOnNewThread("192.168.1")
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
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun readAddresses() {
        deviceList.clear()
        var bufferedReader: BufferedReader? = null
        try {
            bufferedReader = BufferedReader(FileReader("/proc/net/arp"))
            var line: String? = bufferedReader.readLine()
            while (line != null) {
                val splitted =
                    line.split((" +").toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                if (splitted != null && splitted.size >= 4) {
                    val ip = splitted[0]
                    val mac = splitted[3]
                    if (mac.matches(("..:..:..:..:..:..").toRegex())) {
                        if (mac != "00:00:00:00:00:00") {
                            Log.e("READ", "$ip | $mac")
                            deviceList.add(DeviceInfo(ip,mac,"vendor", "host", "100 ms"))
                        }
                    }
                }
                line = bufferedReader.readLine()
            }
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

    private fun scanOnNewThread(subnet: String) { //create a runnable
        val runnable = Runnable {
            for (i in 0..254) {
                val host = subnet + "." + (i + 1)
                try {
                    if (InetAddress.getByName(host).isReachable(300)) println("$host is reachable") else println(
                        "$host is not reachable"
                    )
                } catch (e: UnknownHostException) {
                    e.printStackTrace()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        Log.i("ScanOnNewThread", "Thread Scan IP: " + Thread.currentThread().name)
        //create a new thread
        val thread = Thread(runnable)
        thread.start()
        readAddresses()
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
}
