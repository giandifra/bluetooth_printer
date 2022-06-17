import 'package:freezed_annotation/freezed_annotation.dart';

part 'bluetooth_device.freezed.dart';
part 'bluetooth_device.g.dart';

@freezed
class BluetoothDevice with _$BluetoothDevice {
  const factory BluetoothDevice({
    required String name,
    required String address,
    required int type,
  }) = _BluetoothDevice;

  factory BluetoothDevice.fromJson(Map<String, Object?> json)
  => _$BluetoothDeviceFromJson(json);
}
