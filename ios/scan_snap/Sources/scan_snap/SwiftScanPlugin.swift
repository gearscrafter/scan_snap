import Flutter
import UIKit
import Vision

public class SwiftScanPlugin: NSObject, FlutterPlugin {
  public static func register(with registrar: FlutterPluginRegistrar) {
    let channel = FlutterMethodChannel(name: "scan_snap/scan", binaryMessenger: registrar.messenger())
    let instance = SwiftScanPlugin()
    registrar.addMethodCallDelegate(instance, channel: channel)
    
    registrar.register(ScanViewFactory(registrar: registrar), withId: "scan_snap/scan_view");
  }

  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
    if call.method=="getPlatformVersion" {
      result("iOS " + UIDevice.current.systemVersion)
    }else if call.method == "parse" {
        guard let args = call.arguments as? [String: Any],
              let path = args["path"] as? String else {
            result(FlutterError(code: "INVALID_ARGUMENTS",
                                message: "'path' argument is required and must be a String.",
                                details: nil))
            return
        }

        guard let image = UIImage(contentsOfFile: path) else {
            result(FlutterError(code: "IMAGE_LOAD_FAILED",
                                message: "Failed to load image from the provided path.",
                                details: path))
            return
        }
        if let features = self.detectQRCode(image), !features.isEmpty {
            if let qrFeature = features.first as? CIQRCodeFeature, let message = qrFeature.messageString {
                result(message)
                return 
            }
        }

        self.detectBarCode(image, result: result)
    }
  }
  
  private func detectQRCode(_ image: UIImage?) -> [CIFeature]? {
    if let image = image, let ciImage = CIImage.init(image: image){
      var options: [String: Any];
      let context = CIContext();
      options = [CIDetectorAccuracy: CIDetectorAccuracyHigh];
      let qrDetector = CIDetector(ofType: CIDetectorTypeQRCode, context: context, options: options);
      if ciImage.properties.keys.contains((kCGImagePropertyOrientation as String)){
        options = [CIDetectorImageOrientation: ciImage.properties[(kCGImagePropertyOrientation as String)] ?? 1];
      } else {
        options = [CIDetectorImageOrientation: 1];
      }
      let features = qrDetector?.features(in: ciImage, options: options);
      return features;
    }
    return nil
  }
  
  private func detectBarCode(_ image: UIImage?, result: @escaping FlutterResult) {
    if let image = image, let ciImage = CIImage.init(image: image), #available(iOS 11.0, *) {
      var requestHandler: VNImageRequestHandler;
      if ciImage.properties.keys.contains((kCGImagePropertyOrientation as String)) {
        requestHandler = VNImageRequestHandler(ciImage: ciImage, orientation: CGImagePropertyOrientation(rawValue: ciImage.properties[(kCGImagePropertyOrientation as String)] as! UInt32) ?? .up, options: [:])
      } else {
        requestHandler = VNImageRequestHandler(ciImage: ciImage, orientation: .up, options: [:])
      }
      let request = VNDetectBarcodesRequest { (request,error) in
        var res: String? = nil;
        if let observations = request.results as? [VNBarcodeObservation], !observations.isEmpty {
          let data: VNBarcodeObservation = observations.first!;
          res = data.payloadStringValue;
        }
        DispatchQueue.main.async {
          result(res);
        }
      }
      DispatchQueue.global(qos: .background).async {
        do{
          try requestHandler.perform([request])
        } catch {
          DispatchQueue.main.async {
            result(nil);
          }
        }
      }
    } else {
      result(nil);
    }
  }
}
