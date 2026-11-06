import Foundation
import React
import Security

@objc(YeounAuth)
final class YeounAuth: NSObject {
    @objc static func requiresMainQueueSetup() -> Bool { false }

    @objc(createPkce:rejecter:)
    func createPkce(_ resolve: RCTPromiseResolveBlock, rejecter reject: RCTPromiseRejectBlock) {
        var bytes = [UInt8](repeating: 0, count: 32)
        guard SecRandomCopyBytes(kSecRandomDefault, bytes.count, &bytes) == errSecSuccess else {
            reject("PKCE_UNAVAILABLE", "안전한 계정 연결을 시작하지 못했어요.", nil)
            return
        }
        do {
            let verifier = try PKCE.verifier(entropy: Data(bytes))
            resolve(["verifier": verifier, "challenge": try PKCE.challenge(verifier: verifier)])
        } catch {
            reject("PKCE_UNAVAILABLE", "안전한 계정 연결을 시작하지 못했어요.", nil)
        }
    }
}
