package devcom.premium.clothingandweather.common

import kotlin.math.pow
import kotlin.math.round

data class Weather(val temperatureCelsius: Double, val windSpeed: Double, val humidity: Double) {
    /**
     * перевод температуры из градусов Цельсия в Фаренгейт
     */
    val temperatureFahrenheit = temperatureCelsius * 1.8 + 32
    /**
     * перевод из м/ч в км/ч
     */
    private val windSpeedKmHour = if (windSpeed != 0.0) windSpeed * 3.6 else windSpeed

    /**
     * @return температура по ощущениям в градусах Цельсия
     */
    fun getTemperatureCelsiusPerception(): Double {
        var correctTemperature = temperatureCelsius
        if ((temperatureCelsius <= 10) && (windSpeedKmHour > 4.8)) {
            // ветро-холодовый индекс, согласно формуле
            var windColdIndex =
                13.2 + 0.6215 * temperatureCelsius - 11.37 * windSpeedKmHour.pow(0.16) +
                        0.3965 * temperatureCelsius * windSpeedKmHour.pow(0.16)
            if (humidity > 60)
                windColdIndex -= 2
            correctTemperature = round(10 * windColdIndex) / 10
        }
        correctTemperature += getCelsiusHeatLoad(temperatureCelsius, humidity)
        return correctTemperature
    }

    /**
     * Корректировка температуры по тепловой нагрузке
     * @param temperature температура в градусах Цельсия
     * @param humidity влажность воздуха
     * @return сдвиг на градусы Цельсия в зависимости от тепловой нагрузки
     */
    private fun getCelsiusHeatLoad(temperature: Double, humidity: Double): Double {
        var heatLoad = 0.0
        if (temperature >= 20) {
            if (humidity < 30)
                heatLoad -= 2
            else if (humidity > 50)
                heatLoad += 2

            if ((humidity > 70) && (temperature > 30))
                heatLoad += 2
        }
        return heatLoad
    }
}
