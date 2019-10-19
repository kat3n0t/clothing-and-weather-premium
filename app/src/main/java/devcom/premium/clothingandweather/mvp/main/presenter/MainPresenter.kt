package devcom.premium.clothingandweather.mvp.main.presenter

import android.os.Handler
import android.preference.PreferenceManager
import android.view.View
import androidx.annotation.NonNull
import com.squareup.picasso.Picasso
import devcom.premium.clothingandweather.LocationActivity
import devcom.premium.clothingandweather.R
import devcom.premium.clothingandweather.SettingsActivity
import devcom.premium.clothingandweather.common.Clothes
import devcom.premium.clothingandweather.common.Human
import devcom.premium.clothingandweather.common.Weather
import devcom.premium.clothingandweather.data.WeatherApi
import devcom.premium.clothingandweather.mvp.main.view.MainActivity
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

private const val DEFAULT_CITY = "Kemerovo, RU"

class MainPresenter(@NonNull private val activity: MainActivity) : IMainPresenter {

    private var handler: Handler = Handler()
    private val clothes = Clothes()

    override fun onStart() {
        activity.showDefaultModel()
        updateAPIConnection()
    }

    override fun onLaunchLocation() {
        activity.launchActivity(LocationActivity::class.java)
    }

    override fun onLaunchPreferences() {
        activity.launchActivity(SettingsActivity::class.java)
    }

    override fun updateAPIConnection() {
        activity.setTitle(R.string.waiting_for_network)
        activity.switchInfoVisible(false)
        activity.prBar.visibility = View.VISIBLE

        val sharedPref = PreferenceManager.getDefaultSharedPreferences(activity)

        val human = Human(
            sharedPref.getString("sex", "0").toInt(),
            sharedPref.getString("style", "0").toInt()
        )
        val city = sharedPref.getString("city", DEFAULT_CITY)
        val weatherDegree = sharedPref.getString("degree", "0").toInt()
        val weatherDate = sharedPref.getString("date", "0").toInt()

        object : Thread() {
            override fun run() {
                if (activity.isNetworkAvailable()) {
                    try {
                        activity.setTitle(R.string.loading)

                        val weatherApi = WeatherApi(city)
                        val json: JSONObject = weatherApi.jsonObject(weatherDate)
                            ?: throw Exception(activity.getString(R.string.weather_data_not_found))

                        handler.post {
                            var mainDataObject: JSONObject? = null
                            var windDataObject: JSONObject? = null
                            var weatherDataArray: JSONArray?
                            var iconName = ""

                            when (weatherDate) {
                                0 -> {
                                    mainDataObject = json.getJSONObject("main")
                                    weatherDataArray = json.getJSONArray("weather")
                                    windDataObject = json.getJSONObject("wind")
                                    iconName =
                                        weatherDataArray.getJSONObject(0).getString("icon")
                                }
                                1 -> {
                                    val list = json.getJSONArray("list")
                                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                                    val currentDate =
                                        dateFormat.format(Calendar.getInstance().time)
                                    val cnt = json.getInt("cnt")
                                    for (i in 1 until cnt - 1) {
                                        if (list.getJSONObject(i).getString("dt_txt").contains("$currentDate 1") or
                                            list.getJSONObject(i).getString("dt_txt").contains("$currentDate 2")
                                        ) {
                                            val weatherDay = list.getJSONObject(i)
                                            mainDataObject = weatherDay.getJSONObject("main")
                                            weatherDataArray =
                                                weatherDay.getJSONArray("weather")
                                            windDataObject = weatherDay.getJSONObject("wind")
                                            iconName = weatherDataArray.getJSONObject(0)
                                                .getString("icon")
                                            break
                                        }
                                    }
                                }
                                2 -> {
                                    val list = json.getJSONArray("list")
                                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                                    val calendarTomorrow = Calendar.getInstance()
                                    calendarTomorrow.add(Calendar.DATE, 1)
                                    val tomorrowDate = dateFormat.format(calendarTomorrow.time)
                                    val cnt = json.getInt("cnt")
                                    for (i in 1 until cnt) {
                                        if (list.getJSONObject(i).getString("dt_txt").contains("$tomorrowDate 1") or
                                            list.getJSONObject(i).getString("dt_txt").contains("$tomorrowDate 2")
                                        ) {
                                            val weatherDay = list.getJSONObject(i)
                                            mainDataObject = weatherDay.getJSONObject("main")
                                            weatherDataArray =
                                                weatherDay.getJSONArray("weather")
                                            windDataObject = weatherDay.getJSONObject("wind")
                                            iconName = weatherDataArray.getJSONObject(0)
                                                .getString("icon")
                                            break
                                        }
                                    }
                                }
                            }

                            if ((mainDataObject == null) || (windDataObject == null))
                                throw Exception(activity.getString(R.string.weather_data_not_found))

                            val weather = Weather(
                                mainDataObject.getDouble("temp"),
                                windDataObject.getDouble("speed"),
                                mainDataObject.getDouble("humidity")
                            )
                            activity.setTextInfo(weather)

                            activity.title = when (weatherDegree) {
                                1 -> // celsius
                                    "${weather.temperatureCelsius} °C"
                                2 -> // fahrenheit
                                    String.format(
                                        "%.1f",
                                        weather.temperatureFahrenheit
                                    ).replace(',', '.') + " °F"
                                else -> // celsius and fahrenheit
                                    "${weather.temperatureCelsius} °C / " + String.format(
                                        "%.1f",
                                        weather.temperatureFahrenheit
                                    ).replace(
                                        ',', '.'
                                    ) + " °F"
                            }

                            val perceivedTemp = weather.getTemperatureCelsiusPerception()
                            activity.model.setImageResource(clothes.clothesId(human, perceivedTemp))

                            val urlIcon = weatherApi.iconUrl(iconName)
                            Picasso.get().load(urlIcon.toString()).into(activity.imageView_icon)

                            activity.prBar.visibility = View.GONE
                            activity.switchInfoVisible(true)
                        }
                    } catch (e: Exception) {
                        activity.title = e.message
                        activity.prBar.visibility = View.GONE
                        activity.switchInfoVisible(false)
                    }
                } else {
                    activity.prBar.visibility = View.GONE
                }
            }
        }.start()
    }
}