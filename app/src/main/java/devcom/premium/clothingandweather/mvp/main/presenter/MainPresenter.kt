package devcom.premium.clothingandweather.mvp.main.presenter

import android.content.Context
import android.os.Handler
import android.os.Looper
import devcom.premium.clothingandweather.LocationActivity
import devcom.premium.clothingandweather.R
import devcom.premium.clothingandweather.SettingsActivity
import devcom.premium.clothingandweather.common.*
import devcom.premium.clothingandweather.data.WeatherApi
import devcom.premium.clothingandweather.mvp.main.view.IMainView
import devcom.premium.clothingandweather.mvp.model.DataModel
import moxy.InjectViewState
import moxy.MvpPresenter
import org.json.JSONObject

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
     * Обрабатывает нажатие на пункт меню с местоположением
     */
    fun onLaunchLocation() {
        viewState.launchActivity(LocationActivity::class.java)
    }

    /**
     * Обрабатывает нажатие на пункт меню с настройками
     */
    fun onLaunchPreferences() {
        viewState.launchActivity(SettingsActivity::class.java)
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
        object : Thread() {
            override fun run() {
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
                        viewState.loadIcon(weatherApi.iconUrl(iconName))

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
        }.start()
    }
}