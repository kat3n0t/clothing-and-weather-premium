package devcom.premium.clothingandweather.common

import devcom.premium.clothingandweather.R
import devcom.premium.clothingandweather.data.ClothingConfig
import devcom.premium.clothingandweather.domain.Gender
import devcom.premium.clothingandweather.domain.Style

class Clothes(private val clothing: ClothingConfig) {

    fun clothesId(temperature: Double): Int {
        if (clothing.gender == Gender.MAN) {
            when (clothing.style) {
                Style.CASUAL -> return when (temperature) {
                    in range100n25n -> R.drawable.man_casual_35n25n
                    in range25n5n -> R.drawable.man_casual_25n5n
                    in range5n5p -> R.drawable.man_casual_5n5p
                    in range5p15p -> R.drawable.man_casual_5p15p
                    in range15p25p -> R.drawable.man_casual_15p25p
                    in range25p45p -> R.drawable.man_casual_25p35p
                    else -> R.drawable.man_default
                }
                Style.OFFICIAL -> return when (temperature) {
                    in range100n25n -> R.drawable.man_casual_35n25n
                    in range25n5n -> R.drawable.man_official_25n5n
                    in range5n5p -> R.drawable.man_official_5n5p
                    in range5p15p -> R.drawable.man_official_5p15p
                    in range15p25p -> R.drawable.man_official_15p25p
                    in range25p45p -> R.drawable.man_official_25p35p
                    else -> R.drawable.man_default
                }
                Style.SPORT -> return when (temperature) {
                    in range100n25n -> R.drawable.man_casual_35n25n
                    in range25n5n -> R.drawable.man_sport_25n5n
                    in range5n5p -> R.drawable.man_sport_5n5p
                    in range5p15p -> R.drawable.man_sport_5p15p
                    in range15p25p -> R.drawable.man_sport_15p25p
                    in range25p45p -> R.drawable.man_casual_25p35p
                    else -> R.drawable.man_default
                }
            }
        } else if (clothing.gender == Gender.WOMAN) {
            when (clothing.style) {
                Style.CASUAL -> return when (temperature) {
                    in range100n5n -> R.drawable.woman_casual_25n5n
                    in range5n5p -> R.drawable.woman_official_5n5p
                    in range5p15p -> R.drawable.woman_casual_5p15p
                    in range15p25p -> R.drawable.woman_casual_15p25p
                    in range25p45p -> R.drawable.woman_casual_25p35p
                    else -> R.drawable.woman_default
                }
                Style.OFFICIAL -> return when (temperature) {
                    in range100n5n -> R.drawable.woman_official_25n5n
                    in range5n5p -> R.drawable.woman_official_5n5p
                    in range5p15p -> R.drawable.woman_official_5p15p
                    in range15p25p -> R.drawable.woman_official_15p25p
                    in range25p45p -> R.drawable.woman_official_25p35p
                    else -> R.drawable.woman_default
                }
                Style.SPORT -> return when (temperature) {
                    in range100n5n -> R.drawable.woman_sport_25n5n
                    in range5n5p -> R.drawable.woman_official_5n5p
                    in range5p15p -> R.drawable.woman_sport_5p15p
                    in range15p25p -> R.drawable.woman_sport_15p25p
                    in range25p45p -> R.drawable.woman_casual_25p35p
                    else -> R.drawable.woman_default
                }
            }
        }
        return R.drawable.man_default
    }

    companion object {
        private val range100n25n = -100.0..-25.0
        private val range100n5n = -100.0..-5.0
        private val range25n5n = -25.0..-5.0
        private val range5n5p = -5.0..5.0
        private val range5p15p = 5.0..15.0
        private val range15p25p = 15.0..25.0
        private val range25p45p = 25.0..45.0
    }
}