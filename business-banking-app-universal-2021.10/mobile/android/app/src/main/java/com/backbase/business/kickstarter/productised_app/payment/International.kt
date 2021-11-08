package com.backbase.business.kickstarter.productised_app.payment

import android.content.Context
import com.backbase.android.business.journey.common.date.DateFormatter
import com.backbase.android.business.journey.omnipayments.configuration.*
import com.backbase.android.business.journey.omnipayments.configuration.international.*
import com.backbase.android.business.journey.omnipayments.ext.getAvailableAccountNumber
import com.backbase.android.business.journey.omnipayments.model.PaymentAccount
import com.backbase.android.business.journey.omnipayments.model.ViewMargin
import com.backbase.android.business.journey.omnipayments.view.epoxy.model.*
import com.backbase.android.business.journey.common.date.DateType
import com.backbase.android.business.journey.common.extensions.isToday
import com.backbase.android.business.journey.common.extensions.navigateSafe
import com.backbase.android.business.journey.common.util.ProductUtil
import com.backbase.android.business.journey.omnipayments.model.TrackerScreenName
import com.backbase.android.business.journey.omnipayments.model.paymentoption.*
import com.backbase.android.business.journey.omnipayments.model.usecase.*
import com.backbase.android.common.utils.formatter.SepaFormatter
import com.backbase.android.design.amount.AmountFormatter
import com.backbase.business.kickstarter.productised_app.CountryResolver.buildCountryImageResolver
import com.backbase.business.kickstarter.productised_app.CountryResolver.buildCountryNameResolver
import com.backbase.business.kickstarter.productised_app.CurrencyResolver.buildCurrencyImageResolver
import com.backbase.business.kickstarter.productised_app.CurrencyResolver.buildCurrencyNameResolver
import com.backbase.business.kickstarter.productised_app.R
import com.backbase.deferredresources.*
import java.math.BigDecimal

/**
 * Created by Backbase R&D B.V on 17/02/2021.
 *
 * International payments configuration
 */
object International {

    private fun generatePrioritySubtitle(context: Context, priority: PriorityOption?): String {
        priority?.executionTime?.let { executionTime ->
            val minDays = executionTime.min
            val maxDays = executionTime.max
            maxDays?.let {
                val unit = when (executionTime.unit) {
                    ExecutionTime.Unit.HOUR -> DeferredPlurals.Resource(R.plurals.international_priority_unit_hour)
                    ExecutionTime.Unit.BUSINESSDAY -> DeferredPlurals.Resource(R.plurals.international_priority_unit_business_days)
                    ExecutionTime.Unit.CALENDARDAY -> DeferredPlurals.Resource(R.plurals.international_priority_unit_calendar_days)
                    else -> null
                }?.resolve(context, maxDays) ?: ""
                val cutOffTime = priority.cutOffTime

                return when {
                    cutOffTime.isNullOrBlank() -> when (minDays) {
                        null -> DeferredFormattedString.Resource(R.string.international_priority_subtitle_max)
                            .resolve(context, maxDays, unit)
                        else -> DeferredFormattedString.Resource(R.string.international_priority_subtitle_min_max)
                            .resolve(context, minDays, maxDays, unit)
                    }
                    else -> when (minDays) {
                        null ->
                            DeferredFormattedString.Resource(R.string.international_priority_subtitle_max_cutoff)
                                .resolve(context, maxDays, unit, cutOffTime)
                        else -> DeferredFormattedString.Resource(R.string.international_priority_subtitle_min_max_cutoff)
                            .resolve(context, minDays, maxDays, unit, cutOffTime)
                    }
                }
            }
        }
        return ""
    }

