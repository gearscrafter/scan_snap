import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:scan_snap/scan.dart';
import 'package:image_picker/image_picker.dart';
import 'package:scan_example/scan.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';
  final ImagePicker _picker = ImagePicker();
  String qrcode = 'Unknown';

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  Future<void> initPlatformState() async {
    String platformVersion;
    try {
      platformVersion = await Scan.platformVersion;
    } on PlatformException {
      platformVersion = 'Failed to get platform version.';
    }
    if (!mounted) return;

    setState(() {
      _platformVersion = platformVersion;
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      initialRoute: '/',
      routes: {
        '/': (context) => Scaffold(
              appBar: AppBar(
                title: const Text('Plugin example app'),
              ),
              body: Builder(builder: (context) {
                return Column(
                  children: [
                    Text('Running on: $_platformVersion\n'),
                    Wrap(
                      children: [
                        ElevatedButton(
                          child: Text("parse from image"),
                          onPressed: () async {
                            final List<XFile> res =
                                await _picker.pickMultiImage();
                            String? str = await Scan.parse(res[0].path);
                            if (str != null) {
                              setState(() {
                                qrcode = str;
                              });
                            }
                          },
                        ),
                        ElevatedButton(
                          child: Text('go scan page'),
                          onPressed: () async {
                            final result = await Navigator.of(context)
                                .push(PageRouteBuilder(
                              pageBuilder:
                                  (context, animation, secondaryAnimation) =>
                                      ScanPage(),
                              transitionDuration: Duration.zero,
                              reverseTransitionDuration:
                                  Duration(milliseconds: 100),
                              transitionsBuilder: (context, animation,
                                  secondaryAnimation, child) {
                                return FadeTransition(
                                  opacity: animation,
                                  child: child,
                                );
                              },
                            ));
                            setState(() {
                              qrcode = result.toString();
                            });
                          },
                        ),
                      ],
                    ),
                    Text('scan result is $qrcode'),
                  ],
                );
              }),
            ),
      },
    );
  }
}
