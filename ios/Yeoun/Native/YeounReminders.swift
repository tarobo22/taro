import Foundation
import React
import UIKit
import UserNotifications

@objc(YeounReminders)
final class YeounReminders: NSObject {
    private let center = UNUserNotificationCenter.current()
    private let defaults = UserDefaults.standard
    private let identifier = "yeoun.daily.reminder"
    private let hourKey = "yeoun.reminder.hour"
    private let minuteKey = "yeoun.reminder.minute"
    private let enabledKey = "yeoun.reminder.enabled"

    @objc static func requiresMainQueueSetup() -> Bool { false }

    private func granted(_ settings: UNNotificationSettings) -> Bool {
        [.authorized, .provisional, .ephemeral].contains(settings.authorizationStatus)
    }

    private func state(_ resolve: @escaping RCTPromiseResolveBlock) {
        center.getNotificationSettings { [weak self] settings in
            guard let self else { return }
            self.center.getPendingNotificationRequests { requests in
                let request = requests.first(where: { $0.identifier == self.identifier })
                let allowed = self.granted(settings)
                let next = (request?.trigger as? UNCalendarNotificationTrigger)?.nextTriggerDate()
                resolve(["permission": allowed ? "granted" : "denied",
                    "enabled": allowed && request != nil && self.defaults.bool(forKey: self.enabledKey),
                    "hour": self.defaults.object(forKey: self.hourKey) ?? NSNull(),
                    "minute": self.defaults.object(forKey: self.minuteKey) ?? NSNull(),
                    "nextAt": next.map { $0.timeIntervalSince1970 * 1000 } as Any? ?? NSNull()])
            }
        }
    }

    @objc func read(_ resolve: @escaping RCTPromiseResolveBlock, rejecter reject: RCTPromiseRejectBlock) { state(resolve) }

    @objc func requestPermission(_ resolve: @escaping RCTPromiseResolveBlock, rejecter reject: @escaping RCTPromiseRejectBlock) {
        center.getNotificationSettings { [weak self] settings in
            guard let self else { return }
            if self.granted(settings) { resolve("granted"); return }
            if settings.authorizationStatus == .denied { resolve("blocked"); return }
            self.center.requestAuthorization(options: [.alert, .sound]) { granted, error in
                if error != nil { reject("NOTIFICATION_PERMISSION", "알림 설정을 확인하지 못했어요.", nil) }
                else { resolve(granted ? "granted" : "denied") }
            }
        }
    }

    @objc func schedule(_ hour: Double, minute: Double, resolver resolve: @escaping RCTPromiseResolveBlock, rejecter reject: @escaping RCTPromiseRejectBlock) {
        guard hour.isFinite, minute.isFinite, hour.rounded(.towardZero) == hour, minute.rounded(.towardZero) == minute,
              hour >= 0, hour < 24, minute >= 0, minute < 60 else {
            reject("INVALID_REMINDER_TIME", "알림 시각을 확인해 주세요.", nil); return
        }
        center.getNotificationSettings { [weak self] settings in
            guard let self else { return }
            guard self.granted(settings) else { reject("NOTIFICATION_PERMISSION", "알림 권한을 먼저 허용해 주세요.", nil); return }
            let content = UNMutableNotificationContent()
            content.title = "여운"
            content.body = "잠깐, 오늘의 마음을 살펴볼까요?"
            content.sound = .default
            // No fixed timezone: the system follows local wall-clock time, including DST.
            var time = DateComponents()
            time.hour = Int(hour)
            time.minute = Int(minute)
            let request = UNNotificationRequest(identifier: self.identifier, content: content,
                trigger: UNCalendarNotificationTrigger(dateMatching: time, repeats: true))
            self.center.add(request) { error in
                if error != nil { reject("REMINDER_SCHEDULE", "알림을 예약하지 못했어요.", nil); return }
                self.defaults.set(Int(hour), forKey: self.hourKey)
                self.defaults.set(Int(minute), forKey: self.minuteKey)
                self.defaults.set(true, forKey: self.enabledKey)
                self.state(resolve)
            }
        }
    }

    @objc func disable(_ resolve: @escaping RCTPromiseResolveBlock, rejecter reject: RCTPromiseRejectBlock) {
        center.removePendingNotificationRequests(withIdentifiers: [identifier])
        defaults.set(false, forKey: enabledKey)
        state(resolve)
    }

    @objc func clear(_ resolve: @escaping RCTPromiseResolveBlock, rejecter reject: RCTPromiseRejectBlock) {
        center.removePendingNotificationRequests(withIdentifiers: [identifier, identifier + ".preview"])
        center.removeDeliveredNotifications(withIdentifiers: [identifier, identifier + ".preview"])
        [hourKey, minuteKey, enabledKey].forEach(defaults.removeObject(forKey:))
        state(resolve)
    }

    @objc func openSettings(_ resolve: @escaping RCTPromiseResolveBlock, rejecter reject: RCTPromiseRejectBlock) {
        DispatchQueue.main.async {
            guard let url = URL(string: UIApplication.openSettingsURLString) else { resolve(NSNull()); return }
            UIApplication.shared.open(url) { _ in resolve(NSNull()) }
        }
    }

    @objc func preview(_ resolve: @escaping RCTPromiseResolveBlock, rejecter reject: @escaping RCTPromiseRejectBlock) {
        center.getNotificationSettings { [weak self] settings in
            guard let self else { return }
            guard self.granted(settings) else { reject("NOTIFICATION_PERMISSION", "알림 권한을 먼저 허용해 주세요.", nil); return }
            let content = UNMutableNotificationContent()
            content.title = "여운"
            content.body = "잠깐, 오늘의 마음을 살펴볼까요?"
            content.sound = .default
            self.center.add(UNNotificationRequest(identifier: self.identifier + ".preview", content: content, trigger: nil)) { error in
                if error != nil { reject("REMINDER_PREVIEW", "알림을 확인하지 못했어요.", nil) }
                else { resolve(NSNull()) }
            }
        }
    }
}
