package de.mm20.launcher2.plugin.breezyweather

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log

class BreezyWeatherTrampolineActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            packageManager.getLaunchIntentForPackage("org.breezyweather")?.let {
                startActivity(it)
            }
            finish()
        } catch (e: ActivityNotFoundException) {
            Log.e("KvaesitsoBreezyWeatherPlugin", "BreezyWeather could not be started", e)
            finish()
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e("KvaesitsoBreezyWeatherPlugin", "BreezyWeather could not be started", e)
            finish()
        }
    }
}