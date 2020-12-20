package devcom.premium.clothingandweather

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils
import devcom.premium.clothingandweather.common.storage.PreferencesStorage
import devcom.premium.clothingandweather.common.storage.ConstStorage
import kotlinx.android.synthetic.main.activity_location.*

class LocationActivity : AppCompatActivity() {

    private lateinit var storage: PreferencesStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        storage = PreferencesStorage(this)
    }

    override fun onStart() {
        super.onStart()
        btnCheck.setOnClickListener { onSaveLocation() }
        showCurrentCity()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                NavUtils.navigateUpFromSameTask(this)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Отображает текущий город
     */
    private fun showCurrentCity() {
        val city = storage.value(ConstStorage.TITLE_CITY, ConstStorage.DEFAULT_CITY)!!
        tvActiveCity.text = city
    }

    /**
     * Обрабатывает нажатие на кнопку сохранения
     */
    private fun onSaveLocation() {
        tvCheck.visibility = View.GONE
        val text = etCity.text.toString()
        if (text != "") {
            storage.putString(ConstStorage.TITLE_CITY, transformTextFirstCharToUpperCase(text))
            showCurrentCity()
        } else tvCheck.visibility = View.VISIBLE
    }

    /**
     * Делает первую букву текста заглавной
     */
    private fun transformTextFirstCharToUpperCase(text: String) = if (text != "") {
        text.substring(0, 1).toUpperCase() + text.substring(1)
    } else ""
}