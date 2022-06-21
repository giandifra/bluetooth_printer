package dev.gdifrancesco.bluetooth_printer

import android.app.Activity
import android.content.Context
import androidx.annotation.NonNull
import com.chivado.sunmi.BluetoothStreamHandler

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodChannel

/** BluetoothPrinterPlugin */
class BluetoothPrinterPlugin : FlutterPlugin, ActivityAware {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private lateinit var methodChannel: MethodChannel
  private lateinit var eventChannel: EventChannel
  private lateinit var context: Context
  private lateinit var activity: Activity
  private val bluetoothEventChannel = "bluetooth_stream"
  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    context = flutterPluginBinding.applicationContext

    methodChannel = MethodChannel(flutterPluginBinding.binaryMessenger, "bluetooth_printer")
    methodChannel.setMethodCallHandler(PrinterMethodChannelHandler(context));

    eventChannel = EventChannel(flutterPluginBinding.binaryMessenger, bluetoothEventChannel)
    eventChannel.setStreamHandler(BluetoothStreamHandler(context));

  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    methodChannel.setMethodCallHandler(null)
    eventChannel.setStreamHandler(null)
  }


  override fun onAttachedToActivity(binding: ActivityPluginBinding) {
    activity = binding.activity;
  }

  override fun onDetachedFromActivityForConfigChanges() {
    TODO("Not yet implemented")
  }

  override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
    TODO("Not yet implemented")
  }

  override fun onDetachedFromActivity() {
    TODO("Not yet implemented")
  }
}
