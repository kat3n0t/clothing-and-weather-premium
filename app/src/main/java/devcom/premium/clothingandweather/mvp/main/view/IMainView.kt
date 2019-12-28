package devcom.premium.clothingandweather.mvp.main.view

import android.net.Uri
import androidx.annotation.DrawableRes
import devcom.premium.clothingandweather.common.Weather

interface IMainView {
    fun showDefaultModel()
    fun switchInfoVisible(isCanVisible: Boolean)
    fun setTextInfo(weather: Weather)
    fun launchActivity(targetClass: Class<*>)
    fun loadIcon(iconUri: Uri)
    fun loadModel(@DrawableRes id: Int)
}
