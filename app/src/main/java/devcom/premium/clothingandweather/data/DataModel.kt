package devcom.premium.clothingandweather.data

import android.content.Context
import devcom.premium.clothingandweather.R
import devcom.premium.clothingandweather.domain.Degree
import devcom.premium.clothingandweather.domain.Weather

object DataModel {

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
}