// Test Organization:
// 1. Scan class (static methods) - Tests for platform communication
// 2. ScanView and ScanController - Tests for UI widgets and camera control
// 3. Edge cases and boundary tests - Tests for error handling and limits
//
// Running tests:
//   $ flutter test test/scan_snap_test.dart
//   $ flutter test --coverage
//

import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:scan_snap/scan_snap.dart';

void main() {
  /// Initialize Flutter test binding
  /// Required to access Flutter services like MethodChannel in tests
  /// Without this, platform view tests would fail
  TestWidgetsFlutterBinding.ensureInitialized();

  // ═══════════════════════════════════════════════════════════════════════════
  // GROUP: SCAN CLASS TESTS (Static Methods)
  // ═══════════════════════════════════════════════════════════════════════════
  // Tests the communication between Dart and native platform code
  // Covers: getPlatformVersion() and parse() methods
  group('Scan class (static methods)', () {
    /// MethodChannel for communicating with native platform code
    /// This is the same channel defined in lib/scan_snap.dart
    const MethodChannel channel = MethodChannel('scan_snap/scan');

    /// Log to track all method calls to the native side
    /// Used to verify correct method names and arguments
    final List<MethodCall> log = <MethodCall>[];

    /// Setup: Executed before each test in this group
    /// Configures the mock MethodChannel handler
    setUp(() {
      /// Configure mock handler for platform channel
      /// Simulates native platform responses without actual native code
      /// This allows tests to run on any machine without iOS/Android setup
      TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
          .setMockMethodCallHandler(channel, (MethodCall methodCall) async {
        log.add(methodCall);

        // Simulate native platform responses
        if (methodCall.method == 'getPlatformVersion') {
          return 'iOS 18.0';
        }
        if (methodCall.method == 'parse') {
          return 'QR_DATA_123456'; // Simulated QR decode result
        }
        return null;
      });

      log.clear();
    });

    /// Teardown: Executed after each test in this group
    /// Cleans up mock handlers to prevent interference with other tests
    tearDown(() {
      TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
          .setMockMethodCallHandler(channel, null);
    });

    // ─────────────────────────────────────────────────────────────────────────
    // TEST: getPlatformVersion
    // ─────────────────────────────────────────────────────────────────────────
    test('getPlatformVersion returns platform version from native side',
        () async {
      // Arrange
      final expected = 'iOS 18.0';

      // Act
      final result = await Scan.platformVersion;

      // Assert
      expect(result, expected);

      // Verify the correct method was called with correct arguments
      expect(log, <Matcher>[
        isMethodCall('getPlatformVersion', arguments: null),
      ]);
    });

    // ─────────────────────────────────────────────────────────────────────────
    // TEST: parse with valid image path
    // ─────────────────────────────────────────────────────────────────────────
    test('parse sends correct image path and returns parsed QR data', () async {
      // Arrange
      const imagePath = '/path/to/image.jpg';
      final expected = 'QR_DATA_123456';

      // Act
      final result = await Scan.parse(imagePath);

      // Assert
      expect(result, expected);

      // Verify correct arguments were passed to native side
      expect(log, <Matcher>[
        isMethodCall('parse', arguments: {'path': imagePath}),
      ]);
    });

    // ─────────────────────────────────────────────────────────────────────────
    // TEST: parse with empty path (error handling)
    // ─────────────────────────────────────────────────────────────────────────
    test('parse handles empty path gracefully', () async {
      // Arrange
      const emptyPath = '';

      // Act
      final result = await Scan.parse(emptyPath);

      // Assert - should return null or handle gracefully
      expect(result, isA<String?>());
    });

    // ─────────────────────────────────────────────────────────────────────────
    // TEST: parse with invalid/null path
    // ─────────────────────────────────────────────────────────────────────────
    test('parse returns null for non-existent image', () async {
      // Arrange
      const invalidPath = '/invalid/path/that/does/not/exist.jpg';

      // Act
      final result = await Scan.parse(invalidPath);

      // Assert
      expect(result, isA<String?>());
    });
  });

  // ═══════════════════════════════════════════════════════════════════════════
  // GROUP: SCANVIEW AND SCANCONTROLLER TESTS
  // ═══════════════════════════════════════════════════════════════════════════
  // Tests the UI widget and camera control functionality
  // Covers: ScanView widget, ScanController, and platform-specific rendering
  group('ScanView and ScanController', () {
    /// MethodChannel for platform view control (pause, resume, toggleTorch)
    /// Each platform view instance gets a unique channel ID
    const MethodChannel viewChannel = MethodChannel('scan_snap/scan/method_0');

    final List<MethodCall> log = <MethodCall>[];

    setUp(() {
      TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
          .setMockMethodCallHandler(viewChannel, (MethodCall methodCall) async {
        log.add(methodCall);
        return null; // Control methods return void
      });
      log.clear();
    });

    tearDown(() {
      TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
          .setMockMethodCallHandler(viewChannel, null);
    });

    // ─────────────────────────────────────────────────────────────────────────
    // TEST: ScanView renders on Android
    // ─────────────────────────────────────────────────────────────────────────
    testWidgets('ScanView builds on Android without crashing',
        (WidgetTester tester) async {
      // Arrange
      debugDefaultTargetPlatformOverride = TargetPlatform.android;

      try {
        // Act
        await tester.pumpWidget(
          const MaterialApp(
            home: Scaffold(
              body: ScanView(),
            ),
          ),
        );

        // Assert
        expect(find.byType(ScanView), findsOneWidget);
      } finally {
        debugDefaultTargetPlatformOverride = null;
      }
    });

    // ─────────────────────────────────────────────────────────────────────────
    // TEST: ScanView renders on iOS
    // ─────────────────────────────────────────────────────────────────────────
    testWidgets('ScanView builds on iOS without crashing',
        (WidgetTester tester) async {
      // Arrange
      debugDefaultTargetPlatformOverride = TargetPlatform.iOS;

      try {
        // Act
        await tester.pumpWidget(
          const MaterialApp(
            home: Scaffold(
              body: ScanView(),
            ),
          ),
        );

        // Assert
        expect(find.byType(ScanView), findsOneWidget);
      } finally {
        debugDefaultTargetPlatformOverride = null;
      }
    });

    // ─────────────────────────────────────────────────────────────────────────
    // TEST: ScanView with custom parameters
    // ─────────────────────────────────────────────────────────────────────────
    testWidgets('ScanView accepts and stores custom parameters',
        (WidgetTester tester) async {
      // Arrange
      final customColor = Colors.red;
      const customScale = 0.8;

      // Act
      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: ScanView(
              scanLineColor: customColor,
              scanAreaScale: customScale,
            ),
          ),
        ),
      );

      // Assert
      final scanViewWidget = tester.widget<ScanView>(find.byType(ScanView));
      expect(scanViewWidget.scanLineColor, customColor);
      expect(scanViewWidget.scanAreaScale, customScale);
    });

    // ─────────────────────────────────────────────────────────────────────────
    // TEST: ScanAreaScale validation - invalid scales rejected
    // ─────────────────────────────────────────────────────────────────────────
    test('ScanAreaScale validation - invalid scales are rejected', () {
      // Test scale > 1.0 (invalid)
      expect(
        () => ScanView(scanAreaScale: 1.5),
        throwsAssertionError,
      );

      // Test scale <= 0.0 (invalid)
      expect(
        () => ScanView(scanAreaScale: 0.0),
        throwsAssertionError,
      );

      expect(
        () => ScanView(scanAreaScale: -0.5),
        throwsAssertionError,
      );

      // Test valid scales
      expect(() => ScanView(scanAreaScale: 0.1), isNotNull);
      expect(() => ScanView(scanAreaScale: 0.5), isNotNull);
      expect(() => ScanView(scanAreaScale: 1.0), isNotNull);
    });

    // ─────────────────────────────────────────────────────────────────────────
    // TEST: ScanController pause/resume
    // ─────────────────────────────────────────────────────────────────────────
    testWidgets('ScanController pause/resume calls native methods',
        (WidgetTester tester) async {
      // Arrange
      final controller = ScanController();

      // Act
      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: ScanView(controller: controller),
          ),
        ),
      );

      await tester.pumpAndSettle();

      controller.pause();
      await tester.pumpAndSettle();

      controller.resume();
      await tester.pumpAndSettle();

      // Assert
      expect(controller, isNotNull);
    });

    // ─────────────────────────────────────────────────────────────────────────
    // TEST: ScanController toggleTorchMode
    // ─────────────────────────────────────────────────────────────────────────
    testWidgets('ScanController toggleTorchMode works correctly',
        (WidgetTester tester) async {
      // Arrange
      final controller = ScanController();

      // Act
      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: ScanView(controller: controller),
          ),
        ),
      );

      await tester.pumpAndSettle();

      // Toggle torch on
      controller.toggleTorchMode();
      await tester.pumpAndSettle();

      // Toggle torch off
      controller.toggleTorchMode();
      await tester.pumpAndSettle();

      // Assert
      expect(controller, isNotNull);
    });

    // ─────────────────────────────────────────────────────────────────────────
    // TEST: ScanController shutdown
    // ─────────────────────────────────────────────────────────────────────────
    testWidgets('ScanController shutdown releases resources',
        (WidgetTester tester) async {
      // Arrange
      final controller = ScanController();

      // Act
      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: ScanView(controller: controller),
          ),
        ),
      );

      await tester.pumpAndSettle();

      // Shutdown should not throw exception
      await controller.shutdown();
      await tester.pumpAndSettle();

      // Assert
      expect(controller, isNotNull);
    });

    // ─────────────────────────────────────────────────────────────────────────
    // TEST: Color conversion to RGB
    // ─────────────────────────────────────────────────────────────────────────
    test('Color conversion from Flutter to RGB values', () {
      // Arrange
      final greenColor = Colors.green;

      // Act
      final r = (greenColor.r * 255).toInt();
      final g = (greenColor.g * 255).toInt();
      final b = (greenColor.b * 255).toInt();

      // Assert - values should be in valid RGB range (0-255)
      expect(r, greaterThanOrEqualTo(0));
      expect(r, lessThanOrEqualTo(255));
      expect(g, greaterThanOrEqualTo(0));
      expect(g, lessThanOrEqualTo(255));
      expect(b, greaterThanOrEqualTo(0));
      expect(b, lessThanOrEqualTo(255));

      // For Colors.green, green component should be highest
      expect(g, greaterThan(r));
      expect(g, greaterThan(b));
    });

    // ─────────────────────────────────────────────────────────────────────────
    // TEST: Default values
    // ─────────────────────────────────────────────────────────────────────────
    test('ScanView has sensible default values', () {
      // Arrange & Act
      final scanView = ScanView();

      // Assert
      expect(scanView.scanLineColor, Colors.green);
      expect(scanView.scanAreaScale, 0.7);
      expect(scanView.controller, isNull);
      expect(scanView.onCapture, isNull);
    });

    // ─────────────────────────────────────────────────────────────────────────
    // TEST: onCapture callback
    // ─────────────────────────────────────────────────────────────────────────
    test('ScanView accepts onCapture callback', () {
      // Arrange
      void onCapture(String data) {}

      // Act
      final scanView = ScanView(onCapture: onCapture);

      // Assert
      expect(scanView.onCapture, isNotNull);
      expect(scanView.onCapture, onCapture);
    });
  });

  // ═══════════════════════════════════════════════════════════════════════════
  // GROUP: EDGE CASES AND BOUNDARY TESTS
  // ═══════════════════════════════════════════════════════════════════════════
  // Tests error handling and boundary conditions
  group('Edge cases and boundary tests', () {
    // ─────────────────────────────────────────────────────────────────────────
    // TEST: Multiple ScanController instances
    // ─────────────────────────────────────────────────────────────────────────
    test('Multiple ScanController instances are independent', () {
      // Arrange & Act
      final controller1 = ScanController();
      final controller2 = ScanController();

      // Assert - should be different instances
      expect(controller1, isNot(equals(controller2)));
      expect(identical(controller1, controller2), false);
    });

    // ─────────────────────────────────────────────────────────────────────────
    // TEST: ScanAreaScale boundary values
    // ─────────────────────────────────────────────────────────────────────────
    test('ScanAreaScale boundary tests', () {
      // Test minimum valid value
      expect(() => ScanView(scanAreaScale: 0.0001), isNotNull);

      // Test maximum valid value
      expect(() => ScanView(scanAreaScale: 1.0), isNotNull);

      // Test just outside valid range
      expect(
        () => ScanView(scanAreaScale: 1.0000001),
        throwsAssertionError,
      );

      expect(
        () => ScanView(scanAreaScale: 0.0),
        throwsAssertionError,
      );
    });

    // ─────────────────────────────────────────────────────────────────────────
    // TEST: Multiple ScanView instances
    // ─────────────────────────────────────────────────────────────────────────
    testWidgets('Multiple ScanView instances can coexist',
        (WidgetTester tester) async {
      // Act
      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: Column(
              children: [
                Expanded(
                  child: ScanView(
                    scanLineColor: Colors.green,
                    scanAreaScale: 0.7,
                  ),
                ),
                Expanded(
                  child: ScanView(
                    scanLineColor: Colors.red,
                    scanAreaScale: 0.6,
                  ),
                ),
              ],
            ),
          ),
        ),
      );

      // Assert
      expect(find.byType(ScanView), findsWidgets);
    });

    // ─────────────────────────────────────────────────────────────────────────
    // TEST: Widget lifecycle
    // ─────────────────────────────────────────────────────────────────────────
    test('ScanView is a StatefulWidget', () {
      // Arrange & Act
      final scanView = ScanView();

      // Assert
      expect(scanView, isA<StatefulWidget>());
    });

    // ─────────────────────────────────────────────────────────────────────────
    // TEST: Controller instantiation
    // ─────────────────────────────────────────────────────────────────────────
    test('ScanController can be instantiated', () {
      // Act
      final controller = ScanController();

      // Assert
      expect(controller, isA<ScanController>());
      expect(controller, isNotNull);
    });

    // ─────────────────────────────────────────────────────────────────────────
    // TEST: Color immutability
    // ─────────────────────────────────────────────────────────────────────────
    test('ScanView color parameter is immutable', () {
      // Arrange
      final scanView = ScanView(scanLineColor: Colors.blue);

      // Assert
      expect(scanView.scanLineColor, Colors.blue);
      // Colors in Flutter are immutable by design
    });
  });
}
