package com.subsolis.allblue

import android.app.Application
import com.adapty.Adapty
import com.google.android.material.color.DynamicColors
import com.qonversion.android.sdk.Qonversion
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AllBlueApplication: Application() {
    override fun onCreate() {
        super.onCreate()

            // Do something for Android 12 and above versions
            DynamicColors.applyToActivitiesIfAvailable(this)
//            Qonversion.setDebugMode()
            Qonversion.launch(this, getString(R.string.qonversion_product_key), false)
            Adapty.activate(applicationContext,"public_live_4Lynd5sb.CfxPkeyLHhmw4pDq7251")
    }
}