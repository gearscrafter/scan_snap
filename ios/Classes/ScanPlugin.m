#import "ScanPlugin.h"
#if __has_include(<scan_snap/scan_snap-Swift.h>)
#import <scan_snap/scan_snap-Swift.h>
#else
#import "scan_snap-Swift.h"
#endif

@implementation ScanPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftScanPlugin registerWithRegistrar:registrar];
}
@end
