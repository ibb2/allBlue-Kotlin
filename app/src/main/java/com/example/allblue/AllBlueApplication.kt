package com.example.allblue

import android.app.Application
import android.os.Build
import com.google.android.material.color.DynamicColors

class AllBlueApplication: Application() {
    override fun onCreate() {
        super.onCreate()

            // Do something for Android 12 and above versions
            DynamicColors.applyToActivitiesIfAvailable(this)
    }
}