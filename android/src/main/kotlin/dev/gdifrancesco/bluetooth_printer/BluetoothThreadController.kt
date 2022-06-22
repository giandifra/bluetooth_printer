/*package dev.gdifrancesco.bluetooth_printer

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.text.TextUtils

class BluetoothThreadController(private val context: Context) {

    private var sBluetoothManager //Prevent multiple creations, set as singleton
            : BluetoothThreadController? = null

    private val mBluetoothThreadMap: MutableMap<BluetoothDevice, BluetoothThread> =
        HashMap() //Store the corresponding connection thread when each Bluetooth device is successfully connected

    private var mPrintingThreadCount = 0

    private val bluetoothManager: BluetoothManager


    init {
        val application = context.applicationContext as Application
        bluetoothManager =
            application.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    }

    @Synchronized
    fun getInstance(context: Context): BluetoothThreadController? {
        if (sBluetoothManager == null) {
            sBluetoothManager = BluetoothThreadController(context)
        }
        return sBluetoothManager
    }

    fun connect(device: BluetoothDevice, context: Context?): Boolean {
        bluetoothManager.adapter.cancelDiscovery()
        var thread = mBluetoothThreadMap[device]
        return if (thread == null) {
            try {
                thread = BluetoothThread(device)
                thread.start()
                mBluetoothThreadMap[device] = thread
                true
            } catch (e: Exception) {
                false
            }
        } else {
            true
        }
    }

    fun disconnect(device: BluetoothDevice) {
        val thread = mBluetoothThreadMap[device]
        if (thread != null) {
            thread.quit()
            mBluetoothThreadMap.remove(device)

            //Remove from PrinterManager when disconnected
            //PrinterManager.getInstance().unregister(context, device.address)
        }
    }

    fun disconnectAll() {
        for (thread in mBluetoothThreadMap.values) {
            thread.quit()
        }
        mBluetoothThreadMap.clear()
    }

    fun getConnectedDeviceList(): List<BluetoothDevice?>? {
        return ArrayList(mBluetoothThreadMap.keys)
    }

    fun hasConnectedDevice(): Boolean {
        return mBluetoothThreadMap.isNotEmpty()
    }

    private fun preparePrint() {
        var count: Int
        do {
            synchronized(BluetoothManager::class.java) {
                count = mPrintingThreadCount
                if (count <= 0) {
                    mPrintingThreadCount = mBluetoothThreadMap.size
                }
            }
        } while (count > 0)
    }

    fun sendData(bytes: ByteArray, address: String, listener: OnPrintListener?) {
        preparePrint()
        for (thread in mBluetoothThreadMap.values) {
            /*if (thread.isInnerPrinter()) {
                val innerPrinter: IPrinter = PrinterManager.getInstance().getInnerPrinter()
                if (innerPrinter != null) {
                    synchronized(BluetoothManager::class.java) {
                        mPrintingThreadCount--
                        if (mPrintingThreadCount <= 0) {
                            listener?.onPrintFinished()
                        }
                    }
                    continue
                }
            }*/
            thread.setCallback(object : BluetoothThread.Callback {
                override fun onWriteFinished(device: BluetoothDevice?) {
                    thread.setCallback(null)
                    synchronized(BluetoothManager::class.java) {
                        mPrintingThreadCount--
                        if (mPrintingThreadCount <= 0) {
                            listener?.onPrintFinished()
                        }
                    }
                }

                override fun onWriteFail(device: BluetoothDevice?) {
                    thread.setCallback(null)
                    listener?.onPrintFail(device)
                    synchronized(BluetoothManager::class.java) {
                        mPrintingThreadCount--
                        if (mPrintingThreadCount <= 0) {
                            listener?.onPrintFinished()
                        }
                    }
                }
            })
            thread.write(bytes)
        }
    }

    /*fun printText(device: BluetoothDevice, text: String?, listener: OnPrintListener?) {
        if (TextUtils.isEmpty(text)) {
            return
        }
        val thread = mBluetoothThreadMap[device]
        if (thread != null) {
            if (listener != null) {
                thread.setCallback(object : BluetoothThread.Callback {
                    override fun onWriteFinished(device: BluetoothDevice?) {
                        thread.setCallback(null)
                        listener.onPrintFinished()
                    }

                    override fun onWriteFail(device: BluetoothDevice?) {
                        thread.setCallback(null)
                        listener.onPrintFail(device)
                    }
                })
            }
            thread.write(text) //print text
        }
    }*/

    interface OnPrintListener {
        fun onPrintFinished()
        fun onPrintFail(device: BluetoothDevice?)
    }
}
 */