package devcom.premium.clothingandweather.mvp.main.view

import android.net.Uri
import androidx.annotation.DrawableRes
import devcom.premium.clothingandweather.common.Weather
import moxy.MvpView
import moxy.viewstate.strategy.SkipStrategy
import moxy.viewstate.strategy.StateStrategyType

interface IMainView : MvpView {

    @StateStrategyType(SkipStrategy::class)
    fun showDefaultModel()

    @StateStrategyType(SkipStrategy::class)
    fun switchInfoVisible(isCanVisible: Boolean)

    @StateStrategyType(SkipStrategy::class)
    fun setTextInfo(weather: Weather)

    @StateStrategyType(SkipStrategy::class)
    fun launchActivity(targetClass: Class<*>)

    @StateStrategyType(SkipStrategy::class)
    fun loadIcon(iconUri: Uri)

    @StateStrategyType(SkipStrategy::class)
    fun loadModel(@DrawableRes id: Int)

    @StateStrategyType(SkipStrategy::class)
    fun updateAPIConnection()
}
