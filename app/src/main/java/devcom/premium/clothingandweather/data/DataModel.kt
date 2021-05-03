package devcom.premium.clothingandweather.data

import android.content.Context
import devcom.premium.clothingandweather.R
import devcom.premium.clothingandweather.common.Degree
import devcom.premium.clothingandweather.common.Weather
import devcom.premium.clothingandweather.common.WeatherType
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

object DataModel {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    /**
     * Возвращает заголовок приложения с данными о погоде
     *
     * @param degree [Degree]
     * @param weather данные о погоде
     * @return заголовок приложения
     */
    fun title(degree: Degree, weather: Weather) =
        when (degree) {
            Degree.CELSIUS -> "${weather.temperatureCelsius} °C"
            Degree.FAHRENHEIT ->
                String.format("%.1f", weather.temperatureFahrenheit)
                    .replace(',', '.') + " °F"
            Degree.ALL -> "${weather.temperatureCelsius} °C / " +
                    String.format("%.1f", weather.temperatureFahrenheit)
                        .replace(',', '.') + " °F"
        }

    fun infoWindSpeed(context: Context, wind: Double) =
        context.getString(R.string.wind) + " = $wind " + context.getString(R.string.meter_sec)

    fun infoHumidity(context: Context, humidity: Double) =
        context.getString(R.string.humidity) + " = $humidity%"

    fun weatherDay(json: JSONObject, type: WeatherType): JSONObject? {
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

    private fun isWeatherDay(dateArray: String, date: String): Boolean {
        return dateArray.contains("$date 1") or dateArray.contains("$date 2")
    }
}