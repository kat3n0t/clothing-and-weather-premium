package devcom.premium.clothingandweather.mvp.main.view

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.net.Network
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.annotation.DrawableRes
import com.bumptech.glide.Glide
import devcom.premium.clothingandweather.LocationActivity
import devcom.premium.clothingandweather.R
import devcom.premium.clothingandweather.SettingsActivity
import devcom.premium.clothingandweather.common.ConnectionStateMonitor
import devcom.premium.clothingandweather.common.IntExtensions
import devcom.premium.clothingandweather.data.ClothingConfig
import devcom.premium.clothingandweather.data.DataModel
import devcom.premium.clothingandweather.data.WeatherConfig
import devcom.premium.clothingandweather.data.storage.ConstStorage
import devcom.premium.clothingandweather.data.storage.PreferencesStorage
import devcom.premium.clothingandweather.databinding.ActivityMainBinding
import devcom.premium.clothingandweather.domain.Gender
import devcom.premium.clothingandweather.domain.Weather
import devcom.premium.clothingandweather.mvp.ABaseMvpActivity
import devcom.premium.clothingandweather.mvp.main.presenter.MainPresenter
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import org.koin.android.ext.android.get

class MainActivity : ABaseMvpActivity(), IMainView {

    @InjectPresenter
    internal lateinit var presenter: MainPresenter

    @ProvidePresenter
    fun providePresenter(): MainPresenter = get()

    private lateinit var storage: PreferencesStorage

    private val binding: ActivityMainBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    private val connectMonitor by lazy {
        object : ConnectionStateMonitor(this@MainActivity) {
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

        val view = binding.root
        setContentView(view)

        storage = PreferencesStorage(this)
    }

    override fun onStart() {
        super.onStart()
        connectMonitor.enable()
    }

    override fun onStop() {
        connectMonitor.disable()
        super.onStop()
    }

    override fun showDefaultModel() {
        val genderPref =
            storage.value(ConstStorage.TITLE_GENDER, ConstStorage.DEFAULT_VALUE)!!.toInt()
        val gender = Gender.values()[genderPref]
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

    // todo: тут и проверка на not-null, и завершение без обновления в случае ошибки.
    //  Убрать !!, прокинуть в презентер потенциально неполные данные и обработать дальнейшее развитие событий на уровне презентера
    override fun updateWeatherData() {
        if (!connectMonitor.networkAvailable()) {
            setTitle(R.string.waiting_for_network)
            return
        }

        val genderPref =
            storage.value(ConstStorage.TITLE_GENDER, ConstStorage.DEFAULT_VALUE)!!.toInt()
        val gender = IntExtensions.toGender(genderPref) ?: return

        val stylePref =
            storage.value(ConstStorage.TITLE_STYLE, ConstStorage.DEFAULT_VALUE)!!.toInt()
        val style = IntExtensions.toStyle(stylePref) ?: return

        val weatherDegreePref =
            storage.value(ConstStorage.TITLE_DEGREE, ConstStorage.DEFAULT_VALUE)!!.toInt()
        val weatherDegree = IntExtensions.toDegree(weatherDegreePref) ?: return

        val weatherTypePref =
            storage.value(ConstStorage.TITLE_DATE, ConstStorage.DEFAULT_VALUE)!!.toInt()
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
                startActivity<SettingsActivity>()
                true
            }
            R.id.navigation_btnLocation -> {
                startActivity<LocationActivity>()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
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
}