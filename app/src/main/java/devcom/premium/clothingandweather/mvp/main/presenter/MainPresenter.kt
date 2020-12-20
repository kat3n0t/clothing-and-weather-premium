package devcom.premium.clothingandweather.mvp.main.presenter

import android.content.Context
import android.os.Handler
import devcom.premium.clothingandweather.LocationActivity
import devcom.premium.clothingandweather.R
import devcom.premium.clothingandweather.SettingsActivity
import devcom.premium.clothingandweather.common.Clothes
import devcom.premium.clothingandweather.common.ClothingConfig
import devcom.premium.clothingandweather.common.Weather
import devcom.premium.clothingandweather.common.WeatherConfig
import devcom.premium.clothingandweather.data.WeatherApi
import devcom.premium.clothingandweather.mvp.main.view.IMainView
import devcom.premium.clothingandweather.mvp.model.DataModel
import moxy.InjectViewState
import moxy.MvpPresenter
import org.json.JSONObject

@InjectViewState
class MainPresenter : MvpPresenter<IMainView>() {

    private var handler: Handler = Handler()
    private val clothes = Clothes()

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        viewState.showDefaultModel()
    }

    override fun attachView(view: IMainView?) {
        super.attachView(view)
        viewState.updateWeatherData()
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

        object : Thread() {
            override fun run() {
                try {
                    val weatherApi = WeatherApi(city)
                    val json: JSONObject = weatherApi.data(weather.type)
                        ?: throw Exception(context.getString(R.string.weather_data_not_found))

                    handler.post {
                        viewState.title(context.getString(R.string.loading))

                        val dayJSON: JSONObject = DataModel.weatherDay(json, weather.type)
                            ?: throw Exception(context.getString(R.string.weather_data_not_found))

                        val mainDataObject = dayJSON.getJSONObject("main")
                        val windDataObject = dayJSON.getJSONObject("wind")
                        val weatherData = Weather(
                            mainDataObject.getDouble("temp"),
                            windDataObject.getDouble("speed"),
                            mainDataObject.getDouble("humidity")
                        )

                        viewState.title(DataModel.title(weather.degree, weatherData))
                        viewState.setTextInfo(weatherData)

                        val perceivedTemp = weatherData.getTemperatureCelsiusPerception()
                        viewState.loadModel(clothes.clothesId(clothing, perceivedTemp))

                        val weatherDataArray = dayJSON.getJSONArray("weather")
                        val iconName = weatherDataArray.getJSONObject(0).getString("icon")
                        viewState.loadIcon(weatherApi.iconUrl(iconName))

                        viewState.switchLoadingVisibility(false)
                        viewState.switchInfoVisibility(true)
                    }
                } catch (e: Exception) {
                    handler.removeCallbacksAndMessages(null)
                    handler.post {
                        viewState.switchLoadingVisibility(false)
                        viewState.switchInfoVisibility(false)
                        val dataNotFoundException =
                            context.getString(R.string.weather_data_not_found)
                        if (e.message.equals(dataNotFoundException)) {
                            viewState.title(dataNotFoundException)
                        }
                    }
                }
            }
        }.start()
    }
}