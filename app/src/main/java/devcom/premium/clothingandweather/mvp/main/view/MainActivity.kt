package devcom.premium.clothingandweather.mvp.main.view

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.annotation.DrawableRes
import com.bumptech.glide.Glide
import devcom.premium.clothingandweather.R
import devcom.premium.clothingandweather.common.Weather
import devcom.premium.clothingandweather.mvp.main.presenter.MainPresenter
import devcom.premium.clothingandweather.mvp.model.DataModel
import kotlinx.android.synthetic.main.activity_main.*
import moxy.MvpAppCompatActivity
import moxy.presenter.InjectPresenter

class MainActivity : MvpAppCompatActivity(), IMainView {

    @InjectPresenter
    internal lateinit var presenter: MainPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.CawTheme) // переход от темы .Launcher к обычной теме приложения
        super.onCreate(savedInstanceState)
        setOrientation()
        setContentView(R.layout.activity_main)
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

    override fun showDefaultModel() {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        val sex = sharedPref.getString("sex", "0")
        loadModel(if (sex == "0") R.drawable.man_default else R.drawable.woman_default)
    }

    override fun switchInfoVisibility(canVisible: Boolean) {
        val visibility = if (canVisible) View.VISIBLE else View.INVISIBLE

        textView_speed.visibility = visibility
        textView_humidity.visibility = visibility
        imageView_icon.visibility = visibility
    }

    override fun switchLoadingVisibility(canVisible: Boolean) {
        val visibility = if (canVisible) View.VISIBLE else View.GONE
        prBar.visibility = visibility
    }

    override fun title(title: String) {
        this.title = title
    }

    override fun updateWeatherData() {
        if (canNetworkAvailable()) {
            presenter.updateAPIConnection(this)
        } else {
            setTitle(R.string.waiting_for_network)
        }
    }

    /**
     * Проверяет доступ к интернету
     *
     * @return истина, если есть интернет
     */
    private fun canNetworkAvailable(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = cm.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    override fun setTextInfo(weather: Weather) {
        textView_speed.text = DataModel.infoWindSpeed(this, weather.windSpeed)
        textView_humidity.text = DataModel.infoHumidity(this, weather.humidity)
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
        Glide.with(this).load(iconUri).into(imageView_icon)
    }

    override fun loadModel(@DrawableRes id: Int) {
        model.setImageResource(id)
    }
}