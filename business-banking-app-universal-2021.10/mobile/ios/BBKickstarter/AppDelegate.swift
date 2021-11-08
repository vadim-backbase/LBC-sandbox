//
//  Created by Backbase R&D B.V. on 21/01/2020.
//

import UIKit
import Backbase
import BusinessUniversalApp

@UIApplicationMain
class AppDelegate: BusinessUniversalAppDelegate {
    override init() {
        super.init { (sdk, design) in
            sdk.configPath = "config.json"

            return { appConfig in
                appConfig.splashScreen.backgroundImage = UIImage(named: "splash_background")
                appConfig.splashScreen.logoImage = UIImage(named: "bb-full-logo")
                appConfig.accountsAndTransactionsJourney.accounts.accountsList.navigationTitleView = .fullLogo( (name: "bb-full-logo", bundle: Bundle.main))
                appConfig.omniPaymentsJourney = JourneysConfiguration.omniPayments

                #if !DEBUG
                    Backbase.denyWhenJailbroken()
                    Backbase.denyWhenReverseEngineered()
                #endif
            }
        }
    }

    override func applicationDidEnterBackground(_ application: UIApplication) {
        super.applicationDidEnterBackground(application)
        Security.handleApplicationDidEnterBackground(application)
    }

    override func applicationWillEnterForeground(_ application: UIApplication) {
        super.applicationWillEnterForeground(application)
        Security.handleApplicationWillEnterForeground(application)
    }

    override func application(_ application: UIApplication, shouldAllowExtensionPointIdentifier extensionPointIdentifier: UIApplication.ExtensionPointIdentifier) -> Bool {

        return super.application(application, shouldAllowExtensionPointIdentifier: extensionPointIdentifier) &&
            Security.shouldAllowExtensionPoint(with: extensionPointIdentifier)
    }
}
