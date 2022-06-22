package dev.gdifrancesco.bluetooth_printer

import android.app.Application
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import java.io.IOException
import java.util.*
import java.util.concurrent.Executors

class BluetoothController2(private val context: Context) {

    private val TAG = "BluetoothController"
    private val innerPrinterAddress = "00:11:22:33:44:55"
    private val PRINTER_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private val socketMap = HashMap<String, BluetoothConnectionWrapper>();
    private val bluetoothManager: BluetoothManager
    private val handler = Handler(Looper.getMainLooper())

    init {
        val application = context.applicationContext as Application
        bluetoothManager =
            application.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    }

    fun getBondedDevice(): Set<BluetoothDevice> {
        return bluetoothManager.adapter.bondedDevices;
    }

    fun connectBlueTooth(deviceAddress: String, onResult: (Boolean, Exception?) -> Unit) {
        Log.i(TAG, "connecting to address $deviceAddress")
        val connection = BluetoothConnectionWrapper(deviceAddress, bluetoothManager.adapter)
        Log.i(TAG, "new printer $deviceAddress added to map")
        socketMap[deviceAddress] = connection
        Log.d(TAG, "Connecting to $deviceAddress")
        //val executor = Executors.newSingleThreadExecutor()
        try {
            connection.connect(deviceAddress)
            Log.i(TAG,"connection $deviceAddress complete");
            handler.post {
                onResult(true, null);
            }
        } catch (ex: Exception) {
            Log.i(TAG,"connection $deviceAddress error");
            Log.i(TAG, ex.toString());
            socketMap.remove(deviceAddress)
            handler.post {
                onResult(false, ex);
            }
        }
        //executor.execute { }
    }

    fun sendData(bytes: ByteArray, address: String, onResult: (Boolean, Exception?) -> Unit) {
        val connection = socketMap[address] ?: return
        //val executor = Executors.newSingleThreadExecutor()
        //executor.execute {
            try {
                Log.i(TAG,"start sending data $address");
                connection.write(bytes)
                handler.post {
                    Log.i(TAG,"write $address complete");
                    onResult(true, null);
                }
                //activity.runOnUiThread { result.success(null) }
            } catch (ex: Exception) {
                Log.i(TAG, ex.toString());
                handler.post {
                    Log.i(TAG,"write $address complete");
                    onResult(false, ex);
                }
            }

        //}
    }

    fun getConnectedDevices(): List<String>? {
        val array = mutableListOf<String>()
        socketMap.keys.forEach { k ->
            val b : BluetoothConnectionWrapper = socketMap[k]!!
            if (b.isSocketConnected) array.add(k)
        }
        Log.i(TAG, "connected socket $array")
        return array
    }

    fun isConnected(address: String): Boolean {
        val socket = socketMap[address] ?: return false
        return socket.isConnected && socket.isSocketConnected
    }

    fun disconnectBlueTooth(address: String): Boolean {
        val socket = socketMap[address] ?: return true
        return try {
            socket.disconnect();
            socketMap.remove(address)
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }
}