package dev.gdifrancesco.bluetooth_printer

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import dev.gdifrancesco.bluetooth_printer.utils.BluetoothUtil
import dev.gdifrancesco.bluetooth_printer.utils.ESCUtil
import dev.gdifrancesco.bluetooth_printer.utils.SunmiPrintHelper
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel

class PrinterMethodChannelHandler(private val context: Context) : MethodChannel.MethodCallHandler {


    private val TAG = "PrinterMethodHandler"
    private val btUtil: BluetoothUtil = BluetoothUtil()

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            "getPlatformVersion" -> result.success("Android ${android.os.Build.VERSION.RELEASE}")
            "getBondedDevices" -> getBondedDevices(context, result)
            "connectBluetooth" -> connectBluetooth(
                context,
                call.arguments as HashMap<String, Any>,
                result
            )
            "disconnectBluetooth" -> disconnectBluetooth(context)
            "printRawBytes" -> printRawBytes(call.arguments as HashMap<String, Any>, result)
            "sendData" -> sendData(context, call.arguments as HashMap<String, Any>, result)
            "printText" -> printText(call.arguments as HashMap<String, Any>, result)
            "startService" -> startService(context)
            "stopService" -> stopService(context)
            "connect" -> connect(call.arguments as HashMap<String, Any>, result)
            "getAvailableCharsets" -> result.success(sunmiCharsets)
            "setCharset" -> setCharset(call.arguments as String)
            else -> result.notImplemented()
        }
    }

    private val mainHandler: Handler = Handler(Looper.getMainLooper())
    private var charsetIndex = 0;

    private val sunmiCharsets = arrayListOf<String>(
        "CP437",
        "CP850",
        "CP860",
        "CP863",
        "CP865",
        "CP857",
        "CP737",
        "CP928",
        "Windows-1252",
        "CP866",
        "CP852",
        "CP858",
        "CP874",
        "Windows-775",
        "CP855",
        "CP862",
        "CP864",
        "GB18030",
        "BIG5",
        "KSC5601",
        "utf-8"
    )

    private fun printRawBytes(arguments: Map<String, Any>, result: MethodChannel.Result) {
        try {
            val bytes = arguments["bytes"] as ByteArray
            btUtil.printRawBytes(bytes);
            result.success(true)
        } catch (ex: Exception) {
            result.error(ex.message, "", "")
        }
    }

    private fun setCharset(charset: String) {
        val index = sunmiCharsets.indexOf(charset)
        if (index != -1) {
            charsetIndex = index;
        }
    }

    private fun sendData(
        context: Context,
        arguments: Map<String, Any>,
        result: MethodChannel.Result
    ) {

        Log.i("SunmiPlugi", "printText");
        val printMode = arguments["printMode"] as String
        val bytes = arguments["bytes"] as ByteArray
        if (printMode == "bt") {
            try {
                BluetoothUtil.sendData(bytes)
                result.success(true)
            } catch (ex: Exception) {
                Log.e("sunmi plugin", ex.toString())
                result.error("121", ex.toString(), ex)
            }
        } else {
            SunmiPrintHelper.getInstance().sendRawData(bytes);
        }
    }

    private fun printText(arguments: Map<String, Any>, result: MethodChannel.Result) {
        Log.i("SunmiPlugi", "printText");
        val printMode = arguments["printMode"] as String
        val text = arguments["text"] as String
        val charset = charset(sunmiCharsets[charsetIndex])
        val bytes = text.toByteArray(charset)
        if (printMode == "bt") {
            try {
                Log.i("sunmi", "charset use ${sunmiCharsets[charsetIndex]}")
                if (charsetIndex < 17) {
                    BluetoothUtil.sendData(ESCUtil.singleByte())
                    BluetoothUtil.sendData(ESCUtil.setCodeSystemSingle(codeParse(charsetIndex)))
                } else {
                    BluetoothUtil.sendData(ESCUtil.singleByteOff())
                    BluetoothUtil.sendData(ESCUtil.setCodeSystem(codeParse(charsetIndex)))
                }

                BluetoothUtil.sendData(sunmiCharsets[charsetIndex].toByteArray(charset))
                BluetoothUtil.sendData(ESCUtil.nextLine(1))
                BluetoothUtil.sendData(bytes)
                BluetoothUtil.sendData(ESCUtil.nextLine(3))

            } catch (ex: Exception) {
                Log.e("sunmi plugin", ex.toString())
                result.error("121", ex.toString(), ex)
            }
        } else {
            SunmiPrintHelper.getInstance().sendRawData(bytes);
        }
    }

    private fun codeParse(value: Int): Byte {
        var res: Byte = 0x00
        when (value) {
            0 -> res = 0x00
            1, 2, 3, 4 -> res = (value + 1).toByte()
            5, 6, 7, 8, 9, 10, 11 -> res = (value + 8).toByte()
            12 -> res = 21
            13 -> res = 33
            14 -> res = 34
            15 -> res = 36
            16 -> res = 37
            17, 18, 19 -> res = (value - 17).toByte()
            20 -> res = 0xff.toByte()
            else -> {}
        }
        return res
    }


    private fun getBondedDevices(context: Context, result: MethodChannel.Result) {
        Log.i(TAG, "getBondedDevices");

        val devices: Set<BluetoothDevice> = BluetoothUtil.getBTAdapter().bondedDevices
        result.success(devices.map { it ->
            mapOf(
                "address" to it.address,
                "name" to it.name,
                "type" to it.type
            )
        }.toList<Map<String, Any>>())

    }

    private fun connectBluetooth(
        context: Context,
        arguments: Map<String, Any>,
        result: MethodChannel.Result
    ) {
        Log.i(TAG, "connectBluetooth");
        val address = arguments["address"] as String
        BluetoothUtil.isBlueToothPrinter = BluetoothUtil.connectBlueTooth(context, address)
        result.success(BluetoothUtil.isBlueToothPrinter)
    }

    private fun connect(arguments: Map<String, Any>, result: MethodChannel.Result) {
        Log.i(TAG, "connect");
        val address = arguments["address"] as String
        //BluetoothUtil.isBlueToothPrinter = BluetoothUtil.connectBlueTooth(context, address)

        scanDevice(address, result)
        //result.success(BluetoothUtil.isBlueToothPrinter)
    }

    private fun disconnectBluetooth(context: Context) {
        Log.i(TAG, "disconnectBluetooth");
        BluetoothUtil.disconnectBlueTooth(context)
    }

    private fun startService(context: Context) {
        Log.i(TAG, "startService");
        SunmiPrintHelper.getInstance().initSunmiPrinterService(context)
    }

    private fun stopService(context: Context) {
        Log.i(TAG, "stopService");
        SunmiPrintHelper.getInstance().deInitSunmiPrinterService(context)
    }

    @Throws(IllegalStateException::class)
    private fun scanDevice(address: String, result: MethodChannel.Result) {
        Log.i(TAG, "scanDevice: $address")
        val scanner = BluetoothUtil.getBTAdapter().bluetoothLeScanner
            ?: throw IllegalStateException("getBluetoothLeScanner() is null. Is the Adapter on?")

        // 0:lowPower 1:balanced 2:lowLatency -1:opportunistic
        val settings =
            ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()
        val filters = arrayListOf<ScanFilter>()
        val addressFilter = ScanFilter.Builder()
        addressFilter.setDeviceAddress(address)
        filters.add(addressFilter.build())

        val scanCallback = ScanCallbackHandler(address)
        scanCallback.onDetected = { device ->
            Log.i(TAG, "device detected")
            scanner.stopScan(scanCallback)
            val connected = btUtil.connect(device.address)
            val runnable = Runnable { result.success(connected) }
            mainHandler.post(runnable)

        }

        scanner.startScan(filters, settings, scanCallback)
    }
}

typealias DeviceDetected = (BluetoothDevice) -> Unit

class ScanCallbackHandler(private val address: String) :
    ScanCallback() {

    var onDetected: DeviceDetected? = null

    override fun onScanResult(callbackType: Int, result: ScanResult) {
        Log.i("ScanCallbackHandler", "onScanResult")
        val device = result.device
        if (device != null && device.name != null) {
            if (address == device.address) {
                Log.i("ScanCallbackHandler", "device finded")
                onDetected?.let { it(device) }
            }
        }
    }
}