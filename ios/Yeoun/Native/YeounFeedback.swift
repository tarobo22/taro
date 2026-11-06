import AVFoundation
import React
import UIKit

@objc(YeounFeedback)
final class YeounFeedback: NSObject {
    private let selectionFeedback = UISelectionFeedbackGenerator()
    private let impactFeedback = UIImpactFeedbackGenerator(style: .soft)
    private var sound = false
    private var haptics = true
    private var musicEnabled = false
    private var captureMuted = false
    private var effects: [AVAudioPlayer] = []
    private var music: AVAudioPlayer?
    private var observers: [NSObjectProtocol] = []

    override init() {
        super.init()
        for name in [UIApplication.didEnterBackgroundNotification, UIApplication.willResignActiveNotification] {
            observers.append(NotificationCenter.default.addObserver(forName: name, object: nil, queue: .main) { [weak self] _ in self?.stopAudio() })
        }
        observers.append(NotificationCenter.default.addObserver(forName: UIApplication.didBecomeActiveNotification, object: nil, queue: .main) { [weak self] _ in self?.updateMusic() })
    }

    deinit { observers.forEach(NotificationCenter.default.removeObserver) }
    @objc static func requiresMainQueueSetup() -> Bool { true }
    @objc var methodQueue: DispatchQueue { .main }

    @objc func configure(_ sound: Bool, haptics: Bool, background: Bool) {
        self.sound = sound
        self.haptics = haptics
        musicEnabled = background
        if !sound { effects.forEach { $0.stop() }; effects.removeAll() }
        updateMusic()
    }

    @objc func setCaptureMuted(_ muted: Bool) {
        captureMuted = muted
        if muted { stopAudio() } else { updateMusic() }
    }

    @objc func play(_ kind: String) {
        guard !captureMuted, UIApplication.shared.applicationState == .active else { return }
        if haptics {
            if kind == "select" || kind == "tick" { selectionFeedback.selectionChanged(); selectionFeedback.prepare() }
            else if kind == "confirm" { UINotificationFeedbackGenerator().notificationOccurred(.success) }
            else { impactFeedback.impactOccurred(intensity: kind == "boundary" ? 0.25 : 0.4); impactFeedback.prepare() }
        }
        let names = ["tap": "soft_tap", "select": "soft_select", "confirm": "soft_confirm", "back": "light_tap", "tick": "light_tap", "boundary": "light_tap", "shuffle": "card_shuffle", "flip": "card_flip"]
        guard sound, let name = names[kind], let url = Bundle.main.url(forResource: name, withExtension: "wav", subdirectory: "Sounds") else { return }
        do {
            try AVAudioSession.sharedInstance().setCategory(.ambient, options: [.mixWithOthers])
            try AVAudioSession.sharedInstance().setActive(true)
            effects.removeAll(where: { !$0.isPlaying })
            if effects.count >= 3 { effects.removeFirst().stop() }
            let player = try AVAudioPlayer(contentsOf: url)
            player.volume = kind == "tick" ? 0.18 : 0.45
            effects.append(player)
            player.play()
        } catch { /* A missing output device never blocks the selected action. */ }
    }

    private func stopAudio() {
        effects.forEach { $0.stop() }
        effects.removeAll()
        music?.pause()
    }

    private func updateMusic() {
        guard musicEnabled, !captureMuted, UIApplication.shared.applicationState == .active else { music?.pause(); return }
        do {
            try AVAudioSession.sharedInstance().setCategory(.ambient, options: [.mixWithOthers])
            try AVAudioSession.sharedInstance().setActive(true)
            if music == nil, let url = Bundle.main.url(forResource: "tarot_room", withExtension: "m4a", subdirectory: "Sounds") {
                music = try AVAudioPlayer(contentsOf: url)
                music?.numberOfLoops = -1
                music?.volume = 0.08
            }
            music?.play()
        } catch { music?.pause() }
    }
}
