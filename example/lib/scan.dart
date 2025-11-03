import 'package:flutter/material.dart';
import 'package:scan_snap/scan_snap.dart';

class ScanPage extends StatefulWidget {
  @override
  State<ScanPage> createState() => _ScanPageState();
}

class _ScanPageState extends State<ScanPage> {
  bool _isDisposing = false;

  final ScanController controller = ScanController();

  Future<void> _handlePop() async {
    if (_isDisposing) return;
    setState(() => _isDisposing = true);
    await controller.shutdown();
    if (mounted) Navigator.of(context).pop<String?>(null);
  }

  void _onCapture(String data) {
    if (mounted) {
      Navigator.of(context).pop(data);
    }
  }

  @override
  Widget build(BuildContext context) {
    return PopScope<String>(
      canPop: false,
      onPopInvokedWithResult: (bool didPop, _) {
        if (!didPop && !_isDisposing) {
          _handlePop();
        }
      },
      child: Scaffold(
        body: SafeArea(
          top: true,
          bottom: true,
          child: Stack(
            children: [
              ScanView(
                controller: controller,
                scanAreaScale: .7,
                scanLineColor: Colors.limeAccent,
                onCapture: _onCapture,
              ),
              Positioned(
                bottom: 0,
                child: Row(
                  children: [
                    ElevatedButton(
                        child: Text("toggleTorchMode"),
                        onPressed: controller.toggleTorchMode),
                    ElevatedButton(
                      child: Text("pause"),
                      onPressed: () {
                        controller.pause();
                      },
                    ),
                    ElevatedButton(
                      child: Text("resume"),
                      onPressed: () {
                        controller.resume();
                      },
                    ),
                  ],
                ),
              ),
              if (_isDisposing)
                Positioned.fill(
                  child: Container(
                    color: Colors.black.withValues(alpha: 0.5),
                    child: const Center(
                      child: CircularProgressIndicator(color: Colors.white),
                    ),
                  ),
                ),
            ],
          ),
        ),
      ),
    );
  }
}
