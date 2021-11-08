package com.backbase.business.kickstarter.productised_app

import com.backbase.android.business.app.common.PushNotificationConfiguration
import com.backbase.android.business.app.universal.configuration.BusinessUniversalAppConfiguration
import com.backbase.android.business.journey.omnipayments.configuration.BusinessPaymentsConfiguration
import com.backbase.business.kickstarter.productised_app.payment.International
import com.backbase.business.kickstarter.productised_app.payment.Sepa
import com.backbase.mobilenotifications.firebase.FirebasePushNotificationService

/**
 * Created by Backbase R&D B.V on 30/06/2020.
 * Configuration file for the collection
 */
object Configuration {

    fun apply() = BusinessUniversalAppConfiguration {
        pushNotificationConfiguration = PushNotificationConfiguration {
            pushNotificationServiceDefinition = { context ->
                {
                    FirebasePushNotificationService(context)
                }
            }
            pushNotificationsPlatform = PushNotificationConfiguration.firebasePlatform
        }
        omniPaymentsJourneyConfiguration = BusinessPaymentsConfiguration {
            router = { com.backbase.android.business.app.universal.routers.OmniPaymentRouter() }
            +Sepa.payment
            +International.payment
        }
    }
}