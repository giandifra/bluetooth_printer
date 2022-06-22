package com.chivado.sunmi

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.os.Looper
import android.util.Log
import dev.gdifrancesco.bluetooth_printer.utils.BluetoothUtil
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.EventChannel.EventSink


class BluetoothStreamHandler(private val context: Context) : EventChannel.StreamHandler {

    val TAG = "BluetoothStreamHandler"
    private var events: EventSink? = null
    private val mainHandler: Handler = Handler(Looper.getMainLooper())

    override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
        Log.i(TAG, "onListen")
        this.events = events;
        //startScan();
        updateState();
        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        filter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
        context.registerReceiver(mReceiver, filter)
    }

    fun updateState() {
        val state = BluetoothUtil.getBTAdapter().state;
        val ret = HashMap<String, Any>()
        ret["action"] = "stateChanged"
        ret["state"] = if (state >= 10) state - 10 else 4
        val runnable = Runnable { events!!.success(ret) }
        mainHandler.post(runnable)
    }

    override fun onCancel(arguments: Any?) {
        Log.i(TAG, "onCancel")
        //    stopScan();
        this.events = null;
        context.unregisterReceiver(mReceiver)
    }

    private val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            Log.d(
                TAG,
                "stateStreamHandler, current action: $action"
            )
            val ret = HashMap<String, Any>()
            when (action) {
                BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED -> {
                    val connectionState =
                        intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, -1)
                    val previousConnectionState =
                        intent.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_CONNECTION_STATE, -1)
                    // STATE_DISCONNECTED 0
                    // STATE_CONNECTING 1
                    // STATE_CONNECTED 2
                    // STATE_DISCONNECTING 3
                    Log.i(TAG, "ACTION_CONNECTION_STATE_CHANGED");
                    Log.i(TAG, "EXTRA_STATE: $connectionState");
                    Log.i(TAG, "EXTRA_PREVIOUS_STATE: $previousConnectionState");

                    ret["action"] = "connectionStateChanged"
                    ret["connectionState"] = connectionState
                    ret["previousConnectionState"] = previousConnectionState
                }
                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)
                    val previousState =
                        intent.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_STATE, -1)

                    Log.i(TAG, "ACTION_STATE_CHANGED");
                    Log.i(TAG, "EXTRA_STATE: $state");
                    Log.i(TAG, "EXTRA_PREVIOUS_STATE: $previousState");

                    //STATE_OFF, 10
                    //STATE_TURNING_ON, 11
                    //STATE_ON, 12
                    //STATE_TURNING_OFF 13
                    ret["action"] = "stateChanged"
                    ret["state"] = if (state >= 10) state - 10 else 4
                    ret["previousState"] = if (previousState >= 10) previousState - 10 else 4
                }
                BluetoothDevice.ACTION_ACL_CONNECTED -> {

                    Log.i(TAG, "ACTION_ACL_CONNECTED");
                    val device =
                        intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    ret["action"] = "connected"
                    ret["device"] = device?.address.toString()
                }
                BluetoothDevice.ACTION_ACL_DISCONNECTED -> {

                    Log.i(TAG, "ACTION_ACL_DISCONNECTED");
                    val device =
                        intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    ret["action"] = "disconnected"
                    ret["device"] = device?.address.toString()
                }
                else -> Log.i(TAG, "unmanaged action $action")
            }
            if (ret.isNotEmpty()) {
                //Log.i(TAG, ret.toString())
                val runnable = Runnable { events!!.success(ret) }
                mainHandler.post(runnable)
            }
        }
    }

/*
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
    */
}
