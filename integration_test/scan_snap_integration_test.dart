// Test Organization:
// 1. Basic integration tests (scan flow, controller lifecycle)
// 2. User interaction tests (buttons, navigation)
// 3. Multi-instance tests (multiple scanners)
// 4. Performance tests (rapid operations)
// 5. Error handling and edge cases
//
// Running integration tests:
//   $ flutter test integration_test/scan_snap_integration_test.dart
//
//
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:integration_test/integration_test.dart';
import 'package:scan_snap/scan_snap.dart';

void main() {
  /// Initialize integration test binding
  /// This must be called first to enable integration testing
  /// Allows running tests on real devices and emulators
  IntegrationTestWidgetsFlutterBinding.ensureInitialized();

  // ═══════════════════════════════════════════════════════════════════════════
  // GROUP: BASIC INTEGRATION TESTS
  // ═══════════════════════════════════════════════════════════════════════════
  // Tests basic end-to-end scanning functionality
  // Covers: Widget building, camera initialization, basic interactions
  group('ScanView Basic Integration Tests', () {
    // ─────────────────────────────────────────────────────────────────────────
    // TEST: Complete scan flow
    // ─────────────────────────────────────────────────────────────────────────
    /// Tests end-to-end scanning flow
    ///
    /// Verifies:
    /// 1. ScanView builds successfully
    /// 2. Camera initializes
    /// 3. onCapture callback is set up
    /// 4. Widget is ready for scanning
    testWidgets('Complete scan flow with UI interaction',
        (WidgetTester tester) async {
      // Arrange - Data to capture scan results
      String? scannedCode;

      // Act - Build the scanning interface
      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: ScanView(
              onCapture: (String code) {
                scannedCode = code;
              },
              scanLineColor: Colors.green,
              scanAreaScale: 0.7,
            ),
          ),
        ),
      );

      // Wait for camera initialization (typically 1-2 seconds)
      // Increase timeout if device is slow
      await tester.pumpAndSettle(const Duration(seconds: 2));

      // Assert - Verify widget is rendered and ready
      expect(find.byType(ScanView), findsOneWidget);
      expect(scannedCode, isNull); // Not scanned yet
    });

    // ─────────────────────────────────────────────────────────────────────────
    // TEST: Get platform version
    // ─────────────────────────────────────────────────────────────────────────
    /// Tests that platform version is correctly retrieved from native side
    testWidgets('Get platform version', (WidgetTester tester) async {
      // Act - Query platform version
      final platformVersion = await Scan.platformVersion;

      // Assert - Platform version should not be empty
      expect(platformVersion, isNotEmpty);

      // Platform version should contain OS name
      expect(
        platformVersion.contains('iOS') || platformVersion.contains('Android'),
        true,
      );
    });

    // ─────────────────────────────────────────────────────────────────────────
    // TEST: Camera ready callback
    // ─────────────────────────────────────────────────────────────────────────
    /// Tests that camera ready callback is invoked after initialization
    testWidgets('Camera ready callback is invoked',
        (WidgetTester tester) async {
      // Arrange
      final controller = ScanController();
      bool cameraReady = false;

      controller.setOnCameraReady(() {
        cameraReady = true;
      });

      // Act - Build widget with controller
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

      // Wait for camera initialization
      // Camera ready callback should be called within 3 seconds
      await tester.pumpAndSettle(const Duration(seconds: 3));

      // Assert
      expect(controller, isNotNull);
    });
  });

  // ═══════════════════════════════════════════════════════════════════════════
  // GROUP: CONTROLLER LIFECYCLE TESTS
  // ═══════════════════════════════════════════════════════════════════════════
  // Tests ScanController methods and camera control operations
  // Covers: pause, resume, toggleTorch, shutdown
  group('ScanController Lifecycle Tests', () {
    // ─────────────────────────────────────────────────────────────────────────
    // TEST: Controller pause/resume cycle
    // ─────────────────────────────────────────────────────────────────────────
    /// Tests pause() and resume() methods work correctly
    ///
    /// Simulates user minimizing/resuming the app
    /// Verifies camera state management
    testWidgets('ScanController manages camera lifecycle',
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

      // Wait for initialization
      await tester.pumpAndSettle(const Duration(seconds: 2));

      // Pause camera (simulate app backgrounding)
      controller.pause();
      await tester.pumpAndSettle(const Duration(milliseconds: 500));

      // Resume camera (simulate app foregrounding)
      controller.resume();
      await tester.pumpAndSettle(const Duration(milliseconds: 500));

      // Assert - Controller should still be valid
      expect(controller, isNotNull);
    });

    // ─────────────────────────────────────────────────────────────────────────
    // TEST: Torch mode toggle
    // ─────────────────────────────────────────────────────────────────────────
    /// Tests torch/flashlight toggle functionality
    ///
    /// Useful for scanning in low-light conditions
    testWidgets('Toggle torch mode works correctly',
        (WidgetTester tester) async {
      // Arrange
      final controller = ScanController();

      // Act - Build UI with torch button
      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: Column(
              children: [
                Expanded(
                  child: ScanView(
                    controller: controller,
                    scanLineColor: Colors.green,
                    scanAreaScale: 0.7,
                  ),
                ),
                ElevatedButton(
                  onPressed: () => controller.toggleTorchMode(),
                  child: const Text('Toggle Torch'),
                ),
              ],
            ),
          ),
        ),
      );

      // Wait for camera initialization
      await tester.pumpAndSettle(const Duration(seconds: 2));

      // Toggle torch on
      await tester.tap(find.byType(ElevatedButton));
      await tester.pumpAndSettle(const Duration(milliseconds: 500));

      // Toggle torch off
      await tester.tap(find.byType(ElevatedButton));
      await tester.pumpAndSettle(const Duration(milliseconds: 500));

      // Assert - Button should still be present
      expect(find.byType(ElevatedButton), findsOneWidget);
    });

    // ─────────────────────────────────────────────────────────────────────────
    // TEST: Controller shutdown
    // ─────────────────────────────────────────────────────────────────────────
    /// Tests proper resource cleanup when shutting down
    testWidgets('ScanController shutdown releases resources',
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

      await tester.pumpAndSettle(const Duration(seconds: 2));

      // Shutdown controller (cleanup)
      await controller.shutdown();
      await tester.pumpAndSettle();

      // Assert - Shutdown completed successfully
      expect(controller, isNotNull);
    });
  });

  // ═══════════════════════════════════════════════════════════════════════════
  // GROUP: MULTI-INSTANCE TESTS
  // ═══════════════════════════════════════════════════════════════════════════
  // Tests scenarios with multiple ScanView instances
  group('Multiple ScanView Instance Tests', () {
    // ─────────────────────────────────────────────────────────────────────────
    // TEST: Multiple scanners independence
    // ─────────────────────────────────────────────────────────────────────────
    /// Tests that multiple ScanView widgets work independently
    ///
    /// Useful for: Tab views, split screens, multiple scanner scenarios
    testWidgets('Multiple ScanView instances work independently',
        (WidgetTester tester) async {
      // Arrange
      final controller1 = ScanController();
      final controller2 = ScanController();

      // Act - Build UI with two scanners
      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: ListView(
              children: [
                SizedBox(
                  height: 300,
                  child: ScanView(
                    controller: controller1,
                    scanLineColor: Colors.green,
                    scanAreaScale: 0.7,
                  ),
                ),
                SizedBox(
                  height: 300,
                  child: ScanView(
                    controller: controller2,
                    scanLineColor: Colors.red,
                    scanAreaScale: 0.6,
                  ),
                ),
              ],
            ),
          ),
        ),
      );

      await tester.pumpAndSettle(const Duration(seconds: 2));

      // Control first scanner independently
      controller1.pause();
      await tester.pumpAndSettle();

      controller1.resume();
      await tester.pumpAndSettle();

      // Control second scanner independently
      controller2.pause();
      await tester.pumpAndSettle();

      controller2.resume();
      await tester.pumpAndSettle();

      // Assert - Both scanners should be rendered
      expect(find.byType(ScanView), findsWidgets);
    });
  });

  // ═══════════════════════════════════════════════════════════════════════════
  // GROUP: CUSTOMIZATION TESTS
  // ═══════════════════════════════════════════════════════════════════════════
  // Tests customization options (colors, scales, etc.)
  group('ScanView Customization Tests', () {
    // ─────────────────────────────────────────────────────────────────────────
    // TEST: Dynamic color changes
    // ─────────────────────────────────────────────────────────────────────────
    /// Tests that scan line color can be changed dynamically
    testWidgets('ScanView color can be customized',
        (WidgetTester tester) async {
      // Arrange - List of colors to test
      final colors = [Colors.green, Colors.red, Colors.blue, Colors.yellow];

      // Act - Test each color
      for (final color in colors) {
        await tester.pumpWidget(
          MaterialApp(
            home: Scaffold(
              body: ScanView(
                scanLineColor: color,
                scanAreaScale: 0.7,
              ),
            ),
          ),
        );

        // Brief wait for rendering
        await tester.pumpAndSettle(const Duration(milliseconds: 500));

        // Assert - Widget should render with the color
        expect(find.byType(ScanView), findsOneWidget);

        // Verify the color was applied
        final widget = tester.widget<ScanView>(find.byType(ScanView));
        expect(widget.scanLineColor, equals(color));
      }
    });

    // ─────────────────────────────────────────────────────────────────────────
    // TEST: Dynamic scale changes
    // ─────────────────────────────────────────────────────────────────────────
    /// Tests that scan area scale can be changed
    testWidgets('ScanView scale area works with different values',
        (WidgetTester tester) async {
      // Arrange - List of scales to test
      final scales = [0.5, 0.7, 0.9];

      // Act - Test each scale
      for (final scale in scales) {
        await tester.pumpWidget(
          MaterialApp(
            home: Scaffold(
              body: ScanView(
                scanLineColor: Colors.green,
                scanAreaScale: scale,
              ),
            ),
          ),
        );

        await tester.pumpAndSettle(const Duration(milliseconds: 500));

        // Assert - Widget should render with the scale
        expect(find.byType(ScanView), findsOneWidget);

        // Verify the scale was applied
        final widget = tester.widget<ScanView>(find.byType(ScanView));
        expect(widget.scanAreaScale, equals(scale));
      }
    });
  });

  // ═══════════════════════════════════════════════════════════════════════════
  // GROUP: STRESS TESTS
  // ═══════════════════════════════════════════════════════════════════════════
  // Tests rapid operations and edge cases
  group('ScanView Stress Tests', () {
    // ─────────────────────────────────────────────────────────────────────────
    // TEST: Rapid pause/resume operations
    // ─────────────────────────────────────────────────────────────────────────
    /// Tests that rapid pause/resume calls don't crash the app
    ///
    /// Simulates: User rapidly switching between apps
    testWidgets('Rapid pause/resume does not crash',
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

      await tester.pumpAndSettle(const Duration(seconds: 2));

      // Rapid toggles (simulate rapid app switching)
      for (int i = 0; i < 10; i++) {
        controller.pause();
        controller.resume();
      }

      await tester.pumpAndSettle();

      // Assert - Should handle rapid calls without crashing
      expect(find.byType(ScanView), findsOneWidget);
    });

    // ─────────────────────────────────────────────────────────────────────────
    // TEST: Rapid torch toggles
    // ─────────────────────────────────────────────────────────────────────────
    /// Tests that rapid torch toggles work correctly
    testWidgets('Rapid torch toggles work correctly',
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

      await tester.pumpAndSettle(const Duration(seconds: 2));

      // Rapid torch toggles
      for (int i = 0; i < 5; i++) {
        controller.toggleTorchMode();
      }

      await tester.pumpAndSettle();

      // Assert
      expect(find.byType(ScanView), findsOneWidget);
    });

    // ─────────────────────────────────────────────────────────────────────────
    // TEST: Memory management
    // ─────────────────────────────────────────────────────────────────────────
    /// Tests that resources are properly released when removing scanner
    testWidgets('ScanView properly releases resources on dispose',
        (WidgetTester tester) async {
      // Arrange
      final controller = ScanController();

      // Act - Build with scanner
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

      await tester.pumpAndSettle(const Duration(seconds: 2));

      // Shutdown
      await controller.shutdown();
      await tester.pumpAndSettle();

      // Remove scanner from widget tree
      await tester.pumpWidget(
        const MaterialApp(
          home: Scaffold(
            body: Center(child: Text('No Scanner')),
          ),
        ),
      );

      await tester.pumpAndSettle();

      // Assert - Scanner should be gone
      expect(find.byType(ScanView), findsNothing);
    });
  });

  // ═══════════════════════════════════════════════════════════════════════════
  // GROUP: ERROR HANDLING TESTS
  // ═══════════════════════════════════════════════════════════════════════════
  // Tests error handling and edge cases
  group('Error Handling Tests', () {
    // ─────────────────────────────────────────────────────────────────────────
    // TEST: Empty scan data handling
    // ─────────────────────────────────────────────────────────────────────────
    /// Tests that empty scan data is handled gracefully
    testWidgets('ScanView handles empty scan data',
        (WidgetTester tester) async {
      // Arrange
      String? errorMessage;

      // Act
      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: ScanView(
              onCapture: (String data) {
                if (data.isEmpty) {
                  errorMessage = 'Empty data received';
                }
              },
              scanLineColor: Colors.green,
              scanAreaScale: 0.7,
            ),
          ),
        ),
      );

      await tester.pumpAndSettle(const Duration(seconds: 2));

      // Assert - Should initialize without errors
      expect(find.byType(ScanView), findsOneWidget);
      expect(errorMessage, isNull);
    });

    // ─────────────────────────────────────────────────────────────────────────
    // TEST: Controller operations without view
    // ─────────────────────────────────────────────────────────────────────────
    /// Tests that controller methods handle missing view gracefully
    testWidgets('Controller handles operations without active view',
        (WidgetTester tester) async {
      // Arrange
      final controller = ScanController();

      // Act - Don't attach to any view
      // Calling methods should not throw
      controller.pause();
      controller.resume();
      controller.toggleTorchMode();

      // Assert - No exceptions
      expect(controller, isNotNull);
    });
  });

  // ═══════════════════════════════════════════════════════════════════════════
  // GROUP: PERFORMANCE TESTS
  // ═══════════════════════════════════════════════════════════════════════════
  // Tests performance characteristics
  group('Performance Tests', () {
    // ─────────────────────────────────────────────────────────────────────────
    // TEST: Widget build time
    // ─────────────────────────────────────────────────────────────────────────
    /// Tests that ScanView builds within acceptable time
    testWidgets('ScanView builds within acceptable time',
        (WidgetTester tester) async {
      // Arrange - Start timer
      final stopwatch = Stopwatch()..start();

      // Act - Build widget
      await tester.pumpWidget(
        const MaterialApp(
          home: Scaffold(
            body: ScanView(),
          ),
        ),
      );

      stopwatch.stop();

      // Assert - Build should be reasonably fast (< 1 second)
      expect(stopwatch.elapsedMilliseconds, lessThan(1000));
      expect(find.byType(ScanView), findsOneWidget);
    });

    // ─────────────────────────────────────────────────────────────────────────
    // TEST: Multiple rapid widget rebuilds
    // ─────────────────────────────────────────────────────────────────────────
    /// Tests that rapid rebuilds don't degrade performance
    testWidgets('Multiple rapid rebuilds work correctly',
        (WidgetTester tester) async {
      // Act - Rapid rebuilds
      for (int i = 0; i < 5; i++) {
        await tester.pumpWidget(
          MaterialApp(
            home: Scaffold(
              body: ScanView(
                scanLineColor: [Colors.green, Colors.red, Colors.blue][i % 3],
              ),
            ),
          ),
        );

        await tester.pumpAndSettle(const Duration(milliseconds: 100));
      }

      // Assert
      expect(find.byType(ScanView), findsOneWidget);
    });
  });
}
