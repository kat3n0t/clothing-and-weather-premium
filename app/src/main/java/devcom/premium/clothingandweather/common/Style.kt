package devcom.premium.clothingandweather.common

annotation class Style {
    companion object {
        const val CASUAL = 0
        const val OFFICIAL = 1
        const val SPORT = 2

        val all = setOf(CASUAL, OFFICIAL, SPORT)
    }
}