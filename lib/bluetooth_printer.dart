import 'dart:async';
import 'dart:typed_data';

import 'package:flutter/services.dart';

import 'bluetooth_device.dart';
import 'bluetooth_status.dart';

enum PrintMode { api, bt }

class BluetoothPrinter {
  static const MethodChannel _channel = MethodChannel('bluetooth_printer');
  static const EventChannel _eventChannel = EventChannel('bluetooth_stream');

  Stream<BluetoothStream>? _bluetoothStream;
  StreamSubscription<Map<String, dynamic>>? _sub;

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

  Stream<BluetoothStream> get bluetoothStatusStream {
    _bluetoothStream ??= _eventChannel
        .receiveBroadcastStream()
        //     .timeout(const Duration(seconds: 10), onTimeout: (s) {
        //   print("timeout stream");
        //   s.close();
        // })
        .map((event) =>
            BluetoothStream.fromJson(Map<String, dynamic>.from(event)));
    // _sub ??= _bluetoothStream?.listen((event) { });
    return _bluetoothStream!;
  }

  Stream<BluetoothStream> status() async* {
    await for (final status in bluetoothStatusStream) {
      print('new status => $status');
      yield status;
    }
  }

  Stream<BluetoothState> state() async* {
    await for (final status in bluetoothStatusStream) {
      if (status.action == BluetoothAction.stateChanged) {
        print('BluetoothState => ${status.state}');
        yield status.state;
      }
    }
  }

  // Stream<BluetoothDevice> get scanResults {
  //   _bluetoothStream ??= _eventChannel.receiveBroadcastStream().timeout(
  //       const Duration(seconds: 10), onTimeout: (s) {
  //     print("timeout stream");
  //     s.close();
  //   }).map(
  //           (event) => BluetoothDevice.fromJson(Map<String, dynamic>.from(event)));
  //   // _sub ??= _bluetoothStream?.listen((event) { });
  //   return _bluetoothStream!;
  // }

  // close(){
  //   _sub?.cancel();
  // }

  // Stream<List<BluetoothDevice>> devices() async* {
  //   final list = <BluetoothDevice>{};
  //   await for (final device in scanResults) {
  //     print('new d ${device.name}');
  //     list.add(device);
  //     yield list.toList();
  //   }
  // }

  static Future<bool> connect(BluetoothDevice device) async {
    return await _channel.invokeMethod('connect', device.toJson()) as bool;
  }

/*  static Future<bool> printRawBytes(Uint8List bytes) async {
    return await _channel.invokeMethod('printRawBytes', {'bytes': bytes})
        as bool;
  }*/

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

  static Future<List<String>> getConnectedDevices() async {
    final result = await _channel.invokeMethod('getConnectedDevices');
    return List<String>.from(result);
  }

  static Future<List<BluetoothDevice>> getBondedDevices() async {
    final result = await _channel.invokeMethod('getBondedDevices');
    return List.from(result)
        .map((map) => BluetoothDevice.fromJson(Map<String, dynamic>.from(map)))
        .toList();
  }

  Future<bool> isConnected(String address) async {
    final res =
        await _channel.invokeMethod('isConnected', {'address': address});
    return res;
  }

  Future<bool> connectBluetooth({String? address}) async {
    final connectedAddresses = await BluetoothPrinter.getConnectedDevices();
    final isConnected = connectedAddresses.contains(address);
    if (!isConnected) {
      print('printer $address is not connected');
      final completer = Completer<bool>();
      late StreamSubscription<BluetoothStream> subscription;
      subscription = bluetoothStatusStream.listen((status) {
        if (status.action == BluetoothAction.connected) {
          if (status.device != null && status.device == address) {
            print('printer $address now Connected');
            subscription.cancel();
            completer.complete(true);
          }
        }
      });
      // for (final status in bluetoothStatusStream) {
      //   if (status.action == BluetoothAction.connected) {
      //     if (status.device != null && status.device == address) {
      //       completer.complete(true);
      //     }
      //   }
      // }
      // if (connectedAddresses != null) {
      //   disconnectBluetooth();
      // }
      _channel.invokeMethod('connectBluetooth', {'address': address});

/*    Future.timeout(Duration(seconds: 10), onTimeout: () {
      completer.completeError('timeout');
    });
    Future.wait<bool>[
    ].then((data) {
      print(data);
    }).timeout(Duration(seconds: 10));*/
      print('return completer');
      return completer.future;
    }
    print('already connected');
    return true;
  }

  static Future<bool> disconnectBluetooth(String address) async {
    final res = await _channel
        .invokeMethod('disconnectBluetooth', {'address': address});
    return res as bool;
  }

  static Future<void> sendData(
      Uint8List bytes, PrintMode printMode, String address) async {
    final map = <String, dynamic>{
      'bytes': bytes,
      'printMode': printMode.toString().replaceFirst('PrintMode.', ''),
      'address': address
    };
    await _channel.invokeMethod(
      'sendData',
      map,
    );
  }

/*  static Future<void> printText(String text, PrintMode printMode) async {
    final map = <String, dynamic>{
      'text': text,
      'printMode': printMode.toString().replaceFirst('PrintMode.', '')
    };
    await _channel.invokeMethod(
      'printText',
      map,
    );
  }*/

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
