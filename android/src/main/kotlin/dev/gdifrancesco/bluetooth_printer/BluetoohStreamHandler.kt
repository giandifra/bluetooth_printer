package com.chivado.sunmi

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Handler
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.EventChannel.EventSink
import android.os.Looper
import android.util.Log
import dev.gdifrancesco.bluetooth_printer.utils.BluetoothUtil
import java.lang.IllegalStateException
import java.util.HashMap


class BluetoothStreamHandler(private val context: Context) : EventChannel.StreamHandler {

    val TAG = "BluetoothStreamHandler"
    private var events: EventSink? = null
    private val mainHandler: Handler = Handler(Looper.getMainLooper())

    override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
        this.events = events;
        startScan();
    }

    override fun onCancel(arguments: Any?) {
        Log.i(TAG, "onCancel")
        stopScan();
    }

    @Throws(IllegalStateException::class)
    private fun startScan() {
        Log.i(TAG, "startScan")
        val scanner = BluetoothUtil.getBTAdapter().bluetoothLeScanner
            ?: throw IllegalStateException("getBluetoothLeScanner() is null. Is the Adapter on?")

        // 0:lowPower 1:balanced 2:lowLatency -1:opportunistic
        val settings =
            ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()
        scanner.startScan(null, settings, mScanCallback)
    }



    private fun sendNewDevice(device: BluetoothDevice) {
        Log.i(TAG, "sendNewDevice")
        val ret = HashMap<String, Any>()
        ret["address"] = device.address
        ret["name"] = device.name
        ret["type"] = device.type
        Log.i(TAG, ret.toString())
        val runnable = Runnable { events!!.success(ret) }
        mainHandler.post(runnable)
    }

    private fun stopScan() {
        Log.i(TAG, "stopScan")
        val scanner = BluetoothUtil.getBTAdapter().bluetoothLeScanner
        scanner?.stopScan(mScanCallback)
    }

    private val mScanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            Log.i(TAG, "onScanResult")
            val device = result.device
            if (device != null && device.name != null) {
                sendNewDevice(device);
            }
        }
    }
}