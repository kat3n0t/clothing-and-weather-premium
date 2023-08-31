package devcom.premium.clothingandweather.common

import devcom.premium.clothingandweather.domain.Degree
import devcom.premium.clothingandweather.domain.Gender
import devcom.premium.clothingandweather.domain.Style
import devcom.premium.clothingandweather.domain.WeatherType
import kotlin.Int

/**
 * Объект для преобразования Int в перечисления
 */
@Deprecated("use values().firstOrNull { it.value == value }")
object IntExtensions {
    fun toDegree(i: Int) = Degree.values().associateBy(Degree::ordinal)[i]
    fun toGender(i: Int) = Gender.values().associateBy(Gender::ordinal)[i]
    fun toStyle(i: Int) = Style.values().associateBy(Style::ordinal)[i]
    fun toWeatherType(i: Int) = WeatherType.values().associateBy(WeatherType::ordinal)[i]
}