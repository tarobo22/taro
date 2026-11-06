import AVFoundation
import Foundation
import React
import UIKit
import UniformTypeIdentifiers

@objc(YeounFiles)
final class YeounFiles: NSObject, UIDocumentPickerDelegate {
    private var pickerResolve: RCTPromiseResolveBlock?
    private var pickerReject: RCTPromiseRejectBlock?
    private var audioLimit = 0
    private var durationLimit: Double = 0
    private var activeExport: PrivateExportDownload?

    @objc static func requiresMainQueueSetup() -> Bool { true }
    @objc var methodQueue: DispatchQueue { .main }

    @objc func chooseAudio(_ maxBytes: Double, maxDurationMs: Double,
        resolver resolve: @escaping RCTPromiseResolveBlock, rejecter reject: @escaping RCTPromiseRejectBlock) {
        guard pickerResolve == nil, activeExport == nil else { reject("FILE_BUSY", "진행 중인 파일 선택을 먼저 마쳐 주세요.", nil); return }
        guard maxBytes.isFinite, maxBytes > 0, maxBytes <= 64 * 1024 * 1024,
              maxDurationMs.isFinite, maxDurationMs > 0, let presenter = YeounPresentation.controller() else {
            reject("FILE_UNAVAILABLE", "파일 선택을 시작하지 못했어요.", nil); return
        }
        audioLimit = Int(maxBytes)
        durationLimit = maxDurationMs
        pickerResolve = resolve
        pickerReject = reject
        let picker = UIDocumentPickerViewController(forOpeningContentTypes: [.audio], asCopy: true)
        picker.allowsMultipleSelection = false
        picker.delegate = self
        presenter.present(picker, animated: true)
    }

    func documentPickerWasCancelled(_ controller: UIDocumentPickerViewController) {
        pickerResolve?(NSNull())
        pickerResolve = nil
        pickerReject = nil
    }

    func documentPicker(_ controller: UIDocumentPickerViewController, didPickDocumentsAt urls: [URL]) {
        let resolve = pickerResolve
        let reject = pickerReject
        pickerResolve = nil
        pickerReject = nil
        guard let source = urls.first else { resolve?(NSNull()); return }
        let limit = audioLimit, durationLimit = durationLimit
        Task {
            let accessed = source.startAccessingSecurityScopedResource()
            defer { if accessed { source.stopAccessingSecurityScopedResource() } }
            var copied: URL?
            do {
                let values = try source.resourceValues(forKeys: [.fileSizeKey])
                guard let bytes = values.fileSize, bytes > 0, bytes <= limit else { throw NativeRuleFailure.limitExceeded }
                let duration = try await AVURLAsset(url: source).load(.duration).seconds * 1000
                guard duration.isFinite, duration > 0, duration <= durationLimit else { throw NativeRuleFailure.limitExceeded }
                let types = ["mp3": "audio/mpeg", "m4a": "audio/mp4", "mp4": "audio/mp4", "aac": "audio/aac", "wav": "audio/wav", "ogg": "audio/ogg", "webm": "audio/webm"]
                let ext = source.pathExtension.lowercased()
                guard let mime = types[ext] else { throw NativeRuleFailure.invalidLimit }
                let directory = try FileManager.default.url(for: .cachesDirectory, in: .userDomainMask, appropriateFor: nil, create: true)
                    .appendingPathComponent("yeoun-audio", isDirectory: true)
                try FileManager.default.createDirectory(at: directory, withIntermediateDirectories: true)
                var destination = directory.appendingPathComponent(UUID().uuidString).appendingPathExtension(ext)
                copied = destination
                FileManager.default.createFile(atPath: destination.path, contents: nil,
                    attributes: [.protectionKey: FileProtectionType.completeUntilFirstUserAuthentication])
                let input = try FileHandle(forReadingFrom: source)
                let output = try FileHandle(forWritingTo: destination)
                defer { try? input.close(); try? output.close() }
                var budget = try TransferBudget(limit: limit)
                while let data = try input.read(upToCount: 64 * 1024), !data.isEmpty {
                    try budget.append(data.count)
                    try output.write(contentsOf: data)
                }
                guard budget.received > 0 else { throw NativeRuleFailure.invalidLimit }
                var metadata = URLResourceValues()
                metadata.isExcludedFromBackup = true
                try destination.setResourceValues(metadata)
                resolve?(["uri": destination.absoluteString, "name": "voice." + ext, "mimeType": mime,
                    "sizeBytes": budget.received, "durationMs": duration])
            } catch {
                if let copied { try? FileManager.default.removeItem(at: copied) }
                reject?("AUDIO_FILE_INVALID", "지원되는 음성 파일을 60초·10MiB 이내로 선택해 주세요.", nil)
            }
        }
    }

