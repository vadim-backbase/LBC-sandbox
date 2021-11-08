//
//  Created by Backbase R&D B.V. on 04/02/2021.
//

import UIKit
import BusinessOmniPaymentsJourney
import BackbaseDesignSystem

extension JourneysConfiguration.OmniPaymentsConfiguration {
    struct InternationalPayment {

        static var reviewStep: Step = {
            var reviewStep = Step()
            reviewStep.title = localized("omniPayments.config.steps.reviewStep.title")()

            reviewStep.layoutBuilder = { (paymentModel) -> StackStepLayout in

                let stackStepLayout = StackStepLayout()
                var fields: [UIView] = []
                let continueButton = localized("omniPayments.config.steps.reviewStep.continueButton.title")
                stackStepLayout.configuration.strings.continueButtonTitle = continueButton

                let amountTransferTitleSubtitlePreviewView = TransferTitleSubtitlePreview.build(
                    title: localized("omniPayments.international.paymentReview.amountBlock.title")(),
                    subtitle: {
                        paymentModel.amountFormattedUsingReceiverCurrency ?? "0"
                    },
                    subtitleTextAppearance: DesignSystem.shared.fonts.preferredFont(.title2, .semibold),
                    hideBottomSeparator: true,
                    blankSubtitlePlaceholder: "-",
                    margin: UIEdgeInsets(top: 0,
                                         left: DesignSystem.shared.spacer.md,
                                         bottom: DesignSystem.shared.spacer.md,
                                         right: DesignSystem.shared.spacer.md))

                let dateTransferTitleSubtitlePreviewView = TransferTitleSubtitlePreview.build(
                    title: localized("omniPayments.stack.views.executionDate.title")(),
                    subtitle: { () -> (String) in
                        let text = TransferScheduleInputPreview.Strings()
                        if paymentModel.executionDate.bb.isToday {
                            return text.today() + ", " + paymentModel.executionDate.bb.formatted(dateStyle: .medium)
                        } else {
                            return paymentModel.executionDate.bb.formatted(dateStyle: .medium)
                        }
                    }, subtitleTextAppearance: DesignSystem.shared.fonts.preferredFont(.headline, .semibold),
                    hideBottomSeparator: false,
                    blankSubtitlePlaceholder: "")

                let frequencyTransferTitleSubtitlePreviewView = TransferTitleSubtitlePreview.build(
                    title: localized("omniPayments.stack.views.frequency.title")(),
                    subtitle: { () -> (String) in
                        let text = TransferScheduleInputPreview.Strings()
                        switch paymentModel.transferFrequency {
                        case .once: return text.once()
                        case .daily: return text.daily()
                        case .weekly: return text.weekly()
                        case .biweekly: return text.biweekly()
                        case .monthly: return text.monthly()
                        case .quarterly: return text.quarterly()
                        case .yearly: return text.yearly()
                        default: return ""
                        }
                    }, subtitleTextAppearance: DesignSystem.shared.fonts.preferredFont(.headline, .semibold),
                    hideBottomSeparator: false,
                    blankSubtitlePlaceholder: "-")

                let paymentReferenceTransferTitleSubtitlePreviewView = TransferTitleSubtitlePreview.build(
                    title: localized("omniPayments.stack.views.paymentReference.title")(),
                    subtitle: { () -> (String) in
                        return paymentModel.reference ?? ""
                    }, subtitleTextAppearance: DesignSystem.shared.fonts.preferredFont(.headline, .semibold),
                    hideBottomSeparator: paymentModel.description == nil,
                    blankSubtitlePlaceholder: "")

                let paymentDescriptionTransferTitleSubtitlePreviewView = TransferTitleSubtitlePreview.build(
                    title: localized("omniPayments.stack.views.paymentDescription.title")(),
                    subtitle: { () -> (String) in
                        return paymentModel.description ?? ""
                    }, subtitleTextAppearance: DesignSystem.shared.fonts.preferredFont(.headline, .semibold),
                    hideBottomSeparator: false,
                    blankSubtitlePlaceholder: "")

                let transferAccountReviewView = TransferAccountReview.build(
                    fromLabel: localized("omniPayments.config.fields.transferAccountReview.fromTitle")(),
                    fromAccountName: paymentModel.fromAccount.accountName,
                    fromAccountNumber: paymentModel.fromAccount.accountNumber,
                    toLabel: localized("omniPayments.config.fields.transferAccountReview.toTitle")(),
                    toAccountName: paymentModel.toAccount.contactAccountName,
                    toAccountNumber: paymentModel.toAccount.accountNumber)

                fields = [transferAccountReviewView, amountTransferTitleSubtitlePreviewView]

                fields.append(contentsOf: conversionRelatedFields(using: paymentModel))

                if let correspondentBankInfo = paymentModel.correspondentBank {
                    let title = localized("omniPayments.international.paymentReview.bankInfoBlock.correspondentBank.title")()
                    fields.append(contentsOf: buildBankDetails(for: correspondentBankInfo, with: title))
                }

                if let intermediaryBankInfo = paymentModel.intermediaryBank {
                    let title = localized("omniPayments.international.paymentReview.bankInfoBlock.intermediaryBank.title")()
                    fields.append(contentsOf: buildBankDetails(for: intermediaryBankInfo, with: title))
                }

                fields.append(contentsOf: [dateTransferTitleSubtitlePreviewView, frequencyTransferTitleSubtitlePreviewView])

                if let reference = paymentModel.reference, !reference.isEmpty {
                    fields.append(paymentReferenceTransferTitleSubtitlePreviewView)
                }

                if let description = paymentModel.description, !description.isEmpty {
                    fields.append(paymentDescriptionTransferTitleSubtitlePreviewView)
                }

                stackStepLayout.configuration.fields = fields
                return stackStepLayout
            }

            reviewStep.didInitiate = { (navigation, paymentModel) in
                return StackStep.build(with: reviewStep, omnipaymentsNavigation: navigation, paymentModel: paymentModel)
            }

            reviewStep.didComplete = { (navigation, paymentModel, viewController) in
                let internationalPaymentOrder = InternationalPaymentOrder(with: paymentModel)
                navigation.persist(internationalPaymentOrder, contact: nil)
            }

            return reviewStep
        }()

