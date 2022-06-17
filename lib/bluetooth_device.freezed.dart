// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target

part of 'bluetooth_device.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

T _$identity<T>(T value) => value;

final _privateConstructorUsedError = UnsupportedError(
    'It seems like you constructed your class using `MyClass._()`. This constructor is only meant to be used by freezed and you are not supposed to need it nor use it.\nPlease check the documentation here for more information: https://github.com/rrousselGit/freezed#custom-getters-and-methods');

BluetoothDevice _$BluetoothDeviceFromJson(Map<String, dynamic> json) {
  return _BluetoothDevice.fromJson(json);
}

/// @nodoc
mixin _$BluetoothDevice {
  String get name => throw _privateConstructorUsedError;
  String get address => throw _privateConstructorUsedError;
  int get type => throw _privateConstructorUsedError;

  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;
  @JsonKey(ignore: true)
  $BluetoothDeviceCopyWith<BluetoothDevice> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $BluetoothDeviceCopyWith<$Res> {
  factory $BluetoothDeviceCopyWith(
          BluetoothDevice value, $Res Function(BluetoothDevice) then) =
      _$BluetoothDeviceCopyWithImpl<$Res>;
  $Res call({String name, String address, int type});
}

/// @nodoc
class _$BluetoothDeviceCopyWithImpl<$Res>
    implements $BluetoothDeviceCopyWith<$Res> {
  _$BluetoothDeviceCopyWithImpl(this._value, this._then);

  final BluetoothDevice _value;
  // ignore: unused_field
  final $Res Function(BluetoothDevice) _then;

  @override
  $Res call({
    Object? name = freezed,
    Object? address = freezed,
    Object? type = freezed,
  }) {
    return _then(_value.copyWith(
      name: name == freezed
          ? _value.name
          : name // ignore: cast_nullable_to_non_nullable
              as String,
      address: address == freezed
          ? _value.address
          : address // ignore: cast_nullable_to_non_nullable
              as String,
      type: type == freezed
          ? _value.type
          : type // ignore: cast_nullable_to_non_nullable
              as int,
    ));
  }
}

/// @nodoc
abstract class _$$_BluetoothDeviceCopyWith<$Res>
    implements $BluetoothDeviceCopyWith<$Res> {
  factory _$$_BluetoothDeviceCopyWith(
          _$_BluetoothDevice value, $Res Function(_$_BluetoothDevice) then) =
      __$$_BluetoothDeviceCopyWithImpl<$Res>;
  @override
  $Res call({String name, String address, int type});
}

/// @nodoc
class __$$_BluetoothDeviceCopyWithImpl<$Res>
    extends _$BluetoothDeviceCopyWithImpl<$Res>
    implements _$$_BluetoothDeviceCopyWith<$Res> {
  __$$_BluetoothDeviceCopyWithImpl(
      _$_BluetoothDevice _value, $Res Function(_$_BluetoothDevice) _then)
      : super(_value, (v) => _then(v as _$_BluetoothDevice));

  @override
  _$_BluetoothDevice get _value => super._value as _$_BluetoothDevice;

  @override
  $Res call({
    Object? name = freezed,
    Object? address = freezed,
    Object? type = freezed,
  }) {
    return _then(_$_BluetoothDevice(
      name: name == freezed
          ? _value.name
          : name // ignore: cast_nullable_to_non_nullable
              as String,
      address: address == freezed
          ? _value.address
          : address // ignore: cast_nullable_to_non_nullable
              as String,
      type: type == freezed
          ? _value.type
          : type // ignore: cast_nullable_to_non_nullable
              as int,
    ));
  }
}

/// @nodoc
@JsonSerializable()
class _$_BluetoothDevice implements _BluetoothDevice {
  const _$_BluetoothDevice(
      {required this.name, required this.address, required this.type});

  factory _$_BluetoothDevice.fromJson(Map<String, dynamic> json) =>
      _$$_BluetoothDeviceFromJson(json);

  @override
  final String name;
  @override
  final String address;
  @override
  final int type;

  @override
  String toString() {
    return 'BluetoothDevice(name: $name, address: $address, type: $type)';
  }

  @override
  bool operator ==(dynamic other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$_BluetoothDevice &&
            const DeepCollectionEquality().equals(other.name, name) &&
            const DeepCollectionEquality().equals(other.address, address) &&
            const DeepCollectionEquality().equals(other.type, type));
  }

  @JsonKey(ignore: true)
  @override
  int get hashCode => Object.hash(
      runtimeType,
      const DeepCollectionEquality().hash(name),
      const DeepCollectionEquality().hash(address),
      const DeepCollectionEquality().hash(type));

  @JsonKey(ignore: true)
  @override
  _$$_BluetoothDeviceCopyWith<_$_BluetoothDevice> get copyWith =>
      __$$_BluetoothDeviceCopyWithImpl<_$_BluetoothDevice>(this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$_BluetoothDeviceToJson(this);
  }
}

abstract class _BluetoothDevice implements BluetoothDevice {
  const factory _BluetoothDevice(
      {required final String name,
      required final String address,
      required final int type}) = _$_BluetoothDevice;

  factory _BluetoothDevice.fromJson(Map<String, dynamic> json) =
      _$_BluetoothDevice.fromJson;

  @override
  String get name => throw _privateConstructorUsedError;
  @override
  String get address => throw _privateConstructorUsedError;
  @override
  int get type => throw _privateConstructorUsedError;
  @override
  @JsonKey(ignore: true)
  _$$_BluetoothDeviceCopyWith<_$_BluetoothDevice> get copyWith =>
      throw _privateConstructorUsedError;
}
