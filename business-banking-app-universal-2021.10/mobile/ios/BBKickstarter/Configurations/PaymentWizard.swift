//
//  Created by Backbase R&D B.V. on 18/05/2021.
//

import Foundation
import BusinessOmniPaymentsJourney
import BusinessDesign
import BackbaseDesignSystem
import FlagKit
import UIKit
import BusinessJourneyCommon
import Resolver
import SwiftUI

// MARK: - PaymentWizard configuration object

//swiftlint:disable file_length
extension JourneysConfiguration.OmniPaymentsConfiguration {

    public static let paymentWizard: OmniPayments.Business.Configuration.Payment = {
        let paymentWizardSupport = PaymentWizardSupport()

        var wildcard = BankCodeLabelUtil.wildcard

        let bankCodesMapping: [Pair: String] = [
            Pair("AU", "bsb"): localized("omnipayments.paymentWizard.bankCodes.bsb")(),
            Pair(wildcard, "routing-number"): localized("omnipayments.paymentWizard.bankCodes.routingNumber.CA")(),
            Pair("GB", "sort-code"): localized("omnipayments.paymentWizard.bankCodes.sortCode")(),
            Pair("GB", "clearing"): localized("omnipayments.paymentWizard.bankCodes.clearing")(),
            Pair("IN", "ifsc"): localized("omnipayments.paymentWizard.bankCodes.ifsc")(),
            Pair("RU", "bik"): localized("omnipayments.paymentWizard.bankCodes.bik")(),
            Pair("US", "routing-number"): localized("omnipayments.paymentWizard.bankCodes.routingNumber.USA")(),
            Pair("ZA", "za-code"): localized("omnipayments.paymentWizard.bankCodes.zaCode")()
        ]

        var paymentWizard = OmniPayments.Business.Configuration.Payment()

        paymentWizard.title = localized("omniPayments.paymentWizard.title")()
        paymentWizard.subtitle = localized("omniPayments.paymentWizard.subtitle")()
        paymentWizard.icon = UIImage.named(DesignSystem.Assets.icAddCircleOutline, in: .design) ?? UIImage()

        paymentWizard.configuration = {
            enum Steps: Int {
                case fromAccounts = 0
                case toAccounts
                case editBeneficiary
                case paymentDetails
                case paymentOptions
            }

            var config = OmniPayments.Configuration()
            config.shouldPreloadSanctionedCountries = true

            let validators = Validators()
            config.steps = [{
                var accountsStep = Step()
                accountsStep.title = localized("omniPayments.paymentWizard.steps.fromStep.title")()

                accountsStep.layoutBuilder = { model in
                    let listLayout = AccountsStepLayout()
                    var accountParameters = AccountsStepLayout.Configuration.AccountFetchParams(
                        businessFunction: PaymentWizardSupport.Constants.businessFunction,
                        resourceName: PaymentWizardSupport.Constants.resourceName,
                        privilege: PaymentWizardSupport.Constants.privilege,
                        accountType: .debit)
                    listLayout.configuration.accountFetchParams = accountParameters
                    listLayout.configuration.uiDataMapper.listItem.subTitle = { account in
                        if let iban = account.IBAN {
                            return IBANUtils.formatIBAN(iban)
                        } else if let bban = account.BBAN {
                            let bankBranchCode = account.bankBranchCode ?? ""
                            return "\(bankBranchCode) / \(bban)"
                        } else if let productNumber = account.productNumber {
                            return productNumber
                        }
                        return ""
                    }

                    return listLayout
                }

                accountsStep.didInitiate = { (navigation, paymentModel) in
                    return AccountsStep.build(with: accountsStep,
                                              omnipaymentsNavigation: navigation,
                                              paymentModel: paymentModel)
                }

                accountsStep.didComplete = { (navigation, paymentModel, viewController) in
                    let nextStep = config.steps[Steps.toAccounts.rawValue]

                    if let routingFunction = nextStep.didInitiate {
                        let nextViewController = routingFunction(navigation, paymentModel)
                        viewController.navigationController?.pushViewController(nextViewController, animated: true)
                    }
                }

                return accountsStep
            }(), {

                var toStep = Step()
                toStep.title = localized("omniPayments.domestic.steps.toStep.title")()

                toStep.layoutBuilder = { paymentModel in

                    var accountParameters = TabbedAccountsListLayout.AccountFetchParams(
                        businessFunction: PaymentWizardSupport.Constants.businessFunction,
                        resourceName: PaymentWizardSupport.Constants.resourceName,
                        privilege: PaymentWizardSupport.Constants.privilege,
                        accountType: .credit)

                    let listLayout = BeneficiarySelectionStepLayout()
                    listLayout.configuration.accountsStepLayout.configuration.strings.emptyResultTitle = localized("omniPayments.paymentWizard.ToAccountScreen.error.title")
                    listLayout.configuration.accountsStepLayout.configuration.strings.emptyResultMessage = localized("omniPayments.paymentWizard.ToAccountScreen.error.description")
                    listLayout.configuration.accountsStepLayout.configuration.images.noResultsImage = UIImage(named: DesignSystem.Assets.icWarningAmber, in: .design, compatibleWith: nil)
                    listLayout.configuration.accountsStepLayout.configuration.accountFetchParams = accountParameters
                    listLayout.configuration.contactsStepLayout.configuration.listButton = (isVisible: false, onClick: { _,_ in })

                    return listLayout
                }

                toStep.didInitiate = { (navigation, paymentModel) in
                    return BeneficiarySelectionStep.build(with: toStep,
                                                          omnipaymentsNavigation: navigation,
                                                          paymentModel: paymentModel)
                }

                toStep.didComplete = { (navigation, paymentModel, viewController) in

                    var nextStep = config.steps[Steps.paymentDetails.rawValue]

                    if PaymentWizardSupport.shouldPresentEditBeneficiaryScreen(using: paymentModel) {
                        nextStep = config.steps[Steps.editBeneficiary.rawValue]
                    }

                    if let routingFunction = nextStep.didInitiate {
                        let nextViewController = routingFunction(navigation, paymentModel)
                        viewController.navigationController?.pushViewController(nextViewController, animated: true)
                    }
                }

                return toStep
            }(), {
                var beneficiaryStep = Step()
                beneficiaryStep.title = localized("omniPayments.beneficiary.title")()
                beneficiaryStep.prefersLargeTitles = false
                beneficiaryStep.layoutBuilder = { paymentModel in

                    var fieldMapping : [String: UIView] = [:]
                    let initials = paymentModel.toAccount.initials

                    // Avatar field
                    let avatar = AvatarView.build(initials: initials)

                    // Name field
                    let nameLabel = localized("omniPayments.paymentWizard.beneficiary.accountName")()
                    let nameField =  InlineTextInput.build(
                        primaryLabel: nameLabel,
                        secondaryLabel: nil,
                        initialInput: paymentModel.toAccount.contactName,
                        onValidatedInput: { newValue in
                            let trimmedValue = newValue.trimmingCharacters(in: .whitespacesAndNewlines)
                            if paymentModel.toAccount.isOwnAccount {
                                paymentModel.toAccount.accountName = trimmedValue
                            } else {
                                paymentModel.toAccount.contactName = trimmedValue
                            }
                        },
                        focusLostValidators: [Validators.requiredValidator(for: nameLabel)],
                        submitValidators: [Validators.requiredValidator(for: nameLabel)])

                    // Account name field
                    let accountNameLabel = localized("omniPayments.beneficiary.accountInfo")()
                    let accountNameMargin = UIEdgeInsets(top: 8, left: 16, bottom: 8, right: 16)

                    var conf = Text.Configuration()

                    conf.design.text = { label in
                        label.textColor = DesignSystem.shared.colors.text.default
                        label.font = DesignSystem.shared.fonts.preferredFont(.title2, .semibold)
                    }

                    let accountName = Text.build(textValue: accountNameLabel,
                                                 configuration: conf,
                                                 margin: accountNameMargin)

                    // Account number field
                    let accountNumberLabel = localized("omniPayments.paymentWizard.beneficiary.accountNumber")()

                    let accountNumberValidators = [Validators.nonEmptyRegexValidator(
                                                    for: accountNumberLabel,
                                                    with: paymentModel.accountNumberRegexPattern)]

                    let accountNumberField =  InlineTextInput.build(
                        primaryLabel: accountNumberLabel,
                        secondaryLabel: nil,
                        initialInput: paymentModel.toAccount.accountNumber,
                        onValidatedInput: { accountNumber in
                            let accountFormat = paymentModel
                                .firstMatching(field: PaymentWizardSupport.Constants.accountNumberKey)?
                                .format ?? "BBAN"

                            if accountFormat == "BBAN" {
                                paymentModel.toAccount.bban = accountNumber
                                paymentModel.toAccount.iban = ""
                            } else {
                                paymentModel.toAccount.iban = accountNumber
                                paymentModel.toAccount.bban = ""
                            }
                        },
                        focusLostValidators: accountNumberValidators,
                        submitValidators: accountNumberValidators)

                    // Bank branch code field
                    let bankBranchCodeField =  InlineTextInput.build(
                        primaryLabel: "",
                        secondaryLabel: nil,
                        initialInput: paymentModel.toAccount.bankCode,
                        onValidatedInput: { bankBranchCode in
                            paymentModel.toAccount.bankCode = bankBranchCode
                        },
                        focusLostValidators: [],
                        submitValidators: [],
                        shouldBeVisible: { false })

                    // Sanctioned Countries Picker field
                    let countryNameHandler: (String) -> (String?) = {
                        Locale.autoupdatingCurrent.localizedString(forRegionCode: $0)
                    }
                    let countryFlagHandler: (String) -> (UIImage?) = { Flag(countryCode: $0)?.originalImage }

                    let sanctionedPickerConfiguration = SanctionedCountriesPicker.Configuration()

                    let countryValidator: (String) -> (ValidationResult) = { textValue in
                        let label = sanctionedPickerConfiguration.strings.label.value
                        guard textValue.isEmpty else {
                            return ValidationResult(isValid: true)
                        }

                        let errorMessage = localized("omniPayments.requirementErrorLabel")()
                        let formattedErrorMessage = String(format: errorMessage,
                                                           locale: Locale.current, label)
                        return ValidationResult(isValid: false, errorMessage: formattedErrorMessage)
                    }

                    let sanctionedCountriesPicker = SanctionedCountriesPicker.build(
                        configuration: sanctionedPickerConfiguration,
                        isEnabled: { true },
                        initialValue: {
                            var country = paymentModel.toAccount.sanctionedCountry
                            country?.imageHandler = countryFlagHandler
                            country?.titleHandler = countryNameHandler
                            return country
                        },
                        isSearchEnabled: { true },
                        countryNameHandler: countryNameHandler,
                        countryFlagHandler: countryFlagHandler,
                        didSelectCountry: { country in

                            PaymentWizardSupport.willUpdateAccountNumber(
                                with: country.code,
                                paymentModel: paymentModel,
                                fieldMapping: fieldMapping,
                                label: accountNumberLabel)

                            PaymentWizardSupport.willUpdateBankCode(
                                with: country.code,
                                paymentModel: paymentModel,
                                fieldMapping: fieldMapping,
                                pairs: bankCodesMapping)
                        },
                        submitValidators: [countryValidator],
                        margin: UIEdgeInsets(top: 32, left: 16, bottom: 8, right: 16))

                    fieldMapping[PaymentWizardSupport.Constants.accountBankCodeKey] = bankBranchCodeField
                    fieldMapping[PaymentWizardSupport.Constants.accountNumberKey] = accountNumberField

                    let stackView = StackStepLayout()
                    stackView.configuration.fields = [avatar,
                                                      sanctionedCountriesPicker,
                                                      nameField,
                                                      accountName,
                                                      accountNumberField,
                                                      bankBranchCodeField]

                    // Update bank code and account number fields with bank country code selection
                    if let countryCode = paymentModel.toAccount.bankCountryIsoCode {
                        PaymentWizardSupport.willUpdateAccountNumber(
                            with: countryCode,
                            paymentModel: paymentModel,
                            fieldMapping: fieldMapping,
                            label: accountNumberLabel)

                        PaymentWizardSupport.willUpdateBankCode(
                            with: countryCode,
                            paymentModel: paymentModel,
                            fieldMapping: fieldMapping,
                            pairs: bankCodesMapping)
                    }

                    return stackView
                }

                beneficiaryStep.didInitiate = { (navigation, paymentModel) in
                    return StackStep.build(with: beneficiaryStep,
                                           omnipaymentsNavigation: navigation,
                                           paymentModel: paymentModel,
                                           shouldValidateFieldsWhenInitialised: true)
                }

                beneficiaryStep.didComplete = { (navigation, paymentModel, viewController) in
                    let nextStep = config.steps[Steps.paymentDetails.rawValue]

                    if !PaymentWizardSupport.shouldDisplayBankCodeField(paymentModel: paymentModel) {
                        paymentModel.toAccount.bankCode = ""
                    }

                    if let routingFunction = nextStep.didInitiate {
                        let nextViewController = routingFunction(navigation, paymentModel)
                        viewController.navigationController?.pushViewController(nextViewController, animated: true)
                    }
                }

                return beneficiaryStep
            }(), {
                var beneficiaryDetails = Step()
                beneficiaryDetails.title = localized("omniPayments.paymentWizard.beneficiary.title")()
                beneficiaryDetails.prefersLargeTitles = false
                beneficiaryDetails.layoutBuilder = { paymentModel in
                    let stackView = StackStepLayout()

                    var fromIcon: DetailedTransferAccountPreview.AccountImage? = nil

                    if let image = UIImage(named: DesignSystem.Assets.icAccountBalanceWallet,
                                       in: .design,
                                       compatibleWith: nil) {
                        fromIcon = .icon(image)
                    }

                    let fromAccount = DetailedTransferAccountPreview.FromAccountViewData(
                        name: paymentModel.fromAccount.accountName,
                        accountNumber: paymentModel.fromAccount.getAvailableAccountNumber(),
                        amount: paymentModel.fromAccount.availableFunds,
                        avatar: fromIcon)

                    let accountNameInitials = paymentModel.toAccount.initials ?? ""
                    var toIcon: DetailedTransferAccountPreview.AccountImage? = .initials(accountNameInitials)

                    var defaultOwnAccountCountryCode = paymentModel.sanctionedCountries
                        .first { $0.isDefault == true }?
                        .code

                    var countryCode = paymentModel.toAccount.bankCountryIsoCode
                    if paymentModel.toAccount.isOwnAccount {
                        countryCode = defaultOwnAccountCountryCode
                        toIcon = fromIcon
                    }

                    let toAccount = DetailedTransferAccountPreview.ToAccountViewData(
                        name: paymentModel.toAccount.contactAccountName,
                        accountNumber: paymentModel.toAccount.getAvailableAccountNumber(),
                        amount: paymentModel.toAccount.availableFunds,
                        countryISOCode: countryCode,
                        avatar: toIcon)

                    var configuration = DetailedTransferAccountPreview.Configuration()

                    let countryNameHandler: (String) -> (String?) = {
                        Locale.autoupdatingCurrent.localizedString(forRegionCode: $0)
                    }

                    let countryFlagHandler: (String) -> (UIImage?) = {
                        Flag(countryCode: $0)?.originalImage
                    }

                    configuration.countryImageHandler = countryFlagHandler
                    configuration.countryNameHandler = countryNameHandler
                    configuration.design.margin = .init(top: 16, left: 0, bottom: 0, right: 0)

                    let transferAccountPreview = DetailedTransferAccountPreview.build(
                        fromAccount: fromAccount,
                        toAccount: toAccount,
                        configuration: configuration)

                    var amountConfiguration = AdaptiveAmount.Configuration()
                    amountConfiguration.design.margin = .init(top: 0, left: 16, bottom: 0, right: 16)
                    amountConfiguration.design.amountView = { view in
                        view.backgroundColor = DesignSystem.shared.colors.foundation.default
                        view.clipsToBounds = true
                        view.layer.cornerRadius = 8
                    }

                    let currencyNameHandler: (String) -> (String?) = { code in
                        Locale.autoupdatingCurrent.localizedString(forCurrencyCode: code)
                    }

                    let currencyImageHandler: (String) -> (UIImage?) = { code in
                        Flag(countryCode: String(code.prefix(2)))?.originalImage
                    }

                    func amountValidator(_ amount: String) -> ValidationResult {
                        let decimalNumber = NSDecimalNumber(string: amount,
                                                            locale: Locale.autoupdatingCurrent)
                        guard !decimalNumber.decimalValue.isNaN, decimalNumber.doubleValue > 0 else {
                            return .init(isValid: false, errorMessage: amountConfiguration.strings.amountRequiredErrorLabel())
                        }

                        return .init(isValid: true)
                    }

                    let amountView = AdaptiveAmount.build(paymentModel: paymentModel,
                                                          configuration: amountConfiguration,
                                                          didSelectCurrency: { _ in },
                                                          currencyNameHandler: currencyNameHandler,
                                                          currencyImageHandler: currencyImageHandler,
                                                          didValidateInput: {
                        paymentModel.amount = $0
                        paymentModel.toAccount.currencyCode = $1
                    },
                                                          submitValidators: [amountValidator])

                    stackView.configuration.fields = [transferAccountPreview, amountView]
                    return stackView
                }

                beneficiaryDetails.didInitiate = { (navigation, paymentModel) in
                    return StackStep.build(with: beneficiaryDetails,
                                           omnipaymentsNavigation: navigation,
                                           paymentModel: paymentModel)
                }

                beneficiaryDetails.didComplete = { navigation, paymentModel, viewController in
                    let nextStep = config.steps[Steps.paymentOptions.rawValue]
                    PaymentWizardSupport.handleBeneficiaryStepOutput(navigation: navigation,
                                                                     paymentModel: paymentModel,
                                                                     viewController: viewController,
                                                                     step: nextStep)
                }

                return beneficiaryDetails
            }(), {
                var paymentOptionStep = Step()
                paymentOptionStep.title = localized("omniPayments.paymentWizard.paymentOptions.title")()
                paymentOptionStep.prefersLargeTitles = false

                paymentOptionStep.layoutBuilder = { paymentModel in
                    var configuration = PaymentOptionsLayout.Configuration()

                    configuration.errorState = { error in
                        var errorStateView = PaymentOptionsLayout.ErrorViewState()

                        errorStateView.configuration.strings.title = localized("omniPayments.paymentWizard.paymentOptions.errorTitle")()
                        errorStateView.configuration.strings.description = localized("omniPayments.paymentWizard.paymentOptions.errorDescription")()
                        errorStateView.configuration.strings.primaryButtonTitle = localized("omniPayments.paymentWizard.paymentOptions.retry")()

                        if case .notConnected = error {
                            errorStateView.configuration.strings.title = localized("omniPayments.paymentWizard.paymentOptions.noInternetErrorTitle")()
                            errorStateView.configuration.strings.description = localized("omniPayments.paymentWizard.paymentOptions.noInternetErrorMessage")()
                        }

                        return errorStateView
                    }

                    return PaymentOptionsLayout(configuration: configuration)
                }

                paymentOptionStep.didInitiate = { (navigation, paymentModel) in
                    PaymentOptionsStep.build(with: paymentOptionStep,
                                             omnipaymentsNavigation: navigation,
                                             paymentModel: paymentModel)
                }

                return paymentOptionStep
            }()]

            return config
        }()

        return paymentWizard
    }()
}

