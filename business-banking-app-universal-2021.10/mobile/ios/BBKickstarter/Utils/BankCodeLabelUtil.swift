//
//  Created by Backbase R&D B.V. on 28/09/2021.
//

import Foundation

struct BankCodeLabelUtil {

    static var wildcard = "*"

    /// Find correct bank code label from provided key-value pair for specific country ISO if found.
    /// Return nil if no match found or no default is provided.
    ///
    ///  Search logic will have two fallbacks for the bank code label, sorted by priority
    ///   1. If no country specific found, will use matching bank-code key with wildcard "*" as country iso if any
    ///   2. If no wildcard is provided in [textConfiguration], will use default value of [defaultFallbackLabel]
    ///   3. nil
    ///
    /// - Parameters:
    ///  - key the bank code label identifier to search
    ///  - countryCode current selected country ISO
    ///  - defaultFallbackLabel fallback text to use when there is no match found or no default is provided
    ///  - textConfiguration map of country ISO and bank-code pair to string of bank code label
    static func getBankCodeLabel(key: String?,
                                 countryCode: String,
                                 defaultFallbackLabel: String,
                                 textConfiguration: [Pair: String]) -> String {

        guard let key = key else {
            return defaultFallbackLabel
        }

        let countryLabelMatcher = Pair(countryCode, key)
        let wildcardLabelMatcher = Pair(wildcard, key)

        if let valueForCountry = textConfiguration[countryLabelMatcher] {
            return valueForCountry
        }

        if let valueForWildcard = textConfiguration[wildcardLabelMatcher] {
            return valueForWildcard
        }

        return defaultFallbackLabel
    }
}

struct Pair: Hashable {
    let first: String
    let second: String

    init(_ first: String, _ second: String) {
        self.first = first
        self.second = second
    }
}