    val payment = Payment {
        icon = DeferredDrawable.Resource(R.drawable.ic_baseline_international_24)
        title = DeferredText.Resource(R.string.international)
        subtitle = DeferredText.Resource(R.string.international_desc)
        routerName = "international"
        configuration = OmniPaymentsConfiguration {
            navigationGraph = R.navigation.navigation_international
            steps = Steps {
                +Step {
                    id = R.id.fromAccount
                    analyticsLabel = { TrackerScreenName.PaymentStep.SELECT_ORIGINATOR }
                    title = DeferredText.Resource(R.string.international)
                    subTitle = DeferredText.Resource(R.string.from_account)
                    layoutBuilder = { context, fragmentManager, navController, omniPaymentModel ->
                        AccountsListLayout {
                            enableSearch = true
                            accountFetchParameters = AccountFetchParameters {
                                businessFunction =
                                    AccountFetchParameters.BusinessFunction.US_FOREIGN_WIRE
                                resourceName = AccountFetchParameters.ResourceName.PAYMENTS
                                accountType = AccountFetchParameters.AccountType.DEBIT
                                privilege = AccountFetchParameters.Privilege.CREATE
                            }
                            text = AccountsListTextConfiguration {
                                searchHint = DeferredText.Resource(R.string.accounts_search)
                                errorTitle =
                                    DeferredText.Resource(R.string.shared_error_failed_title)
                                errorMessage =
                                    DeferredText.Resource(R.string.shared_error_load_failed_message)
                                emptyResultTitle =
                                    DeferredText.Resource(R.string.shared_search_error_no_results_title)
                                emptyResultMessage =
                                    DeferredText.Resource(R.string.shared_empty_message)
                                noSearchResultsTitle =
                                    DeferredText.Resource(R.string.shared_search_error_no_results_title)
                                noSearchResultsMessage =
                                    DeferredText.Resource(R.string.shared_search_error_no_results_message)
                                retryButtonText =
                                    DeferredText.Resource(R.string.shared_error_failed_button_retry)
                            }
                            image = AccountsListImageConfiguration {
                                errorImage =
                                    DeferredDrawable.Resource(R.drawable.ic_warning)
                                noInternetImage =
                                    DeferredDrawable.Resource(R.drawable.ic_signal_wifi_off_black_24dp)
                                noSearchResultImage =
                                    DeferredDrawable.Resource(R.drawable.ic_search)
                                noResultsImage =
                                    DeferredDrawable.Resource(R.drawable.ic_accounts)
                            }

                            uiMapper = AccountListUiDataMapper { }
                            accountMapper =
                                AccountMapper {
                                    availableFunds = { model ->
                                        when (model.productKindName) {
                                            ProductUtil.ProductKind.CURRENT_ACCOUNT.title,
                                            ProductUtil.ProductKind.SAVINGS_ACCOUNT.title,
                                            ProductUtil.ProductKind.LOAN.title,
                                            ProductUtil.ProductKind.CREDIT_CARD.title
                                            -> model.bookedBalance ?: BigDecimal.ZERO
                                            ProductUtil.ProductKind.TERM_DEPOSIT.title -> model.principalAmount
                                                ?: BigDecimal.ZERO
                                            ProductUtil.ProductKind.INVESTMENT_ACCOUNT.title -> model.currentInvestmentValue
                                                ?: BigDecimal.ZERO
                                            else -> model.availableBalance ?: BigDecimal.ZERO
                                        }
                                    }
                                }
                        }
                    }
                    onComplete =
                        { context, navController, createPayment, omniPaymentModel, selectedAccount ->
                            navController.navigateSafe(R.id.action_fromAccount_to_transferTo)
                        }
                }
                +Step {
                    id = R.id.transferTo
                    analyticsLabel = { TrackerScreenName.PaymentStep.SELECT_BENEFICIARY }
                    title = DeferredText.Resource(R.string.international)
                    subTitle = DeferredText.Resource(R.string.transfer_to)
                    layoutBuilder = { context, fragmentManager, navController, omniPaymentModel ->
                        ContactsListLayout {
                            enableSearch = true
                            listButton = { isVisible, onClick ->
                                isVisible(true)
                                onClick {
                                    omniPaymentModel.toAccount = PaymentAccount()
                                    navController.navigateSafe(R.id.action_transferTo_to_newBeneficiary)
                                }
                            }
                            text = ContactsListTextConfiguration {
                                searchHint = DeferredText.Resource(R.string.beneficiaries_search)
                                errorTitle =
                                    DeferredText.Resource(R.string.shared_error_failed_title)
                                errorMessage =
                                    DeferredText.Resource(R.string.shared_error_load_failed_message)
                                emptyResultTitle =
                                    DeferredText.Resource(R.string.shared_search_error_no_results_title)
                                emptyResultMessage =
                                    DeferredText.Resource(R.string.shared_empty_message)
                                noSearchResultsTitle =
                                    DeferredText.Resource(R.string.shared_search_error_no_results_title)
                                noSearchResultsMessage =
                                    DeferredText.Resource(R.string.shared_search_error_no_results_message)
                                retryButtonText =
                                    DeferredText.Resource(R.string.shared_error_failed_button_retry)
                                listButtonText = DeferredText.Resource(R.string.add_beneficiary)
                            }
                            image = ContactsListImageConfiguration {
                                errorImage =
                                    DeferredDrawable.Resource(R.drawable.ic_warning)
                                noInternetImage =
                                    DeferredDrawable.Resource(R.drawable.ic_signal_wifi_off_black_24dp)
                                noSearchResultImage =
                                    DeferredDrawable.Resource(R.drawable.ic_search)
                                noResultsImage =
                                    DeferredDrawable.Resource(R.drawable.ic_accounts)
                                listButtonImage =
                                    DeferredDrawable.Resource(R.drawable.ic_outline_person_add_24)
                            }

                            uiMapper = ContactListUiDataMapper { }

                            contactMapper = ContactMapper { }
                        }
                    }
                    onComplete =
                        { context, navController, createPayment, omniPaymentModel, selectedAccount ->
                            navController.navigateSafe(R.id.action_transferTo_to_newBeneficiary)
                        }
                }
                +Step {
                    id = R.id.newBeneficiary
                    analyticsLabel = { omniPayment ->
                        if (omniPayment.toAccount.accountName.isBlank() && omniPayment.toAccount.contactName.isBlank())
                            TrackerScreenName.PaymentStep.CREATE_BENEFICIARY
                        else
                            TrackerScreenName.PaymentStep.EDIT_BENEFICIARY
                    }
                    title = DeferredText.Resource(R.string.add_beneficiary)
                    layoutBuilder = { context, fragmentManager, navController, omniPaymentModel ->
                        InternationalLayout.Beneficiary {

                            val countryPickerConfiguration = SanctionedEntryPickerConfiguration {
                                text = SanctionedEntriesPickerTextConfiguration {
                                    inputLabel = DeferredText.Resource(R.string.country_label)
                                }
                                isSearchEnabled = { true }
                                image = ListImageConfiguration {}
                                countryNameResolver = buildCountryNameResolver()
                                countryImageResolver = buildCountryImageResolver(context)
                            }

                            bankCountryPicker = countryPickerConfiguration

                            beneficiaryAddress = AddressInputPreviewConfiguration {
                                text = AddressInputPreviewTextConfiguration {
                                    newAddressTitle =
                                        DeferredText.Resource(R.string.international_beneficiary_address_new)
                                    editAddressTitle =
                                        DeferredText.Resource(R.string.international_beneficiary_address_edit)
                                }
                                countryPicker = countryPickerConfiguration
                                isRequired = false
                            }
                            bankName = InlineTextInputConfiguration {
                                primaryLabel =
                                    DeferredText.Resource(R.string.international_bank_name)
                                secondaryLabel =
                                    DeferredText.Resource(R.string.optional_indicator_label)
                            }
                            bankAddress = AddressInputPreviewConfiguration {
                                text = AddressInputPreviewTextConfiguration {
                                    newAddressTitle =
                                        DeferredText.Resource(R.string.international_bank_address_new)
                                    editAddressTitle =
                                        DeferredText.Resource(R.string.international_bank_address_edit)
                                }
                                countryPicker = countryPickerConfiguration
                                isRequired = false
                            }
                        }
                    }
                    onComplete =
                        { context, navController, createPayment, omniPaymentModel, skipStep ->
                            if (skipStep == true) {
                                navController.popBackStack()
                                navController.navigateSafe(R.id.action_transferTo_to_paymentDetails)
                            } else
                                navController.navigateSafe(R.id.action_newBeneficiary_to_paymentDetails)
                        }
                }
                +Step {
                    id = R.id.paymentDetails
                    analyticsLabel = { TrackerScreenName.PaymentStep.PAYMENT_DETAILS }
                    title = DeferredText.Resource(R.string.payment_details)
                    layoutBottomOffset = R.dimen.bottom_offset_payment_details
                    layoutBuilder = { context, fragmentManager, navController, omniPaymentModel ->
                        InternationalLayout.PaymentDetails {
                            fromAccount = { omniPaymentModel.fromAccount.accountName }
                            beneficiaryAccount = { omniPaymentModel.toAccount.contactName }
                            bottomSheet = InternationalLayout.PaymentDetailsBottomSheet {
                                transferAmount = TransferAmountOverview {
                                    title = DeferredText.Resource(R.string.amount_to_transfer)
                                    titleTextAppearance = R.style.InternationalSubtitle1Medium
                                    subtitleTextAppearance = R.style.InternationalSubtitle1Medium
                                }

                                transferAmountConverted = TransferAmountOverview {
                                    title = DeferredText.Resource(R.string.you_pay)
                                    titleTextAppearance = R.attr.textAppearanceSubtitle2
                                    subtitleTextAppearance = R.attr.textAppearanceSubtitle2
                                }

                                transferFeeAmount = TransferAmountOverview {
                                    title = DeferredText.Resource(R.string.transfer_fee_label)
                                    titleTextAppearance = R.attr.textAppearanceSubtitle2
                                    subtitleTextAppearance = R.attr.textAppearanceSubtitle2
                                }
                            }
                            paymentDescription = PaymentDescription {
                                text = TextInputPreviewTextConfiguration {
                                    //preview
                                    previewTitle = DeferredText.Resource(R.string.payment_desc)
                                    previewDescription =
                                        DeferredText.Resource(R.string.payment_optional)
                                    //input
                                    inputTitle = DeferredText.Resource(R.string.payment_input_title)
                                    inputDescription =
                                        DeferredText.Resource(R.string.payment_input_desc)
                                    inputAllowedCharactersDescription =
                                        DeferredText.Resource(R.string.payment_text_input_message_allowed_character)
                                    inputSuggestions = DeferredTextArray.Resource(
                                        R.array.description_suggestions,
                                        DeferredTextArray.Resource.Type.STRING
                                    )
                                }
                                value = { omniPaymentModel.description }
                                requiredValue = false
                                forbiddenCharacters = charArrayOf('\\')
                            }
                            amount = PaymentAmount {
                                submitValidators = listOf(::isNotZero)
                                availableFunds = {
                                    val amount = omniPaymentModel.fromAccount.availableFunds
                                    val currency = omniPaymentModel.fromAccount.currencyCode
                                    val formattedValue = currency?.let { safeCurrency ->
                                        AmountFormatter(amount).formatIsoCurrencyAmount(
                                            context.getSystemLocale(),
                                            safeCurrency,
                                            false
                                        )
                                    } ?: amount

                                    context.getString(R.string.available_funds, formattedValue)
                                }
                                currencyNameResolver = buildCurrencyNameResolver()
                                currencyImageResolver = buildCurrencyImageResolver()
                            }
                            val bankNameConfig = InlineTextInputConfiguration {
                                primaryLabel =
                                    DeferredText.Resource(R.string.international_bank_name)
                                secondaryLabel =
                                    DeferredText.Resource(R.string.optional_indicator_label)
                            }
                            val bankAddressConfig = AddressInputPreviewConfiguration {
                                text = AddressInputPreviewTextConfiguration {
                                    newAddressTitle =
                                        DeferredText.Resource(R.string.international_bank_address_new)
                                    editAddressTitle =
                                        DeferredText.Resource(R.string.international_bank_address_edit)
                                }
                                countryPicker = SanctionedEntryPickerConfiguration {
                                    countryNameResolver = buildCountryNameResolver()
                                    countryImageResolver = buildCountryImageResolver(context)
                                }
                            }
                            paymentRouting = PaymentRouting {
                                text = RoutingInputPreviewTextConfiguration {
                                    inputCorrespondentNameLabel = bankNameConfig
                                    inputCorrespondentAddress = bankAddressConfig
                                    inputIntermediaryNameLabel = bankNameConfig
                                    inputIntermediaryAddress = bankAddressConfig
                                }
                            }
                            paymentSchedule = PaymentSchedule {
                                text = ScheduleInputPreviewTextConfiguration { }
                                showDotExitLine = false
                            }

                            paymentPriority = PaymentPriority {
                                submitValidators = listOf(::deliveryOptionsNotEmpty)
                            }

                            paymentChargeBearer = PaymentChargeBearer {
                                label =
                                    DeferredText.Resource(R.string.international_how_fees_are_paid)
                                hint =
                                    DeferredText.Resource(R.string.international_charge_bearer_hint)
                                title =
                                    DeferredText.Resource(R.string.international_how_fees_are_paid)
                                submitValidators = listOf(::chargeBearerNotEmpty)
                                listMapper = { itemId ->
                                    when (itemId) {
                                        "OUR" -> TitleDescriptionModel {
                                            id = "OUR"
                                            title =
                                                DeferredText.Resource(R.string.international_charge_bearer_our_title)
                                            description =
                                                DeferredText.Resource(R.string.international_charge_bearer_our_desc)
                                        }
                                        "BEN" -> TitleDescriptionModel {
                                            id = "BEN"
                                            title =
                                                DeferredText.Resource(R.string.international_charge_bearer_ben_title)
                                            description =
                                                DeferredText.Resource(R.string.international_charge_bearer_ben_desc)
                                        }
                                        "SHA" -> TitleDescriptionModel {
                                            id = "SHA"
                                            title =
                                                DeferredText.Resource(R.string.international_charge_bearer_sha_title)
                                            description =
                                                DeferredText.Resource(R.string.international_charge_bearer_sha_desc)
                                        }
                                        else -> TitleDescriptionModel { }
                                    }
                                }
                            }
                        }
                    }
                    onComplete =
                        { context, navController, createPayment, omniPaymentModel, selectedAccount ->
                            navController.navigateSafe(R.id.action_paymentDetails_to_review)
                        }
                }
                +Step {
                    id = R.id.review
                    analyticsLabel = { TrackerScreenName.PaymentStep.PAYMENT_REVIEW }
                    title = DeferredText.Resource(R.string.international)
                    subTitle = DeferredText.Resource(R.string.review_payment)
                    layoutBuilder = { context, fragmentManager, navController, omniPaymentModel ->
                        StackLayout {
                            text = StackLayoutTextConfiguration {
                                continueButton = DeferredText.Resource(R.string.custom_submit)
                            }
                            +TransferAccountReviewView_().apply {
                                id(1)
                                margin(ViewMargin(16, 16, 16, 16))
                                fromLabel(DeferredText.Resource(R.string.from_label))
                                fromAccountName(omniPaymentModel.fromAccount.accountName)
                                fromAccountNumber(SepaFormatter.format(omniPaymentModel.fromAccount.getAvailableAccountNumber()))
                                toLabel(DeferredText.Resource(R.string.to_label))
                                toAccountName(omniPaymentModel.toAccount.contactAccountName)
                                toAccountNumber(SepaFormatter.format(omniPaymentModel.toAccount.getAvailableAccountNumber()))
                            }
                            +TransferTitleSubtitlePreviewView_().apply {
                                id(2)
                                title(DeferredText.Resource(R.string.amount_to_transfer))
                                blankSubtitlePlaceholder(DeferredText.Resource(R.string.no_amount))
                                subtitle {
                                    val currencyCode =
                                        omniPaymentModel.toAccount.currencyCode ?: ""
                                    AmountFormatter(omniPaymentModel.amount)
                                        .formatIsoCurrencyAmount(
                                            locale = context.getSystemLocale(),
                                            currencyCode = currencyCode,
                                            addPositiveSign = false
                                        )
                                }
                                subtitleTextAppearance(R.attr.textAppearanceHeadline6)
                                hideBottomSeparator { true }
                            }

                            val from = omniPaymentModel.fromAccount.currencyCode
                            val to = omniPaymentModel.toAccount.currencyCode
                            val exchangeRate = omniPaymentModel.exchangeRate

                            if (from != to && exchangeRate != BigDecimal.ZERO) {
                                +HorizontalTitleSubtitlePreviewView_().apply {
                                    id(3)
                                    margin(ViewMargin(16, 10, 16, 0))
                                    titleTextAppearance(R.attr.textAppearanceBody2Medium)
                                    subtitleTextAppearance(R.attr.textAppearanceBody1Medium)
                                    titleValue { context.getString(R.string.international_you_pay) }
                                    subtitleValue {
                                        val amount = omniPaymentModel.amount.divide(
                                            omniPaymentModel.exchangeRate,
                                            2,
                                            BigDecimal.ROUND_HALF_UP
                                        )
                                        val currency = omniPaymentModel.fromAccount.currencyCode
                                        val formattedValue = currency?.let { safeCurrency ->
                                            AmountFormatter(amount).formatIsoCurrencyAmount(
                                                context.getSystemLocale(),
                                                safeCurrency,
                                                false
                                            )
                                        } ?: amount

                                        "≈ $formattedValue"
                                    }
                                }
                            }
                            +HorizontalTitleSubtitlePreviewView_().apply {
                                id(4)
                                margin(ViewMargin(16, 12, 16, 0))
                                titleTextAppearance(R.attr.textAppearanceBody2Medium)
                                subtitleTextAppearance(R.attr.textAppearanceBody1Medium)
                                titleValue { context.getString(R.string.international_transfer_fee) }
                                subtitleValue {
                                    val currency = omniPaymentModel.transferFeeCurrencyCode
                                    val formattedValue = currency?.let { safeCurrency ->
                                        AmountFormatter(omniPaymentModel.transferFee).formatIsoCurrencyAmount(
                                            context.getSystemLocale(),
                                            safeCurrency,
                                            false
                                        )
                                    } ?: BigDecimal.ZERO

                                    "$formattedValue"
                                }
                            }
                            when {
                                exchangeRate == BigDecimal.ZERO -> +TextView_().apply {
                                    id(5)
                                    margin(ViewMargin(16, 12, 16, 0))
                                    textAppearance(R.style.InternationalTextView)
                                    textValue {
                                        context.getString(R.string.currency_exchange_error)
                                    }
                                }

                                from != to -> +TextView_().apply {
                                    id(5)
                                    margin(ViewMargin(16, 12, 16, 0))
                                    textAppearance(R.style.InternationalTextView)
                                    textValue {
                                        val rate = omniPaymentModel.exchangeRate.toString()
                                        val label =
                                            context.getString(R.string.international_exchange_rate_label)
                                        "$label 1 $from ≈ $rate $to"
                                    }
                                }
                            }
                            +TextView_().apply {
                                id(6)
                                margin(ViewMargin(16, 8, 16, 0))
                                textAppearance(R.style.InternationalTextView)
                                textValue {
                                    val chargeBearer = when (omniPaymentModel.chargeBearer) {
                                        "OUR" -> context.getString(R.string.international_charge_bearer_our_title)
                                        "BEN" -> context.getString(R.string.international_charge_bearer_ben_title)
                                        "SHA" -> context.getString(R.string.international_charge_bearer_sha_title)
                                        else -> ""
                                    }

                                    val priorityTypeTitle =
                                        when (omniPaymentModel.priority?.instructionPriority) {
                                            InstructionPriority.NORM ->
                                                context.getString(R.string.international_priority_title_norm)
                                            InstructionPriority.HIGH ->
                                                context.getString(R.string.international_priority_title_high)
                                            else -> ""
                                        }

                                    val priorityTypeSubTitle = generatePrioritySubtitle(
                                        context,
                                        omniPaymentModel.priority
                                    )

                                    val priority = DeferredFormattedString
                                        .Resource(R.string.international_priority_title)
                                        .resolve(
                                            context,
                                            priorityTypeTitle,
                                            priorityTypeSubTitle,
                                            chargeBearer
                                        )

                                    priority
                                }
                            }
                            +SeparatorView_().apply {
                                id(7)
                                margin(ViewMargin(16, 16, 0, 0))
                            }

                            omniPaymentModel.correspondentBank?.apply {
                                val hasBankBranchCode = !bankBranchCode.isNullOrEmpty()
                                val hasName = !name.isNullOrEmpty()
                                val hasPostalAddress = !bankAddress?.postCode.isNullOrEmpty()

                                when {
                                    hasBankBranchCode && hasName && hasPostalAddress -> {
                                        +TransferTitleSubtitlePreviewView_().apply {
                                            id(8)
                                            margin(ViewMargin(0, 16, 0, 0))
                                            title(DeferredText.Resource(R.string.international_correspondent_bank))
                                            subtitle {
                                                val label =
                                                    context.getString(R.string.international_swift_bic_label)
                                                "$label $bankBranchCode"
                                            }
                                            subtitleTextAppearance(R.attr.textAppearanceBody1Medium)
                                            hideBottomSeparator { true }
                                        }
                                        +TextView_().apply {
                                            id(81)
                                            margin(ViewMargin(16, 4, 16, 0))
                                            textAppearance(R.attr.textAppearanceBody1Medium)
                                            textValue { name }
                                        }
                                        +TextView_().apply {
                                            id(82)
                                            margin(ViewMargin(16, 4, 16, 0))
                                            textAppearance(R.style.InternationalTextView)
                                            textValue {
                                                listOfNotNull(
                                                    bankAddress?.streetName,
                                                    bankAddress?.addressLine1,
                                                    bankAddress?.addressLine2,
                                                    bankAddress?.postCode,
                                                    bankAddress?.town,
                                                    bankAddress?.countrySubDivision,
                                                    bankAddress?.country
                                                ).joinToString()
                                            }
                                        }
                                        +SeparatorView_().apply {
                                            id(82)
                                            margin(ViewMargin(16, 16, 0, 0))
                                        }
                                    }
                                    hasBankBranchCode && hasName -> {
                                        +TransferTitleSubtitlePreviewView_().apply {
                                            id(8)
                                            margin(ViewMargin(0, 16, 0, 0))
                                            title(DeferredText.Resource(R.string.international_correspondent_bank))
                                            subtitle {
                                                val label =
                                                    context.getString(R.string.international_swift_bic_label)
                                                "$label $bankBranchCode"
                                            }
                                            subtitleTextAppearance(R.attr.textAppearanceBody1Medium)
                                            hideBottomSeparator { true }
                                        }
                                        +TextView_().apply {
                                            id(81)
                                            margin(ViewMargin(16, 4, 16, 0))
                                            textAppearance(R.attr.textAppearanceBody1Medium)
                                            textValue { name }
                                        }
                                        +SeparatorView_().apply {
                                            id(82)
                                            margin(ViewMargin(16, 16, 0, 0))
                                        }
                                    }
                                    hasBankBranchCode && hasPostalAddress -> {
                                        +TransferTitleSubtitlePreviewView_().apply {
                                            id(8)
                                            margin(ViewMargin(0, 16, 0, 0))
                                            title(DeferredText.Resource(R.string.international_correspondent_bank))
                                            subtitle {
                                                val label =
                                                    context.getString(R.string.international_swift_bic_label)
                                                "$label $bankBranchCode"
                                            }
                                            subtitleTextAppearance(R.attr.textAppearanceBody1Medium)
                                            hideBottomSeparator { true }
                                        }
                                        +TextView_().apply {
                                            id(82)
                                            margin(ViewMargin(16, 4, 16, 0))
                                            textAppearance(R.style.InternationalTextView)
                                            textValue {
                                                listOfNotNull(
                                                    bankAddress?.streetName,
                                                    bankAddress?.addressLine1,
                                                    bankAddress?.addressLine2,
                                                    bankAddress?.postCode,
                                                    bankAddress?.town,
                                                    bankAddress?.countrySubDivision,
                                                    bankAddress?.country
                                                ).joinToString()
                                            }
                                        }
                                        +SeparatorView_().apply {
                                            id(82)
                                            margin(ViewMargin(16, 16, 0, 0))
                                        }
                                    }
                                    hasBankBranchCode -> {
                                        +TransferTitleSubtitlePreviewView_().apply {
                                            id(8)
                                            margin(ViewMargin(0, 16, 0, 0))
                                            title(DeferredText.Resource(R.string.international_correspondent_bank))
                                            subtitle {
                                                val label =
                                                    context.getString(R.string.international_swift_bic_label)
                                                "$label $bankBranchCode"
                                            }
                                            subtitleTextAppearance(R.attr.textAppearanceBody1Medium)
                                            hideBottomSeparator { false }
                                        }
                                    }
                                }
                            }

                            omniPaymentModel.intermediaryBank?.apply {
                                val hasBankBranchCode = !bankBranchCode.isNullOrEmpty()
                                val hasName = !name.isNullOrEmpty()
                                val hasPostalAddress = !bankAddress?.postCode.isNullOrEmpty()

                                when {
                                    hasBankBranchCode && hasName && hasPostalAddress -> {
                                        +TransferTitleSubtitlePreviewView_().apply {
                                            id(9)
                                            margin(ViewMargin(0, 16, 0, 0))
                                            title(DeferredText.Resource(R.string.international_correspondent_bank))
                                            subtitle {
                                                val label =
                                                    context.getString(R.string.international_swift_bic_label)
                                                "$label $bankBranchCode"
                                            }
                                            subtitleTextAppearance(R.attr.textAppearanceBody1Medium)
                                            hideBottomSeparator { true }
                                        }
                                        +TextView_().apply {
                                            id(91)
                                            margin(ViewMargin(16, 4, 16, 0))
                                            textAppearance(R.attr.textAppearanceBody1Medium)
                                            textValue { name }
                                        }
                                        +TextView_().apply {
                                            id(92)
                                            margin(ViewMargin(16, 4, 16, 0))
                                            textAppearance(R.style.InternationalTextView)
                                            textValue {
                                                listOfNotNull(
                                                    bankAddress?.streetName,
                                                    bankAddress?.addressLine1,
                                                    bankAddress?.addressLine2,
                                                    bankAddress?.postCode,
                                                    bankAddress?.town,
                                                    bankAddress?.countrySubDivision,
                                                    bankAddress?.country
                                                ).joinToString()
                                            }
                                        }
                                        +SeparatorView_().apply {
                                            id(93)
                                            margin(ViewMargin(16, 16, 0, 0))
                                        }
                                    }
                                    hasBankBranchCode && hasPostalAddress -> {
                                        +TransferTitleSubtitlePreviewView_().apply {
                                            id(9)
                                            margin(ViewMargin(0, 16, 0, 0))
                                            title(DeferredText.Resource(R.string.international_correspondent_bank))
                                            subtitle {
                                                val label =
                                                    context.getString(R.string.international_swift_bic_label)
                                                "$label $bankBranchCode"
                                            }
                                            subtitleTextAppearance(R.attr.textAppearanceBody1Medium)
                                            hideBottomSeparator { true }
                                        }
                                        +TextView_().apply {
                                            id(91)
                                            margin(ViewMargin(16, 4, 16, 0))
                                            textAppearance(R.style.InternationalTextView)
                                            textValue {
                                                listOfNotNull(
                                                    bankAddress?.streetName,
                                                    bankAddress?.addressLine1,
                                                    bankAddress?.addressLine2,
                                                    bankAddress?.postCode,
                                                    bankAddress?.town,
                                                    bankAddress?.countrySubDivision,
                                                    bankAddress?.country
                                                ).joinToString()
                                            }
                                        }
                                        +SeparatorView_().apply {
                                            id(92)
                                            margin(ViewMargin(16, 16, 0, 0))
                                        }
                                    }
                                    hasBankBranchCode -> {
                                        +TransferTitleSubtitlePreviewView_().apply {
                                            id(9)
                                            margin(ViewMargin(0, 16, 0, 0))
                                            title(DeferredText.Resource(R.string.international_intermediary_bank))
                                            subtitle {
                                                val label =
                                                    context.getString(R.string.international_swift_bic_label)
                                                "$label $bankBranchCode"
                                            }
                                            subtitleTextAppearance(R.attr.textAppearanceBody1Medium)
                                            hideBottomSeparator { false }
                                        }
                                    }
                                }
                            }

                            +TransferTitleSubtitlePreviewView_().apply {
                                id(13)
                                margin(ViewMargin(0, 16, 0, 0))
                                title(DeferredText.Resource(R.string.execution_date))
                                subtitle {
                                    val date = omniPaymentModel.schedule.startDate
                                    var dateString = date?.let {
                                        DateFormatter(it).formatAs(DateType.DATE_MEDIUM)
                                    }

                                    if (date?.isToday() == true)
                                        dateString =
                                            "${
                                                DeferredText.Resource(R.string.custom_today)
                                                    .resolve(context)
                                            }, " +
                                                    "$dateString"

                                    dateString
                                }
                                subtitleTextAppearance(R.style.IntracompanyBody1Medium)
                                hideBottomSeparator { false }
                            }

                            +TransferTitleSubtitlePreviewView_().apply {
                                id(14)
                                title(DeferredText.Resource(R.string.frequency))
                                margin(ViewMargin(0, 16, 0, 0))
                                subtitle {
                                    when (omniPaymentModel.schedule.transferFrequency) {
                                        Schedule.TransferFrequency.ONCE -> DeferredText.Resource(R.string.once)
                                        Schedule.TransferFrequency.DAILY -> DeferredText.Resource(R.string.daily)
                                        Schedule.TransferFrequency.WEEKLY -> DeferredText.Resource(R.string.weekly)
                                        Schedule.TransferFrequency.BIWEEKLY -> DeferredText.Resource(R.string.biweekly)
                                        Schedule.TransferFrequency.MONTHLY -> DeferredText.Resource(R.string.monthly)
                                        Schedule.TransferFrequency.QUARTERLY -> DeferredText.Resource(R.string.quarterly)
                                        Schedule.TransferFrequency.YEARLY -> DeferredText.Resource(R.string.yearly)
                                        else -> DeferredText.Constant("")
                                    }.resolve(context)
                                }
                                subtitleTextAppearance(R.style.IntracompanyBody1Medium)
                                hideBottomSeparator { false }
                            }

                            if (omniPaymentModel.description.isNotEmpty()) {
                                +TransferTitleSubtitlePreviewView_().apply {
                                    id(15)
                                    margin(ViewMargin(0, 16, 0, 0))
                                    title(DeferredText.Resource(R.string.payment_desc_sc))
                                    subtitle { omniPaymentModel.description }
                                    subtitleTextAppearance(R.style.IntracompanyBody1Medium)
                                    hideBottomSeparator { true }
                                }
                            }
                        }
                    }
                    onComplete =
                        { context, navController, createPayment, omniPaymentModel, selectedAccount ->
                            val originator =
                                AccountIdentification {
                                    identification =
                                        Identification {
                                            identification = omniPaymentModel.fromAccount.accountId
                                            schemeName = SchemeNames.ID
                                            additions = omniPaymentModel.fromAccount.additions
                                        }
                                }

                            val amountInstructed =
                                Currency {
                                    amount = omniPaymentModel.amount.toString()
                                    currencyCode = omniPaymentModel.toAccount.currencyCode
                                }

                            val destination =
                                InitiateTransaction {
                                    instructedAmount = amountInstructed
                                    remittanceInformation = omniPaymentModel.description
                                    counterparty =
                                        InvolvedParty {
                                            name = omniPaymentModel.toAccount.contactName
                                            postalAddress =
                                                omniPaymentModel.toAccount.accountPostalAddress
                                        }

                                    val scheme = if (omniPaymentModel.toAccount.iban.isBlank()) {
                                        SchemeNames.BBAN
                                    } else {
                                        SchemeNames.IBAN
                                    }

                                    counterpartyAccount = InitiateCounterPartyAccount {
                                        name = omniPaymentModel.toAccount.accountName
                                        identification =
                                            Identification {
                                                identification =
                                                    omniPaymentModel.toAccount.getAvailableAccountNumber()
                                                schemeName = scheme
                                                additions = omniPaymentModel.toAccount.additions
                                            }
                                    }

                                    counterpartyBank = when {
                                        omniPaymentModel.toAccount.bankSwift.isEmpty() -> Bank {
                                            bankBranchCode =
                                                omniPaymentModel.toAccount.bankCode.takeUnless { it.isBlank() }
                                            name =
                                                omniPaymentModel.toAccount.bankName.takeUnless { it.isBlank() }
                                            bankAddress =
                                                omniPaymentModel.toAccount.bankPostalAddress
                                        }
                                        else -> Bank {
                                            bic = omniPaymentModel.toAccount.bankSwift
                                            name =
                                                omniPaymentModel.toAccount.bankName.takeUnless { it.isBlank() }
                                            bankBranchCode =
                                                omniPaymentModel.toAccount.bankCode.takeUnless { it.isBlank() }
                                            bankAddress =
                                                omniPaymentModel.toAccount.bankPostalAddress
                                        }
                                    }
                                    correspondentBank = omniPaymentModel.correspondentBank
                                    intermediaryBank = omniPaymentModel.intermediaryBank
                                    chargeBearer = when (omniPaymentModel.chargeBearer) {
                                        "OUR" -> ChargeBearer.OUR
                                        "BEN" -> ChargeBearer.BEN
                                        "SHA" -> ChargeBearer.SHA
                                        else -> null
                                    }
                                    transferFee =
                                        Currency {
                                            amount = omniPaymentModel.transferFee.toString()
                                            currencyCode = omniPaymentModel.transferFeeCurrencyCode
                                        }
                                }

                            val modeOfPayment = when (omniPaymentModel.schedule.transferFrequency) {
                                Schedule.TransferFrequency.ONCE -> PaymentMode.SINGLE
                                else -> PaymentMode.RECURRING
                            }

                            val paymentSchedule =
                                when (omniPaymentModel.schedule.transferFrequency) {
                                    Schedule.TransferFrequency.ONCE -> null
                                    else -> omniPaymentModel.schedule
                                }

                            val payload = PaymentOrderPost {
                                paymentType = "INTERNATIONAL_TRANSFER"
                                paymentMode = modeOfPayment
                                requestedExecutionDate = omniPaymentModel.schedule.startDate
                                originatorAccount = originator
                                transferTransactionInformation = destination
                                schedule = paymentSchedule
                                instructionPriority = omniPaymentModel.priority?.instructionPriority
                            }

                            createPayment(payload) {
                                navController.navigateSafe(R.id.action_review_to_result)
                            }

                        }
                }
            }
        }
    }
}