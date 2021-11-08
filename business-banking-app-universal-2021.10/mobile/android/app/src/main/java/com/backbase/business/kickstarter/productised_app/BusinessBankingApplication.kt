package com.backbase.business.kickstarter.productised_app

import com.backbase.android.business.app.universal.BusinessUniversalApp

/**
 * Created by Backbase R&D B.V on 30/06/2020.
 * Productized app for the entire business banking collection
 */
class BusinessBankingApplication : BusinessUniversalApp(configurationUniversal = Configuration.apply()) {
    override fun onCreate() {
        super.onCreate()
    }
}