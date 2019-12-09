package devcom.premium.clothingandweather.mvp.main.view

import androidx.annotation.DrawableRes
import devcom.premium.clothingandweather.common.Weather
import java.net.URL

interface IMainView {
    fun showDefaultModel()
    fun switchInfoVisible(isCanVisible: Boolean)
    fun setTextInfo(weather: Weather)
    fun launchActivity(targetClass: Class<*>)
    fun loadIcon(urlIcon: URL)
    fun loadModel(@DrawableRes id: Int)
}
