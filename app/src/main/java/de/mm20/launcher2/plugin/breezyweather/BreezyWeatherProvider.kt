package de.mm20.launcher2.plugin.breezyweather

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import de.mm20.launcher2.plugin.breezyweather.data.WeatherData
import de.mm20.launcher2.plugin.config.WeatherPluginConfig
import de.mm20.launcher2.sdk.PluginState
import de.mm20.launcher2.sdk.weather.Forecast
import de.mm20.launcher2.sdk.weather.K
import de.mm20.launcher2.sdk.weather.WeatherIcon
import de.mm20.launcher2.sdk.weather.WeatherLocation
import de.mm20.launcher2.sdk.weather.WeatherProvider
import de.mm20.launcher2.sdk.weather.km_h
import de.mm20.launcher2.sdk.weather.mbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.io.IOException

class BreezyWeatherProvider : WeatherProvider(
    WeatherPluginConfig(
        minUpdateInterval = 60000L,
        managedLocation = true,
    )
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
    }

    private suspend fun getLatestWeatherData(): WeatherData? = withContext(Dispatchers.IO) {
        try {
            json.decodeFromStream<WeatherData>(context!!.openFileInput("data.json"))
        } catch (e: SerializationException) {
            Log.e("BreezyWeatherPlugin", "Failed to decode weather data", e)
            null
        } catch (e: IOException) {
            Log.e("BreezyWeatherPlugin", "Failed to read weather data", e)
            null
        }
    }

    override suspend fun getWeatherData(location: WeatherLocation, lang: String?): List<Forecast>? {
        return getWeatherData()
    }

    override suspend fun getWeatherData(lat: Double, lon: Double, lang: String?): List<Forecast>? {
        return getWeatherData()
    }

    private suspend fun getWeatherData(): List<Forecast>? {
        val data = getLatestWeatherData() ?: return null

        val lastUpdate = context!!.getSharedPreferences("prefs", Context.MODE_PRIVATE)
            .getLong("lastUpdate", 0)

        val result = mutableListOf<Forecast>()

        result += Forecast(
            timestamp = data.timestamp?.times(1000L) ?: return null,
            temperature = data.currentTemp?.K ?: return null,
            icon = iconForId(data.currentConditionCode ?: return null),
            condition = data.currentCondition ?: return null,
            location = data.location ?: return null,
            provider = "Breezy Weather",
            clouds = data.cloudCover,
            humidity = data.currentHumidity,
            pressure = data.pressure?.toDouble()?.mbar,
            windSpeed = data.windSpeed?.toDouble()?.km_h,
            rainProbability = data.precipProbability,
            windDirection = data.windDirection?.toDouble(),
            createdAt = lastUpdate,
            providerUrl = "de.mm20.launcher.plugin.breezyweather://-"
        )

        for (hourly in data.hourly ?: emptyList()) {
            result += Forecast(
                timestamp = hourly.timestamp?.times(1000L) ?: continue,
                temperature = hourly.temp?.toDouble()?.K ?: continue,
                icon = iconForId(hourly.conditionCode ?: continue),
                condition = textForId(hourly.conditionCode) ?: continue,
                location = data.location ?: continue,
                provider = "Breezy Weather",
                humidity = hourly.humidity,
                windSpeed = hourly.windSpeed?.toDouble()?.km_h,
                rainProbability = hourly.precipProbability,
                windDirection = hourly.windDirection?.toDouble(),
                createdAt = lastUpdate,
                providerUrl = "de.mm20.launcher.plugin.breezyweather://-"
            )
        }

        return result
    }

    private fun iconForId(id: Int): WeatherIcon {
        return when (id) {
            200, 201, in 230..232 -> WeatherIcon.ThunderstormWithRain
            202 -> WeatherIcon.ThunderstormWithRain
            210, 211 -> WeatherIcon.Thunderstorm
            212, 221 -> WeatherIcon.HeavyThunderstorm
            in 300..302, in 310..312 -> WeatherIcon.Drizzle
            313, 314, 321, in 500..504, 511, in 520..522, 531 -> WeatherIcon.Showers
            in 600..602 -> WeatherIcon.Snow
            611, 612, 615, 616, in 620..622 -> WeatherIcon.Sleet
            701, 711, 731, 741, 761, 762 -> WeatherIcon.Fog
            721 -> WeatherIcon.Haze
            771, 781, in 900..902, in 958..962 -> WeatherIcon.Storm
            800 -> WeatherIcon.Clear
            801 -> WeatherIcon.PartlyCloudy
            802 -> WeatherIcon.MostlyCloudy
            803 -> WeatherIcon.BrokenClouds
            804, 951 -> WeatherIcon.Cloudy
            903 -> WeatherIcon.Cold
            904 -> WeatherIcon.Hot
            905, in 952..957 -> WeatherIcon.Wind
            906 -> WeatherIcon.Hail
            else -> WeatherIcon.Unknown
        }
    }

    private fun textForId(id: Int): String? {
        val context = context ?: return null
        val resId = when (id) {
            800 -> R.string.weather_condition_clearsky
            801 -> R.string.weather_condition_partlycloudy
            803 -> R.string.weather_condition_cloudy
            500 -> R.string.weather_condition_rain
            600 -> R.string.weather_condition_snow
            771 -> R.string.weather_condition_wind
            741 -> R.string.weather_condition_fog
            751 -> R.string.weather_condition_haze
            611 -> R.string.weather_condition_sleet
            511 -> R.string.weather_condition_hail
            210 -> R.string.weather_condition_thunder
            211 -> R.string.weather_condition_thunderstorm
            else -> R.string.weather_condition_unknown
        }
        return context.getString(resId)
    }

    override suspend fun getPluginState(): PluginState {
        val context = context!!
        if (!isBreezyWeatherInstalled()) {
            return PluginState.SetupRequired(
                message = context.getString(R.string.plugin_state_breezyweather_not_installed),
                setupActivity = Intent(
                    Intent.ACTION_VIEW,
                    "https://github.com/breezy-weather/breezy-weather/blob/main/INSTALL.md".toUri()
                ),
            )
        }
        if (getLatestWeatherData() == null) {
            return PluginState.SetupRequired(
                message = context.getString(R.string.plugin_state_data_sharing_not_enabled),
                setupActivity = Intent("android.intent.action.APPLICATION_PREFERENCES").apply {
                    `package` = "org.breezyweather"
                },
            )
        }
        return super.getPluginState()
    }

    private fun isBreezyWeatherInstalled(): Boolean {
        val context = context ?: return false

        return try {
            context.packageManager.getPackageInfo("org.breezyweather", 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
}