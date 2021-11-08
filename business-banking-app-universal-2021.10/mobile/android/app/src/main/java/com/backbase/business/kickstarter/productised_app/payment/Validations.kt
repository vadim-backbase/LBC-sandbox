package com.backbase.business.kickstarter.productised_app.payment

import com.backbase.android.business.journey.omnipayments.validation.ValidationResult
import com.backbase.business.kickstarter.productised_app.R
import com.backbase.deferredresources.DeferredText
import java.math.BigDecimal
import java.util.regex.Pattern

/**
 * Created by Backbase R&D B.V on 17/02/2021.
 * Validation functions
 */
internal fun buildValidationResult(validationResult: Boolean, errorMessage: DeferredText) =
    if (validationResult) ValidationResult.Valid
    else ValidationResult.Invalid(errorMessage)

internal fun isNotZero(value: BigDecimal): ValidationResult = buildValidationResult(
    value != BigDecimal.ZERO,
    DeferredText.Resource(R.string.amount_blank)
)

internal fun deliveryOptionsNotEmpty(value: String): ValidationResult = buildValidationResult(
    value.isNotEmpty(),
    DeferredText.Resource(R.string.international_delivery_options_error)
)

internal fun isNotEmpty(value: String): ValidationResult = buildValidationResult(
    value.isNotBlank(),
    DeferredText.Resource(R.string.error_beneficiary_empty)
)

internal fun isValidIban(iban: String): ValidationResult = buildValidationResult(
    validateIban(iban),
    DeferredText.Resource(R.string.sepa_error_invalid_iban)
)

internal fun validateIban(iban: String): Boolean {
    val pattern =
        Pattern.compile("^(?:(?:IT|SM)\\d{2}[A-Z]\\d{22}|CY\\d{2}[A-Z]\\d{23}|NL\\d{2}[A-Z]{4}\\d{10}|LV\\d{2}[A-Z]{4}\\d{13}|(?:BG|BH|GB|IE)\\d{2}[A-Z]{4}\\d{14}|GI\\d{2}[A-Z]{4}\\d{15}|RO\\d{2}[A-Z]{4}\\d{16}|KW\\d{2}[A-Z]{4}\\d{22}|MT\\d{2}[A-Z]{4}\\d{23}|NO\\d{13}|(?:DK|FI|GL|FO)\\d{16}|MK\\d{17}|(?:AT|EE|KZ|LU|XK)\\d{18}|(?:BA|HR|LI|CH|CR)\\d{19}|(?:GE|DE|LT|ME|RS)\\d{20}|IL\\d{21}|(?:AD|CZ|ES|MD|SA)\\d{22}|PT\\d{23}|(?:BE|IS)\\d{24}|(?:FR|MR|MC)\\d{25}|(?:AL|DO|LB|PL)\\d{26}|(?:AZ|HU)\\d{27}|(?:GR|MU)\\d{28})\$")

    // Remove spaces
    val matcher = pattern.matcher(iban.replace(" ", ""))
    if (matcher.matches()) {
        return true
    }
    return false
}

internal fun chargeBearerNotEmpty(value: String): ValidationResult = buildValidationResult(
    !value.isNullOrEmpty(),
    DeferredText.Resource(R.string.international_charge_bearer_validation_error)
)

internal fun countryNotEmpty(value: String): ValidationResult = buildValidationResult(
    value.isNotEmpty(),
    DeferredText.Resource(R.string.country_required)
)