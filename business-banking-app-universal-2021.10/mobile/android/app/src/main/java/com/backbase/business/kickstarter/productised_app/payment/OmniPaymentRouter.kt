package com.backbase.business.kickstarter.productised_app.payment

import androidx.navigation.NavController
import com.backbase.android.business.journey.omnipayments.navigation.OmniPaymentRouting

/**
 * Created by Backbase R&D B.V on 13/08/2020.
 * Default router for omni payment
 */
internal class OmniPaymentRouter: OmniPaymentRouting {

    private var navController: NavController? = null

    override fun setNavController(navController: NavController?) { this.navController = navController }

    override fun exitNavController(): NavController? = navController

}