        private static func conversionRelatedFields(using paymentModel: OmniPayment) -> [UIView] {
            let separatorView = LineSeparator.build(margin: UIEdgeInsets(top: 16, left: 16, bottom: 0, right: 0))

            return [
                getYouPayAmountLabel(using: paymentModel),
                getTransferFeeAmountLabel(using: paymentModel),
                getExchangeRateAmountLabel(using: paymentModel),
                getPaymentPriorityDescriptionLabel(using: paymentModel),
                separatorView
            ].compactMap { $0 }
        }

        private static func getPaymentPriorityDescriptionLabel(using paymentModel: OmniPayment) -> UIView? {
            guard let paymentPriority = paymentModel.priority else { return nil }

            let paymentChargeBearerTranslation: (String?) -> (String) = { item in
                switch item {
                case "OUR":
                    return localized("omniPayments.international.paymentDetails.paymentFeeSelector.list.opt.our.title")()
                case "BEN":
                    return localized("omniPayments.international.paymentDetails.paymentFeeSelector.list.opt.ben.title")()
                case "SHA":
                    return localized("omniPayments.international.paymentDetails.paymentFeeSelector.list.opt.sha.title")()
                default:
                    return ""
                }
            }

            let entryOne = paymentPriority.title
            let entryTwo = paymentPriority.description
            let entryThree = paymentChargeBearerTranslation(paymentModel.chargeBearer)

            let disclaimerText = [entryOne, entryTwo, entryThree].joined(separator: " • ")

            var configuration = Text.Configuration()
            configuration.design.text = { label in
                label.textColor = DesignSystem.shared.colors.text.support
                label.font = DesignSystem.shared.fonts.preferredFont(.subheadline, .regular)
                label.numberOfLines = 0
            }

            return Text.build(
                textValue: disclaimerText,
                configuration: configuration,
                margin: UIEdgeInsets(top: 0, left: 16, bottom: 0, right: 16))
        }

        private static func getExchangeRateAmountLabel(using paymentModel: OmniPayment) -> UIView? {
            if paymentModel.exchangeRate == nil || paymentModel.isTransferOnSameCurrency {
                return nil
            }

            let exchangeRate = String(format: "%f", paymentModel.exchangeRate?.doubleValue ?? 0)
            let exchangeText = String(
                format: localized("omniPayments.international.paymentReview.amountBlock.currencyExchangeFormat")(),
                locale: Locale.current,
                paymentModel.fromAccount.currencyCode,
                exchangeRate,
                paymentModel.toAccount.currencyCode)

            var configuration = Text.Configuration()
            configuration.design.text = { label in
                label.textColor = DesignSystem.shared.colors.text.support
                label.font = DesignSystem.shared.fonts.preferredFont(.subheadline, .regular)
                label.numberOfLines = 0
            }

            return Text.build(
                textValue: exchangeText,
                configuration: configuration,
                margin: UIEdgeInsets(top: 0, left: 16, bottom: 16, right: 16))
        }

