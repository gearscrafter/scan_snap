# scan_snap

[![pub](https://img.shields.io/badge/pub-0.1.2-blue)](https://pub.dev/packages/scan_snap) [![License: Apache 2.0](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
 [![SPM Support](https://img.shields.io/badge/SPM-Supported-green)](https://swift.org/package-manager/)


A simple, customizable, and high-performance QR and barcode scanner widget for Flutter. Easily decode codes from the live camera stream or from image files.

> 🔄 This package is a maintained and improved **fork** of the popular `scan` plugin by [chavesgu](https://github.com/chavesgu).

---

## ✨ Features

- **Embedded Widget**: Embed a live scanner directly into your widget tree with the `ScanView` widget.
- **Customizable**: Easily adjust the scan area size and the animated scan line color.
- **File Scanning**: Decode a QR/barcode from an image file path using `Scan.parse()`.
- **Full Camera Control**: Pause, resume, and toggle the camera's torch using a `ScanController`.
- **Native Performance**: Utilizes CameraX on Android and AVFoundation on iOS for optimal performance.
- **🆕 Huawei HMS Support**: Optional integration with Huawei Scan Kit for devices without Google Play Services.
- **📦 Swift Package Manager (SPM) Support**: Full native support on iOS/macOS via SPM.

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
        minSdkVersion 21
        ...
    }
}
```
### SPM Integration Details

The package includes a `Package.swift` file at the root level that defines:

- **Minimum iOS:** 11.0
- **Minimum macOS:** 10.15
- **Frameworks:** Foundation, UIKit, AVFoundation, Vision
- **Swift Tools Version:** 6.2

This allows developers and CI/CD systems to use `scan_snap` as a native Swift Package without going through Flutter's plugin system if needed.

### Using SPM in Your iOS/macOS Project

If you want to use `scan_snap` directly as a Swift Package (outside of Flutter):

1. In Xcode: **File → Add Packages...**
2. Enter your repository URL: `https://github.com/gearscrafter/scan_snap.git`
3. Select version and target
4. Xcode will automatically resolve and integrate the native components

---

## 🌐 Huawei Mobile Services (HMS) Support

This plugin includes **optional** support for Huawei Mobile Services (HMS), allowing the scanner to work on Huawei devices without Google Play Services (like devices with HarmonyOS).

### When to Enable HMS

Enable HMS support if:
- Your app will be distributed on **Huawei AppGallery**
- You want to support **Huawei devices** without Google Play Services
- You're targeting markets where Huawei devices are popular

### Setup HMS (Optional)

#### Step 1: Create a Huawei Developer Account

1. Go to [AppGallery Connect](https://developer.huawei.com/consumer/en/service/josp/agc/index.html)
2. Create an account and set up a new project
3. Add your app to the project with your package name

#### Step 2: Download Configuration File

1. In AppGallery Connect, go to **Project Settings** → **General Information**
2. Download the `agconnect-services.json` file
3. Place it in your `android/app/` directory

```
your_app/
└── android/
    └── app/
        ├── build.gradle.kts
        └── agconnect-services.json  ← Place here
```

#### Step 3: Configure Gradle Files

**android/settings.gradle.kts:**

```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://developer.huawei.com/repo/") }  // Add this
    }

    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "com.huawei.agconnect") {
                useModule("com.huawei.agconnect:agcp:${requested.version}")
            }
        }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://developer.huawei.com/repo/") }  // Add this
    }
}
```

**android/build.gradle.kts:**

```kotlin
buildscript {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://developer.huawei.com/repo/") }  // Add this
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.9.1")
        classpath("com.huawei.agconnect:agcp:1.9.1.301")  // Add this
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://developer.huawei.com/repo/") }  // Add this
    }
}
```

**android/app/build.gradle.kts:**

```kotlin
plugins {
    id("com.android.application")
    id("kotlin-android")
    id("com.huawei.agconnect")  // Add this
    id("dev.flutter.flutter-gradle-plugin")
}

android {
    // ... your existing configuration
    defaultConfig {
        applicationId = "com.your.package"  // Must match agconnect-services.json
        // ...
    }
}

dependencies {
    // Your existing dependencies...
}
```

#### Important Notes

- **HMS is automatically enabled** if `agconnect-services.json` is present in your project
- If the file is not found, the plugin will compile without HMS support (using only Google ML Kit/ZXing)
- The `applicationId` in your `build.gradle.kts` **must match** the `package_name` in `agconnect-services.json`

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

# Rules for Huawei HMS (if using HMS support)
-keep class com.huawei.hianalytics.**{*;}
-keep class com.huawei.updatesdk.**{*;}
-keep class com.huawei.hms.**{*;}
-dontwarn com.huawei.**

```

---

### 🙏 Acknowledgements

This plugin is based on the  work of the original `scan` plugin created by [chavesgu](https://github.com/chavesgu). 

### 📄 License

This project is licensed under the **Apache License 2.0** - see the [LICENSE](LICENSE) file for details.

---

## 📞 Support

For issues, feature requests, or questions, please open an issue on the [GitHub repository](https://github.com/gearscrafter/scan_snap).

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.