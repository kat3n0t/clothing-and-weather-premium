package devcom.premium.clothingandweather.mvp.main.presenter

import android.content.Context
import android.os.Handler
import android.os.Looper
import devcom.premium.clothingandweather.R
import devcom.premium.clothingandweather.common.Clothes
import devcom.premium.clothingandweather.common.DataNotFoundException
import devcom.premium.clothingandweather.data.ClothingConfig
import devcom.premium.clothingandweather.data.DataModel
import devcom.premium.clothingandweather.data.ModelRepository
import devcom.premium.clothingandweather.data.WeatherConfig
import devcom.premium.clothingandweather.mvp.main.view.IMainView
import moxy.InjectViewState
import moxy.MvpPresenter
import kotlin.concurrent.thread

@InjectViewState
class MainPresenter(private val repository: ModelRepository) : MvpPresenter<IMainView>() {

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        viewState.showDefaultModel()
    }

    override fun detachView(view: IMainView?) {
        handler.removeCallbacksAndMessages(null)
        super.detachView(view)
    }

    /**
     * Обрабатывает при обновлении соединения
     *
     * @param context [Context]
     * @param clothingConfig данные о модели персонажа
     * @param
     */
    fun updateAPIConnection(
        context: Context,
        clothingConfig: ClothingConfig,
        weatherConfig: WeatherConfig,
        city: String,
    ) {
        viewState.switchInfoVisibility(false)
        viewState.switchLoadingVisibility(true)
        viewState.title(context.getString(R.string.loading))

        handler.removeCallbacksAndMessages(null)
        val clothes = Clothes(clothingConfig)
        thread {
            try {
                val weatherData = repository.weatherData(weatherConfig.type, city)
                    ?: throw DataNotFoundException(context)

                val weather = weatherData.weather
                val iconUri = weatherData.iconUri

                val perceivedTemp = weather.temperatureCelsiusPerception()

                handler.post {
                    with(viewState) {
                        title(DataModel.title(weatherConfig.degree, weather))
                        setTextInfo(weather)
                        loadModel(clothes.clothesId(perceivedTemp))
                        loadIcon(iconUri)

                        switchLoadingVisibility(false)
                        switchInfoVisibility(true)
                    }
                }
            } catch (e: Exception) {
                handler.post {
                    e.message?.let {
                        if (e is DataNotFoundException) {
                            viewState.title(it)
                        }
                    }
                    with(viewState) {
                        loadModel(clothes.clothesId(Clothes.INVALID_TEMPERATURE))
                        switchLoadingVisibility(false)
                        switchInfoVisibility(false)
                    }
                }
            }
        }
    }

    companion object {
        private val handler = Handler(Looper.getMainLooper())
    }
}