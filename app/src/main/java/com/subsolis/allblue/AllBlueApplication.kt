package com.subsolis.allblue

import android.app.Application
import com.google.android.material.color.DynamicColors
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AllBlueApplication: Application() {
    override fun onCreate() {
        super.onCreate()

            // Do something for Android 12 and above versions
            DynamicColors.applyToActivitiesIfAvailable(this)
    }
}