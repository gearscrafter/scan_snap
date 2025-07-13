import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:scan_snap/scan_snap.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  group('Scan class (static methods)', () {
    const MethodChannel channel = MethodChannel('scan_snap/scan');

    final List<MethodCall> log = <MethodCall>[];

    setUp(() {
      TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
          .setMockMethodCallHandler(channel, (MethodCall methodCall) async {
        log.add(methodCall);
        if (methodCall.method == 'getPlatformVersion') {
          return '42.0.0';
        }
        if (methodCall.method == 'parse') {
          return 'Parsed QR Data';
        }
        return null;
      });
      log.clear();
    });

    tearDown(() {
      TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
          .setMockMethodCallHandler(channel, null);
    });

    test('getPlatformVersion returns platform version', () async {
      expect(await Scan.platformVersion, '42.0.0');
      expect(log, <Matcher>[
        isMethodCall('getPlatformVersion', arguments: null),
      ]);
    });

    test('parse calls channel with correct path and returns result', () async {
      const imagePath = '/path/to/image.jpg';
      expect(await Scan.parse(imagePath), 'Parsed QR Data');
      expect(log, <Matcher>[isMethodCall('parse', arguments: imagePath)]);
    });
  });

  group('ScanView and ScanController', () {
    const MethodChannel viewChannel = MethodChannel('scan_snap/scan/method_0');
    final List<MethodCall> log = <MethodCall>[];

    setUp(() {
      TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
          .setMockMethodCallHandler(viewChannel, (MethodCall methodCall) async {
        log.add(methodCall);
        return null;
      });
      log.clear();
    });

    tearDown(() {
      TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
          .setMockMethodCallHandler(viewChannel, null);
    });

    testWidgets('ScanView renders AndroidView on Android',
        (WidgetTester tester) async {
      debugDefaultTargetPlatformOverride = TargetPlatform.android;

      await tester.pumpWidget(MaterialApp(home: ScanView()));

      expect(find.byType(AndroidView), findsOneWidget);
      expect(find.byType(UiKitView), findsNothing);

      debugDefaultTargetPlatformOverride = null;
    });
  });
}
