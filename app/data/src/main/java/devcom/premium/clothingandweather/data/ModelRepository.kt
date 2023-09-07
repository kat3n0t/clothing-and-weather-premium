package devcom.premium.clothingandweather.data

import android.net.Uri
import devcom.premium.clothingandweather.data.rest.WeatherApi
import devcom.premium.clothingandweather.domain.Weather
import devcom.premium.clothingandweather.domain.WeatherType
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class ModelRepository(private val weatherApi: WeatherApi) {

    fun weatherData(type: WeatherType, city: String): WeatherData? {
        val dataResponseJson = weatherApi.data(type, city) ?: return null
        val dayJSON = weatherDayJson(type, dataResponseJson) ?: return null
        return WeatherData(dayJSON) { weatherApi.iconUri(it) }
    }

    private fun weatherDayJson(type: WeatherType, json: JSONObject): JSONObject? {
        when (type) {
            WeatherType.WEATHER -> return json
            WeatherType.FORECAST_TODAY -> {
                val list = json.getJSONArray("list")
                val currentDate = dateFormat.format(Calendar.getInstance().time)
                for (i in 1 until json.getInt("cnt") - 1) {
                    if (isWeatherDay(list.getJSONObject(i).getString("dt_txt"), currentDate)) {
                        return list.getJSONObject(i)
                    }
                }
            }
            WeatherType.FORECAST_TOMORROW -> {
                val list = json.getJSONArray("list")
                val calendarTomorrow = Calendar.getInstance()
                calendarTomorrow.add(Calendar.DATE, 1)
                val tomorrowDate = dateFormat.format(calendarTomorrow.time)
                for (i in 1 until json.getInt("cnt")) {
                    if (isWeatherDay(list.getJSONObject(i).getString("dt_txt"), tomorrowDate)) {
                        return list.getJSONObject(i)
                    }
                }
            }
        }
        return null
    }

    private fun isWeatherDay(dateArray: String, date: String) =
        dateArray.contains("$date 1") or dateArray.contains("$date 2")

    class WeatherData(dayJson: JSONObject, iconUriRequest: (iconName: String) -> Uri) {
        val weather: Weather by lazy {
            val mainDataObject = dayJson.getJSONObject(DATA_MAIN)
            val windDataObject = dayJson.getJSONObject(DATA_WIND)
            Weather(
                mainDataObject.getDouble(DATA_TEMP),
                windDataObject.getDouble(DATA_SPEED),
                mainDataObject.getDouble(DATA_HUMIDITY)
            )
        }

        val iconUri: Uri by lazy {
            val weatherDataArray = dayJson.getJSONArray(DATA_WEATHER)
            iconUriRequest(weatherDataArray.getJSONObject(0).getString(DATA_ICON))
        }

        companion object {
            private const val DATA_WEATHER = "weather"
            private const val DATA_MAIN = "main"
            private const val DATA_WIND = "wind"
            private const val DATA_TEMP = "temp"
            private const val DATA_SPEED = "speed"
            private const val DATA_HUMIDITY = "humidity"
            private const val DATA_ICON = "icon"
        }
    }

    companion object {
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    }
}