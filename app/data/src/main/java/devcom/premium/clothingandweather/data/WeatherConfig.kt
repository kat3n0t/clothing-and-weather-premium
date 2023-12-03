package devcom.premium.clothingandweather.data

import devcom.premium.clothingandweather.domain.Degree
import devcom.premium.clothingandweather.domain.WeatherType

/**
 * Класс данных погодной конфигурации
 */
data class WeatherConfig(val degree: Degree, val type: WeatherType)