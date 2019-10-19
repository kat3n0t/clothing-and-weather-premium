package devcom.premium.clothingandweather.mvp.main.view

import devcom.premium.clothingandweather.common.Weather

interface IMainView {
    fun showDefaultModel()
    fun switchInfoVisible(isCanVisible: Boolean)
    fun setTextInfo(weather: Weather)
    fun launchActivity(targetClass: Class<*>)
}
