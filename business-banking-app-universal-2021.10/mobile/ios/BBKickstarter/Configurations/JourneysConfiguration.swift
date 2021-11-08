//
//  Created by Backbase R&D B.V. on 24/05/2020.
//

import BusinessApprovalsJourney
import BusinessMoreMenuJourney
import BusinessAccountsAndTransactionsJourney
import BusinessManagePaymentsJourney
import BusinessJourneyCommon
import BusinessUniversalApp
import BusinessOmniPaymentsJourney
import BusinessDesign
import BackbaseDesignSystem
import IdentityAuthenticationJourney

public class JourneysConfiguration {
    private static func localized(_ key: String) -> BusinessJourneyCommon.LocalizedString {
        return LocalizedString(key: key, in: Bundle(for: JourneysConfiguration.self))
    }

    public static let omniPayments: OmniPayments.Business.Configuration = {
        var businessPayments = OmniPayments.Business.Configuration()
        businessPayments.menuTitle = JourneysConfiguration.OmniPaymentsConfiguration.localized("omniPayments.config.transfer.title")()
        businessPayments.payments = [
            JourneysConfiguration.OmniPaymentsConfiguration.paymentWizard,
            JourneysConfiguration.OmniPaymentsConfiguration.sepa,
            JourneysConfiguration.OmniPaymentsConfiguration.international]
        return businessPayments
    }()

}
