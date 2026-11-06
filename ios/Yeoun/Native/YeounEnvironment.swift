import Foundation
import React
import UIKit

@objc(YeounEnvironment)
final class YeounEnvironment: NSObject {
    @objc static func requiresMainQueueSetup() -> Bool { false }

    static var apiBaseURL: URL? {
        guard let value = Bundle.main.object(forInfoDictionaryKey: "YeounAPIBaseURL") as? String,
              !value.contains("$("), let url = URL(string: value), url.host != nil else { return nil }
        return url
    }

    @objc func constantsToExport() -> [String: Any] {
        ["apiBaseUrl": Self.apiBaseURL?.absoluteString ?? ""]
    }
}

enum YeounPresentation {
    static func controller() -> UIViewController? {
        let windows = UIApplication.shared.connectedScenes.compactMap { $0 as? UIWindowScene }.flatMap(\.windows)
        var controller = windows.first(where: \.isKeyWindow)?.rootViewController
            ?? (UIApplication.shared.delegate as? AppDelegate)?.window?.rootViewController
        while let presented = controller?.presentedViewController { controller = presented }
        if let navigation = controller as? UINavigationController { return navigation.visibleViewController }
        if let tabs = controller as? UITabBarController { return tabs.selectedViewController }
        return controller
    }
}
