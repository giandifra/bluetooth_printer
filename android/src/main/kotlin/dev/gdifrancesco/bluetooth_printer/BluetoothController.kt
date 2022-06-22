package dev.gdifrancesco.bluetooth_printer

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.system.Os.socket
import android.util.Log
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*


class BluetoothController(private val context: Context) {

    private val TAG = "BluetoothController"
    private val innerPrinterAddress = "00:11:22:33:44:55"
    private val PRINTER_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private val socketMap = HashMap<String, BluetoothSocket>();
    private val bluetoothManager: BluetoothManager


    init {
        val application = context.applicationContext as Application
        bluetoothManager =
            application.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    }

    @Throws(IOException::class)
    private fun getSocket(device: BluetoothDevice): BluetoothSocket {
        try {

            bluetoothManager.adapter.cancelDiscovery();
           /* if (Build.VERSION.SDK_INT >= 10) {
                try {
                    val m: Method = device.javaClass.getMethod(
                        "createInsecureRfcommSocketToServiceRecord", arrayOf<Class<*>>(
                            UUID::class.java
                        )
                    )
                    return (m.invoke(device, mMyUuid) as BluetoothSocket)
                } catch (e: java.lang.Exception) {
                    Log.e(TAG, "Could not create Insecure RFComm Connection", e)
                }
            }*/


            //val uuid = device.uuids[0].uuid;
            //val socket: BluetoothSocket =
            //    device.createRfcommSocketToServiceRecord(uuid)

            val socket: BluetoothSocket =
               device.createRfcommSocketToServiceRecord(PRINTER_UUID)
            Log.i(TAG, "socket is already connected? : ${socket.isConnected}")
            socket.connect()
            return socket
        } catch (e: IOException) {
            //Toast.makeText(context, R.string.toast_6, Toast.LENGTH_SHORT).show();
            e.printStackTrace()
            Log.e(TAG, e.message!!)
            try {
                Log.e("", "trying fallback...")
                val socket :BluetoothSocket= device.javaClass.getMethod(
                    "createRfcommSocket",
                    Int::class.javaPrimitiveType
                ).invoke(device, 2) as BluetoothSocket
                socket.connect()
                Log.e("", "Connected")
                return socket
            } catch (e2: Exception) {
                Log.e("", "Couldn't establish Bluetooth connection!")
                throw e2
            }
            //throw e;
        }
    }

    fun getBondedDevice(): Set<BluetoothDevice> {
        return bluetoothManager.adapter.bondedDevices;
    }

    /**
     * Reset input and output streams and make sure socket is closed.
     * This method will be used during shutdown() to ensure that the connection is properly closed during a shutdown.
     * @return
     */
    private fun resetConnection(address: String) {
        val socket = socketMap[address]
        val inputStream = socket?.inputStream
        val outputStream = socket?.outputStream
        if (inputStream != null) {
            try {
                inputStream.close()
            } catch (e: Exception) {
            }
        }
        if (outputStream != null) {
            try {
                outputStream.close()
            } catch (e: Exception) {
            }
        }
        if (socket != null) {
            try {
                socket.close()
            } catch (e: Exception) {
            }
            socketMap.remove(address)
        }
    }

    fun connectBlueTooth(deviceAddress: String): Boolean {
        Log.i(TAG, "connecting to address $deviceAddress")
        val bluetoothSocket = socketMap[deviceAddress]
        if (bluetoothSocket == null) {
            if (bluetoothManager.adapter == null) {
                //Toast.makeText(context,  R.string.toast_3, Toast.LENGTH_SHORT).show();
                Log.i(TAG, "bluetooth adapter is null")
                return false
            }
            if (!bluetoothManager.adapter.isEnabled) {
                //Toast.makeText(context, R.string.toast_4, Toast.LENGTH_SHORT).show();
                Log.i(TAG, "bluetooth is not enabled")
                return false
            }
            var device: BluetoothDevice? = getDevice(bluetoothManager.adapter, deviceAddress)
            if (device == null) {
                //Toast.makeText(context, R.string.toast_5, Toast.LENGTH_SHORT).show();
                Log.i(TAG, "device is null")
                return false
            }
            try {
                val socket = getSocket(device) ?: return false
                socketMap[deviceAddress] = socket
            } catch (e: IOException) {
                //Toast.makeText(context, R.string.toast_6, Toast.LENGTH_SHORT).show();
                e.printStackTrace()
                return false
            }
        }
        return true
    }

    private fun getDevice(
        bluetoothAdapter: BluetoothAdapter,
        deviceAddress: String?
    ): BluetoothDevice? {
        var innerPrinterDevice: BluetoothDevice? = null
        val devices = bluetoothAdapter.bondedDevices
        Log.i(TAG, "device Address passed is: $deviceAddress")
        for (device in devices) {
            Log.i(TAG, "device: " + device.name + " | " + device.address)
            if (deviceAddress != null) {
                if (device.address == deviceAddress) {
                    innerPrinterDevice = device
                    break
                }
            } else {
                if (device.address == innerPrinterAddress) {
                    innerPrinterDevice = device
                    break
                }
            }
        }
        return innerPrinterDevice
    }

    fun sendData(bytes: ByteArray, address: String) {
        val socket = socketMap[address]
        if (socket != null) {
            var out: OutputStream?
            try {
                //val input = socket.inputStream
                out = socket.outputStream
                out.write(bytes, 0, bytes.size)
                Log.i("BluetoothPrint", "end write ${bytes.size} bytes")
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else {
            Log.i("BluetoothPrint", "bluetoothSocket is null")
        }
    }

    fun getConnectedDevices(): List<String>? {
        val array = mutableListOf<String>()
        socketMap.keys.forEach { k ->
            if (socketMap[k]!!.isConnected) array.add(k)
        }
        Log.i(TAG, "connected socket $array")
        return array
    }

    fun isConnected(address: String): Boolean {
        val socket = socketMap[address] ?: return false
        return socket.isConnected
    }

    fun disconnectBlueTooth(address: String): Boolean {
        val socket = socketMap[address] ?: return true
        return try {
            //Log.i(TAG, socket.isConnected.toString());
            //val input = socket.inputStream
            val out = socket.outputStream

            //input.close()
            //out.close()

            socket.close()
            socketMap.remove(address)
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }
}