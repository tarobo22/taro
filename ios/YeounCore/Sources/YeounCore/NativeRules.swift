import Foundation
#if canImport(CryptoKit)
import CryptoKit
#else
import Crypto
#endif

public enum NativeRuleFailure: Error { case invalidEntropy, invalidVerifier, invalidLimit, limitExceeded }

public enum PKCE {
    public static func verifier(entropy: Data) throws -> String {
        guard (32...96).contains(entropy.count) else { throw NativeRuleFailure.invalidEntropy }
        return base64URL(entropy)
    }
    public static func challenge(verifier: String) throws -> String {
        let allowed = Set("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-._~".utf8)
        guard (43...128).contains(verifier.utf8.count), verifier.utf8.allSatisfy({ allowed.contains($0) }) else {
            throw NativeRuleFailure.invalidVerifier
        }
        return base64URL(Data(SHA256.hash(data: Data(verifier.utf8))))
    }
    private static func base64URL(_ bytes: Data) -> String {
        bytes.base64EncodedString().replacingOccurrences(of: "+", with: "-")
            .replacingOccurrences(of: "/", with: "_").replacingOccurrences(of: "=", with: "")
    }
}

public struct CaptureGeneration {
    private var current: String?
    public init() {}
    public mutating func begin(_ id: String) { current = id.isEmpty ? nil : id }
    public func isCurrent(_ id: String) -> Bool { !id.isEmpty && current == id }
    public mutating func end(_ id: String) -> Bool {
        guard isCurrent(id) else { return false }
        current = nil
        return true
    }
}

public struct TransferBudget {
    public private(set) var received = 0
    private let limit: Int
    public init(limit: Int) throws {
        guard limit > 0 else { throw NativeRuleFailure.invalidLimit }
        self.limit = limit
    }
    public mutating func append(_ count: Int) throws {
        guard count >= 0, count <= limit - received else { throw NativeRuleFailure.limitExceeded }
        received += count
    }
}

public struct PrivateDownloadPolicy {
    private let origin: URL
    public init(origin: URL) { self.origin = origin }
    public func accepts(_ url: URL) -> Bool {
        guard url.user == nil, url.password == nil, origin.user == nil, origin.password == nil,
              let scheme = origin.scheme?.lowercased(), let host = origin.host?.lowercased(),
              url.scheme?.lowercased() == scheme, url.host?.lowercased() == host else { return false }
        let port = scheme == "https" ? 443 : 80
        guard (url.port ?? port) == (origin.port ?? port) else { return false }
        return scheme == "https" || (scheme == "http" && ["localhost", "127.0.0.1", "::1", "[::1]"].contains(host))
    }
}