    @objc func downloadPrivate(_ address: String, bearer: String, filename: String, maxBytes: Double,
        resolver resolve: @escaping RCTPromiseResolveBlock, rejecter reject: @escaping RCTPromiseRejectBlock) {
        guard activeExport == nil, pickerResolve == nil else { reject("FILE_BUSY", "진행 중인 파일 작업을 먼저 마쳐 주세요.", nil); return }
        guard let origin = YeounEnvironment.apiBaseURL, let url = URL(string: address),
              PrivateDownloadPolicy(origin: origin).accepts(url),
              maxBytes.isFinite, maxBytes > 0, maxBytes <= 64 * 1024 * 1024,
              filename.range(of: "^[A-Za-z0-9._-]{1,128}\\.zip$", options: .regularExpression) != nil,
              !bearer.isEmpty else { reject("EXPORT_INVALID", "내보내기 연결을 확인해 주세요.", nil); return }
        do {
            let export = try PrivateExportDownload(url: url, bearer: bearer, filename: filename,
                limit: Int(maxBytes), resolve: resolve, reject: reject, done: { [weak self] in self?.activeExport = nil })
            activeExport = export
            export.start()
        } catch { reject("EXPORT_UNAVAILABLE", "파일을 준비하지 못했어요. 다시 시도해 주세요.", nil) }
    }
}

private final class PrivateExportDownload: NSObject, URLSessionDataDelegate, URLSessionTaskDelegate {
    private let url: URL
    private let bearer: String
    private let file: URL
    private let directory: URL
    private let output: FileHandle
    private var budget: TransferBudget
    private var expected: Int64 = -1
    private let resolve: RCTPromiseResolveBlock
    private let reject: RCTPromiseRejectBlock
    private let done: () -> Void
    private var completed = false
    private var session: URLSession?

    init(url: URL, bearer: String, filename: String, limit: Int,
        resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock, done: @escaping () -> Void) throws {
        self.url = url
        self.bearer = bearer
        self.resolve = resolve
        self.reject = reject
        self.done = done
        self.budget = try TransferBudget(limit: limit)
        directory = try FileManager.default.url(for: .cachesDirectory, in: .userDomainMask, appropriateFor: nil, create: true)
            .appendingPathComponent("yeoun-export-" + UUID().uuidString, isDirectory: true)
        try FileManager.default.createDirectory(at: directory, withIntermediateDirectories: true)
        file = directory.appendingPathComponent(filename)
        FileManager.default.createFile(atPath: file.path, contents: nil,
            attributes: [.protectionKey: FileProtectionType.completeUntilFirstUserAuthentication])
        output = try FileHandle(forWritingTo: file)
        super.init()
    }

    func start() {
        let configuration = URLSessionConfiguration.ephemeral
        configuration.urlCache = nil
        configuration.httpCookieStorage = nil
        configuration.httpShouldSetCookies = false
        configuration.timeoutIntervalForRequest = 30
        configuration.timeoutIntervalForResource = 60
        let session = URLSession(configuration: configuration, delegate: self, delegateQueue: nil)
        self.session = session
        var request = URLRequest(url: url)
        request.setValue("Bearer " + bearer, forHTTPHeaderField: "Authorization")
        request.setValue("application/zip", forHTTPHeaderField: "Accept")
        session.dataTask(with: request).resume()
    }

    func urlSession(_ session: URLSession, task: URLSessionTask, willPerformHTTPRedirection response: HTTPURLResponse,
        newRequest request: URLRequest, completionHandler: @escaping (URLRequest?) -> Void) {
        completionHandler(nil) // Never forward the export's bearer to a redirect target.
    }

    func urlSession(_ session: URLSession, dataTask: URLSessionDataTask, didReceive response: URLResponse,
        completionHandler: @escaping (URLSession.ResponseDisposition) -> Void) {
        guard let http = response as? HTTPURLResponse, http.statusCode == 200,
              response.mimeType == "application/zip" else { completionHandler(.cancel); fail(); return }
        expected = response.expectedContentLength
        completionHandler(.allow)
    }

    func urlSession(_ session: URLSession, dataTask: URLSessionDataTask, didReceive data: Data) {
        guard !completed else { return }
        do { try budget.append(data.count); try output.write(contentsOf: data) }
        catch { fail() }
    }

    func urlSession(_ session: URLSession, task: URLSessionTask, didCompleteWithError error: Error?) {
        guard !completed else { return }
        guard error == nil, budget.received > 0, expected < 0 || expected == Int64(budget.received) else { fail(); return }
        try? output.close()
        session.finishTasksAndInvalidate()
        DispatchQueue.main.async { [self] in
            guard UIApplication.shared.applicationState == .active, let presenter = YeounPresentation.controller() else { finish(false); return }
            let sheet = UIActivityViewController(activityItems: [file], applicationActivities: nil)
            sheet.popoverPresentationController?.sourceView = presenter.view
            sheet.popoverPresentationController?.sourceRect = CGRect(x: presenter.view.bounds.midX, y: presenter.view.bounds.midY, width: 1, height: 1)
            sheet.completionWithItemsHandler = { [self] _, completed, _, _ in finish(completed) }
            presenter.present(sheet, animated: true)
        }
    }

    private func clean() {
        try? output.close()
        try? FileManager.default.removeItem(at: directory)
    }

    private func finish(_ success: Bool) {
        guard !completed else { return }
        completed = true
        clean()
        resolve(success)
        done()
    }

    private func fail() {
        guard !completed else { return }
        completed = true
        session?.invalidateAndCancel()
        clean()
        DispatchQueue.main.async { [self] in
            reject("EXPORT_FAILED", "파일을 저장하지 못했어요. 연결과 용량을 확인한 뒤 다시 시도해 주세요.", nil)
            done()
        }
    }
}
