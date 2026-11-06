import AVFoundation
import React
import Speech
import UIKit

@objc(YeounSpeech)
final class YeounSpeech: RCTEventEmitter {
    private var engine: AVAudioEngine?
    private var request: SFSpeechAudioBufferRecognitionRequest?
    private var recognition: SFSpeechRecognitionTask?
    private var captures = CaptureGeneration()
    private var currentID: String?
    private var observer: NSObjectProtocol?

    override init() {
        super.init()
        observer = NotificationCenter.default.addObserver(forName: UIApplication.didEnterBackgroundNotification, object: nil, queue: .main) { [weak self] _ in self?.stop() }
    }

    deinit { if let observer { NotificationCenter.default.removeObserver(observer) } }
    @objc override static func requiresMainQueueSetup() -> Bool { true }
    override func supportedEvents() -> [String]! { ["YeounSpeech"] }

    @objc func isAvailable(_ resolve: RCTPromiseResolveBlock, rejecter reject: RCTPromiseRejectBlock) {
        resolve(SFSpeechRecognizer(locale: Locale(identifier: "ko-KR"))?.isAvailable == true)
    }

    @objc func startCapture(_ language: String, captureId: String) {
        DispatchQueue.main.async { [weak self] in self?.beginCapture(language, captureId: captureId) }
    }

    private func beginCapture(_ language: String, captureId: String) {
        stopResources()
        currentID = captureId
        captures.begin(captureId)
        SFSpeechRecognizer.requestAuthorization { [weak self] permission in
            DispatchQueue.main.async {
                guard let self, self.captures.isCurrent(captureId) else { return }
                guard permission == .authorized else { self.finish(captureId, message: "음성 인식 권한 없이도 글로 이야기할 수 있어요."); return }
                AVAudioSession.sharedInstance().requestRecordPermission { [weak self] granted in
                    DispatchQueue.main.async {
                        guard let self, self.captures.isCurrent(captureId) else { return }
                        guard granted else { self.finish(captureId, message: "마이크 권한 없이도 글로 이야기할 수 있어요."); return }
                        self.record(language: language, id: captureId)
                    }
                }
            }
        }
    }

    private func record(language: String, id: String) {
        guard let recognizer = SFSpeechRecognizer(locale: Locale(identifier: language)), recognizer.isAvailable else { finish(id, message: "지금은 음성 입력을 사용할 수 없어요. 글로 이어갈 수 있어요."); return }
        do {
            try AVAudioSession.sharedInstance().setCategory(.record, mode: .measurement, options: [.duckOthers])
            try AVAudioSession.sharedInstance().setActive(true, options: .notifyOthersOnDeactivation)
            let engine = AVAudioEngine()
            let request = SFSpeechAudioBufferRecognitionRequest()
            request.shouldReportPartialResults = true
            let input = engine.inputNode
            let format = input.outputFormat(forBus: 0)
            guard format.channelCount > 0, format.sampleRate > 0 else { finish(id, message: "마이크를 확인해 주세요."); return }
            self.engine = engine
            self.request = request
            input.installTap(onBus: 0, bufferSize: 1024, format: format) { [weak request] buffer, _ in request?.append(buffer) }
            recognition = recognizer.recognitionTask(with: request) { [weak self] result, error in
                DispatchQueue.main.async {
                    guard let self, self.captures.isCurrent(id) else { return }
                    if let result {
                        self.sendEvent(withName: "YeounSpeech", body: ["captureId": id, "text": result.bestTranscription.formattedString, "final": result.isFinal])
                        if result.isFinal { self.stopResources(); return }
                    }
                    if error != nil { self.finish(id, message: "음성 입력이 끝났어요. 내용을 확인해 주세요.") }
                }
            }
            engine.prepare()
            try engine.start()
            sendEvent(withName: "YeounSpeech", body: ["captureId": id, "message": "듣고 있어요."])
        } catch { finish(id, message: "음성 입력을 시작하지 못했어요. 글로 이어갈 수 있어요.") }
    }

    private func finish(_ id: String, message: String) {
        guard captures.isCurrent(id) else { return }
        sendEvent(withName: "YeounSpeech", body: ["captureId": id, "final": true, "message": message])
        stopResources()
    }

    @objc func stop() {
        DispatchQueue.main.async { [weak self] in self?.stopOnMainQueue() }
    }

    private func stopOnMainQueue() {
        if let id = currentID { finish(id, message: "음성 입력을 마쳤어요.") }
        else { stopResources() }
    }

    private func stopResources() {
        if let id = currentID { _ = captures.end(id) }
        currentID = nil
        engine?.stop()
        if engine != nil { engine?.inputNode.removeTap(onBus: 0) }
        request?.endAudio()
        recognition?.cancel()
        recognition = nil
        request = nil
        engine = nil
        try? AVAudioSession.sharedInstance().setActive(false, options: .notifyOthersOnDeactivation)
    }
}
