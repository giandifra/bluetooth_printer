package dev.gdifrancesco.bluetooth_printer

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import android.util.Log
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.lang.Exception
import java.util.*

/// Universal Bluetooth serial connection class (for Java)
internal abstract class BluetoothConnection(protected var bluetoothAdapter: BluetoothAdapter) {
    var connectionThread: ConnectionThread? = null
    val isConnected: Boolean
        get() = connectionThread != null && !connectionThread!!.requestedClosing

    var isSocketConnected = false;

    // @TODO . `connect` could be done perfored on the other thread
    // @TODO . `connect` parameter: timeout
    // @TODO . `connect` other methods than `createRfcommSocketToServiceRecord`, including hidden one raw `createRfcommSocket` (on channel).
    // @TODO ? how about turning it into factoried?
    /// Connects to given device by hardware address
    /// Connects to given device by hardware address (default UUID used)
    @JvmOverloads
    @Throws(IOException::class)
    fun connect(address: String, uuid: UUID? = DEFAULT_UUID) {
        if (isConnected) {
            throw IOException("already connected")
        }
        val device = bluetoothAdapter.getRemoteDevice(address)
            ?: throw IOException("device not found")
        val socket = device.createRfcommSocketToServiceRecord(uuid)
            ?: throw IOException("socket connection not established") // @TODO . introduce ConnectionMethod

        // Cancel discovery, even though we didn't start it
        bluetoothAdapter.cancelDiscovery()
        socket.connect()
        isSocketConnected = true;
        connectionThread = ConnectionThread(socket)
        connectionThread!!.start()
        Log.d("BluetoothConnection", "connect complete")
    }

    /// Disconnects current session (ignore if not connected)
    fun disconnect() {
        if (isConnected) {
            connectionThread!!.cancel()
            connectionThread = null
        }
    }

    /// Writes to connected remote device
    @Throws(IOException::class)
    fun write(data: ByteArray?) {
        val connectionIsNotNull = connectionThread != null
        val requestedClosing =  connectionThread?.requestedClosing
        Log.d("BluetoothConnection", "connectionIsNotNull $connectionIsNotNull")
        Log.d("BluetoothConnection", "requestedClosing $requestedClosing")
        if (!isConnected) {
            throw IOException("not connected")
        }
        connectionThread!!.write(data)

    }

    /// Callback for reading data.
    protected abstract fun onRead(data: ByteArray?)

    /// Callback for disconnection.
    protected abstract fun onDisconnected(byRemote: Boolean)

    /// Thread to handle connection I/O
    inner class ConnectionThread internal constructor(socket: BluetoothSocket) : Thread() {
        private val socket: BluetoothSocket?
        private val input: InputStream?
        private val output: OutputStream?
        var requestedClosing = false

        /// Thread main code
        override fun run() {
            val buffer = ByteArray(1024)
            var bytes: Int
            while (!requestedClosing) {
                try {
                    bytes = input!!.read(buffer)
                    onRead(buffer.copyOf(bytes))
                } catch (e: IOException) {
                    // `input.read` throws when closed by remote device
                    break
                }
            }

            // Make sure output stream is closed
            if (output != null) {
                try {
                    output.close()
                } catch (e: Exception) {
                    Log.i("ConnectionThread", "Error to close output stream")
                }
            }

            // Make sure input stream is closed
            if (input != null) {
                try {
                    input.close()
                } catch (e: Exception) {
                    Log.i("ConnectionThread", "Error to close input stream")
                }
            }

            // Callback on disconnected, with information which side is closing
            onDisconnected(!requestedClosing)

            // Just prevent unnecessary `canceling
            requestedClosing = true
        }

        /// Writes to output stream
        fun write(bytes: ByteArray?) {
            Log.d("ConnectionThread", "write")
            try {
                output!!.write(bytes)
                Log.d("ConnectionThread", "end write")
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        /// Stops the thread, disconnects
        fun cancel() {
            if (requestedClosing) {
                return
            }
            requestedClosing = true

            // Flush output buffers befoce closing
            try {
                output!!.flush()
            } catch (e: Exception) {
                Log.i("ConnectionThread", "Error flush")
            }

            // Close the connection socket
            if (socket != null) {
                try {
                    // Might be useful (see https://stackoverflow.com/a/22769260/4880243)
                    sleep(111)
                    socket.close()
                    isSocketConnected = false;
                } catch (e: Exception) {
                    Log.i("ConnectionThread", "Error flush")
                }
            }
        }

        init {
            this.socket = socket
            var tmpIn: InputStream? = null
            var tmpOut: OutputStream? = null
            try {
                tmpIn = socket.inputStream
                tmpOut = socket.outputStream
            } catch (e: IOException) {
                e.printStackTrace()
            }
            input = tmpIn
            output = tmpOut
        }
    }

    companion object {
        protected val DEFAULT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    }
} /// Helper wrapper class for `BluetoothConnection`

internal class BluetoothConnectionWrapper(private val id: String, adapter: BluetoothAdapter) :
    BluetoothConnection(adapter) {
    private val TAG = "BluetoothConnectionWra"
    private val self = this
    override fun onRead(buffer: ByteArray?) {
        Log.i(TAG, Arrays.toString(buffer))
        /*activity.runOnUiThread(() -> {
            if (readSink != null) {
                readSink.success(buffer);
            }
        });*/
    }

    override fun onDisconnected(byRemote: Boolean) {
        Log.i(TAG, "onDisconnected: $byRemote")
        /*activity.runOnUiThread(() -> {
            if (byRemote) {
                Log.d(TAG, "onDisconnected by remote (id: " + id + ")");
                if (readSink != null) {
                    readSink.endOfStream();
                    readSink = null;
                }
            } else {
                Log.d(TAG, "onDisconnected by local (id: " + id + ")");
            }
        });*/
    }
}