package dev.gdifrancesco.bluetooth_printer.utils;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

/**
 * Simple package for connecting a sunmi printer via Bluetooth
 */
public class BluetoothUtil {

    private int id = 0;
    private ThreadPool threadPool;

    private static final String TAG = "BluetoothUtil";
    private static final UUID PRINTER_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final String Innerprinter_Address = "00:11:22:33:44:55";
    public static boolean isBlueToothPrinter = false;
    private static BluetoothSocket bluetoothSocket;

    public static BluetoothAdapter getBTAdapter() {
        return BluetoothAdapter.getDefaultAdapter();
    }

    private static BluetoothDevice getDevice(BluetoothAdapter bluetoothAdapter, String deviceAddress) {
        BluetoothDevice innerprinter_device = null;
        Set<BluetoothDevice> devices = bluetoothAdapter.getBondedDevices();
        Log.i(TAG, "device Address passed is: " + deviceAddress);
        for (BluetoothDevice device : devices) {
            Log.i(TAG, "device: " + device.getName() + " | " + device.getAddress());
            if (deviceAddress != null) {
                if (device.getAddress().equals(deviceAddress)) {
                    innerprinter_device = device;
                    break;
                }
            } else {
                if (device.getAddress().equals(Innerprinter_Address)) {
                    innerprinter_device = device;
                    break;
                }
            }
        }
        return innerprinter_device;
    }

    private static BluetoothSocket getSocket(BluetoothDevice device) throws IOException {
        BluetoothSocket socket;
        socket = device.createRfcommSocketToServiceRecord(PRINTER_UUID);
        socket.connect();
        return socket;
    }

    /**
     * connect bluetooth
     */
    public static boolean connectBlueTooth(Context context, String deviceAddress) {
        if (bluetoothSocket == null) {
            if (getBTAdapter() == null) {
                //Toast.makeText(context,  R.string.toast_3, Toast.LENGTH_SHORT).show();

                Log.i(TAG, "bluetooth adapter is null");
                return false;
            }
            if (!getBTAdapter().isEnabled()) {
                //Toast.makeText(context, R.string.toast_4, Toast.LENGTH_SHORT).show();
                Log.i(TAG, "bluetooth is not enabled");
                return false;
            }
            BluetoothDevice device;
            if ((device = getDevice(getBTAdapter(), deviceAddress)) == null) {
                //Toast.makeText(context, R.string.toast_5, Toast.LENGTH_SHORT).show();
                Log.i(TAG, "device is null");
                return false;
            }

            try {
                bluetoothSocket = getSocket(device);
            } catch (IOException e) {
                //Toast.makeText(context, R.string.toast_6, Toast.LENGTH_SHORT).show();
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    public static boolean connect(BluetoothDevice device) {
        try {
            bluetoothSocket = getSocket(device);
            return true;
        } catch (IOException e) {
            //Toast.makeText(context, R.string.toast_6, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            return false;
        }
    }

    public boolean connect(String address) {
        Log.i(TAG, "connecting to address " + address);
        disconnect();
        try {
            final int id = 0;
            new DeviceConnFactoryManager.Build()
                    .setId(id)
                    .setConnMethod(DeviceConnFactoryManager.CONN_METHOD.BLUETOOTH)
                    .setMacAddress(address)
                    .build();

            threadPool = ThreadPool.getInstantiation();
            threadPool.addSerialTask((Runnable) () -> DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].openPort());
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private boolean disconnect() {
        Log.i(TAG, "disconnect");
        if (DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id] != null && DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].mPort != null) {
            DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].closePort();
        }
        return true;
    }

    /**
     * disconnect bluethooth
     */
    public static void disconnectBlueTooth(Context context) {
        if (bluetoothSocket != null) {
            try {
                OutputStream out = bluetoothSocket.getOutputStream();
                out.close();
                bluetoothSocket.close();
                bluetoothSocket = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * send esc cmd
     */
    public static void sendData(byte[] bytes) {
        if (bluetoothSocket != null) {
            OutputStream out = null;
            try {
                out = bluetoothSocket.getOutputStream();
                out.write(bytes, 0, bytes.length);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            //TODO handle disconnect event
        }
    }


    public void printRawBytes(byte[] bytes) throws Exception {
        boolean isOpenPort = DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].getConnState();
        if (DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id] == null ||
                !isOpenPort) {

            Log.i("BluetoothPrint", "not connect, state not right");
            throw new Exception("not connect => state not right");
        }

        if (bytes != null) {
            threadPool = ThreadPool.getInstantiation();
            threadPool.addSerialTask(() -> DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].sendByteDataImmediately(bytes));
        } else {
            throw new Exception("please add data");
        }
    }
}
