package devcom.premium.clothingandweather

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private var handler: Handler = Handler()
    private val clothes = Clothes()

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.CawTheme) // переход от темы .Launcher к обычной теме приложения
        super.onCreate(savedInstanceState)
        setOrientation()
        setContentView(R.layout.activity_main)
    }

    private fun setOrientation() {
        requestedOrientation = if (resources.configuration.screenLayout
            and Configuration.SCREENLAYOUT_SIZE_MASK == Configuration.SCREENLAYOUT_SIZE_LARGE ||
            resources.configuration.screenLayout
            and Configuration.SCREENLAYOUT_SIZE_MASK == Configuration.SCREENLAYOUT_SIZE_XLARGE
        )
            ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        else
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    override fun onStart() {
        super.onStart()

        loadDefaultModel()
        updateAPIConnection()
    }

    private fun loadDefaultModel() {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        val sex = sharedPref.getString("sex", "0")
        if (sex == "0") model.setImageResource(R.drawable.man_default)
        else if (sex == "1") model.setImageResource(R.drawable.woman_default)
    }

    private fun updateAPIConnection() {
        setTitle(R.string.waiting_for_network)
        setInfoInvisible()
        prBar.visibility = View.VISIBLE

        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)

        val sex = sharedPref.getString("sex", "0")
        val style = sharedPref.getString("style", "0")
        val city = sharedPref.getString("city", "Kemerovo, RU")
        val weatherDegree = sharedPref.getString("degree", "0").toInt()
        val weatherDate = sharedPref.getString("date", "0").toInt()

        object : Thread() {
            override fun run() {
                if (isNetworkAvailable()) {
                    setTitle(R.string.loading)

                    val weather = Weather(city)
                    val json: JSONObject? = weather.jsonObject(weatherDate)
                    if (json != null) {
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
                                    iconName = weatherDataArray.getJSONObject(0).getString("icon")
                                }
                                1 -> {
                                    val list = json.getJSONArray("list")
                                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                                    val currentDate = dateFormat.format(Calendar.getInstance().time)
                                    val cnt = json.getInt("cnt")
                                    for (i in 1 until cnt - 1) {
                                        if (list.getJSONObject(i).getString("dt_txt").contains("$currentDate 1") or
                                            list.getJSONObject(i).getString("dt_txt").contains("$currentDate 2")
                                        ) {
                                            val weatherDay = list.getJSONObject(i)
                                            mainDataObject = weatherDay.getJSONObject("main")
                                            weatherDataArray = weatherDay.getJSONArray("weather")
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
                                            weatherDataArray = weatherDay.getJSONArray("weather")
                                            windDataObject = weatherDay.getJSONObject("wind")
                                            iconName = weatherDataArray.getJSONObject(0)
                                                .getString("icon")
                                            break
                                        }
                                    }
                                }
                            }

                            if (mainDataObject != null) {
                                val temperature = mainDataObject.getDouble("temp")
                                title = when (weatherDegree) {
                                    1 -> // celsius
                                        "$temperature °C"
                                    2 -> // fahrenheit
                                        String.format(
                                            "%.1f",
                                            weather.convertCelsiusToFahrenheit(temperature)
                                        ).replace(
                                            ',', '.'
                                        ) + " °F"
                                    else -> // celsius and fahrenheit
                                        "$temperature °C / " + String.format(
                                            "%.1f",
                                            weather.convertCelsiusToFahrenheit(temperature)
                                        ).replace(
                                            ',', '.'
                                        ) + " °F"
                                }

                                var speed = windDataObject!!.getDouble("speed")
                                val humidity = mainDataObject.getDouble("humidity")
                                setTextInfo(speed, humidity)

                                if (speed != 0.0) speed *= 3.6 // *3.6 - перевод из м/ч в км/ч
                                val perceivedTemp =
                                    weather.coldWindIndex(temperature, speed, humidity)
                                model.setImageResource(
                                    clothes.clothesId(
                                        sex.toInt(),
                                        style.toInt(),
                                        perceivedTemp
                                    )
                                )

                                val urlIcon = weather.iconUrl(iconName)
                                try {
                                    Picasso.get().load(urlIcon.toString()).into(imageView_icon)
                                } catch (e: Exception) {
                                    Error("Error loading image!")
                                }

                                prBar.visibility = View.GONE
                                setInfoVisible()
                            }
                        }
                    } else {
                        handler.post {
                            setTitle(R.string.weather_data_not_found)
                            prBar.visibility = View.GONE
                            setInfoInvisible()
                        }
                    }
                } else {
                    prBar.visibility = View.GONE
                }
            }
        }.start()
    }

    private fun setInfoVisible() {
        textView_speed.visibility = View.VISIBLE
        textView_humidity.visibility = View.VISIBLE
        imageView_icon.visibility = View.VISIBLE
    }

    private fun setInfoInvisible() {
        textView_speed.visibility = View.INVISIBLE
        textView_humidity.visibility = View.INVISIBLE
        imageView_icon.visibility = View.INVISIBLE
    }

    private fun isNetworkAvailable(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = cm.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    private fun setTextInfo(speed: Double, humidity: Double) {
        val textSpeed = getString(R.string.wind) + " = " +
                speed + " " + getString(R.string.meter_sec)
        val textHumidity = getString(R.string.humidity) + " = " + humidity.toString() + "%"
        textView_speed.text = textSpeed
        textView_humidity.text = textHumidity
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.navigation, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.navigation_btnRefresh -> {
                updateAPIConnection()
                return true
            }
            R.id.navigation_btnPreferences -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.navigation_btnLocation -> {
                val intent = Intent(this, LocationActivity::class.java)
                startActivity(intent)
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }
}