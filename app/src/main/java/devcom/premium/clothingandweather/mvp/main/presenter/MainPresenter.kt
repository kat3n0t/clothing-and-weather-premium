package devcom.premium.clothingandweather.mvp.main.presenter

import android.content.Context
import android.os.Handler
import android.os.Looper
import devcom.premium.clothingandweather.R
import devcom.premium.clothingandweather.common.Clothes
import devcom.premium.clothingandweather.common.DataNotFoundException
import devcom.premium.clothingandweather.common.Weather
import devcom.premium.clothingandweather.data.ClothingConfig
import devcom.premium.clothingandweather.data.DataModel
import devcom.premium.clothingandweather.data.WeatherApi
import devcom.premium.clothingandweather.data.WeatherConfig
import devcom.premium.clothingandweather.mvp.main.view.IMainView
import moxy.InjectViewState
import moxy.MvpPresenter
import org.json.JSONObject
import kotlin.concurrent.thread

@InjectViewState
class MainPresenter : MvpPresenter<IMainView>() {

    private var handler: Handler = Handler(Looper.getMainLooper())

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
     * @param clothing данные о модели персонажа
     * @param
     */
    fun updateAPIConnection(
        context: Context,
        clothing: ClothingConfig,
        weather: WeatherConfig,
        city: String,
    ) {
        viewState.switchInfoVisibility(false)
        viewState.switchLoadingVisibility(true)
        viewState.title(context.getString(R.string.loading))

        handler.removeCallbacksAndMessages(null)
        thread {
            try {
                val weatherApi = WeatherApi(city)
                val json: JSONObject = weatherApi.data(weather.type)
                    ?: throw DataNotFoundException(context)
                val dayJSON: JSONObject = DataModel.weatherDay(json, weather.type)
                    ?: throw DataNotFoundException(context)

                val mainDataObject = dayJSON.getJSONObject("main")
                val windDataObject = dayJSON.getJSONObject("wind")
                val weatherData = Weather(
                    mainDataObject.getDouble("temp"),
                    windDataObject.getDouble("speed"),
                    mainDataObject.getDouble("humidity")
                )

                val clothes = Clothes(clothing)
                val perceivedTemp = weatherData.getTemperatureCelsiusPerception()

                val weatherDataArray = dayJSON.getJSONArray("weather")
                val iconName = weatherDataArray.getJSONObject(0).getString("icon")

                handler.post {
                    viewState.title(DataModel.title(weather.degree, weatherData))
                    viewState.setTextInfo(weatherData)
                    viewState.loadModel(clothes.clothesId(perceivedTemp))
                    viewState.loadIcon(weatherApi.iconUri(iconName))

                    viewState.switchLoadingVisibility(false)
                    viewState.switchInfoVisibility(true)
                }
            } catch (e: Exception) {
                handler.post {
                    e.message?.let {
                        if (e is DataNotFoundException) {
                            viewState.title(it)
                        }
                    }
                    viewState.switchLoadingVisibility(false)
                    viewState.switchInfoVisibility(false)
                }
            }
        }
    }
}