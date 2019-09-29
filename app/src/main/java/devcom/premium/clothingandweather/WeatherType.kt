package devcom.premium.clothingandweather

annotation class WeatherType {
    companion object {
        private const val WEATHER = 0
        private const val FORECAST_TODAY = 1
        private const val FORECAST_TOMORROW = 2

        val all = setOf(WEATHER, FORECAST_TODAY, FORECAST_TOMORROW)
        val allForecast = setOf(FORECAST_TODAY, FORECAST_TOMORROW)
    }
}