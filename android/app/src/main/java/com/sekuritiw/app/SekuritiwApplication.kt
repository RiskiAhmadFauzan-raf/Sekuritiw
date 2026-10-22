package com.sekuritiw.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SekuritiwApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialization logic here (e.g. database setup, logging)
    }
}
