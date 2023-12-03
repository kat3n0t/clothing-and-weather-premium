package devcom.premium.clothingandweather.data

import devcom.premium.clothingandweather.domain.Gender
import devcom.premium.clothingandweather.domain.Style

/**
 * Класс данных конфигурации модели персонажа
 */
data class ClothingConfig(val gender: Gender, val style: Style)