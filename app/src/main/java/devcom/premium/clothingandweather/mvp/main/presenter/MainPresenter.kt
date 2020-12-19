package devcom.premium.clothingandweather.mvp.main.presenter

import android.content.Context
import android.os.Handler
import android.preference.PreferenceManager
import devcom.premium.clothingandweather.LocationActivity
import devcom.premium.clothingandweather.R
import devcom.premium.clothingandweather.SettingsActivity
import devcom.premium.clothingandweather.common.Clothes
import devcom.premium.clothingandweather.common.Human
import devcom.premium.clothingandweather.common.IntExtensions
import devcom.premium.clothingandweather.common.Weather
import devcom.premium.clothingandweather.data.WeatherApi
import devcom.premium.clothingandweather.mvp.main.view.IMainView
import devcom.premium.clothingandweather.mvp.model.DataModel
import moxy.InjectViewState
import moxy.MvpPresenter
import org.json.JSONObject

private const val DEFAULT_CITY = "Kemerovo, RU"

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

    override fun detachView(view: IMainView?) {
        handler.removeCallbacksAndMessages(null)
        super.detachView(view)
    }

    /**
     * Обрабатывает при обновлении соединения
     */
    fun updateAPIConnection(context: Context) {
        viewState.switchInfoVisibility(false)
        viewState.switchLoadingVisibility(true)

        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)

        val sexPref = sharedPref.getString("sex", "0")!!.toInt()
        val sex = IntExtensions.toGender(sexPref) ?: return

        val stylePref = sharedPref.getString("style", "0")!!.toInt()
        val style = IntExtensions.toStyle(stylePref) ?: return

        val weatherDegreePref = sharedPref.getString("degree", "0")!!.toInt()
        val weatherDegree = IntExtensions.toDegree(weatherDegreePref) ?: return

        val weatherTypePref = sharedPref.getString("date", "0")!!.toInt()
        val weatherType = IntExtensions.toWeatherType(weatherTypePref) ?: return

        val city: String = sharedPref.getString("city", DEFAULT_CITY)!!

        object : Thread() {
            override fun run() {
                try {
                    val weatherApi = WeatherApi(city)
                    val json: JSONObject = weatherApi.data(weatherType)
                        ?: throw Exception(context.getString(R.string.weather_data_not_found))

                    handler.post {
                        viewState.title(context.getString(R.string.loading))

                        val dayJSON: JSONObject = DataModel.weatherDay(json, weatherType)
                            ?: throw Exception(context.getString(R.string.weather_data_not_found))

                        val mainDataObject = dayJSON.getJSONObject("main")
                        val windDataObject = dayJSON.getJSONObject("wind")
                        val weather = Weather(
                            mainDataObject.getDouble("temp"),
                            windDataObject.getDouble("speed"),
                            mainDataObject.getDouble("humidity")
                        )

                        viewState.title(DataModel.title(weatherDegree, weather))
                        viewState.setTextInfo(weather)

                        val human = Human(sex, style)
                        val perceivedTemp = weather.getTemperatureCelsiusPerception()
                        viewState.loadModel(clothes.clothesId(human, perceivedTemp))

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