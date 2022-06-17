import 'dart:typed_data';
import 'package:bluetooth_printer/bluetooth_device.dart';
import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import 'dart:async';
import 'package:flutter/services.dart';
import 'package:oktoast/oktoast.dart';
import 'package:permission_handler/permission_handler.dart';
import 'package:bluetooth_printer/bluetooth_printer.dart';

void main() {
  runApp(const MaterialApp(home: HomeScreen()));
}

class HomeScreen extends StatefulWidget {
  const HomeScreen({Key? key}) : super(key: key);

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  String _platformVersion = 'Unknown';

  @override
  void initState() {
    super.initState();
    initPlatformState();
    readCharsets();
  }

  String chars = "€\$£ àèìòù áéíóú";

  Future<bool> checkPermission() async {
    var status = await Permission.bluetooth.request();
    var bluetoothConnectStatus = await Permission.bluetoothConnect.request();
    var bluetoothScanStatus = await Permission.bluetoothScan.request();
    var locationRequestStatus = await Permission.location.request();
    if (status.isGranted &&
        bluetoothConnectStatus.isGranted &&
        bluetoothScanStatus.isGranted &&
        locationRequestStatus.isGranted) {
      return true;
    }
    return false;
  }

  var charSets = <String>[];
  BluetoothDevice? selectedDevice;

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> readCharsets() async {
    final c = await BluetoothPrinter.getAvailableCharsets();
    setState(() {
      charSets = c;
    });
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    String platformVersion;
    // Platform messages may fail, so we use a try/catch PlatformException.
    // We also handle the message potentially returning null.
    try {
      platformVersion =
          await BluetoothPrinter.platformVersion ?? 'Unknown platform version';
    } on PlatformException {
      platformVersion = 'Failed to get platform version.';
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      _platformVersion = platformVersion;
    });
  }

  bool permissionIsGranted = false;
  String? selectedCharSet;
  bool connected = false;

  @override
  Widget build(BuildContext context) {
    return OKToast(
      child: MaterialApp(
        home: Scaffold(
          appBar: AppBar(
            title: const Text('Plugin example app'),
          ),
          body: ListView(
            padding: const EdgeInsets.all(16),
            children: [
              Text('Running on: $_platformVersion\n'),
              /* DropdownButton<String>(
                  value: selectedCharSet,
                  items: charSets
                      .map(
                        (e) => DropdownMenuItem<String>(
                          value: e,
                          child: Text(e),
                        ),
                      )
                      .toList(),
                  onChanged: (v) {
                    if (v != null) {
                      setState(() {
                        selectedCharSet = v;
                      });
                      BluetoothPrinter.setCharset(selectedCharSet!);
                    }
                  }),*/
              Container(color: Colors.grey, child: const Text('Bluetooth Mode')),
              ElevatedButton(
                  onPressed: () async {
                    final res = await checkPermission();
                    setState(() {
                      permissionIsGranted = res;
                    });
                  },
                  child: const Text('check bluetooth permission')),
              ElevatedButton(
                  onPressed: permissionIsGranted
                      ? () async {
                    print('getBondedDevices');
                    try {
                      final r = await BluetoothPrinter.getBondedDevices();
                      final device = await Navigator.push(context,
                          MaterialPageRoute(
                              builder: (BuildContext context) {
                                return Scaffold(
                                  body: ListView.builder(
                                      itemCount: r.length,
                                      itemBuilder: (c, i) {
                                        final item = r[i];
                                        return ListTile(
                                          title: Text(item.name),
                                          subtitle: Text(item.address),
                                          onTap: () {
                                            Navigator.of(context).pop(item);
                                          },
                                        );
                                      }),
                                );
                              }));
                      if (device != null) {
                        selectedDevice = device;
                        setState(() {});
                      }
                    } catch (ex) {
                      showToast(ex.toString(),position: ToastPosition.bottom);
                    }
                  }
                      : null,
                  child: const Text('Scan bonded devices')),
              if (selectedDevice != null)
                Container(
                  child: Column(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      Text(selectedDevice!.name),
                      Text(selectedDevice!.address),
                      Text(selectedDevice!.type.toString()),
                    ],
                  ),
                ),
              ElevatedButton(
                  onPressed: permissionIsGranted && selectedDevice != null
                      ? () async {
                    final r = await BluetoothPrinter.connectBluetooth(
                        address: selectedDevice?.address);
                    print('bluetooth connection : $r');
                    setState(() {
                      connected = r;
                    });
                  }
                      : null,
                  child: const Text('connectBluetooth')),
              ElevatedButton(
                  onPressed: permissionIsGranted && connected
                      ? () async {
                    final bytes = await testReceipt();
                    BluetoothPrinter.sendData(
                      bytes,
                      SunmiPrintMode.bt,
                    );
                  }
                      : null,
                  child: const Text('print text with BT')),
              ElevatedButton(
                  onPressed:
                  permissionIsGranted && selectedDevice != null && connected
                      ? () async {
                    BluetoothPrinter.disconnectBluetooth();
                    setState(() {
                      selectedDevice = null;
                      connected = false;
                    });
                  }
                      : null,
                  child: const Text('disconnectBluetooth')),
              // if (permissionIsGranted)
              //   ElevatedButton(
              //       onPressed: () async {
              //         final device = await Navigator.of(context).push(
              //             MaterialPageRoute(
              //                 builder: (c) =>
              //                     const BluetoothDeviceScanResults()));
              //         if (device == null) return;
              //         final result = await BluetoothPrinter.connect(device);
              //         if (result) {
              //           final text = 'janmark';
              //           BluetoothPrinter.printRawBytes(
              //               Uint8List.fromList(utf8.encode(text)));
              //         }
              //       },
              //       child: Text('Scan & print')),
              // ElevatedButton(
              //     onPressed: permissionIsGranted
              //         ? () async {
              //             final bytes = await testReceipt();
              //             print('uint8list =\n$bytes');
              //             BluetoothPrinter.sendData(
              //               bytes,
              //               SunmiPrintMode.bt,
              //             );
              //           }
              //         : null,
              //     child: Text('send Data with BT')),
              // Container(color: Colors.grey, child: Text('Sunmi BT Mode')),
              // ElevatedButton(
              //     onPressed: permissionIsGranted
              //         ? () async {
              //             BluetoothPrinter.printText(
              //               chars,
              //               SunmiPrintMode.bt,
              //             );
              //           }
              //         : null,
              //     child: Text('print text with BT')),
              // Container(color: Colors.grey, child: Text('Sunmi Service Mode')),
              // ElevatedButton(
              //     onPressed: () {
              //       BluetoothPrinter.startService();
              //     },
              //     child: Text('startService')),
              // ElevatedButton(
              //     onPressed: () {
              //       BluetoothPrinter.stopService();
              //     },
              //     child: Text('stopService')),
              // ElevatedButton(
              //     onPressed: () async {
              //       final bytes = await testReceipt();
              //       BluetoothPrinter.sendData(
              //         bytes,
              //         SunmiPrintMode.api,
              //       );
              //     },
              //     child: Text('send Data')),
            ],
          ),
        ),
      ),
    );
  }

  testReceipt() async {
    try {
      const testReceiptEndpoint =
      // "https://b1a8-213-86-221-106.ngrok.io/v1/Receipt/testReceipt";
          "https://api.biz.test.chivado.com/v1/Receipt/testReceipt";

      final dio = Dio();
      final res = await dio.get(testReceiptEndpoint,
          options: Options(responseType: ResponseType.bytes));
      if (res.statusCode == 200) {
        print(res.data);
        // return res.data;
        return Uint8List.fromList(res.data);
      }

      throw Exception();
    } catch (ex) {
      return Uint8List(0);
    }
  }
}

class BluetoothDeviceScanResults extends StatefulWidget {
  const BluetoothDeviceScanResults({Key? key}) : super(key: key);

  @override
  State<BluetoothDeviceScanResults> createState() =>
      _BluetoothDeviceScanResultsState();
}

class _BluetoothDeviceScanResultsState
    extends State<BluetoothDeviceScanResults> {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: StreamBuilder(
        stream: BluetoothPrinter().devices(),
        builder: (BuildContext context,
            AsyncSnapshot<List<BluetoothDevice>> snapshot) {
          if (snapshot.hasError) {
            return Center(
              child: Text(snapshot.error.toString()),
            );
          }
          if (snapshot.hasData) {
            return ListView.builder(
              itemCount: snapshot.data!.length,
              itemBuilder: (c, index) => ListTile(
                onTap: () async {
                  Navigator.of(context).pop(snapshot.data![index]);
                },
                title: Text(snapshot.data![index].name),
                subtitle: Text(snapshot.data![index].address),
              ),
            );
          }
          return const Center(
            child: CircularProgressIndicator(),
          );
        },
      ),
    );
  }
}
