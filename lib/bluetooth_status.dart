// import 'package:freezed_annotation/freezed_annotation.dart';
//
// part 'bluetooth_status.freezed.dart';
// part 'bluetooth_status.g.dart';
//
// @freezed
// class BluetoothStatus with _$BluetoothStatus {
//   const factory BluetoothStatus({
//     required int state,
//     String? address,
//     String? deviceAddress,
//   }) = _BluetoothStatus;
//
//   factory BluetoothStatus.fromJson(Map<String, Object?> json)
//   => _$BluetoothDeviceStatus(json);
// }

enum BluetoothAction {
  stateChanged,
  connectionStateChanged,
  connected,
  disconnectRequest,
  disconnected,
}

enum BluetoothState {
  off, //10
  turningOn, //11
  on, //12
  turningOff, //13
  unknown,
}

enum BluetoothConnectionState {
  disconnected, //0
  connecting, //1
  connected, //2
  disconnecting, //3
  unknown,
}

class BluetoothStream {
  final BluetoothAction action;
  final BluetoothConnectionState? connectionState;
  final BluetoothState state;
  final String? device;

  BluetoothStream({
    required this.action,
    this.connectionState,
    this.state = BluetoothState.unknown,
    this.device,
  });

  factory BluetoothStream.fromJson(Map<String, dynamic> json) {
    print(json);
    final action = enumFromString(json['action'], BluetoothAction.values);
    final state = BluetoothState.values[json['state'] ?? BluetoothState.unknown.index];
    return BluetoothStream(
      action: action,
      state:
          state,
      connectionState: BluetoothConnectionState.values[
          json['connectionState'] ?? BluetoothConnectionState.unknown.index],
      device: json['device']
    );
  }
}

String enumToString(Object o) => o.toString().split('.').last;

T enumFromString<T extends Object>(String key, List<T> values) => values
    .firstWhere((v) => key.toLowerCase() == enumToString(v).toLowerCase());
