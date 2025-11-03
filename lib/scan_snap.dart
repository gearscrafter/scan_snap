import 'dart:async';
import 'dart:io';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter/gestures.dart';
import 'package:flutter/rendering.dart';

/// Provides static methods to interact with the native platform.
/// Used for retrieving platform version and decoding images via native code.
class Scan {
  static const MethodChannel _channel = MethodChannel('scan_snap/scan');

  /// Returns the platform version from the native side (e.g. "Android 13")
  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  /// Attempts to decode a QR code from the given image path using native code.
  static Future<String?> parse(String path) async {
    final String? result = await _channel.invokeMethod('parse', {'path': path});
    return result;
  }
}

/// Callback type for when a QR code is successfully captured.
typedef CaptureCallback = void Function(String data);

class ScanView extends StatefulWidget {
  const ScanView({
    super.key,
    this.controller,
    this.onCapture,
    this.scanLineColor = Colors.green,
    this.scanAreaScale = 0.7,
  })  : assert(scanAreaScale <= 1.0, 'scanAreaScale must <= 1.0'),
        assert(scanAreaScale > 0.0, 'scanAreaScale must > 0.0');

  final ScanController? controller;
  final CaptureCallback? onCapture;
  final Color scanLineColor;
  final double scanAreaScale;

  @override
  State<ScanView> createState() => _ScanViewState();
}

class _ScanViewState extends State<ScanView> {
  bool _isCameraReady = false;
  bool _allowGestures = false;

  @override
  void initState() {
    super.initState();

    // Set camera ready callback
    widget.controller?.setOnCameraReady(() async {
      if (!mounted) return;

      setState(() {
        _isCameraReady = true;
      });

      await Future.delayed(const Duration(milliseconds: 200));

      if (!mounted) return;

      setState(() {
        _allowGestures = true;
      });
    });
  }

  @override
  Widget build(BuildContext context) {
    if (Platform.isIOS) {
      return UiKitView(
        viewType: 'scan_snap/scan_view',
        creationParamsCodec: const StandardMessageCodec(),
        creationParams: _creationParams,
        onPlatformViewCreated: _onPlatformViewCreated,
      );
    } else {
      return Stack(
        children: [
          PlatformViewLink(
            viewType: 'scan_snap/scan_view',
            surfaceFactory: (context, controller) {
              return IgnorePointer(
                ignoring: !_allowGestures,
                child: PlatformViewSurface(
                  controller: controller,
                  hitTestBehavior: PlatformViewHitTestBehavior.opaque,
                  gestureRecognizers: const <Factory<
                      OneSequenceGestureRecognizer>>{},
                ),
              );
            },
            onCreatePlatformView: (params) {
              return PlatformViewsService.initExpensiveAndroidView(
                id: params.id,
                viewType: 'scan_snap/scan_view',
                layoutDirection: TextDirection.ltr,
                creationParams: _creationParams,
                creationParamsCodec: const StandardMessageCodec(),
              )
                ..addOnPlatformViewCreatedListener(params.onPlatformViewCreated)
                ..addOnPlatformViewCreatedListener(_onPlatformViewCreated)
                ..create();
            },
          ),
          AnimatedOpacity(
            opacity: _isCameraReady ? 0.0 : 1.0,
            duration: const Duration(milliseconds: 300),
            child: Container(
              color: Colors.black,
            ),
          ),
        ],
      );
    }
  }

  /// Parameters passed to the native view during creation.
  Map<String, dynamic> get _creationParams {
    final color = widget.scanLineColor;
    return {
      "r": (color.r * 255).toInt(),
      "g": (color.g * 255).toInt(),
      "b": (color.b * 255).toInt(),
      "a": color.a,
      "scale": widget.scanAreaScale,
    };
  }

  /// Called when the platform view is ready. Initializes the communication channel.
  void _onPlatformViewCreated(int id) {
    final channel = MethodChannel('scan_snap/scan/method_$id');
    widget.controller?._initialize(channel, widget.onCapture);
  }
}

/// Controller to interact with the ScanView.
/// Allows controlling the camera and listening for scan events.
class ScanController {
  MethodChannel? _channel;

  VoidCallback? _onCameraReadyCallback;
  CaptureCallback? _onCaptureCallback;

  /// Sets a callback that is invoked when the camera is fully initialized.
  void setOnCameraReady(VoidCallback callback) {
    _onCameraReadyCallback = callback;
  }

  /// Internal method to initialize the method channel and set up callbacks.
  void _initialize(MethodChannel channel, CaptureCallback? onCapture) {
    _channel = channel;
    _onCaptureCallback = onCapture;

    _channel?.setMethodCallHandler((call) async {
      switch (call.method) {
        case 'onCameraReady':
          _onCameraReadyCallback?.call();
          break;
        case 'onCaptured':
          if (call.arguments != null) {
            _onCaptureCallback?.call(call.arguments.toString());
          }
          break;
        default:
          print('Unknown method from native side: ${call.method}');
      }
    });
  }

  /// Resume camera scanning
  void resume() => _channel?.invokeMethod("resume");

  /// Pause camera scanning
  void pause() => _channel?.invokeMethod("pause");

  /// Toggle the flashlight on/off
  void toggleTorchMode() => _channel?.invokeMethod("toggleTorchMode");

  /// Safely shutdown the camera
  Future<void> shutdown() async {
    try {
      await _channel?.invokeMethod("shutdown");
    } catch (e) {
      print("Error during controlled camera shutdown: $e");
    }
  }
}
