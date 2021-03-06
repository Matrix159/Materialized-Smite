package com.matrix.materializedsmite

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber.DebugTree
import timber.log.Timber.Forest.plant


@HiltAndroidApp
class SmiteApplication : Application() {
  // Used to grab string resources from outside activities currently (secrets.xml)
  companion object {
    lateinit var instance: SmiteApplication
      private set
  }

  override fun onCreate() {
    super.onCreate()
    instance = this

    if (BuildConfig.DEBUG) {
      plant(DebugTree())
    } else {
      //plant(CrashReportingTree())
    }
  }
}