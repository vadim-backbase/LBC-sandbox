package com.backbase.business.kickstarter.productised_app.payment

import android.content.Context
import android.os.Build
import java.util.*

/**
 * Created by Backbase R&D B.V on 17/02/2021.
 * Extensions for omni-payments
 */
@Suppress("DEPRECATION")
internal fun Context.getSystemLocale(): Locale = try {
    resources.configuration.let {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N)
            it.locale
        else
            it.locales[0]
    }
} catch (e: Exception) {
    Locale.getDefault()
}