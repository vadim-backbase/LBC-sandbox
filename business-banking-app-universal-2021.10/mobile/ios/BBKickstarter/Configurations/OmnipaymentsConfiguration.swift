//
//  Created by Backbase R&D B.V. on 16/02/2021.
//

import Foundation
import BusinessJourneyCommon
import BusinessOmniPaymentsJourney
import BackbaseDesignSystem

extension JourneysConfiguration {
    struct OmniPaymentsConfiguration {
        static var localized: (String) -> (LocalizedString) = { key in
            return LocalizedString(key: key, in: Bundle(for: OmniPayments.self))
        }
    }
}

extension JourneysConfiguration.OmniPaymentsConfiguration {
    struct Validators {

        init() { }

        var paymentFeeSelectionMustNotEmpty: (String) -> (ValidationResult) = { textValue in

            guard textValue.isEmpty else {
                return ValidationResult(isValid: true)
            }

            let message = localized("omniPayments.config.fields.paymentFeeSelector.validations.isEmpty")()
            return ValidationResult(isValid: false, errorMessage: message)
        }

        var amountIsPositive: (String) -> (ValidationResult) = { textValue in
            let amount = NSDecimalNumber(string: textValue)

            guard amount.decimalValue.isLess(than: 0) else {
                return ValidationResult(isValid: true)
            }

            let message = localized("omniPayments.config.fields.amount.validations.isPositive")()
            return ValidationResult(isValid: false, errorMessage: message)
        }

        var amountNotEmpty: (String) -> (ValidationResult) = { textValue in
            guard textValue.isEmpty else {
                return ValidationResult(isValid: true)
            }

            let message = localized("omniPayments.config.fields.amount.validations.isEmpty")()
            return ValidationResult(isValid: false, errorMessage: message)
        }

        var countryMustBeSelected: (String) -> (ValidationResult) = { textValue in
            guard textValue.isEmpty else {
                return ValidationResult(isValid: true)
            }

            let message = localized("omniPayments.stack.views.sanctionedCountries.field.validations")()
            return ValidationResult(isValid: false, errorMessage: message)
        }

        var beneficiaryNameMustNotBeEmpty: (String) -> (ValidationResult) = { textValue in
            let textValue = textValue.trimmingCharacters(in: .whitespaces)
            guard !textValue.isEmpty else {
                let message = localized("omniPayments.config.fields.beneficiary.validations.isEmpty")()
                return ValidationResult(isValid: false, errorMessage: message)
            }
            return ValidationResult(isValid: true)
        }

        var ibanMustBeValid: (String) -> (ValidationResult) = { textValue in
            guard !textValue.trimmingCharacters(in: .whitespaces).isEmpty, IBANValidator.isValidIBAN(textValue) else {
                let message = localized("omniPayments.config.fields.iban.validations.isEmpty")()
                return ValidationResult(isValid: false, errorMessage: message)
            }

            return ValidationResult(isValid: true)
        }

        static func requiredValidator(for field: String) -> (String) -> (ValidationResult) {
            return {  textValue in
                guard textValue.isEmpty else {
                    return ValidationResult(isValid: true)
                }

                let errorMessage = localized("omniPayments.requirementErrorLabel")()
                return ValidationResult(isValid: false,
                                        errorMessage: String(format: errorMessage, locale: Locale.current, field))
            }
        }

        static func regexValidator(for field: String, with regex: String?) -> (String) -> (ValidationResult) {
            return { textValue in
                guard let regexString = regex else {
                    return ValidationResult(isValid: true)
                }
                let isValid = textValue.range(of: regexString.replacingOccurrences(of: "/", with: ""), options: .regularExpression) != nil
                if isValid {
                    return ValidationResult(isValid: true)
                } else {
                    let errorMessage = localized("%@ is wrongly formatted")()
                    return ValidationResult(isValid: false,
                                            errorMessage: String(format: errorMessage, locale: Locale.current, field))
                }
            }
        }

        static func nonEmptyRegexValidator(for field: String, with regex: String?) -> (String) -> (ValidationResult) {
            return { textValue in
                if textValue.isEmpty {
                    let errorMessage = localized("omniPayments.requirementErrorLabel")()
                    return ValidationResult(isValid: false,
                                            errorMessage: String(format: errorMessage, locale: Locale.current, field)) }

                guard let regexString = regex else {
                    return ValidationResult(isValid: true)
                }
                let isValid = textValue.range(of: regexString.replacingOccurrences(of: "/", with: ""), options: .regularExpression) != nil
                if isValid {
                    return ValidationResult(isValid: true)
                } else {
                    let errorMessage = localized("%@ is wrongly formatted")()
                    return ValidationResult(isValid: false,
                                            errorMessage: String(format: errorMessage, locale: Locale.current, field))
                }
            }
        }

    }
}

extension JourneysConfiguration.OmniPaymentsConfiguration {
    struct RegularExpressions {
        static let allowedCharactersRegexp: NSRegularExpression = {
            do {
                return try NSRegularExpression(pattern: "[a-zA-Z0-9=:;\\-()/.&@,*#'? ]+", options: [])
            } catch { fatalError("Invalid regexp") }
        }()
    }
}
