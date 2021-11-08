package com.backbase.business.kickstarter.productised_app

import android.os.Bundle
import android.view.WindowManager
import com.backbase.android.business.app.universal.BBActivity

class BusinessBankingActivity : BBActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (BuildConfig.BUILD_TYPE == "release") {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
        }
    }
}