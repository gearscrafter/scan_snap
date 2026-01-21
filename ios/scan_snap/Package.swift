// swift-tools-version: 6.2
import PackageDescription

let package = Package(
    name: "scan_snap",
    platforms: [
        .iOS(.v12),
        .macOS(.v10_15),
    ],
    products: [
        .library(
            name: "scan_snap",
            targets: ["scan_snap"]
        ),
    ],
    dependencies: [],
    targets: [
        .target(
            name: "scan_snap",
            dependencies: [],
            path: "Sources/scan_snap",
            exclude: [
                "ScanPlugin.m",
            ],
            publicHeadersPath: "include",
            cSettings: [
                .headerSearchPath("include"),
            ],
            linkerSettings: [
                .linkedFramework("Foundation"),
                .linkedFramework("UIKit"),
                .linkedFramework("AVFoundation"),
                .linkedFramework("Vision"),
            ]
        ),
    ]
)