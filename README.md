# scan_snap

[![pub](https://img.shields.io/badge/pub-0.0.1-blue)](https://pub.dev/packages/scan_snap) [![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)


A simple, customizable, and high-performance QR and barcode scanner widget for Flutter. Easily decode codes from the live camera stream or from image files.

> 🔄 This package is a maintained and improved **fork** of the popular `scan` plugin by [chavesgu](https://github.com/chavesgu).

---

## ✨ Features

- **Embedded Widget**: Embed a live scanner directly into your widget tree with the `ScanView` widget.
- **Customizable**: Easily adjust the scan area size and the animated scan line color.
- **File Scanning**: Decode a QR/barcode from an image file path using `Scan.parse()`.
- **Full Camera Control**: Pause, resume, and toggle the camera's torch using a `ScanController`.
- **Native Performance**: Utilizes CameraX on Android and AVFoundation on iOS for optimal performance.

---

## 🚀 Getting Started

### 1. Add Dependency

Add the package to your `pubspec.yaml` dependencies:

```yaml
dependencies:
  scan_snap: ^0.0.1 # Replace with the latest version
```

Then, import the package in your Dart code:

```dart
import 'package:scan_snap/scan.dart';
```

### 2. Platform Setup

You need to add permissions to access the camera and photo library.

####  iOS

Add the following keys to your `ios/Runner/Info.plist` file:

```xml
<key>NSCameraUsageDescription</key>
<string>This app requires camera access to scan QR codes.</string>
<key>NSPhotoLibraryUsageDescription</key>
<string>This app requires photo library access to scan QR codes from images.</string>

<!-- Required for Flutter Platform Views -->
<key>io.flutter.embedded_views_preview</key>
<true/>
```


Also, ensure your `minSdkVersion` in `android/app/build.gradle` is set to at least 23.

```groovy
// in android/app/build.gradle
android {
    defaultConfig {
        ...
        minSdkVersion 23
        ...
    }
}
```

---

## 📦 Basic Usage

### Live Scanner with `ScanView`

The best way to use the scanner is within a `StatefulWidget`. Here is a complete example of a scanner screen.

```dart
import 'package:flutter/material.dart';
import 'package:scan_snap/scan.dart';

class ScanScreen extends StatefulWidget {
  const ScanScreen({super.key});

  @override
  State<ScanScreen> createState() => _ScanScreenState();
}

class _ScanScreenState extends State<ScanScreen> {
  final ScanController _controller = ScanController();

  void _onCapture(String data) {
    // When a QR is captured, stop the scanner and return to the previous screen
    // with the captured data.
    print('Captured data: $data');
    if (mounted) {
      Navigator.of(context).pop(data);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Scan Code')),
      body: ScanView(
        controller: _controller,
        scanAreaScale: 0.7, // Scan area is 70% of the view size
        scanLineColor: Colors.tealAccent,
        onCapture: _onCapture,
      ),
    );
  }
}
```

### Controlling the Camera

You can control the camera via the `ScanController` passed to the `ScanView`.

```dart
// Pause image analysis (the preview stream continues)
controller.pause();

// Resume image analysis
controller.resume();

// Toggle the flashlight on or off
controller.toggleTorchMode();

// Safely shut down and release the camera (used when leaving the screen)
await controller.shutdown();
```

### Scanning from an Image File

Use the static `Scan.parse()` method to decode a QR/barcode from a saved image on the device. This is ideal for use with plugins like `image_picker`.

```dart
import 'package:image_picker/image_picker.dart';
import 'package:scan_snap/scan.dart';

// ...

// Example of how to pick an image from the gallery and scan it
Future<void> scanFromGallery() async {
  final ImagePicker picker = ImagePicker();
  final XFile? image = await picker.pickImage(source: ImageSource.gallery);
  
  if (image != null) {
    final String? result = await Scan.parse(image.path);
    if (result != null) {
      print('Result from image: $result');
    }
  }
}
```

---
### 🔐 Proguard Rules (Android)

If you use Proguard/R8 to shrink and obfuscate your release app, add the following rules to your `android/app/proguard-rules.pro` file to prevent breaking ZXing and CameraX functionality.

```proguard
# Rules for ZXing (the decoding library)
-keep class com.google.zxing.** { *; }

# Rules for CameraX (usually not needed but recommended for safety)
-keep public class androidx.camera.core.** {
  public *;
}
-keep class * implements androidx.camera.core.ImageAnalysis$Analyzer {
  *;
}
```

---

### 🙏 Acknowledgements

This plugin is based on the  work of the original `scan` plugin created by [chavesgu](https://github.com/chavesgu). 

### 📄 License

[MIT License](https://opensource.org/licenses/MIT)
