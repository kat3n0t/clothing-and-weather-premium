package devcom.premium.clothingandweather.data

import devcom.premium.clothingandweather.common.Degree
import devcom.premium.clothingandweather.common.WeatherType

/**
 * Класс данных погодной конфигурации
 */
data class WeatherConfig(val degree: Degree, val type: WeatherType)