// MARK: - Support object for this payment configuration / PaymentWizardSupport

fileprivate struct PaymentWizardSupport {

    // Triggers an update on the UI for the account number field and updates the account number in the payment
    // model according to the sanctioned countries selection.
    static func willUpdateAccountNumber(with countryCode: String,
                                        paymentModel: OmniPayment,
                                        fieldMapping: [String: UIView],
                                        label: String) {
        paymentModel.toAccount.updateBankAddress(with: countryCode)

        let accountNumberValidators = [JourneysConfiguration.OmniPaymentsConfiguration.Validators.nonEmptyRegexValidator(
                                            for: label,
                                            with: paymentModel.accountNumberRegexPattern)]

        (fieldMapping[PaymentWizardSupport.Constants.accountNumberKey] as? InlineTextInputView)?.update(
                        submitValidators: accountNumberValidators,
                        focusLostValidators: accountNumberValidators)
    }

    // Triggers an update on the UI for the bank code field and updates bank code in the payment
    // model according to the sanctioned countries selection.
    static func willUpdateBankCode(with countryCode: String,
                                   paymentModel: OmniPayment,
                                   fieldMapping: [String: UIView],
                                   pairs: [Pair: String]) {

        let bankCodeInputField = paymentModel.firstMatching(field: Constants.accountBankCodeKey)

        let label = bankCodeInputField?.label
        let bankCodeRegex = bankCodeInputField?.regex
        let isMandatory = bankCodeInputField?.mandatory ?? false

        let bankCodeLabel = BankCodeLabelUtil.getBankCodeLabel(key: label?.keys.first,
                                                               countryCode: countryCode,
                                                               defaultFallbackLabel: label?.values.first ?? "",
                                                               textConfiguration: pairs)

        let isVisible = shouldDisplayBankCodeField(paymentModel: paymentModel)

        var bankCodeValidators = [JourneysConfiguration.OmniPaymentsConfiguration.Validators.nonEmptyRegexValidator(
                                    for: bankCodeLabel,
                                    with: bankCodeRegex)]

        if !isMandatory {
            bankCodeValidators = []
        }

        (fieldMapping[Constants.accountBankCodeKey] as? InlineTextInputView)?
            .update(primaryLabel: bankCodeLabel,
                    shouldBeVisible: { isVisible },
                    submitValidators: bankCodeValidators,
                    focusLostValidators: bankCodeValidators)
    }

    static func handleBeneficiaryStepOutput(navigation: OmniPaymentsNavigable,
                                            paymentModel: OmniPayment,
                                            viewController: UIViewController,
                                            step: Step) {

        let originatorAccount: PostPaymentOptionsRequest.Identification = .init(
            identifier: paymentModel.fromAccount.accountId,
            schemeName: .id,
            additions: paymentModel.fromAccount.additions)

        let toScheme = paymentModel.toAccount.accountSchemeName.toUseCaseSchemeName()
        let counterparyAccount: PostPaymentOptionsRequest.CounterpartyAccount = .init(
            bankBranchCode: paymentModel.toAccount.bankCode,
            identification: paymentModel.toAccount.accountNumber,
            schemeName: toScheme,
            additions: paymentModel.toAccount.additions)

        let amount = PaymentOption.Currency(amount: paymentModel.amount.stringValue,
                                            currencyCode: paymentModel.toAccount.currencyCode,
                                            additions: nil)
        let request = PostPaymentOptionsRequest(originatorAccount: originatorAccount,
                                                counterpartyAccount: counterparyAccount,
                                                counterpartyCountry: paymentModel.toAccount.accountPostalAddress?.country,
                                                instructedAmount: amount,
                                                requestedExecutionDate: paymentModel.executionDate,
                                                additions: nil)

        paymentModel.paymentOptionState = .init(request: request)

        navigation.postPaymentOptions(paymentOptionsRequest: request, completion: { result in
            let pushNext: Bool
            switch result {
            case let .failure(error):
                paymentModel.paymentOptionState?.response = .failure(error)
                pushNext = true
            case let .success(paymentOption):
                paymentModel.paymentOptionState?.response = .success(paymentOption)
                pushNext = false
            }

            if let routingFunction = step.didInitiate, pushNext {
                let nextViewController = routingFunction(navigation, paymentModel)
                viewController.navigationController?.pushViewController(nextViewController, animated: true)
            }
        })
    }

    // Whether the current selection of sanctioned country should make the UI display a bank code field
    static func shouldDisplayBankCodeField(paymentModel: OmniPayment) -> Bool {
        let bankCodeInputField = paymentModel.firstMatching(field: Constants.accountBankCodeKey)

        let label = bankCodeInputField?.label
        let labelKey = label?.keys.first
        let isApplicable = !(bankCodeInputField?.notApplicable ?? false)
        let defaultLabel = label?.values.first ?? ""

        return labelKey != nil && !defaultLabel.isEmpty && isApplicable
    }

    // The logic on this function makes sure that:
    // - There are no missing fields
    // - Skipping the display of the edit beneficiary screen for own accounts
    // The logic within this function can be easily redefined as the user pleases
    static func shouldPresentEditBeneficiaryScreen(using paymentModel: OmniPayment) -> Bool {
        if paymentModel.toAccount.isOwnAccount {
            return false
        }

        let accountNumber = paymentModel.toAccount.accountNumber

        var isAccountNumberValid = !accountNumber.isEmpty
        if let regex = paymentModel.accountNumberRegexPattern {
            isAccountNumberValid = accountNumber.range(of: regex, options: .regularExpression) != nil
        }

        let contactNameIsNotEmpty = !paymentModel.toAccount.contactAccountName.isEmpty
        let isCountrySelected = !(paymentModel.toAccount.bankCountryIsoCode ?? "").isEmpty

        return !(isAccountNumberValid && contactNameIsNotEmpty && isCountrySelected)
    }

    struct Constants {
        public static let privilege = "create"
        public static let businessFunction = "SEPA CT,UK CHAPS,UK Foreign Wire,US Domestic Wire,ACH Credit Transfer"
        public static let resourceName = "Payments"

        public static let accountNumberKey = "account-number"
        public static let accountBankCodeKey = "bank-code"
    }
}

