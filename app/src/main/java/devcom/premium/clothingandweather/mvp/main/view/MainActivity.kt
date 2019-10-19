package devcom.premium.clothingandweather.mvp.main.view

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import devcom.premium.clothingandweather.R
import devcom.premium.clothingandweather.common.Weather
import devcom.premium.clothingandweather.mvp.main.presenter.IMainPresenter
import devcom.premium.clothingandweather.mvp.main.presenter.MainPresenter
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), IMainView {

    private val presenter: IMainPresenter = MainPresenter(this)

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
        presenter.onStart()
    }

    override fun showDefaultModel() {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        val sex = sharedPref.getString("sex", "0")
        if (sex == "0") model.setImageResource(R.drawable.man_default)
        else if (sex == "1") model.setImageResource(R.drawable.woman_default)
    }

    override fun switchInfoVisible(isCanVisible: Boolean) {
        val visibility = if (isCanVisible) View.VISIBLE else View.INVISIBLE

        textView_speed.visibility = visibility
        textView_humidity.visibility = visibility
        imageView_icon.visibility = visibility
    }

    fun isNetworkAvailable(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = cm.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    override fun setTextInfo(weather: Weather) {
        val textSpeed = getString(R.string.wind) + " = " +
                weather.windSpeed + " " + getString(R.string.meter_sec)
        val textHumidity = getString(R.string.humidity) + " = " + weather.humidity.toString() + "%"
        textView_speed.text = textSpeed
        textView_humidity.text = textHumidity
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.navigation, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.navigation_btnRefresh -> {
                presenter.updateAPIConnection()
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
}