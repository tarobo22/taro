// swift-tools-version: 6.0
import PackageDescription

let package = Package(
    name: "YeounCore",
    platforms: [.macOS(.v13), .iOS(.v15)],
    products: [.library(name: "YeounCore", targets: ["YeounCore"])],
    dependencies: [.package(url: "https://github.com/apple/swift-crypto.git", exact: "3.12.3")],
    targets: [
        .target(name: "YeounCore", dependencies: [.product(name: "Crypto", package: "swift-crypto")]),
        .testTarget(name: "YeounCoreTests", dependencies: ["YeounCore"])
    ]
)
