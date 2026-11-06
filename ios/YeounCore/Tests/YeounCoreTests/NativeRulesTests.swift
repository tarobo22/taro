import Foundation
import XCTest
@testable import YeounCore

final class NativeRulesTests: XCTestCase {
    func testPKCEUsesTheRFC7636S256Vector() throws {
        // https://www.rfc-editor.org/rfc/rfc7636#appendix-B
        XCTAssertEqual(try PKCE.challenge(verifier: "dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk"),
            "E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM")
    }

    func testVerifierRequiresEnoughEntropyAndNeverContainsPaddingOrStandardBase64Symbols() throws {
        let verifier = try PKCE.verifier(entropy: Data(repeating: 255, count: 32))
        XCTAssertEqual(verifier.count, 43)
        XCTAssertTrue(verifier.allSatisfy { $0.isASCII && ($0.isLetter || $0.isNumber || $0 == "-" || $0 == "_") })
        XCTAssertThrowsError(try PKCE.verifier(entropy: Data(repeating: 0, count: 16)))
        XCTAssertThrowsError(try PKCE.challenge(verifier: "short"))
        XCTAssertThrowsError(try PKCE.challenge(verifier: String(repeating: "가", count: 50)))
    }

    func testAnOldSpeechCallbackCannotEndOrWriteIntoANewCapture() {
        var captures = CaptureGeneration()
        captures.begin("first")
        XCTAssertTrue(captures.isCurrent("first"))
        captures.begin("second")
        XCTAssertFalse(captures.isCurrent("first"))
        XCTAssertFalse(captures.end("first"))
        XCTAssertTrue(captures.isCurrent("second"))
        XCTAssertTrue(captures.end("second"))
        XCTAssertFalse(captures.isCurrent("second"))
    }

    func testStreamLimitsRejectOverflowWithoutChangingTheAcceptedByteCount() throws {
        var budget = try TransferBudget(limit: 10)
        try budget.append(7)
        try budget.append(3)
        XCTAssertEqual(budget.received, 10)
        XCTAssertThrowsError(try budget.append(1))
        XCTAssertEqual(budget.received, 10)
        XCTAssertThrowsError(try budget.append(Int.max))
        XCTAssertThrowsError(try TransferBudget(limit: 0))
    }

    func testPrivateExportAuthorizationIsRestrictedToItsConfiguredOrigin() {
        let policy = PrivateDownloadPolicy(origin: URL(string: "https://api.example.test")!)
        XCTAssertTrue(policy.accepts(URL(string: "https://api.example.test/v2/data-requests/id/download")!))
        XCTAssertTrue(policy.accepts(URL(string: "https://api.example.test:443/v2/data-requests/id/download")!))
        XCTAssertFalse(policy.accepts(URL(string: "https://other.example.test/v2/data-requests/id/download")!))
        XCTAssertFalse(policy.accepts(URL(string: "http://api.example.test/v2/data-requests/id/download")!))
        XCTAssertFalse(policy.accepts(URL(string: "https://api.example.test:8443/v2/data-requests/id/download")!))
        XCTAssertFalse(policy.accepts(URL(string: "https://user:fixture@api.example.test/v2/data-requests/id/download")!))
    }

    func testInsecureDevelopmentDownloadsAreLimitedToTheSameLoopbackOrigin() {
        let local = PrivateDownloadPolicy(origin: URL(string: "http://localhost:8808")!)
        XCTAssertTrue(local.accepts(URL(string: "http://localhost:8808/v2/data-requests/id/download")!))
        XCTAssertFalse(local.accepts(URL(string: "http://localhost:8809/v2/data-requests/id/download")!))
        let remote = PrivateDownloadPolicy(origin: URL(string: "http://api.example.test")!)
        XCTAssertFalse(remote.accepts(URL(string: "http://api.example.test/v2/data-requests/id/download")!))
    }
}
