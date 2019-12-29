package devcom.premium.clothingandweather.mvp.main.presenter

import android.os.Handler
import android.preference.PreferenceManager
import android.view.View
import devcom.premium.clothingandweather.LocationActivity
import devcom.premium.clothingandweather.R
import devcom.premium.clothingandweather.SettingsActivity
import devcom.premium.clothingandweather.common.Clothes
import devcom.premium.clothingandweather.common.Human
import devcom.premium.clothingandweather.common.Weather
import devcom.premium.clothingandweather.data.WeatherApi
import devcom.premium.clothingandweather.mvp.main.view.IMainView
import devcom.premium.clothingandweather.mvp.main.view.MainActivity
import devcom.premium.clothingandweather.mvp.model.DataModel
import kotlinx.android.synthetic.main.activity_main.*
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
        viewState.updateAPIConnection()
    }

    fun onLaunchLocation() {
        viewState.launchActivity(LocationActivity::class.java)
    }

    fun onLaunchPreferences() {
        viewState.launchActivity(SettingsActivity::class.java)
    }

    fun updateAPIConnection(activity: MainActivity) {
        viewState.switchInfoVisible(false)
        activity.prBar.visibility = View.VISIBLE

        val sharedPref = PreferenceManager.getDefaultSharedPreferences(activity)

        val human = Human(
            sharedPref.getString("sex", "0").toInt(),
            sharedPref.getString("style", "0").toInt()
        )
        val city: String = sharedPref.getString("city", DEFAULT_CITY)
        val weatherDegree = sharedPref.getString("degree", "0").toInt()
        val weatherDate = sharedPref.getString("date", "0").toInt()

        object : Thread() {
            override fun run() {
                try {
                    activity.setTitle(R.string.loading)

                    val weatherApi = WeatherApi(city)
                    val json: JSONObject = weatherApi.data(weatherDate)
                        ?: throw Exception(activity.getString(R.string.weather_data_not_found))

                    handler.post {
                        val dayJSON: JSONObject = DataModel.weatherDay(json, weatherDate)
                            ?: throw Exception(activity.getString(R.string.weather_data_not_found))

                        val mainDataObject = dayJSON.getJSONObject("main")
                        val windDataObject = dayJSON.getJSONObject("wind")
                        val weather = Weather(
                            mainDataObject.getDouble("temp"),
                            windDataObject.getDouble("speed"),
                            mainDataObject.getDouble("humidity")
                        )

                        activity.title = DataModel.title(weatherDegree, weather)
                        viewState.setTextInfo(weather)

                        val perceivedTemp = weather.getTemperatureCelsiusPerception()
                        viewState.loadModel(clothes.clothesId(human, perceivedTemp))

                        val weatherDataArray = dayJSON.getJSONArray("weather")
                        val iconName = weatherDataArray.getJSONObject(0).getString("icon")
                        viewState.loadIcon(weatherApi.iconUrl(iconName))

                        activity.prBar.visibility = View.GONE
                        viewState.switchInfoVisible(true)
                    }
                } catch (e: Exception) {
                    activity.title = e.message
                    activity.prBar.visibility = View.GONE
                    viewState.switchInfoVisible(false)
                }
            }
        }.start()
    }
}