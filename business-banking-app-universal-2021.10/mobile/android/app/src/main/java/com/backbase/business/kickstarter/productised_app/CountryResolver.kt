package com.backbase.business.kickstarter.productised_app

import android.content.Context
import com.backbase.deferredresources.DeferredDrawable
import com.backbase.deferredresources.DeferredText
import com.idmikael.flags_iso.FlagsIso
import java.util.*

/**
 * Created by Backbase R&D B.V on 3/2/21
 * Default country image and name resolver
 */
internal object CountryResolver {
    fun buildCountryNameResolver() = { countryIso: String ->
        DeferredText.Constant(
            Locale(Locale.ROOT.language, countryIso).displayCountry
        )
    }

    fun buildCountryImageResolver(context: Context) = { countryIso: String ->
        val drawable = FlagsIso.getFlagDrawable(context, countryIso)
        DeferredDrawable.Constant(drawable)
    }
}