// MARK: - Helper functions for OmniPayment model
private extension OmniPayment.SchemeNames {
    func toUseCaseSchemeName() -> PostPaymentOptionsRequest.SchemeNames {
        switch self {
        case .iban:
            return .iban
        case .bban:
            return .bban
        case .id, .mobile, .email:
            return .id
        case .externalId:
            return .externalId
        @unknown default:
            return .id
        }
    }
}

private extension OmniPayment {
    func firstMatching(field: String) -> SanctionedCountry.InputFormField? {
        let selectedCountry = toAccount.bankCountryIsoCode

        return sanctionedCountries
            .first { $0.country == selectedCountry }?
            .inputFormSettings?
            .first { $0.key == field }
    }

    var accountNumberRegexPattern: String? {
        return firstMatching(field: PaymentWizardSupport.Constants.accountNumberKey)?.regex
    }

    var bankBranchCodeIsMandatory: Bool {
        return firstMatching(field: PaymentWizardSupport.Constants.accountBankCodeKey)?.mandatory ?? false
    }
}

// MARK: - Helper functions for PaymentAccount object

private extension PaymentAccount {
    func getAvailableAccountNumber() -> String {

        var formattedAccountNumber: String?

        if !iban.isEmpty {
            formattedAccountNumber = IBANUtils.formatIBAN(iban)
        } else if !bban.isEmpty && !bankCode.isEmpty {
            formattedAccountNumber = "\(bankCode) / \(bban)"
        } else if !bban.isEmpty && bankCode.isEmpty {
            formattedAccountNumber = bban
        }

        guard let accountnumber = formattedAccountNumber else {
            fatalError("No valid identification number found")
        }

        return accountnumber
    }

    var isOwnAccount: Bool {
        contactName.isEmpty && !productTypeName.isEmpty
    }

    var bankCountryIsoCode: String? {
        if let countryISO = accountBank?.postalAddress?.country {
            return countryISO
        }

        return bank.postalAddress?.country
    }

    var initials: String? {
        if contactName.isEmpty {
            return nil
        }

        return contactName
            .trimmingCharacters(in: .whitespaces)
            .split(separator: " ")
            .map { ($0.first ?? " ").uppercased() }
            .prefix(2)
            .joined()
    }

    var sanctionedCountry: SanctionedCountry? {
        if let countryISO = bankCountryIsoCode {
            return SanctionedCountry(country: countryISO)
        }

        return nil
    }
}
