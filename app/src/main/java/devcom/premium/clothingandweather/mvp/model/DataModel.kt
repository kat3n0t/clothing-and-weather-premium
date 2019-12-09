package devcom.premium.clothingandweather.mvp.model

import android.content.Context
import devcom.premium.clothingandweather.R
import devcom.premium.clothingandweather.common.Weather
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

object DataModel {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    fun title(weatherDegree: Int, weather: Weather) =
        when (weatherDegree) {
            1 -> // celsius
                "${weather.temperatureCelsius} °C"
            2 -> // fahrenheit
                String.format(
                    "%.1f",
                    weather.temperatureFahrenheit
                ).replace(',', '.') + " °F"
            else -> // celsius and fahrenheit
                "${weather.temperatureCelsius} °C / " + String.format(
                    "%.1f",
                    weather.temperatureFahrenheit
                ).replace(
                    ',', '.'
                ) + " °F"
        }

    fun infoWindSpeed(context: Context, wind: Double) =
        context.getString(R.string.wind) + " = $wind " + context.getString(R.string.meter_sec)

    fun infoHumidity(context: Context, humidity: Double) =
        context.getString(R.string.humidity) + " = $humidity%"

    private fun isWeatherDay(dateArray: String, date: String): Boolean {
        return dateArray.contains("$date 1") or dateArray.contains("$date 2")
    }

    fun weatherDay(json: JSONObject, weatherDate: Int): JSONObject? {
        var result: JSONObject? = null
        when (weatherDate) {
            1 -> {
                val list = json.getJSONArray("list")
                val currentDate = dateFormat.format(Calendar.getInstance().time)
                for (i in 1 until json.getInt("cnt") - 1) {
                    if (isWeatherDay(list.getJSONObject(i).getString("dt_txt"), currentDate)) {
                        result = list.getJSONObject(i)
                        break
                    }
                }
            }
            2 -> {
                val list = json.getJSONArray("list")
                val calendarTomorrow = Calendar.getInstance()
                calendarTomorrow.add(Calendar.DATE, 1)
                val tomorrowDate = dateFormat.format(calendarTomorrow.time)
                for (i in 1 until json.getInt("cnt")) {
                    if (isWeatherDay(list.getJSONObject(i).getString("dt_txt"), tomorrowDate)) {
                        result = list.getJSONObject(i)
                        break
                    }
                }
            }
            else -> {
                result = json
            }
        }
        return result
    }
}