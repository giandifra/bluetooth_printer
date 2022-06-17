import 'dart:async';
import 'dart:typed_data';

import 'package:flutter/services.dart';

import 'bluetooth_device.dart';

enum SunmiPrintMode { api, bt }

class BluetoothPrinter {
  static const MethodChannel _channel = MethodChannel('bluetooth_printer');
  static const EventChannel _eventChannel = EventChannel('bluetoothStream');

  Stream<BluetoothDevice>? _bluetoothStream;
  StreamSubscription<BluetoothDevice>? _sub;

  /// Constructs a singleton instance of [Connectivity].
  ///
  /// [Connectivity] is designed to work as a singleton.
  // When a second instance is created, the first instance will not be able to listen to the
  // EventChannel because it is overridden. Forcing the class to be a singleton class can prevent
  // misuse of creating a second instance from a programmer.
  factory BluetoothPrinter() {
    _singleton ??= BluetoothPrinter._();
    return _singleton!;
  }

  BluetoothPrinter._();

  static BluetoothPrinter? _singleton;

  Stream<BluetoothDevice> get scanResults {
    _bluetoothStream ??= _eventChannel.receiveBroadcastStream().timeout(
        const Duration(seconds: 10), onTimeout: (s) {
      print("timeout stream");
      s.close();
    }).map(
            (event) => BluetoothDevice.fromJson(Map<String, dynamic>.from(event)));
    // _sub ??= _bluetoothStream?.listen((event) { });
    return _bluetoothStream!;
  }

  // close(){
  //   _sub?.cancel();
  // }

  Stream<List<BluetoothDevice>> devices() async* {
    final list = <BluetoothDevice>{};
    await for (final device in scanResults) {
      print('new d ${device.name}');
      list.add(device);
      yield list.toList();
    }
  }

  static Future<bool> connect(BluetoothDevice device) async {
    return await _channel.invokeMethod('connect', device.toJson()) as bool;
  }

  static Future<bool> printRawBytes(Uint8List bytes) async {
    return await _channel.invokeMethod('printRawBytes', {'bytes': bytes})
    as bool;
  }

  static Future<String?> get platformVersion async {
    final String? version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<void> startService() async {
    await _channel.invokeMethod('startService');
  }

  static Future<void> stopService() async {
    await _channel.invokeMethod('stopService');
  }

  static Future<List<BluetoothDevice>> getBondedDevices() async {
    final result = await _channel.invokeMethod('getBondedDevices');
    return List.from(result)
        .map((map) => BluetoothDevice.fromJson(Map<String, dynamic>.from(map)))
        .toList();
  }

  static Future<bool> connectBluetooth({String? address}) async {
    final result =
    await _channel.invokeMethod('connectBluetooth', {'address': address});
    return result;
  }

  static Future<void> disconnectBluetooth() async {
    await _channel.invokeMethod('disconnectBluetooth');
  }

  static Future<void> sendData(Uint8List bytes, SunmiPrintMode printMode) async {
    final map = <String, dynamic>{'bytes': bytes, 'printMode': printMode.toString().replaceFirst('SunmiPrintMode.', '')};
    await _channel.invokeMethod(
      'sendData',
      map,
    );
  }

  static Future<void> printText(String text, SunmiPrintMode printMode) async {
    final map = <String, dynamic>{
      'text': text,
      'printMode': printMode.toString().replaceFirst('SunmiPrintMode.', '')
    };
    await _channel.invokeMethod(
      'printText',
      map,
    );
  }

  static Future<void> setCharset(String charset) async {
    await _channel.invokeMethod(
      'setCharset',
      charset,
    );
  }

  static Future<List<String>> getAvailableCharsets() async {
    final l = await _channel.invokeMethod('getAvailableCharsets') as List;
    return List<String>.from(l);
  }
}
