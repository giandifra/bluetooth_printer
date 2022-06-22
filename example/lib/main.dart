import 'dart:typed_data';
import 'package:bluetooth_printer/bluetooth_device.dart';
import 'package:bluetooth_printer/bluetooth_status.dart';
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
  var connectedDevices = <String>[];

  int delay = 2;
  int copies = 1;

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

              StreamBuilder<BluetoothState>(
                  stream: BluetoothPrinter().state(),
                  builder: (c, snap) {
                    if (!snap.hasData) {
                      return const SizedBox();
                    }
                    final state = snap.data!;
                    return Container(
                      color: state == BluetoothState.on
                          ? Colors.green
                          : Colors.red,
                      padding: const EdgeInsets.all(16),
                      child: Column(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          Text('BLUETOOTH: ${enumToString(state)}'),
                        ],
                      ),
                    );
                  }),

              StreamBuilder<BluetoothStream>(
                  stream: BluetoothPrinter().status(),
                  builder: (c, snap) {
                    if (!snap.hasData) {
                      return const SizedBox();
                    }
                    final action = snap.data!.action;
                    final state = snap.data!.state;
                    final connectionState = snap.data!.connectionState;
                    final address = snap.data!.device;
                    return Container(
                      child: Column(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          Text('action: $action'),
                          Text('state: $state'),
                          Text('connectionState: $connectionState'),
                          Text('mac address: $address'),
                          const SizedBox(height: 8),
                        ],
                      ),
                    );
                  }),
              Container(
                  color: Colors.grey, child: const Text('Bluetooth Mode')),
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
                            showToast(ex.toString(),
                                position: ToastPosition.bottom);
                          }
                        }
                      : null,
                  child: const Text('Scan and select device to connect')),
              ElevatedButton(
                  onPressed: () async {
                    final res = await BluetoothPrinter.getConnectedDevices();
                    setState(() {
                      connectedDevices = res;
                    });
                  },
                  child: const Text('get connected devices')),
              if (connectedDevices.isNotEmpty) ...[
                const Text('Connected device'),
                ...connectedDevices.map(
                  (e) => ListTile(
                    title: Text(e),
                    trailing: IconButton(
                        icon: const Icon(Icons.delete),
                        color: Colors.red,
                        onPressed: () async {
                          disconnectDevice(e);
                        }),
                  ),
                )
              ],
              ElevatedButton(
                  onPressed: permissionIsGranted && selectedDevice != null
                      ? () async {
                          final r = await BluetoothPrinter().connectBluetooth(
                              address: selectedDevice?.address);
                          print('bluetooth connection : $r');
                          final res =
                              await BluetoothPrinter.getConnectedDevices();
                          setState(() {
                            connectedDevices = res;
                            connected = r;
                          });
                        }
                      : null,
                  child: const Text('connectBluetooth')),
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
                  onPressed: permissionIsGranted
                      ? () async {
                          if (selectedDevice == null) return;
                          try {
                            final bytes = await testReceipt();
                            final connected = await BluetoothPrinter()
                                .connectBluetooth(
                                    address: selectedDevice!.address);
                            if (!connected) return;
                            BluetoothPrinter.sendData(
                              bytes,
                              PrintMode.bt,
                              selectedDevice!.address,
                            ).then((value) async {
                              await Future.delayed(Duration(seconds: delay));
                              disconnectDevice(selectedDevice!.address);
                            });
                          } catch (ex) {
                            showToast(ex.toString(),
                                position: ToastPosition.bottom);
                          }
                        }
                      : null,
                  child: const Text('Connect and print')),
              ElevatedButton(
                  onPressed: permissionIsGranted && connectedDevices.isNotEmpty
                      ? () async {
                          try {
                            final bytes = await testReceipt();
                            connectedDevices.forEach((address) async {
                              // for (int i = 0; i < 3; i++) {
                              BluetoothPrinter.sendData(
                                bytes,
                                PrintMode.bt,
                                address,
                              ).then((value) async {
                                print('data sent');
                                await Future.delayed(Duration(seconds: delay));
                                disconnectDevice(address);
                              });
                              // await Future.delayed(
                              //     const Duration(milliseconds: 1000));
                              // await disconnectDevice(address);
                              print(
                                  '------------------------------------------');
                              // }
                            });
                          } catch (ex) {
                            showToast(ex.toString(),
                                position: ToastPosition.bottom);
                          }
                        }
                      : null,
                  child: const Text('print text with BT')),
              Row(
                children: [
                  IconButton(
                      icon: Icon(Icons.remove),
                      color: Colors.red,
                      onPressed: () {
                        if (delay == 1) return;
                        setState(() {
                          delay--;
                        });
                      }),
                  Text('delay: ${delay}'),
                  IconButton(
                      icon: Icon(Icons.add),
                      color: Colors.blue,
                      onPressed: () {
                        setState(() {
                          delay++;
                        });
                      }),
                ],
              ),
              TextFormField(
                keyboardType: TextInputType.number,
                decoration: InputDecoration(hintText: 'numero di copie'),
                onChanged: (v) {
                  final n = int.tryParse(v);
                  if (n == null) return;
                  setState(() {
                    copies = n;
                  });
                },
              )
              /*ElevatedButton(
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
                  child: const Text('disconnectBluetooth')),*/
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

  disconnectDevice(String address) async {
    final res = await BluetoothPrinter.disconnectBluetooth(address);
    if (res) {
      final devices = await BluetoothPrinter.getConnectedDevices();
      setState(() {
        connectedDevices = devices;
      });
    }
  }

  Future<Uint8List> testReceipt() async {
    try {
      final testReceiptEndpoint =
          // "https://b1a8-213-86-221-106.ngrok.io/v1/Receipt/testReceipt";
          "https://api.biz.test.chivado.com/v1/Receipt/testReceipt/$copies";

      final dio = Dio();
      final res = await dio.get(testReceiptEndpoint,
          options: Options(responseType: ResponseType.bytes));
      if (res.statusCode == 200) {
        print(res.data);
        return Uint8List.fromList(res.data);
      }
      throw Exception();
    } catch (ex) {
      rethrow;
    }
  }
}

/*class BluetoothDeviceScanResults extends StatefulWidget {
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
}*/
