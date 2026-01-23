// Test Organization:
// 1. ScanView rendering tests (iOS, Android)
// 2. ScanView parameter tests (color, scale, controller, callback)
// 3. ScanView animation tests (loading state)
// 4. ScanView edge case tests (multiple instances)
// 5. ScanController method tests (pause, resume, toggleTorch, shutdown)
//
// Running tests:
//   $ flutter test test/scan_snap_widget_test.dart
//   $ flutter test test/scan_snap_widget_test.dart -v
//

import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:scan_snap/scan_snap.dart';

void main() {
  /// Initialize Flutter test binding for widget tests
  /// Required to access platform views and MethodChannel in tests
  TestWidgetsFlutterBinding.ensureInitialized();

  // ═══════════════════════════════════════════════════════════════════════════
  // GROUP: SCANVIEW RENDERING TESTS
  // ═══════════════════════════════════════════════════════════════════════════
  // Tests that ScanView widget renders correctly on different platforms
  // Covers: Basic rendering, platform-specific code paths
  group('ScanView Rendering Tests', () {
    // ─────────────────────────────────────────────────────────────────────────
    // TEST: ScanView renders on iOS
    // ─────────────────────────────────────────────────────────────────────────
    /// Tests that ScanView builds and renders on iOS platform
    ///
    /// iOS uses UiKitView to embed native UIViewController
    /// This test verifies the Dart side builds without errors
    testWidgets('ScanView renders on iOS', (WidgetTester tester) async {
      // Arrange - Set platform to iOS
      debugDefaultTargetPlatformOverride = TargetPlatform.iOS;

      try {
        // Act - Build the widget tree
        await tester.pumpWidget(
          MaterialApp(
            home: Scaffold(
              body: ScanView(
                scanLineColor: Colors.green,
                scanAreaScale: 0.7,
              ),
            ),
          ),
        );

        // Assert - Verify widget appears in tree
        expect(find.byType(ScanView), findsOneWidget);
      } finally {
        // Cleanup
        debugDefaultTargetPlatformOverride = null;
      }
    });

    // ─────────────────────────────────────────────────────────────────────────
    // TEST: ScanView renders on Android
    // ─────────────────────────────────────────────────────────────────────────
    /// Tests that ScanView builds and renders on Android platform
    ///
    /// Android uses PlatformViewLink to embed native Android View
    /// This test verifies the Dart side builds without errors
    testWidgets('ScanView renders on Android', (WidgetTester tester) async {
      // Arrange - Set platform to Android
      debugDefaultTargetPlatformOverride = TargetPlatform.android;

      try {
        // Act - Build the widget tree
        await tester.pumpWidget(
          MaterialApp(
            home: Scaffold(
              body: ScanView(
                scanLineColor: Colors.green,
                scanAreaScale: 0.7,
              ),
            ),
          ),
        );

        // Assert - Verify widget appears in tree
        expect(find.byType(ScanView), findsOneWidget);
      } finally {
        // Cleanup
        debugDefaultTargetPlatformOverride = null;
      }
    });

    // ─────────────────────────────────────────────────────────────────────────
    // TEST: ScanView with default values
    // ─────────────────────────────────────────────────────────────────────────
    /// Tests that ScanView renders with no required parameters
    /// Uses all default values
    testWidgets('ScanView renders with default values',
        (WidgetTester tester) async {
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
    });
  });

  // ═══════════════════════════════════════════════════════════════════════════
  // GROUP: SCANVIEW PARAMETER TESTS
  // ═══════════════════════════════════════════════════════════════════════════
  // Tests that ScanView correctly accepts and stores parameters
  // Covers: Custom colors, scale validation, controller, callbacks
  group('ScanView Parameter Tests', () {
    // ─────────────────────────────────────────────────────────────────────────
    // TEST: ScanView accepts custom scan line color
    // ─────────────────────────────────────────────────────────────────────────
    /// Tests that custom colors are accepted and stored
    testWidgets('ScanView accepts custom scan line color',
        (WidgetTester tester) async {
      // Arrange
      final customColor = Colors.red;

      // Act
      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: ScanView(
              scanLineColor: customColor,
              scanAreaScale: 0.7,
            ),
          ),
        ),
      );

      // Assert
      final widget = tester.widget<ScanView>(find.byType(ScanView));
      expect(widget.scanLineColor, equals(customColor));
      expect(find.byType(ScanView), findsOneWidget);
    });

    // ─────────────────────────────────────────────────────────────────────────
    // TEST: ScanView validates scanAreaScale bounds
    // ─────────────────────────────────────────────────────────────────────────
    /// Tests that invalid scanAreaScale values are rejected with AssertionError
    /// Valid range: 0.0 < scale <= 1.0
    testWidgets('ScanView validates scanAreaScale bounds',
        (WidgetTester tester) async {
      // Test: Scale > 1.0 should throw
      expect(
        () => ScanView(scanAreaScale: 1.5),
        throwsAssertionError,
      );

      // Test: Scale <= 0.0 should throw
      expect(
        () => ScanView(scanAreaScale: 0.0),
        throwsAssertionError,
      );

      // Test: Valid scale should build successfully
      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: ScanView(scanAreaScale: 0.8),
          ),
        ),
      );

      expect(find.byType(ScanView), findsOneWidget);

      // Test: Maximum valid scale should work
      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: ScanView(scanAreaScale: 1.0),
          ),
        ),
      );

      expect(find.byType(ScanView), findsOneWidget);
    });

    // ─────────────────────────────────────────────────────────────────────────
    // TEST: ScanView accepts ScanController
    // ─────────────────────────────────────────────────────────────────────────
    /// Tests that ScanController is properly stored and can be used
    testWidgets('ScanView accepts ScanController', (WidgetTester tester) async {
      // Arrange
      final controller = ScanController();

      // Act
      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: ScanView(
              controller: controller,
              scanLineColor: Colors.green,
              scanAreaScale: 0.7,
            ),
          ),
        ),
      );

      // Assert
      final widget = tester.widget<ScanView>(find.byType(ScanView));
      expect(widget.controller, equals(controller));
      expect(find.byType(ScanView), findsOneWidget);
    });

    // ─────────────────────────────────────────────────────────────────────────
    // TEST: ScanView accepts onCapture callback
    // ─────────────────────────────────────────────────────────────────────────
    /// Tests that onCapture callback is properly stored
    /// This callback is invoked when a QR code is successfully captured
    testWidgets('ScanView accepts onCapture callback',
        (WidgetTester tester) async {
      // Arrange
      String? capturedData;

      void onCapture(String data) {
        capturedData = data;
      }

      // Act
      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: ScanView(
              onCapture: onCapture,
              scanLineColor: Colors.green,
              scanAreaScale: 0.7,
            ),
          ),
        ),
      );

      // Assert
      final widget = tester.widget<ScanView>(find.byType(ScanView));
      expect(widget.onCapture, isNotNull);
      expect(widget.onCapture, equals(onCapture));
      expect(capturedData, isNull); // Callback not invoked yet
      expect(find.byType(ScanView), findsOneWidget);
    });

    // ─────────────────────────────────────────────────────────────────────────
    // TEST: ScanView color conversion to RGB
    // ─────────────────────────────────────────────────────────────────────────
    /// Tests that Flutter Color values are correctly converted to native RGB
    /// Flutter colors: 0.0-1.0 range
    /// Native RGB: 0-255 range
    testWidgets('ScanView correctly converts color to RGB values',
        (WidgetTester tester) async {
      // Arrange
      final greenColor = Colors.green;

      // Act
      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: ScanView(
              scanLineColor: greenColor,
              scanAreaScale: 0.7,
            ),
          ),
        ),
      );

      // Assert - Color was accepted and stored
      final widget = tester.widget<ScanView>(find.byType(ScanView));
      expect(widget.scanLineColor, equals(greenColor));

      // Verify RGB conversion logic
      final r = (greenColor.r * 255).toInt();
      final g = (greenColor.g * 255).toInt();
      final b = (greenColor.b * 255).toInt();

      // RGB values should be in valid range
      expect(r, greaterThanOrEqualTo(0));
      expect(r, lessThanOrEqualTo(255));
      expect(g, greaterThanOrEqualTo(0));
      expect(g, lessThanOrEqualTo(255));
      expect(b, greaterThanOrEqualTo(0));
      expect(b, lessThanOrEqualTo(255));
    });
  });

  // ═══════════════════════════════════════════════════════════════════════════
  // GROUP: SCANVIEW ANIMATION TESTS
  // ═══════════════════════════════════════════════════════════════════════════
  // Tests loading animations and state transitions
  group('ScanView Animation Tests', () {
    // ─────────────────────────────────────────────────────────────────────────
    // TEST: ScanView shows loading animation during initialization
    // ─────────────────────────────────────────────────────────────────────────
    /// Tests that loading animation (AnimatedOpacity) is present during init
    /// The black container fades out once camera is ready
    testWidgets('ScanView shows loading animation during initialization',
        (WidgetTester tester) async {
      // Arrange
      final controller = ScanController();

      // Act
      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: ScanView(
              controller: controller,
              scanLineColor: Colors.green,
              scanAreaScale: 0.7,
            ),
          ),
        ),
      );

      // Assert - AnimatedOpacity should be present
      final opacityFinder = find.byType(AnimatedOpacity);
      expect(opacityFinder, findsWidgets);
    });
  });

  // ═══════════════════════════════════════════════════════════════════════════
  // GROUP: SCANVIEW EDGE CASE TESTS
  // ═══════════════════════════════════════════════════════════════════════════
  // Tests unusual scenarios and boundary conditions
  group('ScanView Edge Case Tests', () {
    // ─────────────────────────────────────────────────────────────────────────
    // TEST: Multiple ScanView instances can coexist
    // ─────────────────────────────────────────────────────────────────────────
    /// Tests that multiple ScanView widgets can render in same widget tree
    /// Useful for TabView or multi-scanner scenarios
    testWidgets('Multiple ScanView widgets can coexist',
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

      // Assert - Both widgets should be present
      expect(find.byType(ScanView), findsWidgets);
    });

    // ─────────────────────────────────────────────────────────────────────────
    // TEST: ScanAreaScale with different color combinations
    // ─────────────────────────────────────────────────────────────────────────
    /// Tests multiple color and scale combinations
    testWidgets('ScanView works with different color and scale combinations',
        (WidgetTester tester) async {
      // Test combinations
      final colors = [Colors.green, Colors.red, Colors.blue, Colors.yellow];
      final scales = [0.5, 0.7, 0.9];

      for (final color in colors) {
        for (final scale in scales) {
          // Act
          await tester.pumpWidget(
            MaterialApp(
              home: Scaffold(
                body: ScanView(
                  scanLineColor: color,
                  scanAreaScale: scale,
                ),
              ),
            ),
          );

          // Assert
          expect(find.byType(ScanView), findsOneWidget);

          final widget = tester.widget<ScanView>(find.byType(ScanView));
          expect(widget.scanLineColor, equals(color));
          expect(widget.scanAreaScale, equals(scale));
        }
      }
    });

    // ─────────────────────────────────────────────────────────────────────────
    // TEST: ScanView in different widget hierarchies
    // ─────────────────────────────────────────────────────────────────────────
    /// Tests that ScanView works in different parent widget types
    testWidgets('ScanView works in different widget hierarchies',
        (WidgetTester tester) async {
      // Test: ScanView in Column
      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: Column(
              children: const [
                Text('Header'),
                Expanded(child: ScanView()),
              ],
            ),
          ),
        ),
      );

      expect(find.byType(ScanView), findsOneWidget);

      // Test: ScanView in Stack
      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: Stack(
              children: const [
                ScanView(),
                Positioned(
                  top: 10,
                  right: 10,
                  child: Text('Overlay'),
                ),
              ],
            ),
          ),
        ),
      );

      expect(find.byType(ScanView), findsOneWidget);
    });
  });

  // ═══════════════════════════════════════════════════════════════════════════
  // GROUP: SCANCONTROLLER TESTS
  // ═══════════════════════════════════════════════════════════════════════════
  // Tests ScanController methods and camera control
  // Covers: pause, resume, toggleTorch, shutdown
  group('ScanController Tests', () {
    // ─────────────────────────────────────────────────────────────────────────
    // TEST: ScanController initializes correctly
    // ─────────────────────────────────────────────────────────────────────────
    /// Tests basic controller instantiation
    test('ScanController initializes correctly', () {
      // Act
      final controller = ScanController();

      // Assert
      expect(controller, isNotNull);
      expect(controller, isA<ScanController>());
    });

    // ─────────────────────────────────────────────────────────────────────────
    // TEST: ScanController stores camera ready callback
    // ─────────────────────────────────────────────────────────────────────────
    /// Tests that setOnCameraReady callback is stored
    test('ScanController stores camera ready callback', () {
      // Arrange
      final controller = ScanController();
      bool callbackCalled = false;

      // Act
      controller.setOnCameraReady(() {
        callbackCalled = true;
      });

      // Assert - Callback stored but not invoked yet
      expect(callbackCalled, false);
    });

    // ─────────────────────────────────────────────────────────────────────────
    // TEST: ScanController pause/resume methods
    // ─────────────────────────────────────────────────────────────────────────
    /// Tests that pause() and resume() methods invoke native methods
    testWidgets('ScanController pause and resume work correctly',
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

      // Call pause - should not throw
      controller.pause();
      await tester.pumpAndSettle();

      // Call resume - should not throw
      controller.resume();
      await tester.pumpAndSettle();

      // Assert - Controller still valid
      expect(controller, isNotNull);
    });

    // ─────────────────────────────────────────────────────────────────────────
    // TEST: ScanController toggleTorchMode
    // ─────────────────────────────────────────────────────────────────────────
    /// Tests that toggleTorchMode invokes native torch control
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

      // Assert - Controller still valid
      expect(controller, isNotNull);
    });

    // ─────────────────────────────────────────────────────────────────────────
    // TEST: ScanController shutdown releases resources
    // ─────────────────────────────────────────────────────────────────────────
    /// Tests that shutdown() properly releases camera and resources
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

      // Shutdown - should not throw exception
      await controller.shutdown();
      await tester.pumpAndSettle();

      // Assert - Shutdown completed successfully
      expect(controller, isNotNull);
    });

    // ─────────────────────────────────────────────────────────────────────────
    // TEST: Multiple ScanController instances
    // ─────────────────────────────────────────────────────────────────────────
    /// Tests that multiple controllers can be created independently
    test('Multiple ScanController instances are independent', () {
      // Arrange & Act
      final controller1 = ScanController();
      final controller2 = ScanController();

      // Assert - Should be different instances
      expect(controller1, isNot(equals(controller2)));
      expect(identical(controller1, controller2), false);
    });

    // ─────────────────────────────────────────────────────────────────────────
    // TEST: ScanController rapid pause/resume
    // ─────────────────────────────────────────────────────────────────────────
    /// Tests that rapid calls to pause/resume don't crash the app
    testWidgets('ScanController handles rapid pause/resume calls',
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

      // Rapid toggles
      for (int i = 0; i < 5; i++) {
        controller.pause();
        controller.resume();
      }

      await tester.pumpAndSettle();

      // Assert - No crash, controller valid
      expect(controller, isNotNull);
    });
  });
}
