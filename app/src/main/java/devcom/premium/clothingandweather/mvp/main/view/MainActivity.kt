package devcom.premium.clothingandweather.mvp.main.view

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.net.*
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.annotation.DrawableRes
import com.bumptech.glide.Glide
import devcom.premium.clothingandweather.R
import devcom.premium.clothingandweather.common.*
import devcom.premium.clothingandweather.common.storage.ConstStorage
import devcom.premium.clothingandweather.common.storage.PreferencesStorage
import devcom.premium.clothingandweather.databinding.ActivityMainBinding
import devcom.premium.clothingandweather.mvp.main.presenter.MainPresenter
import devcom.premium.clothingandweather.mvp.model.DataModel
import moxy.MvpAppCompatActivity
import moxy.presenter.InjectPresenter

class MainActivity : MvpAppCompatActivity(), IMainView {

    @InjectPresenter
    internal lateinit var presenter: MainPresenter

    private lateinit var binding: ActivityMainBinding

    private lateinit var storage: PreferencesStorage

    private val connectMonitor by lazy {
        object : ConnectionStateMonitor() {
            override fun onAvailable(network: Network) {
                runOnUiThread {
                    updateWeatherData()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.CawTheme) // переход от темы .Launcher к обычной теме приложения
        super.onCreate(savedInstanceState)

        setOrientation()

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        storage = PreferencesStorage(this)
        connectMonitor.enable(this)
    }

    override fun showDefaultModel() {
        val genderPref = storage.value(ConstStorage.TITLE_GENDER, "0")!!.toInt()
        val gender = IntExtensions.toGender(genderPref)
        loadModel(if (gender == Gender.MAN) R.drawable.man_default else R.drawable.woman_default)
    }

    override fun switchInfoVisibility(canVisible: Boolean) {
        binding.apply {
            val visibility = if (canVisible) View.VISIBLE else View.INVISIBLE

            tvSpeed.visibility = visibility
            tvHumidity.visibility = visibility
            ivIcon.visibility = visibility
        }
    }

    override fun switchLoadingVisibility(canVisible: Boolean) {
        val visibility = if (canVisible) View.VISIBLE else View.GONE
        binding.prBar.visibility = visibility
    }

    override fun title(title: String) {
        this.title = title
    }

    override fun updateWeatherData() {
        if (!networkAvailable()) {
            setTitle(R.string.waiting_for_network)
            return
        }

        val genderPref = storage.value(ConstStorage.TITLE_GENDER, "0")!!.toInt()
        val gender = IntExtensions.toGender(genderPref) ?: return

        val stylePref = storage.value(ConstStorage.TITLE_STYLE, "0")!!.toInt()
        val style = IntExtensions.toStyle(stylePref) ?: return

        val weatherDegreePref = storage.value(ConstStorage.TITLE_DEGREE, "0")!!.toInt()
        val weatherDegree = IntExtensions.toDegree(weatherDegreePref) ?: return

        val weatherTypePref = storage.value(ConstStorage.TITLE_DATE, "0")!!.toInt()
        val weatherType = IntExtensions.toWeatherType(weatherTypePref) ?: return

        val clothing = ClothingConfig(gender, style)
        val weather = WeatherConfig(weatherDegree, weatherType)

        val city = storage.value(ConstStorage.TITLE_CITY, ConstStorage.DEFAULT_CITY)!!

        presenter.updateAPIConnection(this, clothing, weather, city)
    }

    override fun setTextInfo(weather: Weather) {
        binding.apply {
            tvSpeed.text = DataModel.infoWindSpeed(this@MainActivity, weather.windSpeed)
            tvHumidity.text = DataModel.infoHumidity(this@MainActivity, weather.humidity)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.navigation, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.navigation_btnRefresh -> {
                updateWeatherData()
                true
            }
            R.id.navigation_btnPreferences -> {
                presenter.onLaunchPreferences()
                true
            }
            R.id.navigation_btnLocation -> {
                presenter.onLaunchLocation()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun launchActivity(targetClass: Class<*>) {
        val intent = Intent(this, targetClass)
        startActivity(intent)
    }

    override fun loadIcon(iconUri: Uri) {
        Glide.with(this).load(iconUri).into(binding.ivIcon)
    }

    override fun loadModel(@DrawableRes id: Int) {
        binding.ivModel.setImageResource(id)
    }

    /**
     * Устанавливает ориентацию экрана
     */
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

    /**
     * Проверяет доступность интернет-соединения
     */
    private fun networkAvailable(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val networkCapabilities = connectivityManager.activeNetwork ?: return false
            val actNw =
                connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
            return when {
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            @Suppress("DEPRECATION")
            run {
                val networkInfo = connectivityManager.activeNetworkInfo
                return networkInfo != null && networkInfo.isConnected
            }
        }
    }
}