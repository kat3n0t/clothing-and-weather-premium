package devcom.premium.clothingandweather.mvp.main.view

import android.net.Uri
import androidx.annotation.DrawableRes
import devcom.premium.clothingandweather.common.Weather
import moxy.MvpView
import moxy.viewstate.strategy.SkipStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(SkipStrategy::class)
interface IMainView : MvpView {

    /**
     * Отображает стандартную модель персонажа
     */
    fun showDefaultModel()

    /**
     * Переключает видимость информации
     *
     * @param canVisible истина, если нужно отобразить инфу
     */
    fun switchInfoVisibility(canVisible: Boolean)

    /**
     * Переключает видимость прогрессбара
     *
     * @param canVisible истина, если нужно отобразить прогрессбар
     */
    fun switchLoadingVisibility(canVisible: Boolean)

    /**
     * Устанавливает заголовок
     *
     * @param title текст заголовка
     */
    fun title(title: String)

    /**
     * Конвертирует погодные данные в текст и наполняет представление
     *
     * @param weather данные
     */
    fun setTextInfo(weather: Weather)

    /**
     * Запускает целевую активность
     *
     * @param targetClass класс целевой активности
     */
    fun launchActivity(targetClass: Class<*>)

    /**
     * Загружает иконку с погодой
     *
     * @param iconUri ссылка на иконку
     */
    fun loadIcon(iconUri: Uri)

    /**
     * Загружает модель по идентификатору
     *
     * @param id [DrawableRes]
     */
    fun loadModel(@DrawableRes id: Int)

    /**
     * Обновляет погодные данные
     */
    fun updateWeatherData()
}
