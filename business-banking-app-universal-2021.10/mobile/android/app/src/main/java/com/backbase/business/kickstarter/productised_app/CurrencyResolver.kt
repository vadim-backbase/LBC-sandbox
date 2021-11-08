package com.backbase.business.kickstarter.productised_app

import com.backbase.deferredresources.DeferredDrawable
import com.backbase.deferredresources.DeferredText
import com.mynameismidori.currencypicker.ExtendedCurrency
import java.util.*

/**
 * Created by Backbase R&D B.V on 21/6/21
 * Default country image and name resolver
 */
internal object CurrencyResolver {
    fun buildCurrencyNameResolver() = { iso: String ->
        DeferredText.Constant(Currency.getInstance(iso).displayName)
    }

    fun buildCurrencyImageResolver(): (String) -> DeferredDrawable? = { iso: String ->
        val currency = ExtendedCurrency.getCurrencyByISO(iso) ?: null
        currency?.let { DeferredDrawable.Resource(it.flag) }
            ?: DeferredDrawable.Constant(null)
    }
}