        private static func getYouPayAmountLabel(using paymentModel: OmniPayment) -> UIView? {
            if paymentModel.isTransferOnSameCurrency {
                return nil
            }

            let configuration: HorizontalTitleSubtitlePreview.Configuration = {
                var conf = HorizontalTitleSubtitlePreview.Configuration()

                conf.design.title = { label in
                    label.textColor = DesignSystem.shared.colors.text.default
                    label.font = DesignSystem.shared.fonts.preferredFont(.subheadline, .semibold)
                }

                conf.design.subtitle = { label in
                    label.font = DesignSystem.shared.fonts.preferredFont(.headline, .semibold)

                    if paymentModel.exchangeRate == nil {
                        label.textColor = DesignSystem.shared.colors.text.disabled
                    } else {
                        label.textColor = DesignSystem.shared.colors.text.default
                    }
                }

                return conf
            }()

            return HorizontalTitleSubtitlePreview.build(
                titleValue: {
                    localized("omniPayments.international.paymentReview.amountBlock.youPay.title")()
                },
                titleEnabled: { _ in true },
                subtitleValue: {
                    if paymentModel.exchangeRate == nil {
                        return localized("omniPayments.international.paymentReview.amountBlock.unavailable.title")()
                    } else {
                        return paymentModel.amountFormattedUsingSenderCurrency ?? "0"
                    }
                },
                subtitleEnabled: { _ in true },
                configuration: configuration,
                margin: UIEdgeInsets(top: 0, left: 16, bottom: 0, right: 16))
        }

        private static func getTransferFeeAmountLabel(using paymentModel: OmniPayment) -> UIView? {
            guard let paymentPriority = paymentModel.priority else { return nil }

            let configuration: HorizontalTitleSubtitlePreview.Configuration = {
                var conf = HorizontalTitleSubtitlePreview.Configuration()

                conf.design.title = { label in
                    label.textColor = DesignSystem.shared.colors.text.default
                    label.font = DesignSystem.shared.fonts.preferredFont(.subheadline, .semibold)
                }

                conf.design.subtitle = { label in
                    label.textColor = DesignSystem.shared.colors.text.default
                    label.font = DesignSystem.shared.fonts.preferredFont(.headline, .semibold)
                }

                return conf
            }()

            let marginTop: CGFloat = paymentModel.isTransferOnSameCurrency ? 0 : 10

            return HorizontalTitleSubtitlePreview.build(
                    titleValue: {
                        localized("omniPayments.international.paymentReview.amountBlock.transferFee.title")()
                    },
                    titleEnabled: { _ in true },
                    subtitleValue: { paymentPriority.amount },
                    subtitleEnabled: { _ in true },
                    configuration: configuration,
                    margin: UIEdgeInsets(top: marginTop, left: 16, bottom: 16, right: 16))
        }

        private static func buildBankDetails(for bank: OmniPayment.Bank, with title: String) -> [UIView] {
            var views: [UIView] = []

            let lightTextConfiguration: Text.Configuration = {
                var conf = Text.Configuration()

                conf.design.text = { label in
                    label.textColor = DesignSystem.shared.colors.text.support
                    label.font = DesignSystem.shared.fonts.preferredFont(.subheadline, .regular)
                    label.numberOfLines = 0
                }

                return conf
            }()

            let emphTextConfiguration: Text.Configuration = {
                var conf = Text.Configuration()

                conf.design.text = { label in
                    label.textColor = DesignSystem.shared.colors.text.default
                    label.font = DesignSystem.shared.fonts.preferredFont(.headline, .semibold)
                }

                return conf
            }()

            views.append(Text.build(textValue: title,
                                    configuration: lightTextConfiguration,
                                    margin: UIEdgeInsets(top: 16, left: 16, bottom: 0, right: 16)))

            let swiftCode = localized("omniPayments.international.paymentReview.bankInfoBlock.swiftBicCode.title")()
            let bicCode = "\(swiftCode) \(bank.bic ?? "")"
            views.append(Text.build(textValue: bicCode,
                                    configuration: emphTextConfiguration,
                                    margin: UIEdgeInsets(top: 4, left: 16, bottom: 0, right: 16)))

            views.append(Text.build(textValue: bank.name ?? "",
                                    configuration: emphTextConfiguration,
                                    margin: UIEdgeInsets(top: 4, left: 16, bottom: 0, right: 16)))

            views.append(Text.build(textValue: bank.postalAddress?.formatted ?? "",
                                    configuration: lightTextConfiguration,
                                    margin: UIEdgeInsets(top: 4, left: 16, bottom: 0, right: 16)))

            views.append(LineSeparator.build(margin: UIEdgeInsets(top: 16, left: 16, bottom: 0, right: 0)))
            return views
        }
    }
}
