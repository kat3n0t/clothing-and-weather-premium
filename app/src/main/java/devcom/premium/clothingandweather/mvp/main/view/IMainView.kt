package devcom.premium.clothingandweather.mvp.main.view

import android.net.Uri
import androidx.annotation.DrawableRes
import devcom.premium.clothingandweather.common.Weather
import moxy.MvpView
import moxy.viewstate.strategy.SkipStrategy
import moxy.viewstate.strategy.StateStrategyType

interface IMainView : MvpView {

    /**
     * Отображает стандартную модель персонажа
     */
    @StateStrategyType(SkipStrategy::class)
    fun showDefaultModel()

    /**
     * Переключает видимость информации
     *
     * @param isCanVisible истина, если нужно отобразить инфу
     */
    @StateStrategyType(SkipStrategy::class)
    fun switchInfoVisible(isCanVisible: Boolean)

    /**
     * Конвертирует погодные данные в текст и наполняет представление
     *
     * @param weather данные
     */
    @StateStrategyType(SkipStrategy::class)
    fun setTextInfo(weather: Weather)

    /**
     * Запускает целевую активность
     *
     * @param targetClass класс целевой активности
     */
    @StateStrategyType(SkipStrategy::class)
    fun launchActivity(targetClass: Class<*>)

    /**
     * Загружает иконку с погодой
     *
     * @param iconUri ссылка на иконку
     */
    @StateStrategyType(SkipStrategy::class)
    fun loadIcon(iconUri: Uri)

    /**
     * Загружает модель по идентификатору
     *
     * @param id [DrawableRes]
     */
    @StateStrategyType(SkipStrategy::class)
    fun loadModel(@DrawableRes id: Int)

    /**
     * Обновляет погодные данные
     */
    @StateStrategyType(SkipStrategy::class)
    fun updateWeatherData()
}
