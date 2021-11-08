//
//  Security.swift
//  RetailUniversalApp
//

import UIKit
import Resolver

/// Top level security configuration object
public struct Security {
    /// An enum used to represent privacy view mode.
    public enum PrivacyMode {
        /// Blur with a style. Defaults to `.extraLight`
        case blur(UIBlurEffect.Style = .extraLight)
        /// Show launch screen.
        case launchScreen(UIStoryboard)
        /// Show an image
        case image(UIImage, UIView.ContentMode = .scaleAspectFill)
        /// Show a view
        case view(UIView)
    }
}

extension Security {
    /// Top level security configuration object.
    public struct Configuration {
        /// Initializer that loads the default configuration values.
        public init() {}

        /// Privacy mode to be applied before the application enters the background.
        /// Defaults to: `.blur()`, set to `nil` to not apply any privacy mode.
        public var privacyMode: PrivacyMode? = .blur()

        /// Whether to allow third party keyboards. Defaults to `false`.
        public var allowThirdPartyKeyboards = false
    }
}

internal extension Security {
    static func shouldAllowExtensionPoint(with identifier: UIApplication.ExtensionPointIdentifier) -> Bool {
        let security: Security.Configuration = Resolver.optional() ?? .init()
        return security.allowThirdPartyKeyboards || identifier != .keyboard
    }

    static func handleApplicationDidEnterBackground(_ application: UIApplication) {
        application.ignoreSnapshotOnNextApplicationLaunch()
        let security: Security.Configuration = Resolver.optional() ?? .init()
        if let window = application.keyWindow {
            addPrivacyView(with: security.privacyMode, in: window)
        }
    }

    static func handleApplicationWillEnterForeground(_ application: UIApplication) {
        if let window = application.keyWindow {
            removePrivacyView(in: window)
        }
    }

    private static func addPrivacyView(with mode: PrivacyMode?, in view: UIView) {
        guard let mode = mode else { return }

        let privacyView: UIView

        switch mode {
        case let .blur(style):
            let blurEffect = UIBlurEffect(style: style)
            privacyView = UIVisualEffectView(effect: blurEffect)
        case let .launchScreen(storyboard):
            privacyView = storyboard.instantiateInitialViewController()?.view ?? UIView()
        case let .image(image, contentMode):
            privacyView = UIImageView(image: image)
            privacyView.contentMode = contentMode
        case let .view(view):
            privacyView = view
        }

        privacyView.frame = view.bounds
        privacyView.tag = Security.privacyViewTag

        view.addSubview(privacyView)
        view.bringSubviewToFront(privacyView)
    }

    private static func removePrivacyView(in view: UIView) {
        if let privacyView = view.viewWithTag(Security.privacyViewTag) {
            privacyView.removeFromSuperview()
        }
    }

    private static let privacyViewTag = 6471
}
