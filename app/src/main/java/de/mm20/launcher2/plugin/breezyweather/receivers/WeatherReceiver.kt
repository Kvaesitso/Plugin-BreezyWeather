package de.mm20.launcher2.plugin.breezyweather.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.edit

class WeatherReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val weatherJson = intent.getStringExtra("WeatherJson")
        if(weatherJson == null) {
            Log.e("BreezyWeatherPlugin", "Broadcast was received but WeatherJson was null")
            return
        }
        context.openFileOutput("data.json", Context.MODE_PRIVATE).writer().use {
            it.write(weatherJson)
        }
        context.getSharedPreferences("prefs", Context.MODE_PRIVATE).edit {
            putLong("lastUpdate", System.currentTimeMillis())
        }
    }
}